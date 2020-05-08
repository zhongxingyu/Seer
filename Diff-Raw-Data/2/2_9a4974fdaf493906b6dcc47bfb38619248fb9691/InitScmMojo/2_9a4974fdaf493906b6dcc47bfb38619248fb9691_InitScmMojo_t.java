 package com.helemus.maven.misc;
 
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 
 import org.apache.maven.model.Model;
 import org.apache.maven.model.Scm;
 import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
 import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.codehaus.plexus.util.cli.CommandLineException;
 import org.codehaus.plexus.util.cli.CommandLineUtils;
 import org.codehaus.plexus.util.cli.Commandline;
 import org.codehaus.plexus.util.cli.StreamConsumer;
 
 /**
  * Goal which sets up the scm info in a pom.
  *
  * @goal init-scm
  */
 public class InitScmMojo extends AbstractMojo {
     /**
      * Location of the base directory.
      *
      * @parameter expression="${basedir}"
      * @required
      */
     private File basedir;
 
     /**
      * Location of the pom file.
      *
      * @parameter expression="${basedir}/pom.xml"
      * @required
      */
     private File pomFile;
 
     /**
      * @parameter expression="${scm.override}" default-value="false"
      */
     private boolean override;
 
     public void execute() throws MojoExecutionException {
         try {
             MavenXpp3Reader mavenReader = new MavenXpp3Reader();
             Model model = mavenReader.read(new FileReader(pomFile));
 
             if (model.getScm() == null || override) {
                 String scmURL = getGitUrl();
                 if (scmURL != null) {
                     Scm scm = new Scm();
                     scm.setConnection(scmURL);
                     scm.setDeveloperConnection(scmURL);
                     String viewURL = getViewURL(scmURL);
                     if (viewURL != null) {
                         scm.setUrl(viewURL);
                     }
                     model.setScm(scm);
                     new MavenXpp3Writer().write(new FileWriter(pomFile), model);
                     return;
                 }
 
                 getLog().warn("No SCM found.");
             } else {
                 getLog().info("POM already has SCM section.");
             }
         } catch (Exception e) {
             throw new MojoExecutionException("Unable to execute", e);
         }
     }
 
     private String getViewURL(String url) {
         if (url.startsWith("scm:git:ssh://git@github.com/")) {
            String str = "http://github.com/" + url.substring("scm:git:ssh://git@github.com/".length());
             return str.substring(0, str.length() - 4);
         }
         return null;
     }
 
     private String getGitUrl() throws CommandLineException {
         Commandline cmd = new Commandline("git remote show origin");
         cmd.setWorkingDirectory(basedir);
 
         final StringBuilder builder = new StringBuilder();
         CommandLineUtils.executeCommandLine(cmd, new StreamConsumer() {
 
             public void consumeLine(String line) {
                 getLog().debug("** " + line);
                 if (builder.length() == 0) {
                     int idx = line.indexOf("URL:");
                     if (idx >= 0) {
                         getLog().debug("found url line '" + line + "'");
                         String postUrl = line.trim().substring(idx + 3);
                         getLog().debug("post url = '" + postUrl + "'");
                         builder.append(postUrl);
                     }
                 }
             }
         }, new StreamConsumer() {
 
             public void consumeLine(String line) {
                 getLog().error(line);
 
             }
         });
 
         String str = builder.toString();
 
         getLog().debug("got string '" + str + "'");
 
         if (builder.length() > 0) {
 
             if (str.startsWith("git@")) {
                 return "scm:git:ssh://" + str.replace(':', '/');
             } else {
 
                 return "scm:git:" + str;
             }
         }
         return null;
     }
 }
