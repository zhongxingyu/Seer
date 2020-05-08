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
 
 import interdroid.vdb.content.metadata.DatabaseFieldType;
 import interdroid.vdb.content.metadata.EntityInfo;
 import interdroid.vdb.content.metadata.FieldInfo;
 import interdroid.vdb.content.metadata.Metadata;
 import interdroid.vdb.persistence.api.VdbInitializer;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import android.content.ContentValues;
 import android.database.sqlite.SQLiteDatabase;
 
 /**
  * An initializer for a database. This is used to build the initial
  * schema for the database.
  *
  * @author nick &lt;palmer@cs.vu.nl&gt;
  *
  */
 public class DatabaseInitializer implements VdbInitializer {
     /**
      * The logger.
      */
     private static final Logger LOG = LoggerFactory
             .getLogger(DatabaseInitializer.class);
 
     /** The metadata for this database. */
     private final Metadata mDbMetadata;
     /** The namespace for this database. */
     private final String mNamespace;
     /** The schema for this database as a string. */
     private final String mSchema;
 
     /**
      * Constructs a database intitializer with no schema.
      * @param namespace the namespace for the database
      * @param metadata the metadata for the database.
      */
     public DatabaseInitializer(final String namespace,
             final Metadata metadata) {
         mDbMetadata = metadata;
         mNamespace = namespace;
         mSchema = "";
     }
 
     /**
      * Constructs a database inititalizer.
      * @param namespace the namespace for the database
      * @param metadata the metadata for the database
      * @param schema the schema as a string.
      */
     public DatabaseInitializer(final String namespace,
             final Metadata metadata, final String schema) {
         mDbMetadata = metadata;
         mNamespace = namespace;
         mSchema = schema;
     }
 
     @Override
     public final void onCreate(final SQLiteDatabase db) {
         if (LOG.isDebugEnabled()) {
             LOG.debug("Initializing database for: "
                     + mNamespace);
         }
         // Keep track of what has been built as we go so as not to duplicate
         HashMap<String, String> built = new HashMap<String, String>();
         for (EntityInfo entity : mDbMetadata.getEntities()) {
             // Only handle root entities.
             // Children get recursed so foreign key constraints all point up
             if (entity.parentEntity == null) {
                 buildTables(db, entity, built);
             }
         }
     }
 
     /**
      * Builds the tables for this database for the given entity.
      * @param db the database to build in
      * @param entity the entity to build tables for
      * @param built the hash of already built tables
      */
     private void buildTables(final SQLiteDatabase db,
             final EntityInfo entity, final HashMap<String, String> built) {
         boolean firstField = true;
         ArrayList<EntityInfo>children = new ArrayList<EntityInfo>();
 
         if (built.containsKey(entity.name())) {
             LOG.debug("Already built: {}", entity.name());
             return;
         }
 
         if (LOG.isDebugEnabled()) {
             LOG.debug("Creating table for: "
                     + entity.namespace() + " : " + entity.name()
                     + ":"
                     + GenericContentProvider.escapeName(mNamespace, entity));
         }
 
         db.execSQL("DROP TABLE IF EXISTS "
                 + GenericContentProvider.escapeName(mNamespace, entity));
 
         StringBuilder createSql = new StringBuilder("CREATE TABLE ");
         createSql.append(GenericContentProvider.escapeName(mNamespace, entity));
         createSql.append('(');
         for (FieldInfo field : entity.getFields()) {
             switch (field.dbType) {
             case ONE_TO_MANY_INT:
             case ONE_TO_MANY_STRING:
                 // Skip these since they are handled by putting the
                 // key for this one in the targetEntity
                 // but queue the child to be handled when we are
                 // done with this table.
                 LOG.debug("Queueing Target Entity: ", field.targetEntity);
                 children.add(field.targetEntity);
                 break;
             case ONE_TO_ONE:
                 LOG.debug("Building child table: ", field.targetEntity);
                 // First we need to build the child table so we can
                 // do the foreign key on this one
                 buildTables(db, field.targetEntity, built);
 
                 if (!firstField) {
                     createSql.append(",\n");
                 } else {
                     firstField = false;
                 }
                 createSql.append(
                         GenericContentProvider.sanitize(field.fieldName));
                 createSql.append(' ');
                 createSql.append(DatabaseFieldType.INTEGER);
                 createSql.append(" REFERENCES ");
                 createSql.append(
                         GenericContentProvider.escapeName(mNamespace,
                                 field.targetEntity));
                 createSql.append('(');
                 createSql.append(GenericContentProvider.sanitize(field.targetField.fieldName));
                 createSql.append(')');
                 createSql.append(" DEFERRABLE");
                 LOG.debug("Create SQL now: {}", createSql);
                 break;
             default:
                 if (!firstField) {
                     createSql.append(",\n");
                 } else {
                     firstField = false;
                 }
                 createSql.append(GenericContentProvider.sanitize(
                         field.fieldName));
                 createSql.append(' ');
                 createSql.append(field.dbTypeName());
                 if (field.targetEntity != null) {
                     createSql.append(" REFERENCES ");
                     createSql.append(
                             GenericContentProvider.escapeName(mNamespace,
                                     field.targetEntity));
                     createSql.append('(');
                     createSql.append(GenericContentProvider.sanitize(
                             field.targetField.fieldName));
                     createSql.append(") DEFERRABLE");
                 }
                 LOG.debug("Create SQL Default: {}",
                         createSql);
                 break;
             }
         }
 
         // Now add the primary key constraint
         createSql.append(", ");
         createSql.append(" PRIMARY KEY (");
         firstField = true;
         for (FieldInfo field : entity.key) {
             if (!firstField) {
                 createSql.append(", ");
             } else {
                 firstField = false;
             }
             createSql.append(
                     GenericContentProvider.sanitize(field.fieldName));
         }
         createSql.append(')');
 
         // Close the table
         createSql.append(")");
 
         if (LOG.isDebugEnabled()) {
             LOG.debug("Creating: "
                     + createSql.toString());
         }
         db.execSQL(createSql.toString());
 
         // Now process any remaining children
         for (EntityInfo child : children) {
             buildTables(db, child, built);
         }
 
         // Now fill in any enumeration values
         if (entity.enumValues != null) {
             ContentValues values = new ContentValues();
             for (Integer ordinal : entity.enumValues.keySet()) {
                 String value = entity.enumValues.get(ordinal);
                 values.clear();
                 values.put("_id", ordinal);
                 values.put("_value", value);
                 db.insert(GenericContentProvider.escapeName(mNamespace, entity),
                         "_id", values);
             }
         }
         built.put(entity.name(), entity.name());
     }
 
     @Override
     public final String getSchema() {
         return mSchema;
     }
 
     public void updateCopy(SQLiteDatabase updateDb, Metadata masterMetadata) {
         // Keep track of what has been copied as we go so as not to duplicate
         HashMap<String, String> built = new HashMap<String, String>();
 
         for (EntityInfo entity : mDbMetadata.getEntities()) {
             // Only handle root entities.
             // Children get recursed
             if (entity.parentEntity == null) {
                 updateCopyImpl(entity, updateDb,
                         masterMetadata, built);
             }
         }
     }
 
     private void updateCopyImpl(EntityInfo entity, SQLiteDatabase updateDb,
             Metadata masterMetadata,
             HashMap<String, String> built) {
         LOG.debug("Copying entity: {}", entity.name())
         ;
         if (built.containsKey(entity.name())) {
             LOG.debug("Already copied: {}", entity.name());
             return;
         }
 
         // An array to hold fields to copy
         ArrayList<String>copyFields = new ArrayList<String>();
 
         // Build up a hashMap of other fields
         HashMap<String, FieldInfo>otherFields =
                 new HashMap<String, FieldInfo>();
 
         EntityInfo other = masterMetadata.getEntity(entity.name());
 
         if (null != other) {
             for (FieldInfo otherField : other.getFields()) {
                 otherFields.put(otherField.fieldName, otherField);
             }
         }
 
         ArrayList<EntityInfo>children = new ArrayList<EntityInfo>();
 
         for (FieldInfo field : entity.getFields()) {
             switch (field.dbType) {
             case ONE_TO_MANY_INT:
             case ONE_TO_MANY_STRING:
                 // Skip these since they are handled by putting the
                 // key for this one in the targetEntity
                 // but queue the child to be handled when we are
                 // done with this table so we don't violate
                 // the up pointing references.
                 LOG.debug("Queueing Target Entity: ", field.targetEntity);
                 children.add(field.targetEntity);
                 break;
             case ONE_TO_ONE:
                 // These point down so first copy the child table.
                 // then do this table.
                 updateCopyImpl(field.targetEntity, updateDb,
                         masterMetadata, built);
                 break;
             default:
                 // Check if the field existed in the old metadata
                 // and if so then set it to be copied.
                 if (otherFields.containsKey(field.fieldName)) {
                     copyFields.add(field.fieldName);
                 }
                 break;
             }
         }
 
         // Prepare the INSERT copying SQL
         StringBuffer insertSql = new StringBuffer();
         insertSql.append("INSERT INTO ");
         insertSql.append(entity.name());
         insertSql.append(" VALUES (");
         StringBuffer selectSql = new StringBuffer();
         selectSql.append("SELECT ");
 
         boolean first = true;
         for (String field : copyFields) {
             if (!first) {
                 insertSql.append(", ");
                 selectSql.append(", ");
             } else {
                 first = false;
             }
             insertSql.append(field);
             selectSql.append(field);
         }
         insertSql.append(") ");
         selectSql.append(" FROM old.");
         selectSql.append(entity.name());
         insertSql.append(selectSql.toString());
 
         LOG.debug("Insert SQL: {}", insertSql.toString());
         updateDb.execSQL(insertSql.toString());
 
         // Now process any remaining children
         for (EntityInfo child : children) {
             updateCopyImpl(child, updateDb, masterMetadata, built);
         }
 
         built.put(entity.name(), entity.name());
     }
 }
