 /*
  *   Copyright 2012 George Norman
  *
  *   Licensed under the Apache License, Version 2.0 (the "License");
  *   you may not use this file except in compliance with the License.
  *   You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *
  *   Unless required by applicable law or agreed to in writing, software
  *   distributed under the License is distributed on an "AS IS" BASIS,
  *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *   See the License for the specific language governing permissions and
  *   limitations under the License.
  */
 package com.thruzero.common.jsf.utils;
 
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 
 import javax.servlet.http.HttpSession;
 
 import org.apache.commons.lang3.StringUtils;
 
 import com.thruzero.common.core.support.SimpleIdGenerator;
 
 /**
  * This is a "temporary" work-around for issues encountered with the JSF 2 flash scope (it's not working for me). Flash
  * scope is useful if you want to enable users to be able to open multiple tabs/windows in the same browser (session).
  *
  * @author George Norman
  */
 public class FlashUtils {
 
   /** Request parameter key used to pass a cached FlashHack attribute from one request to the next. */
   public static final String FLASH_HACK_REQUEST_PARAMETER_KEY = "fhk";
 
   /** Session key used to store the one and only FlashHack instance. */
   private static final String FLASH_HACK_SESSION_KEY = FlashHack.class.getName();
 
   // ------------------------------------------------------
   // FlashHack --- EXPERIMENTAL (and hopefully temporary)
   // ------------------------------------------------------
 
   /**
    * TODO-p0(george) - The JSF 2.0 "...getExternalContext().getFlash()" wasn't working for me. Not sure if there was a
    * bug in my code or if there's an issue with flash on my platform (Stack Overflow hinted at platform issues). Anyway,
    * will get back to this later, for now using a homebrew solution.
    */
   static class FlashHack {
     private Map<String, Object> attributes = new ConcurrentHashMap<String, Object>();
 
     public Object removeAttribute(String key) {
       return attributes.remove(key);
     }
 
     public Object getAttribute(String key) {
       return attributes.get(key);
     }
 
     public void setAttribute(String key, Object value) {
       attributes.put(key, value);
     }
 
     public int getTotalCount() {
       return attributes.size();
     }
   }
 
   // ============================================================================
   // FlashUtils
   // ============================================================================
 
   /**
    * Return the "flash" attribute for the given flashHackKey; the key is unique per session and was returned when the attribute
    * was saved.
    */
   @SuppressWarnings("unchecked")
   public static <T> T getFlashAttribute(String flashHackKey) {
     T result = null;
 
     if (StringUtils.isNotEmpty(flashHackKey)) {
       FlashHack flashHack = getFlashHack(false);
 
       if (flashHack != null) {
         result = (T)flashHack.getAttribute(flashHackKey);
       }
     }
 
     return result;
   }
 
   /**
    * Return the "flash" attribute using the value of the "fhk" request parameter as the flash key (e.g., "foo?fhk=312" specifies that
    * "321" be used as the flash key).
    */
   public static <T> T getFlashAttribute() {
     String fhk = FacesUtils.getRequest().getParameter(FlashUtils.FLASH_HACK_REQUEST_PARAMETER_KEY);
 
    return getFlashAttribute(fhk);
   }
 
   /**
    * Remove and return the "flash" attribute for the given flashHackKey; the key is unique per session and was returned when the
    * attribute was saved.
    */
   @SuppressWarnings("unchecked")
   public static <T> T removeFlashAttribute(String flashHackKey) {
     T result = null;
 
     if (StringUtils.isNotEmpty(flashHackKey)) {
       FlashHack flashHack = getFlashHack(false);
 
       if (flashHack != null) {
         result = (T)flashHack.removeAttribute(flashHackKey);
       }
     }
 
     return result;
   }
 
   /**
    * Remove and return the "flash" attribute using the value of the "fhk" request parameter as the flash key (e.g., "foo?fhk=312" specifies that
    * "321" be used as the flash key).
    */
   public static <T> T removeFlashAttribute() {
     String fhk = FacesUtils.getRequest().getParameter(FlashUtils.FLASH_HACK_REQUEST_PARAMETER_KEY);
 
    return removeFlashAttribute(fhk);
   }
 
   /**
    * Save the given "flash" attribute to a unique location in the session; the key of this location is returned by this
    * function and can be used to retrieve the attribute at a later time.
    */
   public static String saveFlashAttribute(Object attribute) {
     String flashHackKey = SimpleIdGenerator.getInstance().getNextIdAsString();
     FlashHack flashHack = getFlashHack(true);
 
     flashHack.setAttribute(flashHackKey, attribute);
 
     return flashHackKey;
   }
 
   public static int getFlashCount() {
     return getFlashHack(true).getTotalCount();
   }
 
   private static FlashHack getFlashHack(boolean createIfNonExistant) {
     FlashHack result = null;
     HttpSession session = FacesUtils.getSession(createIfNonExistant);
 
     if (session != null) {
       result = (FlashHack)session.getAttribute(FLASH_HACK_SESSION_KEY);
 
       if (result == null && createIfNonExistant) {
         result = new FlashHack();
 
         session.setAttribute(FLASH_HACK_SESSION_KEY, result);
       }
     }
 
     return result;
   }
 
 }
