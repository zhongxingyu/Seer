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
  */
 package org.icemobile.util;
 
 import java.io.Serializable;
 import java.util.logging.Logger;
 
 import javax.servlet.ServletContext;
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 
 /**
  * A central client feature and detection class for ICEmobile. 
  * Will detect the OS and 
  *
  */
 public class ClientDescriptor implements Serializable{
     
     private final static String SESSION_KEY = "mobiClient";
     private final static String HEADER_ACCEPT = "Accept";
     private final static String HEADER_ACCEPT_BLACKBERRY_EMUL = "vnd.rim";  //found when emulating IE or FF on BB
     private final static String COOKIE_NAME_ICEMOBILE_CONTAINER = "com.icesoft.user-agent";
     private final static String COOKIE_VALUE_ICEMOBILE_CONTAINER = "HyperBrowser";
     private final static String SIMULATOR_KEY = "org.icemobile.simulator";
 
     private static Logger log = Logger.getLogger(ClientDescriptor.class.getName());
     
     private String userAgent = "";
     private String httpAccepted;
     
     public enum OS{ WINDOWS, IOS, MAC, ANDROID, BLACKBERRY, LINUX }
     public enum FORM_FACTOR{ HANDHELD, TABLET, DESKTOP }
     
     private OS os;
     private FORM_FACTOR formFactor;
     private boolean icemobileContainer;
     private boolean sxRegistered;
     private boolean isSimulator = false;
     
     private UserAgentInfo _userAgentInfo;
     
     private ClientDescriptor(HttpServletRequest request, String view){
         updateHttpAccepted(request);
         updateUserAgent(request);
         updateOS();
         updateFormFactor(view);
         updateICEmobileContainer(request);
         updateSXRegistered(request);
         updateSimulator(request);
         //TODO use getSession(false)
         request.getSession().setAttribute(SESSION_KEY, this);
     }
     
     public static ClientDescriptor getInstance(HttpServletRequest request){
         //TODO use getSession(false)
         ClientDescriptor cd = (ClientDescriptor)request.getSession().getAttribute(SESSION_KEY);
         String view = request.getParameter("view");
         //always update if user agent changed
         if( cd == null || cd.isUserAgentUpdateRequired(request) || view != null){
             cd = new ClientDescriptor(request,view);
         }
         //check if sx has been newly registered
         if( !cd.isSXRegistered() ){
             cd.updateSXRegistered(request);
         }
         //check if simulator state updated
         if( !cd.isSimulator() ){
             cd.updateSimulator(request);
         }
         return cd;        
     }
     
     private boolean isUserAgentUpdateRequired(HttpServletRequest request){
         if( !userAgent.equals(request.getHeader("User-Agent"))){
             return true;
         }
         else{
             return false;
         }
     }
 
     public String getUserAgent() {
         return userAgent;
     }
 
     public boolean isHandheldBrowser() {
         return FORM_FACTOR.HANDHELD == formFactor;
     }
 
     public boolean isTabletBrowser() {
         return FORM_FACTOR.TABLET == formFactor;
     }
     
     private void updateOS(){
         if( _userAgentInfo != null ){
             if (_userAgentInfo.isIOS()) {
                 os = OS.IOS;
             }
             else if (_userAgentInfo.isAndroidOS()){
                 os = OS.ANDROID;
             }
             else if (_userAgentInfo.isBlackberryOS() || httpAccepted.contains(HEADER_ACCEPT_BLACKBERRY_EMUL)) {
                 os = OS.BLACKBERRY;
             }
             else if( _userAgentInfo.isMacOS()){
                 os = OS.MAC;
             }
             else if( _userAgentInfo.isWindowsOS()){
                 os = OS.WINDOWS;
             }
             else{
                 os = OS.LINUX;
             }
         }
     }
 
     public boolean isAndroidOS() {
         return os == OS.ANDROID;
     }
     
     public boolean isBlackBerryOS() {
         return os == OS.BLACKBERRY;
     }
     
     public boolean isBlackBerry10OS(){
         return _userAgentInfo.isBlackberry10OS();
     }
 
     public boolean isIEBrowser() {
         return _userAgentInfo.isIE();
     }
 
     public boolean isIE8orLessBrowser() {
         return _userAgentInfo.isIE8orLess();
     }
     
     public boolean isIE9Browser(){
         return _userAgentInfo.isIE9();
     }
     
     public boolean isIE10Browser(){
         return _userAgentInfo.isIE10();
     }
     
     public boolean isIOS() {
         return os == OS.IOS;
     }
 
     public boolean isIOS5() {
         return os == OS.IOS && _userAgentInfo.isIOS5();
     }
 
     public boolean isIOS6() {
         return os == OS.IOS && _userAgentInfo.isIOS6();
     }
     
     public boolean isIOS7() {
         return os == OS.IOS && _userAgentInfo.isIOS7();
     }
     
     public boolean isSXRegistered(){
         return sxRegistered;
     }
 
     public boolean isSimulator(){
         return isSimulator;
     }
     
     public boolean isICEmobileContainer(){
         if (isSimulator)  {
             if (isDesktopBrowser())  {
                 return true;
             }
         }
         return icemobileContainer;
     }
     
     public boolean isEnhancedBrowser(){
         return isICEmobileContainer() || isSXRegistered();
     }
     
     public boolean isDesktopBrowser(){
         return formFactor == FORM_FACTOR.DESKTOP;
     }
     
     private void updateUserAgent(HttpServletRequest request){
         String ua = request.getHeader("User-Agent");
         if( ua == null || ua.length() == 0 ){
             log.warning("Could not detect device settings, user agent is null");
         }
         else{
             userAgent = ua;
             _userAgentInfo = new UserAgentInfo(userAgent);
         }
     }
 
     private void updateHttpAccepted(HttpServletRequest request){
         String accept = request.getHeader(HEADER_ACCEPT);
         httpAccepted = (accept == null ? "" : accept.toLowerCase());
     }
     
     private void updateICEmobileContainer(HttpServletRequest request){
         Cookie cookie = Utils.getCookie(COOKIE_NAME_ICEMOBILE_CONTAINER, request);
         if (null != cookie && cookie.getValue().startsWith(COOKIE_VALUE_ICEMOBILE_CONTAINER)) {
             icemobileContainer = true;
         }
         else{
             icemobileContainer = false;
         }
     }
     
     private void updateSXRegistered(HttpServletRequest request){
         sxRegistered = SXUtils.isSXRegistered(request);
     }
 
     private void updateSimulator(HttpServletRequest request){
         HttpSession session = request.getSession(false);
         ServletContext servletContext = null;
         if (null != session)  {
             servletContext = session.getServletContext();
             String simulatorSetting = (String)
                 session.getAttribute(SIMULATOR_KEY);
             if ("true".equalsIgnoreCase(simulatorSetting))  {
                 isSimulator = true;
                 return;
             }
         }
         if (isSimulator)  {
             return;
         }
         //settings below cannot change after deployment
         String simulatorSetting = System.getProperty(SIMULATOR_KEY);
         if( simulatorSetting == null ){
             simulatorSetting = request.getParameter("simulator");
         }
         if ("true".equalsIgnoreCase(simulatorSetting))  {
             isSimulator = true;
             return;
         }
         //cannot getServletContext from request without Servlet 3.0,
         //so init parameter only works if there is a session for now
         if (null != servletContext)  {
             simulatorSetting = servletContext
                     .getInitParameter(SIMULATOR_KEY);
         }
         if ("true".equalsIgnoreCase(simulatorSetting))  {
             isSimulator = true;
             return;
         }
     }
     
     private void updateFormFactor(String view){
         if( _userAgentInfo != null ){
             if ("large".equals(view) || _userAgentInfo.isTabletBrowser()) {
                 formFactor = FORM_FACTOR.TABLET;
             }
             else if ("small".equals(view) || _userAgentInfo.isMobileBrowser()){
                 formFactor = FORM_FACTOR.HANDHELD;
             }
             else {
                 formFactor = FORM_FACTOR.DESKTOP;
             }
         }
     }
     
     public boolean isChromeBrowser(){
         return _userAgentInfo.isChrome();
     }
     
     public boolean isHasNativeDatePicker() {
         return isIOS5() || isIOS6() || isIOS7() || isBlackBerryOS() || isChromeBrowser() || _userAgentInfo.isFirefoxAndroid();
     }
 	
 	public boolean isAndroidBrowser(){
         return _userAgentInfo.isAndroidBrowserOrWebView() && !isEnhancedBrowser();
     }
     
     public boolean isAndroidBrowserOrWebView(){
         return _userAgentInfo.isAndroidBrowserOrWebView();
     }
     
     public boolean isAndroid2OS(){
         return _userAgentInfo.isAndroid2();
     }
     
     /*
      * Fixed position is problematic on Android 2 (non-Firefox) and all Android
      * WebViews on tablets, as well as BlackBerry
      */
     public boolean isSupportsFixedPosition(){
         return !(isTabletBrowser() && isAndroidBrowserOrWebView())
                && !(isHandheldBrowser() && isAndroid2OS() && !_userAgentInfo.isFirefoxAndroid() ) //Handheld, Android2 (Firefox works well, no Chrome available)
                && !_userAgentInfo.isBlackberry6OS() //BB6 & 7 do not support fixed well
                && !(_userAgentInfo.isBlackberry10OS() && isEnhancedBrowser()); //BB10 container uses Android 2 browser
     }
 
 
 }
