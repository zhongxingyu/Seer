 package com.mainapp.mynoppaapp;
 
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 
 import connector.Connector;
 import datastructures.Course;
 import datastructures.User;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.widget.EditText;
 
 public class MainActivity extends Activity {
     public final static String EXTRA_MESSAGE = "com.mainapp.mynoppaapp.MESSAGE";
     public final static String SESSION = "session_object";
     public final static Connector CONNECTOR = new Connector();
     
 	private final static int BUFFERMAXLENGHT = 1000;
 	public final static String FILENAME = "user_data";
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 	
 		Session s = ((Session)getApplicationContext());
 		User u = new User();
 		s.setUser(u);
 		//read file
 		FileInputStream inputStream;
 		byte[] buffer = new byte[BUFFERMAXLENGHT];
 		int fileLenght;
 
 		try {
 			byte a = (byte) ';';
 			String c = "";
 			inputStream = openFileInput(FILENAME);
 			fileLenght = inputStream.read(buffer);
 			for (int i = 0; i < fileLenght; i++) {
 				byte b = buffer[i];
 				
 				if (a == b) {
 					String[] parts = c.split(":");
 					c="";
 					
 					u.addCourse(new Course(parts[1], parts[0]));
 				}
 				else {
					c = c + (char)b;
 				}
 
 			}
 			inputStream.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 	public void sendMessage(View view){
 		Intent intent = new Intent(this, DisplaySearchResultsActivity.class);
 		EditText editText = (EditText) findViewById(R.id.edit_message);
 		String message = editText.getText().toString();
 		intent.putExtra(EXTRA_MESSAGE, message);
 		
 		startActivity(intent);
 		
 	}
 	public void openUserProfile(View view) {
 		Intent intent = new Intent(this, DisplayUserProfileActivity.class);
 		startActivity(intent);
 	}
 	public void deleteFile(View view) {
		// write to file
 		deleteFile(DisplayUserProfileActivity.FILENAME);
 		
 	}
 	
 }
