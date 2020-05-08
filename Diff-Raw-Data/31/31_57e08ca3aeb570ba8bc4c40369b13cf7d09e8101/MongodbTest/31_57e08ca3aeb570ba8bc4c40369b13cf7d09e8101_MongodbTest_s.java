 package edu.asu.krypton.service;
 
 import java.lang.reflect.Field;
 
import javax.persistence.Transient;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.slf4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.data.mongodb.core.MongoTemplate;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import edu.asu.krypton.model.persist.db.Article;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration
 public class MongodbTest {
 	private Logger logger = org.slf4j.LoggerFactory.getLogger(MongodbTest.class);
 	@Autowired
 	private MongoTemplate mongoTemplate;
 	
 	@Test
 	public void test() throws SecurityException, NoSuchFieldException {
 		System.out.println(Article.class.getField("").getType().getSimpleName());
 	}
 
 }
