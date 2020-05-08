 /**
 * $Id: HTMLOutput.java,v 1.2 2002-09-01 13:05:32 o_rossmueller Exp $
  * $Source: C:\Users\Orionll\Desktop\junitee-cvs/JUnitEE/src/testrunner/org/junitee/output/HTMLOutput.java,v $
  */
 
 package org.junitee.output;
 
 
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.io.CharArrayWriter;
 import java.text.NumberFormat;
 import java.util.*;
 
 import junit.framework.AssertionFailedError;
 import junit.framework.Test;
 
 import org.junitee.runner.JUnitEETestListener;
 import org.junitee.runner.TestInfo;
 import org.junitee.runner.TestSuiteInfo;
 
 
 /**
  * This class implements the {@link JUnitEETestListener} interface and produces an HTML test report.
  *
  * @author  <a href="mailto:oliver@oross.net">Oliver Rossmueller</a>
 * @version $Revision: 1.2 $
  */
 public class HTMLOutput implements JUnitEETestListener {
   protected static final String ERROR     = "Error";
   protected static final String FAILURE   = "Failure";
   protected static final String PASSED    = "Passed";
   protected static final String UNKNOWN   = "Unknown";
 
   protected static final String RESOURCE_RED_BULLET     = "bullets_red_ball.gif";
   protected static final String RESOURCE_YELLOW_BULLET  = "bullets_yellow_ball.gif";
   protected static final String RESOURCE_GREEN_BULLET   = "bullets_green_ball.gif";
   protected static final String RESOURCE_INFO           = "info.png";
 
   private PrintWriter pw;
   private long timestamp;
   private List testInfo = new ArrayList();
   private Map suiteInfo = new HashMap();
   private TestInfo currentInfo;
   private boolean failure = false;
   private String servletPath;
 
 
   /**
    */
   public HTMLOutput(PrintWriter pw, String servletPath) {
     this.pw = pw;
     this.servletPath = servletPath;
   }
 
 
   /**
    */
   protected void printHeader() {
     // Admittedly, much of this stylesheet is unused at the moment.
 
     pw.println("<html>");
     pw.println("<head><title> JUnit Test Results </title></head>");
 
     pw.println("<style type=\"text/css\">");
     pw.println("	<!--");
     pw.println("		body		{ font-family: lucida, verdana, sans-serif; font-size: 10pt;");
     pw.println("						background-color: #FFFFFF }");
 
     pw.println("		a:link		{ color: black; }");
     pw.println("		a:active	{ color: black; }");
     pw.println("		a:visited	{ color: black; }");
     pw.println("		a:hover		{ color: blue; }");
 
     pw.println("		.pageTitle	{ font-size: 2em; font-weight: bold;");
     pw.println("						letter-spacing: 0.25em; text-align: center;");
     pw.print("						color: #FFFFFF;");
 
     if (failure) {
       pw.println("background-color: #980000 }");
     } else {
       pw.println("background-color: #03A35D }");
     }
 
     pw.println("		.sectionTitle	{ font-weight: bold; ");
     pw.println("							background-color: #F4E5E5;");
     pw.println("							border-top-width: 1px; border-bottom-width: 0;");
     pw.println("							border-left-width: 0; border-right-width: 0;");
     pw.println("							border-style: solid; border-color: #980000 }");
     pw.println("		.cell       	{ color: black; background-color: #CCCCFF }");
     pw.println("		.passedcell       	{ color: black; background-color: #CCCCFF }");
     pw.println("		.failedcell       	{ color: black; background-color: #CCCCFF }");
   //  pw.println("		.passedcell  	{ color: black; background-color: lightgreen }");
   //  pw.println("		.failedcell  	{ color: black; background-color: #EF4A4A }");
     pw.println("	-->");
     pw.println("</style>");
 
     pw.println("<body>");
 
     // Print a nice header
     pw.println("<table cellspacing=\"0\" cellpadding=\"0\" width=\"100%\">");
     pw.println("	<tr> <td class=\"pageTitle\"> <h1> JUnit Test Results </h1> </td> </tr>");
     pw.println("</table>");
 
     /*   pw.println("<p> The following units will be tested:");
        pw.println("</p>");
        pw.println("<ul>");
 
        Iterator iterator = suiteInfo.keySet().iterator();
 
        while (iterator.hasNext()) {
          pw.println("  <li> <tt>" + iterator.next() + "</tt> </li>");
        }
        pw.println("</ul>");*/
 
   }
 
 
   /**
    */
   protected void printFooter() {
     pw.println("</body>");
     pw.println("</html>");
   }
 
 
   /**
    */
   protected void printSummary() {
     pw.println("<h2> Summary of results </h2>");
     pw.println("<p> <table border=\"1\" cellspacing=\"0\" cellpadding=\"2\" bgcolor=\"#CCCCFF\" width=\"100%\">");
 
     Iterator iterator = suiteInfo.values().iterator();
 
     while (iterator.hasNext()) {
       TestSuiteInfo suite = (TestSuiteInfo)iterator.next();
 
       pw.println("<tr>");
       pw.println("  <td>");
       pw.println("  <tt> " + suite + " </tt>");
       pw.println("  </td>");
       if (suite.successful()) {
         pw.println("  <td class=\"passedcell\">Succeeded</td>");
       } else {
         pw.println("  <td class=\"failedcell\">FAILED</td>");
       }
       pw.println("</tr>");
     }
 
     pw.println("</table> </p>");
   }
 
 
   /**
    */
   protected void printDetails() {
     int size = testInfo.size();
 
     for (int i = 0; i < size; i++) {
       printTestResult((TestInfo)testInfo.get(i));
       if (i != (size - 1))
         pw.println("<hr width=50%>");
     }
   }
 
 
   /**
    */
   protected void printTestResult(TestInfo test) {
     pw.println("<p><h3> Test results for <tt> " + test.getClass().getName() + " </tt> </h3> </p>");
     pw.println("<p> Elapsed time: " + elapsedTimeAsString(test.getElapsedTime()) + " seconds.</p>");
 
     //printResult(test);
   }
 
 
   private String elapsedTimeAsString(long value) {
     return NumberFormat.getInstance().format((double)value / 1000);
   }
 
 
   /**
    */
 /*  protected void printResult(TestInfo test) {
     if (test.successful()) {
       pw.println("<p> Test completed successfully. </p>");
     }  else {
       pw.println("<p> <font color=red> <strong> TEST FAILED </strong> </font>");
       pw.println("<table border=1>");
 
       if (test.hasError()) {
         printTestFailures(ERROR, test.getErrors(), tro);
       }
 
       if (test.hasFailure()) {
         printTestFailures(FAILURE, test.getFailures(), tro);
       }
 
       pw.println("</table> </p>");
     }
 
   }*/
 
 
   protected void printMethodList() {
     pw.println("<h2> List of executed tests</h2>");
     pw.println("<p> <table border=\"0\" cellspacing=\"2\" cellpadding=\"3\" width=\"100%\">");
 
     Iterator suites = suiteInfo.values().iterator();
     while (suites.hasNext()) {
       TestSuiteInfo suite = (TestSuiteInfo)suites.next();
 
       pw.println("<tr><td colspan=\"4\" class=\"sectionTitle\">" + suite.getTestClassName() + "</td></tr>");
 
       Iterator tests = suite.getTests().iterator();
 
       while (tests.hasNext()) {
         TestInfo test = (TestInfo)tests.next();
 
         if (test.successful()) {
           pw.println("<tr><td class=\"passedcell\">" + singleTestLink(test, image(RESOURCE_GREEN_BULLET, "Run test")) + "</td>");
         } else if (test.hasError()) {
           pw.println("<tr><td class=\"failedcell\">" + singleTestLink(test, image(RESOURCE_RED_BULLET, "Run test")) + "</td>");
         } else {
           pw.println("<tr><td class=\"failedcell\">" + singleTestLink(test, image(RESOURCE_YELLOW_BULLET, "Run test")) + "</td>");
         }
         pw.print("<td class=\"cell\">");
         if (test.successful()) {
           pw.print("&nbsp;");
         } else {
           pw.print("<a href=\"#" + test + "\">");
           pw.print(image(RESOURCE_INFO, "Show details"));
           pw.print("</a>");
         }
         pw.println("</td>");
         pw.println("<td width=\"100%\" class=\"cell\">" + test + "</td><td class=\"cell\" align=\"right\">");
         pw.println(elapsedTimeAsString(test.getElapsedTime()) + "&nbsp;sec</td></tr>");
       }
       pw.println("<tr><td colspan=\"4\">&nbsp;</td></tr>");
     }
     pw.println("</table>");
   }
 
 
   private String image(String resource, String alt) {
     StringBuffer buffer = new StringBuffer();
 
     buffer.append("<img src=\"").append(servletPath).append("/").append(resource).append("\" border=\"0\" height=\"14\" widht=\"14\" alt=\"");
     buffer.append(alt).append("\">");
     return buffer.toString();
   }
 
 
   private String singleTestLink(TestInfo test, String text) {
     StringBuffer buffer = new StringBuffer();
 
     buffer.append("<a href=\"").append(servletPath).append("?suite=").append(test.getTestClassName());
     buffer.append("&test=").append(test.getTestName()).append("\">").append(text).append("</a>");
     return buffer.toString();
   }
 
 
   /**
    * This method converts texts to be displayed on
    * html-page. Following conversion are done
    * "<" => "&lt;" , ">" => "&gt;" and "&" => "&amp;"
    * @author Kaarle Kaila
    * @since 10.10.2001
    *
    * And replaced \n with html breaks - jeff
    */
   private String htmlText(String text) {
     StringBuffer sb = new StringBuffer();
     char c;
     if (text == null) return "";
     for (int i = 0; i < text.length(); i++) {
       c = text.charAt(i);
       switch (c) {
         case '<':
           sb.append("&lt;");
           break;
         case '>':
           sb.append("&gt;");
           break;
         case '&':
           sb.append("&amp;");
           break;
         case '\n':
           sb.append("<br>");
           break;
         default:
           sb.append(c);
       }
     }
     return sb.toString();
   }
 
 
   protected void printErrorsAndFailures() {
     if (!failure) {
        return;
      }
 
     pw.println("<h2> List of errors and failures</h2>");
     pw.println("<p> <table border=\"0\" cellspacing=\"2\" cellpadding=\"3\" width=\"100%\">");
 
     Iterator suites = suiteInfo.values().iterator();
     while (suites.hasNext()) {
       TestSuiteInfo suite = (TestSuiteInfo)suites.next();
 
       Iterator tests = suite.getTests().iterator();
 
       while (tests.hasNext()) {
         TestInfo test = (TestInfo)tests.next();
 
         if (!test.successful()) {
           pw.println("<a name=\"" + test + "\"></a>");
           pw.println("<tr><td class=\"sectionTitle\">" + test + "</td></tr>");
 
           Iterator errors = test.getErrors().iterator();
 
           while (errors.hasNext()) {
             CharArrayWriter buffer = new CharArrayWriter();
             Throwable t = (Throwable)errors.next();
 
             pw.println("<tr><td class=\"cell\">");
             pw.println(t.getMessage());
            pw.println("</td>");
 
             pw.println("<tr><td class=\"cell\">");
             t.printStackTrace(new PrintWriter(buffer));
             pw.println(htmlText(buffer.toString()));
             pw.println("</td>");
           }
           Iterator failures = test.getFailures().iterator();
 
           while (failures.hasNext()) {
             CharArrayWriter buffer = new CharArrayWriter();
             Throwable t = (Throwable)failures.next();
 
             pw.println("<tr><td class=\"cell\">");
             pw.println(t.getMessage());
            pw.println("</td>");
 
             pw.println("<tr><td class=\"cell\">");
             t.printStackTrace(new PrintWriter(buffer));
             pw.println(htmlText(buffer.toString()));
             pw.println("</td>");
           }
         }
        pw.println("<tr><td>&nbsp;</td></tr>");
       }
      pw.println("</table>");
     }
   }
   /**
    */
 /*
   protected void printTestFailures(String type, Enumeration errors, TestRunOutput tro) {
     String tmp1,tmp2;
 
     while (errors.hasMoreElements()) {
       TestFailure bad = (TestFailure) errors.nextElement();
 
 
       Test ff = (Test) bad.failedTest();
       tmp1 = ff.toString();
       int ii;
       for (ii = 0; ii < tro.methodData.size(); ii++) {
         tmp2 = (String) tro.methodData.get(ii);
         if (tmp2.length() > 2) {
           if (tmp1.equals(tmp2.substring(1))) {
             tro.methodData.set(ii, type.substring(0, 1) + tmp2.substring(1));
           }
         }
       }
 
       pw.println("<tr valign=top>");
       pw.println("  <td>");
       pw.println("    " + type);
       pw.println("  </td>");
       pw.println("  <td>");
       pw.println("    " + htmlText(bad.toString()));
       pw.println("  </td>");
       pw.println("  <td>");
       pw.println("    <pre>");
 
       StringWriter sw = new StringWriter();
       PrintWriter spw = new PrintWriter(sw);
       bad.thrownException().printStackTrace(spw);
       pw.write(htmlText(sw.toString()));
 //			bad.thrownException().printStackTrace(pw);
       this.printEJBExceptionDetail(bad.thrownException());
       pw.println("    </pre>");
       pw.println("  </td>");
       pw.println("</tr>");
     }
   }
 */
 
 
 
 
 
   /**
    * Checks to see if t is a RemoteException containing
    * an EJBException, and if it is, prints the nested
    * exception inside the EJBException.  This is necessary
    * because the EJBException.printStackTrace() method isn't
    * intelligent enough to print the nexted exception.
    */
   protected void printEJBExceptionDetail(Throwable t) {
     if (t instanceof java.rmi.RemoteException) {
       java.rmi.RemoteException remote = (java.rmi.RemoteException)t;
       if (remote.detail != null && remote.detail instanceof javax.ejb.EJBException) {
         javax.ejb.EJBException ejbe = (javax.ejb.EJBException)remote.detail;
         if (ejbe.getCausedByException() != null) {
           pw.println("Nested exception is:");
 
           StringWriter sw = new StringWriter();
           PrintWriter spw = new PrintWriter(sw);
           ejbe.getCausedByException().printStackTrace(spw);
 
           pw.write(htmlText(sw.toString()));
 //					ejbe.getCausedByException().printStackTrace(pw);
         }
       }
     }
   }
 
 
   public void addError(Test test, Throwable t) {
     currentInfo.addError(t);
     failure = true;
   }
 
 
   /**
    */
   public void addFailure(Test test, Throwable t) {
     currentInfo.addFailure(t);
     failure = true;
   }
 
 
   /**
    */
   public void addFailure(Test test, AssertionFailedError t) {
     currentInfo.addFailure(t);
     failure = true;
   }
 
 
   /**
    */
   public void endTest(Test test) {
     long elapsedTime = System.currentTimeMillis() - timestamp;
 
     currentInfo.setElapsedTime(elapsedTime);
     testInfo.add(currentInfo);
     addToSuite(currentInfo);
     currentInfo = null;
   }
 
 
   /**
    */
   public void startTest(Test test) {
     currentInfo = new TestInfo(test);
     timestamp = System.currentTimeMillis();
   }
 
 
   public void runFailed(String message) {
   }
 
 
   private void addToSuite(TestInfo info) {
     String className = info.getTest().getClass().getName();
     TestSuiteInfo suite = (TestSuiteInfo)suiteInfo.get(className);
 
     if (suite == null) {
       suite = new TestSuiteInfo(className);
       suiteInfo.put(className, suite);
     }
     suite.add(info);
   }
 
 
   /**
    * Write the test report
    */
   public void writeOutput() {
     printHeader();
     printMethodList();
     printErrorsAndFailures();
     printFooter();
   }
 }
