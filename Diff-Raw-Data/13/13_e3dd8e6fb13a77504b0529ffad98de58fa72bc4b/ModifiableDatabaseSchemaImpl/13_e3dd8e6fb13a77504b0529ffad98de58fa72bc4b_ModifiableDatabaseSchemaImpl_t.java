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
 
 package org.opensubsystems.core.persist.jdbc.impl;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Timestamp;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.opensubsystems.core.data.DataDescriptor;
 import org.opensubsystems.core.data.DataDescriptorManager;
 import org.opensubsystems.core.data.ModifiableDataObject;
 import org.opensubsystems.core.error.OSSConcurentModifyException;
 import org.opensubsystems.core.error.OSSDataCreateException;
 import org.opensubsystems.core.error.OSSDataDeleteException;
 import org.opensubsystems.core.error.OSSDataSaveException;
 import org.opensubsystems.core.error.OSSDatabaseAccessException;
 import org.opensubsystems.core.error.OSSException;
 import org.opensubsystems.core.error.OSSInternalErrorException;
 import org.opensubsystems.core.error.OSSInvalidContextException;
 import org.opensubsystems.core.persist.jdbc.DatabaseSchema;
 import org.opensubsystems.core.persist.jdbc.ModifiableDatabaseSchema;
 import org.opensubsystems.core.util.jdbc.DatabaseUtils;
 
 /**
  * Base class implementation for database schemas that provide queries or 
  * operations that allow to modify data in tables managed by this schema. 
  *  
  * @author bastafidli
  */
 public abstract class ModifiableDatabaseSchemaImpl extends    DatabaseSchemaImpl
                                                    implements ModifiableDatabaseSchema
 {
    // Attributes ///////////////////////////////////////////////////////////////
 
    /**
     * Map of all tables that can be modified by the operations provided by this 
     * database schema. The operations that can modify the data include for example 
     * insert, update or delete of data in this table. Key is the data type, value 
     * is the string with the table name.
     */
    protected Map<Integer, String> m_mpModifiableTableNames;
 
    // Constructors /////////////////////////////////////////////////////////////
    
    /**
     * Constructor allowing to specify attributes for database schema that 
     * support multiple data object types and therefore have multiple sets of 
     * tables that can be modified and columns sets. 
     * 
     * @param arrDependentSchemas - array of dependent schemas
     * @param strSchemaName - name of the schema
     * @param iSchemaVersion - version of the schema
     * @param bIsInDomain - flag signaling if object is in domain
     * @param mpModifiableTableNames - map of all tables belonging to this schema
     *                                 that can be modified by the schema (e.g. 
     *                                 schema allows insert, update or delete on 
     *                                 this table). Key is the data type, value 
     *                                 is the table name.
     * @throws OSSException - an error has occurred
     */
    public ModifiableDatabaseSchemaImpl(
       DatabaseSchema[]     arrDependentSchemas,
       String               strSchemaName,
       int                  iSchemaVersion,
       boolean              bIsInDomain,
       Map<Integer, String> mpModifiableTableNames
    ) throws OSSException
    {
       super(arrDependentSchemas, strSchemaName, iSchemaVersion, bIsInDomain);
       
       m_mpModifiableTableNames = mpModifiableTableNames;
    }
 
    /**
     * Constructor allowing to specify attributes for database schema that 
     * support only single data object type and therefore have most likely only 
     * one table name that can be modified and single columns sets. 
     * 
     * @param arrDependentSchemas - array of dependent schemas
     * @param strSchemaName - name of the schema
     * @param iSchemaVersion - version of the schema
     * @param iModifiableDataType - data type that can be modified using 
     *                              operations in this schema
     * @param strModifiableTableName - table belonging to this schema that can be 
     *                                 modified by the schema (e.g. schema allows
     *                                 insert, update or delete on this table)
     * @param bIsInDomain - flag signaling if object is in domain
     * @throws OSSException - an error has occurred
     */
    public ModifiableDatabaseSchemaImpl(
       DatabaseSchema[] arrDependentSchemas,
       String           strSchemaName,
       int              iSchemaVersion,
       boolean          bIsInDomain,
       Integer          iModifiableDataType,
       String           strModifiableTableName
    ) throws OSSException
    {
       super(arrDependentSchemas, strSchemaName, iSchemaVersion, bIsInDomain);
       
       if ((strModifiableTableName != null) && (strModifiableTableName.length() > 0))
       {
         m_mpModifiableTableNames = new HashMap<>(1);
          m_mpModifiableTableNames.put(iModifiableDataType, strModifiableTableName);
       }
    }
 
    /**
     * Constructor allowing to specify attributes for database schema that 
     * support only single data object type and therefore have most likely only 
     * one table name that can be modified and single columns sets. 
     * 
     * @param arrDependentSchemas - array of dependent schemas
     * @param strSchemaName - name of the schema
     * @param iSchemaVersion - version of the schema
     * @param clsDataDescriptor - data descriptor describing the data type that 
     *                            can be modified using operations in this schema
     * @param strModifiableTableName - table belonging to this schema that can be 
     *                                 modified by the schema (e.g. schema allows
     *                                 insert, update or delete on this table)
     * @param bIsInDomain - flag signaling if object is in domain
     * @throws OSSException - an error has occurred
     */
    public ModifiableDatabaseSchemaImpl(
       DatabaseSchema[]      arrDependentSchemas,
       String                strSchemaName,
       int                   iSchemaVersion,
       boolean               bIsInDomain,
       Class<DataDescriptor> clsDataDescriptor,
       String                strModifiableTableName
    ) throws OSSException
    {
       super(arrDependentSchemas, strSchemaName, iSchemaVersion, bIsInDomain);
       
       if ((strModifiableTableName != null) 
          && (strModifiableTableName.length() > 0))
       {
          DataDescriptor dataDescriptor;
          Integer        iModifiableDataType;
          
          dataDescriptor = DataDescriptorManager.getInstance(clsDataDescriptor);
          iModifiableDataType = dataDescriptor.getDataTypeAsObject();
          
         m_mpModifiableTableNames = new HashMap<>(1);
          m_mpModifiableTableNames.put(iModifiableDataType, strModifiableTableName);
       }
    }
 
    // Logic ////////////////////////////////////////////////////////////////////
 
    /**
     * {@inheritDoc}
     */
    @Override
    public int deleteRelatedData(
       Connection cntDBConnection,
       int        iDataType,
       long       lId
    ) throws OSSException, 
             SQLException
    {
       // Default implementation does nothing
       return 0;
    }
 
    // Helper methods ///////////////////////////////////////////////////////////
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Map<Integer, String> getModifiableTableNames(
    )
    {
       return m_mpModifiableTableNames;
    }
 
    /**
     * {@inheritDoc}
     */
    @Override
    public void handleSQLException(
       SQLException exc,
       Connection   dbConnection,
       int          iOperationType, 
       int          iDataType,
       String       strDisplayableViewName,
       Object       data
    ) throws OSSException
    {
       switch (iOperationType)
       {
          case (DBOP_INSERT) :
          {
             throw new OSSDataCreateException(
                           "Failed to create data in the database.", exc);
          }
          case (DBOP_UPDATE) :
          {
             // This will detects conflict when two users tries to modify the 
             // same data and one of them saves the changes and then other tries 
             // to overwrite them without knowing that the data were modified
             if ((exc.getMessage().indexOf("[100]") > -1)
                && (data instanceof ModifiableDataObject))
             {
                Integer iDataTypeCode = new Integer(iDataType);
                
                checkUpdateError(
                   dbConnection, 
                   strDisplayableViewName, 
                   (String)getModifiableTableNames().get(iDataTypeCode),
                   ((ModifiableDataObject)data).getId(), 
                   ((ModifiableDataObject)data).getModificationTimestamp());
             }
             // If this wasn't concurrent modification, throw the generic error
             throw new OSSDataSaveException(
                          "Failed to update data in the database.", exc);
          }
          case (DBOP_DELETE) :
          {
             throw new OSSDataDeleteException(
                          "Failed to delete data from the database.", exc);
          }
          default:
          {
             super.handleSQLException(exc, dbConnection, iOperationType, 
                                      iDataType, strDisplayableViewName, data);
          }
       }
    }
 
    /**
     * {@inheritDoc}
     */
   @Override
    public void checkUpdateError(
       Connection dbConnection,
       String     strDataName,
       String     strTableName,
       long       lId,
       Timestamp  tmstpModificationDate
    ) throws OSSException
    {
       PreparedStatement psCheck = null;
       ResultSet         rsCheck = null;
       
       try
       {
         StringBuilder sbBuffer = new StringBuilder();
          
          // first try to select from DB existing data identified by ID
          sbBuffer.append("select ID, MODIFICATION_DATE from ");
          sbBuffer.append(strTableName);
          sbBuffer.append(" where ID = ?");
          
          psCheck = dbConnection.prepareStatement(sbBuffer.toString());
          psCheck.setLong(1, lId);
          rsCheck = psCheck.executeQuery();
       
          if (rsCheck.next())
          {
             // if there was found data within the DB, check it's modification timestamp
             if (rsCheck.getTimestamp("MODIFICATION_DATE").equals(tmstpModificationDate))
             {
                // there was found data object with specified id and modification 
                // date throw internal exception since this shouldn't happen
                throw new OSSInternalErrorException(strDataName + 
                   " has been found within the database but it has not been updated.");
             }
             else
             {
                // There was found data object with specified id but with different 
                // modification date, throw concurrent modify exception
                throw new OSSConcurentModifyException(strDataName + 
                   " has been meanwhile modified by somebody else. Your" +
                   " modifications were not performed to prevent overwriting" +
                   " data you have not seen yet. We suggest you save your changes" +
                   " to a temporary location, reload the data you are trying to" +
                   " modify, review the changes that have been meanwhile done by" +
                   " somebody else and then redo your changes if necessary.");
             }
          }
          else
          {
             throw new OSSInvalidContextException(
                strDataName + " does not exists anymore." +
                " It might have been meanwhile deleted from the system.");
          }
       }
       catch (SQLException eExc)
       {
          throw new OSSDatabaseAccessException(
                       "Unexpected error accessing the database.", eExc);
       }
       finally
       {
          DatabaseUtils.closeResultSetAndStatement(rsCheck, psCheck);
       }
    }
 }
