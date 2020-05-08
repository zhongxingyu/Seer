 package mesquite.treecmp.WeightedMatchingSplitMetric;
 
 import static org.junit.Assert.assertEquals;
 import mesquite.treecmp.MetricTest;
 
 import org.junit.Test;
 
 public class WhenCalculatingMatchingSplitDistanceTest extends MetricTest<WeightedMatchingSplitMetric> {
 	@Test public void itShouldCalculateDistanceForTwoTreesFromBogdGiaroPaper() {
 		final String t1 = "((a,b,c,d):10,e,(f,g,h,i,j));";
 		final String t2 = "((a,b,c,d,e):10,f,(g,h,i,j));";
 		
 		assertEquals(11., getMetricValue(t1, t2), 1e-6);
 	}
 	
 	@Test public void itShouldCalculateDistanceForOtherTwoTreesFromBogdGiaroPaper() {
 		final String t1 = "((a,b,c,d):10,e,f,g,h,i,j);";
 		final String t2 = "((a,b,c,d,e):9,f,(g,h,i,j));";
 		
 		assertEquals(17., getMetricValue(t1, t2), 1e-6);
 	}
 
 	@Override
 	protected WeightedMatchingSplitMetric createMetric() {
 		return new WeightedMatchingSplitMetric();
 	}
 }
