 /*******************************************************************************
  * Copyright (c) 2005, 2012 eBay Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  *******************************************************************************/
 package org.eclipse.vjet.eclipse.core.ts;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipFile;
 
 import org.eclipse.core.internal.resources.ResourceException;
 import org.eclipse.core.internal.resources.Workspace;
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IProjectNature;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceChangeEvent;
 import org.eclipse.core.resources.IResourceDelta;
 import org.eclipse.core.resources.IWorkspace;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.dltk.mod.core.DLTKContentTypeManager;
 import org.eclipse.dltk.mod.core.DLTKCore;
 import org.eclipse.dltk.mod.core.IBuildpathEntry;
 import org.eclipse.dltk.mod.core.IDLTKLanguageToolkit;
 import org.eclipse.dltk.mod.core.IScriptProject;
 import org.eclipse.dltk.mod.core.ModelException;
 import org.eclipse.dltk.mod.internal.core.Model;
 import org.eclipse.dltk.mod.internal.core.ModelManager;
 import org.eclipse.dltk.mod.internal.core.ScriptProject;
 import org.eclipse.vjet.dsf.ts.TypeSpace;
 import org.eclipse.vjet.dsf.ts.event.group.RemoveGroupEvent;
 import org.eclipse.vjet.eclipse.codeassist.CodeassistUtils;
 import org.eclipse.vjet.eclipse.core.PiggyBackClassPathUtil;
 import org.eclipse.vjet.eclipse.core.VjoLanguageToolkit;
 import org.eclipse.vjet.eclipse.core.VjoNature;
 import org.eclipse.vjet.eclipse.core.builder.TypeSpaceBuilder;
 import org.eclipse.vjet.vjo.tool.typespace.GroupInfo;
 import org.eclipse.vjet.vjo.tool.typespace.ITypeSpaceLoader;
 import org.eclipse.vjet.vjo.tool.typespace.SourcePathInfo;
 import org.eclipse.vjet.vjo.tool.typespace.SourceTypeName;
 import org.eclipse.vjet.vjo.tool.typespace.TypeSpaceMgr;
 
 
 /**
  * This class load vjo resources to {@link TypeSpace} object. Read all project
  * resource from all vjo projects. Process vjo resources changes. Calls
  * corresponding type space jobs. When changes ".buildpath" file then calls
  * {@link TypeSpaceReloadJob#schedule()} method.
  * 
  * 
  * 
  * @see TypeSpaceLoadJob
  * @see TypeSpaceRefreshJob
  * @see TypeSpaceGroupLoadJob
  * 
  * 
  * 
  */
 public class EclipseTypeSpaceLoader implements ITypeSpaceLoader {
 
 	private List<SourceTypeName> m_types = new ArrayList<SourceTypeName>();
 
 	private IDLTKLanguageToolkit m_toolkit = VjoLanguageToolkit.getDefault();
 
 	private ModelManager m_manager = ModelManager.getModelManager();
 
 	private TypeSpaceRefreshJob m_refreshJob = new TypeSpaceRefreshJob();
 
 	private TypeSpaceReloadJob m_reloadJob = new TypeSpaceReloadJob();
 
 	private List<SourceTypeName> m_changedTypes = new ArrayList<SourceTypeName>();
 
 	private TypeSpaceMgr m_tsmgr = TypeSpaceMgr.getInstance();
 
 	private boolean m_started = false;
 
 	private Map<String, List<String>> m_groupDependency;
 
 	/**
 	 * Create instance of this class. Add listeners to the workspace for control
 	 * resource changes.
 	 */
 	public EclipseTypeSpaceLoader() {
 		super();
 //		final IWorkspace workspace = ResourcesPlugin.getWorkspace();
 //		workspace.removeResourceChangeListener(this);
 //		workspace.addResourceChangeListener(this,
 //				IResourceChangeEvent.POST_CHANGE
 //						| IResourceChangeEvent.PRE_DELETE
 //						| IResourceChangeEvent.PRE_CLOSE);
 	}
 
 	public List<SourceTypeName> getTypes() {
 		m_types.clear();
 		createWorkspaceTypes();
 		createWorkspaceLibrariesTypes();
 		return m_types;
 	}
 
 	/**
 	 * Create list of the types from the workspace libraries.
 	 */
 	private void createWorkspaceLibrariesTypes() {
 		try {
 
 			Model model = m_manager.getModel();
 			IScriptProject[] projects;
 			projects = model.getScriptProjects(VjoNature.NATURE_ID);
 
 			for (IScriptProject project : projects) {
 				loadLibrariesTypes(project);
 			}
 
 		} catch (Exception e) {
 			DLTKCore.error(e.toString(), e);
 		}
 	}
 
 	/**
 	 * Create list of the types from the project libraries.
 	 * 
 	 * @param project
 	 *            {@link IScriptProject} object
 	 * @throws IOException
 	 * @throws CoreException
 	 */
 	private void loadLibrariesTypes(IScriptProject project) throws IOException,
 			CoreException {
 		IBuildpathEntry[] entries = project.getResolvedBuildpath(true);
 		for (IBuildpathEntry entry : entries) {
 			if (entry.getEntryKind() == IBuildpathEntry.BPE_LIBRARY) {
 
 				IPath path = entry.getPath();
 				String strPath = path.toString().toLowerCase();
 				if (CodeassistUtils.isBinaryPath(strPath)) {
 
 					ZipFile jarFile = m_manager.getZipFile(path);
 					Enumeration<? extends ZipEntry> enumeration = jarFile
 							.entries();
 
 					while (enumeration.hasMoreElements()) {
 
 						ZipEntry elem = enumeration.nextElement();
 
 						if (!elem.isDirectory()) {
 							createType(path, jarFile, elem);
 						}
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * Create type from {@link IPath} object, {@link ZipFile} object and
 	 * {@link ZipEntry} object.
 	 * 
 	 * @param path
 	 *            {@link IPath} object
 	 * @param jarFile
 	 *            {@link ZipFile} object
 	 * @param elem
 	 *            {@link ZipEntry} object.
 	 * @throws IOException
 	 */
 	private void createType(IPath path, ZipFile jarFile, ZipEntry elem)
 			throws IOException {
 
 		IPath path2 = new Path(path.segment(0));
 		path2 = path2.addTrailingSeparator();
 		path2 = path2.append(elem.getName());
 
 		Workspace ws = (Workspace) ResourcesPlugin.getWorkspace();
 		IFile file = (IFile) ws.newResource(path2, IResource.FILE);
 
 		if (isValidFile(file)) {
 			byte[] bs = getContent(jarFile, elem);
 			createSourceTypeName(file, bs);
 		}
 	}
 
 	/**
 	 * Returns bytes content of the {@link ZipEntry} object.
 	 * 
 	 * @param jarFile
 	 *            {@link ZipFile} object.
 	 * @param elem
 	 *            {@link ZipEntry} object.
 	 * @return bytes content of the {@link ZipEntry} object.
 	 * @throws IOException
 	 */
 	private byte[] getContent(ZipFile jarFile, ZipEntry elem)
 			throws IOException {
 		InputStream stream = jarFile.getInputStream(elem);
 		byte[] bs = new byte[stream.available()];
 		stream.read(bs);
 		stream.close();
 		return bs;
 	}
 
 	/**
 	 * Creates list of the {@link SourceTypeName} objects from the all workspace
 	 * vjo types and put to inner m_types object.
 	 */
 	private void createWorkspaceTypes() {
 
 		IWorkspace workspace = ResourcesPlugin.getWorkspace();
 		IProject[] projects = workspace.getRoot().getProjects();
 
 		for (int i = 0; i < projects.length; i++) {
 
 			IProject project = projects[i];
 			createProjectTypes(project);
 		}
 	}
 
 	/**
 	 * Create list of the {@link SourceTypeName} objects from specified project
 	 * and put to inner m_types object.
 	 * 
 	 * @param project
 	 *            {@link IProject} object.
 	 */
 	private void createProjectTypes(IProject project) {
 		if (project.isAccessible()) {
 			try {
 				IProjectNature nature = project.getNature(VjoNature.NATURE_ID);
 				if (nature != null) {
 					IResource[] resources = project.members();
 					createResourcesTypes(resources);
 				}
 			} catch (CoreException e) {
 				DLTKCore.error(e.toString(), e);
 			}
 		}
 	}
 
 	/**
 	 * Create list of the {@link SourceTypeName} objects from specified array of
 	 * the {@link IResource} object and put to inner m_types object.
 	 * 
 	 * @param resources
 	 *            array of the {@link IResource} objects.
 	 * 
 	 */
 	private void createResourcesTypes(IResource[] resources) {
 		for (IResource resource : resources) {
 
 			// process children
 			if (resource instanceof IContainer) {
 				IContainer container = (IContainer) resource;
 				loadResources(container);
 			}
 
 			if (resource instanceof IFile) {
 				createType(resource);
 			}
 		}
 	}
 
 	private void createType(IResource resource) {
 		IFile file = (IFile) resource;
 		if (isValidFile(file)) {
 			SourceTypeName name = createSourceTypeName(file);
 			m_types.add(name);
 		}
 	}
 
 	private SourceTypeName createSourceTypeName(IFile file) {
 		byte[] content = SourceTypeName.EMPTY_CONTENT;
 
 		content = getFileContent(file);
 
 		return createSourceTypeName(file, content);
 	}
 
 	/**
 	 * Refresh file from the disk to workspace.
 	 * 
 	 * @param file
 	 *            {@link IFile} object.
 	 */
 	private void refresh(IFile file) {
 		try {
 			file.refreshLocal(IResource.DEPTH_INFINITE, null);
 		} catch (CoreException e) {
 			DLTKCore.error(e.toString(), e);
 		}
 	}
 
 	/**
 	 * Creation {@link SourceTypeName} object from {@link IFile} object and
 	 * bytes content.
 	 * 
 	 * @param file
 	 *            {@link IFile} object
 	 * @param bs
 	 *            bytes content of the file object.
 	 * @return {@link SourceTypeName} object.
 	 */
 	private SourceTypeName createSourceTypeName(IFile file, byte[] bs) {
 		String group = file.getProject().getName();
 
 		String source = new String(bs);
 		String name = CodeassistUtils.getClassName(file);
 		// SourceTypeName typeName = new SourceTypeName(group,
 		// file.getRawLocation().toPortableString(), source);
 		SourceTypeName typeName = new SourceTypeName(group, name, source);
 		return typeName;
 	}
 
 	private byte[] getFileContent(IFile file) {
 		byte[] bs = SourceTypeName.EMPTY_CONTENT;
 		try {
 			bs = doGetFileContent(file);
 		} catch (IOException e) {
 			logError(e);
 		} catch (ResourceException e) {
 			refresh(file);
 			try {
 				bs = doGetFileContent(file);
 			} catch (Exception e1) {
 				logError(e1);
 			}
 		} catch (CoreException e) {
 			logError(e);
 		}
 		return bs;
 	}
 
 	private void logError(Exception e1) {
 		DLTKCore.error(e1.getMessage(), e1);
 	}
 
 	/**
 	 * Read content of the {@link IFile} object to the bytes array.
 	 * 
 	 * @param file
 	 *            {@link IFile}
 	 * @return file content in bytes array.
 	 * @throws CoreException
 	 * @throws IOException
 	 */
 	private byte[] doGetFileContent(IFile file) throws CoreException,
 			IOException {
 		InputStream stream = file.getContents();
 		int available = stream.available();
 		byte[] bs = new byte[available];
 		stream.read(bs);
 		stream.close();
 		return bs;
 	}
 
 	/**
 	 * Returns true if {@link IFile} object has valid file name and is in source
 	 * folder.
 	 * 
 	 * @param file
 	 *            {@link IFile} object.
 	 * @return true if {@link IFile} object has valid file name and is in source
 	 *         folder.
 	 */
 	private boolean isValidFile(IFile file) {
 		return isValidName(file)
 				&& PiggyBackClassPathUtil.isInSourceFolder(file);
 	}
 
 	/**
 	 * Returns true if location of the file has valid name and content type.
 	 * 
 	 * @param file
 	 *            {@link IFile} object.
 	 * @return true if location of the file has valid name and content type.
 	 */
 	private boolean isValidName(IFile file) {
 		boolean isValid = false;
 		try {
 			isValid = DLTKContentTypeManager.isValidFileNameForContentType(
 					m_toolkit, file.getLocation());
 		} catch (Exception e) {
 			DLTKCore.error(e.getMessage(), e);
 		}
 		return isValid;
 	}
 
 	private void loadResources(IContainer container) {
 		try {
 			createResourcesTypes(container.members());
 		} catch (CoreException e) {
 			DLTKCore.error(e.toString(), e);
 		}
 	}
 
 	public List<SourceTypeName> getTypes(String group) {
 		m_types.clear();
 		IWorkspace workspace = ResourcesPlugin.getWorkspace();
 		IProject project = workspace.getRoot().getProject(group);
 		if (project != null) {
 			createProjectTypes(project);
 		}
 		return m_types;
 	}
 
 	/**
 	 * Return map of the group depends.
 	 */
 	public Map<String, List<String>> getGroupDepends() {
 
 		if (m_groupDependency != null) {
 			return m_groupDependency;
 		}
 		try {
 
 			/*
 			 * sleep 3 seconds before getting the model to make sure we use it
 			 * after it's initialized, otherwise
 			 * TypeSpaceMgrTest#testGroupDepends() would fail
 			 */
 			Thread.sleep(3000);
 			updateGroupDepends();
 
 		} catch (InterruptedException e) {
 		}
 		return m_groupDependency;
 	}
 
 	private void updateGroupDepends(IProject project) {
 		m_groupDependency = new HashMap<String, List<String>>();
 		// Model model = m_manager.getModel();
 		// IScriptProject[] projects;
 		//
 		// try {
 		// projects = model.getScriptProjects(VjoNature.NATURE_ID);
 		// for (IScriptProject project : projects) {
 
 		try {
 			createDepends(m_groupDependency, DLTKCore.create(project));
 		} catch (ModelException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		// }
 		// } catch (ModelException e) {
 		// DLTKCore.error(e.toString(), e);
 		// }
 
 	}
 
 	private void updateGroupDepends() {
 		m_groupDependency = new HashMap<String, List<String>>();
 		Model model = m_manager.getModel();
 		IScriptProject[] projects;
 
 		try {
 			projects = model.getScriptProjects(VjoNature.NATURE_ID);
 			for (IScriptProject project : projects) {
 				createDepends(m_groupDependency, project);
 			}
 		} catch (ModelException e) {
 			DLTKCore.error(e.toString(), e);
 		}
 
 	}
 
 	/**
 	 * @param groups
 	 * @param project
 	 * @return
 	 * @throws ModelException
 	 * @see org.eclipse.vjet.eclipse.internal.launching.SdkBuildpathContainer#computeBuildpathEntries(String
 	 *      sdkName)
 	 */
 	private IBuildpathEntry[] createDepends(Map<String, List<String>> groups,
 			IScriptProject project) throws ModelException {
 
 		List<String> list = getDependsProjects(groups, project.getElementName());
 
 		IBuildpathEntry[] entries = TypeSpaceBuilder.getSerFileGroupDepends(
 				project, list);
 
 		groups.put(project.getElementName(), list);
 
 		return entries;
 	}
 
 	/**
 	 * Returns list of the depends groups for specified group.
 	 * 
 	 * @param groups
 	 *            map of the depends between groups.
 	 * @param group
 	 *            specified group name
 	 * @return list of the depends groups for specified group.
 	 */
 	private List<String> getDependsProjects(Map<String, List<String>> groups,
 			String group) {
 		List<String> list = groups.get(group);
 		if (list == null) {
 			list = new ArrayList<String>();
 			groups.put(group, list);
 		}
 		return list;
 	}
 
 	public List<SourceTypeName> getChangedTypes() {
 		return m_changedTypes;
 	}
 
 //	/**
 //	 * Process workspace resources changes.
 //	 * 
 //	 */
 //	public void resourceChanged(IResourceChangeEvent event) {
 //
 //		int type = event.getType();
 //
 //		IProject project = getProject(event);
 //		if (!isStarted() || !m_tsmgr.isAllowChanges() || project==null) {
 //			return;
 //		}
 //
 //		// process close project event.
 //		if (type == IResourceChangeEvent.PRE_CLOSE
 //				|| type == IResourceChangeEvent.PRE_DELETE) {
 //
 //			updateGroupDepends(project);
 //			processCloseProject(event);
 //			// TODO after close build dependent projects
 //		}
 //
 //		// process add/modify/delete resources events.
 //		if (type == IResourceChangeEvent.POST_CHANGE) {
 //
 //			if (isBildPathChangedEvent(event.getDelta())) {
 //				updateGroupDepends(project);
 //
 //
 //				
 //				// m_reloadJob.schedule();
 //			} else {
 //				processChanges(event);
 //			}
 //		}
 //
 //	}
 
 	private IProject getProject(IResourceChangeEvent event) {
 		if (event.getDelta() != null
 				&& event.getDelta().getAffectedChildren().length > 0
 				&& event.getDelta().getAffectedChildren()[0].getResource() != null) {
 			return event.getDelta().getAffectedChildren()[0].getResource().getProject();
 		}
 		if(event.getResource()!=null && event.getResource() instanceof IProject){
 			return event.getResource().getProject();
 		}
 
 		return null;
 	}
 
 	/**
 	 * Process changes resources in workspace.
 	 * 
 	 * @param event
 	 *            {@link IResourceChangeEvent} object.
 	 */
 	private void processChanges(IResourceChangeEvent event) {
 
 		IResourceDelta[] resourceDeltas = event.getDelta()
 				.getAffectedChildren();
 
 		List<GroupInfo> info = new ArrayList<GroupInfo>();
 		m_changedTypes.clear();
 
 		for (IResourceDelta resourceDelta : resourceDeltas) {
 			IProject project = resourceDelta.getResource().getProject();
 
 			if (!project.exists() || !project.isOpen()) {
 				continue;
 			}
 
 			// process delta if project exist and type space loading is
 			// finished else create add group event.
			if (m_tsmgr.existGroup(project.getName()) && m_tsmgr.isLoaded()) {
 				processDelta(resourceDelta);
			} else if (m_tsmgr.isLoaded()) {
 				// for (Object o :
 				// m_tsmgr.getController().getJstTypeSpaceMgr().getTypeSpace().getGroups().keySet().toArray())
 				// {
 				// String s = (String)o;
 				// if (!s.endsWith(".jar")) {
 				// System.out.println(o);
 				// }
 				// }
 				// updateGroupDepends();
 				processAddGroup(info, project);
 			}
 
 		}
 
 		// Calls group load job if group info list in not empty else call
 		// refresh job for changes resources.
 		if (!info.isEmpty()) {
 
 			TypeSpaceGroupLoadJob groupLoadJob = new TypeSpaceGroupLoadJob(info);
			m_tsmgr.setLoaded(false);
 			groupLoadJob.schedule();
 
 		} else if (!m_changedTypes.isEmpty()) {
 			TypeSpaceTracer.loadRefreshEvent(getChangedTypes());
 			m_refreshJob.schedule();
 		}
 
 	}
 
 	private void processAddGroup(List<GroupInfo> info, IProject project) {
 
 		try {
 			IScriptProject scriptProject;
 			Model model = m_manager.getModel();
 			scriptProject = model.getScriptProject(project.getName());
 			if (scriptProject.exists()) {
 				populateGroupInfos(info, scriptProject, getGroupDepends());
 			}
 		} catch (ModelException e) {
 			DLTKCore.error(e.getMessage(), e);
 		}
 	}
 
 	public IProject getProjectName(IResourceDelta delta) {
 		return delta.getAffectedChildren()[0].getResource().getProject();
 	}
 
 	/**
 	 * Send {@link RemoveGroupEvent} to the {@link TypeSpaceMgr} for specified
 	 * {@link IResourceChangeEvent} object.
 	 * 
 	 * @param event
 	 *            {@link IResourceChangeEvent} object.
 	 */
 	private void processCloseProject(IResourceChangeEvent event) {
 		IProject project = (IProject) event.getResource();
 		String name = project.getName();
 		RemoveGroupEvent removeGroupEvent = new RemoveGroupEvent(name, name);
 		m_tsmgr.processEvent(removeGroupEvent);
 	}
 
 	/**
 	 * Returns true if was changed ".buildpath" file only.
 	 * 
 	 * @param delta
 	 *            {@link IResourceDelta} object.
 	 * @return true if was changed ".buildpath" file only.
 	 */
 	public static boolean isBildPathChangedEvent(IResourceDelta delta) {
 
 		boolean isBuildPathCnahgedEvent = false;
 		IResource resource = delta.getResource();
 
 		// if changed .buildpath file
 		if (resource.getName().equals(ScriptProject.BUILDPATH_FILENAME)) {
 			isBuildPathCnahgedEvent = delta.getKind() == IResourceDelta.CHANGED;
 		}
 
 		if(resource instanceof IProject && delta.getAffectedChildren().length==0){
 			return true;
 		}
 
 		// check that no changes of the other recourses
 		if (!isBuildPathCnahgedEvent) {
 
 			IResourceDelta[] deltas = delta.getAffectedChildren();
 			for (IResourceDelta resourceDelta : deltas) {
 				isBuildPathCnahgedEvent = isBildPathChangedEvent(resourceDelta);
 				if (isBuildPathCnahgedEvent) {
 					break;
 				}
 			}
 
 		}
 
 		return isBuildPathCnahgedEvent;
 	}
 
 	/**
 	 * Process resource delta and add {@link SourceTypeName} to the inner
 	 * m_changedTypes list only when resource changed not for add/remove/modify
 	 * markers.
 	 * 
 	 * @param delta
 	 *            {@link IResourceDelta} object.
 	 */
 	private void processDelta(IResourceDelta delta) {
 		IResource resource = delta.getResource();
 
 		// fix typespace not updated after refactor finished
 
 		if (resource instanceof IFile) {
 			switch (delta.getKind()) {
 			case IResourceDelta.CHANGED:
 				if ((delta.getFlags() & ~(IResourceDelta.SYNC | IResourceDelta.MARKERS)) != 0) {
 					loadFile(delta, resource);
 				}
 				break;
 			case IResourceDelta.ADDED:
 				loadFile(delta, resource);
 				break;
 			case IResourceDelta.REMOVED:
 				loadFile(delta, resource);
 				break;
 			default:
 				break;
 			}
 		} else if (PiggyBackClassPathUtil.isInSourceFolder(resource)
 				|| resource instanceof IProject) {
 			IResourceDelta[] deltas = delta.getAffectedChildren();
 			for (IResourceDelta resourceDelta : deltas) {
 				processDelta(resourceDelta);
 			}
 		}
 
 	}
 
 	/**
 	 * Returns true if delta contains markers flag.
 	 * 
 	 * @param delta
 	 *            {@link IResourceDelta} object.
 	 * @return true if delta contains markers flag.
 	 */
 	// private boolean isResourceMarker(IResourceDelta delta) {
 	// return (delta.getFlags() & IResourceDelta.MARKERS) != 0;
 	// }
 	private void loadFile(IResourceDelta delta, IResource resource) {
 		IFile file = (IFile) resource;
 		SourceTypeName name = null;
 		if (isValidFile(file)) {
 			loadVjoFile(delta, file);
 		}
 	}
 
 	/**
 	 * Create for {@link File} object corresponding {@link SourceTypeName}
 	 * object and adding to the inner list of changed types.
 	 * 
 	 * @param delta
 	 *            {@link IResourceDelta} object
 	 * @param file
 	 *            {@link File} object.
 	 */
 	private void loadVjoFile(IResourceDelta delta, IFile file) {
 		SourceTypeName name;
 
 		if (file.exists()) {
 			name = createSourceTypeName(file);
 		} else {
 			name = createSourceTypeName(file, SourceTypeName.EMPTY_CONTENT);
 		}
 
 		String typeName = CodeassistUtils.getClassName(file);
 
 		switch (delta.getKind()) {
 		case IResourceDelta.ADDED:
 			if (!m_tsmgr.existType(file.getProject().getName(), typeName)) {
 				name.setAction(SourceTypeName.ADDED);
 				m_changedTypes.add(name);
 				if (file.exists()) {
 					m_tsmgr.getController().parseAndResolve(name.groupName(),
 							file.getLocation().toFile());
 				}
 			}
 			break;
 		case IResourceDelta.CHANGED:
 			if (m_tsmgr.existType(file.getProject().getName(), typeName)) {
 				name.setAction(SourceTypeName.CHANGED);
 				m_changedTypes.add(name);
 			}
 
 			break;
 		case IResourceDelta.REMOVED:
 			if (m_tsmgr.existType(file.getProject().getName(), typeName)) {
 				name.setAction(SourceTypeName.REMOVED);
 				m_changedTypes.add(name);
 			}
 			break;
 		default:
 			break;
 		}
 		// return !(delta.getKind() == IResourceDelta.ADDED &&
 		// m_tsmgr.existType(file.getProject().getName(), typeName));
 
 	}
 
 	/**
 	 * Return true if not (type name exist in type space and delta kind equals
 	 * {@link IResourceDelta#ADDED})
 	 * 
 	 * @param delta
 	 *            {@link IResourceDelta} object.
 	 * @param file
 	 *            {@link IFile} object
 	 * @param typeName
 	 *            type name of the file
 	 * @return true if not (type name exist in type space and delta kind equals
 	 *         {@link IResourceDelta#ADDED})
 	 */
 	private boolean canModify(IResourceDelta delta, IFile file, String typeName) {
 		return !(delta.getKind() == IResourceDelta.ADDED && m_tsmgr.existType(
 				file.getProject().getName(), typeName));
 	}
 
 	public int getRefreshJobState() {
 		return m_refreshJob.getState();
 	}
 
 	public int getReloadJobState() {
 		return m_reloadJob.getState();
 	}
 
 	public List<GroupInfo> getGroupInfo() {
 		return getScriptProjectEntries();
 	}
 
 	public List<GroupInfo> getGroupInfo(String group) {
 		List<GroupInfo> info = new ArrayList<GroupInfo>();
 		Map<String, List<String>> groupDependency = getGroupDepends();
 		IScriptProject p = m_manager.getModel().getScriptProject(group);
 		try {
 			populateGroupInfos(info, p, groupDependency);
 		} catch (ModelException e) {
 			DLTKCore.error(e.toString(), e);
 		}
 		return info;
 	}
 
 	/**
 	 * Return list of the {@link GroupInfo} object created from the
 	 * corresponding {@link IScriptProject} object.
 	 * 
 	 * @return list of the {@link GroupInfo} object created from the
 	 *         corresponding {@link IScriptProject} object.
 	 */
 	private List<GroupInfo> getScriptProjectEntries() {
 		Model model = m_manager.getModel();
 		List<GroupInfo> info = new ArrayList<GroupInfo>();
 		IScriptProject[] projects;
 		java.io.File groupPath = null;
 
 		try {
 			projects = model.getScriptProjects(VjoNature.NATURE_ID);
 			populateGroupInfos(info, projects);
 
 		} catch (ModelException e) {
 			DLTKCore.error(e.toString(), e);
 		}
 
 		return info;
 	}
 
 	private void populateGroupInfos(List<GroupInfo> info,
 			IScriptProject[] projects) throws ModelException {
 
 		Map<String, List<String>> groupDependency = getGroupDepends();
 		for (IScriptProject project : projects) {
 			populateGroupInfos(info, project, groupDependency);
 		}
 	}
 
 	/**
 	 * Populate info list from the {@link IScriptProject} project.
 	 * 
 	 * @param info
 	 *            list of the {@link GroupInfo} objects.
 	 * @param project
 	 *            {@link IScriptProject} object.
 	 * @throws ModelException
 	 */
 	private void populateGroupInfos(final List<GroupInfo> info,
 			final IScriptProject project,
 			final Map<String, List<String>> groupDependency)
 			throws ModelException {
 		// initialize dltk build path
 		if (!PiggyBackClassPathUtil
 				.ifScriptProjectInitializedFromJavaProject(project)) {
 			PiggyBackClassPathUtil
 					.initializeScriptProjectFromJavProject(project);
 		}
 		// initialize dependant jars
 
 		List<String> classPaths = new ArrayList<String>();
 
 		List<URL> urls = PiggyBackClassPathUtil
 				.getProjectDependantJars_DLTK(project);
 
 		List<String> groupDepends = new ArrayList<String>();
 		TypeSpaceBuilder.getSerFileGroupDepends(project, groupDepends);
 
 		IBuildpathEntry bootstrapPath = TypeSpaceBuilder
 				.getBootstrapDir(project);
 
 		for (URL u : urls) {
 			java.io.File file = new java.io.File(u.getFile());
 			String fileName = file.getName();
 			// Skip the file which has been calculated into type space
 			if (m_tsmgr.existGroup(fileName)) {
 				continue;
 			}
 			// Check if the jar has been added into list
 			if (!classPaths.contains(fileName)) {
 				SourcePathInfo srcPathInfo = new SourcePathInfo();
 				srcPathInfo.addSourcePath(file.getPath());
 				info.add(new GroupInfo(file.getName(), file.getAbsolutePath(),
 						srcPathInfo, null, groupDependency.get(file.getName())));
 				classPaths.add(fileName);
 				System.out.println("ScriptProject<" + project.getElementName()
 						+ "> depends on :" + u);
 			}
 		}
 		// initialize src path
 		SourcePathInfo srcPathInfo =  PiggyBackClassPathUtil
 				.getProjectSrcPath_DLTK(project);
 		List<String> srcPaths =srcPathInfo.getSourcePaths();
 
 		String name = project.getProject().getName();
 		java.io.File groupPath = project.getProject().getLocation().toFile();
 
 		List<String> bootstrapDirs = new ArrayList<String>();
 		if (bootstrapPath != null) {
 			String portableString = "";
 			portableString = getRelativeProjectPath(project, bootstrapPath);
 			bootstrapDirs.add(portableString);
 		}
 		info.add(new GroupInfo(name, groupPath.getAbsolutePath(), srcPathInfo,
 				classPaths, groupDependency.get(name), bootstrapDirs));
 	}
 
 	private String getRelativeProjectPath(IScriptProject project,
 			IBuildpathEntry bootstrap) {
 		String name = project.getElementName();
 		String bootstrapPath = bootstrap.getPath().toPortableString();
 		if (bootstrapPath.lastIndexOf(name) != -1) {
 			if (bootstrapPath.equals(name)) {
 				bootstrapPath = "";
 			} else {
 				bootstrapPath = bootstrapPath.substring(bootstrapPath
 						.indexOf(name) + name.length());
 
 			}
 		}
 		return bootstrapPath;
 	}
 
 	/**
 	 * @return the isStarted
 	 */
 	public boolean isStarted() {
 		return m_started;
 	}
 
 	/**
 	 * @param isStarted
 	 *            the isStarted to set
 	 */
 	public void setStarted(boolean isStarted) {
 		this.m_started = isStarted;
 	}
 
 }
