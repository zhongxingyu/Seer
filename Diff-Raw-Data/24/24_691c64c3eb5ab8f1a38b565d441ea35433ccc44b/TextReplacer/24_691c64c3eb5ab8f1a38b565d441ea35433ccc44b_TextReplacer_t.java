 /*
  * ePUB Corrector - https://github.com/vysokyj/epub-corrector/
  *
  * Copyright (C) 2012 Jiri Vysoky
  *
  * ePUB Corrector is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published
  * by the Free Software Foundation; either version 3 of the License,
  * or (at your option) any later version.
  *
  * ePUB Corrector is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with Cobertura; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
  * USA
  */
 
 package name.vysoky.epub;
 
 import name.vysoky.re.Replacement;
 import name.vysoky.re.ReplacementProvider;
import org.apache.commons.lang.StringEscapeUtils;
 
 import java.io.IOException;
 import java.util.List;
 
 /**
  * Modify string using sequence of replacing expressions.
  */
 @SuppressWarnings("unused")
 public class TextReplacer implements TextProcessor {
 
     private List<Replacement> replacements;
 
     public TextReplacer(ReplacementProvider loader) throws IOException {
         replacements = loader.getReplacements();
     }
 
     /**
      * Modify string using regular expression.
     * @param input input string
      * @return modified output string
      */
    public String process(String input) {
        if (input.replace("\n", "").trim().isEmpty()) return input; // skip empty lines
        input = StringEscapeUtils.unescapeHtml(input); // unescape before
        //input = StringEscapeUtils.escapeHtml(input);
        for (Replacement replacement : replacements) input = replacement.apply(input);
        input = StringEscapeUtils.unescapeHtml(input); // unescape after
        input = input.replace(" ", "&nbsp;"); // fix non breaking space - always escaped
        return input;
     }
 }
