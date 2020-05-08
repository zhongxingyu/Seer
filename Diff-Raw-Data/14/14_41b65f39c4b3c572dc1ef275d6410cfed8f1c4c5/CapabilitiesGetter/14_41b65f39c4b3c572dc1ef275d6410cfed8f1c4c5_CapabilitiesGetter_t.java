 package controllers;
 
 import play.Logger;
import play.Play;
 import play.libs.WS;
 import play.mvc.Controller;
 
 import java.io.UnsupportedEncodingException;
 import java.net.URLDecoder;
 
 public class CapabilitiesGetter extends Controller{
 
     public static void getCapabilities(String serviceUrl) {
         try {
             renderXml(WS.url(buildQueryString(decode(serviceUrl))).get().getString());
         } catch (UnsupportedEncodingException e) {
             Logger.info("CapabilitiesGetter::getCapabilities: Badly encoded URL:" + serviceUrl);
         }
     }
 
     private static String buildQueryString(String serviceUrl) {
         if (serviceUrl.endsWith("?")) {
             serviceUrl = serviceUrl.substring(0, serviceUrl.length()-1);
         }
         return serviceUrl + "?service=WMS&request=getCapabilities";
     }
 
     private static String decode(String utf8Encoded) throws UnsupportedEncodingException {
        return URLDecoder.decode(utf8Encoded, "UTF-8").replace(slashReplacement(), "/");
    }

    private static String slashReplacement() {
        return Play.configuration.getProperty("url.slash_replacement");
     }
 }
