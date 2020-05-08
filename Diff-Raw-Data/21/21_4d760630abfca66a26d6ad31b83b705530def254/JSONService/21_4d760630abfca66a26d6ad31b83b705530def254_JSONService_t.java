 package org.amanzi.awe.catalog.json;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.Serializable;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 
 import net.refractions.udig.catalog.IService;
import net.refractions.udig.catalog.URLUtils;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 
 /**
  * This implementation of IService can resolve either a File or a URL.
  * The CSVServiceExtension will already have filtered for *.csv resources
  * and will construct this class on the URL for that resource. It can then
  * be used to actually access the data in the resource, using the CSV 
  * utility class wrapper on the CSVReader library (separate plug-in).
  * 
  * We support both local files and CSV formated web resources. We expect
  * the CSV files to contain "x" and "y" columns that are then used to
  * place the data on the uDIG map using the current world CRS. It is
  * assumed that the data is in the same projection as the view.
  * 
  * @author craig
  */
 public class JSONService extends IService {
 
 	private Map<String, Serializable> params;
     private URL url;    // original URL created with
     private URL validUrl;   // URL tested for existence (either file or HTTP)
     private Throwable msg;
     private JSONServiceInfo info;
     private Class<Object> type;
 
     @SuppressWarnings("unchecked")
     JSONService( Map<String, Serializable> params ){
         this.params = params;
         url = (URL) params.get(JSONServiceExtension.URL_KEY);
         type = (Class<Object>) params.get(JSONServiceExtension.CLASS_KEY);
     }
 
     public <T> boolean canResolve( Class<T> adaptee ) {
         return (type!=null && adaptee.isAssignableFrom(type)) || super.canResolve(adaptee);
     }
 
     public <T> T resolve( Class<T> adaptee, IProgressMonitor monitor ) throws IOException {
         if (adaptee.isAssignableFrom(File.class)) {
             return adaptee.cast(getValidFile());
         }
         if (adaptee.isAssignableFrom(URL.class)) {
             return adaptee.cast(getValidURL());
         }
         return super.resolve(adaptee, monitor);
     }
 
     @Override
 	public Map<String, Serializable> getConnectionParams() {
 		return params;
 	}
 
 	@Override
 	public JSONServiceInfo getInfo( IProgressMonitor monitor ) throws IOException {
         if (info == null) {
             synchronized (this) {
                 if (info == null) {
                     info = new JSONServiceInfo(this);
                 }
             }
         }       
         return info;
     }
 
 	private List<JSONGeoResource> members;
     /**
      * This method returns a list containing a single CSVGeoResource object which contains the
      * actual data to be displayed. This method is called when the user expands the service
      * node in the catalog to view the resources the service can offer. We could provide a
      * wider range of resources here based on the JSON header, but right now provide only one.
      * 
      * @see org.amanzi.awe.catalog.json.JSONGeoResource
      */
     public List<JSONGeoResource> resources( IProgressMonitor monitor ) throws IOException {
         if (members == null) {
             synchronized (this) {
                 if (members == null) {
                     JSONGeoResource dataHandle = new JSONGeoResource(this);
                     members = Collections.singletonList(dataHandle);
                 }
             }
         }
         return members;
     }
 
     public URL getIdentifier() {
 		return url;
 	}
 
 	public Throwable getMessage() {
 		return msg;
 	}
 
     public Status getStatus() {
         // did an error occur
         if (msg != null)
             return Status.BROKEN;
         // has the file been parsed yet
         if (validUrl == null)
             return Status.NOTCONNECTED;
         return Status.CONNECTED;
     }
 
 	private File getValidFile(){
 	    try {
            return (URLUtils.urlToFile(getValidURL()));
        } catch (Exception e) {
             return null;    // if we get here, the msg should already have been set in getValidURL()
         }
 	}
 
 	public URL getURL(){
 	    return this.url;
 	}
 
 	public URL getValidURL(){
         if (validUrl == null) { //lazy creation
             synchronized (this) { //support concurrent access
                 if (validUrl == null) {
                     try {
                         if(url.getProtocol().equals("file")){
                            File file = URLUtils.urlToFile(url);  // throws exception on invalid file url
                             if( !file.exists() ){
                                 msg = new FileNotFoundException(url.toString());
                             }
                             validUrl = url;
                         }else{
                             url.openConnection();   // throws exception on invalid web url
                             validUrl = url;
                         }
                     } catch (Throwable t) {
                         msg = t;
                     }
                 }
             }
         }
         return validUrl;
     }
 
 }
