 package com.outr.net.communicator.client.connection;
 
 import com.google.gwt.http.client.*;
 import com.outr.net.communicator.client.GWTCommunicator;
 import com.outr.net.communicator.client.JSONConverter;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * @author Matt Hicks <matt@outr.com>
  */
 public class AJAXConnection implements Connection {
     private final ConnectionManager manager;
     private final RequestBuilder pollBuilder;
     private final RequestBuilder sendBuilder;
 
     private Request pollRequest;
     private Request sendRequest;
     private RequestCallback pollCallback = new RequestCallback() {
         @Override
         public void onResponseReceived(Request request, Response response) {
             try {
                 if (response.getStatusCode() == 200) {
                     pollRequest = null;
 //                GWTCommunicator.log("Received: " + response.getText());
                     AJAXResponse r = (AJAXResponse)JSONConverter.fromString(response.getText());
                     if (r.status) {
                         for (int i = 0; i < r.data.size(); i++) {
                             Message message = r.data.get(i);
                             manager.received(message);
                         }
 
                         connectPolling();           // Reconnect polling
                     } else {
                         pollError("Status was failure: " + r.failure, r);
                     }
                 } else {
                     String message = null;
                     if (response.getStatusCode() != 0) {
                         message = "Bad Response: " + response.getStatusText() + " (" + response.getStatusCode() + ")";
                     }
                     pollError(message, null);
                     if (response.getStatusCode() == 404) {      // Resource not found, so reload the browser
                         manager.communicator.reload(true, true, 0);
                     }
                 }
             } catch(Throwable t) {
                pollError("Exception thrown on poll receive (" + t.getMessage() + "). Status: " + response.getStatusText() + " (" + response.getStatusCode() + "). Message: " + response.getText(), null);
             }
         }
 
         @Override
         public void onError(Request request, Throwable exception) {
             pollError(exception.getMessage(), null);
         }
     };
     private RequestCallback sendCallback = new RequestCallback() {
         @Override
         public void onResponseReceived(Request request, Response response) {
             try {
                 if (response.getStatusCode() == 200) {
                     AJAXResponse r = (AJAXResponse)JSONConverter.fromString(response.getText());
                     if (r.status) {             // Successful, lets check the queue for more to send
                         sendRequest = null;
                         manager.queue.confirm();    // Messages sent successfully
                         sendData();
                     } else {
                         sendError("Status was failure: " + r.failure, r);
                     }
                 } else {
                     sendError("Bad Response: " + response.getStatusText() + " (" + response.getStatusCode() + ")", null);
                 }
             } catch(Throwable t) {
                 sendError("Exception thrown on send. Status: " + response.getStatusText() + " (" + response.getStatusCode() + "). Message: " + response.getText(), null);
             }
         }
 
         @Override
         public void onError(Request request, Throwable exception) {
             sendError(exception.getMessage(), null);
         }
     };
 
     public AJAXConnection(ConnectionManager manager, String ajaxURL) {
         this.manager = manager;
         pollBuilder = new RequestBuilder(RequestBuilder.POST, ajaxURL);
         pollBuilder.setTimeoutMillis(60000);
         sendBuilder = new RequestBuilder(RequestBuilder.POST, ajaxURL);
         sendBuilder.setTimeoutMillis(10000);
     }
 
     @Override
     public void connect() {
         connectPolling();
         sendData();
     }
 
     private void connectPolling() {
         Map<String, Object> map = new HashMap<String, Object>();
         map.put("id", manager.uuid);
         map.put("type", "receive");
         map.put("lastReceiveId", manager.getLastReceiveId());
         String json = JSONConverter.toJSONValue(map).toString();
         try {
             pollRequest = pollBuilder.sendRequest(json, pollCallback);
         } catch(RequestException exc) {
             log("PollingRequestError: " + exc.getMessage());
         }
     }
 
     private void sendData() {
         if (sendRequest == null && manager.queue.hasNext()) {
             Map<String, Object> map = new HashMap<String, Object>();
             map.put("id", manager.uuid);
             map.put("type", "send");
             List<Message> messages = new ArrayList<Message>(manager.queue.waiting());
             while (manager.queue.hasNext()) {
                 messages.add(manager.queue.next());
             }
             map.put("messages", messages);
 
             String json = JSONConverter.toJSONValue(map).toString();
             try {
                 sendRequest = sendBuilder.sendRequest(json, sendCallback);
             } catch(RequestException exc) {
                 log("SendRequestError: " + exc.getMessage());
             }
         }
     }
 
     @Override
     public void messageReady() {
         sendData();
     }
 
     private void pollError(String error, AJAXResponse response) {
         if (error != null) {
             log("Error received from poll: " + error);
         }
         manager.disconnected();
         responseError(response);
     }
 
     private void sendError(String error, AJAXResponse response) {
         log("Error received from send: " + error);
         manager.queue.failed();
         manager.disconnected();
         responseError(response);
     }
 
     private void responseError(AJAXResponse response) {
         if (response != null && response.failure != null) {
             manager.handleError(response.failure.error);
         }
     }
 
     public void update(int delta) {
     }
 
     private void log(String message) {
         GWTCommunicator.log(message);
     }
 }
