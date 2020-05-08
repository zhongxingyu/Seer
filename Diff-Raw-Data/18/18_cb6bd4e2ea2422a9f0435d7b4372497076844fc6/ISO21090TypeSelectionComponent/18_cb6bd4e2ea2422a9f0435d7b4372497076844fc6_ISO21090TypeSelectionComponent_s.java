 package org.cagrid.iso21090.portal.discovery;
 
 import gov.nih.nci.cagrid.common.Utils;
 import gov.nih.nci.cagrid.common.XMLUtilities;
 import gov.nih.nci.cagrid.common.portal.MultiEventProgressBar;
 import gov.nih.nci.cagrid.introduce.beans.configuration.NamespaceReplacementPolicy;
 import gov.nih.nci.cagrid.introduce.beans.extension.DiscoveryExtensionDescriptionType;
 import gov.nih.nci.cagrid.introduce.beans.extension.PropertiesProperty;
 import gov.nih.nci.cagrid.introduce.beans.namespace.NamespaceType;
 import gov.nih.nci.cagrid.introduce.beans.namespace.NamespacesType;
 import gov.nih.nci.cagrid.introduce.beans.namespace.SchemaElementType;
 import gov.nih.nci.cagrid.introduce.common.CommonTools;
 import gov.nih.nci.cagrid.introduce.extension.ExtensionTools;
 import gov.nih.nci.cagrid.introduce.extension.ExtensionsLoader;
 import gov.nih.nci.cagrid.introduce.extension.utils.ExtensionUtilities;
 import gov.nih.nci.cagrid.introduce.portal.modification.discovery.NamespaceTypeDiscoveryComponent;
 
 import java.awt.Font;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.GridLayout;
 import java.io.File;
 import java.io.FileFilter;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import javax.swing.BorderFactory;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.border.TitledBorder;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.cagrid.iso21090.portal.discovery.constants.Constants;
 import org.jdom.Element;
 import org.jdom.output.Format;
 import org.jdom.output.XMLOutputter;
 /**
  * 
  * @author Justin Permar
  * @author David
  */
 
 @SuppressWarnings("serial")
 public class ISO21090TypeSelectionComponent extends NamespaceTypeDiscoveryComponent {
     private static final Log LOG = LogFactory.getLog(ISO21090TypeSelectionComponent.class);
     
     private JPanel mainjPanel = null;
     private JTextArea descriptionjTextArea = null;
     private JPanel infoPanel = null;
     private JLabel extNSLabel = null;
     private JTextField extNSjTextField = null;
     private JLabel extPackagejLabel = null;
     private JTextField extPackagejTextField = null;
 
 
     public ISO21090TypeSelectionComponent(DiscoveryExtensionDescriptionType descriptor, NamespacesType currentNamespaces) {
         super(descriptor, currentNamespaces);
         initialize();
     }
 
 
     /**
      * This method initializes this
      */
     private void initialize() {
         GridLayout gridLayout = new GridLayout();
         gridLayout.setRows(1);
         this.setLayout(gridLayout);
         this.add(getMainjPanel(), null);
     }
 
     
     protected String getISOXSDFilename() {
         return ExtensionTools.getProperty(getDescriptor().getProperties(), Constants.DATATYPES_FILENAME_KEY);
     }
 
     
     protected PropertiesProperty getISOExtensionsNamespaceProperty() {
         return ExtensionTools.getPropertyObject(getDescriptor().getProperties(), Constants.EXTENSION_NAMESPACE_KEY);
     }
 
 
     protected String getISOExtensionsXSDFilename() {
         return ExtensionTools.getProperty(getDescriptor().getProperties(), Constants.EXTENSION_FILENAME_KEY);
     }
 
 
     protected PropertiesProperty getISOExtensionsPackageProperty() {
         return ExtensionTools.getPropertyObject(getDescriptor().getProperties(), Constants.EXTENSION_PACKAGE_KEY);
     }
 
 
     @Override
     public NamespaceType[] createNamespaceType(File schemaDir, NamespaceReplacementPolicy replacementPolicy,
         MultiEventProgressBar progress) {
 
         // check the namespace replacement policy and see what to do if the
         // stuff we plan to add already exists    	
         if (namespaceAlreadyExists(getISOExtensionsNamespaceProperty().getValue())) {
             if (replacementPolicy.equals(NamespaceReplacementPolicy.ERROR)) {
                 String error = "Namespace ("
                     + getISOExtensionsNamespaceProperty().getValue()
                     + ") already exists, and policy was to error. " 
                     + "Change the setting in the Preferences to REPLACE or IGNORE to avoid this error.";
                 LOG.error(error);
                 addError(error);
                 return null;
             } else {
                 LOG.info("The ISO21090 Extensions schema already exists, the non-ERROR policy is:"
                     + replacementPolicy.getValue());
             }
         }
 
         // copy the schemas
         File copiedISOExtensionsXSDFilename = null;
         try {
             copySchemaFromExtensionDir(getISOXSDFilename(), schemaDir);
             copiedISOExtensionsXSDFilename = copySchemaFromExtensionDir(getISOExtensionsXSDFilename(), schemaDir);
         } catch (IOException e) {
             addError("Problem copying schemas:" + e.getMessage());
             setErrorCauseThrowable(e);
             return null;
         }
 
         // copy the jar files and fix classpath
         try {
             copyLibraries(getServiceDirectory(schemaDir));
         } catch (Exception e) {
             addError("Problem copying jar files:" + e.getMessage());
             setErrorCauseThrowable(e);
             return null;
         }
 
         NamespaceType[] createdTypes = new NamespaceType[1];
         // create the namespace types with commontools
         try {
             createdTypes[0] = CommonTools.createNamespaceType(
                 copiedISOExtensionsXSDFilename.getAbsolutePath(), schemaDir);
             createdTypes[0].setGenerateStubs(Boolean.FALSE);
             createdTypes[0].setPackageName(getISOExtensionsPackageProperty().getValue());
         } catch (Exception e) {
             addError("Problem creating namespace types:" + e.getMessage());
             setErrorCauseThrowable(e);
             return null;
         }
         
         //There are two types in the ISO schema that are problematic.
         /*
          * <xsd:element name="sub" type="xsd:string"/>
          * <xsd:element name="sup" type="xsd:string"/>
          */
         //These are problematic because they use a type that is not defined in the current namespace
         //The NamespaceType class we use in this extension assume one package for all the Java types in the namespace
         //In this case, the above types are actually defined in the java.lang package (and not our JAXB generated package for the ISO types)
         //Thus, we want to ignore these for simplicity (sub and sup are not required by the NCI spec)
         //let's do a in-memory copy of the createdTypes object and remove the sub and sup from the List
         List<String> filteredTypes = new ArrayList<String>();
         filteredTypes.add("sub");
         filteredTypes.add("sup");
         filteredTypes.add("text");
         filteredTypes.add("br");
         filteredTypes.add("caption");
         filteredTypes.add("col");
         filteredTypes.add("colgroup");
         filteredTypes.add("content");
         filteredTypes.add("footnote");
         filteredTypes.add("footnoteRef");
         filteredTypes.add("item");
         filteredTypes.add("linkHtml");
         filteredTypes.add("list");
         filteredTypes.add("paragraph");
         filteredTypes.add("renderMultiMedia");
         filteredTypes.add("table");
         filteredTypes.add("tbody");
         filteredTypes.add("td");
         filteredTypes.add("tfoot");
         filteredTypes.add("th");
         filteredTypes.add("thead");
         filteredTypes.add("tr");
 
         filterTypes(createdTypes[0], filteredTypes);        
 
         // walk thru them and configure the [de]serializers
         // TODO: should both use the same framework? The reference service just
         // configures the extension schema
         ClassNameDiscoveryUtil nameDiscoverer = new ClassNameDiscoveryUtil(Arrays.asList(getIsoSupportLibraries()));
         //Note: there is exactly one package name per NamespaceType
         String packageName = createdTypes[0].getPackageName();
         Map<String, String> typeClassMap = new HashMap<String, String>();
         for (SchemaElementType se : createdTypes[0].getSchemaElement()) {
             // figure out the JaxB class name for the element type
             String className = null;
             try {
                 className = nameDiscoverer.getJavaClassName(packageName, se.getType());
             } catch (Exception e) {
                 addError("Error determining Java class name for element " + se.getType() + ": " + e.getMessage());
                 setErrorCauseThrowable(e);
             }
             
             // may return null, so do the default
             //TODO JDP remove this type from the list of allowed types if there is no XmlRootElement annotation that matches this type???
             se.setClassName(className != null ? className : se.getType());
             se.setDeserializer(Constants.DESERIALIZER_FACTORY_CLASSNAME);
             se.setSerializer(Constants.SERIALIZER_FACTORY_CLASSNAME);
             
             if (className != null)
             	typeClassMap.put(se.getType(), className);
         }
         
         //
         // Adds postStubs fixes as described in CAGRID-373
         //
         modifyDevBuildFile(copiedISOExtensionsXSDFilename, 
         		getServiceDirectory(schemaDir), 
         		packageName, typeClassMap);
         
         return createdTypes;
     }
     
     
     private void filterTypes(NamespaceType input, List<String> filteredTypes) {
     	SchemaElementType[] elements = input.getSchemaElement();
     	List<SchemaElementType> list = new ArrayList<SchemaElementType>();
     	for (SchemaElementType type : elements) {
     		boolean filter = false;
     		for (String filterType : filteredTypes) {
     			if (type.getType().equals(filterType)) {
     				filter = true;
     			}
     		}
     		if (!filter) {
     			//add to list
     			list.add(type);
     		}
     	}
     	
     	//set the schema elements to new list
     	input.setSchemaElement(list.toArray(new SchemaElementType[0]));
     }
 
 
     /**
      * HACK: This should be done more elegantly but is limited by introduce as
      * described in this RFE:
      * http://gforge.nci.nih.gov/tracker/?func=detail&group_id
      * =25&aid=21615&atid=2252
      * 
      * @param schemaDir
      *            The provided schema directory of the service
      * @return The directory of the service
      */
     protected File getServiceDirectory(File schemaDir) {
         return new File(schemaDir + "/../..");
     }
 
 
     protected File copySchemaFromExtensionDir(String schemaName, File outputDir) throws IOException {
         File schemaFile = new File(ExtensionsLoader.getInstance().getExtensionsDir(),
             Constants.EXTENSION_NAME + File.separator + "schema" + File.separator + schemaName);
         LOG.debug("Copying schema from " + schemaFile.getAbsolutePath());
         File outputFile = new File(outputDir, schemaName);
         LOG.debug("Saving schema to " + outputFile.getAbsolutePath());
         Utils.copyFile(schemaFile, outputFile);
         return outputFile;
     }
 
 
     protected void copyLibraries(File serviceDirectory) throws Exception {
         File[] libs = getIsoSupportLibraries();
         File[] copiedLibs = new File[libs.length];
         for (int i = 0; i < libs.length; i++) {
             File outFile = new File(serviceDirectory, "lib" + File.separator + libs[i].getName());
             copiedLibs[i] = outFile;
             Utils.copyFile(libs[i], outFile);
         }
         modifyClasspathFile(copiedLibs, serviceDirectory);
     }
 
     
     protected File[] getIsoSupportLibraries() {
         File libDir = new File(ExtensionsLoader.getInstance().getExtensionsDir(), 
             File.separator + "lib");
         File[] libs = libDir.listFiles(new FileFilter() {
             public boolean accept(File pathname) {
                 String name = pathname.getName();
                 // take any jar from our extension's directory
                 return (name.endsWith(".jar"));
             }
         });
         return libs;
     }
 
 
     protected void modifyClasspathFile(File[] libs, File serviceDirectory) throws Exception {
         File classpathFile = new File(serviceDirectory, ".classpath");
         ExtensionUtilities.syncEclipseClasspath(classpathFile, libs);
     }
 
     protected Element createReplaceElement(String oldValue, String newValue,
     		String dir, String includes) {
     	
     	//   <replaceregexp match="org.iso._21090.BLNonNull" 
     	//         replace="org.iso._21090.BlNonNull" flags="gi" byline="true">
         //      <fileset dir="${stubs.src}" includes="**/stubs/*.java"/>
         //   </replaceregexp>
     
     	Element replaceEl = new Element("replaceregexp");	
     	replaceEl.setAttribute("match", oldValue);
     	replaceEl.setAttribute("replace", newValue);
     	replaceEl.setAttribute("flags", "gi");
     	replaceEl.setAttribute("byline", "true");
     	
     	Element includeEl = new Element("fileset");
     	includeEl.setAttribute("dir", dir);
     	includeEl.setAttribute("includes", includes);
     	    	
     	replaceEl.addContent(includeEl);
     	    	
     	return replaceEl;
     }
     
     protected Element createStubReplaceElement(String oldValue, String newValue) {
     	return createReplaceElement(oldValue, newValue, 
     			"${stubs.src}", "**/stubs/*.java");
     }
    
     protected void rewritePostStubs(File copiedISOExtensionsXSDFilename,
     		String devBuild, Element root, 
     		Element psTarget, String pkgName, 
     		Map<String, String> typeClassMap) throws Exception {
     	
 		Element xsdRoot = XMLUtilities.fileNameToDocument(copiedISOExtensionsXSDFilename.getAbsolutePath()).getRootElement();
 	    List<Element> elements = xsdRoot.getChildren();
     	for(Element e : elements) {
     		if (e.getName().equals("element")) {
     			// We want to replace the stub name references generated by axis
     			// (which correspond to the "type" attribute in the XSD, axis also
     			// removes the "." from the name), with the actual class name
     			// in the beans jar.
     			String oldValue = pkgName + "." + e.getAttributeValue("type").replace(".", "");
     			String newValue = pkgName + "." + typeClassMap.get(e.getAttributeValue("name"));
     			psTarget.addContent(createStubReplaceElement(oldValue, newValue));
     			
     			// Major hack. If the type is "INT", axis generates get_int and set_int
     			// in the corresponding stubs. This is a problem because the introduce
     			// generated service is expecting these names to be getInt and setInt
     			// respectively. This "unusual" behavior is only happening for the INT
     			// type, which my only guess is that it has something to do with INT
     			// being some sort of reserved word.
     			//
     			// In addition, the source generate by introduce (globus) package gets
     			// broken as well.
    			if (e.getAttributeValue("type").equalsIgnoreCase("int")) {
    				psTarget.addContent(createStubReplaceElement("get_int", "getInt"));
    				psTarget.addContent(createStubReplaceElement("set_int", "setInt"));
    				psTarget.addContent(createReplaceElement("get_int", "getInt", 
    						"${src.dir}", "**/service/globus/*.java"));
    				psTarget.addContent(createReplaceElement("set_int", "setInt", 
    						"${src.dir}", "**/client/*.java"));
    				
    			}
     		}
     	}
 		
     	XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
         FileWriter writer = new FileWriter(devBuild);
         writer.write("<?xml version=\"1.0\"?>\n");
         outputter.output(root, writer);
         writer.flush();
         writer.close();
     }
     
     protected void modifyDevBuildFile(File copiedISOExtensionsXSDFilename, 
     		File svcDir, String packageName, Map<String, String> typeClassMap) {
     	try {
     		String devBuildPath = svcDir.getAbsoluteFile() + "/dev-build.xml";
 			Element root = XMLUtilities.fileNameToDocument(devBuildPath).getRootElement();
 		    List<Element> targets = root.getChildren("target", root.getNamespace());
 		    
 		    //
 		    // Look for the postStubs target
 		    //
 			for(Element target : targets) {
 				
 				String attrName = target.getAttributeValue("name");
 				if (attrName != null && attrName.equals("postStubs")) {
 					//found it - rewrite it
 					rewritePostStubs(copiedISOExtensionsXSDFilename, 
 							devBuildPath, root, target, packageName, typeClassMap);
 			        return;
 			        
 				}
 		    }
     	} catch (Exception e) {
     		addError("Problem modifying dev-build file:" + e.getMessage());
             setErrorCauseThrowable(e);
 		}
     }
 
     /**
      * This method initializes mainjPanel
      * 
      * @return javax.swing.JPanel
      */
     private JPanel getMainjPanel() {
         if (mainjPanel == null) {
             
             TitledBorder centerBorder = BorderFactory.createTitledBorder("ISO 21090 Datatypes and NCI Localizations");
             centerBorder.setTitleFont(centerBorder.getTitleFont().deriveFont(Font.BOLD));
             
             mainjPanel = new JPanel();
             mainjPanel.setBorder(centerBorder);
             mainjPanel.setLayout(new GridBagLayout());
             
             // Description header
             GridBagConstraints gridBagConstraints = new GridBagConstraints();
             gridBagConstraints.fill = GridBagConstraints.BOTH;
             gridBagConstraints.ipady = 30;
             mainjPanel.add(getDescriptionjTextArea(), gridBagConstraints);
             
             // Input data panel
             GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
             gridBagConstraints1.gridx = 0;
             gridBagConstraints1.gridy = 1;
             gridBagConstraints1.fill = GridBagConstraints.HORIZONTAL;
             gridBagConstraints1.weightx = 1.0;
             gridBagConstraints1.weighty = 1.0;
             gridBagConstraints1.ipadx = 70;
             gridBagConstraints1.ipady = 70;      
             mainjPanel.add(getInfoPanel(), gridBagConstraints1);
             
             
         }
         return mainjPanel;
     }
 
 
     /**
      * This method initializes descriptionjTextArea
      * 
      * @return javax.swing.JTextArea
      */
     private JTextArea getDescriptionjTextArea() {
         if (descriptionjTextArea == null) {
             descriptionjTextArea = new JTextArea();
             descriptionjTextArea.setEditable(false);
             descriptionjTextArea.setFont(descriptionjTextArea.getFont().deriveFont(Font.ITALIC));
             descriptionjTextArea.setLineWrap(true);
             descriptionjTextArea.setWrapStyleWord(true);
             descriptionjTextArea.setText(
                 "\nClicking \"Add\" on this type will add the standard ISO Datatypes and NCI localizations to your service.  " +
                 "The types will be configured with custom serialization to leverage Java beans which will also be added to the service.");
         }
         return descriptionjTextArea;
     }
 
 
     /**
      * This method initializes infoPanel
      * 
      * @return javax.swing.JPanel
      */
     private JPanel getInfoPanel() {
         if (infoPanel == null) {
         	
         	infoPanel = new JPanel();
             TitledBorder border = BorderFactory.createTitledBorder("Type information");
             infoPanel.setBorder(border);
             infoPanel.setLayout(new GridBagLayout());
             
             // Row 1 Column 1
             GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
             gridBagConstraints3.gridx = 0;
             gridBagConstraints3.anchor = GridBagConstraints.WEST;
             gridBagConstraints3.gridy = 0;
             extNSLabel = new JLabel();
             extNSLabel.setText(getISOExtensionsNamespaceProperty().getDisplayName());
             infoPanel.add(extNSLabel, gridBagConstraints3);
             
             // Row 1 Column 2
             GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
             gridBagConstraints2.fill = GridBagConstraints.HORIZONTAL;
             gridBagConstraints2.gridx = 1;
             gridBagConstraints2.gridy = 0;
             gridBagConstraints2.anchor = GridBagConstraints.WEST;
             gridBagConstraints2.weightx = 1.0;
             infoPanel.add(getExtNSjTextField(), gridBagConstraints2);
             
             // Row 2 Column 1
             GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
             gridBagConstraints8.anchor = GridBagConstraints.WEST;
             gridBagConstraints8.gridy = 1;
             gridBagConstraints8.gridx = 0;
             extPackagejLabel = new JLabel();
             extPackagejLabel.setText(getISOExtensionsPackageProperty().getDisplayName() + "   ");
             infoPanel.add(extPackagejLabel, gridBagConstraints8);
             
             // Row 2 Column 2
             GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
             gridBagConstraints6.fill = GridBagConstraints.HORIZONTAL;
             gridBagConstraints6.anchor = GridBagConstraints.WEST;
             gridBagConstraints6.gridx = 1;
             gridBagConstraints6.gridy = 1;
             gridBagConstraints6.weightx = 1.0;
             infoPanel.add(getExtPackagejTextField(), gridBagConstraints6);
         }
         return infoPanel;
     }
 
 
     /**
      * This method initializes extNSjTextField
      * 
      * @return javax.swing.JTextField
      */
     private JTextField getExtNSjTextField() {
         if (extNSjTextField == null) {
             extNSjTextField = new JTextField();
             extNSjTextField.setEditable(false);
             extNSjTextField.setText(getISOExtensionsNamespaceProperty().getValue());
             extNSjTextField.setToolTipText(getISOExtensionsNamespaceProperty().getDescription());
         }
         return extNSjTextField;
     }
 
 
     /**
      * This method initializes extPackagejTextField	
      * 	
      * @return javax.swing.JTextField	
      */
     private JTextField getExtPackagejTextField() {
         if (extPackagejTextField == null) {
             extPackagejTextField = new JTextField();
             extPackagejTextField.setEditable(false);
             extPackagejTextField.setText(getISOExtensionsPackageProperty().getValue());
             extPackagejTextField.setToolTipText(getISOExtensionsPackageProperty().getDescription());
         }
         return extPackagejTextField;
     }
 }
