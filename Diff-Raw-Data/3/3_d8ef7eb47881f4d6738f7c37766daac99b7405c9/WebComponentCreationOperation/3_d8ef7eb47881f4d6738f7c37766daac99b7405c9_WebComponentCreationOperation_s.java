 /*******************************************************************************
  * Copyright (c) 2003, 2004, 2005 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.j2ee.internal.web.archive.operations;
 
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jdt.core.IClasspathEntry;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jem.util.logger.proxy.Logger;
 import org.eclipse.jem.workbench.utility.JemProjectUtilities;
 import org.eclipse.jst.j2ee.application.internal.operations.J2EEComponentCreationOperation;
 import org.eclipse.jst.j2ee.internal.J2EEConstants;
 import org.eclipse.jst.j2ee.internal.common.J2EEVersionUtil;
 import org.eclipse.jst.j2ee.internal.web.classpath.WebAppContainer;
 import org.eclipse.jst.j2ee.web.componentcore.util.WebArtifactEdit;
 import org.eclipse.jst.j2ee.web.datamodel.properties.IWebComponentCreationDataModelProperties;
 import org.eclipse.wst.common.componentcore.ComponentCore;
 import org.eclipse.wst.common.componentcore.internal.ComponentcoreFactory;
 import org.eclipse.wst.common.componentcore.internal.Property;
 import org.eclipse.wst.common.componentcore.internal.util.IModuleConstants;
 import org.eclipse.wst.common.componentcore.resources.ComponentHandle;
 import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
 import org.eclipse.wst.common.componentcore.resources.IVirtualFolder;
 import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
 
 public class WebComponentCreationOperation extends J2EEComponentCreationOperation implements IWebComponentCreationDataModelProperties {
 
 	public WebComponentCreationOperation(IDataModel model) {
 		super(model);
 	}
 
 	protected void createAndLinkJ2EEComponentsForMultipleComponents() throws CoreException {
 		createWebStructure("/" + getModuleName());
 	}
 
 	protected void createAndLinkJ2EEComponentsForSingleComponent() throws CoreException {
 		createWebStructure("");
 	}
 
 	protected void createWebStructure(String prefix) throws CoreException {
 		IVirtualComponent component = ComponentCore.createComponent(getProject(), getModuleDeployName());
 		component.create(0, null);
 		// create and link javaSource Source Folder
 		IVirtualFolder javaSourceFolder = component.getRootFolder().getFolder(new Path("/" + J2EEConstants.WEB_INF + "/classes")); //$NON-NLS-1$       
		javaSourceFolder.createLink(new Path(prefix + model.getStringProperty(JAVASOURCE_FOLDER)), 0, null);

 		// create and link META-INF and WEB-INF folder
 		IVirtualFolder webContent = component.getRootFolder().getFolder(new Path("/")); //$NON-NLS-1$       
 		webContent.createLink(new Path(prefix + "/WebContent"), 0, null);
 
 		IVirtualFolder webInfFolder = webContent.getFolder(J2EEConstants.WEB_INF);
 		webInfFolder.create(IResource.FORCE, null);
 
 		IVirtualFolder metaInfFolder = webContent.getFolder(J2EEConstants.META_INF);
 		metaInfFolder.create(IResource.FORCE, null);
 
 		IVirtualFolder webLib = webInfFolder.getFolder("lib");
 		webLib.create(IResource.FORCE, null);
 	}
 
 
 	protected void createDeploymentDescriptor(IProgressMonitor monitor) throws CoreException, InvocationTargetException, InterruptedException {
 
 		WebArtifactEdit webEdit = null;
 		try {
 			ComponentHandle handle = ComponentHandle.create(getProject(), model.getStringProperty(COMPONENT_DEPLOY_NAME));
 			webEdit = WebArtifactEdit.getWebArtifactEditForWrite(handle);
 			Integer version = (Integer) model.getProperty(COMPONENT_VERSION);
 			webEdit.createModelRoot(version.intValue());
 			webEdit.save(monitor);
 		} catch (Exception e) {
 			Logger.getLogger().logError(e);
 		} finally {
 			if (webEdit != null)
 				webEdit.dispose();
 			webEdit = null;
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.j2ee.application.operations.J2EEComponentCreationOperation#getVersion()
 	 */
 	protected String getVersion() {
 		int version = model.getIntProperty(COMPONENT_VERSION);
 		return J2EEVersionUtil.getServletTextVersion(version);
 
 	}
 
 	protected List getProperties() {
 		List newProps = new ArrayList();
 		Property prop = ComponentcoreFactory.eINSTANCE.createProperty();
 		prop.setName(J2EEConstants.CONTEXTROOT);
 		prop.setValue(model.getStringProperty(CONTEXT_ROOT));
 		newProps.add(prop);
 		return newProps;
 	}
 
 	public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
 		try {
 			super.execute(IModuleConstants.JST_WEB_MODULE, monitor);
 			// Add "Web App Libraries" container to the project classpath.
 
 			final IJavaProject jproject = JemProjectUtilities.getJavaProject(getProject());
 			final List cp = new ArrayList();
 			final IClasspathEntry[] old = jproject.getRawClasspath();
 			for (int i = 0; i < old.length; i++) {
 				cp.add(old[i]);
 			}
 			final String name = getModuleDeployName();
 			cp.add(WebAppContainer.convert(name));
 
 			final IClasspathEntry[] cparray = new IClasspathEntry[cp.size()];
 			cp.toArray(cparray);
 			jproject.setRawClasspath(cparray, null);
 		} catch (CoreException e) {
 			Logger.getLogger().log(e.getMessage());
 		} catch (InvocationTargetException e) {
 			Logger.getLogger().log(e.getMessage());
 		} catch (InterruptedException e) {
 			Logger.getLogger().log(e.getMessage());
 		}
 
 		return OK_STATUS;
 	}
 
 	public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 }
