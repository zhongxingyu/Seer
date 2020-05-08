 package com.lghs.stutor;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintStream;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.UnknownHostException;
 
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.Toast;
 
 public class session extends Activity {
 
 	EditText chatTextBox= (EditText) findViewById (R.id.session_edittext_message);
 	EditText chatLog = (EditText) findViewById (R.id.session_edittext_chatlog);
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_session);
 		try {
 			server();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		/*
 		 * voids for the following actions are below.
 		 * Mark as Busy (tutor)
 		 * Start Server (tutor)
 		 * Start client (Local) (tutor)
 		 * Then client  connects (tutee)
 		 * Commence with blah blah blah
 		 */
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 
 	public void onBackPressed()
 	{
 		Toast msg = Toast.makeText(this, "GO BACK!", 3);
 		msg.show();
 	}
 
 	public void markasbusy(){
 		//Only occurs on tutor side.
 	}
 	public void server() throws IOException{
 		final ServerSocket serverSocket = new ServerSocket(15219); // << port can be adjusted
 		class serverloop extends AsyncTask <Void,Void,Void>
 		{
 			String messg;
 			String studentName;
 			String sessionlog;
 			Socket socket1;
 			BufferedReader readFromSocket;
 			PrintStream writeToSocket;
 			protected Void doInBackground(Void...args0)
 			{
 
 				while (!isCancelled())
 				{
 					try{
 						socket1 = serverSocket.accept();
 						readFromSocket = new BufferedReader(new InputStreamReader(socket1.getInputStream()));
 						writeToSocket = new PrintStream(socket1.getOutputStream());
 					}catch (IOException e) {
 						e.printStackTrace();
 					}finally{}
 
 					while(!isCancelled() && socket1.isConnected()) // this only runs AFTER the socket is accepted
 					{
 						try{
 							messg = readFromSocket.readLine();
 							writeToSocket.println("\n"+ messg);
 							sessionlog += ("\n"+ messg); 
 						} catch (IOException e) {
 							e.printStackTrace();
 						}
 						finally{}
 					}
 				}
 				return null;
 			}
 			void onPostExecute() throws IOException
 			{
 				socket1.close();
 			}
 		}
 	}
 	public void client() throws UnknownHostException, IOException{
 		Socket ClientSocket = new Socket("10.153.129.40", 15219);
		//TODO the above IP needs to be the android server ip
		
		final BufferedReader FromServer = new BufferedReader(new InputStreamReader(ClientSocket.getInputStream()));
		
		class backgroundClient extends AsyncTask <Void,Void,Void>
 		{
 			protected Void doInBackground(Void... arg0) 
			{ 
				try
				{
 				chatLog.append(FromServer.readLine());
				} catch (IOException e) {
					e.printStackTrace();
				}
 				return null;
 			}
 		
		}
 	}   
 	
	public void session_button_sendchat_click (View V) throws UnknownHostException, IOException{
		Socket ClientSocket = new Socket("10.153.129.40", 15219); //TODO make this IP the android server ip
		PrintStream toServer = new PrintStream(ClientSocket.getOutputStream());
		toServer.println(chatTextBox.getText());
 		
 	}
 }
