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
 public class DerivativesTopicTab extends Fragment {
 	android.widget.Button limitsGoButton, derivativesGoButton, integralsGoButton;
 	android.widget.Button limitsQuizButton, derivativesQuizButton, integralsQuizButton;
 	Spinner limits_spinner, derivatives_spinner, integrals_spinner;
 	Context context = getActivity();
 	ArrayAdapter<CharSequence> derivatives_adapter;
 
 	public DerivativesTopicTab() {
 		// Required empty public constructor
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		
 		View view = inflater.inflate(R.layout.topics_derivatives_layout, container, false);
 
 		
 		//derivatives spinner
 				derivatives_spinner = (Spinner)view.findViewById(R.id.derivatives_spinner);
 				
 				derivatives_adapter = ArrayAdapter.createFromResource(getActivity(), R.array.derivatives_array, R.layout.custom_spinner_list);
 				
 				derivatives_adapter.setDropDownViewResource(R.layout.custom_spinner);
 				
 				derivatives_spinner.setAdapter(derivatives_adapter);
 				
 				derivativesGoButton = (android.widget.Button)view.findViewById(R.id.button2);
 				
 				derivativesGoButton.setOnClickListener(new OnClickListener() {
 
 					@Override
 					public void onClick(View v) {
 						Intent i = new Intent(getActivity(), DerivativesTabs.class);
 						
 						i.putExtra("derivatives tab", derivatives_spinner.getSelectedItemPosition() + 1);
 						
 						getActivity().startActivity(i);
 					}
 					
 				});
 				
 				derivativesQuizButton = (android.widget.Button)view.findViewById(R.id.derivativesQuizButton);
 				
 				derivativesQuizButton.setOnClickListener(new OnClickListener() {
 
 					@Override
 					public void onClick(View v) {
						Intent i = new Intent(context, Quiz.class);
 						
 						i.putExtra("quiz", "derivativesQuiz.txt");
 						i.putExtra("quiz_description", "This Quiz is on Derivatives. Pick your answers and hit submit for immediate feedback. You can take the quiz as many times as you like. Good Luck!");
 						
						context.startActivity(i);
 					}
 					
 				});
 				
 		// Inflate the layout for this fragment
 		return view;
 	}
 
 }
