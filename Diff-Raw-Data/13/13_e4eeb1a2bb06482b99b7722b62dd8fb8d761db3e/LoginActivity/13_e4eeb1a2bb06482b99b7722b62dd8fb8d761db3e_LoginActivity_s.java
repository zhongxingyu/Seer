 
 package com.huskysoft.eduki;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 
 import com.huskysoft.eduki.data.User;
 import com.huskysoft.eduki.data.UserQuery;
 
 /**
  * @author Cody Thomas LoginActivity allows a user to login to their account
  */
 
 public class LoginActivity extends Activity implements TaskComplete {
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_login);
     }
 
     /**
      * An onClickListener established in the xml of this activity, called when
      * the "login" button is pressed. Note: Method header must be public for the
      * xml to recognize this method.
      * 
      * @param view The view clicked. In this case, a button
      */
     public void loginClicked(View v) {
         TextView userBox = (TextView) findViewById(R.id.loginBox);
         String user = userBox.getText().toString();
         TextView passBox = (TextView) findViewById(R.id.passwordBox);
         String pass = passBox.getText().toString();
         ((ProgressBar) findViewById(R.id.progressBar1)).setVisibility(View.VISIBLE);
         ((TextView) findViewById(R.id.loginError)).setVisibility(View.INVISIBLE);
         UserQuery.attemptLogin(user, pass, this, 0);
     }
 
     @Override
     public void taskComplete(String data, int id) {
         // TODO If data doesn't give me an error, store the authtoken in shared
         // preferences
         ((ProgressBar) findViewById(R.id.progressBar1)).setVisibility(View.INVISIBLE);
         User user = UserQuery.parseLoginAttempt(data);
         if (user != null) {
             SharedPreferences.Editor prefs = PreferenceManager.getDefaultSharedPreferences(this).edit();
             prefs.putBoolean("authenticated", true);
             prefs.putInt("user_id", user.getId());
             prefs.putString("first_name", user.getFirstName());
             prefs.commit();
             Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
             startActivity(intent);
         } else {
             ((TextView) findViewById(R.id.loginError)).setVisibility(View.VISIBLE);
         }
     }
 
     /**
      * Starts the courseslist activity
      */
     public void restartToCoursesActivity(View v) {
         Intent intent = new Intent(this, CoursesListActivity.class);
         startActivity(intent);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.main, menu);
         menu.findItem(R.id.action_dash).setVisible(false);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.action_courses:
                 // app icon in action bar clicked; go home
                 Intent intent = new Intent(this, CoursesListActivity.class);
                 intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                 startActivity(intent);
                 return true;
             case R.id.action_dash:
                 Intent intentMain = new Intent(this, MainActivity.class);
                 intentMain.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                 startActivity(intentMain);
                 return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
     
     public void logout() {
         PreferenceManager.getDefaultSharedPreferences(this).edit().clear().commit();
     }
 }
