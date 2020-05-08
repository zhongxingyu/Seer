 /*   
  * Copyright 2013 Karsten Patzwaldt
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.muckebox.android.net;
 
 import org.muckebox.android.Muckebox;
 
 import android.os.AsyncTask;
 import android.widget.Toast;
 
 public abstract class RefreshTask<Params> extends AsyncTask<Params, Void, Integer> {
     
     private boolean mWasRunning = false;
     
     public interface Callbacks {
         void onRefreshStarted();
         
         void onRefreshFinished(boolean success);
     }
     
     public boolean wasRunning() {
         return mWasRunning;
     }
     
 	Callbacks mCallbacks = null;
 	
 	public RefreshTask<Params> setCallbacks(Callbacks callbacks) {
 		mCallbacks = callbacks;
 		
 		return this;
 	}
 	
 	@Override
 	protected void onPreExecute() {
 		if (mCallbacks != null)
 			mCallbacks.onRefreshStarted();
 		
 		mWasRunning = true;
 	}
 
 	@Override
 	protected void onPostExecute(Integer errorCode) {
 		if (errorCode != null)
 			Toast.makeText(Muckebox.getAppContext(),
 					errorCode,
					Toast.LENGTH_SHORT).show();
 		
 		if (mCallbacks != null)
 			mCallbacks.onRefreshFinished(errorCode == null);
 	}
 }
