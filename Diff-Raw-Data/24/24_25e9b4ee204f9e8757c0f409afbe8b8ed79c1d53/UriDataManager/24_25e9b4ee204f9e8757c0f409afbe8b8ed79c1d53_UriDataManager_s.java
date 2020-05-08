 package interdroid.vdb.avro.model;
 
 import interdroid.vdb.content.EntityUriBuilder;
 import interdroid.vdb.content.EntityUriMatcher;
 import interdroid.vdb.content.GenericContentProvider;
 import interdroid.vdb.content.EntityUriMatcher.UriMatch;
 
 import org.apache.avro.Schema;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import android.content.ContentResolver;
 import android.content.ContentValues;
 import android.database.Cursor;
 import android.net.Uri;
 
 public class UriDataManager {
     private static final Logger logger = LoggerFactory
             .getLogger(UriDataManager.class);
 
     static void safeClose(Cursor cursor) {
         if (null != cursor) {
             try {
                 cursor.close();
             } catch (Exception e) {
                 // Ignored
             }
         }
     }
 
     @SuppressWarnings("rawtypes")
     static Object loadDataFromUri(ContentResolver resolver, Uri rootUri, Cursor cursor,
             String fieldName, Schema fieldSchema) throws NotBoundException {
         logger.debug("Loading field: " + fieldName + " : " + fieldSchema);
         Object value = null;
         switch (fieldSchema.getType()) {
         case ARRAY:
             value = new UriArray(Uri.withAppendedPath(rootUri, fieldName),
                     fieldSchema).load(resolver, fieldName);
             break;
         case BOOLEAN:
             value = (cursor.getInt(cursor.getColumnIndex(fieldName)) == 1);
             break;
         case BYTES:
             // TODO: Should these be handled using streams?
             value = cursor.getBlob(cursor.getColumnIndex(fieldName));
             break;
         case DOUBLE:
             value = cursor.getDouble(cursor.getColumnIndex(fieldName));
             break;
         case ENUM:
             value = cursor.getInt(cursor.getColumnIndex(fieldName));
             break;
         case FIXED:
             // TODO: Should these be handled using streams?
             value = cursor.getBlob(cursor.getColumnIndex(fieldName));
             break;
         case FLOAT:
             value = cursor.getFloat(cursor.getColumnIndex(fieldName));
             break;
         case INT:
             value = cursor.getInt(cursor.getColumnIndex(fieldName));
             break;
         case LONG:
             value = cursor.getLong(cursor.getColumnIndex(fieldName));
             break;
         case MAP:
             value = new UriMap(Uri.withAppendedPath(rootUri, fieldName),
                     fieldSchema).load(resolver, fieldName);
             break;
         case NULL:
             value = null;
             break;
         case RECORD:
             int recordId = cursor.getInt(cursor.getColumnIndex(fieldName));
             if (recordId > 0) {
                 Uri recordUri = getRecordUri(rootUri, fieldSchema);
                 value = new UriRecord(Uri.withAppendedPath(recordUri, String.valueOf(recordId)), fieldSchema).load(resolver);
             } else {
                 value = null;
             }
             break;
         case STRING:
             logger.debug("Loading {} : columns: {}", fieldName, cursor.getColumnNames());
             value = cursor.getString(cursor.getColumnIndex(fieldName));
             logger.debug("Loaded value: " + value);
             break;
         case UNION:
             value = new UriUnion(fieldSchema).load(resolver, rootUri, cursor, fieldName);
             break;
         default:
             throw new RuntimeException("Unsupported type: " + fieldSchema);
         }
         return value;
     }
 
     static Uri getRecordUri(Uri rootUri, Schema fieldSchema) {
         UriMatch match = EntityUriMatcher.getMatch(rootUri);
         return Uri.withAppendedPath(match.getCheckoutUri(), fieldSchema.getFullName());
     }
 
     @SuppressWarnings("rawtypes")
     static Uri storeDataToUri(ContentResolver resolver, Uri rootUri, ContentValues values,
             String fieldName, Schema fieldSchema, Object data) throws NotBoundException {
         logger.debug("Storing to: " + rootUri + " fieldName: " + fieldName + " schema: " + fieldSchema);
         Uri dataUri = null;
         switch (fieldSchema.getType()) {
         case ARRAY:
             if (data != null) {
                 UriArray array = (UriArray)data;
                 array.save(resolver, fieldName);
                 dataUri = array.getInstanceUri();
             } else {
                 // Make sure any old values don't exist
                 logger.warn("Clearing old values.");
                 new UriArray(Uri.withAppendedPath(rootUri, fieldName), fieldSchema).delete(resolver);
             }
             break;
         case BOOLEAN:
             values.put(fieldName, (Boolean) data);
             break;
         case BYTES:
             values.put(fieldName, (byte[]) data);
             break;
         case DOUBLE:
             values.put(fieldName, (Double) data);
             break;
         case ENUM:
             values.put(fieldName, (Integer) data);
             break;
         case FIXED:
             values.put(fieldName, (byte[]) data);
             break;
         case FLOAT:
             values.put(fieldName, (Float) data);
             break;
         case INT:
             values.put(fieldName, (Integer) data);
             break;
         case LONG:
             values.put(fieldName, (Long) data);
             break;
         case MAP:
             if (data != null) {
                 UriMap map = (UriMap)data;
                 map.save(resolver, fieldName);
                 dataUri = map.getInstanceUri();
             } else {
                 new UriMap(Uri.withAppendedPath(rootUri, fieldName), fieldSchema).delete(resolver);
             }
             break;
         case NULL:
             values.putNull(fieldName);
             break;
         case RECORD:
             if (data != null) {
                 UriRecord record = (UriRecord)data;
                UriMatch match = EntityUriMatcher.getMatch(rootUri);

                // Is there an existing record at this location.
                Uri recordUri;
                Uri baseUri;
                switch (match.type) {
                case LOCAL_BRANCH:
                    baseUri = Uri.withAppendedPath(EntityUriBuilder.branchUri(match.authority, match.repositoryName, match.reference),
                            GenericContentProvider.escapeName(match.repositoryName, record.getSchema().getNamespace(), record.getSchema().getName()));
                    if (match.entityIdentifier != null) {
                        recordUri = Uri.withAppendedPath(baseUri, match.entityIdentifier);
                    } else {
                        recordUri = insertUri(resolver, baseUri, new ContentValues());
                    }
                    record.setInstanceUri(recordUri);
                    dataUri = recordUri;
                    break;
                default:
                    throw new RuntimeException("Can only store to local branches.");
                }
                 record.save(resolver);
             }
             break;
         case STRING:
             values.put(fieldName, (String) data);
             break;
         case UNION:
             if (data != null) {
                 UriUnion union = (UriUnion)data;
                 union.save(resolver, rootUri, values, fieldName);
             }
             break;
         default:
             throw new RuntimeException("Unsupported type: " + fieldSchema);
         }
 
         return dataUri;
     }
 
     public static Uri insertUri(ContentResolver resolver, Uri baseUri,
             ContentValues contentValues) {
         return resolver.insert(baseUri, contentValues);
     }
 
     public static void updateUriOrThrow(ContentResolver resolver, Uri rootUri,
             ContentValues values) {
         logger.debug("Updating: " + rootUri);
         if (values.size() > 0) {
             // Turns out update returns 0 if nothing changed in the row. Ah well. Nice try.
             //          int count =
             resolver.update(rootUri, values, null, null);
             //          if (count != 1) {
             //              throw new RuntimeException("Error updating record. Count was: "
             //                      + count);
             //          }
         }
     }
 }
