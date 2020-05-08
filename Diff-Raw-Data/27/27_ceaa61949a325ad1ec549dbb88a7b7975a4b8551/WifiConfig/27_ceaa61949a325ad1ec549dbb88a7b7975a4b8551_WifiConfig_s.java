 package com.wifi.sapguestconnect;
 
 import android.graphics.Typeface;
 import android.os.Bundle;
 import android.text.method.PasswordTransformationMethod;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 
 import com.actionbarsherlock.app.SherlockActivity;
 import com.wifi.sapguestconnect.data.DataFacade;
 import com.wifi.sapguestconnect.dialog.IDialogResult;
 import com.wifi.sapguestconnect.dialog.SelectNetworkListener;
 import com.wifi.sapguestconnect.log.LogManager;
 
 public class WifiConfig extends SherlockActivity 
 {
 	@Override
 	protected void onCreate(Bundle savedInstanceState) 
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.wifi_config);
 		
 	    LogManager.LogFunctionCall("WifiConfig", "onCreate()");
 	    
 		initUILayout();
 	    loadLoginData();
 	}
 	
 	private void loadLoginData()
 	{
 		LogManager.LogFunctionCall("WifiConfig", "loadLoginData()");
 		
 	    LoginData loadedLoginData = DataFacade.LoadLoginData(this);
 	    setUserName(loadedLoginData.getUser());
 	    setPassword(loadedLoginData.getPass());
 	    setWifiSSID(loadedLoginData.getSSID());
 	}
 
 	private void initUILayout() 
 	{
 	    LogManager.LogFunctionCall("WifiConfig", "initUILayout()");
 
 	    initPasswordLayout();
 	    initSSIDEntryLayout();
 	    initSaveBtnLayout();
 	}
 	
 	private void initPasswordLayout()
 	{
 		LogManager.LogFunctionCall("WifiConfig", "initPasswordLayout()");
 		EditText password = (EditText) findViewById(R.id.password_input);
 		password.setTypeface(Typeface.DEFAULT);
 		password.setTransformationMethod(new PasswordTransformationMethod());
 	}
 	
 	private void initSSIDEntryLayout() 
 	{
 		LogManager.LogFunctionCall("WifiConfig", "initSSIDEntryLayout()");
 		EditText wifiPicker = (EditText)findViewById(R.id.wifi_input);
 		
 		wifiPicker.setOnClickListener(new SelectNetworkListener(this, new IDialogResult() {
 					
 					@Override
 					public void OnFinish(Object result) {
 						LogManager.LogFunctionCall("WifiSettings", "initPasswordEntryLayout().SelectNetworkListener().OnFinish()");
 						setWifiSSID((String)result);
 					}
 				}
 		));
 	}
 	
 	private void initSaveBtnLayout()
 	{
 		LogManager.LogFunctionCall("WifiConfig", "initSaveBtnLayout()");
 		Button storeDataBtn = (Button)findViewById(R.id.storeDataBtn);
 		storeDataBtn.setOnClickListener( new OnClickListener() {
 			@Override
 			public void onClick(View v) 
 			{
 				LoginData loginData = new LoginData();
 				loginData.setPass(WifiConfig.this.getPassword());
 				loginData.setUser(WifiConfig.this.getUserName());
 				loginData.setSSID(WifiConfig.this.getWifiSSID());
 				DataFacade.PersistLoginData(WifiConfig.this, loginData);
 			}
 		});
 	}
 	
 	private void setUserName(String username) 
 	{
 		LogManager.LogFunctionCall("WifiConfig", "setUserName()");
 		EditText userNameInput = (EditText)findViewById(R.id.username_input);
 		userNameInput.setText(username);
 	}
 	
 	private String getUserName() 
 	{
 		LogManager.LogFunctionCall("WifiConfig", "getUserName()");
 		EditText userNameInput = (EditText)findViewById(R.id.username_input);
 		return userNameInput.getText().toString();
 	}
 
 	private void setPassword(String password) 
 	{
 		LogManager.LogFunctionCall("WifiConfig", "setPassword()");
 		EditText passwordInput = (EditText)findViewById(R.id.password_input);
 		passwordInput.setText(password);
 	}
 	
 	private String getPassword() 
 	{
 		LogManager.LogFunctionCall("WifiConfig", "getPassword()");
 		EditText passwordInput = (EditText)findViewById(R.id.password_input);
 		return passwordInput.toString();
 	}
 	
 	private void setWifiSSID(String wifiSSID) 
 	{
 		LogManager.LogFunctionCall("WifiConfig", "setWifiSSID()");
 		
 		// Update UI
 		TextView ssidText = (TextView)findViewById(R.id.wifi_input);
 		ssidText.setText(wifiSSID);
 	}
 	
 	private String getWifiSSID() 
 	{
 		LogManager.LogFunctionCall("WifiConfig", "getWifiSSID()");
 		
 		// Update UI
 		TextView ssidText = (TextView)findViewById(R.id.wifi_input);
 		return ssidText.getText().toString();
 	}
 }
