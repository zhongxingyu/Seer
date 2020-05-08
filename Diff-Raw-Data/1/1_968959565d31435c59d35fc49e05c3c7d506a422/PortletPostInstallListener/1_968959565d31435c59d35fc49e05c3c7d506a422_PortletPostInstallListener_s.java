 package org.jboss.tools.portlet.core.internal.project.facet;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Enumeration;
 import java.util.List;
 import java.util.Set;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipFile;
 
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.ProjectScope;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.preferences.IEclipsePreferences;
 import org.eclipse.core.runtime.preferences.IScopeContext;
 import org.eclipse.jst.common.project.facet.core.libprov.ILibraryProvider;
 import org.eclipse.jst.common.project.facet.core.libprov.LibraryInstallDelegate;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.helpers.ArchiveManifest;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.helpers.ArchiveManifestImpl;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.util.ArchiveUtil;
 import org.eclipse.jst.j2ee.internal.project.J2EEProjectUtilities;
 import org.eclipse.ui.dialogs.IOverwriteQuery;
 import org.eclipse.ui.wizards.datatransfer.FileSystemStructureProvider;
 import org.eclipse.ui.wizards.datatransfer.ImportOperation;
 import org.eclipse.ui.wizards.datatransfer.ZipFileStructureProvider;
 import org.eclipse.wst.common.componentcore.ComponentCore;
 import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
 import org.eclipse.wst.common.componentcore.resources.IVirtualFolder;
 import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
 import org.eclipse.wst.common.project.facet.core.IFacetedProject;
 import org.eclipse.wst.common.project.facet.core.IProjectFacet;
 import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
 import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
 import org.eclipse.wst.common.project.facet.core.events.IFacetedProjectEvent;
 import org.eclipse.wst.common.project.facet.core.events.IFacetedProjectListener;
 import org.eclipse.wst.common.project.facet.core.events.IProjectFacetActionEvent;
 import org.eclipse.wst.server.core.IRuntime;
 import org.jboss.ide.eclipse.as.core.server.IJBossServerConstants;
 import org.jboss.ide.eclipse.as.core.server.IJBossServerRuntime;
 import org.jboss.tools.portlet.core.IPortletConstants;
 import org.jboss.tools.portlet.core.Messages;
 import org.jboss.tools.portlet.core.PortletCoreActivator;
 import org.jboss.tools.portlet.core.libprov.AbstractLibraryProviderInstallOperationConfig;
 import org.jboss.tools.portlet.core.libprov.JSFPortletbridgeRuntimeLibraryProviderInstallOperationConfig;
 
 public class PortletPostInstallListener implements IFacetedProjectListener {
 
 	private static final IProjectFacet seamFacet = ProjectFacetsManager.getProjectFacet("jst.seam"); //$NON-NLS-1$
 	private static final IOverwriteQuery OVERWRITE_NONE_QUERY = new IOverwriteQuery()
     {
       public String queryOverwrite(String pathString)
       {
         return IOverwriteQuery.NO_ALL;
       }
     };
 	private String portletbridgeRuntime;
 	private boolean richfacesCapabilities = false;
 	private boolean isEPP = false;
 	//private boolean richfacesSelected;
 	private String richfacesType;
 	private String richfacesRuntime;
 	
 	
 	public void handleEvent(IFacetedProjectEvent event) {
 		if (event instanceof IProjectFacetActionEvent) {
 			IProjectFacetActionEvent actionEvent = (IProjectFacetActionEvent) event;
 			IProjectFacet projectFacet = actionEvent.getProjectFacet();
 			if (!IPortletConstants.JSFPORTLET_FACET_ID.equals(projectFacet
 					.getId())) {
 				return;
 			}
 		}
 		IFacetedProject facetedProject = event.getProject();
 		Set<IProjectFacetVersion> projectFacets = facetedProject
 				.getProjectFacets();
 		boolean isJSFPortlet = false;
 		boolean richfacesFromServerRuntime = false;
 		
 		for (IProjectFacetVersion projectFacetVersion : projectFacets) {
 			IProjectFacet projectFacet = projectFacetVersion.getProjectFacet();
 			if (IPortletConstants.JSFPORTLET_FACET_ID.equals(projectFacet
 					.getId())) {
 				isJSFPortlet = true;
 			}
 		}
 		if (!isJSFPortlet)
 			return;
 
 		if (isJSFPortlet) {
 			IProjectFacetActionEvent actionEvent = (IProjectFacetActionEvent) event;
 			IDataModel dataModel = (IDataModel) actionEvent.getActionConfig();
 			try {
 				LibraryInstallDelegate libraryDelegate = ( (LibraryInstallDelegate) dataModel.getProperty( IPortletConstants.JSFPORTLET_LIBRARY_PROVIDER_DELEGATE ) );
 				if (libraryDelegate != null) {
 					ILibraryProvider libraryProvider = libraryDelegate
 							.getLibraryProvider();
 					String providerId = libraryProvider.getId();
 					if (PortletCoreActivator.JSFPORTLETBRIDGE_LIBRARY_PROVIDER.equals(providerId)) {
 						JSFPortletbridgeRuntimeLibraryProviderInstallOperationConfig libraryConfig = (JSFPortletbridgeRuntimeLibraryProviderInstallOperationConfig) libraryDelegate
 								.getLibraryProviderOperationConfig(libraryProvider);
 						portletbridgeRuntime = libraryConfig.getPortletbridgeHome();
 					} else {
 						portletbridgeRuntime = null;
 					}
 					richfacesFromServerRuntime = PortletCoreActivator.JSFPORTLET_LIBRARY_PROVIDER.equals(providerId); //$NON-NLS-1$
 
 					if (PortletCoreActivator.JSFPORTLETBRIDGE_LIBRARY_PROVIDER.equals(providerId)
 							|| PortletCoreActivator.JSFPORTLET_LIBRARY_PROVIDER.equals(providerId)) {
 						AbstractLibraryProviderInstallOperationConfig libraryConfig = (AbstractLibraryProviderInstallOperationConfig) libraryDelegate.getLibraryProviderOperationConfig(libraryProvider);
 						richfacesCapabilities = libraryConfig.isAddRichfacesCapabilities();
 						isEPP = libraryConfig.isEPP();
 						richfacesRuntime = libraryConfig.getRichfacesRuntime();
 						richfacesType = libraryConfig.getRichfacesType();
 					}
 					
 				}
 			} catch (Exception e) {
 				//PortletCoreActivator.log(e);
 			}
 		}
 		
 		if (isJSFPortlet) {
 			if (portletbridgeRuntime != null || isEPP) {
 				addLibrariesFromPortletBridgeRuntime(facetedProject,portletbridgeRuntime);
 			} 
 			else if (richfacesFromServerRuntime) {
 				addLibrariesFromServerRuntime(facetedProject);
 			}
 		}
 	}
 
 	private void addLibrariesFromServerRuntime(IFacetedProject facetedProject) {
 		final boolean isSeamProject = facetedProject.hasProjectFacet(seamFacet);
 		final boolean addRichfacesFromRichfacesRuntime = richfacesCapabilities && IPortletConstants.LIBRARIES_PROVIDED_BY_RICHFACES.equals(richfacesType) && !isEPP;
 		if (addRichfacesFromRichfacesRuntime) {
 			addRichfacesFromRichfacesRuntime(facetedProject);
 		}
 		if (isSeamProject) {	
 			return;
 		}
 		org.eclipse.wst.common.project.facet.core.runtime.IRuntime facetRuntime = facetedProject.getPrimaryRuntime();
 		if (facetRuntime == null) {
 			return;
 		}
 		IRuntime runtime = PortletCoreActivator.getRuntime(facetRuntime);
 		if (runtime == null) {
 			return;
 		}
 		IJBossServerRuntime jbossRuntime = (IJBossServerRuntime)runtime.loadAdapter(IJBossServerRuntime.class, new NullProgressMonitor());
 		if (jbossRuntime != null) {
 			IPath jbossLocation = runtime.getLocation();
 			IPath configPath = jbossLocation.append(IJBossServerConstants.SERVER).append(jbossRuntime.getJBossConfiguration());
 			IPath portletLib = configPath.append(IPortletConstants.PORTLET_SAR_LIB);
 			File libFile = portletLib.toFile();
 			if (!libFile.exists()) {
 				portletLib = configPath.append(IPortletConstants.PORTLET_SAR_HA_LIB);
 				libFile = portletLib.toFile();
 			}
 			File richfacesLib = libFile;
 			if (!richfacesLib.exists()) {
 				PortletCoreActivator.log(null, Messages.PortletPostInstallListener_Cannot_find_Richfaces_ibraries);
 				return;
 			}
 			if (!richfacesLib.isDirectory()) {
 				PortletCoreActivator.log(null, Messages.PortletPostInstallListener_Cannot_find_Richfaces_ibraries);
 				return;
 			}
 			try {
 				IProject project = facetedProject.getProject();
 				final IProject earProject = getEarProject(project, isSeamProject);
 				
 				String[] fileList = richfacesLib.list(new FilenameFilter() {
 
 					public boolean accept(File dir, String name) {
 						if (!addRichfacesFromRichfacesRuntime) {
 							if (name.startsWith("richfaces-ui") && name.endsWith(".jar")) { //$NON-NLS-1$ //$NON-NLS-2$
 								return true;
 							}
 							if (name.startsWith("richfaces-impl") && name.endsWith(".jar")) { //$NON-NLS-1$ //$NON-NLS-2$
 								return true;
 							}
 							if (earProject == null) {
 								if (name.startsWith("richfaces-api") && name.endsWith(".jar")) { //$NON-NLS-1$ //$NON-NLS-2$
 									return true;
 								}
 							}
 						}
 						if (!isSeamProject) {
 							if (name.startsWith("portal")) { //$NON-NLS-1$
 								return false;
 							}
 							if (name.startsWith("portletbridge")) { //$NON-NLS-1$
 								return false;
 							}
 							return true;
 						}
 						return false;
 					}
 
 				});
 				List<File> filesToImport = new ArrayList<File>();
 				for (int i = 0; i < fileList.length; i++) {
 					filesToImport.add(new File(richfacesLib, fileList[i]));
 				}
 				IVirtualComponent component = ComponentCore.createComponent(project);
 				IVirtualFolder rootFolder = component.getRootFolder();
 				IContainer folder = rootFolder.getUnderlyingFolder();
 				IContainer webinf = folder.getFolder(new Path(IPortletConstants.WEB_INF_LIB));
 			
 				deleteOldRichfacesLibs(earProject, webinf);
 
 				ImportOperation importOperation = new ImportOperation(webinf.getFullPath(), richfacesLib,
 					FileSystemStructureProvider.INSTANCE,
 					PortletCoreActivator.OVERWRITE_ALL_QUERY, filesToImport);
 				importOperation.setCreateContainerStructure(false);
 				importOperation.run(new NullProgressMonitor());
 				if (earProject != null) {
 					fileList = richfacesLib.list(new FilenameFilter() {
 
 						public boolean accept(File dir, String name) {
 							if (name.startsWith("richfaces-api") && name.endsWith(".jar")) { //$NON-NLS-1$ //$NON-NLS-2$
 								return true;
 							}
 							return false;
 						}
 
 					});
 					filesToImport = new ArrayList<File>();
 					for (int i = 0; i < fileList.length; i++) {
 						filesToImport.add(new File(richfacesLib, fileList[i]));
 					}
 					component = ComponentCore.createComponent(earProject);
 					rootFolder = component.getRootFolder();
 					folder = rootFolder.getUnderlyingFolder();
 				
 					deleteOldRichFacesApi(folder);
 
 					importOperation = new ImportOperation(folder.getFullPath(), richfacesLib,
 						FileSystemStructureProvider.INSTANCE,
 						PortletCoreActivator.OVERWRITE_ALL_QUERY, filesToImport);
 					importOperation.setCreateContainerStructure(false);
 					importOperation.run(new NullProgressMonitor());
 					updateEARLibraries(project,isSeamProject);
 				}
 			} catch (Exception e) {
 				PortletCoreActivator.log(e,Messages.JSFPortletFacetInstallDelegate_Error_loading_classpath_container);
 			}
 		}
 	}
 
 	private void addRichfacesFromRichfacesRuntime(
 			IFacetedProject facetedProject) {
 		final boolean isSeamProject = facetedProject.hasProjectFacet(seamFacet);
 		if (!isSeamProject && !richfacesCapabilities) {
 			return;
 		}
 		File richfacesRuntimeHome = new File(richfacesRuntime);
 		File richfacesLib = new File(richfacesRuntimeHome, "lib"); //$NON-NLS-1$
 		if (!richfacesLib.exists()) {
 			PortletCoreActivator.log(null, Messages.PortletPostInstallListener_Cannot_find_Richfaces_Runtime);
 			return;
 		}
 		if (!richfacesLib.isDirectory()) {
 			PortletCoreActivator.log(null,Messages.PortletPostInstallListener_Invalid_Richfaces_Runtime);
 			return;
 		}
 		try {
 			IProject project = facetedProject.getProject();
 			final IProject earProject = getEarProject(project, isSeamProject);
 			String[] fileList = richfacesLib.list(new FilenameFilter() {
 
 				public boolean accept(File dir, String name) {
 					if (name.startsWith("richfaces-ui") && name.endsWith(".jar")) { //$NON-NLS-1$ //$NON-NLS-2$
 						return true;
 					}
 					if (name.startsWith("richfaces-impl") && name.endsWith(".jar")) { //$NON-NLS-1$ //$NON-NLS-2$
 						return true;
 					}
 					if (earProject == null) {
 						if (name.startsWith("richfaces-api") && name.endsWith(".jar")) { //$NON-NLS-1$ //$NON-NLS-2$
 							return true;
 						}
 					}
 					return false;
 				}
 
 			});
 			List<File> filesToImport = new ArrayList<File>();
 			for (int i = 0; i < fileList.length; i++) {
 				filesToImport.add(new File(richfacesLib, fileList[i]));
 			}
 			IVirtualComponent component = ComponentCore
 					.createComponent(project);
 			IVirtualFolder rootFolder = component.getRootFolder();
 			IContainer folder = rootFolder.getUnderlyingFolder();
 			IContainer webinf = folder.getFolder(new Path(
 					IPortletConstants.WEB_INF_LIB));
 			
 			deleteOldRichfacesLibs(earProject, webinf);
 
 			ImportOperation importOperation = new ImportOperation(webinf
 					.getFullPath(), richfacesLib,
 					FileSystemStructureProvider.INSTANCE,
 					PortletCoreActivator.OVERWRITE_ALL_QUERY, filesToImport);
 			importOperation.setCreateContainerStructure(false);
 			importOperation.run(new NullProgressMonitor());
 			if (earProject != null) {
 				fileList = richfacesLib.list(new FilenameFilter() {
 
 					public boolean accept(File dir, String name) {
 						if (name.startsWith("richfaces-api") && name.endsWith(".jar")) { //$NON-NLS-1$ //$NON-NLS-2$
 							return true;
 						}
 						return false;
 					}
 
 				});
 				filesToImport = new ArrayList<File>();
 				for (int i = 0; i < fileList.length; i++) {
 					filesToImport.add(new File(richfacesLib, fileList[i]));
 				}
 				component = ComponentCore
 						.createComponent(earProject);
 				rootFolder = component.getRootFolder();
 				folder = rootFolder.getUnderlyingFolder();
 				
 				deleteOldRichFacesApi(folder);
 
 				importOperation = new ImportOperation(folder
 						.getFullPath(), richfacesLib,
 						FileSystemStructureProvider.INSTANCE,
 						PortletCoreActivator.OVERWRITE_ALL_QUERY, filesToImport);
 				importOperation.setCreateContainerStructure(false);
 				importOperation.run(new NullProgressMonitor());
 				updateEARLibraries(project,isSeamProject);
 			}
 		} catch (Exception e) {
 			PortletCoreActivator
 					.log(e,Messages.JSFPortletFacetInstallDelegate_Error_loading_classpath_container);
 		}
 	}
 
 	private void updateEARLibraries(IProject project, boolean isSeamProject) {
 		IProject ejbProj = getEjbProject(project, isSeamProject);
 		
 		IProject earProject = getEarProject(project, isSeamProject);
 		IVirtualComponent component = ComponentCore
 			.createComponent(earProject);
 		IVirtualFolder rootFolder = component.getRootFolder();
 		IContainer folder = rootFolder.getUnderlyingFolder();
 		File earContentFolder = folder.getLocation().toFile();
 		File[] earJars = earContentFolder.listFiles(new FilenameFilter() {
 			public boolean accept(File dir, String name) {
 				return name.lastIndexOf(".jar") > 0; //$NON-NLS-1$
 			}
 		});
 		String clientURI = ""; //$NON-NLS-1$
 		for (File file : earJars) {
 			clientURI += " " + file.getName(); //$NON-NLS-1$
 		}
 		
 		try {
 			new UpdateManifestOperation(ejbProj,clientURI,true).run();
 			new UpdateManifestOperation(project,clientURI,true).run();
 		} catch (Exception e) {
 			PortletCoreActivator.log(e);
 		}
 	}
 
 	private void addLibrariesFromPortletBridgeRuntime(IFacetedProject facetedProject, String portletbridgeRuntime) {
 		File portletbridgeHome;
 		if (isEPP) {
 			portletbridgeHome = PortletCoreActivator.getEPPDir(facetedProject, PortletCoreActivator.PORTLETBRIDGE);
 		} else {
 			portletbridgeHome = new File(portletbridgeRuntime);
 		}
 		if (!portletbridgeHome.exists()) {
 			PortletCoreActivator.log(null, Messages.PortletPostInstallListener_Cannot_find_Portletbridge_Runtime);
 			return;
 		}
 		if (!portletbridgeHome.isDirectory()) {
 			PortletCoreActivator.log(null, Messages.PortletPostInstallListener_Invalid_Portletbridge_Runtime);
 			return;
 		}
 		if (isEPP) {
 			getLibrariesFromEPP(facetedProject);
 		} else {
 			getLibrariesFromExamples(facetedProject, portletbridgeHome);
 		}
 	}
 
 	private void getLibrariesFromEPP(IFacetedProject facetedProject) {
 		File seamHome = PortletCoreActivator.getEPPDir(facetedProject,
 				PortletCoreActivator.SEAM);
 		File seamLib = new File(seamHome, "lib"); //$NON-NLS-1$
 		if (seamLib == null || !seamLib.isDirectory()) {
 			PortletCoreActivator.log(null,
 					Messages.PortletPostInstallListener_Cannot_find_the_seam_lib_directory);
 			return;
 		}
 		try {
 			boolean isSeamProject = facetedProject.hasProjectFacet(seamFacet);
 			IProject project = facetedProject.getProject();
 			List<File> filesToImport = prepareList(seamLib, facetedProject,
 					isSeamProject);
 			if (filesToImport != null) {
 				IVirtualComponent component = ComponentCore
 						.createComponent(project);
 				IVirtualFolder rootFolder = component.getRootFolder();
 				IContainer folder = rootFolder.getUnderlyingFolder();
 				IContainer webinf = folder.getFolder(new Path(
 						IPortletConstants.WEB_INF_LIB));
 				ImportOperation op = new ImportOperation(webinf.getFullPath(),
 						seamLib, FileSystemStructureProvider.INSTANCE,
 						OVERWRITE_NONE_QUERY, filesToImport);
 				op.setCreateContainerStructure(false);
 				op.run(new NullProgressMonitor());
 			}
 			IProject earProject = getEarProject(project, isSeamProject);
 			filesToImport = prepareEarList(seamLib);
 			if (earProject != null && filesToImport != null) {
 
 				IVirtualComponent component = ComponentCore.createComponent(earProject);
 				IVirtualFolder rootFolder = component.getRootFolder();
 				IContainer folder = rootFolder.getUnderlyingFolder();
 				deleteOldRichFacesApi(folder);
 
 				ImportOperation op = new ImportOperation(folder.getFullPath(), seamLib,
 						FileSystemStructureProvider.INSTANCE,
 						OVERWRITE_NONE_QUERY, filesToImport);
 				op.setCreateContainerStructure(false);
 				op.run(new NullProgressMonitor());
 				updateEARLibraries(project, isSeamProject);
 			}
 		} catch (Exception e) {
 			PortletCoreActivator.log(e);
 		}
 	}
 
 	private List<File> prepareEarList(File seamLib) {
 		File[] files = seamLib.listFiles(new FileFilter() {
 			
			@Override
 			public boolean accept(File pathname) {
 				String name = pathname.getName();
 				if (name.endsWith(".jar") && name.startsWith("richfaces-api")) { //$NON-NLS-1$ //$NON-NLS-2$
 						return true;
 					
 				}
 				return false;
 			}
 		});
 		if (files == null) {
 			return null; 
 		}
 		return Arrays.asList(files);
 	}
 
 	private List<File> prepareList(File directory,
 			IFacetedProject facetedProject, final boolean isSeamProject) {
 		
 		final IProject earProject = getEarProject(facetedProject.getProject(),isSeamProject);
 		File[] files = directory.listFiles(new FileFilter() {
 			
 			public boolean accept(File pathname) {
 				String name = pathname.getName();
 				if (name == null) {
 					return false;
 				}
 				if (!name.endsWith(".jar")) { //$NON-NLS-1$
 					return false;
 				}
 				if (name.startsWith("jsf-facelets")) { //$NON-NLS-1$
 					return true;
 				}
 				if (richfacesCapabilities) {
 					if (name.startsWith("richfaces-ui")) { //$NON-NLS-1$
 						return true;
 					}
 					if (name.startsWith("richfaces-impl")) { //$NON-NLS-1$
 						return true;
 					}
 					if (earProject == null) {
 						if (name.startsWith("richfaces-api")) { //$NON-NLS-1$
 							return true;
 						}
 					}
 				}
 				if (!isSeamProject) {
 					if (name.startsWith("commons-beanutils")) { //$NON-NLS-1$
 						return true;
 					}
 					if (name.startsWith("commons-digester")) { //$NON-NLS-1$
 						return true;
 					}
 				}
 				return false;
 			}
 		});
 		if (files == null) {
 			return null;
 		}
 		return Arrays.asList(files);
 	}
 
 	private void getLibrariesFromExamples(IFacetedProject facetedProject,
 			File portletbridgeHome) {
 		File examplesHome = new File(portletbridgeHome, "examples"); //$NON-NLS-1$
 		if (!examplesHome.exists() || !examplesHome.isDirectory()) {
 			PortletCoreActivator.log(null,
 							Messages.PortletPostInstallListener_Cannot_find_the_examples_directory);
 			return;
 		}
 		File richFacesPortletZip = getRichFacesExamples(examplesHome);
 		if (!richFacesPortletZip.exists() || !richFacesPortletZip.isFile()) {
 			PortletCoreActivator.log(null,
 							Messages.PortletPostInstallListener_Cannot_find_the_RichFacesPortlet_war_file);
 			return;
 		}
 		try {
 			ZipFile zipFile = new ZipFile(richFacesPortletZip);
 			ZipFileStructureProvider structureProvider = new ZipFileStructureProvider(
 					zipFile);
 			boolean isSeamProject = facetedProject
 					.hasProjectFacet(seamFacet);
 			final boolean addRichfacesFromRichfacesRuntime = richfacesCapabilities
 					&& IPortletConstants.LIBRARIES_PROVIDED_BY_RICHFACES
 							.equals(richfacesType);
 			if (addRichfacesFromRichfacesRuntime) {
 				addRichfacesFromRichfacesRuntime(facetedProject);
 			}
 			List<ZipEntry> list = prepareList(zipFile, facetedProject,
 					isSeamProject, addRichfacesFromRichfacesRuntime);
 
 			IProject project = facetedProject.getProject();
 			IProject earProject = getEarProject(project, isSeamProject);
 
 			IVirtualComponent component = ComponentCore
 					.createComponent(project);
 
 			IVirtualFolder rootFolder = component.getRootFolder();
 			IContainer folder = rootFolder.getUnderlyingFolder();
 			IContainer webinf = folder.getFolder(new Path(
 					IPortletConstants.WEB_INF_LIB));
 			if (!addRichfacesFromRichfacesRuntime) {
 				deleteOldRichfacesLibs(earProject, webinf);
 			}
 			IPath destPath = folder.getFullPath();
 
 			ImportOperation op = new ImportOperation(destPath,
 					structureProvider.getRoot(), structureProvider,
 					OVERWRITE_NONE_QUERY, list);
 			op.run(new NullProgressMonitor());
 			if (earProject != null) {
 				list = prepareEarList(zipFile);
 
 				component = ComponentCore.createComponent(earProject);
 				rootFolder = component.getRootFolder();
 				folder = rootFolder.getUnderlyingFolder();
 				deleteOldRichFacesApi(folder);
 				destPath = folder.getFullPath();
 				ZipEntry root = zipFile
 						.getEntry(IPortletConstants.WEB_INF_LIB);
 				if (root == null) {
 					root = zipFile.getEntry(IPortletConstants.LIB);
 				}
 				op = new ImportOperation(destPath, root, structureProvider,
 						OVERWRITE_NONE_QUERY, list);
 				op.setCreateContainerStructure(false);
 				op.run(new NullProgressMonitor());
 				updateEARLibraries(project, isSeamProject);
 			}
 		} catch (Exception e) {
 			PortletCoreActivator.log(e);
 		}
 	}
 
 	private File getRichFacesExamples(File examplesHome) {
 		File file = getExampleFile(examplesHome,"RichFacesPortlet", "war"); //$NON-NLS-1$ //$NON-NLS-2$
 		if (file != null && file.isFile()) {
 			return file;
 		}
 		file = getExampleFile(examplesHome, "richFacesPortlet", ".war");  //$NON-NLS-1$//$NON-NLS-2$
 		if (file != null && file.isFile()) {
 			return file;
 		}
 		file = getExampleFile(examplesHome, "seamPortlet", ".war");  //$NON-NLS-1$//$NON-NLS-2$
 		if (file != null && file.isFile()) {
 			return file;
 		}
 		file = getExampleFile(examplesHome, "seam", ".ear");  //$NON-NLS-1$//$NON-NLS-2$
 		if (file != null && file.isFile()) {
 			return file;
 		}
 		return null;
 	}
 
 	private File getExampleFile(File examplesHome, final String prefix, final String suffix) {
 		File[] listFiles = examplesHome.listFiles(new FilenameFilter() {
 			
 			public boolean accept(File dir, String name) {
 				if (name.startsWith(prefix) && name.endsWith(suffix)) { 
 					return true;
 				}
 				return false;
 			}
 		});
 		if (listFiles.length > 0) {
 			return listFiles[0];
 		}
 		return null;
 	}
 
 	private void deleteOldRichFacesApi(IContainer folder) throws CoreException {
 		IResource[] members = folder.members();
 		for (int i = 0; i < members.length; i++) {
 			IResource resource = members[i];
 			if (resource != null && resource.exists()) {
 				if (resource.getName().startsWith("richfaces-api") //$NON-NLS-1$
 						&& resource.getName().endsWith("jar")) { //$NON-NLS-1$
 					resource.delete(true, null);
 					break;
 				}
 			}
 		}
 	}
 
 	private void deleteOldRichfacesLibs(IProject earProject, IContainer webinf)
 			throws CoreException {
 		
 		if (webinf != null && webinf.exists()) {
 			IResource[] members = webinf.members();
 			for (int i = 0; i < members.length; i++) {
 				IResource resource = members[i];
 				if (resource != null && resource.exists()) {
 					if (resource.getName().startsWith("richfaces-ui") //$NON-NLS-1$
 							&& resource.getName().endsWith("jar")) { //$NON-NLS-1$
 						resource.delete(true, null);
 					}
 					if (resource.getName().startsWith("richfaces-impl") //$NON-NLS-1$
 							&& resource.getName().endsWith("jar")) { //$NON-NLS-1$
 						resource.delete(true, null);
 					}
 					if (earProject == null) {
 						if (resource.getName().startsWith("richfaces-api") //$NON-NLS-1$
 								&& resource.getName().endsWith("jar")) { //$NON-NLS-1$
 							resource.delete(true, null);
 						}
 					}
 				}
 			}
 		}
 	}
 
 	private IProject getEarProject(IProject project, boolean isSeamProject) {
 		if (isSeamProject && project != null) {
 			IEclipsePreferences preferences = getSeamPreferences(project);
 			String earProjectName = preferences.get("seam.ear.project", null); //$NON-NLS-1$
 			if (earProjectName == null) {
 				return null;
 			}
 			IProject earProject = ResourcesPlugin.getWorkspace().getRoot().getProject(earProjectName);
 			if (earProject != null && earProject.isOpen()) {
 				return earProject;
 			}
 		}
 		return null;
 	}
 	
 	private IProject getEjbProject(IProject project, boolean isSeamProject) {
 		if (isSeamProject && project != null) {
 			IEclipsePreferences preferences = getSeamPreferences(project);
 			String ejbProjectName = preferences.get("seam.ejb.project", null); //$NON-NLS-1$
 			if (ejbProjectName == null) {
 				return null;
 			}
 			IProject ejbProject = ResourcesPlugin.getWorkspace().getRoot().getProject(ejbProjectName);
 			if (ejbProject != null && ejbProject.isOpen()) {
 				return ejbProject;
 			}
 		}
 		return null;
 	}
 	
 	public static IEclipsePreferences getSeamPreferences(IProject project) {
 		IScopeContext projectScope = new ProjectScope(project);
 		return projectScope.getNode("org.jboss.tools.seam.core"); //$NON-NLS-1$
 	}
 	
 	private List<ZipEntry> prepareList(ZipFile rootEntry, IFacetedProject facetedProject, boolean isSeamProject, boolean addRichfacesFromRichfacesRuntime) {
 		if (rootEntry == null) {
 			return null;
 		}
 		List<ZipEntry> list = new ArrayList<ZipEntry>();
 		if (!isSeamProject && !richfacesCapabilities) {
 			return list;
 		}
 		
 		Enumeration<? extends ZipEntry> entries = rootEntry.entries();
 		IProject earProject = getEarProject(facetedProject.getProject(),isSeamProject);
 		
 		while (entries.hasMoreElements()) {
 			ZipEntry entry = entries.nextElement();
 			if (entry.getName().endsWith(".jar")) { //$NON-NLS-1$
 				if (!addRichfacesFromRichfacesRuntime) {
 					if (entry.getName().startsWith("WEB-INF/lib/richfaces-ui")) { //$NON-NLS-1$
 						list.add(entry);
 					}
 					if (entry.getName()
 							.startsWith("WEB-INF/lib/richfaces-impl")) { //$NON-NLS-1$
 						list.add(entry);
 					}
 					if (earProject == null) {
 						if (entry.getName().startsWith(
 								"WEB-INF/lib/richfaces-api")) { //$NON-NLS-1$
 							list.add(entry);
 						}
 					}
 				}
 				if (!isSeamProject) {
 					if (entry.getName().startsWith(
 							"WEB-INF/lib/commons-beanutils") //$NON-NLS-1$
 							|| entry.getName().startsWith(
 									"WEB-INF/lib/commons-digester") //$NON-NLS-1$
 							|| entry.getName().startsWith(
 									"WEB-INF/lib/jsf-facelets")) { //$NON-NLS-1$
 						list.add(entry);
 					}
 				}
 			}
 		}
 		return list;
 	}
 
 	private List<ZipEntry> prepareEarList(ZipFile zipFile) {
 		if (zipFile == null) {
 			return null;
 		}
 		List<ZipEntry> list = new ArrayList<ZipEntry>();
 		Enumeration<? extends ZipEntry> entries = zipFile.entries();
 
 		while (entries.hasMoreElements()) {
 			ZipEntry entry = entries.nextElement();
 			if (entry.getName().endsWith(".jar")) { //$NON-NLS-1$
 				if (entry.getName().startsWith("WEB-INF/lib/richfaces-api")) { //$NON-NLS-1$
 					list.add(entry);
 				}
 			}
 		}
 		return list;
 	}
 	
 	private class UpdateManifestOperation implements Runnable {
 		protected IProject project;
 		protected String classPathValue;
 		protected boolean replace;
 
 		public UpdateManifestOperation(IProject project,
 				String aSpaceDelimitedPath, boolean replaceInsteadOfMerge) {
 			super();
 			this.project = project;
 			classPathValue = aSpaceDelimitedPath;
 			replace = replaceInsteadOfMerge;
 		}
 
 		public void run() {
 			ArchiveManifest mf = J2EEProjectUtilities.readManifest(project);
 			if (mf == null)
 				mf = new ArchiveManifestImpl();
 			mf.addVersionIfNecessary();
 			if (replace)
 				mf.setClassPath(classPathValue);
 			else
 				mf.mergeClassPath(ArchiveUtil.getTokens(classPathValue));
 			try {
 				J2EEProjectUtilities.writeManifest(project, mf);
 			} catch (IOException e) {
 				PortletCoreActivator.log(e);
 			}
 
 		}
 	}
 
 }
