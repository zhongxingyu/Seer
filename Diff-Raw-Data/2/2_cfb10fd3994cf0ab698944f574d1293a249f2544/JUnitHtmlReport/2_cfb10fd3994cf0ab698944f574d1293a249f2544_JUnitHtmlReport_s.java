 /*
  * Created on Jul 19, 2006
  */
 package edu.duke.cabig.catrip.test.report.report;
 
 import java.io.PrintStream;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Hashtable;
 
 import edu.duke.cabig.catrip.test.report.data.TestCase;
 import edu.duke.cabig.catrip.test.report.data.TestSuite;
 
 public class JUnitHtmlReport
 	implements JUnitReport
 {
 	private class SuiteStats
 	{
 		int tests = 0;
 		int failures = 0;
 		int errors = 0;
 		int time = 0;		
 	}
 	
 	public void writeReport(TestSuite[] suites, boolean useTestType, PrintStream out)
 	{
 		// header
 		out.println("<html><head><title>JUnitHtmlReport</title><head>");
 		printStyles(out);
 		out.println("<body>");
 		// title
 		out.println("<p><center><h1>JUnitHtmlReport</h1></center></p>");
 
 		// get suite table
 		Hashtable<String,TestSuite[]> suiteTable;
 		if (useTestType) {
 			suiteTable = organizeByTestType(suites);
 		} else {
 			suiteTable = new Hashtable<String,TestSuite[]>(1);
 			suiteTable.put("all", suites);
 		}
 		// order suites
 		ArrayList<String> suiteTypeList = new ArrayList<String>(suiteTable.size());
 		suiteTypeList.addAll(suiteTable.keySet());
 		Collections.sort(suiteTypeList);
 		moveToFront(suiteTypeList, "system");
 		moveToFront(suiteTypeList, "unit");
 		moveToBack(suiteTypeList, "miscellaneous");
 		
 		// summary
 		out.println("<p><h2>Summary</h2>");
 		out.println("<table border=\"1\">");
 		out.println(
 			"<tr>" +
 			"<td valign=\"top\"><b>Test Suite</b></td>" +
 			"<td valign=\"top\"><b>Tests</b></td>" +
 			"<td valign=\"top\"><b>Failures</b></td>" +
 			"<td valign=\"top\"><b>Errors</b></td>" +
 			"<td valign=\"top\"><b>Time (sec)</b></td>" +
 			"</tr>"
 		);
 		
 		
 		for (int i = 0; i < suiteTypeList.size(); i++) {
 			String suiteType = suiteTypeList.get(i);
 			TestSuite[] mySuites = suiteTable.get(suiteType);
 			
 			if (useTestType) {
 				SuiteStats stats = calculateSuiteStats(mySuites);
 				
 				String outline = String.valueOf(i+1);
 
 				out.println("<tr>");
 				out.println("<td valign=\"top\"><b>" + outline + ". <a href=\"#" + outline + "\">" + suiteType + " tests</b></a></td>");
 				out.println("<td valign=\"top\">" + stats.tests + "</td>");
 				out.println("<td valign=\"top\">" + stats.failures + "</td>");
 				out.println("<td valign=\"top\">" + stats.errors + "</td>");
 				out.println("<td valign=\"top\">" + stats.time + "</td>");
 				out.println("</tr>");			
 			}
 			
 			for (int j = 0; j < mySuites.length; j++) {
				TestSuite suite = suites[j];
 
 				String outline = String.valueOf(j+1);
 				if (useTestType) outline = String.valueOf(i+1) + "." + String.valueOf(j+1);
 				
 				out.println("<tr>");
 				out.println("<td valign=\"top\">" + outline + ". <a href=\"#" + outline + "\">" + suite.name + "</a></td>");
 				out.println("<td valign=\"top\">" + suite.tests + "</td>");
 				out.println("<td valign=\"top\">" + suite.failures + "</td>");
 				out.println("<td valign=\"top\">" + suite.errors + "</td>");
 				out.println("<td valign=\"top\">" + suite.time + "</td>");
 				out.println("</tr>");			
 			}
 		}
 
 		SuiteStats stats = calculateSuiteStats(suites);
 		out.println(
 			"<tr>" +
 			"<td valign=\"top\"><b>Total:</b></td>" +
 			"<td valign=\"top\">" + stats.tests + "</td>" +
 			"<td valign=\"top\">" + stats.failures + "</td>" +
 			"<td valign=\"top\">" + stats.errors + "</td>" +
 			"<td valign=\"top\">" + stats.time + " (sec)</td>" +
 			"</tr>"
 		);		
 		out.println("</table>");
 		out.println("</p>");
 
 		// each suite
 		for (int i = 0; i < suiteTypeList.size(); i++) {
 			String suiteType = suiteTypeList.get(i);
 			TestSuite[] mySuites = suiteTable.get(suiteType);
 			
 			if (useTestType) {
 				String outline = String.valueOf(i+1);
 				out.println("<p><a name=\"" + outline + "\"><h2>" + outline + ". " + suiteType + " tests</h2></a></p>");
 			}
 			
 			for (int j = 0; j < mySuites.length; j++) {
 				TestSuite suite = mySuites[j];
 				
 				String suiteOutline = String.valueOf(j+1);
 				String suiteHeader = "h2";
 				if (useTestType) {
 					suiteOutline = String.valueOf(i+1) + "." + String.valueOf(j+1);
 					suiteHeader = "h3";
 				}
 				out.println("<p><a name=\"" + suiteOutline + "\"><" + suiteHeader + ">" + suiteOutline + ". " + suite.name + "</" + suiteHeader + "></a></p>");
 	
 				out.println("<dl><dl><dt>");
 				out.println("<table border=\"0\">");
 				out.println("<tr><td valign=\"top\"><b>Description:</b></td><td>" + (suite.docText == null || suite.docText.equals("") ? "NA" : suite.docText) + "</td>");
 				out.println("<tr><td valign=\"top\"><b>Tests:</b></td><td>" +  suite.tests + "</td>");
 				out.println("<tr><td valign=\"top\"><b>Failures:</b></td><td>" +  suite.failures + "</td>");
 				out.println("<tr><td valign=\"top\"><b>Errors:</b></td><td>" +  suite.errors + "</td>");
 				out.println("<tr><td valign=\"top\"><b>Time&nbsp;(sec):</b></td><td>" +  suite.time + "</td>");
 				out.println("</table>");
 				out.println("</dt></dl></dl>");
 	
 				for (int k = 0; k < suite.testCases.size(); k++) {
 					TestCase test = suite.testCases.get(k);
 					
 					String status = "Success";
 					if (test.failure != null && test.failure.isError) status = "Error";
 					else if (test.failure != null && ! test.failure.isError) status = "Failure";
 					
 					String testOutline = String.valueOf(j+1) + "." + String.valueOf(k+1);
 					String testeHeader = "h3";
 					if (useTestType) {
 						testOutline = String.valueOf(i+1) + "." + String.valueOf(j+1) + "." + String.valueOf(k+1);
 						testeHeader = "h4";
 					}
 					out.println("<p><a name=\"" + testOutline + "\"><" + testeHeader + ">" + testOutline + ". " + test.name + "</" + testeHeader + "></a></p>");
 
 					out.println("<dl><dl><dt>");
 					out.println("<table border=\"0\">");
 					out.println("<tr><td valign=\"top\"><b>Description:</b></td><td>" + (test.docText == null || test.docText.equals("") ? "NA" : test.docText) + "</td>");
 					out.println("<tr><td valign=\"top\"><b>Time&nbsp;(sec):</b></td><td>" +  test.time + "</td>");
 					out.println("<tr><td valign=\"top\"><b>Status:</b></td><td>" +  status.toUpperCase());
 					if (test.failure != null) {
 						out.println("<br/>" + test.failure.type);
 						out.println("<pre>" +  test.failure.stackTrace + "</pre>");
 					}
 					out.println("</td>");
 					out.println("</table>");
 					out.println("</dt></dl></dl>");
 				}
 			}
 		}
 		
 		out.println("</body></html>");
 	}
 
 	private void printStyles(PrintStream out)
 	{
 		out.println("     <style type=\'text/css\'>\r\n" + 
 				"\r\n" + 
 				"    body { background-color:white; }\r\n" + 
 				"\r\n" + 
 				"    p,td,div,span                      {font-size: 10pt; font-family: Arial, Verdana, Helvetica, sans-serif; color: black; text-decoration: none;}\r\n" + 
 				"\r\n" + 
 				"    p.title                   { font-size: 14pt; font-weight: bold; font-family: Arial, Verdana, Helvetica, sans-serif; color: black; text-decoration: none;}\r\n" + 
 				"    p.header,div.header       { font-size: 14pt; font-weight: bold; font-family: Arial, Verdana, Helvetica, sans-serif; color: black; text-decoration: none;}\r\n" + 
 				"    td.nav                    { font-size: 10pt; font-weight: bold; font-family: Arial, Verdana, Helvetica, sans-serif; color: black; text-decoration: none;}\r\n" + 
 				"\r\n" + 
 				"    \r\n" + 
 				"      a:link                         { font-size: 10pt; font-family: Arial, Verdana, Helvetica, sans-serif; color: #000080; text-decoration: underline;}\r\n" + 
 				"      a:link:hover                   { font-size: 10pt; font-family: Arial, Verdana, Helvetica, sans-serif; color: #008000; text-decoration: underline;}\r\n" + 
 				"      a:link:active                  { font-size: 10pt; font-family: Arial, Verdana, Helvetica, sans-serif; color: #800000; text-decoration: underline;}\r\n" + 
 				"      a:link:visited                 { font-size: 10pt; font-family: Arial, Verdana, Helvetica, sans-serif; color: #800080; text-decoration: underline;}\r\n" + 
 				"      a:link:visited:hover           { font-size: 10pt; font-family: Arial, Verdana, Helvetica, sans-serif; color: #008000; text-decoration: underline;}\r\n" + 
 				"    \r\n" + 
 				"    \r\n" + 
 				"\r\n" + 
 				"    \r\n" + 
 				"      a.nav:link                     { font-size: 10pt; font-weight: bold; font-family: Arial, Verdana, Helvetica, sans-serif; color: #000080; text-decoration: none;}\r\n" + 
 				"      a.nav:link:hover               { font-size: 10pt; font-weight: bold; font-family: Arial, Verdana, Helvetica, sans-serif; color: #008000; text-decoration: underline;}\r\n" + 
 				"      a.nav:link:active              { font-size: 10pt; font-weight: bold; font-family: Arial, Verdana, Helvetica, sans-serif; color: #800000; text-decoration: none;}\r\n" + 
 				"      a.nav:link:visited             { font-size: 10pt; font-weight: bold; font-family: Arial, Verdana, Helvetica, sans-serif; color: #000080; text-decoration: none;}\r\n" + 
 				"      a.nav:link:visited:hover       { font-size: 10pt; font-weight: bold; font-family: Arial, Verdana, Helvetica, sans-serif; color: #008000; text-decoration: underline;}\r\n" + 
 				"    \r\n" + 
 				"    \r\n" + 
 				"\r\n" + 
 				"    td.navActive              { font-size: 10pt; font-weight: bold; font-family: Arial, Verdana, Helvetica, sans-serif; color: #008000; text-decoration: none;}\r\n" + 
 				"\r\n" + 
 				"    \r\n" + 
 				"      a.navActive:link          { font-size: 10pt; font-weight: bold; font-family: Arial, Verdana, Helvetica, sans-serif; color: #008000; text-decoration: none;}\r\n" + 
 				"      a.navActive:link:hover         { font-size: 10pt; font-weight: bold; font-family: Arial, Verdana, Helvetica, sans-serif; color: #008000; text-decoration: underline;}\r\n" + 
 				"      a.navActive:link:active        { font-size: 10pt; font-weight: bold; font-family: Arial, Verdana, Helvetica, sans-serif; color: #800000; text-decoration: none;}\r\n" + 
 				"      a.navActive:link:visited       { font-size: 10pt; font-weight: bold; font-family: Arial, Verdana, Helvetica, sans-serif; color: #008000; text-decoration: none;}\r\n" + 
 				"      a.navActive:link:visited:hover { font-size: 10pt; font-weight: bold; font-family: Arial, Verdana, Helvetica, sans-serif; color: #008000; text-decoration: underline;}\r\n" + 
 				"    \r\n" + 
 				"    \r\n" + 
 				"\r\n" + 
 				"\r\n" + 
 				"   </style>\r\n" + 
 				"");
 	}
 
 	private void moveToFront(ArrayList<String> list, String s)
 	{
 		int i = list.indexOf(s);
 		if (i != -1) {
 			list.add(0, list.remove(i));
 		}
 	}
 
 	private void moveToBack(ArrayList<String> list, String s)
 	{
 		int i = list.indexOf(s);
 		if (i != -1) {
 			list.add(list.remove(i));
 		}
 	}
 
 	private SuiteStats calculateSuiteStats(TestSuite[] suites)
 	{
 		SuiteStats stats = new SuiteStats();
 		for (TestSuite suite : suites) {
 			stats.errors += suite.errors;
 			stats.failures += suite.failures;
 			stats.time += suite.time;
 			stats.tests += suite.tests;
 		}
 		return stats;
 	}
 
 	private Hashtable<String, TestSuite[]> organizeByTestType(TestSuite[] suites)
 	{
 		Hashtable<String, ArrayList<TestSuite>> suiteTable = new Hashtable<String, ArrayList<TestSuite>>();
 		
 		for (TestSuite suite : suites) {
 			String name = suite.docTags.getProperty("testType");
 			if (name == null) name = "miscellaneous";
 			ArrayList<TestSuite> suiteList = suiteTable.get(name);
 			if (suiteList == null) suiteTable.put(name, suiteList = new ArrayList<TestSuite>());
 			suiteList.add(suite);
 		}
 		
 		Hashtable<String, TestSuite[]> realTable = new Hashtable<String, TestSuite[]>(suiteTable.size());
 		for (String name : suiteTable.keySet()) {
 			realTable.put(name, suiteTable.get(name).toArray(new TestSuite[0]));
 		}
 		return realTable;
 	}
 }
