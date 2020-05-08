 package org.h2o.eval.madface;
 
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.SortedSet;
 
 import org.h2o.util.exceptions.StartupException;
 
 import uk.ac.standrews.cs.nds.madface.Credentials;
 import uk.ac.standrews.cs.nds.madface.HostDescriptor;
 import uk.ac.standrews.cs.nds.madface.HostState;
 import uk.ac.standrews.cs.nds.madface.MadfaceManagerFactory;
 import uk.ac.standrews.cs.nds.madface.URL;
 import uk.ac.standrews.cs.nds.madface.interfaces.IApplicationManager;
 import uk.ac.standrews.cs.nds.madface.interfaces.IMadfaceManager;
 import uk.ac.standrews.cs.nds.util.Diagnostic;
 import uk.ac.standrews.cs.nds.util.DiagnosticLevel;
 import uk.ac.standrews.cs.nds.util.PrettyPrinter;
 
 public class MadfaceWorkerStarter {
 
     private static final String SPLIT_CHARACTER = " ";
     private final IMadfaceManager madface_manager;
 
     /*
      * Connection information.
      */
     private static final String SSH_USER = "root";
     private static final String SSH_PUBLIC_KEY_PATH = "/root/.ssh/id_dsa";
     private static final String SSH_PUBLIC_KEY_PASSWORD = "";
 
     public MadfaceWorkerStarter(final SortedSet<HostDescriptor> host_descriptors, final IApplicationManager workerManager, final String h2oJarName) throws Exception {
 
         madface_manager = MadfaceManagerFactory.makeMadfaceManager();
 
         madface_manager.setHostScanning(true);
 
         Diagnostic.traceNoEvent(DiagnosticLevel.FINAL, "Set host scanning to true.");
 
         madface_manager.configureApplication(workerManager);
 
         final Set<URL> application_urls = getH2OApplicationURLs(h2oJarName);
 
         madface_manager.configureApplication(application_urls);
 
         Diagnostic.traceNoEvent(DiagnosticLevel.FINAL, "Configured application.");
 
         for (final HostDescriptor new_node_descriptor : host_descriptors) {
 
             Diagnostic.traceNoEvent(DiagnosticLevel.FINAL, "Added host descriptor: " + new_node_descriptor.getHost());
 
             madface_manager.add(new_node_descriptor);
         }
 
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
 
         System.exit(0); // Kill off the process successfully. The current Madface version doesn't successfully shut down scanner threads, etc., on its own.
 
     }
 
     public Set<URL> getH2OApplicationURLs(final String h2oJarName) throws IOException {
 
         final Set<URL> application_urls = new HashSet<URL>();
 
         application_urls.add(new URL("http://www.cs.st-andrews.ac.uk/~angus/eval/" + h2oJarName));
 
         application_urls.add(new URL("http://www.cs.st-andrews.ac.uk/~angus/eval/json.jar"));
         application_urls.add(new URL("http://www.cs.st-andrews.ac.uk/~angus/eval/sigar.jar"));
         application_urls.add(new URL("http://www.cs.st-andrews.ac.uk/~angus/eval/mindterm.jar"));
         application_urls.add(new URL("http://www.cs.st-andrews.ac.uk/~angus/eval/log4j.jar"));
         application_urls.add(new URL("http://www.cs.st-andrews.ac.uk/~angus/eval/slf4j-api-1.5.0.jar"));
         application_urls.add(new URL("http://www.cs.st-andrews.ac.uk/~angus/eval/org.osgi.core-1.2.0.jar"));
         application_urls.add(new URL("http://www.cs.st-andrews.ac.uk/~angus/eval/servlet-api-2.4.jar"));
 
         application_urls.add(new URL("http://www.cs.st-andrews.ac.uk/~angus/eval/libsigar-amd64-linux.so"));
         application_urls.add(new URL("http://www.cs.st-andrews.ac.uk/~angus/eval/libsigar-universal-macosx.dylib"));
         application_urls.add(new URL("http://www.cs.st-andrews.ac.uk/~angus/eval/libsigar-x86-linux.so"));
         application_urls.add(new URL("http://www.cs.st-andrews.ac.uk/~angus/eval/libsigar-universal64-macosx.dylib"));
         application_urls.add(new URL("http://www.cs.st-andrews.ac.uk/~angus/eval/sigar-x86-winnt.dll"));
         application_urls.add(new URL("http://www.cs.st-andrews.ac.uk/~angus/eval/sigar-x86-winnt.lib"));
 
         return application_urls;
     }
 
     /**
      * 
      * @param args[0] Hostnames to start the network on.
      * @param args[1] Name of the h2o jar file to use. Several are available. Typically: h2o.jar or h2o-writedelay.jar.
      * @throws Exception
      */
     public static void main(final String[] args) throws Exception {
 
         Diagnostic.setLevel(DiagnosticLevel.FINAL);
 
         Diagnostic.traceNoEvent(DiagnosticLevel.FINAL, "Args Length: " + args.length);
         Diagnostic.traceNoEvent(DiagnosticLevel.FINAL, "List of args passed in: " + PrettyPrinter.toString(args));
         Diagnostic.traceNoEvent(DiagnosticLevel.FINAL, "List of machines passed in: " + PrettyPrinter.toString(args[0]));
         Diagnostic.traceNoEvent(DiagnosticLevel.FINAL, "H2O JAR: " + args[1]);
 
        if (args.length != 2) { throw new StartupException("To start an evaluation network you need to specify a list of colon separated hostnames (in quotes) and the name of the H2O jar file to use. Only " + args.length + " arguments were given."); }
 
         final String[] hostnames = parseHostnamesArray(args[0]);
 
         final String h2oJarName = args[1];
 
         Diagnostic.traceNoEvent(DiagnosticLevel.FULL, "Creating workers on hosts: " + PrettyPrinter.toString(hostnames));
 
         final Credentials cred = new Credentials(Credentials.constructJSONString(SSH_USER, SSH_PUBLIC_KEY_PATH, SSH_PUBLIC_KEY_PASSWORD));
 
         final SortedSet<HostDescriptor> node_descriptors = HostDescriptor.createDescriptorsUsingPublicKey(Arrays.asList(hostnames), cred);
 
         final IApplicationManager workerManager = new WorkerManager();
 
         new MadfaceWorkerStarter(node_descriptors, workerManager, h2oJarName); //returns when remote hosts have started.
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
 
         if (hostnames.endsWith("'") || hostnames.endsWith("\"")) {
             hostnames = hostnames.substring(0, hostnames.length() - 1);
         }
         return hostnames;
     }
 
 }
