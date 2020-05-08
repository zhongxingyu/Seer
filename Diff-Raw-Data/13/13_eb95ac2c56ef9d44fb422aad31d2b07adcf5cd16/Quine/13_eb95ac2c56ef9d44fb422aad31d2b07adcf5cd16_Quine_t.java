 package pp.ua.raindev;
 
 import java.util.regex.Matcher;
 
 /**
 * Quine, implemented in pure Java.
  *
  * @author raindev
  */
 public class Quine {
    private static final String self = "package pp.ua.raindev;\n\nimport java.util.regex.Matcher;\n\n/**\n * Quine, implemented in pure Java.\n *\n * @author raindev\n */\npublic class Quine {\n    private static final String self = \"package pp.ua.raindev;\\n\\nimport java.util.regex.Matcher;\\n\\n/**\\n * Quine, implemented in Java.\\n *\\n * @author raindev\\n */\\npublic class Quine {\\n    private static final String self = \\\"package pp.ua.raindev;\\\\n\\\\nimport java.util.regex.Matcher;\\\\n\\\\n/**\\\\n * Quine, implemented in Java.\\\\n *\\\\n * @author raindev\\\\n */\\\\npublic class Quine {\\\\n    private static final String self = \\\\\\\"package pp.ua.raindev;\\\\\\\\n\\\\\\\\n/**\\\\\\\\n * Quine, implemented in Java.\\\\\\\\n *\\\\\\\\n * @author raindev\\\\\\\\n */\\\\\\\\npublic class Quine {\\\\\\\\n    private static final String self = \\\\\\\\\\\\\\\"~\\\\\\\\\\\\\\\";\\\\\\\\n\\\\\\\\n    public static void main(String[] args) {\\\\\\\\n         System.out.println(self.replaceFirst(\\\\\\\\\\\\\\\"~\\\\\\\\\\\\\\\", Matcher.quoteReplacement(StringEscapeUtils.escapeJava(self))));\\\\\\\\n    }\\\\\\\\n\\\\\\\\n}\\\\\\\";\\\\n\\\\n    public static void main(String[] args) {\\\\n        System.out.println(self.replaceFirst(\\\\\\\"~\\\\\\\", Matcher.quoteReplacement(\\\\n                self.replace(String.valueOf((char) 10), (char) 92 + \\\\\\\"n\\\\\\\")\\\\n                        .replace(String.valueOf((char) 34), (char) 92 + \\\\\\\"\\\\\\\" + (char) 34))));\\\\n    }\\\\n\\\\n}\\\";\\n\\n    public static void main(String[] args) {\\n        System.out.println(self.replaceFirst(\\\"~\\\", Matcher.quoteReplacement(\\n                self.replace(String.valueOf((char) 10), (char) 92 + \\\"n\\\")\\n                        .replace(String.valueOf((char) 34), (char) 92 + \\\"\\\" + (char) 34))));\\n    }\\n\\n}\";\n\n    public static void main(String[] args) {\n        System.out.println(self.replaceFirst(\"~\", Matcher.quoteReplacement(\n                self.replace((char) 10 + \"\", (char) 92 + \"n\")\n                        .replace((char) 34 + \"\", (char) 92 + \"\" + (char) 34))));\n    }\n\n}";
 
     public static void main(String[] args) {
        System.out.println(self.replaceFirst("~", Matcher.quoteReplacement(
                self.replace((char) 10 + "", (char) 92 + "n")
                        .replace((char) 34 + "", (char) 92 + "" + (char) 34))));
     }
 
 }
