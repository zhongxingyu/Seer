 package com.jgaunt.Soundbox;
 
 import com.jgaunt.Soundbox.R;
 
 import android.app.Fragment;
 import android.os.Bundle;
//import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 
 public class SoundBoxLoginFragment extends Fragment {
 
     //private String mPath = "";
     
     public static SoundBoxLoginFragment newInstance() {
         SoundBoxLoginFragment loginFrag = new SoundBoxLoginFragment();
         return loginFrag;
     }
 
     @Override
     public View onCreateView(LayoutInflater aInflater,
                              ViewGroup aContainer, 
                              Bundle aSavedInstanceState) {
        super.onCreateView(aInflater, aContainer, aSavedInstanceState);
         return aInflater.inflate(R.layout.login_fragment, aContainer, false);
     }
 }
