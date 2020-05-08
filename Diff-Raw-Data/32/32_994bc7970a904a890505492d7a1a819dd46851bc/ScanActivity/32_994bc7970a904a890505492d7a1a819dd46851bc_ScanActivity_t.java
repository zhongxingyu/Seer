 package org.csie.mpp.buku;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.csie.mpp.buku.db.BookEntry;
 import org.csie.mpp.buku.view.BookshelfManager.SearchEntryAdapter;
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 import com.flurry.android.FlurryAgent;
 import com.google.zxing.client.android.CaptureActivity;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.app.TabActivity;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.pm.ActivityInfo;
 import android.content.res.Resources;
 import android.os.Bundle;
 import android.os.Handler;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Spinner;
 import android.widget.TabHost;
 import android.widget.Toast;
 import android.widget.TabHost.OnTabChangeListener;
 import android.widget.TabWidget;
 
 public class ScanActivity extends TabActivity implements OnTabChangeListener {
 	public static final int REQUEST_CODE = 1436;
 
 	private int tab_height;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.scan);
         setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
 
         Resources res = getResources();
         TabHost tabhost = getTabHost();
 
        
         
         // [Yi] Notes: ISBN Input should always has smaller index than Barcode Scanner
         // an unknown bug that cause soft-keyboard can't be set visible
         
         // tab: ISBN Input
         Intent intent = new Intent(this, IsbnInputActivity.class);
         String title = getString(R.string.tab_isbn);
         TabHost.TabSpec spec = tabhost.newTabSpec(title).setIndicator(title, res.getDrawable(R.drawable.ic_menu_text)).setContent(intent);
         tabhost.addTab(spec);
         
         // tab: Barcode Scanner
         intent = new Intent("com.google.zxing.client.android.BUKU_SCAN");
         intent.putExtra("SCAN_MODE", "ONE_D_MODE");
         title = getString(R.string.tab_barcode);
         spec = tabhost.newTabSpec(title).setIndicator(title, res.getDrawable(R.drawable.ic_menu_barcode)).setContent(intent);
         tabhost.addTab(spec);
         
          // tab: keyword Input
         intent = new Intent(this, KeywordSearchActivity.class);
         title = getString(R.string.tab_search);
         spec = tabhost.newTabSpec(title).setIndicator(title, res.getDrawable(R.drawable.ic_menu_search)).setContent(intent);
         tabhost.addTab(spec);
         
         tabhost.setCurrentTab(1);
         tabhost.setOnTabChangedListener(this);
         
         TabWidget tabwidget = tabhost.getTabWidget();
         int count = tabwidget.getChildCount();
         for(int i = 0; i < count; i++) {
         	int height = tabwidget.getChildAt(i).getLayoutParams().height;
         	if(height > tab_height)
         		tab_height = height;
         }
         
         CaptureActivity.TAB_HEIGHT = tab_height;
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
 	public void onTabChanged(String tabId) {
 		// [Yi] Notes: prevent soft-keyboard to show on other view (such as barcode scanner)
 		InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
 		if(tabId.equals(getString(R.string.tab_isbn)) || tabId.equals(getString(R.string.tab_search)))
 			imm.toggleSoftInput(0, InputMethodManager.SHOW_IMPLICIT);
 		else {
 			if(imm != null)
 				imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
 		}
 	}
     
     public static abstract class AbstractTabContentActivity extends Activity {
     	// [Yi] Notes: a work-around for TabActivity
     	// a problems that cause resultCode being RESULT_CANCEL
     	protected void setResultForTabActivity(int resultCode, Intent data) {
     		Activity parent = getParent();
         	if(parent == null)
         		setResult(resultCode, data);
         	else
         		parent.setResult(resultCode, data);
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
     }
     
     public static final class IsbnInputActivity extends AbstractTabContentActivity implements OnClickListener {
     	private EditText input;
     	
     	@Override
     	public void onCreate(Bundle savedInstanceState) {
     		super.onCreate(savedInstanceState);
     		setContentView(R.layout.isbn);
     		
     		((Button)findViewById(R.id.ok)).setOnClickListener(this);
     		
     		input = (EditText)findViewById(R.id.isbn);
     	}
 
 		@Override
 		public void onClick(View v) {
         	Intent data = new Intent();
         	data.putExtra(App.ISBN, input.getText().toString());
         	setResultForTabActivity(RESULT_OK, data);
             finish();
 		}
     }
     public static final class KeywordSearchActivity extends AbstractTabContentActivity implements OnClickListener {
     	private final static int MAX_RESULT = 10;
     	private EditText input;
     	private Spinner spinner;
     	
     	@Override
     	public void onCreate(Bundle savedInstanceState) {
     		super.onCreate(savedInstanceState);
     		setContentView(R.layout.keyword);
     		((Button)findViewById(R.id.search_button)).setOnClickListener(this);
     		
     		spinner = (Spinner)findViewById(R.id.keyword_type);
     		input = (EditText)findViewById(R.id.keyword);
     	}
 
 		@Override
 		public void onClick(View v) {
 			
 			if( input.getText().toString().equals("") )
 			{
 				Toast.makeText(KeywordSearchActivity.this, R.string.search_no_result, App.TOAST_TIME).show();
 				return;
 			}
 			
 			final ProgressDialog progressDialog = ProgressDialog.show(KeywordSearchActivity.this, getString(R.string.key_search), getString(R.string.key_searching));
 			final Handler handler = new Handler();
 			
 			// TODO: change to AsyncTask
 			Thread thread = new Thread(){
 				URL url;
 				String str;
 				int type = spinner.getSelectedItemPosition();
 				String keyword = input.getText().toString();
 				String prefix;
 				
 				@Override
 				public void run(){
 					try {
 						switch(type)
 						{
 						case 0:
 							prefix = "";
 							break;
 						case 1:
 							prefix = "intitle:";
 							break;
 						case 2:
 							prefix = "inauthor:";
 							break;
 						case 3:
 							prefix = "inpublisher:";
 							break;
 						}
 						String urL = "https://www.googleapis.com/books/v1/volumes?q=" + java.net.URLEncoder.encode(prefix + keyword);
 						url = new URL(urL);
 						Log.d("APP", urL);
 						str = Util.urlToString(url);
 						
 						
 					} catch (MalformedURLException e) {
 						e.printStackTrace();
 					}
 					
 					
 					
 					handler.post(new Runnable(){
 						public void run(){					
 							final List<BookEntry> entries = new ArrayList<BookEntry>();
 							/* parse JSON */
 							try{
 								if ( !str.equals("") )
 								{
 									JSONObject json = new JSONObject(str);
 									if ( json.getInt("totalItems") != 0 )
 									{
 										JSONArray data = json.getJSONArray("items");
 										for ( int i = 0 ; i < data.length() && i < MAX_RESULT ; i++)
 										{
 											JSONObject p = data.getJSONObject(i);	
 											JSONObject vol = p.getJSONObject("volumeInfo");
 											
 											//String imgLink = p.getJSONObject("imageLinks").getString("smallThumbnail");
 											BookEntry book = new BookEntry();
 											if ( vol.has("title") )
 												book.title = vol.getString("title");
 											else
 												continue;
 											
 											if ( vol.has("authors") )
 												book.author = vol.getJSONArray("authors").getString(0);
 											else
 												book.author = "";
 											
 											if ( vol.has("industryIdentifiers") )
 											{	
 												JSONArray ary = vol.getJSONArray("industryIdentifiers");
 												JSONObject ind;
 												for ( int j = 0 ; j < ary.length() ; j++ )
 												{
 													ind = ary.getJSONObject(j);
 													if (ind.getString("type").equals("ISBN_10") || ind.getString("type").equals("ISBN_13"))
 													{
 														book.isbn = ind.getString("identifier");
 														break;
 													}
 												}
 												if ( book.isbn == null )
 													continue;
 												
 											}
 											else
 												continue;
 											entries.add(book);
 											
 										}
 									}
 								}
 								
 								if ( entries.size() == 0 )
 								{
 									Toast.makeText(KeywordSearchActivity.this, R.string.search_no_result, App.TOAST_TIME).show();
 									progressDialog.dismiss();
 									return;
 								}
 								SearchEntryAdapter adapter = new SearchEntryAdapter(KeywordSearchActivity.this, R.layout.list_item_keyword, entries);
 
 								AlertDialog dialog = new AlertDialog.Builder(KeywordSearchActivity.this).setAdapter(adapter,  new DialogInterface.OnClickListener() {
 									@Override
 									public void onClick(
 											DialogInterface dialog,
 											int position) {
 										dialog.dismiss();
 										String isbn = entries.get(position).isbn;
 										Intent data = new Intent();
 							        	data.putExtra(App.ISBN, isbn);
 							        	setResultForTabActivity(RESULT_OK, data);
 							            finish();
 									}
 								}).setTitle(R.string.title_search_result).create();
 								progressDialog.dismiss();
 								dialog.show();
 								
 							}
 							catch(Exception e){
 								e.printStackTrace();
 							}
 							
 						}
 						
 					});
 				}
 				
 			};
 			thread.start();
 		}
     }
 }
