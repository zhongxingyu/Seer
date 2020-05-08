 package com.edinarobotics.utils.autonomous;
 
 /**
  * An autonomous step that allows multiple substeps to be run in parallel.
 * It simulates 
  */
 public class ParallelAutonomousStepGroup extends AutonomousStep{
     private AutonomousStep[] steps;
     private boolean[] stepsFinished;
     
     /**
      * Constructs a ParallelAutonomousStepGroup that will run the given steps
      * in parallel.
      * @param steps the steps to be run in parallel. 
      */
     public ParallelAutonomousStepGroup(AutonomousStep[] steps){
         this.steps = steps;
         this.stepsFinished = new boolean[steps.length];
     }
     
     /**
      * Calls {@link AutonomousStep#start()} on all the {@link AutonomousStep}
      * objects contained by this ParallelAutonomousStepGroup.
      */
     public void start(){
         for(int i=0; i<steps.length; i++){
             steps[i].start();
         }
     }
     
     /**
      * Calls {@link AutonomousStep#run()} on all steps that are not yet
      * finished. This simulates running them in parallel.
      */
     public void run(){
         for(int i=0; i<steps.length; i++){
             if(!steps[i].isFinished()){
                 steps[i].run();
             }
             if(steps[i].isFinished() && (!stepsFinished[i])){
                 //Call stop() on the step as soon as it's finished
                 steps[i].stop();
                 stepsFinished[i] = true;
             }
         }
     }
     
     /**
      * Calls {@link AutonomousStep#stop()} on all the steps in this
      * ParallelAutonomousStepGroup.
      */
     public void stop(){
         for(int i=0; i<steps.length; i++){
             if(!stepsFinished[i]){
                 //Stop all steps that have not been stopped
                 steps[i].stop();
             }
         }
         //Reset the steps array so that this step can be reused
         stepsFinished = new boolean[steps.length];
     }
     
     /**
      * Returns whether or not all steps in this in ParallelAutonomousStepGroup
      * are finished.
      * @return {@code true} if all steps have finished running, {@code false}
      * otherwise.
      */
     public boolean isFinished(){
         for(int i=0; i<steps.length; i++){
             if(!steps[i].isFinished()){
                 return false;
             }
         }
         return true;
     }
 }
