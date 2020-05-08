 package com.bazaarvoice.seo.sdk.util;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URLDecoder;
 import java.nio.MappedByteBuffer;
 import java.nio.channels.FileChannel;
 import java.nio.charset.Charset;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.commons.lang3.StringUtils;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.utils.URLEncodedUtils;
 
 /**
  * Bazaarvoice utility class.
  * 
  * @author Anandan Narayanaswamy
  *
  */
 public final class BVUtilty {
 	
 	private BVUtilty() {
 		
 	}
 	
 	public static String getPageNumber(String queryString) {
         if (queryString != null && queryString.length() > 0) {
             List<NameValuePair> parameters = URLEncodedUtils.parse (queryString, Charset.forName("UTF-8"));
             for(NameValuePair parameter : parameters) {
                 if (parameter.getName().equals("bvrrp") || parameter.getName().equals("bvqap") || parameter.getName().equals("bvsyp")) {
                     final Pattern p = Pattern.compile("^[^/]+/\\w+/\\w+/(\\d+)/[^/]+\\.htm$");
                     return matchPageNumber(p, parameter.getValue());
                 } else if (parameter.getName().equals("bvpage")) {
                     final Pattern p = Pattern.compile("^\\w+/(\\d+)$");
                     return matchPageNumber(p, parameter.getValue());
                 }
             }
         }
         return "1";
     }
 	
 	private static String matchPageNumber(Pattern pattern, String value) {
         Matcher m = pattern.matcher(value);
         if (m.matches()) {
             return m.group(1);
         } else {
             return "1";
         }
     }
 	
 	public static String getQueryString(String uri) {
 		
 		if (StringUtils.isBlank(uri)) {
 			return "";
 		}
 		
         URI _uri = null;
         try {
//            _uri = new URI(URLDecoder.decode(uri, "UTF-8"));
        	_uri = new URI(uri);
         } catch (Exception ex) {
 //            _log.warn("Unable to parse URL: " + uri);
         	ex.printStackTrace();
         }
         
         return _uri.getQuery();
     }
 	
 	public static String readFile(String path) throws IOException {
         FileInputStream stream = new FileInputStream(new File(path));
         try {
             FileChannel fc = stream.getChannel();
             MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
             return Charset.forName("UTF-8").decode(bb).toString();
         }
         finally {
             stream.close();
         }
     }
 	
 	public static String readFile(URI uri) throws IOException {
         FileInputStream stream = new FileInputStream(new File(uri));
         try {
             FileChannel fc = stream.getChannel();
             MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
             return Charset.forName("UTF-8").decode(bb).toString();
         }
         finally {
             stream.close();
         }
     }
 	
 	/**
 	 * Utility method to replace the string from StringBuilder.
 	 * @param sb			StringBuilder object.
 	 * @param toReplace		The String that should be replaced.
 	 * @param replacement	The String that has to be replaced by.
 	 * 
 	 */
 	public static void replaceString(StringBuilder sb, String toReplace, String replacement) {
 		int index = -1;
 		while ((index = sb.lastIndexOf(toReplace)) != -1) {
 			sb.replace(index, index + toReplace.length(), replacement);
 		}
 	}
 	
 	public static String removeBVQuery(String queryUrl) {
 
         final URI uri;
         try {
             uri = new URI(queryUrl);
         } catch (URISyntaxException e) {
             return queryUrl;
         }
 
         try {
             String newQuery = null;
             if (uri.getQuery() != null && uri.getQuery().length() > 0) {
                 List<NameValuePair> newParameters = new ArrayList<NameValuePair>();
                 List<NameValuePair> parameters =  URLEncodedUtils.parse(uri.getQuery(), Charset.forName("UTF-8"));
                 List<String> bvParameters = Arrays.asList("bvrrp", "bvsyp", "bvqap", "bvpage");
                 for (NameValuePair parameter : parameters) {
                     if (!bvParameters.contains(parameter.getName())) {
                         newParameters.add(parameter);
                     }
                 }
                 newQuery = newParameters.size() > 0 ? URLEncodedUtils.format(newParameters, Charset.forName("UTF-8")) : null;
 
             }
             return new URI(uri.getScheme(), uri.getAuthority(), uri.getPath(), newQuery, null).toString();
         } catch (URISyntaxException e) {
             return queryUrl;
         }
     }
 }
