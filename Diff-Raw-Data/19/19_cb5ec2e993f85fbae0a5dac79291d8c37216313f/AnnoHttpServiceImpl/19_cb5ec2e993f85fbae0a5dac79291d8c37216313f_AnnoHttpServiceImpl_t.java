 package co.usersource.annoplugin.sync;
 
 import java.io.IOException;
 import java.net.URLEncoder;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.http.ParseException;
 import org.json.JSONObject;
 
 import android.accounts.Account;
 import android.content.Context;
 import android.util.Log;
 import android.widget.Toast;
 import co.usersource.annoplugin.network.HttpConnector;
 import co.usersource.annoplugin.network.IHttpConnectorAuthHandler;
 import co.usersource.annoplugin.network.IHttpRequestHandler;
 import co.usersource.annoplugin.utils.AccountUtils;
 import co.usersource.annoplugin.utils.SystemUtils;
 
 public class AnnoHttpServiceImpl implements AnnoHttpService {
 
   private HttpConnector httpConnector;
   private Context context;
   private Account account;
   private static final String BASE_URL_COMMUNITY = "/community";
   private static final String TAG = AnnoHttpServiceImpl.class.getSimpleName();
 
   public AnnoHttpServiceImpl(Context context) {
     this.context = context;
     httpConnector = new HttpConnector();
   }
 
   private class AnnoResponseHandler implements IHttpRequestHandler {
 
     private ResponseHandler handler;
 
     public AnnoResponseHandler(ResponseHandler handler) {
       this.handler = handler;
     }
 
     @Override
     public void onRequest(JSONObject response) {
       handler.handleResponse(response);
     }
 
   }
 
   private void execute(final IHttpExecution execution,
       final Map<String, Object> input, ResponseHandler handler) {
     if (!SystemUtils.isOnline(context)) {
       Log.i(TAG, "Device is offline, won't synchronize.");
       Exception e = new Exception("No network connectivity.");
       handler.handleError(e);
       return;
     }
     if (account == null) {
       Account[] accounts = AccountUtils.getAllAccounts(context, null);
       if (accounts == null || accounts.length == 0) {
         Log.i(TAG, "No available google account, won't synchronize.");
         Exception e = new Exception("No available google account.");
         handler.handleError(e);
         return;
       }
       account = accounts[0];
     }
 
     if (httpConnector.isAuthenticated()) {
       execution.execute(input);
     } else {
       httpConnector
           .setHttpConnectorAuthHandler(new IHttpConnectorAuthHandler() {
 
             public void onAuthSuccess() {
               execution.execute(input);
             }
 
             public void onAuthFail() {
               Toast.makeText(context, "Authentication failed.",
                   Toast.LENGTH_LONG).show();
             }
           });
       httpConnector.authenticate(context, account);
     }
   }
 
   @Override
   public void getAnnoList(long offset, long limit,
       final ResponseHandler respHandler) {
     IHttpExecution execution = new IHttpExecution() {
 
       @Override
       public void execute(Map<String, Object> input) {
         try {
           Long offset = (Long) input.get("offset");
           Long limit = (Long) input.get("limit");
          String reqUrl = String.format("%s?offset=%d&limit=%d",
              BASE_URL_COMMUNITY, offset, limit);
           httpConnector.sendRequest(reqUrl, null, new AnnoResponseHandler(
               respHandler));
         } catch (ParseException e) {
           respHandler.handleError(e);
         } catch (IOException e) {
           respHandler.handleError(e);
         }
       }
 
     };
     final Map<String, Object> input = new HashMap<String, Object>();
     input.put("offset", offset);
     input.put("limit", limit);
     this.execute(execution, input, respHandler);
   }
 
   @Override
   public void getAnnoDetail(String annoId, final ResponseHandler respHandler) {
     IHttpExecution execution = new IHttpExecution() {
 
       @Override
       public void execute(Map<String, Object> input) {
         try {
           String annoId = (String) input.get("anno_id");
          String reqUrl = String.format("%s?anno_id=%s", BASE_URL_COMMUNITY,
              annoId);
           httpConnector.sendRequest(reqUrl, null, new AnnoResponseHandler(
               respHandler));
         } catch (ParseException e) {
           respHandler.handleError(e);
         } catch (IOException e) {
           respHandler.handleError(e);
         }
       }
 
     };
     final Map<String, Object> input = new HashMap<String, Object>();
     input.put("anno_id", annoId);
     this.execute(execution, input, respHandler);
   }
 
   @Override
   public void updateAppName(String annoId, String appName,
       final ResponseHandler respHandler) {
     IHttpExecution execution = new IHttpExecution() {
 
       @Override
       public void execute(Map<String, Object> input) {
         try {
           String annoId = (String) input.get("anno_id");
           String appName = (String) input.get("app_name");
          String urlParams = String.format("", annoId, appName);
          String reqUrl = String.format("%s?anno_id=%s&setName=%s",
              BASE_URL_COMMUNITY, annoId, URLEncoder.encode(appName, "UTF-8"));
           httpConnector.sendRequest(reqUrl, null, new AnnoResponseHandler(
               respHandler));
         } catch (ParseException e) {
           respHandler.handleError(e);
         } catch (IOException e) {
           respHandler.handleError(e);
         }
       }
 
     };
     final Map<String, Object> input = new HashMap<String, Object>();
     input.put("anno_id", annoId);
     input.put("app_name", appName);
     this.execute(execution, input, respHandler);
   }
 
   private interface IHttpExecution {
     void execute(Map<String, Object> input);
   }
 }
