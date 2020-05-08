 package com.zero.star.tabnyan;
 
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentTransaction;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 
 public class TabNyanRootFragment extends Fragment {
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_tabnyan_root, container, false);
 	}
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 
         initFragment();
 	}
 
 	/**
 	 * Pop fragment backstack.
 	 * @return return true if exists backstack
 	 */
 	public boolean popBackStack() {
 		return getChildFragmentManager().popBackStackImmediate();
 	}
 
 	/**
 	 * Initialize Fragment.
 	 */
 	private void initFragment() {
 
 		Bundle args = getArguments();
 		String rootClass = args.getString(TabNyanActivity.ROOT_FRAGMENT_ARGS);
 
 		FragmentManager fragmentManager = getChildFragmentManager();
        if (fragmentManager.findFragmentById(R.id.fragment) != null) {
             return;
         }
 
         FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
 
         Fragment fragment = Fragment.instantiate(getActivity(), rootClass, args);
        fragmentTransaction.replace(R.id.fragment, fragment);
 
         fragmentTransaction.commit();
 	}
 
 }
