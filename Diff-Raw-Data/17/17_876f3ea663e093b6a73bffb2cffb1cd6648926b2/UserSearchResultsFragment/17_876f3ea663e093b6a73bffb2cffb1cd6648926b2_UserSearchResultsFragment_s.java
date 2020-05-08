 package cs169.project.thepantry;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.FrameLayout;
 import android.widget.ListView;
 import android.widget.AdapterView.OnItemClickListener;
 
 public class UserSearchResultsFragment extends Fragment {
 	
 	public static final String ARG_SECTION_NUMBER = "section_number";
 	ListView matchlist;
 	private ArrayList<Storage> recipes;
 	DatabaseModel dm;
 	private CookbookListAdapter cbAdapter;
 	FrameLayout mFrameOverlay;
 	
 	public UserSearchResultsFragment() {
 		
 	}
 	
 
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		View rootView = inflater.inflate(R.layout.pager_home_page,
 				container, false);
 		recipes = new ArrayList<Storage>();
 		matchlist = (ListView) rootView.findViewById(R.id.matchList);
 		mFrameOverlay = (FrameLayout) rootView.findViewById(R.id.overlay);
 		cbAdapter = new CookbookListAdapter(getActivity(), recipes);   
 		matchlist.setAdapter(cbAdapter);
 		cbAdapter.notifyDataSetChanged();
 		matchlist.setOnItemClickListener(new OnItemClickListener() {
 			public void onItemClick(AdapterView<?> parent, View view,
 					int position, long id) {
 				System.out.println("HI");
 				Recipe rec = (Recipe) view.getTag();
 				Intent intent = new Intent(getActivity(), RecipeActivity.class);
 				intent.putExtra("result", rec);
 				intent.putExtra("type", "cookbook");
 				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 				startActivity(intent);
 				
 			}
 		});
 		return rootView;
 	}
 	
 	public class SearchTask extends AsyncTask<String, String, JSONArray> {
 		
 		private Context context;
 		private ProgressDialog progressDialog;
 		private String baseUrl = "http://cockamamy-island-1557.herokuapp.com/user_recipe/?auth_token=";
 		private String authToken;
 		
 		public SearchTask(Context context, String authToken) {
 	    	this.context = context;
 	    	this.authToken = authToken;
 		}
 		
 		//show progress wheel
 		@Override
 		protected void onPreExecute() {
 			progressDialog = new ProgressDialog(getActivity());
 			progressDialog.setMessage("Loading User Recipes ...");
			progressDialog.show();
 	    };
 		
 		@Override
 		protected JSONArray doInBackground(String... sc) {
 			String[] ingredients = sc[0].split(",");
 			String url = baseUrl + authToken;
 			for (String ingredient : ingredients) {
				url += "&ingredients[]=" + ingredient.toLowerCase();
 			}
 			System.out.println(url);
 			HttpClient client = new DefaultHttpClient();
 			HttpGet get = new HttpGet(url);
 			HttpResponse resp;
 			try {
 				resp = client.execute(get);
 				HttpEntity ent = resp.getEntity();
 	            InputStream instream = ent.getContent();
 	            BufferedReader br = new BufferedReader(new InputStreamReader(instream));
 	            StringBuilder builder = new StringBuilder();
 	            String line = br.readLine();
 	            while (line != null) {
 	            	builder.append(line + "\n");
 	            	line = br.readLine();
 	            }
 	            instream.close();
 	            String result = builder.toString();
 	            System.out.println(result);
 	            JSONArray respObj = new JSONArray(result);
 	            return respObj;
 			} catch (ClientProtocolException e) {
				progressDialog.dismiss();
 				e.printStackTrace();
 			} catch (IOException e) {
				progressDialog.dismiss();
 				e.printStackTrace();
 			} catch (JSONException e) {
				progressDialog.dismiss();
 				e.printStackTrace();
 			}
 			return null;
 		}
 		
 		//update list of matches
 		@Override
 		protected void onPostExecute(JSONArray result) {
			progressDialog.dismiss();
 			if (result != null) {
 				for (int i = 0; i < result.length(); i++) {
 					try {
 						JSONObject resp = (JSONObject) result.get(i);
 						Recipe recipe = new Recipe();
 						recipe.name = resp.getString("name");
 						recipe.id = resp.getString("id");
 						recipe.images = new RecipeImages(resp.getString("imgUrl"), resp.getString("imgUrl"));
 						ArrayList<String> ingLines = new ArrayList<String>();
 						for (String s : resp.getString("ingLines").split(ThePantryContract.SEPERATOR)) {
 							ingLines.add(s);
 						}
 						recipe.ingredientLines = ingLines;
 						ArrayList<String> dirLines = new ArrayList<String>();
 						for (String s : resp.getString("dirLines").split(ThePantryContract.SEPERATOR)) {
 							dirLines.add(s);
 						}
 						recipe.directionLines = dirLines;
 						recipes.add(recipe);
 					} catch (JSONException e) {
 						e.printStackTrace();
 					}
 				}
 				System.out.println(recipes);
 				cbAdapter.values = recipes;
 				cbAdapter.notifyDataSetChanged();
 			}
 		}
 	}
 
 }
