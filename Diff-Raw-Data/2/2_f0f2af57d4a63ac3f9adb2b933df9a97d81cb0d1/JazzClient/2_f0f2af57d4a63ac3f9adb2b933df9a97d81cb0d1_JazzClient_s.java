 package com.deluan.jenkins.plugins.rtc;
 
 import com.deluan.jenkins.plugins.rtc.changelog.JazzChangeSet;
 import com.deluan.jenkins.plugins.rtc.commands.*;
 import hudson.AbortException;
 import hudson.FilePath;
 import hudson.Launcher;
 import hudson.Launcher.ProcStarter;
 import hudson.model.Computer;
 import hudson.model.TaskListener;
 import hudson.util.ArgumentListBuilder;
 import hudson.util.ForkOutputStream;
 import org.kohsuke.stapler.framework.io.WriterOutputStream;
 
 import java.io.*;
 import java.util.*;
 import java.util.concurrent.TimeUnit;
 import java.util.logging.Logger;
 
 /**
  * Encapsulates the invocation of RTC's SCM Command Line Interface, "scm".
  *
  * @author Deluan Quintao
  */
 public class JazzClient implements JazzConfigurationProvider {
     private static final Logger logger = Logger.getLogger(JazzClient.class.getName());
 
     private static final String DAEMON_SCM_CMD = "scm";
 
     private static final int TIMEOUT = 60 * 5; // in seconds
 
 
     private final ArgumentListBuilder base;
     private final Launcher launcher;
     private final TaskListener listener;
     private String repositoryLocation;
     private String workspaceName;
     private String streamName;
     private String username;
     private String password;
     private FilePath jobWorkspace;
 
 
     public JazzClient(Launcher launcher, TaskListener listener, FilePath jobWorkspace, String jazzExecutable,
                       String user, String password, String repositoryLocation,
                       String streamName, String workspaceName) {
         base = new ArgumentListBuilder(jazzExecutable);
         this.launcher = launcher;
         this.listener = listener;
         this.username = user;
         this.password = password;
         this.repositoryLocation = repositoryLocation;
         this.streamName = streamName;
         this.workspaceName = workspaceName;
         this.jobWorkspace = jobWorkspace;
     }
 
     /**
      * Returns true if there is any incoming changes to be accepted.
      *
      * @return <tt>true</tt> if any changes are found
      * @throws IOException
      * @throws InterruptedException
      */
     public boolean hasChanges() throws IOException, InterruptedException {
         Map<String, JazzChangeSet> changes = compare();
 
         return !changes.isEmpty();
     }
 
     /**
      * Call <tt>scm load</tt> command. <p/>
      * <p/>
      * Will load the workspace using the parameters defined.
      *
      * @return <tt>true</tt> on success
      * @throws IOException
      * @throws InterruptedException
      */
     public boolean load() throws IOException, InterruptedException {
         Command cmd = new LoadCommand(this);
 
         return (joinWithPossibleTimeout(run(cmd.getArguments()), true, listener) == 0);
     }
 
     /**
      * Call <tt>scm daemon stop</tt> command. <p/>
      * <p/>
      * Will try to stop any daemon associated with the workspace.
      * <p/>
      * This will be executed with the <tt>scm</tt> command, as the <tt>lscm</tt> command
      * does not support this operation.
      *
      * @return <tt>true</tt> on success
      * @throws IOException
      * @throws InterruptedException
      */
     public boolean stopDaemon() throws IOException, InterruptedException {
         ArgumentListBuilder args = new ArgumentListBuilder(DAEMON_SCM_CMD);
 
        args.add(new StopDaemonCommand(this).getArguments());
 
         return (joinWithPossibleTimeout(l(args), true, listener) == 0);
     }
 
     /**
      * Call <tt>scm accept</tt> command.<p/>
      *
      * @return all changeSets accepted, complete with affected paths and related work itens
      * @throws IOException
      * @throws InterruptedException
      */
     public List<JazzChangeSet> accept() throws IOException, InterruptedException {
         Map<String, JazzChangeSet> compareCmdResults = compare();
 
         if (!compareCmdResults.isEmpty()) {
             Map<String, JazzChangeSet> listCmdResults = accept(compareCmdResults.keySet());
 
             for (Map.Entry<String, JazzChangeSet> entry : compareCmdResults.entrySet()) {
                 JazzChangeSet changeSet1 = entry.getValue();
                 JazzChangeSet changeSet2 = listCmdResults.get(entry.getKey());
                 changeSet1.copyItemsFrom(changeSet2);
             }
         }
 
         return new ArrayList<JazzChangeSet>(compareCmdResults.values());
     }
 
     private Map<String, JazzChangeSet> accept(Collection<String> changeSets) throws IOException, InterruptedException {
 
         AcceptCommand cmd = new AcceptCommand(this, changeSets);
 
         BufferedReader in = new BufferedReader(new InputStreamReader(
                 new ByteArrayInputStream(popen(cmd.getArguments()).toByteArray())));
         Map<String, JazzChangeSet> result = Collections.emptyMap();
 
         try {
             result = cmd.parse(in);
         } catch (Exception e) {
             throw new IOException(e);
         } finally {
             in.close();
         }
 
         return result;
     }
 
     private Map<String, JazzChangeSet> compare() throws IOException, InterruptedException {
         CompareCommand cmd = new CompareCommand(this);
 
         BufferedReader in = new BufferedReader(new InputStreamReader(
                 new ByteArrayInputStream(popen(cmd.getArguments()).toByteArray())));
         Map<String, JazzChangeSet> result = Collections.emptyMap();
 
         try {
             result = cmd.parse(in);
         } catch (Exception e) {
             throw new IOException(e);
         } finally {
             in.close();
         }
 
         return result;
     }
 
     private ArgumentListBuilder seed() {
         return base.clone();
     }
 
     private ProcStarter l(ArgumentListBuilder args) {
         // set the default stdout
         return launcher.launch().cmds(args).stdout(listener);
     }
 
     private ProcStarter run(ArgumentListBuilder args) {
         return l(seed().add(args.toCommandArray()));
     }
 
     private int joinWithPossibleTimeout(ProcStarter proc, boolean useTimeout, final TaskListener listener) throws IOException, InterruptedException {
         return useTimeout ? proc.start().joinWithTimeout(TIMEOUT, TimeUnit.SECONDS, listener) : proc.join();
     }
 
     /**
      * Runs the command and captures the output.
      */
     private ByteArrayOutputStream popen(ArgumentListBuilder args)
             throws IOException, InterruptedException {
 
         // scm produces text in the platform default encoding, so we need to convert it back to UTF-8
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         WriterOutputStream o = new WriterOutputStream(new OutputStreamWriter(baos, "UTF-8"),
                 Computer.currentComputer().getDefaultCharset());
 
         PrintStream output = listener.getLogger();
         ForkOutputStream fos = new ForkOutputStream(o, output);
         if (joinWithPossibleTimeout(run(args).stdout(fos), true, listener) == 0) {
             o.flush();
             return baos;
         } else {
             listener.error("Failed to run " + args.toStringWithQuote());
             throw new AbortException();
         }
     }
 
     public String getRepositoryLocation() {
         return repositoryLocation;
     }
 
     public String getWorkspaceName() {
         return workspaceName;
     }
 
     public String getStreamName() {
         return streamName;
     }
 
     public String getUsername() {
         return username;
     }
 
     public String getPassword() {
         return password;
     }
 
     public String getJobWorkspace() {
         return jobWorkspace.toString();
     }
 
 }
