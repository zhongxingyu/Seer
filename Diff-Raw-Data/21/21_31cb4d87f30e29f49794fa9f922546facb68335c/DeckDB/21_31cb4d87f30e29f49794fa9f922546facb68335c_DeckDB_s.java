 /* memory
  * de.thm.ateam.memory.engine.type
  * DeckDB.java
  * 05.06.2012
  *
  * by Frank Kevin Zey
  */
 package de.thm.ateam.memory.engine.type;
 
 import java.io.ByteArrayOutputStream;
 
 import android.content.ContentValues;
 import android.graphics.Bitmap;
 
 /**
  * @author Frank Kevin Zey
  *
  */
 public class DeckDB {
 	
 	public static final String TABLE_NAME 		= "theme";
 	public static final String ID 				= "_id";
	public static final String NAME 			= "_name";
	public static final String BACK_CARD		= "_backSide";
 	
 	public static final String CARD_TABLE_NAME	= "card";
 	public static final String CARD_ID			= "_id";
	public static final String CARD_DECK_ID		= "_deck_id";
 	public static final String CARD_BLOB		= "card_image";
 
 	protected ContentValues createContentValues(Deck d) {
 		ContentValues cv = new ContentValues();
 		ByteArrayOutputStream out = new ByteArrayOutputStream();
         
 		cv.put(NAME, d.getName());
 		d.getBackSide().compress(Bitmap.CompressFormat.PNG, 100, out);
 		cv.put(CARD_BLOB, out.toByteArray());
 		for (Bitmap b : d.getFrontSide()) {
 			out = new ByteArrayOutputStream();
 	        b.compress(Bitmap.CompressFormat.PNG, 100, out);
 	        cv.put(CARD_BLOB, out.toByteArray());
 		}
 		return cv;
 	}
 	
 }
