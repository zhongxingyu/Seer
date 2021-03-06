 package org.bombusim.lime.activity;
 
 import org.bombusim.lime.Lime;
 import org.bombusim.lime.R;
 import org.bombusim.lime.data.AccountsFactory;
 import org.bombusim.xmpp.XmppAccount;
import org.bombusim.xmpp.XmppJid;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.text.InputType;
 import android.view.View;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.Spinner;
 
 public class AccountSettingsActivity extends Activity {
 
	EditText editJid;
 	EditText pass;
 	EditText resource;
 	EditText priority;
 	EditText xmpphost;
 	EditText xmppport;
 	
 	CheckBox specifichostport;
 	CheckBox zlib;
 	CheckBox autologin;
 	
 	Spinner security;
 	Spinner plainpassword;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		//TODO: account selector
 		XmppAccount account = Lime.getInstance().accounts.get(0);
 		
 		setContentView(R.layout.account_settings);
 		
 		jid  = ((EditText)findViewById(R.id.jid));
 		jid.setText(account.userJid);
 		
 		pass = ((EditText)findViewById(R.id.password));
 		pass.setText(account.password);
 		//TODO: non-empty password
 		//pass.setHint("••••••••");
 		
 		resource = ((EditText)findViewById(R.id.resource));
 		resource.setText(account.resource);
 		
 		priority = ((EditText)findViewById(R.id.priority));
 		priority.setText(String.valueOf(account.priority));
 
 		autologin = ((CheckBox)findViewById(R.id.autoLogin));
 		autologin.setChecked(account.autoLogin);
 
 		xmpphost = ((EditText)findViewById(R.id.xmpphost));
 		xmpphost.setText(account.xmppHost);
 
 		xmppport = ((EditText)findViewById(R.id.xmppport));
 		xmppport.setText(String.valueOf(account.xmppPort));
 		
 		specifichostport = ((CheckBox)findViewById(R.id.specificHostPort));
 		specifichostport.setChecked(account.specificHostPort);
 		
 		zlib = ((CheckBox)findViewById(R.id.zlib));
 		zlib.setChecked(account.trafficCompression);
 		
 		//TODO: populate in runtime to ensure correct order;
 		security = ((Spinner)findViewById(R.id.ssl));
 		security.setSelection(account.secureConnection);
 		
 		//TODO: populate in runtime to ensure correct order;
 		plainpassword = ((Spinner)findViewById(R.id.plainpassword));
 		plainpassword.setSelection(account.enablePlainAuth);
 
 		
 		findViewById(R.id.advancedSettings).setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				findViewById(R.id.advancedSettings).setVisibility(View.GONE);
 				findViewById(R.id.layoutAdvancedSettings).setVisibility(View.VISIBLE);
 			}
 		});
 		
 		
 		findViewById(R.id.showPassword).setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				int inputtype = InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS;
 				
 				if (((CheckBox)v).isChecked())
 					inputtype |= InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD;
 				else
 					inputtype |= InputType.TYPE_TEXT_VARIATION_PASSWORD;
 
 				pass.setInputType(inputtype);
 			}
 		});
 
 
 		findViewById(R.id.specificHostPort).setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				updateHostState();
 			}
 		});
 
 		
 		findViewById(R.id.layoutAdvancedSettings).setVisibility(View.GONE);
 		
 		updateHostState();
 	}
 	
 	void updateHostState() {
 		int visibility = ((CheckBox)findViewById(R.id.specificHostPort)).isChecked()? View.VISIBLE : View.GONE;
 		findViewById(R.id.linearLayotHost).setVisibility(visibility);
 	}
 	
 	@Override
 	protected void onPause() {
		// TODO add save/cancel buttons into form
		// TODO check saveAccount() result
 		saveAccount();
 		super.onPause();
 	}
	boolean saveAccount() {
 		XmppAccount account = Lime.getInstance().accounts.get(0);
 
		XmppJid jid = new XmppJid(editJid.getText().toString());
		if (!jid.isValid()) return false; //not saved
 		account.password = pass.getText().toString();
		account.userJid = jid.getBareJid();
 		//TODO: non-empty password
 		//pass.setHint("••••••••");
 
 		account.resource = resource.getText().toString();
 		
 		try {
 			account.priority = Integer.parseInt(priority.getText().toString());
 		} catch (NumberFormatException ex) {
 			account.priority = XmppAccount.DEFAULT_PRIORITY;
 		}
 
 		account.autoLogin = autologin.isChecked();
 		
 		account.xmppHost = xmpphost.getText().toString();
 		try {
 			account.xmppPort = Integer.parseInt(xmppport.getText().toString());
 		} catch (NumberFormatException ex) {
 			account.xmppPort = XmppAccount.DEFAULT_XMPP_PORT;
 		}
 		
 		account.specificHostPort = specifichostport.isChecked();
 		
 		account.trafficCompression = zlib.isChecked();
 		
 		//TODO: populate in runtime to ensure correct order;
 		account.secureConnection = security.getSelectedItemPosition();
 		
 		//TODO: populate in runtime to ensure correct order;
 		account.enablePlainAuth = plainpassword.getSelectedItemPosition();
 
 		AccountsFactory.saveAccount(getApplicationContext(), account);
		
		return true;
 	}
 
 }
