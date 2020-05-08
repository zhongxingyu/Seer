 package proteinprediction;
 
 /**
  * This is the main class of our ProteinPredictor. Bacause our proteinpredictor
  * supports more stuff, than only predicting the protein structure, we have
  * different "program modes".
  * 
  * @author Yann
  */
 public class ProteinPrediction {
 
     /**
      * All program modes, we support until now! All program nodes are simultaneously
      * "program entry points"
      */
     private static ProgramEntryPoint[] pEPs = new ProgramEntryPoint[] {
        new proteinprediction.io.DatasetGenerator()
     };
     
     /**
      * This function parse the command line arguments and decides, which entry
      * program mode should be selected. This program node get's all the rest
      * parameters
      * 
      * @param args the command line arguments
      */
     public static void main(String[] args) {
         try {
             if(args[0].equalsIgnoreCase("help")) {
                 printHelp();
                 System.exit(ProgramSettings.PROGRAM_EXIT_NORMAL);
             }
             // check all program nodes, if they fit
             for(int i = 0; i < pEPs.length; i++) {
                 if(args[0].equalsIgnoreCase(pEPs[i].getCommandLineArgumentName())) {
                     // help output?
                     if("help".equalsIgnoreCase(args[1])) {
                         System.err.println(pEPs[i].getUsageAndHelp());
                         System.exit(0);
                     }
                     // call prog
                     String[] newArgs = new String[args.length - 1];
                     System.arraycopy(args, 1, newArgs, 0, args.length - 1);
                     int r = pEPs[i].run(newArgs);
                     // print usage for programs that don't exit normally
                     if(r == ProgramSettings.PROGRAM_EXIT_MALFORMED_ARGS) {
                         System.err.println("Malformed arguments for program mode " + pEPs[i].getShortDescription());
                         System.err.println(pEPs[i].getUsageAndHelp());
                     } else if(r == ProgramSettings.PROGRAM_EXIT_IOERROR) {
                         System.err.println("Input/Output Error!");
                     }
                     System.exit(r);
                 }
             }
         } catch(IndexOutOfBoundsException e) {
             System.err.println("Missing or wrong program mode parameter!");
             System.err.println();
             printHelp();
             System.exit(ProgramSettings.PROGRAM_EXIT_ERROR);
         } catch(Exception e) {
             System.err.println("Error reading commandline arguments! This is an unexpected error! Quitting!");
             System.exit(ProgramSettings.PROGRAM_EXIT_UNEXPECTED_ERROR);
         }
     }
     
     /**
      * prints an help message
      */
     public static void printHelp() {
         System.err.println("help message of: " + ProteinPrediction.class.getProtectionDomain().getCodeSource().getLocation().getPath());
         System.err.println("currently supported program modes:");
         for(int i = 0; i < pEPs.length; i++) {
             System.err.println(" " + pEPs[i].getCommandLineArgumentName() + " - " + pEPs[i].getShortDescription());
         }
         System.err.println("to call a program mode call this jar with <program mode> <program mode arguments> arguments");
         System.err.println("to get more informations about a program mode, call jar with <program mode> help");
     }
     
 }
