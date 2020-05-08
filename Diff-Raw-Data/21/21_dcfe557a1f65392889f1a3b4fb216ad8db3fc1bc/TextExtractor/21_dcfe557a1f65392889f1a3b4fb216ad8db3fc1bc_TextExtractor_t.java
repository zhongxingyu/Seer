 package name.vysoky.epub;
 
 import org.apache.commons.lang.StringEscapeUtils;
 
 /**
  * @author Jiri Vysoky
  */
 public class TextExtractor implements TextProcessor {
 
     private StringBuilder buffer = new StringBuilder();
 
     @Override
     public String process(String input) {
        String s = input;
        if (s.contains("\n") && s.trim().isEmpty()) s = "\n";
        else s = s.trim();
         s = StringEscapeUtils.unescapeHtml(s);
        s = removeQuotes(s);
         buffer.append(s);
         return input; // unused
     }
 
     public String getText() {
         return buffer.toString();
     }

    public static String removeQuotes(String input) {
        String s = SmartQuoter.convertToDefaultQuotes(input);
        s = s.replace("\"", "");
        s = s.replace("'", "");
        return s;
    }
 }
