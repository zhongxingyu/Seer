 package test.cli.cloudify;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.concurrent.TimeUnit;
 
 import junit.framework.Assert;
 
 import org.openspaces.admin.pu.ProcessingUnit;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Test;
 
 import com.gigaspaces.cloudify.dsl.internal.CloudifyConstants;
 import com.gigaspaces.cloudify.dsl.internal.ServiceReader;
 import com.gigaspaces.cloudify.dsl.internal.packaging.PackagingException;
 
 import framework.utils.LogUtils;
 
 public class CustomCommandsOnMultipleInstancesTest extends AbstractSingleBootstrapTest {
 	
 	private final String RECIPE_DIR_PATH = CommandTestUtils
 									.getPath("apps/USM/usm/simpleCustomCommandsMultipleInstances");
 	private int totalInstances;
 	
 	@Override
 	@BeforeClass
 	public void beforeClass() throws FileNotFoundException, PackagingException, IOException, InterruptedException{			
 		super.beforeClass();
 		installService();
		ProcessingUnit pu = admin.getProcessingUnits().waitFor("simpleCustomCommandsMultipleInstances" , WAIT_FOR_TIMEOUT , TimeUnit.SECONDS);
 		assertTrue("service was not installed", pu.waitFor(pu.getTotalNumberOfInstances(), WAIT_FOR_TIMEOUT, TimeUnit.SECONDS));
 		totalInstances = pu.getTotalNumberOfInstances();
 	}
 	
 	@Override
 	@AfterClass
 	public void afterClass() throws IOException, InterruptedException{	
 		runCommand("connect " + this.restUrl + 
 			";uninstall-service --verbose simpleCustomCommandsMultipleInstances");
 		super.afterClass();
 	}
 	
 //////////////////////////////////////////////////////////////////////////////////////////////////////////	
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT , groups="1", enabled = true)
 	public void testPrintCommand() throws Exception {
 		LogUtils.log("Checking print command on all instances");
 		checkPrintCommand();
 		
 		LogUtils.log("Starting to check print command by instance id");
 		for(int i=1 ; i<= totalInstances ; i++)
 			checkPrintCommand(i);
 	}
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT , groups="1", enabled = true)
 	public void testParamsCommand() throws Exception {
 		LogUtils.log("Checking params command on all instances");
 		checkParamsCommand();
 		
 		LogUtils.log("Starting to check params command by instance id");
 		for(int i=1 ; i<= totalInstances ; i++)
 			checkParamsCommand(i);		
 	}
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT , groups="1", enabled = true)
 	public void testXceptionCommand() throws Exception {
 		LogUtils.log("Checking exception command on all instances");
 		checkExceptionCommand();
 		
 		LogUtils.log("Starting to check exception command by instance id");
 		for(int i=1 ; i<= totalInstances ; i++)
 			checkExceptionCommand(i);
 	}
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT , groups="1", enabled = true)
 	public void testRunScriptCommand() throws Exception {
 		LogUtils.log("Checking runScript command on all instances");
 		checkRunScriptCommand();
 		
 		LogUtils.log("Starting to check runScript command by instance id");
 		for(int i=1 ; i<= totalInstances ; i++)
 			checkRunScriptCommand(i);
 	}
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT , groups="1", enabled = true)
 	public void testContextCommand() throws Exception {
 		LogUtils.log("Checking context command on all instances");
 		checkContextCommand();
 		
 		LogUtils.log("Starting to check context command by instance id");
 		for(int i=1 ; i<= totalInstances ; i++)
 			checkContextCommand(i);
 	}
 	
 ////////////////////////////////////////////////////////////////////////////////////////////////////////	
 	
 	private void checkPrintCommand() throws IOException, InterruptedException {
 		String invokePrintResult = runCommand("connect " + this.restUrl
 				+ "; invoke simpleCustomCommandsMultipleInstances print");
 		
 		for(int i=1 ; i <= totalInstances ; i++){
 			assertTrue("Custom command 'print' returned unexpected result from instance #" + i +": " + invokePrintResult
 					,invokePrintResult.contains("OK from instance #" + i));
 		}
 	}
 	private void checkPrintCommand(int instanceid) throws IOException, InterruptedException {
 		String invokePrintResult = runCommand("connect " + this.restUrl
 				+ "; invoke -instanceid " + -instanceid + " simpleCustomCommandsMultipleInstances print");
 		
 		assertTrue("Custom command 'print' returned unexpected result from instance #" + -instanceid +": " + invokePrintResult
 				,invokePrintResult.contains("OK from instance #" + -instanceid));
 		
 		for(int i=1 ; i <= totalInstances ; i++){
 			if(i == -instanceid)
 				continue;
 			Assert.assertFalse("should not recive any output from instance" + i ,invokePrintResult.contains("instance #" + i));
 		}
 	}
 	
 	private void checkParamsCommand() throws IOException, InterruptedException {
 		String invokeParamsResult = runCommand("connect " + this.restUrl
 				+ "; invoke simpleCustomCommandsMultipleInstances params ['x=2' 'y=3']");
 		
 		for(int i=1 ; i <= totalInstances ; i++){
 			assertTrue("Custom command 'params' returned unexpected result from instance #" + i + ": " + invokeParamsResult
 					,invokeParamsResult.contains("OK from instance #" + i) && invokeParamsResult.contains("Result: this is the custom parameters command. expecting 123: 123"));
 		}
 	}
 	
 	private void checkParamsCommand(int instanceid) throws IOException, InterruptedException {
 		String invokeParamsResult = runCommand("connect " + this.restUrl
 				+ "; invoke -instanceid " + -instanceid + " simpleCustomCommandsMultipleInstances params");
 		
 		assertTrue("Custom command 'params' returned unexpected result from instance #" + -instanceid +": " + invokeParamsResult
 				,invokeParamsResult.contains("OK from instance #" + -instanceid) && invokeParamsResult.contains("Result: this is the custom parameters command. expecting 123: 123"));
 		
 		for(int i=1 ; i <= totalInstances ; i++){
 			if(i == -instanceid)
 				continue;	
 			Assert.assertFalse("should not recive any output from instance" + i ,invokeParamsResult.contains("instance #" + i));
 		}
 	}
 	
 	private void checkExceptionCommand() throws IOException, InterruptedException {
 		String invokeExceptionResult = runCommand("connect " + this.restUrl
 				+ "; invoke simpleCustomCommandsMultipleInstances exception");
 		
 		for(int i=1 ; i <= totalInstances ; i++){
 			assertTrue("Custom command 'exception' returned unexpected result from instance #" + i +": " + invokeExceptionResult
 					,invokeExceptionResult.contains("FAILED from instance #" + i) && invokeExceptionResult.contains("This is an error test"));
 		}
 	}
 	
 	private void checkExceptionCommand(int instanceid) throws IOException, InterruptedException {
 		String invokeExceptionResult = runCommand("connect " + this.restUrl
 				+ "; invoke -instanceid " + -instanceid + " simpleCustomCommandsMultipleInstances exception");
 		
 		assertTrue("Custom command 'exception' returned unexpected result from instance #" + -instanceid +": " + invokeExceptionResult
 				,invokeExceptionResult.contains("FAILED from instance #" + -instanceid) && invokeExceptionResult.contains("This is an error test"));
 		
 		for(int i=1 ; i <= totalInstances ; i++){
 			if(i == -instanceid)
 				continue;	
 			Assert.assertFalse("should not recive any output from instance" + i ,invokeExceptionResult.contains("instance #" + i));
 		}
 	}
 	
 	private void checkRunScriptCommand() throws IOException, InterruptedException {
 		String invokeRunScriptResult = runCommand("connect " + this.restUrl
 				+ "; invoke simpleCustomCommandsMultipleInstances runScript");
 		
 		for(int i=1 ; i <= totalInstances ; i++){
 			assertTrue("Custom command 'runScript' returned unexpected result from instance #" + i +": " + invokeRunScriptResult
 					,invokeRunScriptResult.contains("OK from instance #" + i) && invokeRunScriptResult.contains("Result: 2"));
 		}
 	}
 	
 	private void checkRunScriptCommand(int instanceid) throws IOException, InterruptedException {
 		String invokeRunScriptResult = runCommand("connect " + this.restUrl
 				+ "; invoke -instanceid " + -instanceid + " simpleCustomCommandsMultipleInstances runScript");
 		
 		assertTrue("Custom command 'exception' returned unexpected result from instance #" + -instanceid +": " + invokeRunScriptResult
 				,invokeRunScriptResult.contains("OK from instance #" + -instanceid) && invokeRunScriptResult.contains("Result: 2"));
 		
 		for(int i=1 ; i <= totalInstances ; i++){
 			if(i == -instanceid)
 				continue;	
 			Assert.assertFalse("should not recive any output from instance" + i ,invokeRunScriptResult.contains("instance #" + i));
 		}
 	}
 	
 	private void checkContextCommand() throws IOException, InterruptedException {
 		String invokeContextResult = runCommand("connect " + this.restUrl
 				+ "; invoke simpleCustomCommandsMultipleInstances context");
 		
 		for(int i=1 ; i <= totalInstances ; i++){
 			assertTrue("Custom command 'context' returned unexpected result from instance #" + i +": " + invokeContextResult
 					,invokeContextResult.contains("OK from instance #" + i) && invokeContextResult.contains("Service Dir is:"));
 		}
 	}
 	
 	private void checkContextCommand(int instanceid) throws IOException, InterruptedException {
 		String invokeContextResult = runCommand("connect " + this.restUrl
 				+ "; invoke -instanceid " + -instanceid + " simpleCustomCommandsMultipleInstances context");
 		
 		assertTrue("Custom command 'context' returned unexpected result from instance #" + -instanceid +": " + invokeContextResult
 				,invokeContextResult.contains("OK from instance #" + -instanceid) && invokeContextResult.contains("Service Dir is:"));
 		
 		for(int i=1 ; i <= totalInstances ; i++){
 			if(i == -instanceid)
 				continue;	
 			Assert.assertFalse("should not recive any output from instance" + i ,invokeContextResult.contains("instance #" + i));
 		}
 	}
 	private void installService() throws FileNotFoundException,
 		PackagingException, IOException, InterruptedException {
 		File serviceDir = new File(RECIPE_DIR_PATH);
 		ServiceReader.getServiceFromDirectory(serviceDir, CloudifyConstants.DEFAULT_APPLICATION_NAME).getService();
 		
 		runCommand("connect " + this.restUrl + ";install-service --verbose " + RECIPE_DIR_PATH);
 	}
 }
