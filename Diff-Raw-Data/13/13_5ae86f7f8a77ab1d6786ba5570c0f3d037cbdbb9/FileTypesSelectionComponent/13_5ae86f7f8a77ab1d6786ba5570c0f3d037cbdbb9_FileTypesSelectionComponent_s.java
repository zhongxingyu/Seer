 package gov.nih.nci.cagrid.introduce.portal.modification.discovery.files;
 
 import gov.nih.nci.cagrid.common.SchemaValidator;
 import gov.nih.nci.cagrid.common.Utils;
 import gov.nih.nci.cagrid.common.XMLUtilities;
 import gov.nih.nci.cagrid.common.portal.MultiEventProgressBar;
 import gov.nih.nci.cagrid.introduce.IntroduceConstants;
 import gov.nih.nci.cagrid.introduce.beans.configuration.NamespaceReplacementPolicy;
 import gov.nih.nci.cagrid.introduce.beans.extension.DiscoveryExtensionDescriptionType;
 import gov.nih.nci.cagrid.introduce.beans.namespace.NamespaceType;
 import gov.nih.nci.cagrid.introduce.beans.namespace.NamespacesType;
 import gov.nih.nci.cagrid.introduce.common.CommonTools;
 import gov.nih.nci.cagrid.introduce.common.FileFilters;
 import gov.nih.nci.cagrid.introduce.common.ResourceManager;
 import gov.nih.nci.cagrid.introduce.portal.modification.discovery.NamespaceTypeDiscoveryComponent;
 
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JTextField;
 
 import org.apache.log4j.Logger;
 import org.cagrid.grape.utils.CompositeErrorDialog;
 import org.jdom.Document;
 
 
 public class FileTypesSelectionComponent extends NamespaceTypeDiscoveryComponent {
 
     private static final Logger logger = Logger.getLogger(FileTypesSelectionComponent.class);
 
     public static String IMPORT_SCHEMA = "import";
     public static String INCLUDE_SCHEMA = "include";
     public static String REDEFINE_SCHEMA = "redefine";
 
 
     private class SchemaInfo {
         String filename;
         String namespace;
         String schemaType = null;
 
 
         public SchemaInfo(String filename, String namespace) {
             this.filename = filename;
             this.namespace = namespace;
         }
 
 
         public SchemaInfo(String filename, String namespace, String schemaType) {
             this(filename, namespace);
             this.schemaType = schemaType;
         }
 
 
         public String getSchemaType() {
             return schemaType;
         }
 
 
         public void setSchemaType(String schemaType) {
             this.schemaType = schemaType;
         }
 
 
         public boolean equals(Object obj) {
             SchemaInfo info = (SchemaInfo) obj;
             return filename.equals(info.getFilename()) && namespace.equals(info.getNamespace());
         }
 
 
         public int hashCode() {
             return toString().hashCode();
         }
 
 
         public String getFilename() {
             return filename;
         }
 
 
         public void setFilename(String filename) {
             this.filename = filename;
         }
 
 
         public String getNamespace() {
             return namespace;
         }
 
 
         public void setNamespace(String namespace) {
             this.namespace = namespace;
         }
 
 
         public String toString() {
             return filename + " : " + namespace;
         }
     }
 
     public String currentNamespace = null;
 
     public String currentFile = null;
 
     private JLabel namespaceLabel = null;
 
     public String filterType = null;
 
     private JButton browseButton = null;
 
     private JTextField namespaceText = null;
 
     private JTextField filenameText = null;
 
 
     /**
      * This method initializes
      */
     public FileTypesSelectionComponent(DiscoveryExtensionDescriptionType descriptor, NamespacesType types) {
         super(descriptor, types);
         initialize();
     }
 
 
     /**
      * This method initializes this
      */
     private void initialize() {
         GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
         gridBagConstraints2.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints2.gridy = 0;
         gridBagConstraints2.weightx = 1.0;
         gridBagConstraints2.insets = new java.awt.Insets(2, 2, 2, 2);
         gridBagConstraints2.gridx = 1;
         GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
         gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
         gridBagConstraints1.gridy = 1;
         gridBagConstraints1.weightx = 1.0;
         gridBagConstraints1.insets = new java.awt.Insets(2, 2, 2, 2);
         gridBagConstraints1.gridx = 1;
         GridBagConstraints gridBagConstraints = new GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
         gridBagConstraints.gridy = 0;
         GridBagConstraints gridBagConstraints9 = new GridBagConstraints();
         gridBagConstraints9.anchor = java.awt.GridBagConstraints.WEST;
         gridBagConstraints9.gridy = 1;
         gridBagConstraints9.insets = new java.awt.Insets(2, 2, 2, 2);
         gridBagConstraints9.gridx = 0;
         this.namespaceLabel = new JLabel();
         this.namespaceLabel.setText("Namespace");
         this.setLayout(new GridBagLayout());
         this.add(this.namespaceLabel, gridBagConstraints9);
         this.add(getBrowseButton(), gridBagConstraints);
         this.add(getNamespaceText(), gridBagConstraints1);
         this.add(getFilenameText(), gridBagConstraints2);
     }
 
 
     /**
      * This method initializes browseButton
      * 
      * @return javax.swing.JButton
      */
     private JButton getBrowseButton() {
         if (this.browseButton == null) {
             this.browseButton = new JButton();
             this.browseButton.setText("Browse");
             this.browseButton.addActionListener(new java.awt.event.ActionListener() {
                 public void actionPerformed(java.awt.event.ActionEvent e) {
                     try {
                         String selectedFilename = ResourceManager.promptFile(null, FileFilters.XSD_FILTER);
                         if (selectedFilename != null) {
                             FileTypesSelectionComponent.this.currentFile = new File(selectedFilename).getAbsolutePath();
                             getFilenameText().setText(FileTypesSelectionComponent.this.currentFile);
                             Document doc = XMLUtilities
                                 .fileNameToDocument(FileTypesSelectionComponent.this.currentFile);
                             FileTypesSelectionComponent.this.currentNamespace = doc.getRootElement().getAttributeValue(
                                 "targetNamespace");
                            getNamespaceText().setText(FileTypesSelectionComponent.this.currentNamespace);
                         }
                     } catch (Exception ex) {
                         CompositeErrorDialog.showErrorDialog("Please make sure the file is a valid XML Schema", ex);
                     }
                 }
             });
         }
         return this.browseButton;
     }
 
 
     @Override
     public NamespaceType[] createNamespaceType(File schemaDestinationDir, NamespaceReplacementPolicy replacementPolicy,
         MultiEventProgressBar progress) {
         try {
             if (this.currentNamespace == null) {
                 addError("Please select a schema file.");
                 return null;
             }
             if (namespaceAlreadyExists(this.currentNamespace)
                 && replacementPolicy.equals(NamespaceReplacementPolicy.ERROR)) {
                 addError("Namespace already exists.");
                 return null;
             }
 
             boolean result = checkAgainstPolicy(this.currentFile, new HashSet(), replacementPolicy);
             if (result == false) {
                 return null;
             }
 
             List namespaces = new ArrayList();
 
             try {
                 SchemaValidator.verify(this.currentFile);
             } catch (Exception e) {
                 addError("File does not appear to be a valid schema");
                 setErrorCauseThrowable(e);
                 return null;
             }
 
             boolean needToCopy = true;
             if (this.currentFile.startsWith(schemaDestinationDir.getAbsolutePath())) {
                 needToCopy = false;
             }
 
             List storedSchemas = new LinkedList();
             processSchemas(new SchemaInfo(this.currentFile, this.currentNamespace), schemaDestinationDir,
                 new HashSet(), storedSchemas, replacementPolicy, needToCopy);
 
             Iterator schemaFileIter = storedSchemas.iterator();
             while (schemaFileIter.hasNext()) {
                 SchemaInfo info = ((SchemaInfo) schemaFileIter.next());
                 File storedSchemaFile = new File(info.getFilename());
 
                 if (info.getSchemaType() == null || !info.getSchemaType().equals(INCLUDE_SCHEMA)) {
 
                     NamespaceType nsType = null;
                     if (CommonTools.getNamespaceType(getCurrentNamespaces(), info.getNamespace()) != null) {
                         NamespaceType oldnsType = CommonTools.getNamespaceType(getCurrentNamespaces(), info
                             .getNamespace());
                         nsType = CommonTools.reCreateNamespaceType(storedSchemaFile.getAbsolutePath(),
                             schemaDestinationDir, oldnsType);
                     } else {
                         nsType = CommonTools.createNamespaceType(storedSchemaFile.getAbsolutePath(),
                             schemaDestinationDir);
                     }
                     namespaces.add(nsType);
                 }
             }
 
             NamespaceType[] types = new NamespaceType[namespaces.size()];
             namespaces.toArray(types);
             return types;
         } catch (Exception e) {
             addError("Error processing schema: " + e.getMessage());
             setErrorCauseThrowable(e);
             return null;
         }
     }
 
 
     private boolean checkAgainstPolicy(String fileName, Set visitedSchemas, NamespaceReplacementPolicy replacementPolicy) {
         try {
             File schemaFile = new File(fileName);
 
             // look for imports
             Document schema = XMLUtilities.fileNameToDocument(schemaFile.getCanonicalPath());
             visitedSchemas.add(schemaFile.getCanonicalPath());
 
             List importEls = schema.getRootElement().getChildren("import",
                 schema.getRootElement().getNamespace(IntroduceConstants.W3CNAMESPACE));
             if (importEls != null) {
                 for (int i = 0; i < importEls.size(); i++) {
                     org.jdom.Element importEl = (org.jdom.Element) importEls.get(i);
                     String location = importEl.getAttributeValue("schemaLocation");
                     if (location != null && !(location.startsWith("http:") || location.startsWith("gme:"))) {
                         String namespace = importEl.getAttributeValue("namespace");
                         if (namespace != null) {
                             // see if namespace already exists if so check
                             // property
                             // to see if supposed to error;
                             if (namespaceAlreadyExists(namespace)) {
                                 if (replacementPolicy.equals(NamespaceReplacementPolicy.ERROR)) {
                                     addError("Imported namespace already exists: "
                                         + namespace
                                         + ". \nIf you want this schema to be overwriten please change the policy to \"ignore\" in the introduce preferences menu");
                                     return false;
                                 }
                             }
 
                         }
 
                         File currentPath = schemaFile.getCanonicalFile().getParentFile();
                         if (!schemaFile.equals(new File(currentPath.getCanonicalPath() + File.separator + location))) {
                             File importedSchema = new File(currentPath + File.separator + location);
                             if (!visitedSchemas.contains(importedSchema.getCanonicalPath())) {
                                 // only copy schemas not yet visited
                                 boolean result = checkAgainstPolicy(importedSchema.getCanonicalPath(), visitedSchemas,
                                     replacementPolicy);
                                 if (result == false) {
                                     return false;
                                 }
                             }
                         } else {
                             System.err.println("WARNING: Schema is importing itself. " + schemaFile);
                         }
 
                     }
                 }
             }
         } catch (Exception e) {
             logger.error(e);
             addError(e.getMessage());
             return false;
         }
         return true;
     }
 
 
     public void processSchemas(SchemaInfo currentSchema, File copyToDirectory, Set visitedSchemas, List storedSchemas,
         NamespaceReplacementPolicy replacementPolicy, boolean copy) throws Exception {
         File schemaFile = new File(currentSchema.getFilename());
         Document schema = XMLUtilities.fileNameToDocument(schemaFile.getCanonicalPath());
         String namespaceURI = schema.getRootElement().getAttribute("targetNamespace").getValue();
         if (namespaceAlreadyExists(namespaceURI) && replacementPolicy.equals(NamespaceReplacementPolicy.IGNORE)) {
             // do nothing just ignore.....
         } else {
             // mark the schema as visited
             visitedSchemas.add(currentSchema.getFilename());
             if (copy) {
                 logger.debug("Copying schema " + schemaFile + " to " + copyToDirectory.getCanonicalPath());
                 File outFile = new File(copyToDirectory.getCanonicalPath() + File.separator + schemaFile.getName());
                 currentSchema.setFilename(outFile.getAbsolutePath());
                 Utils.copyFile(schemaFile, outFile);
             }
             storedSchemas.add(currentSchema);
 
         }
        
         // Look for redefines
         List redefineEls = schema.getRootElement().getChildren("redefine",
             schema.getRootElement().getNamespace(IntroduceConstants.W3CNAMESPACE));
         if (redefineEls != null) {
             for (int i = 0; i < redefineEls.size(); i++) {
                 org.jdom.Element importEl = (org.jdom.Element) redefineEls.get(i);
                 String location = importEl.getAttributeValue("schemaLocation");
                 if (location != null && !(location.startsWith("http:") || location.startsWith("gme:"))) {
                     File currentPath = schemaFile.getCanonicalFile().getParentFile();
                     if (!schemaFile.equals(new File(currentPath.getCanonicalPath() + File.separator + location))) {
                         File importedSchema = new File(currentPath + File.separator + location);
                         SchemaInfo currentSchemaInfo = new SchemaInfo(importedSchema.getAbsolutePath(), namespaceURI,
                             REDEFINE_SCHEMA);
                         if (!visitedSchemas.contains(currentSchemaInfo.getFilename())) {
                             // only copy schemas not yet visited
                             if (importedSchema.exists() && importedSchema.canRead()) {
                                 processSchemas(currentSchemaInfo, new File(copyToDirectory.getCanonicalFile()
                                     + File.separator + location).getParentFile(), visitedSchemas, storedSchemas,
                                     replacementPolicy, copy);
                             } else {
                                 throw new Exception("Redefined schema cannot be found: "
                                     + importedSchema.getAbsolutePath());
                             }
                         }
                     } else {
                         System.err.println("WARNING: Schema is redefining itself. " + schemaFile);
                     }
                 }
 
             }
 
         }
        
 
         // Look for includes
         List includeEls = schema.getRootElement().getChildren("include",
             schema.getRootElement().getNamespace(IntroduceConstants.W3CNAMESPACE));
         if (includeEls != null) {
             for (int i = 0; i < includeEls.size(); i++) {
                 org.jdom.Element importEl = (org.jdom.Element) includeEls.get(i);
                 String location = importEl.getAttributeValue("schemaLocation");
                 if (location != null && !(location.startsWith("http:") || location.startsWith("gme:"))) {
                     File currentPath = schemaFile.getCanonicalFile().getParentFile();
                     if (!schemaFile.equals(new File(currentPath.getCanonicalPath() + File.separator + location))) {
                         File importedSchema = new File(currentPath + File.separator + location);
                         SchemaInfo currentSchemaInfo = new SchemaInfo(importedSchema.getAbsolutePath(), namespaceURI,
                             INCLUDE_SCHEMA);
                         if (!visitedSchemas.contains(currentSchemaInfo.getFilename())) {
                             // only copy schemas not yet visited
                             if (importedSchema.exists() && importedSchema.canRead()) {
                                 processSchemas(currentSchemaInfo, new File(copyToDirectory.getCanonicalFile()
                                     + File.separator + location).getParentFile(), visitedSchemas, storedSchemas,
                                     replacementPolicy, copy);
                             } else {
                                 throw new Exception("Imported schema cannot be found: "
                                     + importedSchema.getAbsolutePath());
                             }
                         }
                     } else {
                         System.err.println("WARNING: Schema is including itself. " + schemaFile);
                     }
                 }
 
             }
 
         }
 
         // look for imports
         List importEls = schema.getRootElement().getChildren("import",
             schema.getRootElement().getNamespace(IntroduceConstants.W3CNAMESPACE));
         if (importEls != null) {
             for (int i = 0; i < importEls.size(); i++) {
                 org.jdom.Element importEl = (org.jdom.Element) importEls.get(i);
                 String location = importEl.getAttributeValue("schemaLocation");
                 String namespace = importEl.getAttributeValue("namespace");
                 if (location != null && !(location.startsWith("http:") || location.startsWith("gme:"))) {
                     File currentPath = schemaFile.getCanonicalFile().getParentFile();
                     if (!schemaFile.equals(new File(currentPath.getCanonicalPath() + File.separator + location))) {
                         File importedSchema = new File(currentPath + File.separator + location);
                         SchemaInfo currentSchemaInfo = new SchemaInfo(importedSchema.getAbsolutePath(), namespace,
                             IMPORT_SCHEMA);
                         if (!visitedSchemas.contains(currentSchemaInfo.getFilename())) {
                             // only copy schemas not yet visited
                             if (importedSchema.exists() && importedSchema.canRead()) {
                                 processSchemas(currentSchemaInfo, new File(copyToDirectory.getCanonicalFile()
                                     + File.separator + location).getParentFile(), visitedSchemas, storedSchemas,
                                     replacementPolicy, copy);
                             } else {
                                 throw new Exception("Imported schema cannot be found: "
                                     + importedSchema.getAbsolutePath());
                             }
                         }
                     } else {
                         System.err.println("WARNING: Schema is importing itself. " + schemaFile);
                     }
                 }
             }
         }
     }
 
 
     /**
      * This method initializes namespaceText
      * 
      * @return javax.swing.JTextField
      */
     private JTextField getNamespaceText() {
         if (this.namespaceText == null) {
             this.namespaceText = new JTextField();
             this.namespaceText.setEditable(false);
         }
         return this.namespaceText;
     }
 
 
     /**
      * This method initializes filenameText
      * 
      * @return javax.swing.JTextField
      */
     private JTextField getFilenameText() {
         if (this.filenameText == null) {
             this.filenameText = new JTextField();
             this.filenameText.setEnabled(true);
             this.filenameText.setEditable(false);
         }
         return this.filenameText;
     }
 
 } // @jve:decl-index=0:visual-constraint="16,10"
