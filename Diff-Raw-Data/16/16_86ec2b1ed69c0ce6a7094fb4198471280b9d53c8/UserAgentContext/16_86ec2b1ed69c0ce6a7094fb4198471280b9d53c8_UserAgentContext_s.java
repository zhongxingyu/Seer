 package org.icefaces.util;
 
import org.icefaces.resources.BrowserType;
 
 import javax.faces.context.ExternalContext;
 import javax.faces.context.FacesContext;
import java.util.Map;
 
 /**
  * Copyright 2010-2013 ICEsoft Technologies Canada Corp.
  * <p/>
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * <p/>
  * http://www.apache.org/licenses/LICENSE-2.0
  * <p/>
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * <p/>
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * <p/>
  * User: Nils Lundquist
  * Date: 2013-02-07
  * Time: 3:50 PM
  */
public class UserAgentContext {
     private static final String SESSION_KEY = "userAgentContext";
    public enum OS{ WINDOWS, IOS, MAC, ANDROID, BLACKBERRY, LINUX }
    public enum FORM_FACTOR{ HANDHELD, TABLET, DESKTOP }
 
     private String userAgent;
     private boolean internetExplorer;
     private boolean internetExplorer7;
     private boolean internetExplorer8;
     private boolean internetExplorer9;
     private boolean internetExplorerOther;
     private boolean internetExplorer8orLower;
     private boolean internetExplorer9orHigher;
     private boolean firefox;
     private boolean safari;
     private boolean chrome;
     private boolean android;
     private boolean ios;
 
     private OS os;
     private FORM_FACTOR formFactor;
 
     private UserAgentContext(String userAgent, Map<String, Object> sessionMap) {
         this.userAgent = userAgent;
 
         UserAgentInfo uaInfo = new UserAgentInfo(userAgent);
 
         detectOS(uaInfo);
         detectFormFactor(uaInfo);
         detectInternetExplorer(uaInfo);
         detectFirefox(uaInfo);
         detectSafari(uaInfo);
         detectChrome(uaInfo);
         detectIOS(uaInfo);
         detectAndroid(uaInfo);
 
         sessionMap.put(SESSION_KEY, this);
     }
 
     private void detectOS(UserAgentInfo uaInfo) {
         if (uaInfo.isIOS()) os = OS.IOS;
         else if (uaInfo.isAndroidOS()) os = OS.ANDROID;
         else if (uaInfo.isBlackberryOS()) os = OS.BLACKBERRY;
         else if (uaInfo.isMacOS()) os = OS.MAC;
         else if (uaInfo.isWindowsOS()) os = OS.WINDOWS;
         else os = OS.LINUX;
     }
 
     private void detectFormFactor(UserAgentInfo uaInfo) {
         if (uaInfo.isTabletBrowser())
             formFactor = FORM_FACTOR.TABLET;
 
         else if (uaInfo.isMobileBrowser())
             formFactor = FORM_FACTOR.HANDHELD;
 
         else
             formFactor = FORM_FACTOR.DESKTOP;
     }
 
     private void detectInternetExplorer(UserAgentInfo uaInfo) {
         int ieVersion = uaInfo.getIEVersion();
 
         if (ieVersion > 0) {
             internetExplorer = true;
             internetExplorer7 = 7 == ieVersion;
             internetExplorer8 = 8 == ieVersion;
             internetExplorer9 = 9 == ieVersion;
             internetExplorer8orLower = ieVersion < 9;
             internetExplorer9orHigher = ieVersion > 8;
             internetExplorerOther = !(internetExplorer7 || internetExplorer8 || internetExplorer9);
         }
     }
 
     private void detectFirefox(UserAgentInfo uaInfo) {
         firefox = uaInfo.isFirefox();
     }
 
     private void detectSafari(UserAgentInfo uaInfo) {
         safari = uaInfo.isSafari();
     }
 
     private void detectChrome(UserAgentInfo uaInfo) {
         chrome = uaInfo.isChrome();
     }
 
     private void detectAndroid(UserAgentInfo uaInfo) {
         android = uaInfo.isAndroidOS();
     }
 
     private void detectIOS(UserAgentInfo uaInfo) {
         ios = uaInfo.isIOS();
     }
 
     public boolean isHandheldBrowser() {
         return FORM_FACTOR.HANDHELD == formFactor;
     }
 
     public boolean isTabletBrowser() {
         return FORM_FACTOR.TABLET == formFactor;
     }
 
     public String getUserAgent() {
         return userAgent;
     }
 
     public boolean isInternetExplorer() {
         return internetExplorer;
     }
 
     public boolean isInternetExplorer7() {
         return internetExplorer7;
     }
 
     public boolean isInternetExplorer8() {
         return internetExplorer8;
     }
 
     public boolean isInternetExplorer9() {
         return internetExplorer9;
     }
 
     public boolean isInternetExplorerOther() {
         return internetExplorerOther;
     }
 
     public boolean isInternetExplorer8orLower() {
         return internetExplorer8orLower;
     }
 
     public boolean isInternetExplorer9orHigher() {
         return internetExplorer9orHigher;
     }
 
     public boolean isFirefox() {
         return firefox;
     }
 
     public boolean isSafari() {
         return safari;
     }
 
     public boolean isChrome() {
         return chrome;
     }
 
     public boolean isAndroid() {
         return android;
     }
 
     public boolean isIos() {
         return ios;
     }
 
     public OS getOs() {
         return os;
     }
 
     public FORM_FACTOR getFormFactor() {
         return formFactor;
     }
 
     public boolean isBrowserType(BrowserType browserType) {
         if (browserType == BrowserType.ALL) return true;
         if (browserType == BrowserType.ANDROID) return isAndroid();
         if (browserType == BrowserType.CHROME) return isChrome();
         if (browserType == BrowserType.FIREFOX) return isFirefox();
         if (browserType == BrowserType.IE) return isInternetExplorer();
         if (browserType == BrowserType.IE7) return isInternetExplorer7();
         if (browserType == BrowserType.IE8) return isInternetExplorer8();
         if (browserType == BrowserType.IE8_OR_LESS) return isInternetExplorer8orLower();
         if (browserType == BrowserType.IE9_OR_GREATER) return isInternetExplorer9orHigher();
         if (browserType == BrowserType.IOS) return isIos();
         if (browserType == BrowserType.SAFARI) return isSafari();
 
         return false;
     }
 
     public static UserAgentContext getInstance(FacesContext context) {
         ExternalContext extContext = FacesContext.getCurrentInstance()
                 .getExternalContext();
         Map<String, Object> sessionMap = extContext.getSessionMap();
 
         UserAgentContext uac = (UserAgentContext)sessionMap.get(SESSION_KEY);
 
         if (uac == null) {
             uac = new UserAgentContext(extContext.getRequestHeaderMap()
                     .get("user-agent"),
                     sessionMap);
         }
 
         return uac;
     }
 
     public static UserAgentContext getInstance() {
         return getInstance(FacesContext.getCurrentInstance());
     }
 }
