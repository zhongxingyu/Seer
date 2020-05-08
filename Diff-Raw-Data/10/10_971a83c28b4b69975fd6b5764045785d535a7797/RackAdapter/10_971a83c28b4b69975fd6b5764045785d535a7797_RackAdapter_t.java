 package no.rkkc.bysykkel.db;
 
 import java.util.ArrayList;
 
 import no.rkkc.bysykkel.OsloCityBikeAdapter;
 import no.rkkc.bysykkel.OsloCityBikeAdapter.OsloCityBikeException;
 import no.rkkc.bysykkel.model.Rack;
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 
 import com.google.android.maps.GeoPoint;
 
 public class RackAdapter extends DatabaseAdapter {
 
     private static final String TAG = "Bysyklist-RackDbAdapter";
     
     public RackAdapter(Context context) {
         super(context);
     }
     
     public void save(Rack rack) {
         ContentValues cv = new ContentValues();
         
         cv.put(DESCRIPTION, rack.getDescription());
         if (rack.getLocation() != null) {
             cv.put(LATITUDE, rack.getLocation().getLatitudeE6());
             cv.put(LONGITUDE, rack.getLocation().getLongitudeE6());
         }
         cv.put(VIEW_COUNTER, rack.getViewCount());
         cv.put(STARRED, rack.isStarred() ? 1 : 0);
         
         if (hasRack(rack.getId())) {
             getWritableDatabase().update(TABLE, cv, ID + " = " + rack.getId(), null);
         } else {
             cv.put(ID, rack.getId());
             getWritableDatabase().insert(TABLE, null, cv);
         }
     }
     
     public void clearRacks() {
         getWritableDatabase().delete(TABLE, null, null);
     }
     
     /**
      * 
      * 
      * @param id
      * @param live Whether to query ClearChannel to also populate this rack with live availability data.
      * @return
      */
     public Rack getRack(int id, boolean live) {
         Rack rack;
         String[] columns={DESCRIPTION, LATITUDE, LONGITUDE, VIEW_COUNTER, STARRED};
         
        Cursor cursor = getReadableDatabase().query(TABLE, columns, ID + " = " + Integer.toString(id), null, null, null, null);
         
         if (cursor.getCount() == 0) {
         	cursor.close();
             return null;
         }
         
         cursor.moveToFirst();
         String description = cursor.getString(cursor.getColumnIndex(DESCRIPTION));
         Integer latitude = cursor.getInt(cursor.getColumnIndex(LATITUDE));
         Integer longitude = cursor.getInt(cursor.getColumnIndex(LONGITUDE));
         Integer viewCount = cursor.getInt(cursor.getColumnIndex(VIEW_COUNTER));
         Boolean isStarred = (cursor.getInt(cursor.getColumnIndex(STARRED)) == 1) ? true : false;
        cursor.close();
         
         rack = new Rack(id, description, latitude, longitude, viewCount, isStarred);
         
         if (live) {
             Rack liveRack;
             try {
                 liveRack = new OsloCityBikeAdapter().getRack(id);
                 rack.setOnline(liveRack.isOnline());
                 if (liveRack.hasBikeAndSlotInfo()) {
 	                rack.setNumberOfReadyBikes(liveRack.getNumberOfReadyBikes());
 	                rack.setNumberOfEmptySlots(liveRack.getNumberOfEmptySlots());
                 }
             } catch (OsloCityBikeException e) {
                 e.printStackTrace();
             }
         }
 
 
         return rack;
     }
     
     public Rack getRack(int id) {
         return getRack(id, false);
     }
     
     public void deleteRack(int id) {
         getWritableDatabase().delete(TABLE, ID + " = " + Integer.toString(id), null);
     }
     
     public boolean hasRack(int id) {
         if (getRack(id) != null) {
             return true;
         } else {
             return false;
         }
     }
     
     public ArrayList<Integer> getRackIds() {
         String[] columns={ID};
         
         Cursor cursor = getReadableDatabase().query(TABLE, columns, null, null, null, null, null);
         
         ArrayList<Integer> rackIds = new ArrayList<Integer>();
         while (cursor.moveToNext()) {
             rackIds.add(cursor.getInt(cursor.getColumnIndex(ID)));
         }
         cursor.close();
         
         return rackIds;
     }
     
     public Integer[] getRackIds(GeoPoint topLeft, GeoPoint bottomRight) {
         String[] columns={ID};
         
         String maxLatitude = Integer.toString(topLeft.getLatitudeE6());
         String minLongitude = Integer.toString(topLeft.getLongitudeE6());
         String minLatitude = Integer.toString(bottomRight.getLatitudeE6());
         String maxLongitude = Integer.toString(bottomRight.getLongitudeE6());
         
         String[] queryParams = new String[] {minLatitude, maxLatitude, minLongitude, maxLongitude}; 
         
         Cursor cursor = getReadableDatabase().query(TABLE, columns, 
                 "latitude > ? and latitude < ? and longitude > ? and longitude < ?", queryParams,
                 null, null, null);
         
         Integer[] rackIds = new Integer[cursor.getCount()];
         for (int i = 0; i < cursor.getCount(); i++) {
             cursor.moveToNext();
             rackIds[i] = cursor.getInt(cursor.getColumnIndex(ID));
         }
         cursor.close();
         
         return rackIds;
     }
     
     public ArrayList<Rack> getRacks() {
         
         ArrayList<Integer> rackIds = getRackIds();
         ArrayList<Rack> racks = new ArrayList<Rack>();
         
         for (int id: rackIds) {
             racks.add(getRack(id));
         }
         
         return racks;
     }
     
     /**
      * 
      * @param limit Maximum number of favorites to return.
      * @return
      */
     public ArrayList<Rack> getFavorites(int limit) {
         ArrayList<Rack> racks = new ArrayList<Rack>();
         
         String[] columns={ID};
         String selection = "viewcount > 0";
         String order = "starred desc, viewcount desc";
         
         Cursor cursor = getReadableDatabase().query(TABLE, columns, selection, null, null, null, order);
 
         while (cursor.moveToNext() && cursor.getPosition() < limit) {
             int rackId = cursor.getInt(cursor.getColumnIndex(ID));
             racks.add(getRack(rackId));
         }
         cursor.close();
         
         return racks;
     }
     
     public void incrementViewCount(int rackId) {
         
         Rack rack = getRack(rackId);
         rack.incrementViewCount();
         save(rack);
     }
     
     public void toggleStarred(int rackId) {
         
         Rack rack = getRack(rackId);
         rack.setStarred(!rack.isStarred()); // Toggle
         save(rack);
     }
     
     public boolean hasRackData() {
         if (!getRackIds().isEmpty()) {
             return true;
         } else {
             return false;
         }
     }
 }
