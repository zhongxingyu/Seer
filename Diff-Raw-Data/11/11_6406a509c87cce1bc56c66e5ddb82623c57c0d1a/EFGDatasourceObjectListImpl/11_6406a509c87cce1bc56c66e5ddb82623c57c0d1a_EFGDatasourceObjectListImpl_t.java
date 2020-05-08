 /**
  * EFGDatasourceObjectLists.java
  *
  ** $Id: EFGDatasourceObjectListImpl.java,v 1.1.1.1 2007/08/01 19:11:17 kasiedu Exp $
  * $Name:  $
  * 
  * @author <a href="mailto:kasiedu@cs.umb.edu">Jacob K Asiedu</a>
  *
  * This file is part of the UMB Electronic Field Guide.
  * UMB Electronic Field Guide is free software; you can redistribute it
  * and/or modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2, or
  * (at your option) any later version.
  *
  * UMB Electronic Field Guide is distributed in the hope that it will be
  * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with the UMB Electronic Field Guide; see the file COPYING.
  * If not, write to:
  * Free Software Foundation, Inc.
  * 59 Temple Place, Suite 330
  * Boston, MA 02111-1307
  * USA
  * Created: Sun Feb 19 09:22:49 2006
  *
  */
 package project.efg.client.rdb.nogui;
 
 import java.net.URI;
 import java.sql.DatabaseMetaData;
 import java.sql.SQLException;
 import java.util.List;
 
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.jdbc.datasource.DataSourceTransactionManager;
 import org.springframework.jdbc.support.DatabaseMetaDataCallback;
 import org.springframework.jdbc.support.JdbcUtils;
 
 import project.efg.client.factory.gui.SpringGUIFactory;
 import project.efg.client.factory.nogui.NoGUIFactory;
 import project.efg.client.interfaces.gui.EFGDatasourceObjectListInterface;
 import project.efg.client.interfaces.gui.ImportBehavior;
 import project.efg.client.interfaces.nogui.CSV2DatabaseAbstract;
 import project.efg.client.interfaces.nogui.EFGDataExtractorInterface;
 import project.efg.client.interfaces.nogui.EFGDatasourceObjectInterface;
 import project.efg.client.utils.nogui.FlushServerCache;
 import project.efg.client.utils.nogui.LoggerUtils;
 import project.efg.util.factory.SpringFactory;
 import project.efg.util.interfaces.EFGImportConstants;
 import project.efg.util.interfaces.EFGQueueObjectInterface;
 import project.efg.util.interfaces.EFGRowMapperInterface;
 import project.efg.util.utils.DBObject;
 import project.efg.util.utils.EFGRDBImportUtils;
 import project.efg.util.utils.EFGUtils;
 import project.efg.util.utils.TemplateMapObjectHandler;
 
 public class EFGDatasourceObjectListImpl extends
 		EFGDatasourceObjectListInterface {
 
 	private JdbcTemplate jdbcTemplate;
 	
 	private DataSourceTransactionManager txManager;
 
 	private EFGRowMapperInterface rowMapper;
 	private String mainTableName;
 	
 	//private String mapLocation;
 	
 	public EFGDatasourceObjectListImpl(DBObject dbObject) {
 		super(dbObject);
 		
 		this.mainTableName = EFGUtils.getCurrentTableName();
 		this.txManager = this.getTransactionManager(this.dbObject);
 		this.jdbcTemplate = this.getJDBCTemplate(this.dbObject);
 		//this.rowMapper = new EFGRowMapperImpl();
 		this.rowMapper = SpringFactory.getRowMapper();
 		this.populateLists();
 	}
 
 	/**
 	 * Remove an object from the database
 	 * 
 	 * @param datasource -
 	 *            The datasource object to remove
 	 * @return true if the datasource object was part of the list and was
 	 *         removed , false otherwise.
 	 */
 	public boolean removeEFGDatasourceObject(
 			EFGDatasourceObjectInterface datasource) {
 		return removeEFGDatasourceObject(datasource.getDisplayName());
 	}
 
 	/**
 	 * Remove an object with the given display name from the database
 	 * 
 	 * @param datasource -
 	 *            The datasource object to remove
 	 * @return true if the datasource object was part of the list and was
 	 *         removed , false otherwise.
 	 */
 	public boolean removeEFGDatasourceObject(String displayName) {
 		return this.deleteTable(displayName);
 	}
 
 	/**
 	 * Add an object to the database
 	 * 
 	 * @param datasource -
 	 *            The datasource object to add
 	 * @param isUpdate -
 	 *            true,If there is an update to a data table.
 	 * @return true if this datasource was successfully added, false otherwise
 	 */
 	public boolean addEFGDatasourceObject(
 			EFGDatasourceObjectInterface datasource, 
 			ImportBehavior isUpdate) {
 
 		try {
 	
 			String delimiter = EFGImportConstants.EFGProperties.getProperty(
 					"delimiter").trim();
 			char[] delimArr = delimiter.toCharArray();
 			// log.debug("After char delimiter");
 			EFGDataExtractorInterface extractor = NoGUIFactory
 					.getDataExtractor(datasource.getDataName(), delimArr[0]);
 		
 			// use abstract method call
 
 			CSV2DatabaseAbstract table = NoGUIFactory
 					.getDatabaseObject(datasource, extractor, dbObject,
 							isUpdate);
			
 			boolean bool = table.import2Database();
 			if (bool) {
 				// add only if it is not already present
 				// if the object already exists then replace it
 				int find = this.findObjectIndex(datasource.getDisplayName());
 				if (find > -1) {
 					this.lists.remove(find);
 				}
 				this.lists.add(datasource);
 
 				datasource.setState(SpringGUIFactory.getSuccessObject());
 				return true;
 			}
 		} catch (Exception ee) {
 			LoggerUtils.logErrors(ee);
 		}
 		datasource.setState(SpringGUIFactory.getFailureObject());
 		return false;
 	}
 
 
 
 	/**
 	 * @param oldDisplayName -
 	 *            the display name to replace.
 	 * @param freshDisplayName-
 	 *            the display name tio be used to replace the old one.
 	 * @return true if the display name for the object is successfully changed.
 	 */
 	public boolean replaceDisplayName(String oldDisplayName,
 			String freshDisplayName) {
 		try {
 
 			int index = this.findObjectIndex(oldDisplayName);
 			if (index > -1) {
 				EFGDatasourceObjectInterface obj = (EFGDatasourceObjectInterface) this.lists
 						.get(index);
 				StringBuffer queryBuffer = new StringBuffer("SELECT DS_DATA FROM ");
 				queryBuffer.append(this.mainTableName);
 				queryBuffer.append(" WHERE DISPLAY_NAME = \"");
 				queryBuffer.append(oldDisplayName);
 				queryBuffer.append("\"");
 				java.util.List list = this.executeQueryForList(queryBuffer.toString(), 1);
 				String datafn = null;
 				for (java.util.Iterator iter = list.iterator(); iter.hasNext();) {
 					EFGQueueObjectInterface queue = (EFGQueueObjectInterface) iter
 							.next();
 					datafn = queue.getObject(0);
 				}
 				
 				String query = "UPDATE " + this.mainTableName
 						+ " SET DISPLAY_NAME = \"" + freshDisplayName + "\" "
 						+ "WHERE DISPLAY_NAME = \"" + oldDisplayName + "\"";
 				// will hold the query
 				
 				this.executeStatement(query);
 				obj.setDisplayName(freshDisplayName);
 //				 get the row from the mainTableName if it exists
 				
 				if(datafn == null){
 					throw new Exception("Could not change display Names for : '" + 
 							oldDisplayName  + "' " + " to '" + freshDisplayName + "'");
 				}
 			
 				boolean bool = TemplateMapObjectHandler.changeDisplayName(datafn,freshDisplayName,this.dbObject);
 			
 				if(!bool){
 					throw new Exception("Could not change display Names for : '" + 
 							oldDisplayName  + "' " + " to '" + freshDisplayName + "'");
 				}
 				else{
 					FlushServerCache.flushServerCache(datafn, 
 							this.mainTableName);
 				}
 				return true;
 			} 
 
 		} catch (Exception ee) {
 			LoggerUtils.logErrors(ee);
 
 		}
 		return false;
 	}
 
 	/**
 	 * @param object
 	 * @return
 	 */
 	private DataSourceTransactionManager getTransactionManager(DBObject dbObject) {
 		if (this.txManager == null) {
 			this.txManager = EFGRDBImportUtils.getTransactionManager(dbObject);
 		}
 		return this.txManager;
 	}
 
 	private int executeStatement(String query) throws Exception {
 		return EFGRDBImportUtils.executeStatement(this.txManager,
 				this.jdbcTemplate, query);
 	}
 
 	private List executeQueryForList(String query, int numberOfColumns)
 			throws Exception {
 		return this.rowMapper
 				.mapRows(this.jdbcTemplate, query, numberOfColumns);
 	}
 
 	private JdbcTemplate getJDBCTemplate(DBObject dbObject) {
 		if (this.jdbcTemplate == null) {
 			// log.debug("Creating new Template");
 			this.jdbcTemplate = EFGRDBImportUtils.getJDBCTemplate(dbObject);
 		}
 		return this.jdbcTemplate;
 	}
 
 	/**
 	 * Check if a Table exist in Database.
 	 * 
 	 * @param tableName
 	 *            the name of the table
 	 * @return true if the table exists, false otherwise FIX ME
 	 */
 	private boolean isExistTable(String tableName) {
 		final String tabName = tableName;
 		try {
 			java.sql.ResultSet rst = (java.sql.ResultSet) JdbcUtils
 					.extractDatabaseMetaData(this.jdbcTemplate.getDataSource(),
 							new DatabaseMetaDataCallback() {
 								public Object processMetaData(
 										DatabaseMetaData dbmd)
 										throws SQLException {
 									String[] types = { "TABLE" };
 									return dbmd.getTables(null, null, tabName,
 											types);
 								}
 							});
 			if (rst.next()) {
 				rst.close();
 				return true;
 			}
 		} catch (Exception ex) {
 			LoggerUtils.logErrors(ex);
 		}
 		return false;
 	}
 
 	/**
 	 * Populate the lists with objects from the database
 	 */
 	private void populateLists() {
 		if (!isExistTable(this.mainTableName)) {
 			return;
 		}
 
 		try {
 			// check if there is a table with that name
 			StringBuffer query = new StringBuffer();
 			query.append("SELECT DS_METADATA, ORIGINAL_FILE_NAME,"
 					+ " DISPLAY_NAME FROM ");
 			query.append(this.mainTableName);
 			List list = EFGRDBImportUtils.executeQueryForList(
 					this.jdbcTemplate, query.toString(), 3);
 
 			
 			for (java.util.Iterator iter = list.iterator(); iter.hasNext();) {
 				EFGQueueObjectInterface queue = (EFGQueueObjectInterface) iter
 						.next();
 				String md_name = queue.getObject(0);
 				String name = queue.getObject(1);
 				String display = queue.getObject(2);
 					
 				EFGDatasourceObjectInterface doInterface = NoGUIFactory
 						.getEFGDatasourceObject(URI.create(EFGUtils
 								.reverseParseEFGSEP(name)), md_name, display);
 				this.lists.add(doInterface);
 			}
 		} catch (Exception ee) {
 			LoggerUtils.logErrors(ee);
 		}
 	}
 
 	private boolean delete(String tableName) throws Exception {
 		if ((tableName == null) || (tableName.trim().equals(""))) {
 			// log.error("Display name must not be null or the empty String");
 			return false;
 		}
 		this.executeStatement("DROP TABLE IF EXISTS " + tableName);
 
 		return true;
 	}
 
 	
 	
 	/**
 	 * 
 	 * @param datafn
 	 *            the data fileName to check .
 	 * @param metadatafn
 	 *            the metadata fileName to check .
 	 * @return true if delete was successful.
 	 */
 	private boolean deleteTable(String displayName) {
 		if ((displayName == null) || (displayName.trim().equals(""))) {
 			// log.error("Display name must not be null or the empty String");
 			return false;
 		}
 
 		boolean isDone = false;
 		StringBuffer query = null;
 		try {
 			// get the row from the mainTableName if it exists
 			query = new StringBuffer("SELECT DS_DATA, DS_METADATA FROM ");
 			query.append(this.mainTableName);
 			query.append(" WHERE DISPLAY_NAME = \"");
 			query.append(displayName);
 			query.append("\"");
 
 			java.util.List list = this.executeQueryForList(query.toString(), 2);
 			for (java.util.Iterator iter = list.iterator(); iter.hasNext();) {
 				EFGQueueObjectInterface queue = (EFGQueueObjectInterface) iter
 						.next();
 				String datafn = queue.getObject(0);
 				String metadatafn = queue.getObject(1);
 				this.delete(datafn);
 				this.delete(metadatafn);
 
 				// remove the row from the mainTableName
 				String qr = "DELETE FROM " + this.mainTableName
 						+ " WHERE DS_DATA = \"" + datafn + "\";";
 				this.executeStatement(qr);
 
 				int index = this.findObjectIndex(displayName);
 				if (index > -1) {
 					this.lists.remove(index);
 				}
 				// also delete the file if it exists
 				boolean bool = false;
 				
 				try{
 					bool = TemplateMapObjectHandler.removeFromTemplateMap(datafn,this.dbObject);
 					
 					if(!bool){
 						throw new Exception(
 								"Application could not delete the template configurations ");
 									
 					}
 					else{//flush server cache. If server cannot be found
 						//Tomcat has to be restarted
 						FlushServerCache.flushServerCache(datafn, 
 								this.mainTableName);
 					}
 				}
 				catch(Exception eex){
 					LoggerUtils.logErrors(eex);
 				}
 				isDone = true;
 			}
 		} catch (Exception ex) {
 			LoggerUtils.logErrors(ex);
 		}
 
 		return isDone;
 	}
 
 }// EFGDatasourceObjectList
 
