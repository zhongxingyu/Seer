 package ch.hsr.bieridee.android.activities;
 
 import android.app.ListActivity;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AbsListView;
 import android.widget.AdapterView;
 import android.widget.ListView;
 import android.widget.AbsListView.OnScrollListener;
 import ch.hsr.bieridee.android.R;
 import ch.hsr.bieridee.android.adapters.BreweryListAdapter;
 import ch.hsr.bieridee.android.config.Res;
 import ch.hsr.bieridee.android.http.AuthJsonHttp;
 import ch.hsr.bieridee.android.http.HttpHelper;
 import ch.hsr.bieridee.android.utils.ErrorHelper;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.impl.client.BasicResponseHandler;
 import org.json.JSONArray;
 import org.json.JSONException;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * Activity that shows a list of all breweries in our database.
  */
 public final class BreweryListActivity extends ListActivity implements ListView.OnScrollListener {
 
 	private static final String LOG_TAG = BreweryListActivity.class.getName();
 	private BreweryListAdapter adapter;
 	private ProgressDialog progressDialog;
 	private HttpHelper httpHelper;
 
 	private int currentPage = 0;
 	private static final int PAGESIZE = 12;
 	private static final int VISIBLECOUNT = 7;
 	private View loadingFooter;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.brewerylist);
 		
		final ListView list = (ListView) this.getListView();
 		this.loadingFooter = getLayoutInflater().inflate(R.layout.loading_item, list, false);
 		list.addFooterView(this.loadingFooter, null, false);
 		
 		getListView().setOnScrollListener(this);
 		
 		this.httpHelper = AuthJsonHttp.create();
 
 		this.adapter = new BreweryListAdapter(this);
 		setListAdapter(this.adapter);
 		this.addOnClickListeners();
 	}
 
 	@Override
 	public void onStart() {
 		super.onStart();
 		new GetBreweryData().execute();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		final MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.refresh_menu, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 			case R.id_refreshmenu.refresh:
 				new GetBreweryData().execute();
 				this.currentPage++;
 				break;
 		}
 		return true;
 	}
 
 	/**
 	 * Async task to get breweries from server and update UI.
 	 */
 	private class GetBreweryData extends AsyncTask<Void, Void, JSONArray> {
 		private boolean showDialog = true;
 
 		public GetBreweryData() {
 			this.showDialog = true;
 		}
 
 		public GetBreweryData(boolean showDialog) {
 			this.showDialog = showDialog;
 		}
 
 		@Override
 		protected void onPreExecute() {
 			BreweryListActivity.this.loadingFooter.setVisibility(View.VISIBLE);
 			if (this.showDialog) {
 				BreweryListActivity.this.progressDialog = ProgressDialog.show(BreweryListActivity.this, getString(R.string.pleaseWait), getString(R.string.loadingData), true);
 			}
 		}
 
 		@Override
 		protected JSONArray doInBackground(Void... voids) {
 			final Map<String, String> params = new HashMap<String, String>();
 			params.put("items", PAGESIZE + "");
 			params.put("page", BreweryListActivity.this.currentPage + "");
 			final String resourceUri = Res.getURIwithGETParams(Res.BREWERY_COLLECTION, params);
 
 			HttpResponse response = null;
 			try {
 				response = BreweryListActivity.this.httpHelper.get(resourceUri);
 			} catch (IOException e) {
 				Log.d(LOG_TAG, e.getMessage(), e);
 				ErrorHelper.onError(BreweryListActivity.this.getString(R.string.connectionError), BreweryListActivity.this);
 			}
 
 			if (response != null) {
 				final int statusCode = response.getStatusLine().getStatusCode();
 				if (statusCode == HttpStatus.SC_OK) {
 					try {
 						final String responseText = new BasicResponseHandler().handleResponse(response);
 						return new JSONArray(responseText);
 					} catch (IOException e) {
 						Log.d(LOG_TAG, e.getMessage(), e);
 						ErrorHelper.onError(BreweryListActivity.this.getString(R.string.malformedData), BreweryListActivity.this);
 					} catch (JSONException e) {
 						Log.d(LOG_TAG, e.getMessage(), e);
 						ErrorHelper.onError(BreweryListActivity.this.getString(R.string.malformedData), BreweryListActivity.this);
 					}
 				}
 			}
 			return null;
 		}
 
 		@Override
 		protected void onPostExecute(JSONArray result) {
 			if (result != null) {
 				BreweryListActivity.this.adapter.updateData(result);
 				BreweryListActivity.this.adapter.notifyDataSetChanged();
 			}
 			if (this.showDialog) {
 				BreweryListActivity.this.progressDialog.dismiss();
 			}
 			BreweryListActivity.this.loadingFooter.setVisibility(View.GONE);
 			BreweryListActivity.this.currentPage++;
 		}
 	}
 
 	/**
 	 * Add onClick listeners to UI elements.
 	 */
 	private void addOnClickListeners() {
 		this.getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
 			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 				final Intent intent = new Intent(view.getContext(), BreweryDetailActivity.class);
 				intent.putExtra(BreweryDetailActivity.EXTRA_BREWERY_ID, id);
 				startActivity(intent);
 			}
 		});
 	}
 
 	// SUPPRESS CHECKSTYLE: Method not used but needed by interface
 	public void onScroll(AbsListView arg0, int arg1, int arg2, int arg3) {
 		// nuffin (muffin|bluffing)
 	}
 
 	/**
 	 * Triggers on scroll state changed.
 	 * 
 	 * @param view
 	 *            The list view
 	 * @param scrollState
 	 *            The scrollstate
 	 * @see android.widget.AbsListView.OnScrollListener#onScrollStateChanged(android.widget.AbsListView, int)
 	 */
 	public void onScrollStateChanged(AbsListView view, int scrollState) {
 		final int last = view.getLastVisiblePosition();
 		switch (scrollState) {
 			case OnScrollListener.SCROLL_STATE_IDLE:
 				if (last >= (this.currentPage) * VISIBLECOUNT) {
 					new GetBreweryData(false).execute();
 				}
 				break;
 			case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
 				if (last >= (this.currentPage) * VISIBLECOUNT) {
 					new GetBreweryData(false).execute();
 				}
 				break;
 			case OnScrollListener.SCROLL_STATE_FLING:
 				break;
 		}
 	}
 }
