 package com.cs301w01.meatload.model.querygenerators;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 import com.cs301w01.meatload.model.Tag;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.util.Log;
 
 public class TagQueryGenerator extends QueryGenerator {
 	
 	 //vars for tags table
     public static final String TABLE_NAME = "tags";
 
     public static final String CREATE_TABLE_QUERY =
         "CREATE TABLE " + TABLE_NAME + " (" +
             COL_ID + " INTEGER PRIMARY KEY, " +
             COL_PICTUREID + " INTEGER, " +
             COL_NAME + " TEXT, " +
             "FOREIGN KEY(" + COL_PICTUREID + ") REFERENCES " +
             	PictureQueryGenerator.TABLE_NAME + "( " + COL_ID + "));";
     
     /**
      * Constructor, calls the super constructor and passes a Context for DB use.
      * @param context To be used for DB operations.
      */
     public TagQueryGenerator(Context context) {
 		super(context);
 	}
     
     /**
      * Return all tags associated with a picture.
      * @param pictureID ID of the picture to select the tags of.
      * @return ArrayList<Tag>
      */
     public ArrayList<Tag> selectPictureTags(int pictureID) {  
         String getTags = "SELECT " + COL_NAME +
         					" FROM " + TagQueryGenerator.TABLE_NAME +
         					" WHERE " + COL_PICTUREID + " = '" + pictureID + "'";
         
         return selectTagsByQuery(getTags);
         
     }
     
     /**
      * Returns a collection of all unique tags currently in the database.
      * @return ArrayList<Tag>
      */
     public ArrayList<Tag> selectAllTags() {
     	
     	String tagQuery = "SELECT t." + COL_NAME + " AS " + COL_NAME + ", COUNT(*) AS numPictures" 
     						+ 
     						" FROM " + TABLE_NAME + 
     						" t LEFT JOIN " + PictureQueryGenerator.TABLE_NAME +
     						" p ON (t." + COL_PICTUREID + " = p." + COL_ID + ")" + 
     						" GROUP BY t." + COL_NAME;
     	
     	return selectTagsByQuery(tagQuery);
     }
     
     /**
      * Returns the number of times this tag occurs in the database, ie the number
      * of pictures that have this tag.
      * @param tagName
      * @return int
      */
     public int getTagPictureCount(String tagName) {
     	String countQuery = "SELECT COUNT(*) AS numPictures" + 
     						" FROM " + TABLE_NAME +
     						" t LEFT JOIN " + PictureQueryGenerator.TABLE_NAME +
     						" P ON (t." + COL_PICTUREID + " = p." + COL_ID + ")" +
     						" WHERE t." + COL_NAME + " = '" + tagName + "'";
     	
     	Cursor c = db.performRawQuery(countQuery);
     	
     	if (c == null) {
     		return 0;
     	}
     	
     	int picCount = Integer.parseInt(c.getString(c.getColumnIndex("numPictures")));
     	
     	c.close();
     	
     	return picCount;
     }
     
     /**
      * Associates a collection of tags to a specific Picture in the Database.
      * @param pictureID The ID of the Picture to update.
      * @param tagNames A collection of tags to add.
      */
     public void addTagsToPicture(int pictureID, ArrayList<String> tagNames) {
     	for (String tagName : tagNames) {
     		if(!tagExists(pictureID, tagName)) {
     			addTag(pictureID, new Tag(tagName, 0));
     		}
     	}
     }
     
     /**
      * Deletes the specified tags from a Picture in the database.
      * @param pictureID The ID of the Picture to update.
      * @param tagNames A collection of tags to delete.
      */
     public void deleteTagsFromPicture(int pictureID, ArrayList<String> tagNames) {
     	for (String tagName : tagNames) {
     		String dQuery = "DELETE FROM " + TABLE_NAME + 
     						" WHERE " + COL_NAME  + " = '" + tagName + "'" + 
     						" AND " + COL_PICTUREID + " = '" + pictureID + "'";
 
             Log.d(TABLE_NAME, "Performing delete: " + dQuery);
             
            db.performRawQuery(dQuery).close();
     	}
     }
     
     /**
      * Deletes the specified tags from a Picture in the database.
      * @param pictureID The ID of the Picture to update.
      * @param tagNames A collection of tags to delete.
      */
     public void deleteAllTagsFromPicture(int pictureID) {
     		String dQuery = "DELETE FROM " + TABLE_NAME + 
     						" WHERE " + COL_PICTUREID + " = '" + pictureID + "'";
 
             Log.d(TABLE_NAME, "Performing delete: " + dQuery);
             
            db.performRawQuery(dQuery).close();
     }
     
     /**
      * Adds a tag to specific Picture in the Database.
      * @param pictureID The ID of Picture to add the tag too.
      * @param tag The tag to add.
      * @return long
      */
     public long addTag(int pictureID, Tag tag) {
     	ContentValues cv = new ContentValues();
         
         //add tag info to cv
         cv.put(COL_NAME, tag.getName());
         cv.put(COL_PICTUREID, pictureID);
 
         //insert picture into picture tables
         return db.insert(TABLE_NAME, COL_ID, cv);
     }
     
     /**
     * Performs a join on a collection of Strings with a delimiter between each String.
     * @param strings Collection of Strings to be joined
     * @param delimiter String to be placed between each String
     * @return String representation of joined Strings
     */
     public String stringJoin(Collection<String> strings, String delimiter) {
 		 String newString = "";
 		 boolean isFirst = true;
 		 
 		 for (String curr : strings) {
 			 newString += curr;
 			 
 			 if (isFirst) {
 				 isFirst = false;
 				 newString += delimiter;
 			 }
 		 }	 
 		 return newString;
     }
     
 
 	private ArrayList<Tag> selectTagsByQuery(String tagQuery) {
     	
 		Cursor c = db.performRawQuery(tagQuery);
     	
     	ArrayList<Tag> tags = new ArrayList<Tag>();
     	
     	if (c == null) {
     		return tags;
     	}
 
         while (!c.isAfterLast()) {
             
             String tagName = c.getString(c.getColumnIndex(COL_NAME));
             int numPictures = getTagPictureCount(tagName);
             
             tags.add(new Tag(tagName, numPictures));
 
             c.moveToNext();
         }
 
         c.close();
         
         return tags;
     }
 	
     private boolean tagExists(int pictureID, String tagName) {
     	String query = "SELECT COUNT(*) AS numTag" + 
     					" FROM " + TABLE_NAME + 
     					" WHERE " + COL_NAME + " = '" + tagName + "'" + 
     					" AND " + COL_PICTUREID + " = '" + pictureID + "'";
     	Cursor c = db.performRawQuery(query);
     	
     	if (c == null) {
     		return false;
     	}
     	
     	Integer i = new Integer(c.getString(c.getColumnIndex("numTag")));
     	boolean exists = false;
     	
     	if(i > 0)
     		exists = true;
     	
     	c.close();
     	
     	return exists;
     }
 }
