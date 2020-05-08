 package com.kxen.han.projection.fpg;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.JUnit4;
 
 
 @RunWith(JUnit4.class)
 public class ProjectionTest {
 
 	@Test
 	public void testProject() throws Exception {
		OutputLayer ol = OutputLayer.newInstance();
		Projection.project("src/test/resources/TestExampleAutoGen", 
				ol, 3);
 	}
 }
