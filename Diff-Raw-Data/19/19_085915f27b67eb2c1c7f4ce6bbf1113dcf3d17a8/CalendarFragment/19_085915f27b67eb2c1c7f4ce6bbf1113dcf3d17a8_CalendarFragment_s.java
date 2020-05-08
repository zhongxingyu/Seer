 package com.photographers_assistant;
 
import com.umd.cmsc420.photographersassistant.R;
import com.umd.cmsc420.photographersassistant.R.layout;

 import android.app.Fragment;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 
 public class CalendarFragment extends Fragment {
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		return inflater.inflate(R.layout.calendar_fragment, container, false);
 	}
 }
