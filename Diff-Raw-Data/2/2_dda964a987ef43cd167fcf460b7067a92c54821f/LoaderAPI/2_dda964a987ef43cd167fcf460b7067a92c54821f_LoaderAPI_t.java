 package io.loader.jenkins.api;
 
 import java.io.IOException;
 import java.io.PrintStream;
 import java.net.URI;
 import java.util.Map;
 import java.util.HashMap;
 
 import net.sf.json.JSONException;
 import net.sf.json.JSONSerializer;
 import net.sf.json.JSONObject;
 import net.sf.json.JSONArray;
 import net.sf.json.JSON;
 
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPut;
 import org.apache.http.client.methods.HttpRequestBase;
 import org.apache.http.util.EntityUtils;
 import org.apache.http.HttpResponse;
 
 public class LoaderAPI {
     static final String baseApiUri = "https://api.loader.io/v2/";
 
     PrintStream logger = new PrintStream(System.out);
     String apiKey;
 
     public LoaderAPI(String apiKey) {
         logger.println("in #LoaderAPI, apiKey: " + apiKey);
         this.apiKey = apiKey;
     }
 
     public Map<String, String> getTestList() {
         JSONArray list = getTests();
         if (list == null) {
             return null;
         }
         Map<String, String> tests = new HashMap<String, String>();
         for (Object test : list) {
             JSONObject t = (JSONObject) test;
             String title = prepareTestTitle(t);
             tests.put(t.getString("test_id"), title);
         }
         return tests;
     }
 
     protected String prepareTestTitle(JSONObject test) {
         String id = "";
         try {
             id = test.getString("test_id");
             String title = test.getString("name");
             String domain = test.getString("domain");
             String asTitle = isEmptyString(title) ? domain : title;
             return String.format("%s (%s)", asTitle, id);
         } catch (RuntimeException ex) {
             logger.println("Got Exception: " + ex);
             return id;
         }
     }
 
     protected boolean isEmptyString(String string) {
         return string == null || string.trim().isEmpty();
     }
 
     public JSONArray getApps() {
         logger.println("in #getApps");
         return getListData("apps");
     }
 
     public JSONArray getTests() {
         logger.println("in #getTests");
         return getListData("tests?status=active&fields[]=name&fields[]=domain");
     }
 
     private JSONArray getListData(String path) {
         Result result = doGetRequest(path);
         logger.println("Result (max 1000 symbols):::" + result.code + "\n" +
             (result.body.length() > 1000 ? result.body.substring(0, 1000) : result.body));
         if (result.isFail()) {
             return null;
         }
         try {
             JSON list = JSONSerializer.toJSON(result.body);
             if (list.isArray()) {
                 return (JSONArray) list;
             } else {
                 return null;
             }
         } catch (RuntimeException ex) {
             logger.println("Got Exception: " + ex);
             return null;
         }
     }
 
     public TestData getTest(String testId) {
         logger.println("in #getTest");
         Result result = doGetRequest("tests/" + testId);
         logger.println("Result :::" + result.code + "\n" + result.body);
         if (result.isFail()) {
             return null;
         }
         return fetchTestData(result.body);
 
     }
 
     private TestData fetchTestData(String data) {
         try {
             JSONObject json = (JSONObject) JSONSerializer.toJSON(data);
             return new TestData(json);
         } catch (RuntimeException ex) {
             logger.format("Got exception: %s", ex);
             return null;
         }
     }
 
     public String runTest(String testId) {
         logger.println("in #getTests");
        Result result = doPutRequest("tests/" + testId + "/run?source=jenkins");
         logger.println("Result :::" + result.code + "\n" + result.body);
         if (result.isFail()) {
             return null;
         }
         //TODO: check on exception
         JSONObject body = (JSONObject) JSONSerializer.toJSON(result.body);
         return body.getString("result_id");
     }
 
     public SummaryData getTestSummaryData(String testId, String summaryId) {
         logger.println("in #getTestSummaryData");
         Result result = doGetRequest("tests/" + testId + "/results/" + summaryId);
         logger.println("Result :::" + result.code + "\n" + result.body);
         if (result.isFail()) {
             return null;
         }
         //TODO: check on exception
         JSONObject json = (JSONObject) JSONSerializer.toJSON(result.body);
         return new SummaryData(json);
     }
 
     public boolean isValidApiKey() {
         if (isEmptyString(apiKey)) {
             logger.println("getTestApi apiKey is empty");
             return false;
         }
         JSON apps = getApps();
         if (null == apps) {
             logger.println("invalid ApiKey");
             return false;
         }
         return true;
     }
 
     private Result doGetRequest(String path) {
         return doRequest(new HttpGet(), path);
     }
 
     private Result doPutRequest(String path) {
         return doRequest(new HttpPut(), path);
     }
 
     private Result doRequest(HttpRequestBase request, String path) {
         stuffHttpRequest(request, path);
         DefaultHttpClient client = new DefaultHttpClient();
         HttpResponse response;
         try {
             response = client.execute(request);
         } catch (IOException ex) {
             logger.format("Error during remote call to API. Exception received: %s", ex);
             return new Result("Network error during remote call to API");
         }
         return new Result(response);
     }
 
     private void stuffHttpRequest(HttpRequestBase request, String path) {
         URI fullUri = null;
         try {
             fullUri = new URI(baseApiUri + path);
         } catch (java.net.URISyntaxException ex) {
             throw new RuntimeException("Incorrect URI format: %s", ex);
         }
         request.setURI(fullUri);
         request.addHeader("Content-Type", "application/json");
         request.addHeader("loaderio-Auth", apiKey);
     }
 
     static class Result {
         public int code;
         public String errorMessage;
         public String body;
 
         static final String badResponseError = "Bad response from API.";
         static final String formatError = "Invalid error format in response.";
 
         public Result(String error) {
             code = -1;
             errorMessage = error;
         }
 
         public Result(HttpResponse response) {
             code = response.getStatusLine().getStatusCode();
             try {
                 body = EntityUtils.toString(response.getEntity());
             } catch (IOException ex) {
                 code = -1;
                 errorMessage = badResponseError;
             }
             //TODO: add setup of error message depending on status code
             //      500, 404, etc
             if (code != 200) {
                 errorMessage = getErrorFromJson(body);
             }
         }
 
         public boolean isOk() {
             return 200 == code;
         }
 
         public boolean isFail() {
             return !isOk();
         }
 
         // format sample:
         // {"message":"error","errors":["wrong api key(xxx)"]}
         private String getErrorFromJson(String json) {
             // parse json
             JSON object;
             try {
                 object = JSONSerializer.toJSON(json);
             } catch (JSONException ex) {
                 return formatError;
             }
             if (!(object instanceof JSONObject)) {
                 return formatError;
             }
             StringBuilder error = new StringBuilder(badResponseError);
             //TODO: check on error
             for (Object message : ((JSONObject) object).getJSONArray("errors")) {
                 error.append(message.toString());
             }
             return error.toString();
         }
     }
 }
