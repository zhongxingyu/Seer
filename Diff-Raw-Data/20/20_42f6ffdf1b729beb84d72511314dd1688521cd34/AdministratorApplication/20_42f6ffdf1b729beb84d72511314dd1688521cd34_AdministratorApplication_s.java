     /*******************************************************************************
  * Copyright 2010-2013 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
  *
  * This file is part of SITools2.
  *
  * SITools2 is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * SITools2 is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with SITools2.  If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 package fr.cnes.sitools.applications;
 
 import org.restlet.Context;
 import org.restlet.Request;
 import org.restlet.Response;
 import org.restlet.Restlet;
 import org.restlet.data.MediaType;
 import org.restlet.ext.wadl.ApplicationInfo;
 import org.restlet.routing.Filter;
 import org.restlet.routing.Router;
 
 import fr.cnes.sitools.common.application.SitoolsParameterizedApplication;
 import fr.cnes.sitools.common.model.Category;
 import fr.cnes.sitools.common.resource.JettyPropertiesResource;
 import fr.cnes.sitools.common.resource.SitoolsJavaVersionResource;
 import fr.cnes.sitools.common.resource.SitoolsSettingsResource;
 import fr.cnes.sitools.notification.business.NotifierFilter;
 import fr.cnes.sitools.proxy.DirectoryProxy;
 import fr.cnes.sitools.server.Consts;
 
 /**
  * Application for managing several settings and plugin resources for administrators.
  * 
  * @author jp.boignard (AKKA Technologies)
  */
 public final class AdministratorApplication extends SitoolsParameterizedApplication {
 
   /** host parent router */
   private Router parentRouter = null;
 
   /** template Dir */
   private String templateDir;
 
   /** css Dir */
   private String cssDir;
 
   /** licence Dir */
   private String licenceDir;
 
   /** Analog reports directory */
   private String analogReportsDir;
 
   /**
    * Category
    * 
    * @param context
    *          parent context
    */
   public AdministratorApplication(Context context) {
     super(context);
   }
 
   /**
    * Default constructor
    * 
    * @param parentRouter
    *          for dynamic applications attachment further if needed.
    * @param context
    *          the context
    */
   public AdministratorApplication(Router parentRouter, Context context) {
     super(context);
     this.parentRouter = parentRouter;
 
     // File Editable Directories
     templateDir = getSettings().getRootDirectory() + getSettings().getString(Consts.FILEEDITOR_FTL_DIR);
     cssDir = getSettings().getRootDirectory() + getSettings().getString(Consts.FILEEDITOR_CSS_DIR);
     licenceDir = getSettings().getRootDirectory() + getSettings().getString(Consts.FILEEDITOR_LICENCE_DIR);
 
     // DO NOT ADD COMMON EXTENSIONS !
     getMetadataService().clearExtensions();
 
     MediaType ftl = MediaType.register("text/freemarker", "freemarker template files");
     this.getMetadataService().addExtension("ftl", ftl);
 
     this.getMetadataService().addExtension("css", MediaType.TEXT_CSS);
     this.getMetadataService().addExtension("html", MediaType.TEXT_HTML);
 
     // Analog reports directory
     analogReportsDir = getSettings().getRootDirectory() + getSettings().getString(Consts.ANALOG_REPORTS_DIR);
 
   }
 
   @Override
   public void sitoolsDescribe() {
     setCategory(Category.ADMIN);
     setName("AdministratorApplication");
     setDescription("Miscellaneous resources for Administrators");
   }
 
   @Override
   public ApplicationInfo getApplicationInfo(Request request, Response response) {
     ApplicationInfo appInfo = super.getApplicationInfo(request, response);
     appInfo.setDocumentation("Specific settings management application for SITools2 administrators.");
     return appInfo;
   }
 
   @Override
   public Restlet createInboundRoot() {
 
     Router router = new Router(getContext());
 
     // attach dynamic resources
     attachParameterizedResources(router);
 
    DirectoryProxy dpTemplate = new DirectoryProxy(getContext(), "file:///" + templateDir, getAttachementRef());
 
    DirectoryProxy dpCss = new DirectoryProxy(getContext(), "file:///" + cssDir, getAttachementRef());
 
     DirectoryProxy dpLicence = new DirectoryProxy(getContext(), "file:///" + licenceDir, getAttachementRef());
 
     /** Template access config */
     dpTemplate.setDeeplyAccessible(true);
     dpTemplate.setListingAllowed(true);
     dpTemplate.setModifiable(true);
     dpTemplate.setRegexp("([^\\s]+(\\.(?i)(ftl|FTL|txt|TXT))$)");
 
     /** Css access config */
     dpCss.setDeeplyAccessible(true);
     dpCss.setListingAllowed(true);
     dpCss.setModifiable(true);
     dpCss.setRegexp("([^\\s]+(\\.(?i)(css|CSS))$)");
 
     /** Licence access config */
     dpLicence.setDeeplyAccessible(true);
     dpLicence.setListingAllowed(true);
     dpLicence.setModifiable(true);
     dpLicence.setRegexp("([^\\s]+(\\.(?i)(htm|html|HTML|HTM))$)");
 
     this.getTunnelService().setEnabled(true);
     this.getTunnelService().setMethodTunnel(true);
     this.getTunnelService().setMethodParameter("method");
 
    router.attach("/ftl", dpTemplate);
    router.attach("/css", dpCss);
     router.attach("/cgu.html", dpLicence);
 
     router.attach("/settings/{PARAMETER}", SitoolsSettingsResource.class);
     router.attach("/javaVersion", SitoolsJavaVersionResource.class);
 
     // Create a directory with no-cache parameter to true to
     DirectoryProxy analogDir = new DirectoryProxy(getContext().createChildContext(), "file:///" + analogReportsDir,
      getAttachementRef() + analogReportsDir);
 
     analogDir.setDeeplyAccessible(true);
     analogDir.setListingAllowed(true);
     analogDir.setModifiable(false);
     analogDir.setNocache(true);
 
     router.attach("/analog", analogDir);
 
     router.attach("/jettyprops", JettyPropertiesResource.class);
 
     Filter filter = new NotifierFilter(getContext());
     filter.setNext(router);
 
     return filter;
   }
 
   /**
    * Gets the templateDir value
    * 
    * @return the templateDir
    */
   public String getTemplateDir() {
     return templateDir;
   }
 
   /**
    * Sets the value of templateDir
    * 
    * @param templateDir
    *          the templateDir to set
    */
   public void setTemplateDir(String templateDir) {
     this.templateDir = templateDir;
   }
 
   /**
    * Gets the cssDir value
    * 
    * @return the cssDir
    */
   public String getCssDir() {
     return cssDir;
   }
 
   /**
    * Sets the value of cssDir
    * 
    * @param cssDir
    *          the cssDir to set
    */
   public void setCssDir(String cssDir) {
     this.cssDir = cssDir;
   }
 
   /**
    * Gets the licenceDir value
    * 
    * @return the licenceDir
    */
   public String getLicenceDir() {
     return licenceDir;
   }
 
   /**
    * Sets the value of licenceDir
    * 
    * @param licenceDir
    *          the licenceDir to set
    */
   public void setLicenceDir(String licenceDir) {
     this.licenceDir = licenceDir;
   }
 
   /**
    * Gets the parentRouter value usefull for dynamic application attachment
    * 
    * @return the parentRouter
    */
   public Router getParentRouter() {
     return parentRouter;
   }
 
 }
