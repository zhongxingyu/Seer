 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.BufferedReader;
 import java.io.PrintWriter;
 import java.io.IOException;
 
 import java.util.ArrayList;
 import java.util.Properties;
 import java.util.StringTokenizer;
 
 import javax.management.Query;
 
 
 /* ga */
 /* sources : http://docs.oracle.com/javase/tutorial/essential/io/charstreams.html */
 /* sources : http://docs.oracle.com/javase/tutorial/essential/environment/properties.html */
 public class Main {
 
 	public static void main(String[] args) throws IOException {
 		
		if (false) {
 
 			System.out.println("Please enter two input file names");
 		}
 		else {
 
 			File fquery = new File(args[0]);
 			File fconfig = new File(args[1]);
 
 			//File fquery = new File("query.txt");
 			//File fconfig = new File("config.txt");
 			/* 		AL queryLines holds one line from query.txt per AL's element 	*/
 			ArrayList<String> queryLines = new ArrayList<String>();  
 
 			BufferedReader inputStream = null;
 			PrintWriter outputStream = null;
 
 			/* 		reading query.txt into AL		*/
 			try {
 				inputStream = new BufferedReader(new FileReader(fquery));
 
 				String l;
 				while ((l = inputStream.readLine()) != null) {
 					queryLines.add(l);
 				}
 			} 
 			catch (IOException e) {
 				e.printStackTrace();
 			} 
 			finally {
 				if (inputStream != null) {
 					try {     	
 						inputStream.close();
 					} 
 					catch (IOException e) {
 						e.printStackTrace();
 					}
 				}
 			}
 			/* 		reading config.properties file 		*/
 			Properties costProps = new Properties();
 			FileInputStream in = null;
 
 			try {
 				in = new FileInputStream(fconfig);
 			} 
 			catch (FileNotFoundException e) {
 				e.printStackTrace();
 			}
 			try {
 				costProps.load(in);
 			}
 			catch (IOException e) {
 				e.printStackTrace();
 			}
 			try {
 				in.close();
 			} 
 			catch (IOException e) {
 				e.printStackTrace();
 			}
 			//actual code starts here    
 			int numPasses = queryLines.size();
 			ArrayList<Float> tempValues;
 			float[][] probs = new float[numPasses][]; //stores the probabilities in a 2D array
 
 			//builds the 2D array with all of the probabilities
 			for (int i = 0; i < queryLines.size(); i++) {
 				String line = queryLines.get(i);
 				StringTokenizer parse = new StringTokenizer(line);
 				tempValues = new ArrayList<Float>();
 				while(parse.hasMoreElements()){
 					String num = String.valueOf(parse.nextElement());
 					tempValues.add(Float.valueOf(num));
 				}
 				probs[i] = new float[tempValues.size()];
 				for (int j = 0; j < tempValues.size(); j++) {
 					probs[i][j] = tempValues.get(j);
 				}
 			}
 			
 			outputStream = new PrintWriter(new FileWriter("output.txt"));
 			
 			//int currentLevel = 0; //represents what row of probabilities we are working on
 			for (int currentLevel = 0; currentLevel < probs.length; currentLevel++) {
 				ArrayList<Term> termsArrayList = Term.generateTermArray(probs[currentLevel].length);
 				Term.fillArrayCosts(termsArrayList, costProps, probs[currentLevel]);
 				
 				//loop that builds optimal plan
 				for (int i = 0; i < termsArrayList.size(); i++) {
 					for (int j = 0; j < termsArrayList.size(); j++) {
 						if(i == j){}
 						else if (termsArrayList.get(i).compareCMetrics(termsArrayList.get(j))) { //checks for CMetric optimization
 						}
 						else if (termsArrayList.get(i).compareDMetrics(termsArrayList.get(j)) && (termsArrayList.get(i)).calcProductivites(probs[currentLevel]) <= 0.5) {
 							
 						}
 						else{
 							if (termsArrayList.get(i).canCombine(termsArrayList.get(j))) {
 								// checks if a && b is better than aUb
 								float combCost = termsArrayList.get(i).calcDoubAnd(termsArrayList.get(j), costProps, probs[currentLevel]);
 								int index = Term.calcValue(termsArrayList.get(i), termsArrayList.get(j)); //sequences are stored in the index of their binary value - 1 since the all 0 sequence has been removed
 								//int[] cmbrep = Term.combinedRep(termsArrayList.get(i), termsArrayList.get(j));
 								//String cmbrepString = Term.repToString(cmbrep);
 								//System.out.println("The && is: " + combCost + " union cost is " + termsArrayList.get(index - 1).cost + " for " + termsArrayList.get(i).repToString()+ " with " + termsArrayList.get(j).repToString() + " vs " + termsArrayList.get(index - 1).repToString() + " index: " + index);
 								float compVal = combCost - termsArrayList.get(index - 1).cost;
 								if (compVal < 0 && Math.abs(compVal) > 0.001) { //checks to see if the && plan is less than the & plan
 									termsArrayList.get(index - 1).leftSeq = termsArrayList.get(i);
 									termsArrayList.get(index - 1).rightSeq = termsArrayList.get(j);
 									termsArrayList.get(index - 1).cost = combCost; //sets the new cost
 									termsArrayList.get(index - 1).costAlgo = 2;
 									termsArrayList.get(i).costAlgo = 0;
 									//System.out.println("A replacement has been made. " + termsArrayList.get(i).repToString() + " + " + termsArrayList.get(j).repToString() + " = " + termsArrayList.get(index - 1).repToString());
 								}
 								// checks if b&&a is better than bUa
 								float combCost2 = termsArrayList.get(j).calcDoubAnd(termsArrayList.get(i), costProps, probs[currentLevel]);
 								//int index2 = Term.calcValue(termsArrayList.get(i), termsArrayList.get(j)); //sequences are stored in the index of their binary value - 1 since the all 0 sequence has been removed
 								//System.out.println("The && is: " + combCost + " union cost is " + termsArrayList.get(index - 1).cost + " for " + termsArrayList.get(i).repToString()+ " with " + termsArrayList.get(j).repToString() + " vs " + termsArrayList.get(index - 1).repToString() + " index: " + index);
 								float compVal2 = combCost2 - termsArrayList.get(index - 1).cost;
 								if (compVal2 < 0 && Math.abs(compVal2) > 0.001) { //checks to see if the && plan is less than the & plan
 									termsArrayList.get(index - 1).leftSeq = termsArrayList.get(j);
 									termsArrayList.get(index - 1).rightSeq = termsArrayList.get(i);
 									termsArrayList.get(index - 1).cost = combCost2; //sets the new cost
 									termsArrayList.get(index - 1).costAlgo = 2;
 									termsArrayList.get(j).costAlgo = 0;
 									//System.out.println("A replacement has been made. " + termsArrayList.get(j).repToString() + " + " + termsArrayList.get(i).repToString() + " = " + termsArrayList.get(index - 1).repToString());
 								}
 								
 							}
 						}
 						
 					}
 				}
 				termsArrayList.get(termsArrayList.size()-1).printCodeOutput(probs[currentLevel], outputStream);
 			}
 			
 			outputStream.close();
 		}
 	}
 }
