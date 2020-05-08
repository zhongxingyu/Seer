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
 package interdroid.vdb.content.avro;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import interdroid.vdb.Authority;
 import interdroid.vdb.content.EntityUriBuilder;
 import interdroid.vdb.content.VdbConfig.RepositoryConf;
 import interdroid.vdb.content.VdbProviderRegistry;
 import interdroid.vdb.content.metadata.DatabaseFieldType;
 import interdroid.vdb.content.orm.DbEntity;
 import interdroid.vdb.content.orm.DbField;
 import interdroid.vdb.content.orm.ORMGenericContentProvider;
 import interdroid.vdb.persistence.api.VdbRepository;
 import interdroid.vdb.persistence.api.VdbRepositoryRegistry;
 
 import org.apache.avro.Schema;
 import org.eclipse.jgit.lib.Constants;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.pm.ProviderInfo;
 import android.database.Cursor;
 import android.net.Uri;
 
 /**
  * This class holds a registry for avro content providers
  * holding the namespace, name and schema for each avro provider.
  *
  * It is a content provider itself, constructed using the ORM
  * system within VDB. Nothing like bootstrapping the system using
  * your own dog food. ;)
  *
  * @author nick &lt;palmer@cs.vu.nl&gt;
  *
  */
 public class AvroProviderRegistry extends ORMGenericContentProvider {
     /**
      * Access to logger.
      */
     private static final Logger LOG = LoggerFactory
             .getLogger(AvroProviderRegistry.class);
 
     /**
      * The registry configuration.
      * @author nick &lt;palmer@cs.vu.nl&gt;
      *
      */
     @DbEntity(name = AvroSchemaRegistrationHandler.NAME,
             itemContentType = "vnd.android.cursor.item/"
                     + AvroSchemaRegistrationHandler.FULL_NAME,
                     contentType = "vnd.android.cursor.dir/"
                             + AvroSchemaRegistrationHandler.FULL_NAME)
     public static final class RegistryConf {
         /**
          * No construction.
          */
         private RegistryConf() { }
 
         /**
          * The default sort order for this table.
          */
         public static final String DEFAULT_SORT_ORDER = "modified DESC";
 
         /**
          * The content URI for this type.
          */
         public static final Uri CONTENT_URI =
                 Uri.withAppendedPath(EntityUriBuilder.branchUri(
                         Authority.VDB, AvroSchemaRegistrationHandler.NAMESPACE,
                         "master"), AvroSchemaRegistrationHandler.NAME);
 
         /**
          * The ID field.
          */
         @DbField(isID = true, dbType = DatabaseFieldType.INTEGER)
         public static final String ID = "_id";
 
         /**
          * The key field.
          */
         @DbField(dbType = DatabaseFieldType.TEXT)
         public static final String NAME =
         AvroSchemaRegistrationHandler.KEY_NAME;
 
         /**
          * The namespace field.
          */
         @DbField(dbType = DatabaseFieldType.TEXT)
         public static final String NAMESPACE =
         AvroSchemaRegistrationHandler.KEY_NAMESPACE;
 
         /**
          * The schema field.
          */
         @DbField(dbType = DatabaseFieldType.TEXT)
         public static final String SCHEMA =
         AvroSchemaRegistrationHandler.KEY_SCHEMA;
 
     }
 
     /**
      * The context we are working in.
      */
     private Context mContext;
 
     /**
      * Construct a provider registry.
      */
     public AvroProviderRegistry() {
         super(AvroSchemaRegistrationHandler.NAMESPACE, RegistryConf.class);
     }
 
     /**
      * Returns all repositories registered with the system.
      * @return the list of repository configurations.
      */
     public final List<RepositoryConf> getAllRepositories() {
         Cursor c = null;
         ArrayList<RepositoryConf> result = new ArrayList<RepositoryConf>();
         try {
             c = query(AvroSchemaRegistrationHandler.URI,
                     new String[]{AvroSchemaRegistrationHandler.KEY_NAMESPACE,
                     AvroSchemaRegistrationHandler.KEY_SCHEMA},
                     null, null, null);
             if (c != null) {
                 int namespaceIndex = c.getColumnIndex(
                         AvroSchemaRegistrationHandler.KEY_NAMESPACE);
                 int schemaIndex = c.getColumnIndex(
                         AvroSchemaRegistrationHandler.KEY_SCHEMA);
                 while (c.moveToNext()) {
                     result.add(new RepositoryConf(
                             c.getString(namespaceIndex),
                             c.getString(schemaIndex)));
                 }
             }
         } finally {
             if (c != null) {
                 try {
                     c.close();
                 } catch (Exception e) {
                     LOG.warn("Exception while closing cursor: ", e);
                 }
             }
         }
 
         return result;
     }
 
     @Override
     public final void onAttach(final Context context, final ProviderInfo info) {
         super.attachInfo(context, info);
         mContext = context;
     }
 
     @Override
     public final void onPostUpdate(final Uri uri, final ContentValues values,
             final String where, final String[] whereArgs) {
         try {
            LOG.debug("Migrating DB.");
             migrateDb(
                    values.getAsString(
                            AvroSchemaRegistrationHandler.KEY_NAMESPACE),
                     values.getAsString(AvroSchemaRegistrationHandler.KEY_SCHEMA));
         } catch (IOException e) {
             throw new RuntimeException("Error updating database.");
         }
     }
 
     @Override
     public final void onPostInsert(final Uri uri,
             final ContentValues userValues) {
         LOG.debug("On post insert: {}", userValues);
        registerRepository(
                userValues.getAsString(
                        AvroSchemaRegistrationHandler.KEY_NAMESPACE),
                        userValues.getAsString(
                                AvroSchemaRegistrationHandler.KEY_SCHEMA));
     }
 
     @Override
     public final void onPostDelete(final Uri uri,
             final String where, final String[] whereArgs) {
         LOG.debug("Deleted avro repo: {}", whereArgs[0]);
         VdbRepositoryRegistry.getInstance().deleteRepository(getContext(),
                 whereArgs[0]);
         try {
			new VdbProviderRegistry(getContext()).unregister(whereArgs[0]);
		} catch (IOException e) {
			// TODO: Auto-generated catch block
			e.printStackTrace();
		}
     }
 
 
     private void registerRepository(final String namespace, final String schema) {
         VdbProviderRegistry registry;
         try {
             LOG.debug("Registering avro repo: {} {}", namespace, schema);
             registry = new VdbProviderRegistry(mContext);
             registry.registerRepository(
                     new RepositoryConf(namespace, schema));
         } catch (IOException e) {
             throw new RuntimeException("Unable to build registry: ", e);
         }
     }
 
     /**
      * Migrates a database from one schema to the new schema.
      * @param namespace the namespace for the repository
      * @param schemaString the new schema as a string.
      * @throws IOException
      */
     private void migrateDb(final String namespace, final String schemaString)
             throws IOException {
         // Make sure the repo is registered.
         registerRepository(namespace, schemaString);
 
         // Parse the schema
         Schema newSchema = Schema.parse(schemaString);
 
         VdbRepository repo = VdbRepositoryRegistry.getInstance()
                 .getRepository(getContext(), namespace);
 
         repo.updateDatabase(Constants.MASTER, newSchema);
     }
 
     /**
      * Queries for the schema for the given URI.
      *
      * @param context the context to query in
      * @param uri the uri to retrieve the schema for
      * @return the schema for the given uri
      */
     public static Schema getSchema(final Context context, final Uri uri) {
         Cursor c = null;
         Schema schema = null;
         Uri dbUri = uri;
         try {
             // We expect to deal with internal paths
             if (!Authority.VDB.equals(uri.getAuthority())) {
                 LOG.debug("Mapping to native: {}", uri);
                 dbUri = EntityUriBuilder.toInternal(uri);
             }
             LOG.debug("Querying for schema for: {} {}", dbUri,
                     dbUri.getPathSegments().get(0));
             c = context.getContentResolver().query(
                     AvroSchemaRegistrationHandler.URI,
                     new String[]{AvroSchemaRegistrationHandler.KEY_SCHEMA},
                     AvroSchemaRegistrationHandler.KEY_NAMESPACE + "=?",
                     new String[] {dbUri.getPathSegments().get(0)}, null);
             if (c != null && c.moveToFirst()) {
                 int schemaIndex = c.getColumnIndex(
                         AvroSchemaRegistrationHandler.KEY_SCHEMA);
                 String schemaString = c.getString(schemaIndex);
                 LOG.debug("Got schema: {}", schemaString);
                 schema = Schema.parse(schemaString);
             } else {
                 LOG.error("Schema not found.");
             }
         } finally {
             if (c != null) {
                 try {
                     c.close();
                 } catch (Exception e) {
                     LOG.warn("Exception while closing cursor: ", e);
                 }
             }
         }
         return schema;
     }
 }
