 package hypermodulesrun;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.util.ArrayList;
 import java.util.HashSet;
 
 import com.google.common.collect.ArrayListMultimap;
 import com.google.common.collect.Multimap;
 
 public class MainRun {
 	
 	public static void main(String[] args){
 		/*
 		if (args.length!=5){
 			System.out.println("Please enter correct number of arguments.");
 			System.out.println("USAGE: java -jar [*.jar] [PATH_TO_NETWORK_INTERACTIONS] [PATH_TO_SAMPLE_VALUES] [PATH_TO_CLINICAL_VALUES] [SHUFFLE_NUMBER] [STAT_TEST]");
 		}
 		*/
 			ArrayList<String[]> network = null;
 			ArrayList<String[]> sampleValues = null;
 			ArrayList<String[]> clinicalValues = null;
 			int shuffleNumber = 1000;
 			String stat = "logrank";
 			double pValueCutoff = 0.05;
 			int numberCores = Runtime.getRuntime().availableProcessors();
 			//System.err.println(numberCores);
 			
 			/*
 			for (int i=0; i<args.length; i++){
 				System.out.println(args[i]);
 			}
 			*/
 			if (args.length<2){
 				printManual();
 				return;
 			}
 			
 			
 			for (int i=0; i<args.length; i++){
 				if (args[i].charAt(0)=='-'){
 					if (args[i].length()!=2){
 						System.err.println("Invalid flag usage.");
 						printManual();
 						return;
 					}
 					if (i+1<args.length){
 						if (args[i+1].charAt(0)!='-'){
 							switch(args[i].charAt(1)){
 							case 'n':
 								network = readCSVfile(args[i+1]);
 								break;
 							case 's':
 								sampleValues = readCSVfile(args[i+1]);
 								break;
 							case 'c':
 								clinicalValues = readCSVfile(args[i+1]);
 								break;
 							case 'S':
 								shuffleNumber = Integer.valueOf(args[i+1]);
 								break;
 							case 'p':
 								pValueCutoff = Double.valueOf(args[i+1]);
 								break;
 							case 't':
 								stat = args[i+1];
 								break;
 							case 'C':
 								numberCores = Integer.valueOf(args[i+1]);
 								break;
 							}
 						}
 					}
 					if(args[i].charAt(1)=='h'){
 						printManual();
 						return;
 					}
 				}
 			}
 			
 			System.err.println("Number of background permutations: " + shuffleNumber);
 		/*
 			network = readCSVfile(args[0]);
 			sampleValues = readCSVfile(args[1]);
 			clinicalValues = readCSVfile(args[2]);
 			shuffleNumber = Integer.valueOf(args[3]);
 			stat = args[4];
 		*/
 			if (!stat.equals("logrank") && !stat.equals("fisher")){
 				System.err.println("Please provide either \"logrank\" or \"fisher\" as the test flag");
 				System.err.println("Enter java -jar *.jar -h for more information");
 				return;
 			}
 			
 			if (network==null){
 				System.err.println("Please provide a network interaction file");
 			}
 			
 			if (sampleValues==null){
 				System.err.println("Please provide a patient-mutation data file");
 			}
 			
 			if (clinicalValues==null){
 				System.err.println("Please provide a clinical data file");
 			}
 			
 			
 			if (sampleValues!=null){
 				handleSampleValues(sampleValues);
 			}
 			
 			if (stat.equals("logrank")){
 				if (!handleSurvivalExceptions(clinicalValues)){
 					System.err.println("Enter java -jar *.jar -h for more information");
 					return;
 				}
 			}
 			else{
 				if (!handleClinicalVariableExceptions(clinicalValues)){
 					System.err.println("Enter java -jar *.jar -h for more information");
 					return;
 				}
 			}
 			
 			
 			if (network!=null && sampleValues!=null && clinicalValues!=null){
 				AlgorithmTask at = new AlgorithmTask(network, sampleValues, clinicalValues, shuffleNumber, stat, pValueCutoff, numberCores);
 				at.run();
 			}
 			else{
 				System.err.println("Enter java -jar *.jar -h for more information");
 				return;
 			}
 	}
 	
 	public static void handleSampleValues(ArrayList<String[]> genes2samplesvalues){
 		for (int i=0; i<genes2samplesvalues.size(); i++){
 			if (genes2samplesvalues.get(i)[1]!=null){
 				if (genes2samplesvalues.get(i)[1].equals(" ")||genes2samplesvalues.get(i)[1].isEmpty())
 				genes2samplesvalues.get(i)[1] = "no_sample";
 			}
 		}
 		
 	}
 
 	public static boolean handleSurvivalExceptions(ArrayList<String[]> clinicalValues){
 		boolean valid = true;
 		HashSet<String> vitals = new HashSet<String>();
 		for (int i=0; i<clinicalValues.size(); i++){
 			if (clinicalValues.get(i).length<3){
 				System.err.println("Please enter 3 columns for log rank");
 				return false;
 			}
 			if (clinicalValues.get(i)[1]!=null){
 				if (!clinicalValues.get(i)[1].isEmpty()){
 					vitals.add(clinicalValues.get(i)[1]);
 				}
 
 			}
 			
 			//System.out.println(clinicalValues.get(i)[1]);
 			if (vitals.size() > 2){
 				System.err.println("INPUT ERROR: Vital Status must be either DECEASED or ALIVE");
 				return false;
 			}
 			if (clinicalValues.get(i)[2]!=null){
 				if (!clinicalValues.get(i)[2].equals("NA")){
 					try{
 						Double.valueOf(clinicalValues.get(i)[2]);
 					}
 					catch (Exception e){
 						valid = false;
 					}
 				}
 			}
 		}
 		if (valid == false){
 			System.err.println("INPUT ERROR: All days followup entries must be a number");
 		}
 		return valid;
 	}
 	
 	public static boolean handleClinicalVariableExceptions(ArrayList<String[]> otherValues){
 		boolean valid = true;
 		HashSet<String> variables = new HashSet<String>();
 		for (int  i=0; i<otherValues.size(); i++){
 			if (otherValues.get(i)[1]!=null){
 				if (otherValues.isEmpty()){
 					otherValues.remove(i);
 				}
 				else{
 					variables.add(otherValues.get(i)[1]);
 					//System.out.println(otherValues.get(i)[1]);
 				}
 			}
 
 			if (variables.size() > 5){
 				System.err.println("INPUT ERROR: Please pick a clinical variable with fewer categories");
 				return false;
 			}
 		}
 		System.err.println("Number of fisher categories: " + variables.size());
 		return valid;
 	}
 	
 	
 	public static void printManual(){
 		System.out.println("HyperModules Command Line Version");
 		System.out.println();
 		System.out.println("SUMMARY:");
 		System.out.println("\tUSAGE: java -jar [*.jar] [-n network_interaction_file] [-s samplemutationdata] [-c clinicaldata] [-t statistical_test] [-S shuffle_number] [-C numberofprocessors] [-p pvaluecutoff]");
 		System.out.println("\tthe first three fields are mandatory.");
		System.out.println("OPTIONS:");
		System.out.println("\t-t \t Either \"logrank\" or \"fisher\". By default, this is set to logrank");
		System.out.println("\t-C \t The number of cores you with to allocate to HyperModules. By default, this is set to all available cores at runtime.");
 		System.out.println("\t-S \t The number of background permutations. By default, this is set to 1000.");
 		System.out.println("\t-p \t The p-value cutoff for significance. By default, this is set to 0.05");
 		System.out.println("\t-h \t Prints the manual.");
 		System.out.println();
 		System.out.println("Please consult baderlab.org/Software/HyperModules for more information.");
 	}
 	
 	 public static ArrayList<String[]> readCSVfile(String path) {
 	   	     ArrayList<String[]> Rs = new ArrayList<String[]>();
 		     String[] OneRow;
 		     
 	            try {
 	            BufferedReader brd = new BufferedReader (new FileReader(path));
 
 	            String st = brd.readLine();
 	            
 	            	while (st!=null) {
 	            			OneRow = st.split(",|\\s|;|\t");
 	            			Rs.add(OneRow);
 	            			st = brd.readLine();
 	                } 
 		            brd.close();
 	            } 
 	            catch (Exception e) {
 	                String errmsg = e.getMessage();
 	                System.out.println ("File not found:" +errmsg);
 	                return null;
 	            }       
 	        return Rs;
	        }
 
 	
 }
 
