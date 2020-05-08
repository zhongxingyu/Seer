 
 package com.egloos.realmove.android.fp.common;
 
 import com.actionbarsherlock.app.SherlockDialogFragment;
 import com.actionbarsherlock.app.SherlockFragment;
 
 import android.app.Activity;
 import android.app.Dialog;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 
 public class BaseFragment extends SherlockDialogFragment {
     protected Activity mContext = null;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         mContext = getActivity();
         FpLog.d(this.getClass().getSimpleName(), "onCreate() called");
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
             Bundle savedInstanceState) {
         FpLog.d(this.getClass().getSimpleName(), "onCreateView() called");
         return super.onCreateView(inflater, container, savedInstanceState);
     }
 
     @Override
     public Dialog onCreateDialog(Bundle savedInstanceState) {
         FpLog.d(this.getClass().getSimpleName(), "onCreateDialog() called");
         return super.onCreateDialog(savedInstanceState);
     }
 
     public void onActivityCreated(Bundle savedInstanceState) {
         super.onActivityCreated(savedInstanceState);
         FpLog.d(this.getClass().getSimpleName(), "onActivityCreated() called");
     }
 
     protected void finishActivity() {
         mContext.finish();
     }
 
     protected void finishMe() {
         getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
     }
 
     @Override
     public void onDetach() {
         super.onDetach();
         FpLog.d(this.getClass().getSimpleName(), "onDetach() called");
     }
 
     @Override
     public void onDestroy() {
         super.onDestroy();
         mContext = null;
         FpLog.d(this.getClass().getSimpleName(), "onDestroy() called");
     }
 
     @Override
     public void onDestroyView() {
         super.onDestroyView();
         FpLog.d(this.getClass().getSimpleName(), "onDestroyView() called");
     }
 
    @Override
    public void onPause() {
        FpLog.d(this.getClass().getSimpleName(), "onPause() called");
        super.onPause();
    }

    @Override
    public void onResume() {
        FpLog.d(this.getClass().getSimpleName(), "onResume() called");
        super.onResume();
    }

 }
