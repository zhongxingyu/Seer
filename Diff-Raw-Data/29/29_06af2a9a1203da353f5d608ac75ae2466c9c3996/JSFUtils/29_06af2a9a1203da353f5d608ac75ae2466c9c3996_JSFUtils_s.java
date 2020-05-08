 /*******************************************************************************
  * Copyright (c) 2005 Oracle Corporation.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Gerry Kessler - initial API and implementation
  *******************************************************************************/ 
 
 package org.eclipse.jst.jsf.core.internal.project.facet;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.jst.j2ee.model.IModelProvider;
 import org.eclipse.jst.javaee.web.WebAppVersionType;
 import org.eclipse.jst.jsf.core.JSFVersion;
 import org.eclipse.jst.jsf.core.internal.JSFCorePlugin;
 import org.eclipse.jst.jsf.core.internal.Messages;
 import org.eclipse.jst.jsf.core.internal.jsflibraryregistry.ArchiveFile;
 import org.eclipse.jst.jsf.core.internal.jsflibraryregistry.JSFLibrary;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
 
 
 /**
  * 
  */
 @SuppressWarnings("deprecation")
 public abstract class JSFUtils {
     /**
 	 * The default name for the Faces servlet
 	 */
 	public static final String JSF_DEFAULT_SERVLET_NAME = "Faces Servlet"; //$NON-NLS-1$
 	/**
 	 * The default name of the Faces servlet class
 	 */
 	public static final String JSF_SERVLET_CLASS = "javax.faces.webapp.FacesServlet"; //$NON-NLS-1$
 	/**
 	 * The name of the context parameter used for JSF configuration files
 	 */
 	public static final String JSF_CONFIG_CONTEXT_PARAM = "javax.faces.CONFIG_FILES"; //$NON-NLS-1$
 	
 	/**
 	 * The name of the context parameter used for defining the default JSP file extension
 	 */
 	public static final String JSF_DEFAULT_SUFFIX_CONTEXT_PARAM = "javax.faces.DEFAULT_SUFFIX"; //$NON-NLS-1$
 	
 	/**
 	 * The path to the default application configuration file
 	 */
 	public static final String JSF_DEFAULT_CONFIG_PATH = "/WEB-INF/faces-config.xml";  //$NON-NLS-1$
 
 	/**
 	 * Default URL mapping to faces servlet
 	 */
 	public static final String JSF_DEFAULT_URL_MAPPING = "/faces/*"; //$NON-NLS-1$
 
 	/**
 	 * the key for implementation libraries in persistent properties
 	 */
 	public static final String PP_JSF_IMPLEMENTATION_LIBRARIES = "jsf.implementation.libraries"; //$NON-NLS-1$
 	/**
 	 * the key for component libraries in persistent properties
 	 */
 	public static final String PP_JSF_COMPONENT_LIBRARIES = "jsf.component.libraries"; //$NON-NLS-1$
 	/**
 	 * the key for implementation type in persistent properties
 	 */
 	public static final String PP_JSF_IMPLEMENTATION_TYPE = "jsf.implementation.type"; //$NON-NLS-1$
 
 	private static final String DEFAULT_DEFAULT_MAPPING_SUFFIX = "jsp"; //$NON-NLS-1$
 	
 	private final JSFVersion  _version;
     private final IModelProvider _modelProvider;
 	
 	/**
 	 * @param version
 	 * @param modelProvider 
 	 */
 	protected JSFUtils(final JSFVersion version, final IModelProvider modelProvider)
 	{
 	    _version = version;
 	    _modelProvider = modelProvider;
 	}
 	
 	/**
 	 * @return the jsf version that this instance is for.
 	 */
 	public final JSFVersion getVersion()
     {
         return _version;
     }
 
 	/**
 	 * @param config
 	 * @return servlet display name to use from wizard data model
 	 */
 	protected final String getDisplayName(IDataModel config) {
 		String displayName = config.getStringProperty(IJSFFacetInstallDataModelProperties.SERVLET_NAME);
 		if (displayName == null || displayName.trim().length() == 0)
 			displayName = JSF_DEFAULT_SERVLET_NAME;
 		return displayName.trim();
 	}
 	
 	/**
 	 * @param config
 	 * @return servlet display name to use from wizard data model
 	 */
 	protected final String getServletClassname(IDataModel config) {
 		String className = config.getStringProperty(IJSFFacetInstallDataModelProperties.SERVLET_CLASSNAME);
 		if (className == null || className.trim().equals("")) //$NON-NLS-1$
 			className = JSF_SERVLET_CLASS;
 		return className.trim();
 	}
 
 	/**
 	 * @return IModelProvider
 	 */
 	public final IModelProvider getModelProvider() {
 		Object webAppObj = _modelProvider.getModelObject();
 		if (webAppObj == null)
 		{
 			return null;
 		}
 		return _modelProvider;
 	}
 
    /**
      * @param configPath
      */
     public final void createConfigFile(IPath configPath)
     {
         FileOutputStream os = null;
 //        final String QUOTE = new String(new char[] { '"' });
         try {
             IPath dirPath = configPath.removeLastSegments(1);
             dirPath.toFile().mkdirs();
             File file = configPath.toFile();
             file.createNewFile();
             os = new FileOutputStream(file);
             printConfigFile(os);
         } catch (FileNotFoundException e) {
             JSFCorePlugin.log(IStatus.ERROR, Messages.JSFUtils_ErrorCreatingConfigFile, e);
         } catch (IOException e) {
             JSFCorePlugin.log(IStatus.ERROR, Messages.JSFUtils_ErrorCreatingConfigFile, e);
         } finally {
             if (os != null) {
                 try {
                     os.close();
                 } catch (IOException e) {
                     JSFCorePlugin.log(IStatus.ERROR, Messages.JSFUtils_ErrorClosingConfigFile, e);
                 }
             }
         }
     }
     
     /**
      * @param out
      */
     protected final void printConfigFile(final OutputStream out)
     {
         PrintWriter pw = null;
         try
         {
             pw = new PrintWriter(out);
             doVersionSpecificConfigFile(pw);
         }
         finally
         {
             if (pw != null)
             {
                 pw.close();
             }
         }
     }
     
     /**
      * @param pw
      */
     public abstract void doVersionSpecificConfigFile(final PrintWriter pw);
     
     
     /**
      * @param webAppObj 
      * @return true if this going into a JavaEE (1.5 and later) or a J2EE (1.4 and earlier)
      * configured application.
      */
     public boolean isJavaEE(final Object webAppObj)
     {
       if (webAppObj instanceof org.eclipse.jst.javaee.web.WebApp)
       {
           org.eclipse.jst.javaee.web.WebApp webApp = (org.eclipse.jst.javaee.web.WebApp)webAppObj;
           return WebAppVersionType.VALUES.contains(webApp.getVersion());
       }
       return false;
     }
 
     /**
      * @param config
      * @return list of URL patterns from the datamodel
      */
     protected final List<String> getServletMappings(final IDataModel config) {
         final List<String> mappings = new ArrayList<String>();
         final String[] patterns = (String[])config.getProperty(IJSFFacetInstallDataModelProperties.SERVLET_URL_PATTERNS);
         if (patterns != null)
         {
             for (final String pattern : patterns)
             {
                 mappings.add(pattern);
             }
         }
         return mappings;
     }
     /**
      * Does an update of the web application's config file.
      * 
      * @param webApp must be WebApp of the appropriate type.
      * @param config
      * @throws ClassCastException if webApp is not the appropriate type.
      */
     public abstract void updateWebApp(Object webApp, IDataModel config);
 
 
     /**
      * Called on a facet uninstall to remove JSF related changes.
      * @param webApp
      */
     public abstract void rollbackWebApp(Object webApp);
     
     /**
      * @param fileExtension
      * @return true if the file extension is deemed to be for a JSF.
      */
     protected boolean isValidKnownExtension(String fileExtension) {
        if (fileExtension != null &&
                (   fileExtension.equalsIgnoreCase(DEFAULT_DEFAULT_MAPPING_SUFFIX) ||  
                fileExtension.equalsIgnoreCase("jspx") ||  //$NON-NLS-1$
                fileExtension.equalsIgnoreCase("jsf") || //$NON-NLS-1$
                fileExtension.equalsIgnoreCase("xhtml"))) //$NON-NLS-1$
            return true;

         return false;
     }
 
     
     /**
      * @param resource
      * @return true if the resource is deemed to be a JSF page.
      */
     protected boolean isJSFPage(IResource resource) {
         // currently always return true.
         // need to find quick way of determining whether this is a JSF JSP Page
         return true;
     }
     
     /**
      * @param webApp
      * @return the default file extension from the context param. Default is
      *         "jsp" if no context param.
      */
     protected String getDefaultSuffix(Object webApp) {
     	String contextParam = null;
     	if(isJavaEE(webApp)) {
     		contextParam = JEEUtils.getContextParam((org.eclipse.jst.javaee.web.WebApp) webApp, JSF_DEFAULT_SUFFIX_CONTEXT_PARAM);
     	}
     	else {
     		contextParam = J2EEUtils.getContextParam((org.eclipse.jst.j2ee.webapplication.WebApp) webApp, JSF_DEFAULT_SUFFIX_CONTEXT_PARAM);
     	}
     	if(contextParam == null) {
     		return getDefaultDefaultSuffix();
     	}
    		return normalizeSuffix(contextParam);
     }
     
     /**
      * @return the default value for the default mapping suffix
      */
     protected String getDefaultDefaultSuffix()
     {
         return DEFAULT_DEFAULT_MAPPING_SUFFIX;
     }
 
     /**
      * @param name 
      * @param value 
      * @return the 
      */
     protected final String calculateSuffix(final String name, final String value)
     {
         if (name != null
                 && JSF_DEFAULT_SUFFIX_CONTEXT_PARAM.equals(name
                         .trim()))
         {
             return normalizeSuffix(value != null ? value.trim() : null);
         }
         return null;
     }
 
     /**
      * @param defSuffix
      * @return the suffix value with any leading dot removed
      */
     protected final String normalizeSuffix(String defSuffix)
     {
         if (defSuffix != null && defSuffix.startsWith(".")) //$NON-NLS-1$
         {
             defSuffix = defSuffix.substring(1);
         }
         return defSuffix;
     }
 
     /**
      * Holds all the obsolete JSF Library stuff.  This will go away post-Helios.
      * @author cbateman
      *
      */
     public static class JSFLibraryHandler
     {
         /**
          * Construct an array that hold paths for all JARs in a JSF library. 
          * 
          * @param jsfLib
          * @param logMissingJar true to log an error for each invalid JAR.
          * @return elements
          */
         public final IPath[] getJARPathforJSFLib(JSFLibrary jsfLib, boolean logMissingJar) {        
             EList archiveFiles = jsfLib.getArchiveFiles();
             int numJars = archiveFiles.size();
             IPath[] elements = new IPath[numJars];
             ArchiveFile ar = null;
             for (int i= 0; i < numJars; i++) {
                 ar = (ArchiveFile)archiveFiles.get(i); 
                 if ( !ar.exists() && logMissingJar ) {
                     logErroronMissingJAR(jsfLib, ar);
                 }
                 elements[i] = new Path(((ArchiveFile)archiveFiles.get(i)).getResolvedSourceLocation()).makeAbsolute();
             }
             return elements;
         }   
         
         private int numberofValidJar(EList archiveFiles) {
             int total = 0;
             final Iterator it = archiveFiles.iterator();
             ArchiveFile ar = null;
             while(it.hasNext()) {
                 ar = (ArchiveFile) it.next();
                 if (ar.exists()) {
                     total++;
                 }
             }
             return total;
         }
         
         private void logErroronMissingJAR(JSFLibrary jsfLib, ArchiveFile ar) {
             String msg = NLS.bind(Messages.JSFUtils_MissingJAR, 
                             ar.getName(),
                             jsfLib.getLabel());
             JSFCorePlugin.log(IStatus.ERROR, msg);
         }
         
         /**
          * Construct an array that hold paths for all JARs in a JSF library. 
          * However, archive files that no longer exist are filtered out.  
          * 
          * @param jsfLib
          * @param logMissingJar true to log an error for each invalid JAR.
          * @return elements
          */
         public final IPath[] getJARPathforJSFLibwFilterMissingJars(JSFLibrary jsfLib, boolean logMissingJar) {
             EList archiveFiles = jsfLib.getArchiveFiles();
             int numJars = numberofValidJar(archiveFiles);
             IPath[] elements = new IPath[numJars];
             ArchiveFile ar = null;
             int idxValidJar = 0;
             for (int i= 0; i < archiveFiles.size(); i++) {
                 ar = (ArchiveFile)archiveFiles.get(i); 
                 if ( !ar.exists() ) {
                     if (logMissingJar) {
                         logErroronMissingJAR(jsfLib, ar);
                     }
                 } else {
                     elements[idxValidJar] = new Path(((ArchiveFile)archiveFiles.get(i)).getResolvedSourceLocation()).makeAbsolute();
                     idxValidJar++;
                 }
             }
             return elements;        
         }
 
     }
     
 	/**
 	 * Finds and returns a JSF Servlet definition, or null if servlet is not defined.
 	 * 
 	 * @param webApp
 	 * @return Servlet or null
 	 */    
     protected Object findJSFServlet(Object webApp) {
     	if(isJavaEE(webApp)) {
     		return JEEUtils.findServlet((org.eclipse.jst.javaee.web.WebApp) webApp, JSF_SERVLET_CLASS);
     	}
    		return J2EEUtils.findServlet((org.eclipse.jst.j2ee.webapplication.WebApp) webApp, JSF_SERVLET_CLASS);
     }
 
     /**
      * Creates servlet reference in WebApp if not present or updates servlet
      * name if found using the passed configuration.
      * 
      * @param webApp
      * @param config
      * @param servlet
      * @return Servlet servlet - if passed servlet was null, will return created
      *         servlet
      */
     protected Object createOrUpdateServletRef(final Object webApp,
             final IDataModel config, Object servlet)
     {
         String displayName = getDisplayName(config);
         String className = getServletClassname(config);
     	if(isJavaEE(webApp)) {
     		return JEEUtils.createOrUpdateServletRef((org.eclipse.jst.javaee.web.WebApp) webApp, displayName, className, (org.eclipse.jst.javaee.web.Servlet) servlet);
     	}
    		return J2EEUtils.createOrUpdateServletRef((org.eclipse.jst.j2ee.webapplication.WebApp) webApp, displayName, className, (org.eclipse.jst.j2ee.webapplication.Servlet) servlet);
     }
 
     /**
      * Creates servlet-mappings for the servlet for 2.5 WebModules or greated
      * 
      * @param webApp
      * @param urlMappingList
      *            - list of string values to be used in url-pattern for
      *            servlet-mapping
      * @param servlet
      */
     protected void setUpURLMappings(final Object webApp,
             final List<String> urlMappingList, final Object servlet)
     {
     	if(isJavaEE(webApp)) {
     		JEEUtils.setUpURLMappings((org.eclipse.jst.javaee.web.WebApp) webApp, urlMappingList, (org.eclipse.jst.javaee.web.Servlet) servlet);
     	}
     	else {
     		J2EEUtils.setUpURLMappings((org.eclipse.jst.j2ee.webapplication.WebApp) webApp, urlMappingList, (org.eclipse.jst.j2ee.webapplication.Servlet) servlet);
     	}
     }
 
 	/**
 	 * Removes servlet-mappings for servlet using servlet-name for >= 2.5 WebModules.
 	 * @param webApp
 	 * @param servlet
 	 */
 	protected void removeURLMappings(final Object webApp, final Object servlet) {
     	if(isJavaEE(webApp)) {
     		JEEUtils.removeURLMappings((org.eclipse.jst.javaee.web.WebApp) webApp, (org.eclipse.jst.javaee.web.Servlet) servlet);
     	}
     	else {
     		J2EEUtils.removeURLMappings((org.eclipse.jst.j2ee.webapplication.WebApp) webApp, (org.eclipse.jst.j2ee.webapplication.Servlet) servlet);
     	}
 	}
 	
 	/**
 	 * Removes servlet definition
 	 * @param webApp
 	 * @param servlet
 	 */
 	protected void removeJSFServlet(final Object webApp, final Object servlet) {
     	if(isJavaEE(webApp)) {
     		JEEUtils.removeServlet((org.eclipse.jst.javaee.web.WebApp) webApp, (org.eclipse.jst.javaee.web.Servlet) servlet);
     	}
     	else {
     		J2EEUtils.removeServlet((org.eclipse.jst.j2ee.webapplication.WebApp) webApp, (org.eclipse.jst.j2ee.webapplication.Servlet) servlet);
     	}
 	}
 	
     /**
      * Removes context-param
      * @param webApp
      */
     protected void removeJSFContextParams(final Object webApp) {
     	if(isJavaEE(webApp)) {
     		JEEUtils.removeContextParam((org.eclipse.jst.javaee.web.WebApp) webApp, JSF_CONFIG_CONTEXT_PARAM);
     	}
     	else {
     		J2EEUtils.removeContextParam((org.eclipse.jst.j2ee.webapplication.WebApp) webApp, JSF_CONFIG_CONTEXT_PARAM);
     	}
     }
     
 	/**
 	 * Creates or updates context-params
 	 * @param webApp
 	 * @param config
 	 */
 	protected void setupContextParams(final Object webApp, final IDataModel config) {
         final String paramValue = config.getStringProperty(IJSFFacetInstallDataModelProperties.CONFIG_PATH);
         if (paramValue != null && !paramValue.equals(JSF_DEFAULT_CONFIG_PATH)) {
         	if(isJavaEE(webApp)) {
         		JEEUtils.setupContextParam((org.eclipse.jst.javaee.web.WebApp) webApp, JSF_CONFIG_CONTEXT_PARAM, paramValue);
         	}
         	else {
         		J2EEUtils.setupContextParam((org.eclipse.jst.j2ee.webapplication.WebApp) webApp, JSF_CONFIG_CONTEXT_PARAM, paramValue);
         	}
         }
 
 	}
 	
     /**
      * @param map
      * @param webApp
      * @return extension from map. Will return null if file extension not found
      *         in url patterns.
      */
     protected String getFileExtensionFromMap(final Object webApp, final Object map) {
     	if(isJavaEE(webApp)) {
     		return JEEUtils.getFileExtensionFromMap((org.eclipse.jst.javaee.web.ServletMapping) map);
     	}
    		return J2EEUtils.getFileExtensionFromMap((org.eclipse.jst.j2ee.webapplication.ServletMapping) map);
     }
 
     /**
      * @param webApp 
      * @param map
      * @return prefix mapping. may return null.
      */
     protected String getPrefixMapping(final Object webApp, final Object map) {
     	if(isJavaEE(webApp)) {
     		return JEEUtils.getPrefixMapping((org.eclipse.jst.javaee.web.ServletMapping) map);
     	}
    		return J2EEUtils.getPrefixMapping((org.eclipse.jst.j2ee.webapplication.ServletMapping) map);
     }
     
     /**
      * @param webAppObj
      * @param resource
      * @param existingURL
      * @return the modified url path for the (possibly) jsf resource.
      */
     public IPath getFileUrlPath(Object webAppObj, IResource resource,
             IPath existingURL) {
         // if not a JSF page, do nothing
         if (!isJSFPage(resource))
         {
             return null;
         }
 
         Object servlet = findJSFServlet(webAppObj);
         if (servlet == null)
         {// if no faces servlet, do nothing
             return null;
         }
 
         String defaultSuffix = getDefaultSuffix(webAppObj);
         // is the resource using default_suffix
         String fileExtension = resource.getFileExtension();
         boolean canUseExtensionMapping = fileExtension != null && fileExtension.equalsIgnoreCase(defaultSuffix);
         // if not using default extension and is not a known file extension,
         // then we will abort
         if (!canUseExtensionMapping
                 && !isValidKnownExtension(resource.getFileExtension()))
             return null;
 
     	if(isJavaEE(webAppObj)) {
     		org.eclipse.jst.javaee.web.WebApp webApp =  (org.eclipse.jst.javaee.web.WebApp) webAppObj;
 
             final String servletName = ((org.eclipse.jst.javaee.web.Servlet) servlet).getServletName();
 
             String foundFileExtension = null;
             for (final org.eclipse.jst.javaee.web.ServletMapping map : webApp.getServletMappings())
             {
                 if (map != null &&
                         map.getServletName() != null &&
                         map.getServletName().trim().equals(servletName.trim()))
                 {
                     foundFileExtension = getFileExtensionFromMap(webAppObj, map);
                     if (foundFileExtension != null && canUseExtensionMapping)
                     {
                         return existingURL.removeFileExtension()
                                 .addFileExtension(foundFileExtension);
                     }
 
                     String foundPrefixMapping = getPrefixMapping(webAppObj, map);
                     if (foundPrefixMapping != null)
                     {
                         return new Path(foundPrefixMapping).append(existingURL);
                     }
                 }
             }
 
             if (!canUseExtensionMapping && foundFileExtension != null)
             {
                 // we could prompt user that this may not work...
                 // for now we will return the extension mapping
                 return existingURL.removeFileExtension().addFileExtension(
                         foundFileExtension);
             }
 
             // we could, at this point, add a url mapping to the faces servlet,
             // or prompt user that it may be a good idea to add one... 
 
     	}
     	else {
             Iterator mappings = ((org.eclipse.jst.j2ee.webapplication.Servlet)servlet).getMappings().iterator();
             org.eclipse.jst.j2ee.webapplication.ServletMapping map = null;
             String foundFileExtension = null;
             String foundPrefixMapping = null;
             while (mappings.hasNext())
             {
                 map = (org.eclipse.jst.j2ee.webapplication.ServletMapping)mappings.next();
 
                 foundFileExtension = getFileExtensionFromMap(webAppObj, map);
                 if (foundFileExtension != null && canUseExtensionMapping) 
                 {
                     return existingURL.removeFileExtension().addFileExtension(foundFileExtension);
                 }
                 
                 if (foundPrefixMapping == null)
                 {
                     foundPrefixMapping = getPrefixMapping(webAppObj, map);
                 }
             }
             if (foundPrefixMapping != null)
             {
                 return new Path(foundPrefixMapping).append(existingURL); 
             }
             if (! canUseExtensionMapping && foundFileExtension != null){
                 //we could prompt user that this may not work...
                 //for now we will return the extension mapping
                 return existingURL.removeFileExtension().addFileExtension(foundFileExtension);
             }
             
             // we could, at this point, add a url mapping to the faces servlet, 
             // or prompt user that it may be a good idea to add one...
 
     	}
     	return null;
     }
 }
