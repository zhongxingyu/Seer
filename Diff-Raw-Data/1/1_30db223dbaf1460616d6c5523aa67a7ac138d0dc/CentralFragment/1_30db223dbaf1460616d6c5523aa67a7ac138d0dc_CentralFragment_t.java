 package com.group5.diceroller;
 
 import android.support.v4.app.Fragment;
 import android.app.Activity;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.TextView;
 import android.os.Bundle;
 
 public class CentralFragment extends Fragment {
     OnDiceRolledListener rolled_listener;
     DiceRollerState state;
     TextView selection_text;
     Button roll_button;
 
     @Override
     /**
      * Assures that the containing activity implements the OnDiceRolledListener
      * and DiceRollerState interfaces. Also calls the superclass onAttach to
      * correctly attach this fragment to the containing activity.
      *
      * @param activity The activity which this fragment is being attached to.
      */
     public void onAttach(Activity activity) {
         super.onAttach(activity);
 
         try {
             rolled_listener = (OnDiceRolledListener) activity;
         } catch (ClassCastException e) {
             throw new ClassCastException(activity.toString() + " must implement OnDiceRolledListener");
         }
 
         state = DiceRollerState.getState();
     }
 
     @Override
     /**
      * Creates the view for this fragment. It gets its layout from global
      * resources, and creates the onClick handler for the roll button.
      */
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
             Bundle savedInstanceState) {
         View layout = inflater.inflate(R.layout.central, container, false);
         selection_text = (TextView) layout.findViewById(R.id.selection_text);
 
         roll_button = (Button) layout.findViewById(R.id.roll_button);
         roll_button.setOnClickListener(new RollEvent());
         if (state.activeSelection().size() == 0)
             roll_button.setEnabled(false);
 
        updateSelectionText();
         return layout;
     }
 
     /**
      * Re-evaluates the dice in the active selection and sets the label for the
      * central view accordingly. If there are no dice in the selection, it
      * defaults to the global empty_selection string in the resources.
      */
     public void updateSelectionText() {
         String des = state.activeSelection().toString();
         if (des.length() > 0) {
             selection_text.setText(des);
             roll_button.setEnabled(true);
         }
         else {
             selection_text.setText(R.string.empty_selection);
             roll_button.setEnabled(false);
         }
     }
 
     /**
      * Listener for the roll button on the central view.
      */
     public class RollEvent implements View.OnClickListener {
         /**
          * Handles the roll button being clicked. It randomizes the values in
          * the set selection (by rolling them), and calls the callback
          * onDiceRolled method in the containing activity of this fragment.
          *
          * @param v The roll button.
          */
         public void onClick(View v) {
             rolled_listener.onDiceRolled();
         }
     }
 
 }
