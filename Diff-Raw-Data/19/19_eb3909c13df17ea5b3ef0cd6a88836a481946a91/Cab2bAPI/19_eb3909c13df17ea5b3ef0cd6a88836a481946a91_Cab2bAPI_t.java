 package gov.nih.nci.gss.util;
 
 
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.BasicResponseHandler;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.params.BasicHttpParams;
 import org.apache.http.params.HttpParams;
 import org.apache.http.protocol.HTTP;
 import org.apache.log4j.Logger;
 import org.hibernate.SessionFactory;
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 /**
  * Interface to caB2B.
  * 
  * @author <a href="mailto:rokickik@mail.nih.gov">Konrad Rokicki</a>
  */
 public class Cab2bAPI {
 
     private static Logger log = Logger.getLogger(Cab2bAPI.class);
     
     private static String cab2b2QueryURL = null;
 
     static {
     	try {
 	        InputStream is = null;
 	        Properties properties = new Properties();
 	        
 	        try {
 	            is = Thread.currentThread().getContextClassLoader().getResourceAsStream(
 	                "gss.properties");
 	            properties.load(is);
 	            cab2b2QueryURL = properties.getProperty("cab2b.query.url");
 	            log.info("Configured cab2b2QueryURL="+cab2b2QueryURL);
 	        }
 	        finally {
 	            if (is != null) is.close();
 	        }
     	}
     	catch (Exception e) {
     		log.error("Error initializing caB2B API",e);
     	}
     }
     
     private Cab2bTranslator cab2bTranslator;
     
     public Cab2bAPI(SessionFactory sessionFactory) throws Exception {
         this.cab2bTranslator = new Cab2bTranslator(sessionFactory);
     }
 	
 	/**
 	 * Queries caB2B and returns a mapping service group names to lists of 
 	 * services in each group.
 	 * @return
 	 * @throws Exception
 	 */
     public Map<String,List<String>> getServiceGroups() throws Exception {
     	
     	Map<String,List<String>> groups = new HashMap<String,List<String>>();
     	DefaultHttpClient httpclient = new DefaultHttpClient();
         
         try {
             String url = cab2b2QueryURL+"/services";
             HttpGet httpget = new HttpGet(url);
             ResponseHandler<String> responseHandler = new BasicResponseHandler();
             String result = httpclient.execute(httpget, responseHandler);
             JSONObject json = new JSONObject(result);
             
         	for(Iterator i = json.keys(); i.hasNext(); ) {
         		String modelGroupName = (String)i.next();
             	String serviceGroup = cab2bTranslator.getServiceGroupForModelGroup(modelGroupName);
             	
         		List<String> serviceList = new ArrayList<String>();
         		groups.put(serviceGroup, serviceList);
         		
         		JSONArray urls = json.getJSONArray(modelGroupName);
         		for(int k=0; k<urls.length(); k++) {
         			serviceList.add(urls.getString(k));
         		}
         	}
         }
         finally {
         	httpclient.getConnectionManager().shutdown();
         }
         
         return groups;
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
     	String modelGroup = cab2bTranslator.getModelGroupForServiceGroup(serviceGroup);
     	
         try {
             String url = cab2b2QueryURL+"/search/";
             HttpPost httppost = new HttpPost(url);

            List <NameValuePair> nvps = new ArrayList <NameValuePair>();
            nvps.add(new BasicNameValuePair("searchString", searchString));
            nvps.add(new BasicNameValuePair("modelGroup", modelGroup));
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
 
 	public Cab2bTranslator getCab2bTranslator() {
 		return cab2bTranslator;
 	}
 
 }
