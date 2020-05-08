 /**
  * JAFER Toolkit Project.
  * Copyright (C) 2002, JAFER Toolkit Project, Oxford University.
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  *
  *
  */
 
 /**
  *  Title: JAFER Toolkit
  *  Description:
  *  Copyright: Copyright (c) 2001
  *  Company: Oxford University
  *
  *@author     Antony Corfield; Matthew Dovey; Colin Tatham
  *@version    1.0
  */
 
 package org.jafer.databeans;
 
 import java.util.Hashtable;
 
 import org.jafer.interfaces.Databean;
 import org.jafer.interfaces.DatabeanFactory;
 import org.jafer.record.CacheFactory;
 
 /**
  * This class is responsible for creating DatabaseBeanManagers
  */
 public class DatabeanManagerFactory extends DatabeanFactory
 {
 
     /**
      * Stores a reference to the SERIAL MODE definition
      */
     public static final String MODE_SERIAL = "serial";
 
     /**
      * Stores a reference to the PARALLEL MODE definition
      */
     public static final String MODE_PARALLEL = "parallel";
 
     /**
      * Stores a reference to the default record schema to set on the databean
      * manager
      */
    public String recordSchema;
 
     /**
      * Stores a reference to factories that can create databeans for specified
      * databases. A map entry consists of key = database name and value =
      * factory that creates a databean supporting Search and Present for the
      * specified database
      */
     private Hashtable databeanFactories = new Hashtable();
 
     /**
      * Stores a reference to cache factory that is passed to the databeanManager
      */
     private CacheFactory cacheFactory = null;
 
     /**
      * Stores a reference to search mode. <br>
      * <br>
      * <ul>
      * <li>serial - The first active bean to return a result set will be used,
      * ignoring all other activebean results</li>
      * <li>parallel - All ActiveBeans results will be combined to provide a
      * super result set (DEFAULT)</li>
      * </ul>
      */
     private String mode = MODE_PARALLEL;
 
     /**
      * Stores a reference to the complete set of configured databases for any
      * new DatabeanManagers
      */
     private String[] allDatabases;
 
     /*
      * (non-Javadoc)
      * 
      * @see org.jafer.interfaces.DatabeanFactory#getDatabean()
      */
     public Databean getDatabean()
     {
         // create the DatabeanManager
         DatabeanManager bean = new DatabeanManager();
         // set the databean and cache factories to be used
         bean.setDatabeanFactories(databeanFactories);
         bean.setCacheFactory(cacheFactory);
         // set the mode to serial or parrallel
         bean.setMode(mode);
         // set the name of the databean manager to the one defined in the
         // factory
         bean.setName(this.getName());
         // set the record schema
         bean.setRecordSchema(recordSchema);
         // set all the DatabeanManager
         bean.setAllDatabases(allDatabases);
         // Now set the databases. By supplying the name of the DatabeanManager
         // will initialise it with the value of all databases
         bean.setDatabases(this.getName());
         return bean;
     }
 
     /**
      * set the database factories that this databeanmaagerfactory uses
      * 
      * @param databeanFactories An array of DatabeanFactories
      */
     public void setDatabeanFactories(org.jafer.interfaces.DatabeanFactory[] databeanFactories)
     {
         // clear the current set
         this.databeanFactories.clear();
 
         // create a new array to hold all the supported databases
         allDatabases = new String[databeanFactories.length];
         java.util.Random rnd = new java.util.Random();
 
         // loop round the supplied databean factories
         for (int index = 0; index < databeanFactories.length; index++)
         {
             // get the factories name
             String name = databeanFactories[index].getName();
 
             // if does not have a name then genererate one
             if (name == null)
             {
                 name = "DB" + Integer.toHexString(rnd.nextInt());
             }
 
             // add this factory to the set of all factories
             this.databeanFactories.put(name, databeanFactories[index]);
             // add the name to the list of all databases
             allDatabases[index] = name;
         }
     }
 
     /**
      * Return the database factories that this databeanmaagerfactory uses
      * 
      * @return An array of databean factories
      */
     public org.jafer.interfaces.DatabeanFactory[] getDatabeanFactories()
     {
         return ((DatabeanFactory[]) databeanFactories.values().toArray(new DatabeanFactory[databeanFactories.size()]));
     }
 
     /**
      * Set the mode that the factory should use when creating DatabeanManagers
      * 
      * @param mode The mode to use SERIAL or PARALLEL
      */
     public void setMode(String mode)
     {
         this.mode = mode;
     }
 
     /**
      * Get the mode that this factory uses when creating databean managers
      * 
      * @return he mode being used SERIAL or PARALLEL
      */
     public String getMode()
     {
         return mode;
     }
 
     /**
      * Sets the cache factory to be used by the DatabeanManagerFactory
      * 
      * @param cacheFactory the cache factory to use
      */
     public void setCacheFactory(CacheFactory cacheFactory)
     {
         this.cacheFactory = cacheFactory;
     }
 
     /**
      * Returns the cache factory used by this databeanManagerFactory
      * 
      * @return The cache factory used
      */
     public CacheFactory getCacheFactory()
     {
         return cacheFactory;
     }
 
     /**
      * Sets the default record schema to be set on any databeanmanagers created
      * 
      * @param recordSchema the record schema to set
      */
     public void setRecordSchema(String recordSchema)
     {
         this.recordSchema = recordSchema;
     }
 
     /**
      * gets the default record schema to be set on any databeanmanagers created
      * 
      * @return The schema that would be set
      */
     public String getRecordSchema()
     {
         return recordSchema;
     }
 }
