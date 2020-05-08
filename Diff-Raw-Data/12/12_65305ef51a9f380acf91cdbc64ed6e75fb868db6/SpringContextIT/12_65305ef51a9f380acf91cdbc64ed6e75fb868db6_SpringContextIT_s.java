 package nz.co.searchwellington;
 
 import static org.junit.Assert.assertNotNull;
import nz.co.searchwellington.feeds.rss.RssNewsitemPrefetcher;
 
 import org.junit.Test;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 
 public class SpringContextIT {
 
 	@Test
 	public void canLoadContext() throws Exception {
 		 ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
 		 assertNotNull(context);
 	}
 	
	@Test
	public void canAutowire() throws Exception {
		final ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");		
		Object autowire = context.getAutowireCapableBeanFactory().autowire(RssNewsitemPrefetcher.class, AutowireCapableBeanFactory.AUTOWIRE_CONSTRUCTOR, true);
		System.out.println(autowire);
		assertNotNull(autowire);
	}
	
 }
