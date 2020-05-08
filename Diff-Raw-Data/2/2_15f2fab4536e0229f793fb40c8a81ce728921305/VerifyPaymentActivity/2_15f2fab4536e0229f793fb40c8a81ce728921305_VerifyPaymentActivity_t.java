 package mobisocial.payments;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import mobisocial.payments.server.BankSession;
 import mobisocial.payments.server.TokenVerifier;
 import mobisocial.socialkit.musubi.DbFeed;
 import mobisocial.socialkit.musubi.DbObj;
 import mobisocial.socialkit.musubi.Musubi;
 import android.app.Activity;
 import android.app.NotificationManager;
 import android.content.Context;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 
 public class VerifyPaymentActivity extends Activity {
     public static final String TAG = "VerifyPaymentActivity";
     
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.accept_bill);
         
         if (getIntent() == null || getIntent().getData() == null) {
             finish();
             return;
         }
         
         // Clear any pending notifications
         NotificationManager nm = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
         nm.cancelAll();
         
         ((TextView)findViewById(R.id.verifyText))
             .setText("Verifying payment information...");
         
         // Send the token if yes is clicked
         findViewById(R.id.yesbutton).setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 Musubi musubi = Musubi.getInstance(VerifyPaymentActivity.this);
                 JSONObject json = musubi.getObj().getJson();
                 try {
                     sendToken(json.getString("token"), json.getString("amount"));
                 } catch (JSONException e) {
                     Log.w(TAG, "JSON incomplete", e);
                     return;
                 }
             }
         });
         findViewById(R.id.nobutton).setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 Intent intent = new Intent(VerifyPaymentActivity.this, PaymentsActivity.class);
                 startActivity(intent);
                 finishActivity();
             }
         });
         
         // Hide the buttons unless there's something to ask
         ((Button)findViewById(R.id.yesbutton)).setVisibility(Button.INVISIBLE);
         ((Button)findViewById(R.id.nobutton)).setVisibility(Button.INVISIBLE);
         
         getBankNames(getIntent().getData());
     }
     
     private void sendToken(final String token, final String amount) {
         runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "token: " + token);
                Log.d(TAG, "amount: " + amount);
                boolean result = BankSession.authorize(token, amount);
                if (result) {
                    ((TextView)findViewById(R.id.verifyText))
                       .setText("Success! Payment of $" + amount + " is complete.");
                } else {
                    ((TextView)findViewById(R.id.verifyText))
                        .setText("Payment could not be verified.");
                }
                ((Button)findViewById(R.id.yesbutton)).setVisibility(Button.INVISIBLE);
                ((Button)findViewById(R.id.nobutton)).setVisibility(Button.VISIBLE);
                ((Button)findViewById(R.id.nobutton)).setText("OK");
            }
         });
     }
     
     // We need to check if the bank information is legitimate
     private void getBankNames(final Uri data) {
         runOnUiThread(new Runnable() {
             @Override
             public void run() {
                 Musubi musubi = Musubi.getInstance(VerifyPaymentActivity.this);
                 DbObj obj = musubi.objForUri(data);
                 DbFeed feed = obj.getContainingFeed();
                 JSONObject token = obj.getJson();
                 Log.d(TAG, token.toString());
                 String certName;
                 String routeName;
                 String amount;
                 String authToken;
                 try {
                     amount = token.getString("amount");
                     authToken = token.getString("token");
                     certName = TokenVerifier.getCertificateOwner(token.getString("bank_url"));
                     routeName = TokenVerifier.nameForRoutingNumber(token.getString("routing_number"));
                 } catch (JSONException e) {
                     Log.w(TAG, "JSON parse error", e);
                     finishActivity();
                     return;
                 }
                 if (authToken != null && amount != null && certName != null &&
                         routeName != null && certName.equals(routeName)) {
                     // no need to prompt user
                     sendToken(authToken, amount);
                     return;
                 }
                 
                 // Ask for permission to send the token
                 int myIndex = (feed.getMembers().get(0).getName()
                         .equals(feed.getLocalUser().getName())) ? 1 : 0;
                 ((TextView)findViewById(R.id.verifyText))
                     .setText(feed.getMembers().get(myIndex).getName() +
                             " provided the following bank information:" +
                             "\nFrom bank URL: " + certName +
                             "\nFrom routing number: " + routeName +
                             "\nAllow this payment?");
                 ((Button)findViewById(R.id.yesbutton)).setVisibility(Button.VISIBLE);
                 ((Button)findViewById(R.id.nobutton)).setVisibility(Button.VISIBLE);
             }
         });
     }
     
     private void finishActivity() {
         finish();
     }
 }
