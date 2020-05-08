 package org.kevoree.library.sky.minicloud.nodeType;
 
 import org.kevoree.ContainerNode;
 import org.kevoree.ContainerRoot;
 import org.kevoree.annotation.*;
 import org.kevoree.api.service.core.handler.UUIDModel;
 import org.kevoree.cloner.ModelCloner;
 import org.kevoree.framework.Constants;
 import org.kevoree.framework.KevoreePlatformHelper;
 import org.kevoree.framework.KevoreePropertyHelper;
 import org.kevoree.library.defaultNodeTypes.JavaSENode;
 import org.kevoree.library.sky.api.CloudNode;
 import org.kevoree.library.sky.api.KevoreeNodeRunnerFactory;
 import org.kevoree.library.sky.api.PaaSNode;
 import org.kevoree.library.sky.api.execution.CommandMapper;
 import org.kevoree.library.sky.api.execution.KevoreeNodeManager;
 import org.kevoree.library.sky.api.execution.KevoreeNodeRunner;
 import org.kevoree.library.sky.api.planning.PlanningManager;
 import org.kevoree.library.sky.minicloud.MiniCloudKevoreeNodeRunner;
 import org.kevoree.log.Log;
 
 import java.lang.management.ManagementFactory;
 import java.util.List;
 import java.util.concurrent.ScheduledThreadPoolExecutor;
 import java.util.concurrent.TimeUnit;
 
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
         @DictionaryAttribute(name = "VMARGS", optional = true)
 })
 @NodeType
 @PrimitiveCommands(value = {
         @PrimitiveCommand(name = CloudNode.ADD_NODE, maxTime = 120000)
 }, values = {CloudNode.REMOVE_NODE})
 public class MiniCloudNode extends JavaSENode implements CloudNode, PaaSNode {
 
     private KevoreeNodeManager nodeManager;
 
     private ScheduledThreadPoolExecutor executor = null;
 
     @Start
     @Override
     public void startNode() {
         Log.debug("Starting node type of {}", this.getName());
         super.startNode();
         nodeManager = new KevoreeNodeManager(new MiniCloudNodeRunnerFactory());
         kompareBean = new PlanningManager(this);
         mapper = new CommandMapper(nodeManager);
         mapper.setNodeType(this);
 
         executor = new ScheduledThreadPoolExecutor(ManagementFactory.getOperatingSystemMXBean().getAvailableProcessors());
         executor.scheduleAtFixedRate(new IPManager(this), 15, 15, TimeUnit.SECONDS);
     }
 
     @Stop
     @Override
     public void stopNode() {
         Log.debug("Stopping node type of {}", this.getName());
         executor.shutdownNow();
         nodeManager.stop();
         super.stopNode();
     }
 
     public class MiniCloudNodeRunnerFactory implements KevoreeNodeRunnerFactory {
         @Override
         public KevoreeNodeRunner createKevoreeNodeRunner(String nodeName) {
             return new MiniCloudKevoreeNodeRunner(nodeName, MiniCloudNode.this);
         }
     }
 
     public class IPManager implements Runnable {
         private MiniCloudNode node;
 
         public IPManager(MiniCloudNode node) {
             this.node = node;
         }
 
         public synchronized void run() {
             UUIDModel uuidModel = node.getModelService().getLastUUIDModel();
             ModelCloner cloner = new ModelCloner();
             ContainerRoot readWriteModel = cloner.clone(node.getModelService().getLastModel());
 
             boolean update = false;
 
             ContainerNode nodeInstance = node.getModelElement();
             List<String> hostIps = KevoreePropertyHelper.instance$.getNetworkProperties(node.getModelService().getLastModel(), nodeInstance.getName(), Constants.instance$.getKEVOREE_PLATFORM_REMOTE_NODE_IP());
 
             if (hostIps.size() > 0) {
                Log.debug("The host {} has some ips so if its childs don't have at least one, we add all of them as ip of the childs");
                 for (ContainerNode child : nodeInstance.getHosts()) {
                     List<String> ips = KevoreePropertyHelper.instance$.getNetworkProperties(node.getModelService().getLastModel(), child.getName(), Constants.instance$.getKEVOREE_PLATFORM_REMOTE_NODE_IP());
                     if (ips.size() <= 0) {
                         for (String ip : hostIps) {
                             Log.debug("Adding {} as IP for {}", ip, child.getName());
                             KevoreePlatformHelper.instance$.updateNodeLinkProp(readWriteModel, child.getName(), child.getName(), org.kevoree.framework.Constants.instance$.getKEVOREE_PLATFORM_REMOTE_NODE_IP(), ip, "LAN", 100);
                             update = true;
                         }
                     }
                 }
             }
 
             if (update) {
                 node.getModelService().compareAndSwapModel(uuidModel, readWriteModel);
             }
         }
     }
 }
