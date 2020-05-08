 package cs2114.group.friendtracker.data;
 
 import android.util.Log;
 
 import cs2114.group.friendtracker.Person;
 
 import cs2114.group.friendtracker.Event;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 
 /**
  * The glue class between the model and database. Any actions that need to be
  * stored on the database should be carried out by calling methods in this
  * class.
  *
  * For example, when creating a new event, we should do the following:
  *
  * DataSource src = new DataSource(getContext());
  *
  * Event newEvent = src.createEvent(eventName, ownerId, startTime, endTime,
  * startDate, endDate, days);
  *
  * @author Tianyu Geng (tony1)
  * @author Chris Schweinhart (schwein)
  * @author Elena Nadolinski (elena)
  * @version Apr 20, 2012
  */
 public class DataSource {
     /**
      * The tag for debugging.
      */
     public final String TAG = "Tracker-Test";
     // Database fields
     private SQLiteDatabase database;
     private H dbHelper;
     private String[] allEventColumns = { H.E_ID, H.E_NAME, H.E_OWNER,
             H.E_STIME, H.E_ETIME, H.E_SDATE, H.E_EDATE, H.E_DAYS[1],
             H.E_DAYS[2], H.E_DAYS[3], H.E_DAYS[4], H.E_DAYS[5],
             H.E_DAYS[6], H.E_DAYS[7] };
     private String[] allPersonColumns = { H.P_ID, H.P_NAME, H.P_PHONE };
 
     /**
      * The constructor.
      *
      * @param context
      *            the context
      */
     public DataSource(Context context) {
         dbHelper = new H(context);
     }
 
     /**
      * Clear all events and persons stored in the database.
      */
     public void clearAll() {
         database.delete(H.TABLE_EVENTS, null, null);
         database.delete(H.TABLE_PERSONS, null, null);
     }
 
     /**
      * Close the database when it is no longer needed.
      */
     public void close() {
         dbHelper.close();
     }
 
     /**
      * Create a new Event and store it into the database.
      *
      * @param name
      *            name of the event.
      * @param owner
      *            the owner Id of this event
      * @param startTime
      *            format: hhmm
      * @param endTime
      *            format: hhmm
      * @param startDate
      *            format: YYYYMMDD
      * @param endDate
      *            format: YYYYMMDD
      * @param days
      *            : format "**111**" means Tue, Wed, Thur have the event. *
      *            means no event, any other char means event happens.
      * @return the new Event that is created.
      */
     public Event
             createEvent(String name, long owner, String startTime,
                     String endTime, String startDate, String endDate,
                     char[] days) {
 
         long eventId =
                 database.insert(H.TABLE_EVENTS, null,
                         eventToValues(new Event(0, name, owner,
                                 startTime, endTime, startDate, endDate,
                                 days)));
 
         return new Event(eventId, name, owner, startTime, endTime,
                 startDate, endDate, days);
 
     }
 
     /**
      * Create a new Event and store it into the database.
      *
      * @param name
      *            name of the event.
      * @param owner
      *            the owner Id of this event
      * @param startTime
      *            format: hhmm
      * @param endTime
      *            format: hhmm
      * @param startDate
      *            format: YYYYMMDD
      * @param endDate
      *            format: YYYYMMDD
      * @param days
      *            : format "**111**" means Tue, Wed, Thur have the event. *
      *            means no event, any other char means event happens.
      * @return the new Event that is created.
      */
     public Event
             createEvent(String name, long owner, String startTime,
                     String endTime, String startDate, String endDate,
                     String days) {
         return createEvent(name, owner, startTime, endTime, startDate,
                 endDate, days.toCharArray());
 
     }
 
     /**
      * Create a new person and store it to the database.
      *
      * @param name
      *            name of the person
      * @param phoneNumber
      *            the phone number
      * @return the person created
      */
     public Person createPerson(String name, int phoneNumber) {
         ContentValues values = new ContentValues();
         values.put(H.P_NAME, name);
         values.put(H.P_PHONE, phoneNumber);
         long personId = database.insert(H.TABLE_PERSONS, null, values);
         return new Person(personId, name, phoneNumber);
 
     }
 
     /**
      * Convert the row the cursor currently points to to an Event.
      *
      * @param cursor
      *            the cursor
      * @return the Event created according to the cursor
      */
     private Event cursorToEvent(Cursor cursor) {
         char[] days = new char[7];
         for (int i = 0; i < 7; i++) {
             days[i] = cursor.getLong(H.NO_DAYS[i + 1]) == 0 ? '*' : '1';
         }
         Event e =
                 new Event(cursor.getLong(H.NO_ID),
                         cursor.getString(H.NO_NAME),
                         cursor.getLong(H.NO_OWNER_PHONE),
                         cursor.getString(H.NO_STIME),
                         cursor.getString(H.NO_ETIME),
                         cursor.getString(H.NO_SDATE),
                         cursor.getString(H.NO_EDATE), days);
 
         return e;
     }
 
     /**
      * Convert the row the cursor currently points to to a Person.
      *
      * @param cursor
      *            the cursor
      * @return the Personn created according to the cursor
      */
     private Person cursorToPerson(Cursor cursor) {
         if (cursor.getCount() < 1)
             return null;
         Person p =
                 new Person(cursor.getLong(H.NO_ID),
                         cursor.getString(H.NO_NAME),
                         cursor.getLong(H.NO_OWNER_PHONE));
         return p;
     }
 
     /**
      * Delete an Event in the database.
      *
      * @param e
      *            the event to be deleted
      */
     public void deleteEvent(Event e) {
         long id = e.getId();
         System.out.println("Event deleted with id: " + id);
         database.delete(H.TABLE_EVENTS, H.E_ID + " = " + id, null);
     }
 
     /**
      * Delete a Person in the database.
      *
      * @param p
      *            the event to be deleted
      */
     public void deletePerson(Person p) {
         long id = p.getId();
         System.out.println("Person deleted with id: " + id);
         database.delete(H.TABLE_PERSONS, H.P_ID + " = " + id, null);
     }
 
     /**
      * Convert an event to a ContentValues in order to store it into the
      * database.
      *
      * @param e
      *            the event to be converted.
      * @return the ContentValues obtained by this event.
      */
     private ContentValues eventToValues(Event e) {
 
         ContentValues values = new ContentValues();
         values.put(H.E_NAME, e.getName());
         values.put(H.E_OWNER, e.getOwner());
         values.put(H.E_STIME, e.getStartTime());
         values.put(H.E_ETIME, e.getEndTime());
         values.put(H.E_SDATE, e.getStartDate());
         values.put(H.E_EDATE, e.getEndDate());
         char[] days = e.getDays();
         for (int i = 0; i < 7; i++) {
             values.put(H.E_DAYS[i + 1], days[i] == '*' ? 0 : 1);
         }
 
         return values;
     }
 
     /**
      * Get a full List of Person from the database. This list is sorted
      * alphabetically by name.
      *
      * @return the List of Persons
      */
     public List<Person> getAllPersons() {
         List<Person> persons = new ArrayList<Person>();
 
         Cursor cursor =
                 database.query(H.TABLE_PERSONS, allPersonColumns, null,
                         null, null, null, H.P_NAME);
         cursor.moveToFirst();
         while (!cursor.isAfterLast()) {
             persons.add(cursorToPerson(cursor));
             cursor.moveToNext();
         }
         cursor.close();
         return persons;
     }
 
     /**
      * Get a List of Events of a day by certain criteria.
      *
      * @param personId
      *            the owner Id
      * @param date
      *            format YYYYMMDD
      * @param dayOfWeek
      *            1: Sunday 2: Monday ... 7: Saturday
      * @return the List of Events
      */
     public List<Event> getEventsForDay(long personId, String date,
             int dayOfWeek) {
 
         Cursor cursor =
                 database.query(H.TABLE_EVENTS, allEventColumns, H.E_OWNER
                         + " = " + personId + " and " + H.E_SDATE + " <= "
                         + date + " and " + H.E_EDATE + " >= " + date
                         + " and " + H.E_DAYS[dayOfWeek] + " = 1", null,
                         null, null, H.E_STIME);
         // *********************************************
         Log.d(TAG, "Cursor.getCount() = " + cursor.getCount());
         // ---------------------------------------------
         List<Event> result = new ArrayList<Event>();
         cursor.moveToFirst();
         while (!cursor.isAfterLast()) {
             result.add(cursorToEvent(cursor));
             cursor.moveToNext();
         }
         cursor.close();
         return result;
     }
 
     /**
      * Get the list of all events for a person by a persons id.
      *
      * @param personId
      *            the id to identify the person
      * @return the full list of events
      */
     public List<Event> getEventsForPerson(long personId) {
         Cursor cursor =
                 database.query(H.TABLE_EVENTS, allEventColumns, H.E_OWNER
                         + " = " + personId, null, null, null, H.E_SDATE);
         // *********************************************
         Log.d(TAG, "Cursor.getCount() = " + cursor.getCount());
         // ---------------------------------------------
         List<Event> result = new ArrayList<Event>();
         cursor.moveToFirst();
         while (!cursor.isAfterLast()) {
             result.add(cursorToEvent(cursor));
             cursor.moveToNext();
         }
         cursor.close();
         return result;
     }
 
     /**
      * Get an event by the id.
      *
      * @param id
      *            the id of the event
      * @return the event
      */
     public Event getEvent(long id) {
         Cursor cursor =
                 database.query(H.TABLE_EVENTS, allEventColumns, H.E_ID
                         + " = " + id, null, null, null, null);
         cursor.moveToFirst();
         if (cursor.isAfterLast()) {
             return null;
         }
         Event e = cursorToEvent(cursor);
         cursor.close();
         return e;
     }
 
     /**
      * Get a person by the id.
      *
      * @param id
      *            the id
      * @return the Person
      */
     public Person getPerson(long id) {
         Cursor cursor =
                 database.query(H.TABLE_PERSONS, allPersonColumns, H.P_ID
                         + " = " + id, null, null, null, null);
         cursor.moveToFirst();
         if (cursor.isAfterLast()) {
             return null;
         }
         Person p = cursorToPerson(cursor);
         cursor.close();
         return p;
     }
 
     /**
      * Open the database.
      *
      * @throws SQLException
      */
     public void open() throws SQLException {
         database = dbHelper.getWritableDatabase();
     }
 
     /**
      * @param p
      * @return
      */
     /**
      * Convert a Person object to a ContentValues object in order to store it
      * into the database.
      *
      * @param p
      * @return
      */
     private ContentValues personToValue(Person p) {
         ContentValues values = new ContentValues();
         values.put(H.P_NAME, p.getName());
         values.put(H.P_PHONE, p.getPhoneNumber());
         return values;
     }
 
     /**
      * Update an existed event.
      *
      * @param e
      *            the event to be updated.
      */
     public void updateEvent(Event e) {
         long eventId = e.getId();
 
        database.update(H.TABLE_PERSONS, eventToValues(e), H.P_ID + " = "
                 + eventId, null);
     }
 
     /**
      * Update an existed person.
      *
      * @param p
      *            the person to be updated.
      */
     public void updatePerson(Person p) {
         long personId = p.getId();
 
         database.update(H.TABLE_PERSONS, personToValue(p), H.P_ID + " = "
                 + personId, null);
     }
 }
