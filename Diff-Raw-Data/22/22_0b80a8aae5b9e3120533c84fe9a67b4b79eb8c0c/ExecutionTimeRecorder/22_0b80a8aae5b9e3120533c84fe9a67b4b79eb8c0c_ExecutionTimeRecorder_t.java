 package minalloy;
 /* This class implements a program that records the execution time of alloy specs.*/
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Stack;
 import java.util.StringTokenizer;
 
 import kodkod.ast.Relation;
 import kodkod.instance.Instance;
 import kodkod.instance.Tuple;
 
 import org.kohsuke.args4j.CmdLineException;
 import org.kohsuke.args4j.CmdLineParser;
 import org.kohsuke.args4j.opts.BooleanOption;
 import org.kohsuke.args4j.opts.FileOption;
 import org.kohsuke.args4j.opts.IntOption;
 
 import edu.mit.csail.sdg.alloy4.A4Reporter;
 import edu.mit.csail.sdg.alloy4.Err;
 import edu.mit.csail.sdg.alloy4.ErrorWarning;
 import edu.mit.csail.sdg.alloy4compiler.ast.Command;
 import edu.mit.csail.sdg.alloy4compiler.ast.Module;
 import edu.mit.csail.sdg.alloy4compiler.parser.CompUtil;
 import test.translator.A4Options;
 import test.translator.A4Solution;
 import test.translator.TranslateAlloyToKodkod;
 import minalloy.translator.MinA4Options;
 import minalloy.translator.MinA4Solution;
 import minalloy.translator.MinTranslateAlloyToKodkod;
 import minsolver.ExplorationException;
 import minsolver.MinSolution.MinimizationHistory;
 
 
 public final class ExecutionTimeRecorder {
     /*
      * Execute every command in every file.
      *
      * This method parses every file, then execute every command.
      *
      * If there are syntax or type errors, it may throw
      * a ErrorSyntax or ErrorType or ErrorAPI or ErrorFatal exception.
      * You should catch them and display them,
      * and they may contain filename/line/column information.
      */
     public static void main(String[] args) throws Err {
     	//The input spec file
     	FileOption optInput = new FileOption("-i");
     	//The output file
     	FileOption optOutput = new FileOption("-o");
     	//Produce minimal solutions (by default non-minimal)
     	BooleanOption optMinimal = new BooleanOption("-m", false);
     	//Number of models to produce
     	IntOption optNumberOfModels = new IntOption("-n", 10);
     	//Symmetry Breaking (Equal to Aluminum's SB by default)
     	IntOption optSymmetryBreaking = new IntOption("-sb", 20);
     	//Record augmentation time
     	FileOption optAugmentation = new FileOption("-a");
     	//Number of trials
     	IntOption optNumberOfTrials = new IntOption("-t", 1);
     	//Record minimization history as opposed to logging execution time
     	BooleanOption optLogMinimizationHistory = new BooleanOption("-hist", false);
     	//Record the execution time for getting consistent facts
     	BooleanOption optLogConsistentFacts = new BooleanOption("-cf", false);    	
 
     	
     	CmdLineParser optParser = new CmdLineParser();
     	optParser.addOption(optInput);
     	optParser.addOption(optOutput);
     	optParser.addOption(optMinimal);
     	optParser.addOption(optNumberOfModels);
     	optParser.addOption(optSymmetryBreaking);
     	optParser.addOption(optAugmentation);
     	optParser.addOption(optNumberOfTrials);
     	optParser.addOption(optLogMinimizationHistory);
     	optParser.addOption(optLogConsistentFacts);
     	
     	try{
     		optParser.parse(args);
     	}
     	catch(CmdLineException e){
     		System.err.println(e.getMessage());
     	}
 
     	if(optInput.value == null){
     		System.err.println("No input file is provided!");
     		System.exit(0);
     	}
     	if(optOutput.value == null){
     		System.err.println("No output file is provided!");
     		System.exit(0);
     	}
     	if(optLogMinimizationHistory.value){	//If -hist is active, then run it only for one trial for only minimal solutions
     		optMinimal.value = true;
     		optNumberOfTrials.value = 1;
     	}
     	if(optLogConsistentFacts.value){	//If -cf is active, then run it for only minimal solutions
     		optMinimal.value = true;
     	}    	
     	if(!optMinimal.value && optAugmentation.value != null){
     		System.err.println("Augmentation is only applicable on minimal model finding.");
     		System.exit(0);
     	}
     	if(optLogConsistentFacts.value && optLogMinimizationHistory.value){
     		System.err.println("One one of -hist or -cf can be active.");
     		System.exit(0);
     	}
     	
     	//TODO this is the worst code ever! Consider revision:
     	if(optMinimal.value)
     		solveMinimal(optInput, optOutput, optMinimal, optNumberOfModels, optSymmetryBreaking, optAugmentation, optNumberOfTrials, 
     				optLogMinimizationHistory, optLogConsistentFacts);
     	else
     		solveNonMinimal(optInput, optOutput, optMinimal, optNumberOfModels, optSymmetryBreaking, optNumberOfTrials);
     }
 
     /**
      * Runs the tests using Aluminum
      */
 	private static void solveMinimal(FileOption optInput, FileOption optOutput, 
 			BooleanOption optMinimal, IntOption optNumberOfModels, 
 			IntOption optSymmetryBreaking, FileOption optAugmentation, 
 			IntOption optNumberOfTrials, BooleanOption optLogMinimizationHistory,
 			BooleanOption optLogConsistentFacts) throws Err {
 		
 		//Loads a dummy model in order to load Kodkod classes.
 		try{
 			initMinimal();
 		}
 		catch(Exception e){
 			System.err.println("Initialization error!");
 			System.exit(0);
 		}
 		
 		ArrayList<String> output = new ArrayList<String>();    	
         // Alloy4 sends diagnostic messages and progress reports to the A4Reporter.
         // By default, the A4Reporter ignores all these events (but you can extend the A4Reporter to display the event for the user)
         A4Reporter rep = new A4Reporter() {
             // For example, here we choose to display each "warning" by printing it to System.out
             @Override public void warning(ErrorWarning msg) {
                 System.out.print("Relevance Warning:\n"+(msg.toString().trim())+"\n\n");
                 System.out.flush();
             }
         };
 
         // Parse+typecheck the model
         System.out.println("Parsing+Typechecking "+optInput.value.getName());
         output.add("Spec: " + optInput.value.getName());
         
         System.out.println("-m = " + optMinimal.value);
         output.add("-m = " + optMinimal.value);
         System.out.println("-sb = " + optSymmetryBreaking.value);
         output.add("-sb = " + optSymmetryBreaking.value);
         System.out.println("-n = " + optNumberOfModels.value);
         output.add("-n = " + optNumberOfModels.value);
         System.out.println("-hist = " + optLogMinimizationHistory.value);
         output.add("-hist = " + optLogMinimizationHistory.value);
         System.out.println("-cf = " + optLogConsistentFacts.value + "\n");
         output.add("-cf = " + optLogConsistentFacts.value + "\n");        
         System.out.println("-t = " + optNumberOfTrials.value + "\n"); 
         
         Module world = CompUtil.parseEverything_fromFile(rep, null, optInput.value.getPath());
 
         // Choose some default options for how you want to execute the commands
         MinA4Options options = new MinA4Options();
         options.symmetry = optSymmetryBreaking.value;
         options.logMinimizationHistory = optLogMinimizationHistory.value;
         
         Stack<AugmentationElement> stack = null;
         if(optAugmentation.value != null){
         	try{
         		stack = parseAugmentationFile(optAugmentation.value);
         	}
         	catch(IOException e){
         		System.err.println(e.getMessage());
         		System.exit(0);
         	}
         }
 
         for (Command command: world.getAllCommands()) {
     		// Execute the command
     		System.out.println("Command: "+command + "-------------\n");
     		output.add("Command:\t" + command.toString() + "-------------\n");
     		
             //Keeps the number of items in the output so far. We keep this number to add data in the next trials.
             int lineNumber = output.size();
     		
         	for(int i = 0; i < optNumberOfTrials.value; i++){   
         		System.out.println("TRIAL " + (i + 1) + "------");
 
         		if(optLogMinimizationHistory.value){
         			System.out.println("invocations\telements\tattributes\trelations");
         			output.add("invocations\telements\tattributes\trelations");
         		}
         		else{
         			if(optLogConsistentFacts.value){
             			System.out.println("time\t#facts\ttotalaugmentation time");
             			//output.add("time\t#facts");
         			}else{
 	        			System.out.println("time");
 	        			//output.add("time");
         			}
         		}
         		
         		MinA4Solution ans = null;
         		try{
         			ans = getFirstSolution(rep, world, command, options, output, stack, optLogMinimizationHistory.value, optLogConsistentFacts.value, i, lineNumber);
         		}
         		catch(ExplorationException e){
         			System.err.println(e.getMessage());
         			System.exit(0);
         		}
 
         		long time = 0;
         		int consistentFacts = 0;
         		int counter = 1;
         		
         		if(!optLogConsistentFacts.value && !optLogMinimizationHistory.value);
 
         		while(ans.satisfiable())
         		{
             		long totalAugmentationTimeNS = 0;
       		
         			if(counter == optNumberOfModels.value)
         				break;
         			
         			//System.out.println(ans.toString());
         			
        			//time = System.currentTimeMillis();
         			ans = ans.next();
         			//time = System.currentTimeMillis() - time;
         			time = ans.getCurrentSolution().stats().solvingTime();
 
         			if(optLogConsistentFacts.value){
         				if(ans.satisfiable()){
         					//Get all the consistent facts:
 	            			time = System.currentTimeMillis();
 	            			Instance facts = ans.getConsistentFacts();
 	            			time = System.currentTimeMillis() - time;
 	                        consistentFacts = 0;	
 	                        // Compute the total number of consistent facts:
 	                        for(Relation relation: facts.relations()){
 	                        	consistentFacts += facts.tuples(relation).size();
 	                        }
 	                        
 	                        // Compute the time to augment the solution using the consistent facts:
 	                        if(consistentFacts > 0) {	
 	                        	//  if there are any consistent facts
 	                        	for(Relation relation: facts.relations()){
 	                        		for(Tuple tuple: facts.tuples(relation)){
 	                        			// "This method provides nanosecond precision, but not necessarily nanosecond accuracy."
 			                        	long augmentationTimeNS = System.nanoTime();
 			                        	try{
 			                        		// No translation (so we can use the tuple string in raw form)
 			                        		ans = ans.lift(relation.toString()+tuple.toString(), null);
 			                        		augmentationTimeNS = System.nanoTime() - augmentationTimeNS;
 				                        	totalAugmentationTimeNS += augmentationTimeNS;
 			                        		ans = ans.backtrack();
 			                        	}
 			                        	catch(ExplorationException e){
 			                        		System.err.println(e.getMessage());
 			                        		System.exit(1);
 			                        	} catch (IOException e) {
 			                        		System.err.println(e.getMessage());
 			                        		System.exit(2);
 										}
 	                        		}
 	                        	}
 	                        	//Freeing up the garbage that augmentation and backtracking produces:
                         		System.gc();
 	                        }
         				}
         				else{
         					time = -1;
         					consistentFacts = -1;
         				}
         			}
         			
         			String info = null;
         			if(optLogMinimizationHistory.value){        				
         				MinimizationHistory history = ans.getCurrentSolution().minimizationHistory;
         				
         				if(history == null) //The last unsatisfiable solution comes back with a null history.
         					break;
         				
         				info = history.SATSolverInvocations + "\t" + history.reducedElements + "\t" + history.reducedAttributes + "\t" + history.reducedRelations;
         			}else{
         	        	if(optLogConsistentFacts.value){
         	        		info = time + "\t" + consistentFacts + "\t" + (totalAugmentationTimeNS/1000000);        		
         	        	}
         	        	else{
         	        		info = new Long(time).toString();
         	        	}        				
         			}
         			
         			System.out.println(++counter + ": " + info);
         			if(i == 0)
             			output.add(info);
         			else{
         				if(!optLogConsistentFacts.value && !optLogMinimizationHistory.value)
         					output.set(counter + lineNumber, output.get(counter + lineNumber) + "\t" + info);
         				else
         					output.set(counter + lineNumber -1, output.get(counter + lineNumber - 1) + "\t" + info);        				
         			}
         		} // end for each solution
         		
         		//Writing the current state of data to a file.
     	        try{
     	        	//We are not keeping appending data to a previous log file.
     	        	writeOutput(output, optOutput.value, false);
     	        }
     	        catch(IOException e){
     	        	System.err.println(e.getMessage());
     	        }        	
         	}	        
         }
 	}
 	
 	private static MinA4Solution getFirstSolution(A4Reporter rep, Module world, Command command, 
 			MinA4Options options, ArrayList<String> output, Stack<AugmentationElement> stack, boolean logMinimizationHistory, 
 			boolean logConsistentFacts, int trial, int lineNumber)
 					throws Err, ExplorationException{
         long time = 0;
         long translTime = 0;
         int consistentFacts = 0;
         
         //time = System.currentTimeMillis();
         MinA4Solution ans = MinTranslateAlloyToKodkod.execute_command(rep, world.getAllReachableSigs(), command, options);
         //time = System.currentTimeMillis() - time;
         translTime = ans.getCurrentSolution().stats().translationTime();
         time = ans.getCurrentSolution().stats().solvingTime();
         
         int counter = 1;
 
         while(stack!= null && !stack.empty()){
         	AugmentationElement aug = stack.pop();
         	while(counter != aug.solutionNumber){
         		ans = ans.next();
         		counter ++;
         	}
         	
         	//time = System.currentTimeMillis();
         	ans = ans.lift(aug.augmentingFact, null);
         	translTime = ans.getCurrentSolution().stats().translationTime();
         	time = ans.getCurrentSolution().stats().solvingTime();
         	//time = System.currentTimeMillis() - time;	
         }
         
 		long totalAugmentationTimeNS = 0;		
         if(logConsistentFacts){ //When logging consistent facts, ignore the time to fetch the last model.
 			if(ans.satisfiable()){
 				//Get all the consistent facts:
     			time = System.currentTimeMillis();
     			Instance facts = ans.getConsistentFacts();
     			time = System.currentTimeMillis() - time;
     			
                 consistentFacts = 0;	
                 // Compute the total number of consistent facts:
                 for(Relation relation: facts.relations()){
                 	consistentFacts += facts.tuples(relation).size();
                 }
                 
                 // Compute the time to augment the solution using the consistent facts:
                 if(consistentFacts > 0) {	
                 	//  if there are any consistent facts
                 	for(Relation relation: facts.relations()){
                 		for(Tuple tuple: facts.tuples(relation)){
                 			// "This method provides nanosecond precision, but not necessarily nanosecond accuracy."
                         	long augmentationTimeNS = System.nanoTime();
                         	try{
                         		// No translation (so we can use the tuple string in raw form)
                         		ans = ans.lift(relation.toString()+tuple.toString(), null);
                         		augmentationTimeNS = System.nanoTime() - augmentationTimeNS;
 	                        	totalAugmentationTimeNS += augmentationTimeNS;	                        	
                         		ans = ans.backtrack();
                         	}
                         	catch(ExplorationException e){
                         		System.err.println(e.getMessage());
                         		System.exit(1);
                         	} catch (IOException e) {
                         		System.err.println(e.getMessage());
                         		System.exit(2);
 							}
                         	
                 		}
                 	}
                 }
 			}
 			else{
 				time = -1;
 				consistentFacts = -1;
 			}
         }
         
         String info = null;
         if(logMinimizationHistory){
         	MinimizationHistory history = ans.getCurrentSolution().minimizationHistory;
         	info = history.SATSolverInvocations + "\t" + history.reducedElements + "\t" + history.reducedAttributes + "\t" + history.reducedRelations;
         }
         else{
         	if(logConsistentFacts){
         		info = time + "\t" + consistentFacts + "\t" + (totalAugmentationTimeNS/1000000);        		
         	}
         	else{
         		info = new Long(time).toString();        		
         	}
         }
         
         if(!logMinimizationHistory && !logConsistentFacts)
         	System.out.println("0: " + translTime);
         	
         System.out.println("1: " + info);
         if(trial == 0){
             if(!logMinimizationHistory && !logConsistentFacts)
             	output.add(new Long(translTime).toString());
         	output.add(info);
         }
         else{
             if(!logMinimizationHistory && !logConsistentFacts){
             	output.set(lineNumber, output.get(lineNumber) + "\t" + translTime);
             	lineNumber++;
             }
         	output.set(lineNumber, output.get(lineNumber) + "\t" + info);
         }
 		
 		return ans;
 	}
 	
 	/**
 	 * Loads Kodkod's classes by loading a dummy spec.
 	 */
 	private static void initMinimal() throws Err{
         A4Reporter rep = new A4Reporter() {
             // For example, here we choose to display each "warning" by printing it to System.out
             @Override public void warning(ErrorWarning msg) {
                 System.out.print("Relevance Warning:\n"+(msg.toString().trim())+"\n\n");
                 System.out.flush();
             }
         };		
 		
         Module world = CompUtil.parseEverything_fromFile(rep, null, "resources/test.als");
         
         // Choose some default options for how you want to execute the commands
         MinA4Options options = new MinA4Options();
         options.symmetry = 20;
         
         for(Command command: world.getAllCommands())
         	MinTranslateAlloyToKodkod.execute_command(rep, world.getAllReachableSigs(), command, options);   
 	}
 	
 	/**
 	 * Runs the tests using Alloy 
 	 */
 	private static void solveNonMinimal(FileOption optInput, FileOption optOutput, 
 			BooleanOption optMinimal, IntOption optNumberOfModels, 
 			IntOption optSymmetryBreaking, IntOption optNumberOfTrials) throws Err {
 		//Loads a dummy model in order to load Kodkod classes.
 		try{
 			initNonMinimal();
 		}
 		catch(Exception e){
 			System.err.println("Initialization error!");
 			System.exit(0);
 		}		
 		
 		ArrayList<String> output = new ArrayList<String>();    	
         // Alloy4 sends diagnostic messages and progress reports to the A4Reporter.
         // By default, the A4Reporter ignores all these events (but you can extend the A4Reporter to display the event for the user)
         A4Reporter rep = new A4Reporter() {
             // For example, here we choose to display each "warning" by printing it to System.out
             @Override public void warning(ErrorWarning msg) {
                 System.out.print("Relevance Warning:\n"+(msg.toString().trim())+"\n\n");
                 System.out.flush();
             }
         };
 
         // Parse+typecheck the model
         System.out.println("Parsing+Typechecking "+optInput.value.getName());
         output.add("Spec: " + optInput.value.getName());
         
         System.out.println("-m = " + optMinimal.value);
         output.add("-m = " + optMinimal.value);
         System.out.println("-sb = " + optSymmetryBreaking.value);
         output.add("-sb = " + optSymmetryBreaking.value);
         System.out.println("-n = " + optNumberOfModels.value + "\n");
         output.add("-n = " + optNumberOfModels.value + "\n");
         
         Module world = CompUtil.parseEverything_fromFile(rep, null, optInput.value.getPath());
 
         // Choose some default options for how you want to execute the commands
         A4Options options = new A4Options();
         options.symmetry = optSymmetryBreaking.value;
 
         for (Command command: world.getAllCommands()) {	
         	// Execute the command
         	System.out.println("Command: "+command + "-------------\n");
         	output.add("Command:\t" + command.toString() + "-------------\n");
 
             //Keeps the number of items in the output so far. We keep this number to add data in the next trials.
             int lineNumber = output.size();
 
         	for(int i = 0; i < optNumberOfTrials.value; i++){
         		System.out.println("TRIAL " + (i + 1) + "------");
         		System.out.println("time");
         		
         		long time = 0;
         		long translTime = 0;
         		//time = System.currentTimeMillis();
         		A4Solution ans = TranslateAlloyToKodkod.execute_command(rep, world.getAllReachableSigs(), command, options);
         		//time = System.currentTimeMillis() - time;
         		translTime = ans.getCurrentSolution().stats().translationTime();
         		time = ans.getCurrentSolution().stats().solvingTime();
         		
         		int counter = 0;
 
         		System.out.println("0: " + translTime);
         		System.out.println("1: " + time);
         		if(i == 0){
         			output.add(new Long(translTime).toString());
         			output.add(new Long(time).toString());
         			counter++;
         		}
         		else{
         			output.set(lineNumber + counter, output.get(lineNumber + counter++) + "\t" + translTime);
         			output.set(lineNumber + counter, output.get(lineNumber + counter) + "\t" + time);
         		}
 
         		while(ans.satisfiable()){
         			if(counter == optNumberOfModels.value)
         				break;
         		
         			//time = System.currentTimeMillis();                	
         			ans = ans.next();
         			//time = System.currentTimeMillis() - time;
         			time = ans.getCurrentSolution().stats().solvingTime();
 
         			System.out.println(++counter + ": " + time);
         			if(i == 0)
         				output.add(new Long(time).toString());
         			else
         				output.set(counter + lineNumber, output.get(counter + lineNumber) + "\t" + time);
         		}
         		
         		//Write the results of this trial to the output.
                 try{
                 	//We do not support appending the data to the end of a previous report file.
                 	//We simply write the current state of data after each trial.
                 	writeOutput(output, optOutput.value, false);
                 }
                 catch(IOException e){
                 	System.err.println(e.getMessage());
                 }
         	}
         }        
 	}
     
 	/**
 	 * Loads Kodkod's classes by loading a dummy spec.
 	 */
 	private static void initNonMinimal() throws Err{
         A4Reporter rep = new A4Reporter() {
             // For example, here we choose to display each "warning" by printing it to System.out
             @Override public void warning(ErrorWarning msg) {
                 System.out.print("Relevance Warning:\n"+(msg.toString().trim())+"\n\n");
                 System.out.flush();
             }
         };		
 		
         Module world = CompUtil.parseEverything_fromFile(rep, null, "resources/test.als");
         
         // Choose some default options for how you want to execute the commands
         A4Options options = new A4Options();
         options.symmetry = 20;
         
         for(Command command: world.getAllCommands())
         	TranslateAlloyToKodkod.execute_command(rep, world.getAllReachableSigs(), command, options);   
 	}
 	
 	/**
 	 * Helper methods
 	 */
     private static void writeOutput(ArrayList<String> output, File outputFile, boolean append) throws IOException{
     	FileWriter fstream = new FileWriter(outputFile,append);
     	BufferedWriter out = new BufferedWriter(fstream);
     	for(String str: output){
     		out.write(str + "\n");
     	}
     	out.close();
     }
     
     private static Stack<AugmentationElement> parseAugmentationFile(File file) 
     		throws IOException{
     	Stack<AugmentationElement> result = new Stack<AugmentationElement>();
     	
 		FileReader fstream = new FileReader(file);
     	BufferedReader in = new BufferedReader(fstream);
     	
     	String currentLine;
     	while((currentLine = in.readLine()) != null){
     		//Skip comment lines
     		if(currentLine.startsWith("--"))
     			continue;
     		
     		StringTokenizer tokenizer = new StringTokenizer(currentLine, "\t");
     		AugmentationElement element = new AugmentationElement(
     				new Integer(tokenizer.nextToken()).intValue(), tokenizer.nextToken());
     		
     		result.add(element);
     	}
     	
     	in.close();
     	return result;
     }
 }
 
 /**
  * A data structure that holds information about a solution that is being augmented and
  * a fact that is augmenting the solution.
  */
 class AugmentationElement {
 	int solutionNumber;
 	String augmentingFact;
 	
 	AugmentationElement(int solutionNumber, String augmentationFact){
 		this.solutionNumber = solutionNumber;
 		this.augmentingFact = augmentationFact;
 	}
 }
