 package com.annadaletech.maven.clojure.osgi;
 
 import clojure.lang.*;
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 
 import java.io.IOException;
 
 /**
  * @goal check-manifest
  *
  * @author pmoriarty 2/28/12 3:38 PM
  */
 public class ClojureOSGiManifestCheckMojo extends AbstractMojo {
 
     /**
      *
      *
      * @parameter default-value="src/main/clojure"
      * @noinspection UnusedDeclaration
      */
     private String baseDir;
 
     /**
      *
      *
      * @parameter default-value="src/main/resources/META-INF/MANIFEST.MF"
      * @noinspection UnusedDeclaration
      */
     private String manifest;
 
     /**
      *
      *
      * @parameter
      * @noinspection UnusedDeclaration
      */
     private String ignore;
 
     @Override
     public void execute() throws MojoExecutionException, MojoFailureException {
         try {
             RT.loadResourceScript("find_package_imports.clj");
         } catch (IOException e) {
             e.printStackTrace();
             throw new MojoExecutionException("Unable to load clojure file: find_package_imports.clj", e);
         }
 
         Var check = RT.var("user", "check");
 
         // Call it!
         Boolean result = (Boolean) check.invoke(baseDir, manifest, ignore);
         if (!result) {
             throw new MojoFailureException("Some packages are missing from the Import-Package directive in " + manifest);
         }
     }
 }
