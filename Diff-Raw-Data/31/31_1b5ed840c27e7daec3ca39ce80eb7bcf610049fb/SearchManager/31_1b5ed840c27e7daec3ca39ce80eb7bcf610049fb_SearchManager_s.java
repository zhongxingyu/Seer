 /*******************************************************************************
  * Copyright (c) 2012 rmateus.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Public License v2.0
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
  ******************************************************************************/
 package cm.aptoide.pt;
 
 import org.holoeverywhere.LayoutInflater;
 import org.holoeverywhere.widget.Button;
 import org.holoeverywhere.widget.ListView;
 import org.holoeverywhere.widget.TextView;
 
 import android.content.Intent;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.LoaderManager.LoaderCallbacks;
 import android.support.v4.content.Loader;
 import android.support.v4.widget.CursorAdapter;
 import android.util.TypedValue;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import cm.aptoide.pt.adapters.InstalledAdapter;
 import cm.aptoide.pt.contentloaders.SimpleCursorLoader;
         
 
public class SearchManager extends FragmentActivity/*SherlockFragmentActivity */implements LoaderCallbacks<Cursor>{
 	ListView lv;
 	String query;
 //	EditText searchBox;
 	Database db;
 	View v;
 	TextView results;
 	private InstalledAdapter adapter;
 
 	@Override
 	protected void onCreate(Bundle arg0) {
 		AptoideThemePicker.setAptoideTheme(this);
 		super.onCreate(arg0);
 		
 		System.out.println("onCreate Search");
 		db = Database.getInstance();
 		setContentView(R.layout.searchmanager);
 //		getSupportActionBar().setIcon(R.drawable.brand_padding);
 //		getSupportActionBar().setTitle(getString(R.string.search_on_aptoide));
 //		getSupportActionBar().setHomeButtonEnabled(true);
 //		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
 		if(getIntent().hasExtra("search")){
 			query = getIntent().getExtras().getString("search");
 		}else{
 			query = getIntent().getExtras().getString(android.app.SearchManager.QUERY).replaceAll("\\s{2,}|\\W", " ").trim();
 			query = query.replaceAll("\\s{2,}", " ");
 		}
 		lv = (ListView) findViewById(R.id.listView);
 //		searchBox = (EditText) findViewById(R.id.search_box);
 		v = LayoutInflater.from(getApplicationContext()).inflate(R.layout.footer_search_aptoide, null);
 		v.setOnClickListener(null);
 		lv.addFooterView(v);
 		
 		results = (TextView) v.findViewById(R.id.results_text);
 		
 		Button searchButton =  (Button) v.findViewById(R.id.baz_src);
 		
 		if(!ApplicationAptoide.SEARCHSTORES){
             findViewById(R.id.baz_src).setVisibility(View.GONE);
         }
 
         
 		searchButton.setText(getString(R.string.search_log)+" '"+query+"' "+getString(R.string.search_stores));
 
 
 		searchButton.setOnClickListener(new OnClickListener() {
 
 			public void onClick(View v) {
 				String url = "http://m.aptoide.com/searchview.php?search="+query;
 				Intent i = new Intent(Intent.ACTION_VIEW);
 				i.setData(Uri.parse(url));
 				startActivity(i);
 			}
 		});
 		adapter = new InstalledAdapter(this,null,CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER,db);
 		lv.setAdapter(adapter);
 
 		
 //		searchBox.setText(query);
 //		searchBox.addTextChangedListener(new TextWatcher() {
 //
 //			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
 //
 //			}
 //
 //			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
 //					int arg3) {
 //
 //			}
 //
 //			public void afterTextChanged(Editable editable) {
 //				query=editable.toString().replaceAll("[\\%27]|[\\']|[\\-]{2}|[\\%23]|[#]|\\s{2,}", " ").trim();
 //				if(editable.length()>2){
 //					getSupportLoaderManager().restartLoader(0x30, null, SearchManager.this);
 //
 //				}
 //				((TextView) v.findViewById(R.id.baz_src)).setText(getString(R.string.search_log)+" '"+query+"' "+getString(R.string.search_stores));
 //			}
 //		});
 
 		getSupportLoaderManager().restartLoader(0x30, null, this);
 
 		lv.setOnItemClickListener(new OnItemClickListener() {
 
 			public void onItemClick(AdapterView<?> parent, View v, int position,
 					long arg3) {
 				Intent i = new Intent(SearchManager.this,ApkInfo.class);
 				i.putExtra("_id", arg3);
 				i.putExtra("category", Category.INFOXML.ordinal());
 				startActivity(i);
 			}
 		});
 	}
 
 
 	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
 		SimpleCursorLoader a = new SimpleCursorLoader(this) {
 
 			@Override
 			public Cursor loadInBackground() {
 
 				return db.getSearch(query);
 			}
 		};
 		return a;
 	}
 	public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
 		adapter.swapCursor(arg1);
 		TypedValue a = new TypedValue();
 		getTheme().resolveAttribute(R.attr.custom_color, a, true);
 		if(adapter.getCount()<=0){
 			results.setText(getString(R.string.no_results_found));
 			results.setTextColor(a.data);
 		}else{
 			results.setText(getString(R.string.found)+" "+adapter.getCount()+" "+getString(R.string.results));
 			results.setTextColor(a.data);
 		}
 		
 	}
 	public void onLoaderReset(Loader<Cursor> arg0) {
 		adapter.swapCursor(null);
 	}
 	
 //	@Override
 //	public boolean onOptionsItemSelected(MenuItem item) {
 //		if (item.getItemId() == android.R.id.home) {
 //			finish();
 //			return true;
 //		}
 //		return super.onOptionsItemSelected(item);
 //	}
 }
