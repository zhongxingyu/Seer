 package com.example.collabtext;
 
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
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.TextView;
 
 public class MainActivity extends Activity {
 
 	//long int userID //based on surface/tablet's serial#?
 
 	/**
 	   * Logging level for HTTP requests/responses.
 	   * <p>
 	   * To turn on, set to {@link Level#CONFIG} or {@link Level#ALL} and run this
 	   * from command line: {@code adb shell setprop log.tag.HttpTransport DEBUG}.
 	   * </p>
 	   */
 	  private static final Level LOGGING_LEVEL = Level.ALL;
 
 	  private static String TAG = "collabText";
 
 	  private CollabrifyClient myClient;
 	  private TextView textViewer;
 	  private EditText broadcastText;
 	  private Button broadcastButton;
 	  private Button createButton;
 	  private Button joinSessionButton;
 	  private Button leaveSessionButton;
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
 
 
 		//withBaseFile = (CheckBox) findViewById(R.id.withBaseFileCheckBox);
 
 		/*broadcastText = (EditText) findViewById(R.id.sendTxt);
 		textViewer = (EditText) findViewById(R.id.editText1);
 	    broadcastButton = (Button) findViewById(R.id.sendButton);
 	    joinSessionButton = (Button) findViewById(R.id.joinButt);
 	    leaveSessionButton = (Button) findViewById(R.id.leaveButt);
 */
 	    // enable logging
 	    Logger.getLogger("edu.umich.imlc.collabrify.client").setLevel(LOGGING_LEVEL);
 
 	    //pull text from broadcast box and send it to myClient
 	    broadcastButton.setOnClickListener(new OnClickListener()
 	    {
 	      @Override
 	      public void onClick(View v)
 	      {
 	        if( broadcastText.getText().toString().isEmpty() )
 	          return; //break if no text added
 	        if( myClient != null && myClient.inSession() ) //if in session
 	        {
 	          try
 	          {
 	        	globalMove message = 
 	        			globalMove.newBuilder()
 	        			.setDelete(false)
 	        			.setLocation(0)
 	        			.setLength(broadcastText.getText().toString().length())
 	        			.setText(broadcastText.getText().toString())
 	        			.build();
 
 	            myClient.broadcast(message.toByteArray(), "lol");
 	            broadcastText.getText().clear();
 	          }
 	          catch( CollabrifyException e ){Log.e(TAG, "error", e);}
 	        }
 	      }
 	    });
 
 	    createButton.setOnClickListener(new OnClickListener()
 	    {
 
 	      @Override
 	      public void onClick(View v)
 	      {
 	        try
 	        {
 	          Random rand = new Random();
 	          sessionName = "C0llabT3xt " + rand.nextInt(Integer.MAX_VALUE);
 
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
 	    });
 
 	    joinSessionButton.setOnClickListener(new OnClickListener()
 	    {
 
 	      @Override
 	      public void onClick(View v)
 	      {
 	        try
 	        {
 	          myClient.requestSessionList(tags); //shows list of available sessions
 	        }
 	        catch( Exception e ){Log.e(TAG, "error", e);}
 	      }
 	    });
 
 	    leaveSessionButton.setOnClickListener(new OnClickListener()
 	    {
 
 	      @Override
 	      public void onClick(View v)
 	      {
 	        try
 	        {
 	          if( myClient.inSession() )
 	            myClient.leaveSession(false); //if true, deletes session when owner leaves
 	        }
 	        catch( CollabrifyException e ){Log.e(TAG, "error", e);}
 	      }
 	    });
 
 	    collabrifyListener = new CollabrifyAdapter()
 	    {
 
 	      @Override
 	      public void onDisconnect()
 	      {
 	        Log.i(TAG, "disconnected");
 	        runOnUiThread(new Runnable()
 	        {
 
 	          @Override
 	          public void run()
 	          {
 	        	  createButton.setText("CreateSession"); 
 	        	  //just resets text of the create button when you leave. 
 	        	  //this is because the default is to 
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
 					textViewer.setText(message.getText()); 
 					//should write to the text box when text is sent over the connection.
 				} catch (InvalidProtocolBufferException e) {
 					// TODO Auto-generated catch block
 					Log.e(TAG, "error", e);
 				}
 
 	          }
 	        });
 	      }
 
 	      @Override
 	      public void onReceiveSessionList(final List<CollabrifySession> sessionList)
 	      {
 	        if( sessionList.isEmpty() )
 	        {
 	          Log.i(TAG, "No session available");
 	          return;
 	        }
 	        List<String> sessionNames = new ArrayList<String>();
 	        for( CollabrifySession s : sessionList )
 	        {
 	          sessionNames.add(s.name());
 	        }
 	        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
 	        builder.setTitle("Choose Session").setItems(
 	            sessionNames.toArray(new String[sessionList.size()]),
 	            new DialogInterface.OnClickListener()
 	            {
 	              @Override
 	              public void onClick(DialogInterface dialog, int which)
 	              {
 	                try
 	                {
 	                  sessionId = sessionList.get(which).id();
 	                  sessionName = sessionList.get(which).name();
 	                  myClient.joinSession(sessionId, null);
 	                }
 	                catch( CollabrifyException e ){Log.e(TAG, "error", e);}
 	              }
 	            });
 
 	        runOnUiThread(new Runnable()
 	        {
 
 	          @Override
 	          public void run()
 	          {
 	            builder.show();
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
 	        	  createButton.setText(sessionName); 
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
 	        	  createButton.setText(sessionName);
 	          }
 	        });
 	      }
 
 	      /*
 	       * (non-Javadoc)
 	       * 
 	       * @see
 	       * edu.umich.imlc.collabrify.client.CollabrifyAdapter#onBaseFileChunkRequested
 	       * (long)
 	       */
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
 
 	      /*
 	       * (non-Javadoc)
 	       * 
 	       * @see
 	       * edu.umich.imlc.collabrify.client.CollabrifyAdapter#onBaseFileChunkReceived
 	       * (byte[])
 	       */
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
 	            	  textViewer.setText(baseFileReceiveBuffer.toString());
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
 
 	      /*
 	       * (non-Javadoc)
 	       * 
 	       * @see
 	       * edu.umich.imlc.collabrify.client.CollabrifyAdapter#onBaseFileUploadComplete
 	       * (long)
 	       */
 	      @Override
 	      public void onBaseFileUploadComplete(long baseFileSize)
 	      {
 	        runOnUiThread(new Runnable()
 	        {
 
 	          @Override
 	          public void run()
 	          {
 	        	  textViewer.setText(sessionName);
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
 
 
 	    tags.add("default");
 
 	}//end onCreate
 
 	public void sendText(View view){
 		//handle incoming text from txtbox and forward it to the control
 		//this puts the onus of deciding how large of a chunk to send at any one given time on the user.
 		//will have to add a separate button for deleting. That way we dont have to stitch text into the box
 
 		//view.getObjectbyID(sendTxt); 
 		//lineCom.insert(uniqUserID, sendTxt.text, timestamp?);
 
 
 	}
 
 	public void movePointer(View view){
 		//update this user's pointer location, and shift focus automatically to the text box
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		//getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 }
