 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.OutputStreamWriter;
 import java.util.ArrayList;
 
 public class Main{
 	public static void main(String[] args){
 		String training = "classifier_data_small_train";
 		String testing = "classifier_data_test";
 		
 		if(args.length == 2) {
 			training = args[0];
 			testing = args[1];
 		} else {
			System.out.println("No parameters passed.\n\nFolders are assumed to be:\nTraining data: " + training + "\nTesting data: " + testing);
 		}
 		testFolder(training, testing);
 	}
 	
 	public static void testFolder(String training, String testing) {
 		ArrayList<File> files = new ArrayList<File>();
 		
 		File folder = new File(testing);
 		File[] fileList = folder.listFiles();
 		for(File f : fileList){
 			files.add(f);
 		}
 		
 		ArrayList<Integer> trainingClasses = new ArrayList<Integer>();
 		ArrayList<File> trainingFiles = new ArrayList<File>();
 		
 		for(int i = 0; i < Trainer.CLASSES.length; i++) {
 			ArrayList<File> temp = loadClass(training, i);
 			
 			for(File f : temp) {
 				trainingFiles.add(f);
 				trainingClasses.add(i);
 			}
 		}
 		
 		int[] results = execute(trainingFiles, trainingClasses, files);
 		try {
 			FileOutputStream fs = new FileOutputStream("test.output");
 			OutputStreamWriter out = new OutputStreamWriter(fs, "UTF-8");
 			for(int i = 0; i < results.length; i++) {
 				out.write(files.get(i).getName() + "|" + Trainer.CLASSES[results[i]] + "\n");
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public static void test(String training){
 		ArrayList<ArrayList<File>> files = new ArrayList<ArrayList<File>>();
 		
 		for(int className = 0; className < Trainer.CLASSES.length; className++){
 			files.add(loadClass(training, className));
 		}
 		
 		int incr = 20;
 		int maxClassSize = 200;
 		
 		int count = 0;
 		int start = 0;
 		int end = incr;
 		
 		Integer[] numCorrect = Utils.initIntArr(Trainer.CLASSES.length, 0);
 		Integer[] numGuesses = Utils.initIntArr(Trainer.CLASSES.length, 0);
 		Integer[] numDocs = Utils.initIntArr(Trainer.CLASSES.length, 0);
 		
 		while(end < maxClassSize){
 			ArrayList<File> trainingFiles = new ArrayList<File>();
 			ArrayList<Integer> trainingClasses = new ArrayList<Integer>();
 			ArrayList<File> unknowns = new ArrayList<File>();
 			ArrayList<Integer> unknownClasses = new ArrayList<Integer>();
 			
 			for(int className = 0; className < files.size(); className++){
 				ArrayList<File> curFiles = files.get(className);
 				for(int i = 0; i < curFiles.size(); i++){
 					if(i < start || i >= end){
 						trainingFiles.add(curFiles.get(i));
 						trainingClasses.add(className);
 					}else{
 						unknowns.add(curFiles.get(i));
 						unknownClasses.add(className);
 					}
 				}
 			}
 			
 			int[] results = execute(trainingFiles, trainingClasses, unknowns);
 			for(int i = 0; i < results.length; i++){
 				if(results[i] == unknownClasses.get(i))
 					numCorrect[results[i]]++;
 				numGuesses[results[i]]++;
 				numDocs[unknownClasses.get(i)]++;
 			}
 			
 			start = end;
 			end += incr;
 		}
 		
 		for(int i = 0; i < Trainer.CLASSES.length; i++){
 			double precision = ((double)numCorrect[i]) / numGuesses[i];
 			System.out.println("Precision(" + Trainer.CLASSES[i] + "): " + precision);
 			double recall = ((double)numCorrect[i]) / numDocs[i];
 			System.out.println("Recall(" + Trainer.CLASSES[i] + "): " + recall);
 		}
 	}
 	
 	private static String buildString(ArrayList<File> files, String[] results){
 		String rtn = "";
 		for(int i = 0; i < files.size(); i++){
 			rtn += files.get(i).getName() + "|" + results[i] + "\n";
 		}
 		return rtn;
 	}
 	
 	private static int[] execute(ArrayList<File> trainingFiles, ArrayList<Integer> trainingClasses, ArrayList<File> unknowns){
 		Trainer t = new Trainer(trainingFiles, trainingClasses);
 		
 		int[] rtn = new int[unknowns.size()]; 
 		for(int i = 0; i < rtn.length; i++){
 			rtn[i] = Classifier.getClass(t, unknowns.get(i));
 		}
 		return rtn;
 	}
 	
 	private static ArrayList<File> loadClass(String trainingFolder, Integer className){
 		ArrayList<File> rtn = new ArrayList<File>();
 		File folder = new File(trainingFolder + "/" + Trainer.CLASSES[className] + "/train");
 		File[] files = folder.listFiles();
 		for(File f : files){
 			rtn.add(f);
 		}
 		return rtn;
 	}
 }
