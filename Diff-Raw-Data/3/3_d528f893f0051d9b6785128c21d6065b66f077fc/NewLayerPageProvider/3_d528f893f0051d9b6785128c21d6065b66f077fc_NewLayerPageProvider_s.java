 /* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
  * This code is licensed under the GPL 2.0 license, available at the root
  * application directory.
  */
 package org.geoserver.web.data.layer;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.Model;
 import org.geoserver.catalog.CatalogBuilder;
 import org.geoserver.catalog.CoverageInfo;
 import org.geoserver.catalog.DataStoreInfo;
 import org.geoserver.catalog.ResourceInfo;
 import org.geoserver.catalog.StoreInfo;
 import org.geoserver.web.wicket.GeoServerDataProvider;
 import org.opengis.feature.type.Name;
 
 /**
  * Provides a list of resources for a specific data store
  * @author Andrea Aime - OpenGeo
  *
  */
 @SuppressWarnings("serial")
 public class NewLayerPageProvider extends GeoServerDataProvider<Resource> {
     
     public static final Property<Resource> NAME = new BeanProperty<Resource>("name", "localName");
     public static final Property<Resource> PUBLISHED = new BeanProperty<Resource>("published", "published");
     
     public static final List<Property<Resource>> PROPERTIES = Arrays.asList(NAME, PUBLISHED);
     
     boolean showPublished;
     
     String storeId;
     
     @Override
     protected List<Resource> getItems() {
         // return an empty list in case we still don't know about the store
         if(storeId == null)
             return new ArrayList<Resource>();
         
         // else, grab the resource list
         try {
             List<Resource> result;
             StoreInfo store = getCatalog().getStore(storeId, StoreInfo.class);
             
             Map<String, Resource> resources = new HashMap<String, Resource>();
             if(store instanceof DataStoreInfo) {
                 DataStoreInfo dstore = (DataStoreInfo) store;
                 
                 // collect all the type names and turn them into resources
                 // for the moment we use local names as datastores are not returning
                 // namespace qualified NameImpl
                 List<Name> names = dstore.getDataStore(null).getNames();
                 for (Name name : names) {
                     resources.put(name.getLocalPart(), new Resource(name));
                 }
                 
             } else {
                 // getting to the coverage name without reading the whole coverage seems to
                 // be hard stuff, let's have the catalog builder to the heavy lifting
                 CatalogBuilder builder = new CatalogBuilder(getCatalog());
                 builder.setStore(store);
                 CoverageInfo ci = builder.buildCoverage();
                 Name name = ci.getQualifiedName();
                 resources.put(name.getLocalPart(), new Resource(name));
             }
             
             // lookup all configured layers, mark them as published in the resources
             List<ResourceInfo> configuredTypes = getCatalog().getResourcesByStore(store, ResourceInfo.class);
             for (ResourceInfo type : configuredTypes) {
                 // compare with native name, which is what the DataStore provides through getNames()
                 // above
                 Resource resource = resources.get(type.getNativeName());
                 if(resource != null)
                     resource.setPublished(true);
             }
             result = new ArrayList<Resource>(resources.values());
             
             // return by natural order
             Collections.sort(result);
             return result;
         } catch(Exception e) {
            throw new RuntimeException("Could not list layers for this store, an error occurred retrieving them", e);
         }
             
     }
     
     public String getStoreId() {
         return storeId;
     }
 
     public void setStoreId(String storeId) {
         this.storeId = storeId;
     }
 
     @Override
     protected List<Resource> getFilteredItems() {
         List<Resource> resources = super.getFilteredItems();
         if(showPublished)
             return resources;
         
         List<Resource> unconfigured = new ArrayList<Resource>();
         for (Resource resource : resources) {
             if(!resource.isPublished())
                 unconfigured.add(resource);
         }
         return unconfigured;
     }
 
     @Override
     protected List<Property<Resource>> getProperties() {
         return PROPERTIES;
     }
 
     public IModel model(Object object) {
         return new Model((Serializable) object);
     }
 
     public void setShowPublished(boolean showPublished) {
         this.showPublished = showPublished;
     }
 
     
 }
