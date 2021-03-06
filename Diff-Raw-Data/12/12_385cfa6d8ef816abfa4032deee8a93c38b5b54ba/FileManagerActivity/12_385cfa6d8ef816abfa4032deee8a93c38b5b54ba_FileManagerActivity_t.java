 /* 
  * Copyright (C) 2008 OpenIntents.org
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 /*
  * Based on AndDev.org's file browser V 2.0.
  */
 
 package org.openintents.filemanager;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.openintents.distribution.DistributionLibraryListActivity;
 import org.openintents.filemanager.util.FileUtils;
 import org.openintents.filemanager.util.MimeTypeParser;
 import org.openintents.filemanager.util.MimeTypes;
 import org.openintents.intents.FileManagerIntents;
 import org.openintents.util.MenuIntentOptionsWithIcons;
 import org.xmlpull.v1.XmlPullParserException;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.ActivityNotFoundException;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
 import android.content.pm.PackageManager;
 import android.content.pm.ResolveInfo;
 import android.content.res.XmlResourceParser;
 import android.graphics.drawable.Drawable;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.preference.PreferenceManager;
 import android.text.TextUtils;
 import android.text.format.DateFormat;
 import android.text.format.Formatter;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.Window;
 import android.widget.AdapterView;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.BaseAdapter;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class FileManagerActivity extends DistributionLibraryListActivity implements OnSharedPreferenceChangeListener { 
 	private static final String TAG = "FileManagerActivity";
 
 	private static final String NOMEDIA_FILE = ".nomedia";
 	
 	/**
 	 * @since 2011-03-23
 	 */
 	private static final Character FILE_EXTENSION_SEPARATOR = '.';
 	
 	private int mState;
 	
 	private static final int STATE_BROWSE = 1;
 	private static final int STATE_PICK_FILE = 2;
 	private static final int STATE_PICK_DIRECTORY = 3;
 	private static final int STATE_MULTI_SELECT = 4;
     
 	protected static final int REQUEST_CODE_MOVE = 1;
 	protected static final int REQUEST_CODE_COPY = 2;
 
     /**
      * @since 2011-02-11
      */
     private static final int REQUEST_CODE_MULTI_SELECT = 3;
 
 	private static final int MENU_PREFERENCES = Menu.FIRST + 3;
 	private static final int MENU_NEW_FOLDER = Menu.FIRST + 4;
 	private static final int MENU_DELETE = Menu.FIRST + 5;
 	private static final int MENU_RENAME = Menu.FIRST + 6;
 	private static final int MENU_SEND = Menu.FIRST + 7;
 	private static final int MENU_OPEN = Menu.FIRST + 8;
 	private static final int MENU_MOVE = Menu.FIRST + 9;
 	private static final int MENU_COPY = Menu.FIRST + 10;
 	/**
      * @since 2011-09-29
      */
     private static final int MENU_MORE = Menu.FIRST + 11;
 	private static final int MENU_INCLUDE_IN_MEDIA_SCAN = Menu.FIRST + 12;
 	private static final int MENU_EXCLUDE_FROM_MEDIA_SCAN = Menu.FIRST + 13;
 	private static final int MENU_SETTINGS = Menu.FIRST + 14;
 	private static final int MENU_MULTI_SELECT = Menu.FIRST + 15;
 	private static final int MENU_FILTER = Menu.FIRST + 16;
 	private static final int MENU_DETAILS = Menu.FIRST + 17;
 	private static final int MENU_DISTRIBUTION_START = Menu.FIRST + 100; // MUST BE LAST
 	
 	private static final int DIALOG_NEW_FOLDER = 1;
 	private static final int DIALOG_DELETE = 2;
 	private static final int DIALOG_RENAME = 3;
 
 	/**
      * @since 2011-02-12
      */
     private static final int DIALOG_MULTI_DELETE = 4;
     private static final int DIALOG_FILTER = 5;
 	private static final int DIALOG_DETAILS = 6;
 
     private static final int DIALOG_DISTRIBUTION_START = 100; // MUST BE LAST
 
 	private static final int COPY_BUFFER_SIZE = 32 * 1024;
 	
 	private static final String BUNDLE_CURRENT_DIRECTORY = "current_directory";
 	private static final String BUNDLE_CONTEXT_FILE = "context_file";
 	private static final String BUNDLE_CONTEXT_TEXT = "context_text";
 	private static final String BUNDLE_SHOW_DIRECTORY_INPUT = "show_directory_input";
 	private static final String BUNDLE_STEPS_BACK = "steps_back";
 	
 	/** Contains directories and files together */
      private ArrayList<IconifiedText> directoryEntries = new ArrayList<IconifiedText>();
 
      /** Dir separate for sorting */
      List<IconifiedText> mListDir = new ArrayList<IconifiedText>();
      
      /** Files separate for sorting */
      List<IconifiedText> mListFile = new ArrayList<IconifiedText>();
      
      /** SD card separate for sorting */
      List<IconifiedText> mListSdCard = new ArrayList<IconifiedText>();
      
      // There's a ".nomedia" file here
      private boolean mNoMedia;
      
      private File currentDirectory = new File(""); 
      
      private String mSdCardPath = "";
      
      private MimeTypes mMimeTypes;
      /** Files shown are filtered using this extension */
      private String mFilterFiletype = "";
      /** Files shown are filtered using this mimetype */
      private String mFilterMimetype = null;
 
      private String mContextText;
      private File mContextFile = new File("");
      private Drawable mContextIcon;
      
      /** How many steps one can make back using the back key. */
      private int mStepsBack;
      
      private EditText mEditFilename;
      private Button mButtonPick;
      private LinearLayout mDirectoryButtons;
      
      /**
       * @since 2011-02-11
       */
      private Button mButtonMove;
 
      /**
       * @since 2011-02-11
       */
      private Button mButtonCopy;
 
      /**
       * @since 2011-02-11
       */
      private Button mButtonDelete;
      
      private boolean fileDeleted = false;
      private int positionAtDelete;
     private boolean deletedFileIsDirectory = false;
 
      private LinearLayout mDirectoryInput;
      private EditText mEditDirectory;
      private ImageButton mButtonDirectoryPick;
      
      /**
       * @since 2011-02-11
       */
      private LinearLayout mActionNormal;
 
      /**
       * @since 2011-02-11
       */
      private LinearLayout mActionMultiselect;
 
      private TextView mEmptyText;
      private ProgressBar mProgressBar;
      
      private DirectoryScanner mDirectoryScanner;
      private File mPreviousDirectory;
      private ThumbnailLoader mThumbnailLoader;
      
      private MenuItem mExcludeMediaScanMenuItem;
      private MenuItem mIncludeMediaScanMenuItem;
      
      private Handler currentHandler;
 
 	private boolean mWritableOnly;
 
     private IconifiedText[] mDirectoryEntries;
 
  	 static final public int MESSAGE_SHOW_DIRECTORY_CONTENTS = 500;	// List of contents is ready, obj = DirectoryContents
      static final public int MESSAGE_SET_PROGRESS = 501;	// Set progress bar, arg1 = current value, arg2 = max value
      static final public int MESSAGE_ICON_CHANGED = 502;	// View needs to be redrawn, obj = IconifiedText
 
      /** Called when the activity is first created. */ 
      @Override 
      public void onCreate(Bundle icicle) { 
           super.onCreate(icicle); 
 
           mDistribution.setFirst(MENU_DISTRIBUTION_START, DIALOG_DISTRIBUTION_START);
           
           // Check whether EULA has been accepted
           // or information about new version can be presented.
           if (mDistribution.showEulaOrNewVersion()) {
               return;
           }
 
           currentHandler = new Handler() {
 			public void handleMessage(Message msg) {
 				FileManagerActivity.this.handleMessage(msg);
 			}
 		};
 
 		  requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
           setContentView(R.layout.filelist);
           
           
           SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
           prefs.registerOnSharedPreferenceChangeListener(this);
 
           
           mEmptyText = (TextView) findViewById(R.id.empty_text);
           mProgressBar = (ProgressBar) findViewById(R.id.scan_progress);
 
 		  getListView().setOnCreateContextMenuListener(this);
 		  getListView().setEmptyView(findViewById(R.id.empty));
 	      getListView().setTextFilterEnabled(true);
 	      getListView().requestFocus();
 	      getListView().requestFocusFromTouch();
 	      
           mDirectoryButtons = (LinearLayout) findViewById(R.id.directory_buttons);
           mActionNormal = (LinearLayout) findViewById(R.id.action_normal);
           mActionMultiselect = (LinearLayout) findViewById(R.id.action_multiselect);
           mEditFilename = (EditText) findViewById(R.id.filename);
           
 
           mButtonPick = (Button) findViewById(R.id.button_pick);
           
           mButtonPick.setOnClickListener(new View.OnClickListener() {
 				
 				public void onClick(View arg0) {
 					pickFileOrDirectory();
 				}
           });
           
           // Initialize only when necessary:
           mDirectoryInput = null;
           
           // Create map of extensions:
           getMimeTypes();
           
           getSdCardPath();
           
           mState = STATE_BROWSE;
           
           Intent intent = getIntent();
           String action = intent.getAction();
           
           File browseto = new File("/");
           
           if (!TextUtils.isEmpty(mSdCardPath)) {
         	  browseto = new File(mSdCardPath);
           }
           
           // Default state
           mState = STATE_BROWSE;
           mWritableOnly = false;
           
           if (action != null) {
         	  
         	  if (action.equals(FileManagerIntents.ACTION_PICK_FILE)) {
         		  mState = STATE_PICK_FILE;        		
         		  mFilterFiletype = intent.getStringExtra("FILE_EXTENSION");
         		  if(mFilterFiletype == null)
         			  mFilterFiletype = "";
         		  mFilterMimetype = intent.getType();
         		  if(mFilterMimetype == null)
         			  mFilterMimetype = "";
           	  } else if (action.equals(FileManagerIntents.ACTION_PICK_DIRECTORY)) {
         		  mState = STATE_PICK_DIRECTORY;        		          		          
         		  mWritableOnly = intent.getBooleanExtra(FileManagerIntents.EXTRA_WRITEABLE_ONLY, false);
         		  
         		  // Remove edit text and make button fill whole line
         		  mEditFilename.setVisibility(View.GONE);
         		  mButtonPick.setLayoutParams(new LinearLayout.LayoutParams(
         				  LinearLayout.LayoutParams.FILL_PARENT,
         				  LinearLayout.LayoutParams.WRAP_CONTENT));
         	  } else if (action.equals(FileManagerIntents.ACTION_MULTI_SELECT)) {
 	    		  mState = STATE_MULTI_SELECT;        		          		          
 	    		  
 	    		  // Remove buttons
                   mDirectoryButtons.setVisibility(View.GONE);
                   mActionNormal.setVisibility(View.GONE);
 
                   // Multi select action: move
 	              mButtonMove = (Button) findViewById(R.id.button_move);
 	              mButtonMove.setOnClickListener(new View.OnClickListener() {
 	                    
 	                    public void onClick(View arg0) {
 	                        if (checkSelection()) {
 	                            promptDestinationAndMoveFile();
 	                        }
 	                    }
 	              });
 	              
 	              // Multi select action: copy
 	              mButtonCopy = (Button) findViewById(R.id.button_copy);
 	              mButtonCopy.setOnClickListener(new View.OnClickListener() {
 	                    
 	                    public void onClick(View arg0) {
                             if (checkSelection()) {
                                 promptDestinationAndCopyFile();
                             }
 	                    }
 	              });
 	            
 	              // Multi select action: delete
 	              mButtonDelete = (Button) findViewById(R.id.button_delete);
 	              mButtonDelete.setOnClickListener(new View.OnClickListener() {
 	                    
 	                    public void onClick(View arg0) {
                             if (checkSelection()) {
                                 showDialog(DIALOG_MULTI_DELETE);
                             }
 	                    }
 	              });
 	            
   	    	  } 
     	  
           } 
           
           if (mState == STATE_BROWSE) {
         	  // Remove edit text and button.
         	  mEditFilename.setVisibility(View.GONE);
         	  mButtonPick.setVisibility(View.GONE);
           }
 
           if (mState != STATE_MULTI_SELECT) {
     		  // Remove multiselect action buttons
     		  mActionMultiselect.setVisibility(View.GONE);
           }
 
           // Set current directory and file based on intent data.
     	  File file = FileUtils.getFile(intent.getData());
     	  if (file != null) {
     		  File dir = FileUtils.getPathWithoutFilename(file);
     		  if (dir.isDirectory()) {
     			  browseto = dir;
     		  }
     		  if (!file.isDirectory()) {
     			  mEditFilename.setText(file.getName());
     		  }
     	  }
     	  
     	  String title = intent.getStringExtra(FileManagerIntents.EXTRA_TITLE);
     	  if (title != null) {
     		  setTitle(title);
     	  }
 
     	  String buttontext = intent.getStringExtra(FileManagerIntents.EXTRA_BUTTON_TEXT);
     	  if (buttontext != null) {
     		  mButtonPick.setText(buttontext);
     	  }
     	  
           mStepsBack = 0;
           
           if (icicle != null) {
         	  browseto = new File(icicle.getString(BUNDLE_CURRENT_DIRECTORY));
         	  mContextFile = new File(icicle.getString(BUNDLE_CONTEXT_FILE));
         	  mContextText = icicle.getString(BUNDLE_CONTEXT_TEXT);
         	  
         	  boolean show = icicle.getBoolean(BUNDLE_SHOW_DIRECTORY_INPUT);
         	  showDirectoryInput(show);
         	  
         	  mStepsBack = icicle.getInt(BUNDLE_STEPS_BACK);
           }
           
           browseTo(browseto);
      }
      
      public void onDestroy() {
     	 super.onDestroy();
     	 
     	 // Stop the scanner.
     	 DirectoryScanner scanner = mDirectoryScanner;
     	 
     	 if (scanner != null) {
     		 scanner.cancel = true;
     	 }
     	 
     	 mDirectoryScanner = null;
     	 
     	 ThumbnailLoader loader = mThumbnailLoader;
     	 
     	 if (loader != null) {
     		 loader.cancel = true;
     		 mThumbnailLoader = null;
     	 }
      }
      
      private void handleMessage(Message message) {
 //    	 Log.v(TAG, "Received message " + message.what);
     	 
     	 switch (message.what) {
     	 case MESSAGE_SHOW_DIRECTORY_CONTENTS:
     		 showDirectoryContents((DirectoryContents) message.obj);
     		 break;
     		 
     	 case MESSAGE_SET_PROGRESS:
     		 setProgress(message.arg1, message.arg2);
     		 break;
     		 
     	 case MESSAGE_ICON_CHANGED:
     		 notifyIconChanged((IconifiedText) message.obj);
     		 break;
     	 }
      }
      
      private void notifyIconChanged(IconifiedText text) {
     	 if (getListAdapter() != null) {
     		 ((BaseAdapter) getListAdapter()).notifyDataSetChanged();
     	 }
      }
      
      private void setProgress(int progress, int maxProgress) {
     	 mProgressBar.setMax(maxProgress);
     	 mProgressBar.setProgress(progress);
     	 mProgressBar.setVisibility(View.VISIBLE);
      }
      
      private void showDirectoryContents(DirectoryContents contents) {
     	 mDirectoryScanner = null;
     	 
     	 mListSdCard = contents.listSdCard;
     	 mListDir = contents.listDir;
     	 mListFile = contents.listFile;
     	 mNoMedia = contents.noMedia;
     	 
     	 directoryEntries.ensureCapacity(mListSdCard.size() + mListDir.size() + mListFile.size());
     	 
          addAllElements(directoryEntries, mListSdCard);
          addAllElements(directoryEntries, mListDir);
          addAllElements(directoryEntries, mListFile);
           
          mDirectoryEntries = directoryEntries.toArray(new IconifiedText[0]); 
 
          IconifiedTextListAdapter itla = new IconifiedTextListAdapter(this); 
          itla.setListItems(directoryEntries, getListView().hasTextFilter());          
          setListAdapter(itla); 
 	     getListView().setTextFilterEnabled(true);
 	     
 	     if(fileDeleted){
 	    	 getListView().setSelection(positionAtDelete);
 	     }
 
          selectInList(mPreviousDirectory);
          refreshDirectoryPanel();
          setProgressBarIndeterminateVisibility(false);
 
     	 mProgressBar.setVisibility(View.GONE);
     	 mEmptyText.setVisibility(View.VISIBLE);
     	 
     	 mThumbnailLoader = new ThumbnailLoader(currentDirectory, mListFile, currentHandler, this, mMimeTypes);
     	 mThumbnailLoader.start();
      }
 
      private void onCreateDirectoryInput() {
     	 mDirectoryInput = (LinearLayout) findViewById(R.id.directory_input);
          mEditDirectory = (EditText) findViewById(R.id.directory_text);
 
          mButtonDirectoryPick = (ImageButton) findViewById(R.id.button_directory_pick);
          
          mButtonDirectoryPick.setOnClickListener(new View.OnClickListener() {
 				
 				public void onClick(View arg0) {
 					goToDirectoryInEditText();
 				}
          });
      }
      
      //private boolean mHaveShownErrorMessage;
      private File mHaveShownErrorMessageForFile = null;
      
      private void goToDirectoryInEditText() {
     	 File browseto = new File(mEditDirectory.getText().toString());
     	 
     	 if (browseto.equals(currentDirectory)) {
     		 showDirectoryInput(false);
     	 } else {
     		 if (mHaveShownErrorMessageForFile != null 
     				 && mHaveShownErrorMessageForFile.equals(browseto)) {
     			 // Don't let user get stuck in wrong directory.
     			 mHaveShownErrorMessageForFile = null;
         		 showDirectoryInput(false);
     		 } else {
 	    		 if (!browseto.exists()) {
 	    			 // browseTo() below will show an error message,
 	    			 // because file does not exist.
 	    			 // It is ok to show this the first time.
 	    			 mHaveShownErrorMessageForFile = browseto;
 	    		 }
 				 browseTo(browseto);
     		 }
     	 }
      }
      
      /**
       * Show the directory line as input box instead of button row.
       * If Directory input does not exist yet, it is created.
       * Since the default is show == false, nothing is created if
       * it is not necessary (like after icicle).
       * @param show
       */
      private void showDirectoryInput(boolean show) {
     	 if (show) {
     		 if (mDirectoryInput == null) {
         		 onCreateDirectoryInput();
         	 }
     	 }
     	 if (mDirectoryInput != null) {
 	    	 mDirectoryInput.setVisibility(show ? View.VISIBLE : View.GONE);
 	    	 mDirectoryButtons.setVisibility(show ? View.GONE : View.VISIBLE);
     	 }
     	 
     	 refreshDirectoryPanel();
      }
 
  	/**
  	 * 
  	 */
  	private void refreshDirectoryPanel() {
  		if (isDirectoryInputVisible()) {
  			// Set directory path
  			String path = currentDirectory.getAbsolutePath();
  			mEditDirectory.setText(path);
  			
  			// Set selection to last position so user can continue to type:
  			mEditDirectory.setSelection(path.length());
  		} else {
  			setDirectoryButtons();
  		}
  	} 
      /*
      @Override
 	protected void onResume() {
 		// TODO Auto-generated method stub
 		super.onResume();
 	}
 */
      
 
  	@Override
  	protected void onSaveInstanceState(Bundle outState) {
  		// TODO Auto-generated method stub
  		super.onSaveInstanceState(outState);
  		
  		// remember file name
  		outState.putString(BUNDLE_CURRENT_DIRECTORY, currentDirectory.getAbsolutePath());
  		outState.putString(BUNDLE_CONTEXT_FILE, mContextFile.getAbsolutePath());
  		outState.putString(BUNDLE_CONTEXT_TEXT, mContextText);
  		boolean show = isDirectoryInputVisible();
  		outState.putBoolean(BUNDLE_SHOW_DIRECTORY_INPUT, show);
  		outState.putInt(BUNDLE_STEPS_BACK, mStepsBack);
  	}
 
 	/**
 	 * @return
 	 */
 	private boolean isDirectoryInputVisible() {
 		return ((mDirectoryInput != null) && (mDirectoryInput.getVisibility() == View.VISIBLE));
 	}
 
 	private void pickFileOrDirectory() {
 		File file = null;
 		if (mState == STATE_PICK_FILE) {
 			String filename = mEditFilename.getText().toString();
 			file = FileUtils.getFile(currentDirectory.getAbsolutePath(), filename);
 		} else if (mState == STATE_PICK_DIRECTORY) {
 			file = currentDirectory;
 		}
     	 
     	Intent intent = getIntent();
     	intent.setData(FileUtils.getUri(file));
     	setResult(RESULT_OK, intent);
     	finish();
      }
      
 	/**
 	 * 
 	 */
      private void getMimeTypes() {
     	 MimeTypeParser mtp = new MimeTypeParser();
 
     	 XmlResourceParser in = getResources().getXml(R.xml.mimetypes);
 
     	 try {
     		 mMimeTypes = mtp.fromXmlResource(in);
     	 } catch (XmlPullParserException e) {
     		 Log
     		 .e(
     				 TAG,
     				 "PreselectedChannelsActivity: XmlPullParserException",
     				 e);
     		 throw new RuntimeException(
     		 "PreselectedChannelsActivity: XmlPullParserException");
     	 } catch (IOException e) {
     		 Log.e(TAG, "PreselectedChannelsActivity: IOException", e);
     		 throw new RuntimeException(
     		 "PreselectedChannelsActivity: IOException");
     	 }
      } 
       
      /** 
       * This function browses up one level 
       * according to the field: currentDirectory 
       */ 
      private void upOneLevel(){
     	 if (mStepsBack > 0) {
     		 mStepsBack--;
     	 }
          if(currentDirectory.getParent() != null) 
                browseTo(currentDirectory.getParentFile()); 
      } 
       
      /**
       * Jump to some location by clicking on a 
       * directory button.
       * 
       * This resets the counter for "back" actions.
       * 
       * @param aDirectory
       */
      private void jumpTo(final File aDirectory) {
     	 mStepsBack = 0;
     	 browseTo(aDirectory);
      }
      
      /**
       * Browse to some location by clicking on a list item.
       * @param aDirectory
       */
      private void browseTo(final File aDirectory){ 
           // setTitle(aDirectory.getAbsolutePath());
           
           if (aDirectory.isDirectory()){
         	  if (aDirectory.equals(currentDirectory)) {
         		  // Switch from button to directory input
         		  showDirectoryInput(true);
         	  } else {
         		   mPreviousDirectory = currentDirectory;
 	               currentDirectory = aDirectory;
 	               refreshList();
 //	               selectInList(previousDirectory);
 	//               refreshDirectoryPanel();
         	  }
           }else{ 
         	  if (mState == STATE_BROWSE || mState == STATE_PICK_DIRECTORY) {
 	              // Lets start an intent to View the file, that was clicked... 
 	        	  openFile(aDirectory); 
         	  } else if (mState == STATE_PICK_FILE) {
         		  // Pick the file
         		  mEditFilename.setText(aDirectory.getName());
         	  }
           } 
      }
 
       
      private void openFile(File aFile) { 
     	 if (!aFile.exists()) {
     		 Toast.makeText(this, R.string.error_file_does_not_exists, Toast.LENGTH_SHORT).show();
     		 return;
     	 }
     	 
           Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
 
           Uri data = FileUtils.getUri(aFile);
           String type = mMimeTypes.getMimeType(aFile.getName());
           intent.setDataAndType(data, type);
 
      	 // Were we in GET_CONTENT mode?
      	 Intent originalIntent = getIntent();
      	 
      	 if (originalIntent != null && originalIntent.getAction() != null && originalIntent.getAction().equals(Intent.ACTION_GET_CONTENT)) {
     		 // In that case, we should probably just return the requested data.
      		 intent.setData(Uri.parse(FileManagerProvider.FILE_PROVIDER_PREFIX + aFile));
      		 setResult(RESULT_OK, intent);
      		 finish();
     		 return;
     	 }
     	 
 
           
           try {
         	  startActivity(intent); 
           } catch (ActivityNotFoundException e) {
         	  Toast.makeText(this, R.string.application_not_available, Toast.LENGTH_SHORT).show();
           };
      } 
 
      private void refreshList() {
     	     	 
     	 boolean directoriesOnly = mState == STATE_PICK_DIRECTORY;
     	 
     	  // Cancel an existing scanner, if applicable.
     	  DirectoryScanner scanner = mDirectoryScanner;
     	  
     	  if (scanner != null) {
     		  scanner.cancel = true;
     	  }
 
     	  ThumbnailLoader loader = mThumbnailLoader;
     	  
     	  if (loader != null) {
     		  loader.cancel = true;
     		  mThumbnailLoader = null;
     	  }
     	  
     	  directoryEntries.clear(); 
           mListDir.clear();
           mListFile.clear();
           mListSdCard.clear();
           
           setProgressBarIndeterminateVisibility(true);
           
           // Don't show the "folder empty" text since we're scanning.
           mEmptyText.setVisibility(View.GONE);
           
           // Also DON'T show the progress bar - it's kind of lame to show that
           // for less than a second.
           mProgressBar.setVisibility(View.GONE);
           setListAdapter(null); 
           
 		  mDirectoryScanner = new DirectoryScanner(currentDirectory, this, currentHandler, mMimeTypes, mFilterFiletype, mFilterMimetype, mSdCardPath, mWritableOnly, directoriesOnly);
 		  mDirectoryScanner.start();
 		  
 		  
            
           // Add the "." == "current directory" 
           /*directoryEntries.add(new IconifiedText( 
                     getString(R.string.current_dir), 
                     getResources().getDrawable(R.drawable.ic_launcher_folder)));        */
           // and the ".." == 'Up one level' 
           /*
           if(currentDirectory.getParent() != null) 
                directoryEntries.add(new IconifiedText( 
                          getString(R.string.up_one_level), 
                          getResources().getDrawable(R.drawable.ic_launcher_folder_open))); 
           */
      } 
      
      private void selectInList(File selectFile) {
     	 String filename = selectFile.getName();
     	 IconifiedTextListAdapter la = (IconifiedTextListAdapter) getListAdapter();
     	 int count = la.getCount();
     	 for (int i = 0; i < count; i++) {
     		 IconifiedText it = (IconifiedText) la.getItem(i);
     		 if (it.getText().equals(filename)) {
     			 getListView().setSelection(i);
     			 break;
     		 }
     	 }
      }
      
      private void addAllElements(List<IconifiedText> addTo, List<IconifiedText> addFrom) {
     	 int size = addFrom.size();
     	 for (int i = 0; i < size; i++) {
     		 addTo.add(addFrom.get(i));
     	 }
      }
      
      private void setDirectoryButtons() {
     	 String[] parts = currentDirectory.getAbsolutePath().split("/");
     	 
     	 mDirectoryButtons.removeAllViews();
     	 
     	 int WRAP_CONTENT = LinearLayout.LayoutParams.WRAP_CONTENT;
     	 
     	 // Add home button separately
     	 ImageButton ib = new ImageButton(this);
     	 ib.setImageResource(R.drawable.ic_launcher_home_small);
 		 ib.setLayoutParams(new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
 		 ib.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View view) {
 				jumpTo(new File("/"));
 			}
 		 });
 		 mDirectoryButtons.addView(ib);
 		 
     	 // Add other buttons
     	 
     	 String dir = "";
     	 
     	 for (int i = 1; i < parts.length; i++) {
     		 dir += "/" + parts[i];
     		 if (dir.equals(mSdCardPath)) {
     			 // Add SD card button
     			 ib = new ImageButton(this);
     	    	 ib.setImageResource(R.drawable.icon_sdcard_small);
     			 ib.setLayoutParams(new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
     			 ib.setOnClickListener(new View.OnClickListener() {
     					public void onClick(View view) {
     						jumpTo(new File(mSdCardPath));
     					}
     			 });
     			 mDirectoryButtons.addView(ib);
     		 } else {
 	    		 Button b = new Button(this);
 	    		 b.setLayoutParams(new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
 	    		 b.setText(parts[i]);
 	    		 b.setTag(dir);
 	    		 b.setOnClickListener(new View.OnClickListener() {
 	 				public void onClick(View view) {
 	 					String dir = (String) view.getTag();
 	 					jumpTo(new File(dir));
 	 				}
 	    		 });
     			 mDirectoryButtons.addView(b);
     		 }
     	 }
     	 
     	 checkButtonLayout();
      }
 
      private void checkButtonLayout() {
     	 
     	 // Let's measure how much space we need:
     	 int spec = View.MeasureSpec.UNSPECIFIED;
     	 mDirectoryButtons.measure(spec, spec);
     	 int count = mDirectoryButtons.getChildCount();
     	 
     	 int requiredwidth = mDirectoryButtons.getMeasuredWidth();
     	 int width = getWindowManager().getDefaultDisplay().getWidth();
     	 
     	 if (requiredwidth > width) {
         	 int WRAP_CONTENT = LinearLayout.LayoutParams.WRAP_CONTENT;
         	 
         	 // Create a new button that shows that there is more to the left:
         	 ImageButton ib = new ImageButton(this);
         	 ib.setImageResource(R.drawable.ic_menu_back_small);
     		 ib.setLayoutParams(new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
     		 // 
     		 ib.setOnClickListener(new View.OnClickListener() {
     				public void onClick(View view) {
     					// Up one directory.
     					upOneLevel();
     				}
     		 });
     		 mDirectoryButtons.addView(ib, 0);
     		 
     		 // New button needs even more space
     		 ib.measure(spec, spec);
     		 requiredwidth += ib.getMeasuredWidth();
 
     		 // Need to take away some buttons
     		 // but leave at least "back" button and one directory button.
     		 while (requiredwidth > width && mDirectoryButtons.getChildCount() > 2) {
     			 View view = mDirectoryButtons.getChildAt(1);
     			 requiredwidth -= view.getMeasuredWidth();
     			 
 	    		 mDirectoryButtons.removeViewAt(1);
     		 }
     	 }
      }
      
      @Override 
      protected void onListItemClick(ListView l, View v, int position, long id) { 
           super.onListItemClick(l, v, position, id); 
           
           IconifiedTextListAdapter adapter = (IconifiedTextListAdapter) getListAdapter();
           
           if (adapter == null) {
         	  return;
           }
           
           IconifiedText text = (IconifiedText) adapter.getItem(position);
 
           if (mState == STATE_MULTI_SELECT) {
         	  text.setSelected(!text.isSelected());
         	  adapter.notifyDataSetChanged();
         	  return;
           }
 			
           String file = text.getText(); 
           /*
           if (selectedFileString.equals(getString(R.string.up_one_level))) { 
                upOneLevel(); 
           } else { 
           */
         	  String curdir = currentDirectory 
               .getAbsolutePath() ;
         	  File clickedFile = FileUtils.getFile(curdir, file);
                if (clickedFile != null) {
             	   if (clickedFile.isDirectory()) {
             		   // If we click on folders, we can return later by the "back" key.
             		   mStepsBack++;
             	   }
                     browseTo(clickedFile);
                }
           /*
           } 
           */
      }
 
      private void getSdCardPath() {
     	 mSdCardPath = android.os.Environment
 			.getExternalStorageDirectory().getAbsolutePath();
      }
      
 
  	@Override
  	public boolean onCreateOptionsMenu(Menu menu) {
  		super.onCreateOptionsMenu(menu);
 
  		menu.add(0, MENU_NEW_FOLDER, 0, R.string.menu_new_folder).setIcon(
  				android.R.drawable.ic_menu_add).setShortcut('0', 'f');
  
  		if (mState == STATE_BROWSE) {
  		// Multi select option menu.
  	        menu.add(0, MENU_MULTI_SELECT, 0, R.string.menu_multi_select).setIcon(
  	                R.drawable.ic_menu_multiselect).setShortcut('1', 'm');
         }
 			
 		mIncludeMediaScanMenuItem = menu.add(0, MENU_INCLUDE_IN_MEDIA_SCAN, 0, R.string.menu_include_in_media_scan).setShortcut('2', 's')
 				.setIcon(android.R.drawable.ic_menu_gallery);
 		mExcludeMediaScanMenuItem = menu.add(0, MENU_EXCLUDE_FROM_MEDIA_SCAN, 0, R.string.menu_exclude_from_media_scan).setShortcut('2', 's')
 				.setIcon(android.R.drawable.ic_menu_gallery);
 
 		menu.add(0, MENU_SETTINGS, 0, R.string.settings).setIcon(
 				android.R.drawable.ic_menu_preferences).setShortcut('9', 's');
 		
 		/* We don't want to allow the user to override a filter set
 		 * by an application.
 		 */
 		if(mState != STATE_PICK_FILE) {
 			menu.add(0, MENU_FILTER, 0, R.string.menu_filter).setIcon(
 					android.R.drawable.ic_menu_search);
 		}
 
  		mDistribution.onCreateOptionsMenu(menu);
  		
  		return true;
  	}
 
 
 	@Override
 	public boolean onPrepareOptionsMenu(Menu menu) {
 		super.onPrepareOptionsMenu(menu);
 
 		mIncludeMediaScanMenuItem.setVisible(false);
 		mExcludeMediaScanMenuItem.setVisible(false);
 		
 		boolean showMediaScanMenuItem = PreferenceActivity.getMediaScanFromPreference(this);
 		
  		// We only know about ".nomedia" once we have the results list back.
  		if (showMediaScanMenuItem && mListDir != null) {
 			if (mNoMedia) {
 				mIncludeMediaScanMenuItem.setVisible(true);
 			} else {
 				mExcludeMediaScanMenuItem.setVisible(true);
  			}
  		}
 
 		// Generate any additional actions that can be performed on the
 		// overall list. This allows other applications to extend
 		// our menu with their own actions.
 		Intent intent = new Intent(null, getIntent().getData());
 		intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
 		// menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0,
 		// new ComponentName(this, NoteEditor.class), null, intent, 0, null);
 
 		// Workaround to add icons:
 		MenuIntentOptionsWithIcons menu2 = new MenuIntentOptionsWithIcons(this,
 				menu);
 		menu2.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0,
 				new ComponentName(this, FileManagerActivity.class), null, intent,
 				0, null);
 
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case MENU_NEW_FOLDER:
 			showDialog(DIALOG_NEW_FOLDER);
 			return true;
 			
 		case MENU_MULTI_SELECT:
             promptMultiSelect();
 			return true;
 			
 		case MENU_INCLUDE_IN_MEDIA_SCAN:
 			includeInMediaScan();
 			return true;
 
 		case MENU_EXCLUDE_FROM_MEDIA_SCAN:
 			excludeFromMediaScan();
 			return true;
 			
 		case MENU_SETTINGS:
 			showSettings();
 			return true;
 			
 		case MENU_FILTER:
 			showDialog(DIALOG_FILTER);
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 
 	}
 
     private void showSettings() {
 		Intent intent = new Intent(this, PreferenceActivity.class);
 		startActivity(intent);
 	}
 	
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View view,
 			ContextMenuInfo menuInfo) {
 		AdapterView.AdapterContextMenuInfo info;
 		try {
 			info = (AdapterView.AdapterContextMenuInfo) menuInfo;
 		} catch (ClassCastException e) {
 			Log.e(TAG, "bad menuInfo", e);
 			return;
 		}
 /*
 		Cursor cursor = (Cursor) getListAdapter().getItem(info.position);
 		if (cursor == null) {
 			// For some reason the requested item isn't available, do nothing
 			return;
 		}
 */
         IconifiedTextListAdapter adapter = (IconifiedTextListAdapter) getListAdapter();
         
         if (adapter == null) {
       	  return;
         }
         
         IconifiedText it = (IconifiedText) adapter.getItem(info.position);
 		menu.setHeaderTitle(it.getText());
 		menu.setHeaderIcon(it.getIcon());
 		File file = FileUtils.getFile(currentDirectory, it.getText());
 
 		
 		if (!file.isDirectory()) {
 			if (mState == STATE_PICK_FILE) {
 				// Show "open" menu
 				menu.add(0, MENU_OPEN, 0, R.string.menu_open);
 			}
 			menu.add(0, MENU_SEND, 0, R.string.menu_send);
 		}
 		menu.add(0, MENU_MOVE, 0, R.string.menu_move);
 		
 		if (!file.isDirectory()) {
 			menu.add(0, MENU_COPY, 0, R.string.menu_copy);
 		}
 		
 		menu.add(0, MENU_RENAME, 0, R.string.menu_rename);
 		menu.add(0, MENU_DELETE, 0, R.string.menu_delete);
 
 		//if (!file.isDirectory()) {
 	        Uri data = Uri.fromFile(file);
 	        Intent intent = new Intent(null, data);
 	        String type = mMimeTypes.getMimeType(file.getName());
 	
 	        intent.setDataAndType(data, type);
 	        intent.addCategory(Intent.CATEGORY_SELECTED_ALTERNATIVE);
 	        //intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
 	
 	        Log.v(TAG, "Data=" + data);
 	        Log.v(TAG, "Type=" + type);
 			
 	        if (type != null) {
 	        	// Add additional options for the MIME type of the selected file.
 				menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0,
 						new ComponentName(this, FileManagerActivity.class), null, intent, 0, null);
 	        }
 		//}
 	    menu.add(0, MENU_DETAILS, 0, R.string.menu_details);
         menu.add(0, MENU_MORE, 0, R.string.menu_more);
 	}
 
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		super.onContextItemSelected(item);
 		AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item
 				.getMenuInfo();
 		
 		// Remember current selection
         IconifiedTextListAdapter adapter = (IconifiedTextListAdapter) getListAdapter();
         
         if (adapter == null) {
       	  return false;
         }
         
         IconifiedText ic = (IconifiedText) adapter.getItem(menuInfo.position);
 		mContextText = ic.getText();
 		mContextIcon = ic.getIcon();
 		mContextFile = FileUtils.getFile(currentDirectory, ic.getText());
 		
 		switch (item.getItemId()) {
 		case MENU_OPEN:
             openFile(mContextFile); 
 			return true;
 			
 		case MENU_MOVE:
 			promptDestinationAndMoveFile();
 			return true;
 			
 		case MENU_COPY:
 			promptDestinationAndCopyFile();
 			return true;
 			
 		case MENU_DELETE:
 			showDialog(DIALOG_DELETE);
 			return true;
 
 		case MENU_RENAME:
 			showDialog(DIALOG_RENAME);
 			return true;
 			
 		case MENU_SEND:
 			sendFile(mContextFile);
 			return true;
 		
 		case MENU_DETAILS:
 			showDialog(DIALOG_DETAILS);
 			return true;
 
 		case MENU_MORE:
 			if (!PreferenceActivity.getShowAllWarning(FileManagerActivity.this)) {
 				showMoreCommandsDialog();
 				return true;
 			}
 
 			showWarningDialog();
 
 			return true;
 		}
 
 		return false;
 	}
 	
 	@Override
 	protected Dialog onCreateDialog(int id) {
 
 		switch (id) {
 		case DIALOG_NEW_FOLDER:
 			LayoutInflater inflater = LayoutInflater.from(this);
 			View view = inflater.inflate(R.layout.dialog_new_folder, null);
 			final EditText et = (EditText) view
 					.findViewById(R.id.foldername);
 			et.setText("");
 			return new AlertDialog.Builder(this)
             	.setIcon(android.R.drawable.ic_dialog_alert)
             	.setTitle(R.string.create_new_folder).setView(view).setPositiveButton(
 					android.R.string.ok, new OnClickListener() {
 						
 						public void onClick(DialogInterface dialog, int which) {
 							createNewFolder(et.getText().toString());
 						}
 						
 					}).setNegativeButton(android.R.string.cancel, new OnClickListener() {
 						
 						public void onClick(DialogInterface dialog, int which) {
 							// Cancel should not do anything.
 						}
 						
 					}).create();
 		
 
 		case DIALOG_DELETE:
 			return new AlertDialog.Builder(this).setTitle(getString(R.string.really_delete, mContextText))
             	.setIcon(android.R.drawable.ic_dialog_alert).setPositiveButton(
 					android.R.string.ok, new OnClickListener() {
 						
 						public void onClick(DialogInterface dialog, int which) {
 							deleteFileOrFolder(mContextFile);
 						}
 						
 					}).setNegativeButton(android.R.string.cancel, new OnClickListener() {
 						
 						public void onClick(DialogInterface dialog, int which) {
 							// Cancel should not do anything.
 						}
 						
 					}).create();
 
 		case DIALOG_RENAME:
 			inflater = LayoutInflater.from(this);
 			view = inflater.inflate(R.layout.dialog_new_folder, null);
 			final EditText et2 = (EditText) view
 				.findViewById(R.id.foldername);
 			return new AlertDialog.Builder(this)
             	.setTitle(R.string.menu_rename).setView(view).setPositiveButton(
 					android.R.string.ok, new OnClickListener() {
 						
 						public void onClick(DialogInterface dialog, int which) {
 							
 							renameFileOrFolder(mContextFile, et2.getText().toString());
 						}
 						
 					}).setNegativeButton(android.R.string.cancel, new OnClickListener() {
 						
 						public void onClick(DialogInterface dialog, int which) {
 							// Cancel should not do anything.
 						}
 						
 					}).create();
 
         case DIALOG_MULTI_DELETE:
             String contentText = null;
             int count = 0;
             for (IconifiedText it : mDirectoryEntries) {
                 if (!it.isSelected()) {
                     continue;
                 }
 
                 contentText = it.getText();
                 count++;
             }
             String string;
             if (count == 1) {
                  string = getString(R.string.really_delete, contentText);
             } else {
                 string = getString(R.string.really_delete_multiselect, count);
             }
             return new AlertDialog.Builder(this).setTitle(string)
                 .setIcon(android.R.drawable.ic_dialog_alert).setPositiveButton(
                     android.R.string.ok, new OnClickListener() {
                         
                         public void onClick(DialogInterface dialog, int which) {
                             deleteMultiFile();
     
                             Intent intent = getIntent();
                             setResult(RESULT_OK, intent);
                             finish();
                         }
                         
                     }).setNegativeButton(android.R.string.cancel, new OnClickListener() {
                         
                         public void onClick(DialogInterface dialog, int which) {
                             // Cancel should not do anything.
                         }
                     
                     }).create();
             
         case DIALOG_FILTER:
 			inflater = LayoutInflater.from(this);
 			view = inflater.inflate(R.layout.dialog_new_folder, null);
 			((TextView)view.findViewById(R.id.foldernametext)).setText(R.string.extension);
 			final EditText et3 = (EditText) view
 					.findViewById(R.id.foldername);
 			et3.setText("");
 			return new AlertDialog.Builder(this)
             	.setIcon(android.R.drawable.ic_dialog_alert)
             	.setTitle(R.string.menu_filter).setView(view).setPositiveButton(
 					android.R.string.ok, new OnClickListener() {
 						
 						public void onClick(DialogInterface dialog, int which) {
 							mFilterFiletype = et3.getText().toString().trim();
 							refreshList();
 						}
 						
 					}).setNegativeButton(android.R.string.cancel, new OnClickListener() {
 						
 						public void onClick(DialogInterface dialog, int which) {
 							// Cancel should not do anything.
 						}
 						
 					}).create();
 			
 
         case DIALOG_DETAILS:
         	inflater = LayoutInflater.from(this);
         	view =  inflater.inflate(R.layout.dialog_details, null);
         	        	
         	return new AlertDialog.Builder(this).setTitle(mContextText).
         			setIcon(mContextIcon).setView(view).create();
 		}
 		return super.onCreateDialog(id);
 			
 	}
 
 
 	@Override
 	protected void onPrepareDialog(int id, Dialog dialog) {
 		super.onPrepareDialog(id, dialog);
 		
 		switch (id) {
 		case DIALOG_NEW_FOLDER:
 			EditText et = (EditText) dialog.findViewById(R.id.foldername);
 			et.setText("");
 			break;
 
 		case DIALOG_DELETE:
 			((AlertDialog) dialog).setTitle(getString(R.string.really_delete, mContextText));
 			break;
 			
 		case DIALOG_RENAME:
 			et = (EditText) dialog.findViewById(R.id.foldername);
 			et.setText(mContextText);
 			TextView tv = (TextView) dialog.findViewById(R.id.foldernametext);
 			if (mContextFile.isDirectory()) {
 				tv.setText(R.string.file_name);
 			} else {
 				tv.setText(R.string.file_name);
 			}
 			((AlertDialog) dialog).setIcon(mContextIcon);
 			break;
 
 		case DIALOG_MULTI_DELETE:
             break;
             
 		case DIALOG_DETAILS:
 			final TextView type = ((TextView)dialog.findViewById(R.id.details_type_value));
         	type.setText((mContextFile.isDirectory() ? R.string.details_type_folder :
         				(mContextFile.isFile() ? R.string.details_type_file :
         					R.string.details_type_other)));
         	
         	final TextView size = ((TextView)dialog.findViewById(R.id.details_size_value));
         	size.setText(FileUtils.formatSize(this, mContextFile.length()));
         	
         	String perms = (mContextFile.canRead() ? "R" : "-") +
         			(mContextFile.canWrite() ? "W" : "-") +
         			(mContextFile.canExecute() ? "X" : "-");
         	
         	final TextView permissions = ((TextView)dialog.findViewById(R.id.details_permissions_value));
         	permissions.setText(perms);
         	
         	final TextView hidden = ((TextView)dialog.findViewById(R.id.details_hidden_value));
         	hidden.setText(mContextFile.isHidden() ? R.string.details_yes : R.string.details_no);
         	
         	final TextView lastmodified = ((TextView)dialog.findViewById(R.id.details_lastmodified_value));
         	lastmodified.setText(FileUtils.formatDate(this, mContextFile.lastModified()));
         	((AlertDialog) dialog).setIcon(mContextIcon);
         	((AlertDialog) dialog).setTitle(mContextText);
 			break;
 
 		}
 	}
 	
 	/**
 	 * @since 2011-09-30
 	 */
 	private void showWarningDialog() {
 		LayoutInflater li = LayoutInflater.from(this);
 		View warningView = li.inflate(R.layout.dialog_warning, null);
 		final CheckBox showWarningAgain = (CheckBox)warningView.findViewById(R.id.showagaincheckbox);
 		
 		showWarningAgain.setChecked(PreferenceActivity.getShowAllWarning(FileManagerActivity.this));
 		
 		new AlertDialog.Builder(this).setView(warningView).setTitle(getString(R.string.title_warning_some_may_not_work))
 				.setMessage(getString(R.string.warning_some_may_not_work, mContextText))
 		    	.setIcon(android.R.drawable.ic_dialog_alert).setPositiveButton(
 					android.R.string.ok, new OnClickListener() {
 						
 						public void onClick(DialogInterface dialog, int which) {
 							PreferenceActivity.setShowAllWarning(FileManagerActivity.this, showWarningAgain.isChecked());
 
 							showMoreCommandsDialog();
 						}
 						
 					}).create()
 				.show();
 	}
 
 	/**
 	 * @since 2011-09-30
 	 */
 	private void showMoreCommandsDialog() {
 		final Uri data = Uri.fromFile(mContextFile);
 		final Intent intent = new Intent(null, data);
 		String type = mMimeTypes.getMimeType(mContextFile.getName());
 
 		intent.setDataAndType(data, type);
 
 		Log.v(TAG, "Data=" + data);
 		Log.v(TAG, "Type=" + type);
 
 		if (type != null) {
 			// Add additional options for the MIME type of the selected file.
 			PackageManager pm = getPackageManager();
 			final List<ResolveInfo> lri = pm.queryIntentActivityOptions(
 					new ComponentName(this, FileManagerActivity.class),
 					null, intent, 0);
 			final int N = lri != null ? lri.size() : 0;
 
 			// Create name list for menu item.
 			final List<CharSequence> items = new ArrayList<CharSequence>();
 			for (int i = 0; i < N; i++) {
 				final ResolveInfo ri = lri.get(i);
 				items.add(ri.loadLabel(pm));
 			}
 			
 			new AlertDialog.Builder(this)
 					.setTitle(mContextText)
 					.setIcon(mContextIcon)
 					.setItems(items.toArray(new CharSequence[0]),
 							new DialogInterface.OnClickListener() {
 								public void onClick(DialogInterface dialog, int item) {
 									final ResolveInfo ri = lri.get(item);
 									Intent rintent = new Intent(intent)
 											.setComponent(new ComponentName(
 													ri.activityInfo.applicationInfo.packageName,
 													ri.activityInfo.name));
 									startActivity(rintent);
 								}
 							}).create()
 						.show();
 		}
 	}
 
 	private void includeInMediaScan() {
 		// Delete the .nomedia file.
 		File file = FileUtils.getFile(currentDirectory, NOMEDIA_FILE);
 		if (file.delete()) {
 			Toast.makeText(this, getString(R.string.media_scan_included), Toast.LENGTH_LONG).show();
 			mNoMedia = false;
 		} else {
 			// That didn't work.
 			Toast.makeText(this, getString(R.string.error_generic), Toast.LENGTH_LONG).show();
 		}
 	}
 
 	private void excludeFromMediaScan() {
 		// Create the .nomedia file.
 		File file = FileUtils.getFile(currentDirectory, NOMEDIA_FILE);
 		try {
 			if (file.createNewFile()) {
 				mNoMedia = true;
 				Toast.makeText(this, getString(R.string.media_scan_excluded), Toast.LENGTH_LONG).show();
 			} else {
 				Toast.makeText(this, getString(R.string.error_media_scan), Toast.LENGTH_LONG).show();
 			}
 		} catch (IOException e) {
 			// That didn't work.
 			Toast.makeText(this, getString(R.string.error_generic) + e.getMessage(), Toast.LENGTH_LONG).show();
 		}
 	}
 	
 	private boolean checkSelection() {
         for (IconifiedText it : mDirectoryEntries) {
             if (!it.isSelected()) {
                 continue;
             }
 
             return true;
         }
 
         Toast.makeText(this, R.string.error_selection, Toast.LENGTH_SHORT).show();
 
         return false;
    }
 
    private void promptDestinationAndMoveFile() {
 
 		Intent intent = new Intent(FileManagerIntents.ACTION_PICK_DIRECTORY);
 		
 		intent.setData(FileUtils.getUri(currentDirectory));
 		
 		intent.putExtra(FileManagerIntents.EXTRA_TITLE, getString(R.string.move_title));
 		intent.putExtra(FileManagerIntents.EXTRA_BUTTON_TEXT, getString(R.string.move_button));
 		intent.putExtra(FileManagerIntents.EXTRA_WRITEABLE_ONLY, true);
 		
 		startActivityForResult(intent, REQUEST_CODE_MOVE);
 	}
 	
 	private void promptDestinationAndCopyFile() {
 
 		Intent intent = new Intent(FileManagerIntents.ACTION_PICK_DIRECTORY);
 		
 		intent.setData(FileUtils.getUri(currentDirectory));
 		
 		intent.putExtra(FileManagerIntents.EXTRA_TITLE, getString(R.string.copy_title));
 		intent.putExtra(FileManagerIntents.EXTRA_BUTTON_TEXT, getString(R.string.copy_button));
 		intent.putExtra(FileManagerIntents.EXTRA_WRITEABLE_ONLY, true);
 		
 		startActivityForResult(intent, REQUEST_CODE_COPY);
 	}
 	
 	/**
 	 * Starts activity for multi select.
 	 */
 	private void promptMultiSelect() {
         Intent intent = new Intent(FileManagerIntents.ACTION_MULTI_SELECT);
         
         intent.setData(FileUtils.getUri(currentDirectory));
         
         intent.putExtra(FileManagerIntents.EXTRA_TITLE, getString(R.string.multiselect_title));
         //intent.putExtra(FileManagerIntents.EXTRA_BUTTON_TEXT, getString(R.string.move_button));
 
         startActivityForResult(intent, REQUEST_CODE_MULTI_SELECT);
     }
 
     private void createNewFolder(String foldername) {
 		if (!TextUtils.isEmpty(foldername)) {
 			File file = FileUtils.getFile(currentDirectory, foldername);
 			if (file.mkdirs()) {
 				
 				// Change into new directory:
 				browseTo(file);
 			} else {
 				Toast.makeText(this, R.string.error_creating_new_folder, Toast.LENGTH_SHORT).show();
 			}
 		}
 	}
 
 	/*! Recursively delete a directory and all of its children.
 	 *  @params toastOnError If set to true, this function will toast if an error occurs.
 	 *  @returns true if successful, false otherwise.
 	 */
 	private boolean recursiveDelete(File file, boolean toastOnError) {
 		// Recursively delete all contents.
 		File[] files = file.listFiles();
 		
 		if (files == null) {
 			Toast.makeText(this, getString(R.string.error_deleting_folder, file.getAbsolutePath()), Toast.LENGTH_LONG);
 			return false;
 		}
 		
 		for (int x=0; x<files.length; x++) {
 			File childFile = files[x];
 			if (childFile.isDirectory()) {
 				if (!recursiveDelete(childFile, toastOnError)) {
 					return false;
 				}
 			} else {
 				if (!childFile.delete()) {
 					Toast.makeText(this, getString(R.string.error_deleting_child_file, childFile.getAbsolutePath()), Toast.LENGTH_LONG);
 					return false;
 				}
 			}
 		}
 		
 		if (!file.delete()) {
 			Toast.makeText(this, getString(R.string.error_deleting_folder, file.getAbsolutePath()), Toast.LENGTH_LONG);
 			return false;
 		}
 		
 		return true;
 	}
 	
 	private class RecursiveDeleteTask extends AsyncTask<Object, Void, Integer> {
 
 		private FileManagerActivity activity = FileManagerActivity.this;
 		private static final int success = 0;
 		private static final int err_deleting_folder = 1;
 		private static final int err_deleting_child_file = 2;
 		private static final int err_deleting_file = 3;
 
 		private File errorFile;
 
 		/**
 		 * Recursively delete a file or directory and all of its children.
 		 * 
 		 * @returns 0 if successful, error value otherwise.
 		 */
 		private int recursiveDelete(File file) {
 			if (file.isDirectory() && file.listFiles() != null)
 				for (File childFile : file.listFiles()) {
 					if (childFile.isDirectory()) {
 						int result = recursiveDelete(childFile);
 						if (result > 0) {
 							return result;
 						}
 					} else {
 						if (!childFile.delete()) {
 							errorFile = childFile;
 							return err_deleting_child_file;
 						}
 					}
 				}
 
 			if (!file.delete()) {
 				errorFile = file;
 				return file.isFile() ? err_deleting_file : err_deleting_folder;
 			}
 
 			return success;
 		}
 
 		@Override
 		protected void onPreExecute() {
 			Toast.makeText(activity, R.string.deleting_files, Toast.LENGTH_SHORT).show();
 		}
 		
 		@SuppressWarnings("unchecked")
 		@Override
 		protected Integer doInBackground(Object... params) {
 			Object files = params[0];
 			
 			if (files instanceof List<?>) {
 				for (File file: (List<File>)files) {
 					int result = recursiveDelete(file);
 					if (result != success) return result;
 				}
 				return success;
 			} else
 				return recursiveDelete((File)files);
 
 		}
 
 		@Override
 		protected void onPostExecute(Integer result) {
 			switch (result) {
 			case success:
 				activity.refreshList();
				if(deletedFileIsDirectory){
					Toast.makeText(activity, R.string.folder_deleted,Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(activity, R.string.file_deleted,Toast.LENGTH_SHORT).show();
				}
 				break;
 			case err_deleting_folder:
 				Toast.makeText(activity,getString(R.string.error_deleting_folder,
 						errorFile.getAbsolutePath()), Toast.LENGTH_LONG).show();
 				break;
 			case err_deleting_child_file:
 				Toast.makeText(activity,getString(R.string.error_deleting_child_file,
 						errorFile.getAbsolutePath()),Toast.LENGTH_SHORT).show();
 				break;
 			case err_deleting_file:
 				Toast.makeText(activity,getString(R.string.error_deleting_file,
 						errorFile.getAbsolutePath()), Toast.LENGTH_LONG).show();
 				break;
 			}
 		}
 
 	}
 
 	private void deleteFileOrFolder(File file) {
 		fileDeleted = true;
 		positionAtDelete = getListView().getFirstVisiblePosition();
		deletedFileIsDirectory = file.isDirectory();
 		new RecursiveDeleteTask().execute(file);
 //		if (file.isDirectory()) {
 //			if (recursiveDelete(file, true)) {
 //				refreshList();
 //				Toast.makeText(this, R.string.folder_deleted, Toast.LENGTH_SHORT).show();
 //			}
 //		} else {
 //			if (file.delete()) {
 //				// Delete was successful.
 //				refreshList();
 //				Toast.makeText(this, R.string.file_deleted, Toast.LENGTH_SHORT).show();
 //			} else {
 //				Toast.makeText(this, R.string.error_deleting_file, Toast.LENGTH_SHORT).show();
 //			}
 //		}
 	}
 	
     private void deleteMultiFile() {
 //        int toast = 0;
         LinkedList<File> files = new LinkedList<File>();
         for (IconifiedText it : mDirectoryEntries) {
             if (!it.isSelected()) {
                 continue;
             }
 
             File file = FileUtils.getFile(currentDirectory, it.getText());
             files.add(file);
 //            if (file.isDirectory()) {
 //                if (!recursiveDelete(file, true)) {
 //                    break;
 //                }
 //            } else {
 //                if (!file.delete()) {
 //                    toast = R.string.error_deleting_file;
 //                    break;
 //                }
 //            }
         }
 
         new RecursiveDeleteTask().execute(files);
         
 //        if (toast == 0) {
 //            // Delete was successful.
 //            refreshList();
 //            toast = R.string.file_deleted;
 //        }
 //
 //        Toast.makeText(FileManagerActivity.this, toast, Toast.LENGTH_SHORT).show();
     }
     
     private void renameFileOrFolder(File file, String newFileName) {
 		
 		if (newFileName != null && newFileName.length() > 0){
 			if (newFileName.lastIndexOf('.') < 0){				
 				newFileName += FileUtils.getExtension(file.getName()); 
 			}
 		}
 		File newFile = FileUtils.getFile(currentDirectory, newFileName);
 		
 		rename(file, newFile);
 	}
 
 	/**
 	 * @param oldFile
 	 * @param newFile
 	 */
 	private void rename(File oldFile, File newFile) {
 		int toast = 0;
 		if (oldFile.renameTo(newFile)) {
 			// Rename was successful.
 			refreshList();
 			if (newFile.isDirectory()) {
 				toast = R.string.folder_renamed;
 			} else {
 				toast = R.string.file_renamed;
 			}
 		} else {
 			if (newFile.isDirectory()) {
 				toast = R.string.error_renaming_folder;
 			} else {
 				toast = R.string.error_renaming_file;
 			}
 		}
 		Toast.makeText(this, toast, Toast.LENGTH_SHORT).show();
 	}
 
 	/*@ RETURNS: A file name that is guaranteed to not exist yet.
 	 * 
 	 * PARAMS:
 	 *   context - Application context.
 	 *   path - The path that the file is supposed to be in.
 	 *   fileName - Desired file name. This name will be modified to
 	 *     create a unique file if necessary.
 	 * 
 	 */
 	private File createUniqueCopyName(Context context, File path, String fileName) {
 		// Does that file exist?
 		File file = FileUtils.getFile(path, fileName);
 		
 		if (!file.exists()) {
 			// Nope - we can take that.
 			return file;
 		}
 		
 		// Split file's name and extension to fix internationalization issue #307
 		int fromIndex = fileName.lastIndexOf(FILE_EXTENSION_SEPARATOR);
 		String extension = "";
 		if (fromIndex > 0) {
 			extension = fileName.substring(fromIndex);
 			fileName = fileName.substring(0, fromIndex);
 		}
 		
 		// Try a simple "copy of".
 		file = FileUtils.getFile(path, context.getString(R.string.copied_file_name, fileName).concat(extension));
 		
 		if (!file.exists()) {
 			// Nope - we can take that.
 			return file;
 		}
 		
 		int copyIndex = 2;
 		
 		// Well, we gotta find a unique name at some point.
 		while (copyIndex < 500) {
 			file = FileUtils.getFile(path, context.getString(R.string.copied_file_name_2, copyIndex, fileName).concat(extension));
 			
 			if (!file.exists()) {
 				// Nope - we can take that.
 				return file;
 			}
 
 			copyIndex++;
 		}
 	
 		// I GIVE UP.
 		return null;
 	}
 	
 	private boolean copy(File oldFile, File newFile) {
 		try {
 			FileInputStream input = new FileInputStream(oldFile);
 			FileOutputStream output = new FileOutputStream(newFile);
 		
 			byte[] buffer = new byte[COPY_BUFFER_SIZE];
 			
 			while (true) {
 				int bytes = input.read(buffer);
 				
 				if (bytes <= 0) {
 					break;
 				}
 				
 				output.write(buffer, 0, bytes);
 			}
 			
 			output.close();
 			input.close();
 			
 		} catch (Exception e) {
 		    return false;
 		}
 		return true;
 	}
 	
 	private void sendFile(File file) {
 
 		String filename = file.getName();
 		String content = "hh";
 		
 		Log.i(TAG, "Title to send: " + filename);
 		Log.i(TAG, "Content to send: " + content);
 
 		Intent i = new Intent();
 		i.setAction(Intent.ACTION_SEND);
 		i.setType(mMimeTypes.getMimeType(file.getName()));
 		i.putExtra(Intent.EXTRA_SUBJECT, filename);
 		//i.putExtra(Intent.EXTRA_STREAM, FileUtils.getUri(file));
 		i.putExtra(Intent.EXTRA_STREAM, Uri.parse("content://" + FileManagerProvider.AUTHORITY + file.getAbsolutePath()));
 
 		i = Intent.createChooser(i, getString(R.string.menu_send));
 		
 		try {
 			startActivity(i);
 		} catch (ActivityNotFoundException e) {
 			Toast.makeText(this, R.string.send_not_available,
 					Toast.LENGTH_SHORT).show();
 			Log.e(TAG, "Email client not installed");
 		}
 	}
 
 	// This code seems to work for SDK 2.3 (target="9")
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		
 		if (keyCode == KeyEvent.KEYCODE_BACK) {
 			if (mStepsBack > 0) {
 				upOneLevel();
 				return true;
 			}
 		}
 		
 		return super.onKeyDown(keyCode, event);
 	}
 	
 	// For targetSdkVersion="5" or higher, one needs to use the following code instead of the one above:
 	// (See http://android-developers.blogspot.com/2009/12/back-and-other-hard-keys-three-stories.html )
 	
 	/*
 	//@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event)  {
 	    if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.ECLAIR
 	            && keyCode == KeyEvent.KEYCODE_BACK
 	            && event.getRepeatCount() == 0) {
 	        // Take care of calling this method on earlier versions of
 	        // the platform where it doesn't exist.
 	        onBackPressed();
 	    }
 
 	    return super.onKeyDown(keyCode, event);
 	}
 
 	//@Override
 	public void onBackPressed() {
 	    // This will be called either automatically for you on 2.0
 	    // or later, or by the code above on earlier versions of the
 	    // platform.
 		if (mStepsBack > 0) {
 			upOneLevel();
 		} else {
 			finish();
 		}
 	}
 	*/
 
     /**
      * This is called after the file manager finished.
      */
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 
 		switch (requestCode) {
 		case REQUEST_CODE_MOVE:
 			if (resultCode == RESULT_OK && data != null) {
 				// obtain the filename
 				File movefrom = mContextFile;
 				File moveto = FileUtils.getFile(data.getData());
 				if (moveto != null) {
 					if (mState != STATE_MULTI_SELECT) {
 					    // Move single file.
                         moveto = FileUtils.getFile(moveto, movefrom.getName());
 						int toast = 0;
 						if (movefrom.renameTo(moveto)) {
 							// Move was successful.
 						    refreshList();
 				            if (moveto.isDirectory()) {
 								toast = R.string.folder_moved;
 							} else {
 								toast = R.string.file_moved;
 							}
 						} else {
 							if (moveto.isDirectory()) {
 								toast = R.string.error_moving_folder;
 							} else {
 								toast = R.string.error_moving_file;
 							}
 						}
 						Toast.makeText(this, toast, Toast.LENGTH_SHORT).show();
 					} else {
     					// Move multi file.
                         int toast = 0;
                         for (IconifiedText it : mDirectoryEntries) {
                             if (!it.isSelected()) {
                                 continue;
                             }
 
                             movefrom = FileUtils.getFile(currentDirectory, it.getText());
 					        File newPath = FileUtils.getFile(moveto, movefrom.getName());
                             if (!movefrom.renameTo(newPath)) {
                                 refreshList();
                                 if (moveto.isDirectory()) {
                                     toast = R.string.error_moving_folder;
                                 } else {
                                     toast = R.string.error_moving_file;
                                 }
                                 break;
                             }
 					    }
 
                         if (toast == 0) {
                             // Move was successful.
                             refreshList();
                             toast = R.string.file_moved;
                         }
 
                         Toast.makeText(this, toast, Toast.LENGTH_SHORT).show();
 
                         Intent intent = getIntent();
                         setResult(RESULT_OK, intent);
                         finish();
                     }
 						
 				}				
 				
 			}
 			break;
 
 		case REQUEST_CODE_COPY:
 			if (resultCode == RESULT_OK && data != null) {
 				// obtain the filename
 				File copyfrom = mContextFile;
 				File copyto = FileUtils.getFile(data.getData());
 				if (copyto != null) {
                     if (mState != STATE_MULTI_SELECT) {
                         // Copy single file.
                         copyto = createUniqueCopyName(this, copyto, copyfrom.getName());
                         
                         if (copyto != null) {
                             int toast = 0;
                             if (copy(copyfrom, copyto)) {
                                 toast = R.string.file_copied;
                                 refreshList();
                             } else {
                                 toast = R.string.error_copying_file;
                             }
                             Toast.makeText(this, toast, Toast.LENGTH_SHORT).show();
                         }
                     } else {
                         // Copy multi file.
                         int toast = 0;
                         for (IconifiedText it : mDirectoryEntries) {
                             if (!it.isSelected()) {
                                 continue;
                             }
 
                             copyfrom = FileUtils.getFile(currentDirectory, it.getText());
                             File newPath = createUniqueCopyName(this, copyto, copyfrom.getName());
                             if (copyto != null) {
                                 if (!copy(copyfrom, newPath)) {
                                     toast = R.string.error_copying_file;
                                     break;
                                 }
                             }
                         }
 
                         if (toast == 0) {
                             // Copy was successful.
                             toast = R.string.file_copied;
                             refreshList();
                         }
 
                         Toast.makeText(this, toast, Toast.LENGTH_SHORT).show();
 
                         Intent intent = getIntent();
                         setResult(RESULT_OK, intent);
                         finish();
                     }
 				}				
 			}
 			break;
 
         case REQUEST_CODE_MULTI_SELECT:
             if (resultCode == RESULT_OK && data != null) {
                 refreshList();
             }
             break;
         }
 		
 	}
 	
 	public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
 	    if (//When the user chooses to show/hide hidden files, update the list
     		//to correspond with the user's choice
     		PreferenceActivity.PREFS_DISPLAYHIDDENFILES.equals(key)
     		//When the user changes the sortBy settings, update the list
     		|| PreferenceActivity.PREFS_SORTBY.equals(key)
     		|| PreferenceActivity.PREFS_ASCENDING.equals(key)){
 	    	
         	refreshList();
 	    }
 	}
 
 
 }
