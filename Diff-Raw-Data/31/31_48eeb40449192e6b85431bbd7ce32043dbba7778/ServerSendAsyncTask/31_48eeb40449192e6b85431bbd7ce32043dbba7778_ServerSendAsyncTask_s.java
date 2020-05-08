 package com.hmml.api;
 
 import java.io.IOException;
 import java.io.PrintStream;
 import java.net.Socket;
 import java.net.UnknownHostException;
 
 import com.hmml.knightswatch_android.GameActivity;
 
 import android.os.AsyncTask;
 import android.widget.Toast;
 
 public class ServerSendAsyncTask extends AsyncTask<String, Void, String> {
 	
 	private GameActivity gameActivity;
 	private Socket socket;
 	
 	public ServerSendAsyncTask(GameActivity gameActivity, Socket socket){
		super();
 		this.gameActivity = gameActivity;
 		this.socket = socket;
 	}
 	
 	@Override
 	protected synchronized String doInBackground(String... params) {
 		try {
 			// socket should NOT be null, as it's created first in ServerReceiveAsyncTask
 			if(socket == null){
 				socket = new Socket(GameActivity.DEFAULT_HOST, GameActivity.DEFAULT_PORT);
 			}
 		} catch (UnknownHostException e1) {
 			e1.printStackTrace();
 		} catch (IOException e1) {
 			e1.printStackTrace();
 		}
 		
 		// send a move
 		if(params[0] != null && params[1] != null && params[0].equalsIgnoreCase("send")){
 			// move to send defined in params[1]
 			try {
 				PrintStream socketOut = new PrintStream(socket.getOutputStream());
 				socketOut.println(params[1]);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		
 		return params.toString();
 	}
 
 	@Override
 	protected void onPostExecute(String result) {
 		Toast.makeText(gameActivity, "ServerAsyncTask Ending.. " + result, Toast.LENGTH_SHORT).show();
 	}
 
 }
