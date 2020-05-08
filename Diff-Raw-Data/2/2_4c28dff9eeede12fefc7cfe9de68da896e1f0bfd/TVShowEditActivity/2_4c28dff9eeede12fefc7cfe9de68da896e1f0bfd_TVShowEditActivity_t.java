 package com.example.tvshowcrawler;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.widget.CheckBox;
 import android.widget.EditText;
 
 public class TVShowEditActivity extends Activity
 {
 	@Override
 	public void finish()
 	{
 		// called automatically when back button is pressed
 
 		Intent intent = new Intent();
 
 		EditText nameEditText = (EditText) findViewById(R.id.editTextName);
 		EditText seasonEditText = (EditText) findViewById(R.id.editTextSeason);
 		EditText episodeEditText = (EditText) findViewById(R.id.editTextEpisode);
 		CheckBox activeBox = (CheckBox) findViewById(R.id.checkBoxActive);
 
 		if (nameEditText.getText().toString().trim().length() > 0)
 		{
 			// update show from views
 			// set name, removing leading/trailing whitespace
 			show.setName(nameEditText.getText().toString().trim());
 			// set season
 			if (seasonEditText.getText().toString().trim().length() > 0)
 				show.setSeason(Integer.parseInt(seasonEditText.getText().toString()));
 			else
 				show.setSeason(1); // default to 1
 			// set episode
			if (episodeEditText.getText().toString().trim().length() > 0)
 				show.setEpisode(Integer.parseInt(episodeEditText.getText().toString()));
 			else
 				show.setEpisode(0); // default to 0
 
 			show.setActive(activeBox.isChecked());
 
 			intent.putExtra("tvShow", show);
 			intent.putExtra("tvShowIndex", position); // ignored when adding new show
 			intent.setAction(getIntent().getAction()); // return original action
 			// Activity finished ok, return the data
 			setResult(RESULT_OK, intent);
 		}
 		else
 		{
 			// no name entered, assume user wanted to cancel
 			setResult(RESULT_CANCELED);
 		}
 
 		super.finish();
 	}
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_tvshowedit);
 
 		final Intent intent = getIntent();
 		final String action = intent.getAction();
 
 		if (Intent.ACTION_EDIT.equals(action))
 		{
 			// get TVShow from bundle
 			position = intent.getExtras().getInt("tvShowIndex");
 			show = intent.getExtras().getParcelable("tvShow");
 
 			// update views
 			EditText nameEditText = (EditText) findViewById(R.id.editTextName);
 			nameEditText.setText(show.getName());
 			EditText seasonEditText = (EditText) findViewById(R.id.editTextSeason);
 			seasonEditText.setText(String.valueOf(show.getSeason()));
 			EditText episodeEditText = (EditText) findViewById(R.id.editTextEpisode);
 			episodeEditText.setText(String.valueOf(show.getEpisode()));
 			CheckBox activeBox = (CheckBox) findViewById(R.id.checkBoxActive);
 			activeBox.setChecked(show.getActive());
 		}
 		else if (Intent.ACTION_INSERT.equals(action))
 		{
 			// create new show
 			show = new TVShow();
 		}
 		else
 		{
 			// Logs an error that the action was not understood, finishes the Activity, and
 			// returns RESULT_CANCELED to an originating Activity.
 			Log.e(TAG, "Unknown action, exiting");
 			finish();
 			return;
 		}
 	}
 
 	private TVShow show;
 	private int position;
 	private static final String TAG = "TVShowCrawler";
 }
