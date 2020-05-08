 package com.garfty.flexmojosprojectcreator;
 
 /*
  * Copyright 2001-2005 The Apache Software Foundation.
  * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  * 
 * http://www.apache.org/licenses/LICENSE-2.0
  * 
 * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 import org.xml.sax.SAXParseException;
 
 /**
  * Goal which initialises the project
  *
  * @goal initialiseProject
  * @phase validate
  */
 public class MyMojo extends AbstractMojo {
 
     /**
      * The Project Location.
      *
      * @parameter expression="${initialiseProject.projectLocation}"
      * default-value=""
      */
     private String projectLocation;
     
     /**
      * The Workspace Location.
      *
      * @parameter expression="${initialiseProject.workspaceLocation}"
      * default-value=""
      */
     private String workspaceLocation;
     
     /**
      * The Project Source Directory.
      */
     private String sourceDirectory;
     
     /**
      * The Project Test Source Directory.
      */
     private String testSourceDirectory = "";
     
     /**
      * The Project Resource Directories.
      */
     private String resourceDirectories = "";
     
     /**
      * System HOME Environment string
      */
     private String homeDirectory;
     
     /**
      * local repository path
      */
     private String localRepositoryPath = "";
     
     /**
      * Main application file
      */
     private String mainApplicationFile = "Empty.as";
     
     /**
      * Set if the project is a library project
      */
     private Boolean isLibraryProject = false;
     
     /**
      * Set if the project has a manifest file
      */
     private Boolean hasManifestFile = false;
     
     /**
      * Set the Namespace URI
      */
     private String namespaceURI;
     
     /**
      * Conditional Compiler element
      */
     private String conditionalCompiler = "";
     
     public void execute() throws MojoExecutionException {
         
         // Add the following slash to the project location
         projectLocation = addFollowingSlash(projectLocation);
 
         // Get the effective pom and parse the dependencies
         File effectivePom = new File(projectLocation + "target/classes/effective-pom.xml");
 
         // Set the homdirectory from the system variable
         homeDirectory = System.getenv("HOME");
 
         try {
             // Declare a new Document Factory Builder / Document Builder for the XML
             DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
             DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
 
             // Parse the XML and normalise it
             Document XMLDoc = dBuilder.parse(effectivePom);
             XMLDoc.getDocumentElement().normalize();
 
             // Get the root XML Node
             Element root = XMLDoc.getDocumentElement();
 
             // Set the children nodes from the root node
             NodeList nodes = root.getChildNodes();
 
             // Loop through the Child Nodes to find required elements
             for (int i = 0; i < nodes.getLength(); i++) {
 
                 Node node = nodes.item(i);
 
                 if (node.getNodeType() == Node.ELEMENT_NODE) {
                     if ("project".equals(node.getNodeName())) {
                         NodeList projects = node.getChildNodes();
                         for (int j = 0; j < projects.getLength(); j++) {
                             Node projectNode = projects.item(j);
                             if( projectNode.getNodeType() == Node.ELEMENT_NODE ){
                                 if("packaging".equals(projectNode.getNodeName()) && "pom".equals(projectNode.getFirstChild().getNodeValue())) {
                                     return;
                                 }
                             }
                         }
                     } else if ("build".equals(node.getNodeName())) {
                         parseBuildElements(node);
                     } else if ("dependencies".equals(node.getNodeName())) {
                         parseDependencies(node);
                     } else if ("properties".equals(node.getNodeName())) {
                         libraryProjectCheck(node);
                     }
                 }
             }
             
             // Write the main application file
             File dir = new File(projectLocation + sourceDirectory);
             if (dir.isDirectory()) {
                 File[] files = dir.listFiles();
                 for (File f : files) {
                     if (f.getName().contains(".as")) {
                         mainApplicationFile = f.getName();
                     }
                 }
             }
             
             // Write the .actionScriptProperties file
             writeActionScriptProperties();
             
             // Write the .project file
             writeProjectProperties();
 
             // If the project is a flex froject, write the .flexLibProperties file
             if(isLibraryProject == true){
                 writeFlexLibraryProperties();
             }
            
         } catch (SAXParseException err) {
             getLog().error( "// XML Parsing Error: " + err.getLineNumber() + ", uri: " + err.getSystemId() + "\n// Error Message:\n\n" + err.getMessage() );
         } catch (SAXException e) {
             Exception x = e.getException();
             if( x == null ){
                 e.printStackTrace();
             } else {
                 x.printStackTrace();
             }
         } catch (Throwable t) {
             t.printStackTrace();
         }
     }
     
     /**
      * 
      * @throws MojoExecutionException 
      */
     private void writeActionScriptProperties() throws MojoExecutionException{
         
         String swfVersion = isLibraryProject ? "-locale en_US -swf-version=13" : "-swf-version=13";
         String libs = "";
         
         if( mIncludes != null ){
             libs = " -include-libraries+=";
             for (Iterator<String> it = mIncludes.iterator(); it.hasNext();) {
                 String lib = it.next();
                 libs += lib + ",";
             }
         }
         
         File actionScriptProperties = new File( projectLocation, ".actionScriptProperties" );
         FileWriter w = null;
         
         try{
             w = new FileWriter( actionScriptProperties );
             w.write("<?xml version='1.0' encoding='UTF-8' standalone='no'?>\n" +
                     "<actionScriptProperties analytics='false' mainApplicationPath='" + mainApplicationFile + "' projectUUID='a03ae41b-a6a9-4d0a-83d9-962b5ea5c7fb' version='10'>\n" +
                     "<compiler additionalCompilerArguments='" + swfVersion + libs + conditionalCompiler + "' autoRSLOrdering='true' copyDependentFiles='true' flexSDK='" + mFlexSDKVersion + "' fteInMXComponents='false' generateAccessible='true' htmlExpressInstall='true' htmlGenerate='true' htmlHistoryManagement='true' htmlPlayerVersionCheck='true' includeNetmonSwc='false' outputFolderPath='bin-debug' removeUnusedRSL='true' sourceFolderPath='" + sourceDirectory + "' strict='true' targetPlayerVersion='" + mTargetPlayerVersion + "' useApolloConfig='false' useDebugRSLSwfs='true' verifyDigests='true' warn='true'>\n" +
                     "<compilerSourcePath>\n" + 
                         "<compilerSourcePathEntry kind='1' linkType='1' path='" + testSourceDirectory + "' />\n" +
                         resourceDirectories + "\n" +
                     "</compilerSourcePath>\n" +
                     "<libraryPath defaultLinkType='0'>\n" +
                         "<libraryPathEntry kind='4' path=''>\n" +
                         "<excludedEntries>\n" + 
                             "<libraryPathEntry kind='3' linkType='1' path='${PROJECT_FRAMEWORKS}/libs/flex.swc' useDefaultLinkType='false'/>\n" +
                         "</excludedEntries>\n" +
                     "</libraryPathEntry>\n" +
                     mLibraries.toString().replace(",", "\n").replace("[", "").replace("]", "") +
                     "\n</libraryPath>\n" + 
                     "<sourceAttachmentPath>\n" +
                     mAttachments.toString().replace(",", "\n").replace("[", "").replace("]", "") + 
                     "\n</sourceAttachmentPath>\n" +
                     "</compiler>\n" +
                     "<applications>\n" +
                     "<application path='" + mainApplicationFile + "'/>\n" +
                     "</applications>\n" +
                     "<modules/>\n" +
                     "<buildCSSFiles/>\n" +
                     "<flashCatalyst validateFlashCatalystCompatibility='false'/>\n" + 
                     "</actionScriptProperties>"
             );
         } catch( IOException e ) {
             throw new MojoExecutionException("Error creating file: " + actionScriptProperties, e );
         } finally {
             if( w != null ){
                 try{
                     w.close();
                 } catch( IOException e ){
                     // ignore
                 }
             }
         }
     }
     
     /**
      * 
      * @throws MojoExecutionException 
      */
     private void writeProjectProperties() throws MojoExecutionException{
         
         File project = new File( projectLocation, ".project" );
         FileWriter w = null;
         
         String flexProperty = isLibraryProject ? "<nature>com.adobe.flexbuilder.project.flexlibnature</nature>\n" : "";
         String[] projectName = projectLocation.split("/");
         
         try{
             w = new FileWriter( project );
             w.write("<?xml version='1.0' encoding='UTF-8'?>\n" +
                     "<projectDescription>\n" + 
                     "<name>" + projectName[projectName.length -1] + "</name>\n" + 
                     "<comment></comment>\n" + 
                     "<projects></projects>\n" +
                     "<buildSpec>\n" +
                     "<buildCommand>\n" +
                     "<name>com.adobe.flexbuilder.project.flexbuilder</name>\n" +
                     "<arguments></arguments>\n" +
                     "</buildCommand>\n" +
                     "<buildCommand>\n" + 
                     "<name>org.maven.ide.eclipse.maven2Builder</name>\n" + 
                     "<arguments></arguments>\n" +
                     "</buildCommand>\n" + 
                     "</buildSpec>\n" + 
                     "<natures>\n" + 
                     "<nature>org.maven.ide.eclipse.maven2Nature</nature>\n" + 
                     flexProperty +
                     "<nature>com.adobe.flexbuilder.project.actionscriptnature</nature>\n" +
                     "</natures>\n" +
                     "</projectDescription>");
             
         } catch( IOException e ) {
             throw new MojoExecutionException("Error creating file: " + project, e );
         } finally {
             if( w != null ){
                 try{
                     w.close();
                 } catch( IOException e ){
                     // ignore
                 }
             }
         }
     }
     
     /**
      * 
      * @throws MojoExecutionException 
      */
     private void writeFlexLibraryProperties() throws MojoExecutionException{
         
         File flexLibsProperties = new File( projectLocation, ".flexLibProperties" );
         FileWriter w = null;
         
         String nameSpaceManifest = "<namespaceManifests/>\n";
         
         if( resourceDirectories.length() == 0 && hasManifestFile ){
             nameSpaceManifest =  "<namespaceManifests>\n<namespaceManifestEntry manifest='manifest.xml' namespace='" + namespaceURI + "'/>\n</namespaceManifests>";
         }
         
         try{
             w = new FileWriter( flexLibsProperties );
             w.write("<?xml version='1.0' encoding='UTF-8' standalone='no'?>\n" +
                     "<flexLibProperties includeAllClasses='true'  useMultiPlatformConfig='false' version='3'>\n" + 
                     "<includeClasses/>\n" +
                     "<includeResources/>\n" +
                     nameSpaceManifest +
                     "</flexLibProperties>");
             
         } catch( IOException e ) {
             throw new MojoExecutionException("Error creating file: " + flexLibsProperties, e );
         } finally {
             if( w != null ){
                 try{
                     w.close();
                 } catch( IOException e ){
                     // ignore
                 }
             }
         }
         
     }
 
     /**
      * 
      * Parse the Build node of the effective POM
      * 
      * @param buildNode 
      */
     private void parseBuildElements(Node buildNode) {
         
         // Set the children nodes from the buildNode node
         NodeList nodes = buildNode.getChildNodes();
 
         // Loop through the Child Nodes to find required elements
         for (int i = 0; i < nodes.getLength(); i++) {
             Node node = nodes.item(i);
             
             if (node.getNodeType() == Node.ELEMENT_NODE) {
                 if ("sourceDirectory".equals(node.getNodeName())) {
                     sourceDirectory = performJoin(node.getFirstChild().getNodeValue(), projectLocation);
                 } else if ("testSourceDirectory".equals(node.getNodeName())) {
                     testSourceDirectory = performJoin(node.getFirstChild().getNodeValue(), projectLocation);
                 } else if ("resources".equals(node.getNodeName())) {
                     NodeList resourceNodes = node.getChildNodes();
                     for (int a = 0; a < resourceNodes.getLength(); a++) {
                         Node resourceNode = resourceNodes.item(a);
                         if( resourceNode.getNodeType() == Node.ELEMENT_NODE ){
                             parseResourceElement(resourceNode);
                         }
                     }
                 } else if ("plugins".equals(node.getNodeName())) {
                     NodeList pluginNodes = node.getChildNodes();
                     for (int j = 0; j < pluginNodes.getLength(); j++) {
                         Node pluginNode = pluginNodes.item(j);
                         if(pluginNode.getNodeType() == Node.ELEMENT_NODE){
                             parsePlugin(pluginNode);
                         }
                     }
                 }
             }
         }
     }
     
     /**
      * 
      * @param pluginNode 
      */
     private void parsePlugin(Node pluginNode) {
         
         NodeList nodes = pluginNode.getChildNodes();
         
         for (int i = 0; i < nodes.getLength(); i++) {
             Node node = nodes.item(i);
             if( node.getNodeType() == Node.ELEMENT_NODE ){
                 if( "configuration".equals(node.getNodeName())){
                     checkHasManifestFile(node);
                     if( hasManifestFile == false ){
                         NodeList configurationNodes = node.getChildNodes();
                         for (int j = 0; j < configurationNodes.getLength(); j++) {
                             Node configurationNode = configurationNodes.item(j); 
                             if( configurationNode.getNodeType() == Node.ELEMENT_NODE ){
                                 if("defines".equals(configurationNode.getNodeName())){
                                     initConditionalCompilerSettings(configurationNode);
                                 }
                             }
                         }
                     }
                 }
             }
         } 
     }
     
     /**
      * 
      * @param node 
      */
     private void initConditionalCompilerSettings( Node configurationNode ){
        
         String propertyName = "";
         String propertyValue = "";
         
         NodeList propertyNodes = configurationNode.getFirstChild().getNextSibling().getChildNodes();
         for (int i = 0; i < propertyNodes.getLength(); i++) {
             Node propertyNode = propertyNodes.item(i);
             if(propertyNode.getNodeType() == Node.ELEMENT_NODE){
                 if("name".equals(propertyNode.getNodeName())){
                     propertyName = propertyNode.getFirstChild().getNodeValue();
                 } else if("value".equals(propertyNode.getNodeName())){
                     propertyValue = propertyNode.getFirstChild().getNodeValue();
                 }
             }
         }
         conditionalCompiler = " -define=" + propertyName + "," + propertyValue;
     }
     
     /**
      * 
      * @param plugin 
      */
     private Boolean checkHasManifestFile( Node plugin ) {
         
         NodeList nodes = plugin.getChildNodes();
         
         for (int i = 0; i < nodes.getLength(); i++) {
             
             Node node = nodes.item(i);
             
             if( node.getNodeType() == Node.ELEMENT_NODE ){
                 if("namespaces".equals(node.getNodeName())){
                     
                     NodeList namespaceNodes = node.getChildNodes();
                     
                     for (int j = 0; j < namespaceNodes.getLength(); j++) {
                         
                         Node namespaceNode = namespaceNodes.item(i);
                         
                         if( namespaceNode.getNodeType() == Node.ELEMENT_NODE ){
                             if("namespace".equals(namespaceNode.getNodeName())){
                                 hasManifestFile = true;
                                 initNamespaceURI(namespaceNode);
                             }
                         }
                     }
                 }
             }
         }
         return hasManifestFile;
     }
     
     /**
      * 
      * @param namespaceNode 
      */
     private void initNamespaceURI( Node namespaceNode ){
         
         NodeList nodes = namespaceNode.getChildNodes();
         
         for (int i = 0; i < nodes.getLength(); i++) {
             Node node = nodes.item(i);
             if( node.getNodeType() == Node.ELEMENT_NODE ){
                 if("uri".equals(node.getNodeName())){
                     namespaceURI = node.getFirstChild().getNodeValue();
                 }
             }
         }
     }
 
     /**
      * 
      * Parse the dependencies
      * 
      * @param dependenciesNode 
      */
     private void parseDependencies(Node dependenciesNode) {
 
         String type = "";
         String artifactID = "";
         String version = "";
         String groupID = "";
         String scope = "";
         String attachmentSourcePath = "";
         
         Boolean exclude = false;
 
         // Set the children nodes from the dependenciesNode
         NodeList nodes = dependenciesNode.getChildNodes();
 
         // Loop through the child nodes to find the required elements
         for (int i = 0; i < nodes.getLength(); i++) {
 
             Node node = nodes.item(i);
 
             if (node.getNodeType() == Node.ELEMENT_NODE) {
                 
                 NodeList dependencyNodes = node.getChildNodes();
 
                 for (int a = 0; a < dependencyNodes.getLength(); a++) {
                     Node dependencyNode = dependencyNodes.item(a);
 
                     if (dependencyNode.getNodeType() == Node.ELEMENT_NODE) {
                         if( "type".equals(dependencyNode.getNodeName())) {
                             type = dependencyNode.getFirstChild().getNodeValue();
                         } else if ("artifactId".equals(dependencyNode.getNodeName())) {
                             artifactID = dependencyNode.getFirstChild().getNodeValue();
                         } else if ("groupId".equals(dependencyNode.getNodeName())) {
                             groupID = dependencyNode.getFirstChild().getNodeValue();
                         } else if ("version".equals(dependencyNode.getNodeName())) {
                             version = dependencyNode.getFirstChild().getNodeValue();
                         } else if ("scope".equals(dependencyNode.getNodeName())) {
                             scope = dependencyNode.getFirstChild().getNodeValue();
                         }
                     }
                 }
 
                 // Run the check to see if the dependency is to be excluded
                 // Set the exclude flag to determine if the dependency is needed
                 exclude = checkDependencyExclusion(groupID);
 
                 if (!exclude) {
                     localRepositoryPath = homeDirectory + "/.m2/repository/"
                             + convertToPath(groupID) + "/" + artifactID + "/" + version + "/"
                             + artifactID + "-" + version + ".swc";
                 }
 
                 // Check if the scope is internal, and add it to the Includes ArrayList
                 if ("internal".equals(scope)) {
                     if (mIncludes == null) {
                         mIncludes = new ArrayList<String>();
                     }
 
                     mIncludes.add(localRepositoryPath);
                 }
 
                 File dir = new File(projectLocation + "../.metadata/.plugins/org.eclipse.core.resources/.projects/" + artifactID);
 
                 if( mAttachments == null ){
                     mAttachments = new ArrayList<String>();
                 }
                 
                 if (dir.isDirectory()) {
                     attachmentSourcePath = "${DOCUMENTS}/" + artifactID + "/" + sourceDirectory;
                     mAttachments.add("<sourceAttachmentPathEntry kind='3' linkType='1' path='"
                             + localRepositoryPath + "' sourcepath='" + attachmentSourcePath + "' useDefaultLinkType='false' />");
                 }
 
                 if (mLibraries == null) {
                     mLibraries = new ArrayList<String>();
                 }
                 
                 if( !exclude && "swc".equals(type)){
                     mLibraries.add("<libraryPathEntry kind='3' linkType='1' path='" + localRepositoryPath
                         + "' sourcepath='" + attachmentSourcePath + "' useDefaultLinkType='false' />");
                 }                   
             }
         }
     }
 
     /**
      * 
      * Check if the dependency is meant to be excluded
      * 
      * @param value
      * @return 
      */
     private Boolean checkDependencyExclusion(String value) {
 
         Boolean exclude = false;
 
         for (int i = 0; i < mExcludes.size(); i++) {
 
             Boolean stringComparison = value.equals(mExcludes.get(i));
 
             if (stringComparison == true) {
                 exclude = true;
                 break;
             }
         }
         //
         return exclude;
     }
 
     /**
      * 
      * Add the following slash to any given string
      * 
      * @param path
      * @return 
      */
     private String addFollowingSlash(String path) {
 
         if (!"/".equals(Character.toString(path.charAt(path.length() - 1)))) {
             return path + "/";
         } else {
             return path;
         }
     }
     
     /**
      * 
      * @param value
      * @return 
      */
     private String convertToPath(String value) {
         return value.replace(".", "/");
     }
     
     private String performJoin( String value, String split ){
         
         String[] splits;
         String returnValue = "";
         
         splits = value.split(split);
         
         for (String s : splits) {
             returnValue += s;
         }
         return returnValue;
     }
 
     /**
      * 
      * @param propertiesNode
      * @return 
      */
     private Boolean libraryProjectCheck(Node propertiesNode) {
 
         NodeList nodes = propertiesNode.getChildNodes();
        
         for (int i = 0; i < nodes.getLength(); i++) {
             Node node = nodes.item(i);
            
             if (node.getNodeType() == Node.ELEMENT_NODE) {
                if ("library".equals(node.getNodeName())) {
                    if( "true".equals(node.getFirstChild().getNodeValue())){
                        isLibraryProject = true;
                    } else {
                        isLibraryProject = false;
                    }
                 }
             }
         }
        
         return isLibraryProject;
     }
 
     /**
      * 
      * Parse the resource path directory
      * 
      * @param node 
      */
     private void parseResourceElement(Node node) {
 
         NodeList resourceNodes = node.getChildNodes();
 
         String[] splits;
         String loc = "";
 
         for (int i = 0; i < resourceNodes.getLength(); i++) {
             Node resourceNode = resourceNodes.item(i);
             if (resourceNode.getNodeType() == Node.ELEMENT_NODE && "directory".equals(resourceNode.getNodeName())) {
                 splits = resourceNode.getFirstChild().getNodeValue().split(projectLocation);
                 if (splits.length > 1) {
                     for (String s : splits) {
                         loc += s;
                     }
                 } else {
                     splits = resourceNode.getFirstChild().getNodeValue().split(workspaceLocation);
                     if (splits.length > 1) {
                         loc = "../";
                         for (String s : splits) {
                             loc += s;
                         }
                     }
                 }
             }
         }
         resourceDirectories = "<compilerSourcePathEntry kind='1' linkType='1' path='" + loc + "' />";
     }
     
     /**
      * The Libraries ArrayList
      *
      * @parameter property="libraries"
      */
     private ArrayList<String> mLibraries;
 
     public void setLibraries(ArrayList<String> libraries) {
         mLibraries = libraries;
     }
     
     /**
      * The Excludes ArrayList
      *
      * @parameter property="excludes"
      */
     private ArrayList<String> mExcludes;
 
     public void setExcludes(ArrayList<String> excludes) {
         mExcludes = excludes;
     }
     
     /**
      * The Includes ArrayList
      *
      * @parameter property="includes"
      */
     private ArrayList<String> mIncludes;
 
     public void setIncludes(ArrayList<String> includes) {
         mIncludes = includes;
     }
     
     /**
      * The Attachments ArrayList
      *
      * @parameter property="attachments"
      */
     private ArrayList<String> mAttachments;
 
     public void setAttachments(ArrayList<String> attachments) {
         mAttachments = attachments;
     }
     
     /**
      * Set the Flex SDK Version Externally, 
      * Default Flex SDK Version is set to 'Flex 4.5.1'
      *
      * @parameter property="flexSDKVersion"
      */
     private String mFlexSDKVersion = "Flex 4.5.1";
     
     public void setFlexSDKVersion( String flexSDKVersion ) {
         mFlexSDKVersion = flexSDKVersion;
     }
     
     /**
      * Set the Target Player Version, 
      * Default Player Version is set to '11.0.0'
      *
      * @parameter property="targetPlayerVersion"
      */
     private String mTargetPlayerVersion = "11.0.0";
     
     public void setFlashPlayerVersion( String targetPlayerVersion ) {
         mTargetPlayerVersion = targetPlayerVersion;
     }
 }
