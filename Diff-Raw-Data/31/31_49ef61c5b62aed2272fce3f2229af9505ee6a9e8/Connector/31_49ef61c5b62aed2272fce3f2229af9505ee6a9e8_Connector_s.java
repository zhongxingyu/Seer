 package stkl.spectropolarisclient;
 
 import java.io.IOException;
 
 import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
 import android.os.AsyncTask;
 import android.widget.Toast;
 
 public class Connector extends AsyncTask<Client, Void, Exception>{
 	private JoinActivity joinActivity;
 	private ProgressDialog dialog;
 	
 	public Connector(JoinActivity joinActivity) {
 		this.joinActivity = joinActivity;
 	}
 	
 	@Override
 	/**
 	 * Create a ProgressDialog.
 	 */
 	protected void onPreExecute() {
 		dialog = ProgressDialog.show(joinActivity, "Connecting", "Setting up the connection, please wait", true);
 		super.onPreExecute();
 	}
 
 
 	/**
 	 * Sets up the connection in the Client. If an exception is thrown during this process, that exception is returned, else null.
 	 */
 	@Override
 	protected Exception doInBackground(Client... params) {
 		try {
 			params[0].connect();
 		} catch (IOException e) {
 			e.printStackTrace();
 			
 			return e;
 		}
 		
 		return null;
 	}
 	
 	/**
 	 * If an exception was thrown, notify the user. Else start the game.
 	 */
 	@Override
 	protected void onPostExecute(Exception result) {
 		dialog.dismiss();
 		if(result != null) {
 			// Show toaster to tell user connection could not be setup.
 			CharSequence text = "Connection failed!\n" + result.toString();
     		int duration = Toast.LENGTH_LONG;
 
     		Toast toast = Toast.makeText(joinActivity, text, duration);
     		toast.show();
 		} else {
 			joinActivity.startGame();
 		}
 		super.onPostExecute(result);
 	}
 
 }
