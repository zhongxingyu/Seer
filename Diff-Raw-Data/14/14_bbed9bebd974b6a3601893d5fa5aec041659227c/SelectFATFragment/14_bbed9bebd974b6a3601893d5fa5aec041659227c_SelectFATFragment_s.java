 package de.questmaster.fatremote.fragments;
 
 import java.net.ConnectException;
 import java.net.InetAddress;
 import java.util.List;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteException;
 import android.os.Bundle;
 import android.support.v4.app.DialogFragment;
 import android.support.v4.app.ListFragment;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 import android.widget.SimpleCursorAdapter;
 import android.widget.Toast;
 import de.questmaster.fatremote.FatDevicesDbAdapter;
 import de.questmaster.fatremote.NetworkProxy;
 import de.questmaster.fatremote.R;
 import de.questmaster.fatremote.datastructures.FATDevice;
 
 // TODO: set marker on auto detected entries, e.g. icon
 
 public class SelectFATFragment extends ListFragment {
 
 	private static final String LOG_TAG = "SelectFATFragment";
 	
 	private Activity c = null;
 	private ProgressDialog mDialog = null;
 	private FatDevicesDbAdapter mDbHelper = null;
 	private FATSelectedListener mListener = null;
 	
 	
 	// Container Activity must implement this interface
     public interface FATSelectedListener {
         void onFATSelected(FATDevice dev);
     }
     
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		// enable options menu
 		this.setHasOptionsMenu(true);
 		
 		// set activity
 		c = this.getActivity();
 		
 		// set Listener callback
 		mListener = (FATSelectedListener) this.getActivity();
 		
 		// Init Database
 		mDbHelper = new FatDevicesDbAdapter(c);
 		try {
 			mDbHelper.open();
 		} catch (SQLiteException e) {
 			Log.e(LOG_TAG, "Database failed to initialize.", e);
 		}
 	}
 	
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		super.onCreateView(inflater, container, savedInstanceState);
 		
         if (container == null) {
             // We have different layouts, and in one of them this
             // fragment's containing frame doesn't exist.  The fragment
             // may still be created from its saved state, but there is
             // no reason to try to create its view hierarchy because it
             // won't be displayed.  Note this is not needed -- we could
             // just run the code below, where we would create and return
             // the view hierarchy; it would just never be used.
             return null;
         }
 
         // Inflate the layout for this fragment
         View root = inflater.inflate(R.layout.selectfat_fragment, container, false);
 		registerForContextMenu(root.findViewById(android.R.id.list));
 
         return root;
 	}
 	
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 
 		// check if WIFI available
 		if (!NetworkProxy.getInstance(c).isWifiEnabled()) {
 			Toast.makeText(c, R.string.app_err_enwifi, Toast.LENGTH_LONG).show();
 		}
 	}
 		
 	@Override
 	public void onListItemClick(ListView l, View v, int position, long id) {
 		super.onListItemClick(l, v, position, id);
 		
 		// retrieve selected fat
 		FATDevice fd = mDbHelper.fetchFatDeviceTyp(id);
 
 		mListener.onFATSelected(fd);
 		
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		if (!mDbHelper.isOpen()) {
 			mDbHelper.open();
 		}
 		
 		// fill list
 		this.updateListView();
 	}
 
//	@Override
//	public void onPause() {
//		super.onPause();
//		if (mDbHelper.isOpen()) {
//			mDbHelper.close();
//		}
//	}
 
 	/**
 	 * 
 	 */
 	private void getAvailableIps() {
 
 		// show progress dialog
 		mDialog = ProgressDialog.show(c, "", getResources().getString(R.string.dialog_wait_searching), true);
 
 		// get data
 		new Thread(new Runnable() {
 			public void run() {
 				List<FATDevice> dev = null;
 
 				// grab available ip's
 				try {
 					dev = NetworkProxy.getInstance(c).discoverFAT();
 
 					// enter dev's into database
 					for (FATDevice f : dev) {
 						addFATDeviceToDatabase(f);
 					}
 				} catch (ConnectException e) {
 					Log.e(LOG_TAG, e.getMessage(), e);
 				}
 
 				// close progress dialog
 				mDialog.dismiss();
 			}
 		}).start();
 	}
 
 	/**
 	 * Add menu items
 	 * 
 	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
 	 */
 	@Override
 	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
 		super.onCreateOptionsMenu(menu, inflater);
 		
 		inflater.inflate(R.menu.menu_select_fat, menu);
 	}
 
 	/**
 	 * Define menu action
 	 * 
 	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
 	 */
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.MENU_ADD:
 			showAddDeviceDialog();
 			
 			break;
 		case R.id.MENU_REFRESH:
 			getAvailableIps();
 
 			break;
 		default:
 			// should not happen
 			break;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
     @Override
     public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
         super.onCreateContextMenu(menu, v, menuInfo);
 
         MenuInflater inflater = getActivity().getMenuInflater();
         inflater.inflate(R.menu.contextmenu_select_fat, menu);
     }
 
     @Override
     public boolean onContextItemSelected(MenuItem item) {
     	AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item.getMenuInfo();
     	
     	
         switch (item.getItemId()) {
             case R.id.cm_delete:
                 mDbHelper.deleteFatDevice(menuInfo.id);
             	updateListView();
                 
                 return true;
             case R.id.cm_delete_all:
                 mDbHelper.deleteAllFatDevices();
             	updateListView();
                 
                 return true;
             default:
                 Log.e(LOG_TAG, "Should not happen: default case of switch reached.");
         }
         return super.onContextItemSelected(item);
     }
     
 	/**
 	 * 
 	 */
 	private void updateListView() {
 		// Get all of the notes from the database and create the item list
 		Cursor cursor = mDbHelper.fetchAllFatDevices();
 		getActivity().startManagingCursor(cursor);
 
 		ListAdapter fatDevices;
 
 		String[] childFrom = new String[] { FatDevicesDbAdapter.KEY_NAME, FatDevicesDbAdapter.KEY_IP };
 		int[] childTo = new int[] { R.id.textName, R.id.textIP };
 
 		fatDevices = new SimpleCursorAdapter(c, R.layout.selectfat_list_item, cursor, childFrom, childTo);
 
 		setListAdapter(fatDevices);
 	}
 
 	/**
 	 * @param dev
 	 */
 	private void addFATDeviceToDatabase(FATDevice dev) {
 		long rowId = mDbHelper.fetchFatDeviceId(dev.getIp());
 		if (rowId < 0) {
 			mDbHelper.createFatDevice(dev.getName(), dev.getIp(), dev.getPort(), dev.isAutoDetected());
 		} else {
 			mDbHelper.updateFatDevice(rowId, dev.getName(), dev.getIp(), dev.getPort(), dev.isAutoDetected());
 		}
 
 		if (mDbHelper.getAllFatDevicesCount() == 0) {
 			c.runOnUiThread(new Runnable() {
 				public void run() {
 					Toast.makeText(c, R.string.app_err_wifioff, Toast.LENGTH_LONG).show();
 				}
 			});
 		} else {
 			c.runOnUiThread(new Runnable() {
 				public void run() {
 					updateListView();
 				}
 			});
 		}
 
 	}
 
 	private void showAddDeviceDialog() {
 		DialogFragment addDialog = AddDeviceDialogFragment.newInstance(R.string.dialog_adddev_title);
 		addDialog.setTargetFragment(this, 0);
 		addDialog.show(getFragmentManager(), "dialog");
 	}
 
 	public void doPositiveClick(String name, String ip) {
 		InetAddress ia = null;
 		boolean errOccured = false;
 		
 		// check ip: 1. Match against regex, 2. check if ip correct
 		try {
 			if (ip.matches("\\b\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\b")) {
 				ia = InetAddress.getByName(ip);
 			} else {
 				throw new Exception("Bad Format");
 			}
 		} catch (Exception e) {
 			c.runOnUiThread(new Runnable() {
 				public void run() {
 					Toast.makeText(c, R.string.app_err_wrongip, Toast.LENGTH_LONG).show();
 				}
 			});
 			errOccured = true;
 		}
 		
 		// set
 		if (!errOccured) {
 			FATDevice dev = new FATDevice(name, ia, false);
 			addFATDeviceToDatabase(dev);
 		}
 	}
 
 }
 	
 
