 /*
  * Created on Dec 9, 2004
  */
 package uk.org.ponder.servletutil;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import javax.servlet.ServletContext;
 import javax.servlet.http.HttpServletRequest;
 
 import uk.org.ponder.stringutil.URLUtil;
 import uk.org.ponder.util.UniversalRuntimeException;
 
 /**
  * A collection of primitive utilities for working with Servlets, in particular
  * for inferring various parts of path components. This link is particularly
  * useful in sorting out the various meanings of HttpServletRequest returns: <a
  * href="http://javaalmanac.com/egs/javax.servlet/GetReqUrl.html?l=new">
  * http://javaalmanac.com/egs/javax.servlet/GetReqUrl.html?l=new</a>
  * 
  * @author Antranig Basman (antranig@caret.cam.ac.uk)
  * 
  */
 public class ServletUtil {
    /**
    * The "Base URL" is the full URL of this servlet, ignoring any extra path due
    * to the particular request.
    */
   // TODO: It might well be more reliable to do this by simply knocking
   // off "PathInfo" from RequestURL. Note that RequestURL can be the
   // ORIGINAL url (before a dispatch) and hence not agree with a URL that
   // could be used to invoke the current request.
 //  public static String getBaseURL(HttpServletRequest hsr) {
 //    String requestURL = hsr.getRequestURL().toString();
 //    String requestpath = hsr.getServletPath();
 //    int embedpoint = requestURL.indexOf(requestpath);
 //    if (embedpoint == -1) {
 //      throw new UniversalRuntimeException("Cannot locate request path of "
 //          + requestpath + " within request URL of " + requestURL);
 //    }
 //    String baseURL = requestURL.substring(0, embedpoint + requestpath.length()
 //        + 1);
 //    return baseURL;
 //  }
 
   /**
    * Computes the "Base URL" of this servlet, defined as the complete request
    * path, with any trailing string agreeing with the "PathInfo" removed. With
    * the exception that the trailing slash IS reappended.
    */
   public static String getBaseURL2(HttpServletRequest hsr) {
     String requestURL = hsr.getRequestURL().toString();
    // In some totally unspecced way, javax implementation provides the URL
    // ENCODED, whereas the PathInfo UNENCODED.
    requestURL = URLUtil.decodeURL(requestURL);
     String extrapath = hsr.getPathInfo();
     String togo;
     if (extrapath == null || extrapath.equals("") || extrapath.equals("/")) {
       togo = requestURL;
     }
     else {
       int embedpoint = requestURL.lastIndexOf(extrapath);
       if (embedpoint == -1) {
         throw new UniversalRuntimeException("Cannot locate path info of "
             + extrapath + " within request URL of " + requestURL);
       }
       togo = requestURL.substring(0, embedpoint);
     }
     // I don't trust the Servlet API further than I can throw it.
     if (togo.charAt(togo.length() - 1) != '/') {
       togo += '/';
     }
     return togo;
   }
 
   /*****************************************************************************
    * Computes the "Context Base URL" of this servlet, which will include the
    * extra stub of the path that refers to the mapping for the specific servlet.
    */
   public static String getContextBaseURL2(HttpServletRequest hsr) {
     String baseurl = getBaseURL2(hsr);
     String servletpath = hsr.getServletPath();
     if (servletpath == null || servletpath == "") {
       return baseurl;
     }
     int embedpoint = baseurl.lastIndexOf(servletpath);
     if (embedpoint == -1) {
       throw new UniversalRuntimeException("Cannot locate servlet path of "
           + servletpath + " within base url of of " + baseurl);
     }
     String togo = baseurl.substring(0, embedpoint);
     if (togo.charAt(togo.length() - 1) != '/') {
       togo += '/';
     }
     return togo;
   }
   //  
   // public static String getExtraPath(HttpServletRequest hsr) {
   // String baseURL = getBaseURL(hsr);
   // return hsr.getRequestURL().substring(baseURL.length());
   // }
   public static final String TEST_RESOURCE = "/WEB-INF/web.xml";
 
   /** Returns the context name of this ServletContext, using an algorithm
    * based on using the context's web.xml file as a "test resource". This
    * name will include both leading and trailing slash.
    */
   public static String computeContextName(ServletContext context) {
     try {
       URL weburl = context.getResource(TEST_RESOURCE);
       String weburls = weburl.toExternalForm();
       // plus one to include trailing slash
       int backhack = 1 + weburls.length() - TEST_RESOURCE.length();
       int protpos = weburls.indexOf(":");
       if (protpos == -1 ) {
         throw new MalformedURLException(
             "Could not find protocol in URL " + weburls);
       }
       ++ protpos;
       while (true) {
         if (weburls.charAt(protpos) == '/') ++ protpos;
         else break;
       }
       int endhostpos = weburls.indexOf('/', protpos + 3);
       if (endhostpos == -1) {
         throw new MalformedURLException(
             "Could not find host and protocol in URL " + weburls);
       }
       return weburls.substring(endhostpos, backhack);
     }
     catch (Exception e) {
       throw UniversalRuntimeException.accumulate(e,
           "Error computing context name");
     }
   }
   
 }
