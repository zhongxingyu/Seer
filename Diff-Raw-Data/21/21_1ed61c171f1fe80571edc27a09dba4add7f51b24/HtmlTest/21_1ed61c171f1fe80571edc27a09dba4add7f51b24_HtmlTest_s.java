 package org.fiz;
 import java.io.*;
 import java.util.*;
 
 /**
  * Junit tests for the Html class.
  */
 
 public class HtmlTest extends junit.framework.TestCase {
     // The following class definition provides additional/modified features
     // for testing.
     protected static class HtmlFixture extends Html {
         public HtmlFixture() {
             super(null);
         }
         public String getPrologue() {
             // This method overrides getPrologue: a simpler prologue generates
             // less clutter in tests.
             return "**Prologue**\n";
         }
     }
 
     // The following class is used to test the handling of IOExceptions.
     protected static class ErrorAppendable implements Appendable {
         public Appendable append(char c) throws IOException {
             throw new IOException("synthetic exception");
         }
         public Appendable append(CharSequence sequence) throws IOException {
             throw new IOException("synthetic exception");
         }
         public Appendable append(CharSequence sequence, int start, int end)
                 throws IOException {
             throw new IOException("synthetic exception");
         }
     }
 
     protected HtmlFixture html = new HtmlFixture();
 
     public void setUp() {
         Html.clearJsDependencyCache();
         html = new HtmlFixture();
        Css.init("test/testData/WEB-INF/css");
        Config.init("test/testData/WEB-INF/config");
     }
 
     public void test_clear() {
         html.setTitle("sample");
         html.getBody().append("body info\n");
         html.includeCssFile("test.css");
         html.includeJsFile("fizlib/Ajax.js");
         html.includeJavascript("window.dummy = 1;");
         html.clear();
         html.getBody().append("<p>Text</p>\n");
         assertEquals("**Prologue**\n" +
                 "<head>\n" +
                 "<title></title>\n" +
                 "<style type=\"text/css\">\n" +
                 "/* Dummy version of main.css for tests */\n" +
                 "body {color: #000000}\n" +
                 "</style>\n" +
                 "</head>\n" +
                 "<body>\n" +
                 "<p>Text</p>\n" +
                 "</body>\n" +
                 "</html>\n", html.toString());
     }
 
     public void test_clearJsDependencyCache() {
         (new File("_test_")).mkdir();
         TestUtil.writeFile("_test_/file1.js", "// Fiz:include file2.js\n");
         assertEquals("dependencies", "file2.js",
                 StringUtil.join(html.getJsDependencies("_test_/file1.js"),
                 ", "));
         TestUtil.writeFile("_test_/file1.js", "// Fiz:include file4.js\n");
         Html.clearJsDependencyCache();
         assertEquals("dependencies", "file4.js",
                 StringUtil.join(html.getJsDependencies("_test_/file1.js"),
                 ", "));
         TestUtil.deleteTree("_test_");
     }
 
     // No tests for getBody; too trivial.
 
     public void test_getCssFiles() {
         (new File("_test_")).mkdir();
         TestUtil.writeFile("_test_/first.css", "first.css");
         TestUtil.writeFile("_test_/second.css", "second.css");
         TestUtil.writeFile("_test_/third.css", "third.css");
         Css.init("_test_");
         html.setTitle("sample");
         html.includeCssFile("second.css");
         html.includeCssFile("first.css");
         html.includeCssFile("third.css");
         TestUtil.assertSubstring("CSS file names",
                 "first.css, second.css, third.css",
                 html.getCssFiles());
         Util.deleteTree("_test_");
     }
 
     public void test_getJsFiles() {
         (new File("_test_")).mkdir();
         TestUtil.writeFile("_test_/file1.js", "");
         TestUtil.writeFile("_test_/file2.js", "");
         html.jsDirectory = "_test_/";
         html.includeJsFile("file1.js");
         html.includeJsFile("file2.js");
         assertEquals("Javascript file names",
                 "file1.js, file2.js",
                 html.getJsFiles());
         Util.deleteTree("_test_");
     }
 
     public void test_getPrologue() {
         assertEquals("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                 + "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\"\n"
                 + "        \"http://www.w3.org/TR/xhtml1/DTD/"
                 + "xhtml1-strict.dtd\">\n" +
                 "<html xmlns=\"http://www.w3.org/1999/xhtml\" "
                 + "xml:lang=\"en\" lang=\"en\">\n",
                 (new Html(null)).getPrologue());
     }
 
     public void test_getAndSetTitle() {
         assertEquals("initial value", null, html.getTitle());
         html.setTitle("new title");
         assertEquals("modified value", "new title", html.getTitle());
         html.setTitle(null);
         assertEquals("new null value", null, html.getTitle());
     }
 
     public void test_includeCss() {
         html.includeCss("/* Sample comment */");
         html.includeCss("h1 {color: red}\n");
         html.includeCss("/* Another comment */");
         assertEquals("simple CSS", "/* Sample comment */\n" +
                 "\n" +
                 "h1 {color: red}\n" +
                 "\n" +
                 "/* Another comment */",
                 html.css.toString());
     }
 
     public void test_includeCssFile() {
         (new File("_test_")).mkdir();
         TestUtil.writeFile("_test_/first.css", "Sample css with newline\n");
         TestUtil.writeFile("_test_/second.css", "Css with no newline");
         TestUtil.writeFile("_test_/third.css", "Line #1\nLine #2\n");
         Css.init("_test_");
         html.includeCssFile("first.css");
         html.includeCssFile("first.css");
         html.includeCssFile("first.css");
         assertEquals("simple CSS", "Sample css with newline\n",
                 html.css.toString());
         html.includeCssFile("second.css");
         html.includeCssFile("third.css");
         assertEquals("check blank lines", "Sample css with newline\n" +
                 "\n" +
                 "Css with no newline\n" +
                 "\n" +
                 "Line #1\n" +
                 "Line #2\n",
                 html.css.toString());
         Util.deleteTree("_test_");
     }
 
     public void test_includeJavascript() {
         html.includeJavascript(
                 "// Javascript comment with special chars <&>\n");
         html.includeJavascript(
                 "var i = 444;\n");
         assertEquals("accumulated Javascript",
                 "// Javascript comment with special chars <&>\n" +
                 "var i = 444;\n",
                 html.jsCode.toString());
     }
 
     public void test_includeJavascript_withTemplateAndDataset() {
         html.includeJavascript("foo(\"@value1\", value2);",
                 new Dataset("value1", "\"\n\000<&'>", "value2", "345"));
         assertEquals("accumulated Javascript",
                 "foo(\"\\\"\\n\\x00<&'>\", value2);",
                 html.jsCode.toString());
     }
 
     public void test_includeJavascript_withTemplateAndArgs() {
         html.includeJavascript("foo(\"@1\", \"@3\");",
                 "\"\n\000<&'>", "value2", "value3");
         assertEquals("accumulated Javascript",
                 "foo(\"\\\"\\n\\x00<&'>\", \"value3\");",
                 html.jsCode.toString());
     }
 
     public void test_includeJsFile() {
         (new File("_test_")).mkdir();
         TestUtil.writeFile("_test_/file1.js", "// Fiz:include file2.js\n");
         TestUtil.writeFile("_test_/file2.js", "// Fiz:include file1.js\n");
         html.jsDirectory = "_test_/";
         html.includeJsFile("file1.js");
         assertEquals("jsFileHtml string",
                 "<script type=\"text/javascript\" " +
                 "src=\"/servlet/file2.js\"></script>\n" +
                 "<script type=\"text/javascript\" " +
                 "src=\"/servlet/file1.js\"></script>\n",
                 html.jsFileHtml.toString());
         html.includeJsFile("file1.js");
         html.includeJsFile("file2.js");
         assertEquals("only include each file once",
                 "<script type=\"text/javascript\" " +
                 "src=\"/servlet/file2.js\"></script>\n" +
                 "<script type=\"text/javascript\" " +
                 "src=\"/servlet/file1.js\"></script>\n",
                 html.jsFileHtml.toString());
         TestUtil.deleteTree("_test_");
     }
 
     public void test_print_emptyHtml() {
         assertEquals("nothing to print", "", html.toString());
         html.setTitle("sample");
         TestUtil.assertSubstring("title only", "<title>sample</title>",
                 html.toString());
         html.clear();
         html.getBody().append("body info\n");
         TestUtil.assertSubstring("body only", "body info",
                 html.toString());
         html.clear();
         html.includeJavascript("x = 44;");
         TestUtil.assertSubstring("Javascript only",
                 "<head>\n" +
                 "<title></title>\n" +
                 "</head>\n" +
                 "<body>\n" +
                 "<script type=\"text/javascript\">\n" +
                 "//<![CDATA[\n" +
                 "x = 44;//]]>\n" +
                 "</script>\n" +
                 "</body>",
                 html.toString());
     }
     public void test_print_noTitle() {
         html.getBody().append("first line\nsecond line\n");
         StringWriter out = new StringWriter();
         html.print(out);
         assertEquals("**Prologue**\n" +
                 "<head>\n" +
                 "<title></title>\n" +
                 "<style type=\"text/css\">\n" +
                 "/* Dummy version of main.css for tests */\n" +
                 "body {color: #000000}\n" +
                 "</style>\n" +
                 "</head>\n" +
                 "<body>\n" +
                 "first line\n" +
                 "second line\n" +
                 "</body>\n" +
                 "</html>\n", out.toString());
     }
     public void test_print_withTitle() {
         html.getBody().append("first line\n");
         html.setTitle("sample title");
         StringWriter out = new StringWriter();
         html.print(out);
         assertEquals("**Prologue**\n" +
                 "<head>\n" +
                 "<title>sample title</title>\n" +
                 "<style type=\"text/css\">\n" +
                 "/* Dummy version of main.css for tests */\n" +
                 "body {color: #000000}\n" +
                 "</style>\n" +
                 "</head>\n" +
                 "<body>\n" +
                 "first line\n" +
                 "</body>\n" +
                 "</html>\n",
                 out.toString());
     }
     public void test_print_skipCssIfNoBody() {
         (new File("_test_")).mkdir();
         TestUtil.writeFile("_test_/first.css", "first.css\n");
         TestUtil.writeFile("_test_/main.css", "main.css\n");
         Css.init("_test_");
         html.setTitle("sample");
         TestUtil.assertSubstring("header section",
                 "<head>\n" +
                 "<title>sample</title>\n" +
                 "</head>\n",
                 html.toString());
         Util.deleteTree("_test_");
     }
     public void test_print_css() {
         (new File("_test_")).mkdir();
         TestUtil.writeFile("_test_/first.css", "first.css\n");
         TestUtil.writeFile("_test_/main.css", "main.css\n");
         Css.init("_test_");
         html.setTitle("sample");
         html.includeCssFile("first.css");
         html.getBody().append("<p>xyz</p>\n");
         TestUtil.assertSubstring("header section",
                 "<head>\n" +
                 "<title>sample</title>\n" +
                 "<style type=\"text/css\">\n" +
                 "main.css\n" +
                 "\n" +
                 "first.css\n" +
                 "</style>\n" +
                 "</head>\n",
                 html.toString());
         Util.deleteTree("_test_");
     }
     public void test_print_css_newlinesMissing() {
         (new File("_test_")).mkdir();
         TestUtil.writeFile("_test_/first.css", "first.css");
         TestUtil.writeFile("_test_/main.css", "main.css");
         Css.init("_test_");
         html.setTitle("sample");
         html.includeCssFile("first.css");
         html.getBody().append("<p>xyz</p>\n");
         TestUtil.assertSubstring("header section",
                 "<head>\n" +
                 "<title>sample</title>\n" +
                 "<style type=\"text/css\">\n" +
                 "main.css\n" +
                 "\n" +
                 "first.css\n" +
                 "</style>\n" +
                 "</head>\n",
                 html.toString());
         Util.deleteTree("_test_");
     }
     public void test_print_css_noCssButMain() {
         (new File("_test_")).mkdir();
         TestUtil.writeFile("_test_/main.css", "main.css");
         Css.init("_test_");
         html.setTitle("sample");
         html.getBody().append("<p>xyz</p>\n");
         TestUtil.assertSubstring("header section",
                 "<head>\n" +
                 "<title>sample</title>\n" +
                 "<style type=\"text/css\">\n" +
                 "main.css\n" +
                 "</style>\n" +
                 "</head>\n",
                 html.toString());
         Util.deleteTree("_test_");
     }
     public void test_print_javascriptIncludes() {
         (new File("_test_")).mkdir();
         TestUtil.writeFile("_test_/file1.js", "");
         TestUtil.writeFile("_test_/file2.js", "");
         html.getBody().append("<p>Body text.</p>\n");
         html.jsDirectory = "_test_/";
         html.setTitle("sample");
         html.includeJsFile("file1.js");
         html.includeJsFile("file2.js");
         TestUtil.assertSubstring("body section",
                 "<body>\n" +
                 "<p>Body text.</p>\n" +
                 "<script type=\"text/javascript\" " +
                 "src=\"/servlet/file1.js\"></script>\n" +
                 "<script type=\"text/javascript\" " +
                 "src=\"/servlet/file2.js\"></script>\n" +
                 "</body>\n",
                 html.toString());
         Util.deleteTree("_test_");
     }
     public void test_print_javascriptCode() {
         html.getBody().append("<p> First paragraph.</p>\n");
         html.includeJavascript(
                 "// Javascript comment with special chars <&>\n");
         html.includeJavascript(
                 "var i = 444;\n");
         TestUtil.assertSubstring("<body> element from document", "<body>\n" +
                 "<p> First paragraph.</p>\n" +
                 "<script type=\"text/javascript\">\n" +
                 "//<![CDATA[\n" +
                 "// Javascript comment with special chars <&>\n" +
                 "var i = 444;\n" +
                 "//]]>\n" +
                 "</script>\n" +
                 "</body>",
                 html.toString());
     }
 
     public void test_toString() {
         html.getBody().append("first line\n");
         html.setTitle("sample title");
         assertEquals("**Prologue**\n" +
                 "<head>\n" +
                 "<title>sample title</title>\n" +
                 "<style type=\"text/css\">\n" +
                 "/* Dummy version of main.css for tests */\n" +
                 "body {color: #000000}\n" +
                 "</style>\n" +
                 "</head>\n" +
                 "<body>\n" +
                 "first line\n" +
                 "</body>\n" +
                 "</html>\n",
                 html.toString());
     }
 
     public void test_escapeHtmlChars() {
         assertEquals("&lt;&amp;&gt;\n'&quot; =",
                 Html.escapeHtmlChars("<&>\n'\" ="));
     }
 
     public void test_escapeHtmlChars_stringBuilder() {
         StringBuilder out = new StringBuilder();
         Html.escapeHtmlChars(new StringBuffer("abc123"), out);
         assertEquals("no special characters", "abc123", out.toString());
         out.setLength(0);
         Html.escapeHtmlChars("!\"#%&';<=>?", out);
         assertEquals("special characters", "!&quot;#%&amp;';&lt;=&gt;?",
                 out.toString());
     }
 
     public void test_escapeUrlChars() {
         assertEquals("%7f--%c2%80--%df%bf--%e0%a0%80--%e1%88%b4",
                 Html.escapeUrlChars("\u007f--\u0080--\u07ff--\u0800--\u1234"));
     }
 
     public void test_escapeUrlChars_stringBuilder() {
         StringBuilder out = new StringBuilder();
         Html.escapeUrlChars(new StringBuffer(" +,-./@ABYZ[`abyz{"), out);
         assertEquals("ASCII characters", "+%2b%2c-.%2f%40ABYZ%5b%60abyz%7b",
                 out.toString());
         out.setLength(0);
         Html.escapeUrlChars("/0123456789:", out);
         assertEquals("digits","%2f0123456789%3a", out.toString());
         out.setLength(0);
         Html.escapeUrlChars("\u007f--\u0080--\u07ff--\u0800--\u1234", out);
         assertEquals("Unicode characters",
                 "%7f--%c2%80--%df%bf--%e0%a0%80--%e1%88%b4",
                 out.toString());
     }
 
     public void test_escapeStringChars() {
         assertEquals("abc\\x1f  | \\n\\t\\r | \\x01 | \\\\\\\"",
                 Html.escapeStringChars("abc\037\040 | \n\t\r | \001 | \\\""));
     }
 
     public void test_escapeStringChars_stringBuilder() {
         StringBuilder out = new StringBuilder();
         Html.escapeStringChars("abc\037\040 | \n\t\r | \001 | \\\"", out);
         assertEquals("abc\\x1f  | \\n\\t\\r | \\x01 | \\\\\\\"",
                 out.toString());
     }
     public void test_escapeStringChars_stringBuilder_closeStringTag() {
         StringBuilder out = new StringBuilder();
         Html.escapeStringChars("aaa</scriptbbb</script>ccc</script", out);
         assertEquals("aaa</scriptbbb\\x3c/script>ccc</script",
                 out.toString());
     }
     public void test_escapeStringChars_stringBuilder_handleException() {
         boolean gotException = false;
         try {
             Html.escapeStringChars("abcdef", new ErrorAppendable());
         }
         catch (IOError e) {
             assertEquals("exception message",
                     "synthetic exception",
                     e.getMessage());
             gotException = true;
         }
         assertEquals("exception happened", true, gotException);
     }
 
     public void test_getJsDependencies_useCachedInformation() {
         (new File("_test_")).mkdir();
         TestUtil.writeFile("_test_/file1.js", "// Fiz:include file2.js\n");
         assertEquals("dependencies", "file2.js",
                 StringUtil.join(html.getJsDependencies("_test_/file1.js"),
                 ", "));
         TestUtil.writeFile("_test_/file1.js", "// Fiz:include file4.js\n");
         assertEquals("dependencies", "file2.js",
                 StringUtil.join(html.getJsDependencies("_test_/file1.js"),
                 ", "));
         TestUtil.deleteTree("_test_");
     }
     public void test_getJsDependencies_commentLines() {
         (new File("_test_")).mkdir();
         TestUtil.writeFile("_test_/file1.js",
                 "//\n" +
                 "/*\n" +
                 " * \n" +
                 " *\n" +
                 " */\n" +
                 "   \n" +
                 "// Fiz:include file2.js\n" +
                 "abc" +
                 "// Fiz:include file3.js\n");
         TestUtil.writeFile("_test_/file2.js", "");
         ArrayList<String> deps = html.getJsDependencies("_test_/file1.js");
         assertEquals("dependencies", "file2.js",
                 StringUtil.join(deps, ", "));
         TestUtil.deleteTree("_test_");
     }
     public void test_getJsDependencies_parsingFizIncludeLine() {
         (new File("_test_")).mkdir();
         TestUtil.writeFile("_test_/file1.js",
                 "   //    Fiz:include file2.js,  file3.js, file4.js   " +
                 "# Comment\n" +
                 "//Fiz:include file5.js    \n" +
                 "//Fiz:includefile6.js");
         TestUtil.writeFile("_test_/file2.js", "");
         TestUtil.writeFile("_test_/file3.js", "");
         TestUtil.writeFile("_test_/file4.js", "");
         TestUtil.writeFile("_test_/file5.js", "");
         ArrayList<String> deps = html.getJsDependencies("_test_/file1.js");
         assertEquals("dependencies", "file2.js, file3.js, file4.js, file5.js",
                 StringUtil.join(deps, ", "));
         TestUtil.deleteTree("_test_");
     }
     public void test_getJsDependencies_missingFile() {
         boolean gotException = false;
         Dataset d = new Dataset();
         try {
             html.getJsDependencies("_test_/bogus.js");
         }
         catch (FileNotFoundError e) {
             assertEquals("exception message",
                     "couldn't open javascript file \"_test_/bogus.js\": ...",
                     TestUtil.truncate(e.getMessage(), "js\": "));
             gotException = true;
         }
         assertEquals("exception happened", true, gotException);
     }
 }
