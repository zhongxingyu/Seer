 /*
  * Copyright (c) 2008-2012 Vrije Universiteit, The Netherlands All rights
  * reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *
  * Redistributions of source code must retain the above copyright notice, this
  * list of conditions and the following disclaimer.
  *
  * Redistributions in binary form must reproduce the above copyright notice,
  * this list of conditions and the following disclaimer in the documentation
  * and/or other materials provided with the distribution.
  *
  * Neither the name of the Vrije Universiteit nor the names of its contributors
  * may be used to endorse or promote products derived from this software without
  * specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS ``AS IS''
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  */
 package interdroid.vdb.content;
 
 import java.io.IOException;
 import java.util.Map.Entry;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import interdroid.vdb.content.EntityUriMatcher.UriMatch;
 import interdroid.vdb.content.metadata.EntityInfo;
 import interdroid.vdb.content.metadata.Metadata;
 import interdroid.vdb.persistence.api.VdbCheckout;
 import interdroid.vdb.persistence.api.VdbInitializer;
 import interdroid.vdb.persistence.api.VdbRepository;
 import interdroid.vdb.persistence.api.VdbRepositoryRegistry;
 
 import android.content.ContentProvider;
 import android.content.ContentUris;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.pm.ProviderInfo;
 import android.database.Cursor;
 import android.database.DatabaseUtils;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteQueryBuilder;
 import android.net.Uri;
 import android.text.TextUtils;
 
 /**
  * The base for all content providers within VDB.
  *
  * @author nick &lt;palmer@cs.vu.nl&gt;
  */
 public abstract class GenericContentProvider extends ContentProvider {
 
     /**
      * The logger.
      */
     static final Logger LOG =
             LoggerFactory.getLogger(GenericContentProvider.class);
 
     /**
      * The separator used in field names.
      */
     public static final String SEPARATOR = "_";
     /**
      * The parent column prefix for columns which reference parent keys.
      */
     public static final String PARENT_COLUMN_PREFIX = SEPARATOR + "parent";
 
     /**
      * The metadata for this content provider.
      */
     protected final Metadata mMetadata;
     /**
      * The namespace for this content provider.
      */
     protected final String mNamespace;
     /**
      * The repository this provider stores information in.
      */
     protected VdbRepository mVdbRepo;
 
     // TODO: (nick) Support for multiple key tables?
     // TODO: (nick) Support for complex primary keys in all tables?
 
     /**
      * Escapes an entity name using the default namespace for this provider.
      * @param info the info for the entity.
      * @return the escaped name.
      */
     private String escapeName(final EntityInfo info) {
         return escapeName(mNamespace, info);
     }
 
     /**
      * Escapes an entity using the specified namespace.
      * @param namespace the namespace to escape for.
      * @param info the info for the entity.
      * @return the escaped name.
      */
     static String escapeName(final String namespace,
             final EntityInfo info) {
         return DatabaseUtils.sqlEscapeString(
                 escapeName(namespace, info.namespace(), info.name()));
     }
 
     /**
      * Escapes an entity using the specified default namespace, namespace and
      * name.
      * @param defaultNamespace the default namespace for this provider
      * @param namespace the namespace for the entity
      * @param name the name to escape
      * @return the escaped name.
      */
     public static String escapeName(final String defaultNamespace,
             final String namespace, final String name) {
         // Don't include the namespace for entities
         // that match our name for simplicity
         if (defaultNamespace.equals(namespace)) {
             return name.replace('.', '_');
         } else {
             // But entities in other namespaces we use the
             // full namespace. This shouldn't happen often.
             return namespace.replace('.', '_') + "_" + name.replace('.', '_');
         }
     }
 
     /**
      * @return the initializer for this database.
      */
     public abstract VdbInitializer buildInitializer();
 
     @Override
     public final boolean onCreate() {
         return true;
     }
 
     /**
      * Called when this provider is attached to a context.
      * This gives us access to the context for the first time
      * which we then use to fetch the actually repository, possibly
      * initializing it if required.
      * @param context the context we are being attached to
      * @param info the info about the provider
      */
     public final void attachInfo(final Context context,
             final ProviderInfo info) {
         super.attachInfo(context, info);
         if (LOG.isDebugEnabled()) {
             LOG.debug("attachInfo for: " + mNamespace);
         }
         try {
             mVdbRepo = VdbRepositoryRegistry.getInstance()
                     .getRepository(context, mNamespace);
         } catch (IOException e) {
             LOG.error("Unable to get repository for: " + mNamespace, e);
         }
         if (mVdbRepo == null) {
             LOG.debug("registering repository");
             try {
                 mVdbRepo = VdbRepositoryRegistry.getInstance()
                         .addRepository(context, mNamespace, buildInitializer());
             } catch (IOException e) {
                 throw new RuntimeException("Error initializing repository", e);
             }
         }
         LOG.debug("Fetched repository.");

         LOG.debug("Done handling attachment.");
     }
 
     /**
      * A place for subclasses to handle attachment.
      * @param context the context being attached to
      * @param info on the provider.
      */
     protected void onAttach(final Context context, final ProviderInfo info) {
         // For subclasses to implement
     }
 
     /**
      * Constructs a content provider with the given namespace and metadata.
      * @param namespace the namespace for this provider
      * @param metadata the metadata for this provider
      */
     public GenericContentProvider(final String namespace,
             final Metadata metadata) {
         mNamespace = namespace;
         mMetadata = metadata;
     }
 
     @Override
     public final String getType(final Uri uri) {
         if (LOG.isDebugEnabled()) {
             LOG.debug("Getting type of: " + uri);
         }
         final UriMatch result = EntityUriMatcher.getMatch(uri);
         final EntityInfo info = mMetadata.getEntity(result);
         if (LOG.isDebugEnabled()) {
             LOG.debug("Got entity: " + info);
         }
         if (info != null) {
             return info.itemContentType();
         }
 
         return null;
     }
 
     /**
      * @param uri the uri to get from
      * @param result the match result for that uri
      * @return a checkout for the uri / match
      */
     private VdbCheckout getCheckoutFor(final Uri uri, final UriMatch result) {
         if (LOG.isDebugEnabled()) {
             LOG.debug("Getting checkout for: " + uri);
         }
         try {
             switch(result.type) {
             case LOCAL_BRANCH:
                 return mVdbRepo.getBranch(result.reference);
             case COMMIT:
                 return mVdbRepo.getCommit(result.reference);
             case REMOTE_BRANCH:
                 return mVdbRepo.getRemoteBranch(result.reference);
             default:
                 throw new RuntimeException("Unsupported uri type " + uri);
             }
         } catch (IOException e) {
             throw new RuntimeException(e);
         }
     }
 
     @Override
     public final Uri insert(final Uri uri, final ContentValues userValues) {
         Uri returnUri = null;
 
         if (LOG.isDebugEnabled()) {
             LOG.debug("Inserting into: " + uri);
         }
         final UriMatch result = EntityUriMatcher.getMatch(uri);
         if (result.entityIdentifier != null) { /* don't accept ID queries */
             throw new IllegalArgumentException("Invalid item URI " + uri);
         }
         if (LOG.isDebugEnabled()) {
             LOG.debug("Getting entity: " + result.entityName);
         }
         if (result.entityName == null) {
             throw new RuntimeException("Uri does not specify an entity: "
                     + result.entityName);
         }
         final EntityInfo entityInfo = mMetadata.getEntity(result);
         if (entityInfo == null) {
             throw new RuntimeException("Unable to find entity for: "
                     + result.entityName);
         }
         if (LOG.isDebugEnabled()) {
             LOG.debug("Got info: " + entityInfo.name());
             LOG.debug("Getting checkout for: " + uri);
         }
         VdbCheckout vdbBranch = getCheckoutFor(uri, result);
 
         ContentValues values;
         if (userValues != null) {
             values = sanitize(userValues);
         } else {
             values = new ContentValues();
         }
 
         // Propogate the change to the preInsertHook if there is one
         ContentChangeHandler handler =
                 ContentChangeHandler.getHandler(entityInfo.namespace(),
                         entityInfo.name());
         if (handler != null) {
             handler.preInsertHook(values);
         }
 
         SQLiteDatabase db;
         try {
             db = vdbBranch.getReadWriteDatabase();
         } catch (IOException e) {
             throw new RuntimeException("getReadWriteDatabase failed", e);
         }
 
         try {
             if (LOG.isDebugEnabled()) {
                 LOG.debug("Inserting: " + entityInfo.name() + " : "
                         + entityInfo.key.get(0).fieldName + ":"
                         + values.getAsString(
                                 entityInfo.key.get(0).fieldName)
                                 + " : " + values.size());
             }
             // Do we need to include the parent identifier?
             if (entityInfo.parentEntity != null
                     && result.parentEntityIdentifiers != null) {
                 if (LOG.isDebugEnabled()) {
                     LOG.debug("Adding parent id: "
                             + entityInfo.parentEntity.key.get(0).fieldName + ":"
                             + result.parentEntityIdentifiers.get(
                                     result.parentEntityIdentifiers.size() - 1));
                 }
                 values.put(PARENT_COLUMN_PREFIX
                         + entityInfo.parentEntity.key.get(0).fieldName,
                         result.parentEntityIdentifiers.get(
                                 result.parentEntityIdentifiers.size() - 1));
             }
             long rowId = db.insert(escapeName(entityInfo),
                     entityInfo.key.get(0).fieldName, values);
             if (rowId > 0) {
                 returnUri = ContentUris.withAppendedId(uri, rowId);
                 getContext().getContentResolver().notifyChange(returnUri, null);
             } else {
                 throw new SQLException("Failed to insert row into " + uri);
             }
             onPostInsert(returnUri, values);
         } finally {
             vdbBranch.releaseDatabase();
         }
 
         return returnUri;
     }
 
     @Override
     public final Cursor query(final Uri uri, final String[] projection,
             final String selection, final String[] selectionArgs,
             final String sortOrder) {
         if (LOG.isDebugEnabled()) {
             LOG.debug("Querying for: " + uri);
         }
         // Validate the requested uri
         final UriMatch result = EntityUriMatcher.getMatch(uri);
         LOG.debug("Query for: {} {}", result.entityName,
                 getClass().getCanonicalName());
         final EntityInfo entityInfo = mMetadata.getEntity(result);
         if (entityInfo == null) {
             throw new RuntimeException("Unable to find entity for: "
                     + result.entityName);
         }
         VdbCheckout vdbBranch = getCheckoutFor(uri, result);
 
         SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
 
         if (result.entityIdentifier != null) {
             qb.setTables(escapeName(entityInfo));
             // qb.setProjectionMap(sNotesProjectionMap);
             qb.appendWhere(entityInfo.key.get(0).fieldName
                     + "=" + result.entityIdentifier);
         } else {
             qb.setTables(escapeName(entityInfo));
         }
 
         // Append ID of parent if required
         if (hasParent(result, entityInfo)) {
             qb.appendWhere(PARENT_COLUMN_PREFIX
                     + entityInfo.parentEntity.key.get(0).fieldName + "="
                     + result.parentEntityIdentifiers.get(
                             result.parentEntityIdentifiers.size() - 1));
         }
 
         // Get the database and run the query
         SQLiteDatabase db;
         try {
             db = vdbBranch.getReadOnlyDatabase();
         } catch (IOException e) {
             throw new RuntimeException("getReadOnlyDatabase failed", e);
         }
 
         LOG.debug("Got database: {}", db);
 
         // TODO: (emilian) default sort order
 
         try {
             if (LOG.isDebugEnabled()) {
                 LOG.debug("Querying with: " + qb.buildQuery(projection,
                         selection, selectionArgs, null, null, sortOrder, null));
             }
             LOG.debug("Projection: " + projection);
             LOG.debug("Selection: " + selection);
             LOG.debug("SelectionArgs: " + selectionArgs);
             Cursor c = qb.query(db, projection, selection, selectionArgs,
                     null, null, sortOrder);
             LOG.debug("Got cursor: {}", c);
             if (c != null && getContext() != null) {
                 // Tell the cursor what uri to watch, so it knows
                 // when its source data changes
                 c.setNotificationUri(getContext().getContentResolver(), uri);
             }
             LOG.debug("Returning cursor.");
             return c;
         } finally {
             // TODO: Is this release legal here or does the
             // cursor still need it?
             vdbBranch.releaseDatabase();
         }
     }
 
     /**
      * Return true if this entity has a parent.
      * @param result the uri match for the entity
      * @param entityInfo the info for the entity
      * @return true if this entity has a parent
      */
     private boolean hasParent(final UriMatch result,
             final EntityInfo entityInfo) {
         return result.parentEntityIdentifiers != null
                 && entityInfo.parentEntity != null;
     }
 
     @Override
     public final int update(final Uri uri, final ContentValues values,
             final String where, final String[] whereArgs) {
         if (LOG.isDebugEnabled()) {
             LOG.debug("Updating: " + uri);
         }
         // Validate the requested uri
         final UriMatch result = EntityUriMatcher.getMatch(uri);
         final EntityInfo entityInfo = mMetadata.getEntity(result);
 
         if (entityInfo == null) {
             throw new RuntimeException("Unable to find entity for: " + uri);
         }
 
         VdbCheckout vdbBranch = getCheckoutFor(uri, result);
 
         int count = 0;
 
         SQLiteDatabase db;
         try {
             db = vdbBranch.getReadWriteDatabase();
         } catch (IOException e) {
             throw new RuntimeException("getReadWriteDatabase failed", e);
         }
 
         try {
             count = db.update(escapeName(entityInfo), sanitize(values),
                     prepareWhereClause(where, result, entityInfo),
                     prepareWhereArgs(whereArgs, result, entityInfo));
             onPostUpdate(uri, values, where, whereArgs);
         } finally {
             vdbBranch.releaseDatabase();
         }
 
         /*
          TODO: (emilian) implement auto commit support
          try {
             vdbBranch.commitBranch();
          } catch(IOException e) {
             LOG.debug(e.toString());
          }
          */
 
         getContext().getContentResolver().notifyChange(uri, null);
         LOG.debug("Updated: {}", count);
 
 
         return count;
     }
 
     /**
      * Prepares a where clause.
      * @param where the where string
      * @param result the uri match
      * @param entityInfo the info for the entity
      * @return a where clause
      */
     private String prepareWhereClause(final String where, final UriMatch result,
             final EntityInfo entityInfo) {
         boolean hasParentId = hasParent(result, entityInfo);
         boolean hasEntityId = result.entityIdentifier != null;
         boolean hasWhere =  !TextUtils.isEmpty(where);
         StringBuffer whereClause = new StringBuffer();
         if (hasParentId) {
             whereClause.append(PARENT_COLUMN_PREFIX);
             whereClause.append(
                     entityInfo.parentEntity.key.get(0).fieldName);
             whereClause.append("=?");
             if (hasEntityId) {
                 whereClause.append(" AND ");
             }
         }
         if (hasEntityId) {
             whereClause.append(entityInfo.key.get(0).fieldName);
             whereClause.append("=?");
         }
         if (hasWhere) {
             if (hasEntityId || hasParentId) {
                 whereClause.append(" AND (");
                 whereClause.append(where);
                 whereClause.append(")");
             } else {
                 whereClause.append(where);
             }
         }
 
         return whereClause.toString();
     }
 
     /**
      * Prepares the where clause arguments.
      * @param whereArgs the arguments
      * @param result the match for the uri
      * @param entityInfo the info for this entity
      * @return an array
      */
     private String[] prepareWhereArgs(final String[] whereArgs,
             final UriMatch result, final EntityInfo entityInfo) {
         boolean hasParentId = hasParent(result, entityInfo);
         boolean hasEntityId = result.entityIdentifier != null;
         String[] preparedArgs = whereArgs;
 
         if (hasParentId && LOG.isDebugEnabled()) {
             LOG.debug("Adding parent id to query: {} {}",
                     PARENT_COLUMN_PREFIX
                     + entityInfo.parentEntity.key.get(0).fieldName,
                     result.parentEntityIdentifiers.get(
                             result.parentEntityIdentifiers.size() - 1));
         }
         if (whereArgs != null) {
             if (hasParentId || hasEntityId) {
                 // One or two more args depending on if we have a parent
                 int newLength = whereArgs.length + (hasParentId ? 1 : 0)
                         + (hasEntityId ? 1 : 0);
                 String[] temp = new String[newLength];
                 System.arraycopy(whereArgs, 0, temp, 0, whereArgs.length);
                 // Append ID of entity if required
                 if (hasEntityId) {
                     temp[temp.length - 1] = result.entityIdentifier;
                 }
                 // Append ID of parent if required
                 if (hasParentId) {
                     temp[temp.length - (hasEntityId ? 2 : 1)] =
                             result.parentEntityIdentifiers.get(
                                     result.parentEntityIdentifiers.size() - 1);
                 }
                 preparedArgs = temp;
             }
         } else {
             if (hasParentId && hasEntityId) {
                 preparedArgs =  new String[] {
                         result.parentEntityIdentifiers.get(
                                 result.parentEntityIdentifiers.size() - 1),
                                 result.entityIdentifier};
             } else if (!hasParentId && hasEntityId) {
                 preparedArgs =  new String[] {result.entityIdentifier};
             } else if (hasParentId && !hasEntityId) {
                 preparedArgs =  new String[] {
                         result.parentEntityIdentifiers.get(
                                 result.parentEntityIdentifiers.size() - 1)};
             }
         }
         return preparedArgs;
     }
 
     /**
      * Android does not quote identifiers so we have to handle that.
      * @param values the values to be quoted
      * @return a sanitized version of the values.
      */
     private ContentValues sanitize(final ContentValues values) {
         ContentValues cleanValues = new ContentValues();
         for (Entry<String, Object> val : values.valueSet()) {
             Object value = val.getValue();
             String cleanName;
             if (val.getKey().charAt(0) == '\'') {
                 cleanName = val.getKey();
             } else {
                 cleanName = "'" + val.getKey() + "'";
             }
             // This really sucks. There is no generic put an object....
             if (value == null) {
                 cleanValues.putNull(cleanName);
             } else if (value instanceof Boolean) {
                 cleanValues.put(cleanName, (Boolean) value);
             } else if (value instanceof Byte) {
                 cleanValues.put(cleanName, (Byte) value);
             } else if (value instanceof byte[]) {
                 cleanValues.put(cleanName, (byte[]) value);
             } else if (value instanceof Double) {
                 cleanValues.put(cleanName, (Double) value);
             } else if (value instanceof Float) {
                 cleanValues.put(cleanName, (Float) value);
             } else if (value instanceof Integer) {
                 cleanValues.put(cleanName, (Integer) value);
             } else if (value instanceof Long) {
                 cleanValues.put(cleanName, (Long) value);
             } else if (value instanceof Short) {
                 cleanValues.put(cleanName, (Short) value);
             } else if (value instanceof String) {
                 cleanValues.put(cleanName, (String) value);
             } else {
                 throw new RuntimeException(
                         "Don't know how to add value of type: "
                                 + value.getClass().getCanonicalName());
             }
         }
         return cleanValues;
     }
 
     @Override
     public final int delete(final Uri uri, final String where,
             final String[] whereArgs) {
         if (LOG.isDebugEnabled()) {
             LOG.debug("Delete Uri: " + uri);
         }
         // Validate the requested uri
         final UriMatch result = EntityUriMatcher.getMatch(uri);
         final EntityInfo entityInfo = mMetadata.getEntity(result);
         if (entityInfo == null) {
             throw new RuntimeException("Unable to find entity for: "
                     + result.entityName);
         }
         VdbCheckout vdbBranch = getCheckoutFor(uri, result);
 
         SQLiteDatabase db;
         try {
             db = vdbBranch.getReadWriteDatabase();
         } catch (IOException e) {
             throw new RuntimeException("getReadWriteDatabase failed", e);
         }
 
         try {
             int count = db.delete(escapeName(entityInfo),
                     prepareWhereClause(where, result, entityInfo),
                     prepareWhereArgs(whereArgs, result, entityInfo));
 
             getContext().getContentResolver().notifyChange(uri, null);
             return count;
         } finally {
             vdbBranch.releaseDatabase();
         }
     }
 
     /**
      * Called when an update is complete for subclasses to perform operations.
      * @param uri the uri being updated
      * @param values the updated values
      * @param where the where clause
      * @param whereArgs the arguments to the where clause
      */
     protected void onPostUpdate(final Uri uri, final ContentValues values,
             final String where, final String[] whereArgs) {
 
     }
 
     /**
      * Called when an insert is complete for subclasses to perform operations.
      * Note that the checkout is still held when this is called.
      *
      * @param uri the uri of the inserted item
      * @param userValues the values being inserted
      */
     protected void onPostInsert(final Uri uri, final ContentValues userValues) {
 
     }
 }
