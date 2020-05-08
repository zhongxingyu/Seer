 package com.atlassian.maven.plugins.amps;
 
 import com.atlassian.maven.plugins.amps.product.ProductHandler;
 import com.atlassian.maven.plugins.amps.product.ProductHandlerFactory;
 import com.atlassian.maven.plugins.amps.product.studio.StudioProductHandler;
 import com.google.common.collect.Lists;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.jfrog.maven.annomojo.annotations.MojoGoal;
 import org.jfrog.maven.annomojo.annotations.MojoParameter;
 
 import java.io.File;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Creates a zip file containing the previous run's home directory
  * in the proper format to use as test-resources.
  *
  * @since 3.1-m3
  */
 @MojoGoal("create-home-zip")
 public class CreateHomeZipMojo extends AbstractProductHandlerMojo {
 
     /**
      * Generated home directory zip file.
      */
     @MojoParameter(expression = "${homeZip}", required = false)
    protected File homeZip;
 
     public void doExecute() throws MojoExecutionException, MojoFailureException
     {
         Product product = getProduct(instanceId);
         ProductHandler productHandler = createProductHandler(product.getId());
 
         if (ProductHandlerFactory.STUDIO.equals(product.getId()))
         {
             configureStudio(product, (StudioProductHandler) productHandler);
         }
 
         final File snapshotDir = productHandler.getSnapshotDirectory(product);
        if (homeZip == null)
         {
            homeZip = new File(productHandler.getBaseDirectory(product), "generated-test-resources.zip");
         }
 
        productHandler.createHomeZip(snapshotDir, homeZip, product);

        getLog().info("Home directory zip created successfully at " + homeZip.getAbsolutePath());

        // Make the file the artifact of the project
        getMavenGoals().attachArtifact(homeZip, "zip");
 
     }
 
 
 
 
     /**
      * Configure the Studio product.
      *
      * @param studioProduct the studio product. Must not be another product, neither null.
      * @param studioProductHandler the Studio product handler
      */
     private void configureStudio(Product studioProduct, StudioProductHandler studioProductHandler) throws MojoExecutionException
     {
         List<ProductExecution> executions = Lists.newArrayList(new ProductExecution(studioProduct, studioProductHandler));
         includeStudioDependentProducts(executions, getMavenGoals());
     }
 
     private Product getProduct(final String instanceId) throws MojoExecutionException
     {
         Map<String, Product> contexts = getProductContexts(getMavenGoals());
 
         Product product = contexts.get(instanceId);
         if (product == null)
         {
             throw new MojoExecutionException("There is no instance with name " + instanceId + " defined in the pom.xml");
         }
         return product;
     }
 }
