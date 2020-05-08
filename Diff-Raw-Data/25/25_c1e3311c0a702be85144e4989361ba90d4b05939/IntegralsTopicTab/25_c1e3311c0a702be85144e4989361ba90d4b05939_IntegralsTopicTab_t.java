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
 public class IntegralsTopicTab extends Fragment {
 	android.widget.Button limitsGoButton, derivativesGoButton, integralsGoButton;
 	android.widget.Button limitsQuizButton, derivativesQuizButton, integralsQuizButton;
 	Spinner limits_spinner, derivatives_spinner, integrals_spinner;
 	Context context = getActivity();
 	ArrayAdapter<CharSequence> integrals_adapter;
 
 	public IntegralsTopicTab() {
 		// Required empty public constructor
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		
 		View view = inflater.inflate(R.layout.topic_integrals_layout, container, false);
 		
 		//derivatives spinner
 		integrals_spinner = (Spinner)view.findViewById(R.id.integrals_spinner);
 		
 		integrals_adapter = ArrayAdapter.createFromResource(getActivity(), R.array.integrals_array, R.layout.custom_spinner_list);
 		
 		integrals_adapter.setDropDownViewResource(R.layout.custom_spinner);
 		
 		integrals_spinner.setAdapter(integrals_adapter);
 		
 		integralsGoButton = (android.widget.Button)view.findViewById(R.id.button3);
 		
 		integralsGoButton.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				Intent i = new Intent(getActivity(), IntegralsTabs.class);
 				
 				i.putExtra("integrals tab", integrals_spinner.getSelectedItemPosition() + 1);
 				
 				getActivity().startActivity(i);
 			}
 			
 		});
 		
 		integralsQuizButton = (android.widget.Button)view.findViewById(R.id.integralsQuizButton);
 		
 		integralsQuizButton.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
				Intent i = new Intent(getActivity(), Quiz.class);
 				
 				i.putExtra("quiz", "integralsQuiz.txt");
 				i.putExtra("quiz_description", "This Quiz is on Integrals. Pick your answers and hit submit for immediate feedback. You can take the quiz as many times as you like. Good Luck!");
 				
				getActivity().startActivity(i);
 			}
 			
 		});
 		
 // Inflate the layout for this fragment
 		
 		
 		// Inflate the layout for this fragment
 		return view;
 	}
 
 }
