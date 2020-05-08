 package org.webmacro.template;
 
 import junit.framework.TestCase;
 import org.apache.regexp.RE;
 import org.webmacro.*;
 import org.webmacro.engine.StringTemplate;
 
 import java.io.*;
 
 
 public abstract class TemplateTestCase extends TestCase
 {
 
     /** our WebMacro instance */
     protected WebMacro _wm;
 
     /** context to use for each template */
     protected Context _context;
 
 
     public TemplateTestCase (String name)
     {
         super(name);
     }
 
 
     protected void setUp () throws Exception
     {
         init();
     }
 
 
     /**
      * create a default-configured instance of WebMacro.  Subclasses may
      * override if WM needs to be created/configured differently.
      */
     protected WebMacro createWebMacro () throws Exception
     {
         return new WM();
     }
 
 
     /**
      * initialize this TemplateTester by creating a WebMacro instance
      * and a default Context.
      */
     public void init () throws Exception
     {
         if (System.getProperties().getProperty("org.webmacro.LogLevel") == null)
             System.getProperties().setProperty("org.webmacro.LogLevel", "NONE");
         _wm = createWebMacro();
         _context = _wm.getContext();
 
         // let subclasses stuff the context with custom data
         stuffContext(_context);
     }
 
 
     /**
      * stuff the provided context with custom variables and objects<p>
      *
      * @throws Exception if something goes wrong while stuffing context
      */
     protected abstract void stuffContext (Context context) throws Exception;
 
 
     /**
      * Utility method to convert a template file to a string for use in
      * the test suite.
      * Note: if the file is not found as an absolute arg, then the
      * broker is called to use its get resource method.  */
     public String templateFileToString (String fileReference) throws Exception
     {
         InputStream in = null;
         try
         {
             in = new FileInputStream(fileReference);
         }
         catch (FileNotFoundException e)
         {
             in = _wm.getBroker().getResourceAsStream(fileReference);
             if (in == null) throw new Exception(fileReference + " not found");
         }
         byte[] value = new byte[in.available()];
         in.read(value);
         in.close();
         String string = new String(value);
         return string;
     }
 
 
     /*
      * Evaluates a template and returns its value. */
     public String executeTemplate (Template template) throws Exception
     {
         return template.evaluateAsString(_context);
     }
 
 
     /**
      * Executes a file template.
      */
     public String executeFileTemplate (String fileReference) throws Exception
     {
         return executeStringTemplate(templateFileToString(fileReference));
     }
 
 
     /** Execute a string as a template against the current context,
      *  and return the result. */
     public String executeStringTemplate (String templateText) throws Exception
     {
         Template template = new StringTemplate(_wm.getBroker(), templateText);
         template.parse();
         String output = template.evaluateAsString(_context);
         return output;
     }
 
 
     public void store (String fileName, String value) throws Exception
     {
         Writer out = new FileWriter(fileName);
         out.write(value);
         out.close();
     }
 
 
    public void assertEvaluationEquals (String eval, Object result) throws Exception
     {
        String template = "#set $assertEvaluationEquals = " + eval;
         executeStringTemplate(template);
         if (result == null)
            assertTrue(_context.get("assertEvaluationEquals") == null);
         else
            assertTrue(result == null ? "null" : result.toString(), result.equals(_context.get("assertEvaluationEquals")));
     }
 
 
     /**
      * asserts that the specified template file (loaded via classpath) evaluates
      * to the given result text when evaluated against the current context
      */
     public void assertTemplateEquals (String templateName, String resultText) throws Exception
     {
         String result = null;
 
         try
         {
             result = executeStringTemplate(templateFileToString(templateName));
         }
         catch (Exception e)
         {
             System.err.println("Execution of /" + templateName + "/ threw " + e.getClass()
                     + ", expecting /" + resultText + "/");
         }
 
         if (result == null || !result.equals(resultText))
         {
             System.err.println("/" + templateName + "/ does not "
                     + "evaluate to /" + resultText + "/ "
                     + "result=/" + result + "/");
             assertTrue(false);
         }
     }
 
 
     /** Asserts that the given template text evalutes to the given result text
      * when evaluated against the current context */
     private void assertStringTemplate(String templateText,
                                      String resultText, boolean equals)
     {
         String result = null;
 
         try
         {
             result = executeStringTemplate(templateText);
         }
         catch (Exception e)
         {
             System.err.println("Execution of /" + templateText + "/"
                     + " threw " + e.getClass()
                     + "(" + e.getMessage() + ")"
                     + ", expecting /"
                     + resultText + "/");
             e.printStackTrace();
             assertTrue(false);
         }
         if (result == null)
             return;
 
         if (result.equals(resultText) != equals)
         {
             System.err.println("Execution of /" + templateText + "/"
                     + " yielded /" + result + "/, " + (equals ? "" : " not ")+
                     "expecting /" + resultText + "/");
             assertTrue(false);
         }
     }
 
 
     /** Asserts that the given template text evalutes to the given result text
      * when evaluated against the current context */
 
     public void assertStringTemplateEquals (String templateText,
                                             String resultText)
     {
         assertStringTemplate( templateText, resultText, true);
     }
 
     /** Asserts that the given template text evalutes to the given result text
      * when evaluated against the current context */
 
     public void assertStringTemplateNotEquals (String templateText,
                                             String resultText)
     {
         assertStringTemplate( templateText, resultText, false);
     }
 
 
     /** Asserts that the given template text throws the given exception
      * when evaluated against the current context, and the message text
      * matches the specified RE */
 
     public void assertStringTemplateThrows (String templateText,
                                             Class exceptionClass,
                                             String messageMatchText)
             throws Exception
     {
         String result = null;
         Exception caught = null;
 
         try
         {
             result = executeStringTemplate(templateText);
         }
         catch (Exception e)
         {
             caught = e;
         }
         if (caught == null)
         {
             System.err.println("Execution of /" + templateText + "/"
                     + " yielded /" + result + "/, expecting throw "
                     + exceptionClass);
             assertTrue(false);
         }
         else if (!exceptionClass.isAssignableFrom(caught.getClass()))
         {
             System.err.println("Execution of /" + templateText + "/"
                     + " threw " + caught.getClass() + ", expecting "
                     + exceptionClass);
             assertTrue(false);
         }
         else if (messageMatchText != null)
         {
             RE re = new RE(messageMatchText);
             if (!re.match(caught.getMessage()))
             {
                 System.err.println("Exception " + caught.getMessage()
                         + " does not match /"
                         + messageMatchText + "/");
                 assertTrue(false);
             }
         }
     }
 
 
     /** Asserts that the given template text throws the given exception
      * when evaluated against the current context */
     public void assertStringTemplateThrows (String templateText,
                                             Class exceptionClass)
             throws Exception
     {
         assertStringTemplateThrows(templateText, exceptionClass, null);
     }
 
 
     /** Asserts that the given template text matches the given regular
      * expression when evaluated against the current context */
 
     public void assertStringTemplateMatches (String templateText,
                                              String resultPattern)
             throws Exception
     {
         String result = null;
 
         try
         {
             result = executeStringTemplate(templateText);
         }
         catch (Exception e)
         {
             System.err.println("Execution of /" + templateText + "/"
                     + " threw " + e.getClass() + "/, expecting match /"
                     + resultPattern + "/");
             e.printStackTrace(System.err);
             assertTrue(false);
         }
         if (result == null)
             return;
 
         RE re = new RE(resultPattern);
         if (!re.match(result))
         {
             System.err.println("Execution of /" + templateText + "/"
                     + " yielded /" + result + "/, expecting match /"
                     + resultPattern + "/");
             assertTrue(false);
         }
     }
 
 
     public void assertStringTemplateThrowsWithCaught (String templateText,
                                                       Class exceptionClass)
     {
         String result = null;
         Throwable caught = null;
 
         try
         {
             result = executeStringTemplate(templateText);
         }
         catch (Exception e)
         {
             caught = e;
             if (!(caught instanceof PropertyException))
             {
                 System.err.println("Execution of /" + templateText + "/"
                         + " yielded /" + caught.getClass()
                         + "/, expecting throw PropertyException");
                 assertTrue(false);
             }
             else
             {
                 caught = ((PropertyException) e).getCaught();
             }
         }
         if (caught == null)
         {
             System.err.println("Execution of /" + templateText + "/"
                     + " yielded /" + result + "/, expecting throw "
                     + "PropertyException with caught exception "
                     + exceptionClass);
             assertTrue(false);
         }
         else if (!exceptionClass.isAssignableFrom(caught.getClass()))
         {
             System.err.println("Execution of /" + templateText + "/"
                     + " threw " + caught.getClass() + ", expecting throw "
                     + "PropertyException with caught exception "
                     + exceptionClass);
             assertTrue(false);
         }
     }
 
 
     /** Asserts that the specified expression is considered true or false
      * depending on the value of 'yesno' */
     public void assertExpr (String expr, boolean yesno)
     {
         assertStringTemplateEquals("#if (" + expr + ") {Yes} #else {No}",
                 yesno ? "Yes" : "No");
     }
 
 
     /** Asserts that the specified expression evaluates to the desired
      * boolean result */
     public void assertBooleanExpr (String expr, boolean result)
     {
         assertStringTemplateEquals("#set $result=(" + expr + ") $result",
                 result ? "true" : "false");
     }
 }
