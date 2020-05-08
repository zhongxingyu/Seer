 package com.pandj.wewrite;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Stack;
 import com.google.protobuf.InvalidProtocolBufferException;
 import com.pandj.wewrite.javaProtoOutput.protoData;
 
 import edu.umich.imlc.collabrify.client.CollabrifyListener;
 import edu.umich.imlc.collabrify.client.CollabrifyParticipant;
 import edu.umich.imlc.collabrify.client.CollabrifySession;
 import edu.umich.imlc.collabrify.client.exceptions.CollabrifyException;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.SharedPreferences;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.util.SparseArray;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Toast;
 
 class EditTextSelection extends EditText
 {
   private int cursorLocation = 0;
   public EditTextSelection(Context context, AttributeSet attrs,
           int defStyle) {
       super(context, attrs, defStyle);
 
   }
 
   public EditTextSelection(Context context, AttributeSet attrs) 
   {
       super(context, attrs);
   }
 
   public EditTextSelection(Context context) 
   {
       super(context);
   }
   public int getcursorLocation()
   {
     return cursorLocation;
   }
   @Override   
   protected void onSelectionChanged(int selStart, int selEnd) { 
      cursorLocation = selEnd;
   } 
 }
 
 public class TextEditor extends Activity implements OnClickListener, CollabrifyListener
 {
 
   private EditTextSelection textBox;
   private customListener textBoxListener;
   private Button undo, redo, disconnect;
   
   private Stack<panCakeLocal> localUndoStack;
   private Stack<panCakeLocal> localRedoStack;
   private Stack<panCake> remoteStack;
   
   private String localText;
   private int cursorLocation;
   
   private boolean createNewSession;
   
   private String userName;
   private String email;
   private ColabrifyClientObject clientListener;
   private String sessionName; 
   private long sessionId;
   private long startingOrderId;
   
   private boolean joinedSession = false;
 
   private SparseArray<panCakeLocal> eventMap;
   private HashMap<String,Integer> cursorMap;
   
   
   //CollabrifyListener Functions
   @Override
   public void onSessionCreated(long id)
   {
 	if(createNewSession)
 	{
 	    sessionId = id;
 	    Log.i("CCO", "Session created.");
 	    startingOrderId = 0;
 	    enableTextEdit();
 	}
 	else
 	{
 		Log.i("CCO ", "On Session Created called without createNewSession");
 	}
   }
   
   @Override
   public void onSessionJoined(long maxOrderId, long baseFileSize)
   {
     Log.i("CCO", "SessionJoinedCalled");
     if(baseFileSize != 0)
     {
     	Log.i("CCO", "Joined session with basefile!");
     	finish();
     }
     joinedSession = true;
     startingOrderId = maxOrderId;
     
     enableTextEdit();
   }
   
   private void enableTextEdit()
   {
 	    runOnUiThread(new Runnable()
 	    {
 	    	
 	    	@Override
 	    	public void run()
 	    	{
 	    	    textBox.setEnabled(true);
 	    	    textBox.setFocusable(true);
 	    	    textBox.requestFocus();
 	    	    textBox.setVisibility(View.VISIBLE);
 	    	    enableButton(disconnect);
 	    	}
 	    });
   }
   
   @Override
   public void onReceiveSessionList(final List<CollabrifySession> sessionList)
   {
     if( sessionList.isEmpty())
     {
     	Log.i("CCO", "No Session Available using Tags: " + clientListener.tags.get(0));
     	runOnUiThread(new Runnable()
     	{
     		@Override
     		public void run()
     		{
     	    	Toast.makeText(getBaseContext(), "No possible Sessions to Join", Toast.LENGTH_SHORT).show();
     	        finish();
     		}
     	});
     	return;
     }
     List<String> sessionNames = new ArrayList<String>();
     for(CollabrifySession s : sessionList)
     {
     	sessionNames.add(s.name());
     }
     final AlertDialog.Builder builder = new AlertDialog.Builder(this);
     builder.setTitle("Choose a session").setItems(
     		sessionNames.toArray(new String[sessionList.size()]), 
     		new DialogInterface.OnClickListener() {
 				
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					try
 					{
 						sessionId = sessionList.get(which).id();
 						sessionName = sessionList.get(which).name();
 						clientListener.myClient.joinSession(sessionId, null);
 					}
 					catch( CollabrifyException e)
 					{
 						Log.i("CCO", "Join Session Failed", e);
 						finish();
 					}
 				}
 			});
     runOnUiThread(new Runnable()
     {
 		@Override
 		public void run() {
 				try{
 				  builder.show();
 				}
 				catch(Exception e)
 				{
 					e.printStackTrace();
 				}
 		}
     });
   }
   @Override
   public void onReceiveEvent(long orderId, int submissionRegistrationId,
       String eventType, byte[] data)
   {
 	  Log.i("CCO", "Event recieved");
 	  if(submissionRegistrationId == -1)//Originated from a different host
 	  {
 		  panCakeRemote event = new panCakeRemote(data, orderId);
 		  event.state.globalOrderId = orderId;
 		  if(cursorMap.keySet().contains(eventType))
 		  {
 			  cursorMap.remove(eventType);
 			  cursorMap.put(eventType, event.state.cursorLocationAfter);
 		  }
 		  else
 		  {
 			  cursorMap.put(eventType, event.state.cursorLocationAfter);
 		  }
 		  //Stupid easy implementation first.
 		  if(orderId > startingOrderId)
 		  {
 			  runOnUiThread(event);
 		  }
 		  else
 		  {
 			  Log.i("CCO", "Event was behind the times");
 		  }
 	  }
 	  else//Originated from this client
 	  {
 /*		  panCakeLocal temp = eventMap.get(submissionRegistrationId);
 		  temp.state.valid = true;
 		  temp.state.globalOrderId = orderId;*/
 	  }
 	  if(orderId > startingOrderId)//TODO: Potential wrap around issue!
 	  {
 		  startingOrderId = orderId;
 	  }
   }
 
   @Override
   public void onDisconnect()
   {
     Log.i("CCO", "Disconnect Triggered in Listener");
 
   }
   
   @Override 
   public void onError(CollabrifyException e)
   {
 	  //Potential to cause problems if we own the session and get error
 	  //Other users experience would be unknown.
 	  e.printStackTrace();
 	  finish();
   }
   
   private class customListener implements TextWatcher 
   {
 
     @Override
     public void onTextChanged(CharSequence s, int start, int before, int count) 
     {
 
       panCakeLocal toTheStack = new panCakeLocal();
       StateInfo insert = new StateInfo();
       insert.textBefore = localText;
       insert.cursorLocationBefore = textBox.getcursorLocation();
       
       cursorLocation = start + count;
       localText = s.toString();
       
       insert.textAfter = localText;
       insert.cursorLocationAfter = cursorLocation;
       insert.valid = false;;//For right now
       insert.populateDifference();
       toTheStack.InsertLocalData(insert, 0);
       toTheStack.broadCast();
       localUndoStack.push(toTheStack);
       enableButton(undo);
     }
     
 
     @Override
     public void beforeTextChanged(CharSequence s, int start, int count, int after) 
     {
 
     }
 
     @Override
     public void afterTextChanged(Editable s) 
     {
 
     }
  
   }
 
   @Override
   protected void onCreate(Bundle savedInstanceState)
   {
     super.onCreate(savedInstanceState);
     setContentView(R.layout.activity_text_editor);
 
     SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
     email = preferences.getString("email","NOTSET");
     userName = preferences.getString("username","NOTSET");
     
     Bundle extras = getIntent().getExtras();
     createNewSession = extras.getBoolean("Create");
     
 	clientListener = new ColabrifyClientObject(getBaseContext(), createNewSession, email, userName, this);
 	clientListener.enterSession();
       
 
     textBox = (EditTextSelection) findViewById(R.id.editText1);
     undo = (Button) findViewById(R.id.undo);
     redo = (Button) findViewById(R.id.redo);
     disconnect = (Button) findViewById(R.id.disconnect);
     
     undo.setOnClickListener(this);
     redo.setOnClickListener(this);
     disconnect.setOnClickListener(this);
     
     textBoxListener = new customListener();
     textBox.addTextChangedListener(textBoxListener);
     
     localUndoStack = new Stack<panCakeLocal>();
     localRedoStack = new Stack<panCakeLocal>();
     
     cursorMap = new HashMap<String,Integer>();
     eventMap = new SparseArray<panCakeLocal>();
 
     localText = "";
     cursorLocation = 0;	
     if(createNewSession)
     {
       panCakeLocal edgeCase = new panCakeLocal();
       StateInfo s = new StateInfo();
       s.textAfter = localText;
       s.valid = true;
       s.textBefore = "";
       s.cursorLocationAfter = 0;
       s.cursorLocationBefore = 0;
       s.globalOrderId = 0;
       s.differText = "";
       edgeCase.InsertLocalData(s, 0);//Solve this with polymorhpism
       localUndoStack.push(edgeCase);
     }
         
     disableButton(undo);
     disableButton(redo);
     disableButton(disconnect);
     textBox.setVisibility(View.GONE);
   }
   
   @Override
   protected void onResume()
   {
     super.onResume();
     sessionName = clientListener.sessionName;
     this.setTitle(sessionName);
   }
   
   private class StateInfo
   {
 		public String textAfter;
 		public String textBefore;
 		public String differText; 
 		public int cursorLocationAfter;
 		public int cursorLocationBefore;
 		public long globalOrderId;
 		public boolean valid;
 	    public void populateDifference() 
 	    {
 	    	differText = "Not Used";
 	     /* if(textAfter.length() > textBefore.length())//Insertion
 	      {
 	        differText = textAfter.substring(cursorLocationBefore, cursorLocationAfter);
 	      }
 	      else
 	      {
 	        differText = textBefore.substring(cursorLocationAfter, cursorLocationBefore);
 	      }*/
 	    }
   }
   
   public class panCake 
   {
 	protected protoData data;
     protected StateInfo state;
     protected int subId;
     
   }
   
   private class panCakeLocal extends panCake implements Runnable 
   {
 	    public void InsertLocalData(StateInfo insert, long startingOrderId)
 	    {
 	    	//Don't need startingOrderId
 	  	    state = insert;
 	    	data = protoData.newBuilder()
 	    			.setTextAfter(insert.textAfter)
 	    			.setTextBefore(insert.textBefore)
 	    			.setValid(insert.valid)
 	    			.setGlobalOrderId(insert.globalOrderId)
 	    			.setCursorLocationBefore(insert.cursorLocationBefore)
 	    			.setCursorLocationAfter(insert.cursorLocationAfter)
 	    			.setDifferText(insert.differText)
 	    			.build();
 	    }
 	    
 	    public void updateLocal()//UNDO REDO Functionality
 	    {
 	    	  Log.i("TEXT BOX", "updateLocal Called!");
 		      textBox.removeTextChangedListener(textBoxListener);
 		      textBox.setText(this.state.textAfter);
 		      localText = this.state.textAfter;
 		      if( this.state.cursorLocationAfter < localText.length())
 		      {
 		    	  textBox.setSelection(this.state.cursorLocationAfter);
 		      }
 		      else 
 		      {
 		    	  textBox.setSelection(localText.length());
 		      }
 		      textBox.addTextChangedListener(textBoxListener);
 		      this.run();
 	    }
 	    
 	    public void broadCast()
 	    {
 	    	this.run();
 	    }
 	    
 	    @Override
 	    public void run()
 	    {
 	      //Send it over the wire!
 	    	byte[] message = data.toByteArray();
 	    	try 
 	    	{//Race Condition possible TODO: Think about undoing something immediatly. 
 				subId = clientListener.myClient.broadcast(message, userName);
 				//eventMap.put(subId, this);
 			} catch (CollabrifyException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 	    }
   }
   
   public class panCakeRemote extends panCake implements Runnable 
   {
     public panCakeRemote(byte[] input, long globalOrder) 
     {
     	state = new StateInfo();
     	try 
     	{	
     		data = protoData.parseFrom(input);
     		if(data.hasCursorLocationAfter())
     		{
     			state.cursorLocationAfter = data.getCursorLocationAfter();
     		}
     		if(data.hasCursorLocationBefore())
     		{
     			state.cursorLocationBefore = data.getCursorLocationBefore();
     		}
     		if(data.hasDifferText())
     		{
     			state.differText = data.getDifferText();
     		}
     		state.globalOrderId = globalOrder;
     		if(data.hasTextAfter())
     		{
     			state.textAfter = data.getTextAfter();
     		}
     		if(data.hasTextBefore())
     		{
     			state.textBefore = data.getTextBefore();
     		}
     		if(data.hasValid())
     		{
     			state.valid = data.getValid();
     		}
 		} catch (InvalidProtocolBufferException e) 
 		{
 			e.printStackTrace();
 		}
 	    if(joinedSession)
 	    {//allows no 	   
 	      localText = "";
 	      cursorLocation = 0;	
 	      panCakeLocal edgeCase = new panCakeLocal();
 	      edgeCase.InsertLocalData(state, startingOrderId);
 	      localUndoStack.push(edgeCase);
 	      joinedSession = false;
 	   }
 	}
     
 	@Override
 	public void run() 
 	{
 	      textBox.removeTextChangedListener(textBoxListener);
 	      textBox.setText(this.state.textAfter);
 	      //textBox.setSelection(this.state.cursorLocationAfter);
 	      textBox.addTextChangedListener(textBoxListener);
 	}
 }
   
   private void disableButton(Button b)
   {
     b.setClickable(false);
     b.setEnabled(false);
     b.setVisibility(View.GONE);
   }
   
   private void enableButton(Button b)
   {
     b.setClickable(true);
     b.setEnabled(true);
     b.setVisibility(View.VISIBLE);
   }
   
   @Override
   public void onBackPressed()
   {
     //Do nothing, avoiding the hairy situations that could arise
   }
 
   @Override
   public boolean onCreateOptionsMenu(Menu menu)
   {
     // Inflate the menu; this adds items to the action bar if it is present.
     getMenuInflater().inflate(R.menu.text_editor, menu);
     return true;
   }
   @Override
   protected void onDestroy()
   {
 	  super.onDestroy();    	
 	  clientListener.destroy();
   }
   @Override
   public void onClick(View v)
   {
     switch(v.getId())
     {
       case(R.id.undo) :
         if(!localUndoStack.isEmpty())
         {
           panCakeLocal obj = localUndoStack.pop();
           //Might need to change validity later
           localRedoStack.push(obj);
           enableButton(redo);
           if(localUndoStack.isEmpty())
           {
             disableButton(undo);
           }
           obj.updateLocal();
         }
         break;
       case(R.id.redo) :
         if(!localRedoStack.isEmpty())
         {
           panCakeLocal obj = localRedoStack.pop();
           localUndoStack.push(obj);
           enableButton(undo);
           if(localRedoStack.isEmpty())
           {
             disableButton(redo);
           }
           obj.updateLocal();
         }
         break;
       case(R.id.disconnect) :
       	this.finish();
         break;
     }
   }
 	@Override
 	public byte[] onBaseFileChunkRequested(long currentBaseFileSize) 
 	{
 		// TODO Auto-generated method stub
 		return null;
 	}
 	@Override
 	public void onBaseFileUploadComplete(long baseFileSize) 
 	{
 		// TODO Auto-generated method stub
 		
 	}
 	@Override
 	public void onBaseFileChunkReceived(byte[] baseFileChunk) 
 	{
 		// TODO Auto-generated method stub
 		
 	}
 	@Override
 	public void onParticipantJoined(CollabrifyParticipant p) 
 	{
 		Toast.makeText(getBaseContext(), p.getDisplayName() + " has Joined!", Toast.LENGTH_LONG).show();
 	}
 	@Override
 	public void onParticipantLeft(CollabrifyParticipant p) 
 	{
 		Toast.makeText(getBaseContext(), p.getDisplayName() + " has Left!", Toast.LENGTH_LONG).show();
 	}
 	@Override
 	public void onSessionEnd(long id) 
 	{
		this.finish();
 	}
 	  
 
 }
