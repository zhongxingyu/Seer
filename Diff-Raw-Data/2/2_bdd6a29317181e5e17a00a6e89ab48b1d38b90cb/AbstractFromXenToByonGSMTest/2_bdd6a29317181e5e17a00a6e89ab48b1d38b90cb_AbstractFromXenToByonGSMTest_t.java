 package test.esm;
 
 import java.util.concurrent.TimeUnit;
 
 import org.cloudifysource.dsl.cloud.Cloud;
 import org.cloudifysource.dsl.cloud.CloudTemplate;
 import org.cloudifysource.esc.driver.provisioning.CloudifyMachineProvisioningConfig;
 import org.cloudifysource.esc.driver.provisioning.ElasticMachineProvisioningCloudifyAdapter;
 import org.openspaces.admin.gsa.GridServiceAgent;
 import org.openspaces.admin.gsa.events.ElasticGridServiceAgentProvisioningProgressChangedEvent;
 import org.openspaces.admin.gsa.events.ElasticGridServiceAgentProvisioningProgressChangedEventListener;
 import org.openspaces.admin.machine.Machine;
 import org.openspaces.admin.machine.events.ElasticMachineProvisioningProgressChangedEvent;
 import org.openspaces.admin.machine.events.ElasticMachineProvisioningProgressChangedEventListener;
 import org.openspaces.admin.pu.ProcessingUnit;
 import org.openspaces.admin.pu.ProcessingUnits;
 import org.openspaces.admin.pu.elastic.ElasticStatefulProcessingUnitDeployment;
 import org.openspaces.admin.pu.elastic.ElasticStatelessProcessingUnitDeployment;
 import org.openspaces.admin.pu.elastic.config.DiscoveredMachineProvisioningConfig;
 import org.openspaces.admin.space.ElasticSpaceDeployment;
 import org.openspaces.admin.space.SpaceDeployment;
 import org.openspaces.grid.gsm.machines.plugins.events.MachineStartRequestedEvent;
 import org.openspaces.grid.gsm.machines.plugins.events.MachineStartedEvent;
 import org.openspaces.grid.gsm.machines.plugins.events.MachineStopRequestedEvent;
 import org.openspaces.grid.gsm.machines.plugins.events.MachineStoppedEvent;
 
 import test.cli.cloudify.cloud.byon.AbstractByonCloudTest;
 import test.cli.cloudify.cloud.services.byon.ByonCloudService;
 import framework.utils.AssertUtils;
 import framework.utils.ByonMachinesUtils;
 import framework.utils.GridServiceAgentsCounter;
 import framework.utils.GridServiceContainersCounter;
 import framework.utils.LogUtils;
 
 public class AbstractFromXenToByonGSMTest extends AbstractByonCloudTest {
 	
 	public final static long OPERATION_TIMEOUT = 5 * 60 * 1000;
 	public final static String DEFAULT_BYON_XAP_MACHINE_MEMORY_MB = "5000";
	public final static String STANDARD_MACHINE_MEMORY_MB = "5850";
 	public final static int NUM_OF_CORES = 2;
 	private static final long RESERVED_MEMORY_PER_MACHINE_MEGABYTES_DISCOVERED = 128;
 	private MachinesEventsCounter machineEventsCounter;
 	private ElasticMachineProvisioningCloudifyAdapter elasticMachineProvisioningCloudifyAdapter;
 	private GridServiceContainersCounter gscCounter;
     private GridServiceAgentsCounter gsaCounter;
 	
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
     
     protected GridServiceAgent[] startNewByonMachines(
 			final ElasticMachineProvisioningCloudifyAdapter elasticMachineProvisioningCloudifyAdapter,
 			int numOfMachines, final long duration,final TimeUnit timeUnit) {
     	return ByonMachinesUtils.startNewByonMachines(elasticMachineProvisioningCloudifyAdapter, numOfMachines, duration, timeUnit);
     }
     
     protected static boolean stopByonMachine (ElasticMachineProvisioningCloudifyAdapter elasticMachineProvisioningCloudifyAdapter, GridServiceAgent agent ,long duration,TimeUnit timeUnit) throws Exception {
     	return ByonMachinesUtils.stopByonMachine(elasticMachineProvisioningCloudifyAdapter, agent, duration, timeUnit);
     }
     
     private void initElasticMachineProvisioningCloudifyAdapter () throws Exception {
 	
 		elasticMachineProvisioningCloudifyAdapter.setAdmin(admin);
 		//sets cloudify configuration directory - so the ServiceReader would be able to read the groovy file
 		//the path should be to the DIRECTORY of the groovy file
 		CloudifyMachineProvisioningConfig config = getMachineProvisioningConfig();
 		config.setCloudConfigurationDirectory(getService().getPathToCloudFolder());
 		elasticMachineProvisioningCloudifyAdapter.setProperties(config.getProperties());		
 		elasticMachineProvisioningCloudifyAdapter.afterPropertiesSet();
 		
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
 		elasticMachineProvisioningCloudifyAdapter = new ElasticMachineProvisioningCloudifyAdapter ();
 		super.bootstrap();
 	}
 	
     public void beforeTestInit() {
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
     	Machine managementMachine = admin.getMachines().getMachines()[0];
     	return managementMachine.getOperatingSystem().getDetails().getTotalPhysicalMemorySizeInMB();
     }
     
     /**
      * Stops all machines except management machines , admin is necessary to apply this method.
      * @throws Exception
      */
     public void stopMachines() throws Exception {
 		GridServiceAgent[] gsas = admin.getGridServiceAgents().getAgents();
         Machine managerMachine = admin.getGridServiceManagers().getManagers()[0].getMachine();
         for (int i = 0; i < gsas.length; i++) {
             Machine curMachine = gsas[i].getMachine();
             if (!curMachine.equals(managerMachine)) {
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
         ProcessingUnits processingUnits = admin.getProcessingUnits();
 		if (processingUnits.getSize() > 0) {
         	LogUtils.log(this.getClass() + " test has not undeployed all processing units !!!");
         }
         for (ProcessingUnit pu : processingUnits) {
         	//cleanup
         	if (!pu.undeployAndWait(OPERATION_TIMEOUT,TimeUnit.MILLISECONDS)) {
         		LogUtils.log(this.getClass() + "#afterTest() failed to undeploy " + pu.getName());
         	}
         }
 	}
 	
 	protected void teardownAfterClass() throws Exception {
 		elasticMachineProvisioningCloudifyAdapter.destroy();
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
 		initElasticMachineProvisioningCloudifyAdapter();
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
 		final CloudTemplate template = cloud.getTemplates().get(templateName);			
 		CloudTemplate managementTemplate = cloud.getTemplates().get(cloud.getConfiguration().getManagementMachineTemplate());
 		managementTemplate.getRemoteDirectory();
 		final CloudifyMachineProvisioningConfig config = new CloudifyMachineProvisioningConfig(
 				cloud, template, templateName,
 				managementTemplate.getRemoteDirectory());
 		return config;
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
 		return admin.getGridServiceManagers().getManagers()[0].deploy(deployment);
 	} 
 	
 	protected ProcessingUnit deploy(ElasticStatelessProcessingUnitDeployment deployment) {
 		return admin.getGridServiceManagers().getManagers()[0].deploy(deployment);
 	} 
 	
 	protected ProcessingUnit deploy(ElasticSpaceDeployment deployment) {
 		return admin.getGridServiceManagers().getManagers()[0].deploy(deployment);
 	}
 
 	public ProcessingUnit deploy(SpaceDeployment deployment) {
 		return admin.getGridServiceManagers().getManagers()[0].deploy(deployment);
 	} 
 	
 }
