 package com.edinarobotics.utils.autonomous;
 
 /**
  * An autonomous step that will run multiple sub-steps in sequence.
  */
 public class SequentialAutonomousStepGroup extends AutonomousStep{
     private AutonomousStep[] steps;
     private int currentStep;
     
     /**
      * Constructs a SequentialAutonomousStepGroup that will run the given
      * steps in sequence.
      * @param steps the array of {@link AutonomousStep} objects to run in
      * sequence.
      */
     public SequentialAutonomousStepGroup(AutonomousStep[] steps){
         this.steps = steps;
         currentStep = -1;
     }
     
     /**
      * Runs the given steps in sequence.
      */
     public void run(){
         if(currentStep>=steps.length){
             //We're done, don't use too large an index.
             return;
         }
         if(currentStep == -1){
             //Initially, start the first step.
             currentStep++;
             steps[currentStep].start();
         }
         if(!steps[currentStep].isFinished()){
             //The current step's not done, run it.
             steps[currentStep].run();
         }
         else{
             //The current step's done, stop it.
             steps[currentStep].stop();
             currentStep++;
             //Start the next one if there is a next step.
             if(currentStep<steps.length){
                 steps[currentStep].start();
             }
         }
     }
     
     /**
      * Stops the current step if we are interrupted.
      */
     public void stop(){
        if(currentStep<steps.length && currentStep>0){
             steps[currentStep].stop();
         }
         //Reset the step group so that it can be reused.
         currentStep = -1;
     }
     
     /**
      * Returns {@code true} if all the steps have finished running.
      * @return {@code true} if all steps have run and been stopped,
      * {@code false} otherwise.
      */
     public boolean isFinished(){
         return currentStep>=steps.length;
     }
 }
