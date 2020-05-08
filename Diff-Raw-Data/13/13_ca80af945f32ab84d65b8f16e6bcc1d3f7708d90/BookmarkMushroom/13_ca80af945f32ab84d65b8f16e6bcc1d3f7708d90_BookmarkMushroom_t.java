 package jp.juggler.bookmarkmushroom;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashSet;
 
 import android.app.Activity;
 import android.content.ContentResolver;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.database.Cursor;
 import android.os.Bundle;
 import android.provider.Browser;
 import android.text.TextUtils;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.Button;
 import android.widget.CompoundButton;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.ToggleButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 
 public class BookmarkMushroom extends Activity {
 	ListView lvBookmark;
 	Button btnSelectAll;
 	Button btnOK;
 	TextView tvError;
 	CheckboxAdapter adapter;
 	ToggleButton tbTitle;
 	ToggleButton tbDetail;
 	
 	InputMethodManager ime;
 	SharedPreferences pref;
 	
 	void initUI(){
         setContentView(R.layout.bookmarklist);
 		lvBookmark = (ListView) findViewById(R.id.lvBookmark);
 		tvError = (TextView)findViewById(R.id.error);
 		btnSelectAll=(Button) findViewById(R.id.btnSelectAll);
 		tbTitle = (ToggleButton)findViewById(R.id.btnTitle);
 		tbDetail = (ToggleButton)findViewById(R.id.btnDetail);
 		btnOK = (Button) findViewById(R.id.btnOK);
 		
 		
 		
 		adapter = new CheckboxAdapter(this);
 		lvBookmark.setAdapter(adapter);
 		
 		ime= (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
 		pref = getSharedPreferences("pref.txt", MODE_PRIVATE);
 		
 		tbTitle.setChecked(pref.getBoolean("tbTitle",true));
 		tbDetail.setChecked(pref.getBoolean("tbDetail",true));
 		
 		tbTitle.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
 			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
 				Editor editor = pref.edit();
 				editor.putBoolean("tbTitle",isChecked);
 				editor.commit();
 			}
 		});
 		tbDetail.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
 			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
 				Editor editor = pref.edit();
 				editor.putBoolean("tbDetail",isChecked);
 				editor.commit();
 			}
 		});
 		
 		btnOK.setOnClickListener(new OnClickListener() {
			@Override
 			public void onClick(View v) {
 				boolean bTitle = tbTitle.isChecked();
 				boolean bDetail = tbDetail.isChecked();
 				
 				ArrayList<String> list = new ArrayList<String>();
 				for(int i=0,end=adapter.getCount(); i<end; ++i){
 					CheckboxItem item = adapter.getItem(i);
 					if(!item.checked) continue;
 					if( list.size() > 0 && (bTitle || bDetail) ) list.add("----");
 					list.add(item.url);
 					if(bTitle) list.add(item.title);
 					if(bDetail) list.add(item.detail);
 				}
 				if(list.size()<=1){
 					mushroom_end( TextUtils.join("",list));
 				}else{
 					mushroom_end( TextUtils.join("\n",list)+"\n");
 				}
 			}
 		});
 		
 		btnSelectAll.setOnClickListener(new OnClickListener() {
			@Override
 			public void onClick(View v) {
 				int nItem = 0;
 				int nSelect = 0;
 				for(int i=0,end=adapter.getCount(); i<end; ++i){
 					CheckboxItem item = adapter.getItem(i);
 					++nItem;
 					if(item.checked) ++nSelect;
 				}
 				// 全て選択なら全て解除、それ以外は全て選択にする
 				boolean newState = (nItem==nSelect?false:true);
 				for(int i=0,end=adapter.getCount(); i<end; ++i){
 					CheckboxItem item = adapter.getItem(i);
 					item.checked = newState;
 				}
 				adapter.notifyDataSetChanged();
 			}
 		});
 	}
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         initUI();
         initPageData(getIntent());
     }
     
     @Override
 	protected void onNewIntent(Intent intent) {
 		super.onNewIntent(intent);
 		initPageData(intent);
 	}
 
     void setError(CharSequence text){
     	tvError.setText(text);
     	tvError.setVisibility(View.VISIBLE);
     	
     }
     void initPageData(Intent intent){
     	
     	ime.hideSoftInputFromWindow(null, InputMethodManager.HIDE_NOT_ALWAYS);
 
     	tvError.setVisibility(View.GONE);
     	adapter.clear();
     	Cursor cur=null;
     	try{
 	    	ContentResolver cr = getContentResolver();
 	    	cur =cr.query(Browser.BOOKMARKS_URI,null,null,null,"created desc,url,title");
 	    	try{
 		    	if( ! cur.moveToFirst() ){
 		    		setError(getText(R.string.empty));
 		    	}else{
 		    		
 		    		String[] colname = cur.getColumnNames();
 
 		    		int col_bookmark = cur.getColumnIndex(Browser.BookmarkColumns.BOOKMARK);
 		    		int col_created  = cur.getColumnIndex(Browser.BookmarkColumns.CREATED);
 		    		int col_date     = cur.getColumnIndex(Browser.BookmarkColumns.DATE);
 		    		int col_favicon  = cur.getColumnIndex(Browser.BookmarkColumns.FAVICON);
 		    		int col_title    = cur.getColumnIndex(Browser.BookmarkColumns.TITLE);
 		    		int col_url      = cur.getColumnIndex(Browser.BookmarkColumns.URL);
 		    	//	int col_visits   = cur.getColumnIndex(Browser.BookmarkColumns. 	VISITS);
 		    		
 		    		HashSet<Integer> ignore_cols = new HashSet<Integer>();
 		    		ignore_cols.add(cur.getColumnIndex("favicon"));
 		    		ignore_cols.add(cur.getColumnIndex("thumbnail"));
 		    		ignore_cols.add(cur.getColumnIndex("touch_icon"));
 		    		ignore_cols.add(cur.getColumnIndex("_id"));
 		    		ignore_cols.add(col_title);
 		    		ignore_cols.add(col_url);
 		    		ignore_cols.add(col_bookmark);
 		    		
 		    		java.text.DateFormat fmt_date = android.text.format.DateFormat.getMediumDateFormat(this);
 		    		java.text.DateFormat fmt_time = android.text.format.DateFormat.getTimeFormat(this);
 		    		
 		    		do{
 		    			if( cur.getInt(col_bookmark)==0) continue;
 		    			
 			    		// get details
 		    			StringBuilder sb = new StringBuilder();
 			    		for(int i=0,e=cur.getColumnCount();i<e;++i){
 			    			if(ignore_cols.contains(i)) continue;
 			    			
 			    			try{
 			    				String v = null;
 			    				if( i == col_date || i == col_created ){
 			    					long n = cur.getLong(i);
 			    					if(n==0) continue;
 			    					Date d = new Date();
 			    					d.setTime(n);
 			    					v = fmt_date.format(d)+" "+fmt_time.format(d);
 			    				}else{
 			    					v=cur.getString(i); 
 			    				}
 				    			if(v!=null){
 					    			if(sb.length() > 0) sb.append('\n');
 					    			sb.append(String.format("[%d]%s: %s",i,colname[i],v));
 				    			}
 			    			}catch(Throwable ex){
 			    				Log.d("bookmark","column "+i+colname[i]+" is not string");
 			    				continue;
 			    			}
 			    		}
 			    		CheckboxItem item = new CheckboxItem();
 			    		item.favicon = cur.getBlob(col_favicon);
 			    		item.checked = false;
 			    		item.title = cur.getString(col_title);
 			    		item.url = cur.getString(col_url);
 			    		item.detail = sb.toString();
 			    		adapter.add(item);
 			    	}while(cur.moveToNext());
 		    	}
 	    	}finally{
 	    		cur.close();
 	    	}
     	}catch(Throwable ex){
     		ex.printStackTrace();
     		setError(ex.getMessage());
     		return;
     	}
     }
     
     
 	//マッシュルームを終了してnew_stringを返す
     void mushroom_end(String new_string){
     	Intent result = new Intent();
     	result.putExtra("replace_key", new_string);
     	setResult(RESULT_OK, result);
     	finish();
     }
 }
