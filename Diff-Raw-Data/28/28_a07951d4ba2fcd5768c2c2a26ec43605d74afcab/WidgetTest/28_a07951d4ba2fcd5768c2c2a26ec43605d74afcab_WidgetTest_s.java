 package me.guymer.spring.mybatis;
 
 import java.util.Calendar;
 import java.util.Date;
 
 import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
 
 import me.guymer.spring.mybatis.domain.Widget;
 import me.guymer.spring.mybatis.persistence.WidgetMapper;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mybatis.spring.MyBatisSystemException;
 import org.springframework.context.annotation.ComponentScan;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.test.context.ActiveProfiles;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.test.context.support.AnnotationConfigContextLoader;
 import org.springframework.transaction.annotation.Transactional;
 import org.springframework.web.servlet.config.annotation.EnableWebMvc;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(loader = AnnotationConfigContextLoader.class)
 @ActiveProfiles("test")
 public class WidgetTest {
 	
 	@Configuration
 	@ComponentScan(basePackages = "me.guymer.spring", excludeFilters = @ComponentScan.Filter({ComponentScan.class, EnableWebMvc.class}))
 	static class ContextConfiguration {}
 	
	@PersistenceContext
	private EntityManager entityManager;
	
 	@Inject
 	private WidgetMapper widgetMapper;
 	
 	@Test(expected = MyBatisSystemException.class)
 	@Transactional
 	public void testSqlError() {
 		String nameToLong = "012345678901234567890123456789012345678901234567890";
 		Calendar calendar = Calendar.getInstance();
 		Date now = calendar.getTime();
 		
 		Widget widget2 = new Widget();
 		widget2.setName(nameToLong);
 		widget2.setCreateDate(now);
 		widget2.setActive(true);
 		
 		widgetMapper.create(widget2);
 	}
 	
 }
