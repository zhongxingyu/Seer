 package ling572;
 
 import java.util.*;
 import java.io.*;
 
 import static org.junit.Assert.*;
 
 import ling572.util.Instance;
 import ling572.util.SVMLightReader;
 
 
 import org.junit.Test;
 
 public class SVMLightReaderTest {
 	List<Instance> instances;
 	Instance instance17;
 	
 	{
		String location = "examples\\ex\\test.txt";
 		File testFile = new File(location);
 		this.instances = SVMLightReader.indexInstances(testFile);
 		this.instance17 = this.instances.get(17);
 	}
 	
 	@Test
 	public void testInstanceName() {
 		assertEquals(this.instance17.getName(),"1-17-lower");
 	}
 	
 	@Test
 	public void testInstanceLabel() {
 		assertEquals(this.instance17.getLabel(), "JJR");
 	}
 	
 	@Test
 	public void testInstanceCount() {
 		assertEquals(this.instances.size(), 36);
 	}
 	
 	@Test
 	public void testFeatureCount() {
 		assertEquals(instance17.getSize(), 5);
 	}
 	
 	@Test
 	public void testContainsFeature() {
 		assertTrue(instance17.containsFeature("prev2W=comma"));
 	}
 	
 	@Test
 	public void testFeatureValue() {
 		assertEquals((int)instance17.getFeatureValue("prev2W=comma"), 1);
 	}
 }
