 /*
  * Copyright 2013 Indiana University
  * Mark H. Wood, IUPUI University Library, 25-Apr-2013
  */
 
 package edu.iupui.ulib.dspace;
 
 import java.io.File;
 import java.net.URISyntaxException;
 import java.net.URL;
 import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
 
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
        project.setBaseDir(new File(System.getProperty("user.dir"))); // XXX current dir?

        ProjectHelper projectHelper = ProjectHelper.getProjectHelper();
        project.addReference(ProjectHelper.PROJECTHELPER_REFERENCE, projectHelper);
         URL buildResource = App.class.getResource("/build.xml");
        projectHelper.parse(project, buildResource);
 
 	// Configure the project
 	project.setProperty("ivySettings",
 			    App.class.getResource("/ivysettings.xml").toString());
 
 	// Select the target operation
 	String target = project.getDefaultTarget(); // TODO parameterize
 
 	// Install
 	project.executeTarget(target);
     }
 }
