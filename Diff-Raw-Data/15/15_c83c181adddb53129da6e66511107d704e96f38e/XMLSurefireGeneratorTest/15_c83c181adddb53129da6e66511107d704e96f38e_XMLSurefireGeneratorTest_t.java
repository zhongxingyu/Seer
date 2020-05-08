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
 
 public class XMLSurefireGeneratorTest {
 	private XMLSurefireGenerator generator;
 	private TestSuiteResult testSuite;
 	private File reportFolder;
 	
 	@Before
 	public void setUp() {
 		reportFolder = new File("target" + File.separator + "surefire-reports-test" + File.separator);
 		
 		generator = new XMLSurefireGenerator("target" + File.separator + "surefire-reports-test" + File.separator, false);
 		testSuite = new TestSuiteResult("TestGenerator", "Project Name", 4000L);
 		
 		TestCaseResult testCase;
 		
 		testCase = new TestCaseResult("TestCase Name1", 3000L, new TestResultSuccessful());
 		testSuite.addTestCaseResult(testCase);
 
 		testCase = new TestCaseResult("TestCase Name2", 3000L, new TestResultError());
 		testSuite.addTestCaseResult(testCase);
 		
 		testCase = new TestCaseResult("TestCase Name3", 3000L, new TestResultSkipped());
 		testSuite.addTestCaseResult(testCase);
 	}
 	
 	@Test
 	public void testGenerator() throws ReporterException, XMLSurefireGeneratorException {
 		generator. generateXML(testSuite);
 		
 		File actual = new File(reportFolder.getPath() + File.separator + "TEST-TestGenerator.xml");
 		
		URL fileURL = this.getClass().getClassLoader().getResource("surefire-reports-test/TEST-TestGenerator-expected.xml");
 		
 		File expected = new File(fileURL.getPath());
 
 		assertBinaryEquals(expected, actual);
 	}
 
 }
