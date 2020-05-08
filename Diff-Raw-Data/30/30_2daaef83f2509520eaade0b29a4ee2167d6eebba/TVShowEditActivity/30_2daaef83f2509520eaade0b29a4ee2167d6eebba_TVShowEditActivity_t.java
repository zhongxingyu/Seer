 package com.example.tvshowcrawler;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.Bundle;
 import android.support.v4.content.LocalBroadcastManager;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.ProgressBar;
 import android.widget.Toast;
 
 public class TVShowEditActivity extends Activity
 {
 	class ShowListBroadcastReceiver extends BroadcastReceiver
 	{
 		@Override
 		public void onReceive(Context context, Intent intent)
 		{
 			if (intent.getAction().equals(UpdateShowService.BROADCAST_GET_SHOW_LIST_COMPLETE))
 			{
 				ArrayList<TVShow> tvShows = intent.getParcelableArrayListExtra("com.example.tvshowcrawler.tvShows");
 				if (tvShows != null && !tvShows.isEmpty())
 				{
 					// display list with matches
 					final CharSequence[] items = new CharSequence[tvShows.size()];
 					final CharSequence[] ids = new CharSequence[tvShows.size()];
 					int i = 0;
 					for (TVShow tvShow : tvShows)
 					{
 						ids[i] = tvShow.getId();
 						items[i++] = tvShow.getName();
 					}
 
 					AlertDialog.Builder builder = new AlertDialog.Builder(TVShowEditActivity.this);
 					builder.setTitle("Pick a show");
 					builder.setItems(items, new DialogInterface.OnClickListener()
 					{
 						// select item in list
 						public void onClick(DialogInterface dialog, int item)
 						{
							Toast.makeText(TVShowEditActivity.this, items[item], Toast.LENGTH_SHORT).show();
 							// fill in correct id
 							EditText showIDEditText = (EditText) findViewById(R.id.editTextShowID);
 							showIDEditText.setText(ids[item]);
 						}
					});
					try
					{
						builder.show();
					} catch (Exception e)
					{
						Log.e(TAG, e.toString());
					}
 				}
 				// hide progress bar
 				ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
 				progressBar.setVisibility(View.INVISIBLE);
 			}
 		}
 	}
 
	private static final String TAG = "TVShowCrawler";
	private int position;
	private TVShow show;
	private ShowListBroadcastReceiver showListBroadcastReceiver;

 	@Override
 	public void finish()
 	{
 		// called automatically when back button is pressed
 
 		Intent intent = new Intent();
 
 		EditText nameEditText = (EditText) findViewById(R.id.editTextName);
 		EditText showIDEditText = (EditText) findViewById(R.id.editTextShowID);
 		EditText seasonEditText = (EditText) findViewById(R.id.editTextSeason);
 		EditText episodeEditText = (EditText) findViewById(R.id.editTextEpisode);
 		CheckBox activeBox = (CheckBox) findViewById(R.id.checkBoxActive);
 
 		if (nameEditText.getText().toString().trim().length() > 0)
 		{
 			// update show from views
 			// set name, removing leading/trailing whitespace
 			show.setName(nameEditText.getText().toString().trim());
 			// set id
 			show.setId(showIDEditText.getText().toString().trim());
 
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

 		final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
 		progressBar.setVisibility(View.INVISIBLE);
 
 		// create ShowListBroadcastReceiver, which handles all registered intents from the UpdateShowService
 		showListBroadcastReceiver = new ShowListBroadcastReceiver();
 		// register BROADCAST_GET_SHOW_LIST_COMPLETE intent
 		LocalBroadcastManager.getInstance(this).registerReceiver(showListBroadcastReceiver,
 				new IntentFilter(UpdateShowService.BROADCAST_GET_SHOW_LIST_COMPLETE));
 
 		final Intent intent = getIntent();
 		final String action = intent.getAction();
 
 		// create intent for updating tv shows
 		final Intent getShowListIntent = new Intent(this, UpdateShowService.class);
 		getShowListIntent.setAction(UpdateShowService.BROADCAST_GET_SHOW_LIST);
 
 		ImageView imageView = (ImageView) findViewById(R.id.ImageViewSearch);
 		imageView.setOnClickListener(new OnClickListener()
 		{
 			@Override
 			public void onClick(View v)
 			{
 				EditText nameEditText = (EditText) findViewById(R.id.editTextName);
 				final String showName = nameEditText.getText().toString().trim();
 				if (showName.isEmpty())
 				{
 					// show toast?
 					return;
 				}
 
 				progressBar.setVisibility(View.VISIBLE);
 
 				// search for show on TV Rage (other thread)
 				getShowListIntent.putExtra("com.example.tvshowcrawler.tvShowName", showName);
 				TVShowEditActivity.this.startService(getShowListIntent);
 			}
 		});
 
 		if (Intent.ACTION_EDIT.equals(action))
 		{
 			// get TVShow from bundle
 			position = intent.getExtras().getInt("tvShowIndex");
 			show = intent.getExtras().getParcelable("tvShow");
 
 			// update views
 			EditText nameEditText = (EditText) findViewById(R.id.editTextName);
 			nameEditText.setText(show.getName());
 			EditText showIDEditText = (EditText) findViewById(R.id.editTextShowID);
 			if (show.getId() != null)
 			{
 				showIDEditText.setText(show.getId());
 			}
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
 }
