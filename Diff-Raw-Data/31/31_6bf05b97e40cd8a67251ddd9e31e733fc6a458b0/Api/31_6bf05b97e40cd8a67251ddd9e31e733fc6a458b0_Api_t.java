 package appnexus.api;
 
 import static java.lang.String.format;
 import static org.apache.commons.lang.StringUtils.isBlank;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import appnexus.AccountDetails;
 import appnexus.Appnexus;
 import appnexus.exception.AppnexusAuthException;
 import appnexus.exception.AppnexusException;
 import appnexus.http.HttpClient;
 import appnexus.utils.JsonUtils;
 
 public class Api {
   
   public interface ApiPath {
     public static final String BASE = Appnexus.getApiBase();  
     public static final String AUTH = format("%s/auth", BASE);
     public static final String MEMBER = format("%s/member", BASE);    
   }
   
   protected HttpClient httpClient = HttpClient.DEFAULT;
   
   protected AccountDetails accountDetails;
   
   protected static final String JSON_RESPONSE_ROOT = "response";
   
   protected static final String AUTH_JSON_PAYLOAD = "{\"auth\": {\"username\" : \"%s\", \"password\" : \"%s\"}}";
 
   public Api(AccountDetails accountDetails) {
     this.accountDetails = accountDetails;
   }
   
   public void auth() {
     String authJsonPayload = format(AUTH_JSON_PAYLOAD, accountDetails.getUsername(), accountDetails.getPassword());
     String token = doPost(ApiPath.AUTH, null, AuthResponse.class, authJsonPayload).getToken();
     this.accountDetails.setAccessToken(token);
   }
   
   public Member getMember() {
     return doGet(ApiPath.MEMBER, MemberResponse.class).getMember();
   }
 
   private <T extends Response> T doGet(String apiPath, Class<T> clazz) {
    T response = fromJson(httpClient.get(apiPath, headers()), clazz);
     try {
       checkResponseStatus(response);
     } catch (AppnexusAuthException e) {
       // Do auth and try one more time
       auth();
       response = fromJson(httpClient.get(apiPath, headers()), clazz);
       checkResponseStatus(response);
     }
     return response;
   }
 
   protected <T extends Response> T doPost(String apiPath, Class<T> clazz, String payload) {
     return doPost(apiPath, headers(), clazz, payload);
   }
   
   protected <T extends Response> T doPost(String apiPath, Map<String, String> headers, Class<T> clazz, String payload) {
     T response = fromJson(httpClient.post(apiPath, headers, payload), clazz);
     try {
       checkResponseStatus(response);
     } catch (AppnexusAuthException e) {
       // Do auth and try one more time
       auth();
       response = fromJson(httpClient.post(apiPath, headers, payload), clazz);
       checkResponseStatus(response);
     }
     return response;
   }
 
   protected void checkResponseStatus(Response response) {
     if (!"OK".equalsIgnoreCase(response.getStatus())) {
       // Check if not logged in
       if ("NOAUTH".equalsIgnoreCase(response.getErrorId())) {
         throw new AppnexusAuthException(format("%s", response.getError()));
       }
       throw new AppnexusException(format("Failed request [status: %s]", response.getStatus()));
     }
   }
   
   protected Map<String, String> headers() {
     if (isBlank(accountDetails.getAccessToken())) {
       throw new RuntimeException("Token required in AccountDetails");
     }
     Map<String, String> headers = new HashMap<String, String>();
     headers.put("Authorization", format("%s", accountDetails.getAccessToken()));
     return headers;
   }
   
   protected <T> T fromJson(String json, Class<T> clazz) {
     return JsonUtils.fromJson(json, JSON_RESPONSE_ROOT, clazz);
   }
 
   protected void setHttpClient(HttpClient httpClient) {
     this.httpClient = httpClient;
   }
   
 }
