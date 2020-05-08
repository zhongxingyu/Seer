 package edu.uci.lighthouse.core.controller;
 
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.apache.log4j.Logger;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFileState;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IWorkspace;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.util.IPropertyChangeListener;
 import org.eclipse.jface.util.PropertyChangeEvent;
 import org.osgi.framework.BundleContext;
 import org.tigris.subversion.subclipse.core.SVNException;
 import org.tigris.subversion.subclipse.core.SVNProviderPlugin;
 import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
 import org.tigris.subversion.svnclientadapter.ISVNInfo;
 import org.tigris.subversion.svnclientadapter.SVNClientException;
 
 import edu.uci.lighthouse.core.Activator;
 import edu.uci.lighthouse.core.listeners.IJavaFileStatusListener;
 import edu.uci.lighthouse.core.listeners.IPluginListener;
 import edu.uci.lighthouse.core.listeners.ISVNEventListener;
 import edu.uci.lighthouse.core.parser.LighthouseParser;
 import edu.uci.lighthouse.core.preferences.DatabasePreferences;
 import edu.uci.lighthouse.core.util.ModelUtility;
 import edu.uci.lighthouse.core.util.UserDialog;
 import edu.uci.lighthouse.core.util.WorkbenchUtility;
 import edu.uci.lighthouse.core.widgets.StatusWidget;
 import edu.uci.lighthouse.model.LighthouseAuthor;
 import edu.uci.lighthouse.model.LighthouseClass;
 import edu.uci.lighthouse.model.LighthouseDelta;
 import edu.uci.lighthouse.model.LighthouseEntity;
 import edu.uci.lighthouse.model.LighthouseEvent;
 import edu.uci.lighthouse.model.LighthouseEvent.TYPE;
 import edu.uci.lighthouse.model.LighthouseFile;
 import edu.uci.lighthouse.model.LighthouseFileManager;
 import edu.uci.lighthouse.model.LighthouseModel;
 import edu.uci.lighthouse.model.LighthouseModelManager;
 import edu.uci.lighthouse.model.LighthouseRelationship;
 import edu.uci.lighthouse.model.io.IPersistence;
 import edu.uci.lighthouse.model.io.PersistenceService;
 import edu.uci.lighthouse.model.jpa.JPAException;
 import edu.uci.lighthouse.model.jpa.JPAUtility;
 import edu.uci.lighthouse.parser.ParserException;
 
 public class Controller implements ISVNEventListener, IJavaFileStatusListener,
 IPluginListener, Runnable, IPropertyChangeListener {
 
 	private static Logger logger = Logger.getLogger(Controller.class);
 	private HashMap<String, Date> mapClassToSVNCommittedTime = new HashMap<String, Date>();
 	/*
 	 * After the file get the changes from the repository, the event 'change'
 	 * will be throw and we can wrong interpret the new changes being
 	 * modifications made by the user. We need this list to ignore those files.
 	 */
 	private Collection<IFile> ignorefilesJustUpdated = new LinkedHashSet<IFile>();
 	private Date lastDBAccess = null;
 	private boolean threadRunning;
 	private boolean threadSuspended;
 	private final int threadTimeout = 10000;
 	private LinkedHashSet<LighthouseEvent> deltaEvents = new LinkedHashSet<LighthouseEvent>();
 
 	private static final String workspaceMetadata = ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString()  + "/.metadata/";
 	private static final String modelFileName = workspaceMetadata + "lighthouse-model.bin";
 	private static final String deltaFileName = workspaceMetadata + "lighthouse-delta.bin";
 	private static final String TEMP_RESOURCE_LHPreviousFile = "TEMP_RESOURCE_LHPreviousFile.java";
 
 	private static Controller instance;
 
 	private Controller () {
 		// Singleton pattern
 	}
 
 	public Map<String, Date> getWorkingCopy() {
 		return mapClassToSVNCommittedTime;
 	}
 
 	@Override
 	public void start(BundleContext context) throws Exception {
 		Activator.getDefault().getPreferenceStore().addPropertyChangeListener(
 				this);
 		loadPreferences();
 		loadModel();
 		//loadDelta();
 		logger.info("Starting thread...");
 		(new Thread(this)).start();
 	}
 
 	@Override
 	public void stop(BundleContext context) throws Exception {
 		threadSuspended = false;
 		threadRunning = false;
 		Activator.getDefault().getPreferenceStore()
 		.removePropertyChangeListener(this);
 		savePreferences();
 		saveModel();
 		//saveDelta();
 	}
 
 	private void loadDelta() {
 		try {
 			ObjectInputStream ois = new ObjectInputStream(
 					new FileInputStream(deltaFileName));
 			deltaEvents = (LinkedHashSet<LighthouseEvent>) ois
 			.readObject();
 			logger.debug("loading " + deltaFileName);
 		} catch (Exception e) {
 			logger.error(e);
 		}
 	}
 
 	private void saveDelta() {
 		try {
 			ObjectOutputStream oos = new ObjectOutputStream(
 					new FileOutputStream(deltaFileName));
 			oos.writeObject(deltaEvents);
 			oos.close();
 			logger.debug("saving " + deltaEvents);
 		} catch (Exception e) {
 			logger.error(e, e);
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	public void loadPreferences() {
 		logger.info("loadPreferences()");
 		String prefix = Activator.PLUGIN_ID;
 		IPreferenceStore prefStore = Activator.getDefault()
 		.getPreferenceStore();
 
 		lastDBAccess = new Date(prefStore.getLong(prefix + "lastDBAccess"));
 		logger.debug("loading lastDBAccess=" + lastDBAccess);
 
 		try {
 			final String filename = "lighthouseWorkingCopy.version";
 			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(
 					filename));
 			mapClassToSVNCommittedTime = (HashMap<String, Date>) ois
 			.readObject();
 			logger.debug("loading " + filename);
 		} catch (Exception e) {
 			logger.error(e);
 		}
 	}
 
 	public void savePreferences() {
 		String prefix = Activator.PLUGIN_ID;
 		IPreferenceStore prefStore = Activator.getDefault()
 		.getPreferenceStore();
 
 		prefStore.setValue(prefix + "lastDBAccess", lastDBAccess.getTime());
 		logger.debug("saving lastDBAccess=" + lastDBAccess);
 
 		try {
 			final String filename = "lighthouseWorkingCopy.version";
 			ObjectOutputStream oos = new ObjectOutputStream(
 					new FileOutputStream(filename));
 			oos.writeObject(mapClassToSVNCommittedTime);
 			oos.close();
 			logger.debug("saving " + filename);
 		} catch (Exception e) {
 			logger.error(e, e);
 		}
 	}
 
 	private void loadModel() {
 		logger.info("loadModel()");
 		try {
 			IPersistence mos = PersistenceService.getService(LighthouseModel.getInstance(),IPersistence.BINARY);
 			mos.load(modelFileName);
 		} catch (Exception e) {
 			logger.error(e);
 		}
 
 		if (LighthouseModel.getInstance().isEmpty()
 				&& mapClassToSVNCommittedTime.size() > 0) {
 			PullModel pullModel = new PullModel(LighthouseModel.getInstance());
 			try {
 				pullModel.executeQueryCheckout(mapClassToSVNCommittedTime);
 			} catch (JPAException e) {
 				logger.error(e, e);
 				// UserDialog.openError(e.getMessage());
 			}
 		}
 		// Show the events in the UI.
 		WorkbenchUtility.updateProjectIcon();
 		LighthouseModel.getInstance().fireModelChanged();
 	}
 
 	private void saveModel() {
 		try {
 			IPersistence mos = PersistenceService.getService(LighthouseModel.getInstance(),IPersistence.BINARY);
 			mos.save(modelFileName);
 		} catch (Exception e) {
 			logger.error(e, e);
 		}
 	}
 
 	@Override
 	public void run() {
 		threadRunning = true;
 		while (threadRunning) {
 			// Sleep for the time defined by thread timeout
 			try {
 				try {
 					refreshModelBasedOnLastDBAccess();
 					Thread.sleep(threadTimeout);
 				} catch (Exception e) {
 					// Database exceptions are RuntimeExceptions
 					StatusWidget.getInstance().setStatus(Status.CANCEL_STATUS);
 					threadSuspended = true;
 					synchronized (this) {
 						while (threadSuspended) {
 							wait();
 						}
 					}
 				} /*
 				 * catch (JPAException e) { logger.error(e,e); }
 				 */
 			} catch (InterruptedException e) {
 				logger.error(e, e);
 			}
 		}
 	};
 
 	/**
 	 * Refresh the LighthouseModel with new events from database and fire this
 	 * changes to the UI.
 	 * 
 	 * @throws JPAException
 	 */
 	public synchronized void refreshModelBasedOnLastDBAccess()
 	throws JPAException {
 		/*
 		 * If the map's size == 0, it means that it is the first time that user
 		 * is running Lighthouse. Then, the LighthouseModel will be updated only
 		 * if the user execute a checkout first.
 		 */
 		if (mapClassToSVNCommittedTime.size() != 0) {
 			LighthouseAuthor author = Activator.getDefault().getAuthor();
 			PullModel pullModel = new PullModel(LighthouseModel.getInstance());
 			List<LighthouseEvent> events = pullModel.getNewEventsFromDB(
 					lastDBAccess, author);
 			fireModificationsToUI(events);
 			if (events.size() != 0) {
 				logger.debug("timeout[ " + lastDBAccess + " ] brought: ["
 						+ events.size() + "] events");
 			}
 			lastDBAccess = getTimestamp();
 		}
 	}
 
 	@Override
 	public void open(final IFile iFile, boolean hasErrors) {
 		// TODO Do Nothing :S
 	}
 
 	@Override
 	public void close(IFile iFile, boolean hasErrors) {
 		// TODO Do Nothing :S
 	}
 
 	@Override
 	public void change(final IFile iFile, boolean hasErrors) {
 		// verify if any of the classWithErrors still have errors
 		// than parse the files that does not have errors anymore
 		// and remove the classes from the list classWithErrors
 		if (ModelUtility.belongsToImportedProjects(iFile, false)) {
 			if (ignorefilesJustUpdated.contains(iFile)) {
 				ignorefilesJustUpdated.remove(iFile);
 				// event change was invoked _withOUT_ a merge problem
 				// Update base version - the delta does not matter
 				// it is not working because we are getting the old IFile
 				// not the new one generateDeltaFromBaseVersion(Collections.singleton(iFile),true);
 				// I will suppose that the revisionTime was set in the update event
 			} else {
 				try {
 					IFile previousIFile = getPreviousVersion(iFile);
 					if (previousIFile!=null) {
 						generateDeltaAndSaveIntoDB(previousIFile, iFile);
 						removeIFile(previousIFile);
 					}
 				} catch (Exception e) {
 					logger.error(e, e);
 				}
 			}
 		}
 	}
 
 	private void generateDeltaAndSaveIntoDB(IFile previousIFile, IFile currentIFile) {
 		try {
 			LighthouseFile previousLhFile = parseIFile(previousIFile);
 			LighthouseFile currentLhFile = parseIFile(currentIFile);
 
 			LighthouseDelta delta = new LighthouseDelta(
 					Activator.getDefault().getAuthor(),
 					previousLhFile,
 					currentLhFile);
 
 			deltaEvents.addAll(delta.getEvents());
 
 			if (deltaEvents.size() == 0) {
 				logger.error("We have an workspace event without delta elements (delta==0)");
 			} else {
 				logger.debug("Workspace event generated delta events: "+ deltaEvents.size());
 			}
 
 			PushModel pushModel = new PushModel(LighthouseModel.getInstance());
			boolean updated = true;
			try {
				pushModel.updateDatabaseFromEvents(deltaEvents);
			} catch (JPAException e) {
				logger.error(e, e);
				updated = false;
			}
 			fireModificationsToUI(deltaEvents);
			if (updated) { 
				deltaEvents.removeAll(deltaEvents);
			}
 		} catch (ParserException e) {
 			logger.error(e, e);		
		} 
 	}
 
 	private LighthouseFile parseIFile(IFile iFile) throws ParserException {
 		if (iFile!=null) {
 			LighthouseParser parser = new LighthouseParser();
 			parser.execute(Collections.singleton(iFile));
 
 			LighthouseFile lhFile = new LighthouseFile();
 			LighthouseFileManager lhFileManager = new LighthouseFileManager(lhFile);
 
 			lhFileManager.populateLHFile(
 					parser.getListEntities(), 
 					parser.getListRelationships());
 			return lhFile;
 		} else {
 			return null;
 		}
 	}
 
 	/**
 	 * The method add happens when a class is added in the workspace or when is
 	 * checkout a project in a empty workspace.
 	 */
 	@Override
 	public void add(IFile iFile, boolean hasErrors) {
 		if (ModelUtility.belongsToImportedProjects(iFile, false)) {
 			final String classFqn = ModelUtility.getClassFullyQualifiedName(iFile);
 			if (!mapClassToSVNCommittedTime.containsKey(classFqn)) {
 				// We have to put the classFqn in this map in order to show the
 				// event in the UI.
 				mapClassToSVNCommittedTime.put(classFqn, new Date(0));
 				try {
 					generateDeltaAndSaveIntoDB(null, iFile);
 				} catch (Exception e) {
 					logger.error(e, e);
 				}
 			}
 		}
 	}
 
 	@Override
 	public void remove(IFile iFile, boolean hasErrors) {
 		if (ModelUtility.belongsToImportedProjects(iFile, false)) {
 			// FIXME:URGENT Gambis pra pegar FQN pelo caminho do arquivo. Melhorar
 			// depois usando o source folder do projeto
 			String srcFolder = "/src/";
 			String projectName = iFile.getProject().getName();
 			int index = iFile.getFullPath().toOSString().indexOf(projectName);
 			String classFqn = iFile.getFullPath().toOSString().substring(
 					index + projectName.length() + srcFolder.length())
 					.replaceAll("/", ".").replaceAll(".java", "");
 			classFqn = projectName + "." + classFqn;
 			try {
 				IFile previousIFile = getPreviousVersion(iFile);
 				if (previousIFile != null) {
 					generateDeltaAndSaveIntoDB(previousIFile, null);
 					mapClassToSVNCommittedTime.remove(classFqn);
 					removeIFile(previousIFile);
 				}
 			} catch (Exception e) {
 				logger.error(e, e);
 			}
 		}
 	}
 
 	@Override
 	public void checkout(Map<IFile, ISVNInfo> svnFiles) {
 		// Ignore these files inside the change event, once we don't want to
 		// generate deltas for other people changes.
 		// ignoredFiles.addAll(svnFiles.keySet());
 
 		HashMap<String, Date> workingCopy = getWorkingCopy(svnFiles, true);
 		mapClassToSVNCommittedTime.putAll(workingCopy);
 		checkoutWorkingCopy(workingCopy);
 	}
 
 	private void checkoutWorkingCopy(HashMap<String, Date> workingCopy){
 
 		PullModel pullModel = new PullModel(LighthouseModel.getInstance());
 		try {
 
 			// magica vai que vai prover a lista de classes (string)
 
 			Collection<LighthouseEvent> events = pullModel
 			.executeQueryCheckout(workingCopy);
 			LighthouseModel.getInstance().fireModelChanged();
 			logger.info("Number of events fetched after checkout = "
 					+ events.size());
 
 		} catch (Exception e) {
 			logger.error(e, e);
 			// UserDialog.openError(e.getMessage());
 		}
 	}
 
 	@Override
 	public void update(Map<IFile, ISVNInfo> svnFiles) {
 		// Ignore in the change event these files, once we don't want to
 		// generate deltas for other people changes.
 		ignorefilesJustUpdated.addAll(svnFiles.keySet());
 
 		// setar a lista de arquivos que foram dados updates
 		// o metodo change vai ser chamado, dai eu nao quero gerar delta
 		HashMap<String, Date> workingCopy = getWorkingCopy(svnFiles, false);
 		mapClassToSVNCommittedTime.putAll(workingCopy);
 
 		LighthouseModel model = LighthouseModel.getInstance();
 		LighthouseModelManager modelManager = new LighthouseModelManager(model);
 
 		modelManager.removeArtifactsAndEvents(workingCopy.keySet());
 
 		//		checkout(svnFiles);
 		checkoutWorkingCopy(workingCopy);
 
 		// // Insert the UPDATE event in the database
 		// // Insert the CHECKOUT event in the database
 		// try {
 		// new PushModel(LighthouseModel.getInstance())
 		// .saveRepositoryEvent(svnFiles,
 		// LighthouseRepositoryEvent.TYPE.UPDATE, new Date());
 		// } catch (Exception e) {
 		// logger.error(e,e);
 		// }
 	}
 
 	@Override
 	public void commit(Map<IFile, ISVNInfo> svnFiles) {
 		HashMap<String, Date> workingCopy = getWorkingCopy(svnFiles, false);
 		mapClassToSVNCommittedTime.putAll(workingCopy);
 		try {
 			PushModel pushModel = new PushModel(LighthouseModel.getInstance());
 
 			// assuming that there is just one committed time
 			ISVNInfo[] svnInfo = svnFiles.values().toArray(new ISVNInfo[0]);
 			// Date svnCommittedTime = svnInfo[0].getLastChangedDate();
 			// FIXME: Synchronization problem
 			Date svnCommittedTime = new Date();
 
 			Collection<LighthouseEvent> listEvents = pushModel
 			.updateCommittedEvents(
 					ModelUtility.getClassesFullyQualifiedName(svnFiles),
 					svnCommittedTime, Activator.getDefault()
 					.getAuthor());
 
 			LighthouseModelManager modelManager = new LighthouseModelManager(
 					LighthouseModel.getInstance());
 			modelManager.removeCommittedEventsAndArtifacts(workingCopy.keySet(),
 					svnCommittedTime);
 
 			fireModificationsToUI(listEvents);
 			logger.debug("Committed [" + listEvents.size() + "] events "
 					+ "with time: " + svnCommittedTime);
 
 			// Insert the CHECKIN event in the database
 			// pushModel.saveRepositoryEvent(svnFiles,
 			// LighthouseRepositoryEvent.TYPE.CHECKIN, new Date());
 		} catch (Exception e) {
 			logger.error(e, e);
 			// UserDialog.openError(e.getMessage());
 		}
 	}
 
 	private HashMap<String, Date> getWorkingCopy(Map<IFile, ISVNInfo> svnFiles, boolean checkDatabase) {
 		HashMap<String, Date> result = new HashMap<String, Date>();
 		for (Entry<IFile, ISVNInfo> entry : svnFiles.entrySet()) {
 			if (ModelUtility.belongsToImportedProjects(entry.getKey(), checkDatabase)) {
 				String fqn = ModelUtility.getClassFullyQualifiedName(entry.getKey());
 				if (fqn != null) {
 					ISVNInfo svnInfo = entry.getValue();
 					result.put(fqn, svnInfo.getLastChangedDate());
 				}
 			}
 		}
 		return result;
 	}
 
 
 
 	private void fireModificationsToUI(Collection<LighthouseEvent> events) {
		logger.debug("fireModificationsToUI ("+events.size()+" events)");
 		// We need hashmap to avoid repaint the UI multiple times
 		HashMap<LighthouseClass, LighthouseEvent.TYPE> mapClassEvent = new HashMap<LighthouseClass, LighthouseEvent.TYPE>();
 		HashMap<LighthouseRelationship, LighthouseEvent.TYPE> mapRelationshipEvent = new HashMap<LighthouseRelationship, LighthouseEvent.TYPE>();
 		LighthouseModel model = LighthouseModel.getInstance();
 		for (LighthouseEvent event : events) {
 			Object artifact = event.getArtifact();
 			// TODO: comment more this method. It is confusing.
 			// ADD creates a new class node in the view and populates it
 			// MODIFY just re-populates the class node
 			if (artifact instanceof LighthouseClass) {
 				LighthouseClass klass = (LighthouseClass) artifact;
 				mapClassEvent.put(klass, event.getType());
 			} else if (artifact instanceof LighthouseEntity) {
 				LighthouseModelManager manager = new LighthouseModelManager(
 						model);
 				LighthouseClass klass = manager
 				.getMyClass((LighthouseEntity) artifact);
 				if (klass != null) {
 					// Never overwrite the ADD event
 					if (!mapClassEvent.containsKey(klass)) {
 						// Just refresh the class node
 						mapClassEvent.put(klass, LighthouseEvent.TYPE.MODIFY);
 					}
 				}
 			} else if (artifact instanceof LighthouseRelationship) {
 				LighthouseRelationship relationship = (LighthouseRelationship) artifact;
 				mapRelationshipEvent.put(relationship, event.getType());
 			}
 		}
 		// Fire class changes to the UI
 		for (Entry<LighthouseClass, TYPE> entry : mapClassEvent.entrySet()) {
			logger.debug("Firing class: "+entry.getKey());
 			model.fireClassChanged(entry.getKey(), entry.getValue());
 		}
 		// Fire relationship changes to the UI
 		for (Entry<LighthouseRelationship, TYPE> entry : mapRelationshipEvent
 				.entrySet()) {
			logger.debug("Firing relationship: "+entry.getKey());
 			model.fireRelationshipChanged(entry.getKey(), entry.getValue());
 		}
 	}
 
 	@Override
 	public void propertyChange(PropertyChangeEvent event) {
 		try {
 			//FIXME: for a better looking code
 			if (event.getProperty().indexOf(DatabasePreferences.ROOT) != -1) {
 				JPAUtility.canConnect(DatabasePreferences
 						.getDatabaseSettings());
 				StatusWidget.getInstance().setStatus(Status.OK_STATUS);
 				synchronizeModelWithDatabase(true);
 				if (threadSuspended) {
 					synchronized (this) {
 						StatusWidget.getInstance().setStatus(Status.OK_STATUS);
 						threadSuspended = false;
 						notify();
 					}
 				}
 			}
 		} catch (Exception e) {
 			logger.error(e, e);
 			StatusWidget.getInstance().setStatus(Status.CANCEL_STATUS);
 		}
 	}
 
 	public void synchronizeModelWithDatabase(){
 		synchronizeModelWithDatabase(false);
 	}
 
 	private void synchronizeModelWithDatabase(final boolean forceNewConnection){
 		final Job job = new Job("Synchronizing Model...") {
 			@Override
 			protected IStatus run(IProgressMonitor monitor) {
 				try {
 					monitor.beginTask("Synchronizing model with database...", IProgressMonitor.UNKNOWN);
 					if (forceNewConnection){
 						JPAUtility.initializeEntityManagerFactory(DatabasePreferences
 								.getDatabaseSettings());
 					}
 					mapClassToSVNCommittedTime = getWorkingCopyFromWorkspace();
 					LighthouseModel.getInstance().clear();
 					checkoutWorkingCopy(mapClassToSVNCommittedTime);
 					WorkbenchUtility.updateProjectIcon();
 					//					LighthouseModel.getInstance().fireModelChanged();
 				} catch (JPAException e) {
 					logger.error(e,e);
 					UserDialog.openError("JPAException: "+e.getMessage());
 				} finally {
 					monitor.done();
 				}
 				return Status.OK_STATUS;
 			}
 		};
 		job.setUser(true);
 		job.schedule();
 	}
 
 	private HashMap<String, Date> getWorkingCopyFromWorkspace(){
 		HashMap<String, Date> result = new HashMap<String, Date>();
 		IWorkspace workspace = ResourcesPlugin.getWorkspace();
 		IProject[] projects = workspace.getRoot().getProjects();
 		try {
 			ISVNClientAdapter svnAdapter = SVNProviderPlugin.getPlugin()
 			.getSVNClient();
 			for (IProject project : projects) {
 				if (project.isOpen()) {
 					try {
 						IJavaProject jProject = (IJavaProject) project
 						.getNature(JavaCore.NATURE_ID);
 						if (jProject != null) {
 							Collection<IFile> iFiles = WorkbenchUtility
 							.getFilesFromJavaProject(jProject);
 							for (IFile iFile : iFiles) {
 								try {
 									ISVNInfo svnInfo = svnAdapter
 									.getInfoFromWorkingCopy(iFile
 											.getLocation().toFile());
 									String fqn = ModelUtility
 									.getClassFullyQualifiedName(iFile);
 									if (fqn != null) {
 										Date revision = svnInfo
 										.getLastChangedDate();
 										if (revision == null) {
 											revision = new Date(0);
 										} else {
 											revision = new Date(revision
 													.getTime());
 										}
 										result.put(fqn, revision);
 									}
 								} catch (SVNClientException ex1) {
 									logger.error(ex1);
 								}
 							}
 						}
 					} catch (CoreException e) {
 						logger.error(e, e);
 					}
 
 				}
 			}
 		} catch (SVNException ex) {
 			logger.error(ex, ex);
 		}
 		return result;
 	}
 
 	public static Controller getInstance(){
 		if (instance == null) {
 			instance = new Controller();
 		}
 		return instance;
 	}
 
 	private synchronized Date getTimestamp() {
 		return new Date();
 	}
 
 	@Override
 	public void conflict(Map<IFile, ISVNInfo> svnFiles) {
 
 	}
 
 	public IFile getPreviousVersion(IFile from) throws Exception{
 		String tempWorkspaceResource = from.getFullPath().toOSString();
 		String fileShortName = from.getFullPath().toFile().getName();
 		tempWorkspaceResource = tempWorkspaceResource.replaceAll(fileShortName, TEMP_RESOURCE_LHPreviousFile);
 
 		IFileState[] history = from.getHistory(null);
 		if (history.length==0) {
 			return null;
 		}
 		InputStream inputStream = history[0].getContents();
 
 		IFile iFile = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(tempWorkspaceResource));
 
 		IProject project = from.getProject();
 		project.getFile(tempWorkspaceResource);
 		iFile.create(inputStream, true, null);
 
 		return iFile;
 	}
 
 	private void removeIFile(IFile iFile) throws CoreException {
 		iFile.delete(true, null);
 	}
 
 }
