 package com.intalker.borrow.cloud;
 
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.os.AsyncTask;
 
 import com.intalker.borrow.HomeActivity;
 import com.intalker.borrow.R;
 
 public class CloudAPIAsyncTask extends AsyncTask<String, Void, Void> {
 
 	public interface ICloudAPITaskListener {
 		public void onFinish(boolean isSuccessful);
 	}
 
 	private ProgressDialog mProgressDialog = null;
 	private String mUrl = "";
 	private String mOp = "";
 	private boolean mIsSuccessful = false;
 	private ICloudAPITaskListener mAPIListener = null;
 
 	public CloudAPIAsyncTask(Context context, String url, String op,
 			ICloudAPITaskListener apiListener) {
 		super();
 		mUrl = url;
 		mOp = op;
 		mAPIListener = apiListener;
 		mProgressDialog = new ProgressDialog(context);
 		mProgressDialog.setCancelable(false);
 		mProgressDialog.setTitle(HomeActivity.getApp().getString(
 				R.string.please_wait));
 		mProgressDialog.setMessage(op);
 		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
 		mProgressDialog.show();
 	}
 
 	@Override
 	protected void onPreExecute() {
 		super.onPreExecute();
 		mIsSuccessful = false;
 	}
 
 	@Override
 	protected Void doInBackground(String... params) {
 		if (mOp.compareTo(CloudApi.API_Login) == 0) {
 			if (CloudApi._login(mUrl)) {
 				if (CloudApi._updateLoggedInUserInfo()) {
 					mIsSuccessful = true;
 				} else {
 
 				}
 			}
 		} else if (mOp.compareTo(CloudApi.API_SignUp) == 0) {
 			if (CloudApi._signUp(mUrl)) {
 				if (CloudApi._updateLoggedInUserInfo()) {
 					mIsSuccessful = true;
 				} else {
 
 				}
 			}
 		} else if (mOp.compareTo(CloudApi.API_GetUserInfo) == 0) {
 			if (CloudApi._updateLoggedInUserInfo()) {
 				mIsSuccessful = true;
 			}
 		}
 		return null;
 	}
 
 	@Override
 	protected void onPostExecute(Void result) {
 		super.onPostExecute(result);
		mProgressDialog.hide();
 		if (null != mAPIListener) {
 			mAPIListener.onFinish(mIsSuccessful);
 		}
 	}
 }
