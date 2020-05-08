 package com.cloudbees.genapp.tomcat8;
 
 import com.cloudbees.genapp.metadata.*;
 
 import java.io.File;
 import java.nio.file.FileSystems;
 import java.nio.file.Path;
 
 /*
  * This class contains the main method to get the Genapp metadata and configure Tomcat 7.
  */
 
 public class Setup {
     /**
      * The main method takes optional arguments for the location of the
      * context.xml file to modify, as well as the location of the metadata.json
      * file. Defaults are:
      * CONTEXT_XML_PATH = $app_dir/server/conf/context.xml
      * METADATA_PATH = $genapp_dir/metadata.json
      *
      * @param args Two optional args: [ CONTEXT_XML_PATH [ METADATA_PATH ]]
      * @throws Exception
      */
     public static void main(String[] args) throws Exception {
         MetadataFinder metadataFinder = new MetadataFinder();
         Metadata metadata = metadataFinder.getMetadata();
 
         EnvBuilder safeEnvBuilder = new EnvBuilder(true, false, metadata);
         safeEnvBuilder.writeControlFile("/env_safe");
 
         {
             Path appDir = FileSystems.getDefault().getPath(System.getenv("app_dir"));
             Path clickstackDir = FileSystems.getDefault().getPath(System.getenv("plugin_dir"));
             Path packageDir = FileSystems.getDefault().getPath(System.getenv("pkg_dir"));
 
             String appPort = System.getenv("app_port");
             Setup2 setup2 = new Setup2(appDir, clickstackDir, packageDir);
             setup2.installCatalinaBase();
             setup2.installCatalinaHome();
             setup2.installCloudBeesJavaAgent();
             setup2.installJmxTransAgent();
             setup2.writeJavaOpts(metadata);
             setup2.writeConfig(metadata, appPort);
             setup2.installControlScripts();
         }
 
         {
             File appDir = new File(System.getenv("app_dir"));
             ContextXmlBuilder contextXmlBuilder = new ContextXmlBuilder(metadata, appDir);
             contextXmlBuilder.buildTomcatConfigurationFiles("/server/conf/server.xml", "/server/conf/context.xml");
         }
 
     }
 }
