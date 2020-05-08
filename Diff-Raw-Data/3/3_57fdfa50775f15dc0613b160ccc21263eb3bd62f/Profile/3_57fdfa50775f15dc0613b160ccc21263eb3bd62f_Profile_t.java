 package edu.fsu.cs.group5socialnetwork;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.widget.RadioButton;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class Profile extends Activity {
 	int mQNum; 
 	TextView mQtv; 
 	RadioButton mAns1, mAns2, mAns3, mAns4; 
 	ArrayList<Question> mQuestion; 
 	int mProfileScore, mArticHighScore, mDesertHighScore, mForrestHighScore, mMountainHighScore, mSwampHighScore; 
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_profile);
 		
 		String currentQuiz = getIntent().getStringExtra("quizType");
		String textInfo = null;
 
 		// Retrieves everything from the text file
 		if (currentQuiz.equals("profileQuiz"))
 			textInfo = readTextFile("profile.txt");
 		else if (currentQuiz.equals("articQuiz"))
 			textInfo = readTextFile("bering.txt");
 		else if (currentQuiz.equals("desertQuiz"))
 			textInfo = readTextFile("desert.txt");
 		else if (currentQuiz.equals("forestQuiz"))
 			textInfo = readTextFile("forest.txt");
 		else if (currentQuiz.equals("swampQuiz"))
 			textInfo = readTextFile("swamp.txt");
 		else if (currentQuiz.equals("mountainQuiz"))
 			textInfo = readTextFile("mountain.txt");
 		// Parses the file and sets the question and answer
 		mQuestion = parseFileSetQuestionAndAnswer(textInfo);
 
 		// Everything is read and parsed, put into activity
 		mQtv = (TextView) findViewById(R.id.questionTV);
 		mAns1 = (RadioButton) findViewById(R.id.radioButton1);
 		mAns2 = (RadioButton) findViewById(R.id.radioButton2);
 		mAns3 = (RadioButton) findViewById(R.id.radioButton3);
 		mAns4 = (RadioButton) findViewById(R.id.radioButton4);
 		addQuestionAnswersToActivity();
 	}
 
 	public void addQuestionAnswersToActivity() {
 		int aNum = 0, totalPointsAwarded = 0;
 		// Error checking
 		if (mQNum < mQuestion.size()) {
 			mQtv.setText(mQuestion.get(mQNum).getQuestion());
 			mAns1.setText(mQuestion.get(mQNum).getAnswer(aNum));
 			mAns2.setText(mQuestion.get(mQNum).getAnswer(++aNum));
 			mAns3.setText(mQuestion.get(mQNum).getAnswer(++aNum));
 			mAns4.setText(mQuestion.get(mQNum).getAnswer(++aNum));
 
 			mAns1.setChecked(false);
 			mAns2.setChecked(false);
 			mAns3.setChecked(false);
 			mAns4.setChecked(false);
 		}
 		else {
 			// Calculate total points awarded
 			for (int i = 0; i < mQuestion.size(); i++)
 				totalPointsAwarded += mQuestion.get(i).mPoints_awarded;  
 			Toast.makeText(this, "This is your points awarded:" + totalPointsAwarded, Toast.LENGTH_SHORT).show();
 		}
 	}
 
 	// When answer radio button is pressed
 	public void ansClick(View v) {
 		if (v.getId() == mAns1.getId()) {
 			mAns2.setChecked(false);
 			mAns3.setChecked(false);
 			mAns4.setChecked(false);
 			mQuestion.get(mQNum).mPoints_awarded = mQuestion.get(mQNum).getAnswerVal(0);
 			Toast.makeText(this, "AnswerVal: " + mQuestion.get(mQNum).getAnswerVal(0), Toast.LENGTH_SHORT).show(); 
 		}
 		else if (v.getId() == mAns2.getId()) {
 			mAns1.setChecked(false);
 			mAns3.setChecked(false);
 			mAns4.setChecked(false);
 			mQuestion.get(mQNum).mPoints_awarded = mQuestion.get(mQNum).getAnswerVal(1);
 			Toast.makeText(this, "AnswerVal: " + mQuestion.get(mQNum).getAnswerVal(1), Toast.LENGTH_SHORT).show();
 		}
 		else if (v.getId() == mAns3.getId()) {
 			mAns1.setChecked(false);
 			mAns2.setChecked(false);
 			mAns4.setChecked(false);
 			mQuestion.get(mQNum).mPoints_awarded += mQuestion.get(mQNum).getAnswerVal(2);
 			Toast.makeText(this, "AnswerVal: " + mQuestion.get(mQNum).getAnswerVal(2), Toast.LENGTH_SHORT).show();
 		}
 		else if (v.getId() == mAns4.getId()) {
 			mAns1.setChecked(false);
 			mAns2.setChecked(false);
 			mAns3.setChecked(false);
 			mQuestion.get(mQNum).mPoints_awarded += mQuestion.get(mQNum).getAnswerVal(3);
 			Toast.makeText(this, "AnswerVal: " + mQuestion.get(mQNum).getAnswerVal(3), Toast.LENGTH_SHORT).show();
 		}
 	}
 
 	// When enter button is pressed
 	public void enterButton(View v) {
 		// Error checking
 		if (mAns1.isChecked() != false || mAns2.isChecked() != false || mAns3.isChecked() || false || mAns4.isChecked() != false) 
 			// Increment question
 			mQNum++;
 		else
 			Toast.makeText(getApplicationContext(), "Please select an answer.", Toast.LENGTH_SHORT).show();
 		addQuestionAnswersToActivity(); 
 	}
 
 	public ArrayList<Question> parseFileSetQuestionAndAnswer(String textInfo) {
 		ArrayList<Question> question = new ArrayList<Question>(); 
 		String[] questionAndAnswer; 
 		String answer; 
 		int pointValue, j = 0, questionCount = 0; 
 
 		// In the file the questions are separated by @ signs 
 		// First, split on the at signs to get the questions and answers together
 		String[] temp = textInfo.split("@");
 		for (int i = 0; i < temp.length; i++) {
 			// Add a new Question to the array list of questions
 			question.add(new Question());
 			// Split on the new lines to get question1 answer1 pointvalue1 a2 pv2 a3 pv3 a4 pv4
 			questionAndAnswer = temp[i].split("\n");
 			// The first array index holds the question, so this will always be 0
 			// Put the question in the question class
 			question.get(questionCount).setQuestion(questionAndAnswer[j]);
 			// Continue 0-7 (total of 8) times since there are 4 answers and each answer has a point value 
 			while (j < 8) {
 				// The one above the first array index holds the answer
 				answer = questionAndAnswer[++j];
 				// The index above the one with the answer holds the point value
 				pointValue = Integer.parseInt(questionAndAnswer[++j]);
 				// Add the answer and the point value to the question class
 				question.get(questionCount).addAnswer(answer, pointValue);
 			}
 			questionCount++;
 			j = 0; 
 		} 
 
 		return question;
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_profile, menu);
 		return true;
 	}
 
 	private String readTextFile(String fileName) {
 		BufferedReader in = null;
 
 		try {
 			String line;
 			in = new BufferedReader(new InputStreamReader(this.getAssets().open(fileName)));
 			final StringBuilder buffer = new StringBuilder();
 			while ((line = in.readLine()) != null)
 				buffer.append(line).append(System.getProperty("line.separator"));
 			return buffer.toString();
 		}
 		catch (final IOException e) {
 			return "";
 		}
 		finally
 		{
 			try {in.close();}
 			catch (IOException e) {
 				// ignore //
 			}
 		}
 	}
 }
