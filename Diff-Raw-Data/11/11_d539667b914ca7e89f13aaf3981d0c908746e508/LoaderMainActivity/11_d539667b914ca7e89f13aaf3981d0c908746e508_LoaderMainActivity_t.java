 package com.jinheyu.lite_mms;
 
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 
 /**
  * Created by xc on 13-8-13.
  */
 public class LoaderMainActivity extends FragmentActivity {
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_loader_main_activity);
 
         Button buttonStartUnload = (Button) findViewById(R.id.buttonStartUnload);
         Button buttonStartLoad = (Button) findViewById(R.id.buttonStartLoad);
 
         buttonStartUnload.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 Intent intent = new Intent(LoaderMainActivity.this, SelectUnloadSessionActivity.class);
                 startActivity(intent);
             }
         });
 
         buttonStartLoad.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
             }
         });
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.loader_actions, menu);
         return super.onCreateOptionsMenu(menu);
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.action_logout:
                 AlertDialog.Builder builder = new AlertDialog.Builder(LoaderMainActivity.this);
                 builder.setMessage("您确认要登出?");
                 builder.setNegativeButton(R.string.cancel, null);
                 builder.setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialogInterface, int i) {
                         Utils.clearUserToken(LoaderMainActivity.this);
                        finish();
                         Intent intent = new Intent(LoaderMainActivity.this, LogInActivity.class);
                         intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                         startActivity(intent);
                     }
                 });
                 builder.create().show();
                 break;
             case R.id.action_settings:
                 Intent intent = new Intent(LoaderMainActivity.this, MyPreferenceActivity.class);
                 startActivity(intent);
                 break;
             default:
                 break;
         }
         return super.onOptionsItemSelected(item);
     }
 }
