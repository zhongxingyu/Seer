 /**
  *  This file is part of MythTV for Android
  * 
  *  MythTV for Android is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  MythTV for Android is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with MythTV for Android.  If not, see <http://www.gnu.org/licenses/>.
  *   
  * This software can be found at <https://github.com/MythTV-Android/MythTV-Service-API/>
  *
  */
 package org.mythtv.services.api.connect;
 
 import org.mythtv.services.api.ApiVersion;
 import org.mythtv.services.api.MythServiceApiRuntimeException;
 
 import java.io.IOException;
 import java.io.BufferedInputStream;
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 
 public class MythAccessFactory {
     private static final String MYTHTV_SERVER_HEADER = "Server";
     private static final String MYTHTV_SERVER_MYTHVERSION = "MythTV ";
 	/**
 	 * Get the service interface instance by inferring the mythtv version by providing the service class
 	 * @param serviceClass the service class to use to infer the backend version
 	 * @param baseUri the backend base uri
 	 * @param logLevel the logging level to use
 	 * @return the mythservice api instance or null if no version handling exists
 	 */
 	@SuppressWarnings("unchecked")
 	public static <T> T getServiceTemplateApiByType(Class<T> serviceClass, String baseUri, Level logLevel){
 		if(serviceClass.isAssignableFrom(org.mythtv.services.api.v026.MythServices.class))
 			return (T) new org.mythtv.services.api.v026.MythServicesTemplate(scrubApiUrl(baseUri), logLevel); // v0.26
 		else if(serviceClass.isAssignableFrom(org.mythtv.services.api.v027.MythServices.class))
			return (T) new org.mythtv.services.api.v027.MythServicesTemplate(scrubApiUrl(baseUri), logLevel); // v0.26
 		return null;
 	}
 	
 	/**
 	 * Get the service interface instance by inferring the mythtv version by providing the service class
 	 * @param serviceClass the service class to use to infer the backend version
 	 * @param baseUri the backend base uri
 	 * @return the mythservice api instance or null if no version handling exists
 	 */
 	public static <T> T getServiceTemplateApiByType(Class<T> serviceClass, String baseUri){
 		return getServiceTemplateApiByType(serviceClass, baseUri, Level.INFO);
 	}
 	
 	/**
 	 * Get the service interface instance by using the mythtv version
 	 * @param version the version to use
 	 * @param baseUri the backend base uri
 	 * @return the mythservice api instance or null if no version handling exists
 	 */
 	public static Object getServiceTemplateApiByVersion(ApiVersion version, String baseUri){
 		return getServiceTemplateApiByVersion(version, baseUri, Level.INFO);
 	}
 	
 	/**
 	 * Get the service interface instance by using the mythtv version
 	 * @param version the version to use
 	 * @param baseUri the backend base uri
 	 * @param logLevel the logging level to use
 	 * @return the mythservice api instance or null if no version handling exists
 	 */
 	public static Object getServiceTemplateApiByVersion(ApiVersion version, String baseUri, Level logLevel){
 		if(version != null)
 			switch(version){
 				case v026:
 					return new org.mythtv.services.api.v026.MythServicesTemplate(scrubApiUrl(baseUri), logLevel);
 				case v027:
 					return new org.mythtv.services.api.v027.MythServicesTemplate(scrubApiUrl(baseUri), logLevel);
 				default:
 			}
 		return null;
 	}
 
     /**
      * Gets the api version of the given backend
      * @param baseUri the backend services api url
      * @return the Api version
      */
     public static ApiVersion getMythVersion(String baseUri){
         try {
            URL url = new URL(baseUri);
             HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
             urlConnection.setRequestProperty("User-Agent", "Services API/Get MythTV Version");
             urlConnection.addRequestProperty("Connection", "Close");
             if(urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK){
                 Map<String,List<String>> headerFields = urlConnection.getHeaderFields();
                 String server = urlConnection.getHeaderField(MYTHTV_SERVER_HEADER);
                 byte[] drainResponse = new byte[ 64 ];
                 InputStream bis = new BufferedInputStream( urlConnection.getInputStream() );
                 while( bis.read( drainResponse , 0, 64 ) > 0 );
                 urlConnection.disconnect();
                 if(server != null) {
                     int idx = server.indexOf(MYTHTV_SERVER_MYTHVERSION);
                     if(idx >= 0){
                         idx += MYTHTV_SERVER_MYTHVERSION.length();
                         String version = server.substring(idx);
                         if(version.startsWith("0.26"))
                             return ApiVersion.v026;
                         else if(version.startsWith("0.27"))
                             return ApiVersion.v027;
                     }
                 }
             }
         } catch (Exception e) {
             throw new MythServiceApiRuntimeException(e);
         }
         return ApiVersion.NotSupported;
     }
 
     /**
      * Check if the server is reachable
      * @param baseUrl The url of the server to test
      * @return true if reachable otherwise false.
      */
     public static boolean isServerReachable(String baseUrl){
         boolean isOK = false;
         try {
             final URL url = new URL(baseUrl.toString());
             final HttpURLConnection urlcon = (HttpURLConnection) url.openConnection();
             urlcon.setRequestProperty("User-Agent", "Services API/Is Server Reachable");
             urlcon.addRequestProperty("Connection", "Close");
             isOK = urlcon.getResponseCode() == HttpURLConnection.HTTP_OK;
             byte[] drainResponse = new byte[ 64 ];
             InputStream bis = new BufferedInputStream( urlcon.getInputStream() );
             while( bis.read( drainResponse , 0, 64 ) > 0 );
             urlcon.disconnect();
         } catch (Exception e) {
             throw new MythServiceApiRuntimeException(e);
         }
         return isOK;
     }
 
 	private static String scrubApiUrl(String apiUrlBase) {
 		if(!apiUrlBase.endsWith("/"))
 			return apiUrlBase + "/";
 		else
 			return apiUrlBase;
 	}
 }
 
