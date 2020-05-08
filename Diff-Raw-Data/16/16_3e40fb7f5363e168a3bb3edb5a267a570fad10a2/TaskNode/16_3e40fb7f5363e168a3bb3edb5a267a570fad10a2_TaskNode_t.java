 package org.levi.engine.impl.bpmn;
 
 
 
 import org.levi.engine.bpmn.Activity;
 import org.omg.spec.bpmn.x20100524.model.TTask;
 
 public class TaskNode extends Activity {
     TTask task;
     FlowNodeFactory flowNodeFac;
 
     public TaskNode(TTask task, FlowNodeFactory flowNodeFac) {
         super();
         // TODO
         assert task != null;
         assert flowNodeFac != null;
         this.task = task;
         this.flowNodeFac = flowNodeFac;
     }
 
     public void run() {
         System.out.print(this.getClassName());
         System.out.println( ": " + task.getName());
         //instance(flowNodeFac.createNextNode());
         instance(flowNodeFac.getNextNode(this));
     }
 
     public String getId() {
         return task.getId();
     }

    public void resumeTask(){
        
    }
 }
