 package nz.gen.wellington.guardian.contentapiproxy.utils;
 
 import java.io.BufferedReader;
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import nz.gen.wellington.guardian.contentapiproxy.datasources.contentapi.HttpForbiddenException;
 
 import org.apache.log4j.Logger;
 
 import com.google.appengine.api.urlfetch.HTTPRequest;
 import com.google.appengine.api.urlfetch.HTTPResponse;
 import com.google.appengine.api.urlfetch.URLFetchService;
 import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
 

 public abstract class HttpFetcher {
 
 	private final Logger log = Logger.getLogger(HttpFetcher.class);
 
 	
 	public String fetchContent(String pageURL, String pageCharacterEncoding) throws HttpForbiddenException {
 		try {
			StringBuilder output = new StringBuilder();
 			URL url = new URL(pageURL);
 
             URLFetchService urlFetchService = (URLFetchService) URLFetchServiceFactory.getURLFetchService(); 
             HTTPRequest httpRequest = new HTTPRequest(url); 
             
 			log.info("Http fetching: " + url);
             HTTPResponse result = urlFetchService.fetch(httpRequest);
             if (result.getResponseCode() == HttpURLConnection.HTTP_FORBIDDEN) {
             	final String errorMessage = readResponseBody(pageCharacterEncoding, result);            	
             	throw new HttpForbiddenException(errorMessage);
             }
             
             if (result.getResponseCode() == HttpURLConnection.HTTP_OK) {
             	return readResponseBody(pageCharacterEncoding, result);           	
 
             } else {
             	log.warn("Error response code was: " + result.getResponseCode());
             }
 
 		} catch (MalformedURLException e) {
 			log.warn(e);
 		} catch (IOException e) {
 			log.warn(e);
 		}
 		return null;
 	}
 	
 	
	
 	public byte[] fetchBytes(String url) throws HttpForbiddenException {
 	
 		URLFetchService urlFetchService = (URLFetchService) URLFetchServiceFactory.getURLFetchService(); 
 		HTTPRequest httpRequest;
 		try {
 			httpRequest = new HTTPRequest(new URL(url));
 			HTTPResponse result = urlFetchService.fetch(httpRequest);
 		
 			if (result.getResponseCode() == HttpURLConnection.HTTP_FORBIDDEN) {
 				final String errorMessage = readResponseBody("UTF-8", result);
             	throw new HttpForbiddenException(errorMessage);
             }
           
 			if (result.getResponseCode() == HttpURLConnection.HTTP_OK) {
 				return result.getContent();
 
 			} else {
 				log.warn("Error response code was: " + result.getResponseCode());
 			}
           
 		} catch (MalformedURLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} 
 		return null;          
 	}
 
 
 	private String readResponseBody(String pageCharacterEncoding, HTTPResponse result) throws UnsupportedEncodingException, IOException {		
 		StringBuilder output = new StringBuilder();
 		
 		InputStream inputStream = new ByteArrayInputStream(result.getContent());
 		InputStreamReader input = new InputStreamReader(inputStream, pageCharacterEncoding);
 		BufferedReader reader = new BufferedReader(input);
 		
 		String line = "";
 		while ((line = reader.readLine()) != null) {
 			output.append(line);
 		}
 		reader.close();
 		return output.toString();
 	}
 
 }
