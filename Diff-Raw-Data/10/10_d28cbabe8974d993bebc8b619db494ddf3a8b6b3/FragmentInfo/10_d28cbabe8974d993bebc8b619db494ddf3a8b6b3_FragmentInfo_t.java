 package br.com.androidzin.launchablesitens;
 
 import java.util.List;
 
import android.app.Activity;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentTransaction;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 
 import com.actionbarsherlock.app.ActionBar.Tab;
 import com.actionbarsherlock.app.ActionBar.TabListener;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.app.SherlockListFragment;
 
 public abstract class FragmentInfo extends SherlockListFragment implements TabListener {
 
 	private Fragment mFragment;
 	private static final String TAG = "Tab";
 	private List<LaunchableItem> data;
 	
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		if(data == null) data = getSampleData();
 		ArrayAdapter<LaunchableItem> adapter = new LaunchableItemAdapter(getActivity(), R.layout.item, this.data, (OnItemCheckedListener) getActivity());
 		setListAdapter(adapter);
 		return super.onCreateView(inflater, container, savedInstanceState);
 	}
 	
 	protected abstract List<LaunchableItem> getSampleData();
 	
 	@Override
 	public void onTabReselected(Tab tab, FragmentTransaction ft) {
 		Log.d(TAG, "OnTabReselected");
 		
 	}
 
 	@Override
 	public void onTabSelected(Tab tab, FragmentTransaction ft) {
 		Log.d(TAG, "OnTabSelected");
 		if (mFragment == null) {
 			try {
 				mFragment = this.getClass().newInstance();
 			} catch (java.lang.InstantiationException e) {
 				e.printStackTrace();
 			} catch (IllegalAccessException e) {
 				e.printStackTrace();
 			}
			ft.replace(android.R.id.content, mFragment);
         } else {
             ft.attach(mFragment);
         }
       
 	}
 
 	@Override
 	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
 		Log.d(TAG, "OnTabUnselected");
 		if(mFragment != null){
 			ft.detach(mFragment);
 		}
 	}
 
 	public List<LaunchableItem> getData() {
 		return data;
 	}
 
 	public void setData(List<LaunchableItem> data) {
 		this.data = data;
 	}
 	
 	
 	
 }
