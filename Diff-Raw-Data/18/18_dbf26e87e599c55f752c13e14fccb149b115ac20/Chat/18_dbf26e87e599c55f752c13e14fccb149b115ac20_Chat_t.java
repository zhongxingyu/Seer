 package com.patchingzone.energyvampire;
 
 import java.io.IOException;
 import com.clwillingham.socket.io.IOMessage;
 import com.clwillingham.socket.io.IOSocket;
 import com.clwillingham.socket.io.IOWebSocket;
 import com.clwillingham.socket.io.MessageCallback;
 
 import android.app.Activity;
 import android.content.Context;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 public class Chat extends MainActivity {
 
 	private Button btnSend;
 	private Context c;
 	private EditText etChat;
	private String address = "";
 	
 	public String[] testString = {"message\" : \"test nr 2"};
 
 	public static IOSocket ioWebSocket;
 	MessageCallback callback;
 	IOMessage msg;
 	IOWebSocket sok;
 
 	int xR;
 	int yR;
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		// TODO Auto-generated method stub
 
 	}
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_chat);
 		c = this;
 		this.btnSend = (Button) findViewById(R.id.btnSend);
 		this.etChat = (EditText) findViewById(R.id.etChat);
 		
		
		if(app_preferences.getString("IP", "") != "" && app_preferences.getString("Port", "") != "")
		{
			address = "ws://"+ app_preferences.getString("IP", "") +":"+ app_preferences.getString("Port", "");
		}
		else
		{
			Log.e("Missing settings", "MISSING ADDRESS AND PORT");
		}
		
		Log.e("addres", address);
		
 		this.btnSend.setOnClickListener(new OnClickListener() {
 		
 			@Override
 			public void onClick(View v) {
 				LinearLayout ll = (LinearLayout) findViewById(R.id.ll);
 				TextView tv = new TextView(c);
 				tv.setWidth(ll.getWidth());
 				tv.setTextColor(Color.parseColor("#FFFFFF"));
 				ll.addView(tv);
 				tv.setText(etChat.getText().toString());
 				etChat.setText("");
 			}
 		});
 
 		msg = new IOMessage(IOMessage.EVENT, -1, "", "");
 
 		callback = new MessageCallback() {
 
 			@Override
 			public void onOpen() {
 			}
 
 			@Override
 			public void onMessage(String message) {
 			}
 
 			@Override
 			public void on(String event, String data) {
 			}
 
 			@Override
 			public void onEvent(String message) {
 					JsonParser.Parse(message);
 					JsonParser.SendMessage("Hello Thijs.");
 					JsonParser.SendJsonMessage("Test",testString);
 			}
 
 		};
 		new Thread() {
 			@Override
 			public void run() {
 				try {
 					ioWebSocket = new IOSocket(address, callback);
 					ioWebSocket.connect();
 
 				} catch (IOException e) {
 					Log.e("exeption", "" + e);
 				}
 			}
 		}.start();
 	}
 
 }
