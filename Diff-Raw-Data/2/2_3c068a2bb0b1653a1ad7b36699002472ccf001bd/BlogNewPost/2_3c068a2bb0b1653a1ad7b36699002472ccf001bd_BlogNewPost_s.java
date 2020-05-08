 package com.daneel87.AneCMS.Blog;
 
 import org.xmlrpc.android.XMLRPCClient;
 import org.xmlrpc.android.XMLRPCException;
 
 import com.daneel87.AneCMS.R;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.Toast;
 
 public class BlogNewPost extends Activity {
 
 	private String server;
 	private String sessionid;
 	
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 	    super.onCreate(savedInstanceState);
 	    setContentView(R.layout.blog_new_post);
 	    
 	    Bundle b = getIntent().getExtras();
 	    server = b.getString("server");
 	    sessionid = b.getString("sessionid");
 	    // TODO Auto-generated method stub
 	}
 
     public void Post(View v) {
     	EditText titolo = (EditText)findViewById(R.id.NewPostTitle);
     	EditText contenuto = (EditText)findViewById(R.id.NewPostContent);
    	String[] parameters = new String[] {sessionid,titolo.getText().toString(), contenuto.getText().toString(), ""};
     	XMLRPCClient client = new XMLRPCClient(server + "/xmlrpc.php");
     	try {
 			Boolean result = (Boolean) client.call("AneCMSBlog.addPost", parameters);
 			if (result){
 				CharSequence text = this.getString(R.string.posted);
 	            int duration = Toast.LENGTH_LONG;
 	            Toast.makeText(getApplicationContext(), text, duration).show();
 	            finish();
 	            return;
 			}
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 		}
 		CharSequence text = this.getString(R.string.not_posted);
         int duration = Toast.LENGTH_LONG;
         Toast.makeText(getApplicationContext(), text, duration).show();
 	}
 	
 	
 }
