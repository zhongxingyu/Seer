 package org.h2o.eval.madface;
 
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.SortedSet;
 
 import org.h2o.util.exceptions.StartupException;
 
 import uk.ac.standrews.cs.nds.madface.HostDescriptor;
 import uk.ac.standrews.cs.nds.madface.HostState;
 import uk.ac.standrews.cs.nds.madface.MadfaceManagerFactory;
 import uk.ac.standrews.cs.nds.madface.URL;
 import uk.ac.standrews.cs.nds.madface.interfaces.IApplicationManager;
 import uk.ac.standrews.cs.nds.madface.interfaces.IMadfaceManager;
 import uk.ac.standrews.cs.nds.util.Diagnostic;
 import uk.ac.standrews.cs.nds.util.DiagnosticLevel;
 import uk.ac.standrews.cs.nds.util.PrettyPrinter;
 
 public class EvaluationNetwork {
 
     private static final String SPLIT_CHARACTER = " ";
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
 
         final Set<URL> application_urls = getH2OApplicationURLs();
 
         madface_manager.configureApplication(application_urls);
 
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
 
     public Set<URL> getH2OApplicationURLs() throws IOException {
 
         final Set<URL> application_urls = new HashSet<URL>();
 
         application_urls.add(new URL("http://www-systems.cs.st-andrews.ac.uk:8080/hudson/job/h2o/lastSuccessfulBuild/artifact/bin/h2o.jar"));
 
         application_urls.add(new URL("http://www-systems.cs.st-andrews.ac.uk:8080/hudson/job/h2o/lastSuccessfulBuild/artifact/lib/json.jar"));
         application_urls.add(new URL("http://www-systems.cs.st-andrews.ac.uk:8080/hudson/job/h2o/lastSuccessfulBuild/artifact/lib/sigar.jar"));
         application_urls.add(new URL("http://www-systems.cs.st-andrews.ac.uk:8080/hudson/job/h2o/lastSuccessfulBuild/artifact/lib/mindterm.jar"));
         application_urls.add(new URL("http://www-systems.cs.st-andrews.ac.uk:8080/hudson/job/h2o/lastSuccessfulBuild/artifact/lib/log4j.jar"));
         application_urls.add(new URL("http://www-systems.cs.st-andrews.ac.uk:8080/hudson/job/h2o/lastSuccessfulBuild/artifact/lib/slf4j-api-1.5.0.jar"));
         application_urls.add(new URL("http://www-systems.cs.st-andrews.ac.uk:8080/hudson/job/h2o/lastSuccessfulBuild/artifact/lib/org.osgi.core-1.2.0.jar"));
         application_urls.add(new URL("http://www-systems.cs.st-andrews.ac.uk:8080/hudson/job/h2o/lastSuccessfulBuild/artifact/lib/servlet-api-2.4.jar"));
 
         application_urls.add(new URL("http://www-systems.cs.st-andrews.ac.uk:8080/hudson/job/h2o/lastSuccessfulBuild/artifact/lib/libsigar-amd64-linux.so"));
         application_urls.add(new URL("http://www-systems.cs.st-andrews.ac.uk:8080/hudson/job/h2o/lastSuccessfulBuild/artifact/lib/libsigar-universal-macosx.dylib"));
         application_urls.add(new URL("http://www-systems.cs.st-andrews.ac.uk:8080/hudson/job/h2o/lastSuccessfulBuild/artifact/lib/libsigar-x86-linux.so"));
         application_urls.add(new URL("http://www-systems.cs.st-andrews.ac.uk:8080/hudson/job/h2o/lastSuccessfulBuild/artifact/lib/libsigar-universal64-macosx.dylib"));
         application_urls.add(new URL("http://www-systems.cs.st-andrews.ac.uk:8080/hudson/job/h2o/lastSuccessfulBuild/artifact/lib/sigar-x86-winnt.dll"));
         application_urls.add(new URL("http://www-systems.cs.st-andrews.ac.uk:8080/hudson/job/h2o/lastSuccessfulBuild/artifact/lib/sigar-x86-winnt.lib"));
 
         return application_urls;
     }
 
     /**
      * 
      * @param args Hostnames to start the network on.
      * @throws Exception
      */
     public static void main(final String[] args) throws Exception {
 
         Diagnostic.setLevel(DiagnosticLevel.FULL);
 
         Diagnostic.traceNoEvent(DiagnosticLevel.FINAL, "List of machines passed in: " + PrettyPrinter.toString(args));
 
         if (args.length != 1) { throw new StartupException("No hostnames were passed to the program, so it cannot startup an evaluation network."); }
 
         final String[] hostnames = parseHostnamesArray(args[0]);
 
         final SortedSet<HostDescriptor> node_descriptors = HostDescriptor.createDescriptorsUsingPassword(Arrays.asList(hostnames), true);
 
         final IApplicationManager workerManager = new WorkerManager();
 
         new EvaluationNetwork(node_descriptors, workerManager); //returns when remote hosts have started.
     }
 
     private static String[] parseHostnamesArray(String hostnames) throws StartupException {
 
         hostnames = removeSurroundingBrackets(hostnames);
 
         if (hostnames != null) {
             return hostnames.split(SPLIT_CHARACTER);
         }
         else {
             throw new StartupException("No hostnames were passed to the program, so it cannot startup an evaluation network.");
         }
     }
 
     public static String removeSurroundingBrackets(String hostnames) {
 
         if (hostnames.startsWith("'") || hostnames.startsWith("\"")) {
             hostnames = hostnames.substring(1);
         }
 
        if (hostnames.endsWith("'") || hostnames.startsWith("\"")) {
             hostnames = hostnames.substring(0, hostnames.length() - 1);
         }
         return hostnames;
     }
 
 }
