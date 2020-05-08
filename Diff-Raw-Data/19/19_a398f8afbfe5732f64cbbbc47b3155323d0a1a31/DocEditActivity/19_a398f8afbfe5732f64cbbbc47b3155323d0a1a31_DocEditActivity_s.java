 package com.example.collabtext;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.os.Bundle;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.content.DialogInterface;
 import android.content.Intent;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import edu.umich.imlc.android.common.Utils;
 import edu.umich.imlc.collabrify.client.CollabrifyAdapter;
 import edu.umich.imlc.collabrify.client.CollabrifyClient;
 import edu.umich.imlc.collabrify.client.CollabrifyListener;
 import edu.umich.imlc.collabrify.client.CollabrifySession;
 import edu.umich.imlc.collabrify.client.exceptions.CollabrifyException;
 import com.example.collabtext.CollabTextProto.globalMove;
 import com.google.protobuf.InvalidProtocolBufferException;
 
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 
 
 public class DocEditActivity extends Activity {
 	
 	User notepad;
 	private static final Level LOGGING_LEVEL = Level.ALL;
 	private static String TAG = "collabText";
 	private CollabrifyClient myClient;
 	private CheckBox withBaseFile;
 	private CollabrifyListener collabrifyListener;
 	private long sessionId;
 	private String sessionName;
 	private ByteArrayInputStream baseFileBuffer;
 	private ByteArrayOutputStream baseFileReceiveBuffer;
 	private ArrayList<String> tags = new ArrayList<String>();
 	
 	@Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_doc_edit);
         EditText ref = (EditText) findViewById(R.id.editText1);
         notepad = new User(ref);
         
         // enable logging
 	    Logger.getLogger("edu.umich.imlc.collabrify.client").setLevel(LOGGING_LEVEL);
 
 	    boolean getLatestEvent = false; 
 	    //true == only get latest update (used for models where whole file is sent at once.
 	    //false allows it to grab all updates.
 
 	    // Instantiate client object
 	    try
 	    {
 	      myClient = new CollabrifyClient(this, "user email", "user display name",
 	          "441fall2013@umich.edu", "XY3721425NoScOpE", getLatestEvent,
 	          collabrifyListener);
 	    }
 	    catch( CollabrifyException e )
 	    {
 	      e.printStackTrace();
 	    }
 	    
 	    
 	    collabrifyListener = new CollabrifyAdapter(){
 		    @Override
 		    public void onReceiveSessionList(final List<CollabrifySession> sessionList)
 		    {
 		      if( sessionList.isEmpty() )
 		      {
 		        Log.i(TAG, "No session available");
 		        return;
 		      }
 		      List<String> sessionNames = new ArrayList<String>();
		      int watcher = 0;
 		      int clock = 0;
 		      for( CollabrifySession s : sessionList )
 		      {
 		        if(s.name() == "C0llabT3xt2319")
 		        	watcher = clock;
 		        clock++;
 		      }
 		      
               try
               {
                 sessionId = sessionList.get(watcher).id();
                 sessionName = sessionList.get(watcher).name();
                 myClient.joinSession(sessionId, null);
               }
               catch( CollabrifyException e ){Log.e(TAG, "error", e);}
 		    }
 		    
 		    @Override
 		      public void onDisconnect()
 		      {
 		        Log.i(TAG, "disconnected");
 		        runOnUiThread(new Runnable()
 		        {
 
 		          @Override
 		          public void run()
 		          {
 		        	  //currently does nothing on disconnect.
 		          }
 		        });
 		      }
 		    
 		    @Override
 		      public void onReceiveEvent(final long orderId, int subId, String eventType, final byte[] data)
 		      {
 		        Utils.printMethodName(TAG);
 		        Log.d(TAG, "RECEIVED SUB ID:" + subId);
 		        runOnUiThread(new Runnable()
 		        {
 		          @Override
 		          public void run()
 		          {
 		            Utils.printMethodName(TAG);
 		            String message1 = new String(data);
 
 		            globalMove message;
 					try {
 						message = globalMove.parseFrom(data);
 						//textViewer.setText(message.getText()); 
 						//need to edit this to send user the data.
 						//should write to the text box when text is sent over the connection.
 					} catch (InvalidProtocolBufferException e) {
 						// TODO Auto-generated catch block
 						Log.e(TAG, "error", e);
 					}
 
 		          }
 		        });
 		      }
 		    
 		    @Override
 		      public void onSessionCreated(long id)
 		      {
 		        Log.i(TAG, "Session created, id: " + id);
 		        sessionId = id;
 		        runOnUiThread(new Runnable()
 		        {
 	
 		          @Override
 		          public void run()
 		          {
 		        	  //createButton.setText(sessionName); 
 		        	  //unnecessary, but should display sessionname somewhere
 		          }
 		        });
 		      }
 		    
 		    @Override
 		      public void onError(CollabrifyException e){Log.e(TAG, "error", e);}
 		    
 		    @Override
 		      public void onSessionJoined(long maxOrderId, long baseFileSize)
 		      {
 		        Log.i(TAG, "Session Joined");
 		        if( baseFileSize > 0 )
 		        {
 		          //initialize buffer to receive base file
 		          baseFileReceiveBuffer = new ByteArrayOutputStream((int) baseFileSize);
 		        }
 		        runOnUiThread(new Runnable()
 		        {
 
 		          @Override
 		          public void run()
 		          {
 		        	  //createButton.setText(sessionName);
 		        	  //same as created
 		          }
 		        });
 		      }
 		    
 		    @Override
 		      public byte[] onBaseFileChunkRequested(long currentBaseFileSize)
 		      {
 		        // read up to max chunk size at a time
 		        byte[] temp = new byte[CollabrifyClient.MAX_BASE_FILE_CHUNK_SIZE];
 		        int read = 0;
 		        try
 		        {
 		          read = baseFileBuffer.read(temp);
 		        }
 		        catch( IOException e )
 		        {
 		          // TODO Auto-generated catch block
 		          e.printStackTrace();
 		        }
 		        if( read == -1 )
 		        {
 		          return null;
 		        }
 		        if( read < CollabrifyClient.MAX_BASE_FILE_CHUNK_SIZE )
 		        {
 		          // Trim garbage data
 		          ByteArrayOutputStream bos = new ByteArrayOutputStream();
 		          bos.write(temp, 0, read);
 		          temp = bos.toByteArray();
 		        }
 		        return temp;
 		      }
 		    
 		    @Override
 		      public void onBaseFileChunkReceived(byte[] baseFileChunk)
 		      {
 		        try
 		        {
 		          if( baseFileChunk != null )
 		          {
 		            baseFileReceiveBuffer.write(baseFileChunk);
 		          }
 		          else
 		          {
 		            runOnUiThread(new Runnable()
 		            {
 		              @Override
 		              public void run()
 		              {
 		            	  //textViewer.setText(baseFileReceiveBuffer.toString());
 		              }
 		            });
 		            baseFileReceiveBuffer.close();
 		          }
 		        }
 		        catch( IOException e )
 		        {
 		          // TODO Auto-generated catch block
 		          e.printStackTrace();
 		        }
 		      }
 		    
 		    @Override
 		      public void onBaseFileUploadComplete(long baseFileSize)
 		      {
 		        runOnUiThread(new Runnable()
 		        {
 
 		          @Override
 		          public void run()
 		          {
 		        	  //textViewer.setText(sessionName);
 		          }
 		        });
 		        try
 		        {
 		          baseFileBuffer.close();
 		        }
 		        catch( IOException e )
 		        {
 		          // TODO Auto-generated catch block
 		          e.printStackTrace();
 		        }
 		      }
 	    };
 	    
 	    tags.add("default");
     }
 	
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.doc_edit, menu);
         return true;
     }
     
     public void undo(MenuItem menu){
     	notepad.undo();
     }
     
     public void redo(MenuItem menu){
     	//notepad.redo();
     }
     
     public void backToMain(MenuItem menu){
     	Intent i = new Intent(this, MainActivity.class);
 		startActivity(i);
     }
     
     public void broadcastMessage(Boolean delete, int location, String text){
     
     	if( myClient != null && myClient.inSession() ) //if in session
         {
           try
           {
         	globalMove message = 
         			globalMove.newBuilder()
         			.setDelete(false)
         			.setLocation(0)
         			.setLength(text.length())
         			.setText(text)
         			.build();
 
             myClient.broadcast(message.toByteArray(), "lol");
           }
           catch( CollabrifyException e ){Log.e(TAG, "error", e);}
         }
     	
     }
     
     public void createSession(){
    	try
         {
           sessionName = "C0llabT3xt2319"; //name hardcoded for now

           if( false)//withBaseFile.isChecked() ) //NOTE: must have a checkbox/option for this, currently unused.
           {
             // initialize basefile data for this example we will use the session
             // name as the data
             baseFileBuffer = new ByteArrayInputStream(sessionName.getBytes());
 
             myClient.createSessionWithBase(sessionName, tags, null, 0);
           }
           else
           {
             myClient.createSession(sessionName, tags, null, 0);
           }
           Log.i(TAG, "Session name is " + sessionName);
         }
         catch( CollabrifyException e ){Log.e(TAG, "error", e);}
     }
     
     public void joinSession(){ 
     	try
         {
           myClient.requestSessionList(tags); //grabs list of available sessions & opens C0llabT3xt2319
         }
         catch( Exception e ){Log.e(TAG, "error", e);}
     }
     
     public void leaveSession(){
     	try
         {
           if( myClient.inSession() )
             myClient.leaveSession(false); //if true, deletes session when owner leaves
         }
         catch( CollabrifyException e ){Log.e(TAG, "error", e);}
     }
     
     
     
 }
 
