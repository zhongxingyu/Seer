 package ru.fit4life.app;
 
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.app.Activity;
 import android.support.v4.widget.SimpleCursorAdapter;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.view.View;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.FilterQueryProvider;
import android.widget.FrameLayout;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 
 public class ToolbarsManager {
 
     private TextView nameActivity;
     private Button buttonSearch;
     private Button cancelSearch;
     private EditText inputSearch;
     private InputMethodManager imm;
     private View functionalToolbar;
 
     public ToolbarsManager(Activity a) {
         this.imm = (InputMethodManager) a.getSystemService(
                 Context.INPUT_METHOD_SERVICE);
 
         nameActivity = (TextView) a.findViewById(R.id.navigation_toolbar_name);
         buttonSearch = (Button) a.findViewById(R.id.functional_toolbar_button_search);
         cancelSearch = (Button) a.findViewById(R.id.navigation_toolbar_button_cancel_search);
         inputSearch = (EditText) a.findViewById(R.id.navigation_toolbar_inputSearch);
        functionalToolbar = (FrameLayout) a.findViewById(R.id.functional_toolbar_button);
     }
 
 
     public void enableSearching(TextWatcher textWatcher) {
         inputSearch.addTextChangedListener(textWatcher);
     }
 
     public void startSearching() {
 
         //change activities visibility
         nameActivity.setVisibility(View.GONE);
         inputSearch.setVisibility(View.VISIBLE);
         buttonSearch.setVisibility(View.GONE);
         cancelSearch.setVisibility(View.VISIBLE);
 
         //change focus to search view and open keyboard
         imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
         inputSearch.requestFocus();
     }
 
     public void finishSearching() {
 
         //clear text & refresh list
         inputSearch.setText(null, TextView.BufferType.NORMAL);
 
         //hide keyboard
         imm.hideSoftInputFromWindow(inputSearch.getWindowToken(), 0);
 
         //change activities visibility
         nameActivity.setVisibility(View.VISIBLE);
         inputSearch.setVisibility(View.GONE);
         buttonSearch.setVisibility(View.VISIBLE);
         cancelSearch.setVisibility(View.GONE);
     }
 
     public void setNameActivity(String name) {
         this.nameActivity.setText(name);
     }
 
     public void setFunctionalToolbarVisible(int visibility) {
 
             functionalToolbar.setVisibility(visibility);
     }
 }
