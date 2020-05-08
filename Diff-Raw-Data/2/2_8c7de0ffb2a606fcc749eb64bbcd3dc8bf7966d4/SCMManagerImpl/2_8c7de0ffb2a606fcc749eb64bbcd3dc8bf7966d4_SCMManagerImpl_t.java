 /**
  * Phresco Framework Implementation
  *
  * Copyright (C) 1999-2013 Photon Infotech Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *         http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.photon.phresco.framework.impl;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.net.ConnectException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 import java.util.UUID;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.filefilter.FileFilterUtils;
 import org.apache.commons.io.filefilter.IOFileFilter;
 import org.apache.commons.io.filefilter.TrueFileFilter;
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 import org.eclipse.jgit.api.AddCommand;
 import org.eclipse.jgit.api.CommitCommand;
 import org.eclipse.jgit.api.Git;
 import org.eclipse.jgit.api.InitCommand;
 import org.eclipse.jgit.api.PushCommand;
 import org.eclipse.jgit.api.ResetCommand;
 import org.eclipse.jgit.api.ResetCommand.ResetType;
 import org.eclipse.jgit.api.Status;
 import org.eclipse.jgit.api.errors.GitAPIException;
 import org.eclipse.jgit.errors.UnsupportedCredentialItem;
 import org.eclipse.jgit.lib.Repository;
 import org.eclipse.jgit.lib.StoredConfig;
 import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
 import org.eclipse.jgit.transport.CredentialItem;
 import org.eclipse.jgit.transport.CredentialsProvider;
 import org.eclipse.jgit.transport.CredentialsProviderUserInfo;
 import org.eclipse.jgit.transport.JschConfigSessionFactory;
 import org.eclipse.jgit.transport.OpenSshConfig;
 import org.eclipse.jgit.transport.SshSessionFactory;
 import org.eclipse.jgit.transport.URIish;
 import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
 import org.tmatesoft.svn.core.SVNCommitInfo;
 import org.tmatesoft.svn.core.SVNDepth;
 import org.tmatesoft.svn.core.SVNDirEntry;
 import org.tmatesoft.svn.core.SVNException;
 import org.tmatesoft.svn.core.SVNLogEntry;
 import org.tmatesoft.svn.core.SVNNodeKind;
 import org.tmatesoft.svn.core.SVNURL;
 import org.tmatesoft.svn.core.auth.BasicAuthenticationManager;
 import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
 import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
 import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
 import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;
 import org.tmatesoft.svn.core.internal.wc.DefaultSVNOptions;
 import org.tmatesoft.svn.core.io.SVNRepository;
 import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
 import org.tmatesoft.svn.core.wc.ISVNCommitParameters;
 import org.tmatesoft.svn.core.wc.ISVNStatusHandler;
 import org.tmatesoft.svn.core.wc.SVNClientManager;
 import org.tmatesoft.svn.core.wc.SVNCommitClient;
 import org.tmatesoft.svn.core.wc.SVNRevision;
 import org.tmatesoft.svn.core.wc.SVNStatus;
 import org.tmatesoft.svn.core.wc.SVNStatusType;
 import org.tmatesoft.svn.core.wc.SVNUpdateClient;
 import org.tmatesoft.svn.core.wc.SVNWCClient;
 import org.tmatesoft.svn.core.wc.SVNWCUtil;
 
 
 
 
 import com.google.gson.Gson;
 import com.jcraft.jsch.Session;
 import com.jcraft.jsch.UserInfo;
 import com.perforce.p4java.client.IClient;
 import com.perforce.p4java.client.IClientSummary.IClientOptions;
 import com.perforce.p4java.core.ChangelistStatus;
 import com.perforce.p4java.core.IChangelist;
 import com.perforce.p4java.core.IChangelistSummary;
 import com.perforce.p4java.core.file.FileSpecBuilder;
 import com.perforce.p4java.core.file.FileSpecOpStatus;
 import com.perforce.p4java.core.file.IFileSpec;
 import com.perforce.p4java.exception.ConnectionException;
 import com.perforce.p4java.exception.P4JavaException;
 import com.perforce.p4java.exception.RequestException;
 import com.perforce.p4java.impl.generic.client.ClientOptions;
 import com.perforce.p4java.impl.generic.client.ClientView;
 import com.perforce.p4java.impl.generic.client.ClientView.ClientViewMapping;
 import com.perforce.p4java.impl.generic.core.Changelist;
 import com.perforce.p4java.impl.mapbased.client.Client;
 import com.perforce.p4java.impl.mapbased.server.Server;
 import com.perforce.p4java.option.client.AddFilesOptions;
 import com.perforce.p4java.option.client.EditFilesOptions;
 import com.perforce.p4java.option.client.GetDiffFilesOptions;
 import com.perforce.p4java.option.client.ReopenFilesOptions;
 import com.perforce.p4java.option.client.SyncOptions;
 import com.perforce.p4java.option.server.GetChangelistsOptions;
 import com.perforce.p4java.server.IOptionsServer;
 import com.perforce.p4java.server.ServerFactory;
 import com.photon.phresco.commons.FrameworkConstants;
 import com.photon.phresco.commons.LockUtil;
 import com.photon.phresco.commons.model.ApplicationInfo;
 import com.photon.phresco.commons.model.ProjectInfo;
 import com.photon.phresco.exception.PhrescoException;
 import com.photon.phresco.framework.api.SCMManager;
 import com.photon.phresco.framework.model.RepoDetail;
 import com.photon.phresco.framework.model.RepoFileInfo;
 import com.photon.phresco.util.FileUtil;
 import com.photon.phresco.util.Utility;
 import com.phresco.pom.util.PomProcessor;
 
 
 public class SCMManagerImpl implements SCMManager, FrameworkConstants {
 
 	private static final Logger S_LOGGER = Logger.getLogger(SCMManagerImpl.class);
 	private static Boolean debugEnabled = S_LOGGER.isDebugEnabled();
 	boolean dotphresco ;
 	SVNClientManager cm = null;
 
 		public ApplicationInfo importProject(RepoDetail repodetail, String displayName, String uniqueKey) throws Exception {
 		if(debugEnabled){
 			S_LOGGER.debug("Entering Method  SCMManagerImpl.importProject()");
 		}
 		if (SVN.equals(repodetail.getType())) {
 			if(debugEnabled){
 				S_LOGGER.debug("SVN type");
 			}
 
 			SVNURL svnURL = SVNURL.parseURIEncoded(repodetail.getRepoUrl());
 			DAVRepositoryFactory.setup();
 			DefaultSVNOptions options = new DefaultSVNOptions();
 			cm = SVNClientManager.newInstance(options, repodetail.getUserName(), repodetail.getPassword());
 			boolean valid = checkOutFilter(repodetail, svnURL, displayName, uniqueKey);
 			if(debugEnabled){
 				S_LOGGER.debug("Completed");
 			}
 
 			if (valid) {
 				ProjectInfo projectInfo = getSvnAppInfo(repodetail.getRevision(), svnURL);
 				if (projectInfo != null) {
 					if (projectInfo.getId().equals("#SEP#")) {
 						ApplicationInfo appInfo = 	new ApplicationInfo();
 						appInfo.setId("#SEP#");
 						return appInfo;
 					} else {
 						return returnAppInfo(projectInfo);
 					}
 				}
 			} 
 			return null;
 		} else if (GIT.equals(repodetail.getType())) {
 			if(debugEnabled){
 				S_LOGGER.debug("GIT type");
 			}
 			String uuid = UUID.randomUUID().toString();
 			File gitImportTemp = new File(Utility.getPhrescoTemp(), uuid);
 			if(debugEnabled){
 				S_LOGGER.debug("gitImportTemp " + gitImportTemp);
 			}
 			if (gitImportTemp.exists()) {
 				if(debugEnabled){
 					S_LOGGER.debug("Empty git directory need to be removed before importing from git ");
 				}
 				FileUtils.deleteDirectory(gitImportTemp);
 			}
 			if(debugEnabled){
 				S_LOGGER.debug("gitImportTemp " + gitImportTemp);
 			}
 			importFromGit(repodetail, gitImportTemp);
 			if(debugEnabled){
 				S_LOGGER.debug("Validating Phresco Definition");
 			}
 			ApplicationInfo applicationInfo = cloneFilter(gitImportTemp, repodetail.getRepoUrl(), true, displayName, uniqueKey);
 			if (gitImportTemp.exists()) {
 				if(debugEnabled){
 					S_LOGGER.debug("Deleting ~Temp");
 				}
 				FileUtils.deleteDirectory(gitImportTemp);
 			}
 			if(debugEnabled){
 				S_LOGGER.debug("Completed");
 			}
 			if (applicationInfo != null) {
 				return applicationInfo;
 			} 
 		} else if (BITKEEPER.equals(repodetail.getType())) {
 		    return importFromBitKeeper(repodetail.getRepoUrl(), displayName, uniqueKey);
 		}
 		
 		return null;
 	}
 	
 	private ApplicationInfo returnAppInfo(ProjectInfo projectInfo) {
 		List<ApplicationInfo> appInfos = null;
 		ApplicationInfo applicationInfo = null;
 		if (projectInfo != null) {
 		appInfos = projectInfo.getAppInfos();
 		}
 		if (appInfos != null) {
 		applicationInfo = appInfos.get(0);
 		}
 		if (applicationInfo != null) {
 			return applicationInfo;
 		}
 		return null;
 	}
 
 	public boolean updateProject(RepoDetail repodetail, ApplicationInfo appInfo) throws Exception  {
 		if(debugEnabled){
 			S_LOGGER.debug("Entering Method  SCMManagerImpl.updateproject()");
 		}
 		if (SVN.equals(repodetail.getType())) {
 			if(debugEnabled){
 				S_LOGGER.debug("SVN type");
 			}
 			DAVRepositoryFactory.setup();
 			SVNURL svnURL = SVNURL.parseURIEncoded(repodetail.getRepoUrl());
 			DefaultSVNOptions options = new DefaultSVNOptions();
 			cm = SVNClientManager.newInstance(options, repodetail.getUserName(), repodetail.getPassword());
 			if(debugEnabled){
 				S_LOGGER.debug("update SCM Connection " + repodetail.getRepoUrl());
 			}
 			 updateSCMConnection(appInfo, repodetail.getRepoUrl());
 				// revision = HEAD_REVISION.equals(revision) ? revision
 				// : revisionVal;
 			File updateDir = new File(Utility.getProjectHome(), appInfo.getAppDirName());
 			if(debugEnabled){
 				S_LOGGER.debug("updateDir SVN... " + updateDir);
 				S_LOGGER.debug("Updating...");
 			}
 			SVNUpdateClient uc = cm.getUpdateClient();
 			uc.doUpdate(updateDir, SVNRevision.parse(repodetail.getRevision()), SVNDepth.UNKNOWN, true, true);
 			if(debugEnabled){
 				S_LOGGER.debug("Updated!");
 			}
 			return true;
 		} else if (GIT.equals(repodetail.getType())) {
 			if(debugEnabled){
 				S_LOGGER.debug("GIT type");
 			}
 			updateSCMConnection(appInfo, repodetail.getRepoUrl());
 			File updateDir = new File(Utility.getProjectHome(), appInfo.getAppDirName()); 
 			if(debugEnabled){
 				S_LOGGER.debug("updateDir GIT... " + updateDir);
 			}
 			Git git = Git.open(updateDir); // checkout is the folder with .git
 			git.pull().call(); // succeeds
 			git.getRepository().close();
 			if(debugEnabled){
 				S_LOGGER.debug("Updated!");
 			}
 			return true;
 		} else if (BITKEEPER.equals(repodetail.getType())) {
 		    if (debugEnabled) {
                 S_LOGGER.debug("BITKEEPER type");
             }
 		    StringBuilder sb = new StringBuilder(Utility.getProjectHome())
 		    .append(appInfo.getAppDirName());
 		    updateFromBitKeeperRepo(repodetail.getRepoUrl(), sb.toString());
 		} else if (PERFORCE.equals(repodetail.getType())) {
 		    if (debugEnabled) {
                 S_LOGGER.debug("PERFORCE type");
             }
 			File updateDir = new File(Utility.getProjectHome(), appInfo.getAppDirName()); 
 			String baseDir = updateDir.getAbsolutePath();
 		    perforceSync(repodetail, baseDir, appInfo.getAppDirName(),"update");
 			updateSCMConnection(appInfo, repodetail.getRepoUrl()+repodetail.getStream());
 		}
 
 		return false;
 	}
 	
 	private boolean updateFromBitKeeperRepo(String repoUrl, String appDir) throws PhrescoException {
         BufferedReader reader = null;
         File file = new File(Utility.getPhrescoTemp() + "bitkeeper.info");
         boolean isUpdated = false;
         try {
             List<String> commands = new ArrayList<String>();
             commands.add(BK_PARENT + SPACE + repoUrl);
             commands.add(BK_PULL);
             for (String command : commands) {
                 Utility.executeStreamconsumer(appDir, command, new FileOutputStream(file));
             }
             reader = new BufferedReader(new FileReader(file));
             String strLine;
             while ((strLine = reader.readLine()) != null) {
                 if (strLine.contains("Nothing to pull")) {
                     throw new PhrescoException("Nothing to pull");
                 } else if (strLine.contains("[pull] 100%")) {
                     isUpdated = true;
                 }
             }
         } catch (Exception e) {
             throw new PhrescoException(e);
         } finally {
             if (reader != null) {
                 try {
                     reader.close();
                 } catch (IOException e) {
                     throw new PhrescoException(e);
                 }
             }
             if (file.exists()) {
                 file.delete();
             }
         }
         
         return isUpdated;
     }
 
 	private void importToWorkspace(File gitImportTemp, String projectHome,String code) throws Exception {
 		try {
 			if(debugEnabled){
 				S_LOGGER.debug("Entering Method  SCMManagerImpl.importToWorkspace()");
 			}
 			File workspaceProjectDir = new File(projectHome + code);
 			if(debugEnabled){
 				S_LOGGER.debug("workspaceProjectDir " + workspaceProjectDir);
 			}
 			if (workspaceProjectDir.exists()) {
 				if(debugEnabled){
 					S_LOGGER.debug("workspaceProjectDir exists "+ workspaceProjectDir);
 				}
 				throw new PhrescoException(PROJECT_ALREADY);
 			}
 			if(debugEnabled){
 				S_LOGGER.debug("Copyin from Temp to workspace...");
 				S_LOGGER.debug("gitImportTemp " + gitImportTemp);
 				S_LOGGER.debug("workspaceProjectDir " + workspaceProjectDir);
 			}
 			FileUtils.copyDirectory(gitImportTemp, workspaceProjectDir);
 			if(debugEnabled){
 				S_LOGGER.debug("Deleting pack file");
 			}
 			FileUtils.deleteDirectory(gitImportTemp);
 		} catch (IOException e) {
 			if(debugEnabled){
 				S_LOGGER.error("Entering into catch block of importToWorkspace() "+ e.getLocalizedMessage());
 				S_LOGGER.error("pack file is not deleted ");
 			}
 		}
 	}
 
 	private void updateSCMConnection(ApplicationInfo appInfo, String repoUrl)throws Exception {
 		if(debugEnabled){
 			S_LOGGER.debug("Entering Method SCMManagerImpl.updateSCMConnection()");
 		}
 		try {
 			PomProcessor processor = getPomProcessor(appInfo);
 				if(debugEnabled){
 					S_LOGGER.debug("processor.getSCM() exists and repo url "+ repoUrl);
 				}
 				processor.setSCM(repoUrl, "", "", "");
 				processor.save();
 		} catch (Exception e) {
 			if(debugEnabled){
 				S_LOGGER.error("Entering catch block of updateSCMConnection()"+ e.getLocalizedMessage());
 			}
 			throw new PhrescoException(POM_URL_FAIL);
 		}
 	}
 
 	private PomProcessor getPomProcessor(ApplicationInfo appInfo)throws Exception {
 		if(debugEnabled){
 			S_LOGGER.debug("Entering Method  SCMManagerImpl.getPomProcessor()");
 		}
 		try {
 			StringBuilder builder = new StringBuilder(Utility.getProjectHome());
 			builder.append(appInfo.getAppDirName());
 			builder.append(File.separatorChar);
 			builder.append(Utility.getPomFileName(appInfo));
 			if(debugEnabled){
 				S_LOGGER.debug("builder.toString() " + builder.toString());
 			}
 			File pomPath = new File(builder.toString());
 			if(debugEnabled){
 				S_LOGGER.debug("file exists " + pomPath.exists());
 			}
 			return new PomProcessor(pomPath);
 		} catch (Exception e) {
 			if(debugEnabled){
 				S_LOGGER.error("Entring into catch block of getPomProcessor() "+ e.getLocalizedMessage());
 			}
 			throw new PhrescoException(NO_POM_XML);
 		}
 	}
 
 	private static void setupLibrary() {
 		if(debugEnabled){
 			S_LOGGER.debug("Entering Method  SCMManagerImpl.setupLibrary()");
 		}
 		DAVRepositoryFactory.setup();
 		SVNRepositoryFactoryImpl.setup();
 		FSRepositoryFactory.setup();
 	}
 
 	private boolean checkOutFilter(RepoDetail repodetail,SVNURL svnURL, String displayName, String uniqueKey) throws Exception {
 		if(debugEnabled){
 			S_LOGGER.debug("Entering Method  SCMManagerImpl.checkOutFilter()");
 		}
 		setupLibrary();
 		SVNRepository repository = null;
 		repository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(repodetail.getRepoUrl()));
 		ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(repodetail.getUserName(), repodetail.getPassword());
 		repository.setAuthenticationManager(authManager);
 			SVNNodeKind nodeKind = repository.checkPath("", -1);
 			if (nodeKind == SVNNodeKind.NONE) {
 				if(debugEnabled){
 					S_LOGGER.error("There is no entry at '" + repodetail.getRepoUrl() + "'.");
 				}
 			} else if (nodeKind == SVNNodeKind.FILE) {
 				if(debugEnabled){
 					S_LOGGER.error("The entry at '" + repodetail.getRepoUrl() + " is a file while a directory was expected.");
 				}
 			}
 			if(debugEnabled){
 				S_LOGGER.debug("Repository Root: " + repository.getRepositoryRoot(true));
 				S_LOGGER.debug("Repository UUID: " + repository.getRepositoryUUID(true));
 			}
 			boolean valid = validateDir(repository, "", repodetail.getRevision(), svnURL, true, displayName, uniqueKey);
 			return valid;
 	}
 
 	private boolean validateDir(SVNRepository repository, String path,
 			String revision, SVNURL svnURL, boolean recursive, String displayName, String uniqueKey)throws Exception {
 		if(debugEnabled){
 			S_LOGGER.debug("Entering Method  SCMManagerImpl.validateDir()");
 		}
 		// first level check
 			Collection entries = repository.getDir(path, -1, null, (Collection) null);
 			Iterator iterator = entries.iterator();
 			if(debugEnabled){
 				S_LOGGER.debug("Entry size " + entries.size());
 			}
 			if (entries.size() != 0) {
 				while (iterator.hasNext()) {
 					SVNDirEntry entry = (SVNDirEntry) iterator.next();
 					if ((entry.getName().equals(FOLDER_DOT_PHRESCO))
 							&& (entry.getKind() == SVNNodeKind.DIR)) {
 						if(debugEnabled){
 							S_LOGGER.debug("Entry name " + entry.getName());
 							S_LOGGER.debug("Entry Path " + entry.getURL());
 						}
 						ProjectInfo projectInfo = getSvnAppInfo(revision, svnURL);
 						
 						if(debugEnabled){
 							S_LOGGER.debug("AppInfo " + projectInfo);
 						}
 						SVNUpdateClient uc = cm.getUpdateClient();
 						if (projectInfo == null) {
 							if(debugEnabled){
 								S_LOGGER.debug("ProjectInfo is Empty");
 							}
 							throw new PhrescoException(INVALID_FOLDER);
 						}
 						List<ApplicationInfo> appInfos = projectInfo.getAppInfos();
 						if (appInfos == null) {
 							if(debugEnabled){
 								S_LOGGER.debug("AppInfo is Empty");
 							}
 							throw new PhrescoException(INVALID_FOLDER);
 						}
 						ApplicationInfo appInfo = appInfos.get(0);
 						File file = new File(Utility.getProjectHome(), appInfo.getAppDirName());
 						if (file.exists()) {
 							throw new PhrescoException(PROJECT_ALREADY);
 			            } else {
 			            	//generate import lock
 			            	String appId = appInfo.getId();
 			            	LockUtil.generateLock(Collections.singletonList(LockUtil.getLockDetail(appId, FrameworkConstants.IMPORT, displayName, uniqueKey)), true);
 			            }
 						if(debugEnabled){
 							S_LOGGER.debug("Checking out...");
 						}
 						uc.doCheckout(svnURL, file, SVNRevision.UNDEFINED,
 								SVNRevision.parse(revision), SVNDepth.UNKNOWN,
 								false);
 						if(debugEnabled){
 							S_LOGGER.debug("updating pom.xml");
 						}
 						// update connection url in pom.xml
 						updateSCMConnection(appInfo,
 								svnURL.toDecodedString());
 						dotphresco = true;
 						return dotphresco;
 					} else if (entry.getKind() == SVNNodeKind.DIR && recursive) {
 						// second level check (only one iteration)
 						SVNURL svnnewURL = svnURL.appendPath(FORWARD_SLASH + entry.getName(), true);
 						if(debugEnabled){
 							S_LOGGER.debug("Appended SVNURL for subdir " + svnURL);
 							S_LOGGER.debug("checking subdirectories");
 						}
 						validateDir(repository,(path.equals("")) ? entry.getName() : path
 										+ FORWARD_SLASH + entry.getName(), revision,svnnewURL, false, displayName, uniqueKey);
 					}
 				}
 			}
 		return dotphresco;
 	}
 
 	private void importFromGit(RepoDetail repodetail, File gitImportTemp)throws Exception {
 		if(debugEnabled){
 			S_LOGGER.debug("Entering Method  SCMManagerImpl.importFromGit()");
 		}		
 			if (StringUtils.isEmpty(repodetail.getBranch())) {
 				repodetail.setBranch(MASTER);
 			}
 			final String passwordPhrase = repodetail.getPassPhrase();
 			JschConfigSessionFactory sessionFactory = new JschConfigSessionFactory() {
 	        	@Override
 	        	protected void configure(OpenSshConfig.Host hc, Session session) {
 	        	    CredentialsProvider provider = new CredentialsProvider() {
 	        	        @Override
 	        	        public boolean isInteractive() {
 	        	            return false;
 	        	        }
 
 	        	        @Override
 	        	        public boolean supports(CredentialItem... items) {
 	        	            return true;
 	        	        }
 	        	        
 	        	        @Override
 	        	        public boolean get(URIish uri, CredentialItem... items) throws UnsupportedCredentialItem {
 	        	            for (CredentialItem item : items) {
 	        	                ((CredentialItem.StringType) item).setValue(passwordPhrase);
 	        	            }
 	        	            return true;
 	        	        }
 	        	    };
 	        	    UserInfo userInfo = new CredentialsProviderUserInfo(session, provider);
 	        	    session.setUserInfo(userInfo);
 	        	}
         	};
         	
         	SshSessionFactory.setInstance(sessionFactory);
 			
 			UsernamePasswordCredentialsProvider userCredential = new UsernamePasswordCredentialsProvider(repodetail.getUserName(), repodetail.getPassword());
 			Git r = Git.cloneRepository().setDirectory(gitImportTemp)
 			.setCredentialsProvider(userCredential)
 			.setURI(repodetail.getRepoUrl())
 //			.setProgressMonitor(new TextProgressMonitor())
 			.setBranch(repodetail.getBranch())
 			.call();
 	        r.getRepository().close();      
 	}
 	
 	private ApplicationInfo importFromBitKeeper(String repoUrl, String displayName, String uniqueKey) throws PhrescoException {
 	    BufferedReader reader = null;
 	    File file = new File(Utility.getPhrescoTemp() + "bitkeeper.info");
 	    boolean isImported = false;
 	    try {
 	        String command = BK_CLONE + SPACE + repoUrl;
 	        String uuid = UUID.randomUUID().toString();
 			File bkImportTemp = new File(Utility.getPhrescoTemp(), uuid);
 			if(debugEnabled){
 				S_LOGGER.debug("bkImportTemp " + bkImportTemp);
 			}
 			if (bkImportTemp.exists()) {
 				if(debugEnabled){
 					S_LOGGER.debug("Empty Bk directory need to be removed before importing from BitKeeper ");
 				}
 				FileUtils.deleteDirectory(bkImportTemp);
 			}
 	        Utility.executeStreamconsumer(bkImportTemp.getPath(), command, new FileOutputStream(file));
 	        reader = new BufferedReader(new FileReader(file));
 	        String strLine;
 	        while ((strLine = reader.readLine()) != null) {
 	            if (strLine.contains("OK")) {
 	                isImported = true;
 	                break;
 	            } else if (strLine.contains(ALREADY_EXISTS)) {
 	                throw new PhrescoException("Project already imported");
 	            } else if (strLine.contains("FAILED")) {
 	                throw new PhrescoException("Failed to import project");
 	            }
 	        }
 	        if (isImported) {
 	        	ProjectInfo projectInfo = getGitAppInfo(bkImportTemp);
 	        	if (projectInfo != null) {
 	        		ApplicationInfo appInfo = returnAppInfo(projectInfo);
 	        		if (appInfo != null) {
 	        			//generate import lock
 	                	String appId = appInfo.getId();
 	                	LockUtil.generateLock(Collections.singletonList(LockUtil.getLockDetail(appId, FrameworkConstants.IMPORT, displayName, uniqueKey)), true);
 	        			importToWorkspace(bkImportTemp, Utility.getProjectHome(), appInfo.getAppDirName());
 	        			return appInfo;
 	        		}
 	        	} 
 	        } 
 	        return null;
 	    } catch (IOException e) {
 	        throw new PhrescoException(e);
 	    } catch (Exception e) {
 			throw new PhrescoException(e);
 		} finally {
 	        if (reader != null) {
 	            try {
 	                reader.close();
 	            } catch (IOException e) {
 	                throw new PhrescoException(e);
 	            }
 	        }
 	        if (file.exists()) {
 	            file.delete();
 	        }
 	    }
 	}
 
 	private ApplicationInfo cloneFilter(File appDir, String url, boolean recursive, String displayName, String uniqueKey)throws Exception {
 		if(debugEnabled){
 			S_LOGGER.debug("Entering Method  SCMManagerImpl.cloneFilter()");
 		}
 		if (appDir.isDirectory()) {
 			ProjectInfo projectInfo = getGitAppInfo(appDir);
 			if(debugEnabled){
 				S_LOGGER.debug("appInfo " + projectInfo);
 			}
 			if (projectInfo == null) {
 				if(debugEnabled){
 					S_LOGGER.debug("ProjectInfo is Empty");
 				}
 				throw new PhrescoException(INVALID_FOLDER);
 			}
 			List<ApplicationInfo> appInfos = projectInfo.getAppInfos();
 			if (appInfos == null) {
 				if(debugEnabled){
 					S_LOGGER.debug("AppInfo is Empty");
 				}
 				throw new PhrescoException(INVALID_FOLDER);
 			}
 			ApplicationInfo appInfo = appInfos.get(0);
 			if (appInfo != null) {
 				//generate import lock
             	String appId = appInfo.getId();
             	LockUtil.generateLock(Collections.singletonList(LockUtil.getLockDetail(appId, FrameworkConstants.IMPORT, displayName, uniqueKey)), true);
 				importToWorkspace(appDir, Utility.getProjectHome(),	appInfo.getAppDirName());
 				if(debugEnabled){
 					S_LOGGER.debug("updating pom.xml");
 				}
 				// update connection in pom.xml
 				updateSCMConnection(appInfo, url);
 				return appInfo;
 			}
 		}
 		return null;
 	}
 
 	private ProjectInfo getGitAppInfo(File directory)throws PhrescoException {
 		if(debugEnabled){
 			S_LOGGER.debug("Entering Method  SCMManagerImpl.getGitAppInfo()");
 		}
 		BufferedReader reader = null;
 		try {
 			File dotProjectFile = new File(directory, FOLDER_DOT_PHRESCO+ File.separator + PROJECT_INFO);
 			if(debugEnabled){
 				S_LOGGER.debug(dotProjectFile.getAbsolutePath());
 				S_LOGGER.debug("dotProjectFile" + dotProjectFile);
 			}
 			if (!dotProjectFile.exists()) {
 				return null;
 			}
 			reader = new BufferedReader(new FileReader(dotProjectFile));
 			return new Gson().fromJson(reader, ProjectInfo.class);
 		} catch (FileNotFoundException e) {
 			if(debugEnabled){
 				S_LOGGER.error("Entering into catch block of getGitAppInfo() "+ e.getLocalizedMessage());
 			}
 			throw new PhrescoException(INVALID_FOLDER);
 		} finally {
 			Utility.closeStream(reader);
 		}
 	}
 
 	private ProjectInfo getSvnAppInfo(String revision, SVNURL svnURL) throws Exception {
 		if(debugEnabled){
 			S_LOGGER.debug("Entering Method  SCMManagerImpl.getSvnAppInfo()");
 		}
 		BufferedReader reader = null;
 		File tempDir = new File(Utility.getSystemTemp(), SVN_CHECKOUT_TEMP);
 		if(debugEnabled){
 			S_LOGGER.debug("temp dir : SVNAccessor " + tempDir);
 		}
 		try {
 			SVNUpdateClient uc = cm.getUpdateClient();
 			try {
 			uc.doCheckout(svnURL.appendPath(PHRESCO, true), tempDir,
 					SVNRevision.UNDEFINED, SVNRevision.parse(revision),
 					SVNDepth.UNKNOWN, false);
 			} catch(SVNException er) {
 				ProjectInfo projInfo = new ProjectInfo();
 				projInfo.setId("#SEP#");
 				return projInfo;
 			}
 			File dotProjectFile = new File(tempDir, PROJECT_INFO);
 			if (!dotProjectFile.exists()) {
 				throw new PhrescoException(INVALID_FOLDER);
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
 
 	public boolean importToRepo(RepoDetail repodetail, ApplicationInfo appInfo) throws Exception {
 		if(debugEnabled){
 			S_LOGGER.debug("Entering Method  SCMManagerImpl.importToRepo()");
 		}
 		File dir = new File(Utility.getProjectHome() + appInfo.getAppDirName());
 		try {
 			if (SVN.equals(repodetail.getType())) {
 				String tail = addAppFolderToSVN(repodetail, dir);
 				String appendedUrl = repodetail.getRepoUrl() + FORWARD_SLASH + tail;
 				repodetail.setRepoUrl(appendedUrl);
 				importDirectoryContentToSubversion(repodetail, dir.getPath());
 				// checkout to get .svn folder
 				checkoutImportedApp(repodetail, appInfo);
 			} else if (GIT.equals(repodetail.getType())) {
 				importToGITRepo(repodetail,appInfo, dir);
 			}
 		} catch (Exception e) {
 			throw e;
 		}
 		return true;
 	}
 	
 	private String addAppFolderToSVN(RepoDetail repodetail, final File dir) throws PhrescoException {
 		if(debugEnabled){
 			S_LOGGER.debug("Entering Method  SCMManagerImpl.addAppFolderToSVN()");
 		}
 		try {
 			//get DirName
 			ProjectInfo projectInfo;
 			projectInfo = getGitAppInfo(dir);
 			List<ApplicationInfo> appInfos = projectInfo.getAppInfos();
 			String appDirName = "";
 			if (CollectionUtils.isNotEmpty(appInfos)) {
 				ApplicationInfo appInfo = appInfos.get(0);
 				appDirName = appInfo.getAppDirName();
 			}
 			
 			//CreateTempFolder
 			File temp = new File(dir, TEMP_FOLDER);
 			if (temp.exists()) {
 				FileUtils.deleteDirectory(temp);
 			}
 			temp.mkdir();
 			
 			File folderName = new File(temp, appDirName);
 			folderName.mkdir();
 			
 			//Checkin rootFolder
 			importDirectoryContentToSubversion(repodetail, temp.getPath());
 			
 			//deleteing temp
 			if (temp.exists()) {
 				FileUtils.deleteDirectory(temp);
 			}
 			
 			return appDirName;
 		} catch (Exception e) {
 			throw new PhrescoException(e);
 		} 
 	}
 	
 	private SVNCommitInfo importDirectoryContentToSubversion(RepoDetail repodetail, final String subVersionedDirectory) throws SVNException {
 		if(debugEnabled){
 			S_LOGGER.debug("Entering Method  SCMManagerImpl.importDirectoryContentToSubversion()");
 		}
 		setupLibrary();
 		DefaultSVNOptions defaultSVNOptions = new DefaultSVNOptions();
         defaultSVNOptions.setIgnorePatterns(new String[] {DO_NOT_CHECKIN_DIR,RUN});
         final SVNClientManager cm = SVNClientManager.newInstance(defaultSVNOptions, repodetail.getUserName(), repodetail.getPassword());
         return cm.getCommitClient().doImport(new File(subVersionedDirectory), SVNURL.parseURIEncoded(repodetail.getRepoUrl()), repodetail.getCommitMessage(), null, true, true, SVNDepth.fromRecurse(true));
     }
 	
 	private void checkoutImportedApp(RepoDetail repodetail, ApplicationInfo appInfo) throws Exception {
 		if(debugEnabled){
 			S_LOGGER.debug("Entering Method  SCMManagerImpl.checkoutImportedApp()");
 		}
 		DefaultSVNOptions options = new DefaultSVNOptions();
 		SVNClientManager cm = SVNClientManager.newInstance(options, repodetail.getUserName(), repodetail.getPassword());
 		SVNUpdateClient uc = cm.getUpdateClient();
 		SVNURL svnURL = SVNURL.parseURIEncoded(repodetail.getRepoUrl());
 		if(debugEnabled){
 			S_LOGGER.debug("Checking out...");
 		}
 		String subVersionedDirectory = Utility.getProjectHome() + appInfo.getAppDirName();
 		File subVersDir = new File(subVersionedDirectory);
 		uc.doCheckout(SVNURL.parseURIEncoded(repodetail.getRepoUrl()), subVersDir, SVNRevision.UNDEFINED, SVNRevision.parse(HEAD_REVISION), SVNDepth.INFINITY, true);
 		if(debugEnabled){
 			S_LOGGER.debug("updating pom.xml");
 		}
 		// update connection url in pom.xml
 		updateSCMConnection(appInfo, svnURL.toDecodedString());
 	}
 
 	private void importToGITRepo(RepoDetail repodetail,ApplicationInfo appInfo, File appDir) throws Exception {
 		if(debugEnabled){
 			S_LOGGER.debug("Entering Method  SCMManagerImpl.importToGITRepo()");
 		}
 		boolean gitExists = false;
 		if(new File(appDir.getPath() + FORWARD_SLASH + DOT + GIT).exists()) {
 			gitExists = true;
 		}
 		try {
 			CredentialsProvider cp = new UsernamePasswordCredentialsProvider(repodetail.getUserName(), repodetail.getPassword());
 			FileRepositoryBuilder builder = new FileRepositoryBuilder();
 			Repository repository = builder.setGitDir(appDir).readEnvironment().findGitDir().build();
 			String dirPath = appDir.getPath();
 			File gitignore = new File(dirPath + GITIGNORE_FILE);
 			gitignore.createNewFile();
 			
 			if (gitignore.exists()) {
 				String contents = FileUtils.readFileToString(gitignore);
 				if (!contents.isEmpty() && !contents.contains(DO_NOT_CHECKIN_DIR)) {
 					String source = NEWLINE + DO_NOT_CHECKIN_DIR + NEWLINE;
 					OutputStream out = new FileOutputStream((dirPath + GITIGNORE_FILE), true);
 					byte buf[] = source.getBytes();
 					out.write(buf);
 					out.close();
 				} else if (contents.isEmpty()){
 				String source = NEWLINE + DO_NOT_CHECKIN_DIR + NEWLINE;
 				OutputStream out = new FileOutputStream((dirPath + GITIGNORE_FILE), true);
 				byte buf[] = source.getBytes();
 				out.write(buf);
 				out.close();
 				}
 			}
 		
 			Git git = new Git(repository);
 		
 			InitCommand initCommand = Git.init();
 			initCommand.setDirectory(appDir);
 			git = initCommand.call();
 		
 			AddCommand add = git.add();
 			add.addFilepattern(".");
 			add.call();
 
 			CommitCommand commit = git.commit().setAll(true);
 			commit.setMessage(repodetail.getCommitMessage()).call();
 			StoredConfig config = git.getRepository().getConfig();
 
 			config.setString(REMOTE, ORIGIN, URL, repodetail.getRepoUrl());
 			config.setString(REMOTE, ORIGIN, FETCH, REFS_HEADS_REMOTE_ORIGIN);
 			config.setString(BRANCH, MASTER, REMOTE, ORIGIN);
 			config.setString(BRANCH, MASTER, MERGE, REF_HEAD_MASTER);
 			config.save();
 
 			try {
 				PushCommand pc = git.push();
 				pc.setCredentialsProvider(cp).setForce(true);
 				pc.setPushAll().call();
 			} catch (Exception e){
 				git.getRepository().close();
 				throw e;
 			}
 		
 			if (appInfo != null) {
 				updateSCMConnection(appInfo, repodetail.getRepoUrl());
 			}
 			git.getRepository().close();
 		} catch (Exception e) {
 			Exception s = e;
 			resetLocalCommit(appDir, gitExists, e);
 			throw s;
 		}
 	}
 
 	private void resetLocalCommit(File appDir, boolean gitExists, Exception e) throws PhrescoException {
 		try {
 			if(gitExists == true && e.getLocalizedMessage().contains("not authorized")) {
 				FileRepositoryBuilder builder = new FileRepositoryBuilder();
 				Repository repository = builder.setGitDir(appDir).readEnvironment().findGitDir().build();
 				Git git = new Git(repository);
 
 				InitCommand initCommand = Git.init();
 				initCommand.setDirectory(appDir);
 				git = initCommand.call();
 			
 				ResetCommand reset = git.reset();
 				ResetType mode = ResetType.SOFT;
 				reset.setRef("HEAD~1").setMode(mode);
 				reset.call();
 						
 				git.getRepository().close();
 			}
 		} catch (Exception pe) {
 			new PhrescoException(pe);
 		}
 	}
 
 	public boolean commitToRepo(RepoDetail repodetail, File dir) throws Exception {
 		if(debugEnabled) {
 			S_LOGGER.debug("Entering Method  SCMManagerImpl.commitToRepo()");
 		}
 		try {
 			if (SVN.equals(repodetail.getType())) {
 				commitDirectoryContentToSubversion(repodetail, dir.getPath());
 			} else if (GIT.equals(repodetail.getType())) {
 				importToGITRepo(repodetail, null, dir);
 			} else if (BITKEEPER.equals(repodetail.getType())) {
 			    return commitToBitKeeperRepo(repodetail.getRepoUrl(), dir.getPath(), repodetail.getCommitMessage());
 			}
 		} catch (PhrescoException e) {
 			throw e;
 		}
 		return true;
 	}
 	
 	private SVNCommitInfo commitDirectoryContentToSubversion(RepoDetail repodetail, String subVersionedDirectory) throws SVNException {
 		if(debugEnabled){
 			S_LOGGER.debug("Entering Method  SCMManagerImpl.commitDirectoryContentToSubversion()");
 		}
 		setupLibrary();
 		
 		final SVNClientManager cm = SVNClientManager.newInstance(new DefaultSVNOptions(), repodetail.getUserName(), repodetail.getPassword());
 		SVNWCClient wcClient = cm.getWCClient();
 		File subVerDir = new File(subVersionedDirectory);
 		// This one recursively adds an existing local item under version control (schedules for addition)
 		wcClient.doAdd(subVerDir, true, false, false, SVNDepth.INFINITY, false, false);
 		return cm.getCommitClient().doCommit(new File[]{subVerDir}, false, repodetail.getCommitMessage(), null, null, false, true, SVNDepth.INFINITY);
     }
 	
 	private boolean commitToBitKeeperRepo(String repoUrl, String appDir, String commitMsg) throws PhrescoException {
 	    BufferedReader reader = null;
         File file = new File(Utility.getPhrescoTemp() + "bitkeeper.info");
         boolean isCommitted = false;
 	    try {
 	        List<String> commands = new ArrayList<String>();
 	        commands.add(BK_PARENT + SPACE + repoUrl);
 	        commands.add(BK_PULL);
 	        commands.add(BK_CI + SPACE + BK_ADD_COMMENT + commitMsg + KEY_QUOTES);
 	        commands.add(BK_ADD_FILES + SPACE + BK_ADD_COMMENT + commitMsg + KEY_QUOTES);
 	        commands.add(BK_COMMIT + SPACE + BK_ADD_COMMENT + commitMsg + KEY_QUOTES);
 	        commands.add(BK_PUSH);
 	        for (String command : commands) {
 	            Utility.executeStreamconsumer(appDir, command, new FileOutputStream(file));
             }
 	        reader = new BufferedReader(new FileReader(file));
             String strLine;
             while ((strLine = reader.readLine()) != null) {
                 if (strLine.contains("push") && strLine.contains("OK")) {
                     isCommitted = true;
                 } else if (strLine.contains("Nothing to push")) {
                     throw new PhrescoException("Nothing to push");
                 } else if (strLine.contains("Cannot resolve host")) {
                     throw new PhrescoException("Failed to commit");
                 }
             }
         } catch (Exception e) {
             throw new PhrescoException(e);
         } finally {
             if (reader != null) {
                 try {
                     reader.close();
                 } catch (IOException e) {
                     throw new PhrescoException(e);
                 }
             }
             if (file.exists()) {
                 file.delete();
             }
         }
         
         return isCommitted;
 	}
 	
 	public SVNCommitInfo deleteDirectoryInSubversion(RepoDetail repodetail, String subVersionedDirectory) throws SVNException, IOException {
 		if(debugEnabled){
 			S_LOGGER.debug("Entering Method  SCMManagerImpl.commitDirectoryContentToSubversion()");
 		}
 		setupLibrary();
 		
 		File subVerDir = new File(subVersionedDirectory);		
 		final SVNClientManager cm = SVNClientManager.newInstance(new DefaultSVNOptions(), repodetail.getUserName(), repodetail.getPassword());
 		
 		SVNWCClient wcClient = cm.getWCClient();
 		//wcClient.doDelete(subVerDir, true, false);
 		FileUtils.listFiles(subVerDir, TrueFileFilter.TRUE, FileFilterUtils.makeSVNAware(null));
 		for (File child : subVerDir.listFiles()) {
 			if (!(DOT+SVN).equals(child.getName())) {
 				wcClient.doDelete(child, true, true, false);
 			}
 		}
 		
 		return cm.getCommitClient().doCommit(new File[]{subVerDir}, false, repodetail.getCommitMessage(), null, null, false, true, SVNDepth.INFINITY);
     }
 	
 	public List<RepoFileInfo> getCommitableFiles(File path, String revision) throws SVNException {
 		
 	    SVNClientManager svnClientManager = SVNClientManager.newInstance();
 	    final List<RepoFileInfo> filesList = new ArrayList<RepoFileInfo>();
 	    svnClientManager.getStatusClient().doStatus(path, SVNRevision.parse(revision), SVNDepth.INFINITY, false, false, false, false, new ISVNStatusHandler() {
 	        public void handleStatus(SVNStatus status) throws SVNException {
 	            SVNStatusType statusType = status.getContentsStatus();
 	            if (statusType != SVNStatusType.STATUS_NONE && statusType != SVNStatusType.STATUS_NORMAL
 	                    && statusType != SVNStatusType.STATUS_IGNORED) {
 	            	RepoFileInfo repoFileInfo = new RepoFileInfo();
 	            	String filePath = status.getFile().getPath();
 	            	 String FileStatus = Character.toString(statusType.getCode());
 	            	 repoFileInfo.setContentsStatus(statusType);
 	            	 repoFileInfo.setStatus(FileStatus);
 	            	 repoFileInfo.setCommitFilePath(filePath);
 	            	 filesList.add(repoFileInfo);
 	            }
 	        }
 	    }, null);
 	    
 	    return filesList;
 	}
 	
 	public List<RepoFileInfo> getGITCommitableFiles(File path) throws IOException, GitAPIException
 	{
 		FileRepositoryBuilder builder = new FileRepositoryBuilder();
 		Repository repository = builder.setGitDir(path).readEnvironment().findGitDir().build(); 
 		Git git = new Git(repository);
 		List<RepoFileInfo> fileslist = new ArrayList<RepoFileInfo>();
 		InitCommand initCommand = Git.init();
 		initCommand.setDirectory(path);
 		git = initCommand.call();
 		Status status = git.status().call();
 		
 		Set<String> added = status.getAdded();
 		Set<String> changed = status.getChanged();
 		Set<String> conflicting = status.getConflicting();
 		Set<String> missing= status.getMissing();
 		Set<String> modified = status.getModified();
 		Set<String> removed = status.getRemoved();
 		Set<String> untracked = status.getUntracked();
 		
 		if (!added.isEmpty()) {
 			for (String add : added) {
 				RepoFileInfo repoFileInfo = new RepoFileInfo();
 				String filePath = path + BACK_SLASH + add;
 				repoFileInfo.setCommitFilePath(filePath);
 				repoFileInfo.setStatus("A");
 				fileslist.add(repoFileInfo);
 			}
 		}
 
 		if (!changed.isEmpty()) {
 			for (String change : changed) {
 				RepoFileInfo repoFileInfo = new RepoFileInfo();
 				String filePath = path + BACK_SLASH + change;
 				repoFileInfo.setCommitFilePath(filePath);
 				repoFileInfo.setStatus("M");
 				fileslist.add(repoFileInfo);
 			}
 		}
 
 		if (!conflicting.isEmpty()) {
 			for (String conflict : conflicting) {
 				RepoFileInfo repoFileInfo = new RepoFileInfo();
 				String filePath = path + BACK_SLASH + conflict;
 				repoFileInfo.setCommitFilePath(filePath);
 				repoFileInfo.setStatus("C");
 				fileslist.add(repoFileInfo);
 			}
 		}
 
 		if (!missing.isEmpty()) {
 			for (String miss : missing) {
 				RepoFileInfo repoFileInfo = new RepoFileInfo();
 				String filePath = path + BACK_SLASH + miss;
 				repoFileInfo.setCommitFilePath(filePath);
 				repoFileInfo.setStatus("!");
 				fileslist.add(repoFileInfo);
 			}
 		}
 
 		if (!modified.isEmpty()) {
 			for (String modify : modified) {
 				RepoFileInfo repoFileInfo = new RepoFileInfo();
 				String filePath = path + BACK_SLASH + modify;
 				repoFileInfo.setCommitFilePath(filePath);
 				repoFileInfo.setStatus("M");
 				fileslist.add(repoFileInfo);
 			}
 		}
 
 		if (!removed.isEmpty()) {
 			for (String remove : removed) {
 				RepoFileInfo repoFileInfo = new RepoFileInfo();
 				String filePath = path + BACK_SLASH + remove;
 				repoFileInfo.setCommitFilePath(filePath);
 				repoFileInfo.setStatus("D");
 				fileslist.add(repoFileInfo);
 			}
 		}
 
 		if (!untracked.isEmpty()) {
 			for (String untrack : untracked) {
 				RepoFileInfo repoFileInfo = new RepoFileInfo();
 				String filePath = path + BACK_SLASH + untrack;
 				repoFileInfo.setCommitFilePath(filePath);
 				repoFileInfo.setStatus("?");
 				fileslist.add(repoFileInfo);
 			}
 		}
 		git.getRepository().close();
 		return fileslist;
 	}
 
 	public List<String> getSvnLogMessages(String Url, String userName, String Password) throws PhrescoException {
 		setupLibrary();
 		long startRevision = 0;
 		long endRevision = -1; //HEAD (the latest) revision
 		SVNRepository repository = null;
 		List<String> logMessages = new ArrayList<String>();
 		try {
 			repository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(Url));
 			ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(userName, Password);
 			repository.setAuthenticationManager(authManager);
 			Collection logEntries = null;
 
 			logEntries = repository.log( new String[] { "" } , null , startRevision , endRevision , true , true );
 			for (Iterator entries = logEntries.iterator(); entries.hasNext();) {
 				SVNLogEntry logEntry = (SVNLogEntry) entries.next( );
 				logMessages.add(logEntry.getMessage());
 			}
 		} catch (SVNException e) {
 			throw new PhrescoException(e);
 		}
 
 		return logMessages;
 	}
 	
 	public SVNCommitInfo commitSpecifiedFiles(List<File> listModifiedFiles, String username, String password, String commitMessage) throws Exception {
 		setupLibrary();
 		
 		final SVNClientManager cm = SVNClientManager.newInstance(new DefaultSVNOptions(), username, password);
 		SVNWCClient wcClient = cm.getWCClient();
 		File[] comittableFiles = listModifiedFiles.toArray(new File[listModifiedFiles.size()]);
 
		SVNCommitClient cc = cm.getCommitClient(); 
 		cc.setCommitParameters(new ISVNCommitParameters() { 
 
 			// delete even those files 
 			// that are not scheduled for deletion. 
 			public Action onMissingFile(File file) { 
 				return DELETE; 
 			} 
 			public Action onMissingDirectory(File file) { 
 				return DELETE; 
 			} 
 
 			// delete files from disk after committing deletion. 
 			public boolean onDirectoryDeletion(File directory) { 
 				return true; 
 			} 
 			public boolean onFileDeletion(File file) { 
 				return true; 
 			} 
 		}); 
 		
 		//to List Unversioned Files 
 		List<File> unversionedFiles = new ArrayList<File>();
 		for (File file : comittableFiles) {
 			List<RepoFileInfo> status = getCommitableFiles(new File(file.getPath()), HEAD_REVISION);
 			if (CollectionUtils.isNotEmpty(status)) {
 				RepoFileInfo svnStatus = status.get(0);
 				SVNStatusType contentsStatus = svnStatus.getContentsStatus();
 				if(UNVERSIONED.equalsIgnoreCase(contentsStatus.toString())) {
 					unversionedFiles.add(file);
 				} 
 			}
 		}
 		
 		//Add only Unversioned Files
 		if (CollectionUtils.isNotEmpty(unversionedFiles)) {
 			File[] newlyAddedFiles = unversionedFiles.toArray(new File[unversionedFiles.size()]);
 			wcClient.doAdd(newlyAddedFiles, true, false, false, SVNDepth.INFINITY, false, false, false);
 		}
 
 		SVNCommitInfo commitInfo = cc.doCommit(comittableFiles, false, commitMessage, null, null, false, true, SVNDepth.INFINITY);
 
 		return commitInfo;
 	}
 	
 	public String svnCheckout(RepoDetail repodetail, String Path) {
 		DAVRepositoryFactory.setup();
 		SVNClientManager clientManager = SVNClientManager.newInstance();
 		ISVNAuthenticationManager authManager = new BasicAuthenticationManager(repodetail.getTestUserName(), repodetail.getTestPassword());
 		clientManager.setAuthenticationManager(authManager);
 		SVNUpdateClient updateClient = clientManager.getUpdateClient();
 		try
 		{
 			File file = new File(Path);
 			SVNURL url = SVNURL.parseURIEncoded(repodetail.getTestRepoUrl());
 			updateClient.doCheckout(url, file, SVNRevision.UNDEFINED, SVNRevision.parse(repodetail.getTestRevision()), true);
 		}
 		catch (SVNException e) {
 			return e.getLocalizedMessage();
 		}
 		return SUCCESSFUL;
 	}
 	public ApplicationInfo importFromPerforce(RepoDetail repodetail) throws Exception {
 		String uuid = UUID.randomUUID().toString();
 		File perforceImportTemp = new File(Utility.getPhrescoTemp(), uuid);
 		String baseDir=perforceImportTemp.getAbsolutePath();
 		perforceSync(repodetail, baseDir, uuid,"import");
 		String path=perforceImportTemp.getAbsolutePath();
 		String[] pathArr=repodetail.getStream().split("/");
 		String projName=pathArr[pathArr.length-1];
 		File actualFile=new  File(path+"/"+projName);
 		ProjectInfo projectInfo=getGitAppInfo(actualFile);
 		if(projectInfo!=null){
 			ApplicationInfo appInfo = returnAppInfo(projectInfo);
 			importToWorkspace(actualFile, Utility.getProjectHome(), appInfo.getAppDirName() );
 			updateSCMConnection(appInfo, repodetail.getRepoUrl()+repodetail.getStream());
 			return returnAppInfo(projectInfo);
 		} else{
 			return null;
 		}
 
 	}
 	
 	public void perforceSync(RepoDetail repodetail,String baseDir , String projectName,String flag) throws ConnectionException, RequestException  {
 		String url=repodetail.getRepoUrl();
 		String userName=repodetail.getUserName();
 		String password=repodetail.getPassword();
 		String stream=repodetail.getStream();
 		try {		
 			IOptionsServer server = ServerFactory.getOptionsServer("p4java://"+url, null, null);
 			server.connect();
 			server.setUserName(userName);
 			if(password!=""){
 				server.login(password);
 			}			
 			IClient client = new Client();
 			client.setName(projectName);
 			if(flag.equals("update")){
 				String[] rootArr=baseDir.split(projectName);
 				String root=rootArr[0].substring(0,rootArr[0].length()-1);
 				client.setRoot(root);
 			} else {
 				client.setRoot(baseDir);
 			}
 			client.setServer(server); 
 			server.setCurrentClient(client);
 			ClientViewMapping tempMappingEntry = new ClientViewMapping();
 			tempMappingEntry.setLeft(stream+"/...");
 			tempMappingEntry.setRight("//"+projectName+"/...");
 			ClientView clientView = new ClientView();
 			clientView.addEntry(tempMappingEntry);
 			try {
 			String[] arr=repodetail.getStream().split("//");
 			String[] arr1=arr[1].split("/");
 			client.setStream("//"+arr1[0]+"/"+arr1[1]);
 			client.setClientView(clientView);
 			client.setOptions(new ClientOptions("noallwrite clobber nocompress unlocked nomodtime normdir"));
 			}catch (ArrayIndexOutOfBoundsException e) {
 				throw new RequestException();
 			}
 			System.out.println(server.createClient(client));
 			if (client != null) {	
 				List<IFileSpec> syncList = client.sync(FileSpecBuilder.makeFileSpecList(stream+"/..."),new SyncOptions());
 				for (IFileSpec fileSpec : syncList) {
 					if (fileSpec != null) {
 						if (fileSpec.getOpStatus() == FileSpecOpStatus.VALID) {
 
 						} else {
 							System.err.println(fileSpec.getStatusMessage());
 						}
 					}
 				}}
 			IOFileFilter filter=new IOFileFilter() {
 				@Override
 				public boolean accept(File arg0, String arg1) {
 					return true;
 				}
 				@Override
 				public boolean accept(File arg0) {
 					return true;
 				}
 			};
 			Iterator<File> iterator= FileUtils.iterateFiles(new File(baseDir), filter, filter);
 			while(iterator.hasNext()){
 				File file = iterator.next();
 				file.setWritable(true);
 			}
 
 		} catch (RequestException rexc) {
 			System.err.println(rexc.getDisplayString());
 			rexc.printStackTrace();
 			throw new RequestException();
 		} catch (P4JavaException jexc) {
 			System.err.println(jexc.getLocalizedMessage());
 			jexc.printStackTrace();
 			throw new ConnectionException();
 		} catch (Exception exc) {
 			System.err.println(exc.getLocalizedMessage());
 			exc.printStackTrace();
 		}
 
 	}
 }
