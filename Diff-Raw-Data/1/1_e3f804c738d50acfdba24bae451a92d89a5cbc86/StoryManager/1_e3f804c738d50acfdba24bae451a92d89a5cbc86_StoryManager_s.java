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
 
 import android.content.Context;
 import android.graphics.BitmapFactory;
 import android.util.Log;
 import android.widget.Toast;
 import ca.cmput301f13t03.adventure_datetime.R;
 import ca.cmput301f13t03.adventure_datetime.model.Interfaces.*;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.UUID;
 
 /**
  * Manages all transactions between views, controllers, and models.
  * Creates new Stories, StoryFragments, etc.
  * Fetches and caches Stories and StoryFragments
  */
 public final class StoryManager implements IStoryModelPresenter,
 		IStoryModelDirector {
 	final String DEFAULT_FRAGMENT_TEXT = "<insert content here...>";
 	private static final String TAG = "StoryManager";
 
 	private ILocalStorage m_db = null;
 	private Context m_context = null;
 	private WebStorage m_webStorage = null;
 	private ThreadPool m_threadPool = null;
 
 	// Current focus
 	private Story m_currentStory = null;
 	private StoryFragment m_currentFragment = null;
 	
 	private Map<UUID, Story> m_stories = null;
 	private Map<UUID, Story> m_onlineStories = null;
 	private Map<UUID, Bookmark> m_bookmarkList = null;
 	private Map<UUID, StoryFragment> m_fragmentList = null;
 	private Map<UUID, List<Comment>> m_comments = null;
 	
 	// Listeners
 	private Set<ICurrentFragmentListener> m_fragmentListeners = new HashSet<ICurrentFragmentListener>();
 	private Set<ICurrentStoryListener> m_storyListeners = new HashSet<ICurrentStoryListener>();
 	private Set<ILocalStoriesListener> m_localStoriesListeners = new HashSet<ILocalStoriesListener>();
 	private Set<IOnlineStoriesListener> m_onlineStoriesListeners = new HashSet<IOnlineStoriesListener>();
 	private Set<IBookmarkListListener> m_bookmarkListListeners = new HashSet<IBookmarkListListener>();
 	private Set<IAllFragmentsListener> m_allFragmentListeners = new HashSet<IAllFragmentsListener>();
 	private Map<UUID, ICommentsListener> m_commentsListeners = new HashMap<UUID, ICommentsListener>();
 
 	/**
 	 * Create a new story manager and initializes other components using the provided context.
 	 * The provided context MUST be the application context
 	 */
 	 public StoryManager(Context context) {
 		m_context = context;
 		m_db = new StoryDB(context);
 		m_webStorage = new WebStorage();
 		m_threadPool = new ThreadPool();
 		
 		m_fragmentList = new HashMap<UUID, StoryFragment>();
 		m_comments = new HashMap<UUID, List<Comment>>();
 	}
 
 	// ============================================================
 	//
 	// IStoryModelPresenter
 	//
 	// The design is such that a publish will to the subscriber will
 	// occur immediately if data is available. If not the data will
 	// be supplied later once it is available.
 	//
 	// ============================================================
 
 	/**
 	 * Subscribe for changes to the current fragment
 	 */
 	public void Subscribe(ICurrentFragmentListener fragmentListener) {
 		m_fragmentListeners.add(fragmentListener);
 		if (m_currentFragment != null) {
 			fragmentListener.OnCurrentFragmentChange(m_currentFragment);
 		}
 	}
 
 	/**
 	 * Subscribe for changes to the current story
 	 */
 	public void Subscribe(ICurrentStoryListener storyListener) {
 		m_storyListeners.add(storyListener);
 		if (m_currentStory != null) {
 			storyListener.OnCurrentStoryChange(m_currentStory);
 		}
 	}
 
 	/**
 	 * Subscribe to changes for the current list of stories
 	 */
 	public void Subscribe(ILocalStoriesListener localStoriesListener) {
 		m_localStoriesListeners.add(localStoriesListener);
 		if (m_stories != null) {
 			localStoriesListener.OnLocalStoriesChange(m_stories);
 		} else {
 			LoadStories();
 			PublishStoriesChanged();
 		}
 	}
 	public void Subscribe(IOnlineStoriesListener onlineStoriesListener) {
 		m_onlineStoriesListeners.add(onlineStoriesListener);
 		if (m_onlineStories != null) {
 			onlineStoriesListener.OnOnlineStoriesChange(m_onlineStories);
 		} else {
 			LoadOnlineStories();
 		}
 	}
 
 	public void Subscribe(IBookmarkListListener bookmarkListListener) {
 		m_bookmarkListListeners.add(bookmarkListListener);
 		if (m_bookmarkList != null) {
 			bookmarkListListener.OnBookmarkListChange(m_bookmarkList);
 		} else {
 			LoadBookmarks();
 			PublishBookmarkListChanged();
 		}
 	}
 	
 	public void Subscribe(IAllFragmentsListener allFragmentsListener)
 	{
 		m_allFragmentListeners.add(allFragmentsListener);
 		if(m_fragmentList != null && m_currentStory != null)
 		{
 			Map<UUID, StoryFragment> currentFrags = GetAllCurrentFragments();
 			allFragmentsListener.OnAllFragmentsChange(currentFrags);
 		}
 	}
 	
 	public void Subscribe(ICommentsListener commentsListener, UUID id) {
 		m_commentsListeners.put(id, commentsListener);
 		LoadComments(id);
 	}
 
 	/**
 	 * Unsubscribe from callbacks when the current fragment changes
 	 */
 	public void Unsubscribe(ICurrentFragmentListener fragmentListener) {
 		m_fragmentListeners.remove(fragmentListener);
 	}
 
 	/**
 	 * Unsubscribe from callbacks when the current story changes
 	 */
 	public void Unsubscribe(ICurrentStoryListener storyListener) {
 		m_storyListeners.remove(storyListener);
 	}
 	
 	/**
 	 * Unsubscribe from callbakcs when the current list of stories changes
 	 */
 	public void Unsubscribe(ILocalStoriesListener storyListListener) {
 		m_localStoriesListeners.remove(storyListListener);
 	}
 	public void Unsubscribe(IOnlineStoriesListener storyListListener) {
 		m_onlineStoriesListeners.remove(storyListListener);
 	}
 	public void Unsubscribe(IBookmarkListListener bookmarkListListener) {
 		m_bookmarkListListeners.remove(bookmarkListListener);
 	}
 	
 	public void Unsubscribe(IAllFragmentsListener allFragmentsListener)
 	{
 		m_allFragmentListeners.remove(allFragmentsListener);
 	}
 	
 	public void Unsubscribe(UUID id) {
 		m_commentsListeners.remove(id);
 	}
 
 	// ============================================================
 	//
 	// Publish
 	//
 	// ============================================================
 
 	/**
 	 * Publish a change to the current story to all listeners
 	 */
 	private void PublishCurrentStoryChanged() {
 		for (ICurrentStoryListener storyListener : m_storyListeners) {
 			storyListener.OnCurrentStoryChange(m_currentStory);
 		}
 		
 		// whenever the current story changes so does the list of current fragments
 		PublishAllFragmentsChanged();
 	}
 
 	/**
 	 * Publish a change to the current fragment to all listeners
 	 */
 	private void PublishCurrentFragmentChanged() {
 		for (ICurrentFragmentListener fragmentListener : m_fragmentListeners) {
 			fragmentListener.OnCurrentFragmentChange(m_currentFragment);
 		}
 	}
 
 	/**
 	 * Publish a changed to the current list of stories to all listeners
 	 */
 	private void PublishStoriesChanged() {
 		for (ILocalStoriesListener localStoriesListener : m_localStoriesListeners) {
 			localStoriesListener.OnLocalStoriesChange(m_stories);
 		}
 	}
 	
 	private void PublishOnlineStoriesChanged() {
 		for (IOnlineStoriesListener onlineStoriesListener : m_onlineStoriesListeners) {
 			onlineStoriesListener.OnOnlineStoriesChange(m_onlineStories);
 		}
 	}
 
 	private void PublishBookmarkListChanged() {
 		for (IBookmarkListListener bookmarkListener : m_bookmarkListListeners) {
 			bookmarkListener.OnBookmarkListChange(m_bookmarkList);
 		}
 	}
 	
 	private void PublishCommentsChanged(UUID finalId) {
 		m_commentsListeners.get(finalId).OnCommentsChange(m_comments.get(finalId));
 	}
 	
 	private void PublishAllFragmentsChanged()
 	{
 		if(m_currentStory != null && m_fragmentList != null)
 		{
 			Map<UUID, StoryFragment> currentStoryFragments = GetAllCurrentFragments();
 			
 			for(IAllFragmentsListener allFragListener : m_allFragmentListeners)
 			{
 				allFragListener.OnAllFragmentsChange(currentStoryFragments);
 			}
 		}
 	}
 
 	// ============================================================
 	//
 	// IStoryModelDirector
 	//
 	// ============================================================
 
 	/**
 	 * Select a story
 	 */
 	public void selectStory(UUID storyId) {
 		m_currentStory = getStory(storyId);
 		PublishCurrentStoryChanged();
 	}
 
 	/**
 	 * Select a fragment as the current fragment
 	 */
 	public void selectFragment(UUID fragmentId) {
 		m_currentFragment = getFragment(fragmentId);
 		if(m_currentFragment != null)
 			PublishCurrentFragmentChanged();
 		else {
 			getFragmentOnline(fragmentId, false);
 			getNextFragments(fragmentId);
 		}
 	}
 	
 	/**
 	* Create a new story and head fragment and insert them into the local database
 	*/
 	public Story CreateNewStory()
 	{
 		Story newStory = new Story();
 		StoryFragment headFragment = new StoryFragment(newStory.getId(), DEFAULT_FRAGMENT_TEXT);
 		
 		newStory.setHeadFragmentId(headFragment);
 		
 		m_stories.put(newStory.getId(), newStory);
 		m_fragmentList.put(headFragment.getFragmentID(), headFragment);
 		
 		PublishCurrentStoryChanged();
 		
 		return newStory;
 	}
 
 	public boolean putStory(Story story) {
 		// Set default image if needed
 		if(story == null) 
 			return false;
 		if (story.getThumbnail() == null)
 			story.setThumbnail(BitmapFactory.decodeResource(
 					m_context.getResources(), R.drawable.grumpy_cat));
 		boolean result = m_db.setStory(story);
 		if(result){
 			m_stories.put(story.getId(), story);
 			PublishStoriesChanged();
 		}
 		return result;
 	}
 
 	/**
 	 * Delete a story from the database
 	 */
 	public void deleteStory(UUID storyId) {
 		m_db.deleteStory(storyId);
         m_stories.remove(storyId);
         PublishStoriesChanged();
 	}
 
 	/**
 	 * Get a story from the database or cloud
 	 */
 	public Story getStory(UUID storyId) {
 		if(m_stories == null)
 		{
 			LoadStories();
 		}
 		Story story = m_stories.get(storyId);
 		if(story == null)
 			story = m_onlineStories.get(storyId);
 		return story;
 	}
 
 	/**
 	 * Save a fragment to the database
 	 */
 	public boolean putFragment(StoryFragment fragment) {
 		
 		// this really should be transactional...
 		boolean result = m_db.setStoryFragment(fragment);
 		if(result)
 		{
 			result = m_db.setStory(m_currentStory);
 			
 			PublishAllFragmentsChanged();
 		}
 		
 		return result;
 	}
 
 	/**
 	 * Delete a fragment from the database
 	 */
 	public void deleteFragment(UUID fragmentId) {
 		m_db.deleteStoryFragment(fragmentId);
         m_fragmentList.remove(fragmentId);
         PublishAllFragmentsChanged();
 	}
 
 	/**
 	 * Get a fragment from the database
 	 */
 	public StoryFragment getFragment(UUID fragmentId) {
 		// The fragment should be part of the current story
 		HashSet<UUID> fragmentIds = m_currentStory.getFragments();
 		UUID theId = null;
 		StoryFragment result = null;
 		
 		// verify that the id is indeed part of the current story!
 		for(UUID id : fragmentIds)
 		{
 			if(fragmentId.equals(id))
 				theId = id;
 		}
 		
 		if(theId == null)
 		{
 			// Then you requested an id not attached to the current story!
 			throw new RuntimeException("Requested Fragment Id not attached to current story!");
 		}
 		
 		if(m_fragmentList.containsKey(theId))
 		{
 			// great we have it cached!
 			result = m_fragmentList.get(theId);
 		}
 		else
 		{
 			//Try loading from db
 			result = m_db.getStoryFragment(theId);
 			if(result == null)
 				return result;
 			else
 				m_fragmentList.put(result.getFragmentID(), result);
 		}
 		
 		return result;
 	}
 	
 	private void getFragmentOnline(UUID fragmentId, boolean storeDB) {
 		// Fetch fragment asynchronously
 		final UUID finalId = fragmentId;
 		final boolean finalStoreDB = storeDB;
 		m_threadPool.execute(new Runnable() {
 			public void run() {
 				try {
 					m_currentFragment = m_webStorage.getFragment(finalId);
 					// afterwards place into cache
 					m_fragmentList.put(m_currentFragment.getFragmentID(), m_currentFragment);
 					PublishCurrentFragmentChanged();
 					if(finalStoreDB) {
 						m_db.setStoryFragment(m_currentFragment);
 					}
 				} catch (Exception e) {
 					Log.e(TAG, e.getMessage());
 				}
 			}
 		});
 	}
 	
 	private void getNextFragments(UUID fragmentId){
 	}
 
 	public ArrayList<Story> getStoriesAuthoredBy(String author) {
 		if(m_stories == null)
 		{
 			LoadStories();
 		}
 		
 		ArrayList<Story> results = new ArrayList<Story>();
 		
 		for(Story story : m_stories.values())
 		{
 			if(author.equalsIgnoreCase(story.getAuthor()))
 			{
 				results.add(story);
 			}
 		}
 		
 		return results;
 	}
 
 	/**
 	 * Fetch a bookmark from local database
 	 */
 	public Bookmark getBookmark(UUID id) {
 		if(m_bookmarkList == null)
 		{
 			LoadBookmarks();
 		}
 		
 		return m_bookmarkList.get(id);
 	}
 
 	public void setBookmark() {
 		Bookmark newBookmark = new Bookmark(m_currentStory.getId(), m_currentFragment.getFragmentID());
 		m_db.setBookmark(newBookmark);
 		PublishBookmarkListChanged();
 	}
 	
 	public void deleteBookmark(UUID storyId) {
 		m_db.deleteBookmarkByStory(storyId);
 		PublishBookmarkListChanged();
 	}
 	
 	public void addComment(Comment comment) {
 		final Comment finalComment = comment;
 		m_threadPool.execute(new Runnable() {
 			public void run() {
 				try {
 					m_webStorage.putComment(finalComment);
 					LoadComments(finalComment.getTargetId());
 				} catch (Exception e) {
 					Log.e(TAG, "Error: ", e);
 				}
 			}
 		});
 	}
 	
 	private void LoadStories()
 	{
 		m_stories = new HashMap<UUID, Story>();
 		ArrayList<Story> localStories = m_db.getStories();
 		
 		for(Story story : localStories)
 		{
 			m_stories.put(story.getId(), story);
 		}
 		
 	}
 	
 	private void LoadOnlineStories()
 	{
 		m_onlineStories = new HashMap<UUID, Story>();
 		
 		// Fetch stories from web asynchronously.
 		m_threadPool.execute(new Runnable() {
 			public void run() {
 				try {
 					
 					List<Story> onlineStories;
 					int size = 10;
 					int i = 0;
 					
 					while(size == 10) {
 						onlineStories = m_webStorage.getStories(i, 10);
 						for(Story story : onlineStories)
 						{
 							m_onlineStories.put(story.getId(), story);
 						}
 						size = onlineStories.size();
 						i += 10;
 					}
 					PublishOnlineStoriesChanged();
 				} catch (Exception e) {
 					Log.e(TAG, e.getMessage());
 				}
 			}
 		});		
 	}
 	
 	private void LoadBookmarks()
 	{
 		m_bookmarkList = new HashMap<UUID, Bookmark>();
 		ArrayList<Bookmark> bookmarks = m_db.getAllBookmarks();
 		
 		for(Bookmark bookmark : bookmarks)
 		{
 			m_bookmarkList.put(bookmark.getStoryID(), bookmark);
 		}
 		
 	}
 	
 	private void LoadComments(UUID id)
 	{
 		final UUID finalId = id;
 		m_threadPool.execute(new Runnable() {
 			public void run() {
 				try {
 					if(m_comments.get(finalId) != null)
 						m_comments.remove(finalId);
 					
 					List<Comment> tempComments;
 					List<Comment> onlineComments = new ArrayList<Comment>();
 					int size = 10;
 					int i = 0;
 					
 					while(size == 10) {
 						tempComments = m_webStorage.getComments(finalId, i, 10);
 						for(Comment comment : tempComments)
 						{
 							onlineComments.add(comment);
 						}
 						size = tempComments.size();
 						i += 10;
 					}
 					m_comments.put(finalId, onlineComments);
 					PublishCommentsChanged(finalId);
 				} catch (Exception e) {
 					Log.e(TAG, e.getMessage());
 				}
 			}
 		});
 		
 	}
 	
 	private Map<UUID, StoryFragment> GetAllCurrentFragments()
 	{
 		Map<UUID, StoryFragment> currentFragments = new HashMap<UUID, StoryFragment>();
 		
 		for(UUID fragmentId : m_currentStory.getFragments())
 		{
 			// first try to fetch from local cache
 			StoryFragment frag = this.getFragment(fragmentId);
 			
 			if(frag != null)
 			{
 				currentFragments.put(frag.getFragmentID(), frag);
 			}
 			else
 			{
 				Log.w(TAG, "Attempted to fetch fragments that aren't cached or in local DB!");
 			}
 		}
 		
 		return currentFragments;
 	}
 	public void uploadCurrentStory() {
 		m_threadPool.execute(new Runnable() {
 			public void run() {
 				try {
 					m_webStorage.publishStory(m_currentStory, new ArrayList<StoryFragment>(GetAllCurrentFragments().values()));
 				} catch (Exception e) {
 					Log.e(TAG, e.getMessage());
 				}
 			}
 		});
 	}
 	
 	public void download() {
 		if(m_currentStory != null) {
 			m_stories.put(m_currentStory.getId(), m_currentStory);
 			m_db.setStory(m_currentStory);
 			for(UUID fragmentId : m_currentStory.getFragments()) {
 				getFragmentOnline(fragmentId, true);
 			}
 		}
 	}
 
 }
