 package org.h2o.eval.madface;
 
 import java.util.SortedSet;
 import java.util.concurrent.ConcurrentSkipListSet;
 
 import org.h2o.util.exceptions.StartupException;
 
 import uk.ac.standrews.cs.nds.madface.HostDescriptor;
 import uk.ac.standrews.cs.nds.madface.HostState;
 import uk.ac.standrews.cs.nds.madface.MadfaceManagerFactory;
 import uk.ac.standrews.cs.nds.madface.interfaces.IApplicationManager;
 import uk.ac.standrews.cs.nds.madface.interfaces.IMadfaceManager;
 import uk.ac.standrews.cs.nds.util.Diagnostic;
 import uk.ac.standrews.cs.nds.util.DiagnosticLevel;
 import uk.ac.standrews.cs.nds.util.PrettyPrinter;
 
 public class EvaluationNetwork {
 
     private final IMadfaceManager madface_manager;
 
     public EvaluationNetwork(final SortedSet<HostDescriptor> host_descriptors, final IApplicationManager workerManager) throws Exception {
 
         madface_manager = MadfaceManagerFactory.makeMadfaceManager();
 
         madface_manager.setHostScanning(true); //XXX does this need to be enabled?
 
         Diagnostic.traceNoEvent(DiagnosticLevel.FINAL, "Set host scanning to true.");
 
         madface_manager.configureApplication(workerManager);
 
         Diagnostic.traceNoEvent(DiagnosticLevel.FINAL, "Configured application.");
 
         for (final HostDescriptor new_node_descriptor : host_descriptors) {
 
             Diagnostic.traceNoEvent(DiagnosticLevel.FINAL, "Added host descriptor: " + new_node_descriptor.getHost());
 
             madface_manager.add(new_node_descriptor);
         }
 
         Diagnostic.traceNoEvent(DiagnosticLevel.FINAL, "Sent kill command to all nodes.");
 
         madface_manager.killAll(true); //blocks until it thinks it's killed everything.
 
         Diagnostic.traceNoEvent(DiagnosticLevel.FINAL, "Kill command executed on all nodes");
 
         madface_manager.deployAll();
 
         Diagnostic.traceNoEvent(DiagnosticLevel.FINAL, "System deployed successfully on all nodes.");
 
         Diagnostic.traceNoEvent(DiagnosticLevel.FINAL, "Waiting for all nodes to start up.");
 
         madface_manager.waitForAllToReachState(HostState.RUNNING);
 
         Diagnostic.traceNoEvent(DiagnosticLevel.FINAL, "All nodes have started.");
 
         madface_manager.setHostScanning(false);
 
         Diagnostic.traceNoEvent(DiagnosticLevel.FINAL, "Host scanning disabled, Evaluation Network complete.");
 
         madface_manager.shutdown();
 
         Diagnostic.traceNoEvent(DiagnosticLevel.FINAL, "Madface manager has been successfully shut down.");
 
     }
 
     /**
      * 
      * @param args Hostnames to start the network on.
      * @throws Exception
      */
     public static void main(final String[] args) throws Exception {
 
         Diagnostic.setLevel(DiagnosticLevel.FULL);
 
         Diagnostic.traceNoEvent(DiagnosticLevel.FINAL, "List of machines passed in: " + PrettyPrinter.toString(args));
 
        if (args.length != 0) { throw new StartupException("No hostnames were passed to the program, so it cannot startup an evaluation network."); }
 
         final String[] hostnames = parseHostnamesArray(args[0]);
 
         final SortedSet<HostDescriptor> node_descriptors = new ConcurrentSkipListSet<HostDescriptor>();
 
         for (final String hostname : hostnames) {
             final HostDescriptor hostDescriptor = new HostDescriptor(hostname);
             node_descriptors.add(hostDescriptor);
         }
 
         final IApplicationManager workerManager = new WorkerManager();
 
         new EvaluationNetwork(node_descriptors, workerManager); //returns when remote hosts have started.
     }
 
     private static String[] parseHostnamesArray(final String hostnames) throws StartupException {
 
         if (hostnames != null) {
             return hostnames.split(" ");
         }
         else {
             throw new StartupException("No hostnames were passed to the program, so it cannot startup an evaluation network.");
         }
     }
 
 }
