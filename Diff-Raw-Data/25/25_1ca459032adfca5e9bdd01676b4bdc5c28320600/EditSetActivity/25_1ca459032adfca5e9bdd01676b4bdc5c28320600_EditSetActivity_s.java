 package com.group5.diceroller;
 
 import java.util.List;
 import java.util.ArrayList;
 import java.util.HashMap;
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Context;
 import android.support.v4.app.FragmentActivity;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.LayoutInflater;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.ArrayAdapter;
 import android.widget.TextView;
 import android.widget.EditText;
 import android.text.TextWatcher;
 import android.text.Editable;
 import android.util.Log;
 
 /**
  * Activity which allows the editing of an existing dice set.
  *
  * @Author Carlos Valera
  */
 public class EditSetActivity extends FragmentActivity
     implements AddDiceDialogFragment.AddDiceListener {
 
     public static final String kTag = "EditSetActivity";
     EditText set_name;
     EditText modifier;
     Button save_button;
     Button cancel_button;
     Button delete_button;
     List<Dice> dice;
     DiceSet set;
     int set_position;
     DiceListAdapter dice_adapter;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.creator);
         dice = new ArrayList<Dice>();
 
         set_position = getIntent().getIntExtra(SetChooserFragment.kIntentItemPosition, -1);
         Log.i(kTag, "Position: " + set_position);
         if (set_position == -1)
             finish();
 
         set = DiceRollerState.getState().diceSets().get(set_position);
 
         set_name = (EditText)findViewById(R.id.name_text);
         set_name.setText(set.name());
 
         modifier = (EditText)findViewById(R.id.modifier);
         modifier.setText("" + set.modifier);
 
         for (Dice d : set)
             dice.add(d);
 
         save_button = (Button)findViewById(R.id.save_button);
         save_button.setOnClickListener(new SaveClickListener());
 
         cancel_button = (Button)findViewById(R.id.cancel_button);
         cancel_button.setOnClickListener(new CancelClickListener());
 
         delete_button = (Button)findViewById(R.id.delete_button);
         delete_button.setOnClickListener(new DeleteClickListener());
 
         Button add_dice = new Button(this);
         add_dice.setText("add");
         add_dice.setOnClickListener(new AddDiceClickListener());
 
         ListView dice_list_view = (ListView) findViewById(R.id.dice_list);
         dice_list_view.addFooterView(add_dice, null, true);
 
         dice_adapter = new DiceListAdapter(this, dice);
         dice_list_view.setAdapter(dice_adapter);
     }
 
     public void addDice(int num_faces) {
         if (num_faces <= 1)
             return;
         for (Dice d : dice) {
             if (d.faces == num_faces)
                 return;
         }
         Dice d = new Dice(num_faces, 1, set.id);
         dice.add(d);
         dice_adapter.notifyDataSetChanged();
     }
 
     class CancelClickListener implements View.OnClickListener {
         public void onClick(View v) {
             setResult(Activity.RESULT_CANCELED);
             finish();
         }
     }
 
     /**
      * Listener for the delete button on the edit page. Deletes the set from
      * the DB, removes it from the dice sets in the global state, and removes
      * it from the user's active selection if needed.
      */
     class DeleteClickListener implements View.OnClickListener {
         public void onClick(View v) {
             DiceRollerState.getState().diceSets().remove(set_position);
             DiceRollerState.getState().activeSelection().remove(set);
             set.delete();
             setResult(Activity.RESULT_OK);
             finish();
         }
     }
 
     class SaveClickListener implements View.OnClickListener {
         public void onClick(View v) {
             String name = set_name.getText().toString();
             int modifier_val = AddDiceDialogFragment.getNumFromEditable(modifier.getText());
 
             boolean contained = false;
             if (DiceRollerState.getState().activeSelection().remove(set))
                 contained = true;
 
             boolean non_empty = false;
             set.name = name;
             set.modifier = modifier_val;
             set.clear();
             for (Dice d : dice) {
                 if (d.count != 0) {
                     non_empty = true;
                     set.add(d);
                 }
             }
 
             if (non_empty) {
                 set.save();
                 if (contained)
                     DiceRollerState.getState().activeSelection().add(set);
                 setResult(Activity.RESULT_OK);
             } else {
                 setResult(Activity.RESULT_CANCELED);
             }
 
             finish();
         }
     }
 
     class AddDiceClickListener implements View.OnClickListener {
         public void onClick(View v) {
             AddDiceDialogFragment dialog = new AddDiceDialogFragment();
             dialog.show(getSupportFragmentManager(), "AddDiceDialog");
         }
     }
 
 }
