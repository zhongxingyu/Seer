 package com.atlassian.maven.plugins.amps;
 
 import com.atlassian.maven.plugins.amps.product.ProductHandler;
 import com.atlassian.maven.plugins.amps.product.ProductHandlerFactory;
 import org.apache.maven.artifact.handler.manager.ArtifactHandlerManager;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.project.MavenProject;
 import org.jfrog.maven.annomojo.annotations.MojoComponent;
 import org.jfrog.maven.annomojo.annotations.MojoGoal;
 import org.jfrog.maven.annomojo.annotations.MojoParameter;
 import org.jfrog.maven.annomojo.annotations.MojoRequiresDependencyResolution;
 
 import java.io.File;
 import java.util.*;
 
 /**
  * Run the integration tests against the webapp
  */
 @MojoGoal("integration-test")
 @MojoRequiresDependencyResolution("test")
 public class IntegrationTestMojo extends AbstractProductHandlerMojo
 {
     /**
      * Pattern for to use to find integration tests.  Only used if no test groups are defined.
      */
     @MojoParameter(expression = "${functional.test.pattern}")
     private String functionalTestPattern = "it/**";
 
     /**
      * The directory containing generated test classes of the project being tested.
      */
     @MojoParameter(expression = "${project.build.testOutputDirectory}", required = true)
     private File testClassesDirectory;
 
     /**
      * The list of specific test groups to execute
      */
     @MojoParameter
     private List<TestGroup> testGroups = new ArrayList<TestGroup>();
 
     /**
      * A comma separated list of test groups to run.  If not specified, all
      * test groups are run.
      */
     @MojoParameter(expression = "${testGroups}")
     private String configuredTestGroupsToRun;
     
     /**
      * Whether the reference application will not be started or not
      */
     @MojoParameter(expression = "${no.webapp}", defaultValue = "false")
     private boolean noWebapp = false;
 
     @MojoComponent
     private ArtifactHandlerManager artifactHandlerManager;
 
     @MojoParameter(expression="${maven.test.skip}", defaultValue = "false")
     private boolean testsSkip = false;
 
     @MojoParameter(expression="${skipTests}", defaultValue = "false")
     private boolean skipTests = false;
 
     private static final String NO_TEST_GROUP = "__no_test_group__";
     protected void doExecute() throws MojoExecutionException
     {
         final MavenProject project = getMavenContext().getProject();
 
         // workaround for MNG-1682/MNG-2426: force maven to install artifact using the "jar" handler
         project.getArtifact().setArtifactHandler(artifactHandlerManager.getArtifactHandler("jar"));
 
         if (!new File(testClassesDirectory, "it").exists())
         {
             getLog().info("No integration tests found");
             return;
         }
 
         if (skipTests || testsSkip)
         {
             getLog().info("Integration tests skipped");
             return;
         }
 
         final MavenGoals goals = getMavenGoals();
         final String pluginJar = targetDirectory.getAbsolutePath() + "/" + finalName + ".jar";
 
         final Set<String> configuredTestGroupIds = getTestGroupIds();
         if (configuredTestGroupIds.isEmpty())
         {
             runTestsForTestGroup(NO_TEST_GROUP, goals, pluginJar, copy(systemPropertyVariables));
         }
         else if (configuredTestGroupsToRun != null)
         {
             String[] testGroupIdsToRun = configuredTestGroupsToRun.split(",");
             
             // fail fast if one of the test groups does not exist
             for (String testGroupId : testGroupIdsToRun)
             {
                 if (!configuredTestGroupIds.contains(testGroupId))
                 {
                     throw new MojoExecutionException("Test group " + testGroupId + " does not exist");
                 }
             }
             // now run the tests
             for (String testGroupId : testGroupIdsToRun)
             {
                 runTestsForTestGroup(testGroupId, goals, pluginJar, copy(systemPropertyVariables));
             }
         }
         else
         {
             for (String testGroupId : configuredTestGroupIds)
             {
                 runTestsForTestGroup(testGroupId, goals, pluginJar, copy(systemPropertyVariables));
             }
         }
     }
 
     private Map<String,Object> copy(Map<String,Object> systemPropertyVariables)
     {
         return new HashMap<String,Object>(systemPropertyVariables);
     }
 
     /**
      * Returns product-specific properties to pass to the container during
      * integration testing. Default implementation does nothing.
      * @param product the {@code Product} object to use
      * @return a {@code Map} of properties to add to the system properties passed
      * to the container
      */
     protected Map<String, String> getProductFunctionalTestProperties(Product product)
     {
         return Collections.emptyMap();
     }
 
     private Set<String> getTestGroupIds() throws MojoExecutionException
     {
         Set<String> ids = new HashSet<String>();
 
         //ids.addAll(ProductHandlerFactory.getIds());
         for (TestGroup group : testGroups)
         {
             ids.add(group.getId());
         }
 
         return ids;
     }
 
     private List<String> getProductIdsForTestGroup(String testGroupId) throws MojoExecutionException
     {
         List<String> productIds = new ArrayList<String>();
         if (NO_TEST_GROUP.equals(testGroupId))
         {
             productIds.add(getProductId());
         }
 
         for (TestGroup group : testGroups)
         {
             if (group.getId().equals(testGroupId))
             {
                 productIds.addAll(group.getProductIds());
             }
         }
         if (ProductHandlerFactory.getIds().contains(testGroupId) && !productIds.contains(testGroupId))
         {
             productIds.add(testGroupId);
         }
 
         if (productIds.isEmpty())
         {
            throw new MojoExecutionException("Unknown test group id");
         }
 
         return productIds;
     }
 
     private void runTestsForTestGroup(String testGroupId, MavenGoals goals, String pluginJar, Map<String,Object> systemProperties) throws MojoExecutionException
     {
         List<String> includes = getIncludesForTestGroup(testGroupId);
         List<String> excludes = getExcludesForTestGroup(testGroupId);
         List<String> productIds = getProductIdsForTestGroup(testGroupId);
 
         // Create a container object to hold product-related stuff
         List<TestGroupProductExecution> products = new ArrayList<TestGroupProductExecution>();
         int dupCounter = 0;
         Set<String> uniqueProductIds = new HashSet<String>();
         for (String productId : productIds)
         {
             Product ctx = getProductContexts(goals).get(productId);
             if (ctx == null)
             {
                 throw new MojoExecutionException("The test group '" + testGroupId + "' refers to a product '" + productId
                     + "' that doesn't have an associated <product> configuration.");
             }
             ProductHandler productHandler = ProductHandlerFactory.create(ctx.getId(), getMavenContext().getProject(), goals, getLog());
 
             // Give unique ids to duplicate product instances
             if (uniqueProductIds.contains(productId))
             {
                 ctx.setInstanceId(productId + "-" + dupCounter++);
             }
             else
             {
                 uniqueProductIds.add(productId);
             }
             products.add(new TestGroupProductExecution(ctx, productHandler));
         }
 
         // Install the plugin in each product and start it
         for (TestGroupProductExecution testGroupProductExecution : products)
         {
             ProductHandler productHandler = testGroupProductExecution.getProductHandler();
             Product product = testGroupProductExecution.getProduct();
             product.setInstallPlugin(installPlugin);
 
             int actualHttpPort = 0;
             if (!noWebapp)
             {
                 actualHttpPort = productHandler.start(product);
             }
 
             if (products.size() == 1)
             {
                 systemProperties.put("http.port", String.valueOf(actualHttpPort));
                 systemProperties.put("context.path", product.getContextPath());
             }
 
             String baseUrl = MavenGoals.getBaseUrl(product.getServer(), actualHttpPort, product.getContextPath());
             // hard coded system properties...
             systemProperties.put("http." + product.getInstanceId() + ".port", String.valueOf(actualHttpPort));
             systemProperties.put("context." + product.getInstanceId() + ".path", product.getContextPath());
             systemProperties.put("http." + product.getInstanceId() + ".url", MavenGoals.getBaseUrl(product.getServer(), actualHttpPort, product.getContextPath()));
 
             systemProperties.put("baseurl." + product.getInstanceId(), baseUrl);
             systemProperties.put("plugin.jar", pluginJar);
 
             // yes, this means you only get one base url if multiple products, but that is what selenium would expect
             if (!systemProperties.containsKey("baseurl"))
             {
                 systemProperties.put("baseurl", baseUrl);
             }
             
             systemProperties.put("homedir." + product.getInstanceId(), productHandler.getHomeDirectory(product).getAbsolutePath());
             if (!systemProperties.containsKey("homedir"))
             {
                 systemProperties.put("homedir", productHandler.getHomeDirectory(product).getAbsolutePath());
             }
             
             systemProperties.putAll(getProductFunctionalTestProperties(product));
         }
         systemProperties.put("testGroup", testGroupId);
         systemProperties.putAll(getTestGroupSystemProperties(testGroupId));
 
         // Actually run the tests
         goals.runTests("group-" + testGroupId, containerId, includes, excludes, systemProperties, targetDirectory);
 
         // Shut all products down
         for (TestGroupProductExecution testGroupProductExecution : products)
         {
             ProductHandler productHandler = testGroupProductExecution.getProductHandler();
             Product product = testGroupProductExecution.getProduct();
             if (!noWebapp)
             {
                 productHandler.stop(product);
             }
         }
     }
 
     private Map<String, String> getTestGroupSystemProperties(String testGroupId)
     {
         if (NO_TEST_GROUP.equals(testGroupId))
         {
             return Collections.emptyMap();
         }
 
         for (TestGroup group : testGroups)
         {
             if (group.getId().equals(testGroupId))
             {
                 return group.getSystemProperties();
             }
         }
         return Collections.emptyMap();
     }
 
     private List<String> getIncludesForTestGroup(String testGroupId)
     {
         if (NO_TEST_GROUP.equals(testGroupId))
         {
             return Collections.singletonList(functionalTestPattern);
         }
         else
         {
             for (TestGroup group : testGroups)
             {
                 if (group.getId().equals(testGroupId))
                 {
                     return group.getIncludes();
                 }
             }
         }
         return Collections.singletonList(functionalTestPattern);
     }
 
 
     private List<String> getExcludesForTestGroup(String testGroupId)
     {
         if (NO_TEST_GROUP.equals(testGroupId))
         {
             return Collections.emptyList();
         }
         else
         {
             for (TestGroup group : testGroups)
             {
                 if (group.getId().equals(testGroupId))
                 {
                     return group.getExcludes();
                 }
             }
         }
         return Collections.emptyList();
     }
 
     /**
      * The execution context for a product in a test group
      */
     private static class TestGroupProductExecution
     {
         private final Product product;
         private final ProductHandler productHandler;
 
         public TestGroupProductExecution(Product product, ProductHandler productHandler)
         {
             this.product = product;
             this.productHandler = productHandler;
         }
 
         public ProductHandler getProductHandler()
         {
             return productHandler;
         }
 
         public Product getProduct()
         {
             return product;
         }
     }
 }
