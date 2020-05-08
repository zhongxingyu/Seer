 package com.masenf.core.progress;
 
 import android.content.Context;
 
 public class ProgressManager {
 	private ProgressListAdapter progressAdapter;
 	private static ProgressManager ins;
 	private ProgressManager(Context ctx) {
 		progressAdapter = new ProgressListAdapter(ctx);
 	}
 	public static ProgressManager initManager(Context ctx) {
 		// to be called from the activity 
		if (ins != null)
 			ins = new ProgressManager(ctx);
 		return ins;
 	}
 	public static ProgressManager getInstance() {
 		// returns null if not initialized
 		return ins;
 	}
 	public ProgressCallback getProgressCallback(String tag) {
 		return progressAdapter.getCallbackByTag(tag);
 	}
 	public ProgressCallback createProgressCallback(String tag) {
 		return progressAdapter.newProgress(tag);
 	}
 	public ProgressListAdapter getAdapter() {
 		return progressAdapter;
 	}
 }
