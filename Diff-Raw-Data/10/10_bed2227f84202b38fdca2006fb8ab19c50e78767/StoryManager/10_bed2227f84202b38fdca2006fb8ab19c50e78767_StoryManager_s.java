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
 import ca.cmput301f13t03.adventure_datetime.model.Interfaces.*;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Set;
 
 public final class StoryManager implements IStoryModelPresenter, IStoryModelDirector {
	private StoryDB m_db = null;
 
 	// Current focus
 	private Story m_currentStory = null;
 	private StoryFragment m_currentFragment = null;
 	private Collection<Story> m_storyList = null;
 
 	// Listeners
 	private Set<ICurrentFragmentListener> m_fragmentListeners = new HashSet<ICurrentFragmentListener>();
 	private Set<ICurrentStoryListener> m_storyListeners = new HashSet<ICurrentStoryListener>();
 	private Set<IStoryListListener> m_storyListListeners = new HashSet<IStoryListListener>();
 
 	public StoryManager(Context context) {
 		m_db = new StoryDB(context);
 	}
 
 	//============================================================
 	//
 	//	IStoryModelPresenter
 	//
 	//	The design is such that a publish will to the subscriber will
 	//	occur immediately if data is available. If not the data will
 	//	be supplied later once it is available.
 	//
 	//============================================================
 
 	public void Subscribe(ICurrentFragmentListener fragmentListener) {
 		m_fragmentListeners.add(fragmentListener);
 		if (m_currentFragment != null) {
 			fragmentListener.OnCurrentFragmentChange(m_currentFragment);
 		}
 	}
 
 	public void Subscribe(ICurrentStoryListener storyListener) {
 		m_storyListeners.add(storyListener);
 		if (m_currentStory != null) {
 			storyListener.OnCurrentStoryChange(m_currentStory);
 		}
 	}
 
 	public void Subscribe(IStoryListListener storyListListener) {
 		m_storyListListeners.add(storyListListener);
 		if (m_storyList != null) {
 			storyListListener.OnCurrentStoryListChange(m_storyList);
 		}
 		/* This may be a good opportunity to async fetch the data from
 		 * either local storage or server*/
 	}
 
 	public void Unsubscribe(ICurrentFragmentListener fragmentListener) {
 		m_fragmentListeners.remove(fragmentListener);
 	}
 
 	public void Unsubscribe(ICurrentStoryListener storyListener) {
 		m_storyListeners.remove(storyListener);
 	}
 
 	public void Unsubscribe(IStoryListListener storyListListener) {
 		m_storyListListeners.remove(storyListListener);
 	}
 
 	//============================================================
 	//
 	//	Publish
 	//
 	//============================================================
 
 	private void PublishCurrentStoryChange() {
 		for (ICurrentStoryListener storyListener : m_storyListeners) {
 			storyListener.OnCurrentStoryChange(m_currentStory);
 		}
 	}
 
 	private void PublishCurrentFragmentChange() {
 		for (ICurrentFragmentListener fragmentListener : m_fragmentListeners) {
 			fragmentListener.OnCurrentFragmentChange(m_currentFragment);
 		}
 	}
 
 	private void PublishStoryListChange() {
 		for (IStoryListListener listListener : m_storyListListeners) {
 			listListener.OnCurrentStoryListChange(m_storyList);
 		}
 	}
 
 	//============================================================
 	//
 	//	IStoryModelDirector
 	//
 	//============================================================
 
 
 	public void selectStory(String storyId) {
 		m_currentStory = getStory(storyId);
 		PublishCurrentStoryChange();
 	}
 
 	public void selectFragment(String fragmentId) {
 		m_currentFragment = getFragment(fragmentId);
 		PublishCurrentFragmentChange();
 
 	}
 
 	public boolean putStory(Story story) {
 		return m_db.setStory(story);
 	}
 
 	public void deleteStory(String storyId) {
 		// TODO Needs to be implemented in database.
 
 	}
 
 	public Story getStory(String storyId) {
 		return m_db.getStory(storyId);
 	}
 
 	public boolean putFragment(StoryFragment fragment) {
 		return m_db.setStoryFragment(fragment);
 	}
 
 	public void deleteFragment(String fragmentId) {
 		// TODO Needs to be implemented in database.
 
 	}
 
 	public StoryFragment getFragment(String fragmentId) {
 		return m_db.getStoryFragment(fragmentId);
 	}
 
 	public ArrayList<Story> getStoriesAuthoredBy(String author) {
 		return m_db.getStoriesAuthoredBy(author);
 	}
 
 
 	@Override
 	public void Subscribe(IBookmarkListListener bookmarkListListener) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void Unsubscribe(IBookmarkListListener bookmarkListListener) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public Bookmark getBookmark(String id) {
 		return m_db.getBookmark(id);
 	}
 
 	public void setBookmark(Bookmark bookmark) {
 		m_db.setBookmark(bookmark);
 	}
 }
