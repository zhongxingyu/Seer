 package edu.ucsb.geog.blake_regalia.wap_tracer;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.util.Log;
 
 public class Timeout {
 	
 	private static final int DEFAULT_SIZE = 16;
 	private static int mSize = DEFAULT_SIZE;
 	
 	private static Timeout[] timeouts = new Timeout[mSize];
 	private static boolean[] indicies = new boolean[mSize];
 
 	private static int index = 0;
 
 	
 	private int mId;
 	private Timer mTimer;
 	private Runnable mTask;
 	private boolean cancelled = false;
 	
 	private Timeout(int id, Runnable task, final long delay) {
 		mId = id;
 		mTimer = new Timer();
 		mTask = task;
 		mTimer.schedule(new TimerTask() {
 			@Override
 			public void run() {
 				if(cancelled) {
 					Log.e("Timeout","task was cancelled but it's still running!");		
 				}
 				else {
 					freeSlot(mId);
 					mTask.run();
 				}
 			}
 		}, delay);
 	}
 	
 	private void cancel() {
 		cancelled = true;
 		mTimer.cancel();
 		freeSlot(mId);
 	}
 	
 	private static void freeSlot(int id) {
 		indicies[id] = false;
 		Log.i("Timeout", "freed slot: "+id+"; ");
 		print();
 	}
 	
 	private static void print() {
 		StringBuilder sb = new StringBuilder();
 		for(int i=0; i<mSize; i++) {
 			sb.append(", "+(indicies[i]?"1":"0"));
 		}
 		Log.i("Timeout", sb.toString());
 	}
 	
 	public static int setTimeout(Runnable task, long delay) {
 		int began = index;
 		while(indicies[index] == true) {
 			index += 1;
 			index %= mSize;
 			if(index == began) {
 				System.err.println("ERROR: RAN OUT OF TIMEOUTS");
 			}
 		}
 		timeouts[index] = new Timeout(index, task, delay);
 		indicies[index] = true;
 		
		Log.i("Timeout", "took new slot: "+index+";");
		print();
 		
 		return index;
 	}
 	
 	public static void clearTimeout(int id) {
		Log.i("Timeout", "clearing Timeout: "+id);
 		
 		Timeout timeout = timeouts[id];
 		timeout.cancel();
 		timeouts[id] = null;
 
 		freeSlot(id);
 	}
 }
