 /*
  * This program is free software; you can redistribute it and/or modify it under the 
  * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software 
  * Foundation.
  *
  * You should have received a copy of the GNU Lesser General Public License along with this 
  * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html 
  * or from the Free Software Foundation, Inc., 
  * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
  * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  * See the GNU Lesser General Public License for more details.
  *
  * Copyright 2008 Pentaho Corporation.  All rights reserved.
  */
 package org.pentaho.gwt.widgets.client.utils;
 
 import java.util.HashMap;
 
 import org.pentaho.gwt.widgets.client.i18n.WidgetsLocalizedMessages;
 import org.pentaho.gwt.widgets.client.i18n.WidgetsLocalizedMessagesSingleton;
 
 import com.google.gwt.http.client.Request;
 import com.google.gwt.http.client.RequestBuilder;
 import com.google.gwt.http.client.RequestCallback;
 import com.google.gwt.http.client.RequestException;
 import com.google.gwt.http.client.Response;
 import com.google.gwt.user.client.Window;
 
 /**
  * This class is a ResourceBundle for GWT projects. Provided with a resource's base-name it will fetch and merge resources as follows:
  * 
  * 1. base-name.properties
  * 2. base-name_xx.properties (where XX = language, such as en)
  * 3. base-name_xx_yy.properties (where yy = country, such as US)
  * 
  * When each new resource is fetched it is merged with previous resources. Resource collisions are resolved by overwriting existing resources with new
  * resources. In this way we are able to provide language/country overrides above the default bundles.
  * 
  * @author Michael D'Amour
  */
 public class MessageBundle {
   
   private static final WidgetsLocalizedMessages MSGS = WidgetsLocalizedMessagesSingleton.getInstance().getMessages();
   public static final String PROPERTIES_EXTENSION = ".properties"; //$NON-NLS-1$
   private HashMap<String, String> bundle = new HashMap<String, String>();
   private RequestCallback baseCallback = null;
   private RequestCallback langCallback = null;
   private RequestCallback langCountryCallback = null;
   private String path = null;
   private String bundleName = null;
   private IMessageBundleLoadCallback bundleLoadCallback = null;
   private String localeName = getLanguagePreference();
 
   /**
    * The MessageBundle class fetches localized properties files by using the GWT RequestBuilder against the supplied path. Ideally the path should be relative,
    * but absolute paths are accepted. When the ResourceBundle has fetched and loaded all available resources it will notify the caller by way of
    * IMessageBundleLoadCallback. This is necessary due to the asynchronous nature of the loading process. Care should be taken to be sure not to request
    * resources until loading has finished as inconsistent and incomplete results will be likely.
    * 
    * @param path
    *          The path to the resources (mantle/messages)
    * @param bundleName
    *          The base name of the set of resource bundles, for example 'messages'
    * @param bundleLoadCallback
    *          The callback to invoke when the bundle has finished loading
    */
   public MessageBundle(String path, String bundleName, IMessageBundleLoadCallback bundleLoadCallback) {
     this.path = path;
     this.bundleName = bundleName;
     this.bundleLoadCallback = bundleLoadCallback;
     initCallbacks();
     // decompose locale
     // _en_US
     // 1. bundleName.properties
     // 2. bundleName_en.properties
     // 3. bundleName_en_US.properties
 
     // always fetch the base first
    RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, path + bundleName + PROPERTIES_EXTENSION); //$NON-NLS-1$
     try {
       requestBuilder.sendRequest(null, baseCallback);
     } catch (RequestException e) {
       Window.alert("base load " + MSGS.error() + ":" + e.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
       fireBundleLoadCallback();
     }
   }
 
   private void initCallbacks() {
     baseCallback = new RequestCallback() {
       public void onError(Request request, Throwable exception) {
         Window.alert("baseCallback " + MSGS.error() + ":" + exception.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
         fireBundleLoadCallback();
       }
 
       public void onResponseReceived(Request request, Response response) {
         String propertiesFileText = response.getText();
         // build a simple map of key/value pairs from the properties file
         bundle = PropertiesUtil.buildProperties(propertiesFileText, bundle);
 
         // now fetch the the lang/country variants
         if (localeName.equalsIgnoreCase("default")) { //$NON-NLS-1$
           // process only bundleName.properties
           fireBundleLoadCallback();
           return;
         } else {
           StringTokenizer st = new StringTokenizer(localeName, '_');
           if (st.countTokens() > 0) {
             String lang = st.tokenAt(0);
             // 2. fetch bundleName_lang.properties
             // 3. fetch bundleName_lang_country.properties
             RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, path + bundleName + "_" + lang + PROPERTIES_EXTENSION); //$NON-NLS-1$ //$NON-NLS-2$
             try {
               requestBuilder.sendRequest(null, langCallback);
             } catch (RequestException e) {
               Window.alert("lang " + MSGS.error() + ":" + e.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
               fireBundleLoadCallback();
             }
           } else if (st.countTokens() == 0) {
             // already fetched
             fireBundleLoadCallback();
             return;
           }
         }
       }
     };
     langCallback = new RequestCallback() {
       public void onError(Request request, Throwable exception) {
         // if the language callback fails this means that message_fr.properties
         // does not exist but something like message_fr_CA.properties still could,
         // so we will go ahead and try that as well
         Window.alert("langCallback " + MSGS.error() + ":" + exception.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
         RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, path + bundleName + "_" + localeName + PROPERTIES_EXTENSION); //$NON-NLS-1$ //$NON-NLS-2$
         try {
           requestBuilder.sendRequest(null, langCountryCallback);
         } catch (RequestException e) {
           Window.alert("langCountry " + MSGS.error() + ":" + e.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
           fireBundleLoadCallback();
         }
       }
 
       public void onResponseReceived(Request request, Response response) {
         String propertiesFileText = response.getText();
         // build a simple map of key/value pairs from the properties file
         bundle = PropertiesUtil.buildProperties(propertiesFileText, bundle);
 
         StringTokenizer st = new StringTokenizer(localeName, '_');
         if (st.countTokens() == 2) {
           // 3. fetch bundleName_lang_country.properties
           RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, path + bundleName + "_" + localeName + PROPERTIES_EXTENSION); //$NON-NLS-1$ //$NON-NLS-2$
           try {
             requestBuilder.sendRequest(null, langCountryCallback);
           } catch (RequestException e) {
             Window.alert("langCountry " + MSGS.error() + ":" + e.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
             fireBundleLoadCallback();
           }
         } else {
           // already fetched
           fireBundleLoadCallback();
           return;
         }
       }
     };
     langCountryCallback = new RequestCallback() {
       public void onError(Request request, Throwable exception) {
         Window.alert("langCountryCallback " + MSGS.error() + ":" + exception.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
         fireBundleLoadCallback();
       }
 
       public void onResponseReceived(Request request, Response response) {
         String propertiesFileText = response.getText();
         // build a simple map of key/value pairs from the properties file
         bundle = PropertiesUtil.buildProperties(propertiesFileText, bundle);
         fireBundleLoadCallback();
       }
     };
   }
 
   private void fireBundleLoadCallback() {
     if (bundleLoadCallback != null) {
       bundleLoadCallback.bundleLoaded(bundleName);
     }
   }
 
   /**
    * This method returns the value for the given key with UTF-8 respected if supplied in \\uXXXX style format (single forward slash, u, followed by 4 digits).
    * UTF-8 escaped values are replaced with entity escaping, such as '&#x0101;' for proper consumption by web browsers.
    * 
    * @param key
    *          The name of the resource being requested
    * @return The UTF-8 friendly value found for the given key
    */
   public String getString(String key) {
     String resource = bundle.get(key);
     if (resource == null) {
       return key;
     }
     return decodeUTF8(bundle.get(key));
   }
   
   /**
    * This method return the value for the given key with UTF-8 respected and will replace {n} tokens with the parameters that are passed in.
    * 
    * @param key The name of the resource being requested
    * @param parameters The values to replace occurrences of {n} in the found resource
    * @return The UTF-8 friendly value found for the given key
    */
   public String getString(String key, String...parameters) {
     String resource = bundle.get(key);
     if (resource == null) {
       return key;
     }
     for (int i=0;i<parameters.length;i++) {
       resource = resource.replace("{" + i + "}", parameters[i]); //$NON-NLS-1$ //$NON-NLS-2$
     }
     return decodeUTF8(resource);
   }
 
   private String decodeUTF8(String str) {
     if (str == null) {
       return str;
     }
     while (str.indexOf("\\u") != -1) { //$NON-NLS-1$
       int index = str.indexOf("\\u"); //$NON-NLS-1$
       String hex = str.substring(index + 2, index + 6);
       str = str.substring(0, index) + "&#" + hex + ";" + str.substring(index + 6); //$NON-NLS-1$ //$NON-NLS-2$
     }
     return str;
   }
 
   private static native String getLanguagePreference()
   /*-{
     var m = $doc.getElementsByTagName('meta'); 
     for(var i in m) { 
       if(m[i].name == 'gwt:property' && m[i].content.indexOf('locale=') != -1) { 
         return m[i].content.substring(m[i].content.indexOf('=')+1); 
       } 
     }
     return 'default';
   }-*/;
 }
