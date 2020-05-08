 package org.kevoree.library.sky.api.planning;
 
 
 import org.kevoree.AdaptationPrimitiveType;
 import org.kevoree.ContainerNode;
 import org.kevoree.ContainerRoot;
 import org.kevoree.Instance;
 import org.kevoree.framework.AbstractNodeType;
 import org.kevoree.kompare.JavaSePrimitive;
 import org.kevoree.kompare.KevoreeKompareBean;
 import org.kevoree.kompare.scheduling.SchedulingWithTopologicalOrderAlgo;
 import org.kevoree.library.sky.api.CloudNode;
 import org.kevoree.log.Log;
 import org.kevoreeadaptation.AdaptationModel;
 import org.kevoreeadaptation.AdaptationPrimitive;
 import org.kevoreeadaptation.KevoreeAdaptationFactory;
 import org.kevoreeadaptation.ParallelStep;
 import org.kevoreeadaptation.impl.DefaultKevoreeAdaptationFactory;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * User: Erwan Daubert - erwan.daubert@gmail.com
  * Date: 13/12/11
  * Time: 09:19
  *
  * @author Erwan Daubert
  * @version 1.0
  */
 
 public class PlanningManager extends KevoreeKompareBean {
     private AbstractNodeType skyNode;
 
     public PlanningManager(AbstractNodeType skyNode) {
         this.skyNode = skyNode;
     }
 
     public AdaptationModel compareModels(ContainerRoot current, ContainerRoot target, String nodeName) {
         DefaultKevoreeAdaptationFactory factory = new DefaultKevoreeAdaptationFactory();
         AdaptationModel adaptationModel = factory.createAdaptationModel();
         AdaptationPrimitiveType removeNodeType = null;
         AdaptationPrimitiveType addNodeType = null;
         for (AdaptationPrimitiveType primitiveType : current.getAdaptationPrimitiveTypes()) {
             if (primitiveType.getName().equals(CloudNode.REMOVE_NODE)) {
                 removeNodeType = primitiveType;
             } else if (primitiveType.getName().equals(CloudNode.ADD_NODE)) {
                 addNodeType = primitiveType;
             }
         }
         if (removeNodeType == null || addNodeType == null) {
             for (AdaptationPrimitiveType primitiveType : target.getAdaptationPrimitiveTypes()) {
                 if (primitiveType.getName().equals(CloudNode.REMOVE_NODE)) {
                     removeNodeType = primitiveType;
                 } else if (primitiveType.getName().equals(CloudNode.ADD_NODE)) {
                     addNodeType = primitiveType;
                 }
             }
         }
         if (removeNodeType == null) {
             Log.warn("there is no adaptation primitive for {}", CloudNode.REMOVE_NODE);
         }
         if (addNodeType == null) {
             Log.warn("there is no adaptation primitive for {}", CloudNode.ADD_NODE);
         }
 
         ContainerNode currentNode = current.findNodesByID(skyNode.getName());
         ContainerNode targetNode = target.findNodesByID(skyNode.getName());
         if (currentNode != null) {
 
             if (targetNode != null) {
                 for (ContainerNode subNode : currentNode.getHosts()) {
                     ContainerNode subNode1 = targetNode.findHostsByID(subNode.getName());
                     if (subNode1 == null) {
                         Log.debug("add a {} adaptation primitive with {} as parameter", CloudNode.REMOVE_NODE, subNode.getName());
                         AdaptationPrimitive command = factory.createAdaptationPrimitive();
                         command.setPrimitiveType(removeNodeType);
                         command.setRef(subNode);
                         adaptationModel.addAdaptations(command);
                         processStopInstance(subNode, adaptationModel, current, factory);
                     }
 
                 }
 
             } else {
                 // TODO IF HARAKIRI is refactoring, maybe this block must be refactoring too or even removed
                 Log.debug("Unable to find the current node on the target model, We remove all the hosted nodes from the current model");
                 for (ContainerNode subNode : currentNode.getHosts()) {
                     Log.debug("add a {} adaptation primitive with {} as parameter", JavaSePrimitive.instance$.getStopInstance(), subNode.getName());
                     AdaptationPrimitive command = factory.createAdaptationPrimitive();
                    command.setPrimitiveType(removeNodeType);
                     command.setRef(subNode);
                     adaptationModel.addAdaptations(command);
                     processStopInstance(subNode, adaptationModel, current, factory);
                 }
             }
         }
 
         if (targetNode != null) {
             if (currentNode != null) {
                 for (ContainerNode subNode : targetNode.getHosts()) {
                     ContainerNode subNode1 = currentNode.findHostsByID(subNode.getName());
                     if (subNode1 == null) {
                         Log.debug("add a {} adaptation primitive with {} as parameter", CloudNode.ADD_NODE, subNode.getName());
                         AdaptationPrimitive command = factory.createAdaptationPrimitive();
                         command.setPrimitiveType(addNodeType);
                         command.setRef(subNode);
                         adaptationModel.addAdaptations(command);
                         processStartInstance(subNode, adaptationModel, current, factory);
                     } else {
                         if (subNode1.getStarted() != subNode.getStarted()) {
                             if (subNode1.getStarted()) {
                                 processStopInstance(subNode, adaptationModel, current, factory);
                             } else {
                                 processStartInstance(subNode, adaptationModel, current, factory);
                             }
                         }
                     }
                 }
             } else {
                 Log.debug("Unable to find the current node on the current model, We add all the hosted nodes from the target model");
                 for (ContainerNode subNode : targetNode.getHosts()) {
                     Log.debug("add a {} adaptation primitive with {} as parameter", CloudNode.ADD_NODE, subNode.getName());
                     AdaptationPrimitive command = factory.createAdaptationPrimitive();
                     command.setPrimitiveType(addNodeType);
                     command.setRef(subNode);
                     adaptationModel.addAdaptations(command);
                     processStartInstance(subNode, adaptationModel, current, factory);
                 }
             }
         }
 
         Log.debug("Adaptation model contain {} Host node primitives", adaptationModel.getAdaptations().size());
 
         AdaptationModel superModel = super.compareModels(current, target, nodeName);
 
         adaptationModel.addAllAdaptations(superModel.getAdaptations());
         Log.debug("Adaptation model contain {} primitives", adaptationModel.getAdaptations().size());
         return adaptationModel;
     }
 
     private void processStopInstance(Instance actualInstance, AdaptationModel adaptationModel, ContainerRoot actualRoot, KevoreeAdaptationFactory adaptationModelFactory) {
         Log.debug("Process StopInstance on {}", actualInstance.getName());
         AdaptationPrimitive ccmd2 = adaptationModelFactory.createAdaptationPrimitive();
         ccmd2.setPrimitiveType(actualRoot.findAdaptationPrimitiveTypesByID(JavaSePrimitive.instance$.getStopInstance()));
         ccmd2.setRef(actualInstance);
         adaptationModel.addAdaptations(ccmd2);
     }
 
     private void processStartInstance(Instance updatedInstance, AdaptationModel adaptationModel, ContainerRoot updateRoot, KevoreeAdaptationFactory adaptationModelFactory) {
         Log.debug("Process StartInstance on {}", updatedInstance.getName());
         AdaptationPrimitive ccmd2 = adaptationModelFactory.createAdaptationPrimitive();
         ccmd2.setPrimitiveType(updateRoot.findAdaptationPrimitiveTypesByID(JavaSePrimitive.instance$.getStartInstance()));
         ccmd2.setRef(updatedInstance);
         adaptationModel.addAdaptations(ccmd2);
     }
 
     @Override
     public AdaptationModel plan(AdaptationModel adaptationModel, String nodeName) {
         if (!adaptationModel.getAdaptations().isEmpty()) {
 
             Log.debug("Planning adaptation and defining steps...");
             SchedulingWithTopologicalOrderAlgo scheduling = new SchedulingWithTopologicalOrderAlgo();
             nextStep();
             adaptationModel.setOrderedPrimitiveSet(getCurrentStep());
 
             // STOP child nodes
             List<AdaptationPrimitive> primitives = new ArrayList<AdaptationPrimitive>();
             for (AdaptationPrimitive primitive : adaptationModel.getAdaptations()) {
                 if (primitive.getPrimitiveType().getName().equals(JavaSePrimitive.instance$.getStopInstance()) && primitive.getRef() instanceof ContainerNode) {
                     primitives.add(primitive);
                 }
             }
             createNextStep(JavaSePrimitive.instance$.getStopInstance(), primitives);
 
             // REMOVE child nodes
             primitives.clear();
             for (AdaptationPrimitive primitive : adaptationModel.getAdaptations()) {
                 if (primitive.getPrimitiveType().getName().equals(CloudNode.REMOVE_NODE)) {
                     primitives.add(primitive);
                 }
             }
             createNextStep(CloudNode.REMOVE_NODE, primitives);
 
             //STOP INSTANCEs (except child node)
             primitives.clear();
             for (AdaptationPrimitive primitive : adaptationModel.getAdaptations()) {
                 if (primitive.getPrimitiveType().getName().equals(JavaSePrimitive.instance$.getStopInstance()) && !(primitive.getRef() instanceof ContainerNode)) {
                     primitives.add(primitive);
                 }
             }
             primitives = scheduling.schedule(primitives, false);
             for (AdaptationPrimitive primitive : primitives) {
                 List<AdaptationPrimitive> primitiveList = new ArrayList<AdaptationPrimitive>(1);
                 primitiveList.add(primitive);
                 createNextStep(JavaSePrimitive.instance$.getStopInstance(), primitiveList);
             }
 
             // REMOVE BINDINGS
             primitives.clear();
             for (AdaptationPrimitive primitive : adaptationModel.getAdaptations()) {
                 if (primitive.getPrimitiveType().getName().equals(JavaSePrimitive.instance$.getRemoveBinding()) ||
                         primitive.getPrimitiveType().getName().equals(JavaSePrimitive.instance$.getRemoveFragmentBinding())) {
                     primitives.add(primitive);
                 }
             }
             createNextStep(JavaSePrimitive.instance$.getRemoveBinding(), primitives);
 
             // REMOVE INSTANCE
             primitives.clear();
             for (AdaptationPrimitive primitive : adaptationModel.getAdaptations()) {
                 if (primitive.getPrimitiveType().getName().equals(JavaSePrimitive.instance$.getRemoveInstance())) {
                     primitives.add(primitive);
                 }
             }
             createNextStep(JavaSePrimitive.instance$.getRemoveInstance(), primitives);
 
             // REMOVE TYPE
             primitives.clear();
             for (AdaptationPrimitive primitive : adaptationModel.getAdaptations()) {
                 if (primitive.getPrimitiveType().getName().equals(JavaSePrimitive.instance$.getRemoveType())) {
                     primitives.add(primitive);
                 }
             }
             createNextStep(JavaSePrimitive.instance$.getRemoveType(), primitives);
 
             // REMOVE TYPE
             primitives.clear();
             for (AdaptationPrimitive primitive : adaptationModel.getAdaptations()) {
                 if (primitive.getPrimitiveType().getName().equals(JavaSePrimitive.instance$.getRemoveDeployUnit())) {
                     primitives.add(primitive);
                 }
             }
             createNextStep(JavaSePrimitive.instance$.getRemoveDeployUnit(), primitives);
 
             // REMOVE THIRD PARTY
             primitives.clear();
             for (AdaptationPrimitive primitive : adaptationModel.getAdaptations()) {
                 if (primitive.getPrimitiveType().getName().equals(JavaSePrimitive.instance$.getAddThirdParty())) {
                     primitives.add(primitive);
                 }
             }
             createNextStep(JavaSePrimitive.instance$.getAddThirdParty(), primitives);
 
             // UPDATE DEPLOYUNITs
             primitives.clear();
             for (AdaptationPrimitive primitive : adaptationModel.getAdaptations()) {
                 if (primitive.getPrimitiveType().getName().equals(JavaSePrimitive.instance$.getUpdateDeployUnit())) {
                     primitives.add(primitive);
                 }
             }
             createNextStep(JavaSePrimitive.instance$.getUpdateDeployUnit(), primitives);
 
             // ADD DEPLOYUNITs
             primitives.clear();
             for (AdaptationPrimitive primitive : adaptationModel.getAdaptations()) {
                 if (primitive.getPrimitiveType().getName().equals(JavaSePrimitive.instance$.getAddDeployUnit())) {
                     primitives.add(primitive);
                 }
             }
             createNextStep(JavaSePrimitive.instance$.getAddDeployUnit(), primitives);
 
             // ADD TYPEs
             primitives.clear();
             for (AdaptationPrimitive primitive : adaptationModel.getAdaptations()) {
                 if (primitive.getPrimitiveType().getName().equals(JavaSePrimitive.instance$.getAddType())) {
                     primitives.add(primitive);
                 }
             }
             createNextStep(JavaSePrimitive.instance$.getAddType(), primitives);
 
             // ADD INSTANCEs
             primitives.clear();
             for (AdaptationPrimitive primitive : adaptationModel.getAdaptations()) {
                 if (primitive.getPrimitiveType().getName().equals(JavaSePrimitive.instance$.getAddInstance())) {
                     primitives.add(primitive);
                 }
             }
             createNextStep(JavaSePrimitive.instance$.getAddInstance(), primitives);
 
             // ADD BINDINGs
             primitives.clear();
             for (AdaptationPrimitive primitive : adaptationModel.getAdaptations()) {
                 if (primitive.getPrimitiveType().getName().equals(JavaSePrimitive.instance$.getAddBinding()) ||
                         primitive.getPrimitiveType().getName().equals(JavaSePrimitive.instance$.getAddFragmentBinding())) {
                     primitives.add(primitive);
                 }
             }
             createNextStep(JavaSePrimitive.instance$.getAddBinding(), primitives);
 
             // UPDATE DICTIONARY
             primitives.clear();
             for (AdaptationPrimitive primitive : adaptationModel.getAdaptations()) {
                 if (primitive.getPrimitiveType().getName().equals(JavaSePrimitive.instance$.getUpdateDictionaryInstance())) {
                     primitives.add(primitive);
                 }
             }
             createNextStep(JavaSePrimitive.instance$.getUpdateDictionaryInstance(), primitives);
 
 
             // ADD child nodes
             primitives.clear();
             for (AdaptationPrimitive primitive : adaptationModel.getAdaptations()) {
                 if (primitive.getPrimitiveType().getName().equals(CloudNode.ADD_NODE)) {
                     primitives.add(primitive);
                 }
             }
             createNextStep(CloudNode.ADD_NODE, primitives);
 
             // START child nodes
             primitives.clear();
             for (AdaptationPrimitive primitive : adaptationModel.getAdaptations()) {
                 if (primitive.getPrimitiveType().getName().equals(JavaSePrimitive.instance$.getStartInstance()) && primitive.getRef() instanceof ContainerNode) {
                     primitives.add(primitive);
                 }
             }
             createNextStep(JavaSePrimitive.instance$.getStartInstance(), primitives);
 
             // START INSTANCEs (except child nodes)
             ParallelStep oldStep = getCurrentStep();
             primitives.clear();
             for (AdaptationPrimitive primitive : adaptationModel.getAdaptations()) {
                 if (primitive.getPrimitiveType().getName().equals(JavaSePrimitive.instance$.getStartInstance()) && !(primitive.getRef() instanceof ContainerNode)) {
                     primitives.add(primitive);
                 }
             }
             primitives = scheduling.schedule(primitives, true);
             for (AdaptationPrimitive primitive : primitives) {
                 List<AdaptationPrimitive> primitiveList = new ArrayList<AdaptationPrimitive>(1);
                 primitiveList.add(primitive);
                 oldStep = getCurrentStep();
                 createNextStep(JavaSePrimitive.instance$.getStartInstance(), primitiveList);
             }
             // remove empty step at the end
             if (getStep() != null && getStep().getAdaptations().isEmpty()) {
                 oldStep.setNextStep(null);
             }
         } else {
             adaptationModel.setOrderedPrimitiveSet(null);
         }
         clearSteps();
         return adaptationModel;
 
     }
 }
