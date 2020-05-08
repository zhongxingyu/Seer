 package com.hyperactivity.android_app.network;
 
 import android.app.ProgressDialog;
 import android.os.AsyncTask;
 import android.util.Log;
 import com.hyperactivity.android_app.Constants;
 import com.hyperactivity.android_app.R;
 import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
 import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
 import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
 import com.thetransactioncompany.jsonrpc2.client.JSONRPC2Session;
 import com.thetransactioncompany.jsonrpc2.client.JSONRPC2SessionException;
 import com.thetransactioncompany.jsonrpc2.client.JSONRPC2SessionOptions;
 
 import java.net.InetAddress;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.UnknownHostException;
 
 public class NetworkAsyncTask extends AsyncTask<Object, Integer, JSONRPC2Response> {
     private NetworkCallback networkCallback;
     ProgressDialog progressDialog;
     private final boolean lockWithLoadingScreen;
 
     public NetworkAsyncTask(NetworkCallback ncb, boolean lockWithLoadingScreen) {
         networkCallback = ncb;
         this.lockWithLoadingScreen = lockWithLoadingScreen;
     }
 
     @Override
     protected JSONRPC2Response doInBackground(Object... jsonrpc2Requests) {
         if(networkCallback instanceof TestNetworkCallback) {
             return ((TestNetworkCallback)networkCallback).getResponse();
         }
 
         JSONRPC2Request jsonrpc2Request = (JSONRPC2Request) jsonrpc2Requests[0];
         int id = (Integer)jsonrpc2Request.getID();
         URL serverURL = null;
         try {
 
                serverURL = new URL("http://" + Constants.Server.IP + ":" + Constants.Server.PORT + "/");
         } catch (MalformedURLException e) {
             Log.e(Constants.Log.TAG, "exception: ", e);
             return new JSONRPC2Response(JSONRPC2Error.PARSE_ERROR.appendMessage(e.getMessage()), null);
         }
         JSONRPC2Session mySession = new JSONRPC2Session(serverURL);
         JSONRPC2SessionOptions options = new JSONRPC2SessionOptions();
         options.setConnectTimeout(Constants.Server.TIMEOUT);
         mySession.setOptions(options);
 
         JSONRPC2Response response;
         // Send request
         try {
             response = mySession.send(jsonrpc2Request);
         } catch (JSONRPC2SessionException e) {
             return handleJSONRPC2Exception(e);
         }
 
         response = validateResponse(id, response);
         return response;
     }
 
     private JSONRPC2Response handleJSONRPC2Exception(JSONRPC2SessionException e) {
         JSONRPC2Response errorResponse;
         String errorType = null;
         if (e.getCauseType() == JSONRPC2SessionException.JSONRPC2_ERROR) {
             errorType = "JSONRPC2 Error: " + e.getMessage();
         }
         if (e.getCauseType() == JSONRPC2SessionException.BAD_RESPONSE) {
             errorType = "Bad response Error: " + e.getMessage();
         }
         if (e.getCauseType() == JSONRPC2SessionException.NETWORK_EXCEPTION) {
             errorType = "ServerLink Error: " + e.getMessage();
         }
         if (e.getCauseType() == JSONRPC2SessionException.UNEXPECTED_CONTENT_TYPE) {
             errorType = "Wrong content Error: " + e.getMessage();
         }
         if (e.getCauseType() == JSONRPC2SessionException.UNEXPECTED_RESULT) {
             errorType = "Strange result Error: " + e.getMessage();
         }
         if (e.getCauseType() == JSONRPC2SessionException.UNSPECIFIED) {
             errorType = "Unknown Error: " + e.getMessage();
         }
         errorResponse = new JSONRPC2Response(new JSONRPC2Error(-32099, errorType), null);
         return errorResponse;
     }
 
     @Override
     protected void onPreExecute() {
         super.onPreExecute();
         if (lockWithLoadingScreen) {
             String caption = "Please wait";
             String text = "Chatting with server...";
             if (networkCallback != null) {
                 caption = networkCallback.getActivity().getResources().getString(R.string.wait_server_caption);
                 text = networkCallback.getActivity().getResources().getString(R.string.wait_server_text);
             }
             progressDialog = ProgressDialog.show(networkCallback.getActivity(), caption, text, true);
         }
     }
 
     @Override
     protected void onPostExecute(JSONRPC2Response jsonrpc2Response) {
         super.onPostExecute(jsonrpc2Response);
         if (lockWithLoadingScreen) {
             progressDialog.dismiss();
         }
 
         Object result = null;
 
         if (jsonrpc2Response.indicatesSuccess()) {
             result = jsonrpc2Response.getResult();
         } else {
             result = jsonrpc2Response.getError();
         }
 
         int id = 0;
 
         if(jsonrpc2Response.getID() != null) {
             id = (int)(long)(Long)jsonrpc2Response.getID();
         }
 
         networkCallback.onNetworkTaskComplete(jsonrpc2Response.indicatesSuccess(), result, id);
     }
 
     private JSONRPC2Response validateResponse(int id, JSONRPC2Response response) {
         //
 
         if (response.indicatesSuccess()) {
             net.minidev.json.JSONObject jsonResponse = response.toJSONObject();
 
             int responseID = (int)(long)(Long)jsonResponse.get(Constants.Transfer.ID); // ಠ_ಠ
 
             if (responseID == id && jsonResponse.get(Constants.Transfer.RESULT) != null) {
                 // Everything is ok!
             } else {
                 return new JSONRPC2Response(JSONRPC2Error.PARSE_ERROR.appendMessage(response.toJSONString()), null);
             }
         }
         return response;
     }
 }
