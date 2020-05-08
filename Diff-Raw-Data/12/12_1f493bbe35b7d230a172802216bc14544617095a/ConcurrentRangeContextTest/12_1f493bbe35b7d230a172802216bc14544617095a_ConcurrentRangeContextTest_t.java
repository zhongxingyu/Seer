 package com.narmnevis.range;
 
 import static org.junit.Assert.assertTrue;
 
 import org.junit.Test;
 
 public class ConcurrentRangeContextTest {
 
 	@Test
 	public void generate() throws Exception {
		Range r = new Range().withDataSpec("date", "date").withDataSpec("number", "range(1,10000)")
				.withDataSpec("item", "enum(1,2,3,4,5,6,7,8,9,10)").withSize(1000).withOutputFormat("csv")
				.withLocation("/tmp/test.csv");
 		Data data = r.generate();
 		assertTrue(data != null);
 	}
 
 }
