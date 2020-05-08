 package pp.ua.raindev;
 
import org.apache.commons.lang.StringEscapeUtils;

 import java.util.regex.Matcher;
 
 /**
 * Quine, implemented in Java.
  *
  * @author raindev
  */
 public class Quine {
    private static final String self = "package pp.ua.raindev;\n\n/**\n * Quine, implemented in Java.\n *\n * @author raindev\n */\npublic class Quine {\n    private static final String self = \"~\";\n\n    public static void main(String[] args) {\n         System.out.println(self.replaceFirst(\"~\", Matcher.quoteReplacement(StringEscapeUtils.escapeJava(self))));\n    }\n\n}";
 
     public static void main(String[] args) {
        System.out.println(self.replaceFirst("~", Matcher.quoteReplacement(StringEscapeUtils.escapeJava(self))));
     }
 
 }
