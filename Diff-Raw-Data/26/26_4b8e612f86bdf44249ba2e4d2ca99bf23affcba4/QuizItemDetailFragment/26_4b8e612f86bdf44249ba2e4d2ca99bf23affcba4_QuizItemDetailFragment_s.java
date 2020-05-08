 package com.example.clipvidva.quizzes;
 
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.RadioButton;
 import android.widget.RadioGroup;
 import android.widget.TextView;
 
 import com.example.clipvidva.R;
 
 public class QuizItemDetailFragment extends Fragment {
 
     public static final String ARG_ITEM_ID = "item_id";
     public static final String ARG_ITEM_NAME = "item_name";
     public static final String ARG_SUBJECT_ID = "subject_id";
     private Question question;
     private UserAnswer userAnswer;
     private int choice;
     private View rootView;
     private QuestionsDataSource questionsDataSource;
     private UserAnswersDataSource userAnswersDataSource;
 
     public QuizItemDetailFragment() {
     }
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         Bundle extras = getActivity().getIntent().getExtras();
         String id = extras.getString(ARG_ITEM_ID);
         String name = extras.getString(ARG_ITEM_NAME);
     	String id2 = getArguments().getString(ARG_ITEM_ID);
     	String name2 = getArguments().getString(ARG_ITEM_NAME);
         Log.v(this.getClass().getName(), "ITEM_NAME "+name);
         Log.v(this.getClass().getName(), "ITEM_ID "+id);
     	Log.v(this.getClass().getName(), "ITEM_ID2 "+id2);
         Log.v(this.getClass().getName(), "ITEM_NAME2 "+name2);
 
         questionsDataSource = new QuestionsDataSource(getActivity());
         questionsDataSource.open();
         question = questionsDataSource.getOneQuestion(id, "1");
         
         userAnswersDataSource = new UserAnswersDataSource(getActivity());
         userAnswersDataSource.open();
         userAnswer = userAnswersDataSource.getUserAnswer(id, "1");
         
         if (name.length() > 0 && id.length() > 0) {
             ((QuizItemDetailActivity)getActivity()).setActionBarTitle(name);
         }
         
 /*
         Bundle extras = getActivity().getIntent().getExtras();
         String id = extras.getString(ARG_ITEM_ID);
         String name = extras.getString(ARG_ITEM_NAME);
         Log.v(this.getClass().getName(), "ITEM_NAME "+name);
 
         if (getArguments().containsKey(ARG_ITEM_ID)) {
             // Load the dummy content specified by the fragment
             // arguments. In a real-world scenario, use a Loader
             // to load content from a content provider.
             mItem = DummyContent.ITEM_MAP.get(getArguments().getString(ARG_ITEM_ID));
             String name = getArguments().getString(ARG_ITEM_NAME);
             Log.v(this.getClass().getName(), "ITEM_NAME "+name);
             
             // Set title bar
             ((QuizItemDetailActivity)getActivity()).setActionBarTitle(name);
         }*/
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
             Bundle savedInstanceState) {
         rootView = inflater.inflate(R.layout.quiz_fragment_item_detail, container, false);  
         
         if (question != null) {
         	
         	Log.v(this.getClass().getName(), "Question: " + question.getQuestion());
         	
         	// Set Choices into view
         	setChoices();
             
             // Set Question text into view
         	setQuestion();
         	
         	// Set Result text into view
         	setResult();
         	
         	// Submit Button Listener
 	        Button submitButton = (Button) rootView.findViewById(R.id.submit_button);
 	        submitButton.setOnClickListener(new QuizButtonOnClickListener());
 	        
         	// Hint Button Listener
 	        Button hintButton = (Button) rootView.findViewById(R.id.hint_button);
 	        hintButton.setOnClickListener(new QuizHintButtonOnClickListener());
 
 	        // Radio Listener
 	        RadioGroup choiceGroup = (RadioGroup) rootView.findViewById(R.id.choices);
 	        for (int i = 0; i < choiceGroup.getChildCount(); i++) {
 	            ((RadioButton) choiceGroup.getChildAt(i)).setOnClickListener(new QuizRadioButtonOnClickListener());
 	        }
 	        if (userAnswer.getAnswer().length() > 0){
 		        switch (Integer.parseInt(userAnswer.getAnswer())) {
 			        case 1:
 			            choiceGroup.check(R.id.choice1);
 			            break;
 			        case 2:
 			            choiceGroup.check(R.id.choice2);
 			            break;
 			        case 3:
 			            choiceGroup.check(R.id.choice3);
 			            break;
 			        case 4:
 			            choiceGroup.check(R.id.choice4);
 			            break;
 		        }
 	        }
         }
         
         return rootView;
     }
     
     private void setChoices(){
         RadioGroup choiceGroup = (RadioGroup) rootView.findViewById(R.id.choices);
         String[] choices = question.getChoices().split("\\|");
         for (int i = 0; i < choiceGroup.getChildCount(); i++) {
         	Log.v(this.getClass().getName(), "Choice" + Integer.toString(i) + ": " + choices[i]);
             ((RadioButton) choiceGroup.getChildAt(i)).setText(choices[i]);
         }
     }
     
     private void setQuestion(){
     	TextView questionView = ((TextView) rootView.findViewById(R.id.question_title));
     	questionView.setText(question.getQuestion());
     	questionView.postInvalidate();
     }
     
     private void setHint(){
     	TextView hintView = ((TextView) rootView.findViewById(R.id.hint));
     	String hintText = question.getHint();
     	if(hintText.length() > 0){
     		hintView.setText(question.getHint());
     	}
     	else{
     		hintView.setText(R.string.no_hint);
     	}
     	hintView.postInvalidate();
     }
     
     private void setResult(String customText){
     	TextView resultView = ((TextView) rootView.findViewById(R.id.result));
     	resultView.setText(customText);
     	resultView.postInvalidate();
     }
     
     private void setResult(){
     	TextView resultView = ((TextView) rootView.findViewById(R.id.result));
     	String resultText = userAnswer.getResult();
     	if(resultText.length() > 0){
     		resultView.setText(resultText);
     	}
     	else{
     		resultView.setText("Never Answer");
     	}
     	resultView.postInvalidate();
     }
 
     private class QuizRadioButtonOnClickListener implements View.OnClickListener {
 		@Override
 		public void onClick(View view) {
 	    	// Is the button now checked?
 	        boolean checked = ((RadioButton) view).isChecked();
 	        
 	        // Check which radio button was clicked
 	        switch(view.getId()) {
 		        case R.id.choice1:
 		            if (checked){
 		            	Log.v(this.getClass().getName(), "Choice1 selected");
 		            	choice = 1;
 		            }
 		            break;
 		        case R.id.choice2:
 		            if (checked){
 		            	Log.v(this.getClass().getName(), "Choice2 selected");
 		            	choice = 2;
 		            }
 		            break;
 		        case R.id.choice3:
 		            if (checked){
 		            	Log.v(this.getClass().getName(), "Choice3 selected");
 		            	choice = 3;
 		            }
 		            break;
 		        case R.id.choice4:
 		            if (checked){
 		            	Log.v(this.getClass().getName(), "Choice4 selected");
 		            	choice = 4;
 		            }
 		            break;
 	        }
 		}
     }
     
     private class QuizButtonOnClickListener implements View.OnClickListener {
 		@Override
 		public void onClick(View view) {
 	    	Log.v(this.getClass().getName(), "Choice entered: "+ Integer.toString(choice));
 	    	Log.v(this.getClass().getName(), "Answer: "+ question.getAnswer());
 	    	String result;
 	    	if (isCorrectChoice()) {
 	    		result = "Correct";
 	    		Log.v(this.getClass().getName(), "Correct!");
 	    		CorrectDialogFragment correctDialog = new CorrectDialogFragment();
 	    		correctDialog.setDescription(question.getDescription());
 	    		correctDialog.show(getActivity().getSupportFragmentManager(), "CorrectDialogFragment");
 	    	}
 	    	else{
 	    		result = "Incorrect";
 	    		Log.v(this.getClass().getName(), "Incorrect!");
 	    		IncorrectDialogFragment incorrectDialog = new IncorrectDialogFragment();
 	    		incorrectDialog.show(getActivity().getSupportFragmentManager(), "IncorrectDialogFragment");	    		
 	    	}
     		userAnswersDataSource.editAnswer(userAnswer.getSubject_id(), 
 					 userAnswer.getQuestion_id(), 
 					 Integer.toString(choice), 
 					 result);
 	    	setResult(result);
 		}
 		
 		private boolean isCorrectChoice(){
 			if (choice == Integer.parseInt(question.getAnswer())){
 				return true;
 			}
 			return false;
 		}
     }
 
     private class QuizHintButtonOnClickListener implements View.OnClickListener {
 		@Override
 		public void onClick(View view) {
 			setHint();
 		}
     }
  
 }
