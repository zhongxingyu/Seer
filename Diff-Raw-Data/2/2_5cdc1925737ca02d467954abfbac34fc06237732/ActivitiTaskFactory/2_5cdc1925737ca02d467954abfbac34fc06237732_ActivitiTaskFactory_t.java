 package com.activiti.bpmn.factories;
 import javax.xml.namespace.QName;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.oryxeditor.server.diagram.Shape;
 
 import com.activiti.bpmn.elements.ActivitiExtensionFieldElement;
 import com.activiti.bpmn.elements.ActivitiMultiInstanceLoopCharacteristics;
 import com.activiti.bpmn.elements.ActivitiServiceTask;
 import com.activiti.bpmn.elements.ActivitiUserTask;
 
 import de.hpi.bpmn2_0.annotations.Property;
 import de.hpi.bpmn2_0.annotations.StencilId;
 import de.hpi.bpmn2_0.factory.node.TaskFactory;
 import de.hpi.bpmn2_0.model.FormalExpression;
 import de.hpi.bpmn2_0.model.activity.Activity;
 import de.hpi.bpmn2_0.model.activity.loop.LoopCharacteristics;
 import de.hpi.bpmn2_0.model.activity.misc.Operation;
 import de.hpi.bpmn2_0.model.activity.misc.ServiceImplementation;
 import de.hpi.bpmn2_0.model.activity.misc.UserTaskImplementation;
 import de.hpi.bpmn2_0.model.activity.type.UserTask;
 
 @StencilId("Task")
 public class ActivitiTaskFactory extends TaskFactory{
 
     @Override
     @Property(name = "tasktype", value = "Service")
     public ActivitiServiceTask createServiceTask(Shape shape) {
         ActivitiServiceTask task = new ActivitiServiceTask();
 
         task.setId(shape.getResourceId());
         task.setName(shape.getProperty("name"));
         
         String implementation = shape.getProperty("implementation");
         if(implementation != null && !(implementation.length() == 0))
             task.setImplementation(ServiceImplementation.fromValue(implementation));
         
         /* Define Operation of the service task */
         String operationString = shape.getProperty("operationref");
         if(operationString != null && !(operationString.length() == 0)) {
             Operation operation = new Operation();
             operation.setId(operationString);
             task.setOperationRef(new QName(operationString));
         }
 
         String activitiType = shape.getProperty("activiti:type");
         if (activitiType!=null && !activitiType.isEmpty() && !activitiType.equals("None")) {
             task.setActivitiType(new QName(activitiType));
         }
 
         String activitField = shape.getProperty("activiti:field");
         if (activitField!=null && !activitField.isEmpty() ) {
                 try {
                     JSONObject modelJSON = new JSONObject(activitField);
                     int count = modelJSON.getInt("totalCount");
                     JSONArray arr = modelJSON.getJSONArray("items");
                     for (int i=0;i<count;i++) {
                         ActivitiExtensionFieldElement e = new ActivitiExtensionFieldElement();
                         JSONObject o = arr.getJSONObject(i);
                         e.getExtensionAttributes().put(new QName("name"), o.getString("name"));
                         e.getExtensionAttributes().put(new QName("expression"), o.getString("expression"));
                         task.getOrCreateExtensionElements().add(e);
                     }
                 } catch (JSONException e) {
                     throw new RuntimeException(e);
                 }
             
         }
         
         return task;
     }
     
     @Override
     @Property(name = "tasktype", value = "User")
     public UserTask createUserTask(Shape shape) {
         ActivitiUserTask task = new ActivitiUserTask();
 
         task.setId(shape.getResourceId());
         task.setName(shape.getProperty("name"));
 
         /* Set implementation property */
         String implementation = shape.getProperty("implementation");
         if (implementation != null) {
             task.setImplementation(UserTaskImplementation
                     .fromValue(implementation));
         }
 
         String activitiAssignee = shape.getProperty("activiti:assignee");
         if (activitiAssignee!=null && !activitiAssignee.isEmpty()) {
             task.setActivitiAssignee(new QName(activitiAssignee));
         }
 
         String activitiCandidateGroups = shape.getProperty("activiti:candidategroups");
         if (activitiCandidateGroups!=null && !activitiCandidateGroups.isEmpty()) {
             task.setActivitiCandidateGroups(new QName(activitiCandidateGroups));
         }
         
         String activitFormProperty = shape.getProperty("activiti:formproperty");
         if (activitFormProperty!=null && !activitFormProperty.isEmpty() ) {
              FormPropertiesUtil.processFormProperties(task, activitFormProperty);    
         }      
         
         return task;
     }
 
     /**
      * Entry method to create the {@link LoopCharacteristics} for a given 
      * activity's shape.
      * 
      * @param activity
      * @param shape
      * @return
      * 
      */
     @Override
     protected void createLoopCharacteristics(Activity activity, Shape shape) {
 
         /* Distinguish between standard and multiple instance loop types */
         String loopType = shape.getProperty("looptype");
         if (loopType != null && !(loopType.length() == 0)) {
 
             if (loopType.equalsIgnoreCase("Parallel")
                     || loopType.equalsIgnoreCase("Sequential")) {
                 activity.setLoopCharacteristics(createMultiInstanceLoopCharacteristics(shape, loopType));
             }
         }
     }
     
     
     /**
      * Creates the loop characteristics for multiple instances loops.
      * 
      * @param shape
      * @param loopType
      */
     private ActivitiMultiInstanceLoopCharacteristics createMultiInstanceLoopCharacteristics(Shape shape,
             String loopType) {
         ActivitiMultiInstanceLoopCharacteristics miLoop = new ActivitiMultiInstanceLoopCharacteristics();
 
         /* Determine whether it is parallel or sequential */
         if (loopType.equalsIgnoreCase("Parallel"))
             miLoop.setIsSequential(false);
         else
             miLoop.setIsSequential(true);
 
         /* Set loop cardinality */
         String loopCardinalityString = shape
                 .getProperty("loopcardinality");
         if (loopCardinalityString != null && !(loopCardinalityString.length() == 0)) {
             FormalExpression loopCardinality = new FormalExpression(
                     loopCardinalityString);
             miLoop.setLoopCardinality(loopCardinality);
         }
 
         String loopDataInput = shape.getProperty("loopdatainput");
         if (loopDataInput != null && !loopDataInput.isEmpty()) {
             miLoop.setActivitiCollection(loopDataInput);
         }
 
         String inputDataItem = shape.getProperty("inputdataitem");
         if (inputDataItem != null && !inputDataItem.isEmpty()) {
             miLoop.setActivitiElementVariable(inputDataItem);
         }
         
         /* Completion condition */
         String completionCondition = shape
                 .getProperty("completioncondition");
         if (completionCondition != null
                 && !(completionCondition.length() == 0)) {
             FormalExpression completionConditionExpr = new FormalExpression(
                     completionCondition);
             miLoop.setCompletionCondition(completionConditionExpr);
         }
 
         /* Handle loop behavior */
 //        handleLoopBehaviorAttributes(shape, miLoop);
         
         return miLoop;
     }
     
 }
