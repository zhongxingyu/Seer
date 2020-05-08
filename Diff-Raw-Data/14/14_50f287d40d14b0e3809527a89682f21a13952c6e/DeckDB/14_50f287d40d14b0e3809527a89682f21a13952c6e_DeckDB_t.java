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
 	public static final String NAME 			= "name";
 	public static final String BACK_CARD		= "backSide";
 	
 	public static final String CARD_TABLE_NAME	= "card";
 	public static final String CARD_ID			= "_id";
 	public static final String CARD_DECK_ID		= "deck_id";
 	public static final String CARD_BLOB		= "card_image";
 
	protected ContentValues createDeckContentValues(Deck d) {
 		ContentValues cv = new ContentValues();
 		ByteArrayOutputStream out = new ByteArrayOutputStream();
         
 		cv.put(NAME, d.getName());
 		d.getBackSide().compress(Bitmap.CompressFormat.JPEG, 100, out);
 		cv.put(BACK_CARD, out.toByteArray());
 		
 		return cv;
 	}
 	
 	protected ContentValues createCardContentValues(Bitmap b, long DeckID) {
 		ContentValues cv = new ContentValues();
 		ByteArrayOutputStream out = new ByteArrayOutputStream();
 		
 		cv.put(CARD_DECK_ID, DeckID);
         
 		b.compress(Bitmap.CompressFormat.JPEG, 100, out);
 	    cv.put(CARD_BLOB, out.toByteArray());
 		
 		return cv;
 	}
 	
 }
