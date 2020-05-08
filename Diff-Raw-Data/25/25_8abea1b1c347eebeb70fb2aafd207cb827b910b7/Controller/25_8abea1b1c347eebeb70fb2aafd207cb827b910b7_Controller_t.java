 package edu.uci.lighthouse.core.controller;
 
 import java.io.InputStream;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.LinkedHashSet;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFileState;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jface.util.IPropertyChangeListener;
 import org.eclipse.jface.util.PropertyChangeEvent;
 import org.osgi.framework.BundleContext;
 import org.tigris.subversion.subclipse.core.SVNProviderPlugin;
 import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
 import org.tigris.subversion.svnclientadapter.ISVNInfo;
 import org.tigris.subversion.svnclientadapter.ISVNStatus;
 import org.tigris.subversion.svnclientadapter.SVNStatusKind;
 
 import edu.uci.lighthouse.core.Activator;
 import edu.uci.lighthouse.core.data.DatabaseActionsThread;
 import edu.uci.lighthouse.core.data.PersistableRegistry;
 import edu.uci.lighthouse.core.dbactions.DatabaseActionsBuffer;
 import edu.uci.lighthouse.core.dbactions.IDatabaseAction;
 import edu.uci.lighthouse.core.dbactions.JobDecoratorAction;
 import edu.uci.lighthouse.core.dbactions.pull.CheckoutAction;
 import edu.uci.lighthouse.core.dbactions.pull.SynchronizeModelAction;
 import edu.uci.lighthouse.core.dbactions.pull.UpdateAction;
 import edu.uci.lighthouse.core.dbactions.push.CommitAction;
 import edu.uci.lighthouse.core.dbactions.push.FileEventAction;
 import edu.uci.lighthouse.core.listeners.IJavaFileStatusListener;
 import edu.uci.lighthouse.core.listeners.IPluginListener;
 import edu.uci.lighthouse.core.listeners.ISVNEventListener;
 import edu.uci.lighthouse.core.parser.LighthouseParser;
 import edu.uci.lighthouse.core.preferences.DatabasePreferences;
 import edu.uci.lighthouse.core.util.ModelUtility;
 import edu.uci.lighthouse.core.util.WorkbenchUtility;
 import edu.uci.lighthouse.core.widgets.StatusWidget;
 import edu.uci.lighthouse.model.LighthouseDelta;
 import edu.uci.lighthouse.model.LighthouseEvent;
 import edu.uci.lighthouse.model.LighthouseFile;
 import edu.uci.lighthouse.model.LighthouseFileManager;
 import edu.uci.lighthouse.model.LighthouseModel;
 import edu.uci.lighthouse.model.LighthouseModelManager;
 import edu.uci.lighthouse.parser.ParserException;
 
 public class Controller implements ISVNEventListener, IJavaFileStatusListener,
 IPluginListener, /*Runnable,*/ IPropertyChangeListener {
 
 	private static Logger logger = Logger.getLogger(Controller.class);
 	//private WorkingCopy workingCopy;// = new WorkingCopy();
 	/*
 	 * After the file get the changes from the repository, the event 'change'
 	 * will be throw and we can wrong interpret the new changes being
 	 * modifications made by the user. We need this list to ignore those files.
 	 */
 	private Collection<IFile> ignorefilesJustUpdated = new LinkedHashSet<IFile>();
 	private DatabaseActionsBuffer buffer;
 	private LighthouseModel model;
 	private DatabaseActionsThread thread;
 	
 	@Override
 	public void start(BundleContext context) throws Exception {
 		Activator.getDefault().getPreferenceStore().addPropertyChangeListener(
 				this);	
 		loadPersistableResources();
 		if (!model.isEmpty()) {
 			WorkbenchUtility.updateProjectIcon();
 			LighthouseModel.getInstance().fireModelChanged();
 		} 
 		thread = new DatabaseActionsThread(buffer);
 		thread.start(context);
 	}
 
 	@Override
 	public void stop(BundleContext context) throws Exception {
 		Activator.getDefault().getPreferenceStore()
 		.removePropertyChangeListener(this);
 		thread.stop(context);
 		savePersistableResources();
 	}
 	
 	private void loadPersistableResources() {
 		logger.info("Loading persistable resources...");
 		//workingCopy = (WorkingCopy) PersistableRegistry.getInstance(WorkingCopy.class);
 		buffer = (DatabaseActionsBuffer) PersistableRegistry.getInstance(DatabaseActionsBuffer.class);
 		model = (LighthouseModel) PersistableRegistry.getInstance(LighthouseModel.class);
 	}
 	
 	private void savePersistableResources() {
 		PersistableRegistry.saveInstances();
 	}
 	
 	@Override
 	public void open(final IFile iFile, boolean hasErrors) {
 		// DO NOTHING
 	}
 
 	@Override
 	public void close(IFile iFile, boolean hasErrors) {
 		// DO NOTHING
 	}
 
 	@Override
 	public void change(final IFile iFile, boolean hasErrors) {
 		if (ModelUtility.belongsToImportedProjects(iFile, false)) {
 			if (ignorefilesJustUpdated.contains(iFile)) {
 				ignorefilesJustUpdated.remove(iFile);
 			} else {
 				try {
 					IFile previousIFile = getPreviousVersion(iFile);
 					if (previousIFile != null) {
 						generateDeltaAndSaveIntoModel(previousIFile, iFile);
 						removeIFile(previousIFile);
 					}
 				} catch (Exception e) {
 					logger.error(e, e);
 				}
 			}
 		}
 	}
 
 	private void generateDeltaAndSaveIntoModel(IFile previousIFile, IFile currentIFile) {
 		try {
 			LighthouseFile previousLhFile = parseIFile(previousIFile);
 			LighthouseFile currentLhFile = parseIFile(currentIFile);
 
 			LighthouseDelta delta = new LighthouseDelta(
 					Activator.getDefault().getAuthor(),
 					previousLhFile,
 					currentLhFile);
 
 			LinkedHashSet<LighthouseEvent> deltaEvents = delta.getEvents();
  
 			if (deltaEvents.size() == 0) {
 				logger.error("We have an workspace event without delta elements (delta==0)");
 			} else {
 				logger.debug("Workspace event generated delta events: "+ deltaEvents.size());
 			}
 			
 			ModelUtility.updateEvents(deltaEvents);
 			ModelUtility.fireModificationsToUI(deltaEvents);
 			
 			FileEventAction fileEventAction = new FileEventAction(deltaEvents);
 			buffer.offer(fileEventAction);
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
 	 * The method add run when a class is added in the workspace or when
 	 * the user checkout a project in a empty workspace.
 	 */
 	@Override
 	public void add(IFile iFile, boolean hasErrors) {
 		if (ModelUtility.belongsToImportedProjects(iFile, false)) {
 			if (!existsInSVN(iFile)) {
 				try {
 					generateDeltaAndSaveIntoModel(null, iFile);
 				} catch (Exception e) {
 					logger.error(e, e);
 				}
 			}
 		}
 	}
 
 	@Override
 	public void remove(IFile iFile, boolean hasErrors) {
 		if (ModelUtility.belongsToImportedProjects(iFile, false)) {
 			// FIXME (tproenca): URGENT Gambis pra pegar FQN pelo caminho do arquivo. Melhorar
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
 					generateDeltaAndSaveIntoModel(previousIFile, null);
 					removeIFile(previousIFile);
				} else { // there is no history for this file
					// FIXME Tiago is trying to get the remove event before actually remove the file
 				}
 			} catch (Exception e) {
 				logger.error(e, e);
 			}
 		}
 	}
 
 	
 	@Override
 	public void checkout(Map<IFile, ISVNInfo> svnFiles) {
 		CheckoutAction checkoutAction = new CheckoutAction(svnFiles);
 		buffer.offer(checkoutAction);
 	}
 
 	@Override
 	public void update(Map<IFile, ISVNInfo> svnFiles) {
 		// Ignore in this files in the change() event, because we don't want to
 		// generate deltas for other people changes.
 		ignorefilesJustUpdated.addAll(svnFiles.keySet());
 		
 		UpdateAction updateAction = new UpdateAction(svnFiles);
 		buffer.offer(updateAction);
 	}
 
 	@Override
 	public void commit(Map<IFile, ISVNInfo> svnFiles) {		
 		// Get events for committing from SVN files.
 		Collection<LighthouseEvent> eventsforCommitting = ModelUtility
 				.getEventsForCommiting(svnFiles);
 
 		// Remove committed events and artifacts from the model.
 		LighthouseModelManager manager = new LighthouseModelManager(model);
 		manager.removeCommittedEventsAndArtifacts(ModelUtility.getClassesFullyQualifiedName(svnFiles));
 
 		// Refresh the UI.
 		ModelUtility.fireModificationsToUI(eventsforCommitting);
 		
 		CommitAction commitAction = new CommitAction(eventsforCommitting);
 		buffer.offer(commitAction);
 	}
 
 	@Override
 	public void conflict(Map<IFile, ISVNInfo> svnFiles) {
 		// DO NOTHING
 	}
 
 	@Override
 	public void propertyChange(PropertyChangeEvent event) {
 		try {
 			// If the property that changed is from Lighthouse preferences.
 			if (event.getProperty().indexOf(DatabasePreferences.ROOT) != -1) {
 //				DatabaseUtility.canConnect(DatabasePreferences
 //						.getDatabaseSettings());
 //				StatusWidget.getInstance().setStatus(Status.OK_STATUS);
 //				synchronizeModelWithDatabase(true);
 //				if (threadSuspended) {
 //					synchronized (this) {
 //						StatusWidget.getInstance().setStatus(Status.OK_STATUS);
 //						threadSuspended = false;
 //						notify();
 //					}
 //				}
 				thread.pause();
 				buffer.clear();
 				buffer.offer(new JobDecoratorAction(new SynchronizeModelAction(WorkbenchUtility.getSVNInfoFromWorkspace())));
 				thread.play();
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
 		//workingCopy = getWorkingCopyFromWorkspace();
 		IDatabaseAction synchronizeModelAction = new SynchronizeModelAction(WorkbenchUtility.getSVNInfoFromWorkspace());
 		buffer.offer(synchronizeModelAction);
 	}
 
 // FIXME I can remove this code (I want to see the JOB code)
 //	private void synchronizeModelWithDatabase(final boolean forceNewConnection){
 //		final Job job = new Job("Synchronizing Model...") {
 //			@Override
 //			protected IStatus run(IProgressMonitor monitor) {
 //				try {
 //					monitor.beginTask("Synchronizing model with database...", IProgressMonitor.UNKNOWN);
 //					if (forceNewConnection){
 //						JPAUtility.initializeEntityManagerFactory(DatabasePreferences
 //								.getDatabaseSettings());
 //					}
 //					mapClassToSVNCommittedTime = getWorkingCopyFromWorkspace();
 //					LighthouseModel.getInstance().clear();
 //					checkoutWorkingCopy(mapClassToSVNCommittedTime);
 //					WorkbenchUtility.updateProjectIcon();
 //					//LighthouseModel.getInstance().fireModelChanged();
 //				} catch (JPAException e) {
 //					logger.error(e,e);
 //					UserDialog.openError("JPAException: "+e.getMessage());
 //				} finally {
 //					monitor.done();
 //				}
 //				return Status.OK_STATUS;
 //			}
 //		};
 //		job.setUser(true);
 //		job.schedule();
 //	}
 
 //	private WorkingCopy getWorkingCopyFromWorkspace(){
 //		WorkingCopy result = new WorkingCopy();
 //		IWorkspace workspace = ResourcesPlugin.getWorkspace();
 //		IProject[] projects = workspace.getRoot().getProjects();
 //		try {
 //			ISVNClientAdapter svnAdapter = SVNProviderPlugin.getPlugin()
 //			.getSVNClient();
 //			for (IProject project : projects) {
 //				if (project.isOpen()) {
 //					try {
 //						IJavaProject jProject = (IJavaProject) project
 //						.getNature(JavaCore.NATURE_ID);
 //						if (jProject != null) {
 //							Collection<IFile> iFiles = WorkbenchUtility
 //							.getFilesFromJavaProject(jProject);
 //							for (IFile iFile : iFiles) {
 //								try {
 //									ISVNInfo svnInfo = svnAdapter
 //									.getInfoFromWorkingCopy(iFile
 //											.getLocation().toFile());
 //									String fqn = ModelUtility
 //									.getClassFullyQualifiedName(iFile);
 //									if (fqn != null) {
 //										Date revision = svnInfo
 //										.getLastChangedDate();
 //										if (revision == null) {
 //											revision = new Date(0);
 //										} else {
 //											revision = new Date(revision
 //													.getTime());
 //										}
 //										result.put(fqn, revision);
 //									}
 //								} catch (SVNClientException ex1) {
 //									logger.error(ex1);
 //								}
 //							}
 //						}
 //					} catch (CoreException e) {
 //						logger.error(e, e);
 //					}
 //
 //				}
 //			}
 //		} catch (SVNException ex) {
 //			logger.error(ex, ex);
 //		}
 //		return result;
 //	}
 
 //	public static Controller getInstance(){
 //		if (instance == null) {
 //			instance = new Controller();
 //		}
 //		return instance;
 //	}
 
	public IFile getPreviousVersion(IFile from) throws Exception{
		String tempWorkspaceResource = from.getFullPath().toOSString();
		String fileShortName = from.getFullPath().toFile().getName();
		tempWorkspaceResource = tempWorkspaceResource.replaceAll(fileShortName, "TEMP_RESOURCE_LHPreviousFile.java");
 
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
 	
 	private boolean existsInSVN(IFile iFile) {
 		boolean result = false;
 		try {
 			ISVNClientAdapter svnAdapter = SVNProviderPlugin.getPlugin()
 			.getSVNClient();
 			 ISVNStatus status = svnAdapter.getSingleStatus(iFile.getFullPath().toFile());
 			 result = status.getTextStatus() != SVNStatusKind.UNVERSIONED;
 		} catch (Exception e) {
 			logger.error(e, e);
 		}
 		return result;
 	}
 
 	private void removeIFile(IFile iFile) throws CoreException {
 		iFile.delete(true, null);
 	}
 
 }
