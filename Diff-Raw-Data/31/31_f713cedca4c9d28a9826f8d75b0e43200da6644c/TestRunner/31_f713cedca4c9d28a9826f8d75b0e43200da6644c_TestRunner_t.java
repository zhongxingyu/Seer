 package weka.subspaceClusterer;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 public class TestRunner {
 
     static String metrics, subspaceClutererName, outputPath, dataSetFilename, command;
     static int numProcessors;
     static ProcessBuilder procBuilder;
     static ArrayList<Process> runningProcs = new ArrayList<Process>();
 
     static void dispatch(List<String> commands) throws IOException, InterruptedException {
 
         // Keep the system busy, but don't overwhelm it.
         if (runningProcs.size() < numProcessors) {
             runningProcs.add(forkProcess(commands));
         }
 
         else {
 
            /*
             * TODO: Use a thread pool to manage each forked process, so that as soon as a process complted, the thread
             * is returned to the pool ready to be used. In the meanwhile, assume that the oldest process will be the
             * first to complete.
             */
             Process oldest = runningProcs.get(0);
             oldest.waitFor();
             runningProcs.remove(oldest);
             runningProcs.add(forkProcess(commands));
 
         }// else
     }// method
 
     static Process forkProcess(List<String> commands) throws IOException {
         if (procBuilder == null) {
             procBuilder = new ProcessBuilder(commands);
             procBuilder.inheritIO();
         }
 
         procBuilder.command(commands);
         Process proc = procBuilder.start();
         return proc;
     }// method
 
     public static void main(String[] args) throws IOException, InterruptedException {
 
         // Set state
         numProcessors = Runtime.getRuntime().availableProcessors();
         metrics = "F1Measure:Accuracy:Entropy";
         subspaceClutererName = "Mocca";
         outputPath = "C:\\results";
 
         // TODO: RUN EXPERIMENTS FOR MULTIPLE DATASETS
         // ArrayList<String> dataSetFilenames;
         dataSetFilename = "breast.arff";
 
         // Run tests
         run();
 
         // Pull all the results into one file
         Consolidator.consolidate(outputPath, "C:\\Users\\ahoffer\\Google Drive\\results.csv");
     }
 
     static void run() throws IOException, InterruptedException {
         double gamma, alpha, beta, epsilon, width, subspaceOverlapThreshold, objectOverlapThreshold;
         int experimentLabel, gammaSteps, sSteps, oSteps, alphaSteps, betaSteps, widthSteps, maxiter;
 
         maxiter = 10000;
         gammaSteps = 10;
         epsilon = 0.05;
         sSteps = 3;
         oSteps = 3;
         alphaSteps = 3;
         betaSteps = 5;
         widthSteps = 5;
         experimentLabel = 1;
 
         for (int sStep = 0; sStep <= sSteps; sStep++) {
             subspaceOverlapThreshold = sStep / ((double) sSteps);
 
             for (int oStep = 0; oStep <= oSteps; oStep++) {
                 objectOverlapThreshold = oStep / ((double) oSteps);
                 alpha = 0;
 
                 for (int alphaStep = 0; alphaStep <= alphaSteps; alphaStep++) {
                     alpha = alpha + 0.1;
                     beta = 0;
 
                     for (int betaStep = 0; betaStep <= betaSteps; betaStep++) {
                         beta = beta + 0.1;
                         gamma = 0;
 
                         for (int gammaStep = 0; gammaStep <= gammaSteps; gammaStep++) {
                             gamma = gammaStep / ((double) gammaSteps);
                             width = .05;
                             for (int widthStep = 0; widthStep <= widthSteps; widthStep++) {
                                 width = width + 0.5 * widthStep;
 
                                 String argString = String
                                         .format("-label %d -M %s -sc %s -t %s -a %f -b %f -e %f -g %f -i %f -s %f -w %f -maxiter %d -path %s -c last",
                                                 experimentLabel, metrics, subspaceClutererName, dataSetFilename, alpha,
                                                 beta, epsilon, gamma, objectOverlapThreshold, subspaceOverlapThreshold,
                                                 width, maxiter, outputPath);
 
                                 // DEBUG - Do not delete the next line. It is handy for debugging.
                                 // System.out.println(args);
 
                                 ArrayList<String> commands = new ArrayList<String>();
                                 commands.add("javaw.exe");
                                 commands.add("-cp");
                                 commands.add("\\Users\\ahoffer\\Documents\\GitHub\\sepc\\workspace\\OpenSubspace\\lib\\*;");
                                 commands.add("weka.subspaceClusterer.MySubspaceClusterEvaluation");
 
                                 /*
                                  * TODO: Splitting the command line by spaces is a hack. This will BREAK if there are
                                  * spaces in the filenames or paths
                                  */
                                 String[] args = argString.split(" ");
                                 commands.addAll(Arrays.asList(args));
                                 // System.err.println(experimentLabel);
                                 // System.err.println(argString);
                                 dispatch(commands);
 
                                 experimentLabel++;
                             }
                         }
                     }
                 }
             }
         }
     }// method
 
 }// class
