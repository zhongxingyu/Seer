 package step.email;
 
 import javax.mail.Transport;
 import javax.mail.internet.MimeMessage;
 
 import com.step.launcher.R;
 
 import android.app.Activity;
 import android.os.AsyncTask;
 import android.util.Log;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class SendEmailTask extends AsyncTask<String, Void, String> {
 	private Activity activity;
 	private Transport transport;
 	private String username;
 	private String passwd;
 	private MimeMessage message;
 	
 	SendEmailTask(Activity a, Transport t, String un, String pass, MimeMessage msg){
 		this.activity = a;
 		this.transport = t;
 		this.username = un;
 		this.passwd = pass;
 		this.message = msg;
 	}
 	@Override
 	protected String doInBackground(String...strings)
 	{
 		String Header = "";
         try
         {
         	this.transport.connect("smtp.gmail.com", 465, this.username, this.passwd);
         	this.transport.sendMessage(this.message, this.message.getAllRecipients());
         	this.transport.close();
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			Toast.makeText(this.activity, "Send Failed!", Toast.LENGTH_SHORT).show();
 			return "Connection Failed";
 		}
         return "Connected";
 	}
 
 	@Override
 	protected void onPostExecute(String result)
 	{
 		Toast.makeText(this.activity, "Email Sent!", Toast.LENGTH_SHORT).show();
 		EditText body = (EditText) this.activity.findViewById(R.id.compose_message_content);
     	EditText subj = (EditText) this.activity.findViewById(R.id.compose_subject);
     	TextView to   = (TextView) this.activity.findViewById(R.id.compose_to);
    	
    	try
    	{
	    	body.setText("");
	    	subj.setText("");
	    	to.setText("");
    	}
	    catch (NullPointerException e){}
 	}
 }
 
