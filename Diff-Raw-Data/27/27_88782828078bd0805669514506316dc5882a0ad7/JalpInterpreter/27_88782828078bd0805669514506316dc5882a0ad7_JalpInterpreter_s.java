 package uk.co.mtford.jalp;
 
 import org.apache.log4j.Logger;
 import uk.co.mtford.jalp.abduction.AbductiveFramework;
 import uk.co.mtford.jalp.abduction.Result;
 import uk.co.mtford.jalp.abduction.logic.instance.IInferableInstance;
 import uk.co.mtford.jalp.abduction.logic.instance.equalities.IInEqualitySolver;
 import uk.co.mtford.jalp.abduction.logic.instance.term.VariableInstance;
 import uk.co.mtford.jalp.abduction.parse.program.ParseException;
 import uk.co.mtford.jalp.abduction.parse.query.JALPQueryParser;
 
 import javax.swing.plaf.basic.BasicInternalFrameTitlePane;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Scanner;
 
 import static java.lang.System.nanoTime;
 
 /**
  * Presents a command line interface to the user. The user can write out abductive theories,
  * load abductive theories from files and execute queries on those theories.
  * @author Michael Ford
  */
 public class JALPInterpreter {
 
     Logger LOGGER = Logger.getLogger(JALPInterpreter.class);
 
     private static final String COMMAND_START = ":";
     private static final String LOAD_COMMAND = COMMAND_START+"l";
     private static final String QUERY_COMMAND = COMMAND_START+"q";
     private static final String PRINT_COMMAND = COMMAND_START+"f";
     private static final String HELP_COMMAND = COMMAND_START+"h";
     private static final String CLEAR_COMMAND = COMMAND_START+"c";
     private static final String REDUCE_COMMAND = COMMAND_START+"r";
    private static final String QUIT_COMMAND = COMMAND_START+"q";
     private static final String EFFICIENT_COMMAND = COMMAND_START+"e";
     private static final String TEST_COMMAND = COMMAND_START+"t";
 
     private Scanner scanner = new Scanner(System.in);
 
     private JALPSystem system;
 
     boolean reduceMode;
     boolean efficientMode;
     int repeat;
 
     public JALPInterpreter(JALPSystem system) {
         this.system=system;
         this.reduceMode = false;
         this.efficientMode=false;
         this.repeat = 1;
     }
 
     /**
      * Starts the interpreter. Prints a help message and waits for commands.
      */
     public void start() throws InterruptedException {
         System.out.println("Welcome to JALP. Type :h for help.");
         String next = null;
         while(true) {
             scanner = new Scanner(System.in); // Quick bug fix where JALP-> appears twice...
             System.out.flush();
             System.err.flush();
             System.out.print("JALP->");
             next=scanner.nextLine();
             next = next.trim();
             if (next.length()==0) continue;
             if (next.startsWith(LOAD_COMMAND)) {
                 try {
                     loadFrameworkFromFile(next);
                 } catch (FileNotFoundException e) {
                     System.err.println("File " + next + " does not exist.");
                 } catch (ParseException e) {
                     e.printStackTrace();
                 }
             }
             else if (next.startsWith(QUERY_COMMAND)) {
                 try {
                     executeQuery(next);
                 } catch (uk.co.mtford.jalp.abduction.parse.query.ParseException e) {
                     e.printStackTrace();
                 }
             }
             else if (next.startsWith(PRINT_COMMAND)) printFramework();
             else if (next.startsWith(HELP_COMMAND)) printHelp();
             else if (next.startsWith(CLEAR_COMMAND)) resetSystem();
             else if (next.startsWith(REDUCE_COMMAND)) toggleReduceMode();
             else if (next.startsWith(EFFICIENT_COMMAND)) toggleEfficientMode();
             else if (next.startsWith(TEST_COMMAND)) {
                 testMode(next);
             }
             else if (next.startsWith(QUIT_COMMAND)) quit();
             else {
                 try {
                     loadFrameworkFromString(next);
                 } catch (ParseException e) {
                     e.printStackTrace();
                 }
             }
         }
     }
 
     /**
      * Processes the test mode command whereby queries are repeated and an average execution time returned.
      *
      * @param next
      */
     private void testMode(String next) {
         try {
             next = next.substring(2, next.length());
             int n = Integer.parseInt(next.trim());
             if (n>0) {
                 repeat = n;
             }
             else {
                 System.err.println("Must be greater than 0.");
             }
         }
         catch (NumberFormatException e) {
             System.err.println("Error: Must be a number.");
         }
     }
 
     /** Prints a help message detailing available commands.
      *
      */
     private void printHelp() {
         System.out.println(":l <filename> -  Load a file.");
         System.out.println(":q <query> - Execute a query.");
         System.out.println(":f - View framework.");
         System.out.println(":c - Reset framework.");
         System.out.println(":r - Enable reduce mode.");
         System.out.println(":e - Enable efficient mode.");
         System.out.println(":t <integer n> - Enable test mode. Run the query n times.");
        System.out.println(":q - Quit.");
     }
 
     /** Terminates the interpreter.
      *
      */
     private void quit() {
         System.out.println("Bye.");
         System.exit(0);
     }
 
     /**
      * Clears the abductive framework.
      */
     private void resetSystem() {
         system.setFramework(new AbductiveFramework());
     }
 
     /**
      * Executes the abductive query represented by the string.
      *
      * @param next A query in prolog-like syntax e.g. 'p(X)'
      * @throws uk.co.mtford.jalp.abduction.parse.query.ParseException
      */
     private void executeQuery(String next) throws uk.co.mtford.jalp.abduction.parse.query.ParseException, InterruptedException {
        List<IInferableInstance> query = JALPQueryParser.readFromString(next.substring(2, next.length() - 1));
         List<VariableInstance> queryVariables = new LinkedList<VariableInstance>();
         for (IInferableInstance i:query) {
             queryVariables.addAll(i.getVariables());
         }
         List<Result> results = null;
         long startTime = nanoTime();
         if (repeat>1) {
             System.out.println("Test mode enabled. Executing query "+repeat+" times.");
         }
         if (!efficientMode) {
             for (int i = 0;i<repeat;i++) {
                 results = system.query(query);
             }
         }
         else {
             for (int i =0;i<repeat;i++) {
                 results = system.efficientQuery(query);
             }
         }
         long finishTime = System.nanoTime();
         if (results.isEmpty()) {
             if (repeat>1) {
                 System.out.println("Computed no explanations in "+(finishTime-startTime)/1000/repeat+" microseconds on average.");
             }
             else {
                 System.out.println("Computed no explanations in "+(finishTime-startTime)/1000+" microseconds.");
             }
         }
         else {
             if (repeat>1) {
                 System.out.println("Computed " +results.size() + " explanations in "+(finishTime-startTime)/1000/repeat+" microseconds on average.\n");
             }
             else {
                 System.out.println("Computed " +results.size() + " explanations in "+(finishTime-startTime)/1000+" microseconds.\n");
             }
             if (reduceMode) {
                 for (Result r:results) {
                     r.reduce(queryVariables);
                 }
             }
             JALP.printResults(query, results);
         }
     }
 
     private void printFramework() {
         System.out.println(system.getFramework());
     }
 
     private void loadFrameworkFromFile(String next) throws FileNotFoundException, ParseException {
         File file = new File(next.substring(2, next.length()).trim());
         system.mergeFramework(file);
     }
 
     private void loadFrameworkFromString(String next) throws ParseException {
         system.mergeFramework(next);
     }
 
     private void toggleReduceMode() {
         reduceMode=!reduceMode;
         System.out.println(reduceMode?"Reduce mode enabled.":"Reduce mode disabled.");
     }
 
     private void toggleEfficientMode() {
         efficientMode=!efficientMode;
         System.out.println(efficientMode?"Efficient mode enabled.":"Efficient mode disabled.");
     }
 
 }
