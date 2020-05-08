 import java.util.ArrayList;
 import java.util.Scanner;
 import java.io.File;
 import java.io.IOException;
 import edu.rit.pj.Comm;
 import edu.rit.util.Random;
 import edu.rit.pj.ParallelTeam;
 import edu.rit.pj.ParallelRegion;
 import edu.rit.pj.LongForLoop;
 import edu.rit.pj.reduction.SharedLong;
 
 /**
  * Parallel implementation of the 3-SAT exhaustive search algorithm
  * for SMP machines that computes the total number of satisfying solutions.
  *
  * @author Christopher Wood
  * @author Eitan Romanoff
  * @author Ankur Bajoria
  **/
 public class ThreeSatExhaustiveSmp
 {
     // CNF formula parameters
     private static int numClauses;
     private static int numVars;
 
     // Shared number of satisfying solutions
     private static SharedLong numSatisfiable;
 
     // Clause collection
     private static Literal[][] formula;
 
     /**
      * The main method. Runs the program.
      **/
     public static void main(String args[]) 
     {
         // Start the clock.
         long startTime = System.currentTimeMillis();
 
         // Set up the communication with the job server.
         try
         {
             Comm.init(args);
         }
         catch (IOException e)
         {
             e.printStackTrace();
         }
 
         // Parse the command line arguments
         ThreeSatExhaustiveSmp sat = null;
         try
         {
             if (args.length == 1)
             {
                 sat = new ThreeSatExhaustiveSmp(args[0]); 
             }
             else if (args.length == 3)
             {
                 sat = new ThreeSatExhaustiveSmp(Integer.parseInt(args[0]),
                     Integer.parseInt(args[1]), Long.parseLong(args[2]));
             }
             else
             {
                 showUsage();
             }
         }
         catch (NumberFormatException e) 
         {
             System.err.println("Error: num_vars, num_clauses, and seed must be integers.");
             showUsage();
         }
 
         // Run the decision algorithm and display the result.
         long numSats = sat.decide();
         if (numSats > 0)
         {
             System.out.printf("Satisfiable with %d solutions.%n", numSats);
         }
         else
         {
             System.out.printf("Not satisfiable.%n");
         }
 
         // Stop the clock and display the time.
         long endTime = System.currentTimeMillis();
         System.out.println((endTime - startTime) + "msec");
     }
 
     /**
      * Constructor to generate a 3-CNF formula from an external file.
      */
     public ThreeSatExhaustiveSmp(String filename)
     {
         try 
         {
             File file = new File(filename);
             Scanner scanner = new Scanner(file);
 
             // Read in the number of clauses and variables
             this.numVars = Integer.parseInt(scanner.next());
             this.numClauses = Integer.parseInt(scanner.next());
 
             // Initialize the formula and variable data structure
             this.formula = new Literal[numClauses][3]; // 3 literals per clause
 
             // Now read in all of the clauses.
             for (int c = 0; c < numClauses; c++) 
             {
                 for (int i = 0; i < 3; i++) 
                 { 
                     int var = Integer.parseInt(scanner.next());
                     boolean negated = var < 0 ? true : false;
                     var = negated ? var * -1 : var;
                     if (var < 1 || var > numVars)  // fix
                     {
                         System.err.println("Error: variables must be within [1," + numVars + "]");
                         System.exit(-1);
                     }
                     Literal lit = new Literal(negated, var - 1);
                     formula[c][i] = lit;
                 }
             }
         }
         catch (Exception e) 
         {
             System.err.println("Error: " + e.getMessage());
             e.printStackTrace();
             System.exit(-1);
         }
     }
 
 
     /**
      * Constructor to generate a random 3-CNF formula for solving.
      */
     public ThreeSatExhaustiveSmp(int numVars, int numClauses, long seed)
     {
         this.numVars = numVars;
         this.numClauses = numClauses;
 
         // Initialize the PRNG and the formula/variable data structures
         Random prng = Random.getInstance(seed);
         formula = new Literal[numClauses][3]; // 3 literals per clause
 
        // Randomly generate the formula the formula
         for (int i = 0; i < numClauses; i++) 
         {
             for (int j = 0; j < 3; j++) 
             {
                 formula[i][j] = new Literal(prng.nextBoolean(), prng.nextInt(numVars));
             }
         }
     }
 
     /**
      * Determine the number of satisfying variable configurations
      * that satisfy this formula, if any.
      *
      * @return the number of satisfying instances.
      */
     public long decide() 
     {
         long tmp = 1L;
         for (int i = 0; i < numVars; i++) 
         {
             tmp <<= 1;
         }
 
         // Create the parallel team and initialize the shared counter variable.
         ParallelTeam team = new ParallelTeam();
         numSatisfiable = new SharedLong(0);
         final long numConfigurations = tmp;
         try 
         {
             team.execute(new ParallelRegion() 
             {
                 public void run() throws Exception 
                 {
                     execute(0, numConfigurations - 1, new LongForLoop() 
                     {
                         long perThreadSolutions; // per-thread solution counter.
 
                         /**
                          * Initialize the per thread solution counter to 0.
                          */
                         public void start() 
                         {
                             perThreadSolutions = 0;
                         }
 
                         public void run(long first, long last) 
                         {
                             // Initialize the variable configuration based on 
                             // the configuration index.
                             boolean[] variables = new boolean[numVars];
                             for (int index = 0; index < numVars; index++)
                             {
                                 if (((1 << (numVars - index - 1)) & first) > 0) 
                                 {
                                     variables[index] = true;
                                 }
                                 else 
                                 {
                                     variables[index] = false;
                                 }
                             }
 
                             // Iterate over every possible configuration for our given slice of the formula
                             for (long config = first; config <= last; config++)
                             {
                                 boolean formulaValue = true;
                                 for (int c = 0; c < numClauses; c++) 
                                 {
                                     boolean clauseValue = false;
                                     for (int l = 0; l < 3 && clauseValue == false; l++) 
                                     {
                                         // A clause is only true if at least one literal is true
                                         if (formula[c][l].negated == true && !variables[formula[c][l].id])
                                         {
                                             clauseValue = true;
                                         } 
                                         else if (formula[c][l].negated == false && variables[formula[c][l].id])
                                         {
                                             clauseValue = true; 
                                         }
                                     }
 
                                     formulaValue = formulaValue && clauseValue;
                                     if (formulaValue == false) 
                                     {
                                         break;
                                     }
                                 }
 
                                 // If satisfiable, print this result
                                 if (formulaValue) 
                                 {
                                     perThreadSolutions++;
                                 }
 
                                 // Go to next configuration.
                                 for (int i = numVars - 1; i >= 0; i--)
                                 {
                                     if (variables[i] == true) 
                                     {
                                         variables[i] = false;
                                     }
                                     else 
                                     {
                                         variables[i] = true;
                                         break;
                                     }
                                 }
                             } // End per-thread for Loop
                         } // End loop's run
 
                         /**
                          * Reduce the per-thread counter value into the shared variable.
                          */
                         public void finish() 
                         {
                             numSatisfiable.addAndGet(perThreadSolutions);
                         }
 
                     }); // End parallel long loop and execute
                 } // End runs
             }); // End team.execute()
         }
         catch (Exception e) 
         {
             System.err.println("Error occurred during the parallel team's execution.");
             e.printStackTrace();
         }
 
         return numSatisfiable.get();
     }
 
     /**
      * Print the usage message and then terminate abnormally.
      */
     public static void showUsage() 
     {
         System.err.println("java ThreeSatSmp [<file> | <num_literals> <num_clauses> <seed>]");
         System.exit(-1);
     }
 }
 
