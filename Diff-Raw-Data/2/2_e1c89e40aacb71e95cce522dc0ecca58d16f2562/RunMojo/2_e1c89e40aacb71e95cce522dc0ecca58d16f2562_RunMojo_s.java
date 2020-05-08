 package com.atlassian.maven.plugins.amps;
 
 import com.atlassian.maven.plugins.amps.product.ProductHandler;
 import com.google.common.base.Predicate;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 import org.apache.commons.io.IOUtils;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.surefire.shade.org.apache.commons.lang.StringUtils;
 import org.apache.maven.artifact.Artifact;
 import org.jfrog.maven.annomojo.annotations.MojoExecute;
 import org.jfrog.maven.annomojo.annotations.MojoGoal;
 import org.jfrog.maven.annomojo.annotations.MojoParameter;
 import org.jfrog.maven.annomojo.annotations.MojoRequiresDependencyResolution;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.NoSuchElementException;
 import java.util.Properties;
 import java.util.concurrent.TimeUnit;
 
 import static org.apache.commons.lang.StringUtils.isBlank;
 
 /**
  * Run the webapp
  */
 @MojoGoal ("run")
 @MojoExecute (phase = "package")
 @MojoRequiresDependencyResolution
 public class RunMojo extends AbstractTestGroupsHandlerMojo
 {
     @MojoParameter (expression = "${wait}", defaultValue = "true")
     private boolean wait;
 
     /**
      * Whether or not to write properties used by the plugin to amps.properties.
      */
     @MojoParameter (expression = "${amps.properties}", required = true, defaultValue = "false")
     protected boolean writePropertiesToFile;
 
     /**
      * Test group to run.  If provided, used to determine the products to run.
      */
     @MojoParameter(expression = "${testGroup}")
     protected String testGroup;
 
     /**
      * Excluded instances from the execution. Useful when Studio brings in all instances and you want to run only one.
      * List of comma separated instanceIds, or *{@literal}/instanceId to exclude all but one product.
      * <p>Examples:
      * <ul><li>mvn amps:run -Dexclude=studio-crowd</li>
      * <li>mvn amps:run -Dexclude=*{@literal}/studio-crowd to run only StudioCrowd</li>
      * </ul>
      */
     @MojoParameter(expression = "${exclude}")
     protected String exclude;
 
     /**
      * The properties actually used by the mojo when running
      */
     protected final Map<String, String> properties = new HashMap<String, String>();
 
     protected void doExecute() throws MojoExecutionException, MojoFailureException
     {
        getGoogleTracker().track(GoogleAmpsTracker.RUN);
 
         final List<ProductExecution> productExecutions = getProductExecutions();
 
         startProducts(productExecutions);
     }
 
     protected void startProducts(List<ProductExecution> productExecutions) throws MojoExecutionException
     {
         List<StartupInformation> successMessages = Lists.newArrayList();
         for (ProductExecution productExecution : productExecutions)
         {
             final ProductHandler productHandler = productExecution.getProductHandler();
             final Product product = productExecution.getProduct();
             if (product.isInstallPlugin() == null)
             {
                 product.setInstallPlugin(shouldInstallPlugin());
             }
 
             // Leave a blank line and say what it's doing
             getLog().info("");
             if (StringUtils.isNotBlank(product.getOutput()))
             {
                 getLog().info(String.format("Starting %s... (see log at %s)", product.getInstanceId(), product.getOutput()));
             }
             else
             {
                 getLog().info(String.format("Starting %s...", product.getInstanceId()));
             }
 
 
             // Actually start the product
             long startTime = System.nanoTime();
             int actualHttpPort = productHandler.start(product);
             long durationSeconds = TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - startTime);
 
             // Log the success message
             StartupInformation message = new StartupInformation(product, "started successfully", actualHttpPort, durationSeconds);
             getLog().info(message.toString());
             successMessages.add(message);
 
             if (writePropertiesToFile)
             {
                 if (productExecutions.size() == 1)
                 {
                     properties.put("http.port", String.valueOf(actualHttpPort));
                     properties.put("context.path", product.getContextPath());
                 }
 
                 properties.put("http." + product.getInstanceId() + ".port", String.valueOf(actualHttpPort));
                 properties.put("context." + product.getInstanceId() + ".path", product.getContextPath());
             }
         }
 
         if (writePropertiesToFile)
         {
             writePropertiesFile();
         }
 
         // Repeat the messages at the end, because we're developer-friendly
         if (successMessages.size() > 1)
         {
             getLog().info("");
             getLog().info("=== Summary:");
             // First show the log files
             for (StartupInformation message : successMessages)
             {
                 if (StringUtils.isNotBlank(message.getOutput()))
                 {
                     getLog().info("Log available at: " + message.getOutput());
                 }
             }
             // Then show the applications
             for (StartupInformation message : successMessages)
             {
                 getLog().info(message.toString());
             }
         }
 
         if (wait)
         {
             getLog().info("Type CTRL-D to shutdown gracefully");
             getLog().info("Type CTRL-C to exit");
             try
             {
                 while (System.in.read() != -1)
                 {
                 }
             }
             catch (final IOException e)
             {
                 // ignore
             }
 
             // We don't stop products when -Dwait=false, because some projects rely on the
             // application running after the end of the RunMojo goal. The SHITTY tests
             // check this behaviour.
             stopProducts(productExecutions);
         }
     }
 
     protected List<ProductExecution> getProductExecutions() throws MojoExecutionException
     {
         final List<ProductExecution> productExecutions;
         final MavenGoals goals = getMavenGoals();
         if (!isBlank(testGroup))
         {
             productExecutions = getTestGroupProductExecutions(testGroup);
         }
         else if (!isBlank(instanceId))
         {
             Product ctx = getProductContexts(goals).get(instanceId);
             if (ctx == null)
             {
                 throw new MojoExecutionException("No product with instance ID '" + instanceId + "'");
             }
             ProductHandler product = createProductHandler(ctx.getId());
             productExecutions = Collections.singletonList(new ProductExecution(ctx, product));
         }
         else
         {
             Product ctx = getProductContexts(goals).get(getProductId());
             ProductHandler product = createProductHandler(ctx.getId());
             productExecutions = Collections.singletonList(new ProductExecution(ctx, product));
         }
         return filterExcludedInstances(includeStudioDependentProducts(productExecutions, goals));
     }
 
     private List<ProductExecution> filterExcludedInstances(List<ProductExecution> executions) throws MojoExecutionException
     {
         if (StringUtils.isBlank(exclude))
         {
             return executions;
         }
         boolean inverted = exclude.startsWith("*/");
         String instanceIdList = inverted ? exclude.substring(2) : exclude;
 
         // Parse the list given by the user and find ProductExecutions
         List<String> excludedInstanceIds = Lists.newArrayList(instanceIdList.split(","));
         List<ProductExecution> excludedExecutions = Lists.newArrayList();
         for (final String instanceId : excludedInstanceIds)
         {
             try
             {
                 excludedExecutions.add(Iterables.find(executions, new Predicate<ProductExecution>()
                 {
                     @Override
                     public boolean apply(ProductExecution input)
                     {
                         return input.getProduct().getInstanceId().equals(instanceId);
                     }
                 }));
             }
             catch (NoSuchElementException nsee)
             {
                 throw new MojoExecutionException("You specified -Dexclude=" + exclude + " but " + instanceId + " is not an existing instance id.");
             }
         }
 
         if (inverted)
         {
             return excludedExecutions;
         }
         else
         {
             executions.removeAll(excludedExecutions);
             return executions;
         }
     }
 
     /**
      * Only install a plugin if the installPlugin flag is true and the project is a jar.  If the test plugin was built,
      * it will be installed as well.
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
 
     /**
      * Wraps information about the startup of a product
      */
     private static class StartupInformation
     {
         int actualHttpPort;
         long durationSeconds;
         Product product;
         String event;
 
         public StartupInformation(Product product, String event, int actualHttpPort, long durationSeconds)
         {
             super();
             this.actualHttpPort = actualHttpPort;
             this.product = product;
             this.event = event;
             this.durationSeconds = durationSeconds;
         }
 
         @Override
         public String toString()
         {
             String message = String.format("%s %s in %ds", product.getInstanceId(), event, durationSeconds);
             if (actualHttpPort != 0)
             {
                 message += " at http://localhost:" + actualHttpPort + product.getContextPath();
             }
             return message;
         }
 
         /**
          * @return the output of the product
          */
         public String getOutput()
         {
             return product.getOutput();
         }
 
     }
 }
