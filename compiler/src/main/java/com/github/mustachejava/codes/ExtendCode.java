package com.github.mustachejava.codes;

import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import com.github.mustachejava.Code;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheException;
import com.github.mustachejava.MustacheFactory;

/**
 * Extending a template.
 * <p/>
 * User: sam
 * Date: 11/27/11
 * Time: 10:39 AM
 */
public class ExtendCode extends PartialCode {

  private MustacheFactory mf;

  public ExtendCode(DefaultMustacheFactory mf, Code[] codes, String name, String file, String sm, String em) throws MustacheException {
    super(mf, codes, "<", name, file, sm, em);
    this.mf = mf;
  }

  private void replaceCode(Code[] supercodes, Map<String, ExtendNameCode> replaceMap) {
    for (int i = 0; i < supercodes.length; i++) {
      Code code = supercodes[i];
      if (code instanceof ExtendNameCode) {
        ExtendNameCode enc = (ExtendNameCode) code;
        ExtendNameCode extendReplaceCode = replaceMap.get(enc.getName());
        if (extendReplaceCode != null) {
          supercodes[i] = extendReplaceCode;
          extendReplaceCode.appended = enc.appended;
        } else {
          replaceCode(enc.getCodes(), replaceMap);
        }
      } else {
        Code[] codes = code.getCodes();
        if (codes != null) {
          replaceCode(codes, replaceMap);
        }
      }
    }
  }

  @Override
  public Writer execute(Writer writer, Object... scopes) throws MustacheException {
    if (partial == null) {
      Map<String, ExtendNameCode> replaceMap = new HashMap<String, ExtendNameCode>();
      for (Code code : codes) {
        if (code instanceof ExtendNameCode) {
          // put name codes in the map
          ExtendNameCode erc = (ExtendNameCode) code;
          replaceMap.put(erc.getName(), erc);
        } else if (code instanceof WriteCode) {
          // ignore text
        } else {
          // fail on everything else
          throw new IllegalArgumentException(
                  "Illegal code in extend section: " + code.getClass().getName());
        }
      }
      partial = mf.compile(name + extension);
      Code[] supercodes = partial.getCodes();
      // recursively replace named sections with replacements
      replaceCode(supercodes, replaceMap);
    }
    return partialExecute(writer, scopes);
  }

}
