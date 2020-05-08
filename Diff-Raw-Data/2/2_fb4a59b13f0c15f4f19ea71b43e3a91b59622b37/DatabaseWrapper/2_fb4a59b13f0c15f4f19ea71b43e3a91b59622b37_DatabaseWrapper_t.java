 package collector.desktop.database;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URI;
 import java.sql.Connection;
 import java.sql.DatabaseMetaData;
 import java.sql.Date;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.Time;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.UUID;
 
 import org.jdbcdslog.ConnectionLoggingProxy;
 
 import collector.desktop.filesystem.FileSystemAccessWrapper;
 import collector.desktop.gui.tobemoved.QueryBuilder;
 import collector.desktop.gui.tobemoved.QueryBuilder.QueryComponent;
 import collector.desktop.gui.tobemoved.QueryBuilder.QueryOperator;
 
 public class DatabaseWrapper  {
 	/** The final name of the picture column. Currently only a single column is supported, this is its name.*/
 	public static final String PICTURE_COLUMN_NAME = "collectorPicture";
 	/** Suffix used to append to the mame of the main table to obtain the index name during index creation.*/
 	private static final String INDEX_NAME_SUFFIX = "_index";
 	/** The internal separator used in-between  the different picture names in the picture column.*/
 	private static final String PICTURE_STRING_SEPERATOR = "<!collector!>";
 	/** The suffix used to append to the main table name to obtain the typeInfo table name.*/
 	private static final String TYPE_INFO_SUFFIX = "_typeinfo";
 	/** The suffix used to append to the main table to obtain the temporary table name.*/
 	private static final String TEMP_TABLE_SUFFIX = "_tempTable";
 	/** The final foreign key of all main table entries to their type information entry.*/
 	private static final int TYPE_INFO_FOREIGN_KEY = 1;
 	/** The final name of the column containing the type information foreign key in the main table.*/
 	protected static final String TYPE_INFO_COLUMN_NAME = "typeinfo";
 	/** The final name of the schema version column. Updated at each structural change of an album.*/
 	protected static final String SCHEMA_VERSION_COLUMN_NAME = "schemaVersion";
 	/** The final name of the content version column. Updated at each change of the content of the field.*/
 	protected static final String CONTENT_VERSION_COLUMN_NAME = "contentVersion";
 	/** The name of the album master table containing all stored album table names and their type table names*/
 	private static final String albumMasterTableName = "albumMasterTable";
 	/** The column name for the album table. */
 	private static final String ALBUM_TABLENAME_IN_ALBUM_MASTER_TABLE= "albumTableName";
 	/** The column name for the album type table. */
 	private static final String TYPE_TABLENAME_ALBUM_MASTER_TABLE = "albumTypeTableName";
 	public static final String ID_COLUMN_NAME = "id";
 	private static long lastChangeTimeStamp = -1;
 	private static String sqliteConnectionString = "jdbc:sqlite:";
 	private static Connection connection = null;
 	/** The extension used on file names for autosaves.*/
 	private static final String autoSaveExtension = "autosave";
 	/** Regular expression describing the file name format including the extension of auto saves.*/
 	public static final String autoSaveFileRegex = "(\\w)+(\\u005F([0-9]+)+\\."+autoSaveExtension+")$";
 	private static final String corruptDatabaseSnapshotPrefix = ".corruptDatabaseSnapshot_";
 	/** The maximum amount of autosaves that can be stored until the oldes it overwritten */
 	private static int autoSaveLimit = 8;
 
 	/**
 	 * Opens the default connection for the FileSystemAccessWrapper.DATABASE database. Only opens a new connection if none is currently open.
 	 * @return True when a connection to the program database file is established, false if the connection is corrupt or not established.
 	 */
 	public static boolean openConnection() {
 		boolean result = false;
 		try {
 			if (connection == null || connection.isClosed()) {
 				connection = DriverManager.getConnection(sqliteConnectionString + FileSystemAccessWrapper.DATABASE);
 				connection =  ConnectionLoggingProxy.wrap(connection);
 				
 				if (enableForeignKeySupportForCurrentSession() == false) {
 					return false;
 				}
 				result  = true;
 			}
 			// Create the album master table if it is not present 
 			createAlbumMasterTableIfNotExits();
 			
 			// Run a query to check if db is ok
 			result = isConnectionReady();
 		} catch (SQLException e) {
 			System.err.println(e.getMessage());			
 			return false;
 		} 
 		return result;
 	}
 
 	public static boolean closeConnection() {
 		boolean result = true;
 		try {
 			if (connection != null) {
 				connection.close();
 			}
 		} catch (SQLException e) {
 			System.err.println(e.getMessage());
 			result = false;
 		}
 		return result;
 	}
 	
 	/**
 	 * Test if the connection is open and ready to be used. 
 	 * @return True if the connection can be safely used to query and manipulate the database.
 	 */
 	public static boolean isConnectionReady() {
 		//TODO do some logging when this method fails.
 		try {
 			if (connection == null || connection.isClosed()) {
 				return false;
 			}
 		} catch (SQLException e1) {
 			return false;
 		}
 		
 		/*
 		 *  Querying all albums should be successful on all working dbs,
 		 *  independently of stored albums.
 		 */
 					
 		List<String> albums = listAllAlbums();
 		if (albums == null) {
 			return  false;
 		}
 		
 		return true;
 	}
 	
 	/**
 	 * This method can be used when the database connection cannot be opened (e.g. corrupt db file).
 	 * I saves the collector home for manual inspection, then clears the whole  collector home including the db
 	 * and opens a connection to a blank db.
 	 * @return True if a connection is available, false if an error occurred in the process.
 	 */
 	public static boolean openCleanConnection() {
 		if (isConnectionReady()) {
 			return true;
 		}
 		
 		if (!closeConnection()) {
 			return false;
 		}				
 		
 		String corruptSnapshotFileName = corruptDatabaseSnapshotPrefix + System.currentTimeMillis();
 		File corruptTemporarySnapshotFile = new File(FileSystemAccessWrapper.USER_HOME + File.separator + corruptSnapshotFileName);
 		corruptTemporarySnapshotFile.deleteOnExit();
 		// Copy file to temporary location
 		try {
 			FileSystemAccessWrapper.copyFile(new File(FileSystemAccessWrapper.DATABASE), corruptTemporarySnapshotFile);
 		} catch (IOException e1) {
 			//TODO log failure
 			return false;
 		}
 		
 		FileSystemAccessWrapper.removeCollectorHome();
 		// Copy the corrupt snapshot from the temporary location into the app data folder 
 		String corruptSnapshotFilePath = FileSystemAccessWrapper.COLLECTOR_HOME_APPDATA + File.separator + corruptSnapshotFileName;
 		File corruptSnapshotFile = new File(corruptSnapshotFilePath);			
 		try {
 			FileSystemAccessWrapper.copyFile(corruptTemporarySnapshotFile, corruptSnapshotFile);
 		} catch (IOException e) {
 			e.printStackTrace();
 			return false;
 		}
 		
 		if (!openConnection()) {
 			return false;
 		}
 		
 		return FileSystemAccessWrapper.updateCollectorFileStructure();
 	}
 
 	/**
 	 * Gets the connection.
 	 * @return A valid connection or null if not properly initialized.
 	 */
 	public static Connection getConnection() {
 		return connection;
 	}
 
 	private static boolean enableForeignKeySupportForCurrentSession() {
 		boolean success = true;
 		PreparedStatement preparedStatement = null;
 		try {
 			preparedStatement = connection.prepareStatement("PRAGMA foreign_keys = ON");
 			preparedStatement.executeUpdate();
 
 		} catch (SQLException e) {
 			success = false;
 		} finally {
 			try {
 				if (preparedStatement != null) {
 					preparedStatement.close();
 				}
 			} catch (SQLException e) {
 				success = false;
 			}	
 		}
 		return success;
 	}
 
 	/**
 	 * Creates a new album according to the specified properties.
 	 * @param albumName The name of the album to be created.
 	 * @param fields The metadata fields describing the fields of the new album. Pass an empty list as argument
 	 * when creating an album with no fields.  
 	 * @param hasAlbumPictureField When set to true creates a single picture field in the album.
 	 * @return True if the creation album succeeded. False otherwise.
 	 */
 	public static boolean createNewAlbum(String albumName, List<MetaItemField> fields, boolean hasAlbumPictureField) {
 		if (fields == null || !albumNameIsAvailable(albumName)) {
 			return false;
 		}
 		boolean previousAutoCommitState = false;
 		try {
 			previousAutoCommitState = connection.getAutoCommit();
 		} catch (SQLException e2) {
 			System.err.println("Could not save the autocommit state before album creation");
 		}
 
 		try {
 			connection.setAutoCommit(false);
 		} catch (SQLException e1) {
 			System.err.println("Autocommit could not be diabled before album creation");
 			return false;
 		}
 		Boolean result = true;
 		try {
 			createNewAlbumTable(fields, albumName, hasAlbumPictureField);
 			// Indicate which fields are quicksearchable
 			List<String> quickSearchableColumnNames = new ArrayList<String>();
 			for (MetaItemField metaItemField : fields) {
 				if (metaItemField.quickSearchable){
 					quickSearchableColumnNames.add(encloseNameWithQuotes(metaItemField.getName()));
 				}
 			}
 			createIndex(albumName, quickSearchableColumnNames);			
 			if ( !FileSystemAccessWrapper.updateAlbumFileStructure(connection) ) {
 				try {
 					connection.rollback();
 				} catch (SQLException e1) {
 					e1.printStackTrace();
 					System.err.println("Could not rollback to a save point before creating the album");
 				}
 				return false;
 			}
 			updateLastDatabaseChangeTimeStamp();
 		} catch (SQLException e) {
 			e.printStackTrace();
 			try {
 				connection.rollback();
 			} catch (SQLException e1) {
 				e1.printStackTrace();
 				System.err.println("Could not rollback to a save point before creating the album");
 			}
 			result = false;
 		} finally {
 			try {
 				connection.setAutoCommit(previousAutoCommitState);
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * Permanently renames an album in the specified databse.
 	 * @param oldAlbumName The old name of the album to be renamed
 	 * @param newAlbumName The new name of the album.
 	 * @return True if the renaming of the album succeeded. False otherwise.
 	 */
 	public static boolean renameAlbum(String oldAlbumName, String newAlbumName) {
 		boolean result = true;		
 
 		// Rename the album table
 		if ( renameTable(oldAlbumName, newAlbumName) == false ) {
 			return false;
 		}
 
 		// Rename the type info table		
 		String oldTypeInfoTableName = makeTypeInfoTableName(oldAlbumName);
 		String newTypeInfoTableName = makeTypeInfoTableName(newAlbumName);
 
 		if ( renameTable(oldTypeInfoTableName, newTypeInfoTableName) == false ) {
 			//TODO rollback
 			return false;
 		}
 
 		// Change the entry in the album master table.
 		if ( modifyAlbumInAlbumMasterTable(oldAlbumName, newAlbumName, newTypeInfoTableName) == false) {
 			//TODO rollback
 			return false;
 		}
 
 		updateLastDatabaseChangeTimeStamp();
 		return result;
 	}
 
 	/**
 	 * Renames a table. All referenced columns and indices
 	 * @param oldTableName The name of the table to be renamed.
 	 * @param newTableName The new name of the table.
 	 * @return True if the operation was successful, false otherwise.
 	 */
 	private static boolean renameTable(String oldTableName, String newTableName) {
 		boolean success = true;
 		
 		StringBuilder sb = new StringBuilder();
 		sb.append("ALTER TABLE ");
 		sb.append(encloseNameWithQuotes(oldTableName));
 		sb.append(" RENAME TO ");
 		sb.append(encloseNameWithQuotes(newTableName));
 		String renameTableSQLString = sb.toString();
 		
 		PreparedStatement preparedStatement = null;
 		try {
 			preparedStatement = connection.prepareStatement(renameTableSQLString);
 			preparedStatement.executeUpdate();
 		} catch (SQLException e) {
 			//TODO log
 			success = false;
 		}finally {
 			try {
 				if (preparedStatement != null) {
 					preparedStatement.close();
 				}
 			} catch (SQLException e) {
 				success = false;
 				// TODO log
 			}
 		}
 		return success;
 		
 	}
 
 	/**
 	 * Permanently removes a field from an album. Removing fields of type ID is not allowed.
 	 * @param albumName The name of the album to be removed.
 	 * @param metaItemField A description, name, type and quicksearch flag of the original metaItemField. 
 	 * The object does not need to be a reference to the original metaItemfield of the album but in order to delete the item field
 	 * ALL values of the meta item field have to be set correctly including the quicksearch flag.
 	 * @return True if the field was successfully removed. 
 	 * False if the metaItemField was not present in the album or could not be removed.
 	 */
 	public static boolean removeAlbumItemField(String albumName, MetaItemField metaItemField) {
 		boolean result = true;
 
 		// Check if the specified columns exists.
 
 		List<MetaItemField> metaInfos =  getAllAlbumItemMetaItemFields(albumName);
 		if (!metaInfos.contains(metaItemField)) {
 			if (metaInfos.contains(new MetaItemField(metaItemField.getName(), metaItemField.getType(), !metaItemField.isQuickSearchable()))){
 				System.err.println("The specified meta item field's quicksearch flag is not set appropriately!");
 			}else {
 				System.err.println("The specified meta item field is not part of the album");
 
 			}
 			return false;
 		}
 
 		try {
 			connection.setAutoCommit(false);
 			// Backup the old data in java objects
 			List<AlbumItem> albumItems = fetchAlbumItemsFromDatabase(createSelectStarQuery(albumName));
 			Map<Long, String> rawPicFieldMap = new HashMap<Long, String>();
 			// Create the new table pointing to new typeinfo
 			boolean keepPictureField = albumHasPictureField(albumName) && !metaItemField.getType().equals(FieldType.Picture);
 			List<MetaItemField> newFields =  getAlbumItemFieldNamesAndTypes(albumName);
 			newFields = removeFieldFromMetaItemList(metaItemField, newFields);// [delete column]
 
 			for (AlbumItem albumItem : albumItems) {				
 				// store the old uri List of all albumItems to restore later
 				long albumItemID = (Long) albumItem.getField("id").getValue();
 				String rawPicString = null;
 				if ( keepPictureField ) {
 					rawPicString = fetchRAWDBPictureString(albumItem.getAlbumName(), albumItemID);
 				}
 				rawPicFieldMap.put(albumItemID, rawPicString);
 			}
 
 			// Drop the old table + typeTable
 			removeAlbum(albumName);
 
 
 			// The following three columns are automatically created by createNewAlbumTable
 			newFields = removeFieldFromMetaItemList(new MetaItemField("id", FieldType.ID), newFields);
 			newFields = removeFieldFromMetaItemList(new MetaItemField(TYPE_INFO_COLUMN_NAME, FieldType.ID), newFields);
 			newFields = removeFieldFromMetaItemList(new MetaItemField(PICTURE_COLUMN_NAME, FieldType.Picture), newFields);
 
 			 createNewAlbumTable( newFields, albumName, keepPictureField);	
 
 			// Restore the old data from the java objects in the new tables [delete column]
 			List<AlbumItem> newAlbumItems = removeFieldFromAlbumItemList(metaItemField, albumItems);
 			for (AlbumItem albumItem : newAlbumItems) {
 				albumItem.setAlbumName(albumName);
 				if (keepPictureField ) {
 					long albumItemID = (Long) albumItem.getField("id").getValue();
 					albumItem.setFieldValue(PICTURE_COLUMN_NAME,rawPicFieldMap.get(albumItemID));
 				}
 				if (addNewAlbumItem(albumItem, true, false) == -1) {					
 					result = false;
 					break;
 				}
 			}	
 
 			rebuildIndexForTable(albumName, newFields);
 			updateLastDatabaseChangeTimeStamp();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}finally {
 			try {
 				connection.setAutoCommit(true);
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * Renames the field specified in oldMetaItemField to the name in newMetaItemField. No type change nor renaming fields of type ID is allowed.
 	 * @param albumName The name of the album to which the item belongs.
 	 * @param oldMetaItemField A description of the original metaItemField, no need for direct reference.
 	 * A description, name, type and quicksearch flag of the original metaItemField. In order to rename the item field
 	 * ALL values of the meta item field have to be set correctly including the quicksearch flag.
 	 * @param newMetaItemField A description of the new metaItemField, no need for direct reference.
 	 * @return True if the renaming of the field succeeded. False otherwise.
 	 */
 	public static boolean renameAlbumItemField(String albumName, MetaItemField oldMetaItemField, MetaItemField newMetaItemField) {
 		boolean result = true;
 		
 		// Check if the specified columns exists.
 		List<MetaItemField> metaInfos =  getAllAlbumItemMetaItemFields(albumName);
 		if (!metaInfos.contains(oldMetaItemField) || oldMetaItemField.getType().equals(FieldType.ID)) {
 			if (metaInfos.contains(new MetaItemField(oldMetaItemField.getName(), oldMetaItemField.getType(), !oldMetaItemField.isQuickSearchable()))){
 				System.err.println("The specified meta item field's quicksearch flag is not set appropriately!");
 			}else {
 				System.err.println("The specified meta item field is not part of the album");
 			}
 			return false;
 		}
 
 		try {
 			connection.setAutoCommit(false);
 			// Backup the old data in java objects
 			List<AlbumItem> albumItems = fetchAlbumItemsFromDatabase(createSelectStarQuery(albumName));
 			Map<Long, String> rawPicFieldMap = new HashMap<Long, String>();
 			// Create the new table pointing to new typeinfo
 			boolean hasPictureField = albumHasPictureField(albumName);
 			List<MetaItemField> newFields =  getAlbumItemFieldNamesAndTypes(albumName);
 			newFields = renameFieldInMetaItemList(oldMetaItemField, newMetaItemField, newFields);// [rename column]
 			for (AlbumItem albumItem : albumItems) {	
 				// store the old uri List of all albumItems to restore later
 				long albumItemID = (Long) albumItem.getField("id").getValue();
 				String rawPicString = null;
 				if ( hasPictureField ) {
 					rawPicString = fetchRAWDBPictureString(albumItem.getAlbumName(), albumItemID);
 				}
 				rawPicFieldMap.put(albumItemID, rawPicString);
 			}
 
 			// Drop the old table + typeTable
 			removeAlbum(albumName);
 
 			// the following three columns are automatically created by createNewAlbumTable
 			newFields = removeFieldFromMetaItemList(new MetaItemField("id", FieldType.ID), newFields);
 			newFields = removeFieldFromMetaItemList(new MetaItemField(TYPE_INFO_COLUMN_NAME, FieldType.ID), newFields);
 			newFields = removeFieldFromMetaItemList(new MetaItemField(PICTURE_COLUMN_NAME, FieldType.Picture), newFields);
 
 			createNewAlbumTable(	newFields, 
 					albumName, 
 					hasPictureField);	
 
 			// Restore the old data from the java objects in the new tables [rename column]
 			List<AlbumItem> newAlbumItems = renameFieldInAlbumItemList(oldMetaItemField, newMetaItemField, albumItems);
 			// replace the empty picField with the saved raw PicField 
 			for (AlbumItem albumItem : newAlbumItems) {
 				albumItem.setAlbumName(albumName);
 				if (hasPictureField) {
 					long albumItemID = (Long) albumItem.getField("id").getValue();
 					albumItem.setFieldValue(PICTURE_COLUMN_NAME,rawPicFieldMap.get(albumItemID));
 				}
 				if (addNewAlbumItem(albumItem, true, false) == -1) {
 					result = false;
 					break;
 				}
 			}	
 
 			rebuildIndexForTable(albumName, newFields);
 			updateLastDatabaseChangeTimeStamp();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}finally {
 			try {
 				connection.setAutoCommit(true);
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * Moves the specified field to a the position directly after the column with the name moveAfterColumnName.
 	 * @param albumName The name of the album to which the item belongs.
 	 * @param metaItemField The metadata to identify the field (column) to be moved.
 	 * @param preceedingField The field (column) which is preceeding the field after the reordering. 
 	 * @return True if the operation succeeded. False is any problems were encountered.
 	 */
 	public static boolean reorderAlbumItemField(String albumName, MetaItemField metaItemField, MetaItemField preceedingField) {
 		boolean result = true;
 		// Check if the specified columns exists.
 
 		List<MetaItemField> metaInfos =  getAllAlbumItemMetaItemFields(albumName);
 		if (!metaInfos.contains(metaItemField)) {
 			return false;
 		}
 
 		try {
 			connection.setAutoCommit(false);
 			// Backup the old data in java objects
 			List<AlbumItem> albumItems = fetchAlbumItemsFromDatabase(createSelectStarQuery(albumName));
 			Map<Long, String> rawPicFieldMap = new HashMap<Long, String>();
 			// Create the new table pointing to new typeinfo
 			boolean hasPictureField = albumHasPictureField(albumName);
 			List<MetaItemField> newFields =  getAlbumItemFieldNamesAndTypes(albumName);
 			newFields = reorderFieldInMetaItemList(metaItemField, preceedingField, newFields);// [reorder column]
 			for (AlbumItem albumItem : albumItems) {
 				// store the old uri List of all albumItems to restore later
 				Long albumItemID = (Long) albumItem.getField("id").getValue();
 				String rawPicString = null;
 				if ( hasPictureField ) {
 					rawPicString = fetchRAWDBPictureString(albumItem.getAlbumName(), albumItemID);
 				}
 				rawPicFieldMap.put(albumItemID, rawPicString);
 			}
 
 			// Drop the old table + typeTable
 			removeAlbum(albumName);
 
 			// the following three columns are automatically created by createNewAlbumTable
 			newFields = removeFieldFromMetaItemList(new MetaItemField("id", FieldType.ID), newFields);
 			newFields = removeFieldFromMetaItemList(new MetaItemField(TYPE_INFO_COLUMN_NAME, FieldType.ID), newFields);
 			newFields = removeFieldFromMetaItemList(new MetaItemField(PICTURE_COLUMN_NAME, FieldType.Picture), newFields);
 
 			// Create the new table pointing to new typeinfo
 			createNewAlbumTable(	newFields, 
 					albumName, 
 					hasPictureField);
 
 			// Restore the old data from the temptables in the new tables [reorder column]
 			List<AlbumItem> newAlbumItems = reorderFieldInAlbumItemList(metaItemField, preceedingField, albumItems);
 			// replace the empty picField with the saved raw PicField 
 			for (AlbumItem albumItem : newAlbumItems) {
 				albumItem.setAlbumName(albumName);
 				if (hasPictureField) {
 					long albumItemID = (Long) albumItem.getField("id").getValue();
 					albumItem.setFieldValue(PICTURE_COLUMN_NAME,rawPicFieldMap.get(albumItemID));
 				}
 				if (addNewAlbumItem(albumItem, true, false) == -1) {
 					result = false;
 					break;
 				}
 			}
 
 			rebuildIndexForTable(albumName, newFields);
 			updateLastDatabaseChangeTimeStamp();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}finally {
 			try {
 				connection.setAutoCommit(true);
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 
 		}
 		return result;
 	}
 
 	/**
 	 * Sets the ability of albumField to the value found in the metaItemField describing that field.
 	 * @param albumName The name of the album to which the item belongs.
 	 * @param metaItemField The field (column) to be set quicksearchable.
 	 * @return True if the operation succeeded. False is any problems were encountered.
 	 */
 	public static boolean setQuickSearchable(String albumName, MetaItemField metaItemField) {
 		boolean result = true;
 
 		List<String> quickSearchableColumnNames = getIndexedColumnNames(albumName);
 		// Enable for quicksearch feature
 		if (metaItemField.quickSearchable && !quickSearchableColumnNames.contains(metaItemField.getName())) {
 			quickSearchableColumnNames.add(metaItemField.getName());
 			result = dropIndex(albumName);
 			boolean createIndexRes = createIndex(albumName, quickSearchableColumnNames);
 			result = (result && createIndexRes);
 		}else if (!metaItemField.quickSearchable){	
 			// Disable for quicksearch feature
 			quickSearchableColumnNames.remove(metaItemField.getName());
 			result = dropIndex(albumName);
 			boolean createIndexRes = createIndex(albumName, quickSearchableColumnNames);
 			result = (result && createIndexRes);
 		}
 		try {
 			updateSchemaVersion(albumName);
 			updateLastDatabaseChangeTimeStamp();
 		} catch (SQLException e) {
 			e.printStackTrace();
 			result = false;
 		}
 		return result;
 	}
 
 	/**
 	 * Retrieves the raw uninterpreted Picture string from the database. Since pictures are stored as single separated String in the db.
 	 * @param tableName The name of the table to which the Picture string belongs.
 	 * @param albumItemID the id of the albumItem entry
 	 * @return The raw picture string, or null if the operation fails.
 	 */
 	private static String fetchRAWDBPictureString (String tableName, Long albumItemID) {
 		PreparedStatement preparedStatement = null;
 		ResultSet rs = null;
 		String rawDBString = null;
 		try {
 			preparedStatement = connection.prepareStatement(createSelectColumnQueryWhere(tableName, PICTURE_COLUMN_NAME, "id"));
 			preparedStatement.setLong(1, albumItemID);
 			rs = preparedStatement.executeQuery();
 			if (rs.next()) {
 				rawDBString = rs.getString(1);
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				rs.close();
 			} catch (SQLException e1) {
 				e1.printStackTrace();
 			}
 			try {
 				preparedStatement.close();
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 		return rawDBString;
 	}
 
 	/**
 	 * Generates a new random UUID. ID collisions may happen although highly unlikely
 	 * @return The uuid value.
 	 */
 	private static UUID generateNewUUID () {
 		return UUID.randomUUID();
 	}
 
 	/**
 	 * Removes a MetaItemField from a list by using an equals test. Returns the result in a new list.
 	 * @param metaItemField The field to be removed from the list.
 	 * @param fieldList The list of which the metaItemField will be removed.
 	 * @return The parameter fieldList with the specified field removed.
 	 */
 	private static List<MetaItemField> removeFieldFromMetaItemList(MetaItemField metaItemField, final List<MetaItemField> fieldList) {
 		List<MetaItemField> newFieldList = fieldList;
 		newFieldList.remove(metaItemField);
 		return newFieldList; 
 	}
 
 	/**
 	 * Renames a metaItemField for all the fields in the list.Type changes are currently not supported. Returns the result in a new list.
 	 * @param oldMetaItemField The metaItemField to be renamed.
 	 * @param newMetaItemField The metaItemField containing the new name.
 	 * @param fieldList The list of fields to be renamed.
 	 * @return The list of renamed fields.
 	 */
 	private static List<MetaItemField> renameFieldInMetaItemList(MetaItemField oldMetaItemField, MetaItemField newMetaItemField, final List<MetaItemField> fieldList) {
 		List<MetaItemField> newFieldList = fieldList;
 		int index = newFieldList.indexOf(oldMetaItemField);
 		MetaItemField renameMetaItemField = newFieldList.get(index);
 		renameMetaItemField.setName(newMetaItemField.getName());
 		return newFieldList; 
 	}
 
 	/**
 	 * Moves the metaItemField after the specified moveAfterField. In case the latter is null, move to beginning of list. 
 	 * Returns the result in a new list.
 	 * @param metaItemField The field to be moved to a new position.
 	 * @param precedingField The field which will precede the field in the new ordering. If null the field will be inserted at the beginning
 	 * of the list.
 	 * @param fieldList List of fields containing the field in the old ordering.
 	 * @return The list of fields in the new ordering.
 	 */
 	private static List<MetaItemField> reorderFieldInMetaItemList(MetaItemField metaItemField, MetaItemField precedingField, final List<MetaItemField> fieldList) {
 		List<MetaItemField> newFieldList = fieldList;
 		newFieldList.remove(metaItemField);
 		if (precedingField == null) {
 			newFieldList.add(0,metaItemField);
 		} else {
 
 			int insertAfterIndex = newFieldList.indexOf(precedingField);
 			if (insertAfterIndex==-1) {
 				newFieldList.add(metaItemField);
 			}else {
 				newFieldList.add(insertAfterIndex+1, metaItemField);
 			}
 		}
 		return newFieldList; 
 	}
 
 	/**
 	 * Removes the provided metaItemField from each AlbumItem entry in the albumList. Returns the result in a new list.
 	 * @param metaItemField The metaItemFiel to be removed from each entry of the list.
 	 * @param albumList The list containing all the AlbumItems.
 	 * @return The albumList with the specified metaItemField removed from each entry.
 	 */
 	private static List<AlbumItem> removeFieldFromAlbumItemList(MetaItemField metaItemField, final List<AlbumItem> albumList) {
 		List<AlbumItem> newAlbumItemList = albumList;
 		for (AlbumItem albumItem: newAlbumItemList) {
 			albumItem.removeField(metaItemField);
 		}
 		return newAlbumItemList; 
 	}
 
 	/**
 	 * Renames a field in all the album items in the list. Returns the result in a new list.
 	 * @param oldMetaItemField The metaItemField to be renamed.
 	 * @param newMetaItemField The metaItemField containing the new name.
 	 * @param albumList The list of album items whose field is to be renamed.
 	 * @return The new list of album items.
 	 */
 	private static List<AlbumItem> renameFieldInAlbumItemList(MetaItemField oldMetaItemField, MetaItemField newMetaItemField, final List<AlbumItem> albumList) {
 		List<AlbumItem> newAlbumItemList = albumList;
 		for (AlbumItem albumItem: newAlbumItemList) {
 			albumItem.renameField(oldMetaItemField, newMetaItemField);
 		}
 		return newAlbumItemList; 
 	}
 
 	/**
 	 * Moves a field of an album item to a new position. Returns the result in a new list. 
 	 * @param metaItemField The field to be moved.
 	 * @param precedingField The field which will precede the specified item after the reordering. If null the item will be moved to the top
 	 * of the list. 
 	 * @param albumList The list of album items in their original ordering.
 	 * @return The list of album items in their new order. Content remains untouched.
 	 */
 	private static List<AlbumItem> reorderFieldInAlbumItemList(MetaItemField metaItemField, MetaItemField precedingField, final List<AlbumItem> albumList) {
 		List<AlbumItem> newAlbumItemList = albumList;
 		for (AlbumItem albumItem: newAlbumItemList) {
 			albumItem.reorderField(metaItemField, precedingField);
 		}
 		return newAlbumItemList; 
 	}
 
 	/**
 	 * Permanently removes an album along with its typeInfo metadata.
 	 * @param albumName The name of the album which is to be removed.
 	 * @return True if successful, false otherwise.
 	 */
 	public static boolean removeAlbum(String albumName) {
 		boolean result = true;
 		try {
 			// Drop the main table
 			String typeInfoTableName = getTypeInfoTableName(albumName);
 			dropTable(albumName);
 
 			// drop the typeInfo table
 			dropTable(typeInfoTableName);
 			
 			if ( !removeAlbumFromAlbumMasterTable(albumName) ) {
 				result = false;
 			}
 			
 			updateLastDatabaseChangeTimeStamp();
 		} catch (SQLException e) {
 			e.printStackTrace();
 			result = false;
 		}
 		return result;
 	}
 
 	/**
 	 * Drops a table if it exists. No error or side effects if it does not exist.
 	 * @param tableName The name of the table which is to be dropped.
 	 * @throws SQLException Exception thrown if any part of the operation fails.
 	 */
 	private static void dropTable(String tableName) throws SQLException {
 		Statement statement = connection.createStatement();
 		statement.setQueryTimeout(5);
 		statement.execute("DROP TABLE IF EXISTS "+encloseNameWithQuotes(tableName));
 		statement.close();
 	}
 
 	/**
 	 * Creates a new table with the specified fields. See fields parameter for temporary table creation.
 	 * Beware a temporary table does not show up in the list of all albums.
 	 * @param fields The fields making up the new album content. Null if a temporary copy of the specified existing table should be made.
 	 * @param tableName The name of the table to be created.
 	 * @param hasAlbumPicture True indicates that the table will contain a picture column.
 	 * @throws SQLException Exception thrown if any part of the operation fails.
 	 * @return The name of  type info table linked to the newly created album table
 	 */
 	private static String createNewAlbumTable(List<MetaItemField> fields, String tableName, boolean hasAlbumPicture) throws SQLException {
 		String typeInfoTableName = "";
 		String createTempTableSQL = "";
 		boolean temporary = (fields == null);
 
 		if (temporary) {
 			// Retrieve the typeInfo of the old Album before creating a new temp table
 			fields = getAllAlbumItemMetaItemFields(tableName);
 			// Remove the id field from the old table
 			fields.remove(new MetaItemField("id", FieldType.ID));
 			// Remove the id field from the old table
 			fields.remove(new MetaItemField(TYPE_INFO_COLUMN_NAME, FieldType.ID));
 			tableName =  tableName + TEMP_TABLE_SUFFIX;
 			typeInfoTableName =  tableName + TYPE_INFO_SUFFIX;
 			createTempTableSQL = "TEMPORARY";
 		}else {
 			typeInfoTableName =  tableName + TYPE_INFO_SUFFIX;
 		}
 
 		// Ensures that the table has a contentVersion column
 		MetaItemField contentVersion = new MetaItemField(CONTENT_VERSION_COLUMN_NAME, FieldType.UUID);
 		if (!fields.contains(contentVersion)) {
 			System.out.println("createNewAlbumTable(), no content version "+fields);
 			fields.add(contentVersion);
 		}
 
 		// Prepare statement string
 		String createMainTableString = "";
 		StringBuilder sb = new StringBuilder("CREATE ");
 
 		// Insert temporary table qualifier when necessary
 		sb.append(createTempTableSQL);
 
 		sb.append(" TABLE ");
 		tableName = encloseNameWithQuotes(tableName);
 		sb.append(tableName);
 		sb.append(" ( id INTEGER PRIMARY KEY");
 
 
 		fields = handleCreatePictureField(fields, hasAlbumPicture);
 		for (MetaItemField item : fields) {
 			sb.append(" , ");
 			sb.append(encloseNameWithQuotes(item.getName()));	// " , 'fieldName'"  
 			sb.append(" ");										// " , 'fieldName' " 
 			sb.append(item.getType().toDatabaseTypeString());	// " , 'fieldName' TYPE"
 		}
 
 		sb.append(" ,  ");
 
 		// Add the typeInfo column and foreign key reference to the typeInfo table.
 		sb.append(TYPE_INFO_COLUMN_NAME);
 		sb.append(" INTEGER, FOREIGN KEY(");
 		sb.append(TYPE_INFO_COLUMN_NAME);
 		sb.append(") REFERENCES ");
 		sb.append(encloseNameWithQuotes(typeInfoTableName));
 		sb.append("(id))");
 		createMainTableString = sb.toString();
 
 		// Save the type informations in a separate table
 		createTypeInfoTable(typeInfoTableName, fields, temporary);
 
 		// Create the Album table
 		Statement statement = connection.createStatement();
 		statement.executeUpdate(createMainTableString);
 		statement.close();
 		
 		// Add the album back to the album master table
 		addNewAlbumToAlbumMasterTable(tableName, typeInfoTableName);
 		return typeInfoTableName;
 	}
 
 
 	/**
 	 * Appends new field to the end of the album. Fields with the type FieldType.ID or FieldType.Picture fail the whole operation, no fields will be added then.
 	 * @param albumName The name of the album to be modified.
 	 * @param metaItemField The metaItemField to be appended to the album. Must not be null for successful insertion
 	 * @return True if the operation succeeded and the fields were addded, false if the operation failed either due to an internal error or invalid fields.
 	 */
 	public static boolean appendNewAlbumField(String albumName, MetaItemField metaItemField) {
 		if (metaItemField.getType().equals(FieldType.ID) || metaItemField.getType().equals(FieldType.Picture) || metaItemField == null || !itemFieldNameIsAvailable(albumName, metaItemField.getName())) {
 			return false;
 		}
 
 		if ( appendNewTableColumn(albumName, metaItemField) ) {
 			//TODO: implement this directly in a single operation such that the picture field
 			// is always the last column
 			List<MetaItemField> metaItemFields = getAlbumItemFieldNamesAndTypes(albumName);
 			if (albumHasPictureField(albumName) && metaItemFields.size()>1) {
 				reorderAlbumItemField(albumName, metaItemField, metaItemFields.get(metaItemFields.size()-1));
 			}
 			updateLastDatabaseChangeTimeStamp();
 			return true;
 		}
 
 		return false;
 	}
 	/**
 	 * Appends a new column to the album table. This internal method does allows to add any type of column, even id and picture column.
 	 * An exception is that you cannot add an additional picture column to an table.
 	 * To prevent accidental corruption of the tables, perform checks in the enclosing methods.
 	 * @param albumName The name of the album to be modified.
 	 * @param metaItemField he metaItemFields to be appended to the album.
 	 * @return True if the operation succeeded and the fields were addded, false if the operation failed either due to an internal error or invalid fields.
 	 */
 	private static boolean appendNewTableColumn(String albumName, MetaItemField metaItemField) {
 		Boolean result = true;
 		PreparedStatement preparedStatement = null;
 		try {
 			connection.setAutoCommit(false);
 
 			// Maximum 1 picture column per table.
 			if (metaItemField.getType().equals(FieldType.Picture) && albumHasPictureField(albumName)) {
 				return false;
 			}
 
 			// Prepare the append column string for the main table.
 			StringBuilder sb = new StringBuilder("ALTER TABLE ");
 			sb.append(encloseNameWithQuotes(albumName));
 			sb.append(" ADD COLUMN ");
 			sb.append(encloseNameWithQuotes(metaItemField.getName()));
 			sb.append(" ");
 			sb.append(FieldType.Text.toDatabaseTypeString());
 
 			preparedStatement = connection.prepareStatement(sb.toString());
 			preparedStatement.executeUpdate();
 
 			updateTableColumnWithDefaultValue(albumName, metaItemField);
 
 			// Append and update column for type table.
 			appendNewTypeInfoTableColumn(albumName, metaItemField);
 
 			updateSchemaVersion(albumName);
 
 			connection.commit();
 		} catch (SQLException e) {
 			e.printStackTrace();
 			result = false;
 		}finally {
 			try {
 				connection.setAutoCommit(true);
 				if (preparedStatement != null) {
 					preparedStatement.close(); 
 				}
 				connection.setAutoCommit(true);
 			} catch (SQLException e) {
 				e.printStackTrace();
 				result = false;
 			}
 		}
 		return result;
 	}
 
 
 	/**
 	 * Adds a picture field to an album. Currently only one picture field is allowed.
 	 * @param albumName The name of the album to which the item belongs.
 	 * @return True if the album has a picture field, either through addition or because one already existed.
 	 */
 	public static boolean appendPictureField(String albumName) {
 		boolean result = true;
 		if (albumHasPictureField(albumName)) {
 			return true;
 		}
 
 		// Appends and updates the schema version
 		result = appendNewTableColumn(albumName, new MetaItemField(PICTURE_COLUMN_NAME, FieldType.Picture));
 		if (result) {
 			FileSystemAccessWrapper.updateAlbumFileStructure(connection);
 			updateLastDatabaseChangeTimeStamp();
 		}
 		return result;
 	}
 
 
 	/**
 	 * Removes a picture field from the album. Does not have any side effects when not picture field is present.
 	 * @param albumName The name of the album to which the item belongs.
 	 * @return True if the album has no picture field, either through deletion or because none were present. False if the operation 
 	 * encounters any problems.
 	 */
 	public static boolean removePictureField(String albumName) {
 		boolean result = removeAlbumItemField(albumName, new MetaItemField(PICTURE_COLUMN_NAME, FieldType.Picture));
 		try {
 			updateSchemaVersion(albumName);
 			updateLastDatabaseChangeTimeStamp();
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return false;
 		}
 		return result;
 	}
 
 
 	/**
 	 * Ensures that only a single picture fields exists when specified so. Otherwise all existing picture fields will be removed from results.
 	 * @param fields The old list of fields with an unknown amount of picture fields
 	 * @param hasalbumPicture True indicates that album has a picture field. False that is does not.
 	 * @return The new list of fields with a single picture field.
 	 */
 	private static List<MetaItemField> handleCreatePictureField(final List<MetaItemField> fields, boolean hasalbumPicture) {
 		List<MetaItemField> newFields = new ArrayList<MetaItemField>();
 		// Filter out all existing picture fields 
 		for (MetaItemField metaItemField : fields) {
 			if (!metaItemField.getType().equals(FieldType.Picture)) {
 				newFields.add(metaItemField);
 			}
 		}
 		if (hasalbumPicture) {
 			newFields.add(new MetaItemField(PICTURE_COLUMN_NAME,FieldType.Picture));
 		}
 		return newFields;
 	}
 
 
 	/**
 	 * Indicates whether the album contains a picture field.
 	 * @param albumName The name of the album to be queried.
 	 * @return True if the album contains a picture field, false otherwise.
 	 */
 	public static boolean albumHasPictureField(String albumName) {
 		List<MetaItemField> metaFields = getAllAlbumItemMetaItemFields(albumName);
 		for (MetaItemField metaItemField : metaFields) {
 			if (metaItemField.getType() == FieldType.Picture) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 
 	/**
 	 * Encloses a given album or field name with single quotes such that db accepts it, except for columnNames in a select query.
 	 * Use {@link #transformColumnNameToSelectQueryName(String)} instead for columnName in select queries.
 	 * Use quote marks to enclose columnnames or album names with spaces for example.    
 	 * @param regularName The usual name without special markup for low level db interaction.
 	 * @return The proper string for the database interaction.
 	 */
 	public static String encloseNameWithQuotes(String regularName) {
 		if (regularName.startsWith("'") && regularName.endsWith("'")) {
 			return regularName;
 		}
 		return "'" + regularName + "'";
 	}
 	
 	/**
 	 * Removes one layer of any single quotes enclosing the name. Quotes are unnecessary if setString is used to
 	 * add the name to a query.    
 	 * @param regularName The usual name with possibly enclosing singhle quotes.
 	 * @return The proper string with one layer of single quotes removed if present.
 	 */
 	private static String removeEnclosingNameWithQuotes(String regularName) {
 		if (regularName.startsWith("'") && regularName.endsWith("'")) {
			return regularName.substring(1, regularName.length()-1);
 		}
 		return regularName ;
 	}
 
 	/**
 	 * Transforms a given fieldName into a columnName format, the db accepts. Use squared brackets to enclose columnnames with spaces for example.    
 	 * @param fieldName The name of a field to be transformed.
 	 * @return The proper string for low level query interaction with the database.
 	 */
 	public static String transformColumnNameToSelectQueryName(String fieldName) {
 		if (fieldName.startsWith("[") && fieldName.endsWith("]")) {
 			return fieldName;
 		}
 		return "[" + fieldName + "]";
 	}
 
 	/**
 	 * Transforms a value of an album Item and escapes single quotes.
 	 * @param value The value which must not be enclosed in single quotes.
 	 * @return A string with the specified value which has the quotes escaped for further processing. Typically used in a 
 	 * raw SELECT statement.
 	 */
 	public static String sanitizeSingleQuotesInAlbumItemValues(String value) {
 		int lastIndex = 0;
 		int singleQuoteIndex = value.indexOf('\'',0);
 		StringBuilder sb= new StringBuilder();
 		while(singleQuoteIndex != -1) {
 			sb.append(value.substring(lastIndex, singleQuoteIndex));
 			sb.append("''");
 			lastIndex = singleQuoteIndex+1;
 			singleQuoteIndex =value.indexOf('\'',singleQuoteIndex+1);
 		}
 		if (lastIndex>-1) {
 			sb.append(value.substring(lastIndex));
 		}
 		return sb.toString();
 	}
 
 
 	/**
 	 * Creates the table that contains the type information for the specified album.
 	 * @param typeInfoTableName The name of the typeInfoTable to be created.
 	 * @param metafields A list of meta data describing the main table and making up the content of the typeInfoTable.
 	 * @param temporary True if the table is for temporary storage only.
 	 */
 	private static void createTypeInfoTable(String typeInfoTableName, List<MetaItemField> metafields, boolean temporary) throws SQLException {
 		// Prepare createTypeInfoTable string.
 		String createTypeInfoTableString = "";
 
 		StringBuilder sb = new StringBuilder("CREATE ");
 		if (temporary) {
 			sb.append("TEMPORARY ");
 		}
 		// Add the table id column, id column of main table is not stored, id type is determined differently via dbMetaData.
 		sb.append("TABLE IF NOT EXISTS ");
 		sb.append(encloseNameWithQuotes(typeInfoTableName));
 		sb.append(" ( id INTEGER PRIMARY KEY");
 		// Add the main table columns sans id field or typeInfo column
 		Iterator<MetaItemField> iter = metafields.iterator();
 		while(iter.hasNext()) {
 			sb.append(" , ");// " , "
 			sb.append(encloseNameWithQuotes(iter.next().getName()));// " , 'fieldName'"
 			sb.append(" TEXT");// " , 'fieldName' TEXT" //stored as text to ease transformation back to java enum.
 		}
 		// Add the schema version uuid column
 		sb.append(" , ");
 		sb.append(SCHEMA_VERSION_COLUMN_NAME);
 		sb.append(" TEXT )");
 		createTypeInfoTableString = sb.toString();
 
 		// Drop old type info table.
 		PreparedStatement preparedStatement = connection.prepareStatement("DROP TABLE IF EXISTS "+ encloseNameWithQuotes(typeInfoTableName));
 		preparedStatement.executeUpdate();
 
 		// Create the createTypeInfo table
 		System.out.println(createTypeInfoTableString);
 		preparedStatement = connection.prepareStatement(createTypeInfoTableString);
 		preparedStatement.executeUpdate();
 		preparedStatement.close();
 
 
 		// Append the schema version uuid to the list of metaFields
 		metafields.add(new MetaItemField(SCHEMA_VERSION_COLUMN_NAME, FieldType.UUID));
 		// Add the entry about the type info in the newly create TypeInfo table 
 		addTypeInfo(typeInfoTableName, metafields);
 	}
 	
 	private static String makeTypeInfoTableName(String albumTableName) {
 		if (albumTableName == null || albumTableName.isEmpty()) {
 			return "";
 		}
 		return albumTableName + TYPE_INFO_SUFFIX;
 	}
 
 
 	/**
 	 * Appends a new column to the typeInfoTable. Necessary when the main table is altered to have additional columns.
 	 * @param tableName The name of the table to which the column belongs.
 	 * @param metaItemField The metadata of the new column.
 	 * @throws SQLException Exception thrown if any part of the operation fails.
 	 */
 	private static void appendNewTypeInfoTableColumn(String tableName, MetaItemField metaItemField) throws SQLException {
 		String typeInfoTableName = encloseNameWithQuotes(getTypeInfoTableName(tableName));
 		String columnName = encloseNameWithQuotes(metaItemField.getName());
 		// Prepare the append column string for the type table.
 		StringBuilder sb = new StringBuilder("ALTER TABLE ");
 		sb.append(encloseNameWithQuotes(typeInfoTableName));
 		sb.append(" ADD COLUMN ");
 		sb.append(columnName);
 		sb.append(" TEXT");
 
 		PreparedStatement preparedStatement = connection.prepareStatement(sb.toString());
 		preparedStatement.executeUpdate();
 
 		sb.delete(0,sb.length());
 		sb.append("UPDATE ");
 		sb.append(typeInfoTableName);
 		sb.append(" SET ");
 		sb.append(columnName);
 		sb.append(" = ?");
 
 		preparedStatement = connection.prepareStatement(sb.toString());
 		preparedStatement.setString(1, metaItemField.getType().toString());
 		preparedStatement.executeUpdate();
 
 		updateSchemaVersion(tableName);
 
 	}
 
 
 	/**
 	 * Helper method which adds an entry to the typeInfo table to indicate the types used in the main table and updates the 
 	 * schema version UUID if properly included in the metafields.
 	 * @param item the item describing the newly created main table. Making up the content of the typeInfoTable.
 	 * @return True if the operation was successful. False otherwise.
 	 */
 	private static boolean addTypeInfo(String typeInfoTableName, List<MetaItemField> metafields) {
 		boolean result = true;
 
 		// Build the string with placeholders '?'
 		String insertIntoTableString = "";
 		StringBuilder sb = new StringBuilder("INSERT INTO ");
 		sb.append(encloseNameWithQuotes(typeInfoTableName));
 		sb.append(" ( ");
 
 		Iterator<MetaItemField> it = metafields.iterator();
 		while(it.hasNext()) {
 			String fieldName = encloseNameWithQuotes(it.next().getName()); 
 			sb.append(fieldName);
 			if (it.hasNext())
 			{
 				sb.append(", ");
 			}
 		}
 		sb.append(" ) VALUES ( ");
 
 		int fieldCount = metafields.size();
 		for (int i=1; i<=fieldCount;i++) {
 			sb.append("?");
 			if (i < fieldCount)
 			{
 				sb.append(", ");
 			}
 		}
 
 		sb.append(") ");
 
 		insertIntoTableString = sb.toString();
 		System.out.println(insertIntoTableString);
 		PreparedStatement preparedStatement;
 		try {
 			preparedStatement = connection.prepareStatement(insertIntoTableString);
 
 			preparedStatement.setQueryTimeout(30); // set timeout to 30 sec.
 
 			// Replace the wildcard character '?' by the real type values
 			it = metafields.iterator();
 			int parameterIndex = 1;
 			while(it.hasNext()) {
 				MetaItemField metaItemField = it.next();
 				String columnValue = metaItemField.getType().toString();
 
 				// Generate a new schema version UUID a table is created or modified.
 				if ( metaItemField.getType().equals(FieldType.UUID) && metaItemField.getName().equals(SCHEMA_VERSION_COLUMN_NAME) ) {
 					columnValue = generateNewUUID().toString();
 				}
 
 				preparedStatement.setString(parameterIndex, columnValue);
 				parameterIndex++;
 			}
 
 			preparedStatement.executeUpdate();
 			preparedStatement.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 			result = false;
 		}
 		return result;
 	}
 
 	/**
 	 * Rebuilds the index for a table after an alter table operation. 
 	 * @param albumName The album to which these fields belong.
 	 * @param items The items containing the information of whether they are quicksearchable.
 	 */
 	private static void rebuildIndexForTable(String albumName, List<MetaItemField> fields) {
 		List<String> quicksearchColumnNames = new ArrayList<String>();
 		for (MetaItemField metaItemField : fields) {
 			if (metaItemField.isQuickSearchable()) {
 				quicksearchColumnNames.add(metaItemField.getName());
 			}
 		}
 		if (!quicksearchColumnNames.isEmpty()){
 			createIndex(albumName, quicksearchColumnNames);
 		}
 	}
 
 
 	/**
 	 * Creates a database index containing the specified columns of the album (table). This index is currently used to identify the 
 	 * fields (columns)  marked for the quicksearch feature.
 	 * @param albumName The name of the album to which the index belongs. This name should NOT be escaped.
 	 * @param columnNames The list of names of columns to be included in the index. Performs an automatic test to see if the column names 
 	 * are quoted. 
 	 * @return True if the operation was successful. False otherwise.
 	 */
 	private static boolean createIndex(String albumName, List<String> columnNames) {
 		if (columnNames.isEmpty()) {
 			return false;
 		}
 
 		PreparedStatement preparedStatement = null;
 		StringBuilder sqlStringbuiler = new StringBuilder(	"CREATE INDEX " + encloseNameWithQuotes(albumName+ INDEX_NAME_SUFFIX) + " ON " + 
 				encloseNameWithQuotes(albumName) + " (");
 		sqlStringbuiler.append(encloseNameWithQuotes(columnNames.get(0)));		
 		if (columnNames.size()>=2) {
 			for (int i=1;i<columnNames.size(); i++) {
 				sqlStringbuiler.append(", ");
 				sqlStringbuiler.append(encloseNameWithQuotes(columnNames.get(i))); 
 			}
 		}
 		sqlStringbuiler.append(")");
 		try {
 			preparedStatement = connection.prepareStatement(sqlStringbuiler.toString());
 			preparedStatement.execute();
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return false;
 		} finally {
 			try {
 				preparedStatement.close();
 			} catch (SQLException e) {
 				e.printStackTrace();
 				return false;
 			}
 		}
 		return true;
 	}
 
 
 	/**
 	 * Drops the first index associated to the given table name. 
 	 * @param tableName The name of the table to which the index belongs.
 	 * @return True if the table has no associated index to it. False if the operation failed.
 	 */
 	private static boolean dropIndex(String tableName) {
 		String indexName = getTableIndexName(tableName);
 		if (indexName == null) {			
 			return true;
 		}
 
 		indexName = DatabaseWrapper.encloseNameWithQuotes(indexName);
 		String sqlStatement = "DROP INDEX IF EXISTS "+ indexName;
 		Statement statement= null;
 
 		try {
 			statement = connection.createStatement();
 			statement.execute(sqlStatement);
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return false;
 		}finally {
 			try {
 				statement.close();
 			} catch (SQLException e) {
 				e.printStackTrace();
 				return false;
 			}
 		}
 		return true;
 	}
 
 
 	/**
 	 * Adds the specified item to an existing album. Automatically sets a new contentVersion for the item if updateContentVersion flaag is set.
 	 * @param item The album item to be added. 
 	 * @param insertRAWPicString When true treats the Picture field as raw string to insert. False for copy and new fileName insert into DB.
 	 * @param updateContentVersion True if the content version should be updated, this is the regular case for any content change. 
 	 * False in case the the last contentversion should be copied over, restore or alter table options make use of this. If the old content version
 	 * should be copied over make sure the albumItem contains a content version!
 	 * @return The ID of the newly added item. Probably -1 if it fails.
 	 */
 	public static long addNewAlbumItem(AlbumItem item, boolean insertRAWPicString, boolean updateContentVersion) {
 		// Check if the item contains a albumName
 		if (item.getAlbumName().isEmpty()) {
 			System.err.println("DatabaseWrapper::addNewAlbumItem(), item has no albumName");
 			return -1;
 		}
 
 		//		// Check if specified album Item is valid
 		//		if (!item.isValid()) {
 		//			System.err.println("DatabaseWrapper::addNewAlbumItem(), item is invalid");
 		//			return -1;
 		//		}
 
 		// Check if content version should be carried over if yes ensure a content version is present
 		if (updateContentVersion == false && item.getContentVersion() == null) {
 			System.err.println("The option for carrying over the old content version is checked but no content version is found in the item!");
 			return -1;
 		}
 
 		long result = -1;
 		// Build the string with placeholders '?'
 		String insertIntoTableString = "";
 		StringBuilder sb = new StringBuilder("INSERT INTO ");
 		sb.append(encloseNameWithQuotes(item.getAlbumName()));
 		sb.append(" ( ");
 
 
 		Iterator<ItemField> it = item.getFields().iterator();
 		while(it.hasNext()) {
 			// Ensure that no field with the name of typeInfoColumnName
 
 			String name = it.next().getName();
 			if (!name.equalsIgnoreCase(TYPE_INFO_COLUMN_NAME)) {
 				sb.append(encloseNameWithQuotes(name));
 				sb.append(", ");
 			}
 		}
 
 		// Add the typeInfoColumnName 
 		sb.append(TYPE_INFO_COLUMN_NAME);
 		sb.append(" ) VALUES ( ");
 
 		it = item.getFields().iterator();
 		while(it.hasNext()) {
 			String name = it.next().getName();
 			if (!name.equalsIgnoreCase(TYPE_INFO_COLUMN_NAME)) {
 				sb.append("?, ");
 			}
 		}
 
 		// Add the typeInfoColumn Value
 		sb.append("? )");
 
 		insertIntoTableString = sb.toString();
 		System.out.println(insertIntoTableString);
 		PreparedStatement preparedStatement = null;
 		ResultSet generatedKeys = null;
 		try {
 			preparedStatement = connection.prepareStatement(insertIntoTableString);
 
 			preparedStatement.setQueryTimeout(30); // set timeout to 30 sec.
 
 			// Replace the wildcard character '?' by the real type values
 			it = item.getFields().iterator();
 			int parameterIndex = 1;
 			while(it.hasNext()) {				
 				ItemField itemfield = it.next();
 				String name = itemfield.getName();
 				if (!name.equalsIgnoreCase(TYPE_INFO_COLUMN_NAME)) {					
 					setValueToPreparedStatement(preparedStatement, parameterIndex, itemfield, item.getAlbumName(), insertRAWPicString);
 					parameterIndex++;
 				}
 			}
 			preparedStatement.setLong(parameterIndex, TYPE_INFO_FOREIGN_KEY);
 
 			// Retrieves the generated key used in the new  album item
 			preparedStatement.executeUpdate();
 			generatedKeys = preparedStatement.getGeneratedKeys();
 			if (generatedKeys.next()) {
 				result = generatedKeys.getLong(1);
 			}
 			// Either copies the old content version over or generates a new one
 			UUID newUUID = generateNewUUID();
 			if (!updateContentVersion) {
 				// Carry over old content version
 				newUUID = item.getContentVersion();
 
 			}
 			updateContentVersion(item.getAlbumName(), result, newUUID);//FIXME:
 			updateLastDatabaseChangeTimeStamp();
 		} catch (SQLException e) {
 			e.printStackTrace();
 			result = -1;
 		}finally {
 			try {
 				generatedKeys.close();
 
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 			try {
 				preparedStatement.close();
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 		return result;
 	}
 
 
 	/**
 	 * This helper method sets a value based on type to a preparedStatement at the specified position
 	 * @param preparedStatement The jdbc prepared statement to which the value is to be set.
 	 * @param parameterIndex The index of the parameter to be set.
 	 * @param field The field containing the value to be set as well as the according metadata.
 	 * @param albumName The name of the album to which the item of the field belongs.
 	 * @param insertRAWPicString When true treats the Picture field as raw string to insert. False for copy and new fileName insert into DB.
 	 * @return True if the operation was successful. False otherwise.
 	 * @throws SQLException Exception thrown if any part of the operation fails.
 	 */
 	private static boolean setValueToPreparedStatement(PreparedStatement preparedStatement, int parameterIndex,  ItemField field, String albumName, boolean insertRAWPicString) throws SQLException {
 
 		switch (field.getType()) {
 		case Text: 
 			String text = field.getValue();
 			preparedStatement.setString(parameterIndex, text);		
 			break;
 		case Number: 
 			Double real = field.getValue();
 			preparedStatement.setDouble(parameterIndex, real);		
 			break;
 		case Date: 
 			Date date = field.getValue();
 			preparedStatement.setDate(parameterIndex, date);		
 			break;
 		case Time: 
 			Time time = field.getValue();
 			preparedStatement.setTime(parameterIndex, time);		
 			break;
 		case Option: 
 			OptionType option = field.getValue();
 			// Textual representation of the enum is stored in the DB. 
 			preparedStatement.setString(parameterIndex, option.toString());		
 			break;
 		case URL: 
 			String	url = field.getValue();
 			preparedStatement.setString(parameterIndex, url);		
 			break;
 		case StarRating: 
 			StarRating	starRating = field.getValue();
 			preparedStatement.setString(parameterIndex, starRating.toString());		
 			break;
 		case Integer: 
 			Integer	integer = field.getValue();
 			preparedStatement.setString(parameterIndex, integer.toString());		
 			break;
 		case Picture:
 			if (insertRAWPicString) {
 				String rawDBString = field.getValue();
 				preparedStatement.setString(parameterIndex, rawDBString);
 			} else {
 				List<URI> uriList = field.getValue();
 				List<String> newFileNamesWithFileExtensionList = new ArrayList<String>();
 
 				for (URI uri : uriList) {
 					// TODO write some checks :)
 					newFileNamesWithFileExtensionList.add(new File(uri).getName());
 				}
 
 				preparedStatement.setString(parameterIndex, embedPictureNamesWithFileExtensionInString(newFileNamesWithFileExtensionList));
 			}
 			break;
 		default:
 			break;
 		}
 
 		//TODO return void?
 		return true;
 	}
 
 
 	/**
 	 * Updates all the fields of the specified item in the database using the values provided through item.
 	 * @param item The item to be updated.
 	 * @return True if the operation was successful. False otherwise.
 	 */
 	public static boolean updateAlbumItem(AlbumItem item) {
 		// Check if the item contains a albumName
 		if (item.getAlbumName().isEmpty()) {
 			System.err.println("DatabaseWrapper::addNewAlbumItem(), item has no albumName");
 			return false;
 		}
 
 		// Get the id and make sure the field exist;
 		ItemField idField = item.getField("id");
 
 		if (idField == null) {
 			System.err.println("DatabaseWrapper::updateAlbumItem(), the item to be updated has no id field");
 			return false;
 		}
 
 		boolean result = true;
 		// Build the string with placeholders '?'
 		String updateAlbumItemString = "";
 
 		StringBuilder sb = new StringBuilder("UPDATE ");
 		sb.append(encloseNameWithQuotes(item.getAlbumName()));
 		sb.append(" SET ");
 
 		// Add each field to be update by the query
 		Iterator<ItemField> it = item.getFields().iterator();
 		boolean firstAppended = true;
 		while (it.hasNext()) {
 			ItemField next = it.next();
 			// Exclude the id and fid fields
 			if (!next.getType().equals(FieldType.ID)){
 				if (!firstAppended) {
 					sb.append(", ");
 
 				}
 				sb.append(encloseNameWithQuotes(next.getName()));
 				sb.append("=? ");
 				firstAppended = false;				
 			}
 
 		}
 
 		updateAlbumItemString = sb.toString();
 		updateAlbumItemString += "WHERE id=?";
 
 		try {
 			System.out.println(updateAlbumItemString);
 			PreparedStatement preparedStatement = connection.prepareStatement(updateAlbumItemString);
 			preparedStatement.setQueryTimeout(30);
 
 			// Replace the wildcards
 			it = item.getFields().iterator();
 			int parameterIndex = 1;
 			while (it.hasNext()) {
 				ItemField next = it.next();
 				// Exclude the id and fid fields
 				if (!next.getType().equals(FieldType.ID)){
 					setValueToPreparedStatement(preparedStatement, parameterIndex, next, item.getAlbumName(), false);
 
 					parameterIndex++;
 				}
 			}
 
 			System.out.println(updateAlbumItemString);
 			System.out.println("parameter index: " + parameterIndex);
 			// Replace wildcard char '?' in WHERE id=? clause
 			Long id = idField.getValue();
 			preparedStatement.setString(parameterIndex, id.toString());
 
 
 			int resrows = preparedStatement.executeUpdate();
 			System.out.println("updateAlbumItem(), resrows: "+resrows);
 			preparedStatement.close();
 
 			updateContentVersion(item.getAlbumName(), id, generateNewUUID());
 			updateLastDatabaseChangeTimeStamp();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} 
 
 		return result;
 	}
 
 
 	/**
 	 * Permanently deletes the albumItem with the specified id from the database.
 	 * @param albumName The name of the album to which the item belongs.
 	 * @param albumItemId The id of the item to be deleted.
 	 * @return  True if the operation was successful. False otherwise.
 	 */
 	public static boolean deleteAlbumItem(String albumName, long albumItemId) {
 		boolean result = true;
 
 		String deleteAlbumItemString = "DELETE FROM " + encloseNameWithQuotes(albumName) + " WHERE id=" + albumItemId;
 		System.out.println(deleteAlbumItemString);
 
 		PreparedStatement preparedStatement;
 		try {
 			preparedStatement = connection.prepareStatement(deleteAlbumItemString);
 
 			preparedStatement.executeUpdate();
 			updateLastDatabaseChangeTimeStamp();
 		} catch (SQLException e) {
 			e.printStackTrace();
 			result = false;
 		}
 
 		return result;
 	}
 
 
 	/**
 	 * Simply executes the provided sql query via the connection against a database and returns the results.
 	 * @param sqlStatement An sql query, typically a SELECT statement like SELECT * FROM albumName.
 	 * @return A resultSet containing the desired entries. Null if the query failed. 
 	 */
 	public static AlbumItemResultSet executeSQLQuery(String sqlStatement){
 		AlbumItemResultSet albumItemRS = null;
 		try {
 
 			albumItemRS = new AlbumItemResultSet(connection, sqlStatement);
 
 		} catch (SQLException e) {
 			//e.printStackTrace();
 			System.err.println("The query: \"" + sqlStatement + "\" could not be executed and terminated with message: " + e.getMessage());
 			return null;
 		}
 		return albumItemRS;
 	}
 
 	public static long getNumberOfItemsInAlbum(String albumName) {
 		Statement statement = null;
 		ResultSet resultSet = null;
 		try {
 			statement = connection.createStatement();
 			resultSet = statement.executeQuery("SELECT COUNT(*) AS numberOfItems FROM " + encloseNameWithQuotes(albumName));
 
 			if (resultSet.next()) {
 				return resultSet.getLong("numberOfItems");
 			}
 
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				resultSet.close();
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 			try {
 				statement.close();
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 
 		return 0;
 	}
 
 	private static void updateContentVersion(String albumName, long itemID, UUID newUuid) throws SQLException {	
 		StringBuilder sb = new StringBuilder("UPDATE ");
 		sb.append(encloseNameWithQuotes(albumName));
 		sb.append(" SET ");
 		sb.append(CONTENT_VERSION_COLUMN_NAME);
 		sb.append(" = ? ");
 		sb.append("WHERE id = ?");
 
 		System.out.println("updateContentVersion(), "+sb.toString());
 		PreparedStatement preparedStatement = connection.prepareStatement(sb.toString());
 		preparedStatement.setString(1, newUuid.toString());
 		preparedStatement.setLong(2, itemID);
 		preparedStatement.executeUpdate();
 	}
 
 	private static void updateSchemaVersion(String albumName) throws SQLException {
 		String typeInfoTableName = getTypeInfoTableName(albumName);
 		StringBuilder sb = new StringBuilder("UPDATE ");
 		sb.append(encloseNameWithQuotes(typeInfoTableName));
 		sb.append(" SET ");
 		sb.append(SCHEMA_VERSION_COLUMN_NAME);
 		sb.append(" = ?");
 
 		System.out.println(sb.toString());
 		PreparedStatement preparedStatement = connection.prepareStatement(sb.toString());
 		preparedStatement.setString(1, generateNewUUID().toString());
 		preparedStatement.executeUpdate();
 	}
 
 	/**
 	 * Executes an SQL query against the database.
 	 * @param sqlStatement The SQL statement to be executed. Must be proper SQL compliant to the database.
 	 * @param albumName The name of the album to which the query refers to.
 	 * @return The albumItemResultSet which represent the results of the query. Null if the query fails at any point.
 	 */
 	public static AlbumItemResultSet executeSQLQuery(String sqlStatement, String albumName){
 		AlbumItemResultSet albumItemRS = null;
 		try {
 			Map<Integer, MetaItemField> metaInfoMap = DatabaseWrapper.getAlbumItemMetaMap(albumName);
 
 			albumItemRS = new AlbumItemResultSet(connection, sqlStatement, metaInfoMap);
 
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return albumItemRS;
 	}
 
 
 	/**
 	 * Performs a quicksearch. A quicksearch is a search limited to the marked fields. Every item return contains at least a field
 	 * whose value partially matches the any query term.
 	 * @param albumName The name of the album to which the query refers to.
 	 * @param quickSearchTerms A list of terms to be matched against the marked fields. If null, a select * is performed.
 	 * @return A valid albumItemResultSet for the provided quicksearch terms or a select * 
 	 */
 	public static AlbumItemResultSet executeQuickSearch(String albumName, List<String> quickSearchTerms) {
 		List<MetaItemField> albumFields = getAllAlbumItemMetaItemFields(albumName);
 		String query = "";
 		ArrayList<QueryComponent> queryFields = null;
 		List<String> quicksearchFieldNames = getIndexedColumnNames(albumName);
 
 		// If no field is quicksearchable return select * from albumName or no terms have been entered
 		if (quicksearchFieldNames == null || quicksearchFieldNames.isEmpty() || quickSearchTerms == null || quickSearchTerms.isEmpty() ) {
 			query = createSelectStarQuery(albumName);
 			return executeSQLQuery(query);
 		}
 		System.out.println("executeQuickSearch(), quickSearchTerms"+ quickSearchTerms);
 
 		boolean first = true;
 		for (String term : quickSearchTerms) {
 			if (term.isEmpty()) {
 				continue;
 			}
 			if (!first) {
 				query += " UNION ";
 			} else {
 				first = false;
 			}
 			queryFields = new ArrayList<QueryComponent>();
 			for (MetaItemField field : albumFields) {
 				// Only take quicksearchable fields into account
 				if (field.quickSearchable) {
 					System.out.println("executeQuickSearch(), quickSearchField: "+ field);
 					if (field.getType().equals(FieldType.Text)) {
 						queryFields.add(QueryBuilder.getQueryComponent(field.getName(), QueryOperator.like, term));
 					} else {
 						queryFields.add(QueryBuilder.getQueryComponent(field.getName(), QueryOperator.equals, term));
 					}
 				}
 			}// end of for - fields
 			if (queryFields != null && !queryFields.isEmpty()) {
 				query += QueryBuilder.buildQuery(queryFields, false, albumName);
 			}
 
 		}// end of for - terms
 
 		System.out.println(query);
 		return executeSQLQuery(query, albumName);
 	}
 
 	// TODO AlbumManager should be used instead. not until a proper 
 	/**
 	 * Lists all albums currently stored in the database.
 	 * @return A list of album names. May be empty if no albums were created yet. Null in case of an error.
 	 */
 	public static List<String> listAllAlbums() {
 		List<String> albumList = new ArrayList<String>();
 		String queryAllAlbumsSQL = "Select " + ALBUM_TABLENAME_IN_ALBUM_MASTER_TABLE + " FROM " + albumMasterTableName;		
 		Statement statement = null;
 		ResultSet rs = null;
 		try {			
 			// Select query
 			statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
 			rs = statement.executeQuery(queryAllAlbumsSQL);
 
 			while(rs.next())
 			{
 				albumList.add(rs.getString(1));
 			}
 		} catch (SQLException e1) {
 			albumList = null;
 		} finally {
 			try {
 				if (statement != null) {
 					statement.close();
 				}
 			}catch (SQLException e) {
 				albumList =  null;
 			}
 		}		
 		return albumList;
 	}
 
 	/** 
 	 * Retrieves the name of the table containing the type information about it.
 	 * @param mainTableName The name of the table of which the type information belongs to.
 	 * @return The name of the related typeInfo table or Empty String if no typeInfo table is available. TypeInfo table itself for example.
 	 * @throws SQLException Exception thrown if any part of the operation fails.
 	 */
 	private static String getTypeInfoTableName(String mainTableName)  {
 		DatabaseMetaData dbmetadata = null;
 		ResultSet dbmetars = null;
 		try {
 			dbmetadata = connection.getMetaData();
 			dbmetars = dbmetadata.getImportedKeys(null, null, mainTableName);
 
 			if (dbmetars.next()) {
 				String typeInfoTable = dbmetars.getString("PKTABLE_NAME");//dbmetars.getString(3);
 				dbmetars.close();
 				return typeInfoTable;
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}finally {
 			try {
 				dbmetars.close();
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 		return "";
 	}
 
 
 	/**
 	 * Lists all the columns of a table which are indexed. Indexed columns are also taken into account for the quicksearch feature.
 	 * @param tableName The name of the table to which the columns belong. Table name must NOT be escaped! 
 	 * @return A list of indexed, meaning also quickSearchable, columns. List may be empty if none were indexed. Null if an error occured. 
 	 */
 	public static List<String> getIndexedColumnNames(String tableName) {
 		List<String> indexedColumns = new ArrayList<String>();
 		ResultSet indexRS = null;
 
 		try {
 			DatabaseMetaData dbmetadata =  connection.getMetaData();
 			indexRS = dbmetadata.getIndexInfo(null, null, tableName, false, true);
 			while (indexRS.next()) {
 				if(indexRS.getString("COLUMN_NAME") != null)
 					indexedColumns.add(indexRS.getString("COLUMN_NAME"));
 			} 
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return null;
 		}finally {
 			try {
 				if (indexRS != null)
 					indexRS.close();
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 		return indexedColumns;
 	}
 
 
 	/**
 	 * Retrieves the name of the index. In case there are multiple indices only the first one is looked up. Multiple indices may indicate 
 	 * database insistency.
 	 * @param tableName The name of the table to which the index belongs.
 	 * @return The name of the index table if it exists, null otherwise.
 	 */
 	public static String getTableIndexName(String tableName) {
 		ResultSet indexRS = null;
 		String indexName = null;
 		try {
 			DatabaseMetaData dbmetadata =  connection.getMetaData();
 			indexRS = dbmetadata.getIndexInfo(null, null, tableName, false, true);
 			if(indexRS.next()) {
 				indexName = indexRS.getString("INDEX_NAME");
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return null;
 		}finally {
 			try {
 				indexRS.close();
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 		return indexName;
 	}
 
 	/**
 	 * Retrieves a list of MetaItemFields, excluding those that are for internal use only. Meta item fields describe the items of the album
 	 * @param albumName The name of the album of which to retrieve the information.
 	 * @return The list of MetaItemFields. Return an empty  list if a structural error exists in the database. Null when an internal SQL error occurred.
 	 */
 	public static List<MetaItemField> getAlbumItemFieldNamesAndTypes(String albumName) {
 		List<MetaItemField> itemMetadata = new ArrayList<MetaItemField>();
 		Statement statement = null;
 
 		if (albumNameIsAvailable(albumName)) {
 			return itemMetadata;
 		}
 
 		List<String> quickSearchableColumnNames = getIndexedColumnNames(albumName);
 		List<String> internalColumnNames = Arrays.asList("id", TYPE_INFO_COLUMN_NAME, CONTENT_VERSION_COLUMN_NAME);
 
 		/* 
 		if (quickSearchableColumns == null) {
 			return null;
 		}*/
 		try {
 			statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
 			String dbAlbumName = encloseNameWithQuotes(albumName);
 
 			// Select query
 			ResultSet rs = statement.executeQuery("SELECT * FROM "+ dbAlbumName);
 
 			// Retrieve table metadata
 			ResultSetMetaData metaData = rs.getMetaData();
 
 			int columnCount = metaData.getColumnCount();
 			// Each ItemField
 			for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
 
 				//Excludes all columns that are for internal use only.
 				String columnName = metaData.getColumnName(columnIndex);
 				if (!internalColumnNames.contains(columnName)) {
 					FieldType type = detectDataType(albumName, columnName);
 					MetaItemField metaItem = new MetaItemField(columnName, type, quickSearchableColumnNames.contains(columnName));
 					itemMetadata.add(metaItem);
 				}
 			}
 			statement.close();
 			rs.close();
 
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return null;
 		}
 
 		return itemMetadata;
 	}
 
 	/**
 	 *  Retrieves a list of all MetaItemFields, including those that are for internal use only. Meta item fields describe the items of the album.
 	 * @param albumName The name of the album of which to retrieve the information.
 	 * @return The list of MetaItemFields. Return an empty  list if a structural error exists in the database. Null when an internal SQL error occurred.
 	 */
 	private static List<MetaItemField> getAllAlbumItemMetaItemFields(String albumName){
 		List<MetaItemField> itemMetadata = new ArrayList<MetaItemField>();
 		Statement statement = null;
 
 		List<String> quickSearchableColumnNames = getIndexedColumnNames(albumName);
 
 		/* 
 		if (quickSearchableColumns == null) {
 			return null;
 		}*/
 		try {
 			statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
 			String dbAlbumName = encloseNameWithQuotes(albumName);
 			// Select query
 			ResultSet rs = statement.executeQuery("SELECT * FROM "+ dbAlbumName);
 
 			// Retrieve table metadata
 			ResultSetMetaData metaData = rs.getMetaData();
 
 			int columnCount = metaData.getColumnCount();
 			// Each ItemField
 			for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
 
 				String columnName = metaData.getColumnName(columnIndex);
 				FieldType type = detectDataType(albumName, columnName);
 				MetaItemField metaItem = new MetaItemField(columnName, type, quickSearchableColumnNames.contains(columnName));
 				itemMetadata.add(metaItem);
 			}
 			statement.close();
 			rs.close();
 
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return null;
 		}
 
 		return itemMetadata;
 	}
 
 
 	/**
 	 * Fetches a map of metaItemFields keyed by their field (column) index. 
 	 * @param albumName The name of the album to which this map belongs.
 	 * @return True if the query was successful. False otherwise.
 	 */
 	public static Map<Integer, MetaItemField> getAlbumItemMetaMap(String albumName) {
 		Map<Integer, MetaItemField> itemMetadata = new HashMap<Integer, MetaItemField>();
 		ResultSetMetaData metaData = null;
 		Statement statement = null;
 		ResultSet set = null;
 
 		List<String> quickSearchableColumns = getIndexedColumnNames(albumName);
 
 		// If
 		if (quickSearchableColumns == null) {
 			return null;
 		}
 
 		try {
 			statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
 
 			// Select query
 			// Retrieve table metadata
 			set =	statement.executeQuery("SELECT * FROM "+encloseNameWithQuotes(albumName.trim()));
 			metaData = set.getMetaData();
 
 			int columnCount = metaData.getColumnCount();
 			// Each ItemField
 			for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
 				String name = metaData.getColumnName(columnIndex);
 				FieldType type = detectDataType(albumName, name);	
 				MetaItemField metaItemField = new MetaItemField(name, type,quickSearchableColumns.contains(name));
 				itemMetadata.put(columnIndex, metaItemField);
 			}
 
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return null;
 		}finally {
 			try {
 				set.close();
 				statement.close();
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 
 		return itemMetadata;
 	}
 
 
 	/**
 	 * A helper method to detect the collector FieldType using the type information in the separate typeInfo table.
 	 * @param tableName The name of the table to which the column belongs to.
 	 * @param columnName The name of the column whose type should be determined.
 	 * @return A FieldType expressing the type of the specified column in the resultSet.
 	 * @throws SQLException Exception thrown if any part of the operation fails.
 	 */
 	private static FieldType detectDataType(String tableName, String columnName) throws SQLException {
 		FieldType type = null;
 		DatabaseMetaData dbmetadata = null;
 		ResultSet dbmetars = null;
 		ResultSet typeRs = null;
 		Statement statement = null;
 		try {
 			dbmetadata = connection.getMetaData();
 			dbmetars = dbmetadata.getImportedKeys(null, null, tableName);
 
 			// Get the primary and foreign keys
 			String primaryKey = dbmetars.getString(4);
 			String foreignKey = dbmetars.getString(8);
 
 
 			if (columnName.equalsIgnoreCase(primaryKey) || columnName.equalsIgnoreCase(foreignKey)) {
 				return FieldType.ID;
 			}
 
 			String dbtypeInfoTableName = encloseNameWithQuotes(getTypeInfoTableName(tableName));
 
 			statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
 			statement.setQueryTimeout(30); // set timeout to 30 sec.
 
 			typeRs = statement.executeQuery("SELECT "+ transformColumnNameToSelectQueryName(columnName) +" FROM "+ dbtypeInfoTableName);
 
 			type =  FieldType.valueOf(typeRs.getString(1));
 		} catch (Exception e) {
 			type = FieldType.Text;
 		}finally {
 			dbmetars.close();
 			if (typeRs != null)
 				typeRs.close();
 			if (statement != null)
 				statement.close();
 		}
 		return type;
 	}
 
 
 	/**
 	 * Fetches the value of an item field.
 	 * @param results The result set pointing to the field whose value is to be fetched.
 	 * @param columnIndex The index of the field (column) whithin the item (entry).
 	 * @param type The type of the field.
 	 * @param albumName The name of the album this value of the item belongs to.
 	 * @return True if the query was successful. False otherwise.
 	 * @throws SQLException Exception thrown if any part of the operation fails.
 	 */
 	public static Object fetchFieldItemValue(ResultSet results, int columnIndex, FieldType type, String albumName) throws SQLException {
 		Object value = null;
 		switch (type) {
 		case ID:
 			value = results.getLong(columnIndex);
 			break;
 		case Text:
 			value = results.getString(columnIndex);
 			break;
 		case Number:
 			value = results.getDouble(columnIndex);
 			break;
 		case Integer:
 			value = results.getInt(columnIndex);
 			break;
 		case Date:
 			value = results.getDate(columnIndex);
 			break;
 		case Time:
 			value = results.getTime(columnIndex);
 			break;
 		case Option:
 			String optionValue = results.getString(columnIndex);
 			if (optionValue != null && !optionValue.isEmpty()) {
 				value =  OptionType.valueOf(optionValue);
 			}else {
 				System.err.println("fetchFieldItemValue() - url string is unexpectantly null or empty");
 			}
 			break;
 		case URL:
 			String urlString = results.getString(columnIndex);
 			value =  urlString;
 			break;
 		case StarRating:
 			String rating = results.getString(columnIndex);
 			if (rating != null && !rating.isEmpty()) {
 				value =  StarRating.valueOf(rating);
 			}else {
 				System.err.println("fetchFieldItemValue() - star rating string is unexpectantly null or empty");
 			}
 			break;
 		case Picture:
 			value = extractFullPicturePathsFromString(results.getString(columnIndex), albumName);
 			break;
 			// TODO : -URL/Picture add new table for new types
 		case UUID:
 			value  = UUID.fromString(results.getString(columnIndex));
 			break;
 		default:
 			// FieldType by is default of type text
 			value = null;
 			break;
 		}
 		return value;
 	}
 
 
 	/**
 	 * Queries whether the field is available for the quicksearch feature.
 	 * @param albumName The name of the album to which the field belongs to.
 	 * @param fieldName The name of the field to be queried.
 	 * @return True if the the specified field is available for the quicksearch feature. False otherwise.
 	 */
 	public static boolean isAlbumFieldQuicksearchable(String albumName, String fieldName) {
 		List<String> quicksearchableFieldNames = getIndexedColumnNames(albumName);
 
 		if (quicksearchableFieldNames != null) {
 			return quicksearchableFieldNames.contains(fieldName);
 		}
 		return false;
 	}
 
 	/**
 	 * Queries whether the specified album contains at least one quicksearchable field.
 	 * @param albumName The name of the album to be queried.
 	 * @return True if the the specified album contains at least a single field enabled for the quicksearch feature. False otherwise.
 	 */
 	public static boolean isAlbumQuicksearchable(String albumName) {
 		List<String> quicksearchableFieldNames = getIndexedColumnNames(albumName);
 
 		if (quicksearchableFieldNames.size()>=1){
 			return true;
 		}else {
 			return false;
 		}	
 	}
 
 	/**
 	 * Tests if the proposed album name is not already in use by another album.
 	 * @param requestedAlbumName The proposed album name to be tested of availability.
 	 * @return True if the name can be inserted into the database. False otherwise.
 	 */
 	public static boolean albumNameIsAvailable(String requestedAlbumName) {
 		for (String albumName : DatabaseWrapper.listAllAlbums()) {
 			if (albumName.equalsIgnoreCase(requestedAlbumName)) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	/**
 	 * Tests if the proposed item field name is not already in use by another field of the same album.
 	 * @param albumName The album name that contains the fields that the name is checked against.
 	 * @return True if the name can be inserted into the database. False otherwise.
 	 */
 	public static boolean itemFieldNameIsAvailable(String albumName, String requestedFieldName) {
 		for (MetaItemField metaItemField : DatabaseWrapper.getAlbumItemFieldNamesAndTypes(albumName)) {
 			if (requestedFieldName.equalsIgnoreCase(metaItemField.getName())) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	/**
 	 * Extracts a list of filepaths from a single string using the pictureStringSeparator.
 	 * @param value The string containing the picture names together with their file extension
 	 * @return A list of strings representing picture names together with their file extension
 	 */
 	public static List<URI> extractFullPicturePathsFromString(String value, String albumName) {
 		List<URI> picturePathURIList = new ArrayList<URI>();
 
 		//FIXME
 		if (value != null) {
 			String pictureNamesWithFileExtensionArray[] = value.split(PICTURE_STRING_SEPERATOR);
 
 			for (String pictureNameWithFileExtension : pictureNamesWithFileExtensionArray) {		
 				if (!pictureNameWithFileExtension.isEmpty()) {
 					picturePathURIList.add(FileSystemAccessWrapper.getURIToImageFile(pictureNameWithFileExtension, albumName));
 				}
 			}
 		}
 
 		return picturePathURIList;
 	}
 
 
 	/** Embeds a list of strings (names of pictures) in a single string using the pictureStringSeparator.
 	 * @param value The string containing the picture names together with their extension. E.g. "pic1.png"
 	 * @return A string containing all picture names together with their extension, separated using the pictureStringSeparator
 	 */
 	public static String embedPictureNamesWithFileExtensionInString(List<String> values) {
 		StringBuilder allPicturesWithSeperator = new StringBuilder("");
 		System.out.println(values);
 		for (String fileName : values) {
 			allPicturesWithSeperator.append(fileName + PICTURE_STRING_SEPERATOR);
 		}
 
 		return allPicturesWithSeperator.toString();
 	}
 
 
 	/**
 	 * Fetches an album item by its id and album name.
 	 * @param albumName The name of the album to which this item belongs to.
 	 * @param albumItemId The unique id of the item within the album.
 	 * @return The requested albumItem. Null if no item with the specified id was found.
 	 */
 	public static AlbumItem fetchAlbumItem(String albumName, long albumItemId) {
 		String queryString =  createSelectStarQuery(albumName)+ " WHERE id=" + albumItemId;
 		List<AlbumItem> items = fetchAlbumItemsFromDatabase(queryString);
 		if (items.isEmpty()) {
 			return null;
 		}else {
 			return items.get(0);
 		}
 	}
 
 
 	/**
 	 * Retrieves a list of albumItems from the database.
 	 * @param queryString The proper SQL query string compliant with the database. 
 	 * @return The list albumItems making up the results of the query. An empty list if no matching album item were found.
 	 */
 	public static List<AlbumItem> fetchAlbumItemsFromDatabase(String queryString) {
 
 		LinkedList<AlbumItem> list = new LinkedList<AlbumItem>();
 		// Select query
 		Statement statement = null;
 		ResultSet rs = null;
 		try {
 			statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
 
 			statement.setQueryTimeout(5); // set timeout to 30 sec.
 			rs = statement.executeQuery(queryString);
 
 			// Retrieve table metadata
 			ResultSetMetaData metaData = rs.getMetaData();
 
 			int columnCount = metaData.getColumnCount();
 			// For each albumItem
 			while (rs.next()) {
 				// Create a new AlbumItem instance
 				AlbumItem item = new AlbumItem("");
 				// Each ItemField
 				for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
 
 					// Add new field
 					String fieldName = metaData.getColumnName(columnIndex);
 					String albumName = metaData.getTableName(1);
 					item.setAlbumName(albumName);
 					FieldType type = detectDataType(albumName, fieldName);
 					Object value = fetchFieldItemValue(rs, columnIndex, type, albumName);
 					boolean quicksearchable = isAlbumFieldQuicksearchable(albumName, fieldName);
 					// omit the typeinfo field and set the contentVersion separately
 					if (type == FieldType.ID && fieldName.endsWith(TYPE_INFO_COLUMN_NAME)){
 						continue;
 					}else if (type.equals(FieldType.UUID) && fieldName.equals(CONTENT_VERSION_COLUMN_NAME)) {
 						item.setContentVersion((UUID) value);
 					}else {
 						item.addField(fieldName, type, value, quicksearchable);
 					}
 				}
 				list.add(item);
 			}
 
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				rs.close();
 				statement.close();
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 
 		return list;
 	}	
 
 	/**
 	 * Creates a simple select * from albumName with a properly formatted albumName
 	 * @param albumName The album on which the query should be performed.
 	 * @return A string containing the proper SQL string.
 	 */
 	public static String createSelectStarQuery(String albumName) {
 		return "SELECT * FROM " + encloseNameWithQuotes(albumName);
 	}
 
 
 	/**
 	 * Creates a simple select * from albumName with a properly formatted albumName and columnName
 	 * @param albumName The name of the album to which this query refers to.
 	 * @param columnName The name of the column to be queried.
 	 * @param whereColumn The name of the column which is referenced in the where clause.
 	 * @return The properly formatted select query containing a wildcard as the value for in th where clause.
 	 */
 	public static String createSelectColumnQueryWhere(String albumName, String columnName, String whereColumn) {
 
 		return  "SELECT " + transformColumnNameToSelectQueryName(columnName)+ 
 				" FROM "+ encloseNameWithQuotes(albumName)+ 
 				" WHERE "+transformColumnNameToSelectQueryName(whereColumn)+ "=?";
 	}
 
 
 	/**
 	 * Updates a table entry with a default value for the specific type of that column.
 	 * @param tableName The name of the table which will be updated.
 	 * @param columnMetaInfo The metadata specifying the name and type of the column entry to be updated.
 	 * @return True if the operation was successful. False otherwise.
 	 */
 	private static boolean updateTableColumnWithDefaultValue(String tableName, MetaItemField columnMetaInfo) {
 		PreparedStatement preparedStatement = null;
 
 		boolean result = true;
 		try {
 			String sqlString = "UPDATE "+ encloseNameWithQuotes(tableName)+ " SET " +encloseNameWithQuotes(columnMetaInfo.getName()) + "=?";
 			System.out.println(sqlString);
 			preparedStatement = connection.prepareStatement(sqlString);
 			switch (columnMetaInfo.getType()) {
 			case Text: 
 				preparedStatement.setString(1, (String) columnMetaInfo.getType().getDefaultValue());
 				break;
 			case Number: 
 				preparedStatement.setDouble(1, (Double) columnMetaInfo.getType().getDefaultValue());
 				break;
 			case Integer: 
 				preparedStatement.setInt(1, (Integer) columnMetaInfo.getType().getDefaultValue());
 				break;
 			case Date: 
 				preparedStatement.setDate(1, (Date) columnMetaInfo.getType().getDefaultValue());
 				break;
 			case Time:
 				preparedStatement.setTime(1, (Time) columnMetaInfo.getType().getDefaultValue());
 				break;
 			case Option: 
 				String option = columnMetaInfo.getType().getDefaultValue().toString();
 				preparedStatement.setString(1, option);
 				break;
 			case URL: 
 				String url = columnMetaInfo.getType().getDefaultValue().toString();
 				preparedStatement.setString(1, url);
 				break;
 			case StarRating: 
 				String rating = columnMetaInfo.getType().getDefaultValue().toString();
 				preparedStatement.setString(1, rating);
 				break;
 			case Picture:
 				preparedStatement.setString(1, (String) columnMetaInfo.getType().getDefaultValue());
 				break;
 			default:
 				break;
 			}
 			preparedStatement.execute();
 		} catch (SQLException e) {
 			e.printStackTrace();
 			result = false;
 		}finally {
 			if (preparedStatement != null) {
 				try {
 					preparedStatement.close();
 				} catch (SQLException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 
 		return result;
 
 	}
 
 
 	/**
 	 * Backs the database entries along the properties and pictures up to the specified file.
 	 * @param filePath The path ending with the file name under which the backup will be stored.
 	 * @return True if the operation was successful, false otherwise.
 	 */
 	public static boolean backupToFile(String filePath) {
 		Statement statement = null;
 		String tempDirName=  java.util.UUID.randomUUID().toString();
 		File tempDir = new File(System.getProperty("user.home")+File.separator,tempDirName);
 		if( !tempDir.exists() ) {
 			tempDir.mkdir();
 		}
 
 		// backup pictures
 		File tempAlbumPictureDir = new File(tempDir.getPath() + File.separatorChar + FileSystemAccessWrapper.ALBUM_PICTURES);
 		File sourceAlbumPictureDir = new File(FileSystemAccessWrapper.COLLECTOR_HOME_ALBUM_PICTURES);
 		try {
 			FileSystemAccessWrapper.copyDirectory(sourceAlbumPictureDir, tempAlbumPictureDir);
 		} catch (IOException e1) {
 			e1.printStackTrace();
 		}
 
 		// backup application data
 		File tempAppDataDir = new File(tempDir.getPath() + File.separatorChar + "app-data");
 		File sourceAppDataDir = new File(FileSystemAccessWrapper.COLLECTOR_HOME_APPDATA);
 		try {
 			String lockFileRegex = "^\\.lock$"; 
 			FileSystemAccessWrapper.copyDirectory(sourceAppDataDir, tempAppDataDir, lockFileRegex);
 		} catch (IOException e1) {
 			e1.printStackTrace();
 		}
 
 		boolean successState = true;
 		try {
 			statement = connection.createStatement();			
 			statement.executeUpdate("backup to '" + tempDir.getPath() + File.separatorChar + FileSystemAccessWrapper.DATABASE_TO_RESTORE_NAME+"'");
 		} catch (SQLException e) {
 			successState = false;
 			e.printStackTrace();
 		}	finally {
 			try {
 				statement.close();
 			} catch (SQLException e) {
 				e.printStackTrace();
 				successState = false;
 			}
 		}	
 
 		// Zip the whole temp folder
 		FileSystemAccessWrapper.zipFolderToFile(tempDir.getPath(), filePath);
 
 		// delete temp folder
 		FileSystemAccessWrapper.recursiveDeleteFSObject(tempDir);
 
 		return successState;		
 	}
 
 
 	/**
 	 * Restores the database entries along the properties and pictures from the specified backup file
 	 * @param filePath The path of the file ending with the file name from which the backup will be restored.
 	 * @return True if the operation was successful, false otherwise.
 	 */
 	public static boolean restoreFromFile(String filePath) {		
 		FileSystemAccessWrapper.clearCollectorHome();
 		FileSystemAccessWrapper.unzipFileToFolder(filePath, FileSystemAccessWrapper.COLLECTOR_HOME);
 
 		Statement statement = null;
 		boolean successState = true;
 		try {
 			statement = connection.createStatement();
 			statement.executeUpdate("restore from '" + FileSystemAccessWrapper.DATABASE_TO_RESTORE+"'");
 			lastChangeTimeStamp =  extractTimeStamp(new File(filePath));
 			if (lastChangeTimeStamp==-1){
 				lastChangeTimeStamp = System.currentTimeMillis();
 			}
 		} catch (SQLException e) {
 			successState = false;
 			e.printStackTrace();
 		} finally {
 			try {
 				statement.close();
 			} catch (SQLException e) {
 				e.printStackTrace();
 				successState = false;
 			}
 		}
 
 		FileSystemAccessWrapper.deleteDatabaseRestoreFile();
 		//TODO: Check if executing this only when success creates undesired side effects.
 		if (successState == true){
 			FileSystemAccessWrapper.updateCollectorFileStructure();
 			FileSystemAccessWrapper.updateAlbumFileStructure(connection);
 			// Update timestamp
 			updateLastDatabaseChangeTimeStamp();
 		}
 
 
 		return successState;		
 	}
 
 	/**
 	 * Gets the time stamp when the last change to the database happened.
 	 * @return The time in milliseconds when the last change to the database occured. -1 If not initialized.
 	 */
 	public static long getLastDatabaseChangeTimeStamp() {
 		return lastChangeTimeStamp;
 	}
 
 	private static void updateLastDatabaseChangeTimeStamp() {
 		lastChangeTimeStamp = System.currentTimeMillis();
 	}
 
 	public static boolean isDateField(String albumName, String fieldName) {
 		for (MetaItemField metaItemField : getAllAlbumItemMetaItemFields(albumName)) {
 			if (metaItemField.getName().equals(fieldName) && metaItemField.getType().equals(FieldType.Date)) {
 				return true;
 			}
 		}
 		
 		return false;
 	}
 	
 	public static boolean isOptionField(String albumName, String fieldName) {
 		for (MetaItemField metaItemField : getAllAlbumItemMetaItemFields(albumName)) {
 			if (metaItemField.getName().equals(fieldName) && metaItemField.getType().equals(FieldType.Option)) {
 				return true;
 			}
 		}
 		
 		return false;
 	}
 	
 	/**
 	 * Gets the list of existing autosaves sorted by filename timestamp, newest to oldest.
 	 * @return List of files of previous autosaves. Empty list if none exist
 	 */
 	public static List<File> getAllAutoSaves() {
 		List<File> autoSaves = FileSystemAccessWrapper.getAllMatchingFilesInCollectorHome(autoSaveFileRegex);
 		Collections.sort(autoSaves, new Comparator<File>() {
 
 			@Override
 			public int compare(File file1, File file2) {
 				
 				return Long.compare(extractTimeStamp(file2),  extractTimeStamp(file1));
 			}
 		});
 		return autoSaves;
 	}
 	
 	/**
 	 * Extracts the database last change timestamp from the autosave file. Requires the correct format of the name.
 	 * @param autoSaveFile
 	 * @return A long integer representing the last change of the database.
 	 */
 	private static long extractTimeStamp(File autoSaveFile) {
 		try {
 			String fileName = autoSaveFile.getCanonicalFile().getName();
 			if ( !fileName.matches(autoSaveFileRegex) ) {
 				return -1;
 			}
 			try {
 				long databaseChangeTimeStamp =Long.parseLong(fileName.substring(fileName.indexOf("_")+1, fileName.indexOf(".")));
 				return databaseChangeTimeStamp;
 			} catch (NumberFormatException e) {
 				e.printStackTrace();
 				return -1;
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 			return -1;
 		}
 	}
 	
 	/**
 	 * Creates an automatic backup when the current state of the database is newer than the most recent autosave.  
 	 * To reduce the memory footprint of the backup (i.e. in case of a large amount of pictures) only the db file is
 	 * backed up. 
 	 * @return True when the last state of the database is saved i.e. either the backup was 
 	 * saved successfully or it was older or on par to the last autosave which does nothing. False when the operation
 	 * encountered an error.
 	 */
 	public static boolean backupAutoSave() {		
 		// TODO: implement programm version.
 		String programVersion = ""; 		
 		// TODO: convert timestamp to UTC to compensate for backup/restores across timezones
 		String timeStamp = Long.toString(getLastDatabaseChangeTimeStamp());		
 		
 		String autoSaveFilePath = 	FileSystemAccessWrapper.COLLECTOR_HOME_APPDATA + 
 				File.separator +
 				"periodicalBackup"+
 				programVersion+
 				"_";// separator for the timestamp	
 
 		List<File> previousAutoSaveList = getAllAutoSaves();
 		
 		if(autoSaveLimit<1) {			
 			return true;
 		};
 
 		if (previousAutoSaveList.isEmpty()) {
 			// When no changes were made then the timestamp is the current time
 			if (getLastDatabaseChangeTimeStamp() == -1) {
 				timeStamp = Long.toString(System.currentTimeMillis());
 			}
 			autoSaveFilePath = autoSaveFilePath +timeStamp+"."+ autoSaveExtension;
 			try {
 				FileSystemAccessWrapper.copyFile(new File(FileSystemAccessWrapper.DATABASE), new File(autoSaveFilePath));
 			} catch (IOException e) {
 				System.err.println("Autosave - backup failed");
 				return false;
 			}
 		}else {// Autosaves detected
 			
 			// No need to overwrite the last autosave when no changes were made.
 			if (getLastDatabaseChangeTimeStamp() == -1 ) {
 				return true;
 			}
 						
 			// Auto save limit reached, delete the oldest
 			if (previousAutoSaveList.size()>=autoSaveLimit) {
 				File oldestAutoSave = previousAutoSaveList.get(previousAutoSaveList.size()-1);
 				if (oldestAutoSave.exists()) {
 					if (!oldestAutoSave.delete()){
 						System.err.println("Autosave - cannot delete old autosave");
 						return false;
 					}
 				}
 			}
 			autoSaveFilePath = autoSaveFilePath +timeStamp+"."+ autoSaveExtension;
 			
 			try {
 				FileSystemAccessWrapper.copyFile(new File(FileSystemAccessWrapper.DATABASE), new File(autoSaveFilePath));
 			} catch (IOException e) {
 				System.err.println("Autosave - backup failed");
 				return false;
 			}
 		}
 
 		return true;
 	}
 	
 	private static void createAlbumMasterTableIfNotExits() throws SQLException  {
 		StringBuilder sb = new StringBuilder("CREATE TABLE IF NOT EXISTS ");
 		sb.append(albumMasterTableName);
 		sb.append(" ( ");
 		sb.append(ID_COLUMN_NAME);
 		sb.append(" INTEGER PRIMARY KEY");
 		
 		// Add the table name column
 		sb.append(" , ");
 		sb.append(ALBUM_TABLENAME_IN_ALBUM_MASTER_TABLE);
 		sb.append(" TEXT ");
 		
 		// Add the table's type info table name column
 		sb.append(" , ");
 		sb.append(TYPE_TABLENAME_ALBUM_MASTER_TABLE);
 		sb.append(" TEXT )");
 		
 		String createAlbumMasterableString = sb.toString();
 
 		// Create the album master table.
 		PreparedStatement preparedStatement = connection.prepareStatement(createAlbumMasterableString);
 		preparedStatement.executeUpdate();
 		preparedStatement.close();
 	}
 	
 	private static boolean addNewAlbumToAlbumMasterTable(String albumTableName, String albumTypeInfoTableName) {		
 		StringBuilder sb = new StringBuilder("INSERT INTO ");	
 		sb.append(encloseNameWithQuotes(albumMasterTableName));
 		sb.append(" (");
 		sb.append(ALBUM_TABLENAME_IN_ALBUM_MASTER_TABLE);
 		sb.append(", ");
 		sb.append(TYPE_TABLENAME_ALBUM_MASTER_TABLE);
 		sb.append(") VALUES( ?, ?)");
 		
 		String registerNewAlbumToAlbumMasterableString = sb.toString();
 		PreparedStatement preparedStatement = null;
 		boolean success = true;
 		
 		try {
 			preparedStatement = connection.prepareStatement(registerNewAlbumToAlbumMasterableString);
 			// New album name
 			preparedStatement.setString(1, removeEnclosingNameWithQuotes(albumTableName));
 			// New type info name
 			preparedStatement.setString(2, removeEnclosingNameWithQuotes(albumTypeInfoTableName));
 			preparedStatement.executeUpdate();
 		} catch (SQLException e) {
 			// TODO log
 			success = false;
 		} finally {
 			try {
 				if (preparedStatement != null) {
 					preparedStatement.close();
 				}
 			} catch (SQLException e) {
 				// TODO log
 				success = false;
 			}			
 		}
 		return success;
 	}
 	
 	private static boolean removeAlbumFromAlbumMasterTable(String albumTableName)  {
 		StringBuilder sb = new StringBuilder("DELETE FROM ");	
 		sb.append(encloseNameWithQuotes(albumMasterTableName));
 		sb.append(" WHERE ");
 		sb.append(ALBUM_TABLENAME_IN_ALBUM_MASTER_TABLE);
 		sb.append(" = ?");
 		
 		String unRegisterNewAlbumFromAlbumMasterableString = sb.toString();
 		PreparedStatement preparedStatement = null;
 		boolean success = true;
 				
 		try {
 			preparedStatement = connection.prepareStatement(unRegisterNewAlbumFromAlbumMasterableString);
 			// WHERE album name
 			preparedStatement.setString(1, albumTableName);
 			preparedStatement.executeUpdate();
 			
 		} catch (SQLException e) {
 			// TODO: log
 			success = false;
 		}finally {
 			try {
 				if (preparedStatement != null) {
 					preparedStatement.close();
 				}
 			} catch (SQLException e) {
 				// TODO log
 				success = false;
 			}
 		}
 		return success;		
 	}
 	
 	private static boolean modifyAlbumInAlbumMasterTable(String oldAlbumTableName, String newAlbumTableName, String newAlbumTypeInfoTableName)  {
 		
 		StringBuilder sb = new StringBuilder("UPDATE ");		
 		sb.append(albumMasterTableName);
 		sb.append(" SET ");
 		sb.append(ALBUM_TABLENAME_IN_ALBUM_MASTER_TABLE);
 		sb.append(" = ?, ");
 		sb.append(TYPE_TABLENAME_ALBUM_MASTER_TABLE);
 		sb.append(" = ? WHERE ");
 		sb.append(ALBUM_TABLENAME_IN_ALBUM_MASTER_TABLE);
 		sb.append(" = ?");
 		
 		String unRegisterNewAlbumFromAlbumMasterableString = sb.toString();
 		PreparedStatement preparedStatement = null;
 		boolean success = true;
 				
 		try {
 			preparedStatement = connection.prepareStatement(unRegisterNewAlbumFromAlbumMasterableString);
 			// New album name
 			preparedStatement.setString(1, newAlbumTableName);
 			// New type info name
 			preparedStatement.setString(2, newAlbumTypeInfoTableName);
 			// Where old album name
 			preparedStatement.setString(3, oldAlbumTableName);
 			preparedStatement.executeUpdate();
 		} catch (SQLException e) {
 			// TODO: log
 			success = false;
 		}finally {
 			try {
 				if (preparedStatement != null) {
 					preparedStatement.close();
 				}
 			} catch (SQLException e) {
 				// TODO log
 				success = false;
 			}
 		}
 		return success;		
 	}
 
 	private static void createTableWithIdAsPrimaryKey(String tableName, List<MetaItemField> fields, boolean temparyTable, boolean ifNotExistsClause) throws SQLException  {
 			StringBuilder sb = new StringBuilder("CREATE ");
 			if (temparyTable) {
 				sb.append("TEMPORARY ");
 			}
 			
 			sb.append("TABLE ");
 			if (ifNotExistsClause) {
 				sb.append("IF NOT EXISTS "); 
 			}
 			
 			sb.append(encloseNameWithQuotes(tableName));
 			sb.append(" ( ");
 			sb.append(ID_COLUMN_NAME);
 			sb.append(" INTEGER PRIMARY KEY");
 			
 			for (MetaItemField item : fields) {
 				sb.append(" , ");
 				sb.append(encloseNameWithQuotes(item.getName()));	// " , 'fieldName'"  
 				sb.append(" ");										// " , 'fieldName' " 
 				sb.append(item.getType().toDatabaseTypeString());	// " , 'fieldName' TYPE"
 			}
 			
 			sb.append(" , parentItem INTEGER )");
 
 			String createTableString = sb.toString();
 
 			// Create the table.
 			PreparedStatement preparedStatement = connection.prepareStatement(createTableString);
 			preparedStatement.executeUpdate();
 			preparedStatement.close();
 		}
 }
