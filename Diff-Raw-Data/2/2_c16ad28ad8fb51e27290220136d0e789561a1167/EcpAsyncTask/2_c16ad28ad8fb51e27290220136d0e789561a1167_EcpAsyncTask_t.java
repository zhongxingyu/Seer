 package com.kniffenwebdesign.roku;
 
 import android.os.AsyncTask;
 import com.kniffenwebdesign.roku.ecp.Key;
 
 public class EcpAsyncTask extends AsyncTask<Key, Integer, Boolean> {
 	@Override
 	protected Boolean doInBackground(Key... keys) {
 		for (Key key : keys) {
			key.keyPress();
 		}
 		return true;
 	}
 }
