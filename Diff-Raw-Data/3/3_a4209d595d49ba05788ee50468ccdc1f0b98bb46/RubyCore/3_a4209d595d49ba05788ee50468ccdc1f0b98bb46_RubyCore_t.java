 /*
  * Author: 
  *
  * Copyright (c) 2003-2005 RubyPeople.
  *
  * This file is part of the Ruby Development Tools (RDT) plugin for eclipse. 
  * RDT is subject to the "Common Public License (CPL) v 1.0". You may not use
  * RDT except in compliance with the License. For further information see 
  * org.rubypeople.rdt/rdt.license.
  */
 package org.rubypeople.rdt.core;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IProjectDescription;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IWorkspace;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.IWorkspaceRunnable;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.Plugin;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.ISchedulingRule;
 import org.eclipse.core.runtime.preferences.DefaultScope;
 import org.eclipse.core.runtime.preferences.IEclipsePreferences;
 import org.eclipse.core.runtime.preferences.InstanceScope;
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleContext;
 import org.rubypeople.rdt.internal.core.BatchOperation;
 import org.rubypeople.rdt.internal.core.DefaultWorkingCopyOwner;
 import org.rubypeople.rdt.internal.core.RubyModel;
 import org.rubypeople.rdt.internal.core.RubyModelManager;
 import org.rubypeople.rdt.internal.core.RubyProject;
 import org.rubypeople.rdt.internal.core.RubyScript;
 import org.rubypeople.rdt.internal.core.SymbolIndexResourceChangeListener;
 import org.rubypeople.rdt.internal.core.builder.IndexUpdater;
 import org.rubypeople.rdt.internal.core.builder.MassIndexUpdater;
 import org.rubypeople.rdt.internal.core.builder.RubyBuilder;
 import org.rubypeople.rdt.internal.core.parser.RubyParser;
 import org.rubypeople.rdt.internal.core.symbols.SymbolIndex;
 
 public class RubyCore extends Plugin {
 
 	private static RubyCore RUBY_CORE_PLUGIN = null;
 
 	public final static String PLUGIN_ID = "org.rubypeople.rdt.core";
 
     private static final String RUBY_PARSER_DEBUG_OPTION = RubyCore.PLUGIN_ID + "/rubyparser";
     private static final String MODEL_MANAGER_VERBOSE_OPTION = RubyCore.PLUGIN_ID + "/modelmanager";
     private static final String SYMBOL_INDEX_VERBOSE_OPTION = RubyCore.PLUGIN_ID + "/symbolIndex";
     private static final String BUILDER_VERBOSE_OPTION = RubyCore.PLUGIN_ID + "/rubyBuilder";
     
     
 	public final static String NATURE_ID = PLUGIN_ID + ".rubynature";
 
 	/**
 	 * New Preferences API
 	 * 
 	 * @since 3.1
 	 */
 	public static final IEclipsePreferences[] preferencesLookup = new IEclipsePreferences[2];
 	static final int PREF_INSTANCE = 0;
 	static final int PREF_DEFAULT = 1;
 
 	/**
 	 * Default task tag
 	 * 
 	 * @since 3.0
 	 */
 	public static final String DEFAULT_TASK_TAGS = "TODO,FIXME,XXX"; //$NON-NLS-1$
 
 	/**
 	 * The identifier for the Ruby builder
 	 * (value <code>"org.rubypeople.rdt.core.rubybuilder"</code>).
 	 */
 	public static final String BUILDER_ID = PLUGIN_ID + ".rubybuilder" ; //$NON-NLS-1$
 
 	/**
 	 * Default task priority
 	 * 
 	 * @since 3.0
 	 */
 	public static final String DEFAULT_TASK_PRIORITIES = "NORMAL,HIGH,NORMAL"; //$NON-NLS-1$
 	/**
 	 * Possible configurable option ID.
 	 * 
 	 * @see #getDefaultOptions()
 	 * @since 2.1
 	 */
 	public static final String COMPILER_TASK_PRIORITIES = PLUGIN_ID + ".compiler.taskPriorities"; //$NON-NLS-1$
 	/**
 	 * Possible configurable option value for COMPILER_TASK_PRIORITIES.
 	 * 
 	 * @see #getDefaultOptions()
 	 * @since 2.1
 	 */
 	public static final String COMPILER_TASK_PRIORITY_HIGH = "HIGH"; //$NON-NLS-1$
 	/**
 	 * Possible configurable option value for COMPILER_TASK_PRIORITIES.
 	 * 
 	 * @see #getDefaultOptions()
 	 * @since 2.1
 	 */
 	public static final String COMPILER_TASK_PRIORITY_LOW = "LOW"; //$NON-NLS-1$
 	/**
 	 * Possible configurable option value for COMPILER_TASK_PRIORITIES.
 	 * 
 	 * @see #getDefaultOptions()
 	 * @since 2.1
 	 */
 	public static final String COMPILER_TASK_PRIORITY_NORMAL = "NORMAL"; //$NON-NLS-1$
 
 	/**
 	 * Possible configurable option ID.
 	 * 
 	 * @see #getDefaultOptions()
 	 * @since 2.1
 	 */
 	public static final String COMPILER_TASK_TAGS = PLUGIN_ID + ".compiler.taskTags"; //$NON-NLS-1$
 	/**
 	 * Possible configurable option ID.
 	 * 
 	 * @see #getDefaultOptions()
 	 * @since 3.0
 	 */
 	public static final String COMPILER_TASK_CASE_SENSITIVE = PLUGIN_ID + ".compiler.taskCaseSensitive"; //$NON-NLS-1$	
 	/**
 	 * Possible configurable option value.
 	 * 
 	 * @see #getDefaultOptions()
 	 * @since 2.0
 	 */
 	public static final String ENABLED = "enabled"; //$NON-NLS-1$
 	/**
 	 * Possible configurable option value.
 	 * 
 	 * @see #getDefaultOptions()
 	 * @since 2.0
 	 */
 	public static final String DISABLED = "disabled"; //$NON-NLS-1$
 
     private SymbolIndex symbolIndex;
     
 	public RubyCore() {
 		super();
 		RUBY_CORE_PLUGIN = this;
         symbolIndex = new SymbolIndex();
 	}
 
 	/**
 	 * Returns the single instance of the Ruby core plug-in runtime class.
 	 * 
 	 * @return the single instance of the Ruby core plug-in runtime class
 	 */
 	public static RubyCore getPlugin() {
 		return RUBY_CORE_PLUGIN;
 	}
 
 	public static IWorkspace getWorkspace() {
 		return ResourcesPlugin.getWorkspace();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.core.runtime.Plugin#start(org.osgi.framework.BundleContext)
 	 */
 	public void start(BundleContext context) throws Exception {
 		super.start(context);
 		// init preferences
 		initializeDefaultPreferences();
 
 		// Listen to instance preferences node removal from parent in order to
 		// refresh stored one
 		IEclipsePreferences.INodeChangeListener listener = new IEclipsePreferences.INodeChangeListener() {
 
 			public void added(IEclipsePreferences.NodeChangeEvent event) {
 			// do nothing
 			}
 
 			public void removed(IEclipsePreferences.NodeChangeEvent event) {
 				if (event.getChild() == preferencesLookup[PREF_INSTANCE]) {
 					preferencesLookup[PREF_INSTANCE] = new InstanceScope().getNode(PLUGIN_ID);
 				}
 			}
 		};
 		((IEclipsePreferences) getInstancePreferences().parent()).addNodeChangeListener(listener);
 
 		// Listen to default preferences node removal from parent in order to
 		// refresh stored one
 		listener = new IEclipsePreferences.INodeChangeListener() {
 
 			public void added(IEclipsePreferences.NodeChangeEvent event) {
 			// do nothing
 			}
 
 			public void removed(IEclipsePreferences.NodeChangeEvent event) {
 				if (event.getChild() == preferencesLookup[PREF_DEFAULT]) {
 					preferencesLookup[PREF_DEFAULT] = new DefaultScope().getNode(PLUGIN_ID);
 				}
 			}
 		};
 		((IEclipsePreferences) getDefaultPreferences().parent()).addNodeChangeListener(listener);
 
         RubyParser.setDebugging(isDebugOptionTrue(RUBY_PARSER_DEBUG_OPTION));
         RubyModelManager.setVerbose(isDebugOptionTrue(MODEL_MANAGER_VERBOSE_OPTION));
         SymbolIndex.setVerbose(isDebugOptionTrue(SYMBOL_INDEX_VERBOSE_OPTION));
         RubyBuilder.setVerbose(isDebugOptionTrue(BUILDER_VERBOSE_OPTION));
 
         SymbolIndexResourceChangeListener.register(symbolIndex);
         IndexUpdater indexUpdater = new IndexUpdater(symbolIndex);
         MassIndexUpdater massUpdater = new MassIndexUpdater(indexUpdater);
        // TODO: move away from start method: long running, syntax errors can occur 
        //massUpdater.updateProjects(Arrays.asList(getRubyProjects()));
         
 	}
 
     private boolean isDebugOptionTrue(String option) {
         String optionText = Platform.getDebugOption(option);
         return optionText == null 
             ? false : optionText.equalsIgnoreCase("true");
     }
 
     public static boolean upgradeOldProjects() throws CoreException {
         boolean projectUpgraded = false;
         IProject[] projects = RubyCore.getRubyProjects();
         for (int i = 0; i < projects.length; i++) {
             if (upgradeOldProject(projects[i])) {
                 projectUpgraded = true;
             }
         }
         return projectUpgraded;
     }
 
     public static boolean upgradeOldProject(IProject project) throws CoreException {
         RubyModelManager rubyModelManager = RubyModelManager.getRubyModelManager();
         RubyModel rubyModel = rubyModelManager.getRubyModel();
         IRubyProject rubyProject = rubyModel.getRubyProject(project);
         if (rubyProject != null)
             return rubyProject.upgrade();
         return false;
     }
 
 	/*
 	 * Initializes the default preferences settings for this plug-in.
 	 */
 	protected void initializeDefaultPreferences() {
 		// Init and store default and instance preferences
 		IEclipsePreferences defaultPreferences = getDefaultPreferences();
 
 		// Override some compiler defaults
 		defaultPreferences.put(COMPILER_TASK_TAGS, DEFAULT_TASK_TAGS);
 		defaultPreferences.put(COMPILER_TASK_PRIORITIES, DEFAULT_TASK_PRIORITIES);
 		defaultPreferences.put(COMPILER_TASK_CASE_SENSITIVE, ENABLED);
 	}
 
 	public static void trace(String message) {
         if (getPlugin().isDebugging()) System.out.println(message);
     }
 
     /**
 	 * @since 3.1
 	 */
 	public static IEclipsePreferences getDefaultPreferences() {
 		if (preferencesLookup[PREF_DEFAULT] == null) {
 			preferencesLookup[PREF_DEFAULT] = new DefaultScope().getNode(PLUGIN_ID);
 		}
 		return preferencesLookup[PREF_DEFAULT];
 	}
 
 	/**
 	 * @since 3.1
 	 */
 	public static IEclipsePreferences getInstancePreferences() {
 		if (preferencesLookup[PREF_INSTANCE] == null) {
 			preferencesLookup[PREF_INSTANCE] = new InstanceScope().getNode(PLUGIN_ID);
 		}
 		return preferencesLookup[PREF_INSTANCE];
 	}
 
 	public static void log(Exception e) {
 		String msg = e.getMessage();
 		if (msg == null) msg = "";
 		log(Status.ERROR, msg, e);
 	}
 
 	/**
 	 * @param string
 	 */
 	public static void log(String string) {
 		log(IStatus.INFO, string);
 	}
 
 	public static void log(int severity, String string) {
 		log(severity, string, null);
 	}
 
 	public static void log(int severity, String string, Throwable e) {
 		getPlugin().getLog().log(new Status(severity, PLUGIN_ID, IStatus.OK, string, e));
 		System.out.println(string);
 		if (e != null) e.printStackTrace();
 	}
 
 	public static String getOSDirectory(Plugin plugin) {
 		final Bundle bundle = plugin.getBundle();
 		String location = bundle.getLocation();
 		int prefixLength = location.indexOf('@');
 		if (prefixLength == -1) { throw new RuntimeException("Location of launching bundle does not contain @: " + location); }
 		String pluginDir = location.substring(prefixLength + 1);
 		File pluginDirFile = new File(pluginDir);
 		if (!pluginDirFile.exists()) 
 		{
 			// pluginDirFile is a relative path, if the working directory is different from
 			// the location of the eclipse executable, we try this ...
 			String installArea = System.getProperty("osgi.install.area");
 			if (installArea.startsWith("file:")) {
 				installArea = installArea.substring("file:".length());
 			}
 			// Path.toOSString() removes a leading slash if on windows, e.g.
 			// /D:/Eclipse => D:/Eclipse
 			File installFile = new File(new Path(installArea).toOSString());
 			pluginDirFile = new File(installFile, pluginDir);
 			if (!pluginDirFile.exists()) 
 				throw new RuntimeException("Unable to find (" + pluginDirFile + ") directory for " + plugin.getClass()); 
 		}
 		return pluginDirFile.getAbsolutePath()+"/";
 	}
 
 	public static IProject[] getRubyProjects() {
 		List rubyProjectsList = new ArrayList();
 		IProject[] workspaceProjects = RubyCore.getWorkspace().getRoot().getProjects();
 
 		for (int i = 0; i < workspaceProjects.length; i++) {
 			IProject iProject = workspaceProjects[i];
 			if (isRubyProject(iProject)) rubyProjectsList.add(iProject);
 		}
 
 		IProject[] rubyProjects = new IProject[rubyProjectsList.size()];
 		return (IProject[]) rubyProjectsList.toArray(rubyProjects);
 	}
 
 	public static RubyProject getRubyProject(String name) {
 		IProject aProject = RubyCore.getWorkspace().getRoot().getProject(name);
 		if (isRubyProject(aProject)) {
 			RubyProject theRubyProject = new RubyProject();
 			theRubyProject.setProject(aProject);
 			return theRubyProject;
 		}
 		return null;
 	}
 
 	public static boolean isRubyProject(IProject aProject) {
 		try {
 			return aProject.hasNature(RubyCore.NATURE_ID);
 		} catch (CoreException e) {}
 		return false;
 	}
 
 	public static IRubyScript create(IFile aFile) {
 		return create(aFile, null);
 	}
 
 	public static IRubyScript create(IFile file, IRubyProject project) {
 		if (project == null) {
 			project = create(file.getProject());
 		}
 		// FIXME Use the associations to determine if we should create the file!
 		for (int i = 0; i < IRubyScript.EXTENSIONS.length; i++) {
 			if (IRubyScript.EXTENSIONS[i].equalsIgnoreCase(file.getFileExtension())) {
 				RubyScript script = new RubyScript((RubyProject) project, file, file.getName(), DefaultWorkingCopyOwner.PRIMARY);
 				return script;
 			}
 		}
 		return null;
 	}
 
 	public static IRubyProject create(IProject project) {
 		if (project == null) {
 			return null;
 		}
 		RubyModel rubyModel = RubyModelManager.getRubyModelManager().getRubyModel();
 		return rubyModel.getRubyProject(project);
 	}
 
 	public static void addRubyNature(IProject project, IProgressMonitor monitor) throws CoreException {
 		if (!project.hasNature(RubyCore.NATURE_ID)) {
 			IProjectDescription description = project.getDescription();
 			String[] prevNatures = description.getNatureIds();
 			String[] newNatures = new String[prevNatures.length + 1];
 			System.arraycopy(prevNatures, 0, newNatures, 0, prevNatures.length);
 			newNatures[prevNatures.length] = RubyCore.NATURE_ID;
 			description.setNatureIds(newNatures);
 			project.setDescription(description, monitor);
 		}
 	}
 
 	public static IRubyElement create(IResource resource) {
 		switch (resource.getType()) {
 		case IResource.FILE:
 			return create((IFile)resource);
 		case IResource.PROJECT:
 			return create((IProject)resource);
 		default: // TODO Is this anywhere near correct?
 			return null;
 		}
 	}
 	
 	/**
 	 * Returns the Ruby model.
 	 * 
 	 * @param root the given root
 	 * @return the Ruby model, or <code>null</code> if the root is null
 	 */
 	public static IRubyModel create(IWorkspaceRoot root) {
 		if (root == null) {
 			return null;
 		}
 		return RubyModelManager.getRubyModelManager().getRubyModel();
 	}
 
 	/**
 	 * Runs the given action as an atomic Java model operation.
 	 * <p>
 	 * After running a method that modifies java elements,
 	 * registered listeners receive after-the-fact notification of
 	 * what just transpired, in the form of a element changed event.
 	 * This method allows clients to call a number of
 	 * methods that modify java elements and only have element
 	 * changed event notifications reported at the end of the entire
 	 * batch.
 	 * </p>
 	 * <p>
 	 * If this method is called outside the dynamic scope of another such
 	 * call, this method runs the action and then reports a single
 	 * element changed event describing the net effect of all changes
 	 * done to java elements by the action.
 	 * </p>
 	 * <p>
 	 * If this method is called in the dynamic scope of another such
 	 * call, this method simply runs the action.
 	 * </p>
 	 * <p>
  	 * The supplied scheduling rule is used to determine whether this operation can be
 	 * run simultaneously with workspace changes in other threads. See 
 	 * <code>IWorkspace.run(...)</code> for more details.
  	 * </p>
 	 *
 	 * @param action the action to perform
 	 * @param rule the scheduling rule to use when running this operation, or
 	 * <code>null</code> if there are no scheduling restrictions for this operation.
 	 * @param monitor a progress monitor, or <code>null</code> if progress
 	 *    reporting and cancellation are not desired
 	 * @exception CoreException if the operation failed.
 	 * @since 3.0
 	 */
 	public static void run(IWorkspaceRunnable action, ISchedulingRule rule, IProgressMonitor monitor) throws CoreException {
 		IWorkspace workspace = ResourcesPlugin.getWorkspace();
 		if (workspace.isTreeLocked()) {
 			new BatchOperation(action).run(monitor);
 		} else {
 			// use IWorkspace.run(...) to ensure that a build will be done in autobuild mode
 			workspace.run(new BatchOperation(action), rule, IWorkspace.AVOID_UPDATE, monitor);
 		}
 	}
 
     public SymbolIndex getSymbolIndex() {
         return symbolIndex;
     }	
 }
