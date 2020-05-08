 package eu.piotrbuda.twittory.signin;
 
 import eu.piotrbuda.twittory.storage.AccessTokenStorage;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.InjectMocks;
 import org.mockito.Mock;
 import org.mockito.runners.MockitoJUnitRunner;
 import twitter4j.Twitter;
 import twitter4j.auth.AccessToken;
 import twitter4j.auth.RequestToken;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 import javax.ws.rs.WebApplicationException;
 
 import static org.mockito.Mockito.*;
 
 /**
  * Unit tests of {@link TwitterSignInResource}
  */
 @RunWith(MockitoJUnitRunner.class)
 public class TwitterSignInResourceTest {
 
     @InjectMocks
     private TwitterSignInResource resource;
 
     @Mock
     private Twitter twitter;
 
     @Mock
     private AccessTokenStorage storage;
 
     private String oauth_token = "token";
 
     private String oauth_verifier = "verifier";
 
     private String oauth_secret = "secret";
 
     @Mock
     private HttpServletRequest request = mock(HttpServletRequest.class);
 
     @Mock
     private HttpSession session = mock(HttpSession.class);
 
     @Before
     public void setUp() throws Exception {
         when(request.getSession()).thenReturn(session);
     }
 
     @After
     public void tearDown() throws Exception {
         resource = null;
     }
 
     @Test(expected = WebApplicationException.class)
     public void it_is_impossible_to_call_callback_without_request_token() throws Exception {
         when(session.getAttribute("requestToken")).thenReturn(null);
 
         resource.callback(request, "", "");
     }
 
     @Test
     public void token_is_obtained() throws Exception {
         when(session.getAttribute("requestToken")).thenReturn(new RequestToken(oauth_token, oauth_secret));
 
         resource.callback(request, oauth_token, oauth_verifier);
 
         verify(twitter).getOAuthAccessToken(any(RequestToken.class), anyString());
     }
 
     @Test
     public void access_token_is_stored_upon_retrieval() throws Exception {
         AccessToken accessToken = new AccessToken("1-token", "secret");
         when(session.getAttribute("requestToken")).thenReturn(new RequestToken(oauth_token, oauth_secret));
         when(twitter.getOAuthAccessToken(any(RequestToken.class), anyString())).thenReturn(accessToken);
 
         resource.callback(request, oauth_token, oauth_verifier);
 
         verify(storage).storeAccessToken(accessToken);
     }
 

 }
