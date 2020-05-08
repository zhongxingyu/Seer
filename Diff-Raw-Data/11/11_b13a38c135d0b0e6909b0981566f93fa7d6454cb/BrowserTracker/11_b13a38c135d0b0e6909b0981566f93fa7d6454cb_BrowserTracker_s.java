 package org.jahia.modules.personalization.tracking.trackers;
 
 import nl.bitwalker.useragentutils.UserAgent;
 import org.jahia.modules.personalization.tracking.TrackingData;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  * Browser tracker based on user agent string
  */
 public class BrowserTracker implements TrackerInterface {
     public boolean track(HttpServletRequest request, HttpServletResponse response, TrackingData trackingData) {
         if (request.getHeader("User-Agent") != null) {
             UserAgent userAgent = UserAgent.parseUserAgentString(request.getHeader("User-Agent"));
            trackingData.addValue("browser", userAgent.getBrowser().getName());
            trackingData.addValue("browser-version", userAgent.getBrowserVersion().getVersion());
         }
         return true;
     }
 }
