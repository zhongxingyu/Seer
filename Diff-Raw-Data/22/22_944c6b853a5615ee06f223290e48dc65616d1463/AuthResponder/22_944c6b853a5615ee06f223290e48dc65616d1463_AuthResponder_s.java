 package no.knubo.accounting.client.misc;
 
 import no.knubo.accounting.client.Constants;
 import no.knubo.accounting.client.I18NAccount;
 import no.knubo.accounting.client.Util;
 
 import com.google.gwt.http.client.Request;
 import com.google.gwt.http.client.RequestCallback;
 import com.google.gwt.http.client.Response;
 import com.google.gwt.user.client.Window;
 
 public class AuthResponder implements RequestCallback {
 
     private final Constants constants;
     private final ServerResponse callback;
     private final I18NAccount messages;
 
    public AuthResponder(Constants constants, I18NAccount messages, ServerResponse callback) {
         this.constants = constants;
         this.messages = messages;
         this.callback = callback;
 
     }
 
     public void onError(Request request, Throwable exception) {
         /* Not needed? */
     }
 
     public void onResponseReceived(Request request, Response response) {
         if (response.getStatusCode() == 510) {
             Util.forward(constants.loginURL());
         } else if (response.getStatusCode() == 511) {
             Window.alert(messages.no_access());
         } else {
             String data = response.getText();
            if(data == null || data.length() == 0) {
                 Window.alert(messages.no_server_response());
                 return;
             }
             data = data.trim();
             callback.serverResponse(data);
         }
     }
 
 }
