 package com.aonghusflynn.salesforce.eventforce;
 
import android.app.Fragment;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 
 public class ListFragment extends Fragment {
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
         // Inflate the layout for this fragment
        return inflater.inflate(R.layout.event_list_fragment, container, false);
     }
 }
