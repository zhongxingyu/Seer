 /*******************************************************************************
  * Copyright (c) 2005, 2012 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 
 package org.eclipse.vjet.eclipse.core.builder;
 
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.core.internal.resources.ResourceException;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceDelta;
 import org.eclipse.core.resources.IResourceDeltaVisitor;
 import org.eclipse.core.resources.IResourceVisitor;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.QualifiedName;
 import org.eclipse.core.runtime.SubProgressMonitor;
 import org.eclipse.dltk.mod.ast.declarations.ModuleDeclaration;
 import org.eclipse.dltk.mod.ast.parser.ISourceParserConstants;
 import org.eclipse.dltk.mod.compiler.problem.DefaultProblem;
 import org.eclipse.dltk.mod.compiler.problem.IProblem;
 import org.eclipse.dltk.mod.compiler.problem.IProblemReporter;
 import org.eclipse.dltk.mod.compiler.problem.ProblemCollector;
 import org.eclipse.dltk.mod.core.DLTKContentTypeManager;
 import org.eclipse.dltk.mod.core.DLTKCore;
 import org.eclipse.dltk.mod.core.DLTKLanguageManager;
 import org.eclipse.dltk.mod.core.IBuildpathEntry;
 import org.eclipse.dltk.mod.core.IDLTKLanguageToolkit;
 import org.eclipse.dltk.mod.core.IModelElement;
 import org.eclipse.dltk.mod.core.IModelElementVisitor;
 import org.eclipse.dltk.mod.core.IModelMarker;
 import org.eclipse.dltk.mod.core.IProjectFragment;
 import org.eclipse.dltk.mod.core.IScriptFolder;
 import org.eclipse.dltk.mod.core.IScriptProject;
 import org.eclipse.dltk.mod.core.ISourceModule;
 import org.eclipse.dltk.mod.core.ISourceModuleInfoCache.ISourceModuleInfo;
 import org.eclipse.dltk.mod.core.IType;
 import org.eclipse.dltk.mod.core.ModelException;
 import org.eclipse.dltk.mod.core.SourceParserUtil;
 import org.eclipse.dltk.mod.core.builder.IBuildContext;
 import org.eclipse.dltk.mod.core.builder.IScriptBuilder;
 import org.eclipse.dltk.mod.core.builder.IScriptBuilderExtension;
 import org.eclipse.dltk.mod.core.environment.EnvironmentPathUtils;
 import org.eclipse.dltk.mod.internal.core.BuildpathEntry;
 import org.eclipse.dltk.mod.internal.core.BuiltinProjectFragment;
 import org.eclipse.dltk.mod.internal.core.BuiltinSourceModule;
 import org.eclipse.dltk.mod.internal.core.ExternalProjectFragment;
 import org.eclipse.dltk.mod.internal.core.ExternalSourceModule;
 import org.eclipse.dltk.mod.internal.core.ModelManager;
 import org.eclipse.dltk.mod.internal.core.ScriptProject;
 import org.eclipse.dltk.mod.internal.core.VjoSourceHelper;
 import org.eclipse.dltk.mod.internal.core.VjoSourceModule;
 import org.eclipse.dltk.mod.internal.core.builder.BuildProblemReporter;
 import org.eclipse.dltk.mod.internal.core.builder.ScriptBuilder;
 import org.eclipse.dltk.mod.internal.core.builder.State;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.vjet.dsf.jst.IJstType;
 import org.eclipse.vjet.dsf.jst.IScriptProblem;
 import org.eclipse.vjet.eclipse.codeassist.CodeassistUtils;
 import org.eclipse.vjet.eclipse.core.VjetPlugin;
 import org.eclipse.vjet.eclipse.core.VjoLanguageToolkit;
 import org.eclipse.vjet.eclipse.core.parser.VjoParserToJstAndIType;
 import org.eclipse.vjet.eclipse.core.ts.EclipseTypeSpaceLoader;
 import org.eclipse.vjet.eclipse.core.validation.ValidationEntry;
 import org.eclipse.vjet.eclipse.core.validation.utils.ProblemUtility;
 import org.eclipse.vjet.eclipse.internal.parser.VjoSourceParser;
 import org.eclipse.vjet.vjo.tool.typespace.SourceTypeName;
 import org.eclipse.vjet.vjo.tool.typespace.TypeSpaceMgr;
 
 public class VjetScriptBuilder extends ScriptBuilder {
 	public static final boolean DEBUG = VjetPlugin.DEBUG_SCRIPT_BUILDER;
 	public static final boolean TRACE = VjetPlugin.TRACE_SCRIPT_BUILDER;
 
 	public IProject currentProject = null;
 	ScriptProject scriptProject = null;
 	State lastState;
 
 	/**
 	 * Last build following resource count.
 	 */
 	public long lastBuildResources = 0;
 	public long lastBuildSourceFiles = 0;
 	private TypeSpaceBuilder typeSpaceBuilder = new TypeSpaceBuilder();
 
 	 class ResourceVisitor implements IResourceDeltaVisitor,
 			IResourceVisitor {
 		final Set resources = new HashSet();
 		List<SourceTypeName> changedTypes = new ArrayList<SourceTypeName>();
 		private final IProgressMonitor monitor;
 
 		public ResourceVisitor(IProgressMonitor monitor) {
 			this.monitor = monitor;
 		}
 
 		public boolean visit(IResourceDelta delta) throws CoreException {
 			if (monitor.isCanceled()) {
 				return false;
 			}
 			IResource resource = delta.getResource();
 
 			if (resource.getType() == IResource.FOLDER) {
 				this.monitor
 						.subTask(Messages.ScriptBuilder_scanningProjectFolder
 								+ resource.getProjectRelativePath().toString());
 			}
 			if (resource.getType() == IResource.FILE) {
 				SourceTypeName name;
 				IFile file = (IFile) resource;
 				
 				ISourceModule module = VjoSourceHelper.getModuleFromResource(file, scriptProject);
 				TypeSpaceMgr tsmgr = TypeSpaceMgr.getInstance();
 				String typeName = CodeassistUtils.getClassName(file);
 				if(module instanceof VjoSourceModule){
 					VjoSourceModule vjoSourceModule = (VjoSourceModule) module;
 					IType iType = vjoSourceModule.getTypes()[0];
 					
 					String pkg = vjoSourceModule.getPackageDeclarations()[0].getElementName();
 					if(!pkg.equals("")){
 						pkg = pkg + ".";
 					}
					typeName = pkg + iType.getElementName();
 					name = new SourceTypeName(scriptProject.getElementName(),typeName);
 					
 //					typeName = module.getElementName();
 				}else{
 				
 					if (file.exists()) {
 						name = createSourceTypeName(file);
 					} else {
 						name = createSourceTypeName(file,
 								SourceTypeName.EMPTY_CONTENT);
 					}
 
 				}
 				
 
 				switch (delta.getKind()) {
 				case IResourceDelta.ADDED:
 					if (!tsmgr.existType(file.getProject().getName(), typeName)) {
 						name.setAction(SourceTypeName.ADDED);
 						// TODO should we do this during visit or defer?
 						changedTypes.add(name);
 						if (file.exists()) {
 							tsmgr.getController().parseAndResolve(
 									name.groupName(),
 									file.getLocation().toFile());
 						}
 					}
 					resources.add(resource);
 					break;
 				case IResourceDelta.CHANGED:
 					if (tsmgr.existType(file.getProject().getName(), typeName)) {
 						name.setAction(SourceTypeName.CHANGED);
 						changedTypes.add(name);
 					}
 					resources.add(resource);
 					break;
 				case IResourceDelta.REMOVED:
 					if (tsmgr.existType(file.getProject().getName(), typeName)) {
 						name.setAction(SourceTypeName.REMOVED);
 						changedTypes.add(name);
 					}
 					break;
 
 				}
 			//	return false;
 			}
 			return true;
 		}
 
 		private SourceTypeName createSourceTypeName(IFile file) {
 			byte[] content = SourceTypeName.EMPTY_CONTENT;
 
 			content = getFileContent(file);
 
 			return createSourceTypeName(file, content);
 		}
 
 		private byte[] doGetFileContent(IFile file) throws CoreException,
 				IOException {
 			InputStream stream = file.getContents();
 			int available = stream.available();
 			byte[] bs = new byte[available];
 			stream.read(bs);
 			stream.close();
 			return bs;
 		}
 
 		private SourceTypeName createSourceTypeName(IFile file, byte[] bs) {
 			String group = file.getProject().getName();
 
 			String source = new String(bs);
 			String name = CodeassistUtils.getClassName(file);
 			// SourceTypeName typeName = new SourceTypeName(group,
 			// file.getRawLocation().toPortableString(), source);
 			SourceTypeName typeName = new SourceTypeName(group, name, source);
 			return typeName;
 		}
 
 		private void logError(Exception e1) {
 			VjetPlugin.error(e1.getMessage(), e1);
 		}
 
 		private void refresh(IFile file) {
 			try {
 				file.refreshLocal(IResource.DEPTH_INFINITE, null);
 			} catch (CoreException e) {
 				DLTKCore.error(e.toString(), e);
 			}
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
 
 		private boolean isValidName(IFile file) {
 			boolean isValid = false;
 			try {
 				isValid = DLTKContentTypeManager.isValidFileNameForContentType(
 						VjoLanguageToolkit.getDefault(), file.getLocation());
 			} catch (Exception e) {
 				DLTKCore.error(e.getMessage(), e);
 			}
 			return isValid;
 		}
 
 		public boolean visit(IResource resource) {
 			if (monitor.isCanceled()) {
 				return false;
 			}
 			if (resource.getType() == IResource.FOLDER) {
 				this.monitor
 						.subTask(Messages.ScriptBuilder_scanningProjectFolder
 								+ resource.getProjectRelativePath().toString());
 			}
 			if (resource.getType() == IResource.FILE) {
 				resources.add(resource);
 				return false;
 			}
 			return true;
 		}
 	}
 
 	static class ExternalModuleVisitor implements IModelElementVisitor {
 		final Set elements = new HashSet();
 		private final IProgressMonitor monitor;
 
 		public ExternalModuleVisitor(IProgressMonitor monitor) {
 			this.monitor = monitor;
 		}
 
 		/**
 		 * Visit only external source modules, witch we aren't builded yet.
 		 */
 		public boolean visit(IModelElement element) {
 			// monitor.worked(1);
 			if (monitor.isCanceled()) {
 				return false;
 			}
 			if (element.getElementType() == IModelElement.PROJECT_FRAGMENT) {
 				if (!(element instanceof ExternalProjectFragment)
 						&& !(element instanceof BuiltinProjectFragment)) {
 					return false;
 				}
 				IProjectFragment fragment = (IProjectFragment) element;
 
 				String localPath = EnvironmentPathUtils.getLocalPath(
 						fragment.getPath()).toString();
 				if (!localPath.startsWith("#")) { //$NON-NLS-1$
 					this.monitor
 							.subTask(Messages.ScriptBuilder_scanningExternalFolder
 									+ localPath);
 				}
 			} else if (element.getElementType() == IModelElement.SOURCE_MODULE) {
 				if (element instanceof ExternalSourceModule
 						|| element instanceof BuiltinSourceModule) {
 					elements.add(element);
 				}
 				return false; // do not enter into source module content.
 			} else if (element.getElementType() == IModelElement.SCRIPT_FOLDER) {
 				IScriptFolder folder = (IScriptFolder) element;
 				String localPath = EnvironmentPathUtils.getLocalPath(
 						folder.getPath()).toString();
 				if (!localPath.startsWith("#")) { //$NON-NLS-1$
 					this.monitor
 							.subTask(Messages.ScriptBuilder_scanningExternalFolder
 									+ localPath);
 				}
 			}
 			return true;
 		}
 
 	}
 
 	/**
 	 * Hook allowing to initialize some static state before a complete build
 	 * iteration. This hook is invoked during PRE_AUTO_BUILD notification
 	 */
 	public static void buildStarting() {
 		if (VjetPlugin.DEBUG)
 			System.out.println("vjet build starting"); //$NON-NLS-1$
 
 	}
 
 	/**
 	 * Hook allowing to reset some static state after a complete build
 	 * iteration. This hook is invoked during POST_AUTO_BUILD notification
 	 */
 	public static void buildFinished() {
 		if (VjetPlugin.DEBUG)
 			System.out.println("vjet build finished"); //$NON-NLS-1$
 	}
 
 	private static void log(String message) {
 		System.out.println(message);
 	}
 
 	private static final QualifiedName PROPERTY_BUILDER_VERSION = new QualifiedName(
 			DLTKCore.PLUGIN_ID, "builderVersion"); //$NON-NLS-1$
 
 	private static final String CURRENT_VERSION = "200810012003-2123"; //$NON-NLS-1$
 
 	protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
 			throws CoreException {
 
 		this.currentProject = getProject();
 		if (currentProject == null || !currentProject.isAccessible())
 			return new IProject[0];
 
 		if (!DLTKLanguageManager.hasScriptNature(this.currentProject)) {
 			return null;
 		}
 		long startTime = 0;
 		if (DEBUG || TRACE) {
 			startTime = System.currentTimeMillis();
 			log("\nStarting build of " + this.currentProject.getName() //$NON-NLS-1$
 					+ " @ " + new Date(startTime)); //$NON-NLS-1$
 		}
 
 		this.scriptProject = (ScriptProject) DLTKCore.create(currentProject);
 
 		final String version = currentProject
 				.getPersistentProperty(PROPERTY_BUILDER_VERSION);
 		if (version == null) {
 			removeWrongTaskMarkers();
 			currentProject.setPersistentProperty(PROPERTY_BUILDER_VERSION,
 					CURRENT_VERSION);
 			kind = FULL_BUILD;
 		} else if (!CURRENT_VERSION.equals(version)) {
 			if ("200810012003".equals(version)) { //$NON-NLS-1$
 				removeWrongTaskMarkers();
 			}
 			currentProject.setPersistentProperty(PROPERTY_BUILDER_VERSION,
 					CURRENT_VERSION);
 			kind = FULL_BUILD;
 		}
 		lastBuildResources = 0;
 		lastBuildSourceFiles = 0;
 		if (monitor == null) {
 			monitor = new NullProgressMonitor();
 		}
 		if (kind == FULL_BUILD) {
 			if (DEBUG)
 				log("Performing full build as requested by user"); //$NON-NLS-1$
 			fullBuild(monitor);
 		} else {
 			if ((this.lastState = getLastState(currentProject, monitor)) == null) {
 				if (DEBUG)
 					log("Performing full build since last saved state was not found"); //$NON-NLS-1$
 				fullBuild(monitor);
 			} else {
 				IResourceDelta delta = getDelta(getProject());
 				if (delta == null) {
 					if (DEBUG)
 						log("Performing full build since deltas are missing after incremental request"); //$NON-NLS-1$
 					fullBuild(monitor);
 				} else if (VjoParserToJstAndIType.getJstParseController()
 						.getJstTypeSpaceMgr().getTypeSpace()
 						.getGroup(currentProject.getName()) == null) {
 					if (DEBUG)
 						log("Performing full build since group was not found in typespace"); //$NON-NLS-1$
 					fullBuild(monitor);
 				} else if (EclipseTypeSpaceLoader.isBildPathChangedEvent(delta)) {
 					if (DEBUG)
 						log("Performing full build since build path changed"); //$NON-NLS-1$
 					fullBuild(monitor);
 				}else if (EclipseTypeSpaceLoader.isBootstrapChangedEvent(delta)) {
 					if (DEBUG)
 						log("Performing full build since bootstrap.js changed"); //$NON-NLS-1$
 					fullBuild(monitor);
 				} else {
 					if (DEBUG)
 						log("Performing incremental build"); //$NON-NLS-1$
 					// EBAY - START MOD
 					try {
 						incrementalBuild(delta, monitor);
 					} catch (ModelException exception) {
 						DLTKCore.error(exception.getMessage());
 					}
 					// EBAY -- STOP MOD
 				}
 			}
 		}
 		IProject[] requiredProjects = getRequiredProjects(true);
 		long endTime = 0;
 		if (DEBUG || TRACE) {
 			endTime = System.currentTimeMillis();
 		}
 		if (DEBUG) {
 			log("Finished build of " + currentProject.getName() //$NON-NLS-1$
 					+ " @ " + new Date(endTime) + ", elapsed " + (endTime - startTime) + " ms"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 		}
 		if (TRACE) {
 			System.out
 					.println("-----SCRIPT-BUILDER-INFORMATION-TRACE----------------------------"); //$NON-NLS-1$
 			System.out.println("Finished build of project:" //$NON-NLS-1$
 					+ currentProject.getName() + "\n" //$NON-NLS-1$
 					+ "Building time:" //$NON-NLS-1$
 					+ Long.toString(endTime - startTime) + "\n" //$NON-NLS-1$
 					+ "Resources count:" //$NON-NLS-1$
 					+ this.lastBuildResources + "\n" //$NON-NLS-1$
 					+ "Sources count:" //$NON-NLS-1$
 					+ this.lastBuildSourceFiles + "\n" //$NON-NLS-1$
 					+ "Build type:" //$NON-NLS-1$
 					+ (kind == FULL_BUILD ? "Full build" //$NON-NLS-1$
 							: "Incremental build")); //$NON-NLS-1$
 			System.out
 					.println("-----------------------------------------------------------------"); //$NON-NLS-1$
 		}
 		monitor.done();
 		return requiredProjects;
 	}
 
 	/**
 	 * Remove incorrect task markers.
 	 * 
 	 * DLTK 0.95 were creating wrong task markers, so this function is here to
 	 * remove them. New markers will be created by the builder later.
 	 * 
 	 * @param project
 	 * @throws CoreException
 	 */
 	private void removeWrongTaskMarkers() throws CoreException {
 		final IMarker[] markers = currentProject.findMarkers(IMarker.TASK,
 				false, IResource.DEPTH_INFINITE);
 		for (int i = 0; i < markers.length; ++i) {
 			final IMarker marker = markers[i];
 			final IResource resource = marker.getResource();
 			if (resource.getType() != IResource.FILE) {
 				continue;
 			}
 			if (!DLTKContentTypeManager.isValidResourceForContentType(
 					scriptProject.getLanguageToolkit(), resource)) {
 				continue;
 			}
 			final Map attributes = marker.getAttributes();
 			if (attributes == null) {
 				continue;
 			}
 			if (!Boolean.FALSE.equals(attributes.get(IMarker.USER_EDITABLE))) {
 				continue;
 			}
 			if (attributes.containsKey(IMarker.LINE_NUMBER)
 					&& attributes.containsKey(IMarker.MESSAGE)
 					&& attributes.containsKey(IMarker.PRIORITY)
 					&& attributes.containsKey(IMarker.CHAR_START)
 					&& attributes.containsKey(IMarker.CHAR_END)) {
 				marker.delete();
 			}
 		}
 	}
 
 	protected void clean(IProgressMonitor monitor) throws CoreException {
 		long start = 0;
 		if (TRACE) {
 			start = System.currentTimeMillis();
 		}
 
 		this.currentProject = getProject();
 
 		if (!DLTKLanguageManager.hasScriptNature(this.currentProject)) {
 			return;
 		}
 		this.scriptProject = (ScriptProject) DLTKCore.create(currentProject);
 
 		monitor.beginTask(MessageFormat.format(
 				"Cleaning typespace for project {0}",
 				new Object[] { currentProject.getName() }), 66);
 		if (monitor.isCanceled()) {
 			return;
 		}
 		TypeSpaceMgr.getInstance().cleanGroup(this.scriptProject.getElementName());
 
 		if (currentProject == null || !currentProject.isAccessible())
 			return;
 
 		// try {
 		monitor.beginTask(MessageFormat.format(
 				Messages.ScriptBuilder_cleaningScriptsIn,
 				new Object[] { currentProject.getName() }), 66);
 		if (monitor.isCanceled()) {
 			return;
 		}
 
 		 IScriptBuilder[] builders = getScriptBuilders();
 
 		 if (builders != null) {
 		 for (int k = 0; k < builders.length; k++) {
 			 IProgressMonitor sub = new SubProgressMonitor(monitor, 1);
 			 builders[k].clean(scriptProject, sub);
 
 		 if (monitor.isCanceled()) {
 		 break;
 		 }
 
 		 }
 		 resetBuilders(builders);
 		 } 
 
 		if (TRACE) {
 			System.out
 					.println("-----SCRIPT-BUILDER-INFORMATION-TRACE----------------------------"); //$NON-NLS-1$
 			System.out.println("Finished clean of project:" //$NON-NLS-1$
 					+ currentProject.getName() + "\n" //$NON-NLS-1$
 					+ "Building time:" //$NON-NLS-1$
 					+ Long.toString(System.currentTimeMillis() - start));
 			System.out
 					.println("-----------------------------------------------------------------"); //$NON-NLS-1$
 		}
 		monitor.done();
 	}
 
 	private IProject[] getRequiredProjects(boolean includeBinaryPrerequisites) {
 		if (scriptProject == null)
 			return new IProject[0];
 		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
 		ArrayList projects = new ArrayList();
 		try {
 			IBuildpathEntry[] entries = scriptProject
 					.getExpandedBuildpath(true);
 			for (int i = 0, l = entries.length; i < l; i++) {
 				IBuildpathEntry entry = entries[i];
 				IPath path = entry.getPath();
 				IProject p = null;
 				switch (entry.getEntryKind()) {
 				case IBuildpathEntry.BPE_PROJECT:
 					p = workspaceRoot.getProject(path.lastSegment());
 					// missing projects are considered too
 					if (((BuildpathEntry) entry).isOptional()
 							&& !ScriptProject.hasScriptNature(p))
 						// except if entry is optional
 						p = null;
 					break;
 				case IBuildpathEntry.BPE_LIBRARY:
 					if (includeBinaryPrerequisites && path.segmentCount() > 1) {
 						/*
 						 * some binary resources on the class path can come from
 						 * projects that are not included in the project
 						 * references
 						 */
 						IResource resource = workspaceRoot.findMember(path
 								.segment(0));
 						if (resource instanceof IProject)
 							p = (IProject) resource;
 					}
 				}
 				if (p != null && !projects.contains(p))
 					projects.add(p);
 			}
 		} catch (ModelException e) {
 			return new IProject[0];
 		}
 		IProject[] result = new IProject[projects.size()];
 		projects.toArray(result);
 		return result;
 	}
 
 	public org.eclipse.dltk.mod.internal.core.builder.State getLastState(
 			IProject project, IProgressMonitor monitor) {
 		return (org.eclipse.dltk.mod.internal.core.builder.State) ModelManager
 				.getModelManager().getLastBuiltState(project, monitor);
 	}
 
 	private State clearLastState() {
 		State state = new State(this);
 		State prevState = (State) ModelManager.getModelManager()
 				.getLastBuiltState(currentProject, null);
 		if (prevState != null) {
 			if (prevState.noCleanExternalFolders) {
 				state.externalFolderLocations = prevState.externalFolderLocations;
 				return state;
 			}
 		}
 		ModelManager.getModelManager().setLastBuiltState(currentProject, null);
 		return state;
 	}
 
 	private static final int WORK_RESOURCES = 50;
 	private static final int WORK_EXTERNAL = 100;
 	private static final int WORK_SOURCES = 100;
 	private static final int WORK_BUILD = 750;
 
 	private static final String NONAME = ""; //$NON-NLS-1$
 
 	protected void fullBuild(final IProgressMonitor monitor) {
 
 		State newState = clearLastState();
 		this.lastState = newState;
 		IScriptBuilder[] builders = null;
 		try {
 			monitor.setTaskName(NLS.bind(
 					Messages.ScriptBuilder_buildingScriptsIn,
 					currentProject.getName()));
 
 			List<String> groupDepends = new ArrayList<String>();
 			IBuildpathEntry bootstrapPath = TypeSpaceBuilder
 					.getBootstrapDir(scriptProject);
 			
 //			TypeSpaceBuilder
 //					.getSerFileGroupDepends(scriptProject, groupDepends);
 
 			// setup type space group but don't add any types to group
 			this.typeSpaceBuilder.buildProject(scriptProject, null, monitor,
 					groupDepends, bootstrapPath);
 
 			monitor.beginTask(NONAME, WORK_RESOURCES + WORK_EXTERNAL
 					+ WORK_SOURCES + WORK_BUILD);
 			Set resources = getResourcesFrom(currentProject, monitor,
 					WORK_RESOURCES).resources;
 			if (monitor.isCanceled()) {
 				return;
 			}
 			Set externalElements = getExternalElementsFrom(scriptProject,
 					monitor, WORK_EXTERNAL, true);
 			Set externalFolders = new HashSet(
 					this.lastState.externalFolderLocations);
 			if (monitor.isCanceled()) {
 				return;
 			}
 			// Project external resources should also be added into list. Only
 			// on full build we need to manage this.
 			// Call builders for resources.
 			int totalFiles = resources.size() + externalElements.size();
 			if (totalFiles == 0)
 				totalFiles = 1;
 
 			builders = getScriptBuilders();
 
 			List localResources = new ArrayList();
 			List localElements = new ArrayList();
 			locateSourceModules(monitor, WORK_SOURCES, resources,
 					localElements, localResources);
 
 			if (monitor.isCanceled()) {
 				return;
 			}
 			int resourceTicks = WORK_BUILD
 					* (resources.size() - localElements.size()) / totalFiles;
 			resourceTicks = Math.min(resourceTicks, WORK_BUILD / 4);
 			Object o = null;
 			try {
 				clearMarkers(localElements);
 				// add types to group
 				buildElements(localElements, externalElements, monitor,
 						WORK_BUILD - resourceTicks, IScriptBuilder.FULL_BUILD,
 						Collections.EMPTY_SET, externalFolders, builders);
 				lastBuildSourceFiles += externalElements.size();
 			} catch (CoreException e) {
 				DLTKCore.error(Messages.ScriptBuilder_errorBuildElements, e);
 			}
 
 			if (monitor.isCanceled()) {
 				return;
 			}
 			buildResources(localResources, monitor, resourceTicks, FULL_BUILD,
 					builders);
 
 			lastBuildResources = resources.size() + externalElements.size();
 		} catch (CoreException e) {
 			if (DLTKCore.DEBUG) {
 				e.printStackTrace();
 			}
 		} finally {
 			resetBuilders(builders);
 
 			monitor.done();
 			ModelManager.getModelManager().setLastBuiltState(currentProject,
 					this.lastState);
 		}
 	}
 
 	private void clearMarkers(List localElements) throws CoreException {
 		Object o;
 		for (Iterator i = localElements.iterator(); i.hasNext();) {
 			o = i.next();
 			if (o instanceof IModelElement) {
 				final IResource resource = ((IModelElement) o)
 						.getResource();
 				if (resource != null) {
 					final String template = Messages.ValidatorBuilder_clearingResourceMarkers;
 					resource.deleteMarkers(
 							DefaultProblem.MARKER_TYPE_PROBLEM, true,
 							IResource.DEPTH_INFINITE);
 					resource.deleteMarkers(
 							DefaultProblem.MARKER_TYPE_TASK, true,
 							IResource.DEPTH_INFINITE);
 				}
 			}
 		}
 	}
 
 	private void resetBuilders(IScriptBuilder[] builders) {
 		if (builders != null) {
 			for (int k = 0; k < builders.length; k++) {
 				builders[k].reset(scriptProject);
 			}
 		}
 	}
 
 	private ResourceVisitor getResourcesFrom(Object el, final IProgressMonitor monitor,
 			int ticks) throws CoreException {
 		monitor.subTask(Messages.ScriptBuilder_scanningProject);
 		try {
 			ResourceVisitor resourceVisitor = new ResourceVisitor(monitor);
 			if (el instanceof IProject) {
 				IProject prj = (IProject) el;
 				prj.accept(resourceVisitor);
 			} else if (el instanceof IResourceDelta) {
 				IResourceDelta delta = (IResourceDelta) el;
 				delta.accept(resourceVisitor);
 			}
 			return resourceVisitor;
 		} finally {
 			monitor.worked(ticks);
 		}
 	}
 
 	private Set getExternalElementsFrom(ScriptProject prj,
 			final IProgressMonitor monitor, int tiks, boolean updateState)
 			throws ModelException {
 		final String name = Messages.ScriptBuilder_scanningExternalFolders;
 		monitor.subTask(name);
 		final SubProgressMonitor mon = new SubProgressMonitor(monitor, tiks);
 
 		final IProjectFragment[] fragments = prj.getAllProjectFragments();
 		// new external fragments
 		final List extFragments = new ArrayList(fragments.length);
 		// external fragments paths
 		final List fragmentPaths = new ArrayList(fragments.length);
 		for (int i = 0; i < fragments.length; i++) {
 			final IProjectFragment fragment = fragments[i];
 			if (fragment instanceof ExternalProjectFragment
 					|| fragment instanceof BuiltinProjectFragment) {
 				final IPath path = fragment.getPath();
 				if (!updateState
 						|| !this.lastState.externalFolderLocations
 								.contains(path)) {
 					extFragments.add(fragment);
 				} else {
 					fragmentPaths.add(path);
 				}
 			}
 		}
 		// monitor.subTask(name);
 		final ExternalModuleVisitor visitor = new ExternalModuleVisitor(mon);
 		mon.beginTask(name, extFragments.size());
 		for (Iterator iterator = extFragments.iterator(); iterator.hasNext();) {
 			IProjectFragment fragment = (IProjectFragment) iterator.next();
 			// New project fragment, we need to obtain all modules
 			// from this fragment.
 			fragment.accept(visitor);
 			if (updateState) {
 				fragmentPaths.add(fragment.getPath());
 			}
 			mon.worked(1);
 		}
 		mon.done();
 		if (updateState) {
 			this.lastState.externalFolderLocations.clear();
 			this.lastState.externalFolderLocations.addAll(fragmentPaths);
 		}
 
 		return visitor.elements;
 	}
 
 	protected void incrementalBuild(IResourceDelta delta,
 			IProgressMonitor monitor) throws CoreException {
 		State newState = new State(this);
 
 		Set externalFoldersBefore = new HashSet();
 		if (this.lastState != null) {
 			newState.copyFrom(this.lastState);
 			externalFoldersBefore.addAll(newState.getExternalFolders());
 		}
 
 		this.lastState = newState;
 		IScriptBuilder[] builders = null;
 		try {
 			monitor.setTaskName(NLS.bind(
 					Messages.ScriptBuilder_buildingScriptsIn,
 					currentProject.getName()));
 			monitor.beginTask(NONAME, WORK_RESOURCES + WORK_EXTERNAL
 					+ WORK_SOURCES + WORK_BUILD);
 
 			if (monitor.isCanceled()) {
 				return;
 			}
 			ResourceVisitor resourcesFrom = getResourcesFrom(delta, monitor, WORK_RESOURCES);
 			Set resources = resourcesFrom.resources;
 			if (monitor.isCanceled()) {
 				return;
 			}
 			if (DEBUG)
 				log("Number of changed resources in delta: " + resources.size()); //$NON-NLS-1$
 			Set externalElements = getExternalElementsFrom(scriptProject,
 					monitor, WORK_EXTERNAL, true);
 			// New external folders set
 			Set externalFolders = new HashSet(lastState.externalFolderLocations);
 			if (monitor.isCanceled()) {
 				return;
 			}
 
 			int totalFiles = resources.size() + externalElements.size();
 			if (totalFiles == 0)
 				totalFiles = 1;
 
 			builders = getScriptBuilders();
 
 			List localResources = new ArrayList();
 			List localElements = new ArrayList();
 			locateSourceModules(monitor, WORK_SOURCES, resources,
 					localElements, localResources);
 
 			if (monitor.isCanceled()) {
 				return;
 			}
 			int resourceTicks = WORK_BUILD
 					* (resources.size() - localElements.size()) / totalFiles;
 
 			if (monitor.isCanceled()) {
 				return;
 			}
 			
 //			clearMarkers(localElements);
 			
 			
 			// TODO build the files that are dependants of the changed types
 			this.typeSpaceBuilder.incrementalBuildProject(resourcesFrom.changedTypes);
 
 		
 //			try {
 //				buildElements(localElements, externalElements, monitor,
 //						WORK_BUILD - resourceTicks,
 //						IScriptBuilder.INCREMENTAL_BUILD,
 //						externalFoldersBefore, externalFolders, builders);
 //			} catch (CoreException e) {
 //				DLTKCore.error(Messages.ScriptBuilder_errorBuildElements, e);
 //			}
 			lastBuildSourceFiles += externalElements.size();
 
 			if (monitor.isCanceled()) {
 				return;
 			}
 			buildResources(localResources, monitor, resourceTicks, FULL_BUILD,
 					builders);
 
 			lastBuildResources = resources.size() + externalElements.size();
 		} finally {
 			resetBuilders(builders);
 
 			monitor.done();
 			ModelManager.getModelManager().setLastBuiltState(currentProject,
 					this.lastState);
 		}
 	}
 
 	/**
 	 * Return script builders for the current project. ScriptBuilders are
 	 * initialized here so this method should be called only once during build
 	 * operation.
 	 * 
 	 * @return
 	 * @throws CoreException
 	 */
 	private IScriptBuilder[] getScriptBuilders() throws CoreException {
 		IDLTKLanguageToolkit toolkit = DLTKLanguageManager
 				.getLanguageToolkit(scriptProject);
 		if (toolkit != null) {
 			IScriptBuilder[] builders = ScriptBuilderManager
 					.getScriptBuilders(toolkit.getNatureId());
 			if (builders != null) {
 				for (int k = 0; k < builders.length; k++) {
 					builders[k].initialize(scriptProject);
 				}
 			}
 			return builders;
 		} else {
 			return null;
 		}
 	}
 
 	/**
 	 * Splits the <code>resources</code> into <code>realElements</code> and
 	 * <code>realResources</code>
 	 * 
 	 * @param monitor
 	 * @param tiks
 	 * @param resources
 	 * @param realResources
 	 * @return
 	 */
 	protected void locateSourceModules(IProgressMonitor monitor, int tiks,
 			Set resources, List realElements, List realResources) {
 		IProgressMonitor sub = new SubProgressMonitor(monitor, tiks / 3);
 		sub.beginTask(NONAME, resources.size());
 		int remainingWork = resources.size();
 		for (Iterator iterator = resources.iterator(); iterator.hasNext();) {
 			if (monitor.isCanceled()) {
 				return;
 			}
 			IResource res = (IResource) iterator.next();
 			sub.subTask(NLS.bind(
 					Messages.ScriptBuilder_Locating_source_modules,
 					String.valueOf(remainingWork), res.getName()));
 			sub.worked(1);
 			if (sub.isCanceled()) {
 				return;
 			}
 			IModelElement element = DLTKCore.create(res);
 			if (element != null
 					&& element.getElementType() == IModelElement.SOURCE_MODULE
 					&& element.exists()) {
 				realElements.add(element);
 			} else {
 				realResources.add(res);
 			}
 			--remainingWork;
 		}
 		sub.done();
 		lastBuildSourceFiles += realElements.size();
 	}
 
 	/**
 	 * Build only resources, if some resources are elements they they will be
 	 * returned.
 	 */
 	protected void buildResources(List realResources, IProgressMonitor monitor,
 			int tiks, int buildType, IScriptBuilder[] builders) {
 		if (builders == null || builders.length == 0 || realResources.isEmpty()) {
 			monitor.worked(tiks);
 			return;
 		}
 		final IProgressMonitor ssub = new SubProgressMonitor(monitor, tiks);
 		try {
 			ssub.beginTask(Messages.ScriptBuilder_building, builders.length);
 			for (int k = 0; k < builders.length; k++) {
 				builders[k].buildResources(scriptProject, realResources,
 						new SubProgressMonitor(ssub, 1), buildType);
 			}
 		} finally {
 			ssub.done();
 		}
 	}
 
 	protected void buildElements(List localElements, Set externalElements,
 			IProgressMonitor monitor, int ticks, int buildType,
 			Set externalFoldersBefore, Set externalFolders,
 			IScriptBuilder[] builders) throws CoreException {
 
 		// TODO: replace this stuff with multistatus
 		if (builders == null) {
 			return;
 		}
 
 		final int[] workEstimations = new int[builders.length];
 		final List[] builderToElements = new List[builders.length];
 		final List[] builderExternalElements = new List[builders.length];
 		final int[] buildTypes = new int[builders.length];
 		Arrays.fill(buildTypes, buildType);
 		estimateBuild(localElements, externalElements, externalFoldersBefore,
 				externalFolders, builders, workEstimations, builderToElements,
 				builderExternalElements, buildTypes);
 
 		int total = 0;
 		for (int k = 0; k < builders.length; k++) {
 			total += workEstimations[k];
 		}
 
 		for (int k = 0; k < builders.length; k++) {
 			if (monitor.isCanceled()) {
 				return;
 			}
 			final IScriptBuilder builder = builders[k];
 			int builderWork = workEstimations[k] * ticks / total;
 			final List buildExternalElements = builderExternalElements[k];
 			if (buildExternalElements != null
 					&& buildExternalElements.size() > 0
 					&& builder instanceof IScriptBuilderExtension) {
 				final int step = buildExternalElements.size() * ticks / total;
 				builderWork -= step;
 				monitor.subTask(NLS.bind(
 						Messages.ScriptBuilder_building_N_externalModules,
 						Integer.toString(buildExternalElements.size())));
 				((IScriptBuilderExtension) builder).buildExternalElements(
 						scriptProject, buildExternalElements,
 						new SubProgressMonitor(monitor, step), buildTypes[k]);
 			}
 			final List buildElementsList = builderToElements[k];
 			if (buildElementsList.size() > 0) {
 				final int step = buildElementsList.size() * ticks / total;
 				builderWork -= step;
 				monitor.subTask(NLS.bind(
 						Messages.ScriptBuilder_building_N_localModules,
 						Integer.toString(buildElementsList.size())));
 				
 				// load into type space
 				
 			
 				List<VjetSourceModuleBuildCtx> postBuildCtx = new ArrayList<VjetSourceModuleBuildCtx>(buildElementsList.size());
 				for (Iterator j = buildElementsList.iterator(); j.hasNext();) {
 					final ISourceModule module = (ISourceModule) j.next();
 					// create context
 					final VjetSourceModuleBuildCtx context = new VjetSourceModuleBuildCtx(
 							module);
 					
 					
 					 
 					// build it/ parse it
 					build(context);
 //					((BuildProblemReporter)context.getProblemReporter()).flush();
 					postBuildCtx.add(context);
 					
 					
 				}
 				
 				
 				// resolve and validate - later can optimize for only types with dependencies
 				for (VjetSourceModuleBuildCtx module : postBuildCtx) {
 					// resolve it now and validate
 					
 					IProblemReporter reporter = module.getProblemReporter();
 					boolean validatable = reporter!=null;
 					IJstType unit = module.getUnit();
 					TypeSpaceMgr.getInstance().getController().resolve(module.getVjoSourceModule().getGroupName(), unit);
 					if (unit != null) {
 						//if disable all the validations (syntax and semantic)
 						if  (!ValidationEntry.isEnableVjetValidation()) {
 							continue;
 						}
 						// deal with problems
 						List<DefaultProblem> dproblems = null;
 						List<IScriptProblem> problems = unit.getProblems();
 						if (problems!=null && !problems.isEmpty() && validatable) {
 							dproblems = ProblemUtility.reportProblems(problems);	
 						}else if(validatable){
 							dproblems = ValidationEntry.validator(unit);
 						}
 						
 						// TODO determine why build is not reporting markers to script unit view
 						if (dproblems != null && reporter != null) {
 							
 							reportProblems(dproblems, reporter);
 							
 							
 						}
 						module.setUnit(null);
 						((BuildProblemReporter)reporter).flush();
 						
 					}
 					
 				}
 				
 				postBuildCtx = null;
 
 //				builder.buildModelElements(scriptProject, buildElementsList,
 //						new SubProgressMonitor(monitor, step), buildTypes[k]);
 			}
 			if (builderWork > 0) {
 				monitor.worked(builderWork);
 			}
 		}
 		// TODO: Do something with status.
 	}
 	
 	private void reportProblems(List<DefaultProblem> problems,
 			IProblemReporter reporter) {
 		if (problems == null) {
 			return;
 		}
 		for (IProblem problem : problems) {
 			reporter.reportProblem(problem);
 		}
 
 	}
 	
 
 	public void build(VjetSourceModuleBuildCtx context) throws CoreException {
 		VjoSourceParser parser = new VjoSourceParser();
 		
 		ModuleDeclaration moduleDeclaration = (ModuleDeclaration) context
 				.get(IBuildContext.ATTR_MODULE_DECLARATION);
 		if (moduleDeclaration != null) {
 			// do nothing if already have AST - optimization for reconcile
 			return;
 		}
 		// get cache entry
 		final ISourceModuleInfo cacheEntry = ModelManager.getModelManager()
 				.getSourceModuleInfoCache().get(context.getSourceModule());
 		// check if there is cached AST
 		moduleDeclaration = SourceParserUtil.getModuleFromCache(cacheEntry,
 				ISourceParserConstants.DEFAULT,
 				context.getProblemReporter());
 		if (moduleDeclaration != null) {
 			// use AST from cache
 			context.set(IBuildContext.ATTR_MODULE_DECLARATION,
 					moduleDeclaration);
 			// eBay mod start
 			// return;
 			// eBay mod end
 		}
 		// create problem collector
 		final ProblemCollector problemCollector = new ProblemCollector();
 		// parse
 		moduleDeclaration = parser.parse(context.getSourceModule()
 				.getPath().toString().toCharArray(), context.getContents(),
 				context.getProblemReporter(), context);
 		// put result to the cache
 		SourceParserUtil.putModuleToCache(cacheEntry, moduleDeclaration,
 				ISourceParserConstants.DEFAULT, problemCollector);
 		// report errors to the build context
 		problemCollector.copyTo(context.getProblemReporter());
 	
 }
 
 	/**
 	 * Processes input data and fills the <code>workEstimations</code>,
 	 * <code>builderToElements</code> and <code>builderExternalElements</code>
 	 * arrays.
 	 * 
 	 * @param localElements
 	 *            changed source modules
 	 * @param externalElements
 	 *            newly added external source modules
 	 * @param externalFoldersBefore
 	 *            old external fragments
 	 * @param externalFolders
 	 *            new external fragments
 	 * @param builders
 	 *            array of builders
 	 * @param workEstimations
 	 *            arrays of work estimations
 	 * @param builderToElements
 	 *            arrays of elements to be build by each builder
 	 * @param builderExternalElements
 	 *            arrays of external elements to be build by each builder
 	 * @param buildTypes
 	 *            resulting build types for each builder
 	 * @throws CoreException
 	 */
 	private void estimateBuild(List localElements, Set externalElements,
 			Set externalFoldersBefore, Set externalFolders,
 			IScriptBuilder[] builders, final int[] workEstimations,
 			final List[] builderToElements, List[] builderExternalElements,
 			int[] buildTypes) throws CoreException {
 		final HashSet elementsAsSet = new HashSet(localElements);
 		List projectElements = null;
 		List projectExternalElements = null;
 		for (int k = 0; k < builders.length; k++) {
 			final IScriptBuilder builder = builders[k];
 			final IScriptBuilder.DependencyResponse response = builder
 					.getDependencies(this.scriptProject, buildTypes[k],
 							elementsAsSet, externalElements,
 							externalFoldersBefore, externalFolders);
 			final List buildElementsList;
 			final List buildExternalElements;
 			if (response == null) {
 				buildElementsList = localElements;
 				buildExternalElements = null;
 			} else {
 				if (response.isFullLocalBuild()) {
 					if (buildTypes[k] == IScriptBuilder.FULL_BUILD) {
 						buildElementsList = localElements;
 					} else {
 						if (projectElements == null) {
 							projectElements = collectLocalElements();
 						}
 						buildElementsList = projectElements;
 					}
 				} else {
 					final Set dependencies = response.getLocalDependencies();
 					if (dependencies != null && !dependencies.isEmpty()
 							&& !elementsAsSet.containsAll(dependencies)) {
 						final Set e = new HashSet(elementsAsSet.size()
 								+ dependencies.size());
 						e.addAll(elementsAsSet);
 						e.addAll(dependencies);
 						buildElementsList = new ArrayList(e);
 					} else {
 						buildElementsList = localElements;
 					}
 				}
 				if (response.isFullExternalBuild()) {
 					if (buildTypes[k] == IScriptBuilder.FULL_BUILD) {
 						buildExternalElements = new ArrayList(externalElements);
 					} else {
 						if (projectExternalElements == null) {
 							projectExternalElements = new ArrayList(
 									getExternalElementsFrom(scriptProject,
 											new NullProgressMonitor(), 1, false));
 						}
 						buildExternalElements = projectExternalElements;
 					}
 				} else {
 					final Set dependencies = response.getExternalDependencies();
 					if (dependencies != null && !dependencies.isEmpty()
 							&& !externalElements.containsAll(dependencies)) {
 						final Set e = new HashSet(externalElements.size()
 								+ dependencies.size());
 						e.addAll(externalElements);
 						e.addAll(dependencies);
 						buildExternalElements = new ArrayList(e);
 					} else {
 						buildExternalElements = null;
 					}
 				}
 				if (response.isFullLocalBuild()
 						|| response.isFullExternalBuild()) {
 					buildTypes[k] = IScriptBuilder.FULL_BUILD;
 				}
 			}
 			builderToElements[k] = buildElementsList;
 			builderExternalElements[k] = buildExternalElements;
 			int work = buildElementsList.size();
 			if (buildExternalElements != null) {
 				work += buildExternalElements.size();
 			}
 			workEstimations[k] = Math.max(work, 1);
 		}
 	}
 
 	private List collectLocalElements() throws CoreException {
 		final IProgressMonitor nullMon = new NullProgressMonitor();
 		final Set resources = getResourcesFrom(currentProject, nullMon, 1).resources;
 		final List elements = new ArrayList();
 		locateSourceModules(nullMon, 1, resources, elements, new ArrayList());
 		return elements;
 	}
 
 	public static void removeProblemsAndTasksFor(IResource resource) {
 		try {
 			if (resource != null && resource.exists()) {
 				resource.deleteMarkers(
 						IModelMarker.SCRIPT_MODEL_PROBLEM_MARKER, false,
 						IResource.DEPTH_INFINITE);
 				resource.deleteMarkers(IModelMarker.TASK_MARKER, false,
 						IResource.DEPTH_INFINITE);
 
 				// delete managed markers
 			}
 		} catch (CoreException e) {
 			// assume there were no problems
 		}
 	}
 
 	public static void writeState(Object state, DataOutputStream out)
 			throws IOException {
 		((State) state).write(out);
 	}
 
 	public static State readState(IProject project, DataInputStream in)
 			throws IOException {
 		State state = State.read(project, in);
 		return state;
 	}
 
 
 }
