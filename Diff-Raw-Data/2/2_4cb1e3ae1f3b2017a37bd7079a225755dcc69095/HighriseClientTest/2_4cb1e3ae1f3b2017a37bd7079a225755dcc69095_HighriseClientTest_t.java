 package org.nobel.highriseapi;
 
 import org.junit.Assert;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.nobel.highriseapi.resources.PersonResource;
 import org.nobel.highriseapi.resources.UserResource;
 
 public class HighriseClientTest {
 
     HighriseClient highriseClient;
 
     @Test
    @Ignore
     public void getMe() throws Exception {
         highriseClient = HighriseClient.create("X");
         Assert.assertNotNull(highriseClient.auth("X", "X"));
         Assert.assertNotNull(highriseClient.getResource(UserResource.class).getMe());
         Assert.assertNotNull(highriseClient.getResource(PersonResource.class).getAll());
         Assert.assertNotNull(highriseClient.getResource(PersonResource.class).get(120593863));
         // highriseClient.getResource(PersonResource.class).get(111);
     }
 }
