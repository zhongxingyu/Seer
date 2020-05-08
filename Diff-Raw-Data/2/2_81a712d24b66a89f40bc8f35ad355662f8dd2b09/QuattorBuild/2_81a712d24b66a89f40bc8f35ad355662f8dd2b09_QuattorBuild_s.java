 package org.quattor.maven;
 
 import java.util.List;
 
 import org.apache.maven.model.Contributor;
 import org.apache.maven.model.Developer;
 import org.apache.maven.model.License;
 import org.apache.maven.model.Model;
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.logging.Log;
 
 /**
  * Goal which sets the quattor build properties. Fails if required information
  * is not available in the pom.xml file.
  * 
  * @goal set-build-properties
  * 
  * @phase initialize
  */
 public class QuattorBuild extends AbstractMojo {
 
     /** @parameter default-value="${project}" */
     private org.apache.maven.project.MavenProject mavenProject;
 
     final private static String licenseFormat = "#   %s (%s)\n#   %s\n";
 
     final private static String developerFormat = "#   %s <%s>\n";
 
     public void execute() throws MojoExecutionException {
 
         Model model = mavenProject.getModel();
 
         String licenseInfo = formatLicenseInfo(model);
         setMavenProperty("license-info", licenseInfo);
 
         String licenseUrl = formatLicenseUrl(model);
         setMavenProperty("license-url", licenseUrl);
 
         String developerInfo = formatDeveloperInfo(model);
         setMavenProperty("developer-info", developerInfo);
 
         String authorInfo = formatAuthorInfo(model);
         setMavenProperty("author-info", authorInfo);
         
         String noSnapshotVersion = getNoSnapshotVersion(model);
         setMavenProperty("no-snapshot-version", noSnapshotVersion);
 
     }
 
     private String getNoSnapshotVersion(Model model) throws MojoExecutionException {
        return model.getVersion().replaceFirst("-\\.*", "");
     }
 
     private void setMavenProperty(String name, String value)
             throws MojoExecutionException {
 
         Log log = getLog();
         log.info("Setting property " + name + " = " + value + "\n");
 
         mavenProject.getProperties().put(name, value);
     }
 
     private String formatLicenseInfo(Model model) throws MojoExecutionException {
 
         List<License> licenses = model.getLicenses();
 
         if (licenses.size() == 0) {
             throw new MojoExecutionException(
                     "must provide license section of pom.xml");
         }
 
         StringBuffer sb = new StringBuffer(
                 "#\n# Software subject to following license(s):\n");
         for (License license : licenses) {
             sb.append(String.format(licenseFormat, license.getName(), license
                     .getUrl(), license.getComments()));
         }
         sb.append("#\n");
 
         return sb.toString();
     }
 
     private String formatLicenseUrl(Model model) throws MojoExecutionException {
 
         List<License> licenses = model.getLicenses();
 
         if (licenses.size() == 0) {
             throw new MojoExecutionException(
                     "must provide license section of pom.xml");
         }
 
         StringBuffer sb = new StringBuffer();
         for (License license : licenses) {
             sb.append(license.getUrl());
             sb.append(" ");
         }
 
         return sb.toString();
     }
 
     private String formatDeveloperInfo(Model model)
             throws MojoExecutionException {
 
         List<Developer> developers = model.getDevelopers();
 
         if (developers.size() == 0) {
             throw new MojoExecutionException(
                     "must provide developer section of pom.xml");
         }
 
         StringBuffer sb = new StringBuffer("#\n# Current developer(s):\n");
         for (Developer developer : developers) {
             sb.append(String.format(developerFormat, developer.getName(),
                     developer.getEmail()));
         }
         sb.append("#\n");
 
         return sb.toString();
     }
 
     private String formatAuthorInfo(Model model) throws MojoExecutionException {
 
         List<Contributor> contributors = model.getContributors();
 
         int numberOfAuthors = 0;
 
         StringBuffer sb = new StringBuffer("#\n# Author(s): ");
         for (Contributor contributor : contributors) {
             if (contributor.getRoles().contains("author")) {
                 sb.append((numberOfAuthors > 0) ? ", " : "");
                 sb.append(contributor.getName());
                 numberOfAuthors++;
             }
         }
         sb.append("\n#\n");
 
         return (numberOfAuthors > 0) ? sb.toString() : "";
     }
 }
