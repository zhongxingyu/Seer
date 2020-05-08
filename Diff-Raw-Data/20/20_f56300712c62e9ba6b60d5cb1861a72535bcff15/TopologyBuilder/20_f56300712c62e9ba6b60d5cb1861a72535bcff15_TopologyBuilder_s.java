 package ch.uzh.ddis.katts;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 
 import javax.management.RuntimeErrorException;
 
 import org.apache.zookeeper.txn.CreateTxn;
 
 import backtype.storm.Config;
 import backtype.storm.generated.StormTopology;
 
 import ch.uzh.ddis.katts.query.Node;
 import ch.uzh.ddis.katts.query.Query;
 
 public class TopologyBuilder extends backtype.storm.topology.TopologyBuilder {
 
 	private Config configuration = null;
 
 	public static final String KATTS_FACTOR_OF_THREADS_CONFIG = "katts_factor_of_threads_config";
 	public static final String KATTS_EXPECTED_NUMBER_OF_EXECUTORS = "katts_expected_number_of_executors_config";
 	public static final String NUMBERS_OF_PROCESSORS = "katts_number_of_processors_config";
 
 	public TopologyBuilder(Config conf) {
 		this.configuration = conf;
 	}
 
 	private Query query = null;
 
 	private int parallelism = 10;
 
 	private int numberOfProcessors = 0;
 
 	/**
 	 * This variable determines how many threads compared to workers are created. A value of 2.5 means that the system
 	 * will adapt the parallelism in the way that in the end 2.5 more threads exists than workers.
 	 */
 	public float factorOfThreadsPerProcessor = 1.1f;
 
 	public Query getQuery() {
 		return query;
 	}
 
 	public TopologyBuilder setQuery(Query query) {
 		this.query = query;
 		updateConfig();
 		return this;
 	}
 
 	public int getParallelism() {
 		return parallelism;
 	}
 
 	public void setParallelism(int parallelism) {
 		this.parallelism = parallelism;
 	}
 
 	/**
 	 * This method sets the parallelism to a optimal value depending on the number of workers. The constant
 	 * factorOfThreadsPerProcessor controls the behavior of this method.
 	 * 
 	 * @param numberOfProcessors
 	 */
 	public void setParallelismByNumberOfProcessors(int numberOfProcessors) {
 		this.numberOfProcessors = numberOfProcessors;
 //		int expectedNumberOfInfiniteParallelNodes = getNumberOfInfiniteParallelizedNodes();
 		float sumOverAllWeightsOfNonFixedParallelizedNodes = getSumOfAllParallelizationWeights();
 		int numberOfWorkersPretermined = getFixParallelization();
 
 		float parallelism = (factorOfThreadsPerProcessor * (float) numberOfProcessors - numberOfWorkersPretermined)
 				/ sumOverAllWeightsOfNonFixedParallelizedNodes;
 		this.setParallelism(Math.round(Math.max(parallelism, 1)));
 		updateConfig();
 	}
 
 	/**
 	 * This method returns the number of tasks that are expected to be created. This value is based on the estimated for
 	 * the nodes that has an infinite parallelization defined. This value may differ from the effective value.
 	 * 
 	 * @return
 	 */
 	public long getEstimatedNumberOfExecutors() {
 
 		return (long)(this.numberOfProcessors * this.factorOfThreadsPerProcessor);
 	}
 
 	/**
 	 * This method returns the number of tasks that must be initialized for sure, due to the fixed parallelization
 	 * specified by the nodes. This method does not include the infinite parallelized nodes.
 	 * 
 	 * @return
 	 */
 	private int getFixParallelization() {
 		int numberOfWorkersPretermined = 0;
 
 		for (Node node : query.getNodes()) {
 			int nodeParallelism = node.getParallelism();
 			if (nodeParallelism >= 1) {
 				numberOfWorkersPretermined += nodeParallelism;
 			}
 		}
 
 		return numberOfWorkersPretermined;
 	}
 
 	/**
 	 * This method updates the configuration values, depending of the current setup of the TopologyBuilder.
 	 */
 	private void updateConfig() {
 		this.configuration.put(KATTS_FACTOR_OF_THREADS_CONFIG, (double)this.getFactorOfThreadsPerProcessor());
 		this.configuration.put(KATTS_EXPECTED_NUMBER_OF_EXECUTORS, this.getEstimatedNumberOfExecutors());
 		this.configuration.put(NUMBERS_OF_PROCESSORS, numberOfProcessors);
 	}
 
 	/**
 	 * This method returns the number of nodes, that has not specified a parallelization. This means that this nodes has
 	 * at least a parallelization of one and at most infinite parallelization.
 	 * 
 	 * @return
 	 */
 	private int getNumberOfInfiniteParallelizedNodes() {
 		int expectedNumberOfInfiniteParallelNodes = 0;
 
 		for (Node node : query.getNodes()) {
 			int nodeParallelism = node.getParallelism();
 			if (nodeParallelism < 1) {
 				expectedNumberOfInfiniteParallelNodes++;
 			}
 		}
 
 		return expectedNumberOfInfiniteParallelNodes;
 	}
 	
 	private float getSumOfAllParallelizationWeights() {
 		
 		float sum = 0;
 		
 		for (Node node : query.getNodes()) {
 			int nodeParallelism = node.getParallelism();
 			if (nodeParallelism < 1) {
 				sum += getParallelizationWeightByNode(node);
 			}
 		}
 		
 		return sum;
 	}
 	
 	public float getParallelizationWeightByNode(Node node) {
 		try {
			Method method = node.getClass().getDeclaredMethod("getParallelismWeight");
 			try {
 				return (Float)method.invoke(node);
 			} catch (IllegalArgumentException e) {
 				throw new RuntimeException("The Method getParallelismWeight should not require any argument.", e);
 			} catch (IllegalAccessException e) {
 				throw new RuntimeException("The Method getParallelismWeight should be public.", e);
 			} catch (InvocationTargetException e) {
 				throw new RuntimeException("Can't invoke the method getParallelismWeight.", e);
 			}
 		} catch (SecurityException e) {
 			throw new RuntimeException(String.format("Could not access the method getParallelismWeight on class %1s.", node.getClass().getCanonicalName()), e);
		} catch (NoSuchMethodException e) {
			// Ignore, we set here the weight by default to 1
			return 1;
		}
 	}
 
 	public StormTopology createTopology() {
 
 		for (Node node : query.getNodes()) {
 			node.createTopology(this);
 		}
 
 		StormTopology topology = super.createTopology();
 
 		// TODO: Optimize the resulting storm topology (e.g. MultiBolts)
 
 		return topology;
 	}
 
 	public float getFactorOfThreadsPerProcessor() {
 		return factorOfThreadsPerProcessor;
 	}
 
 	public void setFactorOfThreadsPerProcessor(float factorOfThreadsPerProcessor) {
 		this.factorOfThreadsPerProcessor = factorOfThreadsPerProcessor;
 		updateConfig();
 	}
 
 }
