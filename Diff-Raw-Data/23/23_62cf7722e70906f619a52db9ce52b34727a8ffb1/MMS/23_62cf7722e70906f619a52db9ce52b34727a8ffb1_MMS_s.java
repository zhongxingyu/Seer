 package ch.zh.dipima.multichannelmobile.message;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.net.Uri;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 import ch.zh.dipima.multichannelmobile.MainActivity;
 import ch.zh.dipima.multichannelmobile.R;
 import ch.zh.dipima.multichannelmobile.WriteMessage;
 import ch.zh.dipima.multichannelmobile.exceptions.ErrorInMessageException;
 
 public class MMS extends Message implements Validatable {
 	
 	private String recipient;
 	private String body;
 	private String attachment = "";
 
 	public MMS(Activity a) {
 		super();
 		setMsgType(MainActivity.MESSAGE_TYPE_MMS);
 		this.a = a;
 	}
 	
 	@Override
 	public boolean validate() throws ErrorInMessageException {
 		EditText recipientView = (EditText) a.findViewById(R.id.mms_recipient);
 		EditText bodyView = (EditText) a.findViewById(R.id.mms_body);
 		TextView attachmentView = (TextView) a.findViewById(R.id.file_attachment);
 		
 		recipient = recipientView.getText().toString();
 		body = bodyView.getText().toString();
 		attachment = attachmentView.getText().toString();
 		
 		if(recipient.equalsIgnoreCase("")) {
 			throw new ErrorInMessageException(
 					ErrorInMessageException.ERROR_MISSINGRECIPIENT);
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
 		a.setTitle(a.getResources().getString(R.string.title_activity_write_mms));
 		a.setContentView(R.layout.activity_write_mms);
 	}
 
 	@Override
 	public void sendMessage() {
 		try {
 			if(validate()) {
 				setSentState(MESSAGE_STATE_SENT);
 				
 				Uri smsUri = Uri.parse("smsto:" + recipient);
 				final Intent intent = new Intent(Intent.ACTION_VIEW, smsUri);
 				intent.putExtra("sms_body", body);
 				intent.setData(Uri.parse("smsto:" + recipient));
 				intent.putExtra("address", recipient);
 				intent.setType("vnd.android-dir/mms-sms");
 				
 				if(!attachment.equalsIgnoreCase("")) {
 					//todo add attachment
 				}
 				
 		        
 		        try {
 		        	a.startActivityForResult(Intent.createChooser(intent, "Send MMS..."), WriteMessage.SEND_REQUEST_CODE); 
 	        	} catch (android.content.ActivityNotFoundException ex) {
 	        	   Toast.makeText(a, "Kein MMS-Client gefunden.", Toast.LENGTH_SHORT).show();
 	        	}
 			}
 		} catch (ErrorInMessageException e) {
 			setSentState(MESSAGE_STATE_NOTSENT);
 			showErrorMessage(e);
 		} finally {
			writeLog("to:" + recipient + "msg:" + body + "attachment:" + attachment);
 		}
 	}
 }
