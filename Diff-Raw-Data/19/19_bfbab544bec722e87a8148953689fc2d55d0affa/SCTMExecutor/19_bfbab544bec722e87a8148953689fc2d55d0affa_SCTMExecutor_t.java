 package hudson.plugins.sctmexecutor;
 
 import hudson.FilePath;
 import hudson.Launcher;
 import hudson.model.AbstractBuild;
 import hudson.model.AbstractProject;
 import hudson.model.BuildListener;
 import hudson.model.Hudson;
 import hudson.plugins.sctmexecutor.exceptions.SCTMException;
 import hudson.plugins.sctmexecutor.service.ISCTMService;
 import hudson.plugins.sctmexecutor.service.SCTMReRunProxy;
 import hudson.plugins.sctmexecutor.service.SCTMService;
 import hudson.tasks.Builder;
 
 import java.io.IOException;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Queue;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Future;
 import java.util.logging.Logger;
 
 import org.kohsuke.stapler.DataBoundConstructor;
 
 import com.borland.tm.webservices.tmexecution.ExecutionHandle;
 
 /**
  * Executes a specified execution definition on Borland's SilkCentral Test Manager.
  * 
  * @author Thomas Fuerer
  * 
  */
 public class SCTMExecutor extends Builder {
   public static final SCTMExecutorDescriptor DESCRIPTOR = new SCTMExecutorDescriptor();
   static final int OPT_NO_BUILD_NUMBER = 1;
   static final int OPT_USE_THIS_BUILD_NUMBER = 2;
   static final int OPT_USE_UPSTREAMJOB_BUILDNUMBER = 3;
   private static final Logger LOGGER = Logger.getLogger("hudson.plugins.sctmexecutor"); //$NON-NLS-1$
 
   private static int resultNoForLastBuild = 0;
 
   private final int projectId;
   private final String execDefIds;
   private final int delay;
   private final int buildNumberUsageOption;
   private final String upStreamJobName;
   private final boolean continueOnError;
   
  private Boolean succeed = Boolean.TRUE;
 
   @DataBoundConstructor
   public SCTMExecutor(int projectId, String execDefIds, int delay, int buildNumberUsageOption, String upStreamJobName, boolean contOnErr) {
     this.projectId = projectId;
     this.execDefIds = execDefIds;
     this.delay = delay;
     this.buildNumberUsageOption = buildNumberUsageOption;
     this.upStreamJobName = upStreamJobName;
     this.continueOnError = contOnErr;
   }
 
   @Override
   public SCTMExecutorDescriptor getDescriptor() {
     return DESCRIPTOR;
   }
 
   public String getExecDefIds() {
     return execDefIds;
   }
 
   public int getProjectId() {
     return projectId;
   }
   
   public int getDelay() {
     return delay;
   }
   
   public int getBuildNumberUsageOption() {
     return this.buildNumberUsageOption;
   }
   
   public String getUpStreamJobName() {
     return this.upStreamJobName;
   }
   
   public boolean getContinueOnError() {
     return this.continueOnError;
   }
   
   public String[] getUpStreamProjects() {
     Collection<String> jobNames = Hudson.getInstance().getJobNames(); // TODO filter real upstream projects
     
     return jobNames.toArray(new String[jobNames.size()]);
   }
 
   @Override
   public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
     ISCTMService service = null;
    succeed = Boolean.TRUE;
     String serviceURL = DESCRIPTOR.getServiceURL();
     try {
       service = new SCTMReRunProxy(new SCTMService(serviceURL, DESCRIPTOR.getUser(), DESCRIPTOR.getPassword()));
       listener.getLogger().println(Messages.getString("SCTMExecutor.log.successfulLogin")); //$NON-NLS-1$
     } catch (SCTMException e) {
       listener.error(e.getMessage());
      succeed = Boolean.FALSE;
     }
     int buildNumber = getBuildNumber(build, listener);
     Queue<ExecutionHandle> execHandles = startExecutions(listener, service, buildNumber);
     
     collectResults(build, listener, service, execHandles);
   
    return continueOnError || succeed;
   }
 
   private int getBuildNumber(AbstractBuild<?, ?> build, BuildListener listener) {
     if (OPT_USE_UPSTREAMJOB_BUILDNUMBER == buildNumberUsageOption)
       return getBuildNumberFromUpStreamProject(upStreamJobName, build.getUpstreamBuilds(), listener);
     else if (OPT_USE_THIS_BUILD_NUMBER == buildNumberUsageOption)
       return build.number;
     else
       return -1;
   }
 
   private int getBuildNumberFromUpStreamProject(String projectName, Map<AbstractProject, Integer> upstreamBuilds, BuildListener listener) {
     for (AbstractProject<?,?> project : upstreamBuilds.keySet()) {
       if (project.getName().equals(projectName))
         return upstreamBuilds.get(project);
     }
     listener.error(MessageFormat.format("The configured job {0} ist not found as Upstreamjob. Check your configuration!", projectName));
     return -1;
   }
 
   private void collectResults(AbstractBuild<?, ?> build, BuildListener listener, ISCTMService service, Queue<ExecutionHandle> execHandles) {
     try {
       FilePath rootDir = build.getProject().getWorkspace();
       if (rootDir == null) {
         LOGGER.severe("Cannot write the result file because slave is not connected.");
         listener.error(Messages.getString("SCTMExecutor.log.slaveNotConnected")); //$NON-NLS-1$
         throw new RuntimeException();
       }
       rootDir = createResultDir(rootDir, build.number);
       ExecutorService tp = DESCRIPTOR.getExecutorPool();
       List<Future<?>> results = new ArrayList<Future<?>>(execHandles.size());
       for (ExecutionHandle executionHandle : execHandles) {
         Runnable resultCollector = new ResultCollectorRunnable(service, listener.getLogger(), executionHandle, new StdXMLResultWriter(rootDir,
             DESCRIPTOR.getServiceURL()));
         results.add(tp.submit(resultCollector));
       }
 
       for (Future<?> res : results) {
         res.get();
       }
     } catch (Exception e) {
       LOGGER.severe(e.getMessage());
       listener.error("Cannot create directory for the testresults in the hudson workspace. Check permissions and diskspace.");
      succeed = Boolean.FALSE;
     }
   }
 
   private Queue<ExecutionHandle> startExecutions(BuildListener listener, ISCTMService service, int buildNumber) {
     Queue<ExecutionHandle> execHandles = new LinkedList<ExecutionHandle>();
     Collection<Integer> ids = csvToIntList(execDefIds);
     for (Integer execDefId : ids) {
       Collection<ExecutionHandle> result = null;
       try {
         if (OPT_NO_BUILD_NUMBER == buildNumberUsageOption || buildNumber == -1)
           result = service.start(execDefId);
         else
           result = service.start(execDefId, String.valueOf(buildNumber));
         if (result == null || result.size() <= 0) {
           listener.error(MessageFormat.format(Messages.getString("SCTMExecutor.err.execDefNotFound"), execDefId)); //$NON-NLS-1$
         } else {
           listener.getLogger().println(MessageFormat.format(Messages.getString("SCTMExecutor.log.successfulStartExecution"), execDefId));
           execHandles.addAll(result);
         }
         if (delay > 0 && ids.size() > 1)
           Thread.sleep(delay*1000);
       } catch (SCTMException e) {
         listener.error(e.getMessage());
        succeed = Boolean.FALSE;
       } catch (InterruptedException e) {
         LOGGER.severe(e.getMessage());
        succeed = Boolean.FALSE;
       }
     }
     return execHandles;
   }
 
   private static FilePath createResultDir(FilePath rootDir, int currentBuildNo) throws IOException, InterruptedException {
     rootDir = new FilePath(rootDir, "SCTMResults"); //$NON-NLS-1$
     if (resultNoForLastBuild != currentBuildNo) {
       if (rootDir.exists())
         rootDir.deleteRecursive();
       rootDir.mkdirs();
       resultNoForLastBuild = currentBuildNo;
     }
     return rootDir;
   }
 
   private List<Integer> csvToIntList(String execDefIds) {
     List<Integer> list = new LinkedList<Integer>();
     if (execDefIds.contains(",")) { //$NON-NLS-1$
       String[] ids = execDefIds.split(","); //$NON-NLS-1$
       for (String str : ids) {
         list.add(Integer.valueOf(str));
       }
     } else {
       list.add(Integer.valueOf(execDefIds));
     }
     return list;
   }
 }
