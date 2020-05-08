 /**
  * Copyright 2013 Alex Wong, Ashley Brown, Josh Tate, Kim Wu, Stephanie Gil
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  * 
  */
 package ca.ualberta.cmput301f13t13.storyhoard.serverClasses;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.UUID;
 
 import org.apache.http.client.ClientProtocolException;
 
 import android.os.StrictMode;
 import ca.ualberta.cmput301f13t13.storyhoard.dataClasses.Story;
 
 /**
  * Role: Uses methods from the ESUpdates and ESRetrieval class to add, remove, 
  * update, and search for stories that have been published onto the server. This
  * class does not implement the storing manager because its retrieval methods are
  * much more varied than just retrieve(), and it also does not interact with the
  * database and therefore has no need for the methods existsLocally() or 
  * setSearchCriteria(). </br></br>
  * 
  * Design Pattern: Singleton
  * 
  * @author Stephanie Gil
  * 
  * @see ESUpdates
  * @see ESRetrieval
  */
 public class ServerManager {
 	private static ESRetrieval esRetrieval = null;
 	private static ESUpdates esUpdates = null;
 	private static ServerManager self = null;
 	public static String server = "http://cmput301.softwareprocess.es:8080/cmput301f13t13/stories/";
 	
 
 	/**
 	 * Initializes a new ServerManager.
 	 */
 	protected ServerManager() {
 		esUpdates = ESUpdates.getInstance();
 		esRetrieval = ESRetrieval.getInstance();
 	}
 
 	/**
 	 * Returns an instance of a ServerManager (singleton).
 	 * @return
 	 */
 	public static ServerManager getInstance() {
 		if (self == null) {
 			self = new ServerManager();
 		}
 		return self;
 	}
 	
 	/**
 	 * Changes the server index. Only used for by the JUnit tests so that
 	 * the tests can run in on a clean server with no other stories in it.
 	 * It also protects the real server used by the application by making
 	 * sure it is not polluted with half complete mock stories.
 	 */
 	public void setTestServer() {
 		server = "http://cmput301.softwareprocess.es:8080/cmput301f13t13/tests/";
 	}
 
 	/**
 	 * Inserts a story story onto the server. Note that the insertStory() from 
 	 * the ESUpdates class is the class that actually sends the story to the 
 	 * server. This is simply the method that can be called by other classes 
 	 * (mainly the ServerStoryController) outside of the package. This function 
 	 * also assumes that the given story is a complete story containing all its 
 	 * chapters and all the information wanted on the server. </br>
 	 * 
 	 * In this application, a complete story is gotten using methods in the 
 	 * controller classes. </br></br>
 	 * 
 	 * Example call: </br>
 	 * Story myStory = new Story(UUID, "My Cow", "John Blaine", "a little
 	 * 								 cow", phoneId). </br>
 	 * ServerManager sm = ServerManager.getInstance(context); </br>
 	 * sm.insert(myStory); </br>
 	 * 
 	 * Note that phone id can be retrieved through the static method 
 	 * getPhoneId() in the Utilities class of the application. Context
 	 * can also be an activity or application context.
 	 * 
 	 * @param story
 	 * 			Story to be inserted in the server. Assumed to be complete.
 	 */	
 	public void insert(Story story){
 		esUpdates.insertStory(story, server);
 	}
 
 	/**
 	 * Calls searchById() in the ESRetrieval class to retrieve a story on the 
 	 * server by id. If no response matching the given id is found, the value 
 	 * returned is null. Note also that in this application, the id's will be 
 	 * strings in the format of a UUID. The ids will also be corresponding to 
 	 * story ids, and the responses' content are story objects in JSON string 
 	 * format. </br></br>
 	 * 
 	 * An example call:
 	 * </br></br>
	 * 	String server = "http://cmput301.softwareprocess.es:8080/cmput301f13t13/stories/" </br>
 	 *  UUID id = 5231b533-ba17-4787-98a3-f2df37de2aD7; </br> 
 	 *  Story myStory = getById(id); </br></br> 
 	 *  
 	 *  myStory will contain the story that was searched for, or null if it  
 	 *  didn't exist on the server. </br></br>
 	 *  
 	 *  One more thing to note is that this operation is currently done on the
 	 *  main UI thread of the android application, which is why the 
 	 *  ThreadPolicy code is needed.
 	 *  
 	 * @param id 
 	 * 			Will be a 128-bit UUID value. See the above example to see the
 	 * 			format of a UUID object.
 	 */
 	public Story getById(UUID id) {
 		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
 		.permitAll().build();
 		StrictMode.setThreadPolicy(policy);
 
 		// search by id
 		return esRetrieval.searchById(id.toString(), server);	
 	}
 
 
 	/**
 	 * Uses the retrieve method in the ESRetrieval class to get all of the 
 	 * stories available on the server. An empty array list will be returned
 	 * if no stories are available. </br></br>
 	 * 
 	 * Example call: </br>
 	 * ArrayList<Story> stories = getAll(); 
 	 * 
 	 * @return
 	 */
 	public ArrayList<Story> getAll() {
 		ArrayList<Story> stories = new ArrayList<Story>();
 		try {
 			stories = esRetrieval.retrieve(null, server);
 		} catch (ClientProtocolException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}		
 		return stories;
 	}
 
 	/**
 	 * Builds a string query to randomly sort stories on the server and 
 	 * retrieves a maximum of one to return (of there are no stories on 
 	 * the server, null will be returned). It also uses the retrieve 
 	 * method from the ESRetrieval class to do so. </br></br>
 	 * 
 	 * Example call: </br>
 	 * Story story = getRandom(); </br></br>
 	 * 
 	 * One more thing to note is that this operation is currently done on the
 	 * main UI thread of the android application, which is why the 
 	 * ThreadPolicy code is needed.
 	 *  
 	 * @return
 	 */
 	public Story getRandom() {
 		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
 		.permitAll().build();
 		StrictMode.setThreadPolicy(policy);
 		
 		String query = "{\"query\": {\"custom_score\" : {\"script\" : "
 				+ "\"random()\", \"query\" : {\"match_all\" : {}}}}, " +
 				"\"sort\" : {\"_score\" : {\"order\" :\"desc\"}}, \"size\" :" 
 				+ " 1 }";
 		ArrayList<Story> stories = new ArrayList<Story>();	
 		try {
 			stories = esRetrieval.retrieve(query, server);
 		} catch (ClientProtocolException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}	
 		if (stories.size() != 1) {
 			return null;
 		} else {
 			return stories.get(0);
 		}		
 	}
 
 	/**
 	 * Builds a query string to find all stories on the server that contain
 	 * the given keywords in their title. This method uses the retrieve
 	 * method in ESRetrieval to actually do the retrieval. </br></br>
 	 * </br> Note also that the titles must contain ALL the keywords
 	 * in their title, not just at least one of them. </br>
 	 * It is also assumes that the string of keywords passed in contains
 	 * the keywords separated by whitespace. </br></br>
 	 * 
 	 * Example Call: </br>
 	 * String keywords = "dog cat blue sky";
 	 * ArrayList<Story> stories = searchByKeywords(keywords); </br></br>
 	 * 
 	 * 
 	 * @param keywords
 	 * 			The keywords that you want appearing in the title of the
 	 * 			stories you are searching. Each keyword must be separated 
 	 * 			by whitespace.
 	 */ 
 	public ArrayList<Story> searchByKeywords(String keywords) {
 		
 		String selection = prepareKeywords(keywords);
 		String query = "{\"query\" : {\"query_string\" : {\"default_field\""
 				+ " : \"title\",\"query\" : \"" + selection + "\"}}}";	
 
 		ArrayList<Story> stories = new ArrayList<Story>();
 		try {
 			stories =  esRetrieval.retrieve(query, server);
 		} catch (ClientProtocolException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return stories;
 	}
 
 	/**
 	 * This method first checks whether or not the story to be updated exists
 	 * on the server. If it doesn't, then the insert method is called. If it
 	 * does, then it updates the story currently on the server. </br></br>
 	 *  
 	 * Updating is done by first removing the story on the server with the 
 	 * deleteStory() method in ESUpdates, and then by re-inserting it using
 	 * the insert() method in ESUpdates. </br></br>
 	 * 
 	 * Example call: </br>
 	 * Assuming a story with the id below already exists on the server, </br>
 	 * UUID id = f1bda3a9-4560-4530-befc-2d58db9419b7; </br>
 	 * Story newStory = new Story(id, "new title", new author", null, null); 
 	 * </br> update(newStory); </br></br>
 	 * 
 	 * The story on the server will now have the title "new title" and 
 	 * "new author", replacing whatever old values it had before. </br>
 	 * Any part of the story can be updated, including objects and fields
 	 * of objects contained within it (eg. chapter, choice text, media).
 	 * 
 	 * @param story
 	 * 			Story with updates/new data that you want to change.
 	 */
 	public void update(Story story) { 
 		String id = story.getId().toString();
 
 		// story already on server
 		if (esRetrieval.searchById(id, server) != null) {
 			try {
 				esUpdates.deleteStory(id, server);
 				esUpdates.insertStory(story, server);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		} else {
 			insert(story);
 		}
 	}	
 
 	/**
 	 * Removes a story from the server. It calls the deleteStory() method
 	 * in ESUpdates to do so. It deletes the story with the matching id  passed
 	 * to this method. Note that the id is passed as a String, not a UUID. But,
 	 * the string will still be in the format of a UUID. </br></br>
 	 * 
 	 * Example call: </br> 
 	 * Assuming a story exists on the server with the below id, </br>
 	 * String id = f1bda3a9-4560-4530-befc-2d58db9419b7; </br>
 	 * remove(id); </br>
 	 * 
 	 * @param id
 	 * 			In the format of a UUID, but is a String. See above example
 	 * 			for the valid format of a UUID.
 	 */
 	public void remove(String id) { 
 		try {
 			esUpdates.deleteStory(id, server);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}		
 	}	
 
 	/**
 	 * This method is a helper method for the searchByKewords() method. It
 	 * takes in a String of keywords, assuming each keyword is separated
 	 * by whitespace, and builds a string to be used in the query string 
 	 * later. </br></br>
 	 * 
 	 * Example call: </br>
 	 * String keywords = "dog cat rain" </br>
 	 * String selection = prepareKeywords(keywords); </br></br>
 	 * 
 	 * The value of selection is: "dog AND cat AND rain" </br>
 	 * 
 	 * </br> If the story's title is null, then an empty selection string
 	 * will be returned, not null.
 	 * 
 	 * @param keywords
 	 * 			They keywords to build the query from. Must be separated by
 	 * 			whitespace.
 	 */
 	private String prepareKeywords(String keywords) {
 		String selection = "";
 
 		if (keywords == null) {
 			return selection;
 		}
 
 		// split keywords and clean them
 		List<String> words = Arrays.asList(keywords.split("\\s+"));
 
 		if (words.size() > 0) {
 			selection += words.get(0);
 		}
 
 		for (int i = 1; i < words.size(); ++i) {
 			selection += " AND " + words.get(i);
 		}
 		return selection;
 	}
 }
