 package co.shoutbreak.ui.views;
 
 import java.util.Observable;
 import java.util.Observer;
 
 import android.os.AsyncTask;
 
 import co.shoutbreak.components.SBStateManager;
 import co.shoutbreak.misc.SBLog;
 import co.shoutbreak.ui.SBContext;
 
 public class ComposeView extends SBView implements Observer {
 	
 	private final String TAG = "ComposeView.java";
 	
 	/* Do NOT store any SBContext parameters, will cause service leak */
 	
 	public ComposeView(SBContext context, String name, int resourceId, int notificationId) {
 		super(context, name, resourceId, notificationId);
 		_Context.getStateManager().addObserver(this);
 	}
 	
 	/* LIFECYCLE METHODS */
 	
 	@Override
 	void onShow() {
 		SBLog.i(TAG, "onShow()");
 		// inflate map view
 		// TODO Auto-generated method stub	
 	}
 	
 	@Override
 	void onHide() {
 		SBLog.i(TAG, "onHide()");
 		// remove map view
 		// TODO Auto-generated method stub
 	}
 
 	@Override
 	void onDestroy() {
 		SBLog.i(TAG, "onDestroy()");
 		_Context.getStateManager().deleteObserver(this);		
 	}
 	
 	/* LISTENERS: use AsyncTasks for expensive shit */
 	
 	/* OBSERVER METHODS */
 
 	public void update(Observable observable, Object data) {
 		SBStateManager stateManager = (SBStateManager) observable;
 	}
 	
 	private class PowerStateChngeTask extends AsyncTask<SBContext, Integer, Integer> {
 
 		@Override
 		protected Integer doInBackground(SBContext... params) {
 			return null;
 		}
 		
 	}
 }
