 package aic12.project3.service.loadBalancing;
 
 import org.apache.log4j.Logger;
 
 import aic12.project3.service.requestManagement.RequestQueueReady;
 import aic12.project3.service.statistics.Statistics;
 import aic12.project3.service.util.FifoWithAverageCalculation;
 import aic12.project3.service.util.LoggerLevel;
 import aic12.project3.service.util.ManagementLogger;
 
 public class BalancingAlgorithmKeepQueueConstantImpl_Thread extends Thread {
 
 	private boolean continueRunning = true;
 	private Statistics statistics;
 	private Logger log = Logger.getLogger(BalancingAlgorithmKeepQueueConstantImpl_Thread.class);
 	private RequestQueueReady requestQReady;
 	private IHighLevelNodeManager highLvlNodeMan;
 	private ManagementLogger managementLogger;
 	private String clazz = this.getClass().getName();
 	private long updateInterval = 2000;
 	private FifoWithAverageCalculation fifo = new FifoWithAverageCalculation(100);
 	private LoadBalancerTime loadBalancer;
 	
 	public BalancingAlgorithmKeepQueueConstantImpl_Thread(
 			Statistics statistics, RequestQueueReady requestQReady,
 			IHighLevelNodeManager highLvlNodeMan,
 			ManagementLogger managementLogger, LoadBalancerTime loadBalancer) {
 		super();
 		this.statistics = statistics;
 		this.requestQReady = requestQReady;
 		this.highLvlNodeMan = highLvlNodeMan;
 		this.managementLogger = managementLogger;
 		this.loadBalancer = loadBalancer;
 	}
 
 
 	@Override
 	public void run() {
 //		long _effectiveUpdateInterval = updateInterval;
 		
 		while(continueRunning ) {
 //			long _updateStart = System.currentTimeMillis();
 			
 			statistics.calculateStatistics();
 			log .info(statistics);
 			int runningNodes = highLvlNodeMan.getRunningNodesCount();
 			int nodeStartupTime = highLvlNodeMan.getNodeStartupTime();
 			
 			double avgTweetProcessingDuration = statistics.getStatistics().getAverageTotalDurationPerTweet();
 //			long tweetsPerBalanceUpdateProcessedPerNode = (long) (_effectiveUpdateInterval / avgTweetProcessingDuration);
 //			long tweetsPerBalanceUpdateProcessedTotal = tweetsPerBalanceUpdateProcessedPerNode * runningNodes;
 			
 			long numTweetsInQ = requestQReady.getNumberOfTweetsInQueue();
 			
 			long expectedDuration = -1;
 			if(runningNodes == 0) {
 				expectedDuration = Long.MAX_VALUE;
 			} else {
 				expectedDuration = calculateExpDuration(numTweetsInQ, avgTweetProcessingDuration, runningNodes);
 			}
 			log.info("Status now:\n#tweetsInQ: " + numTweetsInQ + 
 					"\nrunningNodes: " + runningNodes + 
 					"\nnodeStartupTime: " + nodeStartupTime + 
 					"\nexpectedDuration: " + expectedDuration);
 			fifo.add(expectedDuration);
 
 			int desiredNodeCount = 0;
 			long queueIncreaseToAvg = expectedDuration - fifo.calculateAverage();
 			if(queueIncreaseToAvg > 0) {
 				int nodesToAdd = 0;
 				long newQDuration = -1;
 				do {
 					newQDuration = calculateExpDuration(numTweetsInQ, avgTweetProcessingDuration, runningNodes + nodesToAdd);
 					nodesToAdd++;
 				} while(newQDuration > fifo.calculateAverage());
 				nodesToAdd--; // last run of loop was too much;
 				
 				log.info("nodes to ADD: " + nodesToAdd);
 				desiredNodeCount = runningNodes + nodesToAdd;
 			} else {
 				int nodesToStop = 0;
 				long newQDuration = -1;
 				do {
 					newQDuration = calculateExpDuration(numTweetsInQ, avgTweetProcessingDuration, runningNodes - nodesToStop);
 					nodesToStop--;
 				} while(newQDuration < fifo.calculateAverage());
 				nodesToStop++; // last run of loop was too much;
 				
 				log.info("nodes to STOP: " + nodesToStop);
 			}
 			
 			managementLogger.log(clazz, LoggerLevel.INFO, "desiredNodes calculated: * " + desiredNodeCount + " * setting in nodeManager");
 			log.info("expectedDuration: " + calculateExpDuration(numTweetsInQ, avgTweetProcessingDuration, desiredNodeCount));
 			highLvlNodeMan.runDesiredNumberOfNodes(desiredNodeCount, loadBalancer);
 
 			
 			try {
 				Thread.sleep(updateInterval);
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 //			_effectiveUpdateInterval = _updateStart - System.currentTimeMillis();
 		}
 	}
 
 	private long calculateExpDuration(long numTweetsInQ, double avgTweetProcessingDuration, int nodes) {
		if(nodes == 0) {
			return Long.MAX_VALUE;
		}
 		return (long) (numTweetsInQ * avgTweetProcessingDuration) / nodes;
 	}
 	
 	
 	public void stopRunning() {
 		continueRunning = false;
 	}
 
 }
