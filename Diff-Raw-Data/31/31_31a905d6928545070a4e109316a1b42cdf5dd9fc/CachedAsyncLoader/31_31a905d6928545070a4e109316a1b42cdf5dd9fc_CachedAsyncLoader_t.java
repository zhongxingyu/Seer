 package me.kevinwells.darxen.loaders;
 
 import me.kevinwells.darxen.C;
 import android.content.Context;
 import android.support.v4.content.AsyncTaskLoader;
 import android.util.Log;
 
 public abstract class CachedAsyncLoader<D> extends AsyncTaskLoader<D> {
 	
 	private D mData;
 
 	public CachedAsyncLoader(Context context) {
 		super(context);
 	}
 	
 	@Override
 	protected void onStartLoading() {
 		if (mData == null || shouldUpdate()) {
 			forceLoad();
 			
 		} else {
 			deliverResult(mData);	
 		}
 	}
 
 	@Override
 	public D loadInBackground() {
		try {
 			return doInBackground();
		} catch (RuntimeException ex) {
			Log.e(C.TAG, "Unhandled exception in loader", ex);
			throw ex;
 		}
 	}
 	
 	/**
 	 * Implementations can override this method to specify
 	 * whether or not the loader should use the cached result
 	 * or recompute the data
 	 *  
 	 * @return True if the AsyncTask should be run
 	 */
 	protected boolean shouldUpdate() {
 		return true;
 	}
 	
 	/**
 	 * Implementations can override this method to cleanup
 	 * data that is about to be discarded
 	 * 
 	 * @param data The data to be discarded
 	 */
 	protected void cleanupData(D data) {}
 	
 	protected abstract D doInBackground();
 	
 	@Override
 	public void onCanceled(D data) {
 		//override to dispose data 
 	}
 	
 	@Override
 	public void deliverResult(D data) {
 		D oldData = mData;
 		
 		mData = data;
 		
 		if (isStarted()) {
 			super.deliverResult(data);
 		}
 		
 		if (oldData != null && oldData != data) {
 			cleanupData(oldData);
 		}
 	}
 	
 	@Override
 	protected void onReset() {
 		super.onReset();
 		
 		onStopLoading();
 		
 		if (mData != null) {
 			cleanupData(mData);
 			mData = null;
 		}
 	}
 	
 }
