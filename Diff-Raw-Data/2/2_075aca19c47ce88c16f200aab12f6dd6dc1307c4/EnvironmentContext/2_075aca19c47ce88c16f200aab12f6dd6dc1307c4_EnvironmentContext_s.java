 /*
  * Copyright 2005-2006 The Apache Software Foundation.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.apache.servicemix.jbi.container;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import javax.jbi.JBIException;
 import javax.management.JMException;
 import javax.management.MBeanAttributeInfo;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.servicemix.jbi.framework.LocalComponentConnector;
 import org.apache.servicemix.jbi.management.AttributeInfoHelper;
 import org.apache.servicemix.jbi.management.BaseSystemService;
 import org.apache.servicemix.jbi.util.FileUtil;
 import org.apache.servicemix.jbi.util.FileVersionUtil;
 
 import edu.emory.mathcs.backport.java.util.concurrent.ConcurrentHashMap;
 import edu.emory.mathcs.backport.java.util.concurrent.atomic.AtomicBoolean;
 
 /**
  * Holder for environment infomation
  * 
  * @version $Revision$
  */
 public class EnvironmentContext extends BaseSystemService implements EnvironmentContextMBean {
     private static final Log log = LogFactory.getLog(EnvironmentContext.class);
 
     private File jbiRootDir;
     private File componentsDir;
     private File installationDir;
     private File deploymentDir;
     private File sharedLibDir;
     private File serviceAssembiliesDirectory;
     private File tmpDir;
     private int statsInterval = 5;
     private Map envMap = new ConcurrentHashMap();
     private AtomicBoolean started = new AtomicBoolean(false);
     private boolean dumpStats = false;
     private Timer statsTimer;
     private TimerTask timerTask;
     private JBIContainer container;
 
 
     /**
      * @return the current version of servicemix
      */
     public static String getVersion() {
         String answer = null;
         Package p = Package.getPackage("org.apache.servicemix");
         if (p != null) {
             answer = p.getImplementationVersion();
         }
         return answer;
     }
 
     /**
      * Get Description
      * @return description
      */
     public String getDescription(){
         return "Manages Environment for the Container";
     }
     /**
      * @return Returns the componentsDir.
      */
     public File getComponentsDir() {
         return componentsDir;
     }
 
     /**
      * @return Returns the installationDir.
      */
     public File getInstallationDir() {
         return installationDir;
     }
     
     /**
      * Set the installationDir - rge default location
      * is root/<container name>/installation
      * @param installationDir
      */
     public void setInstallationDir(File installationDir){
         this.installationDir = installationDir;
     }
     
 
     /**
      * @return Returns the deploymentDir.
      */
     public File getDeploymentDir() {
         return deploymentDir;
     }
 
     /**
      * @param deploymentDir The deploymentDir to set.
      */
     public void setDeploymentDir(File deploymentDir) {
         this.deploymentDir = deploymentDir;
     }
     
     /**
      * 
      * @return Returns the shared library directory
      */
     public File getSharedLibDir(){
         return sharedLibDir;
     }
 
     /**
      * @return Returns the tmpDir
      */
     public File getTmpDir() {
         if (tmpDir != null) {
             FileUtil.buildDirectory(tmpDir);
         }
         return tmpDir;
     }
 
         
     /**
      * @return Returns service asseblies directory
      */
     public File getServiceAssembiliesDirectory(){
         return serviceAssembiliesDirectory;
     } 
    
 
     /**
      * Initialize the Environment
      * 
      * @param container
      * @param rootDirPath
      * @exception javax.jbi.JBIException if the root directory informed could not be created or it is not a directory
      */
     public void init(JBIContainer container, String rootDirPath) throws JBIException {
         this.container = container;
         jbiRootDir = new File(rootDirPath, container.getName());
         buildDirectoryStructure();
         container.getManagementContext().registerSystemService(this, EnvironmentContextMBean.class);
     }
 
     /**
      * Start the item.
      * 
      * @exception javax.jbi.JBIException if the item fails to start.
      */
     public void start() throws javax.jbi.JBIException {
         super.start();
         if (started.compareAndSet(false, true)) {
             scheduleStatsTimer();
         }
     }
 
     /**
      * Stop the item. This suspends current messaging activities.
      * 
      * @exception javax.jbi.JBIException if the item fails to stop.
      */
     public void stop() throws javax.jbi.JBIException {
         if (started.compareAndSet(true, false)) {
             super.stop();
             if (timerTask != null) {
                 timerTask.cancel();
             }
         }
     }
 
     /**
      * Shut down the item. The releases resources, preparatory to uninstallation.
      * 
      * @exception javax.jbi.JBIException if the item fails to shut down.
      */
     public void shutDown() throws javax.jbi.JBIException {
         super.shutDown();
         for (Iterator i = envMap.values().iterator();i.hasNext();) {
             ComponentEnvironment ce = (ComponentEnvironment) i.next();
             ce.close();
         }
         if (timerTask != null) {
             timerTask.cancel();
         }
         if (statsTimer != null) {
             statsTimer.cancel();
         }
         envMap.clear();
         container.getManagementContext().unregisterMBean(this);
     }
 
     /**
      * @return Returns the statsInterval (in secs).
      */
     public int getStatsInterval() {
         return statsInterval;
     }
 
     /**
      * @param statsInterval The statsInterval to set (in secs).
      */
     public void setStatsInterval(int statsInterval) {
         this.statsInterval = statsInterval;
         scheduleStatsTimer();
     }
 
     /**
      * @return Returns the dumpStats.
      */
     public boolean isDumpStats() {
         return dumpStats;
     }
 
     /**
      * @param value The dumpStats to set.
      */
     public void setDumpStats(boolean value) {
         if (dumpStats && !value) {
             if (timerTask != null) {
                 timerTask.cancel();
             }
         }
         else if (!dumpStats && value) {
             dumpStats = value;//scheduleStatsTimer relies on dumpStats value
             scheduleStatsTimer();
         }
         dumpStats = value;
     }
 
     protected void doDumpStats() {
         if (isDumpStats()) {
             for (Iterator i = envMap.values().iterator();i.hasNext();) {
                 ComponentEnvironment ce = (ComponentEnvironment) i.next();
                 ce.dumpStats();
             }
         }
     }
 
     /**
      * register the ComponentConnector
      * 
      * @param connector
      * @return the CompponentEnvironment
      * @throws JBIException
      */
     public ComponentEnvironment registerComponent(LocalComponentConnector connector) throws JBIException {
         ComponentEnvironment result = new ComponentEnvironment();
         return registerComponent(result,connector);
     }
     
     /**
      * register the ComponentConnector
      * 
      * @param connector
      * @return the CompponentEnvironment
      * @throws JBIException
      */
     public ComponentEnvironment registerComponent(ComponentEnvironment result,
                                                   LocalComponentConnector connector) throws JBIException {
         if (result == null) {
             result = new ComponentEnvironment();
         }
         if (!connector.isPojo()) {
             if (container.isEmbedded()) {
                 throw new JBIException("JBI component can not be installed in embedded mode");
             }
             // add workspace root and stats root ..
             try {
                 String name = connector.getComponentNameSpace().getName();
                 File componentRoot = getComponentRootDirectory(name);
                 FileUtil.buildDirectory(componentRoot);
                 File privateWorkspace = createWorkspaceDirectory(name);
                 result.setWorkspaceRoot(privateWorkspace);
                 result.setComponentRoot(componentRoot);
             } catch (IOException e) {
                 throw new JBIException(e);
             }
         }
         result.setLocalConnector(connector);
         envMap.put(connector, result);
         return result;
 	}
 
     /**
      * Get root directory for a Component
      * 
      * @param componentName
      * @return directory for deployment/workspace etc
      * @throws IOException
      */
     public File getComponentRootDirectory(String componentName) throws IOException {
         if (getComponentsDir() == null) {
             return null;
         }
         File result = FileUtil.getDirectoryPath(getComponentsDir(), componentName);
         // get the version directory
         result = FileVersionUtil.getLatestVersionDirectory(result);
         return result;
     }
 
     /**
      * Create root directory for a Component
      * 
      * @param componentName
      * @return directory for deployment/workspace etc
      * @throws IOException
      */
     public File createComponentRootDirectory(String componentName) throws IOException {
         if (getComponentsDir() == null) {
             return null;
         }
         File result = FileUtil.getDirectoryPath(getComponentsDir(), componentName);
         // get the version directory
         result = FileVersionUtil.createNewVersionDirectory(result);
         return result;
     }
     
     /**
      * Create installation directory for a Component
      * 
      * @param componentName
      * @return directory to deploy in
      * @throws IOException
      */
     public File getInstallationDirectory(String componentName) throws IOException {
         File result = getComponentRootDirectory(componentName);
         result = FileUtil.getDirectoryPath(result, "installation");
         return result;
     }
     
     /**
      * Get the file holding running state infomation for a Component
      * @param componentName
      * @return the state file
      * @throws IOException 
      */
     public File getComponentStateFile(String componentName) throws IOException {
         File result = getComponentRootDirectory(componentName);
         FileUtil.buildDirectory(result);
         result = new File(result,"state.xml");
         return result;
     }
     
     /**
      * Get the root directory for a Service Assembly
      * @param saName 
      * 
      * @return directory for deployment/workspace etc
      * @throws IOException
      */
     public File getSARootDirectory(String saName) throws IOException{
         if(getServiceAssembiliesDirectory()==null){
             return null;
         }
         File result=FileUtil.getDirectoryPath(getServiceAssembiliesDirectory(),saName);
         // get the version directory
         result=FileVersionUtil.getLatestVersionDirectory(result);
         return result;
     }
     
     /**
      * Create root directory for a Service Assembly
      * @param saName 
      * 
      * @return directory for deployment/workspace etc
      * @throws IOException
      */
     public File createSARootDirectory(String saName) throws IOException{
         if(getServiceAssembiliesDirectory()==null){
             return null;
         }
         File result=FileUtil.getDirectoryPath(getServiceAssembiliesDirectory(),saName);
         // get the version directory
         result=FileVersionUtil.getNewVersionDirectory(result);
         return result;
     }
     
        
     /**
      * Remove a Service Assembly directory
      * @param saName
      * @return true if successful
      * @throws IOException
      */
     public boolean removeSARootDirectory(String saName) throws IOException{
         File result = FileUtil.getDirectoryPath(getServiceAssembiliesDirectory(), saName);
         //get the version directory
         result=FileVersionUtil.getLatestVersionDirectory(result);
         return FileUtil.deleteFile(result);
     }
     
     /**
      * Get the file holding running state infomation for a Service Assembly
      * @param saName 
      * @return the state file
      * @throws IOException 
      */
     public File getServiceAssemblyStateFile(String saName) throws IOException {
         File result = getSARootDirectory(saName);
         FileUtil.buildDirectory(result);
         result = new File(result,"state.xml");
         return result;
     }
     
 
     /**
      * Create workspace directory for a Component
      * 
      * @param componentName
      * @return directory workspace
      * @throws IOException
      */
     public File createWorkspaceDirectory(String componentName) throws IOException {
         File result = getComponentRootDirectory(componentName);
         result = FileUtil.getDirectoryPath(result, "workspace");
         FileUtil.buildDirectory(result);
         return result;
     }
     
     /**
      * Create a SU directory for a Component
      * @param componentName
      * @param suName
      * @return directory
      * @throws IOException
      */
     public File getServiceUnitDirectory(String componentName,String suName) throws IOException {
         File result = getComponentRootDirectory(componentName);
         result = FileUtil.getDirectoryPath(result, "serviceunit");
         result = FileUtil.getDirectoryPath(result,suName);
         FileUtil.buildDirectory(result);
         return result;
     }
     
     /**
      * Remove a SU directory
      * @param componentName
      * @param suName
      * @return true if successful
      * @throws IOException
      */
     public boolean removeServiceUnitDirectory(String componentName,String suName) throws IOException {
         File result = FileUtil.getDirectoryPath(getComponentsDir(), componentName);
         result = FileUtil.getDirectoryPath(result, "serviceunit");
         result = FileUtil.getDirectoryPath(result,suName);
         return FileUtil.deleteFile(result);
     }
 
     /**
      * deregister the ComponentConnector
      * 
      * @param connector
      * @param doDelete true if component is to be deleted
      */
     public void unreregister(LocalComponentConnector connector, boolean doDelete) {
         ComponentEnvironment ce = (ComponentEnvironment) envMap.remove(connector);
         if (ce != null) {
             ce.close();
         }
         if (doDelete) {
             //remove Component directory structure
             String name = connector.getComponentNameSpace().getName();
             removeComponentRootDirectory(name);
         }
     }
 
     /**
      * Remove the Component root directory from the local file system
      * 
      * @param componentName
      */
     public void removeComponentRootDirectory(String componentName) {
         try {
             File file = getComponentRootDirectory(componentName);
             if (file != null) {
                 if (!FileUtil.deleteFile(file)) {
                     log.warn("Failed to remove directory structure for Component: " + componentName);
                 }
                 else {
                     log.info("Removed Component Root directory for " + componentName);
                 }
             }
         }
         catch (IOException e) {
             log.warn("Failed to remove directory structure for Component: " + componentName, e);
         }
     }
     
     /**
      * create a shared library directory
      * 
      * @param name
      * @return directory
      * @throws IOException
      */
     public File createSharedLibraryDirectory(String name) throws IOException {
         File result = FileUtil.getDirectoryPath(getSharedLibDir(), name);
         FileUtil.buildDirectory(result);
         return result;
     }
     
     /**
      * remove shared library directory
      * @param name
      * @throws IOException
      */
     public void removeSharedLibraryDirectory(String name) throws IOException{
         File result = FileUtil.getDirectoryPath(getSharedLibDir(), name);
         FileUtil.deleteFile(result);
     }
 
 
     private void buildDirectoryStructure() throws JBIException  {
         // We want ServiceMix to be able to run embedded
         // so do not create the directory structure if the root does not exist
         if (container.isEmbedded()) {
             return;
         }
         if (!jbiRootDir.exists()) {
             if (!jbiRootDir.mkdirs()) {
             	throw new JBIException("Directory could not be created: "+jbiRootDir.getAbsolutePath());
             }
         } else if (!jbiRootDir.isDirectory()) {
         	throw new JBIException("Not a directory: " + jbiRootDir.getAbsolutePath());
         }         
         if (installationDir == null){
             installationDir = FileUtil.getDirectoryPath(jbiRootDir, "install");
         }
         if (deploymentDir == null){
             deploymentDir = FileUtil.getDirectoryPath(jbiRootDir, "deploy");
         }
         componentsDir = FileUtil.getDirectoryPath(jbiRootDir, "components");
         tmpDir = FileUtil.getDirectoryPath(jbiRootDir, "tmp");
         sharedLibDir = FileUtil.getDirectoryPath(jbiRootDir, "sharedlibs");
         serviceAssembiliesDirectory = FileUtil.getDirectoryPath(jbiRootDir,"service-assemblies");
         //actually create the sub directories
         FileUtil.buildDirectory(installationDir);
         FileUtil.buildDirectory(deploymentDir);
         FileUtil.buildDirectory(componentsDir);
         FileUtil.buildDirectory(tmpDir);
         FileUtil.buildDirectory(sharedLibDir);
         FileUtil.buildDirectory(serviceAssembiliesDirectory);
     }
 
     private void scheduleStatsTimer() {
         if (isDumpStats()) {
             if (statsTimer == null) {
                 statsTimer = new Timer(true);
             }
             if (timerTask != null) {
                 timerTask.cancel();
             }
             timerTask = new TimerTask() {
                 public void run() {
                     doDumpStats();
                 }
             };
             long interval = statsInterval * 1000;
             statsTimer.scheduleAtFixedRate(timerTask, interval, interval);
         }
     }
     
     
     
    
 
     /**
      * Get an array of MBeanAttributeInfo
      * 
      * @return array of AttributeInfos
      * @throws JMException
      */
     public MBeanAttributeInfo[] getAttributeInfos() throws JMException {
         AttributeInfoHelper helper = new AttributeInfoHelper();
         helper.addAttribute(getObjectToManage(), "dumpStats", "Periodically dump Component statistics");
         helper.addAttribute(getObjectToManage(), "statsInterval", "Interval (secs) before dumping statistics");
        return helper.getAttributeInfos();
     }
 
 
 }
