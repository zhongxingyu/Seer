 package com.hotteststudio.epubreader;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Handler;
 import android.util.Log;
 
 import com.hotteststudio.model.UserSettings;
 import com.hotteststudio.model.XMLHandler;
 import com.hotteststudio.util.XCommon;
 
 public class SplashActivity extends Activity {
 	Handler handler;
 	XMLHandler xmlHandler;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 	
 		
 		setContentView(R.layout.activity_splash);
 
 		handler = new Handler();
 		startMainActivity();
 	}
 	
 	public Handler getHandler() {
 		return handler;
 	}
 	public Context getActivityContext(){
 		return this;
 	}
 	
 	private void startMainActivity() {
 		getHandler().postDelayed(new Runnable() {
 			
 			@Override
 			public void run() {
 				if (SplashActivity.this.isFinishing()) {
 					return;
 				}
 				
 				xmlHandler = new XMLHandler();
 				UserSettings settings;
 				String xmlContent = MainActivity.getXMLSettings();
				if (xmlContent.length()>1) {
 					settings = loadXML(xmlContent);
 				} else {
 					settings = new UserSettings(); 
 				}
 				
 				if (settings.isFirst) {
 					settings.isFirst = false;
 					String strXML = xmlHandler.xstream.toXML(settings);
 					XCommon.saveTextToFile(XCommon.XML_FILE, strXML, false);
 					
 					startActivity(new Intent(getActivityContext(), IntroActivity.class));
 					finish();
 					return;
 				}
 				startActivity(new Intent(getActivityContext(), MainActivity.class));
 				finish();
 			}
 		}, 3000);
 	}
 	
 	public UserSettings loadXML(String xml) {
 		try {
 			return (UserSettings)xmlHandler.xstream.fromXML(xml);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 }
