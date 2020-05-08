 package com.example.educationalapp;
 
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
import android.view.View;
 import android.view.ViewGroup;
 
 /**
  * A simple {@link android.support.v4.app.Fragment} subclass.
  * 
  */
 public class VideosTab extends Fragment {
 
 	public VideosTab() {
 		// Required empty public constructor
 	}
 
	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		// Inflate the layout for this fragment
 		return inflater.inflate(R.layout.videos_tab_fragment, container, false);
 		
 		
 	}
 
 }
