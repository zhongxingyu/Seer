 /*
  * 
  * Copyright © 2012 jbundle.org. All rights reserved.
  */
 package org.jbundle.thin.base.db.model;
 
 import java.util.Map;
 
 /**
  * @author don
  *
  */
public interface ThinPhysicalDatabaseParent {
 
     /**
      * The initial cache time param.
      */
     public static final String TIME = "time";
     /**
      * The initial table application object param.
      */
     public static final String APP = "app";
     /**
      * The initial table Database classname param.
      */
     public static final String DBCLASS = "dbclass";
 
     /**
      * Add this physical table to my table list.
      * @param physicalTable The raw data table to add.
      * @param bCreateIfNew Create a new database if this db is not here (requires the DBCLASS property).
      * @return The physical database.
      */
     public ThinPhysicalDatabase getPDatabase(String strDBName, char charPDatabaseType, boolean bCreateIfNew);
     /**
      * Constructor
      * @param mapParams time The default cache review interval.
      * @param mapParams prefix The prefix on the physical file name.
      * @param mapParams suffix The suffix on the physical file name.
      * @param mapParams app The application object (The application object is used by databases that need information about the location of physical data).
      * @param mapParams dbclass The class name of the Database to build if not found (see getPDatabase()).
      */
     public void init(Map<String,Object> mapParams);
     /**
      * Set the value of this property (passed in on initialization).
      * @param key The parameter key value.
      * @param objValue The key's value.
      */
     public void setProperty(String strKey, Object objValue);
 }
