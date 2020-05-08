 package com.innutrac.poly.innutrac;
 
 import com.innutrac.poly.innutrac.database.ProfileDatabase;
 import com.innutrac.poly.innutrac.database.User;
 
 import android.os.Bundle;
 import android.app.Fragment;
 import android.app.FragmentManager;
 import android.content.Intent;
 import android.view.*;
 import android.view.View.OnClickListener;
 import android.widget.*;
 
 public class ExerciseFragment extends Fragment {
 	ImageButton cardio, resistance;
 	RadioButton mod, vig;
 	Button submit;
 	EditText minute;
 	ProfileDatabase pdb;
 	boolean cardioSelected = false, resistanceSelected = false;
 	double calBurned = 0;
 
 	/**
 	 * Very basic implementation of calories burned for exercising.
 	 */
 
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		return inflater.inflate(R.layout.fragment_exercise, null);
 	}
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 
 		cardio = (ImageButton) getView().findViewById(R.id.cardio);
 		resistance = (ImageButton) getView().findViewById(R.id.resistance);
 		submit = (Button) getView().findViewById(R.id.exer_submit);
 		minute = (EditText) getView().findViewById(R.id.minET);
 		mod = (RadioButton) getView().findViewById(R.id.modRB);
 		vig = (RadioButton) getView().findViewById(R.id.vigRB);
 
 		pdb = new ProfileDatabase(getActivity());
 		pdb.open("UserDatabase");
 
 		mod.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				mod.setChecked(true);
 				vig.setChecked(false);
 			}
 		});
 
 		vig.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				vig.setChecked(true);
 				mod.setChecked(false);
 			}
 		});
 
 		cardio.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				if (!cardioSelected) {
 					cardio.setImageResource(R.drawable.running_selected);
 					resistance.setImageResource(R.drawable.weight_lifting);
 					cardioSelected = true;
 					resistanceSelected = false;
 				} else {
 					cardio.setImageResource(R.drawable.running);
 					cardioSelected = false;
 				}
 			}
 		});
 
 		resistance.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				if (!resistanceSelected) {
 					resistance
 							.setImageResource(R.drawable.weight_lifting_selected);
 					cardio.setImageResource(R.drawable.running);
 					resistanceSelected = true;
 					cardioSelected = false;
 				} else {
 					resistance.setImageResource(R.drawable.weight_lifting);
 					resistanceSelected = false;
 				}
 
 			}
 
 		});
 
 		submit.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				if ((!cardioSelected && !resistanceSelected)
 						|| minute.getText().toString().isEmpty()) {
 					Toast.makeText(getActivity(), "Please complete all fields",
 							Toast.LENGTH_SHORT).show();
 				} else {
 					double factor;
 					int weight;
 					int mins = Integer.parseInt(minute.getText().toString());
 
 					if (!pdb.isEmpty()) {
 						User user = pdb.getProfile();
 						weight = Integer.parseInt(user.getWeight());
 					} else {
 						weight = 160;
 					}
 
 					if (cardioSelected) {
 						if (vig.isChecked()) {
 							factor = 0.07;
 						} else {
 							factor = 0.03;
 						}
 
 						calBurned = (factor * weight) * mins;
 					} else if (resistanceSelected) {
 						if (vig.isChecked()) {
 							factor = 0.048;
 						} else {
 							factor = 0.024;
 						}
 
 						calBurned = (factor * weight) * mins;
 					}
 					startActivity(new Intent(getActivity(), MainActivity.class)
 							.putExtra("calBurned", calBurned));
					getActivity().finish();
 				}
 			}
 		});
 	}
 }
