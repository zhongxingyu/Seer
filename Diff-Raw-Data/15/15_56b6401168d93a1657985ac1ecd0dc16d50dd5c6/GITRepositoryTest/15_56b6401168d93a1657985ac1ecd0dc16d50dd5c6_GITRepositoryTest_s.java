 package org.jabox.scm.git;
 
import junit.framework.Assert;
 import junit.framework.TestCase;
 
 public class GITRepositoryTest extends TestCase {
 
	public void testIsInitialized() {
		Assert.assertFalse(GITRepository.isInitialized());

	}

 	public void testInitialize() {
 		GITRepository.initialize();
 	}
 
	public void testGetSubversionBaseDir() {
		fail("Not yet implemented");
 	}
 
 }
