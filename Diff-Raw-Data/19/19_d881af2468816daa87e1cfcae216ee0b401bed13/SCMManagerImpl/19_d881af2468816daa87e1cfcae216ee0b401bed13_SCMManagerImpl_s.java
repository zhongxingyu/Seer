 package com.photon.phresco.framework.impl;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 import java.util.UUID;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.log4j.Logger;
 import org.eclipse.jgit.api.Git;
 import org.eclipse.jgit.api.ListBranchCommand.ListMode;
 import org.tmatesoft.svn.core.SVNDepth;
 import org.tmatesoft.svn.core.SVNDirEntry;
 import org.tmatesoft.svn.core.SVNException;
 import org.tmatesoft.svn.core.SVNNodeKind;
 import org.tmatesoft.svn.core.SVNURL;
 import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
 import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
 import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
 import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
 import org.tmatesoft.svn.core.internal.wc.DefaultSVNOptions;
 import org.tmatesoft.svn.core.io.SVNRepository;
 import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
 import org.tmatesoft.svn.core.wc.SVNClientManager;
 import org.tmatesoft.svn.core.wc.SVNRevision;
 import org.tmatesoft.svn.core.wc.SVNUpdateClient;
 import org.tmatesoft.svn.core.wc.SVNWCUtil;
 
 import com.google.gson.Gson;
 import com.photon.phresco.commons.model.ApplicationInfo;
 import com.photon.phresco.commons.model.ProjectInfo;
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.framework.api.SCMManager;
 import com.photon.phresco.util.FileUtil;
 import com.photon.phresco.util.Utility;
 import com.phresco.pom.util.PomProcessor;
 
 public class SCMManagerImpl implements SCMManager {
 
 	private static final Logger S_LOGGER = Logger.getLogger(SCMManagerImpl.class);
 	private static final String SVN_CHECKOUT_TEMP = "svn-checkout-temp";
 	private static final String PROJECT_INFO = "project.info";
 	private static final String PHRESCO = "/.phresco";
 	String IMPORT_SUCCESS_PROJECT = "Project imported successfully";
 	String HEAD_REVISION = "HEAD";
 	String FOLDER_DOT_PHRESCO = ".phresco";
 //	private String revisionVal = ""; TODO : need this validation in web
 	private static boolean dotphresco;
 
 	public void importProject(String type, String url, String username,
 			String password, String branch, String revision, String projcode)
 			throws Exception {
 		S_LOGGER.debug("Entering Method  Applications.importApplication()");
 		S_LOGGER.debug("repoType " + type);
 		S_LOGGER.debug("repositoryUrl " + url);
 		try {
 			if ("svn".equals(type)) {
 				validateURL(url, username, password);
 				if (!dotphresco) {
 					System.out.println("Phresco Project definition not found");
 				} else if (dotphresco == true) {
 					DAVRepositoryFactory.setup();
 					SVNURL svnURL = SVNURL.parseURIEncoded(url);
 					DefaultSVNOptions options = new DefaultSVNOptions();
 					SVNClientManager cm = SVNClientManager.newInstance(options, username, password);
 					File checkOutDir = new File(Utility.getProjectHome());
 //					revision = HEAD_REVISION.equals(revision) ? revision : revisionVal;
 					// getProjectInfo(revision);
 					File projectRoot = new File(checkOutDir, projcode);
 
 					if (projectRoot.exists()) {
 						throw new PhrescoException("Import Fails", "Project with the code " + projcode + " already present");
 					}
 					SVNUpdateClient uc = cm.getUpdateClient();
 					uc.doCheckout(SVNURL.parseURIEncoded(url), projectRoot, SVNRevision.UNDEFINED, SVNRevision.parse(revision), SVNDepth.UNKNOWN, false);
 					System.out.println("Completed!!!!");
 				}
 			} else if ("git".equals(type)) {
 				String uuid = UUID.randomUUID().toString();
 				File gitImportTemp = new File(Utility.getPhrescoTemp(), uuid);
 				S_LOGGER.debug("gitImportTemp " + gitImportTemp);
 				if (gitImportTemp.exists()) {
 					S_LOGGER.debug("Empty git directory need to be removed before importing from git ");
 					FileUtils.deleteDirectory(gitImportTemp);
 				}
 				S_LOGGER.debug("gitImportTemp " + gitImportTemp);
 				importFromGit(url, gitImportTemp);
 				ApplicationInfo appInfo = GITgetAppInfo(gitImportTemp);
 				S_LOGGER.debug(appInfo.getCode());
 				importToWorkspace(gitImportTemp, Utility.getProjectHome(), projcode);
 				// update connection in pom.xml
 				updateSCMConnection(appInfo.getCode(), url);
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void updateProject(String type, String url, String username,
 			String password, String branch, String revision, String projcode)
 			throws Exception {
 		try {
 			if ("svn".equals(type)) {
 				DAVRepositoryFactory.setup();
 				SVNURL svnURL = SVNURL.parseURIEncoded(url);
 				DefaultSVNOptions options = new DefaultSVNOptions();
 				SVNClientManager cm = SVNClientManager.newInstance(options, username, password);
 				S_LOGGER.debug("update SCM Connection " + url);
 				S_LOGGER.debug("userName " + username);
 				S_LOGGER.debug("Repo type " + type);
 				S_LOGGER.debug("projectCode " + projcode);
 				// updateSCMConnection(projcode, url);
 
 //				revision = HEAD_REVISION.equals(revision) ? revision
 //						: revisionVal;
 				File updateDir = new File(Utility.getProjectHome(), projcode);
 				S_LOGGER.debug("updateDir SVN... " + updateDir);
 				System.out.println("Updating...");
 				SVNUpdateClient uc = cm.getUpdateClient();
 				uc.doUpdate(updateDir, SVNRevision.parse(revision), false);
 				System.out.println("Updated!");
 
 			} else if ("git".equals(type)) {
 				S_LOGGER.debug("update SCM Connection " + url);
 				S_LOGGER.debug("userName " + username);
 				S_LOGGER.debug("Repo type " + type);
 				S_LOGGER.debug("projectCode " + projcode);
 				// updateSCMConnection(projcode, url);
 
 				File updateDir = new File(Utility.getProjectHome(), projcode);
 				System.out.println("Updating...");
 				S_LOGGER.debug("updateDir GIT... " + updateDir);
 				Git git = Git.open(updateDir); // checkout is the folder with
 												// .git
 				git.pull().call(); // succeeds
 				System.out.println("Updated!");
 
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void importToWorkspace(File gitImportTemp, String projectHome,
 			String code) throws Exception {
 		S_LOGGER.debug("Entering Method  Applications.importToWorkspace()");
 		File workspaceProjectDir = new File(projectHome + code);
 		S_LOGGER.debug("workspaceProjectDir " + workspaceProjectDir);
 		if (workspaceProjectDir.exists()) {
 			S_LOGGER.debug("workspaceProjectDir exists" + workspaceProjectDir);
 			throw new org.apache.commons.io.FileExistsException();
 		}
 		S_LOGGER.debug("gitImportTemp ====> " + gitImportTemp);
 		S_LOGGER.debug("workspaceProjectDir ====> " + workspaceProjectDir);
 		FileUtils.copyDirectory(gitImportTemp, workspaceProjectDir);
 		try {
 			FileUtils.deleteDirectory(gitImportTemp);
 		} catch (IOException e) {
 			S_LOGGER.debug("pack file is not deleted " + e.getLocalizedMessage());
 		}
 	}
 
 	private ApplicationInfo GITgetAppInfo(File gitImportTemp) throws Exception {
 		S_LOGGER.debug("Entering Method Applications.getProjectInfo()");
 		BufferedReader reader = null;
 		try {
 			File dotProjectFile = new File(gitImportTemp, FOLDER_DOT_PHRESCO + File.separator + PROJECT_INFO);
 			S_LOGGER.debug("dotProjectFile" + dotProjectFile);
 			if (!dotProjectFile.exists()) {
 				throw new PhrescoException("Phresco Project definition not found");
 			}
 			reader = new BufferedReader(new FileReader(dotProjectFile));
 			return new Gson().fromJson(reader, ApplicationInfo.class);
 		} finally {
 			Utility.closeStream(reader);
 		}
 	}
 
 	private boolean importFromGit(String url, File gitImportTemp) throws Exception {
 		S_LOGGER.debug("Entering Method  Applications.importFromGit()");
 		S_LOGGER.debug("importing git " + url);
 		Git repo = Git.cloneRepository().setURI(url).setDirectory(gitImportTemp).call();
 		for (org.eclipse.jgit.lib.Ref b : repo.branchList().setListMode(ListMode.ALL).call()) {
 			S_LOGGER.debug("(standard): cloned branch " + b.getName());
 		}
 		repo.getRepository().close();
 		return true;
 
 	}
 
 	private void updateSCMConnection(String projCode, String repoUrl) throws Exception {
 		PomProcessor processor = getPomProcessor(projCode);
 		if (processor.getSCM() == null) {
 			S_LOGGER.debug("processor.getSCM() exists and repo url " + repoUrl);
 			processor.setSCM(repoUrl, "", "", "");
 			processor.save();
 		}
 	}
 
 	private PomProcessor getPomProcessor(String projCode) throws PhrescoException {
 		try {
 			StringBuilder builder = new StringBuilder(Utility.getProjectHome());
 			builder.append(projCode);
 			builder.append(File.separatorChar);
 			builder.append("POM_XML");
 			S_LOGGER.debug("builder.toString() " + builder.toString());
 			File pomPath = new File(builder.toString());
 			S_LOGGER.debug("file exists " + pomPath.exists());
 			return new PomProcessor(pomPath);
 		} catch (Exception e) {
 			throw new PhrescoException(e);
 		}
 	}
 
 	private ProjectInfo getProjectInfo(String url, String username, String password, String revision) throws Exception {
 		BufferedReader reader = null;
 		File tempDir = new File(Utility.getSystemTemp(), SVN_CHECKOUT_TEMP);
 		System.out.println("temp dir : SVNAccessor " + tempDir);
 		try {
 			SVNURL svnURL = SVNURL.parseURIEncoded(url);
 			DefaultSVNOptions options = new DefaultSVNOptions();
 			SVNClientManager cm = SVNClientManager.newInstance(options, username, password);
 			
 			SVNUpdateClient uc = cm.getUpdateClient();
 			uc.doCheckout(svnURL.appendPath(PHRESCO, true), tempDir, SVNRevision.UNDEFINED, SVNRevision.parse(revision), SVNDepth.UNKNOWN, false);
 
 			File dotProjectFile = new File(tempDir, PROJECT_INFO);
 			if (!dotProjectFile.exists()) {
 				throw new PhrescoException("Phresco Project definition not found");
 			}
 
 			reader = new BufferedReader(new FileReader(dotProjectFile));
 			return new Gson().fromJson(reader, ProjectInfo.class);
 		} finally {
 			Utility.closeStream(reader);
 			if (tempDir.exists()) {
 				FileUtil.delete(tempDir);
 			}
 		}
 	}
 
 	public static void validateURL(String url, String name, String password) throws Exception {
 		setupLibrary();
 		SVNRepository repository = null;
 		try {
 			repository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(url));
 		} catch (SVNException svne) {
 			System.err.println("error while creating an SVNRepository for location '" + url + "': " + svne.getMessage());
 			System.exit(1);
 		}
 		ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(name, password);
 		repository.setAuthenticationManager(authManager);
 		try {
 			SVNNodeKind nodeKind = repository.checkPath("", -1);
 			if (nodeKind == SVNNodeKind.NONE) {
 				System.out.println("There is no entry at '" + url + "'.");
 //				System.exit(1);
 			} else if (nodeKind == SVNNodeKind.FILE) {
 				System.out.println("The entry at '" + url + "' is a file while a directory was expected.");
 //				System.exit(1);
 			}
 			System.out.println("Repository Root: " + repository.getRepositoryRoot(true));
 			System.out.println("Repository UUID: " + repository.getRepositoryUUID(true));
 			validateRootDir(repository, "", url);
 		} catch (SVNException svne) {
 			System.out.println("error while listing entries: " + svne.getMessage());
 //			System.exit(1);
 		}
 	}
 
 	private static void setupLibrary() {
 		DAVRepositoryFactory.setup();
 		SVNRepositoryFactoryImpl.setup();
 		FSRepositoryFactory.setup();
 	}
 
 	public static void validateRootDir(SVNRepository repository, String path, String url) throws Exception {
 		List<String> list = new ArrayList<String>();
 		Collection entries = repository.getDir(path, -1, null, (Collection) null);
 		Iterator iterator = entries.iterator();
 		if (entries.size() != 0) {
 			while (iterator.hasNext()) {
 				SVNDirEntry entry = (SVNDirEntry) iterator.next();
 				// System.out.println(entry.getURL().removePathTail());
 				// System.out.println();
 				if ((entry.getKind() == SVNNodeKind.DIR)
 						&& (entry.getName().equals(".phresco"))) {
 					System.out.println("iteration 1 Phresco Project definition found @ " + entry.getURL().removePathTail());
 					dotphresco = true;
 					break;
 				} else if (entry.getKind() == SVNNodeKind.DIR) {
 					System.out.println(entry.getName() + " added to list @ iterate1 ");
 					list.add(entry.getName().toString());
 				}
 			}
 			if (dotphresco == false) {
 				for (int i = 0; i < list.size(); i++) {
 					validateChildDir(repository, (path.equals("")) ? list.get(i) : path + "/" + list.get(i), url);
 				}
 			}
 		} else {
 			System.out.println("No Entries in the given directory, try another url!");
 		}
 	}
 
 	public static void validateChildDir(SVNRepository repository, String path, String url) throws Exception {
 		Collection entries1 = repository.getDir(path, -1, null, (Collection) null);
 		Iterator iterator1 = entries1.iterator();
 		while (iterator1.hasNext()) {
 			SVNDirEntry entry = (SVNDirEntry) iterator1.next();
 			// System.out.println(entry.getURL().removePathTail());
 			// System.out.println();
 			if ((entry.getKind() == SVNNodeKind.DIR)
 					&& (entry.getName().equals(".phresco"))) {
 				System.out.println();
 				System.out.println("iteration 2 Phresco Project definition found @ " + entry.getURL().removePathTail());
 				System.out.println();
 				dotphresco = true;
 				break;
 			}
 		}
 	}
 }
