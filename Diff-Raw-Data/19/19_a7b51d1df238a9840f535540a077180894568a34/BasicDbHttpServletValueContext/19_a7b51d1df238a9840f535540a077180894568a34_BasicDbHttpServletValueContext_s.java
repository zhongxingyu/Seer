 /*
  * Copyright (c) 2000-2003 Netspective Communications LLC. All rights reserved.
  *
  * Netspective Communications LLC ("Netspective") permits redistribution, modification and use of this file in source
  * and binary form ("The Software") under the Netspective Source License ("NSL" or "The License"). The following
  * conditions are provided as a summary of the NSL but the NSL remains the canonical license and must be accepted
  * before using The Software. Any use of The Software indicates agreement with the NSL.
  *
  * 1. Each copy or derived work of The Software must preserve the copyright notice and this notice unmodified.
  *
  * 2. Redistribution of The Software is allowed in object code form only (as Java .class files or a .jar file
  *    containing the .class files) and only as part of an application that uses The Software as part of its primary
  *    functionality. No distribution of the package is allowed as part of a software development kit, other library,
  *    or development tool without written consent of Netspective. Any modified form of The Software is bound by these
  *    same restrictions.
  *
  * 3. Redistributions of The Software in any form must include an unmodified copy of The License, normally in a plain
  *    ASCII text file unless otherwise agreed to, in writing, by Netspective.
  *
  * 4. The names "Netspective", "Axiom", "Commons", "Junxion", and "Sparx" are trademarks of Netspective and may not be
  *    used to endorse products derived from The Software without without written consent of Netspective. "Netspective",
  *    "Axiom", "Commons", "Junxion", and "Sparx" may not appear in the names of products derived from The Software
  *    without written consent of Netspective.
  *
  * 5. Please attribute functionality where possible. We suggest using the "powered by Netspective" button or creating
  *    a "powered by Netspective(tm)" link to http://www.netspective.com for each application using The Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS" WITHOUT A WARRANTY OF ANY KIND. ALL EXPRESS OR IMPLIED REPRESENTATIONS AND
  * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT,
  * ARE HEREBY DISCLAIMED.
  *
  * NETSPECTIVE AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE OR ANY THIRD PARTY AS A
  * RESULT OF USING OR DISTRIBUTING THE SOFTWARE. IN NO EVENT WILL NETSPECTIVE OR ITS LICENSORS BE LIABLE FOR ANY LOST
  * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
  * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THE SOFTWARE, EVEN
  * IF HE HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
  *
  * @author Shahid N. Shah
  */
 
 /**
 * $Id: BasicDbHttpServletValueContext.java,v 1.47 2003-11-27 19:35:55 shahid.shah Exp $
  */
 
 package com.netspective.sparx.value;
 
 import java.sql.SQLException;
 import java.io.IOException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpSession;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.Servlet;
 import javax.naming.NamingException;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import freemarker.template.Configuration;
 
 import com.netspective.axiom.value.BasicDatabaseConnValueContext;
 import com.netspective.axiom.SqlManager;
 import com.netspective.axiom.ConnectionContext;
 import com.netspective.sparx.Project;
 import com.netspective.sparx.ProjectComponent;
 import com.netspective.sparx.ProjectManager;
 import com.netspective.sparx.util.HttpUtils;
 import com.netspective.sparx.console.ConsoleServlet;
 import com.netspective.sparx.connection.HttpSessionBindableTransactionConnectionContext;
 import com.netspective.sparx.connection.HttpSessionBindableAutoCommitConnectionContext;
 import com.netspective.sparx.security.HttpLoginManager;
 import com.netspective.sparx.navigate.NavigationContext;
 import com.netspective.sparx.navigate.NavigationControllerServlet;
 import com.netspective.sparx.navigate.NavigationControllerServletOptions;
 import com.netspective.sparx.navigate.NavigationPage;
 import com.netspective.sparx.form.DialogsManager;
 import com.netspective.sparx.form.DialogContext;
 import com.netspective.sparx.theme.Theme;
 import com.netspective.commons.security.AuthenticatedUser;
 import com.netspective.commons.config.ConfigurationsManager;
 import com.netspective.commons.acl.AccessControlListsManager;
 import com.netspective.commons.RuntimeEnvironmentFlags;
 import com.netspective.commons.RuntimeEnvironment;
 import com.netspective.commons.value.ValueSource;
 
 public class BasicDbHttpServletValueContext extends BasicDatabaseConnValueContext
                                       implements ServletValueContext, HttpServletValueContext,
                                                  DatabaseServletValueContext, DatabaseHttpServletValueContext
 {
     private static final Log log = LogFactory.getLog(BasicDbHttpServletValueContext.class);
 
     public static final String CONTEXTATTRNAME_FREEMARKER_CONFIG = "freemarker-config";
     public static final String INITPARAMNAME_DEFAULT_DATA_SRC_ID = "com.netspective.sparx.DEFAULT_DATA_SOURCE";
     public static final String REQATTRNAME_ACTIVE_THEME = "sparx-active-theme";
     public static final String REQATTRNAME_ACTIVE_LOGIN_MANAGER = "sparx-active-login-manager";
     public static final String REQATTRNAME_SHARED_CONN_CONTEXT = "sparx-shared-cc.";
     public static final String SESSATTRNAME_SHARED_CONN_CONTEXT = "sparx-shared-cc.";
 
     public static final int SHARED_CONN_TYPE_NONE    = 0;
     public static final int SHARED_CONN_TYPE_REQUEST = 1;
     public static final int SHARED_CONN_TYPE_SESSION = 2;
 
     public static final String[] SHARED_CONN_TYPES =
             new String[] {"none", "request", "session"};
 
     private NavigationContext navigationContext;
     private DialogContext dialogContext;
     private Servlet servlet;
     private ServletRequest request;
     private ServletResponse response;
     private String rootUrl;
     private boolean redirected;
 
     public BasicDbHttpServletValueContext()
     {
     }
 
     public BasicDbHttpServletValueContext(Servlet servlet, ServletRequest request, ServletResponse response)
     {
         initialize(servlet, request, response);
     }
 
     public void initialize(Servlet servlet, ServletRequest request, ServletResponse response)
     {
         contextNum++;
         this.request = request;
         this.response = response;
         this.servlet = servlet;
         rootUrl = ((HttpServletRequest) request).getContextPath();
     }
 
     public void initialize(NavigationContext nc)
     {
         initialize(nc.getServlet(), nc.getRequest(), nc.getResponse());
         setNavigationContext(nc);
     }
 
     /**
      * Override the parent get connection to provide connection contexts that may be stored in HTTP sessions. If they
      * are stored in HTTP sessions, they will be automatically closed when the session unbinding event occurs. This
      * method allows connection sharing to take place as well -- if a connection is available in either the session or
      * the request then it will be "reused" and a new ConnectionContext will not be created.
      * @param dataSourceId
      * @param transaction
      * @return
      * @throws NamingException
      * @throws SQLException
      */
     public ConnectionContext getConnection(String dataSourceId, boolean transaction) throws NamingException, SQLException
     {
         return getSharedConnection(dataSourceId, transaction, SHARED_CONN_TYPE_NONE);
     }
 
     /**
      * Override the parent get connection to provide connection contexts that may be stored in HTTP sessions. If they
      * are stored in HTTP sessions, they will be automatically closed when the session unbinding event occurs. This
      * method allows connection sharing to take place as well -- if a connection is available in either the session or
      * the request then it will be "reused" and a new ConnectionContext will not be created.
      * @param dataSourceId
      * @param transaction
      * @param sharedType
      * @return
      * @throws NamingException
      * @throws SQLException
      */
     public ConnectionContext getSharedConnection(String dataSourceId, boolean transaction, int sharedType) throws NamingException, SQLException
     {
         ConnectionContext result = null;
 
         HttpServletRequest httpRequest = (HttpServletRequest) request;
         if (sharedType == SHARED_CONN_TYPE_SESSION)
         {
             HttpSession session = httpRequest.getSession();
             result = (ConnectionContext) session.getAttribute(SESSATTRNAME_SHARED_CONN_CONTEXT + dataSourceId);
             if(result != null)
             {
                 if(log.isTraceEnabled())
                     log.trace("Reusing shared session CC " + result + " for data source '"+ result.getDataSourceId() +"'.");
                 return result;
             }
             else
             {
                 if(transaction)
                     result = new HttpSessionBindableTransactionConnectionContext(dataSourceId, this);
                 else
                     result = new HttpSessionBindableAutoCommitConnectionContext(dataSourceId, this);
                 session.setAttribute(SESSATTRNAME_SHARED_CONN_CONTEXT + dataSourceId, result);
             }
         }
         else if (sharedType == SHARED_CONN_TYPE_REQUEST)
         {
             result = (ConnectionContext) httpRequest.getAttribute(REQATTRNAME_SHARED_CONN_CONTEXT + dataSourceId);
             if(result != null)
             {
                 if(log.isTraceEnabled())
                     log.trace("Reusing shared request CC " + result + " for data source '"+ result.getDataSourceId() +"'.");
                 return result;
             }
             else
             {
                 if(transaction)
                     result = new HttpSessionBindableTransactionConnectionContext(dataSourceId, this);
                 else
                     result = new HttpSessionBindableAutoCommitConnectionContext(dataSourceId, this);
             }
         }
         else
         {
             if(transaction)
                 result = new HttpSessionBindableTransactionConnectionContext(dataSourceId, this);
             else
                 result = new HttpSessionBindableAutoCommitConnectionContext(dataSourceId, this);
         }
 
         if(log.isTraceEnabled())
             log.trace("Obtained " + result + " for data source '"+ result.getDataSourceId() +"'.");
 
         return result;
     }
 
     public String getDefaultDataSource()
     {
         String result = super.getDefaultDataSource();
         if(result != null && result.length() > 0)
             return result;
 
         // the default data source is (1) specified as a servlet init param, (2) specified in <default-data-source> in project.xml, or (3) is jdbc/default
         result = ((NavigationControllerServlet) servlet).getServletOptions().getDefaultDataSourceId(null);
         if(result == null)
         {
            ValueSource projectDefaultDataSource = getProject().getDefaultDataSource();
            if(projectDefaultDataSource != null)
                result = projectDefaultDataSource.getTextValue(this);
         }
         if(result == null)
             result = NavigationControllerServletOptions.DEFAULT_DATA_SOURCE_ID;
 
         if(result == null)
             throw new RuntimeException("No default data source available. Provide one using '"+ NavigationControllerServletOptions.INITPARAMNAME_SERVLET_OPTIONS +"' servlet context init parameter or in project.xml using 'default-data-source' tag.");
 
         return result;
     }
 
     public NavigationContext getNavigationContext()
     {
         return navigationContext;
     }
 
     public void setNavigationContext(NavigationContext navigationContext)
     {
         this.navigationContext = navigationContext;
     }
 
     public DialogContext getDialogContext()
     {
         return dialogContext;
     }
 
     public void setDialogContext(DialogContext dialogContext)
     {
         this.dialogContext = dialogContext;
     }
 
     public Object getContextLocation()
     {
         return getHttpRequest().getRequestURI();
     }
 
     public Object getAttribute(String attributeId)
     {
         return request.getAttribute(attributeId);
     }
 
     public void setAttribute(String attributeId, Object attributeValue)
     {
         request.setAttribute(attributeId, attributeValue);
     }
 
     public HttpServletRequest getHttpRequest()
     {
         return (HttpServletRequest) request;
     }
 
     public HttpServletResponse getHttpResponse()
     {
         return (HttpServletResponse) response;
     }
 
     public HttpServlet getHttpServlet()
     {
         return (HttpServlet) servlet;
     }
 
     public ServletRequest getRequest()
     {
         return request;
     }
 
     public ServletResponse getResponse()
     {
         return response;
     }
 
     public Servlet getServlet()
     {
         return servlet;
     }
 
     public boolean isInConsole()
     {
         return servlet instanceof ConsoleServlet;
     }
 
     public AuthenticatedUser getAuthenticatedUser()
     {
         HttpLoginManager loginManager = getActiveLoginManager();
         return loginManager != null ? loginManager.getAuthenticatedUser(this) : null;
     }
 
     public ConfigurationsManager getConfigurationsManager()
     {
         return getProject();
     }
 
     public AccessControlListsManager getAccessControlListsManager()
     {
         return getProject();
     }
 
     public SqlManager getSqlManager()
     {
         return getProject();
     }
 
     public DialogsManager getDialogsManager()
     {
         return getProject();
     }
 
     public RuntimeEnvironmentFlags getRuntimeEnvironmentFlags()
     {
         return ((RuntimeEnvironment) getServlet()).getRuntimeEnvironmentFlags();
     }
 
     public ProjectComponent getProjectComponent()
     {
         return ((ProjectManager) getServlet()).getProjectComponent();
     }
 
     public Project getProject()
     {
         return getProjectComponent().getProject();
     }
 
     public final String getServerRootUrl()
     {
         String reqURL = getHttpRequest().getRequestURL().toString();
         String reqURI = getHttpRequest().getRequestURI();
         return reqURL.substring(0, reqURL.length() - reqURI.length());
     }
 
     public final String getSparxResourceUrl(String resource)
     {
         return rootUrl + "/sparx/" + resource;
     }
 
     public final String getAppResourceUrl(String resource)
     {
         return rootUrl + "/resources/" + resource;
     }
 
     public final String getRootUrl()
     {
         return rootUrl;
     }
 
     public final String getServletRootUrl()
     {
        return rootUrl + (getHttpRequest().getServletPath().startsWith("/") ? getHttpRequest().getServletPath() : "/" + getHttpRequest().getServletPath() );
     }
 
     public final String getConsoleUrl()
     {
         return rootUrl + "/console";
     }
 
     /**
      * Take the given URL and ensure that the current page's retain params are added to it
      * @param url The complete URL to use
      * @return The given url plus any of our current page's retin params
      */
     public final String constructAppUrl(String url)
     {
         NavigationPage activePage = getNavigationContext().getActivePage();
         ValueSource retainParamsVS = activePage.getRetainParams();
 
         if(retainParamsVS != null)
             return HttpUtils.appendParams(getHttpRequest(), url, retainParamsVS.getTextValue(this));
         else
             return url;
     }
 
     public final String getConsoleFileBrowserLink(String absolutePath, boolean showRelative)
     {
         if(showRelative)
         {
             String servletContextPath = servlet.getServletConfig().getServletContext().getRealPath("");
             if(absolutePath.startsWith(servletContextPath))
                 return getConsoleFileBrowserLinkShowAlt(absolutePath, absolutePath.substring(servletContextPath.length()));
         }
 
         return getConsoleFileBrowserLinkShowAlt(absolutePath, null);
     }
 
     public void sendRedirect(String url) throws IOException
     {
         HttpServletResponse resp = (HttpServletResponse) response;
         resp.sendRedirect(resp.encodeRedirectURL(url));
         redirected = true;
     }
 
     public boolean isRedirected()
     {
         return redirected;
     }
 
     public final String getConsoleFileBrowserLinkShowAlt(String absolutePath, String showAltPath)
     {
         String servletContextPath = servlet.getServletConfig().getServletContext().getRealPath("");
         if(absolutePath.startsWith(servletContextPath))
         {
             String relativePath = absolutePath.substring(servletContextPath.length());
             StringBuffer result = new StringBuffer();
             result.append("<a href=\"");
             result.append(getConsoleUrl());
             result.append("/project/files/");
             result.append(relativePath.replace('\\', '/'));
             result.append("\">");
             if(showAltPath != null)
             {
                 result.append("<span title=\"");
                 result.append(absolutePath);
                 result.append("\">");
                 result.append(showAltPath);
                 result.append("</span>");
             }
             else
                 result.append(absolutePath);
             result.append("</a>");
             return result.toString();
         }
         else
         {
             if(showAltPath != null)
                 return showAltPath;
             else
                 return absolutePath;
         }
     }
 
     public Theme getActiveTheme()
     {
         return (Theme) request.getAttribute(REQATTRNAME_ACTIVE_THEME);
     }
 
     public HttpLoginManager getActiveLoginManager()
     {
         return (HttpLoginManager) request.getAttribute(REQATTRNAME_ACTIVE_LOGIN_MANAGER);
     }
 
     public Configuration getFreeMarkerConfiguration()
     {
         return ((NavigationControllerServlet) getHttpServlet()).getFreeMarkerConfiguration();
     }
 }
