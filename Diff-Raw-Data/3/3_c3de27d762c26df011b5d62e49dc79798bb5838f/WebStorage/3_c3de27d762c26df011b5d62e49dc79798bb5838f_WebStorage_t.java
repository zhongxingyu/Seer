 /*
  *        Copyright (c) 2013 Andrew Fontaine, James Finlay, Jesse Tucker, Jacob Viau, and
  *         Evan DeGraff
  *
  *         Permission is hereby granted, free of charge, to any person obtaining a copy of
  *         this software and associated documentation files (the "Software"), to deal in
  *         the Software without restriction, including without limitation the rights to
  *         use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
  *         the Software, and to permit persons to whom the Software is furnished to do so,
  *         subject to the following conditions:
  *
  *         The above copyright notice and this permission notice shall be included in all
  *         copies or substantial portions of the Software.
  *
  *         THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  *         IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
  *         FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
  *         COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
  *         IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
  *         CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
  */
 
 package ca.cmput301f13t03.adventure_datetime.model;
 
 import ca.cmput301f13t03.adventure_datetime.model.Interfaces.IWebStorage;
 import io.searchbox.Action;
 import io.searchbox.client.JestClient;
 import io.searchbox.client.JestResult;
 import io.searchbox.core.*;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.UUID;
 
 
 /**
  * Class for interacting with the ES service
  * The latest error string can be retrieved via getErrorString
  */
 public class WebStorage implements IWebStorage {
 	
 	private static final String MATCH_ALL = 
 		"{\n" +
 		"	\"from\" : %s, \"size\" : %s,\n" +
 		"%s" + // For an optional SORT. No newline on purpose
 		"  		\"query\" : {\n" +
 		"    		\"match_all\" : {}\n" +
 		"  		}\n" +
 		"}";
 	
 	private static final String MATCH =
 		"{\n" +
 		"	\"from\" : %s, \"size\" : %s,\n" +
 		"%s" + // For an optional SORT. No newline on purpose
 		"  		\"query\" : {\n" +
 		"    		\"match\": {\n" +
 		"      			\"%s\" : {\n" +
 		"        			\"query\" : \"%s\",\n" +
 		"        				\"type\" : \"boolean\"\n" +
 		"     	 		}\n" +
 		"    		}\n" +
 		"  		}\n" +
 		"}";
 	
 	private static final String MULTI_MATCH =
 		"{\n" +
 	    "	\"from\" : %s, \"size\" : %s,\n" +
 	    "%s" + // For an optional SORT. No newline on purpose
 	    "	\"query\" : {\n" +
 	    "    	\"multi_match\" : {\n" +
 	    "       	\"query\" : \"%s\",\n" +
 	    "      		\"fields\" : [\"author^3\", \"title^3\", \"tags^2\", \"synopsis\"]\n" +
 	    "    	}\n" +
 	    "	}\n" +
 		"}";
 	
 	private static final String MULTI_ID = 
 		"{\n" +
 		"	\"from\" : %s, \"size\" : %s,\n" +
 		"%s" + // For an optional SORT. No newline on purpose
 		"	\"query\" : {\n" +
 		"		\"ids\" : {\n" +
 		"			\"values\" : [%s]\n" +
 		"		}\n" +
 		"	}\n" +
 		"}";
 	
 	private static final String SORT = 
 	    "	\"sort\": [\n" +
 	    "		{\n" +
 	    "			\"%s\": {\n" +
 	    "				\"order\": \"%s\"\n" +
 		"			}\n" +
 		"		}\n" +
 		"	],\n";  
 	
 	private static final String defaultIndex = "cmput301f13t03";
 	
 	private JestClient client;
 	private String errorMessage;
 	private String jsonString;
 	private String _index;
 	
 	/**
 	 * Construct a basic WebStorage object
 	 */
 	public WebStorage() {
 		client = ES.Client.getClient();
 		_index = defaultIndex;
 	}
 
 	/* (non-Javadoc)
 	 * @see ca.cmput301f13t03.adventure_datetime.model.IWebStorage#getStory(java.util.UUID)
 	 */
 	@Override
 	public Story getStory(UUID storyId) throws Exception {
 		Get get = new Get.Builder(_index, storyId.toString()).type("story").build();
 		JestResult result = execute(get);
 		Story story = result.getSourceAsObject(Story.class);
 		
 		Image image = getImage(storyId);
 		if (image != null)
 			story.setThumbnail(image.getEncodedBitmap());
 		
 		return story;
 	}
 	
 	/* (non-Javadoc)
 	 * @see ca.cmput301f13t03.adventure_datetime.model.IWebStorage#getStories(int, int)
 	 */
 	@Override
 	public List<Story> getStories(int from, int size) throws Exception {
 		String sort = String.format(SORT, "timestamp", "desc");
 		String query = String.format(MATCH_ALL, from, size, sort);
 		Search search = new Search.Builder(query)
 			.addIndex(_index)
 			.addType("story")
 			.build();
 		
 		JestResult result = execute(search);
 		List<Story> stories = result.getSourceAsObjectList(Story.class); 
 		mapImagesToStories(stories);
 				
 		return stories;
 	}
 	
 	/* (non-Javadoc)
 	 * @see ca.cmput301f13t03.adventure_datetime.model.IWebStorage#queryStories(Java.lang.String, int, int)
 	 */
 	public List<Story> queryStories(String filter, int from, int size) throws Exception {
 		String sort = String.format(SORT, "timestamp", "desc");
 		String query = String.format(MULTI_MATCH, from, size, sort, filter);
 		Search search = new Search.Builder(query)
 			.addIndex(_index)
 			.addType("story")
 			.build();
 		
 		JestResult result = execute(search);
 		List<Story> stories = result.getSourceAsObjectList(Story.class); 
 		mapImagesToStories(stories);
 				
 		return stories;
 	}
 	
 	/* (non-Javadoc)
 	 * @see ca.cmput301f13t03.adventure_datetime.model.IWebStorage#getFragment(java.util.UUID)
 	 */
 	@Override
 	public StoryFragment getFragment(UUID fragmentId) throws Exception {
 		Get get = new Get.Builder(_index, fragmentId.toString()).type("fragment").build();
 		JestResult result = execute(get);
 		StoryFragment fragment = result.getSourceAsObject(StoryFragment.class);
 		mapImagesToFragment(fragment);
 		
 		return fragment;
 	}
 	
 	/* (non-Javadoc)
 	 * @see ca.cmput301f13t03.adventure_datetime.model.IWebStorage#getFragmentsForStory(java.util.UUID, int, int)
 	 */
 	@Override
 	public List<StoryFragment> getFragmentsForStory(UUID storyId, int from, int size) throws Exception {
 		Search search = new Search.Builder(
 				String.format(MATCH, from, size, "", "storyID", storyId))
 			.addIndex(_index)
 			.addType("fragment")
 			.build();
 		
 		JestResult result = execute(search);
 		List<StoryFragment> fragments =  result.getSourceAsObjectList(StoryFragment.class);
 		mapImagesToFragments(fragments);
 		
 		return fragments;
 	}
 	
 	/* (non-Javadoc)
 	 * @see ca.cmput301f13t03.adventure_datetime.model.IWebStorage#getComments(java.util.UUID, int from, int size)
 	 */
 	@Override
 	public List<Comment> getComments(UUID targetId, int from, int size) throws Exception {
 		String sort = String.format(SORT, "timestamp", "desc");
 		String query = String.format(MATCH, from, size, sort, "targetId", targetId);
 		Search search = new Search.Builder(query)
 			.addIndex(_index)
 			.addType("comment")
 			.build();
 		
 		JestResult result = execute(search);
 		List<Comment> comments = result.getSourceAsObjectList(Comment.class);
 		mapImagesToComments(comments);
 		
 		return comments;
 	}
 
 	/* (non-Javadoc)
 	 * @see ca.cmput301f13t03.adventure_datetime.model.IWebStorage#putComment(ca.cmput301f13t03.adventure_datetime.model.Comment)
 	 */
 	@Override
 	public boolean putComment(Comment comment) throws Exception {
 		Index index = new Index.Builder(comment)
 			.index(_index)
 			.type("comment")
 			.id(comment.getId().toString())
 			.build();
 		JestResult result = execute(index);
 		
 		boolean success = result.isSucceeded();
 		if (comment.getImage() != null) {
 			success &= putImage(comment.getImage());
 		}
 		
 		return success;
 	}
 	
 	/* (non-Javadoc)
 	 * @see ca.cmput301f13t03.adventure_datetime.model.IWebStorage#deleteComment(java.util.UUID)
 	 */
 	@Override
 	public boolean deleteComment(UUID commentId) throws Exception {
 		JestResult result = execute(new Delete.Builder(commentId.toString())
 			.index(_index)
 			.type("comment").build());
 		
 		// try to delete Image
 		try {
 			deleteImage(commentId);
 		}
 		catch (Exception e) {
 			// I don't care if this fails
 		}
 		
 		return result.isSucceeded();
 	}
 	
 	/* (non-Javadoc)
 	 * @see ca.cmput301f13t03.adventure_datetime.model.IWebStorage#publishStory(ca.cmput301f13t03.adventure_datetime.model.Story, java.util.List)
 	 */
 	@Override
 	public boolean publishStory(Story story, List<StoryFragment> fragments) throws Exception {
 		boolean succeeded;
 		
 		// I am not cleaning up old fragments because I am assuming we do not support
 		// deleting fragments. If we do support that, then I will have to clean them up.
 		Index index = new Index.Builder(story)
 			.index(_index)
 			.type("story")
 			.id(story.getId().toString())
 			.build();
 		JestResult result = execute(index);
 		succeeded = result.isSucceeded();
 		
 		if (story.getThumbnail() != null) {
 			succeeded &= putImage(story.getThumbnail());
 		}
 		
 		Bulk.Builder bulkBuilder = new Bulk.Builder()
 			.defaultIndex(_index)
 			.defaultType("fragment");
 		
 		if (fragments != null) {
 			for (StoryFragment f : fragments) {
 				f.updateMediaIds();
 				bulkBuilder.addAction(new Index.Builder(f).id(f.getFragmentID().toString()).build());
 			}
 		
 			result = execute(bulkBuilder.build());
 			succeeded &= result.isSucceeded();
 			
 			for (StoryFragment f : fragments) {
 				succeeded &= putImages(f.getStoryMedia());
 			}
 		}
 		
 		return succeeded;
 	}
 	
 	/* (non-Javadoc)
 	 * @see ca.cmput301f13t03.adventure_datetime.model.IWebStorage#getImage(java.util.UUID)
 	 */
 	@Override
 	public Image getImage(UUID imageId) throws Exception {
 		Get get = new Get.Builder(_index, imageId.toString()).type("image").build();
 		JestResult result = execute(get);
 		return result.getSourceAsObject(Image.class);
 	}
 	
 	/**
 	 * Gets images in bulk
 	 * @param imageIds the list of images to retrieve
 	 * @return A list of images
 	 * @throws Exception
 	 */
 	public List<Image> getImages(List<UUID> imageIds) throws Exception {
		if (imageIds == null || imageIds.size() == 0)
			return new ArrayList<Image>();
		
 		StringBuilder ids = new StringBuilder(); 
 		for (UUID id : imageIds) {
 			ids.append(String.format("\"%s\", ", id));
 		}
 		
 		// Trim the last comma and space
 		ids.delete(ids.length() - 2, ids.length());
 		
 		Search search = new Search.Builder(
 				String.format(MULTI_ID, 0, imageIds.size(), "", ids))
 			.addIndex(_index)
 			.addType("image")
 			.build();
 		
 		JestResult result = execute(search);
 		return result.getSourceAsObjectList(Image.class);
 	}
 	
 	/* (non-Javadoc)
 	 * @see ca.cmput301f13t03.adventure_datetime.model.IWebStorage#putImage(ca.cmput301f13t03.adventure_datetime.model.Image)
 	 */
 	@Override
 	public boolean putImage(Image image) throws Exception {
 		Index index = new Index.Builder(image)
 			.index(_index)
 			.type("image")
 			.id(image.getId().toString())
 			.build();
 		JestResult result = execute(index);
 		return result.isSucceeded();
 	}
 	
 	private boolean putImages(List<Image> images) throws Exception {
 		if (images == null)
 			return true;
 			
 		Bulk.Builder bulkBuilder = new Bulk.Builder()
 			.defaultIndex(_index)
 			.defaultType("image");
 		
 		for(Image i : images) {
 			bulkBuilder.addAction(new Index.Builder(i).id(i.getId().toString()).build());
 		}
 		
 		JestResult result = execute(bulkBuilder.build());
 		return result.isSucceeded();
 	}
 	
 	/* (non-Javadoc)
 	 * @see ca.cmput301f13t03.adventure_datetime.model.IWebStorage#deleteImage(java.util.UUID)
 	 */
 	@Override
 	public boolean deleteImage(UUID imageId) throws Exception {
 		Delete delete = new Delete.Builder(imageId.toString())
 			.index(_index)
 			.type("image")
 			.build();
 		JestResult result = execute(delete);
 		return result.isSucceeded();
 	}
 	
 	/* (non-Javadoc)
 	 * @see ca.cmput301f13t03.adventure_datetime.model.IWebStorage#deleteStory(java.util.UUID)
 	 */
 	@Override
 	public boolean deleteStory(UUID storyId) throws Exception {
 		JestResult result = execute(new Delete.Builder(storyId.toString())
 			.index(_index)
 			.type("story").build());
 		
 		// try to delete Image
 		try {
 			deleteImage(storyId);
 		}
 		catch (Exception e) {
 			// I don't care if this fails
 		}
 		
 		return result.isSucceeded();
 	}
 	
 	/* (non-Javadoc)
 	 * @see ca.cmput301f13t03.adventure_datetime.model.IWebStorage#deleteFragment(java.util.UUID)
 	 */
 	@Override
 	public boolean deleteFragment(UUID fragId) throws Exception {
 		JestResult result = execute(new Delete.Builder(fragId.toString())
 			.index(_index)
 			.type("fragment").build());
 		
 		return result.isSucceeded();
 	}
 
 	/**
 	 * The latest ErrorMessage, or null if none exist.
 	 * @return The latest error message, or null if none
 	 */
 	public String getErrorMessage() {
 		return this.errorMessage;
 	}
 	
 	/**
 	 * The latest pure JSON result from the ES server
 	 * @return a JSON string returned from the server
 	 */
 	public String getJsonString() {
 		return jsonString;
 	}
 	
 
 	/**
 	 * Gets the current index being used
 	 * @return The index in ES we are using
 	 */
 	public String getIndex() {
 		return this._index;
 	}
 	
 	/**
 	 * Sets the index to run against
 	 * For production use "setDefaultIndex()"
 	 * For test, put "test" in here.
 	 * @return
 	 */
 	public void setIndex(String index) {
 		this._index = index;
 	}
 	
 	/**
 	 * Sets the index to the default production index
 	 */
 	public void setDefaultIndex() {
 		this._index = defaultIndex;
 	}
 
 	
 	/**
 	 * Execute a client action and set this WebRequester's error message
 	 * @param clientRequest the Action to execute
 	 * @return The JestResult
 	 * @throws Exception, connection errors, etc. See JestClient
 	 */
 	private JestResult execute(Action clientRequest) throws Exception {
 		JestResult result = client.execute(clientRequest);
 		this.errorMessage = result.getErrorMessage();
 		this.jsonString = result.getJsonString();
 		return result;
 	}
 	
 	private void mapImagesToStories(List<Story> stories) throws Exception {
 		if (stories == null || stories.size() == 0)
 			return;
 		
 		// Get the thumbnails
 		List<UUID> ids = new ArrayList<UUID>();
 		for (Story s : stories) {
 			ids.add(s.getId());
 		}
 		
 		List<Image> images = getImages(ids);
 		
 		for (Image i : images) {
 			for (Story s : stories) {
 				if (s.getId().equals(i.getId())) {
 					s.setThumbnail(i.getEncodedBitmap());
 					break;
 				}
 			}
 		}
 	}
 	
 	private void mapImagesToComments(List<Comment> comments) throws Exception {
 		if (comments == null || comments.size() == 0)
 			return;
 		
 		// Get the thumbnails
 		List<UUID> ids = new ArrayList<UUID>();
 		for (Comment c : comments) {
 			ids.add(c.getId());
 		}
 		
 		List<Image> images = getImages(ids);
 		
 		for (Image i : images) {
 			for (Comment c : comments) {
 				if (c.getId().equals(i.getId())) {
 					c.setImage(i.getEncodedBitmap());
 					break;
 				}
 			}
 		}
 	}
 	
 	private void mapImagesToFragments(List<StoryFragment> fragments) throws Exception {
 		for (StoryFragment f : fragments) {
 			mapImagesToFragment(f);
 		}
 	}
 	
 	private void mapImagesToFragment(StoryFragment fragment) throws Exception {
 		if (fragment.getMediaIds() == null)
 			return;
 		
 		List<Image> images = getImages(fragment.getMediaIds());	
 		fragment.setStoryMedia(new ArrayList<Image>(images));
 	}
 }
