 package com.almarsoft.GroundhogReader;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.Vector;
 
 import org.apache.commons.net.nntp.Article;
 import org.apache.commons.net.nntp.NNTPNoSuchMessageException;
 import org.apache.james.mime4j.field.address.Mailbox;
 import org.apache.james.mime4j.field.address.MailboxList;
 import org.apache.james.mime4j.message.Header;
 import org.apache.james.mime4j.message.Message;
 import org.apache.james.mime4j.message.TextBody;
 import org.apache.james.mime4j.parser.Field;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.content.res.Configuration;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.webkit.WebSettings;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ScrollView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.almarsoft.GroundhogReader.lib.DBUtils;
 import com.almarsoft.GroundhogReader.lib.FSUtils;
 import com.almarsoft.GroundhogReader.lib.MessageTextProcessor;
 import com.almarsoft.GroundhogReader.lib.QuotedPrintableDecoder;
 import com.almarsoft.GroundhogReader.lib.ServerAuthException;
 import com.almarsoft.GroundhogReader.lib.ServerManager;
 import com.almarsoft.GroundhogReader.lib.UsenetConstants;
 import com.almarsoft.GroundhogReader.lib.UsenetReaderException;
 
 
 public class MessageActivity extends Activity {
     /** Activity showing one message */
 	
 	private static final int NOT_FINISHED = 0;
 	private static final int FINISHED_GET_OK = 1;
 	private static final int FETCH_FINISHED_ERROR = 2;
 	private static final int FETCH_FINISHED_NOMESSAGE = 3;
 	private static final int FETCH_FINISHED_NODISK = 4;
 	
 	private ProgressDialog mProgress;
 	
 	private int mMsgIndexInArray;
 	private long[] mArticleNumbersArray;
 	private String mGroup;
 	
 	// Loaded from the thread, read by the UI updater:
 	private String mBodyText;
 	private String mOriginalText;
 	private String mSubjectText;
 	private String mAuthorText;
 	private String mLastSubject;
 	private String mCharset;
 	private Header mHeader;
 	private Message mMessage;
 	private Vector<HashMap<String, String>> mMimePartsVector;
 	private boolean mIsFavorite = false;
 	private boolean mShowFullHeaders = false;
 	
 	private LinearLayout mMainLayout;
 	private LinearLayout mLayoutAuthor;
 	private LinearLayout mLayoutSubject;
 	private LinearLayout mLayoutDate;
 	private TextView mAuthor;
 	private ImageView mHeart;
 	private TextView mSubject;
 	private TextView mDate;
 	private WebView mContent;
 	private WebSettings mWebSettings;
 	private Button mButton_Prev;
 	private Button mButton_Next;
 	private ScrollView mScroll;
 	
 	private SharedPreferences mPrefs;
 	final Handler mHandler = new Handler();
 	private ServerManager mServerManager;
 	private boolean mOfflineMode;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
     	super.onCreate(savedInstanceState);
     
     	setContentView(R.layout.message);
     	
     	mPrefs   = PreferenceManager.getDefaultSharedPreferences(this);
     	
     	mOfflineMode = mPrefs.getBoolean("offlineMode", false);
     
     	Bundle extras = getIntent().getExtras();
     	mMsgIndexInArray     = extras.getInt("msgIndexInArray");
     	mArticleNumbersArray = extras.getLongArray("articleNumbers");
     	mGroup               = extras.getString("group");
 
     	mMainLayout    = (LinearLayout) this.findViewById(R.id.main_message_layout);
     	mLayoutAuthor  = (LinearLayout) this.findViewById(R.id.layout_author);
     	mLayoutSubject = (LinearLayout) this.findViewById(R.id.layout_subject);
     	mLayoutDate    = (LinearLayout) this.findViewById(R.id.layout_date);
     	
         mAuthor  = (TextView) this.findViewById(R.id.text_author);
         mHeart   = (ImageView) this.findViewById(R.id.img_love);
         //mHeart.setVisibility(ImageView.INVISIBLE);
         mDate    = (TextView) this.findViewById(R.id.text_date);
         mSubject = (TextView) this.findViewById(R.id.text_subject);
         mSubjectText = null;
         mLastSubject = null;
         
         mContent = (WebView) this.findViewById(R.id.text_content);
         mWebSettings = mContent.getSettings();
         mWebSettings.setDefaultTextEncodingName("utf-8");
         mWebSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
         mWebSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
         mWebSettings.setJavaScriptEnabled(false);
         mWebSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);
         mWebSettings.setSupportZoom(false);
         this.setWebViewSizeFromPrefs(0);
         
         mScroll  = (ScrollView) this.findViewById(R.id.textAreaScroller);
         
         // Conectar los botones next y prev (sumar/restar 1 a mMsgIndexInArray y
         // llamar a loadMessage();
         mButton_Prev = (Button) this.findViewById(R.id.btn_prev);        
 		    mButton_Prev.setOnClickListener(new OnClickListener() {
 		    	
 				public void onClick(View arg0) {
 					if (mMsgIndexInArray > 0) {
 						mMsgIndexInArray--;
 						loadMessage();
 					} else {
 						Toast.makeText(MessageActivity.this, "You're already at the first message", Toast.LENGTH_SHORT).show();
 					}
 	
 				}
 	        });
 		    
         mButton_Next = (Button) this.findViewById(R.id.btn_next);        
 	    mButton_Next.setOnClickListener(new OnClickListener() {
 	    	
 			public void onClick(View arg0) {
 				if (mMsgIndexInArray+1 < mArticleNumbersArray.length) {
 					mMsgIndexInArray++;
 					loadMessage();
 				} else {
 					Toast.makeText(MessageActivity.this, "There are no more messages in the group", Toast.LENGTH_SHORT).show();
 				}
 
 			}
         });
 	    
     	mServerManager = new ServerManager(getApplicationContext());
 		    
         loadMessage();
         
 		mButton_Prev.setFocusable(false);        
         mButton_Next.setFocusable(false);
         mContent.requestFocus();
     }
     
     private WebViewClient mWebViewClient = new WebViewClient() {
         @Override public boolean shouldOverrideUrlLoading(WebView view, String url) {
         	
           // Workaround for an Android bug, sometimes if the url doesn't contain a server address it doesn't works
           if (url.startsWith("attachment://fake.com/")) {
         	  MessageActivity.this.attachClicked(url.replace("attachment://fake.com/", ""));
         	  return true;
           }
           
           else {
         	 Intent intent = new Intent();
         	 //ComponentName comp = new ComponentName("com.android.browser", "com.android.browser.BrowserActivity");
         	 //intent.setComponent(comp); 
 			 intent.setAction(android.content.Intent.ACTION_VIEW);
 			 intent.addCategory("android.intent.category.BROWSABLE"); 
 			 Uri myUri = Uri.parse(url);
 			 intent.setData(myUri);
 			 startActivity(intent);
 			 return true;
           }
         }
     };
     
     
     protected void attachClicked(final String attachcode) {
     	
     	HashMap<String, String> tmpattachPart = null;
     	String tmpmd5 = null;
     	
     	for (HashMap<String, String> part : mMimePartsVector) {
     		tmpmd5 = part.get("md5");
     		if (tmpmd5.equals(attachcode)) {
     			tmpattachPart = part;
     			break;
     		}
     	}
     	
     	final String md5 = tmpmd5;
     	final HashMap<String, String> attachPart = tmpattachPart;
     	
     	if (attachPart != null && md5 != null) {
 	    		
 			new AlertDialog.Builder(this).setTitle("Attachment").setMessage(
 					"Open or save attachment?")
 				.setPositiveButton("Open",
 					new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dlg, int sumthin) {
 							 Intent intent = new Intent(); 
 							 intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
 							 intent.setAction(android.content.Intent.ACTION_VIEW);
 							 File attFile = new File(UsenetConstants.EXTERNALSTORAGE + "/" + UsenetConstants.APPNAME + "/" + UsenetConstants.ATTACHMENTSDIR, md5);
 							 Uri attachUri = Uri.fromFile(attFile);
 							 intent.setDataAndType(attachUri, attachPart.get("type"));
 							 startActivity(intent); 
 						}
 					}
 				).setNegativeButton("Save",	
 					new DialogInterface.OnClickListener() {
 						public void onClick(DialogInterface dlg, int sumthin) {
 							try {
 								String finalPath = FSUtils.saveAttachment(md5, attachPart.get("name"));
 								Toast.makeText(MessageActivity.this, "Saved to " + finalPath, Toast.LENGTH_LONG).show();
 							} catch(IOException e) { 	
 								e.printStackTrace();
 								Toast.makeText(MessageActivity.this, "Could not save: " + e.toString(), Toast.LENGTH_LONG).show();
 							}
 						}
 					})
 				.show();
     	}
 	}
 
 
 	private void setWebViewSizeFromPrefs(int increase) {
     	
     	int textSize = mPrefs.getInt("webViewTextSize", UsenetConstants.TEXTSIZE_NORMAL);
     	
     	if (increase > 0) {
     		if (textSize < UsenetConstants.TEXTSIZE_LARGEST) {
     			textSize++;
     		}
     	} else if (increase < 0) {
     		if (textSize > UsenetConstants.TEXTSIZE_SMALLEST) {
     			textSize--;
     		}
     	}
     	
 		Editor editor = mPrefs.edit();
 		editor.putInt("webViewTextSize", textSize);
 		editor.commit();
     	
     	if (mContent != null) {
     		switch(textSize) {
     		case UsenetConstants.TEXTSIZE_SMALLEST:
     			mWebSettings.setTextSize(WebSettings.TextSize.SMALLEST);
     			break;
     			
     		case UsenetConstants.TEXTSIZE_SMALLER:
     			mWebSettings.setTextSize(WebSettings.TextSize.SMALLER);
     			break;
     			
     		case UsenetConstants.TEXTSIZE_NORMAL:
     			mWebSettings.setTextSize(WebSettings.TextSize.NORMAL);
     			break;
     			
     		case UsenetConstants.TEXTSIZE_LARGER:
     			mWebSettings.setTextSize(WebSettings.TextSize.LARGER);
     			break;
     			
     		case UsenetConstants.TEXTSIZE_LARGEST:
     			mWebSettings.setTextSize(WebSettings.TextSize.LARGEST);
     		}
     	}
 	}
 
 	@Override
     protected void onPause() {
     	super.onPause();
     	
     	Log.d(UsenetConstants.APPNAME, "MessageActivity onPause");
     	
     	if (mServerManager != null) 
     		mServerManager.stop();
     	mServerManager = null;
     	
     	if (mContent != null)
     		mContent.clearCache(true);
     }
     
 
     // Try to detect server hostname changes in the settings (if true, go to the 
     // (grouplist activity which will handle better the change)
 	@Override
 	protected void onResume() {
 		super.onResume();
 		
 		Log.d(UsenetConstants.APPNAME, "MessageActivity onResume");
 		
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
 		
 		if (prefs.getBoolean("hostChanged", false)) {
 			// The host  has changed in the prefs, go to the GroupList
 			this.startActivity(new Intent(MessageActivity.this, GroupListActivity.class));
 		}
 		
 		if (mServerManager == null)
 			mServerManager = new ServerManager(getApplicationContext());
 	}
     
     
     @Override
     protected void onActivityResult(int intentCode, int resultCode, Intent data) {
     	super.onActivityResult(intentCode, resultCode, data);
     	
     	if (intentCode == UsenetConstants.COMPOSEMESSAGEINTENT) {
     		
     		if (resultCode == RESULT_OK) { 
     			
     			if (mOfflineMode && !mPrefs.getBoolean("postDirectlyInOfflineMode", false))
     				Toast.makeText(getApplicationContext(), "Message stored in outbox, will be send in next sync", Toast.LENGTH_SHORT).show();
     			else
     				Toast.makeText(getApplicationContext(), "Message send", Toast.LENGTH_SHORT).show();
     		}
     		else if (resultCode == RESULT_CANCELED) 
     			Toast.makeText(getApplicationContext(), "Message discarded", Toast.LENGTH_SHORT).show();
     		
     	} else if (intentCode == UsenetConstants.BANNEDACTIVITYINTENT) {
     		
     		if (resultCode == RESULT_OK) Toast.makeText(getApplicationContext(), "Use reload on the message list to see un-banned authors", Toast.LENGTH_LONG).show();
     		else if (resultCode == RESULT_CANCELED) Toast.makeText(getApplicationContext(), "Nothing to unban", Toast.LENGTH_SHORT).show();
     		
     	} else if (intentCode == UsenetConstants.QUOTINGINTENT) {
     		
     		String composeText;
     		if (resultCode == RESULT_OK) 
     			composeText = data.getStringExtra("quotedMessage");
     		else 
     			composeText = mOriginalText;
     		
     		Intent intent_Post = new Intent(MessageActivity.this, ComposeActivity.class);
 			intent_Post.putExtra("isNew", false);
 			intent_Post.putExtra("From", mAuthorText);
 			intent_Post.putExtra("Newsgroups", mHeader.getField("Newsgroups").getBody());
 			intent_Post.putExtra("Date", mHeader.getField("Date").getBody());
 			intent_Post.putExtra("Message-ID", mHeader.getField("Message-ID").getBody());
 			if (mHeader.getField("References") != null)
 				intent_Post.putExtra("References", mHeader.getField("References").getBody());
 			if (mSubjectText != null)
 				intent_Post.putExtra("Subject", mSubjectText);
 			intent_Post.putExtra("bodytext", composeText);			
 			if (data != null)
 				intent_Post.putExtra("multipleFollowup", data.getStringExtra("multipleFollowup"));
 			intent_Post.putExtra("group", mGroup);
 			startActivityForResult(intent_Post, UsenetConstants.COMPOSEMESSAGEINTENT);
     	}
     }
 
 	
 	
 	// ================================================
 	// Menu setting
 	// ================================================
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		new MenuInflater(getApplication()).inflate(R.menu.messagemenu, menu);
 		
 		return(super.onCreateOptionsMenu(menu));
 
 	}
 	
 	
 	@Override
 	public boolean onPrepareOptionsMenu(Menu menu) {
 		int textSize = mPrefs.getInt("webViewTextSize", UsenetConstants.TEXTSIZE_NORMAL);
 
 		MenuItem bigtext   = menu.findItem(R.id.message_menu_bigtext);
 		MenuItem smalltext = menu.findItem(R.id.message_menu_smalltext);
 		
 		if (textSize == UsenetConstants.TEXTSIZE_LARGEST) 
 			bigtext.setEnabled(false);
 		else
 			bigtext.setEnabled(true);
 		
 		if (textSize == UsenetConstants.TEXTSIZE_SMALLEST)
 			smalltext.setEnabled(false);
 		else
 			smalltext.setEnabled(true);
 		return (super.onPrepareOptionsMenu(menu));
 		
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		
 			case R.id.message_menu_reply:
 				
 				if (mHeader != null) {
 					String multipleFollowup = mPrefs.getString("multipleFollowup", "ASK");
 			    	String groups = mHeader.getField("Newsgroups").getBody();
 			    	String[] groupsArray = null;
 			    	
 			    	// If is configured to ask for multiple followup and there are in fact multiple, show the dialog
 			    	if (groups != null) {
 			    		groupsArray = groups.split(",");
 			    		if (groupsArray.length > 1) {
 			    			if (multipleFollowup.equalsIgnoreCase("ASK")) {
 			    				showFollowupDialog(groupsArray);
 			    				return true;
 			    			}
 			    		}
 			    	}
 			    	
 			    	startPostingOrQuotingActivity(multipleFollowup);
 				} else
 					Toast.makeText(this, "Can't reply; no header data", Toast.LENGTH_SHORT).show();
 				
 		    	return true;
 				
 			case R.id.message_menu_settings:
         		startActivity(new Intent(MessageActivity.this, OptionsActivity.class));
 				return true;
 				
 			case R.id.message_menu_markunread:
 				DBUtils.markAsUnRead(mArticleNumbersArray[mMsgIndexInArray], getApplicationContext());
 				Toast.makeText(this, "Message marked as unread", Toast.LENGTH_SHORT).show();
 				return true;
 				
 			case R.id.message_menu_forward:
 				forwardMessage();
 				return true;
 				
 			case R.id.message_menu_favorite:
 				toggleFavoriteAuthor();
 				return true;
 				
 			case R.id.message_menu_ban:
 				if (mHeader != null) {
 					DBUtils.banUser(mHeader.getField("From").getBody(), getApplicationContext());
 					Toast.makeText(this, "Author banned, reload on the message list to hide", Toast.LENGTH_LONG).show();
 				} else 
 					Toast.makeText(this, "Can't ban author; no header data", Toast.LENGTH_SHORT).show();
 						
 				return true;
 				
 			case R.id.message_menu_manageban:
 				Intent intent_bannedthreads = new Intent(MessageActivity.this, BannedActivity.class);
 				intent_bannedthreads.putExtra("typeban", UsenetConstants.BANNEDTROLLS);
 				startActivityForResult(intent_bannedthreads, UsenetConstants.BANNEDACTIVITYINTENT);
 				return true;
 				
 			case R.id.message_menu_bigtext:
 				setWebViewSizeFromPrefs(1);
 				return true;
 				
 			case R.id.message_menu_smalltext:
 				setWebViewSizeFromPrefs(-1);
 				return true;
 				
 			case R.id.message_menu_fullheaders:
 				toggleFullHeaders();
 				return true;
 		}
 		return false;
 	}    
     
     @Override 
     public void onConfigurationChanged(Configuration newConfig) { 
       //ignore orientation change because it would cause the message body to be reloaded
       super.onConfigurationChanged(newConfig);
     }
     
     
     private void showFollowupDialog(String[] groups) {
     	StringBuilder buf = new StringBuilder(groups.length);
     	
     	for (String g : groups)
     		buf.append(g + "\n");
     	
 		new AlertDialog.Builder(this).setTitle("Multiple followup").setMessage(
 				"The message goes to more than one group. Do you want to followup to all " +
 				"or only to the current group? The groups are:\n" + buf.toString())
 			.setPositiveButton("All",
 				new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dlg, int sumthin) {
 						startPostingOrQuotingActivity("ALL");
 						}
 					}
 			).setNegativeButton("Current",	
 				new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dlg, int sumthin) {
 						startPostingOrQuotingActivity("CURRENT");
 						}
 					})
 			.show();
     }
     
     
     private void startPostingOrQuotingActivity(String multipleFollowup) {
     	
     	if (mHeader == null) {
     		Toast.makeText(this, "Can't reply; no header data", Toast.LENGTH_SHORT);
     		return;
     	}
     		
     	
 		// Check that the user has set "name" and "email on preferences
 		String name  = mPrefs.getString("name", null);
 		String email = mPrefs.getString("email", null);
 		
 		if (name == null || name.trim().length() == 0 || email == null || email.trim().length() == 0) {
 			userInfoNotSet();
 			return;
 		}
 		
 		boolean useQuoter = mPrefs.getBoolean("useQuotingView", true);
 		if (useQuoter) {
 			Intent intent_Quote = new Intent(MessageActivity.this, QuotingActivity.class);
 			intent_Quote.putExtra("origText", mOriginalText);
 			intent_Quote.putExtra("multipleFollowup", multipleFollowup);
 			startActivityForResult(intent_Quote, UsenetConstants.QUOTINGINTENT);
 			
 		} else {
 			
 			Intent intent_Post = new Intent(MessageActivity.this, ComposeActivity.class);
 			intent_Post.putExtra("isNew", false);
 			intent_Post.putExtra("headerdata", mHeader.toString());
 			intent_Post.putExtra("bodytext", mOriginalText);
 			intent_Post.putExtra("multipleFollowup", multipleFollowup);
 			intent_Post.putExtra("group", mGroup);
 			startActivityForResult(intent_Post, UsenetConstants.COMPOSEMESSAGEINTENT);
 		}    	
     }
     
     
     private void userInfoNotSet() {
     	
 		new AlertDialog.Builder(this).setTitle("User info unset").setMessage(
 				"You must fill your name and email (can be fake) on the settings (on the User Info section). " +
 				"Do you want to go to the settings now?")
 				.setPositiveButton("Yes",
 						new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dlg, int sumthin) {
 								startActivity(new Intent(MessageActivity.this, OptionsActivity.class));
 								}
 							}
 						).setNegativeButton("No", null)
 						.show();
     }
     
     
     private void toggleFavoriteAuthor() {
 
     	if (mHeader == null) {
     		Toast.makeText(this, "Can't make favorite; no header data", Toast.LENGTH_SHORT);
     		return;
     	}
     	
     	DBUtils.setAuthorFavorite(mIsFavorite, !mIsFavorite, mHeader.getField("From").toString(), getApplicationContext());
     	mIsFavorite = !mIsFavorite; 
     	
         if (mIsFavorite) {
         	mHeart.setImageDrawable(getResources().getDrawable(R.drawable.love));
         } else {
         	mHeart.setImageDrawable(getResources().getDrawable(R.drawable.nullimage));
         }
     }
     
     
     // ===============================================================
     // Forward a message by email using the configured email program
     // ===============================================================
     private void forwardMessage() {
     	String forwardMsg = "\n\n\nForwarded message originally written by " + mHeader.getField("From").getBody() + 
     	                    " on the newsgroup [" +  mGroup + "]: \n\n" + mOriginalText;
     	
     	Intent i = new Intent(Intent.ACTION_SEND);
     	i.putExtra(Intent.EXTRA_TEXT, forwardMsg);
     	//i.putExtra(Intent.EXTRA_SUBJECT, "FWD: " + mHeader.getField("Subject").getBody());
     	i.putExtra(Intent.EXTRA_SUBJECT, "FWD: " + mSubjectText);
     	i.setType("message/rfc822");
     	startActivity(Intent.createChooser(i, "Title:"));
     }
     
     
     // ====================================================================
     // Get the body on a thread; the thread will update the UI
     // ====================================================================
     private void loadMessage() {
     	
     	Thread serverGetterThread = new Thread() {
     		
 	        // ========================================================
 	        // Main thread activity, get the messages and update the UI
 	        // ========================================================
     		
     		public void run() {
     			
     	    	try {
 
     	    		Field tmpfield = null;
     	    		
     	    		updateStatus("Fetching message body", NOT_FINISHED);
     	    		
     	    		// shortcut
     	    		long serverMsgNumber = mArticleNumbersArray[mMsgIndexInArray];
     	    		Hashtable<String, Object> articleData = DBUtils.getHeaderRecordCatchedData(mGroup, serverMsgNumber, MessageActivity.this);
     	    		boolean isCatched = (Boolean) articleData.get("catched");
     	    		
     	    		if (!isCatched) { // This also connects if its unconnected
     	    			
     	    			mServerManager.selectNewsGroup(mGroup, mOfflineMode);
     	    			
     	    		} else { // This wont connect (since the message is catched), so the loading will be faster even in online mode
     	    			
     	    			mServerManager.selectNewsGroupWithoutConnect(mGroup);
     	    			
     	    		}
     	    		
     	    		// ===========================================================================================
     	    		// Get or load the header, and from the header, the from, subject, date,
     	    		// and Content-Transfer-Encoding
     	    		// ===========================================================================================
 
     	    		mHeader = mServerManager.getHeader((Integer)articleData.get("id"), 
     	    				                           (String)articleData.get("server_article_id"), 
     	    				                           false, isCatched);    	    		
 
     	    		if (mHeader == null)
     	    			throw new UsenetReaderException("Could not fetch header from server");    	    		
 
     	    		// Load the bodyText into mBodyText (the UI updater will drawn it)
     	    		// decoding it it needed
     	    		/*
     	    		String encoding = null;
     	    		
     	    		encoding = mHeader.getField("Content-Transfer-Encoding").getBody();
     	    		*/
 
     	    		// ===========================================================================================
     	    		// Extract the charset from the Content-Type header or if it's MULTIPART/MIME, the boundary
     	    		// between parts
     	    		// ===========================================================================================
 
     	    		String[] tmpContentArr = null;
     	    		String[] contentTypeParts = null;
     	    		String tmpFirstToken;
     	    		
     	    		String mimeBoundary = null;
     	    		mCharset = mPrefs.getString("readDefaultCharset", "ISO8859-15");
     	    		
     	    		// ZZZ: Es posible que teniendo el Message todo esto sobre
     	    		//boolean isMime = (mHeader.getField("MIME-Version") != null);
     	    		
     	    		Field tmpField = mHeader.getField("Content-Type");
     	    		if (tmpField != null) {
     	    			tmpContentArr = tmpField.getBody().split(";");
     	    			int contentLen = tmpContentArr.length;
     	    		
 	    	    		for (int i=0; i<contentLen; i++) {
 	    	    			
 	    	    			contentTypeParts = tmpContentArr[i].split("=", 2);
 	    	    			tmpFirstToken = contentTypeParts[0].trim();
 	    	    			
 	    	    			if (contentTypeParts.length > 1 && tmpFirstToken.equalsIgnoreCase("charset")) {
 	    	    				
 	    	    				mCharset = contentTypeParts[1].replace("\"", "").trim();
 	    	    				
 	    	    			} else if (contentTypeParts.length > 1 && tmpFirstToken.equalsIgnoreCase("boundary")) {
 	    	    				
     	    					mimeBoundary = contentTypeParts[1].replace("\"", "").trim();
 	    	    			}
 	    	    			
 	    	    		}
     	    		}
     	    		
     	    		// ===============================================================================
     	    		// Get or load the body, extract the mime text/plain part if it is a Mime message
     	    		// and decode if it is QuotedPrintable
     	    		// ===============================================================================
     	    		mMessage = mServerManager.getMessage(mHeader, 
     	    				                             (Integer)articleData.get("id"),
     	    				                             (String)articleData.get("server_article_id"),
     	    				                             false, isCatched, mCharset);
     	    		
     	    		Vector<Object> body_attachs = MessageTextProcessor.getBodyAndAttachments(mMessage);
     	    		TextBody textBody = (TextBody)body_attachs.get(0);
     	    		mBodyText = MessageTextProcessor.readerToString(textBody.getReader()).trim();
     	    		
     	    		if (mSubjectText != null)
     	    			mLastSubject = Article.simplifySubject(mSubjectText);
     	    		
     	    		mSubjectText = MessageTextProcessor.decodeSubject(mHeader.getField("Subject"), mCharset, mMessage);
     	    		
     	    		// XXX YYY ZZZ Obtener los adjuntos
 
     	    		/*
 	    			if (mMimePartsVector != null) mMimePartsVector.clear();
     	    		if (isMime && mimeBoundary != null) {
 
     	    			mMimePartsVector = MessageTextProcessor.extractMimeParts(mBodyText, mimeBoundary);
     	    			
     	    			// The bodyText HashMap is always the first element of the Vector
     	    			mBodyText = mMimePartsVector.get(0).get("bodyText");
     	    			mCharset  = mMimePartsVector.get(0).get("charset"); 
     	    			encoding  = mMimePartsVector.get(0).get("encoding");
     	    			mMimePartsVector.removeElementAt(0);
     	    		}
 
     	    		// Check for uuencoded attachments
     	    		Vector<HashMap<String, String>> uuattachData = MessageTextProcessor.getUUEncodedAttachments(mBodyText);
     	    		
     	    		if (uuattachData != null) {
     	    			mBodyText = uuattachData.get(0).get("body");
     	    			uuattachData.removeElementAt(0);
     	    			
     	    			if (uuattachData.size() > 0) {
     	    				if (mMimePartsVector == null || mMimePartsVector.size() == 0) 
     	    					mMimePartsVector = uuattachData;
     	    				else {
     	    					// Join the two vectors
     	    					for (HashMap<String, String> attach : uuattachData) {
     	    						mMimePartsVector.add(attach);
     	    					}
     	    				}
     	    				
     	    			}
     	    		}
     	    		
     	    		if (encoding != null && encoding.equalsIgnoreCase("quoted-printable")) 
                         mBodyText = QuotedPrintableDecoder.decode(mBodyText);
                     */
     	    		
     	    		updateStatus("Fetching message body", FINISHED_GET_OK);
     	    		
 				} catch (NNTPNoSuchMessageException e) {
 					updateStatus("Error", FETCH_FINISHED_NOMESSAGE);
 					e.printStackTrace();
 				} catch (FileNotFoundException e) {
 					updateStatus("Error", FETCH_FINISHED_NODISK);
 					e.printStackTrace();
 				} catch (IOException e) {
 					updateStatus("Error", FETCH_FINISHED_ERROR);
 					e.printStackTrace();
 				} catch (ServerAuthException e) {
 					updateStatus("Error", FETCH_FINISHED_ERROR);
 					e.printStackTrace();
 				} catch (UsenetReaderException e) {
 					updateStatus("Error", FETCH_FINISHED_ERROR);
 					e.printStackTrace();
 				}
     		}
     	};
     	
     	serverGetterThread.start();
 		mProgress = new ProgressDialog(this);
 		mProgress.setMessage("Requesting message");
 		mProgress.setTitle("Message");
 		mProgress.show();   	
     }
     
     
     // =========================================================
     // Sent an update the the UI (progress dialog) from a thread
     // =========================================================
 
     private void updateStatus(final String textStatus, final int threadStatus) {
     	mHandler.post(new Runnable() { 
     		public void run() { 
     			updateResultsInUi(textStatus, threadStatus); 
     			} 
     		}
     	);
     }
     
     
     private void toggleFullHeaders() {
     	
     	mShowFullHeaders = !mShowFullHeaders;
     	loadMessage();
     }
     
     
     // ===================================================================================
     // UI updater from Threads; check the status, progress and message and display them
     // Also: Downloader thread finished => Call the loading of messages from the DB thread
     // Loading of Msgs from DB finished => Set the listview adapter to display messages
     // ===================================================================================
    
 	private void updateResultsInUi(String TextStatus, int ThreadStatus) {
     	
     	if (mProgress != null) {
     		mProgress.setMessage(TextStatus);
     	}
     	
     	if (ThreadStatus == FETCH_FINISHED_ERROR) {
     		if (mProgress != null) mProgress.dismiss();
     		
     		mContent.loadData("Error loading message body (message has been kept unread)", "text/html", "UTF-8");
     		
 			new AlertDialog.Builder(this)
 			.setTitle("Error")
 			.setMessage("There was an error trying to retrieve the message body. Please " +
 					    "check your server settings and connection status (this message will be kept unread.)")
 		    .setNeutralButton("Close", null)
 		    .show();
 			
 			
     	}
     	else if (ThreadStatus == FETCH_FINISHED_NODISK) {
     		if (mProgress != null) mProgress.dismiss();
     		
     		mContent.loadData("Error saving message  (message has been kept unread)", "text/html", "UTF-8");
     		
 			new AlertDialog.Builder(this)
 			.setTitle("Error")
 			.setMessage("There was an error trying to save the message to the sdcard or disk. Please check your sdcard (message has been kept unread)")					    
 		    .setNeutralButton("Close", null)
 		    .show();
     	}
     	else if (ThreadStatus == FETCH_FINISHED_NOMESSAGE) {
     		if (mProgress != null) mProgress.dismiss();
     		
     		mContent.loadData("[[The server doesn't have the message anymore, it's probably too old or was spam]]", "text/html", "UTF-8");
     		
 			new AlertDialog.Builder(this)
 			.setTitle("Error")
 			.setMessage("The server doesn't have the message anymore.")
 		    .setNeutralButton("Close", null)
 		    .show();
 			
 			DBUtils.markAsRead(mArticleNumbersArray[mMsgIndexInArray], getApplicationContext());
 			
     	} 
     	else if (ThreadStatus == FINISHED_GET_OK) {
     		
     		// Show or hide the heart marking favorite authors
             mIsFavorite = DBUtils.isAuthorFavorite(mHeader.getField("From").getBody(), getApplicationContext());
             
             if (mIsFavorite) 
             	mHeart.setImageDrawable(getResources().getDrawable(R.drawable.love));
             else 
             	mHeart.setImageDrawable(getResources().getDrawable(R.drawable.nullimage));
             
             
             mHeart.invalidate();
             mLayoutAuthor.invalidate();
             mMainLayout.invalidate();
 
     		// Save a copy of the body for the reply so we don't break netiquette rules with
     		// the justification applied in sanitizeLinebreaks
 
     		mOriginalText = mBodyText;
     		
     		// Justify the text removing artificial '\n' chars so it looks square and nice on the phone screen
     		// XXX: Optimizacion: aqui se puede utilizar de forma intermedia un StringBuffer (sanitize
     		// lo devuelve y se le pasa a prepareHTML)
     		
     		
             // XXX: NO reutilizar mBodyText tanto, ahorra memoria pero es una mierda
     		mBodyText = MessageTextProcessor.sanitizeLineBreaks(mBodyText);
     		
     		mBodyText = MessageTextProcessor.getHtmlHeader(mCharset) + 
     		            MessageTextProcessor.getAttachmentsHtml(mMimePartsVector)  + 
     		            MessageTextProcessor.prepareHTML(mBodyText);
     		
     		
     		// Show the nice, short, headers or the ugly full headers if the user selected that
     		if (!mShowFullHeaders) {
     			mLayoutAuthor.setVisibility(View.VISIBLE);
     			mLayoutDate.setVisibility(View.VISIBLE);
     			mLayoutSubject.setVisibility(View.VISIBLE);
     			
     			mAuthorText = MessageTextProcessor.decodeFrom(mHeader.getField("From"), mCharset, mMessage);
     			mAuthor.setText(mAuthorText);
     			mDate.setText(mHeader.getField("Date").getBody());
     			mSubject.setText(mSubjectText);
     			
     		} else {
     			mLayoutAuthor.setVisibility(View.INVISIBLE);
     			mLayoutDate.setVisibility(View.INVISIBLE);
     			mLayoutSubject.setVisibility(View.INVISIBLE);
     			mBodyText = MessageTextProcessor.htmlizeFullHeaders(mMessage) + mBodyText;
     		}
     		
     		mContent.loadDataWithBaseURL("x-data://base", mBodyText, "text/html", mCharset, null);
     		mBodyText = null;
     		mContent.requestFocus();
     		
    		DBUtils.markAsRead(mHeader.getField("Message-ID").getBody().trim(), getApplicationContext());
     		
     		// Go to the start of the message
     		mScroll.scrollTo(0, 0);
     		
     		if (mProgress != null) mProgress.dismiss();
     		
     		String simplifiedSubject = Article.simplifySubject(mSubjectText);
 
     		if (mLastSubject != null && (!mLastSubject.equalsIgnoreCase(simplifiedSubject))) {
     			Toast.makeText(getApplicationContext(), "New Subject: \n" + simplifiedSubject, Toast.LENGTH_SHORT).show();
     		}
     		
             // Intercept "attachment://" url clicks
             mContent.setWebViewClient(mWebViewClient);
             
     	}
     }
 }
