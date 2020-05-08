 package com.griddynamics.jagger.jenkins.plugin;
 
 import hudson.Extension;
 import hudson.Launcher;
 import hudson.Proc;
 import hudson.console.ConsoleNote;
 import hudson.model.*;
 import hudson.tasks.BuildStepDescriptor;
 import hudson.tasks.Builder;
 import hudson.util.FormValidation;
 import hudson.util.LogTaskListener;
 import hudson.util.StreamTaskListener;
 import net.sf.json.JSONObject;
 import org.kohsuke.args4j.NamedOptionDef;
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.QueryParameter;
 import org.kohsuke.stapler.StaplerRequest;
 import org.springframework.beans.propertyeditors.PropertiesEditor;
 import org.w3c.dom.NodeList;
 
 import javax.net.ssl.SSLHandshakeException;
 import javax.xml.bind.PropertyException;
 import java.io.*;
 import java.util.*;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 
 public class JaggerEasyDeployPlugin extends Builder
 {
 
     //to collect nodes in one field.
     private ArrayList<Node> nodList = new ArrayList<Node>();
 
     //the collect nodes to attack in one field.
     private ArrayList<NodeToAttack> nodesToAttack = new ArrayList<NodeToAttack>();
 
     private final String PROPERTIES_PATH = "/properties";
 
    private MyProperties commonProperties ;
 
     /**
      * Constructor where fields from *.jelly will be passed
      * @param nodesToAttack
      *                      List of nodes to attack
      * @param nodList
      *               List of nodes to do work
      */
     @DataBoundConstructor
     public JaggerEasyDeployPlugin(ArrayList<NodeToAttack> nodesToAttack, ArrayList<Node> nodList){
 
         this.nodesToAttack = nodesToAttack;
         this.nodList = nodList;
     }
 
     public ArrayList<NodeToAttack> getNodesToAttack() {
         return nodesToAttack;
     }
 
     public ArrayList<Node> getNodList() {
         return nodList;
     }
 
     /**
      * To load EnvVars and create properties_files
      * @param build .
      * @param listener .
      * @return true
      */
     @Override
     public boolean prebuild(Build build, BuildListener listener) {
 
        commonProperties = new MyProperties();

         PrintStream logger = listener.getLogger();
 
         try {
             checkAddressesOnBuildVars(build.getEnvVars());//build.getEnvVars() this works, but deprecated
 
             setUpCommonProperties();
 
             //create folder to collect properties files
             File folder = new File(build.getWorkspace()+PROPERTIES_PATH);
             if(!folder.exists()) {folder.mkdirs();}
             logger.println("\nFOLDER WORKSPACE\n"+folder.toString()+"\n\n");
 
             for(Node node: nodList){
 
                 if(node.isSetPropertiesByHand()){ //then we should generate property file
 
                     generatePropertiesFile(node,folder);
                 }
             }
 
         } catch (Exception e) {
             logger.println("Exception in preBuild: " + e);
         }
 
      //   listener.getLogger().println(System.getProperties().stringPropertyNames());
 
         return true;
     }
 
     /**
      * Check if Build Variables contain addresses
      * @param ev Build Variables
      */
     private void checkAddressesOnBuildVars(Map<String,String> ev) {
 
         String address;
 
         for(Node node: nodList){
 
                 address = node.getServerAddress();
                 if(address.matches("\\$.+")) {
 
                     address = address.substring(1,address.length());
 
                     if(address.matches("\\{.+\\}")){
                         address = address.substring(1,address.length()-1);
                     }
 
                     if(ev.containsKey(address)){
                         node.setServerAddressActual(ev.get(address));
                     }
                 }
         }
 
         for(NodeToAttack node : nodesToAttack){
 
             address = node.getServerAddress();
             if(address.matches("\\$.+")) {
 
                 address = address.substring(1,address.length());
 
                 if(address.matches("\\{.+\\}")){
                     address = address.substring(1,address.length()-1);
                 }
 
                 if(ev.containsKey(address)){
                     node.setServerAddressActual(ev.get(address));
                 }
             }
         }
 
     }
 
     /**
      * rewriting fields on special class foe properties
      */
     private void setUpCommonProperties() {
 
         for(Node node : nodList) {
             if(node.getRdbServer() != null) {
                 setUpRdbProperties(node);
             }
             if(node.getCoordinationServer() !=  null) {
                 setUpCoordinationServerPropeties(node);
             }
         }
 
         for(NodeToAttack node : nodesToAttack) {
             setUpNodeToAttack(node);
         }
 //        for(Node node : nodList) {
 //            if (node.getRdbServer() != null){
 //                propTemp.fsDefaultName = "hdfs://" + node.getServerAddressActual() + "/";
 //                propTemp.rdbClientUrl = "jdbc:h2:tcp://" + node.getServerAddressActual() + ":" +
 //                        node.getRdbServer().getRdbPort() + "/" + node.getRdbServer().getRdbName();
 //            }
 //
 //            if(node.getCoordinationServer() != null) {
 //                propTemp.coordinatorZookeeperEndpoint = node.getServerAddress() +
 //                        "/" + node.getCoordinationServer().getPort();
 //            }
 //
 //        }
 //
 //        for(NodeToAttack node : nodesToAttack) {
 //            //! ports for attacking endpoints
 //                propTemp.serviceEndPoints.add("http://" + node.getServerAddress());
 //            //+ maybe port of environment that we try to Attack
 //        }
     }
 
     /**
      * Setting up Common Properties for Nodes
      * @param node node to attack
      */
     private void setUpNodeToAttack(NodeToAttack node) {
 
         String key = "test.service.endpoints";
         if(commonProperties.get(key) == null){
             commonProperties.setProperty(key, node.getServerAddressActual());
         } else {
             commonProperties.addValueWithComma(key, node.getServerAddressActual());
         }
     }
 
     /**
      * Setting up Common Properties for Nodes
      * @param node Node that play CoordinationServer Role
      */
     private void setUpCoordinationServerPropeties(Node node) {
 
         commonProperties.setProperty("chassis.coordinator.zookeeper.endpoint",node.getServerAddressActual() +
                                             ":" + node.getCoordinationServer().getPort());
         //Is this property belong to Coordination Server
        commonProperties.setProperty("chassis.storage.fs.default.name","hdfs://"+node.getServerAddressActual() + "/");
     }
 
     /**
      * Setting up Common Properties for Nodes
      * @param node Node that play RdbServer Role
      */
     private void setUpRdbProperties(Node node) {
         //if using H2 !!!
         commonProperties.setProperty("chassis.storage.rdb.client.driver", "org.h2.Driver");
         commonProperties.setProperty("chassis.storage.rdb.client.url","jdbc:h2:tcp://" +
                         node.getServerAddressActual() + ":" + node.getRdbServer().getRdbPort() +"/jaggerdb/db");
         commonProperties.setProperty("chassis.storage.rdb.username","jagger");
         commonProperties.setProperty("chassis.storage.rdb.password","rocks");
         commonProperties.setProperty("chassis.storage.hibernate.dialect","org.hibernate.dialect.H2Dialect");
         //standard port 8043 ! can it be changed? or hard code for ever?
         //if external bd ...
     }
 
     /**
      * Generating properties file for Node
      * @param node specified node
      * @param folder where to write file
      * @throws java.io.IOException /
      * @throws javax.xml.bind.PropertyException /
      */
     private void generatePropertiesFile(Node node, File folder) throws PropertyException, IOException {
 
         //file to store properties
         File filePath = new File(folder+"/"+node.getServerAddressActual()+".properties");
         if(filePath.exists()) {
             if(!filePath.delete()){
 
             }
         }
         MyProperties properties = new MyProperties();
 
         if(node.getMaster() != null){
             addMasterProperties(node, properties);
         }
         if(node.getCoordinationServer() != null){
             addCoordinationServerProperties(node, properties);
         }
         if(node.getRdbServer() != null){
             addRdbServerProperties(node, properties);
         }
         if(node.getReporter() != null){
             addReporterServerProperties(node,properties);
         }
         if(node.getKernel() != null){
             addKernelProperties(node, properties);
         }
 
         properties.store(new FileOutputStream(filePath), "generated automatically");
         node.setPropertiesPath(filePath.toString());
 
     }
 
     /**
      * Adding Reporter Server Properties
      * @param node  Node instance
      * @param properties    property of specified Node
      */
     private void addKernelProperties(Node node, MyProperties properties) {
 
         String key = "chassis.roles";
         if(properties.get(key) == null){
             properties.setProperty(key, node.getKernel().getRoleType().toString());
         } else {
             properties.addValueWithComma(key,node.getReporter().getRoleType().toString());
         }
 
         key = "chassis.coordinator.zookeeper.endpoint";
         properties.setProperty(key, commonProperties.getProperty(key));
 
         key = "chassis.storage.fs.default.name";
         properties.setProperty(key, commonProperties.getProperty(key));
 
         addDBProperties(properties);
 
         key = "test.service.endpoints";
         properties.setProperty(key, commonProperties.getProperty(key));
     }
 
     /**
      * Adding Reporter Server Properties
      * @param node  Node instance
      * @param properties    property of specified Node
      */
     private void addReporterServerProperties(Node node, MyProperties properties) {
 
         String key = "chassis.roles";
         if(properties.get(key) == null){
             properties.setProperty(key, node.getRdbServer().getRoleType().toString());
         } else {
             properties.addValueWithComma(key,node.getReporter().getRoleType().toString());
         }
 
 
     }
 
     /**
      * Adding RDB Server Properties
      * @param node  Node instance
      * @param properties    property of specified Node
      */
     private void addRdbServerProperties(Node node, MyProperties properties) {
 
         String key = "chassis.roles";
         if(properties.get(key) == null){
             properties.setProperty(key, node.getRdbServer().getRoleType().toString());
         } else {
             properties.addValueWithComma(key,node.getRdbServer().getRoleType().toString());
         }
 
     }
 
     /**
      * Adding Coordination Server Properties
      * @param node  Node instance
      * @param properties    property of specified Node
      */
     private void addCoordinationServerProperties(Node node, MyProperties properties) {
 
         String key = "chassis.roles";
         if(properties.get(key) == null){
             properties.setProperty(key, node.getCoordinationServer().getRoleType().toString());
         } else {
             properties.addValueWithComma(key,node.getCoordinationServer().getRoleType().toString());
         }
     }
 
     /**
      * Adding Master Properties
      * @param node  Node instance
      * @param properties    property of specified Node
      * @throws javax.xml.bind.PropertyException /
      */
     private void addMasterProperties(Node node, MyProperties properties) throws PropertyException {
 
         String key = "chassis.roles";
         if(properties.getProperty(key) == null){
             properties.setProperty(key, node.getMaster().getRoleType().toString());
         } else {
             properties.addValueWithComma(key,node.getMaster().getRoleType().toString());
         }
         //Http coordinator will always be on Master node (on port 8089?)!
         properties.addValueWithComma(key,"HTTP_COORDINATION_SERVER");
 
         key = "chassis.coordinator.zookeeper.endpoint";
         properties.setProperty(key, commonProperties.getProperty(key));
 
         key = "chassis.storage.fs.default.name";
         properties.setProperty(key, commonProperties.getProperty(key));
 
         addDBProperties(properties);
 
         key = "test.service.endpoints";
         properties.setProperty(key, commonProperties.getProperty(key));
     }
 
     private void addDBProperties(MyProperties properties) {
 
         String key;
 
         key = "chassis.storage.rdb.client.driver";
         properties.setProperty(key, commonProperties.getProperty(key));
 
         key = "chassis.storage.rdb.client.url";
         properties.setProperty(key, commonProperties.getProperty(key));
 
         key = "chassis.storage.rdb.username";
         properties.setProperty(key, commonProperties.getProperty(key));
 
         key = "chassis.storage.rdb.password";
         properties.setProperty(key, commonProperties.getProperty(key));
 
         key = "chassis.storage.hibernate.dialect";
         properties.setProperty(key, commonProperties.getProperty(key));
     }
 
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
     public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
 
         PrintStream logger = listener.getLogger();
         logger.println("\n______Jagger_Easy_Deploy_Started______\n");
         logger.println("\n______DEBUG_INFORMATION_NODES_WITH_ROLES______\n");
 
         try{
 
             for(NodeToAttack node:nodesToAttack){
                 logger.println("-------------------------");
                 logger.println(node.toString());
                 }
                 logger.println("-------------------------\n\n");
 
             for(Node node:nodList){
                 logger.println("-------------------------");
                 logger.println("Node address : "+node.getServerAddressActual());
                 logger.println("-------------------------");
                 logger.println("Node properties path : "+node.getPropertiesPath());
                 logger.println("-------------------------");
                 logger.println("Node's roles : ");
                 if(!node.getHmRoles().isEmpty()){
                     for(Role role: node.getHmRoles().values()){
                         logger.println(role.toString());
                     }
                 } else {
                     logger.println(node.getPropertiesPath());
                 }
                 logger.println("-------------------------\n-------------------------");
 
             }
 
             StringBuilder scriptToExecute = new StringBuilder();
             scriptToExecute.append("ls");
 
             Launcher.ProcStarter procStarter = launcher.new ProcStarter();
 
             procStarter.cmds(scriptToExecute.toString());
             //procStarter.envs(build.getEnvVars());
             procStarter.envs();
             procStarter.pwd(build.getWorkspace());   ///home/amikryukov/temp/
 
             Proc proc = launcher.launch(procStarter);
             logger.println(proc.getStdout());
             int exitCode = proc.join();
             if(exitCode != 0){
                 logger.println("launcher.launch code " + exitCode);
                 return false;
             }
 
            logger.println(build.getBuildVariables());
 
 
             return true;
 
         }catch (Exception e){
             logger.println("Troubles : " +e);
         }
             return false;
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
 
 
 ////    validation for nodeList not yet implemented
 //        /**
 //         * To test number of each role
 //         * @param nodList whole list of nodes that do work
 //         * @return OK if it's OK, ERROR otherwise
 //         */
 //        public FormValidation doCheckNodList(@QueryParameter final ArrayList<Node> nodList){
 //
 //
 //
 //            int numberOfMasters = 0,
 //                numberOfCoordServers = 0,
 //                numberOfKernels = 0,
 //                numberOfRdbServers = 0,
 //                numberOfReporters = 0;
 //
 //            try{
 //
 //                for(Node node:(List<Node>)nodList){
 //                    for(Role role:node.getHmRoles().values()){
 //                        if (role instanceof Kernel){
 //                            numberOfKernels ++;
 //                        } else if (role instanceof Master){
 //                            numberOfMasters ++;
 //                        } else if (role instanceof CoordinationServer){
 //                            numberOfCoordServers ++;
 //                        } else if (role instanceof Reporter){
 //                            numberOfReporters ++;
 //                        } else if (role instanceof RdbServer){
 //                            numberOfRdbServers ++;
 //                        } else {
 //                            throw new Exception("Where this come from? Not role!"); //temporary decision
 //                        }
 //                    }
 //                }
 //
 //                if(numberOfCoordServers == 0){
 //                    return FormValidation.error("no COORDINATION_SERVER was found");
 //                } else if (numberOfCoordServers > 1){
 //                    return FormValidation.error("more then one COORDINATION_SERVER was found");
 //                }
 //
 //                if(numberOfMasters == 0){
 //                    return FormValidation.error("no MASTER was found");
 //                } else if (numberOfMasters > 1) {
 //                    return FormValidation.error("more then one MASTER was found");
 //                }
 //
 //                if(numberOfRdbServers == 0){
 //                    return FormValidation.error("no RDB_SERVER was found");
 //                } else if (numberOfRdbServers > 1){
 //                    return FormValidation.error("more then one RDB_SERVER was found");
 //                }
 //
 //                if(numberOfReporters == 0){
 //                    return FormValidation.error("no REPORTER was found");
 //                } else if (numberOfReporters > 1){
 //                    return FormValidation.error("more then one REPORTER was found");
 //                }
 //
 //                if(numberOfKernels == 0){
 //                    return FormValidation.error("no KERNEL was found");
 //                }
 //
 //                return FormValidation.ok(nodList.getClass().getName());
 //            } catch (Exception e) {
 //                return FormValidation.error("something wrong");
 //            }
 //        }
     }
 
 }
