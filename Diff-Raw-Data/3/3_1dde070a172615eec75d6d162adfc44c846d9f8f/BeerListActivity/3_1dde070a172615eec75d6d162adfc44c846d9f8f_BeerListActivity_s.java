 package eu.silvere;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
 
 public class BeerListActivity extends ListActivity {
 
 	private static final String NAME = "name";
 	private static final String DESCRIPTION = "description";
 
 	private Context mCtx;
 	private ArrayList<BeerBottle> mBootleList;
 
 	@SuppressWarnings("unchecked")
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		mCtx = getApplicationContext();
 
 		// mBeerBottleLoader = (BeerBottleLoader)
 		// savedInstanceState.get("beer_list");
 		// mBootleList = new BeerBottleLoader(getResources()).getBottles();
 		if (savedInstanceState != null && savedInstanceState.containsKey("bottle_list")) {
 
 			mBootleList = (ArrayList<BeerBottle>) savedInstanceState.get("bottle_list");
 		} else {
 			mBootleList = new BeerBottleLoader(getResources()).getBottles();
 		}
 
 		List<Map<String, String>> list = createList(mBootleList);
 
 		// We'll define a custom screen layout here (the one shown above), but
 		// typically, you could just use the standard ListActivity layout.
 		setContentView(R.layout.beer_list_activity_view);
 
 		String[] from = { NAME, DESCRIPTION };
 		int[] to = { R.id.name, R.id.description };
 
		ListAdapter adapter = new SimpleAdapter(mCtx, list, R.layout.beer_list_activity_view, from,
				to);
 		// Bind to our new adapter.
 		setListAdapter(adapter);
 	}
 
 	private List<Map<String, String>> createList(List<BeerBottle> bottles) {
 
 		List<Map<String, String>> list = new ArrayList<Map<String, String>>();
 
 		for (BeerBottle bottle : bottles) {
 
 			Map<String, String> map = new HashMap<String, String>();
 
 			map.put(NAME, bottle.getName());
 			map.put(DESCRIPTION, bottle.getDescription());
 
 			list.add(map);
 		}
 
 		return list;
 	}
 
 	@Override
 	protected void onListItemClick(ListView l, View v, int position, long id) {
 
 		Intent intent = new Intent(mCtx, BeerSoundActivity.class);
 		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 
 		/* Sending some arguments */
 		Bundle bundle = new Bundle();
 
 		bundle.putParcelable("bottle", mBootleList.get(position)); // TODO
 
 		intent.putExtra("b", bundle);
 
 		/* Start Activity */
 		mCtx.startActivity(intent);
 
 	}
 }
