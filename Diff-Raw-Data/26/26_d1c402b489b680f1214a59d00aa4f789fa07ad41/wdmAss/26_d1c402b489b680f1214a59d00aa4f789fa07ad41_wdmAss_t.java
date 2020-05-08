 import java.io.FileNotFoundException;
 import java.text.DecimalFormat;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.Vector;
 
 public class wdmAss {
 
 	static int _flag=0;							// flag=0 is OR. flag=1 is AND
 	static double _initPR = 1;					// Initial PageRank value
 	static double _epsilon = 0.01;				// The value for which we will stop trying to update a vertex's pageRank
 	static double _damping = 0.3;				// Damping Factor
 	static graph g = new graph();				// graph of websites
	static double balancePR = 0.01;				// a factor designed to balance the weight of PR in the relevance scores
 	static DecimalFormat df = new DecimalFormat("###.#########################"); //formatter for results
 
 	public static void main(String[] args) throws FileNotFoundException
 	{
 		String option = args[0];
         if(option.equals("-graph")){
 			String[] files = Arrays.copyOfRange(args,1,args.length);
 			g.genCleanPrint(files,_initPR);  	// Creates the graph, cleans it from dead links and prints it to screen
 			return;
 		}
 		else if(option.equals("-pr")){
 			String[] files = Arrays.copyOfRange(args,1,args.length);
 			double startTime = System.currentTimeMillis();
 			g.genCleanCalc(files,_initPR,_epsilon,_damping);  	// Creates the graph
 			double estimatedTime = System.currentTimeMillis() - startTime;
 			System.out.println("Execution time: " + estimatedTime);
 			System.out.println();
             //print  PageRanks
 			System.out.println("PageRanks:");
 			Iterator<vertex> verItr = g.vertexVec.iterator();
 			while(verItr.hasNext()) //iterate over vertices and print their PR
 			{
 				vertex v = verItr.next();
 				System.out.println(v.name +": "+ df.format(v.newPR));
 			}
 		}
 		else if(option.equals("-tf")){
 			TF tf = new TF();
 			String[] doc = new String[1];
 			doc[0] = args[1];
 			tf.calculateTF(doc); //calculate tf score of word in doc
 			System.out.println("TF: "+df.format(tf.getTF(doc[0],args[2])));
 			return;
 		}
 		else if(option.equals("-ta")){
 			boolean oneWord = false;
 			Vector<String> words = new Vector<String>();
 			int k=1;
 			if(args.length > 1 && args[1].contains("(")){			//parse the word set and determine the boolean operator
 				if(args[k].contains(")")){							//only one word was given without operator
 					words.add(args[1].substring(1,args[1].indexOf(")")).toLowerCase());
 					oneWord = true;
 				}
 				else{
 					words.add(args[1].substring(1).toLowerCase());
 				}
 				for(k=2;!oneWord && !args[k].contains(")");k++){	//parse the boolean expression given
 					if(args[k].equalsIgnoreCase("OR")){
 						_flag = 0;
 					}
 					else if(args[k].equalsIgnoreCase("AND")){
 						_flag = 1;
 					}
 				}
 				if(!oneWord){
 					words.add(args[k].substring(0,args[k].indexOf(")")));   //add last word of the set
 				}
 			}
 			else{
 				System.out.println("Please provide words to search for in the format (word1 [OR / AND] word2)");
 				return;
 			}
 			String[] files = Arrays.copyOfRange(args,k+1,args.length);
 			g.genCleanCalc(files,_initPR,_epsilon,_damping);  	// Creates the graph
 			TF tf = new TF();
 			tf.calculateTF(files); //calculate tf scores for all words in each website and save in a data structure
 			TA ta = new TA(tf,g,words,_flag);
 			ta.run();
 
 		}
 		else if(option.equals("-help")){
 			System.out.println("Options:\n-graph\tCreate a graph from the given files\n");
 			System.out.println("-help\tDisplay command line options\n");
 			System.out.println("-pr\tCalculate PageRank values for a graph generated from files\n");
 			System.out.println("-tf document word\tCalculate TF value for a word in a given file\n");
 			System.out.println("-ta (word1 OR/AND word2)\tRun Threshold Algorithm for a given set of words & documents and display most relevant documents\n");
         }
 		else{
 			System.out.println("Usage: wmdAss.jar [options] file1 file2...");
 		}
 	}
 }
 
 
