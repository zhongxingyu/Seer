 /*
 * $Id: JUnitEETask.java,v 1.5 2002-10-15 20:21:57 o_rossmueller Exp $
  *
  * (c) 2002 Oliver Rossmueller
  *
  *
  */
 
 package org.junitee.anttask;
 
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.URLEncoder;
 import java.util.Enumeration;
 import java.util.Vector;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.FactoryConfigurationError;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.apache.tools.ant.BuildException;
 import org.apache.tools.ant.Project;
 import org.apache.tools.ant.Task;
 import org.w3c.dom.*;
 import org.xml.sax.SAXException;
 
 
 /**
  * This ant task runs server-side unit tests using the JUnitEE test runner.
  *
  * @author  <a href="mailto:oliver@oross.net">Oliver Rossmueller</a>
 * @version $Revision: 1.5 $
  */
 public class JUnitEETask extends Task {
 
 
   /**
    * Set the URL to call the JUnitEE test servlet.
    *
    * @param url URL of the JUnitEE test servlet
    */
   public void setUrl(String url) {
     this.url = url;
   }
 
 
   /**
    * Tell the task how to handle test failures. If set to true, the task will stop execution if a test failure or error occurs.
    * If set to false, the task will continue exectuion.
    *
    * @param value   true, if the task should stop execution on test failures and errors
    */
   public void setHaltonfailure(boolean value) {
     Enumeration enum = tests.elements();
 
     while (enum.hasMoreElements()) {
       ((JUnitEETest)enum.nextElement()).setHaltonfailure(value);
     }
   }
 
 
   /**
    * Tell the task how to handle errors. If set to true, the task will stop execution if an error occurs.
    * If set to false, the task will continue exectuion.
    *
    * @param value   true, if the task should stop execution on errors
    */
   public void setHaltonerror(boolean value) {
     Enumeration enum = tests.elements();
 
     while (enum.hasMoreElements()) {
       ((JUnitEETest)enum.nextElement()).setHaltonerror(value);
     }
   }
 
 
   /**
    * Tell the task to print a verbose test summary.
    *
    */
   public void setPrintsummary(boolean printSummary) {
     this.printSummary = printSummary;
   }
 
 
   /**
    * Tell the task which property should be set in case of an error.
    *
    * @param value name of the property to set in case of an error
    */
   public void setErrorproperty(String value) {
     Enumeration enum = tests.elements();
 
     while (enum.hasMoreElements()) {
       ((JUnitEETest)enum.nextElement()).setErrorproperty(value);
     }
   }
 
 
   /**
    * Tell the task which property should be set in case of an error or test failure.
    *
    * @param value name of the property to set in case of an error or test failure
    */
   public void setFailureproperty(String value) {
     Enumeration enum = tests.elements();
 
     while (enum.hasMoreElements()) {
       ((JUnitEETest)enum.nextElement()).setFailureproperty(value);
     }
   }
 
 
   /**
    * Create a nested test element.
    *
    * @return new test element
    */
   public JUnitEETest createTest() {
     JUnitEETest test = new JUnitEETest();
 
     tests.add(test);
     return test;
   }
 
 
   public void execute() throws BuildException {
     if (url == null) {
       throw new BuildException("You must specify the url attribute", location);
     }
     try {
       new URL(url);
     } catch (MalformedURLException e) {
       throw new BuildException(url + " is no valid URL");
     }
 
     Enumeration enum = tests.elements();
 
     while (enum.hasMoreElements()) {
       JUnitEETest test = (JUnitEETest)enum.nextElement();
 
       execute(test);
     }
 
   }
 
 
   protected void execute(JUnitEETest test) throws BuildException {
     StringBuffer arguments = new StringBuffer();
 
    arguments.append(url).append("?output=xml");
 
     if (test.getResource() != null) {
      arguments.append("&resource=").append(test.getResource());
     }
     if (test.getRunall()) {
      arguments.append("&all=true");
     } else if (test.getName() != null) {
      arguments.append("&suite=").append(URLEncoder.encode(test.getName()));
     } else {
       throw new BuildException("You must specify the test name or runall attribute", location);
     }
     try {
       URL url = new URL(arguments.toString());
       URLConnection con = url.openConnection();
       parseResult(con.getInputStream(), test);
     } catch (Exception e) {
       log("Failed to execute test: " + e, Project.MSG_ERR);
       throw new BuildException("Failed to execute test: " + e.getMessage());
     }
   }
 
 
   private void parseResult(InputStream in, JUnitEETest test) throws Exception {
     DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
     DocumentBuilder builder = factory.newDocumentBuilder();
     Document document = builder.parse(in);
     Element root = document.getDocumentElement();
     NodeList testcases = root.getElementsByTagName("testsuite");
     boolean success = true;
 
     log("Running tests ...", Project.MSG_INFO);
     for (int i = 0; i < testcases.getLength(); i++) {
       Node node = testcases.item(i);
       NamedNodeMap attributes = node.getAttributes();
       String testClass = attributes.getNamedItem("name").getNodeValue();
       String runs = attributes.getNamedItem("tests").getNodeValue();
       int errors = Integer.parseInt(attributes.getNamedItem("errors").getNodeValue());
       int failures = Integer.parseInt(attributes.getNamedItem("failures").getNodeValue());
       String time = attributes.getNamedItem("time").getNodeValue();
 
       if (printSummary) {
         StringBuffer buffer = new StringBuffer();
         buffer.append(testClass).append(" (runs: ").append(runs).append(" errors: ").append(errors);
         buffer.append(" failures: ").append(failures).append(" time: ").append(time).append(" sec)");
         log(buffer.toString(), Project.MSG_INFO);
       }
       if (errors != 0) {
         success = false;
         if (test.getHaltonerror() || test.getHaltonfailure()) {
           throw new BuildException("Test " + testClass + " failed.");
         }
       }
       if (failures != 0) {
         success = false;
         if (test.getHaltonfailure()) {
           throw new BuildException("Test " + testClass + " failed.");
         }
       }
     }
     if (success) {
       log("Test successful", Project.MSG_INFO);
     } else {
       log("TEST FAILED", Project.MSG_INFO);
     }
   }
 
 
   private String url;
   private Vector tests = new Vector();
   private boolean printSummary = false;
 }
