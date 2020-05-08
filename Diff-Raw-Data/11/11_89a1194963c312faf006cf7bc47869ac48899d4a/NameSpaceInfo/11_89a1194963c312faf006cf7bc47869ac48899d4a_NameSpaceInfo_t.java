 /* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 /* Copyright (c) 2004 TOPP - www.openplans.org.  All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.vfny.geoserver.global;
 
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 
 import org.geotools.data.FeatureTypeMetaData;
 import org.geotools.data.NamespaceMetaData;
 import org.vfny.geoserver.global.dto.NameSpaceInfoDTO;
 
 
 /**
  * NameSpaceInfo purpose.
  * 
  * <p>
  * A representation of a namespace for the Geoserver application.
  * </p>
  * 
  * <p></p>
  * 
  * <p>
  * NameSpaceInfo ns = new NameSpaceInfo(dto); System.out.println(ns.getPrefix()
  * + ns.getUri());
  * </p>
  *
  * @author dzwiers, Refractions Research, Inc.
 * @version $Id: NameSpaceInfo.java,v 1.10 2004/04/22 08:26:40 emperorkefka Exp $
  */
 public class NameSpaceInfo extends GlobalLayerSupertype
     implements NamespaceMetaData {
 	
 	private String prefix;
 	private String uri;
 	private boolean _default;
 
     /** ref to parent set of datastores. */
     private Data data;
 
     /** metadata */
     private Map meta;
 
     /**
      * NameSpaceConfig constructor.
      * 
      * <p>
      * Creates a NameSpaceConfig based on the data provided. All the data
      * structures are cloned.
      * </p>
      *
      * @param data DOCUMENT ME!
      * @param ns The namespace to copy.
      *
      * @throws NullPointerException when the param is null
      */
     public NameSpaceInfo(Data data, NameSpaceInfoDTO ns) {
         if (ns == null) {
             throw new NullPointerException("Non null NameSpaceInfoDTO required");
         }
 
         if (data == null) {
             throw new NullPointerException("Non null Data required");
         }
 
         this.data = data;
         
         prefix = ns.getPrefix();
         uri = ns.getUri();
         _default = ns.isDefault();
     }
 
     /**
      * NameSpaceConfig constructor.
      * 
      * <p>
      * Creates a copy of the NameSpaceConfig provided. All the data structures
      * are cloned.
      * </p>
      *
      * @param ns The namespace to copy.
      *
      * @throws NullPointerException when the param is null
      */
     public NameSpaceInfo(NameSpaceInfo ns) {
         if (ns == null) {
             throw new NullPointerException();
         }
 
         setPrefix(ns.getPrefix());
         setUri(ns.getUri());
         setDefault(ns.isDefault());
     }
 
     /**
      * Implement toDTO.
      * 
      * <p>
      * Package method used by GeoServer. This method may return references, and
      * does not clone, so extreme caution sould be used when traversing the
      * results.
      * </p>
      *
      * @return NameSpaceInfoDTO An instance of the data this class represents.
      *         Please see Caution Above.
      *
      * @see org.vfny.geoserver.global.GlobalLayerSupertype#toDTO()
      * @see NameSpaceInfoDTO
      */
     Object toDTO() {
     	NameSpaceInfoDTO dto = new NameSpaceInfoDTO();
     	dto.setDefault(isDefault());
     	dto.setPrefix(getPrefix());
     	dto.setUri(getUri());
         return dto;
     }
 
     /**
      * Implement clone.
      * 
      * <p>
      * creates a clone of this object
      * </p>
      *
      * @return A copy of this NameSpaceConfig
      *
      * @see java.lang.Object#clone()
      */
     public Object clone() {
         return new NameSpaceInfo(this);
     }
 
     /**
      * Implement equals.
      * 
      * <p>
      * recursively tests to determine if the object passed in is a copy of this
      * object.
      * </p>
      *
      * @param obj The NameSpaceConfig object to test.
      *
      * @return true when the object passed is the same as this object.
      *
      * @see java.lang.Object#equals(java.lang.Object)
      */
     public boolean equals(Object obj) {
         NameSpaceInfo ns = (NameSpaceInfo) obj;
 
         return ((getPrefix() == ns.getPrefix())
         && ((getUri() == ns.getUri())
         && (isDefault() == ns.isDefault())));
     }
 
     /**
      * isDefault purpose.
      * 
      * <p>
      * Whether this is the default namespace.
      * </p>
      *
      * @return true when this is the default namespace.
      */
     public boolean isDefault() {
         return _default;
     }
 
     /**
      * getPrefix purpose.
      * 
      * <p>
      * returns the namespace's prefix.
      * </p>
      *
      * @return String the namespace's prefix
      */
     public String getPrefix() {
         return prefix;
     }
 
     /**
      * getUri purpose.
      * 
      * <p>
      * returns the namespace's uri.
      * </p>
      *
      * @return String the namespace's uri.
      */
     public String getUri() {
         return uri;
     }
     
     /**
      * Implementation of getURI.
      * 
      * @see org.geotools.data.NamespaceMetaData#getURI()
      * 
      * @return
      */
     public String getURI() {
     	return uri;
     }
 
     /**
      * setDdefault purpose.
      * 
      * <p>
      * sets the default namespace.
      * </p>
      *
      * @param b this is the default namespace.
      */
     public void setDefault(boolean b) {
         _default = b;
     }
 
     /**
      * setPrefix purpose.
      * 
      * <p>
      * stores the namespace's prefix.
      * </p>
      *
      * @param string the namespace's prefix.
      */
     public void setPrefix(String string) {
         prefix=string;
     }
 
     /**
      * setUri purpose.
      * 
      * <p>
      * Stores the namespace's uri.
      * </p>
      *
      * @param string the namespace's uri.
      */
     public void setUri(String string) {
         uri=string;
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
      * This should be a list of available typeNames for the namespace.
      * 
      * <p>
      * Makes use of data to get the list of all FeatureTypes, returns the names
      * that match this prefix. This is just the typeName and not the full
      * prefix:typeName.
      * </p>
      *
      * @return
      *
      * @see org.geotools.data.NamespaceMetaData#getTypeNames()
      */
     public Set getTypeNames() {
         Set set = new HashSet();
 
         for (Iterator i = data.getFeatureTypeInfos().values().iterator();
                 i.hasNext();) {
             FeatureTypeInfo type = (FeatureTypeInfo) i.next();
 
             if (type.getNameSpace() == this) {
                 set.add(type.getName());
             }
         }
 
         return set;
     }
 
     /**
     * Search for FeatureTypeInfo based on prefix:typeName
      * 
      * <p>
     * Convience method for data.getFeatureTypeInfo( typeName, uri );
      * </p>
      *
      * @param typeName
      *
      * @return
      *
      * @see org.geotools.data.NamespaceMetaData#getFeatureTypeMetaData(java.lang.String)
      */
     public FeatureTypeMetaData getFeatureTypeMetaData(String typeName) {
        return data.getFeatureTypeInfo(typeName, uri );
     }
 
     public String toString() {
         return getPrefix() + ":" + getUri();
     }
 }
