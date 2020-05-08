 package mesquite.treecomp.metrics.RFtreeDifference;
 
 import junit.framework.Assert;
 
 import mesquite.treecomp.metrics.MetricTestHelper;
 
 import org.junit.Before;
 import org.junit.Test;
 
 public class RFMetricTestCase {
 	MetricTestHelper metric;
 
 	@Before
 	public void setUp() throws Exception {
 		metric = new MetricTestHelper(new RFtreeDifference());
 	}
 	
 	@Test
	public void TestTreeWith5Taxa() {
		String t1 = "((1,2),3,(4,5));";
		String t2 = "((1,5),4,(2,3));";
 		
		double result = metric.getDistance(t1, t2);
		Assert.assertEquals(2., result);
	}
	
	@Test
	public void TestTreeWith6Taxa() {
		String t1 = "((t1,t6),t2,(t5,(t3,t4)));";
		String t2 = "((t1,t2),(t3,t4),(t5,t6));";
		
		double result = metric.getDistance(t1, t2);
		Assert.assertEquals(2., result);
 	}
 }
