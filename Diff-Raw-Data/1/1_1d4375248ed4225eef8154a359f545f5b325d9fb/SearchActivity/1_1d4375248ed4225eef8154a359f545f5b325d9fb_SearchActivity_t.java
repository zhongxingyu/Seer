 package com.evancharlton.magnatune;
 
 import java.io.IOException;
 import java.net.UnknownHostException;
 import java.util.HashMap;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.AdapterView;
 import android.widget.Button;
 import android.widget.EditText;
 
 import com.evancharlton.magnatune.objects.Album;
 import com.evancharlton.magnatune.objects.Artist;
 import com.evancharlton.magnatune.objects.SearchResult;
 import com.evancharlton.magnatune.objects.Song;
 import com.evancharlton.magnatune.views.SongController;
 
 public class SearchActivity extends LazyActivity {
 	private static final int MIN_LENGTH = 3;
 
 	private EditText mQuery;
 	private Button mSearch;
 	private InputMethodManager mInputMethodMgr;
 	private SongController mController;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		mFrom = new String[] {
 				SearchResult.TITLE,
 				SearchResult.SUBTEXT,
 				SearchResult.ICON_URL
 		};
 		mTo = new int[] {
 				android.R.id.text1,
 				android.R.id.text2,
 				android.R.id.icon
 		};
 		super.onCreate(savedInstanceState, R.layout.search, R.layout.album_row);
 
 		mQuery = (EditText) findViewById(R.id.query);
 		mSearch = (Button) findViewById(R.id.search);
 		mController = (SongController) findViewById(R.id.controller);
 
 		mSearch.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				startSearch();
 			}
 		});
 
 		mQuery.addTextChangedListener(new TextWatcher() {
 			@Override
 			public void afterTextChanged(Editable s) {
 			}
 
 			@Override
 			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
 			}
 
 			@Override
 			public void onTextChanged(CharSequence s, int start, int before, int count) {
 				mSearch.setEnabled(s != null && s.length() > MIN_LENGTH);
 			}
 		});
 
 		mQuery.setOnKeyListener(new View.OnKeyListener() {
 			public boolean onKey(View v, int keyCode, KeyEvent event) {
 				if (keyCode == KeyEvent.KEYCODE_ENTER && mQuery.getText().length() > MIN_LENGTH) {
 					startSearch();
 					return true;
 				}
 				return false;
 			}
 		});
 
 		mInputMethodMgr = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
 		mList.setTextFilterEnabled(false);
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		mController = (SongController) findViewById(R.id.controller);
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		if (mController != null) {
 			mController.destroy();
 			mController = null;
 			System.gc();
 		}
 	}
 
 	@Override
 	protected LoadTask newLoadTask() {
 		return new SearchTask();
 	}
 
 	@Override
 	public void onItemClick(AdapterView<?> list, View row, int position, long id) {
 		HashMap<String, String> info = mAdapterData.get(position);
 		String model = info.get(SearchResult.MODEL);
 		if (SearchResult.MODEL_ALBUM.equals(model)) {
 			startActivityForPosition(AlbumBrowser.class, position);
 		} else if (SearchResult.MODEL_ARTIST.equals(model)) {
 			startActivityForPosition(ArtistBrowser.class, position);
 		} else if (SearchResult.MODEL_SONG.equals(model)) {
 			try {
 				mController.autoPlay(MagnatuneAPI.getMP3Url(info.get(Song.MP3)));
 			} catch (IllegalArgumentException e) {
 				e.printStackTrace();
 			} catch (UnknownHostException e) {
 				showDialog(DIALOG_ERROR_LOADING);
 			} catch (IllegalStateException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	private void startSearch() {
 		if (mLoadTask.getStatus() == AsyncTask.Status.RUNNING) {
 			mLoadTask.cancel(true);
 		}
 		mInputMethodMgr.hideSoftInputFromWindow(mSearch.getWindowToken(), 0);
 		mLoadTask = newLoadTask();
 		setTaskActivity();
 		if (mLoadTask.getStatus() == AsyncTask.Status.PENDING) {
			mPage = 1;
 			mAdapterData.clear();
 			mAdapter.notifyDataSetChanged();
 			mLoadTask.execute(mQuery.getText().toString().trim());
 		}
 	}
 
 	private static class SearchTask extends LoadTask {
 		@Override
 		protected Boolean doInBackground(String... params) {
 			if (params.length == 1 && params[0].length() > MIN_LENGTH) {
 				mUrl = MagnatuneAPI.getFilterUrl("search", params[0]);
 				return loadUrl(mUrl);
 			}
 			return true;
 		}
 	}
 
 	@Override
 	protected HashMap<String, String> loadJSON(JSONObject resultObject) throws JSONException {
 		HashMap<String, String> resultInfo = new HashMap<String, String>();
 		String id = resultObject.getString("pk");
 		resultInfo.put(SearchResult.ID, id);
 		String model = resultObject.getString("model");
 		resultInfo.put(SearchResult.MODEL, model);
 
 		resultObject = resultObject.getJSONObject("fields");
 		String title = resultObject.getString("title");
 		String artist;
 		String album;
 		resultInfo.put(SearchResult.TITLE, title);
 		if (SearchResult.MODEL_ALBUM.equals(model)) {
 			resultInfo.put(Album.TITLE, title);
 			artist = resultObject.getString("artist_text");
 			resultInfo.put(Album.ARTIST, artist);
 			resultInfo.put(Album.ID, id);
 			resultInfo.put(SearchResult.ICON_URL, MagnatuneAPI.getCoverArtUrl(artist, title, 50));
 			resultInfo.put(SearchResult.SUBTEXT, String.format("%s (%s)", resultObject.getString("artist_text"), resultObject.getString("genre_text")));
 		} else if (SearchResult.MODEL_ARTIST.equals(model)) {
 			resultInfo.put(Artist.NAME, resultObject.getString("title"));
 			resultInfo.put(Artist.BIO, resultObject.getString("bio"));
 			resultInfo.put(Artist.ID, id);
 			resultInfo.put(SearchResult.SUBTEXT, resultObject.getString("bio"));
 		} else if (SearchResult.MODEL_SONG.equals(model)) {
 			artist = resultObject.getString("artist_text");
 			album = resultObject.getString("album_text");
 			resultInfo.put(SearchResult.SUBTEXT, artist + " - " + album);
 			resultInfo.put(Song.ARTIST, artist);
 			resultInfo.put(Song.ARTIST_ID, resultObject.getString("artist"));
 			resultInfo.put(Song.ALBUM, album);
 			resultInfo.put(Song.ALBUM_ID, resultObject.getString("album"));
 			resultInfo.put(Song.MP3, resultObject.getString("mp3"));
 		}
 		return resultInfo;
 	}
 }
