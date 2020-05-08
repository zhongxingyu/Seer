 /* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.vfny.geoserver.global;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.NoSuchElementException;
 
 import org.geotools.catalog.Catalog;
 import org.geotools.catalog.Resolve;
 import org.geotools.catalog.ResolveChangeEvent;
 import org.geotools.catalog.ResolveChangeListener;
 import org.geotools.catalog.Service;
 import org.geotools.catalog.ServiceInfo;
 import org.geotools.catalog.defaults.DefaultServiceInfo;
 import org.geotools.data.DataStore;
 import org.geotools.data.DataStoreFinder;
 import org.geotools.util.ProgressListener;
 import org.vfny.geoserver.global.dto.DataStoreInfoDTO;
 
 
 /**
  * This is the configuration iformation for one DataStore. This class can also
  * generate real datastores.
  * <p>
  * This class implements {@link org.geotools.catalog.Service} interface as a 
  * link to a catalog.
  * </p>
  * @author Gabriel Rold?n
  * @author dzwiers
  * @author Justin Deoliveira
  * @version $Id: DataStoreInfo.java,v 1.14 2004/06/26 19:51:24 jive Exp $
  */
 public class DataStoreInfo extends GlobalLayerSupertype implements Service {
 
     /** DataStoreInfo we are representing */
     private DataStore dataStore = null;
 
     /** ref to the parent class's collection */
     private Data data;
     private String id;
     private String nameSpaceId;
     private boolean enabled;
     private String title;
     private String _abstract;
     private Map connectionParams;
 
     /** Storage for metadata */
     private Map meta;
     
     /**
      * Catalog
      */
     private Catalog catalog;
     
     /**
      * Directory associated with this DataStore.
      * 
      * <p>
      * This directory may be used for File based relative paths.
      * </p>
      */
     File baseDir;
 
     /**
      * URL associated with this DataStore.
      * 
      * <p>
      * This directory may be used for URL based relative paths.
      * </p>
      */
     URL baseURL;
 
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
         this.data = data;
         meta = new HashMap();
 
         connectionParams = config.getConnectionParams();
         enabled = config.isEnabled();
         id = config.getId();
         nameSpaceId = config.getNameSpaceId();
         title = config.getTitle();
         _abstract = config.getAbstract();
         
         catalog = data.getCatalog();
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
         DataStoreInfoDTO dto = new DataStoreInfoDTO();
         dto.setAbstract(_abstract);
         dto.setConnectionParams(connectionParams);
         dto.setEnabled(enabled);
         dto.setId(id);
         dto.setNameSpaceId(nameSpaceId);
         dto.setTitle(title);
 
         return dto;
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
         return id;
     }
 
     protected Map getParams() {
         Map params = new HashMap(connectionParams);
 	params.put("namespace", getNameSpace().getURI());
 	return getParams(params, data.getBaseDir().toString());
     }
 
     /**
      * Get Connect params.
      * 
      * <p>
      * This is used to smooth any relative path kind of issues for any file
      * URLS. This code should be expanded to deal with any other context
      * sensitve isses dataStores tend to have.
      * </p>
      *
      * @return DOCUMENT ME!
      *
      * @task REVISIT: cache these?
      */
 
     public static Map getParams(Map m, String baseDir) {
         Map params = Collections.synchronizedMap(new HashMap(m));
 
         for (Iterator i = params.entrySet().iterator(); i.hasNext();) {
             Map.Entry entry = (Map.Entry) i.next();
             String key = (String) entry.getKey();
             Object value = entry.getValue();
 
             try {
             	//TODO: this code is a pretty big hack, using the name to 
             	// determine if the key is a url, could be named something else
             	// and still be a url
                 if (key != null && key.matches(".* *url") && value instanceof String) {
                     String path = (String) value;
                     LOGGER.finer("in string url");
                     if (path.startsWith("file:")) {
                         path = path.substring(5); // remove 'file:' prefix
 
                        File file = new File(path);
                         if(!file.exists())
                            file = new File(baseDir, path);
                         entry.setValue(file.toURL().toExternalForm());
                     }
                 } else if (value instanceof URL
                         && ((URL) value).getProtocol().equals("file")) {
 		    LOGGER.finer("in URL url");
                     URL url = (URL) value;
                     String path = url.getPath();
 		    LOGGER.finer("path is " + path);
            // try to understand wheter this is a path relative to the data directory
            // of if it's an absolute path
            File file = new File(path);
            if(!file.exists())
                file = new File(baseDir, path);
 			entry.setValue(file.toURL());
                 } /*else if ("dbtype".equals(key) && value instanceof String) {
                     String val = (String) value;
 
                     if ((val != null) && val.equals("postgis")) {
                         if (!params.containsKey("charset")) {
                             params.put("charset",
                                 data.getGeoServer().getCharSet().toString());
                         }
                     }
 		    } */
             } catch (MalformedURLException ignore) {
                 // ignore attempt to fix relative paths
             }
         }
 
         return params;
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
      * @throws IllegalStateException if this DataStoreInfo is disabled by
      *         configuration
      * @throws NoSuchElementException if no DataStoreInfo is found
      */
     public synchronized DataStore getDataStore()
         throws IllegalStateException, NoSuchElementException {
         if (!isEnabled()) {
             throw new IllegalStateException(
                 "this datastore is not enabled, check your configuration");
         }
 
         Map m = getParams();
 
         if (dataStore == null) {
             try {
                 dataStore = DataStoreFinder.getDataStore(m);
                 LOGGER.fine("connection established by " + toString());
             } catch (Throwable ex) {
                 throw new IllegalStateException("can't create the datastore "
                     + getId() + ": " + ex.getClass().getName() + ": "
                     + ex.getMessage() + "\n" + ex.toString());
             }
 
             if (dataStore == null) {
             	 // If datastore is not present, then disable it
                 // (although no change in config).
                 enabled=false;
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
         return title;
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
         return _abstract;
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
         return enabled;
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
         return (NameSpaceInfo) data.getNameSpace(getNamesSpacePrefix());
     }
 
     /**
      * Access namespace id
      *
      * @return DOCUMENT ME!
      */
     public String getNamesSpacePrefix() {
         return nameSpaceId;
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
                                                              .append(getParams())
                                                              .append("]")
                                                              .toString();
     }
 
     /**
      * Implement containsMetaData.
      *
      * @param key
      *
      * @return
      *
      * @see org.geotools.data.MetaData#containsMetaData(java.lang.String)
      */
     public boolean containsMetaData(String key) {
         return meta.containsKey(key);
     }
 
     /**
      * Implement putMetaData.
      *
      * @param key
      * @param value
      *
      * @see org.geotools.data.MetaData#putMetaData(java.lang.String,
      *      java.lang.Object)
      */
     public void putMetaData(String key, Object value) {
         meta.put(key, value);
     }
 
     /**
      * Implement getMetaData.
      *
      * @param key
      *
      * @return
      *
      * @see org.geotools.data.MetaData#getMetaData(java.lang.String)
      */
     public Object getMetaData(String key) {
         return meta.get(key);
     }
 
     //catalog methods
     
     List/*<FeatureTypeInfo>*/ members = new ArrayList();
     ServiceInfo info;
     
     public void addMember( FeatureTypeInfo ftInfo ) {
     		members.add( ftInfo );
     }
     
 	public List members(ProgressListener monitor) throws IOException {
 		return members;
 	}
 
 	public boolean canResolve(Class adaptee) {
 		return DataStore.class.isAssignableFrom( adaptee ) || 
 			List.class.isAssignableFrom( adaptee ) || 
 			ServiceInfo.class.isAssignableFrom( adaptee );
 	}
 	
 	public Object resolve(Class adaptee, ProgressListener monitor) throws IOException {
 		if ( DataStore.class.isAssignableFrom( adaptee ) ) {
 			return getDataStore();
 		}
 		if ( List.class.isAssignableFrom( adaptee ) ) {
 			return members( monitor );
 		}
 		if ( ServiceInfo.class.isAssignableFrom( adaptee ) ) {
 			return getInfo( monitor );
 		}
 		
 		return null;
 	}
 
 	public Map getConnectionParams() {
 		return getParams();
 	}
 
 	public ServiceInfo getInfo(ProgressListener monitor) throws IOException {
 		if ( info == null ) {
 			synchronized ( this ) {
 				if ( info == null ) {
 					info = new DefaultServiceInfo(
 						getTitle(), null, getAbstract(), null, null, null, 
 						null, null
 					);
 				}
 			}
 		}
 		
 		return info;
 	}
 
 	public Resolve parent(ProgressListener monitor) throws IOException {
 		return catalog;
 	}
 
 	public Status getStatus() {
 		if ( isEnabled() ) {
 			return Status.CONNECTED;
 		}
 		
 		return Status.NOTCONNECTED;
 	}
 
 	public Throwable getMessage() {
 		return null;
 	}
 
 	public URI getIdentifier() {
 		
 		try {
 			URI uri = new URI( getNameSpace().getURI() );
 			String path = uri.getPath();
 			if ( path == null ) {
 				path = getId();
 			}
 			else {
 				if ( !path.endsWith( "/") ) {
 					path += "/";
 				}
 				path += getId();
 			}
 			
 			return new URI(
 				uri.getScheme(), uri.getHost(), path
 			);
 			
 		} 
 		catch (URISyntaxException e) {
 			return null;
 		}
 	}
 
 	public void addListener(ResolveChangeListener listener) throws UnsupportedOperationException {
 		//events not supported
 		throw new UnsupportedOperationException();
 	}
 
 	public void removeListener(ResolveChangeListener listener) {
 		//events not supported
 	}
 
 	public void fire(ResolveChangeEvent event) {
 		//events not supported
 	}
 }
