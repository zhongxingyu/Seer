 package de.geotweeter.activities;
 
 import java.io.File;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.scribe.model.Token;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnCancelListener;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.content.pm.ResolveInfo;
 import android.database.Cursor;
 import android.graphics.Color;
 import android.graphics.drawable.GradientDrawable;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.media.MediaScannerConnection;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.IBinder;
 import android.os.Parcelable;
 import android.provider.MediaStore;
 import android.text.Editable;
 import android.text.Selection;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.util.Pair;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnKeyListener;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.Button;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.GridView;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.ToggleButton;
 import de.geotweeter.Account;
 import de.geotweeter.Constants;
 import de.geotweeter.Conversation;
 import de.geotweeter.Geotweeter;
 import de.geotweeter.ImageBaseAdapter;
 import de.geotweeter.R;
 import de.geotweeter.SendableTweet;
 import de.geotweeter.TimelineElementAdapter;
 import de.geotweeter.User;
 import de.geotweeter.Utils;
 import de.geotweeter.apiconn.TwitpicApiAccess;
 import de.geotweeter.services.TweetSendService;
 import de.geotweeter.timelineelements.DirectMessage;
 import de.geotweeter.timelineelements.TimelineElement;
 import de.geotweeter.timelineelements.Tweet;
 import de.geotweeter.timelineelements.UserMention;
 import de.geotweeter.widgets.ProtectedPlaceholderEditText;
 
 /**
  * Interface to create a new tweet 
  */
 public class NewTweetActivity extends Activity {
 	private static final String LOG = "NewTweetActivity";
 	private static final int PICTURE_REQUEST_CODE = 123;
 	protected LocationManager lm = null;
 	protected Location location = null;
 	protected GPSCoordsListener gpslistener = null;
 	private long reply_to_id;
 //	private String picturePath;
 	private ImageBaseAdapter imageAdapter;
 	private String dmRecipient = null;
 	
 	private Account currentAccount;
 	private HashMap<View, Account> viewToAccounts;
 	
 	private TweetSendService service;
 	boolean isServiceBound = false;
 	private ImageButton btnImageManager;
 	private ProtectedPlaceholderEditText editTweetText;
 	private boolean useTwitpic;
 	private Uri cameraFileUri;
 	
 	public void onCreate(Bundle savedInstanceState) {
 		useTwitpic = getSharedPreferences(Constants.PREFS_APP, 0).getString("pref_image_hoster", "twitter").equals("twitpic");
 		
 		Utils.setDesign(this);
 		super.onCreate(savedInstanceState);
 		serviceBind();
 		setContentView(R.layout.new_tweet);
 		
 		editTweetText = ((ProtectedPlaceholderEditText)findViewById(R.id.tweet_text));
 		editTweetText.setPlaceholder(Pattern.compile("http://twitpic\\.com/pic(\\d{3})"));
 		
 		editTweetText.addTextChangedListener(new TextChangedListener(this));
 		if (useTwitpic) {
 			
 			/* Diese Funktionen werden benötigt, um Modifikationen von Twitpic-Platzhaltern
 			 * durch den Benutzer zu unterbinden. Ist Twitter selbst als Bilderdienst eingestellt,
 			 * muss hier gar nichts überwacht werden und wir können uns die Funktionen zur
 			 * Laufzeit komplett sparen */ 
 		
 			editTweetText.setPlaceholder(Pattern.compile("http://twitpic\\.com/pic(\\d{3})"));
 			
 			editTweetText.setOnKeyListener(new OnKeyListener() {
 				
 				@Override
 				public boolean onKey(View v, int keyCode, KeyEvent event) {
 					return tweetTextKeyListener((ProtectedPlaceholderEditText) v, keyCode, event);
 				}
 			});
 			
 		}
 		
 		ToggleButton gpsToggle = (ToggleButton)findViewById(R.id.btnGeo);
 		gpsToggle.setOnCheckedChangeListener(new GPSToggleListener());
 		SharedPreferences prefs = getSharedPreferences(Constants.PREFS_APP, 0);
 		gpsToggle.setChecked(prefs.getBoolean("gpsEnabledByDefault", false));
 		((Button)findViewById(R.id.btnSend)).setOnClickListener(new SendTweetListener());
 		
 		Intent i = getIntent();
 		
 		if (i != null && i.getExtras() != null) {
 			
 			TimelineElement elm = (TimelineElement) i.getExtras().getSerializable("de.geotweeter.reply_to_tweet");
 			Pair<TimelineElement, String> pair_to_delete = null;
 			for (Pair<TimelineElement, String> pair : ((Geotweeter) getApplication()).notifiedElements) {
 				if (pair.first.getClass() == elm.getClass() && pair.first.getID() == elm.getID()) {
 					pair_to_delete = pair;
 					break;
 				}
 			}
 			
 			if (pair_to_delete != null) {
 				((Geotweeter) getApplication()).notifiedElements.remove(pair_to_delete);
 				((Geotweeter) getApplication()).updateNotification(false);
 			}
 			
 			if (elm instanceof DirectMessage) {
 				
 				if (TimelineActivity.current_account == null) {
 					DirectMessage dm = (DirectMessage)elm;
 					List<User> auth_users = getAuthUsers();
 					if (auth_users != null) {
 						for (User u : auth_users) {
 							Account acct = createAccount(u);
 							if (dm.recipient.id == acct.getUser().id) {
 								TimelineActivity.current_account = acct;
 							}
 						}
 					} else {
 						throw new NullPointerException("auth_users is null");
 					}
 				
 				}
				editTweetText.setText("d " + elm.getSenderScreenName() + " ");
 				
 			} else if (elm instanceof Tweet) {
 				reply_to_id = elm.getID();
 				if (TimelineActivity.current_account == null) {
 					Tweet tweet = (Tweet)elm;
 					if (tweet.entities.user_mentions != null) {
 						List<User> auth_users = getAuthUsers();
 						if (auth_users != null) {
 							for (User u : auth_users) {
 								Account acct = createAccount(u);
 								for (UserMention um : tweet.entities.user_mentions) {
 									if (um.id == acct.getUser().id) {
 										TimelineActivity.current_account = acct;
 										break;
 									}
 								}
 							}
 						} else {
 							throw new NullPointerException("auth_users is null");
 						}
 					}
 				}
 				if (TimelineActivity.current_account == null) {
 					throw new NullPointerException("There's something rotten in the state of current_account");
 				}
				
				String reply_string = "@" + elm.getSenderScreenName() + " ";
				int replyStringSelectionStart = reply_string.length();
 				for (UserMention userMention : ((Tweet) elm).entities.user_mentions) {
 					if (    ! (userMention.screen_name.equalsIgnoreCase(TimelineActivity.current_account.getUser().getScreenName())
 							|| userMention.screen_name.equalsIgnoreCase(elm.getSenderScreenName())) ) {
 						reply_string += "@" + userMention.screen_name + " ";
 					}
 				}
				editTweetText.setText(reply_string);
				editTweetText.setSelection(replyStringSelectionStart, reply_string.length());
//				editTweetText.setSelection(reply_string.length());
 			}
 			
 			ListView l = (ListView) findViewById(R.id.timeline);
 			TimelineElementAdapter tea = new TimelineElementAdapter(this, R.layout.timeline_element, 
 																    new ArrayList<TimelineElement>());
 			tea.add(elm);
 			if (elm.getClass() != DirectMessage.class || TimelineActivity.getInstance() != null) {
 				new Conversation(tea, TimelineActivity.current_account, true, false);
 			}
 			l.setAdapter(tea);
 		}
 		
 		/* "Keine DM"-Button */
 		findViewById(R.id.btnNoDM).setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				int oldSelection = editTweetText.getSelectionStart();
 				editTweetText.setText("@" + dmRecipient + " " + editTweetText.getText().toString());
 				editTweetText.setSelection(oldSelection + (dmRecipient.length() + 2));
 				dmRecipient = null;
 				findViewById(R.id.btnNoDM).setVisibility(View.GONE);
 				setTitle(Utils.getString(R.string.new_tweet_activity_title));
 			}
 		});
 		
 		/* Accountauswahl */
 		List<Account> accounts = Account.all_accounts;
 		LinearLayout lin = (LinearLayout) findViewById(R.id.linLayAccounts);
 		
 		currentAccount = TimelineActivity.current_account;
 
 		int size = Utils.convertDipToPixel(40);
 		int padding = Utils.convertDipToPixel(4);
 		viewToAccounts = new HashMap<View, Account>();
 		for (Account account : accounts) {
 			User user = account.getUser();
 			ImageButton img = new ImageButton(this);
 			img.setLayoutParams(new LayoutParams(size, size));
 			img.setScaleType(ImageView.ScaleType.CENTER_CROP);
 			TimelineActivity.getBackgroundImageLoader(getApplicationContext()).displayImage(user.getAvatarSource(), img, true);
 			img.setPadding(padding, padding, padding, padding);
 			changeLayoutOfAccountButton(img, currentAccount == account);
 			img.setOnClickListener(new AccountChangerListener());
 			lin.addView(img);
 			viewToAccounts.put(img, account);
 		}
 		
 		imageAdapter = new ImageBaseAdapter(this);
 		btnImageManager = (ImageButton) findViewById(R.id.btnImageManager);
 		
 	}
 	
 	/**
 	 * Prevents modification of placeholder URLs with by selecting them completely
 	 * 
 	 * @param v The EditText
 	 * @param keyCode The key which is pressed
 	 * @param event The event which triggered the event
 	 * @return true if the event was handled
 	 */
 	protected boolean tweetTextKeyListener(ProtectedPlaceholderEditText v, int keyCode, KeyEvent event) {
 		if (event.getAction() == KeyEvent.ACTION_DOWN) {
 			if (v.isPlaceholderSelected()) {
 				v.setDpadAction(keyCode >= KeyEvent.KEYCODE_DPAD_UP && keyCode <= KeyEvent.KEYCODE_DPAD_RIGHT);
 				if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
 					if (!Selection.moveUp(v.getText(), v.getLayout())) {
 						v.setSelection(v.getSelectionStart());
 					}
 					v.setPlaceholderSelected(false);
 					return true;
 				}
 				if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
 					if (!Selection.moveDown(v.getText(), v.getLayout())) {
 						v.setSelection(v.getSelectionEnd());
 					}
 					v.setPlaceholderSelected(false);
 					return true;
 				}
 				if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
 					if (Selection.moveLeft(v.getText(), v.getLayout())) {
 						Selection.moveLeft(v.getText(), v.getLayout());
 					} else {
 						v.setSelection(0);
 					}
 					v.setPlaceholderSelected(false);
 					return true;
 				}
 				if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
 					if (Selection.moveRight(v.getText(), v.getLayout())) {
 						Selection.moveRight(v.getText(), v.getLayout());
 					} else {
 						v.setSelection(v.getText().length());
 					}
 					v.setPlaceholderSelected(false);
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	protected void onPause() {
 		super.onPause();
 		/* Remove all GPSListeners. */
 		if (gpslistener != null && lm != null) {
 			lm.removeUpdates(gpslistener);
 		}
 	}
 	
 	protected class AccountChangerListener implements OnClickListener {
 		public void onClick(View v) {
 			Account acc = viewToAccounts.get(v);
 			if(acc != currentAccount) {
 				/* TODO: Hole oldView auf anderem Weg. Map, die in 2 Richtungen funktioniert */
 				ImageButton oldView = (ImageButton) getViewFromAccount(currentAccount);
 				changeLayoutOfAccountButton(oldView, false);
 				changeLayoutOfAccountButton((ImageButton) v, true);
 				currentAccount = acc;
 			}
 		}
 	}
 	
 	@SuppressWarnings("deprecation")
 	private void changeLayoutOfAccountButton(ImageView v, boolean chosen) {
 		int bgColor = Color.LTGRAY;
 		int highlightColor = 0xFF000000;
 		if (chosen) {
 			v.setAlpha(Constants.CHECKED_ALPHA_VALUE);
 			GradientDrawable gradDraw = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, 
 															 new int[] {highlightColor, bgColor});
 			gradDraw.setGradientType(GradientDrawable.RADIAL_GRADIENT);
 			gradDraw.setGradientRadius(Utils.convertDipToPixel(30));
 			v.setBackgroundDrawable(gradDraw);
 		} else {
 			v.setAlpha(Constants.UNCHECKED_ALPHA_VALUE);
 			v.setBackgroundColor(bgColor);
 		}
 	}
 	
 	private View getViewFromAccount(Account acc) {
 		for (View v : viewToAccounts.keySet()) {
 			if(viewToAccounts.get(v).equals(acc)) {
 				return v;
 			}
 		}
 		return null;
 	}
 	
 	public void addImageHandler(View v) {
 		// Gallery and Filesystem
 		Intent galleryIntent = new Intent();
 		galleryIntent.setType("image/*");
 		galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
 		galleryIntent.addCategory(Intent.CATEGORY_OPENABLE);
 		
 		// Camera
 		List<Intent> cameraIntents = new ArrayList<Intent>();
 		Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
 		cameraFileUri = getOutputMediaFileUri();
 		captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraFileUri);
 		List<ResolveInfo> listCam = getPackageManager().queryIntentActivities(captureIntent, 0);
 		for(ResolveInfo res : listCam) {
 			final String packageName = res.activityInfo.packageName;
 			final Intent intent = new Intent(captureIntent);
 			intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
 			intent.setPackage(packageName);
 			cameraIntents.add(intent);
 		}
 
 		Intent chooserIntent = Intent.createChooser(galleryIntent, "Select");
 		chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(new Parcelable[] {}));
 		startActivityForResult(chooserIntent, PICTURE_REQUEST_CODE);
 	}
 	
 	protected void onActivityResult(int request_code, int result_code, Intent data) {
 		if (request_code == PICTURE_REQUEST_CODE && result_code == Activity.RESULT_OK) {
 			String picturePath;
 			if(data == null || data.getData() == null) {
 				picturePath = cameraFileUri.getPath();
 				MediaScannerConnection.scanFile(this,
 						new String[] { picturePath }, null,
 						new MediaScannerConnection.OnScanCompletedListener() {
 					public void onScanCompleted(String path, Uri uri) {
 						Log.i("ExternalStorage", "Scanned " + path + ":");
 						Log.i("ExternalStorage", "-> uri=" + uri);
 					}
 				});
 			} else {
 				Cursor cursor = getContentResolver().query(data.getData(), new String[] {MediaStore.Images.Media.DATA}, null, null, null);
 				cursor.moveToFirst();
 				picturePath = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
 				cursor.close();
 			}
 			
 			String image_hoster = getSharedPreferences(Constants.PREFS_APP, 0).getString("pref_image_hoster", "twitter"); 
 			if (image_hoster.equals("twitter")) {
 				// TODO Warnung, dass Bild ausgetauscht wird.
 				imageAdapter.clear();
 			}
 			
 			int image_index = imageAdapter.add(picturePath);
 			if (image_index == -1) {
 				Toast.makeText(this, R.string.too_much_images, Toast.LENGTH_SHORT).show();
 			} else {
 				Log.d(LOG, picturePath + ": " + new File(picturePath).length());
 
 				if (image_hoster.equals("twitpic")) {
 					String editText = editTweetText.getText().toString();
 					Log.d(LOG, "String: " + editText + " Length: " + editText.length());
 					String prefix = " ";
 					if (editText.length() == 0 || editText.matches(".*\\s")) {
 						prefix = "";
 					}
 					editTweetText.getText().insert(editTweetText.getSelectionStart(), prefix + TwitpicApiAccess.getPlaceholder(image_index) + " ");
 //					editTweetText.append(prefix + TwitpicApiAccess.getPlaceholder(imageIndex) + " ");
 				}
 
 				if (imageAdapter.getCount() > 1) {
 					btnImageManager.setImageResource(R.drawable.pictures);
 				} else {
 					btnImageManager.setImageResource(R.drawable.picture);
 				}
 				btnImageManager.setVisibility(ImageView.VISIBLE);
 			}
 		}
 	}
 	
 	private static Uri getOutputMediaFileUri(){
 		return Uri.fromFile(getOutputMediaFile());
 	}
 
 	/**
 	 * Create a File for saving an image or video 
 	 * 
 	 * @return The created file
 	 */
 	private static File getOutputMediaFile(){
 //		File mediaStorageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
 		File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),"Camera");
 		
 		if (! mediaStorageDir.exists()){
 			if (! mediaStorageDir.mkdirs()){
 				Log.d("MyCameraApp", "failed to create directory");
 				return null;
 			}
 		}
 		
 		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
 		File mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
 		
 		return mediaFile;
 	}
 	
 	public void imageManagerHandler(View v) {
 //		ImageView img = new ImageView(this);
 //		img.setImageBitmap(Utils.resizeBitmap(picturePath, 150));
 		LayoutInflater vi = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		GridView gridView = (GridView) vi.inflate(R.layout.image_gridview, null);
 		gridView.setAdapter(imageAdapter);
 		gridView.setOnItemClickListener(new OnItemClickListener() {
 			
 			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
 				View cross = v.findViewById(R.id.cross);
 				if (cross.getVisibility() == View.VISIBLE) {
 					cross.setVisibility(View.INVISIBLE);
 					imageAdapter.unmarkForDelete(position);
 				} else {
 					cross.setVisibility(View.VISIBLE);
 					imageAdapter.markForDelete(position);
 				}
 			}
 			
 		});
 		
 		new AlertDialog.Builder(this)
 		               .setTitle(R.string.image_gridview_title)
 		               .setView(gridView)
 		               .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
 		            	   @Override
 		            	   public void onClick(DialogInterface dialog, int which) {
 		            		   dialog.cancel();
 		            	   }
 		               })
 		               .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
 		            	   @Override
 		            	   public void onClick(DialogInterface dialog, int which) {
 		            		   editTweetText.setText(imageAdapter.deleteAllMarkedPlaceholder(editTweetText.getText().toString()));
 		            		   imageAdapter.deleteMarked();
 		            		   if (imageAdapter.getCount() == 1) {
 		            			   btnImageManager.setImageResource(R.drawable.picture);
 		           			   } else if (imageAdapter.getCount() == 0){
 		           				   btnImageManager.setVisibility(View.GONE);
 		           			   }
 		            	   }
 		               })
 		               .setOnCancelListener(new OnCancelListener() {
 		            	   @Override
 		            	   public void onCancel(DialogInterface dialog) {
 		            		   imageAdapter.unmarkAll();
 		            	   }
 		               })
 		               .show();
 	}
 	
 	protected class TextChangedListener implements TextWatcher {
 		
 		private Activity activity;
 		private boolean delete;
 		private String text;
 		private int start;
 		private Pattern findDMPattern = Pattern.compile("^d @?([a-z0-9_]+)\\s(.*)$", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
 		
 		public TextChangedListener(Activity a) { 
 			activity = a;
 			delete = false;
 		}
 		
 		public void onTextChanged(CharSequence s, int start, int before, int count) {}
 		
 		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
 //			Log.d(LOG, s + " start: " + start + " count: " + count + " after: " + after);
 			Pattern p = Pattern.compile("http://twitpic\\.com/pic(\\d{3})");
 			Matcher matcher = p.matcher(s);
 			while (matcher.find()) {
 //				Log.d(LOG, "start=" + matcher.start() + " end=" + matcher.end() + " " + (matcher.start() <= start && start < matcher.end()) + " Image: " + matcher.group());
 				if (!delete && start < matcher.end()) {
 					boolean insertion = after > count;
 					if ((insertion && matcher.start() < start) || (!insertion && matcher.start() <= start)) {
 						text = s.toString().replace(matcher.group(), "");
 						this.start = matcher.start();
 						delete = true;
 						imageAdapter.deleteIndex(Integer.parseInt(matcher.group(1)));
 						if (imageAdapter.getCount() == 1) {
 							btnImageManager.setImageResource(R.drawable.picture);
 						} else if (imageAdapter.getCount() == 0){
 							btnImageManager.setVisibility(View.GONE);
 						}
 					}
 				}
 			}
 		}
 		
 		public void afterTextChanged(Editable s) {
 			if (delete) {
 				delete = false;
 				editTweetText.setText(text);
 				editTweetText.setSelection(start);
 			} else {
 				Matcher dmMatcher = findDMPattern.matcher(s.toString());
 				if (dmMatcher.find()) {
 					int oldSelectionStart = editTweetText.getSelectionStart();
 					int oldLength = s.toString().length();
 					activity.setTitle(Utils.formatString(R.string.new_tweet_activity_title_sending_dm, dmMatcher.group(1)));
 					dmRecipient = dmMatcher.group(1);
 					s.replace(0, s.length(), dmMatcher.group(2));
 					editTweetText.setText(dmMatcher.group(2));
 					int newLength = s.toString().length();
 					int newSelectionStart = oldSelectionStart - oldLength + newLength;
 					if (newSelectionStart < 0) newSelectionStart = 0;
 					if (newSelectionStart > s.length()) newSelectionStart = s.length();
 					editTweetText.setSelection(newSelectionStart);
 					activity.findViewById(R.id.btnNoDM).setVisibility(View.VISIBLE);
 				}
 				
 				TextView t = (TextView) activity.findViewById(R.id.textCharsRemaining);
 				int remaining = 140 - Utils.countChars(s.toString());
 				t.setText(String.valueOf(remaining));
 				if (remaining < 0) {
 					t.setTextColor(0xFFFF0000);
 				} else {
 					t.setTextColor(0xFF00CD00);
 				}
 			}
 		}
 	}
 	
 	protected class GPSToggleListener implements OnCheckedChangeListener {
 		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
 			SharedPreferences prefs = getSharedPreferences(Constants.PREFS_APP, 0);
 			Editor ed = prefs.edit();
 			ed.putBoolean("gpsEnabledByDefault", isChecked);
 			ed.commit();
 			if (isChecked == true) {
 				lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
 				gpslistener = new GPSCoordsListener();
 				List<String> providers = lm.getAllProviders();
 				if(providers.contains(LocationManager.GPS_PROVIDER)) {
 					lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, gpslistener);
 				}
 				if(providers.contains(LocationManager.NETWORK_PROVIDER)) {
 					lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, gpslistener);
 				}
 			} else {
 				Log.d(LOG, "Lösche Koordinaten.");
 				location = null;
 			}
 		}
 	}
 	
 	protected class GPSCoordsListener implements LocationListener {
 		
 		public void onLocationChanged(Location new_location) {
 			/* Wir nehmen immer die aktuellen Koordinaten, wenn es
 			 *   a) die ersten Koordinaten sind oder
 			 *   b) die bisherigen Koordinaten nur Netzwerk-genau waren oder
 			 *   c) wir aktuell GPS-Koords bekommen haben.
 			 */
 			boolean caseA = (location == null);
 			boolean caseB = (location.getProvider().equals(LocationManager.NETWORK_PROVIDER));
 			boolean caseC = (new_location.getProvider().equals(LocationManager.GPS_PROVIDER));
 			
 			if (caseA || caseB || caseC) {
 				location = new_location;
 			}
 		}
 		
 		public void onProviderDisabled(String provider) {}
 		
 		public void onProviderEnabled(String provider) {}
 		
 		public void onStatusChanged(String provider, int new_status, Bundle extra) {}
 
 	}
 	
 	public class SendTweetListener implements OnClickListener {
 		
 		public void onClick(View v) {
 			String text = ((TextView)findViewById(R.id.tweet_text)).getText().toString().trim();
 			SendableTweet tweet = new SendableTweet(currentAccount, text);
 //			tweet.imagePath = picturePath;
 //			tweet.imagePath = imageAdapter.getItem(0);
 			tweet.images = imageAdapter.getItems();
 			tweet.remainingImages = imageAdapter.getCount();
 			tweet.location = location;
 			tweet.reply_to_status_id = reply_to_id;
 			tweet.dmRecipient = dmRecipient;
 			tweet.imageHoster = getSharedPreferences(Constants.PREFS_APP, 0).getString("pref_image_hoster", "twitter");
 			tweet.imageSize = Long.parseLong(getSharedPreferences(Constants.PREFS_APP, 0).getString("pref_image_size", "-1"));
 			service.addSendableTweet(tweet);
 		
 			if (gpslistener != null && lm != null) {
 				lm.removeUpdates(gpslistener);
 			}
 			finish();
 		}
 		
 	}
 	
 	private ServiceConnection serviceConnection = new ServiceConnection() {
 		
 		@Override
 		public void onServiceConnected(ComponentName name, IBinder binder) {
 			service = ((TweetSendService.TweetSendBinder)binder).getService();
 			Log.d(LOG, "Got service connection.");
 		}
 		
 		@Override
 		public void onServiceDisconnected(ComponentName name) {
 			service = null;
 			Log.d(LOG, "Service disconnected.");
 		}
 		
 	};
 	
 	private void serviceBind() {
 		startService(new Intent(this.getApplicationContext(), TweetSendService.class));
 		bindService(new Intent(this.getApplicationContext(), TweetSendService.class), serviceConnection, Context.BIND_AUTO_CREATE);
 		isServiceBound = true;
 	}
 	
 	private void serviceUnbind() {
 		if (isServiceBound) {
 			unbindService(serviceConnection);
 			isServiceBound = false;
 		}
 	}
 	
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 		serviceUnbind();
 	}
 	
 	private List<User> getAuthUsers() {
 		List<User> result = null;
 		
 		SharedPreferences sp = getSharedPreferences(Constants.PREFS_APP, 0);
 		String accountString = sp.getString("accounts", null);
 		
 		if (accountString != null) {
 			String[] accounts = accountString.split(" ");
 			result = User.getPersistentData(getApplicationContext(), accounts);
 		}
 		
 		return result;
 	}
 	
 	public Account createAccount(User u) {
 		TimelineElementAdapter ta = new TimelineElementAdapter(this, 
 				   R.layout.timeline_element, 
 				   new ArrayList<TimelineElement>());
 		Account acc = Account.getAccount(u);
 		if (acc == null) {
 			acc = new Account(ta, getUserToken(u), u, getApplicationContext(), false);
 		}
 		return acc;
 	}
 
 	private Token getUserToken(User u) {
 		SharedPreferences sp = getSharedPreferences(Constants.PREFS_APP, 0);
 		return new Token(sp.getString("access_token."+String.valueOf(u.id), null), 
 						  sp.getString("access_secret."+String.valueOf(u.id), null));
 	}
 
 }
