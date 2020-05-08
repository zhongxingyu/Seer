 package ussr.remote;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.rmi.RemoteException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.ArrayBlockingQueue;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.ThreadPoolExecutor;
 import java.util.concurrent.TimeUnit;
 
 import ussr.remote.facade.ActiveSimulation;
 import ussr.remote.facade.ParameterHolder;
 
 /**
  * Abstract class for running simulation batches
  * @author ups
  */
 public abstract class SimulationBatch2000 implements ReturnValueHandler {
 
     public static final int SERVER_PORT = 54323;
     public static final int LINE_BUFFER_SIZE = 20;
     public static final int DEFAULT_NUMBER_OF_THREADS = 1;
     private static final long THREAD_KEEPALIVE_MINS = 10; 
     
     private SimulationLauncherServer server;
     private PrintWriter writer;
     private Map<String,ParameterHolder> experimentParameters = new HashMap<String,ParameterHolder>();
 
     public SimulationBatch2000() {
         // Start a simulation server (one that manages a number of running simulation processes)
         try {
             server = new SimulationLauncherServer(SimulationBatch2000.SERVER_PORT);
         } catch (RemoteException e) {
             throw new Error("Unable to create server: "+e);
         }
         try {
             writer = new PrintWriter(new BufferedWriter(new FileWriter("sim_out_err.txt")));
         } catch(IOException exn) {
             throw new Error("Unable to create log file for stdout and stderr");
         }
     }
     
     protected abstract boolean runMoreSimulations();
     
     protected abstract ParameterHolder getNextParameters();
     
     private class Worker implements Runnable {
         private ParameterHolder parameters;
         private int run;
         private Class<?> mainClass;
         public Worker(ParameterHolder parameters, int run) {
             this.mainClass = parameters.getMainClass();
             this.parameters = parameters;
             this.run = run;
         }
         public void run() {
             // Start a simulation server process
             ActiveSimulation simulation;
             try {
                 simulation = server.launchSimulation();
             } catch (IOException e) {
                 throw new Error("Unable to start simulation subprocess: "+e);
             }
             // Discard standard out (avoid buffers running full)
             dumpStream("#"+run+".out ",simulation.getStandardOut());
             // Get standard err, pass it to method that prints it in separate thread
             dumpStream("#"+run+".err ", simulation.getStandardErr());
             // Wait for simulation process to be ready to start a new simulation
             if(!simulation.isReady()) {
                 System.out.println("#"+run+" Waiting for simulation");
                 simulation.waitForReady();
             }
             // Start a simulation in the remote process
             try {
                 simulation.start(mainClass, parameters, SimulationBatch2000.this);
             } catch (RemoteException e) {
                 // Normal or abnormal termination, inspection of remote exception currently needed to determine...
                System.out.println("#"+run+" Simulation stopped");
             }
             System.out.println("#"+run+" Simulation completed");
             // Register in set of parameters
             experimentParameters.put(parameters.toString(), parameters);
             // Report intermediate results
             reportRecord();
         }
     }
     
     public void start(int max_parallel_sims, boolean terminateAtEnd) {
         ThreadPoolExecutor batchExecutor = new ThreadPoolExecutor(max_parallel_sims,max_parallel_sims,THREAD_KEEPALIVE_MINS,TimeUnit.MINUTES,new LinkedBlockingQueue<Runnable>());
         int run = 0; 
         while(runMoreSimulations()) {
             ParameterHolder parameters = getNextParameters();
             Worker worker = new Worker(parameters, run++);
             batchExecutor.execute(worker);
             System.out.println("Assigning task...");
         }
         System.out.println("Waiting for task completion");
         batchExecutor.shutdown();
         try {
             while(!batchExecutor.awaitTermination(1, TimeUnit.DAYS)) {
                 System.err.println("Warning: overly long delay while waiting for task completion");
             }
         } catch (InterruptedException e1) {
             throw new Error("Interrupted while waiting for task completion");
         }
         System.out.println("Batch completed");
         reportRecord();
         if(terminateAtEnd) System.exit(0);
     }
 
     /**
      * Dump an input stream to standard out, prefixing all lines with a fixed text 
      * @param prefix the prefix to use
      * @param stream the stream to dump
      */
     private void dumpStream(final String prefix, final InputStream stream) {
         final List<String> lines = new ArrayList<String>(); 
         new Thread() {
             public void run() {
                 BufferedReader input = new BufferedReader(new InputStreamReader(stream));
                 while(true) {
                     String line;
                     try {
                         line = input.readLine();
                         if(line==null) break;
                         lines.add(prefix+": "+line);
                         if(lines.size()>LINE_BUFFER_SIZE) dumpBuffer(lines);
                     } catch (IOException e) {
                         throw new Error("Unable to dump stream: "+e); 
                     }
                 }
                 dumpBuffer(lines);
             }
         }.start();
     }
 
     private synchronized void dumpBuffer(List<String> lines) {
         for(String line: lines)
             writer.println(line);
         lines.clear();
     }
  
     private Set<String> experiments = Collections.synchronizedSet(new HashSet<String>());
     private Map<String,List<Float>> successes = new HashMap<String,List<Float>>();
     private Map<String,Integer> failures = new HashMap<String,Integer>();
     private Map<String,List<Integer>> packetCounts = new HashMap<String,List<Integer>>();
     private Map<String,Map<String,List<Float>>> events = new HashMap<String,Map<String,List<Float>>>();
 
     public void recordSuccess(String key, float value, int packetCount) {
         experiments.add(key);
         synchronized(successes) {
             List<Float> previous = successes.get(key);
             if(previous==null) {
                 previous = new ArrayList<Float>();
                 successes.put(key, previous);
             }
             previous.add(value);
         }
         recordPacketCount(key, packetCount);
     }
 
     private void recordPacketCount(String key, int packetCount) {
         synchronized(packetCounts) {
             List<Integer> previous = packetCounts.get(key);
             if(previous==null) {
                 previous = new ArrayList<Integer>();
                 packetCounts.put(key, previous);
             }
             previous.add(packetCount);
         }
     }
 
     public void recordFailure(String key, int packetCount) {
         experiments.add(key);
         synchronized(failures) {
             Integer previous = failures.get(key);
             if(previous==null) previous = 0;
             failures.put(key, previous+1);
         }
         recordPacketCount(key, packetCount);
     }
     
     public void recordEvent(String experiment, String name, float time) {
         synchronized(events) {
             Map<String,List<Float>> registry = events.get(experiment);
             if(registry==null) {
                 registry = new HashMap<String,List<Float>>();
                 events.put(experiment, registry);
             }
             List<Float> times = registry.get(name);
             if(times==null) {
                 times = new ArrayList<Float>();
                 registry.put(name, times);
             }
             times.add(time);
         }
     }
 
     public synchronized void reportRecord() {
         PrintWriter resultFile = null;
         try {
             resultFile = new PrintWriter(new BufferedWriter(new FileWriter("results.txt")));
         } catch (IOException e) {
             System.err.println("Warning: unable to create result file");
         }
         for(String experiment: experiments) {
             // Regular result
             List<Float> success = successes.get(experiment);
             List<Integer> counts = packetCounts.get(experiment);
             if(success==null) success = Collections.EMPTY_LIST;
             if(counts==null) counts = Collections.EMPTY_LIST;
             Integer failure = failures.get(experiment);
             if(failure==null) failure=0;
             int total = success.size()+failure;
             StringBuffer output = new StringBuffer();
             output.append(experiment+": nruns,success,Tavg,Tstddev,packets;");
             output.append(total+" ");
             output.append(((float)success.size())/((float)total)+" ");
             output.append(average(success)+" ");
             output.append(stddev(success)+" ");
             output.append(averageI(counts)+" ");
             // Event summary
             synchronized(events) {
                 Map<String,List<Float>> experimentEvents = events.get(experiment);
                 if(experimentEvents!=null) {
                     for(String name: experimentEvents.keySet())
                         output.append(reportEventHook(name,experimentEvents.get(name),total));
                 }
             }
             // Output
             if(resultFile!=null) resultFile.println(output);
             System.out.println(output);
         }
         if(resultFile!=null) resultFile.close();
         reportHook(experiments,successes,failures,experimentParameters);
         experimentParameters.clear();
     }
 
     protected String reportEventHook(String name, List<Float> set, int count) { return ""; }
 
     protected void reportHook(Set<String> experimentsNames,
             Map<String, List<Float>> successes,
             Map<String, Integer> failures,
             Map<String, ParameterHolder> experimentParameters) {
     }
 
     private static float averageI(List<Integer> counts) {
         int total = 0;
         for(float f: counts) total+=f;
         return total/(float)counts.size();
     }
 
     public static float average(List<Float> success) {
         float total = 0;
         for(float f: success) total+=f;
         return total/success.size();
     }
     
     public static float stddev(List<Float> numbers) {
         float avg = average(numbers);
         float sum = 0;
         for(float f: numbers) sum += (f-avg)*(f-avg);
         return (float) Math.sqrt(sum);
     }
 
 }
