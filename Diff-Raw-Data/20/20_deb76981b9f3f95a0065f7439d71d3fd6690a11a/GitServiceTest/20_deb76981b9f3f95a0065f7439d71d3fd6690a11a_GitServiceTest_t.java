 /**
  *   Copyright 2012 meltmedia
  *
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  */
 package com.meltmedia.cadmium.core.git;
 
 import static org.junit.Assert.assertTrue;
 
 import java.io.File;
 import java.util.Properties;
 
 import org.eclipse.jgit.api.AddCommand;
 import org.eclipse.jgit.api.Git;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import com.meltmedia.cadmium.core.config.ConfigManager;
 
 import static org.mockito.Mockito.*;
 
 public class GitServiceTest {
   private File testDir;
   private File gitRepo1;
   private GitService git1;
   private File gitRepo2;
   private GitService git2;
   private File gitRepo3;
   private GitService git3;
   private File localGitRepo;
   private GitService localGit;
   private File localGitRepoCloned;
   private GitService localClone;
   ConfigManager configManager = new ConfigManager();
   
   @Before
   public void createDirForTests() throws Exception {    
           
     
     // TODO delete every line under here....
     testDir = new File("./target/git-test");
     if(!testDir.exists()) {
       if(testDir.mkdir()){
         gitRepo1 = new File(testDir, "checkout1");
         git1 = GitService.cloneRepo("git://github.com/meltmedia/test-content-repo.git", gitRepo1.getAbsolutePath());
         
         gitRepo2 = new File(testDir, "checkout2");
         git2 = GitService.cloneRepo("git://github.com/meltmedia/test-content-repo.git", gitRepo2.getAbsolutePath());
         
         gitRepo3 = new File(testDir, "checkout3");
         git3 = GitService.cloneRepo("git://github.com/meltmedia/test-content-repo.git", gitRepo3.getAbsolutePath());
         localGitRepo = new File(testDir, "local-git");
         localGitRepo.mkdirs();
         new File(localGitRepo, "delete.me").createNewFile();
         new File(localGitRepo, "dir1").mkdirs();
         new File(localGitRepo, "dir1/remove.me").createNewFile();
         
         localGit = new GitService(Git.init().setDirectory(localGitRepo).call());
         AddCommand add = localGit.git.add();
         add.addFilepattern("delete.me");
         add.addFilepattern("dir1");
         add.call();
         localGit.git.commit().setMessage("initial commit").call();
         localGit.git.branchCreate().setName("test").call();
         localGit.git.branchCreate().setName("test-delete").call();
         
         localGitRepoCloned = new File(testDir, "local-git-cloned");
         localClone = GitService.cloneRepo(new File(localGitRepo, ".git").getAbsoluteFile().getAbsolutePath(), localGitRepoCloned.getAbsoluteFile().getAbsolutePath());
         
       } else {
         throw new Exception("Failed to set up tests");
       }
     } else {
       gitRepo1 = new File(testDir, "checkout1");
       git1 = GitService.createGitService(new File(gitRepo1, ".git").getAbsolutePath());
       
       gitRepo2 = new File(testDir, "checkout2");
       git2 = GitService.createGitService(new File(gitRepo2, ".git").getAbsolutePath());
       
       gitRepo3 = new File(testDir, "checkout3");
       git3 = GitService.createGitService(new File(gitRepo3, ".git").getAbsolutePath());
       
       localGitRepo = new File(testDir, "local-git");
       localGit = GitService.createGitService(new File(localGitRepo, ".git").getAbsolutePath());
       
       localGitRepoCloned = new File(testDir, "local-git-cloned");
       localClone = GitService.createGitService(new File(localGitRepoCloned, ".git").getAbsolutePath());
     }
     
     File source = new File(testDir, "source");
     source.mkdirs();
     
     source = new File(source, "other.file");
     source.createNewFile();
 
     source = new File(testDir, "source/dir2");
     source.mkdirs();
 
     source = new File(source, "other.file");
     source.createNewFile();
     
     source = new File(testDir, "source2");
     source.mkdirs();
     
     source = new File(source, "other2.file");
     source.createNewFile();
 
     source = new File(testDir, "source2/dir3");
     source.mkdirs();
 
     source = new File(source, "other2.file");
     source.createNewFile();
   }
   
   @After
   public void closeGitServices() throws Exception {
     git1.close();
     git2.close();
     git3.close();
     localGit.close();
     localClone.close();
   }
   
   @Test
   public void testPull() throws Exception {
     assertTrue("Failed to pull git checkout1", git1.pull());
     assertTrue("Failed to pull git checkout2", git2.pull());
   }
   
   @Test
   public void testSwitchBranch() throws Exception {
     String currentBranch = "master";
     String nextBranch = "other-branch";
     
     git1.switchBranch(nextBranch);
     assertTrue("Branch didn't change ["+git1.getBranchName()+"]", git1.getBranchName().equals(nextBranch));
     git1.switchBranch(currentBranch);
     assertTrue("Branch didn't change back ["+git1.getBranchName()+"]", git1.getBranchName().equals(currentBranch));
     System.out.println("getBranchName for a branch ["+git1.getBranchName()+"]");
   }
   
   @Test
   public void testResetToRev() throws Exception {
     String currentRev = git2.getCurrentRevision();
     String prevRev = "41fb29368e8649c1ee2ea74228414553dd1f2d45";
     
     git2.resetToRev(prevRev);
     assertTrue("Revision not reset to previous revision ["+git2.getCurrentRevision()+"]", git2.getCurrentRevision().equals(prevRev));
     git2.resetToRev(currentRev);
     assertTrue("Revision not reset to current revision ["+git2.getCurrentRevision()+"]", git2.getCurrentRevision().equals(currentRev));
   }
   
   @Test
   public void testCheckRef() throws Exception {
     git2.switchBranch("master");
     assertTrue("Revision should not exist", !git2.checkRevision("c81161f84d24b5b43c5969e6498c3b24d372b4e9"));
     assertTrue("Revision should exist", git2.checkRevision("41fb29368e8649c1ee2ea74228414553dd1f2d45"));
   }
   
   @Test
   public void testCloneDiskBasedRepo() throws Exception {
     GitService git = GitService.cloneRepo(git1.getRepositoryDirectory(), new File(testDir, "cloned").getAbsolutePath());
     assertTrue("New Git service not created", git != null);
     git.close();
     assertTrue("Failed to create new directory", new File(testDir, "cloned").exists());
   }
   
   @Test
   public void testNewRemoteBranch() throws Exception {
     
     assertTrue("Branch shouldn't yet exist", localGit.repository.getRef("newBranch") == null);
     
     assertTrue("Branch should have been created", localClone.newRemoteBranch("newBranch"));
     
     assertTrue("Branch did not get created in remote", localGit.repository.getRef("newBranch") != null);
     
     assertTrue("Branch did not get created in local", localClone.repository.getRef("newBranch") != null);
     
     assertTrue("Branch shouldn't allow me to create it", !localClone.newRemoteBranch("newBranch"));
   }
   
   @Test
   public void testInitializeContentDirectory() throws Exception {
     GitService cloned = null;
     try {
      cloned = GitService.initializeContentDirectory(localGitRepo.getAbsolutePath(), "master", new File(testDir, "content").getAbsolutePath(), "cadmium.war", null, configManager);
     } finally {
       if(cloned != null) {
         cloned.close();
       }
     }
     assertTrue("Initialize method failed", cloned != null);
     cloned = null;
     try {
      cloned = GitService.initializeContentDirectory(localGitRepo.getAbsolutePath(), "master", new File(testDir, "content").getAbsolutePath(), "cadmium.war", null, configManager);
     } finally {
       if(cloned != null) {
         cloned.close();
       }
     }
     
     assertTrue("Initialize method failed", cloned != null);
     File contentDir = new File(testDir, "content");
     assertTrue("Content dir does not exist", contentDir.exists() && contentDir.isDirectory());
     
     File warDir = new File(contentDir, "cadmium.war");
     assertTrue("War dir does not exist", warDir.exists() && contentDir.isDirectory());
     
     File gitDir = new File(warDir, "git-checkout");
     assertTrue("Git repo not checked out", gitDir.exists() && gitDir.isDirectory());
     
     File dotGitDir = new File(gitDir, ".git");
     assertTrue(".Git dir not created", dotGitDir.exists() && dotGitDir.isDirectory());
     
     File renderedContentDir = new File(warDir, "renderedContent");
     assertTrue("RenderedContent dir not created", renderedContentDir.exists() && renderedContentDir.isDirectory());
     
     File otherdotGitDir = new File(renderedContentDir, ".git");
     assertTrue(".Git dir not deleted", !otherdotGitDir.exists());
     
     
   }
   
   @Test
   public void testCheckinNewContent() throws Exception {
     localGit.checkinNewContent("./target/git-test/source", "test");
     
     assertTrue("delete.me should be gone.", !new File(localGitRepo, "delete.me").exists());
     assertTrue("dir1/remove.me should be gone.", !new File(localGitRepo, "dir1/remove.me").exists());
     assertTrue("dir1 should be gone.", !new File(localGitRepo, "dir1").exists());
     assertTrue("other.file should be there.", new File(localGitRepo, "other.file").exists());
     assertTrue("dir2/other.file should be there.", new File(localGitRepo, "dir2/other.file").exists());
   }
   
   @Test
   public void testMoveContentToBranch() throws Exception {
     String rev = GitService.moveContentToBranch("./target/git-test/source2", localGit, "test", "test moveContentToBranch");
     
     assertTrue("New revision(sha) is not returned", rev != null && rev.length() > 0);
     
     localGit.git.checkout().setName("test").call();
 
     assertTrue("other2.file should be there.", new File(localGitRepo, "other2.file").exists());
     assertTrue("dir3/other2.file should be there.", new File(localGitRepo, "dir3/other2.file").exists());
   }
   
   @Test
   public void testTag() throws Exception {
     if(!localGit.getBranchName().equals("test")){
       localGit.git.checkout().setName("test").call();
     }
     
     localGit.tag("release-1.0", "Testing tag creation.");
     
     assertTrue("Tag not created", localGit.repository.getRef("refs/tags/release-1.0") != null);
     
     localClone.fetchRemotes();
     localClone.switchBranch("release-1.0");
     System.out.println("getBranchName for a tag ["+localClone.getBranchName()+"]");
   }
   
   @Test
   public void testIsTag() throws Exception {
     if(!localGit.getBranchName().equals("test")){
       localGit.git.checkout().setName("test").call();
     }
     
     localGit.tag("release-1.1", "Testing isTag test.");
     
     assertTrue("Tag not created", localGit.repository.getRef("refs/tags/release-1.1") != null);
     
     assertTrue("Failed", localGit.isTag("release-1.1"));
     
   }
   
   @Test
   public void testIsBranch() throws Exception {
     if(!localGit.getBranchName().equals("test")){
       localGit.git.checkout().setName("test").call();
     }
        
     assertTrue("Master is a branch", localGit.isBranch("master"));
     
     assertTrue("test is a branch", localGit.isBranch("test"));
     
     assertTrue("test-65 is not a branch", !localGit.isBranch("test-65"));
     
   }
   
   @Test
   public void testNewLocalBranch() throws Exception {
     localGit.newLocalBranch("test-local");
     
     assertTrue("Branch not created", localGit.repository.getRef("refs/heads/test-local") != null);
   }
   
   @Test
   public void testDeleteLocalBranch() throws Exception {
     assertTrue("Branch should exist", localGit.repository.getRef("refs/heads/test-delete") != null);
     
     localGit.deleteLocalBranch("test-delete");
     
     assertTrue("Branch not delete", localGit.repository.getRef("refs/heads/test-delete") == null);
   }
   
   @Test
   public void testFetch() throws Exception {
     git3.fetchRemotes();
   }
 }
 
