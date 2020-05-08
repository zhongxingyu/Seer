 package com.bigvisible.kanbansimulator;
 
 public class IterationParameter {
 
     private String workflowStepName;
     private int iteration;
     private Integer capacity;
 
     public String getWorkflowStepName() {
         return workflowStepName;
     }
 
     public int getIteration() {
         return iteration;
     }
 
     public Integer getCapacity() {
         return capacity;
     }
 
     public void setIteration(int iteration) {
         this.iteration = iteration;
     }
 
     public IterationParameter forStep(String workflowStepName) {
         this.workflowStepName = workflowStepName;
         return this;
     }
 
     public IterationParameter setCapacity(Integer capacity) {
         this.capacity = capacity;
         return this;
     }
 
     public static IterationParameter startingAt(int iteration) {
         IterationParameter newParameter = new IterationParameter();
         newParameter.setIteration(iteration);
         return newParameter;
     }
 
     public IterationParameter setBatchSize(int batchSize) {
        // TODO Auto-generated method stub
         return this;
     }
 
     public boolean isWorkflowConfiguration() {
         return workflowStepName != null;
     }
 
 }
