 package com.stonepeak.monkey.data;
 
import java.io.BufferedInputStream;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.UUID;
 
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpressionException;
 import javax.xml.xpath.XPathFactory;
 
 import org.w3c.dom.DOMException;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.InputSource;
 
 public class TestManager {
 	
 	private String gtestAppPath;
 	
 	private static final String GET_TEST_ARG = "--gtest_list_tests";
 	private static final String RUN_TEST_ARG = "--gtest_filter";
 	private static final String OUTPUT_XML_ARG = "--gtest_output=xml";
 	
 	/**
 	 * Constructor
 	 * @param gtestAppPath The path of the native application binary under test
 	 */
 	public TestManager(String gtestAppPath)
 	{
 		this.gtestAppPath = gtestAppPath;
 	}
 	
 	/**
 	 * Gets list of tests from gtest binary under test, and returns a list
 	 * of objects representing the available test suites and test cases
 	 * @return
 	 */
 	public List<TestSuite> getTests()
 	{
 		List<TestSuite> tests = new ArrayList<TestSuite>();
 		
 		String gtestGetTestsCmd = gtestAppPath + " " + GET_TEST_ARG;
 		
 		try {
 			String line = null;
 			Process gtestApp = Runtime.getRuntime().exec(gtestGetTestsCmd);
 			BufferedReader input = new BufferedReader(new InputStreamReader(gtestApp.getInputStream()));
 			TestSuite currentSuite = null;
 			while ((line = input.readLine()) != null) {
 				
 				// the available tests output can be one of 3 cases
 				if (line.startsWith("Running main"))
 				{
 					// this is the first line returned by the gtest app
 					continue;
 				} else if (line.matches("\\S+\\.")) {
 					// this is a new test suite name
 					if (currentSuite != null)
 					{
 						// we need to add previous suite to list
 						tests.add(currentSuite);
 					}
 					currentSuite = new TestSuite();
 					// trim the last dot from the test suite name
 					line = line.substring(0,  line.length() - 1);
 					currentSuite.setSuiteName(line);
 				} else if (line.matches("\\s{2}\\S+")) {
 					// this is a test case name
 					if (currentSuite == null)
 					{
 						throw new IllegalArgumentException("Unexpected test name detected before test suite name");
 					}
 					currentSuite.addTestCase(line.trim());
 				} else // invalid output from gtest get tests command
 					throw new IllegalArgumentException("gtest_list_tests gave unexpected output line: " + line);
 				//System.out.println(line);
 			}
 			input.close();
 			// add last test suite to list
 			if (currentSuite != null)
 			{
 				tests.add(currentSuite);
 			}
 			
 		} catch (IOException e) {
 			System.out.println("Could not run google test app: \"" + gtestAppPath + "\" - did you specify the correct file?");
 		} catch (IllegalArgumentException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		if (tests.isEmpty())
 			tests = null; // prefer returning null than an empty list
 		
 		return tests;
 	}
 	
 	/**
 	 * Runs a specific test case from the app under test
 	 * @param gtestFilter the filter used to specify which tests to run eg TestSuite.TestCase for one test.
 	 * 			separate multiple expressions by colons - see gtest --gtest_filter docs
 	 * @return GUID of test run, used to uniquely identify a set of results
 	 */
 	public String runTests(String gtestFilter)
 	{
 		String guid = UUID.randomUUID().toString();
 		StringBuilder testCommandBuilder = new StringBuilder();
 		testCommandBuilder.append(gtestAppPath).append(" ")
 			.append(RUN_TEST_ARG).append("=").append(gtestFilter).append(" ")
 			.append(OUTPUT_XML_ARG).append(":").append(guid).append(".xml");
 		//System.out.println("Running:\n" + testCommandBuilder.toString());
 		try {
 			Process gtestApp = Runtime.getRuntime().exec(testCommandBuilder.toString());
			
			// the following prevents Process.waitFor() hanging under windows
			// exhaust input stream
			BufferedInputStream in = new BufferedInputStream(gtestApp.getInputStream());
			byte[] bytes = new byte[4096];
			while (in.read(bytes) != -1) {}
						
			// exhaust error stream
			BufferedInputStream err = new BufferedInputStream(gtestApp.getErrorStream());
			while (err.read(bytes) != -1) {}
			
 			// wait for the gtest app to terminate
 			gtestApp.waitFor();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return guid;
 	}
 	
 	/**
 	 * Query the XML test results using XPath, and return a TestCaseResult list
 	 * @param testRunId
 	 * @return
 	 */
 	public List<TestCaseResult> getTestResults(String testRunId)
 	{
 		List<TestCaseResult> resultList = new ArrayList<TestCaseResult>();
 		
 		XPath xpath = XPathFactory.newInstance().newXPath();
 		String expression = "//testcase[@status='run']";	// select all testcase nodes with attribute status="run"
 		InputSource inputSource = new InputSource(testRunId + ".xml");
 		try {
 			NodeList testCaseNodes = (NodeList) xpath.evaluate(expression, inputSource, XPathConstants.NODESET);
 			
 			// next iterate over the nodes, pull out the data and create a TestCaseResult from it.
 			// involves a bit of fiddly dom traversal unfortunately
 			for (int i = 0; i < testCaseNodes.getLength(); i++)
 			{
 				Node n = testCaseNodes.item(i);
 				NamedNodeMap attribs = n.getAttributes();
 				
 				TestCaseResult testResult = new TestCaseResult();
 				testResult.setSuiteName(attribs.getNamedItem("classname").getNodeValue());
 				testResult.setCaseName(attribs.getNamedItem("name").getNodeValue());
 				testResult.setElapsedTime(Double.parseDouble(attribs.getNamedItem("time").getNodeValue()));
 				testResult.setPassed(true);
 				
 				if (n.hasChildNodes())
 				{
 					NodeList childNodes = n.getChildNodes();
 					for (int j = 0; j < childNodes.getLength(); j++)
 					{
 						if (childNodes.item(j).getNodeName().equals("failure"))
 						{
 							testResult.setPassed(false);
 							testResult.setErrorMessage(childNodes.item(j).getTextContent());
 						}
 					}
 				}
 				
 				resultList.add(testResult);
 			}
 
 		} catch (XPathExpressionException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (NumberFormatException e) {
 			// TODO implement
 			e.printStackTrace();
 		} catch (DOMException e) {
 			// TODO implement
 			e.printStackTrace();
 		} finally {
 			// clean up test result file
 			File file = new File(testRunId + ".xml");
 			file.delete();
 		}
 		
 		if (resultList.isEmpty())
 			resultList = null; // prefer to return null over an empty list
 		
 		return resultList;
 	}
 
 }
