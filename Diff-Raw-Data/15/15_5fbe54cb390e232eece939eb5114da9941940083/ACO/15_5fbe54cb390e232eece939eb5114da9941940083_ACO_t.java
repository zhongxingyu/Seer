 package aco;
 
 import java.util.Arrays;
 import java.util.Timer;
 import java.util.TimerTask;
 
 /**
  * The entry point for the Ant Colony Optimization algorithm.
  * 
  * @author Andre Luiz Verucci da Cunha
  * 
  */
 public class ACO extends Thread {
 
 	/**
 	 * The ants that will traverse the search space.
 	 */
 	private Ant[] mAnts;
 
 	/**
 	 * The QoS attributes to be considered.
 	 */
 	private QoSAttribute[] mQoSAttributes;
 
 	/**
 	 * The total QoS of each concrete service.
 	 */
 	private float[][] mTotalQoS;
 
 	/**
 	 * The amount of pheromone associated with each concrete service.
 	 */
 	private float[][] mPheromone;
 
 	/**
 	 * The evaporation coefficient of the pheromone.
 	 */
 	private float mRho;
 
 	/**
 	 * The maximum number of iterations. On each iteration, all the ants move
 	 * one step forward each.
 	 */
 	private int mMaxIterations;
 
 	/**
 	 * The minimum acceptable aggregated QoS of a solution.
 	 */
 	private float mMinAggregatedQoS;
 
 	/**
 	 * The number of iterations already performed.
 	 */
 	private volatile int mIterations;
 
 	/**
 	 * The most reinforced composition currently in the graph.
 	 */
 	private volatile int[] mCurrentSolution;
 
 	/**
 	 * The aggregated QoS of the current solution.
 	 */
 	private volatile float mCurrentAggregatedQoS;
 
 	/**
 	 * Creates an ACO instance.
 	 * 
 	 * @param noAnts
 	 *            The number of ants.
 	 * @param qosAttributes
 	 *            A vector containing the QoS attributes that will be used.
 	 * @param alpha
 	 *            The relative importance of the amount of pheromone.
 	 * @param beta
 	 *            The relative importance of the heuristic information (the
 	 *            total QoS).
 	 * @param rho
 	 *            The evaporation coefficient of the pheromone.
 	 * @param initialPheromone
 	 *            The initial amount of pheromone that will be deposited in each
 	 *            concrete service before the ants start to walk.
 	 * @param maxIterations
 	 *            The maximum number of iterations.
 	 * @param minQoS
 	 *            The minimum acceptable QoS.
 	 */
 	public ACO(int noAnts, QoSAttribute[] qosAttributes, float alpha,
 			float beta, float rho, float initialPheromone, int maxIterations,
 			float minQoS) {
 		mQoSAttributes = qosAttributes;
 
 		mPheromone = new float[qosAttributes[0].getValues().length][];
 		for (int i = 0; i < mPheromone.length; i++) {
 			mPheromone[i] = new float[qosAttributes[0].getValues()[i].length];
 			Arrays.fill(mPheromone[i], initialPheromone);
 		}
 
 		mTotalQoS = QoSAttribute.calculateTotalQoS(qosAttributes);
 		mAnts = new Ant[noAnts];
 		for (int i = 0; i < noAnts; i++) {
 			mAnts[i] = new Ant(mQoSAttributes, mTotalQoS, mPheromone, alpha,
 					beta);
 		}
 
 		mCurrentSolution = new int[mPheromone.length];
 		mMaxIterations = maxIterations;
 		mMinAggregatedQoS = minQoS;
 		mRho = rho;
 	}
 
 	@Override
 	public void run() {
 		mIterations = 0;
 		mCurrentAggregatedQoS = 0f;
 		Arrays.fill(mCurrentSolution, 0);
 
 		while (!shouldStop()) {
 			for (int i = 0; i < mAnts.length; i++) {
 				mAnts[i].walk();
 			}
 			updatePheromone();
 			updateCurrentSolution();
 			mIterations++;
 		}
 	}
 
 	/**
 	 * Runs ACO for a given amount of time.
 	 * 
 	 * @param millisTimeOut
 	 *            The execution timeout, in milliseconds.
 	 */
 	public void startWithTimeOut(long millisTimeOut) {
		mMaxIterations = -1;
		mMinAggregatedQoS = -1;
		
 		Timer timer = new Timer();
 		TimerTask task = new TimerTask() {
 
 			@Override
 			public void run() {
 				ACO.this.interrupt();
 			}
 		};
 
 		timer.schedule(task, millisTimeOut);
 		start();
 	}
 
 	/**
 	 * 
 	 * @return True if the stop condition is satisfied; false otherwise.
 	 */
 	private boolean shouldStop() {
 		if (isInterrupted()) {
 			return true;
 		} else if (mMaxIterations < 0 && mMinAggregatedQoS < 0) {
 			/* Run forever. */
 			return false;
 		} else if (mMaxIterations > 0 && mMinAggregatedQoS < 0) {
 			/* Run for a fixed amount of iterations. */
 			return mIterations >= mMaxIterations;
 		} else if (mMaxIterations < 0 && mMinAggregatedQoS > 0) {
 			/* Run until a solution with an acceptable QoS is found. */
 			return mCurrentAggregatedQoS >= mMinAggregatedQoS;
 		} else { // mMaxIterations > 0 && mMinQoS > 0
 			/*
 			 * Run until an acceptable solution is found OR a maximum number of
 			 * iterations is reached.
 			 */
 			return mCurrentAggregatedQoS >= mMinAggregatedQoS
 					|| mIterations >= mMaxIterations;
 		}
 	}
 
 	/**
 	 * After each iteration, evaporates and deposits proper amounts of
 	 * pheromone.
 	 */
 	private void updatePheromone() {
 		for (int i = 0; i < mPheromone.length; i++) {
 			for (int j = 0; j < mPheromone[i].length; j++) {
 				mPheromone[i][j] = (1 - mRho) * mPheromone[i][j];
 			}
 		}
 
 		for (Ant a : mAnts) {
 			float newPheromone = a.getNewPheromone();
 			int[] solution = a.getSolution();
 
 			if (solution == null) {
 				continue;
 			}
 
 			for (int i = 0; i < solution.length; i++) {
 				mPheromone[i][solution[i]] += newPheromone;
 			}
 		}
 	}
 
 	/**
 	 * Computes and stores the current solution.
 	 */
 	private void updateCurrentSolution() {
 		for (int i = 0; i < mPheromone.length; i++) {
 			int indexOfMaxPheromone = 0;
 			float maxPheromone = 0;
 			for (int j = 0; j < mPheromone[i].length; j++) {
 				if (mPheromone[i][j] > maxPheromone) {
 					maxPheromone = mPheromone[i][j];
 					indexOfMaxPheromone = j;
 				}
 			}
 
 			mCurrentSolution[i] = indexOfMaxPheromone;
 		}
 		mCurrentAggregatedQoS = QoSAttribute.calculateAggregatedQoS(
 				mQoSAttributes, mCurrentSolution);
 	}
 
 	/**
 	 * 
 	 * @return The solution found.
 	 */
 	public int[] getSolution() {
 		return mCurrentSolution;
 	}
	
	/**
	 * 
	 * @return The number of iterations performed.
	 */
	public int getNoIterations() {
		return mIterations;
	}
 
 	public static void main(String[] args) {
 		float[][] values = { { 1, 0.5f, 1 }, { 1, 0.5f }, { 0.5f, 0.5f, 1 } };
 
 		QoSAttribute attrSum = new QoSAttribute(values,
 				QoSAttribute.AGGREGATE_BY_SUM, 0.2f);
 		QoSAttribute attrProd = new QoSAttribute(values,
 				QoSAttribute.AGGREGATE_BY_PRODUCT, 0.3f);
 		QoSAttribute attrAvg = new QoSAttribute(values,
 				QoSAttribute.AGGREGATE_BY_AVERAGE, 0.5f);
 
 		QoSAttribute[] attrs = { attrSum, attrProd, attrAvg };
 
 		float alpha = 1;
 		float beta = 1;
 		float rho = 0.1f;
 
		ACO aco = new ACO(20, attrs, alpha, beta, rho, 1, 20, 1.0f);
 		aco.start();
 		try {
 			aco.join();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 
 		System.out.println(Arrays.toString(aco.mCurrentSolution));
 		System.out.println(QoSAttribute.calculateAggregatedQoS(attrs,
 				aco.mCurrentSolution));
 		System.out.println(aco.mIterations);
 	}
 }
