 /*
  * Copyright (c) 2008 TouK.pl
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package pl.touk.tola.gwt.client.state;
 
 import com.extjs.gxt.ui.client.state.Provider;
 import com.extjs.gxt.ui.client.state.StateManager;
 
 import com.google.gwt.user.client.Cookies;
 
 import java.util.Date;
 
 
 /**
  * @author Łukasz Kucharski - lkc@touk.pl
  */
 public final class TolaStateManager extends StateManager {
     private static TolaStateManager INSTANCE;
 
     private TolaStateManager() {
     }
 
     public static TolaStateManager get() {
         if (INSTANCE == null) {
             INSTANCE = initialize();
         }
 
         return INSTANCE;
     }
 
     /**
      * Bug fix for gxt-2.x com.extjs.gxt.ui.client.state.Provider.getInteger()
      * That method will throw NPE if value property for given name don't exist.
      * @param name
      * @return
      */
     @Override
     public int getInteger(String name) {
         if (get(name) == null) {
             return -1;
         }
 
         return super.getInteger(name);
     }
 
     /**
      * Z powodu dziwnej implementacji statne providera i managera w GXT nie można pobierać ani przechowywać
      * ujemnych wartości.
      *
      * @param name         of the value to extract from state provder
      * @param defaultValue value to return when state provider return -1 (meaning no cookie has been found)
      * @return integer value of saved cookie or default value if cookie has not been found
      */
     public int getInteger(String name, int defaultValue) {
       int value = super.getInteger(name);
 
         return (value == -1) ? defaultValue : value;
     }
 
     private static TolaStateManager initialize() {
         INSTANCE = new TolaStateManager();
 
         Provider provider = new TolaCookieProvider();
         INSTANCE.setProvider(provider);
 
         return INSTANCE;
     }
 
     static class TolaCookieProvider extends Provider {
         protected Date defaultExpires = new Date(System.currentTimeMillis() +
                 (1000L * 60 * 60 * 24 * 7 * 12));
 
         protected void clearKey(String name) {
             Cookies.removeCookie(name);
         }
 
         protected String getValue(String name) {
             return Cookies.getCookie(name);
         }
 
         protected void setValue(String name, String value) {
             Cookies.setCookie(name, value, defaultExpires);
         }
     }
 }
