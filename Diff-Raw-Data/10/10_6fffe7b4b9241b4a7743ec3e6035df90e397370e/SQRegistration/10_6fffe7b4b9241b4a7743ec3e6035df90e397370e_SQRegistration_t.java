 package ru.petrsu.smartquestions;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.util.Log;
 import android.util.SparseBooleanArray;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.GridView;
 import android.widget.LinearLayout;
 import android.widget.Toast;
 import android.support.v4.app.NavUtils;
 
 public class SQRegistration extends Activity implements OnClickListener{
 	private static final int START_RATE = 10;
 	
 	private static final int ERROR_EMPTY_FIELDS = 1;
 	private static final int ERROR_ALREADY_EXIST = 2;
 	private static final int ERROR_NO_TOPICS = 3;
 
 	private static final int NO_ERRORS = 0;
 	
 	private static final String ERROR_STRING_EMPTY_FIELDS = "  ";
 	private static final String ERROR_STRING_ALREADY_EXIST = "user exists";
 	private static final String ERROR_STRING_NO_ERRORS = "   ";
 	private static final String ERROR_STRING_NO_TOPICS = "Need to chose at least one topic";
 	
 	private EditText login;
 	private EditText password;
 	
 	private Button send;
 	
 	private GridView topics;
 	
 	private SQDBAdapter db;
 	
 	private String[] stringTopics;
 	
 	private String stringLogin;
 	private String stringPassword;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_registration);
         
         db = new SQDBAdapter (this);
     	db.open();
     	stringTopics = db.getAllTopics();
         
         initLayout();
     }
     
     private void initLayout() {
     	login = (EditText)findViewById(R.id.EditTextLogin);
     	password = (EditText)findViewById (R.id.EditTextPassword);
     	
     	send = (Button)findViewById (R.id.ButtonSend);
     	send.setOnClickListener(this);
     	
     	topics = (GridView)findViewById (R.id.GridViewTopics);
     	
     	try {
     		topics.setAdapter(new ArrayAdapter <String> (this, R.layout.gridview_item, stringTopics));
     	}
     	catch (NullPointerException e) {
     		Log.d("null", "happend");
     	}
     }
     
     private String[] getCheckedTopics() {
     	SparseBooleanArray a = topics.getCheckedItemPositions();
     	
     	String ret[] = new String[topics.getCheckedItemCount()];
     	int last = 0;
     	
     	for (int i = 0;i < stringTopics.length;i++) {
     		if (a.get(i)) {
     			ret[last++] = stringTopics[i];
     		}
     	}
     	return ret;
     }
     
     /*
      * validates all shit
      */
     private int validate () {
     	stringLogin = login.getText().toString();
     	stringPassword = password.getText().toString();
     	
     	if (stringLogin == null || stringLogin.isEmpty() || stringPassword == null || stringPassword.isEmpty()) {
     		return ERROR_EMPTY_FIELDS;
     	}
     	
     	String t[] = getCheckedTopics();
     	
     	if (t == null || t.length == 0) {
     		return ERROR_NO_TOPICS;
     	}
 
     	if (db.ifUserExist(stringLogin)) {
     		return ERROR_ALREADY_EXIST;
     	}
     	
     	return NO_ERRORS;	
     }
     
     private void tryToRegister () {
     	String message = "";
     	boolean ok = false;
     	
     	switch (validate()) {
     	case ERROR_EMPTY_FIELDS:
     		message = ERROR_STRING_EMPTY_FIELDS;
     		break;
     	case ERROR_NO_TOPICS:
     		message = ERROR_STRING_NO_TOPICS;
     		break;
     	case ERROR_ALREADY_EXIST:
     		message = ERROR_STRING_ALREADY_EXIST;
     		break;
     	case NO_ERRORS:
     		message = ERROR_STRING_NO_ERRORS;
     		ok = true;
     		break;
     	}
     	Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
     	
     	if (ok) {
     		db.addUser(new User (stringLogin, stringPassword,  "", START_RATE), getCheckedTopics());
     		
    		SharedPreferences s = getSharedPreferences(getString (R.string.file_preferences), MODE_PRIVATE);
     		Editor e = s.edit();
     		
     		e.putString(getString(R.string.preferences_login_key), stringLogin);
     		e.putString(getString (R.string.preferences_password_key), stringPassword);
     		
     		e.commit();
     		
     		startActivity (new Intent (this, SQProfile.class));
    		finish();
     	}
     }
 
 	public void onClick(View v) {
 		switch (v.getId()) {
 		case R.id.ButtonSend:
 			tryToRegister();
 			break;
 		}
 	}
 }
