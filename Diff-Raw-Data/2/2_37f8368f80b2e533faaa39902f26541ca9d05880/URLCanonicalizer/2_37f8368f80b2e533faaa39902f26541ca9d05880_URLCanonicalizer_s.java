 package newsrack.util;
 
 // This class takes urls of news stories so that we can more easily
 // recognize identical urls.  For now, this class hardcodes rules
 // for a few sites.  In future, this information should come from
 // an external file that specifies match-rewrite rules for urls
 public class URLCanonicalizer
 {
 	public static String cleanup(String baseUrl, String url)
 	{
 			// get rid of all white space!
 		url = url.replaceAll("\\s+", "");
 			// Is this allowed in the spec??? Some feeds (like timesnow) uses relative URLs!
 		if (url.startsWith("/"))
 			url = baseUrl + url;
 
 		return url;
 	}
 
 	public static String canonicalize(String url)
 	{
		if (url.indexOf("google.com/") != -1) {
 			int proxyUrlStart = url.lastIndexOf("http://");
 			if (proxyUrlStart != -1) {
 				url = url.substring(proxyUrlStart);
 				url = url.substring(0, url.lastIndexOf("&cid="));
 			}
 		}
 		// Do not use 'else if'
 		if (!url.startsWith("http://uni.medhas.org")) {
 			url = url.substring(url.lastIndexOf("http://"));
 		}
 		// Do not use 'else if'
 		if (url.indexOf("nytimes.com/") != -1) {
          int qi = url.indexOf('?');
          if (qi != -1)
             url = url.substring(0, qi);
       }
 
 		return url;
 	}
 }
