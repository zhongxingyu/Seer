 /*******************************************************************************
  * Copyright (c) 2003, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.j2ee.web.project.facet;
 
 import java.util.Set;
 
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.jst.common.frameworks.CommonFrameworksPlugin;
 import org.eclipse.jst.common.project.facet.IJavaFacetInstallDataModelProperties;
 import org.eclipse.jst.common.project.facet.JavaFacetUtils;
 import org.eclipse.jst.j2ee.internal.J2EEConstants;
 import org.eclipse.jst.j2ee.internal.common.J2EEVersionUtil;
 import org.eclipse.jst.j2ee.internal.plugin.IJ2EEModuleConstants;
 import org.eclipse.jst.j2ee.internal.plugin.J2EEPlugin;
 import org.eclipse.jst.j2ee.internal.plugin.J2EEPreferences;
 import org.eclipse.jst.j2ee.internal.project.ProjectSupportResourceHandler;
 import org.eclipse.jst.j2ee.project.facet.J2EEModuleFacetInstallDataModelProvider;
 import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
 import org.eclipse.wst.common.project.facet.core.IFacetedProject;
 import org.eclipse.wst.common.project.facet.core.IFacetedProjectWorkingCopy;
 import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
 import org.eclipse.wst.project.facet.ProductManager;
 
 import com.ibm.icu.util.StringTokenizer;
 
 public class WebFacetInstallDataModelProvider extends J2EEModuleFacetInstallDataModelProvider implements IWebFacetInstallDataModelProperties {
 
 	public Set getPropertyNames() {
 		Set names = super.getPropertyNames();
 		names.add(CONTEXT_ROOT);
 		names.add(SOURCE_FOLDER);
 		return names;
 	}
 
 	public Object getDefaultProperty(String propertyName) {
 		if (propertyName.equals(CONFIG_FOLDER)) {
 			return J2EEPlugin.getDefault().getJ2EEPreferences().getString(J2EEPreferences.Keys.WEB_CONTENT_FOLDER);
 		} else if (propertyName.equals(SOURCE_FOLDER)) {
 			return CommonFrameworksPlugin.getDefault().getPluginPreferences().getString(CommonFrameworksPlugin.DEFAULT_SOURCE_FOLDER);
 		} else if (propertyName.equals(CONTEXT_ROOT)) {
 			return getStringProperty(FACET_PROJECT_NAME).replace(' ', '_');
 		} else if (propertyName.equals(FACET_ID)) {
 			return DYNAMIC_WEB;
 		} else if (propertyName.equals(MODULE_URI)) {
 			String projectName = model.getStringProperty(FACET_PROJECT_NAME).replace(' ', '_');
 			return projectName + IJ2EEModuleConstants.WAR_EXT;
 		} else if (propertyName.equals(GENERATE_DD)) {
 			IProjectFacetVersion facetVersion = (IProjectFacetVersion)getProperty(FACET_VERSION);
 			if(facetVersion == WebFacetUtils.WEB_25){
 				return Boolean.valueOf(J2EEPlugin.getDefault().getJ2EEPreferences().getBoolean(J2EEPreferences.Keys.DYNAMIC_WEB_GENERATE_DD));
 			}
 			return Boolean.TRUE;
 		}
 		return super.getDefaultProperty(propertyName);
 	}
 
 	public boolean propertySet(String propertyName, Object propertyValue) {
 		if (ADD_TO_EAR.equals(propertyName)) {
 			model.notifyPropertyChange(CONTEXT_ROOT, IDataModel.ENABLE_CHG);
 		} else if (FACET_PROJECT_NAME.equals(propertyName)) {
 			model.notifyPropertyChange(CONTEXT_ROOT, IDataModel.VALID_VALUES_CHG);
 		} else if (propertyName.equals(CONFIG_FOLDER)) {
 			// If using optimized single root structure, update the output folder based on content folder change
 			// The output folder will be "<contentRoot>/WEB-INF/classes"
 			if (ProductManager.shouldUseSingleRootStructure()) 
 			{
 	            final IDataModel javaModel = findJavaFacetInstallDataModel();
 	            
 	            if( javaModel != null )
 	            {
 	                javaModel.setProperty(IJavaFacetInstallDataModelProperties.DEFAULT_OUTPUT_FOLDER_NAME,propertyValue+"/"+J2EEConstants.WEB_INF_CLASSES); //$NON-NLS-1$
 	            }
 			}
 			return true;
 		} else if (propertyName.equals(SOURCE_FOLDER)) 
 		{
 		    final IDataModel javaModel = findJavaFacetInstallDataModel();
 		    
 		    if( javaModel != null )
 		    {
 		        javaModel.setProperty(IJavaFacetInstallDataModelProperties.SOURCE_FOLDER_NAME, propertyValue);
 			}
 		}
 		return super.propertySet(propertyName, propertyValue);
 	}
 	
 	private IDataModel findJavaFacetInstallDataModel()
 	{
         final IFacetedProjectWorkingCopy fpjwc 
             = (IFacetedProjectWorkingCopy) this.model.getProperty( FACETED_PROJECT_WORKING_COPY );
         
        final IFacetedProject.Action javaInstallAction
            = fpjwc.getProjectFacetAction( JavaFacetUtils.JAVA_FACET );
         
        return (IDataModel) javaInstallAction.getConfig();
 	}
 
 	public boolean isPropertyEnabled(String propertyName) {
 		return super.isPropertyEnabled(propertyName);
 	}
 
 	protected int convertFacetVersionToJ2EEVersion(IProjectFacetVersion version) {
 		return J2EEVersionUtil.convertWebVersionStringToJ2EEVersionID(version.getVersionString());
 	}
 
 	public IStatus validate(String name) {
 		if (name.equals(CONTEXT_ROOT)) {
 			return validateContextRoot(getStringProperty(CONTEXT_ROOT));
 		} else if (name.equals(SOURCE_FOLDER)) {
 			return validateFolderName(getStringProperty(SOURCE_FOLDER));
 		}
 		// the superclass validates the content directory which is actually a "CONFIG_FOLDER"
 		return super.validate(name);
 	}
 
 	protected IStatus validateContextRoot(String contextRoot) {
 		if (contextRoot == null) {
 			return J2EEPlugin.newErrorStatus(ProjectSupportResourceHandler.getString(ProjectSupportResourceHandler.Context_Root_cannot_be_empty_2, new Object[]{contextRoot}), null);
 		} else if (contextRoot.trim().equals(contextRoot)) {
 			StringTokenizer stok = new StringTokenizer(contextRoot, "."); //$NON-NLS-1$
 			while (stok.hasMoreTokens()) {
 				String token = stok.nextToken();
 				for (int i = 0; i < token.length(); i++) {
 					if (!(token.charAt(i) == '_') && !(token.charAt(i) == '-') && !(token.charAt(i) == '/') && Character.isLetterOrDigit(token.charAt(i)) == false) {
 						Object[] invalidChar = new Object[]{(new Character(token.charAt(i))).toString()};
 						String errorStatus = ProjectSupportResourceHandler.getString(ProjectSupportResourceHandler.The_character_is_invalid_in_a_context_root, invalidChar); //$NON-NLS-1$
 						return J2EEPlugin.newErrorStatus(errorStatus, null);
 					}
 				}
 			}
 		} else
 			return J2EEPlugin.newErrorStatus(ProjectSupportResourceHandler.getString(ProjectSupportResourceHandler.Names_cannot_begin_or_end_with_whitespace_5, new Object[]{contextRoot}), null); //$NON-NLS-1$
 		return OK_STATUS;
 	}
 }
