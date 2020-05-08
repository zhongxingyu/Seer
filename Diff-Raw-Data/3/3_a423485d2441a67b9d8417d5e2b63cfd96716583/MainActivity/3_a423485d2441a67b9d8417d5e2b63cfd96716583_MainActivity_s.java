 /*
  * Project Java2
  * 
  * Package com.example.java1week2_4
  * 
  * @author Sease, Brandon
  * 
  * date    Jun 6, 2013
  * 
  */
 package com.example.movieInfo;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import services.WebServiceManager;
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.graphics.Paint;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.os.Messenger;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.example.java1week2_4.R;
 import com.example.lib.MovieProvider;
 import com.example.lib.WebManager;
 import com.loopj.android.image.SmartImageView;
 
 /**
  * The Class MainActivity.
  */
 @SuppressLint({ "NewApi", "InlinedApi" })
 public class MainActivity extends Activity {
 
 	// Global variables
 	Context _context;
 	Boolean _connected = false;
 	JSONObject _json;
 	EditText _searchField;
 	TextView _header;
 	TextView _titleTV;
 	TextView _yearTV;
 	TextView _mpaaTV;
 	TextView _runtimeTV;
 	TextView _criticsTV;
 	SmartImageView _iView;
 	TextView _infoTV;
 	String _link;
 	String _JSONString;
 	ProgressDialog _pDialog;
 	
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.app.Activity#onCreate(android.os.Bundle)
 	 */
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		// Set Content View
 		setContentView(R.layout.main);
 
 		// Set defaults for variables
 		_context = this;
 		_searchField = (EditText) findViewById(R.id.searchField);
 		_header = (TextView) findViewById(R.id.header);
 		_titleTV = (TextView) findViewById(R.id.titleTV);
 		_yearTV = (TextView) findViewById(R.id.yearTV);
 	    _mpaaTV = (TextView) findViewById(R.id.mpaaTV);
 		_runtimeTV = (TextView) findViewById(R.id.runtimeTV);
 		_criticsTV = (TextView) findViewById(R.id.criticsTV);
 		_iView = (SmartImageView) findViewById(R.id.posterIV);
 		_infoTV = (TextView) findViewById(R.id.infoTV);
 		
 		//Check for previously saved state
 		if (savedInstanceState == null) {
 			// Set Header Default text
 			_header.setText("Search For A Movie");
 		} else {
 			//Restore old Values
 			_searchField.setText(savedInstanceState.getString("SEARCH_FIELD"));
 			_header.setText(savedInstanceState.getString("HEADER"));
 			_titleTV.setText(savedInstanceState.getString("TITLE"));
 			_yearTV.setText(savedInstanceState.getString("YEAR"));
 			_mpaaTV.setText(savedInstanceState.getString("MPAA"));
 			_runtimeTV.setText(savedInstanceState.getString("RUNTIME"));
 			_criticsTV.setText(savedInstanceState.getString("CRITICS"));
			_iView.setImageUrl(savedInstanceState.getString("LINK"));
 			_infoTV.setText(savedInstanceState.getString("INFO"));
 			_JSONString = savedInstanceState.getString("JSON");
 		}
 		
 		// Detect Network connection
 		_connected = WebManager.getConnectionStatus(_context);
 		if (_connected) {
 			Log.i("NETWORK CONNECTION", WebManager.getConnectionType(_context));
 		} else {
 			// If no connection, load last viewed movie details
 
 			// Warning Display
 			_header.setText("No connection. Viewing last searched movie.");
 
 			
 			// Load last JSON file
 			FileStorageManager.getFileStuffInstance();
 			String stored = (String) FileStorageManager.readStringFile(_context, "Movie", true);
 			if (stored != null) {
 				try {
 					_json = new JSONObject(stored);
 					Log.i("SAVED JSON", _json.toString());
 					// Set Text Values
 					_searchField.setText(_json.getString(
 							"title").toString());
 					//Title
 					_titleTV.setText(_json.getString(
 							getString(R.string.titleAPI))
 							.toString());
 					// Year
 					_yearTV.setText(_json.getString(
 							getString(R.string.yearAPI))
 							.toString());
 					// Rating
 					_mpaaTV.setText(_json.getString(
 							getString(R.string.mpaa_ratingAPI))
 							.toString());
 					// Runtime
 					_runtimeTV.setText(_json.getString(
 							getString(R.string.runtimeAPI))
 							.toString()+" minutes");
 					// Critics
 					_criticsTV.setText(_json.getString(
 							getString(R.string.critics_consensusAPI))
 							.toString());
 					//Poster
 					_link = new String(_json.getJSONObject(
 							"posters").getString(
 							getString(R.string.thumbnailAPI)
 									.toString()));
 					_iView.setImageUrl(_link);
 					//Info
 					_link = new String(_json.getJSONObject(
 							"links").getString(
 							getString(R.string.infoAPI)
 									.toString()));
 					_iView.setImageUrl(_link);
 
 				} catch (JSONException e) {
 					Log.e("SAVED FILE", "CANT MAKE JSON");
 				}
 			}
 
 			// Set Search Text
 			try {
 				_searchField.setText(_json.getString("title").toString());
 			} catch (JSONException e) {
 				Log.e("SAVED FILE", "CANT LOAD MOVIE TITLE");
 			}
 		}
 
 		// Search Handler
 		Button searchButton = (Button) findViewById(R.id.searchButton);
 		searchButton.setOnClickListener(new OnClickListener() {
 			@SuppressLint("HandlerLeak")
 			@Override
 			public void onClick(View v) {
 				Log.i("CLICK HANDLER", _searchField.getText().toString());
 				
 				//Progress bar
 				_pDialog = new ProgressDialog(_context);
 			    _pDialog.setMessage("Fetching data");
 			    _pDialog.setIndeterminate(true);
 			    _pDialog.setCancelable(false);
 			    _pDialog.show();
 			    
 				//Clear out all text fields
 				_titleTV.setText("");
 				_yearTV.setText("");
 				_mpaaTV.setText("");
 				_runtimeTV.setText("");
 				_criticsTV.setText("");
 				_infoTV.setText("");
 				_iView.setImageUrl(null);
 				// Send text field entry to URL builder and request API
 				//HTTP Service Handler
 				Handler webHandler = new Handler() {
 					@Override
 					public void handleMessage(Message msg) {
 						if (msg.arg1 == RESULT_OK && msg.obj != null) {
 							try {
 								_JSONString = (String)msg.obj;
 								JSONObject resultJSON = new JSONObject(_JSONString);
 								// Set data
 								/*
 								 * JSON Array is Movies. object 0 is the first
 								 * result. get string provides the detail
 								 * Object.
 								 */
 								if (resultJSON.getString("total").toString()
 										.compareTo("0") == 0) {
 									Toast toast = Toast.makeText(_context,
 											"No movie by that name exists",
 											Toast.LENGTH_LONG);
 									toast.show();
 									_header.setText("Try Again");
 									//Close Progress Bar
 									_pDialog.dismiss();
 								} else {
 									
 								    
 									//Loop through a JSON Array and get Movie Names
 									ArrayList<HashMap<String, String>> movieList = new ArrayList<HashMap<String,String>>();
 									JSONArray movies = resultJSON.getJSONArray("movies");
 									int movieSize = movies.length();
 									for (int i = 0; i < movieSize; i++) {
 										JSONObject movieDetails = movies.getJSONObject(i);
 										String title = movieDetails.getString(getString(R.string.titleAPI).toString());
 										
 										HashMap<String, String> displayMap = new HashMap<String, String>();
 										displayMap.put("TITLE", title);
 										
 										movieList.add(displayMap);
 									}
 									//Close Progress Bar
 									_pDialog.dismiss();
 									//Load Picker Activity
 									Intent nextActivity = new Intent(_context, MoviePickerActivity.class);
 									nextActivity.putExtra("HEADER", "We found " + String.valueOf(movieSize) + " movies by that name. Select the one you wanted below.");
 									nextActivity.putExtra("MOVIES", movieList);
 									startActivityForResult(nextActivity, 0);
 									
 								}
 							} catch (Exception e) {
 								Log.e("HTTP HANDLER", e.getMessage().toString());
 								e.printStackTrace();
 								_header.setText("No info found. Please double check that the movie title was entered correctly");
 								//Close Progress Bar
 								_pDialog.dismiss();
 							}
 						}
 					}
 					
 				};
 				//HTTP Service Messenger
 				Messenger webMessenger = new Messenger(webHandler);
 				//HTTP Service Intent
 				Intent getWebIntent = new Intent(_context, WebServiceManager.class);
 				getWebIntent.putExtra(WebServiceManager.MESSENGER_KEY, webMessenger);
 				getWebIntent.putExtra(WebServiceManager.MOVIE_KEY, _searchField.getText().toString());
 				//Start service
 				startService(getWebIntent);
 				// Close keyboard
 				InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
 				inputManager.hideSoftInputFromWindow(getCurrentFocus()
 						.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
 				
 			}
 			
 		});
 		
 		//More Info Link handler
 		_infoTV.setPaintFlags(_infoTV.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
 		_infoTV.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				Intent infoIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(_infoTV.getText().toString()));
 				startActivity(infoIntent);
 				
 			}
 		});
 	};
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
 	 */
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 	// Service Handler
 	static final Handler serviceHandler = new Handler() {
 		public void handleMessage(Message message) {
 			Object returnedObject = message.obj;
 
 			// Test if status is ok.
 			if (message.arg1 == RESULT_OK && returnedObject != null) {
 
 			}
 		};
 	};
 
 	@Override
 	protected void onSaveInstanceState(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onSaveInstanceState(savedInstanceState);
 		savedInstanceState.putString("SEARCH_FIELD", _searchField.getText().toString());
 		savedInstanceState.putString("HEADER", _header.getText().toString());
 		savedInstanceState.putString("TITLE", _titleTV.getText().toString());
 		savedInstanceState.putString("YEAR", _yearTV.getText().toString());
 		savedInstanceState.putString("MPAA", _mpaaTV.getText().toString());
 		savedInstanceState.putString("RUNTIME", _runtimeTV.getText().toString());
 		savedInstanceState.putString("CRITICS", _criticsTV.getText().toString());
 		savedInstanceState.putString("LINK", _link);
 		savedInstanceState.putString("INFO", _infoTV.getText().toString());
 		savedInstanceState.putString("JSON", _JSONString);
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 		if (resultCode == RESULT_OK && requestCode == 0) {
 			Bundle results = data.getExtras();
 			if (results != null) {
 				//Trim JSON to just needed part
 				int movieNumber = results.getInt("MOVIE_NUMBER");
 				try {
 					JSONObject storedJSON = new JSONObject(_JSONString).getJSONArray("movies")
 							.getJSONObject(movieNumber);
 					// Store object in external memory
 					FileStorageManager.getFileStuffInstance();
 					FileStorageManager.storeStringFile(_context, "Movie", storedJSON.toString(), true);
 					//Pull data back from content provider (movie provider)
 					Cursor cursor = getContentResolver().query(MovieProvider.MovieData.CONTENT_URI, null, null, null, null);
 					cursor.moveToFirst();
 					String temp = new String(cursor.getString(2));
 					Log.i("Cursor", temp);
 					// Set Text Values
 					_searchField.setText(cursor.getString(1));
 					_header.setText("Movie Found!");
 					//Title
 					_titleTV.setText(cursor.getString(1));
 					// Year
 					_yearTV.setText(cursor.getString(2));
 					// Rating
 					_mpaaTV.setText(cursor.getString(3));
 					// Runtime
 					_runtimeTV.setText(cursor.getString(4)+" minutes");
 					// Critics
 					_criticsTV.setText(cursor.getString(5));
 					//Poster
 					_link = new String(cursor.getString(6));
 					_iView.setImageUrl(_link);
 					//Info
 					_infoTV.setText(cursor.getString(7));
 				} catch (JSONException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 }
