 package com.anderscore.springbatch.article.domain.utils;
 
 import static org.junit.Assert.assertNotNull;
 
 import static org.junit.Assert.assertTrue;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URISyntaxException;
 
 import javax.persistence.EntityManager;
 import javax.persistence.Persistence;
 
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.TestExecutionListeners;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
 
 import com.anderscore.springbatch.article.ManageJobDB;
 
 @ContextConfiguration(locations = { "/DataGeneratorContext.xml" })
 @RunWith(SpringJUnit4ClassRunner.class)
 @TestExecutionListeners({DependencyInjectionTestExecutionListener.class})
 
 public class DataGeneratorTest {
	@Autowired
 	public  DataGenerator  dataGenerator;
 	
 	@Before
 	
 	public  void before() {
 		System.out.println("Calling @Beforeclass -----------------------------------");
 	
 	}
 	
 	@Test
 	public void testSimpleProperties() throws URISyntaxException, IOException {
 		assertTrue(new File(Thread.currentThread().getContextClassLoader().getResource("DataGeneratorContext.xml").toURI()).exists());
 		
 	}
 
 	@Test
 	public void testGenerateData() {
 		// of course, an ApplicationContext is just a BeanFactory
 		 dataGenerator.generateData(20);
 	}
 	
 	@AfterClass
 	public static void after() {
 		
 	}
 
 }
