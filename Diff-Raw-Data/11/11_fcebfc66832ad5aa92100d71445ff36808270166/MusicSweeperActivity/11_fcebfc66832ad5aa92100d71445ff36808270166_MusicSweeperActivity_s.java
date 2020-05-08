 package com.sweeper.music;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.widget.AdapterView;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 
 public class MusicSweeperActivity extends Activity {
 	private static final String STOPPING_SERVICE = "Stopping Service";
 	private static final String STOPPING_MUSIC_EXCEPTION = "Exception thrown while stopping music file ";
 	private static final String TAG = "MusicSweeperActivity";
 	private MusicFileHelper fileHelper = new MusicFileHelper();
 	private Map<String, String> musicFiles = new HashMap<String, String>();
 	private ListView myListView = null;
 	private EditText myEditText = null;
 	private IMusicPlayer mpInterface;
 	private TextView percentField;
 	private Button cancelButton;
 	private MusicFileTask musicFileTask;
 	private RefreshViewTask refreshViewTask;
 	private View progressBar;
 //	private NotificationManager nm;
 //	private static final int APP_ID = 0;
 	private static final int DIALOG_OK_MESSAGE = 1;
 	  
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 		Eula.show(this); //have user aggree to end user license aggreement
 		fileHelper.deletePreviousInstall();
 //		fileHelper.setupTestData(getResources().openRawResource(R.raw.r2d2));//create some test data
 //		nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
 		
 		//Variables used by MusicFileTask
 		percentField = (TextView) findViewById(R.id.percent_field);
 		percentField.setText(" Working!");
 		percentField.setTextColor(android.graphics.Color.CYAN);
 		
 		cancelButton = (Button) findViewById(R.id.cancel_button);
 		cancelButton.setOnClickListener(new CancelButtonListener());
 
 		progressBar = findViewById(R.id.progress);
 		progressBar.setVisibility(View.VISIBLE);
 		//This AsyncTest will recursively search for music files and update the listview
 		musicFileTask = new MusicFileTask();
 		musicFileTask.execute(this);
 
 		//Bind the MusicPlayerService to this activity
 		this.bindService(new Intent(MusicSweeperActivity.this,MusicPlayerService.class), 
 					serviceConnection, Context.BIND_AUTO_CREATE);
 
 		createListView();
 		
 		MyIndexerAdapter<String> aa = createListAndAdapter();		
 		createMyEditText(aa);
 
 		setupButton();
 		// This will allow context menu for each item in the list
 		registerForContextMenu(getListView());
 	}
 	
 	private void createMyEditText(MyIndexerAdapter<String> aa) {
 		myEditText = (EditText) findViewById(R.id.editText);
 		setMyEditText(myEditText, aa);
 //		setMyEditText("");
 	}
 
 	public void setMyEditText(EditText myEditText, final MyIndexerAdapter<String> aa) {
 		this.myEditText = myEditText;
 		this.myEditText.addTextChangedListener(new TextWatcher() {
 
 			public void afterTextChanged(Editable s) {}
 
 			public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
 
 			public void onTextChanged(CharSequence s, int start, int before, int count) {
 				aa.getFilter().filter(s);
 				aa.notifyDataSetChanged();
 			}
 		});	
 	}
 
 	public void setMyEditText(String string) {
 		getMyEditText().setText(string);
 	}
 	
 	public EditText getMyEditText() {
 		return myEditText;
 	}
 
 	private void setupButton() {
 		Button refresh = (Button) findViewById(R.id.refresh_button);
 		refresh.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View view) {
 				stopSong();
 				progressBar.setVisibility(View.VISIBLE);
 				removeFilesNotFoundFromFileMap();
 			}
 		});
 	}
 
 	private MyIndexerAdapter<String> createListAndAdapter() {
 		// Create the array adapter to bind the array to the listview
 		final MyIndexerAdapter<String> aa = new MyIndexerAdapter<String>(
 				getApplicationContext(), android.R.layout.simple_list_item_1,
 				getMusicFilesAsList());
 		// Bind the array adapter to the listview.
 		getListView().setAdapter(aa);
 
 		// By using setTextFilterEnabled method in listview we can filter the listview items.
 		getListView().setTextFilterEnabled(true);
 		return aa;
 	}
 
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
 		super.onCreateContextMenu(menu, v, menuInfo);
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.context_menu, menu);
 	}
 
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		super.onContextItemSelected(item);
 		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
 		switch (item.getItemId()) {
 		case R.id.stoppreview:
 			stopSong();	
 			return true;
 		case R.id.preview:
 			playSong(info.id);
 			return true;
 		case R.id.delete:
 			deleteThisFile(info.id);
 			return true;
 		case R.id.help:
 			showHelp(info.id);
 			return true;
 		default:
 //			return super.onContextItemSelected(item);
 			showHelp(info.id);
 			return true;
 		}
 	}
 
 	/**
 	 * @param menu
 	 * @param v
 	 * @param menuInfo
 	 * phone button menu
 	 */
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);
 	    MenuInflater inflater = getMenuInflater();
 	    inflater.inflate(R.menu.phone_menu, menu);
 	    return true;
 	}
 
 	/* (non-Javadoc)
 	 * phone button menu
 	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
 	 */
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		super.onContextItemSelected(item);
 		switch (item.getItemId()) {
 		case R.id.stoppreview:
 			stopSong();	
 			return true;
 		case R.id.hardrefresh:
 			hardRefresh();
 			return true;
 		case R.id.quit:
 			onStop();
 			moveTaskToBack(true);
 			return true;
 		case R.id.help:
 			showAppHelp();
 			return true;
 		default:
 			showAppHelp();
 			return true;
 		}
 	}
 	
 	private void stopSong() {
 		try {
 			if (mpInterface != null) mpInterface.stopSong();
 		} catch (Exception e) {
 			Log.e(TAG, STOPPING_MUSIC_EXCEPTION, e);
 		} finally {
 			try {
 				if (mpInterface != null) mpInterface.stopSong();
 			} catch (Exception e) {
 				Log.e(TAG, STOPPING_MUSIC_EXCEPTION, e);
 			}
 		}
 	}
 
 	private void playSong(long id) {
 		String item = myListView.getItemAtPosition((int) id).toString();
 		playSong(item);
 	}
 	
 	private void playSong(String item) {
 		String msg = "file not found";
 		File file = new File(getMusicFiles().get(item));
 		if (file.exists()) {
 			Toast.makeText(this, "Playing preview of song " + item, Toast.LENGTH_LONG).show();
 			playSnippet(file.getPath());
 		} else {
 			Toast.makeText(this, msg, Toast.LENGTH_LONG).show();			
 		}
 	}
 
 	private void playSnippet(String songPath) {
 		try {
 			mpInterface.setSongPath(songPath);
 			mpInterface.playSong(songPath);
 		} catch (Exception e) {
 			Log.e(TAG, "Exception thrown while playing music file ", e);
 			try {
 				if (mpInterface != null) mpInterface.stopSong();
 			} catch (Exception e1) {
 				Log.e(TAG, STOPPING_MUSIC_EXCEPTION, e);
 			}
 		} finally {
 			try {
 			} catch (Exception e) {
 				Log.e(TAG, STOPPING_MUSIC_EXCEPTION, e);
 			}
 		}
 	}
 
 	private void deleteThisFile(long id) {
 		String key = myListView.getItemAtPosition((int) id).toString();
 //		setMyEditText(key);
 		deleteThisFile(key);
 	}
 
 	private void deleteThisFile(String key) {
 		stopSong();
 		String msg = "file not found";
 		String item = getMusicFiles().get(key);
 		File file = new File(item);
 		if (file.exists()) {
 			boolean deleted = file.delete();
 			if (deleted) {
 				msg = "you just deleted " + file.getPath();
 				Log.d(TAG, msg);
 				removeFromMusicFileMap(key);
 				refreshView();
 			} else {
 				msg = "couldn't delete " + item;
 			}
 		}
 
 		Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
 	}
 
 	private void showHelp(long id) {
 		String item = myListView.getItemAtPosition((int) id).toString();
 		Toast.makeText(
 			this, "Delete will permanently remove the file from your phone "
 			+ item, Toast.LENGTH_LONG).show();
 	}
 	
 	private void showAppHelp() {
 		//Toast.makeText(this, getApplicationHelpMsg(), Toast.LENGTH_LONG).show();	
         showDialog(DIALOG_OK_MESSAGE);
 	}
 	
     @Override
     protected Dialog onCreateDialog(int id) {
         switch (id) {
         case DIALOG_OK_MESSAGE:
             return new AlertDialog.Builder(this)
            	.setIcon(R.drawable.icon)
                 .setTitle("Music Sweeper")
                 .setMessage(getApplicationHelpMsg())
                 .setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int whichButton) {
                         /* User clicked OK so do some stuff */
                     }
                 })
                 .create();
         	}
 		return null;
         }
 
     private String getApplicationHelpMsg() {
		return "Use reset button for soft reset (re-order) list \r\n" +
 		"Use back button or hard refresh to re-query for songs \r\n" +
		"Use home button or quit to stop using the application \r\n";
     }
     
     private String getReadmeHelpMsg() {
     	return "AUTHOR: Gary Tipton \r\n" +
     	"\r\n" +
     	"This is my first version of an application that will make deleting mp3 files \r\n" + 
     	"that were down loaded by various software \r\n" + 
     	"and stored in many different locations on the sdcard. \r\n" +
     	"\r\n" +
     	"COMPATIBILITY: Android 1.6 (<uses-sdk android:minSdkVersion=4 />) \r\n" +
     	"\r\n" +
     	"This application has the following features: \r\n" +
     	"- Recursively reads the SD card for files ending in .mp3 then displays in a listview \r\n" +
     	"- Short press plays song in background via service \r\n" +
     	"- Long press provides stop preview, preview, delete, and help options \r\n" +
     	"- Menu button provides stop preview, hard refresh (researches for mp3's), quit (move to background), help \r\n" +
     	"- Back button performs a refresh of the view  \r\n" +
     	"- Home button close the app (move to background) \r\n" +
     	"\r\n" +
     	"TODO/ideas: \r\n" +
     	"- Provide multiple delete functionality via multiple select of files \r\n";    	
     }
 
 	void showToast(CharSequence msg) {
         Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
     }
 	
 	@Override
 	public void onPause() {
 		if (this.isFinishing()) {
 			stopSong();
 			progressBar.setVisibility(View.INVISIBLE);
 		}
 		super.onPause();
 	}
 	
 	@Override
 	public void onStop() {
 		if (this.isFinishing()) {
 			stopSong();
 			progressBar.setVisibility(View.INVISIBLE);
 		}
 		super.onStop();
 	}
 
 	@Override
 	protected void onDestroy() {
 		try {
 			stopSong();
 			if (mpInterface != null) mpInterface = null;
 		} finally {
 			try {
 			super.onDestroy();
 			} catch (Exception e) {
 				Log.e(TAG, "onDestroy failed!", e);	
 			}
 		}
 	}
 	
 	@Override
 	public void onBackPressed() {
 		hardRefresh();	
 	}
 
 	private void hardRefresh() {
 		this.onStop();
 		try {
 			progressBar.setVisibility(View.VISIBLE);
 			cancelButton.setVisibility(View.VISIBLE);
 			setMusicFiles(new HashMap<String, String>()); //forces a new list to be created.
 			musicFileTask = new MusicFileTask();
 			musicFileTask.execute(this);
 		} catch (Exception e) {
 			Log.e(TAG, "failure executing musicFileTask in onBackPressed", e);
 		}
 	}
 	
 	private void refreshView() {
 		MyIndexerAdapter<String> aa = createListAndAdapter();		
 		createMyEditText(aa);
 		displayNoFilesMessage();
 	}
 	
 	private void removeFilesNotFoundFromFileMap() {
 		//This AsyncTest will recursively remove files from listview
 		refreshViewTask = new RefreshViewTask();
 		refreshViewTask.execute(this);
 	}
 
 	private void displayNoFilesMessage() {
 		if (getListView() == null || getMusicFiles() == null || getMusicFiles().isEmpty()) {
 			percentField.setText(" No Files!");
 			percentField.setTextColor(android.graphics.Color.RED);
 		}
 		progressBar.setVisibility(View.INVISIBLE);
 	}
 
 	public ListView getListView() {
 		return myListView;
 	}
 
 	public void createListView() {
 		this.myListView = (ListView) findViewById(R.id.fileListView);
 		this.myListView.setOnItemClickListener(
 			new android.widget.AdapterView.OnItemClickListener() {
 				@Override
 				public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
 					String item = getListView().getItemAtPosition(position).toString();
 					playSong(item);					
 				}
 		});		
 	}
 
 	private ServiceConnection serviceConnection = new ServiceConnection() {
 		public void onServiceConnected(ComponentName className, IBinder service) {
 			mpInterface = IMusicPlayer.Stub.asInterface((IBinder)service);
 		}
 
 		public void onServiceDisconnected(ComponentName className) {
 			mpInterface = null;
 		}
     };	
 
 	protected class CancelButtonListener implements View.OnClickListener {
 		public void onClick(View v) {
 			musicFileTask.cancel(true);
 		}
 	}
     
 	private void removeFromMusicFileMap(String key) {
 		if (!getMusicFiles().containsValue(key)) {
 			key = fileHelper.stripSongNameAndLeadingNumbers(key);
 		}
 		try {
 			getMusicFiles().remove(key);
 		} catch(Exception e) {
 			Log.e(TAG, "remove from music file map failed ", e);
 		}
 	}
 
 	public List<String> getMusicFilesAsList() {
 		List<String> list = new ArrayList<String>(getMusicFiles().keySet());
 		if (list != null && !list.isEmpty())
 			Collections.sort(list);
 		return list;
 	}
 	
 	public Map<String,String> getMusicFiles() {
 		return musicFiles;
 	}
 
 	private void setMusicFiles(Map<String,String> musicFiles) {
 		this.musicFiles = musicFiles;
 	}
 	
 	public void addMusicFiles(Map<String,String> musicFiles) {
 		getMusicFiles().putAll(musicFiles);
 	}
 	
 // ******** INNER CLASSES **************** //
     //TODO: asynch task try to move this to it's
 	/**
 	 * sub-class of AsyncTask
 	 */
 	protected class MusicFileTask extends AsyncTask<Context, Integer, String> {
 		// -- run intensive processes here
 		// -- notice that the datatype of the first param in the class
 		// definition matches the param passed to this method
 		// -- and that the datatype of the last param in the class definition
 		// matches the return type of this method
 		@Override
 		protected String doInBackground(Context... params) {
 			// -- on every iteration
 			// -- gets all music files under each directory one at a time
 			// -- publishes the progress - calls the onProgressUpdate handler defined below
 			try {
 			if (fileHelper.hasExternalStorage()) {
 				File rootDir = fileHelper.getRootDir();
 				if (rootDir != null && rootDir.exists()) {
 					int i = 0;
 //					get mp3 directly on root of sdcard
 					File[] filteredFiles = rootDir.listFiles(fileHelper.getFileFilter());
 					if (filteredFiles != null) {
 						for (File file: filteredFiles) {
 							getMusicFiles().putAll(fileHelper.getFileList(file));
 							publishProgress(i);
 							i++;
 						}
 					}//Now recursively search all directories				
 					for (File file: rootDir.listFiles()) {
 						getMusicFiles().putAll(fileHelper.getFileList(file));
 						publishProgress(i);
 						i++;
 					}
 					publishProgress(i);
 				}
 			}
 			displayNoFilesMessage();
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 			return "COMPLETE!";
 		}
 		// -- called from the publish progress
 		// -- notice that the datatype of the second param gets passed to this
 		// method
 		@Override
 		protected void onProgressUpdate(Integer... values) {
 			try {
 				if (values != null) {
 					Log.i(TAG, "onProgressUpdate(): " + String.valueOf(values[0]));
 					percentField.setText(values[0] + " files");
 					percentField.setTextColor(android.graphics.Color.GREEN);
 				}
 			} catch(Exception e) {
 				Log.e(TAG, "Failure trying to update percentField", e);
 			}
 			refreshView();
 	        super.onProgressUpdate(values);
 		}
 		// -- called if the cancel button is pressed
 		@Override
 		protected void onCancelled() {
 			super.onCancelled();
 			Log.d(TAG, "onCancelled()");
 			percentField.setText(" Cancelled!");
 			percentField.setTextColor(0xFFFF0000);
 			progressBar.setVisibility(View.INVISIBLE);
 //			setProgressBarVisibility(false);
 		}
 		// -- called as soon as doInBackground method completes
 		// -- notice that the third param gets passed to this method
 		@Override
 		protected void onPostExecute(String result) {
 			Log.i(TAG, "onPostExecute(): " + result);
 			try {
 				percentField.setText(" " + getMusicFiles().size() + " " + result);
 			} catch(Exception e) {
 				percentField.setText(" " + result);
 			}
 			percentField.setTextColor(0xFF69adea);
 			cancelButton.setVisibility(View.INVISIBLE);
 			progressBar.setVisibility(View.INVISIBLE);
 			super.onPostExecute(result);
 		}
 	}    
     //TODO: end of asynctask
 
 	protected class RefreshViewTask extends AsyncTask<Context, Integer, String> {
 		// -- run intensive processes here
 		// -- notice that the datatype of the first param in the class
 		// definition matches the param passed to this method
 		// -- and that the datatype of the last param in the class definition
 		// matches the return type of this method
 		@Override
 		protected String doInBackground(Context... params) {
 			int i = fileHelper.removeFilesNotFoundFromFileMap(getMusicFiles());
 			publishProgress(i);
 			return "COMPLETE!";
 		}
 		@Override
 		protected void onProgressUpdate(Integer... values) {
 			if (values != null && values.length > 0) {
 				String msg = String.valueOf(values[0]) + " Refreshed";
 				Toast.makeText(MusicSweeperActivity.this, msg, Toast.LENGTH_SHORT).show();
 			}
 	        super.onProgressUpdate(values);
 		}
 	}	
 }
