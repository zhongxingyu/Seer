 package org.paxle.parser.html.impl;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /* From YaCy SVN 3802 */
 public class HtmlTools {
 
     /** Replaces special characters from a string. Avoids XSS attacks and ensures correct display of
       * special characters in non UTF-8 capable browsers.
       * @param text a string that possibly contains HTML
       * @return the string with all special characters encoded
       */
       //[MN]
     public static String replaceHTML(String text) {
         text = replace(text, xmlentities);
         text = replace(text, htmlentities);
         return text;
     }
 
     /** Replaces special characters from a string. Ensures correct display of
       * special characters in non UTF-8 capable browsers.
       * @param text a string that possibly contains special characters
       * @return the string with all special characters encoded
       */
       //[MN]
     public static String replaceHTMLEntities(String text) {
         text = replace(text, htmlentities);
         return text;
     }
 
     /** Replaces special characters from a string. Avoids XSS attacks.
       * @param text a string that possibly contains HTML
       * @return the string without any HTML-tags that can be used for XSS
       */
       //[MN]
     public static String replaceXMLEntities(String text) {
         text = replace(text, xmlentities);
         return text;
     }
 
     /** Replaces characters in a string with other characters defined in an array.
       * @param text a string that possibly contains special characters
       * @param entities array that contains characters to be replaced and characters it will be replaced by
       * @return the string with all characters replaced by the corresponding character from array
       */
       //[FB], changes by [MN]
     public static String replace(String text, String[] entities) {
         if (text==null) { return null; }
         for (int x=0;x<=entities.length-1;x=x+2) {
             int p=0;
             while ((p=text.indexOf(entities[x],p))>=0) {
                 text=text.substring(0,p)+entities[x+1]+text.substring(p+entities[x].length());
                 p+=entities[x+1].length();
             }
         }
         return text;
     }
     
     public static String deReplaceHTML(String text) {
     	text = deReplaceNumericEntities(text);
         text = deReplaceHTMLEntities(text);
         text = deReplaceXMLEntities(text);
         return text;
     }
     
     public static String deReplaceHTMLEntities(String text) {
         return deReplace(text, htmlentities);
     }
     
     public static String deReplaceXMLEntities(String text) {
         return deReplace(text, xmlentities);
     }
     
     public static String deReplace(String text, String[] entities) {
         if (text == null) return null;
         for (int i=entities.length-1; i>0; i-=2) {
             int p = 0;
             while ((p = text.indexOf(entities[i])) >= 0) {
                 text = text.substring(0, p) + entities[i - 1] + text.substring(p + entities[i].length());
                 p += entities[i - 1].length();
             }
         }
         return text;
     }
     
 	private static final Pattern NumericEntityPattern = Pattern.compile("&#((\\d+)|(x)([0-9a-fA-F]{4}));");
 	
 	// TODO: still fails at constructs like '&#x0301;e' - a possible representation of '&eacute;' respectively '&#233;'
 	// [FB]
     public static String deReplaceNumericEntities(String text) {
     	if (text == null) return null;
    	if (text.length() == 0) return text;
     	final Matcher m = NumericEntityPattern.matcher(text);
     	final StringBuffer sb = new StringBuffer(text.length());
 		while (m.find()) {
 			final String repl;
 			final int radix;
 			if (m.group(3) == null) {
 				radix = 10;
 				repl = m.group(2);
 			} else {
 				radix = 16;
 				repl = m.group(4);
 			}
     		m.appendReplacement(sb, String.valueOf((char)Integer.parseInt(repl, radix)));
     	}
 		m.appendTail(sb);
 		return sb.toString();
     }
 
     //This array contains codes (see http://mindprod.com/jgloss/unicode.html for details) 
     //that will be replaced. To add new codes or patterns, just put them at the end
     //of the list. Codes or patterns in this list can not be escaped with [= or <pre>
     public static final String[] xmlentities={
         // Ampersands _have_ to be replaced first. If they were replaced later,
         // other replaced characters containing ampersands would get messed up.
         "\u0026","&amp;",      //ampersand
         "\"","&quot;",         //quotation mark
         "'","&apos;",          //apostroph
         "\u003C","&lt;",       //less than
         "\u003E","&gt;",       //greater than
     };
 
     //This array contains codes (see http://mindprod.com/jgloss/unicode.html for details) and
     //patterns that will be replaced. To add new codes or patterns, just put them at the end
     //of the list. Codes or patterns in this list can not be escaped with [= or <pre>
     public static final String[] htmlentities={
     	// named entities
     	" "     ,"&nbsp;",     //space
         "\u00A1","&iexcl;",    //inverted (spanish) exclamation mark
         "\u00A2","&cent;",     //cent
         "\u00A3","&pound;",    //pound
         "\u00A4","&curren;",   //currency
         "\u00A5","&yen;",      //yen
         "\u00A6","&brvbar;",   //broken vertical bar
         "\u00A7","&sect;",     //section sign
         "\u00A8","&uml;",      //diaeresis (umlaut)
         "\u00A9","&copy;",     //copyright sign
         "\u00AA","&ordf;",     //feminine ordinal indicator
         "\u00AB","&laquo;",    //left-pointing double angle quotation mark
         "\u00AC","&not;",      //not sign
         "\u00AD","&shy;",      //soft hyphen
         "\u00AE","&reg;",      //registered sign
         "\u00AF","&macr;",     //macron
         "\u00B0","&deg;",      //degree sign
         "\u00B1","&plusmn;",   //plus-minus sign
         "\u00B2","&sup2;",     //superscript two
         "\u00B3","&sup3;",     //superscript three
         "\u00B4","&acute;",    //acute accent
         "\u00B5","&micro;",    //micro sign
         "\u00B6","&para;",     //paragraph sign
         "\u00B7","&middot;",   //middle dot
         "\u00B8","&cedil;",    //cedilla
         "\u00B9","&sup1;",     //superscript one
         "\u00BA","&ordm;",     //masculine ordinal indicator
         "\u00BB","&raquo;",    //right-pointing double angle quotation mark
         "\u00BC","&frac14;",   //fraction 1/4
         "\u00BD","&frac12;",   //fraction 1/2
         "\u00BE","&frac34;",   //fraction 3/4
         "\u00BF","&iquest;",   //inverted (spanisch) questionmark
         "\u00C0","&Agrave;",
         "\u00C1","&Aacute;",
         "\u00C2","&Acirc;",
         "\u00C3","&Atilde;",
         "\u00C4","&Auml;",
         "\u00C5","&Aring;",
         "\u00C6","&AElig;",
         "\u00C7","&Ccedil;",
         "\u00C8","&Egrave;",
         "\u00C9","&Eacute;",
         "\u00CA","&Ecirc;",
         "\u00CB","&Euml;",
         "\u00CC","&Igrave;",
         "\u00CD","&Iacute;",
         "\u00CE","&Icirc;",
         "\u00CF","&Iuml;",
         "\u00D0","&ETH;",
         "\u00D1","&Ntilde;",
         "\u00D2","&Ograve;",
         "\u00D3","&Oacute;",
         "\u00D4","&Ocirc;",
         "\u00D5","&Otilde;",
         "\u00D6","&Ouml;",
         "\u00D7","&times;",
         "\u00D8","&Oslash;",
         "\u00D9","&Ugrave;",
         "\u00DA","&Uacute;",
         "\u00DB","&Ucirc;",
         "\u00DC","&Uuml;",
         "\u00DD","&Yacute;",
         "\u00DE","&THORN;",
         "\u00DF","&szlig;",
         "\u00E0","&agrave;",
         "\u00E1","&aacute;",
         "\u00E2","&acirc;",
         "\u00E3","&atilde;",
         "\u00E4","&auml;",
         "\u00E5","&aring;",
         "\u00E6","&aelig;",
         "\u00E7","&ccedil;",
         "\u00E8","&egrave;",
         "\u00E9","&eacute;",
         "\u00EA","&ecirc;",
         "\u00EB","&euml;",
         "\u00EC","&igrave;",
         "\u00ED","&iacute;",
         "\u00EE","&icirc;",
         "\u00EF","&iuml;",
         "\u00F0","&eth;",
         "\u00F1","&ntilde;",
         "\u00F2","&ograve;",
         "\u00F3","&oacute;",
         "\u00F4","&ocirc;",
         "\u00F5","&otilde;",
         "\u00F6","&ouml;",
         "\u00F7","&divide;",
         "\u00F8","&oslash;",
         "\u00F9","&ugrave;",
         "\u00FA","&uacute;",
         "\u00FB","&ucirc;",
         "\u00FC","&uuml;",
         "\u00FD","&yacute;",
         "\u00FE","&thorn;",
         "\u00FF","&yuml;"
     };
 }
