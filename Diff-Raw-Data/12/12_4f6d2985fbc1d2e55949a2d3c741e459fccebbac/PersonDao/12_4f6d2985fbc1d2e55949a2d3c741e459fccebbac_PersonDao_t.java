 package com.cysnake.syncapp.dao;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.graphics.Bitmap.CompressFormat;
 
 import com.cysnake.syncapp.po.PersonPO;
 import com.cysnake.syncapp.tools.CommonUtils;
 
 public class PersonDao extends CommonDao {
 
 	private static final String TABLE_NAME = "PERSON";
 	private static final String KEY_ID = "ID";
 	private static final String KEY_CONTACT_ID = "CONTACT_ID";
 	private static final String KEY_NAME = "NAME";
 	private static final String KEY_L_PHOTO = "L_PHOTO";
 	private static final String KEY_H_PHOTO = "H_PHOTO";
 
 	public PersonDao(Context ctx) {
 		super(ctx);
 		// TODO Auto-generated constructor stub
 	}
 
 	public long insert(PersonPO person) {
 		ContentValues cv = new ContentValues();
 		cv.put(KEY_CONTACT_ID, person.getContactId());
 		cv.put(KEY_NAME, person.getName());
 		cv.put(KEY_L_PHOTO, CommonUtils.getBitePhoto(person.getlPhoto(),
 				CompressFormat.JPEG, 100));
 		return mDb.insert(TABLE_NAME, KEY_L_PHOTO, cv);
 	}
 
 	public boolean isExist(PersonPO person) {
 		Cursor mCursor;
 		mCursor = mDb.query(true, TABLE_NAME, new String[] { KEY_ID },
 				KEY_CONTACT_ID + " = " + person.getContactId() + "", null,
 				null, null, null, null);
 		if (mCursor.getCount() > 0) {
			mCursor.moveToNext();
 			int id = mCursor.getInt(mCursor.getColumnIndex(KEY_ID));
 			person.setId(id);
 			mCursor.close();
 			return true;
 		} else {
 			mCursor.close();
 			return false;
 		}
 	}
 
 	public void update(PersonPO person) {
 		ContentValues cv = new ContentValues();
 		cv.put(KEY_NAME, person.getName());
 		cv.put(KEY_L_PHOTO, CommonUtils.getBitePhoto(person.getlPhoto(),
 				CompressFormat.JPEG, 100));
 		mDb.update(TABLE_NAME, cv, KEY_ID + "=?",
 				new String[] { String.valueOf(person.getId()) });
 	}
 }
