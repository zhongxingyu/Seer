 package benworks.pring.bean.factory.xml.people;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.BeansException;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.ApplicationContextAware;
 import org.springframework.stereotype.Component;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
import benworks.pring.bean.factory.xml.people.People;

 /**
  * 实现ApplicationContextAware的实例会自动调用setApplicationContext()方法
 * 
  * @author Ben
  */
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration
 @Component
 public class PeopleTest implements ApplicationContextAware {
 
 	@Test
 	public void testPeople() {
 		People p = (People) applicationContext.getBean("cutesource");
 		System.out.println(p.getId());
 		System.out.println(p.getName());
 		System.out.println(p.getAge());
 	}
 
 	// 实现接口的方法
 	private ApplicationContext applicationContext;
 
 	@Override
 	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
 		this.applicationContext = applicationContext;
 	}
 }
