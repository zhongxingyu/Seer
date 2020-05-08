 package info.liuqy.adc.aroundme;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.ScrollView;
 import android.widget.TextView;
 
 public class ChatActivity extends Activity {
 	public static final String EXTRA_ID = "id";
 	public static final String EXTRA_ATTRIBUTES = "attributes";
 	public static final String EXTRA_MESSAGE = "message";
 
 	public static final String PREF_ATTRIBUTES = "attributes";
 	public static final String PREF_CHATLOG = "chatlog";
 	
 	TextView nicknameText;
 	TextView timeleftText;
 	ScrollView scroller;
 	TextView chats;
 	EditText msg;
 	int type;
 	String toId;
 	String nickname;
 	String attributes;
 	String message;
 	SharedPreferences prefs;
     SharedPreferences.Editor editor;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.chat);
 		
 		nicknameText = (TextView) this.findViewById(R.id.nickname);
 		timeleftText = (TextView) this.findViewById(R.id.timeleft);
 		scroller = (ScrollView) this.findViewById(R.id.scroller);
 		chats = (TextView) this.findViewById(R.id.chats);
 		msg = (EditText) this.findViewById(R.id.msg);
 		
 		prefs = this.getSharedPreferences("chatlog", 0);
 	    editor = prefs.edit();	
 	}
 
 	//because of FLAG_ACTIVITY_REORDER_TO_FRONT
 	@Override
 	protected void onNewIntent(Intent intent) {
 	    super.onNewIntent(intent);
 	    setIntent(intent);
 	}
 	
 	@Override
 	protected void onPause() {
 		super.onPause();
 		
 		String chatlog = chats.getText().toString();
 		editor.putString(toId + PREF_CHATLOG, chatlog);
 		editor.commit();
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();

		// handle the incoming intent
		Intent i = getIntent();
		Bundle data = i.getExtras();
		toId = data.getString(EXTRA_ID);
		attributes = data.getString(EXTRA_ATTRIBUTES); //possible null
		message = data.getString(EXTRA_MESSAGE); //possible null

 		String savedAttributes = prefs.getString(toId + PREF_ATTRIBUTES, null);
 		if (attributes != null && !attributes.equals(savedAttributes)) {
 			editor.putString(toId + PREF_ATTRIBUTES, attributes);
 			editor.commit();
 		} else
 			attributes = savedAttributes;
 
 		//if toId is an anonymous, the attributes will be null
 		if (attributes == null) {
 			nickname = toId;  //use id as nickname
 			nicknameText.setText(nickname);
 			timeleftText.setText("unknown");
 		} else {
 			String[] parts = attributes.split(",");
 			nickname = parts[0];
 			String until = parts[1];
 			long now = System.currentTimeMillis();
 			long diff = Long.parseLong(until) - now;
 			long diffMin = diff / 1000 / 60 / 60;
 			nicknameText.setText(nickname);
 			timeleftText.setText(diffMin + " min left");
 		}
 
 		String chatlog = prefs.getString(toId + PREF_CHATLOG, "");
 		if (message != null)
 			chatlog += nickname + ": " + message + "\n";
 		chats.setText(chatlog);
 		scroller.post(new Runnable() { 
             public void run() { 
                 scroller.smoothScrollTo(0, chats.getBottom());
             } 
         });
 	}
 
 	public void send(View v) {
 		String content = msg.getText().toString();
 		
 		String msg = "m " + AroundMeActivity.myId + " " + toId + " " + content;
 		Intent i = new Intent(ChatAgent.SEND_ACTION);
 		i.putExtra(ChatAgent.EXTRA_MESSAGE, msg);
 		ChatActivity.this.sendBroadcast(i);
 		
 		chats.setText(chats.getText().toString() + AroundMeActivity.myNickname + ": " + content + "\n");
 		scroller.post(new Runnable() { 
             public void run() { 
                 scroller.smoothScrollTo(0, chats.getBottom());
             } 
         });		
 
 		this.msg.setText(""); //clear
 	}
 }
