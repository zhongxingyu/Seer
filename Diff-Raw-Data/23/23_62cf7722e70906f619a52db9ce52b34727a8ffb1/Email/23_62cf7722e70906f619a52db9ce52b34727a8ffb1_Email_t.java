 package ch.zh.dipima.multichannelmobile.message;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 import ch.zh.dipima.multichannelmobile.MainActivity;
 import ch.zh.dipima.multichannelmobile.R;
 import ch.zh.dipima.multichannelmobile.WriteMessage;
 import ch.zh.dipima.multichannelmobile.exceptions.ErrorInMessageException;
 
 public class Email extends Message implements Validatable {
 	private String recipient;
 	private String subject;
 	private String body;
 	private String attachment = "";
 
 	public Email(Activity a) {
 		super();
 		setMsgType(MainActivity.MESSAGE_TYPE_EMAIL);
 		this.a = a;
 	}
 	
 	@Override
 	public boolean validate() throws ErrorInMessageException {
 		EditText recipientView = (EditText) a.findViewById(R.id.email_recipient);
 		EditText subjectView = (EditText) a.findViewById(R.id.email_subject);
 		EditText bodyView = (EditText) a.findViewById(R.id.email_body);
 		TextView attachmentView = (TextView) a.findViewById(R.id.file_attachment);
 		
 		recipient = recipientView.getText().toString();
 		subject = subjectView.getText().toString();
 		body = bodyView.getText().toString();
 		attachment = attachmentView.getText().toString();
 		
 		if(recipient.equalsIgnoreCase("")) {
 			throw new ErrorInMessageException(
 					ErrorInMessageException.ERROR_MISSINGRECIPIENT);
 		} else if(subject.equalsIgnoreCase("")) {
 			throw new ErrorInMessageException(
 					ErrorInMessageException.ERROR_MISSINGSUBJECT);
 		} else if(body.equalsIgnoreCase("")) {
 			throw new ErrorInMessageException(
 					ErrorInMessageException.ERROR_MISSINGBODY);
 		} else {
 			return true;
 		}
 	}
 	
 	@Override
 	public void showErrorMessage(ErrorInMessageException e) {
 		Toast.makeText(a.getBaseContext(), e.getErrorMessage(), Toast.LENGTH_LONG).show();
 	}
 	
 	@Override
 	public void drawMessageGUI() {
 		a.setTitle(a.getResources().getString(R.string.title_activity_write_email));
 		a.setContentView(R.layout.activity_write_email);
 	}
 
 	@Override
 	public void sendMessage() {
 		try {
 			if(validate()) {
 				setSentState(MESSAGE_STATE_SENT);
 				
 				final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
 		        emailIntent.setType("plain/text");
 		        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{recipient});
 		        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
 		        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, body);
 		        
 		        if(!attachment.equalsIgnoreCase("")) {
 		        	//todo add attachment
 		        }
 		        
 		        try {
 	        	   a.startActivityForResult(Intent.createChooser(emailIntent, "Send Mail..."), WriteMessage.SEND_REQUEST_CODE);
 	        	} catch (android.content.ActivityNotFoundException ex) {
 	        	   Toast.makeText(a, "Kein Emailclient gefunden.", Toast.LENGTH_SHORT).show();
 	        	}
 			}
 		} catch (ErrorInMessageException e) {
 			setSentState(MESSAGE_STATE_NOTSENT);
 			showErrorMessage(e);
 		} finally {
			writeLog("#to:" + recipient + "#subject:" + subject + "#msg:" + body + "#attachment:" + attachment);
 		}
 	}
 }
