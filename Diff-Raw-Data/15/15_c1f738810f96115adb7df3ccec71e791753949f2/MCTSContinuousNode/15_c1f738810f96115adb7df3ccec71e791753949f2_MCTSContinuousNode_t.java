 package ssamot.mcts.ucb.optimisation;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 
 import ssamot.mcts.StatisticsNode;
 import ssamot.utilities.ArraysCopy;
 import ssamot.utilities.MersenneTwister;
 import ssamot.utilities.SummaryStatistics;
 
 public class MCTSContinuousNode extends StatisticsNode {
 
 	public static MersenneTwister twister = new MersenneTwister();
 
 	protected double action = -1;
 
 	boolean hasBeenSplit = false;
 
 	public boolean isHasBeenSplit() {
 		return hasBeenSplit;
 	}
 
 	public String contId = "default";
 
 	// includes min
 	protected double min[];
 	// does not include max
 	protected double max[];
 	protected int splitPoints = 2;
 
 	private int point = -1;
 
 	private double oldAction;
 
 	private double sigma = 1.5;
 
 	double oldValue = Double.NEGATIVE_INFINITY;
 
 	int timeshit = 0;
 
 	private int depth = 0;
 
 	private int maxDepth;
 
 	private double B;
 
	private boolean propagateSamples = false;
 
	private HashMap<double[], Double> samples;
 
 	public MCTSContinuousNode(double[] min, double max[], int splitPoints,
 			int point, int depth, int maxDepth) {
 		this.min = min;
 		this.max = max;
 		this.splitPoints = splitPoints;
 		this.point = point;
 		this.actionStatistics = new SummaryStatistics();
 		this.depth = depth;
 		this.maxDepth = maxDepth;
 		this.autoGenerateChildren = false;
 		this.B = (double) Integer.MAX_VALUE - twister.nextDouble();
 
 	}
 
 	public void addSample(double[] sample, double reward) {
 		if (propagateSamples) {
 			if (samples == null) {
 				samples = new HashMap<double[], Double>();
 			}
 			samples.put(sample, reward);
 		}
 
 	}
 
 	public double[] getMin() {
 		return min;
 	}
 
 	public double[] getMax() {
 		return max;
 	}
 
 	public int getSplitPoints() {
 		return splitPoints;
 	}
 
 	public double getContinuousAction() {
 		return action;
 	}
 
 	@Override
 	public int getAction() {
 		// TODO Auto-generated method stub
 		return point;
 	}
 
 	public double[] sampleAction() {
 		double[] sample = new double[min.length];
 		for (int i = 0; i < min.length; i++) {
 			sample[i] = min[i] + Math.abs(max[i] - min[i])
 					* twister.nextDouble();
 		}
 
 		return sample;
 		// action = min + Math.abs(max - min) / 2.0;
 	}
 
 	public void split() {
 
 		if (depth > maxDepth) {
 
 			return;
 
 		}
 
 		if (!hasBeenSplit) {
 
 			children = new ArrayList<StatisticsNode>();
 
 			// choose a dimension at random
 
 			int splitDim = twister.nextInt(min.length);
 			// System.out.println(splitDim);
 			double interval = Math.abs(max[splitDim] - min[splitDim])
 					/ (double) splitPoints;
 
 			HashMap<double[], Double>[] childrenSamples = new HashMap[2];
 			childrenSamples[0] = new HashMap<double[], Double>();
 			childrenSamples[1] = new HashMap<double[], Double>();
 
			if (samples != null && samples.size() >= 1) {
 
 				for (double[] sample : samples.keySet()) {
 					if (sample[splitDim] <= min[splitDim] + interval) {
 						childrenSamples[0].put(sample, samples.get(sample));
 					} else {
 						childrenSamples[1].put(sample, samples.get(sample));
 					}
 				}
				//System.err.println(samples.keySet().size());
 			}
 
		
			//System.err.println(childrenSamples[0].keySet().size());
			//System.err.println(childrenSamples[1].keySet().size());
 			for (int i = 0; i < splitPoints; i++) {
 
 				double nMinD = min[splitDim] + i * interval;
 				double nMaxD = min[splitDim] + (i + 1) * interval;
 
 				double[] nMin = ArraysCopy.fastShallowArrayCopy(min);
 				double[] nMax = ArraysCopy.fastShallowArrayCopy(max);
 				nMin[splitDim] = nMinD;
 				nMax[splitDim] = nMaxD;
 
 				// System.out.println(Arrays.toString(nMin) +
 				// Arrays.toString(nMax));
 
 				MCTSContinuousNode node = new MCTSContinuousNode(nMin, nMax,
 						splitPoints, i, depth + 1, maxDepth);
 				node.actionStatistics = new SummaryStatistics();
 
 				if (propagateSamples) {
 
 					node.samples = childrenSamples[i];
 					node.recalculateStatistics();
 				}
 				// node.actionStatistics.addValue(actionStatistics.getMean());
 				node.contId = contId;
 				children.add(node);
 			}
 			// System.out.println("");
 			hasBeenSplit = true;
 		}
 
 	}
 
 	private void recalculateStatistics() {
 		for (double value : samples.values()) {
 			actionStatistics.addValue(value);
 		}
 
 		//System.out.println(actionStatistics.getN());
 	}
 
 	public int getDepth() {
 		return depth;
 	}
 
 	@Override
 	public int getRewardId() {
 		// TODO Auto-generated method stub
 		return 1;
 	}
 
 	@Override
 	public boolean isLeaf() {
 		if (isHasBeenSplit()) {
 			return false;
 		} else {
 			return true;
 		}
 	}
 
 	@Override
 	public void generateChildren() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public boolean canBeEvaluated() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public double evaluate(int player) {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	@Override
 	public double evaluateDefaultPolicy(int player) {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	@Override
 	public int getGameTotalGamePlayers() {
 		// TODO Auto-generated method stub
 		return 2;
 	}
 
 	@Override
 	public String toString() {
 		return "Av [ac=" + String.format("%1.5f", actionStatistics.getMean())
 				+ "," + contId + "]";
 	}
 
 	public void setB(double b) {
 		this.B = b;
 
 	}
 
 	public double getB() {
 		return this.B;
 	}
 
 }
