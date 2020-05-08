 package io.blitz.curl;
 
 import io.blitz.curl.exception.AuthenticationException;
 import io.blitz.curl.exception.BlitzException;
 import io.blitz.curl.exception.ValidationException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Map;
 
 /**
  * Abstract class that templates the core tests execution
  * @author ghermeto
  */
public abstract class AbstractTest<Listener extends IListener, Result> 
    extends TestEntity implements IObservable<Listener> {
     
     
     /**
      * Transient property used to connect the client when necessary. 
      * Not meant to be serialized.
      */
     private transient String username;
     
     /**
      * Transient property used to connect the client when necessary. 
      * Not meant to be serialized.
      */
     private transient String apiKey;
     
     /**
      * Transient property used to connect the client when necessary. 
      * Not meant to be serialized.
      */
     private transient String host;
     
     /**
      * Transient property used to connect the client when necessary. 
      * Not meant to be serialized.
      */
     private transient Integer port;
     
     /**
      * Connects with the server and handle the request/response.
      * Not meant to be serialized.
      */
     private transient Client client;
     
     /**
      * Transient property to be used during a test execution. 
      * Not meant to be serialized.
      */
     private transient String jobId;
     
     /**
      * Listener to be notified when the <code>Client</code> send a response.
      * Will not be serialized into the json.
      */
     private transient Collection<Listener> listeners;
     
     /**
      * Adds a listener to the test which will fire when the test receives a 
      * status response from the client. The execute method does not invoke the
      * listeners. When on error, will throw an exception and on success will
      * update jobId.
      * @param listner <code>IListener</code> to be notified
      * @see io.blitz.curl.IObservable
      */
     public void addListener(Listener listner) {
         if (listeners == null) {
            listeners = new ArrayList<Listener>(); 
         }
         listeners.add(listner);
     }
 
     /**
      * Removes the given listener from the list.
      * @param listner <code>IListener</code> to be removed from the list
      * @see io.blitz.curl.IObservable
      */
     public void removeListener(Listener listner) {
         listeners.remove(listner);
     }
     
     /**
      * This method verifies the test requirements and throws a 
      * <code>ValidationException</code> when necessary.
      * @throws io.blitz.curl.exception.ValidationException
      * @see io.blitz.curl.Sprint#checkRequirements() 
      * @see io.blitz.curl.Rush#checkRequirements() 
      */
     public abstract void checkRequirements() throws ValidationException;
     
     /**
      * Executes the test. During its execution it will receive updates from the 
      * <code>Client</code> and will notify all attached listeners.
      */
     public void execute() {
         //specific for each subclass
         checkRequirements();
         //handle the client creation
         createClientInstance();
         //handle client authentication
         if(!client.isAuthenticated()) {
             Map<String, Object> response = client.login();
             if (response.containsKey("error")) {
                 String error = (String) response.get("error");
                 String reason = (String) response.get("reason");
                 notifyError(error, reason);
                 return;
             }
         }
         //after authentication, we send this job to the server (excluding transient)
         Map<String, Object> response = client.execute(this);
         
         if(response.containsKey("error")) {
             String error = (String) response.get("error");
             String reason = (String) response.get("reason");
             notifyError(error, reason);
             return;
         }
         
         jobId = (String) response.get("job_id");
         checkStatus();
     }
     
     /**
      * Notify all listneres about an error response from blitz. It calls  
      * <code>onError</code> on all listeners.
      * @param error
      * @param reason 
      */
     protected void notifyError(String error, String reason) {
         if(listeners != null) {
             ErrorResult result = new ErrorResult(error, reason);
             for(Listener listener : listeners) {
                 listener.onError(result);
             }
         }
     }
     
     /**
      * Notify all listneres about an error response from blitz. It calls  
      * <code>onSuccess</code> on all listeners.
      * @param result the result object from the JSON response 
      */
     protected void notifySuccess(Map<String, Object> result) {
         if(listeners != null) {
             Result success = createSuccessResult(result);
             for(Listener listener : listeners) {
                 listener.onSuccess(success);
             }
         }
     }
     
     /**
      * Should return a <code>Result</code> object populated with the 
      * successful response from the server.
      * @param result the deserialized result from the JSON response
      * @see io.blitz.curl.Sprint#createSuccessResult(java.util.Map) 
      * @see io.blitz.curl.Rush#createSuccessResult(java.util.Map) 
      */
     protected abstract Result createSuccessResult(Map<String, Object> result);
 
     /**
      * Verifies if the client instance was created and tries to create a new 
      * instance if needed.
      */
     protected void createClientInstance() {
         if (client == null) {
             if (username == null || apiKey == null) {
                 throw new AuthenticationException("No credentials");
             }
             else if(host != null && port != null) {
                 client = new Client(username, apiKey, host, port);
             }
             else {
                 client = new Client(username, apiKey);
             }
         }
     }
     
     /**
      * Checks the current job status and notify the listeners about errors and 
      * sucessful responses from the server.
      */
     public void checkStatus() {
         try {
             do {
                 Thread.sleep(2000);
 
                 Map<String, Object> job = client.getJobStatus(jobId);
                 Map<String, Object> result = getResult(job);
                 String status = (String) job.get("status");
 
                 if(job == null) {
                     throw new BlitzException("client", "No response.");
                 }
                 //if no result was issued yet (nothing to notify)
                 else if("queued".equalsIgnoreCase(status) || 
                     ("running".equalsIgnoreCase(status) && result == null)) {
                     
                     continue;
                 }
                 //if the server retuned an error
                 else if(job.containsKey("error")) {
                     String error = (String) job.get("error");
                     String reason = (String) job.get("reason");
                     notifyError(error, reason);
                     break;
                 }
                 //if the result was an error
                 else if(result != null && result.containsKey("error")) {
                     String error = (String) result.get("error");
                     String reason = (String) result.get("reason");
                     notifyError(error, reason);
                     break;
                 }
                 //notify the listeners that a successful status was acquired
                 notifySuccess(result);
                 
                 if("completed".equalsIgnoreCase(status)) {
                     break;
                 }
             }while(true);
             
         } catch (InterruptedException ex) {
             throw new BlitzException("client", ex.getLocalizedMessage());
         }
     }
     
     /**
      * Verifies if the job has a result map and returns it.
      * @param job map response from the job status
      * @return the job result map or null
      */
     private Map<String, Object> getResult(Map<String, Object> job) {
         if(job != null && job.get("result") != null) {
             Object obj = job.get("result");
             if(Map.class.isAssignableFrom(obj.getClass())) {
                 return (Map<String, Object>) obj;
             }
         }
         return null;
     }
     
     /**
      * Stores the user credentials to be used by the <code>Client</code>. The user
      * credentials are available at http://blitz.io on the Settings page or it can
      * be found by typing <code>--api-key</code> on the blitz play bar.
      * @param username blitz.io username
      * @param apiKey blitz.io authentication api key
      */
     protected void setCredentials(String username, String apiKey) {
         this.username = username;
         this.apiKey = apiKey;
     }
 
     /**
      * Stores the user credentials to be used by the <code>Client</code>. The user
      * credentials are available at http://blitz.io on the Settings page or it can
      * be found by typing <code>--api-key</code> on the blitz play bar.
      * @param username blitz.io username
      * @param apiKey blitz.io authentication api key
      * @param host the host to connect
      * @param port the port to connect
      */
     protected void setCredentials(String username, String apiKey, String host, Integer port) {
         this.username = username;
         this.apiKey = apiKey;
         this.host = host;
         this.port = port;
     }
     
     /**
      * Aborts the current job. Sending a abort request doesn't guarantee that 
      * the job will stop immediately.
      * @return true if the server accepts the request
      */
    protected boolean abort() {
         Map<String, Object> job = client.abort(jobId);
         return job != null && job.containsKey("ok");
     }
 }
