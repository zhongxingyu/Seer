 package org.levi.engine.runtime;
 
 import org.apache.ode.jacob.vpu.ExecutionQueueImpl;
 import org.apache.ode.jacob.vpu.JacobVPU;
 import org.levi.engine.LeviException;
 import org.levi.engine.bpmn.BPMNJacobRunnable;
 import org.levi.engine.bpmn.RunnableFlowNode;
 import org.levi.engine.db.DBManager;
 import org.levi.engine.impl.bpmn.FlowNodeFactory;
 import org.levi.engine.impl.bpmn.WaitedTask;
 import org.levi.engine.impl.bpmn.parser.ProcessDefinition;
 import org.levi.engine.impl.db.DBManagerImpl;
 import org.levi.engine.persistence.hibernate.process.hobj.TaskBean;
 import org.levi.engine.utils.LeviUtils;
 import org.omg.spec.bpmn.x20100524.model.TSequenceFlow;
 
 import javax.mail.MessagingException;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 public class ProcessInstance extends BPMNJacobRunnable {
     private ProcessDefinition processDefinition;
     private boolean isRunning;
     private ExecutionQueueImpl soup;
     private JacobVPU vpu;
     private FlowNodeFactory flowNodeFac;
     private Map<String, Object> variables;
     private Map<String, WaitedTask> waitedTasks;
     private List<String> runningTaskIds;
     private List<String> completedTaskIds;
     private String processId;
     private String processDefId;
     private List<String> pauseSignals;
     private List<String> resumeSignals;
     private boolean hasStartForm;
     private String startUserId;
 
     private DBManager dbManager = new DBManagerImpl();
 
     public ProcessInstance(ProcessDefinition processDefinition, Map<String, Object> variables) {
         if (processDefinition == null) {
             throw new LeviException("Cannot create a process instance. Process definition is null.");
         }
         this.processDefinition = processDefinition;
         if (variables == null) {
             this.variables = LeviUtils.newHashMap();
         } else {
             this.variables = variables;
         }
         flowNodeFac = new FlowNodeFactory(this.processDefinition, this);
         // todo; is the following processId ok?
         processDefId = processDefinition.getDefinitionsName();
         processId = String.valueOf(this.hashCode());
         waitedTasks = LeviUtils.newHashMap();
         runningTaskIds = LeviUtils.newArrayList();
         completedTaskIds = LeviUtils.newArrayList();
         pauseSignals = LeviUtils.newArrayList();
         resumeSignals = LeviUtils.newArrayList();
         setIsRunning(false);
         hasStartForm = false;
     }
 
     public void claim(String uid, String itemId) {
         if (itemId.equals(processDefinition.getStartEvent().getId())) {
             setStartUserId(uid);
         }
     }
 
     public String getStartUserId() {
         return startUserId;
     }
 
     public void setStartUserId(String startUserId) {
         this.startUserId = startUserId;
     }
 
     // this is used by the ProcessInstanceManager class
     public static class Builder {
         private ProcessDefinition processDefinition;
         private Map<String, Object> variables;
         private Map<String, WaitedTask> waitedTasks;
         private List<String> runningElemIds;
         private List<String> completedElemIds;
         private String processId;
 
         public Builder(ProcessDefinition processDefinition) {
             this.processDefinition = processDefinition;
         }
 
         public Builder variables(Map<String, Object> variables) {
             if (variables != null) {
                 this.variables = variables;
             } else {
                 this.variables = LeviUtils.newHashMap();
             }
             return this;
         }
 
         public Builder waitedTasks(Map<String, WaitedTask> waitedTasks) {
             this.waitedTasks = waitedTasks;
             return this;
         }
 
         public Builder runningIds(List<String> ids) {
             runningElemIds = ids;
             return this;
         }
 
         public Builder completedIds(List<String> ids) {
             completedElemIds = ids;
             return this;
         }
 
         public Builder processId(String id) {
             processId = id;
             return this;
         }
 
         public ProcessInstance build() {
             return new ProcessInstance(this);
         }
     }
 
     private ProcessInstance(Builder builder) {
         processDefinition = builder.processDefinition;
         setIsRunning(false);
         flowNodeFac = new FlowNodeFactory(processDefinition, this);
         variables = builder.variables;
         waitedTasks = builder.waitedTasks;
         runningTaskIds = builder.runningElemIds;
         completedTaskIds = builder.completedElemIds;
         processDefId = processDefinition.getDefinitionsId();
         if (builder.processId == null) {
             throw new IllegalArgumentException("Process Instance ID is null.");
         }
         processId = builder.processId;
         pauseSignals = LeviUtils.newArrayList();
         resumeSignals = LeviUtils.newArrayList();
         hasStartForm = false;
     }
 
     public String getProcessId() {
         return processId;
     }
 
     public String getDefinitionsId() {
         if (processDefId == null) {
             throw new NullPointerException("ProcessDefID is null.");
         }
         return processDefId;
     }
 
     public void execute() {
         soup = new ExecutionQueueImpl(null);
         vpu = new JacobVPU();
         vpu.setContext(soup);
         vpu.inject(this);
         while (vpu.execute()) {
         }
     }
 
     public void run() {
         synchronized (runningTaskIds) {
             if (runningTaskIds.isEmpty()) {
                 RunnableFlowNode startEvent = flowNodeFac.getStartEvent();
                 if (startEvent.hasInputForm()) {
                     pause(startEvent.getId());
                 } else {
                     instance(startEvent);
                 }
             } else {
                 // todo check if all the
                 //for (String id : (ArrayList<String>) runningTaskIds.clone()) {
                 if (resumeSignals.size() > 1) {
                     throw new RuntimeException("More than one resume signals found.");
                 }
                 try {
                     flowNodeFac.getNextNode(resumeSignals.get(0)).resumeTask();
                 } catch (MessagingException e) {
                     e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                 }
                 //instance(node);
                 //soup.enqueueReaction(new Continuation(node, m, null));
                 //vpu.addReaction(node, m, null, "description");
                 //node.resumeTask();
                 //}
             }
         }
         setIsRunning(true);
     }
 
     public Map<String, Object> getVariables() {
         variables = LeviUtils.newHashMap();
        if (this.getProcessId() == null) {
            return LeviUtils.newHashMap();
        }
         Map<String, String> processVariables = dbManager.getVariables(this.getProcessId());
         if (processVariables != null) {
             for (String key : processVariables.keySet()) {
                 variables.put(key, processVariables.get(key));
             }
         }
         return variables;
     }
 
     public Object getVariable(String name) {
         if (name == null) {
             throw new LeviException("Variable name is null.");
         }
         variables = LeviUtils.newHashMap();
         Map<String, String> processVariables = dbManager.getVariables(this.getProcessId());
         if (processVariables != null) {
             for (String key : processVariables.keySet()) {
                 variables.put(key, processVariables.get(key));
             }
         }
         return variables.get(name);
     }
 
     public Object setVariable(String name, Object value) {
         if (name == null) {
             throw new LeviException("Cannot save a variable with a null name");
         }
         if (variables == null) {
             throw new NullPointerException("Process variables map is null.");
         }
         Object output = variables.put(name, value);
         Map<String, String> processVariables = LeviUtils.newHashMap();
         for (String key : this.variables.keySet()) {
             processVariables.put(key, this.variables.get(key).toString());
         }
         dbManager.setVariables(this.getProcessId(), processVariables);
         return output;
     }
 
     public void setVariables(Map<String, Object> variables) {
         if (variables == null) {
             throw new NullPointerException("Variables map is null.");
         }
         this.variables.putAll(variables);
         Map<String, String> processVariables = LeviUtils.newHashMap();
         for (String key : this.variables.keySet()) {
             processVariables.put(key, this.variables.get(key).toString());
         }
         dbManager.setVariables(this.getProcessId(), processVariables);
 
     }
 
     public RunnableFlowNode executeNext(RunnableFlowNode currentFlowNode) {
         return flowNodeFac.getNextNode(currentFlowNode);
     }
 
     public RunnableFlowNode executeNext(TSequenceFlow currentSeqFlow) {
         return flowNodeFac.getNextNode(currentSeqFlow);
     }
 
     public ProcessDefinition getObjectModel() {
         return processDefinition;
     }
 
     // TODO
     public void continueUserTask(String userTaskId, Map<String, Object> variables) {
         if (userTaskId == null) {
             throw new NullPointerException("User task id is null.");
         }
         waitedTasks.get(userTaskId).resume(variables);
     }
 
     public void addWaitedTask(String id, WaitedTask waitedTask) {
         if (id == null) {
             throw new NullPointerException("Task ID is null.");
         }
         waitedTasks.put(id, waitedTask);
     }
 
     public void addRunning(String id) {
         synchronized (runningTaskIds) {
             if (!runningTaskIds.contains(id)) {
                 runningTaskIds.add(id);
             }
 
         }
     }
 
     public void addCompleted(String taskId) {
         synchronized (runningTaskIds) {
             if (!runningTaskIds.contains(taskId)) {
                 throw new LeviException("No running element found for the processId " + taskId);
             }
             runningTaskIds.remove(taskId);
             dbManager.removeRunningTask(taskId);
         }
         synchronized (completedTaskIds) {
             completedTaskIds.add(taskId);
             dbManager.addCompletedTask(taskId);
         }
         dbManager.unassignTask(taskId);
 
     }
 
     public synchronized List<String> getRunningTaskIds() {
         return runningTaskIds;
     }
 
     public synchronized List<String> getCompletedTaskIds() {
         return completedTaskIds;
     }
 
     public void pause(String taskId) {
         // todo check for the # runningTaskIds before actually pausing.
         if (checkPauseSignal(taskId)) {
             setIsRunning(false);
             System.out.println("Running  :   " + runningTaskIds.toString());
             System.out.println("Completed: " + completedTaskIds.toString());
             //System.out.println("variables: " + variables.toString());
             //resume();
             while (soup.hasReactions()) {
                 System.out.println("Removing reactions from the soup.");
                 soup.dequeueReaction();
             }
             if (soup.isComplete()) {
                 TaskBean taskBeanObj;
                 for (String id : runningTaskIds) {
                     dbManager.addRunningTask(id);
                 }
                 for (String id : completedTaskIds) {
                     dbManager.addCompletedTask(id);
                 }
                 System.out.println("Writing the processs to the database.");
             } else {
                 throw new RuntimeException("Incomplete Runtime soup. Cannot pause the process instance.");
             }
             addRunning(taskId);
         }
     }
 
     private boolean checkPauseSignal(String taskId) {
         if (taskId == null) {
             throw new NullPointerException("TaskId is null.");
         }
         synchronized (pauseSignals) {
             if (!pauseSignals.contains(taskId)) {
                 pauseSignals.add(taskId);
             }
             synchronized (runningTaskIds) {
                 if (pauseSignals.size() == runningTaskIds.size()) {
                     return pauseSignals.containsAll(runningTaskIds);
                 }
             }
         }
         return false;
     }
 
     private boolean checkResumeSignal(String taskId) {
         if (taskId == null) {
             throw new NullPointerException("TaskId is null.");
         }
         synchronized (resumeSignals) {
             if (!resumeSignals.contains(taskId)) {
                 resumeSignals.add(taskId);
             }
             synchronized (runningTaskIds) {
                 if (runningTaskIds.contains(taskId)) {
                     return true;
                 }
             }
         }
         return false;
     }
 
     public void resume() {
         System.out.println("Retrieved process data from the database.");
         execute();
         //setIsRunning(true);
     }
 
     public void resume(String taskId) {
         System.out.println("Retrieved process data from the database.");
         if (checkResumeSignal(taskId)) {
             if (isRunning()) {
                 try {
                     flowNodeFac.getNextNode(taskId).resumeTask();
                 } catch (MessagingException e) {
                     e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                 }
             } else {
                 execute();
             }
         }
         //setIsRunning(true);
     }
 
     private synchronized boolean isRunning() {
         return isRunning;
     }
 
     private synchronized void setIsRunning(boolean value) {
         isRunning = value;
     }
 
     public boolean hasStartForm() {
         return hasStartForm;
     }
 }
