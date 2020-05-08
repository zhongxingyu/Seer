 package hudson.plugins.sctmexecutor;
 
 import hudson.FilePath;
 import hudson.Launcher;
 import hudson.model.AbstractBuild;
 import hudson.model.BuildListener;
 import hudson.model.Descriptor;
 import hudson.model.Hudson;
 import hudson.plugins.sctmexecutor.exceptions.EncryptionException;
 import hudson.tasks.Builder;
 
 import java.io.IOException;
 import java.net.URL;
 import java.rmi.RemoteException;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.xml.rpc.ServiceException;
 
 import org.kohsuke.stapler.DataBoundConstructor;
 
 import com.borland.scc.sccsystem.SystemService;
 import com.borland.scc.sccsystem.SystemServiceServiceLocator;
 import com.borland.tm.webservices.tmexecution.ExecutionHandle;
 import com.borland.tm.webservices.tmexecution.ExecutionWebService;
 import com.borland.tm.webservices.tmexecution.ExecutionWebServiceServiceLocator;
 
 /**
  * Executes a specified execution definition on Borland's SilkCentral Test Manager.
  * 
  * @author Thomas Fuerer
  * 
  */
 public class SCTMExecutor extends Builder {
   public static final SCTMExecutorDescriptor DESCRIPTOR = new SCTMExecutorDescriptor();
   private static final Logger LOGGER = Logger.getLogger("hudson.plumgins.sctmexecutor");  //$NON-NLS-1$
 
   private static int resultNoForLastBuild = 0;
 
   private final int projectId;
   private final String execDefIds;
 
   @DataBoundConstructor
   public SCTMExecutor(int projectId, String execDefIds) {
     this.projectId = projectId;
     this.execDefIds = execDefIds;
   }
 
   public Descriptor<Builder> getDescriptor() {
     return DESCRIPTOR;
   }
 
   public String getExecDefIds() {
     return execDefIds;
   }
 
   public int getProjectId() {
     return projectId;
   }
 
   @Override
   public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
       throws InterruptedException, IOException {
     String serviceURL = DESCRIPTOR.getServiceURL();
     SystemService systemService;
     ExecutionWebService execService;
     try {
       systemService = new SystemServiceServiceLocator().getsccsystem(new URL(serviceURL + "/sccsystem?wsdl")); //$NON-NLS-1$
       execService = new ExecutionWebServiceServiceLocator().gettmexecution(new URL(serviceURL + "/tmexecution?wsdl")); //$NON-NLS-1$
 
       ISessionHandler sessionHandler = new SessionHandler(systemService, SCTMExecutor.DESCRIPTOR.getUser(), SCTMExecutor.DESCRIPTOR.getPassword());
       
       long sessionId = sessionHandler.getSessionId(-1);
       listener.getLogger().println(""); //$NON-NLS-1$
       execService.setCurrentProject(sessionId, projectId);
       List<ExecutionHandle> execHandles;
       try {
         execHandles = startExecutions(listener, execService, sessionId);
       } catch (IllegalArgumentException e) {
         return false;
       }
 
       collectResults(build, listener, execService, sessionHandler, execHandles);
       
       return true;
     } catch (ServiceException e) {
      LOGGER.log(Level.SEVERE, MessageFormat.format("The URL {0} cannot be accessed or no service has been found.", serviceURL)); //$NON-NLS-1$
       listener.error(MessageFormat.format("", serviceURL)); //$NON-NLS-1$
       return false;
     } catch (RemoteException e) {
       LOGGER.log(Level.SEVERE, e.getMessage());
       listener.error(""); //$NON-NLS-1$
       return false;
     } catch (EncryptionException e){
       return false;
     } catch (Exception e) {
       Hudson.getInstance().servletContext.log("", e); //$NON-NLS-1$
       listener.error(e.getLocalizedMessage());
       return false;
     }
   }
 
   private void collectResults(AbstractBuild<?, ?> build, BuildListener listener, ExecutionWebService execService,
       ISessionHandler sessionHandler, List<ExecutionHandle> execHandles) throws IOException, InterruptedException {
     FilePath rootDir = build.getProject().getWorkspace();
     if (rootDir == null) {
      LOGGER.log(Level.SEVERE, "Cannot write the result file because slave is not connected."); //$NON-NLS-1$
       listener.error(""); //$NON-NLS-1$
     }
     
     rootDir = createResultDir(rootDir, build.number);
     List<ResultCollectorThread> collectorThreads = new ArrayList<ResultCollectorThread>(execHandles.size());
     
     for (ExecutionHandle executionHandle : execHandles) {
       // TODO: use ThreadPool
       ResultCollectorThread resultCollector = new ResultCollectorThread(listener.getLogger(), execService, sessionHandler, executionHandle, new StdXMLResultWriter(rootDir, DESCRIPTOR.getServiceURL()));
       resultCollector.start();
       collectorThreads.add(resultCollector);
     }
     
     for (ResultCollectorThread resultCollector : collectorThreads) {
       resultCollector.join(); // maybe it is better to work with a timeout here        
     }
   }
 
   private List<ExecutionHandle> startExecutions(BuildListener listener, ExecutionWebService execService, long sessionId)
       throws RemoteException {
     List<ExecutionHandle> execHandles = new ArrayList<ExecutionHandle>();
     for (Integer execDefId : csvToIntList(execDefIds)) {
       ExecutionHandle[] execHandleArr = execService.startExecution(sessionId, execDefId);
       if (execHandleArr.length <= 0 || execHandleArr[0] == null
           || (execHandleArr[0] != null && execHandleArr[0].getTimeStamp() <= 0)) {
         listener.error(MessageFormat.format("", execDefId)); //$NON-NLS-1$
         throw new IllegalArgumentException();
       } else {
         listener.getLogger().println(MessageFormat.format("", execDefId)); //$NON-NLS-1$
         for (ExecutionHandle executionHandle : execHandleArr) {
           execHandles.add(executionHandle);
         }
       }
     }
     return execHandles;
   }
 
   private static FilePath createResultDir(FilePath rootDir, int currentBuildNo) throws IOException, InterruptedException {
     rootDir = new FilePath(rootDir, "SCTMResults"); //$NON-NLS-1$
     if (resultNoForLastBuild  != currentBuildNo) {
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
