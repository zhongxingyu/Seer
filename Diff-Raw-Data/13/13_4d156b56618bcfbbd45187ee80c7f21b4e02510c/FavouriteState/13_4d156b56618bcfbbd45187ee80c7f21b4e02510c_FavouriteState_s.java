 package com.dbstar.DbstarDVB.VideoPlayer.alert;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.app.Dialog;
 import android.os.Handler;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TextView;
 
 import com.dbstar.DbstarDVB.R;
 
 public class FavouriteState extends ViewState {
 
	private static final int TIMEOUT_IN_MILLIONSECONDS = 3000;
	private static final int TIMEOUT_IN_SECONDS = 3;
 	private static final int UpdatePeriodInMills = 1000;
 	private static final int UpdatePeriodInSeconds = 1;
 
 	int mTimeoutInMills = TIMEOUT_IN_MILLIONSECONDS;
 	int mTimeoutInSeconds = TIMEOUT_IN_SECONDS;
 
 	TextView mTimeoutView;
 	Button mCloseButton;
 
 	static Handler mUIHandler = new Handler();
 
 	Timer mTimer = new Timer();
 	TimeoutTask mTask = null;
 	UpdateTask mUpdateTask = new UpdateTask();
 
 	class TimeoutTask extends TimerTask {
 		public void run() {
 			timeout();
 		}
 	}
 
 	class UpdateTask implements Runnable {
 
 		@Override
 		public void run() {
 			if (mTimeoutInMills == 0) {
 				closePopupView();
 				return;
 			}
 
 			mTimeoutInMills -= UpdatePeriodInMills;
 			mTimeoutInSeconds -= UpdatePeriodInSeconds;
 			updateTimeoutView();
 		}
 
 	}
 
 	void timeout() {
 		mUIHandler.post(mUpdateTask);
 	}
 
 	void updateTimeoutView() {
 		String timeout = String.valueOf(mTimeoutInSeconds);
 		mTimeoutView.setText(timeout);
 	}
 
 	void resetTimer() {
 		Log.d("FavoriteState", "+++reset timer++");
 		
 		if (mTask != null)
 			mTask.cancel();
 
 		mTask = new TimeoutTask();
 
 		mTimeoutInMills = TIMEOUT_IN_MILLIONSECONDS;
 		mTimeoutInSeconds = TIMEOUT_IN_SECONDS;
 
 		updateTimeoutView();
 
 		mTimer.schedule(mTask, UpdatePeriodInMills, UpdatePeriodInMills);
 	}
 
 	void stopTimer() {
 		mTask.cancel();
 		mTimer.cancel();
 	}
 
 	public FavouriteState(Dialog dlg, ViewStateManager manager) {
 		super(dlg, manager);
 	}
 
 	public void enter(Object args) {
 		mDialog.setContentView(R.layout.favorite_confirm_view);
 		initializeView(mDialog);
 	}
 
 	public void start() {
 		resetTimer();
 	}
 
 	public void stop() {
 		stopTimer();
 	}
 
 	public void exit() {
 		stopTimer();
 	}
 	
 	protected void keyEvent(int KeyCode, KeyEvent event) {
 		resetTimer();
 	}
 
 	void initializeView(Dialog dlg) {
 		mTimeoutView = (TextView) dlg.findViewById(R.id.timeout_view);
 		mCloseButton = (Button) dlg.findViewById(R.id.ok_button);
 		mCloseButton.setOnClickListener(mClickListener);
 		mCloseButton.requestFocus();
 	}
 
 	View.OnClickListener mClickListener = new View.OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 			closedButtonClicked();
 		}
 	};
 
 	void closedButtonClicked() {
 		stop();
 		exit();
 		closePopupView();
 	}
 
 	void closePopupView() {
 		mDialog.dismiss();
 	}
 
 }
