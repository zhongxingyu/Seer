 package com.github.tteofili.nlputils;
 
 import org.junit.Test;
 
 /**
  * Testcase for {@link GradientDescentUtils}
  */
 public class GradientDescentUtilsTest {
 
     @Test
     public void testConvergence() throws Exception {
         TrainingSet trainingSet = new TrainingSet();
         TestUtils.fillTrainingSet(trainingSet, 100, 5);
        GradientDescentUtils.batchGradientDescent(new LinearCombinationHypothesis(), trainingSet, 0.00002);
     }
 
 }
