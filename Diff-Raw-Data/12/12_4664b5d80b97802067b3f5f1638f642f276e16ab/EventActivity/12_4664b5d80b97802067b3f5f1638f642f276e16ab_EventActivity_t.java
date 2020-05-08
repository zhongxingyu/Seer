 package com.jshaw.greeknetwork;
 
 import java.text.DateFormat;
 import java.util.Calendar;
 import java.util.Date;
 
 import android.app.Activity;
 import android.app.PendingIntent;
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.telephony.SmsManager;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class EventActivity extends Activity {
 
 	private TextView name;
 	private TextView date;
 	private TextView details;
 	private Button notify;
 	private Button edit;
 	
 	private Long time;
 	
 	private String id;
 	private GreekHelper helper;
 	private Cursor c;
 	
 	@Override
 	public void onCreate(Bundle save)
 	{
 		super.onCreate(save);
 		setContentView(R.layout.event);
 		
 		id = getIntent().getStringExtra(EventListActivity.ID_EXTRA);
 		
 		helper = new GreekHelper(this);
 		c = helper.getEvent(id);
 		c.moveToFirst();
 		
 		name = (TextView)findViewById(R.id.name);
 		date = (TextView)findViewById(R.id.date);
 		details = (TextView)findViewById(R.id.detials);
 		notify = (Button)findViewById(R.id.notify_all);
 		edit = (Button)findViewById(R.id.edit_event);
 		
 		name.setText(helper.getName(c));
 		date.setText(DateFormat.getInstance().format(new Date(helper.getDate(c))));
 		details.setText(helper.getPos(c));
 		
 		time = helper.getDate(c);
 		
 		notify.setOnClickListener(new OnClickListener()
 		{			
 			@Override
 			public void onClick(View v)
 			{
 				Calendar cal = Calendar.getInstance();
 				String m;	      
 				m = "&e"+name.getText().toString()+":"+String.valueOf(time)+":"+details.getText().toString()+":";
 
 				Cursor members = helper.getMembers();
 				members.moveToFirst();
 				while(!members.isAfterLast())
 				{
 					String number = helper.getNumber(members);
 					
 					PendingIntent pi = PendingIntent.getActivity(EventActivity.this, 0, new Intent(EventActivity.this, MessageReceiver.class), 0); 
 					
 					SmsManager sms = SmsManager.getDefault();
					if(number!=null)
 					{
 						sms.sendTextMessage(number, null, m, pi, null);
 					}
 			        
 			        members.moveToNext();
 				}
             	Toast.makeText(getBaseContext(),"Members Notified", Toast.LENGTH_SHORT).show();
             	finish();
 			}
 		});
 		
 		edit.setOnClickListener(new OnClickListener() 
         {
 			@Override
 			public void onClick(View v) {
 				Intent i = new Intent(EventActivity.this, CreateEventActivity.class);
 				i.putExtra(MemberListActivity.ID_EXTRA, id);
 				startActivity(i);
 			}
         });	
 		
 		c.close();
 		helper.close();
 	}
 }
