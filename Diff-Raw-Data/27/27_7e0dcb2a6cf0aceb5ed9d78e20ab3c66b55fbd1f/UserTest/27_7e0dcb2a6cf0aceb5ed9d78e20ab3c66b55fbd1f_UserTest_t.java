 package com.tpg.tmjug.springdata.demo.mongodb.model;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.data.mongodb.core.MongoTemplate;
 import org.springframework.data.mongodb.core.geo.Circle;
 import org.springframework.data.mongodb.core.mapreduce.MapReduceResults;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
import com.tpg.tmjug.springdata.demo.mongodb.config.BasicUserConfig;
 import com.tpg.tmjug.springdata.demo.mongodb.repository.CustomUserRepository;
 import com.tpg.tmjug.springdata.demo.mongodb.repository.StatisticsRepository;
 import com.tpg.tmjug.springdata.demo.mongodb.repository.UserRepository;
 import com.tpg.tmjug.springdata.demo.mongodb.repository.ValueObject;
 
 @RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = BasicUserConfig.class)
 public class UserTest {
 
 	@Autowired
 	UserRepository userRepository; 
 
 	@Autowired
 	StatisticsRepository statisticsRepository;
 
 	@Autowired
 	CustomUserRepository customUserRepository;
 
 	@Autowired
 	MongoTemplate template;
 
 	@Before
 	public void setup() {
 		template.dropCollection(User.class);
 	}
 
 	@Test
 	public void test() {
 		final User user1 = new User("John", "Doe", 21);
 		userRepository.save(user1);
 
 		final User user2 = new User("Johnny", "Doe", 22);
 		userRepository.save(user2);
 
 		final long foundUsers = userRepository.count();
 
 		assertTrue(foundUsers == 2);
 
 		assertEquals(new User("John", "Doe", 21), userRepository.findUserByName("John"));
 	}
 
 	@Test
 	public void test_balance_mapreduce() {
 		int magnitude = 1;
 
 		for(int i = 1; i <= 10; i++) {
 			final List<Account> accounts = new ArrayList<>();
 			for(int j = 1; j <= 10; j++) {
 				accounts.add(new Account(j * magnitude));
 			}
 
 			magnitude++;
 
 			final User user = new User("User_" + i, "", new double[] { 12.0d, 11.0d + (double)magnitude/1000 }, 20 + magnitude, accounts, new EmailAddress("User_" + i + "@demo.com"));
 			userRepository.save(user);
 		}
 
 		/* compute accounts */
 		//		MapReduceResults<ValueObject> accountsTotal = template.mapReduce("user", "classpath:map.js", "classpath:reduce.js", ValueObject.class);
 		MapReduceResults<ValueObject> accountsTotal = statisticsRepository.getUserAccountsTotal();
 		assertNotNull(accountsTotal);
 
 		/* get users age 25+ */
 		final List<User> matureUsers = customUserRepository.findByAgeGreaterThan(25);
 		assertNotNull(matureUsers);
 
 		/* within range */
 		final Circle centerPoint = new Circle(12.0d, 11.0d, 0.004);
 		final List<User> usersCloseToCenter = customUserRepository.findByLocationWithin(centerPoint);
 
 		assertNotNull(usersCloseToCenter);
 		assertEquals(3, usersCloseToCenter.size());
 	}
 
 }
