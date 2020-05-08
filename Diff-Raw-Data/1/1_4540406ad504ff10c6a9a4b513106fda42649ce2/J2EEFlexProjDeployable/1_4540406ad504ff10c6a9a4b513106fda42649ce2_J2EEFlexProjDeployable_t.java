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
 package org.eclipse.jst.j2ee.internal.deployables;
 
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jem.workbench.utility.JemProjectUtilities;
 import org.eclipse.jst.common.jdt.internal.javalite.JavaLiteUtilities;
 import org.eclipse.jst.j2ee.componentcore.util.EARArtifactEdit;
 import org.eclipse.jst.j2ee.ejb.EJBJar;
 import org.eclipse.jst.j2ee.internal.EjbModuleExtensionHelper;
 import org.eclipse.jst.j2ee.internal.IEJBModelExtenderManager;
 import org.eclipse.jst.j2ee.internal.J2EEConstants;
 import org.eclipse.jst.j2ee.internal.common.exportmodel.AddClasspathReferencesParticipant;
 import org.eclipse.jst.j2ee.internal.common.exportmodel.AddMappedOutputFoldersParticipant;
 import org.eclipse.jst.j2ee.internal.common.exportmodel.IgnoreJavaInSourceFolderParticipant;
 import org.eclipse.jst.j2ee.internal.common.exportmodel.JEEHeirarchyExportParticipant;
 import org.eclipse.jst.j2ee.internal.common.exportmodel.ReplaceManifestExportParticipant;
 import org.eclipse.jst.j2ee.internal.common.exportmodel.SingleRootExportParticipant;
 import org.eclipse.jst.j2ee.internal.plugin.J2EEPlugin;
 import org.eclipse.jst.j2ee.project.JavaEEProjectUtilities;
 import org.eclipse.jst.j2ee.project.SingleRootUtil;
 import org.eclipse.jst.server.core.IApplicationClientModule;
 import org.eclipse.jst.server.core.IConnectorModule;
 import org.eclipse.jst.server.core.IEJBModule;
 import org.eclipse.jst.server.core.IEnterpriseApplication;
 import org.eclipse.jst.server.core.IWebModule;
 import org.eclipse.wst.common.componentcore.ArtifactEdit;
 import org.eclipse.wst.common.componentcore.ComponentCore;
 import org.eclipse.wst.common.componentcore.internal.flat.IChildModuleReference;
 import org.eclipse.wst.common.componentcore.internal.flat.IFlattenParticipant;
 import org.eclipse.wst.common.componentcore.internal.util.ComponentUtilities;
 import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
 import org.eclipse.wst.common.componentcore.resources.IVirtualFolder;
 import org.eclipse.wst.server.core.IModule;
 import org.eclipse.wst.web.internal.deployables.FlatComponentDeployable;
 /**
  * J2EE module superclass.
  */
 public class J2EEFlexProjDeployable extends FlatComponentDeployable implements 
 				IEnterpriseApplication, IApplicationClientModule, 
 				IConnectorModule, IEJBModule, IWebModule {
 	 
 	/**
 	 * Constructor for J2EEFlexProjDeployable.
 	 * 
 	 * @param project
 	 * @param aComponent
 	 */
 	public J2EEFlexProjDeployable(IProject project, IVirtualComponent aComponent) {
 		super(project, aComponent);
 	}
 	
 
 	/**
 	 * Constructor for J2EEFlexProjDeployable.
 	 * 
 	 * @param project
 	 */
 	public J2EEFlexProjDeployable(IProject project) {
 		super(project);
 	}
 
 	/**
 	 * @see SingleRootUtil.isSingleRoot(IVirtualComponent component)
 	 * @return <code>true</code> if this module has a single root structure, and
 	 *    <code>false</code> otherwise
 	 */
 	@Override
 	public boolean isSingleRootStructure() {
 		return new SingleRootUtil(getComponent()).isSingleRoot();
 	}
 	
 	@Override
 	protected IFlattenParticipant[] getParticipants() {
 		return new IFlattenParticipant[]{
 				new SingleRootExportParticipant(), 
 				new JEEHeirarchyExportParticipant(), 
 				new AddClasspathReferencesParticipant(),
 				new AddMappedOutputFoldersParticipant(),
 				new ReplaceManifestExportParticipant(),
 				new IgnoreJavaInSourceFolderParticipant()
 		};
 	}
     
     @Override
 	protected IModule gatherModuleReference(IVirtualComponent component, IChildModuleReference child ) {
     	if (!child.isBinary()) 
     		return super.gatherModuleReference(component, child);
     	return J2EEDeployableFactory.j2eeInstance().createChildModule(this, child);
     }
     
 
     /*_________________________________
      * 
 	 * Methods for specific J2EE / JEE Interfaces are below
 	 *_________________________________
 	 */
     public String getJNDIName(String ejbName) {
     	if (!JavaEEProjectUtilities.isEJBProject(component.getProject()))
     		return null;
 		EjbModuleExtensionHelper modHelper = null;
 		EJBJar jar = null;
 		ArtifactEdit ejbEdit = null;
 		try {
 			ejbEdit = ComponentUtilities.getArtifactEditForRead(component);
 			if (ejbEdit != null) {
 				jar = (EJBJar) ejbEdit.getContentModelRoot();
 				modHelper = IEJBModelExtenderManager.INSTANCE.getEJBModuleExtension(null);
 				return modHelper == null ? null : modHelper.getJNDIName(jar, jar.getEnterpriseBeanNamed(ejbName));
 			}
 		} catch (Exception e) {
 			J2EEPlugin.logError(e);
 		} finally {
 			if (ejbEdit != null)
 				ejbEdit.dispose();
 		}
 		return null;
 	}
     
     /**
      * This method returns the context root property from the deployable project's .component file
      */
     public String getContextRoot() {
 		Properties props = component.getMetaProperties();
 		if(props.containsKey(J2EEConstants.CONTEXTROOT))
 			return props.getProperty(J2EEConstants.CONTEXTROOT);
 	    return component.getName();
     }
     
     /**
      * This method is applicable for a web deployable.  The module passed in should either be null or
      * the EAR module the web deployable is contained in.  It will return the context root from the EAR
      * if it has one or return the .component value in the web project if it is standalone.
      * 
      * @param module
      * @return contextRoot String
      */
     public String getContextRoot(IModule earModule) {
     	IProject deployProject = component.getProject();
     	String contextRoot = null;
     	if (earModule == null)
     		return getContextRoot();
     	else if (JavaEEProjectUtilities.isEARProject(earModule.getProject()) && JavaEEProjectUtilities.isDynamicWebProject(deployProject)) {
     		EARArtifactEdit edit = null;
     		try {
     			edit = EARArtifactEdit.getEARArtifactEditForRead(earModule.getProject());
     			contextRoot = edit.getWebContextRoot(deployProject);
     		} finally {
     			if (edit!=null)
     				edit.dispose();
     		}
     	}
     	return contextRoot;
     }
     
     
 	/**
 	 * Returns the root folders for the resources in this module.
 	 * 
 	 * @return a possibly-empty array of resource folders
 	 */
 	public IContainer[] getResourceFolders() {
 		IVirtualComponent vc = ComponentCore.createComponent(getProject());
 		if (vc != null) {
 			IVirtualFolder vFolder = vc.getRootFolder();
 			if (vFolder != null)
 				return vFolder.getUnderlyingFolders();
 		}
 		return new IContainer[]{};
 	}
 	
 	/**
 	 * Returns the root folders containing Java output in this module.
 	 * 
 	 * @return a possibly-empty array of Java output folders
 	 * @deprecated
 	 */
 	public IContainer[] getJavaOutputFolders() {
 		return getJavaOutputFolders(component);
 	}
 	
 	/**
 	 * @param component
 	 * @deprecated
 	 * @return
 	 */
 	public IContainer[] getJavaOutputFolders(IVirtualComponent component) {
 		if (component == null)
 			return new IContainer[0];
 		List<IContainer> l = JavaLiteUtilities.getJavaOutputContainers(component);
 		return l.toArray(new IContainer[l.size()]);
 	}	
 	
     /**
      * Returns the classpath as a list of absolute IPaths.
      * 
      * @deprecated
      * @return an array of paths
      */
     public IPath[] getClasspath() {
 		List<IPath> paths = new ArrayList<IPath>();
         IJavaProject proj = JemProjectUtilities.getJavaProject(getProject());
         URL[] urls = JemProjectUtilities.getClasspathAsURLArray(proj);
 		for (int i = 0; i < urls.length; i++) {
 			URL url = urls[i];
 			paths.add(Path.fromOSString(url.getPath()));
 		}
         return paths.toArray(new IPath[paths.size()]);
     }
 }
