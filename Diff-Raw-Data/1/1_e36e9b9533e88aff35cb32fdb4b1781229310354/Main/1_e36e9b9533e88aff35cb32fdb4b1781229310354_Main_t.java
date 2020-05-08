 package uk.co.mtford.jalp;
 
 import org.apache.log4j.Logger;
 import uk.co.mtford.jalp.abduction.Result;
 import uk.co.mtford.jalp.abduction.logic.instance.IInferableInstance;
 import uk.co.mtford.jalp.abduction.logic.instance.term.VariableInstance;
 import uk.co.mtford.jalp.abduction.parse.program.ParseException;
 import uk.co.mtford.jalp.abduction.parse.query.JALPQueryParser;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.LinkedList;
 import java.util.List;
 
 import static java.lang.System.nanoTime;
 
 /**
  * Controls point of access to either command line or the interpreter.
  * @author Michael Ford
  */
 public class Main {
 
     private static Logger LOGGER = Logger.getLogger(Main.class);
 
     private static final String CMD_START = "-";
     private static final String REDUCE_OPTION = CMD_START+"r";
     private static final String QUERY_OPTION = CMD_START+"q";
     private static final String EFFICIENT_OPTION = CMD_START+"e";
     private static final String DEBUG_OPTION = CMD_START+"d";
     private static final String HELP_OPTION = CMD_START+"h";
 
     private static boolean reduce = false;
     private static boolean efficient = false;
     private static boolean debug = false;
 
     private static String query = null;
     private static LinkedList<String> fileNames = new LinkedList<String>();
     private static String debugFolder = null;
 
     private static void printError(String text) {
         System.err.println(text);
         System.exit(-1);
     }
 
     private static void printError(String text, Throwable throwable) {
         System.err.println(text);
         System.err.println(throwable);
         System.exit(-1);
     }
 
     public static void main(String[] args) throws InterruptedException {
         for (int i=0;i<args.length;i++) {
 
             String s = args[i];
             if (s.equals(REDUCE_OPTION)) {
                 reduce = true;
             }
             if (s.equals(EFFICIENT_OPTION)) {
                 efficient = true;
             }
             else if (s.equals(QUERY_OPTION)) {
                 i++;
                 query = args[i];
             }
             else if (s.equals(DEBUG_OPTION)) {
                 debug = true;
                 i++;
                 debugFolder = args[i];
             }
             else if (s.equals(HELP_OPTION)) {
                 printHelp();
             }
             else {
                 fileNames.add(args[i]);
             }
         }
 
         if (query!=null && fileNames.isEmpty()) {
             printError("You can't run a query when no abductive theory has been loaded.");
             System.exit(-1);
         }
 
         JALPSystem system = new JALPSystem();
 
         for (String fileName:fileNames) {
             System.out.println("Loading "+fileName);
             try {
                 system.mergeFramework(new File(fileName));
             } catch (FileNotFoundException e) {
                 printError("File "+fileName+" doesn't exist.");
             } catch (ParseException e) {
                 printError("Parse error.",e);
             }
         }
 
         if (debug) {
             System.out.println("Generating visualizer and logs in folder "+debugFolder);
             try {
                 system.generateDebugFiles(query,debugFolder);
             } catch (IOException e) {
                 printError("IO problem whilst generating output.",e);
             } catch (JALPException e) {
                 printError("JALP encountered a problem.",e);
             } catch (uk.co.mtford.jalp.abduction.parse.query.ParseException e) {
                 printError("Error parsing query.",e);
             }
         }
 
         else {
             if (query!=null) {
                 try {
                     List<IInferableInstance> queryList = JALPQueryParser.readFromString(query);
 
                     List<Result> results;
 
                     long startTime = nanoTime();
 
                     if (efficient) {
                         results = system.efficientQuery(new LinkedList<IInferableInstance>(queryList));
                     }
                     else {
                         results = system.query(new LinkedList<IInferableInstance>(queryList));
                     }
 
                     long finishTime = System.nanoTime();
 
                     if (results.isEmpty()) {
                         System.out.println("Computed no explanations in "+(finishTime-startTime)/1000+" microseconds.");
                     }
 
                     else {
                         if (reduce) {
                             System.out.println("Computed " +results.size() + " explanations in "+(finishTime-startTime)/1000+" microseconds.\n");
                             LinkedList<VariableInstance> relevantVariables = new LinkedList<VariableInstance>();
                             for (IInferableInstance inferable:queryList) {
                                 relevantVariables.addAll(inferable.getVariables());
                             }
                             for (Result r:results) {
                                 r.reduce(relevantVariables);
                             }
                         }
                         JALP.printResults(queryList,results);
                     }
 
 
                     System.out.println("Exiting...");

                 } catch (JALPException e) {
                     printError("JALP encountered a problem.",e);
                 } catch (uk.co.mtford.jalp.abduction.parse.query.ParseException e) {
                     printError("Error parsing query",e);
                 }
             }
             else {
                 JALPInterpreter jalpInterpreter = new JALPInterpreter(system);
                 jalpInterpreter.start();
             }
 
 
         }
 
     }
     /** Prints a help message detailing available commands.
      *
      */
     private static void printHelp() {
         System.out.println("Syntax: ( (filename)* (<option>)* )*");
         System.out.println("-q <query> - Execute a query.");
         System.out.println("-d <folder> - Create log file and visualizer in <folder>");
         System.out.println("-r - Enable reduce mode.");
         System.out.println("-e - Enable efficient mode.");
         System.out.println("-h - This help.");
     }
 }
