 package com.nbos.phonebook;
 
 import android.content.ContentResolver;
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.net.Uri;
 import android.provider.ContactsContract;
 import android.util.Log;
 
 import com.nbos.phonebook.database.tables.BookTable;
 import com.nbos.phonebook.sync.Constants;
 
 public class Test {
 	static String tag = "Test";
     public static void resetBookTableDirty(Context ctx) {
 		ContentResolver cr = ctx.getContentResolver();
 	    ContentValues values = new ContentValues();
 	    values = new ContentValues();
 	    values.put(BookTable.DIRTY, (Integer) null);
 	    int num = cr.update(
 	    		Uri.parse(Constants.SHARE_BOOK_PROVIDER), values,
 	    		null, null);
 	    Log.i(tag, "Updated "+num+" sharebooks to dirty = 0");
 	}
 
     public static void getContacts(Context ctx) {
     	DatabaseHelper.getContacts(false, ctx);
 	}
 
     public static void getRawContacts(Context ctx) {
     	Cursor c = DatabaseHelper.getRawContactsCursor(ctx.getContentResolver(), false);
     	Log.i(tag, "There are "+c.getCount()+" raw contacts");
     	c.moveToFirst();
     	do {
 			String cId = c.getString(c.getColumnIndex(ContactsContract.RawContacts.CONTACT_ID));
 			String serverId = c.getString(c.getColumnIndex(Constants.CONTACT_SERVER_ID));
 			Log.i(tag, "contactId: "+cId+", serverId: "+serverId);
     	} while(c.moveToNext());
     	DatabaseHelper.getSourceIdFromContactId(c, "793");
 	}
 
 }
