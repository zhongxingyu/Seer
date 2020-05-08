 package com.tropicdreams.stayintouch;
 
 import com.tropicdreams.stayintouch.datasource.ConnectCareGroup;
 import com.tropicdreams.stayintouch.model.CareGroup;
 
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.LinearLayout;
 
 public class CareGroups extends Fragment {
 	ConnectCareGroup datasource;
 	private String LOGTAG= "message";
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
             Bundle savedInstanceState) {
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
         datasource= new ConnectCareGroup(getActivity());
         datasource.open();
         createData();
         return (LinearLayout)inflater.inflate(R.layout.care_groups_layout, container, false);
     }	
 	
 	@Override
 	public void onPause() {
 		super.onPause();
 		datasource.close();
 	}
 	
 	@Override
 	public void onResume() {
 		super.onResume();
 		datasource.open();
 	}
 	
 	private void createData(){
 		CareGroup cg= new CareGroup();
 		cg.Name="Daily";
 		cg.Date="Today";
 		cg = datasource.create(cg);
 		Log.i(LOGTAG, "Care group created with id "+ cg.Id);
 		
 		cg = new CareGroup();
 		cg.Name="Weekly";
 		cg.Date="Today";
 		cg = datasource.create(cg);
 		Log.i(LOGTAG, "Care group created with id "+ cg.Id);
 		
 		cg = new CareGroup();
 		cg.Name="Monthly";
 		cg.Date="Today";
 		cg = datasource.create(cg);
 		Log.i(LOGTAG, "Care group created with id "+ cg.Id);
 		
 		cg = new CareGroup();
 		cg.Name="Annually";
 		cg.Date="Today";
 		cg = datasource.create(cg);
 		Log.i(LOGTAG, "Care group created with id "+ cg.Id);
 		
 		cg = new CareGroup();
 		cg.Name="Tomiwa";
 		cg.Date="Today";
 		cg = datasource.create(cg);
 		Log.i(LOGTAG, "Care group created with id "+ cg.Id);
 	}
 }
