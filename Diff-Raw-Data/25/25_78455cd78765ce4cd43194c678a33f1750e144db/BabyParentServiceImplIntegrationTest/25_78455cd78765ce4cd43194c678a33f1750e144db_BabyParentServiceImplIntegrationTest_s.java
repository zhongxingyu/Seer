/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
 package ro.danix.first.model.service.user.impl;
 
 import static org.junit.Assert.*;
 import static org.hamcrest.Matchers.*;
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
 import ro.danix.first.model.domain.user.BabyParent;
 import ro.danix.first.model.domain.user.User;
 import ro.danix.first.model.domain.user.factory.BabyParentFactory;
 import ro.danix.first.model.domain.user.factory.UserFactory;
 import ro.danix.first.model.repository.user.BabyParentRepository;
 import ro.danix.first.model.service.user.BabyParentService;
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
 public class BabyParentServiceImplIntegrationTest {
 
     public static final String FOLLOWER_EMAIL = "danix1@yahoo.com";
     @Autowired
     private Mongo mongo;
     @Autowired
     private BabyParentService babyParentService;
     @Autowired
     private BabyParentRepository babyParentRepository;
     @Autowired
     private BabyParentFactory babyParentFactory;
 
     @Before
     public void setUp() {
         DB database = mongo.getDB(MongoConfig.DATABASE_NAME);
         DBCollection users = database.getCollection(BabyParent.MONGO_COLLECTION);
         users.remove(new BasicDBObject());
     }
 
     @Test
     public void followTest() {
         // given
         log.info("start followTest");
         BabyParent babyParent = babyParentFactory.build();
         babyParent = babyParentRepository.save(babyParent);
 
         BabyParent follower = babyParentFactory.build();
         follower.setUsername("danix1");
         follower.setEmailAddress(new EmailAddress(FOLLOWER_EMAIL));
         follower = babyParentRepository.save(follower);
         // when
         babyParentService.follow(babyParent, follower);
         // then
         babyParent = babyParentRepository.findByEmailAddress(new EmailAddress(BabyParentFactory.EMAIL));
         follower = babyParentRepository.findByEmailAddress(new EmailAddress(FOLLOWER_EMAIL));
 
         assertThat(babyParent.getFollowersCount(), is(new Long(1)));
         assertThat(babyParent.getFollowerIds(), hasItem(follower.getId()));
 
         assertThat(follower.getFollowingCount(), is(new Long(1)));
         assertThat(follower.getFollowingIds(), hasItem(babyParent.getId()));
     }
 }
