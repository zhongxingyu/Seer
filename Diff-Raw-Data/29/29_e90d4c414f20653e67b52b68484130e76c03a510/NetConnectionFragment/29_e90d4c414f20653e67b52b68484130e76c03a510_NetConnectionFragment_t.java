 package edu.uidaho.engr.seniordesign.umax;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.app.*;
 import android.os.*;
 import android.view.*;
 import android.widget.*;
 
 public class NetConnectionFragment extends ListFragment {
 	
 	public static NetConnectionFragment newInstance() {
 		NetConnectionFragment ncf = new NetConnectionFragment();
 		Bundle args = new Bundle();
 		ncf.setArguments(args);
 		return ncf;
 	}
 	
 	
 	private static final String[] ListItems = new String[] {
 		"Connect", "Disconnect"
 	};
 	
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setListAdapter(new ArrayAdapter<String>(this.getActivity().getApplicationContext(), android.R.layout.simple_list_item_1, ListItems));
 	}
     
     public interface OnNetListClickListener {
     	public void connect();
     	public void disconnect();
     }
 	
 	OnNetListClickListener mListener;
 	@Override
 	public void onAttach(Activity activity) {
 		super.onAttach(activity);
 		try {
 			mListener = (OnNetListClickListener) activity;
 		} catch (ClassCastException e) {
 			return;
 		}
 	}
 	
 	@Override
 	public void onListItemClick(ListView l, View v, int position, long id) {
 		if (mListener == null)
 			return;
 		
 		TextView tv = (TextView) v;
 		String action = tv.getText().toString();
 		if (action.compareTo("Connect") == 0) {
 			//mListener.connect("129.101.24.232");
 			mListener.connect();
 		} else {
 			mListener.disconnect();
 		}
 	}
 	
 	final Handler enableHandler = new Handler() {
 		public boolean en0 = false;
 		public boolean en1 = false;
 		
 		public void handleMessage(Message msg) {
 			en0 = (msg.arg1 == 1);
 			en1 = (msg.arg2 == 1);
 			try {
 				if (getListView().getChildCount() < 2) {
 					Timer t = new Timer();
 					try {
 						t.schedule(enableDisable, 50);
 					} catch (Exception e) { }
 				} else {
					getListView().getChildAt(0).setEnabled(!en0);
					getListView().getChildAt(1).setEnabled(!en1);
 					getListView().getChildAt(0).setClickable(!en0);
 					getListView().getChildAt(1).setClickable(!en1);
 					
 				}
 			} catch (IllegalStateException e) {
 				Timer t = new Timer();
 				try {
 					t.schedule(enableDisable, 50);
 				} catch (Exception f) { }
 			}
 		}
 		
 		private TimerTask enableDisable = new TimerTask() {
 			@Override
 			public void run() {
 				enableHandler.sendMessage(enableHandler.obtainMessage(0, en0 ? 1 : 0, en1 ? 1 : 0));
 			}
 			
 		};
 	};
 	
 	public void enableButton(boolean enableConnect, boolean enableDisconnect) {
 		enableHandler.sendMessage(enableHandler.obtainMessage(0, (enableConnect ? 1 : 0), (enableDisconnect ? 1 : 0)));
 	}
 }
