 /** -----------------------------------------------------------------
  *    Sammelbox: Collection Manager - A free and open-source collection manager for Windows & Linux
  *    Copyright (C) 2011 Jerome Wagener & Paul Bicheler
  *
  *    This program is free software: you can redistribute it and/or modify
  *    it under the terms of the GNU General Public License as published by
  *    the Free Software Foundation, either version 3 of the License, or
  *    (at your option) any later version.
  *
  *    This program is distributed in the hope that it will be useful,
  *    but WITHOUT ANY WARRANTY; without even the implied warranty of
  *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *    GNU General Public License for more details.
  *
  *    You should have received a copy of the GNU General Public License
  *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
  ** ----------------------------------------------------------------- */
 
 package org.sammelbox.model.album;
 
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.sammelbox.model.database.exceptions.DatabaseWrapperOperationException;
 import org.sammelbox.model.database.exceptions.DatabaseWrapperOperationException.DBErrorState;
 import org.sammelbox.model.database.operations.DatabaseConstants;
 import org.sammelbox.model.database.operations.DatabaseOperations;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class AlbumItemResultSet {	
 	private static final Logger LOGGER = LoggerFactory.getLogger(AlbumItemResultSet.class);
 	
 	private ResultSet items;
 	private String albumName = "";
 	private ResultSetMetaData metaData = null;
 	private Map<Integer, MetaItemField> metaInfoMap = new HashMap<Integer, MetaItemField>();
 	
 	/**
 	 * Constructor.
 	 * @param connection The jdbc connection used to access the actual database.
 	 * @param sqlStatement The SQL statement to yield the result set. Must be formatted properly.
 	 * @throws DatabaseWrapperOperationException 
 	 */
 	public AlbumItemResultSet(Connection connection, String sqlStatement) throws DatabaseWrapperOperationException {
 		Statement statement = null;
 		try {
 			statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY,ResultSet.CONCUR_READ_ONLY);
 			this.items = statement.executeQuery(sqlStatement);
 			this.metaData = items.getMetaData();
 			this.albumName = DatabaseOperations.getAlbumName(metaData.getTableName(1));			
 			this.metaInfoMap = DatabaseOperations.getAlbumItemMetaMap(albumName);
 		} catch (SQLException sqlException) {
 			throw new DatabaseWrapperOperationException(DBErrorState.ERROR_CLEAN_STATE, sqlException);
 		}		
 	}	
 	
 	/**
 	 * Constructor. Convenience method which allows to specify meta data explicitly.
 	 * @param connection The jdbc connection used to access the actual database.
 	 * @param sqlStatement The SQL statement to yield the result set. Must be formatted properly.
 	 * @param metaInfoMap A map containing all the metadata of the fields.
 	 * @throws DatabaseWrapperOperationException Exception which will be thrown in case anything went wrong while creating the result set.
 	 */
 	public AlbumItemResultSet(Connection connection, String sqlStatement, Map<Integer, MetaItemField> metaInfoMap) throws DatabaseWrapperOperationException {
 		this.metaInfoMap = metaInfoMap;
 		Statement statement = null;
 		try {
 			statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
 			this.items = statement.executeQuery(sqlStatement);
 			this.metaData = items.getMetaData();
 			this.albumName = DatabaseOperations.getAlbumName(metaData.getTableName(1));	
 		} catch (SQLException e) {
 			throw new DatabaseWrapperOperationException(DBErrorState.ERROR_CLEAN_STATE, e);
 		}
 	}
 	
 	/**
 	 * Constructor. Convenience method which allows to specify the album name and meta data explicitly. 
 	 * Use this method if the album name cannot be determined from the resultset's meta data
 	 * @param albumName The name of the album for which a resultset should be created
 	 * @param connection The jdbc connection used to access the actual database.
 	 * @param sqlStatement The SQL statement to yield the result set. Must be formatted properly.
 	 * @param metaInfoMap A map containing all the metadata of the fields.
 	 * @throws SQLException Exception which will be thrown in case anything went wrong while creating the result set.
 	 */
 	public AlbumItemResultSet(Connection connection, String albumName, String sqlStatement, Map<Integer, MetaItemField> metaInfoMap) throws DatabaseWrapperOperationException {
 		this.metaInfoMap = metaInfoMap;
 		Statement statement = null;
 		try {
 			statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
 			this.items = statement.executeQuery(sqlStatement);
 			this.metaData = items.getMetaData();
 			this.albumName = albumName;
 		} catch (SQLException e) {
 			throw new DatabaseWrapperOperationException(DBErrorState.ERROR_CLEAN_STATE, e);
 		}
 	}
 
 	/**
 	 * Getter for the album name to which this result set refers to.
 	 * @return The name of the album.
 	 */
 	public String getAlbumName() {
 		return albumName;
 	}
 
 	/**
 	 * Setter for the album name to which this result set refers to.
 	 * @param albumName The new album name.
 	 */
 	public void setAlbumName(String albumName) {
 		this.albumName = albumName;
 	}
 
 	/**
 	 * Moves the cursor to the next position and indicates whether such a position exits.
 	 * @return True if an advance in position yields another element. False otherwise.
 	 */
 	public boolean moveToNext() {
 		try {			
 			return items.next();
 		} catch (SQLException e) {
 			LOGGER.error("Cannot move to the next item of the album item result set", e);
 			return false;
 		}
 	}
 	
 	/**
 	 * Gets the number of fields for the current item.
 	 * @return An integer specifying the number of fields.
 	 */
 	public int getFieldCount() {
 		// Retrieve table metadata
 		try {
 			if (metaData != null) {
 				return metaData.getColumnCount();
 			}
 		} catch (SQLException e) {
 			LOGGER.error("An error occured while retrieving the field could from the album item result set");
 		}
 
 		return 0;
 	}
 
 	/**
 	 * Get the value of the specified field. It is attempted to be cast into the specified type T. 
 	 * @param fieldIndex The index under which the field value is stored.
 	 * @return The value of this field.
 	 * @throws DatabaseWrapperOperationException 
 	 */
 	@SuppressWarnings("unchecked")
 	public <T> T getFieldValue(int fieldIndex) throws DatabaseWrapperOperationException {
 		FieldType type =  metaInfoMap.get(fieldIndex).getType();
 		Object outValue = null;
 		try {
 			outValue = DatabaseOperations.fetchFieldItemValue(items, fieldIndex, type, albumName);
 			return (T)outValue;
 		} catch (DatabaseWrapperOperationException e) {
 			LOGGER.error("Fetching the field value for the index {} failed", fieldIndex);
 			throw new DatabaseWrapperOperationException(DBErrorState.ERROR_CLEAN_STATE, e);
 		}		
 	}
 
 	/**
 	 * Getter for a field name. 
 	 * @param fieldIndex The index position under which the item is stored.
 	 * @return The name of the field or null if the index is invalid.
 	 */
 	public String getFieldName(int fieldIndex) {
 		MetaItemField field = metaInfoMap.get(fieldIndex);
 		if (field == null) {
 			return null;
 		}
 		String name = field.getName();
 
 		return name;
 	}
 
 	/**
 	 * Getter for the field type.
 	 * @param fieldIndex The index position under which the item is stored.
 	 * @return The type of the field or null if the index is invalid.
 	 */
 	public FieldType getFieldType(int fieldIndex) {
 		MetaItemField field = metaInfoMap.get(fieldIndex);
 		if (field == null) {
 			return null;
 		}
 		FieldType type =  field.getType();
 
 		return type;
 	}
 	
 	/**
 	 * Indicates if the specified field index points to an Id field.
 	 * @param fieldIndex The index to be checked.
 	 * @return True if the field is of type FieldType.ID. False otherwise.
 	 */
 	public boolean isItemID(int fieldIndex) {
 		if (getFieldName(fieldIndex).equals(DatabaseConstants.TYPE_INFO_COLUMN_NAME)) {
 			return false;
 		}
 		return true; 
 	}
 	
 	public boolean isItemUUID(int fieldIndex) {
 		if (getFieldType(fieldIndex).equals(FieldType.UUID)) {
 			return true;
 		}
 		return false;
 	}
 	
 	/**
 	 * Closes the result set and ensures that allocated resources are freed properly-
 	 * @return true if the closing was successfully, false otherwise.
 	 */
 	public boolean close() {
 		try {
 			items.close();
 		} catch (SQLException e) {			
 			LOGGER.error("An error occured while closing the album item result set");
 			return false;
 		}
 		return true;
 	}
 }
