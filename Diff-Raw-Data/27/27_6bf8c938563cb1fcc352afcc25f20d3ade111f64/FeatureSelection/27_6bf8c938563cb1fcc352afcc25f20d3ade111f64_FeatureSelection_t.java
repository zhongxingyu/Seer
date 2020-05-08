 package project6867;
 
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Set;
 
 import net.sf.javaml.core.Dataset;
 import net.sf.javaml.distance.PearsonCorrelationCoefficient;
 import net.sf.javaml.featureselection.subset.GreedyBackwardElimination;
 import net.sf.javaml.featureselection.subset.GreedyForwardSelection;
 
 public class FeatureSelection {
 
	private static int DATA_SIZE = 5000;
	private static int FEATURE_SIZE = 4697;
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		DataHandler dh = new DataHandler();
 		Runnable[] runners = new Runnable[3];
 		Thread[] threads = new Thread[3];
		runners[0] = new FeatureThread(DataHandler.getCompositeDataset(DATA_SIZE), FEATURE_SIZE, 2, FeatureThread.Direction.FORWARD);
		runners[1] = new FeatureThread(DataHandler.getCompositeDataset(DATA_SIZE), FEATURE_SIZE, 10, FeatureThread.Direction.FORWARD);
		runners[2] = new FeatureThread(DataHandler.getCompositeDataset(DATA_SIZE), FEATURE_SIZE, 100, FeatureThread.Direction.FORWARD);
 		for(int i = 0; i < 3; i++){
 			threads[i] = new Thread(runners[i]);
 			threads[i].start();
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
 		private Direction direction;
 		private DateFormat dateFormat;
 		static enum Direction{FORWARD, BACKWARD};
 		
 		public FeatureThread(Dataset d, int featureSize, double retention, Direction dir){
 			this.data = d.copy();
 			if(retention < 1)
				this.n = (int)(this.featureSize*retention);
 			else
				this.n = (int)retention;
 			this.featureSize = featureSize;
 			this.direction = dir;
 			dateFormat = new SimpleDateFormat("HH:mm:ss");
 		}
 		
 		@Override
 		public void run() {
 			System.out.println(this.toString() + ":Started at " + dateFormat.format(new Date()));
 			Set<Integer> selected;
 			if(this.direction == Direction.FORWARD){
				GreedyForwardSelection selector = new GreedyForwardSelection(this.n, new PearsonCorrelationCoefficient());
 				selector.build(data);
 				selected = selector.selectedAttributes();
 			}else{
				GreedyBackwardElimination selector = new GreedyBackwardElimination(this.n, new PearsonCorrelationCoefficient());
 				selector.build(data);
 				selected = selector.selectedAttributes();
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
 		
 		public String toString(){
 			return (this.direction == Direction.FORWARD ? "forward" : "backward") + "@" + this.n;
 		}
 	}
 
 }
 
 
