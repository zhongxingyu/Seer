 package org.tb.storage.neo.domain;
 
import org.testng.Assert;
 import org.testng.annotations.Parameters;
 import org.testng.annotations.Test;
 
 @Test
 public class TechnologyTest {
 
 	@Test
	@Parameters({"technology.name"})
 	public void setAndGetAttributes(String name) {
		Assert.assertTrue(true);
 	}
 	
 }
