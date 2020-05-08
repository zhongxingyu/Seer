 package framework.utils.xen;
 
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.atomic.AtomicLong;
 import java.util.concurrent.atomic.AtomicReference;
 
 import org.openspaces.admin.Admin;
 import org.openspaces.admin.esm.ElasticServiceManager;
 import org.openspaces.admin.esm.ElasticServiceManagers;
 import org.openspaces.admin.esm.events.ElasticServiceManagerRemovedEventListener;
 import org.openspaces.admin.gsa.GridServiceAgent;
 import org.openspaces.admin.gsc.GridServiceContainer;
 import org.openspaces.admin.gsc.GridServiceContainers;
 import org.openspaces.admin.gsc.events.GridServiceContainerRemovedEventListener;
 import org.openspaces.admin.gsm.GridServiceManager;
 import org.openspaces.admin.gsm.events.GridServiceManagerRemovedEventListener;
 import org.openspaces.admin.lus.LookupService;
 import org.openspaces.admin.lus.LookupServices;
 import org.openspaces.admin.lus.events.LookupServiceAddedEventListener;
 import org.openspaces.admin.lus.events.LookupServiceRemovedEventListener;
 import org.openspaces.admin.machine.Machine;
 import org.openspaces.admin.machine.events.MachineAddedEventListener;
 import org.openspaces.admin.machine.events.MachineRemovedEventListener;
 import org.openspaces.admin.pu.ProcessingUnit;
 import org.openspaces.admin.space.Space;
 import org.openspaces.cloud.xenserver.XenServerMachineProvisioningConfig;
 import org.openspaces.cloud.xenserver.XenUtils;
 import org.openspaces.core.GigaSpace;
import org.openspaces.grid.gsm.capacity.CapacityRequirementsPerAgent;
 import org.openspaces.grid.gsm.capacity.DriveCapacityRequirement;
 import org.openspaces.grid.gsm.capacity.MachineCapacityRequirements;
 import org.openspaces.grid.gsm.rebalancing.RebalancingUtils;
 import org.testng.Assert;
 
 import test.data.Person;
 
 import com.j_spaces.core.client.ReadModifiers;
 
 import framework.utils.AdminUtils;
 import framework.utils.AssertUtils;
import framework.utils.AssertUtils.RepetitiveConditionProvider;
 import framework.utils.LogUtils;
 import framework.utils.TeardownUtils;
 
 public class GsmTestUtils {
 
 	public static void waitForScaleToComplete(final ProcessingUnit pu,
 			final int expectedNumberOfContainers,
 			final int expectedNumberOfMachines,
 			long operationTimeout) {
 		
 		boolean validateCpuSla = true;
 		waitForScaleToComplete(pu, expectedNumberOfContainers, expectedNumberOfMachines, validateCpuSla, operationTimeout);
 	}
 	
 	public static void waitForScaleToCompleteIgnoreCpuSla(ProcessingUnit pu,
 			final int expectedNumberOfContainers,
 			final int expectedNumberOfMachines,
 			long operationTimeout) {
 		
 		boolean validateCpuSla = false;
 		waitForScaleToComplete(pu, expectedNumberOfContainers, expectedNumberOfMachines, validateCpuSla, operationTimeout);
 	}
 	
 	private static void waitForScaleToComplete(final ProcessingUnit pu,
 			final int expectedNumberOfContainers,
 			final int expectedNumberOfMachines,
 			final boolean validateCpuSla,
 			long durationInMilliseconds) {
 		
 		final RepetitiveConditionProvider condition = 
 		    newScaleConditionProvider(pu, expectedNumberOfContainers, expectedNumberOfMachines, validateCpuSla);
 		AssertUtils.repetitiveAssertTrue("Waiting for scale to complete",
 				condition, durationInMilliseconds);
 		LogUtils.log("Done waiting. Repetitive assert is true");
 	}
 
 	public static void killContainer(final GridServiceContainer container) {
 		if (container.isDiscovered()) {
 			final CountDownLatch latch = new CountDownLatch(1);
 			GridServiceContainerRemovedEventListener eventListener = new GridServiceContainerRemovedEventListener() {
 
 				public void gridServiceContainerRemoved(
 						GridServiceContainer gridServiceContainer) {
 					if (gridServiceContainer.equals(container)) {
 						latch.countDown();
 					}
 				}
 			};
 			GridServiceContainers containers = container.getAdmin()
 					.getGridServiceContainers();
 			containers.getGridServiceContainerRemoved().add(eventListener);
 			try {
 				LogUtils.log("Killing container " + container.getVirtualMachine().getDetails().getPid());
 				container.kill();
 				latch.await();
 			} catch (InterruptedException e) {
 				Assert.fail("Interrupted while killing container", e);
 			} finally {
 				containers.getGridServiceContainerRemoved().remove(eventListener);
 			}
 		}
 	}
 
 	public static LookupService restartLookupService(final LookupService lus) {
 		final CountDownLatch latch = new CountDownLatch(2);
 		final AtomicReference<LookupService> newLus = new AtomicReference<LookupService>();
 		LookupServiceRemovedEventListener removedEventListener = new LookupServiceRemovedEventListener() {
 
 			public void lookupServiceRemoved(LookupService lookupService) {
 				if (lookupService.equals(lus)) {
 					latch.countDown();
 				}
 			}
 		};
 		
 		LookupServiceAddedEventListener addedEventListener = new LookupServiceAddedEventListener() {
 
 			public void lookupServiceAdded(LookupService lookupService) {
 				newLus.set(lookupService);
 				latch.countDown();
 			}
 		};
 
 		LookupServices services = lus.getAdmin().getLookupServices();
 		services.getLookupServiceRemoved().add(removedEventListener);
 		services.getLookupServiceAdded().add(addedEventListener);
 		try {
 			lus.restart();
 			latch.await();
 		} catch (InterruptedException e) {
 			Assert.fail("Interrupted while killing lus", e);
 		} finally {
 			services.getLookupServiceRemoved().remove(removedEventListener);
 			services.getLookupServiceAdded().remove(addedEventListener);
 		}
 		return newLus.get();
 	}
 
 	public static void shutdownMachine(Machine machine, XenServerMachineProvisioningConfig xenConfig, long timeoutInMilliseconds) {
 	    shutdownMachine(machine,xenConfig,false,timeoutInMilliseconds);
 	}
 	
 	public static void hardShutdownMachine(Machine machine, XenServerMachineProvisioningConfig xenConfig, long timeoutInMilliseconds) {
         shutdownMachine(machine,xenConfig,true,timeoutInMilliseconds);
     }
 	
 	private static void shutdownMachine(Machine machine, XenServerMachineProvisioningConfig xenConfig, boolean hardShutdown, long timeoutInMilliseconds) {
 		Admin admin = machine.getGridServiceAgent().getAdmin();
 		final String ipAdresss = machine.getHostAddress();
 		final CountDownLatch latch = new CountDownLatch(1);
 			        	
 		final MachineRemovedEventListener eventListener = new MachineRemovedEventListener() {
 
 			public void machineRemoved(final Machine machine) {
 				if (machine.getHostAddress().equals(ipAdresss)) {
 					latch.countDown();
 				}
 				
 			}};
 			
 		admin.getMachines().getMachineRemoved().add(eventListener);
 		try {
 			if (hardShutdown) {
 			    XenUtils.hardShutdownMachineByIpAddress(xenConfig, ipAdresss, timeoutInMilliseconds, TimeUnit.MILLISECONDS);
 			}
 			else {
 			    XenUtils.shutdownMachineByIpAddress(xenConfig, ipAdresss, timeoutInMilliseconds, TimeUnit.MILLISECONDS);
 			}
 			latch.await(timeoutInMilliseconds,TimeUnit.MILLISECONDS);
 		} catch (final Exception e) {
 			AssertUtils.AssertFail("Waiting for machine to be removed", e);
 		}
 		finally {
 			admin.getMachines().getMachineRemoved().remove(eventListener);
 		}
 	}
 
 	public static void writeData(final ProcessingUnit pu, final int numToAdd) {
 		writeData(pu, numToAdd, numToAdd);		
 	}
 	
 	public static void writeData(final ProcessingUnit pu, 
 								final int numberOfObjectsToWrite, 
 								int totalObjectCountAfter) {
 		final Space space = pu.getSpace();
 	    AssertUtils.assertNotNull("Failed getting space instance", space);
 	    final GigaSpace gigaSpace = space.getGigaSpace();
         final Person[] persons = new Person[numberOfObjectsToWrite];
         int startInd = totalObjectCountAfter-numberOfObjectsToWrite;
         for (int id = 0; id < numberOfObjectsToWrite; id++) {
             persons[id] = new Person(new Long(startInd+id));
         }
         gigaSpace.writeMultiple(persons);        
         AssertUtils.assertEquals("Number of Person Pojos in space", totalObjectCountAfter, countData(pu));
 	}
 	
 	public static int countData(ProcessingUnit pu) {
 		return pu.getSpace().getGigaSpace().count(null,ReadModifiers.READ_COMMITTED);
 	}
 
 	public static void waitForScaleToComplete(ProcessingUnit pu,
 			int expectedNumberOfContainers, long operationTimeout) {
 		boolean validateCpuSla = true;
 		waitForScaleToComplete(pu,expectedNumberOfContainers, validateCpuSla, operationTimeout);
 	}
 	
 	public static void waitForScaleToCompleteIgnoreCpuSla(ProcessingUnit pu,
 			int expectedNumberOfContainers, long operationTimeout) {
 		boolean validateCpuSla = false;
 		waitForScaleToComplete(pu, expectedNumberOfContainers, validateCpuSla, operationTimeout);
 	}
 	
 	private static void waitForScaleToComplete(ProcessingUnit pu,
 			int expectedNumberOfContainers, boolean validateCpuSla, long operationTimeout) {
 		waitForScaleToComplete(pu, expectedNumberOfContainers, 0, validateCpuSla, operationTimeout);
 		
 	}
 
     public static void restartElasticServiceManager(final ElasticServiceManager esm) {
         if (esm.isDiscovered()) {
 
             GridServiceAgent agent = esm.getGridServiceAgent();
             final CountDownLatch latch = new CountDownLatch(1);
             
             ElasticServiceManagerRemovedEventListener removedEventListener = new ElasticServiceManagerRemovedEventListener() {
 
                 public void elasticServiceManagerRemoved(
                         ElasticServiceManager elasticServiceManager) {
                     if (elasticServiceManager.equals(esm)) {
                         latch.countDown();
                     }
                 }
             };
             ElasticServiceManagers managers = esm.getAdmin().getElasticServiceManagers();
             managers.getElasticServiceManagerRemoved().add(removedEventListener);
             try {
                 esm.kill();
                 latch.await();
             } catch (InterruptedException e) {
                 Assert.fail("Interrupted while killing esm", e);
             } finally {
                 managers.getElasticServiceManagerRemoved().remove(removedEventListener);
             }
             
             AdminUtils.loadESM(agent);
         }
     }
     
     public static void killGsm(final GridServiceManager gsm) {
         Admin admin = gsm.getAdmin();
         final CountDownLatch latch = new CountDownLatch(1);
         GridServiceManagerRemovedEventListener eventListener = new GridServiceManagerRemovedEventListener() {
             public void gridServiceManagerRemoved(
                     GridServiceManager gridServiceManager) {
                 if (gridServiceManager.equals(gsm)) {
                     latch.countDown();
                 }
                 
             }};
         admin.getGridServiceManagers().getGridServiceManagerRemoved().add(eventListener);
         try {
             gsm.kill();
             latch.await();
         } catch (InterruptedException e) {
             Assert.fail("Interrupted while killing gsm", e);
         } finally {
             admin.getGridServiceManagers().getGridServiceManagerRemoved().remove(eventListener);
         }
     }
     
     public static boolean isEvenlyDistributedAcrossMachines(ProcessingUnit pu, Machine[] machines) {
         
         boolean rebalancedMachines = RebalancingUtils
                 .isEvenlyDistributedAcrossMachines(pu, getClusterCapacity(machines));
         return rebalancedMachines;
     }
 
	private static CapacityRequirementsPerAgent getClusterCapacity(Machine[] machines) {
		CapacityRequirementsPerAgent clusterCapacityRequirements = new CapacityRequirementsPerAgent();
         for (final Machine machine : machines) {
         	if (machine.getGridServiceAgent() != null /* machine not going down*/) { 
             clusterCapacityRequirements = clusterCapacityRequirements.add(
                         machine.getGridServiceAgent().getUid(),
                         new MachineCapacityRequirements(machine));
         	}
         }
 		return clusterCapacityRequirements;
 	}
 
 	public static void waitForDrives(Admin admin, final String drive, final int driveCapacityInMB) {
 		
 		final AtomicLong totalMBs = new AtomicLong();
 		final CountDownLatch latch = new CountDownLatch(1);
         MachineAddedEventListener eventListener = new MachineAddedEventListener() {
             
 			public void machineAdded(Machine machine) {
 			
 				MachineCapacityRequirements machineCapacityRequirements = new MachineCapacityRequirements(machine);
 				LogUtils.log(machineCapacityRequirements.toString());
 				long driveCapacityInMB = machineCapacityRequirements.getRequirement(new DriveCapacityRequirement(drive).getType()).getDriveCapacityInMB();
 				LogUtils.log("machine " + machine.getHostAddress() + " has " + driveCapacityInMB + "MB total disk space on " + drive);
 				totalMBs.addAndGet(
 					driveCapacityInMB);
 				
 				LogUtils.log("All machines have " + totalMBs.get() + "MB total disk space on " + drive +" test target is " + driveCapacityInMB +"MB");
 				if (totalMBs.get() >= driveCapacityInMB) {
 					latch.countDown();
 				}
 				
 			}};
         admin.getMachines().getMachineAdded().add(eventListener);
         try {
             latch.await();
         } catch (InterruptedException e) {
             Assert.fail("Interrupted while waiting for " + driveCapacityInMB + "GB drives", e);
         } finally {
             admin.getMachines().getMachineAdded().remove(eventListener);
         }
 		
 	}
 	
 	public static RepetitiveConditionProvider newScaleConditionProvider(
 	        final ProcessingUnit pu,
             final int expectedNumberOfContainers,
             final int expectedNumberOfMachines,
             final boolean validateCpuSla) {
 	    
 	    return new RepetitiveConditionProvider() {
             
             private String lastMessage = "";
             
             public boolean getCondition() {
                 String message = "waitForScaleToComplete: ";
                 
                 Admin admin = pu.getAdmin();
                 GridServiceContainer[] containers = admin.getGridServiceContainers().getContainers();
                 Machine[] machines = admin.getMachines().getMachines();
                 boolean expectedMachines;
                 if (expectedNumberOfMachines == 0) {
                     // in cases we are not using Xen, we may deploy on only part of the machines.
                     machines = RebalancingUtils.getMachinesHostingContainers(containers);
                     expectedMachines = true;
                 }
                 else {
                     expectedMachines = expectedNumberOfMachines == machines.length;
                     if (!expectedMachines) {
                         message += "expected " + expectedNumberOfMachines +" machines, actual " + machines.length + " machines. ";
                     }
                 }
                 
                 boolean expectedContainers = (containers.length == expectedNumberOfContainers);
                 if (!expectedContainers) {
                     message += "expected " + expectedNumberOfContainers +" containers, actual " + containers.length + " containers. ";
                 }
                 boolean rebalancedMachines = true;
                 if (validateCpuSla) {
                     // only when cpu sla is involved the esm actually distributes evenly accross machines
                     rebalancedMachines = isEvenlyDistributedAcrossMachines(pu, machines);
                     if (!rebalancedMachines) {
                         message += "expected primary instances per machine core to be rebalanced, but they are not. ";
                     }
                 }
                 boolean rebalancedContainers = RebalancingUtils
                         .isEvenlyDistributedAcrossContainers(pu, containers);
                 if (!rebalancedContainers) { 
                     message += "expected instances per container to be rebalanced, but they are not. ";
                 }
                 
                 
                 
                 if (!lastMessage.equals(message)) {
                     LogUtils.log(message);
                     lastMessage = message;
                     LogUtils.log(getClusterCapacity(machines).toDetailedString());
                     TeardownUtils.snapshot(admin);
                 }
                 return expectedMachines && expectedContainers && 
                        rebalancedMachines && rebalancedContainers;
             }
         };
 	}
 	
 	
     public static void assertScaleCompletedIgnoreCpuSla(ProcessingUnit pu,
             int expectedNumberOfContainers) {
         boolean validateCpuSla = false;
         assertScaleCompleted(pu, expectedNumberOfContainers, validateCpuSla);
     }
 
     private static void assertScaleCompleted(ProcessingUnit pu,
             int expectedNumberOfContainers, boolean validateCpuSla) {
         assertScaleCompleted(pu, expectedNumberOfContainers, 0, validateCpuSla);
     }
 
     private static void assertScaleCompleted(final ProcessingUnit pu,
             final int expectedNumberOfContainers,
             final int expectedNumberOfMachines, final boolean validateCpuSla) {
 
         final RepetitiveConditionProvider condition = newScaleConditionProvider(
                 pu, expectedNumberOfContainers, expectedNumberOfMachines,
                 validateCpuSla);
         AssertUtils.assertTrue("Scale condition not met",
                 condition.getCondition());
     }
    
    
 }
