 package com.game.server;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.Collections;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import com.game.client.GreetingService;
 import com.game.shared.FieldVerifier;
 import com.google.gwt.user.server.rpc.RemoteServiceServlet;
 import net.sf.jsr107cache.Cache;
 import net.sf.jsr107cache.CacheException;
 import net.sf.jsr107cache.CacheManager;
 
 /**
  * The server side implementation of the RPC service.
  */
 @SuppressWarnings("serial")
 public class GreetingServiceImpl extends RemoteServiceServlet implements
 		GreetingService {
 
 
 	private String[] tokenizeString(String sentence) throws IllegalArgumentException {
 		sentence = sentence.toLowerCase();
 		String[] words = sentence.split("\\s+");
 		return words;
 	}
 
 	public String greetServer(String input) throws IllegalArgumentException {
 		// Verify that the input is valid. 
 		if (!FieldVerifier.isValidName(input)) {
 			// If the input is not valid, throw an IllegalArgumentException back to
 			// the client.
 			throw new IllegalArgumentException(
 					"Name must be at least 4 characters long");
 		}
 
 		String serverInfo = getServletContext().getServerInfo();
 		String userAgent = getThreadLocalRequest().getHeader("User-Agent");
 
 		// Escape data from the client to avoid cross-site script vulnerabilities.
 		input = escapeHtml(input);
 		userAgent = escapeHtml(userAgent);
 
 		return "Hello, " + input + "!<br><br>I am running " + serverInfo
 				+ ".<br><br>It looks like you are using:<br>" + userAgent;
 	}
 	
 	//Utility Functions for using the memcache
 	public boolean isPresentCache(String key){
 		try{
 			Cache cache;
 			cache = CacheManager.getInstance().getCacheFactory().createCache(Collections.emptyMap());
 			return cache.containsKey(key);
 		}
 		catch(CacheException e){
 			return false; 
 		}
 	}
 	public Object getCached(String key){
 		if(isPresentCache(key)){
 			try{
 				Cache cache;
 				cache = CacheManager.getInstance().getCacheFactory().createCache(Collections.emptyMap());
 				return cache.get(key);
 			}
 			catch(CacheException e){
 				return null;
 			}
 		}
 		else{
 			return null;
 		}
 	}
 	public void putCached(String key, Object value){
 		try{
 			Cache cache;
 			cache = CacheManager.getInstance().getCacheFactory().createCache(Collections.emptyMap());
 			cache.put(key, value);
 		}
 		catch(CacheException e){
 
 		}
 	}
 
 	/**
 	 * Escape an html string. Escaping data received from the client helps to
 	 * prevent cross-site script vulnerabilities.
 	 * 
 	 * @param html the html string to escape
 	 * @return the escaped string
 	 */
 	private String escapeHtml(String html) {
 		if (html == null) {
 			return null;
 		}
 		return html.replaceAll("&", "&amp;").replaceAll("<", "&lt;")
 				.replaceAll(">", "&gt;");
 	}
 
 	String[] UserIDs;
 	String[] Sentences;
 	String[] URLs;
 //	ArrayList<String> UserIDs = new ArrayList<String>();
 //	ArrayList<String> Sentences = new ArrayList<String>();
 //	ArrayList<String> URLs = new ArrayList<String>();
 	int Usercount = 0;
 	int NumUsers = 0;
 	int RoundNum = 0;
 	int NumURLs = 10;
 	int sentence_ctr = 0;
 	int userids_ctr = 0;
 	int urls_ctr = 0;
 	boolean Step1Done = false;
 	
 	/**
 	 * Initialize the game; check if the first round is finished
 	 * 
 	 * @param input ArrayList of strings - always in this order:
 	 * User ID
 	 * Number of users
 	 * Sentence
 	 * @return none
 	 */
 	@Override
 	public ArrayList<String> PushPhraseInit(ArrayList<String> input)
 			throws IllegalArgumentException {
 
 		if(NumUsers == 0) {
 			NumUsers = Integer.parseInt(input.get(1));
 			Usercount = NumUsers;
 			Sentences = new String[(int) (Usercount*Math.ceil((float)Usercount/2.0))];
 			URLs = new String[(int) (Usercount*Math.ceil((float)Usercount/2.0)*NumURLs)];
 			UserIDs = new String[2*Usercount];
 		}
 		
 		Sentences[sentence_ctr++] = input.get(2);
 		UserIDs[userids_ctr++] = input.get(0);
 		UserIDs[userids_ctr++] = Integer.toString(userids_ctr/2-1);
 		// Check if we've inputed all the sentences from the first round
 		Step1Done = true;
 		if(NumUsers > 0) {
 			for(int i=0; i<NumUsers;i++) {
 				if(Sentences[i] == null) {
 					Step1Done = false;
 				}
 			}
 			
 		}
 		else {
 			Step1Done = false;
 		}
 		return null;
 	}
 
 	/**
 	 * check if step 1 is done
 	 * We know it's done because every client is added to UserIDs.
 	 * 
 	 * @param none
 	 * @return boolean value Step1Done
 	 */
 	@Override
 	public ArrayList<String> IstStep1Done(ArrayList<String> input)
 			throws IllegalArgumentException {
 		
 		// TODO return Step1Done, change all instances of this method to boolean
 		ArrayList<String> ret = new ArrayList<String>();
 		ret.add(Boolean.toString(Step1Done));
 		return ret;
 	}
 
 	/**
 	 * returns the sentence that the user is going to be drawing pictures for
 	 * 
 	 * @param input - the userid of the person that wants a sentence(?)
 	 * @return ret - an ArrayList<String> of one element size, the sentence
 	 */
 	@Override
 	public ArrayList<String> GetPhraseGame(ArrayList<String> input)
 			throws IllegalArgumentException {
 		// TODO Auto-generated method stub
 		ArrayList<String> ret = new ArrayList<String>();
 //		ret.add("Ming Wang");
 		//whoseSheet() will return a string that tells me whose sheet to read from
 		int round = calcRound();
 		int index = whoseSheet(input.get(0),round);
 		ret.add(Sentences[(round-1)/2*NumUsers + (index)]);
 		return ret;
 	}
 	
 	
 	@Override
 	public ArrayList<String> QueryImage(ArrayList<String> input)
 			throws IllegalArgumentException {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	/**
 	 * Stores the pictures a user selected during a round into the right spot
 	 * 
 	 * @param input ArrayList of strings - always in this order:
 	 * User ID
 	 * Number of users
 	 * first link
 	 * ...
 	 * last link
 	 * 
 	 * The urls are stored [user 1's sheet, user 2's sheet, ....]
 	 * each sheet is stored [first set of pics, second set of pics, ...]
 	 * a set of pictures is at most NumURLs
 	 * 
 	 * @return none
 	 */	
 	public ArrayList<String> SubmitLinks(ArrayList<String> input)
 			throws IllegalArgumentException {
 		int round = calcRound();
 		int uid_to_write_to = whoseSheet(input.get(0), round);
 		//int num_users=Integer.parseInt(input.get(1));
 		int start=uid_to_write_to*((int) Math.floor(NumUsers/2))*NumURLs+NumURLs*((round-1)/2);
 		for (int i=1; i<NumURLs+1; i++){
 			if(i<Math.min(input.size(),NumURLs+1)){
 				URLs[start+i-1]=input.get(i);
 			}
 			else{
 				URLs[start+i-1] = "";
 			}
 		}
 		return null;
 	}
 	
 	/**
 	 * Retrieves the pictures a user must describe for the round
 	 * 
 	 * @param input ArrayList of strings - always in this order:
 	 * User ID
 	 * Number of users
 	 * 
 	 * The urls are stored [user 1's sheet, user 2's sheet, ....]
 	 * each sheet is stored [first set of pics, second set of pics, ...]
 	 * a set of pictures is at most NumURLs
 	 * 
 	 * @return none
 	 */		
 	public ArrayList<String>GetImagesGame(ArrayList<String> input) 
 	throws IllegalArgumentException{
 		
 		ArrayList<String> resultSet = new ArrayList<String>();
 		int round = calcRound();
 		int sheetIhave=whoseSheet(input.get(0), round);
 		//int num_users=Integer.parseInt(input.get(1));
 		
 		int LastRound = round -1;
 		int start=sheetIhave*((int) Math.floor(NumUsers/2))*NumURLs+NumURLs*((LastRound-1)/2);
 		for (int i=0; i<NumURLs; i++){
 			String item=URLs[start+i];
 			if(item!=null){
 				resultSet.add(item);
 			}else{
 				break;
 			}
 		}
 		return resultSet;
 
 	}
 
 	@Override
 	public ArrayList<String> IsStep3Done(ArrayList<String> input)
 			throws IllegalArgumentException {
 		int round = calcRound();
 		ArrayList<String> ret = new ArrayList<String>();
 		
 		if(round % 2 == 0) {
 			ret.add(Boolean.toString(false));
 		}
 		else {
 			ret.add(Boolean.toString(true));
 		}
 		return ret;
 		/*
 		// TODO Auto-generated method stub
 		boolean[] doneround = new boolean[URLs.length/NumURLs];
 
 		for(int i=0; i<URLs.length/NumURLs;i++) {
 			doneround[i] = false;
 			for(int j=0;j<NumURLs;j++) {
 				if(URLs[i*NumURLs+j] != null) {
 					doneround[i] = true;
 				}
 			}
 		}
 		boolean returnval = true;
 		for(int i=0; i<doneround.length;i++) {
 			if(doneround[i] == false) {
 				returnval = false;
 			}
 		}
 		ArrayList<String> ret = new ArrayList<String>();
 		ret.add(Boolean.toString(returnval));
 		return ret;
 		*/
 	}
 
 	@Override
 	public ArrayList<String> GetPictures(ArrayList<String> input)
 			throws IllegalArgumentException {
 		ArrayList<String> results = new ArrayList();
 	    String query_url;
 		try {
 			query_url = "http://query.yahooapis.com/v1/public/yql?q=" + 
 				URLEncoder.encode("SELECT * FROM flickr.photos.search WHERE text=\""+input.get(0)+"\"", "UTF-8")+
 				"&format=xml";
 		} catch (UnsupportedEncodingException e) {
 			// TODO Auto-generated catch block
 			return null;
 		}
 	    
 	    URL u;
 	    HttpURLConnection conn;
 		try {
 			u = new URL(query_url);
 			conn = (HttpURLConnection) u.openConnection();
 		    conn.setRequestMethod("GET");
 	        conn.setDoOutput(true);
 	        conn.setReadTimeout(10000);
 	        conn.connect();
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			return null;
 		}
 
 
         
         DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
         DocumentBuilder db;
 		try {
 			db = dbf.newDocumentBuilder();
 			Document doc = db.parse(conn.getInputStream());
 			doc.getDocumentElement().normalize();
 	        NodeList nl=doc.getElementsByTagName("results");
 	        Node current_result = nl.item(0).getFirstChild();
 	 
 	        while (current_result != null){
         		String farm=current_result.getAttributes().getNamedItem("farm").getNodeValue();
         		String server= current_result.getAttributes().getNamedItem("server").getNodeValue();
         		String id = current_result.getAttributes().getNamedItem("id").getNodeValue();
         		String secret = current_result.getAttributes().getNamedItem("secret").getNodeValue();
         	
         	
         		results.add(String.format("http://farm%s.static.flickr.com/%s/%s_%s_t.jpg", farm, server,id, secret));
         	
         		current_result=current_result.getNextSibling();
         	}
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			return null;
 		}
         
         return results;
 	}
 
 
 	/**
 	 * stores the sentence submitted by the user in the ArrayList Sentences
 	 * Sentences is structured as [round 1's sentences, round 2's sentences, ...]
 	 * @param input - ArrayList of strings, in the order Sentence, uid
 	 * @return none
 	 */
 	@Override
 	public ArrayList<String> SubmitPhraseGame(ArrayList<String> input)
 			throws IllegalArgumentException {
 		// TODO Auto-generated method stub
 		//whoseSheet() will return a string that tells me whose sheet to write to
 		int round = calcRound();
 		int index = whoseSheet(input.get(0), round);
 		Sentences[(round/2)*NumUsers + (index)] = input.get(1);
 		return null;
 	}
 
 	/**
 	 * linear search through an array to find the index of an item
 	 * @param item - the item to search for
 	 * @param input - the input array
 	 * @return index of the item
 	 */
 	public int findItem(String item,String[] input) {
 		for(int i=0; i<input.length;i++) {
 			if(input[i].equals(item)) {
 				return i;
 			}
 		}
 		return -1;
 	}
 	
 	/**
 	 * this method calculates the round we're on based on the Sentence and URL arrays.
 	 * If this works the first time Greg is a fucking badass
 	 * @return the round number we're on, starting at 1
 	 */
 	private int calcRound() {
 		int sentence_rounds = 0;
 //		String counter = Sentences[0];
 		for(int i=0;i<Sentences.length;i++) {
 			if(Sentences[i] != null) {
 				sentence_rounds++;
 			}
 		}
 //		while(!(counter == (null))) {
 //			sentence_rounds++;
 //			counter = Sentences[sentence_rounds];
 //		}
 		//sentence_rounds = Sentences.length;
 		
 		sentence_rounds = (int)Math.floor((sentence_rounds)/(NumUsers));
 		int pic_rounds = 0;
 //		counter = URLs[0];
 		for(int i=0;i<URLs.length;i++) {
 			if(URLs[i] != null) {
 				pic_rounds++;
 			}
 		}
 //		while(!(counter == (null))) {
 //			pic_rounds++;
 //			counter = URLs[pic_rounds];
 //		}
 		pic_rounds = (int)Math.floor((pic_rounds)/(NumUsers*NumURLs));
 		return sentence_rounds+pic_rounds+1;
 	}
 	
 	
 	/**
 	 * @param userID
 	 * @param round
 	 * 
 	 * figures out whose sheet you are currently writing to (returns the uid)
 	 * -1 on error
 	 * 
 	 * @return
 	 */
 	public int whoseSheet (String userID, int round){
 		int myIndex=-1;
 		for (int i=0; i <UserIDs.length; i++){
 			if (UserIDs[i].contentEquals(userID)){
 				myIndex=i;
 				break;
 			}
 		}
 		if (myIndex==-1){
 			return -1;
 		}
 		int my_uid=Integer.parseInt(UserIDs[myIndex+1]);
 		return Integer.parseInt(UserIDs[2*((my_uid+round-1) % (UserIDs.length/2))+1]);
 	}
 	
 	@Override
 	public ArrayList<String> IsStep4Done(ArrayList<String> input)
 			throws IllegalArgumentException {
 		
 		int round = calcRound();
 		ArrayList<String> ret = new ArrayList<String>();
 		
 		if(round % 2 == 1) {
 			ret.add(Boolean.toString(false));
 		}
 		else {
 			ret.add(Boolean.toString(true));
 		}
 		return ret;
 	}
 
 	@Override
 	public ArrayList<String> IsGameDone(ArrayList<String> input)
 			throws IllegalArgumentException {
 		// TODO Auto-generated method stub
 		int round = calcRound();
 		ArrayList<String> ret = new ArrayList<String>();
 		if(round == 6){
 			ret.add("true");
 			return ret;
 		}
 		ret.add("false");
 		return ret;
 	}
 
 	@Override
 	public ArrayList<ArrayList<ArrayList<String>>> ViewAllGames(ArrayList<String> input)
 			throws IllegalArgumentException {
		
 		ArrayList<ArrayList<ArrayList<String>>> result = new ArrayList<ArrayList<ArrayList<String>>> ();
 		for (int i=0; i < NumUsers; i++){
 			ArrayList<ArrayList<String>> user_sheet = new ArrayList<ArrayList<String>> ();
 			ArrayList<String> name = new ArrayList<String>();
 			name.add(UserIDs[i*2]);
 			user_sheet.add(name);
 			for (int j=0; j<NumUsers;j++){
 				ArrayList<String> entry = new ArrayList<String>();
 				if (j%2==0){
 					//Sentence
 					entry.add(Sentences[j*NumUsers/2+i]);
 				} else {
 					//Image
					int start=(i*(NumUsers-1)*NumURLs/2)+((j-1)/2)*NumURLs;
 					for (int k=0; k<NumURLs;k++){
 						String u=URLs[start+k];
						if (u != ""){
 							entry.add(u);
 						}
 					}
 				}
 				user_sheet.add(entry);
 			}
 			result.add(user_sheet);
 		}
 		return result;
 	}
 }
