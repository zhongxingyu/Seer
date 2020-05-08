 package com.thoughtworks.orteroid.activities;
 
 import android.app.ActionBar;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Build;
 import android.os.Bundle;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.EditText;
 import com.thoughtworks.orteroid.R;
 import com.thoughtworks.orteroid.constants.Constants;
 
 public class SelectBoardActivity extends Activity {
     private ActionBar actionBar;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.select_board);
         if (Build.VERSION.SDK_INT > 11)useActionBar();
 
     }
 
     private void useActionBar() {
         actionBar = getActionBar();
         actionBar.setHomeButtonEnabled(true);
         actionBar.setIcon(R.drawable.ic_launcher);
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case android.R.id.home:
                 Intent intent = new Intent(this, MainActivity.class);
                 startActivity(intent);
                 return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
 
 
     public void navigateToUrl(View view){
         EditText editText = (EditText) findViewById(R.id.url);
         String url = editText.getText().toString();
         String boardKey = url.substring(0, url.indexOf('/'));
         String boardId = url.substring(url.indexOf('/')+1,url.length());
         Intent intent = new Intent(this, ViewBoardActivity.class);
         intent.putExtra(Constants.BOARD_KEY, boardKey);
         intent.putExtra(Constants.BOARD_ID, boardId);
         startActivity(intent);
     }
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
        super.onBackPressed();
    }
 
 }
