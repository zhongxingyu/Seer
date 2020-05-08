 /*
  * Copyright (c) 2012 Jim Svensson <jimpa@tla.se>
  *
  * Permission to use, copy, modify, and distribute this software for any
  * purpose with or without fee is hereby granted, provided that the above
  * copyright notice and this permission notice appear in all copies.
  *
  * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
  * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
  * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
  * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
  * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
  * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
  * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
  */
 
 package se.tla.mavenversionbumper;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.log4j.Logger;
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.JDOMException;
 import org.jdom.Namespace;
 import org.jdom.input.SAXBuilder;
 import org.jdom.output.XMLOutputter;
 
 /**
  * Represents a Maven project file, pom.xml.
  *
  * It contains methods to easily view and manipulate dependency information.
  */
 public class Module {
 
     private final Logger logger;
 
     private static final String SNAPSHOTPATTERN = "-SNAPSHOT";
    private final Document document;
    private final File pomFile;
    protected final Element root;
    protected final Namespace nameSpace;
    protected final String moduleName;
     protected final String originalVersion;
     private String label;
     private String commitMessage;
     private boolean labelOnlyPomXml = false;
 
     /**
      * Constructor.
      *
      * @param baseDirName Filename of the base directory of the Maven module.
      * @param moduleName The symbolic name of the Maven module.
      * @throws JDOMException Problem reading the pom.xml file.
      * @throws IOException Problem reading the pom.xml file.
      */
     public Module(String baseDirName, String moduleName) throws JDOMException, IOException {
         if (moduleName == null) {
             moduleName = "";
         }
         this.moduleName = moduleName;
         File dir = openDir(null, baseDirName);
         if (moduleName.length() > 0) {
             dir = openDir(dir, moduleName);
         }
         pomFile = new File(dir, "pom.xml");
         SAXBuilder builder = new SAXBuilder();
         document = builder.build(pomFile);
         root = document.getRootElement();
         nameSpace = root.getNamespace();
         Element version = root.getChild("version", nameSpace);
         if (version != null) {
             originalVersion = version.getText();
         } else {
             originalVersion = null;
         }
         logger = Logger.getLogger("Updated " + ga());
     }
 
     /**
      * Constructor used only by the sub class ReadonlyModule to satisfy the compiler. Should never be used.
      */
     protected Module() {
         document = null;
         pomFile = null;
         root = null;
         nameSpace = null;
         moduleName = null;
         originalVersion = null;
         logger = null;
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
 
     /**
      * @return GA-coordinates. GroupId, ArtifactId.
      */
     public String ga() {
         return groupId() + ":" + artifactId();
     }
 
     public String groupId() {
         return myOrParent("groupId");
     }
 
     public String artifactId() {
         return root.getChildText("artifactId", nameSpace);
     }
 
     public String version() {
         return myOrParent("version");
     }
 
     /**
      * @param newVersion New Version.
      */
     public void version(String newVersion) {
         if (originalVersion == null) {
             throw new IllegalStateException("Modules version can't be updated since it has no version of its own.");
         }
         if (commitMessage == null) {
             commitMessage = "Bump " + originalVersion + " -> " + newVersion;
         }
         Element versionElement = root.getChild("version", nameSpace);
         String existingVersion = versionElement.getText();
         if (! existingVersion.equals(newVersion)) {
             logger.info("version: " + existingVersion + " -> " + newVersion);
             versionElement.setText(newVersion);
         }
     }
 
     public String parentVersion() {
         Element parent = root.getChild("parent", nameSpace);
         if (parent == null) {
             return null;
         }
         return parent.getChildText("version", nameSpace);
     }
 
     /**
      * @param newParentVersion New parentVersion.
      */
     public void parentVersion(String newParentVersion) {
         Element parent = root.getChild("parent", nameSpace);
         if (parent == null) {
             throw new IllegalArgumentException("No parent defined in module " + ga());
         }
         Element version = parent.getChild("version", nameSpace);
         if (version == null) {
             throw new IllegalStateException("No version defined for parent in module " + ga());
         }
 
         String existingParentVersion = version.getText();
         if (! newParentVersion.equals(existingParentVersion)) {
             logger.info("parent version: " + existingParentVersion + " -> " + newParentVersion);
             version.setText(newParentVersion);
         }
     }
 
     /**
      * Update the parent version to that of this Module.
      *
      * @param newParent Use this modules version.
      */
     public void parentVersion(Module newParent) {
         Element existingParent = root.getChild("parent", nameSpace);
         if (existingParent == null) {
             throw new IllegalArgumentException("No parent in module " + ga() + ". Can't update version.");
         }
         String existingGroupId = existingParent.getChild("groupId", nameSpace).getText();
         String existingArtifactId = existingParent.getChild("artifactId", nameSpace).getText();
         if ((! newParent.groupId().equals(existingGroupId)) || (! newParent.artifactId().equals(existingArtifactId))) {
             throw new IllegalArgumentException("No such parent in " + ga() + ": " + newParent.ga());
         }
 
         Element version = existingParent.getChild("version", nameSpace);
         if (version == null) {
             throw new IllegalStateException("No version defined for parent.");
         }
 
         String existingParentVersion = version.getText();
         String newParentVersion = newParent.version();
         if (! newParentVersion.equals(existingParentVersion)) {
             logger.info("parent version: " + existingParentVersion + " -> " + newParentVersion);
             version.setText(newParentVersion);
         }
     }
 
     private String myOrParent(String itemName) {
         String itemValue = root.getChildText(itemName, nameSpace);
         if (itemValue != null) {
             return itemValue;
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
      * If the found dependency refers to a property for the version, an updateProperty is tried on that property name.
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
             throw new IllegalArgumentException("No such dependency found in " + ga() + ": " + moduleToUpdate.ga());
         }
 
         Element version = dep.getChild("version", nameSpace);
         if (version == null) {
             throw new IllegalArgumentException("In " + ga() + ", no version defined for " + moduleToUpdate.ga() +
             ". Probably defined elsewhere in a dependencyManagement.");
         }
 
         String versionText = version.getText();
         if (versionText.startsWith("${") && versionText.endsWith("}")) {
             String propertyName = versionText.substring(2).substring(0, versionText.length() - 3);
 
             updateProperty(propertyName, moduleToUpdate.version());
             return;
         }
 
         String existingVersion = version.getText();
         if (! existingVersion.equals(moduleToUpdate.version())) {
             logger.info("update dependency version " + moduleToUpdate.ga() + ": " + existingVersion + " -> " + moduleToUpdate.version());
             version.setText(moduleToUpdate.version());
         }
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
             throw new IllegalArgumentException("No such plugin dependency found in " + ga() + ": " + pluginToUpdate.ga());
         }
 
         Element version = dep.getChild("version", nameSpace);
         if (version == null) {
             throw new IllegalArgumentException("In " + ga() + ", no version defined for " + pluginToUpdate.ga() +
                     ". Probably defined elsewhere in a pluginManagement.");
         }
 
         String versionText = version.getText();
         if (versionText.startsWith("${") && versionText.endsWith("}")) {
             String propertyName = versionText.substring(2).substring(0, versionText.length() - 3);
 
             updateProperty(propertyName, pluginToUpdate.version());
             return;
         }
 
         String existingVersion = version.getText();
         if (! existingVersion.equals(pluginToUpdate.version())) {
             logger.info("update plugin dependency version " + pluginToUpdate.ga() + ": " + existingVersion + " -> " + pluginToUpdate.version());
             version.setText(pluginToUpdate.version());
         }
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
             throw new IllegalArgumentException("No properties defined in module " + ga());
         }
 
         Element property = properties.getChild(propertyName, nameSpace);
         if (property == null) {
             throw new IllegalArgumentException("No property " + propertyName + " defined in module " + ga());
         }
 
         String existingProperty = property.getText();
         if (! existingProperty.equals(value)) {
             logger.info("update property " + propertyName + ": " + existingProperty + " -> " + value);
             property.setText(value);
         }
     }
 
     /**
      * Save this module back to its original pom.xml file.
      *
      * If a VersionControl was provided while creating this Module, the pom.xml is first
      * committed and then, optionally, labeled.
      *
      * @throws IOException in case of IO-related problems.
      */
     public void save() throws IOException {
         XMLOutputter o = new XMLOutputter();
         // TODO Make sure that the line endings are preserved.
         o.getFormat().setLineSeparator("\n"); // Nicht funktioniren
         // TODO Make sure that the character encoding of the pom.xml is preserved.
         FileUtils.write(pomFile, o.outputString(document), "utf-8");
     }
 
     /**
      * Apply this label to the Module when it is saved. Requires that a VersionControl was provided to work.
      *
      * @param label Label.
      */
     public void label(String label) {
         logger.info("label: " + label);
         this.label = label;
     }
 
     public String label() {
         return label;
     }
 
     /**
      * Use this commit message if a commit to VersionControl is performed. If no custom message is provided,
      * a default message is used.
      *
      * @param commitMessage Custom message to use during a commit.
      */
     public void commitMessage(String commitMessage) {
         logger.info("commit message " + commitMessage);
         this.commitMessage = commitMessage;
     }
 
     /**
      * @return The message to use during a commit.
      */
     public String commitMessage() {
         return commitMessage;
     }
 
     private Element findDependencyElement(Module moduleToFind, String ... path) {
         for (Element dep : getChildElements(path)) {
             String groupId = dep.getChildText("groupId", nameSpace);
             String artifactId = dep.getChildText("artifactId", nameSpace);
 
             if (moduleToFind.groupId().equals(groupId) && moduleToFind.artifactId().equals(artifactId)) {
                 return dep;
             }
         }
 
         return null;
     }
 
     private List<Element> getChildElements(String... path) {
         Element cur = root;
 
         for (String pathPart : path) {
             cur = cur.getChild(pathPart, nameSpace);
             if (cur == null) {
                 return new LinkedList<Element>();
             }
         }
 
         return (List<Element>) cur.getChildren();
     }
 
     public List<String> findSnapshots() {
         List<String> result = new LinkedList<String>();
 
         // Own version
         if (version().endsWith(SNAPSHOTPATTERN)) {
             result.add("Module version");
         }
 
         // Parent Version
         String parentVersion = parentVersion();
         if (parentVersion != null && parentVersion.endsWith(SNAPSHOTPATTERN)) {
             result.add("Parent version " + parentVersion);
         }
 
         // Properties
         Element properties = root.getChild("properties", nameSpace);
         if (properties != null) {
             for (Element child : (List<Element>) properties.getChildren()) {
                 String text = child.getText();
                 if (text != null && text.endsWith(SNAPSHOTPATTERN)) {
                     result.add("Property " + child.getName() + ":" + text);
                 }
             }
         }
 
         // Dependencies
         for (Element dep : getChildElements("dependencies")) {
             String version = extractText(dep, "version");
             if (version != null && version.endsWith(SNAPSHOTPATTERN)) {
                 result.add("Dependency " + extractText(dep, "groupId") + ":" + extractText(dep, "artifactId") + ":" + version);
             }
         }
 
         // Dependency management
         for (Element dep : getChildElements("dependencyManagement", "dependencies")) {
             String version = extractText(dep, "version");
             if (version != null && version.endsWith(SNAPSHOTPATTERN)) {
                 result.add("Dependency management " + extractText(dep, "groupId") + ":" + extractText(dep, "artifactId") + ":" + version);
             }
         }
 
         // Plugins
         for (Element dep : getChildElements("build", "plugins")) {
             String version = extractText(dep, "version");
             if (version != null && version.endsWith(SNAPSHOTPATTERN)) {
                 result.add("Plugin " + extractText(dep, "groupId") + ":" + extractText(dep, "artifactId") + ":" + version);
             }
         }
 
         // Plugin management
         for (Element dep : getChildElements("build", "pluginManagement", "plugins")) {
             String version = extractText(dep, "version");
             if (version != null && version.endsWith(SNAPSHOTPATTERN)) {
                 result.add("Plugin management " + extractText(dep, "groupId") + ":" + extractText(dep, "artifactId") + ":" + version);
             }
         }
 
         return result;
     }
 
     private String extractText(Element dep, String elementName) {
         return dep.getChildText(elementName, nameSpace);
     }
 
     /**
      * Controls how much will be labeled if labeling is required.
      *
      * Please observe that this functionality isn't implemented in all VersionControl implementations since
      * they simply don't support labeling of sub trees.
      *
      * @param labelOnlyPomXml If false (default) label whole Module recursively, if true only label the pom.xml.
      */
     public void labelOnlyPomXml(boolean labelOnlyPomXml) {
         logger.info("label only pom.xml");
         this.labelOnlyPomXml = labelOnlyPomXml;
     }
 
     /**
      * @return If true, only label the pom.xml, if false, label whole file tree recursively, including any sub module..
      */
     public boolean labelOnlyPomXml() {
         return labelOnlyPomXml;
     }
 
     /**
      * @return The modules pom.xml file.
      */
     public File pomFile() {
         return pomFile;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public String toString() {
         return moduleName;
     }
 }
