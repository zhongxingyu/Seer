 package org.mule.tooling.jubula.xmlgenerator;
 
 import static junitx.framework.FileAssert.assertBinaryEquals;
 
 import java.io.File;
 import java.net.URL;
 
 import org.apache.maven.surefire.report.ReporterException;
 import org.junit.Before;
 import org.junit.Test;
 import org.mule.tooling.jubula.results.TestCaseResult;
 import org.mule.tooling.jubula.results.TestResultError;
 import org.mule.tooling.jubula.results.TestResultSkipped;
 import org.mule.tooling.jubula.results.TestResultSuccessful;
 import org.mule.tooling.jubula.results.TestSuiteResult;
 
 public class XMLSurefireReporterTest {
 
 	private XMLSurefireReporter reporter;
 	private TestSuiteResult testSuite;
 	private File reportFolder;
 
 	@Before
 	public void setUp() {
 		// TODO - tidy this up, send test resources to src/test/resources,
 		// acquire them through this.getClass().getClassLoader().getResource(arg0)
 		// generate results in a target folder
 		
		reportFolder = new File("target" + File.separator + "surefire-reports-test" + File.separator);
 		
 		reporter = new XMLSurefireReporter(reportFolder);
 		reporter.setPrintProperties(false);
 
 		testSuite = new TestSuiteResult("TestReporter", "Project Name", 4000L);
 
 		TestCaseResult testCase;
 
 		testCase = new TestCaseResult("TestCase Name1", 11088000L, new TestResultSuccessful());
 		testSuite.addTestCaseResult(testCase);
 
 		testCase = new TestCaseResult("TestCase Name2", 3000L, new TestResultError());
 		testSuite.addTestCaseResult(testCase);
 
 		testCase = new TestCaseResult("TestCase Name3", 3000L, new TestResultSkipped());
 		testSuite.addTestCaseResult(testCase);
 	}
 
 	@Test
 	public void testReporter() throws ReporterException {
 		testSuite.report(reporter);
 
		File actual = new File(reportFolder.getPath() + File.separator + "TEST-TestReporter.xml");
 		
		URL fileURL = this.getClass().getClassLoader().getResource("surefire-reports-test" + File.separator + "TEST-TestReporter-expected.xml");
 		
 		File expected = new File(fileURL.getPath());
 
 		assertBinaryEquals(expected, actual);
 	}
 
 }
