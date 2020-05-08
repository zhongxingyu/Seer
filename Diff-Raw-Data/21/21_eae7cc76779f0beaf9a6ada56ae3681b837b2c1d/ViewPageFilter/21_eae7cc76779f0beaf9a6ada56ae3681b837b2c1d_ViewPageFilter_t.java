 /* ===============================================================================
 *
 * Part of the InfoGlue Content Management Platform (www.infoglue.org)
 *
 * ===============================================================================
 *
 *  Copyright (C)
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License version 2, as published by the
 * Free Software Foundation. See the file LICENSE.html for more information.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, including the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc. / 59 Temple
 * Place, Suite 330 / Boston, MA 02111-1307 / USA.
 *
 * ===============================================================================
 */
 
 package org.infoglue.deliver.applications.filters;
 
 import java.io.IOException;
 import java.net.URLDecoder;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.StringTokenizer;
 
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletRequestWrapper;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.apache.log4j.Logger;
 import org.exolab.castor.jdo.Database;
 import org.infoglue.cms.applications.common.VisualFormatter;
 import org.infoglue.cms.controllers.kernel.impl.simple.CastorDatabaseService;
 import org.infoglue.cms.entities.management.LanguageVO;
 import org.infoglue.cms.entities.management.RepositoryVO;
 import org.infoglue.cms.exception.SystemException;
 import org.infoglue.cms.security.InfoGluePrincipal;
 
 import org.infoglue.cms.util.CmsPropertyHandler;
 import org.infoglue.deliver.applications.databeans.DeliveryContext;
 import org.infoglue.deliver.controllers.kernel.impl.simple.BaseDeliveryController;
 import org.infoglue.deliver.controllers.kernel.impl.simple.ExtranetController;
 import org.infoglue.deliver.controllers.kernel.impl.simple.LanguageDeliveryController;
 import org.infoglue.deliver.controllers.kernel.impl.simple.NodeDeliveryController;
 import org.infoglue.deliver.controllers.kernel.impl.simple.RepositoryDeliveryController;
 import org.infoglue.deliver.util.CacheController;
 
 /**
  *
  *
  * @author Lars Borup Jensen (lbj@atira.dk)
  */
 
 public class ViewPageFilter implements Filter 
 {
     private final static Logger logger = Logger.getLogger(ViewPageFilter.class.getName());
 
     private FilterConfig filterConfig = null;
     private URIMatcher uriMatcher = null;
     private URIMapperCache uriCache = null;
     public static String attributeName = null;
 
     public void init(FilterConfig filterConfig) throws ServletException 
     {
         this.filterConfig = filterConfig;
         String filterURIs = filterConfig.getInitParameter(FilterConstants.FILTER_URIS_PARAMETER);
         uriMatcher = URIMatcher.compilePatterns(splitString(filterURIs, ","));
         attributeName = filterConfig.getInitParameter(FilterConstants.ATTRIBUTE_NAME_PARAMETER);
         logger.info("attributeName:" + attributeName);
         if(attributeName == null || attributeName.equals(""))
             attributeName = "NavigationTitle";
         uriCache = new URIMapperCache();
     }
 
     public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException 
     {
         String enableNiceURI = CmsPropertyHandler.getProperty("enableNiceURI");
         if (enableNiceURI == null)
             enableNiceURI = "false";
 
         long end, start = System.currentTimeMillis();
         HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
         HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;
         HttpSession httpSession = httpRequest.getSession(true);
         validateCmsProperties(httpRequest);
         String requestURI = URLDecoder.decode(getContextRelativeURI(httpRequest), "UTF-8");
        logger.info("requestURI:" + requestURI);
        
         if (enableNiceURI.equalsIgnoreCase("true") && !uriMatcher.matches(requestURI)) 
         {
             Integer repositoryId = null;
             Integer languageId = null;
 
             try
             {
                 repositoryId = getRepositoryId(httpRequest, httpSession);
                 languageId = getLanguageId(httpRequest, httpSession, repositoryId);
             }
             catch(Exception e)
             {
                 throw new ServletException("An error occurred when looking for page:" + e.getMessage());
             }
             
             Integer siteNodeId = null;
             String[] nodeNames = splitString(requestURI, "/");
             //logger.info("RepositoryId.: "+repositoryId);
             //logger.info("LanguageId...: "+languageId);
             //logger.info("RequestURI...: "+requestURI);
 
             try 
             {
                 InfoGluePrincipal infoGluePrincipal = (InfoGluePrincipal) httpSession.getAttribute("infogluePrincipal");
                 if (infoGluePrincipal == null) 
                 {
                     try 
                     {
                         infoGluePrincipal = (InfoGluePrincipal) CacheController.getCachedObject("userCache", "anonymous");
                         if (infoGluePrincipal == null) 
                         {
            				    Map arguments = new HashMap();
         				    arguments.put("j_username", "anonymous");
         				    arguments.put("j_password", "anonymous");
         				    
         					infoGluePrincipal = (InfoGluePrincipal)ExtranetController.getController().getAuthenticatedPrincipal(arguments);
         					if(infoGluePrincipal != null)
         						CacheController.cacheObject("userCache", "anonymous", infoGluePrincipal);
                             
                         }
                         //this.principal = ExtranetController.getController().getAuthenticatedPrincipal("anonymous", "anonymous");
 
                     } 
                     catch (Exception e) 
                     {
                         throw new SystemException("There was no anonymous user found in the system. There must be - add the user anonymous/anonymous and try again.", e);
                     }
                 }
 
                 siteNodeId = NodeDeliveryController.getSiteNodeIdFromPath(infoGluePrincipal, repositoryId, nodeNames, attributeName, languageId, DeliveryContext.getDeliveryContext());
 
                 end = System.currentTimeMillis();
 
                 logger.info("Mapped URI " + requestURI + " --> " + siteNodeId + " in " + (end - start) + "ms");
 
                 HttpServletRequest wrappedHttpRequest = prepareRequest(httpRequest, siteNodeId, languageId);
                 //logger.info("wrappedHttpRequest:" + wrappedHttpRequest.getRequestURI() + "?" + wrappedHttpRequest.getQueryString());
                 wrappedHttpRequest.getRequestDispatcher("/ViewPage.action").forward(wrappedHttpRequest, httpResponse);
             } 
             catch (SystemException e) 
             {
                 logger.error("Failed to resolve siteNodeId", e);
                 throw new ServletException(e);
             } 
             catch (Exception e) 
             {
                 throw new ServletException(e);
             }
         } 
         else 
         {
             filterChain.doFilter(httpRequest, httpResponse);
         }
     }
 
     public void destroy() {
         this.filterConfig = null;
     }
 
     private void validateCmsProperties(HttpServletRequest request) {
         if (CmsPropertyHandler.getProperty(FilterConstants.CMS_PROPERTY_SERVLET_CONTEXT) == null) {
             CmsPropertyHandler.setProperty(
                 FilterConstants.CMS_PROPERTY_SERVLET_CONTEXT,
                 request.getContextPath());
         }
     }
 
     private Integer getRepositoryId(HttpServletRequest request, HttpSession session) throws ServletException, Exception 
     {
         if (session.getAttribute(FilterConstants.REPOSITORY_ID) != null) 
         {
             logger.info("Fetching repositoryId from session");
             return (Integer) session.getAttribute(FilterConstants.REPOSITORY_ID);
         }
 
         logger.info("Trying to lookup repositoryId");
         String serverName = request.getServerName();
         String portNumber = new Integer(request.getServerPort()).toString();
         logger.info("serverName:" + serverName);
 
         RepositoryVO repository = null;
         
         Database db = CastorDatabaseService.getDatabase();
 		
         BaseDeliveryController.beginTransaction(db);
 
         try
         {
     		try 
             {
                 repository = RepositoryDeliveryController.getRepositoryDeliveryController().getRepositoryFromServerName(db, serverName, portNumber);
                 logger.info("repository:" + repository);
             } 
             catch (Exception e) 
             {
                 logger.info("Failed to map servername " + serverName + " to a repository");
             }
 
             if (repository == null) 
             {
                 try 
                 {
                     BaseDeliveryController.beginTransaction(db);
 
                     repository = RepositoryDeliveryController.getRepositoryDeliveryController().getMasterRepository(db);
                     
                     BaseDeliveryController.closeTransaction(db);
                 } 
                 catch (Exception e1) 
                 {
                     logger.error("Failed to lookup master repository");
                 }
             }
 
             BaseDeliveryController.closeTransaction(db);
         }
         catch (Exception e1) 
         {
             logger.error("Failed to lookup master repository");
             BaseDeliveryController.rollbackTransaction(db);
         }       
 
         if (repository == null)
             throw new ServletException("Unable to find a repository for server-name " + serverName);
 
         session.setAttribute(FilterConstants.REPOSITORY_ID, repository.getRepositoryId());
         return repository.getRepositoryId();
     }
 
     private Integer getLanguageId(HttpServletRequest request, HttpSession session, Integer repositoryId) throws ServletException, Exception 
     {
         Integer languageId = null;
         if (request.getParameter("languageId") != null) 
         {
             logger.info("Language is explicitely given in request");
             try 
             {
                 languageId = Integer.valueOf(request.getParameter("languageId"));
                 session.setAttribute(FilterConstants.LANGUAGE_ID, languageId);
             } 
             catch (NumberFormatException e) {}
         }
 
         if (languageId != null)
             return languageId;
 
         if (session.getAttribute(FilterConstants.LANGUAGE_ID) != null) {
             logger.info("Fetching languageId from session");
             return (Integer) session.getAttribute(FilterConstants.LANGUAGE_ID);
         }
 
         logger.info("Looking for languageId for repository " + repositoryId);
         Locale requestLocale = request.getLocale();
         
         Database db = CastorDatabaseService.getDatabase();
 		
         BaseDeliveryController.beginTransaction(db);
 
         try
         {
 	        try 
 	        {
 	            List availableLanguagesForRepository = LanguageDeliveryController.getLanguageDeliveryController().getAvailableLanguagesForRepository(db, repositoryId);
 	
 	            if (requestLocale != null) 
 	            {
 	                for (int i = 0; i < availableLanguagesForRepository.size(); i++) 
 	                {
 	                    LanguageVO language = (LanguageVO) availableLanguagesForRepository.get(i);
 	                    logger.info("language:" + language.getLanguageCode());
 	                    logger.info("browserLanguage:" + requestLocale.getLanguage());
 	                    if (language.getLanguageCode().equalsIgnoreCase(requestLocale.getLanguage())) {
 	                        languageId = language.getLanguageId();
 	                    }
 	                }
 	            }
 	            if (languageId == null && availableLanguagesForRepository.size() > 0) {
 	                languageId = ((LanguageVO) availableLanguagesForRepository.get(0)).getLanguageId();
 	            }
 	        } 
 	        catch (Exception e) 
 	        {
 	            logger.error("Failed to fetch available languages for repository " + repositoryId);
 	        }
 	        
 	        BaseDeliveryController.closeTransaction(db);
         }
         catch (Exception e1) 
         {
             logger.error("Failed to lookup master repository");
             BaseDeliveryController.rollbackTransaction(db);
         }         
 	        
         if (languageId == null)
             throw new ServletException("Unable to determine language for repository " + repositoryId);
 
         session.setAttribute(FilterConstants.LANGUAGE_ID, languageId);
         return languageId;
     }
 
     // @TODO should I URLDecode the strings first? (incl. context path)
     private String getContextRelativeURI(HttpServletRequest request) {
         String requestURI = request.getRequestURI();
         String contextPath = request.getContextPath();
         if (contextPath != null && requestURI.length() > 0) {
             requestURI = requestURI.substring(contextPath.length(), requestURI.length());
         }
         if (requestURI.length() == 0)
             return "/";
         return requestURI;
     }
     
     private String[] splitString(String str, String delimiter) {
         List list = new ArrayList();
         StringTokenizer st = new StringTokenizer(str, delimiter);
         while (st.hasMoreTokens()) {
             // Updated to handle portal-url:s
             String t = st.nextToken();
             if (t.startsWith("_")) {
                 break;
             } else {
                 // Not related to portal - add
                 list.add(t.trim());
             }
         }
         return (String[]) list.toArray(new String[list.size()]);
     }
 
     private HttpServletRequest prepareRequest(
         HttpServletRequest request,
         Integer siteNodeId,
         Integer languageId) {
         HttpServletRequest wrappedRequest =
             new IGHttpServletRequest(request, siteNodeId, languageId);
 
         return wrappedRequest;
     }
 
     private class IGHttpServletRequest extends HttpServletRequestWrapper {
         Map requestParameters = new HashMap();
 
         public IGHttpServletRequest(
             HttpServletRequest httpServletRequest,
             Integer siteNodeId,
             Integer languageId) {
             super(httpServletRequest);
             requestParameters.putAll(httpServletRequest.getParameterMap());
             requestParameters.put("siteNodeId", new String[] { String.valueOf(siteNodeId)});
             requestParameters.put("languageId", new String[] { String.valueOf(languageId)});
             if (requestParameters.get("contentId") == null)
                 requestParameters.put("contentId", new String[] { String.valueOf(-1)});
 
             //logger.info("siteNodeId:" + siteNodeId);
             //logger.info("languageId:" + languageId);
             //logger.info("contentId:" + requestParameters.get("contentId"));
         }
 
         public String getParameter(String s) {
             String[] array = (String[]) requestParameters.get(s);
             if (array != null && array.length > 0)
                 return array[0];
             return null;
         }
 
         public Map getParameterMap() {
             return Collections.unmodifiableMap(requestParameters);
         }
 
         public Enumeration getParameterNames() {
             return new ParameterNamesEnumeration(requestParameters.keySet().iterator());
         }
 
         public String[] getParameterValues(String s) {
             String[] array = (String[]) requestParameters.get(s);
             if (array != null && array.length > 0)
                 return array;
             return null;
         }
 
     }
 
     private class ParameterNamesEnumeration implements Enumeration {
         Iterator it = null;
 
         public ParameterNamesEnumeration(Iterator it) {
             this.it = it;
         }
 
         public boolean hasMoreElements() {
             return it.hasNext();
         }
 
         public Object nextElement() {
             return it.next();
         }
 
     }
 
 }
