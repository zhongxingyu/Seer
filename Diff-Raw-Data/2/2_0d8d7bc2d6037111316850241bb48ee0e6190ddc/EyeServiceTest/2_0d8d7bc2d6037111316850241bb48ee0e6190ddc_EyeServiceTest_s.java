 package ch.busyboxes.redwatcher.service;
 
 import static org.junit.Assert.assertTrue;
 
 import java.io.IOException;
 
 import org.junit.Ignore;
 import org.junit.Test;
 
 public class EyeServiceTest {
 
 	private EyeService eyeService = new EyeService();
 
 	@Ignore
 	@Test
 	public void testTestSnmp() throws IOException {
 
		boolean result = eyeService.testSnmp("twinhead.busyboxes.ch");
 
 		assertTrue(result);
 	}
 
 }
