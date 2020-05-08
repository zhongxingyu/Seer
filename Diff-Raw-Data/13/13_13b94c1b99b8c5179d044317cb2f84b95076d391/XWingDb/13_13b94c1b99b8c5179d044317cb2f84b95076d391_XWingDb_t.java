 package com.itkweb.xwinga.db;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 
 import com.itkweb.xwinga.model.Box;
 
 public class XWingDb {
 
 	private XWingDbTables xwingDb;
 	private SQLiteDatabase readableDb;
 
 	public XWingDb(Context context) {
 		// On crer la BDD et sa table
 		xwingDb = new XWingDbTables(context);
 	}
 
 	public void open() {
 		// on ouvre la BDD en criture
 		readableDb = xwingDb.getReadableDatabase();
 	}
 
 	public void close() {
 		// on ferme l'accs  la BDD
 		readableDb.close();
 	}
 
 	public SQLiteDatabase getBDD() {
 		return readableDb;
 	}
 
 	public Set<Box> getAllBoxes() {
 
 		Set<Box> resultBoxList = new HashSet<Box>();
		
		open();
		
 		Cursor c = readableDb.query(XWingDbTables.TABLE_BOX, new String[] {XWingDbTables.COL_ID, XWingDbTables.COL_NAME, XWingDbTables.COL_PICTURE}, "", null, null, null, null);
 
 		//si aucun lment n'a t retourn dans la requte, on renvoie null
 		if (c.getCount() == 0)
 			return null;
  
 		//Sinon on se place sur le premier lment
 		if(c.moveToFirst()) {
 			do {
 				Box box = new Box();
 				box.setId(c.getInt(0));
 				box.setName(c.getString(1));
 				box.setPicture(c.getString(2));
 				resultBoxList.add(box);
 			} while (c.moveToNext());
 			
 		}
 		
 		//On ferme le cursor
 		c.close();
  
		close();
		
 		//On retourne le livre
 		return resultBoxList;
 	}
 }
