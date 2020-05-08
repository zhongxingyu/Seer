 /*
 ESManager Class for CreateYourOwnAdventure.
 This deals with the organization and management of stories interacting with the server.
 All saving & loading is handled here, along with deletion.
 
      Copyright  �2013 Jesse Chu, Reginald Miller, Jesse Huard
     <Contact: rmiller3@ualberta.ca, jhchu@ualberta.ca, jhuard@ualberta.ca>
     
     License GPLv3: GNU GPL Version 3
     <http://gnu.org/licenses/gpl.html>.
     
     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 package cmput301.f13t01.elasticsearch;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Random;
 import java.util.UUID;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.net.Uri;
 import android.util.Base64;
 import android.util.Log;
 import cmput301.f13t01.createyourownadventure.GlobalManager;
 import cmput301.f13t01.createyourownadventure.LibraryManager;
 import cmput301.f13t01.createyourownadventure.LocalManager;
 import cmput301.f13t01.createyourownadventure.Media;
 import cmput301.f13t01.createyourownadventure.Story;
import cmput301.f13t01.createyourownadventure.StoryBitmapFactory;
 import cmput301.f13t01.createyourownadventure.StoryFragment;
 import cmput301.f13t01.createyourownadventure.StoryFragmentInfo;
 import cmput301.f13t01.createyourownadventure.StoryInfo;
 import cmput301.f13t01.createyourownadventure.Text;
 
 /**
  * This class is designed to interact with stored stories on ElasticSearch.
  * Saving, Loading, and Deleting are handled.
  * 
  * @author Jesse Chu, Reginald Miller, Jesse Huard
  */
 
 public class ESManager implements LibraryManager {
 
 	private ESClient client;
 	private LocalManager localManager;
 	private Context context;
 
 	private int resultSize = 20;
 
 	/**
 	 * Initializes the ESClient and gets the LocalManager.
 	 * 
 	 * @param context   The context to be used for saving purposes.
 	 */
 	public ESManager(Context context) {
 		this.client = new ESClient();
 		this.localManager = GlobalManager.getLocalManager();
 		this.context = context;
 	}
 
 	/**
 	 * Method that returns a requested Story by ID and fetches all associated
 	 * resources.
 	 * 
 	 * @param storyId
 	 *            the ID of the requested Story
 	 * @return the requested story
 	 */
 	public Story getStory(UUID storyId) {
 
 		// Grabs the associated StoryResource object
 		StoryResource storyResource = client.getStoryResources(storyId);
 		ArrayList<MediaResource> resources = storyResource.getMediaResources();
 
 		for (MediaResource resource : resources) {
 			// Grabs base64 string
 			String identifier = resource.getIdentifier();
 			String type = resource.getType();
 			String media = client.getMedia(identifier, type);
 			localManager.saveMediaFromBase64(identifier, type, media);
 		}
 
 		return client.getStory(storyId);
 	}
 
 	/**
 	 * When this method is called, it handles the process of fetching a random story
 	 * amongst all stories available on the server.
 	 * 
 	 * @return   Returns the random Story object.
 	 */
 	public UUID getRandomOnlineStory() {
 		// Get count of total number of stories online
 		Integer totalOnlineStories = client.getStoryCount();
 		// Randomly selects an index within the total number
 		Random randSelect = new Random();
 		Integer index = randSelect.nextInt(totalOnlineStories);
 		// Grabs the StoryInfo using the client
 		ArrayList<StoryInfo> infoList = client.getStoryInfos(index, 1);
 		UUID oldId = infoList.get(0).getId();
 		UUID newId = downloadStory(oldId);
 		
 		return newId;
 	}
 
 	/**
 	 * Method to return an ArrayList of first batch of StoryInfo objects
 	 * 
 	 * @return an ArrayList of all StoryInfo
 	 */
 	public ArrayList<StoryInfo> getStoryInfoList() {
 		return client.getStoryInfos(0, resultSize);
 	}
 
 	/**
 	 * Fetches first batch of StoryInfos starting from the index start. Call
 	 * this if want new batch of StoryInfo objects.
 	 * 
 	 * @param start
 	 *            The start index of which StoryInfos to fetch (equal to current
 	 *            total number of StoryInfos)
 	 * @return The ArrayList of StoryInfos of size resultSize (or less if no
 	 *         more on server)
 	 */
 	public ArrayList<StoryInfo> getStoryInfoList(int start) {
 		return client.getStoryInfos(start, resultSize);
 	}
 
 	/**
 	 * Saves a story from the server.
 	 * 
 	 * @param id
 	 *            ID of the story to be posted.
 	 * @param story
 	 *            The story to be posted.
 	 */
 	public UUID downloadStory(UUID id) {
 		Story story = getStory(id);
 		UUID newId = localManager.addStory(story);
 		return newId;
 	}
 
 	/**
 	 * Method to remove/delete a Story and all associated files from the server.
 	 * 
 	 * @param storyId
 	 *            the ID of the Story to remove
 	 * @return true if successful, false otherwise
 	 */
 	public boolean removeStory(UUID storyId) {
 		try {
 			client.deleteStory(storyId);
 		} catch (IOException e) {
 			e.printStackTrace();
 			return false;
 		}
 
 		try {
 			client.deleteStoryResources(storyId);
 		} catch (IOException e) {
 			e.printStackTrace();
 			return false;
 		}
 
 		try {
 			client.deleteStoryInfo(storyId);
 		} catch (IOException e) {
 			e.printStackTrace();
 			return false;
 		}
 
 		return true;
 	}
 
 	/**
 	 * Publishes the story and all its resources to the server.
 	 * 
 	 * @param id
 	 *            Current ID of the story to be published
 	 * @param story
 	 *            The story to be saved
 	 * @return true if successful, false otherwise
 	 */
 	public boolean saveStory(UUID id, Story story) {
 
 		try {
 			client.postStory(id, story);
 		} catch (IOException e) {
 			e.printStackTrace();
 			return false;
 		} catch (IllegalStateException e) {
 			e.printStackTrace();
 			return false;
 		}
 
 		try {
 			client.postStoryInfo(new StoryInfo(id, story));
 		} catch (IOException e) {
 			e.printStackTrace();
 			return false;
 		} catch (IllegalStateException e) {
 			e.printStackTrace();
 			return false;
 		}
 
 		// Compiles the StoryResource to post
 		StoryResource storyResource = compileMediaResources(id, story);
 
 		try {
 			client.postStoryResources(storyResource);
 		} catch (IOException e) {
 			e.printStackTrace();
 			return false;
 		} catch (IllegalStateException e) {
 			e.printStackTrace();
 			return false;
 		}
 
 		ArrayList<MediaResource> mediaList = storyResource.getMediaResources();
 		for (MediaResource resource : mediaList) {
 			String identifier = resource.getIdentifier();
 			String type = resource.getType();
 			String base64Media = mediaToBase64(identifier, type);
 			try {
 				client.postMedia(identifier, type, base64Media);
 			} catch (IllegalStateException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 
 		return true;
 	}
 
 	/**
 	 * Returns an ArrayList of StoryInfo objects that match the results of what
 	 * was desired to be searched from the Browse Online Stories activity
 	 * 
 	 * @param title
 	 *            The input text of the title field
 	 * @param author
 	 *            The input text of the author field
 	 * @param description
 	 *            The input text of the description field
 	 * @param start
 	 *            The index to start searching for the objects
 	 * @return The ArrayList of fetched StoryInfo objects
 	 */
 	public ArrayList<StoryInfo> searchOnlineStories(String title,
 			String author, String description, int start) {
 		String query = SearchManager.createQuery(title, author, description);
 		ArrayList<StoryInfo> infos = client.getStoryInfosByQuery(query, start,
 				resultSize);
 		return infos;
 	}
 
 	private StoryResource compileMediaResources(UUID id, Story story) {
 		// Instantiate return value
 		StoryResource storyResource = new StoryResource(id);
 		// Get list of all fragments in the story
 		ArrayList<StoryFragmentInfo> fragmentList = story.getFragmentInfoList();
 		// Iterate over all fragments in the story
 		for (StoryFragmentInfo info : fragmentList) {
 			// Get each fragment
 			Integer fragmentId = info.getId();
 			StoryFragment fragment = story.getFragment(fragmentId);
 			// Add all content media to storyResource
 			ArrayList<Media> content = fragment.getContentList();
 			storyResource = extractMedia(storyResource, content);
 			// Add all annotation media to storyResource
 			ArrayList<Media> annotations = fragment.getAnnotationList();
 			storyResource = extractMedia(storyResource, annotations);
 		}
 		return storyResource;
 	}
 
 	private StoryResource extractMedia(StoryResource resourceList,
 			ArrayList<Media> mediaList) {
 		// Check all media in given list
 		for (Media media : mediaList) {
 			// Type check the media
 			Log.d("oops", "media content " + media.getContent());
 			Log.d("oops", "media type: " + media.getType());
 			Log.d("oops", "media class: " + media.getClass());
 			if (!media.getClass().equals(Text.class)) {
 				Log.d("oops", "Adding a new media: " + media.getResource());
 				// Media is not text, generate a MediaResource object
 				MediaResource resource = new MediaResource(media);
 				// Add to resource list if not already in it
 				if (!resourceList.contains(resource)) {
 					resourceList.addMediaResource(resource);
 				}
 			}
 		}
 		return resourceList;
 	}
 
 	private String mediaToBase64(String identifier, String type) {
 		File media = new File(context.getFilesDir().getAbsolutePath() + "/"
 				+ type + "/" + identifier);
 
 		String base64Media = new String();
 		Log.d("Base64", "media: " + media.getAbsolutePath());
 
 		// We are assuming that all media files are already compressed by the
 		// time that we try to upload them onto the server. Therefore, encoding
 		// into one string from the file shouldn't be a problem. If we extend
 		// support to larger files, we'll need to rewrite this portion of the
 		// function.
 		try {
 			byte[] buf = new byte[1024];
 			
 			Bitmap map = StoryBitmapFactory.decodeUri(Uri.fromFile(media), StoryBitmapFactory.MAX_SIZE, StoryBitmapFactory.MAX_SIZE, context);
 			Log.d("Base64", "map size: " + map.getRowBytes() * map.getHeight());
 			
 			base64Media += Base64.encodeToString(buf, Base64.NO_WRAP | Base64.NO_PADDING);
 			
 			ByteArrayOutputStream out = new ByteArrayOutputStream();
 			map.compress(Bitmap.CompressFormat.PNG, 100, out);
 			byte[] b = out.toByteArray();
 			Log.d("Base64", "Byte size: " + b.length);
 			base64Media = Base64.encodeToString(b, Base64.NO_WRAP
 						| Base64.NO_PADDING);
 			//Log.d("Base64", "Big encoded: " + base64Media);
 			out.close();
 		} catch (FileNotFoundException e) {
 			Log.d("Base64", "File not found for loading encoded media");
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		Log.d("Base64", "Encoded size: " + base64Media.length());
 		return base64Media;
 	}
 }
