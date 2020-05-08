 /*
  * Copyright (C) 2005 - 2013 OpenSubsystems.com/net/org and its owners. All rights reserved.
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
 
 package org.opensubsystems.core.logic;
 
 import java.rmi.RemoteException;
 import java.util.HashMap;
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
  * Class responsible for instantiation of controllers. This class determines
  * what controller should be used based on currently used component model, 
  * creates the controller instance if it wasn't created yet and caches created 
  * instances. This of course assumes that the controllers are implemented to
  * be stateless and reentrant.
  *
  * @author bastafidli
  */
 //TODO: JDK 1.5: All Manager classes could be refactored using template classes 
 public class ControllerManager extends OSSObject
 {
    // Constants ////////////////////////////////////////////////////////////////
    
    /**
     * Lock used in synchronized sections.
     */
    private static final String IMPL_LOCK = "IMPL_LOCK";
 
    // Attributes ///////////////////////////////////////////////////////////////
    
    /**
     * Class factory used to instantiate controller.
     */
    protected ClassFactory<? extends StatelessController> m_controllerClassFactory;
    
    /**
     * Cache where already instantiated controllers will be cached. We can 
     * cache them since the controllers should be reentrant.
     */
    private final Map<String, StatelessController> m_mpControllerCache; 
    
    // Cached values ////////////////////////////////////////////////////////////
 
    /**
     * Logger for this class
     */
    private static Logger s_logger = Log.getInstance(ControllerManager.class);
    
    /**
     * Reference to the instance actually in use.
     */
    private static ControllerManager s_defaultInstance;
 
    // Constructors /////////////////////////////////////////////////////////////
 
    /**
     * Default constructor.
     */
    public ControllerManager(
    )
    {
       m_controllerClassFactory = new ControllerClassFactory();
       m_mpControllerCache = new HashMap<>();
    }
    
    // Logic ////////////////////////////////////////////////////////////////////
    
    /**
     * Create controller for specified class.
     * 
     * @param clsController - the controller class for which we want 
     *                        applicable controller. This is usually component
     *                        model independent class and we will try to create 
     *                        dependent class.  
     * @return StatelessController - controller to use for given class
     * @throws OSSException - an error has occurred 
     */
    public static StatelessController getInstance(
       Class<StatelessController> clsController
    ) throws OSSException
    {
       return getManagerInstance().getControllerInstance(clsController);
    }
    
    /**
     * Get the default instance. This method is here to make the controller manager
     * configurable. Once can specify in configuration file derived class to used
     * instead of this one [ControllerManager.class]=new class to use.
     *
     * @return ControllerManager
     * @throws OSSException - an error has occurred
     */
    public static ControllerManager getManagerInstance(
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
                Class<? extends ControllerManager> defaultManager;
                
                defaultManager = ControllerManager.class;
    
                ClassFactory<ControllerManager> cf;
                
                cf = new ClassFactory<>(ControllerManager.class);
 
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
       ControllerManager defaultInstance
    )
    {
       if (GlobalConstants.ERROR_CHECKING)
       {
          assert defaultInstance != null : "Default instance cannot be null";
       }   
       
       synchronized (IMPL_LOCK)
       {
          s_defaultInstance = defaultInstance;
          s_logger.log(Level.FINE, "Default controller manager is {0}", 
                       s_defaultInstance.getClass().getName());
       }   
    }
 
    /**
     * Method to create actual controllers based on specified class. This method 
     * can be overridden and new manager can be setup either through 
     * setManagerInstance or through configuration file if different strategy is 
     * desired.
     * 
     * @param clsController - the controller interface for which we want 
     *                        applicable controller. This is usually component
     *                        model independent class and we will try to create 
     *                        dependent class.  
     * @return StatelessController - controller to use for given interface
     * @throws OSSException - an error has occurred
     */
    public StatelessController getControllerInstance(
       Class<? extends StatelessController> clsController
    ) throws OSSException
    {
       StatelessController control;
       
      control = m_mpControllerCache.get(clsController.getName());
       if (control == null)
       {
          synchronized (m_mpControllerCache)
          {
             // TODO: Improve: This is suppose to be
             // control = m_controllerClassFactory.createInstance(clsController);
             // but I am getting compiler error.
             control = m_controllerClassFactory.createInstance(clsController.getName());
             // First cache the created instance and only then call the 
             // constructor() in case there is some circular reference
             // which would resolve to the controller of the same type
             m_mpControllerCache.put(clsController.getName(), control);
             // Initialize the object
             try
             {
                control.constructor();
             }
             catch (RemoteException rExc)
             {
                // We cannot propagate this exception otherwise XDoclet would generate 
                // the local interface incorrectly since it would include the declared
                // RemoteException in it (to propagate we would have to declare it)
                throw new OSSInternalErrorException("Remote error occurred", rExc);
             }
          }
       }
       
       return control; 
    }
 }
