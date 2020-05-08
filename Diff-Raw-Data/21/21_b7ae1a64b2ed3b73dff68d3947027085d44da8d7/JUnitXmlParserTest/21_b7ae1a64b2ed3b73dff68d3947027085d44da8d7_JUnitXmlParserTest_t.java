 /*
  * Created on Jul 19, 2006
  */
 package edu.duke.cabig.catrip.test.report;
 
 import java.io.File;
 import java.io.IOException;
 
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.xml.sax.SAXException;
 
 import edu.duke.cabig.catrip.test.report.data.TestSuite;
 
 import junit.textui.TestRunner;
 
 public class JUnitXmlParserTest
 	extends junit.framework.TestCase
 {
 	public JUnitXmlParserTest(String name)
 	{
 		super(name);
 	}
 
 	public void testJUnit() 
 		throws SAXException, IOException, ParserConfigurationException
 	{
 		JUnitXmlParser parser = new JUnitXmlParser();
 		parser.parse(new File(System.getProperty("junit.report", 
 			"test" + File.separator + "resources" + File.separator + "TEST-edu.duke.cabig.catrip.test.report.JUnitDocletTest.xml"
 		)));
 		
 		TestSuite[] suites = parser.getTestSuites();
 		assertEquals(1, suites.length);
 		// suite 1
 		assertEquals(0, suites[0].failures);
		assertEquals(0, suites[0].errors);
 		assertEquals("edu.duke.cabig.catrip.test.report.JUnitDocletTest", suites[0].name);
 		assertEquals(1, suites[0].tests);
		assertEquals(2.977, suites[0].time);
 		assertEquals("C:\\caBIG\\catrip\\codebase\\share\\cobertura-1.8", suites[0].props.get("cobertura.home"));
 		assertEquals(1, suites[0].testCases.size());
 		// suite 1, test 1
 		assertEquals("edu.duke.cabig.catrip.test.report.JUnitDocletTest", suites[0].testCases.get(0).className);
 		assertEquals("testDoclet", suites[0].testCases.get(0).name);
		assertEquals(2.727, suites[0].testCases.get(0).time);
		assertNull(suites[0].testCases.get(0).failure);
 	}
 
 	public void testJUnitReport() 
 		throws SAXException, IOException, ParserConfigurationException
 	{
 		JUnitXmlParser parser = new JUnitXmlParser();
 		parser.parse(new File(System.getProperty("junit.report", 
 			"test" + File.separator + "resources" + File.separator + "TESTS-TestSuites.xml"
 		)));
 		
 		TestSuite[] suites = parser.getTestSuites();
 		assertEquals(2, suites.length);
 		// suite 1
 		assertEquals(1, suites[0].failures);
 		assertEquals(0, suites[0].errors);
 		assertEquals("edu.duke.cabig.catrip.security.dukeidp.NTAuthenticationTest", suites[0].name);
 		assertEquals(2, suites[0].tests);
 		assertEquals(23.224, suites[0].time);
 		assertEquals("HotSpot Client Compiler", suites[0].props.get("sun.management.compiler"));
 		assertEquals(2, suites[0].testCases.size());
 		// suite 1, test 1
 		assertEquals("edu.duke.cabig.catrip.security.dukeidp.NTAuthenticationTest", suites[0].testCases.get(0).className);
 		assertEquals("testInvalidCredentials", suites[0].testCases.get(0).name);
 		assertEquals(21.922, suites[0].testCases.get(0).time);
 		assertNull(suites[0].testCases.get(0).failure);
 		// suite 1, test 2
 		assertEquals("edu.duke.cabig.catrip.security.dukeidp.NTAuthenticationTest", suites[0].testCases.get(0).className);
 		assertEquals("testValidCredentials", suites[0].testCases.get(1).name);
 		assertEquals(1.052, suites[0].testCases.get(1).time);
 		assertNotNull(suites[0].testCases.get(1).failure);
 		assertFalse(suites[0].testCases.get(1).failure.isError);
 		assertEquals("junit.framework.AssertionFailedError", suites[0].testCases.get(1).failure.type);
 		assertEquals("junit.framework.AssertionFailedError\n" + 
 				"	at edu.duke.cabig.catrip.security.dukeidp.NTAuthenticationTest.testValidCredentials(NTAuthenticationTest.java:77)\n" + 
 				"	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)\n" + 
 				"	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:39)\n" + 
 				"	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)\n" + 
 				"", suites[0].testCases.get(1).failure.stackTrace
 		);
 		// suite 2
 		assertEquals(0, suites[1].failures);
 		assertEquals(0, suites[1].errors);
 		assertEquals("edu.duke.cabig.catrip.security.password.SecurePasswordTest", suites[1].name);
 		assertEquals(3, suites[1].tests);
 		assertEquals(2.854, suites[1].time);
 		assertEquals("C:\\caBIG\\catrip\\codebase\\projects\\security-password/lib", suites[1].props.get("lib.dir"));
 		assertEquals(3, suites[1].testCases.size());
 		// suite 2, test 1
 		assertEquals("edu.duke.cabig.catrip.security.password.SecurePasswordTest", suites[1].testCases.get(0).className);
 		assertEquals("testGenerateTempKeys", suites[1].testCases.get(0).name);
 		assertEquals(0.931, suites[1].testCases.get(0).time);
 		assertNull(suites[1].testCases.get(0).failure);
 		// suite 2, test 2
 		assertEquals("edu.duke.cabig.catrip.security.password.SecurePasswordTest", suites[1].testCases.get(1).className);
 		assertEquals("testSecurePasswordGenerated", suites[1].testCases.get(1).name);
 		assertEquals(1.642, suites[1].testCases.get(1).time);
 		assertNull(suites[1].testCases.get(1).failure);
 		// suite 2, test 3
 		assertEquals("edu.duke.cabig.catrip.security.password.SecurePasswordTest", suites[1].testCases.get(2).className);
 		assertEquals("testSecurePassword", suites[1].testCases.get(2).name);
 		assertEquals(0.02, suites[1].testCases.get(2).time);
 		assertNull(suites[1].testCases.get(2).failure);
 	}
 	
 	/**
 	 * Run the tests
 	 * @param args ignored
 	 */
 	public static void main(String[] args)
 	{
 		TestRunner runner = new TestRunner();
 		junit.framework.TestResult result = runner.doRun(new junit.framework.TestSuite(JUnitXmlParserTest.class));
 		System.exit(result.errorCount() + result.failureCount());
 	}
 
 }
