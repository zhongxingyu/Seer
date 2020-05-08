 package nz.co.searchwellington;
 
 import static org.junit.Assert.assertNotNull;
 
 import org.junit.Test;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 
 public class SpringContextIT {
 
 	@Test
 	public void canLoadContext() throws Exception {
 		 ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
 		 assertNotNull(context);
 	}
 	
 }
