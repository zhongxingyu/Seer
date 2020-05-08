 /*
 * Copyright (C) 2003 - 2013 OpenSubsystems.com/net/org and its owners. All rights reserved.
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
 
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import org.opensubsystems.core.data.DataObject;
 import org.opensubsystems.core.error.OSSException;
 import org.opensubsystems.core.persist.jdbc.Database;
 import org.opensubsystems.core.persist.jdbc.DatabaseFactory;
 import org.opensubsystems.core.persist.jdbc.DatabaseSchema;
 import org.opensubsystems.core.util.GlobalConstants;
 import org.opensubsystems.core.util.TwoIntStruct;
 import org.opensubsystems.core.util.jdbc.DatabaseUtils;
 
 /**
  * Base class for adapter to simplify writing of database operations, which 
  * should take care of requesting and returning connections, transaction 
  * management and exception handling. 
  * 
  * @author bastafidli
  */
 public abstract class DatabaseOperation
 {
    // Attributes ///////////////////////////////////////////////////////////////
    
    /**
     * Factory which is executing this operation.
     */
    protected DatabaseFactory m_factory;
    
    /**
     * Attribute to store data to return from executeUpdate.
     */
    protected Object m_returnData;
    
    /**
     * Query specified by the caller to prepare if any.
     */
    protected String m_strQuery;
 
    /**
     * Schema which is executing this operation. The meaning of the data is 
     * defined by the derived class.
     */
    protected DatabaseSchema m_dbschema;
 
    /**
     * Data used for this operation.
     */
    protected Object m_data;
 
    // Constructors /////////////////////////////////////////////////////////////
    
    /**
     * Constructor to use when the database operation doesn't require any 
     * prepared statement.
     * 
     * @param factory - factory which is executing this operation
     */
    public DatabaseOperation(
       DatabaseFactory factory
    )
    {
       this(factory, null);
    }
 
    /**
     * Constructor to use when database operation requires prepared statement.
     * 
     * @param factory - factory which is executing this operation
     * @param strQueryToPrepare - query which should be used to construct 
     *                            prepared statement that will be passed in to 
     *                            executeUpdate. If null the no statement is 
     *                            prepared.
     */
    public DatabaseOperation(
       DatabaseFactory factory,
       String          strQueryToPrepare
    )
    {
       super();
       
       m_factory = factory;
       m_strQuery = strQueryToPrepare;
    }
 
    /**
     * Full constructor to use when database operation doesn't require any prepared 
     * statement.
     * 
     * @param factory - factory which is executing this operation
     * @param strQueryToPrepare - query which should be used to construct 
     *                            prepared statement that will be passed in to 
     *                            executeUpdate. If null the no statement is 
     *                            prepared.
     * @param schema - database schema used with this operation
     * @param data - data used for this operation. The meaning of the data is 
     *               defined by the derived class.
     */
    public DatabaseOperation(
       DatabaseFactory factory,
       String          strQueryToPrepare,
       DatabaseSchema  schema,
       Object          data
    )
    {
       super();
       
       m_factory   = factory;
       m_strQuery  = strQueryToPrepare;
       m_dbschema  = schema;
       m_data      = data;
    }
 
    // Logic ////////////////////////////////////////////////////////////////////
    
    /**
     * Get data which should be returned from method which actually implements
     * the database operation
     * 
     * @return Object - data set by executeUpdate using set method
     */
    public Object getReturnData(
    )
    {
       return m_returnData;
    }
 
    /**
     * Set data which should be returned from method which actually implements
     * the database operation
     * 
     * @param returnData - data set by executeUpdate using set method
     */
    public void setReturnData(
       Object returnData
    )
    {
       m_returnData = returnData;
    }
    
    // Helper methods ///////////////////////////////////////////////////////////
    
    /**
     * Execute statement and load multiple strings from the result set. If there 
     * are no results null will be returned. The prepared statement should be
     * constructed with the appropriate result set type and concurrency to allow
     * absolute positioning if the database supports it to retrieve the data
     * efficiently.
     * 
     * Note: Since the caller constructed the prepared statement, it is responsible
     * for closing it.
     * 
     * @param factory - factory used to load data
     * @param pstmQuery - query to execute
     * @return List - list of loaded strings or null if there were no results
     * @throws SQLException - an error has occurred
     * @throws OSSException - an error has occurred
     */
    protected List<String> loadMultipleStrings(
       DatabaseFactory   factory,
       PreparedStatement pstmQuery
    ) throws SQLException,
             OSSException
    {
       ResultSet    rsQueryResults = null;
       List<String> lstData = null;
       
       try
       {
          int iCount;
          
          rsQueryResults = pstmQuery.executeQuery();
          iCount = estimateResultSetSize(factory, rsQueryResults);
          if (iCount != 0)
          {
             if (iCount >  0)
             {
                // Use ArrayList since it is fast, lightweight and we know the count 
               lstData = new ArrayList<>(iCount);
                // We have to call next here because we have to call next below
                // so that we know if to allocate data
                rsQueryResults.next();
             }
             else
             {
                // If there is not absolute positioning supported we don't know 
                // at this moment if there are some data in the resultset. If 
                // there are not, don't create array list 'lstData'.
                // We cannot user first() and beforeFirst() since we don know
                // id absolute positioning is supported so lets
                // call next and add call to next above and convert the loop
                // below to do while
                if (rsQueryResults.next())
                {
                   // Here we do not know the count so we do not know how many 
                   // items to load, for now use ArrayList even though it may be 
                   // slow if it has to expand a lot
                  lstData = new ArrayList<>();
                }
             }
             if (lstData != null)
             {
                do
                {
                   lstData.add(rsQueryResults.getString(1));
                }
                while (rsQueryResults.next());
             }
          }
       }
       finally
       {
          DatabaseUtils.closeResultSet(rsQueryResults);
       }
        
       return lstData;
    }
 
    /**
     * Execute statement and load multiple int's from the result set. If there 
     * are no results null will be returned. The prepared statement should be
     * constructed with the appropriate result set type and concurrency to allow
     * absolute positioning if the database supports it to retrieve the data
     * efficiently.
     * 
     * Note: Since the caller constructed the prepared statement, it is 
     * responsible for closing it.
     * 
     * @param factory - factory used to load data
     * @param pstmQuery - query to execute
     * @return int[] - list of loaded int's or null if there were no results
     * @throws SQLException - an error has occurred
     * @throws OSSException - an error has occurred
     */
    protected int[] loadMultipleIntsAsArray(
       DatabaseFactory   factory,
       PreparedStatement pstmQuery
    ) throws SQLException,
             OSSException
    {
       ResultSet rsQueryResults = null;
       int[]     arrData = null;
       
       try
       {
          int iCount;
          
          rsQueryResults = pstmQuery.executeQuery();
          iCount = estimateResultSetSize(factory, rsQueryResults);
          if (iCount != 0)
          {
             int iActualCount = 0;
             if (iCount >  0)
             {
                // Use ArrayList since it is fast, lightweight and we know the count 
                arrData = new int[iCount];
                while (rsQueryResults.next())
                {  
                   arrData[iActualCount++] = rsQueryResults.getInt(1);
                }
             }
             else
             {
                // Here we do not know the count so we do not know how many items
                // to load, for now use ArrayList even though it may be slow
                // if it has to expand a lot
                if (rsQueryResults.next())
                {
                  List<Integer> lstData = new ArrayList<>();
                   do
                   {  
                      lstData.add(new Integer(rsQueryResults.getInt(1)));
                   }
                   while (rsQueryResults.next());
                   arrData = new int[lstData.size()];
                   for (Iterator<Integer> items = lstData.iterator(); items.hasNext();)
                   {
                      arrData[iActualCount++] = (items.next()).intValue();
                   }
                }
             }
          }
       }
       finally
       {
          DatabaseUtils.closeResultSet(rsQueryResults);
       }
        
       return arrData;
    }
 
    /**
     * Execute statement and load multiple TwoIntStruct objects from the result 
     * set. If there are no results null will be returned. The prepared statement 
     * should be constructed with the appropriate result set type and concurrency 
     * to allow absolute positioning if the database supports it to retrieve the 
     * data efficiently. 
     * 
     * Note: Since the caller constructed the prepared statement, it is responsible
     * for closing it.
     * 
     * @param factory - factory used to load data
     * @param pstmQuery - query to execute
     * @return TwoIntStruct[] - list of loaded int's or null if there were no results
     * @throws SQLException - an error has occurred
     * @throws OSSException - an error has occurred
     */
    protected TwoIntStruct[] loadMultipleTwoIntStruct(
       DatabaseFactory   factory,
       PreparedStatement pstmQuery
    ) throws SQLException,
             OSSException
    {
       ResultSet      rsQueryResults = null;
       TwoIntStruct[] arrData = null;
       
       try
       {
          int iCount;
          
          rsQueryResults = pstmQuery.executeQuery();
          iCount = estimateResultSetSize(factory, rsQueryResults);
          if (iCount != 0)
          {
             int iActualCount = 0;
             int iItem1;
             int iItem2;
             if (iCount >  0)
             {
                arrData = new TwoIntStruct[iCount];
                while (rsQueryResults.next())
                {  
                   iItem1 = rsQueryResults.getInt(1);
                   iItem2 = rsQueryResults.getInt(2);
    
                   arrData[iActualCount++] = new TwoIntStruct(iItem1, iItem2);
                }
             }
             else
             {
                // Here we do not know the count so we do not know how many items
                // to load, for now use ArrayList even though it may be slow
                // if it has to expand a lot
                if (rsQueryResults.next())
                {
                  List<TwoIntStruct> lstData = new ArrayList<>();
                   do
                   {  
                      lstData.add(new TwoIntStruct(rsQueryResults.getInt(1),
                                                   rsQueryResults.getInt(2)));
                   }
                   while (rsQueryResults.next());
                   arrData = new TwoIntStruct[lstData.size()];
                   for (Iterator<TwoIntStruct> items = lstData.iterator(); 
                        items.hasNext();)
                   {
                      arrData[iActualCount++] = items.next();
                   }
                }
             }
          }
       }
       finally
       {
          DatabaseUtils.closeResultSet(rsQueryResults);
       }
        
       return arrData;
    }
 
    /**
     * Execute statement and load at most one data object from the result set and 
     * if the result set contains more than one item announce error.
     * 
     * Note: Since the caller constructed the prepared statement, it is responsible
     * for closing it.
     * 
     * @param factory - factory used to load data
     * @param pstmQuery - query to execute
     * @param strErrorMessage - error message to announce if there is more than 
     *                          one item
     * @return DataObject - loaded data object or null if the result set was empty
     * @throws SQLException - an error has occurred
     * @throws OSSException - an error has occurred
     */
    protected DataObject loadAtMostOneData(
       DatabaseFactory   factory,
       PreparedStatement pstmQuery,
       String            strErrorMessage
    ) throws SQLException,
             OSSException
    {
       DataObject data = null;
       ResultSet  rsQueryResults = null;
       
       try
       {
          rsQueryResults = pstmQuery.executeQuery();
          while (rsQueryResults.next())
          {
             if (GlobalConstants.ERROR_CHECKING)
             {
                assert data == null : strErrorMessage;
             }
    
             data = factory.load(rsQueryResults, 1);
          }
       }
       finally
       {
          DatabaseUtils.closeResultSet(rsQueryResults);
       }
       
       return data;
    }
 
    /**
     * Execute statement and load multiple data objects from the result set. If 
     * there are no items null will be returned. The prepared statement should be
     * constructed with the appropriate result set type and concurrency to allow
     * absolute positioning if the database supports it to retrieve the data
     * efficiently.
     * 
     * Note: Since the caller constructed the prepared statement, it is responsible
     * for closing it.
     * 
     * @param factory - factory used to load data
     * @param pstmQuery - query to execute
     * @return List - list of loaded data objects or null if there were no results
     * @throws SQLException - an error has occurred
     * @throws OSSException - an error has occurred
     */
    protected List<DataObject> loadMultipleData(
       DatabaseFactory   factory,
       PreparedStatement pstmQuery
    ) throws SQLException,
             OSSException
    {
       ResultSet        rsQueryResults = null;
       List<DataObject> lstData = null;
       
       try
       {
          int iCount;
          
          rsQueryResults = pstmQuery.executeQuery();
          iCount = estimateResultSetSize(factory, rsQueryResults);
          if (iCount != 0)
          {
             if (iCount >  0)
             {
                // Use ArrayList since it is fast, lightweight and we know the count 
               lstData = new ArrayList<>(iCount);
                // We have to call next here because we have to call next below
                // so that we know if to allocate data
                rsQueryResults.next();
             }
             else
             {
                // If there is not absolute positioning supported we don't know 
                // at this moment if there are some data in the resultset. If 
                // there are not, don't create array list 'lstData'.
                // We cannot user first() and beforeFirst() since we don know
                // id absolute positioning is supported so lets
                // call next and add call to next above and convert the loop
                // below to do while
                if (rsQueryResults.next())
                {
                   // Here we do not know the count so we do not know how many 
                   // items to load, for now use ArrayList even though it may be 
                   // slow if it has to expand a lot
                  lstData = new ArrayList<>();
                }
             }
             if (lstData != null)
             {
                do
                {
                   lstData.add(factory.load(rsQueryResults, 1));
                }
                while (rsQueryResults.next());
             }
          }
       }
       finally
       {
          DatabaseUtils.closeResultSet(rsQueryResults);
       }
        
       return lstData;
    }
 
    /**
     * Execute statement and load multiple data objects from the result set. If 
     * there are no items null will be returned. The prepared statement should be
     * constructed with the appropriate result set type and concurrency to allow
     * absolute positioning if the database supports it to retrieve the data
     * efficiently.
     * 
     * Note: Since the caller constructed the prepared statement, it is responsible
     * for closing it.
     * 
     * @param factory - factory used to load data
     * @param pstmQuery - query to execute
     * @return Set - set of loaded data objects or null if there were no results
     * @throws SQLException - an error has occurred
     * @throws OSSException - an error has occurred
     */
    protected Set<DataObject> loadMultipleDataToSet(
       DatabaseFactory   factory,
       PreparedStatement pstmQuery
    ) throws SQLException,
             OSSException
    {
       ResultSet   rsQueryResults = null;
       Set<DataObject> returnSet = null;
       
       try
       {
          int iCount;
          
          rsQueryResults = pstmQuery.executeQuery();
          iCount = estimateResultSetSize(factory, rsQueryResults);
          if (iCount != 0)
          {
             if (iCount >  0)
             {
                // Use HashSet since it is fast, lightweight and we know the count
               returnSet = new HashSet<>(iCount);
                // We have to call next here because we have to call next below
                // so that we know if to allocate data
                rsQueryResults.next();
             }
             else
             {
                // If there is not absolute positioning supported we don't know 
                // at this moment if there are some data in the resultset. If 
                // there are not, don't create array list 'lstData'.
                // We cannot user first() and beforeFirst() since we don know
                // id absolute positioning is supported so lets
                // call next and add call to next above and convert the loop
                // below to do while
                if (rsQueryResults.next())
                {
                   // Here we do not know the count so we do not know how many 
                   // items to load, for now use HashSet even though it may be 
                   // slow if it has to expand a lot
                  returnSet = new HashSet<>();
                }
             }
             if (returnSet != null)
             {
                do
                {
                   // load all columns from resultset and set up whole object
                   // to the HashSet
                   returnSet.add(factory.load(rsQueryResults, 1));   
                }
                while (rsQueryResults.next());
             }
          }
       }
       finally
       {
          DatabaseUtils.closeResultSet(rsQueryResults);
       }
        
       return returnSet;
    }
 
    /**
     * Execute statement and load multiple string objects from the 1st column of 
     * the result set. If there are no items null will be returned. The prepared 
     * statement should be constructed with the appropriate result set type and 
     * concurrency to allow absolute positioning if the database supports it to 
     * retrieve the data efficiently.
     * 
     * Note: Since the caller constructed the prepared statement, it is responsible
     * for closing it.
     * 
     * @param factory - factory used to load data
     * @param pstmQuery - query to execute
     * @return Set - set of loaded data objects or null if there were no results
     * @throws SQLException - an error has occurred
     * @throws OSSException - an error has occurred
     */
    protected Set<String> loadMultipleStringsToSet(
       DatabaseFactory   factory,
       PreparedStatement pstmQuery,
       boolean           bLoadSpecific
    ) throws SQLException,
             OSSException
    {
       ResultSet   rsQueryResults = null;
       Set<String> returnSet = null;
       
       try
       {
          int iCount;
          
          rsQueryResults = pstmQuery.executeQuery();
          iCount = estimateResultSetSize(factory, rsQueryResults);
          if (iCount != 0)
          {
             if (iCount >  0)
             {
                // Use HashSet since it is fast, lightweight and we know the count
               returnSet = new HashSet<>(iCount);
                // We have to call next here because we have to call next below
                // so that we know if to allocate data
                rsQueryResults.next();
             }
             else
             {
                // If there is not absolute positioning supported we don't know 
                // at this moment if there are some data in the resultset. If 
                // there are not, don't create array list 'lstData'.
                // We cannot user first() and beforeFirst() since we don know
                // id absolute positioning is supported so lets
                // call next and add call to next above and convert the loop
                // below to do while
                if (rsQueryResults.next())
                {
                   // Here we do not know the count so we do not know how many 
                   // items to load, for now use HashSet even though it may be 
                   // slow if it has to expand a lot
                  returnSet = new HashSet<>();
                }
             }
             if (returnSet != null)
             {
                do
                {
                   // load just one specific column from resultset
                   returnSet.add(rsQueryResults.getString(1));
                }
                while (rsQueryResults.next());
             }
          }
       }
       finally
       {
          DatabaseUtils.closeResultSet(rsQueryResults);
       }
        
       return returnSet;
    }
 
    /**
     * Estimate the size of the result set so that data structures can be 
     * allocated efficiently.
     * 
     * @param factory - factory used to load data
     * @param rsQueryResults - resultset of size of which to estimate
     * @return int - if the size can be estimated it returns the size, otherwise
     *               it will return -1
     * @throws SQLException - an error has occurred
     * @throws OSSException - an error has occurred
     */
    protected static int estimateResultSetSize(
       DatabaseFactory factory,
       ResultSet       rsQueryResults
    ) throws SQLException,
             OSSException
    {
       // Initialize to negative so that we can recognize when we actually 
       // know the count and when we do not know it
       int iCount = -1;
       
       // If the database supports absolute positioning and it is efficient 
       // (so that by general we can't use it to determine size of results)
       // instead of executing the query with count() we can determine the count
       // to better allocate memory
       Database database = factory.getDatabase();
       
       if ((database.hasAbsolutePositioningSupport())
          && (!database.preferCountToLast()))
       {
          // It seems like absolute positioning is efficient so use it to 
          // determine since of result set so it can allocate optimal amount 
          // of memory
          if (rsQueryResults.last())
          {
             // The last row number will tell us the row count (since it is 1 based)
             iCount = rsQueryResults.getRow();
             rsQueryResults.beforeFirst();
          }
          else
          {
             // It is empty
             iCount = 0;
          }
       }
       
       return iCount;
    }
 }
