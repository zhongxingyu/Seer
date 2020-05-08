 package org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.scale;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.TimeUnit;
 
 import org.apache.commons.io.FileUtils;
 import org.cloudifysource.esc.driver.provisioning.jclouds.DefaultProvisioningDriver;
 import org.openspaces.admin.zone.config.AnyZonesConfig;
 import org.openspaces.admin.zone.config.ZonesConfig;
 import org.testng.Assert;
 import org.testng.annotations.AfterClass;
 import org.testng.annotations.AfterMethod;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 import org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.services.ec2.Ec2CloudService;
 
 import iTests.framework.tools.SGTestHelper;
 import iTests.framework.utils.DeploymentUtils;
 import iTests.framework.utils.IOUtils;
 import iTests.framework.utils.LogUtils;
 import iTests.framework.utils.TestUtils;
 
 public class Ec2LocationAwareScalingRulesTest extends AbstractScalingRulesCloudTest {
 	
 	private static final String LOCATION_AWARE_POSTFIX = "-location-aware";
 	private static final String NEWLINE = System.getProperty("line.separator");
 	private static final long STEADY_STATE_DURATION = 1000 * 120; // 120 seconds
 	
 	
 	@Override
 	public void beforeBootstrap() throws IOException {
 		
 		// copy custom location aware driver to cloudify-overrides
		File locationAwareDriver = DeploymentUtils.getArchive("location-aware-driver.jar");
 		File uploadOverrides =
 				new File(getService().getPathToCloudFolder() + "/upload/cloudify-overrides/");
 		if (!uploadOverrides.exists()) {
 			uploadOverrides.mkdir();
 		}
 		File uploadEsmDir = new File(uploadOverrides.getAbsoluteFile() + "/lib/platform/esm");
 		File localEsmFolder = new File(SGTestHelper.getBuildDir() + "/lib/platform/esm");
 		
 		FileUtils.copyFileToDirectory(locationAwareDriver, uploadEsmDir, true);
 		FileUtils.copyFileToDirectory(locationAwareDriver, localEsmFolder, false);		
 		
 		final Map<String, String> propsToReplace = new HashMap<String, String>();
 		final String oldCloudDriverClazz = DefaultProvisioningDriver.class.getName();
 		String newCloudDriverClazz = null;
 		
 		String region = ((Ec2CloudService) getService()).getRegion();
 		
 		if (region.contains("eu")) {
 			newCloudDriverClazz = "org.cloudifysource.test.EUWestLocationAwareDriver";
 		} else {
 			newCloudDriverClazz = "org.cloudifysource.test.USEastLocationAwareDriver";
 		}
 		propsToReplace.put(toClassName(oldCloudDriverClazz),toClassName(newCloudDriverClazz));
 		IOUtils.replaceTextInFile(getService().getPathToCloudGroovy(), propsToReplace);
 	}
 	
 	@Override
 	protected String getCloudName() {
 		return "ec2";
 	}
 	
 	@BeforeClass(alwaysRun = true)
 	protected void bootstrap() throws Exception {
 		super.bootstrap();
 		cloneApplicaitonRecipeAndInjectLocationAware();
 	}
 	
 	@BeforeMethod
 	public void startExecutorService() {	
 		super.startExecutorService();	
 	}
 		
 	@Override
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT * 2, enabled = true)
 	public void testPetclinicSimpleScalingRules() throws Exception {	
 		
 		final String applicationPath = getApplicationPath();
 		installApplicationAndWait(applicationPath, getApplicationName());
 		
 		// check that there are two global instances
 		repititiveAssertNumberOfInstances(getAbsoluteServiceName(),new AnyZonesConfig(), 2);
 
 		Set<ZonesConfig> puExactZones = getProcessingUnitZones(getAbsoluteServiceName());
 		ZonesConfig zonesToPerformAutoScaling = puExactZones.iterator().next(); // just take the first zone
 		
 		// increase web traffic for the instance of the specific zone, wait for scale out
 		LogUtils.log("starting threads on an instance with zones " + zonesToPerformAutoScaling.getZones());
 		
 		InstanceDetails instanceToPing = getInstancesDetails(getAbsoluteServiceName(), zonesToPerformAutoScaling).get(0);
 		
 		startThreads(instanceToPing);
 		repititiveAssertNumberOfInstances(getAbsoluteServiceName(),zonesToPerformAutoScaling, 2);
 		// assert that we reach a steady state. number of instances should not increase any further since 2 is the maximum per zone
 		repetitiveNumberOfInstancesHolds(getAbsoluteServiceName(), zonesToPerformAutoScaling, 2, STEADY_STATE_DURATION, TimeUnit.MILLISECONDS);
 
 		// stop web traffic, wait for scale in
 		stopThreads();
 		repititiveAssertNumberOfInstances(getAbsoluteServiceName(), zonesToPerformAutoScaling, 1);
 		// assert that we reach a steady state. number of instances should not decrease any further since 1 is the minimum per zone
 		repetitiveNumberOfInstancesHolds(getAbsoluteServiceName(), zonesToPerformAutoScaling, 1, STEADY_STATE_DURATION, TimeUnit.MILLISECONDS);
 
 		LogUtils.log("stopping threads");
 		stopThreads();
 		uninstallApplicationAndWait(getApplicationName());
 	}
 	
 	@Test(timeOut = DEFAULT_TEST_TIMEOUT * 2, enabled = true)
 	public void testScaleOutCancelation() throws Exception {	
 		
 		LogUtils.log("installing application " + getApplicationName());
 
 		final String applicationPath = getApplicationPath();
 		installApplicationAndWait(applicationPath, getApplicationName());
 		
 		// check that there are two global instances
 		repititiveAssertNumberOfInstances(getAbsoluteServiceName(),new AnyZonesConfig(), 2, OPERATION_TIMEOUT, TimeUnit.MILLISECONDS);
 
 		Set<ZonesConfig> puExactZones = getProcessingUnitZones(getAbsoluteServiceName());
 		ZonesConfig zonesToPerformAutoScaling = puExactZones.iterator().next(); // just take the first zone
 
 		InstanceDetails instanceToPing = getInstancesDetails(getAbsoluteServiceName(), zonesToPerformAutoScaling).get(0);
 		
 		// Try to start a new machine and then cancel it.
 		startThreads(instanceToPing);
 		executor.schedule(new Runnable() {
 
 			@Override
 			public void run() {
 				stopThreads();
 
 			}
 		}, 30, TimeUnit.SECONDS);
 	
 		repetitiveNumberOfInstancesHolds(getAbsoluteServiceName(),zonesToPerformAutoScaling, 1, 500, TimeUnit.SECONDS);
 	
 		LogUtils.log("stopping threads");
 		stopThreads();
 		LogUtils.log("uninstalling application");
 		uninstallApplicationAndWait(getApplicationName());
 		
 		super.scanForLeakedAgentNodes();
 	}
 	
 	private Set<ZonesConfig> getProcessingUnitZones(
 			String absoluteServiceName) throws Exception {
 		List<InstanceDetails> detailss = getInstancesDetails(absoluteServiceName, new AnyZonesConfig());
 		
 		Set<ZonesConfig> zones = new HashSet<ZonesConfig>();
 		for (InstanceDetails details : detailss) {
 			zones.add(details.getAgentZones());
 		}
 		
 		return zones;
 	}
 	
 
 	@AfterMethod(alwaysRun = true)
 	public void cleanup() throws IOException, InterruptedException {
 		super.cleanup();
 	}
 	
 	@AfterClass(alwaysRun = true)
 	protected void teardown() throws Exception {
 		super.teardown();
 	}
 	
 	private void cloneApplicaitonRecipeAndInjectLocationAware() {
 		try {
 			FileUtils.copyDirectory(new File(super.getApplicationPath()), new File(this.getApplicationPath()));
 			final File newServiceFile = new File(this.getApplicationPath(),"tomcat/tomcat-service.groovy");
 			TestUtils.writeTextFile(newServiceFile,
 					"service {" +NEWLINE +
 						"\textend \"../../../services/tomcat\""+NEWLINE+
 						"\tlocationAware true"+NEWLINE+
 						"\tnumInstances 2"+NEWLINE+        // initial total number of instances 2
 						"\tminAllowedInstances 1"+NEWLINE+ // total
 						"\tmaxAllowedInstances 4"+NEWLINE+ // total
 						"\tminAllowedInstancesPerLocation 1"+NEWLINE+ // per zone
 						"\tmaxAllowedInstancesPerLocation 2"+NEWLINE+ // per zone
 					"}");
 		} catch (final IOException e) {
 			Assert.fail("Failed to create " + this.getApplicationPath(),e);
 		}
 	}
 
 
 	@Override
 	protected String getApplicationPath() {
 		final File applicationPath = new File(super.getApplicationPath());
 		final File newApplicationPath = new File(applicationPath.getParentFile(), applicationPath.getName() + LOCATION_AWARE_POSTFIX);
 		return newApplicationPath.getAbsolutePath();
 	}
 	
 	public String toClassName(String className) {
 		return "className \""+className+"\"";
 	}
 
 }
