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
 import java.util.List;
 
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IWorkspace;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jdt.core.IClasspathEntry;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jem.util.emf.workbench.ProjectUtilities;
 import org.eclipse.jem.util.logger.proxy.Logger;
 import org.eclipse.jst.common.project.facet.WtpUtils;
 import org.eclipse.jst.j2ee.application.internal.operations.AddComponentToEnterpriseApplicationDataModelProvider;
 import org.eclipse.jst.j2ee.internal.common.J2EEVersionUtil;
 import org.eclipse.jst.j2ee.project.facet.J2EEFacetInstallDelegate;
 import org.eclipse.jst.j2ee.web.componentcore.util.WebArtifactEdit;
 import org.eclipse.wst.common.componentcore.ComponentCore;
 import org.eclipse.wst.common.componentcore.datamodel.properties.ICreateReferenceComponentsDataModelProperties;
 import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
 import org.eclipse.wst.common.componentcore.resources.IVirtualFolder;
 import org.eclipse.wst.common.frameworks.datamodel.DataModelFactory;
 import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
 import org.eclipse.wst.common.project.facet.core.IDelegate;
 import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
 import org.eclipse.wst.common.project.facet.core.runtime.classpath.ClasspathHelper;
 
 public final class WebFacetInstallDelegate extends J2EEFacetInstallDelegate implements IDelegate {
 
 	private static final String WEB_LIB_CONTAINER = "org.eclipse.jst.j2ee.internal.web.container";
 
 
 	public void execute(final IProject project, final IProjectFacetVersion fv, final Object cfg, final IProgressMonitor monitor) throws CoreException {
 		if (monitor != null) {
 			monitor.beginTask("", 1);
 		}
 
 		try {
 			IDataModel model = (IDataModel) cfg;
 
 			final IJavaProject jproj = JavaCore.create(project);
 
 			// Add WTP natures.
 
 			WtpUtils.addNatures(project);
 
 			// Create the directory structure.
 
 			final IWorkspace ws = ResourcesPlugin.getWorkspace();
 			final IPath pjpath = project.getFullPath();
 
 			final IPath contentdir = pjpath.append(model.getStringProperty(IWebFacetInstallDataModelProperties.CONTENT_DIR));
 			mkdirs(ws.getRoot().getFolder(contentdir));
 
 			final IPath webinf = contentdir.append("WEB-INF");
 			IFolder webinfFolder = ws.getRoot().getFolder(webinf);
 			mkdirs(webinfFolder);
 
 			final IPath webinflib = webinf.append("lib");
 			mkdirs(ws.getRoot().getFolder(webinflib));
 
 			try {
 				createManifest(project, model.getStringProperty(IWebFacetInstallDataModelProperties.CONTENT_DIR), monitor);
 			} catch (InvocationTargetException e) {
 				Logger.getLogger().logError(e);
 			} catch (InterruptedException e) {
 				Logger.getLogger().logError(e);
 			}
 
 			// Setup WEB-INF/src, if necessary.
 
 			if (model.getBooleanProperty(IWebFacetInstallDataModelProperties.CREATE_WEB_INF_SRC)) {
 				final IPath webinfsrc = webinf.append("src");
 				mkdirs(ws.getRoot().getFolder(webinfsrc));
 
 				addToClasspath(jproj, JavaCore.newSourceEntry(webinfsrc));
 			}
 
 			// Setup the flexible project structure.
 
 			final IVirtualComponent c = ComponentCore.createComponent(project);
 
 			c.create(0, null);
 
 			// final ComponentType ctype = ComponentcoreFactory.eINSTANCE.createComponentType();
 			//
 			// ctype.setComponentTypeId(IModuleConstants.JST_WEB_MODULE);
 			// ctype.setVersion(fv.getVersionString());
 
 			c.setMetaProperty("context-root", model.getStringProperty(IWebFacetInstallDataModelProperties.CONTEXT_ROOT));
 			c.setMetaProperty("java-output-path", "/build/classes/");
 
 			// final StructureEdit edit = StructureEdit.getStructureEditForWrite(project);
 			//
 			// try {
 			// StructureEdit.setComponentType(c, ctype);
 			// edit.saveIfNecessary(null);
 			// } finally {
 			// edit.dispose();
 			// }
 
 			final IVirtualFolder jsrc = c.getRootFolder().getFolder("/WEB-INF/classes");
 			final IClasspathEntry[] cp = jproj.getRawClasspath();
 
 			for (int i = 0; i < cp.length; i++) {
 				final IClasspathEntry cpe = cp[i];
 
 				if (cpe.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
 					jsrc.createLink(cpe.getPath().removeFirstSegments(1), 0, null);
 				}
 			}
 
 			final IVirtualFolder webroot = c.getRootFolder();
 
 			webroot.createLink(new Path("/" + model.getStringProperty(IWebFacetInstallDataModelProperties.CONTENT_DIR)), 0, null);
 
 			// Create the deployment descriptor (web.xml) if one doesn't exist
 			if (!webinfFolder.getFile("web.xml").exists()) {
 				String ver = fv.getVersionString();
 				int nVer = J2EEVersionUtil.convertVersionStringToInt(ver);
 				WebArtifactEdit.createDeploymentDescriptor(project, nVer);
 			}
 
 			// Setup the classpath.
 
 			ClasspathHelper.removeClasspathEntries(project, fv);
 
 			if (!ClasspathHelper.addClasspathEntries(project, fv)) {
 				// TODO: Support the no runtime case.
 				// ClasspathHelper.addClasspathEntries( project, fv, <something> );
 			}
 
 			// Add the web libraries container.
 
 			final IPath cont = new Path(WEB_LIB_CONTAINER).append(project.getName());
 
 			addToClasspath(jproj, JavaCore.newContainerEntry(cont));
 
 			// Associate with an EAR, if necessary.
 
 			final String earProjectName = model.getStringProperty(IWebFacetInstallDataModelProperties.EAR_PROJECT_NAME);
 
 			if (earProjectName != null && !earProjectName.equals("")) { //$NON-NLS-1$
 				String ver = fv.getVersionString();
 				String j2eeVersionText = J2EEVersionUtil.convertVersionIntToString(J2EEVersionUtil.convertWebVersionStringToJ2EEVersionID(ver));
 				installEARFacet(j2eeVersionText, earProjectName, monitor);
                 
                 IProject earProject = ProjectUtilities.getProject( earProjectName );
                 IVirtualComponent earComp = ComponentCore.createComponent( earProject );
                 
                 IDataModel dataModel = DataModelFactory.createDataModel( new AddComponentToEnterpriseApplicationDataModelProvider());
                 dataModel.setProperty( ICreateReferenceComponentsDataModelProperties.SOURCE_COMPONENT, earComp );
                 List modList = (List) dataModel.getProperty(ICreateReferenceComponentsDataModelProperties.TARGET_COMPONENT_LIST);
                 modList.add(c);
                 dataModel.setProperty(ICreateReferenceComponentsDataModelProperties.TARGET_COMPONENT_LIST, modList);
                 try {
                     dataModel.getDefaultOperation().execute(null, null);
                 } catch (ExecutionException e) {
                     Logger.getLogger().logError(e);
                 }
 			}
 
 			if (monitor != null) {
 				monitor.worked(1);
 			}
 		}
 		finally {
 			if (monitor != null) {
 				monitor.done();
 			}
 		}
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
