 package com.dbstar.DbstarDVB.VideoPlayer.alert;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 import com.dbstar.DbstarDVB.R;
 
 import android.app.Dialog;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TextView;
 
 public class NormalState extends ViewState {
 	private static final String TAG = "NormalState";
 
	private static final int TIMEOUT_IN_MILLIONSECONDS = 30000;
	private static final int TIMEOUT_IN_SECONDS = 30;
 	private static final int UpdatePeriodInMills = 1000;
 	private static final int UpdatePeriodInSeconds = 1;
 
 	TextView mMovieTitle;
 	TextView mMovieDescription;
 	TextView mMovieDirector;
 	TextView mMovieActors;
 	TextView mMovieType;
 	TextView mMovieRegion;
 
 	TextView mTimeoutView;
 	int mTimeoutInMills = TIMEOUT_IN_MILLIONSECONDS;
 	int mTimeoutInSeconds = TIMEOUT_IN_SECONDS;
 
 	Button mCloseButton, mReplayButton, mAddFavouriteButton, mDeleteButton;
 
 	MediaData mMediaData;
 
 	static Handler mUIHandler = new Handler();
 	Timer mTimer = new Timer();
 	TimeoutTask mTask = null;
 	UpdateTask mUpdateTask = new UpdateTask();
 
 	class TimeoutTask extends TimerTask {
 		public void run() {
 			Log.d(TAG, " --===========================  timeout mUIHandler = "
 					+ mUIHandler);
 			mUIHandler.post(mUpdateTask);
 		}
 	}
 
 	class UpdateTask implements Runnable {
 
 		@Override
 		public void run() {
 			Log.d(TAG, " ++++++++++ timeout = " + mTimeoutInMills + " s="
 					+ mTimeoutInSeconds);
 			if (mTimeoutInMills == 0) {
 				closePopupView();
 				return;
 			}
 
 			mTimeoutInMills -= UpdatePeriodInMills;
 			mTimeoutInSeconds -= UpdatePeriodInSeconds;
 			updateTimeoutView();
 		}
 
 	}
 
 	void resetTimer() {
 		Log.d(TAG, "+++reset timer++");
 		if (mTask != null) {
 			mTask.cancel();
 		}
 
 		mTimeoutInMills = TIMEOUT_IN_MILLIONSECONDS;
 		mTimeoutInSeconds = TIMEOUT_IN_SECONDS;
 
 		updateTimeoutView();
 
 		mTask = new TimeoutTask();
 		mTimer.schedule(mTask, UpdatePeriodInMills, UpdatePeriodInMills);
 	}
 
 	void stopTimer() {
 		mTask.cancel();
 		mTimer.cancel();
 	}
 
 	void updateTimeoutView() {
 		String timeout = String.valueOf(mTimeoutInSeconds);
 		mTimeoutView.setText(timeout);
 	}
 
 	public NormalState(Dialog dlg, ViewStateManager manager) {
 		super(dlg, manager);
 	}
 
 	public void enter(Object args) {
 		mMediaData = (MediaData) args;
 
 		mDialog.setContentView(R.layout.movie_info_view);
 
 		initializeView(mDialog);
 		mActionHandler = new ActionHandler(mDialog.getContext(), mMediaData);
 
 	}
 
 	protected void start() {
 		updateView(mDialog);
 		resetTimer();
 	}
 
 	protected void stop() {
 		stopTimer();
 	}
 
 	public void exit() {
 		stopTimer();
 	}
 
 	protected void keyEvent(int KeyCode, KeyEvent event) {
 		if (mDialog != null && mDialog.isShowing()) {
 			resetTimer();
 		}
 	}
 
 	public void initializeView(Dialog dlg) {
 
 		mTimeoutView = (TextView) dlg.findViewById(R.id.timeout_view);
 
 		mMovieTitle = (TextView) dlg.findViewById(R.id.title_view);
 		mMovieDescription = (TextView) dlg.findViewById(R.id.description_view);
 		mMovieDirector = (TextView) dlg.findViewById(R.id.director_view);
 		mMovieActors = (TextView) dlg.findViewById(R.id.actor_view);
 		mMovieType = (TextView) dlg.findViewById(R.id.type_view);
 
 		mCloseButton = (Button) dlg.findViewById(R.id.close_button);
 		mReplayButton = (Button) dlg.findViewById(R.id.replay_button);
 		mAddFavouriteButton = (Button) dlg
 				.findViewById(R.id.add_favourite_button);
 		mDeleteButton = (Button) dlg.findViewById(R.id.delete_button);
 
 		mCloseButton.setOnClickListener(mClickListener);
 		mReplayButton.setOnClickListener(mClickListener);
 		mAddFavouriteButton.setOnClickListener(mClickListener);
 		mDeleteButton.setOnClickListener(mClickListener);
 
 		mCloseButton.requestFocus();
 	}
 
 	View.OnClickListener mClickListener = new View.OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 			Log.d(TAG, "button clicked");
 
 			buttonClicked((Button) v);
 		}
 	};
 
 	private void buttonClicked(Button button) {
 		Log.d(TAG, "buttonClicked clicked " + button);
 
 		if (button == mCloseButton) {
 			closedButtonClicked();
 		} else if (button == mReplayButton) {
 			replayButtonClicked();
 			closePopupView();
 		} else if (button == mAddFavouriteButton) {
 			addFavoriteClicked();
 		} else if (button == mDeleteButton) {
 			deleteButtonClicked();
 		} else {
 
 		}
 	}
 
 	void closePopupView() {
 		Log.d(TAG, "+++++++++ close popus dialog");
 		mDialog.dismiss();
 	}
 
 	void closedButtonClicked() {
 		Log.d(TAG, "closedButtonClicked");
 
 		closePopupView();
 	}
 
 	void replayButtonClicked() {
 		Log.d(TAG, "replayButtonClicked");
 
 		mActionHandler.sendCommnd(ActionHandler.COMMAND_REPLAY);
 	}
 
 	void addFavoriteClicked() {
 		Log.d(TAG, "addFavoriteClicked");
 
 		mActionHandler.sendCommnd(ActionHandler.COMMAND_ADDTOFAVOURITE);
 
 		ViewState state = new FavouriteState(mDialog, mManager);
 		mManager.changeToState(state, null);
 	}
 
 	void deleteButtonClicked() {
 		Log.d(TAG, "deleteButtonClicked");
 
 		ViewState state = new DeleteState(mDialog, mManager);
 		mManager.changeToState(state, mMediaData);
 	}
 
 	public void updateView(Dialog dlg) {
 		if (mMediaData != null) {
 			if (mMediaData.Title != null) {
 				mMovieTitle.setText(mMediaData.Title);
 			}
 
 			if (mMediaData.Description != null) {
 				mMovieDescription.setText(mMediaData.Description);
 			}
 
 			String director = dlg.getContext().getResources()
 					.getString(R.string.property_director);
 			if (mMediaData.Director != null) {
 				director += ": " + mMediaData.Director;
 			}
 			mMovieDirector.setText(director);
 
 			String actors = dlg.getContext().getResources()
 					.getString(R.string.property_actors);
 			if (mMediaData.Actors != null) {
 				actors += ": " + mMediaData.Actors;
 			}
 			mMovieActors.setText(actors);
 		}
 	}
 }
