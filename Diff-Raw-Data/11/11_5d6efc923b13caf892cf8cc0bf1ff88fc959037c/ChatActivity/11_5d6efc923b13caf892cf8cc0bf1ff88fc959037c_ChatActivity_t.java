 package net.meneame.fisgodroid;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import net.meneame.fisgodroid.SmileyPickerView.OnSmileySelectedListener;
 import net.meneame.fisgodroid.adapters.BubblesChatAdapter;
 import net.meneame.fisgodroid.adapters.ChatMessageAdapter;
 import net.meneame.fisgodroid.adapters.LegacyChatAdapter;
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.content.SharedPreferences;
 import android.content.pm.PackageManager;
 import android.content.pm.ResolveInfo;
 import android.content.res.Configuration;
 import android.content.res.Resources;
 import android.content.res.TypedArray;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Color;
 import android.graphics.drawable.Drawable;
 import android.net.Uri;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.os.Parcelable;
 import android.preference.PreferenceManager;
 import android.provider.MediaStore;
 import android.support.v4.view.MotionEventCompat;
 import android.support.v7.app.ActionBar;
 import android.support.v7.app.ActionBarActivity;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.util.Log;
import android.util.TypedValue;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnFocusChangeListener;
 import android.view.View.OnTouchListener;
 import android.view.inputmethod.EditorInfo;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 
 import com.google.analytics.tracking.android.EasyTracker;
 
 public class ChatActivity extends ActionBarActivity
 {
     // Request codes for activities
     private static final int REQUEST_PICTURE = 1;
 
     // Shared preferences settings.
     private static final String PREFS_NAME = "ChatActivity";
     private static final String PREF_SENDAS = "send as";
 
     private ThreeStateChecboxHackView mCheckboxFriends;
     private ListView mMessages;
     private EditText mMessagebox;
     private ImageButton mSendButton;
     private ImageButton mSmileyButton;
     private SmileyPickerView mSmileyPicker;
     private ChatType mType = ChatType.PUBLIC;
     private ChatType mSendAs = ChatType.PUBLIC;
     private ChatMessageAdapter mAdapter;
     private Date mLastMessage = null;
     private File mCameraTempFile = null;
     private MenuItem mCameraMenuItem;
     private ProgressBar mCameraProgress;
     private NotificationsIndicatorDrawable mNotificationsDrawable;
     private View mActionBarDisplayer;
 
     // Create a handler to update the view from the UI thread
     // when the message list changes.
     private Handler mHandler = new Handler()
     {
         public void handleMessage(Message msg)
         {
             updateMessages(mFisgoBinder.getMessages());
         }
     };
 
     // Reference to the service binder
     private FisgoService.FisgoBinder mFisgoBinder = null;
     private ServiceConnection mServiceConn = new ServiceConnection()
     {
         @Override
         public void onServiceConnected(ComponentName arg0, IBinder binder)
         {
             mFisgoBinder = (FisgoService.FisgoBinder) binder;
 
             // If the service is not logged in, we should go back to the login
             // screen.
             if ( mFisgoBinder.isLoggedIn() == false )
             {
                 finish();
                 startActivity(new Intent(ChatActivity.this, LoginActivity.class));
             }
             else
             {
                 mFisgoBinder.setType(mType);
                 mFisgoBinder.addHandler(mHandler);
                 mFisgoBinder.setOnForeground(true);
                 if ( mCheckboxFriends != null )
                 {
                     Drawable whipDrawable = getResources().getDrawable(R.drawable.ic_whip);
                     mCheckboxFriends.setThirdStateDrawable(mFisgoBinder.isAdmin() ? whipDrawable : null);
                 }
                 updateMessages(mFisgoBinder.getMessages());
             }
         }
 
         @Override
         public void onServiceDisconnected(ComponentName arg0)
         {
         }
     };
 
     @Override
     protected void onResume()
     {
         super.onResume();
 
         // Avoid the edit field auto-gaining focus
         final View mainLayout = findViewById(R.id.main_layout);
         mainLayout.requestFocus();
 
         // Setup the message adapter
         SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
         boolean useBubbles = prefs.getBoolean("chat_enable_bubbles", true);
         if ( !useBubbles )
         {
             mAdapter = new LegacyChatAdapter(this);
         }
         else
         {
             mAdapter = new BubblesChatAdapter(ChatActivity.this);
             boolean join = prefs.getBoolean("chat_join_bubbles", true);
             ((BubblesChatAdapter) mAdapter).setJoinBubbles(join);
         }
         mMessages.setAdapter(mAdapter);
         if (mFisgoBinder != null) {
             updateMessages(mFisgoBinder.getMessages());
         }
        
        setActionBarVisible(true);
     }
 
     @Override
     protected void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_chat);
 
         // Setup the action bar
         ActionBar actionBar = getSupportActionBar();
         actionBar.setTitle(R.string.general);
         actionBar.setDisplayHomeAsUpEnabled(false);
 
         // Set the icon drawable to reuse it for notifications
         final Drawable defaultDrawable = getResources().getDrawable(R.drawable.ic_launcher);
         final int backgroundColor = getResources().getColor(R.color.meneame_light);
         mNotificationsDrawable = new NotificationsIndicatorDrawable(Color.RED, backgroundColor, Color.WHITE, defaultDrawable);
         actionBar.setIcon(mNotificationsDrawable);
 
         // Display the title only if we are in landscape mode
         if ( getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT )
         {
             actionBar.setDisplayShowTitleEnabled(false);
         }
 
         // Get views
         mCheckboxFriends = (ThreeStateChecboxHackView) findViewById(R.id.checkbox_friends);
         mMessages = (ListView) findViewById(R.id.chat_messages);
         mMessagebox = (EditText) findViewById(R.id.chat_messagebox);
         mSendButton = (ImageButton) findViewById(R.id.button_send);
         mSmileyButton = (ImageButton) findViewById(R.id.smileys_button);
         mSmileyPicker = (SmileyPickerView) findViewById(R.id.smiley_picker);
         mCameraProgress = (ProgressBar) findViewById(R.id.camera_progress);
 
         // Restore stuff from shared prefs
         SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
         int optionOrdinal = prefs.getInt(PREF_SENDAS, ChatType.PUBLIC.ordinal());
         if ( optionOrdinal >= 0 && optionOrdinal < ChatType.values().length )
             mSendAs = ChatType.values()[optionOrdinal];
 
         // Setup
         Drawable whipDrawable = getResources().getDrawable(R.drawable.ic_whip);
         mCheckboxFriends.setThirdStateDrawable((mFisgoBinder != null && mFisgoBinder.isAdmin()) ? whipDrawable : null);
         setType(mType);
         setSendAs(mSendAs);
 
         // Setup the smiley picker
         mSmileyButton.setOnClickListener(new OnClickListener()
         {
             @Override
             public void onClick(View v)
             {
                 if ( mSmileyPicker.getVisibility() == View.VISIBLE )
                     mSmileyPicker.setVisibility(View.GONE);
                 else
                     mSmileyPicker.setVisibility(View.VISIBLE);
             }
         });
         mSmileyPicker.setVisibility(View.GONE);
         mSmileyPicker.setOnSmileySelectedListener(new OnSmileySelectedListener()
         {
             @Override
             public void onSmileySelected(Smiley smiley)
             {
                 mSmileyPicker.setVisibility(View.GONE);
                 String smileyText = smiley.getInputText() + " ";
                 int start = Math.max(mMessagebox.getSelectionStart(), 0);
                 int end = Math.max(mMessagebox.getSelectionEnd(), 0);
                 mMessagebox.getText().replace(Math.min(start, end), Math.max(start, end), smileyText, 0, smileyText.length());
                 mMessagebox.requestFocus();
             }
         });
 
         // Handle key presses for the nick completion feature
         mMessagebox.addTextChangedListener(new TextWatcher()
         {
             @Override
             public void onTextChanged(CharSequence s, int start, int before, int count)
             {
                 String str = s.toString();
                 if ( count == 1 && str.charAt(start) == '\t' )
                 {
                     // Remove the tab from the string
                     str = str.substring(0, start) + str.substring(start + 1);
 
                     doNickCompletion(str, start);
                 }
             }
 
             @Override
             public void beforeTextChanged(CharSequence s, int start, int count, int after)
             {
             }
 
             @Override
             public void afterTextChanged(Editable s)
             {
             }
         });
 
         // Close the keyboard when clicking outside the edit box
         mMessagebox.setOnFocusChangeListener(new OnFocusChangeListener()
         {
             @Override
             public void onFocusChange(View v, boolean hasFocus)
             {
                 if ( !hasFocus )
                 {
                     InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                     imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                 }
             }
         });
 
         // Connect with the chat service
         Intent intent = new Intent(this, FisgoService.class);
         startService(intent);
         bindService(intent, mServiceConn, BIND_AUTO_CREATE);
 
         // Make pressing enter in the message box send the chat
         mMessagebox.setOnEditorActionListener(new TextView.OnEditorActionListener()
         {
             @Override
             public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent)
             {
                 if ( id == EditorInfo.IME_NULL )
                 {
                     if ( keyEvent.getAction() == KeyEvent.ACTION_UP )
                         sendChat();
                     return true;
                 }
 
                 return false;
             }
         });
 
         // Also send messages with the send button
         mSendButton.setOnClickListener(new OnClickListener()
         {
             @Override
             public void onClick(View v)
             {
                 sendChat();
             }
         });
 
         // Display the action bar again when they click the shower button
         mActionBarDisplayer = findViewById(R.id.action_bar_displayer);
         mActionBarDisplayer.setOnClickListener(new OnClickListener()
         {
             @Override
             public void onClick(View v)
             {
                 setActionBarVisible(true);
             }
         });
         // Also allow to display it by dragging
         mActionBarDisplayer.setOnTouchListener(new OnTouchListener()
         {
             private float mInitialY = -1.0f;
 
             @Override
             public boolean onTouch(View v, MotionEvent event)
             {
                 final int pointerIndex = MotionEventCompat.getActionIndex(event);
                 switch (event.getAction())
                 {
                 case MotionEvent.ACTION_DOWN:
                     mInitialY = MotionEventCompat.getY(event, pointerIndex);
                     break;
 
                 case MotionEvent.ACTION_MOVE:
                     if ( mInitialY != -1.0f )
                     {
                         final float finalY = MotionEventCompat.getY(event, pointerIndex);
                         if ( (finalY - mInitialY) > 2.0f )
 
                         {
                             mInitialY = -1.0f;
                             setActionBarVisible(true);
                         }
                     }
                     break;
                 }
                 return false;
             }
         });
         setActionBarVisible(true);
 
         // Update the listener in the image upload singleton to restore ongoing
         // uploads
         // after device rotation refresh.
         ImageUpload.updateListener(mImageUploadListener);
     }
 
     @Override
     protected void onDestroy()
     {
         // Save the shared prefs
         getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().putInt(PREF_SENDAS, getSendAs().ordinal()).commit();
 
         super.onDestroy();
         if ( mFisgoBinder != null )
         {
             mFisgoBinder.removeHandler(mHandler);
         }
         unbindService(mServiceConn);
 
         ImageUpload.updateListener(null);
     }
 
     @Override
     protected void onStart()
     {
         super.onStart();
 
         EasyTracker.getInstance(this).activityStart(this);
 
         Notifications.setOnForeground(getApplicationContext(), true);
         if ( mFisgoBinder != null )
         {
             mFisgoBinder.setOnForeground(true);
         }
     }
 
     @Override
     protected void onStop()
     {
         super.onStop();
 
         EasyTracker.getInstance(this).activityStop(this);
 
         Notifications.setOnForeground(getApplicationContext(), false);
         if ( mFisgoBinder != null )
         {
             mFisgoBinder.setOnForeground(false);
         }
     }
 
     @Override
     public boolean onKeyDown(int keyCode, KeyEvent event)
     {
         if ( keyCode == KeyEvent.KEYCODE_BACK )
         {
             // If they want to go back while having the smileys window open,
             // simply close this window.
             if ( mSmileyPicker.getVisibility() != View.GONE )
             {
                 mSmileyPicker.setVisibility(View.GONE);
             }
             else
             {
                 moveTaskToBack(true);
             }
             return true;
         }
         return super.onKeyDown(keyCode, event);
     }
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data)
     {
         if ( resultCode != Activity.RESULT_OK )
             return;
 
         if ( requestCode == REQUEST_PICTURE )
         {
             Bitmap bitmap = null;
             if ( data != null )
             {
                 final String action = data.getAction();
                 if ( action != null && action.equals("inline-data") )
                 {
                     bitmap = (Bitmap) data.getExtras().get("data");
                 }
                 else
                 {
                     try
                     {
                         bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData());
                     }
                     catch (IOException e)
                     {
                         e.printStackTrace();
                     }
                 }
             }
             else if ( mCameraTempFile != null )
             {
                 try
                 {
                     bitmap = BitmapFactory.decodeStream(new FileInputStream(mCameraTempFile));
                     mCameraTempFile.delete();
                     mCameraTempFile = null;
                 }
                 catch (FileNotFoundException e)
                 {
                     e.printStackTrace();
                 }
             }
 
             if ( bitmap != null )
             {
                 ImageUpload.upload(mFisgoBinder, bitmap, mImageUploadListener);
             }
         }
     }
 
     /**
      * Backward-compatible version of {@link ActionBar#getThemedContext()} that
      * simply returns the {@link android.app.Activity} if
      * <code>getThemedContext</code> is unavailable.
      */
     @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
     private Context getActionBarThemedContextCompat()
     {
         if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH )
         {
             return getActionBar().getThemedContext();
         }
         else
         {
             return this;
         }
     }
 
     @Override
     public void onRestoreInstanceState(Bundle savedInstanceState)
     {
         if ( savedInstanceState.containsKey("send as") )
         {
             mSendAs = (ChatType) savedInstanceState.getSerializable("send as");
             setSendAs(mSendAs);
         }
         if ( savedInstanceState.containsKey("type") )
         {
             mType = (ChatType) savedInstanceState.getSerializable("type");
             setType(mType);
         }
         if ( savedInstanceState.containsKey("messagebox") )
         {
             mMessagebox.setText(savedInstanceState.getString("messagebox"));
             if ( savedInstanceState.containsKey("selectionStart") && savedInstanceState.containsKey("selectionEnd") )
             {
                 mMessagebox.setSelection(savedInstanceState.getInt("selectionStart"), savedInstanceState.getInt("selectionEnd"));
             }
         }
     }
 
     @Override
     public void onSaveInstanceState(Bundle outState)
     {
         // Serialize the current dropdown position.
         outState.putSerializable("type", mType);
         outState.putSerializable("send as", getSendAs());
         outState.putString("messagebox", mMessagebox.getText().toString());
         outState.putInt("selectionStart", mMessagebox.getSelectionStart());
         outState.putInt("selectionEnd", mMessagebox.getSelectionEnd());
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu)
     {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.chat, menu);
         mCameraMenuItem = menu.findItem(R.id.action_take_picture);
 
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item)
     {
         switch (item.getItemId())
         {
         case android.R.id.home:
             Log.v("A", "Display notifications");
             return true;
 
         case R.id.action_hide_action_bar:
             setActionBarVisible(false);
             return true;
 
         case R.id.action_take_picture:
             takePicture();
             return true;
 
         case R.id.action_settings:
             startActivity(new Intent(this, SettingsActivity.class));
             return true;
 
         case R.id.action_logout:
             mFisgoBinder.logOut();
             stopService(new Intent(this, FisgoService.class));
             finish();
             startActivity(new Intent(this, LoginActivity.class));
             return true;
 
         case R.id.action_savelog:
             LogSaver saver = new LogSaver(getApplicationContext(), mFisgoBinder.getMessages());
             saver.save();
             return true;
 
         case R.id.action_only_friends:
             boolean checked = item.isChecked();
             item.setChecked(!checked);
             setType(!checked ? ChatType.FRIENDS : ChatType.PUBLIC);
             return true;
         }
 
         return super.onOptionsItemSelected(item);
     }
 
     private void sendChat()
     {
         String text = mMessagebox.getText().toString();
         if ( text.length() > 0 )
         {
             Date now = new Date();
             Resources res = getResources();
             int delayBetweenMessages = res.getInteger(R.integer.time_between_messages);
 
             // Check for too small messages
             if ( text.length() < res.getInteger(R.integer.min_message_length) )
             {
                 AlertDialog.Builder builder = new AlertDialog.Builder(this);
                 builder.setTitle(R.string.error).setMessage(R.string.message_too_short).setIcon(android.R.drawable.ic_dialog_alert).setNeutralButton(android.R.string.ok, null).create().show();
             }
             else if ( mLastMessage != null && (now.getTime() - mLastMessage.getTime()) < (delayBetweenMessages * 1000) )
             {
                 String errMsg = String.format(res.getString(R.string.message_too_soon), delayBetweenMessages);
                 AlertDialog.Builder builder = new AlertDialog.Builder(this);
                 builder.setTitle(R.string.error).setMessage(errMsg).setIcon(android.R.drawable.ic_dialog_alert).setNeutralButton(android.R.string.ok, null).create().show();
             }
             else
             {
                 // Send with the appropiate chat type prefix
                 ChatType target;
                 if ( mType == ChatType.PUBLIC )
                     target = getSendAs();
                 else
                     target = mType;
 
                 if ( target == ChatType.FRIENDS )
                     text = "@" + text;
                 else if ( target == ChatType.ADMIN )
                     text = "#" + text;
 
                 mFisgoBinder.sendChat(text);
                 mMessagebox.setText("");
                 mLastMessage = now;
             }
         }
     }
 
     public ChatType getSendAs()
     {
         switch (mCheckboxFriends.getState())
         {
         case UNCHECKED:
             mSendAs = ChatType.PUBLIC;
             break;
         case CHECKED:
             mSendAs = ChatType.FRIENDS;
             break;
         case THIRD_STATE:
             mSendAs = ChatType.ADMIN;
             break;
         }
         return mSendAs;
     }
 
     public void setSendAs(ChatType type)
     {
         mSendAs = type;
         if ( mCheckboxFriends != null )
         {
             switch (type)
             {
             case PUBLIC:
                 mCheckboxFriends.setState(ThreeStateChecboxHackView.State.UNCHECKED);
                 break;
             case FRIENDS:
                 mCheckboxFriends.setState(ThreeStateChecboxHackView.State.CHECKED);
                 break;
             case ADMIN:
                 mCheckboxFriends.setState(ThreeStateChecboxHackView.State.THIRD_STATE);
                 break;
             }
         }
     }
 
     public void setType(ChatType type)
     {
         mType = type;
         if ( mCheckboxFriends != null )
             mCheckboxFriends.setVisibility(type == ChatType.PUBLIC ? View.VISIBLE : View.GONE);
 
         if ( mFisgoBinder != null )
             mFisgoBinder.setType(mType);
 
         // Update the action bar title
         ActionBar actionBar = getSupportActionBar();
         switch (mType)
         {
         case PUBLIC:
             actionBar.setTitle(R.string.general);
             break;
         case FRIENDS:
             actionBar.setTitle(R.string.friends);
             break;
         case ADMIN:
             actionBar.setTitle(R.string.admin);
             break;
 
         default:
             break;
         }
     }
 
     public void updateMessages(List<ChatMessage> messages)
     {
         if ( mAdapter != null )
         {
             mAdapter.setUsername(mFisgoBinder.getUsername());
             mAdapter.setIsAdmin(mFisgoBinder.isAdmin());
             mAdapter.setMessages(messages);
         }
     }
 
     private void doNickCompletion(String str, int start)
     {
         // We will need to restore the cursor position after replacements
         int cursorPos = start;
 
         if ( str.length() > 0 )
         {
             int wordBegin = str.lastIndexOf(' ', Math.max(0, start - 1));
             wordBegin = Math.max(0, wordBegin);
             if ( str.charAt(wordBegin) == ' ' )
                 ++wordBegin;
 
             int wordEnd = str.indexOf(' ', wordBegin);
             if ( wordEnd == -1 )
                 wordEnd = str.length();
 
             // Get the partial name from the detected word
             String partialName = str.substring(wordBegin, wordEnd);
 
             // Perform nick completion if we at least got two characters of the
             // name
             if ( (wordEnd - wordBegin) >= 2 )
             {
                 // Search a matching nickname in the last 15 minutes messages,
                 // using
                 // at least 10 messages.
                 Date now = new Date();
                 long timeThreshold = now.getTime() - 15 * 60 * 1000;
 
                 String nameReplacement = null;
                 partialName = partialName.toLowerCase();
                 int messageCount = 0;
                 for (ChatMessage msg : mFisgoBinder.getMessages())
                 {
                     ++messageCount;
                     if ( messageCount > 10 && msg.getWhen().getTime() < timeThreshold )
                         break;
 
                     if ( msg.getUser().toLowerCase().startsWith(partialName) )
                     {
                         nameReplacement = msg.getUser();
                         break;
                     }
                 }
 
                 // If we didn't find anything in the last messages, search in
                 // the
                 // friend names.
                 if ( nameReplacement == null )
                 {
                     for (String friendName : mFisgoBinder.getFriendNames())
                     {
                         if ( friendName.toLowerCase().startsWith(partialName) )
                         {
                             nameReplacement = friendName;
                             break;
                         }
                     }
                 }
 
                 // Replace it!
                 if ( nameReplacement != null )
                 {
                     str = str.substring(0, wordBegin) + nameReplacement + str.substring(wordEnd);
                     cursorPos = wordBegin + nameReplacement.length();
 
                     // If we were at the end of the string, add an extra space.
                     if ( cursorPos == str.length() )
                     {
                         str = str + " ";
                         cursorPos++;
                     }
                 }
             }
         }
 
         mMessagebox.setText(str);
         mMessagebox.setSelection(cursorPos);
     }
 
     private void takePicture()
     {
         // Create a temporary file for in case they decide to use the camera
         File cacheDir = getExternalCacheDir();
         mCameraTempFile = null;
         try
         {
             mCameraTempFile = File.createTempFile("fisgodroid", "capture.jpg", cacheDir);
         }
         catch (IOException e)
         {
             e.printStackTrace();
         }
 
         // http://stackoverflow.com/questions/4455558/allow-user-to-select-camera-or-gallery-for-image
         // Camera.
         final List<Intent> cameraIntents = new ArrayList<Intent>();
         final Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
         if ( mCameraTempFile != null )
             captureIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, Uri.fromFile(mCameraTempFile));
 
         final PackageManager packageManager = getPackageManager();
         final List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
         for (ResolveInfo res : listCam)
         {
             final String packageName = res.activityInfo.packageName;
             final Intent intent = new Intent(captureIntent);
             intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
             intent.setPackage(packageName);
             cameraIntents.add(intent);
         }
 
         // Filesystem.
         final Intent galleryIntent = new Intent();
         galleryIntent.setType("image/*");
         galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
 
         // Chooser of filesystem options.
         final Intent chooserIntent = Intent.createChooser(galleryIntent, "Select Source");
 
         // Add the camera options.
         chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(new Parcelable[] {}));
 
         startActivityForResult(chooserIntent, REQUEST_PICTURE);
     }
 
     private void setActionBarVisible(boolean visible)
     {
         ActionBar actionBar = getSupportActionBar();
         if ( !visible )
         {
             actionBar.hide();
         }
         else
         {
             actionBar.show();
         }
         adjustActionBarPadding(visible);
     }
 
     private void adjustActionBarPadding(boolean visible)
     {
         if ( !visible )
         {
             mMessages.setPadding(mMessages.getPaddingLeft(), 0, mMessages.getPaddingRight(), mMessages.getPaddingBottom());
         }
         else
         {
             final TypedArray styledAttributes = getTheme().obtainStyledAttributes(new int[] { R.attr.actionBarSize });
             int actionBarSize = (int) styledAttributes.getDimension(0, 0);
             styledAttributes.recycle();
            int displayerHeight = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getResources().getDisplayMetrics());
            int paddingTop = actionBarSize - displayerHeight;
             mMessages.setPadding(mMessages.getPaddingLeft(), paddingTop, mMessages.getPaddingRight(), mMessages.getPaddingBottom());
         }
     }
 
     private void setNotificationCount(int count)
     {
        if ( count > 0 && count > mNotificationsDrawable.getNotificationCount() )
         {
             setActionBarVisible(true);
         }
         mNotificationsDrawable.setNotificationCount(count);
     }
 
     private ImageUpload.Listener mImageUploadListener = new ImageUpload.Listener()
     {
         @Override
         public void onProgressUpdate(float progress)
         {
             mCameraProgress.setIndeterminate(false);
             mCameraProgress.setMax(100);
             mCameraProgress.setProgress((int) (100 * progress));
         }
 
         @Override
         public void onFinished(String url)
         {
             // Restore the camera button
             mCameraProgress.setProgress(0);
             mCameraProgress.setVisibility(View.GONE);
             mCameraMenuItem.setEnabled(true);
 
             // Did everything go ok?
             if ( url != null )
             {
                 mMessagebox.getText().append(" " + url + " ");
             }
         }
 
         @Override
         public void onStart()
         {
             // Hide the camera button and display a progress bar
             mCameraMenuItem.setEnabled(false);
             mCameraProgress.setProgress(0);
             mCameraProgress.setIndeterminate(true);
             mCameraProgress.setVisibility(View.VISIBLE);
         }
     };
 }
