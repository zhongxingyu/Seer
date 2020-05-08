 /**
  * 
  */
 package VisualizeBinary;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileFilter;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Map;
 import javax.swing.JCheckBox;
 import javax.swing.JOptionPane;
 import net.sf.javaml.classification.Classifier;
 import net.sf.javaml.classification.evaluation.CrossValidation;
 import net.sf.javaml.classification.evaluation.PerformanceMeasure;
 import net.sf.javaml.classification.tree.RandomForest;
 import net.sf.javaml.core.Dataset;
 import net.sf.javaml.core.DefaultDataset;
 import net.sf.javaml.core.DenseInstance;
 import net.sf.javaml.core.Instance;
 import net.sf.javaml.tools.weka.WekaClassifier;
 import weka.classifiers.bayes.NaiveBayes;
 import weka.classifiers.trees.J48;
 
 /**
  * This class will use the output from the matrix generator to analyze the data with Weka
  * The class offers both a gui based interface and will allow commands to be read in from a file.
  * The results of either (gui or txt based) will be save to csv(s)
  * @author hartell
  *
  */
 public class DataCruncher {
 
	private static int granularity = 3;
 	private static boolean useGUI = false;
 	private static boolean generateReport = false;
	private static String problemName = "Problem2";
 	private static String reportName = "Test";
 	private static BufferedWriter out;
 	private static FileWriter fStream;
 
 	public static void main(String[] args){
 		//If the user wants to use the gui....
 		if(useGUI){
 			getCrunchyGUI();
 		} else {
 			//Read in text file: 
 			BufferedReader br = null;
 			try{
 				String line;
 				br = new BufferedReader(new FileReader("H:\\SVM\\Reports\\" + reportName + ".txt"));
 				if(generateReport) {fStream = new FileWriter("H://SVM/Reports/" + reportName + "Report.csv");}
 				if(generateReport) {out = new BufferedWriter(fStream);}
 				//Split each line, fragment and output the results
 				while((line = br.readLine())!= null){
 					//Split the line by ":" marker into seven sections (folders, filename, numberofFrags, type, sizeOfFrags, booleanDeleteOld, features).
 					String[] section = line.split(":");
 					//Calculate the fragments and output the results of the test to a csv
					if(problemName.equals("Problem1")){
						calculateResults1(section);
					}
					if(problemName.equals("Problem2")){
						calculateResults2(section);
					}
 				}
 			}
 			catch (IOException e){
 				e.printStackTrace();
 			} finally {
 				//Finally close all the important bits!
 				if(br != null){
 					try {
 						br.close();
 					} catch (IOException e) {
 						e.printStackTrace();
 					}
 				}
 				if(generateReport){
 					if(out != null){
 						try {
 							out.close();
 						} catch (IOException e) {
 							e.printStackTrace();
 						}
 					}
 					if(fStream != null){
 						try {
 							fStream.close();
 						} catch (IOException e) {
 							e.printStackTrace();
 						}
 					}
 				}
 			}
 		}
 	}
 	
 	/**
 	 * The Command line version of the data Cruncher, reads in commands from a txt file outputs results to a csv
 	 * Format [group types], [group2 types]:nameOfFile:numberFragsPerGroup:Type:SizeOfFrags:DeleteOldFrags:[Features]
 	 * Example: [PDF],[JPG GIF]:PdfVsJpgGif:100:Mid Fragments:512:true:[TotalPointsFeature TotalAgeFeature]
 	 * @param section - the command
 	 */
 	private static void calculateResults1(String[] section) {	
 		//For each section
 		String[] groups = getFolderNames(section[0]);
 		String fileName = section[1];
 		int numOfFrags = Integer.parseInt(section[2]);
 		//String typeOfFrag = section[3];
 		//int sizeOfFrag = Integer.parseInt(section[4]);
 		//boolean deleteOld = Boolean.parseBoolean(section[5]);
 		String[] features = getFeatures(section[6]);
 		
 //		//Use Fragmenter to fragment the files!
 //		for(String name : groups){
 //			//Split on space
 //			String[] folders = name.split(" ");
 //			int num = numOfFrags / folders.length;
 //			for(String f : folders){
 //				System.out.println("Preparing to fragment: " + f);
 //				Fragmenter.fragmenterCL(f, num, typeOfFrag, sizeOfFrag, deleteOld);
 //				System.out.println("");
 //			}
 //		}
 		
 		//Now files are fragmented we just need to add them to the dataset
 		//Initialize both arff files and instances
 		ArrayList<String> arffFile = new ArrayList<String>();
 		ArrayList<Instance> instances = new ArrayList<Instance>();
 		
 		//Ask users what they want this relation called (@relation)
 		String rName = fileName;
 		arffFile.add("@RELATION " + rName);
 		arffFile.add(" ");
 		
 		//Determine the features:
 		//Create the @Attributes section of Arff file, with a specified granularity
 		arffFile = ArffGenerator.createAttribute(arffFile, groups.length, features, granularity);
 		
 		//For each group:
 		for(int j = 0; j < groups.length; j ++){
 			String[] folders = groups[j].split(" ");
 			//For each folder in that group
 			for(String folder : folders){
 				System.out.println("Calculate Attributes for: H://SVM/" + problemName + "/"+ folder + " Fragmented");
 				//Get all the files
 				File dir = new File("H://SVM/" + problemName + "/" + folder + " Fragmented");
 				File[] files = dir.listFiles();
 				
 				//Go through all the files
 				for (int i = 0; i < (numOfFrags / folders.length); i++) {
 					File f = files[i];
 					//Get the metrics, given the features, and granularity
 					String result = ArffGenerator.calcAttributes(f, features, granularity);
 					//System.out.println("Result = " + result);
 					int dirNum = j+1;
 
 					//Add it to the Arff File
 					arffFile.add(f.getName() + "," + result + dirNum);
 
 					if(generateReport){
 						//Create the instance from the result;
 						String[] stringData = result.split(",");
 						double[] data = new double[stringData.length];
 	
 						for(int m = 0; m < stringData.length; m++){
 							data[m] = Double.parseDouble(stringData[m]);
 							//System.out.println("adding " + stringData[m]);
 						}
 						instances.add(new DenseInstance(data, dirNum));
 					}
 				}
 			}
 		}
 		
 		if(generateReport){
 			//Create a dataset from all the instances
 			System.out.println("Making dataset...");
 			Dataset data = makeDataSet(instances);
 	
 			//Run a classifier
 			//runRandomForestClassifier(data);
 			//runNaiveBaysClassifier(data);
 			Map<Object, PerformanceMeasure> map = runJ48Classifier(data);	
 			
 			//Print the results:
 			outputResults(map, arffFile, section);
 		}
 		
 		//Write out the newly created Arff file
 		ArffGenerator.makeArffFile(rName, problemName, arffFile);	
 	}
 
 	/**
 	 * The Command line version of the data Cruncher, reads in commands from a txt file outputs results to a csv
 	 * Format [group types], [group2 types]:nameOfFile:numberFragsPerGroup:Type:SizeOfFrags:DeleteOldFrags:[Features]
 	 * Example: [PDF],[JPG GIF]:PdfVsJpgGif:100:Mid Fragments:512:true:[TotalPointsFeature TotalAgeFeature]
 	 * @param section - the command
 	 */
 	private static void calculateResults2(String[] section) {	
 		//For each section
 		File mainFolder = new File("H://SVM/" + problemName + "/" + getFolderNames(section[0])[0]);
 		System.out.println(mainFolder);
 		String fileName = section[1];
 		String[] features = getFeatures(section[6]);
 		
 		//Now files are already fragmented, so we just need to add them to the dataset
 		//Initialize both arff files and instances
 		ArrayList<String> arffFile = new ArrayList<String>();
 		
 		//Ask users what they want this relation called (@relation)
 		String rName = fileName;
 		arffFile.add("@RELATION " + rName);
 		arffFile.add(" ");
 		
 		//Determine the features:
 		//Create the @Attributes section of Arff file, with a specified granularity
 		arffFile = ArffGenerator.createAttribute(arffFile, 3, features, granularity);
 		
 		//Make a filter to only get directories:
 		FileFilter dirFilter = new FileFilter(){
 			@Override
 			public boolean accept(File file) {
 				return file.isDirectory();
 			}
 		};
 		
 		
 		//Get all the folders in that directory
 		File[] folders = mainFolder.listFiles(dirFilter);
 		
 		//For all the folders (filled with fragments)
 		for(File f : folders){
 			//Divide the number of fragments in each folder into three parts..
 			File[] frags = f.listFiles(); //<--- sort this!
 			
 			//We need to sort the frags before we let them be categoried otherwise they will not be sort via natural ordering.
 			String[] fragNames = new String[frags.length];
 			for(int i = 0; i < frags.length; i++){
 				fragNames[i] = frags[i].getName();
 			}
 			Arrays.sort(fragNames, new FileSorter());
 			
 			for(int i = 0; i < frags.length; i++){
 				frags[i] = new File(f + "/" + fragNames[i]);
 				//System.out.println(frags[i]);
 			}
 			
 			int pointer = 0;
 			int position = 1;
 			int numberPerSection = frags.length / 3;
 			
 			for(int i = 0; i < frags.length; i++){
 				if(numberPerSection == pointer){
 					position++;
 					pointer = 0;
 				}
 				//Get the metrics, given the features, and granularity
 				String result = ArffGenerator.calcAttributes2(frags[i], features, granularity);
 				
 				//Deal with leftovers?
 				if(position == 4){
 					position = 3;
 				}
 
 				//System.out.println("Result = " + result);
 				//Add it to the Arff File
 				result = frags[i].getName() + "," + result + position;
 				arffFile.add(result);
 				//System.out.println(result);
 				pointer++;		
 			}
 		}
 		
 		//Write out the newly created Arff file
 		ArffGenerator.makeArffFile(rName, problemName, arffFile);
 		
 //		//For each group:
 //		for(int j = 0; j < groups.length; j ++){
 //			String[] folders = groups[j].split(" ");
 //			//For each folder in that group
 //			for(String folder : folders){
 //				System.out.println("Calculate Attributes for: H://SVM/" + problemName + "/"+ folder + " Fragmented");
 //				//Get all the files
 //				File dir = new File("H://SVM/" + problemName + "/" + folder + " Fragmented");
 //				File[] files = dir.listFiles();
 //				
 //				//Go through all the files
 //				for (int i = 0; i < (numOfFrags / folders.length); i++) {
 //					File f = files[i];
 //					//Get the metrics, given the features, and granularity
 //					String result = ArffGenerator.calcAttributes2(f, features, granularity);
 //					//System.out.println("Result = " + result);
 //					int dirNum = j+1;
 //
 //					//Add it to the Arff File
 //					arffFile.add(f.getName() + "," + result + dirNum);
 //				}
 //			}
 //		}
 //		
 //		//Write out the newly created Arff file
 //		ArffGenerator.makeArffFile(rName, problemName, arffFile);	
 	}
 	
 	/**
 	 * Returns an array of features
 	 * @param f - string of features
 	 * @return
 	 */
 	private static String[] getFeatures(String f) {
 		//First we need the directory, and remove the brackets
 		String[] features = f.split(" ");
 		for(int i = 0; i < features.length; i++){
 			features[i] = features[i].replace("[", "");
 			features[i] = features[i].replace("]", "");
 		}
 		return features;
 	}
 
 	/**
 	 * Gets the folder names from the given string
 	 * @param fNames - the section containing all the file names to fragment
 	 * @return
 	 */
 	private static String[] getFolderNames(String fNames) {
 		//First we need the directory, and remove the brackets
 		String[] folderNames = fNames.split(",");
 		for(int i = 0; i < folderNames.length; i++){
 			folderNames[i] = folderNames[i].replace("[", "");
 			folderNames[i] = folderNames[i].replace("]", "");
 		}
 		return folderNames;
 	}
 
 	/**
 	 * This method will crunch the data with a gui interface to guide the user into their sections.
 	 */
 	public static void getCrunchyGUI() {
 		System.out.println("Please select a dataset to load...");
 
 		//Declare Globals
 		ArrayList<File> fragDirs = new ArrayList<File>();
 		ArrayList<String> arffFile = new ArrayList<String>();
 		ArrayList<Instance> instances = new ArrayList<Instance>();
 
 		//Ask the user to build their dataSets by adding fragmented Files
 		boolean done = false;
 		while(!done){
 			done = ArffGenerator.selectDataSetsGUI(fragDirs);
 		}
 
 		//Ask users what they want this relation called (@relation)
 		String rName = JOptionPane.showInputDialog("Name of Relation?");
 		arffFile.add("@RELATION " + rName);
 		arffFile.add(" ");
 
 		//Ask the user what metrics they want to run on the files	
 		ArrayList<JCheckBox> theBoxes = new ArrayList<JCheckBox>();
 		theBoxes.add(new JCheckBox("Total Points Feature", true));  
 		//theBoxes.add(new JCheckBox("Percentage Feature")); // same as total Feature
 		theBoxes.add(new JCheckBox("Total Age Feature",true));
 		theBoxes.add(new JCheckBox("Average Age Feature"));
 		String message = "Which attributes/features would you like to calculate?";  
 		Object[] params = new Object[theBoxes.size() + 1];
 		params[0] = message;
 		for(int i = 0; i < theBoxes.size(); i++){
 			params[i+1] = theBoxes.get(i);
 		}
 		JOptionPane.showOptionDialog(null, params, "Available attributes:", JOptionPane.OK_OPTION, JOptionPane.QUESTION_MESSAGE, null, new String[]{"OK"}, "default");
 
 		//Create the @Attributes section of Arff file, with a specified granularity
 		arffFile = ArffGenerator.createAttributesGUI(arffFile, fragDirs, theBoxes, granularity);
 
 		// Run the metrics on the file
 		// For each directory of fragments...
 		for (int i = 0; i < fragDirs.size(); i++) {
 			// Get all files
 			File[] files = fragDirs.get(i).listFiles();
 
 			// Go through all the files
 			for (int j = 0; j < files.length; j++) {
 				// Grab each file
 				File f = files[j];
 				// Get the metrics: from the file f, with the all the attrib in theBoxes, from directory i
 				String result = ArffGenerator.calcAttributesGUI(f, theBoxes, granularity);
 				int dirNum = i+1;
 				arffFile.add(f.getName() + "," + result + dirNum);
 				//Create the instance from the result;
 				String[] stringData = result.split(",");
 				double[] data = new double[stringData.length];
 				for(int m = 0; m < stringData.length; m++){
 					data[m] = Double.parseDouble(stringData[m]);
 					//System.out.println("adding " + stringData[m]);
 				}
 				instances.add(new DenseInstance(data, dirNum));
 			}
 		}
 
 		//Create a dataset from all the instances
 		System.out.println("Making dataset...");
 		Dataset data = makeDataSet(instances);
 
 		//Run a classifier
 		//runRandomForestClassifier(data);
 		//runNaiveBaysClassifier(data);
 		Map<Object, PerformanceMeasure> map = runJ48Classifier(data);
 		
 		//Print the results:
 		outputResultsGUI(map, arffFile, rName);
 		
 		//Write out the newly created Arff file
 		ArffGenerator.makeArffFile(rName, problemName, arffFile);
 	}
 
 	/**
 	 * This will attempt to classifier the data using the J48 decision tree
 	 * @param data - the data to be classified
 	 * @return map - the results of the classification
 	 */
 	private static Map<Object, PerformanceMeasure> runJ48Classifier(Dataset data) {
 		//Build a classifier
 		J48 j48 = new J48();
 		Classifier nBays = new WekaClassifier(j48);
 
 		//Build the 10-fold default CrossValidation
 		System.out.println("Performing Cross Validation...");
 		CrossValidation cv = new CrossValidation(nBays);
 
 		//Get Results of cross validation
 		System.out.println("Mapping results...");
 		Map<Object, PerformanceMeasure> map = cv.crossValidation(data);
 		
 		//Return the map of the results
 		return map;
 	}
 
 	/**
 	 * This will attempt to classify the data using the Naive Bays Classifier
 	 * @param data - the data to be classified
 	 * @return map - the results of the classification
 	 */
 	@SuppressWarnings("unused")
 	private static Map<Object, PerformanceMeasure> runNaiveBaysClassifier(Dataset data) {
 		//Build a classifier
 		NaiveBayes naiveBays = new NaiveBayes();
 		Classifier nBays = new WekaClassifier(naiveBays);
 
 		//Build the 10-fold default CrossValidation
 		System.out.println("Performing Cross Validation...");
 		CrossValidation cv = new CrossValidation(nBays);
 
 		//Get Results of cross validation
 		System.out.println("Mapping results...");
 		Map<Object, PerformanceMeasure> map = cv.crossValidation(data);
 
 		return map;
 	}
 
 
 	/**
 	 * This will attempt to classify the data using the Random Forest Classifier
 	 * Random forest of 10 trees, each constructed considering 5 random features
 	  * @param data - the data to be classified
 	 * @return map - the results of the classification
 	 */
 	@SuppressWarnings("unused")
 	private static Map<Object, PerformanceMeasure> runRandomForestClassifier(Dataset data) {
 		//Build Classifier
 		System.out.println("Building Classifier...");
 		Classifier randForest = new RandomForest(10);
 
 		//Build the 10-fold default CrossValidation
 		System.out.println("Performing Cross Validation...");
 		CrossValidation cv = new CrossValidation(randForest);
 
 		//Get Results of cross validation
 		System.out.println("Mapping results...");
 		Map<Object, PerformanceMeasure> map = cv.crossValidation(data);
 		
 		return map;
 	}
 	
 	/**
 	 * Prints stats for the classifier
 	 * @param map - the results of the classification
 	 * @param arffFile - the arffFile to output too
 	 * @param section - the command read in from the text file
 	 */
 	private static void outputResults(Map<Object, PerformanceMeasure> map, ArrayList<String> arffFile, String[] section) {
 		
 		ArrayList<Double> tpRates = new ArrayList<Double>();
 		
 		try{
 			//Create a Writer, write each line out to a file.
 			System.out.println("Outputting Results");
 			//Get the information about the relation:
 			String[] groups = getFolderNames(section[0]);
 			int numOfFrags = Integer.parseInt(section[2]);
 			String typeOfFrag = section[3];
 			int sizeOfFrag = Integer.parseInt(section[4]);
 			String[] features = getFeatures(section[6]);
 			
 			String relation = null;
 			for(String g : groups){
 				if(relation != null){
 					relation = relation + " vs ";
 				} else {
 					relation = "Relation: ";
 				}
 				relation = relation + g;
 			}
 			
 			double accuracy = map.get(1).getAccuracy();
 			double bcr = map.get(1).getBCR();
 			double correlation = map.get(1).getCorrelation();
 			double corrCoefficient = map.get(1).getCorrelationCoefficient();
 			double errorRate = map.get(1).getErrorRate();
 			double q9 = map.get(1).getQ9();
 			double total = map.get(1).getTotal();
 			
 			out.write(relation);
 			out.newLine();
 			out.write("Granularity: " + granularity);
 			out.newLine();
 			out.write("Number of fragments: " + numOfFrags + " per type, Total #: " + total);
 			out.newLine();
 			out.write("Fragment Location: " + typeOfFrag);
 			out.newLine();
 			out.write("Fragment Size: " + sizeOfFrag);
 			out.newLine();
 			out.write("Features: " + Arrays.toString(features));
 			out.newLine();
 			
 			out.write("Accuracy: " + accuracy);
 			out.newLine();
 			out.write("BCR: "+ bcr);
 			out.newLine();
 			out.write("Correlation: "+ correlation);
 			out.newLine();
 			out.write("Coefficient: "+ corrCoefficient);
 			out.newLine();
 			out.write("Error Rate: "+ errorRate);
 			out.newLine();
 			out.write("Q9: "+ q9);
 			out.newLine();
 			
 			//Calculate all the things...
 			for(int i = 1; i < 3; i++){
 				System.out.println("MAP " + i + ": ------------------");
 				double tPRate = map.get(i).getTPRate();
 				tpRates.add(tPRate);
 				double fPRate = map.get(i).getFPRate();
 				double tNRate = map.get(i).getTNRate();
 				double fNRate = map.get(i).getFNRate();
 				double cost = map.get(i).getCost();
 				double fMeasure = map.get(i).getFMeasure();
 				double precision = map.get(i).getPrecision();
 				double recall = map.get(i).getRecall();
 				
 //				Output all the things:
 				out.write("File type " + i + ": ");
 				out.write("TP Rate: "+ tPRate + ",");
 				out.write("FP Rate: "+ fPRate + ",");
 				out.write("TN Rate: "+ tNRate + ",");
 				out.write("FN Rate: "+ fNRate + ",");
 				out.write("Cost: "+ cost + ",");
 				out.write("FMeasure: "+ fMeasure + ",");
 				out.write("Precision: "+ precision + ",");
 				out.write("Recall: "+ recall + ",");
 				//Line Break
 				out.newLine();
 			}
 			
 			//Calculate the number of correct/incorrect classifications
 			double avgTP = 0.0;
 			for(Double d : tpRates){
 				avgTP = avgTP + d;
 			}
 			avgTP = avgTP / tpRates.size();
 			
 			double correctClassifications = total * avgTP;
 			double incorrectClassifications = total - correctClassifications;
 			
 			DecimalFormat df = new DecimalFormat("00.00%");
 			
 			out.newLine();
 			out.write("Correct Classifications: " + correctClassifications + ", ");
 			out.write("Percentage: " + (df.format(correctClassifications/total)));
 			out.newLine();
 			out.write("Incorrect Classifications: " + incorrectClassifications + ", ");
 			out.write("Percentage: " + (df.format(incorrectClassifications/total)));
 
 			out.newLine();
 			out.newLine();
 			out.write("---------------------------------------------------------------------------------------------------------");
 			out.newLine();
 			out.newLine();
 		}
 		catch(Exception e){
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Prints stats for the classifier
 	 * @param map - the results of the classification
 	 * @param arffFile - the arffFile to output too
 	 * @param rName - the name of the relation
 	 */
 	private static void outputResultsGUI(Map<Object, PerformanceMeasure> map, ArrayList<String> arffFile, String rName) {
 
 		ArrayList<Double> tpRates = new ArrayList<Double>();
 
 		try{
 			//Create a Writer, write each line out to a file.
 			System.out.println("Outputting Results");
 			FileWriter fStream = new FileWriter("H://SVM/Reports/" + rName + "Report.csv");
 			BufferedWriter out = new BufferedWriter(fStream);
 			//Write out the name of the relation
 			out.write(arffFile.get(0));
 			out.newLine();
 			
 			double accuracy = map.get(1).getAccuracy();
 			double bcr = map.get(1).getBCR();
 			double correlation = map.get(1).getCorrelation();
 			double corrCoefficient = map.get(1).getCorrelationCoefficient();
 			double errorRate = map.get(1).getErrorRate();
 			double q9 = map.get(1).getQ9();
 			double total = map.get(1).getTotal();
 			
 			out.write("Accuracy: " + accuracy + ",");
 			out.newLine();
 			out.write("BCR: "+ bcr + ",");
 			out.newLine();
 			out.write("Correlation: "+ correlation + ",");
 			out.newLine();
 			out.write("Coefficient: "+ corrCoefficient + ",");
 			out.newLine();
 			out.write("Error Rate: "+ errorRate + ",");
 			out.newLine();
 			out.write("Q9: "+ q9 + ",");
 			out.newLine();
 			out.write("Total: "+ total + ",");
 			out.newLine();
 			
 
 			//Calculate all the things...
 			for(int i = 1; i < 3; i++){
 				System.out.println("MAP " + i + ": ------------------");
 				double tPRate = map.get(i).getTPRate();
 				tpRates.add(tPRate);
 				double fPRate = map.get(i).getFPRate();
 				double tNRate = map.get(i).getTNRate();
 				double fNRate = map.get(i).getFNRate();
 				double cost = map.get(i).getCost();
 				double fMeasure = map.get(i).getFMeasure();
 				double precision = map.get(i).getPrecision();
 				double recall = map.get(i).getRecall();
 
 
 				// Output all the things:
 				out.write("File type " + i + ": ");
 				out.write("TP Rate: "+ tPRate + ",");
 				out.write("FP Rate: "+ fPRate + ",");
 				out.write("TN Rate: "+ tNRate + ",");
 				out.write("FN Rate: "+ fNRate + ",");
 				out.write("Cost: "+ cost + ",");
 				out.write("FMeasure: "+ fMeasure + ",");
 				out.write("Precision: "+ precision + ",");
 				out.write("Recall: "+ recall + ",");
 				
 				//Line Break
 				out.newLine();
 			}
 
 			//Calculate the number of correct/incorrect classifications
 			double avgTP = 0.0;
 			for(Double d : tpRates){
 				avgTP = avgTP + d;
 			}
 			avgTP = avgTP / tpRates.size();
 
 			double correctClassifications = total * avgTP;
 			double incorrectClassifications = total - correctClassifications;
 
 			DecimalFormat df = new DecimalFormat("00.00%");
 
 			out.newLine();
 			out.write("Correct Classifications: " + correctClassifications + ", ");
 			out.write("Percentage: " + (df.format(correctClassifications/total)));
 			out.newLine();
 			out.write("Incorrect Classifications: " + incorrectClassifications + ", ");
 			out.write("Percentage: " + (df.format(incorrectClassifications/total)));
 
 
 			//Close the important bits
 			out.close();
 			fStream.close();
 		}
 		catch(Exception e){
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Adds all the instances to the dataset
 	 * @param instanceList - a list of all instances
 	 * @return the dataset
 	 */
 	private static Dataset makeDataSet(ArrayList<Instance> instanceList) {
 		Dataset dataset = new DefaultDataset();
 		for(int i = 0; i < instanceList.size(); i++){
 			dataset.add(instanceList.get(i));
 		}
 
 		return dataset;
 	}
 }
