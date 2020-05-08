 package org.geoserver.wfs;
 
 import java.io.IOException;
 import java.util.Iterator;
 
 import net.opengis.wfs.GetGmlObjectType;
 
 import org.geotools.data.DataStore;
 import org.geotools.data.GmlObjectStore;
 import org.geotools.factory.Hints;
 import org.opengis.filter.FilterFactory;
 import org.opengis.filter.identity.GmlObjectId;
 import org.vfny.geoserver.global.Data;
 import org.vfny.geoserver.global.DataStoreInfo;
 
 /**
  * Web Feature Service GetGmlObject operation.
  * 
  * @author Justin Deoliveira, The Open Planning Project, jdeolive@openplans.org
  *
  */
 public class GetGmlObject {
 
     /**
      * wfs config
      */
     WFS wfs;
     
     /**
      * the catalog
      */
     Data catalog;
     
     /**
      * filter factory
      */
     FilterFactory filterFactory;
     
     public GetGmlObject( WFS wfs, Data catalog ) {
         this.wfs = wfs;
         this.catalog = catalog;
     }
     
     public void setFilterFactory(FilterFactory filterFactory) {
         this.filterFactory = filterFactory;
     }
     
     public Object run( GetGmlObjectType request ) throws WFSException {
      
         //get the gml object id
         GmlObjectId id = request.getGmlObjectId();
         
         //set up the hints
         Hints hints = new Hints();
         if ( request.getTraverseXlinkDepth() != null ) {
            Integer depth = 
                GetFeature.traverseXlinkDepth(request.getTraverseXlinkDepth());
            hints.put( Hints.ASSOCIATION_TRAVERSAL_DEPTH , depth); 
         }
         
         //walk through datastores finding one that is gmlobject aware
         for ( Iterator d = catalog.getDataStores().iterator(); d.hasNext(); ) {
             DataStoreInfo dsInfo = (DataStoreInfo) d.next();
             DataStore ds = dsInfo.getDataStore();
             
             if ( ds instanceof GmlObjectStore ) {
                try {
                    Object obj = ((GmlObjectStore) ds).getGmlObject(id, hints);
                    if ( obj != null ) {
                        return obj;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                
             }
         }
         
         throw new WFSException( "No such object: " + id );
     }
 }
