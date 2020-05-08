 package com.example.spanishtalk.questions;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.http.HttpResponse;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ProgressBar;
 import android.widget.Toast;
 
 import com.example.lib.BaseUtils;
 import com.example.lib.HttpPack;
 import com.example.lib.SessionManagement;
 import com.example.logic.BaseAction;
 import com.example.logic.BaseEventActivity;
 import com.example.logic.BaseUrl;
 import com.example.spanishtalk.R;
 
 public class NewActivity extends BaseEventActivity {
 
 	private EditText edit_text_title, edit_text_content;
 	private Button sendBtn;
 	private ProgressBar progressBar;
 	String title, content;
 	Integer user_id;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_question_new);
 
 		loadUi();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_question, menu);
 		return true;
 	}
 
 	private void loadUi() {
 		edit_text_title = (EditText) findViewById(R.id.question_title);
 		edit_text_content = (EditText) findViewById(R.id.question_content);
 		
 		sendBtn = (Button) findViewById(R.id.link_to_question);
 		progressBar = (ProgressBar) findViewById(R.id.progressBar1);
 
 		user_id = new SessionManagement(getApplicationContext()).getUserId();
 	}
 
 	public boolean validateQuestionForm(String title, String content) {
 		if (BaseUtils.is_str_blank(title) || BaseUtils.is_str_blank(content)) {
 			Context context = getApplicationContext();
 			Toast.makeText(context, R.string.new_question_required,
 					Toast.LENGTH_SHORT).show();
 
 			return false;
 		}
 
 		return true;
 	}
 
 	public void postQuestion(View view) {
 		title = edit_text_title.getText().toString();
 		content = edit_text_content.getText().toString();
 
 		if (validateQuestionForm(title, content)) {
 			new PostQuestionTask().execute();
 		}
 		
 	}
 
 	public class PostQuestionTask extends AsyncTask<Void, Void, JSONObject> {
 
 		@Override
 		protected JSONObject doInBackground(Void... arg0) {
 			Map<String, String> params = new HashMap<String, String>();
 			params.put("question[title]", edit_text_title.getText().toString());
 			params.put("question[content]", edit_text_content.getText()
 					.toString());
 
 			HttpResponse response = HttpPack.sendPost(getApplicationContext(),
 					BaseUrl.questionCreate, params);
 	
 			if (response == null) {
 				cancel(true);
 				return null;
 			}
 			
 			Integer statusCode = response.getStatusLine().getStatusCode();
 			if (statusCode == 200) {
 				return HttpPack.getJsonByResponse(response);
 			}
 			
 			cancel(true);
 			return null;
 		}
 		
 		@Override
 		protected void onPreExecute() {
 			progressBar.setVisibility(View.VISIBLE);
 
 			if (!HttpPack.hasConnected(NewActivity.this)) {
 				Context context = getApplicationContext();
 				BaseAction.showFormNotice(context,
 						context.getString(R.string.network_error));
 				cancel(true);
 				return;
 			}
 
 			super.onPreExecute();
 		}
 
 		@Override
 		protected void onCancelled() {
 			progressBar.setVisibility(View.INVISIBLE);
 
 			Context context = getApplicationContext();
 			BaseAction.showFormNotice(context, context.getString(R.string.server_connection_error));
 		}
 
 		@Override
 		protected void onPostExecute(JSONObject q) {
 			final Integer questionId;
 						
 			try {
				questionId = q.getInt("question_id");
 				AlertDialog.Builder builder = new AlertDialog.Builder(
 						NewActivity.this);
 				builder.setMessage(R.string.be_sent)
 						.setCancelable(false)
 						.setPositiveButton(R.string.confirm_btn,
 								new DialogInterface.OnClickListener() {
 									public void onClick(DialogInterface dialog,
 											int id) {
 
 										Intent intent = new Intent(
 												getApplicationContext(),
 												ShowActivity.class);
 										intent.putExtra("questionId",
 												questionId);
 										startActivity(intent);
 									}
 								});
 				builder.create().show();
 
 			} catch (JSONException e) {
 				e.printStackTrace();
 			}
 
 			super.onPostExecute(q);
 		}
 
 	}
 
 }
