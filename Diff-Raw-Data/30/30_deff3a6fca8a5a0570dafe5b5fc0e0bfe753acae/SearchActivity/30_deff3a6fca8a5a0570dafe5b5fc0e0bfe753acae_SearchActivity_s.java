 package se.chalmers.krogkollen.search;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import se.chalmers.krogkollen.IView;
 import se.chalmers.krogkollen.R;
 import se.chalmers.krogkollen.adapter.SearchViewAdapter;
 import se.chalmers.krogkollen.detailed.DetailedActivity;
 import se.chalmers.krogkollen.map.MapActivity;
 import se.chalmers.krogkollen.pub.IPub;
 import se.chalmers.krogkollen.pub.Pub;
 import se.chalmers.krogkollen.pub.PubUtilities;
 import se.chalmers.krogkollen.sort.SortBySearchRelevance;
 import android.os.Bundle;
 import android.app.ListActivity;
 import android.app.SearchManager;
 import android.content.Intent;
 import android.view.View;
 import android.widget.ListView;
 import android.widget.Toast;
 
 /**
  * This activity is shown when a user has searched for something in a search widget
  * 
  * @author Oskar Karrman
  *
  */
 public class SearchActivity extends ListActivity implements IView {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_search);
 		handleIntent(getIntent());
 	}
 	
 	@Override
 	public void onNewIntent(Intent intent) {
 		setIntent(intent);
 		handleIntent(intent);
 	}
 
 	@Override
 	public void onListItemClick(ListView l, View v, int position, long id) {
		// TODO BLA
		// Call detail activity for clicked entry
 	}
 
 	// Checks if the intent passed is a search intent or 
 	// an intent to open the detailed view.
 	private void handleIntent(Intent intent) {
 		if (intent.getAction().equals(Intent.ACTION_SEARCH)) {
 			String query = intent.getStringExtra(SearchManager.QUERY);
 			this.doSearch(query);
 		} else {
 			String pubID = intent.getDataString();
 			Intent newIntent = new Intent(this, DetailedActivity.class);
 			newIntent.putExtra(MapActivity.MARKER_PUB_ID, pubID);
 			
 			// This leaks window but also makes sure that pressing the back button
 			// from the detailed view doesn't return the user to this activity but map or list.
 			this.finish();
 			startActivity(newIntent);
 		}
 	}
 	
 	// Searches
 	private void doSearch(String query) {
 		this.setTitle(this.getResources().getString(R.string.title_activity_search) + ": " + query);
 		
 		List<IPub> allPubs = PubUtilities.getInstance().getPubList();
 
 		List<IPub> matchingPubs = getMatchingPubs(query, allPubs);
 		
 		matchingPubs = new SortBySearchRelevance(query).sortAlgorithm(matchingPubs);
 		
		IPub[] pubs = this.convertListToArray(matchingPubs);
 		
 		this.addMatchesToListView(pubs);
 	}
 	
 	// Adds all the search matches to the listview
 	private void addMatchesToListView(IPub[] pubs) {
 		SearchViewAdapter adapter = new SearchViewAdapter(this, R.layout.searchview_item, pubs);
 		
 		getListView().setAdapter(adapter);
 	}
 	
 	// TODO use in utils instead, temp
 	private IPub[] convertListToArray(List <IPub> list) {
 		Pub[] pubArray = new Pub[list.size()];
 		for(int i = 0; i < list.size(); i++){
 			pubArray[i] = (Pub)list.get(i);
 		}
 		return pubArray;
 	}
 	
 	/**
 	 * Searches a list of IPubs for Pubs with names that in some way matches the query.
 	 * @param query the search
 	 * @param allPubs the list with pubs to search in
 	 * @return	a list of IPubs where all Pubs match the query
 	 */
 	public static List<IPub> getMatchingPubs(String query, List<IPub> allPubs) {
 		List<IPub> matchingPubs = new ArrayList<IPub>();
 		for (IPub pub : allPubs) {
 			if (pub.getName().toLowerCase().contains(query.toLowerCase())) { // TODO check this warning
 				matchingPubs.add(pub);
 			}
 		}
 		return matchingPubs;
 	}
 
 	@Override
 	public void navigate(Class<?> destination) {
		// TODO Auto-generated method stub
		
 	}
 
 	@Override
 	public void navigate(Class<?> destination, Bundle extras) {
		// TODO Auto-generated method stub
		
 	}
 
 	@Override
 	public void showErrorMessage(String message) {
     	CharSequence text = message;
     	int duration = Toast.LENGTH_LONG;
 
     	Toast toast = Toast.makeText(this, text, duration);
     	toast.show();
 	}
 }
