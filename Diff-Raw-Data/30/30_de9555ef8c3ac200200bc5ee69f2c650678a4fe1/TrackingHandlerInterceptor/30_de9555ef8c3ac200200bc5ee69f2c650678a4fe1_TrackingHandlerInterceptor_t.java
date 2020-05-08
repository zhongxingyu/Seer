 package org.jahia.modules.personalization.tracking;
 
 import org.jahia.modules.personalization.tracking.TrackingData;
 import org.jahia.modules.personalization.tracking.TrackingHelper;
 import org.jahia.modules.personalization.tracking.TrackingService;
 import org.jahia.modules.personalization.tracking.trackers.TrackerInterface;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
 
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  */
 public class TrackingHandlerInterceptor extends HandlerInterceptorAdapter {
     private String trackingCookieName = "jahiaTrackingID";
 
     private String trackingSessionName = "org.jahia.modules.personalization.trackingData";
 
     private TrackingService trackingService;
 
     private List<TrackerInterface> trackers = new ArrayList<TrackerInterface>();
 
     private static ThreadLocal<TrackingData> trackingDataThreadLocal = new ThreadLocal<TrackingData>();
 
     public String getTrackingCookieName() {
         return trackingCookieName;
     }
 
     public String getTrackingSessionName() {
         return trackingSessionName;
     }
 
     public void setTrackingCookieName(String trackingCookieName) {
         this.trackingCookieName = trackingCookieName;
     }
 
     public void setTrackingSessionName(String trackingSessionName) {
         this.trackingSessionName = trackingSessionName;
     }
 
     public void setTrackingService(TrackingService trackingService) {
         this.trackingService = trackingService;
     }
 
     public void setTrackers(List<TrackerInterface> trackers) {
         this.trackers = trackers;
     }
 
     public static TrackingData getThreadLocalTrackingData() {
         return trackingDataThreadLocal.get();
     }
 
     @Override
     public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
 
         HttpSession session = request.getSession();
 
         TrackingData trackingData = (TrackingData) session.getAttribute(trackingSessionName);
         if (trackingData == null) {
             Cookie[] cookies = request.getCookies();
             if (cookies != null) {
                 for (Cookie cookie : cookies) {
                     if (cookie.getName().equals(trackingCookieName)) {
                         // we have found the cookie, we must lookup the tracking data in the persistent storage.
                         String trackingID = cookie.getValue();
                         trackingData = trackingService.getByClientId(trackingID);
                         break;
                     }
                 }
             }
             if (trackingData == null) {
                 // tracking data was not found using a cookie, this probably means that we need to create a new one
                 trackingData = new TrackingData();
                 trackingData.setClientID(TrackingHelper.getInstance().generateNewClientID());
                 Cookie trackingCookie = new Cookie(trackingCookieName, trackingData.getClientID());
                 trackingCookie.setMaxAge(Integer.MAX_VALUE);
                 trackingCookie.setPath("/");
                 response.addCookie(trackingCookie);
             }
         }
 
         // now let's call all the trackers to update the trackingData structure.
        boolean interrupted = false;

         for (TrackerInterface tracker : trackers) {
             if (!tracker.track(request, response, trackingData)) {
                interrupted = true;
                break;
             }
         }
 
         session.setAttribute(trackingSessionName, trackingData);
         request.setAttribute(trackingSessionName, trackingData);
 
         trackingDataThreadLocal.set(trackingData);
 
        if (interrupted) {
            return false;
        } else {
            return super.preHandle(request, response, handler);    //To change body of overridden methods use File | Settings | File Templates.
        }
     }
 
     @Override
     public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
         super.postHandle(request, response, handler, modelAndView);    //To change body of overridden methods use File | Settings | File Templates.
 
         HttpSession session = request.getSession(false);
                 if (session == null) {
                     // if no session is present, we don't do anything...
                     return;
                 }
         try {
         TrackingData trackingData = (TrackingData) session.getAttribute(trackingSessionName);
         if (trackingData == null) {
             // tracking data was probably removed because of a session.invalidate call, let's put it back.
             trackingData = trackingDataThreadLocal.get();
             if (trackingData != null) {
                 session.setAttribute(trackingSessionName, trackingData);
             }
         }
         } catch (IllegalStateException ise) {
             // we have an invalid session, cannot do anything with it.
         }
         trackingDataThreadLocal.set(null);
     }
 }
