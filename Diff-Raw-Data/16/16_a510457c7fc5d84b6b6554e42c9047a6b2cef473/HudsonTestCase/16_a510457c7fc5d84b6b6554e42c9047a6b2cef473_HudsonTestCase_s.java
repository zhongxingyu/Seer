 package hudson.model;
 
 import hudson.tasks.Builder;
 import hudson.tasks.Shell;
 
 import java.io.File;
 import java.io.IOException;
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.Arrays;
 import java.util.List;
 
 import javax.servlet.ServletContext;
 
 import org.apache.tools.ant.taskdefs.ExecTask;
 import org.apache.tools.ant.taskdefs.Execute;
 import org.apache.tools.ant.types.Commandline;
 import org.apache.tools.ant.util.StringUtils;
 import org.easymock.EasyMock;
 
 import junit.framework.TestCase;
 
 public class HudsonTestCase extends TestCase {
     Hudson hudson;
 
     @Override
     protected void setUp() throws Exception {
         hudson = newHudson();
 
         // Limit to 1 executor
         setNumExecutors(1);
     }
 
     protected void setNumExecutors(int i) {
         // FIXME would be nice to have a Hudson.setNumExecutors() method
         Field numExecutors;
         try {
             numExecutors = Hudson.class.getDeclaredField("numExecutors");
             numExecutors.setAccessible(true);
             numExecutors.set(hudson, 1);
             Method updateComputerList = Hudson.class.getDeclaredMethod("updateComputerList", new Class[] {});
             updateComputerList.setAccessible(true);
             updateComputerList.invoke(hudson, new Object[] {});
         } catch (Exception e) {
             throw new RuntimeException(e);
         }
     }
 
     private Hudson newHudson() {
         // FIXME does Hudson really need a ServletContext?
         try {
             return new Hudson(createTempDir("hudson"), EasyMock.createMock(ServletContext.class));
         } catch (IOException e) {
             throw new RuntimeException(e);
         }
     }
 
     /**
      * Create a new temporary directory
      */
     protected File createTempDir(String prefix) {
         File dir;
         try {
             dir = File.createTempFile(prefix, null);
             dir.delete();
             dir.mkdir();
             return dir;
         } catch (IOException e) {
             throw new RuntimeException(e);
         }
     }
 
     /**
      * Schedules a build and waits for completion
      * 
      * @param project
      *            the project to build
     * @return build result
      * @throws Exception
      */
     protected Result build(Project project) {
         try {
             int numbuilds = project.getBuilds().size() + 1;
            project.scheduleBuild();
             while (project.getBuilds().size() != numbuilds || project.getBuildByNumber(numbuilds).isBuilding()) {
                 Thread.sleep(100);
             }
             Build build = ((Build) project.getBuildByNumber(numbuilds));
             System.out.println(build.getLog());
             return build.getResult();
         } catch (Exception e) {
             throw new RuntimeException(e);
         }
     }
 
     /**
      * Clears the builders for the given project and add a Shell builder with specified command
      * 
      * @param project
      *            project to build
      * @param command
      *            command to run in shell
      * @throws Exception
      */
     protected void setCommand(FreeStyleProject project, String command) {
         // FIXME would be nice to be able to set the builders programmatically
         Field buildersField;
         try {
             buildersField = Project.class.getDeclaredField("builders");
             buildersField.setAccessible(true);
             List<Builder> builders = ((List<Builder>) buildersField.get(project));
             builders.clear();
             builders.add(new Shell(command));
         } catch (Exception e) {
             throw new RuntimeException(e);
         }
     }
     protected void assertSuccess(Result result) {
         assertTrue("Expected SUCCESS, got " + result.toString(), result.equals(Result.SUCCESS));
     }
     protected void assertFailure(Result result) {
         assertTrue("Expected FAILURE, got " + result.toString(), result.equals(Result.FAILURE));
     }
     void exec(String[] args) {
         Execute exec = new Execute();
         exec.setCommandline(args);
         int status;
         try {
             status = exec.execute();
         } catch (IOException e) {
             throw new RuntimeException(e);
         }
         if (status != 0)
             throw new RuntimeException("Command returned status " + status + ": " + Arrays.asList(args));
     }
 }
