 package com.michalkazior.simplemusicplayer;
 
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.View.OnCreateContextMenuListener;
 import android.view.View.OnKeyListener;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.ListView;
 
 /**
  * Activity for song searching and enqueueing.
  * 
  * The user may filter songs by a given phrase set. The filter matches songs
  * that contain all typed words.
  */
 public class Songlist extends Activity {
 	private ListView availableSongs;
 	private EditText filter;
 	private Song selectedSong;
 	private Song[] songs = {};
 	private ArrayList<Song> filtered = new ArrayList<Song>();
 
 	/**
 	 * Context menu when long-pressing song
 	 */
 	private enum ContextMenu {
 		PLAY_NOW {
 			@Override
 			public int getLabelId() {
 				return R.string.context_menu_play_now;
 			}
 
 			@Override
 			public void call(Songlist s) {
 				s.sendBroadcast(Player.Remote.Request.EnqueueSong
 						.getIntent()
 						.putExtra("song", s.selectedSong)
 						.putExtra("index", 0));
 			}
 		},
 		PLAY_NEXT {
 			@Override
 			public int getLabelId() {
 				return R.string.context_menu_play_next;
 			}
 
 			@Override
 			public void call(Songlist s) {
 				s.sendBroadcast(Player.Remote.Request.EnqueueSong
 						.getIntent()
 						.putExtra("song", s.selectedSong)
 						.putExtra("index", 1));
 			}
 		},
 		ENQUEUE {
 			@Override
 			public int getLabelId() {
 				return R.string.context_menu_enqueue;
 			}
 
 			@Override
 			public void call(Songlist s) {
 				s.sendBroadcast(Player.Remote.Request.EnqueueSong.getIntent().putExtra("song",
 						s.selectedSong));
 			}
 		};
 		public abstract int getLabelId();
 
 		public abstract void call(Songlist s);
 
 		public static void generate(Menu menu) {
 			for (ContextMenu item : values()) {
 				menu.add(0, item.ordinal(), 0, item.getLabelId());
 			}
 		}
 
 		public static void run(Songlist s, MenuItem item) {
 			values()[item.getItemId()].call(s);
 		}
 	};
 
 	/**
 	 * Option menu when menu button is pressed
 	 */
 	private enum OptionMenu {
 		ENQUEUE_ALL {
 			@Override
 			public int getLabelId() {
 				return R.string.option_menu_enqueue_all;
 			}
 
 			@Override
 			public void call(Songlist s) {
 				for (Song song : s.filtered) {
 					s.sendBroadcast(Player.Remote.Request.EnqueueSong.getIntent().putExtra("song",
 							song));
 				}
 			}
 		};
 		public abstract int getLabelId();
 
 		public abstract void call(Songlist s);
 
 		public static void generate(Menu menu) {
 			for (OptionMenu item : values()) {
 				menu.add(0, item.ordinal(), 0, item.getLabelId());
 			}
 		}
 
 		public static void run(Songlist s, MenuItem item) {
 			values()[item.getItemId()].call(s);
 		}
 	};
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.playlist);
 
 		availableSongs = (ListView) findViewById(R.id.playlistAvailableSongsListView);
 		filter = (EditText) findViewById(R.id.playlistFilterEditText);
 
 		availableSongs.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
 			@Override
 			public void onCreateContextMenu(android.view.ContextMenu menu, View v,
 					ContextMenuInfo menuInfo) {
 				AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
 				selectedSong = (Song) availableSongs.getItemAtPosition(info.position);
 				ContextMenu.generate(menu);
 			}
 		});
 
 		registerReceiver(new BroadcastReceiver() {
 			@Override
 			public void onReceive(Context context, Intent intent) {
 				songs = Player.parcelableArrayToSongs(intent.getParcelableArrayExtra("songs"));
 				updateAvailableSongsListView();
 			}
 		}, Player.Remote.Reply.AvailableSongs.getIntentFilter());
 
 		filter.setOnKeyListener(new OnKeyListener() {
 			@Override
 			public boolean onKey(View v, int keyCode, KeyEvent event) {
 				if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
 					updateAvailableSongsListView();
					InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
 					imm.hideSoftInputFromWindow(filter.getWindowToken(), 0);
 					return true;
 				}
 				return false;
 			}
 		});
 
 		sendBroadcast(Player.Remote.Request.GetAvailableSongs.getIntent());
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		OptionMenu.generate(menu);
 		return super.onCreateOptionsMenu(menu);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		OptionMenu.run(this, item);
 		return super.onOptionsItemSelected(item);
 	}
 
 	/**
 	 * Updates the available songs list.
 	 * 
 	 * Updates the list accordingly to the entered filter phrase in the edit
 	 * text box.
 	 */
 	private void updateAvailableSongsListView() {
 		filtered = new ArrayList<Song>();
 		String words[] = filter.getText().toString().toLowerCase().split(" ");
 
 		for (Song song : songs) {
 			String name = song.getPath().toLowerCase();
 			boolean matches = true;
 			for (String word : words) {
 				if (!name.contains(word)) matches = false;
 			}
 			if (matches) filtered.add(song);
 		}
 
		availableSongs.setAdapter(new SongAdapter(this, R.layout.listitem, filtered.toArray(new Song[] {})));
 	}
 
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		ContextMenu.run(this, item);
 		return super.onContextItemSelected(item);
 	}
 }
