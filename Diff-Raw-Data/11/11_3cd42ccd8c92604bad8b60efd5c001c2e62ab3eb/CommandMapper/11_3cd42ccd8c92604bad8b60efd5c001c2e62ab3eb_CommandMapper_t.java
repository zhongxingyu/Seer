 package org.kevoree.library.sky.api.execution;
 
 import org.kevoree.ContainerNode;
 import org.kevoree.ContainerRoot;
 import org.kevoree.api.PrimitiveCommand;
 import org.kevoree.kompare.JavaSePrimitive;
 import org.kevoree.library.sky.api.CloudNode;
 import org.kevoree.library.sky.api.execution.command.AddNodeCommand;
 import org.kevoree.library.sky.api.execution.command.RemoveNodeCommand;
 import org.kevoree.library.sky.api.execution.command.StartNodeCommand;
 import org.kevoree.library.sky.api.execution.command.StopNodeCommand;
 import org.kevoree.log.Log;
 import org.kevoreeadaptation.AdaptationPrimitive;
 
 /**
  * User: Erwan Daubert - erwan.daubert@gmail.com
  * Date: 12/06/13
  * Time: 13:02
  *
  * @author Erwan Daubert
  * @version 1.0
  */
 public class CommandMapper extends org.kevoree.library.defaultNodeTypes.CommandMapper {
 
     private KevoreeNodeManager nodeManager;
 
     public CommandMapper(KevoreeNodeManager nodeManager) {
         this.nodeManager = nodeManager;
     }
 
 
     public PrimitiveCommand buildPrimitiveCommand(AdaptationPrimitive adaptationPrimitive, String nodeName) {
         Log.debug("ask for primitiveCommand corresponding to {}", adaptationPrimitive.getPrimitiveType().getName());
         PrimitiveCommand command = null;
        if (adaptationPrimitive.getPrimitiveType().getName().equals(CloudNode.REMOVE_NODE)) {
             Log.debug("add REMOVE_NODE command on {}", ((ContainerNode) adaptationPrimitive.getRef()).getName());
 
             ContainerNode targetNode = (ContainerNode) adaptationPrimitive.getRef();
             ContainerRoot targetNodeRoot = (ContainerRoot) ((ContainerNode) adaptationPrimitive.getRef()).eContainer();
             command = new RemoveNodeCommand(targetNodeRoot, targetNode.getName(), nodeManager);
        } else if (adaptationPrimitive.getPrimitiveType().getName().equals(CloudNode.ADD_NODE)) {
             Log.debug("add ADD_NODE command on {}", ((ContainerNode) adaptationPrimitive.getRef()).getName());
 
             ContainerNode targetNode = (ContainerNode) adaptationPrimitive.getRef();
             ContainerRoot targetNodeRoot = (ContainerRoot) ((ContainerNode) adaptationPrimitive.getRef()).eContainer();
             command = new AddNodeCommand(targetNodeRoot, targetNode.getName(), nodeManager);
        } else if (adaptationPrimitive.getPrimitiveType().getName().equals(JavaSePrimitive.instance$.getStartInstance()) && adaptationPrimitive.getRef() instanceof ContainerNode) {
             Log.debug("add START_NODE command on {}", ((ContainerNode) adaptationPrimitive.getRef()).getName());
 
             ContainerNode targetNode = (ContainerNode) adaptationPrimitive.getRef();
             ContainerRoot targetNodeRoot = (ContainerRoot) ((ContainerNode) adaptationPrimitive.getRef()).eContainer();
             command = new StartNodeCommand(targetNodeRoot, targetNode.getName(), nodeManager);
        } else if (adaptationPrimitive.getPrimitiveType().getName().equals(JavaSePrimitive.instance$.getStopInstance()) && adaptationPrimitive.getRef() instanceof ContainerNode) {
             Log.debug("add STOP_NODE command on {}", ((ContainerNode) adaptationPrimitive.getRef()).getName());
 
             ContainerNode targetNode = (ContainerNode) adaptationPrimitive.getRef();
             ContainerRoot targetNodeRoot = (ContainerRoot) ((ContainerNode) adaptationPrimitive.getRef()).eContainer();
             command = new StopNodeCommand(targetNodeRoot, targetNode.getName(), nodeManager);
         }
         if (command == null) {
             Log.debug("AdaptationPrimitive is not managed by CloudNode, asking to JavaSeNode...");
             command = super.buildPrimitiveCommand(adaptationPrimitive, nodeName);
         }
         return command;
     }
 
 }
