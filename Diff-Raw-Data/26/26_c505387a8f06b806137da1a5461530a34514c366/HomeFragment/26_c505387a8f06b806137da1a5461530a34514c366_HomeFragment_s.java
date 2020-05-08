 package com.ecn.urbapp.fragments;
 
 import android.app.Fragment;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 
 import com.ecn.urbapp.R;
 
 /**
  * This is the fragment used to make the user choose between the differents type of project.
  * 
  * @author	COHENDET Sébastien
  * 			DAVID Nicolas
  * 			GUILBART Gabriel
  * 			PALOMINOS Sylvain
  * 			PARTY Jules
  * 			RAMBEAU Merwan
  * 			
  */
 
 public class HomeFragment extends Fragment{
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 	}
 	
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
 		View v = inflater.inflate(R.layout.layout_home, null);
 		return v;
 	}
 }
