 /* Copyright 2009 Jeremy Chone - Licensed under the Apache License, Version 2.0
  * http://www.apache.org/licenses/LICENSE-2.0
  */
 package org.snowfk.web;
 
 import java.io.IOException;
 import java.io.Serializable;
 import java.io.UnsupportedEncodingException;
 import java.io.Writer;
 import java.lang.reflect.Array;
 import java.net.URLDecoder;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Queue;
 
 import javax.servlet.ServletContext;
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.fileupload.FileItem;
 import org.apache.commons.fileupload.FileUploadException;
 import org.apache.commons.fileupload.servlet.ServletFileUpload;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.snowfk.util.HttpRequestUtil;
 import org.snowfk.util.MapUtil;
 import org.snowfk.util.ObjectUtil;
 import org.snowfk.web.auth.Auth;
 import org.snowfk.web.db.hibernate.HibernateDaoHelper;
 import org.snowfk.web.part.Part;
 import org.snowfk.web.part.PriUtil;
 
 public class RequestContext {
     static private Logger logger = LoggerFactory.getLogger(RequestContext.class);
     
     HttpServletRequest          req;
     HttpServletResponse         res;
     ServletContext              servletContext;
 
     private ServletFileUpload   fileUploader;
     private boolean             isParamInitialized    = false;
     private boolean             isMultipart           = false;
     private List<?>             fileItems;
 
     //built on demand
     private Map<String, Object> paramMap              = null;
 
     // built on demand
     private Map<String, String> cookieMap;
     private Map<String,WebStateHandle> webStateMap;
 
     private Part                currentPart;
 
     //this is the roo model for the container the "r" map (for the request) and "m" for the model (i.e. WebMap)
     private Map                 rootModel             = new HashMap();
     //this is the HashMap for the "m" model map
     private Map                 webMap                = new HashMap();
 
     private Map<Class, Object>  beanMap               = new HashMap<Class, Object>();
 
     //usually set by the WebController.service
     private WebActionResponse   webActionResponse;
 
     //optional
     private Writer              writer;
     private String              pathInfo;
 
     //set by AuthService.authRequest
     private Auth<?>             auth;
 
     private Queue<WebModule>    currentWebModuleStack = new LinkedList<WebModule>();
 
     /**
      * Shortcut way to create a RequestContext object, usually for testing
      * purpose.
      * 
      * @param part
      * @param paramMap
      */
     public RequestContext(Part part, Map<String, Object> paramMap) {
         setCurrentPart(part);
         this.paramMap = paramMap;
         isParamInitialized = true;
         init();
     }
 
     public RequestContext(HttpServletRequest req, HttpServletResponse res, ServletContext servletContext,
                           ServletFileUpload fileUploader) {
         this.req = req;
         this.res = res;
         this.servletContext = servletContext;
         this.fileUploader = fileUploader;
         init();
     }
 
     private void init() {
         rootModel.put("m", webMap);
     }
 
     /*--------- Auth Methods ---------*/
 
     public Auth<?> getAuth() {
         return auth;
     }
 
     public void setAuth(Auth<?> auth) {
         this.auth = auth;
     }
 
     @SuppressWarnings("unchecked")
     public <T> T getUser(Class<T> userClass) {
         if (auth != null && userClass.isInstance(auth.getUser())) {
             return (T) auth.getUser();
         } else {
             return null;
         }
     }
 
     /*--------- /Auth Methods ---------*/
 
     // --------- Bean Methods --------- //
     /**
      * Add a bean for a given class.
      * 
      * @param <T>
      * @param bean
      * @return the bean for chainability
      */
     public <T> T addBean(T bean) {
         if (bean != null) {
             beanMap.put(bean.getClass(), bean);
         }
         return bean;
     }
 
     /**
      * Return a bean by Class.
      * 
      * @param <T>
      * @param beanClass
      * @return
      */
     public <T> T getBean(Class<T> beanClass) {
         return (T) beanMap.get(beanClass);
     }
 
     // --------- /Bean Methods --------- //    
 
     // --------- currentWebModuleStack Methods --------- //
     public WebModule pushCurrentWebModule(WebModule currentWebModule) {
         currentWebModuleStack.offer(currentWebModule);
         return currentWebModule;
     }
     
     public WebModule pollCurrentWebModule() {
         return currentWebModuleStack.poll();
     }
     
     public WebModule peekCurrentWebModule(){
         return currentWebModuleStack.peek();
     }
     // --------- /currentWebModuleStack Methods --------- //
     
     // --------- Entity & DaoHelper --------- //
     public <T> T getEntity(Class<T> entityClass,Serializable id){
         T t = null;
         if (entityClass != null && id != null){
             WebModule currentWebModule = peekCurrentWebModule();
             if (currentWebModule != null){
                 HibernateDaoHelper daoHelper = currentWebModule.getHibernateDaoHelper();
                 if (daoHelper != null){
                     t = daoHelper.get(entityClass, id);
                 }
             }
         }
         return t;
     }
     // --------- Entity & DaoHelper --------- //
     
     /*--------- Param Methods ---------*/
 
     public Map<String, Object> getParamMap() {
         initParamsIfNeeded();
         return paramMap;
     }
 
     /**
      * A map of all the param starting with the prefix.
      * 
      * @param prefix
      * @return The key is the string after the prefix, and the value, is the
      *         value itself. Return null if no param started with the prefix.
      */
     public Map<String, Object> getParamMap(String prefix) {
         Map<String, Object> allParamMap = getParamMap();
         Map<String, Object> prefixParamMap = null;
         if (allParamMap != null && prefix != null) {
             for (String paramName : allParamMap.keySet()) {
 
                 if (paramName.startsWith(prefix)) {
                     if (prefixParamMap == null) {
                         prefixParamMap = new HashMap<String, Object>();
                     }
                     Object value = allParamMap.get(paramName);
                     prefixParamMap.put(paramName.substring(prefix.length()), value);
                 }
             }
 
         }
         return prefixParamMap;
     }
 
     /**
      * @param <T>
      * @param prefix
      * @param cls
      * @return The list of values given a param prefix
      */
     public <T> List<T> getParamMapValues(String prefix, Class<T> cls) {
         List<T> list = new ArrayList<T>();
         Map<String, Object> prefixParamMap = getParamMap(prefix);
         if (prefixParamMap != null) {
             for (Object valueObj : prefixParamMap.values()) {
                 if (valueObj instanceof String) {
                     T value = ObjectUtil.getValue((String) valueObj, cls, null);
                     if (value != null) {
                         list.add(value);
                     }
                 }
 
             }
         }
         return list;
     }
 
     /**
      * Simple method to get the Request value for a given parameter name. Return
      * null if the value is null or empty.
      * 
      * @param name
      *            parameter value
      * @return
      */
     public String getParam(String name) {
         return getParam(name, String.class, null);
     }
 
     /**
      * Return the param value as "cls" class object. If the value is null (or
      * empty string) return null.
      * 
      * @param <T>
      * @param name
      * @param cls
      * @return the http param value if exist or valid, otherwise, null.
      */
     public <T> T getParam(String name, Class<T> cls) {
         return getParam(name, cls, null);
     }
 
     /**
      * Return the param value as "cls" class object. If the value is null (or
      * empty string) return the defaultValue.<br>
      * 
      * Note: For the first call, this method will parse the request (in case of
      * a multipart).<br />
      * 
      * <div class="issues"> <strong>Issues</strong>
      * <ul>
      * <li>FIXME: Need to fix the multipart handling. Can be simplified</li>
      * </ul>
      * </div>
      * 
      * @param <T>
      *            Class of the return element
      * @param name
      *            of the parameter
      * @param cls
      *            Class of the return element
      * @param defaultValue
      *            Default value in case of an error or null/empty value
      * @return
      */
     @SuppressWarnings("unchecked")
     public <T> T getParam(String name, Class<T> cls, T defaultValue) {
         Map<String, Object> paramMap = getParamMap();
         if (paramMap == null) {
             return defaultValue;
         }
         //if we have a primitive type or array, then, just get the single value and convert it to the appropriate type
         if (ObjectUtil.isPrimitive(cls) || cls.isArray() || cls == FileItem.class || cls.isEnum()) {
             // first, try to get it from the paramMap
             Object valueObject = paramMap.get(name);
 
             if (isMultipart) {
                 // HACK
                 // if not found, try to get it from the regular HttpServletRequest
                 // (in the case of a multiPart post,
                 // HttpServletRequest.getParameter still have the URL params)
                 if (valueObject == null) {
                     valueObject = getReq().getParameter(name);
                 }
             }
 
             if (valueObject == null) {
                 return defaultValue;
             } else if (valueObject instanceof String) {
                 return (T) ObjectUtil.getValue((String) valueObject, cls, defaultValue);
             } else if (valueObject instanceof String[]) {
                 return (T) ObjectUtil.getValue((String[]) valueObject, cls, defaultValue);
             } else {
                 //hope for the best (should be a fileItem)
                 return (T) valueObject;
             }
         }
         //otherwise, if it is not a primitive type, attempt to create the targeted object with the corresponding paramMap
         else {
             Map subParamMap = getParamMap(name + "."); // i.e., "product."
             if (subParamMap != null) {
                 try {
                     T value = cls.newInstance();
                     ObjectUtil.populate(value, subParamMap);
                     return value;
                 } catch (Exception e) {
                     logger.warn(e.getMessage());
                     return defaultValue;
                 }
             } else {
                 return defaultValue;
             }
 
         }
     }
 
     /**
      * Get the param from the request, and if not found, from the cookie. TODO:
      * need to support the # params as well.
      * 
      * @param <T>
      * @param name
      * @param cls
      * @param defaultValue
      * @return
      */
     public <T> T getOmniParam(String name, Class<T> cls, T defaultValue) {
         T value = getParam(name, cls, null);
         if (value == null) {
             value = getCookie(name, cls, defaultValue);
         }
         return value;
     }
 
     @SuppressWarnings("unchecked")
     private void initParamsIfNeeded() {
         if (!isParamInitialized) {
             isMultipart = ServletFileUpload.isMultipartContent(getReq());
             if (isMultipart) {
                 try {
                     fileItems = fileUploader.parseRequest(getReq());
                     paramMap = new HashMap<String, Object>();
 
                     Map<String,Class> paramBaseClasses = new HashMap<String,Class>();
                     boolean hasMultivalues = false;
 
                     for (Object item : fileItems) {
                         FileItem fileItem = (FileItem) item;
                         String paramName = fileItem.getFieldName();
 
                         // in case of normal fields, take the string value.  otherwise
                         // put the whole file item into the map.
                         Object value;
                         Class paramBaseClass;
                         if(fileItem.isFormField()) {
                            value = fileItem.getString();
                             paramBaseClass = String.class;
                         }
                         else {
                             value = fileItem;
                             paramBaseClass = FileItem.class;
                         }
 
                         // make sure that the client isn't calling something that is mixing and
                         // matching parameter types...
                         // todo - could support this as Object arrays or arrays of most specific shared super class.
                         Class prevBaseClass = paramBaseClasses.put(paramName, paramBaseClass);
                         if(prevBaseClass != null && !prevBaseClass.equals(paramBaseClass)) {
                             throw new IllegalArgumentException("parameter " + paramName + " has mixed parameter types (expected all file or all string)");
                         }
 
                         // if there is already a value, then, create a list
                         if (paramMap.containsKey(paramName)) {
                             hasMultivalues = true;
                             Object prevValue = paramMap.get(paramName);
                             if (prevValue instanceof List) {
                                 ((List) prevValue).add(value);
                             } else {
                                 List values = new ArrayList(2);
                                 values.add(prevValue);
                                 values.add(value);
                                 paramMap.put(paramName, values);
                             }
                         } else {
                             paramMap.put(paramName, value);
                         }
                     }
 
                     // if we had multivalues, need to change the list to arrays
                     if (hasMultivalues) {
                         for (String name : paramMap.keySet()) {
                             Object value = paramMap.get(name);
                             if (value instanceof List) {
                                 List valueList = (List) value;
                                 Object[] valueArray = (Object[]) Array.newInstance(paramBaseClasses.get(name), valueList.size());
                                 valueList.toArray(valueArray);
                                 paramMap.put(name, valueArray);
                             }
                         }
                     }
                 } catch (FileUploadException e) {
                     // TODO Auto-generated catch block
                     logger.error(e.getMessage());
                 }
             } else {
                 paramMap = new HashMap<String, Object>();
                 // By the httpServletRequest spect, we can assume the type of
                 // the return Map (name and values)
                 Map<String, String[]> reqMap = getReq().getParameterMap();
                 // now, simplify the map, by replacing single string array to
                 // the string itself.
                 for (String paramName : reqMap.keySet()) {
                     String[] values = reqMap.get(paramName);
                     if (values.length == 1) {
                         paramMap.put(paramName, values[0]);
                     } else {
                         paramMap.put(paramName, values);
                     }
                 }
             }
             isParamInitialized = true;
         }
     }
 
     /*--------- /Param Methods ---------*/
 
     /*--------- Cookie Methods ---------*/
     public Map<String, String> getCookieMap() {
         if (cookieMap == null) {
             cookieMap = new HashMap<String, String>();
             Cookie[] cookies = getReq().getCookies();
 
             if (cookies != null) {
                 for (Cookie c : cookies) {
                     String value = c.getValue();
                     try {
                         value = URLDecoder.decode(value, "UTF-8");
                     } catch (Exception e) {
                         //YES, ignore for now. If failed, the raw value will be in the cookie. 
                     }
                     cookieMap.put(c.getName(), value);
                 }
             }
         }
         return cookieMap;
     }
 
     public String getCookie(String name) {
         return getCookieMap().get(name);
     }
 
     public <T> T getCookie(String name, Class<T> cls, T defaultValue) {
         return ObjectUtil.getValue(getCookie(name), cls, defaultValue);
     }
 
     public void setCookie(String name, Object value) {
         // update the response
         HttpRequestUtil.setCookieValue(getRes(), name, value, true);
         // update the cookieMap (making sure that the template gets what we send to the browser)
         if (value != null) {
             getCookieMap().put(name, value.toString());
         } else {
             removeCookie(name);
         }
     }
 
     public void removeCookie(String name) {
         // update the response
         HttpRequestUtil.removeCookie(getReq(), getRes(), name);
         //update the cookieMap
         getCookieMap().remove(name);
     }
 
     /*--------- /Cookie Methods ---------*/
 
     // --------- WebState Methods --------- //
     /**
      * @param stateContext
      * @return return a WebState for a given uiContext. Never return null.
      */
     public WebStateHandle getWebState(String stateContext){
         if (webStateMap == null){
             webStateMap = new HashMap<String, WebStateHandle>();
         }
         WebStateHandle webState = webStateMap.get(stateContext);
         if (webState == null){
             webState = peekCurrentWebModule().getWebStateHandleFactory().constructWebStateHandle(stateContext, this);
             webStateMap.put(stateContext, webState);
         }
         return webState;
     }
     // --------- /WebState Methods --------- //    
     
     /* --------- CurrentPriPathInfo Methods --------- */
 
     public void setCurrentPart(Part part) {
         currentPart = part;
     }
 
     public Part getCurrentPart() {
         return currentPart;
     }
 
     public String getCurrentPri() {
         return (currentPart != null) ? currentPart.getPri() : null;
     }
 
     /**
      * @return The array of path from after the contextPath
      */
     public String[] getCurrentPriPaths() {
         String[] currentPriPaths = null;
         if (getCurrentPri() != null) {
 
             String priPath = getCurrentPriFullPath();
             String[] tmpPriPaths = priPath.split("/");
             // remove the first element (always empty since the currentPri
             // start starts with "/")
             if (tmpPriPaths.length > 1) {
                 currentPriPaths = new String[tmpPriPaths.length - 1];
                 if (tmpPriPaths.length > 1) {
                     System.arraycopy(tmpPriPaths, 1, currentPriPaths, 0, currentPriPaths.length);
                 }
             } else {
                 currentPriPaths = new String[0];
             }
         } else {
             currentPriPaths = new String[0];
         }
         return currentPriPaths;
     }
 
     /**
      * @param i
      * @return Safely return the path at a given index. If it does not exists,
      *         return null;
      */
     public String getCurrentPriPathAt(int i) {
         String[] paths = getCurrentPriPaths();
         if (paths.length > i) {
             return paths[i];
         } else {
             return null;
         }
     }
 
     public <T> T getCurrentPriPathAt(int i, Class<T> cls) {
         return getCurrentPriPathAt(i, cls, null);
     }
 
     public <T> T getCurrentPriPathAt(int i, Class<T> cls, T defaultValue) {
         String valueStr = getCurrentPriPathAt(i);
         return ObjectUtil.getValue(valueStr, cls, defaultValue);
     }
 
     public String getCurrentLastPriPath() {
         String[] paths = getCurrentPriPaths();
         if (paths != null && paths.length > 1) {
             return paths[paths.length - 1];
         } else {
             return null;
         }
     }
 
     public String getCurrentPriFullPath() {
         String currentPri = getCurrentPri();
         String priPath = currentPri;
         if (currentPri != null) {
             int idx = currentPri.lastIndexOf(':');
 
             if (idx != -1) {
                 priPath = currentPri.substring(idx + 1);
             }
         }
         return priPath;
     }
 
     /* --------- /CurrentPriPathInfo Methods --------- */
 
     /*--------- Writer ---------*/
     public void setWriter(Writer writer) {
         this.writer = writer;
     }
 
     public Writer getWriter() {
         if (writer != null) {
             return writer;
         } else if (res != null) {
             try {
                 return res.getWriter();
             } catch (IOException e) {
                 logger.error(e.getMessage());
                 throw new RuntimeException(e);
             }
         } else {
             return null;
         }
     }
 
     /*--------- /Writer ---------*/
 
     /*--------- RootModel ---------*/
     /**
      * Return the value in the model map (the m.**) with the appropriate type or
      * null if not found.
      * 
      * @see #getModelValue(String, Class, Object)
      */
     public <T> T getModelValue(String namePath, Class<T> cls) {
         return getModelValue(namePath, cls, null);
     }
 
     /**
      * Return the value in the model map (the m.**) with the appropriate type
      * and fall back value.
      * 
      * @param <T>
      * @param namePath
      *            path deliminated with the ".". Note that the "m." should not
      *            be in this namePath.
      * @param cls
      *            The type of the value to be casted to
      * @param defaultValue
      *            The fall back value
      * @return
      */
     public <T> T getModelValue(String namePath, Class<T> cls, T defaultValue) {
         return MapUtil.getNestedValue(webMap, namePath, cls, defaultValue);
     }
 
     public Map getRootModel() {
         return rootModel;
     }
 
     /**
      * @return the Model M (use for the page model "m")
      */
     public Map getWebMap() {
         return webMap;
     }
 
     /*--------- /RootModel ---------*/
 
     public WebActionResponse getWebActionResponse() {
         return webActionResponse;
     }
 
     public void setWebActionResponse(WebActionResponse webActionResponse) {
         this.webActionResponse = webActionResponse;
     }
 
     /*--------- HttpServlet ---------*/
     public HttpServletRequest getReq() {
         return req;
     }
 
     public HttpServletResponse getRes() {
         return res;
     }
 
     public ServletContext getServletContext() {
         return servletContext;
     }
 
     public String getContextPath(){
     	HttpServletRequest request = getReq();
 		if (request != null) {
 			return request.getContextPath();
 		}
 		return null;
     }
     public String getPathInfo() {
         if (pathInfo == null) {
             if (req != null) {
                 // first try the traditional way
                 pathInfo = req.getPathInfo();
 
                 // otherwise build it from the requestURI
                 if (pathInfo == null) {
                     // remove the contextPath
                     pathInfo = req.getRequestURI().substring(req.getContextPath().length());
 
                     try {
                         pathInfo = URLDecoder.decode(pathInfo, "UTF-8");
                     } catch (UnsupportedEncodingException e) {
                         // TODO Auto-generated catch block
                         logger.error(e.getMessage());
                     }
                 }
             }
             //if there is not request, take the pri path as pathInfo
             else if (currentPart != null) {
                 pathInfo = PriUtil.getPathFromPri(currentPart.getPri());
             }
         }
         return pathInfo;
     }
 
     /*--------- /HttpServlet ---------*/
 }
