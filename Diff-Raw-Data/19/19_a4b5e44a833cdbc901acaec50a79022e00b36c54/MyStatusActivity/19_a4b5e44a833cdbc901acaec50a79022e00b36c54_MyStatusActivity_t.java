 package org.kth.cos.android.sw;
 
 import java.util.HashMap;
 import java.util.List;
 
 import org.kth.cos.android.sw.data.Response;
 import org.kth.cos.android.sw.data.ResponseStatus;
 import org.kth.cos.android.sw.data.Status;
 import org.kth.cos.android.sw.data.UserAccount;
 import org.kth.cos.android.sw.network.StatusService;
 
 import android.app.Dialog;
 import android.app.ListActivity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.os.Bundle;
 import android.os.Handler;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.TextView;
 
 public class MyStatusActivity extends ListActivity {
 	List<Status> statusList;
 
 	final Handler mHandler = new Handler();
 	Dialog progressDialog;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setTitle("My Status");
 		startLoadingList();
 	}
 
 	private void generateList() {
 		StatusService friendService = getStatusService();
 		try {
 			Response response = friendService.getStatusList();
 			if (response.getStatus() == ResponseStatus.STATUS_SUCCESS) {
 				statusList = (List<Status>) (response.getResponse());
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	private StatusService getStatusService() {
 		UserAccount account = UserAccount.getAccount(this);
 		StatusService statusService = new StatusService(account.getEmail(), account.getDataAuthToken());
 		return statusService;
 	}
 
 	private void updateStatusListInUI() {
 		this.setListAdapter(new StatusListAdapter(this, statusList));
 		registerForContextMenu(getListView());
 		this.progressDialog.dismiss();
 	}
 
 	final Runnable resultUpdater = new Runnable() {
 		public void run() {
 			updateStatusListInUI();
 		}
 	};
 
 	protected void startLoadingList() {
 		this.progressDialog = ProgressDialog.show(MyStatusActivity.this, "Wait", "Loading notifications...", true);
 		Thread t = new Thread() {
 			public void run() {
 				generateList();
 				mHandler.post(resultUpdater);
 			}
 		};
 		t.start();
 	}
 
 	class StatusListAdapter extends ArrayAdapter<Status> {
 
 		public StatusListAdapter(Context context, List<Status> objects) {
 			super(context, R.id.txtStatus, objects);
 		}
 
 		@Override
 		public View getView(final int position, View convertView, ViewGroup parent) {
 			View v = convertView;
 			if (v == null) {
 				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 				v = vi.inflate(R.layout.status_rowlayout, null);
 			}
 			final Status status = statusList.get(position);
			((TextView) v.findViewById(R.id.txtStatus)).setText(status.getValue());
			((TextView) v.findViewById(R.id.txtPostedAt)).setText(status.getPostedAtStr());
 			return v;
 		}
 	}
 
 }
