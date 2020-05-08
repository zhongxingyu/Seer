 package fr.mildlyusefulsoftware.mpdremote.activity;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.os.Bundle;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.CheckedTextView;
 import android.widget.ImageButton;
 import android.widget.ListView;
 import android.widget.TextView;
 import fr.mildlyusefulsoftware.mpdremote.R;
 import fr.mildlyusefulsoftware.mpdremote.bo.CurrentlyPlayingSong;
 import fr.mildlyusefulsoftware.mpdremote.bo.Song;
 import fr.mildlyusefulsoftware.mpdremote.service.MPDListener;
 import fr.mildlyusefulsoftware.mpdremote.service.MPDService;
 import fr.mildlyusefulsoftware.mpdremote.util.MPDRemoteUtils;
 
 public class SongLibraryActivity extends AbstractMPDActivity implements
 		MPDListener {
 
 	private MPDService mpd;
 	private String searchFilter = "";
 	private final List<Song> songLibrary = new ArrayList<Song>();
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		mpd = MPDService.getInstance(this);
 		mpd.addMPDListener(this);
 		setContentView(R.layout.song_library_layout);
 		final ListView songLibraryView = (ListView) findViewById(R.id.songLibraryView);
 		registerForContextMenu(songLibraryView);
		final PlaylistAdapter songLibraryAdapter=new PlaylistAdapter(this, songLibrary,
				R.layout.song_library_item_layout);
 		songLibraryView.setAdapter(songLibraryAdapter);
 
 		final ImageButton setFilterButton = (ImageButton) findViewById(R.id.song_library_search);
 		setFilterButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				final TextView filterTextView = (TextView) findViewById(R.id.song_library_search_filter_text);
 				searchFilter = filterTextView.getText().toString();
 				executeSongSearch();
 			}
 		});
 
 		final ImageButton clearFilterButton = (ImageButton) findViewById(R.id.song_library_clear_search);
 		clearFilterButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				final TextView filterTextView = (TextView) findViewById(R.id.song_library_search_filter_text);
 				filterTextView.setText("");
 				searchFilter = "";
 				executeSongSearch();
 			}
 		});
 
 		final ImageButton addToPlaylistButton = (ImageButton) findViewById(R.id.song_library_add);
 		addToPlaylistButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				List<Song> songsToAdd = new ArrayList<Song>();
 				for (int i = 0; i < songLibraryView.getChildCount(); i++) {
					if (songLibraryView.getCheckedItemPositions().get(i)){
 						songsToAdd.add(songLibraryAdapter.getItem(i));
 					}
 				}
 				mpd.addSongToPlayList(songsToAdd);
 			}
 		});
 
 		if (mpd.isConnected()) {
 			connectionChanged(true);
 		}
 
 	}
 
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v,
 			ContextMenuInfo menuInfo) {
 		super.onCreateContextMenu(menu, v, menuInfo);
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.song_library_list_context_menu, menu);
 	}
 
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 
 		switch (item.getItemId()) {
 		case R.id.addSongToPlaylist:
 			final ListView songLibraryView = (ListView) findViewById(R.id.songLibraryView);
 			AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
 					.getMenuInfo();
 			Song selectedSong = (Song) (songLibraryView
 					.getItemAtPosition(info.position));
 			mpd.addSongToPlayList(selectedSong);
 			return true;
 		default:
 			return super.onContextItemSelected(item);
 
 		}
 	}
 
 	@Override
 	public void playListChanged(List<Song> playList) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void currentlyPlayingSongChanged(
 			CurrentlyPlayingSong currentlyPlayingSong) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public synchronized void connectionChanged(final boolean connected) {
 		Log.d(MPDRemoteUtils.TAG, "songlib -> cnx changed");
 		executeSongSearch();
 		setEnabled(connected);
 	}
 
 	private void executeSongSearch() {
		songLibrary.clear();
		songLibrary.addAll(mpd.getSongsInLibrary(searchFilter));
		this.runOnUiThread(new Runnable() {
 
 			@Override
			public void run() {
 
 				final ListView songLibraryView = (ListView) findViewById(R.id.songLibraryView);
 				for (int i = 0; i < songLibraryView.getChildCount(); i++) {
 					songLibraryView.getCheckedItemPositions().delete(i);
					
 				}
 				((PlaylistAdapter) songLibraryView.getAdapter())
 						.notifyDataSetChanged();

 			}
		});

 	}
 
 }
