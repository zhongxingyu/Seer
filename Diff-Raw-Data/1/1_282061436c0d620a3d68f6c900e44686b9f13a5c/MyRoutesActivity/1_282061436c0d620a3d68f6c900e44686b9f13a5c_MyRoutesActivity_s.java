 package jp.knct.di.c6t.ui.route;
 
 import java.io.IOException;
 import java.text.ParseException;
 import java.util.List;
 
 import jp.knct.di.c6t.IntentData;
 import jp.knct.di.c6t.R;
 import jp.knct.di.c6t.communication.BasicClient;
 import jp.knct.di.c6t.model.Route;
 
 import org.apache.http.client.ClientProtocolException;
 import org.json.JSONException;
 
 import android.app.ListActivity;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.ListView;
 import android.widget.Toast;
 
 public class MyRoutesActivity extends ListActivity {
 
 	private List<Route> mRoutes;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_routes);
 
 		// TODO: display alert dialog
 		new LoadingTask().execute();
 	}
 
 	@Override
 	protected void onListItemClick(ListView l, View v, int position, long id) {
 		Route targetRoute = mRoutes.get(position);
 		Intent intent = new Intent(this, RouteActivity.class)
 				.putExtra(IntentData.EXTRA_KEY_ROUTE, targetRoute);
 		startActivity(intent);
		finish();
 	}
 
 	private class LoadingTask extends AsyncTask<Void, Void, List<Route>> {
 
 		@Override
 		protected List<Route> doInBackground(Void... params) {
 			BasicClient client = new BasicClient();
 			List<Route> routes = null;
 			try {
 				routes = client.getRoutes(client.getUserFromLocal(getApplicationContext()));
 			}
 			catch (ClientProtocolException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			catch (JSONException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			catch (ParseException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			return routes;
 		}
 
 		@Override
 		protected void onPostExecute(List<Route> myRoutes) {
 			mRoutes = myRoutes;
 			RoutesAdapter adapter = new RoutesAdapter(MyRoutesActivity.this, mRoutes);
 			setListAdapter(adapter);
 
 			if (myRoutes.size() == 0) {
 				Toast.makeText(getApplicationContext(), "쐬ς݂̃[g͂܂", Toast.LENGTH_SHORT).show();
 			}
 		}
 	}
 }
