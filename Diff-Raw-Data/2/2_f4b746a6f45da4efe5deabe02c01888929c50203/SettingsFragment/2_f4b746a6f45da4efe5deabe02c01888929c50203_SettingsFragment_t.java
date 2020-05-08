 package com.hyperactivity.android_app.activities;
 
 import java.util.ArrayList;
 import java.util.Locale;
 
 import android.content.Context;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 
 import com.hyperactivity.android_app.R;
 import com.hyperactivity.android_app.core.Engine;
 import com.hyperactivity.android_app.forum.models.Category;
 import com.hyperactivity.android_app.forum.models.Thread;
 
 public class SettingsFragment extends Fragment {
 
     ThreadListFragment searchResultList;
     TextView noResultsText;
     EditText searchEditText;
 
     public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
 
         // THIS WILL BE REMOVED, IT'S HERE SO THAT THE PAGE WILL NOT BE EMPTY
        View view = inflater.inflate(R.layout.settings_fragment, null);
         searchResultList = new ThreadListFragment();
         searchResultList.updateThreadList(new ArrayList<Thread>());
 
 
         return view;
     }
 
 
 
 
 }
