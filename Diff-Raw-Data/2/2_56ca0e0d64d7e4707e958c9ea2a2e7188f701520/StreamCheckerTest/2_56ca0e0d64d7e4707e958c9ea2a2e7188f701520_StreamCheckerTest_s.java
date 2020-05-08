 package com.im1x;
 
 import static org.junit.Assert.*;
 
 import java.io.File;
 
 import org.junit.Test;
 /**
  * 
  * @author Vitaly Batrakov
  *
  */
 public class StreamCheckerTest {
 	StreamChecker streamChecker = new StreamChecker();
 
 
 	@Test
 	public void testCheckDirs() {
		assertFalse(streamChecker.checkDirs());
 		
 		streamChecker.setInputDir("Not empty");
 		streamChecker.setOutputDir("Not empty");
 		
 		assertTrue(streamChecker.checkDirs());
 	}
 	
 	@Test
 	public void testExistOrCreateDirs() {
 		String inD = System.getProperty("user.dir") + System.getProperty("file.separator") + "input";
 		String outD = System.getProperty("user.dir") + System.getProperty("file.separator") + "output";
 
 		streamChecker.setInputDir(inD);
 		streamChecker.setOutputDir(outD);
 		
 		assertTrue(streamChecker.existOrCreateDirs());
 		
 		new File(inD).delete();
 		new File(outD).delete();
 	}
 
 }
