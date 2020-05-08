 package com.edmondapps.android.sample.fragment;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 
 import android.app.Activity;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.AsyncTask;
 import android.os.Bundle;
 
 import com.edmondapps.utils.android.Logs;
 import com.edmondapps.utils.android.fragment.AbstractAsyncFragment;
 import com.edmondapps.utils.java.IoUtils;
 
 public class BitmapTaskFragment extends AbstractAsyncFragment<String, Void, Bitmap> {
 	public static final String TAG = "BitmapTaskFragment";
 
 	private static final String KEY_URL = "key_url";
 
 	public interface Callback {
 		void onBitmapLoaded(BitmapTaskFragment f, Bitmap b);
 	}
 
 	public static BitmapTaskFragment newInstance(String url) {
 		Bundle bundle = new Bundle();
 		bundle.putString(KEY_URL, url);
 
 		BitmapTaskFragment fragment = new BitmapTaskFragment();
 		fragment.setArguments(bundle);
 
 		return fragment;
 	}
 
 	private Callback mCallback;
 	private String mUrl;
 
 	@Override
 	public void onAttach(Activity activity) {
 		super.onAttach(activity);
 
 		if (activity instanceof Callback) {
 			mCallback = (Callback)activity;
 		} else {
 			throw new IllegalStateException(activity + " must implement Callback.");
 		}
 
 		mUrl = getArguments().getString(KEY_URL);
 	}
 
 	public void loadNewBitmap(String url, Bitmap inBitmap) {
 		mUrl = url;
 		newAsyncTask(onCreateAsyncTask());
 	}
 
 	public void finish() {
		getFragmentManager()
				.beginTransaction()
				.remove(this)
				.commit();
 	}
 
 	@Override
 	protected AsyncTask<String, Void, Bitmap> onCreateAsyncTask() {
 		return new AsyncTask<String, Void, Bitmap>() {
 			private static final int MAX_RETRIES = 3;
 			private int mRetryCount;
 
 			@Override
 			protected Bitmap doInBackground(String... url) {
 				Bitmap bitmap = null;
 
 				HttpURLConnection conn = null;
 				InputStream stream = null;
 				try {
 					conn = IoUtils.newConnection(mUrl);
 					stream = conn.getInputStream();
 
 					bitmap = BitmapFactory.decodeStream(stream);
 				} catch (IOException e) {
 					mRetryCount++;
 					Logs.e(TAG, "mRetryCount: " + mRetryCount, e);
 					if (mRetryCount <= MAX_RETRIES) {
 						return doInBackground(url);
 					}
 				} finally {
 					IoUtils.quietClose(stream);
 					IoUtils.quietDisconnet(conn);
 				}
 
 				return bitmap;
 			}
 
 			@Override
 			protected void onPostExecute(Bitmap result) {
 				super.onPostExecute(result);
 				mCallback.onBitmapLoaded(BitmapTaskFragment.this, result);
 			}
 
 			@Override
 			protected void onCancelled(Bitmap result) {
 				if (result != null) {
 					result.recycle();
 				}
 				mCallback.onBitmapLoaded(BitmapTaskFragment.this, null);
 			}
 		};
 	}
 }
