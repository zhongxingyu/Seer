 package de.flower.common.ui.util;
 
 import org.apache.wicket.protocol.http.servlet.ErrorAttributes;
 import org.apache.wicket.protocol.http.servlet.ForwardAttributes;
 import org.apache.wicket.request.Request;
 
import javax.servlet.http.HttpServletRequest;

 /**
  * @author flowerrrr
  */
 public class LoggingUtils {
 
     public static String toString(Request request) {
         try {
             HttpServletRequest servletRequest = (HttpServletRequest) request.getContainerRequest();
 
             ErrorAttributes errorAttributes = ErrorAttributes.of(servletRequest);
             ForwardAttributes forwardAttributes = ForwardAttributes.of(servletRequest);
 
             String s = String.format("url [%s], referer [%s]",
                     request.getUrl(),
                     servletRequest.getHeader("referer"));
             if (errorAttributes != null) {
                 s += ", " + errorAttributes.toString();
             }
             if (forwardAttributes != null) {
                 s += ", " + forwardAttributes.toString();
             }
             return s;
         } catch (RuntimeException e) {
             return e.toString();
         }
 
     }
 
 
 }
