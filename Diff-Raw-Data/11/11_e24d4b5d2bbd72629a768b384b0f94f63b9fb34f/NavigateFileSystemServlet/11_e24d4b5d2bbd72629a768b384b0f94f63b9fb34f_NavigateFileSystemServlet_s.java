 /*
  * Copyright (c) 2000-2002 Netspective Corporation -- all rights reserved
  *
  * Netspective Corporation permits redistribution, modification and use
  * of this file in source and binary form ("The Software") under the
  * Netspective Source License ("NSL" or "The License"). The following
  * conditions are provided as a summary of the NSL but the NSL remains the
  * canonical license and must be accepted before using The Software. Any use of
  * The Software indicates agreement with the NSL.
  *
  * 1. Each copy or derived work of The Software must preserve the copyright
  *    notice and this notice unmodified.
  *
  * 2. Redistribution of The Software is allowed in object code form only
  *    (as Java .class files or a .jar file containing the .class files) and only
  *    as part of an application that uses The Software as part of its primary
  *    functionality. No distribution of the package is allowed as part of a software
  *    development kit, other library, or development tool without written consent of
  *    Netspective Corporation. Any modified form of The Software is bound by
  *    these same restrictions.
  *
  * 3. Redistributions of The Software in any form must include an unmodified copy of
  *    The License, normally in a plain ASCII text file unless otherwise agreed to,
  *    in writing, by Netspective Corporation.
  *
  * 4. The names "Sparx" and "Netspective" are trademarks of Netspective
  *    Corporation and may not be used to endorse products derived from The
  *    Software without without written consent of Netspective Corporation. "Sparx"
  *    and "Netspective" may not appear in the names of products derived from The
  *    Software without written consent of Netspective Corporation.
  *
  * 5. Please attribute functionality to Sparx where possible. We suggest using the
  *    "powered by Sparx" button or creating a "powered by Sparx(tm)" link to
  *    http://www.netspective.com for each application using Sparx.
  *
  * The Software is provided "AS IS," without a warranty of any kind.
  * ALL EXPRESS OR IMPLIED REPRESENTATIONS AND WARRANTIES, INCLUDING ANY
  * IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
  * OR NON-INFRINGEMENT, ARE HEREBY DISCLAIMED.
  *
  * NETSPECTIVE CORPORATION AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES
  * SUFFERED BY LICENSEE OR ANY THIRD PARTY AS A RESULT OF USING OR DISTRIBUTING
  * THE SOFTWARE. IN NO EVENT WILL NETSPECTIVE OR ITS LICENSORS BE LIABLE
  * FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL,
  * CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND
  * REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR
  * INABILITY TO USE THE SOFTWARE, EVEN IF HE HAS BEEN ADVISED OF THE POSSIBILITY
  * OF SUCH DAMAGES.
  *
  * @author Shahid N. Shah
  */
 
 /**
 * $Id: NavigateFileSystemServlet.java,v 1.2 2002-05-19 23:31:28 snshah Exp $
  */
 
 package com.netspective.sparx.xaf.navigate;
 
 import java.io.File;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Hashtable;
 import java.util.Iterator;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.netspective.sparx.util.config.Configuration;
 import com.netspective.sparx.util.config.ConfigurationManager;
 import com.netspective.sparx.util.config.ConfigurationManagerFactory;
 import com.netspective.sparx.xaf.form.DialogContext;
 import com.netspective.sparx.xaf.security.AuthenticatedUser;
 import com.netspective.sparx.xaf.security.LoginDialog;
 import com.netspective.sparx.xaf.skin.SkinFactory;
 import com.netspective.sparx.util.value.ServletValueContext;
 import com.netspective.sparx.util.value.ValueContext;
 
 public class NavigateFileSystemServlet extends HttpServlet implements FilenameFilter
 {
     private static final String CONTENT_TYPE = "text/html";
 
     protected ConfigurationManager manager;
     protected Configuration appConfig;
     protected String skinJspPageName;
     protected String rootPath;
     protected String rootURL;
     protected String rootCaption;
     protected HashSet excludeEntryNames = new HashSet();
     protected Hashtable fileTypeIcons = new Hashtable();
     protected LoginDialog loginDialog;
     protected String sharedImagesUrl;
 
     public void init(ServletConfig config) throws javax.servlet.ServletException
     {
         super.init(config);
 
         ServletContext context = config.getServletContext();
         manager = ConfigurationManagerFactory.getManager(context);
         if(manager == null)
             throw new ServletException("Unable to obtain a ConfigurationManager");
         appConfig = manager.getDefaultConfiguration();
         if(appConfig == null)
             throw new ServletException("Unable to obtain the default Configuration");
 
         ValueContext vc = new ServletValueContext(context, this, null, null);
        sharedImagesUrl = appConfig.getTextValue(vc, com.netspective.sparx.Globals.SHARED_CONFIG_ITEMS_PREFIX + "images-url");
         skinJspPageName = appConfig.getTextValue(vc, "app.navigate.skin-jsp", null);
         rootURL = appConfig.getTextValue(vc, "app.navigate.root-url");
         rootPath = appConfig.getTextValue(vc, "app.navigate.root-path");
         rootCaption = appConfig.getTextValue(vc, "app.navigate.root-caption");
         String loginDialogClassName = appConfig.getTextValue(vc, "app.navigate.login.dialog-class");
         String loginDialogCookieName = appConfig.getTextValue(vc, "app.navigate.login.user-id.cookie-name");
         String loginDialogUserInfoAttrName = appConfig.getTextValue(vc, "app.navigate.login.user.session-attr-name");
 
         if(loginDialogClassName != null)
         {
             try
             {
                 Class loginDialogClass = Class.forName(loginDialogClassName);
                 loginDialog = (LoginDialog) loginDialogClass.newInstance();
                 loginDialog.initialize();
                 loginDialog.setUserNameCookieName(loginDialogCookieName);
                 loginDialog.setUserInfoSessionAttrName(loginDialogUserInfoAttrName);
             }
             catch(ClassNotFoundException e)
             {
                 throw new ServletException(e);
             }
             catch(InstantiationException e)
             {
                 throw new ServletException(e);
             }
             catch(IllegalAccessException e)
             {
                 throw new ServletException(e);
             }
         }
 
         excludeEntryNames.add(skinJspPageName);
         excludeEntryNames.add("WEB-INF");
         excludeEntryNames.add("resources");
         excludeEntryNames.add("temp");
         excludeEntryNames.add("index.jsp");
 
         //fileTypeIcons.put("*", "");
     }
 
     /* called in FileSystemContext when Filenames need to be filtered */
 
     public boolean accept(File dir, String name)
     {
         boolean ret = true;
         if(excludeEntryNames.contains(name) || name.startsWith(".") || name.startsWith("_"))
             ret = false;
         return ret;
     }
 
     public AuthenticatedUser getActiveUser(ServletRequest req)
     {
         ValueContext vc = new ServletValueContext(getServletContext(), this, req, null);
         return loginDialog.getActiveUser(vc);
     }
 
     public String getParentsHtml(HttpServletRequest req, FileSystemContext fsContext)
     {
         StringBuffer parentsHtml = new StringBuffer();
         String rootURI = fsContext.getRootURI();
         ArrayList parentList = fsContext.getActivePath().getParents();
         if(parentList != null)
         {
             Iterator i = parentList.iterator();
             while(i.hasNext())
             {
                 FileSystemEntry entry = (FileSystemEntry) i.next();
                 if(entry.isRoot())
                     parentsHtml.append("<a class='nfs_parent' href='" + rootURI + entry.getEntryURI() + "'><img src='" + sharedImagesUrl + "/navigate/home-sm.gif' border='0'></a> <a href='" + rootURI + entry.getEntryURI() + "'>Home</a>");
                 else
                     parentsHtml.append("<a class='nfs_parent' href='" + rootURI + entry.getEntryURI() + "'>" + entry.getEntryCaption() + "</a>");
 
                 if(i.hasNext())
                     parentsHtml.append("&nbsp;<img src='" + sharedImagesUrl + "/navigate/parent-separator.gif'>&nbsp;");
             }
         }
 
         return parentsHtml.toString();
     }
 
     public String getChildrenHtml(HttpServletRequest req, FileSystemContext fsContext)
     {
         StringBuffer folderRows = new StringBuffer();
         StringBuffer fileRows = new StringBuffer();
         String rootFolderURI = req.getContextPath() + req.getServletPath();
         String rootFileURI = req.getContextPath();
 
         File[] entries = fsContext.getActivePath().listFiles(this);
         if(entries != null)
         {
             for(int i = 0; i < entries.length; i++)
             {
                 FileSystemEntry entry = new FileSystemEntry(fsContext.getRootPath(), entries[i].getAbsolutePath());
                 if(entry.isDirectory())
                     folderRows.append("<tr><td><img src='" + sharedImagesUrl + "/navigate/folder-orange-closed.gif' border='0'></td><td class='nfs_child_folder_caption'><a class='nfs_child_folder' href='" + rootFolderURI + entry.getEntryURI() + "'>" + entry.getEntryCaption() + "</a></td></tr>");
                 else
                 {
                     String icon = (String) fileTypeIcons.get(entry.getEntryType());
                     if(icon == null) icon = "" + sharedImagesUrl + "/navigate/page-yellow.gif";
                     fileRows.append("<tr><td><img src='" + icon + "' border='0'></td><td class='nfs_child_file_caption'><a class='nfs_child_file' href='" + rootFolderURI + entry.getEntryURI() + "'>" + entry.getEntryCaption() + "</a></td></tr>");
                 }
             }
         }
 
         return "<table>" + folderRows.toString() + fileRows.toString() + "</table>";
     }
 
     protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
     {
         ServletContext servletContext = getServletContext();
         resp.setContentType(CONTENT_TYPE);
 
         if(req.getParameter("_logout") != null)
         {
             ValueContext vc = new ServletValueContext(servletContext, this, req, resp);
             loginDialog.logout(vc);
             resp.sendRedirect(appConfig.getTextValue(vc, "app.navigate.root-url"));
             return;
         }
 
         String relativePath = req.getPathInfo();
         FileSystemContext fsContext = new FileSystemContext(req.getContextPath() + req.getServletPath(), rootPath, rootCaption, relativePath);
 
         req.getSession(true).setAttribute("NavigateFileSystemServlet", this);
         req.getSession(true).setAttribute("NavigateFileSystemServlet.fsContext", fsContext);
 
         if(!loginDialog.accessAllowed(servletContext, req, resp))
         {
             DialogContext dc = loginDialog.createContext(servletContext, this, req, resp, SkinFactory.getDialogSkin());
             loginDialog.prepareContext(dc);
             if(dc.inExecuteMode())
             {
                 loginDialog.execute(dc);
             }
             else
             {
                 loginDialog.producePage(resp.getWriter(), dc);
                 return;
             }
         }
 
         FileSystemEntry activeEntry = fsContext.getActivePath();
         if(activeEntry.isDirectory())
         {
             PrintWriter out = resp.getWriter();
             String skinJspPage = fsContext.getActivePath().findInPath(skinJspPageName);
             if(skinJspPage == null)
             {
                 out.println("<b>NavigateFileSystem page processor '" + skinJspPageName + "' (specified as 'navigate.skin-jsp' initParameter) not found in '" + fsContext.getActivePath().getAbsolutePath() + "' or any of its parent directories.</b> Showing servlet defaults instead.<p>");
                 out.println("<h1>" + activeEntry.getEntryCaption() + "</h1>" + getParentsHtml(req, fsContext) + "<p>" + getChildrenHtml(req, fsContext));
                 return;
             }
 
             // since the skinJspPage is an absolute path/file we need to convert it to a URL relative to the application
             skinJspPage = skinJspPage.substring(fsContext.getRootPath().getAbsolutePath().length()).replace('\\', '/');
 
             RequestDispatcher rd = getServletContext().getRequestDispatcher(skinJspPage);
             rd.include(req, resp);
         }
         else
         {
             if(req.getQueryString() != null)
                 resp.sendRedirect(req.getContextPath() + activeEntry.getEntryURI() + "?" + req.getQueryString());
             else
                 resp.sendRedirect(req.getContextPath() + activeEntry.getEntryURI());
         }
     }
 
     protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
     {
         doGet(req, resp);
     }
 }
 
