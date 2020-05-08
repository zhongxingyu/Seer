 /**
 * $Id: JUnitEEServlet.java,v 1.6 2002-09-05 13:35:29 o_rossmueller Exp $
  * $Source: C:\Users\Orionll\Desktop\junitee-cvs/JUnitEE/src/testrunner/org/junitee/servlet/JUnitEEServlet.java,v $
  */
 
 package org.junitee.servlet;
 
 
 import java.io.*;
 import java.util.*;
 import java.util.jar.*;
 
 import javax.servlet.*;
 import javax.servlet.http.*;
 
 import org.junitee.runner.TestRunner;
 import org.junitee.runner.JUnitEEOutputProducer;
 import org.junitee.output.HTMLOutput;
 import org.junitee.output.XMLOutput;
 
 
 /**
  * This is an abstract base class.  In order to use it, you must create
  * a derived class *in the WEB-INF/classes directory* of your web application.
  * That class should simply implement the getDynamicClassLoader() method.
  * This is so that we can use the app server's special class loader which
  * both intelligently reloads changed classes and knows where to look for
  * web application class files.
  *
  * A future version of this servlet & runner would start the test in another
  * thread, store results in the session, and use the HTTP Refresh header to
  * cause the client to poll for summary results until the test is complete.
  * Right now the entire test is run within the scope of a single request, so
  * long-running tests could produce a timeout.
  *
  * @author Jeff Schnitzer (jeff@infohazard.org)
  * @author <a href="mailto:oliver@oross.net">Oliver Rossmueller</a>
  */
 public class JUnitEEServlet extends HttpServlet {
   /**
    * The form parameter which defines the name of the suite
    * class to run.  This parameter can appear more than once
    * to run multiple test suites.
    */
   protected static final String PARAM_SUITE = "suite";
 
   /**
    * The form parameter which defines the test out of the defined suite to be run.
    */
   protected static final String PARAM_TEST = "test";
 
   /**
    * The form parameter which defines if
    * resources should be checked to run all included test cases
    */
   protected static final String PARAM_RUN_ALL = "all";
   protected static final String PARAM_SEARCH = "search";
   protected static final String PARAM_OUTPUT = "output";
 
   protected static final String OUTPUT_HTML = "html";
   protected static final String OUTPUT_XML = "xml";
 
 
   private static final String RESOURCE_PREFIX = "resource";
 
 
   private String searchResources;
 
 
   /**
    * Answer the classloader used to load the test classes. The default implementation
    * answers the classloader of this class, which usally will be the classloader of
    * the web application the servlet is a part of.
    *
    * If this default behaviour does not work for you, overwrite this method and answer
    * the classloader that fits your needs.
    */
   protected ClassLoader getDynamicClassLoader() {
     return getClass().getClassLoader();
   }
 
 
   public void init(ServletConfig config) throws ServletException {
     super.init(config);
 
     searchResources = config.getInitParameter("searchResources");
   }
 
 
   /**
    */
   public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
     String resource = request.getPathInfo();
 
     if (resource != null) {
       streamResource(resource, response);
     }
 
     // Set up the response
     response.setHeader("Cache-Control", "no-cache");
     PrintWriter pw = response.getWriter();
     String test = request.getParameter(PARAM_TEST);
     String runAll = request.getParameter(PARAM_RUN_ALL);
     String[] testClassNames = null;
 
     if (runAll != null) {
       testClassNames = searchForTests(request.getParameterValues(PARAM_SEARCH));
     } else {
       testClassNames = request.getParameterValues(PARAM_SUITE);
     }
 
     if (testClassNames == null) {
       // TODO: move to method
       pw.println("<html>");
       pw.println("<head><title> Error </title></head>");
       pw.println("<body>");
       if (runAll != null) {
         pw.println("<p> No test class(es) specified. </p>");
         pw.println("<p>");
         pw.println("  You requested all test cases to be run by setting the \"all\" parameter,");
         pw.println("  but not test case was found searching the resources");
         pw.print("  \"");
         pw.print(searchResources);
         pw.println("\"");
         pw.println("</p>");
       } else {
         pw.println("<p> No test class(es) specified. </p>");
         pw.println("<p>");
         pw.println("  This servlet uses the form parameter \"suite\" to identify");
         pw.println("  which Test to run.  The parameter can appear multiple times");
         pw.println("  to run multiple tests.");
         pw.println("</p>");
       }
       pw.println("</body>");
       pw.println("</html>");
     } else {
       if ((test != null) && (testClassNames.length != 1)) {
         // TODO: print error message
         throw new ServletException("Error");
       }
 
 
       TestRunner tester = null;
 
       JUnitEEOutputProducer output = getOutputProducer(request.getParameter(PARAM_OUTPUT), response, request.getContextPath() + request.getServletPath());
       tester = new TestRunner(this.getDynamicClassLoader(), output);
       if (test == null) {
         tester.run(testClassNames);
       } else {
         tester.run(testClassNames[0], test);
       }
     }
   }
 
 
   private void streamResource(String resource, HttpServletResponse response) throws IOException {
     InputStream in = getClass().getClassLoader().getResourceAsStream(RESOURCE_PREFIX + resource);
     OutputStream out = response.getOutputStream();
     byte[] buffer = new byte[1024];
     int r = 0;
 
     response.setContentType("image/gif");
     while ((r = in.read(buffer)) != -1) {
       out.write(buffer, 0, r);
     }
     in.close();
     out.close();
   }
 
 
   /**
    */
   public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
     // Chain to get so that either will work
     this.doGet(request, response);
   }
 
 
   protected String[] searchForTests(String[] param) {
     StringBuffer buffer = new StringBuffer();
 
    if (param != null) {
      for (int i = 0; i < param.length; i++) {
        buffer.append(param[i]).append(",");
      }
     }
     return searchForTests(buffer.toString());
   }
 
 
   /**
    * Search all resources set via the searchResources init parameter for classes ending with "Tests"
    */
   protected String[] searchForTests(String param) {
     if (searchResources == null && param == null) {
       return null;
     }
 
     StringTokenizer tokenizer;
 
     if (param != null) {
       tokenizer = new StringTokenizer(param, ",");
     } else {
       tokenizer = new StringTokenizer(searchResources, ",");
     }
 
     List tests = new ArrayList();
 
     while (tokenizer.hasMoreTokens()) {
       String token = tokenizer.nextToken().trim();
 
       try {
         InputStream in = getServletContext().getResourceAsStream("WEB-INF/lib/" + token);
 
         if (in != null) {
           JarInputStream jar = new JarInputStream(in);
 
           while (jar.available() != 0) {
             JarEntry entry = jar.getNextJarEntry();
             String name = entry.getName();
 
             if (name.endsWith("Tests.class") || name.endsWith("Test.class")) {
               tests.add(name.substring(0, name.length() - 6).replace('/', '.'));
             }
           }
         }
       } catch (Exception e) {
       }
     }
     String[] answer = new String[tests.size()];
     tests.toArray(answer);
     return answer;
   }
 
 
   /**
    * Answer the default output format of the test report. This implementation returns html as the default output. It
    * is possible to set the output format by using the <code>output</code> request parameter. Overwrite this method
    * in your subclass to change the output format without the need for the request parameter.
    *
    * @return
    */
   protected String getDefaultOutput() {
     return OUTPUT_HTML;
   }
 
 
   /**
    * Answer the output producer for the given output format.
    *
    * @param outputParam required output format
    * @param response  response object of the current servlet request
    * @param servletPath path of this servlet
    * @return  output producer
    * @throws IOException
    */
   protected JUnitEEOutputProducer getOutputProducer(String outputParam, HttpServletResponse response, String servletPath) throws IOException {
     String output = outputParam;
 
     if (output == null) {
       output = getDefaultOutput();
     }
 
     if (output.equals(OUTPUT_HTML)) {
       return new HTMLOutput(response, servletPath);
     }
     if (output.equals(OUTPUT_XML)) {
       return new XMLOutput(response, servletPath);
     }
     return null;
   }
 
 }
