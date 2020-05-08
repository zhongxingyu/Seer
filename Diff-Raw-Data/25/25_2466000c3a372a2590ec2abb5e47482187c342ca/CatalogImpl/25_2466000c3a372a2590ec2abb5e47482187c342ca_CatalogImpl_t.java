 /* Copyright (c) 2001 - 2008 TOPP - www.openplans.org. All rights reserved.
  * This code is licensed under the GPL 2.0 license, available at the root
  * application directory.
  */
 package org.geoserver.catalog.impl;
 
 import java.lang.reflect.Method;
 import java.lang.reflect.Proxy;
 import java.net.URI;
 import java.rmi.server.UID;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.apache.commons.collections.MultiHashMap;
 import org.geoserver.catalog.Catalog;
 import org.geoserver.catalog.CatalogException;
 import org.geoserver.catalog.CatalogFactory;
 import org.geoserver.catalog.CatalogInfo;
 import org.geoserver.catalog.CatalogVisitor;
 import org.geoserver.catalog.CoverageDimensionInfo;
 import org.geoserver.catalog.CoverageInfo;
 import org.geoserver.catalog.CoverageStoreInfo;
 import org.geoserver.catalog.DataStoreInfo;
 import org.geoserver.catalog.FeatureTypeInfo;
 import org.geoserver.catalog.LayerGroupInfo;
 import org.geoserver.catalog.LayerInfo;
 import org.geoserver.catalog.MapInfo;
 import org.geoserver.catalog.MetadataMap;
 import org.geoserver.catalog.NamespaceInfo;
 import org.geoserver.catalog.ResourceInfo;
 import org.geoserver.catalog.ResourcePool;
 import org.geoserver.catalog.StoreInfo;
 import org.geoserver.catalog.StyleInfo;
 import org.geoserver.catalog.WMSLayerInfo;
 import org.geoserver.catalog.WorkspaceInfo;
 import org.geoserver.catalog.event.CatalogAddEvent;
 import org.geoserver.catalog.event.CatalogEvent;
 import org.geoserver.catalog.event.CatalogListener;
 import org.geoserver.catalog.event.CatalogModifyEvent;
 import org.geoserver.catalog.event.CatalogPostModifyEvent;
 import org.geoserver.catalog.event.CatalogRemoveEvent;
 import org.geoserver.catalog.event.impl.CatalogAddEventImpl;
 import org.geoserver.catalog.event.impl.CatalogModifyEventImpl;
 import org.geoserver.catalog.event.impl.CatalogPostModifyEventImpl;
 import org.geoserver.catalog.event.impl.CatalogRemoveEventImpl;
 import org.geoserver.ows.util.ClassProperties;
 import org.geoserver.ows.util.OwsUtils;
 import org.geoserver.platform.GeoServerResourceLoader;
 import org.geotools.util.logging.Logging;
 import org.opengis.feature.type.Name;
 
 /**
  * A default catalog implementation that is memory based.
  * 
  * @author Justin Deoliveira, The Open Planning Project
  * 
  */
 public class CatalogImpl implements Catalog {
     
     /**
      * logger
      */
     private static final Logger LOGGER = Logging.getLogger(CatalogImpl.class);
 
     /**
      * Contains the stores keyed by implementation class
      */
     protected MultiHashMap/* <Class> */stores = new MultiHashMap();
     
     /**
      * The default store keyed by workspace id
      */
     protected Map<String, DataStoreInfo> defaultStores = new HashMap<String, DataStoreInfo>();
 
     /**
      * resources
      */
     protected MultiHashMap/* <Class> */resources = new MultiHashMap();
 
     /**
      * namespaces
      */
     protected HashMap<String, NamespaceInfo> namespaces = new HashMap<String, NamespaceInfo>();
 
     /**
      * workspaces
      */
     protected HashMap<String, WorkspaceInfo> workspaces = new HashMap<String, WorkspaceInfo>();
     
     /**
      * layers
      */
     protected List<LayerInfo> layers = new ArrayList();
 
     /**
      * maps
      */
     protected List<MapInfo> maps = new ArrayList<MapInfo>();
 
     /**
      * layer groups
      */
     protected List<LayerGroupInfo> layerGroups = new ArrayList<LayerGroupInfo>();
     
     /**
      * styles
      */
     protected List<StyleInfo> styles = new ArrayList();
 
     /**
      * listeners
      */
     protected List listeners = new ArrayList();
 
     /** 
      * resources
      */
     protected ResourcePool resourcePool;
     protected GeoServerResourceLoader resourceLoader;
 
     public CatalogImpl() {
         resourcePool = new ResourcePool(this);
     }
     
     public String getId() {
         return "catalog";
     }
     
     public CatalogFactory getFactory() {
         return new CatalogFactoryImpl( this );
     }
 
     // Store methods
     public void add(StoreInfo store) {
         
         if ( store.getWorkspace() == null ) {
             store.setWorkspace( getDefaultWorkspace() );
         }
 
         validate(store, true);
         resolve(store);
         synchronized (stores) {
             stores.put(store.getClass(), store);
             // if there is no default store use this one as the default
             if(getDefaultDataStore(store.getWorkspace()) == null && store instanceof DataStoreInfo) {
                 setDefaultDataStore(store.getWorkspace(), (DataStoreInfo) store);
             }
         }
         added(store);
     }
 
     void validate(StoreInfo store, boolean isNew) {
         if ( isNull(store.getName()) ) {
             throw new IllegalArgumentException( "Store name must not be null");
         }
         if ( store.getWorkspace() == null ) {
             throw new IllegalArgumentException( "Store must be part of a workspace");
         }
         
         WorkspaceInfo workspace = store.getWorkspace();
         StoreInfo existing = getStoreByName( workspace, store.getName(), StoreInfo.class );
         if ( existing != null && !existing.getId().equals( store.getId() )) {
             String msg = "Store '"+ store.getName() +"' already exists in workspace '"+workspace.getName()+"'";
             throw new IllegalArgumentException( msg );
         }    
     }
     
     public void remove(StoreInfo store) {
         if ( !getResourcesByStore(store, ResourceInfo.class).isEmpty() ) {
             throw new IllegalArgumentException( "Unable to delete non-empty store.");
         }
         store = unwrap(store);
         
         synchronized(stores) {
             stores.remove(store.getClass(),store);
             
             WorkspaceInfo workspace = store.getWorkspace();
             DataStoreInfo defaultStore = getDefaultDataStore(workspace);
             if (store.equals(defaultStore)) {
                 defaultStores.remove(workspace.getId());
                 
                 // default removed, choose another store to become default if possible
                 List dstores = getDataStoresByWorkspace(workspace);
                 if (!dstores.isEmpty()) {
                     setDefaultDataStore(workspace, (DataStoreInfo) dstores.get(0));
                 }
             }
         }
         
         removed(store);
     }
 
     public void save(StoreInfo store) {
         validate(store, false);
         
         if ( store.getId() == null ) {
             //add it instead of saving
             add( store );
             return;
         }
         
         saved(store);
     }
 
     public <T extends StoreInfo> T getStore(String id, Class<T> clazz) {
         List l = lookup(clazz, stores);
         for (Iterator i = l.iterator(); i.hasNext();) {
             StoreInfo store = (StoreInfo) i.next();
             if (id.equals(store.getId())) {
                 return ModificationProxy.create( (T) store, clazz );
                 //return store;
             }
         }
 
         return null;
     }
 
     public <T extends StoreInfo> T getStoreByName(String name, Class<T> clazz) {
        T store = getStoreByName( (WorkspaceInfo) null, name, clazz );
        if ( store != null ) {
            return store;
        }
        
        //look for secondary match
        List l = lookup(clazz, stores);
        ArrayList matches = new ArrayList();
        for (Iterator i = l.iterator(); i.hasNext();) {
            store = (T) i.next();
            if ( name.equals( store.getName() ) ) {
                matches.add( store );
            }
        }
        
        if ( matches.size() == 1 ) {
            return ModificationProxy.create( (T) matches.get( 0 ), clazz);
        }
        
        return null;
     }
 
     public <T extends StoreInfo> T getStoreByName(WorkspaceInfo workspace,
             String name, Class<T> clazz) {
         
         if ( workspace == null ) {
             workspace = getDefaultWorkspace();
         }
         
         if(name == null || name.equals(DEFAULT)) {
             return (T) getDefaultDataStore(workspace);
         }
         
         List l = lookup(clazz, stores);
         for (Iterator i = l.iterator(); i.hasNext();) {
             StoreInfo store = (StoreInfo) i.next();
             if (name.equals(store.getName()) && store.getWorkspace().equals( workspace )) {
                 return ModificationProxy.create( (T) store, clazz );
             }
         }
         
         return null;
     }
 
     public <T extends StoreInfo> T getStoreByName(String workspaceName,
            String name, Class<T> clazz) {
         return getStoreByName(
             workspaceName != null ? getWorkspaceByName(workspaceName) : null, name, clazz);
     }
     
     public <T extends StoreInfo> List<T> getStoresByWorkspace(
             String workspaceName, Class<T> clazz) {
         
         WorkspaceInfo workspace = null;
         if ( workspaceName != null ) {
             workspace = getWorkspaceByName(workspaceName);
             if ( workspace == null ) {
                 return Collections.EMPTY_LIST;
             }
         }
         
         return getStoresByWorkspace(workspace, clazz);
         
     }
 
     public <T extends StoreInfo> List<T> getStoresByWorkspace(
             WorkspaceInfo workspace, Class<T> clazz) {
 
         if ( workspace == null ) {
             workspace = getDefaultWorkspace();
         }
 
         List all = lookup(clazz, stores);
         List matches = new ArrayList();
 
         for (Iterator s = all.iterator(); s.hasNext();) {
             StoreInfo store = (StoreInfo) s.next();
             if (workspace.equals(store.getWorkspace())) {
                 matches.add(store);
             }
         }
 
         return ModificationProxy.createList(matches,clazz);
     }
 
     public List getStores(Class clazz) {
         return ModificationProxy.createList(lookup(clazz, stores) , clazz);
     }
 
     public DataStoreInfo getDataStore(String id) {
         return (DataStoreInfo) getStore(id, DataStoreInfo.class);
     }
 
     public DataStoreInfo getDataStoreByName(String name) {
         return (DataStoreInfo) getStoreByName(name,DataStoreInfo.class);
     }
 
     public DataStoreInfo getDataStoreByName(String workspaceName, String name) {
         return (DataStoreInfo) getStoreByName(workspaceName, name, DataStoreInfo.class);
     }
 
     public DataStoreInfo getDataStoreByName(WorkspaceInfo workspace, String name) {
         return (DataStoreInfo) getStoreByName(workspace, name, DataStoreInfo.class);
     }
 
     public List<DataStoreInfo> getDataStoresByWorkspace(String workspaceName) {
         return getStoresByWorkspace( workspaceName, DataStoreInfo.class );
     }
     
     public List<DataStoreInfo> getDataStoresByWorkspace(WorkspaceInfo workspace) {
         return getStoresByWorkspace( workspace, DataStoreInfo.class );
     }
 
     public List getDataStores() {
         return getStores(DataStoreInfo.class);
     }
     
     public DataStoreInfo getDefaultDataStore(WorkspaceInfo workspace) {
         if(defaultStores.containsKey(workspace.getId())) {
             DataStoreInfo defaultStore = defaultStores.get(workspace.getId());
             return ModificationProxy.create(defaultStore, DataStoreInfo.class);
         } else {
             return null;
         }
     }
     
     public void setDefaultDataStore(WorkspaceInfo workspace, DataStoreInfo store) {
         // basic sanity check
         if (store.getWorkspace() == null) {
             throw new IllegalArgumentException("The store has not been assigned a workspace");
         }
 
         if (!store.getWorkspace().equals(workspace)) {
             throw new IllegalArgumentException("Trying to mark as default " + "for workspace "
                     + workspace.getName() + " a store that " + "is contained in "
                     + store.getWorkspace().getName());
         }
         
         DataStoreInfo old = defaultStores.get(workspace.getId());
         defaultStores.put(workspace.getId(), store);
         
         //fire change event
         fireModified(this, 
             Arrays.asList("defaultDataStore"), Arrays.asList(old), Arrays.asList(store));
     }
 
     public CoverageStoreInfo getCoverageStore(String id) {
         return (CoverageStoreInfo) getStore(id, CoverageStoreInfo.class);
     }
 
     public CoverageStoreInfo getCoverageStoreByName(String name) {
         return (CoverageStoreInfo) getStoreByName(name, CoverageStoreInfo.class);
     }
     
     public CoverageStoreInfo getCoverageStoreByName(String workspaceName,
             String name) {
         return getStoreByName(workspaceName,name,CoverageStoreInfo.class);
     }
     
     public CoverageStoreInfo getCoverageStoreByName(WorkspaceInfo workspace,
             String name) {
         return getStoreByName(workspace, name,CoverageStoreInfo.class);
     }
     
     public List<CoverageStoreInfo> getCoverageStoresByWorkspace(
             String workspaceName) {
         return getStoresByWorkspace( workspaceName, CoverageStoreInfo.class );
     }
     
     public List<CoverageStoreInfo> getCoverageStoresByWorkspace(
             WorkspaceInfo workspace) {
         return getStoresByWorkspace( workspace, CoverageStoreInfo.class );
     }
 
     public List getCoverageStores() {
         return getStores(CoverageStoreInfo.class);
     }
 
     // Resource methods
     public void add(ResourceInfo resource) {
         if ( resource.getNamespace() == null ) {
             //default to default namespace
             resource.setNamespace( getDefaultNamespace() );
         }
         
         validate(resource,true);
         resolve(resource);
         resources.put(resource.getClass(), resource);
         added(resource);
     }
 
     void validate(ResourceInfo resource, boolean isNew) {
         if ( isNull(resource.getName()) ) {
             throw new NullPointerException( "Resource name must not be null");
         }
         if ( resource.getStore() == null ) {
             throw new IllegalArgumentException( "Resource must be part of a store");
         }
         if ( resource.getNamespace() == null ) {
             throw new IllegalArgumentException( "Resource must be part of a namespace");
         }
         
         StoreInfo store = resource.getStore();
         ResourceInfo existing = getResourceByStore( store, resource.getName(), ResourceInfo.class);
         if ( existing != null && !existing.getId().equals( resource.getId() ) ) {
             String msg = "Resource named '"+resource.getName()+"' already exists in store: '"+ store.getName()+"'";
             throw new IllegalArgumentException( msg );
         }
         
         NamespaceInfo namespace = resource.getNamespace();
         existing =  getResourceByName( namespace, resource.getName(), ResourceInfo.class);
         if ( existing != null && !existing.getId().equals( resource.getId() ) ) {
             String msg = "Resource named '"+resource.getName()+"' already exists in namespace: '"+ namespace.getPrefix()+"'";
             throw new IllegalArgumentException( msg );
         }
         
     }
     
     public void remove(ResourceInfo resource) {
         //ensure no references to the resource
         if ( !getLayers( resource ).isEmpty() ) {
             throw new IllegalArgumentException( "Unable to delete resource referenced by layer");
         }
         resource = unwrap(resource);
         resources.remove(resource.getClass(), resource);
         removed(resource);
     }
 
     public void save(ResourceInfo resource) {
         validate(resource,false);
         saved(resource);
     }
 
     public <T extends ResourceInfo> T getResource(String id, Class<T> clazz) {
         List l = lookup(clazz, resources);
         for (Iterator i = l.iterator(); i.hasNext();) {
             ResourceInfo resource = (ResourceInfo) i.next();
             if (id.equals(resource.getId())) {
                 return ModificationProxy.create((T) resource, clazz );
             }
         }
 
         return null;
     }
 
     public <T extends ResourceInfo> T getResourceByName(String ns, String name, Class<T> clazz) {
 
         NamespaceInfo namespace = null;
         if ("".equals( ns ) ) {
             ns = null;
         }
         if ( ns == null ) {
             //if namespace was null, try the default namespace
             if ( getDefaultNamespace() != null ) {
                 namespace = getDefaultNamespace();
             }
         }
         else {
             namespace = getNamespaceByPrefix( ns );
             if ( namespace == null ) {
                 namespace = getNamespaceByURI( ns );
             }
         }
 
         List l = lookup(clazz, resources);
         if ( namespace != null ) {
             for (Iterator i = l.iterator(); i.hasNext();) {
                 ResourceInfo resource = (ResourceInfo) i.next();
                 if (name.equals(resource.getName())) {
                     NamespaceInfo namespace1 = resource.getNamespace();
                     if (namespace1 != null && namespace1.equals( namespace )) {
                             return ModificationProxy.create( (T) resource, clazz );
                     }
                 }
             }
         }
 
         if ( ns == null ) {
             // no namespace was specified, so do an exhaustive lookup
             List matches = new ArrayList();
             for (Iterator i = l.iterator(); i.hasNext();) {
                 ResourceInfo resource = (ResourceInfo) i.next();
                 if (name.equals(resource.getName())) {
                     matches.add( resource );
                 }
             }
             
             if ( matches.size() == 1 ) {
                 return ModificationProxy.create( (T) matches.get( 0 ), clazz );
             }
         }
         return null;
     }
     
     public <T extends ResourceInfo> T getResourceByName(NamespaceInfo ns,
             String name, Class<T> clazz) {
         return getResourceByName( ns != null ? ns.getPrefix() : null , name, clazz);
     }
 
     public <T extends ResourceInfo> T getResourceByName(Name name, Class<T> clazz) {
         return getResourceByName( name.getNamespaceURI(), name.getLocalPart(), clazz );
     }
     
     public <T extends ResourceInfo> T getResourceByName( String name, Class<T> clazz ) {
         ResourceInfo resource;
         
         // check is the name is a fully qualified one
         int colon = name.indexOf( ':' );
         if ( colon != -1 ) {
             String ns = name.substring(0, colon);
             String localName = name.substring(colon + 1);
             return getResourceByName(ns, localName, clazz);
         }
         else {
             return getResourceByName((String)null,name,clazz);
         }
     }
     
     public List getResources(Class clazz) {
         return ModificationProxy.createList( lookup(clazz,resources), clazz );
     }
 
     public List getResourcesByNamespace(NamespaceInfo namespace, Class clazz) {
         List all = lookup(clazz, resources);
         List matches = new ArrayList();
 
         if ( namespace == null ) {
             namespace = getDefaultNamespace();
         }
 
         for (Iterator r = all.iterator(); r.hasNext();) {
             ResourceInfo resource = (ResourceInfo) r.next();
             if (namespace != null ) {
                 if (namespace.equals(resource.getNamespace())) {
                     matches.add( resource );
                 }
             }
             else if ( resource.getNamespace() == null ) {
                 matches.add(resource);
             }
         }
 
         return ModificationProxy.createList( matches, clazz );
     }
     
     public <T extends ResourceInfo> List<T> getResourcesByNamespace(
             String namespace, Class<T> clazz) {
         if ( namespace == null ) {
             return getResourcesByNamespace((NamespaceInfo)null,clazz); 
         }
         
         NamespaceInfo ns = getNamespaceByPrefix(namespace);
         if ( ns == null ) {
             ns = getNamespaceByURI(namespace);
         }
         if ( ns == null ) {
             return Collections.EMPTY_LIST;
         }
         
         return getResourcesByNamespace(ns, clazz);
     }
 
     public <T extends ResourceInfo> T getResourceByStore(StoreInfo store,
             String name, Class<T> clazz) {
         List all = lookup(clazz,resources);
         for (Iterator r = all.iterator(); r.hasNext(); ) {
             ResourceInfo resource = (ResourceInfo) r.next();
             if ( name.equals( resource.getName() ) && store.equals( resource.getStore() ) ) {
                 return ModificationProxy.create((T)resource, clazz);
             }
                 
         }
         
         return null;
     }
 
     public <T extends ResourceInfo> List<T> getResourcesByStore(
             StoreInfo store, Class<T> clazz) {
         List all = lookup(clazz,resources);
         List matches = new ArrayList();
         
         for (Iterator r = all.iterator(); r.hasNext();) {
             ResourceInfo resource = (ResourceInfo) r.next();
             if (store.equals(resource.getStore())) {
                 matches.add(resource);
             }
         }
 
         return  ModificationProxy.createList( matches, clazz );
     }
 
     public FeatureTypeInfo getFeatureType(String id) {
         return (FeatureTypeInfo) getResource(id, FeatureTypeInfo.class);
     }
 
     public FeatureTypeInfo getFeatureTypeByName(String ns, String name) {
         return (FeatureTypeInfo) getResourceByName(ns, name,
                 FeatureTypeInfo.class);
     }
 
     public FeatureTypeInfo getFeatureTypeByName(NamespaceInfo ns, String name) {
         return getResourceByName(ns, name, FeatureTypeInfo.class );
     }
 
     public FeatureTypeInfo getFeatureTypeByName(Name name) {
         return getResourceByName(name, FeatureTypeInfo.class);
     }
     
     public FeatureTypeInfo getFeatureTypeByName(String name) {
         return (FeatureTypeInfo) getResourceByName(name, FeatureTypeInfo.class);
     }
     
     public List getFeatureTypes() {
         return getResources(FeatureTypeInfo.class);
     }
 
     public List getFeatureTypesByNamespace(NamespaceInfo namespace) {
         return getResourcesByNamespace(namespace, FeatureTypeInfo.class);
     }
     
     public FeatureTypeInfo getFeatureTypeByStore(DataStoreInfo dataStore,
             String name) {
         return getFeatureTypeByDataStore(dataStore, name);
     }
     
     public FeatureTypeInfo getFeatureTypeByDataStore(DataStoreInfo dataStore,
             String name) {
         return getResourceByStore( dataStore, name, FeatureTypeInfo.class );
     }
     
     public List<FeatureTypeInfo> getFeatureTypesByStore(DataStoreInfo store) {
         return getFeatureTypesByDataStore(store);
     }
 
     public List<FeatureTypeInfo> getFeatureTypesByDataStore(DataStoreInfo store) {
         return getResourcesByStore(store, FeatureTypeInfo.class);
     }
 
     public CoverageInfo getCoverage(String id) {
         return (CoverageInfo) getResource(id, CoverageInfo.class);
     }
 
     public CoverageInfo getCoverageByName(String ns, String name) {
         return (CoverageInfo) getResourceByName(ns, name, CoverageInfo.class);
     }
     
     public CoverageInfo getCoverageByName(NamespaceInfo ns, String name) {
         return (CoverageInfo) getResourceByName(ns, name, CoverageInfo.class);
     }
     
     public CoverageInfo getCoverageByName(Name name) {
         return getResourceByName(name, CoverageInfo.class);
     }
     
     public CoverageInfo getCoverageByName(String name) {
         return (CoverageInfo) getResourceByName( name, CoverageInfo.class );
     }
 
     public List getCoverages() {
         return getResources(CoverageInfo.class);
     }
 
     public List getCoveragesByNamespace(NamespaceInfo namespace) {
         return getResourcesByNamespace(namespace, CoverageInfo.class);
     }
     
     public List<CoverageInfo> getCoveragesByStore(CoverageStoreInfo store) {
         return getResourcesByStore(store,CoverageInfo.class);
     }
     
     public CoverageInfo getCoverageByCoverageStore(
             CoverageStoreInfo coverageStore, String name) {
         return getResourceByStore( coverageStore, name, CoverageInfo.class );
     }
     public List<CoverageInfo> getCoveragesByCoverageStore(
             CoverageStoreInfo store) {
         return getResourcesByStore( store, CoverageInfo.class );
     }
 
     // Layer methods
     public void add(LayerInfo layer) {
         validate(layer,true);
         resolve(layer);
         
         if ( layer.getType() == null ) {
             if ( layer.getResource() instanceof FeatureTypeInfo ) {
                 layer.setType( LayerInfo.Type.VECTOR );
             } else if ( layer.getResource() instanceof CoverageInfo ) {
                 layer.setType( LayerInfo.Type.RASTER );
             } else if ( layer.getResource() instanceof WMSLayerInfo ) {
                 layer.setType( LayerInfo.Type.WMS );
             } else {
                 String msg = "Layer type not set and can't be derived from resource";
                 throw new IllegalArgumentException( msg );
             }
         }
         
         layers.add(layer);
         added(layer);
     }
 
     void validate( LayerInfo layer, boolean isNew) {
         // TODO: bring back when the layer/publishing split is in act
 //        if ( isNull(layer.getName()) ) {
 //            throw new NullPointerException( "Layer name must not be null" );
 //        }
         
         LayerInfo existing = getLayerByName( layer.getName() );
         if ( existing != null && !existing.getId().equals( layer.getId() ) ) {
             //JD: since layers are not qualified by anything (yet), check 
             // namespace of the resource, if they are different then allow the 
             // layer to be added
             if ( existing.getResource().getNamespace().equals( layer.getName() ) ) {
                 throw new IllegalArgumentException( "Layer named '"+layer.getName()+"' already exists.");
             }
         }
         
         if ( layer.getResource() == null ) {
             throw new NullPointerException( "Layer resource must not be null" );
         }
         //(JD): not sure if default style should be mandatory
         //if ( layer.getDefaultStyle() == null ){
         //    throw new NullPointerException( "Layer default style must not be null" );
         //}
     }
    
     public void remove(LayerInfo layer) {
         //ensure no references to the layer
         for ( LayerGroupInfo lg : layerGroups ) {
             if ( lg.getLayers().contains( layer ) ) {
                 String msg = "Unable to delete layer referenced by layer group '"+lg.getName()+"'";
                 throw new IllegalArgumentException( msg );
             }
         }
         layers.remove(unwrap(layer));
         removed(layer);
     }
 
     public void save(LayerInfo layer) {
         validate( layer, false );
         saved(layer);
     }
 
     public LayerInfo getLayer(String id) {
         for (Iterator l = layers.iterator(); l.hasNext();) {
             LayerInfo layer = (LayerInfo) l.next();
             if (id.equals(layer.getId())) {
                 return ModificationProxy.create( layer, LayerInfo.class );
             }
         }
 
         return null;
     }
     
     
     public LayerInfo getLayerByName(Name name) {
         if ( name.getNamespaceURI() != null ) {
             NamespaceInfo ns = getNamespaceByURI( name.getNamespaceURI() );
             if ( ns != null ) {
                 return getLayerByName( ns.getPrefix() + ":" + name.getLocalPart() );
             }
         }
         
         return getLayerByName( name.getLocalPart() );
     }
     
     public LayerInfo getLayerByName(String name) {
         String prefix = null;
         String resource = null;
         
         int colon = name.indexOf( ':' );
         if ( colon != -1 ) {
             //search by resource name
             prefix = name.substring( 0, colon );
             resource = name.substring( colon + 1 );
             
             for (Iterator l = layers.iterator(); l.hasNext();) {
                 LayerInfo layer = (LayerInfo) l.next();
                 ResourceInfo r = layer.getResource();
                 
                 if ( prefix.equals( r.getNamespace().getPrefix() ) && resource.equals( r.getName() ) ) {
                     return ModificationProxy.create( layer, LayerInfo.class );
                 }
             }
         }
         else {
             //search by layer name
             for (Iterator l = layers.iterator(); l.hasNext();) {
                 LayerInfo layer = (LayerInfo) l.next();
                 if ( name.equals( layer.getName() ) ) {
                     return ModificationProxy.create( layer, LayerInfo.class );
                 }
             }
         }
 
         return null;
     }
 
     public List<LayerInfo> getLayers(ResourceInfo resource) {
         List<LayerInfo> matches = new ArrayList<LayerInfo>();
         for (Iterator l = layers.iterator(); l.hasNext();) {
             LayerInfo layer = (LayerInfo) l.next();
             if ( resource.equals( layer.getResource() ) ) {
                 matches.add( layer );
             }
         }
 
         return ModificationProxy.createList(matches,LayerInfo.class);
     }
     
     public List<LayerInfo> getLayers(StyleInfo style) {
         List<LayerInfo> matches = new ArrayList<LayerInfo>();
         for (Iterator l = layers.iterator(); l.hasNext();) {
             LayerInfo layer = (LayerInfo) l.next();
             if ( style.equals( layer.getDefaultStyle() ) || layer.getStyles().contains( style ) ) {
                 matches.add( layer );
             }
         }
 
         return ModificationProxy.createList(matches,LayerInfo.class);
     }
     
     public List getLayers() {
         return ModificationProxy.createList( new ArrayList(layers), LayerInfo.class );
     }
 
     // Map methods
     public MapInfo getMap(String id) {
         for (MapInfo map : maps) {
             if (id.equals(map.getId())) {
                 return ModificationProxy.create(map,MapInfo.class);
             }
         }
 
         return null;
     }
 
     public MapInfo getMapByName(String name) {
         for (MapInfo map : maps) {
             if (name.equals(map.getName())) {
                 return ModificationProxy.create(map,MapInfo.class);
             }
         }
 
         return null;
     }
     
     public List<MapInfo> getMaps() {
         return ModificationProxy.createList( new ArrayList(maps), MapInfo.class );
     }
 
     public void add(LayerGroupInfo layerGroup) {
         validate(layerGroup,true);
         resolve(layerGroup);
         
         if ( layerGroup.getStyles().isEmpty() ) {
             for ( LayerInfo l : layerGroup.getLayers() ) {
                 // default style
                 layerGroup.getStyles().add(null);
             }    
         }
         
         layerGroups.add( layerGroup );
         added( layerGroup );
     }
     
     void validate( LayerGroupInfo layerGroup, boolean isNew ) {
         if( isNull(layerGroup.getName()) ) {
             throw new NullPointerException( "Layer group name must not be null");
         }
         
         LayerGroupInfo existing = getLayerGroupByName( layerGroup.getName() );
         if ( existing != null && !existing.getId().equals( layerGroup.getId() ) ) {
             throw new IllegalArgumentException( "Layer group named '" + layerGroup.getName() + "' already exists." );
         }
         
         if ( !isNew ) {
             if ( layerGroup.getLayers() == null || layerGroup.getLayers().isEmpty() ) {
                 throw new NullPointerException( "Layer group must not be empty");
             }
         }
        
         if ( layerGroup.getStyles() != null && !layerGroup.getStyles().isEmpty() && 
                 !(layerGroup.getStyles().size() == layerGroup.getLayers().size()) ) {
             throw new IllegalArgumentException( "Layer group has different number of styles than layers");
         }
     }
     
     public void remove(LayerGroupInfo layerGroup) {
         layerGroups.remove( unwrap(layerGroup) );
         removed( layerGroup );
     }
     
     public void save(LayerGroupInfo layerGroup) {
         validate(layerGroup,false);
         saved(layerGroup);
     }
     
     public List<LayerGroupInfo> getLayerGroups() {
         return ModificationProxy.createList( new ArrayList(layerGroups), LayerGroupInfo.class );
     }
     
     public LayerGroupInfo getLayerGroup(String id) {
         for (LayerGroupInfo layerGroup : layerGroups ) {
             if ( id.equals( layerGroup.getId() ) ) {
                 return ModificationProxy.create(layerGroup,LayerGroupInfo.class);
             }
         }
         
         return null;
     }
     
     public LayerGroupInfo getLayerGroupByName(String name) {
         for (LayerGroupInfo layerGroup : layerGroups ) {
             if ( name.equals( layerGroup.getName() ) ) {
                 return ModificationProxy.create(layerGroup,LayerGroupInfo.class);
             }
         }
         
         return null;
     }
     
     public void add(MapInfo map) {
         resolve(map);
         maps.add(map);
         added(map);
     }
 
     public void remove(MapInfo map) {
         maps.remove(unwrap(map));
         removed(map);
     }
 
     public void save(MapInfo map) {
         saved( map );
     }
     
     // Namespace methods
     public NamespaceInfo getNamespace(String id) {
         for (NamespaceInfo namespace : namespaces.values() ) {
             if (id.equals(namespace.getId())) {
                 return ModificationProxy.create( namespace, NamespaceInfo.class ); 
             }
         }
 
         return null;
     }
 
     public NamespaceInfo getNamespaceByPrefix(String prefix) {
         NamespaceInfo ns = namespaces.get( prefix ); 
         return ns != null ? ModificationProxy.create(ns, NamespaceInfo.class ) : null;
     }
 
     public NamespaceInfo getNamespaceByURI(String uri) {
         for (NamespaceInfo namespace : namespaces.values() ) {
             if (uri.equals(namespace.getURI())) {
                 return ModificationProxy.create( namespace, NamespaceInfo.class );
             }
         }
 
         return null;
     }
 
     public List getNamespaces() {
         ArrayList<NamespaceInfo> ns = new ArrayList<NamespaceInfo>();
         for ( Map.Entry<String,NamespaceInfo> e : namespaces.entrySet() ) {
             if ( e.getKey() == null || e.getKey().equals(DEFAULT)) 
                 continue;
             ns.add( e.getValue() );
         }
         
         return ModificationProxy.createList( ns, NamespaceInfo.class );
     }
 
     public void add(NamespaceInfo namespace) {
         validate(namespace,true);
         resolve(namespace);
         
         synchronized (namespaces) {
             namespaces.put(namespace.getPrefix(),namespace);
             if ( getDefaultNamespace() == null ) {
                 setDefaultNamespace(namespace);
             }
         }
         
         added(namespace);
     }
 
     void validate(NamespaceInfo namespace, boolean isNew) {
         if ( isNull(namespace.getPrefix()) ) {
             throw new NullPointerException( "Namespace prefix must not be null");
         }
         
         if(namespace.getPrefix().equals(DEFAULT)) {
             throw new IllegalArgumentException(DEFAULT + " is a reserved keyword, can't be used as the namespace prefix");
         }
         
         NamespaceInfo existing = getNamespaceByPrefix( namespace.getPrefix() );
         if ( existing != null && !existing.getId().equals( namespace.getId() ) ) {
             throw new IllegalArgumentException( "Namespace with prefix '" + namespace.getPrefix() + "' already exists.");
         }
         
         existing = getNamespaceByURI( namespace.getURI() );
         if ( existing != null && !existing.getId().equals( namespace.getId() ) ) {
             throw new IllegalArgumentException( "Namespace with URI '" + namespace.getURI() + "' already exists.");
         }
     
         if ( isNull(namespace.getURI()) ) {
             throw new NullPointerException( "Namespace uri must not be null");
         }
         
         try {
             new URI(namespace.getURI());
         } catch(Exception e) {
             throw new IllegalArgumentException("Invalid URI syntax for '" + namespace.getURI() 
                     + "' in namespace '" + namespace.getPrefix() + "'");
         }
     }
     
     public void remove(NamespaceInfo namespace) {
         if ( !getResourcesByNamespace(namespace, ResourceInfo.class ).isEmpty() ) {
             throw new IllegalArgumentException( "Unable to delete non-empty namespace.");
         }
 
         NamespaceInfo defaultNamespace = getDefaultNamespace();
         if (namespace.equals(defaultNamespace)) {
             namespaces.remove(null);
             namespaces.remove(DEFAULT);
         }
         
         namespaces.remove(namespace.getPrefix());
         removed(namespace);
     }
 
     public void save(NamespaceInfo namespace) {
         validate(namespace,false);
         
         ModificationProxy h = 
             (ModificationProxy) Proxy.getInvocationHandler(namespace);
         
         NamespaceInfo ns = (NamespaceInfo) h.getProxyObject();
         if ( !namespace.getPrefix().equals( ns.getPrefix() ) ) {
             synchronized (namespaces) {
                 namespaces.remove( ns.getPrefix() );
                 namespaces.put( namespace.getPrefix(), ns );
             }
         }
         
         saved(namespace);
     }
 
     public NamespaceInfo getDefaultNamespace() {
         return namespaces.containsKey(null) ? 
                 ModificationProxy.create(namespaces.get( null ),NamespaceInfo.class) : null;
     }
 
     public void setDefaultNamespace(NamespaceInfo defaultNamespace) {
         NamespaceInfo ns = namespaces.get( defaultNamespace.getPrefix() );
         if ( ns == null ) {
             throw new IllegalArgumentException( "No such namespace: '" + defaultNamespace.getPrefix() + "'" );
         }
         
         NamespaceInfo old = namespaces.get(null);
         namespaces.put( null, ns );
         namespaces.put( DEFAULT, ns );
         
         //fire change event
         fireModified(this, 
             Arrays.asList("defaultNamespace"), Arrays.asList(old), Arrays.asList(defaultNamespace));
         
     }
 
     // Workspace methods
     public void add(WorkspaceInfo workspace) {
         validate(workspace,true);
         
         if ( workspaces.containsKey( workspace.getName() ) ) {
             throw new IllegalArgumentException( "Workspace with name '" + workspace.getName() + "' already exists.");
         }
         
         resolve(workspace);
         synchronized (workspaces) {
             workspaces.put( workspace.getName(), workspace );
             // if there is no default workspace use this one as the default
             if ( workspaces.get( null ) == null ) {
                 setDefaultWorkspace(workspace);
             }
         }
         
         added( workspace );
     }
     
     void validate(WorkspaceInfo workspace, boolean isNew) {
         if ( isNull(workspace.getName()) ) {
             throw new NullPointerException( "workspace name must not be null");
         }
         
         if(workspace.getName().equals(DEFAULT)) {
             throw new IllegalArgumentException(DEFAULT + " is a reserved keyword, can't be used as the workspace name");
         }
         
         WorkspaceInfo existing = getWorkspaceByName( workspace.getName() );
         if ( existing != null && !existing.getId().equals( workspace.getId() ) ) {
             throw new IllegalArgumentException( "Workspace named '"+ workspace.getName() +"' already exists.");
         }
         
     }
     
     public void remove(WorkspaceInfo workspace) {
         //JD: maintain the link between namespace and workspace, remove this when this is no 
         // longer necessary
         if ( getNamespaceByPrefix( workspace.getName() ) != null ) {
             throw new IllegalArgumentException ( "Cannot delete workspace with linked namespace");
         }
         if ( !getStoresByWorkspace( workspace, StoreInfo.class).isEmpty() ) {
             throw new IllegalArgumentException( "Cannot delete non-empty workspace.");
         }
         
         workspaces.remove( workspace.getName() );
         
         WorkspaceInfo defaultWorkspace = getDefaultWorkspace();
         if (workspace.equals(defaultWorkspace)) {
             workspaces.remove(null);
             workspaces.remove(DEFAULT);
             
             //default removed, choose another workspace to become default
             if (!workspaces.isEmpty()) {
                 setDefaultWorkspace(workspaces.values().iterator().next());
             }
         }
         
         
         removed( workspace );
     }
     
     public void save(WorkspaceInfo workspace) {
         validate(workspace,false);
         
         ModificationProxy h = 
             (ModificationProxy) Proxy.getInvocationHandler(workspace);
         
         WorkspaceInfo ws = (WorkspaceInfo) h.getProxyObject();
         if ( !workspace.getName().equals( ws.getName() ) ) {
             synchronized (workspaces) {
                 workspaces.remove( ws.getName() );
                 workspaces.put( workspace.getName(), ws );
             }
         }
         
         saved(workspace);
     }
     
     public WorkspaceInfo getDefaultWorkspace() {
         return workspaces.containsKey( null ) ? 
                 ModificationProxy.create( workspaces.get( null ), WorkspaceInfo.class ) : null;
     }
     
     public void setDefaultWorkspace(WorkspaceInfo workspace) {
         WorkspaceInfo old = workspaces.get(null);
         workspaces.put( null, workspace );
         workspaces.put( "default", workspace );
         
         //fire change event
         fireModified(this, 
             Arrays.asList("defaultWorkspace"), Arrays.asList(old), Arrays.asList(workspace));
     }
     
     public List<WorkspaceInfo> getWorkspaces() {
         ArrayList<WorkspaceInfo> ws = new ArrayList<WorkspaceInfo>();
         
         //strip out default namespace
         for ( Map.Entry<String, WorkspaceInfo> e : workspaces.entrySet() ) {
             if ( e.getKey() == null || e.getKey().equals(DEFAULT) ) {
                 continue;
             }
             
             ws.add( e.getValue() );
         }
         
         return ModificationProxy.createList( ws, WorkspaceInfo.class );
     }
     
     public WorkspaceInfo getWorkspace(String id) {
         for ( WorkspaceInfo ws : workspaces.values() ) {
             if ( id.equals( ws.getId() ) ) {
                 return ModificationProxy.create(ws,WorkspaceInfo.class);
             }
         }
         
         return null;
     }
     
     public WorkspaceInfo getWorkspaceByName(String name) {
         return workspaces.containsKey(name) ? 
                 ModificationProxy.create( workspaces.get( name ), WorkspaceInfo.class ) : null;
     }
     
     // Style methods
     public StyleInfo getStyle(String id) {
         for (Iterator s = styles.iterator(); s.hasNext();) {
             StyleInfo style = (StyleInfo) s.next();
             if (id.equals(style.getId())) {
                 return ModificationProxy.create(style,StyleInfo.class);
             }
         }
 
         return null;
     }
 
     public StyleInfo getStyleByName(String name) {
         for (Iterator s = styles.iterator(); s.hasNext();) {
             StyleInfo style = (StyleInfo) s.next();
             if (name.equals(style.getName())) {
                 return ModificationProxy.create(style,StyleInfo.class);
             }
         }
 
         return null;
     }
 
     public List getStyles() {
         return ModificationProxy.createList(styles,StyleInfo.class);
     }
 
     public void add(StyleInfo style) {
         validate(style,true);
         resolve(style);
         styles.add(style);
         added(style);
     }
 
     void validate( StyleInfo style, boolean isNew ) {
         if ( isNull(style.getName()) ) {
             throw new NullPointerException( "Style name must not be null");
         }
         if ( isNull(style.getFilename()) ) {
             throw new NullPointerException( "Style fileName must not be null");
         }
         
         StyleInfo existing = getStyleByName( style.getName() );
         if ( existing != null && !existing.getId().equals( style.getId() )) {
             throw new IllegalArgumentException( "Style named '" +  style.getName() +"' already exists.");
         }
     }
     
     public void remove(StyleInfo style) {
         //ensure no references to the style
         for ( LayerInfo l : layers ) {
             if ( style.equals( l.getDefaultStyle() ) || l.getStyles().contains( style )) {
                 throw new IllegalArgumentException( "Unable to delete style referenced by '"+ l.getName()+"'");
             }
         }
         styles.remove(unwrap(style));
         removed(style);
     }
 
     public void save(StyleInfo style) {
         validate(style,false);
         saved( style );
     }
 
     // Event methods
     public Collection getListeners() {
         return Collections.unmodifiableCollection(listeners);
     }
 
     public void addListener(CatalogListener listener) {
         listeners.add(listener);
 
     }
 
     public void removeListener(CatalogListener listener) {
         listeners.remove(listener);
     }
 
     public Iterator search(String cql) {
         // TODO Auto-generated method stub
         return null;
     }
 
     public ResourcePool getResourcePool() {
         return resourcePool;
     }
     
     public void setResourcePool(ResourcePool resourcePool) {
         this.resourcePool = resourcePool;
     }
     
     public GeoServerResourceLoader getResourceLoader() {
         return resourceLoader;
     }
     public void setResourceLoader(GeoServerResourceLoader resourceLoader) {
         this.resourceLoader = resourceLoader;
     }
     public void dispose() {
         if ( stores != null ) stores.clear();
         if ( defaultStores != null ) defaultStores.clear();
         if ( resources != null ) resources.clear();
         if ( namespaces != null ) namespaces.clear();
         if ( workspaces != null ) workspaces.clear();
         if ( layers != null ) layers.clear();
         if ( layerGroups != null ) layerGroups.clear();
         if ( maps != null ) maps.clear();
         if ( styles != null ) styles.clear();
         if ( listeners != null ) listeners.clear();
         
         if ( resourcePool != null ) resourcePool.dispose();
     }
     
     List lookup(Class clazz, MultiHashMap map) {
         ArrayList result = new ArrayList();
         for (Iterator k = map.keySet().iterator(); k.hasNext();) {
             Class key = (Class) k.next();
             if (clazz.isAssignableFrom(key)) {
                 result.addAll(map.getCollection(key));
             }
         }
 
         return result;
     }
 
     protected void added(CatalogInfo object) {
         fireAdded( object );
     }
     
     protected void fireAdded(CatalogInfo object) {
         CatalogAddEventImpl event = new CatalogAddEventImpl();
         event.setSource(object);
 
         event(event);
     }
 
     protected void saved(CatalogInfo object) {
         //this object is a proxy
         ModificationProxy h = 
             (ModificationProxy) Proxy.getInvocationHandler(object);
         
         //get the real object
         CatalogInfo real = (CatalogInfo) h.getProxyObject();
         
         //fire out what changed
         List propertyNames = h.getPropertyNames();
         List newValues = h.getNewValues();
         List oldValues = h.getOldValues();
         
         //TODO: protect this original object, perhaps with another proxy 
         fireModified( real, propertyNames, oldValues, newValues );
         
         //commit to the original object
         h.commit();    
         
         //resolve to do a sync on the object
         //syncIdWithName(real);
         
         //fire the post modify event
         firePostModified( real );
     }
     
     protected void fireModified(CatalogInfo object, List propertyNames, List oldValues,
             List newValues) {
         CatalogModifyEventImpl event = new CatalogModifyEventImpl();
 
         event.setSource(object);
         event.setPropertyNames(propertyNames);
         event.setOldValues(oldValues);
         event.setNewValues(newValues);
 
         event(event);
     }
 
     protected void firePostModified(CatalogInfo object) {
         CatalogPostModifyEventImpl event = new CatalogPostModifyEventImpl();
         event.setSource( object);
         
         event(event);
     }
     protected void removed(CatalogInfo object) {
         CatalogRemoveEventImpl event = new CatalogRemoveEventImpl();
         event.setSource(object);
 
         event(event);
     }
 
     protected void event(CatalogEvent event) {
         CatalogException toThrow = null;
         
         for (Iterator l = listeners.iterator(); l.hasNext();) {
             try {
                 CatalogListener listener = (CatalogListener) l.next();
                 if (event instanceof CatalogAddEvent) {
                     listener.handleAddEvent((CatalogAddEvent) event);
                 } else if (event instanceof CatalogRemoveEvent) {
                     listener.handleRemoveEvent((CatalogRemoveEvent) event);
                 } else if (event instanceof CatalogModifyEvent) {
                     listener.handleModifyEvent((CatalogModifyEvent) event);
                 } else if (event instanceof CatalogPostModifyEvent) {
                     listener.handlePostModifyEvent((CatalogPostModifyEvent)event);
                 }
             } catch(Throwable t) {
                 if ( t instanceof CatalogException && toThrow == null) {
                     toThrow = (CatalogException) t;
                 }
                 else {
                     LOGGER.log(Level.WARNING, "Catalog listener threw exception handling event.", t);
                 }
             }
         }
         
         if (toThrow != null) {
             throw toThrow;
         }
     }
     
     /**
      * Implementation method for resolving all {@link ResolvingProxy} instances.
      */
     public void resolve() {
         //JD creation checks are done here b/c when xstream depersists 
         // some members may be left null
         
         //workspaces
         if ( workspaces == null ) {
             workspaces = new HashMap<String, WorkspaceInfo>();
         }
         for ( WorkspaceInfo ws : workspaces.values() ) {
             resolve(ws);
         }
         
         //namespaces
         if ( namespaces == null ) {
             namespaces = new HashMap<String, NamespaceInfo>();
         }
         for ( NamespaceInfo ns : namespaces.values() ) {
             resolve(ns);
         }
         
         //stores
         if ( stores == null ) {
             stores = new MultiHashMap();
         }
         for ( Object o : stores.values() ) {
             resolve((StoreInfoImpl)o);
         }
         
         //styles
         if ( styles == null ) {
             styles = new ArrayList<StyleInfo>();
         }
         for ( StyleInfo s : styles ) {
             resolve(s);
         }
         
         //resources
         if ( resources == null ) {
             resources = new MultiHashMap();    
         }
         for( Object o : resources.values() ) {
             resolve((ResourceInfo)o);
         }
         
         //layers
         if ( layers == null ) {
             layers = new ArrayList<LayerInfo>();    
         }
         for ( LayerInfo l : layers ) { 
             resolve(l);
         }
         
         //layer groups
         if ( layerGroups == null ) {
             layerGroups = new ArrayList<LayerGroupInfo>();    
         }
         for ( LayerGroupInfo lg : layerGroups ) {
             resolve(lg);
         }
         
         //maps
         if ( maps == null ) {
             maps = new ArrayList<MapInfo>();
         }
         for ( MapInfo m : maps ) {
             resolve(m);
         }
         
         if ( listeners == null ) {
             listeners = new ArrayList<CatalogListener>();
         }
         
         if ( resourcePool == null ) {
             resourcePool = new ResourcePool(this);
         }
     }
     
     protected void resolve(WorkspaceInfo workspace) {
         setId(workspace);
         resolveCollections(workspace);
     }
     
     protected void resolve(NamespaceInfo namespace) {
         setId(namespace);
         resolveCollections(namespace);
     }
     
     protected void resolve(StoreInfo store) {
         setId(store);
         StoreInfoImpl s = (StoreInfoImpl) store;
         
         //resolve the workspace
         WorkspaceInfo resolved = ResolvingProxy.resolve( this, s.getWorkspace());
         if ( resolved != null ) {
             s.setWorkspace(  resolved );    
         }
         else {
             //this means the workspace has not yet been added to the catalog, keep the proxy around
         }
         resolveCollections(s);
         
         s.setCatalog( this );
     }
 
     protected void resolve(ResourceInfo resource) {
         setId(resource);
         ResourceInfoImpl r = (ResourceInfoImpl) resource;
         
         //resolve the store
         StoreInfo resolved = ResolvingProxy.resolve( this, r.getStore() );
         if ( resolved != null ) {
             r.setStore( resolved );
         }
         
         if ( resource instanceof FeatureTypeInfo ) {
             resolve( (FeatureTypeInfo) resource );
         }
         if(r instanceof CoverageInfo){
             resolve((CoverageInfo) resource);
         }
        if(r instanceof WMSLayerInfo){
            resolve((WMSLayerInfo) resource);
        }
         r.setCatalog(this);
     }
 
     private void resolve(CoverageInfo r) {
         CoverageInfoImpl c = (CoverageInfoImpl)r;
         if(c.getDimensions() == null) {
             c.setDimensions(new ArrayList<CoverageDimensionInfo>());
         } else {
             for (CoverageDimensionInfo dim : c.getDimensions()) {
                 if(dim.getNullValues() == null)
                     ((CoverageDimensionImpl) dim).setNullValues(new ArrayList<Double>());
             }
         }
         resolveCollections(r);
     }
     
     /**
      * We don't want the world to be able and call this without 
      * going trough {@link #resolve(ResourceInfo)}
      * @param featureType
      */
     private void resolve(FeatureTypeInfo featureType) {
         FeatureTypeInfoImpl ft = (FeatureTypeInfoImpl) featureType;
         resolveCollections(ft);
     }
    
    private void resolve(WMSLayerInfo wmsLayer) {
        WMSLayerInfoImpl impl = (WMSLayerInfoImpl) wmsLayer;
        resolveCollections(impl);
    }
 
     protected void resolve(LayerInfo layer) {
         setId(layer);
         if (layer.getAttribution() == null) {
             layer.setAttribution(getFactory().createAttribution());
         }
         resolveCollections(layer);
     }
     
     protected void resolve(LayerGroupInfo layerGroup) {
         setId(layerGroup);
         resolveCollections(layerGroup);
         LayerGroupInfoImpl lg = (LayerGroupInfoImpl) layerGroup;
         
         for ( int i = 0; i < lg.getLayers().size(); i++ ) {
             LayerInfo l = lg.getLayers().get( i );
             LayerInfo resolved = ResolvingProxy.resolve( this, l );
             lg.getLayers().set( i, resolved );
         }
         
         for ( int i = 0; i < lg.getStyles().size(); i++ ) {
             StyleInfo s = lg.getStyles().get( i );
             if(s != null) {
                 StyleInfo resolved = ResolvingProxy.resolve( this, s );
                 lg.getStyles().set( i, resolved );
             }
         }
         
     }
     
     protected void resolve(StyleInfo style) {
         setId(style);
         ((StyleInfoImpl)style).setCatalog( this );
     }
     
     protected void resolve(MapInfo map) {
         setId(map);
     }
     
     /**
      * Method which reflectively sets all collections when they are null.
      */
     protected void resolveCollections(Object object) {
         ClassProperties properties = OwsUtils.getClassProperties( object.getClass() );
         for ( String property : properties.properties() ) {
             Method g = properties.getter( property, null );
             if ( g == null ) {
                 continue;
             }
             
             Class type = g.getReturnType();
             //only continue if this is a collection or a map
             if (  !(Map.class.isAssignableFrom( type ) || Collection.class.isAssignableFrom( type ) ) ) {
                 continue;
             }
             
             //only continue if there is also a setter as well
             Method s = properties.setter( property, null );
             if ( s == null ) {
                 continue;
             }
             
             //if the getter returns null, call the setter
             try {
                 Object value = g.invoke( object, null );
                 if ( value == null ) {
                     if ( Map.class.isAssignableFrom( type ) ) {
                         if ( MetadataMap.class.isAssignableFrom( type ) ) {
                             value = new MetadataMap();
                         }
                         else {
                             value = new HashMap();
                         }
                     }
                     else if ( List.class.isAssignableFrom( type ) ) {
                         value = new ArrayList();
                     }
                     else if ( Set.class.isAssignableFrom( type ) ) {
                         value = new HashSet();
                     }
                     else {
                         throw new RuntimeException( "Unknown collection type:" + type.getName() );
                     }
                   
                     //initialize
                     s.invoke( object, value );
                 }
             } 
             catch (Exception e) {
                 throw new RuntimeException( e );
             }
         }
     }
     
     protected void setId( Object o ) {
         if ( OwsUtils.get( o, "id") == null ) {
             String uid = new UID().toString();
             OwsUtils.set( o, "id", o.getClass().getSimpleName() + "-"+uid );
         }
     }
     
     protected boolean isNull( String string ) {
         return string == null || "".equals( string.trim() );
     }
     
     public void sync( CatalogImpl other ) {
         stores = other.stores;
         defaultStores = other.defaultStores;
         resources = other.resources;
         namespaces = other.namespaces;
         workspaces = other.workspaces;
         layers = other.layers;
         maps = other.maps;
         layerGroups = other.layerGroups;
         styles = other.styles;
         listeners = other.listeners;
         
         if ( resourcePool != other.resourcePool ) {
             resourcePool.dispose();
             resourcePool = other.resourcePool;
         }
         
         resourceLoader = other.resourceLoader;
     }
     
     public static <T> T unwrap(T obj) {
         return ModificationProxy.unwrap(obj);
     }
     
     public void accept(CatalogVisitor visitor) {
         visitor.visit(this);
     }
     
 }
