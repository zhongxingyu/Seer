 package project6867;
 
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.Vector;
 
 import net.sf.javaml.core.Dataset;
 import net.sf.javaml.core.Instance;
 import net.sf.javaml.distance.PearsonCorrelationCoefficient;
 import net.sf.javaml.featureselection.scoring.SymmetricalUncertainty;
 import net.sf.javaml.featureselection.subset.GreedyBackwardElimination;
 import net.sf.javaml.featureselection.subset.GreedyForwardSelection;
 import net.sf.javaml.utils.ArrayUtils;
 import net.sf.javaml.utils.ContingencyTables;
 
 public class FeatureSelection {
 

	private static int DATA_SIZE = 1000;
 	private static int FEATURE_SIZE = 4697;
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		Runnable[] runners = new Runnable[3];
 		Thread[] threads = new Thread[3];
 		runners[0] = new FeatureThread(DataHandler.getCompositeDataset(DATA_SIZE), FEATURE_SIZE, .9, FeatureThread.Type.SU);
 		runners[1] = new FeatureThread(DataHandler.getCompositeDataset(DATA_SIZE), FEATURE_SIZE, .8, FeatureThread.Type.SU);
 		runners[2] = new FeatureThread(DataHandler.getCompositeDataset(DATA_SIZE), FEATURE_SIZE, .5, FeatureThread.Type.SU);
 		for(int i = 0; i < 3; i++){
 			threads[i] = new Thread(runners[i]);
 			threads[i].start();
 			
 		}
 		for(int i = 0; i < 3; i++) {
 			try{
 				threads[i].join();
 			}catch(InterruptedException e){
 				e.printStackTrace();
 			}
 		}
 		System.exit(0);
 	}
 	
 	private static class FeatureThread implements Runnable {
 
 		private Dataset data;
 		private int n;
 		private int featureSize;
 		private Type type;
 		private DateFormat dateFormat;
 		static enum Type{FORWARD, BACKWARD, SU};
 		
 		public FeatureThread(Dataset d, int featureSize, double retention, Type typ){
 			this.data = d.copy();
 			if(retention < 1)
 				this.n = (int)(this.featureSize*retention);
 			else
 				this.n = (int)retention;
 			this.featureSize = featureSize;
 			this.type = typ;
 			dateFormat = new SimpleDateFormat("HH:mm:ss");
 		}
 		
 		@Override
 		public void run() {
 			System.out.println(this.toString() + ":Started at " + dateFormat.format(new Date()));
 			Set<Integer> selected;
 			if(this.type == Type.FORWARD){
 				GreedyForwardSelection selector = new GreedyForwardSelection(this.n, new PearsonCorrelationCoefficient());
 				selector.build(data);
 				selected = selector.selectedAttributes();
 			}else if(this.type == Type.BACKWARD){
 				GreedyBackwardElimination selector = new GreedyBackwardElimination(this.n, new PearsonCorrelationCoefficient());
 				selector.build(data);
 				selected = selector.selectedAttributes();
 			}
 			else {
 				SymmetricalUncertainty scorer = new SymmetricalUncertainty();
 //				RankingFromScoring rfs = new RankingFromScoring(scorer);
 //
 //				rfs.build(data);
 				int[] ranking;
 				int noAttributes=data.noAttributes();
 		        //scorer.build(data);
 		        double[] values = new double[noAttributes];
 		        for (int i = 0; i < values.length; i++)
 		            values[i] = score(i);
 
 		        ranking = new int[values.length];
 		        int[] order = ArrayUtils.sort(values);
 		        for (int i = 0; i < order.length; i++) {
 		            ranking[order[i]] = order.length - i - 1;
 		        }
 				
 				selected = new HashSet<Integer>(this.n);
 				for(int i =0; i < noAttributes; ++i) {
 					int rank = ranking[i];
 					if(rank < this.n) {
 						selected.add(i);
 					}
 				}
 			}
 			System.out.println(this.toString() + ":Writing output at " + dateFormat.format(new Date()));
 			try{
 				BufferedWriter out = new BufferedWriter(new FileWriter(this.toString() + "_" + data.size() + "mixed.data"));
 				for(Integer i : selected){
 					out.write(i.toString() + ", ");
 				}
 				out.flush();
 				out.close();
 				System.out.println(this.toString() + ":Finished successfully at " + dateFormat.format(new Date()));
 			}catch(IOException e){
 				e.printStackTrace();
 			}
 		}
 		
 		private int bins = 2;
 		
 	    public double score(int attribute) {
 	        // int ii, jj;
 	        // int ni = training.numValues(attribute) + 1;
 	        // int nj = training.numClasses() + 1;
 	        double[][] counts = new double[bins][data.classes().size()];
 	        List<Object> classes = new Vector<Object>();
 	        classes.addAll(data.classes());
 	        // System.out.println(training);
 	        for (Instance inst : data) {
 	            // ii = (int) inst.value(attribute);
 	            // jj = (int) inst.classValue();
 	            if ((int) inst.value(attribute) >= bins) {
 	                System.err.println("Exceeding bins: " + bins);
 	            }
 	            if (classes.indexOf(inst.classValue()) >= data.classes().size())
 	                System.err.println("Exceeding classes: " + data.classes().size());
 	            counts[(int) inst.value(attribute)][classes.indexOf(inst.classValue())]++;
 	        }
 	        return ContingencyTables.symmetricalUncertainty(counts);
 	    }
 
 		
 		public String toString(){
 			return (this.type == Type.FORWARD ? "forward" : (this.type == Type.BACKWARD)? "backward" : "su") + "@" + ((double)this.n)/this.featureSize;
 		}
 	}
 
 }
 
 
