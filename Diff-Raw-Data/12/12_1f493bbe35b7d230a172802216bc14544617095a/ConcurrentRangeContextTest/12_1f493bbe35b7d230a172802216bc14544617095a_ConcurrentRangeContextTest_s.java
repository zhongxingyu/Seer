 package com.narmnevis.range;
 
 import static org.junit.Assert.assertTrue;
 
 import org.junit.Test;
 
 public class ConcurrentRangeContextTest {
 
 	@Test
 	public void generate() throws Exception {
		Range r = new Range().withDataSpec("date", "date").withDataSpec("number", "range(1,10000)").withSize(500)
				.withOutputFormat("csv").withLocation("/tmp/test.csv");
 		Data data = r.generate();
 		assertTrue(data != null);
 	}
 
 }
