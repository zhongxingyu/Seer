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
 
 	public static void main(String[] args) {
 		
 		if (false) {
 
 			System.out.println("Please enter two input file names");
 		}
 		else {
 
 			//File fquery = new File(args[0]);
 			//File fconfig = new File(args[1]);
 
 			File fquery = new File("query.txt");
 			File fconfig = new File("config.txt");
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
 			
 			int currentLevel = 2; //represents what row of probabilities we are working on
 			ArrayList<Term> termsArrayList = Term.generateTermArray(probs[currentLevel].length);
 			Term.fillArrayCosts(termsArrayList, costProps, probs[currentLevel]);
 			
 			//loop that builds optimal plan
 			for (int i = 0; i < termsArrayList.size(); i++) {
 				for (int j = 0; j < termsArrayList.size(); j++) {
					if(false){}
 					else{
 						if (termsArrayList.get(i).canCombine(termsArrayList.get(j))) {
 							
 							float combCost = termsArrayList.get(i).calcDoubAnd(termsArrayList.get(j), costProps, probs[currentLevel]);
 							int index = Term.calcValue(termsArrayList.get(i), termsArrayList.get(j)); //sequences are stored in the index of their binary value - 1 since the all 0 sequence has been removed
 							int[] cmbrep = Term.combinedRep(termsArrayList.get(i), termsArrayList.get(j));
							String cmbrepString = Term.repToString(cmbrep);
							System.out.println("The && is: " + combCost + " union cost is " + termsArrayList.get(index - 1).cost + "for " + cmbrepString + " and " + termsArrayList.get(index - 1).repToString());
 							if (combCost < termsArrayList.get(index - 1).cost) { //checks to see if the && plan is less than the & plan
 								termsArrayList.get(index - 1).leftSeq = termsArrayList.get(i);
 								termsArrayList.get(index - 1).rightSeq = termsArrayList.get(j);
 								termsArrayList.get(index - 1).cost = combCost; //sets the new cost
 								termsArrayList.get(index - 1).costAlgo = 2;
 								System.out.println("A replacement has been made. " + termsArrayList.get(i).repToString() + " + " + termsArrayList.get(j).repToString() + " = " + termsArrayList.get(index - 1).repToString());
 							}
 						}
 					}
 					
 				}
 			}
 			/*
 			for (Term term : termsArrayList) {
 				System.out.println(term.repToString() + " costs: " + term.cost);
 			}
 			
 			System.out.println(termsArrayList.get(termsArrayList.size()-1).repToString());
 			System.out.println(termsArrayList.get(termsArrayList.size()-1).costAlgo);
 			System.out.println(termsArrayList.get(termsArrayList.size()-1).cost + " " + termsArrayList.get(termsArrayList.size()-1).algoName[termsArrayList.get(termsArrayList.size()-1).costAlgo]);
 			*/
 			//termsArrayList.get(termsArrayList.size()-1).printCodeOutput(probs[currentLevel]);
 			/*
 			for (Term term : termsArrayList) {
 				System.out.println("The cost is " + term.cost + " and it uses the algo: " + term.algoName[term.costAlgo]);
 			}
 			*/
 
 			/* 		writing dummy output.txt 	*/
 			/*     try {
 
 			outputStream = new PrintWriter(new FileWriter("output.txt"));
 
 		        for (String line : queryLines) {
 
 	            	outputStream.println("==================================================================");
 	                outputStream.println(line);
 	                outputStream.println("------------------------------------------------------------------");
 	                outputStream.println();
 	                outputStream.println("OPTIMAL PLAN HERE"); 
 	                outputStream.println();
 	                outputStream.println("------------------------------------------------------------------");
 	                outputStream.println("cost: XX.X");
 		        }
 		} 
 	        catch (IOException e) {
 
 				e.printStackTrace();
 		}
 	        finally {
 
 	            if (outputStream != null) {
 
 	                outputStream.close();
 	            }
 	        }
 	        //		AL elements test    
 	        System.out.println("AL test");
 	        for (String line : queryLines) {
 
 	        	System.out.println(line);
 	        }
 	        // 		Properties test     
 	        // 	NOTE : properties values of type String!    
 	        System.out.println("Properties test");
 	        System.out.println("* NOTE : properties values are of type String!");
 	        System.out.println("# : " + costProps.size());
 	        System.out.println("r = " + costProps.getProperty("r"));
 	        System.out.println("t = " + costProps.getProperty("t"));
 	        System.out.println("l = " + costProps.getProperty("l"));
 	        System.out.println("m = " + costProps.getProperty("m"));
 	        System.out.println("a = " + costProps.getProperty("a"));
 	        System.out.println("f = " + costProps.getProperty("f"));
 			*/
 		}
 	}
 }
