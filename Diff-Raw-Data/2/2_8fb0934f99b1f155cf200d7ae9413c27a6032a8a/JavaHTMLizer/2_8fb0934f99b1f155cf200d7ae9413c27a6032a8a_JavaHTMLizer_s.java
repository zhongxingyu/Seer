 package org.thymeleaf.tools.htmlizer;
 
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 
 public class JavaHTMLizer {
 
     private static final String[] RESERVED_WORDS =
         new String[] {
             "return", "new", "public", "static", "final", "void", "synchronized",
             "class", "enum", "private", "protected", "import", "package",
             "try", "catch", "finally", "for", "while", "switch", "case",
             "byte", "short", "int", "long", "float", "double", "boolean",
            "extends", "implements", "super", "true", "false"
         };
     
     private static final Map<String,String> ESCAPE_MAPS = createEscapeMaps();
     
     
     
     
     private static Map<String,String> createEscapeMaps() {
         
         final Map<String,String> escapeMaps = new LinkedHashMap<String, String>();
         
         escapeMaps.put(" ", "&nbsp;");
         escapeMaps.put("<", "&lt;");
         escapeMaps.put(">", "&gt;");
         escapeMaps.put("\"", "&quot;");
         escapeMaps.put("&", "&amp;");
         escapeMaps.put("'", "&#39;");
         escapeMaps.put("\n", "<br />\n");
         
         return escapeMaps;
         
     }
     
     
     private static String escapeChar(final char c) {
         final String escaped = ESCAPE_MAPS.get(String.valueOf(c));
         if (escaped != null) {
             return escaped;
         }
         return String.valueOf(c);
     }
     
 
     
     
     
     
     
     private static String htmlEscape(final String text) {
         
         final StringBuilder strBuilder = new StringBuilder();
         for (int i = 0; i < text.length(); i++) {
             strBuilder.append(escapeChar(text.charAt(i)));
         }
         
         return strBuilder.toString();
         
     }
     
     
     private static String formatReservedWords(final String text) {
         
         String result = text;
         
         for (final String reservedWord : RESERVED_WORDS) {
             result = result.replaceAll("(\\W)("+reservedWord+")(\\W)", "$1<b>$2</b>$3");
         }
         
         return result;
         
     }
 
     
     private static String formatComments(final String text) {
         
         String result = text;
         
         final Pattern multilineCommentPattern = Pattern.compile("\\/\\*(.*?)\\*\\/", Pattern.DOTALL);
         final Matcher multilineCommentMatcher = multilineCommentPattern.matcher(result);
         result = multilineCommentMatcher.replaceAll("<span class=\"comment\">/*$1*/</span>");
         
         final Pattern singlelineCommentPattern = Pattern.compile("\\/\\/(.*?)<br \\/>", Pattern.DOTALL);
         final Matcher singlelineCommentMatcher = singlelineCommentPattern.matcher(result);
         result = singlelineCommentMatcher.replaceAll("<span class=\"comment\">//$1</span><br />");
         
         return result;
         
     }
     
     
     
     private static String formatStrings(final String text) {
         
         String result = text;
         
         final Pattern stringPattern = Pattern.compile("&quot;(.*?)&quot;");
         final Matcher stringMatcher = stringPattern.matcher(result);
         result = stringMatcher.replaceAll("<span class=\"string\">&quot;$1&quot;</span>");
         
         return result;
         
     }
 
     
     
     public static String htmlize(final String text) {
         
         String result = text;
         
         result = htmlEscape(result);
         result = formatReservedWords(result);
         result = formatComments(result);
         result = formatStrings(result);
         
         return result;
         
     }
     
     
     private JavaHTMLizer() {
         super();
     }
     
 }
