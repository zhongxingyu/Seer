 package com.code_pig.pocketfma;
 
 import android.os.AsyncTask;
 import android.util.Log;
 
 /**
  *  Asynchronous task to prepare {@link MusicRetriever} and report completion 
  *  to {@MusicRetrieverPreparedListener}.
  **/
 public class MusicRetrieverTask extends AsyncTask<String,Void,Void> {
 	final static String TAG = "MusicRetrieverTask";
 	MusicRetriever retriever;
 	MusicRetrieverPreparedListener listener;
 
 	public MusicRetrieverTask(MusicRetriever retriever, MusicRetrieverPreparedListener listener) {
 		Log.i(TAG, "MusicRetrieverTask() created");
 		this.retriever = retriever;
 		this.listener = listener;
 	}
 	
 	@Override
 	protected Void doInBackground(String... params) {
 		Log.i(TAG, "doInBackground() called");
		retriever.onInit(params[0]);
 		return null;
 	}
 	
 	@Override
 	protected void onPostExecute(Void result) {
 		Log.i(TAG, "onPostExecute() called");
 		listener.onMusicRetrieverPrepared();
 	}
 	
 	//TODO document
     public interface MusicRetrieverPreparedListener {
         public void onMusicRetrieverPrepared();
     }
 }
