 package com.futilities.mindtimer;
 
 import java.io.File;
 import java.util.HashMap;
 
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.SystemClock;
import android.text.TextUtils.TruncateAt;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.ImageButton;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 import com.futilities.mindtimer.HourGlass.TimerState;
 
 public class MindTimerListItemView extends LinearLayout implements
 		OnClickListener {
 
 	private static final String TAG = "MINDTIMERLISTVIEWITEM";
 	private Context mContext;
 	private TextView mLabelView;
 	private ImageButton mTimerControlButton;
 	private long mTimerId;
 	private TextView mDurationLabelView;
 	private ImageButton mTimerIconView;
 	private long mDeadline;
 	private TimerState mTimerState;
 	private TextView mTimeRemainingView;
 	private long mDeltaSeconds;
 	private int mSecondsDuration;
 	private View mProgressBar;
 	private int mStartingProgressBarWidth;
 
 	public MindTimerListItemView(Context context) {
 		super(context);
 
 		mContext = context;
 		LayoutInflater inflater = (LayoutInflater) mContext
 				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 
 		inflater.inflate(R.layout.timers_row, this, true);
 
 		mLabelView = (TextView) findViewById(R.id.TimerLabel);
		mLabelView.setEllipsize(TruncateAt.MIDDLE);
 		mDurationLabelView = (TextView) findViewById(R.id.Duration);
 		mTimeRemainingView = (TextView) findViewById(R.id.TimeRemaining);
 		mTimerIconView = (ImageButton) findViewById(R.id.TimerImageButton);
 		mTimerControlButton = (ImageButton) findViewById(R.id.ToggleTimerOnOff);
 		mProgressBar = findViewById(R.id.progress_bar);
 		mTimerState = TimerState.NOT_STARTED;
 
 	}
 
 	public TimerState getTimerState() {
 		return mTimerState;
 	}
 
 	public void setTimerState(TimerState timerState) {
		// This code is broken because setTimerState doesn't exist on MindTimerList
		// Trying to figure out if it went missing from TagTimerActivity
		//MindTimerList list = (MindTimerList) mContext;
		//list.setTimerState(mTimerId, timerState);
 		mTimerState = timerState;
 	}
 
 	public void setTimerId(long id) {
 		mTimerId = id;
 	}
 
 	public long getTimerId() {
 		return mTimerId;
 	}
 
 	public void setLabel(String label) {
 		mLabelView.setText(label);
 	}
 
 	public void setDurationLabel(String durationLabel) {
 		mDurationLabelView.setText(durationLabel);
 	}
 
 	public void setThumbnail(String thumbnailFilePath) {
 		File file = new File(thumbnailFilePath);
 
 		if (file.exists()) {
 			Bitmap bm = BitmapFactory.decodeFile(thumbnailFilePath);
 			mTimerIconView.setImageBitmap(bm);
 		} else {
 			mTimerIconView.setImageDrawable(getResources().getDrawable(
 					R.drawable.button_down));
 		}
 	}
 
 	public void setDeadline(long deadline) {
 		mDeadline = deadline;
 	}
 
 	public void setSecondsDuration(int secondsDuration) {
 		mSecondsDuration = secondsDuration;
 
 	}
 
 	@Override
 	public void onClick(View v) {
 		if (mTimerIconView == v) {
 			Log.i(TAG, "timer icon clicked");
 			Intent i = new Intent(mContext, TimerEdit.class);
 			i.putExtra(TimersDbAdapter.KEY_ROWID, mTimerId);
 			((MindTimerList) mContext).startActivityForResult(i,
 					MindTimerList.ACTIVITY_EDIT);
 
 		} else if (mTimerControlButton == v) {
 			Log.i(TAG, "play button clicked " + mTimerId);
 
 			MindTimerList list = (MindTimerList) mContext;
 
 			MindTimerCursorAdapter adapter = (MindTimerCursorAdapter) list
 					.getListAdapter();
 			
 			HashMap<Long, HourGlass> glasses = adapter.getRunningTimers();
 			TimerState newState = glasses.get(mTimerId).transitionTimerState();
 			
 			setTimerState(newState);
 
 			list.requery();
 		}
 
 	}
 
 	public void setEditClickListener() {
 		mTimerIconView.setOnClickListener(this);
 	}
 
 	// Listener is MindTimerListActivity
 	public void setTimerControlClickListener() {
 		mTimerControlButton.setOnClickListener(this);
 	}
 
 	public void updateProgress() {
 		// If the app is stopped and resumed, the deadline in the db may be in
 		// the past
 		long delta = Math.max(mDeadline - SystemClock.elapsedRealtime(), 0);
 		mDeltaSeconds = delta / 1000;
 
 		ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) mTimeRemainingView
 				.getLayoutParams();
 
 		switch (mTimerState) {
 		case RUNNING:
 			if (mSecondsDuration <= 0) {
 				return;
 			}
 
 			mTimerControlButton
 					.setImageResource(R.drawable.slowpoke_stop_button);
 
 			mTimeRemainingView.setText(HourGlass.getDurationString(delta));
 
 			mStartingProgressBarWidth = Math.max(
 					mProgressBar.getMeasuredWidth(), mStartingProgressBarWidth);
 
 			// Use the left margin of the time remaining text to affect the
 			// width of the progress bar
 			int newProgressBarWidth = (int) ((mDeltaSeconds * mStartingProgressBarWidth) / mSecondsDuration);
 
 			mlp.setMargins(
 					(int) (mStartingProgressBarWidth - newProgressBarWidth),
 					mlp.topMargin, mlp.rightMargin, mlp.bottomMargin);
 
 			if (delta <= 0) {
 				setTimerState(HourGlass.TimerState.FINISHED);
 
 			}
 
 			break;
 		case NOT_STARTED:
 			mTimeRemainingView.setText(HourGlass.getDurationString(0));
 			mTimerControlButton
 					.setImageResource(R.drawable.slowpoke_play_button);
 
 			mlp.setMargins(0, mlp.topMargin, mlp.rightMargin, mlp.bottomMargin);
 			break;
 
 		case FINISHED:
 			mTimerControlButton
 					.setImageResource(R.drawable.slowpoke_pause_button);
 			break;
 		}
 
 	}
 
 }
