 package com.github.mobileartisans.bawall;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.pm.PackageManager;
 import android.content.pm.ResolveInfo;
 import android.os.Bundle;
 import android.speech.RecognizerIntent;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.inputmethod.EditorInfo;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.TextView;
 import android.widget.Toast;
 import com.crittercism.app.Crittercism;
 import com.github.mobileartisans.bawall.domain.UserPreference;
 
 import java.util.List;
 
 public class HomeActivity extends Activity implements TextView.OnEditorActionListener {
     private static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;
     private static final String TAG = HomeActivity.class.getName();
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         Crittercism.init(getApplicationContext(), "505ecde4be790e36c6000002");
         if (!new UserPreference(this).isSet()) {
             startActivity(new Intent(this, LoginActivity.class));
             return;
         }
         setContentView(R.layout.main);
         EditText issueNumber = (EditText) findViewById(R.id.issueNumber);
         issueNumber.setOnEditorActionListener(this);
         PackageManager pm = getPackageManager();
         List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
         if (activities.size() == 0) {
             ImageButton voiceCapture = (ImageButton) findViewById(R.id.voiceCapture);
             voiceCapture.setClickable(false);
         }
 
     }
 
     public void onCameraCapture(View view) {
         Toast.makeText(this, "You wish!", Toast.LENGTH_SHORT).show();
     }
 
     public void onVoiceCapture(View view) {
         Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
         intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                 RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
         intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say issue number...");
         intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
         startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
     }
 
     public void onGestureCapture(View view) {
         Toast.makeText(this, "You wish!", Toast.LENGTH_SHORT).show();
     }
 
     @Override
     public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
         if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            String issueKey = textView.getEditableText().toString();
            openIssue(issueKey);
             return true;
         }
         return false;
     }
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {
             List<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
             if (matches == null || matches.isEmpty()) {
                 Toast.makeText(this, "Voice match failed", Toast.LENGTH_SHORT);
             } else {
                 EditText issueNumber = (EditText) findViewById(R.id.issueNumber);
                 String issueKey = matches.get(0);
                 issueNumber.setText(issueKey.replaceAll("[^\\d]", ""));
                openIssue(issueKey);
             }
 
         }
         super.onActivityResult(requestCode, resultCode, data);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);
         menu.add("Preferences");
         menu.add("Log out");
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         if ("Log out".equals(item.getTitle())) {
             new UserPreference(this).clear();
             startActivity(new Intent(this, LoginActivity.class));
             return true;
         }
         if ("Preferences".equals(item.getTitle())) {
             startActivity(new Intent(this, ProjectPreference.class));
             return true;
         }
         return false;
     }
 
    private void openIssue(String issue_key) {
        Intent intent = new Intent(this, IssueViewActivity.class);
        intent.putExtra(IssueViewActivity.ISSUE_KEY, issue_key);
        startActivity(intent);
    }

 }
