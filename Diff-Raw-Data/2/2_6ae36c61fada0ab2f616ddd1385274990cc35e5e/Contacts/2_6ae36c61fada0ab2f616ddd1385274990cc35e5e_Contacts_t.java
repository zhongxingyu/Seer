 package ch.ffhs.esa.lifeguard.domain;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.ContentValues;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteException;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 import ch.ffhs.esa.lifeguard.persistence.TableGateway;
 
 /**
  * Contacts table gateway implementation
  * 
  * @author Juerg Gutknecht <juerg.gutknecht@students.ffhs.ch>
  * 
  */
 public class Contacts extends TableGateway implements ContactsInterface
 {
 
     /*
      * //////////////////////////////////////////////////////////////////////////
      * CLASS CONSTANTS
      */
 
     private static final String COLUMN_NAME = "name";
     private static final String COLUMN_PHONE = "phone";
     private static final String COLUMN_POSITION = "position";
 
     private static final int COLUMN_INDEX_NAME = 1;
     private static final int COLUMN_INDEX_PHONE = 2;
     private static final int COLUMN_INDEX_POSITION = 3;
 
     /*
      * //////////////////////////////////////////////////////////////////////////
      * INITIALIZATION
      */
 
     public Contacts (SQLiteOpenHelper helper)
     {
         super (helper, "contacts");
     }
 
     /*
      * //////////////////////////////////////////////////////////////////////////
      * TABLE CREATION AND UPDATING
      */
 
     @Override
     public void onCreate (SQLiteDatabase db)
     {
         Log.d (Contacts.class.toString (), "onCreate...");
         try {
             String query = "CREATE TABLE " + this.getTable () + "(" + COLUMN_ID
                     + " INTEGER PRIMARY KEY AUTOINCREMENT" + ", " + COLUMN_NAME
                     + " TEXT NOT NULL" + ", " + COLUMN_PHONE + " TEXT NOT NULL"
                     + ", " + COLUMN_POSITION
                     + " UNSIGNED INTEGER NOT NULL DEFAULT 0" + ");";
             Log.d (Contacts.class.toString (),
                     "Creating table " + this.getTable () + "...");
             Log.d (Contacts.class.toString (), query);
             db.execSQL (query);
             Log.d (Contacts.class.toString (), "Table " + this.getTable ()
                     + " created.");
 
             Log.d (Contacts.class.toString (), "Inserting test data...");
             db.execSQL ("INSERT INTO "
                     + this.getTable ()
                     + " (name, phone, position) VALUES ('Thomas Aregger', '+41794198461', 1);");
             db.execSQL ("INSERT INTO "
                     + this.getTable ()
                     + " (name, phone, position) VALUES ('Jane Doe', '0123456789', 2);");
             db.execSQL ("INSERT INTO "
                     + this.getTable ()
                     + " (name, phone, position) VALUES ('John Doe', '0234567890', 3);");
             Log.d (Contacts.class.toString (), "Test data inserted.");
         }
         catch (SQLiteException e) {
             Log.d (Contacts.class.toString (), e.getMessage ());
         }
     }
 
     @Override
     public void onUpgrade (SQLiteDatabase db, int oldVersion, int newVersion)
     {
         Log.d (Contacts.class.toString (), "onUpgrade...");
         Log.d (Contacts.class.toString (), "Dropping table " + this.getTable ()
                 + " if exists...");
         db.execSQL ("DROP TABLE IF EXISTS " + this.getTable ());
         Log.d (Contacts.class.toString (), "Table " + this.getTable ()
                 + " dropped.");
         this.onCreate (db);
     }
 
     /*
      * //////////////////////////////////////////////////////////////////////////
      * CRUD OPERATIONS
      */
 
     @Override
     public long persist (ContactInterface object)
     {
         Log.d (Contacts.class.getName (), "Persisting " + object);
         ContentValues values = new ContentValues ();
         values.put (COLUMN_NAME, object.getName ());
         values.put (COLUMN_PHONE, object.getPhone ());
 
         long id = -1L;
         
         int targetPos = object.getPosition();
 
         try {
             if (object.getId () > 0) {
                 // Update
                 this.getWritableDatabase ().update (this.getTable (), values,
                         COLUMN_ID + " = ?",
                         new String[] { String.valueOf (object.getId ()) });
                 id = object.getId ();
             }
             else {
                 // Insert
                 int pos = this.getMaxPosition () + 1;
                 values.put (COLUMN_POSITION, pos);
                 id = this.getWritableDatabase ().insert (this.getTable (),
                         null, values);
                 object.setId (id);
                 object.setPosition (pos);
             }
         }
         catch (SQLiteException e) {
             Log.e (Contacts.class.getName (), e.getMessage ());
         }
         finally {
             this.closeDatabase ();
         }
         
         this.moveContact(object, targetPos);
 
         return id;
     }
 
     @Override
     public int delete (ContactInterface object)
     {
         int rows = this.getWritableDatabase ().delete (this.getTable (), "_id = ?",
                 new String[] { String.valueOf (object.getId ()) });
         this.closeDatabase ();
         
         if (rows > 0) {
             this.reorder();
         }
         return rows;
     }
 
     @Override
     public List<ContactInterface> getAll ()
     {
         Cursor cursor = null;
         try {
             cursor = this.getReadableDatabase ().rawQuery (
                     "SELECT * FROM " + this.getTable () + " WHERE ? ORDER BY "
                            + COLUMN_POSITION + " ASC", new String[] { "1" });
         }
         catch (IllegalStateException e) {
             Log.e (Contacts.class.toString (), e.getMessage ());
         }
         catch (SQLiteException e) {
             Log.e (Contacts.class.toString (), e.getMessage ());
         }
 
         List<ContactInterface> contacts = new ArrayList<ContactInterface> ();
 
         if (null != cursor && cursor.moveToFirst ()) {
             while (!cursor.isAfterLast ()) {
                 contacts.add (this.createContact (cursor));
                 cursor.moveToNext ();
             }
         }
 
         return contacts;
     }
 
     @Override
     public ContactInterface findById (long id)
     {
         Cursor cursor = this.getReadableDatabase ().rawQuery (
                 "SELECT * FROM " + this.getTable () + " WHERE " + COLUMN_ID
                         + " = ? LIMIT 0, 1",
                 new String[] { String.valueOf (id) });
 
         ContactInterface contact;
 
         if (null != cursor && cursor.moveToFirst ()) {
             contact = this.createContact (cursor);
         }
         else {
             contact = new Contact ();
         }
         this.closeDatabase ();
         return contact;
     }
 
     /**
      * Returns the number of stored contacts.
      * 
      * @return the number of stored contacts
      * @throws IllegalStateException if the query has failed
      */
     public Long getCount ()
     {
         final String query
             = "SELECT COUNT (u." + COLUMN_ID + ") "
             + "FROM " + getTable () + " u";
 
         Cursor cursor = getReadableDatabase ().rawQuery (query, null);
 
         if (null == cursor || !cursor.moveToFirst ()) {
             throw new IllegalStateException (
                     "Cannot retrieve the number of contacts");
         }
 
         Long count = cursor.getLong (0);
         closeDatabase ();
 
         return count;
     }
 
     /**
      * Returns the contact owning the given position or null if no such contact exists.
      * @param position the position of the desired contact
      * @return the contact on the given position or null
      * @throws IllegalArgumentException if position < 1
      * @throws IllegalStateException if the query has failed
      */
     public ContactInterface findByPosition (int position)
         {
 	    if (position < 1) {
 	        throw new IllegalArgumentException ("There is no position below 1");
 	    }
 
 	    final String query
 	        = "SELECT * FROM " + getTable ()
 	        + " WHERE " + COLUMN_POSITION
 	        + "=" + position
 	        + " LIMIT 1";
 
 	    Cursor cursor = getReadableDatabase ().rawQuery (query, null);
 
 	    if (null == cursor) {
 	        throw new IllegalStateException (
 	                "Cannot query for the contact on the given position");
 	    }
 
             ContactInterface contact = null;
             if (cursor.moveToFirst ()) {
                 contact = createContact (cursor);
             }
             closeDatabase ();
 
 	    return contact;
 	}
 
     /*
      * //////////////////////////////////////////////////////////////////////////
      * PROTECTED OPERATIONS
      */
 
     /**
      * Creates a contact object from a database cursor
      * 
      * @param cursor The cursor to retrieve the data from
      * @return The contact object
      */
     protected ContactInterface createContact (Cursor cursor)
     {
         ContactInterface contact = new Contact ();
 
         contact.setId (cursor.getLong (COLUMN_INDEX_ID));
         contact.setName (cursor.getString (COLUMN_INDEX_NAME))
                 .setPhone (cursor.getString (COLUMN_INDEX_PHONE))
                 .setPosition (cursor.getInt (COLUMN_INDEX_POSITION));
 
         return contact;
     }
 
     /**
      * Retrieves the highest currently store position
      * 
      * @return The max position
      */
     protected int getMaxPosition ()
     {
         String query = "SELECT MAX(" + COLUMN_POSITION + ") FROM "
                 + this.getTable ();
         Cursor c = this.getReadableDatabase ().rawQuery (query, null);
 
         int pos = -1;
         if (null != c && c.moveToFirst ()) {
             pos = c.getInt (0);
         }
 
         return pos;
     }
     
     /**
      * Moves a contact up or down to targetPos.
      * 
      * @param contact The contact to move
      * @param targetPos The position to move to
      */
     protected void moveContact(ContactInterface contact, int targetPos) {
         ContactInterface storedContact = this.findById(contact.getId());
         int storedPos = storedContact.getPosition();
         
         if (storedPos == targetPos) {
             return;
         }
         
         /*
          * Why are we iterating over all contacts? Wouldn't it be better to
          * select only the relevant rows from the database?
          * 
          * Yes, it would. But we only have <= 5 rows (contacts), so why bother?
          */
         
         if (storedPos > targetPos) {
             // Moving up
             for (ContactInterface c : this.getAll()) {
                 if (c.getPosition() >= targetPos) {
                     this.updatePosition(c, c.getPosition()+1);
                 }
             }
         } else {
             // Moving down
             for (ContactInterface c : this.getAll()) {
                 if (c.getPosition() >= storedPos
                     && c.getPosition() <= targetPos) {
                     this.updatePosition(c, c.getPosition()-1);
                 }
             }
         }
         
         this.updatePosition(contact, targetPos);
         this.reorder();
     }
     
     /**
      * Re-orders the contacts' positions to eliminate gaps in positions.
      * 
      * Example: (1,2,4) -> (1,2,3)
      */
     protected void reorder() {
         int pos = 0;
         for (ContactInterface c : this.getAll()) {
             this.updatePosition(c, ++pos);
         }
     }
     
     /**
      * Low-cost convenience operation to update a contact's position.
      * 
      * @param contact The contact to move
      * @param pos The position to move to
      */
     protected void updatePosition(ContactInterface contact, int pos) {
         ContentValues values = new ContentValues ();
         values.put (COLUMN_POSITION, pos);
         this.getWritableDatabase ().update (this.getTable (), values,
                 COLUMN_ID + " = ?",
                 new String[] { String.valueOf (contact.getId ()) });
         this.closeDatabase();
     }
 }
