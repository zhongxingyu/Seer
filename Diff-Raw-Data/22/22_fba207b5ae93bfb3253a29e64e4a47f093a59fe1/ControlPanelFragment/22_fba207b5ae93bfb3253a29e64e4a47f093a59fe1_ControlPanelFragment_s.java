 package com.uruwolf.vominer.fragments;
 
 import com.uruwolf.vominer.R;
 
 import android.app.Fragment;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.Spinner;
 
 public class ControlPanelFragment extends Fragment {
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState){
 		super.onCreate(savedInstanceState);
 	}
 	
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
 		
 		View view = inflater.inflate(R.layout.sector_select, container, false);
 		
		Spinner spinner = (Spinner) view.findViewById(R.id.systemList);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                 getActivity(), R.array.system_list, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
 		
 		return view;
 	}
 	
 	@Override
 	public void onPause(){
 		super.onPause();
 	}
 }
