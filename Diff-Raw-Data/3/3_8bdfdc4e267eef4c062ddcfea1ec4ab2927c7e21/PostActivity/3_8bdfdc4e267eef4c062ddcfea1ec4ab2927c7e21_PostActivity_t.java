 package com.ph.tymyreader;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.Toast;
 
 import com.ph.tymyreader.model.DiscussionPref;
 
 public class PostActivity extends Activity {
 
 	private TymyReader app;
 	private DiscussionPref dsPref;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.post);
 		app = (TymyReader) getApplication();
 		
 		dsPref = app.getDsPref();
 		app.clearDsPref();		
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.post, menu);
 		return true;
 	}
 
 	public void onClick (View v) {
 		switch (v.getId()) {
 		case R.id.post_button_post:
 			sendPost();
 			return;
 		case R.id.post_button_cancel:
 			setResult(RESULT_CANCELED);
 			finish();
 			break;
 		}
 	}
 	
 	private void sendPost() {
 		// TODO Auto-generated method stub
 		EditText post = (EditText) findViewById(R.id.post_edit_text);
 		if (post.getText().toString().equals("")) return;
 		new Post(post.getText().toString()).execute(dsPref);
 	}
 
 	private class Post extends AsyncTask<DiscussionPref, Void, String> {
 		private ProgressDialog dialog = new ProgressDialog(PostActivity.this);
 		private String post;
 		
 		public Post (String post) {
 			super();
 			this.post = post;
 		}
 
 		@Override
 		protected void onPreExecute() {
 			dialog.setMessage(getString(R.string.sending));
 			dialog.show();
 		}
 
 		@Override
 		protected String doInBackground(DiscussionPref... dsPref) {
 			TymyPageLoader loader = new TymyPageLoader();
 			return loader.newPost(dsPref[0].getUrl(), dsPref[0].getId(), dsPref[0].getUser(), dsPref[0].getPass(), dsPref[0].getHttpContext(), post);
 		}
 
 		// onPostExecute displays the results of the AsyncTask.
 		@Override
 		protected void onPostExecute(String response) {
 			try
 			{
 				if(dialog.isShowing())
 				{
 					dialog.dismiss();
 				}
 				// do your Display and data setting operation here
 			} catch(Exception e) {
 				Log.v(TymyReader.TAG, "Error sending NewPost " + e);				
 			}
 			Toast.makeText(PostActivity.this, response != null ? R.string.send_ok : R.string.send_failed, Toast.LENGTH_LONG).show();
			finish();
 		}
 	}
 
 }
