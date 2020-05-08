 package postageapp;
 
 import com.google.gson.Gson;
 import com.google.gson.reflect.TypeToken;
 import org.apache.http.client.utils.URIBuilder;
 import org.apache.http.impl.client.DefaultHttpClient;
 import postageapp.http.PostageAppException;
 import postageapp.http.PostageAppHttpClient;
 import postageapp.http.PostageAppHttpClientImpl;
 import postageapp.models.*;
 import postageapp.params.MessageParams;
 
 import java.lang.reflect.Type;
 import java.net.URISyntaxException;
 import java.util.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: stephanleroux
  * Date: 2012-11-23
  * Time: 6:28 PM
  * To change this template use File | Settings | File Templates.
  */
 public class PostageAppClientImpl implements PostageAppClient {
     private final String apiVersion;
 
     private static final String API_HOST = "api.postageapp.com";
 
     private static final class Endpoints {
         static final String SEND_MESSAGE = "send_message";
         static final String GET_MESSAGE_RECEIPT = "get_message_receipt";
         static final String GET_METHOD_LIST = "get_method_list";
         static final String GET_ACCOUNT_INFO = "get_account_info";
         static final String GET_PROJECT_INFO = "get_project_info";
         static final String GET_MESSAGES = "get_messages.json";
         static final String GET_MESSAGE_TRANSMISSIONS = "get_message_transmissions";
         static final String GET_METRICS = "get_metrics";
     }
 
     private final String apiKey;
     private final PostageAppHttpClient httpClient;
     private final Gson gson;
 
     public PostageAppClientImpl(final String apiKey) {
         this.apiKey = apiKey;
         this.apiVersion = "v.1.0";
         this.httpClient = new PostageAppHttpClientImpl(new DefaultHttpClient());
         this.gson = new Gson();
     }
 
     @Override
     public long sendMessage(MessageParams params) throws PostageAppException {
         Map<String, ?> data = this.getDataFromResponse(this.sendRequest(Endpoints.SEND_MESSAGE, params.toString()));
         Map<String, String> message = (Map<String, String>) data.get("message");
        return Long.parseLong((String) message.get("id"));
     }
 
     @Override
     public long getMessageReceipt(String messageUid) throws PostageAppException {
         Map<String, ?> data = this.getDataFromResponse(this.sendRequest(Endpoints.GET_MESSAGE_RECEIPT, this.messageUidRequestString(messageUid)));
         Map<String, String> message = (Map<String, String>) data.get("message");
        return Long.parseLong((String) message.get("id"));
     }
 
     @Override
     public String[] getMethodList() throws PostageAppException {
         Map<String, ?> data = this.getDataFromResponse(this.sendRequest(Endpoints.GET_METHOD_LIST, this.apiKeyRequestString()));
         String methods = (String) data.get("methods");
         return methods.split(", ");
     }
 
     @Override
     public AccountInfo getAccountInfo() throws PostageAppException {
         Map<String, ?> data = this.getDataFromResponse(this.sendRequest(Endpoints.GET_ACCOUNT_INFO, this.apiKeyRequestString()));
         AccountInfo accountInfo = new AccountInfo((Map<String, ?>) data.get("account"));
         return accountInfo;
     }
 
     @Override
     public ProjectInfo getProjectInfo() throws PostageAppException {
         Map<String, ?> data = this.getDataFromResponse(this.sendRequest(Endpoints.GET_PROJECT_INFO, this.apiKeyRequestString()));
         ProjectInfo projectInfo = new ProjectInfo((Map<String, ?>) data.get("project"));
         return projectInfo;
     }
 
     @Override
     public List<Message> getMessages() throws PostageAppException {
         Map<String, ?> data = this.getDataFromResponse(this.sendRequest(Endpoints.GET_MESSAGES, this.apiKeyRequestString()));
         List<Message> messages = new ArrayList<Message>();
 
         for (String messageUid : data.keySet()) {
             messages.add(new Message(messageUid, (Map<String, ?>) data.get(messageUid)));
         }
 
         return messages;
     }
 
     @Override
     public MessageTransmissonsResponse getMessageTransmissions(String messageUid) throws PostageAppException {
         Map<String, ?> data = this.getDataFromResponse(this.sendRequest(Endpoints.GET_MESSAGE_TRANSMISSIONS, this.messageUidRequestString(messageUid)));
         return new MessageTransmissonsResponse(data);
     }
 
     @Override
     public Map<String, Map<String, ProjectMetrics>> getMetrics() throws PostageAppException {
         Map<String, ?> data = this.getDataFromResponse(this.sendRequest(Endpoints.GET_METRICS, this.apiKeyRequestString()));
         Map<String, ?> metrics = (Map<String, ?>) data.get("metrics");
         Map<String, Map<String, ProjectMetrics>> metricsMap = new HashMap<String, Map<String, ProjectMetrics>>();
 
         String[] metricIntervals = new String[]{"hour", "date", "week", "month"};
 
         for (String interval : metricIntervals) {
             Map<String, ?> intervalResponseMap = (Map<String, ?>) metrics.get("interval");
 
             Map<String, ProjectMetrics> intervalMetrics = new HashMap<String, ProjectMetrics>();
 
             intervalMetrics.put("delivered", new ProjectMetrics((Map<String, String>) intervalResponseMap.get("delivered")));
             intervalMetrics.put("opened", new ProjectMetrics((Map<String, String>) intervalResponseMap.get("opened")));
             intervalMetrics.put("failed", new ProjectMetrics((Map<String, String>) intervalResponseMap.get("failed")));
             intervalMetrics.put("rejected", new ProjectMetrics((Map<String, String>) intervalResponseMap.get("rejected")));
             intervalMetrics.put("created", new ProjectMetrics((Map<String, String>) intervalResponseMap.get("created")));
             intervalMetrics.put("queued", new ProjectMetrics((Map<String, String>) intervalResponseMap.get("queued")));
 
             metricsMap.put(interval, intervalMetrics);
         }
 
         return metricsMap;
     }
 
     // Since these requests are so small, don't bother creating builders / params for them
     private String apiKeyRequestString() {
         Map<String, String> params = new HashMap<String, String>();
         params.put("apiKey", this.apiKey);
         return new Gson().toJson(params);
     }
 
     private String messageUidRequestString(String messageUid) {
         Map<String, String> params = new HashMap<String, String>();
         params.put("apiKey", this.apiKey);
         params.put("uid", messageUid);
         return new Gson().toJson(params);
     }
 
     private String sendRequest(String endpoint, String content) throws PostageAppException {
         URIBuilder uriBuilder = new URIBuilder()
                 .setScheme("https")
                 .setHost(API_HOST)
                 .setPath(this.apiVersion + '/' + endpoint);
 
         try {
             return this.httpClient.post(uriBuilder.build().toString(), content);
         } catch (URISyntaxException e) {
             e.printStackTrace();
         }
 
         return null;
     }
 
     private Map<String, ?> getDataFromResponse(String responseString) throws PostageAppException {
         Type mapType = new TypeToken<Map<String, ?>>() {
         }.getType();
         Map<String, ?> responseMap = this.gson.fromJson(responseString, mapType);
         Map<String, ?> response = (Map<String, ?>) responseMap.get("response");
 
         this.checkForError(response);
 
         return (Map<String, ?>) response.get("data");
     }
 
     private void checkForError(Map<String, ?> responseJson) throws PostageAppException {
         String responseStatus = (String) responseJson.get("status");
 
         if (!responseStatus.equalsIgnoreCase("ok")) {
             PostageAppException exception = new PostageAppException((String) responseJson.get("message"));
             exception.setMessageUid((String) responseJson.get("uid"));
             exception.setStatus((String) responseJson.get("status"));
             throw exception;
         }
     }
 }
