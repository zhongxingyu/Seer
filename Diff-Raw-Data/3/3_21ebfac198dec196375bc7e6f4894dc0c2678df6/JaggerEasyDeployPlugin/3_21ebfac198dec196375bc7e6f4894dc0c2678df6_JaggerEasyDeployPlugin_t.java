 package com.griddynamics.jagger.jenkins.plugin;
 
 import hudson.Extension;
 import hudson.Launcher;
 import hudson.model.*;
 import hudson.tasks.BuildStepDescriptor;
 import hudson.tasks.Builder;
 import hudson.util.*;
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.QueryParameter;
 
 import java.io.*;
 import java.util.*;
 
 
 public class JaggerEasyDeployPlugin extends Builder
 {
 
     //to collect kernel nodes in one field.
     private ArrayList<KernelNode> kernelNodeList = new ArrayList<KernelNode>();
 
     private final MasterNode masterNode;
 
     //the collect nodes to attack in one field.
     private ArrayList<SuT> sutsList = new ArrayList<SuT>();
 
     private final DBOptions dbOptions;
 
     private final AdditionalProperties additionalProperties;
 
     private JaggerProperties commonProperties ;
 
     //environment properties file for test suit
     private final String envProperties;
 
     private String envPropertiesActual;
 
     private StringBuilder deploymentScript;
 
     //path to Jagger Test Suit .zip
     private final String jaggerTestSuitePath;
 
     private String jaggerTestSuitePathActual;
 
     private final String baseDir = "result";
 
     private final String jaggerHome = "runned_jagger" ;
 
     private final boolean multiNodeConfiguration;
 
     //looking for processes with that names
     private final String AGENT_STARTER="AgentStarter";
 
     private final String JAGGER_LAUNCHER="JaggerLauncher";
 
     /**
      * Constructor where fields from *.jelly will be passed
      * @param sutsList
      *                      List of nodes to test
      * @param masterNode  Master
      * @param kernelNodeList  Kernels
      * @param jaggerTestSuitePath test suite path
      * @param dbOptions properties of dataBase
      * @param additionalProperties properties from text area
      * @param envProperties properties for all nodes
      */
     @DataBoundConstructor
     public JaggerEasyDeployPlugin(ArrayList<SuT> sutsList, ArrayList<KernelNode> kernelNodeList, String jaggerTestSuitePath, DBOptions dbOptions,
                                   AdditionalProperties additionalProperties, String envProperties, MasterNode masterNode) {
 
         this.dbOptions = dbOptions;
         this.sutsList = sutsList;
         this.kernelNodeList = kernelNodeList;
         this.jaggerTestSuitePath = jaggerTestSuitePath;
         this.jaggerTestSuitePathActual = jaggerTestSuitePath;
         this.additionalProperties = additionalProperties;
 
         this.envProperties = envProperties;
         this.envPropertiesActual = envProperties;
         this.masterNode = masterNode;
 
         multiNodeConfiguration = kernelNodeList != null && kernelNodeList.size() > 0;
     }
 
     public ArrayList<KernelNode> getKernelNodeList() {
         return kernelNodeList;
     }
 
     public MasterNode getMasterNode() {
         return masterNode;
     }
 
     public boolean isMultiNodeConfiguration() {
         return multiNodeConfiguration;
     }
 
     public String getBaseDir() {
         return baseDir;
     }
 
     public String getEnvPropertiesActual() {
         return envPropertiesActual;
     }
 
     public void setEnvPropertiesActual(String envPropertiesActual) {
         this.envPropertiesActual = envPropertiesActual;
     }
 
     public String getEnvProperties() {
         return envProperties;
     }
 
     public String getJaggerTestSuitePathActual() {
         return jaggerTestSuitePathActual;
     }
 
     public void setJaggerTestSuitePathActual(String jaggerTestSuitePathActual) {
         this.jaggerTestSuitePathActual = jaggerTestSuitePathActual;
     }
 
     public DBOptions getDbOptions() {
         return dbOptions;
     }
 
     public AdditionalProperties getAdditionalProperties() {
         return additionalProperties;
     }
 
     public String getJaggerTestSuitePath() {
         return jaggerTestSuitePath;
     }
 
     public ArrayList<SuT> getSutsList() {
         return sutsList;
     }
 
     public StringBuilder getDeploymentScript() {
         return deploymentScript;
     }
 
     /**
      * Loading EnvVars and create properties_files
      * @param build .
      * @param listener .
      * @return true
      */
     @Override
     public boolean prebuild(Build build, BuildListener listener) {
 
         PrintStream logger = listener.getLogger();
 
         try {
             checkUsesOfEnvironmentProperties(build, listener);
         } catch (IOException e) {
             logger.println("EXCEPTION WHILE CHECKING USES OF ENV VARAIBLES");
             logger.println(e);
             return false;
         } catch (InterruptedException e) {
             logger.println("EXCEPTION WHILE CHECKING USES OF ENV VARAIBLES");
             logger.println(e);
             return false;
         }
 
         setUpCommonProperties();
 
         generateDeploymentScript();
 
         logger.println("\n-------------Deployment-Script-------------------\n");
         logger.println(getDeploymentScript().toString());
         logger.println("\n-------------------------------------------------\n\n");
 
         return true;
     }
 
 
     private void generateDeploymentScript() {
 
         deploymentScript = new StringBuilder();
         deploymentScript.append("#!/bin/bash\n\n");
         deploymentScript.append("TimeStart=`date +%y/%m/%d_%H:%M`\n\n");
 
         deploymentScript.append("rm -rf ").append(getBaseDir()).append("\n");
         deploymentScript.append("mkdir ").append(getBaseDir()).append("\n\n");
 
 
 
         killOldJagger(deploymentScript);
 
         startAgents(deploymentScript);
 
         startKernels(deploymentScript);
 
         checkIfKernelsAgentsRuns(deploymentScript);
 
         startMasterNode(masterNode, deploymentScript);
 
         copyReports(deploymentScript);
 
         copyAllLogs(deploymentScript);
 
         deploymentScript.append("\n\ncd ").append(getBaseDir()).append("\n");
 
         deploymentScript.append("zip -9 ").append("report.zip *.pdf *.html *.xml\n");
 
         checkExitStatus(deploymentScript);
 
     }
 
     private void checkIfKernelsAgentsRuns(StringBuilder script) {
 
         script.append("\nsleep 5\n\necho \"Checking If Kernels, Agents runs\"\n\n");
 
         if( getSutsList() != null) {
             for( SshNode node : getSutsList()) {
                 checkIfProcessRuns(node, AGENT_STARTER, script);
             }
         }
 
         if(multiNodeConfiguration) {
             for(SshNode node : getKernelNodeList()) {
                 checkIfProcessRuns(node, JAGGER_LAUNCHER, script);
             }
         }
     }
 
     private void checkIfProcessRuns(SshNode node, String findName, StringBuilder script) {
 
         StringBuilder command = new StringBuilder();
         command.append("ps axwww | grep ").append(findName).append(" | wc -l");
 
         script.append("JOUT=$(");
         doOnVmSSH(node.getUserNameActual(), node.getServerAddressActual(), node.getSshKeyPathActual(), command.toString(), script);
         script.append(")\n");
 
         script.append("\n\tif ");
         script.append("[ \"$JOUT\" -le 2 ]");
         script.append(" ; then \n\t\techo \"No ").append(findName).append(" running on ");
         script.append(node.getServerAddressActual()).append("\"\n\t\texit $JOUT\n\tfi\n\n");
 
         script.append("echo \"number of process = $JOUT\"");
 
         script.append("\n\n");
     }
 
 
     private void checkExitStatus(StringBuilder script, int ... exitCodes) {
 
         script.append("\n\tstatus=$?\n\tif ");
         if(exitCodes.length == 0) {
             script.append("[ \"$status\" -ne 0 ]");
         } else {
             script.append("[ \"$status\" -ne ").append(exitCodes[0]).append(" ]");
             for(int i = 1; i < exitCodes.length ; i++){
                 script.append(" && [ \"$status\" -ne ").append(exitCodes[i]).append(" ]");
             }
         }
         script.append(" ; then \n\t\texit $status\n\tfi\n\n");
     }
 
 
 
     /**
      * stop Agents
      * @param deploymentScript /
      */
     private void stopJagger(StringBuilder deploymentScript) {
 
         if(getSutsList() != null) {
             for(SuT sut: getSutsList()) {
                 stopJaggerAgent(deploymentScript, sut.getUserNameActual(), sut.getServerAddressActual(),
                         sut.getSshKeyPathActual());
                 checkExitStatus(deploymentScript, 0 , 123);
             }
         }
     }
 
     private void stopJaggerAgent(StringBuilder script,
                                  String userName, String serverAddress, String keyPath) {
 
         doOnVmSSH(userName, serverAddress, keyPath, jaggerHome + File.separator + "stop_agent.sh", script);
         script.append("\n");
     }
 
     private void stopJagger(StringBuilder script,
                                  String userName, String serverAddress, String keyPath) {
 
         doOnVmSSH(userName, serverAddress, keyPath, jaggerHome + File.separator + "stop.sh", script);
         script.append("\n");
     }
 
 
     /**
      * provide ability to use environment properties
      * @param listener /
      * @param build    /
      * @throws java.io.IOException /
      * @throws InterruptedException /
      */
     private void checkUsesOfEnvironmentProperties(Build build, BuildListener listener) throws IOException, InterruptedException {
 
         checkNodesOnBuildVars(build, listener);
         checkAdditionalPropertiesOnBuildVars(build, listener);
         checkJaggerTestSuitOnBuildVars(build, listener);
         checkDBOptionsOnBuildVars(build, listener);
         checkAgentsOnBuildVars(build, listener);
         checkEnvPropertiesOnBuildVars(build, listener);
     }
 
     private void checkEnvPropertiesOnBuildVars(Build build, BuildListener listener) throws IOException, InterruptedException {
 
         String temp = getEnvProperties();
         setEnvPropertiesActual(build.getEnvironment(listener).expand(temp));
         if(getEnvPropertiesActual().matches("\\s*")) {
             setEnvPropertiesActual("./configuration/basic/default.environment.properties");
         }
     }
 
     private void checkAgentsOnBuildVars(Build build, BuildListener listener) throws IOException, InterruptedException {
 
         if(getSutsList() != null) {
             for(SuT node : getSutsList()) {
                 checkJavaHome(build, listener, node);
                 checkSshNodesServerAddresses(build, listener, node);
                 checkSshNodesSSHKeyPath(build, listener, node);
                 checkSshNodesUserName(build, listener, node);
                 checkJmxPort(build, listener, node);
                 checkJavaOptions(build, listener, node);
             }
         }
     }
 
     private void checkJmxPort(Build build, BuildListener listener, SuT node) throws IOException, InterruptedException {
 
         String temp = node.getJmxPort();
         node.setJmxPortActual(build.getEnvironment(listener).expand(temp));
     }
 
 
     private void checkDBOptionsOnBuildVars(Build build, BuildListener listener) throws IOException, InterruptedException {
 
         String temp = dbOptions.getRdbDialect();
         dbOptions.setRdbDialectActual(build.getEnvironment(listener).expand(temp));
 
         temp = dbOptions.getRdbUserName();
         dbOptions.setRdbUserNameActual(build.getEnvironment(listener).expand(temp));
 
         temp = dbOptions.getRdbClientUrl();
         dbOptions.setRdbClientUrlActual(build.getEnvironment(listener).expand(temp));
 
         temp = dbOptions.getRdbDriver();
         dbOptions.setRdbDriverActual(build.getEnvironment(listener).expand(temp));
     }
 
     private void checkJaggerTestSuitOnBuildVars(Build build, BuildListener listener) throws IOException, InterruptedException {
 
         String temp = getJaggerTestSuitePath();
         setJaggerTestSuitePathActual(build.getEnvironment(listener).expand(temp));
     }
 
 
     private void checkAdditionalPropertiesOnBuildVars(Build build, BuildListener listener) throws IOException, InterruptedException {
 
         String temp = getAdditionalProperties().getTextFromArea();
         additionalProperties.setTextFromAreaActual(build.getEnvironment(listener).expand(temp));
     }
 
 
     /**
      * Check if Build Variables contain addresses , or VERSION (of Jagger)
      * @param build  /
      * @param listener /
      * @throws java.io.IOException /
      * @throws InterruptedException /
      */
     private void checkNodesOnBuildVars(Build build, BuildListener listener) throws IOException, InterruptedException {
 
         checkSshNodesServerAddresses(build, listener, masterNode);
         checkSshNodesUserName(build, listener, masterNode);
         checkSshNodesSSHKeyPath(build, listener, masterNode);
         checkJavaHome(build, listener, masterNode);
         checkJavaOptions(build, listener, masterNode);
 
         if(multiNodeConfiguration) {
             for(Node node: kernelNodeList){
 
                 checkSshNodesServerAddresses(build, listener, node);
                 checkSshNodesUserName(build, listener, node);
                 checkSshNodesSSHKeyPath(build, listener, node);
                 checkJavaHome(build, listener, node);
                 checkJavaOptions(build, listener, node);
             }
         }
     }
 
     private void checkJavaOptions(Build build, BuildListener listener, SshNode node) throws IOException, InterruptedException {
 
         String temp = node.getJavaOptions();
         node.setJavaOptionsActual(build.getEnvironment(listener).expand(temp));
     }
 
     private void checkJavaHome(Build build, BuildListener listener, SshNode node) throws IOException, InterruptedException {
 
         String temp = node.getJavaHome();
         node.setJavaHomeActual(build.getEnvironment(listener).expand(temp));
     }
 
     private void checkSshNodesSSHKeyPath(Build build, BuildListener listener, SshNode node) throws IOException, InterruptedException {
 
         String temp = node.getSshKeyPath();
         node.setSshKeyPathActual(build.getEnvironment(listener).expand(temp));
     }
 
     private void checkSshNodesUserName(Build build, BuildListener listener, SshNode node) throws IOException, InterruptedException {
 
         String temp = node.getUserName();
         node.setUserNameActual(build.getEnvironment(listener).expand(temp));
     }
 
     private void checkSshNodesServerAddresses(Build build, BuildListener listener, SshNode node) throws IOException, InterruptedException {
 
         String temp = node.getServerAddress();
         node.setServerAddressActual(build.getEnvironment(listener).expand(temp));
     }
 
 
     private void copyAllLogs(StringBuilder script) {
 
         script.append("\n");
 
         copyMastersLogs(script);
 
         copyKernelsLogs(script);
 
         copyAgentsLogs(script);
     }
 
 
     private void copyAgentsLogs(StringBuilder script) {
 
         if(getSutsList() != null){
             for(SuT node : getSutsList()) {
                 script.append("\necho \"Copy agents logs\"\n");
                 copyLogs(node.getUserNameActual(), node.getServerAddressActual(), node.getSshKeyPathActual(), script);
                 script.append("echo \"Stop Agent\"\n");
                 stopJaggerAgent(script, node.getUserNameActual(), node.getServerAddressActual(), node.getSshKeyPathActual());
             }
         }
     }
 
     private void copyKernelsLogs(StringBuilder script) {
 
         if(multiNodeConfiguration) {
             for (Node node: kernelNodeList) {
                     script.append("\necho \"Copy kernels logs\"\n");
                     copyLogs(node.getUserNameActual(), node.getServerAddressActual(), node.getSshKeyPathActual(), script);
             }
         }
     }
 
     private void copyMastersLogs(StringBuilder script) {
 
         script.append("\necho \"Copy master logs\"\n");
         copyLogs(masterNode.getUserNameActual(), masterNode.getServerAddressActual(),
                 masterNode.getSshKeyPathActual(), script);
     }
 
 
     private void copyLogs(String userName, String address, String keyPath, StringBuilder script) {
 
         scpGetKey(userName, address, keyPath, jaggerHome + File.separator + "*.log*", getBaseDir(), script);
         script.append("cd " + baseDir + "; zip -9 ").append(address).append(".zip jagger*.log*; rm jagger*.log*; cd ..\n");
         checkExitStatus(script, 0, 1);
     }
 
 
     private void copyReports(StringBuilder script) {
 
         copyReports(masterNode, script);
         checkExitStatus(script, 0, 1);
     }
 
 
     private void copyReports(Node node, StringBuilder script) {
 
         String userName = node.getUserNameActual();
         String address = node.getServerAddressActual();
         String keyPath = node.getSshKeyPathActual();
 
         script.append("\n\necho \"Copy reports\"\n");
 
         scpGetKey(userName,
                 address,
                 keyPath,
                 "\"" + jaggerHome + File.separator + "*.xml " + jaggerHome + File.separator + "*.pdf " +
                         jaggerHome + File.separator + "*.html\"",
                 getBaseDir(),
                 script);
     }
 
 
     /**
      * Starting Nodes New
      * @param script deploymentScript
      */
     private void startKernels(StringBuilder script) {
 
         if(multiNodeConfiguration) {
 
             script.append("\n\necho \"Starting Kernels\"\n\n");
 
             for(Node node : kernelNodeList) {
 
                 startKernelNode(node, script);
             }
         }
     }
 
 
     private void startMasterNode(MasterNode node, StringBuilder script) {
 
         String userName = node.getUserNameActual();
         String address = node.getServerAddressActual();
         String keyPath = node.getSshKeyPathActual();
 
         script.append("\necho \"").append(address).append(" : cd ").append(jaggerHome).append("; ./start.sh properties_file\"\n");
 
         StringBuilder command = new StringBuilder();
         command.append("cd ").append(jaggerHome);
 
         if (node.isSetJavaHome()) {
             command.append("; export JAVA_HOME=").append(node.getJavaHomeActual());
         }
 
         command.append("; ./start.sh ").append(getEnvPropertiesActual()).append(" \'\\\n");
 
         if(!node.getJavaOptionsActual().matches("\\s*")) {
             command.append("\t").append(node.getJavaOptionsActual()).append(" \\\n");
         }
 
         String key = "chassis.conditions.min.agents.count";
         if(commonProperties.containsKey(key)) {
             command.append("\t-D").append(key).append("=").append(commonProperties.getProperty(key)).append(" \\\n");
         }
 
         key = "chassis.conditions.min.kernels.count";
         if(commonProperties.containsKey(key)) {
             command.append("\t-D").append(key).append("=").append(commonProperties.getProperty(key)).append(" \\\n");
         }
 
         key = RoleTypeName.MASTER + "," + RoleTypeName.COORDINATION_SERVER + "," + RoleTypeName.HTTP_COORDINATION_SERVER;
 
         if(getDbOptions().isUseExternalDB()) {
             setRdbProperties(command);
         } else {
             key += "," + RoleTypeName.RDB_SERVER;
         }
 
         if(!multiNodeConfiguration) {
             key += "," + RoleTypeName.KERNEL;
         }
 
         command.append("\t-Dchassis.roles=").append(key).append(" \\\n");
 
         key = "chassis.coordinator.zookeeper.endpoint";
         command.append("\t-D").append(key).append("=").append(commonProperties.getProperty(key)).append(" \\\n");
         key = "chassis.storage.fs.default.name";
         command.append("\t-D").append(key).append("=").append(commonProperties.getProperty(key)).append(" \\\n");
 
         if(getAdditionalProperties().isDeclared()) {
             for(String line: getAdditionalProperties().getTextFromAreaActual().split("\\n")) {
                 command.append("\t-D").append(line.trim()).append(" \\\n");
             }
         }
 
         command.append("\'");
 
         doOnVmSSH(userName, address, keyPath,
                 command.toString(), script);
 
         script.append("\n");
         checkExitStatus(script);
         script.append("\n\n");
     }
 
 
     private void startKernelNode(Node node, StringBuilder script) {
 
         String userName = node.getUserNameActual();
         String address = node.getServerAddressActual();
         String keyPath = node.getSshKeyPathActual();
 
         script.append("echo \"").append(address).append(" : cd ").append(jaggerHome).append("; ./start.sh properties_file\"\n");
 
         StringBuilder command = new StringBuilder();
         command.append("cd ").append(jaggerHome);
 
         if (node.isSetJavaHome()) {
             command.append("; export JAVA_HOME=").append(node.getJavaHomeActual());
         }
 
         command.append("; ./start.sh ").append(getEnvPropertiesActual()).append(" \'\\\n");
 
         if(!node.getJavaOptionsActual().matches("\\s*")) {
             command.append("\t").append(node.getJavaOptionsActual()).append(" \\\n");
         }
 
         command.append("\t-Dchassis.roles=" + RoleTypeName.KERNEL + " \\\n");
 
         setRdbProperties(command);
 
         String key;
 
         key = "chassis.coordinator.zookeeper.endpoint";
         command.append("\t-D").append(key).append("=").append(commonProperties.getProperty(key)).append(" \\\n");
         key = "chassis.storage.fs.default.name";
         command.append("\t-D").append(key).append("=").append(commonProperties.getProperty(key)).append(" \\\n");
 
         if(getAdditionalProperties().isDeclared()) {
             for(String line: getAdditionalProperties().getTextFromAreaActual().split("\\n")) {
                 command.append("\t-D").append(line.trim()).append(" \\\n");
             }
         }
 
         command.append("\' > /dev/null 2>&1");
 
         doOnVmSSHDaemon(userName, address, keyPath,
                 command.toString(), script);
 
         script.append("\n\n");
 
     }
 
 
     private void setRdbProperties(StringBuilder command) {
 
         String key = "chassis.storage.rdb.client.driver";
         command.append("\t-D").append(key).append("=").append(commonProperties.getProperty(key)).append(" \\\n");
         key = "chassis.storage.rdb.client.url";
         command.append("\t-D").append(key).append("=").append(commonProperties.getProperty(key)).append(" \\\n");
         key = "chassis.storage.rdb.username";
         command.append("\t-D").append(key).append("=").append(commonProperties.getProperty(key)).append(" \\\n");
         key = "chassis.storage.rdb.password";
         command.append("\t-D").append(key).append("=").append(commonProperties.getProperty(key)).append(" \\\n");
         key = "chassis.storage.hibernate.dialect";
         command.append("\t-D").append(key).append("=").append(commonProperties.getProperty(key)).append(" \\\n");
     }
 
 
     /**
      * Starting Agents, if it declared
      * @param script deploymentScript
      */
     private void startAgents(StringBuilder script) {
 
         if (getSutsList() != null) {
             for(SuT node : getSutsList()){
 
                 script.append("echo \"Starting Agents\"\n");
                 script.append("echo \"").append(node.getServerAddressActual()).append(" : cd ").append(jaggerHome).append("; ./start_agent.sh\"\n");
 
                 StringBuilder command = new StringBuilder();
                 command.append("cd ").append(jaggerHome);
 
                 if(node.isSetJavaHome()) {
                     command.append("; export JAVA_HOME=").append(node.getJavaHomeActual());
                 }
 
                 command.append("; ./start_agent.sh \'\\\n\t");
 
                 if(!node.getJavaOptionsActual().matches("\\s*")) {
                     command.append(node.getJavaOptionsActual()).append(" \\\n\t");
                 }
 
                 command.append("-Dchassis.coordination.http.url=");
                 command.append(commonProperties.get("chassis.coordination.http.url")).append(" \\\n\t");
 
                 if(node.isUseJmx()) {
 
                     command.append("-Djmx.enabled=true \\\n\t");
                     String[] ports = node.getJmxPortActual().split(",");
                     command.append("-Djmx.services=");
                     for(int i = 0; i<ports.length -1 ; i ++) {
                         command.append("localhost:").append(ports[i]).append(",");// with ";" in old version Jagger
                     }
                     command.append("localhost:").append(ports[ports.length-1]);
                     command.append(" \\\n");
                 } else {
                      command.append("-Djmx.enabled=false \\\n");
                 }
 
                 if(getAdditionalProperties().isDeclared()) {
                     for(String line: getAdditionalProperties().getTextFromAreaActual().split("\\n")) {
                         command.append("\t-D").append(line.trim()).append(" \\\n");
                     }
                 }
 
                 command.append("\' > /dev/null 2>&1");
                 doOnVmSSHDaemon(node.getUserNameActual(), node.getServerAddressActual(), node.getSshKeyPathActual(), command.toString(), script);
 
                 script.append("\n");
             }
         }
     }
 
 
     /**
      * kill old Jagger , deploy new one , stop processes in jagger
      * @param script String Builder of deployment Script
      */
     private void killOldJagger(StringBuilder script) {
 
         script.append("\necho \"KILLING old jagger\"\n\n");
 
         killOldJagger1(masterNode.getUserNameActual(), masterNode.getServerAddressActual(),
                 masterNode.getSshKeyPathActual(), jaggerHome, script);
 
         if(multiNodeConfiguration) {
             for(Node node:kernelNodeList){
 
                 killOldJagger1(node.getUserNameActual(), node.getServerAddressActual(),
                         node.getSshKeyPathActual(), jaggerHome,  script);
             }
         }
 
         if (getSutsList() != null) {
             for(SuT node : getSutsList()){
 
                 killOldJagger1(node.getUserNameActual(), node.getServerAddressActual(),
                         node.getSshKeyPathActual(), jaggerHome, script);
             }
         }
 
     }
 
 
     private void killOldJagger1(String userName, String serverAddress, String keyPath, String jaggerHome, StringBuilder script){
 
         script.append("echo \"TRYING TO DEPLOY JAGGER to ").append(userName).append("@").append(serverAddress).append("\"\n");
         doOnVmSSH(userName, serverAddress, keyPath, "rm -rf " + jaggerHome, script);
         script.append("\n");
         checkExitStatus(script);
         script.append("\n");
 
         doOnVmSSH(userName, serverAddress, keyPath, "mkdir " + jaggerHome, script);
         script.append("\n");
         checkExitStatus(script);
         script.append("\n");
 
         scpSendKey(userName,
                 serverAddress,
                 keyPath,
                 getJaggerTestSuitePathActual(),
                 jaggerHome, script);
         checkExitStatus(script);
         script.append("\n");
 
         //here we take name of file from path: '~/path/to/file' -> 'file'
         String jaggerFileName = getJaggerTestSuitePathActual();
         int index = getJaggerTestSuitePathActual().lastIndexOf(File.separator);
         if(index >= 0) {
             jaggerFileName = getJaggerTestSuitePathActual().substring(index + 1);
         }
 
         doOnVmSSH(userName, serverAddress, keyPath,
                 "unzip " + jaggerHome + File.separator + jaggerFileName + " -d " + jaggerHome,
                 script);
         script.append(" > ").append(File.separator).append("dev").append(File.separator).append("null\n");
         checkExitStatus(script);
         script.append("\n\n");
 
         script.append("echo \"KILLING previous processes ").append(userName).append("@").append(serverAddress).append("\"\n");
 
         stopJagger(script, userName, serverAddress, keyPath);
         checkExitStatus(script, 0, 123);
         stopJaggerAgent(script, userName, serverAddress, keyPath);
         checkExitStatus(script, 0, 123);
 
         script.append("\n\n");
     }
 
 
     /**
      *  Common Properties that will be used
      */
     private void setUpCommonProperties()  {
 
         String key = "chassis.coordination.http.url";
 
         commonProperties = new JaggerProperties();
         commonProperties.setProperty(key,
                 "http://" + masterNode.getServerAddressActual() + ":8089");
 
         setUpMasterProperties(masterNode);
         setUpCoordinatorProperties(masterNode, 2181);
 
         setUpRdbProperties();
 
         key = "chassis.conditions.min.kernels.count";
         if(multiNodeConfiguration) {
 
             commonProperties.setProperty(key , String.valueOf(kernelNodeList.size()));
         } else {
 
             commonProperties.setProperty(key, "1");
         }
 
         key = "chassis.conditions.min.agents.count";
         if(getSutsList() != null) {
 
             commonProperties.setProperty(key, String.valueOf(getSutsList().size()));
         } else {
 
             commonProperties.setProperty(key, "0");
         }
     }
 
 
     private void setUpMasterProperties(Node node) {
 
         commonProperties.setProperty("chassis.coordination.http.url",
                 "http://" + node.getServerAddressActual() + ":8089");
 
         commonProperties.setProperty("chassis.storage.fs.default.name", "hdfs://" + node.getServerAddressActual() + "/");
     }
 
 
     private void setUpCoordinatorProperties(Node node, int port) {
 
         commonProperties.setProperty("chassis.coordinator.zookeeper.endpoint", node.getServerAddressActual() + ":" + port);
     }
 
 
     /**
      * Setting up Common Properties for Nodes
      */
     private void setUpRdbProperties() {
 
         if (!dbOptions.isUseExternalDB()) {
 
             setUpH2RdbProperties(masterNode, 8043);
         } else {
 
             commonProperties.setProperty("chassis.storage.rdb.client.driver", getDbOptions().getRdbDriverActual());
             commonProperties.setProperty("chassis.storage.rdb.client.url", getDbOptions().getRdbClientUrlActual());
             commonProperties.setProperty("chassis.storage.rdb.username", getDbOptions().getRdbUserNameActual());
             commonProperties.setProperty("chassis.storage.rdb.password", getDbOptions().getRdbPassword());
             commonProperties.setProperty("chassis.storage.hibernate.dialect", getDbOptions().getRdbDialectActual());
         }
     }
 
 
     private void setUpH2RdbProperties(Node node ,int port) {
 
         commonProperties.setProperty("chassis.storage.rdb.client.driver", "org.h2.Driver");
         commonProperties.setProperty("chassis.storage.rdb.client.url","jdbc:h2:tcp://" +
                         node.getServerAddressActual() + ":" + port +"/jaggerdb");
         commonProperties.setProperty("chassis.storage.rdb.username","jagger");
         commonProperties.setProperty("chassis.storage.rdb.password", "rocks");
         commonProperties.setProperty("chassis.storage.hibernate.dialect","org.hibernate.dialect.H2Dialect");
     }
 
 
     // Start's processes on computer where jenkins run ProcStarter is not serializable
     transient private Launcher.ProcStarter procStarter = null;
 
 
     /**
      * This method will be called in build time (when you build job)
      * @param build   .
      * @param launcher .
      * @param listener .
      * @return boolean : true if build passed, false in other way
      * @throws InterruptedException
      * @throws IOException
      */
     @Override
     public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)  throws InterruptedException, IOException {
 
         PrintStream logger = listener.getLogger();
         logger.println("\n______Jagger_Easy_Deploy_Started______\n");
         String pathToDeploymentScript = build.getWorkspace() + File.separator + "deploy-script.sh";
 
         try{
 
             setUpProcStarter(launcher,build,listener);
 
             createScriptFile(pathToDeploymentScript);
 
             logger.println("\n-----------------Deploying--------------------\n\n");
 
             int exitCode = procStarter.cmds(stringToCmds("./deploy-script.sh")).start().join();
 
             logger.println("exit code : " + exitCode);
 
             logger.println("\n\n----------------------------------------------\n\n");
 
             if(exitCode != 0) {
 
                 if(getSutsList() != null) {
 
                     for(SuT sut: getSutsList()) {
                         deploymentScript.setLength(0);
 
                         stopJaggerAgent(deploymentScript, sut.getUserNameActual(), sut.getServerAddressActual(),
                                 sut.getSshKeyPathActual());
 
                         logger.println("\n" + deploymentScript.toString() + "\n");
 
                         procStarter.cmds(stringToCmds(deploymentScript.toString())).start().join();
 
                         logger.println("\n");
                     }
                 }
 
                 return false;
             } else {
 
                 return true;
             }
         } catch (IOException e) {
 
             logger.println("!!!\nException in perform " + e +
                     "can't create script file or run script");
 
             if(new File(pathToDeploymentScript).delete()) {
                 logger.println(pathToDeploymentScript + " has been deleted");
             } else {
                 logger.println(pathToDeploymentScript + " haven't been created");
             }
         }
 
         return true;
     }
 
 
     /**
      * creating script file to execute later
      * @throws IOException  if can't create file  or ru cmds.
      * @param file 5
      */
     private void createScriptFile(String file) throws IOException {
 
         PrintWriter fw = null;
         try{
             fw = new PrintWriter(new FileOutputStream(file));
             fw.write(deploymentScript.toString());
 
         } finally {
             if(fw != null){
                 fw.close();
             }
         }
 
         //setting permissions for executing
         procStarter.cmds(stringToCmds("chmod +x " + file)).start();
     }
 
 
     /**
      * Copy files via scp using public key autorisation
      * @param userName user name
      * @param address   address of machine
      * @param keyPath   path of private key
      * @param filePathFrom  file path that we want to copy
      * @param filePathTo  path where we want to store file
      * @param script String Builder for deployment script
      */
     private void scpGetKey(String userName, String address, String keyPath, String filePathFrom, String filePathTo, StringBuilder script) {
 
         script.append("scp");
         if(! keyPath.matches("\\s*")) {
             script.append(" -i ");
             script.append(keyPath);
         }
         script.append(" ");
         script.append(userName);
         script.append("@");
         script.append(address);
         script.append(":");
         script.append(filePathFrom);
         script.append(" ");
         script.append(filePathTo).append("\n");
 
     }
 
 
     /**
      * Copy files via scp using public key autorisation
      * @param userName user name
      * @param address   address of machine
      * @param keyPath   path of private key
      * @param filePathFrom  file path that we want to copy
      * @param filePathTo  path where we want to store file
      * @param script String Builder for deployment script
      */
     private void scpSendKey(String userName, String address, String keyPath, String filePathFrom, String filePathTo, StringBuilder script) {
 
         script.append("scp");
         if(! keyPath.matches("\\s*")) {
             script.append(" -i ");
             script.append(keyPath);
         }
         script.append(" ");
         script.append(filePathFrom);
         script.append(" ");
         script.append(userName);
         script.append("@");
         script.append(address);
         script.append(":");
         script.append(filePathTo).append("\n");
 
     }
 
 
     private void setUpProcStarter(Launcher launcher, AbstractBuild<?, ?> build, BuildListener listener) {
 
        procStarter = launcher.launch();
         procStarter.pwd(build.getWorkspace());
         procStarter.stdout(listener);
     }
 
 
     /**
      * do commands on remote machine via ssh using public key authorisation
      * @param userName user name
      * @param address address of machine
      * @param keyPath path to private key
      * @param commandString command
      * @param script String Builder where we merge all commands
      */
     private void doOnVmSSH(String userName, String address, String keyPath, String commandString,StringBuilder script) {
 
         script.append("ssh");
         if(! keyPath.matches("\\s*")) {
             script.append(" -i ");
             script.append(keyPath);
         }
         script.append(" ").append(userName).append("@").append(address);
         script.append(" \"").append(commandString).append("\"");
     }
 
 
     /**
      * do commands daemon on remote machine via ssh using public key authorisation
      *
      * @param userName user name
      * @param address address of machine
      * @param keyPath path to private key
      * @param commandString command
      * @param script String Builder where we merge all commands
      */
     private void doOnVmSSHDaemon(String userName, String address, String keyPath, String commandString,StringBuilder script) {
 
         script.append("ssh -f");
         if(! keyPath.matches("\\s*")) {
             script.append(" -i ");
             script.append(keyPath);
         }
         script.append(" ").append(userName).append("@").append(address);
         script.append(" \"").append(commandString).append("\"");
     }
 
 
     /**
      * String to array
      * cd directory >> [cd, directory]
      * @param str commands in ine string
      * @return array of commands
      */
     private String[] stringToCmds(String str){
         return QuotedStringTokenizer.tokenize(str);
     }
 
 
     /**
      * Unnecessary, but recommended for more type safety
      * @return Descriptor of this class
      */
     @Override
     public Descriptor<Builder> getDescriptor() {
         return (DescriptorJEDP)super.getDescriptor();
     }
 
     @Extension
     public static final class DescriptorJEDP  extends BuildStepDescriptor<Builder>
     {
 
         @Override
         public boolean isApplicable(Class<? extends AbstractProject> aClass) {
             return true;
         }
 
         @Override
         public String getDisplayName() {
             //how it names in build step config
             return "Jagger Easy Deploy";
         }
 
         public FormValidation doCheckEnvProperties(@QueryParameter final String value) {
 
             if(value.matches("\\s*")){
                 return FormValidation.ok("default properties will be used");
             }
 
             return FormValidation.ok();
         }
 
         public FormValidation doCheckJaggerTestSuitePath(@QueryParameter final String value) {
 
             if(value.matches("\\s*")){
                 return FormValidation.warning("set path, please");
             }
             if(value.contains("$")) {
                 return FormValidation.ok();
             }
             String temp = value;
             if(value.startsWith("~")){
                 temp = temp.substring(1,temp.length());
                 temp = System.getProperty("user.home") + temp;
             }
             if(!new File(temp).exists()){
                 return FormValidation.error("file not exists");
             }
 
             return FormValidation.ok();
         }
     }
 }
