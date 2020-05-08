 package org.levi.engine.impl;
 
 import org.levi.engine.*;
 import org.levi.engine.identity.IdentityService;
 import org.levi.engine.persistence.hibernate.HibernateDao;
 import org.levi.engine.persistence.hibernate.process.hobj.EngineDataBean;
 import org.levi.engine.utils.Bean2Impl;
 import org.levi.engine.persistence.hibernate.process.hobj.DeploymentBean;
 import org.levi.engine.utils.LeviUtils;
 import org.levi.engine.utils.ObjectLoader;
 import org.levi.engine.utils.ObjectSaver;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 /**
  * @author Ishan Jayawardena
  */
 public class ProcessEngineImpl implements ProcessEngine {
 
     private EngineData engineData;
     private StorageService storageService;
     private RuntimeService runtimeService;
     private TaskService taskService = new TaskServiceImpl();
     private IdentityService identityService = new IdentityServiceImpl();
 
     private static final ProcessEngine processEngine = new ProcessEngineImpl();
     //private static Logger log = Logger.getLogger(ProcessEngineImpl.class);
 
     private ProcessEngineImpl() {    // todo <== process engine configuration
         try {
             start();
         } catch (IOException e) {
             e.printStackTrace();
         } catch (ClassNotFoundException e) {
             e.printStackTrace();
         }
     }
 
     public static ProcessEngine getProcessEngine() {
         return processEngine;
     }
 
     private void start()
             throws IOException, ClassNotFoundException {
         File leviHome = new File(Constants.LEVI_HOME);
         if (!leviHome.exists()) {
             if (!leviHome.mkdirs()) {
                 throw new LeviException("Could not create Levi home at " + Constants.LEVI_HOME);
             }
             System.out.println("Created Levi home at " + Constants.LEVI_HOME);
         }
         File extractPath = new File(Constants.LAR_EXTRACT_PATH);
         if (!extractPath.exists()) {
             //extractPath.mkdir();
             if (!extractPath.mkdir()) {
                 throw new LeviException("Could not create LAR_EXTRACT_PATH at " + Constants.LAR_EXTRACT_PATH);
             }
             System.out.println("Created Lar extract path at " + Constants.LAR_EXTRACT_PATH);
         }
         File lomPath = new File(Constants.LOM_PATH);
         if (!lomPath.exists()) {
             if (!lomPath.mkdir()) {
                 throw new LeviException("Could not create LOM_PATH at " + Constants.LOM_PATH);
             }
             System.out.println("Created Lom directory at " + Constants.LOM_PATH);
         }
 
         /*
         File engineDataFile = new File(Constants.ENGINEDATA_PATH);
         if (engineDataFile.exists()) {
             ObjectLoader loader = new ObjectLoader(Constants.ENGINEDATA_PATH);
             EngineData engineData = (EngineData) loader.readNextObject();
             if (engineData == null) {
                 throw new RuntimeException("Retrieved engine data is null.");
             }
             this.engineData = engineData;
         } else {
             engineData = new EngineData();
         }
         */
         HibernateDao dao = new HibernateDao();
         try{
             EngineDataBean bean = (EngineDataBean)dao.getObject(EngineDataBean.class, "1");
             Bean2Impl b2i = new Bean2Impl();
             engineData = b2i.engineData(bean);
             //TODO need to clarified the exception   
         }catch(Exception e){
             engineData = new EngineData();
         }
 
         storageService = new StorageServiceImpl(engineData);
         storageService.start();
         runtimeService = new RuntimeServiceImpl(engineData);
         runtimeService.start();
     }
 
     public synchronized List<String> getDeploymentIds() {
         return LeviUtils.giveList(engineData.getDeploymentIds());
     }
 
     public List<DeploymentBean> getDeploymentBeans() {
         return engineData.getDeploymentBeans();
     }
 
     public synchronized List<String> getRunningProcessIds() {
         return LeviUtils.giveList(engineData.getRunningProcessIds());
     }
 
     public void resumeProcessInstance(String processId) {
         runtimeService.resumeProcessInstance(processId);
     }
 
     public synchronized void stop()
             throws FileNotFoundException, IOException {
         File f = new File(Constants.ENGINEDATA_PATH);
         if (f.exists()) {
             f.delete();
         } // todo this is too bad! fix it
         ObjectSaver saver = new ObjectSaver(Constants.ENGINEDATA_PATH);
         saver.saveObject(this.engineData);
         storageService.stop();
         runtimeService.stop();
     }
 
     public synchronized String deploy(String larPath) throws IOException {
         if (larPath == null) {
             throw new LeviException("Lar path is null.");
         }
         Deployment d = storageService.createDeployment(larPath).deploy();
         return d.getDefinitionsId();
     }
 
     public void undeployAll() throws IOException {
         storageService.undeployAll();
     }
 
     public synchronized void undeploy(String id) throws IOException {
         if (id == null) {
             throw new LeviException("Process ID is null.");
         }
         storageService.undeploy(id);
     }
 
     public void cleanup() throws IOException {
         storageService.cleanup();
     }
 
     public synchronized String startProcess(String id)
             throws IOException, ClassNotFoundException {
         return startProcess(id, LeviUtils.<String, Object>newHashMap(), null);   // Collections.<String, Object>emptyMap()   //TODO:Check!! userId=null??
     }
 
     public synchronized String startProcess(String id, Map<String, Object> variables, String userId)
             throws IOException, ClassNotFoundException {
         if (id == null) {
             throw new LeviException("Process ID is null.");
         }
         try {
             return runtimeService.startProcess(id, variables, userId);
         } catch (Exception e) {
             cleanup();
             throw new LeviException(e);
         }
     }
 
     public synchronized void stopProcess(String id)
             throws IOException, ClassNotFoundException {
         if (id == null) {
             throw new LeviException("Process ID is null.");
         }
         runtimeService.stopProcess(id);
     }
 
     public void claimUserTask(String pid, String userTaskId, Map<String, Object> variables) {
         runtimeService.claimUserTask(pid, userTaskId, variables);
     }
 
     public String getDiagramPath(String id) {
         if (id == null) {
             throw new LeviException("Process definitions id is null");
         }
         return storageService.getDiagramPath(id);
     }
 
     public void setVariable(String processId, String name, Object value) {
         if (processId == null) {
             throw new NullPointerException("Process Id is null.");
         }
         runtimeService.setVariable(processId, name, value);
     }
 
     public void setVariables(String processId, Map<String, Object> values) {
         if (processId == null) {
             throw new NullPointerException("Process Id is null.");
         }
         runtimeService.setVariables(processId, values);
     }
 
     public Object getVariable(String processId, String name) {
         if (processId == null) {
             throw new NullPointerException("Process Id is null.");
         }
         return runtimeService.getVariable(processId, name);
     }
 
     public Map<String, Object> getVariables(String processId) {
         if (processId == null) {
             throw new NullPointerException("Process Id is null.");
         }
         return runtimeService.getVariables(processId);
     }
 
     public boolean hasStartForm(String processId) {
         if (processId == null) {
             throw new NullPointerException("process ID is null.");
         }
         // get the definitions ID
         engineData.getProcessInstance(processId).getProcessId();
         return false;
     }
 
 
 }
