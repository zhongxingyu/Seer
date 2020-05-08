 package com.bigvisible.kanbansimulatortester.core.unit;
 
 import static org.junit.Assert.*;
 import static org.junit.Assert.assertEquals;
 
 import org.junit.Test;
 
 import com.bigvisible.kanbansimulator.InvalidSimulatorConfiguration;
 import com.bigvisible.kanbansimulator.IterationParameter;
 import com.bigvisible.kanbansimulator.IterationResult;
 import com.bigvisible.kanbansimulator.Simulator;
 import com.bigvisible.kanbansimulator.SimulatorEngine;
 
 public class StimulatorTest {
 
     @Test
     public void simulator_runs_until_all_stories_are_finished() {
         Simulator stimuator = new SimulatorEngine();
         stimuator.addStories(88);
         stimuator.run(null);
 
         assertEquals(88, stimuator.getStoriesCompleted());
     }
 
     @Test
     public void when_capacity_is_modified_for_a_workflow_step_at_a_given_iteration_THEN_the_amount_of_work_completed_matches_that_modification_for_that_iteration()
             throws Exception {
         // how's that for a test method name?!?!?!?!
 
         Simulator stimulator = new SimulatorEngine();
         int initialBACapacity = 4;
         int greaterThanBACapacity = 10;
         int increaseInBACapacity = 2;
 
         stimulator.addStories(greaterThanBACapacity);
         stimulator.setBatchSize(greaterThanBACapacity);
         stimulator.setBusinessAnalystCapacity(initialBACapacity);
         stimulator.addParameter(IterationParameter.startingAt(2).forStep("BA")
                 .setCapacity(initialBACapacity + increaseInBACapacity));
 
         stimulator.run(null);
 
         IterationResult firstIteration = stimulator.results().get(0);
         IterationResult secondIteration = stimulator.results().get(1);
 
         assertEquals("Increase in work completed should match increase in capacity.", increaseInBACapacity,
                 secondIteration.getCompleted("BA") - firstIteration.getCompleted("BA"));
     }
     
     @Test(expected=InvalidSimulatorConfiguration.class)
     public void when_a_parameter_is_added_for_a_non_existent_workflow_step_THEN_that_configuration_is_rejected() throws Exception {
         Simulator stimulator = new SimulatorEngine();
         stimulator.addStories(1);
         
         stimulator.addParameter(IterationParameter.startingAt(1).forStep("InvalidWorkflowStepName").setCapacity(10));
         stimulator.run(null);
     }
     
     @Test
     public void when_the_batch_size_is_configured_at_a_given_iteration_THEN_the_amount_put_into_play_matches_that_configuration() throws Exception {
         Simulator stimulator = new SimulatorEngine();
         stimulator.addStories(20);
         stimulator.setBatchSize(1);
         
         stimulator.addParameter(IterationParameter.startingAt(2).setBatchSize(10));
         
         stimulator.run(null);
         
         assertEquals(10, stimulator.results().get(1).getBatchSize());
     }
     
     public void when_batch_size_exceeds_number_of_stories_available_to_play_THEN_only_the_number_of_stories_available_are_actually_put_into_play() throws Exception {
         int totalStories = 10;
         int storiesPutInPlayForIteration1 = 1;
         int expectedStoriesPutInPlayForIteration2 = totalStories - storiesPutInPlayForIteration1;
         int batchSizeForIteration2 = expectedStoriesPutInPlayForIteration2 + 1;  // i.e. batchSize > putInPlay
         
         Simulator stimulator = new SimulatorEngine();
         stimulator.addStories(totalStories);
         stimulator.setBatchSize(storiesPutInPlayForIteration1);
         
         stimulator.addParameter(IterationParameter.startingAt(2).setBatchSize(batchSizeForIteration2));
         
         stimulator.run(null);
         
         int storiesPutInPlayForIteration2 = stimulator.results().get(1).getPutIntoPlay();
         
         assertEquals(expectedStoriesPutInPlayForIteration2, storiesPutInPlayForIteration2);
     }
 }
