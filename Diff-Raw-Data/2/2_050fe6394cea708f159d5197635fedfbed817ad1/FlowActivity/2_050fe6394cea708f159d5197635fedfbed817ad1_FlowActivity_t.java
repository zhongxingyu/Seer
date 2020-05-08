 package com.smp.funwithmusic.activities;
 
 import static com.smp.funwithmusic.utilities.Constants.*;
 import static com.smp.funwithmusic.utilities.UtilityMethods.*;
 
 import java.util.List;
 import java.util.Locale;
 
 import com.afollestad.cardsui.Card;
 import com.afollestad.cardsui.Card.CardMenuListener;
 import com.afollestad.cardsui.CardBase;
 import com.afollestad.cardsui.CardHeader;
 import com.afollestad.cardsui.CardListView;
 import com.android.volley.RequestQueue;
 import com.android.volley.toolbox.Volley;
 import com.smp.funwithmusic.R;
 import com.smp.funwithmusic.adapters.SongCardAdapter;
 import com.smp.funwithmusic.dataobjects.Song;
 import com.smp.funwithmusic.dataobjects.SongCard;
 import com.smp.funwithmusic.services.IdentifyMusicService;
 import com.smp.funwithmusic.views.ProgressWheel;
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.Bundle;
 import android.support.v4.content.LocalBroadcastManager;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class FlowActivity extends Activity implements CardMenuListener<Card>
 {	
 	private List<Song> songs;
 	private IntentFilter filter;
 	private UpdateActivityReceiver receiver;
 	private SongCardAdapter<SongCard> cardsAdapter;
 	private CardListView cardsList;
 	private String lastArtist;
 	private View idDialog;
 	private View welcomeScreen;
 	private RequestQueue queue;
 	
 	private class UpdateActivityReceiver extends BroadcastReceiver
 	{
 		@Override
 		public void onReceive(Context context, Intent intent)
 		{
 			if (intent.getAction().equals(ACTION_REMOVE_IDENTIFY))
 			{
 				viewGone(idDialog);
 			}
 			else if (intent.getAction().equals(ACTION_ADD_SONG))
 			{
 				addCardsFromList();
 				if (intent.getBooleanExtra(EXTRA_FROM_ID, false))
 					scrollToBottomOfList();
 			}
 		}
 
 	}
 
 	private void resetArtist()
 	{
 		lastArtist = null;
 	}
 
 	@Override
 	protected void onPause()
 	{
 		super.onPause();
 		LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
 		saveSongs();
 		resetArtist();
 		queue.cancelAll(TAG_VOLLEY);
 	}
 
 	// reseting the imageUrl and lryics will allow the program to attempt to
 	// find them the next time if it couldn't this time.
 	// Song object determines if they should be reset internally.
 	private void saveSongs()
 	{
 		for (Song song : songs)
 		{
 			song.resetImageUrl();
 			song.resetLyrics();
 		}
 		writeObjectToFile(this, SONG_FILE_NAME, songs);
 	}
 
 	@Override
 	protected void onResume()
 	{
 		super.onResume();
 		addCardsFromList();
 		scrollToBottomOfList();
 		LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);
 
 		if (isMyServiceRunning(this, IdentifyMusicService.class))
 		{
 			viewVisible(idDialog);
 		}
 		else
 		{
 			viewGone(idDialog);
 		}
 	}
 
 	// Scrolls the view so that last item is at the bottom of the screen.
 	//
 	private void scrollToBottomOfList()
 	{
 		cardsList.post(new Runnable()
 		{
 			@Override
 			public void run()
 			{
 				// Select the last row so it will scroll into view
 				cardsList.setSelection(cardsAdapter.getCount() - 1);
 			}
 		});
 
 	}
 
 	// need to make old OS friendly
 	@SuppressLint("NewApi")
 	@Override
 	protected void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_flow);
 		queue = Volley.newRequestQueue(this);
		cardsAdapter = new SongCardAdapter<SongCard>(this, queue, TAG_VOLLEY);
 		cardsAdapter.setAccentColorRes(android.R.color.holo_blue_dark);
 		cardsAdapter.setPopupMenu(R.menu.card_popup, this); // the popup menu
 		// callback is this
 		// activity
 
 		TextView progressText = (TextView) findViewById(R.id.progress_text);
 		progressText.setText(getResources().getText(R.string.identify));
 
 		idDialog = findViewById(R.id.progress);
 		welcomeScreen = findViewById(R.id.welcome_screen);
 
 		cardsList = (CardListView) findViewById(R.id.cardsList);
 		cardsList.setAdapter(cardsAdapter);
 		cardsList.setOnCardClickListener(new CardListView.CardClickListener()
 
 		{
 			@SuppressWarnings("rawtypes")
 			@Override
 			public void onCardClick(int index, CardBase card, View view)
 			{
 
 				SongCard songCard = (SongCard) card;
 				Song song = songCard.getSong();
 				if (song.hasLyrics())
 				{
 
 					Intent intent = new Intent(FlowActivity.this, WebActivity.class);
 					intent.putExtra(WEB_URL, song.getFullLyricsUrl());
 					startActivity(intent);
 					/*
 					 * Uri uri = Uri.parse(song.getFullLyricsUrl()); Intent
 					 * intent = new Intent(Intent.ACTION_VIEW, uri);
 					 * startActivity(intent);
 					 */
 				}
 
 			}
 		});
 
 		filter = new IntentFilter();
 		filter.addAction(ACTION_ADD_SONG);
 		filter.addAction(ACTION_REMOVE_IDENTIFY);
 		filter.addCategory(Intent.CATEGORY_DEFAULT);
 
 		receiver = new UpdateActivityReceiver();
 
 	}
 
 	private void addCardsFromList()
 	{
 		// songs never is null
 		songs = getSongList(this);
 		cardsAdapter.clear();
 		cardsAdapter.notifyDataSetChanged();
 		for (Song song : songs)
 		{
 			addCard(song);
 		}
 		if (cardsAdapter.getCount() == 0)
 		{
 			viewVisible(welcomeScreen);
 		}
 		else
 		{
 			viewGone(welcomeScreen);
 		}
 	}
 
 	// Adds to same stack if the artist is the same.
 
 	private void addCard(final Song song)
 	{
 		// lastArtist null check probably not necessary here.
 		Locale locale = Locale.getDefault();
 		if (cardsAdapter.getCount() != 0 && lastArtist != null
 				&& lastArtist.toUpperCase(locale).equals(song.getArtist().toUpperCase(locale)))
 		{
 			cardsAdapter.add(new SongCard(song, this));
 			return;
 		}
 		lastArtist = song.getArtist();
 		cardsAdapter.add(new CardHeader(song.getArtist())
 
 				.setAction(this, R.string.artist_info, new CardHeader.ActionListener()
 				{
 					@Override
 					public void onClick(CardHeader header)
 					{
 						Intent intent = new Intent(FlowActivity.this, ArtistActivity.class);
 						intent.putExtra(ARTIST_NAME, song.getArtist());
 						startActivity(intent);
 					}
 				}));
 
 		cardsAdapter.add(new SongCard(song, this));
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item)
 	{
 		switch (item.getItemId())
 		{
 			case R.id.clear:
 				doDeleteFlow(this);
 				resetArtist();
 				addCardsFromList();
 				break;
 			case R.id.listen:
 				if (!isMyServiceRunning(this, IdentifyMusicService.class))
 				{
 					doListen(getApplicationContext(), idDialog);
 				}
 				break;
 			default:
 				return false;
 		}
 		return true;
 	}
 
 	public static void doListen(Context context, final View idDialog)
 	{
 		viewVisible(idDialog);
 		Intent intent = new Intent(context, IdentifyMusicService.class);
 		context.startService(intent);
 	}
 
 	public static void viewVisible(final View view)
 	{
 		view.post(new Runnable()
 		{
 			@Override
 			public void run()
 			{
 				ProgressWheel pw = (ProgressWheel) view.findViewById(R.id.pw_spinner);
 				if (pw != null)
 					pw.spin();
 				view.setVisibility(View.VISIBLE);
 			}
 		});
 	}
 
 	public static void viewGone(final View view)
 	{
 		view.post(new Runnable()
 		{
 			@Override
 			public void run()
 			{
 				ProgressWheel pw = (ProgressWheel) view.findViewById(R.id.pw_spinner);
 				if (pw != null)
 					pw.stopSpinning();
 				view.setVisibility(View.GONE);
 			}
 		});
 	}
 
 	public static void doDeleteFlow(Context context)
 	{
 		context.deleteFile(SONG_FILE_NAME);
 		Toast.makeText(context, TOAST_FLOW_DELETED, Toast.LENGTH_SHORT).show();
 	}
 
 	@Override
 	public void onMenuItemClick(Card card, MenuItem item)
 	{
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu)
 	{
 		getMenuInflater().inflate(R.menu.menu_flow, menu);
 		return true;
 	}
 
 }
