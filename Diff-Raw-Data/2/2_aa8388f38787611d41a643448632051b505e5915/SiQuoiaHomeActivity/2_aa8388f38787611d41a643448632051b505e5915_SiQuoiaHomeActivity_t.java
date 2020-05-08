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
 import com.sjsu.siquoia.model.User;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 
 /**
  * @author Parnit Sainion
  * @since 26 November 2013
  * Description: This app is the home landing screen for the app. Users can: continue a previous quiz, start a new quiz,
  * 				check the leader-board, submit a question to put into the same, and quit the app.
  *
  */
 public class SiQuoiaHomeActivity extends Activity {
 	
 	//Variable Declaration
 	private Button continueButton, newGameButton, leaderboardButton, submitQuestionButton, quitButton;
 	private ProgressDialog progressBar;
 	private SharedPreferences preferences;
 	private final String USER_INFO_URL = "http://ec2-54-201-65-140.us-west-2.compute.amazonaws.com/getUser.php";
 	private final String REDEEM_CODE_URL = "http://ec2-54-201-65-140.us-west-2.compute.amazonaws.com/getBrandedQuestion.php";
 	private TextView currentPointsTextView; 
 	protected User user;
 	private AlertDialog alertDialog;
 	
 	//preferences
 	protected static final String SIQUOIA_PREF = "SiquoiaPref";
 	protected static final String LOGGED_IN = "loggedIn";
 	protected static final String NEW_USER = "newUser";
 	protected static final String EMAIL = "email";
 	protected static final String QUIZ = "currentQuiz";
 	protected static final String ANSWERS = "currentAnswers";
 	protected static final String CURRENT_SCORE = "currentScore";
 	protected static final String QUESTION_TEXT = "questionText";
 	protected static final String PACKET_TYPE = "packetType";
 	protected static final String NORMAL = "normal";
 	protected static final String BRANDED = "branded";
 	
 	@Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.home_screen);
         
         //initialize buttons from view
         continueButton = (Button) findViewById(R.id.continueButton);
         newGameButton = (Button) findViewById(R.id.newGameButton);
         leaderboardButton = (Button) findViewById(R.id.leaderboardButton);
         submitQuestionButton = (Button) findViewById(R.id.submitQuesButton);
         quitButton = (Button) findViewById(R.id.quitButton);
         currentPointsTextView = (TextView) findViewById(R.id.currentPointsText);
         
        //get users info from app
         preferences = getSharedPreferences(SiQuoiaHomeActivity.SIQUOIA_PREF, 0);
         
         Intent intent = getIntent();
         boolean newUser = intent.getBooleanExtra(SiQuoiaHomeActivity.NEW_USER, true);
         
         if(newUser) //if user is a new user
         {
         	String email = intent.getStringExtra(SiQuoiaHomeActivity.EMAIL);
         	user =  new User(email);
         }
         {
         	//get user info from database
         	new SiQuoiaGetUserTask().execute(preferences.getString(EMAIL, EMAIL));
         }        
         
         //Set Listener for continue
         continueButton.setOnClickListener(new OnClickListener(){
 			@Override
 			public void onClick(View arg0) {			
 				if(!preferences.getString(QUIZ, "").equalsIgnoreCase("")&&!preferences.getString(QUIZ, "").equalsIgnoreCase("[]"))
 				{
 					Intent intent = new Intent();
 					intent.setClass(SiQuoiaHomeActivity.this, QuizActivity.class);
 					startActivity(intent);
 				}
 				else
 				{
 					Toast toast = Toast.makeText(getApplicationContext(), "No Saved Quiz", Toast.LENGTH_SHORT);
 					toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
 					toast.show();
 				}				
 			}        	
         });
         
       //Set Listener for newGameButton
         newGameButton.setOnClickListener(new OnClickListener(){
 			@Override
 			public void onClick(View arg0) {
 				showNewGameAlert();
 			}        	
         });
         
       //Set Listener for leaderboardButton
         leaderboardButton.setOnClickListener(new OnClickListener(){
 			@Override
 			public void onClick(View arg0) {
 				Log.i("homeScreenButtons", "leaderboardButton clicked");				
 			}        	
         });
         
         //Set Listener for submitQuestionButton
         submitQuestionButton.setOnClickListener(new OnClickListener(){
 			@Override
 			public void onClick(View arg0) {
 				Log.i("homeScreenButtons", "submitQuestionButton clicked");				
 			}        	
         });
         
       //Set Listener for quitButton
         quitButton.setOnClickListener(new OnClickListener(){
 			@Override
 			public void onClick(View arg0) {
 				//closes the application
 				finish();				
 			}        	
         });
     }
 	
 	/**
 	 * Displays a alert dialog for user to confirm creating a new game
 	 */
 	public void showNewGameAlert()
 	{
 		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
 		
 		alertDialogBuilder.setTitle("Start New Game");
 		
		alertDialogBuilder.setMessage("A new game costs 5 SiQuoia points and will override any uncompleted Quiz. Do you want to start a new Quiz?");
 		alertDialogBuilder.setCancelable(false);
 		
 		//create "Yes" button
 		alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
 			
 			@Override
 			public void onClick(DialogInterface dialog, int which) {						
 				if(user.buyPacket())
 				{
 					//if user has enough points to buy quiz, start New Quiz Activity
 					Intent intent = new Intent();
 					intent.setClass(SiQuoiaHomeActivity.this, NewQuizActivity.class);
 					startActivity(intent);
 				}
 				else
 				{
 					Toast toast = Toast.makeText(getApplicationContext(), "Not Enough SiQuoia Points", Toast.LENGTH_SHORT);
 					toast.show();
 				}
 			}
 		});
 		
 		//create "No" button
 		alertDialogBuilder.setNegativeButton("No",  new DialogInterface.OnClickListener() {
 
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				dialog.cancel();						
 			}					
 		});				
 		
 		//create and display alert dialog
 		alertDialog = alertDialogBuilder.create();
 		alertDialog.show();	
 	}
 
 	/**
 	 * Displays a alert dialog for user to redeem a code
 	 */
 	public void showRedeemCodeAlert()
 	{
 		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
 		
 		alertDialogBuilder.setTitle("Redeem Code");
 		
 		alertDialogBuilder.setMessage("Redeeming a code and will override any uncompleted Quiz. Do you want to redeem a code?");
 		alertDialogBuilder.setCancelable(false);
 		
 		final EditText userInput = new EditText(this);
 		alertDialogBuilder.setView(userInput);
 		
 		//create "Yes" button
 		alertDialogBuilder.setPositiveButton("Enter Code", new DialogInterface.OnClickListener() {
 			
 			@Override
 			public void onClick(DialogInterface dialog, int which) {						
 					String input= userInput.getText().toString();
 					
 					new SiQuoiaRedeemCodeTask().execute(input);
 			}
 		});
 		
 		//create "No" button
 		alertDialogBuilder.setNegativeButton("Cancel",  new DialogInterface.OnClickListener() {
 
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				dialog.cancel();						
 			}					
 		});				
 		
 		//create and display alert dialog
 		alertDialog = alertDialogBuilder.create();
 		alertDialog.show();	
 	}
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.main_menu, menu);
         return true;
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item)
     {
 		switch(item.getItemId())
 		{
 			//user wants to log out
 			case R.id.action_logout:
 				//update user info
 				SharedPreferences preferences = getSharedPreferences(SiQuoiaHomeActivity.SIQUOIA_PREF, 0);
 				SharedPreferences.Editor perferenceUpdater = preferences.edit();
 				perferenceUpdater.putBoolean(SiQuoiaHomeActivity.LOGGED_IN, false);
 				perferenceUpdater.putString(SiQuoiaHomeActivity.EMAIL, "");
 				perferenceUpdater.putString(SiQuoiaHomeActivity.QUIZ, "");
 				perferenceUpdater.putString(SiQuoiaHomeActivity.ANSWERS, "");
 				perferenceUpdater.putString(SiQuoiaHomeActivity.PACKET_TYPE, "");
 				
 				
 				//commit preference changes
 				perferenceUpdater.commit();
 				
 				//finish this activity and take user to login screen
 				Intent intent = new Intent();
 	        	intent.setClass(SiQuoiaHomeActivity.this, SiQuoiaLoginActivity.class);
 	        	startActivity(intent);
 	        	finish();
 				break;
 				
 			//user is redeeming a code
 			case R.id.action_redeem:
 				showRedeemCodeAlert();
 				break;
 				
 			default:
 				break;
 		}
     	return false;    	
     }    
     
     @Override
     public void onResume()
     {
     	super.onResume();
     	
     	//set Current points
     	if(currentPointsTextView != null && user != null)
         currentPointsTextView.setText("Current Points: " + user.getSiquoiaBucks());
     }
     
     /**
      * get user's information from the database
      * @param email user's email
      * @return user information or nothing
      */
     public String getUser(String email)
     {
     	//variables declared
     	String message ="";
     	HttpClient httpclient = new DefaultHttpClient();
     	HttpPost httppost = new HttpPost(USER_INFO_URL);
     	
     	try {
     		//add user information to post
         	List<NameValuePair> data = new ArrayList<NameValuePair>(1);    	
         	data.add(new BasicNameValuePair("email",email));
 			httppost.setEntity(new UrlEncodedFormEntity(data));
 			
 			//set up response handler and execute post
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
      * @param code code to redeem
      * @return returns JSOn containing branded quiz
      */
     public String redeemCode(String code)
     {
     	//variables declared
     	String message ="";
     	HttpClient httpclient = new DefaultHttpClient();
     	HttpPost httppost = new HttpPost(REDEEM_CODE_URL);
     	
     	try {
     		//add user information to post
         	List<NameValuePair> data = new ArrayList<NameValuePair>(1);    	
         	data.add(new BasicNameValuePair("code", code));
         	data.add(new BasicNameValuePair(EMAIL, user.getEmail()));
 			httppost.setEntity(new UrlEncodedFormEntity(data));
 			
 			//set up response handler and execute post
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
      * This is the background task that will get the user's current information from the database.
      * @author Parnit Sainion
      *
      */
     class SiQuoiaGetUserTask extends AsyncTask<String, String, String>
     {
     	@Override
 		protected void onPreExecute() {
 			//create the progress dialog and display it
     		progressBar = new ProgressDialog(SiQuoiaHomeActivity.this);
 			progressBar.setIndeterminate(true);
 			progressBar.setCancelable(false);
 			progressBar.setMessage("Getting User Info");
 			progressBar.show();			
 		}
     	
     	@Override
 		protected String doInBackground(String... input) {
     		//input[0] = username
 			return getUser(input[0]);
 		}
 		
 		protected void onPostExecute(String result) {		
 				user = SiQuoiaJSONParser.parseUser(result);					
 			
 				//update user's quiz
 				preferences = getSharedPreferences(SiQuoiaHomeActivity.SIQUOIA_PREF, 0);
 				SharedPreferences.Editor perferenceUpdater = preferences.edit();
 				perferenceUpdater.putString(QUIZ, user.getCurrentQuiz());
 				perferenceUpdater.putString(ANSWERS, user.getAnswers());
 				perferenceUpdater.putString(PACKET_TYPE, user.getPacketType());
 				perferenceUpdater.commit();
 
 		        //set Current points
 		        currentPointsTextView.setText("Current Points: " + user.getSiquoiaBucks());
 				
 				//close progress dialog
 				progressBar.dismiss();
 		}    	
     }
     
     /**
      * This is the background task that will get the user's current information from the database.
      * @author Parnit Sainion
      *
      */
     class SiQuoiaRedeemCodeTask extends AsyncTask<String, String, String>
     {
     	@Override
 		protected void onPreExecute() {
 			//create the progress dialog and display it
     		progressBar = new ProgressDialog(SiQuoiaHomeActivity.this);
 			progressBar.setIndeterminate(true);
 			progressBar.setCancelable(false);
 			progressBar.setMessage("Redeeming Code");
 			progressBar.show();			
 		}
     	
     	@Override
 		protected String doInBackground(String... input) {
     		//input[0] = code
 			return redeemCode(input[0]);
 		}
 		
 		protected void onPostExecute(String result) {		
 			
 				if(!result.equalsIgnoreCase("[]"))
 				{
 					//update user's quiz
 					preferences = getSharedPreferences(SiQuoiaHomeActivity.SIQUOIA_PREF, 0);
 					SharedPreferences.Editor perferenceUpdater = preferences.edit();
 					perferenceUpdater.putString(QUIZ, result);
 					perferenceUpdater.putString(ANSWERS, "");
 					
 					//set packet type to Branded
 					perferenceUpdater.putString(PACKET_TYPE, BRANDED);
 					
 					perferenceUpdater.commit();
 					
 					Intent intent = new Intent();
 					intent.setClass(SiQuoiaHomeActivity.this, QuizActivity.class);
 					startActivity(intent);
 				}	
 				else{
 					Toast toast = Toast.makeText(getApplicationContext(), "Incorrect Code", Toast.LENGTH_SHORT);
 					toast.show();
 				}
 				
 				//close progress dialog
 				progressBar.dismiss();
 		}    	
     }
 }
