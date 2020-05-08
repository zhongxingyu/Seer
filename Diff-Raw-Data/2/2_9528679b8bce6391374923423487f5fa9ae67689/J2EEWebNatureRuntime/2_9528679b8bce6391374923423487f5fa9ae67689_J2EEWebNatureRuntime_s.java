 package org.eclipse.jst.j2ee.internal.web.operations;
 
 /*
  * Licensed Material - Property of IBM (C) Copyright IBM Corp. 2001, 2002 - All Rights Reserved. US
  * Government Users Restricted Rights - Use, duplication or disclosure restricted by GSA ADP
  * Schedule Contract with IBM Corp.
  */
 import java.io.ByteArrayInputStream;
 import java.io.InputStream;
 import java.util.Map;
 
 import org.eclipse.core.resources.ICommand;
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IProjectDescription;
 import org.eclipse.core.resources.IProjectNature;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IWorkspace;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.URIConverter;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jst.j2ee.application.ApplicationPackage;
 import org.eclipse.jst.j2ee.application.Module;
 import org.eclipse.jst.j2ee.common.XMLResource;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.Archive;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.WARFile;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.exception.OpenFailureException;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.helpers.ArchiveOptions;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.strategy.LoadStrategy;
 import org.eclipse.jst.j2ee.internal.J2EEConstants;
 import org.eclipse.jst.j2ee.internal.J2EEEditModel;
 import org.eclipse.jst.j2ee.internal.J2EEVersionConstants;
 import org.eclipse.jst.j2ee.internal.plugin.J2EEPlugin;
 import org.eclipse.jst.j2ee.internal.project.IWebNatureConstants;
 import org.eclipse.jst.j2ee.internal.project.J2EEModuleNature;
 import org.eclipse.jst.j2ee.internal.web.archive.operations.WTProjectLoadStrategyImpl;
 import org.eclipse.jst.j2ee.internal.web.plugin.WebPlugin;
 import org.eclipse.jst.j2ee.internal.webservices.WebServiceEditModel;
 import org.eclipse.jst.j2ee.web.taglib.ITaglibRegistry;
 import org.eclipse.jst.j2ee.webapplication.WebApp;
 import org.eclipse.jst.j2ee.webapplication.WebAppResource;
 import org.eclipse.wst.common.internal.emfworkbench.integration.EditModel;
 import org.eclipse.wst.common.modulecore.ModuleCoreNature;
 import org.eclipse.wst.common.modulecore.internal.impl.ModuleCoreURIConverter;
 import org.eclipse.wst.web.internal.operation.IBaseWebNature;
 import org.eclipse.wst.web.internal.operation.ILibModule;
 import org.eclipse.wst.web.internal.operation.WebSettings;
 
 import com.ibm.wtp.emf.workbench.EMFWorkbenchContextBase;
 import com.ibm.wtp.emf.workbench.ProjectResourceSet;
 import com.ibm.wtp.emf.workbench.ProjectUtilities;
 import com.ibm.wtp.emf.workbench.WorkbenchURIConverter;
 
 /*
  * Licensed Materials - Property of IBM, WebSphere Studio Workbench (c) Copyright IBM Corp 2000
  */
 public class J2EEWebNatureRuntime extends J2EEModuleNature implements IDynamicWebNature, IWebNatureConstants {
 	static protected String PROJECTTYPE_J2EE_VALUE = "J2EE"; //$NON-NLS-1$
 	private static final String WEB_PROJECT_12_OVERLAY = "1_2_ovr"; //$NON-NLS-1$
 	private static final String WEB_PROJECT_13_OVERLAY = "1_3_ovr"; //$NON-NLS-1$
 	private static final String WEB_PROJECT_14_OVERLAY = "1_4_ovr"; //$NON-NLS-1$
 	public static final String SERVLETLEVEL_2_2 = "Servlet 2.2"; //$NON-NLS-1$
 	public static final String SERVLETLEVEL_2_3 = "Servlet 2.3"; //$NON-NLS-1$
 	public static final String SERVLETLEVEL_2_4 = "Servlet 2.4"; //$NON-NLS-1$
 	public static final String JSPLEVEL_1_1 = "JSP 1.1"; //$NON-NLS-1$
 	public static final String JSPLEVEL_1_2 = "JSP 1.2"; //$NON-NLS-1$
 	public static final String JSPLEVEL_2_0 = "JSP 2.0"; //$NON-NLS-1$
 	public static final String DEFAULT_JSPLEVEL = JSPLEVEL_1_1;
 	public static final String DEFAULT_SERVLETLEVEL = SERVLETLEVEL_2_2;
 	// Version number may not change with every release,
 	// only when changes necessitate a new version number
 	public static int CURRENT_VERSION = 600;
 	/*
 	 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 	 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! PLEASE NOTE:
 	 * 
 	 * If you add any instance variables, make sure to update the resetWebSettings() method if
 	 * appropriate.
 	 * 
 	 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 	 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 	 */
 	public static int instanceCount = 1;
 	public int instanceStamp = 0;
 	protected RelationData relationData;
 	protected String contextRoot = null;
 	protected String jspLevel = null;
 	protected String servletLevel = null;
 	protected ILibModule[] libModules = null;
 	protected String[] featureIds = null;
 	protected int fVersion = -1;
 	private int fWebNatureType = -1;
 	protected WebSettings fWebSettings;
 
 	/*
 	 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 	 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! PLEASE NOTE:
 	 * 
 	 * If you add any instance variables, make sure to update the resetWebSettings() method if
 	 * appropriate.
 	 * 
 	 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 	 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 	 */
 	/**
 	 * WebNatureRuntime constructor comment.
 	 */
 	public J2EEWebNatureRuntime() {
 		super();
 		instanceStamp = instanceCount;
 		instanceCount++;
 		//        class WebSettingsModifier implements IResourceChangeListener,
 		// IResourceDeltaVisitor {
 		//
 		//            public void resourceChanged(IResourceChangeEvent event) {
 		//                if (event.getSource() instanceof IWorkspace) {
 		//                    IResourceDelta delta = event.getDelta();
 		//                    switch (event.getType()) {
 		//                        case IResourceChangeEvent.PRE_AUTO_BUILD :
 		//                            if (delta != null) {
 		//                                try {
 		//                                    delta.accept(this);
 		//                                } catch (CoreException e) {
 		//                                }
 		//                            }
 		//                            break;
 		//                    }
 		//                }
 		//            }
 		//            public boolean visit(IResourceDelta delta) throws CoreException {
 		//                if (delta != null) {
 		//                    // get target IResource
 		//                    final IResource resource = delta.getResource();
 		//                    if (resource != null) {
 		//                        if (resource.getType() == IResource.FILE) {
 		//                            // If the websettings file is being modified, reset
 		//							// all the cached values
 		//                            // in the nature
 		//                            IFile file = (IFile) resource;
 		//                            if
 		// ((file.getName().equals(IWebNatureConstants.WEBSETTINGS_FILE_NAME))
 		// && (resource.getProject().getName().equals(getProject().getName())))
 		// {
 		//                                resetWebSettings();
 		//                            }
 		//                        }
 		//                    }
 		//                    return true;
 		//                }
 		//                return false;
 		//            }
 		//
 		//        }
 		//        IWorkspace workspace = ResourcesPlugin.getWorkspace();
 		//        workspace.addResourceChangeListener(new WebSettingsModifier(),
 		// IResourceChangeEvent.PRE_AUTO_BUILD);
 	}
 
 	/*
 	 * Do nothing with a cvs ignore file for web projects,
 	 */
 	public void addCVSIgnoreFile() {
 		//Default nothing
 	}
 
 	public void addLibDirBuilder() throws CoreException {
 		addToFrontOfBuildSpec(J2EEPlugin.LIBDIRCHANGE_BUILDER_ID);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.j2ee.internal.internal.j2eeproject.J2EENature#getJ2EEVersion()
 	 */
 	public int getJ2EEVersion() {
 		int j2eeVersion;
 		switch (getModuleVersion()) {
 			case J2EEVersionConstants.WEB_2_2_ID :
 				j2eeVersion = J2EEVersionConstants.J2EE_1_2_ID;
 				break;
 			case J2EEVersionConstants.WEB_2_3_ID :
 				j2eeVersion = J2EEVersionConstants.J2EE_1_3_ID;
 				break;
 			default :
 				j2eeVersion = J2EEVersionConstants.J2EE_1_4_ID;
 		}
 		return j2eeVersion;
 	}
 
 	/**
 	 * Adds a builder to the build spec for the given project.
 	 */
 	protected void addToFrontOfBuildSpec(String builderID) throws CoreException {
 		IProjectDescription description = getProject().getDescription();
 		ICommand[] commands = description.getBuildSpec();
 		boolean found = false;
 		for (int i = 0; i < commands.length; ++i) {
 			if (commands[i].getBuilderName().equals(builderID)) {
 				found = true;
 				break;
 			}
 		}
 		if (!found) {
 			ICommand command = description.newCommand();
 			command.setBuilderName(builderID);
 			ICommand[] newCommands = new ICommand[commands.length + 1];
 			System.arraycopy(commands, 0, newCommands, 1, commands.length);
 			newCommands[0] = command;
 			IProjectDescription desc = getProject().getDescription();
 			desc.setBuildSpec(newCommands);
 			getProject().setDescription(desc, null);
 		}
 	}
 
 	public Archive asArchive() throws OpenFailureException {
 		return asWARFile();
 	}
 
 	public Archive asArchive(boolean shouldExportSource) throws OpenFailureException {
 		if (getWebNatureType() == IWebNatureConstants.J2EE_WEB_PROJECT)
 			return asWARFile(shouldExportSource);
 
 		return null;
 	}
 
 	public org.eclipse.jst.j2ee.commonarchivecore.internal.WARFile asWARFile() throws OpenFailureException {
 		if (getWebNatureType() == IWebNatureConstants.J2EE_WEB_PROJECT) {
 			IProject proj = getProject();
 			if (proj == null)
 				return null;
 			LoadStrategy loader = new WTProjectLoadStrategyImpl(proj);
 			loader.setResourceSet(getResourceSet());
 			return getCommonArchiveFactory().openWARFile(loader, proj.getName());
 		}
 		return null;
 
 	}
 
 	public WARFile asWARFile(boolean shouldExportSource) throws OpenFailureException {
 		if (getWebNatureType() == IWebNatureConstants.J2EE_WEB_PROJECT) {
 			IProject proj = getProject();
 			if (proj == null)
 				return null;
 			WTProjectLoadStrategyImpl loader = new WTProjectLoadStrategyImpl(proj);
 			loader.setExportSource(shouldExportSource);
 			loader.setResourceSet(getResourceSet());
 			ArchiveOptions options = new ArchiveOptions();
 			options.setLoadStrategy(loader);
 			if (isBinaryProject()) {
 				options.setIsReadOnly(true);
 			}
 			return getCommonArchiveFactory().openWARFile(options, proj.getName());
 		}
 		return null;
 
 	}
 
 	/**
 	 * Configures the project with this nature.
 	 * 
 	 * @see IProjectNature#configure()
 	 */
 	public void primConfigure() throws CoreException {
 		//TODO - Move builders to the operations
 		// add Validation Builder to Web Projects' builder list
 		ProjectUtilities.addToBuildSpec(J2EEPlugin.VALIDATION_BUILDER_ID, getProject());
 		// add LibCopy Builder to Web Projects' builder list
 		// Note: since this is the last nature added, we are assuming it will
 		// be after the Java builder. May need to be more explicit about this.
 		ProjectUtilities.addToBuildSpec(J2EEPlugin.LIBCOPY_BUILDER_ID, getProject());
 		addLibDirBuilder();
 		super.primConfigure();
 	}
 
 	protected EditModel createCacheEditModel() {
 		return getWebAppEditModelForRead(this);
 	}
 
 	/**
 	 * Create a default file for the user given the name (directory relative to the project) and the
 	 * default contents for the file.
 	 * 
 	 * @param newFilePath -
 	 *            IPath
 	 * @param newFileContents -
 	 *            String
 	 */
 	public void createFile(IPath newFilePath, String newFileContents) throws CoreException {
 		IPath projectPath = project.getFullPath();
 		IWorkspace workspace = J2EEPlugin.getWorkspace();
 		createFolder(newFilePath.removeLastSegments(1).toString());
 		IFile outputFile = workspace.getRoot().getFile(projectPath.append(newFilePath));
 		outputFile.refreshLocal(IResource.DEPTH_INFINITE, null);
 		InputStream inputStream = new ByteArrayInputStream(newFileContents.getBytes());
 		if (!(outputFile.exists()))
 			outputFile.create(inputStream, true, null);
 	}
 
 	/**
 	 * Create the folders for the project we have just created.
 	 * 
 	 * @exception com.ibm.itp.core.api.resources.CoreException
 	 *                The exception description.
 	 */
 	protected void createFolders() throws CoreException {
 		// Create the WEB_MODULE directory
 		createFolder(getBasicWebModulePath());
 		//build for metapath
 		if (getWebNatureType() == IWebNatureConstants.J2EE_WEB_PROJECT) {
 			createFolder(getMetaPath().toString());
 			super.createFolders();
 		}
 		// Create the WEB_INF/lib directory
 		if (getWebNatureType() == IWebNatureConstants.J2EE_WEB_PROJECT)
 			createFolder(getLibraryPath());
 	}
 
 	/**
 	 * @see org.eclipse.jst.j2ee.internal.internal.j2eeproject.J2EENature
 	 */
 	public Module createNewModule() {
 		return ((ApplicationPackage) EPackage.Registry.INSTANCE.getEPackage(ApplicationPackage.eNS_URI)).getApplicationFactory().createWebModule();
 	}
 
 	/**
 	 * Create a new nature runtime from the project info
 	 */
 	//	public static J2EEWebNatureRuntime createRuntime(WebProjectInfo info)
 	// throws CoreException {
 	//		IProject project = info.getProject();
 	//		if (!hasRuntime(project)) {
 	//			addNatureToProject(project, IWebNatureConstants.J2EE_NATURE_ID);
 	//			J2EEWebNatureRuntime runtime = getRuntime(project);
 	//			runtime.initializeFromInfo(info);
 	//			return runtime;
 	//		}
 	//		return getRuntime(project);
 	//	}
 	/**
 	 * Removes this nature from the project.
 	 * 
 	 * @see IProjectNature#deconfigure
 	 */
 	public void deconfigure() throws CoreException {
 		removeFromBuildSpec(J2EEPlugin.LIBDIRCHANGE_BUILDER_ID);
 		super.deconfigure();
 	}
 
 	/*
 	 * Returns the context root that the server is configured with (also called the web app path).
 	 * This is the path that the war is placed on within the deployed server. This path must be
 	 * included as the first segment of a doc relative path specification within an html file.
 	 */
 	public String getContextRoot() {
		if (contextRoot == null) {
 			WebSettings settings = getWebSettings();
 			contextRoot = settings.getContextRoot();
 			if (contextRoot == null)
 				contextRoot = getProject().getName();
 		}
 		return contextRoot;
 	}
 
 	public WebSettings getWebSettings() {
 		if (fWebSettings == null) {
 			fWebSettings = new WebSettings(getProject());
 		}
 		return fWebSettings;
 	}
 
 	public IContainer getCSSFolder() {
 		return getProject().getFolder(getBasicWebModulePath().append(IWebNatureConstants.CSS_DIRECTORY));
 	}
 
 	/*
 	 * See description in IJ2EEWebNature interface Creation date: (10/31/2001 10:21:37 AM) @return
 	 * org.eclipse.jdt.core.IJavaProject
 	 */
 	public IJavaProject getJ2EEJavaProject() {
 		return ProjectUtilities.getJavaProject(project);
 	}
 
 	protected IPath getLibraryPath() {
 		return getWEBINFPath().append(IWebNatureConstants.LIBRARY_DIRECTORY);
 	}
 
 	public IContainer getLibraryFolder() {
 		return getProject().getFolder(getLibraryPath());
 	}
 
 	protected String getMetaPathKey() {
 		return J2EEConstants.WEB_INF;
 	}
 
 	/*
 	 * Returns the root that the server runs off of. In the case of a web project, this is the "Web
 	 * content" folder. For projects created under V4, this is the webApplication folder.
 	 */
 	public IContainer getModuleServerRoot() {
 		return getProject().getFolder(getModuleServerRootName());
 	}
 
 	/*
 	 * Returns the name of the module server root directory. For projects created in v4, this is
 	 * webApplication. For projects created in v5.0, this is Web Content. For projects created in
 	 * v5.0.1 and later, this is configurable per project by the user.
 	 */
 	public String getModuleServerRootName() {
 		String name = getWebSettings().getWebContentName();
 		if (name == null) {
 			name = J2EEPlugin.getDefault().getJ2EEPreferences().getJ2EEWebContentFolderName();
 			if (name == null || name.length() == 0)
 				name = IWebNatureConstants.WEB_MODULE_DIRECTORY_;
 		}
 		return name;
 	}
 
 	public void setModuleServerRootName(String name) throws CoreException {
 		getWebSettings().setWebContentName(name);
 		getWebSettings().write();
 	}
 
 	/**
 	 * Return the root location for loading mof resources; defaults to the source folder, subclasses
 	 * may override
 	 */
 	public IContainer getEMFRoot() {
 		return getModuleServerRoot();
 	}
 
 	/**
 	 * Return the nature's ID.
 	 */
 	public String getNatureID() {
 		return IWebNatureConstants.J2EE_NATURE_ID;
 	}
 
 	/**
 	 * Return the ID of the plugin that this nature is contained within.
 	 */
 	protected String getPluginID() {
 		return IWebToolingCoreConstants.PLUG_IN_ID;
 	}
 
 	/**
 	 * return the inlinks for this project. This is done by asking the relationData to restore
 	 * itself.
 	 */
 	public RelationData getRelationData() {
 		if (relationData == null) {
 			relationData = new RelationData();
 			relationData.restore(getProject());
 		}
 		return relationData;
 	}
 
 	/**
 	 * Insert the method's description here. Creation date: (10/30/2001 11:12:41 PM)
 	 * 
 	 * @return org.eclipse.core.resources.IContainer
 	 */
 	public IContainer getRootPublishableFolder() {
 		return getModuleServerRoot();
 	}
 
 	/**
 	 * Get a WebNatureRuntime that corresponds to the supplied project.
 	 * 
 	 * @return com.ibm.itp.wt.IWebNature
 	 * @param project
 	 *            com.ibm.itp.core.api.resources.IProject
 	 */
 	public static J2EEWebNatureRuntime getRuntime(IProject project) {
 		try {
 			J2EEWebNatureRuntime a = (J2EEWebNatureRuntime) project.getNature(IWebNatureConstants.J2EE_NATURE_ID);
 			return a;
 		} catch (CoreException e) {
 			return null;
 		}
 	}
 
 	public ITaglibRegistry getTaglibRegistry() {
 		return WebPlugin.getDefault().getTaglibRegistryManager().getTaglibRegistry(getProject());
 	}
 
 	/**
 	 * @see org.eclipse.jst.j2ee.internal.internal.j2eeproject.J2EENature#getEditModelKey()
 	 */
 	public String getEditModelKey() {
 		return EDIT_MODEL_ID;
 	}
 
 	/**
 	 * Returns a web xml model that consists of the MOF model and the DOM model. Important!!!
 	 * Calling this method increments the use count of this model. When you are done accessing the
 	 * model, call releaseAccess()!
 	 */
 	public WebEditModel getWebAppEditModelForRead(Object accessorKey) {
 		return (WebEditModel) getEditModelForRead(EDIT_MODEL_ID, accessorKey);
 	}
 
 	/**
 	 * Returns a web xml model that consists of the MOF model and the DOM model. Important!!!
 	 * Calling this method increments the use count of this model. When you are done accessing the
 	 * model, call releaseAccess()!
 	 */
 	public WebEditModel getWebAppEditModelForWrite(Object accessorKey) {
 		return (WebEditModel) getEditModelForWrite(EDIT_MODEL_ID, accessorKey);
 	}
 
 	/**
 	 * @return org.eclipse.core.runtime.IPath
 	 */
 	public IPath getWEBINFPath() {
 		return getBasicWebModulePath().append(IWebNatureConstants.INFO_DIRECTORY);
 	}
 
 	/**
 	 * @return org.eclipse.core.runtime.IPath
 	 */
 	public IPath getDeploymentDescriptorPath() {
 		return getWEBINFPath().append(IWebNatureConstants.DEPLOYMENT_DESCRIPTOR_FILE_NAME);
 	}
 
 	/**
 	 * @return org.eclipse.core.runtime.IPath
 	 */
 	public IPath getWebBindingsPath() {
 		if (getWebNatureType() == IWebNatureConstants.J2EE_WEB_PROJECT)
 			return getProjectPath().append(getWEBINFPath()).append(IWebNatureConstants.BINDINGS_FILE_NAME);
 
 		return null;
 	}
 
 	/**
 	 * @return org.eclipse.core.runtime.IPath
 	 */
 	public IPath getWebExtensionsPath() {
 		if (getWebNatureType() == IWebNatureConstants.J2EE_WEB_PROJECT)
 			return getProjectPath().append(getWEBINFPath()).append(IWebNatureConstants.EXTENSIONS_FILE_NAME);
 
 		return null;
 	}
 
 	public IPath getBasicWebModulePath() {
 		WebSettings webSettings = getWebSettings();
 		String name = webSettings.getWebContentName();
 		if (name == null) {
 			int version = getVersion();
 			// If created in V5 or beyond
 			if (version != -1 && version >= 500)
 				return IWebNatureConstants.WEB_MODULE_PATH_;
 
 			return IWebNatureConstants.WEB_MODULE_PATH_V4;
 		}
 		return new Path(name);
 	}
 
 	public IPath getWebModulePath() {
 		return getProjectPath().append(getBasicWebModulePath());
 	}
 
 	/**
 	 * Insert the method's description here. Creation date: (10/30/2001 5:25:06 PM)
 	 * 
 	 * @return boolean
 	 */
 	public int getWebNatureType() {
 		if (fWebNatureType == -1) {
 			WebSettings settings = getWebSettings();
 			String strType = settings.getProjectType();
 			if (strType != null) {
 				if (strType.equalsIgnoreCase(PROJECTTYPE_J2EE_VALUE))
 					fWebNatureType = IWebNatureConstants.J2EE_WEB_PROJECT;
 			}
 			if (fWebNatureType == -1) {
 				// Invalid value, don't make any unnecessary noice and
 				// just fix it quietly. find out if web-inf directory
 				// exists and take an educated guess
 				IContainer webmoduleFolder = getRootPublishableFolder();
 				IFolder webinfFolder = ((IFolder) webmoduleFolder).getFolder(IWebNatureConstants.INFO_DIRECTORY);
 				if (webinfFolder.exists())
 					fWebNatureType = IWebNatureConstants.J2EE_WEB_PROJECT;
 			}
 		}
 		return fWebNatureType;
 	}
 
 	/*
 	 * Return the current version number.
 	 */
 	public static int getCurrentVersion() {
 		return CURRENT_VERSION;
 	}
 
 	/*
 	 * Return the version number stored in the web settings file. The version number is used to
 	 * determine when the web project was created (i.e., under what product version). The current
 	 * version number does not necessarily change with each product version -- it's only changed
 	 * when it becomes necessary to distinguish a new version from a prior version.
 	 */
 	public int getVersion() {
 		if (fVersion == -1) {
 			try {
 				String versionString = getWebSettings().getVersion();
 				if (versionString != null)
 					fVersion = Integer.parseInt(versionString);
 			} catch (NumberFormatException e) {
 				//Do nothing
 			}
 		}
 		return fVersion;
 	}
 
 	/*
 	 * Set the version number stored in the web settings file. The version number is used to
 	 * determine when the web project was created (i.e., under what product version). The current
 	 * version number does not necessarily change with each product version -- it's only changed
 	 * when it becomes necessary to distinguish a new version from a prior version.
 	 */
 	public void setVersion(String newVersion) throws CoreException {
 		getWebSettings().setVersion(newVersion);
 		getWebSettings().write();
 		fVersion = -1;
 	}
 
 	public IPath getWebSettingsPath() {
 		return getProjectPath().append(IWebNatureConstants.WEBSETTINGS_FILE_NAME);
 	}
 
 	/**
 	 * Return the MOF Resource (model) representing the Web.xml file.
 	 */
 	protected XMLResource getWebXmiResource() {
 		Resource res = getResource(URI.createURI(J2EEConstants.WEBAPP_DD_URI));
 		return (XMLResource) res;
 	}
 
 	/**
 	 * Insert the method's description here. Creation date: (5/10/2001 3:41:00 PM)
 	 * 
 	 * @return org.eclipse.core.runtime.IPath
 	 */
 	public IPath getWebXMLPath() {
 		if (getWebNatureType() == IWebNatureConstants.J2EE_WEB_PROJECT)
 			return getProjectPath().append(getWEBINFPath()).append(IWebNatureConstants.DEPLOYMENT_DESCRIPTOR_FILE_NAME);
 
 		return null;
 	}
 
 	/**
 	 * Return whether or not the project has a runtime created on it.
 	 * 
 	 * @return boolean
 	 * @param project
 	 *            com.ibm.itp.core.api.resources.IProject
 	 */
 	public static boolean hasRuntime(IProject project) {
 		try {
 			return project.hasNature(IWebNatureConstants.J2EE_NATURE_ID);
 		} catch (CoreException e) {
 			return false;
 		}
 	}
 
 	/**
 	 * Insert the method's description here. Creation date: (10/31/2001 5:32:12 PM)
 	 * 
 	 * @param info
 	 *            com.ibm.iwt.webproject.WebProjectInfo
 	 * @exception org.eclipse.core.runtime.CoreException
 	 *                The exception description.
 	 */
 	public void initializeFromInfo(WebProjectInfo info) throws org.eclipse.core.runtime.CoreException {
 		int natureType = info.getWebProjectType();
 		fWebNatureType = natureType;
 		WebSettings webSettings = getWebSettings();
 		webSettings.setProjectType(convertNatureTypeToString(natureType));
 		webSettings.setWebContentName(info.getWebContentName());
 		webSettings.write();
 		super.initializeFromInfo(info);
 	}
 
 	/**
 	 * Insert the method's description here. Creation date: (11/1/2001 2:25:22 PM)
 	 * 
 	 * @param builderID
 	 *            java.lang.String
 	 * @exception org.eclipse.core.runtime.CoreException
 	 *                The exception description.
 	 */
 	protected void removeFromBuildSpec(String builderID) throws org.eclipse.core.runtime.CoreException {
 		IProjectDescription description = getProject().getDescription();
 		ICommand[] commands = description.getBuildSpec();
 		boolean found = false;
 		for (int i = 0; i < commands.length; ++i) {
 			if (commands[i].getBuilderName().equals(builderID)) {
 				found = true;
 				break;
 			}
 		}
 		if (!found) {
 			ICommand command = description.newCommand();
 			command.setBuilderName(builderID);
 			ICommand[] newCommands = new ICommand[commands.length + 1];
 			System.arraycopy(commands, 0, newCommands, 1, commands.length);
 			newCommands[0] = command;
 			IProjectDescription desc = getProject().getDescription();
 			desc.setBuildSpec(newCommands);
 			getProject().setDescription(desc, null);
 		}
 	}
 
 	public void removeFeatureId(String featureId) throws CoreException {
 		WebSettings webSettings = getWebSettings();
 		webSettings.removeFeatureId(featureId);
 		webSettings.write();
 	}
 
 	//    /*
 	//	 * JEC - made public so the cache can be cleared from the webview's
 	//	 * resource changed listener. Not ideal.
 	//	 */
 	//    public void resetWebSettings() {
 	//        fWebSettings = null;
 	//        contextRoot = null;
 	//        jspLevel = null;
 	//        libModules = null;
 	//        featureIds = null;
 	//        fVersion = -1;
 	//        fWebNatureType = -1;
 	//    }
 	public void setContextRoot(String newContextRoot) throws CoreException {
 		getWebSettings().setContextRoot(newContextRoot);
 		getWebSettings().write();
 		contextRoot = newContextRoot;
 	}
 
 	public void primaryContributeToContext(EMFWorkbenchContextBase aNewEMFContext) {
 		if (emfContext == aNewEMFContext)
 			return;
 		ModuleCoreNature moduleCoreNature = ModuleCoreNature.getModuleCoreNature(getProject());
 		if(moduleCoreNature == null)
 		    setupNonFlexibleProject(aNewEMFContext);
 		else
 		    setupFlexibleProject(aNewEMFContext);
 	}
 
 	/**
      * @param aNewEMFContext
      */
     private void setupFlexibleProject(EMFWorkbenchContextBase aNewEMFContext) {
 		emfContext = aNewEMFContext;
 		getEmfContext().setDefaultToMOF5Compatibility(true);
 		//Overriding superclass to use our own URI converter, which knows about binary projects
 		ProjectResourceSet projectResourceSet = aNewEMFContext.getResourceSet();
 		projectResourceSet.setURIConverter(createURIConverter(getProject(), projectResourceSet));
 		/* Flexible projects have their own ResourceFactories and their URI Converters */
 //		set.setResourceFactoryRegistry(new J2EEResourceFactoryRegistry());
 //		WorkbenchURIConverter conv = initializeWorbenchURIConverter(set);
 //		set.setURIConverter(conv);
 		initializeCacheEditModel();
 		addAdapterFactories(projectResourceSet);
 		projectResourceSet.getSynchronizer().addExtender(this); //added so we can be informed of closes to the
 		// project.
 		//new J2EEResourceDependencyRegister(set); //This must be done after the URIConverter is
 		// created.
     }
     
 	/**
 	 * @param project
 	 * @return
 	 */
 	private URIConverter createURIConverter(IProject aProject, ProjectResourceSet aResourceSet ) {
 		return new ModuleCoreURIConverter(aProject, aResourceSet.getSynchronizer()); 
 	}
 
 	/**
      * @param aNewEMFContext
      */
     private void setupNonFlexibleProject(EMFWorkbenchContextBase aNewEMFContext) {
         super.primaryContributeToContext(aNewEMFContext);
 		WorkbenchURIConverter converter = (WorkbenchURIConverter) aNewEMFContext.getResourceSet().getURIConverter();
 		converter.addInputContainer(getProject());
     }
 
     protected String convertNatureTypeToString(int type) {
 		return PROJECTTYPE_J2EE_VALUE;
 	}
 
 	/**
 	 * Set the web nature's type to either Static (IWebNatureConstants.STATIC_WEB_NATURE) or J2EE
 	 * (IWebNatureConstants.J2EE_WEB_NATURE)
 	 * 
 	 * @param newIsStaticWebProject
 	 *            boolean
 	 */
 	public void setWebNatureType(int natureType) throws CoreException {
 		getWebSettings().setProjectType(convertNatureTypeToString(natureType));
 		getWebSettings().write();
 		fWebNatureType = natureType;
 	}
 
 	/*
 	 * @deprecated - Use getModuleVersion() with J2EEVersionConstants
 	 * @see IJ2EEWebNature#isJSP1_2()
 	 */
 	public boolean isJSP1_2() {
 		return getJSPLevel().equals(JSPLEVEL_1_2);
 	}
 
 	/*
 	 * @deprecated - Use getModuleVersion() with J2EEVersionConstants
 	 * @see IJ2EEWebNature#isServlet2_3()
 	 */
 	public boolean isServlet2_3() {
 		return SERVLETLEVEL_2_3.equals(getServletLevel());
 	}
 
 	/*
 	 * @deprecated - Use getModuleVersion() with J2EEVersionConstants
 	 * @see IJ2EEWebNature#setIsJSP1_2(boolean)
 	 */
 	/*
 	 * public void setIsJSP1_2(boolean isJSP1_2) throws CoreException { if (isJSP1_2)
 	 * setJSPLevel(JSPLEVEL_1_2); else setJSPLevel(JSPLEVEL_1_1); }
 	 */
 	/*
 	 * @deprecated - Use getModuleVersion() with J2EEVersionConstants
 	 * @see IJ2EEWebNature#setIsServlet2_3(boolean)
 	 */
 	/*
 	 * public void setIsServlet2_3(boolean isServlet2_3) throws CoreException { if (isServlet2_3)
 	 * setServletLevel(SERVLETLEVEL_2_3); else setServletLevel(SERVLETLEVEL_2_2); }
 	 */
 	/*
 	 * @see IJ2EEWebNature#getJSPLevel()
 	 */
 	public String getJSPLevel() {
 		if (jspLevel == null) {
 			switch (getModuleVersion()) {
 				case J2EEVersionConstants.WEB_2_2_ID :
 					jspLevel = JSPLEVEL_1_1;
 					break;
 				case J2EEVersionConstants.WEB_2_3_ID :
 					jspLevel = JSPLEVEL_1_2;
 					break;
 				case J2EEVersionConstants.WEB_2_4_ID :
 					jspLevel = JSPLEVEL_2_0;
 					break;
 				default :
 					jspLevel = DEFAULT_JSPLEVEL;
 			}
 		}
 		return jspLevel;
 	}
 
 	/**
 	 * Return the root object, the web-app, from the web.xml DD.
 	 * 
 	 * used for Read-Only Purpose
 	 */
 	public WebApp getWebApp() {
 		return ((WebEditModel) getCacheEditModel()).getWebApp();
 	}
 
 	/*
 	 * @see IJ2EEWebNature#getServletLevel()
 	 */
 	public String getServletLevel() {
 		WebEditModel editModel = (WebEditModel) getCacheEditModel();
 		String retVal = SERVLETLEVEL_2_2;
 		if (editModel != null) {
 			WebAppResource resource = editModel.getWebXmiResource();
 			WebApp app = resource.getWebApp();
 			switch (app.getVersionID()) {
 				case J2EEVersionConstants.WEB_2_4_ID :
 					retVal = SERVLETLEVEL_2_4;
 					break;
 				case J2EEVersionConstants.WEB_2_3_ID :
 					retVal = SERVLETLEVEL_2_3;
 					break;
 				case J2EEVersionConstants.WEB_2_2_ID :
 				default :
 					retVal = SERVLETLEVEL_2_2;
 					break;
 			}
 		}
 		return retVal;
 	}
 
 	/*
 	 * @see IJ2EEWebNature#setJSPLevel(String)
 	 */
 	public void setJSPLevel(String level) throws CoreException {
 		if (jspLevel != null && jspLevel.equals(level))
 			return;
 		String tJspLevel = null;
 		WebSettings webSettings = getWebSettings();
 		if (JSPLEVEL_1_1.equals(level)) {
 			webSettings.setJSPLevel(JSPLEVEL_1_1);
 			tJspLevel = JSPLEVEL_1_1;
 		} else if (JSPLEVEL_1_2.equals(level)) {
 			webSettings.setJSPLevel(JSPLEVEL_1_2);
 			tJspLevel = JSPLEVEL_1_2;
 		} else if (JSPLEVEL_2_0.equals(level)) {
 			webSettings.setJSPLevel(JSPLEVEL_2_0);
 			tJspLevel = JSPLEVEL_2_0;
 		}
 		webSettings.write();
 		jspLevel = tJspLevel;
 	}
 
 	//TODO depricate this method and create a new one that uses ints.
 	/*
 	 * @see IJ2EEWebNature#setServletLevel(String)
 	 */
 	public void setServletLevel(String servletLevel) {
 		WebEditModel editModel = null;
 		try {
 			editModel = getWebAppEditModelForWrite(this);
 			if (editModel != null) {
 				XMLResource resource = editModel.getWebXmiResource();
 				if (SERVLETLEVEL_2_3.equals(servletLevel)) {
 					servletLevel = SERVLETLEVEL_2_3;
 					resource.setDoctypeValues(J2EEConstants.WEBAPP_PUBLICID_2_3, J2EEConstants.WEBAPP_SYSTEMID_2_3);
 				} else if (SERVLETLEVEL_2_2.equals(servletLevel)) {
 					servletLevel = SERVLETLEVEL_2_2;
 					resource.setDoctypeValues(J2EEConstants.WEBAPP_SYSTEMID_2_2, J2EEConstants.WEBAPP_PUBLICID_2_2);
 				} else if (SERVLETLEVEL_2_4.equals(servletLevel)) {
 					servletLevel = SERVLETLEVEL_2_4;
 					resource.setDoctypeValues(null, null);
 				} else
 					throw new RuntimeException(ProjectSupportResourceHandler.getString("Invalid_Servlet_Level_set_on_WebNature_3_EXC_")); //$NON-NLS-1$
 				editModel.saveIfNecessary(this);
 			}
 		} finally {
 			if (editModel != null) {
 				editModel.releaseAccess(this);
 			}
 		}
 	}
 
 	/*
 	 * @see IJ2EEWebNature#getLibModules()
 	 */
 	public ILibModule[] getLibModules() {
 		if (libModules == null) {
 			WebSettings settings = getWebSettings();
 			libModules = settings.getLibModules();
 			if (libModules == null)
 				libModules = new ILibModule[0];
 		}
 		return libModules;
 	}
 
 	/*
 	 * @see IJ2EEWebNature#setLibModules(ILibModule[])
 	 */
 	public void setLibModules(ILibModule[] libModules) throws CoreException {
 		WebSettings webSettings = getWebSettings();
 		webSettings.setLibModules(libModules);
 		webSettings.write();
 		this.libModules = libModules;
 	}
 
 	public String[] getFeatureIds() {
 		WebSettings settings = getWebSettings();
 		featureIds = settings.getFeatureIds();
 		if (featureIds == null)
 			featureIds = new String[0];
 		return featureIds;
 	}
 
 	public void setFeatureIds(String[] featureIds) throws CoreException {
 		WebSettings webSettings = getWebSettings();
 		webSettings.setFeatureIds(featureIds);
 		webSettings.write();
 	}
 
 	/**
 	 * @deprecated - Use getJ2EEVersion() with J2EEVersionConstants
 	 * @see IJ2EENature#isJ2EE1_3()
 	 */
 	public boolean isJ2EE1_3() {
 		// Removed for Defect 218792 - Performance
 		//		return isServlet2_3() || isJSP1_2();
 		return getJ2EEVersion() == J2EEVersionConstants.J2EE_1_3_ID;
 	}
 
 	/**
 	 * @see IBaseWebNature#isJ2EE()
 	 */
 	public boolean isJ2EE() {
 		return true;
 	}
 
 	/**
 	 * @see IBaseWebNature#isStatic()
 	 */
 	public boolean isStatic() {
 		return false;
 	}
 
 	public String getOverlayIconName() {
 		switch (getJ2EEVersion()) {
 			case J2EEVersionConstants.J2EE_1_2_ID :
 				return WEB_PROJECT_12_OVERLAY;
 			case J2EEVersionConstants.J2EE_1_3_ID :
 				return WEB_PROJECT_13_OVERLAY;
 			case J2EEVersionConstants.J2EE_1_4_ID :
 			default :
 				return WEB_PROJECT_14_OVERLAY;
 		}
 	}
 
 	public int getDeploymentDescriptorType() {
 		return XMLResource.WEB_APP_TYPE;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.j2ee.internal.internal.j2eeproject.J2EENature#getDeploymentDescriptorRoot()
 	 */
 	public EObject getDeploymentDescriptorRoot() {
 		return getWebApp();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.j2ee.internal.internal.j2eeproject.J2EENature#getVersionFromModuleFile()
 	 */
 	protected int getVersionFromModuleFile() {
 		WebApp ddRoot = getWebApp();
 		if (ddRoot != null) {
 			return ddRoot.getVersionID();
 		}
 		return J2EEVersionConstants.WEB_2_4_ID;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.j2ee.internal.internal.j2eeproject.J2EENature#getJ2EEEditModelForRead(java.lang.Object)
 	 */
 	public J2EEEditModel getJ2EEEditModelForRead(Object accessorKey) {
 		return getWebAppEditModelForRead(accessorKey);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.j2ee.internal.internal.j2eeproject.J2EENature#getJ2EEEditModelForWrite(java.lang.Object)
 	 */
 	public J2EEEditModel getJ2EEEditModelForWrite(Object accessorKey) {
 		return getWebAppEditModelForWrite(accessorKey);
 	}
 
 	/**
 	 * Return an editing model used to read web service resources. Important!!! Calling this method
 	 * increments the use count of this model. When you are done accessing the model, call
 	 * releaseAccess()!
 	 */
 	public WebServiceEditModel getWebServiceEditModelForRead(Object accessorKey, Map params) {
 		return (WebServiceEditModel) getEditModelForRead(WEB_SERVICE_EDIT_MODEL_ID, accessorKey, params);
 	}
 
 	/**
 	 * Return an editing model used to edit web service resources. Important!!! Calling this method
 	 * increments the use count of this model. When you are done accessing the model, call
 	 * releaseAccess()!
 	 */
 	public WebServiceEditModel getWebServiceEditModelForWrite(Object accessorKey, Map params) {
 		return (WebServiceEditModel) getEditModelForWrite(WEB_SERVICE_EDIT_MODEL_ID, accessorKey, params);
 	}
 }
