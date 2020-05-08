 /*
  * Created on Jan 14, 2005
  *
  */
 package org.rubypeople.rdt.internal.core;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 import java.util.Map.Entry;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceChangeEvent;
 import org.eclipse.core.resources.ISaveContext;
 import org.eclipse.core.resources.ISaveParticipant;
 import org.eclipse.core.resources.ISavedState;
 import org.eclipse.core.resources.IWorkspace;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.IWorkspaceRunnable;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IExtension;
 import org.eclipse.core.runtime.IExtensionPoint;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.MultiStatus;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.PerformanceStats;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.Plugin;
 import org.eclipse.core.runtime.Preferences;
 import org.eclipse.core.runtime.QualifiedName;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.content.IContentTypeManager.ContentTypeChangeEvent;
 import org.eclipse.core.runtime.content.IContentTypeManager.IContentTypeChangeListener;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.core.runtime.preferences.DefaultScope;
 import org.eclipse.core.runtime.preferences.IEclipsePreferences;
 import org.eclipse.core.runtime.preferences.IPreferencesService;
 import org.eclipse.core.runtime.preferences.InstanceScope;
 import org.osgi.service.prefs.BackingStoreException;
 import org.rubypeople.rdt.core.ILoadpathContainer;
 import org.rubypeople.rdt.core.ILoadpathEntry;
 import org.rubypeople.rdt.core.IParent;
 import org.rubypeople.rdt.core.IProblemRequestor;
 import org.rubypeople.rdt.core.IRubyElement;
 import org.rubypeople.rdt.core.IRubyModel;
 import org.rubypeople.rdt.core.IRubyProject;
 import org.rubypeople.rdt.core.IRubyScript;
 import org.rubypeople.rdt.core.ISourceFolder;
 import org.rubypeople.rdt.core.ISourceFolderRoot;
 import org.rubypeople.rdt.core.LoadpathContainerInitializer;
 import org.rubypeople.rdt.core.RubyCore;
 import org.rubypeople.rdt.core.RubyModelException;
 import org.rubypeople.rdt.core.WorkingCopyOwner;
 import org.rubypeople.rdt.core.parser.IProblem;
 import org.rubypeople.rdt.internal.compiler.util.HashtableOfObjectToInt;
 import org.rubypeople.rdt.internal.core.buffer.BufferManager;
 import org.rubypeople.rdt.internal.core.builder.RubyBuilder;
 import org.rubypeople.rdt.internal.core.util.Messages;
 import org.rubypeople.rdt.internal.core.util.Util;
 import org.rubypeople.rdt.internal.core.util.WeakHashSet;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 
 /**
  * @author cawilliams
  * 
  */
 public class RubyModelManager implements IContentTypeChangeListener, ISaveParticipant {
 
     private static final String BUFFER_MANAGER_DEBUG = RubyCore.PLUGIN_ID + "/debug/buffermanager"; //$NON-NLS-1$
     private static final String RUBYMODEL_DEBUG = RubyCore.PLUGIN_ID + "/debug/rubymodel"; //$NON-NLS-1$
     private static final String DELTA_DEBUG = RubyCore.PLUGIN_ID + "/debug/rubydelta"; //$NON-NLS-1$
     private static final String DELTA_DEBUG_VERBOSE = RubyCore.PLUGIN_ID
             + "/debug/rubydelta/verbose"; //$NON-NLS-1$
     private static final String POST_ACTION_DEBUG = RubyCore.PLUGIN_ID + "/debug/postaction"; //$NON-NLS-1$
     private static final String BUILDER_DEBUG = RubyCore.PLUGIN_ID + "/debug/builder"; //$NON-NLS-1$
 
     private static final String ENABLE_NEW_FORMATTER = RubyCore.PLUGIN_ID + "/formatter/enable_new"; //$NON-NLS-1$
 
     public static final String DELTA_LISTENER_PERF = RubyCore.PLUGIN_ID + "/perf/rubydeltalistener"; //$NON-NLS-1$
     public static final String RECONCILE_PERF = RubyCore.PLUGIN_ID + "/perf/reconcile"; //$NON-NLS-1$
 
     
 	/**
 	 * Name of the extension point for contributing classpath variable initializers
 	 */
 	public static final String CPVARIABLE_INITIALIZER_EXTPOINT_ID = "loadpathVariableInitializer" ; //$NON-NLS-1$
 
 	/**
 	 * Name of the extension point for contributing classpath container initializers
 	 */
 	public static final String CPCONTAINER_INITIALIZER_EXTPOINT_ID = "loadpathContainerInitializer" ; //$NON-NLS-1$
 
     
     /**
 	 * Classpath variables pool
 	 */
 	public HashMap<String, IPath> variables = new HashMap<String, IPath>(5);
 	public HashSet variablesWithInitializer = new HashSet(5);
 	public HashMap previousSessionVariables = new HashMap(5);
 	private ThreadLocal<HashSet<String>> variableInitializationInProgress = new ThreadLocal<HashSet<String>>();
 		
 	/**
 	 * Classpath containers pool
 	 */
 	public HashMap<IRubyProject, Map> containers = new HashMap<IRubyProject, Map>(5);
 	public HashMap previousSessionContainers = new HashMap(5);
 	private ThreadLocal<Map> containerInitializationInProgress = new ThreadLocal<Map>();
 	public boolean batchContainerInitializations = false;
 	public HashMap containerInitializersCache = new HashMap(5);
     
     /**
      * The singleton manager
      */
     private final static RubyModelManager MANAGER = new RubyModelManager();
 
     /**
      * Holds the state used for delta processing.
      */
     public DeltaProcessingState deltaState = new DeltaProcessingState();
 
     /**
      * Unique handle onto the RubyModel
      */
     final RubyModel rubyModel = new RubyModel();
 
     /*
      * Temporary cache of newly opened elements
      */
     private ThreadLocal<HashMap> temporaryCache = new ThreadLocal<HashMap>();
 
     /**
      * Set of elements which are out of sync with their buffers.
      */
     protected HashSet elementsOutOfSynchWithBuffers = new HashSet(11);
 
     /*
      * A HashSet that contains the IJavaProject whose classpath is being
      * resolved.
      */
     private ThreadLocal<HashSet<IRubyProject>> classpathsBeingResolved = new ThreadLocal<HashSet<IRubyProject>>();
 
     /**
      * Infos cache.
      */
     protected RubyModelCache cache = new RubyModelCache();
 
     /**
      * Table from IProject to PerProjectInfo. NOTE: this object itself is used
      * as a lock to synchronize creation/removal of per project infos
      */
     protected Map<IProject, PerProjectInfo> perProjectInfos = new HashMap<IProject, PerProjectInfo>(5);
 
     /**
      * Table from WorkingCopyOwner to a table of ICompilationUnit (working copy
      * handle) to PerWorkingCopyInfo. NOTE: this object itself is used as a lock
      * to synchronize creation/removal of per working copy infos
      */
     protected Map<WorkingCopyOwner, Map> perWorkingCopyInfos = new HashMap<WorkingCopyOwner, Map>(5);
 
     private static boolean verbose = false;
     public static boolean CP_RESOLVE_VERBOSE = false;
 	public static boolean ZIP_ACCESS_VERBOSE = false;
 
     // Preferences
     HashSet optionNames = new HashSet(20);
     Hashtable<String, String> optionsCache;
 
     public final IEclipsePreferences[] preferencesLookup = new IEclipsePreferences[2];
 	private WeakHashSet stringSymbols = new WeakHashSet(5);
     static final int PREF_INSTANCE = 0;
     static final int PREF_DEFAULT = 1;
     
 	public static final IRubyScript[] NO_WORKING_COPY = new IRubyScript[0];
 	public static final boolean VERBOSE = false;
 	
 	public final static String CP_VARIABLE_PREFERENCES_PREFIX = RubyCore.PLUGIN_ID+".loadpathVariable."; //$NON-NLS-1$
 	public final static String CP_CONTAINER_PREFERENCES_PREFIX = RubyCore.PLUGIN_ID+".loadpathContainer."; //$NON-NLS-1$
 	
 	/**
 	 * Special value used for recognizing ongoing initialization and breaking initialization cycles
 	 */
 	public final static IPath VARIABLE_INITIALIZATION_IN_PROGRESS = new Path("Variable Initialization In Progress"); //$NON-NLS-1$
 	public final static ILoadpathContainer CONTAINER_INITIALIZATION_IN_PROGRESS = new ILoadpathContainer() {
 		public ILoadpathEntry[] getLoadpathEntries() { return null; }
 		public String getDescription() { return "Container Initialization In Progress"; } //$NON-NLS-1$
 		public int getKind() { return 0; }
 		public IPath getPath() { return null; }
 		public String toString() { return getDescription(); }
 	};
 	public final static String CP_ENTRY_IGNORE = "##<cp entry ignore>##"; //$NON-NLS-1$
 	public final static IPath CP_ENTRY_IGNORE_PATH = new Path(CP_ENTRY_IGNORE);
 	private static final int VARIABLES_AND_CONTAINERS_FILE_VERSION = 1;
 	
 	public static boolean PERF_VARIABLE_INITIALIZER = false;
 	public static boolean PERF_CONTAINER_INITIALIZER = false;
     
 	/**
      * Update the classpath variable cache
      */
     public static class EclipsePreferencesListener implements IEclipsePreferences.IPreferenceChangeListener {
         /**
          * @see org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener#preferenceChange(org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent)
          */
         public void preferenceChange(IEclipsePreferences.PreferenceChangeEvent event) {
             // TODO Listen for loadpath changes!
         }
     }
     
     /**
      * Constructs a new RubyModelManager
      */
     private RubyModelManager() {
         // singleton: prevent others from creating a new instance
     }
 
     /**
      * Returns the singleton RubyModelManager
      */
     public final static RubyModelManager getRubyModelManager() {
         return MANAGER;
     }
     
     /**
      * Initialize preferences lookups for JavaCore plugin.
      */
     public void initializePreferences() {
         
         // Create lookups
         preferencesLookup[PREF_INSTANCE] = new InstanceScope().getNode(RubyCore.PLUGIN_ID);
         preferencesLookup[PREF_DEFAULT] = new DefaultScope().getNode(RubyCore.PLUGIN_ID);
 
         // Listen to instance preferences node removal from parent in order to refresh stored one
         IEclipsePreferences.INodeChangeListener listener = new IEclipsePreferences.INodeChangeListener() {
             public void added(IEclipsePreferences.NodeChangeEvent event) {
                 // do nothing
             }
             public void removed(IEclipsePreferences.NodeChangeEvent event) {
                 if (event.getChild() == preferencesLookup[PREF_INSTANCE]) {
                     preferencesLookup[PREF_INSTANCE] = new InstanceScope().getNode(RubyCore.PLUGIN_ID);
                     preferencesLookup[PREF_INSTANCE].addPreferenceChangeListener(new EclipsePreferencesListener());
                 }
             }
         };
         ((IEclipsePreferences) preferencesLookup[PREF_INSTANCE].parent()).addNodeChangeListener(listener);
         preferencesLookup[PREF_INSTANCE].addPreferenceChangeListener(new EclipsePreferencesListener());
 
         // Listen to default preferences node removal from parent in order to refresh stored one
         listener = new IEclipsePreferences.INodeChangeListener() {
             public void added(IEclipsePreferences.NodeChangeEvent event) {
                 // do nothing
             }
             public void removed(IEclipsePreferences.NodeChangeEvent event) {
                 if (event.getChild() == preferencesLookup[PREF_DEFAULT]) {
                     preferencesLookup[PREF_DEFAULT] = new DefaultScope().getNode(RubyCore.PLUGIN_ID);
                 }
             }
         };
         ((IEclipsePreferences) preferencesLookup[PREF_DEFAULT].parent()).addNodeChangeListener(listener);
     }
 
 
     /**
      * Returns the info for the element.
      */
     public synchronized Object getInfo(IRubyElement element) {
         HashMap tempCache = this.temporaryCache.get();
         if (tempCache != null) {
             Object result = tempCache.get(element);
             if (result != null) { return result; }
         }
         return this.cache.getInfo(element);
     }
 
     /*
      * Removes all cached info for the given element (including all children)
      * from the cache. Returns the info for the given element, or null if it was
      * closed.
      */
     public synchronized Object removeInfoAndChildren(RubyElement element) throws RubyModelException {
         Object info = this.cache.peekAtInfo(element);
         if (info != null) {
             element.closing(info);
             if (element instanceof IParent && info instanceof RubyElementInfo) {
                 IRubyElement[] children = ((RubyElementInfo) info).getChildren();
                 for (int i = 0, size = children.length; i < size; ++i) {
                     RubyElement child = (RubyElement) children[i];
                     child.close();
                 }
             }
             this.cache.removeInfo(element);
             return info;
         }
         return null;
     }
 
     /**
      * Returns the info for this element without disturbing the cache ordering.
      */
     protected synchronized Object peekAtInfo(IRubyElement element) {
         HashMap tempCache = this.temporaryCache.get();
         if (tempCache != null) {
             Object result = tempCache.get(element);
             if (result != null) { return result; }
         }
         return this.cache.peekAtInfo(element);
     }
 
     /*
      * Puts the infos in the given map (keys are IRubyElements and values are
      * RubyElementInfos) in the Ruby model cache in an atomic way. First checks
      * that the info for the opened element (or one of its ancestors) has not
      * been added to the cache. If it is the case, another thread has opened the
      * element (or one of its ancestors). So returns without updating the cache.
      */
     protected synchronized void putInfos(IRubyElement openedElement, Map newElements) {
         // remove children
         Object existingInfo = this.cache.peekAtInfo(openedElement);
         if (openedElement instanceof IParent && existingInfo instanceof RubyElementInfo) {
             IRubyElement[] children = ((RubyElementInfo) existingInfo).getChildren();
             for (int i = 0, size = children.length; i < size; ++i) {
                 RubyElement child = (RubyElement) children[i];
                 try {
                     child.close();
                 } catch (RubyModelException e) {
                     // ignore
                 }
             }
         }
 
         Iterator iterator = newElements.keySet().iterator();
         while (iterator.hasNext()) {
             IRubyElement element = (IRubyElement) iterator.next();
             Object info = newElements.get(element);
             this.cache.putInfo(element, info);
         }
     }
 
     /**
      * Returns the temporary cache for newly opened elements for the current
      * thread. Creates it if not already created.
      */
     public HashMap getTemporaryCache() {
         HashMap result = this.temporaryCache.get();
         if (result == null) {
             result = new HashMap();
             this.temporaryCache.set(result);
         }
         return result;
     }
 
     /*
      * Returns whether there is a temporary cache for the current thread.
      */
     public boolean hasTemporaryCache() {
         return this.temporaryCache.get() != null;
     }
 
     /*
      * Reset project options stored in info cache.
      */
     public void resetProjectOptions(RubyProject rubyProject) {
         synchronized (this.perProjectInfos) { // use the perProjectInfo
             // collection as its own lock
             IProject project = rubyProject.getProject();
             PerProjectInfo info = this.perProjectInfos.get(project);
             if (info != null) {
                 info.options = null;
             }
         }
     }
 
     /*
      * Reset project preferences stored in info cache.
      */
     public void resetProjectPreferences(RubyProject rubyProject) {
         synchronized (this.perProjectInfos) { // use the perProjectInfo
             // collection as its own lock
             IProject project = rubyProject.getProject();
             PerProjectInfo info = this.perProjectInfos.get(project);
             if (info != null) {
                 info.preferences = null;
             }
         }
     }
 
     /*
      * Resets the temporary cache for newly created elements to null.
      */
     public void resetTemporaryCache() {
         this.temporaryCache.set(null);
     }
 
     /**
      * Returns the handle to the active Ruby Model.
      */
     public final RubyModel getRubyModel() {
         return this.rubyModel;
     }
 
     public static class PerWorkingCopyInfo implements IProblemRequestor {
 
         int useCount = 0;
         IRubyScript workingCopy;
         private IProblemRequestor problemRequestor;
 
         public PerWorkingCopyInfo(IRubyScript workingCopy, IProblemRequestor problemRequestor) {
             this.workingCopy = workingCopy;
             this.problemRequestor = problemRequestor;
         }
 
         public IRubyScript getWorkingCopy() {
             return this.workingCopy;
         }
 
         public String toString() {
             StringBuffer buffer = new StringBuffer();
             buffer.append("Info for "); //$NON-NLS-1$
             buffer.append(((RubyElement) this.workingCopy).toString());
             buffer.append("\nUse count = "); //$NON-NLS-1$
             buffer.append(this.useCount);
             buffer.append("\nProblem requestor:\n  "); //$NON-NLS-1$
             buffer.append(this.problemRequestor);
             return buffer.toString();
         }
 
         public void acceptProblem(IProblem problem) {
             if (this.problemRequestor == null) return;
             this.problemRequestor.acceptProblem(problem);
         }
 
         public void beginReporting() {
             if (this.problemRequestor == null) return;
             this.problemRequestor.beginReporting();
         }
 
         public void endReporting() {
             if (this.problemRequestor == null) return;
             this.problemRequestor.endReporting();
         }
 
         public boolean isActive() {
             return this.problemRequestor != null && this.problemRequestor.isActive();
         }
     }
 
     /**
      * @param script
      * @param create
      * @param recordUsage
      * @param problemRequestor
      * @param object
      * @return
      */
     public PerWorkingCopyInfo getPerWorkingCopyInfo(RubyScript workingCopy, boolean create,
             boolean recordUsage, IProblemRequestor problemRequestor) {
         synchronized (this.perWorkingCopyInfos) { // use the
             // perWorkingCopyInfo
             // collection as its own
             // lock
             WorkingCopyOwner owner = workingCopy.owner;
             Map<RubyScript, PerWorkingCopyInfo> workingCopyToInfos = this.perWorkingCopyInfos.get(owner);
             if (workingCopyToInfos == null && create) {
                 workingCopyToInfos = new HashMap<RubyScript, PerWorkingCopyInfo>();
                 this.perWorkingCopyInfos.put(owner, workingCopyToInfos);
             }
 
             PerWorkingCopyInfo info = workingCopyToInfos == null ? null
                     : workingCopyToInfos.get(workingCopy);
             if (info == null && create) {
                 info = new PerWorkingCopyInfo(workingCopy, problemRequestor);
                 workingCopyToInfos.put(workingCopy, info);
             }
             if (info != null && recordUsage) info.useCount++;
             return info;
         }
     }
 
     /*
      * Discards the per working copy info for the given working copy (making it
      * a compilation unit) if its use count was 1. Otherwise, just decrement the
      * use count. If the working copy is primary, computes the delta between its
      * state and the original compilation unit and register it. Close the
      * working copy, its buffer and remove it from the shared working copy
      * table. Ignore if no per-working copy info existed. NOTE: it must NOT be
      * synchronized as it may interact with the element info cache (if useCount
      * is decremented to 0), see bug 50667. Returns the new use count (or -1 if
      * it didn't exist).
      */
     public int discardPerWorkingCopyInfo(RubyScript workingCopy) throws RubyModelException {
         PerWorkingCopyInfo info = null;
         synchronized (this.perWorkingCopyInfos) {
             WorkingCopyOwner owner = workingCopy.owner;
             Map workingCopyToInfos = this.perWorkingCopyInfos.get(owner);
             if (workingCopyToInfos == null) return -1;
 
             info = (PerWorkingCopyInfo) workingCopyToInfos.get(workingCopy);
             if (info == null) return -1;
 
             if (--info.useCount == 0) {
                 // remove per working copy info
                 workingCopyToInfos.remove(workingCopy);
                 if (workingCopyToInfos.isEmpty()) {
                     this.perWorkingCopyInfos.remove(owner);
                 }
             }
         }
         if (info.useCount == 0) { // info cannot be null here (check was done
             // above)
             // remove infos + close buffer (since no longer working copy)
             // outside the perWorkingCopyInfos lock (see bug 50667)
             removeInfoAndChildren(workingCopy);
             workingCopy.closeBuffer();
         }
         return info.useCount;
     }
 
     /**
      * Returns the set of elements which are out of synch with their buffers.
      */
     protected HashSet getElementsOutOfSynchWithBuffers() {
         return this.elementsOutOfSynchWithBuffers;
     }
 
     /*
      * Returns the per-project info for the given project. If specified, create
      * the info if the info doesn't exist.
      */
     public PerProjectInfo getPerProjectInfo(IProject project, boolean create) {
         synchronized (this.perProjectInfos) { // use the perProjectInfo
             // collection as its own lock
             PerProjectInfo info = this.perProjectInfos.get(project);
             if (info == null && create) {
                 info = new PerProjectInfo(project);
                 this.perProjectInfos.put(project, info);
             }
             return info;
         }
     }
 
     public void removePerProjectInfo(RubyProject rubyProject) {
         synchronized (this.perProjectInfos) { // use the perProjectInfo
             // collection as its own lock
             IProject project = rubyProject.getProject();
             PerProjectInfo info = this.perProjectInfos.get(project);
             if (info != null) {
                 this.perProjectInfos.remove(project);
             }
         }
     }
 
     public boolean isLoadpathBeingResolved(IRubyProject project) {
         return getLoadpathBeingResolved().contains(project);
     }
 
     private HashSet<IRubyProject> getLoadpathBeingResolved() {
         HashSet<IRubyProject> result = this.classpathsBeingResolved.get();
         if (result == null) {
             result = new HashSet<IRubyProject>();
             this.classpathsBeingResolved.set(result);
         }
         return result;
     }
 
     public static class PerProjectInfo {
 
         public IProject project;
         public Object savedState;
         public boolean triedRead;
         public ILoadpathEntry[] rawLoadpath;
         public ILoadpathEntry[] resolvedLoadpath;
         public Map resolvedPathToRawEntries; // reverse map from resolved
         // path to raw entries
         public IPath outputLocation;
 
         public IEclipsePreferences preferences;
         public Hashtable options;
 
         public PerProjectInfo(IProject project) {
 
             this.triedRead = false;
             this.savedState = null;
             this.project = project;
         }
 
         // updating raw classpath need to flush obsoleted cached information
         // about resolved entries
         public synchronized void updateLoadpathInformation(ILoadpathEntry[] newRawClasspath) {
 
             this.rawLoadpath = newRawClasspath;
             this.resolvedLoadpath = null;
             this.resolvedPathToRawEntries = null;
         }
 
         public String toString() {
             StringBuffer buffer = new StringBuffer();
             buffer.append("Info for "); //$NON-NLS-1$
             buffer.append(this.project.getFullPath());
             buffer.append("\nRaw classpath:\n"); //$NON-NLS-1$
             if (this.rawLoadpath == null) {
                 buffer.append("  <null>\n"); //$NON-NLS-1$
             } else {
                 for (int i = 0, length = this.rawLoadpath.length; i < length; i++) {
                     buffer.append("  "); //$NON-NLS-1$
                     buffer.append(this.rawLoadpath[i]);
                     buffer.append('\n');
                 }
             }
             buffer.append("Resolved classpath:\n"); //$NON-NLS-1$
             ILoadpathEntry[] resolvedCP = this.resolvedLoadpath;
             if (resolvedCP == null) {
                 buffer.append("  <null>\n"); //$NON-NLS-1$
             } else {
                 for (int i = 0, length = resolvedCP.length; i < length; i++) {
                     buffer.append("  "); //$NON-NLS-1$
                     buffer.append(resolvedCP[i]);
                     buffer.append('\n');
                 }
             }
             buffer.append("Output location:\n  "); //$NON-NLS-1$
             if (this.outputLocation == null) {
                 buffer.append("<null>"); //$NON-NLS-1$
             } else {
                 buffer.append(this.outputLocation);
             }
             return buffer.toString();
         }
 
 		public void rememberExternalLibTimestamps() {
 			ILoadpathEntry[] classpath = this.resolvedLoadpath;
 			if (classpath == null) return;
 			IWorkspaceRoot wRoot = ResourcesPlugin.getWorkspace().getRoot();
 			Map<IPath, Long> externalTimeStamps = RubyModelManager.getRubyModelManager().deltaState.getExternalLibTimeStamps();
 			for (int i = 0, length = classpath.length; i < length; i++) {
 				ILoadpathEntry entry = classpath[i];
 				if (entry.getEntryKind() == ILoadpathEntry.CPE_LIBRARY) {
 					IPath path = entry.getPath();
 					if (externalTimeStamps.get(path) == null) {
 						Object target = RubyModel.getTarget(wRoot, path, true);
 						if (target instanceof java.io.File) {
 							long timestamp = DeltaProcessor.getTimeStamp((java.io.File)target);
 							externalTimeStamps.put(path, new Long(timestamp));							
 						}
 					}
 				}
 			}
 		}
     }
 
     /*
      * Returns the per-project info for the given project. If the info doesn't
      * exist, check for the project existence and create the info. @throws
      * RubyModelException if the project doesn't exist.
      */
     public PerProjectInfo getPerProjectInfoCheckExistence(IProject project)
             throws RubyModelException {
         RubyModelManager.PerProjectInfo info = getPerProjectInfo(project, false /*
                                                                                  * don't
                                                                                  * create
                                                                                  * info
                                                                                  */);
         if (info == null) {
             if (!RubyProject.hasRubyNature(project)) { throw ((RubyProject) RubyCore
                     .create(project)).newNotPresentException(); }
             info = getPerProjectInfo(project, true /* create info */);
         }
         return info;
     }
 
     public void setLoadpathBeingResolved(IRubyProject project, boolean classpathIsResolved) {
         if (classpathIsResolved) {
             getLoadpathBeingResolved().add(project);
         } else {
             getLoadpathBeingResolved().remove(project);
         }
     }
 
     public static void setVerbose(boolean verbose) {
         RubyModelManager.verbose = verbose;
     }
 
     public static boolean isVerbose() {
         return verbose;
     }
 
     public String getOption(String optionName) {
         if (RubyCore.CORE_ENCODING.equals(optionName)) { return RubyCore.getEncoding(); }
         String propertyName = optionName;
         if (this.optionNames.contains(propertyName)) {
             IPreferencesService service = Platform.getPreferencesService();
             String value = service.get(optionName, null, this.preferencesLookup);
             return value == null ? null : value.trim();
         }
         return null;
     }
 
     public Hashtable<String, String> getOptions() {
 
         // return cached options if already computed
         if (this.optionsCache != null) return new Hashtable<String, String>(this.optionsCache);
 
         // init
         Hashtable<String, String> options = new Hashtable<String, String>(10);
         IPreferencesService service = Platform.getPreferencesService();
 
         // set options using preferences service lookup
         Iterator iterator = optionNames.iterator();
         while (iterator.hasNext()) {
             String propertyName = (String) iterator.next();
             String propertyValue = service.get(propertyName, null, this.preferencesLookup);
             if (propertyValue != null) {
                 options.put(propertyName, propertyValue);
             }
         }
 
         // get encoding through resource plugin
         options.put(RubyCore.CORE_ENCODING, RubyCore.getEncoding());
 
         // store built map in cache
         this.optionsCache = new Hashtable<String, String>(options);
 
         // return built map
         return options;
     }
 
     public DeltaProcessor getDeltaProcessor() {
         return this.deltaState.getDeltaProcessor();
     }
 
     public void startup() throws CoreException {
         try {
             configurePluginDebugOptions();
             
 //          initialize Ruby model cache
 			this.cache = new RubyModelCache();
 
             // request state folder creation (workaround 19885)
             RubyCore.getPlugin().getStateLocation();
             
 //          Initialize eclipse preferences
             initializePreferences();
 
             // Listen to preference changes
             Preferences.IPropertyChangeListener propertyListener = new Preferences.IPropertyChangeListener() {
                 public void propertyChange(Preferences.PropertyChangeEvent event) {
                     RubyModelManager.this.optionsCache = null;
                 }
             };
             RubyCore.getPlugin().getPluginPreferences().addPropertyChangeListener(propertyListener);
 
 //          Listen to content-type changes
              Platform.getContentTypeManager().addContentTypeChangeListener(this);
             
 //           retrieve variable values
  			long start = -1;
  			if (VERBOSE)
  				start = System.currentTimeMillis();
   			loadVariablesAndContainers();
 //  			if (VERBOSE)
 // 				traceVariableAndContainers("Loaded", start); //$NON-NLS-1$
 
  			final IWorkspace workspace = ResourcesPlugin.getWorkspace();
  			workspace.addResourceChangeListener(
  				this.deltaState,
  				/* update spec in JavaCore#addPreProcessingResourceChangedListener(...) if adding more event types */
  				IResourceChangeEvent.PRE_BUILD
  					| IResourceChangeEvent.POST_BUILD
  					| IResourceChangeEvent.POST_CHANGE
  					| IResourceChangeEvent.PRE_DELETE
  					| IResourceChangeEvent.PRE_CLOSE);
 
 // 			startIndexing();
  			
  			// process deltas since last activated in indexer thread so that indexes are up-to-date.
  			// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=38658
  			Job processSavedState = new Job(Messages.savedState_jobName) { 
  				protected IStatus run(IProgressMonitor monitor) {
  					try {
  						// add save participant and process delta atomically
  						// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=59937
  						workspace.run(
  							new IWorkspaceRunnable() {
  								public void run(IProgressMonitor progress) throws CoreException {
  									ISavedState savedState = workspace.addSaveParticipant(RubyCore.getRubyCore(), RubyModelManager.this);
  									if (savedState != null) {
  										// the event type coming from the saved state is always POST_AUTO_BUILD
  										// force it to be POST_CHANGE so that the delta processor can handle it
  										RubyModelManager.this.deltaState.getDeltaProcessor().overridenEventType = IResourceChangeEvent.POST_CHANGE;
  										savedState.processResourceChangeEvents(RubyModelManager.this.deltaState);
  									}
  								}
  							},
  							monitor);
  					} catch (CoreException e) {
  						return e.getStatus();
  					}
  					return Status.OK_STATUS;
  				}
  			};
  			processSavedState.setSystem(true);
  			processSavedState.setPriority(Job.SHORT); // process asap
  			processSavedState.schedule();
         } catch (RuntimeException e) {
             shutdown();
             throw e;
         }
     }
     
     public void loadVariablesAndContainers() throws CoreException {
 		// backward compatibility, consider persistent property	
 		QualifiedName qName = new QualifiedName(RubyCore.PLUGIN_ID, "variables"); //$NON-NLS-1$
 		String xmlString = ResourcesPlugin.getWorkspace().getRoot().getPersistentProperty(qName);
 		
 		try {
 			if (xmlString != null){
 				StringReader reader = new StringReader(xmlString);
 				Element cpElement;
 				try {
 					DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
 					cpElement = parser.parse(new InputSource(reader)).getDocumentElement();
 				} catch(SAXException e) {
 					return;
 				} catch(ParserConfigurationException e){
 					return;
 				} finally {
 					reader.close();
 				}
 				if (cpElement == null) return;
 				if (!cpElement.getNodeName().equalsIgnoreCase("variables")) { //$NON-NLS-1$
 					return;
 				}
 				
 				NodeList list= cpElement.getChildNodes();
 				int length= list.getLength();
 				for (int i= 0; i < length; ++i) {
 					Node node= list.item(i);
 					short type= node.getNodeType();
 					if (type == Node.ELEMENT_NODE) {
 						Element element= (Element) node;
 						if (element.getNodeName().equalsIgnoreCase("variable")) { //$NON-NLS-1$
 							variablePut( 
 								element.getAttribute("name"), //$NON-NLS-1$
 								new Path(element.getAttribute("path"))); //$NON-NLS-1$
 						}
 					}
 				}
 			}
 		} catch(IOException e){
 			// problem loading xml file: nothing we can do
 		} finally {
 			if (xmlString != null){
 				ResourcesPlugin.getWorkspace().getRoot().setPersistentProperty(qName, null); // flush old one
 			}
 		}
 
 		// backward compatibility, load variables and containers from preferences into cache
 		loadVariablesAndContainers(getDefaultPreferences());
 		loadVariablesAndContainers(getInstancePreferences());
 
 		// load variables and containers from saved file into cache
 		File file = getVariableAndContainersFile();
 		DataInputStream in = null;
 		try {
 			in = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
 			switch (in.readInt()) {
				case VARIABLES_AND_CONTAINERS_FILE_VERSION :
 					new VariablesAndContainersLoadHelper(in).load();
 					break;
 			}
 		} catch (IOException e) {
 			if (file.exists())
 				Util.log(e, "Unable to read variable and containers file"); //$NON-NLS-1$
 		} catch (RuntimeException e) {
 			if (file.exists())
 				Util.log(e, "Unable to read variable and containers file (file is corrupt)"); //$NON-NLS-1$
 		} finally {
 			if (in != null) {
 				try {
 					in.close();
 				} catch (IOException e) {
 					// nothing we can do: ignore
 				}
 			}
 		}
 
 		// override persisted values for variables which have a registered initializer
 		String[] registeredVariables = getRegisteredVariableNames();
 		for (int i = 0; i < registeredVariables.length; i++) {
 			String varName = registeredVariables[i];
 			this.variables.put(varName, null); // reset variable, but leave its entry in the Map, so it will be part of variable names.
 		}
 		// override persisted values for containers which have a registered initializer
 		containersReset(getRegisteredContainerIDs());
 	}
     
 	public static void recreatePersistedContainer(String propertyName, String containerString, boolean addToContainerValues) {
 		int containerPrefixLength = CP_CONTAINER_PREFERENCES_PREFIX.length();
 		int index = propertyName.indexOf('|', containerPrefixLength);
 		if (containerString != null) containerString = containerString.trim();
 		if (index > 0) {
 			String projectName = propertyName.substring(containerPrefixLength, index).trim();
 			IRubyProject project = getRubyModelManager().getRubyModel().getRubyProject(projectName);
 			IPath containerPath = new Path(propertyName.substring(index+1).trim());
 			recreatePersistedContainer(project, containerPath, containerString, addToContainerValues);
 		}
 	}
     
     private static void recreatePersistedContainer(final IRubyProject project, final IPath containerPath, String containerString, boolean addToContainerValues) {
 		if (!project.getProject().isAccessible()) return; // avoid leaking deleted project's persisted container	
 		if (containerString == null) {
 			getRubyModelManager().containerPut(project, containerPath, null);
 		} else {
 			final ILoadpathEntry[] containerEntries = ((RubyProject) project).decodeLoadpath(containerString, false, false);
 			if (containerEntries != null && containerEntries != RubyProject.INVALID_LOADPATH) {
 				ILoadpathContainer container = new ILoadpathContainer() {
 					public ILoadpathEntry[] getLoadpathEntries() {
 						return containerEntries;
 					}
 					public String getDescription() {
 						return "Persisted container ["+containerPath+" for project ["+ project.getElementName()+"]"; //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
 					}
 					public int getKind() {
 						return 0; 
 					}
 					public IPath getPath() {
 						return containerPath;
 					}
 					public String toString() {
 						return getDescription();
 					}
 
 				};
 				if (addToContainerValues) {
 					getRubyModelManager().containerPut(project, containerPath, container);
 				}
 				Map projectContainers = (Map)getRubyModelManager().previousSessionContainers.get(project);
 				if (projectContainers == null){
 					projectContainers = new HashMap(1);
 					getRubyModelManager().previousSessionContainers.put(project, projectContainers);
 				}
 				projectContainers.put(containerPath, container);
 			}
 		}
 	}
 
 	private File getVariableAndContainersFile() {
 		return RubyCore.getPlugin().getStateLocation().append("variablesAndContainers.dat").toFile(); //$NON-NLS-1$
 	}
 	
 	private synchronized void containersReset(String[] containerIDs) {
 		for (int i = 0; i < containerIDs.length; i++) {
 			String containerID = containerIDs[i];
 			Iterator projectIterator = this.containers.keySet().iterator();
 			while (projectIterator.hasNext()){
 				IRubyProject project = (IRubyProject)projectIterator.next();
 				Map projectContainers = (Map)this.containers.get(project);
 				if (projectContainers != null){
 					Iterator containerIterator = projectContainers.keySet().iterator();
 					while (containerIterator.hasNext()){
 						IPath containerPath = (IPath)containerIterator.next();
 						if (containerPath.segment(0).equals(containerID)) { // registered container
 							projectContainers.put(containerPath, null); // reset container value, but leave entry in Map
 						}
 					}
 				}
 			}
 		}
 	}
 	
 	/**
  	 * Returns the name of the variables for which an CP variable initializer is registered through an extension point
  	 */
 	public static String[] getRegisteredVariableNames(){
 		
 		Plugin jdtCorePlugin = RubyCore.getPlugin();
 		if (jdtCorePlugin == null) return null;
 
 		ArrayList variableList = new ArrayList(5);
 		IExtensionPoint extension = Platform.getExtensionRegistry().getExtensionPoint(RubyCore.PLUGIN_ID, RubyModelManager.CPVARIABLE_INITIALIZER_EXTPOINT_ID);
 		if (extension != null) {
 			IExtension[] extensions =  extension.getExtensions();
 			for(int i = 0; i < extensions.length; i++){
 				IConfigurationElement [] configElements = extensions[i].getConfigurationElements();
 				for(int j = 0; j < configElements.length; j++){
 					String varAttribute = configElements[j].getAttribute("variable"); //$NON-NLS-1$
 					if (varAttribute != null) variableList.add(varAttribute);
 				}
 			}	
 		}
 		String[] variableNames = new String[variableList.size()];
 		variableList.toArray(variableNames);
 		return variableNames;
 	}	
 
 	private void loadVariablesAndContainers(IEclipsePreferences preferences) {
 		try {
 			// only get variable from preferences not set to their default
 			String[] propertyNames = preferences.keys();
 			int variablePrefixLength = CP_VARIABLE_PREFERENCES_PREFIX.length();
 			for (int i = 0; i < propertyNames.length; i++){
 				String propertyName = propertyNames[i];
 				if (propertyName.startsWith(CP_VARIABLE_PREFERENCES_PREFIX)){
 					String varName = propertyName.substring(variablePrefixLength);
 					String propertyValue = preferences.get(propertyName, null);
 					if (propertyValue != null) {
 						String pathString = propertyValue.trim();
 						
 						if (CP_ENTRY_IGNORE.equals(pathString)) {
 							// cleanup old preferences
 							preferences.remove(propertyName); 
 							continue;
 						}
 						
 						// add variable to table
 						IPath varPath = new Path(pathString);
 						this.variables.put(varName, varPath); 
 						this.previousSessionVariables.put(varName, varPath);
 					}
 				} else if (propertyName.startsWith(CP_CONTAINER_PREFERENCES_PREFIX)){
 					String propertyValue = preferences.get(propertyName, null);
 					if (propertyValue != null) {
 						// cleanup old preferences
 						preferences.remove(propertyName); 
 						
 						// recreate container
 						recreatePersistedContainer(propertyName, propertyValue, true/*add to container values*/);
 					}
 				}
 			}
 		} catch (BackingStoreException e1) {
 			// TODO (frederic) see if it's necessary to report this failure...
 		}
 	}
     
 	/**
 	 * Get default eclipse preference for JavaCore plugin.
 	 */
 	public IEclipsePreferences getDefaultPreferences() {
 		return preferencesLookup[PREF_DEFAULT];
 	}
 	
 	/**
  	 * Returns the name of the container IDs for which an LP container initializer is registered through an extension point
  	 */
 	public static String[] getRegisteredContainerIDs(){
 		Plugin jdtCorePlugin = RubyCore.getPlugin();
 		if (jdtCorePlugin == null) return null;
 
 		ArrayList containerIDList = new ArrayList(5);
 		IExtensionPoint extension = Platform.getExtensionRegistry().getExtensionPoint(RubyCore.PLUGIN_ID, RubyModelManager.CPCONTAINER_INITIALIZER_EXTPOINT_ID);
 		if (extension != null) {
 			IExtension[] extensions =  extension.getExtensions();
 			for(int i = 0; i < extensions.length; i++){
 				IConfigurationElement [] configElements = extensions[i].getConfigurationElements();
 				for(int j = 0; j < configElements.length; j++){
 					String idAttribute = configElements[j].getAttribute("id"); //$NON-NLS-1$
 					if (idAttribute != null) containerIDList.add(idAttribute);
 				}
 			}	
 		}
 		String[] containerIDs = new String[containerIDList.size()];
 		containerIDList.toArray(containerIDs);
 		return containerIDs;
 	}	
 
     public void shutdown() {
         RubyCore javaCore = RubyCore.getRubyCore();
         javaCore.savePluginPreferences();
         IWorkspace workspace = ResourcesPlugin.getWorkspace();
         workspace.removeResourceChangeListener(this.deltaState);
         workspace.removeSaveParticipant(javaCore);
 
         // wait for the initialization job to finish
         try {
             Platform.getJobManager().join(RubyCore.PLUGIN_ID, null);
         } catch (InterruptedException e) {
             // ignore
         }
 
         // Note: no need to close the Java model as this just removes Java
         // element infos from the Java model cache
     }
     
     /**
 	 * @see ISaveParticipant
 	 */
 	public void saving(ISaveContext context) throws CoreException {
 		
 	    // save variable and container values on snapshot/full save
 		long start = -1;
 //		if (VERBOSE)
 //			start = System.currentTimeMillis();
 		saveVariablesAndContainers();
 //		if (VERBOSE)
 //			traceVariableAndContainers("Saved", start); //$NON-NLS-1$
 		
 		if (context.getKind() == ISaveContext.FULL_SAVE) {
 			// will need delta since this save (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=38658)
 			context.needDelta();
 			
 			// clean up indexes on workspace full save
 			// (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=52347)
 //			IndexManager manager = this.indexManager;
 //			if (manager != null 
 //					// don't force initialization of workspace scope as we could be shutting down
 //					// (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=93941)
 //					&& this.workspaceScope != null) { 
 //				manager.cleanUpIndexes();
 //			}
 		}
 	
 		IProject savedProject = context.getProject();
 		if (savedProject != null) {
 			if (!RubyProject.hasRubyNature(savedProject)) return; // ignore
 			PerProjectInfo info = getPerProjectInfo(savedProject, true /* create info */);
 			saveState(info, context);
 			info.rememberExternalLibTimestamps();
 			return;
 		}
 	
 		ArrayList<IStatus> vStats= null; // lazy initialized
 		ArrayList<PerProjectInfo> values = null;
 		synchronized(this.perProjectInfos) {
 			values = new ArrayList<PerProjectInfo>(this.perProjectInfos.values());
 		}
 		if (values != null) {
 			Iterator<PerProjectInfo> iterator = values.iterator();
 			while (iterator.hasNext()) {
 				try {
 					PerProjectInfo info = iterator.next();
 					saveState(info, context);
 					info.rememberExternalLibTimestamps();
 				} catch (CoreException e) {
 					if (vStats == null)
 						vStats= new ArrayList<IStatus>();
 					vStats.add(e.getStatus());
 				}
 			}
 		}
 		if (vStats != null) {
 			IStatus[] stats= new IStatus[vStats.size()];
 			vStats.toArray(stats);
 			throw new CoreException(new MultiStatus(RubyCore.PLUGIN_ID, IStatus.ERROR, stats, Messages.build_cannotSaveStates, null)); 
 		}
 		
 		// save external libs timestamps
 		this.deltaState.saveExternalLibTimeStamps();
 	}
 
 	private void saveVariablesAndContainers() throws CoreException {
 		File file = getVariableAndContainersFile();
 		DataOutputStream out = null;
 		try {
 			out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
 			out.writeInt(VARIABLES_AND_CONTAINERS_FILE_VERSION);
 			new VariablesAndContainersSaveHelper(out).save();
 		} catch (IOException e) {
 			IStatus status = new Status(IStatus.ERROR, RubyCore.PLUGIN_ID, IStatus.ERROR, "Problems while saving variables and containers", e); //$NON-NLS-1$
 			throw new CoreException(status);
 		} finally {
 			if (out != null) {
 				try {
 					out.close();
 				} catch (IOException e) {
 					// nothing we can do: ignore
 				}
 			}
 		}
 	}
 
 	private void saveState(PerProjectInfo info, ISaveContext context) throws CoreException {
 
 		// passed this point, save actions are non trivial
 		if (context.getKind() == ISaveContext.SNAPSHOT) return;
 		
 		// save built state
 		if (info.triedRead) saveBuiltState(info);
 	}
 	
 	/**
 	 * Saves the built state for the project.
 	 */
 	private void saveBuiltState(PerProjectInfo info) throws CoreException {
 		if (RubyBuilder.DEBUG)
 			System.out.println(Messages.bind(Messages.build_saveStateProgress, info.project.getName())); 
 		File file = getSerializationFile(info.project);
 		if (file == null) return;
 		long t = System.currentTimeMillis();
 		try {
 			DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
 			try {
 				out.writeUTF(RubyCore.PLUGIN_ID);
 				out.writeUTF("STATE"); //$NON-NLS-1$
 				if (info.savedState == null) {
 					out.writeBoolean(false);
 				} else {
 					out.writeBoolean(true);
 					RubyBuilder.writeState(info.savedState, out);
 				}
 			} finally {
 				out.close();
 			}
 		} catch (RuntimeException e) {
 			try {
 				file.delete();
 			} catch(SecurityException se) {
 				// could not delete file: cannot do much more
 			}
 			throw new CoreException(
 				new Status(IStatus.ERROR, RubyCore.PLUGIN_ID, Platform.PLUGIN_ERROR,
 					Messages.bind(Messages.build_cannotSaveState, info.project.getName()), e)); 
 		} catch (IOException e) {
 			try {
 				file.delete();
 			} catch(SecurityException se) {
 				// could not delete file: cannot do much more
 			}
 			throw new CoreException(
 				new Status(IStatus.ERROR, RubyCore.PLUGIN_ID, Platform.PLUGIN_ERROR,
 					Messages.bind(Messages.build_cannotSaveState, info.project.getName()), e)); 
 		}
 		if (RubyBuilder.DEBUG) {
 			t = System.currentTimeMillis() - t;
 			System.out.println(Messages.bind(Messages.build_saveStateComplete, String.valueOf(t))); 
 		}
 	}
 	
 	/**
 	 * Returns the File to use for saving and restoring the last built state for the given project.
 	 */
 	private File getSerializationFile(IProject project) {
 		if (!project.exists()) return null;
 		IPath workingLocation = project.getWorkingLocation(RubyCore.PLUGIN_ID);
 		return workingLocation.append("state.dat").toFile(); //$NON-NLS-1$
 	}
 	
     /**
      * Configure the plugin with respect to option settings defined in
      * ".options" file
      */
     public void configurePluginDebugOptions() {
         if (RubyCore.getPlugin().isDebugging()) {
             String option = Platform.getDebugOption(BUFFER_MANAGER_DEBUG);
             if (option != null) BufferManager.VERBOSE = option.equalsIgnoreCase("true"); //$NON-NLS-1$
 
             option = Platform.getDebugOption(BUILDER_DEBUG);
             if (option != null) RubyBuilder.DEBUG = option.equalsIgnoreCase("true"); //$NON-NLS-1$
 
             option = Platform.getDebugOption(DELTA_DEBUG);
             if (option != null) DeltaProcessor.DEBUG = option.equalsIgnoreCase("true"); //$NON-NLS-1$
 
             option = Platform.getDebugOption(DELTA_DEBUG_VERBOSE);
             if (option != null) DeltaProcessor.VERBOSE = option.equalsIgnoreCase("true"); //$NON-NLS-1$
 
             option = Platform.getDebugOption(RUBYMODEL_DEBUG);
             if (option != null) RubyModelManager.verbose = option.equalsIgnoreCase("true"); //$NON-NLS-1$
 
             option = Platform.getDebugOption(POST_ACTION_DEBUG);
             if (option != null)
                 RubyModelOperation.POST_ACTION_VERBOSE = option.equalsIgnoreCase("true"); //$NON-NLS-1$
 
             // configure performance options
             if (PerformanceStats.ENABLED) {
                 DeltaProcessor.PERF = PerformanceStats.isEnabled(DELTA_LISTENER_PERF);
                 ReconcileWorkingCopyOperation.PERF = PerformanceStats.isEnabled(RECONCILE_PERF);
             }
         }
 
     }
 
     public void contentTypeChanged(ContentTypeChangeEvent event) {
         Util.resetRubyLikeExtensions();        
     }
 
 	public static IRubyElement create(IResource resource, IRubyProject project) {
 		if (resource == null) {
 			return null;
 		}
 		int type = resource.getType();
 		switch (type) {
 			case IResource.PROJECT :
 				return RubyCore.create((IProject) resource);
 			case IResource.FILE :
 				return create((IFile) resource, project);
 			case IResource.FOLDER :
 //				System.err.println("Tried to create a RubyElement for: " + resource.getFullPath().toOSString());
 				return create((IFolder) resource, project);
 //				return null;
 			case IResource.ROOT :
 				return RubyCore.create((IWorkspaceRoot) resource);
 			default :
 				return null;
 		}
 	}
 	
 	/**
 	 * Returns the package fragment or package fragment root corresponding to the given folder,
 	 * its parent or great parent being the given project. 
 	 * or <code>null</code> if unable to associate the given folder with a Java element.
 	 * <p>
 	 * Note that a package fragment root is returned rather than a default package.
 	 * <p>
 	 * Creating a Java element has the side effect of creating and opening all of the
 	 * element's parents if they are not yet open.
 	 */
 	public static IRubyElement create(IFolder folder, IRubyProject project) {
 		if (folder == null) {
 			return null;
 		}
 		IRubyElement element;
 		if (project == null) {
 			project = RubyCore.create(folder.getProject());
 			
 			element = determineIfOnLoadpath(folder, project);
 			if (element == null) {
 				// walk all projects and find one that have the given folder on the load path
 				IRubyProject[] projects;
 				try {
 					projects = RubyModelManager.getRubyModelManager().getRubyModel().getRubyProjects();
 				} catch (RubyModelException e) {
 					return null;
 				}
 				for (int i = 0, length = projects.length; i < length; i++) {
 					project = projects[i];
 					element = determineIfOnLoadpath(folder, project);
 					if (element != null)
 						break;
 				}
 			}
 		} else {
 			element = determineIfOnLoadpath(folder, project);
 		}		
 
 		return element;		
 	}
 	
 	private static IRubyElement determineIfOnLoadpath(IResource resource,
 			IRubyProject project) {
 		// TODO Actually take load paths into account
 		IPath resourcePath = resource.getFullPath();
 		IPath rootPath = project.getPath();
 		if (rootPath.equals(resourcePath)) {
 			return project.getSourceFolderRoot(resource);
 		} else if (rootPath.isPrefixOf(resourcePath)) {
 			SourceFolderRoot root =(SourceFolderRoot) ((RubyProject) project).getFolderSourceFolderRoot(rootPath);
 			if (root == null) return null;
 			
 			IPath pkgPath = resourcePath.removeFirstSegments(rootPath.segmentCount());
 			
 			if (resource.getType() == IResource.FILE) {
 				// if the resource is a file, then remove the last segment which
 				// is the file name in the package
 				pkgPath = pkgPath.removeLastSegments(1);
 			}
 			String[] pkgName = pkgPath.segments();
 			return root.getSourceFolder(pkgName);
 		}
 		return null;
 	}
 
 	public static IRubyScript create(IFile file, IRubyProject project) {
 		if (file == null) {
 			return null;
 		}
 		if (project == null) {
 			project = RubyCore.create(file.getProject());
 		}
 	
 		if (file.getFileExtension() != null) {
 			String name = file.getName();
 			if (org.rubypeople.rdt.internal.core.util.Util.isRubyLikeFileName(name))
 				return createRubyScriptFrom(file, project);
 		}
 		return null;
 	}
 
 	public static IRubyScript createRubyScriptFrom(IFile file, IRubyProject project) {
 		if (file == null) return null;
 
 		if (project == null) {
 			project = RubyCore.create(file.getProject());
 		}
 		ISourceFolder pkg = (ISourceFolder) determineIfOnLoadpath(file, project);
 		if (pkg == null) {
 			// not on classpath - make the root its folder, and a default package
 			ISourceFolderRoot root = project.getSourceFolderRoot(file.getParent());
 			pkg = root.getSourceFolder(ISourceFolder.DEFAULT_PACKAGE_NAME);
 			
 			if (VERBOSE){
 				System.out.println("WARNING : creating unit element outside classpath ("+ Thread.currentThread()+"): " + file.getFullPath()); //$NON-NLS-1$//$NON-NLS-2$
 			}
 		}
 		return pkg.getRubyScript(file.getName());
 	}
 	
 	/*
 	 * Returns all the working copies which have the given owner.
 	 * Adds the working copies of the primary owner if specified.
 	 * Returns null if it has none.
 	 */
 	public IRubyScript[] getWorkingCopies(WorkingCopyOwner owner, boolean addPrimary) {
 		synchronized(this.perWorkingCopyInfos) {
 			IRubyScript[] primaryWCs = addPrimary && owner != DefaultWorkingCopyOwner.PRIMARY 
 				? getWorkingCopies(DefaultWorkingCopyOwner.PRIMARY, false) 
 				: null;
 			Map workingCopyToInfos = this.perWorkingCopyInfos.get(owner);
 			if (workingCopyToInfos == null) return primaryWCs;
 			int primaryLength = primaryWCs == null ? 0 : primaryWCs.length;
 			int size = workingCopyToInfos.size(); // note size is > 0 otherwise pathToPerWorkingCopyInfos would be null
 			IRubyScript[] result = new IRubyScript[primaryLength + size];
 			int index = 0;
 			if (primaryWCs != null) {
 				for (int i = 0; i < primaryLength; i++) {
 					IRubyScript primaryWorkingCopy = primaryWCs[i];
 					IRubyScript workingCopy = new RubyScript((SourceFolder) primaryWorkingCopy.getParent(), primaryWorkingCopy.getElementName(), owner);
 					if (!workingCopyToInfos.containsKey(workingCopy))
 						result[index++] = primaryWorkingCopy;
 				}
 				if (index != primaryLength)
 					System.arraycopy(result, 0, result = new IRubyScript[index+size], 0, index);
 			}
 			Iterator iterator = workingCopyToInfos.values().iterator();
 			while(iterator.hasNext()) {
 				result[index++] = ((RubyModelManager.PerWorkingCopyInfo)iterator.next()).getWorkingCopy();
 			}
 			return result;
 		}		
 	}
 
 	public synchronized String intern(String s) {
 		// make sure to copy the string (so that it doesn't hold on the underlying char[] that might be much bigger than necessary)
 		return (String) this.stringSymbols.add(new String(s));
 		
 		// Note1: String#intern() cannot be used as on some VMs this prevents the string from being garbage collected
 		// Note 2: Instead of using a WeakHashset, one could use a WeakHashMap with the following implementation
 		// 			   This would costs more per entry (one Entry object and one WeakReference more))
 		
 		/*
 		WeakReference reference = (WeakReference) this.symbols.get(s);
 		String existing;
 		if (reference != null && (existing = (String) reference.get()) != null)
 			return existing;
 		this.symbols.put(s, new WeakReference(s));
 		return s;
 		*/	
 	}
 
 	public void doneSaving(ISaveContext context) {
 		// nothing to do		
 	}
 
 	public void prepareToSave(ISaveContext context) throws CoreException {
 		// nothing to do		
 	}
 
 	public void rollback(ISaveContext context) {
 		// nothing to do		
 	}
 
 	public synchronized IPath variableGet(String variableName){
 		// check initialization in progress first
 		HashSet<String> initializations = variableInitializationInProgress();
 		if (initializations.contains(variableName)) {
 			return VARIABLE_INITIALIZATION_IN_PROGRESS;
 		}
 		return this.variables.get(variableName);
 	}
 	
 	/*
 	 * Returns the set of variable names that are being initialized in the current thread.
 	 */
 	private HashSet<String> variableInitializationInProgress() {
 		HashSet<String> initializations = this.variableInitializationInProgress.get();
 		if (initializations == null) {
 			initializations = new HashSet<String>();
 			this.variableInitializationInProgress.set(initializations);
 		}
 		return initializations;
 	}
 
 	/**
 	 * Returns a persisted container from previous session if any
 	 */
 	public IPath getPreviousSessionVariable(String variableName) {
 		IPath previousPath = (IPath)this.previousSessionVariables.get(variableName);
 		if (previousPath != null){
 			if (CP_RESOLVE_VERBOSE){
 				Util.verbose(
 					"CPVariable INIT - reentering access to variable during its initialization, will see previous value\n" + //$NON-NLS-1$
 					"	variable: "+ variableName + '\n' + //$NON-NLS-1$
 					"	previous value: " + previousPath); //$NON-NLS-1$
 				new Exception("<Fake exception>").printStackTrace(System.out); //$NON-NLS-1$
 			}
 			return previousPath;
 		}
 	    return null; // break cycle
 	}
 	
 	public synchronized void variablePut(String variableName, IPath variablePath){		
 
 		// set/unset the initialization in progress
 		HashSet<String> initializations = variableInitializationInProgress();
 		if (variablePath == VARIABLE_INITIALIZATION_IN_PROGRESS) {
 			initializations.add(variableName);
 			
 			// do not write out intermediate initialization value
 			return;
 		} else {
 			initializations.remove(variableName);
 
 			// update cache - do not only rely on listener refresh		
 			if (variablePath == null) {
 				// if path is null, record that the variable was removed to avoid asking the initializer to initialize it again
 				// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=112609
 				this.variables.put(variableName, CP_ENTRY_IGNORE_PATH);
 			} else {
 				this.variables.put(variableName, variablePath);
 			}
 			// discard obsoleted information about previous session
 			this.previousSessionVariables.remove(variableName);
 		}
 	}
 
 	public ILoadpathContainer getLoadpathContainer(IPath containerPath, IRubyProject project) throws RubyModelException {
 
 		ILoadpathContainer container = containerGet(project, containerPath);
 
 		if (container == null) {
 			if (this.batchContainerInitializations) {
 				// avoid deep recursion while initializaing container on workspace restart
 				// (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=60437)
 				this.batchContainerInitializations = false;
 				return initializeAllContainers(project, containerPath);
 			}
 			return initializeContainer(project, containerPath);
 		}
 		return container;			
 	}
 	
 	ILoadpathContainer initializeContainer(IRubyProject project, IPath containerPath) throws RubyModelException {
 
 		ILoadpathContainer container = null;
 		final LoadpathContainerInitializer initializer = RubyCore.getLoadpathContainerInitializer(containerPath.segment(0));
 		if (initializer != null){
 			if (CP_RESOLVE_VERBOSE){
 				Util.verbose(
 					"CPContainer INIT - triggering initialization\n" + //$NON-NLS-1$
 					"	project: " + project.getElementName() + '\n' + //$NON-NLS-1$
 					"	container path: " + containerPath + '\n' + //$NON-NLS-1$
 					"	initializer: " + initializer + '\n' + //$NON-NLS-1$
 					"	invocation stack trace:"); //$NON-NLS-1$
 				new Exception("<Fake exception>").printStackTrace(System.out); //$NON-NLS-1$
 			}
 //			PerformanceStats stats = null;
 //			if(RubyModelManager.PERF_CONTAINER_INITIALIZER) {
 //				stats = PerformanceStats.getStats(RubyModelManager.CONTAINER_INITIALIZER_PERF, this);
 //				stats.startRun(containerPath + " of " + project.getPath()); //$NON-NLS-1$
 //			}
 			containerPut(project, containerPath, CONTAINER_INITIALIZATION_IN_PROGRESS); // avoid initialization cycles
 			boolean ok = false;
 			try {
 				// let OperationCanceledException go through
 				// (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=59363)
 				initializer.initialize(containerPath, project);
 				
 				// retrieve value (if initialization was successful)
 				container = containerGet(project, containerPath);
 				if (container == CONTAINER_INITIALIZATION_IN_PROGRESS) return null; // break cycle
 				ok = true;
 			} catch (CoreException e) {
 				if (e instanceof RubyModelException) {
 					throw (RubyModelException) e;
 				} else {
 					throw new RubyModelException(e);
 				}
 			} catch (RuntimeException e) {
 				if (RubyModelManager.CP_RESOLVE_VERBOSE) {
 					e.printStackTrace();
 				}
 				throw e;
 			} catch (Error e) {
 				if (RubyModelManager.CP_RESOLVE_VERBOSE) {
 					e.printStackTrace();
 				}
 				throw e;
 			} finally {
 //				if(RubyModelManager.PERF_CONTAINER_INITIALIZER) {
 //					stats.endRun();
 //				}
 				if (!ok) {
 					// just remove initialization in progress and keep previous session container so as to avoid a full build
 					// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=92588
 					containerRemoveInitializationInProgress(project, containerPath); 
 					if (CP_RESOLVE_VERBOSE) {
 						if (container == CONTAINER_INITIALIZATION_IN_PROGRESS) {
 							Util.verbose(
 								"CPContainer INIT - FAILED (initializer did not initialize container)\n" + //$NON-NLS-1$
 								"	project: " + project.getElementName() + '\n' + //$NON-NLS-1$
 								"	container path: " + containerPath + '\n' + //$NON-NLS-1$
 								"	initializer: " + initializer); //$NON-NLS-1$
 							
 						} else {
 							Util.verbose(
 								"CPContainer INIT - FAILED (see exception above)\n" + //$NON-NLS-1$
 								"	project: " + project.getElementName() + '\n' + //$NON-NLS-1$
 								"	container path: " + containerPath + '\n' + //$NON-NLS-1$
 								"	initializer: " + initializer); //$NON-NLS-1$
 						}
 					}
 				}
 			}
 			if (CP_RESOLVE_VERBOSE){
 				StringBuffer buffer = new StringBuffer();
 				buffer.append("CPContainer INIT - after resolution\n"); //$NON-NLS-1$
 				buffer.append("	project: " + project.getElementName() + '\n'); //$NON-NLS-1$
 				buffer.append("	container path: " + containerPath + '\n'); //$NON-NLS-1$
 				if (container != null){
 					buffer.append("	container: "+container.getDescription()+" {\n"); //$NON-NLS-2$//$NON-NLS-1$
 					ILoadpathEntry[] entries = container.getLoadpathEntries();
 					if (entries != null){
 						for (int i = 0; i < entries.length; i++){
 							buffer.append("		" + entries[i] + '\n'); //$NON-NLS-1$
 						}
 					}
 					buffer.append("	}");//$NON-NLS-1$
 				} else {
 					buffer.append("	container: {unbound}");//$NON-NLS-1$
 				}
 				Util.verbose(buffer.toString());
 			}
 		} else {
 			if (CP_RESOLVE_VERBOSE){
 				Util.verbose(
 					"CPContainer INIT - no initializer found\n" + //$NON-NLS-1$
 					"	project: " + project.getElementName() + '\n' + //$NON-NLS-1$
 					"	container path: " + containerPath); //$NON-NLS-1$
 			}
 		}
 		return container;
 	}
 	
 	private void containerRemoveInitializationInProgress(IRubyProject project, IPath containerPath) {
 		HashSet<IPath> projectInitializations = containerInitializationInProgress(project);
 		projectInitializations.remove(containerPath);
 		if (projectInitializations.size() == 0) {
 			Map initializations = this.containerInitializationInProgress.get();
 			initializations.remove(project);
 		}
 	}
 
 	public synchronized void containerPut(IRubyProject project, IPath containerPath, ILoadpathContainer container){
 
 		// set/unset the initialization in progress
 		if (container == CONTAINER_INITIALIZATION_IN_PROGRESS) {
 			HashSet<IPath> projectInitializations = containerInitializationInProgress(project);
 			projectInitializations.add(containerPath);
 			
 			// do not write out intermediate initialization value
 			return;
 		} else {
 			containerRemoveInitializationInProgress(project, containerPath);
 
 			Map<IPath, ILoadpathContainer> projectContainers = this.containers.get(project);	
  			if (projectContainers == null){
 				projectContainers = new HashMap<IPath, ILoadpathContainer>(1);
 				this.containers.put(project, projectContainers);
 			}
 	
 			if (container == null) {
 				projectContainers.remove(containerPath);
 			} else {
   				projectContainers.put(containerPath, container);
 			}
 			// discard obsoleted information about previous session
 			Map previousContainers = (Map)this.previousSessionContainers.get(project);
 			if (previousContainers != null){
 				previousContainers.remove(containerPath);
 			}
 		}
 		// container values are persisted in preferences during save operations, see #saving(ISaveContext)
 	}
 	
 	/*
 	 * Initialize all container at the same time as the given container.
 	 * Return the container for the given path and project.
 	 */
 	private ILoadpathContainer initializeAllContainers(IRubyProject javaProjectToInit, IPath containerToInit) throws RubyModelException {
 		if (CP_RESOLVE_VERBOSE) {
 			Util.verbose(
 				"CPContainer INIT - batching containers initialization\n" + //$NON-NLS-1$
 				"	project to init: " + javaProjectToInit.getElementName() + '\n' + //$NON-NLS-1$
 				"	container path to init: " + containerToInit); //$NON-NLS-1$
 		}
 
 		// collect all container paths
 		final HashMap<IRubyProject, HashSet<IPath>> allContainerPaths = new HashMap<IRubyProject, HashSet<IPath>>();
 		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
 		for (int i = 0, length = projects.length; i < length; i++) {
 			IProject project = projects[i];
 			if (!RubyProject.hasRubyNature(project)) continue;
 			IRubyProject javaProject = new RubyProject(project, getRubyModel());
 			HashSet<IPath> paths = null;
 			ILoadpathEntry[] rawClasspath = javaProject.getRawLoadpath();
 			for (int j = 0, length2 = rawClasspath.length; j < length2; j++) {
 				ILoadpathEntry entry = rawClasspath[j];
 				IPath path = entry.getPath();
 				if (entry.getEntryKind() == ILoadpathEntry.CPE_CONTAINER
 						&& containerGet(javaProject, path) == null) {
 					if (paths == null) {
 						paths = new HashSet<IPath>();
 						allContainerPaths.put(javaProject, paths);
 					}
 					paths.add(path);
 				}
 			}
 			/* TODO (frederic) put back when JDT/UI dummy project will be thrown away...
 			 * See https://bugs.eclipse.org/bugs/show_bug.cgi?id=97524
 			 *
 			if (javaProject.equals(javaProjectToInit)) {
 				if (paths == null) {
 					paths = new HashSet();
 					allContainerPaths.put(javaProject, paths);
 				}
 				paths.add(containerToInit);
 			}
 			*/
 		}
 		// TODO (frederic) remove following block when JDT/UI dummy project will be thrown away...
 		HashSet<IPath> containerPaths = allContainerPaths.get(javaProjectToInit);
 		if (containerPaths == null) {
 			containerPaths = new HashSet<IPath>();
 			allContainerPaths.put(javaProjectToInit, containerPaths);
 		}
 		containerPaths.add(containerToInit);
 		// end block
 		
 		// mark all containers as being initialized
 		this.containerInitializationInProgress.set(allContainerPaths);
 		
 		// initialize all containers
 		boolean ok = false;
 		try {
 			// if possible run inside an IWokspaceRunnable with AVOID_UPATE to avoid unwanted builds
 			// (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=118507)
 			IWorkspaceRunnable runnable = 				
 				new IWorkspaceRunnable() {
 					public void run(IProgressMonitor monitor) throws CoreException {
 						Set<IRubyProject> keys = allContainerPaths.keySet();
 						int length = keys.size();
 						IRubyProject[] javaProjects = new IRubyProject[length]; // clone as the following will have a side effect
 						keys.toArray(javaProjects);
 						for (int i = 0; i < length; i++) {
 							IRubyProject javaProject = javaProjects[i];
 							HashSet pathSet = allContainerPaths.get(javaProject);
 							if (pathSet == null) continue;
 							int length2 = pathSet.size();
 							IPath[] paths = new IPath[length2];
 							pathSet.toArray(paths); // clone as the following will have a side effect
 							for (int j = 0; j < length2; j++) {
 								IPath path = paths[j];
 								initializeContainer(javaProject, path);
 							}
 						}
 					}
 				};
 			IWorkspace workspace = ResourcesPlugin.getWorkspace();
 			if (workspace.isTreeLocked())
 				runnable.run(null/*no progress available*/);
 			else
 				workspace.run(
 					runnable,
 					null/*don't take any lock*/,
 					IWorkspace.AVOID_UPDATE,
 					null/*no progress available here*/);
 			ok = true;
 		} catch (CoreException e) {
 			// ignore
 			Util.log(e, "Exception while initializing all containers"); //$NON-NLS-1$
 		} finally {
 			if (!ok) { 
 				// if we're being traversed by an exception, ensure that that containers are 
 				// no longer marked as initialization in progress
 				// (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=66437)
 				this.containerInitializationInProgress.set(null);
 			}
 		}
 		
 		return containerGet(javaProjectToInit, containerToInit);
 	}
 
 	
 	public synchronized ILoadpathContainer containerGet(IRubyProject project, IPath containerPath) {	
 		// check initialization in progress first
 		HashSet<IPath> projectInitializations = containerInitializationInProgress(project);
 		if (projectInitializations.contains(containerPath)) {
 			return CONTAINER_INITIALIZATION_IN_PROGRESS;
 		}
 		
 		Map projectContainers = this.containers.get(project);
 		if (projectContainers == null){
 			return null;
 		}
 		ILoadpathContainer container = (ILoadpathContainer)projectContainers.get(containerPath);
 		return container;
 	}
 	
 	/*
 	 * Returns the set of container paths for the given project that are being initialized in the current thread.
 	 */
 	private HashSet<IPath> containerInitializationInProgress(IRubyProject project) {
 		Map<IRubyProject, HashSet<IPath>> initializations = this.containerInitializationInProgress.get();
 		if (initializations == null) {
 			initializations = new HashMap<IRubyProject, HashSet<IPath>>();
 			this.containerInitializationInProgress.set(initializations);
 		}
 		HashSet<IPath> projectInitializations = initializations.get(project);
 		if (projectInitializations == null) {
 			projectInitializations = new HashSet<IPath>();
 			initializations.put(project, projectInitializations);
 		}
 		return projectInitializations;
 	}
 
 	/**
 	 * Returns a persisted container from previous session if any. Note that it is not the original container from previous
 	 * session (i.e. it did not get serialized) but rather a summary of its entries recreated for CP initialization purpose.
 	 * As such it should not be stored into container caches.
 	 */
 	public ILoadpathContainer getPreviousSessionContainer(IPath containerPath, IRubyProject project) {
 			Map previousContainerValues = (Map)this.previousSessionContainers.get(project);
 			if (previousContainerValues != null){
 				ILoadpathContainer previousContainer = (ILoadpathContainer)previousContainerValues.get(containerPath);
 			    if (previousContainer != null) {
 					if (RubyModelManager.CP_RESOLVE_VERBOSE){
 						StringBuffer buffer = new StringBuffer();
 						buffer.append("CPContainer INIT - reentering access to project container during its initialization, will see previous value\n"); //$NON-NLS-1$ 
 						buffer.append("	project: " + project.getElementName() + '\n'); //$NON-NLS-1$
 						buffer.append("	container path: " + containerPath + '\n'); //$NON-NLS-1$
 						buffer.append("	previous value: "); //$NON-NLS-1$
 						buffer.append(previousContainer.getDescription());
 						buffer.append(" {\n"); //$NON-NLS-1$
 						ILoadpathEntry[] entries = previousContainer.getLoadpathEntries();
 						if (entries != null){
 							for (int j = 0; j < entries.length; j++){
 								buffer.append(" 		"); //$NON-NLS-1$
 								buffer.append(entries[j]); 
 								buffer.append('\n'); 
 							}
 						}
 						buffer.append(" 	}"); //$NON-NLS-1$
 						Util.verbose(buffer.toString());
 						new Exception("<Fake exception>").printStackTrace(System.out); //$NON-NLS-1$
 					}			    
 					return previousContainer;
 			    }
 			}
 		    return null; // break cycle if none found
 	}
 
 	/*
 	 * The given project is being removed. Remove all containers for this project from the cache.
 	 */
 	public void containerRemove(IRubyProject project) {
 		Map initializations = this.containerInitializationInProgress.get();
 		if (initializations != null) {
 			initializations.remove(project);
 		}
 		this.containers.remove(project);
 	}
 
 	/**
 	 * Sets the last built state for the given project, or null to reset it.
 	 */
 	public void setLastBuiltState(IProject project, Object state) {
 		if (RubyProject.hasRubyNature(project)) {
 			// should never be requested on non-Ruby projects
 			PerProjectInfo info = getPerProjectInfo(project, true /*create if missing*/);
 			info.triedRead = true; // no point trying to re-read once using setter
 			info.savedState = state;
 		}
 		if (state == null) { // delete state file to ensure a full build happens if the workspace crashes
 			try {
 				File file = getSerializationFile(project);
 				if (file != null && file.exists())
 					file.delete();
 			} catch(SecurityException se) {
 				// could not delete file: cannot do much more
 			}
 		}
 	}
 	
 	/*
 	 * Optimize startup case where a container for 1 project is initialized at a time with the same entries as on shutdown.
 	 */
 	public boolean containerPutIfInitializingWithSameEntries(IPath containerPath, IRubyProject[] projects, ILoadpathContainer[] respectiveContainers) {
 			int projectLength = projects.length;
 			if (projectLength != 1) 
 				return false;
 			final ILoadpathContainer container = respectiveContainers[0];
 			if (container == null)
 				return false;
 			IRubyProject project = projects[0];
 			if (!containerInitializationInProgress(project).contains(containerPath))
 				return false;
 			ILoadpathContainer previousSessionContainer = getPreviousSessionContainer(containerPath, project);
 			final ILoadpathEntry[] newEntries = container.getLoadpathEntries();
 			if (previousSessionContainer == null) 
 				if (newEntries.length == 0) {
 					containerPut(project, containerPath, container);
 					return true;
 				} else {
 					return false;
 				}
 			final ILoadpathEntry[] oldEntries = previousSessionContainer.getLoadpathEntries();
 			if (oldEntries.length != newEntries.length) 
 				return false;
 			for (int i = 0, length = newEntries.length; i < length; i++) {
 				if (!newEntries[i].equals(oldEntries[i])) {
 					if (CP_RESOLVE_VERBOSE) {
 						Util.verbose(
 							"CPContainer SET  - missbehaving container\n" + //$NON-NLS-1$
 							"	container path: " + containerPath + '\n' + //$NON-NLS-1$
 							"	projects: {" +//$NON-NLS-1$
 							org.rubypeople.rdt.internal.compiler.util.Util.toString(
 								projects, 
 								new org.rubypeople.rdt.internal.compiler.util.Util.Displayable(){ 
 									public String displayString(Object o) { return ((IRubyProject) o).getElementName(); }
 								}) +
 							"}\n	values on previous session: {\n"  +//$NON-NLS-1$
 							org.rubypeople.rdt.internal.compiler.util.Util.toString(
 								respectiveContainers, 
 								new org.rubypeople.rdt.internal.compiler.util.Util.Displayable(){ 
 									public String displayString(Object o) { 
 										StringBuffer buffer = new StringBuffer("		"); //$NON-NLS-1$
 										if (o == null) {
 											buffer.append("<null>"); //$NON-NLS-1$
 											return buffer.toString();
 										}
 										buffer.append(container.getDescription());
 										buffer.append(" {\n"); //$NON-NLS-1$
 										for (int j = 0; j < oldEntries.length; j++){
 											buffer.append(" 			"); //$NON-NLS-1$
 											buffer.append(oldEntries[j]); 
 											buffer.append('\n'); 
 										}
 										buffer.append(" 		}"); //$NON-NLS-1$
 										return buffer.toString();
 									}
 								}) +
 							"}\n	new values: {\n"  +//$NON-NLS-1$
 							org.rubypeople.rdt.internal.compiler.util.Util.toString(
 								respectiveContainers, 
 								new org.rubypeople.rdt.internal.compiler.util.Util.Displayable(){ 
 									public String displayString(Object o) { 
 										StringBuffer buffer = new StringBuffer("		"); //$NON-NLS-1$
 										if (o == null) {
 											buffer.append("<null>"); //$NON-NLS-1$
 											return buffer.toString();
 										}
 										buffer.append(container.getDescription());
 										buffer.append(" {\n"); //$NON-NLS-1$
 										for (int j = 0; j < newEntries.length; j++){
 											buffer.append(" 			"); //$NON-NLS-1$
 											buffer.append(newEntries[j]); 
 											buffer.append('\n'); 
 										}
 										buffer.append(" 		}"); //$NON-NLS-1$
 										return buffer.toString();
 									}
 								}) +
 							"\n	}"); //$NON-NLS-1$
 					}
 					return false;
 				}
 			}
 			containerPut(project, containerPath, container);
 			return true;
 		}
 
 	/*
 	 * Internal updating of a variable values (null path meaning removal), allowing to change multiple variable values at once.
 	 */
 	public void updateVariableValues(
 		String[] variableNames,
 		IPath[] variablePaths,
 		boolean updatePreferences,
 		IProgressMonitor monitor) throws RubyModelException {
 	
 		if (monitor != null && monitor.isCanceled()) return;
 		
 		if (CP_RESOLVE_VERBOSE){
 			Util.verbose(
 				"CPVariable SET  - setting variables\n" + //$NON-NLS-1$
 				"	variables: " + org.rubypeople.rdt.internal.compiler.util.Util.toString(variableNames) + '\n' +//$NON-NLS-1$
 				"	values: " + org.rubypeople.rdt.internal.compiler.util.Util.toString(variablePaths)); //$NON-NLS-1$
 		}
 		
 		if (variablePutIfInitializingWithSameValue(variableNames, variablePaths))
 			return;
 
 		int varLength = variableNames.length;
 		
 		// gather classpath information for updating
 		final HashMap affectedProjectClasspaths = new HashMap(5);
 		IRubyModel model = getRubyModel();
 	
 		// filter out unmodified variables
 		int discardCount = 0;
 		for (int i = 0; i < varLength; i++){
 			String variableName = variableNames[i];
 			IPath oldPath = this.variableGet(variableName); // if reentering will provide previous session value 
 			if (oldPath == VARIABLE_INITIALIZATION_IN_PROGRESS){
 //				IPath previousPath = (IPath)this.previousSessionVariables.get(variableName);
 //				if (previousPath != null){
 //					if (CP_RESOLVE_VERBOSE){
 //						Util.verbose(
 //							"CPVariable INIT - reentering access to variable during its initialization, will see previous value\n" +
 //							"	variable: "+ variableName + '\n' +
 //							"	previous value: " + previousPath);
 //					}
 //					this.variablePut(variableName, previousPath); // replace value so reentering calls are seeing old value
 //				}
 				oldPath = null;  //33695 - cannot filter out restored variable, must update affected project to reset cached CP
 			}
 			if (oldPath != null && oldPath.equals(variablePaths[i])){
 				variableNames[i] = null;
 				discardCount++;
 			}
 		}
 		if (discardCount > 0){
 			if (discardCount == varLength) return;
 			int changedLength = varLength - discardCount;
 			String[] changedVariableNames = new String[changedLength];
 			IPath[] changedVariablePaths = new IPath[changedLength];
 			for (int i = 0, index = 0; i < varLength; i++){
 				if (variableNames[i] != null){
 					changedVariableNames[index] = variableNames[i];
 					changedVariablePaths[index] = variablePaths[i];
 					index++;
 				}
 			}
 			variableNames = changedVariableNames;
 			variablePaths = changedVariablePaths;
 			varLength = changedLength;
 		}
 		
 		if (monitor != null && monitor.isCanceled()) return;
 
 		if (model != null) {
 			IRubyProject[] projects = model.getRubyProjects();
 			nextProject : for (int i = 0, projectLength = projects.length; i < projectLength; i++){
 				RubyProject project = (RubyProject) projects[i];
 						
 				// check to see if any of the modified variables is present on the loadpath
 				ILoadpathEntry[] classpath = project.getRawLoadpath();
 				for (int j = 0, cpLength = classpath.length; j < cpLength; j++){
 					
 					ILoadpathEntry entry = classpath[j];
 					for (int k = 0; k < varLength; k++){
 	
 						String variableName = variableNames[k];						
 						if (entry.getEntryKind() ==  ILoadpathEntry.CPE_VARIABLE){
 	
 							if (variableName.equals(entry.getPath().segment(0))){
 								affectedProjectClasspaths.put(project, project.getResolvedLoadpath(true/*ignoreUnresolvedEntry*/, false/*don't generateMarkerOnError*/, false/*don't returnResolutionInProgress*/));
 								continue nextProject;
 							}
 						}												
 					}
 				}
 			}
 		}
 		// update variables
 		for (int i = 0; i < varLength; i++){
 			variablePut(variableNames[i], variablePaths[i]);
 			if (updatePreferences)
 				variablePreferencesPut(variableNames[i], variablePaths[i]);
 		}
 		final String[] dbgVariableNames = variableNames;
 				
 		// update affected project loadpaths
 		if (!affectedProjectClasspaths.isEmpty()) {
 			try {
 				final boolean canChangeResources = !ResourcesPlugin.getWorkspace().isTreeLocked();
 				RubyCore.run(
 					new IWorkspaceRunnable() {
 						public void run(IProgressMonitor progressMonitor) throws CoreException {
 							// propagate loadpath change
 							Iterator projectsToUpdate = affectedProjectClasspaths.keySet().iterator();
 							while (projectsToUpdate.hasNext()) {
 			
 								if (progressMonitor != null && progressMonitor.isCanceled()) return;
 			
 								RubyProject affectedProject = (RubyProject) projectsToUpdate.next();
 
 								if (CP_RESOLVE_VERBOSE){
 									Util.verbose(
 										"CPVariable SET  - updating affected project due to setting variables\n" + //$NON-NLS-1$
 										"	project: " + affectedProject.getElementName() + '\n' + //$NON-NLS-1$
 										"	variables: " + org.rubypeople.rdt.internal.compiler.util.Util.toString(dbgVariableNames)); //$NON-NLS-1$
 								}
 
 								affectedProject
 									.setRawLoadpath(
 										affectedProject.getRawLoadpath(),
 										SetLoadpathOperation.DO_NOT_SET_OUTPUT,
 										null, // don't call beginTask on the monitor (see http://bugs.eclipse.org/bugs/show_bug.cgi?id=3717)
 										canChangeResources, 
 										(ILoadpathEntry[]) affectedProjectClasspaths.get(affectedProject),
 										false, // updating - no need for early validation
 										false); // updating - no need to save
 							}
 						}
 					},
 					null/*no need to lock anything*/,
 					monitor);
 			} catch (CoreException e) {
 				if (CP_RESOLVE_VERBOSE){
 					Util.verbose(
 						"CPVariable SET  - FAILED DUE TO EXCEPTION\n" + //$NON-NLS-1$
 						"	variables: " + org.rubypeople.rdt.internal.compiler.util.Util.toString(dbgVariableNames), //$NON-NLS-1$
 						System.err); 
 					e.printStackTrace();
 				}
 				if (e instanceof RubyModelException) {
 					throw (RubyModelException)e;
 				} else {
 					throw new RubyModelException(e);
 				}
 			}
 		}
 	}
 
 	private void variablePreferencesPut(String variableName, IPath variablePath) {
 		String variableKey = CP_VARIABLE_PREFERENCES_PREFIX+variableName;
 		if (variablePath == null) {
 			this.variablesWithInitializer.remove(variableName);
 			getInstancePreferences().remove(variableKey);
 		} else {
 			getInstancePreferences().put(variableKey, variablePath.toString());
 		}
 		try {
 			getInstancePreferences().flush();
 		} catch (BackingStoreException e) {
 			// ignore exception
 		}
 	}
 	
 	/**
 	 * Get workpsace eclipse preference for JavaCore plugin.
 	 */
 	public IEclipsePreferences getInstancePreferences() {
 		return preferencesLookup[PREF_INSTANCE];
 	}
 
 	/*
 	 * Optimize startup case where 1 variable is initialized at a time with the same value as on shutdown.
 	 */
 	public boolean variablePutIfInitializingWithSameValue(String[] variableNames, IPath[] variablePaths) {
 		if (variableNames.length != 1)
 			return false;
 		String variableName = variableNames[0];
 		IPath oldPath = getPreviousSessionVariable(variableName);
 		if (oldPath == null)
 			return false;
 		IPath newPath = variablePaths[0];
 		if (!oldPath.equals(newPath))
 			return false;
 		variablePut(variableName, newPath);
 		return true;
 	}
 	
 	private final class VariablesAndContainersLoadHelper {
 
 		private static final int ARRAY_INCREMENT = 200;
 
 		private ILoadpathEntry[] allLoadpathEntries;
 		private int allLoadpathEntryCount;
 
 		private final Map allPaths; // String -> IPath
 
 		private String[] allStrings;
 		private int allStringsCount;
 
 		private final DataInputStream in;
 
 		VariablesAndContainersLoadHelper(DataInputStream in) {
 			super();
 			this.allLoadpathEntries = null;
 			this.allLoadpathEntryCount = 0;
 			this.allPaths = new HashMap();
 			this.allStrings = null;
 			this.allStringsCount = 0;
 			this.in = in;
 		}
 
 		void load() throws IOException {
 			loadProjects(RubyModelManager.this.getRubyModel());
 			loadVariables();
 		}
 
 		private boolean loadBoolean() throws IOException {
 			return this.in.readBoolean();
 		}
 
 		private ILoadpathEntry[] loadLoadpathEntries() throws IOException {
 			int count = loadInt();
 			ILoadpathEntry[] entries = new ILoadpathEntry[count];
 
 			for (int i = 0; i < count; ++i)
 				entries[i] = loadLoadpathEntry();
 
 			return entries;
 		}
 
 		private ILoadpathEntry loadLoadpathEntry() throws IOException {
 			int id = loadInt();
 
 			if (id < 0 || id > this.allLoadpathEntryCount)
 				throw new IOException("Unexpected loadpathentry id"); //$NON-NLS-1$
 
 			if (id < this.allLoadpathEntryCount)
 				return this.allLoadpathEntries[id];
 
 			int entryKind = loadInt();
 			IPath path = loadPath();
 			IPath[] inclusionPatterns = loadPaths();
 			IPath[] exclusionPatterns = loadPaths();
 			boolean isExported = loadBoolean();
 
 			ILoadpathEntry entry = new LoadpathEntry(entryKind,
 					path, inclusionPatterns, exclusionPatterns, isExported);
 
 			ILoadpathEntry[] array = this.allLoadpathEntries;
 
 			if (array == null || id == array.length) {
 				array = new ILoadpathEntry[id + ARRAY_INCREMENT];
 
 				if (id != 0)
 					System.arraycopy(this.allLoadpathEntries, 0, array, 0, id);
 
 				this.allLoadpathEntries = array;
 			}
 
 			array[id] = entry;
 			this.allLoadpathEntryCount = id + 1;
 
 			return entry;
 		}
 
 		private void loadContainers(IRubyProject project) throws IOException {
 			boolean projectIsAccessible = project.getProject().isAccessible();
 			int count = loadInt();
 			for (int i = 0; i < count; ++i) {
 				IPath path = loadPath();
 				ILoadpathEntry[] entries = loadLoadpathEntries();
 				
 				if (!projectIsAccessible) 
 					// avoid leaking deleted project's persisted container,
 					// but still read the container as it is is part of the file format
 					continue; 
 
 				ILoadpathContainer container = new PersistedLoadpathContainer(project, path, entries);
 
 				RubyModelManager.this.containerPut(project, path, container);
 
 				Map oldContainers = (Map) RubyModelManager.this.previousSessionContainers.get(project);
 
 				if (oldContainers == null) {
 					oldContainers = new HashMap();
 					RubyModelManager.this.previousSessionContainers.put(project, oldContainers);
 				}
 
 				oldContainers.put(path, container);
 			}
 		}
 
 		private int loadInt() throws IOException {
 			return this.in.readInt();
 		}
 
 		private IPath loadPath() throws IOException {
 			if (loadBoolean())
 				return null;
 
 			String portableString = loadString();
 			IPath path = (IPath) this.allPaths.get(portableString);
 
 			if (path == null) {
 				path = Path.fromPortableString(portableString);
 				this.allPaths.put(portableString, path);
 			}
 
 			return path;
 		}
 
 		private IPath[] loadPaths() throws IOException {
 			int count = loadInt();
 			IPath[] pathArray = new IPath[count];
 
 			for (int i = 0; i < count; ++i)
 				pathArray[i] = loadPath();
 
 			return pathArray;
 		}
 
 		private void loadProjects(IRubyModel model) throws IOException {
 			int count = loadInt();
 
 			for (int i = 0; i < count; ++i) {
 				String projectName = loadString();
 
 				loadContainers(model.getRubyProject(projectName));
 			}
 		}
 
 		private String loadString() throws IOException {
 			int id = loadInt();
 
 			if (id < 0 || id > this.allStringsCount)
 				throw new IOException("Unexpected string id"); //$NON-NLS-1$
 
 			if (id < this.allStringsCount)
 				return this.allStrings[id];
 
 			String string = this.in.readUTF();
 			String[] array = this.allStrings;
 
 			if (array == null || id == array.length) {
 				array = new String[id + ARRAY_INCREMENT];
 
 				if (id != 0)
 					System.arraycopy(this.allStrings, 0, array, 0, id);
 
 				this.allStrings = array;
 			}
 
 			array[id] = string;
 			this.allStringsCount = id + 1;
 
 			return string;
 		}
 
 		private void loadVariables() throws IOException {
 			int size = loadInt();
 			Map loadedVars = new HashMap(size);
 
 			for (int i = 0; i < size; ++i) {
 				String varName = loadString();
 				IPath varPath = loadPath();
 
 				if (varPath != null)
 					loadedVars.put(varName, varPath);
 			}
 
 			RubyModelManager.this.previousSessionVariables.putAll(loadedVars);
 			RubyModelManager.this.variables.putAll(loadedVars);
 		}
 	}
 	
 	private static final class PersistedLoadpathContainer implements ILoadpathContainer {
 
 		private final IPath containerPath;
 		private final ILoadpathEntry[] entries;
 		private final IRubyProject project;
 
 		PersistedLoadpathContainer(IRubyProject project, IPath containerPath, ILoadpathEntry[] entries) {
 			super();
 			this.containerPath = containerPath;
 			this.entries = entries;
 			this.project = project;
 		}
 
 		public ILoadpathEntry[] getLoadpathEntries() {
 			return entries;
 		}
 
 		public String getDescription() {
 			return "Persisted container [" + containerPath //$NON-NLS-1$
 					+ " for project [" + project.getElementName() //$NON-NLS-1$
 					+ "]]"; //$NON-NLS-1$  
 		}
 
 		public int getKind() {
 			return 0;
 		}
 
 		public IPath getPath() {
 			return containerPath;
 		}
 
 		public String toString() {
 			return getDescription();
 		}
 	}
 	
 	private final class VariablesAndContainersSaveHelper {
 
 		private final HashtableOfObjectToInt loadpathEntryIds; // ILoadpathEntry -> int
 		private final DataOutputStream out;
 		private final HashtableOfObjectToInt stringIds; // Strings -> int
 
 		VariablesAndContainersSaveHelper(DataOutputStream out) {
 			super();
 			this.loadpathEntryIds = new HashtableOfObjectToInt();
 			this.out = out;
 			this.stringIds = new HashtableOfObjectToInt();
 		}
 
 		void save() throws IOException, RubyModelException {
 			saveProjects(RubyModelManager.this.getRubyModel().getRubyProjects());
 			
 			// remove variables that should not be saved
 			HashMap varsToSave = null;
 			Iterator iterator = RubyModelManager.this.variables.entrySet().iterator();
 			IEclipsePreferences defaultPreferences = getDefaultPreferences();
 			while (iterator.hasNext()) {
 				Map.Entry entry = (Map.Entry) iterator.next();
 				String varName = (String) entry.getKey();
 				if (defaultPreferences.get(CP_VARIABLE_PREFERENCES_PREFIX + varName, null) != null // don't save classpath variables from the default preferences as there is no delta if they are removed
 					|| CP_ENTRY_IGNORE_PATH.equals(entry.getValue())) {
 					
 					if (varsToSave == null)
 						varsToSave = new HashMap(RubyModelManager.this.variables);
 					varsToSave.remove(varName);
 				}
 					
 			}
 			
 			saveVariables(varsToSave != null ? varsToSave : RubyModelManager.this.variables);
 		}
 
 		private void saveLoadpathEntries(ILoadpathEntry[] entries)
 				throws IOException {
 			int count = entries == null ? 0 : entries.length;
 
 			saveInt(count);
 			for (int i = 0; i < count; ++i)
 				saveLoadpathEntry(entries[i]);
 		}
 
 		private void saveLoadpathEntry(ILoadpathEntry entry)
 				throws IOException {
 			if (saveNewId(entry, this.loadpathEntryIds)) {
 				saveInt(entry.getEntryKind());
 				savePath(entry.getPath());
 				savePaths(entry.getInclusionPatterns());
 				savePaths(entry.getExclusionPatterns());
 				this.out.writeBoolean(entry.isExported());
 			}
 		}
 
 		private void saveContainers(IRubyProject project, Map containerMap)
 				throws IOException {
 			saveInt(containerMap.size());
 
 			for (Iterator i = containerMap.entrySet().iterator(); i.hasNext();) {
 				Entry entry = (Entry) i.next();
 				IPath path = (IPath) entry.getKey();
 				ILoadpathContainer container = (ILoadpathContainer) entry.getValue();
 				ILoadpathEntry[] cpEntries = null;
 
 				if (container == null) {
 					// container has not been initialized yet, use previous
 					// session value
 					// (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=73969)
 					container = RubyModelManager.this.getPreviousSessionContainer(path, project);
 				}
 
 				if (container != null)
 					cpEntries = container.getLoadpathEntries();
 
 				savePath(path);
 				saveLoadpathEntries(cpEntries);
 			}
 		}
 
 		private void saveInt(int value) throws IOException {
 			this.out.writeInt(value);
 		}
 
 		private boolean saveNewId(Object key, HashtableOfObjectToInt map) throws IOException {
 			int id = map.get(key);
 
 			if (id == -1) {
 				int newId = map.size();
 
 				map.put(key, newId);
 
 				saveInt(newId);
 
 				return true;
 			} else {
 				saveInt(id);
 
 				return false;
 			}
 		}
 
 		private void savePath(IPath path) throws IOException {
 			if (path == null) {
 				this.out.writeBoolean(true);
 			} else {
 				this.out.writeBoolean(false);
 				saveString(path.toPortableString());
 			}
 		}
 
 		private void savePaths(IPath[] paths) throws IOException {
 			int count = paths == null ? 0 : paths.length;
 
 			saveInt(count);
 			for (int i = 0; i < count; ++i)
 				savePath(paths[i]);
 		}
 
 		private void saveProjects(IRubyProject[] projects) throws IOException,
 				RubyModelException {
 			int count = projects.length;
 
 			saveInt(count);
 
 			for (int i = 0; i < count; ++i) {
 				IRubyProject project = projects[i];
 
 				saveString(project.getElementName());
 
 				Map containerMap = (Map) RubyModelManager.this.containers.get(project);
 
 				if (containerMap == null) {
 					containerMap = Collections.EMPTY_MAP;
 				} else {
 					// clone while iterating
 					// (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=59638)
 					containerMap = new HashMap(containerMap);
 				}
 
 				saveContainers(project, containerMap);
 			}
 		}
 
 		private void saveString(String string) throws IOException {
 			if (saveNewId(string, this.stringIds))
 				this.out.writeUTF(string);
 		}
 
 		private void saveVariables(Map map) throws IOException {
 			saveInt(map.size());
 
 			for (Iterator i = map.entrySet().iterator(); i.hasNext();) {
 				Entry entry = (Entry) i.next();
 				String varName = (String) entry.getKey();
 				IPath varPath = (IPath) entry.getValue();
 
 				saveString(varName);
 				savePath(varPath);
 			}
 		}
 	}
 
 }
