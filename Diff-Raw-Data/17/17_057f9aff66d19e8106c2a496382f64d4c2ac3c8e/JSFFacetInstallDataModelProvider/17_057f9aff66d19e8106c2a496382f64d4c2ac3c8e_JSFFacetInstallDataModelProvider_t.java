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
 
 import java.util.ArrayList;
import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jst.j2ee.web.componentcore.util.WebArtifactEdit;
 import org.eclipse.jst.jsf.core.internal.JSFCorePlugin;
 import org.eclipse.jst.jsf.core.internal.Messages;
 import org.eclipse.jst.jsf.core.internal.jsflibraryconfig.JSFLibraryDecorator;
 import org.eclipse.jst.jsf.core.internal.jsflibraryregistry.JSFLibrary;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.wst.common.componentcore.datamodel.FacetInstallDataModelProvider;
 import org.eclipse.wst.common.componentcore.datamodel.properties.IFacetDataModelProperties;
 import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
 import org.eclipse.wst.common.frameworks.internal.operations.IProjectCreationPropertiesNew;
 import org.eclipse.wst.common.frameworks.internal.plugin.WTPCommonPlugin;
 
 /**
  * Provides a data model used by the JSF facet install.
  * 
  * @author Gerry Kessler - Oracle
  */
 public class JSFFacetInstallDataModelProvider extends
 		FacetInstallDataModelProvider implements
 		IJSFFacetInstallDataModelProperties {
 
 	private String 	errorMessage;
 	private IPath 	webContentPath;
 	
 	public Set getPropertyNames() {
 		Set names = super.getPropertyNames();
 		names.add(IMPLEMENTATION);
 		names.add(DEPLOY_IMPLEMENTATION);
 		names.add(CONFIG_PATH);
 		names.add(SERVLET_NAME);
 		names.add(SERVLET_URL_PATTERNS);
 		names.add(WEBCONTENT_DIR);
 		
 		names.add(IMPLEMENTATION_LIBRARIES);
 		names.add(COMPONENT_LIBRARIES);
 		names.add(DEFAULT_IMPLEMENTATION_LIBRARY);
 		
 		return names;
 	}
 
 	public Object getDefaultProperty(String propertyName) {
 		if (propertyName.equals(IMPLEMENTATION)) {
 			if (JSFCorePlugin.getDefault().getJSFLibraryRegistry() == null)
 				return null;
 			return JSFCorePlugin.getDefault().getJSFLibraryRegistry().getDefaultImplementation();
 		} else if (propertyName.equals(DEPLOY_IMPLEMENTATION)) {
 			return Boolean.TRUE;
 		} else if (propertyName.equals(CONFIG_PATH)) {
 			return JSFUtils.JSF_DEFAULT_CONFIG_PATH; //$NON-NLS-1$;
 		} else if (propertyName.equals(SERVLET_NAME)) {
 			return "Faces Servlet"; //$NON-NLS-1$
 		} else if (propertyName.equals(SERVLET_URL_PATTERNS)) {
 			return new String[] { "*.faces" }; //$NON-NLS-1$
 		} else if (propertyName.equals(FACET_ID)) {
 			return JSFCorePlugin.FACET_ID;
 		} else if (propertyName.equals(WEBCONTENT_DIR)){
 			return "WebContent";  //not sure I need this
 		} else if (propertyName.equals(COMPONENT_LIBRARIES)) {
 			return getDefaultJSFComponentLibraries();
 		} else if (propertyName.equals(IMPLEMENTATION_LIBRARIES)) {
 			return getDefaultJSFImplementationLibraries();
 		} else if (propertyName.equals(DEFAULT_IMPLEMENTATION_LIBRARY)) {
 			return getDefaultImplementationLibrary();
 		}
 		return super.getDefaultProperty(propertyName);
 	}
 	public IStatus validate(String name) {
 		errorMessage = null;
 		if (name.equals(IMPLEMENTATION) && getBooleanProperty(DEPLOY_IMPLEMENTATION)) {
 			return validateImpl((JSFLibrary)getProperty(IMPLEMENTATION));
 		} else if (name.equals(CONFIG_PATH)) {
 			return validateConfigLocation(getStringProperty(CONFIG_PATH));
 		} else if (name.equals(SERVLET_NAME)) {			
 			return validateServletName(getStringProperty(SERVLET_NAME));
 		}
 		return super.validate(name);
 	}
 	
 	private IStatus validateServletName(String servletName) {
 		if (servletName == null || servletName.trim().length() == 0) {
 			errorMessage = Messages.JSFFacetInstallDataModelProvider_ValidateServletName;
 			return WTPCommonPlugin.createErrorStatus(errorMessage);				
 		}
 		else 
 			return OK_STATUS;
 	}
 
 	private IStatus validateImpl(JSFLibrary impl) {
 		if (impl == null) {
 			errorMessage = Messages.JSFFacetInstallDataModelProvider_ValidateJSFImpl; 
 		}
 		if (errorMessage != null)
 			return WTPCommonPlugin.createErrorStatus(errorMessage);
 		else
 			return OK_STATUS;
 	}
 	
 	private IStatus validateConfigLocation(String text) {
 		if (text == null || text.trim().equals("")) { //$NON-NLS-1$
 			errorMessage = Messages.JSFFacetInstallDataModelProvider_ValidateConfigFileEmpty;
 			return WTPCommonPlugin.createErrorStatus(errorMessage);
 		}
 		text = text.trim();
 		
 		if (getProjectPath() == null) //this circumstance occurs on page init
 			return OK_STATUS;
 		
 		IPath fullPath = getProjectPath().append(text);
 		IPath passedPath = new Path(text);
 		if (!fullPath.isValidPath(text)){
 			errorMessage = Messages.JSFFacetInstallDataModelProvider_ValidateConfigFilePath;
 			return WTPCommonPlugin.createErrorStatus(errorMessage);
 		}
 		
 		//FIXME:  check for valid file path also [passedPath.toFile().isFile()] 
 		if (text.toLowerCase().lastIndexOf(".xml") != text.length() - 4) { //$NON-NLS-1$
 			errorMessage = Messages.JSFFacetInstallDataModelProvider_ValidateConfigFileXML;
 			return WTPCommonPlugin.createErrorStatus(errorMessage);
 		}
 		
 		if (text.lastIndexOf("\\") >= 0){ //$NON-NLS-1$
 			errorMessage = Messages.JSFFacetInstallDataModelProvider_ValidateConfigFileSlashes;
 			return WTPCommonPlugin.createErrorStatus(errorMessage);
 		} 
 //		if (1 == 1){
 //			//FIXME!!!!
 //			//until i can figure out how to get WebContent dir from the WebApp config,
 //			//skip validation here
 //			return OK_STATUS;
 //		}
 		
 		if (passedPath.getDevice() != null) {
 			errorMessage = NLS.bind(
 					Messages.JSFFacetInstallDataModelProvider_ValidateConfigFileRelative1,
 					getWebContentFolder().removeFirstSegments(getWebContentFolder().segmentCount() - 1).toString());
 			return WTPCommonPlugin.createErrorStatus(errorMessage);
 		}
 
 		IPath setPath = getWebContentFolder().append(passedPath);
 		if (!getWebContentFolder().isPrefixOf(setPath)) {
 			errorMessage = NLS.bind(
 					Messages.JSFFacetInstallDataModelProvider_ValidateConfigFileRelative2,
 					getWebContentFolder().removeFirstSegments(getWebContentFolder().segmentCount() - 1).toString());
 			return WTPCommonPlugin.createErrorStatus(errorMessage);
 		}
 
 		return OK_STATUS;
 	}
 	
 	private IPath getProjectPath() {
 		String projName = (String)getProperty(FACET_PROJECT_NAME);
 		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projName);
 		if (project.exists()){
 			return project.getLocation();
 		} else {
 			IDataModel projModel = (IDataModel)getProperty(MASTER_PROJECT_DM);
 			if (projModel.getBooleanProperty(IProjectCreationPropertiesNew.USE_DEFAULT_LOCATION)){
 				return new Path(projModel.getStringProperty(IProjectCreationPropertiesNew.PROJECT_LOCATION)).append(projName);
 			}
 			else {
 				return new Path(projModel.getStringProperty(IProjectCreationPropertiesNew.USER_DEFINED_LOCATION)).append(projName);
 			}	
 		}
 	}
 
 	private IPath getWebContentFolder() {
 		//One can get here 2 ways:
 		//if web app exists and user is adding a facet, or
 		// if creating a new web app.   
 		if (webContentPath != null)
 			return webContentPath;
 		
 		WebArtifactEdit webApp = null;
 		try {
 			String projName = model.getStringProperty(IFacetDataModelProperties.FACET_PROJECT_NAME);
 			IProject proj = ResourcesPlugin.getWorkspace().getRoot()
 					.getProject(projName);
 			webApp = JSFUtils.getWebArtifactEditForRead(proj);
 			if (webApp != null) {
 				webContentPath = JSFUtils.getWebArtifactEditForRead(proj)
 					.getDeploymentDescriptorPath().removeLastSegments(2);
 				return webContentPath;
 			} else {
 				webContentPath = new Path(getStringProperty(WEBCONTENT_DIR));
 				return webContentPath;
 			}
 				
 		} finally {
 			if (webApp != null) {
 				webApp.dispose();
 			}
 		}
 
 	}
 
 	
 	private List getDefaultJSFComponentLibraries() {
 		List list = new ArrayList();		
 		JSFLibraryDecorator prjJSFLib = null;
 		JSFLibrary jsfLib = null;
 		if (JSFCorePlugin.getDefault().getJSFLibraryRegistry() != null) {
 			Iterator it = JSFCorePlugin.getDefault().getJSFLibraryRegistry().getNonImplJSFLibraries().iterator();
 			boolean selected = false;	// not selected when project is created
 			while (it.hasNext()) {
 				jsfLib = (JSFLibrary)it.next(); 
 				prjJSFLib = new JSFLibraryDecorator(jsfLib, selected, true);
 			}
 		}
 		return list;
 	}	
 
	private List getDefaultJSFImplementationLibraries() {
		List list = new ArrayList(Collections.EMPTY_LIST);
		
 		if (JSFCorePlugin.getDefault().getJSFLibraryRegistry() != null) {
			JSFLibrary jsfLib = JSFCorePlugin.getDefault().getJSFLibraryRegistry().getDefaultImplementation();
			JSFLibraryDecorator prjJSFLib = new JSFLibraryDecorator(jsfLib, true, true);
 			list.add(prjJSFLib);			
 		}
 		return list;
 	}	
 	
 	private JSFLibraryDecorator getDefaultImplementationLibrary() {		
 		if (JSFCorePlugin.getDefault().getJSFLibraryRegistry() != null) {
 			JSFLibrary jsfLib = JSFCorePlugin.getDefault().getJSFLibraryRegistry().getDefaultImplementation();
			return new JSFLibraryDecorator(jsfLib, true, true);	
 		}
 		return null;	
 	}	
 	
 }
