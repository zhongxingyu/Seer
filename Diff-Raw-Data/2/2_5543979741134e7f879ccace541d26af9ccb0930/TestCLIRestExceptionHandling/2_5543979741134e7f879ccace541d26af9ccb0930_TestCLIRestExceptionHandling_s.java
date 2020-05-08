 package test.cli.cloudify;
 
 import java.io.IOException;
 
 import org.testng.annotations.Test;
 
 public class TestCLIRestExceptionHandling extends AbstractLocalCloudTest {
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT, groups = "1", enabled = true)
 	public void testBadConnection() throws IOException, InterruptedException {
 		//Try to connect using a malformed URL.
 		String commandOutput = CommandTestUtils.runCommand("connect --verbose " + restUrl.substring(0, restUrl.length() - 1) + ";exit;", true, true);
		assertTrue(commandOutput.contains("Connection to http://192.168.10.122:810 refused"));
 	}
 }
