 /* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.vfny.geoserver.global;
 
 import org.geotools.coverage.grid.io.AbstractGridFormat;
 import org.geotools.factory.Hints;
 import org.opengis.coverage.grid.Format;
 import org.opengis.coverage.grid.GridCoverageReader;
 import org.opengis.parameter.InvalidParameterValueException;
 import org.opengis.parameter.ParameterNotFoundException;
 import org.vfny.geoserver.global.dto.CoverageStoreInfoDTO;
 import org.vfny.geoserver.util.CoverageStoreUtils;
 import java.io.File;
 import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.NoSuchElementException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 
 /**
  * This is the configuration iformation for one coverage Format.
  *
  * @author $Author: Alessio Fabiani (alessio.fabiani@gmail.com) $ (last
  *         modification)
  * @author $Author: Simone Giannecchini (simboss1@gmail.com) $ (last
  *         modification)
  */
 public final class CoverageStoreInfo extends GlobalLayerSupertype {
     /** for logging */
     private static final Logger LOGGER = Logger.getLogger(CoverageStoreInfo.class.toString());
 
     /**
      * CoverageStoreInfo we are representing
      */
     private Format format = null;
 
     /**
      * ref to the parent class's collection
      */
     private Data data;
 
     /**
      *
      */
     private String id;
    private WeakReference reader = null;
    private WeakReference hintReader = null;
 
     /**
      *
      */
     private String nameSpaceId;
 
     /**
      *
      */
     private String type;
 
     /**
      *
      */
     private String url;
     private boolean enabled;
 
     /**
      *
      */
     private String title;
     private String _abstract;
 
     /**
      * Storage for metadata
      */
     private Map meta;
 
     /**
      * CoverageStoreInfo constructor.
      *
      * <p>
      * Stores the specified data for later use.
      * </p>
      *
      * @param config
      *            CoverageStoreInfoDTO the current configuration to use.
      * @param data
      *            Data a ref to use later to look up related informtion
      */
     public CoverageStoreInfo(CoverageStoreInfoDTO config, Data data) {
         this.data = data;
         meta = new HashMap(10);
         enabled = config.isEnabled();
         id = config.getId();
         nameSpaceId = config.getNameSpaceId();
         type = config.getType();
         url = config.getUrl();
         title = config.getTitle();
         _abstract = config.getAbstract();
         format = lookupFormat();
     }
 
     private Format lookupFormat() {
         final int length = CoverageStoreUtils.formats.length;
 
         for (int i = 0; i < length; i++) {
             if (CoverageStoreUtils.formats[i].getName().equals(type)) {
                 return CoverageStoreUtils.formats[i];
             }
         }
 
         return null;
     }
 
     /**
      * toDTO purpose.
      *
      * <p>
      * This method is package visible only, and returns a reference to the
      * GeoServerDTO. This method is unsafe, and should only be used with extreme
      * caution.
      * </p>
      *
      * @return CoverageStoreInfoDTO the generated object
      */
     Object toDTO() {
         CoverageStoreInfoDTO dto = new CoverageStoreInfoDTO();
         dto.setAbstract(_abstract);
         dto.setEnabled(enabled);
         dto.setId(id);
         dto.setNameSpaceId(nameSpaceId);
         dto.setType(type);
         dto.setUrl(url);
         dto.setTitle(title);
 
         return dto;
     }
 
     /**
      * getId purpose.
      *
      * <p>
      * Returns the format's id.
      * </p>
      *
      * @return String the id.
      */
     public String getId() {
         return id;
     }
 
     //	/**
     //	 * Get Connect params.
     //	 * 
     //	 * @return DOCUMENT ME!
     //	 */
     //
     //	public static Map getParams(Map m, String baseDir) {
     //		Map params = Collections.synchronizedMap(new HashMap(m));
     //
     //		for (Iterator i = params.entrySet().iterator(); i.hasNext();) {
     //			Map.Entry entry = (Map.Entry) i.next();
     //			String key = (String) entry.getKey();
     //			Object value = entry.getValue();
     //
     //			try {
     //				if ("url".equals(key) && value instanceof String) {
     //					String path = (String) value;
     //					if (LOGGER.isLoggable(Level.INFO)) {
     //						LOGGER.info("in string url");
     //					}
     //					if (path.startsWith("file:data/")) {
     //						path = path.substring(5); // remove 'file:' prefix
     //
     //						File file = new File(baseDir, path);
     //						entry.setValue(file.toURL().toExternalForm());
     //					}
     //					// Not sure about this
     //				} else if (value instanceof URL
     //						&& ((URL) value).getProtocol().equals("file")) {
     //					if (LOGGER.isLoggable(Level.INFO)) {
     //						LOGGER.info("in URL url");
     //					}
     //					URL url = (URL) value;
     //					String path = url.getPath();
     //					if (LOGGER.isLoggable(Level.INFO)) {
     //						LOGGER.info(new StringBuffer("path is ").append(path)
     //								.toString());
     //					}
     //					if (path.startsWith("data/")) {
     //						File file = new File(baseDir, path);
     //						entry.setValue(file.toURL());
     //					}
     //				}
     //			} catch (MalformedURLException ignore) {
     //				// ignore attempt to fix relative paths
     //			}
     //		}
     //
     //		return params;
     //	}
 
     /**
      * DOCUMENT ME !
      *
      * @return Format
      *
      * @throws IllegalStateException
      *             if this CoverageStoreInfo is disabled by configuration
      * @throws NoSuchElementException
      *             if no CoverageStoreInfo is found
      */
     public Format getFormat() throws IllegalStateException, NoSuchElementException {
         if (!isEnabled()) {
             throw new IllegalStateException("this format is not enabled, check your configuration");
         }
 
         if (format == null) {
             LOGGER.warning("failed to establish connection with " + toString());
             throw new NoSuchElementException("No format found capable of managing " + toString());
         }
 
         return format;
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
      * Implement toString.
      *
      * @return String
      *
      * @see java.lang.Object#toString()
      */
     public String toString() {
         return new StringBuffer("FormatConfig[type=").append(getType()).append(", enabled=")
                                                      .append(isEnabled()).append(", abstract=")
                                                      .append(getAbstract()).append("]").toString();
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
 
     /**
      * @return Returns the type.
      */
     public String getType() {
         return type;
     }
 
     /**
      * @return Returns the url.
      */
     public String getUrl() {
         return url;
     }
 
     /**
      * getNameSpace purpose.
      *
      * @return NameSpaceInfo the namespace for this format.
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
 
     public synchronized GridCoverageReader getReader() {
        if (reader != null && reader.get() != null) {
             return (GridCoverageReader) reader.get();
         }
 
         try {
             // /////////////////////////////////////////////////////////
             //
             // Getting coverage config
             //
             // /////////////////////////////////////////////////////////
             final CoverageStoreInfo gcInfo = data.getFormatInfo(id);
 
             if (gcInfo == null) {
                 return null;
             }
 
             // /////////////////////////////////////////////////////////
             //
             // Getting coverage reader using the format and the real path.
             //
             // /////////////////////////////////////////////////////////
             final File obj = GeoserverDataDirectory.findDataFile(gcInfo.getUrl());
 
             // XXX CACHING READERS HERE
            reader = new WeakReference(((AbstractGridFormat) gcInfo.getFormat()).getReader(obj));
 
             return (GridCoverageReader) reader.get();
         } catch (InvalidParameterValueException e) {
             LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
         } catch (ParameterNotFoundException e) {
             LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
         } catch (IllegalArgumentException e) {
             LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
         } catch (SecurityException e) {
             LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
         }
 
         return null;
     }
 
     public synchronized GridCoverageReader createReader(Hints hints) {
        if (hintReader != null && hintReader.get() != null) {
             return (GridCoverageReader) hintReader.get();
        } else if ((hints == null) && (reader != null && reader.get() != null)) {
             return (GridCoverageReader) reader.get();
         }
 
         try {
             // /////////////////////////////////////////////////////////
             //
             // Getting coverage config
             //
             // /////////////////////////////////////////////////////////
             final CoverageStoreInfo gcInfo = data.getFormatInfo(id);
 
             if (gcInfo == null) {
                 return null;
             }
 
             // /////////////////////////////////////////////////////////
             //
             // Getting coverage reader using the format and the real path.
             //
             // /////////////////////////////////////////////////////////
             final File obj = GeoserverDataDirectory.findDataFile(gcInfo.getUrl());
 
             // XXX CACHING READERS HERE
            hintReader = new WeakReference(((AbstractGridFormat) gcInfo.getFormat()).getReader(
                         obj, hints));
 
             return (GridCoverageReader) hintReader.get();
         } catch (InvalidParameterValueException e) {
             LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
         } catch (ParameterNotFoundException e) {
             LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
         } catch (IllegalArgumentException e) {
             LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
         } catch (SecurityException e) {
             LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
         }
 
         return null;
     }
 }
