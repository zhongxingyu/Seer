 package nl.tudelft.cloud_computing_project.instance_allocation;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 
 import nl.tudelft.cloud_computing_project.AmazonEC2Initializer;
 import nl.tudelft.cloud_computing_project.CloudOCR;
 import nl.tudelft.cloud_computing_project.Monitor;
 import nl.tudelft.cloud_computing_project.model.Database;
 
 import org.apache.commons.codec.binary.Base64;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.sql2o.Sql2o;
 
 import com.amazonaws.AmazonServiceException;
 import com.amazonaws.services.ec2.AmazonEC2;
 import com.amazonaws.services.ec2.model.CreateTagsRequest;
 import com.amazonaws.services.ec2.model.DescribeInstancesResult;
 import com.amazonaws.services.ec2.model.Instance;
 import com.amazonaws.services.ec2.model.Reservation;
 import com.amazonaws.services.ec2.model.RunInstancesRequest;
 import com.amazonaws.services.ec2.model.RunInstancesResult;
 import com.amazonaws.services.ec2.model.Tag;
 import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
 import com.amazonaws.services.ec2.model.TerminateInstancesResult;
 
 public class AllocationManager {
 	
 	private static final String 		delete_instance_assignment_sql 	= "DELETE FROM Assignment WHERE worker_instanceid = :instanceId";
 	private static final String 		PROVISIONING_POLICY_CLASS 		= (String)CloudOCR.Configuration.get("PROVVISIONING_POLICY_CLASS");
 	private static final int 			MAX_NORMAL_INSTANCES 			= Integer.parseInt((String)CloudOCR.Configuration.get("MAX_NORMAL_INSTANCES"));
 	
 	private static Base64 				base64 							= new Base64();
 	private static String 				WORKER_SCRIPT 					= new String(base64.encode("#!/bin/bash\njava -jar /home/ubuntu/Worker/worker.jar\nexit 0".getBytes()));
 	private static Logger 				LOG 							= LoggerFactory.getLogger(AllocationManager.class);
 	private static AllocationManager 	instance;
 	
 	private AmazonEC2 					ec2 							= AmazonEC2Initializer.getInstance();
 	private Map<String, Date> 			protectedInstances 				= new Hashtable<String, Date>(); 
 	private Sql2o 						sql2o;
 	private ProvisioningPolicyInterface provisioningPolicy;
 	
 
 	private AllocationManager(){
 		try {
 			 provisioningPolicy = (ProvisioningPolicyInterface) Class.forName("nl.tudelft.cloud_computing_project.instance_allocation." + PROVISIONING_POLICY_CLASS).newInstance();
 		} catch (Exception e) {
 			LOG.error("Error instantiating ProvisioningPolicy class:\n" + e.getMessage());
 		}
 	}
 	
 	public static AllocationManager getInstance(){
 		if(instance == null) { instance = new AllocationManager(); }
 		return instance;
 	}
 	
 	public void setProtectedInstance(String instanceId) {
 		protectedInstances.put(instanceId, new Date());
 	}
 	
 	public Map<String, Date> getProtectedInstance() {
 		return protectedInstances;
 	}
 	
 	public void updateProtectedInstances() {
 		
 		LOG.debug("protectedInstances: " + protectedInstances.size());
 		
 		long toDeleteTime = new Date().getTime() - (5 * 60000);
 		Collection<String> toDeleteKeys = new ArrayList<String>();
 		
 		for(Map.Entry<String, Date> protectedInstance : protectedInstances.entrySet()) {
 			if (protectedInstance.getValue().getTime() < toDeleteTime)
 				toDeleteKeys.add(protectedInstance.getKey());
 		}
 		
 		for (String key : toDeleteKeys)
 			protectedInstances.remove(key);
 		
 		LOG.debug("protectedInstances after processing: " + protectedInstances.size());
 
 	}
 	
 	public void applyProvvisioningPolicy() {
 		
 		LOG.info("Applying provisioning policy");
 		int provisioningPolicyResult = provisioningPolicy.applyProvisioningPolicy();
 		LOG.info(Math.abs(provisioningPolicyResult) + " instances will be " + (provisioningPolicyResult < 0? "un" : "") + "allocated");
 
 		
 		// INSTANCE ALLOCATION 
 		if(provisioningPolicyResult > 0)
 			allocateMachines(provisioningPolicyResult);
 	
 		// INSTANCE DEALLOCATION 
 		else if (provisioningPolicyResult < 0) {
 			int result = deallocateMachines(Math.abs(provisioningPolicyResult), true);
 			if(result < Math.abs(provisioningPolicyResult))
				LOG.error("Deallocating machines stopped (error or no charging soon)");
 		}
 
 		
 	}
 	
 	private void allocateMachines(int instancesToAllocate) {
 		
 		int normalInstancesRunning = Monitor.getInstance().getNumRunningOrPendingNormalInstances();
 		int maxAllocatableInstances;
 		int allocatedNormalInstances;
 		
 		LOG.debug("normalInstancesRunning: " + normalInstancesRunning);
 		
 		if (normalInstancesRunning < MAX_NORMAL_INSTANCES){
 			
 			maxAllocatableInstances = MAX_NORMAL_INSTANCES - normalInstancesRunning;
 			LOG.debug("maxAllocatableInstances: " + maxAllocatableInstances);
 			
 			if(maxAllocatableInstances < instancesToAllocate)
 				allocatedNormalInstances = allocateNormalInstances(maxAllocatableInstances);
 			else 
 				allocatedNormalInstances = allocateNormalInstances(instancesToAllocate);
 
 			instancesToAllocate -= (allocatedNormalInstances);
 			
 			LOG.info("Allocated " + allocatedNormalInstances + " default instances");
 			
 		}
 		
 		if (instancesToAllocate > 0){
 			Thread SpotInstancesThread = new SpotInstancesThread (instancesToAllocate);
 			SpotInstancesThread.start();
 		}
 		
 	}
 
 	private int allocateNormalInstances(int instancesToAllocate) {
 		int startedInstances = 0;
 		
 		try {
 			
 			for (startedInstances = 0; startedInstances < instancesToAllocate; startedInstances++) {
 
 				// CREATE EC2 INSTANCES
 				RunInstancesRequest runInstancesRequest = new RunInstancesRequest()
 					.withInstanceType("t1.micro")
 					.withImageId("ami-836685f4")
 					.withMinCount(1)
 					.withMaxCount(1)
 					.withSecurityGroupIds("cloudocr-worker")
 					.withUserData(WORKER_SCRIPT);
 
 				RunInstancesResult runInstances = ec2.runInstances(runInstancesRequest);
 				
 				// Tag instances + put them under protection
 				List<Instance> instances = runInstances.getReservation().getInstances();
 				for (Instance instance : instances) {
 					
 					setProtectedInstance(instance.getInstanceId());
 					
 					CreateTagsRequest createTagsRequest = new CreateTagsRequest();
 					createTagsRequest.withResources(instance.getInstanceId()).withTags(new Tag("cloudocr", "worker"));
 					
 					ec2.createTags(createTagsRequest);
 			    	LOG.info("Starting instance '" + instance.getInstanceId() + "':");
 				}
 
 			}
 		} catch (AmazonServiceException ase) {
 			LOG.error("Caught Exception while allocating instance(s): " + ase.getMessage());
 			LOG.error("Reponse Status Code: " + ase.getStatusCode());
 			LOG.error("Error Code: " + ase.getErrorCode());
 			LOG.error("Request ID: " + ase.getRequestId());
 		}
 		
 		return startedInstances;
 	}
 
 	private int deallocateMachines(int instancesToTerminateNum, boolean onlySpotInstances) {
 		
 		int terminatedInstancesCount = 0;
 		int launchedMinutes, currentMinutes, minutesToCharging;
 		String instanceId, instanceIdToBeRemoved;
 		Date launchTime;
 		Date currentTime = new Date();
 		Calendar calendar = Calendar.getInstance();
 		TreeMap<Integer, List<String>> orderedInstances = new TreeMap<Integer, List<String>>(); 
 		
 		
 		try {
 
 			//Retrieve the list of instances from EC2
 			DescribeInstancesResult describeInstancesRequest = ec2.describeInstances();
 			List<Reservation> reservations = describeInstancesRequest.getReservations();
 			Set<Instance> instances = new HashSet<Instance>();
 
 			for (Reservation reservation : reservations) 
 				instances.addAll(reservation.getInstances());
 
 			//Decide which instances to terminate
 			for (Instance instance : instances) {
 				
 				//Only terminate running machines
 				if(!instance.getState().getName().equals("running"))
 					continue;
 				
 				//Continue if: a non cloudOCR instance is being analyzed
 				boolean isCloudOCR = false;
 				
 				if(instance.getTags().isEmpty())
 					isCloudOCR = true;
 				else {
 					for (Tag tag : instance.getTags()){
 						if(!tag.getKey().equals("test")) {
 							isCloudOCR = true;
 							continue;
 						}
 					}
 				}
 				
 				if(!isCloudOCR){
 					LOG.debug(instance.getInstanceId() + ": skipped, no cloudocr");
 					continue;
 				} else {
 					//If onlySpotInstances is true then deallocate ONLY Spot Instances
 					if (onlySpotInstances) {
 						if (!instance.getTags().isEmpty()) {
 							LOG.debug(instance.getInstanceId() + ": skipped, no spotinstance");
 							continue;
 						}
 					//Else deallocate it unless its the Master
 					} else {
 						if (instance.getTags().contains(new Tag().withKey("cloudocr").withValue("master"))) {
 							LOG.debug(instance.getInstanceId() + ": skipped master");
 							continue;
 						}
 					}
 				}
 				
 				//Retrieves the launchTime of the instance
 				launchTime = instance.getLaunchTime();
 				
 				calendar.setTime(launchTime);
 				launchedMinutes = calendar.get(Calendar.MINUTE);
 
 				calendar.setTime(currentTime);
 				currentMinutes = calendar.get(Calendar.MINUTE);
 
 				//Calculates the minutes to charging for the instance
 				minutesToCharging = launchedMinutes - currentMinutes;
 				if (minutesToCharging < 0) 
 					minutesToCharging += 60;
 				
 				instanceId = instance.getInstanceId();
 				
 				//Inserts the pair in a ordered map
 				Integer keyMinutes = new Integer(minutesToCharging);
 			
 				List<String> tempList = orderedInstances.get(keyMinutes);
 				tempList = (tempList == null? new ArrayList<String>() : tempList);
 				tempList.add(instanceId);
 				
 				orderedInstances.put(keyMinutes, tempList);
 
 			}
 			
 			//This loop scans the ordered map and issues the instances terminations.
 			while (!orderedInstances.isEmpty() && terminatedInstancesCount < instancesToTerminateNum) {
 				
 				//Determinate which instance to terminate
 				int firstKey = orderedInstances.firstKey();
 				
 				//Do not terminate any if they still have a lot of time before charging
 				if(firstKey > 5) {
 					if(!onlySpotInstances)
 						return terminatedInstancesCount;
 					else
 						break;
 				}
 				
 				
 				List<String> instanceList = orderedInstances.get(firstKey);
 				
 				while (!instanceList.isEmpty() && terminatedInstancesCount < instancesToTerminateNum) {
 					
 					instanceIdToBeRemoved = instanceList.get(0);
 					
 					//Terminates the required instances and logs the information
 					TerminateInstancesRequest terminateRequest = new TerminateInstancesRequest().withInstanceIds(instanceIdToBeRemoved);
 					TerminateInstancesResult terminateResult = ec2.terminateInstances(terminateRequest);
 					LOG.warn("Allocation Manager stopped Instance: " + terminateResult.toString());
 
 					sql2o = Database.getConnection();
 					sql2o.createQuery(delete_instance_assignment_sql, "delete_instance_assignment_sql").addParameter("instanceId", instanceIdToBeRemoved).executeUpdate();
 					
 					instanceList.remove(instanceIdToBeRemoved);
 					terminatedInstancesCount++;
 					
 				}
 				
 				//Removes the key from the map and continues
 				orderedInstances.remove(firstKey);
 				
 			}
 			
 		} catch (AmazonServiceException ase) {
 			LOG.error("Caught Exception: " + ase.getMessage());
 			LOG.error("Reponse Status Code: " + ase.getStatusCode());
 			LOG.error("Error Code: " + ase.getErrorCode());
 			LOG.error("Request ID: " + ase.getRequestId());
 		}
 		
 		if (terminatedInstancesCount < instancesToTerminateNum) {
 			LOG.info("Terminated " + terminatedInstancesCount + " spot instances, proceeding to terminate " + (instancesToTerminateNum - terminatedInstancesCount) + " default instance(s)");
 			return terminatedInstancesCount + deallocateMachines(instancesToTerminateNum - terminatedInstancesCount, false);
 		}
 		LOG.info("Terminated " + terminatedInstancesCount + " default instances");
 		return terminatedInstancesCount;
 
 
 	}
 	
 	/**
 	 * This class is used to thread upon the allocation of spot instances.
 	 */
 	private class SpotInstancesThread extends Thread {
 		private int instancesToAllocate;
 		
 		public SpotInstancesThread (int instancesToAllocate) {
 			this.instancesToAllocate = instancesToAllocate;
 		}
 		
 		public void run(){
 			SpotInstancesAllocator.getInstance().requestSpotInstances(instancesToAllocate);
 		}
 	}
 }
