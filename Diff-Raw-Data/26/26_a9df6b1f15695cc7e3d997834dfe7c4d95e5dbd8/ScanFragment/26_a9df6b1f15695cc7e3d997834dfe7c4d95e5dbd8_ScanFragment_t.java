 /*Copyright [2010-2011] [David Van de Ven]
 
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
 
        http://www.apache.org/licenses/LICENSE-2.0
 
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 
  */
 
 package org.wahtod.wififixer.ui;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import org.wahtod.wififixer.R;
 import org.wahtod.wififixer.utility.NotifUtil;
 import org.wahtod.wififixer.utility.StringUtil;
 import org.wahtod.wififixer.utility.WFScanResult;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.graphics.Color;
 import android.graphics.PorterDuff;
 import android.net.wifi.WifiManager;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.support.v4.app.Fragment;
 import android.view.ContextMenu;
 import android.view.LayoutInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.AdapterView.OnItemLongClickListener;
 
 public class ScanFragment extends Fragment {
 	private boolean receiverRegistered;
 	private WFScanResult clicked;
 	private ScanListAdapter adapter;
 	private ListView lv;
 
 	private static final int CONTEXT_CONNECT = 13;
 	private static final int CONTEXT_INFO = 15;
 	protected static final int REFRESH_LIST_ADAPTER = 0;
 	protected static final int CLEAR_LIST_ADAPTER = 1;
 	protected static Fragment self;
 
 	private Handler drawhandler = new Handler() {
 		@Override
 		public void handleMessage(Message message) {
 			/*
 			 * handle SCAN_RESULTS_AVAILABLE intents to refresh ListView
 			 * asynchronously (to avoid ANR)
 			 */
 			switch (message.what) {
 			case REFRESH_LIST_ADAPTER:
 				if (getActivity() != null)
 					refreshScanListAdapter();
 				break;
 
 			case CLEAR_LIST_ADAPTER:
 				if (getActivity() != null)
 					clearScanListAdapter();
 				break;
 			}
 
 		}
 	};
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		View v = inflater.inflate(R.layout.scannetworks, null);
 		lv = (ListView) v.findViewById(R.id.scanlist);
 		registerContextMenu();
 		registerReceiver();
 		self = this;
 		return v;
 	}
 
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v,
 			ContextMenuInfo menuInfo) {
 		super.onCreateContextMenu(menu, v, menuInfo);
 		/*
 		 * Clicked is the ListView selected WFScanResult
 		 */
 		menu.setHeaderTitle(clicked.SSID);
 		menu.add(4, CONTEXT_CONNECT, 2,
 				getConnectMenuStringFromClicked(getContext(), clicked));
 		menu.add(4, CONTEXT_INFO, 3, R.string.about);
 	}
 
 	@Override
 	public void onDestroy() {
 		unregisterReceiver();
 		super.onDestroy();
 	}
 
 	public static int getConnectMenuStringFromClicked(final Context context,
 			WFScanResult clicked) {
 		if (KnownNetworksFragment.getNetworks(context).contains(clicked.SSID))
 			return R.string.connect;
 		else if (StringUtil.getCapabilitiesString(clicked.capabilities) == StringUtil.OPEN)
 			return R.string.connect;
 		else
 			return R.string.add;
 	}
 
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 
 		switch (item.getItemId()) {
 		case CONTEXT_CONNECT:
 			dispatchContextMenuSelected(ConnectFragment.class.getName());
 			break;
 
 		case CONTEXT_INFO:
 			dispatchContextMenuSelected(AboutFragment.class.getName());
 			break;
 		}
 
 		return super.onContextItemSelected(item);
 	}
 
 	private void dispatchContextMenuSelected(String classname) {
 		Intent i = new Intent(getContext(), WifiFixerActivity.class);
 		i.putExtra(WifiFixerActivity.SHOW_FRAGMENT, true);
 		i.putExtra(WFScanResult.BUNDLE_KEY, clicked.toBundle());
 		i.putExtra(FragmentSwitchboard.FRAGMENT_KEY, classname);
 		i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
 		getActivity().startActivity(i);
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		List<WFScanResult> scan = getNetworks(getContext());
 		if (adapter == null)
 			createAdapter(getView(), scan);
 	}
 
 	/*
 	 * custom adapter for Network List ListView
 	 */
 	private class ScanListAdapter extends ArrayAdapter<WFScanResult> {
 		private List<WFScanResult> scanresultArray;
 		private LayoutInflater inflater;
 
 		public ScanListAdapter(Context context, int textViewResourceId,
 				List<WFScanResult> scan) {
 			super(context, textViewResourceId);
 			inflater = (LayoutInflater) getContext().getSystemService(
 					Context.LAYOUT_INFLATER_SERVICE);
 			scanresultArray = scan;
 		}
 
 		public int getCount() {
 			if (scanresultArray == null)
 				return 0;
 			else
 				return scanresultArray.size();
 		}
 
 		public WFScanResult getItem(int position) {
 			return scanresultArray.get(position);
 		}
 
 		public long getItemId(int position) {
 			return position;
 		}
 
 		public View getView(int position, View convertView, ViewGroup parent) {
 			ViewHolder holder;
 			if (convertView == null) {
 				convertView = inflater.inflate(R.layout.scan_list_layout, null);
 				holder = new ViewHolder();
 				holder.text = (TextView) convertView.findViewById(R.id.ssid);
 				holder.icon = (ImageView) convertView
 						.findViewById(R.id.NETWORK_ICON);
 				holder.encryption = (TextView) convertView
 						.findViewById(R.id.encryption);
 				holder.security = (ImageView) convertView
 						.findViewById(R.id.SECURE_ICON);
 				convertView.setTag(holder);
 			} else {
 				holder = (ViewHolder) convertView.getTag();
 			}
 			/*
 			 * Set SSID text and color
 			 */
 			holder.text.setText(scanresultArray.get(position).SSID);
 
 			/*
 			 * Set signal icon
 			 */
 			int adjusted = WifiManager.calculateSignalLevel(
 					scanresultArray.get(position).level, 5);
 
 			holder.icon.setImageResource(NotifUtil.getIconfromSignal(adjusted,
 					NotifUtil.ICON_SET_SMALL));
 
 			/*
 			 * Set security icon and encryption text
 			 */
 			if (StringUtil.getCapabilitiesString(
 					scanresultArray.get(position).capabilities).equals(
 					StringUtil.OPEN)) {
 				holder.security.setImageResource(R.drawable.buttons);
 				holder.security.setColorFilter(Color.GREEN,
 						PorterDuff.Mode.SRC_ATOP);
 				holder.encryption.setText(R.string.open_network);
 			} else {
 				holder.security.setColorFilter(Color.TRANSPARENT,
 						PorterDuff.Mode.SRC_ATOP);
 				holder.security.setImageResource(R.drawable.secure);
 				holder.encryption
 						.setText(StringUtil
 								.getCapabilitiesString(scanresultArray
 										.get(position).capabilities));
 			}
 
 			return convertView;
 		}
 
 		private class ViewHolder {
 			TextView text;
 			TextView encryption;
 			ImageView icon;
 			ImageView security;
 		}
 	}
 
 	public static ScanFragment newInstance(int num) {
 		ScanFragment f = new ScanFragment();
 
 		// Supply num input as an argument.
 		Bundle args = new Bundle();
 		args.putInt("num", num);
 		f.setArguments(args);
 
 		return f;
 	}
 
 	private BroadcastReceiver receiver = new BroadcastReceiver() {
 		public void onReceive(final Context context, final Intent intent) {
 			/*
 			 * On Scan result intent refresh ListView
 			 */
 			if (self.isDetached())
 				return;
 			else if (intent.getAction().equals(
 					WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
 				drawhandler.sendEmptyMessage(REFRESH_LIST_ADAPTER);
 			else if (intent.getExtras().getInt(WifiManager.EXTRA_WIFI_STATE) == WifiManager.WIFI_STATE_DISABLED) {
 				/*
 				 * clear list
 				 */
 				if (!(adapter == null)) {
 					drawhandler.sendEmptyMessage(CLEAR_LIST_ADAPTER);
 				}
 			}
 		}
 	};
 
 	protected void clearScanListAdapter() {
 		WFScanResult w = new WFScanResult();
 		w.SSID = getString(R.string.wifi_is_disabled);
 		w.level = -99;
 		List<WFScanResult> list = new ArrayList<WFScanResult>();
 		list.add(w);
 		refreshArray(list);
 		adapter.notifyDataSetChanged();
 	}
 
 	/*
 	 * Create adapter
 	 */
 	private void createAdapter(View v, List<WFScanResult> scan) {
 		adapter = new ScanListAdapter(getContext(), 0, scan);
 		lv.setAdapter(adapter);
 	}
 
 	private Context getContext() {
 		return getActivity().getApplicationContext();
 	}
 
 	/*
 	 * Note that this WILL return a null String[] if called while wifi is off.
 	 */
 	private static List<WFScanResult> getNetworks(final Context context) {
 		WifiManager wm = (WifiManager) context
 				.getSystemService(Context.WIFI_SERVICE);
 
 		if (wm.isWifiEnabled())
 			return WFScanResult.fromScanResultArray(wm.getScanResults());
 		else
 			return new ArrayList<WFScanResult>();
 	}
 
 	private void refreshScanListAdapter() {
 		/*
 		 * Firing this from a handler In case of ANR
 		 */
 		List<WFScanResult> scan = getNetworks(getContext());
 		if (adapter == null)
 			createAdapter(getView(), scan);
 		refreshArray(scan);
 		adapter.notifyDataSetChanged();
 	}
 
 	private void refreshArray(List<WFScanResult> scan) {
		
		/*
		 * Null check
		 */
		if (scan == null)
			return;
 
 		/*
 		 * Comparator for sorting results by signal level
 		 */
 		class SortBySignal implements Comparator<WFScanResult> {
 			public int compare(WFScanResult o2, WFScanResult o1) {
 				/*
 				 * Sort by signal
 				 */
 				return (o1.level < o2.level ? -1 : (o1.level == o2.level ? 0
 						: 1));
 			}
 		}
 
 		List<WFScanResult> toremove = new ArrayList<WFScanResult>();
 		for (WFScanResult result : adapter.scanresultArray) {
 			if (!scan.contains(result))
 				toremove.add(result);
 		}
 
 		for (WFScanResult result : toremove) {
 			adapter.scanresultArray.remove(result);
 		}
 
		for (WFScanResult network : scan) {
			/*
			 * If network isn't in adapter's list
			 * add it, otherwise update signal level
			 */
			if (!adapter.scanresultArray.contains(network))
				adapter.scanresultArray.add(network);
 			else {
				int index = adapter.scanresultArray.indexOf(network);
 				if (index != -1) {
					if (network.level != adapter.scanresultArray.get(index).level)
						adapter.scanresultArray.get(index).level = network.level;
 				}
 			}
 		}
 		Collections.sort(adapter.scanresultArray, new SortBySignal());
 	}
 
 	private void registerContextMenu() {
 		lv.setOnItemLongClickListener(new OnItemLongClickListener() {
 			@Override
 			public boolean onItemLongClick(AdapterView<?> adapterview, View v,
 					int position, long id) {
 				clicked = adapter.scanresultArray.get(position);
 				return false;
 			}
 
 		});
 		registerForContextMenu(lv);
 	}
 
 	private void registerReceiver() {
 		if (receiverRegistered == true)
 			return;
 		IntentFilter filter = new IntentFilter(
 				WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
 		filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
 		getContext().registerReceiver(receiver, filter);
 		receiverRegistered = true;
 	}
 
 	private void unregisterReceiver() {
 		if (receiverRegistered) {
 			getContext().unregisterReceiver(receiver);
 			receiverRegistered = false;
 		}
 	}
 }
