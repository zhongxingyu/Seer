 package com.component.tab;
 
import java.util.List;

 import android.os.Bundle;
 import android.support.v4.app.FragmentTransaction;
import android.util.Log;
 import android.view.View;
 import android.widget.ArrayAdapter;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.ActionBar.Tab;
 import com.actionbarsherlock.app.SherlockListFragment;
import com.music.model.MediaFile;
import com.music.objectmapping.Datasource_mediafile;
 
 public class FoldersFragment extends SherlockListFragment implements ActionBar.TabListener {
 	
 	String folders[] = new String[]{
 	        "Nhip Cau Tri Am",
 	        "Xuan Chien Truong",
 	        "De Lai Cho Doi", 
 	};
 	
	private Datasource_mediafile transfer_file;
	protected String TAG = "FoldersFragment";
	
 	@Override
 	public void onViewCreated(View view, Bundle savedInstanceState) {
 		 /** Creating array adapter to set data in listview */
         ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity().getBaseContext(), android.R.layout.simple_list_item_multiple_choice, folders);
  
         /** Setting the array adapter to the listview */
         setListAdapter(adapter);
        
        transfer_file = new Datasource_mediafile();
        List<MediaFile> b =  transfer_file.getAllMediaFromSpecifiedFolders();
        
        Log.i(TAG,"Toi khong biet chuyen");
        
 		super.onViewCreated(view, savedInstanceState);
 	}
 
 	@Override
 	public void onTabSelected(Tab tab, FragmentTransaction ft) {
 		// TODO Auto-generated method stub
 		ft.add(android.R.id.content, this,"Albums");
         ft.attach(this);
 	}
 
 	@Override
 	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
 		// TODO Auto-generated method stub
 		ft.detach(this);
 	}
 
 	@Override
 	public void onTabReselected(Tab tab, FragmentTransaction ft) {
 		// TODO Auto-generated method stub
 		
 	} 
 
 }
