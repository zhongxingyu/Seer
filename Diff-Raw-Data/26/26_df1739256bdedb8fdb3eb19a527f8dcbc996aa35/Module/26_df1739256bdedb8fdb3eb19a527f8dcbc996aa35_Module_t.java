 package se.tla.mavenversionbumper;
 
 import org.apache.commons.io.FileUtils;
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.JDOMException;
 import org.jdom.Namespace;
 import org.jdom.input.SAXBuilder;
 import org.jdom.output.XMLOutputter;
 import se.tla.mavenversionbumper.vcs.VersionControl;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 
 /**
  * Represents a Maven project file, pom.xml.
  *
  * It contains methods to easily view and manipulate dependency information.
  */
 public class Module {
     final private Document document;
     final private File pomFile;
     final private Element root;
     final private Namespace nameSpace;
     final private String moduleName;
     final private VersionControl versionControl;
     final private String originalVersion;
     private String label;
     private String commitMessage;
     private boolean labelOnlyPomXml = false;
 
     /**
      * Constructor.
      *
      * @param baseDirName Filename of the base directory of the Maven module.
      * @param moduleName The symbolic name of the Maven module.
      * @param versionControl Optional VersionControl implementation to use.
      * @throws JDOMException
      * @throws IOException
      */
     public Module(String baseDirName, String moduleName, VersionControl versionControl) throws JDOMException, IOException {
         this.moduleName = moduleName;
         this.versionControl = versionControl;
         File dir = openDir(null, baseDirName);
         if (moduleName.length() > 0) {
             dir = openDir(dir, moduleName);
         }
         File baseDir = dir;
         pomFile = new File(baseDir, "pom.xml");
         SAXBuilder builder = new SAXBuilder();
         document = builder.build(pomFile);
         root = document.getRootElement();
         nameSpace = root.getNamespace();
         originalVersion = version();
     }
 
     private File openDir(File base, String name) {
         File dir;
         if (base != null) {
              dir = new File(base, name);
         } else {
             dir = new File(name);
         }
         if (!dir.isDirectory()) {
             throw new IllegalArgumentException("No such directory: " + dir.getName());
         }
         return dir;
     }
 
     /**
      * @return GAV-coordinates. GroupId, ArtifactId, Version.
      */
     public String gav() {
         return groupId() + ":" + artifactId() + ":" + version();
     }
 
     public String groupId() {
         return myOrParent("groupId");
     }
 
     /**
      * @param groupId New GroupId.
      */
     public void groupId(String groupId) {
         root.getChild("groupId", nameSpace).setText(groupId);
     }
 
     public String artifactId() {
         return root.getChildText("artifactId", nameSpace);
     }
 
     /**
      * @param artifactId New ArtifactId.
      */
     public void artifactId(String artifactId) {
         root.getChild("artifactId", nameSpace).setText(artifactId);
     }
 
     public String version() {
         return myOrParent("version");
     }
 
     /**
      * @param version New Version.
      */
     public void version(String version) {
         root.getChild("version", nameSpace).setText(version);
     }
 
     public String parentVersion() {
         Element parent = root.getChild("parent", nameSpace);
         if (parent == null) {
             return null;
         }
         return parent.getChildText("version", nameSpace);
     }
 
     /**
      * @param parentVersion New parentVersion.
      */
     public void parentVersion(String parentVersion) {
         Element parent = root.getChild("parent", nameSpace);
         if (parent == null) {
             throw new IllegalArgumentException("No parent defined in module");
         }
         Element version = parent.getChild("version", nameSpace);
         if (version == null) {
             throw new IllegalStateException("No version defined for parent.");
         }
         version.setText(parentVersion);
     }
 
     /**
      * Update the parent version to that of this Module.
      *
      * @param parent
      */
     public void parentVersion(Module parent) {
         parentVersion(parent.version());
     }
 
     private String myOrParent(String itemName) {
         String item = root.getChildText(itemName, nameSpace);
         if (item != null) {
             return item;
         }
         Element parent = root.getChild("parent", nameSpace);
         if (parent == null) {
             return null;
         }
         return parent.getChildText(itemName, nameSpace);
     }
 
     /**
      * Find this module in either the modules dependency management list or in the dependency list.
      *
      * @param moduleToUpdate Module to find and update version for.
      * @throws IllegalArgumentException If the moduleToUpdate can't be found in either list.
      */
     public void updateDependency(Module moduleToUpdate) {
 
         // Look in dependencyManagement
         Element dep = findDependencyElement(moduleToUpdate, "dependencyManagement", "dependencies");
 
         if (dep == null) {
             // Look i dependencies
             dep = findDependencyElement(moduleToUpdate, "dependencies");
         }
 
         if (dep == null) {
             throw new IllegalArgumentException("No such dependency found");
         }
 
         Element version = dep.getChild("version", nameSpace);
         if (version == null) {
             throw new IllegalArgumentException("In " + gav() + ", no version defined for " + moduleToUpdate.gav() +
             ". Probably defined elsewhere in a dependencyManagement.");
         }
 
         if (version.getText().startsWith("${") && version.getText().endsWith("}")) {
             throw new IllegalArgumentException("In " + gav() + ", the dependency to " + moduleToUpdate.gav() +
                     "'s version is controlled by a property. Use updateProperty instead.");
         }
 
         version.setText(moduleToUpdate.version());
     }
 
     /**
      * Find this plugin in either the modules plugin management list or in the plugin list.
      *
      * @param pluginToUpdate Plugin to find and update version for.
      * @throws IllegalArgumentException If the moduleToUpdate can't be found in either list.
      */
     public void updatePluginDependency(Module pluginToUpdate) {
 
         // Look in pluginManagement
         Element dep = findDependencyElement(pluginToUpdate, "build", "pluginManagement", "plugins");
 
         if (dep == null) {
             // Look i plugins
             dep = findDependencyElement(pluginToUpdate, "build", "plugins");
         }
 
         if (dep == null) {
             throw new IllegalArgumentException("No such plugin dependency found");
         }
 
         Element version = dep.getChild("version", nameSpace);
         if (version == null) {
             throw new IllegalArgumentException("In " + gav() + ", no version defined for " + pluginToUpdate.gav() +
                     ". Probably defined elsewhere in a pluginManagement.");
         }
 
         if (version.getText().startsWith("${") && version.getText().endsWith("}")) {
             throw new IllegalArgumentException("In " + gav() + ", the plugin dependency to " + pluginToUpdate.gav() +
                     "'s version is controlled by a property. Use updateProperty instead.");
         }
 
         version.setText(pluginToUpdate.version());
     }
 
     /**
      * Find the named property and update its value.
      *
      * @param propertyName Name.
      * @param value Value.
      * @throws IllegalArgumentException if named property can't be found.
      */
     public void updateProperty(String propertyName, String value) {
         Element properties = root.getChild("properties", nameSpace);
         if (properties == null) {
             throw new IllegalArgumentException("No properties defined in module " + gav());
         }
 
         Element property = properties.getChild(propertyName, nameSpace);
         if (property == null) {
            throw new IllegalArgumentException("No property " + propertyName + " defined in module " + gav());
         }
 
         property.setText(value);
     }
 
     /**
      * Save this module back to its original pom.xml file.
      *
      * If a VersionControl was provided while creating this Module, the pom.xml is first
      * commited and then, optionally, labeled.
      *
      * @throws IOException in case of IO-related problems.
      */
     public void save() throws IOException {
         if (versionControl != null) {
             versionControl.prepareSave(pomFile);
         }
         XMLOutputter o = new XMLOutputter();
         o.getFormat().setLineSeparator("\n"); // Nicht funktioniren
         FileUtils.write(pomFile, o.outputString(document), "utf-8");
 
         if (versionControl != null) {
             if (commitMessage == null) {
                 commitMessage = "Bump " + originalVersion + " -> " + version();
             }
             versionControl.commit(pomFile, commitMessage);
         }
 
         if (versionControl != null) {
             versionControl.label(label, (labelOnlyPomXml ? pomFile : pomFile.getParentFile()));
         }
     }
 
     /**
      * Apply this label to the Module when it is saved. Requires that a VersionControl was provided to work.
      *
      * @param label
      * @throws IOException
      */
     public void label(String label) throws IOException {
         this.label = label;
     }
 
     /**
      * Use this commit message if a commit to VersionControl is performed. If no custom message is provided,
      * a default message is used.
      *
      * @param commitMessage
      * @throws IOException
      */
     public void commitMessage(String commitMessage) throws IOException {
         this.commitMessage = commitMessage;
     }
 
     private Element findDependencyElement(Module moduleToFind, String ... path) {
         Element cur = root;
 
         for (String pathPart : path) {
             cur = cur.getChild(pathPart, nameSpace);
             if (cur == null) {
                 return null;
             }
         }
 
         for (Element dep : (List<Element>) cur.getChildren()) {
             String groupId = dep.getChildText("groupId", nameSpace);
             String artifactId = dep.getChildText("artifactId", nameSpace);
 
             if (groupId.equals(moduleToFind.groupId()) && artifactId.equals(moduleToFind.artifactId())) {
                 return dep;
             }
         }
 
         return null;
     }
 
     @Override
     public String toString() {
         return moduleName;
     }
 
     public boolean labelOnlyPomXml() {
         return labelOnlyPomXml;
     }
 
     /**
      * Controls how much will be labeled if labeling is required.
      *
      * Please observe that this functionality isn't implemented in all VersionControl implementations since
      * they simply don't support labeling of subtrees.
      *
      * @param labelOnlyPomXml If false (default) label whole Module recursively, if true only label the pom.xml.
      */
     public void labelOnlyPomXml(boolean labelOnlyPomXml) {
         this.labelOnlyPomXml = labelOnlyPomXml;
     }
 }
