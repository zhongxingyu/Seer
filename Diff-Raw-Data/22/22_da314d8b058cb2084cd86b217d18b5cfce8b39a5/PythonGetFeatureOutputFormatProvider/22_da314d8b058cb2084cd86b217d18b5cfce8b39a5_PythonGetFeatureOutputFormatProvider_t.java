 package org.geoserver.python.format;
 
import org.geoserver.config.GeoServer;
 import org.geoserver.python.Python;
 import org.geoserver.wfs.WFSGetFeatureOutputFormat;
 
 public class PythonGetFeatureOutputFormatProvider extends PythonOutputFormatProvider<WFSGetFeatureOutputFormat> {
 
    GeoServer gs;
    
    public PythonGetFeatureOutputFormatProvider(Python py, GeoServer gs) {
         super(py);
        this.gs = gs;
     }
     
     public Class<WFSGetFeatureOutputFormat> getExtensionPoint() {
         return WFSGetFeatureOutputFormat.class;
     }
     
     @Override
     protected WFSGetFeatureOutputFormat createOutputFormat(PythonFormatAdapter adapter) {
        return new PythonGetFeatureOutputFormat((PythonVectorFormatAdapter) adapter, gs);
     }
 
 }
