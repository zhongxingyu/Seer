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
 import android.graphics.BitmapFactory;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 public class Story {
 
 	private long headFragmentId; /** The _ID of the head fragment of the Story */
 	private long id; /** The _ID of the story, -1 if there is no _ID */
 	private long timestamp; /** The UNIX time of the last time the Story was updated/downloaded */
 	private String author; /** The author of the Story */
 	private String title; /** The title of the Story */
 	private String synopsis; /** The synopsis of the Story */
 	private Bitmap thumbnail; /** The bitmap image of the Story */
 	private Collection<String> tags; /** A collection of Tags for the Story */
 	private Collection<Long> fragmentIDs; /** The collection of fragment _IDs attached to the story */
 
 	public Story(long headFragmentId, long id, String author, long timestamp, String synopsis,
 	             Bitmap thumbnail, String title) {
 		this.headFragmentId = headFragmentId;
 		this.id = id;
 		this.author = author;
 		this.timestamp = timestamp;
 		this.synopsis = synopsis;
 		this.thumbnail = thumbnail;
 		this.title = title;
 		fragmentIDs = new ArrayList<Long>();
 		fragmentIDs.add(headFragmentId);
 	}
 
 	public Story() {
 		id = -1;
 		title = "";
 		headFragmentId = -1;
 		fragmentIDs = new ArrayList<Long>();
 		fragmentIDs.add(headFragmentId);
 		tags = new ArrayList<String>();
 		tags.add("new");
 		timestamp = System.currentTimeMillis() / 1000L;
 	}
 
 	public long getId() {
 		return id;
 	}
 
 	public void setId(long id) {
 		this.id = id;
 	}
 
 	public String getTitle() {
 		return title;
 	}
 
 	public void setTitle(String title) {
 		this.title = title;
 	}
 
 	public String getSynopsis() {
 		return synopsis;
 	}
 
 	public void setSynopsis(String synopsis) {
 		this.synopsis = synopsis;
 	}
 
 	public long getHeadFragmentId() {
 		return headFragmentId;
 	}
 
 	public void setHeadFragmentId(long headFragmentId) {
 		this.headFragmentId = headFragmentId;
 	}
 
 	public Bitmap getThumbnail() {
 		return thumbnail;
 	}
 
 	public void setThumbnail(String bitmap) {
 		this.thumbnail = BitmapFactory.decodeFile(bitmap);
 	}
 
 	public void setThumbnail(Bitmap bitmap) {
 		this.thumbnail = bitmap;
 	}
 
 	public void addTag(String tag) {
 		this.tags.add(tag);
 	}
 
 	public void removeTag(String tag) {
 		this.tags.remove(tag);
 	}
 
 	public void addFragment(Long id) {
 		this.fragmentIDs.add(id);
 	}
 
 	public void removeFragment(Long id) {
 		this.fragmentIDs.remove(id);
 	}
 
 	public long getTimestamp() {
 		return timestamp;
 	}
 
 	public void setTimestamp(long timestamp) {
 		this.timestamp = timestamp;
 	}
 
 	public String getAuthor() {
 		return author;
 	}
 
 	public void setAuthor(String author) {
 		this.author = author;
 	}
 }
