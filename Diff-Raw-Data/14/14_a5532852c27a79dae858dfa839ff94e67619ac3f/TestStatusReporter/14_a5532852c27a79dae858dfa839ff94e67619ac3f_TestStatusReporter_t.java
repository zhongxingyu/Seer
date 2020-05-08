 package unitTest;
 
 import static org.junit.Assert.*;
 
 import java.io.ByteArrayInputStream;
 import java.io.InputStream;
 
 import org.jmock.Expectations;
 import org.jmock.Mockery;
 import org.junit.Test;
 import org.sharon.cpputest.Reporter;
 import org.eclipse.cdt.testsrunner.model.ITestItem;
 import org.eclipse.cdt.testsrunner.model.ITestModelUpdater;
 public class TestStatusReporter {
 	
 	Mockery context = new Mockery();
 
 	@Test
 	public void testSuccessParseStream() {
 		final ITestModelUpdater testDashBoard = context.mock(ITestModelUpdater.class);
		byte[] cppUTestOutputByte = "TEST(testSuite, testCase1),... \nTEST(testSuite, testCase2)...".getBytes();
 		InputStream outputStream = new ByteArrayInputStream(cppUTestOutputByte);
 		Reporter testStatusUpdater = new Reporter(testDashBoard, outputStream);
 		context.checking(new Expectations(){
 			{
				oneOf(testDashBoard).enterTestCase("testCase1");
 				oneOf(testDashBoard).setTestStatus(ITestItem.Status.Passed);
 				oneOf(testDashBoard).exitTestCase();
				oneOf(testDashBoard).enterTestCase("testCase2");
 				oneOf(testDashBoard).setTestStatus(ITestItem.Status.Passed);
 				oneOf(testDashBoard).exitTestCase();
 			}
 		});
 		testStatusUpdater.reportTestResult();
 		context.assertIsSatisfied();
 	}
 }
