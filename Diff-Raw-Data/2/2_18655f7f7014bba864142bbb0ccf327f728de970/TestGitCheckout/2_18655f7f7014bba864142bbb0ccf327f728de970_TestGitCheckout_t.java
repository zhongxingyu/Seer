 package edu.nyu.cs.javagit.api.commands;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import junit.framework.TestCase;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import edu.nyu.cs.javagit.api.JavaGitException;
 import edu.nyu.cs.javagit.api.Ref;
 import edu.nyu.cs.javagit.api.commands.GitAdd;
 import edu.nyu.cs.javagit.api.commands.GitAddOptions;
 import edu.nyu.cs.javagit.api.commands.GitCheckout;
 import edu.nyu.cs.javagit.api.commands.GitCheckoutOptions;
 import edu.nyu.cs.javagit.api.commands.GitCheckoutResponse;
 import edu.nyu.cs.javagit.api.commands.GitCommit;
 import edu.nyu.cs.javagit.test.utilities.FileUtilities;
 import edu.nyu.cs.javagit.test.utilities.HelperGitCommands;
 
 public class TestGitCheckout extends TestCase {
 
   private File repositoryDirectory;
   private String repositoryPath;
   private GitCommit gitCommit;
   private GitAdd gitAdd;
   private GitCheckout gitCheckout;
   private File file1;
   private File file2;
 
   @Before
   public void setUp() throws Exception {
     repositoryDirectory = FileUtilities.createTempDirectory("GitCheckoutTestRepository");
     HelperGitCommands.initRepo(repositoryDirectory);
     gitCommit = new GitCommit();
     gitAdd = new GitAdd();
     gitCheckout = new GitCheckout();
     file1 = FileUtilities.createFile(repositoryDirectory, "foobar01", "Sample Contents");
     file2 = FileUtilities.createFile(repositoryDirectory, "foobar02", "Sample Contents");
     repositoryPath = repositoryDirectory.getAbsolutePath();
     List<File> filesToAdd = new ArrayList<File>();
     filesToAdd.add(file1);
     filesToAdd.add(file2);
     GitAddOptions addOptions = new GitAddOptions();
     gitAdd.add(repositoryDirectory, addOptions, filesToAdd);
     gitCommit.commit(repositoryDirectory, "New Repository");
   }
 
   /**
    * Test for creating a new branch switching to it from base master branch
    * 
    * @throws IOException
    * @throws JavaGitException
    */
   @Test
   public void testCreatingNewBranchFromMaster() throws IOException, JavaGitException {
     GitCheckoutOptions options = new GitCheckoutOptions();
     options.setOptB(Ref.createBranchRef("testBranch"));
     Ref branch = Ref.createBranchRef("master");
     GitCheckoutResponse response = gitCheckout.checkout(repositoryDirectory, options, branch);
     assertEquals("New Branch created should be created with name- testBranch", "\"testBranch\"",
         response.getNewBranch().getName());
   }
 
   /**
    * Test for checking out a locally deleted file from the repository.
    * 
    * @throws JavaGitException
    * @throws IOException
    */
   @Test
   public void testCheckingOutLocalllyDeletedFiles() throws JavaGitException, IOException {
     List<File> filePaths = new ArrayList<File>();
     filePaths.add(new File(repositoryPath + File.separator + "foobar01"));
     if (file1.delete()) { // locally delete the file
       // check out the file from the repository after deletion
       GitCheckoutResponse response = gitCheckout.checkout(repositoryDirectory, filePaths);
       File checkedOutFile = new File(repositoryPath + File.separator + "foobar01");
       assertTrue(checkedOutFile.exists());
       FileUtilities.modifyFileContents(file2, "Test for append to a file");
       GitCheckoutOptions options = new GitCheckoutOptions();
       Ref branch = Ref.createBranchRef("master");
       response = gitCheckout.checkout(repositoryDirectory, options, branch);
       assertEquals("Modified File exists", 1, response.getNumberOfModifiedFiles());
     } else {
       fail("File delete failed");
     }
   }
 
   /**
    * Assert method for finding out if a file exists under a given directory.
    * 
    * @param repositoryDirectory
    * @param file
    */
   private void assertFileExistsInDirectory(File repositoryDirectory, File file) throws IOException {
     assertEquals(repositoryDirectory.getAbsolutePath(), file.getParent());
   }
 
   /**
    * Test for checking out a file from another branch. A new branch testBranch01 is created with
    * base as master, and a new test file foobar03 is created and committed to the repository in
    * testBranch01. Now another branch testBranch02 is created from master branch and file foobar03
    * should not be existing in current branch testBranch02. We checkout this file from testBranch01
    * and verify that the file exists after checking out from testBranch01.
    * 
    * @throws JavaGitException
    * @throws IOException
    */
   @Test
   public void testCheckingOutFileFromAnotherBranch() throws JavaGitException, IOException {
     // Create a testBranch01 from master
     GitCheckoutOptions options = new GitCheckoutOptions();
     Ref branch1 = Ref.createBranchRef("testBranch01");
     options.setOptB(branch1);
     Ref branch = Ref.createBranchRef("master");
     gitCheckout.checkout(repositoryDirectory, options, branch);
     File file3 = FileUtilities.createFile(repositoryDirectory, "foobar03",
         "Sample Contents foobar03\n");
     // add a file to testBranch01
     gitAdd.add(repositoryDirectory, null, file3);
     gitCommit.commit(repositoryDirectory, "Added foobar03 to the repository");
     assertFileExistsInDirectory(repositoryDirectory, file3);
     Ref master = Ref.createBranchRef("master");
     options = new GitCheckoutOptions();
     // Switch to new branch - testBranch02, base branch as master
     Ref newBranch = Ref.createBranchRef("testBranch02");
     options.setOptB(newBranch);
     gitCheckout.checkout(repositoryDirectory, options, master);
     // foobar03 does not exist in testBranch02 as it was created from master branch
     assertFalse(file3.exists());
     // checking out foobar03 file from branch - testBranch01
     gitCheckout.checkout(repositoryDirectory, null, branch1, file3);
     assertFileExistsInDirectory(repositoryDirectory, file3);
   }
 
   @After
   public void tearDown() throws Exception {
    FileUtilities.removeDirectoryRecursivelyAndForcefully(repositoryDirectory);
   }
 
 }
