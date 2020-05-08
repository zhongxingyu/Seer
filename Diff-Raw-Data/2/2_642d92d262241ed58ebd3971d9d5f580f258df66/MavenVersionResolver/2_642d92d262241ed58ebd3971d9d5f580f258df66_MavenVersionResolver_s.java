 package com.zenika.dorm.maven.pom;
 
 import com.zenika.dorm.core.exception.CoreException;
 import org.apache.maven.model.Dependency;
 import org.apache.maven.model.Model;
 
 import java.util.Properties;
 
 /**
  * @author: Antoine ROUAZE <antoine.rouaze AT zenika.com>
  */
 public class MavenVersionResolver {
 
     private MavenPomReader pomReader;
 
     public MavenVersionResolver(MavenPomReader pomReader) {
         this.pomReader = pomReader;
     }
 
     private boolean isDependencyManaged(org.apache.maven.model.Dependency dependency) {
         return dependency.getVersion() == null;
     }
 
     public String getVersion(Dependency dependency) {
         String version = dependency.getVersion();
         if (isDependencyManaged(dependency)) {
             version = getDependencyVersionManaged(dependency);
         }
         if (isMavenProperties(dependency.getVersion())) {
             version = getPropertyValue(dependency.getVersion());
         }
         return version;
     }
 
     private String getPropertyValue(String version) {
         String versionResolved = resolveProperty(pomReader.getModel(), version);
         if (versionResolved == null) {
             versionResolved = resolveProperty(pomReader.getParentModel(), version);
         }
         return versionResolved;
     }
 
     private String getDependencyVersionManaged(Dependency dependency) {
         String version = null;
         if (pomReader.getModel().getDependencyManagement() != null) {
             version = resolveDependencyManaged(pomReader.getModel(), dependency);
         }
         if (version == null) {
             version = resolveDependencyManaged(pomReader.getParentModel(), dependency);
         }
         return version;
     }
     
     private String resolveDependencyManaged(Model model, Dependency dependency) {
         for (Dependency itDependency : model.getDependencyManagement().getDependencies()) {
             if (itDependency.getArtifactId().equals(dependency.getArtifactId()) && itDependency.getGroupId().equals(dependency.getGroupId())) {
                 if (isMavenProperties(itDependency.getVersion())) {
                     return resolveVersion(model, itDependency);
                 }
                 return itDependency.getVersion();
             }
         }
         return null;
     }
 
     public boolean isMavenProperties(String version) {
         return version != null && version.matches("\\$\\{[a-zA-Z0-9.\\-]*\\}");
     }
 
     public String resolveVersion(Model model, Dependency dependency) {
         String propertyVersion = dependency.getVersion();
         return resolveProperty(model, propertyVersion);
     }
 
     public String resolveProperty(Model model, String property) {
         String value = null;
        if (property.matches("\\$\\{project.version\\}")) {
             return property.replace("${project.version}", model.getVersion());
         } else if (model.getProperties() != null) {
             return getValueProperty(model, property);
         }
         return value;
     }
 
     private String getValueProperty(Model model, String property) {
         Properties properties = model.getProperties();
         if (property.length() > 3) {
             property = property.substring(2, property.length() - 1);
             return properties.getProperty(property);
         }
         return null;
     }
 
 }
