 /**
  *    Copyright 2012 meltmedia
  *
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  *
  *        http://www.apache.org/licenses/LICENSE-2.0
  *
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  */
 package com.meltmedia.cadmium.core.git;
 
 import java.io.Closeable;
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.IOUtils;
 import org.eclipse.jgit.api.AddCommand;
 import org.eclipse.jgit.api.CheckoutCommand;
 import org.eclipse.jgit.api.CloneCommand;
 import org.eclipse.jgit.api.CreateBranchCommand;
 import org.eclipse.jgit.api.CreateBranchCommand.SetupUpstreamMode;
 import org.eclipse.jgit.api.Git;
 import org.eclipse.jgit.api.ListBranchCommand.ListMode;
 import org.eclipse.jgit.api.PushCommand;
 import org.eclipse.jgit.api.ResetCommand.ResetType;
 import org.eclipse.jgit.api.RmCommand;
 import org.eclipse.jgit.api.errors.RefNotFoundException;
 import org.eclipse.jgit.lib.ObjectId;
 import org.eclipse.jgit.lib.Ref;
 import org.eclipse.jgit.lib.Repository;
 import org.eclipse.jgit.revwalk.RevCommit;
 import org.eclipse.jgit.revwalk.RevTag;
 import org.eclipse.jgit.revwalk.RevWalk;
 import org.eclipse.jgit.storage.file.FileRepository;
 import org.eclipse.jgit.transport.SshSessionFactory;
 import org.eclipse.jgit.transport.TagOpt;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.meltmedia.cadmium.core.FileSystemManager;
 import com.meltmedia.cadmium.core.config.ConfigManager;
 import com.meltmedia.cadmium.core.history.HistoryManager;
 import com.meltmedia.cadmium.core.history.HistoryEntry.EntryType;
 
 public class GitService
   implements Closeable
 {
   private static final Logger log = LoggerFactory.getLogger(GitService.class);
   
   protected Git git;
     
   
   public GitService(Repository repository) {
     git = new Git(repository);
   }
   
   protected GitService(Git gitRepo) {
     this.git = gitRepo;
   }
   
   public static void setupSsh(String sshDir) {
     SshSessionFactory.setInstance(new GithubConfigSessionFactory(sshDir));
   }
   
   public static void setupLocalSsh(String sshDir) {
     setupLocalSsh(sshDir, false);
   }
   
   public static void setupLocalSsh(String sshDir, boolean noPrompt) {
     SshSessionFactory.setInstance(new LocalConfigSessionFactory(sshDir, noPrompt));
   }
   
   public static GitService createGitService(String repositoryDirectory) throws Exception {
     if(repositoryDirectory != null) {
       if(!repositoryDirectory.endsWith(".git")) {
         String gitDir = FileSystemManager.getChildDirectoryIfExists(repositoryDirectory, ".git");
         if(gitDir != null) {
           repositoryDirectory = gitDir;
         } else {
           repositoryDirectory = null;
         }
       }
       if(repositoryDirectory != null 
           && FileSystemManager.isDirector(repositoryDirectory) 
           && FileSystemManager.canRead(repositoryDirectory)
           && FileSystemManager.canWrite(repositoryDirectory)){
         return new GitService(new FileRepository(repositoryDirectory));        
       }
     }
     throw new Exception("Invalid git repo");
   }
   
   public static GitService cloneRepo(String uri, String dir) throws Exception {
     if(dir == null || !FileSystemManager.exists(dir)) {
       log.debug("Cloning \""+uri+"\" to \""+dir+"\"");
       CloneCommand clone = Git.cloneRepository();
       clone.setCloneAllBranches(false);
       clone.setCloneSubmodules(false);
       if(dir != null) {
         clone.setDirectory(new File(dir));
       }
       clone.setURI(uri);
      
      return new GitService(clone.call());
     } 
     return null;
   }
   
   public static String moveContentToBranch(String source, GitService gitRepo, String targetBranch, String comment) throws Exception {
     if(!gitRepo.getBranchName().equals(targetBranch)) {
       File tmpDir = File.createTempFile("git_", targetBranch);
       if(tmpDir.delete()){
         log.info("Cloning repository @ " + gitRepo.getBaseDirectory() + " for branch " + targetBranch);
         GitService forBranch = GitService.cloneRepo(gitRepo.getBaseDirectory(), tmpDir.getAbsolutePath());
         String returnVal = null;
         try { 
           forBranch.switchBranch(targetBranch);
           forBranch.pull();
           
           returnVal = moveContentToGit(source, forBranch, comment);
           log.info("Pushing branch modifications back to repository @ " + gitRepo.getBaseDirectory());
           forBranch.git.push().call();
         } finally {
           if(forBranch != null) {
             forBranch.close();
           }
         }
         return returnVal;
       } else {
         throw new Exception("Failed to delete temp file for temp dir creation.");
       }
     } else if(!new File(gitRepo.getBaseDirectory()).getAbsoluteFile().getAbsolutePath().equals(new File(source).getAbsoluteFile().getAbsolutePath())){
       String rev = moveContentToGit(source, gitRepo, comment);
       return rev;
     } else {
       throw new Exception("Source must not be the same as the target.");
     }
   }
   
   private static String moveContentToGit(String source, GitService git, String comment) throws Exception {
     return git.checkinNewContent(source, comment);
   }
 
   
   /**
    * Initializes war configuration directory for a Cadmium war.
    * @param uri The remote Git repository ssh URI.
    * @param branch The remote branch to checkout.
    * @param root The shared root.
    * @param warName The name of the war file.
    * @param historyManager The history manager to log the initialization event.
    * @return A GitService object the points to the freshly cloned Git repository.
    * @throws RefNotFoundException
    * @throws Exception
    */
   public static GitService initializeConfigDirectory(String uri, String branch, String root, String warName, HistoryManager historyManager, ConfigManager configManager) throws Exception {
     initializeBaseDirectoryStructure(root, warName);
     String warDir = FileSystemManager.getChildDirectoryIfExists(root, warName);
     GitService cloned = initializeRepo(uri, branch, warDir, "git-config-checkout");
 
     File configPropsFile = new File(warDir, "config.properties");
     Properties configProperties = configManager.getDefaultProperties();
     
     String renderedContentDir = initializeSnapshotDirectory(warDir,
         configProperties, "com.meltmedia.cadmium.config.lastUpdated", "git-config-checkout", "config"); 
     
     configPropsFile = new File(warDir, "config.properties").getAbsoluteFile();
     
     boolean hasExisting = configProperties.containsKey("com.meltmedia.cadmium.config.lastUpdated") && renderedContentDir != null && renderedContentDir.equals(configProperties.getProperty("com.meltmedia.cadmium.config.lastUpdated"));
     if(renderedContentDir != null) {
       configProperties.setProperty("com.meltmedia.cadmium.config.lastUpdated", renderedContentDir);
     }
     configProperties.setProperty("config.branch", cloned.getBranchName());
     configProperties.setProperty("config.git.ref.sha", cloned.getCurrentRevision());
     configProperties.setProperty("config.repo", uri);
     
     configManager.persistProperties(configProperties, configPropsFile, "initialized configuration properties for configuration");
 
     boolean closeHistoryManager = false;
     if(historyManager == null) {
       closeHistoryManager = true;
       historyManager = new HistoryManager(warDir);
     }
     
     try{
       if(historyManager != null && !hasExisting) {
         historyManager.logEvent(EntryType.CONFIG, cloned.getRemoteRepository(), cloned.getBranchName(), cloned.getCurrentRevision(), "AUTO", renderedContentDir, "", "Initial config pull.", true, true);
       }
     } finally {
       if(closeHistoryManager) {
         IOUtils.closeQuietly(historyManager);
       }
     }
     
     return cloned;
   }
   
   /**
    * Initializes war content directory for a Cadmium war.
    * @param uri The remote Git repository ssh URI.
    * @param branch The remote branch to checkout.
    * @param root The shared content root.
    * @param warName The name of the war file.
    * @param historyManager The history manager to log the initialization event.
    * @return A GitService object the points to the freshly cloned Git repository.
    * @throws RefNotFoundException
    * @throws Exception
    */
   public static GitService initializeContentDirectory(String uri, String branch, String root, String warName, HistoryManager historyManager, ConfigManager configManager) throws Exception {
     initializeBaseDirectoryStructure(root, warName);
     String warDir = FileSystemManager.getChildDirectoryIfExists(root, warName);
     GitService cloned = initializeRepo(uri, branch, warDir, "git-checkout");
 
     File configPropsFile = new File(warDir, "config.properties");
     Properties configProperties = configManager.getDefaultProperties();
     
     String renderedContentDir = initializeSnapshotDirectory(warDir,
         configProperties, "com.meltmedia.cadmium.lastUpdated", "git-checkout", "renderedContent"); 
     
     configPropsFile = new File(warDir, "config.properties").getAbsoluteFile();
 
     boolean hasExisting = configProperties.containsKey("com.meltmedia.cadmium.lastUpdated") && renderedContentDir != null && renderedContentDir.equals(configProperties.getProperty("com.meltmedia.cadmium.lastUpdated"));
     if(renderedContentDir != null) {
       configProperties.setProperty("com.meltmedia.cadmium.lastUpdated", renderedContentDir);
     }
     configProperties.setProperty("branch", cloned.getBranchName());
     configProperties.setProperty("git.ref.sha", cloned.getCurrentRevision());
     configProperties.setProperty("repo", uri);
     
     if(renderedContentDir != null) {
       String sourceFilePath = renderedContentDir + File.separator + "MET-INF" + File.separator + "source";
       if(sourceFilePath != null && FileSystemManager.canRead(sourceFilePath)) {
         try {
           configProperties.setProperty("source", FileSystemManager.getFileContents(sourceFilePath));
         } catch(Exception e) {
           log.warn("Failed to read source file {}", sourceFilePath);
         }
       } else if(!configProperties.containsKey("source")){
         configProperties.setProperty("source", "{}");
       }
     } else if(!configProperties.containsKey("source")){
       configProperties.setProperty("source", "{}");
     }
     
     configManager.persistProperties(configProperties, configPropsFile, "initialized configuration properties");
 
     boolean closeHistoryManager = false;
     if(historyManager == null) {
       closeHistoryManager = true;
       historyManager = new HistoryManager(warDir);
     }
     
     try{
       if(historyManager != null && !hasExisting) {
         historyManager.logEvent(EntryType.CONTENT, cloned.getRemoteRepository(), cloned.getBranchName(), cloned.getCurrentRevision(), "AUTO", renderedContentDir, "", "Initial content pull.", true, true);
       }
     } finally {
       if(closeHistoryManager) {
         IOUtils.closeQuietly(historyManager);
       }
     }
     
     return cloned;
   }
 
   private static String initializeSnapshotDirectory(String warDir,
       Properties configProperties, String key, String checkoutDir, String snapshotDir) throws Exception, IOException {
     String renderedContentDir = configProperties.getProperty(key);
     log.debug("Making sure {} exists.", renderedContentDir);
     if(renderedContentDir == null || !FileSystemManager.exists(renderedContentDir)) {
       log.info(snapshotDir + " directory does not exist. Creating!!!");
       GitService rendered = null;
       File snapshotFile = new File(warDir, snapshotDir);
       if(snapshotFile.exists()) {
         log.warn("{} exists @ {}, Deleting and recreating. ", snapshotDir, snapshotFile);
         FileUtils.forceDelete(snapshotFile);
       }
       try {
         rendered = GitService.cloneRepo(new File(FileSystemManager.getChildDirectoryIfExists(warDir, checkoutDir), ".git").getAbsolutePath(), snapshotFile.getAbsolutePath());
         renderedContentDir = rendered.getBaseDirectory();
         log.info("Removing .git directory from freshly cloned " + snapshotDir + " directory @ {}.", renderedContentDir);
         String gitDir = FileSystemManager.getChildDirectoryIfExists(snapshotFile.getAbsolutePath(), ".git");
         if(gitDir != null) {
           FileSystemManager.deleteDeep(gitDir);
         }
         
       } finally {
         if(rendered != null) {
           rendered.close();
         }
       }
     }
     return renderedContentDir;
   }
 
   private static GitService initializeRepo(String uri, String branch,
       String warDir, String checkoutDir) throws Exception, RefNotFoundException {
     GitService cloned = null;
     if(FileSystemManager.getChildDirectoryIfExists(warDir, checkoutDir) == null) {
       log.info("Cloning remote git repository to " + checkoutDir);
       cloned = cloneRepo(uri, new File(warDir, checkoutDir).getAbsolutePath());
       
       if(!cloned.checkForRemoteBranch(branch)) {
         String envString = System.getProperty("com.meltmedia.cadmium.environment", "development");
         branch = "cd-"+envString+"-"+branch;
       }
       log.info("Switching to branch {}", branch);
       cloned.switchBranch(branch);
       
     } else {
       cloned = createGitService(FileSystemManager.getChildDirectoryIfExists(warDir, checkoutDir));
     }
 
     if(cloned == null) {
       throw new Exception("Failed to clone remote github repo from "+uri);
     }
     return cloned;
   }
 
   private static void initializeBaseDirectoryStructure(String root,
       String warName) throws Exception {
     if(!FileSystemManager.exists(root)) {
       log.info("Content Root directory [{}] does not exist. Creating!!!", root);
       if(!new File(root).mkdirs()) {
         throw new Exception("Failed to create cadmium root @ "+root);
       }
     }
     String warDir = FileSystemManager.getChildDirectoryIfExists(root, warName);
     if(warDir == null) {
       log.info("War directory [{}] does not exist. Creating!!!", warName);
       if(!new File(root, warName).mkdirs()) {
         throw new Exception("Failed to create war content directory @ " + warDir);
       }
     }
   }
   
   public static GitService init(String site, String dir) throws Exception {
   	String repoPath = dir + "/" + site;
   	log.debug("Repository Path :" + repoPath);
   	Repository repo = new FileRepository(repoPath + "/.git");
   	try {
   		repo.create();
   		Git git = new Git(repo);
   		
   		File localGitRepo = new File(repoPath);
   		localGitRepo.mkdirs();
   		new File(localGitRepo, "delete.me").createNewFile();
   		
   		git.add().addFilepattern("delete.me").call();
   		git.commit().setMessage("initial commit").call();
   		return new GitService(git);  		
   	} catch (IllegalStateException e) {
   		log.debug("Repo Already exists locally");
   	}
 		return null;
   }
   
   public String getRepositoryDirectory() throws Exception {
     return git.getRepository().getDirectory().getAbsolutePath();
   }
   
   public String getBaseDirectory() throws Exception {
     return FileSystemManager.getParent(getRepositoryDirectory());
   }
   
   public boolean pull() throws Exception {
     log.debug("Pulling latest updates from remote git repo");
     return git.pull().call().isSuccessful();
   }
   
   public void push(boolean tags) throws Exception {
     PushCommand push = git.push();
     if(tags) {
       push.setPushTags();
     }
     push.call();
   }
   
   public void switchBranch(String branchName) throws RefNotFoundException, Exception {
     Repository repository = git.getRepository();
     if(branchName != null && !repository.getBranch().equals(branchName)) {
       log.info("Switching branch from {} to {}", repository.getBranch(), branchName);
       CheckoutCommand checkout = git.checkout();
       if(isTag(branchName)) {
         checkout.setName(branchName);
       } else {
         checkout.setName("refs/heads/"+branchName);
         if(repository.getRef("refs/heads/"+ branchName) == null) {
           CreateBranchCommand create = git.branchCreate();
           create.setName(branchName);
           create.setUpstreamMode(SetupUpstreamMode.SET_UPSTREAM);
           create.setStartPoint("origin/"+branchName);
           create.call();
         }
       }
       checkout.call();
     }
   }
   
   public void resetToRev(String revision) throws Exception {
     if(revision != null) {
       log.info("Resetting to sha {}", revision);
       git.reset().setMode(ResetType.HARD).setRef(revision).call();
     }
   }
 
   public boolean checkRevision(String revision) throws Exception {
     String oldRevision = null;
     try {
       log.info("Checking if revision is on current branch.");
       oldRevision = getCurrentRevision();
       resetToRev("refs/remotes/origin/"+getBranchName());
       boolean found = false;
       for(RevCommit commit : git.log().call()){
         if(commit.getId().getName().equals(revision)) {
           found = true;
           break;
         }
       }
       return found;
     } catch(Exception e){
       log.error("Failed to read repository state.", e);
       throw e;
     } finally {
       if(oldRevision != null && !getCurrentRevision().equals(oldRevision)) {
         resetToRev(oldRevision);
       }
     }
   }
   
   private boolean checkForRemoteBranch(String branchName) throws Exception {
     log.info("Getting list of existing branches.");
     List<Ref> refs = git.branchList().setListMode(ListMode.ALL).call();
     boolean branchExists = false; 
     if(refs != null) {
       for(Ref ref : refs) {
         if(ref.getName().endsWith("/" + branchName)) {
           branchExists = true;
           break;
         }
       }
     }
     return branchExists;
   }
   
   public boolean newRemoteBranch(String branchName) throws Exception {
     try{
       log.info("Purging branches that no longer have remotes.");
       git.fetch().setRemoveDeletedRefs(true).call();
     } catch(Exception e) {
       log.warn("Tried to fetch from remote when there is no remote.");
       return false;
     }
     boolean branchExists = checkForRemoteBranch(branchName);
     if(!branchExists) {
       Ref ref = newLocalBranch(branchName);
       git.push().add(ref).call();
       return true;
     } else {
       log.info(branchName + " already exists.");
     }
     return false;
   }
   
   public Ref newLocalBranch(String branchName) throws Exception {
     return git.branchCreate().setName(branchName).call();
   }
   
   public void deleteLocalBranch(String branchName) throws Exception {
     git.branchDelete().setForce(true).setBranchNames(branchName).call();
   }
   
   /**
    * Checks in content from a source directory into the current git repository.
    * @param sourceDirectory The directory to pull content in from.
    * @param message The commit message to use.
    * @return The new SHA revision.
    * @throws Exception
    */
   public String checkinNewContent(String sourceDirectory, String message) throws Exception {
     log.info("Removing old content.");
     RmCommand remove = git.rm();
     for(String filename : new File(getBaseDirectory()).list()) {
       if(!filename.equals(".git")) {
         remove.addFilepattern(filename);
       }
     }
     remove.call();
     log.info("Copying in new content.");
     FileSystemManager.copyAllContent(sourceDirectory, getBaseDirectory(), true);
     log.info("Adding new content.");
     AddCommand add = git.add();
     for(String filename : new File(getBaseDirectory()).list()) {
       if(!filename.equals(".git")) {
         add.addFilepattern(filename);
       }
     }
     add.call();
     log.info("Committing new content.");
     git.commit().setMessage(message).call();
     return getCurrentRevision();
   }
   
   public boolean tag(String tagname, String comment) throws Exception {
     try{
       git.fetch().setTagOpt(TagOpt.FETCH_TAGS).call();
     } catch(Exception e) {
       log.debug("Fetch from origin failed.", e);
     }
     List<RevTag> tags = git.tagList().call();
     if(tags != null && tags.size() > 0) {
       for(RevTag tag : tags) {
         if(tag.getTagName().equals(tagname)) {
           throw new Exception("Tag already exists.");
         }
       }
     }
     boolean success = git.tag().setMessage(comment).setName(tagname).call() != null;
     try{
       git.push().setPushTags().call();
     } catch(Exception e){
       log.debug("Failed to push changes.", e);
     }
     return success;
   }
   
   public boolean isTag(String tagname) throws Exception {
     Map<String, Ref> allRefs = git.getRepository().getTags();
     for(String key : allRefs.keySet()) {
       Ref ref = allRefs.get(key);
       log.debug("Checking tag key{}, ref{}", key, ref.getName());
       if(key.equals(tagname) && ref.getName().equals("refs/tags/" + tagname)) {
         return true;
       }
     }
     return false;
   }
   
   public void fetchRemotes() throws Exception {
     git.fetch().setTagOpt(TagOpt.FETCH_TAGS).setCheckFetchedObjects(false).setRemoveDeletedRefs(true).call();
   }
   
   public boolean isBranch(String branchname) throws Exception {
     log.debug("Checking if {} is a branch on {}", branchname, getRemoteRepository());
     for(Ref ref : git.branchList().setListMode(ListMode.ALL).call()){
       log.debug("Checking {}, {}", ref.getName(), branchname);
       if(ref.getName().equals("refs/heads/"+branchname) || ref.getName().equals("refs/remotes/origin/"+branchname)) {
         return true;
       }
     }
     return false;
   }
   
   public String getBranchName() throws Exception {
     Repository repository = git.getRepository();
     if(ObjectId.isId(repository.getFullBranch()) && repository.getFullBranch().equals(repository.resolve("HEAD").getName())) {
       RevWalk revs = null;
       try {
         log.debug("Trying to resolve tagname: {}", repository.getFullBranch());
         ObjectId tagRef = ObjectId.fromString(repository.getFullBranch());
         revs = new RevWalk(repository);
         RevCommit commit = revs.parseCommit(tagRef);
         Map<String, Ref> allTags = repository.getTags();
         for(String key : allTags.keySet()) {
           Ref ref = allTags.get(key);
           RevTag tag = revs.parseTag(ref.getObjectId());
           log.debug("Checking ref {}, {}", commit.getName(), tag.getObject());
           if(tag.getObject().equals(commit)) {
             return key;
           }
         }
       } catch(Exception e) {
         log.warn("Invalid id: {}", repository.getFullBranch(), e);
       } finally {
         revs.release();
       }
     }
     return repository.getBranch();
   }
   
   public String getCurrentRevision() throws Exception {
     return git.getRepository().getRef(getBranchName()).getObjectId().getName();
   }
   
   public String getRemoteRepository() {
     return git.getRepository().getConfig().getString("remote", "origin", "url");
   }
   
   public void close() throws IOException {
     git.getRepository().close();
   }
 }
