 package com.teamluper.luper;
 
 import com.googlecode.androidannotations.annotations.EFragment;
 
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
import android.widget.LinearLayout;
 
 public class TabProjectsFragment extends Fragment {
   @Override
   public View onCreateView(LayoutInflater infl, ViewGroup vg, Bundle state) {
     if(vg == null) return null;
    return (LinearLayout)infl.inflate(R.layout.tab_projects_layout, vg, false);
   }
 }
