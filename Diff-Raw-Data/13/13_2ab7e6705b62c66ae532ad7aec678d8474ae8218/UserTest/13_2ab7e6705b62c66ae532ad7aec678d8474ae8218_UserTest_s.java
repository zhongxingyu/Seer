 package org.atlasapi.application.users;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
import org.atlasapi.application.Application;
import org.atlasapi.application.ApplicationConfiguration;
import org.atlasapi.application.ApplicationCredentials;
 import org.atlasapi.media.entity.Publisher;
 import org.junit.Test;
 
 import com.google.common.collect.ImmutableSet;
 import com.metabroadcast.common.base.Maybe;
 
 
 public class UserTest {
     
     @Test
     public void testAddingApplication() {
         User user = User.builder().withApplicationSlugs(ImmutableSet.of("app1", "app2")).build();
         ApplicationCredentials creds = new ApplicationCredentials("apiKey123");
         ApplicationConfiguration config = ApplicationConfiguration.defaultConfiguration();
         Application newApp = Application.application("app3")
                 .withCredentials(creds)
                 .withConfiguration(config)
                 .build();
         User modified = user.copyWithAddedApplication(newApp);
         assertTrue(modified.getApplicationSlugs().contains(newApp.getSlug()));
         assertEquals(3, modified.getApplicationSlugs().size());
     }
     
     @Test
     public void testManages() {
         User user = User.builder()
                 .withApplicationSlugs(ImmutableSet.of("app1", "app2", "app3"))
                 .withSources(ImmutableSet.of(Publisher.YOUTUBE, Publisher.SVERIGES_RADIO))
                 .build();
         ApplicationCredentials creds = new ApplicationCredentials("apiKey123");
         ApplicationConfiguration config = ApplicationConfiguration.defaultConfiguration();
         Application app3 = Application.application("app3")
                 .withCredentials(creds)
                 .withConfiguration(config)
                 .build();
         assertTrue(user.manages(app3));
         assertTrue(user.manages("app3"));
         assertTrue(user.manages(Maybe.just(Publisher.SVERIGES_RADIO)));
     }
 
 }
