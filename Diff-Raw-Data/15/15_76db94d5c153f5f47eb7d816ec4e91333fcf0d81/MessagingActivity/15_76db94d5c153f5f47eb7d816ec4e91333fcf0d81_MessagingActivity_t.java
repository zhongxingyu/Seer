 package org.easysms.android;
 
 import java.io.FileOutputStream;
 import java.io.OutputStreamWriter;
 import java.sql.Date;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.StringTokenizer;
 
 import org.easysms.android.data.Conversation;
 import org.easysms.android.data.Sms;
 import org.easysms.android.ui.FlingAndScrollViewer;
 import org.easysms.android.ui.FlowLayout;
 import org.easysms.android.util.TextToSpeechManager;
 
 import android.annotation.TargetApi;
 import android.app.Activity;
 import android.app.PendingIntent;
 import android.content.BroadcastReceiver;
 import android.content.ContentResolver;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.pm.PackageManager;
 import android.content.pm.ResolveInfo;
 import android.database.Cursor;
import android.database.DatabaseUtils;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.media.MediaPlayer;
 import android.media.MediaPlayer.OnCompletionListener;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Vibrator;
 import android.provider.ContactsContract;
 import android.provider.ContactsContract.CommonDataKinds.Phone;
 import android.provider.ContactsContract.CommonDataKinds.Photo;
 import android.provider.ContactsContract.Contacts;
 import android.provider.ContactsContract.Data;
 import android.provider.ContactsContract.PhoneLookup;
 import android.speech.RecognizerIntent;
 import android.telephony.SmsManager;
 import android.util.Log;
 import android.util.TypedValue;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnLongClickListener;
 import android.view.ViewGroup;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.BaseAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.GridView;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.Spinner;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.SherlockActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 
 @TargetApi(8)
 public class MessagingActivity extends SherlockActivity implements
 		OnClickListener {
 
 	// class for the GridView of quick sender
 	public class ImageAdapter extends BaseAdapter {
 
 		private Context mContext;
 
 		private String[] mTextLabels = { "Ok", "Non", "Retard", "Appel",
 				"Vide", "Occup", "Vite!", "a va?", "Amour", "Voiture",
 				"Date?", "Heure?" };
 
 		// references to our images
 		private Integer[] mThumbIds = { R.drawable.ok, R.drawable.no,
 				R.drawable.late, R.drawable.ic_action_call,
 				R.drawable.nobattery, R.drawable.busy, R.drawable.hurryup,
 				R.drawable.howareu, R.drawable.loveu, R.drawable.driving,
 				R.drawable.whatdate, R.drawable.whattime };
 
 		public ImageAdapter(Context c) {
 			mContext = c;
 		}
 
 		public int getCount() {
 			return mThumbIds.length;
 		}
 
 		public Object getItem(int position) {
 			return null;
 		}
 
 		public long getItemId(int position) {
 			return 0;
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			View v;
 			if (convertView == null) {
 				LayoutInflater li = getLayoutInflater();
 				v = li.inflate(R.layout.icon, null);
 				TextView tv = (TextView) v.findViewById(R.id.icon_text);
 
 				// sets the text that matches the image.
 				tv.setText(mTextLabels[position]);
 
 				ImageView iv = (ImageView) v.findViewById(R.id.icon_image);
 				iv.setImageResource(mThumbIds[position]);
 
 			} else {
 				v = convertView;
 			}
 			return v;
 		}
 	}
 
 	/**
 	 * Handles the response of the broadcast request about the recognizer
 	 * supported languages.
 	 * 
 	 * The receiver is required only if the application wants to do recognition
 	 * in a specific language.
 	 */
 	private class SupportedLanguageBroadcastReceiver extends BroadcastReceiver {
 
 		@Override
 		public void onReceive(Context context, final Intent intent) {
 			Log.i("MessageActivity", "Receiving broadcast " + intent);
 
 			final Bundle extra = getResultExtras(false);
 
 			if (getResultCode() != Activity.RESULT_OK) {
 				mHandler.post(new Runnable() {
 					@Override
 					public void run() {
 						showToast("Error code:" + getResultCode());
 						TextToSpeechManager.getInstance().say(
 								"Error code:" + getResultCode());
 					}
 				});
 			}
 
 			if (extra == null) {
 				mHandler.post(new Runnable() {
 					@Override
 					public void run() {
 						showToast("No extra");
 					}
 				});
 			}
 
 			if (extra.containsKey(RecognizerIntent.EXTRA_SUPPORTED_LANGUAGES)) {
 				mHandler.post(new Runnable() {
 
 					@Override
 					public void run() {
 					}
 				});
 			}
 
 			if (extra.containsKey(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE)) {
 				mHandler.post(new Runnable() {
 
 					@Override
 					public void run() {
 					}
 				});
 			}
 		}
 
 		private void showToast(String text) {
 			// Toast.makeText(this, text, 1000).show();
 		}
 	}
 
 	private static final String[] LABELS = new String[] { "Ok. ", "Non. ",
 			"Dsol je suis en retard. ", "Appelle moi. ",
 			"Dsol je n'ai plus de batterie. ",
 			"Je ne peux pas rpondre, je suis occup. ", "Dpche toi! ",
 			"a va? ", "Je pense  toi. ",
 			"Je ne peux pas rpondre, je suis au volant. ", "Quelle date? ",
 			"Quelle heure? " };
 
 	private static final ArrayList<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();
 
 	/** Identifier of the extra that indicates a new message should be shown. */
 	public static final String NEW_MESSAGE_EXTRA = "NewMsg";
 	/** Identifier of the extra used to pass the name. */
 	public static final String NAME_EXTRA = "Name";
 	/** Identifier of the extra used to pass the phone number. */
 	public static final String PHONENUMBER_EXTRA = "Tel";
 
 	static final int TIME_DIALOG_ID = 1;
 	private static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;
 	private LinearLayout bubblelayoutreceivedplay;
 	private ImageView flaglanguage;
 	// for the snapping scroll
 	private FlingAndScrollViewer flingAndScrollViewer;
 	private FlowLayout flowlayout;
 	private FlowLayout flowlayoutreceived;
 	private FlowLayout flowlayoutspeechrecog1;
 
 	private FileOutputStream fOut = null;
 	private Handler handler;
 	private Locale language = Locale.FRENCH;
 	private ArrayList<String> matches;
 	// ---------date and time
 	private TextView mDateDisplay;
 	private int mDay;
 	// Button btnSend;
 	private String messageSent = "";
 	private Handler mHandler;
 	private int mhour;
 	private ListView mList;
 	private MediaPlayer mMediaPlayer;
 	private int mminute;
 	private int mMonth;
 	private ImageView mPickDate;
 	private ImageView mPickTime;
 	private LinearLayout msgbubblelayout;
 	private LinearLayout msgdetailslayout;
 	private LinearLayout msgreceived;
 	private LinearLayout msgreceived1;
 	private LinearLayout msgsent;
 	private LinearLayout msgsent1;
 	private Spinner mSupportedLanguageView;
 	private TextView mTimeDisplay;
 	private int mYear;
 	private String nameContact = "Numro inconnu";
 	// to know if it is message details or new message
 	private Boolean newMsg;
 	private InboxActivity ob;
 	private MessagingActivity objectTest;
 	private OutputStreamWriter osw = null;
 	private String phoneNo = "";
 	// get info from the previous page
 	private String phoneNumContact = "Nom inconnu";
 	private int PICK_CONTACT;
 	private ImageView profile;
 	private TextView recipient;
 	private TextView recipientnum;
 	private ImageView speakButton;
 	private LinearLayout speechrecolayout;
 	private int timesKaraoke = 0;
 	private EditText txtMessage;
 
 	// displays all the SMSs in a conversation.
 	private void createLayoutbubbleconv(Conversation conv) {
 
 		for (final Sms sms : conv.listsms) {
 
 			LinearLayout wholelayout = new LinearLayout(this);
 
 			LinearLayout.LayoutParams layoutParamsWhole = new LinearLayout.LayoutParams(
 					(int) TypedValue.applyDimension(
 							TypedValue.COMPLEX_UNIT_DIP, 310, getResources()
 									.getDisplayMetrics()),
 					LinearLayout.LayoutParams.WRAP_CONTENT);
 			layoutParamsWhole.setMargins(0, (int) TypedValue.applyDimension(
 					TypedValue.COMPLEX_UNIT_DIP, 2, getResources()
 							.getDisplayMetrics()), 0, (int) TypedValue
 					.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2,
 							getResources().getDisplayMetrics()));
 			wholelayout.setLayoutParams(layoutParamsWhole);
 			wholelayout.setOrientation(LinearLayout.HORIZONTAL);
 			// LINEAR LAYOUT
 			LinearLayout linlayout = new LinearLayout(this);
 			linlayout.setLayoutParams(new LayoutParams((int) TypedValue
 					.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 260,
 							getResources().getDisplayMetrics()),
 					LayoutParams.WRAP_CONTENT));
 			// linlayout.setBackgroundResource(R.drawable.bubblelast);
 			linlayout.setOrientation(LinearLayout.HORIZONTAL);
 
 			if (sms.isSent) {
 				linlayout.setGravity(Gravity.RIGHT);
 			} else {
 				linlayout.setGravity(Gravity.LEFT);
 			}
 
 			// FLOWLAYOUT
 			final FlowLayout fl = new FlowLayout(this);
 			fl.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
 					LayoutParams.MATCH_PARENT));
 			fl.setBackgroundResource(R.drawable.bubblelast);
 
 			Button dateButton = new Button(this);
 			dateButton.setSingleLine(false);
 			String textButton = sms.getDate(this); // + "\n" + sms.timesms;
 			dateButton.setText(textButton);
 			dateButton.setLayoutParams(new LayoutParams(
 					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
 			dateButton.setBackgroundResource(R.drawable.datebutton);
 			dateButton.setGravity(Gravity.RIGHT);
 
 			// TODO: test how date is represented.
 			final String datesmsplayed = textButton;
 			dateButton.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View v) {
 
 					// plays the audio.
 					TextToSpeechManager.getInstance().say(datesmsplayed);
 				}
 			});
 			fl.addView(dateButton);
 
 			StringTokenizer st = new StringTokenizer(sms.body);
 			String[] tabWords = new String[100];
 			int nbWords = 0;
 			while (st.hasMoreElements()) {
 				tabWords[nbWords] = (String) st.nextElement();
 				nbWords++;
 			}
 			// create a button for each words and append it to the bubble
 			// composition
 			for (int i = 0; i < nbWords; ++i) {
 				final Button btn = new Button(this);
 				btn.setText(tabWords[i]);
 				// btn.setTextSize((int)
 				// TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,10,
 				// getResources().getDisplayMetrics()));
 				btn.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
 						LayoutParams.WRAP_CONTENT));
 				btn.setBackgroundResource(R.drawable.button);
 				// btn.setTextSize(16);
 
 				final String toSay = tabWords[i];
 
 				// play each button
 				btn.setOnClickListener(new OnClickListener() {
 					@Override
 					public void onClick(View v) {
 
 						// plays the audio.
 						TextToSpeechManager.getInstance().say(toSay);
 
 					}
 				});
 				final Vibrator vibrationrecomp = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
 				// recomposition
 				btn.setOnLongClickListener(new OnLongClickListener() {
 
 					@Override
 					public boolean onLongClick(View v) {
 						final Button bouton = new Button(getBaseContext());
 						bouton.setText(btn.getText());
 						// bouton.setTextSize((int)
 						// TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,10,
 						// getResources().getDisplayMetrics()));
 						bouton.setLayoutParams(new LayoutParams(
 								LayoutParams.WRAP_CONTENT,
 								LayoutParams.WRAP_CONTENT));
 						bouton.setBackgroundResource(R.drawable.button);
 						flowlayout.addView(bouton, new LayoutParams(
 								LayoutParams.WRAP_CONTENT,
 								LayoutParams.WRAP_CONTENT));
 						vibrationrecomp.vibrate(200);
 
 						Date currentDate = new Date(System.currentTimeMillis());
 						String date = (String) android.text.format.DateFormat
 								.format("yyyy-MM-dd'T'kk:mm:ss'Z'", currentDate);
 
 						// play each button
 						bouton.setOnClickListener(new OnClickListener() {
 							@Override
 							public void onClick(View v) {
 
 								// plays the audio.
 								TextToSpeechManager.getInstance().say(toSay);
 
 								Date currentDate = new Date(System
 										.currentTimeMillis());
 								String date = (String) android.text.format.DateFormat
 										.format("yyyy-MM-dd'T'kk:mm:ss'Z'",
 												currentDate);
 
 							}
 						});
 
 						final Vibrator vibrationdelete = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
 						// on long click, delete the button
 						bouton.setOnLongClickListener(new OnLongClickListener() {
 							@Override
 							public boolean onLongClick(View v) {
 								flowlayout.removeView(bouton);
 								vibrationdelete.vibrate(200);
 
 								Date currentDate = new Date(System
 										.currentTimeMillis());
 								String date = (String) android.text.format.DateFormat
 										.format("yyyy-MM-dd'T'kk:mm:ss'Z'",
 												currentDate);
 
 								return true;
 							}
 						});
 						return true;
 					}
 
 				});
 
 				LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
 						LinearLayout.LayoutParams.MATCH_PARENT,
 						LinearLayout.LayoutParams.WRAP_CONTENT);
 
 				layoutParams.setMargins((int) TypedValue.applyDimension(
 						TypedValue.COMPLEX_UNIT_SP, 50, getResources()
 								.getDisplayMetrics()), (int) TypedValue
 						.applyDimension(TypedValue.COMPLEX_UNIT_SP, 80,
 								getResources().getDisplayMetrics()),
 						(int) TypedValue.applyDimension(
 								TypedValue.COMPLEX_UNIT_SP, 50, getResources()
 										.getDisplayMetrics()), (int) TypedValue
 								.applyDimension(TypedValue.COMPLEX_UNIT_SP, 80,
 										getResources().getDisplayMetrics()));
 
 				fl.addView(btn, layoutParams);
 
 			}
 
 			// PLAYBUTTON
 
 			ImageView playButton = new ImageView(this);
 			playButton.setBackgroundResource(R.drawable.playsmsclick);
 			playButton.setOnCreateContextMenuListener(this);
 			playButton.setOnCreateContextMenuListener(this);
 			playButton.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View v) {
 					playKaraoke(fl);
 				}
 			});
 
 			linlayout.addView(fl);
 			if (sms.isSent) {
 				wholelayout.addView(playButton);
 				wholelayout.addView(linlayout);
 			} else {
 				wholelayout.addView(linlayout);
 				wholelayout.addView(playButton);
 			}
 			msgdetailslayout.addView(wholelayout);
 		}
 	}
 
 	private String getContactNameFromNumber(String number) {
 		/*
 		 * We have a phone number and we want to grab the name of the contact
 		 * with that number, if such a contact exists
 		 */
 		Uri lookupUri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI,
 				Uri.encode(number));
 		/* phoneNumber here being a variable with the phone number stored. */
 		Cursor c = getBaseContext().getContentResolver().query(lookupUri,
 				new String[] { PhoneLookup.DISPLAY_NAME }, null, null, null);
 		/*
 		 * If we want to get something other than the displayed name for the
 		 * contact, then just use something else instead of DISPLAY_NAME
 		 */
 		String name = "Contact inconnu";
 		while (c.moveToNext()) {
 			/* If we find a match we put it in a String. */
 			name = c.getString(c
 					.getColumnIndexOrThrow(PhoneLookup.DISPLAY_NAME));
 		}
 		return name;
 
 	}
 
 	private String getContactPhotoFromNumber(String number) {
 		/*
 		 * We have a phone number and we want to grab the name of the contact
 		 * with that number, if such a contact exists
 		 */
 		Uri lookupUri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI,
 				Uri.encode(number));
 		/* phoneNumber here being a variable with the phone number stored. */
 		Cursor c = getBaseContext().getContentResolver().query(lookupUri,
 				new String[] { PhoneLookup.PHOTO_ID }, null, null, null);
 		// long photo =
 		// c.getLong(c.getColumnIndexOrThrow(ContactsContract.Contacts.PHOTO_ID));
 		/*
 		 * If we want to get something other than the displayed name for the
 		 * contact, then just use something else instead of DISPLAY_NAME
 		 */
 		String photoId = null;
 		while (c.moveToNext()) {
 			// if we find a match we put it in a String.
 			photoId = c
 					.getString(c.getColumnIndexOrThrow(PhoneLookup.PHOTO_ID));
 		}
 		return photoId;
 	}
 
 	public void helpVoiceRecog() {
 
 		Runnable runnable = new Runnable() {
 			@Override
 			public void run() {
 
 				// plays the audio.
 				TextToSpeechManager.getInstance().say("Parlez aprs le bip.");
 
 				try {
 					Thread.sleep(1200);
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 				handler.post(new Runnable() {
 					@Override
 					public void run() {
 
 						playAudio();
 
 					}
 				});
 
 			}
 		};
 		new Thread(runnable).start();
 
 	}
 
 	// to check if there is an Internet connection available
 	public final boolean isInternetOn() {
 		ConnectivityManager connec = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 		// ARE WE CONNECTED TO THE NET
 		if ((connec.getNetworkInfo(0) != null && connec.getNetworkInfo(0)
 				.getState() == NetworkInfo.State.CONNECTED)
 				|| (connec.getNetworkInfo(1) != null && connec
 						.getNetworkInfo(1).getState() == NetworkInfo.State.CONNECTED)) {
 			// MESSAGE TO SCREEN FOR TESTING (IF REQ)
 			// Toast.makeText(this, connec.getNetworkInfo(0) + "connected",
 			// Toast.LENGTH_LONG).show();
 			return true;
 		} else if ((connec.getNetworkInfo(0) != null && connec
 				.getNetworkInfo(0).getState() == NetworkInfo.State.DISCONNECTED)
 				|| (connec.getNetworkInfo(1) != null && connec
 						.getNetworkInfo(1).getState() == NetworkInfo.State.DISCONNECTED)) {
 			// System.out.println(Not Connected);
 			return false;
 		}
 		return false;
 	}
 
 	private boolean MenuChoice(MenuItem item) {
 		String select = "Vous avez slectionn";
 		String langue = "";
 
 		switch (item.getItemId()) {
 		case 0:
 			language = Locale.FRENCH;
 			langue = "franais";
 			flaglanguage.setBackgroundResource(R.drawable.frenchflag);
 
 			// plays the audio.
 			TextToSpeechManager.getInstance().say(select + langue);
 
 			return true;
 
 		case 1:
 			language = Locale.ENGLISH;
 			langue = "anglais";
 			flaglanguage.setBackgroundResource(R.drawable.americanflag);
 
 			// plays the audio.
 			TextToSpeechManager.getInstance().say(select + langue);
 
 			return true;
 
 		case 2:
 			language = Locale.ITALIAN;
 			langue = "italien";
 			flaglanguage.setBackgroundResource(R.drawable.italianflag);
 
 			// plays the audio.
 			TextToSpeechManager.getInstance().say(select + langue);
 
 			return true;
 
 		case 3:
 
 			language = Locale.GERMAN;
 			langue = "allemand";
 			flaglanguage.setBackgroundResource(R.drawable.germanflag);
 
 			// plays the audio.
 			TextToSpeechManager.getInstance().say(select + langue);
 			return true;
 
 		}
 
 		return false;
 	}
 
 	/**
 	 * Handle the results from the recognition activity.
 	 */
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (requestCode == VOICE_RECOGNITION_REQUEST_CODE
 				&& resultCode == RESULT_OK) {
 
 			// in this case we scroll to the right page (if we are on quick
 			// sender)
 			flingAndScrollViewer.scrollBy(500, 10);
 
 			// fill the list view with the strings the recognizer thought it
 			// could have heard
 			matches = data
 					.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
 
 			final Vibrator vibrationadd = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
 			// fill the list view with the strings the recognizer thought it
 			// could have heard
 
 			speechrecolayout.removeAllViewsInLayout();
 
 			for (int it = 0; it < 3; it++) {
 
 				Date currentDate = new Date(System.currentTimeMillis());
 				String date = (String) android.text.format.DateFormat.format(
 						"yyyy-MM-dd'T'kk:mm:ss'Z'", currentDate);
 
 				LinearLayout wholelayout = new LinearLayout(this);
 				LinearLayout.LayoutParams layoutParamsWhole = new LinearLayout.LayoutParams(
 						(int) TypedValue.applyDimension(
 								TypedValue.COMPLEX_UNIT_DIP, 310,
 								getResources().getDisplayMetrics()),
 						LinearLayout.LayoutParams.WRAP_CONTENT);
 				layoutParamsWhole.setMargins(0, 5, 0, 5);
 				wholelayout.setLayoutParams(layoutParamsWhole);
 				wholelayout.setOrientation(LinearLayout.HORIZONTAL);
 				// create a new flow layout for each choice
 				final FlowLayout fl = new FlowLayout(this);// bubble
 															// conversation
 				LayoutParams layoutParams = new LayoutParams(
 						(int) TypedValue.applyDimension(
 								TypedValue.COMPLEX_UNIT_DIP, 260,
 								getResources().getDisplayMetrics()),
 						LayoutParams.WRAP_CONTENT);
 				fl.setLayoutParams(layoutParams);
 				fl.setBackgroundResource(R.drawable.bubblelast);
 
 				// add the microphone
 				// ImageView micro = new ImageView(this);
 				// micro.setBackgroundResource(R.drawable.androidmic);
 				// fl.addView(micro);
 				// add the view number
 				ImageView number = new ImageView(this);
 				if (it == 0) {
 					number.setBackgroundResource(R.drawable.one);
 					fl.addView(number);
 
 				} else if (it == 1) {
 					number.setBackgroundResource(R.drawable.two);
 					fl.addView(number);
 
 				} else if (it == 2) {
 					number.setBackgroundResource(R.drawable.three);
 					fl.addView(number);
 
 				}
 
 				String str = matches.get(it).toString();
 				// write the 3 voice recognition results
 
 				StringTokenizer st = new StringTokenizer(str);
 				String[] tabWords = new String[100];
 				int nbWords = 0;
 				while (st.hasMoreElements()) {
 					tabWords[nbWords] = (String) st.nextElement();
 					nbWords++;
 				}
 				// create a button for each words and append it to the bubble
 				// composition
 				for (int i = 0; i < nbWords; ++i) {
 					final Button btn = new Button(this);
 					btn.setText(tabWords[i]);
 					// btn.setTextSize((int)
 					// TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,10,
 					// getResources().getDisplayMetrics()));
 					btn.setLayoutParams(new LayoutParams(
 							LayoutParams.WRAP_CONTENT,
 							LayoutParams.WRAP_CONTENT));
 					btn.setBackgroundResource(R.drawable.button);
 
 					final String toSay = tabWords[i];
 					// play each button
 					btn.setOnClickListener(new OnClickListener() {
 						@Override
 						public void onClick(View v) {
 							// TODO Auto-generated method stub
 							// String sentence = tabWords[i];
 							// btn.setBackgroundColor(Color.RED);
 
 							// plays the audio.
 							TextToSpeechManager.getInstance().say(toSay);
 
 						}
 					});
 					final Vibrator vibrationdelete = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
 					// ici code
 					btn.setOnLongClickListener(new OnLongClickListener() {
 						@Override
 						public boolean onLongClick(View v) {
 
 							final Button bouton = new Button(getBaseContext());
 							bouton.setText(btn.getText());
 							bouton.setLayoutParams(new LayoutParams(
 									LayoutParams.WRAP_CONTENT,
 									LayoutParams.WRAP_CONTENT));
 							bouton.setBackgroundResource(R.drawable.button);
 
 							// play each button
 							bouton.setOnClickListener(new OnClickListener() {
 								@Override
 								public void onClick(View v) {
 
 									// plays the audio.
 									TextToSpeechManager.getInstance()
 											.say(toSay);
 
 								}
 							});
 
 							final Vibrator vibrationdelete = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
 							// on long click, delete the button
 							bouton.setOnLongClickListener(new OnLongClickListener() {
 								@Override
 								public boolean onLongClick(View v) {
 									flowlayout.removeView(bouton);
 
 									vibrationdelete.vibrate(200);
 									return true;
 								}
 							});
 							// add the button to the flow layout
 							flowlayout.addView(bouton, new LayoutParams(
 									LayoutParams.WRAP_CONTENT,
 									LayoutParams.WRAP_CONTENT));
 							vibrationdelete.vibrate(200);
 
 							return true;
 						}
 					});
 
 					// ((MarginLayoutParams) layoutParams).setMargins(50, 80,
 					// 50, 80);
 					fl.addView(btn);
 
 					vibrationadd.vibrate(200);
 				} // end for each word
 
 				wholelayout.addView(fl);
 				speechrecolayout.addView(wholelayout);
 				// play button
 				ImageView playbutton = new ImageView(this);
 				playbutton.setBackgroundResource(R.drawable.playsmsclick);
 				playbutton.setOnCreateContextMenuListener(this);
 				wholelayout.addView(playbutton);
 				playbutton.setOnClickListener(new OnClickListener() {
 					@Override
 					public void onClick(View v) {
 						playKaraoke(fl);
 					}
 
 				});
 				final Vibrator vibrationvoicerecog = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
 
 				number.setOnLongClickListener(new OnLongClickListener() {
 					@Override
 					public boolean onLongClick(View v) {
 						String sentenceChoosen = "";
 						for (int i = 1; i < fl.getChildCount(); ++i) {
 							// get the first word of the results
 							Button btn = (Button) fl.getChildAt(i);
 							// create a new word with the same characteristics
 							final Button bouton = new Button(getBaseContext());
 							bouton.setText(btn.getText());
 							// bouton.setTextSize((int)
 							// TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,10,
 							// getResources().getDisplayMetrics()));
 							bouton.setLayoutParams(new LayoutParams(
 									LayoutParams.WRAP_CONTENT,
 									LayoutParams.WRAP_CONTENT));
 							bouton.setBackgroundResource(R.drawable.button);
 							// add the button to the flow layout
 							flowlayout.addView(bouton, new LayoutParams(
 									LayoutParams.WRAP_CONTENT,
 									LayoutParams.WRAP_CONTENT));
 							sentenceChoosen += btn.getText();
 
 						}
 						vibrationvoicerecog.vibrate(200);
 
 						return true;
 					}
 
 				});
 
 				fl.setOnLongClickListener(new OnLongClickListener() {
 					@Override
 					public boolean onLongClick(View v) {
 						String sentenceChoosen = "";
 						for (int i = 1; i < fl.getChildCount(); ++i) {
 							// get the first word of the results
 							Button btn = (Button) fl.getChildAt(i);
 							// create a new word with the same characteristics
 							final Button bouton = new Button(getBaseContext());
 							bouton.setText(btn.getText());
 							// bouton.setTextSize((int)
 							// TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,10,
 							// getResources().getDisplayMetrics()));
 							bouton.setLayoutParams(new LayoutParams(
 									LayoutParams.WRAP_CONTENT,
 									LayoutParams.WRAP_CONTENT));
 							bouton.setBackgroundResource(R.drawable.button);
 							// add the button to the flow layout
 							flowlayout.addView(bouton, new LayoutParams(
 									LayoutParams.WRAP_CONTENT,
 									LayoutParams.WRAP_CONTENT));
 							sentenceChoosen += btn.getText();
 
 						}
 						vibrationvoicerecog.vibrate(200);
 
 						return true;
 					}
 
 				});
 
 			}// end main for loop
 		}
 
 		else if (requestCode == PICK_CONTACT) {
 			if (resultCode == Activity.RESULT_OK) {
 
 				phoneNo = "";
 				Uri contactData = data.getData();
 				Cursor cur = getContentResolver().query(contactData, null,
 						null, null, null);
 				ContentResolver contect_resolver = getContentResolver();
 				String nameContact = "Nom inconnu";
 				String photoId = null;
 				if (cur.moveToFirst()) {
 					String id = cur.getString(cur
 							.getColumnIndexOrThrow(Phone._ID));
 					// contact name
 					nameContact = cur
 							.getString(cur
 									.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));
 					// photoID
 					long photo = cur
 							.getLong(cur
 									.getColumnIndexOrThrow(ContactsContract.Contacts.PHOTO_ID));
 					String no = "Numro inconnu";
 					// if the contact has a phone number
 					if (Integer
 							.parseInt(cur.getString(cur
 									.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
 						Cursor pCur = getContentResolver()
 								.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
 										null,
 										ContactsContract.CommonDataKinds.Phone.CONTACT_ID
 												+ " = ?", new String[] { id },
 										null);
 						// this second loop will retrieve all the contact
 						// numbers for a particular contact id
 						String mobilePhone = "Inconnu";
 						String homePhone = "Inconnu";
 						String workPhone = "Inconnu";
 						String otherPhone = "Inconnu";
 
 						if (pCur != null) {
 							while (pCur.moveToNext()) {
 								// takes only the MOBILE number
 								if (pCur.getInt(pCur.getColumnIndex(Phone.TYPE)) == Phone.TYPE_MOBILE) {
 									/*
 									 * int phNumber =
 									 * pCur.getColumnIndexOrThrow(
 									 * ContactsContract
 									 * .CommonDataKinds.Phone.NUMBER); no =
 									 * pCur.getString(phNumber);
 									 */
 									switch (pCur.getInt(pCur
 											.getColumnIndex(Phone.TYPE))) {
 									case Phone.TYPE_MOBILE:
 										mobilePhone = pCur.getString(pCur
 												.getColumnIndex(Phone.NUMBER));
 										break;
 									case Phone.TYPE_HOME:
 										homePhone = pCur.getString(pCur
 												.getColumnIndex(Phone.NUMBER));
 										break;
 									case Phone.TYPE_WORK:
 										workPhone = pCur.getString(pCur
 												.getColumnIndex(Phone.NUMBER));
 										break;
 									case Phone.TYPE_OTHER:
 										otherPhone = pCur.getString(pCur
 												.getColumnIndex(Phone.NUMBER));
 										break;
 									}
 								}
 							}
 							if (mobilePhone != "Inconnu") {
 								no = mobilePhone;
 							}
 							/*
 							 * else if (homePhone != "Inconnu") { no =
 							 * homePhone; } else if (workPhone != "Inconnu") {
 							 * no = workPhone; } else if (otherPhone !=
 							 * "Inconnu") { no = otherPhone; }
 							 */
 
 						}
 					}
 
 					photoId = cur.getString(cur
 							.getColumnIndex(Contacts.PHOTO_ID));
 					if (photo != 0) {
 						Cursor photo2 = getContentResolver().query(
 								// column for the blob
 								Data.CONTENT_URI, new String[] { Photo.PHOTO },
 								Data._ID + "=?", // select row by id
 								new String[] { photoId }, // filter by photoId
 								null);
 
 						if (photo2.moveToFirst()) {
 							byte[] photoBlob = photo2.getBlob(photo2
 									.getColumnIndex(Photo.PHOTO));
 							Bitmap photoBitmap = BitmapFactory.decodeByteArray(
 									photoBlob, 0, photoBlob.length);
 							profile.setImageBitmap(photoBitmap);
 						}
 						photo2.close();
 					} else {
 
 						profile.setImageResource(R.drawable.nophotostored);
 					}
 
 					/*
 					 * if (photo != 0) { Uri uri =
 					 * ContentUris.withAppendedId(People.CONTENT_URI, photo);
 					 * Bitmap bitmap = People.loadContactPhoto(getBaseContext(),
 					 * uri, R.drawable.icon, null);
 					 * profile.setImageBitmap(bitmap); }
 					 */
 
 					// reinitialise le textview
 					recipient.setText("");
 					recipientnum.setText("");
 					// append new name selected
 					recipient.append(nameContact);
 					recipientnum.append(no);
 					phoneNo += no;
 
 					id = null;
 					nameContact = null;
 					no = null;
 
 					contect_resolver = null;
 					cur = null;
 
 				}
 			}
 		} else {
 			// plays the audio.
 			TextToSpeechManager.getInstance().say(
 					"Il n'y a pas de connection internete.");
 
 		}
 
 		super.onActivityResult(requestCode, resultCode, data);
 
 	}
 
 	@Override
 	public void onClick(View v) {
 
 	}
 
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 
 		return MenuChoice(item);
 	}
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		// sets the theme used throughout the application.
 		setTheme(EasySmsApp.THEME);
 
 		// checks the bundle to handle correctly the two cases.
 		Bundle bundle = getIntent().getExtras();
 		newMsg = bundle.getBoolean(NEW_MESSAGE_EXTRA);
 
 		if (newMsg) { // if new message don't display the message details page
 			setContentView(R.layout.messagecomposition);
 
 			profile = (ImageView) findViewById(R.id.selectcontact);
 			profile.setOnClickListener(new OnClickListener() {
 
 				@Override
 				public void onClick(View v) {
 					Intent intent = new Intent(Intent.ACTION_PICK,
 							ContactsContract.Contacts.CONTENT_URI);
 					startActivityForResult(intent, PICK_CONTACT);
 
 				}
 
 			});
 			recipient = (TextView) findViewById(R.id.contactname);
 			recipientnum = (TextView) findViewById(R.id.contactnumber);
 
 		} else {
 			// shows and existing thread.
 			setContentView(R.layout.act_view_message);
 
 			// obtains the user info from the extras.
 			nameContact = (String) bundle.get("Name");
 			phoneNumContact = (String) bundle.get("Tel");
 
 			// allows the top bar to be different.
 			ActionBar actionBar = getSupportActionBar();
 			actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
 
 			View view = View.inflate(getApplicationContext(),
 					R.layout.actionbar, null);
 			actionBar.setCustomView(view);
 
 			// sets the data in the action bar.
 			getSupportActionBar().setTitle(nameContact);
 			getSupportActionBar().setSubtitle(phoneNumContact);
 
 			// enables the icon to serve as back.
 			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
 
 			phoneNo = phoneNumContact;
 			msgdetailslayout = (LinearLayout) findViewById(R.id.msgdetailslayout);
 
 			String photoid = getContactPhotoFromNumber(phoneNumContact);
 			// profile = (ImageView) findViewById(R.id.selectcontact);
 			if (photoid == null) {
 				// profile.setBackgroundResource(R.drawable.nophotostored);
 			} else {
 
 				Cursor photo2 = getContentResolver().query(Data.CONTENT_URI,
 						new String[] { Photo.PHOTO }, // column for the blob
 						Data._ID + "=?", // select row by id
 						new String[] { photoid }, // filter by photoId
 						null);
 				Bitmap photoBitmap = null;
 				if (photo2.moveToFirst()) {
 					byte[] photoBlob = photo2.getBlob(photo2
 							.getColumnIndex(Photo.PHOTO));
 					photoBitmap = BitmapFactory.decodeByteArray(photoBlob, 0,
 							photoBlob.length);
 					// profile.setImageBitmap(photoBitmap);
 
 				}
 				photo2.close();
 
 			}
 
 			List<Conversation> listallconv = populateList(smsAllRetrieve());
 			String threadidconv = retrieveThreadIdFromNumberContact(phoneNumContact);
 			// Toast.makeText(getApplicationContext(),threadidconv,
 			// Toast.LENGTH_LONG).show();
 			Conversation conv = retrieveConvFromThreadId(listallconv,
 					threadidconv);
 			createLayoutbubbleconv(conv);
 			// }
 
 		} // end of else
 
 		speechrecolayout = (LinearLayout) findViewById(R.id.elsalayout);
 		LinearLayout wholeLayout = new LinearLayout(this);
 		wholeLayout.setLayoutParams(new LayoutParams((int) TypedValue
 				.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 310,
 						getResources().getDisplayMetrics()),
 				LayoutParams.WRAP_CONTENT));
 		// for the speech to text
 
 		flowlayoutspeechrecog1 = new FlowLayout(this);// bubble conversation
 		flowlayoutspeechrecog1.setLayoutParams(new LayoutParams(
 				(int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
 						260, getResources().getDisplayMetrics()),
 				LayoutParams.WRAP_CONTENT));
 		flowlayoutspeechrecog1.setBackgroundResource(R.drawable.bubblelast);
 		// microphone button
 		speakButton = new ImageView(this);
 		speakButton.setBackgroundResource(R.drawable.ic_action_voice);
 		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
 				LinearLayout.LayoutParams.MATCH_PARENT,
 				LinearLayout.LayoutParams.WRAP_CONTENT);
 
 		layoutParams.setMargins((int) TypedValue.applyDimension(
 				TypedValue.COMPLEX_UNIT_DIP, 2, getResources()
 						.getDisplayMetrics()), (int) TypedValue.applyDimension(
 				TypedValue.COMPLEX_UNIT_DIP, 2, getResources()
 						.getDisplayMetrics()), 0, 0);
 		flowlayoutspeechrecog1.addView(speakButton, layoutParams);
 
 		// play button
 		ImageView helpplay = new ImageView(this);
 		helpplay.setBackgroundResource(R.drawable.playsmsclick);
 		helpplay.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				playKaraoke(flowlayoutspeechrecog1);
 			}
 		});
 
 		final String[] messageHelp = new String[] { "Pour", "enregistrer",
 				"votre", "message", "cliquez", "sur", "le", "microphone", "en",
 				"bas", "", "gauche." };
 
 		for (int j = 0; j < messageHelp.length; ++j) {
 			final Button but = new Button(this);
 			but.setText(messageHelp[j]);
 			but.setBackgroundResource(R.drawable.button);
 			but.setOnClickListener(new OnClickListener() {
 				@Override
 				public void onClick(View v) {
 
 					// plays the audio.
 					TextToSpeechManager.getInstance().say(
 							but.getText().toString());
 
 				}
 
 			});
 			flowlayoutspeechrecog1.addView(but);
 		}
 
 		LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(
 				LinearLayout.LayoutParams.MATCH_PARENT,
 				LinearLayout.LayoutParams.WRAP_CONTENT);
 
 		layoutParams2.setMargins((int) TypedValue.applyDimension(
 				TypedValue.COMPLEX_UNIT_DIP, 2, getResources()
 						.getDisplayMetrics()), (int) TypedValue.applyDimension(
 				TypedValue.COMPLEX_UNIT_DIP, 10, getResources()
 						.getDisplayMetrics()), 0, (int) TypedValue
 				.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources()
 						.getDisplayMetrics()));
 
 		wholeLayout.addView(flowlayoutspeechrecog1);
 		wholeLayout.addView(helpplay);
 		speechrecolayout.addView(wholeLayout, layoutParams2);
 
 		msgbubblelayout = (LinearLayout) findViewById(R.id.bubblelayout);
 
 		// --------------for the snapping scroll--------------------------------
 		flingAndScrollViewer = (FlingAndScrollViewer) findViewById(R.id.flingScrollViewer);
 		flingAndScrollViewer.setInitialPosition(0); // set the initial
 													// position<br />
 
 		// -------------------quick sender-------------------
 		GridView gridview = (GridView) findViewById(R.id.gridview);
 		gridview.setAdapter(new ImageAdapter(this));
 		// conversation bubble
 
 		flowlayout = new FlowLayout(this);// bubble conversation
 		flowlayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
 				LayoutParams.MATCH_PARENT));
 		msgbubblelayout.setBackgroundResource(R.drawable.bubblelast);
 
 		ImageView implaybutton = new ImageView(this);
 		implaybutton.setBackgroundResource(R.drawable.playsmsclick);
 		implaybutton.setOnCreateContextMenuListener(this);
 		implaybutton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				playKaraoke(flowlayout);
 
 			}
 
 		});
 		flowlayout.addView(implaybutton);
 
 		final Vibrator vibrationadd = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
 		gridview.setOnItemClickListener(new OnItemClickListener() {
 			public void onItemClick(AdapterView<?> parent, View v,
 					int position, long id) {
 				if (position < LABELS.length) {
 					String sentence = LABELS[position];
 
 					// plays the audio.
 					TextToSpeechManager.getInstance().say(sentence);
 				}
 			}
 		});
 
 		gridview.setOnItemLongClickListener(new OnItemLongClickListener() {
 			public boolean onItemLongClick(AdapterView<?> parent, View v,
 					int position, long id) {
 				if (position < LABELS.length) {
 
 					// parse the sentence into words and put it into an array of
 					// words
 					StringTokenizer st = new StringTokenizer(LABELS[position]);
 					String[] tabWords = new String[100];
 					int nbWords = 0;
 					while (st.hasMoreElements()) {
 						tabWords[nbWords] = (String) st.nextElement();
 						nbWords++;
 					}
 					// create a button for each words and append it to the
 					// bubble composition
 					for (int i = 0; i < nbWords; ++i) {
 						final Button btn = new Button(v.getContext());
 						btn.setText(tabWords[i]);
 
 						// btn.setTextSize( (int)
 						// TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
 						// 10, getResources().getDisplayMetrics()));
 						btn.setLayoutParams(new LayoutParams(
 								LayoutParams.WRAP_CONTENT,
 								LayoutParams.WRAP_CONTENT));
 						// before being clicked the button is grey
 						// btn.setBackgroundResource(R.drawable.buttonbeforeclicked);
 						btn.setBackgroundResource(R.drawable.button);
 
 						final String toSay = tabWords[i];
 
 						// play each button
 						btn.setOnClickListener(new OnClickListener() {
 							@Override
 							public void onClick(View v) {
 
 								// plays the audio.
 								TextToSpeechManager.getInstance().say(toSay);
 
 							}
 						});
 						final Vibrator vibrationdelete = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
 						// on long click, delete the button
 						btn.setOnLongClickListener(new OnLongClickListener() {
 							@Override
 							public boolean onLongClick(View v) {
 								flowlayout.removeView(btn);
 								vibrationdelete.vibrate(200);
 
 								return true;
 							}
 						});
 
 						if (flowlayout.getChildCount() >= 16) {
 							String sentence = "Message trop long";
 							Toast.makeText(getBaseContext(), sentence,
 									Toast.LENGTH_SHORT).show();
 
 							// plays the audio.
 							TextToSpeechManager.getInstance().say(sentence);
 
 						} else {
 							flowlayout.addView(btn, new LayoutParams(
 									LayoutParams.WRAP_CONTENT,
 									LayoutParams.WRAP_CONTENT));
 						}
 
 					}
 
 					vibrationadd.vibrate(200);
 
 				}
 
 				return true;
 
 			}// end of onitemclick
 		});
 
 		msgbubblelayout.addView(flowlayout, new LayoutParams(
 				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
 
 		// --------------------speech to text-------------------
 		mHandler = new Handler();
 		// Get display items for later interaction
 		// ImageView speakButton = (ImageView) findViewById(R.id.btn_speak);
 
 		mList = (ListView) findViewById(R.id.list);
 
 		// Check to see if a recognition activity is present
 		PackageManager pm = getPackageManager();
 		List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(
 				RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
 		if (activities.size() != 0) {
 			speakButton.setOnClickListener(this);
 		} else {
 			speakButton.setEnabled(false);
 			// speakButton.setText("Recognizer not present");
 		}
 
 		// Most of the applications do not have to handle the voice settings. If
 		// the application does not require a recognition in a specific language
 		// (i.e., different from the system locale), the application does not
 		// need to read the voice settings.
 		// refreshVoiceSettings();
 
 		// for the karaoke
 		handler = new Handler();
 
 	}
 
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View view,
 			ContextMenuInfo menuInfo) {
 		super.onCreateContextMenu((ContextMenu) menu, view, menuInfo);
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 
 		MenuInflater inflater = getSupportMenuInflater();
 		inflater.inflate(R.menu.menu_view_message, menu);
 
 		// RelativeLayout relativeLayout = (RelativeLayout) menu.findItem(
 		// R.id.layout_item).getActionView();
 		//
 		// View inflatedView = getLayoutInflater().inflate(
 		// R.layout.media_bottombar, null);
 		//
 		// relativeLayout.addView(inflatedView);
 		//
 		// return true;
 
 		return true;
 	}
 
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 		if (mMediaPlayer != null) {
 			mMediaPlayer.release();
 			mMediaPlayer = null;
 		}
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(
 			com.actionbarsherlock.view.MenuItem item) {
 
 		switch (item.getItemId()) {
 		case android.R.id.home:
 
 			// goes back to the home upon the back button click.
 			Intent intent = new Intent();
 			intent.setClass(this, InboxActivity.class);
 			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 			startActivity(intent);
 
 			return true;
 
 		case R.id.menu_send:
 
 			if (phoneNo.length() > 0 && flowlayout.getChildCount() > 1) {
 				// retrieve SMS body
 				String message = "";
 				for (int i = 1; i < flowlayout.getChildCount(); ++i) {
 					// message += flowlayout.getChildAt(i).getText();
 					Button btn = (Button) flowlayout.getChildAt(i);
 					message += btn.getText();
 					message += " ";
 				}
 				// send SMS
 				sendSMS(phoneNo, message);
 				// insert SMS sent into DB
 				final Calendar c = Calendar.getInstance();
 				String dateToday = (String) android.text.format.DateFormat
 						.format("yyyy-MM-dd kk:mm", c.getTime());
 				String threadid = retrieveThreadIdFromNumberContact(phoneNo);
 				// right after the msg is sent, navigate to the message
 				// details page
 				Intent i = new Intent(MessagingActivity.this,
 						MessagingActivity.class);
 				String name = getContactNameFromNumber(phoneNo);
 				Bundle bundle = new Bundle();
 				// Add the parameters to bundle
 				bundle.putString("Name", name);
 				bundle.putString("Tel", phoneNo);
 				bundle.putBoolean("NewMsg", false);
 				// Add this bundle to the intent
 				i.putExtras(bundle);
 				startActivity(i);
 				// insert SMS in the data base content provider
 				ContentValues values = new ContentValues();
 				values.put("address", phoneNo);
 				// values.put("date",dateToday);
 				values.put("read", 1);
 				values.put("thread_id", threadid);
 				values.put("body", message);
 				getContentResolver().insert(Uri.parse("content://sms/sent"),
 						values);
 
 			}
 
 			else if (flowlayout.getChildCount() <= 1) {
 				Toast.makeText(getBaseContext(),
 						"Entrez un message s'il vous plait.",
 						Toast.LENGTH_SHORT).show();
 
 				// plays the audio.
 				TextToSpeechManager.getInstance().say(
 						"Entrez un message s'il vous plait.");
 
 			} else if (phoneNo.length() > 0) {
 				Toast.makeText(getBaseContext(),
 						"Entrez un numro s'il vous plait.", Toast.LENGTH_SHORT)
 						.show();
 
 				// plays the audio.
 				TextToSpeechManager.getInstance().say(
 						"Entrez un message s'il vous plait.");
 
 			}
 			return true;
 		case R.id.menu_call:
 			return true;
 		case R.id.menu_delete:
 			// Do something long
 			Runnable runnable = new Runnable() {
 				@Override
 				public void run() {
 					for (int i = 1; i < flowlayout.getChildCount(); ++i) {
 						final Button btn = (Button) flowlayout.getChildAt(i);
 
 						try {
 							Thread.sleep(800);
 						} catch (InterruptedException e) {
 							e.printStackTrace();
 						}
 						handler.post(new Runnable() {
 							@Override
 							public void run() {
 								flowlayout.removeView(btn);
 							}
 						});
 					}
 				}
 			};
 			new Thread(runnable).start();
 
 			return true;
 		case R.id.menu_voice:
 
 			if (isInternetOn()) {
 				// Do something long
 				Runnable runnable2 = new Runnable() {
 					@Override
 					public void run() {
 
 						// plays the audio.
 						TextToSpeechManager.getInstance().say(
 								"Parlez maintenant.");
 
 						// helpVoiceRecog();
 						try {
 							Thread.sleep(800);
 						} catch (InterruptedException e) {
 							e.printStackTrace();
 						}
 						handler.post(new Runnable() {
 							@Override
 							public void run() {
 
 								startVoiceRecognitionActivity();
 
 							}
 						});
 
 					}
 				};
 				new Thread(runnable2).start();
 			}
 
 			return true;
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 	public void onUtteranceCompleted(String uttId) {
 		if (uttId == "end of wakeup message ID") {
 			// playAnnoyingMusic();
 
 			// Toast.makeText(getApplicationContext(), "onutterance completed",
 			// Toast.LENGTH_LONG);
 		}
 	}
 
 	private void playAudio() {
 
 		try {
 
 			mMediaPlayer = MediaPlayer.create(this, R.raw.beep);
 			mMediaPlayer.setLooping(false);
 			Log.e("beep", "started0");
 			mMediaPlayer.start();
 			mMediaPlayer.setOnCompletionListener(new OnCompletionListener() {
 				public void onCompletion(MediaPlayer arg0) {
 					finish();
 				}
 			});
 		} catch (Exception e) {
 			Log.e("beep", "error: " + e.getMessage(), e);
 		}
 	}
 
 	public void playKaraoke(final FlowLayout fl) {
 		timesKaraoke++;
 
 		if (timesKaraoke <= 1) {
 
 			// Do something long
 			Runnable runnable = new Runnable() {
 				@Override
 				public void run() {
 
 					for (int i = 1; i < fl.getChildCount(); ++i) {
 						final Button btn = (Button) fl.getChildAt(i);
 						// wholesentenceplayed += btn.getText();
 						btn.setFocusableInTouchMode(true);
 						try {
 							Thread.sleep(1000);
 						} catch (InterruptedException e) {
 							e.printStackTrace();
 						}
 						handler.post(new Runnable() {
 							@Override
 							public void run() {
 								// progress.setProgress(value);
 								btn.requestFocus();
 								// drop all pending entries in the playback
 								// queue.
 
 								// plays the audio.
 								TextToSpeechManager.getInstance().say(
 										(String) btn.getText());
 							}
 						});
 
 					}
 					timesKaraoke = 0;
 
 				}
 			};
 			new Thread(runnable).start();
 
 			String wholesentenceplayed = "";
 			for (int i = 1; i < fl.getChildCount(); ++i) {
 				final Button btn = (Button) fl.getChildAt(i);
 				wholesentenceplayed += btn.getText();
 			}
 		}
 	}
 
 	private List<Conversation> populateList(List<Sms> allSMS) {
 
 		// list with all the conversations
 		List<Conversation> allconversations = new ArrayList<Conversation>();
 		for (Sms smsnew : allSMS) {
 			boolean add = false;
 			for (Conversation conv : allconversations) {
 				if (conv.threadid.equals(smsnew.threadid)) {
 					conv.listsms.add(smsnew);
 					add = true;
 				}
 
 			}
 			if (add == false) { // we create a new conversation
 				Conversation newconv = new Conversation();
 				List<Sms> newlist = new ArrayList<Sms>();
 				newlist.add(smsnew);
 				newconv.listsms = newlist;
 				newconv.threadid = smsnew.threadid;
 				allconversations.add(newconv);
 			}
 		}
 		return allconversations;
 	}
 
 	private void refreshVoiceSettings() {
 		Log.i("MessageActivity", "Sending broadcast");
 		sendOrderedBroadcast(RecognizerIntent.getVoiceDetailsIntent(this),
 				null, new SupportedLanguageBroadcastReceiver(), null,
 				Activity.RESULT_OK, null, null);
 
 	}
 
 	public Conversation retrieveConvFromThreadId(List<Conversation> allConv,
 			String threadid) {
 		for (Conversation conv : allConv) {
 			String convthreadid = conv.threadid;
 			if (convthreadid.equals(threadid)) {
 				return conv;
 			}
 		}
 
 		return null;
 	}
 
 	public String retrieveThreadIdFromNumberContact(String phoneNumContact) {
 		for (Sms sms : smsAllRetrieve()) {
 			String smscontact = sms.contact;
			// TODO: could it really be null?
			if (smscontact != null && smscontact.equals(phoneNumContact))
 				return sms.threadid;
 		}
 		return "error";
 	}
 
 	// ------------------sends a SMS message to another device---
 	private void sendSMS(String phoneNumber, String message) {
 
 		String SENT = "SMS_SENT";
 		String DELIVERED = "SMS_DELIVERED";
 
 		PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, new Intent(
 				SENT), 0);
 
 		PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0,
 				new Intent(DELIVERED), 0);
 
 		// ---when the SMS has been sent---
 		registerReceiver(new BroadcastReceiver() {
 			@Override
 			public void onReceive(Context arg0, Intent arg1) {
 				switch (getResultCode()) {
 				case Activity.RESULT_OK: // message sent
 
 					// vocal feedback when message sent
 					String sentence = "Message envoy";
 
 					// plays the audio.
 					TextToSpeechManager.getInstance().say(sentence);
 
 					break;
 				case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
 					Toast.makeText(getBaseContext(),
 							"Erreur d'envoi du message", Toast.LENGTH_SHORT)
 							.show();
 
 					break;
 				case SmsManager.RESULT_ERROR_NO_SERVICE:
 					Toast.makeText(getBaseContext(), "Erreur, pas de  service",
 							Toast.LENGTH_SHORT).show();
 					break;
 				case SmsManager.RESULT_ERROR_NULL_PDU:
 					Toast.makeText(getBaseContext(), "Null PDU",
 							Toast.LENGTH_SHORT).show();
 					break;
 				case SmsManager.RESULT_ERROR_RADIO_OFF:
 					Toast.makeText(getBaseContext(), "Radio off",
 							Toast.LENGTH_SHORT).show();
 					break;
 				}
 			}
 		}, new IntentFilter(SENT));
 
 		// ---when the SMS has been delivered---
 		registerReceiver(new BroadcastReceiver() {
 			@Override
 			public void onReceive(Context arg0, Intent arg1) {
 				switch (getResultCode()) {
 				case Activity.RESULT_OK:
 					break;
 				case Activity.RESULT_CANCELED:
 					break;
 				}
 			}
 		}, new IntentFilter(DELIVERED));
 
 		SmsManager sms = SmsManager.getDefault();
 		sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);
 	}
 
 	// for the page navigation
 	public void setOb(InboxActivity obA) {
 		this.ob = obA;
 	}
 
 	// return a list with all the SMS and for each sms a status sent: yes or no
 	public List<Sms> smsAllRetrieve() {
 
 		// we put all the SMS sent and received in a list
 		List<Sms> allSMSlocal = new ArrayList<Sms>();
 		Uri uriSMSURIinbox = Uri.parse("content://sms/");
 		Cursor curinbox = getContentResolver().query(uriSMSURIinbox, null,
 				null, null, null);
 		if (curinbox.moveToFirst()) {
			
			DatabaseUtils.dumpCurrentRow(curinbox);
 			long datesms = 0;
 			String phoneNumber = null;
 			String body = null;
 			String threadid;
 
 			int type = -1;
 			int read = -1;
 			// return -1 if the column does not exist.
 			int dateColumn = curinbox.getColumnIndex("date");
 			int numberColumn = curinbox.getColumnIndex("address");
 			int bodyColumn = curinbox.getColumnIndex("body");
 			int threadColumn = curinbox.getColumnIndex("thread_id");
 			int typeColumn = curinbox.getColumnIndex("type");
 			int typeRead = curinbox.getColumnIndex("read");
 			do {
 				if (dateColumn != -1) {
 					datesms = curinbox.getLong(dateColumn);
 
 				}
 				if (typeRead != -1)
 					read = curinbox.getInt(typeRead);
 				if (typeColumn != -1)
 					type = curinbox.getInt(typeColumn);
 				if (bodyColumn != -1)
 					body = curinbox.getString(bodyColumn);
 				if (numberColumn != -1)
 					phoneNumber = curinbox.getString(numberColumn);
 				if (threadColumn != -1)
 					threadid = curinbox.getString(threadColumn);
 				else
 					threadid = "nada";
 
 				// we create a new SMS
 				Sms smsnew = new Sms(threadid, new Date(datesms), phoneNumber,
 						body);
 
 				// to know if it is a message sent or received
 				if (type == 2) { // SENT
 					smsnew.isSent = true;
 				} else if (type == 1) { // INBOX
 					smsnew.isSent = false;
 				}
 
 				smsnew.isRead = read == 1;
 
 				// we add this SMS to the list of all the SMS
 				allSMSlocal.add(smsnew);
 
 			} while (curinbox.moveToNext());
 
 		}
 
 		return allSMSlocal;
 
 	}
 
 	/**
 	 * Fire an intent to start the speech recognition activity.
 	 */
 	private void startVoiceRecognitionActivity() {
 		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
 
 		// Specify the calling package to identify your application
 		intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass()
 				.getPackage().getName());
 
 		// Display an hint to the user about what he should say.
 		// intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
 		// "Speech recognition demo");
 
 		// Given an hint to the recognizer about what the user is going to say
 		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
 				RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
 
 		// Specify how many results you want to receive. The results will be
 		// sorted
 		// where the first result is the one with higher confidence.
 		// intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);
 		intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
 
 		// Specify the recognition language. This parameter has to be specified
 		// only if the
 		// recognition has to be done in a specific language and not the default
 		// one (i.e., the
 		// system locale). Most of the applications do not have to set this
 		// parameter.
 		/*
 		 * if
 		 * (!mSupportedLanguageView.getSelectedItem().toString().equals("Default"
 		 * )) { intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,
 		 * mSupportedLanguageView.getSelectedItem().toString()); }
 		 */
 
 		// intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,language);
 		// intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,Locale.FRENCH);
 		if (language == Locale.FRENCH)
 			intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "fr-FR");
 		else if (language == Locale.ITALIAN)
 			intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "it-IT");
 		else if (language == Locale.ENGLISH)
 			intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-EN");
 
 		startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
 	}
 }
