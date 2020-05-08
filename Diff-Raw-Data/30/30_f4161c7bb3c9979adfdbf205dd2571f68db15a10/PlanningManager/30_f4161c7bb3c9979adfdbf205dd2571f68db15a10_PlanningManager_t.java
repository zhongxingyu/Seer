 package org.kevoree.library.sky.lxc;
 
 import org.kevoree.framework.AbstractNodeType;
 import org.kevoree.library.sky.api.CloudNode;
 import org.kevoreeadaptation.AdaptationPrimitive;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * User: Erwan Daubert - erwan.daubert@gmail.com
  * Date: 01/07/13
  * Time: 10:10
  *
  * @author Erwan Daubert
  * @version 1.0
  */
 class PlanningManager extends org.kevoree.library.sky.api.planning.PlanningManager {
 
    private int maxAddNode;

    public PlanningManager(AbstractNodeType skyNode, int maxAddNode) {
         super(skyNode);
     }
 
     public void createNextStep(String primitiveType, List commands) {
         if (primitiveType == CloudNode.ADD_NODE) {
             List<AdaptationPrimitive> addNodeCommands = new ArrayList <AdaptationPrimitive>(10);
             for (AdaptationPrimitive command : (List<AdaptationPrimitive>) commands) {
                 addNodeCommands.add(command);
                if (addNodeCommands.size() == maxAddNode) {
                     super.createNextStep(CloudNode.ADD_NODE, addNodeCommands);
                     addNodeCommands.clear();
                 }
             }
             super.createNextStep(CloudNode.ADD_NODE, addNodeCommands);
         } else {
             super.createNextStep(primitiveType, commands);
         }
     }
 }
