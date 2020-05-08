 package edu.uci.lighthouse.core.controller;
 
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.LinkedHashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Map.Entry;
 
 import org.apache.log4j.Logger;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IWorkspace;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
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
 import edu.uci.lighthouse.core.parser.IParserAction;
 import edu.uci.lighthouse.core.parser.LighthouseParser;
 import edu.uci.lighthouse.core.preferences.DatabasePreferences;
 import edu.uci.lighthouse.core.util.ModelUtility;
 import edu.uci.lighthouse.core.util.UserDialog;
 import edu.uci.lighthouse.core.util.WorkbenchUtility;
 import edu.uci.lighthouse.core.widgets.StatusWidget;
 import edu.uci.lighthouse.model.BuildLHBaseFile;
 import edu.uci.lighthouse.model.LighthouseAuthor;
 import edu.uci.lighthouse.model.LighthouseClass;
 import edu.uci.lighthouse.model.LighthouseDelta;
 import edu.uci.lighthouse.model.LighthouseEntity;
 import edu.uci.lighthouse.model.LighthouseEvent;
 import edu.uci.lighthouse.model.LighthouseFile;
 import edu.uci.lighthouse.model.LighthouseFileManager;
 import edu.uci.lighthouse.model.LighthouseModel;
 import edu.uci.lighthouse.model.LighthouseModelManager;
 import edu.uci.lighthouse.model.LighthouseRelationship;
 import edu.uci.lighthouse.model.LighthouseEvent.TYPE;
 import edu.uci.lighthouse.model.io.IPersistence;
 import edu.uci.lighthouse.model.io.LighthouseModelXMLPersistence;
 import edu.uci.lighthouse.model.jpa.JPAException;
 import edu.uci.lighthouse.model.jpa.JPAUtility;
 import edu.uci.lighthouse.parser.ParserException;
 
 public class Controller implements ISVNEventListener, IJavaFileStatusListener,
 IPluginListener, Runnable, IPropertyChangeListener {
 
 	private static Logger logger = Logger.getLogger(Controller.class);
 	private HashMap<String, Date> mapClassToSVNCommittedTime = new HashMap<String, Date>();
 	private HashMap<String, LighthouseFile> classBaseVersion = new HashMap<String, LighthouseFile>();
 	private Set<IFile> classWithErrors = new LinkedHashSet<IFile>();
 	/*
 	 * After the file get the changes from the repository, the event 'change'
 	 * will be throw and we can wrong interpret the new changes being
 	 * modifications made by the user. We need this list to ignore those files.
 	 */
 	private Collection<IFile> ignorefilesJustUpdated = new LinkedHashSet<IFile>();
 	private Date lastDBAccess = null;
 	private boolean threadRunning;
 	private boolean threadSuspended;
 	private final int threadTimeout = 5000;
 	
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
 			logger.debug("saving " + filename);
 		} catch (Exception e) {
 			logger.error(e, e);
 		}
 	}
 
 	private void loadModel() {
 		logger.info("loadModel()");
 		IPersistence mos = new LighthouseModelXMLPersistence(LighthouseModel
 				.getInstance());
 		try {
 			mos.load();
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
 			IPersistence mos = new LighthouseModelXMLPersistence(
 					LighthouseModel.getInstance());
 			mos.save();
 		} catch (IOException e) {
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
 		Thread task = new Thread() {
 			@Override
 			public void run() {
 				// Removed the thread because the unopened files. When you try
 				// to guarantee the lifecycle (open,change,close) the base
 				// version can not
 				try {
 					final String classFqn = ModelUtility.getClassFullyQualifiedName(iFile);
 					LighthouseFile lhBaseFile = getBaseVersionFromDB(classFqn);
 					classBaseVersion.put(classFqn, lhBaseFile);
 				} catch (JPAException e) {
 					logger.error(e, e);
 					// UserDialog.openError(e.getMessage());
 				}
 			}
 		};
 		task.start();
 	}
 
 	public LighthouseFile getBaseVersionFromDB(String classFullyQualifiedName)
 	throws JPAException {
 		LighthouseFile result = null;
 		Date revisionTime = mapClassToSVNCommittedTime
 		.get(classFullyQualifiedName);
 		if (revisionTime != null) {
 			result = BuildLHBaseFile.execute(LighthouseModel.getInstance(),
 					classFullyQualifiedName, revisionTime, Activator
 					.getDefault().getAuthor());
 		}
 		return result;
 	}
 
 	@Override
 	public void change(final IFile iFile, boolean hasErrors) {
 		// verify if any of the classWithErrors still have errors
 		// than parse the files that does not have errors anymore
 		// and remove the classes from the list classWithErrors
 		if (ModelUtility.belongsToImportedProjects(iFile, false)) {
 			if (!hasErrors) {
 
 				if (ignorefilesJustUpdated.contains(iFile)) {
 					// event change was invoked without a merge problem
 					ignorefilesJustUpdated.remove(iFile);
 					// Update base version - the delta does not matter
 					// it is not working because we are getting the old IFile
 					// not
 					// the new one
 					// generateDeltaFromBaseVersion(Collections.singleton(iFile),true);
 
 					// I will suppose that the revisionTime was setted in the
 					// update
 					// event
 					String classFqn = ModelUtility.getClassFullyQualifiedName(iFile);
 					try {
 						LighthouseFile lhBaseFile = getBaseVersionFromDB(classFqn);
 						classBaseVersion.put(classFqn, lhBaseFile);
 					} catch (JPAException e) {
 						logger.error(e, e);
 						// UserDialog.openError(e.getMessage());
 					}
 
 				} else {
 					// TODO: Think about DB operations in a thread
 
 					try {
 						Collection<LighthouseEvent> deltaEvents = generateDeltaFromBaseVersion(Collections
 								.singleton(iFile));
 						if (deltaEvents.size() == 0) {
 							logger
 							.error("We have a change event without delta elements (delta==0)");
 						} else {
 							logger
 							.debug("Event change generated delta events: "
 									+ deltaEvents.size());
 						}
 						PushModel pushModel = new PushModel(LighthouseModel
 								.getInstance());
 						pushModel.updateDatabaseFromEvents(deltaEvents);
 						// FIXME: This implies if some error happen, the UI is
 						// not
 						// going to be updated. Check with Nilmax to see if this
 						// the
 						// behavior that we want.
 						fireModificationsToUI(deltaEvents);
 					} catch (Exception e) {
 						logger.error(e, e);
 						// UserDialog.openError(e.getMessage());
 					}
 
 				}
 
 			} else { // if file with error
 				// FIXME: we are calling this if block in the algorithm for both
 				// error/no erros. A better approach will put them outside the
 				// main
 				// if. Check this later.
 				if (ignorefilesJustUpdated.contains(iFile)) {
 					// it is a merge conflict, next time that event change will
 					// be
 					// invoked
 					// we are going to have just two situation:
 					// 1. file still with error
 					// 2. merge problems resolved
 					ignorefilesJustUpdated.remove(iFile);
 				}
 			}
 		}
 	}
 
 	private Collection<LighthouseEvent> generateDeltaFromBaseVersion(
 			Collection<IFile> files) {
 		return generateDeltaFromBaseVersion(files, false);
 	}
 
 	private Collection<LighthouseEvent> generateDeltaFromBaseVersion(
 			Collection<IFile> files, boolean force) {
 		final List<LighthouseEvent> result = new LinkedList<LighthouseEvent>();
 
 		// Use iterator to be able to remove the IFile in the loop
 		for (IFile file : files) {
 			final String classFqn = ModelUtility.getClassFullyQualifiedName(file);
 			final LighthouseFile lhBaseFile = classBaseVersion.get(classFqn);
 			if (force || lhBaseFile != null) {
 				try {
 					final LighthouseParser parser = new LighthouseParser();
 					parser.executeInAJob(Collections.singleton(file),
 							new IParserAction() {
 						@Override
 						public void doAction() throws ParserException {
 							LighthouseFile currentLhFile = new LighthouseFile();
 							new LighthouseFileManager(currentLhFile)
 							.populateLHFile(parser
 									.getListEntities(), parser
 									.getListRelationships());
 							LighthouseDelta delta = new LighthouseDelta(
 									Activator.getDefault()
 									.getAuthor(),
 									lhBaseFile, currentLhFile);
 							classBaseVersion.put(classFqn,
 									currentLhFile);
 							result.addAll(delta.getEvents());
 
 						}
 					});
 				} catch (ParserException e) {
 					logger.error(e, e);
 					// UserDialog.openError(e.getMessage());
 				}
 			}
 		}
 		return result;
 	}
 
 	@Override
 	public void close(IFile iFile, boolean hasErrors) {
 		final String classFqn = ModelUtility.getClassFullyQualifiedName(iFile);
 		if (hasErrors) {
 			classWithErrors.add(iFile);
 		} else {
 			classBaseVersion.remove(classFqn);
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
 			model.fireClassChanged(entry.getKey(), entry.getValue());
 		}
 		// Fire relationship changes to the UI
 		for (Entry<LighthouseRelationship, TYPE> entry : mapRelationshipEvent
 				.entrySet()) {
 			model.fireRelationshipChanged(entry.getKey(), entry.getValue());
 		}
 	}
 
 	@Override
 	public void remove(IFile iFile, boolean hasErrors) {
 		if (ModelUtility.belongsToImportedProjects(iFile, false)) {
 			// FIXME: Gambis pra pegar FQN pelo caminho do arquivo. Melhorar
 			// depois
 			// usando o source folder do projeto
 			String srcFolder = "/src/";
 			String projectName = iFile.getProject().getName();
 			int index = iFile.getFullPath().toOSString().indexOf(projectName);
 			String classFqn = iFile.getFullPath().toOSString().substring(
 					index + projectName.length() + srcFolder.length())
 					.replaceAll("/", ".").replaceAll(".java", "");
 			classFqn = projectName + "." + classFqn;
 
 			try {
 				LighthouseFile lhBaseFile = getBaseVersionFromDB(classFqn);
 				if (lhBaseFile != null) {
 					mapClassToSVNCommittedTime.remove(classFqn);
 					LighthouseDelta delta = new LighthouseDelta(Activator
 							.getDefault().getAuthor(), lhBaseFile, null);
 
 					PushModel pushModel = new PushModel(LighthouseModel
 							.getInstance());
 					pushModel.updateDatabaseFromEvents(delta.getEvents());
 					fireModificationsToUI(delta.getEvents());
 				}
 			} catch (Exception e) {
 				logger.error(e, e);
 				// UserDialog.openError(e.getMessage());
 			}
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
 
 			// It is a new class created by the user. We are assuming that the
 			// user creates a class that doesn't contain errors.
 			if (!mapClassToSVNCommittedTime.containsKey(classFqn)) {
 				/*
 				 * We have to put the classFqn in this map in order to show the
 				 * event in the UI.
 				 */
 				mapClassToSVNCommittedTime.put(classFqn, new Date(0));
 				// TODO: Think about DB operations in a thread
 				try {
 					Collection<LighthouseEvent> deltaEvents = generateDeltaFromBaseVersion(
 							Collections.singleton(iFile), true);
 					PushModel pushModel = new PushModel(LighthouseModel
 							.getInstance());
 					pushModel.updateDatabaseFromEvents(deltaEvents);
 					fireModificationsToUI(deltaEvents);
 				} catch (Exception e) {
 					// TODO: Try to throw up this exception
 					logger.error(e, e);
 					// UserDialog.openError(e.getMessage());
 				}
 			}
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
 					LighthouseModel.getInstance().fireModelChanged();
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
 						Collection<IFile> iFiles = WorkbenchUtility
 								.getFilesFromJavaProject(jProject);
 						for (IFile iFile : iFiles) {
 							try {
 							ISVNInfo svnInfo = svnAdapter
 									.getInfoFromWorkingCopy(iFile.getLocation().toFile());
 							String fqn = ModelUtility
 									.getClassFullyQualifiedName(iFile);
 							if (fqn != null) {
								Date revision = svnInfo.getLastChangedDate();
								if (revision == null){
									revision = new Date(0);
								} else {
									revision = new Date(revision.getTime());
								}
								result.put(fqn, revision);
 							}
 							} catch (SVNClientException ex1) {
 								logger.error(ex1);
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
 
 }
