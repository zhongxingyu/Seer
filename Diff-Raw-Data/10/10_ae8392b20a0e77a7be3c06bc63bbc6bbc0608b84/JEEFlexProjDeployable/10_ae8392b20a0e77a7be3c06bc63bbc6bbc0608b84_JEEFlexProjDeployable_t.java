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
 package org.eclipse.jst.jee.internal.deployables;
 
 import java.util.Iterator;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.jst.j2ee.componentcore.J2EEModuleVirtualArchiveComponent;
 import org.eclipse.jst.j2ee.internal.EjbModuleExtensionHelper;
 import org.eclipse.jst.j2ee.internal.IEJBModelExtenderManager;
 import org.eclipse.jst.j2ee.internal.deployables.J2EEFlexProjDeployable;
 import org.eclipse.jst.j2ee.internal.project.J2EEProjectUtilities;
 import org.eclipse.jst.j2ee.model.IEARModelProvider;
 import org.eclipse.jst.j2ee.model.IModelProvider;
 import org.eclipse.jst.j2ee.model.ModelProviderManager;
import org.eclipse.jst.j2ee.project.JavaEEProjectUtilities;
 import org.eclipse.jst.javaee.application.Application;
 import org.eclipse.jst.javaee.ejb.EJBJar;
 import org.eclipse.jst.javaee.ejb.EnterpriseBeans;
 import org.eclipse.jst.javaee.ejb.SessionBean;
 import org.eclipse.wst.common.componentcore.ArtifactEdit;
 import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
 import org.eclipse.wst.common.componentcore.resources.IVirtualReference;
 import org.eclipse.wst.server.core.IModule;
 import org.eclipse.wst.server.core.ServerUtil;
 
 /**
  * JEE module superclass.
  */
 public class JEEFlexProjDeployable extends J2EEFlexProjDeployable {
 
 	/**
 	 * Constructor for JEEFlexProjDeployable.
 	 * 
 	 * @param project
 	 * @param aComponent
 	 */
 	public JEEFlexProjDeployable(IProject project, IVirtualComponent aComponent) {
 		super(project, aComponent);
 	}
 
 	/**
 	 * Constructor for JEEFlexProjDeployable.
 	 * 
 	 * @param project
 	 */
 	public JEEFlexProjDeployable(IProject project) {
 		super(project);
 	}
 
 	protected boolean shouldIncludeUtilityComponent(IVirtualComponent virtualComp,IVirtualReference[] references, IEARModelProvider model) {
 		// If the component module is an EAR we know all archives are filtered out of virtual component members
 		// and we will return only those archives which are not binary J2EE modules in the EAR DD.  These J2EE modules will
 		// be returned by getChildModules()
		if (JavaEEProjectUtilities.isEARProject(component.getProject()))
 			return virtualComp != null && virtualComp.isBinary() && !isNestedJ2EEModule(virtualComp, references, model);
 		return super.shouldIncludeUtilityComponent(virtualComp, references, ArtifactEdit.class.isInstance(model) ? (ArtifactEdit)model : null);
 	}
 	    
     public String getJNDIName(String ejbName, String interfaceName) {
     	if (!J2EEProjectUtilities.isEJBProject(component.getProject()))
     		return null;
 
 		EjbModuleExtensionHelper modHelper = null;
 		EJBJar jar = null;
 		
 		IModelProvider model = ModelProviderManager.getModelProvider(component.getProject());
 		if (model != null) {
 			jar = (EJBJar) model.getModelObject();
 			SessionBean bean = getSessionBeanNamed(jar, ejbName);
 			modHelper = IEJBModelExtenderManager.INSTANCE.getEJBModuleExtension(null);
 			return modHelper == null ? null : modHelper.getJavaEEJNDIName(jar, bean, interfaceName);
 		}
 		
 		return null;
 	}
     /**
      * Return List of Session beans in this jar.
      * @return java.util.List
      */
     public SessionBean getSessionBeanNamed(EJBJar jar, String beanName) {
     	
     	EnterpriseBeans allBeans = jar.getEnterpriseBeans();
     	for (Iterator iterator = allBeans.getSessionBeans().iterator(); iterator.hasNext();) {
 			SessionBean bean = (SessionBean) iterator.next();
     		if (bean.getEjbName().equals(beanName))
     			return bean;
     	}
     	return null;
     }
     
     @Override
    protected IModule gatherModuleReference(IVirtualComponent component, IVirtualComponent targetComponent ) {
     	IModule module = super.gatherModuleReference(component, targetComponent);
     	// Handle binary module components
     	if (targetComponent instanceof J2EEModuleVirtualArchiveComponent) {
    		if (JavaEEProjectUtilities.isEARProject(component.getProject()) || targetComponent.getProject()!=component.getProject())
     			module = ServerUtil.getModule(JEEDeployableFactory.ID+":"+targetComponent.getName()); //$NON-NLS-1$
     	}
 		return module;
     }
     
     /**
      * Determine if the component is nested J2EE module on the application.xml of this EAR
      * @param aComponent
      * @return boolean is passed in component a nested J2EE module on this EAR
      */
     private boolean isNestedJ2EEModule(IVirtualComponent aComponent, IVirtualReference[] references, IEARModelProvider model) {
     	if (model==null) 
 			return false;
 		Application app = (Application)model.getModelObject();
 		IVirtualReference reference = getReferenceNamed(references,aComponent.getName());
 		// Ensure module URI exists on EAR DD for binary archive
 		return app.getFirstModule(reference.getArchiveName()) != null;
     }
 }
