 package il.ac.huji.beepme.ui;
 
 import il.ac.huji.beepme.business.BeepMeApplication;
 import il.ac.huji.beepme.db.Customer;
 import il.ac.huji.beepme.db.Queue;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentTransaction;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import il.ac.huji.beepme.business.R;
 
 public class StationFragment extends Fragment implements Queue.OnQueueStatusChangedListener, ConfirmFragment.ConfirmListener, View.OnClickListener{
 
 	private TextView tv_waiting;
 	private TextView tv_number;
 	private TextView tv_total;
 	private TextView tv_uid;
 	private Button bt_next;	
 	private LinearLayout ll_uid;
 	
 	private Queue queue;
 			
 	private static final int ID_RETRY = 1;
 	
 	private Handler mHandler;
 		
 	public static StationFragment newInstance(){
 		StationFragment fragment = new StationFragment();
 				
 		return fragment;
 	}
 	
 	@Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		View v = inflater.inflate(R.layout.fragment_station, container, false);
 		
 		tv_waiting = (TextView)v.findViewById(R.id.station_tv_waiting);
 		tv_number = (TextView)v.findViewById(R.id.station_tv_number);
 		tv_total = (TextView)v.findViewById(R.id.station_tv_total);
 		tv_uid = (TextView)v.findViewById(R.id.station_tv_uid);
 		ll_uid = (LinearLayout)v.findViewById(R.id.station_ll_uid);
 		bt_next = (Button)v.findViewById(R.id.station_bt_next);
 		
 		bt_next.setOnClickListener(this);
 		
 		queue = BeepMeApplication.workingQueue;
 		queue.addOnQueueStatusChangedListener(this);
 					
 		showCustomer();
 		
 		mHandler = new Handler();
 		
 		return v;
 	}
 	
 	public void onResume(){
 		super.onResume();		
 	}
 		
 	public void onPause(){
 		super.onPause();			
 	}
 	
 	public void onDestroy(){
 		super.onDestroy();
 		queue.removeOnQueueStatusChangedListener(this);
 	}
 	
 	protected void showInfoDialog(String title, int iconID, String message, String okText){
 		FragmentTransaction ft = getFragmentManager().beginTransaction();
 		
 	    InfoFragment dialog = InfoFragment.newInstance(title, iconID, message, okText);
 	    ft.add(dialog, InfoFragment.class.getName());
 	    ft.commitAllowingStateLoss();
 	}
 	
 	protected void showConfirmDialog(int id, String title, int iconID, String message){
 		FragmentTransaction ft = getFragmentManager().beginTransaction();
 		
 	    ConfirmFragment dialog = ConfirmFragment.newInstance(id, title, iconID, message);
 	    dialog.setListener(this);
 	    ft.add(dialog, ConfirmFragment.class.getName());
 	    ft.commitAllowingStateLoss();
 	}
 	
 	private void showCustomer(){
 		tv_total.setText(String.format(getString(R.string.total), queue.getTotal()));
 		
 		Customer customer = queue.getCustomer();
 		
 		if(customer != null){
 			tv_waiting.setText("Waiting for client:");
 			tv_number.setText(String.valueOf(customer.number));
 			ll_uid.setVisibility(View.VISIBLE);
 			tv_uid.setText(customer.uid);
 			bt_next.setText(queue.getCurrent() < queue.getTotal() ? "Next" : "Done");
 		}
 		else{
 			tv_waiting.setText("Last client:");
 			tv_number.setText(String.valueOf(queue.getCurrent()));
 			ll_uid.setVisibility(View.INVISIBLE);
			if(queue.getCurrent() == queue.getTotal()){
 				Toast.makeText(getActivity(), "There are no more customers, please select a different queue!", Toast.LENGTH_LONG).show();
 				getActivity().onBackPressed();
 			}
 		}
 	}
 	
 	@Override
 	public void onClick(View v) {
 		switch (v.getId()) {
 			case R.id.station_bt_next:
 				bt_nextClicked();
 				break;
 		}
 	}
 		
 	private void bt_nextClicked(){
 		FindCustomerTask task = new FindCustomerTask(getActivity(), queue);
 		task.start();
 	}
 
 	@Override
 	public void onQueueTotalChanged(final int total) {
 		mHandler.post(new Runnable() {			
 			@Override
 			public void run() {
 				showCustomer();
 			}
 		});		
 	}
 
 	@Override
 	public void onQueueDeleted() {
 	}
 		
 	@Override
 	public void confirm(ConfirmFragment dialog, boolean yes) {
 		if(!yes)
 			return;
 		
 		if(dialog.getConfirmId() == ID_RETRY)
 			bt_nextClicked();
 	}
 	
 	private class FindCustomerTask extends AsyncTask<Void, Void, String>{
 
 		private Queue queue;
 		private Context mContext;
 		private ProgressDialog dialog;
 		
 		public FindCustomerTask(Context context, Queue queue){
 			this.queue = queue;
 			this.mContext = context;
 		}
 		
 		public void start(){
 			this.execute(new Void[0]);	
 		}
 		
 		@Override
 		protected void onPreExecute(){
 			dialog = ProgressDialog.show(mContext, "", "Please wait ...", true, false);
 		}
 		
 		@Override
 		protected String doInBackground(Void... params) {			
 			return queue.findNextCustomer();
 		}
 		
 		@Override
 		protected void onPostExecute(String result){
 			dialog.dismiss();
 			
 			if(result == null)
 				showCustomer();			
 			else
 				showConfirmDialog(ID_RETRY, "Error", android.R.drawable.ic_dialog_alert, result + "\nDo you want to try again?");
 		}
 		
 	}
 	
 }
