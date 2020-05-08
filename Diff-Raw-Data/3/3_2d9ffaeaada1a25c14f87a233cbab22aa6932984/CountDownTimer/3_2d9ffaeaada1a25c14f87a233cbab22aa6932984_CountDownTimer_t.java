 package net.diogomarques.utils;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 /**
  * A plain old Java implementation of Android's <a href=
  * "http://developer.android.com/reference/android/os/CountDownTimer.html"
  * >CountDownTimer</a>.
  * 
  * @author Diogo Marques <diogohomemmarques@gmail.com>
  * 
  */
 public abstract class CountDownTimer {
 
 	private final long mMillisInFuture;
 	private final long mCountdownInterval;
 	private Timer mTimer;
 
 	public CountDownTimer(long millisInFuture, long countDownInterval) {
 		mMillisInFuture = millisInFuture;
 		mCountdownInterval = countDownInterval;
 	}
 
 	public final void cancel() {
 		if (mTimer != null)
 			mTimer.cancel();
 	}
 
 	public synchronized final CountDownTimer start() {
 		mTimer = new Timer();
 		final long deadline = System.currentTimeMillis() + mMillisInFuture;
 		TimerTask tickerTask = new TimerTask() {
 
 			@Override
 			public void run() {
 				long now = System.currentTimeMillis();
 				if (now + mCountdownInterval > deadline) {
					onFinish();
					cancel();					
 				} else {
 					onTick(deadline - now);
 				}
 
 			}
 		};
 
 		mTimer.scheduleAtFixedRate(tickerTask, mCountdownInterval,
 				mCountdownInterval);
 		return this;
 	}
 
 	public abstract void onTick(long millisUntilFinished);
 
 	public abstract void onFinish();
 
 }
