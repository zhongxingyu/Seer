 package test.cli.cloudify.xen;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 
 import org.cloudifysource.dsl.Service;
 import org.cloudifysource.dsl.internal.CloudifyConstants;
 import org.cloudifysource.dsl.internal.DSLException;
 import org.cloudifysource.dsl.internal.ServiceReader;
 import org.cloudifysource.dsl.internal.packaging.PackagingException;
 import org.cloudifysource.dsl.utils.ServiceUtils;
 import org.openspaces.admin.pu.ProcessingUnit;
 import org.testng.Assert;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 import test.cli.cloudify.CommandTestUtils;
 import framework.utils.LogUtils;
 import framework.utils.ScriptUtils;
 
 public class TravelAppFailOverUsingXenTest extends AbstractApplicationFailOverXenTest {
 	
 	private static final String TRAVEL_APPLICATION_NAME = "travel";
 	private static final String TOMCAT_PU_NAME = "tomcat";
 	private static final String TOMCAT_ABSOLUTE_PU_NAME = ServiceUtils.getAbsolutePUName(TRAVEL_APPLICATION_NAME,TOMCAT_PU_NAME);
 	private static final String CASSANDRA_PU_NAME = "cassandra";
 	private static final String CASSANDRA_ABSOLUTE_PU_NAME = ServiceUtils.getAbsolutePUName(TRAVEL_APPLICATION_NAME, CASSANDRA_PU_NAME);
	private final String travelAppDirPath = ScriptUtils.getBuildPath() + "/examples/travel";
 	private int tomcatPort;
 	private String travelHostIp;
 		
 	@Override
 	@BeforeMethod
 	public void beforeTest() {
 		super.beforeTest();
 		tomcatPort = tomcatPort();
 		
 		startAgent(0 ,TOMCAT_PU_NAME,CASSANDRA_PU_NAME);
 	    repetitiveAssertNumberOfGSAsAdded(3, OPERATION_TIMEOUT);
 	    repetitiveAssertNumberOfGSAsRemoved(0, OPERATION_TIMEOUT);	 
 	    travelHostIp = admin.getZones().getByName(CASSANDRA_PU_NAME).getGridServiceAgents().getAgents()[0].getMachine().getHostAddress();
 	}
 	
 	private void uninstallApplication() {
 		try {			
 			CommandTestUtils.runCommandAndWait("connect " + restUrl + " ;uninstall-application travel");
 		} catch (Exception e) {	
 			Assert.fail("Failed to uninstall application travel",e);
 		}
 		
 		assertAppUninstalled("travel");	
 	}
 
 ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT, groups = "1", enabled = true)
 	public void testTravelAppEagerModeNoFailover() throws Exception{
 		installTravelApplication(tomcatPort, travelHostIp, travelAppDirPath);
 		uninstallApplication();
 	}
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT, groups = "1", enabled = false)
 	public void testTravelAppTomcatPuInstFailOver() throws Exception{
 		installTravelApplication(tomcatPort, travelHostIp, travelAppDirPath);
 		
 		ProcessingUnit tomcat = admin.getProcessingUnits().getProcessingUnit(TOMCAT_ABSOLUTE_PU_NAME);
 		
 		int tomcatPuInstancesAfterInstall = tomcat.getInstances().length;
 		LogUtils.log("destroying the pu instance holding tomcat");
 		tomcat.getInstances()[0].destroy();
 		assertPuInstanceKilled(TOMCAT_ABSOLUTE_PU_NAME , tomcatPort ,travelHostIp , tomcatPuInstancesAfterInstall);
 		assertPuInstanceRessurected(TOMCAT_ABSOLUTE_PU_NAME , tomcatPort ,travelHostIp, tomcatPuInstancesAfterInstall) ;
 		uninstallApplication();
 	}
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT, groups = "1", enabled = false)
 	public void testTravelAppTomcatGSCFailOver() throws Exception{
 		installTravelApplication(tomcatPort, travelHostIp, travelAppDirPath);
 		
 		ProcessingUnit tomcat = admin.getProcessingUnits().getProcessingUnit(TOMCAT_ABSOLUTE_PU_NAME);
 		int tomcatPuInstancesAfterInstall = tomcat.getInstances().length;
 		LogUtils.log("restarting GSC containing tomcat");
 		tomcat.getInstances()[0].getGridServiceContainer().kill();
 		assertPuInstanceKilled(TOMCAT_PU_NAME , tomcatPort,travelHostIp , tomcatPuInstancesAfterInstall);
 		assertPuInstanceRessurected(TOMCAT_PU_NAME , tomcatPort,travelHostIp , tomcatPuInstancesAfterInstall);
 		uninstallApplication();
 	}
 
 /////////////////////////////////////////////////////////////////////////////////////////////////////////
 	
 	private void isTravelAppInstalled(int tomcatPort ,String host) {
 		ProcessingUnit cassandra = admin.getProcessingUnits().waitFor(CASSANDRA_ABSOLUTE_PU_NAME);
 		ProcessingUnit tomcat = admin.getProcessingUnits().waitFor(TOMCAT_ABSOLUTE_PU_NAME);
 		assertNotNull("cassandra was not deployed", cassandra);
 		assertNotNull("tomcat was not deployed", tomcat);
 		
 		boolean cassandraDeployed = cassandra.waitFor(cassandra.getTotalNumberOfInstances());
 		boolean tomcatDeployed = tomcat.waitFor(tomcat.getTotalNumberOfInstances());
 		
 		assertTrue("cassandra pu didn't deploy all of it's instances", cassandraDeployed);
 		assertTrue("tomcat pu didn't deploy all of it's instances", tomcatDeployed);
 		
 		LogUtils.log("asserting needed ports for application are taken");
 		boolean tomcatPortAvailible = portIsAvailible(tomcatPort ,host);
 		assertTrue("tomcat port was not occupied after installation - port number " + tomcatPort, !tomcatPortAvailible);
 		
 		LogUtils.log("Travel application installed");
 	}
 	
 	private int tomcatPort() {
 		Service tomcatService = null;
 		try {
 			tomcatService = ServiceReader.getServiceFromDirectory(new File(travelAppDirPath + "/" + TOMCAT_PU_NAME), CloudifyConstants.DEFAULT_APPLICATION_NAME).getService();
 		} catch (FileNotFoundException e) {
 			Assert.fail("tomcat port",e);
 		} catch (PackagingException e) {
 			Assert.fail("tomcat port",e);
 		} catch (DSLException e) {
 			Assert.fail("tomcat port",e);
 		}
 		return tomcatService.getNetwork().getPort();
 	}
 	
 	private void installTravelApplication(int tomcatPort, String host, String appDirPath) throws IOException, InterruptedException {
 		LogUtils.log("asserting needed ports for application are availible");
 		boolean travelPortAvailible = portIsAvailible(tomcatPort, host);
 		assertTrue("port was not free before installation - port number " + tomcatPort, travelPortAvailible);
 		    
 		String commandOutput = CommandTestUtils.runCommandAndWait("connect --verbose " + restUrl + ";install-application --verbose " + appDirPath);
 		String appName = new File(appDirPath).getName();
 		assertTrue("install-application command didn't install the application" , commandOutput.contains("Application " + appName + " installed successfully"));
 		
 		isTravelAppInstalled(tomcatPort, host);
 	}
 }
