 /*
  * Copyright 2004-2013 ICEsoft Technologies Canada Corp.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the
  * License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an "AS
  * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
  * express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  *
  */
 package org.icepush;
 
 import javax.servlet.ServletContext;
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 public class PushContext {
     private static final Logger log = Logger.getLogger(PushContext.class.getName());
     private static final String BrowserIDCookieName = "ice.push.browser";
     //assign noop manager to avoid NPEs before the real manager is assign during startup
     private PushGroupManager pushGroupManager = NoopPushGroupManager.Instance;
 
     private int browserCounter = 0;
     private int subCounter = 0;
 
     /**
      * <p>
     *     Constructs a new PushContext.
      * </p>
      */
    private PushContext() {
     }
 
     /**
      * <p>
      *     Instructs the specified browser to back off from ajax push listen for the specified number of milliseconds.
      * </p>
      *
      * @param      browserID
      *                 The ICEpush browser ID as stored in the ice.push.browser cookie.
      * @param      delay
      *                 The delay in milliseconds the browser needs to back off.
      */
     public void backOff(final String browserID, final long delay) {
         pushGroupManager.backOff(browserID, delay);
     }
 
     /**
      * <p>
      *     Creates a Push ID consisting of the Browser ID and Sub ID.  If the specified <code>request</code> does not
      *     contain a Browser ID already, it will be created and set on the specified <code>request</code> and
      *     <code>response</code>.
      * </p>
      *
      * @param      request
      *                 The HTTP Servlet request.
      * @param      response
      *                 The HTTP Servlet response.
      * @return     The created Push ID.
      */
 
     public synchronized String createPushId(HttpServletRequest request, HttpServletResponse response) {
         String browserID = getBrowserIDFromCookie(request);
         if (browserID == null) {
             String currentBrowserID = (String)
                     request.getAttribute(BrowserIDCookieName);
             if (null == currentBrowserID) {
                 browserID = generateBrowserID();
                 Cookie cookie = new Cookie(BrowserIDCookieName, browserID);
                 cookie.setPath("/");
                 response.addCookie(cookie);
                 request.setAttribute(BrowserIDCookieName, browserID);
             } else {
                 browserID = currentBrowserID;
             }
         }
 
         String id = browserID + ":" + generateSubID();
         if (log.isLoggable(Level.FINEST)) {
             log.finest("Created new pushId '" + id + "'.");
         }
         return id;
     }
 
     /**
      * <p>
      *     Initiate a Server Push to the members of the group specified by the <code>groupName</code>.
      * </p>
      *
      * @param      groupName
      *                 The group name of the group.
      * @see        #push(String, PushConfiguration)
      */
     public void push(final String groupName) {
         pushGroupManager.push(groupName);
     }
 
     /**
      * <p>
      *     Initiate a Server Push to the members of the group specified by the <code>groupName</code>.
      * </p>
      *
      * @param      groupName
      *                 The group name of the group.
      * @param      config
      *                 The Push configuration.
      * @see        #push(String)
      */
     public void push(final String groupName, PushConfiguration config) {
         pushGroupManager.push(groupName, config);
     }
 
     /**
      * <p>
      *     Adds the specified <code>pushId</code> to the group with the specified <code>groupName</code>.
      * </p>
      *
      * @param      groupName
      *                 The name of the group the specified <code>pushId</code> needs to be added to.
      * @param      pushId
      *                 The Push ID that needs to be added.
      */
     public void addGroupMember(final String groupName, final String pushId) {
         pushGroupManager.addMember(groupName, pushId);
     }
 
     /**
      * <p>
      *     Removes the specified <code>pushId</code> from the group with the specified <code>groupName</code>.
      * </p>
      *
      * @param      groupName
      *                 The name of the group the specified <code>pushId</code> needs to be removed from.
      * @param      pushId
      *                 The Push ID that needs to be removed.
      */
     public void removeGroupMember(final String groupName, final String pushId) {
         pushGroupManager.removeMember(groupName, pushId);
     }
 
     /**
      * <p>
      *     Sets the push group manager.
      * </p>
      * <p>
      *     <b>Note: This method is not intended for application use!</b>
      * </p>
      *
      * @param      pushGroupManager
      */
     public void setPushGroupManager(final PushGroupManager pushGroupManager) {
         this.pushGroupManager = pushGroupManager;
     }
 
     /**
      * <p>
     *     Gets the PushContext instance associated with the specified <code>servletContext</code>.
      * </p>
      *
     * @param      servletContext
      *                 The ServletContext from which to get the PushContext.
      * @return     The PushContext.
      */
    public static synchronized PushContext getInstance(final ServletContext servletContext) {
        PushContext pushContext = (PushContext)servletContext.getAttribute(PushContext.class.getName());
        if (pushContext == null) {
            servletContext.setAttribute(PushContext.class.getName(), pushContext = new PushContext());
        }
        return pushContext;
     }
 
     private static String getBrowserIDFromCookie(HttpServletRequest request) {
         Cookie[] cookies = request.getCookies();
         if (cookies != null) {
             for (Cookie cookie : cookies) {
                 if (BrowserIDCookieName.equals(cookie.getName())) {
                     return cookie.getValue();
                 }
             }
         }
 
         return null;
     }
 
     synchronized String generateBrowserID() {
         return Long.toString(++browserCounter, 36) + Long.toString(System.currentTimeMillis(), 36);
     }
 
     private synchronized String generateSubID() {
         return Integer.toString((++subCounter) + (hashCode() / 10000), 36);
     }
 }
