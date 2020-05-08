 package org.witness.informa;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
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
 
 import net.sf.json.JSONArray;
 import net.sf.json.JSONObject;
 
 import org.apache.commons.codec.digest.DigestUtils;
 import org.ektorp.*;
 import org.ektorp.http.HttpClient;
 import org.ektorp.http.StdHttpClient;
 import org.ektorp.impl.StdCouchDbConnector;
 import org.ektorp.impl.StdCouchDbInstance;
 
 import org.witness.informa.utils.Constants;
 import org.witness.informa.utils.CouchParser;
 import org.witness.informa.utils.CouchParser.Submission;
 import org.witness.informa.utils.CouchParser.User;
 import org.witness.informa.utils.LocalConstants;
 import org.witness.informa.utils.CouchParser.Derivative;
 
 public class MediaLoader implements Constants {
 	public InformaSearch search;
 		
 	StdCouchDbConnector dbSources, dbDerivatives, dbUsers, dbSubmissions;
 	
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
 		dbSubmissions = new StdCouchDbConnector("submissions", db);
 				
 		search = new InformaSearch(dbDerivatives);
		
		CouchParser.ClearDatabase(dbDerivatives, Derivative.class);
 	}
 	
 	public ArrayList<JSONObject> getDerivatives() {
 		ViewQuery getDerivatives = new ViewQuery().designDocId(Couch.Design.DERIVATIVES);
 		ArrayList<JSONObject> res = CouchParser.getRows(dbDerivatives, getDerivatives, Couch.Views.Derivatives.GET_ALL_SHORTENED, null);
 		return res;
 	}
 	
 	@SuppressWarnings("unchecked")
 	public Map<String, Object> saveSearch(Map<String, Object> saveRequest) {
 		
 		ViewQuery getUsers = new ViewQuery().designDocId(Couch.Design.ADMIN);
 		Map<String, Object> savedSearches = new HashMap<String, Object>();
 		savedSearches.put(Couch.Views.Admin.Keys.SAVED_SEARCHES, search.saveSearch((String) saveRequest.get(DC.Options.ALIAS), (Map<String, Object>) saveRequest.get(DC.Options.PARAMETERS), CouchParser.getRecord(dbUsers, getUsers, Couch.Views.Admin.GET_BY_ID, (String) saveRequest.get(DC.Options._ID), null)));
 				
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
 		// TODO: set current session id, cookies, etc.
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
 		
 		// TODO: this should be streamed directly to the client, not dropped into the session_cache...
 		Iterator<String> rIt = derivative.getJSONArray("representation").iterator();
 		String path = "";
 		while(rIt.hasNext()) {
 			String repName = rIt.next();
 			path = repName.substring(0, repName.length() - 4);
 			File original = new File(DERIVATIVE_ROOT + path, repName);
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
 		
 		derivative.put(DC.Options.MESSAGES, getMessages(path).toString());
 		return derivative;
 	}
 	
 	public void cleanup() {
 		CouchParser.Log(Couch.INFO, "cleaning up derivatives");
 		for(JSONObject d : getDerivatives()) {
 			ViewQuery getDerivative = new ViewQuery().designDocId(Couch.Design.DERIVATIVES);
 			String id = (String) d.get(DC.Options._ID);
 			
 			JSONObject derivative = CouchParser.getRecord(dbDerivatives, getDerivative, Couch.Views.Derivatives.GET_BY_ID, id, Annotation.OMIT_FOR_UPDATE);
 			try {
 				String rev = (String) derivative.get(DC.Options._REV);
 				JSONArray discussions = derivative.getJSONArray("discussions");
 				for(Object discussion_ : discussions) {
 					JSONObject discussion = (JSONObject) discussion_;
 					
 					if(discussion.containsKey(DC.Options.DELETE_FLAG) && discussion.getBoolean(DC.Options.DELETE_FLAG)) {
 						discussions.remove(discussion);
 					}
 				}
 				
 				Map<String, Object> updateValues = new HashMap<String, Object>();
 				updateValues.put("discussions", discussions);
 				
 				CouchParser.updateRecord(Derivative.class, dbDerivatives, id, rev, updateValues);
 			} catch(NullPointerException e) {}
 		}
 	}
 	
 	private JSONArray getMessages(String path) {
 		JSONArray messages = new JSONArray();
 		File dir = new File(DERIVATIVE_ROOT + path + "/messages");
 		for(File msgFile : dir.listFiles()) {
 			if(!msgFile.getName().equals(".DS_Store")) {
 				JSONObject message = new JSONObject();
 				try {
 					String ts = msgFile.getName().split("_")[0];
 					if(ts.length() == 10)
 						ts += "000";
 					message.put(DC.Options.CONTENT, fileToString(msgFile));
 					message.put(DC.Options.TIMESTAMP, Long.parseLong(ts));
 					message.put(DC.Options.FROM_CLIENT, (msgFile.getName().split(".txt")[0] + ".").contains("_R.") ? true : false);
 					messages.add(message);
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 			
 		return messages;
 	}
 	
 	public static String fileToString(File file) throws IOException {
 		StringBuffer sb = new StringBuffer();
 		FileInputStream fis = new FileInputStream(file);
 		BufferedReader br = new BufferedReader(new InputStreamReader(fis, Charset.forName("UTF-8")));
 		String line;
 		while((line = br.readLine()) != null)
 			sb.append(line + "<br />");
 		return sb.toString();
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
 
 	@SuppressWarnings("unchecked")
 	public JSONObject addAnnotation(Map<String, Object> annotationPack) {
 		JSONObject result = new JSONObject();
 		result.put(DC.Keys.RESULT, DC.Results.FAIL);
 		
 		Map<String, Object> user = (Map<String, Object>) annotationPack.get(DC.Options.USER);
 		if(!CouchParser.validateUser(dbUsers, user))
 			return result;
 		
 		Map<String, Object> entity = (Map<String, Object>) annotationPack.get(DC.Options.ENTITY);
 		String id = (String) entity.remove(DC.Options._ID);
 		
 		JSONObject derivative = CouchParser.getRecord(dbDerivatives, new ViewQuery().designDocId(Couch.Design.DERIVATIVES), Couch.Views.Derivatives.GET_BY_ID, id, new String[] {"j3m"});
 		JSONArray d = derivative.getJSONArray("discussions");
 		String rev = (String) derivative.get(DC.Options._REV);
 		
 		JSONObject annotationStub = new JSONObject();
 		annotationStub.put(DC.Options.TIMESTAMP, System.currentTimeMillis());
 		annotationStub.put(DC.Options.ORIGINATED_BY, user.get(DC.Options._ID));
 		
 		Map<String, Object> annotation = (Map<String, Object>) entity.get(DC.Options.ANNOTATION);
 		annotationStub.put(DC.Options.DURATION, annotation.get(DC.Options.DURATION));
 		annotationStub.put(DC.Options.TIME_IN, annotation.get(DC.Options.TIME_IN));
 		annotationStub.put(DC.Options.TIME_OUT, annotation.get(DC.Options.TIME_OUT));
 		annotationStub.put(DC.Options.REGION_BOUNDS, annotation.get(DC.Options.REGION_BOUNDS));
 		annotationStub.put(DC.Options.ANNOTATIONS, new JSONArray());
 		d.add(annotationStub);
 		
 		Map<String, Object> updateValues = new HashMap<String, Object>();
 		updateValues.put("discussions", d);
 		
 		CouchParser.updateRecord(Derivative.class, dbDerivatives, id, rev, updateValues);
 		JSONObject updatedDerivative = CouchParser.getRecord(dbDerivatives, new ViewQuery().designDocId(Couch.Design.DERIVATIVES), Couch.Views.Derivatives.GET_BY_ID, id, Annotation.OMIT_FOR_UPDATE);
 		
 		Iterator<String> rIt = updatedDerivative.getJSONArray("representation").iterator();
 		String repName = rIt.next();
 		String path = repName.substring(0, repName.length() - 4);
 		
 		updatedDerivative.put(DC.Options.MESSAGES, getMessages(path));
 		int discussionId = d.size() - 1;
 		
 		result.put(DC.Options.RESULT, updatedDerivative);
 		result.put(DC.Options.DISCUSSION_ID, discussionId);
 		
 		return result;
 	}
 
 	@SuppressWarnings("unchecked")
 	public JSONObject appendToAnnotation(Map<String, Object> annotationPack) {
 		JSONObject result = new JSONObject();
 		result.put(DC.Keys.RESULT, DC.Results.FAIL);
 		
 		if(!CouchParser.validateUser(dbUsers, (Map<String, Object>) annotationPack.get(DC.Options.USER)))
 			return result;
 		
 		Map<String, Object> discussion = (Map<String, Object>) annotationPack.get(DC.Options.ENTITY);
 		String id = (String) discussion.remove(DC.Options._ID);
 		String rev = (String) discussion.remove(DC.Options._REV);
 		int discussionId = Integer.parseInt(String.valueOf(discussion.get(DC.Options.DISCUSSION_ID)));
 		
 		JSONObject derivative = CouchParser.getRecord(dbDerivatives, new ViewQuery().designDocId(Couch.Design.DERIVATIVES), Couch.Views.Derivatives.GET_BY_ID, id, new String[] {"j3m"});
 		JSONArray d = derivative.getJSONArray("discussions");
 		
 		JSONObject annotation = new JSONObject();
 		annotation.put(Annotation.Keys.CONTENT, (String) discussion.remove(Annotation.Keys.CONTENT));
 		annotation.put(Annotation.Keys.SUBMITTED_BY, ((Map<String, Object>) annotationPack.get(DC.Options.USER)).get(DC.Options._ID));
 		annotation.put(Annotation.Keys.DATE, System.currentTimeMillis());
 		((JSONArray) ((JSONObject) d.get(discussionId)).get(DC.Options.ANNOTATIONS)).add(annotation);
 		
 		Map<String, Object> updateValues = new HashMap<String, Object>();
 		updateValues.put("discussions", d);
 		
 		CouchParser.updateRecord(Derivative.class, dbDerivatives, id, rev, updateValues);
 		JSONObject updatedDerivative = CouchParser.getRecord(dbDerivatives, new ViewQuery().designDocId(Couch.Design.DERIVATIVES), Couch.Views.Derivatives.GET_BY_ID, id, Annotation.OMIT_FOR_UPDATE);
 		
 		Iterator<String> rIt = updatedDerivative.getJSONArray("representation").iterator();
 		String repName = rIt.next();
 		String path = repName.substring(0, repName.length() - 4);
 		
 		updatedDerivative.put(DC.Options.MESSAGES, getMessages(path));
 		
 		result.put(DC.Options.RESULT, updatedDerivative);
 		result.put(DC.Options.DISCUSSION_ID, discussionId);
 		
 		return result;
 	}
 
 	@SuppressWarnings("unchecked")
 	public Object sendMessage(Map<String, Object> messagePack) {
 		JSONObject result = new JSONObject();
 		result.put(DC.Keys.RESULT, DC.Results.FAIL);
 		
 		if(!CouchParser.validateUser(dbUsers, (Map<String, Object>) messagePack.get(DC.Options.USER)))
 				return result;
 		
 		Map<String, Object> entity = (Map<String, Object>) messagePack.get(DC.Options.ENTITY);
 		JSONObject derivative = CouchParser.getRecord(dbDerivatives, new ViewQuery().designDocId(Couch.Design.DERIVATIVES), Couch.Views.Derivatives.GET_BY_ID, entity.get(DC.Options._ID), new String[] {"j3m"});
 		
 		Iterator<String> rIt = derivative.getJSONArray("representation").iterator();
 		String repName = rIt.next();
 		String path = repName.substring(0, repName.length() - 4);
 		
 		String messagePath = DERIVATIVE_ROOT + path + 
 				"/messages/" + 
 				System.currentTimeMillis() + "_" +
 				DigestUtils.md5Hex((String) entity.get(DC.Options.CONTENT)) +
 				".txt";
 		CouchParser.Log(Couch.INFO, messagePath);
 		
 		try {
 			FileWriter fw = new FileWriter(messagePath);
 			fw.write((String) entity.get(DC.Options.CONTENT));
 			fw.flush();
 			fw.close();
 			derivative.put(DC.Options.MESSAGES, getMessages(path));
 			result.put(DC.Keys.RESULT, derivative);
 		} catch (IOException e) {
 			CouchParser.Log(Couch.ERROR, e.toString());
 			e.printStackTrace();
 		}
 
 		return result;
 	}
 
 	@SuppressWarnings("unchecked")
 	public Object modifyAnnotation(Map<String, Object> annotationPack) {
 		JSONObject result = new JSONObject();
 		result.put(DC.Keys.RESULT, DC.Results.FAIL);
 		
 		// user, annotation, modification to perform
 		Map<String, Object> user = (Map<String, Object>) annotationPack.get(DC.Options.USER);
 		if(!CouchParser.validateUser(dbUsers, user))
 			return result;
 		
 		Map<String, Object> discussion = (Map<String, Object>) annotationPack.get(DC.Options.ENTITY);
 		String id = (String) discussion.remove(DC.Options._ID);
 		String rev = (String) discussion.remove(DC.Options._REV);
 		int discussionId = Integer.parseInt(String.valueOf(discussion.get(DC.Options.DISCUSSION_ID)));
 		
 		JSONObject derivative = CouchParser.getRecord(dbDerivatives, new ViewQuery().designDocId(Couch.Design.DERIVATIVES), Couch.Views.Derivatives.GET_BY_ID, id, new String[] {"j3m"});
 		JSONArray d = derivative.getJSONArray("discussions");
 		
 		// does annotation really belong to user?
 		String originatedBy = d.getJSONObject(discussionId).getString(DC.Options.ORIGINATED_BY);
 		if(!originatedBy.equals(user.get(DC.Options._ID)))
 			return result;
 		
 		// if so...
 		long editType = (Long) discussion.get(DC.Options.EDIT_TYPE);		
 		switch((int) editType) {
 		case DC.EditType.DELETE:
 			d.getJSONObject(discussionId).put(DC.Options.DELETE_FLAG, true);
 			// set this as deleted, but update the record after everyone is gone
 			break;
 		case DC.EditType.MOVE:
 			// perform modification on timeIn, timeOut, duration, regionBounds
 			Map<String, Object> annotation = (Map<String, Object>) discussion.get(DC.Options.ANNOTATION);
 			CouchParser.Log(Couch.INFO, annotation.toString());
 			d.getJSONObject(discussionId).put(DC.Options.TIME_IN, annotation.get(DC.Options.TIME_IN));
 			d.getJSONObject(discussionId).put(DC.Options.TIME_OUT, annotation.get(DC.Options.TIME_OUT));
 			d.getJSONObject(discussionId).put(DC.Options.DURATION, annotation.get(DC.Options.DURATION));
 			d.getJSONObject(discussionId).put(DC.Options.REGION_BOUNDS, annotation.get(DC.Options.REGION_BOUNDS));
 			
 			break;
 		}
 		
 		Map<String, Object> updateValues = new HashMap<String, Object>();
 		updateValues.put("discussions", d);
 		
 		CouchParser.updateRecord(Derivative.class, dbDerivatives, id, rev, updateValues);
 		JSONObject updatedDerivative = CouchParser.getRecord(dbDerivatives, new ViewQuery().designDocId(Couch.Design.DERIVATIVES), Couch.Views.Derivatives.GET_BY_ID, id, Annotation.OMIT_FOR_UPDATE);
 		
 		Iterator<String> rIt = updatedDerivative.getJSONArray("representation").iterator();
 		String repName = rIt.next();
 		String path = repName.substring(0, repName.length() - 4);
 		
 		updatedDerivative.put(DC.Options.MESSAGES, getMessages(path));
 		
 		result.put(DC.Options.RESULT, updatedDerivative);
 		result.put(DC.Options.DISCUSSION_ID, discussionId);
 		
 		return result;
 	}
 
 	@SuppressWarnings("unchecked")
 	public JSONObject requestUploadTicket(Map<String, Object> uploadOpts) {
 		JSONObject result = new JSONObject();
 		result.put(DC.Keys.RESULT, DC.Results.FAIL);
 		
 		Map<String, Object> user = (Map<String, Object>) uploadOpts.get(DC.Options.USER);
 		if(!CouchParser.validateUser(dbUsers, user))
 			return result;
 		
 		Map<String, Object> sourceId = new HashMap<String, Object>();
 		sourceId.put("sourceId", "import_" + user.get(DC.Options._ID));
 		
 		Map<String, Object> initialValues = new HashMap<String, Object>();
 		initialValues.put("java.lang.String", sourceId);
 		
 		
 		// create a blank submission descriptor with user with only a sourceId as "import_USERHASH"
 		// _rev is auth token here (later to be exchanged for j3m)
 		String[] newSubmission = CouchParser.createRecord(Submission.class, dbSubmissions, initialValues);
 		if(newSubmission == null)
 			return result;
 		
 		result.put(DC.Keys.RESULT, DC.Results.OK);
 		result.put(DC.Options.NEW_SUBMISSION_ID, newSubmission[0]);
 		result.put(DC.Options.NEW_SUBMISSION_REV, newSubmission[1]);
 		
 		return result;
 	}
 }
