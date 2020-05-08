 package org.cloudifysource.quality.iTests.test.esm;
 
 import iTests.framework.utils.AssertUtils;
 import iTests.framework.utils.DumpUtils;
 import iTests.framework.utils.GridServiceAgentsCounter;
 import iTests.framework.utils.GridServiceContainersCounter;
 import iTests.framework.utils.LogUtils;
 import iTests.framework.utils.SSHUtils;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.TimeUnit;
 
 import org.cloudifysource.domain.cloud.Cloud;
 import org.cloudifysource.domain.cloud.compute.ComputeTemplate;
 import org.cloudifysource.dsl.internal.CloudifyConstants;
 import org.cloudifysource.esc.driver.provisioning.CloudifyMachineProvisioningConfig;
 import org.cloudifysource.esc.driver.provisioning.ElasticMachineProvisioningCloudifyAdapter;
 import org.cloudifysource.quality.iTests.framework.utils.ByonMachinesUtils;
 import org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.byon.AbstractByonCloudTest;
 import org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.services.byon.ByonCloudService;
import org.cloudifysource.quality.iTests.test.esm.component.machines.AbstractMachinesSlaEnforcementTest;
 import org.openspaces.admin.GridComponent;
 import org.openspaces.admin.esm.ElasticServiceManager;
 import org.openspaces.admin.esm.ElasticServiceManagers;
 import org.openspaces.admin.esm.events.ElasticServiceManagerAddedEventListener;
 import org.openspaces.admin.gsa.GridServiceAgent;
 import org.openspaces.admin.gsa.events.ElasticGridServiceAgentProvisioningProgressChangedEvent;
 import org.openspaces.admin.gsa.events.ElasticGridServiceAgentProvisioningProgressChangedEventListener;
 import org.openspaces.admin.machine.Machine;
 import org.openspaces.admin.machine.events.ElasticMachineProvisioningProgressChangedEvent;
 import org.openspaces.admin.machine.events.ElasticMachineProvisioningProgressChangedEventListener;
 import org.openspaces.admin.pu.ProcessingUnit;
 import org.openspaces.admin.pu.elastic.ElasticStatefulProcessingUnitDeployment;
 import org.openspaces.admin.pu.elastic.ElasticStatelessProcessingUnitDeployment;
 import org.openspaces.admin.pu.elastic.config.DiscoveredMachineProvisioningConfig;
 import org.openspaces.admin.space.ElasticSpaceDeployment;
 import org.openspaces.admin.space.SpaceDeployment;
 import org.openspaces.grid.gsm.machines.plugins.events.MachineStartRequestedEvent;
 import org.openspaces.grid.gsm.machines.plugins.events.MachineStartedEvent;
 import org.openspaces.grid.gsm.machines.plugins.events.MachineStopRequestedEvent;
 import org.openspaces.grid.gsm.machines.plugins.events.MachineStoppedEvent;
 
 public class AbstractFromXenToByonGSMTest extends AbstractByonCloudTest {
 	
 	public final static long OPERATION_TIMEOUT = 5 * 60 * 1000;
 	public final static String DEFAULT_BYON_XAP_MACHINE_MEMORY_MB = "5000";
 	public final static String STANDARD_MACHINE_MEMORY_MB = "5850";
     public final static String LRMI_BIND_PORT_RANGE = "7010-7110";
 	public final static int NUM_OF_CORES = 2;
 	protected static final boolean ENABLE_LOGSTASH = Boolean.parseBoolean(System.getProperty("iTests.enableLogstash", "false"));
 	private static final long RESERVED_MEMORY_PER_MACHINE_MEGABYTES_DISCOVERED = 128;
 	private MachinesEventsCounter machineEventsCounter;
 	private ElasticMachineProvisioningCloudifyAdapter elasticMachineProvisioningCloudifyAdapter;
 	private GridServiceContainersCounter gscCounter;
     private GridServiceAgentsCounter gsaCounter;
     protected ElasticGridServiceAgentProvisioningProgressChangedEventListener agentEventListener;
     protected  ElasticMachineProvisioningProgressChangedEventListener machineEventListener;
     protected Machine firstManagementMachine = null;
 	
     public void repetitiveAssertNumberOfGSAsHolds(int expectedAdded, int expectedRemoved, long timeoutMilliseconds) {
     	gsaCounter.repetitiveAssertNumberOfGSAsHolds(expectedAdded, expectedRemoved, timeoutMilliseconds);
     }
     
     public void repetitiveAssertNumberOfGSAsAdded(int expected, long timeoutMilliseconds) {
     	gsaCounter.repetitiveAssertNumberOfGridServiceAgentsAdded(expected, timeoutMilliseconds);
     }
     
     public void repetitiveAssertNumberOfGSAsRemoved(int expected, long timeoutMilliseconds) {
     	gsaCounter.repetitiveAssertNumberOfGridServiceAgentsRemoved(expected, timeoutMilliseconds);
     }
     
     public void repetitiveAssertGridServiceAgentRemoved(final GridServiceAgent agent, long timeoutMilliseconds) {
     	gsaCounter.repetitiveAssertGridServiceAgentRemoved(agent, timeoutMilliseconds);
     }
     
     public void repetitiveAssertNumberOfGSCsAdded(int expected, long timeoutMilliseconds) {
     	gscCounter.repetitiveAssertNumberOfGridServiceContainersAdded(expected, timeoutMilliseconds);
     }
     
     public void repetitiveAssertNumberOfGSCsRemoved(int expected, long timeoutMilliseconds) {
     	gscCounter.repetitiveAssertNumberOfGridServiceContainersRemoved(expected, timeoutMilliseconds);
     }
 	
     public void repetitiveAssertNumberOfGridServiceContainersHolds(final int expectedAdded, final int expectedRemoved, long timeout, TimeUnit timeunit) {
     	gscCounter.repetitiveAssertNumberOfGridServiceContainersHolds(expectedAdded, expectedRemoved, timeout, timeunit);
     }
     
     protected void repetitiveAssertNumberOfMachineEvents(Class<? extends ElasticMachineProvisioningProgressChangedEvent> eventClass, int expected, long timeoutMilliseconds) {
 		machineEventsCounter.repetitiveAssertNumberOfMachineEvents(eventClass, expected, timeoutMilliseconds);
 	}
     
     protected GridServiceAgent startNewByonMachine (ElasticMachineProvisioningCloudifyAdapter elasticMachineProvisioningCloudifyAdapter, long duration,TimeUnit timeUnit) throws Exception {
     	return ByonMachinesUtils.startNewByonMachine(elasticMachineProvisioningCloudifyAdapter, duration, timeUnit);
     }
     
     protected GridServiceAgent startNewByonMachineWithZones (ElasticMachineProvisioningCloudifyAdapter elasticMachineProvisioningCloudifyAdapter,String[] zoneList, long duration,TimeUnit timeUnit) throws Exception {
     	return ByonMachinesUtils.startNewByonMachineWithZones(elasticMachineProvisioningCloudifyAdapter,zoneList, duration, timeUnit);
     }
 
     protected static GridServiceAgent[] startNewByonMachinesWithZones(
             final ElasticMachineProvisioningCloudifyAdapter elasticMachineProvisioningCloudifyAdapter,
             int numOfMachines,final String[] zoneList,  final long duration,final TimeUnit timeUnit) {
         return ByonMachinesUtils.startNewByonMachinesWithZones(elasticMachineProvisioningCloudifyAdapter,numOfMachines,zoneList,duration,timeUnit);
     }
     
     protected GridServiceAgent[] startNewByonMachines(
 			final ElasticMachineProvisioningCloudifyAdapter elasticMachineProvisioningCloudifyAdapter,
 			int numOfMachines, final long duration,final TimeUnit timeUnit) {
     	return ByonMachinesUtils.startNewByonMachines(elasticMachineProvisioningCloudifyAdapter, numOfMachines, duration, timeUnit);
     }
     
     protected static void stopByonMachine (ElasticMachineProvisioningCloudifyAdapter elasticMachineProvisioningCloudifyAdapter, GridServiceAgent agent ,long duration,TimeUnit timeUnit) throws Exception {
     	ByonMachinesUtils.stopByonMachine(elasticMachineProvisioningCloudifyAdapter, agent, duration, timeUnit);
     }
 
     /**
      * Kills an agent Process in a machine - by killing his pid.
      */
     protected static boolean stopByonMachineHard (GridServiceAgent gsa) {
         String ipAddress = gsa.getMachine().getHostAddress();
         int pid = ((int) gsa.getVirtualMachine().getDetails().getPid());
         return SSHUtils.killProcess(ipAddress, pid);
     }
 
    /**
     * @see AbstractMachinesSlaEnforcementTest#updateMachineProvisioningConfig()
     */
     private void initElasticMachineProvisioningCloudifyAdapter () throws Exception {
         elasticMachineProvisioningCloudifyAdapter = new ElasticMachineProvisioningCloudifyAdapter ();
         //clears byon deployer in use machine map in order to free machines in each test
         ElasticMachineProvisioningCloudifyAdapter.clearContext();
 		elasticMachineProvisioningCloudifyAdapter.setAdmin(admin);
 		//sets cloudify configuration directory - so the ServiceReader would be able to read the groovy file
 		//the path should be to the DIRECTORY of the groovy file
 		CloudifyMachineProvisioningConfig config = getMachineProvisioningConfig();
 		config.setCloudConfigurationDirectory(getService().getPathToCloudFolder());
 		elasticMachineProvisioningCloudifyAdapter.setProperties(config.getProperties());		
 		elasticMachineProvisioningCloudifyAdapter.afterPropertiesSet();
 		elasticMachineProvisioningCloudifyAdapter.blockingAfterPropertiesSet();
 		
 		ElasticGridServiceAgentProvisioningProgressChangedEventListener agentEventListener = new ElasticGridServiceAgentProvisioningProgressChangedEventListener() {
 
 			@Override
 			public void elasticGridServiceAgentProvisioningProgressChanged(
 					ElasticGridServiceAgentProvisioningProgressChangedEvent event) {
 				LogUtils.log(event.toString());
 			}
 		};
 		elasticMachineProvisioningCloudifyAdapter.setElasticGridServiceAgentProvisioningProgressEventListener(agentEventListener);
 		
 		ElasticMachineProvisioningProgressChangedEventListener machineEventListener = new ElasticMachineProvisioningProgressChangedEventListener() {
 
 			@Override
 			public void elasticMachineProvisioningProgressChanged(
 					ElasticMachineProvisioningProgressChangedEvent event) {
 				LogUtils.log(event.toString());
 			}
 		};
 		elasticMachineProvisioningCloudifyAdapter.setElasticMachineProvisioningProgressChangedEventListener(machineEventListener);
 	}
 	
 	protected void bootstrapBeforeClass() throws Exception {
 		super.bootstrap();
 	}
 
     public void beforeTestInit() {
         try {
             initElasticMachineProvisioningCloudifyAdapter();
         }
         catch (Exception e){
            AssertUtils.assertFail("Init to Elastic machine provisioning cloudify adapter failed: " + e.getCause(),e);
         }
         agentEventListener = new ElasticGridServiceAgentProvisioningProgressChangedEventListener() {
 
             @Override
             public void elasticGridServiceAgentProvisioningProgressChanged(
                     ElasticGridServiceAgentProvisioningProgressChangedEvent event) {
                 LogUtils.log(event.toString());
             }
         };
 
          machineEventListener = new ElasticMachineProvisioningProgressChangedEventListener() {
 
             @Override
             public void elasticMachineProvisioningProgressChanged(
                     ElasticMachineProvisioningProgressChangedEvent event) {
                 LogUtils.log(event.toString());
             }
         };
 
 		gscCounter = new GridServiceContainersCounter(admin); 
         gsaCounter = new GridServiceAgentsCounter(admin);
         machineEventsCounter = new MachinesEventsCounter(admin);
 		repetitiveAssertNumberOfMachineEvents(MachineStartRequestedEvent.class, 0, OPERATION_TIMEOUT);
         repetitiveAssertNumberOfMachineEvents(MachineStartedEvent.class, 0, OPERATION_TIMEOUT);
         repetitiveAssertNumberOfMachineEvents(MachineStopRequestedEvent.class, 0, OPERATION_TIMEOUT);
         repetitiveAssertNumberOfMachineEvents(MachineStoppedEvent.class, 0, OPERATION_TIMEOUT);
 	}
     
     //assuming first machine is management
     public double getFirstManagementMachinePhysicalMemoryInMB() {
     	return firstManagementMachine.getOperatingSystem().getDetails().getTotalPhysicalMemorySizeInMB();
     }
     
     /**
      * Stops all machines except management machines , admin is necessary to apply this method.
      * @throws Exception
      */
     public void stopMachines() throws Exception {	
 		GridServiceAgent[] gsas = admin.getGridServiceAgents().getAgents();
 		if (admin.getGridServiceManagers().getManagers().length == 0){
             AssertUtils.assertFail("NO GSMS!");
 		}
         for (int i = 0; i < gsas.length; i++) {
             Machine curMachine = gsas[i].getMachine();
             if (!curMachine.equals(firstManagementMachine)) {
                 stopByonMachine(getElasticMachineProvisioningCloudifyAdapter(), gsas[i], OPERATION_TIMEOUT, TimeUnit.MILLISECONDS);
             }
         }
 	}
 	
     public void afterTest() {
     	if (gscCounter != null) {
         	gscCounter.close();
         }
         if (gsaCounter != null) {
         	gsaCounter.close();
         }
         if (!ENABLE_LOGSTASH) {
             DumpUtils.dumpLogs(admin);
         }
         // undeploy all zombie pu's
         List<ProcessingUnit> processingUnits = new ArrayList<ProcessingUnit>(Arrays.asList(admin.getProcessingUnits().getProcessingUnits()));
 		final Iterator<ProcessingUnit> iterator = processingUnits.iterator();
 		while (iterator.hasNext()) {
 		    if (iterator.next().getName().equals(CloudifyConstants.MANAGEMENT_SPACE_NAME)) {
 		        iterator.remove();
 		    }
 		}
         if (processingUnits.size() > 0) {
         	LogUtils.log(this.getClass() + " test has not undeployed all processing units !!!");
         }
         for (ProcessingUnit pu : processingUnits) {
         	//cleanup
         	boolean undeploySucceeded = pu.undeployAndWait(OPERATION_TIMEOUT, TimeUnit.MILLISECONDS);
         	if (undeploySucceeded) {
         	    LogUtils.log(this.getClass() + " afterTest undeployed " + pu.getName());
         	}
         	else {
         	    LogUtils.log(this.getClass() + " afterTest failed to undeploy " + pu.getName());
         	}
         }
         // stoping all machines and ProvisioningCloudifyAdapter
         try {
             LogUtils.log("releasing all machines except the first management machine");
 			stopMachines();
 		} catch (Exception e) {
 			LogUtils.log(this.getClass() + "stopMachines after test failed");
 			e.printStackTrace();
 		}
         LogUtils.log("Machines were released");
         try {
             elasticMachineProvisioningCloudifyAdapter.destroy();
         }
         catch (Exception e) {
             AssertUtils.assertFail("destroying elasticMachineProvisioningCloudifyAdapter failed: "+e.getCause());
         }
         
 	}
 
 
 	protected void teardownAfterClass() throws Exception {
 		super.teardown(admin);
 	}
 
 	@Override
 	protected String getCloudName() {
 		return "byon-xap";
 	}
 	
 	protected ElasticMachineProvisioningCloudifyAdapter getElasticMachineProvisioningCloudifyAdapter() {
 		return elasticMachineProvisioningCloudifyAdapter;
 	}
 	
 	@Override
 	protected void afterBootstrap() throws Exception {
 		admin = super.createAdmin();
         admin.getGridServiceManagers().waitFor(1);
         firstManagementMachine = admin.getGridServiceManagers().getManagers()[0].getMachine();
     }
 	
 	protected DiscoveredMachineProvisioningConfig getDiscoveredMachineProvisioningConfig() {
 		DiscoveredMachineProvisioningConfig config = new DiscoveredMachineProvisioningConfig();
 		config.setReservedMemoryCapacityPerMachineInMB(RESERVED_MEMORY_PER_MACHINE_MEGABYTES_DISCOVERED);
 		//config.setReservedMemoryCapacityPerManagementMachineInMB(RESERVED_MEMORY_PER_MACHINE_MEGABYTES_DISCOVERED);
 		return config;
 	}
 	
 	protected void customizeCloud() throws Exception {
 		String oldMemory = "machineMemoryMB " + STANDARD_MACHINE_MEMORY_MB;
 		String newMemory = "machineMemoryMB " + DEFAULT_BYON_XAP_MACHINE_MEMORY_MB;
 		String numOfCores = "numberOfCores "+NUM_OF_CORES;
 		// sets number of cores to 2 - can be modified
 		getService().getAdditionalPropsToReplace().put(oldMemory, newMemory +"\n"+numOfCores);					
 	}
 
 	protected void assertUndeployAndWait(ProcessingUnit pu) {
 		LogUtils.log("Undeploying processing unit " + pu.getName());
 		boolean success = pu.undeployAndWait(OPERATION_TIMEOUT, TimeUnit.MILLISECONDS);
 		AssertUtils.assertTrue("Undeployment of "+pu.getName()+"failed",success);
 		LogUtils.log("Undeployed processing unit " + pu.getName());		
 	}
 	
 	protected CloudifyMachineProvisioningConfig getMachineProvisioningConfig() {
 		String templateName = "SMALL_LINUX";
 		ByonCloudService cloudService = getService();
 		Cloud cloud = cloudService.getCloud();
 		final ComputeTemplate template = cloud.getCloudCompute().getTemplates().get(templateName);			
 		ComputeTemplate managementTemplate = cloud.getCloudCompute().getTemplates().get(cloud.getConfiguration().getManagementMachineTemplate());
 		managementTemplate.getRemoteDirectory();
 		return  new CloudifyMachineProvisioningConfig(
 				cloud, template, templateName,
 				managementTemplate.getRemoteDirectory(), null);
 	}
 
     protected CloudifyMachineProvisioningConfig getMachineProvisioningConfigDedicatedManagement() {
         CloudifyMachineProvisioningConfig machineProvisioningConfig = getMachineProvisioningConfig();
         machineProvisioningConfig.setDedicatedManagementMachines(true);
         return machineProvisioningConfig;
     }
 
 	protected CloudifyMachineProvisioningConfig getMachineProvisioningConfigWithMachineZone(
 			String[] machineZones) {
 		if (machineZones.length == 0) {
 			throw new IllegalArgumentException("no machine zones");
 		}
 		CloudifyMachineProvisioningConfig config = getMachineProvisioningConfig();
 		config.setGridServiceAgentZones(machineZones);
 		//config.setGridServiceAgentZoneMandatory(true);
 		return config;
 	}
 
     protected ProcessingUnit deploy(ElasticStatefulProcessingUnitDeployment deployment) {
 		return admin.getGridServiceManagers().getManagers()[0].deploy(deployment.addCommandLineArgument("-Dcom.gs.transport_protocol.lrmi.bind-port="+LRMI_BIND_PORT_RANGE));
 	} 
 
 
 	protected ProcessingUnit deploy(ElasticStatelessProcessingUnitDeployment deployment) {
 		return admin.getGridServiceManagers().getManagers()[0].deploy(deployment.addCommandLineArgument("-Dcom.gs.transport_protocol.lrmi.bind-port="+LRMI_BIND_PORT_RANGE));
 	} 
 	
 	protected ProcessingUnit deploy(ElasticSpaceDeployment deployment) {
 		return admin.getGridServiceManagers().getManagers()[0].deploy(deployment.addCommandLineArgument("-Dcom.gs.transport_protocol.lrmi.bind-port="+LRMI_BIND_PORT_RANGE));
 	}
 
 	public ProcessingUnit deploy(SpaceDeployment deployment) {
 		return admin.getGridServiceManagers().getManagers()[0].deploy(deployment);
 	} 
 	
 
     protected void restartEsmAndWait() throws InterruptedException {
         final ElasticServiceManagers elasticServiceManagers = admin.getElasticServiceManagers();
         final ElasticServiceManager esm = elasticServiceManagers.waitForAtLeastOne(OPERATION_TIMEOUT, TimeUnit.MILLISECONDS);
         assertNotNull("Failed discovering esm", esm);
         final CountDownLatch newEsmStarted = new CountDownLatch(1);
         ElasticServiceManagerAddedEventListener eventListener = new ElasticServiceManagerAddedEventListener() {
             @Override
             public void elasticServiceManagerAdded(
                     ElasticServiceManager addedEsm) {
                 if (!addedEsm.equals(esm)) {
                     newEsmStarted.countDown();
                 }
                 
             }
         };
         elasticServiceManagers.getElasticServiceManagerAdded().add(eventListener);
         try {
             killGridComponent(esm);
             boolean latchZero = newEsmStarted.await(OPERATION_TIMEOUT, TimeUnit.MILLISECONDS);
             assertTrue("Timedout waiting for new ESM to start", latchZero);
         }
         finally {
             elasticServiceManagers.getElasticServiceManagerAdded().remove(eventListener);
         }
     }
 
     protected void killEsm() {
         final ElasticServiceManagers elasticServiceManagers = admin.getElasticServiceManagers();
         final ElasticServiceManager esm = elasticServiceManagers.waitForAtLeastOne(OPERATION_TIMEOUT, TimeUnit.MILLISECONDS);
         assertNotNull("Failed discovering esm", esm);
         killGridComponent(esm);
     }
     private void killGridComponent(final GridComponent component) {
         final String ipAddress = component.getMachine().getHostAddress();
         final int pid = (int) component.getVirtualMachine().getDetails().getPid();
         final boolean killed = SSHUtils.killProcess(ipAddress, pid);
         assertTrue("Failed killing " + component.getClass().getSimpleName() + " " + component.getUid(), killed);
     }
 }
