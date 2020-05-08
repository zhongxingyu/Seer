 package com.atlassian.maven.plugins.amps;
 
 import com.atlassian.maven.plugins.amps.product.ProductHandler;
 import org.apache.commons.io.IOUtils;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.artifact.Artifact;
 import org.jfrog.maven.annomojo.annotations.MojoExecute;
 import org.jfrog.maven.annomojo.annotations.MojoGoal;
 import org.jfrog.maven.annomojo.annotations.MojoParameter;
 import org.jfrog.maven.annomojo.annotations.MojoRequiresDependencyResolution;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 
 import static org.apache.commons.lang.StringUtils.isBlank;
 
 /**
  * Run the webapp
  */
 @MojoGoal ("run")
 @MojoExecute (phase = "package")
 @MojoRequiresDependencyResolution
 public class RunMojo extends AbstractProductHandlerMojo
 {
     private static final char CONTROL_C = (char) 27;
 
     @MojoParameter (expression = "${wait}", defaultValue = "true")
     private boolean wait;
 
     /**
      * Whether or not to write properties used by the plugin to amps.properties.
      */
     @MojoParameter (expression = "${amps.properties}", required = true, defaultValue = "false")
     protected boolean writePropertiesToFile;
 
     /**
      * Instance id to run.  If provided, used to determine the product to run instead of just the product ID.
      */
     @MojoParameter(expression = "${instanceId}")
     protected String instanceId;
     
     /**
      * The properties actually used by the mojo when running
      */
     protected final Map<String, String> properties = new HashMap<String, String>();
 
     protected void doExecute() throws MojoExecutionException, MojoFailureException
     {
         final MavenGoals goals = getMavenGoals();
         Product ctx;
        if (!isBlank(instanceId))
         {
             ctx = getProductContexts(goals).get(instanceId);
             if (ctx == null)
             {
                 throw new MojoExecutionException("No product with instance ID '" + instanceId + "'");
             }
         }
         else
         {
             ctx = getProductContexts(goals).get(getProductId());
         }
         ProductHandler product = createProductHandler(ctx.getId());
 
         ctx.setInstallPlugin(shouldInstallPlugin());
 
         int actualHttpPort = product.start(ctx);
 
         getLog().info(ctx.getInstanceId() + " started successfully and available at http://localhost:" + actualHttpPort + ctx.getContextPath());
 
         if (writePropertiesToFile)
         {
             properties.put("http.port", String.valueOf(actualHttpPort));
             properties.put("context.path", ctx.getContextPath());
             writePropertiesFile();
         }
 
         if (wait)
         {
             getLog().info("Type CTRL-C to exit");
             try
             {
                 while (System.in.read() != CONTROL_C)
                 {
                 }
             }
             catch (final IOException e)
             {
                 // ignore
             }
         }
     }
 
     /**
      * Only install a plugin if the installPlugin flag is true and the project is a jar
      */
     private boolean shouldInstallPlugin()
     {
         Artifact artifact = getMavenContext().getProject().getArtifact();
         return installPlugin &&
                 (artifact != null && !"pom".equalsIgnoreCase(artifact.getType()));
     }
 
     private void writePropertiesFile() throws MojoExecutionException
     {
         final Properties props = new Properties();
 
         for (Map.Entry<String, String> entry : properties.entrySet())
         {
             props.setProperty(entry.getKey(), entry.getValue());
         }
 
         final File ampsProperties = new File(getMavenContext().getProject().getBuild().getDirectory(), "amps.properties");
         OutputStream out = null;
         try
         {
             out = new FileOutputStream(ampsProperties);
             props.store(out, "");
         }
         catch (IOException e)
         {
             throw new MojoExecutionException("Error writing " + ampsProperties.getAbsolutePath(), e);
         }
         finally
         {
             IOUtils.closeQuietly(out);
         }
     }
 }
