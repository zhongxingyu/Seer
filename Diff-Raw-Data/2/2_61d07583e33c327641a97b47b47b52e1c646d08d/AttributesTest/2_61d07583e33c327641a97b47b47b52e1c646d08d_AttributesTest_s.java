 package test.cli.cloudify.recipes.attributes;
 
 import java.io.IOException;
 import java.util.concurrent.TimeUnit;
 
 import org.cloudifysource.dsl.utils.ServiceUtils;
 import org.openspaces.admin.pu.ProcessingUnit;
 import org.openspaces.core.GigaSpace;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 import test.cli.cloudify.AbstractLocalCloudTest;
 import test.usm.USMTestUtils;
 import framework.utils.LogUtils;
 
 public class AttributesTest extends AbstractLocalCloudTest {
 
 	private static final String MAIN_APPLICATION_NAME = "attributesTestApp";
 	private static final String SECONDARY_APPLICATION_NAME = "attributesTestApp2";
 	
 	private GigaSpace gigaspace;
 
 	@BeforeMethod
 	@Override
 	public void beforeTest() {
         super.beforeTest();
 		gigaspace = admin.getSpaces().waitFor("cloudifyManagementSpace", 20, TimeUnit.SECONDS).getGigaSpace();
 	}
 		
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT , groups="1", enabled = true)
 	public void testSimpleApplicationSetAttribute() throws Exception {
 		installApplication();
 		LogUtils.log("setting an application attribute from setter service");
 		runCommand("connect " + restUrl + ";use-application attributesTestApp" 
 				+ "; invoke setter setApp");
 
 		String simpleGet = runCommand("connect " + restUrl + ";use-application attributesTestApp" 
 				+ "; invoke getter getApp");
 		
 		String simpleGet2 = runCommand("connect " + restUrl + ";use-application attributesTestApp" 
 				+ "; invoke setter getApp");
 		
 		String simpleGet3 = runCommand("connect " + restUrl + ";use-application attributesTestApp" 
 				+ "; invoke setter getService");
 		
 		assertTrue("command did not execute" , simpleGet.contains("OK"));
 		assertTrue("command did not execute" , simpleGet2.contains("OK"));
 		assertTrue("command did not execute" , simpleGet3.contains("OK"));
 		assertTrue("getter service cannot get the application attribute", simpleGet.contains("myValue"));
 		assertTrue("setter service cannot get the application attribute", simpleGet2.contains("myValue"));
 		assertTrue("setter service shouldn't be able to get the application attribute using getService", 
 				   simpleGet3.contains("null"));
 		uninstallApplication();
 	}
 
 	private void cleanAttributes() throws IOException, InterruptedException {
 		gigaspace.clear(null);
 	}
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT , groups="1", enabled = true)
 	public void testOverrideInstanceAttribute() throws Exception {
 		
		assertEquals("wrong number of objects in space", 1, gigaspace.count(null)); //CloudConfigurationHolder
 		installApplication();//installApplication clears all object in the space. 
 		
 		runCommand("connect " + restUrl + ";use-application " + MAIN_APPLICATION_NAME 
 				+ "; invoke -instanceid 1 setter setInstanceCustom myKey1 myValue1");
 		
 		String getBeforeOverride = runCommand("connect " + restUrl + ";use-application attributesTestApp" 
 				+ "; invoke -instanceid 1 setter getInstanceCustom myKey1");
 		assertEquals("wrong number of objects in space", 1, gigaspace.count(null));
 		assertTrue("command did not execute" , getBeforeOverride.contains("OK"));
 		assertTrue("service cannot get the instance attribute", getBeforeOverride.contains("myValue1"));
 		
 		runCommand("connect " + restUrl + ";use-application attributesTestApp" 
 				+ "; invoke -instanceid 1 setter setInstanceCustom myKey1 myValue2");
 		String getAfterOverride = runCommand("connect " + restUrl + ";use-application attributesTestApp" 
 				+ "; invoke -instanceid 1 setter getInstanceCustom myKey1");
 		assertEquals("wrong number of objects in space", 1, gigaspace.count(null));
 		assertTrue("command did not execute" , getAfterOverride.contains("OK"));
 		assertTrue("instance attribute was not overriden properly", getAfterOverride.contains("myValue2") && 
 																   !getAfterOverride.contains("myValue1"));
 		uninstallApplication();
 	}
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT , groups="1", enabled = true)
 	public void testApplicationSetAttributeCustomParams() throws Exception {
 		installApplication();
 		runCommand("connect " + restUrl + ";use-application attributesTestApp" 
 				+ "; invoke setter setAppCustom myKey1 myValue1");
 		
 		String simpleGet = runCommand("connect " + restUrl + ";use-application attributesTestApp" 
 				+ "; invoke getter getAppCustom myKey1");
 		
 		assertTrue("command did not execute" , simpleGet.contains("OK"));
 		assertTrue("getter service cannot get the application attribute when using parameters", simpleGet.contains("myValue1"));
 		uninstallApplication();
 	}
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT , groups="1", enabled = true)
 	public void testServiceSetAttribute() throws Exception {
 		
 		installApplication();
 		runCommand("connect " + restUrl + ";use-application attributesTestApp" 
 				+ "; invoke setter setService");
 		
 		String crossServiceGet = runCommand("connect " + restUrl + ";use-application attributesTestApp" 
 				+ "; invoke getter getService");
 		
 		String serviceGet = runCommand("connect " + restUrl + ";use-application attributesTestApp" 
 				+ "; invoke setter getService");
 		
 		String appGet = runCommand("connect " + restUrl + ";use-application attributesTestApp" 
 				+ "; invoke getter getApp");
 		
 		String getInstance1 = runCommand("connect " + restUrl + ";use-application attributesTestApp" 
 				+ "; invoke -instanceid 1 setter getService");
 		String getInstance2 = runCommand("connect " + restUrl + ";use-application attributesTestApp" 
 				+ "; invoke -instanceid 2 setter getService");
 		
 		String instanceGetApp = runCommand("connect " + restUrl + ";use-application attributesTestApp" 
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
 		
 		uninstallApplication();
 	}
 
     @Test(timeOut = DEFAULT_TEST_TIMEOUT , groups="1", enabled = true)
     public void testServiceSetAttributeByName() throws Exception {
 
     	installApplication();
         String setServiceByName = runCommand("connect " + restUrl + ";use-application attributesTestApp"
                 + "; invoke setter setServiceByName");
 
         String getServiceByName = runCommand("connect " + restUrl + ";use-application attributesTestApp"
                 + "; invoke setter getServiceByName");
 
         assertTrue("command did not execute" , setServiceByName.contains("OK"));
         assertTrue("command did not execute" , getServiceByName.contains("OK"));
 
         assertTrue(getServiceByName.contains("myValue"));
         uninstallApplication();
     }
 
     @Test(timeOut = DEFAULT_TEST_TIMEOUT , groups="1", enabled = true)
     public void testServiceInstanceSetAttribute() throws Exception {
 
     	installApplication();
         runCommand("connect " + restUrl + ";use-application attributesTestApp"
                 + "; invoke -instanceid 1 setter setInstanceCustom1 myKey1 myValue1");
 
         runCommand("connect " + restUrl + ";use-application attributesTestApp"
                 + "; invoke -instanceid 2 setter setInstanceCustom2 myKey1 myValue2");
 
         String getInstance1 = runCommand("connect " + restUrl + ";use-application attributesTestApp"
                 + "; invoke -instanceid 1 setter getInstanceCustom1 myKey1");
 
         String getInstance2 = runCommand("connect " + restUrl + ";use-application attributesTestApp"
                 + "; invoke -instanceid 2 setter getInstanceCustom2 myKey1");
 
         String getService = runCommand("connect " + restUrl + ";use-application attributesTestApp"
                 + "; invoke setter getAppCustom myKey1");
 
         String getApp = runCommand("connect " + restUrl + ";use-application attributesTestApp"
                 + "; invoke  setter getAppCustom myKey1");
 
         assertTrue("command did not execute" , getInstance1.contains("OK"));
         assertTrue("command did not execute" , getInstance2.contains("OK"));
         assertTrue("command did not execute" , getService.contains("OK"));
         assertTrue("command did not execute" , getApp.contains("OK"));
 
         assertTrue("getAppCustom should be return myValue1 when instanceId=1", getInstance1.contains("myValue1"));
         assertTrue("getAppCustom should be return myValue2 when instanceId=2", getInstance2.contains("myValue2"));
         assertTrue("getApp should be able to get a service attribute", getApp.contains("null"));
         assertTrue("getService should be able to get a service attribute", getService.contains("null"));
         uninstallApplication();
     }
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT , groups="1", enabled = true)
 	public void testSetCustomPojo() throws Exception {
 		installApplication();
 		LogUtils.log("setting a custom pojo on service level");
 		runCommand("connect " + restUrl + ";use-application attributesTestApp" 
 				+ "; invoke setter setAppCustomPojo");
 
 		String getCustomPojo = runCommand("connect " + restUrl + ";use-application attributesTestApp" 
 				+ "; invoke getter getAppCustomPojo");
 				
 		assertTrue("command did not execute" , getCustomPojo.contains("OK"));
 		assertTrue("getter service cannot get the data pojo", getCustomPojo.contains("data"));
 		uninstallApplication();
 	}
 	
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT , groups="1", enabled = true)
 	public void testInstanceIteration() throws Exception {
 		installApplication();
 		runCommand("connect " + restUrl + ";use-application attributesTestApp" 
 				+ "; invoke -instanceid 1 setter setInstanceCustom myKey myValue1;" +
 				"invoke -instanceid 2 setter setInstanceCustom myKey myValue2");
 		
 		String iterateInstances = runCommand("connect " + restUrl + ";use-application attributesTestApp" 
 				+ "; invoke -instanceid 1 setter iterateInstances");
 		
 		assertTrue("command did not execute" , iterateInstances.contains("OK"));
 		assertTrue("iteratoring over instances", iterateInstances.contains("myValue1") && iterateInstances.contains("myValue2"));
 		uninstallApplication();
 	}
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT , groups="1", enabled = true )
 	public void testRemoveThisService() throws Exception {
 		installApplication();
 		runCommand("connect " + restUrl + ";use-application attributesTestApp" 
 				+ "; invoke setter setService; invoke setter setService2");
 		String getOutputAfterSet = runCommand("connect " + restUrl + ";use-application attributesTestApp" 
 				+ "; invoke setter getService; invoke setter getService2");
 		assertTrue("set command did not execute" , getOutputAfterSet.contains("myValue") && getOutputAfterSet.contains("myValue2"));
 		
 		runCommand("connect " + restUrl + ";use-application attributesTestApp" 
 				+ "; invoke setter removeService");
 		String getOutputAfterRemove = runCommand("connect " + restUrl + ";use-application attributesTestApp" 
 				+ "; invoke setter getService ;invoke setter getService2");
 		assertTrue("get myKey command should return null after myKey was removed" , getOutputAfterRemove.contains("null"));
 		assertTrue("myKey2 should not be affected by remove myKey" , getOutputAfterRemove.contains("myValue2"));
 		uninstallApplication();
 	}
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT , groups="1", enabled = true)
 	public void testRemoveInstance() throws Exception {
 		installApplication();
 		runCommand("connect " + restUrl + ";use-application attributesTestApp" 
 				+ "; invoke setter setInstance"); 
 		String getOutputAfterSet = runCommand("connect " + restUrl + ";use-application attributesTestApp" 
 				+ "; invoke setter getInstance");
 		assertTrue("set command did not execute" , getOutputAfterSet.contains("myValue"));
 		
 		runCommand("connect " + restUrl + ";use-application attributesTestApp" 
 				+ "; invoke setter removeInstance");
 		String getOutputAfterRemove = runCommand("connect " + restUrl + ";use-application attributesTestApp" 
 				+ "; invoke setter getInstance");
 		assertTrue("getInstance command should return null after key was removed" , getOutputAfterRemove.toLowerCase().contains("null"));
 		uninstallApplication();
 	}
 	
 	
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT , groups="1", enabled = true)
 	public void testCleanInstanceAfterSetService() throws Exception {
 		installApplication();
 		runCommand("connect " + restUrl + ";use-application attributesTestApp" 
 				+ "; invoke setter setInstance1");
 		runCommand("connect " + restUrl + ";use-application attributesTestApp" 
 				+ "; invoke setter setInstance2");
 		String getInstanceBeforeRemove = runCommand("connect " + restUrl + ";use-application attributesTestApp" 
 				+ "; invoke -instanceid 1 setter getInstance");
 		assertTrue("set command did not execute" , getInstanceBeforeRemove.contains("myValue1"));
 		getInstanceBeforeRemove = runCommand("connect " + restUrl + ";use-application attributesTestApp" 
 				+ "; invoke -instanceid 2 setter getInstance");
 		assertTrue("set command did not execute" , getInstanceBeforeRemove.contains("myValue2"));
 		
 		runCommand("connect " + restUrl + ";use-application attributesTestApp" 
 				+ "; invoke -instanceid 1 setter cleanThisInstance");
 		String getInstanceAfterRemove = runCommand("connect " + restUrl + ";use-application attributesTestApp" 
 				+ "; invoke -instanceid 1 setter getInstance");
 		assertTrue("get command should return null after key was removed" , getInstanceAfterRemove.toLowerCase().contains("null"));
 		getInstanceAfterRemove = runCommand("connect " + restUrl + ";use-application attributesTestApp" 
 				+ "; invoke -instanceid 2 setter getInstance");
 		assertTrue("clear on instance 1 should not affect instance 2" , getInstanceAfterRemove.contains("myValue2"));
 		uninstallApplication();
 	}
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT , groups="1", enabled = true)
 	public void testCleanAppAfterSetService() throws Exception {
 		installApplication();
 		runCommand("connect " + restUrl + ";use-application attributesTestApp" 
 				+ "; invoke setter setService; invoke setter setService2");
 		String getServiceBeforeClear = runCommand("connect " + restUrl + ";use-application attributesTestApp" 
 				+ "; invoke setter getService; invoke setter getService2");
 		assertTrue("set command did not execute" , getServiceBeforeClear.contains("myValue") && getServiceBeforeClear.contains("myValue2"));
 				
 		runCommand("connect " + restUrl + ";use-application attributesTestApp" 
 				+ "; invoke getter cleanThisApp");
 		String getServiceAfterClear = runCommand("connect " + restUrl + ";use-application attributesTestApp" 
 				+ "; invoke setter setService ; invoke setter setService2");
 		assertTrue("clear app should not affect service attributes" , 
 				getServiceAfterClear.contains("myValue") && getServiceAfterClear.contains("myValue2"));
 		uninstallApplication();
 	}
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT , groups="1", enabled = true)
 	public void testSimpleSetGlobalAttributesOneApp() throws Exception {
 		installApplication();
 		runCommand("connect " + restUrl + ";use-application attributesTestApp" 
 				+ "; invoke setter setGlobal myGValue");
 		
 		String simpleGet = runCommand("connect " + restUrl + ";use-application attributesTestApp" 
 				+ "; invoke getter getGlobal");
 		
 		assertTrue("command did not execute" , simpleGet.contains("OK"));
 		assertTrue("getter service cannot get the global attribute when using parameters", simpleGet.contains("myGValue"));
 		uninstallApplication();
 	}
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT , groups="1", enabled = true)
 	public void testGlobalSetAttributeCustomParamsTwoApps() throws Exception {
 		installApplication();
 		LogUtils.log("setting an global attribute from setter service");
 		runCommand("connect " + restUrl + ";use-application attributesTestApp" 
 				+ "; invoke setter setGlobalCustom myGlobalKey myGlobalValue");
 
 		String simpleGet = runCommand("connect " + restUrl + ";use-application attributesTestApp" 
 				+ "; invoke getter getGlobalCustom myGlobalKey");
 	
 		//install a different app
 		installApplication(SECONDARY_APPLICATION_NAME);
 		
 		//Get the attribute from a different application
 		String simpleGet2 = runCommand("connect " + restUrl + ";use-application attributesTestApp2" 
 				+ "; invoke getter getGlobalCustom myGlobalKey");
 		
 		assertTrue("command did not execute" , simpleGet.contains("OK"));
 		assertTrue("command did not execute" , simpleGet2.contains("OK"));
 		assertTrue("getter service cannot get the application attribute", simpleGet.contains("myGlobalValue"));
 		assertTrue("setter service cannot get the application attribute", simpleGet2.contains("myGlobalValue"));
 		uninstallApplication(SECONDARY_APPLICATION_NAME);
 		uninstallApplication();
 	}
 
 	private void installApplication() throws IOException, InterruptedException {
 		installApplication(MAIN_APPLICATION_NAME);
 		cleanAttributes();
         		
 		final String absolutePUNameSimple1 = ServiceUtils.getAbsolutePUName(MAIN_APPLICATION_NAME, "getter");
 		final String absolutePUNameSimple2 = ServiceUtils.getAbsolutePUName(MAIN_APPLICATION_NAME, "setter");
 		final ProcessingUnit pu1 = admin.getProcessingUnits().waitFor(absolutePUNameSimple1 , WAIT_FOR_TIMEOUT_SECONDS , TimeUnit.SECONDS);
 		final ProcessingUnit pu2 = admin.getProcessingUnits().waitFor(absolutePUNameSimple2, WAIT_FOR_TIMEOUT_SECONDS , TimeUnit.SECONDS);
 		assertNotNull(pu1);
 		assertNotNull(pu2);
 		assertTrue("applications was not installed", pu1.waitFor(pu1.getTotalNumberOfInstances(), WAIT_FOR_TIMEOUT_SECONDS, TimeUnit.SECONDS));
 		assertTrue("applications was not installed", pu2.waitFor(pu2.getTotalNumberOfInstances(), WAIT_FOR_TIMEOUT_SECONDS, TimeUnit.SECONDS));
 		assertNotNull("applications was not installed", admin.getApplications().getApplication(MAIN_APPLICATION_NAME));
 		assertTrue("USM Service State is NOT RUNNING", USMTestUtils.waitForPuRunningState(absolutePUNameSimple1, 60, TimeUnit.SECONDS, admin));
 		assertTrue("USM Service State is NOT RUNNING", USMTestUtils.waitForPuRunningState(absolutePUNameSimple2, 60, TimeUnit.SECONDS, admin));
 	}
 
 	private void uninstallApplication() {
 		uninstallApplication(MAIN_APPLICATION_NAME);
 	}
 
 }
