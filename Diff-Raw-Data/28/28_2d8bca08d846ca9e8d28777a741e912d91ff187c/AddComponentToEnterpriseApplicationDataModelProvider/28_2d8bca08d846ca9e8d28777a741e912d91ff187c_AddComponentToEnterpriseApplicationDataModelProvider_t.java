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
 package org.eclipse.jst.j2ee.application.internal.operations;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.jst.j2ee.internal.project.J2EEProjectUtilities;
 import org.eclipse.wst.common.componentcore.internal.operation.CreateReferenceComponentsDataModelProvider;
 import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
 import org.eclipse.wst.common.frameworks.datamodel.IDataModelOperation;
 
 public class AddComponentToEnterpriseApplicationDataModelProvider extends CreateReferenceComponentsDataModelProvider implements IAddComponentToEnterpriseApplicationDataModelProperties {
 
 	public AddComponentToEnterpriseApplicationDataModelProvider() {
 		super();
 	}
 
 	public Object getDefaultProperty(String propertyName) {
 		if (TARGET_COMPONENTS_TO_URI_MAP.equals(propertyName)) {
 			Map map = new HashMap();
 			List components = (List) getProperty(TARGET_COMPONENT_LIST);
 			for (int i = 0; i < components.size(); i++) {
 				IVirtualComponent component = (IVirtualComponent) components.get(i);
 				IProject project = component.getProject();
 				String name = component.getName();
				if(name != null)
					name = name.replace(' ','_');
				
 				if (J2EEProjectUtilities.isDynamicWebProject(project)) {
 					name += IModuleExtensions.DOT_WAR;
 				} else if (J2EEProjectUtilities.isJCAProject(project)) {
 					name += IModuleExtensions.DOT_RAR;
 				} else {
 					name += IModuleExtensions.DOT_JAR;
 				}
 				map.put(component, name);
 			}
 			setProperty(propertyName, map);
 			return map;
 		}
 		return super.getDefaultProperty(propertyName);
 	}
 
 
 	public IDataModelOperation getDefaultOperation() {
 		return new AddComponentToEnterpriseApplicationOp(model);
 	}
 
 }
