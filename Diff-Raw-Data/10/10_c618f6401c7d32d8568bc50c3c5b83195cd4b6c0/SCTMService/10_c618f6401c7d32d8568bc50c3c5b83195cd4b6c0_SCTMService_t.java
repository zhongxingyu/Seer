 package hudson.plugins.sctmexecutor.service;
 
 import hudson.plugins.sctmexecutor.Messages;
 import hudson.plugins.sctmexecutor.exceptions.SCTMException;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.rmi.RemoteException;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.xml.rpc.ServiceException;
 
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.HttpException;
 import org.apache.commons.httpclient.HttpMethod;
 import org.apache.commons.httpclient.methods.GetMethod;
 
 import com.borland.sctm.ws.administration.MainEntities;
 import com.borland.sctm.ws.administration.MainEntitiesServiceLocator;
 import com.borland.sctm.ws.execution.ExecutionWebService;
 import com.borland.sctm.ws.execution.ExecutionWebServiceServiceLocator;
 import com.borland.sctm.ws.execution.entities.ExecutionHandle;
 import com.borland.sctm.ws.execution.entities.ExecutionNode;
 import com.borland.sctm.ws.execution.entities.ExecutionResult;
 import com.borland.sctm.ws.execution.entities.PropertyValue;
 import com.borland.sctm.ws.logon.SystemService;
 import com.borland.sctm.ws.logon.SystemServiceServiceLocator;
 import com.borland.sctm.ws.performer.PerformerService;
 import com.borland.sctm.ws.performer.PerformerServiceServiceLocator;
 import com.borland.sctm.ws.performer.SPNamedEntity;
 import com.borland.sctm.ws.planning.PlanningService;
 import com.borland.sctm.ws.planning.PlanningServiceServiceLocator;
 
 public class SCTMService implements ISCTMService {
   private static final int MAX_LOGONRETRYCOUNT = 3;
   private static final Logger LOGGER = Logger.getLogger("hudson.plugins.sctmservice");  //$NON-NLS-1$
   
   private SystemService systemService;
   private ExecutionWebService execService;
   private MainEntities adminService;
   private PlanningService planningService;
   private PerformerService performerService;
   private long sessionId;
   private volatile int logonRetryCount;
   private String user;
   private String pwd;
   private int projectId;
   private String serviceExchangeURL;
 
   public SCTMService(String serviceURL, String user, String pwd, int projectId) throws SCTMException {
     try {
       this.user = user;
       this.pwd = pwd;
       this.projectId = projectId;
       
       systemService = new SystemServiceServiceLocator().getsccsystem(new URL(serviceURL + "/sccsystem?wsdl")); //$NON-NLS-1$
       execService = new ExecutionWebServiceServiceLocator().gettmexecution(new URL(serviceURL + "/tmexecution?wsdl")); //$NON-NLS-1$
       adminService = new MainEntitiesServiceLocator().getsccentities(new URL(serviceURL+"/sccentities?wsdl")); //$NON-NLS-1$
       planningService = new PlanningServiceServiceLocator().gettmplanning(new URL(serviceURL+"/tmplanning?wsdl")); //$NON-NLS-1$
       performerService = new PerformerServiceServiceLocator().gettmperformer(new URL(serviceURL+"/tmperformer?wsdl")); //$NON-NLS-1$
       serviceExchangeURL = String.format("%sExchange?hid=%s", serviceURL, "SilkPerformer"); //$NON-NLS-1$ //$NON-NLS-2$
       
       logon();
     } catch (MalformedURLException e) {
       LOGGER.log(Level.SEVERE, e.getMessage(), e);
       throw new SCTMException(Messages.getString("SCTMService.err.serviceUrlWrong")); //$NON-NLS-1$
     } catch (ServiceException e) {
       LOGGER.log(Level.SEVERE, e.getMessage(), e);
       throw new SCTMException(MessageFormat.format(Messages.getString("SCTMService.err.urlOrServiceBroken"), serviceURL)); //$NON-NLS-1$
     } catch (RemoteException e) {
       LOGGER.log(Level.SEVERE, e.getMessage(), e);
       throw new SCTMException(MessageFormat.format(Messages.getString("SCTMService.err.commonFatalError"), e.getMessage())); //$NON-NLS-1$
     }
   }
 
   private void logon() throws RemoteException {
     this.sessionId = this.systemService.logonUser(this.user, this.pwd);;
     execService.setCurrentProject(sessionId, projectId);
     planningService.setCurrentProject(sessionId, String.valueOf(projectId));
   }
 
   private boolean handleLostSessionException(RemoteException e) throws SCTMException {
     if (lostSessionExceptionThrown(e) && logonRetryCount < MAX_LOGONRETRYCOUNT) { //$NON-NLS-1$
       logonRetryCount++;
       LOGGER.warning(Messages.getString("SCTMService.warn.SessionLostReconnect")); //$NON-NLS-1$
       try {
         logon();
         return true;
       } catch (RemoteException e1) {
         LOGGER.log(Level.SEVERE, e.getMessage(), e);
         if (e.getMessage().contains("Not logged in")) //$NON-NLS-1$
           throw new SCTMException(Messages.getString("SCTMService.err.accessDenied")); //$NON-NLS-1$
         else
           throw new SCTMException(MessageFormat.format(Messages.getString("SCTMService.err.commonFatalError"), e.getMessage())); //$NON-NLS-1$
       }
     }
     return false;
   }
 
   private boolean lostSessionExceptionThrown(RemoteException e) {
     String message = e.getMessage();
     return message.contains("Not logged in.") || message.contains("Connection timed out") || //$NON-NLS-1$ //$NON-NLS-2$
            (message.contains("InvalidIdException: sid") && message.contains("is invalid or expired")) || // InvalidIdException is not exposed as class in the wsdl //$NON-NLS-1$ //$NON-NLS-2$
            (message.contains("InvalidSidException: sid") && message.contains("is invalid or expired")); // InvalidSidException is not exposed as class in the wsdl //$NON-NLS-1$ //$NON-NLS-2$
   }
 
   private Collection<ExecutionHandle> convertToList(ExecutionHandle[] handles) {
     Collection<ExecutionHandle> runs = new ArrayList<ExecutionHandle>(handles.length);
     for (ExecutionHandle handle : handles) {
       runs.add(handle);
     }
     return runs;
   }
 
   private Collection<String> toList(String[] versions) {
     Collection<String> c = new ArrayList<String>(versions.length);
     for (String version : versions) {
       c.add(version);
     }
     return c;
   }
 
   private String getExecutionNodePropertyValue(ExecutionNode node, String propertyName) {
     PropertyValue[] propertyValues = node.getPropertyValues();
     for (PropertyValue propertyValue : propertyValues) {
       if (propertyName.equals(propertyValue.getName()))
         return propertyValue.getValue();
     }
     return null;
   }
 
   private ExecutionNode getExecDefNode(int nodeId) throws RemoteException, SCTMException {
     ExecutionNode node = execService.getNode(sessionId, nodeId);
     if (node == null)
       throw new SCTMException(MessageFormat.format(Messages.getString("SCTMService.err.missExecDef"), nodeId)); //$NON-NLS-1$
     return node;
   }
 
   private String getProductName(ExecutionNode node) throws RemoteException {
     String testContainerId = getExecutionNodePropertyValue(node, "PROP_TESTCONTAINER"); //$NON-NLS-1$
     String productId = planningService.getProperty(sessionId, testContainerId, "_node_properties_ProductID_pk_fk").getValue(); //$NON-NLS-1$
     String productName = adminService.getProductNameById(sessionId, Integer.valueOf(productId));
     return productName;
   }
 
   /* (non-Javadoc)
    * @see hudson.plugins.sctmexecutor.service.ISCTMService#start(int)
    */
   @Override
   public Collection<ExecutionHandle> start(int executionId) throws SCTMException {
     try {
       ExecutionHandle[] handles = execService.startExecution(this.sessionId, executionId);
       logonRetryCount = 0;
       return convertToList(handles);
     } catch (RemoteException e) {
       if (handleLostSessionException(e))
         return start(executionId);
       LOGGER.log(Level.WARNING, e.getMessage(), e);
       throw new SCTMException(MessageFormat.format(Messages.getString("SCTMService.err.commonFatalError"), e.getMessage())); //$NON-NLS-1$
     }
   }
 
   /* (non-Javadoc)
    * @see hudson.plugins.sctmexecutor.service.ISCTMService#start(int, java.lang.String)
    */
   @Override
   public Collection<ExecutionHandle> start(int executionId, String buildNumber) throws SCTMException {
     try {
       ExecutionHandle[] handles = execService.startExecution(this.sessionId, executionId, buildNumber, 1, null);
       return convertToList(handles);
     } catch (RemoteException e) {
       if (handleLostSessionException(e)) {
         return start(executionId, buildNumber);
       }
       LOGGER.log(Level.WARNING, e.getMessage(), e);
       throw new SCTMException(MessageFormat.format(Messages.getString("SCTMService.err.commonFatalError"), e.getMessage())); //$NON-NLS-1$
     }
   }
 
   /* (non-Javadoc)
    * @see hudson.plugins.sctmexecutor.service.ISCTMService#isFinished(com.borland.tm.webservices.tmexecution.ExecutionHandle)
    */
   @Override
   public boolean isFinished(ExecutionHandle handle) throws SCTMException {
     try {
       return execService.getStateOfExecution(sessionId, handle) < 0;
     } catch (RemoteException e) {
       if (handleLostSessionException(e))
         return isFinished(handle);
       LOGGER.log(Level.SEVERE, e.getMessage(), e);
       throw new SCTMException(MessageFormat.format(Messages.getString("SCTMService.err.commonFatalError"), e.getMessage())); //$NON-NLS-1$
     }
   }
 
   /* (non-Javadoc)
    * @see hudson.plugins.sctmexecutor.service.ISCTMService#getExecutionResult(com.borland.tm.webservices.tmexecution.ExecutionHandle)
    */
   @Override
   public ExecutionResult getExecutionResult(ExecutionHandle handle) throws SCTMException {
     try {
       return execService.getExecutionResult(this.sessionId, handle);
     } catch (RemoteException e) {
       if (handleLostSessionException(e))
         return getExecutionResult(handle);
       LOGGER.log(Level.SEVERE, e.getMessage(), e);
       throw new SCTMException(MessageFormat.format(Messages.getString("SCTMService.err.commonFatalError"), e.getMessage())); //$NON-NLS-1$
     }
   }
 
   @Override
   public boolean addBuildNumber(String productName, String version, int buildNumber) throws SCTMException {
     try {
       return adminService.addBuild(sessionId, productName, version, String.valueOf(buildNumber), Messages.getString("SCTMService.msg.sctm.buildnumberComment"), true); //$NON-NLS-1$
     } catch (RemoteException e) {
       if (handleLostSessionException(e))
         addBuildNumber(productName, version, buildNumber);
       LOGGER.log(Level.SEVERE, e.getMessage(), e);
       throw new SCTMException(MessageFormat.format(Messages.getString("SCTMService.err.commonFatalError"), e.getMessage())); //$NON-NLS-1$
     }
   }
 
   @Override
   public boolean buildNumberExists(String productName, String version, int buildNumber) throws SCTMException {
     try {
       String[] builds = adminService.getBuilds(sessionId, productName, version);
      if (builds != null) {
        String value = String.valueOf(buildNumber);
        for (String build : builds) {
          if (value.equals(build))
            return true;
        }
       }
       return false;
     } catch (RemoteException e) {
       if (handleLostSessionException(e))
         return buildNumberExists(productName, version, buildNumber);
       LOGGER.log(Level.SEVERE, e.getMessage(), e);
       throw new SCTMException(MessageFormat.format(Messages.getString("SCTMService.err.commonFatalError"), e.getMessage())); //$NON-NLS-1$
     }
   }
 
   @Override
   public int getLatestSCTMBuildnumber(String productName, String version) throws SCTMException {
     try {
       String[] builds = adminService.getBuilds(sessionId, productName, version);
       int latestBuildnumber = -1;
       for (String bn : builds) {
         int buildnumber = 0;
         try {
           buildnumber = Integer.parseInt(bn);
           if (buildnumber > latestBuildnumber)
             latestBuildnumber = buildnumber;          
         } catch (NumberFormatException e) {
           LOGGER.warning(MessageFormat.format(Messages.getString("SCTMService.err.buildnumberConvertion"), buildnumber)); //$NON-NLS-1$
         }
       }
       return latestBuildnumber;
     } catch (RemoteException e) {
       if (handleLostSessionException(e))
         return getLatestSCTMBuildnumber(productName, version);
       LOGGER.log(Level.SEVERE, e.getMessage(), e);
       throw new SCTMException(MessageFormat.format(Messages.getString("SCTMService.err.commonFatalError"), e.getMessage())); //$NON-NLS-1$
     }
   }
 
   @Override
   public String getExecDefinitionName(int execDefId) throws SCTMException {
     try {
       ExecutionNode node = getExecDefNode(execDefId);
       return getExecutionNodePropertyValue(node, "PROP_NAME");   //$NON-NLS-1$
     } catch (RemoteException e) {
       if (handleLostSessionException(e))
         return getExecDefinitionName(execDefId);
       LOGGER.log(Level.SEVERE, e.getMessage(), e);
       throw new SCTMException(MessageFormat.format(Messages.getString("SCTMService.err.commonFatalError"), e.getMessage())); //$NON-NLS-1$
     }
   }
 
   @Override
   public Collection<String> getAllVersions(int execDefId) throws SCTMException {
     String[] versions = new String[0];
     try {
       ExecutionNode node = getExecDefNode(execDefId);
       versions = adminService.getVersions(execDefId, getProductName(node ));
     } catch (RemoteException e) {
       if (handleLostSessionException(e))
         return getAllVersions(execDefId);
       LOGGER.log(Level.SEVERE, e.getMessage(), e);
       throw new SCTMException(MessageFormat.format(Messages.getString("SCTMService.err.commonFatalError"), e.getMessage())); //$NON-NLS-1$
     }
     return toList(versions);
   }
 
   @Override
   public SPNamedEntity[] getResultFiles(int testDefRunId) throws SCTMException {
     try {
       return performerService.getExecutionFiles(sessionId, testDefRunId);
     } catch (RemoteException e) {
       if (handleLostSessionException(e))
         return getResultFiles(testDefRunId);
       LOGGER.log(Level.SEVERE, e.getMessage(), e);
       throw new SCTMException(MessageFormat.format(Messages.getString("SCTMService.err.commonFatalError"), e.getMessage())); //$NON-NLS-1$
     }
   }
 
   @Override
   public InputStream loadResultFile(int fileId) {
     try {
       URL url = new URL(String.format("%s&sid=%s&rfid=%s", this.serviceExchangeURL, this.sessionId, fileId)); //$NON-NLS-1$
       
       HttpClient client = new HttpClient();
       HttpMethod get = new GetMethod(url.toExternalForm());
       client.executeMethod(get);
       return get.getResponseBodyAsStream();
     } catch (MalformedURLException e) {
       LOGGER.log(Level.SEVERE, Messages.getString("SCTMService.err.malformedURL"), e); //$NON-NLS-1$
     } catch (HttpException e) {
       LOGGER.log(Level.FINE, Messages.getString("SCTMService.err.loadingResults"), e); //$NON-NLS-1$
     } catch (IOException e) {
       LOGGER.log(Level.SEVERE, Messages.getString("SCTMService.err.loadingResults"), e); //$NON-NLS-1$
     }
     return null; 
   }
 
   @Override
   public String getProductName(int nodeId) throws SCTMException {
     try {
       return getProductName(getExecDefNode(nodeId));
     } catch (RemoteException e) {
       if (handleLostSessionException(e))
         return getProductName(nodeId);
       LOGGER.log(Level.SEVERE, e.getMessage(), e);
       throw new SCTMException(MessageFormat.format(Messages.getString("SCTMService.err.commonFatalError"), e.getMessage())); //$NON-NLS-1$
     }
   }
 }
