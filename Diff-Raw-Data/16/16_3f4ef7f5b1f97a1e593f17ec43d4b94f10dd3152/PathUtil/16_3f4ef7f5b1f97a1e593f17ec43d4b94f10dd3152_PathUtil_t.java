 /*
  * Team : AGF AM / OSI / SI / BO
  *
  * Copyright (c) 2001 AGF Asset Management.
  */
 package net.codjo.test.common;
 import java.io.File;
 import java.net.URL;
 import junit.framework.AssertionFailedError;
 /**
  * Classe de gestion des chemins d'acces aux rpertoires target, src et test.
  */
 public final class PathUtil {
     public static final String TARGET_DIRECTORY_NAME = "target";
     public static final String TEST_DIRECTORY = "src/test/java";
     public static final String SRC_DIRECTORY = "src/main/java";
     public static final String TEST_RESOURCES_DIRECTORY = "src/test/resources";
     public static final String SRC_RESOURCES_DIRECTORY = "src/main/resources";
     private final Class root;
 
 
     public PathUtil(Class baseClass) {
         this.root = baseClass;
     }
 
 
     public Directory findTargetDirectory() {
         return findTargetDirectory(root);
     }
 
 
     public static Directory findTargetDirectory(Class baseClass) {
         return findDirectory(baseClass, TARGET_DIRECTORY_NAME);
     }
 
 
     public Directory findTestDirectory() {
         return findTestDirectory(root);
     }
 
 
     public static Directory findTestDirectory(Class baseClass) {
         return findDirectory(baseClass, TEST_DIRECTORY);
     }
 
 
     public Directory findTestResourcesDirectory() {
         return findTestResourcesDirectory(root);
     }
 
 
     public static Directory findTestResourcesDirectory(Class baseClass) {
         return findDirectory(baseClass, TEST_RESOURCES_DIRECTORY);
     }
 
 
     public Directory findSrcDirectory() {
         return findSrcDirectory(root);
     }
 
 
     public static Directory findSrcDirectory(Class baseClass) {
         return findDirectory(baseClass, SRC_DIRECTORY);
     }
 
 
     public Directory findBaseDirectory() {
         return findBaseDirectory(root);
     }
 
 
     public static Directory findBaseDirectory(Class baseClass) {
         return findDirectory(baseClass, "");
     }
 
 
     public Directory findJavaFileDirectory() {
         return findJavaFileDirectory(root);
     }
 
 
     public static Directory findJavaFileDirectory(Class baseClass) {
         String name = "/" + baseClass.getName().replace('.', '/');
         return new Directory(new File(findTestDirectory(baseClass), name).getParentFile().getAbsolutePath());
     }
 
 
     public static Directory findResourcesFileDirectory(Class baseClass) {
         Directory classDirectory = findJavaFileDirectory(baseClass);
 
         return new Directory(classDirectory.getAbsolutePath()
               .replaceAll("\\\\", "/")
               .replace(TEST_DIRECTORY, TEST_RESOURCES_DIRECTORY));
     }
 
    public static String normalize(String path) {
    	String result;
    	if (File.separatorChar == '/') {
    		result = path.replaceAll("\\\\", "/");
    	} else {
    		result = path.replaceAll("/", "\\\\");
    	}
    	return result;
    }
 
     public File find(String resourceName) {
         return find(root, resourceName);
     }
 
 
     public static File find(Class baseClass, String resourceName) {
         URL resource = baseClass.getResource(resourceName);
         if (resource == null) {
             throw new AssertionFailedError("Resource '" + resourceName + "' est introuvable.");
         }
         return new File(resource.getFile());
     }
 
 
     private static Directory findDirectory(Class baseClass, String directory) {
         String name = "/" + baseClass.getName().replace('.', '/') + ".class";
         String absolutePath = baseClass.getResource(name).getFile();
         return toDirectory(absolutePath, directory);
     }
 
 
     private static Directory toDirectory(String absolutePath, String directory) {
         String path = absolutePath.substring(0, absolutePath.lastIndexOf(PathUtil.TARGET_DIRECTORY_NAME));
         return new Directory(new File(path, directory).getAbsolutePath());
     }
 }
