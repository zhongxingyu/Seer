 package project.efg.client.rdb.nogui;
 
 import java.io.StringReader;
 import java.io.StringWriter;
 import java.net.URI;
 import java.util.Arrays;
 import java.util.Comparator;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.jdbc.datasource.DataSourceTransactionManager;
 import org.springframework.transaction.TransactionDefinition;
 import org.springframework.transaction.TransactionStatus;
 import org.springframework.transaction.support.DefaultTransactionDefinition;
 
 import project.efg.client.factory.nogui.SpringNoGUIFactory;
 import project.efg.client.impl.gui.ImportBehaviorImplReplace;
 import project.efg.client.impl.gui.ImportBehaviorImplUpdate;
 import project.efg.client.impl.gui.ImportBehaviorImplUseExisting;
 import project.efg.client.impl.nogui.ImportBehaviorImplNew;
 import project.efg.client.interfaces.gui.ImportBehavior;
 import project.efg.client.interfaces.nogui.CSV2DatabaseAbstract;
 import project.efg.client.interfaces.nogui.EFGDataExtractorInterface;
 import project.efg.client.interfaces.nogui.EFGDatasourceObjectInterface;
 import project.efg.client.utils.nogui.FlushServerCache;
 import project.efg.templates.taxonPageTemplates.TaxonPageTemplates;
 import project.efg.util.interfaces.EFGImportConstants;
 import project.efg.util.interfaces.EFGQueueObjectInterface;
 import project.efg.util.utils.DBObject;
 import project.efg.util.utils.EFGRDBImportUtils;
 import project.efg.util.utils.EFGUniqueID;
 import project.efg.util.utils.EFGUtils;
 import project.efg.util.utils.TaxonPageDefaultConfig;
 import project.efg.util.utils.TemplateMapObjectHandler;
 import project.efg.util.utils.UnicodeToASCIIFilter;
 
 /**
  * CSV2Database.java
  * 
  * $Id: CSV2Database.java,v 1.1.1.1 2007/08/01 19:11:17 kasiedu Exp $
  * 
  * Created: Tue Feb 28 13:14:19 2006
  * 
  * @author <a href="mailto:kasiedu@cs.umb.edu">Jacob K Asiedu</a>
  * 
  * This file is part of the UMB Electronic Field Guide. UMB Electronic Field
  * Guide is free software; you can redistribute it and/or modify it under the
  * terms of the GNU General Public License as published by the Free Software
  * Foundation; either version 2, or (at your option) any later version.
  * 
  * UMB Electronic Field Guide is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
  * Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * the UMB Electronic Field Guide; see the file COPYING. If not, write to: Free
  * Software Foundation, Inc. 59 Temple Place, Suite 330 Boston, MA 02111-1307
  * USA Imports a csv file into a relational database
  * 
  */
 public class CSV2Database extends CSV2DatabaseAbstract {
 
 	private JdbcTemplate jdbcTemplate;
 	private String[] dataHeaders;
 
 	private String metadataTableName; // metadata table to use
 
 	private String tableName;
 
 	private Comparator compare;
 
 	// read from an external file
 	private String efgRDBTable;
 
 	private DataSourceTransactionManager txManager;
 
 	static Logger log = null;
 	static {
 		try {
 			log = Logger.getLogger(CSV2Database.class);
 		} catch (Exception ee) {
 		}
 	}
 	private String[] lowerCaseHeaders(String[] lists){
 		String[] lowercaseHeaders = new String[lists.length];
 		
 		for(int i = 0;i < lists.length; i++){
 			lowercaseHeaders[i] = lists[i].trim().toLowerCase();
 		}
 		return lowercaseHeaders;
 	}
 	public  void setDatasource(EFGDatasourceObjectInterface datasource) {
 		super.setDatasource(datasource);
 	}
 	public  void setDataExtractor(EFGDataExtractorInterface dataExtractor) {
 		super.setDataExtractor(dataExtractor);
 		try {
 			this.dataHeaders = this.dataExtractor.getFieldNames();
 		} catch (Exception e) {
 			
 			log.error(e.getMessage());
 		}
 		
 	}
 	public  void setDBObject(DBObject dbObject) {
 		super.setDBObject(dbObject);
 		this.txManager = this.getTransactionManager(this.dbObject);
 		this.jdbcTemplate = this.getJDBCTemplate(this.dbObject);
 	}
 	public  void setISUpdate(ImportBehavior isUpdate) {
 		super.setISUpdate(isUpdate);
 	}
 	public CSV2Database() {
 	
 
 		try {
 			//this.dataHeaders = this.dataExtractor.getFieldNames();
 		} catch (Exception e) {
 			
 			log.error(e.getMessage());
 		}
 		log.debug("After get header");
 		//this.txManager = this.getTransactionManager(this.dbObject);
 	//	this.jdbcTemplate = this.getJDBCTemplate(this.dbObject);
 		// READ FROM PROPERTIES FILE
 		this.compare = SpringNoGUIFactory.getComparator("caseinsensitive_comparator");
 		
 		
 		this.efgRDBTable = EFGUtils.getCurrentTableName();
 	}
 	/**
 	 * @return true if import was successful. false otherwise
 	 */
 	public boolean import2Database() {
 
 		log.debug("File Name : " + this.datasource.getDataName());
 
 		boolean isSuccess = true;
 		this.format2efg();
 
 		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
 		def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
 
 		TransactionStatus status = this.txManager.getTransaction(def);
 		try {
 
 			if (isSuccess) {
 				log.debug("About to import: "
 						+ this.datasource.getDataName().toString());
 
 				if (this.isUpdate instanceof ImportBehaviorImplUpdate)
 			  {// we
 																						// are
 																						// doing
					//warn here																	// updates
 					log.debug("About to run an update");
 
 					isSuccess = this.createOrUpdateEFGTable();
 
 					if (isSuccess) {// replace table
 						isSuccess = this.createDataTable();
 						if (isSuccess) {
 							log.debug("Updates run successfully");
 							// do nothing
 						}
 					}
 				} else if ((this.isUpdate instanceof ImportBehaviorImplNew) || 
 						(this.isUpdate instanceof ImportBehaviorImplUseExisting)){// we
 																			// are
 																			// not
 																			// doing
 																			// updates
 					this.tableName = this.generateTableName(this.datasource
 							.getDataName());
 					if ((this.tableName == null)
 							|| (this.tableName.trim().equals(""))) {// error
 						// message
 						// table
 						// name
 						// could not
 						// be
 						// generated
 						log
 								.error("Table name must not be null or the empty string!!!");
 						this.dataExtractor.close(); 
 						return false;
 					}
 					if ((this.getDisplayName() == null)
 							|| this.getDisplayName().trim().equals("")) {
 						this.datasource.setDisplayName(this.tableName);
 						log.debug("A new display name '"
 								+ this.getDisplayName()
 								+ " was created successfully");
 					}
 					log.debug("About to create Data table");
 					isSuccess = this.createDataTable();
 
 					if (isSuccess) {// data table created successfully
 						log
 								.debug("A Data table "
 										+ this.tableName
 										+ " was created successfully out of the file: '"
 										+ this.datasource.getDataName()
 												.toString() + "'!!");
 
 						this.metadataTableName = this.createMetadataTableName();
 						if (this.metadataTableName == null) {
 							log
 									.error("System could not create a metadata table for : "
 											+ this.datasource.getDataName()
 													.toString());
 							this.txManager.rollback(status);
 							this.dataExtractor.close(); 
 							return false;
 						}
 						String[] fieldNames = this.getDataTableHeaders();
 						String[] legalNames = this.lowerCaseHeaders(this.getLegalNames());
 
 						if ((this.datasource.getTemplateDisplayName() == null)
 								|| (this.datasource.getTemplateDisplayName()
 										.trim().equals(""))) {// create a new
 							// metadata
 							// table
 							log.debug("About to create new Metadata table");
 							isSuccess = this.createMetadataTable(fieldNames,
 									legalNames);
 							// create the default Template files for the current
 							// datasource
 						} else {// use an old metadata table as template
 							log
 									.debug("About to clone an existing Metadata table");
 							// find out if this metadata table exists. Quit if
 							// it does not
 
 							EFGQueueObjectInterface metadataTableToClone = this
 									.findMetadataTable(this.datasource
 											.getTemplateDisplayName());
 
 							if (metadataTableToClone == null) {// specified
 								// metadata
 								// table does
 								// not exists
 								log
 										.error("System could not find the requested metadata table with display name: '"
 												+ this.datasource
 														.getTemplateDisplayName()
 												+ "' for the table '"
 												+ this.datasource.getDataName()
 														.toString() + "'");
 								isSuccess = false;
 							} else {// specified metadata table exists. copy it
 								isSuccess = this.cloneMetadataTable(
 										metadataTableToClone.getObject(0),
 										fieldNames, legalNames);
 								if (isSuccess) {// get the template file and
 												// clone it for the current
 												// datasource
 									String dataTableAssociateWithClonedMetadataTable = metadataTableToClone
 											.getObject(1);
 									boolean bool = this
 											.copyClone(dataTableAssociateWithClonedMetadataTable);
 
 									if (!bool) {
 										log
 												.error("Could not create default templates for '"
 														+ this.getDisplayName()
 														+ "'");
 									}
 								}
 								// if isSuccess clone the template files for
 								// this stuff
 							}
 						}
 
 						if (isSuccess) {
 							log.debug("Metadata table: "
 									+ this.metadataTableName
 									+ " was created successfully");
 							log.debug("About to create EFGTable");
 							isSuccess = this.createEFGTable();
 							log
 									.debug("EFGRDBTable was created/updated successfully");
 							TaxonPageDefaultConfig taxonPageConfig = 
 								new TaxonPageDefaultConfig(
 									this.dbObject,  this.efgRDBTable);
 							boolean bool = taxonPageConfig.processNew(this
 									.getDataTableName(), this.getDisplayName());
 							
 							
 							TaxonPageTemplates tps = TemplateMapObjectHandler.getTemplateFromDB(dbObject,
 									this.getDisplayName(), 
 									null, this.efgRDBTable);
 
 						if (!bool) {		
 								log.error("Could not create default templates for '"
 												+ this.getDisplayName()
 												+ "'");
 							}
 						} else {
 							log.error("An error occurred while creating Metadata table '"
 											+ this.metadataTableName + "'!!!");
 							isSuccess = false;
 						}
 					} else {
 						log.error("An error occurred while creating data table '"
 										+ this.tableName + "'!!!");
 						isSuccess = false;
 					}
 				} else if (this.isUpdate instanceof ImportBehaviorImplReplace) {
					//warn here
 					isSuccess = this.updateMetadataEFGTable();
 					if (isSuccess) {// replace table
 						isSuccess = this.createDataTable();
 						if (isSuccess) {
 							log.debug("Updates run successfully");
 						}
 					}
 
 				}
 				else{
 					isSuccess = false;
 					log.error("command not understood!!");
 				}
 			}
 
 			if (!isSuccess) {
 				txManager.rollback(status);
 				log.error("The data contained in "
 						+ this.datasource.getDataName().toString()
 						+ " could not be imported successfully. \n");
 				log
 						.error("Please view the logs for more verbose error messages!!!");
 				this.dataExtractor.close(); 
 				return false;
 			}
 			FlushServerCache.flushServerCache(this.tableName, 
 					this.efgRDBTable);
 
 
 		} catch (Exception ex) {
 			txManager.rollback(status);
 			try {
 				this.dataExtractor.close();
 			} catch (Exception e) {
 				
 			} 
 			ex.printStackTrace();
 			return false;
 		}
 		txManager.commit(status);
 		try {
 			this.dataExtractor.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		} 
 		return isSuccess;
 	}
 
 	/**
 	 * @return the name of the database table created for the current data
 	 */
 	public String getDataTableName() {
 		return this.tableName.toLowerCase();
 	}
 
 	/**
 	 * @return the name metadata table associated with the current imported
 	 *         data.
 	 */
 	public String getMetadataTableName() {
 		return this.metadataTableName.toLowerCase();
 	}
 
 	public int getCount(String metaName){
 		try{
 			String countQuery = "SELECT DISTINCT count(*) FROM " + metaName;
 			java.util.List listNew = this.executeQueryForList(
 					countQuery, 1);
 			String counter = ((EFGQueueObjectInterface) listNew.get(0))
 					.getObject(0);
 			
 			return Integer.parseInt(counter);
 		}
 		catch(Exception eex){
 			
 		}
 		return 0;
 		
 	}
 	private boolean copyClone(String clonedDataTableName) {
 		
 		TaxonPageDefaultConfig taxonPageConfig = 
 			new TaxonPageDefaultConfig(this.dbObject, 
 					this.efgRDBTable);
 		
 		boolean bool = taxonPageConfig.cloneTemplate(clonedDataTableName, this
 				.getDataTableName(), this.getDisplayName());
 		if (!bool) {
 			log.error("Could not create  templates for '"
 					+ this.getDisplayName() + "'");
 		}
 		return bool;
 	}
 
 	
 
 	private DataSourceTransactionManager getTransactionManager(DBObject dbObject) {
 		return EFGRDBImportUtils.getTransactionManager(dbObject);
 	}
 
 	private JdbcTemplate getJDBCTemplate(DBObject dbObject) {
 		if (this.jdbcTemplate == null) {
 
 			this.jdbcTemplate = EFGRDBImportUtils.getJDBCTemplate(dbObject);
 		}
 		return this.jdbcTemplate;
 	}
 
 	/**
 	 * Start processing csv file
 	 */
 	private void format2efg() {
 		try {
 			log.debug(this.compare.getClass().getName());
 			this.legalNames = this.translateHeaders(this.dataHeaders);
 
 		} catch (Exception ee) {
 
 			log.error(" An error occured during importation of : "
 					+ this.datasource.getDataName() + "\n");
 
 		}
 	}
 
 	private String[] getSortedLegalNames() {
 		String[] sortedLegalNames = this.translateHeaders(this.dataHeaders);
 		java.util.Arrays.sort(sortedLegalNames, this.compare);
 		return sortedLegalNames;
 	}
 
 	/**
 	 * 
 	 * @param name
 	 *            the uri to the csv file
 	 * @return a name that could be used as a table name in a relational
 	 *         database
 	 */
 	private String generateTableName(URI name) {
 		if (name == null) {
 			return null;
 		}
 		return EFGUtils.getDataBaseTableName(name) + "_" + EFGUniqueID.getID();
 	}
 
 	/**
 	 * @return a String array of DataTable header names
 	 * 
 	 */
 	private String[] getDataTableHeaders() {
 		return this.dataHeaders;
 	}
 
 	/**
 	 * Create a table out of the supplied csv File
 	 * 
 	 * @return true if the table was successfully created false otherwise.
 	 */
 	private boolean createDataTable() {
 		boolean isDone = true;
 		// int ret = -1;
 		try {
 			String[] legalNames = this.getLegalNames();
 			if (legalNames == null) {
 				log.error("Could not obtain legal names for : "
 						+ this.datasource.getDataName().toString());
 				return false;
 			}
 			if (isDone) {
 				StringBuffer query = new StringBuffer();
 				query.append("uniqueID VARCHAR(255) PRIMARY KEY");
 				for (int i = 0; i < legalNames.length; i++) {
 					String th = legalNames[i];
 					if ((th == null) || (th.trim().equals(""))) {
 						StringBuffer errBuffer = new StringBuffer();
 						errBuffer.append("The file: ");
 						errBuffer.append(this.datasource.getDataName()
 								.toString());
 						errBuffer.append(" contains a blank column name.\n");
 						if (i != 0) {
 							errBuffer
 									.append(" This column comes after the column named: "
 											+ this.dataHeaders[i - 1] + "\n");
 						}
 						if ((i + 1) < legalNames.length) {
 							errBuffer.append(" and before "
 									+ this.dataHeaders[i + 1] + "\n");
 						}
 						errBuffer
 								.append("The import is being terminated.Please fix your data and try the import again!!");
 						log.error(errBuffer.toString());
 						isDone = false;
 						break;
 					}
 					query.append(", `");
 					query.append(th);
 					query.append("` ");
 					// READ FROM PROPERTIES FILE
 					query.append("TEXT");
 				}
 				if (isDone) {
 					log.debug("About to drop table : " + this.tableName
 							+ " if it exists!!");
 					this.executeStatement("DROP TABLE IF EXISTS "
 							+ this.tableName.toLowerCase());
 
 					String st = "CREATE TABLE " + this.tableName.toLowerCase() + "( "
 							+ query.toString() + ")";
 					log.debug("Query: " + st);
 					this.executeStatement(st);
 
 					// now get each row of data
 					while (this.dataExtractor.nextValue() != null) {
 						query = new StringBuffer();
 						// Sets up the uniqueID
 						query.append("\"");
 						query.append(EFGUniqueID.getID() + "");
 						query.append("\"");
 						boolean exists = false;// means that the line is not
 												// blank
 						for (int i = 0; i < this.dataHeaders.length; i++) {
 							String title = this.dataHeaders[i];
 
 							String dataVal = this.dataExtractor
 									.getValueByFieldName(title);
 							if ((dataVal == null)
 									|| (dataVal.trim().equals(""))) {
 								dataVal = EFGImportConstants.EFGProperties
 										.getProperty("dbBlank");
 								if ((dataVal == null)
 										|| (dataVal.trim().equals(""))) {
 									dataVal = "";
 								}
 							} else {
 								exists = true;// there is at least one
 												// variable
 							}
 							query.append(",");
 							query.append("\"");
 							StringWriter writer = new StringWriter();
 							
 							UnicodeToASCIIFilter.convertIllegalToUnicode(new StringReader(dataVal.trim()),writer);
 							String newData = writer.toString();
 							query.append(escapeQuotes(newData.trim()));
 							query.append("\"");
 						}
 						try {
 							if (query.length() > 0) {
 								if (exists) {
 									String stmtQuery = "INSERT INTO "
 											+ this.tableName.toLowerCase() + " VALUES("
 											+ query.toString() + ")";
 									log.debug("Insert query: " + stmtQuery);
 									this.executeStatement(stmtQuery);
 								} else {
 									log.debug("Empty line.Do not insert!!!");
 								}
 							}
 						} catch (Exception eex) {
 							eex.printStackTrace();
 							isDone = false;
 
 							this.logMessage(eex);
 							break;
 						}
 						if (!isDone) {
 							break;
 						}
 					}
 				}
 			}
 		} catch (Exception ee) {
 			isDone = false;
 			this.logMessage(ee);
 		}
 		return isDone;
 	}
 
 	/**
 	 * @param string
 	 * @return
 	 */
 	private String escapeQuotes(String string) {
 		if (string == null) {
 			return null;
 		}
 		return string.trim().replaceAll("\"", "\\\\\"");
 	}
 
 	private int executeStatement(String query) throws Exception {
 
 		return this.jdbcTemplate.update(query);
 	}
 
 	private java.util.List executeQueryForList(String query, int numberOfColumns)
 			throws Exception {
 		return EFGRDBImportUtils.executeQueryForList(this.jdbcTemplate, query,
 				numberOfColumns);
 	}
 
 	/**
 	 * @return true if efgRDBTable exists, false otherwise
 	 */
 	private boolean checkEFGRDBTable() {
 		if ((this.efgRDBTable == null) || (this.efgRDBTable.trim().equals(""))) {
 			log.error("The efgRDBTable property is not set!!!");
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * @param displayName -
 	 *            Use this displayName to find the metadata table associated
 	 *            with it If displayName is null or the empty String, System
 	 *            generates a Metadata name to use to create the metadata table ,
 	 *            else System finds the metadataTable in the database. If system
 	 *            cannot find metadata table in database it returns null;
 	 */
 	private EFGQueueObjectInterface findMetadataTable(String displayName) {
 		EFGQueueObjectInterface queue = null;
 
 		if ((displayName == null) || (displayName.trim().equals(""))) {
 
 			log.error("Specified display name: '" + displayName
 					+ "' was null or the empty string.");
 		} else {
 			if (checkEFGRDBTable()) {
 				//StringBuffer query = new StringBuffer(
 					//	"SELECT DS_METADATA, DS_DATA FROM ");
 				
 				StringBuffer query = new StringBuffer("SELECT ");
 				query.append(EFGImportConstants.DS_METADATA_COL);
 				query.append(",");
 				query.append(EFGImportConstants.DS_DATA_COL);
 				query.append(" FROM ");
 				query.append(this.efgRDBTable);
 				query.append(" WHERE DISPLAY_NAME=");
 				query.append("\"");
 				query.append(displayName.trim());
 				query.append("\"");
 				log.debug("About to retrieve metadata table for : "
 						+ displayName);
 				try {
 					java.util.List list = executeQueryForList(query.toString(),
 							2);
 					for (java.util.Iterator iter = list.iterator(); iter
 							.hasNext();) {
 						queue = (EFGQueueObjectInterface) iter.next();
 
 						break;// because only one row is required
 					}
 					if (queue == null) {
 						log
 								.error("A metadataTable could not be found with display name:  "
 										+ displayName);
 
 					}
 				} catch (Exception ee) {
 					this.logMessage(ee);
 					queue = null;
 				}
 			}
 		}
 		return queue;
 	}
 
 	/**
 	 * @return true if the table was successfully updated.
 	 */
 	private boolean createOrUpdateEFGTable() {
 		StringBuffer query = null;
 		if (!checkEFGRDBTable()) {
 			return false;
 		}
 		try {
 			query = new StringBuffer("SELECT ");
 			query.append(EFGImportConstants.DS_METADATA_COL);
 			query.append(",");
 			query.append(EFGImportConstants.DS_DATA_COL);
 			query.append(" FROM ");
 			query.append(this.efgRDBTable);
 			
 			//query = new StringBuffer("SELECT DS_METADATA,DS_DATA FROM ");
 		//	query.append(this.efgRDBTable);
 			query.append(" WHERE DISPLAY_NAME=");
 			query.append("\"");
 			query.append(this.getDisplayName());
 			query.append("\"");
 
 			// log.debug("About to execute query: " + query.toString());
 			List list = this.executeQueryForList(query.toString(), 2);
 
 			for (java.util.Iterator iter = list.iterator(); iter.hasNext();) {
 				EFGQueueObjectInterface queue = (EFGQueueObjectInterface) iter
 						.next();
 				this.metadataTableName = queue.getObject(0);
 				log.debug("MetadaTable to use: " + this.metadataTableName);
 				this.tableName = queue.getObject(1);
 				log.debug("Table to use: " + this.tableName);
 				break;
 			}
 			if (this.metadataTableName == null) {
 				log
 						.error("A metadata table could not be found for the current data!!");
 				return false;
 			}
 
 			query = new StringBuffer("SELECT LEGALNAME FROM ");
 			query.append(this.metadataTableName);
 			list = this.executeQueryForList(query.toString(), 1);
 
 			log.debug("queue size: " + list.size());
 			log.debug("legalNames size: " + this.getLegalNames().length);
 
 			if (list.size() != this.getLegalNames().length) {
 				log
 						.error("The number of columns in the MetadataTable to be used "
 								+ "for this update "
 								+ "does not match the number of field names in the data being "
 								+ "imported!!");
 				return false;
 			}
 
 			for (java.util.Iterator iter = list.iterator(); iter.hasNext();) {
 				EFGQueueObjectInterface queue = (EFGQueueObjectInterface) iter
 						.next();
 				String legalname = queue.getObject(0);
 				if (Arrays.binarySearch(this.getSortedLegalNames(), legalname
 						.trim(), this.compare) < 0) {
 					log.error("The column named: '" + legalname
 							+ "' does not exists in the table '"
 							+ this.metadataTableName + "'");
 					// remove from table
 					return false;
 				}
 			}
 			// do other compare and add those absent to table
 		} catch (Exception ee) {
 			this.logMessage(ee);
 			return false;
 
 		}
 		return true;
 	}
 
 	/**
 	 * Remove columns that do not exists in table to be used as replacement.
 	 * Then add columns that exists in table as replacement with default values.
 	 * 
 	 * @return true if the table was successfully updated.
 	 */
 	private boolean updateMetadataEFGTable() {
 		StringBuffer query = null;
 		if (!checkEFGRDBTable()) {
 			return false;
 		}
 		try {
 			query = new StringBuffer("SELECT ");
 			query.append(EFGImportConstants.DS_METADATA_COL);
 			query.append(",");
 			query.append(EFGImportConstants.DS_DATA_COL);
 			query.append(" FROM ");
 			query.append(this.efgRDBTable);
 			//query = new StringBuffer("SELECT DS_METADATA,DS_DATA FROM ");
 			//query.append(this.efgRDBTable);
 			query.append(" WHERE DISPLAY_NAME=");
 			query.append("\"");
 			query.append(this.getDisplayName());
 			query.append("\"");
 
 			// log.debug("About to execute query: " + query.toString());
 			List list = this.executeQueryForList(query.toString(), 2);
 
 			for (java.util.Iterator iter = list.iterator(); iter.hasNext();) {
 				EFGQueueObjectInterface queue = (EFGQueueObjectInterface) iter
 						.next();
 				this.metadataTableName = queue.getObject(0);
 				log.debug("MetadaTable to use: " + this.metadataTableName);
 				this.tableName = queue.getObject(1);
 				log.debug("Table to use: " + this.tableName);
 				break;
 			}
 			if (this.metadataTableName == null) {
 				log
 						.error("A metadata table could not be found for the current data!!");
 				return false;
 			}
 
 			query = new StringBuffer("SELECT LEGALNAME FROM ");
 			query.append(this.metadataTableName.toLowerCase());
 			list = this.executeQueryForList(query.toString(), 1);
 
 			boolean isDone = true;
 			Set newTableLegalNamesClone1 = new HashSet(Arrays.asList(this
 					.getSortedLegalNames()));
 			Set newTableLegalNamesClone2 = new HashSet(Arrays.asList(this
 					.getSortedLegalNames()));
 			Set oldTableLegalNamesClone1 = new HashSet(list.size());
 			Set oldTableLegalNamesClone2 = new HashSet(list.size());
 
 			for (java.util.Iterator iter = list.iterator(); iter.hasNext();) {
 				EFGQueueObjectInterface queue = (EFGQueueObjectInterface) iter
 						.next();
 				String legalname = queue.getObject(0);
 
 				oldTableLegalNamesClone1.add(legalname);
 				oldTableLegalNamesClone2.add(legalname);
 			}
 			
 			if ((oldTableLegalNamesClone1.size() != 0)
 					&& (oldTableLegalNamesClone1.size() != 0)) {
 
 				oldTableLegalNamesClone1.removeAll(newTableLegalNamesClone1);// things
 																				// in
 																				// oclone1
 																				// not
 																				// in
 																				// nclone1
 			
 				newTableLegalNamesClone2.removeAll(oldTableLegalNamesClone2);// things
 																				// in
 																				// nclone2
 																				// not
 																				// in
 				boolean toRemove = false;
 				if(oldTableLegalNamesClone1.size() > 0){
 					
 					this.removeFromMetadataTable(oldTableLegalNamesClone1);
 					toRemove = true;
 				}
 				if(newTableLegalNamesClone2.size() > 0){
 					int newList = getCount(this.metadataTableName);
 					if(!toRemove){
 						newList = newList + 1;
 					}
 					isDone = this.addToMetadataTable(newTableLegalNamesClone2,
 							newList);
 				}	
 
 			}
 			return isDone;
 		
 		} catch (Exception ee) {
 			this.logMessage(ee);
 
 		}
 		return false;
 	}
 
 	/**
 	 * @param newTableLegalNamesClone2
 	 */
 	private boolean addToMetadataTable(Set itemsToAdd, int numberOfRows) {
 
 		String[] fieldNames = this.getDataTableHeaders();
 		String[] legalNames = this.lowerCaseHeaders(this.getLegalNames());
 		int counter = numberOfRows;
 		
 		try {
 
 			for (int ii = 0; ii < legalNames.length; ii++) {
 				String legalName = legalNames[ii].trim();
 
 				if (itemsToAdd.contains(legalName)) {
 					StringBuffer query = new StringBuffer();
 					query.append("\"");
 					query.append(legalName);
 					query.append("\"");
 					query.append(",");
 
 					query.append("\"");
 					query.append(fieldNames[ii].trim());
 					query.append("\"");
 					query.append(",");
 
 					query.append("\"");
 					query.append(EFGImportConstants.EFG_FALSE);
 					query.append("\"");
 					query.append(",");
 
 					query.append("\"");
 					query.append(EFGImportConstants.EFG_FALSE);
 					query.append("\"");
 					query.append(",");
 
 					query.append("\"");
 					query.append(EFGImportConstants.EFG_TRUE);
 					query.append("\"");
 					query.append(",");
 
 					query.append("\"");
 					query.append(EFGImportConstants.EFG_FALSE);
 					query.append("\"");
 					query.append(",");
 
 					query.append("\"");
 					query.append(EFGImportConstants.EFG_TRUE);
 					query.append("\"");
 					query.append(",");
 
 					query.append("\"");
 					query.append(EFGImportConstants.EFG_FALSE);
 					query.append("\"");
 					query.append(",");
 
 					query.append("\"");
 					query.append(EFGImportConstants.EFG_FALSE);
 					query.append("\"");
 					query.append(",");
 
 					query.append("\"");
 					query.append(EFGImportConstants.EFG_FALSE);
 					query.append("\"");
 					query.append(",");
 					if(counter < 1){
 						++counter;
 					}
 					query.append(new Integer(counter));
 					++counter;
 					
 					String stmtQuery = "INSERT INTO " + this.metadataTableName.toLowerCase() + " " +  this.getMetadataHeadQuery()
 							+ " VALUES(" + query.toString() + ")";
 					log.debug("Insert query: " + stmtQuery);
 					this.executeStatement(stmtQuery);
 				}
 			}
 			return true;
 		} catch (Exception ee) {
 			log.error(ee.getMessage());
 			ee.printStackTrace();
 		}
 		return false;
 	}
 
 	/**
 	 * @param oldTableLegalNamesClone1
 	 */
 	private void removeFromMetadataTable(Set itemsToRemove) {
 		StringBuffer deleteQuery = new StringBuffer();
 		StringBuffer selectQuery = new StringBuffer();
 		
 		
 		selectQuery.append("SELECT OrderValue FROM ");
 		selectQuery.append(this.metadataTableName);
 		selectQuery.append(" WHERE ");
 	
 		deleteQuery.append("DELETE FROM ");
 		deleteQuery.append(this.metadataTableName);
 		deleteQuery.append(" WHERE ");
 		
 		Iterator iter = itemsToRemove.iterator();
 		int i = 0;
 		while (iter.hasNext()) {
 			StringBuffer queryBuffer = new StringBuffer();
 			String legalname = (String) iter.next();
 			queryBuffer.append(EFGImportConstants.LEGALNAME);
 			queryBuffer.append("=");
 			queryBuffer.append("\"");
 			queryBuffer.append(legalname);
 			queryBuffer.append("\"");
 			if (i < (itemsToRemove.size() - 1)) {
 				queryBuffer.append(" OR ");
 			}
 			selectQuery.append(queryBuffer);
 			deleteQuery.append(queryBuffer);
 			++i;
 		}
 		log.debug("Select query: " + selectQuery.toString());
 		log.debug("Delete query: " + deleteQuery.toString());
 		try {
 			List list = this.executeQueryForList(selectQuery.toString(), 1);
 			 this.executeStatement(deleteQuery.toString());
 			
 			this.updateMetadataTable(list,this.metadataTableName);
 			
 		} catch (Exception ee) {
 			log.error(ee.getMessage());
 		}
 		
 		// execute the query here
 	}
 	private void updateMetadataTable(List list, String metaName){
 		
 		
 		for(int listCounter =  list.size()-1; listCounter >= 0 ;listCounter-- ){
 			EFGQueueObjectInterface queue = 
 				(EFGQueueObjectInterface)list.get(listCounter);
 			if(queue != null){
 				int weight = Integer.parseInt(queue.getObject(0));	
 				
 				
 				StringBuffer query = new StringBuffer();
 				query.append("UPDATE ");
 				query.append(metaName);
 				query.append(" SET OrderValue = (OrderValue - 1) ");
 				query.append( " WHERE ");
 				query.append("OrderValue > ");
 				query.append(weight);
 				
 				try {
 					this.executeStatement(query.toString());
 				
 				} catch (Exception e) {
 					//System.err.println("Error: " + e.getMessage());
 				}
 			}
 		}
 
 	}
 	private boolean createHelperTable(String tableName){
 		try{
 			StringBuffer query = new StringBuffer();
 			log.debug("About to create table: '" + this.efgRDBTable
 					+ "' if it does not exists");
 			// PUT IN PROPERTIES FILE
 			query.append("CREATE TABLE IF NOT EXISTS ");
 			query.append(tableName);
 			query.append("( DS_DATA VARCHAR(255) not null,");
 			query.append("ORIGINAL_FILE_NAME TEXT, ");
 			query.append("DS_METADATA VARCHAR(255) not null, ");
 			query.append("DISPLAY_NAME VARCHAR(255) unique not null, ");
 			query.append("XSL_FILENAME_TAXON VARCHAR(255), ");
 			query.append("XSL_FILENAME_SEARCHPAGE_PLATES VARCHAR(255), ");
 			query.append("XSL_FILENAME_SEARCHPAGE_LISTS VARCHAR(255), ");
 			query.append("CSS_FILENAME VARCHAR(255), ");
 			query.append("JAVASCRIPT_FILENAME VARCHAR(255), ");
 			query.append("TEMPLATE_OBJECT BLOB ");
 			query.append(")");
 			log.debug("About to execute query : '" + query.toString());
 			this.executeStatement(query.toString());
 			log.debug("Query executed successfully!!");
 			return true;
 		}
 		catch(Exception ee){
 			this.logMessage(ee);
 		}
 		log.debug("About to return false!!!");
 		return false;
 	}
 	/**
 	 * Create EFGRDTable if it does not already exists
 	 * 
 	 * @return true if query executed successfully.
 	 */
 	private boolean createEFGRDBTable() {
 		try {
 			if (checkEFGRDBTable()) {
 				return createHelperTable(this.efgRDBTable);
 			}
 			
 		} catch (Exception ee) {
 			this.logMessage(ee);
 		}
 		log.debug("About to return false!!!");
 		return false;
 	}
 
 	
 
 	/**
 	 * Create helper table for Data and metadata tables
 	 * 
 	 * @return true if successfully created, false otherwise
 	 */
 	private boolean createEFGTable() {
 		boolean isDone = true;
 		try {
 
 			if ((this.getDisplayName() == null)
 					|| (this.getDisplayName().trim().equals(""))) {
 				this.datasource.setDisplayName(this.tableName);
 			}
 
 			isDone = createEFGRDBTable();
 			if (isDone) {
 				StringBuffer query = new StringBuffer();
 				query = new StringBuffer("SELECT count(*) FROM ");
 				query.append(this.efgRDBTable);
 				query.append(" WHERE DISPLAY_NAME=");
 				query.append("\"");
 				query.append(this.getDisplayName());
 				query.append("\"");
 
 				java.util.List list = this.executeQueryForList(
 						query.toString(), 1);
 				String counter = ((EFGQueueObjectInterface) list.get(0))
 						.getObject(0);
 				int count = 0;
 				if (counter != null) {
 					count = Integer.parseInt(counter);
 				}
 
 				if (count > 0) {
 					log.debug("Row already exists.Updating row");
 					query = new StringBuffer("UPDATE ");
 					query.append(this.efgRDBTable);
 					query.append(" SET DS_METADATA = ");
 					query.append("\"");
 					query.append(this.metadataTableName);
 					query.append("\"");
 					query.append(" WHERE DISPLAY_NAME = ");
 					query.append("\"");
 					query.append(this.getDisplayName());
 					query.append("\"");
 
 				} else {
 					log.debug("Row does not already exists.Inserting row");
 					query = new StringBuffer("INSERT INTO ");
 					query.append(this.efgRDBTable);
 					query.append(" (DS_DATA,ORIGINAL_FILE_NAME,DS_METADATA,DISPLAY_NAME)");
 					query.append(" VALUES( ");
 					query.append("\"");
 					query.append(EFGUtils.parse240(this.tableName.toLowerCase()));
 					query.append("\"");
 					query.append(",");
 					query.append("\"");
 					query.append(EFGUtils.parseEFGSEP(this.datasource
 							.getDataName().toString()));
 					query.append("\"");
 					query.append(",");
 					query.append("\"");
 					query.append(EFGUtils.parse240(this.metadataTableName.toLowerCase()));
 					query.append("\"");
 					query.append(",");
 					query.append("\"");
 					query.append(EFGUtils.parse240(this.getDisplayName()));
 					query.append("\"");
 					query.append(")");
 
 				}
 				log.debug("About to execute query: " + query.toString());
 				this.executeStatement(query.toString());
 				log.debug("Query is executed!!!");
 			}
 		} catch (Exception ee) {
 			isDone = false;
 			this.logMessage(ee);
 		}
 		return isDone;
 	}
 
 	/**
 	 * Create a name for the Metadata table if none is supplied
 	 */
 	private String createMetadataTableName() {
 		String meta = this.tableName + EFGImportConstants.METAFILESUFFIX;
 		log.debug("About to create Metadata table name: '" + meta + "'");
 		return meta.toLowerCase();
 	}
 
 	/**
 	 * @param ee-
 	 *            The exception to log
 	 */
 	private void logMessage(Exception ee) {
 
 		log.error(ee.getMessage());
 
 	}
 
 	private boolean cloneMetadataTable(String metadataTableToClone,
 			String[] fieldNames, String[] legalNames) {
 		log.debug("Table to clone: " + metadataTableToClone);
 		log.debug("Metadata table Name: " + this.metadataTableName);
 
 		boolean isDone = this.createMetadataTableHeader(this.metadataTableName.toLowerCase(),
 				fieldNames, legalNames);
 		try {
 			if (isDone) {
 				StringBuffer query = new StringBuffer();
 				query.append("INSERT INTO ");
 				query.append(this.metadataTableName.toLowerCase());
 				query.append("( SELECT * FROM " + metadataTableToClone);
 				query.append(")");
 				log.debug("Insert query: " + query.toString());
 
 				int ret = this.executeStatement(query.toString());
 
 				if (ret == 0) {
 					log.error("'" + this.metadataTableName
 							+ "' could not be created from '"
 							+ metadataTableToClone + "'");
 					isDone = false;
 				}
 			}
 		} catch (Exception ee) {
 			isDone = false;
 			this.logMessage(ee);
 		}
 		return isDone;
 	}
 
 	private boolean createMetadataTableHeader(String infoTable,
 			String[] fieldNames, String[] legalNames) {
 		boolean isDone = true;
 		// create the query
 
 		if (legalNames == null) {
 			log.error("The field names in the file "
 					+ this.datasource.getDataName().toString()
 					+ " could not be converted to legal names");
 			isDone = false;
 		}
 
 		if ((legalNames == null) || (fieldNames == null)
 				|| (fieldNames.length != legalNames.length)) {
 			log.error("Data table and metadata table fields do not match!!");
 			isDone = false;
 		}
 		if (fieldNames == null) {
 			// log.error("The file " + this.datasource.getDataName().toString()
 			// + " does not contain field names");
 			isDone = false;
 		}
 		log.debug("Number of Field Names in current data is : "
 				+ fieldNames.length);
 		log.debug("Number of Legalnames Names in current data is : "
 				+ legalNames.length);
 
 		if (!isDone) {
 			log.error("Import terminated");
 
 		} else {
 
 			String[] metaHead = this.getMetadataTableHeaders();
 			if (metaHead == null) {
 				log
 						.error("System could not find metadata table header file.Import is terminating!!\n");
 				isDone = false;
 			}
 			log.debug("Number of Field Names in system table csv file is : "
 					+ metaHead.length);
 
 			if (isDone) {
 				// create metadata table headers
 				// get this from a utils class perhaps
 				log.debug("About to create table : " + infoTable);
 				StringBuffer query = new StringBuffer();
 
 				for (int i = 0; i < metaHead.length; i++) {
 					String th = metaHead[i];
 					if ((th == null) || (th.trim().equals(""))) {
 						StringBuffer errBuffer = new StringBuffer();
 						errBuffer.append("The table: ");
 						errBuffer.append(this.tableName);
 						errBuffer.append(" contains a blank column name.\n");
 						if (i != 0) {
 							errBuffer
 									.append(" This column comes after the column named: "
 											+ metaHead[i - 1] + "\n");
 						}
 						if ((i + 1) < metaHead.length) {
 							errBuffer.append(" and before " + metaHead[i + 1]
 									+ "\n");
 						}
 						errBuffer
 								.append("The import is being terminated.Please fix the metadata table and try the import again!!");
 						log.error(errBuffer.toString());
 						isDone = false;
 						break;
 					}
 
 					query.append(" `");
 					query.append(th);
 					query.append("` ");
 
 					if (EFGImportConstants.ORDER.equalsIgnoreCase(th.trim())) {
 						query.append(" INTEGER ");
 
 					} else if ((EFGImportConstants.LEGALNAME
 							.equalsIgnoreCase(th))
 							|| (EFGImportConstants.NAME.equalsIgnoreCase(th))) {
 						query.append(" VARCHAR(255) ");
 					} else {
 						query.append(" VARCHAR(6) ");
 					}
 					if ((i + 1) < metaHead.length) {
 						query.append(",");
 					}
 				}
 				if (isDone) {
 					try {
 						log.debug("About to drop the table: '" + infoTable
 								+ "' if it exists");
 						this.executeStatement("DROP TABLE IF EXISTS "
 								+ infoTable);
 
 						String stmtQuery = "CREATE TABLE " + infoTable + "( "
 								+ query.toString() + ")";
 
 						log.debug("About to execute query : " + stmtQuery);
 						this.executeStatement(stmtQuery);
 
 						log.debug("Metadata table: '" + infoTable
 								+ "' was created successfully");
 					} catch (Exception ee) {
 						isDone = false;
 						this.logMessage(ee);
 					}
 				}
 			}
 		}
 		return isDone;
 	}
 	private String getMetadataHeadQuery(){
 		
 		StringBuffer query = new StringBuffer("(");
 		query.append(EFGImportConstants.LEGALNAME);
 		query.append(",");
 		query.append(EFGImportConstants.NAME);
 		query.append(",");
 		query.append(EFGImportConstants.SEARCHABLE);
 		query.append(",");
 		query.append(EFGImportConstants.ISLISTS);
 		query.append(",");
 
 		query.append(EFGImportConstants.ONTAXONPAGE);
 		query.append(",");
 		query.append(EFGImportConstants.CATEGORICAL);
 		query.append(",");
 		query.append(EFGImportConstants.NARRATIVE);
 		query.append(",");
 		query.append(EFGImportConstants.NUMERIC);
 		query.append(",");
 		query.append(EFGImportConstants.NUMERICRANGE);
 		query.append(",");
 		query.append(EFGImportConstants.MEDIARESOURCE);
 		query.append(",");
 		query.append(EFGImportConstants.ORDER);
 		query.append(")");
 		
 		return query.toString();
 		
 	}
 	/**
 	 * 
 	 * @return an array of headers to be used for Metadata table REFACTOR USE
 	 *         STATIC SINGLETON FACTORY METHOD CONFIGURE WITH SPRING
 	 */
 	private String[] getMetadataTableHeaders() {
 		return EFGUtils.getMetadataTableHeaders();
 	}
 
 	/**
 	 * @return true if Metadata table was successfully created
 	 */
 	private boolean createMetadataTable(String[] fieldNames, String[] legalNames) {
 
 		boolean isDone = this.createMetadataTableHeader(this.metadataTableName.toLowerCase(),
 				fieldNames, legalNames);
 		try {
 			if (isDone) {
 				log.debug("About to populate metadataTable");
 				String[] metaHead = this.getMetadataTableHeaders();
 				for (int j = 0; j < fieldNames.length; j++) {
 					StringBuffer query = new StringBuffer();
 
 					for (int i = 0; i < metaHead.length; i++) {
 						String th = metaHead[i].trim();
 						if (EFGImportConstants.LEGALNAME.equalsIgnoreCase(th)) {
 							query.append("\"");
 							query.append(legalNames[j].trim());
 							query.append("\"");
 						} else if (EFGImportConstants.NAME.equalsIgnoreCase(th)) {
 							query.append("\"");
 							query.append(fieldNames[j].trim());
 							query.append("\"");
 						} else if (EFGImportConstants.SEARCHABLE
 								.equalsIgnoreCase(th)) {
 							query.append("\"");
 							query.append(EFGImportConstants.EFG_FALSE);
 							query.append("\"");
 						} else if (EFGImportConstants.ISLISTS
 								.equalsIgnoreCase(th)) {
 							query.append("\"");
 							query.append(EFGImportConstants.EFG_FALSE);
 							query.append("\"");
 						} else if (EFGImportConstants.ONTAXONPAGE
 								.equalsIgnoreCase(th)) {
 							query.append("\"");
 							query.append(EFGImportConstants.EFG_TRUE);
 							query.append("\"");
 						} else if (EFGImportConstants.CATEGORICAL
 								.equalsIgnoreCase(th)) {
 							query.append("\"");
 							query.append(EFGImportConstants.EFG_FALSE);
 							query.append("\"");
 						} else if (EFGImportConstants.NARRATIVE
 								.equalsIgnoreCase(th)) {
 							query.append("\"");
 							query.append(EFGImportConstants.EFG_TRUE);
 							query.append("\"");
 						} else if (EFGImportConstants.NUMERIC
 								.equalsIgnoreCase(th)) {
 							query.append("\"");
 							query.append(EFGImportConstants.EFG_FALSE);
 							query.append("\"");
 						} else if (EFGImportConstants.NUMERICRANGE
 								.equalsIgnoreCase(th)) {
 							query.append("\"");
 							query.append(EFGImportConstants.EFG_FALSE);
 							query.append("\"");
 						} else if (EFGImportConstants.MEDIARESOURCE
 								.equalsIgnoreCase(th)) {
 							query.append("\"");
 							query.append(EFGImportConstants.EFG_FALSE);
 							query.append("\"");
 						} else if (EFGImportConstants.ORDER
 								.equalsIgnoreCase(th)) {
 							query.append(new Integer(j + 1));
 						}
 						if ((i + 1) < metaHead.length) {
 							query.append(",");
 						}
 					}
 					if (query.length() > 0) {
 						String stmtQuery = "INSERT INTO "
 								+ this.metadataTableName.toLowerCase() + " VALUES("
 								+ query.toString() + ")";
 						log.debug("Insert query: " + stmtQuery);
 						this.executeStatement(stmtQuery);
 					}
 				}
 			}
 		} catch (Exception ee) {
 			isDone = false;
 			log.error(ee.getMessage());
 			ee.printStackTrace();
 			this.logMessage(ee);
 		}
 		return isDone;
 	}
 
 	/**
 	 * Main test class. Replace with unit tests
 	 */
 	public static void main(String args[]) {
 
 	}
 } // CSV2Database
