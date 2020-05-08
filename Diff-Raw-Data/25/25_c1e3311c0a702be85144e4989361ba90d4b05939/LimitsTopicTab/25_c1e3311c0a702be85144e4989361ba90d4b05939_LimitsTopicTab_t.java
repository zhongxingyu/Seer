 package com.example.educationalapp;
 
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.View.OnClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Spinner;
 
 /**
  * A simple {@link android.support.v4.app.Fragment} subclass.
  * 
  */
 public class LimitsTopicTab extends Fragment {
 	android.widget.Button limitsGoButton, derivativesGoButton, integralsGoButton;
 	android.widget.Button limitsQuizButton, derivativesQuizButton, integralsQuizButton;
 	Spinner limits_spinner, derivatives_spinner, integrals_spinner;
 	Context context = getActivity();
 	ArrayAdapter<CharSequence> limits_adapter;
 
 	public LimitsTopicTab() {
 		// Required empty public constructor
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		View view = inflater.inflate(R.layout.topics_limits_layout, container, false);
 		
 		//limit spinner
 				limits_spinner = (Spinner)view.findViewById(R.id.limits_spinner);
 				
 				limits_adapter = ArrayAdapter.createFromResource(getActivity(), R.array.limits_array, R.layout.custom_spinner_list);
 				
 				limits_adapter.setDropDownViewResource(R.layout.custom_spinner);
 				
 				limits_spinner.setAdapter(limits_adapter);
 				
 				limitsGoButton = (android.widget.Button)view.findViewById(R.id.button1);
 				
 				limitsGoButton.setOnClickListener(new OnClickListener() {
 
 					@Override
 					public void onClick(View v) {
 						Intent i = new Intent(getActivity(), LimitsTabs.class);
 						
 						i.putExtra("limits tab", limits_spinner.getSelectedItemPosition() + 1);
 						
 						getActivity().startActivity(i);
 					}
 					
 				});
 				
 			limitsQuizButton = (android.widget.Button)view.findViewById(R.id.limitsQuizButton);
 				
 				limitsQuizButton.setOnClickListener(new OnClickListener() {
 
 					@Override
 					public void onClick(View v) {
						Intent i = new Intent(getActivity(), Quiz.class);
 						
 						i.putExtra("quiz", "limitsQuiz.txt");
 						i.putExtra("quiz_description", "This Quiz is on Limits. Pick your answers and hit submit for immediate feedback. You can take the quiz as many times as you like. Good Luck!");
 						
						getActivity().startActivity(i);
 					}
 					
 				});
 		
 		
 		// Inflate the layout for this fragment
 		return view;
 	}
 	
 		
 
 }
