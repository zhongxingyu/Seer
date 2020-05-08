 package com.example.wecharades.presenter;
 
 import java.util.Observable;
 import java.util.Observer;
 
 import android.content.Context;
 import android.content.Intent;
 import android.net.ConnectivityManager;
 import android.widget.Toast;
 
 import com.example.wecharades.model.DCMessage;
 import com.example.wecharades.model.DataController;
 import com.example.wecharades.views.GenericActivity;
 import com.example.wecharades.views.LoginActivity;
 
 public abstract class Presenter implements Observer{
 
 	protected DataController dc;
 	protected GenericActivity activity;
 
 	/**
 	 * Needed in order to use parse commands
 	 * @param context - the context (the activity: use 'this' most often)
 	 */
 	public Presenter(GenericActivity activity) {
 		this.activity = activity;
 		this.dc = DataController.getDataController(activity);
 		dc.addObserver(this);
 	}
 
 	/**
 	 * A method to show a toast
 	 * @param context
 	 * @param msg
 	 */
 	protected void showToast(Context context, String msg) {
 		Toast toast = Toast.makeText(context, msg, Toast.LENGTH_LONG);
 		toast.show();
 	}
 
 	/**
 	 * Go to the login screen
 	 */
 	public void goToLoginActivity() {
 		Intent i = new Intent(activity.getApplicationContext(), LoginActivity.class);
 		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 		activity.startActivity(i);
 		// Close current view, effectively restarting the app
 		activity.finish();
 	}
 
 	/**
 	 * Called whenever an activity is closed.
 	 */
 	public void saveState(){
 		dc.deleteObserver(this);
 		dc.saveState(activity);
 	}
 
 	/**
 	 * Called whenever an update is received from a class this presenter subscribes to.
 	 */
 	public void update(Observable dataController, Object obj){
 		if(dataController.getClass().equals(DataController.class) 
 				&& obj != null
 				&& obj.getClass().equals(DCMessage.class)){
 			DCMessage dcm = (DCMessage) obj;
 			if(dcm.getMessage() == DCMessage.ERROR){
				//activity.showErrorDialog((String) dcm.getData());
 			} else if(dcm.getMessage() == DCMessage.MESSAGE){
 				activity.showToast((String) dcm.getData());
 			}
 		}
 	}
 	
 	/**
 	 * Check if the user has internet connection
 	 * @return true if the user has internet connection, false otherwise
 	 */
 	public boolean isNetworkConnected() {
 		return ((ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo() != null;
 	}
 }
