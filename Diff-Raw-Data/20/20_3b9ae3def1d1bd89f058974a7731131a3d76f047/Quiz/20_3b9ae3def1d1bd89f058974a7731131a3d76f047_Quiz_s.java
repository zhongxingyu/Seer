 package com.example.educationalapp;
 import java.util.Scanner; 
 import java.io.InputStream;
 import java.util.ArrayList;
 
 import android.app.ActionBar;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Color;
 import android.graphics.drawable.ColorDrawable;
 import android.os.Bundle;
 import android.widget.Button;
 import android.widget.RadioButton;
 import android.widget.RadioGroup;
 import android.widget.TextView;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 
 
 public class Quiz extends Activity {
 	MissedLimits mL;
 	MissedDerivatives mD;
 	MissedIntegrals mI;
 
 	private class Question
 	{
 	   String question;
 	   String answer;
 	   String response;
 	   String[] possibles;
 	   String video;
 	   String topic;
 	   public Question(String q , String a , String[] p , String v , String t)
 	   {
 	      question = q;
 	      answer = a;
 	      possibles = p;
 	      video = v;
 	      topic = t;
 	   }
 	}
 	private ArrayList<Question> qList = new ArrayList<Question>(); 
 	private ArrayList<Question> madeList = new ArrayList<Question>();
 	private final int NUM_ANSWERS = 4;
 	private final int NUM_QUESTIONS = 10;
 	private final Context context = this;
 	private String fileName;
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.quizlayout);
		TextView tv = (TextView)findViewById(R.id.quiz_description);
	    
		Intent i = getIntent();
 		
 
 		fileName = i.getStringExtra("quiz");
 		
 		mL = new MissedLimits((Context)this);
 		mI = new MissedIntegrals((Context)this);
 		mD = new MissedDerivatives((Context)this);

		tv.setText(i.getStringExtra("quiz_description"));
 		
 		ActionBar bar = getActionBar();
 		bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#3CC7C7")));
 		
 		getQuestions();	
 		createQuiz();
 		displayQuiz();
 	    addListener();
 	}
 	private void getQuestions()
 	{
 	   try {
 	      Scanner scan = new Scanner(readFile(fileName));
 	      while(scan.hasNextLine())
 	      {
 	         String question = scan.nextLine();
 	         String answer = scan.nextLine();
 	         String[] possibles = new String[NUM_ANSWERS];
 	         for(int i = 0; i < possibles.length; i++)
 	            possibles[i] = scan.nextLine();
 	         String video = scan.nextLine();
 	         String topic = scan.nextLine();
 	         Question q = new Question(question,answer,possibles,video,topic);
 	         qList.add(q);
           }
 	   } catch (Exception ex) {
 	      System.err.println("Error reading the file...");
 	   }
 	      
 	}
 	private void createQuiz()
 	{ 
 	   ArrayList<Integer> questionNums = new ArrayList<Integer>(); 
 	   while(questionNums.size() < NUM_QUESTIONS)
 	   {
 		   int ran = (int)(Math.random() * 20);
 		   if(!questionNums.contains(ran))
 			   questionNums.add(ran);
 	   }
 	   for(int i = 0; i < NUM_QUESTIONS; i++)
 	   {
 		   madeList.add(qList.get(questionNums.get(i)));	   
 	   }
 	   
 	}
 	private void displayQuiz()
 	{
 	   ArrayList<TextView> listOfQuestions = new ArrayList<TextView>();
 	   ArrayList<RadioButton> listOfButtons = new ArrayList<RadioButton>();
 	   
 	   listOfQuestions.add((TextView)findViewById(R.id.quiz_question1));
 	   listOfQuestions.add((TextView)findViewById(R.id.quiz_question2));
 	   listOfQuestions.add((TextView)findViewById(R.id.quiz_question3));
 	   listOfQuestions.add((TextView)findViewById(R.id.quiz_question4));
 	   listOfQuestions.add((TextView)findViewById(R.id.quiz_question5));
 	   listOfQuestions.add((TextView)findViewById(R.id.quiz_question6));
 	   listOfQuestions.add((TextView)findViewById(R.id.quiz_question7));
 	   listOfQuestions.add((TextView)findViewById(R.id.quiz_question8));
 	   listOfQuestions.add((TextView)findViewById(R.id.quiz_question9));
 	   listOfQuestions.add((TextView)findViewById(R.id.quiz_question10));
 	  
 	   
 	   listOfButtons.add((RadioButton)findViewById(R.id.q1a1));
 	   listOfButtons.add((RadioButton)findViewById(R.id.q1a2));
 	   listOfButtons.add((RadioButton)findViewById(R.id.q1a3));
 	   listOfButtons.add((RadioButton)findViewById(R.id.q1a4));
 	   listOfButtons.add((RadioButton)findViewById(R.id.q2a1));
 	   listOfButtons.add((RadioButton)findViewById(R.id.q2a2));
 	   listOfButtons.add((RadioButton)findViewById(R.id.q2a3));
 	   listOfButtons.add((RadioButton)findViewById(R.id.q2a4));
 	   listOfButtons.add((RadioButton)findViewById(R.id.q3a1));
 	   listOfButtons.add((RadioButton)findViewById(R.id.q3a2));
 	   listOfButtons.add((RadioButton)findViewById(R.id.q3a3));
 	   listOfButtons.add((RadioButton)findViewById(R.id.q3a4));
 	   listOfButtons.add((RadioButton)findViewById(R.id.q4a1));
 	   listOfButtons.add((RadioButton)findViewById(R.id.q4a2));
 	   listOfButtons.add((RadioButton)findViewById(R.id.q4a3));
 	   listOfButtons.add((RadioButton)findViewById(R.id.q4a4));
 	   listOfButtons.add((RadioButton)findViewById(R.id.q5a1));
 	   listOfButtons.add((RadioButton)findViewById(R.id.q5a2));
 	   listOfButtons.add((RadioButton)findViewById(R.id.q5a3));
 	   listOfButtons.add((RadioButton)findViewById(R.id.q5a4));
 	   listOfButtons.add((RadioButton)findViewById(R.id.q6a1));
 	   listOfButtons.add((RadioButton)findViewById(R.id.q6a2));
 	   listOfButtons.add((RadioButton)findViewById(R.id.q6a3));
 	   listOfButtons.add((RadioButton)findViewById(R.id.q6a4));
 	   listOfButtons.add((RadioButton)findViewById(R.id.q7a1));
 	   listOfButtons.add((RadioButton)findViewById(R.id.q7a2));
 	   listOfButtons.add((RadioButton)findViewById(R.id.q7a3));
 	   listOfButtons.add((RadioButton)findViewById(R.id.q7a4));
 	   listOfButtons.add((RadioButton)findViewById(R.id.q8a1));
 	   listOfButtons.add((RadioButton)findViewById(R.id.q8a2));
 	   listOfButtons.add((RadioButton)findViewById(R.id.q8a3));
 	   listOfButtons.add((RadioButton)findViewById(R.id.q8a4));
 	   listOfButtons.add((RadioButton)findViewById(R.id.q9a1));
 	   listOfButtons.add((RadioButton)findViewById(R.id.q9a2));
 	   listOfButtons.add((RadioButton)findViewById(R.id.q9a3));
 	   listOfButtons.add((RadioButton)findViewById(R.id.q9a4));
 	   listOfButtons.add((RadioButton)findViewById(R.id.q10a1));
 	   listOfButtons.add((RadioButton)findViewById(R.id.q10a2));
 	   listOfButtons.add((RadioButton)findViewById(R.id.q10a3));
 	   listOfButtons.add((RadioButton)findViewById(R.id.q10a4));
 	   
 	   for(int i = 0; i < madeList.size(); i++)
 	   {
 	      Question q = madeList.get(i);
 	      listOfQuestions.get(i).setText(q.question);
 	      listOfQuestions.get(i).setTextColor(getResources().getColor(R.color.black));
 	      for(int j = i*NUM_ANSWERS, k = 0; k < NUM_ANSWERS;k++, j++)
 	      {
 	    	  listOfButtons.get(j).setText(q.possibles[k]);
 	    	  listOfButtons.get(j).setTextColor(getResources().getColor(R.color.black));
 	      }
 	   }
 	}
 	private String readFile(String fileName)
 	{
 		String text = fileName; //your text file name in the assets folder
 		byte[] buffer = null;
 		InputStream is;
 		try {
 		is = getAssets().open(text);
 		int size = is.available(); //size of the file in bytes
 		buffer = new byte[size]; //declare the size of the byte array with size of the file
 		is.read(buffer); //read file
 		is.close(); //close file
 		} catch (Exception e) {
 		e.printStackTrace();
 		}
 		// Store text file data in the string variable
 		String str_data = new String(buffer);
         return str_data;
 	}
 	private void addListener()
 	{
 		Button submit = (Button) findViewById(R.id.submit); 
 		submit.setOnClickListener(new OnClickListener(){
 	        @Override
 	        public void onClick(View view) {
 	        	try {
 	        		RadioButton b1 = (RadioButton)findViewById(((RadioGroup)findViewById(R.id.qg1)).getCheckedRadioButtonId());
 		        	RadioButton b2 = (RadioButton)findViewById(((RadioGroup)findViewById(R.id.qg2)).getCheckedRadioButtonId()); 
 		        	RadioButton b3 = (RadioButton)findViewById(((RadioGroup)findViewById(R.id.qg3)).getCheckedRadioButtonId()); 
 		        	RadioButton b4 = (RadioButton)findViewById(((RadioGroup)findViewById(R.id.qg4)).getCheckedRadioButtonId()); 
 		        	RadioButton b5 = (RadioButton)findViewById(((RadioGroup)findViewById(R.id.qg5)).getCheckedRadioButtonId()); 
 		        	RadioButton b6 = (RadioButton)findViewById(((RadioGroup)findViewById(R.id.qg6)).getCheckedRadioButtonId()); 
 		        	RadioButton b7 = (RadioButton)findViewById(((RadioGroup)findViewById(R.id.qg7)).getCheckedRadioButtonId()); 
 		        	RadioButton b8 = (RadioButton)findViewById(((RadioGroup)findViewById(R.id.qg8)).getCheckedRadioButtonId());
 		        	RadioButton b9 = (RadioButton)findViewById(((RadioGroup)findViewById(R.id.qg9)).getCheckedRadioButtonId());
 		        	RadioButton b10 = (RadioButton)findViewById(((RadioGroup)findViewById(R.id.qg10)).getCheckedRadioButtonId());
 	        	
 	        	ArrayList<String> answers = new ArrayList<String>();
 	        	
 	        	answers.add(b1.getText().toString());
 	        	answers.add(b2.getText().toString());
 	        	answers.add(b3.getText().toString());
 	        	answers.add(b4.getText().toString());
 	        	answers.add(b5.getText().toString());
 	        	answers.add(b6.getText().toString());
 	        	answers.add(b7.getText().toString());
 	        	answers.add(b8.getText().toString());
 	        	answers.add(b9.getText().toString());
 	        	answers.add(b10.getText().toString());
 	        	
 	        	ArrayList<Question> incorrect = new ArrayList<Question>();
 	        	ArrayList<String> wrong = new ArrayList<String>();
 	        	
 	        	for(int i = 0; i < madeList.size(); i++)
 	        	{
 	        	   Question q = madeList.get(i);
 	        	   if(!q.answer.equals(answers.get(i)))
 	        	   {
 	        		  q.response = answers.get(i);
 	        	      incorrect.add(q);
 	        	      if(!wrong.contains(q.topic))
 	        	      {
 	        	         wrong.add(q.topic);
 	        	      }
 	        	   }
 	        	}
 	        	
 	        	if (fileName.equals("limitsQuiz.txt"))
 	        		mL.clean();
 	        	else if (fileName.equals("derivativesQuiz.txt"))
 	        	    mD.clean();
 	        	else
 	        		mI.clean();
 	        	
 	        	AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context, AlertDialog.THEME_HOLO_LIGHT);
 	        	alertBuilder.setTitle("Quiz Results");
 	        	String resultsMessage = "";
 	        	
 	        	if(incorrect.size() == 0)
 	        	{
 	        	   resultsMessage = "Congratulations! You got 100%";	
 	        	}
 	        	else
 	        	{	
 	        	   resultsMessage = "You answered " + (10 - incorrect.size()) + "/" + "10 questions correctly" + "\n\n" + "You missed the following questions: \n\n";
 	        	  
 	        	   
 	        	   for(int i = 0; i < incorrect.size(); i++)
 	        	   {
 	        	      resultsMessage += "-----------------------------------------------------------------------------------------------\n"
 	        	    		  			+ "QUESTION: " + incorrect.get(i).question + "\n\n" +
 	        	                        "YOU ANSWERED: " + incorrect.get(i).response + "\n\n" +
 	        	                        "CORRECT ANSWER: "  + incorrect.get(i).answer + "\n\n" +
 	        	                        "RECOMMENDATION: The topic of " + incorrect.get(i).topic + " was discussed in the video '" + incorrect.get(i).video + "'. You may want to review this video.\n\n";
 	        	      if (fileName.equals("limitsQuiz.txt")) {
 	        	    	  mL.add(mL.new MissedQuestion(incorrect.get(i).video, incorrect.get(i).topic));
 	        	    	//  Log.d("MathPath", "Limits; adding " + incorrect.get(i).topic);
 	        	      }
 	        	      else if (fileName.equals("derivativesQuiz.txt")) {
 	        	    	  mD.add(mD.new MissedQuestion(incorrect.get(i).video, incorrect.get(i).topic));
 	        	    	//  Log.d("MathPath", "Derivatives; adding " + incorrect.get(i).topic);
 	        	      }
 	      
 	        	      else if (fileName.equals("integralsQuiz.txt")){
 	        	    	  mI.add(mI.new MissedQuestion(incorrect.get(i).video, incorrect.get(i).topic));
 	        	    	//  Log.d("MathPath", "Integrals; adding " + incorrect.get(i).topic);
 	        	      }
 	        	   }
 	        	   if (fileName.equals("limitsQuiz.txt")) 
 	        		   mL.analyze();
 	        	   else if (fileName.equals("derivativesQuiz.txt"))
 	        		   mD.analyze();
 	        	   else if (fileName.equals("integralsQuiz.txt"))
 	        		   mI.analyze();
 	        	   
 	        	   // save the missed topic in the MissedTopics class here it is the wrong arraylist
 	        	}
 	        	alertBuilder.setMessage(resultsMessage);
 				AlertDialog results = alertBuilder.create();
 				results.show();
 	        	} catch (Exception ex) {
 	        		AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
 	        		alertBuilder.setMessage("Please answer all the questions then press submit again");
 					AlertDialog results = alertBuilder.create();
 					results.show();
 					ex.printStackTrace();
 	        	}
 	        }
 	    });
 	}
 }
