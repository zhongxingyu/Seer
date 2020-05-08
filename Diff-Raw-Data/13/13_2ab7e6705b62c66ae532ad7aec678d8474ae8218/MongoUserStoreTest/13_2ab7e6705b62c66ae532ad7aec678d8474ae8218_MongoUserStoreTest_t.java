 package org.atlasapi.application.users;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 import java.util.Set;
 
import org.atlasapi.application.users.v3.User;
 import org.atlasapi.media.entity.Publisher;
 import org.junit.Before;
 import org.junit.Test;
 
 import com.google.common.collect.ImmutableSet;
 import com.metabroadcast.common.persistence.MongoTestHelper;
 import com.metabroadcast.common.social.model.UserRef;
 import com.metabroadcast.common.social.model.UserRef.UserNamespace;
 
 
 public class MongoUserStoreTest {
     
     private UserStore store;
     
     @Before
     public void setup() {
         store = new MongoUserStore(MongoTestHelper.anEmptyTestDatabase());
     }
     
     @Test 
     public void testUserPersists() {
         final Long id = Long.valueOf(5000);
         final UserRef userRef = new UserRef(5000, UserNamespace.TWITTER, "test");
         final String screenName = "test123";
         final String fullName = "Test One Two Three";
         final String company = "the company";
         final String email = "me@example.com";
         final String website = "http://example.com";
         final String profileImage = "http://example.com/image.png";
         final Role role = Role.REGULAR;
         final Set<String> applicationSlugs = ImmutableSet.of("app1", "app2");
         final Set<Publisher> sources = ImmutableSet.of(Publisher.YOUTUBE, Publisher.SVERIGES_RADIO);
         
         final boolean profileComplete = true;
         User user = User.builder()
                 .withId(id)
                 .withUserRef(userRef)
                 .withScreenName(screenName)
                 .withFullName(fullName)
                 .withCompany(company)
                 .withEmail(email)
                 .withWebsite(website)
                 .withProfileImage(profileImage)
                 .withApplicationSlugs(applicationSlugs)
                 .withSources(sources)
                 .withRole(role)
                 .withProfileComplete(profileComplete)
                 .build();
         store.store(user);
         
         User retrieved = store.userForRef(userRef).get();
         assertEquals(id, retrieved.getId());
         assertEquals(userRef, retrieved.getUserRef());
         assertEquals(screenName, retrieved.getScreenName());
         assertEquals(company, retrieved.getCompany());
         assertEquals(email, retrieved.getEmail());
         assertEquals(website, retrieved.getWebsite());
         assertEquals(profileImage, retrieved.getProfileImage());
         assertEquals(role, retrieved.getRole());
         assertEquals(2, retrieved.getApplicationSlugs().size());
         assertTrue(retrieved.getApplicationSlugs().contains("app2"));
         assertEquals(2, retrieved.getSources().size());
         assertTrue(retrieved.getSources().contains(Publisher.YOUTUBE));
         assertTrue(retrieved.isProfileComplete());
     }
     
 
 }
