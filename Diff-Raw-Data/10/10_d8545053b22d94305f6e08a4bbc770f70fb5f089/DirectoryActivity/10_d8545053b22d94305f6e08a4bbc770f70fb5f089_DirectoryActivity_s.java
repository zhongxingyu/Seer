 package edu.purdue.cs252.lab06;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 import android.app.AlertDialog;
 import android.app.ListActivity;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 
 public class DirectoryActivity extends ListActivity
 {
 	String serverAddress;
 	String username;
 	
 	Handler UIhandler;
 	
 	private DirectoryClient dc;
 	
 	private ArrayAdapter<String> database;
 	private ArrayList<String> usernames;
 	private ArrayList<User> users;
 	
 	private ProgressDialog ringingDialog = null;
 	
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.directory);
         
         users = new ArrayList<User>();
         usernames = new ArrayList<String>();
         database = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, usernames);
         
         setListAdapter(database);
         
         final ListView lv1 = getListView();
         lv1.setTextFilterEnabled(true);
         lv1.setOnItemClickListener(new OnItemClickListener() {
 			public void onItemClick(AdapterView<?> a, View v, int position, long id)
 			{
 				User target = null;
 				for (User u : users)
 				{
 					if (u.getUsername().equals(lv1.getItemAtPosition(position)))
 					{
 						target = u;						
 					}
 				}
 				
 				if (target == null) return;
 				
 				final String username = target.getUsername();
 				final String destinationIP = target.getIPAddress();
 				
 				AlertDialog.Builder adb = new AlertDialog.Builder(DirectoryActivity.this);
 				
 				adb.setTitle("Send Call");
 				adb.setMessage("Are you sure you want to call " + target.getUsername() + " at " + target.getIPAddress() + "?");
 				adb.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int which)
 					{
 						try
 						{
 							dc.sendCall(destinationIP);
 							
 							ringingDialog = new ProgressDialog(DirectoryActivity.this);
 							ringingDialog.setTitle("Ringing");
 							ringingDialog.setMessage("Waiting for " + username + " to respond.");
 							ringingDialog.show();
 						}
 						catch (IOException e)
 						{
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						}
 			        }
 				});
 				adb.setNegativeButton("No", new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int which)
 					{
						dc.hangUp(destinationIP);
 			        }
 				});
 				adb.show();
 			}
         }); 
         
         connectToServer();
     }
     
     public void connectToServer()
     {
     	Log.i("Checkpoint", "Entered connectToServer()...");
     	Bundle extras = getIntent().getExtras();
     	serverAddress = extras.getString("serverAddress");
     	username = extras.getString("username");
         
         try
         {
         	setupHandler();
         	
             dc = new DirectoryClient(serverAddress, UIhandler);
               
             dc.connect();
             dc.addUser(username);
         }
         catch (Exception ex)
         {
         	AlertDialog.Builder adb = new AlertDialog.Builder(DirectoryActivity.this);
 			   
 			adb.setTitle("Error");
 			adb.setMessage(ex.getMessage());
 			adb.setPositiveButton("OK", null);
 			adb.show();
         }
         
         finishActivity(0);
     }
     
     public void setupHandler()
     {
     	UIhandler = new Handler() {    		
     		public void handleMessage(Message msg)
     		{
     			if (msg.what == 0)
     			{
     				updateDirectory(((ArrayList<User>)msg.obj));
     			}
     			else if (msg.what == 1)
     			{
     				displayIncomingCall(((String[])msg.obj)[0], ((String[])msg.obj)[1]);
     			}
     			else if (msg.what == 2)
     			{
     				displayHangup();
     			}
     			else if (msg.what == 3)
     			{
     				displayBusy();
     			}
     			else if (msg.what == 4)
     			{
     				acceptCall((String)msg.obj);
     			}
     		}
     	};
     }
     
     public void updateDirectory(ArrayList<User> directory)
     {
 		users.clear();    	
     	usernames.clear();
     	
 		for (User u : directory)
 		{
 			users.add(u);
 			usernames.add(u.getUsername());
 		}
 		
 		database.notifyDataSetChanged();
     }
     
     //private String senderIP = null;
     public void displayIncomingCall(String username, String ipAddress)
     {
     	final String senderIP = ipAddress;
     	Log.i("Checkpoint", "Entered displayIncomingCall()...");
     	AlertDialog.Builder adb = new AlertDialog.Builder(DirectoryActivity.this);
 
 		adb.setTitle("Incoming Call");
 		adb.setMessage("You have an incoming call from " + username + " at " + ipAddress + ".");
 		adb.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int which)
 			{
 				// TODO: implement this action
 				Log.i("Value Validation", "Sender IP: " + senderIP);
 					
 				try {
 					dc.acceptCall(senderIP);
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 				}
 	        }
 		});
 		adb.setNegativeButton("Decline", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int which)
 			{
 				// TODO: implement this action
 				Log.i("Value Validation", "Sender IP: " + senderIP);
 				
 				try {
 					dc.hangUp(senderIP);
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 				}
 	        }
 		});
 		adb.show();
     }
     
     public void displayHangup()
     {
     	if (ringingDialog == null) return;
     	
     	ringingDialog.dismiss();
     	
     	AlertDialog.Builder adb = new AlertDialog.Builder(DirectoryActivity.this);
 		   
 		adb.setTitle("Busy");
 		adb.setMessage("The user that you tried to call is busy and declined to answer.");
 		adb.setPositiveButton("OK", null);
 		adb.show();
     }
     
     public void acceptCall(String ipAddress)
     {
     	if (ringingDialog == null) return;
     	
     	ringingDialog.dismiss();
     }
     
     public void displayBusy()
     {
 		if (ringingDialog == null) return;
     	
     	ringingDialog.dismiss();
     	
     	AlertDialog.Builder adb = new AlertDialog.Builder(DirectoryActivity.this);
 		   
 		adb.setTitle("Busy");
 		adb.setMessage("The user that you tried to call is busy and declined to answer.");
 		adb.setPositiveButton("OK", null);
 		adb.show();
     }
 }
