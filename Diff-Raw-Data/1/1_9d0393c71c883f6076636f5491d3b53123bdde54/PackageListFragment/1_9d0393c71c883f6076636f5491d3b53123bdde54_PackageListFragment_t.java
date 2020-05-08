 package net.hogelab.android.AppDetailsSettings.Fragment;
 
 import java.util.List;
 
 import net.hogelab.android.AppDetailsSettings.AppDetailsSettingsApplication;
 import net.hogelab.android.AppDetailsSettings.ApplicationSettings;
 import net.hogelab.android.AppDetailsSettings.Dialog.LabelColorDialog;
 import net.hogelab.android.AppDetailsSettings.ListAdapter.PackageListAdapter;
 import net.hogelab.android.AppDetailsSettings.Model.LabelColorModel;
 import net.hogelab.android.AppDetailsSettings.Model.PackageModel;
 
 import android.app.Activity;
 import android.app.ListFragment;
 import android.content.Intent;
 import android.content.pm.PackageInfo;
 import android.net.Uri;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.ListView;
 import android.widget.AdapterView.OnItemLongClickListener;
 
 
 //--------------------------------------------------
 // class MainActivity
 
 public class PackageListFragment extends ListFragment implements LabelColorDialog.LabelColorDialogOnClickListener {
 
 	private static final String TAG = PackageListFragment.class.getSimpleName();
 
 	private static final String APPLICATION_DETAILS_SETTINGS = "android.settings.APPLICATION_DETAILS_SETTINGS";
 
 
 	private String			mCurrentListingApplicationType = null;
 	private int				mCurrentListPosition = -1;
 
 
 	@Override
 	public void onAttach(Activity activity) {
 		// from Fragment is added
 		// to onCreate
 		Log.v(TAG, "onAttach");
 		super.onAttach(activity);
 	}
 
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		// from onAttach
 		// to onCreateView
 		Log.v(TAG, "onCreate");
 		super.onCreate(savedInstanceState);
 	}
 
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		// from onCreate, onDestroyView
 		// to onActivityCreated
 		Log.v(TAG, "onCreateView");
 		return super.onCreateView(inflater, container, savedInstanceState);
 	}
 
 
 	@Override
     public void onActivityCreated(Bundle savedInstanceState) {
 		// from onCreateView
 		// to onStart
 		Log.v(TAG, "onActivityCreated");
         super.onActivityCreated(savedInstanceState);
 
 		mCurrentListingApplicationType = ApplicationSettings.getListingApplicationTypeSetting();
 
 		setListAdapter(getPackageListAdapter());
 		setListViewOnItemLongClickListener();
     }
 
 
 	@Override
 	public void onStart() {
 		// from onActivityCreated
 		// to onResume
 		Log.v(TAG, "onStart");
 		super.onStart();
 	}
 
 
 	@Override
 	public void onResume() {
 		// from onStart
 		// to Fragment is active
 		super.onResume();
 
 		String setupApplicationType = ApplicationSettings.getListingApplicationTypeSetting();
 		if (mCurrentListingApplicationType != setupApplicationType) {
 			mCurrentListingApplicationType = setupApplicationType;
 	        setListAdapter(getPackageListAdapter());
 		}
 	}
 
 
 	@Override
 	public void onPause() {
 		// from Fragment is active
 		// to onStop
 		super.onPause();
 	}
 
 
 	@Override
 	public void onStop() {
 		// from onPause
 		// to onDestroyView
 		super.onStop();
 	}
 
 
 	@Override
 	public void onDestroyView() {
 		// from onStop
 		// to onDestroy
 		super.onDestroyView();
 	}
 
 
 	@Override
 	public void onDestroy() {
 		// from onDestroyView
 		// to onDetach
 		super.onDestroy();
 	}
 
 
 	@Override
 	public void onDetach() {
 		// from onDestroy
 		// to Fragment is destroyed
		super.onDetach();
 	}
 
 
 	@Override
 	public void onListItemClick(ListView l, View v, int position, long id) {
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
 		if (mCurrentListingApplicationType.equals("0")) {
 			list = model.getSystemPackages();
 		} else if (mCurrentListingApplicationType.equals("1")) {
 			list = model.getDownloadedPackages();
 		} else if (mCurrentListingApplicationType.equals("2")) {
 			list = model.getAllPackages();
 		}
 
 		return new PackageListAdapter(getActivity(), list);
 	}
 
 
 	private void setListViewOnItemLongClickListener() {
 		getListView().setOnItemLongClickListener(new OnItemLongClickListener() {
 			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int position, long id) {
 				mCurrentListPosition = position;
 
 				LabelColorDialog dialog = LabelColorDialog.newInstance(PackageListFragment.this);
                 dialog.show(getFragmentManager(), "dialog");
 
                 return true;
 			}
 		});
 	}
 
 
 
 	@Override
 	public void onClickLabelColorDialogList(int position) {
 		if (mCurrentListPosition < 0 || mCurrentListPosition >= getListAdapter().getCount()) {
 			return;
 		}
 
 		PackageInfo info = (PackageInfo)getListAdapter().getItem(mCurrentListPosition);
 		LabelColorModel lmodel = AppDetailsSettingsApplication.getApplication().getLabelColorModel();
 		lmodel.setLabelColor(info.packageName, position);
 
 		PackageListAdapter adapter = (PackageListAdapter)getListAdapter();
 		adapter.notifyDataSetChanged();
 
 		mCurrentListPosition = -1;
 	}
 }
