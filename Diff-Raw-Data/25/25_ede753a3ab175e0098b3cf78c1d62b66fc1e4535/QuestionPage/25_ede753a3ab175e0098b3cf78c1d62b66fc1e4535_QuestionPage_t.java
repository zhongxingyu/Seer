 package com.example.foundationsofsuccessfulprogramminginjava;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.os.Handler;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.RadioButton;
 import android.widget.RadioGroup;
 import android.widget.RadioGroup.OnCheckedChangeListener;
 import android.widget.TextView;
 
 public class QuestionPage extends Activity {
 
 	QuestionList qList = new QuestionList();
 
 	static int numQuestions = 10;
 	private ArrayList<Question> questions = new ArrayList<Question>(numQuestions); //questions presented 
 	private int[] responses = new int[numQuestions];	//answers given
 	private int questionNum = -1; //numbered viewed out of 10
 	private int correctAnswer = -1;
 	private int selectedAnswer = -1;
 
 	private boolean questionAnswered = false;
 	private RadioGroup rGroup;
 
 	static int clearCheck = 0;
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.question);
 		for(int i = 0; i < numQuestions; i++){
 			responses[i] = -1;
 		}
 
 		questionNum++;
 		correctAnswer = initializeQuestion();
 		rGroup = (RadioGroup)findViewById(R.id.radioGroup1);
 
 		/* This handles if question is answered correctly */
 		rGroup.clearCheck();
 		rGroup.setOnCheckedChangeListener(new OnCheckedChangeListener(){
 			@Override
 			public void onCheckedChanged(RadioGroup group, int checkedId) {
 				TextView response = (TextView)findViewById(R.id.responseText);
 				selectedAnswer = rGroup.indexOfChild(findViewById(rGroup.getCheckedRadioButtonId()));
 				if(selectedAnswer == -1 || checkedId == -1){
 					response.setText("");
 					questionAnswered = false;
 				} else if(selectedAnswer == correctAnswer){
 					response.setText("Correct!");
 					response.setTextColor(Color.GREEN);
 					questionAnswered = true;
 				} else{
 					response.setText("Incorrect.");
 					response.setTextColor(Color.RED);
 					questionAnswered = true;
 				}
 				if(questionNum == clearCheck){
 					clearCheck++;
 					if(questionAnswered){
 						// Delay for 1.5 seconds, then go to next question automatically
 						Handler h = new Handler();
 						h.postDelayed(new Runnable(){
 							@Override
 							public void run() { if((questionNum+1) < numQuestions) nextQuestion();	}
 						}, 1500);
 					}
 				}
 			}
 
 		});
 
 		Button next = (Button)findViewById(R.id.nextButton);
 		next.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				nextQuestion();
 			}
 		});
 
 		Button back = (Button)findViewById(R.id.backButton);
 		back.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				backQuestion();
 			}
 		});
 
 		Button finish = (Button)findViewById(R.id.finishButton);
 		finish.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				Intent intent = new Intent(v.getContext(), StatisticsPage.class);
 				int[] answers = new int[10];
 				for(int i = 0; i < answers.length; i++){
 					if(questions.size() <= i)
 						answers[i] = -1;
 					else
 						answers[i] = questions.get(i).correct;
 				}
 				if(questionNum > -1)
 					responses[questionNum] = selectedAnswer;
 				intent.putExtra("answers", answers);
 				intent.putExtra("responses", responses);
 				startActivityForResult(intent, 0);
 			}
 		});
 
 	}
 
 	private int initializeQuestion(){
 		Button back = (Button)findViewById(R.id.backButton);
 		if(questionNum == 0){
 			back.setVisibility(View.INVISIBLE);
 		}else{
 			back.setVisibility(View.VISIBLE);
 		}
 		Button next = (Button)findViewById(R.id.nextButton);
 		if(questionNum == numQuestions-1){
 			next.setVisibility(View.INVISIBLE);
 		}else{
 			next.setVisibility(View.VISIBLE);
 		}
 
 		if(questionNum == -1)
 			return -1;
 
 		Question q;
 		if(questions.size() == 0 || questions.size() <= questionNum){
 			q = qList.getQuestion();
 			while(questions.contains(q)){
 				q = qList.getQuestion();
 			}
 			questions.add(questionNum, q);
 			clearCheck = questionNum;
 		}else {
 			q = questions.get(questionNum);
 			correctAnswer = q.correct;
 			int index = -1;
 			switch(responses[questionNum]){
 			case 0: index = ((RadioButton)findViewById(R.id.radio0)).getId(); break;
 			case 1: index = ((RadioButton)findViewById(R.id.radio1)).getId(); break;
 			case 2: index = ((RadioButton)findViewById(R.id.radio2)).getId(); break;
 			case 3: index = ((RadioButton)findViewById(R.id.radio3)).getId(); break;
 			}
 			
 			//Advance to next question anyway? something with clearcheck
 			
 			rGroup.check(index);
 		}
 		questionAnswered = false;
 		TextView question = (TextView)findViewById(R.id.questionText);
 		RadioButton r0 = (RadioButton)findViewById(R.id.radio0);
 		RadioButton r1 = (RadioButton)findViewById(R.id.radio1);
 		RadioButton r2 = (RadioButton)findViewById(R.id.radio2);
 		RadioButton r3 = (RadioButton)findViewById(R.id.radio3);
 		question.setText((questionNum+1) + ". " + q.question);
 		r0.setText(q.answers[0]);
 		r1.setText(q.answers[1]);
 		r2.setText(q.answers[2]);
 		r3.setText(q.answers[3]);
 		return q.correct;
 	}
 
 	private void nextQuestion(){
 		questionAnswered = false;
 		// Save current answer for later results
 		if(questionNum > -1)
 			responses[questionNum] = selectedAnswer;
 
 		rGroup.clearCheck();
 		questionNum++;
 		correctAnswer = initializeQuestion();
 	}
 
 	private void backQuestion(){
 		questionAnswered = false;
 		// Save current answer for later results
 		if(questionNum > 0){
 			responses[questionNum] = selectedAnswer; 
 			questionNum--;
 			correctAnswer = initializeQuestion();
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 
 }
