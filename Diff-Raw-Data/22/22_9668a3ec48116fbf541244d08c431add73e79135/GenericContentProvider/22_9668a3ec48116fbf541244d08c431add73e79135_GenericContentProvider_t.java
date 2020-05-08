 package interdroid.vdb.content;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map.Entry;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import interdroid.vdb.content.EntityUriMatcher.UriMatch;
 import interdroid.vdb.content.metadata.DatabaseFieldType;
 import interdroid.vdb.content.metadata.EntityInfo;
 import interdroid.vdb.content.metadata.FieldInfo;
 import interdroid.vdb.content.metadata.Metadata;
 import interdroid.vdb.persistence.api.VdbCheckout;
 import interdroid.vdb.persistence.api.VdbInitializer;
 import interdroid.vdb.persistence.api.VdbInitializerFactory;
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
 
 
 public abstract class GenericContentProvider extends ContentProvider implements VdbInitializerFactory {
 	private static final Logger logger = LoggerFactory.getLogger(GenericContentProvider.class);
 
 	public static final String SEPARATOR = "_";
 	public static final String PARENT_COLUMN_PREFIX = SEPARATOR + "parent";
 
 	protected final Metadata metadata_;
 	protected final String namespace_;
 	protected VdbRepository vdbRepo_;
 
 	// TODO: (nick) Support for multiple key tables?
 
 	private String escapeName(EntityInfo info) {
 		return escapeName(namespace_, info);
 	}
 
 	private static String escapeName(String namespace, EntityInfo info) {
 		return DatabaseUtils.sqlEscapeString(escapeName(namespace, info.namespace(), info.name()));
 	}
 
 	public static String escapeName(String defaultNamespace, String namespace, String name) {
 		// Don't include the namespace for entities that match our name for simplicity
 		if (defaultNamespace.equals(namespace)) {
 			return name.replace('.', '_');
 		} else {
 			// But entities in other namespaces we use the full namespace. This shouldn't happen often.
 			return namespace.replace('.', '_') + "_" + name.replace('.', '_');
 		}
 	}
 
 	public static class DatabaseInitializer implements VdbInitializer {
 		private final Metadata metadata_;
 		private final String namespace_;
 		private final String schema_;
 
 		public DatabaseInitializer(String namespace, Metadata metadata) {
 			metadata_ = metadata;
 			namespace_ = namespace;
 			schema_ = "";
 		}
 
 		public DatabaseInitializer(String namespace, Metadata metadata, String schema) {
 			metadata_ = metadata;
 			namespace_ = namespace;
 			schema_ = schema;
 		}
 
 		@Override
 		public void onCreate(SQLiteDatabase db)
 		{
 			if (logger.isDebugEnabled())
 				logger.debug("Initializing database for: " + namespace_);
 			// Keep track of what has been built as we go so as not to duplicate
 			HashMap<String, String> built = new HashMap<String, String>();
 			for (EntityInfo entity : metadata_.getEntities()) {
 				// Only handle root entities.
 				// Children get recursed so foreign key constraints all point up
 				if (entity.parentEntity == null) {
 					buildTables(db, entity, built);
 				}
 			}
 		}
 
 		private void buildTables(SQLiteDatabase db, EntityInfo entity, HashMap<String, String> built) {
 			boolean firstField = true;
 			ArrayList<EntityInfo>children = new ArrayList<EntityInfo>();
 			if (built.containsKey(entity.name())) {
 				logger.debug("Already built: {}", entity.name());
 				return;
 			}
 			if (logger.isDebugEnabled())
 				logger.debug("Creating table for: " + entity.namespace() + " : " + entity.name() + ":" + escapeName(namespace_, entity));
 			db.execSQL("DROP TABLE IF EXISTS " + escapeName(namespace_, entity));
 
 			StringBuilder createSql = new StringBuilder("CREATE TABLE ");
 			createSql.append(escapeName(namespace_, entity));
 			createSql.append('(');
 			for (FieldInfo field : entity.getFields()) {
 				switch (field.dbType) {
 				case ONE_TO_MANY_INT:
 				case ONE_TO_MANY_STRING:
 					// Skip these since they are handled by putting the key for this one in the targetEntity
 					// but queue the child to be handled when we are done with this table.
 					logger.debug("Queueing Target Entity: ", field.targetEntity);
 					children.add(field.targetEntity);
 					break;
 				case ONE_TO_ONE:
 					logger.debug("Building child table: ", field.targetEntity);
 					// First we need to build the child table so we can do the foreign key on this one
 					buildTables(db, field.targetEntity, built);
 
 					if (!firstField) {
 						createSql.append(",\n");
 					} else {
 						firstField = false;
 					}
 					createSql.append(DatabaseUtils.sqlEscapeString(field.fieldName));
 					createSql.append(' ');
 					createSql.append(DatabaseFieldType.INTEGER);
 					createSql.append(" REFERENCES ");
 					createSql.append(escapeName(namespace_, field.targetEntity));
 					createSql.append('(');
 					createSql.append(field.targetField.fieldName);
 					createSql.append(')');
 					createSql.append(" DEFERRABLE");
 					logger.debug("Create SQL now: {}", createSql);
 					break;
 				default:
 					if (!firstField) {
 						createSql.append(",\n");
 					} else {
 						firstField = false;
 					}
 					createSql.append(DatabaseUtils.sqlEscapeString(field.fieldName));
 					createSql.append(' ');
 					createSql.append(field.dbTypeName());
 					if (field.targetEntity != null) {
 						createSql.append(" REFERENCES ");
 						createSql.append(escapeName(namespace_, field.targetEntity));
 						createSql.append('(');
 						createSql.append(field.targetField.fieldName);
 						createSql.append(") DEFERRABLE");
 					}
 					logger.debug("Create SQL Default: {}", createSql);
 					break;
 				}
 			}
 
 			// Now add the primary key constraint
 			createSql.append(", ");
 			createSql.append(" PRIMARY KEY (");
 			firstField = true;
 			for(FieldInfo field : entity.key) {
 				if (!firstField) {
 					createSql.append(", ");
 				} else {
 					firstField = false;
 				}
 				createSql.append(field.fieldName);
 			}
 			createSql.append(')');
 
 			// Close the table
 			createSql.append(")");
 
 			if (logger.isDebugEnabled())
 				logger.debug("Creating: " + createSql.toString());
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
 					db.insert(escapeName(namespace_, entity), "_id", values);
 				}
 			}
 			built.put(entity.name(), entity.name());
 		}
 
 		@Override
 		public String getSchema() {
 			return schema_;
 		}
 	}
 
 	public VdbInitializer buildInitializer() {
 		logger.debug("Building initializer.");
 		return new DatabaseInitializer(namespace_, metadata_);
 	}
 
 	@Override
 	public boolean onCreate() {
 
 		return true;
 	}
 
 	public void attachInfo(Context context, ProviderInfo info) {
 		super.attachInfo(context, info);
 		if (logger.isDebugEnabled())
 			logger.debug("attachInfo for: " + namespace_);
 		try {
 			vdbRepo_ = VdbRepositoryRegistry.getInstance().getRepository(context, namespace_);
 		} catch (IOException e) {
 			logger.error("Unable to get repository for: " + namespace_, e);
 		}
 		if (vdbRepo_ == null) {
 			logger.debug("registering repository");
 			try {
 				vdbRepo_ = VdbRepositoryRegistry.getInstance().addRepository(context, namespace_, buildInitializer());
 			} catch (IOException e) {
 				throw new RuntimeException("Error initializing repository", e);
 			}
 		}
 		logger.debug("Fetched repository.");
 	}
 
 	public GenericContentProvider(String namespace, Metadata metadata)
 	{
 		namespace_ = namespace;
 		metadata_ = metadata;
 	}
 
 	@Override
 	public String getType(Uri uri)
 	{
 		if (logger.isDebugEnabled())
 			logger.debug("Getting type of: " + uri);
 		final UriMatch result = EntityUriMatcher.getMatch(uri);
 		final EntityInfo info = metadata_.getEntity(result);
 		if (logger.isDebugEnabled())
 			logger.debug("Got entity: " + info);
 		return info != null ? info.itemContentType()
 				: null;
 	}
 
 	private VdbCheckout getCheckoutFor(Uri uri, UriMatch result) {
 		if (logger.isDebugEnabled())
 			logger.debug("Getting checkout for: " + uri);
 		try {
 			switch(result.type) {
 			case LOCAL_BRANCH:
 				return vdbRepo_.getBranch(result.reference);
 			case COMMIT:
 				return vdbRepo_.getCommit(result.reference);
 			case REMOTE_BRANCH:
 				return vdbRepo_.getRemoteBranch(result.reference);
 			default:
 				throw new RuntimeException("Unsupported uri type " + uri);
 			}
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	@Override
 	public Uri insert(Uri uri, ContentValues userValues)
 	{
 		if (logger.isDebugEnabled())
 			logger.debug("Inserting into: " + uri);
 		final UriMatch result = EntityUriMatcher.getMatch(uri);
 		if (result.entityIdentifier != null) { /* don't accept ID queries */
 			throw new IllegalArgumentException("Invalid item URI " + uri);
 		}
 		if (logger.isDebugEnabled())
 			logger.debug("Getting entity: " + result.entityName);
 		if (result.entityName == null) {
 			throw new RuntimeException("Uri does not specify an entity: " + result.entityName);
 		}
 		final EntityInfo entityInfo = metadata_.getEntity(result);
 		if (entityInfo == null) {
 			throw new RuntimeException("Unable to find entity for: " + result.entityName);
 		}
 		if (logger.isDebugEnabled()) {
 			logger.debug("Got info: " + entityInfo.name());
 			logger.debug("Getting checkout for: " + uri);
 		}
 		VdbCheckout vdbBranch = getCheckoutFor(uri, result);
 
 		ContentValues values;
 		if (userValues != null) {
 			values = sanitize(userValues);
 		} else {
 			values = new ContentValues();
 		}
 
 		// Propogate the change to the preInsertHook if there is one
 		ContentChangeHandler handler = ContentChangeHandler.getHandler(entityInfo.namespace(), entityInfo.name());
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
 			if (logger.isDebugEnabled())
 				logger.debug("Inserting: " + entityInfo.name() + " : " + entityInfo.key.get(0).fieldName + ":" + values.getAsString(entityInfo.key.get(0).fieldName) + " : " + values.size());
 			// Do we need to include the parent identifier?
 			if (entityInfo.parentEntity != null && result.parentEntityIdentifiers != null) {
 				if (logger.isDebugEnabled())
 					logger.debug("Adding parent id: " + entityInfo.parentEntity.key.get(0).fieldName + ":" + result.parentEntityIdentifiers.get(result.parentEntityIdentifiers.size() - 1));
 				values.put(PARENT_COLUMN_PREFIX + entityInfo.parentEntity.key.get(0).fieldName, result.parentEntityIdentifiers.get(result.parentEntityIdentifiers.size() - 1));
 			}
 			long rowId = db.insert(escapeName(entityInfo), entityInfo.key.get(0).fieldName, values);
 			if (rowId > 0) {
 				Uri noteUri = ContentUris.withAppendedId(uri, rowId);
 				getContext().getContentResolver().notifyChange(noteUri, null);
 				return noteUri;
 			}
 
 			throw new SQLException("Failed to insert row into " + uri);
 		} finally {
 			vdbBranch.releaseDatabase();
 		}
 
 	}
 
 	@Override
 	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
 			String sortOrder)
 	{
 		if (logger.isDebugEnabled())
 			logger.debug("Querying for: " + uri);
 		// Validate the requested uri
 		final UriMatch result = EntityUriMatcher.getMatch(uri);
 		logger.debug("Query for: {} {}", result.entityName, getClass().getCanonicalName());
 		final EntityInfo entityInfo = metadata_.getEntity(result);
 		if (entityInfo == null) {
 			throw new RuntimeException("Unable to find entity for: " + result.entityName);
 		}
 		VdbCheckout vdbBranch = getCheckoutFor(uri, result);
 
 		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
 
 		if (result.entityIdentifier != null) {
 			qb.setTables(escapeName(entityInfo));
 			// qb.setProjectionMap(sNotesProjectionMap);
 			qb.appendWhere(entityInfo.key.get(0).fieldName + "=" + result.entityIdentifier);
 		} else {
 			qb.setTables(escapeName(entityInfo));
 		}
 
 		// Append ID of parent if required
 		if (hasParent(result, entityInfo)) {
 			qb.appendWhere(PARENT_COLUMN_PREFIX  + entityInfo.parentEntity.key.get(0).fieldName + "=" + result.parentEntityIdentifiers.get(result.parentEntityIdentifiers.size() - 1));
 		}
 
 		// Get the database and run the query
 		SQLiteDatabase db;
 		try {
 			db = vdbBranch.getReadOnlyDatabase();
 		} catch (IOException e) {
 			throw new RuntimeException("getReadOnlyDatabase failed", e);
 		}
 
 		logger.debug("Got database: {}", db);
 
 		// TODO(emilian): default sort order
 
 		try {
 			if (logger.isDebugEnabled())
 				logger.debug("Querying with: " + qb.buildQuery(projection, selection, selectionArgs, null, null, sortOrder, null));
 			Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
 			logger.debug("Got cursor: {}", c);
 			if (c != null && getContext() != null) {
 				// Tell the cursor what uri to watch, so it knows when its source data changes
 				c.setNotificationUri(getContext().getContentResolver(), uri);
 			}
 			logger.debug("Returning cursor.");
 			return c;
 		} finally {
 			vdbBranch.releaseDatabase();
 		}
 	}
 
	private boolean hasParent(final UriMatch result, final EntityInfo entityInfo) {
		return result.parentEntityIdentifiers != null && entityInfo.parentEntity != null;
	}
 
 	@Override
 	public int update(Uri uri, ContentValues values, String where, String[] whereArgs)
 	{
 		if (logger.isDebugEnabled())
 			logger.debug("Updating: " + uri);
 		// Validate the requested uri
 		final UriMatch result = EntityUriMatcher.getMatch(uri);
 		final EntityInfo entityInfo = metadata_.getEntity(result);
 
 		if (entityInfo == null) {
 			throw new RuntimeException("Unable to find entity for: " + uri);
 		}
 
 		VdbCheckout vdbBranch = getCheckoutFor(uri, result);
 
 		SQLiteDatabase db;
 		try {
 			db = vdbBranch.getReadWriteDatabase();
 		} catch (IOException e) {
 			throw new RuntimeException("getReadWriteDatabase failed", e);
 		}
 
 		int count = 0;
 		try {
 			values = sanitize(values);
 			count = db.update(escapeName(entityInfo), values,
 					prepareWhereClause(where, result, entityInfo),
 					prepareWhereArgs(whereArgs, result, entityInfo));
 		} finally {
 			vdbBranch.releaseDatabase();
 		}
 
 		/*
 		TODO(emilian) implement auto commit support
 		try {
 			vdbBranch.commitBranch();
 		} catch(IOException e) {
 			Log.v(GenericContentProvider.class.getSimpleName(), e.toString());
 		}
 		 */
 
 		getContext().getContentResolver().notifyChange(uri, null);
 		logger.debug("Updated: {}", count);
 		return count;
 
 	}
 
 	private String prepareWhereClause(String where, final UriMatch result,
 			final EntityInfo entityInfo) {
 		boolean hasParentId = hasParent(result, entityInfo);
 		boolean hasEntityId = result.entityIdentifier != null;
 		boolean hasWhere =  !TextUtils.isEmpty(where);
 		String ret =
				(hasParentId ? PARENT_COLUMN_PREFIX + entityInfo.parentEntity.key.get(0).fieldName + "=?" : "") +
				(hasParentId && hasEntityId ? " AND " : "") +
				(hasEntityId ? entityInfo.key.get(0).fieldName + "=?" : "") +
				(hasWhere ?
						(hasEntityId || hasParentId ? " AND (" + where + ')' : where )
						: "");
 		return ret;
 	}
 
 	private String[] prepareWhereArgs(String[] whereArgs,
 			final UriMatch result, final EntityInfo entityInfo) {
 		boolean hasParentId = hasParent(result, entityInfo);
 		boolean hasEntityId = result.entityIdentifier != null;
 
 		if (hasParentId && logger.isDebugEnabled()) {
 			logger.debug("Adding parent id to query: {} {}",
 					PARENT_COLUMN_PREFIX  + entityInfo.parentEntity.key.get(0).fieldName,
 					result.parentEntityIdentifiers.get(result.parentEntityIdentifiers.size() - 1));
 		}
 		if (whereArgs != null) {
 			if (hasParentId || hasEntityId) {
 				// One or two more args depending on if we have a parent
 				int newLength = whereArgs.length + (hasParentId ? 1 : 0) + (hasEntityId ? 1 : 0);
 				String[] temp = new String[newLength];
 				System.arraycopy(whereArgs, 0, temp, 0, whereArgs.length);
 				// Append ID of entity if required
 				if (hasEntityId) {
 					temp[temp.length - 1] = result.entityIdentifier;
 				}
 				// Append ID of parent if required
 				if (hasParentId) {
 					temp[temp.length - (hasEntityId ? 2 : 1)] = result.parentEntityIdentifiers.get(result.parentEntityIdentifiers.size() - 1);
 				}
 			}
 		} else {
 			if (hasParentId && hasEntityId) {
 				whereArgs =  new String[] {result.parentEntityIdentifiers.get(result.parentEntityIdentifiers.size() - 1), result.entityIdentifier};
 			} else if (!hasParentId && hasEntityId) {
 				whereArgs =  new String[] {result.entityIdentifier};
 			} else if (hasParentId && !hasEntityId) {
 				whereArgs =  new String[] {result.parentEntityIdentifiers.get(result.parentEntityIdentifiers.size() - 1)};
 			}
 		}
 		return whereArgs;
 	}
 
 	// This sucks. Android does not quote value identifiers properly.
 	private ContentValues sanitize(ContentValues values) {
 		ContentValues cleanValues = new ContentValues();
 		for (Entry<String, Object> val : values.valueSet()) {
 			Object value = val.getValue();
 			String cleanName = val.getKey().charAt(0) == '\'' ? val.getKey() : "'" + val.getKey() + "'";
 			// This really sucks. There is no generic put an object....
 			if (value == null) {
 				cleanValues.putNull(cleanName);
 			} else if (value instanceof Boolean) {
 				cleanValues.put(cleanName, (Boolean)value);
 			} else if (value instanceof Byte) {
 				cleanValues.put(cleanName, (Byte)value);
 			} else if (value instanceof byte[] ) {
 				cleanValues.put(cleanName, (byte[])value);
 			} else if (value instanceof Double ) {
 				cleanValues.put(cleanName, (Double)value);
 			} else if (value instanceof Float ) {
 				cleanValues.put(cleanName, (Float)value);
 			} else if (value instanceof Integer ) {
 				cleanValues.put(cleanName, (Integer)value);
			}else if (value instanceof Long ) {
				cleanValues.put(cleanName, (Long)value);
 			} else if (value instanceof Short ) {
 				cleanValues.put(cleanName, (Short)value);
 			} else if (value instanceof String ) {
 				cleanValues.put(cleanName, (String)value);
 			} else {
 				throw new RuntimeException("Don't know how to add value of type: " + value.getClass().getCanonicalName());
 			}
 		}
 		return cleanValues;
 	}
 
 	@Override
 	public int delete(Uri uri, String where, String[] whereArgs)
 	{
 		if (logger.isDebugEnabled())
 			logger.debug("Delete Uri: " + uri);
 		// Validate the requested uri
 		final UriMatch result = EntityUriMatcher.getMatch(uri);
 		final EntityInfo entityInfo = metadata_.getEntity(result);
 		if (entityInfo == null) {
 			throw new RuntimeException("Unable to find entity for: " + result.entityName);
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
 }
