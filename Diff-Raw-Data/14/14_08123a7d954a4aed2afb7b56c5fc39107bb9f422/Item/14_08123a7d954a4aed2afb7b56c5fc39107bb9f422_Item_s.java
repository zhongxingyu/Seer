 package net.sourcewalker.garanbot.api;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Locale;
 
 import net.sourcewalker.garanbot.data.GaranbotDBMetaData;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.json.JSONTokener;
 
 import android.content.ContentValues;
 import android.database.Cursor;
 
 /**
  * Contains the data of one registered item.
  * 
  * @author Xperimental
  */
 public class Item {
 
     public static final int UNKNOWN_ID = -1;
 
     private static final SimpleDateFormat JSON_DATE_FORMAT = new SimpleDateFormat(
             "yyyy-MM-dd'T'HH:mm:ssZ");
 
     private static final SimpleDateFormat HTTP_DATE_FORMAT = new SimpleDateFormat(
             "EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH);
 
     /**
      * Unique id in database. This ID is assigned by the device database and is
      * only {@link #UNKNOWN_ID} if the item is not saved to a database. Items
      * with no local ID should only be used to create new items in the local
      * database and then discarded.
      */
     private final int localId;
 
     /**
      * Unique id on server. This ID is assigned by the Garanbo server and is
      * {@link #UNKNOWN_ID} for local-only items ({@link #getModifiedAt()} is
      * {@link ModificationOrigin#CREATED}).
      */
     private int serverId;
 
     /**
      * Displayname of item.
      */
     private String name = "";
 
     /**
      * Manufacturer of item.
      */
     private String manufacturer = "";
 
     /**
      * Model name of item.
      */
     private String itemType = "";
 
     /**
      * Vendor where the item was purchased.
      */
     private String vendor = "";
 
     /**
      * Location where the item is currently.
      */
     private String location = "";
 
     /**
      * Notes about the item.
      */
     private String notes = "";
 
     /**
      * True, if the user has uploaded a picture for this item.
      */
     private boolean hasPicture = false;
 
     /**
      * Visibility of item to friends.
      */
     private ItemVisibility visibility = ItemVisibility.PRIVATE;
 
     /**
      * Date the item was purchased.
      */
     private Date purchaseDate = new Date();
 
     /**
      * Date the item's warranty ends.
      */
     private Date endOfWarranty = new Date();
 
     /**
      * Date the Item was last modified.
      */
     private Date lastModified = new Date();
 
     /**
      * Where the Item was last modified.
      */
     private ModificationOrigin modifiedAt = ModificationOrigin.UNKNOWN;
 
     public int getLocalId() {
         return localId;
     }
 
     public int getServerId() {
         return serverId;
     }
 
     public void setServerId(int serverId) {
         this.serverId = serverId;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public String getManufacturer() {
         return manufacturer;
     }
 
     public void setManufacturer(String manufacturer) {
         this.manufacturer = manufacturer;
     }
 
     public String getItemType() {
         return itemType;
     }
 
     public void setItemType(String itemType) {
         this.itemType = itemType;
     }
 
     public String getVendor() {
         return vendor;
     }
 
     public void setVendor(String vendor) {
         this.vendor = vendor;
     }
 
     public String getLocation() {
         return location;
     }
 
     public void setLocation(String location) {
         this.location = location;
     }
 
     public String getNotes() {
         return notes;
     }
 
     public void setNotes(String notes) {
         this.notes = notes;
     }
 
     public boolean hasPicture() {
         return hasPicture;
     }
 
     public void setHasPicture(boolean hasPicture) {
         this.hasPicture = hasPicture;
     }
 
     public ItemVisibility getVisibility() {
         return visibility;
     }
 
     public void setVisibility(ItemVisibility visibility) {
         this.visibility = visibility;
     }
 
     public Date getPurchaseDate() {
         return purchaseDate;
     }
 
     public void setPurchaseDate(Date purchaseDate) {
         this.purchaseDate = purchaseDate;
     }
 
     public Date getEndOfWarranty() {
         return endOfWarranty;
     }
 
     public void setEndOfWarranty(Date endOfWarranty) {
         this.endOfWarranty = endOfWarranty;
     }
 
     public Date getLastModified() {
         return lastModified;
     }
 
     public void setLastModified(Date lastModified) {
         this.lastModified = lastModified;
     }
 
     public ModificationOrigin getModifiedAt() {
         return modifiedAt;
     }
 
     public void setModifiedAt(ModificationOrigin modifiedAt) {
         this.modifiedAt = modifiedAt;
     }
 
     public Item(int id) {
         this.localId = id;
         this.serverId = UNKNOWN_ID;
     }
 
     public JSONObject json() throws ClientException {
         try {
             JSONObject result = new JSONObject();
             if (getServerId() != UNKNOWN_ID) {
                 result.put("id", getServerId());
             }
             result.put("name", getName());
             result.put("manufacturer", getManufacturer());
             result.put("itemType", getItemType());
             result.put("vendor", getVendor());
             result.put("location", getLocation());
             result.put("notes", getNotes());
             result.put("hasPicture", Boolean.toString(hasPicture()));
             result.put("visibility",
                     Integer.toString(getVisibility().getValue()));
             result.put("purchaseDate", dateString(getPurchaseDate()));
             result.put("endOfWarranty", dateString(getEndOfWarranty()));
             return result;
         } catch (JSONException e) {
             throw new ClientException("Error creating JSON object: "
                     + e.getMessage(), e);
         }
     }
 
     private String dateString(Date date) {
         String dateString = JSON_DATE_FORMAT.format(date);
         return dateString.substring(0, 22) + ":" + dateString.substring(22);
     }
 
     /**
      * Tries to create a Item object from the provided JSON string.
      * 
      * @param content
      *            JSON content as String.
      * @param lastModified
      *            Value of last-modified-header if available or
      *            <code>null</code> if current device time should be used.
      * @return Item object
      * @throws ClientException
      *             If the provided JSON is not valid.
      */
     public static Item fromJSON(String content, String lastModified)
             throws ClientException {
         try {
             JSONObject object = (JSONObject) new JSONTokener(content)
                     .nextValue();
            Item result = new Item(object.getInt("id"));
             result.setName(object.getString("name"));
             result.setManufacturer(object.getString("manufacturer"));
             result.setItemType(object.getString("itemType"));
             result.setVendor(object.getString("vendor"));
             result.setLocation(object.getString("location"));
             result.setNotes(object.getString("notes"));
             result.setHasPicture(object.getBoolean("hasPicture"));
             result.setVisibility(ItemVisibility.parseInt(object
                     .getInt("visibility")));
             result.setPurchaseDate(parseDate(object.getString("purchaseDate")));
             result.setEndOfWarranty(parseDate(object.getString("endOfWarranty")));
             if (lastModified == null) {
                 result.setLastModified(new Date());
             } else {
                 try {
                     result.setLastModified(HTTP_DATE_FORMAT.parse(lastModified));
                 } catch (ParseException e) {
                     throw new ClientException(
                             "Error parsing Last-Modified header: "
                                     + e.getMessage(), e);
                 }
             }
             result.setModifiedAt(ModificationOrigin.MODIFIED_SERVER);
             return result;
         } catch (JSONException e) {
             throw new ClientException("Error parsing Item: " + e.getMessage(),
                     e);
         }
     }
 
     private static Date parseDate(String string) throws ClientException {
         try {
             return JSON_DATE_FORMAT.parse(string);
         } catch (ParseException e) {
             throw new ClientException("Error parsing date: " + e.getMessage(),
                     e);
         }
     }
 
     /**
      * Tries to create an Item object from the database row.
      * 
      * @param cursor
      *            Cursor to read row from.
      * @return Item object.
      * @throws ClientException
      *             If the cursor doesn't contain the data necessary for the
      *             object.
      */
     public static Item fromCursor(Cursor cursor) throws ClientException {
         int id = cursor.getInt(cursor.getColumnIndex(GaranbotDBMetaData._ID));
         Item result = new Item(id);
         try {
             result.setName(cursor.getString(cursor
                     .getColumnIndexOrThrow(GaranbotDBMetaData.NAME)));
             result.setManufacturer(cursor.getString(cursor
                     .getColumnIndexOrThrow(GaranbotDBMetaData.MANUFACTURER)));
             result.setItemType(cursor.getString(cursor
                     .getColumnIndexOrThrow(GaranbotDBMetaData.ITEMTYPE)));
             result.setVendor(cursor.getString(cursor
                     .getColumnIndexOrThrow(GaranbotDBMetaData.VENDOR)));
             result.setLocation(cursor.getString(cursor
                     .getColumnIndexOrThrow(GaranbotDBMetaData.LOCATION)));
             result.setNotes(cursor.getString(cursor
                     .getColumnIndexOrThrow(GaranbotDBMetaData.NOTES)));
             result.setHasPicture(cursor.getInt(cursor
                     .getColumnIndexOrThrow(GaranbotDBMetaData.HASPICTURE)) == 1);
             result.setVisibility(ItemVisibility.parseInt(cursor.getInt(cursor
                     .getColumnIndexOrThrow(GaranbotDBMetaData.VISIBILITY))));
             result.setPurchaseDate(parseDate(cursor.getString(cursor
                     .getColumnIndexOrThrow(GaranbotDBMetaData.PURCHASEDATE))));
             result.setEndOfWarranty(parseDate(cursor.getString(cursor
                     .getColumnIndexOrThrow(GaranbotDBMetaData.ENDOFWARRANTY))));
             result.setLastModified(parseDate(cursor.getString(cursor
                     .getColumnIndexOrThrow(GaranbotDBMetaData.LAST_MODIFIED))));
             result.setModifiedAt(ModificationOrigin.parseInt(cursor.getInt(cursor
                     .getColumnIndexOrThrow(GaranbotDBMetaData.MODIFIED_AT))));
             result.setServerId(cursor.getInt(cursor
                     .getColumnIndexOrThrow(GaranbotDBMetaData.SERVER_ID)));
         } catch (IllegalArgumentException e) {
             throw new ClientException("Cursor data invalid: " + e.getMessage(),
                     e);
         }
         return result;
     }
 
     /**
      * Turn the item into a representation the local database can use.
      * 
      * @return {@link ContentValues} object filled with the data from the Item
      *         instance.
      */
     public ContentValues toContentValues() {
         ContentValues result = new ContentValues();
         result.put(GaranbotDBMetaData._ID, getLocalId());
         result.put(GaranbotDBMetaData.NAME, getName());
         result.put(GaranbotDBMetaData.MANUFACTURER, getManufacturer());
         result.put(GaranbotDBMetaData.ITEMTYPE, getItemType());
         result.put(GaranbotDBMetaData.VENDOR, getVendor());
         result.put(GaranbotDBMetaData.LOCATION, getLocation());
         result.put(GaranbotDBMetaData.NOTES, getNotes());
         result.put(GaranbotDBMetaData.HASPICTURE, hasPicture() ? 1 : 0);
         result.put(GaranbotDBMetaData.VISIBILITY, getVisibility().getValue());
         result.put(GaranbotDBMetaData.PURCHASEDATE,
                 dateString(getPurchaseDate()));
         result.put(GaranbotDBMetaData.ENDOFWARRANTY,
                 dateString(getEndOfWarranty()));
         result.put(GaranbotDBMetaData.LAST_MODIFIED,
                 dateString(getLastModified()));
         result.put(GaranbotDBMetaData.MODIFIED_AT, getModifiedAt().getValue());
         result.put(GaranbotDBMetaData.SERVER_ID, getServerId());
         return result;
     }
 
 }
