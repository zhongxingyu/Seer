 package hudson.plugins.ec2;
 
 import hudson.model.Hudson;
 import hudson.util.TimeUnit2;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang.time.StopWatch;
 
 import com.amazonaws.AmazonClientException;
 import com.amazonaws.services.ec2.AmazonEC2;
 import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
 import com.amazonaws.services.ec2.model.DescribeInstancesResult;
 import com.amazonaws.services.ec2.model.DescribeSpotInstanceRequestsRequest;
 import com.amazonaws.services.ec2.model.DescribeSpotInstanceRequestsResult;
 import com.amazonaws.services.ec2.model.Instance;
 import com.amazonaws.services.ec2.model.Reservation;
 import com.amazonaws.services.ec2.model.SpotInstanceRequest;
 import com.trilead.ssh2.ChannelCondition;
 import com.trilead.ssh2.Connection;
 import com.trilead.ssh2.ServerHostKeyVerifier;
 import com.trilead.ssh2.Session;
 
 final class SpotRequestConnectSupervisor implements Runnable {
 	private final List<SpotInstanceRequest> reqInstances;
 	private final List<EC2AbstractSlave> spotSlaves;
 	private final AmazonEC2 ec2;
 	private String remoteAdmin;
 	private char [] privateKey;
 	private EC2Logger logger;
 
 	public static void start(EC2Logger logger, 
 			List<SpotInstanceRequest> reqInstances, 
 			List<EC2AbstractSlave> spotSlaves, 
 			AmazonEC2 ec2, 
 			char [] privateKey, 
 			String remoteAdmin) {
 		new Thread(new SpotRequestConnectSupervisor(logger, reqInstances, spotSlaves, ec2, privateKey, remoteAdmin)).start();
 	}
 
 	private SpotRequestConnectSupervisor(
 			EC2Logger logger, 
 			List<SpotInstanceRequest> reqInstances,
 			List<EC2AbstractSlave> spotSlaves, 
 			AmazonEC2 ec2,
 			char [] privateKey, 
 			String remoteAdmin) {
 		this.logger = logger;
 		this.reqInstances = reqInstances;
 		this.spotSlaves = spotSlaves;
 		this.ec2 = ec2;
 		this.privateKey = privateKey;
 		this.remoteAdmin = remoteAdmin;
 	}
 
 	@Override 
 	public void run() {
 		List<String> spotInstanceRequestIds = new ArrayList<String>();
 		for (SpotInstanceRequest req : reqInstances) {
 			spotInstanceRequestIds.add(req.getSpotInstanceRequestId());
 		}
 		LinkedList<EC2AbstractSlave> remainingSlaves = new LinkedList<EC2AbstractSlave>();
 		for (EC2AbstractSlave ec2AbstractSlave : spotSlaves) {
 			remainingSlaves.add(ec2AbstractSlave);
 		}
 		
 		do {
 			DescribeSpotInstanceRequestsRequest describeRequest = new DescribeSpotInstanceRequestsRequest();
 			describeRequest.setSpotInstanceRequestIds(spotInstanceRequestIds);
 	
 			connectFulfilledInstancesToJenkins(spotInstanceRequestIds, remainingSlaves, describeRequest);
 	
 			try {
 				Thread.sleep(60 * 1000);
 			} catch (Exception e) {
 			}
 		} while (spotInstanceRequestIds.size()>0);
 	}
 
 	private void connectFulfilledInstancesToJenkins(
 			List<String> spotInstanceRequestIds,
 			LinkedList<EC2AbstractSlave> remainingSlaves,
 			DescribeSpotInstanceRequestsRequest describeRequest) {
 		try {
 			logger.println("Checking whether spot requests have been fulfilled");
 			DescribeSpotInstanceRequestsResult describeResult = ec2.describeSpotInstanceRequests(describeRequest);
 			List<SpotInstanceRequest> describeResponses = describeResult.getSpotInstanceRequests();
 
 			List<String> fulfilled = new LinkedList<String>();
 			for (SpotInstanceRequest describeResponse : describeResponses) {
 				if (describeResponse.getState().equals("open")) {
 					continue;
 				}
 				logger.println("Request fulfilled: " + 
 				describeResponse.getSpotInstanceRequestId() +
 				" Instance id : " + describeResponse.getInstanceId());
 				fulfilled.add(describeResponse.getInstanceId());
 				spotInstanceRequestIds.remove(describeResponse.getSpotInstanceRequestId());
 			}
 			
 			makeInstancesConnectBackOnJenkins(fulfilled, remainingSlaves);
 		} catch (Exception e) {
 			logger.printStackTrace(e);
 		}
 	}
 
 	private void makeInstancesConnectBackOnJenkins(List<String> fulfilledInstanceIds, LinkedList<EC2AbstractSlave> remainingSlaves) 
 			throws AmazonClientException, IOException {
 		if (fulfilledInstanceIds.size() == 0)
 			return;
 		assert(remainingSlaves.size() != 0);
 				
 		DescribeInstancesResult describeInstances = ec2.describeInstances(new DescribeInstancesRequest().withInstanceIds(fulfilledInstanceIds) );
 		List<Instance> instances = new ArrayList<Instance>();
 		
 		List<Reservation> reservations = describeInstances.getReservations();
 		for (Reservation reservation : reservations) {
 			instances.addAll(reservation.getInstances());
 		}
 		
 		logger.println("Count of instances to connect to: " + instances.size());
 		for (final Instance instance : instances) {
 			final EC2AbstractSlave slaveToAssociate = getSlaveToAssociate(instance.getSpotInstanceRequestId(), remainingSlaves);
 			if(slaveToAssociate == null){
 				String message = "SlaveToAssociate is null!!! "+instance.getInstanceId()+"/"+instance.getPrivateIpAddress();
 				logger.println(message);
 				throw new RuntimeException(message);
 			}
 			logger.println("Firing up connection for "+instance.getSpotInstanceRequestId()+" : "+instance.getInstanceId()+"/"+instance.getPrivateIpAddress());
 			new Thread(new Runnable() {  @Override public void run() {
 				associateSlaveToInstanceIpAddress(instance, slaveToAssociate);
 			}}).start();
 		}
 		logger.println("Done firing up threads to handle connections for " + StringUtils.join(fulfilledInstanceIds,", "));
 	}
 
 	private EC2AbstractSlave getSlaveToAssociate(String reqId, LinkedList<EC2AbstractSlave> remainingSlaves) {
 		for (EC2AbstractSlave ec2AbstractSlave : remainingSlaves) {
 			if (((EC2SpotSlave)ec2AbstractSlave).getSpotInstanceRequestId().equals(reqId)) {
 				return ec2AbstractSlave;
 			}
 		}
 		return null;
 	}
 
 	private void associateSlaveToInstanceIpAddress(Instance instance, EC2AbstractSlave slaveToAssociate) {
 		String privateIpAddress = instance.getPrivateIpAddress();
 		boolean success;
 		long timeout = EC2AxisCloud.getTimeout(slaveToAssociate);
 		int retryIntervalSecs = 5;
 		long retryIntervalMillis = TimeUnit2.SECONDS.toMillis(retryIntervalSecs);
 		long maxWait = System.currentTimeMillis() + timeout;
 		StopWatch stopwatch = new StopWatch();
 		stopwatch.start();
 		String slaveName = slaveToAssociate.getDisplayName();
 		
 		logger.println("Trying to connect Slave " + slaveName + " "+ slaveToAssociate.getLabelString() + " to "+privateIpAddress);
 		do{
 			success = tryToLaunchSlave(slaveToAssociate.getNodeName(), privateIpAddress);
 			try {
 				Thread.sleep(retryIntervalMillis);
 			} catch (InterruptedException e) {
 				logger.println("InterruptedException!!");
 				logger.printStackTrace(e);
 			}
 		} while(!success && System.currentTimeMillis() < maxWait );
 		
 		stopwatch.stop();
 		String slaveIdentString = slaveName+"/"+instance.getInstanceId()+"/"+privateIpAddress;
 		if(!success){
 			EC2AxisCloud.finishSlaveAndQueuedItems(slaveToAssociate);
 			logger.println("Slave " + slaveIdentString + " failed to come up after " + timeout + " ms");
 		}
 		else {
 			logger.println("It took " + stopwatch.getTime() + " ms to connect to "+ slaveIdentString);
 		}
 	}
 
 	private boolean tryToLaunchSlave(String slaveToAssociate, String privateIpAddress) {
 		String jenkinsUrl = Hudson.getInstance().getRootUrl();
 		
 		try {
 			Connection sshConnection = new Connection(privateIpAddress);
 			sshConnection.connect(new ServerHostKeyVerifier() {
 		        public boolean verifyServerHostKey(String hostname, int port, String serverHostKeyAlgorithm, byte[] serverHostKey) throws Exception {
 		            return true;
 		        }
 		    });
 			if (sshConnection.authenticateWithPublicKey(remoteAdmin, privateKey, "")) {
				logger.println("Will associate slave " + slaveToAssociate + " with instance with ip " + privateIpAddress);
 				
 				try {
 					startSlaveAgentOnRemoteInstance(slaveToAssociate, jenkinsUrl, sshConnection);
 					logger.println("Successfully connected to "+privateIpAddress);
 					return true; 
 				}catch(Exception e) {
 					return false;
 				}
 			}
 			else {
 				String message = "Could not connect with user " + remoteAdmin + " on " + privateIpAddress;
 				logger.println(message);
 				throw new RuntimeException(message);
 			}
 		}catch(Exception e) {
 			return false;
 		}
 	}
 
 	private void startSlaveAgentOnRemoteInstance(String slaveToAssociate, String jenkinsUrl, Connection sshConnection)
 					throws IOException,	InterruptedException {
 		Session openSession = sshConnection.openSession();
 		String wgetCmd = "wget " + jenkinsUrl + "jnlpJars/slave.jar -O slave.jar";
 		String encodedSlaveToAssociate = slaveToAssociate.replace(" ", "%20");
 		String slaveLaunch = "java -jar slave.jar -jnlpUrl \"" + jenkinsUrl + "computer/" + encodedSlaveToAssociate + "/slave-agent.jnlp\"";
 		String slaveLaunchCmd = "nohup " +slaveLaunch + " > slave.log 2> slave.err </dev/null &";
 		
 		execCommandAndWaitForCompletion(openSession, wgetCmd + " && " + slaveLaunchCmd);
 		openSession.close();
 	}
 
 	private void execCommandAndWaitForCompletion(Session openSession, String cmd) throws IOException, InterruptedException {
 		long timeoutForCommand = TimeUnit2.MINUTES.toMillis(5);
 		openSession.execCommand(cmd);
 		openSession.waitForCondition(ChannelCondition.EXIT_STATUS, timeoutForCommand);
 		Integer exitStatus = openSession.getExitStatus();
 		if(exitStatus != 0){
 			logger.println("Command failed: " + cmd);
 			throw new RuntimeException("Command failed: " + cmd);
 		}
 	}
 }
