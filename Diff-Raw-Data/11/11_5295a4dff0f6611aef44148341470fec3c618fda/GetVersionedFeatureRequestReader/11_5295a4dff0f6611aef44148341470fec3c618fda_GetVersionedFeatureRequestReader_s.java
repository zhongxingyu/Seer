 package org.geoserver.wfsv.kvp;
 
 import net.opengis.wfs.WfsFactory;
 import net.opengis.wfsv.GetVersionedFeatureType;
 import net.opengis.wfsv.WfsvFactory;
 
 import org.geoserver.wfs.WebFeatureService;
 import org.geoserver.wfs.kvp.GetFeatureKvpRequestReader;
 import org.opengis.filter.FilterFactory;
 import org.vfny.geoserver.global.Data;
 
 public class GetVersionedFeatureRequestReader extends GetFeatureKvpRequestReader {
 
     public GetVersionedFeatureRequestReader(Data catalog,
            FilterFactory filterFactory, WebFeatureService wfs) {
        super(GetVersionedFeatureType.class, catalog, filterFactory, wfs);
         factory = WfsvFactory.eINSTANCE;
     }
     
     protected WfsFactory getWfsFactory() {
         return WfsFactory.eINSTANCE;
     }
 
 }
