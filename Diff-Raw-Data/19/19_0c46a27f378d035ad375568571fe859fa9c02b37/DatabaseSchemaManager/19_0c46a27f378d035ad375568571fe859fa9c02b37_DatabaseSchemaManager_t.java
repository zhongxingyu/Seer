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
 
 package org.opensubsystems.core.persist.jdbc;
 
 import java.util.HashMap;
 import java.util.Map;
import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.opensubsystems.core.error.OSSException;
 import org.opensubsystems.core.util.ClassFactory;
 import org.opensubsystems.core.util.GlobalConstants;
 import org.opensubsystems.core.util.Log;
 import org.opensubsystems.core.util.OSSObject;
 
 /**
  * Class responsible for instantiation of database schemas. This class determines
  * what database schema should be used based on currently used database, creates
  * the schema instance if it wasn't created yet and caches created instances.
  * This of course assumes that the data factories are implemented to be stateless 
  * and reentrant.
  *
  * @author bastafidli
  */
 //TODO: JDK 1.5: All Manager classes could be refactored using template classes 
 public class DatabaseSchemaManager extends OSSObject
 {
    // Constants ////////////////////////////////////////////////////////////////
    
    /**
     * Lock used in synchronized sections.
     */
    private static final String IMPL_LOCK = "IMPL_LOCK";
 
    // Attributes ///////////////////////////////////////////////////////////////
    
    /**
     * Class factory used to instantiate database schemas.
     */
    protected ClassFactory<DatabaseSchema> m_schemaClassFactory;
    
    /**
     * Cache where already instantiated database schemas will be cached. We can 
     * cache them since database schemas should be reentrant.
     */
   private final Map<String, DatabaseSchema> m_mpSchemaCache; 
    
    // Cached values ////////////////////////////////////////////////////////////
 
    /**
     * Logger for this class
     */
    private static Logger s_logger = Log.getInstance(DatabaseSchemaManager.class);
    
    /**
     * Reference to the instance actually in use.
     */
    private static DatabaseSchemaManager s_defaultInstance;
 
    // Constructors /////////////////////////////////////////////////////////////
 
    /**
     * Default constructor.
     * 
     * @throws OSSException - an error has occurred 
     */
    public DatabaseSchemaManager(
    ) throws OSSException
    {
       m_schemaClassFactory = new DatabaseSchemaClassFactory();
      m_mpSchemaCache = new HashMap<>();
    }
    
    /**
     * Create database schema for specified class.
     * 
     * @param clsDatabaseSchema - the database schema class for which we want 
     *                            applicable schema. This is usually database 
     *                            independent class and we will try to create 
     *                            database dependent class.  
     * @return DatabaseSchema - the database schema to use for given class
     * @throws OSSException - an error has occurred 
     */
    public static DatabaseSchema getInstance(
       Class<? extends DatabaseSchema> clsDatabaseSchema
    ) throws OSSException
    {
       return getManagerInstance().getSchemaInstance(clsDatabaseSchema);
    }
    
    /**
     * Create database schema for specified class.
     * 
     * @param strDatabaseSchemaClassname - the database schema class name for 
     *                                     which we want applicable schema. This 
     *                                     is usually database independent class 
     *                                     and we will try to create database 
     *                                     dependent class.  
     * @return DatabaseSchema - the database schema to use for given class
     * @throws OSSException - an error has occurred 
     */
    public static DatabaseSchema getInstance(
       String strDatabaseSchemaClassname
    ) throws OSSException
    {
       return getManagerInstance().getSchemaInstance(strDatabaseSchemaClassname);
    }
    
    /**
     * Get the default instance. This method is here to make the schema manager
     * configurable. Once can specify in configuration file derived class to used
     * instead of this one [DatabaseSchemaManager.class]=new class to use.
     *
     * @return DatabaseSchemaManager
     * @throws OSSException - cannot get current database
     */
    public static DatabaseSchemaManager getManagerInstance(
    ) throws OSSException
    {
       if (s_defaultInstance == null)
       {
          // Only if the default instance wasn't set by other means create a new one
          // Synchronize just for the creation
          synchronized (IMPL_LOCK)
          {
             if (s_defaultInstance == null)
             {
                ClassFactory<DatabaseSchemaManager> cf;
                
               cf = new ClassFactory<>(DatabaseSchemaManager.class);
                setManagerInstance(cf.createInstance(DatabaseSchemaManager.class, 
                                                     DatabaseSchemaManager.class));
             }
          }   
       }
       
       return s_defaultInstance;
    }
    
    /**
     * Set default instance. This instance will be returned by getManagerInstance 
     * method until it is changed.
     *
     * @param defaultInstance - new default instance
     * @see #getManagerInstance
     */
    public static void setManagerInstance(
       DatabaseSchemaManager defaultInstance
    )
    {
       if (GlobalConstants.ERROR_CHECKING)
       {
          assert defaultInstance != null : "Default instance cannot be null";
       }   
       
       synchronized (IMPL_LOCK)
       {
          s_defaultInstance = defaultInstance;
         s_logger.log(Level.FINE, "Default database schema manager is {0}", 
                      s_defaultInstance.getClass().getName());
       }   
    }
 
    /**
     * Method to create actual schema based on specified class. This method can
     * be overridden and new manager can be setup either through setManagerInstance
     * or through configuration file if different strategy is desired.
     * 
     * @param clsDatabaseSchema - the database schema class for which we want 
     *                            applicable schema. This is usually database 
     *                            independent class and we will try to create 
     *                            database dependent class.  
     * @return DatabaseSchema - the database schema to use for given class
     * @throws OSSException - an error has occurred
     */
    public DatabaseSchema getSchemaInstance(
       Class<? extends DatabaseSchema> clsDatabaseSchema
    ) throws OSSException
    {
       DatabaseSchema schema;
       
       schema = m_mpSchemaCache.get(clsDatabaseSchema.getName());
       if (schema == null)
       {
          synchronized (m_mpSchemaCache)
          {
             schema = m_schemaClassFactory.createInstance(clsDatabaseSchema);
             m_mpSchemaCache.put(clsDatabaseSchema.getName(), schema);
          }
       }
       
       return (DatabaseSchema)schema; 
    }
 
    /**
     * Method to create actual schema based on specified class. This method can
     * be overridden and new manager can be setup either through setManagerInstance
     * or through configuration file if different strategy is desired.
     * 
     * @param strDatabaseSchemaClassname - the database schema class name for 
     *                                     which we want applicable schema. This 
     *                                     is usually database independent class 
     *                                     and we will try to create database 
     *                                     dependent class.  
     * @return DatabaseSchema - the database schema to use for given class
     * @throws OSSException - an error has occurred
     */
    public DatabaseSchema getSchemaInstance(
       String strDatabaseSchemaClassname
    ) throws OSSException
    {
       DatabaseSchema schema;
       
       schema = m_mpSchemaCache.get(strDatabaseSchemaClassname);
       if (schema == null)
       {
          synchronized (m_mpSchemaCache)
          {
             schema = m_schemaClassFactory.createInstance(
                                              strDatabaseSchemaClassname);
             m_mpSchemaCache.put(strDatabaseSchemaClassname, schema);
          }
       }
       
       return (DatabaseSchema)schema; 
    }
 }
