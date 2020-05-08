 package net.awired.jstest.mojo;
 
 import java.io.File;
 import java.io.InputStream;
 import java.io.StringWriter;
 import java.util.ArrayList;
 import net.awired.jstest.mojo.inherite.AbstractJsTestMojo;
 import net.awired.jstest.resource.ResourceDirectory;
 import net.awired.jstest.resource.ResourceResolver;
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
  * @goal dev
  * @execute lifecycle="jstest-lifecycle" phase="generate-test-sources"
  * @requiresDirectInvocation true
  */
 public class DevMojo extends AbstractJsTestMojo {
 
     public static final String INSTRUCTION_FORMAT = "\n\n"
             + "You can run your tests as you develop by visiting this URL in a web browser: \n\n"
             + "  http://localhost:%s"
             + "\n\n"
             + "The server will monitor these two directories for scripts that you add, remove, and change:\n\n"
             + "  source directory: %s\n\n"
             + "  test directory: %s"
             + "\n\n"
             + "Leave this process running as you test-drive your code, refreshing your browser window to re-run tests.\n"
             + "You can kill the server with Ctrl-C when you're done.";
 
     @Override
     public void run() throws MojoExecutionException, MojoFailureException {
         JsTestServer jsTestServer = new JsTestServer(getLog(), getDevPort(), false);
         try {
             ResourceResolver resourceResolver = new ResourceResolver(getLog(), buildCurrentSrcDir(true),
                     buildTestResourceDirectory(), buildOverlaysResourceDirectories(),
                     new ArrayList<ResourceDirectory>());
             //TODO remove resultHandler creation we dont need it here
             ResultHandler resultHandler = new ResultHandler(getLog(), null, buildTestType(resourceResolver));
             
             JsTestHandler jsTestHandler = new JsTestHandler(resultHandler, getLog(), resourceResolver,
                     buildAmdRunnerType(), buildTestType(resourceResolver), true, getLog().isDebugEnabled(),
                     getAmdPreloads(), getTargetSourceDirectory());
             
             Handler[] handlers = new Handler[2];
             handlers[0] = jsTestHandler;
 
             if (isPackBeforeTest()) {
                 getLog().info("Package Started");
                Commandline cmdLine = new Commandline("mvn package -Pjava -DskipTests");
                 cmdLine.setWorkingDirectory(".");
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
 
             getLog().info(String.format(INSTRUCTION_FORMAT, getDevPort(), getSourceDir(), getTestDir()));
             jsTestServer.join();
         } catch (Exception e) {
             throw new RuntimeException("Cannot start Jstest server", e);
         } finally {
             jsTestServer.close();
         }
     }
 }
