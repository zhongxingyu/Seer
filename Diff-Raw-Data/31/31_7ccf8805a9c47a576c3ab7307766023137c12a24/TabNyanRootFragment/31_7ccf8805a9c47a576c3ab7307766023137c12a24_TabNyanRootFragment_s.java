 package com.zero.star.tabnyan;
 
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentTransaction;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
import android.widget.FrameLayout;
 
 public class TabNyanRootFragment extends Fragment {
 
    /** view id of fragment */
    private static final int VIEW_ID = 1;

 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return createView(container);
 	}
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 
         initFragment();
 	}
 
 	/**
	 * Create view.
	 * @param container parent view
	 * @return Created view
	 */
	private View createView(ViewGroup container) {
        FrameLayout v = new FrameLayout(getActivity());
        ViewGroup.LayoutParams params = container.getLayoutParams();
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        v.setLayoutParams(params);
        v.setId(VIEW_ID);

        return v;
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
        if (fragmentManager.findFragmentById(VIEW_ID) != null) {
             return;
         }
 
         FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
 
         Fragment fragment = Fragment.instantiate(getActivity(), rootClass, args);
        fragmentTransaction.replace(VIEW_ID, fragment);
 
         fragmentTransaction.commit();
 	}
 
 }
