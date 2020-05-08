 package framework.utils;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.TimeUnit;
 
 import org.openspaces.admin.Admin;
 import org.openspaces.admin.application.Application;
 import org.openspaces.admin.gsm.GridServiceManager;
 import org.openspaces.admin.machine.Machine;
 import org.openspaces.admin.pu.DeploymentStatus;
 import org.openspaces.admin.pu.ProcessingUnit;
 import org.openspaces.admin.pu.ProcessingUnitInstance;
 import org.openspaces.admin.space.events.SpaceModeChangedEvent;
 import org.openspaces.admin.space.events.SpaceModeChangedEventListener;
 import org.openspaces.core.cluster.ClusterInfo;
 import org.openspaces.pu.service.ServiceDetails;
 
 import test.AbstractTest;
 
 import com.gigaspaces.cluster.activeelection.SpaceMode;
 
 public class ProcessingUnitUtils {
 
 	public static void waitForActiveElection(ProcessingUnit processingUnit) {
 		waitForActiveElection(processingUnit,Integer.MAX_VALUE,TimeUnit.SECONDS);
 	}
 	
     /**
      * waits for active election is over
      */
     public static boolean waitForActiveElection(ProcessingUnit processingUnit,int timeout, TimeUnit units) {
         for (ProcessingUnitInstance puInstance : processingUnit.getInstances()) {
             final CountDownLatch latch = new CountDownLatch(1);
             puInstance.waitForSpaceInstance();
             SpaceMode currentMode = puInstance.getSpaceInstance().getMode();
             if (currentMode.equals(SpaceMode.NONE)) {
                 SpaceModeChangedEventListener listener = new SpaceModeChangedEventListener() {
                     public void spaceModeChanged(SpaceModeChangedEvent event) {
                         if (!event.getNewMode().equals(SpaceMode.NONE)) {
                             latch.countDown();
                         }
                     }
                 };
                 puInstance.getSpaceInstance().getSpaceModeChanged().add(listener);
                 try {
                 	//one last check before we go into await
                 	if (SpaceMode.NONE.equals(puInstance.getSpaceInstance().getMode()) 
                 			&& !latch.await(timeout,units)) {
                     	return false;
                     }
                 } catch (InterruptedException e) {
                     //ignore
                 } finally {
                     puInstance.getSpaceInstance().getSpaceModeChanged().remove(listener);
                 }
             }
         }
 		return true;
     }
 
 
     /**
      * waits 1 minute until deployment status changes to the expected
      */
     public static DeploymentStatus waitForDeploymentStatus(ProcessingUnit processingUnit, DeploymentStatus expected) {
         int retries = 60;
         while (retries-- > 0 && !expected.equals(processingUnit.getStatus())) {
             try {
                 Thread.sleep(1000);
             } catch (InterruptedException e) {
                 e.printStackTrace();
                 break;
             }
         }
 
         return processingUnit.getStatus();
     }
 
     /**
      * waits 1 minute until processing unit is managed by the provided GSM
      */
     public static GridServiceManager waitForManaged(ProcessingUnit processingUnit, GridServiceManager gridServiceManager) {
         int retries = 60;
         while (retries-- > 0 && !gridServiceManager.equals(processingUnit.getManagingGridServiceManager())) {
             try {
                 Thread.sleep(1000);
             } catch (InterruptedException e) {
                 e.printStackTrace();
                 break;
             }
         }
 
         return processingUnit.getManagingGridServiceManager();
     }
     
 	public static Set<Machine> getMachinesFromPu(final ProcessingUnit pu) {
 		
 		Set<Machine> machines = new HashSet<Machine>();
 		for (ProcessingUnitInstance groovy2Instance : pu.getInstances()) {
 			machines.add(groovy2Instance.getMachine());
 		}
 		
 		return machines;
 		
 	}
 
     /**
      * waits 1 minute until processing unit has a backup GSM
      */
     public static GridServiceManager waitForBackupGsm(ProcessingUnit processingUnit, GridServiceManager gridServiceManager) {
         int retries = 60;
         while (retries-- > 0 && !gridServiceManager.equals(processingUnit.getBackupGridServiceManager(gridServiceManager.getUid()))) {
             try {
                 Thread.sleep(1000);
             } catch (InterruptedException e) {
                 e.printStackTrace();
                 break;
             }
         }
 
         return processingUnit.getManagingGridServiceManager();
     }
 
     /**
      * Return the name representing each Processing Unit (as shown in the UI).
      */
     public static String getProcessingUnitInstanceName(ProcessingUnitInstance[] pus) {
         int index = 0;
         String[] strings = new String[pus.length];
         for (ProcessingUnitInstance pu : pus) {
             String name = ProcessingUnitUtils.getProcessingUnitInstanceName(pu);
             strings[index++] = name;
         }
         return Arrays.toString(strings);
     }
 
     /**
      * Return the name representing this Processing Unit (as shown in the UI).
      */
     public static String getProcessingUnitInstanceName(ProcessingUnitInstance pu) {
         String name = "null";
         ClusterInfo clusterInfo = pu.getClusterInfo();
         if (clusterInfo != null) {
             name = clusterInfo.getName();
             Integer id = clusterInfo.getInstanceId();
             if (clusterInfo.getNumberOfBackups() > 0) {
                 Integer bid = clusterInfo.getBackupId();
                 if (bid == null) {
                     bid = Integer.valueOf(0);
                 }
                 name += "." + id + " [" + (bid + 1) + "]";
             } else {
                 name += " [" + id + "]";
             }
         }
         return name;
     }
     
 	public static URL getWebProcessingUnitURL(ProcessingUnit pu, boolean isSecured) {
 		
 		String url;
 		
 		ProcessingUnitInstance pui = pu.getInstances()[0];
 		Map<String, ServiceDetails> alldetails = pui
 		.getServiceDetailsByServiceId();
 
 		ServiceDetails details = alldetails.get("jee-container");
 		String host = details.getAttributes().get("host").toString();
 		String port = details.getAttributes().get("port").toString();
 		String ctx = details.getAttributes().get("context-path").toString();
 		if(isSecured){			
 			url = "https://" + host + ":" + port + ctx;
 		}
 		else{
 			url = "http://" + host + ":" + port + ctx;
 		}
 		try {
 			return new URL(url);
 		} catch (MalformedURLException e) {
 			// this is a bug since we formed the URL correctly
 			throw new IllegalStateException(e);
 		}
 	}
 	
 	public static Set<Machine> getMachinesOfApplication(final Admin admin , final String applicationName) {
 		Set<Machine> machines = new HashSet<Machine>();
 		Application app = admin.getApplications().waitFor(applicationName, AbstractTest.OPERATION_TIMEOUT, TimeUnit.MILLISECONDS);
 		for (ProcessingUnit pu : app.getProcessingUnits()) {
 			for (ProcessingUnitInstance puInstance : pu.getInstances()) {
 				machines.add(puInstance.getMachine());
 			}
 			
 		}
 		return machines;
 	}
 	
 	public static Set<Machine> getMachinesOfService(final Admin admin, final String serviceName) {
		LogUtils.log("Retreiving machines for processing unit " + serviceName);
 		Set<Machine> machines = new HashSet<Machine>();
 		ProcessingUnit pu = admin.getProcessingUnits().waitFor(serviceName, AbstractTest.OPERATION_TIMEOUT, TimeUnit.MILLISECONDS);
 		for (ProcessingUnitInstance puInstance : pu.getInstances()) {
 			machines.add(puInstance.getMachine());
 		}
 		return machines;
 	}
 }
