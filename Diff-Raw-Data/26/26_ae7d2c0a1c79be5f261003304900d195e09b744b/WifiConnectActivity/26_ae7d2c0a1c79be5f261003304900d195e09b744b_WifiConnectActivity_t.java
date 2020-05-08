 package com.worthwhilegames.cardgames.shared.activities;
 
 import java.net.InetAddress;
 
 import org.apache.http.conn.util.InetAddressUtils;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.text.InputType;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.inputmethod.EditorInfo;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView.OnEditorActionListener;
 
 import com.worthwhilegames.cardgames.R;
 import com.worthwhilegames.cardgames.shared.TextView;
 import com.worthwhilegames.cardgames.shared.Util;
 
 /**
  * This Activity allows a user to type in the IP address they want to connect
  * to, and the address of that device is returned in the result Intent.
  *
  * Activity Results:
  * 		RESULT_OK - If the user chose a device
  * 					The Device's MAC address will be in the result
  * 					Intent with the key WifiConnectActivity.EXTRA_DEVICE_ADDRESS
  *
  * 		RESULT_CANCELLED - If no device was chosen
  */
 public class WifiConnectActivity extends Activity implements OnEditorActionListener {
 
 	/**
 	 * Return Intent extra
 	 */
 	public static String EXTRA_DEVICE_ADDRESS = "deviceAddress";
 
 	/**
 	 * Indicates whether we are connecting via IPv6 or IPv4
 	 */
 	private boolean isIPv4 = true;
 
 	/* (non-Javadoc)
 	 * @see android.app.Activity#onCreate(android.os.Bundle)
 	 */
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.text_dialog);
 
 		TextView title = (TextView) findViewById(R.id.dialogPromptTitle);
 		title.setText(R.string.enterIpAddress);
 
 		InetAddress currentIP = Util.getLocalIpAddress();
 
 		// Send back to main menu if not connected
 		if (currentIP == null) {
 			setResult(RESULT_CANCELED);
 			finish();
 		}
 
 		// If not IPv4, set as IPv6
 		if ((currentIP != null) && !InetAddressUtils.isIPv4Address(currentIP.getHostAddress())) {
 			isIPv4 = false;
 		}
 
 		final EditText textView = (EditText) findViewById(R.id.dialogPromptTextbox);
 
 		if (isIPv4) {
			textView.setInputType(InputType.TYPE_CLASS_PHONE);
 			textView.setHint(R.string.ipv4Address);
 		} else {
 			textView.setHint(R.string.ipv6Address);
 			textView.setInputType(InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
 		}
 
 		textView.setOnEditorActionListener(this);
 
 		// create button for the view
 		Button ok = (Button) findViewById(R.id.ok);
 		ok.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				addressEntered();
 			}
 		});
 	}
 
 	/* (non-Javadoc)
 	 * @see android.app.Activity#onBackPressed()
 	 */
 	@Override
 	public void onBackPressed() {
 		setResult(RESULT_CANCELED);
 		finish();
 	}
 
 	/**
 	 * Will check the Address, and return the result to calling activity
 	 */
 	private void addressEntered() {
 		EditText textView = (EditText) findViewById(R.id.dialogPromptTextbox);
 		String ipAddress = "";
 
 		// If the user didn't enter anything, send them back to main menu
 		if (textView.getText().length() == 0) {
 			setResult(RESULT_CANCELED);
 			finish();
 		} else {
 			ipAddress = textView.getText().toString();
 
 			// Return the name the player chose with the result of this Activity
 			Intent nameToSend = new Intent();
 			nameToSend.putExtra(EXTRA_DEVICE_ADDRESS, ipAddress);
 			setResult(RESULT_OK, nameToSend);
 			finish();
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see android.widget.TextView.OnEditorActionListener#onEditorAction(android.widget.TextView, int, android.view.KeyEvent)
 	 */
 	@Override
 	public boolean onEditorAction(android.widget.TextView v, int actionId, KeyEvent event) {
 		if (EditorInfo.IME_ACTION_DONE == actionId) {
 			// Return input text to activity
 			addressEntered();
 			return true;
 		}
 		return false;
 	}
 }
 
