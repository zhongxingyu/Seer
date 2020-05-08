 package aima.test.core.unit.learning.inductive;
 
 import aima.core.learning.framework.Attribute;
 import aima.core.learning.framework.DataSet;
 import aima.core.learning.framework.Example;
 import aima.core.learning.inductive.DecisionList;
 import aima.test.core.unit.learning.framework.DataSetTest;
 import java.io.IOException;
 import org.junit.Assert;
 import org.junit.Test;
 
 /**
  * DecisionListTest
  * @author Andrew Brown
  */
 public class DecisionListTest {
 
     /**
      * Test default prediction; should be null
      */
     @Test
     public void testDefaultPrediction() {
         DecisionList dl = new DecisionList();
         Example<Integer> e = new Example();
         e.add(new Attribute<Integer>("Test", 10));
         e.setOutput(20);
         // test
         Assert.assertEquals(null, dl.predict(e));
     }
 
     /**
      * Test predict(); a test will return it's output value if it matches the
      * example; null otherwise
      */
     @Test
     public void testPredict() {
         // get restaurant data
        DataSet restaurant = null;
         try{
            DataSetTest.loadRestaurantData();
         }
         catch(IOException e){
             Assert.fail("Could not load restaurant data from URL.");
         }
         // create decision list test on two attributes: Price and Est
         aima.core.learning.inductive.DecisionListTest<String> t = new aima.core.learning.inductive.DecisionListTest();
         t.add(new Attribute<String>("Price", "$$$"));
         t.add(new Attribute<String>("Est", ">60"));
         t.setOutput("No");
         // create decision list
         DecisionList dl = new DecisionList();
         dl.add(t);
         // test fall-through
        Assert.assertEquals(null, dl.predict(restaurant.getExample(0)));
         // test matching
        Assert.assertEquals("No", dl.predict(restaurant.getExample(4)));
     }
     
     /**
      * Demonstrate use of mergeWith()
      */
     @Test
     public void testMergeWith() {
         // create decision list tests
         aima.core.learning.inductive.DecisionListTest<String> t1 = new aima.core.learning.inductive.DecisionListTest();
         aima.core.learning.inductive.DecisionListTest<String> t2 = new aima.core.learning.inductive.DecisionListTest();
         aima.core.learning.inductive.DecisionListTest<String> t3 = new aima.core.learning.inductive.DecisionListTest();
         // create decision lists
         DecisionList dl1 = new DecisionList();
         dl1.add(t1);
         DecisionList dl2 = new DecisionList();
         dl2.add(t2);
         DecisionList dl3 = new DecisionList();
         dl3.add(t3);
         // merge
         DecisionList merged = dl1.mergeWith(dl2.mergeWith(dl3));
         Assert.assertEquals(3, merged.size());
     }
 
     /**
      * Demonstrate use of toString()
      */
     @Test
     public void testToString() {
         // create decision list test on two attributes: Price and Est
         aima.core.learning.inductive.DecisionListTest<String> t = new aima.core.learning.inductive.DecisionListTest();
         t.add(new Attribute<String>("Price", "$$$"));
         t.add(new Attribute<String>("Est", ">60"));
         t.setOutput("No");
         // create decision list
         DecisionList dl = new DecisionList();
         dl.add(t);
         // test
         Assert.assertEquals("IF [Price=$$$ AND Est=>60] THEN No ELSE null", dl.toString());
     }
 }
