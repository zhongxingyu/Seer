 package hudson.model;
 
 import hudson.scm.SubversionSCM;
 
 import java.io.File;
 
 /**
  * Tests for {@link SubversionSCM}. Requires a *nix machine with Subversion client and server installed.
  */
 public class SubversionSCMTest extends HudsonTestCase {
     public void testSubversionSCM() {
         FreeStyleProject project = new FreeStyleProject(Hudson.getInstance(), "test");
         File tempdir = createTempDir("svn");
         File svnrepo = new File(tempdir, "repo");
         svnrepo.mkdir();
         File svnwc = new File(tempdir, "wc");
         svnwc.mkdir();
         File projectDir = new File(svnwc, "project");
         File hello = new File(projectDir, "hello");
         exec(new String[] { "svnadmin", "create", svnrepo.getPath() });
         exec(new String[] { "svn", "co", "file://" + svnrepo.getPath(), svnwc.getPath() });
         exec(new String[] { "mkdir", projectDir.getPath() });
         exec(new String[] { "svn", "add", projectDir.getPath() });
         exec(new String[] { "svn", "commit", "-m", "newproject", projectDir.getPath() });
 
         project.setScm(new SubversionSCM(new String[] { "file://" + svnrepo + "/project" }, new String[] { "." }, true,
                 "user1", null));
         setCommand(project, "echo Hello World");
 
         Result result;
 
         result = build(project);
         assertSuccess(result);
 
         exec(new String[] { "touch", hello.getPath() });
         exec(new String[] { "svn", "add", hello.getPath() });
         exec(new String[] { "svn", "commit", "-m", "hello", hello.getPath() });
         result = build(project);
         assertSuccess(result);
 
         exec(new String[] { "svn", "up", projectDir.getPath() });
         exec(new String[] { "svn", "rm", projectDir.getPath() });
         exec(new String[] { "svn", "commit", "-m", "deleted", projectDir.getPath() });
         result = build(project);
         assertFailure(result);
     }
 }
