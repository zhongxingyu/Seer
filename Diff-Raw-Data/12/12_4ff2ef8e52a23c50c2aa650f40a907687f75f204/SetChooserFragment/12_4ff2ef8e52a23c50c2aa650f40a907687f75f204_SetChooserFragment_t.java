 package com.group5.diceroller;
 
 import java.util.List;
 import android.support.v4.app.ListFragment;
 import android.app.Activity;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ToggleButton;
 import android.widget.Button;
 import android.widget.ArrayAdapter;
 import android.os.Bundle;
 
 public class SetChooserFragment extends ListFragment {
     DiceRollerState state;
 
     @Override
     public void onActivityCreated(Bundle savedInstanceState) {
         super.onActivityCreated(savedInstanceState);
         DiceSelectionAdapter adapter = new DiceSelectionAdapter();
         setListAdapter(adapter);
     }
 
     @Override
     public void onAttach(Activity activity) {
         super.onAttach(activity);
 
         try {
             state = (DiceRollerState) activity;
         } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement DiceRollerState");
         }
     }
 
     public class DiceSelectionAdapter extends ArrayAdapter<DiceSet> {
 
         public DiceSelectionAdapter() {
             super(getActivity(), R.layout.chooser_row, state.diceSets());
         }
 
         @Override
         public View getView(int position, View convertView, ViewGroup parent) {
             View row;
 
             // Last item in the list is the "add a set button"
             if (position == state.diceSets().size()-1) {
                 row = new Button(getActivity());
                 ((Button) row).setText("Add a Set");
             } else {
                 LayoutInflater inflater =  getActivity().getLayoutInflater();
                 row = inflater.inflate(R.layout.chooser_row, parent, false);
 
                 ToggleButton main_description = (ToggleButton) row.findViewById(R.id.main_description);
                 main_description.setText(state.diceSets().get(position).name());
                 main_description.setTextOn(state.diceSets().get(position).name());
                 main_description.setTextOff(state.diceSets().get(position).name());
 
                 main_description.setOnClickListener(new ToggleOnClickListener(position));
             }
             return row;
         }
     }
 
     public class ToggleOnClickListener implements View.OnClickListener {
         DiceSet described_set;
 
         public ToggleOnClickListener(int position) {
             described_set = state.diceSets().get(position);
         }
 
         public void onClick(View v) {
             ToggleButton btn = (ToggleButton) v;
             if (btn.isChecked()) {
                 state.activeSelection().add(described_set);
             } else {
                 state.activeSelection().remove(described_set.id);
             }
         }
     }
 }
