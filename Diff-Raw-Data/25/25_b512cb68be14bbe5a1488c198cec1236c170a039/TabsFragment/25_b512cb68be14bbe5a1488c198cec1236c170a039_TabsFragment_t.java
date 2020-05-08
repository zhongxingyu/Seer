 package com.bigtheta.ragedice;
 
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentTabHost;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 
 public class TabsFragment extends Fragment {
 	private FragmentTabHost mTabHost;
 	
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		mTabHost = new FragmentTabHost(getActivity());
		mTabHost.setup(getActivity(), getFragmentManager(), R.id.tabs_content_container);
 		
 		mTabHost.addTab(mTabHost.newTabSpec("ksdf").setIndicator("KSD"), KSDescriptionFragment.class, null);
 		mTabHost.addTab(mTabHost.newTabSpec("glf").setIndicator("GLF"), GameLogFragment.class, null);
 		return mTabHost;
 	}
 
 }
 
 
