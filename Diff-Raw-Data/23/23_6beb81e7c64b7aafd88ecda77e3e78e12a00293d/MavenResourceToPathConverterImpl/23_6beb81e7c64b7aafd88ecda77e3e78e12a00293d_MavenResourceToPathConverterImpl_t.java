 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.peacocks;
 
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.util.ArrayList;
 import java.util.List;
 import org.apache.maven.model.Build;
 import org.apache.maven.model.Resource;
 
 /**
  *
  * @author edvicif
  */
 public enum MavenResourceToPathConverterImpl implements MavenResourceToPathConverter {
 
    INSTANCE;
 
     @Override
     public List<Path> getResourceDirectories(Build mavenBuild, List<Resource> mavenResourceDirs) {
         List<Path> resources = new ArrayList<>();
         for (Resource resource : mavenResourceDirs) {
             Path path = Paths.get(resource.getDirectory());
             resources.add(path);
         }
         return resources;
     }
 
 }
