 package org.etp.portalKit.test.controller;
 
 import java.io.IOException;
 
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.HttpException;
 import org.apache.commons.httpclient.HttpMethodBase;
 import org.apache.commons.httpclient.HttpStatus;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.apache.commons.httpclient.methods.PostMethod;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 /**
  * Hold all the request that related to settings.
  */
 @Controller
 public class TestController {
 
     private static final HttpClient httpclient = new HttpClient();
     private static final Log logger = LogFactory.getLog(TestController.class);
 
     private String getDefatltIP() {
         return "127.0.0.1";
     }
 
     private String getDefaultPort() {
         return "8080";
     }
 
     private String getDefaultContextPath() {
         return "portal-testserver-war-3.4.2";
     }
 
     private String getDataRequestURL() {
         return "public/test/data.ajax";
     }
 
     private String getStartTestRequestURL() {
         return "public/test/startTest.ajax?async=ture";
     }
 
     /**
      * @param targetIP
      * @param targetPort
      * @param targetContextPath
      * @return do nothing
     * @throws IOException 
     * @throws HttpException 
      */
     @RequestMapping(value = "/test/data.ajax", method = RequestMethod.GET)
     public @ResponseBody
    String getData(String targetIP, String targetPort, String targetContextPath) throws HttpException, IOException {
         String requestIP = null != targetIP ? targetIP : getDefatltIP();
         String requestPort = null != targetPort ? targetPort : getDefaultPort();
         String requestContextPath = null != targetContextPath ? targetContextPath : getDefaultContextPath();
         String requestURL = String.format("http://%s:%s/%s/%s", requestIP, requestPort, requestContextPath,
                 getDataRequestURL());
        return sendGetRequest(requestURL);
     }
 
     /**
      * @param targetIP
      * @param targetPort
      * @param targetContextPath
      * @return do nothing
     * @throws IOException 
     * @throws HttpException 
      */
     @RequestMapping(value = "/test/start.ajax", method = RequestMethod.GET)
     public @ResponseBody
    String startTest(String targetIP, String targetPort, String targetContextPath) throws HttpException, IOException {
         String requestIP = null != targetIP ? targetIP : getDefatltIP();
         String requestPort = null != targetPort ? targetPort : getDefaultPort();
         String requestContextPath = null != targetContextPath ? targetContextPath : getDefaultContextPath();
         String requestURL = String.format("http://%s:%s/%s/%s", requestIP, requestPort, requestContextPath,
                 getStartTestRequestURL());
        return sendGetRequest(requestURL);
     }
 
     protected String sendGetRequest(String url) throws HttpException, IOException {
         return execMethod(new GetMethod(url));
     }
 
     protected String sendPostRequest(String url) throws HttpException, IOException {
         return execMethod(new PostMethod(url));
     }
 
     protected String execMethod(HttpMethodBase method) throws HttpException, IOException {
         String result = null;
         try {
             int statusCode = httpclient.executeMethod(method);
             if (statusCode != HttpStatus.SC_OK) {
                 throw new HttpException("Failed to get response!");
             }
             result = method.getResponseBodyAsString();
         } catch (HttpException e) {
             throw new IllegalStateException(e.toString());
         } catch (IOException e) {
             throw new IllegalStateException(e.toString());
         } finally {
             method.releaseConnection();
         }
         return result;
     }
 }
