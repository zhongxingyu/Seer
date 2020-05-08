 package org.springframework.social.appdotnet.connect;
 
 import org.junit.Test;
 import org.mockito.Mockito;
 import org.springframework.social.appdotnet.api.Appdotnet;
 import org.springframework.social.appdotnet.api.UsersOperations;
 import org.springframework.social.appdotnet.api.data.user.ADNUser;
 import org.springframework.social.connect.UserProfile;
 
 import static org.junit.Assert.assertEquals;
 
 /**
  * @author Arik Galansky
  */
 public class AppdotnetAdapterTest {
     private AppdotnetAdapter apiAdapter = new AppdotnetAdapter();
 
     private Appdotnet api = Mockito.mock(Appdotnet.class);
 
     @Test
     public void fetchProfile() {
         UsersOperations usersOperations = Mockito.mock(UsersOperations.class);
         Mockito.when(api.usersOperations()).thenReturn(usersOperations);
         Mockito.when(usersOperations.getUserProfile()).thenReturn(createBasicUser());
         UserProfile userProfile = apiAdapter.fetchUserProfile(api);
         assertEquals("Arik Galansky", userProfile.getName());
         assertEquals("arikg", userProfile.getUsername());
     }
 
     private ADNUser createBasicUser() {
        return new ADNUser("123", "arikg", "Arik Galansky", null, null, null, null, null, null, null, null, null, false, false, false);
     }
 }
