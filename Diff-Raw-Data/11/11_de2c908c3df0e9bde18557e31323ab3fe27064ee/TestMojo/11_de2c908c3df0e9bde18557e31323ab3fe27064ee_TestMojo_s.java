 package net.awired.jstest.mojo;
 
 import java.io.File;
 import java.io.InputStream;
 import java.io.StringWriter;
 import java.net.URL;
 import java.util.ArrayList;
 import net.awired.jstest.executor.RunnerExecutor;
 import net.awired.jstest.mojo.inherite.AbstractJsTestMojo;
 import net.awired.jstest.resource.ResourceDirectory;
 import net.awired.jstest.resource.ResourceResolver;
 import net.awired.jstest.result.RunResult;
 import net.awired.jstest.server.JsTestServer;
 import net.awired.jstest.server.handler.JsTestHandler;
 import net.awired.jstest.server.handler.ResultHandler;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.codehaus.plexus.util.cli.CommandLineException;
 import org.codehaus.plexus.util.cli.Commandline;
 import org.eclipse.jetty.server.Handler;
 import org.eclipse.jetty.server.handler.HandlerCollection;
 import org.eclipse.jetty.webapp.WebAppContext;
 
 /**
  * @component
  * @goal test
  * @phase test
  * @execute lifecycle="jstest-lifecycle" phase="process-test-resources""
  */
 public class TestMojo extends AbstractJsTestMojo {
 
     private static final String ERROR_MSG = "There are test failures.\n\nPlease refer to %s for the individual test results.";
 
     @Override
     public void run() throws MojoExecutionException, MojoFailureException {
         if (isSkipTests()) {
             getLog().info("Skipping JsTest");
             return;
         }
         
         JsTestServer jsTestServer = new JsTestServer(getLog(), getTestPort(), isTestPortFindFree());
         RunnerExecutor executor = null;
         try {
             ResourceResolver resourceResolver = new ResourceResolver(getLog(), buildCurrentSrcDir(false),
                     buildTestResourceDirectory(), buildOverlaysResourceDirectories(),
                     new ArrayList<ResourceDirectory>());
             ResultHandler resultHandler = new ResultHandler(getLog(), getPreparedReportDir(), buildTestType(resourceResolver));
             
             JsTestHandler jsTestHandler = new JsTestHandler(resultHandler, getLog(), resourceResolver,
                     buildAmdRunnerType(), buildTestType(resourceResolver), false, getLog().isDebugEnabled(),
                     getAmdPreloads(), getTargetSourceDirectory());
 
             Handler[] handlers = new Handler[2];
             handlers[0] = jsTestHandler;
 
             if (isPackBeforeTest()) {
                 getLog().info("Package Started");
                Commandline cmdLine = new Commandline("mvn package -DskipTests");
                 //cmdLine.setWorkingDirectory(".");
                 try {
                     Process process = cmdLine.execute();
                     InputStream inputStream = process.getInputStream();
                     StringWriter writer = new StringWriter();
                     IOUtils.copy(inputStream, writer);
                     String output = writer.toString();
                     if (output.contains("[INFO] BUILD SUCCESS")) {
                         getLog().info("Packaged successfully");
                         WebAppContext webAppContext = new WebAppContext();
                         webAppContext.setWar(getWarTargetDir());
 
                         //webAppContext.setDescriptor("WEB-INF/web.xml");
                         //webAppContext.setResourceBase("/Users/bharatkumarradha/Downloads/BT/bt/src/main/webapp");
 
                         webAppContext.setContextPath("/" + context);
                         webAppContext.setParentLoaderPriority(true);
                         handlers[1] = webAppContext;
                     } else {
                         getLog().info("Package Failed");
                     }
                 } catch (CommandLineException e1) {
                     throw new MojoFailureException("Do not package the application to test");
                 }
                
             }
 
             HandlerCollection handlerCollect = new HandlerCollection();
             handlerCollect.setHandlers(handlers);
             jsTestServer.startServer(handlerCollect);
 
             if (isEmulator()) {
                 executor = new RunnerExecutor();
                 executor.execute(new URL("http://localhost:" + getDevPort() + "/?emulator=true"));
             }
 
             // let browsers detect that server is back
             Thread.sleep(7000);
 
             if (!resultHandler.waitAllResult(10000, 1000)) {
                 throw new MojoFailureException("Do not receive all test results from clients");
             }
 
             RunResult buildAggregatedResult = resultHandler.getRunResults().buildAggregatedResult();
             if (buildAggregatedResult.findErrors() > 0 || buildAggregatedResult.findFailures() > 0) {
                 String message = String.format(ERROR_MSG, getPreparedReportDir());
                 if (isIgnoreFailure()) {
                     getLog().error(message);
                 } else {
                     throw new MojoFailureException(message);
                 }
             }
         } catch (MojoFailureException e) {
             throw e;
         } catch (Exception e) {
             throw new MojoExecutionException("JsTest execution failure", e);
         } finally {
             if (executor != null) {
                 executor.close();
             }
             jsTestServer.close();
         }
     }
 }
