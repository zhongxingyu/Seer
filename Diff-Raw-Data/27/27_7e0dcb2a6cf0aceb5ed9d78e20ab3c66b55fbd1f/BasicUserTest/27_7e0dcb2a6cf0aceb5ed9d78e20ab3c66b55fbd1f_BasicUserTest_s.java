 package com.tpg.tmjug.springdata.demo.mongodb.entity;
 
import static org.junit.Assert.*;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.data.mongodb.core.MongoTemplate;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import com.tpg.tmjug.springdata.demo.entity.User;
import com.tpg.tmjug.springdata.demo.mongodb.config.BasicUserConfig;
 import com.tpg.tmjug.springdata.demo.repository.UserRepository;
 
 @RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = BasicUserConfig.class)
 public class BasicUserTest {
 
 	@Autowired
 	MongoTemplate template;
 	
 	@Autowired
 	UserRepository genericUserRepository;
 	
 	@Before
 	public void setup() {
 		template.dropCollection(User.class);
 	}	
 	
 	@Test
 	public void shouldPersistNewUser() {
 		final User user = new User("John", 21);
 		final User savedUser = genericUserRepository.save(user);
 		
 		final User foundUser = genericUserRepository.findOne(savedUser.getId());
 		
 		assertNotNull(savedUser);
 		assertEquals(savedUser, foundUser);
 	}
 }
