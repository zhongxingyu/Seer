 package name.vysoky.epub;
 
 import org.apache.commons.lang.StringEscapeUtils;
 
 /**
  * @author Jiri Vysoky
  */
 public class TextExtractor implements TextProcessor {
 
     private StringBuilder buffer = new StringBuilder();
 
     @Override
     public String process(String input) {
        String s = input.replace("\n\n", "\n").trim(); // remove empty lines
         s = StringEscapeUtils.unescapeHtml(s);
         buffer.append(s);
         return input; // unused
     }
 
     public String getText() {
         return buffer.toString();
     }
 }
