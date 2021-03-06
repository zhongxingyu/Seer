 /*  Copyright (C) 2009 Mobile Sorcery AB
 
     This program is free software; you can redistribute it and/or modify it
     under the terms of the Eclipse Public License v1.0.
 
     This program is distributed in the hope that it will be useful, but WITHOUT
     ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
     FITNESS FOR A PARTICULAR PURPOSE. See the Eclipse Public License v1.0 for
     more details.
 
     You should have received a copy of the Eclipse Public License v1.0 along
     with this program. It is also available at http://www.eclipse.org/legal/epl-v10.html
  */
 package com.mobilesorcery.sdk.core;
 
 import java.io.File;
 import java.io.IOException;
 import java.text.DateFormat;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.cdt.core.CCorePlugin;
 import org.eclipse.cdt.core.ErrorParserManager;
 import org.eclipse.cdt.core.IErrorParser;
 import org.eclipse.cdt.core.IMarkerGenerator;
 import org.eclipse.cdt.core.model.ICModelMarker;
 import org.eclipse.cdt.core.resources.ACBuilder;
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceDelta;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.SubProgressMonitor;
 import org.eclipse.debug.core.ILaunchConfiguration;
 
 import com.mobilesorcery.sdk.core.LineReader.ILineHandler;
 import com.mobilesorcery.sdk.internal.PipeTool;
 import com.mobilesorcery.sdk.internal.builder.MoSyncBuilderVisitor;
 import com.mobilesorcery.sdk.internal.builder.MoSyncIconBuilderVisitor;
 import com.mobilesorcery.sdk.internal.builder.MoSyncResourceBuilderVisitor;
 import com.mobilesorcery.sdk.internal.dependencies.CompoundDependencyProvider;
 import com.mobilesorcery.sdk.internal.dependencies.DependencyManager;
 import com.mobilesorcery.sdk.internal.dependencies.GCCDependencyProvider;
 import com.mobilesorcery.sdk.internal.dependencies.IDependencyProvider;
 import com.mobilesorcery.sdk.internal.dependencies.ProjectResourceDependencyProvider;
 import com.mobilesorcery.sdk.internal.dependencies.ResourceFileDependencyProvider;
 import com.mobilesorcery.sdk.profiles.IProfile;
 
 /**
  * The main builder. This builder extends ACBuilder for its implementation of
  * {@link IMarkerGenerator}.
  * 
  * @author Mattias Bybro
  * 
  */
 public class MoSyncBuilder extends ACBuilder {
 
 	public final static String ID = CoreMoSyncPlugin.PLUGIN_ID + ".builder";
 
 	public static final String COMPATIBLE_ID = "com.mobilesorcery.sdk.builder.builder";
 
 	public static final String CONSOLE_ID = "com.mobilesorcery.build.console";
 
 	public final static String IS_FINALIZER_BUILD = "finalizer-build";
 
 	private static final String BUILD_PREFS_PREFIX = "build.prefs:";
 
 	public final static String ADDITIONAL_INCLUDE_PATHS = BUILD_PREFS_PREFIX
 			+ "additional.include.paths";
 
 	public final static String IGNORE_DEFAULT_INCLUDE_PATHS = BUILD_PREFS_PREFIX
 			+ "ignore.default.include.paths";
 
 	public final static String ADDITIONAL_LIBRARY_PATHS = BUILD_PREFS_PREFIX
 			+ "additional.library.paths";
 
 	public final static String IGNORE_DEFAULT_LIBRARY_PATHS = BUILD_PREFS_PREFIX
 			+ "ignore.default.library.paths";
 
 	public static final String DEFAULT_LIBRARIES = BUILD_PREFS_PREFIX
 			+ "default.libraries";
 
 	public final static String ADDITIONAL_LIBRARIES = BUILD_PREFS_PREFIX
 			+ "additional.libraries";
 
 	public final static String IGNORE_DEFAULT_LIBRARIES = BUILD_PREFS_PREFIX
 			+ "ignore.default.libraries";
 
 	public static final String LIB_OUTPUT_PATH = BUILD_PREFS_PREFIX
 			+ "lib.output.path";
 
 	public static final String APP_OUTPUT_PATH = BUILD_PREFS_PREFIX
 			+ "app.output.path";
 
 	public static final String DEAD_CODE_ELIMINATION = BUILD_PREFS_PREFIX
 			+ "dead.code.elim";
 
 	public static final String EXTRA_LINK_SWITCHES = BUILD_PREFS_PREFIX
 			+ "extra.link.sw";
 
 	public static final String EXTRA_RES_SWITCHES = BUILD_PREFS_PREFIX
 			+ "extra.res.sw";
 
 	public static final String PROJECT_TYPE = BUILD_PREFS_PREFIX
 			+ "project.type";
 
 	public static final String PROJECT_TYPE_APPLICATION = "app";
 
 	public static final String PROJECT_TYPE_LIBRARY = "lib";
 
 	public static final String EXTRA_COMPILER_SWITCHES = BUILD_PREFS_PREFIX
 			+ "gcc.switches";
 
 	public static final String GCC_WARNINGS = BUILD_PREFS_PREFIX
 			+ "gcc.warnings";
 
 	public static final String MEMORY_PREFS_PREFIX = BUILD_PREFS_PREFIX
 			+ "memory.";
 
 	public static final String MEMORY_HEAPSIZE_KB = MEMORY_PREFS_PREFIX
 			+ "heap";
 
 	public static final String MEMORY_STACKSIZE_KB = MEMORY_PREFS_PREFIX
 			+ "stack";
 
 	public static final String MEMORY_DATASIZE_KB = MEMORY_PREFS_PREFIX
 			+ "data";
 
 	public static final String USE_DEBUG_RUNTIME_LIBS = BUILD_PREFS_PREFIX
 			+ "runtime.debug";
 
 	public static final int GCC_WALL = 1 << 1;
 
 	public static final int GCC_WEXTRA = 1 << 2;
 
 	public static final int GCC_WERROR = 1 << 3;
 
 	public final class GCCLineHandler implements ILineHandler {
 
 		private ErrorParserManager epm;
 
 		public GCCLineHandler(ErrorParserManager epm) {
 			this.epm = epm;
 		}
 
 		private String aggregateLine = "";
 
 		public void newLine(String line) {
 			// We need to 'aggregate' lines;
 			// whenever a line is indented,
 			// it is actually an extension
 			// of last line - it's a hack to reconcile
 			// the CDT error parser and the 'real world'
 			if (isLineIndented(line)) {
 				aggregateLine += " " + line.trim();
 			} else {
 				reportLine(aggregateLine);
 				aggregateLine = line.trim();
 			}
 		}
 
 		private boolean isLineIndented(String line) {
 			return line.startsWith(Util.fill(' ', 2));
 		}
 
 		public void reportLine(String line) {
 			try {
 				// Kind of backwards..., first we remove a \n, and
 				// now
 				// we add it, so the ErrorParserManager can remove
 				// it again...
 				if (epm != null) {
 					// Write directly to emp, ignore
 					// getoutputstream.
 					epm.write((line + '\n').getBytes());
 				}
 			} catch (IOException e) {
 				// Ignore.
 			}
 		}
 
 		public void stop(IOException e) {
 			newLine("");
 			/*
 			 * if (aggregateLine.length() > 0) { reportLine(aggregateLine); }
 			 */
 		}
 
 		public void reset() {
 			stop(null);
 			aggregateLine = "";
 		}
 	}
 
 	public MoSyncBuilder() {
 	}
 
 	public IProfile getTargetProfile() {
 		MoSyncProject project = MoSyncProject.create(getProject());
 		IProfile target = project.getTargetProfile();
 		if (target == null) {
 			target = MoSyncTool.getDefault().getDefaultTargetProfile();
 		}
 
 		return target;
 	}
 
 	public IPath getProjectPath() {
 		return getProject().getLocation();
 	}
 
 	public static IPath getOutputPath(IProject project) {
 		// TODO: Ability to build config without changing active.
 		return getOutputPath(project, getActivePropertyOwner(MoSyncProject
 				.create(project)));
 	}
 
 	public static IPath getOutputPath(IProject project,
 			IPropertyOwner buildProperties) {
 		String outputPath = buildProperties.getProperty(APP_OUTPUT_PATH);
 		if (outputPath == null) {
 			throw new IllegalArgumentException("No output path specified");
 		}
 
 		return toAbsolute(project.getLocation().append("Output"), outputPath);
 		/*
 		 * IPath output = project.getFile(new Path("Output")).getLocation();
 		 * IPath cfgPath = getRelativeConfigurationOutputPath(project); if
 		 * (cfgPath != null) { output.append(cfgPath); }
 		 * 
 		 * return output;
 		 */
 	}
 
 	/**
 	 * Converts a project-relative path to an absolute path.
 	 * 
 	 * @param path
 	 * @return An absolute path; if <code>path</code> already is an absolute
 	 *         path, then that path is used.
 	 */
 	public static IPath toAbsolute(IPath root, String pathStr) {
 		Path path = new Path(pathStr);
 		if (path.isAbsolute()) {
 			return path;
 		} else {
 			return root.append(path);
 		}
 	}
 
 	public static IPath getFinalOutputPath(IProject project,
 			IProfile targetProfile) {
 		String outputPath = getActivePropertyOwner(
 				MoSyncProject.create(project)).getProperty(APP_OUTPUT_PATH);
 		if (outputPath == null) {
 			throw new IllegalArgumentException("No output path specified");
 		}
 
 		return toAbsolute(project.getLocation().append("FinalOutput"),
 				outputPath).append(targetProfile.getVendor().getName()).append(
 				targetProfile.getName());
 	}
 
 	static IPath getCompileOutputPath(IProject project, IProfile targetProfile,
 			boolean isFinalizeBuild) {
 		return isFinalizeBuild ? getFinalOutputPath(project, targetProfile)
 				: getOutputPath(project);
 	}
 
 	public static IPath getProgramOutputPath(IProject project,
 			IProfile targetProfile, boolean isFinalizeBuild) {
 		return getCompileOutputPath(project, targetProfile, isFinalizeBuild)
 				.append("program");
 	}
 
 	public static IPath getProgramCombOutputPath(IProject project,
 			IProfile targetProfile, boolean isFinalizeBuild) {
 		return getCompileOutputPath(project, targetProfile, isFinalizeBuild)
 				.append("program.comb");
 	}
 
 	public static IPath getResourceOutputPath(IProject project,
 			IProfile targetProfile, boolean isFinalizeBuild) {
 		return getCompileOutputPath(project, targetProfile, isFinalizeBuild)
 				.append("resources");
 	}
 
 	public static IPath getPackageOutputPath(IProject project,
 			IProfile targetProfile, boolean isFinalizerBuild) {
 		if (isFinalizerBuild) {
 			return getFinalOutputPath(project, targetProfile).append("package");
 		} else {
 			return getOutputPath(project).append(
 					getAbbreviatedPlatform(targetProfile));
 		}
 	}
 
 	public static String getAbbreviatedPlatform(IProfile targetProfile) {
 		String platform = targetProfile.getPlatform();
 		String abbrPlatform = platform.substring(
 				"profiles\\runtime\\".length() + 1, platform.length());
 		return abbrPlatform;
 	}
 
 	protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
 			throws CoreException {
 		IProject project = getProject();
 		IProfile targetProfile = getTargetProfile();
 
 		if (MoSyncProject.NULL_DEPENDENCY_STRATEGY == PropertyUtil.getInteger(
 				MoSyncProject.create(project),
 				MoSyncProject.DEPENDENCY_STRATEGY,
 				MoSyncProject.GCC_DEPENDENCY_STRATEGY)) {
 			// TODO: At this point, we only have a GCC dependency strategy and a
 			// "always full build" strategy
 			kind = FULL_BUILD;
 		}
 
 		// TODO: Smarter incr for changed configs - not too difficult, actually,
 		// so
 		// do it asap.
 		if (hasConfigChanged(MoSyncProject.create(project))) {
 			kind = FULL_BUILD;
 		}
 
 		IResourceDelta[] deltas = kind == FULL_BUILD ? null
 				: getDeltas(getProject());
 		boolean doPack = kind == FULL_BUILD;
 		incrementalBuild(project, deltas, targetProfile, false, true, doPack,
 				null, true, monitor);
 
 		Set<IProject> dependencies = CoreMoSyncPlugin.getDefault()
 				.getProjectDependencyManager(ResourcesPlugin.getWorkspace())
 				.getDependenciesOf(project);
 		dependencies.add(project);
 
 		return dependencies.toArray(new IProject[dependencies.size()]);
 	}
 
 	private boolean hasErrorMarkers(IProject project) throws CoreException {
 		return hasErrorMarkers(project, IResource.DEPTH_INFINITE);
 	}
 
 	private boolean hasErrorMarkers(IProject project, int depth)
 			throws CoreException {
 		return project.findMaxProblemSeverity(
 				ICModelMarker.C_MODEL_PROBLEM_MARKER, true, depth) == IMarker.SEVERITY_ERROR;
 	}
 
 	private boolean hasConfigChanged(MoSyncProject project) {
 		// TODO: Don't store transient build vars like this.
 		String lastBuildConfigKey = "last.build.config";
 		String oldConfig = project.getProperty(lastBuildConfigKey);
 		if ("".equals(oldConfig)) {
 			oldConfig = null;
 		}
 		IBuildConfiguration activeConfig = project
 				.getActiveBuildConfiguration();
 		String activeConfigId = activeConfig == null ? null : activeConfig
 				.getId();
 		boolean changedConfig = !NameSpacePropertyOwner.equals(oldConfig,
 				activeConfigId);
 		project.initProperty(lastBuildConfigKey, activeConfigId,
 				MoSyncProject.LOCAL_PROPERTY);
 		return changedConfig;
 	}
 
 	/**
 	 * Returns all deltas for <code>project</code>; including any projects that
 	 * <code>project</code> depends on.
 	 * 
 	 * @param project
 	 * @return
 	 */
 	private IResourceDelta[] getDeltas(IProject project) {
 		Set<IProject> projectDependencies = CoreMoSyncPlugin.getDefault()
 				.getProjectDependencyManager().getDependenciesOf(project);
 		projectDependencies.add(project);
 		ArrayList<IResourceDelta> result = new ArrayList<IResourceDelta>();
 		for (IProject projectDependency : projectDependencies) {
 			IResourceDelta delta = getDelta(projectDependency);
 			if (delta != null) {
 				result.add(delta);
 			}
 		}
 
 		return result.toArray(new IResourceDelta[result.size()]);
 	}
 
 	public void clean(IProgressMonitor monitor) {
 		forgetLastBuiltState();
 		IProject project = getProject();
 		IProfile targetProfile = getTargetProfile();
 		clean(project, targetProfile, false, monitor);
 		MoSyncProject.create(project).getDependencyManager().clear();
 	}
 
 	public void clean(IProject project, IProfile targetProfile,
 			boolean isFinalizerBuild, IProgressMonitor monitor) {
 		IPath output = getCompileOutputPath(project, targetProfile,
 				isFinalizerBuild);
 		File outputFile = output.toFile();
 
 		IProcessConsole console = CoreMoSyncPlugin.getDefault().createConsole(
 				CONSOLE_ID);
 		prepareConsole(console);
 
 		console.addMessage(MessageFormat.format("Cleaning project {0}", project
 				.getName()));
 		Util.deleteFiles(getPackageOutputPath(project, targetProfile,
 				isFinalizerBuild).toFile(), null, 512, monitor);
 		Util.deleteFiles(getProgramOutputPath(project, targetProfile,
 				isFinalizerBuild).toFile(), null, 1, monitor);
 		Util.deleteFiles(getProgramCombOutputPath(project, targetProfile,
 				isFinalizerBuild).toFile(), null, 1, monitor);
 		Util.deleteFiles(getResourceOutputPath(project, targetProfile,
 				isFinalizerBuild).toFile(), null, 1, monitor);
 		Util
 				.deleteFiles(outputFile, Util.getExtensionFilter("s"), 512,
 						monitor);
 	}
 
 	public IBuildResult fullBuild(IProject project, IProfile targetProfile,
 			boolean isFinalizerBuild, boolean doClean, IProgressMonitor monitor)
 			throws CoreException {
 		return fullBuild(project, targetProfile, isFinalizerBuild, doClean,
 				true, true, null, true, monitor);
 	}
 
 	public IBuildResult fullBuild(IProject project, IProfile targetProfile,
 			boolean isFinalizerBuild, boolean doClean,
 			boolean doBuildResources, boolean doPack,
 			IFilter<IResource> resourceFilter, boolean doLink,
 			IProgressMonitor monitor) throws CoreException {
 		try {
 			// TODO: Allow for setting build config explicitly!
 			monitor.beginTask(MessageFormat.format("Full build of {0}", project
 					.getName()), 8);
 			if (doClean) {
 				clean(project, targetProfile, isFinalizerBuild,
 						new SubProgressMonitor(monitor, 1));
 			} else {
 				monitor.worked(1);
 			}
 			return incrementalBuild(project, null, targetProfile,
 					isFinalizerBuild, doBuildResources, doPack, resourceFilter,
 					doLink, monitor);
 		} finally {
 			monitor.done();
 		}
 	}
 
 	// TODO: Refactor, this is becoming a jack-of-all-trades method, esp. now
 	// with the partial builds as well. Maybe need a new class like 'build
 	// parameters' to avoid all setters and this huge method signature...
 	IBuildResult incrementalBuild(IProject project, IResourceDelta[] deltas,
 			IProfile targetProfile, boolean isFinalizerBuild,
 			boolean doBuildResources, boolean doPack,
 			IFilter<IResource> resourceFilter, boolean doLink,
 			IProgressMonitor monitor) throws CoreException {
 
 		if (CoreMoSyncPlugin.getDefault().isDebugging()) {
 			CoreMoSyncPlugin.trace("Building project {0}", project);
 		}
 
 		ErrorParserManager epm = createErrorParserManager(project);
 
 		BuildResult buildResult = new BuildResult(project);
 
 		try {
 			monitor.beginTask(MessageFormat.format("Building {0}", project), 4);
 
 			MoSyncProject mosyncProject = MoSyncProject.create(project);
 
 			IPropertyOwner buildProperties = getActivePropertyOwner(mosyncProject);
 
 			// TODO: No longer necessary, since dependency mgr?
 			// if (deltas != null && !hasDeltaThatAffectsBuild(mosyncProject,
 			// deltas)) {
 			// monitor.done();
 			// return buildResult;
 			// }
 
 			monitor.setTaskName("Clearing old problem markers");
 
 			// And we only remove things that are on the project.
 			boolean hadSevereBuildErrors = hasErrorMarkers(project,
 					IResource.DEPTH_ZERO);
 
 			if (hadSevereBuildErrors) {
 				// Build all
 				deltas = null;
 			}
 
 			if (deltas == null) {
 				mosyncProject.getDependencyManager().clear();
 			}
 
 			boolean isLib = PROJECT_TYPE_LIBRARY.equals(mosyncProject
 					.getProperty(PROJECT_TYPE));
 
 			GCCLineHandler linehandler = new GCCLineHandler(epm);
 
 			IProcessConsole console = CoreMoSyncPlugin.getDefault()
 					.createConsole(CONSOLE_ID);
 			prepareConsole(console);
 
 			if (!MoSyncTool.getDefault().isValid()) {
 				String error = MoSyncTool.getDefault().validate();
 				console.addMessage(MessageFormat.format(
 						"MoSync Tool not properly initialized: {0}", error));
 				console
 						.addMessage("- go to Window > Preferences > MoSync Tool to set the MoSync home directory");
 			}
 
 			if (doLink && !doBuildResources) {
 				throw new CoreException(
 						new Status(IStatus.ERROR, CoreMoSyncPlugin.PLUGIN_ID,
 								"If resource building is suppressed, then linking should also be."));
 			}
 
 			console.addMessage("Build started at "
 					+ DateFormat.getDateTimeInstance(DateFormat.SHORT,
 							DateFormat.LONG).format(
 							Calendar.getInstance().getTime()));
 
 			console.addMessage(MessageFormat.format(
 					"Building project {0} for profile {1}", project.getName(),
 					targetProfile));
 			if (mosyncProject.areBuildConfigurationsSupported()) {
 				IBuildConfiguration activeBuildConfig = mosyncProject
 						.getActiveBuildConfiguration();
 				console.addMessage(MessageFormat
 						.format("Building configuration {0}", activeBuildConfig
 								.getId()));
 			}
 
 			MoSyncBuilderVisitor compilerVisitor = new MoSyncBuilderVisitor();
 
 			PipeTool pipeTool = new PipeTool();
 			pipeTool.setAppCode(PipeTool.generateAppCode());
 			pipeTool.setProject(project);
 			pipeTool.setConsole(console);
 			pipeTool.setLineHandler(linehandler);
 			pipeTool.setArguments(buildProperties);
 
 			MoSyncResourceBuilderVisitor resourceVisitor = new MoSyncResourceBuilderVisitor();
 
 			IPath resource = getResourceOutputPath(project, targetProfile,
 					isFinalizerBuild);
 			resourceVisitor.setProject(project);
 			resourceVisitor.setBuildProperties(buildProperties);
 			resourceVisitor.setPipeTool(pipeTool);
 			resourceVisitor.setOutputFile(resource);
 			resourceVisitor.setDependencyProvider(compilerVisitor
 					.getDependencyProvider());
 			resourceVisitor.setDelta(deltas);
 			resourceVisitor.setResourceFilter(resourceFilter);
 
 			if (doBuildResources) {
 				// First we build the resources...
 				monitor.setTaskName("Assembling resources");
 				resourceVisitor.incrementalCompile(monitor, mosyncProject
 						.getDependencyManager());
 			}
 
 			// ...and then the actual code is compiled
 			IPath compileDir = getCompileOutputPath(project, targetProfile,
 					isFinalizerBuild);
 			monitor.setTaskName(MessageFormat.format("Compiling for {0}",
 					targetProfile));
 
 			compilerVisitor.setProfile(targetProfile);
 			compilerVisitor.setProject(project);
 			compilerVisitor.setBuildProperties(buildProperties);
 			compilerVisitor.setConsole(console);
 			compilerVisitor.setExtraCompilerSwitches(buildProperties
 					.getProperty(EXTRA_COMPILER_SWITCHES));
 			Integer gccWarnings = PropertyUtil.getInteger(buildProperties,
 					GCC_WARNINGS);
 			compilerVisitor.setGCCWarnings(gccWarnings == null ? 0
 					: gccWarnings.intValue());
 			compilerVisitor.setOutputPath(compileDir);
 			compilerVisitor.setLineHandler(linehandler);
 			compilerVisitor.setBuildResult(buildResult);
 			compilerVisitor.setDelta(deltas);
 			compilerVisitor.setResourceFilter(resourceFilter);
 			compilerVisitor.incrementalCompile(monitor, mosyncProject
 					.getDependencyManager());
 
 			IResource[] allAffectedResources = compilerVisitor
 					.getAllAffectedResources();
 			Set<IProject> projectDependencies = computeProjectDependencies(
 					monitor, mosyncProject, allAffectedResources);
 			DependencyManager<IProject> projectDependencyMgr = CoreMoSyncPlugin
 					.getDefault().getProjectDependencyManager(
 							ResourcesPlugin.getWorkspace());
 			projectDependencyMgr.setDependencies(project, projectDependencies);
 			monitor.worked(1);
 
 			if (monitor.isCanceled()) {
 				return buildResult;
 			}
 
 			String[] objectFiles = compilerVisitor
 					.getObjectFilesForProject(project);
 
 			int ec = compilerVisitor.getErrorCount();
 
 			monitor.setTaskName(MessageFormat.format("Packaging for {0}",
 					targetProfile));
 
 			IPath program = getProgramOutputPath(project, targetProfile,
 					isFinalizerBuild);
 
 			IPath programComb = getProgramCombOutputPath(project,
 					targetProfile, isFinalizerBuild);
 
 			// We'll relink if we had some non-empty delta; we'll use
 			// the compiler visitor to tell us that. We'll also relink
 			// if some library that our project depends on is changed.
 			boolean requiresLinking = allAffectedResources.length > 0;
 			if (!requiresLinking) {
 				long librariesTouched = mosyncProject.getLibraryLookup(
 						buildProperties).getLastTouched();
 				long programCombTouched = programComb.toFile().lastModified();
 				boolean librariesNewer = librariesTouched > programCombTouched;
 				requiresLinking = librariesNewer;
 
 				if (librariesNewer && programCombTouched > 0) {
 					console
 							.addMessage("Libraries have changed, will require re-linking");
 				}
 			}
 
 			requiresLinking &= doLink;
 
 			if (ec == 0 && requiresLinking) {
 				boolean elim = !isLib
 						&& PropertyUtil.getBoolean(buildProperties,
 								DEAD_CODE_ELIMINATION);
 				pipeTool.setMode(isLib ? PipeTool.BUILD_LIB_MODE
 						: PipeTool.BUILD_C_MODE);
 				pipeTool.setInputFiles(objectFiles);
 				IPath libraryOutput = computeLibraryOutput(mosyncProject,
 						buildProperties);
 				pipeTool.setOutputFile(isLib ? libraryOutput : program);
 				pipeTool.setLibraryPaths(getLibraryPaths(project,
 						buildProperties));
 				pipeTool.setLibraries(getLibraries(buildProperties));
 				pipeTool.setDeadCodeElimination(elim);
 				pipeTool.setCollectStabs(true);
 
 				String[] extraLinkerSwitches = PropertyUtil.getStrings(
 						buildProperties, EXTRA_LINK_SWITCHES);
 				pipeTool.setExtraSwitches(extraLinkerSwitches);
 
 				pipeTool.run();
 
 				if (elim) {
 					PipeTool elimPipeTool = new PipeTool();
 					elimPipeTool.setProject(project);
 					elimPipeTool.setLineHandler(linehandler);
 					elimPipeTool.setNoVerify(true);
 					elimPipeTool.setGenerateSLD(false);
 					elimPipeTool.setMode(PipeTool.BUILD_C_MODE);
 					elimPipeTool.setOutputFile(program);
 					elimPipeTool.setConsole(console);
 					elimPipeTool.setExtraSwitches(extraLinkerSwitches);
 					File rebuildFile = new File(elimPipeTool.getExecDir(),
 							"rebuild.s");
 					elimPipeTool.setInputFiles(new String[] { rebuildFile
 							.getAbsolutePath() });
 					elimPipeTool.run();
 				}
 
 				if (!isLib) {
 					// Create "comb" file - program + resources in one. We'll
 					// always
 					// make one, even though no resources present.
 					ArrayList<File> parts = new ArrayList<File>();
 					parts.add(program.toFile());
 					if (resourceVisitor.getResourceFiles().length > 0
 							&& program.toFile().exists()
 							&& resource.toFile().exists()) {
 						parts.add(resource.toFile());
 					}
 
 					if (parts.size() > 1) {
 						console.addMessage(MessageFormat.format(
 								"Combining {0} into one large file, {1}", Util
 										.join(parts.toArray(), ", "),
 								programComb.toFile()));
 					}
 					Util.mergeFiles(new SubProgressMonitor(monitor, 1), parts
 							.toArray(new File[parts.size()]), programComb
 							.toFile());
 				}
 			}
 
 			// And the icon, finally...
 			MoSyncIconBuilderVisitor iconVisitor = new MoSyncIconBuilderVisitor();
 			iconVisitor.setProject(project);
 			iconVisitor.setBuildProperties(buildProperties);
 			iconVisitor.setConsole(console);
 			iconVisitor.setDelta(deltas);
 			iconVisitor.setResourceFilter(resourceFilter);
 			iconVisitor.incrementalCompile(monitor, mosyncProject
 					.getDependencyManager());
 
 			if (doPack && !isLib) {
 				IPackager packager = targetProfile.getPackager();
 				packager.setParameter(IS_FINALIZER_BUILD, Boolean
 						.toString(isFinalizerBuild));
 				packager.setParameter(USE_DEBUG_RUNTIME_LIBS, Boolean
 						.toString(PropertyUtil.getBoolean(mosyncProject
 								.getPropertyOwner(), USE_DEBUG_RUNTIME_LIBS)));
 
 				if (ec == 0) {
 					packager.createPackage(mosyncProject, targetProfile,
 							buildResult);
 
 					if (buildResult.getBuildResult() == null
 							|| !buildResult.getBuildResult().exists()) {
 						throw new IOException(MessageFormat.format(
 								"Failed to create package for {0}",
 								targetProfile));
 					}
 				}
 			}
 
 			monitor.worked(1);
 
 			console.addMessage("Build finished at "
 					+ DateFormat.getDateTimeInstance(DateFormat.SHORT,
 							DateFormat.LONG).format(
 							Calendar.getInstance().getTime()));
 
 			project.refreshLocal(IProject.DEPTH_INFINITE,
 					new SubProgressMonitor(monitor, 1));
 
 			buildResult.setSuccess(true);
 			return buildResult;
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new CoreException(new Status(IStatus.ERROR,
 					CoreMoSyncPlugin.PLUGIN_ID, e.getMessage(), e));
 		} finally {
 			if (!isFinalizerBuild) {
 				epm.reportProblems();
 			}
 			if (!monitor.isCanceled() && !buildResult.success()
 					&& !hasErrorMarkers(project)) {
 				addBuildFailedMarker(project);
 			} else if (buildResult.success()) {
 				clearCMarkers(project);
 			}
 		}
 	}
 
 	private void addBuildFailedMarker(IProject project) throws CoreException {
 		// Ensure there is a build failed marker if the build failed; will cause
 		// all failed builds to
 		// be completely rebuilt later.
 		IMarker marker = project
 				.createMarker(ICModelMarker.C_MODEL_PROBLEM_MARKER);
 		marker.setAttribute(IMarker.MESSAGE, "Last build failed");
 		marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
 	}
 
 	private static IPropertyOwner getActivePropertyOwner(
 			MoSyncProject mosyncProject) {
 		IBuildConfiguration activeBuildConfig = mosyncProject
 				.getActiveBuildConfiguration();
 		if (mosyncProject.areBuildConfigurationsSupported()
 				&& activeBuildConfig == null) {
 			throw new IllegalStateException("No configuration is active");
 		}
 
 		return mosyncProject.areBuildConfigurationsSupported() ? activeBuildConfig
 				.getProperties()
 				: mosyncProject;
 	}
 
 	/**
 	 * 'Prepares' a console for printing -- first checks whether the CDT
 	 * preference is set to clear the console, and if so, clears it.
 	 * 
 	 * @param console
 	 */
 	private void prepareConsole(IProcessConsole console) {
 		if (CoreMoSyncPlugin.isHeadless()) {
 			// No need to prepare anything.
 			return;
 		}
 
 		console.prepare();
 	}
 
 	private Set<IProject> computeProjectDependencies(IProgressMonitor monitor,
 			MoSyncProject mosyncProject, IResource[] allAffectedResources) {
 		IProject project = mosyncProject.getWrappedProject();
 		monitor.setTaskName(MessageFormat.format(
 				"Computing project dependencies for {0}", project.getName()));
 		DependencyManager<IProject> projectDependencies = CoreMoSyncPlugin
 				.getDefault().getProjectDependencyManager(
 						ResourcesPlugin.getWorkspace());
 		projectDependencies.clearDependencies(project);
 		HashSet<IProject> allProjectDependencies = new HashSet<IProject>();
 		Set<IResource> dependencies = mosyncProject.getDependencyManager()
 				.getDependenciesOf(Arrays.asList(allAffectedResources));
 		for (IResource resourceDependency : dependencies) {
 			if (resourceDependency.getType() != IResource.ROOT) {
 				allProjectDependencies.add(resourceDependency.getProject());
 			}
 		}
 
 		// No deps on self
 		allProjectDependencies.remove(project);
 
 		if (CoreMoSyncPlugin.getDefault().isDebugging()) {
 			CoreMoSyncPlugin
 					.trace(MessageFormat
 							.format(
 									"Computed project dependencies. Project {0} depends on {1}",
 									project.getName(), allProjectDependencies));
 		}
 		return allProjectDependencies;
 	}
 
 	/*
 	 * private boolean hasDeltaThatAffectsBuild(MoSyncProject project,
 	 * IResourceDelta[] deltas) throws CoreException { for (int i = 0; i <
 	 * deltas.length; i++) { if (hasDeltaThatAffectsBuild(project, deltas[i])) {
 	 * return true; } }
 	 * 
 	 * return false; }
 	 */
 
 	/*
 	 * private boolean hasDeltaThatAffectsBuild(final MoSyncProject project,
 	 * IResourceDelta delta) throws CoreException { // A delta must contain at
 	 * least one c source/header file // or be a dependency of a resource file
 	 * 
 	 * final boolean[] hasSourceDelta = new boolean[1]; hasSourceDelta[0] =
 	 * false;
 	 * 
 	 * delta.accept(new IResourceDeltaVisitor() { public boolean
 	 * visit(IResourceDelta delta) throws CoreException { IResource resource =
 	 * delta.getResource(); if
 	 * (MoSyncBuilderVisitor.doesResourceAffectBuild(resource)) {
 	 * hasSourceDelta[0] = true; } else { hasSourceDelta[0] =
 	 * project.getDependencyManager().getReverseDependenciesOf(resource,
 	 * 1).contains(); }
 	 * 
 	 * return !hasSourceDelta[0]; } });
 	 * 
 	 * return hasSourceDelta[0]; }
 	 */
 
 	public static IPath computeLibraryOutput(MoSyncProject mosyncProject,
 			IPropertyOwner buildProperties) {
 		String outputPath = buildProperties.getProperty(LIB_OUTPUT_PATH);
 		if (outputPath == null) {
 			throw new IllegalArgumentException("Library path is not specified");
 		}
 
 		return toAbsolute(mosyncProject.getWrappedProject().getLocation()
 				.append("Output"), outputPath);
 		/*
 		 * String libraryOutputSetting =
 		 * buildProperties.getProperty(MoSyncBuilder.LIB_OUTPUT_PATH); IPath
 		 * libraryOutput = (libraryOutputSetting == null ||
 		 * libraryOutputSetting.length() == 0) ?
 		 * computeDefaultLibraryOutput(mosyncProject) : new
 		 * Path(libraryOutputSetting); return libraryOutput;
 		 */
 	}
 
 	/**
 	 * Clears all C markers of the given resource
 	 * 
 	 * @param resource
 	 * @param severityError
 	 * @return true if at least one marker was cleared, false otherwise
 	 * @throws CoreException
 	 * @throws CoreException
 	 */
 	public static boolean clearCMarkers(IResource resource)
 			throws CoreException {
 		return clearCMarkers(resource, IMarker.SEVERITY_INFO);
 	}
 
 	public static boolean clearCMarkers(IResource resource, int severity)
 			throws CoreException {
 		if (!resource.exists()) {
 			return false;
 		}
 
 		IMarker[] markers = resource.findMarkers(
 				ICModelMarker.C_MODEL_PROBLEM_MARKER, true,
 				IResource.DEPTH_INFINITE);
 		IMarker[] toBeRemoved = markers;
 
 		if (severity != IMarker.SEVERITY_INFO) {
 			ArrayList<IMarker> toBeRemovedList = new ArrayList<IMarker>();
 			for (int i = 0; i < markers.length; i++) {
 				Object severityOfMarker = markers[i]
 						.getAttribute(IMarker.SEVERITY);
 				if (severityOfMarker instanceof Integer) {
 					if (((Integer) severityOfMarker) >= severity) {
 						toBeRemovedList.add(markers[i]);
 					}
 				}
 			}
 			toBeRemoved = toBeRemovedList.toArray(new IMarker[0]);
 		}
 
 		resource.getWorkspace().deleteMarkers(toBeRemoved);
 		return toBeRemoved.length > 0;
 	}
 
 	private ErrorParserManager createErrorParserManager(IProject project) {
 		String epId = CCorePlugin.PLUGIN_ID + ".GCCErrorParser";
 		IErrorParser[] gccEp = CCorePlugin.getDefault().getErrorParser(epId);
 		if (gccEp.length < 1) {
 			return null; // No error parser for gcc available.
 		} else {
 			return new ErrorParserManager(project, this, new String[] { epId });
 		}
 	}
 
 	public static IPath[] getIncludePaths(MoSyncProject project) {
 		return getIncludePaths(project, getActivePropertyOwner(project));
 	}
 
 	public static IPath[] getIncludePaths(MoSyncProject project,
 			IPropertyOwner buildProperties) {
 		ArrayList<IPath> result = new ArrayList<IPath>();
 		if (!PropertyUtil.getBoolean(project, IGNORE_DEFAULT_INCLUDE_PATHS)) {
 			result.addAll(Arrays.asList(MoSyncTool.getDefault()
 					.getMoSyncDefaultIncludes()));
			result.addAll(Arrays.asList(getProfileIncludes(project
					.getTargetProfile())));
 			// result.add(getOutputPath(project.getWrappedProject(),
 			// buildProperties).removeTrailingSeparator());
 		}
 
 		IPath[] additionalIncludePaths = PropertyUtil.getPaths(buildProperties,
 				ADDITIONAL_INCLUDE_PATHS);
 		for (int i = 0; i < additionalIncludePaths.length; i++) {
 			if (additionalIncludePaths[i].getDevice() == null) {
 				// Then might be project relative path.
 				IPath relativeAdditionalIncludePath = project
 						.getWrappedProject().getLocation().append(
 								additionalIncludePaths[i]);
 				if (relativeAdditionalIncludePath.toFile().exists()) {
 					additionalIncludePaths[i] = relativeAdditionalIncludePath;
 				}
 			}
 		}
 
 		if (additionalIncludePaths != null) {
 			result.addAll(Arrays.asList(additionalIncludePaths));
 		}
 
 		return result.toArray(new IPath[0]);
 	}
 
	private static IPath[] getProfileIncludes(IProfile profile) {
 		IPath profilePath = MoSyncTool.getDefault().getProfilePath(profile);
 		return profilePath == null ? new IPath[0] : new IPath[] { profilePath };
 	}
 
 	public static IPath[] getLibraryPaths(IProject project,
 			IPropertyOwner buildProperties) {
 		ArrayList<IPath> result = new ArrayList<IPath>();
 		if (!PropertyUtil.getBoolean(buildProperties,
 				IGNORE_DEFAULT_LIBRARY_PATHS)) {
 			result.addAll(Arrays.asList(MoSyncTool.getDefault()
 					.getMoSyncDefaultLibraryPaths()));
 		}
 
 		IPath[] additionalLibraryPaths = PropertyUtil.getPaths(buildProperties,
 				ADDITIONAL_LIBRARY_PATHS);
 
 		if (additionalLibraryPaths != null) {
 			result.addAll(Arrays.asList(additionalLibraryPaths));
 		}
 
 		return result.toArray(new IPath[0]);
 	}
 
 	public static IPath[] getLibraries(MoSyncProject project) {
 		return getLibraries(getActivePropertyOwner(project));
 	}
 
 	public static IPath[] getLibraries(IPropertyOwner buildProperties) {
 		// Ehm, I think I've seen this code elsewhere...
 		ArrayList<IPath> result = new ArrayList<IPath>();
 		if (!PropertyUtil.getBoolean(buildProperties, IGNORE_DEFAULT_LIBRARIES)) {
 			result.addAll(Arrays.asList(PropertyUtil.getPaths(buildProperties,
 					DEFAULT_LIBRARIES)));
 		}
 
 		IPath[] additionalLibraries = PropertyUtil.getPaths(buildProperties,
 				ADDITIONAL_LIBRARIES);
 
 		if (additionalLibraries != null) {
 			result.addAll(Arrays.asList(additionalLibraries));
 		}
 
 		return result.toArray(new IPath[0]);
 	}
 
 	private IDependencyProvider<IResource> createDependencyProvider(
 			MoSyncBuilderVisitor mbv) {
 		return new CompoundDependencyProvider<IResource>(
 				new GCCDependencyProvider(mbv),
 				new ProjectResourceDependencyProvider(),
 				new ResourceFileDependencyProvider());
 	}
 
 	public static boolean isBuilderPreference(String preferenceKey) {
 		return preferenceKey != null
 				&& preferenceKey.startsWith(BUILD_PREFS_PREFIX);
 	}
 
 	public static IProject getProject(ILaunchConfiguration launchConfig)
 			throws CoreException {
 		String projectName = launchConfig.getAttribute(
 				ILaunchConstants.PROJECT, "");
 		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(
 				projectName);
 		if (!project.exists()) {
 			throw new CoreException(new Status(IStatus.ERROR,
 					CoreMoSyncPlugin.PLUGIN_ID, MessageFormat.format(
 							"Cannot launch: Project {0} does not exist",
 							project.getName())));
 		}
 
 		return project;
 	}
 
 }
