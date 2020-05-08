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
 * $Id: StandardTemplateNavigationSkin.java,v 1.1 2003-11-27 19:39:46 shahid.shah Exp $
  */
 
 package com.netspective.sparx.theme.basic;
 
 import java.util.Map;
 import java.util.HashMap;
 import java.util.List;
 import java.io.Writer;
 import java.io.IOException;
 import java.io.File;
 import java.net.URL;
 import java.net.MalformedURLException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.ServletContext;
 
 import org.apache.commons.logging.LogFactory;
 import org.apache.commons.logging.Log;
 
 import freemarker.template.Configuration;
 import freemarker.template.TemplateException;
 import freemarker.template.TemplateModel;
 import freemarker.ext.beans.BeansWrapper;
 
 import com.netspective.sparx.theme.Theme;
 import com.netspective.sparx.template.freemarker.FreeMarkerTemplateProcessor;
 import com.netspective.sparx.template.freemarker.FreeMarkerConfigurationAdapter;
 import com.netspective.sparx.template.freemarker.FreeMarkerConfigurationAdapters;
 import com.netspective.sparx.navigate.NavigationContext;
 import com.netspective.sparx.navigate.NavigationTree;
 import com.netspective.sparx.navigate.NavigationControllerServlet;
 import com.netspective.sparx.navigate.NavigationPath;
 import com.netspective.sparx.navigate.NavigationSkin;
 import com.netspective.commons.template.TemplateProcessor;
 import com.netspective.commons.template.TemplateProcessorException;
 
 public class StandardTemplateNavigationSkin extends AbstractThemeSkin implements NavigationSkin
 {
     private static final Log log = LogFactory.getLog(StandardTemplateNavigationSkin.class);
 
     private FreeMarkerConfigurationAdapter fmConfigAdapter;
 
     public StandardTemplateNavigationSkin(Theme theme, String name)
     {
         super(theme, name);
 
         FreeMarkerConfigurationAdapters configFactory = FreeMarkerConfigurationAdapters.getInstance();
 
         fmConfigAdapter = FreeMarkerConfigurationAdapters.getInstance().createConfigurationAdapter();
         fmConfigAdapter.setName(getFreeMarkerConfigName());
         configFactory.addConfiguration(fmConfigAdapter);
 
         Configuration fmConfig = fmConfigAdapter.getConfiguration();
         configFactory.applyWebAppConfiguration(fmConfig, NavigationControllerServlet.getThreadServletContext());
 
         fmConfig.addAutoInclude("console/content/library.ftl");
         fmConfig.addAutoInclude("*/macros.ftl");
     }
 
     public String getFreeMarkerConfigName()
     {
         return StandardTemplateNavigationSkin.class.getName() + "." + hashCode();
     }
 
     public NavigationContext createContext(HttpServlet servlet, HttpServletRequest request, HttpServletResponse response, NavigationTree tree, String navTreeId)
     {
         NavigationContext nc = new NavigationContext(tree, servlet, request, response, this, navTreeId);
 
         Map tmplVars = new HashMap();
        tmplVars.put("resourcesPath", nc.getServletRootUrl() + "resources");
         tmplVars.put("activePage", nc.getActivePage());
         tmplVars.put("activePageId", nc.getActivePage().getQualifiedName());
         tmplVars.put("theme", this.getTheme());
 
         nc.setAttribute(TemplateProcessor.VCATTRNAME_SHARED_TEMPLATE_VARS, tmplVars);
         nc.setAttribute(FreeMarkerTemplateProcessor.VCATTRNAME_SHARED_FM_CONFIG, fmConfigAdapter.getConfiguration());
 
         return nc;
     }
 
     public boolean templateAvailable(ServletContext servletContext, String name) throws IOException
     {
         // First try to open as plain file (to bypass servlet container resource caches).
         String realPath = servletContext.getRealPath(name);
         if (realPath != null)
         {
             File file = new File(realPath);
             if (!file.isFile())
                 return false;
 
             if (file.canRead())
                 return true;
         }
 
         // If it fails, try to open it with servletContext.getResource.
         URL url = null;
         try
         {
             url = servletContext.getResource(name);
         }
         catch (MalformedURLException e)
         {
             return false;
         }
         return url == null ? false : true;
     }
 
 
     public void includeTemplates(Writer writer, NavigationContext nc, String tmplPrefix, boolean topDown) throws IOException, TemplateProcessorException
     {
         Configuration fmConfig = fmConfigAdapter.getConfiguration();
         try
         {
             Map instanceVars = new HashMap();
             Map sharedVars = (Map) nc.getAttribute(TemplateProcessor.VCATTRNAME_SHARED_TEMPLATE_VARS);
             if (sharedVars != null)
                 instanceVars.putAll(sharedVars);
 
             TemplateModel ncModel = BeansWrapper.getDefaultInstance().wrap(nc);
             instanceVars.put("vc", ncModel);
 
             String overrideName = tmplPrefix + "-override.ftl";
             String inheritName = tmplPrefix + ".ftl";
 
             NavigationPath activePath = nc.getActivePage();
             List ancestors = activePath.getAncestorsList();
             ServletContext servletContext = nc.getServlet().getServletConfig().getServletContext();
 
             // find the first ancestor that wants to override the template, that's where the "top" will start
             int overrideAtAncestor = -1;
             if (ancestors.size() > 0)
             {
                 for (int i = ancestors.size()-1; i >= 0; i--)
                 {
                     if (templateAvailable(servletContext, ((NavigationPath) ancestors.get(i)).getAbsPathRelativeToThisPath(overrideName)))
                     {
                         overrideAtAncestor = i;
                         break;
                     }
                 }
             }
 
             if (topDown)
             {
                 // now, start from the first overridden template path or the top path and apply all the templates
                 for (int i = overrideAtAncestor != -1 ? overrideAtAncestor : 0; i < ancestors.size(); i++)
                 {
                     NavigationPath ancestorPath = (NavigationPath) ancestors.get(i);
                     String name = ancestorPath.getAbsPathRelativeToThisPath(inheritName);
                     if(templateAvailable(servletContext, name))
                         fmConfig.getTemplate(name).process(instanceVars, writer);
                 }
 
                 // now apply the template in the current page
                 String name = activePath.getAbsPathRelativeToThisPath(inheritName);
                 if(templateAvailable(servletContext, name))
                     fmConfig.getTemplate(name).process(instanceVars, writer);
             }
             else
             {
                 // first apply the template in the current page (if any)
                 String name = activePath.getAbsPathRelativeToThisPath(inheritName);
                 if(templateAvailable(servletContext, name))
                     fmConfig.getTemplate(name).process(instanceVars, writer);
 
                 // now, start from the first ancestor and go up the chain until the first override
                 for (int i = ancestors.size() - 1; i >= (overrideAtAncestor != -1 ? overrideAtAncestor : 0); i--)
                 {
                     NavigationPath ancestorPath = (NavigationPath) ancestors.get(i);
                     name = ancestorPath.getAbsPathRelativeToThisPath(inheritName);
                     if(templateAvailable(servletContext, name))
                         fmConfig.getTemplate(name).process(instanceVars, writer);
                 }
             }
 
         }
         catch (TemplateException e)
         {
             log.error("Unable to process template", e);
             throw new TemplateProcessorException(e);
         }
     }
 
     public void renderPageMetaData(Writer writer, NavigationContext nc) throws IOException
     {
         includeTemplates(writer, nc, "meta-data", true);
     }
 
     public void renderPageHeader(Writer writer, NavigationContext nc) throws IOException
     {
         includeTemplates(writer, nc, "header", true);
     }
 
     public void renderPageFooter(Writer writer, NavigationContext nc) throws IOException
     {
         includeTemplates(writer, nc, "footer", false);
     }
 }
