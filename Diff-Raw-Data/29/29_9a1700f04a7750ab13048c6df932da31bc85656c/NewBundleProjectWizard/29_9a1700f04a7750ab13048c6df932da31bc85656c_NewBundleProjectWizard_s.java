 /*******************************************************************************
 * Copyright (c) 2009 SpringSource, a divison of VMware, Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     SpringSource, a division of VMware, Inc. - initial API and implementation
  *******************************************************************************/
 package org.eclipse.virgo.ide.ui.wizards;
 
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.jdt.core.IClasspathEntry;
 import org.eclipse.jdt.core.IJavaElement;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jdt.internal.ui.packageview.PackageExplorerPart;
 import org.eclipse.jdt.internal.ui.wizards.NewElementWizard;
 import org.eclipse.jdt.ui.wizards.NewJavaProjectWizardPageOne;
 import org.eclipse.jdt.ui.wizards.NewJavaProjectWizardPageTwo;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.wizard.IWizardPage;
 import org.eclipse.pde.internal.ui.wizards.IProjectProvider;
 import org.eclipse.pde.internal.ui.wizards.plugin.AbstractFieldData;
 import org.eclipse.pde.internal.ui.wizards.plugin.PluginFieldData;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.INewWizard;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.IWorkingSet;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.actions.WorkspaceModifyOperation;
 import org.eclipse.ui.part.FileEditorInput;
 import org.eclipse.virgo.ide.facet.core.BundleFacetInstallDataModelProvider;
 import org.eclipse.virgo.ide.facet.core.FacetCorePlugin;
 import org.eclipse.virgo.ide.manifest.core.BundleManifestUtils;
 import org.eclipse.virgo.ide.ui.ServerIdeUiPlugin;
 import org.eclipse.virgo.ide.ui.editors.BundleManifestEditor;
 import org.eclipse.wst.common.componentcore.datamodel.properties.IFacetProjectCreationDataModelProperties;
 import org.eclipse.wst.common.frameworks.datamodel.DataModelFactory;
 import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
 import org.eclipse.wst.common.project.facet.core.IFacetedProject;
 import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
 import org.eclipse.wst.common.project.facet.core.runtime.IRuntime;
 import org.springframework.ide.eclipse.core.SpringCore;
 import org.springframework.ide.eclipse.core.SpringCoreUtils;
 
 /**
  * @author Christian Dupuis
  * @author Leo Dos Santos
  */
 public class NewBundleProjectWizard extends NewElementWizard implements INewWizard {
 
 	private NewJavaProjectWizardPageOne projectPage;
 
 	private NewBundleInformationPage bundlePage;
 
 	private AbstractPropertiesPage propertiesPage;
 
 	private NewJavaProjectWizardPageTwo finalPage;
 
 	private IProjectProvider projectProvider;
 
 	private final AbstractFieldData bundleData;
 
 	private final IDataModel model;
 
 	private final String title = "New Bundle Project";
 
 	private Map<String, String> properties;
 
 	private String platformModule;
 
 	public NewBundleProjectWizard() {
 		super();
 		setWindowTitle(title);
 		setDefaultPageImageDescriptor(ServerIdeUiPlugin.getImageDescriptor("full/wizban/wizban-bundle.png"));
 		setNeedsProgressMonitor(true);
 		bundleData = new PluginFieldData();
 		model = DataModelFactory.createDataModel(new BundleFacetInstallDataModelProvider());
 	}
 
 	private void addFacetsToProject(final IJavaProject project) {
 		WorkspaceModifyOperation oper = new WorkspaceModifyOperation() {
 			@Override
 			protected void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException,
 					InterruptedException {
 
 				SpringCoreUtils.addProjectNature(project.getProject(), SpringCore.NATURE_ID, monitor);
 				IFacetedProject fProject = ProjectFacetsManager.create(project.getProject(), true, monitor);
 
 				// WST 3.0 only
 
 				if (model.getBooleanProperty(BundleFacetInstallDataModelProvider.ENABLE_WEB_BUNDLE)) {
 					fProject.installProjectFacet(ProjectFacetsManager.getProjectFacet("jst.java").getDefaultVersion(),
 							null, monitor);
 					fProject.installProjectFacet(ProjectFacetsManager.getProjectFacet(FacetCorePlugin.WEB_FACET_ID)
 							.getDefaultVersion(), null, monitor);
 
 					// wanna uninstall JavaScript facet, but it doesn't seem to
 					// be there yet
 					// fProject.uninstallProjectFacet(ProjectFacetsManager
 					// .getProjectFacet(FacetCorePlugin.WEB_JS_FACET_ID).getDefaultVersion(),
 					// null, monitor);
 
 					removeFromClasspath(project, "org.eclipse.jst.j2ee.internal.web.container", monitor);
 					removeFromClasspath(project, "org.eclipse.jst.j2ee.internal.module.container", monitor);
 				}
 
 				fProject.installProjectFacet(ProjectFacetsManager.getProjectFacet(FacetCorePlugin.BUNDLE_FACET_ID)
 						.getDefaultVersion(), null, monitor);
 				IRuntime runtime = (IRuntime) model.getProperty(IFacetProjectCreationDataModelProperties.FACET_RUNTIME);
 				if (runtime != null
 						&& runtime.supports(ProjectFacetsManager.getProjectFacet(FacetCorePlugin.BUNDLE_FACET_ID)
 								.getDefaultVersion())) {
 					fProject.setTargetedRuntimes(Collections.singleton(runtime), monitor);
 				}
 				if (model.getBooleanProperty(BundleFacetInstallDataModelProvider.ENABLE_SERVER_CLASSPATH_CONTAINER)) {
 					addToClasspath(project, JavaCore.newContainerEntry(FacetCorePlugin.CLASSPATH_CONTAINER_PATH),
 							monitor);
 				}
 			}
 		};
 
 		try {
 			getContainer().run(true, true, oper);
 		}
 		catch (InvocationTargetException e) {
 			SpringCore.log(e);
 		}
 		catch (InterruptedException e) {
 			SpringCore.log(e);
 		}
 	}
 
 	protected void removeFromClasspath(IJavaProject javaProject, String entryPath, IProgressMonitor monitor)
 			throws CoreException {
 		List<IClasspathEntry> rawClasspath = new ArrayList<IClasspathEntry>();
 		rawClasspath.addAll(Arrays.asList(javaProject.getRawClasspath()));
 
 		Iterator<IClasspathEntry> iter = rawClasspath.iterator();
 		while (iter.hasNext()) {
 			IClasspathEntry entry = iter.next();
 			if (entry.getPath() != null && entry.getPath().toString() != null
 					&& entry.getPath().toString().equals(entryPath)) {
 				iter.remove();
 			}
 		}
 
 		javaProject.setRawClasspath(rawClasspath.toArray(new IClasspathEntry[0]), monitor);
 	}
 
 	protected void addToClasspath(IJavaProject javaProject, IClasspathEntry entry, IProgressMonitor monitor)
 			throws CoreException {
 		IClasspathEntry[] current = javaProject.getRawClasspath();
 		IClasspathEntry[] updated = new IClasspathEntry[current.length + 1];
 		System.arraycopy(current, 0, updated, 0, current.length);
 		updated[current.length] = entry;
 		javaProject.setRawClasspath(updated, monitor);
 	}
 
 	@Override
 	public void addPages() {
 		projectPage = new NewBundleProjectSettingsPage();
 		addPage(projectPage);
 
 		projectProvider = new IProjectProvider() {
 
 			public IPath getLocationPath() {
 				return getProject().getLocation();
 			}
 
 			public IProject getProject() {
 				return ResourcesPlugin.getWorkspace().getRoot().getProject(getProjectName());
 			}
 
 			public String getProjectName() {
 				return projectPage.getProjectName();
 			}
 
 		};
 
 		bundlePage = new NewBundleInformationPage(title, projectProvider, bundleData, model);
 		addPage(bundlePage);
 
 		setPropertiesPage(new NullPropertiesPage());
 
 		finalPage = new NewBundleProjectCreationPage(projectPage);
 		addPage(finalPage);
 	}
 
 	@Override
 	public boolean canFinish() {
 		IWizardPage page = getContainer().getCurrentPage();
 		return super.canFinish() && page != projectPage;
 	}
 
 	@Override
 	protected void finishPage(IProgressMonitor monitor) throws InterruptedException, CoreException {
 		finalPage.performFinish(monitor);
 	}
 
 	private IWorkbenchPart getActivePart() {
 		IWorkbenchWindow activeWindow = getWorkbench().getActiveWorkbenchWindow();
 		if (activeWindow != null) {
 			IWorkbenchPage activePage = activeWindow.getActivePage();
 			if (activePage != null) {
 				return activePage.getActivePart();
 			}
 		}
 		return null;
 	}
 
 	@Override
 	public IJavaElement getCreatedElement() {
 		return finalPage.getJavaProject();
 	}
 
 	@Override
 	public boolean performCancel() {
 		finalPage.performCancel();
 		return super.performCancel();
 	}
 
 	@Override
 	public boolean performFinish() {
 		boolean res = super.performFinish();
 		if (res) {
 			bundlePage.performPageFinish();
 			properties = propertiesPage.getProperties();
 			platformModule = propertiesPage.getModuleType();
 
 			final IJavaElement element = getCreatedElement();
 			IJavaProject project = element.getJavaProject();
 			addFacetsToProject(project);
 			writeBundleData(project, platformModule, properties);
 
 			IWorkingSet[] workingSets = projectPage.getWorkingSets();
 			if (workingSets.length > 0) {
 				getWorkbench().getWorkingSetManager().addToWorkingSets(element, workingSets);
 			}
 			selectAndReveal(project.getProject());
 
 			Display.getDefault().asyncExec(new Runnable() {
 				public void run() {
 					IWorkbenchPart activePart = getActivePart();
 					if (activePart instanceof PackageExplorerPart) {
 						((PackageExplorerPart) activePart).tryToReveal(element);
 					}
 				}
 			});
 
 			IFile manifestFile = (IFile) project.getProject().findMember("src/META-INF/MANIFEST.MF");
 			if (manifestFile != null) {
 				IWorkbenchWindow workbenchWindow = getWorkbench().getActiveWorkbenchWindow();
 				IWorkbenchPage page = workbenchWindow.getActivePage();
 				try {
 					page.openEditor(new FileEditorInput(manifestFile), BundleManifestEditor.ID_EDITOR);
 				}
 				catch (PartInitException e) {
 					MessageDialog.openError(workbenchWindow.getShell(), "Error opening editor", e.getMessage());
 				}
 			}
 		}
 
 		return res;
 	}
 
 	private void writeBundleData(final IJavaProject project, final String platformModule,
 			final Map<String, String> properties) {
 		WorkspaceModifyOperation oper = new WorkspaceModifyOperation() {
 			@Override
 			protected void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException,
 					InterruptedException {
 				BundleManifestUtils.createNewBundleManifest(project, bundleData.getId(), bundleData.getVersion(),
 						bundleData.getProvider(), bundleData.getName(), platformModule, properties);
 				project.getProject().refreshLocal(IResource.DEPTH_INFINITE, monitor);
 			}
 		};
 
 		try {
 			getContainer().run(true, true, oper);
 		}
 		catch (InvocationTargetException e) {
 			e.printStackTrace();
 		}
 		catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 
 	protected void setPropertiesPage(AbstractPropertiesPage page) {
 		propertiesPage = page;
 		if (getPage(page.getName()) == null) {
 			addPage(page);
 		}
 	}
 
 	protected AbstractPropertiesPage getPropertiesPage() {
 		return propertiesPage;
 	}
 
 	protected NewBundleInformationPage getInformationPage() {
 		return bundlePage;
 	}
 
 	protected NewJavaProjectWizardPageTwo getFinalPage() {
 		return finalPage;
 	}
 
 }
