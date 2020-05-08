 package org.lilian.util.ranges;
 
 import static org.junit.Assert.*;
 import static org.lilian.util.Series.ranges;
 import static org.lilian.util.Series.series;
 
 import org.junit.Test;
 
 public class BoundedLinearRangeTest
 {
 
 	@Test
 	public void testBoundedLinearRangeSetTest()
 	{
 		RangeSet set;
 		set = new BoundedLinearRangeSet(-1.5, 0.5, 1.5);
 		
 		assertEquals(new Range(0.5, 1.0), set.first(0.6));
 		assertEquals(new Range(-1.0, -0.5), set.first(-0.6));
 		assertEquals(new Range(-1.5, -1.0), set.first(-1.5));		
 		
 		assertNull(set.first(1.6));
 		assertNull(set.first(-1.6));		
 		assertEquals(new Range(-1.5, -1.0), set.first(-1.5));
 
 		set = new BoundedLinearRangeSet(-1.5, 0.5, 1.5, true);
 		
 		assertEquals(new Range(0.5, 1.0), set.first(0.6));
 		assertEquals(new Range(-1.0, -0.5), set.first(-0.6));
 		assertEquals(new Range(-1.5, -1.0), set.first(-1.5));		
 
 		assertEquals(new Range(1.5, Double.POSITIVE_INFINITY), set.first( 1.6));
 		assertEquals(new Range(Double.NEGATIVE_INFINITY, -1.5), set.first(-1.6));		
 		assertEquals(new Range(-1.5, -1.0), set.first(-1.5));
 	}
 	
	
 	public void testRanges()
 	{
 		RangeSet ranges = new BoundedLinearRangeSet(-1.0, 0.3, 1.0, true);
 		
 		for(Range range : ranges(series(-1.0, 0.3, 1.0)) )
 		{
 			Range range2 = ranges.first(range.center());
 			assertEquals(
 					(Double.doubleToLongBits(range.to()) - Double.doubleToLongBits(range2.to())) + " ",
 					range, range2 );
 		}
 	}
 	
	 
 	public void quick() 
 	{
 		assertEquals(-1.0 + (2.0 + 1.0) * 0.3, -1.0 + 0.3 + 0.3 + 0.3, 0.0);
 	}		
 
 }
