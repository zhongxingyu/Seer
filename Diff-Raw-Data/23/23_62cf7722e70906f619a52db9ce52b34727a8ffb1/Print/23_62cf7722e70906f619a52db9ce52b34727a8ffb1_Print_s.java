 package ch.zh.dipima.multichannelmobile.message;
 
 import android.app.Activity;
 import android.widget.EditText;
 import android.widget.Toast;
 import ch.zh.dipima.multichannelmobile.MainActivity;
 import ch.zh.dipima.multichannelmobile.R;
 import ch.zh.dipima.multichannelmobile.exceptions.ErrorInMessageException;
 
 public class Print extends Message implements Validatable {
 	
 	private String body;
 
 	public Print(Activity a) {
 		super();
 		setMsgType(MainActivity.MESSAGE_TYPE_PRINT);
 		this.a = a;
 	}
 	
 	@Override
 	public boolean validate() throws ErrorInMessageException {
 		EditText bodyView = (EditText) a.findViewById(R.id.print_body);
 		body = bodyView.getText().toString();
 		
 		if(body.equalsIgnoreCase("")) {
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
 		a.setTitle(a.getResources().getString(R.string.title_activity_write_print));
 		a.setContentView(R.layout.activity_write_print);
 	}
 
 	@Override
 	public void sendMessage() {
 		try {
 			if(validate()) {
 				//todo really send to printer
 				setSentState(MESSAGE_STATE_SENT);
 				Toast.makeText(a.getBaseContext(), "Print erfolgreich versendet.", Toast.LENGTH_LONG).show();
 			}
 		} catch (ErrorInMessageException e) {
 			setSentState(MESSAGE_STATE_NOTSENT);
 			showErrorMessage( e);
 		} finally {
			writeLog("msg to print:" + body);
 		}
 	}
 }
