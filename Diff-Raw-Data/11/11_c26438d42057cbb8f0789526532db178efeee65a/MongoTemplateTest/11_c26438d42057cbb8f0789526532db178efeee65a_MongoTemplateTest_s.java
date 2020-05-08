 package org.springframework.mongo.core.support;
 
 import com.mongodb.BasicDBObject;
 import com.mongodb.DBObject;
 import org.junit.Test;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.mongo.core.CursorMapper;
 import org.springframework.mongo.core.MongoOperations;
 import org.springframework.mongo.test.MongoTestSupport;
 import org.springframework.mongo.test.fixture.Profile;
 import org.springframework.test.context.ContextConfiguration;
 
 import static org.junit.Assert.assertEquals;
 import static org.springframework.mongo.support.MongoUtil.extractId;
 import static org.springframework.mongo.support.MongoUtil.withId;
 
 @ContextConfiguration(classes = MongoTemplateTest.Config.class)
 public final class MongoTemplateTest extends MongoTestSupport {
     @Autowired
     private MongoOperations mo;
 
     @Test
     public void shouldInsertUpdateAndFindObject() {
         Profile profile = new Profile("bob", 36);
         final String id = mo.insert("Profile", toDBObject(profile));
         profile = new Profile(id, profile);
         assertEquals(profile, mo.queryForObject("Profile", new ProfileMapper(), withId(id)));
        profile = new Profile("dave", 47);
         mo.update("Profile", withId(id), toDBObject(profile));
         assertEquals(profile, mo.queryForObject("Profile", new ProfileMapper(), withId(id)));
     }
 
     @Configuration
     public static class Config {
         @Bean
         public MongoOperations mongoOperations() {
             return new MongoTemplate();
         }
     }
 
     private static DBObject toDBObject(Profile value) {
         return new BasicDBObject()
                 .append("name", value.getName())
                 .append("age", value.getAge());
     }
 
     private static final class ProfileMapper implements CursorMapper<Profile> {
         @Override
         public Profile mapCursor(DBObject cursor, int rowNum) {
             return new Profile(extractId(cursor), (String) cursor.get("name"), (Integer) cursor.get("age"));
         }
     }
 }
