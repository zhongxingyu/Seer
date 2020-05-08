 package com.boomui.ddcharactersheet;
 
 import java.util.ArrayList;
 
 import android.app.*;
 import android.os.Bundle;
 import android.view.*;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class DiceRollerTabFragment extends Fragment {
 
 	View mainView;
 	TextView inputField;
 	ListView historyListView;
 	ArrayAdapter<String> historyListAdapter;
 	ArrayList<String> historyList;
 	// TextView history;
 
 	DiceRollSequence diceRolls = new DiceRollSequence();
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 	}
 
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		mainView = inflater.inflate(R.layout.dice_roller_tab_fragment_layout,
 				container, false);
 
 		inputField = (TextView) mainView.findViewById(R.id.input_viewer);
 
 		historyList = new ArrayList<String>();
 
 		historyListAdapter = new ArrayAdapter<String>(getActivity()
 				.getApplicationContext(), android.R.layout.simple_list_item_1,
 				historyList);
 
 		historyListView = (ListView) mainView
 				.findViewById(R.id.dice_roll_history);
 		historyListView.setAdapter(historyListAdapter);
 
 		View.OnClickListener onClickListener = new View.OnClickListener() {
 
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				switch (v.getId()) {
 				case R.id.mult0_button:
 					diceRolls.addMultiplier(0);
 					break;
 				case R.id.mult1_button:
 					diceRolls.addMultiplier(1);
 					break;
 				case R.id.mult2_button:
 					diceRolls.addMultiplier(2);
 					break;
 				case R.id.mult3_button:
 					diceRolls.addMultiplier(3);
 					break;
 				case R.id.mult4_button:
 					diceRolls.addMultiplier(4);
 					break;
 				case R.id.mult5_button:
 					diceRolls.addMultiplier(5);
 					break;
 				case R.id.mult6_button:
 					diceRolls.addMultiplier(6);
 					break;
 				case R.id.mult7_button:
 					diceRolls.addMultiplier(7);
 					break;
 				case R.id.mult8_button:
 					diceRolls.addMultiplier(8);
 					break;
 				case R.id.mult9_button:
 					diceRolls.addMultiplier(9);
 					break;
 
 				case R.id.d2_button:
 					diceRolls.addDiceRoll(2);
 					break;
 				case R.id.d4_button:
 					diceRolls.addDiceRoll(4);
 					break;
 				case R.id.d6_button:
 					diceRolls.addDiceRoll(6);
 					break;
 				case R.id.d8_button:
 					diceRolls.addDiceRoll(8);
 					break;
 				case R.id.d10_button:
 					diceRolls.addDiceRoll(10);
 					break;
 				case R.id.d12_button:
 					diceRolls.addDiceRoll(12);
 					break;
 				case R.id.d20_button:
 					diceRolls.addDiceRoll(20);
 					break;
 				case R.id.d100_button:
 					diceRolls.addDiceRoll(100);
 					break;
 				case R.id.dX_button:
 					diceRolls.addDiceRoll(0);
 					break;
 				case R.id.dConstant_button:
 					diceRolls.addDiceRoll(0);
 					break;
 				case R.id.dice_roller_clear_button:
 					diceRolls.clearSequence();
 					break;
 				case R.id.dice_roller_done_button:
 					String seq = diceRolls.sequenceDone();
 					historyList.add(seq);
 					historyListAdapter.notifyDataSetChanged();
 
 					break;
 				default:
 					break;
 				}
 				inputField.setText(diceRolls.diceToString());
 			}
 		};
 
 		Button mult0_button = (Button) mainView.findViewById(R.id.mult0_button);
 		mult0_button.setOnClickListener(onClickListener);
 		Button mult1_button = (Button) mainView.findViewById(R.id.mult1_button);
 		mult1_button.setOnClickListener(onClickListener);
 		Button mult2_button = (Button) mainView.findViewById(R.id.mult2_button);
 		mult2_button.setOnClickListener(onClickListener);
 		Button mult3_button = (Button) mainView.findViewById(R.id.mult3_button);
 		mult3_button.setOnClickListener(onClickListener);
 		Button mult4_button = (Button) mainView.findViewById(R.id.mult4_button);
 		mult4_button.setOnClickListener(onClickListener);
 		Button mult5_button = (Button) mainView.findViewById(R.id.mult5_button);
 		mult5_button.setOnClickListener(onClickListener);
 		Button mult6_button = (Button) mainView.findViewById(R.id.mult6_button);
 		mult6_button.setOnClickListener(onClickListener);
 		Button mult7_button = (Button) mainView.findViewById(R.id.mult7_button);
 		mult7_button.setOnClickListener(onClickListener);
 		Button mult8_button = (Button) mainView.findViewById(R.id.mult8_button);
 		mult8_button.setOnClickListener(onClickListener);
 		Button mult9_button = (Button) mainView.findViewById(R.id.mult9_button);
 		mult9_button.setOnClickListener(onClickListener);
 
 		Button d2_button = (Button) mainView.findViewById(R.id.d2_button);
 		d2_button.setOnClickListener(onClickListener);
 		Button d4_button = (Button) mainView.findViewById(R.id.d4_button);
 		d4_button.setOnClickListener(onClickListener);
 		Button d6_button = (Button) mainView.findViewById(R.id.d6_button);
 		d6_button.setOnClickListener(onClickListener);
 		Button d8_button = (Button) mainView.findViewById(R.id.d8_button);
 		d8_button.setOnClickListener(onClickListener);
 		Button d10_button = (Button) mainView.findViewById(R.id.d10_button);
 		d10_button.setOnClickListener(onClickListener);
 		Button d12_button = (Button) mainView.findViewById(R.id.d12_button);
 		d12_button.setOnClickListener(onClickListener);
 		Button d20_button = (Button) mainView.findViewById(R.id.d20_button);
 		d20_button.setOnClickListener(onClickListener);
 		Button d100_button = (Button) mainView.findViewById(R.id.d100_button);
 		d100_button.setOnClickListener(onClickListener);
 		Button dX_button = (Button) mainView.findViewById(R.id.dX_button);
 		dX_button.setOnClickListener(onClickListener);
 		Button dConstant_button = (Button) mainView
 				.findViewById(R.id.dConstant_button);
 		dConstant_button.setOnClickListener(onClickListener);
 
 		Button doneButton = (Button) mainView
 				.findViewById(R.id.dice_roller_done_button);
 		doneButton.setOnClickListener(onClickListener);
 		Button clearButton = (Button) mainView
 				.findViewById(R.id.dice_roller_clear_button);
 		clearButton.setOnClickListener(onClickListener);
 
 		return mainView;
 	}
 
 }
