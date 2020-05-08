 /*
  * Copyright (C) 2003 - 2012 OpenSubsystems.com/net/org and its owners. All rights reserved.
  * 
  * This file is part of OpenSubsystems.
  *
  * OpenSubsystems is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
  */
 
 package org.opensubsystems.core.persist.jdbc;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 
 import org.opensubsystems.core.data.BasicDataObject;
 import org.opensubsystems.core.data.ModifiableDataObject;
 import org.opensubsystems.core.error.OSSException;
 
 /**
  * Interface to define abstraction for initialization and management of database 
  * instance accessed by the application. The main purpose is to define a standard 
  * way how to interact with the database, which is used by the application to 
  * access and persist data so that the application can (if possible) bring the 
  * database online or shut it down in case the application runs in embedded 
  * environment or environment without DBA babysitting the database. It also 
  * define interfaces to access database dependent information such as different 
  * SQL functions or statements which can be then used by database schemas. 
  * 
  * @author bastafidli
  */
 public interface Database
 {
    // Logic ////////////////////////////////////////////////////////////////////
    
    /**
     * Start the database.
     * 
     * @throws OSSException - an error has occurred
     */   
    void start(
    ) throws OSSException;
    
    /**
     * Stop the database. After the database is stopped, no more requests can be 
     * issued until the database is started again.
     * 
     * @throws OSSException - an error has occurred
     */
    void stop(
    ) throws OSSException;
    
    /**
     * Add new schema to the database.
     * 
     * @param dsSchema - schema to add to the database
     * @throws OSSException - an error has occurred
     */
    void add(
       DatabaseSchema dsSchema
    ) throws OSSException;
    
    /**
     * Add new schema to the database schema by specifying the generic schema
     * class or interface. The schema itself will be created by 
     * DatabaseSchemaManager for the current environment taking into account 
     * current configuration settings. 
     * 
     * @param clsSchema - class representing schema to add to the database
     * @throws OSSException - an error has occurred
     */
    void add(
      Class<DatabaseSchema> clsSchema
    ) throws OSSException;
    
    /**
     * Returns the identifier for the type of database represented by this 
     * instance. This identifier can be used to construct package and class 
     * names for classes that implements database specific behavior. .
     * 
     * @return String - database type
     */
    String getDatabaseTypeIdentifier(
    );
 
    /**
     * Test if the database is started.
     * 
     * @return boolean - true if the database was started and hasn't been stopped 
     *                   yet
     */
    boolean isStarted(
    );
    
    // Methods to retrieve database dependent information ///////////////////////
    
    /**
     * Get very efficient parameterless SQL statement which can be used to test
     * if the connection to the database is still valid.
     *   
     * @return String - SQL statement
     */
    String getConnectionTestStatement(
    );
    
    /**
     * Different databases have different limitations about what transaction 
     * isolation levels they support so this method will take the desired 
     * transaction isolation level and convert it to the one supported by 
     * the specific database.
     * 
     * @param iTransactionIsolation - desired transaction isolation level
     * @return int - supported transaction isolation level
     */
    int getTransactionIsolation(
       int iTransactionIsolation
    );
    
    /**
     * Get the default result set type that should be used to load lists of items 
     * from the result set efficiently. Some databases support efficient absolute 
     * cursors that allow to efficiently find out the size of the result set and 
     * efficiently allocate memory for it and they may require special result set 
     * type to do so.
     * 
     * @return int - result set type to use to load list, see ResultSet.TYPE_XXX 
     *               constants
     */
    int getSelectListResultSetType();
    
    /**
     * Get the default result set concurrency that should be used to load lists 
     * of items from the result set efficiently. Some databases support efficient 
     * absolute cursors that allow to efficiently find out the size of result set 
     * and efficiently allocate memory for it and they may require special result 
     * set concurrency to do so.
     * 
     * @return int - result set concurrency to use to load list, see 
     *               ResultSet.CONCUR_XXX  constants
     */
    int getSelectListResultSetConcurrency();   
    
    /**
     * Get the default value specifying how many database operations should be 
     * batched together before they are sent to the database to execute
     * 
     * @return int - batch size specifying how many database operations should be 
     *               batched together before they are sent to the database to 
     *               execute
     */
    int getBatchSize();
 
    /**
     * Find out if database (driver) allows to call methods such as absolute() 
     * or last() for the retrieved result sets.
     * 
     * @return boolean - true if it is possible to call methods such as 
     *                   absolute(), last() for the retrieved result sets, false
     *                   otherwise
     */
    boolean hasAbsolutePositioningSupport();
    
    /**
     * Find out if when trying to find out size of the result set we should use
     * count(*)/count(1)/count(id) instead of using last() in case the
     * hasAbsolutePositioningSupport() indicates the database supports it.
     * 
     * @return boolean - true if we should use count(x) instead of last()
     */
    boolean preferCountToLast();
 
    // Methods to retrieve differently named functions or attributes ////////////
    
    /**
     * Get string which can be used in SQL queries to retrieve timestamp 
     * representing the current date and time.
     * 
     * @return String - SQL representation of function call to get the current 
     *                  timestamp
     */
    String getSQLCurrentTimestampFunctionCall(
    );
 
    /**
     * Get database specific SQL queries to analyze tables or update statistics 
     * on the tables to improve their performance.
     * 
     * @param mpTableNames - map of table names the should be optimized. Key is 
     *                       the data type that is stored in the table and value 
     *                       is the name of the table
     * @return Object[] - index 0 - String[] - array of SQL commands to optimize 
     *                                         the specified tables and possibly 
     *                                         their related indexes
     *                  - index 1 - Boolean flag signaling if when executing 
     *                              these statements the autocommit should be 
     *                              true or false
     */
    Object[] getSQLAnalyzeFunctionCall(
       Map<Integer, String> mpTableNames
    );
 
    /**
     * Get string which can be used in SQL queries to retrieve record count.
     * 
     * @return String - SQL representation of function call to get record count
     */
    String getSQLCountFunctionCall(
    );
 
    /**
     * Find out if database supports limiting the range of rows retrieved by a 
     * query. This means that database has to provide a way how to construct 
     * EFFICIENT SQL that allows to retrieve items starting from row X and 
     * ending at row Y from the result set matching also other possible criteria. 
     * 
     * @return boolean - true if it is possible to construct efficient SQL to
     *                   limit the retrieved range of rows
     */
    boolean hasRangeSupport(
    );
 
    /**
     * Test if the specified query invokes stored procedure or if it is just 
     * a regular prepared statement.
     * 
     * @param strQuery - query to test
     * @return boolean - true if query invokes stored procedure false otherwise 
     */
    boolean isCallableStatement(
       String strQuery
    );
    
    // Methods to execute database specific behavior ////////////////////////////
 
    /**
     * Insert the data and fetch from the database the generated id and generated 
     * creation and optionally modification timestamp for the newly created data 
     * object.
     * 
     * Note: Since the caller created the prepared (or callable) statement, 
     * the caller is responsible for its closing.
     *  
     * @param dbConnection - connection to use to access the database
     * @param insertStatement - statement used to insert the data. This can be
     *                          CallableStatement if stored procedure is used.
     * @param bIsInDomain - are the data objects maintained in domains
     * @param strTableName - name of the table where the data are being inserted
     * @param iIndex - 1 based index of the next parameter value of which can be
     *                 set on the statement (last parameter set by caller + 1)
     * @param data - data object to update with the fetched values
     * @throws SQLException - an error has occurred
     * @throws OSSException - an error has occurred
     */
    void insertAndFetchGeneratedValues(
       Connection        dbConnection,
       PreparedStatement insertStatement,
       boolean           bIsInDomain,
       String            strTableName,
       int               iIndex,
       BasicDataObject   data
    ) throws SQLException,
             OSSException;
    
    /**
     * Update the data, check for  errors and fetch from the database the 
     * generated modification timestamps for the updated data object.
     * 
     * Note: Since the caller created the prepared statement, the caller is
     * responsible for its closing. 
     * 
     * @param strDataName - name of the data object
     * @param dbConnection - connection to use to access the database
     * @param updateStatement - statement to update data in the database. This 
     *                          can be CallableStatement if stored procedure is 
     *                          used.
     * @param bIsInDomain - are the data objects maintained in domains
     * @param strTableName - name of the table
     * @param iIndex - 1 based index of the next parameter value of which can be
     *                 set on the statement (last parameter set by caller + 1)
     * @param data - data object to update
     * @throws SQLException - an error has occurred
     * @throws OSSException - an error has occurred
     */
    void updatedAndFetchGeneratedValues(
       String               strDataName,
       Connection           dbConnection,
       PreparedStatement    updateStatement,
       boolean              bIsInDomain,
       String               strTableName,
       int                  iIndex,
       ModifiableDataObject data
    ) throws SQLException,
             OSSException;
 
    /**
     * Because there is limitation for sql statement length and in() expression 
     * can contain lot of members, this function will construct list of strings 
     * with maximum number of allowed members (representing ids) each. Strings 
     * do not contain ().
     * 
     * @param idList - collection with ids (as Objects) 
     * @param bQuote - flag signaling if there will be used quotes when
     *                 string items will be served
     *               - true = there will be used quotes; false = otherwise
     * @return List - list of strings with safe length
     */
    public List<String> getInListWithSafeLength(
       Collection<?> idList,
       boolean    bQuote
    );
 }
