 package com.tamagohan.cloudnote_android;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Toast;
 
 public class NoteNewActivity extends Activity {
 	String title = null;
 	String body  = null;
 	String id    = null;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         Log.v("EXAMPLE", "onCreate of noteNewActivity was called.");
         setContentView(R.layout.activity_note_new);
         final EditText titleText = (EditText) findViewById(R.id.note_title);
         final EditText bodyText  = (EditText) findViewById(R.id.note_body);
 		Button createButton = (Button) findViewById(R.id.note_create);
 		Button backButton   =   (Button) findViewById(R.id.note_back);
 		
 		final DefaultHttpClient httpClient = ((MyCloudNote) this.getApplication()).getHttpClient();
 		
 	    createButton.setOnClickListener(new View.OnClickListener() {
     		public void onClick(View view) {
     			title = titleText.getText().toString();
     		    body  = bodyText .getText().toString();
     		    final Map<String, String> params = new HashMap<String, String>();
     		    params.put("title", title);
     		    params.put("body",  body);
     		    
     		    String path = "notes/";
     		    exec_post(path, params, httpClient);
    			}
    		});
 		
 		backButton.setOnClickListener(new View.OnClickListener() {
     		public void onClick(View view) {
     			finish();
    			}
    		});
    	}
     
     // POSTʐMs
     @SuppressLint("HandlerLeak")
 	private void exec_post(String url, Map<String, String> params, final DefaultHttpClient httpClient) {
 
       // 񓯊^XN`
       HttpPostTask task = new HttpPostTask(
         this,
         Constants.SERVER_URL + url,
 
         // ^XNɌĂ΂UĨnh
         new HttpPostHandler(){
 
           @Override
           public void onPostCompleted(String response, Integer status) {
             // Ȃ΃m[g̃Xgʂ֑J
         	  Log.v("http request success", "post success");
         	  Intent intent = new Intent(NoteNewActivity.this, NoteListActivity.class);
         	  intent.putExtra("FROM_ACTIVITY", "new");
         	  intent.putExtra("MESSAGE",       "쐬ɐ܂B");
         	  startActivity(intent);
           }
 
           @Override
           public void onPostFailed(String response, Integer status) {
         	  Log.v("http request error", Integer.toString(status));
         	  Log.d("http request error", response);
         	  String err_msg = "ʐMG[܂B";
         	  if (status == 403){
        		  err_msg = "쐬Ɏs܂B";
         	  }
             Toast.makeText(
               getApplicationContext(), 
               err_msg, 
               Toast.LENGTH_LONG
             ).show();
           }
         },
         httpClient
       );
       // p[^ݒ
       for (Map.Entry<String, String> entry : params.entrySet()) {
     	  String key = entry.getKey();
     	  String val = entry.getValue();
     	  task.addPostParam( key, val );
       }
 
       // ^XNJn
       task.execute();
     }
 }
