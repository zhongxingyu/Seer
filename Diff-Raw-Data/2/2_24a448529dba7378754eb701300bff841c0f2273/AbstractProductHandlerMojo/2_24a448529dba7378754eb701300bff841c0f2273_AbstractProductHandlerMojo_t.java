 package com.atlassian.maven.plugins.amps;
 
 import com.atlassian.maven.plugins.amps.product.ProductHandler;
 import com.atlassian.maven.plugins.amps.product.ProductHandlerFactory;
 import com.atlassian.maven.plugins.amps.util.ArtifactRetriever;
 import org.apache.maven.artifact.factory.ArtifactFactory;
 import org.apache.maven.artifact.repository.ArtifactRepository;
 import org.apache.maven.artifact.resolver.ArtifactResolver;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.jfrog.maven.annomojo.annotations.MojoParameter;
 import org.jfrog.maven.annomojo.annotations.MojoComponent;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Base class for webapp mojos
  */
 public abstract class AbstractProductHandlerMojo extends AbstractProductHandlerAwareMojo
 {
     // ------ start inline product context
 
     /**
      * Container to run in
      */
     @MojoParameter(expression = "${container}", defaultValue = "tomcat6x")
     protected String containerId;
 
     /**
      * HTTP port for the servlet containers
      */
     @MojoParameter(expression = "${http.port}", defaultValue = "0")
     private int httpPort;
 
     /**
      * Application context path
      */
     @MojoParameter(expression = "${context.path}")
     protected String contextPath;
 
     /**
      * Application server
      */
     @MojoParameter(expression = "${server}", defaultValue = "localhost")
     protected String server;
 
     /**
      * Webapp version
      */
     @MojoParameter(expression = "${product.version}")
     protected String productVersion;
 
     /**
      * JVM arguments to pass to cargo
      */
     @MojoParameter(expression = "${jvmargs}")
     protected String jvmArgs;
 
     /**
      * A log4j properties file
      */
     @MojoParameter
     protected File log4jProperties;
 
     /**
      * The test resources version
      */
     @MojoParameter(expression = "${testResources.version}", defaultValue = "LATEST")
     protected String testResourcesVersion;
 
     /**
      */
     @MojoParameter
     private List<ProductArtifact> pluginArtifacts = new ArrayList<ProductArtifact>();
 
     /**
      */
     @MojoParameter
     private List<ProductArtifact> libArtifacts = new ArrayList<ProductArtifact>();
 
     /**
      */
     @MojoParameter
     private List<ProductArtifact> bundledArtifacts = new ArrayList<ProductArtifact>();
 
     /**
      * SAL version
      */
     @MojoParameter(expression = "${sal.version}")
     private String salVersion;
 
     /**
      * Atlassian Plugin Development Kit (PDK) version
      */
     @MojoParameter(expression = "${pdk.version}")
     private String pdkVersion;
 
     /**
      * Atlassian REST module version
      */
     @MojoParameter(expression = "${rest.version}")
     private String restVersion;
 
 
     // ---------------- end product context
 
     /**
      * Comma-delimited list of plugin artifacts in GROUP_ID:ARTIFACT_ID:VERSION form, where version can be
      * ommitted, defaulting to LATEST
      */
     @MojoParameter(expression = "${plugins}")
     private String pluginArtifactsString;
 
     /**
      * Comma-delimited list of lib artifacts in GROUP_ID:ARTIFACT_ID:VERSION form, where version can be
      * ommitted, defaulting to LATEST
      */
     @MojoParameter(expression = "${lib.plugins}")
     private String libArtifactsString;
 
     /**
      * Comma-delimited list of bundled plugin artifacts in GROUP_ID:ARTIFACT_ID:VERSION form, where version can be
      * ommitted, defaulting to LATEST
      */
     @MojoParameter(expression = "${bundled.plugins}")
     private String bundledArtifactsString;
 
     /**
      * The build directory
      */
     @MojoParameter(expression = "${project.build.directory}", required = true)
     protected File targetDirectory;
 
     /**
      * The jar name
      */
     @MojoParameter(expression = "${project.build.finalName}", required = true)
     protected String finalName;
 
     /**
      * The artifact resolver is used to dynamically resolve JARs that have to be in the embedded
      * container's classpaths. Another solution would have been to statitically define them a
      * dependencies in the plugin's POM. Resolving them in a dynamic manner is much better as only
      * the required JARs for the defined embedded container are downloaded.
      */
     @MojoComponent
     private ArtifactResolver artifactResolver;
 
     /**
      * The local Maven repository. This is used by the artifact resolver to download resolved
      * JARs and put them in the local repository so that they won't have to be fetched again next
      * time the plugin is executed.
      */
     @MojoParameter(expression = "${localRepository}")
     private ArtifactRepository localRepository;
 
     /**
      * The remote Maven repositories used by the artifact resolver to look for JARs.
      */
     @MojoParameter(expression = "${project.remoteArtifactRepositories}")
     private List repositories;
 
 
     /**
      * The artifact factory is used to create valid Maven
      * {@link org.apache.maven.artifact.Artifact} objects. This is used to pass Maven artifacts to
      * the artifact resolver so that it can download the required JARs to put in the embedded
      * container's classpaths.
      */
     @MojoComponent
     private ArtifactFactory artifactFactory;
 
     /**
      * A list of product-specific configurations
      */
     @MojoParameter
     private List<Product> products = new ArrayList<Product>();
 
     private Product createDefaultProductContext() throws MojoExecutionException
     {
         Product ctx = new Product();
         ctx.setId(getProductId());
         ctx.setContainerId(containerId);
         ctx.setServer(server);
         ctx.setContextPath(contextPath);
         ctx.setJvmArgs(jvmArgs);
         ctx.setBundledArtifacts(bundledArtifacts);
         ctx.setLibArtifacts(libArtifacts);
         ctx.setPluginArtifacts(pluginArtifacts);
         ctx.setLog4jProperties(log4jProperties);
         ctx.setTestResourcesVersion(testResourcesVersion);
         ctx.setHttpPort(httpPort);
         ctx.setArtifactRetriever(new ArtifactRetriever(artifactResolver, artifactFactory, localRepository, repositories));
 
         ctx.setRestVersion(restVersion);
         ctx.setSalVersion(salVersion);
         ctx.setPdkVersion(pdkVersion);
 
         ctx.setHttpPort(httpPort);
         ctx.setVersion(productVersion);
         ctx.setContextPath(contextPath);
         return ctx;
     }
 
     private List<ProductArtifact> stringToArtifactList(String val, List<ProductArtifact> artifacts)
     {
         if (val == null || val.trim().length() == 0)
         {
             return artifacts;
         }
 
         for (String ptn : val.split(","))
         {
             String[] items = ptn.split(":");
             if (items.length < 2 || items.length > 3)
             {
                 throw new IllegalArgumentException("Invalid artifact pattern: " + ptn);
             }
             String groupId = items[0];
             String artifactId = items[1];
             String version = (items.length == 3 ? items[2] : "LATEST");
             artifacts.add(new ProductArtifact(groupId, artifactId, version));
         }
         return artifacts;
     }
 
     public final void execute() throws MojoExecutionException, MojoFailureException
     {
         stringToArtifactList(pluginArtifactsString, pluginArtifacts);
         stringToArtifactList(libArtifactsString, libArtifacts);
         stringToArtifactList(bundledArtifactsString, bundledArtifacts);
 
         doExecute();
     }
 
     protected List<Product> getProductContexts(MavenGoals goals) throws MojoExecutionException
     {
         List<Product> list = new ArrayList<Product>(products);
         if (getProductId() != null)
         {
             list.add(0, createDefaultProductContext());
         }
 
         for (Product ctx : list)
         {
             ProductHandler handler = ProductHandlerFactory.create(ctx.getId(), getMavenContext().getProject(), goals);
             ctx.setHttpPort(ctx.getHttpPort() == 0 ? handler.getDefaultHttpPort() : ctx.getHttpPort());
             ctx.setVersion(ctx.getVersion() == null ? "RELEASE" : ctx.getVersion());
            ctx.setContextPath(ctx.getContextPath() == null ? "/" + handler.getId() : ctx.getContextPath());
         }
         return list;
     }
 
     protected abstract void doExecute() throws MojoExecutionException, MojoFailureException;
 }
