 package org.webmacro.template;
 
 import java.io.*;
 import org.webmacro.*;
 import org.webmacro.engine.StringTemplate;
 import org.webmacro.engine.DefaultEvaluationExceptionHandler;
 
 import junit.framework.*;
 
 import org.apache.regexp.RE;
 
 
 /**
  * the block test.  makes sure WM can parse the supported
  * block sytles correctly.
  */
 public class TestBlocks extends TemplateTestCase {
 
    public TestBlocks (String name) {
       super (name);
    }
 
    public void stuffContext (Context context) throws Exception {
       context.setEvaluationExceptionHandler (
                 new DefaultEvaluationExceptionHandler ());
    }
 
 
    public void testBraces () throws Exception {
       String tmpl = "#if (true) {pass} #else {fail}";
       assertStringTemplateEquals (tmpl, "pass");
    }
   
    public void testBeginEnd () throws Exception {
       String tmpl = "#if (true) #begin pass #end #else #begin fail #end";
       assertStringTemplateEquals (tmpl, "pass");

      tmpl = "#if (true)\n #begin pass\n #end\n #else\n #begin fail\n #end";
      assertStringTemplateEquals (tmpl, "pass");
    }
 
    public void testOnlyEnd () throws Exception {
       String tmpl = "#if (true) pass #end";
       assertStringTemplateEquals (tmpl, "pass");
    }
 
    /**
     * until Brian adds support for #if #elseif #else #end
     * this will fail.
     *
     * The point of this is really to make sure the parser
     * doesn't throw exceptions, not to test the conditionals
     */
    public void testIfElseIfEnd () throws Exception {
       String tmpl = "#if ($foo)\n"
                   + "pass\n"
                   + "#elseif ($foo2)\n"
                   + "fail\n"
                   + "#else\n"
                   + "fail\n"
                   + "#end";
 
 
       _context.put ("foo", "foo");
       assertStringTemplateEquals (tmpl, "pass\n");
    }
    
    public void testIfEnd () throws Exception {
       String tmpl = "#if ($foo)\n"
                   + "pass\n"
                   + "#end";
 
 
       _context.put ("foo", "foo");
       assertStringTemplateEquals (tmpl, "pass\n");
    }
 
 
    public void testNestedIfElseEnd () throws Exception {
       String tmpl = "#if (true)\n"
                   + "   #if (false)\n"
                   + "      should not be here 1\n"
                   + "   #else\n"
                   + "      #if (true)\n"
                   + "         you should see this\n" 
                   + "      #else\n"
                   + "         should not be here 2\n"
                   + "      #end\n"
                   + "   #end\n"
 //                  + " putting anything other than whitespace here makes it work\n"
                   + "#else\n"
                   + "   should not be here 3\n"
                   + "#end\n";
 
       String output = executeStringTemplate (tmpl);
       assert (output.indexOf ("should not be here") == -1);
    }
 }
