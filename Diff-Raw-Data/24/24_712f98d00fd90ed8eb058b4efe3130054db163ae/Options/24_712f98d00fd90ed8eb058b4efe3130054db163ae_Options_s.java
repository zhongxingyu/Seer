 package org.vika.routing;
 
 import java.util.HashMap;
 
 /**
  * @author oleg
  */
 public class Options {
     private static final String INPUT_FILE = "input";
     private static final String OUTPUT_FILE = "output";
     private static final String TIME_QUANT = "quantum";
     private static final String TIME_LIMIT = "time";
     private static final String MESSAGE_COUNT = "messages";
     private static final String EXPERIMENT_COUNT = "experiments";
     private static final String SHOW = "show";
     private boolean showUi = false;
     private final HashMap<String, String> myValues = new HashMap<String, String>();
 
     public String getInputFile(){
         return myValues.get(INPUT_FILE);
     }
 
     public String getOutputFile(){
         return myValues.get(OUTPUT_FILE);
     }
 
     public int getTimeQuant(){
         return myValues.containsKey(TIME_QUANT) ? Integer.valueOf(myValues.get(TIME_QUANT)) : 100;
     }
 
     public int getTimeLimit(){
         return myValues.containsKey(TIME_LIMIT) ? Integer.valueOf(myValues.get(TIME_LIMIT)) : 100;
     }
 
     public int getMessagesCount(){
         return myValues.containsKey(MESSAGE_COUNT) ? Integer.valueOf(myValues.get(MESSAGE_COUNT)) : 1;
     }
 
     public int getExperimentCount(){
         return myValues.containsKey(EXPERIMENT_COUNT) ? Integer.valueOf(myValues.get(EXPERIMENT_COUNT)) : 1;
     }
 
     public boolean showUi() {
         return showUi;
     }
 
     public static Options readOptions(final String[] args){
         final Options result = new Options();
         for (int i=0;i<args.length;i++){
             String arg = args[i];
             if (!arg.startsWith("--")){
                 System.err.println("Wrong parameter: " + arg);
                 printUsage();
                 System.exit(0);
             }
             arg = arg.substring(2);
             if (INPUT_FILE.equals(arg)){
                 ensureNotEnd(args, i);
                 result.myValues.put(INPUT_FILE, args[i+1]);
                 i++;
             }
             else if (OUTPUT_FILE.equals(arg)) {
                 ensureNotEnd(args, i);
                 result.myValues.put(OUTPUT_FILE, args[i+1]);
                 i++;
             }
             else if (TIME_QUANT.equals(arg)) {
                 ensureNotEnd(args, i);
                 result.myValues.put(TIME_QUANT, args[i+1]);
                 i++;
             }
             else if (TIME_LIMIT.equals(arg)) {
                 ensureNotEnd(args, i);
                 result.myValues.put(TIME_LIMIT, args[i+1]);
                 i++;
             }
             else if (SHOW.equals(arg)){
                 result.showUi = true;
             }
             else {
                 System.err.println("Unknown argument: " + arg);
                 printUsage();
                 System.exit(0);
             }
         }
         return result;
     }
 
     private static void ensureNotEnd(String[] args, int i) {
         if (i == args.length - 1){
             System.err.println("Unexpected end");
             printUsage();
             System.exit(0);
         }
     }
 
     public static void printUsage() {
         System.out.println("Usage description:");
         System.out.println("Input file: --" + INPUT_FILE);
         System.out.println("Output file: --" + OUTPUT_FILE);
         System.out.println("Time quantum: --" + TIME_QUANT + " default = 100");
         System.out.println("Time limit: --" + TIME_LIMIT + " default = 100");
         System.out.println("Messages count: --" + MESSAGE_COUNT + " default = 1");
         System.out.println("Experiment count: --" + EXPERIMENT_COUNT + " default = 1");
         System.out.println("Show UI: --" + SHOW);
         System.out.println("Sample input file:\n" +
                 "5\n" +
                 "6\n" +
                 "0 1 6\n" +
                 "1 2 5\n" +
                 "2 3 3\n" +
                 "2 0 4\n" +
                 "0 3 2\n" +
                 "3 4 1");
     }
 }
