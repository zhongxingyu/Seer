 /*
  * Copyright (c) 2007, Kalixia, SARL. All Rights Reserved.
  */
 package org.jini.rio.tools.maven.rio;
 
 import org.apache.maven.artifact.Artifact;
 import org.apache.maven.artifact.DependencyResolutionRequiredException;
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.project.MavenProject;
 import org.apache.maven.project.MavenProjectHelper;
 import org.apache.tools.ant.Project;
 import org.apache.tools.ant.Location;
 import org.apache.tools.ant.taskdefs.Jar;
 import org.apache.tools.ant.taskdefs.Manifest;
 import org.apache.tools.ant.taskdefs.ManifestException;
 import org.apache.tools.ant.types.FileSet;
 import org.apache.tools.ant.types.Path;
 import org.apache.tools.ant.types.ZipFileSet;
 import org.apache.tools.ant.types.selectors.FilenameSelector;
 import org.jini.rio.tools.ant.ClassDepAndJarTask;
 import java.io.File;
 import java.io.PrintWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 
 /**
  * Properly packages a OAR (Operational string ARchive).
  * This goal creates both the implementation JAR, the client JAR, the downloadable code JAR,
  * the optional Service UI JAR and finally bundle them all in the OAR.
  * @goal oar
  * @description Build & deploy a Rio OAR
  * @phase package
  * @requiresProject true
  * @requiresDependencyResolution
  */
 public class OarMojo extends AbstractMojo {
 
     /**
      * @parameter
      * @required
      */
     private List implementationIns;
 
     /**
      * @parameter
      */
     private List implementationOuts = new ArrayList();
 
     /**
      * @parameter
      */
     private List implementationSkips = new ArrayList();
 
     /**
      * @parameter
      */
     private List implementationTopclasses;
 
     /**
      * @parameter
      */
     private String implementationPreferredlist;
 
     /**
      * @parameter
      */
     private List clientIns;
 
     /**
      * @parameter
      */
     private List clientOuts = new ArrayList();
 
     /**
      * @parameter
      */
     private List clientSkips = new ArrayList();
 
     /**
      * @parameter
      */
     private List clientTopclasses;
 
     /**
      * @parameter
      */
     private String clientPreferredlist;
 
     /**
      * @parameter
      */
     private List downloadableIns;
 
     /**
      * @parameter
      */
     private List downloadableOuts = new ArrayList();
 
     /**
      * @parameter
      */
     private List downloadableSkips = new ArrayList();
 
     /**
      * @parameter
      */
     private List downloadableTopclasses;
 
     /**
      * @parameter
      */
     private String downloadablePreferredlist;
 
     /**
      * @parameter
      */
     private List uiIns;
 
     /**
      * @parameter
      */
     private List uiOuts = new ArrayList();
 
     /**
      * @parameter
      */
     private List uiSkips = new ArrayList();
 
     /**
      * @parameter
      */
     private List uiTopclasses;
 
     /**
      * @parameter
      */
     private String uiPreferredlist;
 
     /**
      * Directory containing the generated JARs.
      *
      * @parameter expression="${project.build.directory}"
      * @required
      */
     private File outputDirectory;
 
     /**
      * Name of the service implementation JAR.
      *
      * @parameter expression="${project.build.directory}/${project.build.finalName}-impl.jar"
      * @required
      */
     private String jarImplName;
 
     /**
      * Name of the service client JAR.
      *
      * @parameter expression="${project.build.directory}/${project.build.finalName}.jar"
      * @required
      */
     private String jarClientName;
 
     /**
      * Name of the service client JAR.
      *
      * @parameter expression="${project.build.directory}/${project.build.finalName}-dl.jar"
      * @required
      */
     private String jarDownloadableName;
 
     /**
      * Name of the service implementation JAR.
      *
      * @parameter expression="${project.build.directory}/${project.build.finalName}-ui.jar"
      * @required
      */
     private String jarUiName;
 
     /**
      * Name of the OAR.
      *
      * @parameter expression="${project.build.finalName}"
      * @required
      */
     private String oarName;
 
     /**
      * The OAR to generate.
      *
      * @parameter expression="${project.build.directory}/${project.build.finalName}.oar"
      * @required
      */
     private String oarFile;
 
     /**
      * OpString to deploy.
      * @parameter
      * @required
      */
     private String opstring;
 
     /**
      * OAR activation. Either "Automatic" or "Manual"
      * @parameter expression="Automatic"
      */
     private String activation;
 
     /**
      * The maven project.
      *
      * @parameter expression="${project}"
      * @required
      * @readonly
      */
     private MavenProject project;
 
     /**
      * @parameter expression="${project.dependencyArtifacts}"
      */
     private Collection dependencies;
 
     /**
      * @component
      */
     private MavenProjectHelper projectHelper;
 
     public void execute() throws MojoExecutionException {
         // build Ant project
         Project antProject = new Project();
 
         // build the compilation classpath as a Ant path
         Path classpath = new Path(new Project());
         List classpaths;
         try {
             classpaths = project.getCompileClasspathElements();
         } catch (DependencyResolutionRequiredException e) {
             throw new MojoExecutionException("Can't get compilation classpath", e);
         }
         for (int i = 0; i < classpaths.size(); i++) {
             Object o = classpaths.get(i);
             getLog().debug("Found classpath element " + o.toString());
             classpath.append(new Path(antProject, o.toString()));
         }
         for (Iterator iterator = dependencies.iterator(); iterator.hasNext();) {
             Artifact artifact = (Artifact) iterator.next();
             getLog().debug("Found artifact element " + artifact.getFile());
             if (artifact.getFile() != null)
                 classpath.append(new Path(antProject, artifact.getFile().toString()));
         }
         getLog().debug("Build ant path: " + classpath.toString());
 
         getLog().info("Building service implementation jar (in " + jarImplName + ")...");
         ClassDepAndJarTask classDepAndJarTask = new ClassDepAndJarTask();
         classDepAndJarTask.setProject(antProject);
         classDepAndJarTask.setClasspath(classpath);
         classDepAndJarTask.setFiles(true);
         File resourcesDirectory = new File(project.getBasedir(), "src" + File.separatorChar + "main" + File.separatorChar + "resources");
         if (resourcesDirectory.exists()) {
             FileSet resourcesFileSet = new FileSet();
             resourcesFileSet.setDir(resourcesDirectory);
             resourcesFileSet.setIncludes("**/*");
             classDepAndJarTask.addFileset(resourcesFileSet);
         }
         if (implementationPreferredlist != null && !"".equals(implementationPreferredlist))
             classDepAndJarTask.setPreferredlist(new File(implementationPreferredlist));
         classDepAndJarTask.setJarfile(new File(jarImplName));
         for (Iterator iterator = implementationIns.iterator(); iterator.hasNext();) {
             String in = (String) iterator.next();
             classDepAndJarTask.setIn(in);
         }
         for (Iterator iterator = implementationOuts.iterator(); iterator.hasNext();) {
             String out = (String) iterator.next();
             classDepAndJarTask.setOut(out);
         }
         for (Iterator iterator = implementationSkips.iterator(); iterator.hasNext();) {
             String skip = (String) iterator.next();
             classDepAndJarTask.setSkip(skip);
         }
         for (Iterator iterator = implementationTopclasses.iterator(); iterator.hasNext();) {
             String topclass = (String) iterator.next();
             classDepAndJarTask.setTopclass(topclass);
         }
         classDepAndJarTask.execute();
 
 		if (clientIns != null) {
         	getLog().info("Building service client jar (in " + jarClientName + ")...");
 	        classDepAndJarTask = new ClassDepAndJarTask();
 	        classDepAndJarTask.setProject(antProject);
 	        classDepAndJarTask.setClasspath(classpath);
 	        classDepAndJarTask.setFiles(true);
 	        if (clientPreferredlist != null && !clientPreferredlist.equals(""))
 	            classDepAndJarTask.setPreferredlist(new File(clientPreferredlist));
 	        classDepAndJarTask.setJarfile(new File(jarClientName));
 	        for (Iterator iterator = clientIns.iterator(); iterator.hasNext();) {
 	            String in = (String) iterator.next();
 	            classDepAndJarTask.setIn(in);
 	        }
 	        for (Iterator iterator = clientOuts.iterator(); iterator.hasNext();) {
 	            String out = (String) iterator.next();
 	            classDepAndJarTask.setIn(out);
 	        }
 	        for (Iterator iterator = clientSkips.iterator(); iterator.hasNext();) {
 	            String skip = (String) iterator.next();
 	            classDepAndJarTask.setSkip(skip);
 	        }
 	        for (Iterator iterator = clientTopclasses.iterator(); iterator.hasNext();) {
 	            String topclass = (String) iterator.next();
 	            classDepAndJarTask.setTopclass(topclass);
 	        }
 	        classDepAndJarTask.execute();
 		}
 
 		if (downloadableIns != null) {
        	getLog().info("Building downnloadable classes jar (in " + jarDownloadableName + ")...");
 	        classDepAndJarTask = new ClassDepAndJarTask();
 	        classDepAndJarTask.setProject(antProject);
 	        classDepAndJarTask.setClasspath(classpath);
 	        classDepAndJarTask.setFiles(true);
 	        if (downloadablePreferredlist != null && !downloadablePreferredlist.equals(""))
 	            classDepAndJarTask.setPreferredlist(new File(downloadablePreferredlist));
 	        classDepAndJarTask.setJarfile(new File(jarDownloadableName));
 	        for (Iterator iterator = downloadableIns.iterator(); iterator.hasNext();) {
 	            String in = (String) iterator.next();
 	            classDepAndJarTask.setIn(in);
 	        }
 	        for (Iterator iterator = downloadableOuts.iterator(); iterator.hasNext();) {
 	            String out = (String) iterator.next();
 	            classDepAndJarTask.setIn(out);
 	        }
 	        for (Iterator iterator = downloadableSkips.iterator(); iterator.hasNext();) {
 	            String skip = (String) iterator.next();
 	            classDepAndJarTask.setSkip(skip);
 	        }
 	        for (Iterator iterator = downloadableTopclasses.iterator(); iterator.hasNext();) {
 	            String topclass = (String) iterator.next();
 	            classDepAndJarTask.setTopclass(topclass);
 	        }
 	        classDepAndJarTask.execute();
 		}
 
         if (uiTopclasses != null && uiTopclasses.size() > 0) {
             getLog().info("Building service ui jar (in " + jarUiName + ")...");
             classDepAndJarTask = new ClassDepAndJarTask();
             classDepAndJarTask.setProject(antProject);
             classDepAndJarTask.setClasspath(classpath);
             classDepAndJarTask.setFiles(true);
             if (uiPreferredlist != null && !uiPreferredlist.equals(""))
                 classDepAndJarTask.setPreferredlist(new File(uiPreferredlist));
             classDepAndJarTask.setJarfile(new File(jarUiName));
             for (Iterator iterator = uiIns.iterator(); iterator.hasNext();) {
                 String in = (String) iterator.next();
                 classDepAndJarTask.setIn(in);
             }
             for (Iterator iterator = uiOuts.iterator(); iterator.hasNext();) {
                 String out = (String) iterator.next();
                 classDepAndJarTask.setIn(out);
             }
             for (Iterator iterator = uiSkips.iterator(); iterator.hasNext();) {
                 String skip = (String) iterator.next();
                 classDepAndJarTask.setSkip(skip);
             }
             for (Iterator iterator = uiTopclasses.iterator(); iterator.hasNext();) {
                 String topclass = (String) iterator.next();
                 classDepAndJarTask.setTopclass(topclass);
             }
             classDepAndJarTask.execute();
             projectHelper.attachArtifact(project, "jar", "ui", new File(jarUiName));
         }
 
         getLog().info("Building OAR (in " + oarFile + ")...");
         
         Jar oar = new Jar();
         oar.setProject(antProject);
         oar.setDestFile(new File(oarFile));
         // add the opstring
         ZipFileSet fileSetOpstring = new ZipFileSet();
        fileSetOpstring.setDir(new File(opstring).getParentFile());
        fileSetOpstring.setIncludes(new File(opstring).getName());
         oar.addZipfileset(fileSetOpstring);
         // add the required JARs
         ZipFileSet fileSetJARs = new ZipFileSet();
        fileSetJARs.setDir(new File("target"));
         fileSetJARs.setPrefix("lib");
         fileSetJARs.setIncludes("*.jar");
         oar.addZipfileset(fileSetJARs);
         // add the manifest
         Manifest manifest = new Manifest();
         try {
             manifest.addConfiguredAttribute(new Manifest.Attribute("OAR-Name", oarName));
             manifest.addConfiguredAttribute(new Manifest.Attribute("OAR-Version", project.getVersion()));
             manifest.addConfiguredAttribute(new Manifest.Attribute("OAR-OperationalString", new File(opstring).getName()));
             manifest.addConfiguredAttribute(new Manifest.Attribute("OAR-Activation", activation));
         } catch (ManifestException e) {
             getLog().error("Can't generate OAR manifest", e);
         }
         try {
             oar.addConfiguredManifest(manifest);
         } catch (ManifestException e) {
             getLog().error("Can't add manifest into OAR", e);
         }
         // execute the Ant task
         oar.execute();
 
         project.getArtifact().setFile(new File(oarFile));
 		projectHelper.attachArtifact(project, "jar", "impl", new File(jarImplName));
 		if (new File(jarClientName).exists())
         	projectHelper.attachArtifact(project, "jar", "", new File(jarClientName));
         if (new File(jarDownloadableName).exists())
 	        projectHelper.attachArtifact(project, "jar", "dl", new File(jarDownloadableName));
     }
 
 }
