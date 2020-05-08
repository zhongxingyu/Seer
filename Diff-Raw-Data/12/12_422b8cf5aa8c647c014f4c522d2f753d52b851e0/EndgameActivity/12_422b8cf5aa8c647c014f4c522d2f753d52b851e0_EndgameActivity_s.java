 package com.project.gemswapper;
 
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.graphics.Typeface;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.View;
 import android.view.Window;
 import android.view.WindowManager.LayoutParams;
 import android.widget.TextView;
 
 public class EndgameActivity extends Activity {
 	
 	Intent finishedGame;
 	String scoreString;
 	String achievementPoints;
 	Typeface typeface;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
     	requestWindowFeature(Window.FEATURE_NO_TITLE); //hide title bar
     	getWindow().setFlags(0xFFFFFFFF, LayoutParams.FLAG_FULLSCREEN | LayoutParams.FLAG_KEEP_SCREEN_ON);
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_endgame);
         
         typeface = Typeface.createFromAsset(getAssets(), "YellowMagician.ttf");
         finishedGame = getIntent();
         scoreString = finishedGame.getStringExtra(GameView.SCORE);
         
         TextView highscore_main = (TextView) findViewById(R.id.highscore_main);
         TextView highscore_score = (TextView) findViewById(R.id.highscore_score);
 	    
 	    highscore_main.setTypeface(typeface);
 	    highscore_score.setTypeface(typeface);
         
         highscore_main.setText(R.string.highscore_main);
         highscore_score.setText(scoreString);
     }
 
  // Does the networking in a background thread. 
  	private class DataSend extends AsyncTask <Void, Void, Void> 
  	{
  		@Override
  		protected Void doInBackground(Void... params) {
  			send_data();
  			return null;
  		}
  	}
  	
  	// creates a get request and executes it. 
  	private void send_data()
  	{
      	DefaultHttpClient httpClient = new DefaultHttpClient();
      	
         SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
         
         String urlToSend = "";
         String coreUrl = "";
         
         String name = preferences.getString("Name","");
         String id = preferences.getString("Id","");
         
         int formerScore = preferences.getInt("Score",0);
         int currentScore = Integer.parseInt(scoreString);
         
      	coreUrl = "http://game-details.com/gemswapper/insertscore.php?";   
      	
         if(currentScore > formerScore)
         {
 	        SharedPreferences.Editor editor = preferences.edit();
 	        editor.putInt("Score",currentScore);
 	        editor.commit();
	        urlToSend = coreUrl + "id=" + id + "&" + "name=" + name + "&" + "score=" + scoreString;
         }
         
      	HttpGet getScore = new HttpGet(urlToSend);
      	
      	coreUrl = "http://game-details.com/gemswapper/insertachievements.php?";
         urlToSend = coreUrl + "id=" + id + "&" + "name=" + name + "&" + "achievements=" + achievementPoints;
      	
         System.out.println(urlToSend);
         
      	HttpGet getAchievement = new HttpGet(urlToSend);
      	
      	try 
      	{
  			httpClient.execute(getScore);
  			httpClient.execute(getAchievement);
  		} 
      	catch (Exception e) 
      	{
      		Log.i("derp", "herpderp");
  			e.printStackTrace();
  		}
      	
  	}
  	
  	// Checks to see if the user is connected to the Internet
  	public boolean isOnline()
  	{
  		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
  		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
  			
  		return (networkInfo != null && networkInfo.isConnected());
  	}
  	
  	public void Submit(View view)
  	{
  		updateDB();
  		
  		if(isOnline())
  		{
  			new DataSend().execute();
  			
  			Intent startGameIntent = new Intent(this, Game.class);
     		startActivity(startGameIntent);
  		}
  	}
  	
  	public void Quit(View view)
  	{
  		updateDB();
  		Intent intent = new Intent(getApplicationContext(), MainActivity.class);
  		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
  		startActivity(intent);
  	}
  	
  	public void updateDB()
  	{
  		DatabaseAdapter mDbHelper;
  		mDbHelper = new DatabaseAdapter(this);
  		mDbHelper.open();
  		
  		Cursor achievementsCursor = mDbHelper.fetchAll();
  		int numberOfAchievements = achievementsCursor.getCount();
  		
  		int oldCurrents[] = new int[numberOfAchievements];
  		int oldCompleteds[] = new int[numberOfAchievements];
  		int newCurrents[] = new int[numberOfAchievements];
  		int newCompleteds[] = new int[numberOfAchievements];
  		int progressCurrents[] = new int[numberOfAchievements];
  		
  		int pointsTemp = 0;
  		
  		progressCurrents = finishedGame.getIntArrayExtra(GameView.COUNTERS);
  		
  		achievementsCursor.moveToFirst();
  		
  		for (int i = 0; i < numberOfAchievements; ++i)
  		{
  			int goal = achievementsCursor.getInt(achievementsCursor.getColumnIndexOrThrow("Goal"));
  			int pointValue = achievementsCursor.getInt(achievementsCursor.getColumnIndexOrThrow("PointValue"));
 
  			oldCurrents[i] = achievementsCursor.getInt(achievementsCursor.getColumnIndexOrThrow("Current"));
  			oldCompleteds[i] = achievementsCursor.getInt(achievementsCursor.getColumnIndexOrThrow("Completed"));
  			newCompleteds[i] = ((oldCompleteds[i] * goal) + oldCurrents[i] + progressCurrents[i]) / goal;
  			newCurrents[i] = (oldCurrents[i] + progressCurrents[i]) % goal;
  			
  			mDbHelper.update(i+1, newCompleteds[i], newCurrents[i]);
  			pointsTemp += (newCompleteds[i] * pointValue);
  			
  			achievementsCursor.moveToNext();
  		}
  		achievementPoints = Integer.toString(pointsTemp);
  		
  		mDbHelper.close();
  	}
  	
 }
