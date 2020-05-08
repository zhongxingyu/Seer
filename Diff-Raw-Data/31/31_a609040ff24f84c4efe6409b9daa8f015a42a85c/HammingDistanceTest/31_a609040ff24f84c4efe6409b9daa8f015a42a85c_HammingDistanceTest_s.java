 package org.lilian.util;
 
 import static org.junit.Assert.*;
 
 import java.util.Arrays;
 import java.util.List;
 
 import org.junit.Test;
 import org.lilian.util.distance.Distance;
 import org.lilian.util.distance.HammingDistance;
 
 public class HammingDistanceTest
 {
 
 	@Test
 	public void testDistance()
 	{
 		List<Integer> a = Arrays.asList(0, 1, 0, 0, 1),
                      b = Arrays.asList(1, 1, 1, 0, 0, 0, 1,0);
 
 		Distance<List<Integer>> distance = new HammingDistance<Integer>();
 
 		assertEquals(6.0, distance.distance(a, b), 0.0);
 	}
 
 }
