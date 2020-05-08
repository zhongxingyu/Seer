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
 package org.eclipse.jst.j2ee.internal.earcreation;
 
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.jst.j2ee.internal.common.J2EEVersionUtil;
 import org.eclipse.jst.j2ee.internal.plugin.J2EEPlugin;
 import org.eclipse.jst.j2ee.internal.project.J2EEProjectUtilities;
 import org.eclipse.jst.j2ee.project.facet.J2EEFacetInstallDataModelProvider;
 import org.eclipse.wst.common.componentcore.ComponentCore;
 import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
 import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
 
 public class EarFacetInstallDataModelProvider extends J2EEFacetInstallDataModelProvider implements IEarFacetInstallDataModelProperties {
 
 	public Set getPropertyNames() {
 		Set names = super.getPropertyNames();
 		names.add(CONTENT_DIR);
 		names.add(J2EE_PROJECTS_LIST);
 		names.add(JAVA_PROJECT_LIST);
 		return names;
 	}
 
 	public Object getDefaultProperty(String propertyName) {
 		if (propertyName.equals(FACET_ID)) {
 			return J2EEProjectUtilities.ENTERPRISE_APPLICATION;
 		} else if (propertyName.equals(CONTENT_DIR)) {
 			return "EarContent";
 		} else if (propertyName.equals(J2EE_PROJECTS_LIST) || propertyName.equals(JAVA_PROJECT_LIST)) {
 			return Collections.EMPTY_LIST;
 		}
 		return super.getDefaultProperty(propertyName);
 	}
 
 	public IStatus validate(String name) {
 		if (name.equals(J2EE_PROJECTS_LIST)) {
 			return validateTargetComponentVersion((List) model.getProperty(J2EE_PROJECTS_LIST));
 		}
 		return super.validate(name);
 	}
 
 	private IStatus validateTargetComponentVersion(List list) {
 		int earVersion = getJ2EEVersion();
 		for (Iterator iter = list.iterator(); iter.hasNext();) {
 			IProject handle = (IProject) iter.next();
 			IVirtualComponent comp = ComponentCore.createComponent(handle.getProject());
			if (comp == null) return OK_STATUS; //Not a faceted project, so version not relevant
 			int compVersion = J2EEVersionUtil.convertVersionStringToInt(comp);
 			if (earVersion < compVersion) {
 				String errorStatus = "The Module specification level of " + handle.getName() + ", is incompatible with the containing EAR version"; //$NON-NLS-1$
 				return J2EEPlugin.newErrorStatus(errorStatus, null);
 			}
 		}
 		return OK_STATUS;
 	}
 
 	protected int convertFacetVersionToJ2EEVersion(IProjectFacetVersion version) {
 		return J2EEVersionUtil.convertVersionStringToInt(version.getVersionString());
 	}
 }
