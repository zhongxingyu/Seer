 /**
  * 
  */
 package com.sjsu.siquoia;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 
 import com.sjsu.siquoia.adapters.SiQuoiaQuizAdapter;
 import com.sjsu.siquoia.model.Question;
 import com.sjsu.siquoia.model.SiQuoiaJSONParser;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 /**
  * @author Parnit Sainion
 * @since 28 November 2013
  * Description: This activity displays a list of questions the use can select to answer
  */
 public class QuizActivity extends Activity {
 
 	//declare variable
 	private ArrayList<Question> quiz;
 	private SiQuoiaQuizAdapter quizAdapter;
 	private ListView quizList;
 	private Question selectedQuestion;
 	private int selectedPosition;
 	private String currentAnswers;
 	private SharedPreferences preferences;
 	private int numberCorrect;
 	private TextView currentScoreTextView;
 	private String packetType;
 	private final int QUIZ_SIZE = 20;
 	private final String UPDATE_POINTS_URL ="http://ec2-54-201-65-140.us-west-2.compute.amazonaws.com/updateSiquoiaPoints.php";
     
 
 	@Override
     protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.quiz_list);
 		
 		//get textview
 		currentScoreTextView = (TextView) findViewById(R.id.currentScore);
 		
 		//get preferences
 		preferences = getSharedPreferences(SiQuoiaHomeActivity.SIQUOIA_PREF, 0);
 		
 		String quizJson = preferences.getString(SiQuoiaHomeActivity.QUIZ, "");
 		currentAnswers = preferences.getString(SiQuoiaHomeActivity.ANSWERS, "");
 		packetType = preferences.getString(SiQuoiaHomeActivity.PACKET_TYPE, "");
 		
 		//set number of  correct ANswers
 		setNumberCorrect();
 		currentScoreTextView.setText("Current Score: "+numberCorrect+"/20");
 		currentScoreTextView.refreshDrawableState();
 		
 		//parses the stored quiz into arraylist
 		quiz = SiQuoiaJSONParser.parseQuiz(quizJson,currentAnswers,packetType);
 		
 		//sets my listAdapter to the list
 		quizAdapter = new SiQuoiaQuizAdapter(this,R.layout.quiz_row,quiz);
 		quizList = (ListView) findViewById(R.id.quizList);
 		quizList.setAdapter(quizAdapter);
 		  
         //Creates the click listener
 		quizList.setOnItemClickListener(new AdapterView.OnItemClickListener() {           
 		
 
 			@Override
 			public void onItemClick(AdapterView<?> arg0, View view, int position,
 					long id) 
 			{
 				//gets the question at the clicked position
 				selectedQuestion = (Question) quizAdapter.getItem(position);
 				selectedPosition = position;
 				int nextQuestion = currentAnswers.length();
 				
 				if(selectedQuestion.getAnswered() == Question.UNANSWERED)
 				{					
 					if(nextQuestion != position) //check if selected question is the next question
 					{
 						Toast toast = Toast.makeText(getApplicationContext(), "Please Select Question " + ++nextQuestion, Toast.LENGTH_SHORT);
 						toast.show();
 					}
 					else{
 						//start next question
 						Intent intent = new Intent(QuizActivity.this, SiQuoiaQuestionActivity.class);
 						intent.putExtra("question", selectedQuestion);
 						intent.putExtra("selectedPosition", selectedPosition);
 						intent.putExtra(SiQuoiaHomeActivity.CURRENT_SCORE, numberCorrect);
 						final int intentMarker = 0;
 						startActivityForResult(intent, intentMarker);
 					}
 				}				
 			}
         });
 	}
 	
 	@Override
     public void onActivityResult(int requestCode,int resultCode, Intent data)
     {
 		super.onActivityResult(requestCode, resultCode, data);
 		int size;
 
 		//checks if data is not null (in case user pressed back)
 		if(data != null)
 		{
 			int answer = data.getIntExtra("chosenAnswer", 3);   
 	     	int position =data.getIntExtra("position", 101);
 			currentAnswers = preferences.getString(SiQuoiaHomeActivity.ANSWERS, "");
 	     	
 			size = currentAnswers.length();
 	     	if(position != 101)
 	     	{
 	     		//gets the question that was answered from list of questions
 	         	Question question = quiz.get(position);
 	         	
 	         	//changes the status of question to correct or incorrect
 	         	question.setStatus(answer);
 	         	
 	         	if(question.getAnswered() == Question.CORRECT)
 	         	{
 	         		//increment current score and change text
 	        		currentScoreTextView.setText("Current Score: "+ ++numberCorrect + "/20");
 	        		currentScoreTextView.refreshDrawableState();
 	         	}
 	         	
 	         	//updates GUI to reflect changes
 	         	quizAdapter.notifyDataSetChanged();	         	
 	     	}
 
 	     	//if all 20 questions have been answered
          	if(size == QUIZ_SIZE)
          	{
          		Toast toast = Toast.makeText(getApplicationContext(), "Quiz Complete", Toast.LENGTH_SHORT);
          		toast.show();
          		
          		if(preferences.getString(SiQuoiaHomeActivity.PACKET_TYPE, SiQuoiaHomeActivity.NORMAL).equalsIgnoreCase(SiQuoiaHomeActivity.NORMAL))
          		{
              		//update current scores
              		SharedPreferences.Editor perferenceUpdater = preferences.edit();
     				perferenceUpdater.putString(SiQuoiaHomeActivity.CURRENT_SCORE, currentAnswers);
     				perferenceUpdater.commit();
     				
    				
     				new SiQuoiaUpdatePointsTask().execute(preferences.getString(SiQuoiaHomeActivity.EMAIL,""), numberCorrect+"");    				
          		}
          	}
          	
 	     	/**
 	     	 * If more questions are available go directly to the next question.
 	     	 */
 	     	if(size < QUIZ_SIZE) 
 	     	{
 	     		//gets the question at the clicked position
 				selectedQuestion = (Question) quizAdapter.getItem(size);
 				selectedPosition = size;
 	     		
 	     		Intent intent = new Intent(QuizActivity.this, SiQuoiaQuestionActivity.class);
 				intent.putExtra("question", selectedQuestion);
 				intent.putExtra("selectedPosition", selectedPosition);
 				intent.putExtra(SiQuoiaHomeActivity.CURRENT_SCORE, numberCorrect);
 				final int intentMarker = 0;
 				startActivityForResult(intent, intentMarker);
 	     	}
 		}		
     }
 	
 	/**
 	 * Sets the number of correct answers based on Users current Answers
 	 */
 	public void setNumberCorrect()
 	{
 		numberCorrect = 0;
 		int size = currentAnswers.length();
 		String correct = Question.CORRECT + "";
 		
 		for(int count = 0; count < size ; count++ )
 		{			
 			if(correct.equals(currentAnswers.charAt(count)+""))
 			{
 				numberCorrect++;
 			}
 		}
 	}
 	
 	/**
 	 * updates user total points in data base
 	 * @param email email of user
 	 * @param input new points gained from current quiz
 	 */
 	public void updatePoints(String email, String input)
     {
     	//variables declared
     	HttpClient httpclient = new DefaultHttpClient();
     	HttpPost httppost = new HttpPost(UPDATE_POINTS_URL);
     	
     	try {
     		//add user information to post
         	List<NameValuePair> data = new ArrayList<NameValuePair>(1); 
            	
         	data.add(new BasicNameValuePair(SiQuoiaHomeActivity.EMAIL,email));
         	data.add(new BasicNameValuePair("points",input));
 			httppost.setEntity(new UrlEncodedFormEntity(data));
 			
 			httpclient.execute(httppost);
 			
 		} catch (UnsupportedEncodingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ClientProtocolException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}  
     }
 	
 	 /**
      * This is the background task that will update the user's SiQuoia Points from the database.
      * @author Parnit Sainion
      *
      */
     class SiQuoiaUpdatePointsTask extends AsyncTask<String, String, String>
     {
     	
     	@Override
 		protected String doInBackground(String... input) {
     		updatePoints(input[0],input[1]);
     		return "";
 		} 	
     }
 }
