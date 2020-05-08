 package framework.utils.xen;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.TimeoutException;
 
 import junit.framework.Assert;
 
 import org.cloudifysource.dsl.internal.CloudifyConstants;
 import org.openspaces.admin.Admin;
 import org.openspaces.admin.AdminException;
 import org.openspaces.admin.AdminFactory;
 import org.openspaces.admin.gsa.GridServiceAgent;
 import org.openspaces.admin.gsm.GridServiceManager;
 import org.openspaces.admin.internal.InternalAdminFactory;
 import org.openspaces.admin.lus.LookupService;
 import org.openspaces.admin.machine.Machine;
 import org.openspaces.admin.pu.elastic.config.DiscoveredMachineProvisioningConfig;
 import org.openspaces.cloud.xenserver.XenServerException;
 import org.openspaces.cloud.xenserver.XenServerMachineProvisioningConfig;
 import org.openspaces.cloud.xenserver.XenUtils;
 import org.openspaces.grid.gsm.machines.MachinesSlaEnforcement;
 import org.openspaces.grid.gsm.machines.plugins.ElasticMachineProvisioningException;
 import org.testng.annotations.AfterMethod;
 import org.testng.annotations.BeforeMethod;
 
 import test.AbstractTest;
 
 import com.j_spaces.kernel.PlatformVersion;
 
 import framework.tools.SGTestHelper;
 import framework.utils.AdminUtils;
 import framework.utils.DumpUtils;
 import framework.utils.GridServiceAgentsCounter;
 import framework.utils.GridServiceContainersCounter;
 import framework.utils.LogUtils;
 import framework.utils.ScriptUtils;
 import framework.utils.TeardownUtils;
 
 
 public class AbstractXenGSMTest extends AbstractTest {
 
     protected static final int XENSERVER_ROOT_DRIVE_CAPACITY = 3967;
     protected static final int RESERVED_DRIVE_CAPACITY_PER_MACHINE_MEGABYTES = 2200;
 	protected static final String XENSERVER_ELASTIC_SCALE_HANDLER_CLASS = MachinesSlaEnforcement.class.getCanonicalName();
 	private static final long RESERVED_MEMORY_PER_MACHINE_MEGABYTES = 128;
 	
     private XenServerMachineProvisioningConfig machineProvisioningConfig;
     protected Admin admin;
     protected GridServiceManager gsm;
     private String xenServerMasterMachineLabelPrefix = null;
     private String zone = "testzone";
 	private GridServiceContainersCounter gscCounter;
     private GridServiceAgentsCounter gsaCounter;
 	private boolean acceptGSCsOnStartup = false;
 	private int lookupPort = CloudifyConstants.DEFAULT_LUS_PORT;
 	private String edition = PlatformVersion.getEdition();
     
 	protected void setEdition(String edition) {
 		this.edition = edition;
 	}
 	
 	protected void setZone(String zone) {
 		this.zone= zone;
 	}
 	
 	protected String getZone() {
 		return this.zone;
 	}
 	
 	protected DiscoveredMachineProvisioningConfig getDiscoveredMachineProvisioningConfig() {
 		DiscoveredMachineProvisioningConfig config = new DiscoveredMachineProvisioningConfig();
 		config.setReservedMemoryCapacityPerMachineInMB(RESERVED_MEMORY_PER_MACHINE_MEGABYTES);
 		return config;
 	}
 	
 	protected XenServerMachineProvisioningConfig getMachineProvisioningConfig() {
 		XenServerMachineProvisioningConfig config = new XenServerMachineProvisioningConfig(new HashMap<String,String>(machineProvisioningConfig.getProperties()));
 		config.setReservedMemoryCapacityPerMachineInMB(RESERVED_MEMORY_PER_MACHINE_MEGABYTES);
 		Map<String,Long> reservedDriveCapacity = new HashMap<String,Long>();
 		reservedDriveCapacity.put("/", (long)RESERVED_DRIVE_CAPACITY_PER_MACHINE_MEGABYTES);
 		config.setReservedDriveCapacityPerMachineInMB(reservedDriveCapacity);
 		return config;
 	}
 	
 	protected XenServerMachineProvisioningConfig getMachineProvisioningConfigWithMachineZone(String[] machineZones) {
 		if (machineZones.length == 0) {
 			throw new IllegalArgumentException("no machine zones");
 		}
 		XenServerMachineProvisioningConfig config = new XenServerMachineProvisioningConfig(new HashMap<String,String>(machineProvisioningConfig.getProperties()));
 		config.setReservedMemoryCapacityPerMachineInMB(RESERVED_MEMORY_PER_MACHINE_MEGABYTES);
 		config.setGridServiceAgentZones(machineZones);
 		config.setGridServiceAgentZoneMandatory(true);
 		return config;
 	}
 	
     @Override
     @BeforeMethod
     public void beforeTest() {
                 
         loadXenServerProperties();
         
         assertCleanVMSetup();
         
         prepareXapInstallation();
         
         createMasterVMImage();
         
         startFirstVM();
         
         admin.getGridServiceAgents().waitFor(1);
         admin.getGridServiceManagers().waitFor(1);
         admin.getElasticServiceManagers().waitFor(1);
         
         gscCounter = new GridServiceContainersCounter(admin); 
         gsaCounter = new GridServiceAgentsCounter(admin);
         
         if (!acceptGSCsOnStartup) {
     	    assertEquals(0, gscCounter.getNumberOfGSCsAdded());
         }
         
 		while (gsaCounter.getNumberOfGSAsAdded() == 0) {
 			try {
 				Thread.sleep(1000);
 			} catch (InterruptedException e) {
 				Assert.fail(e.getMessage());
 			}
 		}
 	    assertEquals(1, gsaCounter.getNumberOfGSAsAdded());
     }
     
     protected void loadXenServerProperties() {
         File root = null;
         String pathname = SGTestHelper.getSGTestRootDir() + "/lib/xenserver";
         try {
             root = new File(pathname).getCanonicalFile();
         } catch (IOException e1) {
             AssertFail("Cannot resolve " + new File(pathname).getAbsolutePath() +" directory");
         }
         
         File path = new File(root, "conf/xenserver.properties");
         try {
             machineProvisioningConfig = new XenServerMachineProvisioningConfig(
                     ScriptUtils.loadPropertiesFromFile(path));
         } catch (IOException e) {
             AssertFail("failed reading " + path, e);
         }
         
         // fix local path
         machineProvisioningConfig.setFileLocalDirectory(root.getAbsolutePath());
         
         // keep master vm label prefix for removing old master VMs later on
         xenServerMasterMachineLabelPrefix = machineProvisioningConfig.getMasterMachineNameLabel();
         
         // Cloudify or XAP
         machineProvisioningConfig.setMasterMachineNameLabel(getXenServerMasterMachineNameLabel());
         
         machineProvisioningConfig.setDriveCapacityPerMachineInMB(XENSERVER_ROOT_DRIVE_CAPACITY);
         
         overrideXenServerProperties(machineProvisioningConfig);
         
     }
 
 	private String getXenServerMasterMachineNameLabel() {
 		final String xenServerMasterMachineNameLabel = xenServerMasterMachineLabelPrefix + "_"+this.edition.replace(' ', '_')+"_" + PlatformVersion.getBuildNumber();
 		return xenServerMasterMachineNameLabel;
 	}
     
     protected Map<String, String> loadXenServerMappingProperties() {
         File root = null;
         String pathname = SGTestHelper.getSGTestRootDir() + "/lib/xenserver";
         try {
             root = new File(pathname).getCanonicalFile();
         } catch (IOException e1) {
             AssertFail("Cannot resolve " + new File(pathname).getAbsolutePath() +" directory");
         }
         
         File path = new File(root, "conf/xenserver-mapping.properties");
         try {
             return ScriptUtils.loadPropertiesFromFile(path);
         } catch (IOException e) {
             AssertFail("failed reading " + path, e);
             return null;
         }
     }
     
 	protected void overrideXenServerProperties(XenServerMachineProvisioningConfig machineProvisioningConfig) {
 		
 	}
 
 	private void assertCleanVMSetup() {
         
         try {
             
             List<String> machineLabels = getAllMachineNameLabels();
             
             for (String label : machineLabels) {
                 if (label.startsWith(xenServerMasterMachineLabelPrefix) &&
                     !label.equals(getXenServerMasterMachineNameLabel())) {
                 	XenUtils.hardShutdownMachinesByLabelStartsWith(machineProvisioningConfig, label, 5*60, TimeUnit.SECONDS);
                 }
             }
         
             teardownAllVMs();
             
             try {
             	XenUtils.cleanStaleVDIs(getMachineProvisioningConfig(), OPERATION_TIMEOUT, TimeUnit.MILLISECONDS);
             }
             catch (XenServerException e) {
             	LogUtils.log("Could not clean unused virtual disk drive. Please perform manual cleanup",e);
             }
         
         } catch (InterruptedException e) {
             AssertFail("Failed setting clean setup", e);
         } catch (ElasticMachineProvisioningException e) {
             AssertFail("Failed setting clean setup", e);
         } catch (TimeoutException e) {
         	AssertFail("Failed setting clean setup", e);
 		}
         
     }
 
 	/**
 	 * Cleanup has a hard time accessing the xen for the first time still it's still working on the shutdown of prev test machines.
 	 * That's why there is a retry mechanism here to improve reliability.
 	 */
 	private List<String> getAllMachineNameLabels() throws XenServerException, InterruptedException {
 		int retryCount = 3;
 		final long sleepBetweenRetriesMilliseconds = 5000;
 		while (true) {
 			try {
 				List<String> machineLabels = XenUtils.getAllMachineLabels(machineProvisioningConfig);
 				return machineLabels;
 			}
 			catch(XenServerException e) {
 				if (retryCount == 0) {
 					throw e;
 				}
 				retryCount--;
 				LogUtils.log("Failed retrieving list of xen machines", e);
 				Thread.sleep(sleepBetweenRetriesMilliseconds);
 			}
 		}
 		
 	}
     
     private void prepareXapInstallation() {
         File gigaspacesZipFile = null;
         try {
             gigaspacesZipFile = ScriptUtils.getGigaspacesZipFile();
         } catch (IOException e) {
             AssertFail("failed to get gigaspaces zip file",e); 
         }
         try {
             machineProvisioningConfig.setFileXapLocalLocation(gigaspacesZipFile.getCanonicalPath());
         } catch (IOException e) {
             AssertFail("failed reading " + gigaspacesZipFile.getAbsolutePath(), e);
         }
     }
     
     private void createMasterVMImage() {
         try {
             XenUtils.createMasterVirtualMachineImage(machineProvisioningConfig, 15*60, TimeUnit.SECONDS);
         } catch (InterruptedException e) {
             Thread.interrupted();
             AssertFail("Failed creating master VM",e);
         } catch (ElasticMachineProvisioningException e) {
             AssertFail("Failed creating master VM",e);
         } catch (TimeoutException e) {
         	AssertFail("Failed creating master VM",e);
 		}
     }
     
     private void startFirstVM() {
     	String group = AdminUtils.getTestGroups()+"-xenserver"+new Random().nextLong();
 		machineProvisioningConfig.setXapGroups(new String[]{group});
 		machineProvisioningConfig.setLookupServicePort(lookupPort);
 		try {
            GridServiceAgent gsa = XenUtils.startFirstVirtualMachine(machineProvisioningConfig.getProperties(), 15 * 60, TimeUnit.SECONDS);
 
             // we replace the admin with a single threaded admin needed for test
             String[] locators = machineProvisioningConfig.getXapLocators();
             if (locators.length == 0) {
                 locators = new String[] { gsa.getMachine().getHostAddress() + ":" + lookupPort };
             }
             
             gsa.getAdmin().close();
             System.setProperty("com.gs.multicast.enabled", "false");
             
             AdminFactory adminFactory = new InternalAdminFactory()
                 .singleThreadedEventListeners()
                 .useDaemonThreads(true)
                 .addGroup(group);
             
             for (String locator : locators) {
                 adminFactory.addLocator(locator);
             }
             
             admin = adminFactory.createAdmin();
 
             gsm = admin.getGridServiceManagers().waitForAtLeastOne();
             
             if (admin.getMachines().getSize() > 1) {
             	Machine[] machines = admin.getMachines().getMachines();
             	List<String> machineNames = new ArrayList<String>(); 
             	for (Machine machine : machines) {
             		machineNames.add(machine.getHostName()+"("+machine.getHostAddress()+")");
             	}
             	Assert.fail("Expected only a single machine, instead found " + Arrays.toString(machineNames.toArray()));
             }
         } catch (ElasticMachineProvisioningException e) {
             AssertFail("Failed starting first VM",e);
         } catch (InterruptedException e) {
             AssertFail("Failed starting first VM",e);
         } catch (TimeoutException e) {
         	AssertFail("Failed starting first VM",e);
 		}
     }
 
     public GridServiceAgent[] startNewVMs(int numberOfVms, final long duration, final TimeUnit unit) {
 
         GridServiceAgent[] result = new GridServiceAgent[numberOfVms];
 
         ExecutorService  service = Executors.newFixedThreadPool(numberOfVms);
         
         List<Callable<GridServiceAgent>> tasks = new ArrayList<Callable<GridServiceAgent>>();
         for (int i=0; i<numberOfVms; i++) {
             tasks.add(new Callable<GridServiceAgent>() {
                 public GridServiceAgent call() {
                     return startNewVM(duration, unit);
                 }
             });
         }
 
         List<Future<GridServiceAgent>> futureAgents = new ArrayList<Future<GridServiceAgent>>();
         
         for (Callable<GridServiceAgent> task : tasks) {
             Future<GridServiceAgent> future = service.submit(task);
             futureAgents.add(future);
             // we have to wait ~10sec because XenServer doesn't handle this all at once
             int xenServerDelayBetweenClones = 10 * 1000;
             try {
                 Thread.sleep(xenServerDelayBetweenClones);
             } catch (InterruptedException e) {
                 AssertFail("Failed starting new VMs", e);
             } finally {
                 service.shutdown();
             }
         }
         
         try {
             int i = 0;
             for (Future<GridServiceAgent> future : futureAgents) {
                 result[i] = future.get(duration, unit);
                 i += 1;
             }
 
         } catch (InterruptedException e) {
             AssertFail("Failed starting new VMs", e);
         } catch (ExecutionException e) {
             AssertFail("Failed starting new VMs", e);
         } catch (TimeoutException e) {
             AssertFail("Failed starting new VMs", e);
         } finally {
             service.shutdown();
         }
         
         return result;
     }
     
     /* starts a new Xen VM with current memory/cpu configuration */
     public GridServiceAgent startNewVM(long duration, TimeUnit unit) {
         return startNewVM(0,0, duration, unit);
     }
 
     public GridServiceAgent startNewVM(int numberOfCPUs, int memoryCapacityInMB, long duration, TimeUnit unit) {
     	return startNewVM(numberOfCPUs, memoryCapacityInMB, getMachineProvisioningConfig(),duration, unit); 
     }
     
     /* Starts a new Xen VM */
     public GridServiceAgent startNewVM(int numberOfCPUs, int memoryCapacityInMB, XenServerMachineProvisioningConfig machineProvisioningConfig, long duration, TimeUnit unit) {
 
         GridServiceAgent newGsa = null;
         
         XenServerMachineProvisioningConfig newMachineProvisioningConfig;
         Map<String, String> properties = new HashMap<String, String>(machineProvisioningConfig.getProperties());
         newMachineProvisioningConfig = new XenServerMachineProvisioningConfig(properties);
         
         if (numberOfCPUs > 0) {
             newMachineProvisioningConfig.setNumberOfCpuCores(numberOfCPUs);
         }
         
         if (memoryCapacityInMB > 0) {
             newMachineProvisioningConfig.setMemoryCapacityPerMachineInMB(memoryCapacityInMB);
         }
         
         try {
             newGsa = XenUtils.startMachine(newMachineProvisioningConfig, admin, duration, unit);
         } catch (ElasticMachineProvisioningException e) {
             AssertFail("Failed starting new VM",e);
         } catch (InterruptedException e) {
             AssertFail("Failed starting new VM",e);
         } catch (TimeoutException e) {
             AssertFail("Failed starting new VM",e);
         }
         
         return newGsa;
     }
     
 	@Override
     @AfterMethod
     public void afterTest() {
         if (gscCounter != null) {
         	gscCounter.close();
         }
         if (gsaCounter != null) {
         	gsaCounter.close();
         }
         if (admin != null) {
 	        try {
 	        	TeardownUtils.snapshot(admin);
 	        } catch(Throwable t) {
 	        	LogUtils.log("failed on snapshot", t);
 	        }
 	        try {
 	            DumpUtils.dumpLogs(admin);
 	        } catch (Throwable t) {
 	        	LogUtils.log("failed to dump logs", t);
 	        }
         }
         
         teardownAllVMs();
     }
     
     protected void teardownAllVMs() {
         
         try {
             // make sure esm doesn't try to start a new VM
             if (gsm != null) {
                 LookupService[] lookupServices = gsm.getAdmin().getLookupServices().getLookupServices();
                 if (lookupServices.length > 0 && lookupServices[0].getMachine().getGridServiceAgents().waitFor(1,1,TimeUnit.SECONDS)) {
                     lookupServices[0].kill();
                 }
             }
         }
         catch (AdminException e) {
         	LogUtils.log("Failed killing lookup service.");
         }
         finally {
             gsm = null;
         }
         
         try {
             if (admin != null) {
                 admin.close();
             }
         }
         catch (AdminException e) {
         	LogUtils.log("Failed closing admin");
         }
         finally {
             admin = null;
         }
         
         String label = machineProvisioningConfig.getStartMachineNameLabel();
         try {
             while (XenUtils.getNumberOfMachinesByLabel(machineProvisioningConfig, label) > 0) {
                 XenUtils.hardShutdownMachinesByLabelStartsWith(machineProvisioningConfig, label, 60*10, TimeUnit.SECONDS);
                 Thread.sleep(1000);
             }
         } 
         catch (InterruptedException e) {
             Thread.interrupted();
             LogUtils.log("Failed removing VMs with label: " + label, e);
         } 
         catch (ElasticMachineProvisioningException e) {
         	LogUtils.log("Failed removing VMs with label: " + label, e);
         } 
         catch (TimeoutException e) {
         	LogUtils.log("Failed removing VMs with label: " + label, e);
 		} 
     }
 
     public void repetitiveAssertNumberOfGSAsAdded(int expected, long timeoutMilliseconds) {
     	gsaCounter.repetitiveAssertNumberOfGridServiceAgentsAdded(expected, timeoutMilliseconds);
     }
     
     public void repetitiveAssertNumberOfGSAsRemoved(int expected, long timeoutMilliseconds) {
     	gsaCounter.repetitiveAssertNumberOfGridServiceAgentsRemoved(expected, timeoutMilliseconds);
     }
     
     public void repetitiveAssertNumberOfGSCsAdded(int expected, long timeoutMilliseconds) {
     	gscCounter.repetitiveAssertNumberOfGridServiceContainersAdded(expected, timeoutMilliseconds);
     }
     
     public void repetitiveAssertNumberOfGSCsRemoved(int expected, long timeoutMilliseconds) {
     	gscCounter.repetitiveAssertNumberOfGridServiceContainersRemoved(expected, timeoutMilliseconds);
     }
     
 	public int countMachines() {
 		try {
 			return XenUtils.getAllMachinesByNameLabelStartsWith(machineProvisioningConfig, machineProvisioningConfig.getStartMachineNameLabel()).size();
 		} catch (XenServerException e) {
 			AssertFail("Failed to count number of running machines",e);
 			return -1;
 		}
 	}
 
     public void setAcceptGSCsOnStartup(boolean acceptGSCsOnStartup) {
         this.acceptGSCsOnStartup = acceptGSCsOnStartup;
     }
 
 	public void setLookupServicePort(int lookupServicePort) {
 		this.lookupPort = lookupServicePort;
 	}
 
 }
