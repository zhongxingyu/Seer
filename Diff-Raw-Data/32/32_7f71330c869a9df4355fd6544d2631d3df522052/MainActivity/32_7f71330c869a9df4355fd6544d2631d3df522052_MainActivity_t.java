 package net.hogelab.android.AppDetailsSettings;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.net.Uri;
 import android.os.Bundle;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ListActivity;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.content.Intent;
 import android.content.pm.PackageInfo;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.ListView;
 
 
 //--------------------------------------------------
 // class MainActivity
 
 public class MainActivity extends ListActivity {
 
 	private static final String TAG = MainActivity.class.getSimpleName();
 
 	private static final String APPLICATION_DETAILS_SETTINGS = "android.settings.APPLICATION_DETAILS_SETTINGS";
 
 
 	private String			mCurrentListupApplicationType = null;
	private int				mCurrentListPosition = -1;
 
 
 	//--------------------------------------------------
 	// public functions
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// from Activity start, onStop
 		// to onStart
 		Log.v(TAG, "onCreate");
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.activity_main);
 
 		mCurrentListupApplicationType = SettingsActivity.getListupApplicationTypeSetting(this);
 
 		setListAdapter(getPackageListAdapter());
 		setListViewOnItemLongClickListener();
 	}
 
 
 	@Override
 	protected void onRestart() {
 		// from onStop
 		// to onStart
 		Log.v(TAG, "onRestart");
 		super.onRestart();
 	}
 
 
 	@Override
 	protected void onStart() {
 		// from onCreate, onRestart
 		// to onResume
 		Log.v(TAG, "onStart");
 		super.onStart();
 	}
 
 
 	@Override
 	protected void onResume() {
 		// from onStart, onPause
 		// to onPause
 		Log.v(TAG, "onResume");
 		super.onResume();
 
 		String setupApplicationType = SettingsActivity.getListupApplicationTypeSetting(this);
 		if (mCurrentListupApplicationType != setupApplicationType) {
 			mCurrentListupApplicationType = setupApplicationType;
 	        setListAdapter(getPackageListAdapter());
 		}
 	}
 
 
 	@Override
 	protected void onPause() {
 		// from onResume
 		// to onResume, onStop
 		Log.v(TAG, "onPause");
 		super.onPause();
 	}
 
 
 	@Override
 	protected void onStop() {
 		// from onPause
 		// to onDestroy, onRestart, onCreate
 		Log.v(TAG, "onStop");
 		super.onStop();
 	}
 
 
 	@Override
 	protected void onDestroy() {
 		// from onStop
 		// to Activity shut down
 		Log.v(TAG, "onDestroy");
 		super.onDestroy();
 	}
 
 
 	@Override
	protected Dialog onCreateDialog(int id) {
 		switch (id) {
 
 		case R.id.dialog_labelcolor:
 			return new AlertDialog.Builder(this)
             .setTitle(R.string.dialog_labelcolor_title)
             .setNegativeButton(android.R.string.cancel, null)
             .setCancelable(true)
             .setAdapter(getLabelColorListAdapter(), new OnClickListener() {
 				@Override
 				public void onClick(DialogInterface arg0, int arg1) {
					onDialogLabelColorSelected(arg1);
 				}
 			})
             .create();
 
 		default:
 			break;
 		}
 
 		return null;
 	}
 
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 	    super.onOptionsItemSelected(item);
 
 	    switch(item.getItemId()){
 
 	    case R.id.menu_settings:
 	    	doSettings();
 	        return true;
 	    }
 
 	    return false;
 	}
 
 
 	@Override
 	protected void onListItemClick(ListView l, View v, int position, long id) {
 		PackageInfo info = (PackageInfo)getListAdapter().getItem(position);
 		if (info != null) {
 			Uri uri = Uri.parse("package:" + info.packageName);
 			Intent intent = new Intent(APPLICATION_DETAILS_SETTINGS, uri);
 			startActivityForResult(intent, 0);
 		}
     }
 
 
 	//--------------------------------------------------
 	// private functions
 
 	private PackageListAdapter getPackageListAdapter() {
 		List<PackageInfo> list = null;
 
 		PackageModel model = AppDetailsSettingsApplication.getApplication().getPackageModel();
 		if (mCurrentListupApplicationType.equals("0")) {
 			list = model.getSystemPackages();
 		} else if (mCurrentListupApplicationType.equals("1")) {
 			list = model.getDownloadedPackages();
 		} else if (mCurrentListupApplicationType.equals("2")) {
 			list = model.getAllPackages();
 		}
 
 		return new PackageListAdapter(this, list);
 	}
 
 	private LabelColorListAdapter getLabelColorListAdapter() {
 		List<Integer> list = new ArrayList<Integer>();
 
 		int[] entries = getResources().getIntArray(R.array.labelcolor_index_entries);
 		for (int i = 0; i < entries.length; i++) {
 			list.add(entries[i]);
 		}
 
 		return new LabelColorListAdapter(this, list);
 	}
 
 
 	private void setListViewOnItemLongClickListener() {
 		getListView().setOnItemLongClickListener(new OnItemLongClickListener() {
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int position, long id) {
				mCurrentListPosition = position;
				showDialog(R.id.dialog_labelcolor);
 				return true;
 			}
 		});
 	}
 
 
	private void onDialogLabelColorSelected(int index) {
		if (mCurrentListPosition < 0 || mCurrentListPosition >= getListAdapter().getCount()) {
			return;
		}

		PackageInfo info = (PackageInfo)getListAdapter().getItem(mCurrentListPosition);
 		LabelColorModel lmodel = AppDetailsSettingsApplication.getApplication().getLabelColorModel();
 		lmodel.setLabelColor(info.packageName, index);
 
 		PackageListAdapter adapter = (PackageListAdapter)getListAdapter();
 		adapter.notifyDataSetChanged();

		mCurrentListPosition = -1;
 	}
 
 
 	private void doSettings() {
 		Intent intent = new Intent();
 		intent.setClassName(getPackageName(), getPackageName() + ".SettingsActivity");
 		startActivity(intent);
 	}
 }
