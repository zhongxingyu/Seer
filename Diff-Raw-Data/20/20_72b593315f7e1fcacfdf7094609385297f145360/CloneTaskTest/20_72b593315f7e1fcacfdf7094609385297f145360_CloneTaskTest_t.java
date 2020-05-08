 /***********************************************************************************************
  * Copyright (c) Microsoft Corporation All rights reserved.
  * 
  * MIT License:
  * 
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  ***********************************************************************************************/
 
 package com.microsoft.gittf.core.tasks;
 
 import java.io.File;
 import java.net.URI;
 import java.util.Calendar;
 import java.util.List;
 
 import junit.framework.TestCase;
 
 import org.eclipse.jgit.api.Git;
 import org.eclipse.jgit.lib.PersonIdent;
 import org.eclipse.jgit.lib.Ref;
 import org.eclipse.jgit.lib.Repository;
 import org.eclipse.jgit.revwalk.RevCommit;
 import org.junit.Test;
 
 import com.microsoft.gittf.core.GitTFConstants;
import com.microsoft.gittf.core.config.ConfigurationConstants;
 import com.microsoft.gittf.core.config.GitTFConfiguration;
 import com.microsoft.gittf.core.mock.MockChangesetProperties;
 import com.microsoft.gittf.core.mock.MockVersionControlService;
 import com.microsoft.gittf.core.tasks.framework.NullTaskProgressMonitor;
 import com.microsoft.gittf.core.tasks.framework.TaskStatus;
 import com.microsoft.gittf.core.test.Util;
 import com.microsoft.gittf.core.util.RepositoryUtil;
 
 public class CloneTaskTest
     extends TestCase
 {
     protected void setUp()
         throws Exception
     {
         Util.setUp(getName());
     }
 
     protected void tearDown()
         throws Exception
     {
         Util.tearDown(getName());
     }
 
     @Test
     public void testShallowCloneFilesAndFolders()
         throws Exception
     {
         URI projectCollectionURI = new URI("http://fakeCollection:8080/tfs/DefaultCollection"); //$NON-NLS-1$
         String tfsPath = "$/project"; //$NON-NLS-1$
         String gitRepositoryPath = Util.getRepositoryFile(getName()).getAbsolutePath();
 
         final MockVersionControlService mockVersionControlService = new MockVersionControlService();
 
         mockVersionControlService.AddFile("$/project/folder/file0.txt", 1); //$NON-NLS-1$
         mockVersionControlService.AddFile("$/project/folder2/file0.txt", 1); //$NON-NLS-1$
         mockVersionControlService.AddFile("$/project/folder/nestedFolder/file0.txt", 1); //$NON-NLS-1$
 
         mockVersionControlService.AddFile("$/project/folder/file1.txt", 2); //$NON-NLS-1$
         mockVersionControlService.AddFile("$/project/folder2/file1.txt", 2); //$NON-NLS-1$
         mockVersionControlService.AddFile("$/project/folder/nestedFolder/file1.txt", 2); //$NON-NLS-1$
 
         mockVersionControlService.AddFile("$/project/folder/file2.txt", 3); //$NON-NLS-1$
         mockVersionControlService.AddFile("$/project/folder2/file2.txt", 3); //$NON-NLS-1$
         mockVersionControlService.AddFile("$/project/folder/nestedFolder/file2.txt", 3); //$NON-NLS-1$
 
         Calendar date = Calendar.getInstance();
         date.set(2012, 11, 12, 18, 15);
 
         MockChangesetProperties changesetProperties = new MockChangesetProperties("ownerDisplayName", //$NON-NLS-1$
             "ownerName", //$NON-NLS-1$
             "committerDisplayName", //$NON-NLS-1$
             "committerName", //$NON-NLS-1$
             "comment", //$NON-NLS-1$
             date);
         mockVersionControlService.updateChangesetInformation(changesetProperties, 3);
 
         final Repository repository = RepositoryUtil.createNewRepository(gitRepositoryPath, false);
        final boolean defaultDeep =
            repository.getConfig().getInt(
                ConfigurationConstants.CONFIGURATION_SECTION,
                ConfigurationConstants.GENERAL_SUBSECTION,
                ConfigurationConstants.DEPTH,
                GitTFConstants.GIT_TF_SHALLOW_DEPTH) > 1;
 
         CloneTask cloneTask = new CloneTask(projectCollectionURI, mockVersionControlService, tfsPath, repository);
         TaskStatus cloneTaskStatus = cloneTask.run(new NullTaskProgressMonitor());
 
         // Verify task completed without errors
         assertTrue(cloneTaskStatus.isOK());
 
         // Load Git Repo
         Git git = new Git(repository);
         git.checkout().setName("master").call(); //$NON-NLS-1$
 
         // Verify Changeset 1
         assertTrue(mockVersionControlService.verifyFileContent(new File(gitRepositoryPath, "folder/file0.txt"), //$NON-NLS-1$
             "$/project/folder/file0.txt", //$NON-NLS-1$
             1));
 
         assertTrue(mockVersionControlService.verifyFileContent(new File(gitRepositoryPath, "folder2/file0.txt"), //$NON-NLS-1$
             "$/project/folder2/file0.txt", //$NON-NLS-1$
             1));
 
         assertTrue(mockVersionControlService.verifyFileContent(new File(
             gitRepositoryPath,
             "folder/nestedFolder/file0.txt"), //$NON-NLS-1$
             "$/project/folder/nestedFolder/file0.txt", //$NON-NLS-1$
             1));
 
         // Verify Changeset 2
         assertTrue(mockVersionControlService.verifyFileContent(new File(gitRepositoryPath, "folder/file1.txt"), //$NON-NLS-1$
             "$/project/folder/file1.txt", //$NON-NLS-1$
             2));
 
         assertTrue(mockVersionControlService.verifyFileContent(new File(gitRepositoryPath, "folder2/file1.txt"), //$NON-NLS-1$
             "$/project/folder2/file1.txt", //$NON-NLS-1$
             2));
 
         assertTrue(mockVersionControlService.verifyFileContent(new File(
             gitRepositoryPath,
             "folder/nestedFolder/file1.txt"), //$NON-NLS-1$
             "$/project/folder/nestedFolder/file1.txt", //$NON-NLS-1$
             2));
 
         // Verify Changeset 3
         assertTrue(mockVersionControlService.verifyFileContent(new File(gitRepositoryPath, "folder/file2.txt"), //$NON-NLS-1$
             "$/project/folder/file2.txt", //$NON-NLS-1$
             3));
 
         assertTrue(mockVersionControlService.verifyFileContent(new File(gitRepositoryPath, "folder2/file2.txt"), //$NON-NLS-1$
             "$/project/folder2/file2.txt", //$NON-NLS-1$
             3));
 
         assertTrue(mockVersionControlService.verifyFileContent(new File(
             gitRepositoryPath,
             "folder/nestedFolder/file2.txt"), //$NON-NLS-1$
             "$/project/folder/nestedFolder/file2.txt", //$NON-NLS-1$
             3));
 
         // Verify Git Repo configuration
         GitTFConfiguration gitRepoServerConfig = GitTFConfiguration.loadFrom(repository);
 
         assertEquals(gitRepoServerConfig.getServerURI(), projectCollectionURI);
         assertEquals(gitRepoServerConfig.getServerPath(), tfsPath);
        assertEquals(gitRepoServerConfig.getDeep(), defaultDeep);
 
         // Verify the number of commits
         Iterable<RevCommit> commits = git.log().call();
 
         assertNotNull(commits);
 
         int commitCounter = 0;
         for (RevCommit commit : commits)
         {
             assertEquals(commit.getFullMessage(), "comment"); //$NON-NLS-1$
 
             PersonIdent ownwer = commit.getAuthorIdent();
             assertEquals(ownwer.getEmailAddress(), "ownerName"); //$NON-NLS-1$
             assertEquals(ownwer.getName(), "ownerDisplayName"); //$NON-NLS-1$
 
             PersonIdent committer = commit.getCommitterIdent();
             assertEquals(committer.getEmailAddress(), "committerName"); //$NON-NLS-1$
             assertEquals(committer.getName(), "committerDisplayName"); //$NON-NLS-1$
 
             commitCounter++;
         }
 
         assertEquals(commitCounter, 1);
 
         // Verify the tags
         List<Ref> tags = git.tagList().call();
         assertEquals(1, tags.size());
     }
 
     @Test
     public void testDeepCloneFilesAndFoldersSimple()
         throws Exception
     {
         URI projectCollectionURI = new URI("http://fakeCollection:8080/tfs/DefaultCollection"); //$NON-NLS-1$
         String tfsPath = "$/project"; //$NON-NLS-1$
         String gitRepositoryPath = Util.getRepositoryFile(getName()).getAbsolutePath();
 
         final MockVersionControlService mockVersionControlService = new MockVersionControlService();
 
         mockVersionControlService.AddFile("$/project/folder/file0.txt", 1); //$NON-NLS-1$
         mockVersionControlService.AddFile("$/project/folder2/file0.txt", 1); //$NON-NLS-1$
         mockVersionControlService.AddFile("$/project/folder/nestedFolder/file0.txt", 1); //$NON-NLS-1$
 
         mockVersionControlService.AddFile("$/project/folder/file1.txt", 2); //$NON-NLS-1$
         mockVersionControlService.AddFile("$/project/folder2/file1.txt", 2); //$NON-NLS-1$
         mockVersionControlService.AddFile("$/project/folder/nestedFolder/file1.txt", 2); //$NON-NLS-1$
 
         mockVersionControlService.AddFile("$/project/folder/file2.txt", 3); //$NON-NLS-1$
         mockVersionControlService.AddFile("$/project/folder2/file2.txt", 3); //$NON-NLS-1$
         mockVersionControlService.AddFile("$/project/folder/nestedFolder/file2.txt", 3); //$NON-NLS-1$
 
         Calendar date = Calendar.getInstance();
         date.set(2012, 11, 12, 18, 15);
 
         MockChangesetProperties changesetProperties = new MockChangesetProperties("ownerDisplayName1", //$NON-NLS-1$
             "ownerName1", //$NON-NLS-1$
             "committerDisplayName1", //$NON-NLS-1$
             "committerName1", //$NON-NLS-1$
             "comment1", //$NON-NLS-1$
             date);
 
         mockVersionControlService.updateChangesetInformation(changesetProperties, 1);
 
         changesetProperties = new MockChangesetProperties("ownerDisplayName2", //$NON-NLS-1$
             "ownerName2", //$NON-NLS-1$
             "committerDisplayName2", //$NON-NLS-1$
             "committerName2", //$NON-NLS-1$
             "comment2", //$NON-NLS-1$
             date);
 
         mockVersionControlService.updateChangesetInformation(changesetProperties, 2);
 
         changesetProperties = new MockChangesetProperties("ownerDisplayName3", //$NON-NLS-1$
             "ownerName3", //$NON-NLS-1$
             "committerDisplayName3", //$NON-NLS-1$
             "committerName3", //$NON-NLS-1$
             "comment3", //$NON-NLS-1$
             date);
 
         mockVersionControlService.updateChangesetInformation(changesetProperties, 3);
 
         final Repository repository = RepositoryUtil.createNewRepository(gitRepositoryPath, false);
        final boolean defaultDeep =
            repository.getConfig().getInt(
                ConfigurationConstants.CONFIGURATION_SECTION,
                ConfigurationConstants.GENERAL_SUBSECTION,
                ConfigurationConstants.DEPTH,
                GitTFConstants.GIT_TF_SHALLOW_DEPTH) > 1;
 
         CloneTask cloneTask = new CloneTask(projectCollectionURI, mockVersionControlService, tfsPath, repository);
         cloneTask.setDepth(10);
 
         TaskStatus cloneTaskStatus = cloneTask.run(new NullTaskProgressMonitor());
 
         // Verify task completed without errors
         assertTrue(cloneTaskStatus.isOK());
 
         // Load Git Repo
         Git git = new Git(repository);
         git.checkout().setName("master").call(); //$NON-NLS-1$
 
         // Verify Changeset 1
         assertTrue(mockVersionControlService.verifyFileContent(new File(gitRepositoryPath, "folder/file0.txt"), //$NON-NLS-1$
             "$/project/folder/file0.txt", //$NON-NLS-1$
             1));
 
         assertTrue(mockVersionControlService.verifyFileContent(new File(gitRepositoryPath, "folder2/file0.txt"), //$NON-NLS-1$
             "$/project/folder2/file0.txt", //$NON-NLS-1$
             1));
 
         assertTrue(mockVersionControlService.verifyFileContent(new File(
             gitRepositoryPath,
             "folder/nestedFolder/file0.txt"), //$NON-NLS-1$
             "$/project/folder/nestedFolder/file0.txt", //$NON-NLS-1$
             1));
 
         // Verify Changeset 2
         assertTrue(mockVersionControlService.verifyFileContent(new File(gitRepositoryPath, "folder/file1.txt"), //$NON-NLS-1$
             "$/project/folder/file1.txt", //$NON-NLS-1$
             2));
 
         assertTrue(mockVersionControlService.verifyFileContent(new File(gitRepositoryPath, "folder2/file1.txt"), //$NON-NLS-1$
             "$/project/folder2/file1.txt", //$NON-NLS-1$
             2));
 
         assertTrue(mockVersionControlService.verifyFileContent(new File(
             gitRepositoryPath,
             "folder/nestedFolder/file1.txt"), //$NON-NLS-1$
             "$/project/folder/nestedFolder/file1.txt", //$NON-NLS-1$
             2));
 
         // Verify Changeset 3
         assertTrue(mockVersionControlService.verifyFileContent(new File(gitRepositoryPath, "folder/file2.txt"), //$NON-NLS-1$
             "$/project/folder/file2.txt", //$NON-NLS-1$
             3));
 
         assertTrue(mockVersionControlService.verifyFileContent(new File(gitRepositoryPath, "folder2/file2.txt"), //$NON-NLS-1$
             "$/project/folder2/file2.txt", //$NON-NLS-1$
             3));
 
         assertTrue(mockVersionControlService.verifyFileContent(new File(
             gitRepositoryPath,
             "folder/nestedFolder/file2.txt"), //$NON-NLS-1$
             "$/project/folder/nestedFolder/file2.txt", //$NON-NLS-1$
             3));
 
         // Verify Git Repo configuration
         GitTFConfiguration gitRepoServerConfig = GitTFConfiguration.loadFrom(repository);
 
         assertEquals(gitRepoServerConfig.getServerURI(), projectCollectionURI);
         assertEquals(gitRepoServerConfig.getServerPath(), tfsPath);
        assertEquals(gitRepoServerConfig.getDeep(), defaultDeep);
 
         // Verify the number of commits
         Iterable<RevCommit> commits = git.log().call();
 
         assertNotNull(commits);
 
         int commitCounter = 3;
         for (RevCommit commit : commits)
         {
             assertEquals(commit.getFullMessage(), "comment" + Integer.toString(commitCounter)); //$NON-NLS-1$
 
             PersonIdent ownwer = commit.getAuthorIdent();
             assertEquals(ownwer.getEmailAddress(), "ownerName" + Integer.toString(commitCounter)); //$NON-NLS-1$
             assertEquals(ownwer.getName(), "ownerDisplayName" + Integer.toString(commitCounter)); //$NON-NLS-1$
 
             PersonIdent committer = commit.getCommitterIdent();
             assertEquals(committer.getEmailAddress(), "committerName" + Integer.toString(commitCounter)); //$NON-NLS-1$
             assertEquals(committer.getName(), "committerDisplayName" + Integer.toString(commitCounter)); //$NON-NLS-1$
 
             commitCounter--;
         }
 
         assertEquals(commitCounter, 0);
 
         // Verify the tags
         List<Ref> tags = git.tagList().call();
         assertEquals(3, tags.size());
     }
 }
