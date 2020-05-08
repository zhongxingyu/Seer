 /*
  * Created on Nov 19, 2005
  */
 package uk.org.ponder.rsf.servlet;
 
 import java.io.UnsupportedEncodingException;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.springframework.web.multipart.MultipartHttpServletRequest;
 import org.springframework.web.multipart.MultipartResolver;
 
 import uk.org.ponder.rsf.request.EarlyRequestParser;
 import uk.org.ponder.util.Logger;
 import uk.org.ponder.util.UniversalRuntimeException;
 
 /**
  * <p>
  * Nothing should touch the HttpServletRequest before this request-scope factory
  * bean, which parses primitive information such as the request type and
  * parameter map, as well as resolving multipart uploads.
  */
 
 public class ServletEarlyRequestParser implements EarlyRequestParser {
   protected HttpServletRequest request;
   private MultipartResolver multipartresolver;
   private Map multipartmap = new HashMap();
 
   public void setMultipartResolver(MultipartResolver multipartresolver) {
     this.multipartresolver = multipartresolver;
   }
 
   public void setHttpServletRequest(HttpServletRequest request) {
     this.request = request;
   }
 
   public void init() {
    Logger.log.info("begin parseRequest for " + request.getRequestURI());
     if (multipartresolver.isMultipart(request)) {
       try {
         this.request = multipartresolver.resolveMultipart(request);
         this.multipartmap = ((MultipartHttpServletRequest) request)
             .getFileMap();
       }
       catch (Exception e) {
         throw UniversalRuntimeException.accumulate(e,
             "Error decoding multipart request");
       }
       Logger.log.info("Successfully decoded multipart request");
     }
     // We INSIST that all data passed in and out should be in UTF-8.
     // TODO: Make sure we do not tread on the toes of a POST request in
     // doing this however.
     try {
       request.setCharacterEncoding("UTF-8");
     }
     catch (UnsupportedEncodingException uee) {
       throw UniversalRuntimeException.accumulate(uee,
           "Fatal internal error: UTF-8 encoding not found");
     }
   }
 
   public Map getRequestMap() {
     return request.getParameterMap();
   }
 
   public Map getMultipartMap() {
     return multipartmap;
   }
 
   /**
    * The pathinfo as returned from the request. This INCLUDES an initial slash
    * but no final slash.
    */
   public String getPathInfo() {
     String pathinfo = request.getPathInfo();
     return pathinfo == null ? "/"
         : pathinfo;
   }
 
   /**
    * A factory method for a String encoding the nature of the current request
    * cycle, either ViewParameters.RENDER_REQUEST or
    * ViewParameters.ACTION_REQUEST.
    */
   public String getRequestType() {
     String requesttype = request.getMethod().equals("GET") ? EarlyRequestParser.RENDER_REQUEST
         : EarlyRequestParser.ACTION_REQUEST;
     return requesttype;
   }
 
   public String getEnvironmentType() {
     return EarlyRequestParser.SERVLET_ENVIRONMENT;
   }
 
 }
