 /*
  * Copyright 2013 Indiana University
  * Mark H. Wood, IUPUI University Library, 25-Apr-2013
  */
 
 package edu.iupui.ulib.dspace;
 
 import java.io.File;
import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import org.apache.tools.ant.Project;
import org.apache.tools.ant.helper.ProjectHelper2;
 
 /**
  * Build an Ant project from an embedded build file.
  *
  * @author Mark H. Wood
  */
 public class App
 {
     public static void main( String[] args )
             throws URISyntaxException
     {
 	// Initialize an Ant project from the build file
 	Project project = new Project();
	//ProjectHelper.configureProject(project, getBuildFile());
        ProjectHelper2 projectHelper = new ProjectHelper2();
         URL buildResource = App.class.getResource("/build.xml");
        URI buildURI = buildResource.toURI(); // XXX throws URISyntaxException
        File buildFile = new File(buildURI);
        projectHelper.parse(project, buildFile);
 
 	// Configure the project
 	project.setProperty("ivySettings",
 			    App.class.getResource("/ivysettings.xml").toString());
 
 	// Select the target operation
 	String target = project.getDefaultTarget(); // TODO parameterize
 
 	// Install
 	project.executeTarget(target);
     }
 }
