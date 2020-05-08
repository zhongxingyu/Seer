 package se.alexanderblom.apk;
 
 import java.util.Arrays;
 
 import se.alexanderblom.apk.ArticleContract.Columns;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.LoaderManager.LoaderCallbacks;
 import android.support.v4.content.CursorLoader;
 import android.support.v4.content.Loader;
 import android.support.v4.widget.CursorAdapter;
 import android.support.v4.widget.ResourceCursorAdapter;
 import android.text.TextUtils;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class ArticlesActivity extends FragmentActivity implements LoaderCallbacks<Cursor> {
 	private static final String TAG = "ArticlesActivity";
 	
 	private static final String[] PROJECTION = { Columns._ID, Columns.NAME, Columns.NAME2, Columns.VOLUME, Columns.APK };
 	
 	private static int REQUEST_FILTER = 1;
 	
 	private ListView listView;
 	private CursorAdapter adapter;
 	
 	private String selection;
 	private String[] selectionArgs;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.activity_articles);
 		
 		listView = (ListView) findViewById(android.R.id.list);
 		adapter = new Adapter(this, null, 0);
 		listView.setAdapter(adapter);
 		
 		listView.setOnItemClickListener(new OnItemClickListener() {
 			@Override
 			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 				Uri uri = ArticleContract.getUri(id);
 				
 				Intent intent = new Intent(Intent.ACTION_VIEW, uri, ArticlesActivity.this, DetailsActivity.class);
 				startActivity(intent);
 			}
 		});
 		
 		getSupportLoaderManager().initLoader(0, null, this);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		new MenuInflater(this).inflate(R.menu.menu_main, menu);
 		
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 			case R.id.menu_filter:
 				startFilterActivity();
 				break;
 			default:
 				return false;
 		}
 		
 		return true;
 	}
 	
 	@Override
 	protected void onActivityResult(int requstCode, int resultCode, Intent data) {
 		if (requstCode == REQUEST_FILTER && resultCode == RESULT_OK) {
 			String selection = data.getStringExtra(FilterActivity.EXTRA_SELECTION);
 			String args[] = data.getStringArrayExtra(FilterActivity.EXTRA_SELECTION_ARGS);
 			
 			setFilter(selection, args);
 		}
 	}
 	
 	private void setFilter(String selection, String[] selectionArgs) {
 		Log.d(TAG, "Setting filter, selection: " + selection + ", selectionArgs: " + Arrays.toString(selectionArgs));
 		
 		this.selection = selection;
 		this.selectionArgs = selectionArgs;
 		
 		getSupportLoaderManager().restartLoader(0, null, this);
 	}
 
 	private void startFilterActivity() {
 		Intent intent = new Intent(this, FilterActivity.class);
 		startActivityForResult(intent, REQUEST_FILTER);
 	}
 
 	@Override
 	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
 		return new CursorLoader(this, ArticleContract.CONTENT_URI, PROJECTION, selection, selectionArgs, null);
 	}
 
 	@Override
 	public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
 		adapter.swapCursor(c);
 	}
 
 	@Override
 	public void onLoaderReset(Loader<Cursor> c) {
 		adapter.swapCursor(null);
 	}
 	
 	private static class Adapter extends ResourceCursorAdapter {
 		public Adapter(Context context, Cursor c, int flags) {
 			super(context, R.layout.item, c, flags);
 		}
 
 		@Override
 		public View newView(Context context, Cursor cursor, ViewGroup parent) {
 			View v = super.newView(context, cursor, parent);
 			
 			Holder holder = new Holder();
 			holder.name1 = (TextView) v.findViewById(R.id.name1);
 			holder.name2 = (TextView) v.findViewById(R.id.name2);
 			holder.apk = (TextView) v.findViewById(R.id.apk);
 			
 			v.setTag(holder);
 			
 			return v;
 		}
 
 		@Override
 		public void bindView(View v, Context context, Cursor c) {
 			String name1 = c.getString(c.getColumnIndexOrThrow(Columns.NAME));
 			String name2 = c.getString(c.getColumnIndexOrThrow(Columns.NAME2));
 			
 			int volume = c.getInt(c.getColumnIndexOrThrow(Columns.VOLUME));
 			float apk = c.getFloat(c.getColumnIndexOrThrow(Columns.APK));
 			
 			Holder holder = (Holder) v.getTag();
 			
 			String info = null;
 			if (TextUtils.isEmpty(name2)) {
 				info = String.valueOf(volume) + " ml";
 			} else {
 				info = String.valueOf(volume) + " ml - " + name2;
 			}
 			
 			holder.name1.setText(name1);
 			holder.name2.setText(info);
 			
			if (apk == 0f) {
				holder.apk.setText(R.string.apk_text_infinity);
			} else {
				String apkText = context.getString(R.string.apk_text, apk);
				holder.apk.setText(apkText);
			}
			
			
 			
 		}
 		
 		private static class Holder {
 			public TextView name1;
 			public TextView name2;
 			public TextView apk;
 		}
 	}
 }
