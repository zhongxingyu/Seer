 package org.peacocks;
 
 import java.io.IOException;
 import java.nio.file.*;
 import java.nio.file.attribute.BasicFileAttributes;
 import java.util.LinkedList;
 import java.util.List;
 import org.apache.maven.model.Resource;
 
 /**
  *
  */
 public enum ResourceScanner {
 
     INSTANCE;
 
     public List<ResourceSubject> findResources(Resource mavenResource) throws IOException {
         return findResources(mavenResource.getDirectory());
     }
 
     List<ResourceSubject> findResources(Path startDir) throws IOException {
         FindResources findResources = new FindResources(startDir);
         Files.walkFileTree(startDir, findResources);
         return findResources.resourceSubjects;
     }
 
     public List<ResourceSubject> findResources(String resourceDirPath) throws IOException {
         return findResources(Paths.get(resourceDirPath));
     }
 
     private static class FindResources extends SimpleFileVisitor<Path> {
 
         private final Path startDir;
 
         public FindResources(Path startDir) {
             if (!Files.isDirectory(startDir)) {
                 throw new IllegalArgumentException("Path for resource scanning should be a directory");
             }
             this.startDir = startDir;
         }
         private List<ResourceSubject> resourceSubjects = new LinkedList<>();
 
         @Override
         public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
             if (Files.isDirectory(file)) {
                 return FileVisitResult.CONTINUE;
             }
 
             resourceSubjects.add(
                     ResourceSubject.Builder.instance().
                     packageName(generatePackageName(file)).
                     resourceName(generateResourceName(file)).
                     resourceType(generateFileExtension(file)).build());
 
             return FileVisitResult.CONTINUE;
         }
 
         private String generateFileExtension(Path file) {
             String fileName = file.getFileName().toString();
             int exntensionPosition = fileName.lastIndexOf(".");
             if (exntensionPosition < 0) {
                 return "";
             }
             String fileExtension = fileName.substring(exntensionPosition + 1, fileName.length());
             return fileExtension;
         }
 
         private String generateResourceName(Path file) {
             String fileName = file.getFileName().toString();
             int exntensionPosition = fileName.lastIndexOf(".");
             if (exntensionPosition < 0) {
                 return fileName;
             }
             String fileTitle = fileName.substring(0, exntensionPosition);
             return fileTitle;
         }
 
         private String generatePackageName(Path file) {
             String packageName;
             if (startDir.equals(file.getParent())) {
                 packageName = "";
             } else {
                 packageName = turnSeparatorsToDots(startDir.relativize(file).getParent().toString());
             }
             return packageName;
         }
     }
 
     static String turnSeparatorsToDots(String pathWithSeparator) {
        return pathWithSeparator.replaceAll("\\W", ".");
     }
 }
