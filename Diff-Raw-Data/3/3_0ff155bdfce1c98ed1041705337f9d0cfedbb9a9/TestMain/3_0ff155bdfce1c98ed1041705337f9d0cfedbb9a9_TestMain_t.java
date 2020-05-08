 package org.zend.sdk.test.sdkcli;
 
 import org.junit.Test;
 import org.zend.sdkcli.Main;
 
 public class TestMain {
 
 	@Test
 	public void testValidCommandLine() {
		Main.main(new String[] { "create", "project", "-n", "testName" });
 	}
 
 	@Test
 	public void testInalidCommandLine() {
 		Main.main(new String[] { "create", "project", });
 	}
 
 }
