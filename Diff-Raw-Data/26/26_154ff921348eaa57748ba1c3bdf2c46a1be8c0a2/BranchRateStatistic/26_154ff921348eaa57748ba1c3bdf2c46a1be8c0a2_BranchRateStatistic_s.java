 package statistics;
 
 import java.io.PrintStream;
 import java.util.ArrayList;
 import java.util.Stack;
 
 import javax.swing.JFrame;
 
 import population.Locus;
 
 import dnaModels.DNASequence;
 
 import tree.DiscreteGenTree;
 
 
 public class BranchRateStatistic extends TreeStatistic {
 
 	public static final String identifier = "Rate-depth relationship";
 
 	int calls = 0;
 	int startSite = 0;
 	int endSite = 0;
 	ArrayList<Pair> rateHisto;
 	ArrayList<Pair> fitHisto;
 	
 
 	public BranchRateStatistic() {
 
 	}
 	
 	/**
 	 * We need ancestral info
 	 */
 	public boolean requiresGenealogy() {
 		return true;
 	}
 	
 	private BranchRateStatistic(int startPos, int endPos) {
 		startSite = startPos;
 		endSite = endPos;
 		rateHisto = new ArrayList<Pair>();
 		fitHisto = new ArrayList<Pair>();
 	}
 	
 	public BranchRateStatistic getNew(Options ops) {
 		this.options = ops;
 		if (ops != null) {
 			Integer[] pos = (Integer[])ops.optData;
 			Integer start = pos[0];
 			Integer endPos = pos[1];
 			return new BranchRateStatistic(start, endPos);
 		}
 		else
 			return new BranchRateStatistic(0, 0);	
 	}
 	
 	public boolean collectDuringBurnin() {
 		return false;
 	}
 	
 	
 	/**
 	 * True if a configuration tool is available for this statistic
 	 * @return
 	 */
 	public boolean hasConfigurationTool() {
 		return true;
 	}
 	
 	/**
 	 * Returns a GUI component that allows the user to configure some options of this statistic
 	 * @return
 	 */
 	public JFrame getConfigurationTool() {
 		return new NoHistoConfigTool(this);
 	}
 	
 	public String getIdentifier() {
 		return identifier;
 	}
 	
 	
 	public void summarize(PrintStream out) {
 		out.println("Summary for Rate-Depth calculator");
 		
 		double difSum = 0;
 		double fitSum = 0;
 		out.println("Depth (gens) \t Mean mut. differences \t Mean fitness dif");
 		for(int i=0; i<rateHisto.size(); i++) {
 			difSum += rateHisto.get(i).rate/(double)rateHisto.get(i).count;
 			fitSum += fitHisto.get(i).rate/(double)fitHisto.get(i).count;
 			if (i%10==0) {
 				out.println(i + "\t" + difSum/10.0 + "\t" + fitSum/10.0 + "\t" + fitHisto.get(i).count);
 				difSum = 0;
 				fitSum = 0;
 			}
 		}
 	}
 	
 	public void collect(DiscreteGenTree tree) {
 		
 		if (tree == null || tree.getRoot() == null)
 			return;
 		
 		if (tree.getRoot().getPrimaryDNA() == null) {
 			return;
 		}
 
 		traverseAddRates(tree.getRoot()); //Traverse the tree and add rates to the histogram
 	}
 	
 	public void emitAll() {
 		double sum = 0;
 		for(int i=0; i<rateHisto.size(); i++) {
 			sum += rateHisto.get(i).rate/(double)rateHisto.get(i).count;
 			if (i%10==0) {
 				output.println(i + "\t" + sum/10.0);
 				sum = 0;
 			}
 		}
 	}
 	
 	private void traverseAddRates(Locus root) {
 		//Must do an iterative tree traversal to avoid a bagillion recursive calls...
 		Locus current = root;
 		Locus p = root;
 		
 		int maxDepth = 0;
 		while(p.numOffspring()>0) {
 			p = p.getOffspring(0);
 			maxDepth++;
 		}
 		
 		Stack<Locus> stack = new Stack<Locus>();
 
 		root.setDepth(maxDepth);
 		stack.add(root);
 
 		//int depth = maxDepth;
 		while(stack.size()>0) {
 			current = stack.pop();
 			addRateToHistogram(current);
 			
 			ArrayList<Locus> kids = current.getOffspring();
 			for(Locus kid : kids) {
 				kid.setDepth(current.getDepth()-1);
 			}
 			stack.addAll(kids);
 		}
 		
 
 	}
 	
 	/**
 	 * We need to keep the ancestral DNA data around so we can compare kids to offspring. 
 	 */
 	public boolean requiresPreserveAncestralData() {
 		return true;
 	}
 	
 	/**
 	 * Compares 'node' to all of its offspring and adds the number of DNA differences and the
 	 * (mean) difference in fitness between them to the appropriate histograms 
 	 * @param node
 	 */
 	private void addRateToHistogram(Locus node) {
 		double difSum = 0; //Total number of differences between this node and it's offspring's DNA
 		double fitSum = 0; //Difference in fitness between this node and it's offspring
 		int depth = node.getDepth();
 		if (node.numOffspring() == 0) {
 			return;
 		}
 		
 		double divisor;
 		if (startSite ==0 && endSite == 0)
 			divisor = node.getPrimaryDNA().length();
 		else {
 			divisor = endSite - startSite;
 		}
 		
 		for(Locus kid : node.getOffspring()) {
 			difSum += countDifs(node.getPrimaryDNA(), kid.getPrimaryDNA())/divisor;
 			fitSum += node.getFitness() - kid.getFitness();
 		}
 		double rate = difSum / (double)node.numOffspring();
 		double fitDif = fitSum / (double)node.numOffspring();
 		
 		while( rateHisto.size() <= depth ) {
 			rateHisto.add(new Pair(0, 0));
 		}
 		
 		while( fitHisto.size() <= depth ) {
 			fitHisto.add(new Pair(0, 0));
 		}
 		
 		rateHisto.get(depth).rate += rate;
 		rateHisto.get(depth).count++;
 		
 		fitHisto.get(depth).rate += fitDif;
 		fitHisto.get(depth).count++;		
 		//System.out.println("Adding rate "  + rate + " to depth : " + depth);
 	}
 	
 
 	/**
 	 * Returns the total number of sites which differ between two DNA sequences
 	 * @param one
 	 * @param two
 	 * @return
 	 */
 	private int countDifs(DNASequence one, DNASequence two) {
 		int sum = 0;
 		
 		if (startSite == 0 && endSite == 0) {
 			for(int i=0; i<Math.min(one.length(), two.length()); i++) {
 				if ( one.getBaseChar(i) != two.getBaseChar(i)) 
 					sum++;
 			}
 			
 		} else {
 			
 			for(int i=startSite; i<Math.min(one.length(), endSite); i++) {
 				if ( one.getBaseChar(i) != two.getBaseChar(i)) 
 					sum++;
 			}
 			
 		}
 		
 		return sum;
 	}
 	
 	
 	public String getDescription() {
 		if (startSite == 0 && endSite==0)
 			return "Tree branch evolutionary rate calculator for all sites.";
 		else
 			return "Tree branch evolutionary rate calculator for sites " + startSite + " - " + endSite;
 	}
 
 	class Pair {
 		double rate= 0;
 		double count = 0;
 		
 		public Pair(double count, double rate) {
 			this.count = count;
 			this.rate = rate;
 		}
 	}
 
 }
