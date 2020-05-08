 /*******************************************************************************
  * Copyright (c) 2003, 2005 IBM Corporation and others.
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
 import org.eclipse.jst.common.project.facet.IJavaFacetInstallDataModelProperties;
 import org.eclipse.jst.j2ee.internal.common.J2EEVersionUtil;
 import org.eclipse.jst.j2ee.internal.plugin.IJ2EEModuleConstants;
 import org.eclipse.jst.j2ee.internal.plugin.J2EEPlugin;
 import org.eclipse.jst.j2ee.internal.project.J2EEProjectUtilities;
 import org.eclipse.jst.j2ee.internal.project.ProjectSupportResourceHandler;
 import org.eclipse.jst.j2ee.project.facet.J2EEModuleFacetInstallDataModelProvider;
 import org.eclipse.wst.common.componentcore.datamodel.properties.IFacetProjectCreationDataModelProperties;
 import org.eclipse.wst.common.componentcore.datamodel.properties.IFacetProjectCreationDataModelProperties.FacetDataModelMap;
 import org.eclipse.wst.common.componentcore.internal.util.IModuleConstants;
 import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
 import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
 import org.eclipse.wst.project.facet.IProductConstants;
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
 			return ProductManager.getProperty(IProductConstants.WEB_CONTENT_FOLDER);
 		} else if (propertyName.equals(SOURCE_FOLDER)) {
 			return "src"; //$NON-NLS-1$
 		} else if (propertyName.equals(CONTEXT_ROOT)) {
 			return getProperty(FACET_PROJECT_NAME);
 		} else if (propertyName.equals(FACET_ID)) {
 			return J2EEProjectUtilities.DYNAMIC_WEB;
 		} else if (propertyName.equals(MODULE_URI)) {
 			String projectName = model.getStringProperty(FACET_PROJECT_NAME);
 			return projectName + IJ2EEModuleConstants.WAR_EXT; 
 		}
 		return super.getDefaultProperty(propertyName);
 	}
 
 	public boolean propertySet(String propertyName, Object propertyValue) {
 		if (ADD_TO_EAR.equals(propertyName)) {
 			model.notifyPropertyChange(CONTEXT_ROOT, IDataModel.ENABLE_CHG);
 		} else if (FACET_PROJECT_NAME.equals(propertyName)) {
 			model.notifyPropertyChange(CONTEXT_ROOT, IDataModel.VALID_VALUES_CHG);
 		} else if (propertyName.equals(CONFIG_FOLDER)) {
 			return true;
 		} else if (propertyName.equals(SOURCE_FOLDER)) {
 			IDataModel masterModel = (IDataModel) model.getProperty(MASTER_PROJECT_DM);
 			if (masterModel != null) {
 				FacetDataModelMap map = (FacetDataModelMap) masterModel.getProperty(IFacetProjectCreationDataModelProperties.FACET_DM_MAP);
 				IDataModel javaModel = map.getFacetDataModel(IModuleConstants.JST_JAVA);
 				if (javaModel != null)
 					javaModel.setProperty(IJavaFacetInstallDataModelProperties.SOURCE_FOLDER_NAME, propertyValue);
 			}
 		}
 		return super.propertySet(propertyName, propertyValue);
 	}
 
 	public boolean isPropertyEnabled(String propertyName) {
 		return super.isPropertyEnabled(propertyName);
 	}
 
 	protected int convertFacetVersionToJ2EEVersion(IProjectFacetVersion version) {
 		return J2EEVersionUtil.convertWebVersionStringToJ2EEVersionID(version.getVersionString());
 	}
 
 	public IStatus validate(String name) {
		if (name.equals(CONTEXT_ROOT) && getBooleanProperty(ADD_TO_EAR)) {
 			return validateContextRoot(getStringProperty(CONTEXT_ROOT));
 		}
 		return super.validate(name);
 	}
 
 	protected IStatus validateContextRoot(String contextRoot) {
 		if (contextRoot.equals("") || contextRoot == null) { //$NON-NLS-1$
 			return J2EEPlugin.newErrorStatus(ProjectSupportResourceHandler.getString(ProjectSupportResourceHandler.Context_Root_cannot_be_empty_2, new Object[]{contextRoot}), null); //$NON-NLS-1$
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
