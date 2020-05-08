 /*******************************************************************************
  * Copyright (c) 2005 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.j2ee.project.facet;
 
 import java.util.Set;
 
 import org.eclipse.jst.j2ee.internal.common.CreationConstants;
 import org.eclipse.jst.j2ee.internal.common.J2EEVersionUtil;
 import org.eclipse.jst.j2ee.internal.plugin.IJ2EEModuleConstants;
 import org.eclipse.jst.j2ee.internal.project.J2EEProjectUtilities;
 import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
 
 public class AppClientFacetInstallDataModelProvider extends J2EEModuleFacetInstallDataModelProvider implements IAppClientFacetInstallDataModelProperties {
 
 	public AppClientFacetInstallDataModelProvider() {
 		super();
 	}
 
 	public Set getPropertyNames() {
 		Set names = super.getPropertyNames();
 		names.add(CREATE_DEFAULT_MAIN_CLASS);
 		return names;
 	}
 
 	public Object getDefaultProperty(String propertyName) {
 		if (propertyName.equals(FACET_ID))
 			return J2EEProjectUtilities.APPLICATION_CLIENT;
 		else if (propertyName.equals(CREATE_DEFAULT_MAIN_CLASS))
 			return Boolean.TRUE;
 		else if (propertyName.equals(CONFIG_FOLDER))
 			return CreationConstants.DEFAULT_APPCLIENT_SOURCE_FOLDER;
 		else if (propertyName.equals(MODULE_URI)) {
			String projectName = model.getStringProperty(FACET_PROJECT_NAME).replace(' ','_');
 			return projectName + IJ2EEModuleConstants.JAR_EXT; 
 		}
 		return super.getDefaultProperty(propertyName);
 	}
 	
 	protected int convertFacetVersionToJ2EEVersion(IProjectFacetVersion version) {
 		return J2EEVersionUtil.convertAppClientVersionStringToJ2EEVersionID(version.getVersionString());
 	}
 }
