 package org.cloudifysource.quality.iTests.framework.utils;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.TimeoutException;
 
 import iTests.framework.utils.AssertUtils;
 import org.cloudifysource.esc.driver.provisioning.ElasticMachineProvisioningCloudifyAdapter;
 import org.openspaces.admin.gsa.GSAReservationId;
 import org.openspaces.admin.gsa.GridServiceAgent;
 import org.openspaces.admin.zone.config.ExactZonesConfig;
 import org.openspaces.admin.zone.config.ExactZonesConfigurer;
 
 public class ByonMachinesUtils {
 
 	public static GridServiceAgent startNewByonMachine (ElasticMachineProvisioningCloudifyAdapter elasticMachineProvisioningCloudifyAdapter, long duration,TimeUnit timeUnit) throws Exception {	
 		ExactZonesConfig zones = new ExactZonesConfig();
 		GSAReservationId reservationId = GSAReservationId.randomGSAReservationId();
 		return elasticMachineProvisioningCloudifyAdapter.startMachine(zones, reservationId, duration, timeUnit);
 	}
 	
 	public static GridServiceAgent startNewByonMachineWithZones (ElasticMachineProvisioningCloudifyAdapter elasticMachineProvisioningCloudifyAdapter,String[] zoneList, long duration,TimeUnit timeUnit) throws Exception {	
 		ExactZonesConfig zones = new ExactZonesConfigurer().addZones(zoneList).create();
 		GSAReservationId reservationId = GSAReservationId.randomGSAReservationId();
 		return elasticMachineProvisioningCloudifyAdapter.startMachine(zones, reservationId, duration, timeUnit);
 	}
 
 	public static GridServiceAgent[] startNewByonMachines(
 			final ElasticMachineProvisioningCloudifyAdapter elasticMachineProvisioningCloudifyAdapter,
 			int numOfMachines, final long duration,final TimeUnit timeUnit) {
 		
 		GridServiceAgent[] result = new GridServiceAgent[numOfMachines];   
 		List<Callable<GridServiceAgent>> tasks = new ArrayList<Callable<GridServiceAgent>>();
 		for (int i=0; i<numOfMachines; i++) {
 			tasks.add(new Callable<GridServiceAgent>() {
 				public GridServiceAgent call() throws Exception {
 					return startNewByonMachine(elasticMachineProvisioningCloudifyAdapter, duration,timeUnit);
 				}
 			});
 		}
 		ExecutorService  service = Executors.newFixedThreadPool(tasks.size());
         try {
             List<Future<GridServiceAgent>> futures = service.invokeAll(tasks);
     		for (int i = 0 ; i < futures.size() ; i++) {
             	result[i] = futures.get(i).get(duration, TimeUnit.MILLISECONDS);
             }
         } catch (InterruptedException e) {
             AssertUtils.assertFail("Failed starting new VMs", e);
         } catch (ExecutionException e) {
         	AssertUtils.assertFail("Failed starting new VMs", e);
         } catch (TimeoutException e) {
         	AssertUtils.assertFail("Failed starting new VMs", e);
         } finally {
             service.shutdown();
         }
         
         return result;
 	}
 
     public static GridServiceAgent[] startNewByonMachinesWithZones(
             final ElasticMachineProvisioningCloudifyAdapter elasticMachineProvisioningCloudifyAdapter,
             int numOfMachines,final String[] zoneList,  final long duration,final TimeUnit timeUnit) {
         AssertUtils.assertTrue("zone list size should be equal to number of machines to start",zoneList.length == numOfMachines);
         GridServiceAgent[] result = new GridServiceAgent[numOfMachines];
         List<Callable<GridServiceAgent>> tasks = new ArrayList<Callable<GridServiceAgent>>();
         for (int i=0; i<numOfMachines; i++) {
             final String[] zone = {zoneList[i]};
             tasks.add(new Callable<GridServiceAgent>() {
                 public GridServiceAgent call() throws Exception {
                     return startNewByonMachineWithZones (elasticMachineProvisioningCloudifyAdapter,zone, duration,timeUnit);
                 }
             });
         }
         ExecutorService  service = Executors.newFixedThreadPool(tasks.size());
         try {
             List<Future<GridServiceAgent>> futures = service.invokeAll(tasks);
             for (int i = 0 ; i < futures.size() ; i++) {
                 result[i] = futures.get(i).get(duration, TimeUnit.MILLISECONDS);
             }
         } catch (InterruptedException e) {
             AssertUtils.assertFail("Failed starting new VMs", e);
         } catch (ExecutionException e) {
             AssertUtils.assertFail("Failed starting new VMs", e);
         } catch (TimeoutException e) {
             AssertUtils.assertFail("Failed starting new VMs", e);
         } finally {
             service.shutdown();
         }
 
         return result;
     }
 	
	public static void stopByonMachine (ElasticMachineProvisioningCloudifyAdapter elasticMachineProvisioningCloudifyAdapter, GridServiceAgent agent ,long duration,TimeUnit timeUnit) throws Exception {
		elasticMachineProvisioningCloudifyAdapter.stopMachine(agent, duration, timeUnit);
 	}
 
 }
