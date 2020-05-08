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
 
 package name.vysoky.re;
 
 import org.apache.commons.lang.StringUtils;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * Parse and store one regular expression from RE file.
  * Parse regular expression used for replacing texts.
  */
 @SuppressWarnings("unused")
 public class Replacement {
 
     private String comment;
     private String expression;
     private Pattern pattern;
     private String replacement;
 
     public Replacement(String comment, String expression) throws IllegalArgumentException {
         this.comment = comment;
         this.expression = expression;
         parseExpression();
     }
 
     public String getComment() {
         return comment;
     }
 
     public String getExpression() {
         return expression;
     }
 
     public Pattern getPattern() {
         return pattern;
     }
 
     public String getReplacement() {
         return replacement;
     }
 
     public String apply(String string) {
         Matcher matcher;
         String oldString;
         do {
             oldString = string;
             matcher = pattern.matcher(string);
             string = matcher.replaceAll(replacement);
         } while(!string.equals(oldString));
         return string;
     }
 
     private void parseExpression() throws IllegalArgumentException {
         String[] a = StringUtils.split(expression, '/');
        if (a.length != 4) throw new IllegalArgumentException("Invalid format of regular expression! expression='" + expression + "'");
         pattern = Pattern.compile(a[1]);
         replacement = a[2];
     }
 }
