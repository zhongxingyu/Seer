 package com.flashmath.activity;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Calendar;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.ActionBar;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.Bitmap.CompressFormat;
 import android.graphics.Color;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.view.Window;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.activeandroid.query.Select;
 import com.codepath.oauth.OAuthLoginActivity;
 import com.flashmath.R;
 import com.flashmath.models.OfflineScore;
 import com.flashmath.models.Question;
 import com.flashmath.models.UserSetting;
 import com.flashmath.network.FlashMathClient;
 import com.flashmath.network.TwitterClient;
 import com.flashmath.util.ColorUtil;
 import com.flashmath.util.ConnectivityUtil;
 import com.flashmath.util.Constants;
 import com.flashmath.util.ResultUtil;
 import com.flashmath.util.SoundUtil;
 import com.jjoe64.graphview.CustomLabelFormatter;
 import com.jjoe64.graphview.GraphView;
 import com.jjoe64.graphview.GraphView.GraphViewData;
 import com.jjoe64.graphview.GraphViewSeries;
 import com.jjoe64.graphview.GraphViewSeries.GraphViewSeriesStyle;
 import com.jjoe64.graphview.GraphViewStyle;
 import com.jjoe64.graphview.LineGraphView;
 import com.loopj.android.http.JsonHttpResponseHandler;
 
 public class ResultActivity extends OAuthLoginActivity<TwitterClient> {
 
 	public static final String SUBJECT_INTENT_KEY = "subject";
 	private ArrayList<Question> resultList;
 	private int score = 0;
 	private String subject;
 	private LinearLayout llStats;
 	private TextView tvTotal;
 	private TextView tvScore;
 	private TextView tvSubject;
 	private Button btnMainMenu;
 	private ImageView ivProfilePicture;
 	private UserSetting currentUserSettings;
 	private boolean wentThroughTwitterFlow = false;
 	private boolean isMockQuiz;
 	private TextView etProfileName;
 	
 	@SuppressWarnings("unchecked")
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
 		
 		setContentView(R.layout.activity_result);
 		wentThroughTwitterFlow = false;
 		llStats = (LinearLayout) findViewById(R.id.llStats);
 		tvTotal = (TextView) findViewById(R.id.tvTotal);
 		tvScore = (TextView) findViewById(R.id.tvScore);
 		tvSubject = (TextView) findViewById(R.id.tvSubject);
 		btnMainMenu = (Button) findViewById(R.id.btnMainMenu);
 		ivProfilePicture = (ImageView) findViewById(R.id.ivProfile);
 		etProfileName = (TextView) findViewById(R.id.tvProfileName);
 		
 		if (savedInstanceState == null) {
 			resultList = (ArrayList<Question>) getIntent().getSerializableExtra("QUESTIONS_ANSWERED");
 			subject = getIntent().getStringExtra(SUBJECT_INTENT_KEY);
 			isMockQuiz = getIntent().getBooleanExtra(QuestionActivity.IS_MOCK_QUIZ_INTENT_KEY, true);
 			evaluate();
 		}
 		
 		
 		retrieveUserProfileDetails();
 		
 	}
 
 	public void retrieveUserProfileDetails() {
 		ArrayList<UserSetting> currentUserSettingsObjects = new Select().from(UserSetting.class).execute();
 		if (currentUserSettingsObjects != null && currentUserSettingsObjects.size() > 0) {
 			currentUserSettings = currentUserSettingsObjects.get(0);
 		} 
 		
 		if (currentUserSettings != null && currentUserSettings.getUserProfileImageBitmapURI() != null) {
 			//set the picture of the user here
 			ivProfilePicture.setImageURI(Uri.parse(currentUserSettings.getUserProfileImageBitmapURI()));
		} else if(currentUserSettings != null && currentUserSettings.getUserName() != null && !currentUserSettings.getUserName().isEmpty())  {
 			//set user name if it has been set
 			etProfileName.setText(currentUserSettings.getUserName());
 		}
 	}
 
 	private void playSounds(float pc) {
 		if (pc >= .8) {
 			SoundUtil.playSound(this, 3);
 		} else if (pc >= .5) {
 			SoundUtil.playSound(this, 2);
 		} else {
 			SoundUtil.playSound(this, 1);
 		}
 		
 		
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.result, menu);
 		return true;
 	}
 	
 	public void evaluate(){
 		//if (resultList != null) {
 		ActionBar ab = getActionBar();
 		ab.setIcon(ColorUtil.getBarIcon(subject, this));
 		for(int i = 0; i < resultList.size(); i++){
 			String correctAnswer = resultList.get(i).getCorrectAnswer();
 			if (resultList.get(i).getUserAnswer().equals(correctAnswer)){
 				score++;
 			}
 		}
 		Button btnTweet = (Button) findViewById(R.id.btnTweet);
 		btnTweet.setBackground(ColorUtil.getButtonStyle(subject, this));
 		btnMainMenu.setBackground(ColorUtil.getButtonStyle(subject, this));
 		tvScore.setText(String.valueOf(score));
 		tvScore.setTextColor(ColorUtil.getScoreColor((float) score / resultList.size()));
 		tvTotal.setText("/ " + String.valueOf(resultList.size()));
 		String subjectTitle = Character.toUpperCase(subject.charAt(0))+subject.substring(1);
 		ab.setTitle(subjectTitle + " Results");
 		
 		tvSubject.setText(" Score History for " + subjectTitle + " ");
 		tvSubject.setBackgroundColor(ColorUtil.subjectColorInt(subject));
 		tvSubject.setTextColor(Color.WHITE);
 		FlashMathClient client = FlashMathClient.getClient(this);
 		
 		boolean isConnectionAvailable = ConnectivityUtil.isInternetConnectionAvailable(this.getApplicationContext());
 		
 		//Real quiz && Internet is available
 		if (!isMockQuiz) {
 			if (isConnectionAvailable) {
 				setProgressBarIndeterminateVisibility(true);
 				btnMainMenu.setEnabled(false);
 				
 				client.putScore(subject, String.valueOf(score), new JsonHttpResponseHandler() {
 					@Override
 					public void onSuccess(JSONArray jsonScores) {
 						if (jsonScores != null && jsonScores.length() > 1) {
 							GraphViewData[] data = new GraphViewData[jsonScores.length()];
 							int max_score = 1;
 							for (int i = 0; i < jsonScores.length(); i++) {
 								try {
 									int val = jsonScores.getJSONObject(i).getInt("value");
 									max_score = val > max_score ? val : max_score;
 									data[i] = (new GraphViewData(i + 1, val));
 								} catch (JSONException e) {
 									e.printStackTrace();
 								}
 							}
 							GraphView graphView = new LineGraphView(ResultActivity.this, "");
 							graphView.setCustomLabelFormatter(new CustomLabelFormatter.IntegerOnly());
 							GraphViewStyle style = new GraphViewStyle();
 							int datapoints = data.length;
 							while (datapoints >= 10) {
 								datapoints /= 2;
 							}
 							style.setNumHorizontalLabels(Math.max(2, datapoints));
 							style.setVerticalLabelsColor(Color.BLACK);
 							style.setHorizontalLabelsColor(Color.BLACK);
 							style.setGridColor(Color.GRAY);
 							GraphViewSeriesStyle lineStyle = new GraphViewSeriesStyle(ColorUtil.subjectColorInt(subject), 5);
 							graphView.addSeries(new GraphViewSeries("Scores", lineStyle, data));
 							graphView.addSeries(new GraphViewSeries(new GraphViewData[] { new GraphViewData(1, 0) }));
 							graphView.addSeries(new GraphViewSeries(new GraphViewData[] { new GraphViewData(2, 3) }));
 							graphView.setGraphViewStyle(style);
 							llStats.addView(graphView);
 						} else {
 							ResultUtil.showAlternativeTextForGraph(Constants.NEED_TO_TAKE_MORE_TESTS, getApplicationContext(), llStats);
 						}
 						
 						setProgressBarIndeterminateVisibility(false);
 						btnMainMenu.setEnabled(true);
 					}
 					
 					@Override
 					public void onFailure(Throwable arg0, JSONObject errorResponse) {
 						super.onFailure(arg0, errorResponse);
 						setProgressBarIndeterminateVisibility(false);
 						btnMainMenu.setEnabled(true);
 						ResultUtil.showAlternativeTextForGraph("Could not load historical data. Please try again later!", getApplicationContext(), llStats);
 					}
 				});
 			} else {
 				// Real quiz but no Internet
 				OfflineScore os = new OfflineScore();
 				os.setScore(score);
 				os.setSubject(subject);
 				
 				Calendar c = Calendar.getInstance(); 
 				os.setTimeStampInSeconds(c.get(Calendar.SECOND));
 				//ConnectivityUtil.setUnsentScore(os);
 				os.save();
 				ResultUtil.showAlternativeTextForGraph("Your results will be submitted when internet connection is back.", getApplicationContext(), llStats);
 			}
 		} else {
 			if(!isConnectionAvailable) {
 				//Mock quiz and no Internet
 				ResultUtil.showAlternativeTextForGraph("Thank you for completing the offline quiz! Your score will not be submitted.", getApplicationContext(), llStats);
 			} else {
 				//Mock quiz and Internet
 				ResultUtil.showAlternativeTextForGraph("Thank you for completing the offline quiz! You can try a real quiz in the Main Menu.", getApplicationContext(), llStats);
 			}
 		}
 		playSounds((float) score / resultList.size());
 	}
 	
 	public void tweetScore(View v) {
 		if (ConnectivityUtil.isInternetConnectionAvailable(this)) {
 			if (!getClient().isAuthenticated()) {
 				getClient().connect();
 				wentThroughTwitterFlow = true;
 			} else {
 				tweet();
 			}
 		} else {
 			Toast.makeText(this, ConnectivityUtil.INTERNET_CONNECTION_IS_NOT_AVAILABLE, Toast.LENGTH_SHORT).show();
 		}
 	}
 	
 	private void tweet() {
 		getClient().sendTweetWithImage(new JsonHttpResponseHandler() {
 			@Override
 			public void onSuccess(JSONObject object) {
 				Toast.makeText(ResultActivity.this, "Sent tweet \"" + getTweet((float) score / resultList.size()) + "\"", Toast.LENGTH_LONG).show();
 			}
 			
 			@Override
 			public void onFailure(Throwable e, JSONObject errorResponse) {
 				super.onFailure(e, errorResponse);
 				e.printStackTrace();
 				Toast.makeText(ResultActivity.this, errorResponse.toString(), Toast.LENGTH_SHORT).show();
 			}
 		}, getTweet((float) score / resultList.size()), getScreenShotFile()
 		);
 	}
 	
 	
 	private InputStream getScreenShotFile() {
 		// create bitmap screen capture
 		Bitmap bitmap;
 		View resultView = findViewById(R.id.rlResult).getRootView();
 		resultView.setDrawingCacheEnabled(true);
 		bitmap = Bitmap.createBitmap(resultView.getDrawingCache());
 		resultView.setDrawingCacheEnabled(false);
 
 		ByteArrayOutputStream stream = new ByteArrayOutputStream();
 		bitmap.compress(CompressFormat.JPEG, 100, stream);
 		InputStream is = new ByteArrayInputStream(stream.toByteArray());
         
 		return is;
 	}
 
 	public String getTweet(float pc) {
 		if (pc >= .8) {
 			return "Look Ma! I passed " + subject + " with a " + String.format("%.0f", pc * 100) + "%.";
 		} else if (pc >= .5) {
 			return "Alas, I am mortal! I barely passed " + subject + " with a " + String.format("%.0f", pc * 100) + "%.";
 		} else {
 			return "I have brought shame to my family. I failed " + subject + " with a " + String.format("%.0f", pc * 100) + "%.";
 		}
 	}
 
 	@Override
 	public void onLoginSuccess() {
 		if (wentThroughTwitterFlow) {
 			Toast.makeText(this, "Success! You can tweet your score now!", Toast.LENGTH_LONG).show();
 		}
 	}
 
 	@Override
 	public void onLoginFailure(Exception e) {
 		Toast.makeText(ResultActivity.this, "Error logging into Twitter!", Toast.LENGTH_SHORT).show();
 	}
 	
 	public void onTakeQuizAgain(View v) {
 		Intent i = new Intent(this, QuestionActivity.class);
 		i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 		i.putExtra(SUBJECT_INTENT_KEY, subject);
 		startActivity(i);
 	}
 	
 	public void onMainMenuSelected(View v) {
 		Intent i = new Intent(this, SubjectActivity.class);
 		i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 		startActivity(i);
 	}
 
 	@Override
 	public void onBackPressed() {
 		onMainMenuSelected(null);
 	}
 
 }
