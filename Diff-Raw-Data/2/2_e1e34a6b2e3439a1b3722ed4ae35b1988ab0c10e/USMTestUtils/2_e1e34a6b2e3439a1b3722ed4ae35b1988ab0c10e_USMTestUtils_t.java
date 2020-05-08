 package test.usm;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.UnknownHostException;
 import java.util.Map;
 import java.util.concurrent.TimeUnit;
 import java.util.logging.Logger;
 
 import org.openspaces.admin.Admin;
 import org.openspaces.admin.AdminFactory;
 import org.openspaces.admin.gsm.GridServiceManager;
 import org.openspaces.admin.pu.ProcessingUnit;
 import org.openspaces.admin.pu.ProcessingUnitDeployment;
 import org.openspaces.admin.pu.ProcessingUnitInstance;
 import org.openspaces.admin.pu.ProcessingUnitInstanceStatistics;
 import org.openspaces.pu.service.ServiceMonitors;
 import org.testng.Assert;
 
 import com.gigaspaces.cloudify.dsl.Service;
 import com.gigaspaces.cloudify.dsl.internal.CloudifyConstants;
 import com.gigaspaces.cloudify.dsl.internal.ServiceReader;
 import com.gigaspaces.cloudify.dsl.internal.CloudifyConstants.USMState;
 import com.gigaspaces.cloudify.dsl.internal.packaging.Packager;
 import com.gigaspaces.cloudify.dsl.internal.packaging.PackagingException;
 import com.gigaspaces.cloudify.dsl.utils.ServiceUtils;
 
 import framework.tools.SGTestHelper;
 import framework.utils.LogUtils;
 
 public class USMTestUtils {
 
 	private static final String USM_MONITOR = "USM";
 	private static final String LOOKUP_LOCATORS_ENV = "LOOKUPLOCATORS";
 	private static final String LOOKUP_GROUPS_ENV = "LOOKUPGROUPS";
 	private static final long GSM_WAIT_TIME = 15 * 1000;
 
 	public final static Logger logger = Logger.getLogger(USMTestUtils.class.getName());
 
 	/**
 	 * 
 	 * @param processFolder - full path inside SGTest of the process. including /apps/USM/usm if necessary
 	 * @return
 	 */
 	public static File usmCreateJar(String processFolder) {
 		System.setProperty("com.gs.home", SGTestHelper.getBuildDir());
 		try {
 			return Packager.pack(new File(SGTestHelper.getSGTestRootDir() , processFolder));
 		} catch (Exception e) {
 			Assert.fail("failed to create usm jar file",e);
 			return null;
 		}
 	}
 
 
     
     private static Service packAndDeploy(final String folderPath, final String serviceFileName, String processName) throws IOException,
     PackagingException {
     	
     	System.setProperty("com.gs.home", SGTestHelper.getBuildDir());
     	
     	Service service;
     	if (serviceFileName == null) {
     		service = ServiceReader.readService(new File(folderPath));
     	}
     	else {
     		service = ServiceReader.readService(new File(folderPath,serviceFileName));
     	}
 
     	return packAndDeploy(folderPath, serviceFileName, service, processName);
     }
 
 
     public static Service packAndDeploy(final String folderPath, Service service, String absolutePuName) throws IOException, PackagingException {
     	return packAndDeploy(folderPath, null, service, absolutePuName);
     }
     
     
 
 	private static Service packAndDeploy(final String folderPath, final String serviceFileName, Service service, String absolutePuName) throws IOException,
 			PackagingException {
		final File puZipFile = Packager.pack(new File(folderPath));
 
     	final ProcessingUnitDeployment processingUnitDeployment = new ProcessingUnitDeployment(puZipFile).numberOfInstances(service.getNumInstances()).name(absolutePuName);    	
     	deploy(processingUnitDeployment, puZipFile, serviceFileName);
     	logger.info("deployed " + puZipFile.getName());
     	puZipFile.deleteOnExit();
     	return service;
 	}
     
     private static void deploy(ProcessingUnitDeployment processingUnitDeployment, final File packagedPuFile, String serviceFileName) {
     	Admin admin = null;
     	try {
     		admin = createAdmin();
     		final GridServiceManager gsm =
     			admin.getGridServiceManagers().waitForAtLeastOne(GSM_WAIT_TIME, TimeUnit.MILLISECONDS);
     		if (gsm == null) {
     			admin.close();
     			throw new IllegalStateException("could not find a Gsm");
     		}
     		logger.info("Found gsm, deploying " + packagedPuFile.getName());
     		
 
     		if (serviceFileName != null) {
     			processingUnitDeployment.setContextProperty("com.gs.cloudify.service-file-name", serviceFileName);
     		}
     		gsm.deploy(processingUnitDeployment);
 
     	} finally {
     		if (admin != null) {
     			admin.close();
     		}
     	}
     }
 
     private static Admin createAdmin() {
     	String locators = System.getProperty("com.gs.jini_lus.locators");
     	String groups = System.getProperty("com.gs.jini_lus.groups");
 
     	// check env
     	if (locators == null) {
     		locators = System.getenv().get(LOOKUP_LOCATORS_ENV);
     	}
     	if (groups == null) {
     		groups = System.getenv().get(LOOKUP_GROUPS_ENV);
     	}
 
     	logger.fine("using locators: " + locators);
     	logger.fine("using groups: " + groups);
 
     	final StringBuilder sb = new StringBuilder();
     	final AdminFactory adminFactory = new AdminFactory();
 
     	if ((locators != null) && (locators.length() > 0)) {
     		adminFactory.addLocators(locators);
     		sb.append("with locators " + locators);
     	} else if ((groups != null) && (groups.length() > 0)) {
     		adminFactory.addGroups(groups);
     		sb.append("with lookupGroups " + groups);
     	}
 
     	final Admin admin = adminFactory.createAdmin();
     	if (admin == null) {
     		sb.insert(0, "Could not create an Admin ");
     		throw new IllegalStateException(sb.toString());
     	}
     	logger.fine("created admin " + sb.toString());
     	return admin;
     }
 
     
     
 	
     /*******
      * Assumes there is only one service configuration file in the folder, in the format *-service.groovy
      * @param processName
      * @return the service.
      * @throws PackagingException 
      * @throws IOException 
      */
 	public static Service usmDeploy(String processName) throws IOException, PackagingException {
 		return usmDeploy(processName, null);
 	}
 	
 	public static Service usmDeploy(String processName, String serviceFileName) throws IOException, PackagingException {
 		final String processFolder = SGTestHelper.getSGTestRootDir() + "/apps/USM/usm/" + ServiceUtils.getFullServiceName(processName).getServiceName();
 		
 		return packAndDeploy(processFolder, serviceFileName, processName);
 	}
 	    
     public static Long getActualPID(ProcessingUnitInstance puInstance) {
         return (Long) getMonitor(puInstance, "Actual Process ID");
     }
     
     public static Long getChildPID(ProcessingUnitInstance puInstance) {
         return (Long) getMonitor(puInstance, "Child Process ID");
     }
 
     public static Object getMonitor(ProcessingUnitInstance puInstance,String monitorName) {
     	ServiceMonitors serviceMonitors = puInstance.getStatistics().getMonitors().get(USM_MONITOR);
 		Map<String, Object> monitors = serviceMonitors.getMonitors();
 		return monitors.get(monitorName);
     }
 
     public static void assertMonitors(ProcessingUnit pu){
     	for(ProcessingUnitInstance pui : pu.getInstances()){
     		assertMonitors(pui, new String[] {});
     	}
     }
     
     public static void assertMonitors(ProcessingUnit pu, String[] monitors){
     	for(ProcessingUnitInstance pui : pu.getInstances()){
     		assertMonitors(pui, monitors);
     	}
     }
     
     public static void assertMonitors(ProcessingUnitInstance pui){
     	assertMonitors(pui, new String[] {});
     }
     
     private static void assertMonitors(ProcessingUnitInstance pui, String[] monitors){
     	long pid = getActualPID(pui);
         Assert.assertNotNull(pid);
         long childPid = getChildPID(pui);
         Assert.assertNotNull(childPid);
         for (String monitorName : monitors) {
         	Assert.assertNotNull(getMonitor(pui, monitorName),monitorName + " monitor cannot be null.");
         }
     }
 
 	public static void assertPIDDoesntExist(ProcessingUnit pu, long pidToDec) {
 		for(ProcessingUnitInstance pui : pu.getInstances()){
 			Assert.assertTrue(pidToDec != getActualPID(pui));
 		}
 	}
 
 	public static void assertPIDExists(ProcessingUnit pu, Long pid1) {
 		for(ProcessingUnitInstance pui : pu.getInstances()){
 			if (getActualPID(pui).equals(pid1)){
 				return;
 			}
 		}
 		Assert.fail("pid " + pid1 + " doesnt exist");
 	}
 	
 	public static boolean waitForPuRunningState(String absolutePuName, long timeout, TimeUnit timeunit,Admin admin) throws UnknownHostException{
 		long end = System.currentTimeMillis() + timeunit.toMillis(timeout);
 		while (System.currentTimeMillis() < end) {
 			if (isUSMServiceRunning(absolutePuName, admin)){
 				return true;
 			}
 		}
 		LogUtils.log("USM Service state is " + getUSMServiceState(absolutePuName, admin));
 		return false;
 	}
 	
 	public static USMState getUSMServiceState(String absoluteServiceName, Admin admin) throws UnknownHostException{
 		ProcessingUnit processingUnit = admin.getProcessingUnits().waitFor(absoluteServiceName, 60, TimeUnit.SECONDS);
 		int state = 0;
 		boolean instance = processingUnit.waitFor(1,60, TimeUnit.SECONDS);
 		if(instance){
 			ProcessingUnitInstance processingUnitInstance = processingUnit.getInstances()[0];
 			ProcessingUnitInstanceStatistics statistics = processingUnitInstance.getStatistics();
 			ServiceMonitors serviceMonitors = statistics.getMonitors().get(CloudifyConstants.USM_MONITORS_SERVICE_ID);
 			state = (Integer)serviceMonitors.getMonitors().get(CloudifyConstants.USM_MONITORS_STATE_ID);
 		}
 		return USMState.values()[state];
 	}
 	
 	public static USMState getServiceState(String serviceName, String applicationName, Admin admin) throws UnknownHostException{
 		String absolutePUName = ServiceUtils.getAbsolutePUName(applicationName, serviceName);
 		return getUSMServiceState(absolutePUName, admin);
 	}
 	
 	public static boolean isUSMServiceRunning(String absoluteServiceName, Admin admin) throws UnknownHostException{
 		USMState serviceState = getUSMServiceState(absoluteServiceName, admin);
 		return serviceState.equals(USMState.RUNNING);
 	}
 	
 	public static boolean isUSMServiceRunning(String serviceName, String applicationName, Admin admin) throws UnknownHostException{
 		String absolutePUName = ServiceUtils.getAbsolutePUName(applicationName, serviceName);
 		return isUSMServiceRunning(absolutePUName, admin);
 	}
 }
