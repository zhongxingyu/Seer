 package org.csie.mpp.buku;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.csie.mpp.buku.db.BookEntry;
 import org.csie.mpp.buku.db.DBHelper;
 import org.csie.mpp.buku.helper.SearchSuggestionProvider;
 import org.csie.mpp.buku.view.BookshelfManager;
 import org.csie.mpp.buku.view.BookshelfManager.BookEntryAdapter;
 import org.csie.mpp.buku.view.ViewManager.ViewListener;
 import org.csie.mpp.buku.view.FriendsManager;
 import org.csie.mpp.buku.view.StreamManager;
 import org.csie.mpp.buku.view.ViewPageFragment;
 import org.csie.mpp.buku.view.ViewPagerAdapter;
 
 import android.app.AlertDialog;
 import android.app.SearchManager;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.content.DialogInterface.OnMultiChoiceClickListener;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.provider.SearchRecentSuggestions;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.view.ViewPager;
 import android.support.v4.view.ViewPager.OnPageChangeListener;
 import android.text.util.Linkify;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.facebook.android.BaseDialogListener;
 import com.facebook.android.SessionStore;
 import com.flurry.android.FlurryAgent;
 import com.markupartist.android.widget.ActionBar;
 import com.markupartist.android.widget.ActionBar.IntentAction;
 import com.viewpagerindicator.TitlePageIndicator;
 
 public class MainActivity extends FragmentActivity implements OnPageChangeListener {
 	private static final String PREFS_PAGE_IDX = "VIEWPAGER_INDEX";
 	
 	private DBHelper db;
 	private SharedPreferences prefs;
 	
 	private ActionBar actionbar;
 	
 	private TitlePageIndicator indicator;
 	private ViewPager viewpager;
 	private ViewPagerAdapter viewpagerAdapter;
 	
 	private ViewPageFragment bookshelf, stream, friends;
 	
 	private BookshelfManager bookMan;
 	private StreamManager streamMan;
 	private FriendsManager friendsMan;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 		
         // initialize FB
         SessionStore.restore(App.fb, this);
 
         db = new DBHelper(this);
         prefs = getPreferences(MODE_PRIVATE);
 
         /* initialize ActionBar */
         actionbar = (ActionBar)findViewById(R.id.actionbar);
         actionbar.addAction(new IntentAction(this, new Intent(this, ScanActivity.class), R.drawable.ic_camera, ScanActivity.REQUEST_CODE));
 
         /* initialize ViewPageFragments */
         viewpagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
         
         bookMan = new BookshelfManager(this, db, new ViewListener() {
         	@Override
         	public void onListViewCreated(ListView view) {
         		registerForContextMenu(view);
         		view.setOnItemClickListener(new OnItemClickListener() {
         			@Override
         			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
         				startBookActivity(bookMan.get(position).isbn);
         			}
         		});
         	}
         });
         bookshelf = new ViewPageFragment(getString(R.string.title_bookshelf), bookMan);
         viewpagerAdapter.addItem(bookshelf);
         
         /* initialize ViewPager */
         indicator = (TitlePageIndicator)findViewById(R.id.indicator);
         viewpager = (ViewPager)findViewById(R.id.viewpager);
 
         viewpager.setAdapter(viewpagerAdapter);
         indicator.setViewPager(viewpager);
         indicator.setOnPageChangeListener(this);

    	App.fb.extendAccessTokenIfNeeded(this, null);
     	
     	if(App.fb.isSessionValid())
         	createSessionView();
         else {
         	if(prefs.getBoolean(PREFS_ACCOUNT_ITEM + ITEM_FB, false))
         		loginFacebook();
         }
     }
     
     @Override
     public void onResume() {
     	super.onResume();
 
     	App.fb.extendAccessTokenIfNeeded(this, null);
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
     protected void onNewIntent(Intent intent) {
     	super.onNewIntent(intent);
 
         if(Intent.ACTION_SEARCH.equals(intent.getAction())) {
         	String query = intent.getStringExtra(SearchManager.QUERY);
         	SearchRecentSuggestions suggestions = new SearchRecentSuggestions(
         		this, SearchSuggestionProvider.AUTHORITY, SearchSuggestionProvider.MODE
         	);
         	suggestions.saveRecentQuery(query, null);
         	showSearchResult(query);
         }
     }
 	 
 	private void showSearchResult(String query) {
 		final List<BookEntry> entries = new ArrayList<BookEntry>();
 		for(BookEntry entry: BookEntry.search(db.getReadableDatabase(), query))
 			entries.add(entry);
 		if(entries.size() == 0) {
 			Toast.makeText(this, R.string.search_no_result, App.TOAST_TIME).show();
 			return;
 		}
 		BookEntryAdapter adapter = new BookEntryAdapter(this, R.layout.list_item_book, entries);
 		AlertDialog dialog = new AlertDialog.Builder(this).setAdapter(adapter, new OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int position) {
 				dialog.dismiss();
 				
 				String isbn = entries.get(position).isbn;
 				startBookActivity(isbn);
 			}
 		}).setTitle(R.string.title_search_result).create();
 		dialog.show();
 	}
     
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
     	switch(requestCode) {
     		case ScanActivity.REQUEST_CODE:
     			if(resultCode == RESULT_OK) {
     				String isbn = data.getStringExtra(App.ISBN);
     				if(isbn.contains("BukuDroid")) {
     					FlurryAgent.logEvent(App.FlurryEvent.FAN_PAGE_OPENED.toString());
     					/* Tricks for our fan page. */
     					Uri uri = Uri.parse(App.FB_FAN_PAGE);
     					Intent intent = new Intent(Intent.ACTION_VIEW, uri);
     					startActivity(intent);
     				}
     				else {
     					startBookActivity(isbn, true);
     				}
     			}
     			break;
     		case BookActivity.REQUEST_CODE:
     			if(resultCode == BookActivity.RESULT_ADD) {
     				String isbn = data.getStringExtra(App.ISBN);
     				bookMan.add(isbn);
 					indicator.setCurrentItem(0);
 					Toast.makeText(this, getString(R.string.msg_book_added), App.TOAST_TIME).show();
     			}
     			else if(resultCode == BookActivity.RESULT_ISBN_INVALID) {
     				Toast.makeText(this, getString(R.string.msg_invalid_isbn), App.TOAST_TIME).show();
     			}
     			else if(resultCode == BookActivity.RESULT_NOT_FOUND) {
     				Toast.makeText(this, getString(R.string.msg_book_not_found), App.TOAST_TIME).show();
     			}
 				else if(resultCode == BookActivity.RESULT_DELETE) {
 					String isbn = data.getStringExtra(App.ISBN);
 					BookEntry entry = bookMan.get(isbn);
 					deleteBookEntry(entry);
 				}
     			break;
     		default:
     			break;
     	}
     	
     	App.fb.authorizeCallback(requestCode, resultCode, data);
     }
     
     private void startBookActivity(String isbn) {
     	startBookActivity(isbn, false);
     }
     
     private void startBookActivity(String isbn, boolean checkDuplicate) {
     	Intent intent = new Intent(this, BookActivity.class);
 		intent.putExtra(App.ISBN, isbn);
 		intent.putExtra(BookActivity.CHECK_DUPLICATE, checkDuplicate);
 		startActivityForResult(intent, BookActivity.REQUEST_CODE);
     }
     
     private void deleteBookEntry(BookEntry entry) {
     	if(entry.delete(db.getWritableDatabase()))
     		Toast.makeText(MainActivity.this, getString(R.string.msg_deleted), App.TOAST_TIME).show();
     	else {
 			Toast.makeText(MainActivity.this, getString(R.string.msg_delete_failed) + entry.title, App.TOAST_TIME).show();
 			Log.e(App.TAG, "Delete failed \"" + entry.isbn + "\".");
 		}
 		bookMan.remove(entry);
     }
     
     @Override
     public void onDestroy() {
     	super.onDestroy();
     	
     	db.close();
     }
     
     private void createSessionView() {
     	streamMan = new StreamManager(this, db);
     	stream = new ViewPageFragment(getString(R.string.title_stream), streamMan);
 		viewpagerAdapter.addItem(stream);
 		
 		friendsMan = new FriendsManager(this, db, streamMan);
 		friends = new ViewPageFragment(getString(R.string.title_friends), friendsMan);
 		viewpagerAdapter.addItem(friends);
 		
 		viewpagerAdapter.notifyDataSetChanged();
 		indicator.setCurrentItem(prefs.getInt(PREFS_PAGE_IDX, 1));
     }
     
     private void removeSessionView() {
     	viewpagerAdapter.removeItem(stream);
     	viewpagerAdapter.removeItem(friends);
     	
     	viewpagerAdapter.notifyDataSetChanged();
     	
     }
     
     private void loginFacebook() {
 		App.fb.authorize(MainActivity.this, App.FB_APP_PERMS, new BaseDialogListener(MainActivity.this, App.TOAST_TIME) {
 			@Override
 			public void onComplete(Bundle values) {
 				Toast.makeText(MainActivity.this, R.string.msg_login_success, App.TOAST_TIME).show();
 				SessionStore.save(App.fb, MainActivity.this);
 				MainActivity.this.runOnUiThread(new Runnable() {
 					@Override
 					public void run() {
 						createSessionView();
 					}
 				});
 			}
 		});
     }
 
     /* --- OptionsMenu			(start) --- */
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
     	MenuInflater inflater = getMenuInflater();
     	inflater.inflate(R.menu.main, menu);
     	return true;
     }
     
     @Override
     public boolean onPrepareOptionsMenu(Menu menu) {
     	switch(prefs.getInt(PREFS_PAGE_IDX, 0)) {
     		case 0:
     			menu.setGroupVisible(R.id.menu_books, true);
     			menu.setGroupVisible(R.id.menu_streams, false);
     			menu.setGroupVisible(R.id.menu_friends, false);
     			break;
     		case 1:
     			menu.setGroupVisible(R.id.menu_books, false);
     			menu.setGroupVisible(R.id.menu_streams, true);
     			menu.setGroupVisible(R.id.menu_friends, false);
     			break;
     		case 2:
     			menu.setGroupVisible(R.id.menu_books, false);
     			menu.setGroupVisible(R.id.menu_streams, false);
     			menu.setGroupVisible(R.id.menu_friends, true);
     			break;
     	}
     	return true;
     }
     
     private static final String PREFS_ACCOUNT_ITEM = "ACCOUNT_SYNC";
     private static final int ITEM_FB = 0;
     
     private int[] itemIds = { ITEM_FB };
     private String[] accountItems;
     private boolean[] checked;
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
     	switch(item.getItemId()) {
     		case R.id.menu_search:
     			SearchManager sm = (SearchManager)getSystemService(SEARCH_SERVICE);
     			if(sm != null)
     				sm.startSearch(null, false, getComponentName(), null, false); 
     			break;
     		case R.id.menu_invite:
     			Bundle params = new Bundle();
     			params.putString("message", getString(R.string.msg_invite));
     			
     			App.fb.dialog(this, "apprequests", params, new BaseDialogListener(this, App.TOAST_TIME) {
 					@Override
 					public void onComplete(Bundle values) {
 						Log.d("Yi", values.toString());
 						if(values.containsKey("post_id"))
 							Toast.makeText(MainActivity.this, R.string.fb_message_posted, App.TOAST_TIME).show();
 					}
 				});
     			break;
     		case R.id.menu_account_sync:
     			if(accountItems == null) {
     				accountItems = getResources().getStringArray(R.array.list_account);
     				checked = new boolean[itemIds.length];
     				for(int i = 0; i < itemIds.length; i++)
     					checked[i] = prefs.getBoolean(PREFS_ACCOUNT_ITEM + i, false);
     			}
     			new AlertDialog.Builder(this).setTitle(R.string.title_account_sync).setMultiChoiceItems(accountItems, checked, new OnMultiChoiceClickListener() {
 					@Override
 					public void onClick(final DialogInterface dialog, int which, boolean isChecked) {
 						switch(which) {
 							case ITEM_FB:
 								if(isChecked)
 									loginFacebook();
 								else {
 									try {
 										App.fb.logout(MainActivity.this);
 										Toast.makeText(MainActivity.this, R.string.msg_logout_success, App.TOAST_TIME).show();
 										SessionStore.clear(MainActivity.this);
 										removeSessionView();
 									}
 									catch(Exception e) {
 										Log.e(App.TAG, "Logout failed: " + e.toString());
 									}
 								}
 								updateStatus(itemIds[which], isChecked);
 								break;
 							default:
 								break;
 						}
 					}
 					private void updateStatus(int id, boolean isChecked) {
 						Editor editor = prefs.edit();
 						editor.putBoolean(PREFS_ACCOUNT_ITEM + id, isChecked);
 						editor.commit();
 					}
     			}).create().show();
     			break;
     		case R.id.menu_about:
     			View view = getLayoutInflater().inflate(R.layout.about, null);
     			Linkify.addLinks((TextView)view.findViewById(R.id.link), Linkify.ALL);
     			new AlertDialog.Builder(this).setTitle(R.string.title_about_bukudroid).setView(view).create().show();
     			break;
     		default:
     			break;
     	}
     	return true;
     }
     /* --- OptionsMenu			(end) --- */
 
     /* --- ContextMenu			(start) --- */
     private static final int MENU_INFO = 0;
 	private static final int MENU_DELETE = 1;
 	
 	private int[] menuIds = { MENU_INFO, MENU_DELETE };
 	private String[] menuItems;
     
     @Override
     public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
     	int position = ((AdapterView.AdapterContextMenuInfo)menuInfo).position;
 		if(menuItems == null)
 			menuItems = getResources().getStringArray(R.array.list_longclick);
 		for(int i = 0; i < menuIds.length; i++)
 			menu.add(0, menuIds[i], 0, menuItems[i]);
 		BookEntry entry = bookMan.get(position);
 		menu.setHeaderTitle(entry.title);
     }
     
     @Override
     public boolean onContextItemSelected(MenuItem item) {
 		int position = ((AdapterView.AdapterContextMenuInfo)item.getMenuInfo()).position;
 		BookEntry entry = bookMan.get(position);
 		
     	switch(item.getItemId()) {
     		case MENU_INFO:
     			startBookActivity(entry.isbn);
     			break;
 			case MENU_DELETE:
 				deleteBookEntry(entry);
 				break;
 			default:
 				break;
 		}
     	return true;
     }
     /* --- ContextMenu			(end) --- */
 
     /* --- OnPageChageListener	(start) --- */
 	@Override
 	public void onPageScrollStateChanged(int state) {}
 
 	@Override
 	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
 
 	@Override
 	public void onPageSelected(int position) {
 		Editor editor = prefs.edit();
 		editor.putInt(PREFS_PAGE_IDX, position);
 		editor.commit();
 	}
 	/* --- OnPageChageListener	(end) --- */
 }
