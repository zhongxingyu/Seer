 package ie.cit.patrick;
 
 import static org.junit.Assert.*;
 
 import java.util.ArrayList;
 
 import ie.cit.patrick.service.impl.BookBatchProcessor;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration( { "classpath:/ie/cit/patrick/app-context.xml" } )
 
 public class testBookBatchProcessor {
 	
 	@Autowired
 	ApplicationContext context;
 
 	@Before
 	public void setUp() throws Exception {
 	}
 
 	@Test
 	public void basicSetup(){
 
 		context = new ClassPathXmlApplicationContext("classpath:/ie/cit/patrick/test-context.xml");
 		
 		BookBatchProcessor bBP = (BookBatchProcessor) context.getBean("bookBatchProcessor");
 		
		String x = "BookBatchProcessor [fileLocation=src\\test\\resources\\batchFile, delineator=~]";
 		
 		String y = bBP.toString();
 		
 		assertEquals(x, y);
 		
 		ArrayList<String> lines = bBP.convertFiletoStrings();
 		
		String testOutput = "[A~A Book~AN Other~Publisher~134565376x, " +
								"A~Dune~Frank Herbert~Manifold Press~15656357A]";
		
		assertEquals(testOutput, lines.toString());
		
		

 		
 	}
 	
 	
 	
 	
 	@After
 	public void tearDown() throws Exception {
 	}
 }
