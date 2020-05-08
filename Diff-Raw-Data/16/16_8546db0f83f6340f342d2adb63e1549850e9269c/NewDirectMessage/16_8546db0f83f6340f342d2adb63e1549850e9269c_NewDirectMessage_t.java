 package com.tuit.ar.activities;
 
 import java.util.ArrayList;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.message.BasicNameValuePair;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.Window;
 import android.view.View.OnClickListener;
 import android.view.View.OnKeyListener;
 import android.widget.ArrayAdapter;
 import android.widget.AutoCompleteTextView;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.tuit.ar.R;
 import com.tuit.ar.api.Twitter;
 import com.tuit.ar.api.TwitterAccountRequestsObserver;
 import com.tuit.ar.api.TwitterRequest;
 import com.tuit.ar.api.request.Options;
 import com.tuit.ar.models.User;
 
 public class NewDirectMessage extends Activity implements OnClickListener, TwitterAccountRequestsObserver {
 	static private final int MAX_CHARS = 140;
 
 	private String toUser;
 	private EditText messageField;
 	private TextView charCount;
 	private Timer autocompleter = new Timer(false);
 	private AutoCompleteTextView userSelect = null;
 	private ArrayAdapter<String> usersAdapter = null;
 	private TimerTask autocompleterTask = null;
 	private Button send = null;
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
 		setContentView(R.layout.new_direct_message);
 		send = (Button)findViewById(R.id.send);
 		send.setOnClickListener(this);
 		Twitter.getInstance().getDefaultAccount().addRequestObserver(this);
 
 		Intent intent = getIntent();
 		toUser = intent.getStringExtra("to_user");
 		TextView username = (TextView)findViewById(R.id.toUser);
 		userSelect = (AutoCompleteTextView)findViewById(R.id.toUserSelect);
 		if (toUser != null) {
 			username.setText("To @" + toUser);
 			userSelect.setVisibility(View.INVISIBLE);
 		} else {
 			username.setVisibility(View.INVISIBLE);
 			usersAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, new String[]{});
 			userSelect.setAdapter(usersAdapter);
 			userSelect.addTextChangedListener(new TextWatcher() {
 				public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
 				}
 				
 				public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
 						int arg3) {
 				}
 				
 				public void afterTextChanged(Editable arg0) {
 					if (autocompleterTask != null) autocompleterTask.cancel();
 					autocompleterTask = new UpdateUsernameToSelect();
 					autocompleter.schedule(autocompleterTask, 1000);
 				}
 			});
 		}
 
 		messageField = (EditText) findViewById(R.id.tweetMessage);
 		charCount = (TextView) findViewById(R.id.charCount);
 
 		messageField.setOnKeyListener(new OnKeyListener() {
 			public boolean onKey(View v, int keyCode, KeyEvent event) {
 				updateCharCount();
 				return false;
 			}
 		});
 	}
 
 	private void updateCharCount() {
 		charCount.setText(String.valueOf(MAX_CHARS - messageField.getText().toString().length()));
 	}
 
 	public void onClick(View v) {
 		if (toUser == null) {
 			String user = userSelect.getText().toString();
 			toUser = user;
 		}
 
 		if (toUser == null) {
 			// FIXME: improve error message?
 			sendFailed(null);
 			return;
 		}
 
 		String message = messageField.getText().toString();
 
 		if (message == null || message.length() == 0) {
 			// FIXME: improve error message?
 			sendFailed(null);
 			return;
 		}
 
 		try {
 			ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
 			params.add(new BasicNameValuePair("text", message));
 			params.add(new BasicNameValuePair("screen_name", toUser));
 			int method = TwitterRequest.METHOD_POST;
 			Twitter.getInstance().getDefaultAccount().requestUrl(Options.SEND_DIRECT_MESSAGE, params, method);
 		} catch (Exception e) {
 			sendFailed(e.getLocalizedMessage());
 		}
 	}
 	public void sendFailed(String message) {
 		if (message == null)
 			Toast.makeText(this, getString(R.string.unableToPost), Toast.LENGTH_SHORT).show();
 		else
			Toast.makeText(this, getString(R.string.unableToPost) + " (" + message + ")", Toast.LENGTH_SHORT).show();
 	}
 
 	public void requestHasStarted(TwitterRequest request) {
 		setProgressBarIndeterminateVisibility(true);
 	}
 
 	public void requestHasFinished(TwitterRequest request) {
 		setProgressBarIndeterminateVisibility(false);
     	if (request.getUrl().equals(Options.SEND_DIRECT_MESSAGE)) {
     		if (request.getStatusCode() >= 200 && request.getStatusCode() < 400) {
     			Toast.makeText(this, getString(R.string.messageSent), Toast.LENGTH_SHORT).show();
     			finish();
     		}
     		else sendFailed(request.getErrorMessage());
     	}
 	}
 
 	public void onDestroy() {
 		super.onDestroy();
 		send.setOnClickListener(null);
 		messageField.setOnKeyListener(null);
 		Twitter.getInstance().getDefaultAccount().removeRequestObserver(this);
 	}
 
 	protected class UpdateUsernameToSelect extends TimerTask {
 		public void run() {
 			String username = userSelect.getText().toString();
 			ArrayList<User> users = User.select("substr(screen_name,0," + username.length() + ") LIKE ?", new String[]{username}, null, null, null, "5");
 			usersAdapter.clear();
 			for (User u : users) {
 				usersAdapter.add(u.getScreenName());
 			}
 			usersAdapter.notifyDataSetChanged();
 		}
 	}
 }
