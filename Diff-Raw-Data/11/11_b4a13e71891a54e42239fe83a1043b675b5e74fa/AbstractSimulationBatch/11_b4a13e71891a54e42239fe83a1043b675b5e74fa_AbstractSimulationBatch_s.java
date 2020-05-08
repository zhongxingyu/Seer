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
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import ussr.remote.facade.ActiveSimulation;
 import ussr.remote.facade.ParameterHolder;
 import ussr.remote.facade.RemotePhysicsSimulation;
 
 /**
  * Abstract class for running simulation batches
  * @author ups
  */
 public abstract class AbstractSimulationBatch implements ReturnValueHandler {
 
     public static final int SERVER_PORT = 54323;
     public static final int LINE_BUFFER_SIZE = 20;
     
    private Class<?> mainClass;
     private SimulationLauncherServer server;
     private PrintWriter writer;
     private List<Worker> passiveWorkers = new ArrayList<Worker>();
 
    public AbstractSimulationBatch(Class<?> mainClass) {
        this.mainClass = mainClass;
         // Start a simulation server (one that manages a number of running simulation processes)
         try {
             server = new SimulationLauncherServer(AbstractSimulationBatch.SERVER_PORT);
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
     
     private class Worker extends Thread {
         private ParameterHolder parameters;
         private int run;
         private boolean active = false;
         private Object signal = new Object();
         public void activate(ParameterHolder parameters, int run) {
             if(active) throw new Error("Active worker activated");
             this.parameters = parameters;
             this.run = run;
             this.active = true;
             synchronized(signal) {
                 signal.notifyAll();
             }
         }
         public void run() {
             while(true) {
                 synchronized(signal) {
                     try {
                         signal.wait();
                     } catch(InterruptedException exn) {
                         throw new Error("Unexpected interruption");
                     }
                 }
                 if(parameters==null) {
                     System.out.println("Worker thread terminated");
                     break;
                 }
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
                     simulation.start(mainClass, parameters, AbstractSimulationBatch.this);
                 } catch (RemoteException e) {
                     // Normal or abnormal termination, inspection of remote exception currently needed to determine...
                     System.out.println("#"+run+" Simulation stopped");
                 }
                 System.out.println("#"+run+" Simulation completed");
                 active = false;
                 synchronized(passiveWorkers) {
                     passiveWorkers.add(this);
                     passiveWorkers.notifyAll();
                 }
             }
         }
     }
     
     public void start(int max_parallel_sims) {
         createWorkers(max_parallel_sims);
         int run = 0; 
         while(runMoreSimulations()) {
             ParameterHolder parameters = getNextParameters();
             Worker worker = null;
             synchronized(passiveWorkers) {
                 while(worker==null) {
                     if(passiveWorkers.size()==0) {
                         try {
                             System.out.println("Waiting for available worker");
                             passiveWorkers.wait();
                         } catch (InterruptedException e) {
                             throw new Error("Unexpected interruption");
                         }
                         continue;
                     }
                     worker = passiveWorkers.remove(0);
                 }
             }
             System.out.println("Assigning task...");
             worker.activate(parameters, run++);
         }
         synchronized(passiveWorkers) {
             while(passiveWorkers.size()<max_parallel_sims) {
                 try {
                     passiveWorkers.wait();
                 } catch (InterruptedException e) {
                     throw new Error("Unexpected interruption");
                 }
             }
         }
         System.out.println("Batch completed");
         reportRecord();
         System.exit(0);
     }
 
     private void createWorkers(int max_parallel_sims) {
         System.out.println("Creating "+max_parallel_sims+" workers");
         for(int i=0; i<max_parallel_sims; i++) {
             Worker worker = new Worker();
             worker.start();
             passiveWorkers.add(worker);
         }
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
     public void recordSuccess(String key, float value) {
         experiments.add(key);
         synchronized(successes) {
             List<Float> previous = successes.get(key);
             if(previous==null) {
                 previous = new ArrayList<Float>();
                 successes.put(key, previous);
             }
             previous.add(value);
         }
     }
     public void recordFailure(String key) {
         experiments.add(key);
         synchronized(failures) {
             Integer previous = failures.get(key);
             if(previous==null) previous = 0;
             failures.put(key, previous+1);
         }
     }
     public void reportRecord() {
         PrintWriter resultFile = null;
         try {
             resultFile = new PrintWriter(new BufferedWriter(new FileWriter("results.txt")));
         } catch (IOException e) {
             System.err.println("Warning: unable to create result file");
         }
         for(String experiment: experiments) {
             List<Float> success = successes.get(experiment);
             if(success==null) success = Collections.EMPTY_LIST;
             Integer failure = failures.get(experiment);
             if(failure==null) failure=0;
             int total = success.size()+failure;
             StringBuffer output = new StringBuffer();
             output.append(experiment+": nruns,success,Tavg,Tstddev;");
             output.append(total+" ");
             output.append(((float)success.size())/((float)total)+" ");
             output.append(average(success)+" ");
             output.append(stddev(success)+" ");
             if(resultFile!=null) resultFile.println(output);
             System.out.println(output);
         }
         if(resultFile!=null) resultFile.close();
     }
 
     private float average(List<Float> success) {
         float total = 0;
         for(float f: success) total+=f;
         return total/success.size();
     }
     
     private float stddev(List<Float> numbers) {
         float avg = average(numbers);
         float sum = 0;
         for(float f: numbers) sum += (f-avg)*(f-avg);
         return (float) Math.sqrt(sum);
     }
 }
