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
 
 package org.opensubsystems.core.persist.jdbc.operation;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.opensubsystems.core.data.DataObject;
 import org.opensubsystems.core.error.OSSDataCreateException;
 import org.opensubsystems.core.error.OSSDataDeleteException;
 import org.opensubsystems.core.error.OSSDataSaveException;
 import org.opensubsystems.core.error.OSSException;
 import org.opensubsystems.core.persist.jdbc.BasicDatabaseFactory;
 import org.opensubsystems.core.persist.jdbc.DatabaseFactory;
 import org.opensubsystems.core.persist.jdbc.ModifiableDatabaseFactory;
 import org.opensubsystems.core.persist.jdbc.ModifiableDatabaseSchema;
 import org.opensubsystems.core.persist.jdbc.impl.DatabaseConnectionFactoryImpl;
 import org.opensubsystems.core.persist.jdbc.impl.DatabaseTransactionFactoryImpl;
 import org.opensubsystems.core.util.GlobalConstants;
 import org.opensubsystems.core.util.Log;
 import org.opensubsystems.core.util.jdbc.DatabaseUtils;
 
 /**
  * Adapter to simplify writing of database updates, which takes care of
  * requesting and returning connections, transaction management and 
  * exception handling. To use this adapter you just need to define anonymous 
  * class and override method performOperation to provide the actual database 
  * update. Optionally you may want to override one of the handleXXX methods 
  * to provide custom error handling. 
  *
  * Example how to perform the update using query retrieved from schema
  * 
  * public ModifiableDataObject save(
  *    final ModifiableDataObject data
  * ) throws OSSException
  * {
  *    DatabaseUpdateOperation dbop = new DatabaseUpdateOperation(
  *       this, m_schema.getQueryToUpdateMyData(data), 
  *       DatabaseUpdateOperation.DBOP_UPDATE, m_schema, 
  *       DataConstants.MY_DATA, data
  *    )
  *    {
  *       protected void updateDatabase(
  *          DatabaseFactoryImpl dbfactory,
  *          Connection          cntConnection,
  *          PreparedStatement   pstmQuery
  *       ) throws OSSException,
  *                SQLException
  *       {
  *          ModifiableDataObject objData = data;
  *          int iIndex = ((ModifiableDatabaseFactory)dbfactory).setValuesForUpdate(
  *                          pstmQuery, objData);
  *      
  *          DatabaseImpl.getInstance().updatedAndFetchGeneratedValues(
  *                                        m_strDataObjectName, 
  *                                        cntConnection, pstmQuery, 
  *                                        m_dbschema.isInDomain(), 
  *                                        m_dbschema.getTableNames().get(
  *                                           new Integer(m_iDataType)).toString(), 
  *                                        iIndex, objData);
  *          setReturnData(objData);
  *       }         
  *    };
  *    dbop.executeUpdate();
  *     
  *    return (ModifiableDataObject)dbop.getReturnData();
  * }
  *
  *
  * Example of method in factory which saves data using its schema 
  *
  * public ModifiableDataObject save(
  *    final ModifiableDataObject data
  * ) throws OSSException
  * {
  *    DatabaseUpdateOperation dbop = new DatabaseUpdateOperation(
  *       this, DatabaseUpdateOperation.DBOP_UPDATE)
  *    {
  *       protected void updateDatabase(
  *          DatabaseFactoryImpl dbfactory,
  *          Connection          cntConnection,
  *          PreparedStatement   pstmQuery
  *       ) throws OSSException,
  *                SQLException
  *       {
  *          setReturnData(m_schema.updateData(cntConnection, (MyData)data));
  *       }         
  *    };
  *    dbop.executeUpdate();
  *     
  *    return (ModifiableDataObject)dbop.getReturnData();
  * }
  *
  * @author bastafidli
  */
 public abstract class DatabaseUpdateOperation extends    DatabaseOperation 
                                               implements DatabaseOperations
 {
    // Cached values ////////////////////////////////////////////////////////////
 
    /**
     * Logger for this class
     */
    private static Logger s_logger = Log.getInstance(DatabaseUpdateOperation.class);
    
    // Attributes ///////////////////////////////////////////////////////////////
 
    /**
     * Type of update performed by this class.
     */
    private int m_iUpdateType;
    
    // Constructors /////////////////////////////////////////////////////////////
    
    /**
     * Constructor to use when database update doesn't require any prepared
     * statement.
     * 
     * @param factory - factory which is executing this operation
     * @param iUpdateType - type of update, one of the constants defined above
     */
    public DatabaseUpdateOperation(
       DatabaseFactory factory,
       int             iUpdateType
    )
    {
       this(factory, null, null, iUpdateType, null);
    }
 
 
    /**
     * Constructor to use when database update requires prepared statement.
     * 
     * @param factory - factory which is executing this operation
     * @param strQueryToPrepare - query which should be used to construct prepared
     *                            statement which will be passed in to 
     *                            executeUpdate
     * @param schema - database schema used with this operation
     * @param iUpdateType - type of update, one of the constants defined above
     * @param data - data used for operation
     */
    public DatabaseUpdateOperation(
       DatabaseFactory          factory,
       String                   strQueryToPrepare,
       ModifiableDatabaseSchema schema,
       int                      iUpdateType,
       Object                   data
    )
    {
       super(factory, strQueryToPrepare, schema, data);
       
       m_iUpdateType = iUpdateType;
    }
 
    // Logic ////////////////////////////////////////////////////////////////////
    
    /**
     * Method to execute database update invoking the user defined code
     * in performOperation.
     * 
     * @throws OSSException - an error has occurred
     */
    public void executeUpdate(
    ) throws OSSException
    {
       Connection        cntConnection = null;
       PreparedStatement pstmQuery = null;
       
       try
       {
          // Request autocommit false since we are modifying database
          cntConnection = DatabaseConnectionFactoryImpl.getInstance()
                             .requestConnection(false);
          if ((m_strQuery != null) && (m_strQuery.length() > 0))
          {
             // Based on type of query we either execute just a normal query
             // or invoke a stored procedure
             if (m_factory.getDatabase().isCallableStatement(m_strQuery))
             {
                pstmQuery = cntConnection.prepareCall(m_strQuery);
             }
             else
             {
                pstmQuery = cntConnection.prepareStatement(m_strQuery);
             }
          }
 
          // Execute the update hopefully defined in the derived class
          performOperation(m_factory, cntConnection, pstmQuery);
 
          // At this point we don't know if this is just a single operation
          // and we need to commit or if it is a part of bigger transaction
          // and the commit is not desired until all operations proceed. 
          // Therefore let the DatabaseTransactionFactory resolve it 
          DatabaseTransactionFactoryImpl.getInstance().commitTransaction(
                                                          cntConnection);
       }
       // We want to handle SQLException separately since it often means 
       // constraint violation which can be detected and we can provide more 
       // meaningful message
       catch (SQLException sqleExc)
       {
          // At this point we don't know if this is just a single operation
          // and we need to commit or if it is a part of bigger transaction
          // and the commit is not desired until all operations proceed. 
          // Therefore let the DatabaseTransactionFactory resolve it 
          rollbackAndIgnoreException(cntConnection);
          handleSQLException(sqleExc, cntConnection, m_iUpdateType, 
                             m_factory.getDataDescriptor().getDataType(),
                             m_factory.getDataDescriptor().getDisplayableViewName(),
                             m_data);
       }
       // We just want to propagate OSSException since we do not want
       // to loose any error message produced by underlying layer and there
       // is no need to add any more messaging.
       catch (OSSException ossExc)
       {
          // At this point we don't know if this is just a single operation
          // and we need to rollback or if it is a part of bigger transaction
          // and the rollback is not desired until all operations proceed. 
          // Therefore let the DatabaseTransactionFactory resolve it 
          rollbackAndIgnoreException(cntConnection);
          handleKnownError(ossExc, cntConnection, m_iUpdateType, 
                           m_factory.getDataDescriptor().getDataType(),
                           m_factory.getDataDescriptor().getDisplayableViewName(),
                           m_data);
       }
       // We must catch Throwable to rollback since assert throw Error and not 
       // Exception
       catch (Throwable thr)
       {
          // At this point we don't know if this is just a single operation
          // and we need to rollback or if it is a part of bigger transaction
          // and the rollback is not desired until all operations proceed. 
          // Therefore let the DatabaseTransactionFactory resolve it 
          rollbackAndIgnoreException(cntConnection);
          handleUnknownError(thr, cntConnection, m_iUpdateType, 
                             m_factory.getDataDescriptor().getDataType(),
                             m_factory.getDataDescriptor().getDisplayableViewName(),
                             m_data);
       }
       finally
       {
          DatabaseUtils.closeStatement(pstmQuery);
          DatabaseConnectionFactoryImpl.getInstance().returnConnection(
                                                         cntConnection);
       }         
    }
 
    // Helper methods ///////////////////////////////////////////////////////////
    
    /**
     * Define content of this method to perform the database operation using the 
     * provided connection and optional prepared statement.
     * 
     * @param dbfactory     - database factory used for this operation 
     * @param cntConnection - ready to use connection to perform the database
     *                        operation. No need to return this connection.
     * @param pstmStatement - prepared statement for query passed in as a 
     *                        parameter to the constructor. No need to close 
     *                        this statement. If no query was passed into 
     *                        constructor, this will be null.
     * 
     * @throws OSSException - an error has occurred
     * @throws SQLException - an error has occurred
     */
    protected void performOperation(
       DatabaseFactory   dbfactory,
       Connection        cntConnection, 
       PreparedStatement pstmStatement
    ) throws OSSException,
             SQLException
    {
       // Override this method to provide actual commands to update the database
    }   
 
    /**
     * Define content of this method to perform the prepare data (update 
     * dataobject attribute).
     * 
     * @param data - data object the attributes will be updated for 
     */   
    protected void prepareData(
       DataObject data
    )
    {
       // Override this method to provide actual commands to update the data 
       // object
    }
 
    /**
     * Method sets values to the prepared statement for insert of data object. 
     *
     * @param insertStatement - prepared statement the values will be set up for
     * @param data - data object to insert, based on the type of the data object
     *               it can be determined what data are we inserting
     * @param initialIndex - initial index for values to be set up into statement
     * @return int - index of the last parameter in prepared statement (can be 
     *               used for later processing outside of this method)   
     * @throws OSSException - exception during setting values
     * @throws SQLException - exception during setting values
     */
    protected int setValuesForInsert(
       PreparedStatement insertStatement,
       DataObject        data,
       int               initialIndex
    ) throws OSSException, 
             SQLException
    {
       // Override this method to provide changes in set up values for insert
       return ((BasicDatabaseFactory)m_factory).setValuesForInsert(
                   insertStatement, data, initialIndex);
    }
 
    /**
     * Method sets values to the prepared statement for update of data object. 
     *
     * @param updateStatement - prepared statement the values will be set up for
     * @param data - data object to update, based on the type of the data object
     *               it can be determined what data are we updating
     * @param initialIndex - initial index for values to be set up into statement
     * @return int - index of the last parameter in prepared statement (can be 
     *               used for later processing outside of this method)   
     * @throws OSSException - exception during setting values
     * @throws SQLException - exception during setting values
     */
    protected int setValuesForUpdate(
       PreparedStatement updateStatement,
       DataObject        data,
       int               initialIndex               
    ) throws OSSException, 
             SQLException
    {
       // Override this method to provide changes in set up values for update
       return ((ModifiableDatabaseFactory)m_factory).setValuesForUpdate(
                   updateStatement, data, initialIndex);
    }
 
    /**
     * Provide custom handling of SQL Exceptions to usually detect constraint
     * violation. By default just handle it as unknown error.
     * 
     * @param sqleExc - SQLException to handle
     * @param cntConnection - ready to use connection to perform the database
     *                        operation. No need to return this connection. At 
     *                        this point we don't know if this is just a single 
     *                        operation and we need to rollback or if it is a 
     *                        part of bigger transaction and the rollback is not 
     *                        desired until all operations proceed. Therefore 
     *                        before this method was called, the operation has 
     *                        already let the DatabaseTransactionFactory resolve 
     *                        if rollback was necessary and if it was the operation
     *                        was already rolled back. 
     * @param iOperationType - type of the operation that caused the exception, 
     *                         see DatabaseOperations for possible values
     * @param iDataType - data type the data object represents (e.g if this is
     *                    type user and data is Integer, that means it is id
     *                    of user object). This is one of the DataConstant 
     *                    constants.
     * @param strDisplayableViewName - displayable name for the view of the data 
     *                                 object instance. This name allows different 
     *                                 views of the same data objects to be called 
     *                                 differently to make them more user friendly.  
     * @param data - data object the exception is handled for 
     * @throws OSSException - properly handled exception
     */
    protected void handleSQLException(
       SQLException sqleExc,
       Connection   cntConnection,
       int          iOperationType,
       int          iDataType,
       String       strDisplayableViewName,
       Object       data
    ) throws OSSException
    {
       if (m_dbschema != null)
       {
          m_dbschema.handleSQLException(
             sqleExc, cntConnection, iOperationType, iDataType, 
             strDisplayableViewName, data);
       }
       else
       {
          // By default just handle it as unknown error
          handleUnknownError(sqleExc, cntConnection, iOperationType, iDataType, 
                             strDisplayableViewName, data);
       }
    }
    
    /**
     * Override this method to provide any custom error handling for expected 
     * error, which were most likely produced by lower layer. The default
     * implementation just propagates this error.
 
     * @param exc - known error which must be handled.
     * @param cntConnection - ready to use connection to perform the database
     *                        operation. No need to return this connection. At 
     *                        this point we don't know if this is just a single 
     *                        operation and we need to rollback or if it is a 
     *                        part of bigger transaction and the rollback is not 
     *                        desired until all operations proceed. Therefore 
     *                        before this method was called, the operation has 
     *                        already let the DatabaseTransactionFactory resolve 
     *                        if rollback was necessary and if it was the operation
     *                        was already rolled back. 
     * @param iOperationType - type of the operation that caused the exception, 
     *                         see DatabaseOperations for possible values
     * @param iDataType - data type the data object represents (e.g if this is
     *                    type user and data is Integer, that means it is id
     *                    of user object). This is one of the DataConstant 
     *                    constants.
     * @param strDisplayableViewName - displayable name for the view of the data 
     *                                 object instance. This name allows different 
     *                                 views of the same data objects to be called 
     *                                 differently to make them more user friendly.  
     * @param data - data object the exception is handled for 
     * @throws OSSException - properly handled exception
     */
    protected void handleKnownError(
       OSSException exc,
       Connection   cntConnection,
       int          iOperationType,
       int          iDataType,
       String       strDisplayableViewName,
       Object       data
    ) throws OSSException
    {
       throw exc;
    }
    
    /**
     * Override this method to provide any custom error handling for unexpected 
     * error, which weren't handled by lower layer. The default implementation
     * wraps it in proper error based on the type of operation.
     * 
     * @param thr - throwable causing this error. This is not OSSException
     *              or a derived class.
     * @param cntConnection - ready to use connection to perform the database
     *                        operation. No need to return this connection. At 
     *                        this point we don't know if this is just a single 
     *                        operation and we need to rollback or if it is a 
     *                        part of bigger transaction and the rollback is not 
     *                        desired until all operations proceed. Therefore 
     *                        before this method was called, the operation has 
     *                        already let the DatabaseTransactionFactory resolve 
     *                        if rollback was necessary and if it was the operation
     *                        was already rolled back. 
     * @param iOperationType - type of the operation that caused the exception, 
     *                         see DatabaseOperations for possible values
     * @param iDataType - data type the data object represents (e.g if this is
     *                    type user and data is Integer, that means it is id
     *                    of user object). This is one of the DataConstant 
     *                    constants.
     * @param strDisplayableViewName - displayable name for the view of the data 
     *                                 object instance. This name allows different 
     *                                 views of the same data objects to be called 
     *                                 differently to make them more user friendly.  
     * @param data - data object the exception is handled for 
     * @throws OSSException - properly handled exception
     */
    protected void handleUnknownError(
       Throwable  thr,
       Connection cntConnection,
       int        iOperationType,
       int        iDataType,
       String     strDisplayableViewName,
       Object     data
    ) throws OSSException
    {
       switch (iOperationType)
       {
          case (DBOP_INSERT) :
          {
             throw new OSSDataCreateException(
                           "Failed to create " + strDisplayableViewName 
                           + " data in the database.", thr);
          }
          case (DBOP_UPDATE) :
          {
             throw new OSSDataSaveException(
                          "Failed to update " + strDisplayableViewName 
                          + " data in the database.", thr);
          }
          case (DBOP_DELETE) :
          {
             throw new OSSDataDeleteException(
                          "Failed to delete " + strDisplayableViewName 
                          + " data from the database.", thr);
          }
          default:
          {
             if (GlobalConstants.ERROR_CHECKING)
             {
                assert false : "Unknown database update type " + iOperationType;
             }               
          }
       }
    }
 
    /**
     * Rollback transaction and ignore any exception if it occurs.
     * The rollback will be performed only if this is a single operation which
     * is not managed as part of multiconnection transaction.
     * 
     * @param cntConnection - connection to rollback if necessary
     */
    protected void rollbackAndIgnoreException(
       Connection cntConnection
    )
    {
       try
       {
          // At this point we don't know if this is just a single operation
          // and we need to commit or if it is a part of bigger transaction
          // and the commit is not desired until all operations proceed. 
          // Therefore let the DatabaseTransactionFactory resolve it 
          DatabaseTransactionFactoryImpl.getInstance().rollbackTransaction(
                                                          cntConnection);
       }
      catch (SQLException sqleExc)
       {
          // Ignore this
          s_logger.log(Level.WARNING, 
                       "Failed to rollback current transaction", 
                       sqleExc);
       }
      catch (OSSException osseExc)
      {
         // Ignore this
         s_logger.log(Level.WARNING, 
                      "Failed to rollback current transaction", 
                      osseExc);
      }
    }
 }
