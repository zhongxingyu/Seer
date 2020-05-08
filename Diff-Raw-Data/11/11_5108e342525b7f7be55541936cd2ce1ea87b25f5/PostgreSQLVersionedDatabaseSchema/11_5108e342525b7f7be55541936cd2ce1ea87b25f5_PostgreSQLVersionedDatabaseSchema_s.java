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
 
 package org.opensubsystems.core.persist.jdbc.database.postgresql;
 
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.opensubsystems.core.error.OSSDatabaseAccessException;
 import org.opensubsystems.core.error.OSSException;
 import org.opensubsystems.core.persist.jdbc.DatabaseSchema;
 import org.opensubsystems.core.persist.jdbc.impl.DatabaseTransactionFactoryImpl;
 import org.opensubsystems.core.persist.jdbc.impl.VersionedDatabaseSchemaImpl;
 import org.opensubsystems.core.util.Log;
 import org.opensubsystems.core.util.jdbc.DatabaseUtils;
 
 /**
  * Database specific operations related to persistence of database schemas in 
  * PostgreSQL.
  *
  * @author OpenSubsystems
  */
 public class PostgreSQLVersionedDatabaseSchema extends VersionedDatabaseSchemaImpl
 {
    /*
       Database tables used to track database schemas in the database
       The real name can be different based on prefix
 
       CREATE TABLE BF_SCHEMA
       (
          ID SERIAL,
          SCHEMA_NAME VARCHAR(50) NOT NULL,
          SCHEMA_VERSION INTEGER NOT NULL,
          CREATION_DATE TIMESTAMP WITH TIME ZONE NOT NULL,
          MODIFICATION_DATE TIMESTAMP WITH TIME ZONE NOT NULL,
          CONSTRAINT BF_SCH_PK PRIMARY KEY (ID),
          CONSTRAINT BF_SCH_UQ UNIQUE (SCHEMA_NAME)
       );
    */
 
    // Cached values ////////////////////////////////////////////////////////////
 
    /**
     * Logger for this class
     */
    private static Logger s_logger = Log.getInstance(
                                        PostgreSQLVersionedDatabaseSchema.class);
 
    // Constructors /////////////////////////////////////////////////////////////
    
    /**
     * @throws OSSException - database cannot be started.
     */
    public PostgreSQLVersionedDatabaseSchema(
    ) throws OSSException
    {
       super();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
   public void loadExistingSchemas(
       Connection                  cntDBConnection,
       Map<String, DatabaseSchema> mpSchemasToAdd,
       Map<String, Integer>        mpSchemasToUpgrade
    ) throws OSSException
    {
       super.loadExistingSchemas(cntDBConnection, mpSchemasToAdd, 
                                 mpSchemasToUpgrade);
 
       try
       {
          DatabaseTransactionFactoryImpl.getInstance().commitTransaction(
                                                          cntDBConnection);
       }
       catch (Throwable thr)
       {
          s_logger.log(Level.SEVERE, "Failed to initialize database.", 
                              thr);
          if (cntDBConnection != null)
          {
             try
             {
                // At this point we don't know if this is just a single operation
                // and we need to commit or if it is a part of bigger transaction
                // and the commit is not desired until all operations proceed. 
                // Therefore let the DatabaseTransactionFactory resolve it 
                DatabaseTransactionFactoryImpl.getInstance().rollbackTransaction(
                                                        cntDBConnection);
             }
             catch (SQLException sqleExc)
             {
                // Ignore this
                s_logger.log(Level.WARNING, 
                             "Failed to rollback changes for creation of database.", 
                             sqleExc);
             }
          }
          throw new OSSDatabaseAccessException("Failed to initialize database.", 
                                               thr);
       }
    }
 
    /**
     * {@inheritDoc}
     */
    public void create(
       Connection cntDBConnection, 
       String strUserName
    ) throws SQLException, 
             OSSException
    {
       s_logger.entering(this.getClass().getName(), "create");
 
       try
       {
          // For PostgreSQL database we need to create user defined type so that
          // we can use it to create the database table
          Statement stmQuery = null;
          try
          {
 
             stmQuery = cntDBConnection.createStatement();
             
             if (stmQuery.execute("CREATE TYPE type_int_timestamp AS ("
                   + "intgr INTEGER, tmstp TIMESTAMP WITH TIME ZONE)"))
             {
                // Close any results
                stmQuery.getMoreResults(Statement.CLOSE_ALL_RESULTS);
             }
             s_logger.log(Level.FINEST, "Type 'type_int_timestamp' created.");
          }
          catch (SQLException sqleExc)
          {
             // Catch this just so we can log the message
             s_logger.log(Level.WARNING, "Failed to create int - timestamp type.",
                                 sqleExc);
             throw sqleExc;
          }
          finally
          {
             DatabaseUtils.closeStatement(stmQuery);
             stmQuery = null;
          }
          
          try
          {
             stmQuery = cntDBConnection.createStatement();
             if (stmQuery.execute(
                "create table " + SCHEMA_TABLE_NAME + NL + 
                "(" + NL + 
                "   ID SERIAL," + NL +
                "   SCHEMA_NAME VARCHAR(" + SCHEMA_NAME_MAXLENGTH + ") NOT NULL," + NL +
                "   SCHEMA_VERSION INTEGER NOT NULL," + NL +
                "   CREATION_DATE TIMESTAMP WITH TIME ZONE NOT NULL," + NL +
                "   MODIFICATION_DATE TIMESTAMP WITH TIME ZONE NOT NULL," + NL +
                "   CONSTRAINT " + getSchemaPrefix() + "SCH_PK PRIMARY KEY (ID)," + NL +
                "   CONSTRAINT " + getSchemaPrefix() + "SCH_UQ UNIQUE (SCHEMA_NAME)" + NL +
                ")"))
             {
                // Close any results
                stmQuery.getMoreResults(Statement.CLOSE_ALL_RESULTS);
             }
            s_logger.log(Level.FINEST, "Table " + SCHEMA_TABLE_NAME + " created.");
          }
          catch (SQLException sqleExc)
          {
             // Catch this just so we can log the message
             s_logger.log(Level.WARNING, "Failed to create version schema.",
                                 sqleExc);
             throw sqleExc;
          }
          finally
          {
             DatabaseUtils.closeStatement(stmQuery);
          }
       }
       finally
       {
          s_logger.exiting(this.getClass().getName(), "create");
       }
    }
 }
