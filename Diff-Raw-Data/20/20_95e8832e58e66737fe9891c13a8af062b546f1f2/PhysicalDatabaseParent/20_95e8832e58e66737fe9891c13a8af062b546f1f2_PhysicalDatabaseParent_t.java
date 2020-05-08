 /*
  * Copyright © 2011 jbundle.org. All rights reserved.
  */
 package org.jbundle.thin.base.db.mem.base;
 
 /**
  * @(#)PDatabase.java 1.16 95/12/14 Don Corley
  *
  * Copyright (c) 2009 tourapp.com. All Rights Reserved.
  *      don@tourgeek.com
  *
  */
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Hashtable;
 import java.util.Map;
 import java.util.Set;
 
 import org.jbundle.model.Freeable;
 import org.jbundle.thin.base.db.model.ThinPhysicalDatabaseParent;
 import org.jbundle.thin.base.db.model.ThinPhysicalTableOwner;
 
 
 /**
  * Implement the Raw data database owner.
  *
  * @version 1.0.0
  * @author    Don Corley
  */
 public class PhysicalDatabaseParent extends Object
    implements /*ActionListener,*/ Freeable, ThinPhysicalDatabaseParent, ThinPhysicalTableOwner
 {
     /**
      * List of all the raw data databases.
      */
     protected Hashtable<Object,PDatabase> m_htDBList = null;
     /**
      * Turn this on to cache all the tables for this database.
      */
//    protected javax.swing.Timer m_timerCache = null;
     /**
      * The cache time in minutes.
      */
     protected int m_iCacheMinutes = 0;
     /**
      * 10 Minutes of non-use to release cache.
      */
     public static final int DEFAULT_CACHED_MINUTES = 10;
     /**
      * The initialization params (passed in the constructor).
      */
     protected Map<String, Object> m_mapParams = null;
     /**
      * The initial table prefix param.
      */
     public static final String PREFIX = "prefix";
     /**
      * The initial table suffix param.
      */
     public static final String SUFFIX = "suffix";
 
     /**
      * Constructor.
      */
     public PhysicalDatabaseParent()
     {
         super();
     }
     /**
      * Constructor.
      * @param mapParams time The default cache review interval.
      * @param mapParams prefix The prefix on the physical file name.
      * @param mapParams suffix The suffix on the physical file name.
      * @param mapParams app The application object (The application object is used by databases that need information about the location of physical data).
      * @param mapParams dbclass The class name of the Database to build if not found (see getPDatabase()).
      */
     public PhysicalDatabaseParent(Map<String,Object> mapParams)
     {
         this();
         this.init(mapParams);
     }
     /**
      * Constructor
      * @param mapParams time The default cache review interval.
      * @param mapParams prefix The prefix on the physical file name.
      * @param mapParams suffix The suffix on the physical file name.
      * @param mapParams app The application object (The application object is used by databases that need information about the location of physical data).
      * @param mapParams dbclass The class name of the Database to build if not found (see getPDatabase()).
      */
     public void init(Map<String,Object> mapParams)
     {
         m_htDBList = new Hashtable<Object,PDatabase>();
         m_mapParams = mapParams;
         Object strMinutes = this.getProperty(TIME);
         int iMinutes = -1;  // Default time
         try   {
             if (strMinutes instanceof String)
                 iMinutes = Integer.parseInt((String)strMinutes);
         } catch (NumberFormatException ex)  {
             iMinutes = -1;
         }
         this.setCacheMinutes(iMinutes);
     }
     /**
      * Free the objects.
      * There is no need to call this, as the raw tables will automatically be removed
      * when their physical tables are freed.
      */
     public void free()
     {
         this.setCacheMinutes(0);    // Turn off the cache
         Object[] keys = m_htDBList.keySet().toArray();
         for (int i = 0; i < keys.length; i++)
         { 
             Object key = keys[i];
             PDatabase pDatabase = (PDatabase)m_htDBList.get(key);
             pDatabase.free();
         } 
         m_htDBList.clear();
         m_htDBList = null;
     } 
     /**
      * Add this physical table to my table list.
      * @param physicalTable The raw data table to add.
      */
     public void addPDatabase(PDatabase pDatabase)
     {
         Object strKey = pDatabase.getDatabaseKey();
         m_htDBList.put(strKey, pDatabase);
     }
     /**
      * Remove this raw data table from this record.
      * <p/>Do not call this method, it is called automatically from rawtable.free().
      * @param pDatabase The raw data database to remove from the database owner.
      * @return True if successfully removed.
      */
     public boolean removePDatabase(PDatabase pDatabase)
     {
         Object strKey = pDatabase.getDatabaseKey();
         Object obj = m_htDBList.get(strKey);
         if (obj == null)
             return false;
         return (m_htDBList.remove(strKey) != null);
     }
     /**
      * Add this physical table to my table list.
      * @param physicalTable The raw data table to add.
      * @param bCreateIfNew Create a new database if this db is not here (requires the DBCLASS property).
      * @return The physical database.
      */
     public PDatabase getPDatabase(String strDBName, char charPDatabaseType, boolean bCreateIfNew)
     {
         PDatabase pDB = (PDatabase)m_htDBList.get(charPDatabaseType + ":" + strDBName);
         if (pDB == null)
             if (bCreateIfNew)
         {
             String strClassName = (String)this.getProperty(DBCLASS);
             if (strClassName != null)
             {
                 try
                 {
                     Class<?> c = Class.forName(strClassName);
                     if (c != null)
                     {
                         pDB = (PDatabase)c.newInstance();
                         if (pDB != null)
                         {
                             pDB.init(this, strDBName, charPDatabaseType);
                             String strPrefix = (String)this.getProperty(PREFIX);
                             String strSuffix = (String)this.getProperty(SUFFIX);
                             if ((strPrefix != null) || (strSuffix != null))
                                 pDB.setFilePath(strPrefix, strSuffix);
                         }
                     }
                 }
                 catch (Exception e) {
                     e.printStackTrace();
                     pDB = null;
                 }
             }
         }
         return pDB;
     }
     /**
      * This will set this database to start caching records until they haven't been used for
      * iMinutes minutes.
      * @param iMinutes free tables once they haven't been accessed, set to 0 to turn off cache and free cached tables.
      */
     public void setCacheMinutes(int iMinutes)
     {
         if (iMinutes == -1)
             iMinutes = DEFAULT_CACHED_MINUTES;      // Default cache time.
         m_iCacheMinutes = iMinutes;
         if (iMinutes == 0)
/*        {
             if (m_timerCache != null)
             {
                 m_timerCache.stop();
                 m_timerCache = null;
                 this.stopCache();
             }
         }
         else
         {
             int iMilliseconds = iMinutes * 60 * 1000;
             if (m_timerCache != null)
                 m_timerCache.setDelay(iMilliseconds);
             else
             { // Set up the timer for the first time.
                 this.startCache();
                 m_timerCache = new javax.swing.Timer(iMilliseconds, this);
                 m_timerCache.setRepeats(true);
                 m_timerCache.start();
             }
         }
*/    }
     /**
      * Get the cache time in minutes.
      * @return The cache time in minutes.
      */
     public int getCacheMinutes()
     {
         return m_iCacheMinutes;
     }
     /**
      * Called from the timer.
      */
/*    public void actionPerformed(ActionEvent e)
     {
         if (e.getSource() == m_timerCache)
         { // Always
             this.checkCache();
         }
     }
  */  /**
      * Get the value of this property (passed in on initialization).
      * @param key The parameter key value.
      * @return The key's value.
      */
     public Object getProperty(String strKey)
     {
         if (m_mapParams == null)
             return null;
         return m_mapParams.get(strKey);
     }
     /**
      * Set the value of this property (passed in on initialization).
      * @param key The parameter key value.
      * @param objValue The key's value.
      */
     public void setProperty(String strKey, Object objValue)
     {
         if (m_mapParams == null)
             m_mapParams = new HashMap<String,Object>();
         m_mapParams.put(strKey, objValue);
     }
     /**
      * Add this table to the cache.
      * @param pTable
      */
     public void addTableToCache(PTable pTable)
     {
         if (m_iCacheMinutes != 0)
         {
             pTable.addPTableOwner(this);
             m_setTableCacheList.add(pTable);
         }
     }
     /**
      * This will set this database to start caching records by bumping the use count of all the open tables.
      */
     private synchronized void startCache()
     {
         for (PDatabase pDatabase : m_htDBList.values())
         {
             for (PTable pTable : pDatabase.getTableList().values())
             {
                 this.addTableToCache(pTable);
             }
         }
     }
     /**
      * This will set this database to stop caching records by decrementing (and freeing if unused) the use count of all the open tables.
      */
     private synchronized void stopCache()
     {
         Object[] pTables = m_setTableCacheList.toArray();
         for (Object objTable : pTables)
         {
             PTable pTable = (PTable)objTable;
             pTable.removePTableOwner(this, true);
             m_setTableCacheList.remove(pTable);
         }
         m_setTableCacheList.removeAll(m_setTableCacheList);
     }
     /**
      * Check all the cached tables and flush the old ones.
      */
     private synchronized void checkCache()
     {
         Object[] pTables = m_setTableCacheList.toArray();
         for (Object objTable : pTables)
         {
             PTable pTable = (PTable)objTable;
             if (pTable.addPTableOwner(null) == 1)
             { // Not currently being used is a candidate for flushing from the cache.
                 long lTimeLastUsed = pTable.getLastUsed();
                 long lTimeCurrent = System.currentTimeMillis();
                 if ((lTimeCurrent - lTimeLastUsed) > (this.getCacheMinutes() * 60 * 1000))
                 {
                     pTable.removePTableOwner(this, true);
                     m_setTableCacheList.remove(pTable);
                 }
             }
         }
     }
     /**
      * Set the ptable that I am an owner of.
      * @param pTable
      */
     public void setPTable(PTable pTable)
     {
         // ????
     }
     protected Set<PTable> m_setTableCacheList = new HashSet<PTable>();
 }
