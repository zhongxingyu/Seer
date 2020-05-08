 package com.example.msims.keypad;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentTransaction;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 
public class HostFragment extends Fragment{
 
     @Override
     public void onAttach(final Activity activity) {
         super.onAttach(activity);
         Log.e("flubber", "I was attached");
     }
 
     @Override
     public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
         View layout = inflater.inflate(R.layout.host_fragment, container, false);
 
         layout.findViewById(R.id.warpMeToHalifax).setOnClickListener(new View.OnClickListener() {
             public void onClick(final View view) {
                 PinKeypadFragment fragment = new PinKeypadFragment();
 
                 Bundle bundle = new Bundle();
                 bundle.putString(PinKeypadFragment.Arguments.pinToMatchAgainst.toString(), "8675");
                 fragment.setArguments(bundle);
 
                 FragmentTransaction ft = getFragmentManager().beginTransaction();
                 ft.addToBackStack(null);
                 fragment.show(ft, "dialog");
             }
         });
         return layout;
     }
 }
