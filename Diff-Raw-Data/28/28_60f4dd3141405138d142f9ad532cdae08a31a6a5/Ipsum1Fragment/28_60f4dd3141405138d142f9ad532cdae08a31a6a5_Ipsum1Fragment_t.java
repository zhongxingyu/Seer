 package com.example.fun_with_fragments;
 
 import android.app.Activity;
 import android.app.Fragment;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Button;
 
 public class Ipsum1Fragment extends Fragment {
     public static interface Listener{
         public void onIpsumDone();
     }
 
     Listener listener;
 
     @Override
     public void onAttach(Activity activity) {
         super.onAttach(activity);
         listener  = (Listener) activity;
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         View view = inflater.inflate(R.layout.ipsum_1_fragment, container, false);
         Button doneButton = (Button)view.findViewById(R.id.doneButton);
         doneButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 listener.onIpsumDone();
             }
         });
         return view;
     }
 }
