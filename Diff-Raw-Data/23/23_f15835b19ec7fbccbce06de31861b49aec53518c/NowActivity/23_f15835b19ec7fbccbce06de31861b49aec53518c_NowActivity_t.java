 package com.ucb.dcm;
 
 import android.app.Activity;
 import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
 import com.actionbarsherlock.app.SherlockFragment;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.ucb.dcm.data.DataService;
import com.ucb.dcm.data.Venue;

import java.util.ArrayList;
 
 /**
  * Created by kurtguenther on 6/5/13.
  */
 public class NowActivity extends SherlockFragment {
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_now, container, false);
     }
 }
