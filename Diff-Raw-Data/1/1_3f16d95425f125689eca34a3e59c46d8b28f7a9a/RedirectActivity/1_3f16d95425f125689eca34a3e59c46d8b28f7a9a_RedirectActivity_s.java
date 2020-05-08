 package com.facebook.flw;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 
 /**
  * Created with IntelliJ IDEA.
  * User: ostrulovich
  * Date: 9/26/13
  * Time: 11:13 PM
  * To change this template use File | Settings | File Templates.
  */
 public class RedirectActivity extends Activity {
 
   @Override
   protected void onCreate(Bundle savedInstanceState) {
     Intent intent = new Intent(this, PickRestaurantActivity.class);
     Log.i(FreeLunchWednesdayApplication.TAG, "Lets redirect");
     startActivity(intent);
   }
 }
