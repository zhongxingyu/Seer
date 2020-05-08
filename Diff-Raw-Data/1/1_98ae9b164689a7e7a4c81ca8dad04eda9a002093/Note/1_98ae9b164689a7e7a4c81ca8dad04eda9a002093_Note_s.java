 package com.bryanjswift.simplenote.model;
 
 import com.bryanjswift.simplenote.Constants;
 import com.google.common.collect.ImmutableList;
 import org.joda.time.DateTime;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.util.List;
 
 public class Note {
     private static final Logger logger = LoggerFactory.getLogger(Note.class);
     private static final DateTime EPOCH = longAsDate(0);
     private static final List<String> NO_TAGS = (new ImmutableList.Builder<String>()).build();
 
     public static final Note EMPTY = new Note();
 
     public final String key;
     public final boolean deleted;
     public final DateTime modifydate;
     public final DateTime createdate;
     public final int syncnum;
     public final int version;
     public final int minversion;
     public final String sharekey;
     public final String publishkey;
     public final List<String> systemtags;
     public final List<String> tags;
     public final String content;
 
     /**
      * Create an empty Note
      */
     private Note() {
         this(null, false, EPOCH, EPOCH, 0, 0, 0, null, null, NO_TAGS, NO_TAGS, null);
     }
 
     /**
      * Create a Note instance from provided values
      * @param key to set
      * @param deleted to set
      * @param modifydate to set
      * @param createdate to set
      * @param syncnum to set
      * @param version to set
      * @param minversion to set
      * @param sharekey to set
      * @param publishkey to set
      * @param systemtags to copy and set
      * @param tags to copy and set
      * @param content to set
      */
     public Note(final String key, final boolean deleted, final DateTime modifydate,
                 final DateTime createdate, final int syncnum, final int version,
                 final int minversion, final String sharekey, final String publishkey,
                 final List<String> systemtags, final List<String> tags, final String content) {
         this.key = key;
         this.deleted = deleted;
         this.modifydate = modifydate;
         this.createdate = createdate;
         this.syncnum = syncnum;
         this.version = version;
         this.minversion = minversion;
         this.sharekey = sharekey;
         this.publishkey = publishkey;
         ImmutableList.Builder<String> systemtagsBuilder = ImmutableList.builder();
         systemtagsBuilder.addAll(systemtags);
         this.systemtags = systemtagsBuilder.build();
         ImmutableList.Builder<String> tagsBuilder = ImmutableList.builder();
         tagsBuilder.addAll(tags);
         this.tags = tagsBuilder.build();
         this.content = content;
     }
 
     /**
      * Create a Note instance from provided values
      * @param deleted to set
      * @param modifydate to set
      * @param createdate to set
      * @param systemtags to copy and set
      * @param tags to copy and set
      * @param content to set
      */
     public Note(final boolean deleted, final DateTime modifydate, final DateTime createdate,
                 final List<String> systemtags, final List<String> tags, final String content) {
         this.key = Constants.DEFAULT_KEY;
         this.deleted = deleted;
         this.modifydate = modifydate;
         this.createdate = createdate;
         this.syncnum = Constants.DEFAULT_VERSION;
         this.version = Constants.DEFAULT_VERSION;
         this.minversion = Constants.DEFAULT_VERSION;
         this.sharekey = null;
         this.publishkey = null;
         ImmutableList.Builder<String> systemtagsBuilder = ImmutableList.builder();
         systemtagsBuilder.addAll(systemtags);
         this.systemtags = systemtagsBuilder.build();
         ImmutableList.Builder<String> tagsBuilder = ImmutableList.builder();
         tagsBuilder.addAll(tags);
         this.tags = tagsBuilder.build();
         this.content = content;
     }
 
     /**
      * Create a note from a JSON string
      * @param json to pass to JSONObject constructor
      * @throws JSONException if a problem occurs while creating or reading JSONObject
      */
     public Note(final String json) throws JSONException {
         this(new JSONObject(json));
     }
 
     /**
      * Create a Note from a JSONObject by fetching values out of it
      * @param o JSONObject containing values to populate the note
      */
     public Note(final JSONObject o) {
         this.key = o.optString("key", null);
         this.deleted = intAsBoolean(o.optInt("deleted", 0));
         this.modifydate = longAsDate(o.optLong("modifydate", 0));
         this.createdate = longAsDate(o.optLong("createdate", 0));
         this.syncnum = o.optInt("syncnum", 0);
         this.version = o.optInt("version", 0);
         this.minversion = o.optInt("minversion", 0);
         this.sharekey = o.optString("sharekey", null);
         this.publishkey = o.optString("publishkey", null);
         this.systemtags = jsonArrayAsList(o.optJSONArray("systemtags"));
         this.tags = jsonArrayAsList(o.optJSONArray("tags"));
         this.content = o.optString("content", null);
     }
 
     /**
      * Create a new note with the delete flag updated
      * @param deleted to set on the new Note
      * @return a copy of this Note with deleted set to parameter
      */
     public Note setDeleted(final boolean deleted) {
         return new Note(this.key, deleted, this.modifydate, this.createdate, this.syncnum, this.version, this.minversion, this.sharekey, this.publishkey, this.systemtags, this.tags, this.content);
     }
 
     /**
      * Combine properties from passed in Note and this Note to create a new one
      * @param note to copy values from
      * @return a copy of the combination of this note and the passed in note
      */
     public Note merge(final Note note) {
         final String key = note.key == null ? this.key : note.key;
         final boolean deleted = this.deleted;
         final DateTime modifydate = note.modifydate;
         final DateTime createdate = note.createdate.equals(longAsDate(0)) ? this.createdate : note.createdate;
         final int syncnum = note.syncnum;
         final int version = note.version;
         final int minversion = note.minversion == 0 ? this.minversion : note.minversion;
         final String sharekey = note.sharekey == null ? this.sharekey : note.sharekey;
         final String publishkey = note.publishkey == null ? this.publishkey : note.publishkey;
         final List<String> systemtags = note.systemtags.size() == 0 ? this.systemtags : note.systemtags;
         final List<String> tags = note.tags.size() == 0 ? this.tags : note.tags;
         final String content = note.content == null ? this.content : note.content;
         return new Note(key, deleted, modifydate, createdate, syncnum, version, minversion, sharekey, publishkey, systemtags, tags, content);
     }
 
     /**
      * Convert a number of seconds into a DateTime
      * @param seconds since epoch
      * @return DateTime representing the instant seconds since epoch
      */
     private static DateTime longAsDate(final long seconds) {
         return new DateTime(seconds * 1000);
     }
 
     /**
      * Use Simplenote logic to coerce an integer to a boolean
      * @param i to treat as an integer
      * @return false if i != 0, true otherwise
      */
     private static boolean intAsBoolean(final int i) {
         return i != 0;
     }
 
     /**
      * Convert a JSONArray to an ImmutableList of Strings
      * @param a JSONArry to converto to a list
      * @return immutable list of Strings of the values in key's array
      */
     private static List<String> jsonArrayAsList(final JSONArray a) {
         final ImmutableList.Builder<String> builder = ImmutableList.builder();
         if (a != null) {
             final int size = a.length();
             for (int i = 0; i < size; i++) {
                 if (!a.isNull(i)) {
                     builder.add(a.optString(i));
                 }
             }
         }
         return builder.build();
     }
 
     /**
      * Get a JSONObject representation of Note that can be sent to Simplenote servers
      * @return JSONObject with the modifiable properties of a note populated
      */
     public JSONObject json() {
         JSONObject o = null;
         try {
             o = new JSONObject();
             o.put("key", key);
             o.put("deleted", deleted ? 1 : 0);
             if (modifydate != null) { o.put("modifydate", modifydate.getMillis() / 1000.0); }
             if (createdate != null) { o.put("createdate", createdate.getMillis() / 1000.0); }
             if (systemtags != null) { o.put("systemtags", systemtags); }
             if (tags != null) { o.put("tags", tags); }
             if (content != null) { o.put("content", content); }
         } catch (JSONException jsone) {
             logger.error("Unable to create Note from response {}", jsone);
         }
         return o;
     }
 
     /**
      * Create an empty Note with just a key
      * @param key to set
      */
     public static Note fromKey(final String key) {
         return new Note(key, false, null, null, 0, 0, 0, null, null, NO_TAGS, NO_TAGS, null);
     }
 }
