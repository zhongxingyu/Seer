 package org.jahia.modules.personalization.tracking.trackers;
 
 import org.jahia.modules.personalization.tracking.TrackingData;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 
 /**
  * A tracker that tracks HTTP POST activity in live mode, to be able to limit spamming using automated software.
  */
 public class SpamPostLimiterTracker implements TrackerInterface {
 
     private static Logger logger = LoggerFactory.getLogger(SpamPostLimiterTracker.class);
 
     private double lastPostLoadAverageLimit = Double.MAX_VALUE;
     private boolean limitPreviewMode = false;
     private boolean logoutSpammingUser = true;
     private boolean lockSpammingUserAccount = true;
     private String spamNotificationEmail = null;
 
     public void setLastPostLoadAverageLimit(double lastPostLoadAverageLimit) {
         this.lastPostLoadAverageLimit = lastPostLoadAverageLimit;
     }
 
     public void setLimitPreviewMode(boolean limitPreviewMode) {
         this.limitPreviewMode = limitPreviewMode;
     }
 
     public boolean track(HttpServletRequest request, HttpServletResponse response, TrackingData trackingData) {
 
         Boolean alreadyCalculated = (Boolean) request.getAttribute("alreadyCalculatedPostAverage");
         if (alreadyCalculated != null && alreadyCalculated.booleanValue()) {
             return true;
         }
 
         String urlPrefix = "/cms/render/live";
         if (limitPreviewMode) {
             urlPrefix = "/cms/render";
         }
 
         if (request.getMethod().equalsIgnoreCase("post") && request.getRequestURI().startsWith(urlPrefix)) {
             Long lastPostMethodTime = trackingData.getLong("lastPostMethodTime");
             Double lastPostLoadAverage = trackingData.getDouble("lastPostLoadAverage");
             long now = System.currentTimeMillis();
             if (lastPostMethodTime != null) {
                 // let's check for spam activity
                 long elapsedTime = now - lastPostMethodTime;
                 double elapsedTimeInSeconds = elapsedTime / 1000.0;
                 double timeInMinutes = 1;
                 double calcFreqDouble = 5.0;
                 if (lastPostLoadAverage == null) {
                     lastPostLoadAverage = 0.0;
                 }
                 lastPostLoadAverage = lastPostLoadAverage * Math.exp(-calcFreqDouble / (60.0 * timeInMinutes)) + (5 / (elapsedTimeInSeconds)) * (1 - Math.exp(-calcFreqDouble / (60.0 * timeInMinutes)));
                 trackingData.setDouble("lastPostLoadAverage", lastPostLoadAverage);
 
                 if (lastPostLoadAverage > lastPostLoadAverageLimit) {
                     // we have detected possible spam activity
                     logger.warn("Possible spam posting detected by user " + trackingData.getAssociatedUserKey() + " from ip " + request.getRemoteAddr() + " with session " + request.getSession().getId());
                     try {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Too many submissions from the same user in a short amount of time, suspecting spamming.");
                         return false;
                     } catch (IOException ioe) {
                         logger.error("Error sending response error status ", ioe);
                     }
                 }
 
             }
 
             trackingData.setLong("lastPostMethodTime", now);
         }
 
         request.setAttribute("alreadyCalculatedPostAverage", new Boolean(true));
 
         return true;
     }
 }
