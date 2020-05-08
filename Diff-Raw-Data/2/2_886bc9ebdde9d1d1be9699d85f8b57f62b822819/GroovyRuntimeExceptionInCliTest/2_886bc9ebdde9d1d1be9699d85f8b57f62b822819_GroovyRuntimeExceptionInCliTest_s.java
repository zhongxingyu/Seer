 package test.cli.cloudify;
 
 import java.io.IOException;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.testng.annotations.Test;
 /**
  * Test whether the CLI prints runtime exception details that occur in the groovy script.
  * A Cassandra service that throws an exception in the Cassandra-install.groovy file is used.
  * @author adaml
  *
  */
 public class GroovyRuntimeExceptionInCliTest extends AbstractLocalCloudTest {
	private static final String BAD_SERVICE_PATH = CommandTestUtils.getPath("apps/USM/badUsmServices/cassandra");
 	private static final String EXCEPTION_CAUGHT_REGEX = "Caught:.*\\s*?.*\\.groovy:[1-9]{1,}\\)";
 	
 	/**
 	 * Install a service and assert CLI exception details message. 
 	 * @throws IOException
 	 * @throws InterruptedException
 	 */
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT, groups = "1", enabled = true)
 	public void testGroovyRuntimeExceptionHandling() throws IOException, InterruptedException {
 		String commandOutput = CommandTestUtils.runCommandExpectedFail("connect " + this.restUrl 
 															+ ";install-service " + BAD_SERVICE_PATH 
 															+ ";exit");
 		
 		//See that the runtime exception print pattern exists in the CLI output.
 		Pattern pattern = Pattern.compile(EXCEPTION_CAUGHT_REGEX, Pattern.MULTILINE);
 		Matcher matcher = pattern.matcher(commandOutput);
 		int beginIndex = 0;
 		int endIndex = 0;
 		if (matcher.find()){
 			beginIndex = matcher.start(0);
 			endIndex = matcher.end(0);
 		}
 		//does the expression exist?
 		assertTrue("Runtime exception was not printed by the CLI.", endIndex != 0 && beginIndex != 0 );
 		
 	}
 }
