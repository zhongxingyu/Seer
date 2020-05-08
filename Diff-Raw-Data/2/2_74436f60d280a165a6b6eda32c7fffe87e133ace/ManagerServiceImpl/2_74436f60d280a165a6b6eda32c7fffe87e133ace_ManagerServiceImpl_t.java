 package edu.toronto.ece1779.ec2.service;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Vector;
 
 import com.amazonaws.AmazonClientException;
 import com.amazonaws.AmazonServiceException;
 import com.amazonaws.auth.BasicAWSCredentials;
 import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
 import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;
 import com.amazonaws.services.cloudwatch.model.Dimension;
 import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsRequest;
 import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsResult;
 import com.amazonaws.services.cloudwatch.model.ListMetricsRequest;
 import com.amazonaws.services.cloudwatch.model.ListMetricsResult;
 import com.amazonaws.services.cloudwatch.model.Metric;
 import com.amazonaws.services.ec2.AmazonEC2;
 import com.amazonaws.services.ec2.AmazonEC2Client;
 import com.amazonaws.services.ec2.model.Instance;
 import com.amazonaws.services.ec2.model.Reservation;
 import com.amazonaws.services.ec2.model.RunInstancesRequest;
 import com.amazonaws.services.ec2.model.RunInstancesResult;
 import com.amazonaws.services.ec2.model.StartInstancesRequest;
 import com.amazonaws.services.ec2.model.StopInstancesRequest;
 import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancingClient;
 import com.amazonaws.services.elasticloadbalancing.model.DeregisterInstancesFromLoadBalancerRequest;
 import com.amazonaws.services.elasticloadbalancing.model.RegisterInstancesWithLoadBalancerRequest;
 
 import edu.toronto.ece1779.awsAccess.AwsAccessManager;
 import edu.toronto.ece1779.ec2.dao.ManagerDAO;
 import edu.toronto.ece1779.ec2.dao.ManagerDAOImpl;
 import edu.toronto.ece1779.ec2.entity.ManagerConfig;
 import edu.toronto.ece1779.ec2.entity.Worker;
 import edu.toronto.ece1779.ec2.web.ManageWorkerThread;
 import edu.toronto.ece1779.ec2.web.UpdateBalancerThread;
 
 public class ManagerServiceImpl implements ManagerService {
 	
 	public static final String IMAGE_ID = "ami-394add50";
 	public static final String KEY_PAIR_NAME = "myKeys";
 	
 	
 	public List<Worker> retrieveRunningWorkersInfo() {
 		Map<String, Double> cpuUsageMap = retrieveCPUUsageMap();
 		List<Worker> workers = retrieveWorkersInfo();
 		List<Worker> runningWorkers = new ArrayList<Worker>();
 		
 		for(int i=0; i<workers.size(); i++) {
 			if("running".equals(workers.get(i).getStatus())) {
 				runningWorkers.add(workers.get(i));
 			}
 		}
 		
 		for(int j=0; j<runningWorkers.size(); j++) {
 			String id = runningWorkers.get(j).getId();
 			if (cpuUsageMap.containsKey(id)){
 				runningWorkers.get(j).setCpuUsage(cpuUsageMap.get(id));
 			} else {
 				runningWorkers.get(j).setCpuUsage(0);
 			}
 		}
 		
 		return runningWorkers;
 	}
 
 	
     public Map<String, Double> retrieveCPUUsageMap(){
     	Map<String, Double> cpuUsageMap = new HashMap<String, Double>();
         AmazonCloudWatch cw = getCloudWatch();
    
         try {
         	ListMetricsRequest listMetricsRequest = new ListMetricsRequest();
         	listMetricsRequest.setMetricName("CPUUtilization");
         	listMetricsRequest.setNamespace("AWS/EC2");
         	ListMetricsResult result = cw.listMetrics(listMetricsRequest);
         	java.util.List<Metric> 	metrics = result.getMetrics();
         	for (Metric metric : metrics) {
         		String namespace = metric.getNamespace();
         		String metricName = metric.getMetricName();
         		List<Dimension> dimensions = metric.getDimensions();
             	GetMetricStatisticsRequest statisticsRequest = new GetMetricStatisticsRequest();
             	statisticsRequest.setNamespace(namespace);
             	statisticsRequest.setMetricName(metricName);
             	statisticsRequest.setDimensions(dimensions);
             	Date endTime = new Date();
             	Date startTime = new Date();
             	startTime.setTime(endTime.getTime()-600000);
             	statisticsRequest.setStartTime(startTime);
             	statisticsRequest.setEndTime(endTime);
             	statisticsRequest.setPeriod(60);
             	Vector<String>statistics = new Vector<String>();
             	statistics.add("Maximum");
             	statisticsRequest.setStatistics(statistics);
             	GetMetricStatisticsResult stats = cw.getMetricStatistics(statisticsRequest);
             	
             	System.out.print("Namespace = " + namespace + " Metric = " + metricName + " Dimensions = " + dimensions);
             	System.out.print("Values = " + stats.toString());
             	
             	if(stats.getDatapoints().size()>0 && metric.getDimensions()!=null && metric.getDimensions().size()>0 && metric.getDimensions().get(0)!=null && stats.getDatapoints().get(0)!=null) {
             		cpuUsageMap.put(metric.getDimensions().get(0).getValue(), stats.getDatapoints().get(0).getMaximum());
             	}
             }
         } catch (AmazonServiceException ase) {
         	System.out.println("Caught an AmazonServiceException, which means your request made it "
                     + "to Amazon EC2, but was rejected with an error response for some reason.");
             System.out.println("Error Message:    " + ase.getMessage());
             System.out.println("HTTP Status Code: " + ase.getStatusCode());
             System.out.println("AWS Error Code:   " + ase.getErrorCode());
             System.out.println("Error Type:       " + ase.getErrorType());
             System.out.println("Request ID:       " + ase.getRequestId());
         } catch (AmazonClientException ace) {
             System.out.println("Caught an AmazonClientException, which means the client encountered "
                     + "a serious internal problem while trying to communicate with EC2, "
                     + "such as not being able to access the network.");
             System.out.println("Error Message: " + ace.getMessage());
         }
         
         return cpuUsageMap;
     }
     
     
     public List<Worker> retrieveWorkersInfo() {
     	List<Worker> workers = new ArrayList<Worker>();
     	AmazonEC2 ec2 = getEC2();
     	for(Reservation res : ec2.describeInstances().getReservations()) {
     		for(Instance instance : res.getInstances()) {
     			String id = instance.getInstanceId();
     			String state = instance.getState().getName();
    				workers.add(new Worker(id, state));
     		}
     	}
     	return workers;
     }
     
     
     public void increaseWorkers(int number) {
     	List<Worker> workers = retrieveWorkersInfo();
     	List<Worker> notRunningWorkers = new ArrayList<Worker>();
     	
     	for(int i=0; i<workers.size(); i++) {
    		if(!("running".equals(workers.get(i).getStatus())) && !("terminated".equals(workers.get(i).getStatus()))) {
     			notRunningWorkers.add(workers.get(i));
     		}
     	}
     	
 		List<String> ids = new ArrayList<String>();
     	if(notRunningWorkers.size() >= number) {
     		for(int j=0; j<number; j++) {
     			ids.add(notRunningWorkers.get(j).getId());
     		}
     	}
     	else {
     		for(int j=0; j<notRunningWorkers.size(); j++) {
     			ids.add(notRunningWorkers.get(j).getId());
     		}
     		createInstances(number-notRunningWorkers.size());
     	}
     	startInstances(ids);
     }
     
     
     public void reduceWorkers(int number) {
     	List<Worker> runningWorkers = retrieveRunningWorkersInfo();
     	List<String> ids = new ArrayList<String>();
     	for(int i=0; i<number; i++) {
     		ids.add(runningWorkers.get(runningWorkers.size()-1-i).getId());
     	}
     	stopInstances(ids);
     }
     
     
 	private void createInstances(int number) {
 		BasicAWSCredentials awsCredentials = AwsAccessManager.getInstance().getAWSCredentials();
 		AmazonEC2 ec2 = new AmazonEC2Client(awsCredentials);
 		
 		try{
 			RunInstancesRequest request = new RunInstancesRequest(IMAGE_ID, number, number);
 			request.setKeyName(KEY_PAIR_NAME);
 			RunInstancesResult result = ec2.runInstances(request);
 			List<Instance> instances = result.getReservation().getInstances();
 			
 			UpdateBalancerThread updateBalancerThread = new UpdateBalancerThread(instances);
 			new Thread(updateBalancerThread).start();
 			
 		} catch (AmazonClientException ace) {
             System.out.println("Error Message: " + ace.getMessage());
 		}
 	}
 	
 	private void startInstances(List<String> ids) {
 		BasicAWSCredentials awsCredentials = AwsAccessManager.getInstance().getAWSCredentials();
 		AmazonEC2 ec2 = new AmazonEC2Client(awsCredentials);
 		
 		try{
 			StartInstancesRequest request = new StartInstancesRequest();
 			request.setInstanceIds(ids);
 			addOrRemoveInstancesToLoadBalancer(ids, true);
 			ec2.startInstances(request);
 		} catch(AmazonClientException ace) {
             System.out.println("Error Message: " + ace.getMessage());
 		}
 	}
 	
 	private void stopInstances(List<String> ids) {
 		BasicAWSCredentials awsCredentials = AwsAccessManager.getInstance().getAWSCredentials();
 		AmazonEC2 ec2 = new AmazonEC2Client(awsCredentials);
 		
 		try{
 			StopInstancesRequest request = new StopInstancesRequest();
 			request.setInstanceIds(ids);
 			addOrRemoveInstancesToLoadBalancer(ids, false);
 			ec2.stopInstances(request);
 		} catch(AmazonClientException ace) {
             System.out.println("Error Message: " + ace.getMessage());
 		}
 	}
     
 	
 	public void addOrRemoveInstancesToLoadBalancer(List<String> ids, boolean add) {
 		BasicAWSCredentials credentials = AwsAccessManager.getInstance().getAWSCredentials();
 		AmazonElasticLoadBalancingClient elb = new AmazonElasticLoadBalancingClient(credentials);
 //        CreateLoadBalancerRequest lbRequest = new CreateLoadBalancerRequest();
 //        lbRequest.setLoadBalancerName("Group4Balancer");
 //        List<Listener> listeners = new ArrayList<Listener>(1);
 //        listeners.add(new Listener("HTTP", 8080, 8080));
 //        lbRequest.withAvailabilityZones("us-east-1b");
 //        lbRequest.setListeners(listeners);
 //        CreateLoadBalancerResult lbResult=elb.createLoadBalancer(lbRequest);
 //        System.out.println("created load balancer Group4Balancer");	    
         List<com.amazonaws.services.elasticloadbalancing.model.Instance> instanceIds = new ArrayList<com.amazonaws.services.elasticloadbalancing.model.Instance>();
 //        instanceIds.add(new com.amazonaws.services.elasticloadbalancing.model.Instance("i-a82ec6db"));
 //        instanceIds.add(new com.amazonaws.services.elasticloadbalancing.model.Instance("i-2a71ea59"));      
         if(ids != null) {
         	for(int i=0; i<ids.size(); i++) {
                 	instanceIds.add(new com.amazonaws.services.elasticloadbalancing.model.Instance(ids.get(i))); 
                 }
         }
         
         if(add) {
             RegisterInstancesWithLoadBalancerRequest register =new RegisterInstancesWithLoadBalancerRequest();
             register.setLoadBalancerName("Group4Balancer");
             register.setInstances(instanceIds);
             elb.registerInstancesWithLoadBalancer(register);
         } else {
         	DeregisterInstancesFromLoadBalancerRequest deregister =new DeregisterInstancesFromLoadBalancerRequest();
             deregister.setLoadBalancerName("Group4Balancer");
             deregister.setInstances(instanceIds);
             elb.deregisterInstancesFromLoadBalancer(deregister);
         }
 	}
     
 	private AmazonEC2 getEC2() {
 		BasicAWSCredentials awsCredentials = AwsAccessManager.getInstance().getAWSCredentials();
 		AmazonEC2 ec2 = new AmazonEC2Client(awsCredentials);
 		return ec2;
 	}
 	
 	private AmazonCloudWatch getCloudWatch() {
 		BasicAWSCredentials awsCredentials = AwsAccessManager.getInstance().getAWSCredentials();
         AmazonCloudWatch cw = new AmazonCloudWatchClient(awsCredentials);
         return cw;
 	}
 
 
 	public void updateManagerConfig(ManagerConfig config) {
 		ManagerDAO managerDAO = new ManagerDAOImpl();
 		managerDAO.updateManagerConfig(config);
 	}
 
 
 	public ManagerConfig retrieveManagerConfig() {
 		ManagerDAO managerDAO = new ManagerDAOImpl();
 		return managerDAO.retrieveManagerConfig();
 	}
 }
