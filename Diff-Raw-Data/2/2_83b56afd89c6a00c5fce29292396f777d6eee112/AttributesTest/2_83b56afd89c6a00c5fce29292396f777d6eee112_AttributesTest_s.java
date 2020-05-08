 package test.cli.cloudify.recipes.attributes;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.concurrent.TimeUnit;
 
 import framework.utils.DumpUtils;
 import framework.utils.TeardownUtils;
 import org.openspaces.admin.pu.ProcessingUnit;
 import org.openspaces.core.GigaSpace;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.AfterMethod;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Test;
 
 import test.cli.cloudify.AbstractLocalCloudTest;
 import test.cli.cloudify.CommandTestUtils;
 import test.usm.USMTestUtils;
 
 import com.gigaspaces.cloudify.dsl.Application;
 import com.gigaspaces.cloudify.dsl.Service;
 import com.gigaspaces.cloudify.dsl.internal.DSLException;
 import com.gigaspaces.cloudify.dsl.internal.ServiceReader;
 import com.gigaspaces.cloudify.dsl.internal.packaging.PackagingException;
 import com.gigaspaces.cloudify.dsl.utils.ServiceUtils;
 
 import framework.utils.LogUtils;
 
 public class AttributesTest extends AbstractLocalCloudTest {
 	private final String appName = "serviceContextProperties";
 	private final String APPLICAION_DIR_PATH = CommandTestUtils
 									.getPath("apps/USM/usm/applications/serviceContextProperties");
 	
 	private Application app;
 	private Service getter;
 	private Service setter;	
 	private GigaSpace gigaspace;
 
 	@BeforeClass
 	public void beforeClass() throws Exception{
         super.beforeClass();
 		gigaspace = admin.getSpaces().waitFor("cloudifyManagementSpace", 20, TimeUnit.SECONDS).getGigaSpace();
 		installApplication();
 		String absolutePUNameSimple1 = ServiceUtils.getAbsolutePUName("serviceContextProperties", "getter");
 		String absolutePUNameSimple2 = ServiceUtils.getAbsolutePUName("serviceContextProperties", "setter");
 		ProcessingUnit pu1 = admin.getProcessingUnits().waitFor(absolutePUNameSimple1 , WAIT_FOR_TIMEOUT , TimeUnit.SECONDS);
 		ProcessingUnit pu2 = admin.getProcessingUnits().waitFor(absolutePUNameSimple2, WAIT_FOR_TIMEOUT , TimeUnit.SECONDS);
 		assertNotNull(pu1);
 		assertNotNull(pu2);
 		assertTrue("applications was not installed", pu1.waitFor(pu1.getTotalNumberOfInstances(), WAIT_FOR_TIMEOUT, TimeUnit.SECONDS));
 		assertTrue("applications was not installed", pu2.waitFor(pu2.getTotalNumberOfInstances(), WAIT_FOR_TIMEOUT, TimeUnit.SECONDS));
 		assertNotNull("applications was not installed", admin.getApplications().getApplication("serviceContextProperties"));
 		assertTrue("USM Service State is NOT RUNNING", USMTestUtils.waitForPuRunningState(absolutePUNameSimple1, 60, TimeUnit.SECONDS, admin));
 		assertTrue("USM Service State is NOT RUNNING", USMTestUtils.waitForPuRunningState(absolutePUNameSimple2, 60, TimeUnit.SECONDS, admin));
 		
 	}
 
 	@Override
 	@AfterMethod
 	public void afterTest(){
 		gigaspace.clear(null);
         if (admin != null) {
             TeardownUtils.snapshot(admin);
             DumpUtils.dumpLogs(admin);
         }
 	}
 
     @AfterClass
     public void afterClass() throws IOException, InterruptedException{
         runCommand("connect " + restUrl + ";uninstall-application --verbose serviceContextProperties");
     }
 		
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT , groups="1", enabled = true)
 	public void testSimpleApplicationSetContext() throws Exception {
 		LogUtils.log("setting an application attribute from setter service");
 		runCommand("connect " + restUrl + ";use-application serviceContextProperties" 
 				+ "; invoke setter setApp");
 
 		String simpleGet = runCommand("connect " + restUrl + ";use-application serviceContextProperties" 
 				+ "; invoke getter getApp");
 		
 		String simpleGet2 = runCommand("connect " + restUrl + ";use-application serviceContextProperties" 
 				+ "; invoke setter getApp");
 		
 		String simpleGet3 = runCommand("connect " + restUrl + ";use-application serviceContextProperties" 
 				+ "; invoke setter getService");
 		
 		assertTrue("command did not execute" , simpleGet.contains("OK"));
 		assertTrue("command did not execute" , simpleGet2.contains("OK"));
 		assertTrue("command did not execute" , simpleGet3.contains("OK"));
 		assertTrue("getter service cannot get the application attribute", simpleGet.contains("myValue"));
 		assertTrue("setter service cannot get the application attribute", simpleGet2.contains("myValue"));
 		assertTrue("setter service shouldn't be able to get the application attribute using getService", 
 				   simpleGet3.contains("null"));
 	}
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT , groups="1", enabled = true)
 	public void testApplicationSetContextCustomParams() throws Exception {
 		
 		runCommand("connect " + restUrl + ";use-application serviceContextProperties" 
 				+ "; invoke setter setAppCustom myKey1 myValue1");
 		
 		String simpleGet = runCommand("connect " + restUrl + ";use-application serviceContextProperties" 
 				+ "; invoke getter getAppCustom myKey1");
 		
 		assertTrue("command did not execute" , simpleGet.contains("OK"));
 		assertTrue("getter service cannot get the application attribute when using parameters", simpleGet.contains("myValue1"));
 	}
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT , groups="1", enabled = true)
 	public void testServiceSetContext() throws Exception {
 		
 		runCommand("connect " + restUrl + ";use-application serviceContextProperties" 
 				+ "; invoke setter setService");
 		
 		String crossServiceGet = runCommand("connect " + restUrl + ";use-application serviceContextProperties" 
 				+ "; invoke getter getService");
 		
 		String serviceGet = runCommand("connect " + restUrl + ";use-application serviceContextProperties" 
 				+ "; invoke setter getService");
 		
 		String appGet = runCommand("connect " + restUrl + ";use-application serviceContextProperties" 
 				+ "; invoke getter getApp");
 		
 		String getInstance1 = runCommand("connect " + restUrl + ";use-application serviceContextProperties" 
 				+ "; invoke -instanceid 1 setter getService");
 		String getInstance2 = runCommand("connect " + restUrl + ";use-application serviceContextProperties" 
 				+ "; invoke -instanceid 2 setter getService");
 		
 		String instanceGetApp = runCommand("connect " + restUrl + ";use-application serviceContextProperties" 
 				+ "; invoke -instanceid 1 setter getApp");
 		
 		assertTrue("command did not execute" , crossServiceGet.contains("OK"));
 		assertTrue("command did not execute" , serviceGet.contains("OK"));
 		assertTrue("command did not execute" , appGet.contains("OK"));
 		assertTrue("command did not execute" , getInstance1.contains("OK"));
 		assertTrue("command did not execute" , getInstance2.contains("OK"));
 		assertTrue("command did not execute" , instanceGetApp.contains("OK"));
 		assertTrue("setService should be visible to the same service", serviceGet.contains("myValue"));
 		assertTrue("setService should be visible to all service instances", getInstance1.contains("myValue") && getInstance2.contains("myValue"));
 		assertTrue("setService should not be visible to a different service", crossServiceGet.contains("null"));
 		assertTrue("getApp should be able to get a service attribute", appGet.contains("null"));
 		assertTrue("getApp should be able to get a service attribute", instanceGetApp.contains("null"));
 	}
 
     @Test(timeOut = DEFAULT_TEST_TIMEOUT , groups="1", enabled = true)
     public void testServiceSetContextByName() throws Exception {
 
         String setServiceByName = runCommand("connect " + restUrl + ";use-application serviceContextProperties"
                 + "; invoke setter setServiceByName");
 
         String getServiceByName = runCommand("connect " + restUrl + ";use-application serviceContextProperties"
                 + "; invoke setter getServiceByName");
 
         assertTrue("command did not execute" , setServiceByName.contains("OK"));
         assertTrue("command did not execute" , getServiceByName.contains("OK"));
 
         assertTrue(getServiceByName.contains("myValue"));
     }
 
     @Test(timeOut = DEFAULT_TEST_TIMEOUT , groups="1", enabled = true)
     public void testServiceInstanceSetContext() throws Exception {
 
         runCommand("connect " + restUrl + ";use-application serviceContextProperties"
                 + "; invoke -instanceid 1 setter setInstanceCustom1 myKey1 myValue1");
 
         runCommand("connect " + restUrl + ";use-application serviceContextProperties"
                 + "; invoke -instanceid 2 setter setInstanceCustom2 myKey1 myValue2");
 
         String getInstance1 = runCommand("connect " + restUrl + ";use-application serviceContextProperties"
                 + "; invoke -instanceid 1 setter getInstanceCustom1 myKey1");
 
         String getInstance2 = runCommand("connect " + restUrl + ";use-application serviceContextProperties"
                 + "; invoke -instanceid 2 setter getInstanceCustom2 myKey1");
 
         String getService = runCommand("connect " + restUrl + ";use-application serviceContextProperties"
                 + "; invoke setter getAppCustom myKey1");
 
         String getApp = runCommand("connect " + restUrl + ";use-application serviceContextProperties"
                 + "; invoke  setter getAppCustom myKey1");
 
         assertTrue("command did not execute" , getInstance1.contains("OK"));
         assertTrue("command did not execute" , getInstance2.contains("OK"));
         assertTrue("command did not execute" , getService.contains("OK"));
         assertTrue("command did not execute" , getApp.contains("OK"));
 
         assertTrue("getAppCustom should be return myValue1 when instanceId=1", getInstance1.contains("myValue1"));
         assertTrue("getAppCustom should be return myValue2 when instanceId=2", getInstance2.contains("myValue2"));
         assertTrue("getApp should be able to get a service attribute", getApp.contains("null"));
         assertTrue("getService should be able to get a service attribute", getService.contains("null"));
     }
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT , groups="1", enabled = true)
 	public void testSetCustomPojo() throws Exception {
 		LogUtils.log("setting a custom pojo on service level");
 		runCommand("connect " + restUrl + ";use-application serviceContextProperties" 
 				+ "; invoke setter setAppCustomPojo");
 
 		String getCustomPojo = runCommand("connect " + restUrl + ";use-application serviceContextProperties" 
 				+ "; invoke getter getAppCustomPojo");
 				
 		assertTrue("command did not execute" , getCustomPojo.contains("OK"));
 		assertTrue("getter service cannot get the data pojo", getCustomPojo.contains("data"));
 	}
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT , groups="1", enabled = false)
 	public void testSetDouble() throws Exception {
 		LogUtils.log("setting a double on service level");
 		runCommand("connect " + restUrl + ";use-application serviceContextProperties" 
 				+ "; invoke setter setServiceDouble");
 
 		String getDouble = runCommand("connect " + restUrl + ";use-application serviceContextProperties" 
 				+ "; invoke getter getServiceDouble");
 				
 		assertTrue("command did not execute" , getDouble.contains("OK"));
 		assertTrue("getter service cannot get the double", !getDouble.contains("null"));
 	}
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT , groups="1", enabled = true)
 	public void testInstanceIteration() throws Exception {
 		runCommand("connect " + restUrl + ";use-application serviceContextProperties" 
 				+ "; invoke -instanceid 1 setter setInstance1; invoke -instanceid 2 setter setInstance2");
 		
 		String iterateInstances = runCommand("connect " + restUrl + ";use-application serviceContextProperties" 
 				+ "; invoke -instanceid 1 setter iterateInstances");
 		
 		assertTrue("command did not execute" , iterateInstances.contains("OK"));
 		assertTrue("iteratoring over instances", iterateInstances.contains("myValue1") && iterateInstances.contains("myValue2"));
 	}
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT , groups="1", enabled = true )
 	public void testRemoveThisService() throws Exception {
 		runCommand("connect " + restUrl + ";use-application serviceContextProperties" 
 				+ "; invoke setter setService; invoke setter setService2");
 		String getOutputAfterSet = runCommand("connect " + restUrl + ";use-application serviceContextProperties" 
 				+ "; invoke setter getService; invoke setter getService2");
 		assertTrue("set command did not execute" , getOutputAfterSet.contains("myValue") && getOutputAfterSet.contains("myValue2"));
 		
 		runCommand("connect " + restUrl + ";use-application serviceContextProperties" 
 				+ "; invoke setter removeService");
 		String getOutputAfterRemove = runCommand("connect " + restUrl + ";use-application serviceContextProperties" 
 				+ "; invoke setter getService ;invoke setter getService2");
 		assertTrue("get myKey command should return null after myKey was removed" , getOutputAfterRemove.contains("null"));
 		assertTrue("myKey2 should not be affected by remove myKey" , getOutputAfterRemove.contains("myValue2"));
 	}
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT , groups="1", enabled = true)
 	public void testRemoveInstanceByServiceName() throws Exception {
 		runCommand("connect " + restUrl + ";use-application serviceContextProperties" 
 				+ "; invoke getter setService"); 
 		String getOutputAfterSet = runCommand("connect " + restUrl + ";use-application serviceContextProperties" 
 				+ "; invoke getter getService");
 		assertTrue("set command did not execute" , getOutputAfterSet.contains("myValue"));
 		
 		runCommand("connect " + restUrl + ";use-application serviceContextProperties" 
 				+ "; invoke setter removeInstanceByServiceName");
 //		String getServiceOutputAfterRemove = runCommand("connect " + restUrl + ";use-application serviceContextProperties" 
 //				+ "; invoke getter getService");
 //		assertTrue("getServie command should return null after key was removed" , getServiceOutputAfterRemove.toLowerCase().contains("null"));
 		
 		String getInstanceOutputAfterRemove = runCommand("connect " + restUrl + ";use-application serviceContextProperties" 
 				+ "; invoke -instanceid 1 getter getInstance");
 		assertTrue("getInstance command should return null after key was removed" , getInstanceOutputAfterRemove.toLowerCase().contains("null"));
 		
 	}
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT , groups="1", enabled = true)
 	public void testCleanInstanceAfterSetService() throws Exception {
 		runCommand("connect " + restUrl + ";use-application serviceContextProperties" 
 				+ "; invoke setter setInstance1");
 		runCommand("connect " + restUrl + ";use-application serviceContextProperties" 
 				+ "; invoke setter setInstance2");
 		String getInstanceBeforeRemove = runCommand("connect " + restUrl + ";use-application serviceContextProperties" 
 				+ "; invoke -instanceid 1 setter getInstance");
 		assertTrue("set command did not execute" , getInstanceBeforeRemove.contains("myValue1"));
 		getInstanceBeforeRemove = runCommand("connect " + restUrl + ";use-application serviceContextProperties" 
 				+ "; invoke -instanceid 2 setter getInstance");
 		assertTrue("set command did not execute" , getInstanceBeforeRemove.contains("myValue2"));
 		
 		runCommand("connect " + restUrl + ";use-application serviceContextProperties" 
 				+ "; invoke -instanceid 1 setter cleanThisInstance");
 		String getInstanceAfterRemove = runCommand("connect " + restUrl + ";use-application serviceContextProperties" 
 				+ "; invoke -instanceid 1 setter getInstance");
 		assertTrue("get command should return null after key was removed" , getInstanceAfterRemove.toLowerCase().contains("null"));
 		getInstanceAfterRemove = runCommand("connect " + restUrl + ";use-application serviceContextProperties" 
 				+ "; invoke -instanceid 2 setter getInstance");
 		assertTrue("clear on instance 1 should not affect instance 2" , getInstanceAfterRemove.contains("myValue2"));
 	}
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT , groups="1", enabled = true)
 	public void testCleanAppAfterSetService() throws Exception {
 		runCommand("connect " + restUrl + ";use-application serviceContextProperties" 
 				+ "; invoke setter setService; invoke setter setService2");
 		String getServiceBeforeClear = runCommand("connect " + restUrl + ";use-application serviceContextProperties" 
 				+ "; invoke setter getService; invoke setter getService2");
 		assertTrue("set command did not execute" , getServiceBeforeClear.contains("myValue") && getServiceBeforeClear.contains("myValue2"));
 				
 		runCommand("connect " + restUrl + ";use-application serviceContextProperties" 
 				+ "; invoke getter cleanThisApp");
 		String getServiceAfterClear = runCommand("connect " + restUrl + ";use-application serviceContextProperties" 
 				+ "; invoke setter setService ; invoke setter setService2");
 		assertTrue("clear app should not affect service attributes" , 
				getServiceAfterClear.contains("myValue1") && getServiceAfterClear.contains("myValue2"));
 	}
 	
 	private void installApplication() throws PackagingException, IOException, InterruptedException, DSLException {
 		File applicationDir = new File(APPLICAION_DIR_PATH);
 		app = ServiceReader.getApplicationFromFile(applicationDir).getApplication();
 		getter = app.getServices().get(0).getName().equals("getter") ? app.getServices().get(0) : app.getServices().get(1);
 		setter = app.getServices().get(0).getName().equals("setter") ? app.getServices().get(0) : app.getServices().get(1);
 		
 		runCommand("connect " + restUrl + ";install-application --verbose " + APPLICAION_DIR_PATH);
 	}
 }
