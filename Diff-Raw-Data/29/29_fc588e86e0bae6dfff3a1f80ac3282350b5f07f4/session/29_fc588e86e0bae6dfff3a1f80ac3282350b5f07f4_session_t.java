 package com.lghs.stutor;
 
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
 import android.widget.Toast;
 
 public class session extends Activity {
 	
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_session);
        	/*
          * voids for the following actions are below.
          * Mark as Busy (tutor)
          * Start Server (tutor)
          * Start client (Local) (tutor)
          * Then client  connects (tutee)
          * Commence with blah blah blah
         */
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }
     
     public void onBackPressed()
 	{
 		Toast msg = Toast.makeText(this, "GO BACK!", 3);
 		msg.show();
 	}
     
     public void markasbusy(){
     	//Only occurs on tutor side.
     }
    public void server() throws IOException{
    	//Only starts on tutor side.
     	//Only starts on tutor side.
    	//this establishes the connection and declares the in and out lines to the student client
    	 ServerSocket serverSocket = new ServerSocket(15219); // << port can be adjusted
    	Socket socket = serverSocket.accept();
		BufferedReader readFromSocket = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		PrintStream writeToSocket = new PrintStream(socket.getOutputStream());
		
		// loop this in background
        
        String messg="";
        String studentName="";
        String sessionlog="";
        //start loop
		messg = readFromSocket.readLine();
    	writeToSocket.println(studentName + ":" + messg);
    	sessionlog += (studentName + ":" + messg); 
		//end loop
     	
     }
     public void client(){
     	//Depending on login type this will connect to localhost or IP from the database.
     	Client client = new Client();
     }
     
 }
