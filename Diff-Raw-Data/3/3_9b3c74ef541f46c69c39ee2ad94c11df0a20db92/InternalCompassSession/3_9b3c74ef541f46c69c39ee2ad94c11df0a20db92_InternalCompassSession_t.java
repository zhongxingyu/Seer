 package org.compass.core.spi;
 
 import org.compass.core.CompassException;
 import org.compass.core.CompassSession;
 import org.compass.core.Resource;
 import org.compass.core.cache.first.FirstLevelCache;
 import org.compass.core.engine.SearchEngine;
 import org.compass.core.mapping.CompassMapping;
import org.compass.core.marshall.MarshallingContext;
 import org.compass.core.marshall.MarshallingStrategy;
 import org.compass.core.metadata.CompassMetaData;
 
 import at.molindo.elastic.compass.CompassAdapted;
 
 @CompassAdapted
 public interface InternalCompassSession extends CompassSession {
 
 	InternalCompass getCompass();
 
 	FirstLevelCache getFirstLevelCache();
 
 	CompassMapping getMapping();
 	
     CompassMetaData getMetaData();
 	
 	SearchEngine getSearchEngine();
 
 	MarshallingStrategy getMarshallingStrategy();
 	
 	boolean isReadOnly();
 
 	boolean isClosed();
 
 	void delete(Object value, DirtyOperationContext context);
 
 	void create(Object value, DirtyOperationContext context);
 
 	void save(Object value, DirtyOperationContext context);
 
 	Object getByResource(Resource resource) throws CompassException;
 
 	void addDelegateClose(InternalSessionDelegateClose delegateClose);
 	
     Resource getResourceByIdResource(Resource idResource) throws CompassException;
 
     Resource getResourceByIdResourceNoCache(Resource idResource) throws CompassException;
 
    Object get(String alias, Object id, MarshallingContext context) throws CompassException;
 }
