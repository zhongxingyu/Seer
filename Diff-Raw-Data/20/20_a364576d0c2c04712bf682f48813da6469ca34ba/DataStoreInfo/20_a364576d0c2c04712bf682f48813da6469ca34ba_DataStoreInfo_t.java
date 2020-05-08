 /* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.vfny.geoserver.global;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.NoSuchElementException;
 import java.util.logging.Logger;
 
 import org.geotools.data.DataStore;
 import org.geotools.data.DataStoreFinder;
 import org.geotools.data.DataStoreMetaData;
 import org.vfny.geoserver.global.dto.DataStoreInfoDTO;
 
 
 /**
  * This is the configuration iformation for one DataStore. This class can also
  * generate real datastores.
  *
  * @author Gabriel Roldn
  * @author dzwiers
 * @version $Id: DataStoreInfo.java,v 1.5 2004/01/20 06:30:52 jive Exp $
  */
 public class DataStoreInfo extends GlobalLayerSupertype implements DataStoreMetaData {
     /** for logging */
     private static final Logger LOGGER = Logger.getLogger(
             "org.vfny.geoserver.config");
 
     /** DataStoreInfo we are representing */
     private DataStore dataStore = null;
 
     /** ref to the parent class's collection */
     private Data data;
 
     /** The dataStore information for this object */
     private DataStoreInfoDTO dsc;
 
     /** Storage for metadata */
     private Map meta;
     /**
      * DataStoreInfo constructor.
      * 
      * <p>
      * Stores the specified data for later use.
      * </p>
      *
      * @param config DataStoreInfoDTO the current configuration to use.
      * @param data Data a ref to use later to look up related informtion
      */
     public DataStoreInfo(DataStoreInfoDTO config, Data data) {
         dsc = config;
         this.data = data;
         meta = new HashMap();
     }
 
     /**
      * toDTO purpose.
      * 
      * <p>
      * This method is package visible only, and returns a reference to the
      * GeoServerDTO. This method is unsafe, and should only be used with
      * extreme caution.
      * </p>
      *
      * @return DataStoreInfoDTO the generated object
      */
     Object toDTO() {
         return dsc;
     }
 
     /**
      * getId purpose.
      * 
      * <p>
      * Returns the dataStore's id.
      * </p>
      *
      * @return String the id.
      */
     public String getId() {
         return dsc.getId();
     }
 
     /**
      * By now just uses DataStoreFinder to find a new instance of a
      * DataStoreInfo capable of process <code>connectionParams</code>. In the
      * future we can see if it is better to cache or pool DataStores for
      * performance, but definitely we shouldn't maintain a single
      * DataStoreInfo as instance variable for synchronizing reassons
      * 
      * <p>
      * JG: Umm we actually require a single DataStoreInfo for for locking &
      * transaction support to work. DataStoreInfo is expected to be thread
      * aware (that is why it has Transaction Support).
      * </p>
      *
      * @return DataStore
      *
      * @throws IOException if a datastore is found but can not be created for
      *         the passed parameters
      * @throws IllegalStateException if this DataStoreInfo is disabled by
      *         configuration
      * @throws NoSuchElementException if no DataStoreInfo is found
      * @throws DataSourceException
      */
     public synchronized DataStore getDataStore()
         throws IllegalStateException, NoSuchElementException {
         if (!isEnabled()) {
             throw new IllegalStateException(
                 "this datastore is not enabled, check your configuration files");
         }
 
         if (dataStore == null) {
             try {
                 dataStore = DataStoreFinder.getDataStore(dsc
                         .getConnectionParams());
                 LOGGER.fine("connection established by " + toString());
             } catch (Throwable ex) {
                 throw new IllegalStateException("can't create the datastore "
                     + getId() + ": " + ex.getClass().getName() + ": "
                     + ex.getMessage()+"\n"+ ex.toString());
             }
 
             if (dataStore == null) {
                 LOGGER.fine("failed to establish connection with " + toString());
                 throw new NoSuchElementException(
                     "No datastore found capable of managing " + toString());
             }
         }
         return dataStore;
     }
 
     /**
      * getTitle purpose.
      * 
      * <p>
      * Returns the dataStore's title.
      * </p>
      *
      * @return String the title.
      */
     public String getTitle() {
         return dsc.getTitle();
     }
 
     /**
      * getAbstract purpose.
      * 
      * <p>
      * Returns the dataStore's abstract.
      * </p>
      *
      * @return String the abstract.
      */
     public String getAbstract() {
         return dsc.getAbstract();
     }
 
     /**
      * isEnabled purpose.
      * 
      * <p>
      * Returns true when the data store is enabled.
      * </p>
      *
      * @return true when the data store is enabled.
      */
     public boolean isEnabled() {
         return dsc.isEnabled();
     }
 
     /**
      * getNameSpace purpose.
      * 
      * <p>
      * Returns the namespace for this datastore.
      * </p>
      *
      * @return NameSpaceInfo the namespace for this datastore.
      */
     public NameSpaceInfo getNameSpace() {
         return (NameSpaceInfo) data.getNameSpace( getNamesSpacePrefix() );
     }
     /** Access namespace id */
     public String getNamesSpacePrefix(){
         return dsc.getNameSpaceId();
     }
     /**
      * Implement toString.
      *
      * @return String
      *
      * @see java.lang.Object#toString()
      */
     public String toString() {
         return new StringBuffer("DataStoreConfig[namespace=").append(getNameSpace()
                                                                          .getPrefix())
                                                              .append(", enabled=")
                                                              .append(isEnabled())
                                                              .append(", abstract=")
                                                              .append(getAbstract())
                                                              .append(", connection parameters=")
                                                              .append(dsc
             .getConnectionParams()).append("]").toString();
     }
 
     /**
      * Implement containsMetaData.
      * 
      * @see org.geotools.data.MetaData#containsMetaData(java.lang.String)
      * 
      * @param key
      * @return
      */
     public boolean containsMetaData(String key) {
         return meta.containsKey( key );
     }
 
     /**
      * Implement putMetaData.
      * 
      * @see org.geotools.data.MetaData#putMetaData(java.lang.String, java.lang.Object)
      * 
      * @param key
      * @param value
      */
     public void putMetaData(String key, Object value) {
         meta.put( key, value );
     }
 
     /**
      * Implement getMetaData.
      * 
      * @see org.geotools.data.MetaData#getMetaData(java.lang.String)
      * 
      * @param key
      * @return
      */
     public Object getMetaData(String key) {
         return meta.get( key );
     }    
 }
