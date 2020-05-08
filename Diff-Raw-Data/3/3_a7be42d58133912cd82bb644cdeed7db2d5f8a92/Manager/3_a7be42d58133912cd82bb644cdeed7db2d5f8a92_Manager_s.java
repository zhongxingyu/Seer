 package ece1779.manager;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Vector;
 
 import javax.servlet.ServletContext;
 
 import com.amazonaws.AmazonClientException;
 import com.amazonaws.auth.BasicAWSCredentials;
 import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
 import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;
 import com.amazonaws.services.cloudwatch.model.Dimension;
 import com.amazonaws.services.cloudwatch.model.DimensionFilter;
 import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsRequest;
 import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsResult;
 import com.amazonaws.services.cloudwatch.model.ListMetricsRequest;
 import com.amazonaws.services.cloudwatch.model.ListMetricsResult;
 import com.amazonaws.services.cloudwatch.model.Metric;
 import com.amazonaws.services.ec2.AmazonEC2;
 import com.amazonaws.services.ec2.AmazonEC2Client;
 import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
 import com.amazonaws.services.ec2.model.DescribeInstancesResult;
 import com.amazonaws.services.ec2.model.Filter;
 import com.amazonaws.services.ec2.model.Instance;
 import com.amazonaws.services.ec2.model.InstanceStateChange;
 import com.amazonaws.services.ec2.model.Reservation;
 import com.amazonaws.services.ec2.model.RunInstancesRequest;
 import com.amazonaws.services.ec2.model.RunInstancesResult;
 import com.amazonaws.services.ec2.model.StartInstancesRequest;
 import com.amazonaws.services.ec2.model.StopInstancesRequest;
 import com.amazonaws.services.ec2.model.StopInstancesResult;
 import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancingClient;
 import com.amazonaws.services.elasticloadbalancing.model.DeregisterInstancesFromLoadBalancerRequest;
 import com.amazonaws.services.elasticloadbalancing.model.RegisterInstancesWithLoadBalancerRequest;
 
 public class Manager {
 
 	// Global Variables
 	private static final String IMAGE_ID = "ami-2cd74845";
 	private static final String KEY_NAME = "g9kp";
 	private static final String BALANCER_NAME = "a1g9";
 
 	// Constructor
 	public Manager() {
 
 	}
 
 	// Methods
 
 	public static List<Object[]> getCPUUsage(ServletContext ctxt) {
 		BasicAWSCredentials awsCredentials = (BasicAWSCredentials) ctxt
 				.getAttribute("AWSCredentials");
 		AmazonCloudWatch cw = new AmazonCloudWatchClient(awsCredentials);
 		ListMetricsRequest listMetricsRequest = new ListMetricsRequest();
 		listMetricsRequest.setMetricName("CPUUtilization");
 		listMetricsRequest.setNamespace("AWS/EC2");
 		// DimensionFilter filter = new DimensionFilter();
 		// filter.setName("image-id");
 		// filter.setValue(IMAGE_ID);
 		// List<DimensionFilter> filters = new ArrayList<DimensionFilter>();
 		// filters.add(filter);
 		// listMetricsRequest.setDimensions(filters);
 		List<Instance> instances = getRunningInstances(ctxt, IMAGE_ID);
 		List<String> instanceIds = new ArrayList<String>();
 		for (Instance inst : instances) {
 			instanceIds.add(inst.getInstanceId());
 		}
 		ListMetricsResult result = cw.listMetrics(listMetricsRequest);
 		java.util.List<Metric> metrics = result.getMetrics();
 		List<Object[]> cpuStats = new ArrayList<Object[]>();
 		for (Metric metric : metrics) {
 			Object[] data = new Object[2];
 			;
 			String namespace = metric.getNamespace();
 			String metricName = metric.getMetricName();
 			List<Dimension> dimensions = metric.getDimensions();
 			GetMetricStatisticsRequest statisticsRequest = new GetMetricStatisticsRequest();
 			statisticsRequest.setNamespace(namespace);
 			statisticsRequest.setMetricName(metricName);
 			statisticsRequest.setDimensions(dimensions);
 			Date endTime = new Date();
 			Date startTime = new Date();
 			startTime.setTime(endTime.getTime() - 600000);
 			statisticsRequest.setStartTime(startTime);
 			statisticsRequest.setEndTime(endTime);
 			statisticsRequest.setPeriod(60);
 			Vector<String> statistics = new Vector<String>();
 			statistics.add("Average");
 			statisticsRequest.setStatistics(statistics);
 			GetMetricStatisticsResult stats = cw
 					.getMetricStatistics(statisticsRequest);
 
 			if (!dimensions.isEmpty() && dimensions.size() > 0
 					&& instanceIds.contains(dimensions.get(0).getValue())) {
 				data[0] = dimensions.get(0).getValue();
 				if (!stats.getDatapoints().isEmpty()
 						&& stats.getDatapoints().size() > 0) {
 					data[1] = Double.valueOf(stats.getDatapoints().get(0)
 							.getAverage());
 				} else {
 					data[1] = Double.valueOf(0.0);
 				}
 				cpuStats.add(data);
 			}
 
 			// out.print("<p>");
 			// out.print("Namespace = " + namespace + " Metric = " + metricName
 			// + " Dimensions = " + dimensions);
 			// out.print("Values = " + stats.toString());
 			// out.println("</p>");
 
 		}
 		return cpuStats;
 
 	}
 
 	public static List<String[]> getInstancesCPUUsgae(ServletContext ctxt) {
 		List<Object[]> stats = getCPUUsage(ctxt);
 		List<String[]> cpuStats = new ArrayList<String[]>();
 		for (Object[] o : stats) {
 			System.out.println("Instance: " + o[0] + " @ " + o[1]);
 			String[] data = new String[2];
 			data[0] = (String) o[0];
 			data[1] = "" + ((Double) o[1]) + "%";
 			cpuStats.add(data);
 		}
 
 		return cpuStats;
 
 	}
 
 	public static void startInstances(ServletContext ctxt, int count) {
 
 		// Get the AWS Controllers
 		BasicAWSCredentials awsCredentials = (BasicAWSCredentials) ctxt
 				.getAttribute("AWSCredentials");
 		AmazonEC2 ec2 = new AmazonEC2Client(awsCredentials);
 		AmazonElasticLoadBalancingClient elb = new AmazonElasticLoadBalancingClient(
 				awsCredentials);
 		List<Instance> instances = getStoppedInstances(ctxt, IMAGE_ID);
 		List<Instance> startedInstances = new ArrayList<Instance>();
 
 		try {
 			if (instances != null && !instances.isEmpty()
 					&& instances.size() > 0) {
 				List<String> instanceIds = new ArrayList<String>();
 				if (instances.size() >= count) {
 					System.out
 							.println("GGG: More stopped instances than we need to start: Have "
 									+ instances.size()
 									+ " stopped instances. Need to start "
 									+ count + " instances");
 					for (int i = 0; i < count; i++) {
 						instanceIds.add(instances.get(i).getInstanceId());
 					}
 					count = 0;
 				} else {
 					System.out
 							.println("GGG: Less stopped instances than we need to start: Have "
 									+ instances.size()
 									+ " stopped instances. Need to start "
 									+ count + " instances");
 					for (Instance inst : instances) {
 						instanceIds.add(inst.getInstanceId());
 					}
 					count -= instances.size();
 				}
 
 				System.out.println("GGG: Starting: " + instanceIds.size()
 						+ " instances");
 				for (String id : instanceIds) {
 					System.out.println("GGG: Starting: ID: " + id + ".");
 				}
 
 				StartInstancesRequest startRequest = new StartInstancesRequest(
 						instanceIds);
 				// StartInstancesResult result =
 				ec2.startInstances(startRequest);
 				// for (InstanceStateChange stateChange :
 				// result.getStartingInstances()) {
 				// com.amazonaws.services.elasticloadbalancing.model.Instance i2
 				// = new
 				// com.amazonaws.services.elasticloadbalancing.model.Instance();
 				// i2.setInstanceId(stateChange.getInstanceId());
 				// balanceInstances.add(i2);
 				// }
 				startedInstances.addAll(instances);
 			}
 
 			if (count != 0) {
 
 				RunInstancesRequest request = new RunInstancesRequest(IMAGE_ID,
 						count, count);
 				request.setKeyName(KEY_NAME);
 				RunInstancesResult result = ec2.runInstances(request);
 				Reservation reservation = result.getReservation();
 				List<Instance> newInstances = reservation.getInstances();
 				startedInstances.addAll(newInstances);
 			}
 			RegisterInstancesWithLoadBalancerRequest registerRequest = new RegisterInstancesWithLoadBalancerRequest();
 			List<com.amazonaws.services.elasticloadbalancing.model.Instance> balanceInstances = new ArrayList<com.amazonaws.services.elasticloadbalancing.model.Instance>();
 			for (Instance i : startedInstances) {
 				com.amazonaws.services.elasticloadbalancing.model.Instance i2 = new com.amazonaws.services.elasticloadbalancing.model.Instance();
 				i2.setInstanceId(i.getInstanceId());
 				balanceInstances.add(i2);
 			}
 
 			registerRequest.withInstances(balanceInstances);
 			registerRequest.setLoadBalancerName(BALANCER_NAME);
 			// RegisterInstancesWithLoadBalancerResult regResult =
 			elb.registerInstancesWithLoadBalancer(registerRequest);
 
 			for (Instance inst : instances) {
 				System.out.println("Instance Info = " + inst.toString());
 			}
 		} catch (AmazonClientException ace) {
 			System.err
 					.println("Caught an AmazonClientException, which means the client encountered "
 							+ "a serious internal problem while trying to communicate with EC2, "
 							+ "such as not being able to access the network.");
 			System.err.println("Error Message: " + ace.getMessage());
 		}
 
 	}
 
 	public static void stopInstances(ServletContext ctxt, int count) {
 
 		// Get the AWS Controllers
 		BasicAWSCredentials awsCredentials = (BasicAWSCredentials) ctxt
 				.getAttribute("AWSCredentials");
 		AmazonEC2 ec2 = new AmazonEC2Client(awsCredentials);
 		AmazonElasticLoadBalancingClient elb = new AmazonElasticLoadBalancingClient(
 				awsCredentials);
 		List<Instance> instances = getRunningInstances(ctxt, IMAGE_ID);
 		List<com.amazonaws.services.elasticloadbalancing.model.Instance> balanceInstances = new ArrayList<com.amazonaws.services.elasticloadbalancing.model.Instance>();
 		List<String> instanceIds = new ArrayList<String>();
 		if (instances.size() > count) {
 			for (int i = 0; i < count; i++) {
 				instanceIds.add(instances.get(i).getInstanceId());
 			}
 		} else {
 			for (int i = 0; i < (instances.size() - 1); i++) {
 				instanceIds.add(instances.get(i).getInstanceId());
 			}
 		}
 
 		if (instanceIds != null && !instanceIds.isEmpty()
 				&& instanceIds.size() > 0) {
 
 			try {
 				StopInstancesRequest request = new StopInstancesRequest(
 						instanceIds);
 				StopInstancesResult result = ec2.stopInstances(request);
 				List<InstanceStateChange> stateChanges = result
 						.getStoppingInstances();
 
 				for (InstanceStateChange stateChange : stateChanges) {
 					com.amazonaws.services.elasticloadbalancing.model.Instance i2 = new com.amazonaws.services.elasticloadbalancing.model.Instance();
 					i2.setInstanceId(stateChange.getInstanceId());
 					balanceInstances.add(i2);
 				}
 
 				DeregisterInstancesFromLoadBalancerRequest deregReq = new DeregisterInstancesFromLoadBalancerRequest();
 				deregReq.setLoadBalancerName(BALANCER_NAME);
 				deregReq.setInstances(balanceInstances);
 				// DeregisterInstancesFromLoadBalancerResult deregResult =
 				elb.deregisterInstancesFromLoadBalancer(deregReq);
 
 			} catch (AmazonClientException ace) {
 				System.err
 						.println("Caught an AmazonClientException, which means the client encountered "
 								+ "a serious internal problem while trying to communicate with EC2, "
 								+ "such as not being able to access the network.");
 				System.err.println("Error Message: " + ace.getMessage());
 			}
 		}
 
 	}
 
 	private static List<Instance> getRunningInstances(ServletContext ctxt,
 			String imageId) {
 		List<Instance> instances = getInstances(ctxt, imageId);
 		List<Instance> runningInstances = new ArrayList<Instance>();
 		if (instances != null && !instances.isEmpty() && instances.size() > 0) {
 			for (Instance inst : instances) {
 				if (inst.getState().getName().equalsIgnoreCase("running")) {
 					runningInstances.add(inst);
 				}
 			}
 		}
 
 		return runningInstances;
 
 	}
 
 	private static List<Instance> getStoppedInstances(ServletContext ctxt,
 			String imageId) {
 		List<Instance> instances = getInstances(ctxt, imageId);
 		List<Instance> runningInstances = new ArrayList<Instance>();
 		if (instances != null && !instances.isEmpty() && instances.size() > 0) {
 			for (Instance inst : instances) {
 				if (inst.getState().getName().equalsIgnoreCase("stopped")) {
 					runningInstances.add(inst);
 				}
 			}
 		}
 
 		return runningInstances;
 
 	}
 
 	private static List<Instance> getInstances(ServletContext ctxt,
 			String imageId) {
 		// Get the AWS Controllers
 		BasicAWSCredentials awsCredentials = (BasicAWSCredentials) ctxt
 				.getAttribute("AWSCredentials");
 		AmazonEC2 ec2 = new AmazonEC2Client(awsCredentials);
 		DescribeInstancesRequest request = new DescribeInstancesRequest();
 		request.setFilters(getImageIdFilter(imageId));
 		DescribeInstancesResult result = ec2.describeInstances(request);
 		List<Instance> instances = new ArrayList<Instance>();
 		for (Reservation r : result.getReservations()) {
 			for (Instance inst : r.getInstances()) {
 				instances.add(inst);
 			}
 		}
 		return instances;
 	}
 
 	private static List<Filter> getImageIdFilter(String imageID) {
 		Filter filter = new Filter("image-id");
 		List<String> imageIds = new ArrayList<String>();
 		imageIds.add(imageID);
 		filter.setValues(imageIds);
 		List<Filter> filters = new ArrayList<Filter>();
 		filters.add(filter);
 		return filters;
 
 	}
 
 	public static void autoScale(ServletContext ctxt, double growThreshold,
 			double shrinkThreshold, double growRatio, double shrinkRatio) {
 		Double avg = getAverageCPUUsage(ctxt);
 		List<Instance> running = getRunningInstances(ctxt, IMAGE_ID);
 		int count = 0;
 		if (avg >= growThreshold) {
 			count = (int) Math.floor(running.size() * growRatio);
 			if (count > 0) {
 				startInstances(ctxt, count);
 			}
 		} else if (avg <= shrinkThreshold) {
 			if (running.size() > 1) {
 				count = (int) Math.floor(running.size() / shrinkRatio);
 				stopInstances(ctxt, count);
 			}
 		}
 	}
 
 	private static Double getAverageCPUUsage(ServletContext ctxt) {
 		List<Object[]> stats = getCPUUsage(ctxt);
 		Double avg = 0.0;
 		Double count = 0.0;
 		for (Object[] stat : stats) {
 			avg += (Double) stat[1];
 			count++;
 		}
 
 		avg = avg / count;
 		return avg;
 
 	}
 }
