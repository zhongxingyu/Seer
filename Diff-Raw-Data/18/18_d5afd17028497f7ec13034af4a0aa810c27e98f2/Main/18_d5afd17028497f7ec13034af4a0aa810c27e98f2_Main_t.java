 package instanceerproperty;
 
 import edacc.model.ExperimentResult;
 import java.io.ObjectInputStream;
 import java.util.ArrayList;
 
 /**
  *
  * @author simon
  */
 public class Main {
 
     public static final int SAT = 11;
     public static final int UNSAT = 10;
     public static final String help =
             "Instance Property Calculator from Experiment Results\n"
             + "----------------------------------------------------\n"
             + "Expects an object stream containing an array list of experiment results.\n"
             + "Usage: java -jar InstanceERProperty.jar [Option] [--expid <id>]\n"
             + " Option:\n"
             + "  --help     prints this help\n"
             + "  --mintime  prints the minimum time needed to solve this instance\n"
             + "  --avgtime  prints the average time needed to solve this instance\n"
             + "  --expid    if specified, use results only from the experiment with that id\n";
 
     public static void printHelp() {
         System.out.println(help);
     }
 
     public static Float getAvgTime(ArrayList<ExperimentResult> er, Integer expId) {
         float res = 0.f;
         int count = 0;
         for (ExperimentResult e : er) {
             if (expId != null && e.getExperimentId() != expId) {
                 continue;
             }
             if (e.getResultCode().getResultCode() == SAT || e.getResultCode().getResultCode() == UNSAT) {
                 res += e.getResultTime();
                 count++;
             }
         }
         if (count == 0) {
             return null;
         } else {
             return (res / (float) count);
         }
     }
 
     public static Float getMinTime(ArrayList<ExperimentResult> er, Integer expId) {
         Float res = null;
         for (ExperimentResult e : er) {
             if (expId != null && e.getExperimentId() != expId) {
                 continue;
             }
             if (e.getResultCode().getResultCode() == SAT || e.getResultCode().getResultCode() == UNSAT) {
                 if (res == null || res > e.getResultTime()) {
                     res = e.getResultTime();
                 }
             }
         }
         return res;
     }
 
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) throws Exception {
         if (args.length < 1) {
             printHelp();
         } else {
             if ("--help".equals(args[0])) {
                 printHelp();
             } else if ("--mintime".equals(args[0]) || "--avgtime".equals(args[0])) {
                 Integer expId = null;
                if (args.length > 1 && "--expid".equals(args[1])) {
                     expId = Integer.parseInt(args[2]);
                 }
                 ObjectInputStream is = new ObjectInputStream(System.in);
                 ArrayList<ExperimentResult> er = (ArrayList<ExperimentResult>) is.readUnshared();
                 if ("--mintime".equals(args[0])) {
                     Float res = getMinTime(er, expId);
                     if (res != null) {
                         System.out.println(res);
                     } else {
                         System.out.println("");
                     }
                 } else if ("--avgtime".equals(args[0])) {
                     Float res = getAvgTime(er, expId);
                     if (res != null) {
                         System.out.println(res);
                     } else {
                         System.out.println("");
                     }
                 }
             }
         }
     }
 }
