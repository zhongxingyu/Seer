 package com.envsocial.android.features.order;
 
 import org.apache.http.HttpStatus;
 
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.os.AsyncTask;
 import android.util.Log;
 import android.widget.Toast;
 
 import com.envsocial.android.R;
 import com.envsocial.android.api.Annotation;
 import com.envsocial.android.api.exceptions.EnvSocialComException;
 import com.envsocial.android.api.exceptions.EnvSocialContentException;
 import com.envsocial.android.utils.ResponseHolder;
 
 public class SendOrderTask extends AsyncTask<Void, Void, ResponseHolder> {
 	private static final String TAG = "SendOrderTask";
 	
 	// loader dialog for sending an order
 	private ProgressDialog mSendOrderDialog;
 	private Context mContext;
 	private ISendOrder mOrderFragment;
 	private boolean error = true;
 	
 	private Annotation mOrder;
 	
 	public SendOrderTask(Context context, ISendOrder orderFragment, Annotation order) {
 		this.mOrder = order;
 		this.mContext = context;
 		this.mOrderFragment = orderFragment;
 	}
 	
 	
 	@Override
 	protected void onPreExecute() {
 		mSendOrderDialog = ProgressDialog.show(mContext, 
 				"", "Sending Order ...", true);
 	}
 	
 	
 	@Override
 	protected ResponseHolder doInBackground(Void...args) {
 		return mOrder.post(mContext);
 	}
 	
 	
 	@Override
 	protected void onPostExecute(ResponseHolder holder) {
 		mSendOrderDialog.cancel();
 		
 		if (!holder.hasError()) {
 			error = false;
 			int msgId = R.string.msg_send_order_ok;
 
 			switch(holder.getCode()) {
 			case HttpStatus.SC_CREATED: 					
 				error = false;
 				break;
 
 			case HttpStatus.SC_BAD_REQUEST:
 				msgId = R.string.msg_send_order_400;
 				error = true;
 				break;
 
 			case HttpStatus.SC_UNAUTHORIZED:
 				msgId = R.string.msg_send_order_401;
 				error = true;
 				break;
 
 			case HttpStatus.SC_METHOD_NOT_ALLOWED:
 				msgId = R.string.msg_send_order_405;
 				error = true;
 				break;
 
 			default:
 				msgId = R.string.msg_send_order_err;
 				error = true;
 				break;
 			}
 
 			if (error) {
				Log.d(TAG, "[DEBUG]>> Error sending order: " + msgId);
 				Toast toast = Toast.makeText( mContext,
 						msgId, Toast.LENGTH_LONG);
 				toast.show();
 			}
 			else {
 				Toast toast = Toast.makeText( mContext,
 						msgId, Toast.LENGTH_LONG);
 				toast.show();
 			}
 		}
 		else {
 			int msgId = R.string.msg_service_unavailable;
 
 			try {
 				throw holder.getError();
 			} catch (EnvSocialComException e) {
 				Log.d(TAG, e.getMessage(), e);
 				msgId = R.string.msg_service_unavailable;
 			} catch (EnvSocialContentException e) {
 				Log.d(TAG, e.getMessage(), e);
 				msgId = R.string.msg_service_error;
 			} catch (Exception e) {
 				Log.d(TAG, e.toString(), e);
 				msgId = R.string.msg_service_error;
 			}
 
 			Toast toast = Toast.makeText(mContext, msgId, Toast.LENGTH_LONG);
 			toast.show();
 		}
 		
 		// call post send order handler on the parent fragment
 		mOrderFragment.postSendOrder(!error);
 	}
 	
 	
 	public boolean successful() {
 		return !error;
 	}
 }
