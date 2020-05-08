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
 
 
 /**
  * Activity which allows creation and saving of a new dice set.
  *
  * @Author Carlos Valera
  */
 public class SetCreatorActivity extends FragmentActivity
     implements AddDiceDialogFragment.AddDiceListener {
 
     EditText set_name;
     EditText modifier;
     Button save_button;
     Button cancel_button;
     List<Dice> dice;
     DiceListAdapter dice_adapter;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.creator);
         dice = new ArrayList<Dice>();
 
         set_name = (EditText)findViewById(R.id.name_text);
         modifier = (EditText)findViewById(R.id.modifier);
 
         save_button = (Button)findViewById(R.id.save_button);
         save_button.setOnClickListener(new SaveClickListener());
 
         cancel_button = (Button)findViewById(R.id.cancel_button);
         cancel_button.setOnClickListener(new CancelClickListener());
 
         Button delete_button = (Button)findViewById(R.id.delete_button);
         delete_button.setVisibility(View.GONE);
 
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
         Dice d = new Dice(num_faces, 1);
         dice.add(d);
         dice_adapter.notifyDataSetChanged();
     }
 
     class CancelClickListener implements View.OnClickListener {
         public void onClick(View v) {
             setResult(Activity.RESULT_CANCELED);
             finish();
         }
     }
 
     class SaveClickListener implements View.OnClickListener {
         public void onClick(View v) {
             String name = set_name.getText().toString();
             int modifier_val = AddDiceDialogFragment.getNumFromEditable(modifier.getText());
 
             boolean non_empty = false;
             DiceSet set = new DiceSet(name, modifier_val);
             for (Dice d : dice) {
                 if (d.count != 0) {
                     non_empty = true;
                     set.add(d);
                 }
             }
 
             if (non_empty) {
                 DiceRollerState.getState().diceSets().add(set);
                 set.save();
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
