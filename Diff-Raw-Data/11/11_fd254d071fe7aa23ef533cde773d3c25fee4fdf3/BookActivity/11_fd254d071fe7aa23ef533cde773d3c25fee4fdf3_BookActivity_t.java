 package org.csie.mpp.buku;
 
 import java.io.IOException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.csie.mpp.buku.db.BookEntry;
 import org.csie.mpp.buku.db.DBHelper;
 import org.csie.mpp.buku.db.FriendEntry;
 import org.csie.mpp.buku.helper.BookUpdater;
 import org.csie.mpp.buku.helper.BookUpdater.OnUpdateStatusChangedListener;
 import org.csie.mpp.buku.view.FriendsManager.FriendEntryAdapter;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.facebook.android.BaseDialogListener;
 import com.facebook.android.SessionStore;
 import com.flurry.android.FlurryAgent;
 import com.markupartist.android.widget.ActionBar;
 import com.markupartist.android.widget.ActionBar.AbstractAction;
 import com.markupartist.android.widget.ActionBar.Action;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnCancelListener;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.text.Html;
 import android.text.method.LinkMovementMethod;
 import android.text.method.ScrollingMovementMethod;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.webkit.WebSettings;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.RatingBar;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class BookActivity extends Activity implements OnUpdateStatusChangedListener, View.OnClickListener {
 	public static final int REQUEST_CODE = 1437;
 	public static final int RESULT_ADD = 632;
 	public static final int RESULT_ISBN_INVALID = 633;
 	public static final int RESULT_NOT_FOUND = 634;
 	public static final int RESULT_DELETE = 643;
 	
 	public static final String CHECK_DUPLICATE = "duplicate";
 	public static final String LINK = "link";
 	
 	private DBHelper db;
 	
 	private BookEntry entry;
 	private BookUpdater updater;
 	private ActionBar actionBar;
 	private Action actionAdd, actionDelete, actionShare;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.book);
 
         // initialize FB
         SessionStore.restore(App.fb, this);
         
         db = new DBHelper(this);
         
         createActionButtons();
         
         Intent intent = getIntent();
         String isbn = intent.getStringExtra(App.ISBN);
         if(isbn == null) {
         	String link = intent.getStringExtra(LINK);
         	entry = new BookEntry();
 	        updater = BookUpdater.create(entry, link);
 	        updater.setOnUpdateFinishedListener(this);
 	        updater.updateEntry();
 	        updater.updateInfo();
         }
         else {
 	        if(Util.checkIsbn(isbn) == false) {
 	        	setResult(RESULT_ISBN_INVALID);
 	        	finish();
 	        	return;
 	        }
 	        entry = BookEntry.get(db.getReadableDatabase(), isbn);
 	        
 	        boolean updateAll = false;
 	        
 	        if(entry != null) {
 	        	if(intent.getBooleanExtra(CHECK_DUPLICATE, false))
 	        		Toast.makeText(this, R.string.msg_book_already_exists, App.TOAST_TIME).show();
 	
 	        	updateView(null);
 	        }
 	        else {
 	        	entry = new BookEntry();
 	        	entry.isbn = isbn;
 	        	updateAll = true;
 	        }	
 	        
 	        updater = BookUpdater.create(entry);
 	        updater.setOnUpdateFinishedListener(this);
 	  
 	       	if(updateAll)
 	       		updater.updateEntry();
 	       	else {
 	       		updater.updateInfo();
 	       		actionBar.addAction(actionDelete, 0);
 	       		actionBar.addAction(actionShare, 1);
 	       	}
         }
     }
 
     /* --- OptionsMenu			(start) --- */
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
     	MenuInflater inflater = getMenuInflater();
     	inflater.inflate(R.menu.book, menu);
     	return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
     	switch(item.getItemId()) {
     		case R.id.menu_share:
     			openShareDialog(null);
     			break;
     		case R.id.menu_share_to:
     			openShareToFriendDialog();
     			break;
     		default:
     			break;
     	}
     	return true;
     }
     /* --- OptionsMenu			(end) --- */
     
     private void createLikeButton() {
         final WebView like = new WebView(this);
         final LinearLayout container = (LinearLayout)findViewById(R.id.like_button);
         container.addView(like);
         
         WebSettings settings = like.getSettings();
         settings.setJavaScriptEnabled(true);
         settings.setSupportZoom(false);
         
         like.setHorizontalScrollBarEnabled(false);
         like.setVerticalScrollBarEnabled(false);
         
         like.setOnTouchListener(new View.OnTouchListener() {
 			@Override
 			public boolean onTouch(View v, MotionEvent event) {
 				return (event.getAction() == MotionEvent.ACTION_MOVE);
 			}
 		});
 
         final String locale = getString(R.string.country_code);
         String httpSrc = Util.streamToString(getResources().openRawResource(R.raw.fb_like)).replace(
         	"#BOOK_TITLE#", entry.title
         ).replace(
         	"#BOOK_URL#", entry.info.source
         ).replace(
         	"#BOOK_IMAGE#", entry.coverLink
         ).replace(
         	"#SITE_NAME#", BookUpdater.SOURCE_BOOKS_TW
         ).replace(
         	"#COUNTRY_CODE#", locale
         );
         
         like.loadDataWithBaseURL("https://www.facebook.com/", httpSrc, "text/html", "utf-8", null);
         like.setWebViewClient(new WebViewClient() {
         	@Override
         	public boolean shouldOverrideUrlLoading(WebView view, String url) {
         		return true;
         	}
         });
     }
     
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
     	App.fb.authorizeCallback(requestCode, resultCode, data);
     }
     
     @Override
     public void onStart() {
     	super.onStart();
 
 		FlurryAgent.onStartSession(this, App.FLURRY_APP_KEY);
     }
     
     @Override
     public void onStop() {
     	super.onStop();
     	
     	FlurryAgent.onEndSession(this);
     }
     
     @Override
     public void onDestroy() {
     	super.onDestroy();
     	
     	db.close();
     }
 
     /* --- OnUpdateFinishedListener	(start) --- */
     @Override
     public void onUpdateStart() {
     	updateView(null);
     }
     
     @Override
     public void onUpdateProgress() {
     	// TODO: enhance efficiency
     	updateView(null);
     }
 	
 	@Override
 	public void onUpdateFinish(Status status) {
 		switch(status) {
 			case OK_ENTRY:
 				if(BookEntry.exists(db.getReadableDatabase(), entry.isbn))
 					actionBar.addAction(actionDelete, 0);
 				else
 					actionBar.addAction(actionAdd, 0);
 				actionBar.addAction(actionShare, 1);
 				updater.updateInfo();
 				updateView(status);
 				break;
 			case OK_INFO:
 				updateView(status);
 				break;
 			case BOOK_NOT_FOUND:
 	    		FlurryAgent.logEvent(App.FlurryEvent.BOOK_NOT_FOUND.toString());
 	    		FlurryAgent.logEvent(entry.isbn);
 	    		setResult(RESULT_NOT_FOUND);
 	    		finish();
 	    		break;
 			default:
 				Toast.makeText(this, R.string.msg_unexpected_error, App.TOAST_TIME).show();
 				break;
 		}
 	}
 	/* --- OnUpdateFinishedListener	(end) --- */
     
     private void updateView(Status status) {
     	ImageView cover = ((ImageView)findViewById(R.id.image));
     	if(entry.cover != null)
     		cover.setImageBitmap(entry.cover);
     	else
     		cover.setImageResource(R.drawable.book);
     	
     	if(entry.title != null) {
     		((TextView)findViewById(R.id.title)).setText(entry.title);
     		((TextView)findViewById(R.id.author)).setText(entry.author);
             ((RatingBar)findViewById(R.id.rating)).setRating(entry.info.rating);
     	}
     	else {
         	if(status == null)
         		((TextView)findViewById(R.id.title)).setText(R.string.msg_updating);
     	}
     	
     	if(BookUpdater.SOURCE_BOOKS_TW.equals(entry.info.sourceName))
 			createLikeButton();
         
         TextView description = ((TextView)findViewById(R.id.description));
         if(entry.info.description != null) {
         	String shortContent = Util.shortenString(entry.info.description.toString(), 200);
         	if(entry.info.description.length() > shortContent.length()) {
         		description.setOnClickListener(new OnClickListener() {
         	        @Override
         	        public void onClick(View v) {
         	        	new AlertDialog.Builder(BookActivity.this).setMessage(entry.info.description).show();               
         	        }
         	    });
         	}
         	description.setText(shortContent);
         	description.setMovementMethod(new ScrollingMovementMethod());
         }
         else {
         	if(status == null)
         		description.setText(R.string.msg_updating);
         	else if(status == Status.OK_INFO)
         		description.setText(R.string.text_no_data);
         }
         
         LinearLayout reviews = (LinearLayout)findViewById(R.id.reviews);
         if(entry.info.reviews != null) {
         	if(reviews.getChildCount() == 1) {
         		reviews.removeAllViews();
         		
         		int size = entry.info.reviews.size();
 	        	for(int i = 0; i < size; i++) {
 	        		View view = getLayoutInflater().inflate(R.layout.list_item_review, null);
 	        		TextView review = ((TextView)view.findViewById(R.id.list_review));
 	        		if(entry.info.reviews.get(i).length()>100){
 	        			String shortContent = Util.shortenString(entry.info.reviews.get(i).toString(), 100);
 	        			review.setOnClickListener(this);
 	        			review.setId(i);
 			            review.setText(shortContent);
 	        		}
                     else {
 		        		review.setText(entry.info.reviews.get(i));
 		        		review.setMovementMethod(LinkMovementMethod.getInstance());
 	        		}
 	        		reviews.addView(view);        	  
 	        	}
         	}
         }
         else {
         	if(status == null) {
         		if(reviews.getChildCount() == 0) {
         			View view = getLayoutInflater().inflate(R.layout.list_item_review, null);
         			((TextView)view.findViewById(R.id.list_review)).setText(R.string.msg_updating);
         			reviews.addView(view);
         		}
         	}
         	else if(status == Status.OK_INFO)
         		description.setText(R.string.text_no_data);
         }
         
         if(entry.info.source != null) {
         	TextView sources = ((TextView)findViewById(R.id.sources));
         	sources.setText(Html.fromHtml("<a href=\"" + entry.info.source + "\">"+ entry.info.sourceName + "</a>"));
         	sources.setMovementMethod(LinkMovementMethod.getInstance());
         }
     }
     
     private void createActionButtons() {
         actionBar = ((ActionBar)findViewById(R.id.actionbar));
         
         // FB share button
     	actionShare = new AbstractAction(R.drawable.ic_share) {
 			@Override
 			public void performAction(View view) {
 				if(App.fb.isSessionValid())
 					openShareDialog(null);
 				else {
 					FlurryAgent.logEvent(App.FlurryEvent.SHARE_ON_FB.toString());
 					App.fb.authorize(BookActivity.this, App.FB_APP_PERMS, new BaseDialogListener(BookActivity.this, App.TOAST_TIME) {
 						@Override
 						public void onComplete(Bundle values) {
 							SessionStore.save(App.fb, BookActivity.this);
 							openShareDialog(null);
 						}
 					});
 				}
 			}
     	};
 
 		actionAdd = new AbstractAction(R.drawable.ic_bookshelf) {
 			@Override
 			public void performAction(View view) {
 				if(entry.insert(db.getWritableDatabase()) == false)
 					Log.e(App.TAG, "Insert failed \"" + entry.isbn + "\".");
 				
 				Intent data = new Intent();
 				data.putExtra(App.ISBN, entry.isbn);
 				setResult(RESULT_ADD, data);
 				finish();
 			}
 		};
 		
 		actionDelete = new AbstractAction(R.drawable.ic_delete) {
 			@Override
 			public void performAction(View view) {
 				Intent data = new Intent();
 				data.putExtra(App.ISBN, entry.isbn);
 				setResult(RESULT_DELETE, data);
 				finish();
 			}
 		};
     }
 	
 	private void openShareDialog(String who) {
 		Bundle params = new Bundle();
 		params.putString("name", entry.title);
 		params.putString("link", entry.info.source != null? entry.info.source : App.FB_FAN_PAGE);
 		params.putString("caption", entry.author);
 		
 		if(entry.info.description != null)
 			params.putString("description", Util.shortenString(entry.info.description.toString(), 120));
 		if(entry.coverLink != null)
 			params.putString("picture", entry.coverLink);
 		
 		if(who != null)
 			params.putString("to", who);
 		
 		App.fb.dialog(BookActivity.this, "feed", params, new BaseDialogListener(BookActivity.this, App.TOAST_TIME) {
 			@Override
 			public void onComplete(Bundle values) {
 				if(values.containsKey("post_id"))
 					Toast.makeText(BookActivity.this, R.string.fb_message_posted, App.TOAST_TIME).show();
 			}
 		});
 	}
 	
 	private void openShareToFriendDialog() {
 		final List<FriendEntry> entries = new ArrayList<FriendEntry>();
 		final FriendEntryAdapter adapter = new FriendEntryAdapter(this, R.layout.list_item_friend, entries);
 		
		final AsyncTask<String, Integer, Boolean> async = new AsyncTask<String, Integer, Boolean>() {
 			@Override
 			protected Boolean doInBackground(String... paths) {
 				try {
 					String response = App.fb.request(paths[0]);
 					JSONArray data = new JSONObject(response).getJSONArray("data");
 					for(int i = 0; i < data.length(); i++) {
 						JSONObject json = data.getJSONObject(i);
 						
 						FriendEntry entry = new FriendEntry();
 						entry.name = json.getString("name");
 						entry.id = json.getString("id");
 						entry.icon = Util.urlToImage(new URL("http://graph.facebook.com/" + entry.id + "/picture"));
 						
 						entries.add(entry);
 						
 						publishProgress(i);
 					}
 				}
 				catch(IOException e) {
 					Log.e(App.TAG, e.toString());
 				}
 				catch(JSONException e) {
 					Log.e(App.TAG, e.toString());
 				}
 				return null;
 			}
 			
 			@Override
 			protected void onProgressUpdate(Integer... progresses) {
 				adapter.notifyDataSetChanged();
 			}
		};
		
		async.execute("me/friends");
 		
 		AlertDialog dialog = new AlertDialog.Builder(this).setAdapter(adapter, new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
				async.cancel(true);
				dialog.dismiss();
				
 				FriendEntry entry = entries.get(which);
 				openShareDialog(entry.id);
 			}
 		}).setOnCancelListener(new OnCancelListener() {
 			@Override
 			public void onCancel(DialogInterface arg0) {
 				Toast.makeText(BookActivity.this, android.R.string.cancel, App.TOAST_TIME).show();
 			}
 		}).setTitle(R.string.title_select_friend).create();
 		dialog.show();
 	}
 
     private AlertDialog dialog;
     
 	@Override
 	public void onClick(View v) {
 		if(dialog == null)
 			dialog = new AlertDialog.Builder(BookActivity.this).create();
 		dialog.setMessage(entry.info.reviews.get(v.getId()));
 		dialog.show();
 		((TextView)dialog.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
 	}
 }
