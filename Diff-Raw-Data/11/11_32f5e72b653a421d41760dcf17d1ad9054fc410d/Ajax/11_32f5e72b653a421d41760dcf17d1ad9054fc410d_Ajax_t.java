 package nl.sense_os.commonsense.client.ajax;
 
 import java.util.HashMap;
 
 import com.extjs.gxt.ui.client.event.EventType;
 import com.extjs.gxt.ui.client.mvc.AppEvent;
 
 public class Ajax {
 
     private static final String TAG = "Ajax";
 
     /**
      * Does cross-domain request using the JSNI to make this work for IE. Calls back to
      * {@link AjaxController#onSuccess(String, EventType)} after the request is complete, or to
      * {@link AjaxController#onFailure()} or {@link AjaxController#onAuthError()} if something went
      * wrong.
      * 
      * @param method
      *            HTTP method
      * @param url
      * @param sessionId
      *            Optional session ID for authentication. Will be sent as X-SESSION_ID header (or as
      *            URL parameter in IE)
      * @param body
      *            String with optional body for the request (e.g. for POST or PUT requests)
      * @param params
      *            HTTP parameters
      * @param onSuccess
      *            AppEvent to dispatch after the request is complete
      * @param onFailure
      *            AppEvent to dispatch if the request fails
      * @param handler
      *            AjaxController instance to return the ajax result to
      * 
      * @see <a href="http://goo.gl/ajJWN">Making cross domain JavaScript requests</a>
      */
     // @formatter:off
     public static native void request(
     		String method,
     		String url,
             String sessionId,
     		String body,
     		HashMap<String, Object> params,
             AppEvent onSuccess,
             AppEvent onFailure,
     		AjaxController handler) /*-{
 
 		var isIE8 = window.XDomainRequest ? true : false;
 		var xhr = createCrossDomainRequest();
 
 		function createCrossDomainRequest() {
 			return (isIE8) ? new window.XDomainRequest() : new XMLHttpRequest();
 		}
 
 		function readyStateHandler() {
 			if (xhr.readyState == 4) {
 				if ((xhr.status >= 200) && (xhr.status < 300)) {
 					handleSuccess();
 				} else if (xhr.status == 403) {
 					handleAuthError();
 				} else {
 					handleFailure();
 				}
 			}
 		}
 
 		// NB: this is only called by Chrome, other browsers do not give the status code of failed requests
 		function handleAuthError() {
 			handler.@nl.sense_os.commonsense.client.ajax.AjaxController::onAuthError(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ILcom/extjs/gxt/ui/client/mvc/AppEvent;)(method, url, sessionId, body, xhr.status, onFailure);
 		}
 
 		function handleFailure() {
 			handler.@nl.sense_os.commonsense.client.ajax.AjaxController::onFailure(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ILcom/extjs/gxt/ui/client/mvc/AppEvent;)(method, url, sessionId, body, xhr.status, onFailure);
 		}
 
 		function handleSuccess() {
 			handler.@nl.sense_os.commonsense.client.ajax.AjaxController::onSuccess(Ljava/lang/String;Lcom/extjs/gxt/ui/client/mvc/AppEvent;)(xhr.responseText, onSuccess);
 		}
 
 		if (xhr) {
 			if (isIE8) {
 				// IE does not support custom headers: add session ID to URL parameters 
 				if (undefined != sessionId) {
 					url = url + "?session_id=" + sessionId;
 				}
 
 				// IE does not support DELETE or PUT requests: use custom URL parameter
 				if ("DELETE" === method || "PUT" === method) {
 					url = url + "&_METHOD=" + method;
					method = "GET";
 				}

				xhr.open(method, url);
				xhr.onload = handleSuccess;
				xhr.onerror = handleFailure;
 				xhr.send(body);
 			} else {
 				xhr.open(method, url, true);
 				xhr.onreadystatechange = readyStateHandler;
 				if (undefined != sessionId) {
 					xhr.setRequestHeader("X-SESSION_ID", sessionId);
 				}
 				xhr.send(body);
 			}
 		} else {
 			handleFailure();
 		}
     }-*/;
     // @formatter:on
 }
