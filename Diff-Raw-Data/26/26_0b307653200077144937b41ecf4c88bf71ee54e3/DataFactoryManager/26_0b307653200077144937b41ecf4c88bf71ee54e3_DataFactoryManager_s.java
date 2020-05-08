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
 
 package org.opensubsystems.core.persist;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Logger;
 
 import org.opensubsystems.core.error.OSSDynamicClassException;
 import org.opensubsystems.core.error.OSSException;
 import org.opensubsystems.core.error.OSSInternalErrorException;
 import org.opensubsystems.core.persist.jdbc.DatabaseFactoryClassFactory;
 import org.opensubsystems.core.util.ClassFactory;
 import org.opensubsystems.core.util.GlobalConstants;
 import org.opensubsystems.core.util.Log;
 import org.opensubsystems.core.util.OSSObject;
 
 /**
  * Class responsible for instantiation of data factories. This class determines
  * what data factory should be used based on currently used persistence mechanism, 
  * creates the factory instance if it wasn't created yet and caches created 
  * instances. This of course assumes that the data factories are implemented to
  * be stateless and reentrant.
  *
  * @author bastafidli
  */
 // TODO: JDK 1.5: All Manager classes could be refactored using template classes 
 public class DataFactoryManager extends OSSObject
 {
    // Constants ////////////////////////////////////////////////////////////////
    
    /**
     * Lock used in synchronized sections.
     */
    private static final String IMPL_LOCK = "IMPL_LOCK";
 
    // Attributes ///////////////////////////////////////////////////////////////
    
    /**
     * Class factory used to instantiate data factories.
     */
    protected ClassFactory<? extends DataFactory> m_factoryClassFactory;
    
    /**
     * Cache where already instantiated data factories will be cached. We can 
     * cache them since data factories should be reentrant. Key is the data 
     * factory interface class, value is the data factory instance.
     */
   private Map<String, DataFactory> m_mpClassFactoryCache; 
    
    /**
     * Cache where already instantiated data factories will be cached. We can 
     * cache them since data factories should be reentrant. Key is the data type 
     * view supported by the factory instance, value is the data factory instance.
     * 
     * This should work because each factory should be constructed as soon as the
     * application starts because the BackendModule interface requires that the 
     * modules publish list of data factories they use. Since all the factories
     * are constructed ahead of the time, we can create mapping between the data
     * type view and the factory instances.
     */
   private Map<String, DataFactory> m_mpViewFactoryCache; 
    
    // Cached values ////////////////////////////////////////////////////////////
 
    /**
     * Logger for this class
     */
    private static Logger s_logger = Log.getInstance(DataFactoryManager.class);
    
    /**
     * Reference to the instance actually in use.
     */
    private static DataFactoryManager s_defaultInstance;
 
    // Constructors /////////////////////////////////////////////////////////////
 
    /**
     * Default constructor.
     */
    public DataFactoryManager(
    )
    {
       m_factoryClassFactory = new DatabaseFactoryClassFactory();
      m_mpClassFactoryCache = new HashMap<String, DataFactory>();
      m_mpViewFactoryCache  = new HashMap<String, DataFactory>();
    }
    
    /**
     * Create data factory for specified class.
     * 
     * @param clsDataFactory - the data factory interface for which we want 
     *                         applicable factory. This is usually persistence 
     *                         independent class and we will try to create 
     *                         dependent class, such as one which can persist
     *                         data in database.  
     * @return DataFactory - the data factory to use for given interface
     * @throws OSSException - an error has occurred 
     */
    public static DataFactory getInstance(
      Class<DataFactory> clsDataFactory
    ) throws OSSException
    {
       return getManagerInstance().getFactoryInstance(clsDataFactory);
    }
    
    /**
     * Get instance of data factory for specified data type view if it was 
     * already constructed before using the getInstance method call.
     * 
     * @param strDataTypeView - identifier of the data type view the factory will  
     *                          be used for, since the same type of data objects
     *                          can be retrieved and presented to clients in 
     *                          multiple type of views 
     * @return DataFactory - the data factory to use for given data type view
     * @throws OSSException - an error has occurred 
     */
    public static DataFactory getInstanceForView(
       String strDataTypeView
    ) throws OSSException
    {
       return getManagerInstance().getFactoryInstanceForView(strDataTypeView);
    }
    
    /**
     * Get the default instance. This method is here to make the factory manager
     * configurable. Once can specify in configuration file derived class to used
     * instead of this one [DataFactoryManager.class]=new class to use.
     *
     * @return DatabaseFactoryManager
     * @throws OSSException - cannot get current database
     */
    public static DataFactoryManager getManagerInstance(
    ) throws OSSException
    {
       if (s_defaultInstance == null)
       {
          // Only if the default instance wasn't set by other means create a new one
          // Synchronize just for the creation
          synchronized (IMPL_LOCK)
          {
             ClassFactory<DataFactoryManager> cf;
             
            cf = new ClassFactory<DataFactoryManager>(DataFactoryManager.class);
             setManagerInstance(cf.createInstance(DataFactoryManager.class, 
                                                  DataFactoryManager.class));
          }   
       }
       
       return s_defaultInstance;
    }
    
    /**
     * Set default instance. This instance will be returned by getInstance 
     * method until it is changed.
     *
     * @param defaultInstance - new default instance
     * @see #getManagerInstance
     */
    public static void setManagerInstance(
       DataFactoryManager defaultInstance
    )
    {
       if (GlobalConstants.ERROR_CHECKING)
       {
          assert defaultInstance != null : "Default instance cannot be null";
       }   
       
       synchronized (IMPL_LOCK)
       {
          s_defaultInstance = defaultInstance;
         s_logger.fine("Default data factory manager is " 
                       + s_defaultInstance.getClass().getName());
       }   
    }
 
    /**
     * Method to create actual factory based on specified class. This method can
     * be overridden and new manager can be setup either through setManagerInstance
     * or through configuration file if different strategy is desired.
     * 
     * @param clsDataFactory - the data factory interface for which we want 
     *                         applicable factory. This is usually persistence 
     *                         independent class and we will try to create 
     *                         dependent class, such as one which can persist
     *                         data in database.  
     * @return DataFactory - the data factory to use for given interface
     * @throws OSSException - an error has occurred
     */
    public DataFactory getFactoryInstance(
       Class<? extends DataFactory> clsDataFactory
    ) throws OSSException
    {
       DataFactory factory;
       
       factory = m_mpClassFactoryCache.get(clsDataFactory.getName());
       if (factory == null)
       {
          synchronized (m_mpClassFactoryCache)
          {
             // TODO: Improve: This is suppose to be
             // factory = m_factoryClassFactory.createInstance(clsDataFactory);
             // but I am getting compiler error.
             factory = m_factoryClassFactory.createInstance(clsDataFactory.getName());
             // Use name and not the instance as a key since if the class is loaded
             // through different class loader, it wouldn't match
             m_mpClassFactoryCache.put(clsDataFactory.getName(), factory);
          }
          synchronized (m_mpViewFactoryCache)
          {
             DataFactory existingFactory;
             String      strViewName;
             
             strViewName = factory.getDataDescriptor().getViewName();
             existingFactory = m_mpViewFactoryCache.get(strViewName); 
             if ((existingFactory != null)
                && (!existingFactory.getClass().getName().equals(
                      factory.getClass().getName())))
             {
                throw new OSSInternalErrorException(
                   "There is already an existing data factory cached for the"
                   + " data type view " + strViewName
                   + ". The existing data factory class is "
                   + existingFactory.getClass().getName()
                   + ". The new data factory class is "
                   + factory.getClass().getName());
             }
             else
             {
                // Once cached we can retrieve it using getFactoryInstance
                // It should be cached as soon as the BackendModule instance is
                // created when it is being added to the application
                m_mpViewFactoryCache.put(strViewName, factory);
             }
          }
       }
       
       return factory; 
    }
 
    /**
     * Get instance of data factory for specified data type view if it was 
     * already constructed before using the getFactoryInstance method call.
     * 
     * @param strDataTypeView - identifier of the data type view the factory will  
     *                          be used for, since the same type of data objects
     *                          can be retrieved and presented to clients in 
     *                          multiple type of views 
     * @return DataFactory - the data factory to use for given data type view
     * @throws OSSException - an error has occurred
     */
    public DataFactory getFactoryInstanceForView(
       String strDataTypeView
    ) throws OSSException
    {
       DataFactory factory;
       
       factory = m_mpViewFactoryCache.get(strDataTypeView);
       if (factory == null)
       {
          throw new OSSDynamicClassException(
                       "Data factory that support data type view " 
                       + strDataTypeView + " has not been constructed yet and"
                       + " therefore it is not available.");
       }
 
       return factory; 
    }
 }
