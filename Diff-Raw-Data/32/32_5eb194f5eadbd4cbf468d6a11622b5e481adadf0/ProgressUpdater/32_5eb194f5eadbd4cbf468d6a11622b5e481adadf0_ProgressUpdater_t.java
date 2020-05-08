 package com.sandwich;
 
 import android.app.Activity;
 import android.widget.ProgressBar;
 
 import com.sandwich.client.IndexDownloadListener;
 
 public class ProgressUpdater implements IndexDownloadListener, Runnable {
 	private ProgressBar progressBar;
 	private Activity activity;
 	
 	private int max;
 	private int current;
 	private boolean resetInProgress;
 	
 	public ProgressUpdater(Activity activity, ProgressBar progressBar) {
 		this.progressBar = progressBar;
 		this.activity = activity;
 		this.resetInProgress = false;
 	}
 	
 	public synchronized void reset() {
 		max = 0;
 		current = 0;
 		resetInProgress = true;
 		activity.runOnUiThread(this);
 	}
 	
 	public synchronized void updateMax(int max) {
 		if (max == 0) {
 			this.max = 1;
 			this.current = 1;
 		} else {
 			this.max = max;
 		}
 		activity.runOnUiThread(this);
 	}
 	
 	@Override
 	public synchronized void indexDownloadComplete(String peer) {
 		this.current++;
 		activity.runOnUiThread(this);
 	}
 
 	@Override
 	public void run() {
 		if (resetInProgress) {
 			progressBar.setProgress(0);
 			resetInProgress = false;
		} else {
			progressBar.setSecondaryProgress((current / max) * progressBar.getMax());
 		}
 	}
 
 }
