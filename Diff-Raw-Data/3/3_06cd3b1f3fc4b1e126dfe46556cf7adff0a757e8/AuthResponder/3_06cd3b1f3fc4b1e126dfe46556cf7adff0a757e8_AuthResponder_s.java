 package no.knubo.accounting.client.misc;
 
 import java.util.ArrayList;
 
 import no.knubo.accounting.client.AccountingGWT;
 import no.knubo.accounting.client.Constants;
 import no.knubo.accounting.client.Elements;
 import no.knubo.accounting.client.I18NAccount;
 import no.knubo.accounting.client.Util;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.http.client.Request;
 import com.google.gwt.http.client.RequestBuilder;
 import com.google.gwt.http.client.RequestCallback;
 import com.google.gwt.http.client.RequestException;
 import com.google.gwt.http.client.Response;
 import com.google.gwt.json.client.JSONArray;
 import com.google.gwt.json.client.JSONParser;
 import com.google.gwt.json.client.JSONValue;
 import com.google.gwt.user.client.DOM;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.DialogBox;
 import com.google.gwt.user.client.ui.HTML;
 
 public class AuthResponder implements RequestCallback {
 
     private final Constants constants;
     private final ServerResponse callback;
     private final I18NAccount messages;
     private final Logger logger;
     private static boolean noDB;
 
     private AuthResponder(Constants constants, I18NAccount messages, ServerResponse callback) {
         this.constants = constants;
         this.messages = messages;
         this.callback = callback;
         if (callback == null) {
             throw new RuntimeException("Callback cannot be null");
         }
         this.logger = new Logger(this.constants);
         AccountingGWT.setLoading();
     }
 
     public void onError(Request request, Throwable exception) {
         /* Not needed? */
     }
 
     public void onResponseReceived(Request request, Response response) {
         AccountingGWT.setDoneLoading();
         if (response.getStatusCode() == 510) {
             Util.forward(constants.loginURL());
         } else if (response.getStatusCode() == 511) {
             Window.alert(messages.no_access());
         } else if (response.getStatusCode() == 512) {
             logger.error("database", response.getText());
             Elements elements = (Elements) GWT.create(Elements.class);
 
             ErrorReportingWindow.reportError(elements.error_database(), response.getText());
         } else if (response.getStatusCode() == 513) {
             JSONValue parse = JSONParser.parse(response.getText());
 
             ArrayList<String> fields = new ArrayList<String>();
             JSONArray array = parse.isArray();
 
             for (int i = 0; i < array.size(); i++) {
                 fields.add(Util.str(array.get(i)));
             }
 
             if (callback instanceof ServerResponseWithValidation) {
                 ((ServerResponseWithValidation) callback).validationError(fields);
             } else {
                 ErrorReportingWindow.reportError("Validation error of fields:" + fields,
                         "Uncought (bug) validation error:" + fields);
                 Window.alert("Validation error:" + fields);
             }
         } else if (response.getStatusCode() == 514) {
             String data = response.getText();
 
             new MissingDataPopup(data).center();
         } else if (response.getStatusCode() == 515) {
             handleNODB();
         } else if (response.getStatusCode() == 516) {
             handleClosedSite(response.getText());
         } else {
             String data = response.getText();
             if (data == null || data.length() == 0) {
                 logger.error("error", "no server response");
                 return;
             }
             data = data.trim();
 
             if (callback instanceof ServerResponseString) {
                 ServerResponseString srs = (ServerResponseString) callback;
                 srs.serverResponse(data);
                 return;
             }
 
             JSONValue jsonValue = null;
 
             try {
                 jsonValue = JSONParser.parse(data);
             } catch (Exception e) {
                 Elements elements = (Elements) GWT.create(Elements.class);
                 ErrorReportingWindow.reportError(elements.error_bad_return_data(), e.toString());
 
                 /* We catch this below in bad return data */
             }
 
             if (jsonValue == null) {
                 if (callback instanceof ServerResponseWithErrorFeedback) {
                     ((ServerResponseWithErrorFeedback) callback).onError();
                 } else {
                     // logger.error("baddata", data);
                     // Window.alert("Bad return data:" + data);
                 }
             } else {
                 try {
                     callback.serverResponse(jsonValue);
                 } catch (Exception e) {
                     Elements elements = (Elements) GWT.create(Elements.class);
 
                     ErrorReportingWindow.reportError(elements.error_uncought_exception(), e.toString());
                     Util.log(e.toString());
                     throw new RuntimeException(e);
                 }
             }
         }
     }
 
     private void handleClosedSite(String string) {
        noDB = true;
         if (!noDB) {
             DialogBox dialogBox = new DialogBox();
             DOM.setElementAttribute(dialogBox.getElement(), "id", "closed_site");
 
             HTML html = new HTML(string);
             dialogBox.setWidget(html);
             dialogBox.center();
         }
     }
 
     private void handleNODB() {
         if (!noDB) {
             noDB = true;
             Window.alert(messages.no_db_connection());
         }
     }
 
     public static void getExternal(Constants constants, I18NAccount messages, ServerResponse callback, String url) {
         RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, url);
         builder.setHeader("Content-Type", "text/plain;charset=utf-8");
 
         try {
             builder.sendRequest("", new AuthResponder(constants, messages, callback));
         } catch (RequestException e) {
 
             ErrorReportingWindow.reportError(e.getMessage(), e.toString());
         }
 
     }
 
     public static void get(Constants constants, I18NAccount messages, ServerResponse callback, String url) {
         RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, constants.baseurl() + url);
 
         try {
             builder.sendRequest("", new AuthResponder(constants, messages, callback));
         } catch (RequestException e) {
 
             ErrorReportingWindow.reportError(e.getMessage(), e.toString());
         }
     }
 
     public static void post(Constants constants, I18NAccount messages, ServerResponse callback,
             StringBuffer parameters, String url) {
 
         RequestBuilder builder = new RequestBuilder(RequestBuilder.POST, constants.baseurl() + url);
 
         try {
             builder.setHeader("Content-Type", "application/x-www-form-urlencoded");
             builder.sendRequest(parameters.toString(), new AuthResponder(constants, messages, callback));
         } catch (RequestException e) {
 
             ErrorReportingWindow.reportError(e.getMessage(), e.toString());
         }
 
     }
 
 }
