 package com.example.educationalapp;
 import java.util.Scanner; 
 import java.io.InputStream;
 import java.util.ArrayList;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.os.Bundle;
 import android.widget.Button;
 import android.widget.RadioButton;
 import android.widget.RadioGroup;
 import android.widget.TextView;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 
 public class Quiz extends Activity {
 
 	public class Question
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
 	    
 		fileName = "limitQuiz.txt";
 		
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
 	   
 	   TextView question1 = (TextView)findViewById(R.id.quiz_question1);
 	   listOfQuestions.add(question1);
 	   TextView question2 = (TextView)findViewById(R.id.quiz_question2);
 	   listOfQuestions.add(question2);
 	   TextView question3 = (TextView)findViewById(R.id.quiz_question3);
 	   listOfQuestions.add(question3);
 	   TextView question4 = (TextView)findViewById(R.id.quiz_question4);
 	   listOfQuestions.add(question4);
 	   TextView question5 = (TextView)findViewById(R.id.quiz_question5);
 	   listOfQuestions.add(question5);
 	   TextView question6 = (TextView)findViewById(R.id.quiz_question6);
 	   listOfQuestions.add(question6);
 	   TextView question7 = (TextView)findViewById(R.id.quiz_question7);
 	   listOfQuestions.add(question7);
 	   TextView question8 = (TextView)findViewById(R.id.quiz_question8);
 	   listOfQuestions.add(question8);
 	   TextView question9 = (TextView)findViewById(R.id.quiz_question9);
 	   listOfQuestions.add(question9);
 	   TextView question10 = (TextView)findViewById(R.id.quiz_question10);
 	   listOfQuestions.add(question10);
 	   
 	   RadioButton a1 = (RadioButton)findViewById(R.id.q1a1);
 	   listOfButtons.add(a1);
 	   RadioButton b1 = (RadioButton)findViewById(R.id.q1a2);
 	   listOfButtons.add(b1);
 	   RadioButton c1 = (RadioButton)findViewById(R.id.q1a3);
 	   listOfButtons.add(c1);
 	   RadioButton d1 = (RadioButton)findViewById(R.id.q1a4);
 	   listOfButtons.add(d1);
 	   RadioButton a2 = (RadioButton)findViewById(R.id.q2a1);
 	   listOfButtons.add(a2);
 	   RadioButton b2 = (RadioButton)findViewById(R.id.q2a2);
 	   listOfButtons.add(b2);
 	   RadioButton c2 = (RadioButton)findViewById(R.id.q2a3);
 	   listOfButtons.add(c2);
 	   RadioButton d2 = (RadioButton)findViewById(R.id.q2a4);
 	   listOfButtons.add(d2);
 	   RadioButton a3 = (RadioButton)findViewById(R.id.q3a1);
 	   listOfButtons.add(a3);
 	   RadioButton b3 = (RadioButton)findViewById(R.id.q3a2);
 	   listOfButtons.add(b3);
 	   RadioButton c3 = (RadioButton)findViewById(R.id.q3a3);
 	   listOfButtons.add(c3);
 	   RadioButton d3 = (RadioButton)findViewById(R.id.q3a4);
 	   listOfButtons.add(d3);
 	   RadioButton a4 = (RadioButton)findViewById(R.id.q4a1);
 	   listOfButtons.add(a4);
 	   RadioButton b4 = (RadioButton)findViewById(R.id.q4a2);
 	   listOfButtons.add(b4);
 	   RadioButton c4 = (RadioButton)findViewById(R.id.q4a3);
 	   listOfButtons.add(c4);
 	   RadioButton d4 = (RadioButton)findViewById(R.id.q4a4);
 	   listOfButtons.add(d4);
 	   RadioButton a5 = (RadioButton)findViewById(R.id.q5a1);
 	   listOfButtons.add(a5);
 	   RadioButton b5 = (RadioButton)findViewById(R.id.q5a2);
 	   listOfButtons.add(b5);
 	   RadioButton c5 = (RadioButton)findViewById(R.id.q5a3);
 	   listOfButtons.add(c5);
 	   RadioButton d5 = (RadioButton)findViewById(R.id.q5a4);
 	   listOfButtons.add(d5);
 	   RadioButton a6 = (RadioButton)findViewById(R.id.q6a1);
 	   listOfButtons.add(a6);
 	   RadioButton b6 = (RadioButton)findViewById(R.id.q6a2);
 	   listOfButtons.add(b6);
 	   RadioButton c6 = (RadioButton)findViewById(R.id.q6a3);
 	   listOfButtons.add(c6);
 	   RadioButton d6 = (RadioButton)findViewById(R.id.q6a4);
 	   listOfButtons.add(d6);
 	   RadioButton a7 = (RadioButton)findViewById(R.id.q7a1);
 	   listOfButtons.add(a7);
 	   RadioButton b7 = (RadioButton)findViewById(R.id.q7a2);
 	   listOfButtons.add(b7);
 	   RadioButton c7 = (RadioButton)findViewById(R.id.q7a3);
 	   listOfButtons.add(c7);
 	   RadioButton d7 = (RadioButton)findViewById(R.id.q7a4);
 	   listOfButtons.add(d7);
 	   RadioButton a8 = (RadioButton)findViewById(R.id.q8a1);
 	   listOfButtons.add(a8);
 	   RadioButton b8 = (RadioButton)findViewById(R.id.q8a2);
 	   listOfButtons.add(b8);
 	   RadioButton c8 = (RadioButton)findViewById(R.id.q8a3);
 	   listOfButtons.add(c8);
 	   RadioButton d8 = (RadioButton)findViewById(R.id.q8a4);
 	   listOfButtons.add(d8);
 	   RadioButton a9 = (RadioButton)findViewById(R.id.q9a1);
 	   listOfButtons.add(a9);
 	   RadioButton b9 = (RadioButton)findViewById(R.id.q9a2);
 	   listOfButtons.add(b9);
 	   RadioButton c9 = (RadioButton)findViewById(R.id.q9a3);
 	   listOfButtons.add(c9);
 	   RadioButton d9 = (RadioButton)findViewById(R.id.q9a4);
 	   listOfButtons.add(d9);
 	   RadioButton a10 = (RadioButton)findViewById(R.id.q10a1);
 	   listOfButtons.add(a10);
 	   RadioButton b10 = (RadioButton)findViewById(R.id.q10a2);
 	   listOfButtons.add(b10);
 	   RadioButton c10 = (RadioButton)findViewById(R.id.q10a3);
 	   listOfButtons.add(c10);
 	   RadioButton d10 = (RadioButton)findViewById(R.id.q10a4);
 	   listOfButtons.add(d10);
 	   
 	   for(int i = 0; i < madeList.size(); i++)
 	   {
 	      Question q = madeList.get(i);
 	      listOfQuestions.get(i).setText(q.question);
 	      for(int j = i*NUM_ANSWERS, k = 0; k < NUM_ANSWERS;k++, j++)
 	      {
 	    	  listOfButtons.get(j).setText(q.possibles[k]);
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
 	        	RadioGroup g1 = (RadioGroup) findViewById(R.id.g1); 
 	        	int selected1 = g1.getCheckedRadioButtonId();
 	        	RadioButton b1 = (RadioButton) findViewById(selected1);
 	        	RadioGroup g2 = (RadioGroup) findViewById(R.id.g2); 
 	        	int selected2 = g2.getCheckedRadioButtonId();
 	        	RadioButton b2 = (RadioButton) findViewById(selected2);
 	        	RadioGroup g3 = (RadioGroup) findViewById(R.id.g3); 
 	        	int selected3 = g3.getCheckedRadioButtonId();
 	        	RadioButton b3 = (RadioButton) findViewById(selected3);
 	        	RadioGroup g4 = (RadioGroup) findViewById(R.id.g4); 
 	        	int selected4 = g4.getCheckedRadioButtonId();
 	        	RadioButton b4 = (RadioButton) findViewById(selected4);
 	        	RadioGroup g5 = (RadioGroup) findViewById(R.id.g5); 
 	        	int selected5 = g5.getCheckedRadioButtonId();
 	        	RadioButton b5 = (RadioButton) findViewById(selected5);
 	        	RadioGroup g6 = (RadioGroup) findViewById(R.id.g6); 
 	        	int selected6 = g6.getCheckedRadioButtonId();
 	        	RadioButton b6 = (RadioButton) findViewById(selected6);
 	        	RadioGroup g7 = (RadioGroup) findViewById(R.id.g7); 
 	        	int selected7 = g7.getCheckedRadioButtonId();
 	        	RadioButton b7 = (RadioButton) findViewById(selected7);
 	        	RadioGroup g8 = (RadioGroup) findViewById(R.id.g8); 
 	        	int selected8 = g8.getCheckedRadioButtonId();
 	        	RadioButton b8 = (RadioButton) findViewById(selected8);
 	        	RadioGroup g9 = (RadioGroup) findViewById(R.id.g9); 
 	        	int selected9 = g9.getCheckedRadioButtonId();
 	        	RadioButton b9 = (RadioButton) findViewById(selected9);
 	        	RadioGroup g10 = (RadioGroup) findViewById(R.id.g10); 
 	        	int selected10 = g10.getCheckedRadioButtonId();
 	        	RadioButton b10 = (RadioButton) findViewById(selected10);
 	        	
 	        	ArrayList<String> answers = new ArrayList<String>();
 	        	
 	        	String a1 = b1.getText().toString();
 	        	answers.add(a1);
 	        	String a2 = b2.getText().toString();
 	        	answers.add(a2);
 	        	String a3 = b3.getText().toString();
 	        	answers.add(a3);
 	        	String a4 = b4.getText().toString();
 	        	answers.add(a4);
 	        	String a5 = b5.getText().toString();
 	        	answers.add(a5);
 	        	String a6 = b6.getText().toString();
 	        	answers.add(a6);
 	        	String a7 = b7.getText().toString();
 	        	answers.add(a7);
 	        	String a8 = b8.getText().toString();
 	        	answers.add(a8);
 	        	String a9 = b9.getText().toString();
 	        	answers.add(a9);
 	        	String a10 = b10.getText().toString();
 	        	answers.add(a10);
 	        	
 	        	ArrayList<Question> incorrect = new ArrayList<Question>();
 	        	for(int i = 0; i < madeList.size(); i++)
 	        	{
 	        	   Question q = madeList.get(i);
 	        	   if(!q.answer.equals(answers.get(i)))
 	        	   {
 	        		  q.response = answers.get(i);
 	        	      incorrect.add(q);
 	        	   }
 	        	}
 	        	AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
 	        	alertBuilder.setTitle("Quiz Results");
 	        	String resultsMessage = "";
 	        	if(incorrect.size() == 0)
 	        	{
 	        	   resultsMessage = "You got 100%";	
 	        	}
 	        	else
 	        	{
 	        	   resultsMessage = "You missed the following questions...\n\n";
 	        	   for(int i = 0; i < incorrect.size(); i++)
 	        	   {
 	        	      resultsMessage += incorrect.get(i).question + "\n" +
 	        	                        "You put " + incorrect.get(i).response + "\n" +
 	        	                        "The correct answer is "  + incorrect.get(i).answer + "\n" +
 	        	                        "You should review video " + incorrect.get(i).video + " for topic " + incorrect.get(i).topic + "\n\n";
 	        	   }
 	        	}
 	        	alertBuilder.setMessage(resultsMessage);
 				AlertDialog results = alertBuilder.create();
 				results.show();
 	        	} catch (Exception ex) {
 	        		AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
 	        		alertBuilder.setMessage("Please answer all the questions");
 					AlertDialog results = alertBuilder.create();
 					results.show();
					ex.printStackTrace();
					Log.d("mathpath","exception");
 	        	}
 	        }
 	    });
 	}
 }
