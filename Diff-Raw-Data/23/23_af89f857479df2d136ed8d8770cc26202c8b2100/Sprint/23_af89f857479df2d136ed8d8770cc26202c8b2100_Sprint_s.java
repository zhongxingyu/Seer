 package io.blitz.curl;
 
 import io.blitz.curl.exception.ValidationException;
 import io.blitz.curl.sprint.ISprintListener;
 import io.blitz.curl.sprint.Request;
 import io.blitz.curl.sprint.Response;
 import io.blitz.curl.sprint.SprintResult;
 import io.blitz.curl.sprint.Step;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Map;
 
 /**
  * Used to generate a Sprint Test.
  * <p> 
  * Sprinting is a simple HTTP (or SSL) request to a page in your app or 
  * your RESTful API.
  * </p>
  * <pre>
  * Sprint sprint = new Sprint("you@example.com", "my-bitz-api-key");
  * Collection<TestStep> steps = new ArrayList<TestStep>();
  * steps.add(new TestStep(new URL("http://your.cool.app")));
  * sprint.setSteps(steps);
  * sprint.addListener(new ISprintListener() {
 *      booolean onData(SprintResult result) {
 *          //do something...
 *      }
  * });
  * sprint.execute();
  * </pre>
  * @author ghermeto
  * @version 0.1.0
  * @see io.blitz.curl.AbstractTest
  */
 public class Sprint extends AbstractTest<ISprintListener, SprintResult> {
 
     public Sprint(String username, String apiKey) {
         setCredentials(username, apiKey);
     }
 
     public Sprint(String username, String apiKey, String host, Integer port) {
         setCredentials(username, apiKey, host, port);
     }
 
     /**
      * Verifies the Sprint requirements. Should throw a 
      * <code>ValidationException</code> if the URL field is not set.
      * @throws ValidationException 
      */
     @Override
     public void checkRequirements() throws ValidationException {
         if (getSteps() == null) {
             throw new ValidationException("At least one step is required");
         }
         for (TestStep step : getSteps()) {
             if(step.getUrl() == null) {
                 throw new ValidationException("Url is required");
             }
         }
     }
 
     /**
      * Should return a <code>SprintResult</code> object populated with the 
      * successful response from the server.
      * @param result the deserialized result from the JSON response
      * @return a successful sprint result object
      * @see SprintResult
      */
     protected SprintResult createSuccessResult(Map<String, Object> result) {
         
         String region = (String) result.get("region");
         Number duration = (Number) result.get("duration");
 
         Double durationDbl = (duration != null) ? duration.doubleValue() : null;
         
         Collection<Step> steps = new ArrayList<Step>();
         Collection<?> list = (Collection<?>) result.get("steps");
         if (list != null) {
             for(Object obj : list) {
                 Map<String, Object> item = (Map<String, Object>) obj;
                 Number itemDuration = (Number) item.get("duration");
                 Number itemConnect = (Number) item.get("connect");
                 //assemble the request object
                 Request request = null;
                 if(item.containsKey("request")) {
                     Map<String, Object> req = 
                             (Map<String, Object>) item.get("request"); 
                     String line = (String) req.get("line");
                     String method = (String) req.get("method");
                     String url = (String) req.get("url");
                     String content = (String) req.get("content");
                     Map<String, Object> headers = (req.containsKey("headers")) ?
                             (Map<String, Object>) req.get("headers") : null;
 
                     request = new Request(line, method, url, headers, content);
                 }
                 //assemble the response object
                 Response response = null;
                 if(item.containsKey("response")) {
                     Map<String, Object> res = 
                             (Map<String, Object>) item.get("response"); 
                     String line = (String) res.get("line");
                     Number status = (Number) res.get("status");
                     String message = (String) res.get("message");
                     String content = (String) res.get("content");
                     Map<String, Object> headers = (res.containsKey("headers")) ?
                             (Map<String, Object>) res.get("headers") : null;
 
                     response = new Response(line, (status!=null) ? status.intValue() : null, 
                             message, headers, content);
                 }
                 Double itemDurationDbl = (itemDuration != null) ? 
                         itemDuration.doubleValue() : null;
                 Double itemConnectDbl = (itemConnect != null) ? 
                         itemConnect.doubleValue() : null;
                 
                 Step step = new Step(itemDurationDbl, itemConnectDbl, request, response);
                 steps.add(step);
             }
         }
         return new SprintResult(region, durationDbl, steps);
     }
 }
