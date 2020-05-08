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
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.Properties;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.opensubsystems.core.error.OSSDatabaseAccessException;
 import org.opensubsystems.core.error.OSSException;
 import org.opensubsystems.core.persist.jdbc.DatabaseSchema;
 import org.opensubsystems.core.persist.jdbc.operation.DatabaseOperations;
 import org.opensubsystems.core.util.Config;
 import org.opensubsystems.core.util.GlobalConstants;
 import org.opensubsystems.core.util.Log;
 import org.opensubsystems.core.util.OSSObject;
 import org.opensubsystems.core.util.PropertyUtils;
 import org.opensubsystems.core.util.jdbc.DatabaseUtils;
 
 /**
  * Base class implementation for database schemas that provides the common
  * functionality needed by all schemas that allow to read data from the tables
  * managed by this schema.
  *  
  * @author bastafidli
  */
 public abstract class DatabaseSchemaImpl extends OSSObject
                                          implements DatabaseSchema, 
                                                     DatabaseOperations
 {
    // Configuration settings ///////////////////////////////////////////////////
 
    /** 
     * Name of the property that can specify default prefix to use for database 
     * objects.
     */   
    public static final String DATABASE_SCHEMA_PREFIX = "oss.dbschema.prefix";
 
    // Constants ////////////////////////////////////////////////////////////////
    
    /**
     * Default prefix to use for database objects.
     */
    public static final String DATABASE_SCHEMA_PREFIX_DEFAULT = "BF_";
    
    /**
     * New line constant which should be used when creating any table or stored
     * procedure in the database. We can reverse engineer them from database e.g.
     * using CaseStudio but if they do not contain new lines then they appear as
     * single long string and it is impossible to find out what has changed.
     */
    public static final String NL = "\n";
    
    // Attributes ///////////////////////////////////////////////////////////////
 
    /**
     * Array of dependent schemas.
     */
    protected DatabaseSchema[] m_arrDependentSchemas;
 
    /**
     * Name of the schema.
     */
    protected String m_strSchemaName;
 
    /**
     * Version of the schema.
     */
    protected int m_iSchemaVersion;
 
    /**
     * Flag signaling if object is in domain.
     */
    protected boolean m_bIsInDomain;
 
    // Cached values ////////////////////////////////////////////////////////////
    
    /**
     * Logger for this class
     */
    private static Logger s_logger = Log.getInstance(DatabaseSchemaImpl.class);
 
    // Constructors /////////////////////////////////////////////////////////////
    
    /**
     * Constructor allowing to specify attributes for database schema that 
     * doesn't provide any operations that would modify any of the tables it is 
     * accessing.
     * 
     * @param arrDependentSchemas - array of dependent schemas
     * @param strSchemaName - name of the schema
     * @param iSchemaVersion - version of the schema
     * @param bIsInDomain - flag signaling if object is in domain
     * @throws OSSException - an error has occurred
     */
    public DatabaseSchemaImpl(
       DatabaseSchema[] arrDependentSchemas,
       String           strSchemaName,
       int              iSchemaVersion,
       boolean          bIsInDomain
    ) throws OSSException
    {
       super();
       
       m_arrDependentSchemas = arrDependentSchemas;
       m_strSchemaName = strSchemaName;
       m_iSchemaVersion = iSchemaVersion;
       m_bIsInDomain = bIsInDomain;
    }
 
    // Logic ////////////////////////////////////////////////////////////////////
    
    /**
     * {@inheritDoc}
     */
   @Override
    public boolean isInDomain()
    {
       return m_bIsInDomain;
    }
 
    /**
     * {@inheritDoc}
     */
   @Override
    public DatabaseSchema[] getDependentSchemas(
    ) throws OSSException
    {
       return m_arrDependentSchemas;
    }
 
    /**
     * {@inheritDoc}
     */
   @Override
    public String getName(
    )
    {
       return m_strSchemaName;
    }
    
    /**
     * {@inheritDoc}
     */
   @Override
    public int getVersion(
    )
    {
       return m_iSchemaVersion;
    }
    
    /**
     * {@inheritDoc}
     */
   @Override
    public void upgrade(
       Connection cntDBConnection,
       String strUserName,
       int iOriginalVersion
    ) throws SQLException
    {
       // Most schemas do not provide upgrade method unless they were shipped
       // to customer and then they were changed. Therefore this empty method
       // shields the derived classes from the need to implement this method
    }
    
    /**
     * Get prefix which should be used to construct database objects.
     * 
     * @return String - prefix to use for database objects.
     */
    public static String getSchemaPrefix(
    )
    {
       // Read it here instead of in static block or constructor since if this 
       // code is executed in different execution context, it might have 
       // different configuration settings.
       Properties prpSettings;
       String     strSchemaPrefix;
       
       prpSettings = Config.getInstance().getProperties();
       strSchemaPrefix = PropertyUtils.getStringProperty(
                              prpSettings, DATABASE_SCHEMA_PREFIX, 
                              DATABASE_SCHEMA_PREFIX_DEFAULT, 
                              "Defaut database table prefix", false);
       if (strSchemaPrefix.length() > DATABASE_SCHEMA_PREFIX_DEFAULT.length())
       {
          s_logger.config("Length of " + DATABASE_SCHEMA_PREFIX + " value is" +
                          " longer than the default value " + 
                          DATABASE_SCHEMA_PREFIX_DEFAULT + ". This may cause" +
                          " issues for some databases.");
       }
       
       return strSchemaPrefix;
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
          case (DBOP_SELECT) :
          {
             throw new OSSDatabaseAccessException(
                          "Failed to read " + strDisplayableViewName 
                          + " data from the database.", exc);
          }
          default:
          {
             if (GlobalConstants.ERROR_CHECKING)
             {
                assert false : "Unknown operation type " + iOperationType;
             }               
          }
       }
    }
    
    // Helper methods ///////////////////////////////////////////////////////////
    
    /**
     * Upgrade the view by dropping it and recreating it again. Developer has to 
     * make sure that no view depends on it since the depending view will be 
     * dropped too. This method works if the schema contain only a single view.
     *
     * @param cntDBConnection - valid connection to database
     * @param strUserName - name of user who will be accessing this table
     * @param iOriginalVersion - original version from which to upgrade
     * @param strViewName - view to upgrade
     * @throws SQLException - problem creating the database schema
     * @throws OSSException - problem creating the database schema
     */
    protected void upgradeView(
       Connection cntDBConnection,
       String     strUserName,
       int        iOriginalVersion,
       String     strViewName
    ) throws SQLException, OSSException
    {
       // The faster way to upgrade a view is to drop it and recreate it
       Statement stmQuery = null;
       try
       {        
          int iVersion = iOriginalVersion;
          
          stmQuery = cntDBConnection.createStatement();
          
          if (iVersion < getVersion())
          {
             if (stmQuery.execute("drop view " + strViewName))
             {
                // Close any results
                stmQuery.getMoreResults(Statement.CLOSE_ALL_RESULTS);      
             }
            s_logger.log(Level.FINEST, "View {0} deleted.", strViewName);
 
             // And now recreate it again
             create(cntDBConnection, strUserName);
          }
       }
       catch (SQLException sqleExc)
       {
          s_logger.log(Level.WARNING, "Failed to upgrade schema" + getName() +
                " from version " + iOriginalVersion + " to version " + getVersion(), 
                sqleExc);
          throw sqleExc;
       }
       finally
       {
          DatabaseUtils.closeStatement(stmQuery);
       }
    }
 }
