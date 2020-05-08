 package org.kevoree.library.sky.jails.nodeType;
 
 import org.kevoree.annotation.*;
 import org.kevoree.api.service.core.script.KevScriptEngine;
 import org.kevoree.library.defaultNodeTypes.JavaSENode;
 import org.kevoree.library.sky.api.CloudNode;
 import org.kevoree.library.sky.api.KevoreeNodeRunnerFactory;
 import org.kevoree.library.sky.api.execution.CommandMapper;
 import org.kevoree.library.sky.api.execution.KevoreeNodeManager;
 import org.kevoree.library.sky.api.execution.KevoreeNodeRunner;
 import org.kevoree.library.sky.api.planning.PlanningManager;
 import org.kevoree.library.sky.jails.JailKevoreeNodeRunner;
 import org.kevoree.library.sky.jails.JailsReasoner;
 import org.kevoree.log.Log;
 
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

 /**
  * User: Erwan Daubert - erwan.daubert@gmail.com
  * Date: 15/09/11
  * Time: 16:26
  *
  * @author Erwan Daubert
  * @version 1.0
  */
 
 @Library(name = "SKY")
 @DictionaryType({
         @DictionaryAttribute(name = "inet", optional = true),
         @DictionaryAttribute(name = "subnet", optional = true),
         @DictionaryAttribute(name = "mask", optional = true),
         @DictionaryAttribute(name = "defaultFlavor", optional = true),
         /*@DictionaryAttribute(name = "useArchive", defaultValue = "false", vals= {"true", "false"}, optional = true),
 		@DictionaryAttribute(name = "archives", defaultValue = "http://localhost:8080/archives/", optional = true),*/
         @DictionaryAttribute(name = "MODE", defaultValue = "RELAX", vals = {"STRICT", "RELAX", "AVOID"}, optional = true),
         // how the restrictions are managed : STRICT = the jail is stopped, RELAX = the jail continue to execute, AVOID means to refused to execute something that break the limitation
         @DictionaryAttribute(name = "alias_mask", defaultValue = "24", optional = true),
         @DictionaryAttribute(name = "availableFlavors", optional = true, defaultValue = "example"),
         @DictionaryAttribute(name = "manageChildKevoreePlatform", defaultValue = "false", vals = {"true", "false"})
 })
 @NodeType
 @PrimitiveCommands(value = {
         @PrimitiveCommand(name = CloudNode.ADD_NODE, maxTime = JailNode.ADD_TIMEOUT),
         @PrimitiveCommand(name = CloudNode.REMOVE_NODE, maxTime = JailNode.REMOVE_TIMEOUT)
 })
 public class JailNode extends JavaSENode implements CloudNode {
 
     private String inet;
     private String subnet;
     private String mask;
     private String aliasMask;
     boolean initialization;
     public static final long ADD_TIMEOUT = 300000l;
     public static final long REMOVE_TIMEOUT = 180000l;
 
     private KevoreeNodeManager nodeManager;
 
 
     @Start
     @Override
     public void startNode() {
         inet = this.getDictionary().get("inet").toString();
         subnet = this.getDictionary().get("subnet").toString();
         mask = this.getDictionary().get("mask").toString();
         aliasMask = this.getDictionary().get("alias_mask").toString();
         super.startNode();
         initialization = true;
         nodeManager = new KevoreeNodeManager(new JailNodeRunnerFactory());
         kompareBean = new PlanningManager(this);
         mapper = new CommandMapper(nodeManager);
         mapper.setNodeType(this);
     }
 
     @Stop
     @Override
     public void stopNode () {
         Log.debug("stopping node type of {}", this.getNodeName());
         nodeManager.stop();
         super.stopNode();
     }
 
     @Update
     public void updateNode() {
         if ((inet != null && this.getDictionary().get("inet") != null && !inet.equals(this.getDictionary().get("inet").toString()))
                 || (subnet != null && this.getDictionary().get("subnet") != null && !subnet.equals(this.getDictionary().get("subnet").toString()))
                 || (mask != null && this.getDictionary().get("mask") != null && !mask.equals(this.getDictionary().get("mask").toString()))) {
             stopNode();
             startNode();
         }
         super.updateNode();
     }
 
     public void modelUpdated() {
         if (initialization) {
             initialization = false;
             KevScriptEngine kengine = getKevScriptEngineFactory().createKevScriptEngine();
             // look at all the vms that are already defined and add them on the model
             if (JailsReasoner.createNodes(kengine, this)) {
                 getModelService().unregisterModelListener(this);
                 updateModel(kengine);
                 getModelService().registerModelListener(this);
             }
         }
     }
 
     public class JailNodeRunnerFactory implements KevoreeNodeRunnerFactory {
         @Override
         public KevoreeNodeRunner createKevoreeNodeRunner(String nodeName) {
             return new JailKevoreeNodeRunner(nodeName, JailNode.this, ADD_TIMEOUT, REMOVE_TIMEOUT, getDictionary().get("manageChildKevoreePlatform").equals("true"));
         }
     }
 
     public String getNetworkInterface() {
         return inet;
     }
 
     public String getNetwork() {
         return subnet;
     }
 
     public String getMask() {
         return mask;
     }
 
     public String getAliasMask() {
         return aliasMask;
     }
 
    public List<String> getDefaultFlavors() {
         if (this.getDictionary().get("defaultFlavor") != null) {
            return Arrays.asList(this.getDictionary().get("defaultFlavor").toString().trim().split(","));
         }
        return new ArrayList<String>(0);
     }
 
     public String[] getAvailableFlavors() {
         if (this.getDictionary().get("availableFlavors") != null) {
             return this.getDictionary().get("availableFlavors").toString().split(",");
         }
         return null;
     }
 
     public boolean useArchive() {
         return "true".equals(this.getDictionary().get("useArchive").toString());
     }
 
     public String getArchives() {
         return this.getDictionary().get("archives").toString();
     }
 
     protected void updateModel (KevScriptEngine kengine) {
         Boolean created = false;
         for (int i = 0; i < 20; i++) {
             try {
                 kengine.atomicInterpretDeploy();
                 created = true;
                 break;
             } catch (Exception e) {
                 Log.warn("Error while try to update the configuration of node " + getName() + ", try number " + i, e);
             }
         }
         if (!created) {
             Log.error("After 20 attempt, it was not able to update the configuration of {}", getName());
         }
     }
 }
