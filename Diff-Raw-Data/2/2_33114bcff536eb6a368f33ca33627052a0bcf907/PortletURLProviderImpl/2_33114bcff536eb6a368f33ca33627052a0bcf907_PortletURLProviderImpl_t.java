 /*
  * Copyright 2000-2001,2004 The Apache Software Foundation.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *      http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.apache.wsrp4j.producer.provider.sakaiproject.driver;
 
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 
 import oasis.names.tc.wsrp.v1.types.PortletContext;
 import oasis.names.tc.wsrp.v1.types.RuntimeContext;
 import oasis.names.tc.wsrp.v1.types.UserContext;
 
 import org.apache.wsrp4j.log.LogManager;
 import org.apache.wsrp4j.log.Logger;
 import org.apache.wsrp4j.producer.provider.Provider;
 import org.apache.wsrp4j.producer.provider.URLComposer;
 import org.apache.wsrp4j.producer.provider.sakaiproject.PortletURLProvider;
 import org.apache.wsrp4j.producer.provider.sakaiproject.WSRPDynamicInformationProvider;
 import org.apache.wsrp4j.producer.util.Base64;
 import org.apache.wsrp4j.util.Modes;
 import org.apache.wsrp4j.util.WindowStates;
 
 /**
   Implementation of the PortletURLProvider interface
   
   @see org.apache.pluto.information.PortletURLProvider
 */
 public abstract class PortletURLProviderImpl implements PortletURLProvider
 {
     // the relative servlet path
     protected String path;
     
     //servlet request
     protected HttpServletRequest request = null;
 
     //portlet mode
     protected Modes mode = null;
 
     //portlet window state
     protected WindowStates state = null;
 
     protected static final HashSet URLSCHEMES = new HashSet();
 
     static {
         URLSCHEMES.add("http");
         URLSCHEMES.add("https");
         URLSCHEMES.add("ftp");
         URLSCHEMES.add("gopher");
         // URLSCHEMES.add("news");
         // URLSCHEMES.add("mailto");
         URLSCHEMES.add("telnet");
         URLSCHEMES.add("nntp");
         URLSCHEMES.add("wais");
         URLSCHEMES.add("prospero");
     }
 
     //action
     protected boolean action = false;
 
     //secure
     protected boolean secure = false;
 
     //clear render parameters
     protected boolean clearParameters = false;
 
 	// render params
     protected Map parameters = null;
 
     //provider
     protected Provider provider = null;
 
     //logging / tracing
     protected Logger logger = LogManager.getLogManager().getLogger(getClass());
 
     // wsrp runtime context
     protected RuntimeContext runtimeContext = null;
 
     // wsrp user context
     protected UserContext userContext = null;
 
     // wsrp portlet context
     protected PortletContext portletContext = null;
 	
     /**
       Default constructor
       @param request as HttpServletRequest
     */
     public PortletURLProviderImpl(HttpServletRequest request)
     {
         this.request = request;
         
         WSRPDynamicInformationProvider infoProvider = (WSRPDynamicInformationProvider) 
             request.getAttribute(WSRPDynamicInformationProvider.INFO_PROVIDER);
         this.provider = (Provider) request.getAttribute(WSRPDynamicInformationProvider.PROVIDER);
         
         this.runtimeContext = infoProvider.getRuntimeContext();
         this.portletContext = infoProvider.getPortletContext();
         this.userContext = infoProvider.getUserContext();
         this.mode = infoProvider.getPortletMode();
         this.secure = infoProvider.isSecure();
         this.state = infoProvider.getWindowState();
     }
 
     /* (non-Javadoc)
      * @see org.apache.wsrp4j.producer.provider.sakaiproject.driver.PortalURLProvider#setPortletMode(org.apache.wsrp4j.util.Modes)
      */
     public void setPortletMode(Modes mode)
     {
         this.mode = mode;
     }
 
     public static String getAbsoluteURL(String url, HttpServletRequest request)
     {
         // Ensure that the URL is not an absolute URL to an external resouce, in which case,
         // we may not want to encode it at all! We test for protocol schemes that do not contain :// first
         if (url.startsWith("mailto:") || url.startsWith("news:")) 
             return url;
     
         // Make URL relative to the portlet
         StringBuffer serverUrl = new StringBuffer();
         serverUrl.append(request.getScheme())
         .append("://")
         .append(request.getServerName());
         if (((request.getServerPort() != 80) && (!request.isSecure())) || 
             ((request.getServerPort() != 443) && (request.isSecure())))
         {
             serverUrl.append(":");
             serverUrl.append(request.getServerPort());
         }
         String hostPort = serverUrl.toString();
         
         // Is it already an absolute URL
         if (url.startsWith(hostPort)) return url;
         
         if (url.startsWith("/")) return (hostPort + url);
     
         // If the URL still begins with <scheme>:// after potentially filtering out references to this server,
         // we treat it as an external reference and do not encode so that browser can directly get it.
         int pos = url.indexOf("://");
         if (pos > 0) {
             String scheme = url.substring(0, pos).toLowerCase();
             if (URLSCHEMES.contains(scheme)) 
                 return url;
         }
         
         // This looks like a relative URL ... add servlet and context paths
         return (hostPort + request.getContextPath() + request.getServletPath() + "/" + url);
     }
     
    static String getRelativeURL(String url, HttpServletRequest request)
     {
         // Ensure that the URL is not an absolute URL to an external resouce, in which case,
         // we return it as is! We test for protocol schemes that do not contain :// first
         if (url.startsWith("mailto:") || url.startsWith("news:")) 
             return url;
 
         // Make URL relative to the portlet
         StringBuffer serverUrl = new StringBuffer();
         serverUrl.append(request.getScheme())
         .append("://")
         .append(request.getServerName());
         if (((request.getServerPort() != 80) && (!request.isSecure())) || 
             ((request.getServerPort() != 443) && (request.isSecure())))
         {
             serverUrl.append(":");
             serverUrl.append(request.getServerPort());
         }
         String prefix = serverUrl.toString();
         if (url.startsWith(prefix)) url = url.substring(prefix.length());
         
         // If this was a relative URL only, we will remove context and servlet path as well, so that the URL 
         // is truly relative to the portlet  ... 
         prefix = request.getContextPath() + request.getServletPath();
         if (url.startsWith(prefix)) url = url.substring(prefix.length());
 
         return url;
     }
 
     public String toString() {
         return encodeURL(path);
     }
 
     /* (non-Javadoc)
      * @see org.apache.wsrp4j.producer.provider.sakaiproject.driver.PortalURLProvider#setWindowState(org.apache.wsrp4j.util.WindowStates)
      */
     public void setWindowState(WindowStates state)
     {
         this.state = state;
     }
 
     /* (non-Javadoc)
      * @see org.sakaiproject.api.kernel.tool.ToolURL#setPath(java.lang.String)
      */
     public void setPath(String path) {
         this.path = path;
     }
 
     /* (non-Javadoc)
      * @see org.apache.wsrp4j.producer.provider.sakaiproject.driver.PortalURLProvider#setAction()
      */
     public void setAction()
     {
         action = true;
     }
 
     /* (non-Javadoc)
      * @see org.apache.wsrp4j.producer.provider.sakaiproject.driver.PortalURLProvider#setSecure()
      */
     public void setSecure()
     {
         secure = true;
     }
 
     public void setParameter(String name, String value)
     {
         if (value == null)
         {
             parameters.remove(name);
         }
         else
         {
             parameters.put(name, new String [] {value});
         }
     }
 
     public void setParameter(String name, String[] values) 
     {
         if ((values == null) || (values.length == 0))
         {
             parameters.remove(name);
         }
         else
         {
             parameters.put(name, values);
         }            
     }    
 
     /* (non-Javadoc)
      * @see org.apache.wsrp4j.producer.provider.sakaiproject.driver.PortalURLProvider#clearParameters()
      */
     public void clearParameters()
     {
         clearParameters = true;
     }
 
     /* (non-Javadoc)
      * @see org.apache.wsrp4j.producer.provider.sakaiproject.driver.PortalURLProvider#setParameters(java.util.Map)
      */
     public void setParameters(Map parameters)
     {
         this.parameters = parameters;
 
         // we convert all values in the map to arrays of type string
         convertToArrayMap();
     }
 
     /* (non-Javadoc)
      * @see org.apache.wsrp4j.producer.provider.sakaiproject.driver.PortalURLProvider#toString()
      */
     public String encodeURL(String url)
     {
         if (logger.isLogging(Logger.TRACE_HIGH))
         {
             logger.entry(Logger.TRACE_HIGH, "encodeURL()");
         }
         
         url = getRelativeURL(url, request);
         // If the URL still begins with <scheme>:// after potentially filtering out references to this server,
         // we treat it as an external reference and do not encode so that browser can directly get it.
         int pos = url.indexOf("://");
         if (pos > 0) {
             String scheme = url.substring(0, pos).toLowerCase();
             if (URLSCHEMES.contains(scheme)) 
                 return url;
         }
 
         if (mode == null)
         {
             mode = Modes.view;
             if (logger.isLogging(Logger.TRACE_HIGH))
             {
                 logger.text(Logger.TRACE_HIGH, "encodeURL()", "PortletMode is null. Setting portlet mode to 'view'");
             }
         }
 
         if (state == null)
         {
             state = WindowStates.normal;
 
             if (logger.isLogging(Logger.TRACE_HIGH))
             {
                 logger.text(Logger.TRACE_HIGH, "encodeURL()", "WindowState is null. Setting window state to 'normal'");
             }
         }
 
         URLComposer urlComposer = provider.getURLComposer();
         String navigationalState = null;
         
         if ((url != null) && (url.length() > 0))
         {
             navigationalState = Base64.encode(url.getBytes());
         }
         // create urls                                 
         if (action)
         {
             String actionStateStr = null;
             if ((parameters!= null) && (!parameters.isEmpty())) 
             {
                 actionStateStr = Base64.encode(encodeParameters().getBytes());
             }
             //TBD: introduce mode / state mapping class instead of string appending...
             url = urlComposer.createBlockingActionURL(
                     mode.toString(),
                     navigationalState,
                     actionStateStr,
                     state.toString(),
                     secure,
                     runtimeContext,
                     portletContext,
                     userContext);
         } 
         else
         {
             String naviStateStr = null;
             if ((parameters!= null) && (!parameters.isEmpty())) 
             {
                 naviStateStr = encodeParameters();
                 if (url != null)
                 {
                     if (url.indexOf('?') != -1) url = url + "?" + naviStateStr;
                     else url = url + "&" + naviStateStr;
                 } else {
                     url = naviStateStr;
                 }
                 navigationalState = Base64.encode(url.getBytes());
             }
 
             url = urlComposer.createRenderURL(
                     mode.toString(),
                     navigationalState,
                     state.toString(),
                     secure,
                     runtimeContext,
                     portletContext,
                     userContext);
         }
 
         if (logger.isLogging(Logger.TRACE_HIGH))
         {
             logger.exit(Logger.TRACE_HIGH, "encodeURL()", url);
         }
 
         return url;
     }
 
     /**
       Internal method
       Converts parameter values represented as String objects to
       parameter values represented by an array of (a single) String object. 
     */
     private void convertToArrayMap()
     {
 
         if (this.parameters != null)
         {
             Iterator keys = this.parameters.keySet().iterator();
             while (keys != null && keys.hasNext())
             {
 
                 String key = (String)keys.next();
                 Object value = this.parameters.get(key);
                 if (value instanceof String)
                 {
                     String[] values = new String[] {(String)value };
                     this.parameters.put(key, values);
                 }
             }
         }
     }
     
     protected String encodeParameters()
     {
         StringBuffer rv = new StringBuffer();
         String c = "";
         if (parameters.size() > 0)
         {
             for (Iterator iEntries = parameters.entrySet().iterator(); iEntries.hasNext();)
             {
                 Map.Entry entry = (Map.Entry) iEntries.next();
                 String key = (String) entry.getKey();
                 Object val = entry.getValue();
                 if (val instanceof String [])
                 {
                     String [] values = (String [])val;
                     for (int i = 0; i < values.length; i++) {
                         rv.append(c).append(key).append("=").append(values[i]);   
                         c = "&";
                     }
                 }
                 else {
                     rv.append(c).append(key).append("=").append((String)val);
                     c = "&";
                 }
             }
         }
         return rv.toString();
     }
 
 }
