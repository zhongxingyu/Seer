 package gov.nih.nci.gss.util;
 
import gov.nih.nci.gss.domain.DataServiceGroup;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ResponseHandler;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.BasicResponseHandler;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.protocol.HTTP;
 import org.apache.log4j.Logger;
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 /**
 * Interface to caB2B via a JSON API. 
  * 
  * @author <a href="mailto:rokickik@mail.nih.gov">Konrad Rokicki</a>
  */
 public class Cab2bAPI {
     
	private static Logger log = Logger.getLogger(Cab2bAPI.class);
 
 	private Cab2bTranslator cab2bTranslator;
     
     public Cab2bAPI(Cab2bTranslator cab2bTranslator) {
     	this.cab2bTranslator = cab2bTranslator;
     }
     
     public class Cab2bService {
    
         private String url;
         private String modelGroupName;
         private boolean searchDefault;
         
         public Cab2bService(String url, String modelGroupName,
                 boolean searchDefault) {
             this.url = url;
             this.modelGroupName = modelGroupName;
             this.searchDefault = searchDefault;
         }
 
         public String getUrl() {
             return url;
         }
         
         public String getModelGroupName() {
             return modelGroupName;
         }
         
         public boolean isSearchDefault() {
             return searchDefault;
         }
     }
 	
 	/**
 	 * Queries caB2B and returns a mapping of service URLs to Cab2bServices.
 	 * @return
 	 * @throws Exception
 	 */
     public Map<String,Cab2bService> getServices() throws Exception {
     	
         Map<String,Cab2bService> services = new HashMap<String,Cab2bService>();
     	DefaultHttpClient httpclient = new DefaultHttpClient();
         
         try {
             String queryURL = GSSProperties.getCab2b2QueryURL()+"/services";
             HttpGet httpget = new HttpGet(queryURL);
             ResponseHandler<String> responseHandler = new BasicResponseHandler();
             String result = httpclient.execute(httpget, responseHandler);
             JSONObject json = new JSONObject(result);
             
         	for(Iterator i = json.keys(); i.hasNext(); ) {
         		String modelGroupName = (String)i.next();
         		JSONArray urls = json.getJSONArray(modelGroupName);
         		for(int k=0; k<urls.length(); k++) {
         		    JSONObject jsonService = urls.getJSONObject(k);
         		    
         		    String serviceURL = jsonService.getString("url");
         		    
         		    boolean searchDefault = false;
        		    if (jsonService.has("searchDefault")) {
        		        searchDefault = "true".equals(jsonService.getString("searchDefault"));
         		    }
         		    
         		    Cab2bService service = new Cab2bService(
         		        serviceURL, modelGroupName, searchDefault);
         		    
         		    services.put(serviceURL, service);
         		}
         	}
         }
         finally {
         	httpclient.getConnectionManager().shutdown();
         }
        
		log.info("Retrieved " + services.size() + " services from caB2B");
         return services;
     }
 
     /**
      * Query caB2B for a keyword search on a particular service or caB2B modelGroup.
      * @param searchString keyword search string
      * @param serviceGroup service group (will get translated to caB2B model group)
      * @param serviceUrl the URL to search
      * @return
      * @throws Exception
      */
     public String search(String searchString, String serviceGroup, List<String> serviceUrls) throws Exception {
 
     	DefaultHttpClient httpclient = new DefaultHttpClient();
    	DataServiceGroup group = cab2bTranslator.getServiceGroupByName(serviceGroup);
     	
         try {
             String url = GSSProperties.getCab2b2QueryURL()+"/search/";
             HttpPost httppost = new HttpPost(url);
 
             List <NameValuePair> nvps = new ArrayList <NameValuePair>();
             nvps.add(new BasicNameValuePair("searchString", searchString));
            nvps.add(new BasicNameValuePair("modelGroup", group.getCab2bName()));
             for(String serviceUrl : serviceUrls) {
                 nvps.add(new BasicNameValuePair("serviceUrl", serviceUrl));
             }
             
             httppost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
             
             ResponseHandler<String> responseHandler = new BasicResponseHandler();
             return httpclient.execute(httppost, responseHandler);
         }
         finally {
         	httpclient.getConnectionManager().shutdown();
         }
     }
     
 }
