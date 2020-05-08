 package pw.pref.service;
 
 import static org.mockito.Matchers.anyString;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 import pw.authentication.api.client.RestServiceClient;
 import pw.pref.constants.ApiConstants;
 import pw.pref.api.helper.ApiContext;
 
 public class ChatServiceTest {
     ChatService chatService;
     RestServiceClient restServiceClient;
     private ApiContext apiContext;
     private static final String USERID = "some_userid";
     private static final String KEY = "some_key";
     private static final String VALUE = "some_value";
 
     @BeforeMethod
     public void setUp() {
         restServiceClient = mock(RestServiceClient.class);
         apiContext = mock(ApiContext.class);
         chatService = new ChatService(restServiceClient);
     }
 
     @Test
     public void notifiesChatServer() {
         when(apiContext.getUserId()).thenReturn(USERID);
         when(apiContext.getKey()).thenReturn(KEY);
         when(apiContext.getValue()).thenReturn(VALUE);
         chatService.notifyChatOfNewValue(apiContext);
         verify(restServiceClient)
                .callRestApi("chatserver.api.pw", "notifyPreferenceChange", ApiConstants.USER_ID_PARAMETER, USERID,
                         "key", KEY, "value", VALUE);
     }
 
 }
