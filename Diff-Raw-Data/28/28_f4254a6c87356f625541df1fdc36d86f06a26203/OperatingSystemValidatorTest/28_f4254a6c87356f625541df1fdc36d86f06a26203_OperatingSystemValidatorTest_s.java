 package de.fhb.projects.Twitchess.controller.osvalidator;
 
import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 import org.junit.Test;
 
 public class OperatingSystemValidatorTest {
 
 	private OperatingSystemValidator osv = new OperatingSystemValidator();
 
	@Test 
 	public void testWindows() {
		String os = "win";
		assertTrue(osv.isWindows(os));
 	}

 }
