 package edu.lcu.masterfollies.domain;
 
 import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 
@Configurable
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:/META-INF/spring/applicationContext.xml", 
"classpath:/META-INF/spring/applicationContext-datasource-test.xml"})
 public class JudgesTest {
 	@Test
 	public void testMarkerMethod(){
 		
 	}
 	@Test
 	public void testGetJudges(){
 		
 	}
 	@Test
 	public void testJudgeFind(){
		JudgesMapper je = null;
		//je.selectByPrimaryKey(1);
 	}
 }
