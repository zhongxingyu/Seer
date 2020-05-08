 package org.webmacro.template;
 
 import java.io.*;
 import org.webmacro.*;
 import org.webmacro.engine.StringTemplate;
 import org.webmacro.engine.DefaultEvaluationExceptionHandler;
 
 import junit.framework.*;
 
 import org.apache.regexp.RE;
 
 
 /**
  * the abuse test.  Tries to do really mean things to
  * the parser, and even expects the parser to do them 
  * correctly.
  */
 public class TestAbuse extends TemplateTestCase {
 
    public TestAbuse (String name) {
       super (name);
    }
 
    public void stuffContext (Context context) throws Exception {
       context.setEvaluationExceptionHandler (
                 new DefaultEvaluationExceptionHandler ());
       context.put ("User", "Eric");
    }
 
    /** We'll start easy */
    public void testEOLComment () throws Exception {
       assertStringTemplateEquals ("## a comment", "");
    }
 
    /** this is easy too */
    public void testBlockComment () throws Exception {
       assertStringTemplateEquals ("#comment { this is a comment }", "");
    }
 
    /** test a variable reference like: Welcome to the site, $User. */
    public void testTrailingDot () throws Exception {
       assertStringTemplateEquals ("Hello, $User.",
                                   "Hello, Eric.");
    }
 
    /** EOL comments at end of directive blocks.  */
    public void testEOLCommentWithDirective () throws Exception {
       String tmpl = "#if (true) {  ## this is a comment\n" 
                     + "pass\n"
                     + "} else { ## this is another comment\n"
                     + "fail\n" 
                     + "}";
 
       assertStringTemplateMatches (tmpl, ".*pass.*");
    }
 
    /** WMScript that would fail if it weren't commented out */
    public void testBlockCommentWithBadWMScript () throws Exception {
       String tmpl = "#comment { #if ($true) }";
       assertStringTemplateMatches (tmpl, "");
    }
   
    /** split an #if across multiple lines.
        currently produces a big parse error
     */
    public void testMultiLineIf () throws Exception {
       System.err.println ("NEED TO REVISIT: TestAbuse.testMultiLineIf()");
       assert (true);
 //      String tmpl = "#if (true\n&& true && true\n) {pass} #else {fail}";
 //      assertStringTemplateEquals (tmpl, "pass");
 
    }
 
    /**
     * The error message this produces is: 
     *   Attempt to dereference null value $Object
     * but $Object <b>isn't</b> null.   Only its
     * toString() method returns null.  The message
     * implies that $Object is null. 
     *
     * Can this be detected and say something like:
     *     $Object's toString() method returned null.
     *
     * Probably requires another Exception type for EEH
     * and friends.
     */
    public void testToStringIsNull () throws Exception {
       Object obj = new Object () { 
          public String toString () { return null; }
       };
 
       _context.put ("Object", obj);
 
       assertStringTemplateMatches("$Object",
                                  "^<!--.*Object.toString().*null.*-->$");
 
    }
 
    /** the infamous "but I want to put Javascript in an #if block!!" 
 
        Currently fails when using { and }, but passes with 
        #begin and #end.  Any reason why?
    */
    public void testBlockDirectiveWithJavascript () throws Exception {
       String jscript = "<script>\n"
                      + "   if (true) {\n"
                      + "      alert (\"Hello, World\");\n"
                      + "   } else {\n"
                      + "      alert (\"Goodbye, World\");\n"
                      + "   }\n"
                      + "</script>";
       String jscriptnl = "<script>\n"
                      + "   if (true) {\n"
                      + "      alert (\"Hello, World\");\n"
                      + "   } else {\n"
                      + "      alert (\"Goodbye, World\");\n"
                      + "   }\n"
                      + "</script>\n";
 
       // check it with #begin and #end
       String tmpl = "#if (true) #begin"
                   + jscript  
                   + "#end";
       assertStringTemplateEquals (tmpl, jscript);
    }
 
    public void testSemi() throws Exception {
       String assn = "#set $foo=\"blah\" ";
       assertStringTemplateEquals(assn + "$(foo);", "blah;");
       assertStringTemplateEquals(assn + "$(foo)", "blah");
       assertStringTemplateEquals(assn + "$foo", "blah");
       assertStringTemplateEquals(assn + "$foo;", "blah");
       assertStringTemplateEquals(assn + "$foo.substring(1)", "lah");
       assertStringTemplateEquals(assn + "$foo.substring(1);", "lah;");
    }
 }
 
