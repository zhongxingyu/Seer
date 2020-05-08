 package deeva.utils;
 
 /*
   Adapted from http://dzone.com/snippets/get-all-classes-within-package
  */
 import java.io.File;
 import java.util.*;
 
 public class SourceClassFinder {
     private final List<File> classPaths;
     private final List<File> sourcePaths;
     private Map<String, SourceClassMeta> sourceClassMetas;
     private Map<String, String> sources;
 
     /* Maybe abstract out? who knows :S :S definitely in need of refactor at
     some point */
     public SourceClassFinder(List<String> classPathStrings,
                              List<String> sourcePathStrings) {
         if (classPathStrings == null) {
             classPathStrings = new LinkedList<String>();
         }
 
         if (sourcePathStrings == null) {
             sourcePathStrings = new LinkedList<String>();
         }
 
         /* Add the current directory as the default if no classpaths are
         specified */
         if (classPathStrings.isEmpty()) {
             classPathStrings.add(".");
         }
 
         if (sourcePathStrings.isEmpty()) {
             sourcePathStrings.add(".");
         }
 
         this.classPaths = new LinkedList<File>();
         this.sourcePaths = new LinkedList<File>();
 
         for (String cpString : classPathStrings) {
             this.classPaths.add(new File(cpString));
         }
 
         for (String srcString : sourcePathStrings) {
             this.sourcePaths.add(new File(srcString));
         }
 
     }
 
     /**
      *  Finds all the classes within the given directory, however,
      *  it may not produce the right package names, since we're also not
      *  loading the class either, so it is up to the user to provide valid
      *  source paths.
      *
      *  Do sym-links need to be dealt with?
      *
      */
     private Map<String, SourceClassMeta> findClasses(File directory, String packageName) {
         Map<String, SourceClassMeta> foundClasses = new HashMap<String, SourceClassMeta>();
         Map<String, String> nestedClasses = new HashMap<String, String>();
 
         if (!directory.exists()) {
             return foundClasses;
         }
 
         File[] files = directory.listFiles();
         for (File file : files) {
             String packagePrepend = packageName.equals("") ? "" : packageName + '.';
             if (file.isDirectory()) {
                 /* If the directory is a explicit classpath,
                 we'll skip it as it will be dealt with later */
                 if (this.classPaths.contains(file)) {
                     continue;
                 }
 
                 /* Skip directories with dots in, malformed */
                 if (file.getName().contains(".")) {
                     continue;
                 }
 
                 String newPackageName = packagePrepend + file.getName();
                 foundClasses.putAll(findClasses(file, newPackageName));
             } else if (file.getName().endsWith(".class")) {
                 /* We've found a class */
                 String className = file.getName().substring(0,
                         file.getName().length() - 6);
 
                 String fullClassName = packagePrepend + className;
 
                 // If we find a nested class, deal with it later,
                 // as its parent class may not have been added yet
                 int sentinelIndex = className.indexOf('$');
                 if (sentinelIndex != -1) {
                     String hostingClass = className.substring(0, sentinelIndex);
                     nestedClasses.put(className, hostingClass);
                     continue;
                 }
                 SourceClassMeta meta = new SourceClassMeta(fullClassName,
                         file);
                 foundClasses.put(fullClassName, meta);
             }
         }
 
         /* Deal with nested classes */
         for (String nestedClass : nestedClasses.keySet()) {
             String hostingClass = nestedClasses.get(nestedClass);
 
             SourceClassMeta meta = foundClasses.get(hostingClass);
             if (meta == null) {
                 continue;
             }
 
             meta.addNestedClass(nestedClass);
             foundClasses.put(nestedClass, meta);
         }
 
         return foundClasses;
     }
 
     private void findSources(File directory, String packageName) {
         //Map<String, String> foundSources = new HashMap<String, String>();
 
         if (!directory.exists()) {
             return;
         }
 
         File[] files = directory.listFiles();
         for (File file : files) {
             String packagePrepend = packageName.equals("") ? "" : packageName + '.';
             String fileName = file.getName();
             if (file.isDirectory()) {
                 /* Only process a directory once i.e. if contained within
                 classpath */
                 if (this.sourcePaths.contains(file)) {
                     continue;
                 }
 
                 /* Find sources in all the folders */
                 findSources(file, packagePrepend + fileName);
 
             } else if (fileName.endsWith(".java")) {
 
                 String className = packagePrepend + fileName.substring(0,
                         fileName.length() - 5);
 
                 /* We've found a source file with a corresponding class */
                 if (this.sourceClassMetas.containsKey(className)) {
                     SourceClassMeta meta = this.sourceClassMetas.get(className);
                     meta.setSourceFile(file);
                 }
             }
         }
 
         return;
     }
 
     public Map<String, SourceClassMeta> getAllClasses() {
         /* Get the cached result */
         if (this.sourceClassMetas != null) {
             return this.sourceClassMetas;
         }
 
         this.sourceClassMetas = new HashMap<String, SourceClassMeta>();
         /* For each class path we find the classes within it */
         for (File classPath : this.classPaths) {
             this.sourceClassMetas.putAll(findClasses(classPath, ""));
         }
 
         return this.sourceClassMetas;
     }
 
     public Map<String, String> getAllSources() {
         /*
          We have a list of classes, generate a list of the class names with
          .java extensions, search the source paths, and if we see any java
          files, then we add their location, and their name iff they are a
          named class
 
          */
 
         /* Get the cached result */
         if (this.sources != null) {
             return this.sources;
         }
 
         this.sources = new HashMap<String, String>();
 
         /* Get all the classes first */
         Map<String, SourceClassMeta> classes = this.getAllClasses();
 
         /* Find all the sources */
         for (File sourcePath : this.sourcePaths) {
             findSources(sourcePath, "");
         }
 
         for (String className : classes.keySet()) {
             SourceClassMeta meta = classes.get(className);
             String fileName = meta.getSourceFileName();
             this.sources.put(className, fileName);
         }
 
         return this.sources;
     }
 
     public static void main(String[] args) {
         List<String> cps = new LinkedList<String>();
 
         SourceClassFinder finder = new SourceClassFinder(null, null);
 
         Map<String, String> sources = finder.getAllSources();
 
         for (String source : sources.keySet()) {
             System.out.println("--------");
             System.out.println("ClassName: " + source);
             System.out.println("Location: " + sources.get(source));
         }
         System.out.println("--------");
     }
 
 }
