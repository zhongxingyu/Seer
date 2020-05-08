/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
 package ro.danix.first.model.repository.user.mongo;
 
 import static org.hamcrest.Matchers.*;
 import static ro.danix.first.CoreMatchers.*;
 import static org.junit.Assert.*;
 import com.mongodb.BasicDBObject;
 import com.mongodb.DB;
 import com.mongodb.DBCollection;
 import com.mongodb.Mongo;
 import lombok.extern.slf4j.Slf4j;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.experimental.categories.Category;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ActiveProfiles;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import ro.danix.first.model.config.FactoriesConfig;
 import ro.danix.first.model.config.MongoConfig;
 import ro.danix.first.model.domain.EmailAddress;
 import ro.danix.first.model.domain.factory.BabyFactory;
 import ro.danix.first.model.domain.user.BabyParent;
 import ro.danix.first.model.domain.Baby;
 import ro.danix.first.model.domain.user.User;
 import ro.danix.first.model.domain.user.factory.BabyParentFactory;
 import ro.danix.first.model.repository.BabyRepository;
 import ro.danix.first.model.repository.user.BabyParentRepository;
 import ro.danix.test.SlowRunningTests;
 
 /**
  *
  * @author moro
  */
 @Category(SlowRunningTests.class)
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(classes = {MongoConfig.class, FactoriesConfig.class})
 @ActiveProfiles(profiles = "factories")
 @Slf4j
 public class BabyParentRepositoryImplIntegrationTest {
 
     @Autowired
     private Mongo mongo;
     @Autowired
     private BabyParentRepository babyParentRepository;
     @Autowired
     private BabyParentFactory babyParentFactory;
     @Autowired
     private BabyFactory babyFactory;
     @Autowired
     private BabyRepository babyRepository;
 
     @Before
     public void setUp() {
         log.info("start setUp");
         DB database = mongo.getDB(MongoConfig.DATABASE_NAME);
         DBCollection babyParent = database.getCollection(User.MONGO_COLLECTION);
         babyParent.remove(new BasicDBObject());        
     }
 
     @Test
     public void saveTest() {
                 
         Baby baby = babyFactory.build();
         babyRepository.save(baby);
         
         BabyParent babyParent = babyParentFactory.build();
         babyParent.addBaby(baby);        
         babyParent = babyParentRepository.save(babyParent);
 
         log.debug("babyParent saved with id = {}", babyParent.getId());
 
         assertThat(babyParent, is(notNullValue()));
         assertThat(babyParent, is(firstNamed(BabyParentFactory.FIRST_NAME)));
 
         assertThat(babyParent.getBabiesCount(), is(1l));
         assertThat(babyParent.getBabiesIds(), hasItem(baby.getId()));        
 
     }
 
     @Test
     public void findByEmailAddressTest() {        
         log.info("start findByEmailAddressTest");
         BabyParent babyParent = babyParentFactory.build();                
         babyParentRepository.save(babyParent);
 
         EmailAddress emailAddress = new EmailAddress(BabyParentFactory.EMAIL);
         babyParent = babyParentRepository.findByEmailAddress(emailAddress);
         assertThat(babyParent, is(notNullValue()));
         assertThat(babyParent, is(firstNamed(BabyParentFactory.FIRST_NAME)));
     }
 }
