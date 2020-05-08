 package net.eledge.android.eu.europeana.search.task;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.List;
 
 import net.eledge.android.eu.europeana.Config;
 import net.eledge.android.eu.europeana.search.listeners.SearchTaskListener;
 import net.eledge.android.eu.europeana.search.model.SearchResult;
 import net.eledge.android.eu.europeana.search.model.searchresults.BreadCrumb;
 import net.eledge.android.eu.europeana.search.model.searchresults.Facet;
 import net.eledge.android.eu.europeana.search.model.searchresults.Field;
 import net.eledge.android.eu.europeana.search.model.searchresults.Item;
 import net.eledge.android.eu.europeana.tools.UriHelper;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 import android.os.AsyncTask;
 import android.util.Log;
 
 public class SearchTask extends AsyncTask<String, Void, Boolean> {
 	private final static String TAG = "SearchTask";
 
 	private SearchTaskListener[] listeners;
 
 	private List<Item> searchItems;
 	private List<BreadCrumb> breadcrumbs;
 	private List<Facet> facets;
 	private int totalResults;
 
 	private int pageLoad = 1;
 	
 	public SearchTask(int pageLoad, SearchTaskListener... listeners) {
 		super();
 		this.pageLoad = pageLoad;
 		this.listeners = listeners;
 	}
 
 	@Override
 	protected void onPreExecute() {
 		// TODO: start wait animation...
 	}
 
 	@Override
 	protected Boolean doInBackground(String... terms) {
 		searchItems = new ArrayList<Item>();
 		boolean facetsUpdated = false;
 		URI url = UriHelper.getSearchURI(terms, pageLoad);
 		try {
 			HttpResponse response = new DefaultHttpClient().execute(new HttpGet(url));
 			BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(),
 					Config.JSON_CHARSET));
 			StringBuilder json = new StringBuilder();
 			String line = reader.readLine();
 			while (line != null) {
 				json.append(line);
 				line = reader.readLine();
 			}
 			JSONObject jsonObj = new JSONObject(json.toString());
 			// check for error
 			if (jsonObj.has("error")) {
 				// report error
 			} else {
 				// get statistics
 				totalResults = jsonObj.getInt("totalResults");
 				if (totalResults == 0) {
 					return null;
 				}
 				// get items
 				JSONArray items = jsonObj.getJSONArray("items");
 				if ((items != null) && (items.length() > 0)) {
 					for (int i = 0; i < items.length(); i++) {
 						if (isCancelled()) {
 							break;
 						}
 						JSONObject item = items.getJSONObject(i);
 						Item tmp = new Item();
 						tmp.id = item.getString("id");
 						tmp.link = item.getString("link");
 						if (item.has("title")) {
							tmp.title = item.getJSONArray("title").getString(0);
 						}
 						tmp.type = item.getString("type");
 						if (item.has("edmPreview")) {
 							tmp.thumbnail = item.getJSONArray("edmPreview").getString(0);
 						}
 						searchItems.add(tmp);
 					}
 				}
 				if (jsonObj.has("breadCrumbs")) {
 					breadcrumbs = new ArrayList<BreadCrumb>();
 					items = jsonObj.getJSONArray("breadCrumbs");
 					for (int i = 0; i < items.length(); i++) {
 						if (isCancelled()) {
 							break;
 						}
 						JSONObject bcObject = items.getJSONObject(i);
 						BreadCrumb bc = new BreadCrumb();
 						bc.display = bcObject.getString("display");
 						bc.href = bcObject.getString("href");
 						bc.param = bcObject.getString("param");
 						bc.value = bcObject.getString("value");
 						bc.last = bcObject.getBoolean("last");
 						breadcrumbs.add(bc);
 					}
 				}
 				if (jsonObj.has("facets")) {
 					facetsUpdated = true;
 					facets = new ArrayList<Facet>();
 					items = jsonObj.getJSONArray("facets");
 					for (int i = 0; i < items.length(); i++) {
 						if (isCancelled()) {
 							break;
 						}
 						JSONObject facetObject = items.getJSONObject(i);
 						Facet facet = new Facet();
 						facet.name = facetObject.getString("name");
 						JSONArray fields = facetObject.getJSONArray("fields");
 						for (int j = 0; j < fields.length(); j++) {
 							if (isCancelled()) {
 								break;
 							}
 							JSONObject fieldObject = fields.getJSONObject(j);
 							Field field = new Field();
 							field.label = fieldObject.getString("label");
 							field.count = fieldObject.getLong("count");
 							facet.fields.add(field);
 						}
 						facets.add(facet);
 					}
 				}
 			}
 		} catch (Exception e) {
 			Log.e(TAG, e.getMessage());
 		}
 		return Boolean.valueOf(facetsUpdated);
 	}
 
 	@Override
 	protected void onPostExecute(Boolean upgradeFacets) {
 		SearchResult result = new SearchResult();
 		result.searchItems = searchItems;
 		result.breadcrumbs = breadcrumbs;
 		result.facets = facets;
 		result.totalResults = totalResults;
 		result.facetUpdated = upgradeFacets.booleanValue();
 		for (SearchTaskListener l: listeners) {
 			if (l != null) {
 				if (upgradeFacets.booleanValue()) {
 					l.onSearchFacetsUpdate(facets);
 				}
 				l.onSearchFinish(result);
 			}
 		}
 	}
 
 }
