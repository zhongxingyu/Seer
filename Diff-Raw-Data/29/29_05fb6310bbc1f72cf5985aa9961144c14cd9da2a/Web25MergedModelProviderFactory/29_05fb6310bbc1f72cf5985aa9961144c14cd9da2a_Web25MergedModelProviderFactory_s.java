 /***********************************************************************
  * Copyright (c) 2008 by SAP AG, Walldorf. 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     SAP AG - initial API and implementation
  ***********************************************************************/
 package org.eclipse.jst.jee.model.internal;
 
 import java.util.HashMap;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.jst.j2ee.model.IModelProvider;
 import org.eclipse.jst.j2ee.model.IModelProviderFactory;
 import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
 
 /**
  * @author Kiril Mitov k.mitov@sap.com
  * 
  */
 public class Web25MergedModelProviderFactory implements IModelProviderFactory {
 
 	private HashMap<IProject, IModelProvider> xmlResources = new HashMap<IProject, IModelProvider>();
 
 	public IModelProvider create(IProject project) {
 		IModelProvider result = getResource(project);
 		if(result == null || ((Web25MergedModelProvider)result).isDisposed()){
 			result = new Web25MergedModelProvider(project);
 			addResource(project, result);
 		}
 		return result;
 	}
 
 	public IModelProvider create(IVirtualComponent component) {
 		return create(component.getProject());
 	}
 
 	private void addResource(IProject project, IModelProvider modelProvider){
 		xmlResources.put(project, modelProvider);
 	}
 
 	private IModelProvider getResource(IProject project){
 		return xmlResources.get(project);
 	}
 
 }
