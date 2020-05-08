 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package etc.aloe.cscw2013;
 
 import etc.aloe.data.ExampleSet;
 import etc.aloe.data.FeatureSpecification;
 import etc.aloe.data.Message;
 import etc.aloe.data.Segment;
 import etc.aloe.data.SegmentSet;
 import java.util.Date;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import static org.junit.Assert.*;
 import weka.core.Attribute;
 import weka.core.Instances;
 import weka.core.SelectedTag;
 import weka.filters.Filter;
 import weka.filters.unsupervised.attribute.Add;
 import weka.filters.unsupervised.instance.RemoveWithValues;
 
 /**
  *
  * @author michael
  */
 public class FeatureExtractionImplTest {
 
     public FeatureExtractionImplTest() {
     }
 
     @BeforeClass
     public static void setUpClass() {
     }
 
     @AfterClass
     public static void tearDownClass() {
     }
 
     @Before
     public void setUp() {
     }
 
     @After
     public void tearDown() {
     }
 
     /**
      * Test of extractFeatures method, of class FeatureExtractionImpl.
      */
     @Test
     public void testExtractFeatures() {
         System.out.println("extractFeatures");
         SegmentSet segments = new SegmentSet();
 
         Segment seg0 = new Segment();
         seg0.add(new Message(0, new Date(), "Alice", "it's"));
         seg0.add(new Message(1, new Date(), "Bob", "cow"));
         seg0.add(new Message(2, new Date(), "Alice", "time"));
         seg0.setTrueLabel(Boolean.TRUE);
         segments.add(seg0);
 
         Segment seg1 = new Segment();
         seg1.add(new Message(3, new Date(), "Bob", "noooooooo"));
         seg1.setTrueLabel(Boolean.FALSE);
         segments.add(seg1);
 
         Segment seg2 = new Segment();
         seg2.add(new Message(4, new Date(), "Bob", "once"));
         seg2.add(new Message(5, new Date(), "Alice", "upon"));
         seg2.setTrueLabel(Boolean.FALSE);
         segments.add(seg2);
 
         Segment seg3 = new Segment();
         seg3.add(new Message(6, new Date(), "Bob", "a"));
         seg3.add(new Message(7, new Date(), "Alice", "time"));
         seg3.setTrueLabel(Boolean.TRUE);
         segments.add(seg3);
 
         Instances basicInstances = segments.getBasicExamples().getInstances();
 
         String attrName = "newAtt";
         Add addFilter = new Add();
         addFilter.setAttributeName(attrName);
         addFilter.setAttributeType(new SelectedTag(0, Add.TAGS_TYPE));
         try {
             addFilter.setInputFormat(basicInstances);
         } catch (Exception e) {
             assertTrue(e.getMessage(), false);
         }
 
         RemoveWithValues removeFilter = new RemoveWithValues();
         removeFilter.setAttributeIndex("3"); //the label attribute
         removeFilter.setNominalIndicesArr(new int[]{0}); //false
         try {
             removeFilter.setInputFormat(addFilter.getOutputFormat());
         } catch (Exception e) {
             assertTrue(e.getMessage(), false);
         }
 
         FeatureSpecification spec = new FeatureSpecification();
         spec.addFilter(addFilter);
         spec.addFilter(removeFilter);
 
         FeatureExtractionImpl instance = new FeatureExtractionImpl();
         ExampleSet examples = instance.extractFeatures(segments.getBasicExamples(), spec);
         assertNotNull(examples);
         assertNotNull(examples.getInstances());
 
         Instances instances = examples.getInstances();
        //3 base attrs + 4 basic features + 1 label
        assertEquals(8, instances.numAttributes());
         //Contains the added attribute in the right place
         assertEquals(attrName, instances.attribute(basicInstances.numAttributes()).name());
 
         // the middle 2 segments were removed
         assertEquals(2, instances.size());
 
         //The base attributes are present
         assertNotNull(instances.attribute(ExampleSet.MESSAGE_ATTR_NAME));
         assertNotNull(instances.attribute(ExampleSet.LABEL_ATTR_NAME));
         Attribute idAttr = instances.attribute(ExampleSet.ID_ATTR_NAME);
         assertNotNull(idAttr);
 
        //Basic features are present
        assertNotNull(instances.attribute(SegmentSet.CPS_ATTR_NAME));

         //The correct segments remain
         assertEquals(seg0.getId(), instances.get(0).value(idAttr), 0);
         assertEquals(seg3.getId(), instances.get(1).value(idAttr), 0);
     }
 }
