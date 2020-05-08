 /*
  *  Copyright (c) 2013 Andrew Fontaine, James Finlay, Jesse Tucker, Jacob Viau, and
  *  Evan DeGraff
  *
  *  Permission is hereby granted, free of charge, to any person obtaining a copy of
  *  this software and associated documentation files (the "Software"), to deal in
  *  the Software without restriction, including without limitation the rights to
  *  use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
  *  the Software, and to permit persons to whom the Software is furnished to do so,
  *  subject to the following conditions:
  *
  *  The above copyright notice and this permission notice shall be included in all
  *  copies or substantial portions of the Software.
  *
  *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
  *  FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
  *  COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
  *  IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
  *  CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
  */
 
 package ca.cmput301f13t03.adventure_datetime.model;
 
 import android.graphics.Bitmap;
 
 import java.util.Calendar;
 import java.util.HashSet;
 import java.util.UUID;
 
 /**
  * A model for an entire Choose-Your-Own-Adventure story
  *
  * @author Andrew Fontaine
  * @version 1.0
  * @since 23/10/13
  */
 public class Story {
 
 	/**
 	 * The GUID of the head fragment of the Story
 	 */
 	private UUID headFragmentId;
 	/**
 	 * The GUID of the story, -1 if there is no _ID
 	 */
 	private UUID id;
 	/**
 	 * The UNIX time of the last time the Story was updated/downloaded
 	 */
 	private long timestamp;
 	/**
 	 * The author of the Story
 	 */
 	private String author;
 	/**
 	 * The title of the Story
 	 */
 	private String title;
 	/**
 	 * The synopsis of the Story
 	 */
 	private String synopsis;
 	
 	/**
 	 * The thumbnail for the story
 	 */
 	private transient Image thumbnail;
 	
 	/**
 	 * A collection of Tags for the Story
 	 */
 	private HashSet<String> tags;
 	/**
 	 * The collection of fragment _GUIDs attached to the story
 	 */
 	private HashSet<UUID> fragmentIDs;
 
     /**
      * Creates a new Story, used by @link{StoryDB}
      *
      * @param headFragmentId UUID of the head StoryFragment of the Story
      * @param id UUID of the Story
      * @param author Name of author of Story
      * @param timestamp Time Story was last modified
      * @param synopsis Synopsis of Story
      * @param thumbnail Thumbnail of Story
      * @param title Title of Story
      */
 	protected Story(UUID headFragmentId, UUID id, String author, long timestamp, String synopsis,
 	             String thumbnail, String title) {
 		this.headFragmentId = headFragmentId;
 		this.id = id;
 		this.author = author;
 		this.timestamp = timestamp;
 		this.synopsis = synopsis;
 		this.thumbnail = new Image(this.id, thumbnail);
 		this.title = title;
 		fragmentIDs = new HashSet<UUID>();
 		fragmentIDs.add(this.headFragmentId);
 	}
 
     /**
      * Creates a new Story, used by @link{StoryDB}
      *
      * @param headFragmentId UUID of the head StoryFragment of the Story
      * @param id UUID of the Story
      * @param author Name of author of Story
      * @param timestamp Time Story was last modified
      * @param synopsis Synopsis of Story
      * @param thumbnail Thumbnail of Story
      * @param title Title of Story
      */
     protected Story(UUID headFragmentId, UUID id, String author, long timestamp, String synopsis,
                     Image thumbnail, String title) {
         this.headFragmentId = headFragmentId;
         this.id = id;
         this.author = author;
         this.timestamp = timestamp;
         this.synopsis = synopsis;
         this.thumbnail = thumbnail;
         this.title = title;
         fragmentIDs = new HashSet<UUID>();
         fragmentIDs.add(this.headFragmentId);
     }
 	
 	/**
      * Creates a new Story, used by @link{StoryDB}
      *
      * @param headFragmentId UUID of the head StoryFragment of the Story
      * @param id UUID of the Story
      * @param author Name of author of Story
      * @param timestamp Time Story was last modified
      * @param synopsis Synopsis of Story
      * @param thumbnail Thumbnail of Story
      * @param title Title of Story
      */
 	protected Story(UUID headFragmentId, UUID id, String author, long timestamp, String synopsis,
 	             Bitmap thumbnail, String title) {
 		this.headFragmentId = headFragmentId;
 		this.id = id;
 		this.author = author;
 		this.timestamp = timestamp;
 		this.synopsis = synopsis;
 		this.thumbnail = new Image(this.id, thumbnail);
 		this.title = title;
 		fragmentIDs = new HashSet<UUID>();
 		fragmentIDs.add(this.headFragmentId);
 	}
 	
     /**
      * Creates a new Story, used by @link{StoryDB}
      *
      * @param headFragmentId UUID of the head StoryFragment of the Story
      * @param id UUID of the Story
      * @param author Name of author of Story
      * @param timestamp Time Story was last modified
      * @param synopsis Synopsis of Story
      * @param thumbnail Thumbnail of Story
      * @param title Title of Story
      */
 	protected Story(String headFragmentId, String id, String author, long timestamp, String synopsis,
 	             String thumbnail, String title) {
 		this(UUID.fromString(headFragmentId), UUID.fromString(id), author, timestamp, synopsis, thumbnail, title);
 	}
 	
 	/**
      * Creates a new Story, used by @link{StoryDB}
      *
      * @param headFragmentId UUID of the head StoryFragment of the Story
      * @param id UUID of the Story
      * @param author Name of author of Story
      * @param timestamp Time Story was last modified
      * @param synopsis Synopsis of Story
      * @param thumbnail Thumbnail of Story
      * @param title Title of Story
      */
 	protected Story(String headFragmentId, String id, String author, long timestamp, String synopsis,
 	             Bitmap thumbnail, String title) {
 		this(UUID.fromString(headFragmentId), UUID.fromString(id), author, timestamp, synopsis, thumbnail, title);
 	}
 
     /**
      * Creates a new Story, used by @link{StoryManager}
      *
      * @param author Name of author of Story
      * @param title Title of Story
      * @param synopsis Synopsis of Story
      */
 	protected Story(String author, String title, String synopsis) {
 		this.author = author;
 		this.title = title;
 		this.synopsis = synopsis;
 		id = UUID.randomUUID();
 		headFragmentId = null;
 		fragmentIDs = new HashSet<UUID>();
 		tags = new HashSet<String>();
 		tags.add("new");
 		timestamp = System.currentTimeMillis() / 1000L;
 	}
 
     /**
      * Constructs a new Story with a random UUID
      */
 	public Story() {
 		id = UUID.randomUUID();
 		title = "Untitled";
 		headFragmentId = null;
 		fragmentIDs = new HashSet<UUID>();
 		tags = new HashSet<String>();
 		tags.add("new");
 		timestamp = System.currentTimeMillis() / 1000L;
 	}
 
     /**
      * Gets the UUID of the Story
      *
      * @return UUID of the Story
      */
 	public UUID getId() {
 		return id;
 	}
 	
 	/**
      * Sets the UUID of the Story
      * @param UUID id
      */
 	public void setId(UUID storyId) {
 		this.id = storyId;
 	}
 
     /**
      * Gets the title of the Story
      *
      * @return Title of the Story
      */
 	public String getTitle() {
 		return title;
 	}
 
     /**
      * Sets the new title of the Story
      *
      * @param title New title of the Story
      */
 	public void setTitle(String title) {
 		this.title = title;
 	}
 
     /**
      * Gets the synopsis of the Story
      *
      * @return Synopsis of the Story
      */
 	public String getSynopsis() {
 		return synopsis;
 	}
 
     /**
      * Sets the new synopsis of the Story
      *
      * @param synopsis The new synopsis
      */
 	public void setSynopsis(String synopsis) {
 		this.synopsis = synopsis;
 	}
 
     /**
      * Gets the UUID of the head StoryFragment
      * @return UUID of the head StoryFragment
      */
 	public UUID getHeadFragmentId() {
 		return headFragmentId;
 	}
 
     /**
      * Sets the UUID of the head StoryFragment
      *
      * @param headFragmentId New UUID of the head StoryFragment
      */
 	public void setHeadFragmentId(UUID headFragmentId) {
 		this.headFragmentId = headFragmentId;
 		addFragment(headFragmentId);
 	}
 
     /**
      * Sets the UUID of the head StoryFragment
      *
      * @param frag New head StoryFragment
      */
 	public void setHeadFragmentId(StoryFragment frag) {
 		setHeadFragmentId(frag.getFragmentID());
 	}
 	
 	/**
 	 * Decodes the thumbnail and returns it as a bitmap
 	 * @return A decoded bitmap of the thumbnail
 	 */
 	public Bitmap decodeThumbnail() {
 		return thumbnail == null ? null : thumbnail.decodeBitmap();
 	}
 	
 	/**
 	 * Set the bitmap to the internal image object
 	 * @param bitmap the bitmap to set
 	 */
 	public void setThumbnail(String bitmap) {
 		if (bitmap == null)
 			this.thumbnail = null;
 		else if (this.thumbnail == null)
 			this.thumbnail = new Image(this.id, bitmap);
 		else
 			this.thumbnail.setBitmap(bitmap);
 	}
 	
 	/**
 	 * Set the bitmap to the internal image object
 	 * @param bitmap the bitmap to set
 	 */
 	public void setThumbnail(Bitmap bitmap) {
 		if (bitmap == null)
 			this.thumbnail = null;
 		else if (this.thumbnail == null)
 			this.thumbnail = new Image(this.id, bitmap);
 		else
 			this.thumbnail.setBitmap(bitmap);
 	}
 	
 	/**
 	 * Gets the thumbnail image object
 	 * @return The Image object of the thumbnail
 	 */
 	public Image getThumbnail() {
 		return this.thumbnail;
 	}
 
     /**
      * Gets the Set of tags of the Story
      * @return The set of tags
      */
 	public HashSet<String> getTags() {
 		return tags;
 	}
 
     /**
      * Adds a tag to the set of Tags
      *
      * @param tag new tag
      */
 	public void addTag(String tag) {
 		this.tags.add(tag);
 	}
 
 	public void removeTag(String tag) {
 		this.tags.remove(tag);
 	}
 
     /**
      * Gets the set of StoryFragmnet UUIDs associated with the Story
      *
      * @return Set of UUIDs
      */
 	public HashSet<UUID> getFragments() {
 		HashSet<UUID> fragmentIds = new HashSet<UUID>();
 		for (UUID uuid : this.fragmentIDs) {
 			fragmentIds.add(uuid);
 		}
 		return fragmentIds;
 	}
 
     /**
      * Adds a UUID to the set of associated StoryFragments
      *
      * @param id UUID of new StoryFragment
      */
 	public void addFragment(UUID id) {
 		if (this.fragmentIDs.isEmpty())
 			this.headFragmentId = id;
 		this.fragmentIDs.add(id);
 	}
 
     /**
      * Adds a UUID to the set of associated StoryFragment
      * @param frag StoryFragment to add to set
      */
 	public void addFragment(StoryFragment frag) {
 		if (this.fragmentIDs.isEmpty())
 			this.headFragmentId = frag.getFragmentID();
 		this.fragmentIDs.add(frag.getFragmentID());
 	}
 
     /**
      * Removes a UUID from the set of StoryFragments
      *
      * @param id UUID of StoryFragment
      *
      * @return Whether the UUID was removed or not
      */
 	public boolean removeFragment(UUID id) {
 		return this.fragmentIDs.remove(id);
 	}
 
     /**
      * Gets the timestamp as UNIX time
      *
      * @return the timestamp
      */
 	public long getTimestamp() {
 		return timestamp;
 	}
 
     /**
      * Gets a formatted timestamp string
      *
      * @return Timestamp in format 'mm/dd/yyyy'
      */
 	public String getFormattedTimestamp() {
 		Calendar cal = Calendar.getInstance();
 		cal.setTimeInMillis(timestamp * 1000);
 		return (cal.get(Calendar.MONTH) + 1) + "/" +
 				cal.get(Calendar.DAY_OF_MONTH) + "/" +
 				cal.get(Calendar.YEAR);
 	}
 
     /**
      * Sets the timestamp to a new time
      *
      * @param timestamp the new timestamp in Unix time
      */
 	public void setTimestamp(long timestamp) {
 		this.timestamp = timestamp;
 	}
 
     /**
      * Updates the timestamp to right now
      */
 	public void updateTimestamp() {
 		this.timestamp = Calendar.getInstance().getTimeInMillis()/1000;
 	}
 
     /**
      * Gets the set of all StoryFragment UUIDs
      * @return the UUIDs
      */
 	public HashSet<UUID> getFragmentIds() {
 		return fragmentIDs;
 	}
 
     /**
      * Gets the author of the Story
      *
      * @return The author
      */
 	public String getAuthor() {
 		return author;
 	}
 
     /**
      * Sets the author of the Story
      *
      * @param author The new Story
      */
 	public void setAuthor(String author) {
 		this.author = author;
 	}
 
     @Override
     public boolean equals(Object o) {
         if (this == o) 
         	return true;
         if (o == null || getClass() != o.getClass()) 
         	return false;
 
         Story s = (Story) o;
 
         return id == null ? s.getId() == null : id.equals(s.getId())
         	&& headFragmentId == null ? s.getHeadFragmentId() == null : headFragmentId.equals(s.getHeadFragmentId())
         	&& author == null ? s.getAuthor() == null : author.equals(s.getAuthor())
         	&& title == null ? s.getTitle() == null : title.equals(s.getTitle())
         	&& synopsis == null ? s.getSynopsis() == null : synopsis.equals(s.getSynopsis())
         	&& tags == null ? s.getTags() == null : tags.equals(s.getTags())
         	&& fragmentIDs == null ? s.getFragmentIds() == null : fragmentIDs.equals(s.getFragmentIds());      
     }
 
     @Override
     public int hashCode() {
         int result = headFragmentId.hashCode();
         result = 31 * result + (id == null ? 0 : id.hashCode());
         result = 31 * result + (int) (timestamp ^ (timestamp >>> 32));
         result = 31 * result + (author == null ? 0 : author.hashCode());
         result = 31 * result + (title == null ? 0 : title.hashCode());
         result = 31 * result + (synopsis == null ? 0 : synopsis.hashCode());
         result = 31 * result + (thumbnail == null ? 0 : thumbnail.hashCode());
         result = 31 * result + (tags == null ? 0 : tags.hashCode());
         result = 31 * result + (fragmentIDs == null ? 0 : fragmentIDs.hashCode());
         return result;
     }
 
 	public Story newId() {
 		this.id = UUID.randomUUID();
		//getThumbnail().setId(UUID.randomUUID());
 		return this;
 	}
 }
