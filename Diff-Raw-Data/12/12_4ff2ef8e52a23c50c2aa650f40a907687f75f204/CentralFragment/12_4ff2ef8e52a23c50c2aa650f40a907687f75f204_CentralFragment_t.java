 package com.group5.diceroller;
 
 import android.support.v4.app.Fragment;
 import android.app.Activity;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.os.Bundle;
 
 public class CentralFragment extends Fragment {
     OnDiceRolledListener rolled_listener;
     DiceRollerState state;
 
     @Override
     public void onAttach(Activity activity) {
         super.onAttach(activity);
 
         try {
             rolled_listener = (OnDiceRolledListener) activity;
         } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnDiceRolledListener");
         }
 
         try {
             state = (DiceRollerState) activity;
         } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement DiceRollerState");
         }
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
             Bundle savedInstanceState) {
         View layout = inflater.inflate(R.layout.central, container, false);
         Button roll_button = (Button) layout.findViewById(R.id.roll_button);
         roll_button.setOnClickListener(new RollEvent());
         return layout;
     }
 
     public class RollEvent implements View.OnClickListener {
         public void onClick(View v) {
             state.activeSelection().roll();
             rolled_listener.onDiceRolled();
         }
     }
 
 }
