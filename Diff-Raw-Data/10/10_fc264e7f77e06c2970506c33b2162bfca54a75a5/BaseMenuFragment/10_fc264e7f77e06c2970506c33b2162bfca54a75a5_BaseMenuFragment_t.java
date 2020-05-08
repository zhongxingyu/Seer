 package com.quizz.core.fragments;
 
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentTransaction;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 
 import com.actionbarsherlock.internal.nineoldandroids.animation.AnimatorSet;
 import com.quizz.core.activities.BaseQuizzActivity;
 import com.quizz.core.interfaces.FragmentContainer;
 import com.quizz.core.utils.NavigationUtils;
 
 public class BaseMenuFragment extends Fragment {
 
    private boolean mMenuLocked = false;

     @Override
     public void onCreate(Bundle savedInstanceState) {
 	super.onCreate(savedInstanceState);
     }
 
     @Override
     public void onActivityCreated(Bundle savedInstanceState) {
 	if (getActivity() instanceof BaseQuizzActivity) {
 	    ((BaseQuizzActivity) getActivity()).setHideAbOnRotationChange(true);
 	}
 	super.onActivityCreated(savedInstanceState);
     }
 
     @Override
     public void onSaveInstanceState(Bundle outState) {
 	super.onSaveInstanceState(outState);
     }
 
     @Override
     public void onPause() {
 	super.onPause();
     }
 
     @Override
     public void onResume() {
 	super.onResume();
	mMenuLocked = false;
     }
 
     protected void initMenuButton(Button button, final Class<?> cls,
 	    final FragmentTransaction transaction) {
 	initMenuButton(button, cls, transaction, null);
     }
 
     protected void initMenuButton(Button button, final Class<?> cls,
 	    final FragmentTransaction transaction, final AnimatorSet animatorSet) {
 	button.setOnClickListener(new OnClickListener() {
 
 	    @Override
 	    public void onClick(View v) {
		if (mMenuLocked) {
		    return;
		}
 
 		if (getActivity() instanceof FragmentContainer) {
		    mMenuLocked = true;
 		    FragmentContainer container = (FragmentContainer) getActivity();
 		    NavigationUtils
 			    .animatedNavigationTo(cls, getActivity().getSupportFragmentManager(),
 				    container, true, transaction, animatorSet);
 		}
 	    }
 	});
     }
 }
