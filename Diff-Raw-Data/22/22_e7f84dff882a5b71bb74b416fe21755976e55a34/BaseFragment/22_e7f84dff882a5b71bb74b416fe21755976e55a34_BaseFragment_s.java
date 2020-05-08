 package com.zipcar.testingprototype.shared;
 
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.ListFragment;
 
 public class BaseFragment extends Fragment {
     @Override public void onActivityCreated(Bundle savedInstanceState) {
         super.onActivityCreated(savedInstanceState);
        ((BaseActivity) getActivity()).inject(this);
     }
 }
