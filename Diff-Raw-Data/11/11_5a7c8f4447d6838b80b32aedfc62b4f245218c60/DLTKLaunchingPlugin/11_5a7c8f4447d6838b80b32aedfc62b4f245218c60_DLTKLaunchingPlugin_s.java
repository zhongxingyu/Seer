 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.internal.launching;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.FactoryConfigurationError;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import org.eclipse.core.resources.IWorkspaceRunnable;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IExtensionPoint;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.Plugin;
 import org.eclipse.core.runtime.Preferences;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.Preferences.PropertyChangeEvent;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.dltk.ast.declarations.ModuleDeclaration;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.IBuildpathEntry;
 import org.eclipse.dltk.core.IDLTKProject;
 import org.eclipse.dltk.launching.IInterpreterInstall;
 import org.eclipse.dltk.launching.IInterpreterInstallChangedListener;
 import org.eclipse.dltk.launching.IInterpreterInstallType;
 import org.eclipse.dltk.launching.IRuntimeBuildpathEntry2;
 import org.eclipse.dltk.launching.InterpreterStandin;
 import org.eclipse.dltk.launching.LaunchingMessages;
 import org.eclipse.dltk.launching.ScriptRuntime;
 import org.osgi.framework.BundleContext;
 import org.w3c.dom.Document;
 import org.xml.sax.helpers.DefaultHandler;
 
 import com.ibm.icu.text.MessageFormat;
 
 public class DLTKLaunchingPlugin extends Plugin implements
 		Preferences.IPropertyChangeListener, IInterpreterInstallChangedListener {
 	
 	public static final int DBGP_SERVICE_NOT_AVAILABLE = 200;
 	
 	public static final int DEBUGGING_ENGINE_NOT_STARTED = 201;
 	
 	public static final int DEBUGGING_ENGINE_NOT_CONNECTED = 202;
 	
 	public final static String ID_PLUGIN = "org.eclipse.dltk.launching";
 
 	public static final String ID_EXTENSION_POINT_RUNTIME_BUILDPATH_ENTRIES = "runtimeBuildpathEntries"; //$NON-NLS-1$
 
 	/**
 	 * Runtime buildpath extensions
 	 */
 	private HashMap fBuildpathEntryExtensions = null;
 
 	private String fOldInterpreterPrefString = EMPTY_STRING;
 
 	private static final String EMPTY_STRING = ""; //$NON-NLS-1$
 
 	private boolean fIgnoreInterpreterDefPropertyChangeEvents = false;
 
 	private boolean fBatchingChanges = false;
 
 	/**
 	 * Shared XML parser
 	 */
 	private static DocumentBuilder fgXMLParser = null;
 
 	private static DLTKLaunchingPlugin fgLaunchingPlugin;
 	
 	public DLTKLaunchingPlugin() {
 		super();
 		fgLaunchingPlugin = this;
 	}
 
 	/**
 	 * Convenience method which returns the unique identifier of this plug-in.
 	 */
 	public static String getUniqueIdentifier() {
 		return ID_PLUGIN;
 	}
 
 	public static DLTKLaunchingPlugin getDefault() {
 		return fgLaunchingPlugin;
 	}
 
 	public static void log(IStatus status) {
 		getDefault().getLog().log(status);
 	}
 
 	public static void log(String message) {
 		log(new Status(IStatus.ERROR, getUniqueIdentifier(), IStatus.ERROR,
 				message, null));
 	}
 
 	public static void log(Throwable e) {
 		log(new Status(IStatus.ERROR, getUniqueIdentifier(), IStatus.ERROR, e
 				.getMessage(), e));
 	}
 
 	/**
 	 * Returns a Document that can be used to build a DOM tree
 	 * 
 	 * @return the Document
 	 * @throws ParserConfigurationException
 	 *             if an exception occurs creating the document builder
 	 */
 	public static Document getDocument() throws ParserConfigurationException {
 		DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance();
 		DocumentBuilder docBuilder = dfactory.newDocumentBuilder();
 		Document doc = docBuilder.newDocument();
 		return doc;
 	}
 
 	/**
 	 * Serializes a XML document into a string - encoded in UTF8 format, with
 	 * platform line separators.
 	 * 
 	 * @param doc
 	 *            document to serialize
 	 * @return the document as a string
 	 */
 	public static String serializeDocument(Document doc) throws IOException,
 			TransformerException {
 		ByteArrayOutputStream s = new ByteArrayOutputStream();
 
 		TransformerFactory factory = TransformerFactory.newInstance();
 		Transformer transformer = factory.newTransformer();
 		transformer.setOutputProperty(OutputKeys.METHOD, "xml"); //$NON-NLS-1$
 		transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$
 
 		DOMSource source = new DOMSource(doc);
 		StreamResult outputTarget = new StreamResult(s);
 		transformer.transform(source, outputTarget);
 
 		return s.toString("UTF8"); //$NON-NLS-1$			
 	}
 
 	/**
 	 * Returns a shared XML parser.
 	 * 
 	 * @return an XML parser
 	 * @throws CoreException
 	 *             if unable to create a parser
 	 * 
 	 */
 	public static DocumentBuilder getParser() throws CoreException {
 		if (fgXMLParser == null) {
 			try {
 				fgXMLParser = DocumentBuilderFactory.newInstance()
 						.newDocumentBuilder();
 				fgXMLParser.setErrorHandler(new DefaultHandler());
 			} catch (ParserConfigurationException e) {
 				abort(LaunchingMessages.LaunchingPlugin_33, e);
 			} catch (FactoryConfigurationError e) {
 				abort(LaunchingMessages.LaunchingPlugin_34, e);
 			}
 		}
 		return fgXMLParser;
 	}
 
 	/**
 	 * Throws an exception with the given message and underlying exception.
 	 * 
 	 * @param message
 	 *            error message
 	 * @param exception
 	 *            underlying exception or <code>null</code> if none
 	 * @throws CoreException
 	 */
 	protected static void abort(String message, Throwable exception)
 			throws CoreException {
 		IStatus status = new Status(IStatus.ERROR, DLTKLaunchingPlugin
 				.getUniqueIdentifier(), 0, message, exception);
 		throw new CoreException(status);
 	}
 
 	/**
 	 * Returns a new runtime buildpath entry of the specified type.
 	 * 
 	 * @param id
 	 *            extension type id
 	 * @return new uninitialized runtime buildpath entry
 	 * @throws CoreException
 	 *             if unable to create an entry
 	 */
 	public IRuntimeBuildpathEntry2 newRuntimeBuildpathEntry(String id)
 			throws CoreException {
 		if (fBuildpathEntryExtensions == null) {
 			initializeRuntimeBuildpathExtensions();
 		}
 		IConfigurationElement config = (IConfigurationElement) fBuildpathEntryExtensions
 				.get(id);
 		if (config == null) {
 			abort(MessageFormat.format(
 					LaunchingMessages.LaunchingPlugin_32,
 					new String[] { id }), null);
 		}
 		return (IRuntimeBuildpathEntry2) config
 				.createExecutableExtension("class"); //$NON-NLS-1$
 	}
 
 	/**
 	 * Loads runtime buildpath extensions
 	 */
 	private void initializeRuntimeBuildpathExtensions() {
 		IExtensionPoint extensionPoint = Platform.getExtensionRegistry()
 				.getExtensionPoint(DLTKLaunchingPlugin.ID_PLUGIN,
 						ID_EXTENSION_POINT_RUNTIME_BUILDPATH_ENTRIES);
 		IConfigurationElement[] configs = extensionPoint
 				.getConfigurationElements();
 		fBuildpathEntryExtensions = new HashMap(configs.length);
 		for (int i = 0; i < configs.length; i++) {
 			fBuildpathEntryExtensions.put(
 					configs[i].getAttribute("id"), configs[i]); //$NON-NLS-1$
 		}
 	}
 	
 	/**
 	 * @see Plugin#start(BundleContext)
 	 */
 	public void start(BundleContext context) throws Exception {
 		super.start(context);
 
 		// Exclude launch configurations from being copied to the output
 		// directory
 		String launchFilter = "*." + ILaunchConfiguration.LAUNCH_CONFIGURATION_FILE_EXTENSION; //$NON-NLS-1$
 		Hashtable optionsMap = DLTKCore.getOptions();
 		String filters = (String) optionsMap
 				.get("org.eclipse.dltk.core.builder.resourceCopyExclusionFilter"); //$NON-NLS-1$
 		boolean modified = false;
 		if (filters == null || filters.length() == 0) {
 			filters = launchFilter;
 			modified = true;
 		} else if (filters.indexOf(launchFilter) == -1) {
 			filters = filters + ',' + launchFilter;
 			modified = true;
 		}
 
 		if (modified) {
 			optionsMap
 					.put(
 							"org.eclipse.dltk.core.builder.resourceCopyExclusionFilter", filters); //$NON-NLS-1$
 			DLTKCore.setOptions(optionsMap);
 		}
 
 		// set default preference values
 		getPluginPreferences().setDefault(ScriptRuntime.PREF_CONNECT_TIMEOUT,
 				ScriptRuntime.DEF_CONNECT_TIMEOUT);
 		getPluginPreferences().addPropertyChangeListener(this);
 
 		ScriptRuntime.addInterpreterInstallChangedListener(this);
 		// ResourcesPlugin.getWorkspace().addResourceChangeListener(this,
 		// IResourceChangeEvent.PRE_DELETE | IResourceChangeEvent.PRE_CLOSE |
 		// IResourceChangeEvent.PRE_BUILD);
 		// DebugPlugin.getDefault().getLaunchManager().addLaunchListener(this);
 		// DebugPlugin.getDefault().addDebugEventListener(this);
 		
 		// prefetch library locations
 		Job libPrefetch = new Job("Inializing DLTK launching") {
 
 			protected IStatus run(IProgressMonitor monitor) {
 				IInterpreterInstallType[] installTypes = ScriptRuntime.getInterpreterInstallTypes();
 				monitor.beginTask("Fetching default interpreter library locations", installTypes.length);
 				for (int i = 0; i < installTypes.length; i++) {
 					IInterpreterInstall[] installs = installTypes[i].getInterpreterInstalls();
 					for (int j = 0; j < installs.length; j++) {
 						File path = installs[j].getInstallLocation();
 						installTypes[i].getDefaultLibraryLocations(path);			
 					}
 					monitor.worked(1);
 				}
 				monitor.done();
 				return Status.OK_STATUS;
 			}
 			
 		};
 		
 		libPrefetch.schedule();
		
		
 	}
 
 	/**
 	 * Clears zip file cache. Shutdown the launch config helper.
 	 * 
 	 * @see Plugin#stop(BundleContext)
 	 */
 	public void stop(BundleContext context) throws Exception {
 		try {
 			// DebugPlugin.getDefault().getLaunchManager().removeLaunchListener(this);
 			// DebugPlugin.getDefault().removeDebugEventListener(this);
 			// ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
 			getPluginPreferences().removePropertyChangeListener(this);
 			ScriptRuntime.removeInterpreterInstallChangedListener(this);
 			ScriptRuntime.saveInterpreterConfiguration();
 			savePluginPreferences();
 			fgXMLParser = null;
 		} finally {
 			super.stop(context);
 		}
 	}
 
 	public void setIgnoreInterpreterDefPropertyChangeEvents(boolean ignore) {
 		fIgnoreInterpreterDefPropertyChangeEvents = ignore;
 	}
 
 	public boolean isIgnoreInterpreterDefPropertyChangeEvents() {
 		return fIgnoreInterpreterDefPropertyChangeEvents;
 	}
 
 	/**
 	 * Save preferences whenever the connect timeout changes. Process changes to
 	 * the list of installed InterpreterEnvironments.
 	 * 
 	 * @see org.eclipse.core.runtime.Preferences.IPropertyChangeListener#propertyChange(PropertyChangeEvent)
 	 */
 	public void propertyChange(PropertyChangeEvent event) {
 		String property = event.getProperty();
 		if (property.equals(ScriptRuntime.PREF_CONNECT_TIMEOUT)) {
 			savePluginPreferences();
 		} else if (property.equals(ScriptRuntime.PREF_INTERPRETER_XML)) {
 			if (!isIgnoreInterpreterDefPropertyChangeEvents()) {
 				processInterpreterPrefsChanged((String) event.getOldValue(),
 						(String) event.getNewValue());
 			}
 		}
 	}
 
 	/**
 	 * Check for differences between the old & new sets of installed
 	 * InterpreterEnvironments. Differences may include additions, deletions and
 	 * changes. Take appropriate action for each type of difference.
 	 * 
 	 * When importing preferences, TWO propertyChange events are fired. The
 	 * first has an old value but an empty new value. The second has a new
 	 * value, but an empty old value. Normal user changes to the preferences
 	 * result in a single propertyChange event, with both old and new values
 	 * populated. This method handles both types of notification.
 	 */
 	protected void processInterpreterPrefsChanged(String oldValue,
 			String newValue) {
 
 		// batch changes
 		fBatchingChanges = true;
 		InterpreterChanges InterpreterChanges = null;
 		try {
 
 			String oldPrefString;
 			String newPrefString;
 
 			// If empty new value, save the old value and wait for 2nd
 			// propertyChange notification
 			if (newValue == null || newValue.equals(EMPTY_STRING)) {
 				fOldInterpreterPrefString = oldValue;
 				return;
 			}
 			// An empty old value signals the second notification in the import
 			// preferences
 			// sequence. Now that we have both old & new prefs, we can parse and
 			// compare them.
 			else if (oldValue == null || oldValue.equals(EMPTY_STRING)) {
 				oldPrefString = fOldInterpreterPrefString;
 				newPrefString = newValue;
 			}
 			// If both old & new values are present, this is a normal user
 			// change
 			else {
 				oldPrefString = oldValue;
 				newPrefString = newValue;
 			}
 
 			InterpreterChanges = new InterpreterChanges();
 			ScriptRuntime
 					.addInterpreterInstallChangedListener(InterpreterChanges);
 
 			// Generate the previous Interpreters
 			InterpreterDefinitionsContainer oldResults = getInterpreterDefinitions(oldPrefString);
 
 			// Generate the current
 			InterpreterDefinitionsContainer newResults = getInterpreterDefinitions(newPrefString);
 
 			// Determine the deteled Interpreters
 			List deleted = oldResults.getInterpreterList();
 			List current = newResults.getValidInterpreterList();
 			deleted.removeAll(current);
 
 			// Dispose deleted Interpreters. The 'disposeInterpreterInstall'
 			// method fires notification of the
 			// deletion.
 			Iterator deletedIterator = deleted.iterator();
 			while (deletedIterator.hasNext()) {
 				InterpreterStandin deletedInterpreterStandin = (InterpreterStandin) deletedIterator
 						.next();
 				deletedInterpreterStandin.getInterpreterInstallType()
 						.disposeInterpreterInstall(
 								deletedInterpreterStandin.getId());
 			}
 
 			// Fire change notification for added and changed Interpreters. The
 			// 'convertToRealInterpreter'
 			// fires the appropriate notification.
 			Iterator iter = current.iterator();
 			while (iter.hasNext()) {
 				InterpreterStandin standin = (InterpreterStandin) iter.next();
 				standin.convertToRealInterpreter();
 			}
 
 			// set the new default Interpreter installs. This will fire a
 			// 'defaultInterpreterChanged',
 			// if it in fact changed
 			String newDefaultId[] = newResults
 					.getDefaultInterpreterInstallCompositeID();
 			for (int i = 0; i < newDefaultId.length; i++) {
 				if (newDefaultId[i] != null) {
 					IInterpreterInstall newDefaultInterpreter = ScriptRuntime
 							.getInterpreterFromCompositeId(newDefaultId[i]);
 					if (newDefaultInterpreter != null) {
 						try {
 							ScriptRuntime.setDefaultInterpreterInstall(
 									newDefaultInterpreter, null, false);
 						} catch (CoreException ce) {
 							log(ce);
 						}
 					}
 				}
 			}
 
 		} finally {
 			// stop batch changes
 			fBatchingChanges = false;
 			if (InterpreterChanges != null) {
 				ScriptRuntime
 						.removeInterpreterInstallChangedListener(InterpreterChanges);
 				try {
 					InterpreterChanges.process();
 				} catch (CoreException e) {
 					log(e);
 				}
 			}
 		}
 
 	}
 
 	/**
 	 * Parse the given xml into a Interpreter definitions container, returning
 	 * an empty container if an exception occurs.
 	 * 
 	 * @param xml
 	 * @return InterpreterDefinitionsContainer
 	 */
 	private InterpreterDefinitionsContainer getInterpreterDefinitions(String xml) {
 		if (xml.length() > 0) {
 			try {
 				ByteArrayInputStream stream = new ByteArrayInputStream(xml
 						.getBytes("UTF8")); //$NON-NLS-1$
 				return InterpreterDefinitionsContainer
 						.parseXMLIntoContainer(stream);
 			} catch (IOException e) {
 				DLTKLaunchingPlugin.log(e);
 			}
 		}
 		return new InterpreterDefinitionsContainer();
 	}
 
 	public void defaultInterpreterInstallChanged(IInterpreterInstall previous,
 			IInterpreterInstall current) {
 		if (!fBatchingChanges) {
 			try {
 				InterpreterChanges changes = new InterpreterChanges();
 				changes.defaultInterpreterInstallChanged(previous, current);
 				changes.process();
 			} catch (CoreException e) {
 				log(e);
 			}
 		}
 	}
 
 	public void interpreterAdded(IInterpreterInstall Interpreter) {
 	}
 
 	public void interpreterChanged(
 			org.eclipse.dltk.launching.PropertyChangeEvent event) {
 		if (!fBatchingChanges) {
 			try {
 				InterpreterChanges changes = new InterpreterChanges();
 				changes.interpreterChanged(event);
 				changes.process();
 			} catch (CoreException e) {
 				log(e);
 			}
 		}
 	}
 
 	public void interpreterRemoved(IInterpreterInstall Interpreter) {
 		if (!fBatchingChanges) {
 			try {
 				InterpreterChanges changes = new InterpreterChanges();
 				changes.interpreterRemoved(Interpreter);
 				changes.process();
 			} catch (CoreException e) {
 				log(e);
 			}
 		}
 	}
 
 	/**
 	 * Re-bind buildpath variables and containers affected by the
 	 * InterpreterEnvironment changes.
 	 * 
 	 * @param monitor
 	 */
 	public void rebind(IProgressMonitor monitor, IDLTKProject[] projects, Map renamedContainerIds)
 			throws CoreException {
 		monitor.worked(1);
 	
 		// re-bind all container entries
 		for (int i = 0; i < projects.length; i++) {
 			IDLTKProject project = projects[i];
 			IBuildpathEntry[] entries = project.getRawBuildpath();
 			boolean replace = false;
 			for (int j = 0; j < entries.length; j++) {
 				IBuildpathEntry entry = entries[j];
 				switch (entry.getEntryKind()) {
 				case IBuildpathEntry.BPE_CONTAINER:
 					IPath reference = entry.getPath();
 					IPath newBinding = null;
 					String firstSegment = reference.segment(0);
 					if (ScriptRuntime.INTERPRETER_CONTAINER
 							.equals(firstSegment)) {
 						if (reference.segmentCount() > 1) {
 							IPath renamed = (IPath) renamedContainerIds
 									.get(reference);
 							if (renamed != null) {
 								// The interpreter was re-named. This
 								// changes the identifier of
 								// the container entry.
 								newBinding = renamed;
 							}
 						}
 						InterpreterContainerInitializer initializer = new InterpreterContainerInitializer();
 						if (newBinding == null) {
 							// rebind old path
 							initializer.initialize(reference, project);
 						} else {
 							// replace old bp entry with a new one
 							IBuildpathEntry newEntry = DLTKCore
 									.newContainerEntry(newBinding, entry
 											.isExported());
 							entries[j] = newEntry;
 							replace = true;
 						}
 					}
 					break;
 				default:
 					break;
 				}
 			}
 			if (replace) {
 				project.setRawBuildpath(entries, null);
 			}
 			monitor.worked(1);
 		}
 	
 	}
 
 	/**
 	 * Stores Interpreter changes resulting from a InterpreterEnvironment
 	 * preference change.
 	 */
 	class InterpreterChanges implements IInterpreterInstallChangedListener {
 
 		// old container ids to new
 		private HashMap fRenamedContainerIds = new HashMap();
 
 		/**
 		 * Returns the InterpreterEnvironment container id that the given
 		 * Interpreter would map to, or <code>null</code> if none.
 		 * 
 		 * @param Interpreter
 		 * @return container id or <code>null</code>
 		 */
 		private IPath getContainerId(IInterpreterInstall Interpreter) {
 			if (Interpreter != null) {
 				String name = Interpreter.getName();
 				if (name != null) {
 					IPath path = new Path(ScriptRuntime.INTERPRETER_CONTAINER);
 					path = path.append(new Path(Interpreter
 							.getInterpreterInstallType().getId()));
 					path = path.append(new Path(name));
 					return path;
 				}
 			}
 			return null;
 		}
 
 		public void defaultInterpreterInstallChanged(
 				IInterpreterInstall previous, IInterpreterInstall current) {
 		}
 
 		public void interpreterAdded(IInterpreterInstall Interpreter) {
 		}
 
 		public void interpreterChanged(
 				org.eclipse.dltk.launching.PropertyChangeEvent event) {
 			String property = event.getProperty();
 			IInterpreterInstall Interpreter = (IInterpreterInstall) event
 					.getSource();
 			if (property
 					.equals(IInterpreterInstallChangedListener.PROPERTY_NAME)) {
 				IPath newId = getContainerId(Interpreter);
 				IPath oldId = new Path(ScriptRuntime.INTERPRETER_CONTAINER);
 				oldId = oldId.append(Interpreter.getInterpreterInstallType()
 						.getId());
 				String oldName = (String) event.getOldValue();
 				// bug 33746 - if there is no old name, then this is not a
 				// re-name.
 				if (oldName != null) {
 					oldId = oldId.append(oldName);
 					fRenamedContainerIds.put(oldId, newId);
 				}
 			}
 		}
 
 		public void interpreterRemoved(IInterpreterInstall Interpreter) {
 		}
 
 		/**
 		 * Re-bind buildpath variables and containers affected by the
 		 * InterpreterEnvironment changes.
 		 */
 		public void process() throws CoreException {
 			InterpreterUpdateJob job = new InterpreterUpdateJob(this);
 			job.schedule();
 		}
 
 		protected void doit(IProgressMonitor monitor) throws CoreException {
 			IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
 				public void run(IProgressMonitor monitor1) throws CoreException {
 					IDLTKProject[] projects = DLTKCore.create(
 							ResourcesPlugin.getWorkspace().getRoot())
 							.getScriptProjects();
 					monitor1.beginTask(LaunchingMessages.LaunchingPlugin_0,
 							projects.length + 1);
 					rebind(monitor1, projects, fRenamedContainerIds);
 					monitor1.done();
 				}
 			};
 			DLTKCore.run(runnable, null, monitor);
 		}
 
 	}
 
 	class InterpreterUpdateJob extends Job {
 		private InterpreterChanges fChanges;
 
 		public InterpreterUpdateJob(InterpreterChanges changes) {
 			super(LaunchingMessages.LaunchingPlugin_1);
 			fChanges = changes;
 			setSystem(true);
 		}
 
 		protected IStatus run(IProgressMonitor monitor) {
 			try {
 				fChanges.doit(monitor);
 			} catch (CoreException e) {
 				return e.getStatus();
 			}
 			return Status.OK_STATUS;
 		}
 	}
 }
