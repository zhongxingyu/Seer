 package com.github.mobileartisans.bawall;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.inputmethod.EditorInfo;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 import com.example.R;
 
 public class HomeActivity extends Activity implements TextView.OnEditorActionListener {
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         if (!new UserPreference(this).isSet()) {
             startActivity(new Intent(this, LoginActivity.class));
             return;
         }
         setContentView(R.layout.main);
         EditText issueNumber = (EditText) findViewById(R.id.issueNumber);
         issueNumber.setOnEditorActionListener(this);
     }
 
     public void onCameraCapture(View view) {
         Toast.makeText(this, "You wish!", Toast.LENGTH_SHORT).show();
     }
 
     public void onKeyboardCapture(View view) {
         Toast.makeText(this, "You wish!", Toast.LENGTH_SHORT).show();
     }
 
     public void onGestureCapture(View view) {
         Toast.makeText(this, "You wish!", Toast.LENGTH_SHORT).show();
     }
 
     @Override
     public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
         if (actionId == EditorInfo.IME_ACTION_SEARCH) {
             Intent intent = new Intent(this, IssueViewActivity.class);
            intent.putExtra(IssueViewActivity.ISSUE_KEY, textView.getText());
             startActivity(intent);
             return true;
         }
         return false;
     }
 }
