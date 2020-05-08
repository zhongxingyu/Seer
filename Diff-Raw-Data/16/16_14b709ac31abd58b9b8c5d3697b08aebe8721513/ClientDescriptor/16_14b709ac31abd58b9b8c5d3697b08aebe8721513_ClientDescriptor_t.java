 /*
  * Copyright 2004-2012 ICEsoft Technologies Canada Corp.
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
 
 import java.util.logging.Logger;
 
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletRequest;
 
 /**
  * A central client feature and detection class for ICEmobile. 
  * Will detect the OS and 
  *
  */
 public class ClientDescriptor {
     
     private final static String SESSION_KEY = "mobiClient";
     private final static String HEADER_ACCEPT = "Accept";
     private final static String HEADER_ACCEPT_BLACKBERRY_EMUL = "vnd.rim";  //found when emulating IE or FF on BB
     private final static String COOKIE_NAME_ICEMOBILE_CONTAINER = "com.icesoft.user-agent";
     private final static String COOKIE_VALUE_ICEMOBILE_CONTAINER = "HyperBrowser";
 
     private static Logger log = Logger.getLogger(ClientDescriptor.class.getName());
     
     private String userAgent = "";
     private String httpAccepted;
     
     public enum OS{ WINDOWS, IOS, MAC, ANDROID, BLACKBERRY, LINUX }
     public enum FORM_FACTOR{ HANDHELD, TABLET, DESKTOP }
     
     private OS os;
     private FORM_FACTOR formFactor;
     private boolean icemobileContainer;
     private boolean sxRegistered;
     
     private UserAgentInfo _userAgentInfo;
     
     private ClientDescriptor(HttpServletRequest request){
         updateHttpAccepted(request);
         updateUserAgent(request);
         updateOS();
         updateFormFactor();
         updateICEmobileContainer(request);
         updateSXRegistered(request);
         request.getSession().setAttribute(SESSION_KEY, this);
     }
     
     public static ClientDescriptor getInstance(HttpServletRequest request){
         ClientDescriptor cd = (ClientDescriptor)request.getSession().getAttribute(SESSION_KEY);
         //always update if user agent changed
         if( cd == null || cd.isUserAgentUpdateRequired(request)){
             cd = new ClientDescriptor(request);
         }
         //check if sx has been newly registered
         if( !cd.isSXRegistered() ){
             cd.updateSXRegistered(request);
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
 
     public boolean isIEBrowser() {
         return _userAgentInfo.isIE();
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
     
     public boolean isSXRegistered(){
         return sxRegistered;
     }
     
     public boolean isICEmobileContainer(){
         return icemobileContainer;
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
     
     private void updateFormFactor(){
         if( _userAgentInfo != null ){
             if (_userAgentInfo.isTabletBrowser()) {
                 formFactor = FORM_FACTOR.TABLET;
             }
            else if (_userAgentInfo.isMobileBrowser()){
                 formFactor = FORM_FACTOR.HANDHELD;
             }
             else {
                 formFactor = FORM_FACTOR.DESKTOP;
             }
         }
     }
 
 }
