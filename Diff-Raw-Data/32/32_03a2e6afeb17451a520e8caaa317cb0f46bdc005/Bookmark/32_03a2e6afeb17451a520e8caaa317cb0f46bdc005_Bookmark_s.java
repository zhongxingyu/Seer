 /*
  * Copyright (c) 2013 Andrew Fontaine, James Finlay, Jesse Tucker, Jacob Viau, and
  * Evan DeGraff
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy of
  * this software and associated documentation files (the "Software"), to deal in
  * the Software without restriction, including without limitation the rights to
  * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
  * the Software, and to permit persons to whom the Software is furnished to do so,
  * subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in all
  * copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
  * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
  * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
  * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
  * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
  */
 
 package ca.cmput301f13t03.adventure_datetime.model;
 
 import android.database.Cursor;
 import java.util.Calendar;
 import java.util.Date;
 
 /**
  * @author Andrew Fontaine
  * @version 1.0
  * @since 23/10/13
  */
 public class Bookmark {
 
 	private String fragmentID;
 	private String storyID;
 	private Date date;
 
	public Bookmark(String fragmentID, String storyID) {
 		this.fragmentID = fragmentID;
 		this.storyID = storyID;
 		this.date = Calendar.getInstance().getTime();
 	}
 
 	public Bookmark(String fragmentID, String storyID, Date date) {
 		this.fragmentID = fragmentID;
 		this.storyID = storyID;
 		this.date = date;
 	}
 
 	public String getFragmentID() {
 		return fragmentID;
 	}
 
 	public void setFragmentID(String fragmentID) {
 		this.fragmentID = fragmentID;
 	}
 
 	public Date getDate() {
 		return date;
 	}
 
 	public void setDate(Date date) {
 		this.date = date;
 	}
 
 	public String getStoryID() {
 
 		return storyID;
 	}
 
 	public void setStoryID(String storyID) {
 		this.storyID = storyID;
 	}
 }
