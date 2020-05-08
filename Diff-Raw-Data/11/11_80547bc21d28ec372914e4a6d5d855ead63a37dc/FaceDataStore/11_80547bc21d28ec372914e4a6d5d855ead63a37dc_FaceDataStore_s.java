 /**
  * GeoTools Example
  * 
  *  (C) 2011 LISAsoft
  *  
  *  This library is free software; you can redistribute it and/or modify it under
  *  the terms of the GNU Lesser General Public License as published by the Free
  *  Software Foundation; version 2.1 of the License.
  *  
  *  This library is distributed in the hope that it will be useful, but WITHOUT
  *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  *  FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
  */
 package com.lisasoft.face.data;
 
 import java.beans.BeanInfo;
 import java.beans.PropertyDescriptor;
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.geotools.data.Query;
 import org.geotools.data.store.ContentDataStore;
 import org.geotools.data.store.ContentEntry;
 import org.geotools.data.store.ContentFeatureSource;
 import org.geotools.feature.NameImpl;
 import org.geotools.feature.simple.SimpleFeatureBuilder;
 import org.geotools.util.WeakValueHashMap;
 import org.opengis.feature.simple.SimpleFeature;
 import org.opengis.feature.simple.SimpleFeatureType;
 import org.opengis.feature.type.AttributeDescriptor;
 import org.opengis.feature.type.Name;
 
 /**
  * This is a really simple DataStore that is built around a List<Face>.
  * <p>
  * It provides wrappers making the contents available as Feature for use the rendering engine.
  * <p>
  * The first draft is read-only; as such we do not need to worry about transactions or modifications
  * to the wrapped objects.
  * 
  * @author Jody Garnett
  */
 public class FaceDataStore extends ContentDataStore {
 
     private FaceDAO data;
 
     public FaceDataStore(FaceDAO data) {
         this.data = data;
     }
 
     protected List<Name> createTypeNames() throws IOException {
         return Collections.singletonList((Name) new NameImpl("Face"));
     }
 
     @Override
     protected ContentFeatureSource createFeatureSource(ContentEntry entry) throws IOException {
         return new FaceFeatureSource(entry, Query.ALL);
     }
 
     public FaceDAO getData() {
         return data;
     }
 
     public SimpleFeature toFeature(SimpleFeatureType schema, Face face) {
         BeanInfo info = getData().getBeanInfo();
 
         SimpleFeatureBuilder build = new SimpleFeatureBuilder(schema);
         Map<String, PropertyDescriptor> lookup = access(info, schema);
         for( AttributeDescriptor attribute : schema.getAttributeDescriptors()){
             String name = attribute.getLocalName();
             PropertyDescriptor descriptor = lookup.get(name);
             Method read = descriptor.getReadMethod();
            Object value;
             try {
             // no argument getXXX() or isXXX()
                value = read.invoke(face, null);
             } catch (Exception e) {
                value = null;
             }
             build.set(name,value);
         }
         // build using identifier for "FeatureID"
         SimpleFeature feature = build.buildFeature("Face."+face.getNummer() );
         return feature;
     }
 
     private static WeakValueHashMap< String, Map<String, PropertyDescriptor>> cache =
             new WeakValueHashMap<String, Map<String, PropertyDescriptor>>();
     /**
      * List of property descriptors used to access a bean.
      * 
      * @param info
      * @param schema
      * @return
      */
     public static synchronized Map<String, PropertyDescriptor> access(BeanInfo info, SimpleFeatureType schema) {
         Map<String, PropertyDescriptor> lookup;
         
         lookup = cache.get(schema.getTypeName());
         if( lookup == null ){
             lookup = new HashMap<String, PropertyDescriptor>();
             for( PropertyDescriptor property : info.getPropertyDescriptors() ){
                 String name = property.getName();
                 schema.getDescriptor(name);
                 lookup.put(name, property );
             }
             cache.put( schema.getTypeName(), lookup);
         }        
         return lookup;
     }
 
 }
