 package cc.hughes.droidchatty;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 
 public class ComposePostView extends Activity {
 
 	private int _replyToPostId = 0;
 	private ProgressDialog _progressDialog;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState)
 	{
         super.onCreate(savedInstanceState);
         setContentView(R.layout.edit_post);
         
         // grab the post being replied to, if this is a reply
         Bundle extras = getIntent().getExtras();
         if (extras != null && extras.containsKey(SingleThreadView.THREAD_ID))
             _replyToPostId = getIntent().getExtras().getInt(SingleThreadView.THREAD_ID);
         
         Button reply_button = (Button)findViewById(R.id.replyButton);
         Button cancel_button = (Button)findViewById(R.id.cancelButton);
         
         if (_replyToPostId == 0)
             setTitle(getTitle() + " - New Post");
         else
             setTitle(getTitle() + " - Reply");
         
         reply_button.setOnClickListener(onButtonClick);
         cancel_button.setOnClickListener(new OnClickListener()
         {
     		@Override
     		public void onClick(View v)
     		{
     		    finish();
     		}
         });
 	}
 	
 	OnClickListener onButtonClick = new OnClickListener()
 	{
 		@Override
 		public void onClick(View v)
 		{
 		    // grab the content to post
 		    EditText et = (EditText)findViewById(R.id.textContent);
 		    String content = et.getText().toString();
 		    
 		    // post in the background
 		    _progressDialog = ProgressDialog.show(ComposePostView.this, "Posting", "Attempting to post...");
 		    new PostTask().execute(content);
 		}
 	};
 	
 	void postSuccessful(int postId)
 	{
 	    Intent reply = new Intent();
 	    if (postId > 0)
 		    reply.putExtra("postId", postId);
 	    setResult(RESULT_OK, reply);
 	    
 	    // lets get the hell out of here!
 	    finish();
 	}
 	
 	class PostTask extends AsyncTask<String, Void, Integer>
 	{
 	    Exception _exception;
 	    
         @Override
         protected Integer doInBackground(String... params)
         {
             try
             {
                 String content = params[0];
             
                 int reply_id = ShackApi.postReply(ComposePostView.this, _replyToPostId, content);
 	    
                 return new Integer(reply_id);
             }
             catch (Exception e)
             {
                 Log.e("DroidChatty", "Error posting reply", e);
                 _exception = e;
                 return null;
             }
         }
 
         @Override
         protected void onPostExecute(Integer result)
         {
             _progressDialog.dismiss();
             
             if (_exception != null)
                 ErrorDialog.display(ComposePostView.this, "Error", "Error posting:\n" + _exception.getMessage());
             else
                 postSuccessful(result.intValue());
         }
         
 	}
 	
 }
