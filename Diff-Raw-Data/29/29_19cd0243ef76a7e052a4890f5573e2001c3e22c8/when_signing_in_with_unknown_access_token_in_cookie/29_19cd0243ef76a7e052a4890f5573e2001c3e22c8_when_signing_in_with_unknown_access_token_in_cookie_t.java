 package eu.piotrbuda.twittory.signin;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.InjectMocks;
 import org.mockito.Mock;
 import org.mockito.runners.MockitoJUnitRunner;
 
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletRequest;
 import javax.ws.rs.core.Response;
 import java.util.List;
 
import static junit.framework.Assert.*;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 
 /**
  * .
  */
 @RunWith(MockitoJUnitRunner.class)
 public class when_signing_in_with_unknown_access_token_in_cookie {
     @InjectMocks
     private TwitterSignInResource resource;
 
     @Mock
     private HttpServletRequest request = mock(HttpServletRequest.class);
 
     @Test
     public void then_user_is_redirected_to_twitter_for_authentication() {
         givenACookieWithAccessToken();
         andUnknownAccessTokenSecret();
         userIsRedirectedToTwitterForAuthentication();
     }
 
     private void givenACookieWithAccessToken() {
         when(request.getCookies()).thenReturn(new Cookie[]{new Cookie("accesstoken", "token")});
     }
 
     private void andUnknownAccessTokenSecret() {
        fail("Accessing access tokens is not possible at the moment");
     }
 
     private void userIsRedirectedToTwitterForAuthentication() {
         Response response = resource.signIn(request);
         assertThatResponse(response).isRedirect();
        assertThatResponse(response).redirectsTo("http://api.twitter.com/oauth/authenticate?oauth_token=");
     }
 
     private ResponseAssertion assertThatResponse(Response response) {
         return new ResponseAssertion(response);
     }
 
     private class ResponseAssertion {
         private Response response;
 
         public ResponseAssertion(Response response) {
             this.response = response;
         }
 
         public void isRedirect() {
             assertEquals(Response.Status.SEE_OTHER.getStatusCode(), response.getStatus());
         }
 
         public void redirectsTo(String url) {
             assertTrue(response.getMetadata().containsKey("Location"));
             List<Object> location = response.getMetadata().get("Location");
             assertTrue(location.get(0).toString().contains(url));
         }
     }
 }
