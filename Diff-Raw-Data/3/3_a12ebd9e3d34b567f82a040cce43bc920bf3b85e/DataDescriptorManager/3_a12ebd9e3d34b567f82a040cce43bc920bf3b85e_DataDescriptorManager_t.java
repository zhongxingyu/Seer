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
  
 package org.opensubsystems.core.data;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.opensubsystems.core.error.OSSException;
 import org.opensubsystems.core.error.OSSInternalErrorException;
 import org.opensubsystems.core.util.ClassFactory;
 import org.opensubsystems.core.util.GlobalConstants;
 import org.opensubsystems.core.util.Log;
 import org.opensubsystems.core.util.OSSObject;
 
 /**
  * Class responsible for instantiation of data descriptors. This class 
  * determines what data descriptor class should be used based on current 
  * configuration, creates the data descriptor instance if it wasn't created yet 
  * and caches the created  instances. This of course assumes that the data 
  * descriptors are implemented to be stateless and reentrant.
  *
  * @author bastafidli
  */
 //TODO: JDK 1.5: All Manager classes could be refactored using template classes 
 public class DataDescriptorManager extends OSSObject
 {
    // Constants ////////////////////////////////////////////////////////////////
    
    /**
     * Lock used in synchronized sections.
     */
    private static final String IMPL_LOCK = "IMPL_LOCK";
 
    // Attributes ///////////////////////////////////////////////////////////////
    
    /**
     * Class factory used to instantiate data descriptors.
     */
    protected ClassFactory<DataDescriptor> m_descriptorClassFactory;
    
    /**
     * Cache where already instantiated data descriptors will be cached. We can 
     * cache them since data descriptors should be reentrant. Key is the class 
     * name of the descriptor and value is the descriptor itself.
     */
    private final Map<String, DataDescriptor> m_mpDescriptorCache; 
    
    /**
     * Map that is used to transform the desired data types to real data types.
     * The key is the value of the desired data type code and the value is the 
     * real data type code. If no mapping is defined the desired data type code 
     * will be used as the real data type code unless a conflict is detected.  
     * Each data descriptor defines code for its desired data type. In distributed
     * development it is possible that two module developers use the same data 
     * type code for two different data types. When an application developer tries 
     * to construct an application using these two modules, he would encounter a 
     * conflict because the data type codes are the same. Using this map the 
     * application developer can remap one of the modules to use a different data 
     * type to avoid the conflict. Using this scenario the first module would use
     * as its real data type the value of the desired one specified by the data
     * descriptor while the second module would use the remapped value and instead
     * of the desired value specified in thats module data descriptor the module
     * will use the value specified in this map. 
     */
    protected Map<Integer, Integer> m_mpDesiredDataTypeMap;
    
    /**
     * Map of all data descriptors organized by the data type of the descriptor. 
     * Key is data type, value is list of data descriptors for this data type. 
     * The first one in the list is the default one, the rest of them if any are 
     * representing different views of the same data type.
     */
    protected Map<Integer, List<DataDescriptor>> m_mpDataTypeToDescriptors;
    
    /**
     * Map of logical names of all views. Key is the logical name of the view and 
     * value is the data type Integer code.
     */
    protected Map<String, Integer> m_mpDataTypeViews;
 
    // Cached values ////////////////////////////////////////////////////////////
 
    /**
     * Logger for this class
     */
    private static Logger s_logger = Log.getInstance(DataDescriptorManager.class);
    
    /**
     * Reference to the instance actually in use.
     */
    private static DataDescriptorManager s_defaultInstance;
 
    // Constructors /////////////////////////////////////////////////////////////
 
    /**
     * Default constructor.
     */
    public DataDescriptorManager(
    )
    {
       m_descriptorClassFactory = new ClassFactory<>(DataDescriptor.class);
       m_mpDescriptorCache = new HashMap<>();
       m_mpDataTypeToDescriptors = new HashMap<>();
       m_mpDataTypeViews = new HashMap<>();
       m_mpDesiredDataTypeMap = null;
    }
    
    // Logic ////////////////////////////////////////////////////////////////////
    
    /**
     * Create data descriptor for specified class.
     * 
     * @param clsDescriptor - the data descriptor class for which we want 
     *                        applicable data descriptor. 
     * @return DataDescriptor - data descriptor to use for given class
     * @throws OSSException - an error has occurred 
     */
    public static DataDescriptor getInstance(
       Class<? extends DataDescriptor> clsDescriptor
    ) throws OSSException
    {
       return getManagerInstance().getDataDescriptorInstance(clsDescriptor);
    }
    
    /**
     * Get the default instance. This method is here to make the data descriptor 
     * manager configurable. Once can specify in configuration file derived class 
     * to used instead of this one [DataDescriptorManager.class]=new class to use.
     *
     * @return DataDescriptorManager
     * @throws OSSException - an error has occurred
     */
    public static DataDescriptorManager getManagerInstance(
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
                Class<DataDescriptorManager> defaultManager = DataDescriptorManager.class;
                ClassFactory<DataDescriptorManager> cf;
                
               cf = new ClassFactory<>(DataDescriptorManager.class);
                setManagerInstance(cf.createInstance(defaultManager, 
                                                     defaultManager));
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
       DataDescriptorManager defaultInstance
    )
    {
       if (GlobalConstants.ERROR_CHECKING)
       {
          assert defaultInstance != null : "Default instance cannot be null";
       }   
       
       synchronized (IMPL_LOCK)
       {
          s_defaultInstance = defaultInstance;
          s_logger.log(Level.FINE, "Default data descriptor manager is {0}", 
                       s_defaultInstance.getClass().getName());
       }   
    }
 
    /**
     * Set the data type map which maps the desired data types into real data 
     * types in case they need to be remapped.
     * 
     * @param mpDesiredDataTypeMap - map that is used to transform the desired 
     *                               data types to real data types. The key is 
     *                               the value of the desired data type code and 
     *                               the value is the real data type code. If no 
     *                               mapping is defined the desired data type code 
     *                               will be used as the real data type code 
     *                               unless a conflict is detected. Each data 
     *                               descriptor defines code for its desired data 
     *                               type. In distributed development it is possible 
     *                               that two module developers use the same data 
     *                               type code for two different data types. When 
     *                               an application developer tries to construct 
     *                               an application using these two modules, he 
     *                               would encounter a conflict because the data 
     *                               type codes are the same. Using this map the 
     *                               application developer can remap one of the 
     *                               modules to use a different data type to avoid 
     *                               the conflict. Using this scenario the first 
     *                               module would use as its real data type the 
     *                               value of the desired one specified by the 
     *                               data descriptor while the second module would 
     *                               use the remapped value and instead of the 
     *                               desired value specified in thats module data 
     *                               descriptor the module will use the value 
     *                               specified in this map. 
     */
    public void setDesiredDataTypeMap(
       Map<Integer, Integer> mpDesiredDataTypeMap
    ) throws OSSException
    {
       if ((m_mpDescriptorCache != null) && (!m_mpDescriptorCache.isEmpty()))
       {
          throw new OSSInternalErrorException(
                       "The desired data type map cannot be set once any data"
                       + " descriptors were created ");
       }
       m_mpDesiredDataTypeMap = mpDesiredDataTypeMap; 
    }
 
    /**
     * Method to create actual data descriptor based on specified class. This 
     * method can be overridden and new manager can be setup either through 
     * setManagerInstance or through configuration file if different strategy is 
     * desired.
     * 
     * @param clsDescriptor - the data descriptor class for which we want 
     *                        applicable data descriptor. 
     * @return DataDescriptor - data descriptor to use for given class
     * @throws OSSException - an error has occurred
     */
    public DataDescriptor getDataDescriptorInstance(
       Class<? extends DataDescriptor> clsDescriptor
    ) throws OSSException
    {
       DataDescriptor descriptor;
       
       descriptor = m_mpDescriptorCache.get(clsDescriptor.getName());
       if (descriptor == null)
       {
          synchronized (m_mpDescriptorCache)
          {
             descriptor = m_descriptorClassFactory.createInstance(clsDescriptor);
             initialize(descriptor);
          }
       }
       
       return descriptor; 
    }
 
    /**
     * Get data type code knowing just the logical name of the view displaying 
     * objects of that data type.
     * 
     * @param strViewName - logical name of the data type view
     * @return Integer - code of know data type represented by that logical view
     *                   or DataObject.NO_DATA_TYPE_OBJ if no such view exists.
     */
    public Integer getDataType(
       String strViewName
    )
    {
       if (GlobalConstants.ERROR_CHECKING)
       {
          assert strViewName != null : "Data type view cannot be null.";
       }
       
       Integer iDataTypeCode = m_mpDataTypeViews.get(strViewName);
       if (iDataTypeCode == null)
       {
          iDataTypeCode = DataObject.NO_DATA_TYPE_OBJ;
       }
       
       return iDataTypeCode;
    }
    
    /**
     * @return Map - Key is a Integer representing the data type of the data 
     *               descriptor module name, value is List of the data descriptors.
     *               The first one in the list is the default one while the other
     *               ones are descriptors for different views for the same data 
     *               type. 
     */
    public Map<Integer, List<DataDescriptor>> getDataDescriptors() 
    {
       return m_mpDataTypeToDescriptors;
    }
 
    /**
     * Get registered data descriptors for the specified data type. The first one
     * in the list is the default descriptor while the other one are descriptors
     * for different types of views for the specified data type.
     * 
     * @param iDataType - data type to get the descriptor for.
     * @return List - list of data for the specified data type or null if there 
     *                are none. The first one in the list is the default descriptor 
     *                while the other ones are descriptors for different views 
     *                for the same data type.
     */
    public List<DataDescriptor> getDataDescriptors(
       int iDataType
    )
    {
       return getDataDescriptors(new Integer(iDataType));
    }
    
    /**
     * Get registered data descriptors for the specified data type. The first one
     * in the list is the default descriptor while the other one are descriptors
     * for different types of views for the specified data type.
     * 
     * @param intDataType - data type to get the descriptor for.
     * @return List<DataDescriptor> - list of data for the specified data type 
     *                                or null if there are none. The first one is 
     *                                the list is the default descriptor while 
     *                                the other ones are descriptors for different 
     *                                views for the same data type.
     */
    public List<DataDescriptor> getDataDescriptors(
       Integer intDataType
    )
    {
       List<DataDescriptor> lstDataDescriptors;
       
       lstDataDescriptors = m_mpDataTypeToDescriptors.get(intDataType);      
       
       return lstDataDescriptors;
    }
 
    // Helper methods ///////////////////////////////////////////////////////////
    
    /**
     * Properly initialize data descriptor and all necessary internal structures.
     * 
     * @param descriptor - descriptor to add
     * @throws OSSException - an error has occurred
     */
    protected void initialize(
      DataDescriptor descriptor
    ) throws OSSException
    {
       Class<DataDescriptor> clsParentDescriptor;
       List<DataDescriptor>  lstDescriptors;
       
       clsParentDescriptor = descriptor.getParentDescriptorClass();
       if (clsParentDescriptor != null)
       {
          DataDescriptor parentDescriptor;
          
          do
          {
             parentDescriptor = m_mpDescriptorCache.get(
                                   clsParentDescriptor.getName());
             if (parentDescriptor == null)
             {
                // If it doesn't exist throw an exception because we cannot do 
                // anything. We need to find it in the application since we want 
                // one which has already adjusted data type.
                throw new OSSInternalErrorException(
                   "Parent data descriptor " + clsParentDescriptor.getName()
                   + " of data descriptor " + descriptor.getClass().getName()
                   + " doesn't exist in the application. The application may have"
                   + " not specified the data descriptors in modules in correct"
                   + " order or the dependencies between modules are not specified"
                   + " correctly.");
             }
             else
             {
                clsParentDescriptor = parentDescriptor.getParentDescriptorClass();
             }
          }
          while (clsParentDescriptor != null);
          
          // Set the same data type as the parent descriptor since there are
          // just two different views of the same type of data
          descriptor.setDataType(parentDescriptor.getDataType());
          
          // Add this descriptor to a map of data types to data descriptors so we 
          // can resolve them later if needed
          lstDescriptors = m_mpDataTypeToDescriptors.get(
                              descriptor.getDataTypeAsObject());
          if (GlobalConstants.ERROR_CHECKING)
          {
             assert lstDescriptors != null 
                    : "Application has to contain data descriptor for data type " 
                      + descriptor.getDataType()
                      + " since this data descriptor has parent data descriptor " 
                      + parentDescriptor.getClass().getName();
          }
          lstDescriptors.add(descriptor);
          
          s_logger.log(Level.FINE, "Registered data descriptor {0} - {1} - {2}"
                       + " - {3} as child of {4}", 
                       new Object[]{descriptor.getDataType(), 
                                    descriptor.getViewName(), 
                                    descriptor.getDisplayableViewName(), 
                                    descriptor.getClass().getName(), 
                                    parentDescriptor.getClass().getName()});
       }
       else
       {
          int     iDesiredDataType = descriptor.getDesiredDataType();
          Integer iRealDataType = null;
          
          if (m_mpDesiredDataTypeMap != null)
          {
             iRealDataType = m_mpDesiredDataTypeMap.get(
                                new Integer(iDesiredDataType));
          }
          if (iRealDataType == null)
          {
             // There is no mapping set so the desired data type should be the 
             // real data type
             descriptor.setDataType(iDesiredDataType);
          }
          else
          {
             // The application has setup mapping to change the desired data type
             // to a different one so use the mapped one
             descriptor.setDataType(iRealDataType.intValue());
          }
          
          // Add this descriptor to a map of data types to data descriptors so we 
          // can resolve them later if needed
          lstDescriptors = m_mpDataTypeToDescriptors.get(
                              descriptor.getDataTypeAsObject());
          // Since this is not a dependent data descriptor, there shouldn't be any
          if (lstDescriptors != null)
          {
             throw new OSSInternalErrorException(
                      "Application already contains data descriptor for data type " 
                      + descriptor.getDataType() + ": " + lstDescriptors.toString()
                      + ". The data descriptor " + descriptor.getClass().getName()
                      + " cannot be added.");
          }
          else
          {
             // Most of these will have only 1 descriptor per data type
             lstDescriptors = new ArrayList<>(1);
             lstDescriptors.add(descriptor);
             m_mpDataTypeToDescriptors.put(descriptor.getDataTypeAsObject(), 
                                           lstDescriptors);
          }
          
          s_logger.log(Level.FINE, "Registered data descriptor {0} - {1} - {2}"
                       + " - {3}", 
                       new Object[]{descriptor.getDataType(), 
                                    descriptor.getViewName(), 
                                    descriptor.getDisplayableViewName(), 
                                    descriptor.getClass().getName()});
       }
       m_mpDataTypeViews.put(descriptor.getViewName(), 
                             descriptor.getDataTypeAsObject());
       m_mpDescriptorCache.put(descriptor.getClass().getName(), descriptor);
    }
 }
