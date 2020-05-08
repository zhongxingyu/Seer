 package jp.knct.di.c6t.ui.route;
 
 import java.util.List;
 
 import jp.knct.di.c6t.IntentData;
 import jp.knct.di.c6t.R;
 import jp.knct.di.c6t.communication.Client;
 import jp.knct.di.c6t.communication.DebugSharedPreferencesClient;
 import jp.knct.di.c6t.model.Route;
 import jp.knct.di.c6t.util.ActivityUtil;
 import android.app.ListActivity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ListView;
 
 public class SearchRouteActivity extends ListActivity implements OnClickListener {
 	private List<Route> mRoutes;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_routes);
 
 		ActivityUtil.setOnClickListener(this, this, new int[] {
 				R.id.search_route_search,
 		});
 	}
 
 	protected void onListItemClick(ListView l, View v, int position, long id) {
 		Route targetRoute = mRoutes.get(position);
 		Intent intent = new Intent(this, RouteActivity.class)
 				.putExtra(IntentData.EXTRA_KEY_ROUTE, targetRoute);
 		startActivity(intent);
 		finish();
 	}
 
 	@Override
 	public void onClick(View v) {
 		switch (v.getId()) {
 		case R.id.search_route_search:
			List<Route> routes = fetchRoutes();
			setRoutes(routes);
 			break;
 
 		default:
 			break;
 		}
 	}
	
 	// TODO
 	private List<Route> fetchRoutes() {
 		String searchText = ActivityUtil.getText(this, R.id.search_route_name);
 		Client client = new DebugSharedPreferencesClient(this);
 		return client.getRoutes(searchText);
 	}
 
 	private void setRoutes(List<Route> routes) {
		RoutesAdapter adapter = new RoutesAdapter(this, mRoutes);
 		setListAdapter(adapter);
 	}
 }
