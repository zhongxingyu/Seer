 /*
  * Copyright (C) 2010 Wysie Soh
  * 
  * NubDial is free software. It is based upon Lawrence's Greenfield's SpellDial
  * and as such, is under the terms of the GNU General Public License as published
  * by the Free Software Foundation, either version 2 of the License, or (at your option)
  * any later version.
  * 
  * Copyright (C) 2010 Lawrence Greenfield
  * 
  *  SpellDial is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 2 of the License, or
  *  (at your option) any later version.
  *
  *  SpellDial is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with SpellDial.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 // Features:
 // - figure out when to clear the partially completed number
 // - audible touch tones
 //
 // Display:
 // - make landspace mode pretty?
 // - make icon pretty
 // - add number so far to the ListView?
 // - show pictures?
 //
 // Bugs:
 // - occasional crash in landspace mode when returning from a call
 // - figure out how to deal with accents
 //
 // Performance if we want the types of phones avail:
 // - suck the whole thing into memory? would suck for lots of contacts...
 // - do a join and have a non 1:1 mapping from results to rows?
 
 package com.wysie.wydialer;
 
 import java.util.TimerTask;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import android.app.Activity;
 import android.content.ContentResolver;
 import android.content.ContentUris;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.res.ColorStateList;
 import android.content.res.Resources;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Color;
 import android.graphics.drawable.Drawable;
 import android.media.AudioManager;
 import android.media.ToneGenerator;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Vibrator;
 import android.preference.PreferenceManager;
 import android.provider.ContactsContract.Data;
 import android.provider.ContactsContract.CommonDataKinds.Phone;
 import android.provider.ContactsContract.CommonDataKinds.Photo;
 import android.telephony.PhoneNumberFormattingTextWatcher;
 import android.telephony.TelephonyManager;
 import android.text.Spannable;
 import android.text.TextUtils;
 import android.text.method.DialerKeyListener;
 import android.text.style.BackgroundColorSpan;
 import android.text.style.ForegroundColorSpan;
 import android.text.style.StyleSpan;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.GestureDetector;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.SoundEffectConstants;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.GestureDetector.SimpleOnGestureListener;
 import android.view.View.OnClickListener;
 import android.view.View.OnLongClickListener;
 import android.view.View.OnTouchListener;
 import android.widget.AbsListView;
 import android.widget.AdapterView;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.QuickContactBadge;
 import android.widget.ResourceCursorAdapter;
 import android.widget.TextView;
 import android.widget.AbsListView.OnScrollListener;
 import android.widget.AdapterView.OnItemClickListener;
 
 public class PhoneSpellDialer extends Activity implements OnScrollListener,
 		OnClickListener, OnLongClickListener, /* OnCreateContextMenuListener, */
 		OnItemClickListener
 {
   private static final String TAG = "SpellDial";
 
   // Identifiers for our menu items.
   private static final int ADD_TO_CONTACTS = 0;
   private static final int CALL_LOG = 1;
   private static final int CONTACTS = 2;
   private static final int SETTINGS_ID = 4;
 
   private ToneGenerator mToneGenerator;
   private Object mToneGeneratorLock = new Object();
   private static final int TONE_LENGTH_MS = 150;
   private static final int TONE_RELATIVE_VOLUME = 80;
   private static final int DIAL_TONE_STREAM_TYPE = AudioManager.STREAM_MUSIC;
 
   // Delay for contacts list recalculation. 
   private static final int RECALC_DELAY = 200;
 
   private static boolean hideDialpadOnScroll, showContactPictures,
     matchAnywhere, mDTMFToneEnabled, matchedItalics, matchedBold,
     matchedDigits, matchedHighlight, noMatches = false;
   private static Vibrator mVibrator;
   private static boolean prefVibrateOn;
   private static int pref_vibrate_time;
   private static boolean pref_click_sound;
 	
   private static boolean dialpad_visible = true;
 
   private static final StyleSpan ITALIC_STYLE
     = new StyleSpan(android.graphics.Typeface.ITALIC);
   private static final StyleSpan BOLD_STYLE
     = new StyleSpan(android.graphics.Typeface.BOLD);
   private static BackgroundColorSpan matchedHighlightColor;
   private static ForegroundColorSpan matchedDigitsColor;
 
   private static View top_view; 
   private Drawable mDigitsBackground;
   private Drawable mDigitsEmptyBackground;
   private EditText digitsView;
   private ImageButton dialButton, deleteButton;
   private ContactAccessor contactAccessor;
   private MenuItem mAddToContacts;
 
   private StringBuilder curFilter;
   private StringBuilder num_pat;
   private StringBuilder name_pat;
   private ContactListAdapter myAdapter;
   private ListView myContactList;
   private static String [] find_patterns;
   private Pattern name_pattern = Pattern.compile("");
   private Pattern number_pattern = Pattern.compile("");
 
   private Handler mHandler = new Handler();
   private UpdateTimerTask mUpdateTimeTask = new UpdateTimerTask();
 
   /** Called when the activity is first created. */
   @Override
   public void onCreate(Bundle savedInstanceState)
   {
     super.onCreate(savedInstanceState);
 
     PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
 
     setContentView(R.layout.main);
 
     contactAccessor = new ContactAccessor (getContentResolver());
     // Cursor cur = contactAccessor.recalculate("", matchAnywhere);
     // startManagingCursor(cur);
     myAdapter = new ContactListAdapter(this, null,
 				       contactAccessor.getContactSplit());
     curFilter = new StringBuilder();
     num_pat = new StringBuilder();
     name_pat = new StringBuilder();
 		
 
     // scott
 
     new SearchContactsTask().execute("");
 
     Resources r = getResources();
     mDigitsBackground = r.getDrawable(R.drawable.btn_digits_activated);
     mDigitsEmptyBackground = r.getDrawable(R.drawable.btn_digits);
     dialButton = (ImageButton) findViewById(R.id.dialButton);
     deleteButton = (ImageButton) findViewById(R.id.deleteButton);
     digitsView = (EditText) findViewById(R.id.digitsText);
     myContactList = (ListView) findViewById(R.id.contactlist);
     myContactList.setOnCreateContextMenuListener(this);
     myContactList.setAdapter(myAdapter);
     top_view = findViewById(R.id.toplevel);
     setHandlers();
     setPreferences();
 
     mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
 
     PhoneSpellDialer.find_patterns = r.getStringArray(R.array.numpad_patterns);
   }
 
   private void setPreferences()
   {
     int i;
     
     SharedPreferences prefs 
       = PreferenceManager.getDefaultSharedPreferences(this);
     mDTMFToneEnabled = prefs.getBoolean("dial_enable_dial_tone", false);
     prefVibrateOn = prefs.getBoolean("dial_enable_haptic", false);
 
     matchedItalics = prefs.getBoolean("matched_italics", false);
     matchedBold = prefs.getBoolean("matched_bold", true);
     matchedDigits = prefs.getBoolean("matched_colour", false);
     i = Integer.parseInt(prefs.getString("matched_colour_choice",
 					 "-16777216"));
     matchedDigitsColor = new ForegroundColorSpan(i);
 						 
     matchedHighlight = prefs.getBoolean("matched_highlight", true);
     i = Integer.parseInt(prefs.getString("matched_highlight_choice",
 					 "-3355444"));
     matchedHighlightColor = new BackgroundColorSpan(i);
     matchAnywhere = prefs.getBoolean("match_num_sequence", true);
     showContactPictures = prefs.getBoolean("show_contact_pictures", true);
     hideDialpadOnScroll = prefs.getBoolean("auto_hide_dialpad_on_fling",
 					   true);
     setDigitsColor(prefs);
     pref_vibrate_time = Integer.parseInt(prefs.getString("vibration_time",
 							 "30"));
     pref_click_sound = prefs.getBoolean("click_sound", true);
 
     ImageButton digitOne = (ImageButton) findViewById(R.id.button1);
     if (hasVoicemail())
       digitOne.setImageResource(R.drawable.dial_num_1_with_vm);
     else
       digitOne.setImageResource(R.drawable.dial_num_1_no_vm);
   }
 
   @Override
   public boolean onCreateOptionsMenu(Menu menu)
   {
     super.onCreateOptionsMenu(menu);
     mAddToContacts
       = (menu.add(0, ADD_TO_CONTACTS, 0, R.string.menu_new_contacts)
 	 .setIcon(android.R.drawable.ic_menu_add));
     menu.add(0, CALL_LOG, 0, R.string.menu_call_log)
       .setIcon(R.drawable.ic_tab_unselected_recent);
     menu.add(0, CONTACTS, 0, R.string.menu_contacts)
       .setIcon(R.drawable.ic_tab_unselected_contacts);
     menu.add(0, SETTINGS_ID, 0, R.string.menu_settings)
       .setIcon(android.R.drawable.ic_menu_preferences);
 
     return true;
   }
 
   @Override
   public boolean onPrepareOptionsMenu(Menu menu)
   {
     if (digitsView.length() == 0)
       mAddToContacts.setTitle(R.string.menu_new_contacts);
     else
       mAddToContacts.setTitle(R.string.menu_add_contacts);
 
     return true;
   }
 
   @Override
   protected void onResume()
   {
     super.onResume();
 
     // if the mToneGenerator creation fails, just continue without it. It is
     // a local audio signal, and is not as important as the dtmf tone
     // itself.
     synchronized (mToneGeneratorLock)
       {
 	if (mToneGenerator == null)
 	  {
 	    try
 	      {
 		// we want the user to be able to control the volume of the
 		// dial tones
 		// outside of a call, so we use the stream type that is also
 		// mapped to the
 		// volume control keys for this activity
 		mToneGenerator = new ToneGenerator(DIAL_TONE_STREAM_TYPE,
 						   TONE_RELATIVE_VOLUME);
 		setVolumeControlStream(DIAL_TONE_STREAM_TYPE);
 	      }
 	    catch (RuntimeException e)
 	      {
 		Log.w(TAG,
 		      "Exception caught while creating local tone generator: "
 		      + e);
 		mToneGenerator = null;
 	      }
 	  }
       }
 
     setPreferences();
   }
 
   @Override
   protected void onPause()
   {
     super.onPause();
 
     synchronized (mToneGeneratorLock)
       {
 	if (mToneGenerator != null)
 	  {
 	    mToneGenerator.release();
 	    mToneGenerator = null;
 	  }
       }
   }
 
   /*
    * @Override public void onCreateContextMenu(ContextMenu menu, View view,
    * ContextMenuInfo menuInfo) { menu.add(0, SMS, 0, R.string.context_sms); }
    * 
    * @Override public boolean onContextItemSelected(MenuItem item) {
    * AdapterContextMenuInfo info = (AdapterContextMenuInfo)
    * item.getMenuInfo(); switch (item.getItemId()) { case SMS: //Todo: SMS
    * Cursor cursor = (Cursor)myAdapter.getItem(info.position);
    * 
    * return true; default: return super.onContextItemSelected(item); } }
    */
 
   @Override
   public boolean onOptionsItemSelected(MenuItem item)
   {
     switch (item.getItemId())
       {
       case ADD_TO_CONTACTS:
 	startActivity(contactAccessor.addToContacts(digitsView.getText()
 						    .toString()));
 	break;
 
       case SETTINGS_ID:
 	Intent launchPreferencesIntent
 	  = new Intent().setClass(this, Preferences.class);
 	startActivity(launchPreferencesIntent);
 	break;
 
       case CALL_LOG:
 	startActivity(contactAccessor.getCallLogIntent());
 	break;
 
       case CONTACTS:
 	startActivity(contactAccessor.getContactsIntent());
 	break;
       }
     return super.onOptionsItemSelected(item);
   }
 
   private void setupButton(int id)
   {
     ImageButton button = (ImageButton) findViewById(id);
     button.setOnClickListener(this);
     button.setOnTouchListener(onTouchListener);
 
     if (id == R.id.button0 || id == R.id.button1 || id == R.id.deleteButton)
       button.setOnLongClickListener(this);
   }
 
   private void setHandlers()
   {
     setupButton(R.id.button0);
     setupButton(R.id.button1);
     setupButton(R.id.button2);
     setupButton(R.id.button3);
     setupButton(R.id.button4);
     setupButton(R.id.button5);
     setupButton(R.id.button6);
     setupButton(R.id.button7);
     setupButton(R.id.button8);
     setupButton(R.id.button9);
     setupButton(R.id.buttonstar);
     setupButton(R.id.buttonpound);
     setupButton(R.id.dialButton);
     setupButton(R.id.deleteButton);
 
     digitsView.setOnClickListener(this);
     digitsView.setKeyListener(DialerKeyListener.getInstance());
     digitsView
       .addTextChangedListener(new PhoneNumberFormattingTextWatcher());
     digitsView.setInputType(android.text.InputType.TYPE_NULL);
 
     ListView list = (ListView) findViewById(R.id.contactlist);
     list.setOnItemClickListener(this);
     list.setOnScrollListener(this);
 
     /*
      * View keypad = findViewById(R.id.keypad); keypad.setClickable(true);
      * keypad.setOnTouchListener(onTouchListener);
      * 
      * View dialDelete = findViewById(R.id.dial_digits_delete);
      * dialDelete.setOnTouchListener(onTouchListener);
      */
 
     View digits = findViewById(R.id.digitsText);
     digits.setOnTouchListener(onTouchListener);
   }
 
   /*
    * @Override public boolean dispatchKeyEvent(KeyEvent event) { int kc =
    * event.getKeyCode(); if (event.getAction() != KeyEvent.ACTION_UP) { //
    * Only handle up events here. return super.dispatchKeyEvent(event); }
    * 
    * if (kc == KeyEvent.KEYCODE_CALL) { doCall(); return true; } else if (kc
    * == KeyEvent.KEYCODE_DEL) { removeClick(); return true; } else if (kc >=
    * KeyEvent.KEYCODE_0 && kc <= KeyEvent.KEYCODE_9) { char c =
    * event.getNumber(); String s = Character.toString(c); addClick(s, s);
    * return true; } else if (kc >= KeyEvent.KEYCODE_A && kc <=
    * KeyEvent.KEYCODE_Z) { char c = (char) event.getUnicodeChar(); char num =
    * mapToPhone(c); if (LOG) Log.d(TAG, "saw press [" + c + "] -> [" + num +
    * "]"); addClick(Character.toString(num),
    * Character.toString(Character.toUpperCase(c))); return true; } else {
    * return super.dispatchKeyEvent(event); } }
    */
 
   private void createGlob()
   {
     char[] currInput = digitsView.getText().toString().toCharArray();
 		
     curFilter.setLength(0);
     num_pat.setLength(0);
     name_pat.setLength(0);
 		
 
     for (char c : currInput)
       {
 	// Support the dashes or spaces in phone number.
 	if (num_pat.length() > 0)
 	  num_pat.append("[- ]*");
 			
 	if (c == '*')
 	  {
 	    name_pat.append(".");
 	    num_pat.append(".");
 	  }
 	else if (c == '+')
 	  {
 	    name_pat.append("[+]");
 	    num_pat.append("[+]");
 	  }
 	else
 	  {
 	    name_pat.append(buttonToGlobPiece(c));
 	    num_pat.append(c);
 	  }
 	curFilter.append(buttonToGlobPiece(c));
       }
 		
     name_pattern = Pattern.compile(name_pat.toString());
     number_pattern = Pattern.compile(num_pat.toString());
   }
 
   private void updateFilter(boolean add)
   {
     if (!add)
       noMatches = false;
 
     createGlob();
 
     if (noMatches)
       return;
     else
       recalculate();
   }
 
   private void removeAll()
   {
     curFilter.setLength(0);
     digitsView.getText().clear();
     noMatches = false;
     recalculate();
   }
 
   private void doCall()
   {
     Intent i = new Intent(Intent.ACTION_CALL);
     // if it was a long press do something else?
     i.setData(Uri.parse("tel://" + digitsView.getText().toString()));
     startActivity(i);
   }
 
   private void recalculate()
   {
     mHandler.removeCallbacks(mUpdateTimeTask);
     mHandler.postDelayed(mUpdateTimeTask, RECALC_DELAY);
   }
 
   public void onScroll(AbsListView view, int firstVisibleItem,
 		       int visibleItemCount, int totalItemCount)
   {
     // TODO Auto-generated method stub
   }
 
   public void onScrollStateChanged(AbsListView view, int scrollState)
   {
     // TODO Auto-generated method stub
     if (scrollState == OnScrollListener.SCROLL_STATE_FLING)
       {
 	if (hideDialpadOnScroll)
 	  {
 	    toggleDialpad(false);
 	  }
       }
   }
 
   public void onClick(View view)
   {
     click_sound ();
     vibrate ();
 		
     switch (view.getId())
       {
       case R.id.button0:
 	{
 	  playTone(ToneGenerator.TONE_DTMF_0);
 	  keyPressed(KeyEvent.KEYCODE_0);
 	  updateFilter(true);
 	  break;
 	}
       case R.id.button1:
 	{
 	  playTone(ToneGenerator.TONE_DTMF_1);
 	  keyPressed(KeyEvent.KEYCODE_1);
 	  updateFilter(true);
 	  break;
 	}
       case R.id.button2:
 	{
 	  playTone(ToneGenerator.TONE_DTMF_2);
 	  keyPressed(KeyEvent.KEYCODE_2);
 	  updateFilter(true);
 	  break;
 	}
       case R.id.button3:
 	{
 	  playTone(ToneGenerator.TONE_DTMF_3);
 	  keyPressed(KeyEvent.KEYCODE_3);
 	  updateFilter(true);
 	  break;
 	}
       case R.id.button4:
 	{
 	  playTone(ToneGenerator.TONE_DTMF_4);
 	  keyPressed(KeyEvent.KEYCODE_4);
 	  updateFilter(true);
 	  break;
 	}
       case R.id.button5:
 	{
 	  playTone(ToneGenerator.TONE_DTMF_5);
 	  keyPressed(KeyEvent.KEYCODE_5);
 	  updateFilter(true);
 	  break;
 	}
       case R.id.button6:
 	{
 	  playTone(ToneGenerator.TONE_DTMF_6);
 	  keyPressed(KeyEvent.KEYCODE_6);
 	  updateFilter(true);
 	  break;
 	}
       case R.id.button7:
 	{
 	  playTone(ToneGenerator.TONE_DTMF_7);
 	  keyPressed(KeyEvent.KEYCODE_7);
 	  updateFilter(true);
 	  break;
 	}
       case R.id.button8:
 	{
 	  playTone(ToneGenerator.TONE_DTMF_8);
 	  keyPressed(KeyEvent.KEYCODE_8);
 	  updateFilter(true);
 	  break;
 	}
       case R.id.button9:
 	{
 	  playTone(ToneGenerator.TONE_DTMF_9);
 	  keyPressed(KeyEvent.KEYCODE_9);
 	  updateFilter(true);
 	  break;
 	}
       case R.id.buttonpound:
 	{
 	  playTone(ToneGenerator.TONE_DTMF_P);
 	  keyPressed(KeyEvent.KEYCODE_POUND);
 	  updateFilter(true);
 	  break;
 	}
       case R.id.buttonstar:
 	{
 	  playTone(ToneGenerator.TONE_DTMF_S);
 	  keyPressed(KeyEvent.KEYCODE_STAR);
 	  updateFilter(true);
 	  break;
 	}
       case R.id.deleteButton:
 	{
 	  keyPressed(KeyEvent.KEYCODE_DEL);
 	  updateFilter(false);
 	  break;
 	}
       case R.id.dialButton:
 	{
 	  doCall();
 	  break;
 	}
       case R.id.digitsText:
 	{
 			
 	  if (digitsView.length() != 0)
 	    digitsView.setCursorVisible(true);
 	  else
 	    digitsView.setCursorVisible(false);
 
 	  if (dialpad_visible == false)
 	    toggleDialpad(true);
 	  break;
 	}
 
       case R.id.call_button:
 	{
 	  String number = (String) view.getTag();
 	  if (!TextUtils.isEmpty(number))
 	    {
 	      Uri telUri = Uri.fromParts("tel", number, null);
 	      startActivity(new Intent(Intent.ACTION_CALL, telUri));
 	    }
 	  return;
 	}
       }
     toggleDrawable();
   }
 
   public boolean onLongClick(View view)
   {
     boolean result = false;
     switch (view.getId())
       {
       case R.id.button0:
 	{
 	  keyPressed(KeyEvent.KEYCODE_PLUS);
 	  result = true;
 	  updateFilter(true);
 	  break;
 	}
       case R.id.button1:
 	{
 	  if (digitsView.length() == 0)
 	    {
 	      if (hasVoicemail())
 		{
 		  Intent i = new Intent(Intent.ACTION_CALL);
 		  i.setData(Uri.parse("voicemail:"));
 		  startActivity(i);
 		  result = true;
 		  ImageButton digitOne = (ImageButton) findViewById(R.id.button1);
 		  digitOne.setPressed(false);
 		}
 	    }
 	  break;
 	}
       case R.id.deleteButton:
 	{
 	  removeAll();
 	  deleteButton.setPressed(false);
 	  result = true;
 	  break;
 	}
       }
     toggleDrawable();
     return result;
   }
 
   void playTone(int tone)
   {
     if (!mDTMFToneEnabled)
       return;
 
     AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
     int ringerMode = audioManager.getRingerMode();
 
     if (ringerMode == AudioManager.RINGER_MODE_SILENT
 	|| ringerMode == AudioManager.RINGER_MODE_VIBRATE)
       return;
 
     synchronized (mToneGeneratorLock)
       {
 	if (mToneGenerator == null)
 	  {
 	    Log.w(TAG, "playTone: mToneGenerator == null, tone: " + tone);
 	    return;
 	  }
 	mToneGenerator.startTone(tone, TONE_LENGTH_MS);
       }
   }
 
   private void keyPressed(int keyCode)
   {
     KeyEvent event = new KeyEvent(KeyEvent.ACTION_DOWN, keyCode);
     digitsView.onKeyDown(keyCode, event);
   }
 
   static synchronized void vibrate()
   {
     if (prefVibrateOn)
       mVibrator.vibrate(pref_vibrate_time);
   }
 
   static synchronized void click_sound ()
   {
     if (pref_click_sound)
       top_view.playSoundEffect (SoundEffectConstants.CLICK);
   }
   
   // Listeners for the list items.
   private void startContactActivity(Uri lookupUri)
   {
     Uri contactUri = contactAccessor.getContactSplit().getContactUri(
 								     lookupUri);
     Intent i = new Intent(Intent.ACTION_VIEW);
     i.setData(contactUri);
     startActivity(i);
   }
 
   public void onItemClick(AdapterView<?> parent, View view, int position,
 			  long rowid)
   {
     click_sound ();
     vibrate ();
 		
     ContactListItemCache contact = (ContactListItemCache) view.getTag();
     startContactActivity(contact.lookupUri);
   }
 
   private class ContactListAdapter extends ResourceCursorAdapter
   {
     IContactSplit contactSplit;
     private Context mContext;
 
     public ContactListAdapter(Context context, Cursor cur, IContactSplit ics)
     {
       super(context, R.layout.recent_calls_list_item, cur, false);
       contactSplit = ics;
       mContext = context;
     }
 
     @Override
     public View newView(Context context, Cursor cursor, ViewGroup parent)
     {
       final View view = super.newView(context, cursor, parent);
 
       final ContactListItemCache cache = new ContactListItemCache();
       cache.divider = view.findViewById(R.id.list_divider);
       cache.nameView = (TextView) view.findViewById(R.id.name);
       cache.callView = view.findViewById(R.id.call_view);
       cache.callButton = (ImageView) view.findViewById(R.id.call_button);
       if (cache.callButton != null)
 	{
 	  cache.callButton.setOnClickListener(PhoneSpellDialer.this);
 	}
       cache.labelView = (TextView) view.findViewById(R.id.label);
       cache.dataView = (TextView) view.findViewById(R.id.data);
       cache.photoView = (QuickContactBadge) view.findViewById(R.id.photo);
       //cache.photoView.setOnClickListener(PhoneSpellDialer.this);
       cache.nonQuickContactPhotoView 
 	= (ImageView) view.findViewById(R.id.noQuickContactPhoto);
 
       view.setTag(cache);
 
       return view;
     }
 		
     private void highlight (Pattern pat, Spannable str, String digits)
     {
       if (digits.length() == 0)
 	return;
 			
       Matcher m = pat.matcher(str.toString());
 			
       if (m.find(0))
 	apply_highlight(str, m.start(), m.end() - m.start());
     }
 		
     @Override
     public void bindView(View view, Context context, Cursor cursor)
     {
 
       final ContactListItemCache cache = (ContactListItemCache) view.getTag();
       final int DISPLAY_NAME_INDEX = 2;
       final int PHONE_NUMBER_INDEX = 3;
       final int PHONE_TYPE_INDEX = 4;
       final int PHONE_LABEL_INDEX = 5;
       final int PHOTO_ID_INDEX = 6;
 
       // Set the name
       final String name = cursor.getString(DISPLAY_NAME_INDEX);
       cache.nameView.setText(name, TextView.BufferType.SPANNABLE);
 			
       highlight(name_pattern, (Spannable) cache.nameView.getText(),
 		digitsView.getText().toString());
 			
       if (!cursor.isNull(PHONE_TYPE_INDEX))
 	{
 	  cache.labelView.setVisibility(View.VISIBLE);
 
 	  final int type = cursor.getInt(PHONE_TYPE_INDEX);
 	  final String label = cursor.getString(PHONE_LABEL_INDEX);
 	  cache.labelView.setText(Phone.getTypeLabel(context.getResources(),
 						     type, label));
 	} 
       else
 	{
 	  // There is no label, hide the the view
 	  cache.labelView.setVisibility(View.GONE);
 	}
 
       final String number = cursor.getString(PHONE_NUMBER_INDEX);
       cache.dataView.setText(number, TextView.BufferType.SPANNABLE);
 
       highlight(number_pattern, (Spannable) cache.dataView.getText(),
 		digitsView.getText().toString());
 
       cache.callButton.setTag(number);
       Uri lookupUri = contactSplit.getLookupUri(cursor);
       cache.lookupUri = lookupUri;
 
       if (showContactPictures)
 	{
 	  cache.photoView.assignContactUri(lookupUri);
 	  cache.photoView.setVisibility(View.VISIBLE);
 	  cache.nonQuickContactPhotoView.setVisibility(View.INVISIBLE);
 
 	  long photoId = -1;
 
 	  if (!cursor.isNull(PHOTO_ID_INDEX))
 	    {
 	      photoId = cursor.getLong(PHOTO_ID_INDEX);
 	    }
 
 	  // Reference:
 	  // http://thinkandroid.wordpress.com/2009/12/30/handling-contact-photos-all-api-levels/
 	  Bitmap photo = null;
 	  if (photoId != -1)
 	    {
 	      photo = loadContactPhoto(mContext, photoId, null);
 	    }
 
 	  if (photo != null)
 	    {
 	      cache.photoView.setImageBitmap(photo);
 	    } 
 	  else
 	    {
 	      cache.photoView.setImageResource(R.drawable
 					       .ic_contact_list_picture);
 	    }
 	}
       else
 	{
 	  cache.photoView.setVisibility(View.GONE);
 	  cache.nonQuickContactPhotoView.setVisibility(View.GONE);
 	}
 
     }
 
     @Override
     public String convertToString(Cursor cursor)
     {
       return cursor.getString(2);
     }
   }
 
   public static Bitmap loadContactPhoto(Context context, long photoId,
 					BitmapFactory.Options options)
   {
     Cursor photoCursor = null;
     Bitmap photoBm = null;
 
     try
       {
 	ContentResolver cr = context.getContentResolver();
 	photoCursor = cr.query(ContentUris.withAppendedId(Data.CONTENT_URI,
 							  photoId),
 			       new String[] { Photo.PHOTO }, null, null, null);
 
 	if (photoCursor.moveToFirst() && !photoCursor.isNull(0))
 	  {
 	    byte[] photoData = photoCursor.getBlob(0);
 	    photoBm = BitmapFactory.decodeByteArray(photoData, 0,
 						    photoData.length, options);
 	  }
       }
     finally
       {
 	if (photoCursor != null)
 	  {
 	    photoCursor.close();
 	  }
       }
 
     return photoBm;
   }
 
   private static void apply_highlight(Spannable name, int start, int end)
   {
    if (len == 0)
       return;
     
     if (matchedItalics)
       name.setSpan(ITALIC_STYLE, start, end,
 		   Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
 
     if (matchedBold)
       name.setSpan(BOLD_STYLE, start, end,
 		   Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
 
     if (matchedDigits)
       name.setSpan(matchedDigitsColor, start, end,
 		   Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
 
     if (matchedHighlight)
       name.setSpan(matchedHighlightColor, start, end,
 		   Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
   }
 
   private void toggleDrawable()
   {
     final boolean notEmpty = digitsView.length() != 0;
     if (notEmpty)
       {
 	digitsView.setBackgroundDrawable(mDigitsBackground);
 	dialButton.setEnabled(true);
 	deleteButton.setEnabled(true);
       } 
     else
       {
 	digitsView.setCursorVisible(false);
 	digitsView.setBackgroundDrawable(mDigitsEmptyBackground);
 	dialButton.setEnabled(false);
 	deleteButton.setEnabled(false);
       }
   }
 
   private final OnTouchListener onTouchListener = new OnTouchListener()
     {
       GestureDetector detect = new GestureDetector(new ToggleDialPadListener());
       {
 	detect.setIsLongpressEnabled(false);
       }
 
       public boolean onTouch(View v, MotionEvent event)
       {
 	return detect.onTouchEvent(event);
       }
     };
 
   private class ToggleDialPadListener extends SimpleOnGestureListener
   {
     private static final float MIN_VELOCITY_DIP = -100.0f;
     private static final float MIN_VELOCITY_RISE = 100.0f;
 
     @Override
     public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
 			    float distanceY)
     {
       return super.onScroll(e1, e2, distanceX, distanceY);
     }
 		
     @Override
     public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
 			   float velocityY)
     {
       // Calculate required fling length. It's twice of digits view height.
       int twice_height = digitsView.getHeight() * 2;
       // Real fling length. 
       // XXX What can I do with getYprecision() ?
       int fling_y_length = (int)(Math.abs(e1.getY() - e2.getY()));
       int fling_x_length = (int)(Math.abs(e1.getX() - e2.getX()));
 
       if (fling_y_length >= twice_height)
 	{
 	  if (velocityY < MIN_VELOCITY_DIP)
 	    toggleDialpad(true);
 	  else if (velocityY > MIN_VELOCITY_RISE)
 	    toggleDialpad(false);
 	}
       else if (fling_x_length >= twice_height)
 	{
 	  if (velocityX < MIN_VELOCITY_DIP)
 	    {
 	      vibrate();
 	      keyPressed(KeyEvent.KEYCODE_DEL);
 	      updateFilter(false);
 	    }
 	}
 			
       return super.onFling(e1, e2, velocityX, velocityY);
     }
   }
 
   private void toggleDialpad(boolean showDialPad)
   {
     if (showDialPad == dialpad_visible)
       return;
 
     vibrate();
     
     View dialPad = findViewById(R.id.keypad);
     if (showDialPad)
       {
 	dialPad.setVisibility(View.VISIBLE);
 	dialpad_visible = true;
       } 
     else
       {
 	dialPad.setVisibility(View.GONE);
 	dialpad_visible = false;
       }
   }
 
   private static String buttonToGlobPiece(char c)
   {
     if (find_patterns.length > 0 && c > '0' && c <= '9')
       {
 	if (c == '*')
 	  return "?";
 	int index = c -'1';
 	return find_patterns[index];
       }
     switch (c)
       {
       case '2':
 	return "[2ABC\u00c0\u00c1\u00c2\u00c3\u00c4\u00c5\u00e0\u00e1\u00e2\u00e3\u00e4\u00e5\u0100\u0101\u0102\u0103\u0104\u0105\u01cd\u01ce\u01de\u01df\u01e0\u01e1\u01fa\u01fb\u0200\u0201\u0202\u0203\u0226\u0227\u1e00\u1e01\u1ea0\u1ea1\u1ea2\u1ea3\u1ea4\u1ea5\u1ea6\u1ea7\u1ea8\u1ea9\u1eaa\u1eab\u1eac\u1ead\u1eae\u1eaf\u1eb0\u1eb1\u1eb2\u1eb3\u1eb4\u1eb5\u1eb6\u1eb7\u212bfrom b: \u1e02\u1e03\u1e04\u1e05\u1e06\u1e07\u00c7\u00e7\u0106\u0107\u0108\u0109\u010a\u010b\u010c\u010d\u1e08\u1e09]";
       case '3':
 	return "[3DEF\u010e\u010f\u1e0a\u1e0b\u1e0c\u1e0d\u1e0e\u1e0f\u1e10\u1e11\u1e12\u1e13\u00c8\u00c9\u00ca\u00cb\u00e8\u00e9\u00ea\u00eb\u0112\u0113\u0114\u0115\u0116\u0117\u0118\u0119\u011a\u011b\u0204\u0205\u0206\u0207\u0228\u0229\u1e14\u1e15\u1e16\u1e17\u1e18\u1e19\u1e1a\u1e1b\u1e1c\u1e1d\u1eb8\u1eb9\u1eba\u1ebb\u1ebc\u1ebd\u1ebe\u1ebf\u1ec0\u1ec1\u1ec2\u1ec3\u1ec4\u1ec5\u1ec6\u1ec7\u1e1e\u1e1f]";
       case '4':
 	return "[4GHI\u011c\u011d\u011e\u011f\u0120\u0121\u0122\u0123\u01e6\u01e7\u01f4\u01f5\u1e20\u1e21\u0124\u0125\u021e\u021f\u1e22\u1e23\u1e24\u1e25\u1e26\u1e27\u1e28\u1e29\u1e2a\u1e2b\u1e96\u00cc\u00cd\u00ce\u00cf\u00ec\u00ed\u00ee\u00ef\u0128\u0129\u012a\u012b\u012c\u012d\u012e\u012f\u0130\u01cf\u01d0\u0208\u0209\u020a\u020b\u1e2c\u1e2d\u1e2e\u1e2f\u1ec8\u1ec9\u1eca\u1ecb]";
       case '5':
 	return "[5JKL\u0134\u0135\u01f0\u0136\u0137\u01e8\u01e9\u1e30\u1e31\u1e32\u1e33\u1e34\u1e35\u212a\u0139\u013a\u013b\u013c\u013d\u013e\u1e36\u1e37\u1e38\u1e39\u1e3a\u1e3b\u1e3c\u1e3d]";
       case '6':
 	return "[6MNO\u1e3e\u1e3f\u1e40\u1e41\u1e42\u1e43\u00d1\u00f1\u0143\u0144\u0145\u0146\u0147\u0148\u01f8\u01f9\u1e44\u1e45\u1e46\u1e47\u1e48\u1e49\u1e4a\u1e4b\u00d2\u00d3\u00d4\u00d5\u00d6\u00d8\u00f2\u00f3\u00f4\u00f5\u00f6\u00f8\u014c\u014d\u014e\u014f\u0150\u0151\u01a0\u01a1\u01d1\u01d2\u01ea\u01eb\u01ec\u01ed\u020c\u020d\u020e\u020f\u022a\u022b\u022c\u022d\u022e\u022f\u0230\u0231\u1e4c\u1e4d\u1e4e\u1e4f\u1e50\u1e51\u1e52\u1e53\u1ecc\u1ecd\u1ece\u1ecf\u1ed0\u1ed1\u1ed2\u1ed3\u1ed4\u1ed5\u1ed6\u1ed7\u1ed8\u1ed9\u1eda\u1edb\u1edc\u1edd\u1ede\u1edf\u1ee0\u1ee1\u1ee2\u1ee3]";
       case '7':
 	return "[7PQRS\u1e54\u1e55\u1e56\u1e57\u0154\u0155\u0156\u0157\u0158\u0159\u0210\u0211\u0212\u0213\u1e58\u1e59\u1e5a\u1e5b\u1e5c\u1e5d\u1e5e\u1e5f\u00df\u015a\u015b\u015c\u015d\u015e\u015f\u0160\u0161\u0218\u0219\u1e60\u1e61\u1e62\u1e63\u1e64\u1e65\u1e66\u1e67\u1e68\u1e69]";
       case '8':
 	return "[8TUV\u0162\u0163\u0164\u0165\u021a\u021b\u1e6a\u1e6b\u1e6c\u1e6d\u1e6e\u1e6f\u1e70\u1e71\u1e97\u00d9\u00da\u00db\u00dc\u00f9\u00fa\u00fb\u00fc\u0168\u0169\u016a\u016b\u016c\u016d\u016e\u016f\u0170\u0171\u0172\u0173\u01af\u01b0\u01d3\u01d4\u01d5\u01d6\u01d7\u01d8\u01d9\u01da\u01db\u01dc\u0214\u0215\u0216\u0217\u1e72\u1e73\u1e74\u1e75\u1e76\u1e77\u1e78\u1e79\u1e7a\u1e7b\u1ee4\u1ee5\u1ee6\u1ee7\u1ee8\u1ee9\u1eea\u1eeb\u1eec\u1eed\u1eee\u1eef\u1ef0\u1ef1\u1e7c\u1e7d\u1e7e\u1e7f]";
       case '9':
 	return "[9WXYZ\u0174\u0175\u1e80\u1e81\u1e82\u1e83\u1e84\u1e85\u1e86\u1e87\u1e88\u1e89\u1e98\u1e8a\u1e8b\u1e8c\u1e8d\u00dd\u00fd\u00ff\u0176\u0177\u0178\u0232\u0233\u1e8e\u1e8f\u1e99\u1ef2\u1ef3\u1ef4\u1ef5\u1ef6\u1ef7\u1ef8\u1ef9\u0179\u017a\u017b\u017c\u017d\u017e\u1e90\u1e91\u1e92\u1e93\u1e94\u1e95]";
       case '*':
 	return "?";
       default:
 	return String.valueOf(c);
       }
   }
 
   // Wysie: Method to set digits colour
   private void setDigitsColor(SharedPreferences ePrefs)
   {
     int colorPressed = -16777216;
     int colorFocused = -1;
     int colorUnselected = -1;
 
     if (ePrefs.getBoolean("dial_digit_use_custom_color", false))
       {
 	try
 	  {
 	    String c;
 	    c = ePrefs.getString("pressed_digit_color_custom", "-16777216");
 	    colorPressed = Color.parseColor(c);
 	    c = ePrefs.getString("focused_digit_color_custom", "-1");
 	    colorFocused = Color.parseColor(c);
 	    c = ePrefs.getString("unselected_digit_color_custom", "-1");
 	    colorUnselected = Color.parseColor(c);
 	  } 
 	catch (IllegalArgumentException e)
 	  {
 	    // Do nothing
 	  }
       } 
     else
       {
 	String c;
 	c = ePrefs.getString("pressed_digit_color", "-16777216");
 	colorPressed = Integer.parseInt(c);
 	c = ePrefs.getString("focused_digit_color", "-1");
 	colorFocused = Integer.parseInt(c);
 	c = ePrefs.getString("unselected_digit_color", "-1");
 	colorUnselected = Integer.parseInt(c);
       }
 
     digitsView.setTextColor(new ColorStateList(new int[][]
       { new int[] { android.R.attr.state_pressed },
 	new int[] { android.R.attr.state_focused },
 	new int[0] },
 	new int[] { colorPressed, colorFocused, colorUnselected }));
     digitsView.setCursorVisible(false);
   }
 
   // Wysie: Check for voicemail number
   private boolean hasVoicemail()
   {
     boolean hasVoicemail = false;
     TelephonyManager mgr
       = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
 
     try
       {
 	String num = mgr.getVoiceMailNumber();
 	if (!(num == null || num.equals("")))
 	  hasVoicemail = true;
       } 
     catch (SecurityException se)
       {
 	// Possibly no READ_PHONE_STATE privilege.
       } 
     catch (NullPointerException e)
       {
 	//
       }
 
     return hasVoicemail;
   }
 
   private void updatecontactlist(Cursor cur)
   {
 
     Log.i("updatecontactlist", String.format("cursor count: %d",
 					     cur.getCount()));
 
     if (cur.getCount() == 0)
       {
 	noMatches = true;
       }
 
     if (myAdapter == null)
       {
 	startManagingCursor(cur);
 	myAdapter = new ContactListAdapter(this, cur,
 					   contactAccessor.getContactSplit());
       }
     else
       {
 	try
 	  {
 	    myAdapter.getCursor().deactivate();
 	  }
 	catch (java.lang.NullPointerException ex)
 	  {
 	  }
 	myAdapter.changeCursor(cur);
       }
 
     myContactList.invalidate();
   }
 
   final static class ContactListItemCache
   {
     public View divider;
     public TextView nameView;
     public View callView;
     public ImageView callButton;
     public TextView labelView;
     public TextView dataView;
     public Uri lookupUri;
 
     public QuickContactBadge photoView;
     public ImageView nonQuickContactPhotoView;
   }
 
   class SearchContactsTask extends AsyncTask<String, Integer, Cursor>
   {
 
     @Override
     protected Cursor doInBackground(String... filter)
       {
 
 	Log.i("SearchContactsTask.doInBackground", "Filter:"
 	      .concat(filter[0]));
 	Cursor cur
 	  = PhoneSpellDialer.this.contactAccessor.recalculate(filter[0],
 							      matchAnywhere);
 	return cur;
       }
 
     protected void onProgressUpdate(Integer... progress)
     {
     }
 
     protected void onPostExecute(Cursor result)
     {
       PhoneSpellDialer.this.updatecontactlist(result);
     }
   }
 
   /*
    * delay the search
    */
 
   class UpdateTimerTask extends TimerTask
   {
     public void run()
     {
       String s = PhoneSpellDialer.this.curFilter.toString();
 			
       if (s.indexOf("#") != -1)
 	s = s.replace('#', ' ');
 
       if (s.indexOf('-') != -1)
 	s = s.replaceAll("-", "");
 			
       new SearchContactsTask().execute(s);
 			
       // Log.i("scott: timer", "Execute Timer:".concat(s));
     }
   }
 
   // Declare custom class to produce click sound and vibration.
   public static class QuickContactBadgeClick extends QuickContactBadge
   {
     public QuickContactBadgeClick (Context context, AttributeSet attrs)
     {
       super (context, attrs);
     }
     
     @Override
     public void onClick (View view)
     {
       PhoneSpellDialer.click_sound();
       PhoneSpellDialer.vibrate();
       super.onClick (view);
     }
   }
 }
