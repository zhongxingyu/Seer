 package net.skweez.sipgate.model;
 
 import java.util.List;
 import java.util.Observable;
 
 import net.skweez.sipgate.api.Call;
 import net.skweez.sipgate.api.ISipgateAPI;
 import net.skweez.sipgate.api.Price;
 import net.skweez.sipgate.api.SipgateException;
 import net.skweez.sipgate.api.UserName;
 import net.skweez.sipgate.api.UserUri;
 import net.skweez.sipgate.api.xmlrpc.SipgateXmlRpcImpl;
 import android.app.Activity;
 import android.os.AsyncTask;
 import android.util.Log;
 
 /**
  * @author Florian Mutter
  * @author Michael Kanis
  */
 public class AccountInfo extends Observable {

 	private static final String TAG = AccountInfo.class.getName();
 
 	private UserName userName;
 
 	private UserUri defaultUserUri;
 
 	private Price balance;

 	private List<Call> callHistory;
 
 	private Activity mActivity;
 
 	public void refresh(Activity activity) {
 		mActivity = activity;
		new RefreshAccountInfoTask().execute();
 	}

 	private class RefreshAccountInfoTask extends AsyncTask<Void, Void, Void> {

 		private Exception exception = null;
 
 		@Override
 		protected Void doInBackground(Void... params) {
 			try {
 				final ISipgateAPI sipgate = new SipgateXmlRpcImpl();
 
 				setUserName(sipgate.getUserName());
 				setDefaultUserUri(sipgate.getUserUriList());
 				setBalance(sipgate.getBalance());
 				setCallHistory(sipgate.getHistoryByDate());
 			} catch (SipgateException exception) {
 				Log.e(TAG, "Exception when refreshing AccountInfo", exception);
 				this.exception = exception;
 			}
 
 			return null;
 		}

 		@Override
 		protected void onPostExecute(Void result) {
			mActivity.setProgressBarIndeterminateVisibility(false);
 			setChanged();
 			notifyObservers(exception);
 		}
 
 		@Override
 		protected void onPreExecute() {
			mActivity.setProgressBarIndeterminateVisibility(true);
 		}

 	}
 
 	public String getCustomerNumber() {
 		if (getDefaultUserUri() != null)
 			return getDefaultUserUri().getSipUri().getUserInfo();
 		else
 			return "";
 	}
 
 	public String getPhoneNumber() {
 		if (getDefaultUserUri() != null)
 			return getDefaultUserUri().getOutgoingNumber();
 		else
 			return "";
 	}
 
 	public UserUri getDefaultUserUri() {
 		return defaultUserUri;
 	}
 
 	private synchronized void setDefaultUserUri(UserUri[] userUriArray) {
 		for (int i = 0; i < userUriArray.length; i++) {
 			if (userUriArray[i].isDefaultUri())
 				defaultUserUri = userUriArray[i];
 		}
 		// if there is no default userUri, we use the first one in the array
 		defaultUserUri = userUriArray[0];
 	}
 
 	public UserName getUserName() {
 		return userName;
 	}
 
 	private synchronized void setUserName(UserName userName) {
 		this.userName = userName;
 	}
 
 	public Price getBalance() {
 		return balance;
 	}
 
 	private synchronized void setBalance(Price balance) {
 		this.balance = balance;
 	}
 
 	public Call getCall(int index) {
 		return callHistory.get(index);
 	}

 	private void setCallHistory(List<Call> callHistory) {
 		this.callHistory = callHistory;
 	}
 
 	public int getCallHistorySize() {
 		if (callHistory == null) {
 			return 0;
 		}
 
 		return callHistory.size();
 	}
 
 }
