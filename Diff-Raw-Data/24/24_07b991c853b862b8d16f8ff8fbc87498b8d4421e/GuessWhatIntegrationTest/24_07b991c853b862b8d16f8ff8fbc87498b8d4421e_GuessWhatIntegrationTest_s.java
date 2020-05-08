 package bneijt.guesswhat;
 
 
 import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;
 
 public class GuessWhatIntegrationTest {
@Test
public void shouldLoadConfiguration() throws Exception {
	//load application context
	new ClassPathXmlApplicationContext("WEB-INF/applicationContext.xml");
}
 }
