 package edu.csumb.ideasofmarch.codecruncher;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.HashMap;
 import java.util.Map;
 
 import com.google.gson.Gson;
 import com.google.gson.reflect.TypeToken;
 
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.StrictMode;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnKeyListener;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.text.Editable;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 
 public class MainActivity extends Activity {
 
 	Button newGameButton;
 	Button donateButton;
 	Button highScoresButton;
 	Button quitButton;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 		disabledPolicy();
 		initLocalPreferences();
 		initLocalScores();
 
 		newGameButton = (Button) findViewById(R.id.newGameButton);
 		donateButton = (Button) findViewById(R.id.donateButton);
 		highScoresButton = (Button) findViewById(R.id.highScoresButton);
 		quitButton = (Button) findViewById(R.id.quitButton);
 
 		newGameButton.setOnClickListener(new NewGameButtonListener());
 		donateButton.setOnClickListener(new DonateButtonListener());
 		highScoresButton.setOnClickListener(new HighScoresButtonListener());
 		quitButton.setOnClickListener(new QuitButtonListener());
 	}
 
 	private void getUserName() {
 
 		AlertDialog.Builder alert = new AlertDialog.Builder(this);
 		final Dialog dialog;
 		alert.setTitle("Username");
 		alert.setMessage("What is your name?");
 
 		// Set an EditText view to get user input
 		final EditText input = new EditText(this);
 		alert.setView(input);
 
 	
 
 		dialog = alert.create();
 		
 		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int whichButton) {
 				Editable value = input.getText();
 				// Do something with value!
 				updateUserName(value.toString());
 
 			}
 		});
 
 		input.setOnKeyListener(new View.OnKeyListener() {
 			@Override
 			public boolean onKey(View v, int keyCode, KeyEvent event) {
 				// TODO Auto-generated method stub
 				if (keyCode == KeyEvent.KEYCODE_ENTER) {
 					Editable value = input.getText();
 					// Do something with value!
 					updateUserName(value.toString());
 					dialog.cancel();
 					return true;
 				}
 				return false;
 
 			}
 		});
 
 		alert.setNegativeButton("Cancel",
 				new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int whichButton) {
 						// clicked cancel, set name to UNK.
 						updateUserName("Anonymous");
 					}
 				});
 		
 		
 		dialog.show();
 
 	}
 
 	private void updateUserName(String name) {
 
 		CrunchConstants.myPreferencesMap.remove(CrunchConstants.JSON_NAME);
 		CrunchConstants.myPreferencesMap.put(CrunchConstants.JSON_NAME, name);
 
 		try {
 			FileOutputStream fos = openFileOutput(
 					CrunchConstants.PREFERENCES_FILENAME, MODE_PRIVATE);
 
 			fos.write(new Gson().toJson(CrunchConstants.myPreferencesMap)
 					.getBytes());
 			fos.close();
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 
 		return true;
 	}
 
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// Handle item selection
 		switch (item.getItemId()) {
 		case R.id.action_settings:
 			startSettings();
 			return true;
 		case R.id.action_help:
 			startHelp();
 			return true;
 		case R.id.action_about:
 			startAbout();
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	public void startSettings() {
 		startActivity(new Intent(this, Settings.class));
 	}
 
 	public void startHelp() {
 		startActivity(new Intent(this, Help.class));
 	}
 
 	public void startAbout() {
 		startActivity(new Intent(this, About.class));
 	}
 
 	private class NewGameButtonListener implements OnClickListener {
 		public void onClick(View view) {
 			startChooseMode();
 		}
 	}
 
 	public void startChooseMode() {
 		startActivity(new Intent(this, ChooseMode.class));
 	}
 
 	private class DonateButtonListener implements OnClickListener {
 		public void onClick(View view) {
 			startDonate();
 		}
 	}
 
 	private void startDonate() {
 		goToUrl("https://milcyber.org/#donate");
 	}
 
 	private void goToUrl(String url) {
 		Uri uriUrl = Uri.parse(url);
 		Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
 		startActivity(launchBrowser);
 	}
 
 	private class HighScoresButtonListener implements OnClickListener {
 		public void onClick(View view) {
 			startHighScores();
 		}
 	}
 
 	public void startHighScores() {
 		startActivity(new Intent(this, HighScores.class));
 	}
 
 	private class QuitButtonListener implements OnClickListener {
 		public void onClick(View view) {
 			System.exit(0);
 		}
 	}
 
 	private void disabledPolicy() {
 		if (android.os.Build.VERSION.SDK_INT > 9) {
 			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
 					.permitAll().build();
 			StrictMode.setThreadPolicy(policy);
 		}
 	}
 
 	/**
 	 * Eats file saved local information
 	 */
 	private void initLocalScores() {
 		try {
 
 			InputStream is = openFileInput(CrunchConstants.SCORES_FILENAME);
 
 			final Gson gson = new Gson();
 			final BufferedReader reader = new BufferedReader(
 					new InputStreamReader(is));
 
 			CrunchConstants.myScoresMap = gson.fromJson(reader,
 					new TypeToken<Map<Integer, Integer>>() {
 					}.getType());
 
 			is.close();
 
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			Log.v("error", "died in get assets init local high scores", e);
 
 			Map<Integer, Integer> myTemporaryMap = new HashMap<Integer, Integer>();
 
 			myTemporaryMap.put(CrunchConstants.DECIMAL_TO_BINARY, 0);
 			myTemporaryMap.put(CrunchConstants.DECIMAL_TO_HEX, 0);
 			myTemporaryMap.put(CrunchConstants.HEX_TO_BINARY, 0);
 			myTemporaryMap.put(CrunchConstants.HEX_TO_DECIMAL, 0);
 			myTemporaryMap.put(CrunchConstants.BINARY_TO_DECIMAL, 0);
 			myTemporaryMap.put(CrunchConstants.BINARY_TO_HEX, 0);
 			myTemporaryMap.put(CrunchConstants.DECIMAL_TO_BINARY_HARD, 0);
 			myTemporaryMap.put(CrunchConstants.DECIMAL_TO_HEX_HARD, 0);
 			myTemporaryMap.put(CrunchConstants.HEX_TO_BINARY_HARD, 0);
 			myTemporaryMap.put(CrunchConstants.HEX_TO_DECIMAL_HARD, 0);
 			myTemporaryMap.put(CrunchConstants.BINARY_TO_DECIMAL_HARD, 0);
 			myTemporaryMap.put(CrunchConstants.BINARY_TO_HEX_HARD, 0);
 
 			CrunchConstants.myScoresMap = myTemporaryMap;
 		}
 
 		try {
 			FileOutputStream fos = openFileOutput(
 					CrunchConstants.SCORES_FILENAME, MODE_PRIVATE);
 
 			fos.write(new Gson().toJson(CrunchConstants.myScoresMap).getBytes());
 			fos.close();
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 	}
 
 	public void initLocalPreferences() {
 		
 
 		try {
 
 			InputStream is = openFileInput(CrunchConstants.PREFERENCES_FILENAME);
 
 			final Gson gson = new Gson();
 			final BufferedReader reader = new BufferedReader(
 					new InputStreamReader(is));
 
 			CrunchConstants.myPreferencesMap = gson.fromJson(reader,
 					new TypeToken<Map<String, String>>() {
 					}.getType());
 
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			Log.v("error", "died in get assets init local high scores", e);
 
 			Map<String, String> myTemporaryMap = new HashMap<String, String>();
 
 			// Update this to ask user for input to add to file.
 			myTemporaryMap.put(CrunchConstants.JSON_NAME, "XXX");
 
 			CrunchConstants.myPreferencesMap = myTemporaryMap;
 		}
 
 		// Successfully init
 
 		try {
 			FileOutputStream fos = openFileOutput(
 					CrunchConstants.PREFERENCES_FILENAME, MODE_PRIVATE);
 
 			fos.write(new Gson().toJson(CrunchConstants.myPreferencesMap)
 					.getBytes());
 			fos.close();
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		if (CrunchConstants.myPreferencesMap == null || 
				!CrunchConstants.myPreferencesMap.containsKey(CrunchConstants.JSON_NAME) || 
 				CrunchConstants.myPreferencesMap.get(CrunchConstants.JSON_NAME).isEmpty())
 			getUserName();
 
 	}
 
 }
