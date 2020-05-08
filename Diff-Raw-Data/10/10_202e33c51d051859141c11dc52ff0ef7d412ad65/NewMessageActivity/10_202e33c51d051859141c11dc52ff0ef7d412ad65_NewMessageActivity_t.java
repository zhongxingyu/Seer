 package ca.ryerson.scs.rus.messenger;
 
 import java.util.Calendar;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import ca.ryerson.scs.rus.MenuActivity;
 import ca.ryerson.scs.rus.R;
 import ca.ryerson.scs.rus.SplashActivity;
 import ca.ryerson.scs.rus.adapters.HttpRequestArrayAdapter;
 import ca.ryerson.scs.rus.util.DefaultUser;
 import ca.ryerson.scs.rus.util.URLResource;
 import ca.ryerson.scs.rus.util.ValidityCheck;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.Window;
 import android.view.View.OnClickListener;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.Toast;
 import android.widget.TextView;
 
 public class NewMessageActivity extends Activity implements OnClickListener {
 	private TextView btnSend, tvName;
 	private EditText evMessage;
 	private LinearLayout hideKeyboard;
 
 	Intent intent;
 	Context context;
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
 		setContentView(R.layout.new_message);
 		intent = getIntent();
 		context = this;
 
 		btnSend = (TextView) findViewById(R.id.BtnSend);
 		evMessage = (EditText) findViewById(R.id.EVMessage);
 		tvName = (TextView) findViewById(R.id.TVReceiver);
 		hideKeyboard = (LinearLayout) findViewById(R.id.hideKeyboard);
 
 		tvName.setText(intent.getStringExtra("receiver"));
 
 		btnSend.setFocusable(true);
 		hideKeyboard.setFocusable(true);
 
 		btnSend.setOnClickListener(this);
 		hideKeyboard.setOnClickListener(this);
 	}
 
 	@Override
 	public void onClick(View v) {
 		if (v == hideKeyboard) {
 			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
 			imm.hideSoftInputFromWindow(evMessage.getWindowToken(), 0);
 		} else if (v == btnSend) {
 			if (SplashActivity.DEBUG) {
 				if (SplashActivity.DEBUG)
 					Log.i(MenuActivity.TAG, "Send Button");
 			}
 
 			if ((evMessage.getText().toString() != "")) {
 				String mydate = java.text.DateFormat.getDateTimeInstance()
 						.format(Calendar.getInstance().getTime());
 				mydate.replaceAll("\\s", "");
 				String URLfinal = ValidityCheck
 						.whiteSpace(URLResource.SEND_MESSAGES + "?sender="
 								+ DefaultUser.getUser() + "&user="
 								+ intent.getStringExtra("receiver")
 								+ "&message=" + evMessage.getText().toString()
 								+ "&date=" + mydate);
 
 				HttpRequestArrayAdapter.httpRequest(this, URLfinal,
 						new UpdateHandler());
 			} else {
 				Toast.makeText(getApplicationContext(),
 						"Message/Recipient Cannot Be Empty", Toast.LENGTH_LONG)
 						.show();
 			}
 
 		}
 
 	}
 
 	private class UpdateHandler implements
 			HttpRequestArrayAdapter.ResponseHandler {
 		@Override
 		public void postResponse(JSONArray response) {
 
 			try {
 				if (response.getJSONObject(0).getString("Status")
 						.equals("Success")) {
 					Toast.makeText(context,
 							response.getJSONObject(0).getString("Status"),
 							Toast.LENGTH_LONG).show();
 				} else {
 					Toast.makeText(context, "Service Currently Unavailable",
 							Toast.LENGTH_LONG).show();
 				}
 
 				finish();
 			}
 
 			catch (JSONException e) {
 				Toast.makeText(context, "Service Currently Unavailable",
 						Toast.LENGTH_LONG).show();
 			}
 		}
 
 		@Override
 		public void postTimeout() {
 			Toast.makeText(context, "Connection timed out", Toast.LENGTH_LONG)
 					.show();
 		}
 	}
 }
