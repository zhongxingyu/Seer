 package com.mucwiz;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import java.io.File;
 
 import Spotify.Config;
 import Spotify.Session;
 import Spotify.Track;
 
 import org.codehaus.jackson.JsonGenerationException;
 import org.codehaus.jackson.map.JsonMappingException;
 
 import com.mucwiz.model.Question;
 import com.mucwiz.model.Quiz;
 import com.mucwiz.model.User;
 import com.mucwiz.webservice.MucwizApi;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Environment;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Toast;
 
 public class MainActivity extends Activity {
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         if (false)/*isLoggedIn()) */ {
         	setContentView(R.layout.main);
         	attachMainMenuListeners();
         } else {
         	setContentView(R.layout.login);
 	        attachLoginListeners();
         }
     }
     
     private void attachLoginListeners() {
     	Button b = (Button)findViewById(R.id.login);
         b.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				String user = ((EditText)findViewById(R.id.username)).getText().toString();
 				String password = ((EditText)findViewById(R.id.password)).getText().toString();
 				if (login(user, password)){
 					setContentView(R.layout.main);
 					attachMainMenuListeners();
 				}
 				else
 	                Toast.makeText(getBaseContext(), "Incorrect username/password.", Toast.LENGTH_SHORT).show();
 			}
 		});
     }
     
     private void attachMainMenuListeners() {
     	Button b = (Button)findViewById(R.id.main_create_quiz_button);
         b.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				Intent i = new Intent(MainActivity.this, CreateQuizActivity.class);
 				startActivity(i);
 			}
 		});
 	        
         b = (Button)findViewById(R.id.main_join_quiz_button);
         b.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				Intent i = new Intent(MainActivity.this, JoinActivity.class);
 				startActivity(i);
 			}
 		});
         
         b = (Button)findViewById(R.id.logout);
         b.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				//TODO: log out
 				setContentView(R.layout.login);
 				attachLoginListeners();
 			}
 		});
     }
     
     private boolean login(String username, String password){
     	User.getInstance().setUsername(username);
     	User.getInstance().setUsername(password);
     	spotifyLogin(username, password);
     	//TODO: login
     	return true;
     }
     
     private void spotifyLogin(String username, String password){
     	 File f = new File(Environment.getExternalStorageDirectory().getPath() + "/testcache/");
    	 f.mkdir();
    	 f = new File(Environment.getExternalStorageDirectory().getPath() + "/dummytracefile");
          try {
  			f.createNewFile();
  		} catch (IOException e) {
  			// TODO Auto-generated catch block
  			e.printStackTrace();
  		}
  		System.out.println("Main - create session");
  		Session session = new Session();
  		System.out.println("Main - session created");
  		
  		Config config = new Config();
  		final char[] g_appkey = {
  				0x01, 0x61, 0xF3, 0xE6, 0x59, 0xD4, 0x2C, 0x61, 0x8C, 0xA3, 0xD1, 0x69, 0x6A, 0x61, 0x1A, 0x60,
  				0x6C, 0x48, 0xB1, 0x44, 0xFD, 0x5A, 0x5F, 0xCA, 0x10, 0xCC, 0x8B, 0x95, 0xC9, 0xFF, 0x24, 0xD8,
  				0x0A, 0xC6, 0x29, 0x5E, 0x78, 0x24, 0x83, 0x56, 0x76, 0x80, 0x76, 0x86, 0xBE, 0xDE, 0x1B, 0xE0,
  				0x8A, 0x57, 0x3E, 0xCA, 0x12, 0x95, 0x1D, 0x4E, 0x67, 0xD4, 0xAA, 0xB0, 0xAE, 0xAB, 0xF5, 0xE9,
  				0xE7, 0x94, 0xE7, 0x5B, 0x1B, 0x24, 0x55, 0x1C, 0x08, 0x91, 0x28, 0x57, 0x82, 0x1D, 0x78, 0x95,
  				0xC7, 0xA9, 0xF8, 0x3F, 0x83, 0xAC, 0xE4, 0x3F, 0x55, 0x7A, 0x00, 0x3F, 0x82, 0x1A, 0x83, 0x55,
  				0x50, 0x4C, 0xED, 0xCF, 0x8A, 0x9D, 0xD8, 0x01, 0x29, 0x53, 0xD3, 0x5C, 0x4A, 0x98, 0x01, 0x20,
  				0x1C, 0x02, 0xC6, 0x0D, 0x16, 0x08, 0x15, 0x7E, 0x37, 0x1C, 0x80, 0x84, 0x9E, 0x98, 0x8C, 0x7F,
  				0x50, 0xBE, 0x75, 0x34, 0xC8, 0x02, 0x07, 0x03, 0x8C, 0xD4, 0xD4, 0x66, 0x8D, 0xE9, 0xB5, 0xD5,
  				0xA5, 0xFC, 0x41, 0x84, 0x85, 0x3C, 0xF3, 0x60, 0xD3, 0xA5, 0x1A, 0xA5, 0x57, 0xF1, 0x9C, 0xEA,
  				0x58, 0x1D, 0xCC, 0x3C, 0x23, 0x64, 0xC9, 0xA9, 0x30, 0x69, 0x5D, 0xF7, 0x8A, 0x7E, 0xD3, 0xED,
  				0xF5, 0xBA, 0xA6, 0xEF, 0xDB, 0xF7, 0x45, 0x53, 0xC9, 0x4D, 0x74, 0x93, 0xF4, 0xE4, 0x69, 0x6E,
  				0xE6, 0xBF, 0xE3, 0xDA, 0xB5, 0x83, 0xD0, 0x71, 0x08, 0xEB, 0x2C, 0xDE, 0xD9, 0xD9, 0x2F, 0xDE,
  				0xD3, 0x2C, 0xFC, 0xC8, 0x69, 0x43, 0xB4, 0xFC, 0x4E, 0x06, 0x74, 0x8E, 0x5B, 0xC2, 0x9C, 0xED,
  				0x80, 0x0E, 0x66, 0xAE, 0x1C, 0x1F, 0x87, 0xC1, 0xF6, 0x4E, 0xF4, 0xCE, 0xEC, 0xA5, 0x96, 0x3D,
  				0x6C, 0xC8, 0x56, 0x01, 0x60, 0x1E, 0xDF, 0xD9, 0x35, 0x92, 0xB5, 0x68, 0xD1, 0xE4, 0xFB, 0xB7,
  				0x95, 0x98, 0x67, 0x5D, 0x5C, 0x96, 0x87, 0xCC, 0x31, 0x70, 0xDC, 0xE1, 0x83, 0xEF, 0x8A, 0xA1,
  				0x3C, 0xF8, 0x08, 0xFF, 0xFA, 0x1E, 0x7B, 0xA2, 0x4F, 0x96, 0x39, 0x96, 0xB9, 0x6D, 0xCC, 0x28,
  				0xE4, 0x45, 0x48, 0xB0, 0x28, 0xC3, 0x46, 0x5F, 0xDB, 0xF5, 0x30, 0x51, 0xA0, 0x77, 0x65, 0xB0,
  				0x2A, 0x42, 0x63, 0x2C, 0x27, 0x08, 0x6F, 0x34, 0x62, 0x5D, 0xC6, 0xD1, 0x45, 0x97, 0x21, 0x7C,
  				0x2D,
  			};
 
  		config.m_appKey = g_appkey;
  		config.m_appKeySize = g_appkey.length;
  		config.m_cacheLocation = Environment.getExternalStorageDirectory().getPath() + "/testcache";//"/sdcard/test.cache";//AppData.g_cacheLocation;
  		config.m_settingsLocation = Environment.getExternalStorageDirectory().getPath() + "/testcache";//AppData.g_settingsLocation;
  		//config.m_tinySettings = false;
  		config.m_userAgent = "fiskterror";//AppData.g_userAgent;
  		config.m_traceFile = "/dev/null";//Environment.getExternalStorageDirectory().getPath() + "/dummytracefile";
 
  		System.out.println("Main - session initialising");
 
  		int error = session.Initialise(config);
  		System.out.printf("Main - session initialised [%d]\n", error);
  		
  		System.out.println("Main - logging in");
  		//session.Login( "Addeventure", "rulerer1337");
  		session.Login(username, password);
  		while ( !session.IsLoggedIn() )
  		{
  			session.Update();
  			try {
  				Thread.sleep(100);
  			} catch (InterruptedException e) {
  	
  				e.printStackTrace();
  			}
  		}		
  		//session.GetTrackFromURI("spotify:track:0xAjq4KAQdJvUyCM7fFZ9K");
 
 // 		System.out.println("Main - logging out");
 		//session.Logout();
// 		while ( session.IsLoggedIn() )
// 		{
// 			session.Update();
// 		}		
  		
  		
 // 		System.out.println("Main - session shutting down");
 		//session.Shutdown();
 // 		System.out.println("Main - session shutdown");
  		
 		//session = null;
  	
     	
     }
 }
