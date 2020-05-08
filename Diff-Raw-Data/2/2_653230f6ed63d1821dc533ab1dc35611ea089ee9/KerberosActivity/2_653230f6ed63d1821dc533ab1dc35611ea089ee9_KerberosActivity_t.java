 package net.brainvitamins.kerberos;
 
 /*
  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.security.auth.callback.PasswordCallback;
 
 import net.brainvitamins.state.Edge;
 import net.brainvitamins.state.FiniteStateGraph;
 import net.brainvitamins.state.Vertex;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.text.InputType;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup.LayoutParams;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import edu.mit.kerberos.R;
 
 public class KerberosActivity extends Activity {
 
 	private static final String LOG_TAG = "KinitActivity";
 	private static final String principalKey = "principal";
 	private SharedPreferences preferences;
 
 	private static final Vertex start = new Vertex("START");
 	private static final Vertex requestingAuthentication = new Vertex(
 			"REQUEST AUTHENTICATION");
 	private static final Vertex queryingUser = new Vertex("USER CONVERSATION");
 	private static final Vertex sentCredentials = new Vertex("SENT CREDENTIALS");
 	private static final Vertex failure = new Vertex("FAILURE");
 	private static final Vertex success = new Vertex("SUCCESS");
 
 	private Button authenticateButton;
 	private Button cancelButton;
 	private Button listButton;
 	private Button destroyButton;
 	private EditText principalField;
 	private LinearLayout conversationLayout;
 	private TextView logView;
 
 	private KerberosCallbackArray callbackArray;
 
 	private static File localConfigurationFile;
 
 	private static LayoutParams passwordPromptLayoutParameters = new LayoutParams(
 			LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
 
 	final Edge toStart = new Edge(start, new Runnable() {
 		public void run() {
 			KinitOperation.cancel(); // cancel any running operations
 			logView.setText("");
 
 			authenticateButton.setText(R.string.label_start_authentication);
 			resetUIComponents();
 		}
 	});
 
 	final Edge toSuccess = new Edge(success, new Runnable() {
 		public void run() {
 			SharedPreferences.Editor editor = preferences.edit();
 			editor.putString(principalKey, principalField.getText().toString());
 			editor.commit();
 
 			authenticateButton.setText(R.string.label_start_authentication);
 			resetUIComponents();
 		}
 	});
 
 	final Edge toFailure = new Edge(failure, new Runnable() {
 		public void run() {
 			authenticateButton.setText(R.string.label_retry_authentication);
 
 			resetUIComponents();
 		}
 	});
 
 	final Edge toRequestingAuthentication = new Edge(requestingAuthentication,
 			new Runnable() {
 				public void run() {
 					logView.setText("");
 
 					listButton.setVisibility(View.GONE);
 					destroyButton.setVisibility(View.GONE);
 
 					authenticateButton
 							.setText(R.string.label_initializing_authentication);
 					authenticateButton.setEnabled(false);
 					authenticateButton
 							.setOnClickListener(toRequestingAuthenticationListener);
 
 					principalField.setEnabled(false);
 
 					String principal = principalField.getText().toString();
 
 					Log.d("KerberosActivity", "Starting async kinit operation.");
 					KinitOperation.execute(principal, localConfigurationFile,
 							messageHandler);
 				}
 			});
 
 	final Edge toQueryingUser = new Edge(queryingUser, new Runnable() {
 		public void run() {
 			authenticateButton.setText(R.string.label_complete_authentication);
 			authenticateButton.setEnabled(true);
 			authenticateButton.setOnClickListener(toSentCredentialsListener);
 
 			cancelButton.setVisibility(View.VISIBLE);
 
 			javax.security.auth.callback.Callback[] callbacks = callbackArray
 					.getCallbacks();
 
 			// setup UI elements
 			for (javax.security.auth.callback.Callback callback : callbacks) {
 				PasswordCallback asPasswordCallback = (PasswordCallback) callback;
 
 				EditText promptEditField = new EditText(KerberosActivity.this);
 				promptEditField.setLayoutParams(passwordPromptLayoutParameters);
 				promptEditField.setHint(asPasswordCallback.getPrompt());
 				if (!asPasswordCallback.isEchoOn())
 					promptEditField.setInputType(InputType.TYPE_CLASS_TEXT
 							| InputType.TYPE_TEXT_VARIATION_PASSWORD
 							| InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
 
 				conversationLayout.addView(promptEditField);
 			}
 
 			((EditText) conversationLayout.getChildAt(0)).requestFocus();
 		}
 	});
 
 	final Edge toSentCredentials = new Edge(sentCredentials, new Runnable() {
 		public void run() {
 			authenticateButton.setText(R.string.label_authenticating);
 			authenticateButton.setEnabled(false);
 
 			javax.security.auth.callback.Callback[] callbacks = callbackArray
 					.getCallbacks();
 
 			for (int i = 0; i < callbacks.length; i++) {
 				PasswordCallback callback = (PasswordCallback) callbacks[i];
 				EditText callbackEditText = (EditText) conversationLayout
 						.getChildAt(i);
 
 				callback.setPassword(callbackEditText.getText().toString()
 						.toCharArray());
 				// callback.setPassword("password".toCharArray());
 			}
 
 			callbackArray.getSource().signalCallbackProcessFinished();
 		}
 	});
 
 	@SuppressWarnings("serial")
 	private final FiniteStateGraph stateGraph = new FiniteStateGraph(
 			new HashMap<Vertex, Set<Edge>>() {
 				{
 					put(start, new HashSet<Edge>() {
 						{
 							add(toRequestingAuthentication);
 						}
 					});
 
 					put(requestingAuthentication, new HashSet<Edge>() {
 						{
 							add(toFailure);
 							add(toQueryingUser);
 						}
 					});
 
 					put(queryingUser, new HashSet<Edge>() {
 						{
 							add(toStart);
 							add(toFailure);
 							add(toSentCredentials);
 						}
 					});
 
 					put(sentCredentials, new HashSet<Edge>() {
 						{
 							add(toStart);
 							add(toFailure);
 							add(toSuccess);
 						}
 					});
 
 					put(success, new HashSet<Edge>() {
 						{
 							add(toStart); // use case: opens Settings, then
 											// returns to the main activity
 							add(toRequestingAuthentication);
 						}
 					});
 
 					put(failure, new HashSet<Edge>() {
 						{
 							add(toStart); // use case: opens Settings, then
 											// returns to the main activity
 							add(toRequestingAuthentication);
 						}
 					});
 				}
 			}, start);
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_kerberos);
 
 		authenticateButton = (Button) findViewById(R.id.authentication);
 		cancelButton = (Button) findViewById(R.id.cancel);
 		listButton = (Button) findViewById(R.id.list);
 		destroyButton = (Button) findViewById(R.id.destroy);
 
 		principalField = (EditText) findViewById(R.id.principal);
 		conversationLayout = (LinearLayout) findViewById(R.id.conversation_layout);
 		logView = (TextView) findViewById(R.id.log);
 
 		authenticateButton
 				.setOnClickListener(toRequestingAuthenticationListener);
 
 		// TODO: investigate the possibility of replacing krb5.conf or
 		// generating it from Android settings
 		// -flex/bison?
 		// TODO: share credentials between apps:
 		// http://developer.android.com/guide/topics/providers/content-providers.html
 
 		preferences = getPreferences(MODE_PRIVATE);
 		principalField.setText(preferences.getString(principalKey, ""));
 
 		localConfigurationFile = new File(getFilesDir() + File.separator
 				+ "krb5.conf");
 
 		try {
 			Log.d(LOG_TAG, "Using configuration file: "
 					+ localConfigurationFile.getCanonicalPath().toString());
 		} catch (IOException e) {
 			Log.e(LOG_TAG, e.getMessage());
 		}
 
 		// make sure localConfigurationFile has a default value
 		if (!localConfigurationFile.exists()) {
 			InputStream is;
 			try {
 				Log.d(LOG_TAG, "Initializing local configuration file "
 						+ localConfigurationFile.getCanonicalPath().toString());
 				is = getAssets().open("krb5.conf");
 				int size = is.available();
 				byte[] buffer = new byte[size];
 				is.read(buffer);
 				is.close();
 
 				FileOutputStream fos = new FileOutputStream(
 						localConfigurationFile);
 				fos.write(buffer);
 				fos.close();
 			} catch (IOException e) {
 				Log.e(LOG_TAG, e.getMessage());
 			}
 		}
 
 		listButton.setOnClickListener(new View.OnClickListener() {
 
 			public void onClick(View v) {
 				clearView();
 
 				KlistOperation.execute(Utilities.getDefaultCredentialsCache(),
 						localConfigurationFile, messageHandler);
 			}
 		});
 
 		destroyButton.setOnClickListener(new View.OnClickListener() {
 
 			public void onClick(View v) {
 				clearView();
 
 				KdestroyOperation.execute(
 						Utilities.getDefaultCredentialsCache(),
 						localConfigurationFile, messageHandler);
 			}
 		});
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_kinit, menu);
 		return true;
 	}
 
 	@Override
 	public void onStop() {
 		super.onStop();
 
 		if (stateGraph.getCurrentState() != start) {
 			stateGraph.transition(start);
 		}
 	}
 
 	// UI event handlers
 	public View.OnClickListener toRequestingAuthenticationListener = new View.OnClickListener() {
 		public void onClick(View v) {
 			stateGraph.transition(requestingAuthentication);
 		}
 	};
 
 	public View.OnClickListener toSentCredentialsListener = new View.OnClickListener() {
 		public void onClick(View v) {
 			stateGraph.transition(sentCredentials);
 		}
 	};
 
 	public void cancel(View v) {
 		KinitOperation.cancel();
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// Handle item selection
 		switch (item.getItemId()) {
 
 		case R.id.menu_settings:
 			Intent menuIntent = new Intent(this, ConfigurationActivity.class);
 			startActivity(menuIntent);
 			return true;
 
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	// library event handler
 	Handler messageHandler = new Handler() {
 		public void handleMessage(Message message) {
 			switch (message.what) {
 
 			case KerberosOperation.LOG_MESSAGE:
 				log((String) message.obj);
 				break;
 
 			case KerberosOperation.SUCCESS_MESSAGE:
 				stateGraph.transition(success);
 				break;
 
 			case KerberosOperation.FAILURE_MESSAGE:
 				stateGraph.transition(failure);
 				break;
 
 			case KerberosOperation.CANCEL_MESSAGE:
 				stateGraph.transition(start);
 				break;
 
 			case KerberosOperation.PROMPTS_MESSAGE:
 				callbackArray = (KerberosCallbackArray) message.obj;
 
 				for (javax.security.auth.callback.Callback callback : callbackArray
 						.getCallbacks()) {
 					if (!(callback instanceof PasswordCallback)) {
 						log("Unrecognized callback type sent to kinit UI: "
 								+ callback.getClass().toString());
 
 						stateGraph.transition(start);
 						return;
 					}
 				}
 
 				stateGraph.transition(queryingUser);
 				break;
 
 			default:
 				Log.d(LOG_TAG, "Unrecognized message from Kerberos operation: "
 						+ message);
 			}
 		}
 	};
 
 	private void log(String input) {
 		logView.append(input);
 	}
 
 	private void clearView() {
 		logView.setText("");
 
 		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
 		imm.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
 	}
 
 	private void resetUIComponents() {
 		authenticateButton.setEnabled(true);
 		cancelButton.setVisibility(View.GONE);
 		listButton.setVisibility(View.VISIBLE);
 		destroyButton.setVisibility(View.VISIBLE);
 
 		conversationLayout.removeAllViews();
 		principalField.requestFocus();
 
 		authenticateButton
 				.setOnClickListener(toRequestingAuthenticationListener);
 
 		principalField.setEnabled(true);
 	}
 }
