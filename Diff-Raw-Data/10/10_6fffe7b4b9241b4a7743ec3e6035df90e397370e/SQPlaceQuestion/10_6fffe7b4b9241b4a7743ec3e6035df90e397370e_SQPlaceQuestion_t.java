 package ru.petrsu.smartquestions;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Spinner;
 import android.widget.Toast;
 
 public class SQPlaceQuestion extends Activity implements OnClickListener{
 	
 	private User user;
 	
 	private Button buttonSend;
 	private Button buttonBack;
 	
 	private EditText editTextTitle;
 	private EditText editTextText;
 	private EditText editTextAward;
 	private EditText editTextTimeout;
 	
 	private Spinner spinnerTopics;
 	
 	private SQDBAdapter db;
 	
 	private String[] topics;
 	
 	private String stringTitle;
 	private String stringText;
 	private String stringTopic;
 	
 	private int award;
 	private int timeout;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_place_question);
 		
 		db = new SQDBAdapter(this);
 		db.open();
 		
 		getUser();
 		getTopics();
 		
 		initLayout();
 	}
 	
 	private void getTopics() {
 		topics = db.getAllTopics();
 	}
 	
 	private void getUser() {
 		SharedPreferences s = getSharedPreferences(getString(R.string.file_preferences), MODE_PRIVATE);
 		
 		String log = s.getString(getString (R.string.preferences_login_key), "");
 		
 		user = db.getUserByNickname(log);
 	}
 	
 	private void initLayout() {
 		buttonSend = (Button)findViewById (R.id.ButtonSend);
 		buttonBack = (Button)findViewById (R.id.ButtonBack);
 		
 		editTextTitle = (EditText)findViewById (R.id.EditTextTitle);
 		editTextText = (EditText)findViewById (R.id.EditTextText);
 		editTextAward = (EditText)findViewById (R.id.EditTextAward);
 		editTextTimeout = (EditText)findViewById (R.id.EditTextTimeout);
 		
 		spinnerTopics = (Spinner)findViewById (R.id.SpinnerTopics);
 		
 		ArrayAdapter<String> adapter = new ArrayAdapter <String> (this, R.layout.spinner_item, topics);
 		spinnerTopics.setAdapter(adapter);
 		
 		buttonSend.setOnClickListener(this);
 		buttonBack.setOnClickListener(this);
 	}
 	
 	private int validate () {
 		stringTitle = editTextTitle.getText().toString();
 		if (stringTitle == null || stringTitle.isEmpty()) {
 			return R.string.error_empty_fields;
 		}
 		
 		String t = editTextAward.getText().toString();
 		if (t == null || t.isEmpty()) {
 			return R.string.error_empty_fields;
 		}
 		award = Integer.parseInt(t);
 		
 		t = editTextTimeout.getText().toString();
 		if (t == null || t.isEmpty()) {
 			return R.string.error_empty_fields;
 		}
 		timeout = Integer.parseInt(t);
 		
 		stringText = editTextText.getText().toString();
 		if (stringText == null || stringText.isEmpty()) {
 			return R.string.error_empty_fields;
 		}
 		
 		stringTopic = (String) spinnerTopics.getSelectedItem();
 		
 		return R.string.error_no_errors;
 	}
 	
 	private void tryToPlaceQuestion () {
 		int ret = validate();
 		Toast.makeText(this, getString(ret), Toast.LENGTH_SHORT).show();
 		
 		if (ret == R.string.error_no_errors) {
 			Question q = new Question (db.getTopicId (stringTopic), stringTitle, user, "", 
 									   timeout, Question.STATUS_NO_ANSWER, award, stringText);
 			db.addQuestion(q);
 			
 			//go to my questions here
 			startActivity (new Intent (this, SQMyQuestions.class));
			finish();
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_place_question, menu);
 		return true;
 	}
 
 	public void onClick(View v) {
 		switch (v.getId()) {
 		case R.id.ButtonSend:
 			tryToPlaceQuestion();
 			break;
 		case R.id.ButtonBack:
 			//TODO
 			break;
 		}
 	}
 
 }
