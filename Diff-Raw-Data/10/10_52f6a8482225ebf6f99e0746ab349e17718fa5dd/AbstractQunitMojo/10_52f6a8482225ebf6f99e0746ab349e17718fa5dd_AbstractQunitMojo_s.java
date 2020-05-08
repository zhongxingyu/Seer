 package com.cj.qunit.mojo;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.maven.plugin.AbstractMojo;
 
 abstract class AbstractQunitMojo extends AbstractMojo {
 
     /**
      * @parameter default-value="${basedir}
      * @readonly
      * @required
      */
     private File basedir;
 
     /**
      * @parameter
      */
    private String webRoot = "/";
     
     /**
      * @parameter
      */
     private String webPathToRequireDotJsConfig = "";
 
     /**
      * @parameter default-value=5000
      */
     private int timeout;
     
     /**
      * @parameter 
      */
     private List<String> extraPathsToServe = new ArrayList<String>();
     
     protected String webPathToRequireDotJsConfig() {
 		return webPathToRequireDotJsConfig;
 	}
 
     protected int returnTimeout(){
 	return timeout;
     }
     
     protected List<File> codePaths(){
         return QunitTestLocator.findCodePaths(basedir);
     }
     
     public String webRoot() {
         return webRoot;
     }
 
     protected List<File> extraPathsToServe(){ 
         List<File> extraPathsToServe = new ArrayList<File>();
 
         for(String path : this.extraPathsToServe){
             extraPathsToServe.add(new File(basedir, path));
         }
         System.out.println("The extra paths are " + extraPathsToServe);
         return extraPathsToServe;
     }
 }
