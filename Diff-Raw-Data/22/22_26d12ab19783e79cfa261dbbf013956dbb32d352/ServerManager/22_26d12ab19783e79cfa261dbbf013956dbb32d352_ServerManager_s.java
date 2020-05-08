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
 package ca.ualberta.cmput301f13t13.storyhoard.backend;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.UUID;
 
 import org.apache.http.client.ClientProtocolException;
 
 import android.os.AsyncTask;
 import android.os.StrictMode;
 
 /**
  * Role: Uses functions from the ESClient to add, remove, update, and 
  * search for stories that have been published onto the server. All 
  * operations except for retrieving stories are performed on a different
  * thread than the main UI thread.
  * 
  * </br> 
  * Design Pattern: Singleton
  * 
  * @author Stephanie Gil
  * 
  * @see ESClient
  * @see StoringManager
  */
 public class ServerManager implements StoringManager {
 	private static ESClient esclient = null;
 	private static ServerManager self = null;
 
 	/**
 	 * Initializes a new ServerManager.
 	 */
 	protected ServerManager() {
 		esclient = new ESClient();
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
 	 * Inserts a story object onto the server.The insertStory from the ESClient 
 	 * class, here the story is prepared (all its images are first turned into 
 	 * strings to be stored), and this method also sets up a new thread for the
 	 * task.
 	 * 
 	 * </br>
 	 * Eg. Story myStory = new Story(UUID, "My Cow", "John Blaine", "a little
 	 * 								 cow", phoneId).
 	 * </br>
 	 * ServerManager sm = ServerManager.getInstance(context);
 	 * </br>
 	 * sm.insert(myStory);
 	 * 
 	 * @param object
 	 * 			Story object to be inserted.
 	 */	
 	@Override
 	public void insert(Object object){
 		new AsyncTask<Object, Void, Void>()
 		{
 			@Override
 			protected Void doInBackground(Object... params)
 			{
 				Story story = (Story) params[0];
 				prepareStory(story);
 				esclient.insertStory(story);
 				return null;
 			}
 
 			@Override
 			protected void onPostExecute(Void result) {
 				super.onPostExecute(result);
 			}
 		}.execute(object);
 	}
 
 	/**
 	 * Due to performance issues, Media objects don't actually hold Bitmaps.
 	 * A path to the location of the file is instead saved and used to 
 	 * retrieve bitmaps whenever needed. Media objects can also hold the
 	 * bitmaps after the have been converted to a string (Base64). 
 	 * </br>
 	 * Before a Story can be inserted into the server, all the images 
 	 * belonging to the story's chapters must have their bitmap strings
 	 * set. This is only done when the story is published to avoid doing
 	 * the expensive conversion unnecessarily (local stories only need to 
 	 * know the path).
 	 * </br>
 	 * 
 	 * This function takes care of setting all the Medias' bitmap strings.
 	 * 
 	 * @param story
 	 */
 	private void prepareStory(Story story) {
 
 		// get any media associated with the chapters of the story
 		HashMap<UUID, Chapter> chaps = story.getChapters();
 
 		for (UUID key : chaps.keySet()) {
 			Chapter chap = chaps.get(key);
 			ArrayList<Media> photos = chap.getPhotos();
 
 			for (Media photo : photos) {
 				photo.setBitmapString(photo.getBitmap());
 			}
 			chap.setPhotos(photos);
 
 			ArrayList<Media> ills = chap.getIllustrations();
 			for (Media ill : ills) {
 				ill.setBitmapString(ill.getBitmap());
 			}
 			chap.setIllustrations(ills);
 		}
 		story.setChapters(chaps);
 	}
 
 	/**
 	 * Retrieves stories from the server. Calls the delegateSearch method
 	 * to determine whether it is a search by id, get all, or search by 
 	 * keywords. It also sets the permission for the main UI thread to
 	 * do networking. The actual interaction with the server is done by
 	 * calling the corresponding methods in ESClient (done in the 
 	 * delegateSearch method).
 	 * 
 	 * </br> An example call
 	 * </br> Searching for a specific story by id.
 	 * </br> Story criteria = new Story(UUID, null, null, null, null). OR
 	 * </br> Searching for stories by keywords.
 	 * </br> Story criteria = new Story(null, "love", null, null, null). OR
 	 * </br> Searching for all available published stories.
 	 * </br> Story criteria = new Story(null, null, null, null, null).
 	 * </br></br> Retrieving story/stories.
 	 * </br> ServerManager sm = ServerManager.getInstance(context);
 	 * </br> ArrayList<Object> objs = sm.retrieve(criteria);
 	 * 
 	 * @param criteria
 	 */
 	@Override
 	public ArrayList<Object> retrieve(Object criteria) {
         StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                 .permitAll().build();
         StrictMode.setThreadPolicy(policy);
         
 		return delegateSearch(criteria);
 	}
 
 	/**
 	 * Calls the correct retrieval method in ESCLient depending on
 	 * what search criteria is given. You can either search by Id,
 	 * by keywords in the title, or search for all published stories.
 	 * Note that this method is always called by the retrieve method,
 	 * never on its own outside of this class.
 	 * 
 	 * </br> An example call
 	 * </br> Searching for a specific story by id.
 	 * </br> Story criteria = new Story(UUID, null, null, null, null). OR
 	 * </br> Searching for stories by keywords.
 	 * </br> Story criteria = new Story(null, "love", null, null, null). OR
 	 * </br> Searching for all available published stories.
 	 * </br> Story criteria = new Story(null, null, null, null, null).
 	 * </br></br> Retrieving story/stories.
 	 * </br> ServerManager sm = ServerManager.getInstance(context);
 	 * </br> ArrayList<Object> objs = sm.delegateSearch(criteria);
 	 * 
 	 * @param object
 	 * 			Story object holding search criteria.
 	 * @return matching stories
 	 */
 	private ArrayList<Object> delegateSearch(Object object) {
 		Story crit = (Story) object;
 		ArrayList<Object> stories = new ArrayList<Object>();
 
 		if (crit.getId() != null) { 
 
 			// search by id
 			Story story = esclient.searchById(crit.getId().toString());
 			if (story != null) {
 				stories.add(story);
 			}
 		} else {
 
 			// search for multiple stories
 			try {
 				ArrayList<String> sargs = new ArrayList<String>();
 				HashMap<String, String> storyData = crit.getSearchCriteria();
 
 				// setting selection string
 				for (String key: storyData.keySet()) {
 					sargs.add(storyData.get(key));
 				}
 
 				String selection = setSearchCriteria(crit, sargs);	
 
 				stories = esclient.searchStories(crit, selection);
 			} catch (ClientProtocolException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}			
 		} 
 
 		return stories;
 	}
 	
 	/**
 	 * Updates a story on the server. This is done by first deleting the
 	 * story matching the object to be updated's id, and then re-inserting
 	 * it. This method sets up a new thread to do the updating on, and uses
 	 * ESClient methods to remove and insert the story.
 	 * 
 	 * </br> Example call.
 	 * </br> Story newStory = new Story(exisitingUUID, "new title", 
 	 * 									"new author", null, null);
 	 * </br> ServerManager sm = ServerManager.getInstance(context);
 	 * </br> sm.update(newStory);
 	 * 
 	 * @param object
 	 * 			Story with updates/new data that you want to publish.
 	 */
 	@Override
 	public void update(Object object) { 
 		new AsyncTask<Object, Void, Void>()
 		{
 			@Override
 			protected Void doInBackground(Object... params)
 			{
 				Story story = (Story) params[0];
 
 				try {
 					esclient.deleteStory(story);
 					esclient.insertStory(story);
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 				return null;
 			}
 
 			@Override
 			protected void onPostExecute(Void result) {
 				super.onPostExecute(result);
 			}
 		}.execute(object);		
 	}	
 
 	/**
 	 * Removes a story from the server. It deletes the story with
 	 * the matching id of the story passed to it.
 	 * 
 	 * </br> Example call.
 	 * </br> Story criteria = (UUID, null, null, null, null);
 	 * </br> ServerManager sm = ServerManager.getInstance(context);
 	 * </br> sm.remove(criteria);
 	 * 
 	 * @param object
 	 * 			Story with id of the story you want to delete from server. 
 	 */
 	public void remove(Object object) { 
 		new AsyncTask<Object, Void, Void>()
 		{
 			@Override
 			protected Void doInBackground(Object... params)
 			{
 				Story story = (Story) params[0];
 				
 				try {
 					esclient.deleteStory(story);
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 				return null;
 			}
 
 			@Override
 			protected void onPostExecute(Void result) {
 				super.onPostExecute(result);
 			}
 		}.execute(object);			
 	}	
 
 	/**
 	 * Sets the selection string for doing a keyword search of story title.
 	 * The argument args is not needed when dealing with the server, it is
 	 * used with the classes that implement StoringManager and interact with
 	 * the database.
 	 * 
 	 * </br> Example. Story crit = new Story(null, "ham butter eggs", null,
 	 * 										null, null);
 	 * </br> String selection = setSearchCriteria(crit, null);
 	 * 
 	 * </br> selection holds "ham AND butter AND eggs".
 	 * 
 	 * </br> If the story's title is null, then an empty selection string
 	 * will be returned.
 	 * 
 	 * @param object
 	 */
 	@Override
 	public String setSearchCriteria(Object object, ArrayList<String> args) {
 		String selection = "";
 		Story story = (Story) object;
 
 		if (story.getTitle() == null) {
 			return selection;
 		}
 
 		String allWords = story.getTitle();
 
 		// split keywords and clean them
 		List<String> words = Arrays.asList(allWords.split("\\s+"));
 
 		if (words.size() > 0) {
 			selection += words.get(0);
 		}
 
 		for (int i = 1; i < words.size(); ++i) {
 			selection += " AND " + words.get(i);
 		}
 		return selection;
 	}
 }
