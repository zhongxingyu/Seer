 package org.witness.informa;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.nio.channels.FileChannel;
 import java.nio.charset.Charset;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.concurrent.ExecutionException;
 
 import javax.swing.filechooser.FileFilter;
 
import net.sf.json.JSONArray;
 import net.sf.json.JSONException;
 import net.sf.json.JSONObject;
 
 import org.ektorp.*;
 import org.ektorp.http.HttpClient;
 import org.ektorp.http.StdHttpClient;
 import org.ektorp.impl.StdCouchDbConnector;
 import org.ektorp.impl.StdCouchDbInstance;
 
 import org.witness.informa.utils.Constants;
 import org.witness.informa.utils.CouchParser;
 import org.witness.informa.utils.CouchParser.User;
 import org.witness.informa.utils.LocalConstants;
 import org.witness.informa.utils.Constants.Couch;
 import org.witness.informa.utils.Constants.Couch.Views;
 import org.witness.informa.utils.Constants.Couch.Views.Derivatives;
 import org.witness.informa.utils.Constants.Couch.Views.Derivatives.Geolocate;
 import org.witness.informa.utils.Constants.Media.MediaTypes;
 import org.witness.informa.utils.CouchParser.Derivative;
 
 public class MediaLoader implements Constants {
 	public InformaSearch search;
 		
 	StdCouchDbConnector dbSources, dbDerivatives, dbUsers;
 	
 	public MediaLoader() {
 		StdCouchDbInstance db = null;
 		try {
 			HttpClient couchClient = new StdHttpClient.Builder()
 				.url("http://localhost:5984")
 				.username(LocalConstants.USERNAME)
 				.password(LocalConstants.PASSWORD)
 				.build();
 			db = new StdCouchDbInstance(couchClient);
 		} catch (MalformedURLException e) {
 			CouchParser.Log(Couch.ERROR, e.toString());
 			e.printStackTrace();
 			
 		}
 		
 		dbSources = new StdCouchDbConnector("sources", db);
 		dbDerivatives = new StdCouchDbConnector("derivatives", db);
 		dbUsers = new StdCouchDbConnector("admin", db);
 				
 		search = new InformaSearch(dbDerivatives);
 	}
 	
 	public ArrayList<JSONObject> getDerivatives() {
 		ViewQuery getDerivatives = new ViewQuery().designDocId(Couch.Design.DERIVATIVES);
 		ArrayList<JSONObject> res = CouchParser.getRows(dbDerivatives, getDerivatives, Couch.Views.Derivatives.GET_ALL_SHORTENED, null);
 		/*
 		for(JSONObject j : res)
 			CouchParser.Log(Couch.INFO, j.toString());
 		*/
 		return res;
 	}
 	
 	@SuppressWarnings("unchecked")
 	public Map<String, Object> saveSearch(Map<String, Object> saveRequest) {
 		
 		ViewQuery getUsers = new ViewQuery().designDocId(Couch.Design.ADMIN);
 		Map<String, Object> savedSearches = new HashMap<String, Object>();
		
		JSONArray searches = new JSONArray();
		for(JSONObject s : search.saveSearch((String) saveRequest.get(DC.Options.ALIAS), (Map<String, Object>) saveRequest.get(DC.Options.PARAMETERS), CouchParser.getRecord(dbUsers, getUsers, Couch.Views.Admin.GET_BY_ID, (String) saveRequest.get(DC.Options._ID), null)))
			searches.add(s);
		
		savedSearches.put(Couch.Views.Admin.Keys.SAVED_SEARCHES, searches);
 				
 		Map<String, Object> savedSearchResult = new HashMap<String, Object>();
 		savedSearchResult.put(Couch.Views.Admin.Keys.SAVED_SEARCHES, (List<JSONObject>) savedSearches.get(Couch.Views.Admin.Keys.SAVED_SEARCHES));
 		
 		String newRev = CouchParser.updateRecord(User.class, dbUsers, (String) saveRequest.get(DC.Options._ID), (String) saveRequest.get(DC.Options._REV), savedSearches);
 		if(newRev != null) {
 			savedSearchResult.put(Couch.Views.Admin.Keys._REV, newRev);
 			return savedSearchResult;
 		} else
 			return null;
 		
 	}
 	
 	public ArrayList<JSONObject> getSources() {
 		ArrayList<JSONObject> sourcesList = new ArrayList<JSONObject>();
 		
 		return sourcesList;
 	}
 	
 	public ArrayList<JSONObject> getSearchResults(Map<String, Object> searchParams) {
 		return search.query(searchParams);
 	}
 	
 	public ArrayList<JSONObject> getSearchResults(String viewHash) {
 		return search.query(viewHash);
 	}
 	
 	public JSONObject loginUser(Map<String, Object> credentials) {
 		ViewQuery getUsers = new ViewQuery().designDocId(Couch.Design.ADMIN);
 		String unpw = (String) credentials.get("username") + (String) credentials.get("password");
 		return CouchParser.getRecord(dbUsers, getUsers, Couch.Views.Admin.ATTEMPT_LOGIN, unpw, new String[] {"unpw"});
 	}	
 	
 	public JSONObject renameMedia(String id, String rev, String newAlias) {
 		Map<String, Object> setAlias = new HashMap<String, Object>();
 		setAlias.put("alias", newAlias);
 		
 		JSONObject res = new JSONObject();
 		res.put(DC.Options.ALIAS, newAlias);
 		res.put(DC.Options.RESULT, CouchParser.updateRecord(Derivative.class, dbDerivatives, id, rev, setAlias));
 		return res;
 	}
 	
 	@SuppressWarnings("unchecked")
 	public JSONObject loadMedia(String id) {
 		ViewQuery getDerivative = new ViewQuery().designDocId(Couch.Design.DERIVATIVES);
 		JSONObject derivative = CouchParser.getRecord(dbDerivatives, getDerivative, Couch.Views.Derivatives.GET_BY_ID, id, null);
 		
 		// copy representations to img cache
 		File mediaCache = new File(MEDIA_CACHE);
 		if(!mediaCache.exists())
 			mediaCache.mkdir();
 		
 		Iterator<String> rIt = derivative.getJSONArray("representation").iterator();
 		while(rIt.hasNext()) {
			File original = new File(DERIVATIVE_ROOT, rIt.next());
 			File representation = new File(MEDIA_CACHE, original.getName());
 			try {
 				FileChannel o = new FileInputStream(original).getChannel();
 				FileChannel r = new FileOutputStream(representation).getChannel();
 				r.transferFrom(o, 0, o.size());
 			} catch(IOException e) {
 				CouchParser.Log(Couch.ERROR, e.toString());
 				e.printStackTrace();
 			}
 		}
 		return derivative;
 	}
 	
 	public static ArrayList<String> fileToStrings(File file) throws IOException {
 		ArrayList<String> fStrings = new ArrayList<String>();
 		FileInputStream fis = new FileInputStream(file);
 		BufferedReader br = new BufferedReader(new InputStreamReader(fis, Charset.forName("UTF-8")));
 		String line;
 		while((line = br.readLine()) != null)
 			fStrings.add(line);
 		return fStrings;
 	}
 }
