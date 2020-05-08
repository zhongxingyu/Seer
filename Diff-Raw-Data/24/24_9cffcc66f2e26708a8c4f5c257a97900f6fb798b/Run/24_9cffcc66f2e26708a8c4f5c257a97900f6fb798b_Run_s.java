 package bookstore;
 
 import org.apache.log4j.Logger;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

 import bookstore.entity.User;
 import bookstore.repository.UserRepository;
 
 
 //@Component
 //@ContextConfiguration(locations = "classpath:applicationContext.xml")
 public class Run {
 	private static Logger logger = Logger.getLogger(Run.class.getName());
 	
 	private static UserRepository repository;
 	
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext.xml");
		repository=(UserRepository) ctx.getBean(UserRepository.class);
 		logger.info("Number of users in the db before adding more: " + repository.count());
 		
 		User tom = new User("123 California", "Apt 143", "LA", "Tom@gmail.com", "Tom", "Hanks", "Itsasecret", "CA","54221");
 		User patrick = new User("847 Mapple Dr.", "", "Washington", "Patrick@gmail.com", "Patrick", "Steward", "moneyMonkey", "MD","64541");
 		User  john = new User("8484 Bristol", "", "Columbus", "john@gmail.com", "John", "Roberts", "pass", "OH","57963");
 		repository.save(tom);
 		repository.save(patrick);
 		repository.save(john);
 		
 		logger.info("Number of users in the db after adding more: " + repository.count());
 	}
 
 }
