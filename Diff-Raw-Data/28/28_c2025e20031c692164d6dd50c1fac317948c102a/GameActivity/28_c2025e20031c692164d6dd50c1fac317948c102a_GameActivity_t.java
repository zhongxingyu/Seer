 package net.redlinesoft.app.plusclaver;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.os.Bundle;
 import android.os.Handler;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.content.pm.ActivityInfo;
 import android.graphics.Typeface;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class GameActivity extends Activity {
 
 	// game lavel
 	private Integer GAME_LEVEL = 1;
 
 	// game score
 	private Integer GAME_SCORE = 0;
 
 	// overall correct answer
 	private Integer GAME_ANSWER = 0;
 
 	// overall have answer
 	private Integer GAME_GETANSWER = 0;
 
 	// overall time out
 	public Integer GAME_TIMEOUT = 20;
 	public Integer GAME_TIMETICK = 1;
 
 	// result for puls score
 	public Integer GAME_RESULT = 1;
 
 	// question string
 	private String GAME_QUESTION = "";
 
 	// ratio for change level
 	public Integer GAME_RATIO = 10;
 
 	// ratio count
	public Integer GAME_RATIO_COUNT = 1;
 
 	Handler handler = new Handler();
 	Timer timer = new Timer();
 	TimerTask timetask,timeremain;
 	
 	// type facce
 	Typeface face;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_game);
 		
 		// set typeface for text all game
 		face=Typeface.createFromAsset(getAssets(),"fonts/Arabica.ttf");
 		
 		// TODO this should change in to text style
 		TextView textUnit = (TextView) findViewById(R.id.textUnit);
 		textUnit.setTypeface(face);
 		TextView textLevel = (TextView) findViewById(R.id.textLevel);
 		textLevel.setTypeface(face);
 		TextView textTime = (TextView) findViewById(R.id.textTime);
 		textTime.setTypeface(face);
 		
 		
 
 		// initial value for first start
 		GAME_GETANSWER = 1;			
 		
 		// set all answer button to false
 		setButton(false);
 		getNewQuestion();
 		doTask();
 		
 		/*
 		 * // get question via level getQuestion(GAME_LEVEL);
 		 * 
 		 * // set data setData2View(GAME_QUESTION);
 		 */
 
 		// button lisener
 		Button buttonChoice1 = (Button) findViewById(R.id.button1);
 		buttonChoice1.setTypeface(face);
 		buttonChoice1.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				setScore(0);
 			}
 		});
 
 		Button buttonChoice2 = (Button) findViewById(R.id.button2);
 		buttonChoice2.setTypeface(face);
 		buttonChoice2.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				setScore(1);
 			}
 		});
 
 		Button buttonChoice3 = (Button) findViewById(R.id.button3);
 		buttonChoice3.setTypeface(face);
 		buttonChoice3.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				setScore(2);
 			}
 		});
 
 		Button buttonChoice4 = (Button) findViewById(R.id.button4);
 		buttonChoice4.setTypeface(face);
 		buttonChoice4.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				setScore(3);
 			}
 		});
 
 		Button buttonChoice5 = (Button) findViewById(R.id.button5);
 		buttonChoice5.setTypeface(face);
 		buttonChoice5.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				setScore(4);
 			}
 		});
 
 		Button buttonChoice6 = (Button) findViewById(R.id.button6);
 		buttonChoice6.setTypeface(face);
 		buttonChoice6.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				setScore(5);
 			}
 		});
 
 		Button buttonChoice7 = (Button) findViewById(R.id.button7);
 		buttonChoice7.setTypeface(face);
 		buttonChoice7.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				setScore(6);
 			}
 		});
 
 		Button buttonChoice8 = (Button) findViewById(R.id.button8);
 		buttonChoice8.setTypeface(face);
 		buttonChoice8.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				setScore(7);
 			}
 		});
 
 		Button buttonChoice9 = (Button) findViewById(R.id.button9);
 		buttonChoice9.setTypeface(face);
 		buttonChoice9.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				setScore(8);
 			}
 		});
 
 		Button buttonChoice10 = (Button) findViewById(R.id.button10);
 		buttonChoice10.setTypeface(face);
 		buttonChoice10.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				setScore(9);
 			}
 
 		});
 
 	}
 	 
 	protected void setScore(int intChoice) {
 		// TODO Auto-generated method stub 
 		if (GAME_ANSWER == intChoice) {
			GAME_SCORE = (int) (GAME_SCORE + ((GAME_LEVEL * GAME_RATIO_COUNT) * (GAME_TIMEOUT-GAME_TIMETICK)));
 			GAME_RATIO_COUNT = GAME_RATIO_COUNT + 1;
 			GAME_RESULT = 1;
 			setButton(false);
 			getNewQuestion();
 		} else {
 			GAME_RESULT = 0;
 			stopTask();
 		}
 		updateScoreView();
 	}
   
 	public void stopTask() {
 		// TODO Auto-generated method stub
 		setButton(false);
 		timetask.cancel();
 		timeremain.cancel(); 
 		final AlertDialog.Builder adb = new AlertDialog.Builder(this);
 		adb.setCancelable(false);
 		adb.setTitle("Game Over!");
 		adb.setMessage("Your score is " + String.valueOf(GAME_SCORE)
 				+ " , you can do better than this, try again later.");
 		adb.setPositiveButton("OK I'm Noob", new AlertDialog.OnClickListener() {
 			public void onClick(DialogInterface dialog, int arg1) {
 				finish();
 			}
 		});
 		adb.show();
 	}
 
 	public void doTask() {
 		timetask = new TimerTask() {
 			@Override
 			public void run() {
 				// TODO Auto-generated method stub
 				handler.post(new Runnable() {
 					@Override
 					public void run() {						 
 						// check for timeout if not anwser then stop timer
 						// if (GAME_GETANSWER == 1) {
 						//     GAME_GETANSWER = 0;
 						//	 getNewQuestion();
 						// } else {
 						//	GAME_GETANSWER = 0;
 						//	 stopTask();
 						// }
						
 					}
 				});
 			}
 		};
 		timer.schedule(timetask, 0, (GAME_TIMEOUT * 1000));
 		
 		timeremain = new TimerTask() {
 			@Override
 			public void run() {
 				// TODO Auto-generated method stub
 				handler.post(new Runnable() {
 					@Override
					public void run() {
							Log.d("APP", "TICK " + GAME_TIMETICK + "=" + GAME_TIMEOUT);
							if (GAME_TIMETICK==GAME_TIMEOUT) {
								stopTask();
							}
 							setTimeOut(GAME_TIMETICK);							
 							GAME_TIMETICK++; 
 					}
 				});
 			}
 		};
 		timer.schedule(timeremain, 0, 1000);
 		
 	}
 
 	public void getNewQuestion() {
 		setButton(true);
 		GAME_TIMETICK=0;
 		// get question via level
 		getQuestion(GAME_LEVEL);
 		// set data
 		setData2View(GAME_QUESTION);
 		// set level
 		TextView textLevel = (TextView) findViewById(R.id.textLevel);
		textLevel.setText("L"+GAME_LEVEL+"-"+(GAME_RATIO_COUNT));
 	}
 
 	protected void updateScoreView() {
 		// TODO Auto-generated method stub
 		TextView textScore = (TextView) findViewById(R.id.textScore);
 		textScore.setText(String.valueOf(GAME_SCORE));
 		GAME_GETANSWER = 1; 
		Log.d("APP", "RATIO " + GAME_RATIO_COUNT + "=" + GAME_RATIO);
 		// check game ratio and change level
		if (GAME_RATIO == GAME_RATIO_COUNT) {
 			GAME_LEVEL = GAME_LEVEL + 1;
 			GAME_RATIO_COUNT = 0;			
 		}		
 		// disable button
 		// setButton(false);
 	}
 
 	@Override
 	protected void onDestroy() {
 		// TODO Auto-generated method stub
 		super.onDestroy();
 		timetask.cancel();
		timeremain.cancel(); 
 		finish();
 	}
 
 	@Override
 	protected void onStop() {
 		// TODO Auto-generated method stub
 		super.onStop();
 		timetask.cancel();
		timeremain.cancel(); 
 		finish();
 	}
 
 	private void setButton(Boolean value) {
 		// TODO noob code will change later it prevent pump score
 		Button buttonChoice1 = (Button) findViewById(R.id.button1);
 		Button buttonChoice2 = (Button) findViewById(R.id.button2);
 		Button buttonChoice3 = (Button) findViewById(R.id.button3);
 		Button buttonChoice4 = (Button) findViewById(R.id.button4);
 		Button buttonChoice5 = (Button) findViewById(R.id.button5);
 		Button buttonChoice6 = (Button) findViewById(R.id.button6);
 		Button buttonChoice7 = (Button) findViewById(R.id.button7);
 		Button buttonChoice8 = (Button) findViewById(R.id.button8);
 		Button buttonChoice9 = (Button) findViewById(R.id.button9);
 		Button buttonChoice10 = (Button) findViewById(R.id.button10);
 		// set enable or disable
 		buttonChoice1.setEnabled(value);
 		buttonChoice2.setEnabled(value);
 		buttonChoice3.setEnabled(value);
 		buttonChoice4.setEnabled(value);
 		buttonChoice5.setEnabled(value);
 		buttonChoice6.setEnabled(value);
 		buttonChoice7.setEnabled(value);
 		buttonChoice8.setEnabled(value);
 		buttonChoice9.setEnabled(value);
 		buttonChoice10.setEnabled(value);
 	}
 
 	public void setTimeOut(Integer val) {
 		// TODO Auto-generated method stub
 		TextView textTimeOut = (TextView) findViewById(R.id.textTimeOut);
 		textTimeOut.setText(String.valueOf((GAME_TIMEOUT-val)));
 	}
 
 	private void setData2View(String question) {		
 		// set text question
 		TextView textQuestion = (TextView) findViewById(R.id.textQuestion);		
 		textQuestion.setTypeface(face);
 		textQuestion.setText(question);
 		// set text timeout
 		TextView textTimeOut = (TextView) findViewById(R.id.textTimeOut);
 		textTimeOut.setTypeface(face);
 		textTimeOut.setText(String.valueOf(GAME_TIMEOUT));
 		// set game score
 		TextView textScore = (TextView) findViewById(R.id.textScore);
 		textScore.setTypeface(face);
 		textScore.setText(String.valueOf(GAME_SCORE));
 	}
 
 	private void getQuestion(Integer level) {
 		// TODO Auto-generated method stub
 		// if level more than 9 then set to 9 and change algorithms
 		if (level >= 9) level = 9;
 		Integer totalNumber = level + 1;
 
 	
 
 		// random number 0-9 for total number
 		Integer i, intVar, intSum = 0, intAnswer = 0;
 		String questionNumber = "";
 		for (i = 0; i < totalNumber; i++) {
 			intVar = (int) (Math.random() * 9);
 			if (intVar == 0)
 				intVar = (int) (Math.random() * 9);
 			intSum += intVar;
 			questionNumber += String.valueOf(intVar);
 		}
 		
 		// set question
 		this.GAME_QUESTION = questionNumber;
 		Log.d("APP", "Question : " + GAME_QUESTION);
 
 		// find answer check number of lenght
 		Integer lenghtSum;
 		lenghtSum = String.valueOf(intSum).length();
 		//Log.d("APP", "Sum Lenght : " + String.valueOf(intSum).length());
 		//Log.d("APP", "Sum : " + String.valueOf(intSum));
 		
 		while ((String.valueOf(intSum).length())==2) {		
 			String sumChar = String.valueOf(intSum);
 			intAnswer = Integer.parseInt(sumChar.substring(0, 1)) + Integer.parseInt(sumChar.substring(1, 2));
 			intSum=intAnswer;
 		}
 		Log.d("APP", "Answer >> " + String.valueOf(intSum));
 		this.GAME_ANSWER = intSum;
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_game, menu);
 		return true;
 	}
 }
