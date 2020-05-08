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
 
 import org.apache.avro.Schema;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.pm.ProviderInfo;
 import android.database.Cursor;
 import android.net.Uri;
 
 public class AvroProviderRegistry extends ORMGenericContentProvider {
 	private static final Logger logger = LoggerFactory.getLogger(AvroProviderRegistry.class);
 
 	@DbEntity(name=AvroSchemaRegistrationHandler.NAME,
			itemContentType = "vnd.android.cursor.item/"
	+ AvroSchemaRegistrationHandler.FULL_NAME,
			contentType = "vnd.android.cursor.dir/"
	+ AvroSchemaRegistrationHandler.FULL_NAME)
 	public static class RegistryConf {
 		// Don't allow instantiation
 		private RegistryConf() {}
 
 		/**
 		 * The default sort order for this table
 		 */
 		public static final String DEFAULT_SORT_ORDER = "modified DESC";
 
 		public static final Uri CONTENT_URI =
			Uri.withAppendedPath(EntityUriBuilder.branchUri(
					Authority.VDB, AvroSchemaRegistrationHandler.NAMESPACE,
					"master"), AvroSchemaRegistrationHandler.NAME);
 
 		@DbField(isID=true, dbType=DatabaseFieldType.INTEGER)
 		public static final String _ID = "_id";
 
 		@DbField(dbType=DatabaseFieldType.TEXT)
		public static final String NAME =
			AvroSchemaRegistrationHandler.KEY_NAME;
 
 		@DbField(dbType=DatabaseFieldType.TEXT)
		public static final String NAMESPACE =
			AvroSchemaRegistrationHandler.KEY_NAMESPACE;
 
 		@DbField(dbType=DatabaseFieldType.TEXT)
		public static final String SCHEMA =
			AvroSchemaRegistrationHandler.KEY_SCHEMA;
 
 	}
 
 	private Context mContext;
 
 	public AvroProviderRegistry() {
 		super(AvroSchemaRegistrationHandler.NAMESPACE, RegistryConf.class);
 	}
 
 	public List<RepositoryConf> getAllRepositories() {
 		Cursor c = null;
 		ArrayList<RepositoryConf> result = new ArrayList<RepositoryConf>();
 		try {
 			c = query(AvroSchemaRegistrationHandler.URI, new String[]{AvroSchemaRegistrationHandler.KEY_NAMESPACE, AvroSchemaRegistrationHandler.KEY_SCHEMA}, null, null, null);
 			if (c != null) {
 				int namespaceIndex = c.getColumnIndex(AvroSchemaRegistrationHandler.KEY_NAMESPACE);
 				int schemaIndex = c.getColumnIndex(AvroSchemaRegistrationHandler.KEY_SCHEMA);
 				while (c.moveToNext()) {
 					result.add(new RepositoryConf(c.getString(namespaceIndex), c.getString(schemaIndex)));
 				}
 			}
 		} finally {
 			if (c != null) {
 				try {
 					c.close();
 				} catch (Exception e) {
 					logger.warn("Exception while closing cursor: ", e);
 				}
 			}
 		}
 
 		return result;
 	}
 
 	@Override
 	public void attachInfo(Context context, ProviderInfo info) {
 		super.attachInfo(context, info);
 		mContext = context;
 	}
 
 	@Override
 	public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
 		int ret = super.update(uri, values, where, whereArgs);
 		migrateDb(values.getAsString(AvroSchemaRegistrationHandler.KEY_SCHEMA));
 		return ret;
 	}
 
 	@Override
 	public Uri insert(Uri uri, ContentValues userValues)
 	{
 		Uri result = super.insert(uri, userValues);
 
 
 
 		VdbProviderRegistry registry;
 		try {
 			registry = new VdbProviderRegistry(mContext);
 			registry.registerRepository(
 					new RepositoryConf(
 							userValues.getAsString(
 								AvroSchemaRegistrationHandler.KEY_NAMESPACE),
 							userValues.getAsString(
 								AvroSchemaRegistrationHandler.KEY_SCHEMA)));
 		} catch (IOException e) {
 			throw new RuntimeException("Unable to build registry: ", e);
 		}
 
 		return result;
 	}
 
 	private void migrateDb(String schemaString) {
 		// TODO: Finish implemnting this
 		Cursor c = null;
 		Schema schema = Schema.parse(schemaString);
 		try {
 			c = getContext().getContentResolver().query(
 					EntityUriBuilder.branchUri(Authority.VDB, schema.getNamespace(),
 							"master/" + schema.getName()), new String[] {"_id"}, null, null, null);
 		} finally {
 			try {
 				if (c != null) {
 					c.close();
 				}
 			} catch (Exception e) {
 				logger.error("Caught excption closing cursor", e);
 			}
 		}
 	}
 
 	public static Schema getSchema(Context context, Uri uri) {
 		Cursor c = null;
 		Schema schema = null;
 		try {
 			// We expect to deal with internal paths
 			if (!Authority.VDB.equals(uri.getAuthority())) {
 				logger.debug("Mapping to native: {}", uri);
 				uri = EntityUriBuilder.toInternal(uri);
 			}
 			logger.debug("Querying for schema for: {} {}", uri, uri.getPathSegments().get(0));
 			c = context.getContentResolver().query(AvroSchemaRegistrationHandler.URI, new String[]{AvroSchemaRegistrationHandler.KEY_SCHEMA}, AvroSchemaRegistrationHandler.KEY_NAMESPACE + "=?", new String[] {uri.getPathSegments().get(0)}, null);
 			if (c != null && c.moveToFirst()) {
 				int schemaIndex = c.getColumnIndex(AvroSchemaRegistrationHandler.KEY_SCHEMA);
 				String schemaString = c.getString(schemaIndex);
 				logger.debug("Got schema: {}", schemaString);
 				schema = Schema.parse(schemaString);
 			} else {
 				logger.error("Schema not found.");
 			}
 		} finally {
 			if (c != null) {
 				try {
 					c.close();
 				} catch (Exception e) {
 					logger.warn("Exception while closing cursor: ", e);
 				}
 			}
 		}
 		return schema;
 	}
 }
