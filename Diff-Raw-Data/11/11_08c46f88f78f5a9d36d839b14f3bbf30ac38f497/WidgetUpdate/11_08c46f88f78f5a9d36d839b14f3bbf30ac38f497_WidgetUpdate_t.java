 package database;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.ObjectOutput;
 import java.io.ObjectOutputStream;
 import java.io.Serializable;
 import java.util.Timer;
 import java.util.TimerTask;
 
 
 import rinor.Rest_com;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.content.SharedPreferences;
 import android.os.AsyncTask;
 import android.os.Handler;
 import android.util.Log;
 
 public class WidgetUpdate implements Serializable {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	private SharedPreferences sharedparams;
 	private static WidgetUpdate instance;
 	
 	private boolean activated;
 	private DomodroidDB domodb;
 	private Handler sbanim;
 	private String mytag="WidgetUpdate";
 	
 	/*
 	 * This class is a background engine 
 	 * 		On instantiation, it connects to Rinor server, and submit queries 
 	 * 		each 'update' timer, to update local database values for all known devices
 	 * When variable 'activated' is set to false, the thread is kept alive, 
 	 *     but each timer is ignored (no more requests to server...)
 	 * When variable 'activated' is true, each timer generates a database update with server's response
 	 */
 	public WidgetUpdate(Activity context, Handler anim, SharedPreferences params){
 		this.sharedparams=params;
 		activated = true;
 		domodb = new DomodroidDB(context);	
 		domodb.owner=mytag;
 		sbanim = anim;
 		Log.d(mytag,"Initial start requested....");
 		Timer();
 	}
 	
 
 	public void Timer() {
 		final Timer timer = new Timer();
 		
 		TimerTask doAsynchronousTask;
 		final Handler handler = new Handler();
 		doAsynchronousTask = new TimerTask() {
 		
 		@Override
 		public void run() {
 			Runnable myTH = new Runnable() {
 				
 					public void run() {
 						if(activated) {
 							try {
 								//Log.d(mytag,"timer expires : update Database !");
 								new UpdateThread().execute();
 							} catch (Exception e) {
 								e.printStackTrace();
 							}
 						
 						}
 					} //End of run method
 				};	// End of runnable bloc
 				
 				
 				try {
 					handler.post(myTH);		//To avoid exception on ICS
 					} catch (Exception e) {
 						e.printStackTrace();
 					}
 			}
 		};
		doAsynchronousTask.run();	//Force a 1st execution immediate (request #1636 )
		// and arm the timer to do automatically this each 'update' seconds
 		if(timer != null)
 			timer.schedule(doAsynchronousTask, 0, sharedparams.getInt("UPDATE_TIMER", 300)*1000);
 	}
 	 
 	
 	public void stopThread(){
 		Log.d(mytag,"stopThread requested....");
 		activated = false;
 	}
 	public void restartThread(){
 		Log.d(mytag,"restartThread requested....");
 		activated = true;
 	}
 	public void cancelEngine(){
 		Log.d(mytag,"cancelEngine requested....");
 		activated = false;
 		try {
 			Timer();	//That should cancel running timer
 			finalize();
 		} catch (Throwable e) {
 			
 		}
 	}
 	public class UpdateThread extends AsyncTask<Void, Integer, Void>{
 
 		@Override
 		protected Void doInBackground(Void... params) {
 			// Added by Doume to correctly release resources when exiting
 			if(! activated) {
 				Log.d(mytag,"UpdateThread frozen....");
 				
 			} else {
 				Log.d(mytag,"UpdateThread Getting widget infos from server...");
 				if(sharedparams.getString("UPDATE_URL", null) != null){
 					try {
 						sbanim.sendEmptyMessage(0);
 						JSONObject json_widget_state = Rest_com.connect(sharedparams.getString("UPDATE_URL", null));
 						//Log.d(mytag,"UPDATE_URL = "+ sharedparams.getString("UPDATE_URL", null).toString());
 						//Log.d(mytag,"result : "+ json_widget_state);
 						sbanim.sendEmptyMessage(1);
 						domodb.insertFeatureState(json_widget_state);
 						sbanim.sendEmptyMessage(2);
 					} catch (Exception e) {
 						sbanim.sendEmptyMessage(3);
 						e.printStackTrace();
 					}
 				}
 			}
 			return null;
 		}
 	}
 }
 
