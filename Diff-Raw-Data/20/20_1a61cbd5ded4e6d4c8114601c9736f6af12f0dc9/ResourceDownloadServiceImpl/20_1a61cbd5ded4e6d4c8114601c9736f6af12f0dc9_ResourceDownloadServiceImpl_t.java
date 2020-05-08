 /**
  * 
  */
 package org.easysoa.registry.dbb;
 
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.net.URL;
 import java.util.Properties;
 
 import org.easysoa.registry.DocumentService;
 import org.nuxeo.runtime.api.Framework;
 import org.nuxeo.runtime.model.ComponentContext;
 import org.nuxeo.runtime.model.DefaultComponent;
 
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.WebResource;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import org.easysoa.registry.types.ResourceDownloadInfo;
 
 /**
  * Impl of ResourceDownloadService for Nuxeo components.
  * 
  * Makes the global ResourceDownloadService available in Nuxeo,
  * but if not available or timeout at activation, disables it and does instead
  * a local synchronous download (using HttpDownloaderServiceImpl).
  * 
  * Default configuration (change it in nxserver/config/easysoa.properties ) :
  * * ResourceDownloadServiceImpl.delegateResourceDownloadServiceUrl = http://localhost:7080/get
  * 
  * Reuses a single Jersey client (rather than creating one per download which costs too much).
  * 
  * TODO LATER better :
  * test delegate async at start
  * called by ResourceUpdate within async using Nuxeo Work
  * 
  * @author jguillemotte, mdutoo
  *
  */
 public class ResourceDownloadServiceImpl extends DefaultComponent implements ResourceDownloadService {
 
     private String delegateResourceDownloadServiceUrl;
     
     // caches
     private Client client = null;
     private boolean isDelegatedDownloadDisabled = false;
 
     @Override
     public void activate(ComponentContext context) throws Exception {
         super.activate(context);
         
         // Configuration (following ex. SchedulerImpl)
         // TODO LATER maybe rather as an extension point / contribution ??
     	try {
 	    	DocumentService documentService = Framework.getService(DocumentService.class);
 	    	Properties props = documentService.getProperties();
 	    	if (props != null) {
 		    	delegateResourceDownloadServiceUrl = props.getProperty(
 		    			"ResourceDownloadServiceImpl.delegateResourceDownloadServiceUrl", "http://localhost:7080/get");
 	    	}
 		} catch (Exception e) {
 			throw new RuntimeException("Unable to get DocumentService", e);
 		}
 
         // Create reusable Jersey client, rather than creating one per download else costs too much
         // (ex. lots of threads at SignatureParser l. 79 within annotation parsing).
         // TODO LATER better : called by ResourceUpdate within async using Nuxeo Work
 		client = Client.create();
         client.setConnectTimeout(3000); // Set timeout to 3 seconds TODO LATER make it configurable
         
         // TODO LATER start async Work that tests delegated download and sets isDelegatedDownloadDisabled
     }
 
     @Override
     public void deactivate(ComponentContext context) throws Exception {
         client = null;
         super.deactivate(context);
     }
     
     @Override
     public ResourceDownloadInfo get(URL url) throws Exception {
 
         ResourceDownloadInfoImpl resourceDownloadInfo = new ResourceDownloadInfoImpl();
         File file = null;
     	if (!isDelegatedDownloadDisabled) {
 	        // First try : Connect to FraSCAti studio download service (TODO : this service must be exposed as REST service ...)
 	        try {
 	            //return delegatedDownload(url);
                     file = delegatedDownload(url);
 	        }
 	        catch(Exception ex){
 	            // Error or timeout, try the second donwload method
                     isDelegatedDownloadDisabled = true;
 	        }
    	} else {
            // If no response : Use local downloader service                    
            file = localDownload(url);
        }
         
         // Compute timestamp
        if(file != null){
            Date date = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat(ResourceDownloadInfo.TIMESTAMP_DATETIME_PATTERN);
            resourceDownloadInfo.setTimestamp(sdf.format(date));
            resourceDownloadInfo.setFile(file);
        }
         return resourceDownloadInfo;
     }
 
     /**
      * 
      * @param url
      * @return
      * @throws Exception 
      */
     private File delegatedDownload(URL url) throws Exception {
         WebResource webResource = client.resource(delegateResourceDownloadServiceUrl);
         // NB. reuses Jersey client rather than creating one per download else costs too much
         // (ex. lots of threads at SignatureParser l. 79 within annotation parsing)
         // TODO LATER better : called by ResourceUpdate within async using Nuxeo Work
         
         // TODO for frascati service side : Add a parameter to pass the url
         webResource.setProperty("fileURL", url.toURI().toString());
         
         // Get the resource
         File resourceFile = webResource.get(File.class);
         return resourceFile;
     }
 
     /**
      * 
      * @param url
      * @return
      * @throws Exception 
      */
     private File localDownload(URL url) throws Exception{
         HttpDownloaderService httpDownloaderService = new HttpDownloaderServiceImpl();
         HttpDownloader fileDownloader = httpDownloaderService.createHttpDownloader(url);
         fileDownloader.download();
         return fileDownloader.getFile();
     }
 
     /**
      * Read data from File
      * @param file
      * @return 
      */
     @Override
     public String getData(File file) throws Exception {
         String data;
         FileInputStream fis = null;
         try {
             if (file == null) {
                 // ex. if site root url returns something else than 200 (ex. 403)
                 return ""; // else cleaner.clean(siteRootFile) throws NullPointerException 
             }
             fis = new FileInputStream(file);
             StringBuilder dataBuffer = new StringBuilder();
             int c;
             while ((c = fis.read()) != -1) {
                 dataBuffer.append((char) c);
             }
             data = dataBuffer.toString();
         }
         catch (Exception e) {
                 data = null;
         }
         finally {
             if (fis != null) {
                 fis.close();
             }
         }
         return data;
     }
     
     //@Reference
     //protected List<UrlPatternWebResourceDownloadService> downloadServices;
     
     /* (non-Javadoc)
      * @see org.easysoa.registry.rest.integration.resources.RoutingWebResourceDownloadService#get(java.net.URL)
      */
     /*@Override
     @Path("/get/")
     @GET
     public File get(URL url, PreferencesManagerItf preferences) throws Exception {
 
         // - Sort the list, to have the webResourceDownloadServices in the specified order
         // Need to be sorted manually because FraSCAti doesn't provide a native way to specify an execution order
         Collections.sort(downloadServices, new DownloadServicesComparator());
         
         for(UrlPatternWebResourceDownloadService service : downloadServices){
             if(service.matchUrl(url)){
                 // file type is always WSDL ??
                 File localTempFolder = new File(tempFolder + "tempWsdl");
                 if(!localTempFolder.exists()){
                     localTempFolder.mkdir();
                 }
                 File localWsdlFile = new File(tempFolder + "tempWsdl/temp_" + System.currentTimeMillis() + ".wsdl");
                 if (!localWsdlFile.createNewFile()) {
                     throw new IOException("Unable to create local WSDL file at " + localWsdlFile.getAbsolutePath());
                 }
                 // TODO closes ?
                 IOUtils.write(service.get(url), new FileOutputStream(localWsdlFile));
                 return localWsdlFile;
                 //return Response.ok(service.get(url)).type("text/xml").build();
             }
         }
         //return Response.serverError().build();
         return null;
     }
 */
     
 }
