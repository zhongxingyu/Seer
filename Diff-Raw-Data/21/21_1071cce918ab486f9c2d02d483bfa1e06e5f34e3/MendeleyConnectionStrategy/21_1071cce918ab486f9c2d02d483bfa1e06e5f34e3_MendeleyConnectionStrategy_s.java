 package main;
 
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.util.List;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.scribe.model.Response;
 import org.scribe.model.Verb;
 
 import util.MendeleyApiUrls;
 
 public class MendeleyConnectionStrategy implements ConnectionStrategy {
 	
 	/** Reference to the authentication manager singleton */
 	private AuthenticationManager auth;
 	
 	public MendeleyConnectionStrategy(AuthenticationManager auth) {
 		this.auth = auth;
 	}
 	
 	
 	/**
 	 * The method search() searches.
 	 * @param searchTerm
 	 * @param title
 	 * @param author
 	 * @param maxResults
 	 * @return
 	 * @throws JSONException 
 	 */
 	public List<Paper> search(String searchTerm, String title, String author, int maxResults) throws JSONException {
 	
 		String searchURL = String.format(MendeleyApiUrls.PUBLIC_GET_SEARCH_FOR_DOCUMENTS, buildSearch(searchTerm, title, author), maxResults);
 
 		Response response = auth.sendPublicRequest(Verb.GET, searchURL);
		
 		try {
 			JSONObject results = new JSONObject(response.getBody());
 			JSONArray documents = results.getJSONArray("documents");
 			for(int i = 0; i < documents.length(); i++) {
 				JSONObject doc = documents.getJSONObject(i);
 				String uuid = doc.getString("uuid");
 				String docURL = String.format(MendeleyApiUrls.PUBLIC_GET_DOCUMENT_DETAILS, uuid);
 				
 				response = auth.sendPublicRequest(Verb.GET, docURL);
 				
 				doc = new JSONObject(response.getBody());
 				Paper p = new Paper();
 				
 				if(doc.has("abstract"))
 					p.abst = doc.getString("abstract");
 				
 				JSONArray authors = doc.getJSONArray("authors");
 				String[] docAuthors = new String[authors.length()];
 				for(int j = 0; j < authors.length(); j++) {
					JSONObject docAuthor = authors.getJSONObject(i);
 					docAuthors[j] = docAuthor.getString("forename") + " " + docAuthor.getString("surname");
 				}
 				p.authors = docAuthors;
 				
 				p.title = doc.getString("title");
 				
 				if(doc.has("publication_outlet")) {
 					p.venue = doc.getString("publication_outlet");
 					p.type = "Journal Article";
 				} else p.type = "Generic";
 				
				p.year = doc.getString("year");
				
 				
				System.out.println(response.getBody());
 			}
 			
			
			
 		} catch (JSONException e) { e.printStackTrace(); }
 		
		System.out.println(response.getBody());
		return null;
 	}
 	
 	public  String buildSearch(String all, String title, String author) {
 		try {
 			all = (all==null)?null:URLEncoder.encode(all, "UTF-8").replace("+", "%20");
 			title = (title==null)?null:URLEncoder.encode(title, "UTF-8").replace("+", "%20");
 			author = (author==null)?null:URLEncoder.encode(author, "UTF-8").replace("+", "%20");
 
 			StringBuilder term = new StringBuilder();
 			boolean alle = true, tie = true;
 			if(all == null || all.equals(""))
 				alle = false;
 			else term.append(all);
 
 			if(title == null || title.equals(""))
 				tie = false;
 			else {
 				if(alle)
 					term.append("%20");
 				term.append("title:"+title);
 			}
 
 			if(author != null && !author.equals("")) {
 				if(alle || tie)
 					term.append("%20");
 				term.append("author:"+author);
 			}
 
 			return term.toString();
 		} catch (UnsupportedEncodingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
 	
 	public String identifyConnection(){
 		
 		return "============================\nImported from Mendeley: \n";
 		
 		
 	}
 
 }
