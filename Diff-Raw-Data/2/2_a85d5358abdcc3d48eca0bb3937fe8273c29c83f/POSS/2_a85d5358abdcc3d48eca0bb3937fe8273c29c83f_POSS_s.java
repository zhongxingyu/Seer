 package fr.utc.assos.payutc.api;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.util.Base64;
 import android.util.Log;
 import fr.utc.assos.payutc.Item;
 
 public class POSS extends JsonApiClient {
 	public static final String LOG_TAG = "POSS";
 	
 	public POSS(String url) {
 		super(url);
 	}
 	
 	public String getCasUrl() throws IOException, JSONException, ApiException {
 		String url = (String) call("getCasUrl");
 		Log.d(LOG_TAG, "getCasUrl : "+url);
 		return url;
 	}
 
     public boolean loadPos(String ticket, String service, int poi_id) throws IOException, JSONException, ApiException {
     	Arg[] args =  {
     			new Arg("ticket", ticket),
     			new Arg("service", service),
     			new Arg("poi_id", ""+poi_id)
     	};
 		Object result = call("loadPos", args);
 		Log.d(LOG_TAG, "loadPos : "+result.toString());
 		return true;
     }
 
     public boolean unload() throws IOException, JSONException, ApiException {
		Object result = call("unload");
 		Log.d(LOG_TAG, "unload" + result.toString());
     	return true;
     }
 	
 	public ArrayList<Item> getArticles() throws IOException, JSONException, ApiException {
 		Object raw_result = call("getArticles");
 		Log.d(LOG_TAG, "getArticles : " + raw_result.toString());
 		
 		JSONObject json_result = (JSONObject) raw_result;
 		JSONObject json_articles = json_result.getJSONObject("articles");
 		ArrayList<Item> articles = new ArrayList<Item>();
 		Iterator<?> keys = json_articles.keys();
 		while(keys.hasNext()) {
 			String key = (String) keys.next();
 			//Log.d(LOG_TAG, "key : "+key);
 			JSONObject i = json_articles.getJSONObject(key);
 			//Log.d(LOG_TAG, "object : "+i.toString());
 			articles.add(new Item(i.getInt("id"), i.getString("name"), i.optString("category_id", "noCategory"), i.optInt("image", 1), i.getInt("price")));
 		}
 		
 		return articles;
 	}
 	
 	public Bitmap getImage64(int id, int width, int height) throws IOException, JSONException, ApiException {
     	Arg[] args =  {
     			new Arg("img_id", ""+id),
     			new Arg("outw", ""+width),
     			new Arg("outh", ""+height),
     	};
 		Object raw_result = call("getImage64", args);
 		String encodedImage = (String) raw_result;
 		byte[] decodedString = Base64.decode(encodedImage, Base64.DEFAULT);
 		Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
 		return decodedByte;
 	}
 	
 	public class TransactionResult {
 		public String firstName;
 		public String lastName;
 		public int solde;
 		public TransactionResult(String first_name, String last_name, int _solde) {
 			firstName = first_name;
 			lastName = last_name;
 			solde = _solde;
 		}
 	}
 	
 	public TransactionResult transaction(String badge_id, ArrayList<Integer> ids, String trace) 
 			throws IOException, JSONException, ApiException {
 		String s_ids = "";
 		for (int id : ids) {
 			s_ids += " "+id;
 		}
     	Arg[] args =  {
     			new Arg("badge_id", badge_id),
     			new Arg("obj_ids", s_ids),
     			new Arg("trace", ""+trace)
     	};
 		Object raw_result = call("transaction", args);
 		Log.d(LOG_TAG, "transaction : " + raw_result.toString());
 		JSONObject json_result = (JSONObject) raw_result;
 		String first_name = json_result.getString("firstname");
 		String last_name = json_result.getString("lastname");
 		int solde = json_result.getInt("solde");
 		TransactionResult retour = new TransactionResult(first_name, last_name, solde); 
 		return retour;
 	}
 	
 	public class CustomerDetails {
 		public String mFirstName, mLastName;
 		public int mSolde;
 		
 		public CustomerDetails(String fName, String lName, int solde) {
 			mFirstName = fName;
 			mLastName = lName;
 			mSolde = solde;
 		}
 	}
 	
 	public CustomerDetails getCustomerDetails(String id) throws IOException, JSONException, ApiException {
     	Arg[] args =  {
     		new Arg("badge_id", id),
     	};
 		Object raw_result = call("getBuyerInfo", args);
 		Log.d(LOG_TAG, "transaction : " + raw_result.toString());
 		
 		JSONObject json_result = (JSONObject) raw_result;
 		String fName = json_result.optString("firstname", "Inconnu");
 		String lName = json_result.optString("lastname", "Inconnu");
 		int solde = json_result.optInt("solde", -1);
 		
 		return new CustomerDetails(fName, lName, solde);
 	}
 
 }
