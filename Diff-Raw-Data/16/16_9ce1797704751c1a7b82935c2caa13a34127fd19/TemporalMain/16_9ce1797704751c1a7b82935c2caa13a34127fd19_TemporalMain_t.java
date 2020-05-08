 package edu.uw.cs.lil.tiny.tempeval;
 
 import edu.uw.cs.lil.tiny.tempeval.preprocessing.TemporalReader;
 import edu.uw.cs.lil.tiny.tempeval.structures.TemporalSentence;
 import edu.uw.cs.lil.tiny.tempeval.structures.TemporalDataset;
 import edu.uw.cs.lil.tiny.tempeval.util.Debug;
 import edu.uw.cs.lil.tiny.tempeval.util.Debug.Type;
 import edu.uw.cs.lil.tiny.tempeval.util.TemporalStatistics;
 
 import java.io.IOException;
 import java.util.List;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.xml.sax.SAXException;
 
 public class TemporalMain {
 	private static final String DATASET_DIR = "data/TempEval3/TBAQ-cleaned/";
 	private static final String[] DATASETS =  {"AQUAINT", "TimeBank"};
 	//private static final String[] DATASETS = {"TimeBank"};
 	//private static final String[] DATASETS =  {"debug_dataset"};
 
 	public static final int CV_FOLDS = 10;
 	public static final boolean STRICT_MATCHING = false;
 	public static final boolean GOLD_MENTIONS = false;
	public static final boolean FORCE_SERIALIZATION = false;
 	public static final boolean CROSS_VALIDATION = true;
 	public static final boolean PARALLEL_EXECUTION = true;
 
 	private static void evaluate(TemporalDataset dataset) {
 		Debug.printf (Type.PROGRESS,"Evaluating %d sentences...\n\n", dataset.size());
 		TemporalStatistics stats = new TemporalStatistics();
 
 		if (CROSS_VALIDATION){
 			List<List<TemporalSentence>> partitions = dataset.partition(CV_FOLDS);
 			TemporalEvaluation[] threads = new TemporalEvaluation[partitions.size()];
 
 			for (int i = 0; i < partitions.size(); i++){
 				TemporalDataset trainData = new TemporalDataset();
 				TemporalDataset testData = new TemporalDataset(partitions.get(i));
 				for (int j = 0; j < partitions.size(); j++)
 					if (j != i)
 						trainData.addSentences(partitions.get(j));
 
 				threads[i] = new TemporalEvaluation(trainData, testData, stats);
 				if(PARALLEL_EXECUTION)
 					threads[i].start();
 				else
 					threads[i].run();
 			}
 			for (int i = 0; i < threads.length; i++){
 				try{
 					threads[i].join();
 				} catch (InterruptedException e){
 					e.printStackTrace();
 					System.err.println("Some problems getting the threads to join again!");
 				}
 			}
 		} else {
 			// Train and test on the same dataset for debugging
 			new TemporalEvaluation(dataset, dataset, stats).run();
 		}
 		Debug.println(Type.STATS, stats);
 		Debug.println(Type.PROGRESS, "Done with analysis.");
 	}
 
 	public static void main(String[] args) throws SAXException, IOException, ParserConfigurationException, ClassNotFoundException {
 		Debug.setLogs("relaxed");
 		Debug.addFilter("", System.out, Type.PROGRESS, Type.STATS);
 		Debug.addFilter("ERROR:", System.out, Type.ERROR);
 		Debug.addFilter("DEBUG:", System.out, Type.DEBUG);
 		Debug.addFilter("", "stats.txt", Type.STATS);
 		Debug.addFilter("", "attributes.txt", Type.ATTRIBUTE);
 		Debug.addFilter("", "detection.txt", Type.DETECTION);
 		TemporalDataset dataset = new TemporalReader().getDataset(DATASET_DIR, DATASETS, FORCE_SERIALIZATION);
 		evaluate(dataset);
 	}
 }
