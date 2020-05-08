 package com.baiboly.katolika;
 
 import java.util.ArrayList;
 import java.util.TreeSet;
 import java.lang.*;
 
 
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.database.Cursor;
 import android.graphics.Typeface;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.text.ClipboardManager;
 import android.text.Spannable;
 import android.text.style.BackgroundColorSpan;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.GestureDetector;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.ViewTreeObserver;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.text.Html;
 import android.text.Spanned;
 
 
 import com.baiboly.katolika.providers.BookmarksProviderWrapper;
 
 public class SearchResultActivity extends BaseActivity  {
 	public static final int MENU_ITEM_COPY = Menu.FIRST;
 	public static final int MENU_ITEM_SHARE = Menu.FIRST + 1;
 	public static final int MENU_ITEM_BOOKMARK_VERSES = Menu.FIRST + 2;
 	public static final int MENU_ITEM_BOOKMARKS = Menu.FIRST + 3;
 	public static final int MENU_ITEM_CLEAR = Menu.FIRST + 4;
 	
 	private static final int FLIP_PIXEL_THRESHOLD = 200;
 	private static final int FLIP_TIME_THRESHOLD = 400;
 	
 	private Book book;
 	private int chapterId;
     private int recordsPerPage;
     private int searchPage;
     private int total;
     
     private String searchText;
     private int searchBook;
 	ArrayList<Integer> verseIds = null;
 	private ProgressDialog dialog;
 	
 	private DataBaseAdapter adapter;
 	private Cursor cursor;
     private int noOfPages;
 
 	
 	private static boolean preferenceChanged = false;
 	
 	
 
 	
 	int[] tl1Heights;
 	int[] tl2Heights;
 	
 	private BackgroundColorSpan selectionSpan = new BackgroundColorSpan(ThemeUtils.getSelectionResource());
 	private ArrayList<String> selectedVerses = new ArrayList<String>();
 	
 	private boolean isVerseView = false;
 	private GestureDetector mGestureDetector;
 	private VerseLongClickHandler mVerseLongClickHandler;
 	
 	private boolean bookmarkOnLongPress = false;
 	
 	public static void setPreferenceChanged(boolean preferenceChanged) {
 		SearchResultActivity.preferenceChanged = preferenceChanged;
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 	    super.onCreate(savedInstanceState);
 	    setTheme(ThemeUtils.getThemeResource());
 	    setContentView(R.layout.searchresult);
 	    
 	    selectedVerses.clear();
 	    
 	    Bundle extras = getIntent().getExtras();
         this.searchText = extras.getString("com.baiboly.katolika.searchText");
         
         this.recordsPerPage = 20;
         this.searchPage = 1;
 	    if(extras == null) {
 	    	/*
             no search specified
             */
 	    }
 	    else {
 	        this.searchText = extras.getString("com.baiboly.katolika.searchText");
 	        if(extras.containsKey("com.baiboly.katolika.searchBook")) {
 	        	this.searchBook = extras.getInt("com.baiboly.katolika.searchBook");
                 
 	        }
             else
             {
                 this.searchBook = 0;
             }
             /*
             if(extras.containsKey("com.baiboly.katolika.searchPage")) {
                 searchPage = extras.getInt("com.baiboly.katolika.searchPage");
             }
             */
 	    }
 	    
 	    preferenceChanged = false;
 	    
 	    mGestureDetector = new GestureDetector(this, new GestureListener());
 	    mVerseLongClickHandler = new VerseLongClickHandler();
 	    
 	    getContent(); 
 	}
 	
 	@Override
 	protected void onStart() {
 		super.onStart();
 		
 		if(preferenceChanged) {
 			preferenceChanged = false;
 			selectionSpan = new BackgroundColorSpan(ThemeUtils.getSelectionResource());
 			getContent();
 		}
 	}
 	
 	private void getContent() {
 		
 		tl1Heights = null;
 		tl2Heights = null;
 		
 		dialog = ProgressDialog.show(SearchResultActivity.this, "", "Andraso kely...");
 		new WorkerThread().start();
 	}
 	
 	private void showContent() {
 		selectedVerses.clear();
 		
 		final Preference pref = Preference.getInstance(this);
 		int renderingFix = pref.getRendering();
 		float fontSize = pref.getFontSize();
 		bookmarkOnLongPress = pref.isBookmarkOnLongPress();
 		
 		showSingleLanguage(renderingFix, fontSize);
 				
 	}
 
 	private void showHideBackToChapter() {
 		TextView backToChapter = (TextView) findViewById(R.id.backToChapter);
 		if(this.isVerseView) {
 			backToChapter.setVisibility(View.VISIBLE);
 		}
 		else {
 			backToChapter.setVisibility(View.GONE);
 		}
 	}
         
 	private void showSingleLanguage(int renderingFix, float fontSize) {	
 		setTheme(ThemeUtils.getThemeResource());
 		setContentView(R.layout.searchresult);
 		
 		if(cursor == null || cursor.isClosed()) {
 			return;
 		}
 		
 		setupToolbar();
         		
 		Resources res = getResources();
 		
 		TextView tv = (TextView) findViewById(R.id.heading);
			tv.setText("Valiny");
 		
 		tv = (TextView) findViewById(R.id.chapterNumber);
 		//tv.setTextSize(fontSize);
 		
			tv.setText(res.getString(R.string.searchResultTotal) + " " + this.total);
 		
 		cursor.moveToFirst();
 		
 		int rowLayout = R.layout.verserow;
 				
 		TableLayout tl = (TableLayout) findViewById(R.id.chapterLayout);
 		tl.removeAllViews();
 		
 		LayoutInflater inflater = getLayoutInflater();
 		// a.b_and , a.b_text , a.b_toko, b.b_sname,b._id as b_id
 		while (!cursor.isAfterLast()) {
 			int verseId = cursor.getInt(0);
 			String verse = cursor.getString(1);
             int chapterId = cursor.getInt(2);
             String bookName = cursor.getString(3);
             int bookId = cursor.getInt(4);
 		
 			TableRow tr = (TableRow)inflater.inflate(rowLayout, tl, false);
 			TextView t = (TextView) tr.findViewById(R.id.verse);
 			
 			t.setTextSize(fontSize);
 			
             Spanned tmptext = Html.fromHtml("<b>" + bookName + " " + chapterId + ":" + verseId + "</b> " + verse);
             
 			t.setText(tmptext, TextView.BufferType.SPANNABLE);
 			t.setTag(bookName+":"+bookId+":"+chapterId+":"+verseId);
 			setVerseOnLongClickHandler(t);
 			
 			tl.addView(tr);
 			
 			cursor.moveToNext();
 		}
 		
 		cursor.close();
 		adapter.close();
 		
 		showHideBackToChapter();
 	}
     
  
 	
 	private void setupToolbar() {
 		
 //jerena raha page voalohany dia tsy misy sinon misy mandeha am page mialoha	    
 	    Button previous = (Button) findViewById(R.id.prevButton);
         if(this.searchPage > 1 ) {
         	previous.setVisibility(View.VISIBLE);
         	previous.setOnClickListener(new View.OnClickListener() {
     			public void onClick(View v) {
     				showPrevious();
     			}
     		});
         }
         else {
         	previous.setVisibility(View.GONE);
         }
         
         Button next = (Button) findViewById(R.id.nextButton);
         if(this.searchPage < this.noOfPages ) {
         	next.setVisibility(View.VISIBLE);
         	next.setOnClickListener(new View.OnClickListener() {
     			public void onClick(View v) {
     				showNext();
     			}
     		});
         }
         else {
         	next.setVisibility(View.GONE);
         }
 	}
 	
 	private class WorkerThread extends Thread {
 		@Override
         public void run() {
 			Preference pref = Preference.getInstance(SearchResultActivity.this);
 			
 			adapter = new DataBaseAdapter(SearchResultActivity.this);
 			adapter.open();
 			
 			if(cursor != null && !cursor.isClosed()) {
 				cursor.close();
 			}
 			
 			
 			String table = "verses";
 			
                         
             if(searchBook == 0) {
                 cursor = adapter.fetchVerses(searchText, recordsPerPage, (searchPage - 1)*recordsPerPage);
                 total = adapter.countVerses(searchText);
                 noOfPages = (int) Math.ceil(total * 1.0 / recordsPerPage);
             }
             else {
                 cursor = adapter.fetchVerses(searchText, searchBook, recordsPerPage, (searchPage - 1)*recordsPerPage);
                 total = adapter.countVerses(searchText, searchBook);
                 noOfPages = (int) Math.ceil(total * 1.0 / recordsPerPage);
                 
 
 
             }
 			
             
 			/* pref.setLastBook(book.getId());
 			pref.setLastChapter(chapterId);
 			 */
 			
 			
             handler.sendEmptyMessage(0);
         }
 
         private Handler handler = new Handler() {
             @Override
             public void handleMessage(Message msg) {
                 runOnUiThread(new Runnable() {
 					public void run() {
 						showContent();
 					}
 				});
                 dialog.dismiss();
             }
         };
 	}
 
 	
 	
 	@Override
     public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
 		if(selectedVerses.size() > 0) {
 			menu.add(0, MENU_ITEM_COPY, 0, R.string.copy);
 			menu.add(0, MENU_ITEM_SHARE, 0, R.string.share);
 			menu.add(0, MENU_ITEM_CLEAR, 0, R.string.clear);
 			menu.add(0, MENU_ITEM_BOOKMARK_VERSES, 0, R.string.bookmarkVerses);
 		}
 		menu.add(0, MENU_ITEM_BOOKMARKS, 0, R.string.bookmarks);
 	}
 	
 	@Override
     public boolean onContextItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case MENU_ITEM_COPY: {
             	copySelectedVerses(getSelectedVerseText());
                 return true;
             }
             case MENU_ITEM_SHARE: {
             	shareSelectedVerses(getSelectedVerseText());
                 return true;
             }
             case MENU_ITEM_CLEAR: {
             	clearSelectedVerses();
                 return true;
             }
             case MENU_ITEM_BOOKMARK_VERSES: {
             	addBookmarkVerses();
                 return true;
             }
             case MENU_ITEM_BOOKMARKS: {
             	openBookmarksActivity(this);
                 return true;
             }
         }
         return false;
     }
 	
 	private void addBookmarkVerses() {
 		if(selectedVerses.size() == 0) {
 			return;
 		}
 		
 		for(String sid : selectedVerses) {
 			try {
                 String[] tokens = sid.split(":");
                 // bookName+"|"+bookId+"|"+chapterId+"|"+verseId
                 
                 String title = tokens[0] + " " + tokens[2] + ":" + tokens[3];
                 String url = tokens[1] + ":" + tokens[2] + ":" + tokens[3];
                 
                 
                 BookmarksProviderWrapper.setAsBookmark(getContentResolver(), -1, title, url, true);
 			}
 			catch(Exception e){}
 		}
 		
 		Toast.makeText(SearchResultActivity.this, "Voatahiry ny andininy", Toast.LENGTH_SHORT).show();
 	}
 	
 
 	public void onAppMenuClickEvent(View sender)
 	{
             registerForContextMenu(sender); 
             openContextMenu(sender);
             unregisterForContextMenu(sender);
 
 	}
 	
 	public void onAppMenuLeftClickEvent(View sender)
 	{
             startActivity(new Intent(SearchResultActivity.this, SearchViewActivity.class));
             //startActivity(new Intent(this, SearchViewActivity.class));
 	}
 	
 	public void onBackToChapterClickEvent(View sender) {
 		this.isVerseView = false;
 		getContent();
 	}
 	
 	public void onVerseSelected(View sender) {
         selectedVerses.clear();
         
 		try {
 			TextView tv = (TextView) sender;
 			Spannable sText = (Spannable) tv.getText();
 			if(sText.getSpanStart(selectionSpan) > -1) {
 				sText.removeSpan(selectionSpan);
 				selectedVerses.remove((String) tv.getTag());
 			}
 			else {
 				sText.setSpan(selectionSpan, 0, sText.length(), 0);
 				selectedVerses.add((String) tv.getTag());
 			}
 		}
 		catch(Exception e){}
         
         
 	}
 	
 	private void clearSelectedVerses() {
 		TableLayout tl = (TableLayout) findViewById(R.id.chapterLayout);
 		
 		Preference pref = Preference.getInstance(SearchResultActivity.this);
 		if(pref.getSecLanguage() == Preference.LANG_NONE) {
 			removeSpan(tl, 1, 0);
 		}
 		else {
 			if(pref.getLanguageLayout() == Preference.LAYOUT_BOTH_VERSE) {
 				removeSpan(tl, 2, 0);
 				removeSpan(tl, 2, 1);
 			}
 			else {
 				removeSpan(tl, 1, 0);
 				tl = (TableLayout) findViewById(R.id.chapterLayoutSec);
 				removeSpan(tl, 1, 0);
 			}
 		}
 		
 		selectedVerses.clear();
 	}
 
 	private void removeSpan(TableLayout tl, int level, int childIndex) {
 		int count = tl.getChildCount();
 		TextView tv = null;
 		for(int i=0; i<count; i++) {
 			if(level == 1) {
 				tv = (TextView) ((TableRow)tl.getChildAt(i)).getChildAt(childIndex);
 			}
 			else {
 				tv = (TextView) ((LinearLayout) ((TableRow)tl.getChildAt(i)).getChildAt(0)).getChildAt(childIndex);
 			}
 			if(tv != null) {
 				try {
 					Spannable sText = (Spannable) tv.getText();
 					sText.removeSpan(selectionSpan);
 				}
 				catch(Exception e){}
 			}
 		}
 	}
 	
 	private void copySelectedVerses(String text) {
 		ClipboardManager ClipMan = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
 		ClipMan.setText(text);
 		Toast.makeText(SearchResultActivity.this, "Voadika ny andininy nofidina", Toast.LENGTH_SHORT).show();
 	}
 	
 	private String getSelectedVerseText() {
 		StringBuilder sb = new StringBuilder();
 
 		Preference pref = Preference.getInstance(SearchResultActivity.this);
 		
 		adapter = new DataBaseAdapter(SearchResultActivity.this);
 		adapter.open();
 		
 		if(cursor != null && !cursor.isClosed()) {
 			cursor.close();
 		}
 		
 		
 		String table = "verses";
 		
 		cursor = adapter.fetchVerses(selectedVerses);
 		// a.b_and , a.b_text , a.b_toko, b.b_sname,b._id as b_id
 		boolean hasFirstLangSelection = false;
 		String link = "http://baiboly.katolika.org/boky/";
 		if(cursor != null && !cursor.isClosed()) {
 			cursor.moveToFirst();
 			
 			if(!cursor.isAfterLast()) {
 				hasFirstLangSelection = true;
 				
                 sb.append(cursor.getString(3));
 				
                 link += cursor.getString(3);
 				sb.append(" ");
                 link += "/";
 				
 			}
 			
 			while(!cursor.isAfterLast()) {
 				sb.append(cursor.getString(2)).append(":").append(cursor.getString(0)).append(" ");
                 link += cursor.getString(2) + "/" + cursor.getString(0);
 				sb.append(cursor.getString(1)).append("\n");
                 
 				cursor.moveToNext();
 			}
 			cursor.close();
 		}
 		
         adapter.close();
         link = link.replace(" ", "+");
 		sb.append(" " + link);
 		return sb.toString();
 	}
 	
 	private void shareSelectedVerses(String text) {
 		copySelectedVerses(text);
 		
 		Intent sharingIntent = new Intent(Intent.ACTION_SEND);
 		sharingIntent.setType("text/plain");
 		sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, text);
 		sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Avy ao amin'ny baiboly katolika");
 		startActivity(Intent.createChooser(sharingIntent, "Zarao amin'ny"));
 	}
 	
     /*
 	@Override
     protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
         if (requestCode == OPEN_BOOKMARKS_ACTIVITY) {
         	Book book = getBookFromBookmark(intent);
         	if(book != null) {
         		this.book = book;
 	    		this.chapterId = book.getSelectedChapterId();
 	    		this.verseIds = book.getSelectedVerseIds();
 	    		getContent();
         	}        	
         }
 	}
     */
     
 	private void showPrevious() {
 		if(this.searchPage > 1 ) {
             this.searchPage--;
 			getContent();
 		}
 	}
 
 	private void showNext() {
 		if(this.searchPage < this.noOfPages ) {
             this.searchPage++;
 			getContent();
 		}
 	}
 
 	private class GestureListener extends GestureDetector.SimpleOnGestureListener {
 		@Override
 		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,	float velocityY) {
 			if (e2.getEventTime() - e1.getEventTime() <= FLIP_TIME_THRESHOLD) {
 				if (e2.getX() > (e1.getX() + FLIP_PIXEL_THRESHOLD)) {
 					showPrevious();
 					return true;
 				}
 
 				// going forwards: pushing stuff to the left
 				if (e2.getX() < (e1.getX() - FLIP_PIXEL_THRESHOLD)) {
 					showNext();
 					return true;
 				}
 			}
 			
 			return super.onFling(e1, e2, velocityX, velocityY);
 		}
 	}
 	
 	@Override
 	public boolean dispatchTouchEvent(MotionEvent ev) {
 		if(!mGestureDetector.onTouchEvent(ev)) {
 			return super.dispatchTouchEvent(ev);
 		}
 		
 		return true;
 	}
 	
 	private void setVerseOnLongClickHandler(View v) {
 		if(bookmarkOnLongPress) {
 			v.setOnLongClickListener(mVerseLongClickHandler);
 		}
 	}
 	
 	private class VerseLongClickHandler implements View.OnLongClickListener {
 		@Override
 		public boolean onLongClick(View v) {
             
 			try {
                 
 				TextView tv = (TextView) v;
 				Spannable sText = (Spannable) tv.getText();
 				if(sText.getSpanStart(selectionSpan) == -1) {
 					sText.setSpan(selectionSpan, 0, sText.length(), 0);
 					selectedVerses.add((String) tv.getTag());
 				}
 			}
 			catch(Exception e){}
 			
 			addBookmarkVerses();
 			return true;
 		}		
 	}
 }
