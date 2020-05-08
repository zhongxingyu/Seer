 /******************************************************************************
  * Copyright (c) 2005 BEA Systems, Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Konstantin Komissarchik - initial API and implementation
  ******************************************************************************/
 
 package org.eclipse.jst.j2ee.web.project.facet;
 
 import java.lang.reflect.InvocationTargetException;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IWorkspace;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.jdt.core.IClasspathEntry;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jem.util.emf.workbench.ProjectUtilities;
 import org.eclipse.jem.util.logger.proxy.Logger;
 import org.eclipse.jst.common.project.facet.WtpUtils;
 import org.eclipse.jst.common.project.facet.core.ClasspathHelper;
 import org.eclipse.jst.j2ee.application.ApplicationPackage;
 import org.eclipse.jst.j2ee.application.Module;
 import org.eclipse.jst.j2ee.application.internal.operations.AddComponentToEnterpriseApplicationDataModelProvider;
 import org.eclipse.jst.j2ee.application.internal.operations.AddComponentToEnterpriseApplicationOp;
 import org.eclipse.jst.j2ee.application.internal.operations.IAddComponentToEnterpriseApplicationDataModelProperties;
 import org.eclipse.jst.j2ee.internal.J2EEConstants;
 import org.eclipse.jst.j2ee.internal.common.J2EEVersionUtil;
 import org.eclipse.jst.j2ee.internal.web.classpath.WebAppContainer;
 import org.eclipse.jst.j2ee.project.facet.IJ2EEModuleFacetInstallDataModelProperties;
 import org.eclipse.jst.j2ee.project.facet.J2EEFacetInstallDelegate;
 import org.eclipse.jst.j2ee.web.componentcore.util.WebArtifactEdit;
 import org.eclipse.wst.common.componentcore.ComponentCore;
 import org.eclipse.wst.common.componentcore.datamodel.FacetDataModelProvider;
 import org.eclipse.wst.common.componentcore.datamodel.properties.ICreateReferenceComponentsDataModelProperties;
 import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
 import org.eclipse.wst.common.componentcore.resources.IVirtualFile;
 import org.eclipse.wst.common.componentcore.resources.IVirtualFolder;
 import org.eclipse.wst.common.frameworks.datamodel.DataModelFactory;
 import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
 import org.eclipse.wst.common.frameworks.datamodel.IDataModelOperation;
 import org.eclipse.wst.common.project.facet.core.IDelegate;
 import org.eclipse.wst.common.project.facet.core.IFacetedProject;
 import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
 import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
 
 public final class WebFacetInstallDelegate extends J2EEFacetInstallDelegate implements IDelegate {
 
 	public void execute(final IProject project, final IProjectFacetVersion fv, final Object cfg, final IProgressMonitor monitor) throws CoreException {
 		if (monitor != null) {
 			monitor.beginTask("", 1); //$NON-NLS-1$
 		}
 
 		try {
 			final IDataModel model = (IDataModel) cfg;
 
 			final IJavaProject jproj = JavaCore.create(project);
 
 			// Add WTP natures.
 
 			WtpUtils.addNatures(project);
 
 			// Create the directory structure.
 
 			final IWorkspace ws = ResourcesPlugin.getWorkspace();
 			final IPath pjpath = project.getFullPath();
 
 			final IPath contentdir = setContentPropertyIfNeeded(model, pjpath, project);
 			mkdirs(ws.getRoot().getFolder(contentdir));
 
 			final IPath webinf = contentdir.append("WEB-INF"); //$NON-NLS-1$
 			IFolder webinfFolder = ws.getRoot().getFolder(webinf);
 			mkdirs(webinfFolder);
 
 			final IPath webinflib = webinf.append("lib"); //$NON-NLS-1$
 			mkdirs(ws.getRoot().getFolder(webinflib));
 
 			// Setup the flexible project structure.
 
 			final IVirtualComponent c = ComponentCore.createComponent(project);
 
 			c.create(0, null);
 
 			String contextRoot = model.getStringProperty(IWebFacetInstallDataModelProperties.CONTEXT_ROOT);
 			if (contextRoot == null || contextRoot.length() == 0)
 				contextRoot = project.getName();
 			setContextRootPropertyIfNeeded(c, contextRoot);
 			setJavaOutputPropertyIfNeeded(c);
 
 			final IVirtualFolder webroot = c.getRootFolder();
 			if (webroot.getProjectRelativePath().equals(new Path("/"))) //$NON-NLS-1$
 				webroot.createLink(new Path("/" + model.getStringProperty(IJ2EEModuleFacetInstallDataModelProperties.CONFIG_FOLDER)), 0, null); //$NON-NLS-1$
 
 			// Create the deployment descriptor (web.xml) if one doesn't exist
 			if (!webinfFolder.getFile("web.xml").exists()) { //$NON-NLS-1$
 				String ver = fv.getVersionString();
 				int nVer = J2EEVersionUtil.convertVersionStringToInt(ver);
 				WebArtifactEdit.createDeploymentDescriptor(project, nVer);
 			}
 			
 			// Set entries for src folders
 			final IVirtualFolder jsrc = c.getRootFolder().getFolder("/WEB-INF/classes"); //$NON-NLS-1$
 			final IClasspathEntry[] cp = jproj.getRawClasspath();
 			for (int i = 0; i < cp.length; i++) {
 				final IClasspathEntry cpe = cp[i];
 				if (cpe.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
 					jsrc.createLink(cpe.getPath().removeFirstSegments(1), 0, null);
 				}
 			}
 			
 			IVirtualFile vf = c.getRootFolder().getFile(new Path(J2EEConstants.MANIFEST_URI));
 			IFile manifestmf = vf.getUnderlyingFile();
 			if (manifestmf == null || !manifestmf.exists()) {
 				try {
 					createManifest(project, c.getRootFolder().getUnderlyingFolder(), monitor);
 				} catch (InvocationTargetException e) {
 					Logger.getLogger().logError(e);
 				} catch (InterruptedException e) {
 					Logger.getLogger().logError(e);
 				}
 			}
 
 			// Setup the classpath.
 
 			ClasspathHelper.removeClasspathEntries(project, fv);
 
 			if (!ClasspathHelper.addClasspathEntries(project, fv)) {
 				// TODO: Support the no runtime case.
 				// ClasspathHelper.addClasspathEntries( project, fv, <something> );
 			}
 
 			// Add the web libraries container.
 
 			final IPath cont = new Path(WebAppContainer.CONTAINER_ID);
 			addToClasspath(jproj, JavaCore.newContainerEntry(cont));
 
 			// Associate with an EAR, if necessary.
 
 
 			if (model.getBooleanProperty(IJ2EEModuleFacetInstallDataModelProperties.ADD_TO_EAR)) {
 				final String earProjectName = model.getStringProperty(IJ2EEModuleFacetInstallDataModelProperties.EAR_PROJECT_NAME);
 				if (earProjectName != null && !earProjectName.equals("")) { //$NON-NLS-1$
 					String ver = fv.getVersionString();
 					String j2eeVersionText = J2EEVersionUtil.convertVersionIntToString(J2EEVersionUtil.convertWebVersionStringToJ2EEVersionID(ver));
 					IFacetedProject facetedProject = ProjectFacetsManager.create(project);
					installEARFacet(j2eeVersionText, earProjectName, facetedProject.getRuntime(), monitor);
 
 					IProject earProject = ProjectUtilities.getProject(earProjectName);
 					IVirtualComponent earComp = ComponentCore.createComponent(earProject);
 
 					final IDataModel dataModel = DataModelFactory.createDataModel(new AddComponentToEnterpriseApplicationDataModelProvider() {
 						public Object getDefaultProperty(String propertyName) {
 							if (IAddComponentToEnterpriseApplicationDataModelProperties.TARGET_COMPONENTS_TO_URI_MAP.equals(propertyName)) {
 								Map map = new HashMap();
 								List components = (List) getProperty(TARGET_COMPONENT_LIST);
 								for (int i = 0; i < components.size(); i++) {
 									IVirtualComponent component = (IVirtualComponent) components.get(i);
 									String name = component.getName();
 									name += ".war"; //$NON-NLS-1$
 									map.put(component, name);
 								}
 								return map;
 							}
 							return super.getDefaultProperty(propertyName);
 						}
 
 						public IDataModelOperation getDefaultOperation() {
 							return new AddComponentToEnterpriseApplicationOp(model) {
 								protected Module createNewModule(IVirtualComponent wc) {
 									return ((ApplicationPackage) EPackage.Registry.INSTANCE.getEPackage(ApplicationPackage.eNS_URI)).getApplicationFactory().createWebModule();
 								}
 							};
 						}
 					});
 					dataModel.setProperty(ICreateReferenceComponentsDataModelProperties.SOURCE_COMPONENT, earComp);
 					List modList = (List) dataModel.getProperty(ICreateReferenceComponentsDataModelProperties.TARGET_COMPONENT_LIST);
 					modList.add(c);
 					dataModel.setProperty(ICreateReferenceComponentsDataModelProperties.TARGET_COMPONENT_LIST, modList);
 					try {
 						dataModel.getDefaultOperation().execute(null, null);
 					} catch (ExecutionException e) {
 						Logger.getLogger().logError(e);
 					}
 
 				}
 			}
 
 			try {
 				((IDataModelOperation) model.getProperty(FacetDataModelProvider.NOTIFICATION_OPERATION)).execute(monitor, null);
 			} catch (ExecutionException e) {
 				Logger.getLogger().logError(e);
 			}
 
 			if (monitor != null) {
 				monitor.worked(1);
 			}
 		} finally {
 			if (monitor != null) {
 				monitor.done();
 			}
 		}
 	}
 
 	private void setJavaOutputPropertyIfNeeded(final IVirtualComponent c) {
 		String existing = c.getMetaProperties().getProperty("java-output-path"); //$NON-NLS-1$
 		if (existing == null)
 			c.setMetaProperty("java-output-path", "/build/classes/"); //$NON-NLS-1$ //$NON-NLS-2$
 	}
 
 	private void setContextRootPropertyIfNeeded(final IVirtualComponent c, String contextRoot) {
 		String existing = c.getMetaProperties().getProperty("context-root"); //$NON-NLS-1$
 		if (existing == null)
 			c.setMetaProperty("context-root", contextRoot); //$NON-NLS-1$
 	}
 
 //	private IPath setSourcePropertyIfNeeded(final IDataModel model, final IPath pjpath, IProject project) {
 //		IVirtualComponent c = ComponentCore.createComponent(project);
 //		if (c.exists()) {
 //			return J2EEProjectUtilities.getSourcePathOrFirst(project, null).makeAbsolute();
 //		}
 //		return pjpath.append(model.getStringProperty(IWebFacetInstallDataModelProperties.SOURCE_FOLDER));
 //	}
 
 	private IPath setContentPropertyIfNeeded(final IDataModel model, final IPath pjpath, IProject project) {
 		IVirtualComponent c = ComponentCore.createComponent(project);
 		if (c.exists()) {
 			return c.getRootFolder().getUnderlyingResource().getFullPath();
 		}
 		return pjpath.append(model.getStringProperty(IJ2EEModuleFacetInstallDataModelProperties.CONFIG_FOLDER));
 	}
 
 	private static void addToClasspath(final IJavaProject jproj, final IClasspathEntry entry)
 
 	throws CoreException
 
 	{
 		final IClasspathEntry[] current = jproj.getRawClasspath();
 		final IClasspathEntry[] updated = new IClasspathEntry[current.length + 1];
 		System.arraycopy(current, 0, updated, 0, current.length);
 		updated[current.length] = entry;
 		jproj.setRawClasspath(updated, null);
 	}
 
 	private static void mkdirs(final IFolder folder)
 
 	throws CoreException
 
 	{
 		if (!folder.exists()) {
 			if (folder.getParent() instanceof IFolder) {
 				mkdirs((IFolder) folder.getParent());
 			}
 
 			folder.create(true, true, null);
 		}
 	}
 }
