 package com.ezgliding.igc;
 
 import org.junit.Before;
 import org.junit.Test;
 import static org.junit.Assert.assertEquals;
 
 public class OptimizerTest {
 
 	@Before
 	public void setUp() {
 
 	}
 
 	@Test
 	public void testToKml() {
 		Fix[] fixes = new Fix[] {
			new Fix(0, 45.664, 108.766, 1001, 1010, 'V'),	
			new Fix(0, 45.264, 108.966, 1201, 1210, 'V'),	
			new Fix(0, 44.964, 109.766, 1301, 1310, 'V')
 		};
 		Optimizer.Result result = new BrokenLineOptimizer(new Flight(), 3).new Result(fixes);
 		assertEquals(
 			"<LineString><coordinates>108.766,45.664,1001 108.966,45.264,1201 109.766,44.964,1301 </coordinates></LineString>",
 			result.toKml());
 	}
 }
