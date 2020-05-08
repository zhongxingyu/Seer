 package com.hackathon.jugs.TweetHunt.authenticate;
 
 import android.accounts.*;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import com.hackathon.jugs.TweetHunt.R;
 
 import java.io.IOException;
 
 /**
  * Created with IntelliJ IDEA.
  * User: RobCastellow
  * Date: 8/24/13
  * Time: 12:42 AM
  * To change this template use File | Settings | File Templates.
  */
 public class TwitterInfo extends Activity {
 
     Activity activity;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.twitter_info);
         activity = this;
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         Intent intent = getIntent();
         AccountManager accountManager = AccountManager.get(getApplicationContext());
         Account account = (Account) intent.getExtras().get("account");
         AccountManagerFuture<Bundle> accountManagerFuture = accountManager.
                getAuthToken(account, "com.twitter.android.auth.login", false, new GetAuthTokenCallback(), null);
     }
 
     private class GetAuthTokenCallback implements AccountManagerCallback<Bundle> {
         public void run(AccountManagerFuture<Bundle> result) {
             Bundle bundle;
             try {
                 bundle = result.getResult();
                 Intent intent = (Intent) bundle.get(AccountManager.KEY_INTENT);
 
                 if (intent != null) {
                     startActivity(intent);
                    String authToken = bundle.getString(AccountManager.KEY_AUTHTOKEN);
//                    Log.i("authToken", authToken);
                 } else {
//                    onGetAuthToken(bundle);
                 }
             } catch (android.accounts.OperationCanceledException e) {
                 e.printStackTrace();
             } catch (AuthenticatorException e) {
                 e.printStackTrace();
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
     }
 
     ;
 }
