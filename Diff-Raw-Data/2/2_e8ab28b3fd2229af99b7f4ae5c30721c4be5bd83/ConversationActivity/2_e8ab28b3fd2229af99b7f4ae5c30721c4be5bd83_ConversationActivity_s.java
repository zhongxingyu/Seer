 package org.touchirc.view;
 
 import java.util.LinkedList;
 
 import org.touchirc.R;
 import org.touchirc.irc.IrcBinder;
 import org.touchirc.irc.IrcService;
 import org.touchirc.model.Conversation;
 import org.touchirc.model.Message;
 
 import android.app.ListActivity;
 import android.content.BroadcastReceiver;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.ServiceConnection;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.widget.ArrayAdapter;
 
 public class ConversationActivity extends ListActivity implements ServiceConnection {
 	private IrcBinder ircServiceBind;
 	private BroadcastReceiver MessageReceiver;
 	private Conversation conversation;
 	private LinkedList<Message> values;
 	private ArrayAdapter<Message> adapter;
 
 
 	public void onCreate(Bundle icicle) {
 		super.onCreate(icicle);
 		
		setContentView(R.layout.activity_main);
 		this.values = new LinkedList<Message>();
 
 		Message m1 = new Message("Android", "Bugdroid", 0);
 		Message m2 = new Message("iPhone", "Steve Jobs", 0);
 		Message m3 = new Message("WindowsMobile", "Bill Gates", 0);
 		Message m4 = new Message("Blackberry", "BBy", 0);
 		Message m5 = new Message("WebOS", "WebSO", 0);
 		Message m6 = new Message("Ubuntu", "Unix", 0);
 		Message m7 = new Message("Windows7", "Bill Gates", 0);
 		Message m8 = new Message("Max OS X", "Paul Emploi", 0);
 		Message m9 = new Message("Linux", "Tux", 0);
 		Message m10 = new Message("OS/2" , "Steevy", 0);
 
 		values.add(m1);
 		values.add(m2);
 		values.add(m3);
 		values.add(m4);
 		values.add(m5);
 		values.add(m6);
 		values.add(m7);
 		values.add(m8);
 		values.add(m9);
 		values.add(m10);
 
 		adapter = new ArrayAdapter<Message>(this,	android.R.layout.simple_list_item_1, values);
 		setListAdapter(adapter);
 		
 		/*			/--------- TODO ----------\
 		 * 
 		final EditText editText = (EditText) findViewById(R.id.messageToSend);
 		editText.setOnEditorActionListener(new OnEditorActionListener() {
 
 			@Override
 			public boolean onEditorAction(TextView v, int actionId,
 					KeyEvent event) {
 		        boolean handled = false;
 		        if (actionId == EditorInfo.IME_ACTION_SEND) {
 		            values.add(new Message("You", editText.getText().toString(), 0));
 		            adapter.notifyDataSetChanged();
 		            handled = true;
 		        }
 		        return handled;
 			}
 		});
 		*/
 		
 		values.add(new Message("msg","author"));
 		
 		
 		Intent intent = new Intent(this, IrcService.class);
 		getApplicationContext().startService(intent);
 		getApplicationContext().bindService(intent, this, 0);
 		
 		this.MessageReceiver = new BroadcastReceiver() {
 			@Override
 			public void onReceive(Context context, Intent intent) {
 				System.out.println("[ConversationActivity] Message recu");
 				LinkedList<Message> buffer = conversation.getBuffer();
 				for(Message m : buffer){
 					System.out.println(m.getMessage());
 					values.add(m);
 				}
 				conversation.cleanBuffer();
 				adapter.notifyDataSetChanged();
 				
 			}	
 		};
 		registerReceiver(this.MessageReceiver , new IntentFilter("org.touchirc.irc.newMessage"));
 
 
 	}
 
 	
 	@Override
 	public void onServiceDisconnected(ComponentName name) {
 		// TODO Auto-generated method stub
 		this.ircServiceBind = null;
 		
 	}
 
 
 	@Override
 	public void onServiceConnected(ComponentName name, IBinder service) {
 		// TODO Auto-generated method stub
 		this.ircServiceBind = (IrcBinder) service;
 		this.conversation = this.ircServiceBind.getService().getServerById(0).getConversation("#Boulet");
 
 	}
 
 }
