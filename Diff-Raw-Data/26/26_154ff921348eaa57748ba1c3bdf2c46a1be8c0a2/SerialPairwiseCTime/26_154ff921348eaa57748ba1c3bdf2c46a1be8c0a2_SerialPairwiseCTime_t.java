 package statistics.treeShape;
 
 import java.io.PrintStream;
 import java.text.DecimalFormat;
 import java.text.NumberFormat;
 import java.util.ArrayList;
 import java.util.List;
 
 import population.Locus;
 
 import statistics.Histogram;
 import statistics.Options;
 import statistics.SerialTreeSampler;
 import statistics.Statistic;
 import statistics.TreeStatistic;
 import tree.DiscreteGenTree;
 
 
 /**
  * Computes the distribution of pairwise coalescent times for tips sampled at different times from a serially-sampled
  * tree. THIS ONLY WORKS IF THE TREE HAS TWO OR ONE SAMPLING PERIODS! IT'LL BREAK IF THERE ARE MORE THAN TWO!
  * @author brendan
  *
  */
 public class SerialPairwiseCTime extends TreeStatistic {
 
 	SerialTreeSampler sSampler;
 	Histogram histoDiff; //The histogram for pairs sampled at different times
 	Histogram histoSame; //The histogram for pairs sampled at the same time
 	
 	NumberFormat formatter = new DecimalFormat("#0.0##");
 	double lastMean;
 	
 	double histMin = 0;
 
 	double popSize = 1;
 	
 	public SerialPairwiseCTime(SerialTreeSampler sSampler) {
 		super();
 		this.sSampler = sSampler;
 		histoDiff = new Histogram(100, 0, 0.02);
 		histoSame = new Histogram(100, 0, 0.02);
 		sSampler.addTreeCollectionListener(this);
 		if (sSampler.getNumSamplingPeriods()>2) {
 			throw new IllegalArgumentException("The serial pc. time sampler only works with trees that have one or two sampling periods. This one has : " + sSampler.getNumSamplingPeriods());
 		}
 	}
 
 	
	public void clear() {
		histoDiff.clear();
		histoSame.clear();
	}
	
 	public void setPopSize(int N) {
 		popSize = N;
 	}
 	
 	public void collect(DiscreteGenTree tree) {
 		if (tree == null) 
 			return;
 		double count = 0;
 		
 		List<Locus> tips = tree.getTips();
 		ArrayList<Integer> tipTimes = new ArrayList<Integer>();
 		int treeHeight = tree.getMaxHeight();
 		for(int i=0; i<tips.size(); i++) {
 			tipTimes.add(treeHeight - tips.get(i).distToRoot());
 			tips.get(i).setDepth(treeHeight - tips.get(i).distToRoot());
 		}
 		
 		for(int i=0; i<tips.size(); i++) {
 			for(int j=i+1; j<tips.size(); j++) {
 				if (tips.get(i).getDepth()!=tips.get(j).getDepth()) {
 					int ctime = calcCTime(tips.get(i), tips.get(j));
 					lastMean += ctime;
 					count++;
 					histoDiff.addValue(new Double(ctime)/popSize);
 				}
 				else {
 					int ctime = calcCTime(tips.get(i), tips.get(j));
 					histoSame.addValue(new Double(ctime)/popSize);
 				}
 			}
 		}
 		
 		lastMean /= count;
 	}
 	
 	
 	private int calcCTime(Locus indA, Locus indB) {
 		int gens = 0;
 		
 		if (indA.getDepth()<indB.getDepth()) {
 			int dif = indB.getDepth()-indA.getDepth();
 			for(int i=0; i<dif; i++) {
 				indA = indA.getParent();
 				//gens++;
 			}
 		}
 		else {
 			int dif = indA.getDepth()-indB.getDepth();
 			for(int i=0; i<dif; i++) {
 				indB = indB.getParent();
 				//gens++;
 			}
 		}
 		
 		//Now indA and indB should be in the same generation
 		while(indA.getParent()!=indB.getParent()) {
 			indA = indA.getParent();
 			indB = indB.getParent();
 			gens++;
 			if (indA==null || indB==null) {
 				System.out.println("Yikes, found a null individual - probably indA and indB are not in the same generation.");
 			}
 		}
 		
 		return gens;
 	}
 
 
 	public boolean showOnScreenLog() {
 		return true;
 	}
 	
 	public String getScreenLogStr() {
 		return formatter.format(lastMean);
 	}
 
 	public String getDescription() {
 		return "Calculates coalescent rate for individuals sampled at different time points";
 	}
 
 
 	public String getIdentifier() {
 		return "Serial Pairwise Coal. Time";
 	}
 
 	
 	public void emitHistogram(int bins, double min, double max, PrintStream out) {
 		out.println("Histogram for individuals sampled at DIFFERENT TIMES:");
 		String histoStr = histoDiff.toString();
 		out.println(histoStr);
 		
 		out.println("\n Histogram for individuals sampled at THE SAME TIME:");
 		histoStr = histoSame.toString();
 		out.println(histoStr);
    }
 	
 	public double getMean() {
 		return histoDiff.getMean();
 	}
 
 	@Override
 	public Statistic getNew(Options ops) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 	
 	
 	public boolean collectDuringBurnin() {
 		return false;
 	}
 	
 	/**
 	 * Since we listen to the SerialTreeSampler for collection events we don't want
 	 * to be called by a PopulationRunner 
 	 */
 	public boolean useDefaultCollectionFrequency() {
 		return false;
 	}
 	
 	/**
 	 * We never want .collect to get called by the PopulationRunner, we only listen for tree collection events
 	 * @return
 	 */
 	public int getSampleFrequency() {
 		return Integer.MAX_VALUE;
 	}
 }
