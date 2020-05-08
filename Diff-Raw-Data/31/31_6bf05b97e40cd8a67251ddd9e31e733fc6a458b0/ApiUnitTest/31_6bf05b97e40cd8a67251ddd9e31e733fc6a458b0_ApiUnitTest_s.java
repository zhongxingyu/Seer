 package appnexus.api;
 
 import static java.lang.String.format;
 import static org.junit.Assert.assertEquals;
 import static org.mockito.Mockito.times;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 import static org.mockito.MockitoAnnotations.initMocks;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.Mock;
 
 import appnexus.AccountDetails;
 import appnexus.http.HttpClient;
 
 public class ApiUnitTest {
   
   private AccountDetails accountDetails = new AccountDetails.Builder()
     .username("username").password("password").accessToken(Fixtures.DUMMY_TOKEN).build();
   
   private Api api = new Api(accountDetails);
   
   @Mock private HttpClient mockHttpClient;
   
   @Before
   public void setUp() {    
     initMocks(this);
     api.setHttpClient(mockHttpClient);
   }
   
   @Test
   public void testAuth() {
     String url = Api.ApiPath.AUTH;
     String authJsonPayload = format(Api.AUTH_JSON_PAYLOAD, accountDetails.getUsername(), accountDetails.getPassword());
     when(mockHttpClient.post(url, null, authJsonPayload)).thenReturn(Fixtures.AUTH_RESPONSE);
     
     api.accountDetails.setAccessToken(null);
     api.auth();
     assertEquals(Fixtures.DUMMY_TOKEN, api.accountDetails.getAccessToken());
     
     verify(mockHttpClient, times(1)).post(url, null, authJsonPayload);
   }
   
 }
