 package ussr.remote;
 
 import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
import java.io.PrintWriter;
 import java.rmi.RemoteException;
 
 import ussr.remote.facade.ActiveSimulation;
 import ussr.remote.facade.ParameterHolder;
 import ussr.remote.facade.RemotePhysicsSimulation;
 
 /**
  * Abstract class for running simulation batches
  * @author ups
  */
 public abstract class AbstractSimulationBatch implements ReturnValueHandler {
 
     public static final int SERVER_PORT = 54323;
     private Class<?> mainClass;
     private SimulationLauncherServer server;
    private PrintWriter writer;
 
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
     
     public void start() {
         int run = 0; 
         while(runMoreSimulations()) {
             ParameterHolder parameters = getNextParameters();
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
                 System.out.println("Waiting for simulation");
                 simulation.waitForReady();
             }
             // Start a simulation in the remote process
             try {
                 simulation.start(mainClass, parameters, this);
             } catch (RemoteException e) {
                 // Normal or abnormal termination, inspection of remote exception currently needed to determine...
                 System.err.println("Simulation stopped");
             }
             System.out.println("Simulation completed");
         }
         System.out.println("Batch completed");
         System.exit(0);
     }
 
     /**
      * Dump an input stream to standard out, prefixing all lines with a fixed text 
      * @param prefix the prefix to use
      * @param stream the stream to dump
      */
    private void dumpStream(final String prefix, final InputStream stream) {
         new Thread() {
             public void run() {
                 BufferedReader input = new BufferedReader(new InputStreamReader(stream));
                 while(true) {
                     String line;
                     try {
                         line = input.readLine();
                         if(line==null) break;
                        writer.println(prefix+": "+line);
                     } catch (IOException e) {
                         throw new Error("Unable to dump stream: "+e); 
                     }
                 }
             }
         }.start();
     }
 
     
 }
