 /*
 * Copyright (C) 2005 - 2012 OpenSubsystems.com/net/org and its owners. All rights reserved.
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
 import java.util.Collection;
 import java.util.Iterator;
 
 import org.opensubsystems.core.data.BasicDataObject;
 import org.opensubsystems.core.error.OSSException;
 import org.opensubsystems.core.persist.jdbc.BasicDatabaseFactory;
 import org.opensubsystems.core.persist.jdbc.Database;
 import org.opensubsystems.core.persist.jdbc.DatabaseFactory;
 import org.opensubsystems.core.persist.jdbc.ModifiableDatabaseSchema;
 
 /**
  * Adapter to simplify writing of batched database inserts, which takes care of
  * requesting and returning connections, transaction management and 
  * exception handling. To use this adapter you just need to create an instance
  * of this class and call executeUpdate method.
  *
  * Example of method in factory which creates collection of data using batch 
  * insert: 
  *
  * public int create(
  *    final Collection colDataObject
  * ) throws OSSException
  * {
  *    DatabaseBatchCreateOperation dbop = new DatabaseBatchCreateOperation(
  *       this, m_schema.getInsertMyData(), m_schema, dataType, colDataObject);
  *    dbop.executeUpdate();
  *      
  *    return ((Integer)dbop.getReturnData()).intValue();
  * }
  *
  * @author bastafidli
  */
 public class DatabaseCreateMultipleDataObjectsOperation<T> extends DatabaseUpdateOperation
 {
    // Attributes ///////////////////////////////////////////////////////////////
 
    /**
     * Flag signaling if there will be fetched generated values.
     */
    private boolean m_bFetchGeneratedValues;
 
    // Constructors /////////////////////////////////////////////////////////////
    
    /**
     * Constructor
     * 
     * @param factory - factory which is executing this operation
     * @param query - query to insert data
     * @param schema - schema to set the data to the statement
     * @param colDataObject - collection of data objects that will be created
     * @param bFetchGeneratedValues - flag signaling if there have to be returned 
     *                                generated values
     *                                true = there will be returned generated values
     *                                false = will be returned number of inserted 
     *                                records
     */
    public DatabaseCreateMultipleDataObjectsOperation(
       DatabaseFactory          factory,
       String                   query,
       ModifiableDatabaseSchema schema,
       Collection<T>            colDataObject,
       boolean                  bFetchGeneratedValues
    ) 
    {
       super(factory, query, schema, DatabaseUpdateOperation.DBOP_INSERT, colDataObject);
       
       m_bFetchGeneratedValues = bFetchGeneratedValues;
    }
 
    // Helper methods ///////////////////////////////////////////////////////////
    
    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    protected void performOperation(
       DatabaseFactory   dbfactory,
       Connection        cntConnection, 
       PreparedStatement pstmQuery
    ) throws OSSException,
             SQLException
    {
       int[]           arrInsertedReturn;
       int             iBatchedCount = 0;
       Iterator<T>     items;
      BasicDataObject data = null;
       int             size;
       int             iTotalInsertedReturn = 0;
      int             iIndex = 0;
       int             iBatchSize;
       Database        database;
       
       database = dbfactory.getDatabase();
       iBatchSize = database.getBatchSize();
       
       if (!m_bFetchGeneratedValues)
       {
          size =  ((Collection<T>)m_data).size();
          for (items = ((Collection<T>)m_data).iterator(); items.hasNext();)
          {
             data = (BasicDataObject)items.next();
             // prepare data if necessary (update object values)
             prepareData(data);
             // set values for prepared statement
             setValuesForInsert(pstmQuery, data, 1);
             pstmQuery.addBatch();
             iBatchedCount++;
    
             // test if there is time to execute batch
             if (((iBatchedCount % iBatchSize) == 0) 
                || (iBatchedCount == size))
             {
                arrInsertedReturn = pstmQuery.executeBatch();
                iTotalInsertedReturn += arrInsertedReturn.length; 
             }
          }
          // TODO: Performance: Consider defining setReturnData(int)
          // so we do not have to create extra object
          setReturnData(new Integer(iTotalInsertedReturn));
       }
       else
       {
          for (items = ((Collection<T>)m_data).iterator(); items.hasNext();)
          {
             data = (BasicDataObject)items.next();
             // prepare data if necessary (update object values)
             prepareData(data);
             pstmQuery.clearParameters();
             iIndex = ((BasicDatabaseFactory)dbfactory).setValuesForInsert(
                                                           pstmQuery, data, 1);
             database.insertAndFetchGeneratedValues(
                cntConnection, pstmQuery, 
                m_dbschema.isInDomain(), 
                ((ModifiableDatabaseSchema)m_dbschema).getModifiableTableNames().get(
                   m_factory.getDataDescriptor().getDataTypeAsObject()).toString(), 
                iIndex, data);
          }
          setReturnData(m_data);
       }      
    }
 }
