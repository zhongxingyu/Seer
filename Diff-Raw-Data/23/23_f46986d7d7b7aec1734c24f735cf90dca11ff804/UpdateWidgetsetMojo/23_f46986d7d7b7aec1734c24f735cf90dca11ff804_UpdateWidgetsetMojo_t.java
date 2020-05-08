 package com.vaadin.integration.maven;
 
 import java.io.File;
 import java.util.Collection;
 
 import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Resource;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.codehaus.mojo.gwt.shell.AbstractGwtShellMojo;
 import org.codehaus.mojo.gwt.shell.CompileMojo;
 
 /**
  * Updates Vaadin widgetsets based on other widgetset packages on the classpath.
  * It is assumed that the project does not directly contain other GWT modules.
  * In part adapted from gwt-maven-plugin {@link CompileMojo}.
  *
  * @goal update-widgetset
  * @requiresDependencyResolution compile
  * @phase process-classes
  */
 public class UpdateWidgetsetMojo extends AbstractGwtShellMojo {
     public static final String WIDGETSET_BUILDER_CLASS = "com.vaadin.server.widgetsetutils.WidgetSetBuilder";
 
     public static final String GWT_MODULE_EXTENSION = ".gwt.xml";
 
     /**
      * {@inheritDoc}
      *
      * @see org.apache.maven.plugin.Mojo#execute()
      */
     @Override
     public final void doExecute() throws MojoExecutionException,
             MojoFailureException {
         if ("pom".equals(getProject().getPackaging())) {
             getLog().info("GWT compilation is skipped");
             return;
         }
 
         // compile one widgetset at a time
         String[] modules = getModules();
         if (modules.length > 0) {
             for (String module : modules) {
                 updateWidgetset(module);
             }
         } else {
             // ask the user to explicitly indicate the widgetset to create
             getLog().info("No widgetsets to update.");
             getLog().info(
                     "To create a widgetset, define a non-existing module in your pom.xml .");
         }
 
     }
 
     private void updateWidgetset(String module) throws MojoExecutionException {
         // class path order has "compile" sources first as it should
 
         getLog().info("Updating widgetset " + module);
 
         JavaCommand cmd = new JavaCommand(WIDGETSET_BUILDER_CLASS);
         // make sure source paths are first on the classpath to update the .gwt.xml there, not in target
        // Check src/main/resources first
		Collection<?> resources = getProject().getResources();
		if (null != resources) {
			for (Object resObj: resources) {
				Resource res = (Resource)resObj;
				File resourceDirectory = new File(res.getDirectory());
				if (resourceDirectory.exists()) {
					getLog().info("Adding resource directory to command classpath: " + resourceDirectory);
		            cmd.withinClasspath(resourceDirectory);
				} else {
					getLog().warn("Ignoring missing resource directory: " + resourceDirectory);
				}
			}
			
		}
        
         Collection<String> sourcePaths = getProject().getCompileSourceRoots();
         if (null != sourcePaths) {
             for (String sourcePath : sourcePaths) {
                 File sourceDirectory = new File(sourcePath);
                 if ( sourceDirectory.exists() ) {
                	getLog().info("Adding to cmd classpath: " + sourceDirectory);
                     cmd.withinClasspath(sourceDirectory);
                } else {
                	getLog().warn("Ignoring missing source directory: " + sourceDirectory);
                 }
             }
         }
         cmd.withinScope( Artifact.SCOPE_COMPILE );
         cmd.withinClasspath(getGwtUserJar()).withinClasspath(getGwtDevJar());
 
         cmd.arg(module);
         cmd.execute();
     }
 
 }
