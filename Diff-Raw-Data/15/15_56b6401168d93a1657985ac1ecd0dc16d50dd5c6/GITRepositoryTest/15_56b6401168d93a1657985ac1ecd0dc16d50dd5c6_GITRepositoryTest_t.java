 package org.jabox.scm.git;
 
import java.io.File;

 import junit.framework.TestCase;
 
 public class GITRepositoryTest extends TestCase {
 
 	public void testInitialize() {
 		GITRepository.initialize();
 	}
 
	public void testGetGitBaseDir() {
		File gitBaseDir = GITRepository.getGitBaseDir();
		assertTrue(gitBaseDir.exists());
 	}
 
 }
