 package org.racenet.racesow;
 
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.message.BasicNameValuePair;
 import org.racenet.framework.XMLParser;
 import org.racenet.helpers.IsServiceRunning;
 import org.racenet.racesow.R;
 import org.racenet.racesow.models.Database;
 import org.racenet.racesow.models.PasswordPreference;
 import org.racenet.racesow.models.RecoveryPreference;
 import org.racenet.racesow.threads.HttpLoaderTask;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 
 import android.app.AlertDialog;
 import android.app.NotificationManager;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.DialogInterface.OnCancelListener;
 import android.content.DialogInterface.OnClickListener;
 import android.os.Bundle;
 import android.os.PowerManager;
 import android.os.PowerManager.WakeLock;
 import android.preference.EditTextPreference;
 import android.preference.Preference;
 import android.preference.PreferenceScreen;
 import android.preference.Preference.OnPreferenceChangeListener;
 import android.preference.PreferenceActivity;
 import android.text.Editable;
 import android.text.InputType;
 import android.text.TextWatcher;
 import android.text.method.PasswordTransformationMethod;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ScrollView;
 import android.widget.TextView;
 
 /**
  * Activity to handle the game settings
  * 
  * @author soh#zolex
  *
  */
 public class Settings extends PreferenceActivity implements HttpCallback {
 	
 	WakeLock wakeLock;
 	String nick;
 	ProgressDialog pd;
 	SharedPreferences prefs;
 	boolean quitAfterLogin = false;
 	PasswordPreference password;
 	RecoveryPreference recovery;
 	
     @Override
     /**
      * create the view and add handle changes in the settings
      */
     protected void onCreate(Bundle savedInstanceState) {
     	
         super.onCreate(savedInstanceState);
         addPreferencesFromResource(R.xml.settings);
         setContentView(R.layout.listview);
         
         PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
     	this.wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "racesow");
     	this.wakeLock.acquire();
         
     	this.prefs = Settings.this.getSharedPreferences("racesow", Context.MODE_PRIVATE);
     	
     	this.nick = this.prefs.getString("name", "");
     	
     	if (getIntent().getBooleanExtra("setNick", false)) {
     		
     		editNick();
     		
     	} else if (getIntent().getBooleanExtra("enterPassword", false)) {
     		
     		quitAfterLogin = true;
    		showLogin("Your session has expired. Please enter your password.");
     	}
     	
         OnPreferenceChangeListener listener = new OnPreferenceChangeListener() {
 			
 			public boolean onPreferenceChange(Preference pref, Object value) {
 				
 				if (pref.getKey().equals("sound")) {
 					
 					prefs.edit().putBoolean("sound", value.toString().equals("true") ? true : false).commit();
 
 				} else if (pref.getKey().equals("ambience")) {
 					
 					prefs.edit().putBoolean("ambience", value.toString().equals("true") ? true : false).commit();
 
 				} else if (pref.getKey().equals("gfx")) {
 					
 					prefs.edit().putBoolean("gfx", value.toString().equals("true") ? true : false).commit();
 					
 				} else if (pref.getKey().equals("blur")) {
 					
 					prefs.edit().putBoolean("blur", value.toString().equals("true") ? true : false).commit();
 					
 				} else if (pref.getKey().equals("name")) {
 					
 					nick = value.toString().trim();
 					if (nick.equals("")) {
 				
 						askEditNick();
 						
 					} else {
 					
 						String session = Database.getInstance().getSession(nick);
 						if (session != null && !session.equals("")) {
 							
 							checkSession(session);
 							
 						} else {
 						
 							checkNick();
 						}
 					}
 				
 				} else if (pref.getKey().equals("ups")) {
 					
 					prefs.edit().putBoolean("ups", value.toString().equals("true") ? true : false).commit();
 
 				} else if (pref.getKey().equals("fps")) {
 					
 					prefs.edit().putBoolean("fps", value.toString().equals("true") ? true : false).commit();
 					
 				} else if (pref.getKey().equals("demos")) {
 					
 					prefs.edit().putBoolean("demos", value.toString().equals("true") ? true : false).commit();
 					
 				} else if (pref.getKey().equals("notification")) {
 					
 					prefs.edit().putString("notification", value.toString()).commit();
 					
 				} else if (pref.getKey().equals("icon")) {
 					
 					NotificationManager manager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
 					if (value.toString() == "true") {
 						
 						if (IsServiceRunning.check("org.racenet.racesow.PullService", getApplicationContext())) {
 							
 					        manager.notify(PullService.SERVICE_NOTIFICATION,
 					        	PullService.getServiceNotification(getApplicationContext(), Settings.this));
 						}
 						
 					} else {
 						
 						manager.cancel(PullService.SERVICE_NOTIFICATION);
 					}
 					
 					prefs.edit().putBoolean("icon", value.toString().equals("true") ? true : false).commit();
 				}
 				
 				return true;
 			}
 		};
         
 		findPreference("name").setOnPreferenceChangeListener(listener);
 		findPreference("password").setOnPreferenceChangeListener(listener);
 		findPreference("recovery").setOnPreferenceChangeListener(listener);
 		findPreference("demos").setOnPreferenceChangeListener(listener);
 		findPreference("sound").setOnPreferenceChangeListener(listener);
 		findPreference("ambience").setOnPreferenceChangeListener(listener);
 		findPreference("gfx").setOnPreferenceChangeListener(listener);
 		findPreference("blur").setOnPreferenceChangeListener(listener);
 		findPreference("ups").setOnPreferenceChangeListener(listener);
 		findPreference("fps").setOnPreferenceChangeListener(listener);
 		findPreference("icon").setOnPreferenceChangeListener(listener);
 		findPreference("notification").setOnPreferenceChangeListener(listener);
 		
 		recovery = (RecoveryPreference)findPreference("recovery");
 		recovery.setRequestListener(new OnClickListener() {
 			
 			public void onClick(DialogInterface dialog, int which) {
 				
 				showRequestRecovery(false);
 			}
 		});
 		recovery.setRecoveryListener(new OnClickListener() {
 			
 			public void onClick(DialogInterface dialog, int which) {
 
 				sendRecoveryRequest(recovery.getCode());
 			}
 		});
 		
 		password = (PasswordPreference)findPreference("password");
 		password.setMessage("Change password for '"+ this.nick +"'");
 		password.setListener(new OnClickListener() {
 			
 			public void onClick(DialogInterface dialog, int which) {
 				
 				String session = Database.getInstance().getSession(nick);
 				if (session == null || session.equals("")) {
 					
 					new AlertDialog.Builder(Settings.this)
 						.setCancelable(false)
 						.setMessage("You must be logged in to change your password.")
 						.setNeutralButton("OK", null)
 						.show();
 					return;
 				}
 				
 				String url = "http://racesow2d.warsow-race.net/accounts.php";
 				List<NameValuePair> values = new ArrayList<NameValuePair>();
 				values.add(new BasicNameValuePair("action", "pass"));
 				values.add(new BasicNameValuePair("pass", password.getPassword()));
 				values.add(new BasicNameValuePair("session", session));
 				final HttpLoaderTask task = new HttpLoaderTask(Settings.this);
 				task.execute(url, values);
 				
 				pd = new ProgressDialog(Settings.this);
 				pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
 				pd.setMessage("Changing password...");
 				pd.setCancelable(true);
 				pd.setOnCancelListener(new OnCancelListener() {
 					
 					public void onCancel(DialogInterface dialog) {
 
 						task.cancel(true);
 						if (quitAfterLogin) {
 							
 							Settings.this.onBackPressed();
 							
 						} else {
 						
 							editNick();
 						}
 					}
 				});
 				pd.show();
 			}
 		});
     }
     
     /**
      * Check if the session is valid
      * 
      * @param String session
      */
     private void checkSession(String session) {
     	
     	String url = "http://racesow2d.warsow-race.net/accounts.php";
 		List<NameValuePair> values = new ArrayList<NameValuePair>();
 		values.add(new BasicNameValuePair("action", "session"));
 		values.add(new BasicNameValuePair("id", session));
 		final HttpLoaderTask task = new HttpLoaderTask(Settings.this);
 		task.execute(url, values);
 		
 		pd = new ProgressDialog(Settings.this);
 		pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
 		pd.setMessage("Checking session...");
 		pd.setCancelable(true);
 		pd.setOnCancelListener(new OnCancelListener() {
 			
 			public void onCancel(DialogInterface dialog) {
 
 				task.cancel(true);
 				editNick();
 			}
 		});
 		pd.show();
     }
     
     /**
      * Check if the nick is available
      */
     private void checkNick() {
     	
     	String url = "http://racesow2d.warsow-race.net/accounts.php";
 		List<NameValuePair> values = new ArrayList<NameValuePair>();
 		values.add(new BasicNameValuePair("action", "check"));
 		values.add(new BasicNameValuePair("name", nick));
 		final HttpLoaderTask task = new HttpLoaderTask(Settings.this);
 		task.execute(url, values);
 		
 		pd = new ProgressDialog(Settings.this);
 		pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
 		pd.setMessage("Checking name...");
 		pd.setCancelable(true);
 		pd.setOnCancelListener(new OnCancelListener() {
 			
 			public void onCancel(DialogInterface dialog) {
 
 				task.cancel(true);
 				editNick();
 			}
 		});
 		pd.show();
     }
     
     /**
      * Called by HttpLoaderTask
      * 
      * @param InputStream xmlStream
      */
 	public void httpCallback(InputStream xmlStream) {
 				
 		pd.dismiss();
 		
 		// not online, just use the nick for now without checking it
 		if (xmlStream == null) {
 			
 			setNickName(nick);
 			return;
 		}
 		
 		XMLParser parser = new XMLParser();
 		parser.read(xmlStream);
 		
 		// error response
 		NodeList errors = parser.doc.getElementsByTagName("error");
 		if (errors.getLength() == 1) {
 			
 			String returnTo = null;
 			if (parser.doc.getElementsByTagName("registration").getLength() == 1) {
 				
 				returnTo = "registration";
 			
 			} else if (parser.doc.getElementsByTagName("recovery").getLength() == 1) {
 				
 				returnTo = "recovery";
 			}
 			
 			String message = parser.getNodeValue(errors.item(0));
 			showError(message, returnTo);
 			return;
 		}
 			
 		// name availability check response
 		NodeList checks = parser.doc.getElementsByTagName("checkname");
 		if (checks.getLength() == 1) {
 			
 			try {
 				
 				int available = Integer.parseInt(parser.getValue((Element)checks.item(0), "available"));
 				if (available == 1) {
 					
 					askRegister();
 					
 				} else {
 					
 					askLogin();
 				}
 				
 			} catch (NumberFormatException e) {}
 			return;
 		}
 		
 		// login response
 		NodeList auths = parser.doc.getElementsByTagName("auth");
 		if (auths.getLength() == 1) {
 			
 			try {
 
 				Element auth = (Element)auths.item(0);
 				int result = Integer.parseInt(parser.getValue(auth, "result"));
 				if (result == 1) {
 					
 					String session = parser.getValue(auth, "session");
 					loginSuccessful(session);
 					
 				} else {
 					
 					showLogin("Wrong password. Please try again.");
 				}
 				
 			} catch (NumberFormatException e) {}
 			return;
 		}
 		
 		// session response
 		NodeList sessions = parser.doc.getElementsByTagName("checksession");
 		if (sessions.getLength() == 1) {
 			
 			try {
 				
 				Element session = (Element)sessions.item(0);
 				int result = Integer.parseInt(parser.getValue(session, "result"));
 				if (result == 1) {
 					
 					Database.getInstance().setSession(nick, parser.getValue(session, "session"));
 					setNickName(nick);
 					
 				} else {
 					
 					showLogin("Your session has expired.\nPlease enter your Password.");
 				}
 				
 			} catch (NumberFormatException e) {}
 			return;
 		}
 		
 		// registration response
 		NodeList registrations = parser.doc.getElementsByTagName("registration");
 		if (registrations.getLength() == 1) {
 			
 			try {
 				
 				Element registration = (Element)registrations.item(0);
 				int result = Integer.parseInt(parser.getValue(registration, "result"));
 				if (result == 1) {
 					
 					Database.getInstance().setSession(nick, parser.getValue(registration, "session"));
 					setNickName(nick);
 					new AlertDialog.Builder(Settings.this)
 						.setCancelable(false)
 						.setMessage("Registration successful.")
 						.setNeutralButton("OK", null)
 						.show();
 					
 				} else {
 					
 					showError("Could not complete registration.", "registration");
 				}
 				
 			} catch (NumberFormatException e) {}
 			return;
 		}
 		
 		// request recovery response
 		NodeList recoveries = parser.doc.getElementsByTagName("recovery");
 		if (recoveries.getLength() == 1) {
 			
 			try {
 
 				Element recovery = (Element)recoveries.item(0);
 				int result = Integer.parseInt(parser.getValue(recovery, "result"));
 				if (result == 1) {
 					
 					new AlertDialog.Builder(Settings.this)
 						.setCancelable(false)
 				        .setMessage("An E-Mail with instructions has been sent to your address.")
 				        .setNeutralButton("OK", new OnClickListener() {
 							
 							public void onClick(DialogInterface dialog, int which) {
 								
 								showPasswordRecovery();
 							}
 						})
 				        .show();
 						
 				} else {
 					
 					showError("E-Mail could not be sent. Please try again.", "recovery");
 				}
 				
 			} catch (NumberFormatException e) {}
 			return;
 		}
 		
 		// new password response
 		NodeList newpasswords = parser.doc.getElementsByTagName("newpass");
 		if (newpasswords.getLength() == 1) {
 			
 			Element newpass = (Element)newpasswords.item(0);
 			String session = parser.getValue(newpass, "session");
 			if (!session.equals("")) {
 				
 				Database.getInstance().setSession(nick, session);
 				showNewPassword();
 				
 			} else {
 				
 				showError("Could not send code.", "recovery");
 			}
 			
 			return;
 		}
 		
 		// password change response
 		NodeList passwords = parser.doc.getElementsByTagName("pass");
 		if (passwords.getLength() == 1) {
 			
 			Element password = (Element)passwords.item(0);
 			int result = Integer.parseInt(parser.getValue(password, "result"));
 			if (result == 1) {
 				
 				String session = parser.getValue(password, "session");
 				Database.getInstance().setSession(nick, session);
 				new AlertDialog.Builder(Settings.this)
 					.setCancelable(false)
 					.setMessage("Password changed.")
 					.setNeutralButton("OK", null)
 					.show();
 				
 			} else {
 				
 				showError("Could not change password.", null);
 			}
 			
 			return;
 		}
 	}
 	
 	/**
 	 * Show the form for entering a new password
 	 * 
 	 */
 	private void showNewPassword() {
 		
 		PreferenceScreen screen = (PreferenceScreen)findPreference("settings");
 		int itemPos = findPreference("password").getOrder();
 		screen.onItemClick(null, null, itemPos, 0);
 	}
     
 	/**
 	 * Show the password recovery dialog
 	 */
 	private void showPasswordRecovery() {
 		
 		PreferenceScreen screen = (PreferenceScreen)findPreference("settings");
 		int itemPos = findPreference("recovery").getOrder();
 		screen.onItemClick(null, null, itemPos, 0);
 	}
 
 	/**
 	 * Send the request to revocer a password
 	 * 
 	 * @param String code
 	 */
 	public void sendRecoveryRequest(String code) {
 		
 		String url = "http://racesow2d.warsow-race.net/accounts.php";
 		List<NameValuePair> values = new ArrayList<NameValuePair>();
 		values.add(new BasicNameValuePair("action", "newpass"));
 		values.add(new BasicNameValuePair("code", code));
 		final HttpLoaderTask task = new HttpLoaderTask(Settings.this);
 		task.execute(url, values);
 		
 		pd = new ProgressDialog(Settings.this);
 		pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
 		pd.setMessage("Checking code...");
 		pd.setCancelable(true);
 		pd.setOnCancelListener(new OnCancelListener() {
 			
 			public void onCancel(DialogInterface dialog) {
 
 				task.cancel(true);
 				showPasswordRecovery();
 			}
 		});
 		pd.show();
 	}
 	
 	/**
 	 * Show an error message
 	 * 
 	 * @param String message
 	 */
 	private void showError(String message, final String returnTo) {
 		
 		new AlertDialog.Builder(Settings.this)
 			.setCancelable(false)
 	        .setMessage("Error: " + message)
 	        .setNeutralButton("OK", new OnClickListener() {
 				
 				public void onClick(DialogInterface dialog, int which) {
 					
 					if (returnTo != null) {
 						
 						if (returnTo.equals("registration")) {
 							
 							showRegistration();
 						
 						} else if (returnTo.equals("recovery")) {
 							
 							showRequestRecovery(true);
 						}
 					}
 				}
 			})
 	        .show();
 	}
 	
 	/**
 	 * On Successful login
 	 * 
 	 * @param String session
 	 */
 	private void loginSuccessful(String session) {
 		
 		new AlertDialog.Builder(Settings.this)
 			.setCancelable(false)
 	        .setMessage("Login successful")
 	        .setNeutralButton("OK", new OnClickListener() {
 				
 				public void onClick(DialogInterface dialog, int which) {
 					
 					if (quitAfterLogin) {
 						
 						Settings.this.onBackPressed();
 					}
 				}
 			})
 	        .show();
 		
 		Database.getInstance().setSession(nick, session);
 		setNickName(nick);
 	}
 	
 	/**
 	 * Set the current nickname
 	 * 
 	 * @param String nick
 	 */
 	public void setNickName(String nick) {
 		
 		this.password.setMessage("Change password for '"+ nick +"'");
 		prefs.edit().putString("name", nick).commit();
 		if (!IsServiceRunning.check("org.racenet.racesow.PullService", getApplicationContext())) {
         		
     		startService(new Intent(getApplicationContext(), PullService.class));
     	}
 	}
 	
 	/**
 	 * Ask the user to login
 	 */
 	private void askLogin() {
 		
 		new AlertDialog.Builder(Settings.this)
 			.setCancelable(false)
 	        .setMessage("The name '"+ this.nick +"' is registered.\nDo you want to login?")
 	        .setPositiveButton("Yes", new OnClickListener() {
 				
 				public void onClick(DialogInterface dialog, int which) {
 	
 					showLogin("Please enter your password.");
 				}
 			})
 			.setNegativeButton("No", new OnClickListener() {
 				
 				public void onClick(DialogInterface dialog, int which) {
 	
 					((EditTextPreference)findPreference("name")).setText("");
 					editNick();
 				}
 			})
 	        .show();
 	}
 	
 	/**
 	 * Ask the user to register the name
 	 */
 	private void askRegister() {
 		
 		new AlertDialog.Builder(Settings.this)
 			.setCancelable(false)
 	        .setMessage("Do you want to register the name '"+ this.nick +"'?")
 	        .setPositiveButton("Yes", new OnClickListener() {
 				
 				public void onClick(DialogInterface dialog, int which) {
 				
 					showRegistration();
 				}
 			})
 			.setNegativeButton("No, thanks", new OnClickListener() {
 				
 				public void onClick(DialogInterface dialog, int which) {
 					
 					setNickName(nick);
 				}
 			})
 	        .show();
 	}
 	
 	/**
 	 * Show the registration form
 	 */
 	private void showRegistration() {
 		
 		ScrollView layout = (ScrollView)View.inflate(Settings.this, R.layout.registration, null);
 		final EditText name = (EditText)layout.findViewById(R.id.field_name);
 		final TextView nameError = (TextView)layout.findViewById(R.id.name_error);
 		final EditText email = (EditText)layout.findViewById(R.id.field_email);
 		final TextView emailError = (TextView)layout.findViewById(R.id.email_error);
 		final EditText pass = (EditText)layout.findViewById(R.id.field_pass);
 		final EditText conf = (EditText)layout.findViewById(R.id.field_conf);
 		final TextView confError = (TextView)layout.findViewById(R.id.conf_error);
 		
 		name.setText(nick);
 		
 		final AlertDialog register = new AlertDialog.Builder(Settings.this)
 			.setView(layout)
 			.setCancelable(true)
 			.setPositiveButton("Register", new OnClickListener() {
 				
 				public void onClick(DialogInterface dialog, int which) {
 					
 					String url = "http://racesow2d.warsow-race.net/accounts.php";
 					List<NameValuePair> values = new ArrayList<NameValuePair>();
 					values.add(new BasicNameValuePair("action", "register"));
 					values.add(new BasicNameValuePair("name", name.getText().toString().trim()));
 					values.add(new BasicNameValuePair("pass", pass.getText().toString().trim()));
 					values.add(new BasicNameValuePair("email", email.getText().toString().trim()));
 					final HttpLoaderTask task = new HttpLoaderTask(Settings.this);
 					task.execute(url, values);
 					
 					pd = new ProgressDialog(Settings.this);
 					pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
 					pd.setMessage("Registering...");
 					pd.setCancelable(true);
 					pd.setOnCancelListener(new OnCancelListener() {
 						
 						public void onCancel(DialogInterface dialog) {
 
 							task.cancel(true);
 							editNick();
 						}
 					});
 					pd.show();
 				}
 			})
 	        .setNegativeButton("Cancel", null)
 	        .create();
 		
 		TextWatcher listener = new TextWatcher() {
 			
 			public void onTextChanged(CharSequence s, int start, int before, int count) {
 				
 				String nick = name.getText().toString().trim();
 				String password = pass.getText().toString().trim();
 				String confirmation = conf.getText().toString().trim();
 				boolean valid = true;
 				if (nick.length() > 0) {
 					
 					nameError.setVisibility(View.GONE);
 					
 				} else {
 					
 					valid = false;
 					nameError.setVisibility(View.VISIBLE);
 				}
 				
 				if (email.getText().toString().matches(".+@.+\\..+")) {
 					
 					emailError.setVisibility(View.GONE);
 					
 				} else {
 					
 					valid = false;
 					emailError.setVisibility(View.VISIBLE);
 				}
 				
 				if (password.length() > 0 && password.equals(confirmation)) {
 					
 					confError.setVisibility(View.GONE);
 					
 				} else {
 					
 					valid = false;
 					confError.setVisibility(View.VISIBLE);
 				}
 				
 				Button button = register.getButton(AlertDialog.BUTTON_POSITIVE);
 				if (button != null) {
 				
 					button.setEnabled(valid);
 				}
 			}
 			
 			public void beforeTextChanged(CharSequence s, int start, int count,int after) {}
 			public void afterTextChanged(Editable s) {}
 		};
 		
 		name.addTextChangedListener(listener);
 		email.addTextChangedListener(listener);
 		pass.addTextChangedListener(listener);
 		conf.addTextChangedListener(listener);
 		
 		register.show();
 		register.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
 	}
 	
 	/**
 	 * Show the login window
 	 */
 	private void showLogin(String message) {
 		
 		final EditText pass = new EditText(Settings.this);
 		pass.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
 		pass.setTransformationMethod(PasswordTransformationMethod.getInstance());
 		AlertDialog login = new AlertDialog.Builder(Settings.this)
 			.setCancelable(true)
 			.setView(pass)
 	        .setMessage(message)
 	        .setPositiveButton("Login", new OnClickListener() {
 				
 				public void onClick(DialogInterface dialog, int which) {
 
 					String password = pass.getText().toString();
 					
 					String url = "http://racesow2d.warsow-race.net/accounts.php";
 					List<NameValuePair> values = new ArrayList<NameValuePair>();
 					values.add(new BasicNameValuePair("action", "auth"));
 					values.add(new BasicNameValuePair("name", nick));
 					values.add(new BasicNameValuePair("pass", password));
 					final HttpLoaderTask task = new HttpLoaderTask(Settings.this);
 					task.execute(url, values);
 					
 					pd = new ProgressDialog(Settings.this);
 					pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
 					pd.setMessage("Loggin in...");
 					pd.setCancelable(true);
 					pd.setOnCancelListener(new OnCancelListener() {
 						
 						public void onCancel(DialogInterface dialog) {
 
 							task.cancel(true);
 							if (quitAfterLogin) {
 								
 								Settings.this.onBackPressed();
 								
 							} else {
 							
 								editNick();
 							}
 						}
 					});
 					pd.show();
 				}
 			})
 			.setNegativeButton("Cancel", new OnClickListener() {
 				
 				public void onClick(DialogInterface dialog, int which) {
 					
 					if (quitAfterLogin) {
 						
 						Settings.this.onBackPressed();
 						
 					} else {
 						
 						editNick();
 					}
 				}
 			})
 			.setNeutralButton("Forgot password?", new OnClickListener() {
 				
 				public void onClick(DialogInterface dialog, int which) {
 					
 					showRequestRecovery(true);
 				}
 			})
 			.create();
 		
 		login.setOnCancelListener(new OnCancelListener() {
 			
 			public void onCancel(DialogInterface dialog) {
 				
 				if (quitAfterLogin) {
 					
 					Settings.this.onBackPressed();
 					
 				} else {
 				
 					editNick();
 				}
 			}
 		});
 		
 		login.show();
 	        
 	}
 	
 	/**
 	 * Show the password recovery dialog
 	 */
 	private void showRequestRecovery(final boolean returnTo) {
 		
 		final EditText email = new EditText(Settings.this);
 		email.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
 		final AlertDialog recovery = new AlertDialog.Builder(Settings.this)
 			.setView(email)
 			.setCancelable(true)
 			.setOnCancelListener(new OnCancelListener() {
 				
 				public void onCancel(DialogInterface dialog) {
 					
 					if (returnTo) {
 					
 						showLogin("Please enter your password.");
 					}
 				}
 			})
 	        .setMessage("Enter your E-Mail addresss to receive a new password.")
 	        .setPositiveButton("OK", new OnClickListener() {
 				
 				public void onClick(DialogInterface arg0, int arg1) {
 					
 					requestRevoceryCode(email.getText().toString().trim());
 				}
 			})
 			.setNegativeButton("Cancel", new OnClickListener() {
 				
 				public void onClick(DialogInterface arg0, int arg1) {
 					
 					if (returnTo) {
 					
 						showLogin("Please enter your password.");
 					}
 				}
 			})
 	        .create();
 
 		email.addTextChangedListener(new TextWatcher() {
 			
 			public void onTextChanged(CharSequence s, int start, int before, int count) {
 				
 				boolean valid;
 				if (email.getText().toString().trim().matches(".+@.+\\..+")) {
 					
 					valid = true;
 					
 				} else {
 					
 					valid = false;
 				}
 				
 				recovery.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(valid);
 			}
 			
 			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
 			public void afterTextChanged(Editable s) {}
 		});
 		
 		recovery.show();
 		recovery.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
 	}
 	
 	/**
 	 * Send a pasword recovery request
 	 * 
 	 * @param String email
 	 */
 	private void requestRevoceryCode(String email) {
 		
 		String url = "http://racesow2d.warsow-race.net/accounts.php";
 		List<NameValuePair> values = new ArrayList<NameValuePair>();
 		values.add(new BasicNameValuePair("action", "recover"));
 		values.add(new BasicNameValuePair("email", email));
 		final HttpLoaderTask task = new HttpLoaderTask(Settings.this);
 		task.execute(url, values);
 		
 		pd = new ProgressDialog(Settings.this);
 		pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
 		pd.setMessage("Requesting recovery code...");
 		pd.setCancelable(true);
 		pd.setOnCancelListener(new OnCancelListener() {
 			
 			public void onCancel(DialogInterface dialog) {
 
 				task.cancel(true);
 				showLogin("Please enter your password.");
 			}
 		});
 		pd.show();
 	}
 	
     /**
      * Ask the user to set his nickname
      */
     private void askEditNick() {
     	
     	new AlertDialog.Builder(Settings.this)
         .setMessage("Nickname must not be blank")
         .setPositiveButton("OK", new OnClickListener() {
 			
 			public void onClick(DialogInterface arg0, int arg1) {
 				
 				editNick();
 			}
 		})
         .show();
     }
     
     /**
      * Show the edit nickname preference
      */
     private void editNick() {
     	
     	PreferenceScreen screen = (PreferenceScreen)findPreference("settings");
 		int itemPos = findPreference("name").getOrder();
 		screen.onItemClick(null, null, itemPos, 0);
     }
     
     /**
      * Acquire the wakelock on resume
      */
     public void onResume() {
     	
     	super.onResume();
     	this.wakeLock.acquire();
     }
     
     /**
      * Release the wakelock on destroy
      */
     public void onDestroy() {
     	
     	super.onDestroy();
     	this.wakeLock.release();
     }
     
     /**
      * Disable animations when leaving the activity
      */
     public void onBackPressed() {
     	
     	EditTextPreference pref = (EditTextPreference)findPreference("name");
     	if (pref != null && (pref.getText() == null || pref.getText().trim().equals(""))) {
     	
     		askEditNick();
     		
     	} else {
     	
 	    	this.finish();
 	    	this.overridePendingTransition(0, 0);
     	}
     }
 }
