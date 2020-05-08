 package com.hoos.around;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import com.facebook.android.*;
 import com.facebook.android.Facebook.*;
 import com.facebook.android.AsyncFacebookRunner;
 
 public class Login extends Activity{
 	Facebook facebook = new Facebook("332459203518890");
     AsyncFacebookRunner mAsyncRunner = new AsyncFacebookRunner(facebook);
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
     	System.out.println("login created............................");
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 
         facebook.authorize(this, new DialogListener() {
             @Override
             public void onComplete(Bundle values) {}
 
             @Override
             public void onFacebookError(FacebookError error) {}
 
             @Override
             public void onError(DialogError e) {}
 
             @Override
             public void onCancel() {}
         });
     }
 
     @Override
     public void onActivityResult(int requestCode, int resultCode, Intent data) {
         super.onActivityResult(requestCode, resultCode, data);
         facebook.authorizeCallback(requestCode, resultCode, data);
         System.out.println("login result............................");
        finish();
     }
 
 }
