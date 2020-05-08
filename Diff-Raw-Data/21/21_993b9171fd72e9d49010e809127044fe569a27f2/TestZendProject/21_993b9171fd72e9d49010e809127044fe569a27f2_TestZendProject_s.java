 package org.zend.sdk.test.sdklib.library;
 
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 import java.io.File;
 
 import org.junit.Test;
 import org.zend.sdk.test.AbstractTest;
 import org.zend.sdkcli.ParseError;
 import org.zend.sdklib.ZendProject;
 
 public class TestZendProject extends AbstractTest {
 
 	@Test
 	public void testZendProjectCreation1() throws ParseError {
 		new File("testdir1").mkdir();
 		ZendProject project = new ZendProject("name", true, "testdir1");
 		assertTrue(project.create());
 		
 		assertTrue(new File("testdir1/descriptor.xml").exists());
 		assertTrue(new File("testdir1/public/index.html").exists());
 		assertTrue(new File("testdir1/scripts/post_activate.php").exists());
 		assertTrue(new File("testdir1/scripts/post_deactivate.php").exists());
 		assertTrue(new File("testdir1/scripts/post_stage.php").exists());
 		assertTrue(new File("testdir1/scripts/post_unstage.php").exists());
 		assertTrue(new File("testdir1/scripts/pre_activate.php").exists());
 		assertTrue(new File("testdir1/scripts/pre_deactivate.php").exists());
 		assertTrue(new File("testdir1/scripts/pre_stage.php").exists());
 		assertTrue(new File("testdir1/scripts/pre_unstage.php").exists());
 	}
 
 	@Test
 	public void testZendProjectCreation2() throws ParseError {
 		ZendProject project = new ZendProject("name", true, null);
 		assertTrue(project.create());
 		
 		assertTrue(new File("descriptor.xml").exists());
 		assertTrue(new File("public/index.html").exists());
 		assertTrue(new File("scripts/post_activate.php").exists());
 		assertTrue(new File("scripts/post_deactivate.php").exists());
 		assertTrue(new File("scripts/post_stage.php").exists());
 		assertTrue(new File("scripts/post_unstage.php").exists());
 		assertTrue(new File("scripts/pre_activate.php").exists());
 		assertTrue(new File("scripts/pre_deactivate.php").exists());
 		assertTrue(new File("scripts/pre_stage.php").exists());
 		assertTrue(new File("scripts/pre_unstage.php").exists());
 	}
 
 	@Test
 	public void testZendProjectCreation3() throws ParseError {
 		new File("testdir3").mkdir();
 		ZendProject project = new ZendProject("name", false, "testdir3");
 		assertTrue(project.create());
 		
 		assertTrue(new File("testdir3/descriptor.xml").exists());
 		assertTrue(new File("testdir3/public/index.html").exists());
 		assertFalse(new File("testdir3/scripts/post_activate.php").exists());
 		assertFalse(new File("testdir3/scripts/post_deactivate.php").exists());
 		assertFalse(new File("testdir3/scripts/post_stage.php").exists());
 		assertFalse(new File("testdir3/scripts/post_unstage.php").exists());
 		assertFalse(new File("testdir3/scripts/pre_activate.php").exists());
 		assertFalse(new File("testdir3/scripts/pre_deactivate.php").exists());
 		assertFalse(new File("testdir3/scripts/pre_stage.php").exists());
 		assertFalse(new File("testdir3/scripts/pre_unstage.php").exists());
 	}
 
 }
