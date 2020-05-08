 package edu.upenn.cis350;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.graphics.Typeface;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 
 public class ShowEvent extends Activity {
 	
 	private EventPOJO event;
 	private String uname;
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.showevent);
         Bundle extras = this.getIntent().getExtras();
         if(extras != null){
         	event = (EventPOJO)extras.get("eventPOJO");
            	uname = extras.getString("user");
 
            	EventPOJO event = (EventPOJO)extras.get("eventPOJO");
         	System.out.println(event.getEventTitle());
         	TextView temp = (TextView)findViewById(R.id.eventTitleText);
         	temp.setText(event.getEventTitle());
         	temp = (TextView)findViewById(R.id.eventDescText);
         	temp.setText(event.getEventDesc());
         	temp = (TextView)findViewById(R.id.eventActionsText);
         	temp.setText(event.getEventActions());
         	temp = (TextView)findViewById(R.id.startDateDisplay2);
         	temp.setText(event.getStart());
         	temp = (TextView)findViewById(R.id.endDateDisplay2);
         	temp.setText(event.getEnd());
         	temp = (TextView)findViewById(R.id.affilsText);
         	/*
         	CharSequence[] temp2 = extras.getCharSequenceArray("affils");
         	boolean[] temp3 = extras.getBooleanArray("affilsChecked");
         	StringBuilder affilText = new StringBuilder();
         	if(temp2 != null && temp3 != null){
         		for(int i = 0; i < temp2.length; i++){
         			if(temp3[i])
         				affilText.append(temp2[i] + "\t");
         		}
         	}
         	*/
         	List<String> affilList = event.getAffils();
         	StringBuilder affilText = new StringBuilder();
         	for(String s : affilList){
         		affilText.append(s + "\t");
         	}
         	temp.setText(affilText.toString());
         	temp = (TextView)findViewById(R.id.systemsText);
         	/*
         	temp2 = extras.getCharSequenceArray("systems");
         	temp3 = extras.getBooleanArray("systemsChecked");
         	StringBuilder systemText = new StringBuilder();
         	if(temp2 != null && temp3 != null){
         		for(int i = 0; i < temp2.length; i++){
         			if(temp3[i])
         				systemText.append(temp2[i] + "\t");
         		}
         	}
         	*/
         	List<String> systemList = event.getSystems();
         	StringBuilder systemText = new StringBuilder();
         	for(String s : systemList){
         		systemText.append(s + "\t");
         	}
         	temp.setText(systemText.toString());
         	temp = (TextView)findViewById(R.id.personText1);
         	temp.setText(event.getContact1());
         	temp = (TextView)findViewById(R.id.personText2);
         	temp.setText(event.getContact2());
         	temp = (TextView)findViewById(R.id.severityText);
         	temp.setBackgroundColor(event.getSeverity());
         	temp = (TextView)findViewById(R.id.typeText);
         	temp.setText(event.getType());
         	
         }
     }
     
     @Override
     public void onBackPressed() {
        Intent i = new Intent(this, Agenda.class);
        if(event != null)
     	   i.putExtra("eventPOJO", event);
        i.putExtra("user", uname);
        startActivity(i);
     }
     
     public void onBackToAgendaClick(View view){
     	Intent i = new Intent(this, Agenda.class);
     	startActivity(i);
     }
     
     public void populateMessages() {
     	List<MessagePOJO> messages = getMessages();
         
         LinearLayout messagesPane = (LinearLayout) findViewById(R.id.messagesPane);
         messagesPane.removeAllViews();
         for (final MessagePOJO m : messages) {
         	LinearLayout messageFrame = new LinearLayout(this);
         	messageFrame.setOrientation(1);
         	messageFrame.setPadding(1, 1, 1, 1);
         	
         	LinearLayout header = new LinearLayout(this);
         	header.setOrientation(0);
         	
         	TextView author = new TextView(this);
         	author.setText(m.getAuthor());
         	author.setTypeface(Typeface.DEFAULT_BOLD);
         	
         	TextView timestamp = new TextView(this);
         	Long time = Long.parseLong(m.getTimestamp());
         	SimpleDateFormat formatter = new SimpleDateFormat();
         	timestamp.setText(" at " + formatter.format(new Date(time)));
         	
         	header.addView(author);
         	header.addView(timestamp);
         	
         	TextView messageText = new TextView(this);
         	messageText.setText(m.getText());
         	
         	messageFrame.addView(header);
         	messageFrame.addView(messageText);
         	messageFrame.setOnClickListener(new LinearLayout.OnClickListener() {  
                 public void onClick(View v){
                     onMessageClick(m);
                 }
              });
         	
         	messagesPane.addView(messageFrame);
         }
     }
     
     public List<MessagePOJO> getMessages() {
     	List<MessagePOJO> messageList = new ArrayList<MessagePOJO>();
     	
     	// First we have to open our DbHelper class by creating a new object of that
     	AndroidOpenDbHelper dbHelper = new AndroidOpenDbHelper(this);
 
     	// Then we need to get a writable SQLite database, because we are going to insert some values
     	// SQLiteDatabase has methods to create, delete, execute SQL commands, and perform other common database management tasks.
     	SQLiteDatabase db = dbHelper.getReadableDatabase();
     	dbHelper.createCommentsTable(db);
     	
     	Cursor cursor = db.query(AndroidOpenDbHelper.TABLE_NAME_MESSAGES, null, null, null, null, null, null);
     	startManagingCursor(cursor);
     	
     	while (cursor.moveToNext()) {
     		String text = cursor.getString(cursor.getColumnIndex(AndroidOpenDbHelper.COLUMN_NAME_MESSAGE_TEXT));
     		String author = cursor.getString(cursor.getColumnIndex(AndroidOpenDbHelper.COLUMN_NAME_MESSAGE_AUTHOR));
     		String timestamp = cursor.getString(cursor.getColumnIndex(AndroidOpenDbHelper.COLUMN_NAME_MESSAGE_TIMESTAMP));
     		
     		MessagePOJO message = new MessagePOJO();
     		message.setText(text);
     		message.setAuthor(author);
     		message.setTimestamp(timestamp);
     		messageList.add(message);
     	}
     	
     	db.close();
     	return messageList;
     }
     
     public void onMessageClick(MessagePOJO message){
     	Intent i = new Intent(this, ShowComments.class);
     	i.putExtra("messagePOJO", message);
     	i.putExtra("user", uname);
     	startActivity(i);
     }
 	
 }
