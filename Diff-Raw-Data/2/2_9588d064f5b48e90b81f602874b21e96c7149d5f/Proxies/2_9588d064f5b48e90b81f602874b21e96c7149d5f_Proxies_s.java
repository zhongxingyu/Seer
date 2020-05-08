 package org.genedb.crawl.dao.proxy;
 
 import java.io.IOException;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.commons.lang.ArrayUtils;
 import org.apache.log4j.Logger;
 import org.codehaus.jackson.type.JavaType;
 import org.genedb.crawl.CrawlException;
 import org.genedb.crawl.client.CrawlClient;
 import org.springframework.util.Assert;
 import org.springframework.web.context.request.RequestAttributes;
 import org.springframework.web.context.request.RequestContextHolder;
 import org.springframework.web.context.request.ServletRequestAttributes;
 
 public class Proxies {
     
     static Logger logger = Logger.getLogger(Proxies.class);
     private static String[] resources;
 
     public String[] getResources() {
         return resources;
     }
 
     public void setResources(String[] resources) {
         logger.info("setting resources");
         logger.info(resources);
         Proxies.resources = resources;
     }
     
     public static <T extends Object> T proxyRequest(JavaType type) throws CrawlException {
         
         RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
         Assert.isInstanceOf(ServletRequestAttributes.class, attrs);
         ServletRequestAttributes servletAttrs = (ServletRequestAttributes) attrs;
         HttpServletRequest request = servletAttrs.getRequest();
         
         @SuppressWarnings("unchecked")
         Map<String, String[]> parameters = request.getParameterMap();
         
         String[] uriSplit = request.getRequestURI().split("/");
         ArrayUtils.reverse(uriSplit);
         
         String method = uriSplit[0];
         String resource = uriSplit[1];
         
         String dot = ".";
         int dotPos = method.indexOf(dot);
         
         if (dotPos > 0)
             method = method.substring(0, dotPos);
         
         for (String url : resources) {
             
             logger.info(url + " - " + resource + " - " + method);
             
             CrawlClient client = new CrawlClient(url);
             
             try {
                 
                T result = client.request(type, resource, method, parameters);
                 if (result != null) {
                     logger.info(type + " -- " + result.getClass());
                     logger.info("found result, returning");
                     return result;
                 }
                     
             } catch (IOException e) {
                 throw new RuntimeException(e);
             } catch (CrawlException e) {
                 throw (e);
             }
             
         }
         
         return null;
     }
     
 }
