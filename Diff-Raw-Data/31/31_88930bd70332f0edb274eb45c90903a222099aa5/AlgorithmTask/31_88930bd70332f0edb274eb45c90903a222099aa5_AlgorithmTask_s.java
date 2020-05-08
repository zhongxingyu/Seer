 package hypermodulesrun;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.text.DateFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 
 import javax.swing.JOptionPane;
 
 import org.cytoscape.util.swing.FileUtil;
 
 import com.google.common.collect.ArrayListMultimap;
 import com.google.common.collect.Multimap;
 
 public class AlgorithmTask implements Runnable {
 
 	/**
 	 * original pValues from OriginalTest
 	 */
 	private HashMap<String, HashMap<String, Double>> originalResults;
 	/**
 	 * classification of "HIGH" or "LOW" survival for all the modules in originalResults
 	 */
 	private HashMap<String, HashMap<String, Double>> classification;
 	/**
 	 * all the shuffled data to perform FDR adjustment
 	 */
 	private HashMap<String, Multimap<String, Double>> shuffling;
 	/**
 	 * an arraylist to add to in order to manage having multiple threads - to avoid synchronization
 	 * issues related to adding to a hashmap simultaneously from different threads
 	 */
 	private ArrayList<HashMap<String, Multimap<String, Double>>> combinedShuffling;
 	/**
 	 * FDR p-values
 	 */
 	private HashMap<String, HashMap<String, Double>> adjustedResults;
 	
 	private HashMap<String, HashMap<ArrayList<HashMap<String, Double>>, Multimap<String, Double>>> allResults;
 	
 	private ArrayList<String[]> network;
 	private ArrayList<String[]> sampleValues;
 	private ArrayList<String[]> clinicalValues;
 	
 	private int shuffleNumber;
 
 	private String stat;
 	
 	public AlgorithmTask(ArrayList<String[]> network, 
 						 ArrayList<String[]> sampleValues, 
 						 ArrayList<String[]> clinicalValues,
 						 int shuffleNumber, String stat){
 		
 		this.network = network;
 		this.sampleValues = sampleValues;
 		this.shuffleNumber = shuffleNumber;
 		this.stat = stat;
 		ArrayList<String[]> sortedClinicals = new ArrayList<String[]>();
 		Multimap<Double, Integer> followupDaysPosition = ArrayListMultimap.create();
 		
 		for (int k=0; k<clinicalValues.size(); k++){
 			boolean b = true;
 			try{
 				Double d = Double.valueOf(clinicalValues.get(k)[2]);
 			}
 			catch (NumberFormatException e){
 				b = false;
 			}
 			finally{
 				if (b){
 					followupDaysPosition.put(Double.valueOf(clinicalValues.get(k)[2]), Integer.valueOf(k));
 				}
 			}
 		}
 		
 		ArrayList<Double> sortedTime = new ArrayList<Double>();
 		ArrayList<Integer> sortedPositions = new ArrayList<Integer>();
 		
 		for (int k=0; k<clinicalValues.size(); k++){
 			sortedTime.add(Double.valueOf(clinicalValues.get(k)[2]));
 		}
 		
 		Collections.sort(sortedTime);
 		HashMap<Double, Boolean> alreadyAdded = new HashMap<Double, Boolean>();
 		
 		for (Double key : followupDaysPosition.keySet()){
 			alreadyAdded.put(key,  false);
 		}
 
 		for (int k=0; k<sortedTime.size(); k++){
 			if (alreadyAdded.get(sortedTime.get(k))==false){
 				Collection<Integer> coll = followupDaysPosition.get(sortedTime.get(k));
 				for (Integer value : coll){
 					sortedPositions.add(value);
 				}
 			}
 			alreadyAdded.put(sortedTime.get(k), true);
 		}
 		
 		for (int k=0; k<sortedPositions.size(); k++){
 			String[] thisString = new String[4];
 			thisString[0]=clinicalValues.get(sortedPositions.get(k))[0];
 			thisString[1]=clinicalValues.get(sortedPositions.get(k))[1];
 			thisString[2]=clinicalValues.get(sortedPositions.get(k))[2];
 			thisString[3]=clinicalValues.get(sortedPositions.get(k))[3];
 			sortedClinicals.add(thisString);
 			
 		}
 		this.clinicalValues = sortedClinicals;
 	}
 	
 	public void run() {
 		long before = System.nanoTime();
 		System.out.println("Running Original Test...");
 		OriginalTest ot = new OriginalTest(network, sampleValues, clinicalValues, shuffleNumber, stat);
 		this.originalResults = ot.callTest();
 		fixOriginalResults();
 		
 
		this.classification = ot.testHighOrLow(this.originalResults);
		
		
 		int nCores = Runtime.getRuntime().availableProcessors();
 		System.out.println("Number of Available Processors: " + nCores);
 		combinedShuffling = new ArrayList<HashMap<String, Multimap<String, Double>>>();
 		
 		int shuffleCount = 0;
 		
 		ExecutorService executor = Executors.newFixedThreadPool(nCores);
 		List<Future<HashMap<String, Multimap<String, Double>>>> list = new ArrayList<Future<HashMap<String, Multimap<String, Double>>>>();
 		
 		for (int i=0; i<nCores-1; i++){
 			//reinitializeVariables();
 			ShuffleTestCall st = new ShuffleTestCall(this.network, this.sampleValues, this.clinicalValues, (int) shuffleNumber/nCores, this.stat);
 			Future<HashMap<String, Multimap<String, Double>>> submitPool = executor.submit(st);
 			list.add(submitPool);
 			shuffleCount += (int) shuffleNumber/nCores;
 		}
 		
 		ShuffleTestCall st = new ShuffleTestCall(this.network, this.sampleValues, this.clinicalValues, (int) (shuffleNumber - shuffleCount), this.stat);
 		Future<HashMap<String, Multimap<String, Double>>> submitPool = executor.submit(st);
 		list.add(submitPool);
 		
 		for (Future<HashMap<String, Multimap<String, Double>>> future : list){
 			try{
 				combinedShuffling.add(future.get());
 			}
 			catch (Exception e){
 				e.printStackTrace();
 			}
 		}
 		
 		executor.shutdown();
 		moveShuffled();
 		System.out.println("Finished Moving");
 		
 		adjustResults();
 		System.out.println("Finished Adjusting");
 		
 		allResults = resultsFormat();
 		HashMap<String, String> parameters = new HashMap<String, String>();
 		parameters.put("nShuffled", String.valueOf(this.shuffleNumber));
 		parameters.put("stat", this.stat);
 
 		long after = System.nanoTime();
 		double timeToRun = (double) (after-before)/1000000000;
 		System.out.println("Time to run: " + timeToRun + " seconds");
 		loop();
 	}
 
 	
 	public void loop(){
 		int counter = 0;
 		printOptions();
 		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
 		while (true){
 			int i = 5;
 			try {
 				i = Integer.valueOf(br.readLine());
 				if (i<0 || i>2){
 					System.out.println("Please enter a number between 0 and 2");
 					printOptions();
 				}
 			} catch (NumberFormatException e) {
 				System.out.println("Please enter a number between 0 and 2");
 			} catch (IOException e) {
 				System.out.println("Please enter a number between 0 and 2");
 			}
 			
 			if (i==0){
 				exportMostCorrelated();
 				printOptions();
 			}
 			else if (i==1){
 				printToScreen();
 				printOptions();
 			}
 			else if (i==2){
 				System.exit(0);
 			}
 		}
 	}
 	
 	public void printOptions(){
 		System.out.println("Options: ");
 		System.out.println("0 - Export results to a CSV file");
 		System.out.println("1 - Print results to screen");
 		System.out.println("2 - Exit Program (WARNING - ALL RESULTS WILL BE ERASED)");
 	}
 	
 	public void printToScreen(){
 		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
 		System.out.println("Please enter a p-Value cutoff (between 0 and 1)");
 		double i = 0.05;
 		try {
 			i = Double.valueOf(br.readLine());
 			if (i<0 || i>1){
 				System.out.println("Please enter a p-Value cutoff between 0 and 1");
 			}
 		} catch (NumberFormatException e) {
 			System.out.println("Please enter a p-Value cutoff between 0 and 1");
 			return;
 		} catch (IOException e) {
 			System.out.println("Please enter a p-Value cutoff between 0 and 1");
 			return;
 		}
 		
 		ArrayList<HashMap<String, Double>> a = extractMostCorrelated(i);
 
 		HashMap<String, Double> mostCorrelated = a.get(0);
 		HashMap<String, Double> mostCorrelatedFDR = a.get(1);
 		
 		System.out.println("Module" + '-' + "Statistical Test P-Value" + '-' + "FDR P-Value");
 		for (String s : mostCorrelated.keySet()){
 			System.out.println(s + '-' + mostCorrelated.get(s) + '-' + mostCorrelatedFDR.get(s));
 		}
 		
 	}
 	
 	public ArrayList<HashMap<String, Double>> extractMostCorrelated(double cutoff){
 		ArrayList<HashMap<String, Double>> rt = new ArrayList<HashMap<String, Double>>();
 		HashMap<String, Double> mostCorrelated = new HashMap<String, Double>();
 		HashMap<String, Double> mostCorrelatedFDR = new HashMap<String, Double>();
 		
 		for (String s : allResults.keySet()){
 			for (ArrayList<HashMap<String, Double>> ahhs : allResults.get(s).keySet()){
 				HashMap<String, Double> original = ahhs.get(0);
 				HashMap<String, Double> adjusted = ahhs.get(1);
 				for (String set : original.keySet()){
 					if (adjusted.containsKey(set)){
 						if (original.get(set)<cutoff && adjusted.get(set)<cutoff){
 							mostCorrelated.put(set, original.get(set));
 							mostCorrelatedFDR.put(set, adjusted.get(set));
 						}
 					}
 				}
 			}
 		}
 		
 		rt.add(mostCorrelated);
 		rt.add(mostCorrelatedFDR);
 		
 		return rt;
 		
 	}
 	
 	private void fixOriginalResults(){
 		HashMap<String, HashMap<String, Double>> newOriginal = new HashMap<String, HashMap<String, Double>>();
 		for (String s : this.originalResults.keySet()){
 			HashMap<String, Double> newHash = new HashMap<String, Double>();
 			for (String t : this.originalResults.get(s).keySet()){
 				
 				String[] st = t.split(":");
 				HashSet<String> hs = new HashSet<String>();
 				for (int i=0; i<st.length; i++){
 					hs.add(st[i]);
 				}
 				String newString = "";
 				for (String u : hs){
 					newString = newString + u + ":";
 				}
 				newString = newString.substring(0, newString.length()-1);
 				Double d = this.originalResults.get(s).get(t);
 				//d = (double)Math.round(d * 10000) / 10000;
 				newHash.put(newString, d);
 			}
 			newOriginal.put(s,  newHash);
 		}
 		
 		this.originalResults = newOriginal;
 		
 	}
 	
 	private void moveShuffled(){
 		shuffling = new HashMap<String, Multimap<String, Double>>();
 		HashMap<String, Multimap<String, Double>> lastCore = combinedShuffling.get(combinedShuffling.size()-1);
 		for (String str : lastCore.keySet()){
 			Multimap<String, Double> mhsd = ArrayListMultimap.create();
 			for (String hs : lastCore.get(str).keySet()){
 				for (Double d : lastCore.get(str).get(hs)){
 					mhsd.put(hs, d);
 				}
 			}
 			shuffling.put(str, mhsd);
 		}
 		
 		for (int i=1; i<combinedShuffling.size(); i++){
 			HashMap<String, Multimap<String, Double>> thisCore = combinedShuffling.get(i);
 			for (String s : thisCore.keySet()){
 				for (String hs : thisCore.get(s).keySet()){
 					for (Double d : thisCore.get(s).get(hs)){
 						shuffling.get(s).put(hs, d);
 					}
 				}
 			}
 		}
 	}
 	
 	private void adjustResults(){
 		adjustedResults = new HashMap<String, HashMap<String, Double>>();
 		for (String s : originalResults.keySet()){
 			FDRAdjust fdr = new FDRAdjust(originalResults.get(s), shuffling.get(s));
 			HashMap<String, Double> adjusted = fdr.fdrAdjust();
 			adjustedResults.put(s, adjusted);
 		}
 	}
 	
 	private HashMap<String, HashMap<ArrayList<HashMap<String, Double>>, Multimap<String, Double>>> resultsFormat(){
 		HashMap<String, HashMap<ArrayList<HashMap<String, Double>>, Multimap<String, Double>>> allResults = new HashMap<String, HashMap<ArrayList<HashMap<String, Double>>, Multimap<String, Double>>>();
 		for (String s : adjustedResults.keySet()){
 			ArrayList<HashMap<String, Double>> ah = new ArrayList<HashMap<String, Double>> ();
 			ah.add(originalResults.get(s));
 			ah.add(adjustedResults.get(s));
			ah.add(classification.get(s));
 			//ah.add(adjustedWithR.get(s));
 			HashMap<ArrayList<HashMap<String, Double>>, Multimap<String, Double>> hah = new HashMap<ArrayList<HashMap<String, Double>>, Multimap<String, Double>>();
 			hah.put(ah,  shuffling.get(s));
 			allResults.put(s, hah);
 		}
 
 		return allResults;
 	}
 	
 	public void exportMostCorrelated(){
 		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
 		System.out.println("Please enter a p-Value cutoff (between 0 and 1)");
 		double i = 0.05;
 		try {
 			i = Double.valueOf(br.readLine());
 			if (i<0 || i>1){
 				System.out.println("Please enter a p-Value cutoff between 0 and 1");
 				return;
 			}
 		} catch (NumberFormatException e) {
 			System.out.println("Please enter a p-Value cutoff between 0 and 1");
 			return;
 		} catch (IOException e) {
 			System.out.println("Please enter a p-Value cutoff between 0 and 1");
 			return;
 		}
 		
 		ArrayList<HashMap<String, Double>> a = extractMostCorrelated(i);
 
 		HashMap<String, Double> mostCorrelated = a.get(0);
 		HashMap<String, Double> mostCorrelatedFDR = a.get(1);
 		
 		final String lineSep = System.getProperty("line.separator");
 		String fileName = null;
 		FileWriter fout = null;
 		
 		System.out.println("Please enter a valid path to save the file");
 		try {
 			fileName = br.readLine();
 		} catch (IOException e1) {
 			e1.printStackTrace();
 			return;
 		}
 
 		try {
 				//fileName = file.getAbsolutePath();
 				if (!fileName.substring(fileName.length()-4,fileName.length()).equals(".csv")){
 					fileName = fileName + ".csv";
 				}
 				fout = new FileWriter(fileName);
 				
 				
 				fout.write("Module" + ',' + "Statistical Test P-Value" + ',' + "FDR P-Value" + lineSep);
 				for (String s : mostCorrelated.keySet()){
 					fout.write(s + ',' + mostCorrelated.get(s) + ',' + mostCorrelatedFDR.get(s) + lineSep);
 				}
 				fout.write(lineSep);
 			
 				fout.write("HyperModules Results" + lineSep);
 				fout.write("Date: " + ',' + DateFormat.getDateTimeInstance().format(new Date()) + lineSep + lineSep);
 				
 				fout.write("Shuffle Number: "+ ',' + shuffleNumber + lineSep);
 				fout.write("Statistical Test: " + ','+ stat + lineSep + lineSep);
 		} 
 		catch (IOException e) {
 			JOptionPane.showMessageDialog(null,
 										  e.toString(),
 										  "Error Writing to \"" + fileName + "\"",
 										  JOptionPane.ERROR_MESSAGE);
 		} 
 		catch (Exception e){
 			e.printStackTrace();
 		}
 		finally {
 			if (fout != null) {
 				try {
 					fout.close();
 					System.out.println("file saved.");
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 		
 	}
 	
 	
 	
 }
