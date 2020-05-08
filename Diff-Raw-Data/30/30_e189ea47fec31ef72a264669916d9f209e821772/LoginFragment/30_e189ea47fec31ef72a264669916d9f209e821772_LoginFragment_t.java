 package com.randy.fbsample.fragments;
 
 
 
 import java.util.Arrays;
 
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 
 import com.facebook.widget.LoginButton;
 import com.randy.fbsample.R;
 
 public class LoginFragment extends Fragment {
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
 		View loginFragmentView = inflater.inflate(R.layout.fragment_login, container, false);
 		
 		return loginFragmentView;
 	}
 	
 	
 
 }
