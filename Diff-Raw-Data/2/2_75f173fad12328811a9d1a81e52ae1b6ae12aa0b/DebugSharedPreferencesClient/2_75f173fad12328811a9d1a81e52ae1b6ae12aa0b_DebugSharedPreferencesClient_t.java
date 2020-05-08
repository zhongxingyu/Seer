 package jp.knct.di.c6t.communication;
 
 import java.text.ParseException;
 import java.util.LinkedList;
 import java.util.List;
 
 import jp.knct.di.c6t.model.Exploration;
 import jp.knct.di.c6t.model.Route;
 import jp.knct.di.c6t.model.User;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.preference.PreferenceManager;
 
 public class DebugSharedPreferencesClient implements Client {
 
 	private static final String KEY_MY_ROUTES = "__debug_my_routes__";
 	private static final String KEY_MY_EXPLORATIONS = "__debug_my_explorations__";
 
 	private SharedPreferences mPreferences;
 
 	public DebugSharedPreferencesClient(Context context) {
 		mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
 	}
 
 	@Override
 	public List<Exploration> getExplorations(String searchText) {
 		// TODO
 		return getExplorations(getMyUserData());
 	}
 
 	@Override
 	public List<Exploration> getExplorations(User user) {
 		String explorationsJSON = mPreferences.getString(KEY_MY_EXPLORATIONS, "[]");
 		List<Exploration> explorations = new LinkedList<Exploration>();
 
 		try {
 			JSONArray explorationsArray = new JSONArray(explorationsJSON);
 			for (int i = 0; i < explorationsArray.length(); i++) {
 				JSONObject explorationObject = explorationsArray.getJSONObject(i);
 				Exploration exploration = Exploration.parseJSON(explorationObject);
 				explorations.add(exploration);
 			}
 			return explorations;
 
 		}
 		catch (JSONException e) {
 			e.printStackTrace();
 			return null;
 		}
 		catch (ParseException e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 	@Override
 	public List<Route> getRoutes(String searchText) {
 		// TODO
 		return getRoutes(getMyUserData());
 	}
 
 	@Override
 	public List<Route> getRoutes(User user) {
 		// TODO: get other user's routes
 		String routesJSON = mPreferences.getString(KEY_MY_ROUTES, "[]");
 		List<Route> routes = new LinkedList<Route>();
 		try {
 			JSONArray routesArray = new JSONArray(routesJSON);
 			for (int i = 0; i < routesArray.length(); i++) {
 				JSONObject routeObject = routesArray.getJSONObject(i);
 				Route route = Route.parseJSON(routeObject);
 				routes.add(route);
 			}
 			return routes;
 		}
 		catch (JSONException e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 	@Override
 	public void saveExploration(Exploration exploration) {
 		List<Exploration> explorations = getExplorations(getMyUserData());
 		explorations.add(exploration);
 		JSONArray explorationsArray = new JSONArray();
 
 		for (Exploration e : explorations) {
 			explorationsArray.put(e.toJSON());
 		}
 
 		mPreferences.edit()
 				.putString(KEY_MY_EXPLORATIONS, explorationsArray.toString())
 				.commit();
 	}
 
 	@Override
 	public void saveRoute(Route route) {
 		List<Route> routes = getRoutes(getMyUserData());
 		routes.add(route);
 		JSONArray routesArray = new JSONArray();
 
 		for (Route r : routes) {
 			routesArray.put(r.toJSON());
 		}
 
 		mPreferences.edit()
 				.putString(KEY_MY_ROUTES, routesArray.toString())
 				.commit();
 	}
 
 	@Override
 	public User getMyUserData() {
		return new User("taro", "tokyo", 0);
 	}
 
 	@Override
 	public Exploration refreshExplorationInfo(Exploration exploration) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public void joinExploration(Exploration exploration, User user) {
 		// TODO Auto-generated method stub
 
 	}
 }
