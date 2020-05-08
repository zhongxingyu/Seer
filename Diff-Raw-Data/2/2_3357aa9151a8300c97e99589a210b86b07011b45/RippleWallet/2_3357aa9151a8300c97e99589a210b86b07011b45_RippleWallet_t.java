 package org.icehat.ripplewallet;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.io.IOException;
 import java.util.List;
 import org.apache.http.message.BasicNameValuePair;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.TextView;
 
 
 import com.codebutler.android_websockets.WebSocketClient;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 /** Starting activity with a log in screen.
  *
  *  @author Matthías Ragnarsson
  */
 public class RippleWallet extends Activity
 {
     private static final String TAG = "RippleWallet";
     private static final String rippleServerURI = "wss://s1.ripple.com";
     private WebSocketClient client;
     List<BasicNameValuePair> extraHeaders;
     private Activity activity;
     private static final int ID_ACCOUNT_INFO = 100;
 
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 
         client = new WebSocketClient(URI.create(rippleServerURI), new WebSocketClient.Listener() {
             @Override
             public void onConnect() {
                 Log.d(TAG, "Connected!");
             }
 
             @Override
             public void onMessage(String message) {
                 Log.d(TAG, String.format("Got string message! %s", message));
 
                 JSONObject object = null;
                 try {
                     object = new JSONObject(message);
                 } catch (JSONException e) {
                     e.printStackTrace();
                 }
 
                 final JSONObject jsonobject = object;
 
                 int temp_id = 0;
                 JSONObject temp_result = new JSONObject();
                 try {
                     temp_id = jsonobject.getInt("id");
                     temp_result = jsonobject.getJSONObject("result");
                 } catch (JSONException e) {
                     e.printStackTrace();
                 }
 
                 final int transaction_id = temp_id;
                 final JSONObject result = temp_result;
                 Log.d(TAG, "Got result: " + result);
 
             }
 
             @Override
             public void onMessage(byte[] data) {
                 Log.d(TAG, "Got binary message!");
             }
 
             @Override
             public void onDisconnect(int code, String reason) {
                 Log.d(TAG, String.format("Disconnected! Code: %d Reason: %s", code, reason));
             }
 
             @Override
             public void onError(Exception error) {
                 Log.e(TAG, "Error!", error);
             }
         }, extraHeaders);
         client.connect();
     }
 
     /** Logs a user into a wallet with an address and password
      *  specified in the activity's address and password fields.\ When
      *  that occurs, appropriate information is stored and the activity
      *  switches to wallet's balance activity.\ If the address or password
      *  are invalid, an error message is displayed on screen.
      *
      *  Note: Method in the making, doing tests.
      * @throws JSONException
      */
 
     public JSONObject parseResult(String message) throws JSONException{
         JSONObject json = null;
         json = new JSONObject(message);
         final JSONObject result = json;
         return result.getJSONObject("result");
     }
 
     public void account_info(String address) throws JSONException{
         JSONObject json = new JSONObject();
         json.put("account", address);
         client.send(json.toString());
     }
 
     public void logIn(View view) {
         Log.i(TAG, "Button clicked");
 
         // get user and pass
         EditText address = (EditText)findViewById(R.id.address);
         EditText password = (EditText)findViewById(R.id.password);
        TextView login_msg = (TextView)findViewById(R.id.login_msg);
 
         Log.i(TAG, address.getText().toString());
         Log.i(TAG, password.getText().toString());
         try {
             JSONObject json = new JSONObject();
             json.put("command", "subscribe");
             json.put("id", 0);
             JSONArray arr = new JSONArray();
             arr.put("ledger");
             json.put("streams", arr);
             client.send(json.toString());
             login_msg.setText("Success!: ");
             account_info(address.getText().toString());
 
         } catch(JSONException e) {
             Log.e(TAG, e.toString());
             login_msg.setText("Error: " + e.toString());
         }
     }
 
 }
