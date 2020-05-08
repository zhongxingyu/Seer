 /**
  *
  */
 package org.jabylon.team.svn.impl;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 
 import org.apache.felix.scr.annotations.Component;
 import org.apache.felix.scr.annotations.Property;
 import org.apache.felix.scr.annotations.Service;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.SubMonitor;
 import org.eclipse.emf.common.util.URI;
 import org.jabylon.common.team.TeamProvider;
 import org.jabylon.common.team.TeamProviderException;
 import org.jabylon.common.util.PreferencesUtil;
 import org.jabylon.properties.DiffKind;
 import org.jabylon.properties.ProjectVersion;
 import org.jabylon.properties.PropertiesFactory;
 import org.jabylon.properties.PropertyFileDescriptor;
 import org.jabylon.properties.PropertyFileDiff;
 import org.osgi.service.prefs.Preferences;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.tmatesoft.svn.core.SVNCancelException;
 import org.tmatesoft.svn.core.SVNCommitInfo;
 import org.tmatesoft.svn.core.SVNDepth;
 import org.tmatesoft.svn.core.SVNException;
 import org.tmatesoft.svn.core.SVNURL;
 import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
 import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
 import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
 import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
 import org.tmatesoft.svn.core.wc.ISVNEventHandler;
 import org.tmatesoft.svn.core.wc.ISVNStatusHandler;
 import org.tmatesoft.svn.core.wc.SVNClientManager;
 import org.tmatesoft.svn.core.wc.SVNCommitClient;
 import org.tmatesoft.svn.core.wc.SVNEvent;
 import org.tmatesoft.svn.core.wc.SVNEventAction;
 import org.tmatesoft.svn.core.wc.SVNRevision;
 import org.tmatesoft.svn.core.wc.SVNStatus;
 import org.tmatesoft.svn.core.wc.SVNStatusClient;
 import org.tmatesoft.svn.core.wc.SVNStatusType;
 import org.tmatesoft.svn.core.wc.SVNWCClient;
 import org.tmatesoft.svn.core.wc.SVNWCUtil;
 
 /**
  * @author Johannes Utzig (jutzig.dev@googlemail.com)
  * 
  */
 @Component(enabled = true, immediate = true)
 @Service
 public class SVNTeamProvider implements org.jabylon.common.team.TeamProvider {
 
 	private static final Logger logger = LoggerFactory.getLogger(SVNTeamProvider.class);
 
 	@Property(value = "SVN")
 	private static String KEY_KIND = TeamProvider.KEY_KIND;
 
 	static {
 		init();
 
 	}
 
 	@Override
 	public void checkout(ProjectVersion project, final IProgressMonitor monitor) {
 
 		SubMonitor sub = SubMonitor.convert(monitor);
 		sub.beginTask("Checking out", 100);
 		SVNClientManager manager = null;
 		try {
 			manager = createSVNClientManager(project);
 			checkDirectories(project);
 			monitor.worked(5);
 			SVNURL svnurl = createSVNURL(project);
 			logger.info("Checking out " + svnurl);
 			File targetDir = new File(project.absoluteFilePath().path());
 			manager.getUpdateClient().setEventHandler(new ProgressMonitorHandler(sub.newChild(1000), targetDir.getPath()));
			long revision = manager.getUpdateClient().doCheckout(svnurl, targetDir, SVNRevision.HEAD, SVNRevision.HEAD, SVNDepth.INFINITY, true);
 			logger.info("Checkout successful at revision {}", revision);
 
 		} catch(SVNException e) {
 			// delete directory if checkout fails, otherwise we present update /
 			// commit actions instead
 			cleanupProjectVersionDirectory(project);
 			throw new TeamProviderException(e.getMessage(), e);			
 		}
 		catch (Exception e) {
 			// delete directory if checkout fails, otherwise we present update /
 			// commit actions instead
 			cleanupProjectVersionDirectory(project);
 			throw new TeamProviderException("Checkout failed", e);
 		} finally {
 			if (manager != null)
 				manager.dispose();
 
 			if (monitor != null)
 				monitor.done();
 		}
 	}
 
 	private SVNURL createSVNURL(ProjectVersion projectVersion) throws SVNException {
 		Preferences prefs = PreferencesUtil.scopeFor(projectVersion.getParent());
 		URI uri = projectVersion.getParent().getRepositoryURI();
 		String branch = projectVersion.getName();
 		//if it's not trunk we need the branches folder
 		if(!"trunk".equals(branch))
			uri = uri.appendSegment("branches");
		uri = uri.appendSegment(branch);
 		if (prefs.get(SVNConstants.KEY_MODULE, null) != null)
			uri = uri.appendSegments(prefs.get(SVNConstants.KEY_MODULE, null).split("/"));
 
 		return SVNURL.parseURIEncoded(uri.toString());
 	}
 
 	private static void init() {
 		SVNRepositoryFactoryImpl.setup();
 		DAVRepositoryFactory.setup();
 		FSRepositoryFactory.setup();
 	}
 
 	private void cleanupProjectVersionDirectory(ProjectVersion project) {
 		File versionDir = new File(project.absolutPath().toFileString());
 		try {
 			versionDir.delete();
 		} catch (Exception e) {
 			// ignore
 		}
 	}
 
 	private void checkDirectories(ProjectVersion projectVersion) {
 		File projectDir = new File(projectVersion.getParent().absoluteFilePath().toFileString());
 		if (!projectDir.exists()) {
 			if (!projectDir.mkdirs())
 				throw new TeamProviderException("Checkout failed. Unable to create project directory");
 		}
 	}
 
 	@Override
 	public void commit(ProjectVersion project, final IProgressMonitor monitor) {
 		SVNClientManager manager = null;
 		SubMonitor subMon = SubMonitor.convert(monitor);
 		try {
 			manager = createSVNClientManager(project);
 			final String fullPath = project.absolutPath().toFileString();
 			subMon.beginTask("Calculating local changes", 100);
 
 			List<File> filesToAdd = calculateChangedFiles(new File(fullPath), manager, subMon.newChild(20));
 			SVNWCClient wcClient = manager.getWCClient();
 			for (File file : filesToAdd) {
 				logger.info("SVN ADD {}", file.getPath());
 				wcClient.doAdd(file, true, false, false, SVNDepth.FILES, false, true);
 			}
 			subMon.worked(10);
 			SVNCommitClient commitClient = manager.getCommitClient();
 			subMon.setTaskName("Committing");
 			commitClient.setEventHandler(new ProgressMonitorHandler(subMon.newChild(70), fullPath));
 			if(filesToAdd.isEmpty())
 				logger.info("Nothing to commit");
 			else
 			{
 				SVNCommitInfo commitInfo = commitClient.doCommit(filesToAdd.toArray(new File[filesToAdd.size()]), false, "Jabylon Auto-Sync Up", null, null, false,
 						false, SVNDepth.FILES);
 				if (commitInfo.getErrorMessage() != null)
 					throw new TeamProviderException(commitInfo.getErrorMessage().getMessage(), commitInfo.getErrorMessage().getCause());
 				logger.info("SVN commit successfully at revision {}", commitInfo.getNewRevision());				
 			}
 		} catch (SVNException e) {
 			throw new TeamProviderException(e.getMessage(), e);
 		} catch (IOException e) {
 			throw new TeamProviderException("Commit failed",e);
 		} finally {
 			if (manager != null)
 				manager.dispose();
 			if (monitor != null)
 				monitor.done();
 		}
 
 	}
 
 	private SVNClientManager createSVNClientManager(ProjectVersion project) throws SVNException {
 		SVNClientManager manager = SVNClientManager.newInstance(SVNWCUtil.createDefaultOptions(false), createAuthenticationManager(project));
 		manager.createRepository(createSVNURL(project), true);;
 		return manager;
 	}
 
 	private ISVNAuthenticationManager createAuthenticationManager(ProjectVersion projectVersion) {
 		Preferences prefs = PreferencesUtil.scopeFor(projectVersion.getParent());
 		String username = prefs.get(SVNConstants.KEY_USERNAME, null);
 		String password = prefs.get(SVNConstants.KEY_PASSWORD, null);
 		return SVNWCUtil.createDefaultAuthenticationManager(username, password);
 	}
 
 	private List<File> calculateChangedFiles(File parentDir, SVNClientManager manager, IProgressMonitor monitor) throws IOException, SVNException {
 		final List<File> filesToAdd = new ArrayList<File>();
 		SVNStatusClient statusClient = manager.getStatusClient();
 		monitor.beginTask("Computing changed files", 100);
 		statusClient.setEventHandler(new ProgressMonitorHandler(monitor, parentDir.getAbsolutePath()));
 		statusClient.doStatus(parentDir, SVNRevision.WORKING, SVNDepth.INFINITY, false, false, false, false, new ISVNStatusHandler() {
 
 			@Override
 			public void handleStatus(SVNStatus status) throws SVNException {
 				SVNStatusType type = status.getNodeStatus();
 				if (type.equals(SVNStatusType.STATUS_MISSING))
 					filesToAdd.add(status.getFile());
 				else if (type.equals(SVNStatusType.STATUS_MODIFIED))
 					filesToAdd.add(status.getFile());
 				else if (type.equals(SVNStatusType.STATUS_UNVERSIONED))
 					filesToAdd.add(status.getFile());
 				else if (type.equals(SVNStatusType.STATUS_ADDED))
 					filesToAdd.add(status.getFile());
 				else if (type.equals(SVNStatusType.STATUS_DELETED))
 					filesToAdd.add(status.getFile());
 			}
 		}, null);
 		monitor.done();
 		return filesToAdd;
 	}
 
 	@Override
 	public void commit(PropertyFileDescriptor descriptor, IProgressMonitor monitor) {
 
 	}
 
 	@Override
 	public Collection<PropertyFileDiff> update(ProjectVersion project, IProgressMonitor monitor) throws TeamProviderException {
 		SubMonitor subMon = SubMonitor.convert(monitor);
 		SVNClientManager manager = null;
 		try {
 			manager = createSVNClientManager(project);
 			final String fullPath = project.absolutPath().toFileString();
 			subMon.beginTask("Fetching remote changes", 100);
 			DiffHandler handler = new DiffHandler(subMon, fullPath);
 			manager.getUpdateClient().setEventHandler(handler);
 			long revision = manager.getUpdateClient().doUpdate(new File(fullPath), SVNRevision.HEAD, SVNDepth.INFINITY, false, false);
 			logger.info("SVN update to revision {} successfull", revision);
 			return handler.getDiff();
 		} catch (SVNException e) {
 			throw new TeamProviderException(e.getMessage(), e);
 		} catch (Exception e) {
 			throw new TeamProviderException("Update failed", e);
 		} finally {
 			if (manager != null)
 				manager.dispose();
 			if (monitor != null)
 				monitor.done();
 		}
 	}
 
 	@Override
 	public Collection<PropertyFileDiff> update(PropertyFileDescriptor descriptor, IProgressMonitor monitor) throws TeamProviderException {
 		// TODO : implement
 		return Collections.emptyList();
 	}
 
 }
 
 class ProgressMonitorHandler implements ISVNEventHandler {
 
 	private final IProgressMonitor monitor;
 	private String basePath;
 
 	public ProgressMonitorHandler(IProgressMonitor monitor, String basePath) {
 		super();
 		this.monitor = monitor;
 		this.basePath = basePath;
 	}
 
 	@Override
 	public void handleEvent(SVNEvent event, double progress) throws SVNException {
 		File file = event.getFile();
 		if(file!=null) {
 			monitor.subTask(truncatePath(deresolve(file.getPath())));
 			monitor.worked(1);			
 		}
 
 	}
 
 	protected String truncatePath(String path) {
 		if (path.length() > 50)
 			return "..." + path.substring(path.length() - 50);
 		return path;
 	}
 
 	protected String deresolve(String path) {
 		return path.substring(basePath.length());
 	}
 
 	@Override
 	public void checkCancelled() throws SVNCancelException {
 		if (monitor.isCanceled())
 			throw new SVNCancelException();
 
 	}
 
 }
 
 class DiffHandler extends ProgressMonitorHandler {
 
 	private List<PropertyFileDiff> diff;
 
 	public DiffHandler(IProgressMonitor monitor, String basePath) {
 		super(monitor, basePath);
 		diff = new ArrayList<PropertyFileDiff>();
 	}
 
 	@Override
 	public void handleEvent(SVNEvent event, double progress) throws SVNException {
 		super.handleEvent(event, progress);
 		SVNEventAction action = event.getAction();
 		if (action.equals(SVNEventAction.UPDATE_REPLACE) || action.equals(SVNEventAction.UPDATE_UPDATE)) {
 			PropertyFileDiff fileDiff = PropertiesFactory.eINSTANCE.createPropertyFileDiff();
 			fileDiff.setKind(DiffKind.MODIFY);
 			fileDiff.setOldPath(deresolve(event.getFile().getAbsolutePath()));
 			fileDiff.setNewPath(deresolve(event.getFile().getAbsolutePath()));
 			diff.add(fileDiff);
 		} else if (action.equals(SVNEventAction.UPDATE_ADD)) {
 			PropertyFileDiff fileDiff = PropertiesFactory.eINSTANCE.createPropertyFileDiff();
 			fileDiff.setKind(DiffKind.ADD);
 			fileDiff.setNewPath(deresolve(event.getFile().getAbsolutePath()));
 			diff.add(fileDiff);
 		} else if (action.equals(SVNEventAction.UPDATE_DELETE)) {
 			PropertyFileDiff fileDiff = PropertiesFactory.eINSTANCE.createPropertyFileDiff();
 			fileDiff.setKind(DiffKind.REMOVE);
 			fileDiff.setOldPath(deresolve(event.getFile().getAbsolutePath()));
 			diff.add(fileDiff);
 		}
 	}
 
 	public List<PropertyFileDiff> getDiff() {
 		return diff;
 	}
 
 }
