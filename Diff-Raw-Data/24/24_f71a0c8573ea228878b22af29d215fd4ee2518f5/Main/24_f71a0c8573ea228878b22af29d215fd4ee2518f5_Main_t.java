 package com.avantics.savingscalc.common.activities;
 
import android.os.Build;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.view.View;
 import com.avantics.common.IBindManager;
 import com.avantics.common.UiBindingContainer;
 import com.avantics.savingscalc.common.R;
 import com.avantics.savingscalc.common.UiBindingManager;
 
 import java.util.ArrayList;
 
 /**
  * Created by tom on 02/06/13.
  */
 public class Main extends FragmentActivity implements IBindManager {
     private static UiBindingManager binder;
 
     public Main() {
         if (binder == null) {
             binder = new UiBindingManager();
         }
     }
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            setContentView(com.avantics.savingscalc.common.R.layout.standard_form);
        } else {
            setContentView(R.layout.main);
        }
     }
 
     @Override
     public ArrayList<UiBindingContainer> AttachToView(View view) {
         return binder.AttachToView(view);
     }
 }
