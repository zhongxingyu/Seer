 package com.cs371m.notesync;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 
 import java.util.Calendar;
 import java.util.Currency;
 
 import java.util.Locale;
 
 import android.app.ActionBar;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.DialogFragment;
 import android.app.FragmentTransaction;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.database.Cursor;
 import android.media.MediaPlayer;
 import android.media.MediaRecorder;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.os.PowerManager;
 import android.os.PowerManager.WakeLock;
 import android.provider.MediaStore;
 import android.provider.MediaStore.Images.ImageColumns;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentPagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.text.format.Time;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.animation.Animation;
 import android.view.animation.AnimationUtils;
 import android.widget.AdapterView;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class MainActivity extends FragmentActivity implements ActionBar.TabListener {
 
 	private static int currentIndex;
 	private static final String LOG_TAG = "AudioRecordTest";
 	private static String mFileName = null;
 
 	private ImageView mRecordButton = null; //Button
 	private MediaRecorder mRecorder = null;
 
 	private Button   mPlayButton = null;
 	private MediaPlayer   mPlayer = null;
 	private Long startRecTime=null;
 	boolean mStartRecording, mStartPlaying;
 
 	private WakeLock mWakeLock;
 	private RecordService mBoundRecService;
 	PlaybackService mBoundPlayService;
 	private boolean mIsRecBound = false;
 	private boolean mIsPlayBound = false;
 	protected static ArrayList<Note> notes;
 	Note mCurrentNote;
 	protected static ArrayList<Long> tempTimestamps;
 	//0: Title 1: Class Name 2: Tag(s)
 	//Create enumeration class of the 3 types above
 	protected static EditText [] txtInputVals= new EditText[3];
 	protected static String [] inputVals= new String[3];
 	static Time time = new Time();
 	private static boolean clickedOk=false; 
 	private static boolean isRecording=false;
 	public static final int DIALOG_TUTORIAL=1;
 	int numTagsSoFar;
 	/**
 	 * The {@link android.support.v4.view.PagerAdapter} that will provide
 	 * fragments for each of the sections. We use a
 	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
 	 * will keep every loaded fragment in memory. If this becomes too memory
 	 * intensive, it may be best to switch to a
 	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
 	 */
 	SectionsPagerAdapter mSectionsPagerAdapter;
 
 	/**
 	 * The {@link ViewPager} that will host the section contents.
 	 */
 	public ViewPager mViewPager;
 
 	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
 	public static final int MEDIA_TYPE_IMAGE = 1;
 
 	private static Uri fileUri;
 
 	private static String fileString;
 
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 
 		if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
 			if (resultCode == RESULT_OK) {
 //				if (data == null)
 //					Toast.makeText(this, "Image saved correctly", Toast.LENGTH_SHORT).show();
 //				else
 //					Toast.makeText(this, "Image saved to:\n" +
 //							data.getData(), Toast.LENGTH_LONG).show();
 			} else if (resultCode == RESULT_CANCELED) {
 				// User cancelled the image capture
 			} else {
 				// Image capture failed, advise user
 			}
 		}
 		if (requestCode == GALLERY_PICTURE) {
 			if (resultCode == RESULT_OK) {
 				if (data != null) {
 					Cursor cursor = getContentResolver().query(data.getData(),
 							null, null, null, null);
 					if (cursor != null) {
 
 						cursor.moveToFirst();
 
 						int idx = cursor.getColumnIndex(ImageColumns.DATA);
 						fileString = cursor.getString(idx);
 					}
 				}
 			}
 		}
 
 		showEditRecInfoDialog();
 	}
 
 	private Intent pictureActionIntent = null;
 	protected static final int CAMERA_REQUEST = 0;
 	protected static final int GALLERY_PICTURE = 1;
 
 	private void startDialog() {
 		AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(this);
 		myAlertDialog.setTitle("Upload Pictures Option");
 		myAlertDialog.setMessage("Attach visual note from:");
 
 		myAlertDialog.setPositiveButton("Gallery",
 				new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface arg0, int arg1) {
 				pictureActionIntent = new Intent(
 						Intent.ACTION_GET_CONTENT, null);
 				pictureActionIntent.setType("image/*");
 				pictureActionIntent.putExtra("return-data", true);
 				startActivityForResult(pictureActionIntent,
 						GALLERY_PICTURE);
 			}
 		});
 
 		myAlertDialog.setNegativeButton("Camera",
 				new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface arg0, int arg1) {
 				startCameraIntent();
 			}
 		});
 		myAlertDialog.show();
 	}
 
 	public void startCameraIntent() {
 		// create Intent to take a picture and return control to the calling application
 		pictureActionIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
 
 		fileUri = Helper.getOutputMediaFileUri(MEDIA_TYPE_IMAGE, this.getApplicationContext()); // create a file to save the image
 
 		fileString = fileUri.getPath();
 		//TODO: Not saving to correct output path.
 		/*
 		startRecTime = "TESTNAME"; //ignore DST?
 
 		String temp = this.getFilesDir().getAbsolutePath();
 
 		temp += File.separator+startRecTime+".jpg";
 		fileUri = Uri.fromFile(new File(temp));
 		 */
 
 		pictureActionIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file name
 
 		// start the image capture Intent
 		startActivityForResult(pictureActionIntent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
 	}
 
 	public void showEditRecInfoDialog() {
 		// Create an instance of the dialog fragment and show it
 		DialogFragment dialog = new EditRecInfoDialogFragment();
 		dialog.show(getFragmentManager(), "Edit Title Fragment");
 	}
 
 	public void showReEditRecInfoDialog() {
 		// Create an instance of the dialog fragment and show it
 		DialogFragment dialog = new ReEditRecInfoDialogFragment();
 		dialog.show(getFragmentManager(), "Edit Title Fragment");
 	}
 
 	// The dialog fragment receives a reference to this Activity through the
 	// Fragment.onAttach() callback, which it uses to call the following methods
 	// defined by the NoticeDialogFragment.NoticeDialogListener interface
 	public void onDialogPositiveClick() {
 		// User touched the dialog's positive button
 		Log.v(LOG_TAG, "clicked on pos");
 	}
 
 	public void onDialogNegativeClick(DialogFragment dialog) {
 		// User touched the dialog's negative button
 		Log.v(LOG_TAG, "clicked on neg");
 	}
 
 	/*  RecordService  */
 
 	private ServiceConnection mRecConnection = new ServiceConnection() {
 		public void onServiceConnected(ComponentName className, IBinder service) {
 			// This is called when the connection with the service has been
 			// established, giving us the service object we can use to
 			// interact with the service.  Because we have bound to a explicit
 			// service that we know is running in our own process, we can
 			// cast its IBinder to a concrete class and directly access it.
 			mBoundRecService = ((RecordService.LocalBinder)service).getService();
 
 			// Tell the user about this for our demo.
 //			Toast.makeText(mBoundRecService, "RecordService connected",
 //					Toast.LENGTH_SHORT).show();
 		}
 
 		public void onServiceDisconnected(ComponentName className) {
 			// This is called when the connection with the service has been
 			// unexpectedly disconnected -- that is, its process crashed.
 			// Because it is running in our same process, we should never
 			// see this happen.
 //			Toast.makeText(mBoundRecService, "RecordService disconnected",
 //					Toast.LENGTH_SHORT).show();
 			mBoundRecService = null;
 
 		}
 	};
 
 	void doBindRecService() {
 		// Establish a connection with the service.  We use an explicit
 		// class name because we want a specific service implementation that
 		// we know will be running in our own process (and thus won't be
 		// supporting component replacement by other applications).
 		boolean ret = bindService(new Intent(this, 
 				RecordService.class), mRecConnection, Context.BIND_AUTO_CREATE);
 		mIsRecBound = true;
 	}
 
 	void doUnbindRecService() {
 		if (mIsRecBound) {
 			// Detach our existing connection.
 			unbindService(mRecConnection);
 			mIsRecBound = false;
 		}
 	}
 
 	private void onRecord(boolean start) {
 		if (start) 
 		{
 			isRecording=true;
 			startRecording();
 		} else 
 		{
 			isRecording=false;
 			stopRecording();
 		}
 	}
 
 	private void startRecording() 
 	{
 		if (mIsRecBound) {
 			time.setToNow();
 			startRecTime = time.toMillis(false);
 			mBoundRecService.Record();
 		}
 	}
 
 	private void stopRecording() {
 		if (mIsRecBound) {
 			mFileName = ((RecordService) mBoundRecService).mFileName;
 			mBoundRecService.Stop();
 		}
 		startDialog();
 	}
 
 	public void onClickStartRec(View v) {
 		onRecord(mStartRecording);
 
 		if (mStartRecording) {
 			mRecordButton=(ImageView) findViewById(R.id.mRecordButton);
 			mRecordButton.setImageResource(R.drawable.stop_button); 
 			//mRecordButton.setText(R.string.stopRecord);
 		} else {
 			mRecordButton.setImageResource(R.drawable.record_button); 
 			//mRecordButton.setText(R.string.startRecord);
 		}
 		mStartRecording = !mStartRecording;
 	}
 
 	public void onClickMakeTag(View v)
 	{
 		Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake);
 		findViewById(R.id.mTagButton).startAnimation(shake);
 		//Get time since start of recording
 		if (isRecording)
 		{
 			time.setToNow();
 			Long currTime=time.toMillis(false);
 			Long offset = currTime - startRecTime;
 			//add to temp arrayList of timestamps
 			tempTimestamps.add(offset);
 			//Displays number of tags made so far
 			numTagsSoFar++;
 			Toast.makeText(this, "Tag #: "+numTagsSoFar,Toast.LENGTH_SHORT).show();
 		}
 	}
 
 	/*  PlaybackService  */
 
 	private ServiceConnection mPlayConnection = new ServiceConnection() {
 		public void onServiceConnected(ComponentName className, IBinder service) {
 			// This is called when the connection with the service has been
 			// established, giving us the service object we can use to
 			// interact with the service.  Because we have bound to a explicit
 			// service that we know is running in our own process, we can
 			// cast its IBinder to a concrete class and directly access it.
 			mBoundPlayService = ((PlaybackService.LocalBinder)service).getService();
 		}
 
 		public void onServiceDisconnected(ComponentName className) {
 			// This is called when the connection with the service has been
 			// unexpectedly disconnected -- that is, its process crashed.
 			// Because it is running in our same process, we should never
 			// see this happen.
 
 			mBoundPlayService = null;
 
 		}
 	};
 
 
 	void doBindPlayService() {
 		// Establish a connection with the service.  We use an explicit
 		// class name because we want a specific service implementation that
 		// we know will be running in our own process (and thus won't be
 		// supporting component replacement by other applications).
 		boolean ret = bindService(new Intent(this, 
 				PlaybackService.class), mPlayConnection, Context.BIND_AUTO_CREATE);
 		mIsPlayBound = true;
 	}
 
 	void doUnbindPlayService() {
 		if (mIsPlayBound) {
 			// Detach our existing connection.
 			unbindService(mPlayConnection);
 			mIsPlayBound = false;
 		}
 	}
 
 	/* Dialog */
 
 	public static class EditRecInfoDialogFragment extends DialogFragment 
 	{
 		public interface EditRecInfoDialogListener 
 		{
 			public void onDialogPositiveClick(DialogFragment dialog);
 			public void onDialogNegativeClick(DialogFragment dialog);
 		}
 
 		//EditRecInfoDialogListener mListener;
 		/*
 		// Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
 	    @Override
 	    public void onAttach(Activity activity) 
 	    {
 	        super.onAttach(activity);
 	        // Verify that the host activity implements the callback interface
 	        try 
 	        {
 	            // Instantiate the NoticeDialogListener so we can send events to the host
 	            mListener = (EditRecInfoDialogListener) activity;
 	        } catch (ClassCastException e) {
 	            // The activity doesn't implement the interface, throw exception
 	            throw new ClassCastException(activity.toString()
 	                    + " must implement NoticeDialogListener");
 	        }
 	    }
 		 */
 		public Dialog onCreateDialog(Bundle savedInstanceState) 
 		{
 			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity ());
 			// Get the layout inflater
 			LayoutInflater inflater = getActivity ().getLayoutInflater();
 			//DialogInterface onClickInterface=new DialogInterface.OnClickListener();
 			// Inflate and set the layout for the dialog
 			// Pass null as the parent view because its going in the dialog layout
 			builder.setView(inflater.inflate(R.layout.edit_title_dialog, null))
 			// Add action buttons
 			.setPositiveButton(R.string.done, new DialogInterface.OnClickListener() {
 				@Override
 				public void onClick(DialogInterface dialog, int id) 
 				{
 
 					//Retrieve entered Title, Course name and Tags.
 					txtInputVals[0]= (EditText) ((AlertDialog) dialog).findViewById(R.id.changeTitle); 
 					inputVals[0]= txtInputVals[0].getText().toString();
 					txtInputVals[1]= (EditText) ((AlertDialog) dialog).findViewById(R.id.changeClass); 
 					inputVals[1]= txtInputVals[1].getText().toString();
 					clickedOk=true;
 
 				}
 
 			})
 			.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int id) {
 					//Give default name
 					EditRecInfoDialogFragment.this.getDialog().cancel();
 				}
 			}); 
 			//Return the created dialogue box
 			return builder.create();
 
 		}
 
 		@Override
 		public void onDetach()
 		{
 			super.onDetach();
 			Log.v(LOG_TAG, "onDetach() dialog\n");
 			if (clickedOk)
 			{
 				//Fill in a Note obj w/ the input str values
 				//Add this entry to notebook view
 				Note perNote= new Note();
 				perNote.topic=inputVals[0];
 				perNote.course=inputVals[1];
 
 				Calendar cal = Calendar.getInstance();
 				cal.add(Calendar.DATE, 1);
 				SimpleDateFormat format1 = new SimpleDateFormat("MM-dd-yyyy");
 
 				String formatted = format1.format(cal.getTime());
 
 				perNote.date = formatted;
 				//Update timeStamp
 
 				//Debug
 				perNote.timestamps=tempTimestamps;
 				perNote.recording = mFileName;
 				//perNote.image = fileUri.getPath();
 				perNote.image = fileString;
 				//Rest temparray list
 				notes.add(perNote);
 				tempTimestamps=new ArrayList<Long>();
 			}
 			clickedOk=false;
 		}
 
 	}
 
 	/* Other MainActivity stuff */
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.activity_main);
 		//Initialize number of tags made so far
 		
 		mStartRecording = true;
 		mStartPlaying = true;
 		//FIXME: doesn't work b/c fragment not visible
 		//mRecordButton=(Button) findViewById(R.id.mRecordButton);
 		//mPlayButton=(Button) findViewById(R.id.mPlayButton);
 
 		// Set up the action bar.
 		final ActionBar actionBar = getActionBar();
 		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
 
 		tempTimestamps= new ArrayList<Long>();
 		numTagsSoFar=0;
 		
 		// Create the adapter that will return a fragment for each of the three
 		// primary sections of the app.
 		mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
 
 		// Set up the ViewPager with the sections adapter.
 		mViewPager = (ViewPager) findViewById(R.id.pager);
 		mViewPager.setAdapter(mSectionsPagerAdapter);
 
 		mWakeLock = ((PowerManager)this.getSystemService(Context.POWER_SERVICE)).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "recordlock");
 
 		// When swiping between different sections, select the corresponding
 		// tab. We can also use ActionBar.Tab#select() to do this if we have
 		// a reference to the Tab.
 		mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
 			@Override
 			public void onPageSelected(int position) {
 				actionBar.setSelectedNavigationItem(position);
 			}
 		});
 
 		// For each of the sections in the app, add a tab to the action bar.
 		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
 			// Create a tab with text corresponding to the page title defined by
 			// the adapter. Also specify this Activity object, which implements
 			// the TabListener interface, as the callback (listener) for when
 			// this tab is selected.
 			actionBar.addTab(
 					actionBar.newTab()
 					.setText(mSectionsPagerAdapter.getPageTitle(i))
 					.setTabListener(this));
 		}
 
 		//ListView list = (ListView)findViewById(android.R.id.list);
 		//registerForContextMenu(list);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.record_screen, menu);
 		return true;
 	}
 
 	@Override
 	public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
 		// When the given tab is selected, switch to the corresponding page in
 		// the ViewPager.
 		mViewPager.setCurrentItem(tab.getPosition());
 		if (tab.getPosition() == 1) {
 			FragmentPagerAdapter adapter = (FragmentPagerAdapter) mViewPager.getAdapter();
 			if (adapter != null) {
 				NotesViewFragment frag = (NotesViewFragment) adapter.instantiateItem(mViewPager, 1);
 				if (frag != null)
 					frag.updateList();
 
 				ListView list = (ListView) findViewById(android.R.id.list);
 				if (list != null)
 					registerForContextMenu(list);
 
 			}
 		} else if (tab.getPosition() == 2) {
 			FragmentPagerAdapter adapter = (FragmentPagerAdapter) mViewPager.getAdapter();
 			if (adapter != null) {
 				StudyViewFragment frag = (StudyViewFragment) adapter.instantiateItem(mViewPager, 2);
 				if (frag != null)
 					frag.updateStudyView();
 			}
 		}
 	}
 
 	@Override
 	public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
 	}
 
 	@Override
 	public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
 	}
 
 	/**
 	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
 	 * one of the sections/tabs/pages.
 	 */
 	public class SectionsPagerAdapter extends FragmentPagerAdapter {
 
 		public SectionsPagerAdapter(FragmentManager fm) {
 			super(fm);
 		}
 
 		@Override
 		public Fragment getItem(int position) {
 			// getItem is called to instantiate the fragment for the given page.
 			// Return a DummySectionFragment (defined as a static inner class
 			// below) with the page number as its lone argument.
 			switch(position) {
 			case 0: 
 				return new RecordViewFragment();
 			case 1: //FIXME: also called on init? never called on button press...
 				return new NotesViewFragment();
 			case 2: //FIXME: called on middle view for some reason.., seamlessly transitions to third screen...?
 				return new StudyViewFragment();
 				//debug bundle
 				//Bundle bundle = new Bundle();
 				//bundle.putString(StudyViewFragment.ARG_IMAGE_PATH, "/123.jpg"); //no img/ if getDir
 				//bundle.putString(StudyViewFragment.ARG_RECORDING_PATH, "/test.mp3");
 				//frag.setArguments(bundle);
 				//return frag;
 			default: //should never be called
 				Fragment fragment = new DummySectionFragment();
 				Bundle args = new Bundle();
 				args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, position + 1);
 				fragment.setArguments(args);
 				return fragment;
 			}
 		}
 
 		@Override
 		public int getCount() {
 			// Show 3 total pages.
 			return 3;
 		}
 
 		@Override
 		public CharSequence getPageTitle(int position) {
 			Locale l = Locale.getDefault();
 			switch (position) {
 			case 0:
 				return getString(R.string.title_section1).toUpperCase(l);
 			case 1:
 				return getString(R.string.title_section2).toUpperCase(l);
 			case 2:
 				return getString(R.string.title_section3).toUpperCase(l);
 			}
 			return null;
 		}
 	}
 
 	/**
 	 * A dummy fragment representing a section of the app, but that simply
 	 * displays dummy text.
 	 */
 	public static class DummySectionFragment extends Fragment {
 		/**
 		 * The fragment argument representing the section number for this
 		 * fragment.
 		 */
 		public static final String ARG_SECTION_NUMBER = "section_number";
 
 		public DummySectionFragment() {
 		}
 
 		@Override
 		public View onCreateView(LayoutInflater inflater, ViewGroup container,
 				Bundle savedInstanceState) {
 			View rootView = inflater.inflate(R.layout.fragment_main_dummy, container, false);
 			TextView dummyTextView = (TextView) rootView.findViewById(R.id.section_label);
 			dummyTextView.setText(Integer.toString(getArguments().getInt(ARG_SECTION_NUMBER)));
 
 			return rootView;
 		}
 	}
 	
 	protected Dialog onCreateDialog(int id)
 	{
 			Dialog dialog = null;
 			AlertDialog.Builder builder = new AlertDialog.Builder(this);
 			switch(id)
 			{
 			
 					case DIALOG_TUTORIAL:
 						// Create the quit confirmation dialog
 						builder.setMessage(R.string.helpRecord)
 						.setCancelable(true)
 						.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener()
 						{
 							public void onClick(DialogInterface dialog, int id)
 							{
 								//Make a toast
 							}
 						});
 						dialog = builder.create();
 						break;
 
 			}
 			return dialog;
 		}
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) 
 		{
 			//Tutorial/Help Dialog
 			case R.id.help:
 				showDialog(DIALOG_TUTORIAL);
 				return true;
 		}
 		return false;
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		doBindRecService();
 		doBindPlayService();
 
 		if (mWakeLock == null) {
 			mWakeLock = ((PowerManager)this.getSystemService(Context.POWER_SERVICE)).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "recordlock");
 		}
 		try {
 			notes = Helper.loadNotes(this.getApplicationContext());
 			//tempTimestamps= new ArrayList<Long>();
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			notes = new ArrayList<Note>();
 
 		}
 	}
 
 	@Override
 	public void onPause() {
 		super.onPause();
 		//doUnbindService();
 		if (mRecorder != null) {
 			mRecorder.release();
 			mRecorder = null;
 		}
 
 		if (mPlayer != null) {
 			mPlayer.release();
 			mPlayer = null;
 		}
 		try {
 			Helper.writeNotes(notes, false, this.getApplicationContext());
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	protected void onStop() {
 		super.onStop();
 		//doUnbindService();
 		try {
 			Helper.writeNotes(notes, false, this.getApplicationContext());
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 		//doUnbindService();
 		try {
 			Helper.writeNotes(notes, false, this.getApplicationContext());
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v,
 			ContextMenuInfo menuInfo) {
 		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
 		menu.setHeaderTitle(notes.get(notes.size() - info.position - 1).topic);
 		String[] menuItems = getResources().getStringArray(R.array.menu);
 		for (int i = 0; i<menuItems.length; i++) {
 			menu.add(Menu.NONE, i, i, menuItems[i]);
 		}
 
 	}
 
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
 		int menuItemIndex = item.getItemId();
 		String[] menuItems = getResources().getStringArray(R.array.menu);
 		String menuItemName = menuItems[menuItemIndex];
 		String listItemName = notes.get(notes.size() - info.position - 1).topic;
 
 		switch(item.getItemId()) {
 		case 0:
 			currentIndex = info.position;
 			showReEditRecInfoDialog();
 			break;
 		case 1:
 			ListView list = (ListView) findViewById(android.R.id.list);
 			NotesAdapter adapter = (NotesAdapter) list.getAdapter();
 			adapter.remove(adapter.getItem(info.position));
 			adapter.notifyDataSetChanged();
 			notes.remove(notes.size() - info.position - 1);
 		}
 		return super.onContextItemSelected(item);
 	}
 
 	/* Second edit Dialog */
 
 	public static class ReEditRecInfoDialogFragment extends DialogFragment 
 	{
 		public interface ReEditRecInfoDialogListener 
 		{
 			public void onDialogPositiveClick(DialogFragment dialog);
 			public void onDialogNegativeClick(DialogFragment dialog);
 		}
 
 
 		public Dialog onCreateDialog(Bundle savedInstanceState) 
 		{
 			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity ());
 			// Get the layout inflater
 			LayoutInflater inflater = getActivity ().getLayoutInflater();
 			final View dialogView = inflater.inflate(R.layout.edit_title_dialog, null);
 			txtInputVals[0] = (EditText) dialogView.findViewById(R.id.changeTitle);
 			txtInputVals[1] = (EditText) dialogView.findViewById(R.id.changeClass);
 			//DialogInterface onClickInterface=new DialogInterface.OnClickListener();
 			// Inflate and set the layout for the dialog
 			// Pass null as the parent view because its going in the dialog layout
 			builder.setView(dialogView)
 			// Add action buttons
 			.setPositiveButton(R.string.done, new DialogInterface.OnClickListener() {
 				@Override
 				public void onClick(DialogInterface dialog, int id) 
 				{
 
 					//Retrieve entered Title, Course name and Tags.
 					//txtInputVals[0]= (EditText) ((AlertDialog) dialog).findViewById(R.id.changeTitle); 
 					inputVals[0]= txtInputVals[0].getText().toString();
 					//txtInputVals[1]= (EditText) ((AlertDialog) dialog).findViewById(R.id.changeClass); 
 					inputVals[1]= txtInputVals[1].getText().toString();
 					clickedOk=true;
 
 				}
 
 			})
 			.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int id) {
 					//Give default name
 					ReEditRecInfoDialogFragment.this.getDialog().cancel();
 				}
 			}); 
 			txtInputVals[0].setText(notes.get(notes.size() - currentIndex - 1).topic);
 			txtInputVals[1].setText(notes.get(notes.size() - currentIndex - 1).course);
 
 
 			//Return the created dialogue box
 			return builder.create();
 
 		}
 
 
 
 		@Override
 		public void onDetach()
 		{
 			super.onDetach();
 			Log.v(LOG_TAG, "onDetach() dialog\n");
 			if (clickedOk)
 			{
 				//Fill in a Note obj w/ the input str values
 				//Add this entry to notebook view
 				Note perNote= notes.get(notes.size() - currentIndex - 1);
 				perNote.topic=inputVals[0];
 				perNote.course=inputVals[1];
 			}
 			clickedOk=false;
 		}
 
 	}
 }
