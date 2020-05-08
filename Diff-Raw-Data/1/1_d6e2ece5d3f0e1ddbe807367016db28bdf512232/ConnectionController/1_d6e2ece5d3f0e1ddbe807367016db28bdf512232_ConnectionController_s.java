 package net.hermeto.android.main;
 
 import org.jivesoftware.smack.XMPPException;
 import org.jivesoftware.smack.packet.Message;
 
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.res.Resources;
 import android.os.AsyncTask;
 import android.util.Log;
 import android.view.View;
 import android.widget.Toast;
 
 public class ConnectionController {
 	protected Main mHostActivity;
 	protected XMPPClient chatClient;
 	protected String nickname;
 	protected String clientID;
 	protected Status connectionStatus;
 	
 	private final String SERVER_LOGIN="a@lilab.info";
 	private final String SERVER_ADDRESS="lilab.info";
 	private final String CLIENT_LOGIN="b";
 	private final String CLIENT_PASSWORD="123456";
 
 	protected enum Status {
 		DISCONNECTED, WAITING_RESPONSE, CONNECTED
 	}
 
 	public ConnectionController(Main activity) {
 		mHostActivity = activity;
 		this.connectionStatus = Status.DISCONNECTED;
 	}
 
 	protected View findViewById(int id) {
 		return mHostActivity.findViewById(id);
 	}
 
 	protected Resources getResources() {
 		return mHostActivity.getResources();
 	}
 
 	public void upClick() {
 		if (this.connectionStatus == Status.CONNECTED) {
 			try {
 				chatClient.sendMessage(clientID + " up");
 			} catch (XMPPException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}else{
 			this.disconnectedMessage();
 		}
 	}
 
 	public void downClick() {
 		if (this.connectionStatus == Status.CONNECTED) {
 			try {
 				chatClient.sendMessage(clientID + " down");
 			} catch (XMPPException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}else{
 			this.disconnectedMessage();
 		}
 	}
 
 	public void leftClick() {
 		if (this.connectionStatus == Status.CONNECTED) {
 			try {
 				chatClient.sendMessage(clientID + " left");
 			} catch (XMPPException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}else{
 			this.disconnectedMessage();
 		}
 	}
 
 	public void rightClick() {
 		if (this.connectionStatus == Status.CONNECTED) {
 			try {
 				chatClient.sendMessage(clientID + " right");
 			} catch (XMPPException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}else{
 			this.disconnectedMessage();
 		}
 	}
 
 	public void buttonClick() {
 		if (this.connectionStatus == Status.CONNECTED) {
 			try {
 				chatClient.sendMessage(clientID + " button");
 			} catch (XMPPException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}else{
 			this.disconnectedMessage();
 		}
 	}
 
 	private void disconnectedMessage() {
 		AlertDialog.Builder builder = new AlertDialog.Builder(mHostActivity);
 		builder.setMessage("You need to Connect first!");
 		AlertDialog alert = builder.create();		
 		alert.show();		
 	}
 
 	public void connect(String nickname) {
 		if (chatClient != null) {
 			disconnect();
 		}
 		
 		if(nickname.length() > 10){
 			nickname = nickname.substring(0, 10);
 		}
 		this.nickname = nickname;
 		
 		//Connect to server
 		new ConnectionTask().execute();
 
 	}
 	
 	public void disconnect() {
 		if(this.connectionStatus == Status.CONNECTED){
 			try {
 				chatClient.sendMessage(clientID + " disconnect");
 				Log.d("XMPP", "Disconnect Message");
 				Thread.sleep(3000);
 			} catch (XMPPException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		
 		this.nickname = "";
 		this.clientID = "";
 		this.connectionStatus = Status.DISCONNECTED;
 		
 		if (chatClient != null) {
 			chatClient.disconnect();
 			chatClient = null;
 		}
 	}
 
 	public void processMessage(Message message) {
 		Log.d("XMPP", "Message Received");
 		if ((this.connectionStatus == Status.WAITING_RESPONSE)
 				&& ((message.getType() == Message.Type.chat) || (message.getType() == Message.Type.normal)) ) {
 			
 			Log.d("XMPP", "Good Message");
 			
 			if(message.getBody()!=null){
 				String[] sMessage = message.getBody().split(" ");
 				if (sMessage[0].equals("HELLO")
 						&& sMessage[1].equals(this.nickname)) {
 					this.clientID=sMessage[2];
 					this.connectionStatus = Status.CONNECTED;
 					Log.d("XMPP", "Connected");
 				}
 			}else{
 				Log.d("XMPP", "Null Body");
 			}
 		} else {
 			Log.d("XMPP", "Unknown Message("+message.getType()+"): " + message.getBody());
 		}
 	}
 	
 	/**
 	 * Class to run connection tasks in background.
 	 * Connects to XMPP server, sends Hello message, waits for HELLO response.
 	 * @author Thiago A. Lechuga
 	 *
 	 */
 	private class ConnectionTask extends AsyncTask<Void, Void, Void> {
 
 		 private ProgressDialog progressDialog;
 
 		 @Override
 		 protected void onPreExecute() {
 
 		    progressDialog = new ProgressDialog(mHostActivity);
 
 		    progressDialog.setMessage("Connecting...");
 
 		    progressDialog.show();
 
 		 }
 		 
 		@Override
 		protected Void doInBackground(Void... params) {
 			try {
 				chatClient = new XMPPClient(5222, SERVER_LOGIN, SERVER_ADDRESS, CLIENT_LOGIN, CLIENT_PASSWORD);
 				Log.d("XMPP", "Conected");
 
 				connectionStatus = ConnectionController.Status.WAITING_RESPONSE;
 				chatClient.sendMessage("HELLO " + nickname);
 				Log.d("XMPP", "Hello Message Sent: HELLO "+ nickname);
 				
 				long startTime=System.currentTimeMillis();
 				while ( !isCancelled() && connectionStatus==ConnectionController.Status.WAITING_RESPONSE){
 					Message msg=chatClient.checkMessage();
 					
 					if(msg!=null){ //Got a Message
 						processMessage(msg);
 					}else{ //No messages
 						if((System.currentTimeMillis()-startTime) > 20000){
 							cancel(true);
 						}
 					}
 				}
 			} catch (XMPPException e) {
 				cancel(true);
 				e.printStackTrace();
 			}catch (Exception e2){
 				cancel(true);
 				e2.printStackTrace();
 			}
 			
 			return null;
 		}
 		
 		@Override
 	    protected void onPostExecute(Void result) {
 			progressDialog.dismiss();
 			Toast.makeText(mHostActivity, "Connected!", Toast.LENGTH_SHORT).show();
 		}
 		
 		@Override
 	    protected void  onCancelled() {
 			progressDialog.dismiss();
 			Toast.makeText(mHostActivity, "Connection Problem :(", Toast.LENGTH_LONG).show();
 		}
 	}
 }
