 package org.webmacro.template;
 
 import java.io.*;
 import org.webmacro.*;
 import org.webmacro.engine.StringTemplate;
 import org.webmacro.engine.DefaultEvaluationExceptionHandler;
 
 import junit.framework.*;
 
 import org.apache.regexp.RE;
 
 
 /**
  * tests whitespace.
  * most of these fail.  
  *
  * I believe all #directives and block tokens ({,},#begin,#end) should 
  * consume ALL whitespace to their right up to and including the first 
  * EOL character (\n or \r\n combo)
  */
 public class TestWhitespace extends TemplateTestCase {
 
    public TestWhitespace (String name) {
       super (name);
    }
 
    public void stuffContext (Context context) throws Exception {
       context.setEvaluationExceptionHandler (
                 new DefaultEvaluationExceptionHandler ());
 
       context.put ("Array", new String[] {"one", "two", "three"});
    }
 
 
    //
    // test whitespace before a directive
    //
 
    public void testBeforeDirective1 () throws Exception {
       String tmpl = "  \n#if(true){pass}";      
       assertStringTemplateEquals (tmpl, "pass");
    }
  
    public void testBeforeDirective2 () throws Exception {
       String tmpl = "\n   #if(true){pass}";
       assertStringTemplateEquals (tmpl, "pass");
    }
 
    public void testBeforeDirective3 () throws Exception {
       String tmpl = "\n  \n#if(true){pass}";  
       assertStringTemplateEquals (tmpl, "\npass");
    } 
 
 
    //
    // test whitespace after a directive
    //
 
    public void testAfterDirective () throws Exception {
       String tmpl = "#set $a = \"foo\"   \npass";   
       assertStringTemplateEquals (tmpl, "\npass");
    }
 
 
    // 
    // test whitespace after a begin 
    //
 
    public void testAfterBeginBlock1 () throws Exception {
       String tmpl = "#if(true)#begin  \npass#end";             
       assertStringTemplateEquals (tmpl, "pass");
    }
 
    public void testAfterBeginBlock2 () throws Exception {
       String tmpl = "#if(true){  \npass}";            
       assertStringTemplateEquals (tmpl, "pass");
    }
 
    public void testAfterBeginBlock3 () throws Exception {
       String tmpl = "#if(true)  \npass #end";
       assertStringTemplateEquals (tmpl, "pass");
    }
 
 
    //
    // test whitespace before an end
    //
 
    public void testBeforeEnd1 () throws Exception {
       String tmpl = "#if(true)#begin pass\n#end";
       assertStringTemplateEquals (tmpl, "pass");
    }
  
    public void testBeforeEnd2 () throws Exception {
       String tmpl = "#if(true){pass\n}";
       assertStringTemplateEquals (tmpl, "pass\n");
    }
  
    public void testBeforeEnd3 () throws Exception {
       String tmpl = "#if(true) pass\n#end";
       assertStringTemplateEquals (tmpl, "pass");
    }   
 
 
    //
    // test whitespace after an end
    //
 
    public void testAfterEndBlock1 () throws Exception {
       String tmpl = "#if(true)#begin pass #end\n";
       assertStringTemplateEquals (tmpl, "pass");
    }
  
    public void testAfterEndBlock2 () throws Exception {
       String tmpl = "#if(true){pass}\n";
       assertStringTemplateEquals (tmpl, "pass");
    }
 
    public void testAfterEndBlock3 () throws Exception {
       String tmpl = "#if(true) pass #end\n";
       assertStringTemplateEquals (tmpl, "pass");
    }
 
    public void testSpacesInParens() throws Exception {
       String assn = "#set $s=\"String\" ";
       assertStringTemplateEquals(assn + "$s", "String");
       assertStringTemplateEquals(assn + "$s.substring(1,4)", "tri");
       assertStringTemplateEquals(assn + "$s.substring( 1,4)", "tri");
       assertStringTemplateEquals(assn + "$s.substring( 1 ,4)", "tri");
       assertStringTemplateEquals(assn + "$s.substring( 1 , 4)", "tri");
       assertStringTemplateEquals(assn + "$s.substring( 1 , 4 )", "tri");
    }
 }
