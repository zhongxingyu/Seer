 package edu.purdue.cs.cs180.safewalk;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.os.Handler;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TextView;
 import edu.purdue.cs.cs180.channel.ChannelException;
 import edu.purdue.cs.cs180.channel.MessageListener;
 import edu.purdue.cs.cs180.channel.TCPChannel;
 
 public class ResponseActivity extends Activity implements MessageListener {
 
 	TCPChannel channel = null;
 	Handler mHandler = null;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_response);
 
 		// the ready button.
 		final Button button = (Button) findViewById(R.id.ready_button);
 		final TextView status = (TextView) findViewById(R.id.status_textview);
 
 		String host = getString(R.string.host_name);
 		int port = Integer.parseInt(getString(R.string.port_number));
 		try {
 			channel = new TCPChannel(host, port);
			channel.setMessageListener(this);
 		} catch (ChannelException e) {
 			status.setText("ERROR: could not create channel to "+host+":"+port);
 		}
 
 		// A handler is needed since the message received is called from a
 		// different Thread, and only the main thread can update the UI.
 		// As a workaround, we create a handler in the main thread that displays
 		// whatever it receives from the message received.
 		mHandler = new Handler() {
 			@Override
 			public void handleMessage(android.os.Message msg) {
 				Message safeWalkMessage = (Message) msg.obj;
 				switch (safeWalkMessage.getType()) {
 				case Searching:
 					status.setText("Searching");
 					break;
 				case Assigned:
 					status.setText("Assigned: "+safeWalkMessage.getInfo());
 					button.setEnabled(true);
 					break;
 				default:
 					System.err.println("Unexpected message type: "+safeWalkMessage.getType());
 					break;
 				}
 			}
 		};
 
 		// The on click event.
 		button.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				button.setEnabled(false);
 				Message msg = new Message(Message.Type.Response,
 							  "Help Team "+channel.getID(),
 							  channel.getID());
 				try {
 					channel.sendMessage(msg.toString());
 				} catch (ChannelException e) {
 					status.setText("ERROR: could not send message");
 				}
 			}
 		});
 	}
 
 	@Override
 	public void messageReceived(String message, int clientID) {
 		// Create a handler message, and send it to the Main Thread.
 		Message safeWalkMessage = new Message(message, clientID);
 		android.os.Message msg = new android.os.Message();
 		msg.obj = safeWalkMessage;
 		mHandler.sendMessage(msg);
 	}
 
 	/**
 	 * Close the application if sent to the background.
 	 */
 	@Override
 	protected void onPause() {
 	    super.onPause();
 	    System.exit(0);
 	}
 }
