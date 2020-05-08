 package com.codingspezis.android.metalonly.player;
 
 import java.net.*;
 import java.util.*;
 import android.annotation.SuppressLint;
 import android.app.*;
 import android.content.*;
 import android.content.DialogInterface.OnClickListener;
 import android.net.*;
 import android.os.*;
 import android.support.v4.app.*;
 import android.view.*;
 import android.widget.*;
 import android.widget.AdapterView.OnItemClickListener;
 import com.actionbarsherlock.app.*;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 import com.actionbarsherlock.view.SubMenu;
 import com.codingspezis.android.metalonly.player.favorites.*;
 import com.codingspezis.android.metalonly.player.siteparser.*;
 import com.codingspezis.android.metalonly.player.wish.*;
 import com.googlecode.androidannotations.annotations.*;
 
 /**
  * FavoritesActivity
  * 
  * this activity displays favorites and allows to handle them
  * 
  */
 @EActivity(R.layout.activity_favorites)
 @OptionsMenu(R.menu.favoritesmenu)
public class FavoritesActivity extends SherlockListActivity {
 
 	public static final String JSON_FILE_FAV = "mo_fav.json";
 
 	@ViewById
 	ListView list;
 
 	private Menu menu;
 
 	private SongSaver favoritesSaver;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
 		getSupportActionBar().setHomeButtonEnabled(true);
 
 		favoritesSaver = new SongSaver(this, JSON_FILE_FAV, -1);
 		list = getListView();
 		displayFavorites();
 	}
 
 	@Override
 	public void onPause() {
 		favoritesSaver.saveSongsToStorage();
 		super.onPause();
 	}
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		favoritesSaver.reload();
 		displayFavorites();
 	}
 
 	@OptionsItem(R.id.mnu_add_manually)
 	void showAddSongDialog() {
 		AlertDialog.Builder alert = new AlertDialog.Builder(this);
 		alert.setTitle(R.string.menu_add_mannually);
 		final View v = getLayoutInflater().inflate(R.layout.dialog_add_song, null);
 		alert.setView(v);
 		alert.setNegativeButton(R.string.abort, null);
 		alert.setPositiveButton(R.string.ok, new OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				EditText artist = (EditText) v.findViewById(R.id.edit_artist);
 				EditText txtTitle = (EditText) v.findViewById(R.id.edit_title);
 
 				String interpret = artist.getText().toString();
 				String title = txtTitle.getText().toString();
 
 				Song song = new Song(interpret, title);
 
 				if (song.isValid() && favoritesSaver.isAlreadyIn(song) < 0) {
 					favoritesSaver.addSong(song);
 					displayFavorites();
 				}
 			}
 		});
 		alert.show();
 	}
 
 	/**
 	 * displays favorites on screen
 	 */
 	private void displayFavorites() {
 		list.removeAllViewsInLayout();
 		ArrayList<Song> songs = new ArrayList<Song>();
 		for (int i = favoritesSaver.size() - 1; i >= 0; i--) {
 			songs.add(favoritesSaver.get(i));
 		}
 		SongAdapterFavorites adapter = new SongAdapterFavorites(this, songs);
 		list.setAdapter(adapter);
 	}
 
 	/**
 	 * handles an action on an index
 	 * 
 	 * @param index
 	 *            item to handle
 	 * @param action
 	 *            action to handle
 	 */
 	private void handleAction(final int index, int action) {
 		switch (action) {
 		case 0: // wish
 			if (!HTTPGrabber.displayNetworkSettingsIfNeeded(this)) {
 				WishChecker wishChecker = new WishChecker(this, WishActivity.URL_WISHES);
 				wishChecker.setOnWishesCheckedListener(new OnWishesCheckedListener() {
 					@Override
 					public void onWishesChecked(AllowedActions allowedActions) {
 						if (WishActivity
 								.canWishOrDisplayNot(FavoritesActivity.this, allowedActions)) {
 							Bundle bundle = new Bundle();
 							bundle.putBoolean(WishActivity.KEY_WISHES_ALLOWED,
 									allowedActions.wishes);
 							bundle.putBoolean(WishActivity.KEY_REGARDS_ALLOWED,
 									allowedActions.regards);
 							bundle.putString(WishActivity.KEY_NUMBER_OF_WISHES,
 									allowedActions.limit);
 							bundle.putString(WishActivity.KEY_DEFAULT_INTERPRET,
 									favoritesSaver.get(index).interpret);
 							bundle.putString(WishActivity.KEY_DEFAULT_TITLE,
 									favoritesSaver.get(index).title);
 							Intent wishIntent = new Intent(FavoritesActivity.this,
 									WishActivity.class);
 							wishIntent.putExtras(bundle);
 							FavoritesActivity.this.startActivity(wishIntent);
 						}
 					}
 				});
 				wishChecker.start();
 			}
 			break;
 		case 1: // YouTube
 			String searchStr = favoritesSaver.get(index).interpret + " - "
 					+ favoritesSaver.get(index).title;
 			try {
 				searchStr = URLEncoder.encode(searchStr, "UTF-8");
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 			Uri url = Uri.parse("http://www.youtube.com/results?search_query=" + searchStr);
 			Intent youtube = new Intent(Intent.ACTION_VIEW, url);
 			startActivity(youtube);
 			break;
 		case 2: // share
 			String message = favoritesSaver.get(index).interpret + " - "
 					+ favoritesSaver.get(index).title;
 			Intent share = new Intent(Intent.ACTION_SEND);
 			share.setType("text/plain");
 			share.putExtra(Intent.EXTRA_TEXT, message);
 			startActivity(Intent.createChooser(share,
 					getResources().getStringArray(R.array.favorite_options_array)[2]));
 			break;
 		case 3: // delete
 			favoritesSaver.removeAt(index);
 			displayFavorites();
 		}
 	}
 
	@ItemClick(android.R.id.list)
	public void listItemClicked(final int position) {
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setItems(R.array.favorite_options_array, new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
				handleAction(favoritesSaver.size() - position - 1, which);
 			}
 		});
 		builder.show();
 	}
 
 	@Override
 	public boolean onKeyUp(int keyCode, KeyEvent event) {
 		if (keyCode == KeyEvent.KEYCODE_MENU) {
 			if (event.getAction() == KeyEvent.ACTION_UP && menu != null
 					&& menu.findItem(R.id.mnu_sub) != null) {
 				menu.performIdentifierAction(R.id.mnu_sub, 0);
 				return true;
 			}
 		}
 		return super.onKeyUp(keyCode, event);
 	}
 
 	@OptionsItem(R.id.mnu_deleteall)
 	void deleteAllClicked() {
 		askSureDelete(this, new OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				favoritesSaver.clear();
 				displayFavorites();
 			}
 		}, null);
 	}
 
 	@OptionsItem(R.id.mnu_shareall)
 	void shareAllClicked() {
 		String message = "";
 		for (int i = favoritesSaver.size() - 1; i >= 0; i--) {
 			message += favoritesSaver.get(i).interpret + " - " + favoritesSaver.get(i).title + "\n";
 		}
 		// open share dialog
 		Intent share = new Intent(Intent.ACTION_SEND);
 		share.setType("text/plain");
 		share.putExtra(Intent.EXTRA_TEXT, message);
 		startActivity(Intent.createChooser(share,
 				getResources().getStringArray(R.array.favorite_options_array)[2]));
 	}
 
 	@SuppressLint("InlinedApi")
 	@OptionsItem(android.R.id.home)
 	void upButtonClicked() {
 		Intent intent = new Intent(this, MainActivity.class);
 		NavUtils.navigateUpTo(this, intent);
 	}
 
 	/**
 	 * asks if user is sure to delete something
 	 * 
 	 * @param yes
 	 *            what is to do if user clicks yes
 	 * @param no
 	 *            what is to do if user clicks no
 	 */
 	public static void askSureDelete(Context context, OnClickListener yes, OnClickListener no) {
 		AlertDialog.Builder alert = new AlertDialog.Builder(context);
 		alert.setMessage(R.string.delete_sure);
 		alert.setNegativeButton(R.string.no, no);
 		alert.setPositiveButton(R.string.yes, yes);
 		alert.show();
 	}
 }
