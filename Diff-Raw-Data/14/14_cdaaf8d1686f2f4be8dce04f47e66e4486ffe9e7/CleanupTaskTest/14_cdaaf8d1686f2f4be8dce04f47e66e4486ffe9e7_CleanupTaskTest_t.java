 package net.codjo.test.release.task.cleanup;
 import net.codjo.test.common.Directory.NotDeletedException;
 import net.codjo.test.common.LogString;
 import net.codjo.test.common.PathUtil;
 import net.codjo.test.common.fixture.DirectoryFixture;
 import net.codjo.test.release.TestEnvironment;
 import net.codjo.test.release.TestEnvironmentMock;
 import net.codjo.test.release.task.util.RemoteCommandMock;
 import java.io.File;
 import java.io.FileInputStream;
 import org.apache.tools.ant.BuildException;
 import org.apache.tools.ant.Project;
 import org.junit.After;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 import org.junit.Before;
 import org.junit.Test;
import org.junit.runner.RunWith;
 
import com.googlecode.junit.ext.JunitExtRunner;
import com.googlecode.junit.ext.RunIf;
import com.googlecode.junit.ext.checkers.OSChecker;
 
@RunWith(JunitExtRunner.class)
 public class CleanupTaskTest {
     private CleanupTask cleanupTask;
     private Project project = new Project();
     private DirectoryFixture localDir;
     private static final String CLEANUP_SPECIFIC = "cleanupDirectory";
     private LogString log = new LogString();
 
 
     @Before
     public void setUp() throws NotDeletedException {
         project.addReference(TestEnvironment.class.getName(), new TestEnvironmentMock(project));
 
         cleanupTask = new CleanupTask();
         cleanupTask.setProject(project);
         cleanupTask.setDir(CLEANUP_SPECIFIC);
     }
 
 
     @Before
     public void setUpLocal() throws Exception {
         localDir = new DirectoryFixture(
               PathUtil.findTargetDirectory(getClass()).getPath() + "\\cleanup-local");
         localDir.doSetUp();
 
         project.setProperty(CLEANUP_SPECIFIC + ".local", localDir.getAbsolutePath());
     }
 
 
     @After
     public void tearDownLocal() throws Exception {
         localDir.doTearDown();
     }
 
 
     @Before
     public void setUpRemote() throws Exception {
         project.setProperty(CLEANUP_SPECIFIC + ".remote", "c:/root");
         project.setProperty(CleanupTask.REMOTE_SERVER, "localhost");
         project.setProperty(CleanupTask.REMOTE_USER, "user");
     }
 
 
     @Test
     public void test_deleteLocal() throws Exception {
         File file = new File(localDir, "cleanupTaskTest.txt");
         file.createNewFile();
         assertTrue(file.exists());
 
         cleanupTask.execute();
 
         assertFalse(file.exists());
     }
 
 
     @Test
     public void test_deleteRemote() throws Exception {
         project.setProperty("agf.test.remote", "YES");
         cleanupTask.setRemoteCommand(new RemoteCommandMock(log));
 
         cleanupTask.execute();
 
         log.assertContent("execute()");
     }
 
 
     @Test
     public void test_deleteRemote_ko() throws Exception {
         project.setProperty("agf.test.remote", "YES");
         RemoteCommandMock remoteCommandMock = new RemoteCommandMock();
         remoteCommandMock.mockFailure();
         cleanupTask.setRemoteCommand(remoteCommandMock);
 
         try {
             cleanupTask.execute();
             fail();
         }
         catch (Exception e) {
             assertEquals("Impossible de supprimer les fichiers", e.getMessage());
         }
     }
 
 
     @Test
     public void test_notSpecifiedDir() throws Exception {
         cleanupTask.setDir(null);
         try {
             cleanupTask.execute();
             fail();
         }
         catch (BuildException exception) {
             assertEquals("L'attribut 'dir' est obligatoire et n'est pas spcifi.",
                          exception.getLocalizedMessage());
         }
     }
 
 
     @Test
     public void test_invalidLocalDir() throws Exception {
         cleanupTask.setDir("bidon");
         try {
             cleanupTask.execute();
             fail();
         }
         catch (BuildException exception) {
             assertEquals("manque argument bidon.local dans le fichier de configuration",
                          exception.getLocalizedMessage());
         }
     }
 
 
     @Test
     public void test_invalidRemoteDir() throws Exception {
         project.setProperty("agf.test.remote", "YES");
         cleanupTask.setDir("bidon");
         try {
             cleanupTask.execute();
             fail();
         }
         catch (BuildException exception) {
             assertEquals("manque argument bidon.remote dans le fichier de configuration",
                          exception.getLocalizedMessage());
         }
     }
 
 
     @Test
     public void test_unexistingLocalDir() throws Exception {
         String invalidDirectory = "c:\\invalid\\directory";
         project.setProperty(CLEANUP_SPECIFIC + ".local", invalidDirectory);
         cleanupTask.execute();
     }
 
 
    /**
     * Since only Windows is locking files, ignore that test on other OSes.
     * @throws Exception
     */
     @Test
    @RunIf(value = OSChecker.class, arguments = OSChecker.WINDOWS)
     public void test_couldNotDeleteLocalFile() throws Exception {
         File file = new File(localDir, "lockedFile.txt");
         file.createNewFile();
         FileInputStream inputStream = new FileInputStream(file);
 
         try {
             cleanupTask.execute();
             fail();
         }
         catch (BuildException exception) {
             assertEquals("Impossible de supprimer le fichier " + file.getAbsolutePath(),
                          exception.getLocalizedMessage());
         }
         finally {
             inputStream.close();
         }
     }
 }
