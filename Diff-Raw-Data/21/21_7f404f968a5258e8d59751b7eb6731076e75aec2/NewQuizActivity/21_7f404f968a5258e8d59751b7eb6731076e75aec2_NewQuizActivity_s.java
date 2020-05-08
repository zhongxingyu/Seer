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
 import org.apache.http.client.ResponseHandler;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.BasicResponseHandler;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 
 import com.sjsu.siquoia.model.SiQuoiaJSONParser;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.Spinner;
 
 /**
  * @author Parnit Sainion
 * @since 25 November 2013
  * Description: THis activity allow the user to create a new quiz based on subject, topic, or subtopic.
  */
 public class NewQuizActivity extends Activity {
 	
 	//Declare variables
 	private ArrayList<String> subjects, topics, subtopics;
 	private Spinner subjectSpinner, topicSpinner, subtopicSpinner;
 	private ArrayAdapter <String> subjectAdapter, topicAdapter, subtopicAdapter ;
 	private Button createButton;
 	private NewQuizActivity newQuizActivity = this;
 	private ProgressDialog progressBar;
 	private final String SUBJECT = "subject";
 	private final String TOPIC = "topic";
 	private final String SUBTOPIC ="subtopic";
 	private String quiz;
 	
 	//url to get data from database
 	private final String  SUBJECT_URL = "http://ec2-54-201-65-140.us-west-2.compute.amazonaws.com/getSubject.php";
 	private final String  TOPIC_URL = "http://ec2-54-201-65-140.us-west-2.compute.amazonaws.com/getTopic.php";
 	private final String  SUBTOPIC_URL = "http://ec2-54-201-65-140.us-west-2.compute.amazonaws.com/getSubtopic.php";
 	private final String  QUIZ_URL = "http://ec2-54-201-65-140.us-west-2.compute.amazonaws.com/getQuestions.php";
 	
 	public void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);		
 		//set view
 		setContentView(R.layout.new_quiz_layout);
 		
 		//initialize arrays
 		subjects = new ArrayList<String>();
 		topics = new ArrayList<String>();
 		subtopics = new ArrayList<String>();
 		
 		//get different spinners and create button
 		subjectSpinner= (Spinner) findViewById(R.id.subjectSpinner);
 		topicSpinner= (Spinner) findViewById(R.id.topicSpinner);
 		subtopicSpinner= (Spinner) findViewById(R.id.subtopicSpinner);
 		createButton = (Button) findViewById(R.id.createQuizButton);
 		
 		//add initial values to arrays
 		new SiQuoiaGetInfoTask().execute(SUBJECT);
 		
 		//set listeners for  spinners
 		setItemChangeListeners();
 		
 		//create on click listener for the create button
 		createButton.setOnClickListener(new OnClickListener(){
 			@Override
 			public void onClick(View arg0) {
 				String subject = subjectSpinner.getSelectedItem().toString().trim();
 				String topic = topicSpinner.getSelectedItem().toString().trim();
 				String subtopic = subtopicSpinner.getSelectedItem().toString().trim();	
 				
 				//execute background task to get quiz data
 				new SiQuoiaGetQuizTask().execute(subject, topic, subtopic);
 			}
 			
 		});
 	}
 
 	
 	/**
 	 * Set on ItemChangeListener for three spinners
 	 */
 	public void setItemChangeListeners()
 	{
 		//set on item selected listener for the subjects
 		subjectSpinner.setOnItemSelectedListener(new OnItemSelectedListener(){
 			@Override
 			public void onItemSelected(AdapterView<?> arg0, View arg1,
 					int arg2, long arg3) {
 				String subject = subjectSpinner.getSelectedItem().toString();
 				
 				//if selected subject is not "Any", get topics for that subject
 				if(!subject.equalsIgnoreCase("Any"))
 				{
 					new SiQuoiaGetInfoTask().execute(TOPIC,subject);
 				}	
 				else //if selected subject is "Any", reset Topic and Subtopic lists
 				{
 					//set adapters topics
 					topics.clear();
 					topics.add("Any");
 					topicAdapter = new ArrayAdapter<String>(newQuizActivity, android.R.layout.simple_list_item_1, topics);
 					topicAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 					topicSpinner.setAdapter(topicAdapter);
 					
 					//set adapters subtopic
 					subtopics.clear();
 					subtopics.add("Any");
 					subtopicAdapter = new ArrayAdapter<String>(newQuizActivity, android.R.layout.simple_list_item_1, subtopics);
 					subtopicAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 					subtopicSpinner.setAdapter(subtopicAdapter);		
 				}
 			}
 
 			@Override
 			public void onNothingSelected(AdapterView<?> arg0) {
 				// TODO Auto-generated method stub
 				
 			}	
 		});
 		
 		//set on item selected listener for the topics
 		topicSpinner.setOnItemSelectedListener(new OnItemSelectedListener(){
 			@Override
 			public void onItemSelected(AdapterView<?> arg0, View arg1,
 					int arg2, long arg3) {
 				String topic = topicSpinner.getSelectedItem().toString();
 				
 				//if selected topic is  not "Any", get list of subtopics
 				if(!topic.equalsIgnoreCase("Any"))
 				{
 					new SiQuoiaGetInfoTask().execute(SUBTOPIC,subjectSpinner.getSelectedItem().toString(),topic);
 				}		
 				else //if selected topic is "Any"
 				{
 					//set adapters subtopic
 					subtopics.clear();
 					subtopics.add("Any");
 					subtopicAdapter = new ArrayAdapter<String>(newQuizActivity, android.R.layout.simple_list_item_1, subtopics);
 					subtopicAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 					subtopicSpinner.setAdapter(subtopicAdapter);	
 				}
 			}
 
 			@Override
 			public void onNothingSelected(AdapterView<?> arg0) {
 				// TODO Auto-generated method stub
 				
 			}			
 		});		
 	}
 	
 	/**
 	 * @return JSON string containing subjects
 	 */
 	public String getSubject()
     {
     	//variables declared
     	String message ="";
     	HttpClient httpclient = new DefaultHttpClient();
     	HttpPost httppost = new HttpPost(SUBJECT_URL);
     	
     	try {
     		//add user information to post
         	List<NameValuePair> data = new ArrayList<NameValuePair>(1);    	
         	data.add(new BasicNameValuePair("",""));
 			httppost.setEntity(new UrlEncodedFormEntity(data));
 						
 			ResponseHandler<String> handler = new BasicResponseHandler();
 			message = httpclient.execute(httppost,handler);
 			
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
     	return message;
     }
 	
 	/**
 	 * @param subject subjects whose topic are required
 	 * @return JSON containing topics for given subject
 	 */
 	public String getTopic(String subject)
     {
     	//variables declared
     	String message ="";
     	HttpClient httpclient = new DefaultHttpClient();
     	HttpPost httppost = new HttpPost(TOPIC_URL);
     	
     	try {
     		//add user information to post
         	List<NameValuePair> data = new ArrayList<NameValuePair>(1);    	
         	data.add(new BasicNameValuePair(SUBJECT,subject));
 			httppost.setEntity(new UrlEncodedFormEntity(data));
 			
 			ResponseHandler<String> handler = new BasicResponseHandler();
 			message = httpclient.execute(httppost,handler);
 			
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
     	return message;
     }
 	
 	/**
 	 * Gets subtopics based on subject and topi
 	 * @param subject over-arching subject whose subtopics are desired
 	 * @param topic topics whose subtopics are desired
 	 * @return JSON contain subtopics based on subject and topic
 	 */
 	public String getSubtopic(String subject, String topic)
     {
     	//variables declared
     	String message ="";
     	HttpClient httpclient = new DefaultHttpClient();
     	HttpPost httppost = new HttpPost(SUBTOPIC_URL);
     	
     	try {
     		//add user information to post
         	List<NameValuePair> data = new ArrayList<NameValuePair>(2);    	
         	data.add(new BasicNameValuePair(SUBJECT,subject));
         	data.add(new BasicNameValuePair(TOPIC,topic));
 			httppost.setEntity(new UrlEncodedFormEntity(data));
 						
 			ResponseHandler<String> handler = new BasicResponseHandler();
 			message = httpclient.execute(httppost,handler);
 			
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
     	return message;
     }
 	
 	/**
 	 * gets quiz based on subject, topic and subtopic
 	 * @param subject subject of quiz
 	 * @param topic topic of quiz
 	 * @param subtopic subtopic of quiz
 	 * @return JSON  of quiz based on subject, topic and subtopic
 	 */
 	public String getQuiz(String subject, String topic, String subtopic)
     {
     	//variables declared
     	HttpClient httpclient = new DefaultHttpClient();
     	HttpPost httppost = new HttpPost(QUIZ_URL);
     	
     	//get preferences
 		SharedPreferences preferences = getSharedPreferences(SiQuoiaHomeActivity.SIQUOIA_PREF, 0);
     	
     	try {
     		//add user information  and subject, topic, and subtopic to post
         	List<NameValuePair> data = new ArrayList<NameValuePair>(3);    	
         	data.add(new BasicNameValuePair(SUBJECT,subject));
         	data.add(new BasicNameValuePair(TOPIC,topic));
         	data.add(new BasicNameValuePair(SUBTOPIC,subtopic));
         	data.add(new BasicNameValuePair(SiQuoiaHomeActivity.EMAIL,preferences.getString(SiQuoiaHomeActivity.EMAIL, "")));
         	data.add(new BasicNameValuePair(SiQuoiaHomeActivity.PACKET_TYPE, SiQuoiaHomeActivity.NORMAL));
 			httppost.setEntity(new UrlEncodedFormEntity(data));
 				
 			ResponseHandler<String> handler = new BasicResponseHandler();
 			quiz = httpclient.execute(httppost,handler);
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
     	return quiz;
     }
 	
     /**
      * This is the background task that will get either the subject,topic or sub-topics from the database.
      * @author Parnit Sainion
      *
      */
     class SiQuoiaGetInfoTask extends AsyncTask<String, String, String>
     {
     	String task;
     	@Override
 		protected void onPreExecute() {
 			//create the progress dialog and display it
     		progressBar = new ProgressDialog(NewQuizActivity.this);
 			progressBar.setIndeterminate(true);
 			progressBar.setCancelable(false);
 			progressBar.setMessage("Please Wait");
 			progressBar.show();			
 		}
     	
     	@Override
 		protected String doInBackground(String... input) {
     		task = input[0];
     		if(task.equalsIgnoreCase(SUBJECT))
     			return getSubject();
     		else if(task.equalsIgnoreCase(TOPIC))
     			return getTopic(input[1]);
     		else
     			return getSubtopic(input[1],input[2]);
 		}
 		
 		protected void onPostExecute(String result) {
 
 				if(task.equalsIgnoreCase(SUBJECT)) //case where subject is changeg
     			{
 					//set subject topics
 					subjects = SiQuoiaJSONParser.parseSubject(result);
 					subjectAdapter = new ArrayAdapter<String>(newQuizActivity, android.R.layout.simple_list_item_1, subjects);
 					subjectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 					subjectSpinner.setAdapter(subjectAdapter);
 					
 					//set adapters topics
 					topics.clear();
 					topics.add("Any");
 					topicAdapter = new ArrayAdapter<String>(newQuizActivity, android.R.layout.simple_list_item_1, topics);
 					topicAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 					topicSpinner.setAdapter(topicAdapter);
 					
 					//set adapters subtopic
 					subtopics.clear();
 					subtopics.add("Any");
 					subtopicAdapter = new ArrayAdapter<String>(newQuizActivity, android.R.layout.simple_list_item_1, subtopics);
 					subtopicAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 					subtopicSpinner.setAdapter(subtopicAdapter);					
     			}
 	    		else if(task.equalsIgnoreCase(TOPIC)) //case where topic is changed
 	    		{
 	    			topics = SiQuoiaJSONParser.parseTopics(result);
 	    			topicAdapter = new ArrayAdapter<String>(newQuizActivity, android.R.layout.simple_list_item_1, topics);
 	    			topicAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 	    			topicSpinner.setAdapter(topicAdapter);
 	    			
 	    			subtopics.clear();
 	    			subtopics.add("Any");
 	    			subtopicAdapter = new ArrayAdapter<String>(newQuizActivity, android.R.layout.simple_list_item_1, subtopics);
 	    			subtopicAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 	    			subtopicSpinner.setAdapter(subtopicAdapter);
 	    		}
 	    		else //case where subtopic is changed
 	    		{ 
 	    			subtopics = SiQuoiaJSONParser.parseSubtopics(result);
 	    			subtopicAdapter = new ArrayAdapter<String>(newQuizActivity, android.R.layout.simple_list_item_1, subtopics);
 	    			subtopicAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 	    			subtopicSpinner.setAdapter(subtopicAdapter);
 	    		}
 			
 				//close progress dialog
 				progressBar.dismiss();
 		}    	
     }
     
     /**
      * This task gets a new quiz for the user based on user input of subject, topic, subtopic.
      *  @author Parnit Sainion
      *
      */
     class SiQuoiaGetQuizTask extends AsyncTask<String, String, String>
     {
     	String task;
     	@Override
 		protected void onPreExecute() {
 			//create the progress dialog and display it
     		progressBar = new ProgressDialog(NewQuizActivity.this);
 			progressBar.setIndeterminate(true);
 			progressBar.setCancelable(false);
 			progressBar.setMessage("Please Wait");
 			progressBar.show();			
 		}
     	
     	@Override
 		protected String doInBackground(String... input) {
     		return getQuiz(input[0],input[1],input[2]);
 		}
 		
 		protected void onPostExecute(String result) 
 		{
 				//get preferences
 				SharedPreferences preferences = getSharedPreferences(SiQuoiaHomeActivity.SIQUOIA_PREF, 0);
 				
 				//update user's quiz
 				SharedPreferences.Editor perferenceUpdater = preferences.edit();
 				perferenceUpdater.putString(SiQuoiaHomeActivity.QUIZ, quiz);
 				perferenceUpdater.putString(SiQuoiaHomeActivity.ANSWERS, "");
 			
 				//commit preference changes
 				perferenceUpdater.commit();
 			
 				//close progress dialog
 				progressBar.dismiss();
 				
 				Intent intent = new Intent();
 				intent.setClass(newQuizActivity, QuizActivity.class);
 				startActivity(intent);
 				finish();
 		}    	
     }
 }
