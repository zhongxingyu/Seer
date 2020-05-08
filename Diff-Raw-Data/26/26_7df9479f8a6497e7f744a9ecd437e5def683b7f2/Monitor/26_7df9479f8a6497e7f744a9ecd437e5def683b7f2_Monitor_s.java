 package ch.uzh.agglorecommender.clusterer;
 
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.Serializable;
 import java.util.List;
 
 import ch.uzh.agglorecommender.util.LimitedQueue;
 import ch.uzh.agglorecommender.util.TBLogger;
 
 /**
  * Class that keeps current count of nodes in the tree
  * Can be used for other data saving during clustering.
  *
  */
 public class Monitor implements Serializable {
 
 	/**
 	 * Determines if a de-serialized file is compatible with this class.
 	 * <br>
 	 * <br>
 	 * Maintainers must change this value if and only if the new version
 	 * of this class is not compatible with old versions.
 	 */
 	private static final long serialVersionUID = 1L;
 	
 	private static Monitor monitor = new Monitor();
 	
 	private long openContentNodes = 0;
 	private long openUserNodes = 0;
 	private long cycles = 0;
 	private long startTime = 0;
 	private long timeOfLastCycle = 0;
 	private List<Integer> expectationQueue = new LimitedQueue(10);
 	
 	/**
 	 * Constructor to establish node counts and display
 	 */
 	private Monitor () {
 		// singleton
 	}
 	
 	public static Monitor getInstance() {
 		return monitor;
 	}
 	
 	/**
 	 * Initialize the counter on start of clustering process.
 	 * 
 	 * @param Count of userNodes in userNode Set 
 	 * @param Count of contentNodes in contentNode Set
 	 */
 	public void initMonitoring(int openUserNodes,int openContentNodes) {
 		setInitialCounts(openContentNodes,openUserNodes);
 		this.openUserNodes 		= openUserNodes;
 		this.openContentNodes 	= openContentNodes;
		this.startTime = System.currentTimeMillis();
 	}
 	
 	/**
 	 * Set the initial number of content and user nodes.
 	 * 
 	 * @param contentNodeCounts number of content nodes
 	 * @param userNodeCounts number of user nodes
 	 */
 	public void setInitialCounts(long contentNodeCounts, long userNodeCounts) {
 		this.openContentNodes = contentNodeCounts;
 		this.openUserNodes = userNodeCounts;
 	}
 	
 	private void addCycle() {	
 		timeOfLastCycle = System.currentTimeMillis();
 		TBLogger.getLogger(getClass().getName()).info("cycle nr: "+ cycles);
 		cycles++;
 //		TBLogger.getLogger(getClass().getName()).info("ID: " + serialVersionUID);
 	}
 	
 	public long getCycleCount() {
 //		TBLogger.getLogger(getClass().getName()).info("cycle nr: "+ cycles);
 //		TBLogger.getLogger(getClass().getName()).info("ID: " + serialVersionUID);
 		return cycles;
 	}
 	
     /*
      * Calculates elapsed time since start
      */
 	public long getElapsedTime() {
 		return (long) (((double)(System.currentTimeMillis() - this.startTime)) / 1000.0);
 	}
 	
     /*
      * Calculates number of total nodes
      */
 	public long getTotalOpenNodes() {
     	
 		long totalNodes = 0;
     	totalNodes += openContentNodes;
     	totalNodes += openUserNodes;
     	
     	return totalNodes;
 	}
 	
 	/*
 	 * Delivers total comparisons
 	 */
 	
 	public long getComparisonsOnLevel(double i) {
 		
 		double totalNodes = getTotalOpenNodes() + getCycleCount();
 		double openNodesOnLevel = totalNodes - i;
 				
 		long n = (long) (openNodesOnLevel - 1);
 		long comparisons =  (long) (n*(n+1))/2;
 		
 		return comparisons;
 	}
 	
 	public long getComparisonSum(double start, double end){
 		
 		long comparisons = 0;
 		
 		for(double i = start;i < end; i++){
 			comparisons += getComparisonsOnLevel(i);
 		}
 		
 		return comparisons;
 		
 	}
 	
 	public long getProcessedComparisons() {
 		
 		double start = 0; 
 		double end = getCycleCount();		
 		
 		return getComparisonSum(start,end);
 	}
 	
 	public long getExpectedFutureComparisons() {
 		
 		double totalNodes = getTotalOpenNodes() + getCycleCount();
 		
 		double start = getCycleCount();
 		double end = totalNodes;
 		
 		return getComparisonSum(start,end);	
 	}
 	
     /*
 	 * Calulates the time used for Merge
 	 */
 	public double getTimePerMerge() {
 		if(getElapsedTime() > 0){
 			double timePerMerge =  (double) getCycleCount() / (double) getElapsedTime();
 			return timePerMerge;
 		}
 		return 0;
 	}
 	
 	public double getComparisonsPerSecond() {
 		
 		long compOnLevel = getComparisonsOnLevel(getCycleCount());
 		
 		if(compOnLevel > 0){
 			double timePerComparison = (double) getTimePerMerge() / (double) compOnLevel;
 			return 1 / timePerComparison;
 		}			
 		return 0;
 	}
     
     /*
      * Calulates the percentage of comparisons actually calculated
      */
     public double getPercentageOfComparisons() {
     	
 		long processed = getProcessedComparisons();
 		long future = getExpectedFutureComparisons();
 		
     	if(processed + future > 0){
     		double percentage = (double) processed  / ((double) processed  + future);
     		return percentage * 100;
     	}
     	return 0;
     }
     
     public int getTotalExpectedSeconds() {
 	    
     	double percentage = getPercentageOfComparisons();
     	
     	if(percentage > 0){
 	    	
 	    	double multiplier = 100 / percentage;
 	    	double total = getElapsedTime() * multiplier;
 	    	int expTime = (int) (total - getElapsedTime());
 	    	
	    	return expTime;
 	    	
//	    	expectationQueue.add(expTime);
//	    	int sumOfExpTime = 0;
//	    	for(int expectation : expectationQueue){
//	    		sumOfExpTime += expectation;
//	    	}
//	    	
//	    	return (int) sumOfExpTime / expectationQueue.size();
     	}
     	return 0;
 	}
 
 	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException { 
     	ois.defaultReadObject();
     	Monitor c = Monitor.getInstance();
     	
     	c.openContentNodes 	= this.openContentNodes;
     	c.openUserNodes 	= this.openUserNodes;
     	c.cycles 			= this.cycles;
     	c.startTime 		= this.startTime + (System.currentTimeMillis() - this.timeOfLastCycle);
     }
 
 	public void update(int openUserNodes, int openContentNodes) {
 		
 		// Update Cycle
 		addCycle();
 		
 		// Update Open Nodes
 		this.openUserNodes = openUserNodes;
 		this.openContentNodes = openContentNodes;
 	}
 
 }
