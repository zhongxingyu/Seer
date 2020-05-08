 package aima.test.core.unit.learning.learners;
 
 import aima.core.learning.framework.DataSet;
 import aima.core.learning.learners.AdaBoostLearner;
 import aima.core.learning.learners.StumpLearner;
 import aima.test.core.unit.learning.framework.DataSetTest;
 import java.io.IOException;
 import org.junit.Assert;
 import org.junit.Test;
 
 /**
  * AdaBoostLearnerTest
 *
  * @author Andrew Brown
  */
 public class AdaBoostLearnerTest {
 
     /**
      * Test ADABOOST with parameters described on page 750, AIMAv3; uses five
     * decision stumps (StumpLearner) to train on the twelve restaurant
      * examples.
      */
     @Test
     public void trainWithDecisionStump() {
         // get restaurant data
         DataSet restaurantData = null;
         try {
             restaurantData = DataSetTest.loadRestaurantData();
         } catch (IOException e) {
             Assert.fail("Could not load restaurant data from URL.");
         }
         // setup ADABOOST learner
         int ensembleSize = 5;
         AdaBoostLearner learner = new AdaBoostLearner(StumpLearner.class, ensembleSize);
         // train
         learner.train(restaurantData);
         // test
         int[] results = learner.test(restaurantData);
        double proportionCorrect = (double) results[0] / restaurantData.size();
        System.out.println("ADABOOST Accuracy: " + proportionCorrect);
         Assert.assertTrue(proportionCorrect > 0.95);
     }
 }
