 package gov.nih.nci.cagrid.introduce.portal.modification.services.methods;
 
 import gov.nih.nci.cagrid.common.Utils;
 import gov.nih.nci.cagrid.common.XMLUtilities;
 import gov.nih.nci.cagrid.common.portal.PortalLookAndFeel;
 import gov.nih.nci.cagrid.common.portal.validation.IconFeedbackPanel;
 import gov.nih.nci.cagrid.introduce.IntroduceConstants;
 import gov.nih.nci.cagrid.introduce.beans.ServiceDescription;
 import gov.nih.nci.cagrid.introduce.beans.method.MethodType;
 import gov.nih.nci.cagrid.introduce.beans.method.MethodTypeExceptions;
 import gov.nih.nci.cagrid.introduce.beans.method.MethodTypeExceptionsException;
 import gov.nih.nci.cagrid.introduce.beans.method.MethodTypeImportInformation;
 import gov.nih.nci.cagrid.introduce.beans.method.MethodTypeInputs;
 import gov.nih.nci.cagrid.introduce.beans.method.MethodTypeInputsInput;
 import gov.nih.nci.cagrid.introduce.beans.method.MethodTypeOutput;
 import gov.nih.nci.cagrid.introduce.beans.method.MethodTypeProviderInformation;
 import gov.nih.nci.cagrid.introduce.beans.method.MethodsType;
 import gov.nih.nci.cagrid.introduce.beans.namespace.NamespaceType;
 import gov.nih.nci.cagrid.introduce.beans.namespace.SchemaElementType;
 import gov.nih.nci.cagrid.introduce.beans.service.ServiceType;
 import gov.nih.nci.cagrid.introduce.common.CommonTools;
 import gov.nih.nci.cagrid.introduce.common.SpecificServiceInformation;
 import gov.nih.nci.cagrid.introduce.portal.common.IntroduceLookAndFeel;
 import gov.nih.nci.cagrid.introduce.portal.modification.security.MethodSecurityPanel;
 import gov.nih.nci.cagrid.introduce.portal.modification.types.NamespaceTypeTreeNode;
 import gov.nih.nci.cagrid.introduce.portal.modification.types.NamespacesJTree;
 import gov.nih.nci.cagrid.introduce.portal.modification.types.SchemaElementTypeTreeNode;
 
 import java.awt.CardLayout;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JFileChooser;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSplitPane;
 import javax.swing.JTabbedPane;
 import javax.swing.JTextField;
 import javax.swing.ScrollPaneConstants;
 import javax.swing.WindowConstants;
 import javax.swing.border.TitledBorder;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 import javax.swing.filechooser.FileFilter;
 import javax.swing.table.TableCellEditor;
 import javax.xml.namespace.QName;
 
 import org.apache.axis.utils.JavaUtils;
 import org.apache.log4j.Logger;
 import org.apache.xml.utils.DefaultErrorHandler;
 import org.cagrid.grape.GridApplication;
 import org.cagrid.grape.utils.CompositeErrorDialog;
 import org.cagrid.grape.utils.ErrorDialog;
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.Namespace;
 
 import com.jgoodies.validation.Severity;
 import com.jgoodies.validation.ValidationResult;
 import com.jgoodies.validation.ValidationResultModel;
 import com.jgoodies.validation.message.SimpleValidationMessage;
 import com.jgoodies.validation.util.DefaultValidationResultModel;
 import com.jgoodies.validation.util.ValidationUtils;
 import com.jgoodies.validation.view.ValidationComponentUtils;
 import com.sun.xml.xsom.XSComplexType;
 import com.sun.xml.xsom.XSElementDecl;
 import com.sun.xml.xsom.XSSchemaSet;
 import com.sun.xml.xsom.parser.XSOMParser;
 
 
 /**
  * MethodViewer
  * 
  * @author <A HREF="MAILTO:hastings@bmi.osu.edu">Shannon Hastings </A>
  * @author <A HREF="MAILTO:oster@bmi.osu.edu">Scott Oster </A>
  * @author <A HREF="MAILTO:langella@bmi.osu.edu">Stephen Langella </A>
  */
 public class MethodViewer extends javax.swing.JDialog {
     private static final Logger logger = Logger.getLogger(MethodViewer.class);
 
 
     public class ElementHolder {
 
         Element serviceElement;
 
         Element methodElement;
 
 
         public ElementHolder(Element service, Element method) {
             serviceElement = service;
             methodElement = method;
         }
 
 
         public Element getServiceElement() {
             return serviceElement;
         }
 
 
         public Element getMethodElement() {
             return methodElement;
         }
 
 
         public String toString() {
             return serviceElement.getAttributeValue("name");
         }
     }
 
 
     public class ServiceHolder {
         ServiceType service;
 
 
         public ServiceHolder(ServiceType service) {
             this.service = service;
         }
 
 
         public ServiceType getService() {
             return service;
         }
 
 
         public String toString() {
             return service.getName();
         }
     }
 
 
     public class ExceptionHolder implements Comparable {
         boolean isCreated;
 
         QName qname;
 
 
         public ExceptionHolder(QName qname, boolean isCreated) {
             this.qname = qname;
             this.isCreated = isCreated;
         }
 
 
         public int compareTo(Object arg0) {
             return this.toString().compareTo(((ExceptionHolder) arg0).toString());
         }
 
 
         public String toString() {
             return qname.toString();
         }
 
     }
 
     private MethodType method;
 
     private JPanel mainPanel = null;
 
     private JScrollPane inputParamScrollPanel = null;
 
     private InputParametersTable inputParamTable = null;
 
     private JScrollPane outputTypejScrollPane = null;
 
     private OutputTypeTable outputTypeTable = null;
 
     private JPanel buttonPanel = null;
 
     private JButton doneButton = null;
 
     private JButton addInputParamButton = null;
 
     private JPanel namePanel = null;
 
     private JTextField nameField = null;
 
     private JButton removeButton = null;
 
     private JLabel methodLabel = null;
 
     private JPanel inputButtonPanel = null;
 
     private JPanel exceptionsPanel = null;
 
     private JScrollPane exceptionScrollPane = null;
 
     private JPanel exceptionInputPanel = null;
 
     private ExceptionsTable exceptionsTable = null;
 
     private JButton addExceptionButton = null;
 
     private JButton removeExceptionButton = null;
 
     private JTabbedPane tabbedPanel = null;
 
     private JPanel methodPanel = null;
 
     private JPanel securityContainerPanel = null;
 
     private SpecificServiceInformation info;
 
     private JTabbedPane configureTabbedPane = null;
 
     private JComboBox exceptionJComboBox = null;
 
     private JPanel inputNamespacesPanel = null;
 
     private JScrollPane inputNamespaceScrollPane = null;
 
     private NamespacesJTree inputNamespaceTypesJTree = null;
 
     private JPanel methodPropertiesPanel = null;
 
     private JPanel outputNamespacePanel = null;
 
     private JScrollPane outputNamespacesTypeScrollPane = null;
 
     private NamespacesJTree outputNamespacesJTree = null;
 
     private JPanel outputTypesTablePanel = null;
 
     private JPanel inputTypesTablePanel = null;
 
     private JPanel inputTableControlsPanel = null;
 
     private JLabel upLabel = null;
 
     private JLabel downLabel = null;
 
     private JButton clearOutputTypeButton = null;
 
     private JPanel exceptionsInputButtonPanel = null;
 
     private JPanel importInformationPanel = null;
 
     private JCheckBox isImportedCheckBox = null;
 
     private JCheckBox isProvidedCheckBox = null;
 
     private JScrollPane servicesTypeScrollPane = null;
 
     private ServiceReferencesTable servicesTypeTable = null;
 
     private JTextField providerClassnameTextField = null;
 
     private JPanel providerInformationPanel = null;
 
     private JLabel providerClassnameLabel = null;
 
     private JSplitPane inputParamsSplitPane = null;
 
     private JSplitPane outputTypeSplitPane = null;
 
     private JPanel createFaultPanel = null;
 
     private JTextField newFaultNameTextField = null;
 
     private JButton createFaultButton = null;
 
     private JLabel faultTypeNameLabel = null;
 
     private JLabel existingExceptionLabel = null;
 
     private JSplitPane exceptionsPanelSplitPane = null;
 
     private JScrollPane exceptionNamespacesScrollPane = null;
 
     private NamespacesJTree namespacesJTree = null;
 
     private JPanel faultsFromTypesPanel = null;
 
     private JButton addFaultFromTypeButton = null;
 
     private JPanel removeFaultPanel = null;
 
     private JPanel providerInfoPanel = null;
 
     private JPanel baseImportInfoPanel = null;
 
     private JPanel importTypeCardPanel = null;
 
     private JCheckBox isFromIntroduceCheckBox = null;
 
     private JPanel fromIntroducePanel = null;
 
     private JPanel notFromIntroducePanel = null;
 
     private JTextField introduceServiceLocationTextField = null;
 
     private JButton introduceServiceLocationBrowseButton = null;
 
     private JLabel introduceServiceLocationLabel = null;
 
     private JLabel introduceServiceServicesLabel = null;
 
     private JComboBox introduceServiceServicesComboBox = null;
 
     private JLabel introduceServiceOperationLabel = null;
 
     private JLabel wsdlFileLabel = null;
 
     private JTextField wsdlFileNameTextField = null;
 
     private JButton wsdlFileBrowseButton = null;
 
     private JComboBox wsdlServiceServicesComboBox = null;
 
     private Document currentImporWSDL;
 
     private ServiceDescription currentImportServiceSescription;
 
     private JLabel portTypeLabel = null;
 
     private JTextField wsdlImportPackageNameTextField = null;
 
     private JLabel wsdlImportPackageNameLabel = null;
 
     private JPanel descriptionPanel = null;
 
     private JTextField descriptionTextField = null;
 
     private ValidationResultModel methodNameValidationModel = new DefaultValidationResultModel(); // @jve:decl-index=0:
 
     private static final String METHOD_NAME = "Method name";
 
     private ValidationResultModel methodDescriptionValidationModel = new DefaultValidationResultModel(); // @jve:decl-index=0:
 
     private static final String METHOD_DESCRIPTION = "Method description";
 
     private ValidationResultModel methodProviderValidationModel = new DefaultValidationResultModel(); // @jve:decl-index=0:
 
     private static final String METHOD_PROVIDER = "Provider classname"; // @jve:decl-index=0:
 
     private ValidationResultModel methodFaultValidationModel = new DefaultValidationResultModel(); // @jve:decl-index=0:
 
     private static final String METHOD_FAULT = "New fault"; // @jve:decl-index=0:
 
     private ValidationResultModel methodImportValidationModel = new DefaultValidationResultModel(); // @jve:decl-index=0:
 
     private static final String METHOD_IMPORT_PACKAGE = "Method import package name"; // @jve:decl-index=0:
 
     private static final String METHOD_IMPORT_PORT_TYPE = "Method import port type";
 
     private static final String METHOD_IMPORT_WSDL_FILE = "Method import wsdl file";
 
     private boolean windowClosed = false;
 
 
     // @jve:decl-index=0:
 
     public MethodViewer(MethodType method, SpecificServiceInformation info) {
         super(GridApplication.getContext().getApplication());
         this.setModal(true);
         this.info = info;
         this.method = method;
         this.setTitle("Modify Method");
         initialize();
     }
 
 
     public boolean wasClosed() {
         return this.windowClosed;
     }
 
 
     private void initialize() {
 
         this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
         this.addWindowListener(new WindowAdapter() {
 
             public void windowClosing(WindowEvent e) {
                 super.windowClosed(e);
                 windowClosed = true;
                 setVisible(false);
                 dispose();
             }
 
         });
 
         this.setContentPane(getMainPanel());
         this.setTitle("Build/Modify Operation");
         this.setContentPane(getMainPanel());
         // this.setFrameIcon(IntroduceLookAndFeel.getModifyIcon());
 
         initMethodNameValidation();
         initMethodDescriptionValidation();
         initNewFaultValidation();
         initProviderValidation();
         initImportValidation();
 
         this.setSize(new Dimension(900, 700));
         GridApplication.getContext().centerDialog(this);
 
     }
 
 
     private void updateDoneButton() {
         if (methodNameValidationModel.hasErrors() || methodDescriptionValidationModel.hasErrors()
             || methodFaultValidationModel.hasErrors() || methodProviderValidationModel.hasErrors()
             || methodImportValidationModel.hasErrors()) {
             getDoneButton().setEnabled(false);
         } else {
             getDoneButton().setEnabled(true);
         }
     }
 
 
     private void initMethodNameValidation() {
         ValidationComponentUtils.setMessageKey(getNameField(), METHOD_NAME);
 
         validateMethodNameInput();
         updateMethodNameComponentTreeSeverity();
         updateDoneButton();
     }
 
 
     private void validateMethodNameInput() {
 
         ValidationResult result = new ValidationResult();
 
         if (ValidationUtils.isNotBlank(this.getNameField().getText())) {
             // First process the method name
             if (!CommonTools.isValidJavaMethod(getNameField().getText())) {
                 String message = "Method " + method.getName() + " contains invalid characters.";
                 result.add(new SimpleValidationMessage(message, Severity.ERROR, METHOD_NAME));
             }
 
             List usedNames = new ArrayList();
             MethodsType methodsType = info.getService().getMethods();
             if (methodsType != null) {
                 MethodType methods[] = methodsType.getMethod();
                 if (methods != null) {
                     for (int j = 0; j < methods.length; j++) {
                         MethodType tmethod = methods[j];
                         if (!usedNames.contains(tmethod.getName())) {
                             usedNames.add(tmethod.getName());
                         } else if (!tmethod.getName().equals(method.getName())) {
                             String message = "Method name is not unique: " + tmethod.getName();
                             result.add(new SimpleValidationMessage(message, Severity.ERROR, METHOD_NAME));
                         }
                     }
                 }
             }
 
             if (!method.getName().equals(getNameField().getText())) {
                 if (usedNames.contains(getNameField().getText())) {
                     String message = "Method name is not unique: " + getNameField().getText();
                     result.add(new SimpleValidationMessage(message, Severity.ERROR, METHOD_NAME));
                 }
             }
         } else {
             result.add(new SimpleValidationMessage(METHOD_NAME + " cannot be blank.", Severity.ERROR, METHOD_NAME));
         }
 
         this.methodNameValidationModel.setResult(result);
         updateMethodNameComponentTreeSeverity();
         updateDoneButton();
     }
 
 
     private void updateMethodNameComponentTreeSeverity() {
         ValidationComponentUtils.updateComponentTreeMandatoryAndBlankBackground(this);
         ValidationComponentUtils
             .updateComponentTreeSeverityBackground(this, this.methodNameValidationModel.getResult());
     }
 
 
     private void initMethodDescriptionValidation() {
         ValidationComponentUtils.setMessageKey(getDescriptionTextField(), METHOD_DESCRIPTION);
 
         validateDescriptionInput();
         updateMethodDescriptionComponentTreeSeverity();
         updateDoneButton();
     }
 
 
     private void validateDescriptionInput() {
 
         ValidationResult result = new ValidationResult();
 
         if (ValidationUtils.isBlank(this.getDescriptionTextField().getText())) {
             result.add(new SimpleValidationMessage(
                 "Method description is blank.\n If populated will be used to document the generated source code",
                 Severity.WARNING, METHOD_DESCRIPTION));
         }
 
         this.methodDescriptionValidationModel.setResult(result);
         updateMethodDescriptionComponentTreeSeverity();
         updateDoneButton();
     }
 
 
     private void updateMethodDescriptionComponentTreeSeverity() {
         ValidationComponentUtils.updateComponentTreeMandatoryAndBlankBackground(this);
         ValidationComponentUtils.updateComponentTreeSeverityBackground(this, this.methodDescriptionValidationModel
             .getResult());
     }
 
 
     private void initNewFaultValidation() {
         ValidationComponentUtils.setMessageKey(getNewFaultNameTextField(), METHOD_FAULT);
 
         validateNewFaultInput();
         updateNewFaultComponentTreeSeverity();
         updateAddNewFaultButton();
         updateDoneButton();
     }
 
 
     private void updateAddNewFaultButton() {
         if (ValidationUtils.isBlank(this.getNewFaultNameTextField().getText())) {
             getCreateFaultButton().setEnabled(false);
         } else if (methodFaultValidationModel.hasErrors()) {
             getCreateFaultButton().setEnabled(false);
         } else {
             getCreateFaultButton().setEnabled(true);
         }
     }
 
 
     private void validateNewFaultInput() {
 
         ValidationResult result = new ValidationResult();
 
         if (ValidationUtils.isNotBlank(this.getNewFaultNameTextField().getText())
             && !CommonTools.isValidClassName(this.getNewFaultNameTextField().getText())) {
             result.add(new SimpleValidationMessage("New fault must be a valid java class name format. ("
                 + CommonTools.ALLOWED_JAVA_CLASS_REGEX + ")", Severity.ERROR, METHOD_FAULT));
         }
 
         this.methodFaultValidationModel.setResult(result);
         updateNewFaultComponentTreeSeverity();
         updateAddNewFaultButton();
         updateDoneButton();
     }
 
 
     private void updateNewFaultComponentTreeSeverity() {
         ValidationComponentUtils.updateComponentTreeMandatoryAndBlankBackground(this.getExceptionsPanel());
         ValidationComponentUtils.updateComponentTreeSeverityBackground(this.getExceptionsPanel(),
             this.methodFaultValidationModel.getResult());
     }
 
 
     private void initProviderValidation() {
         ValidationComponentUtils.setMessageKey(getProviderClassnameTextField(), METHOD_PROVIDER);
 
         validateProviderInput();
         updateProviderComponentTreeSeverity();
         updateDoneButton();
     }
 
 
     private void validateProviderInput() {
 
         ValidationResult result = new ValidationResult();
 
         if (getIsProvidedCheckBox().isSelected()) {
             if (ValidationUtils.isNotBlank(this.getProviderClassnameTextField().getText())
                 && !CommonTools.isValidPackageAndClassName(this.getProviderClassnameTextField().getText())) {
                 result.add(new SimpleValidationMessage(
                     "Provider classname might not be in valid fully qualified java class name format. ("
                         + CommonTools.ALLOWED_JAVA_CLASS_REGEX + ")", Severity.WARNING, METHOD_PROVIDER));
             } else if (ValidationUtils.isBlank(this.getProviderClassnameTextField().getText())) {
                 result.add(new SimpleValidationMessage(
                     "If isProvided is selected you must provide the provider class name.", Severity.ERROR,
                     METHOD_PROVIDER));
             }
         }
 
         this.methodProviderValidationModel.setResult(result);
         updateProviderComponentTreeSeverity();
         updateDoneButton();
     }
 
 
     private void updateProviderComponentTreeSeverity() {
         ValidationComponentUtils.updateComponentTreeMandatoryAndBlankBackground(this.getProviderInfoPanel());
         ValidationComponentUtils.updateComponentTreeSeverityBackground(this.getProviderInfoPanel(),
             this.methodProviderValidationModel.getResult());
     }
 
 
     private void initImportValidation() {
         ValidationComponentUtils.setMessageKey(getWsdlImportPackageNameTextField(), METHOD_IMPORT_PACKAGE);
         ValidationComponentUtils.setMessageKey(getWsdlServiceServicesComboBox(), METHOD_IMPORT_PORT_TYPE);
         ValidationComponentUtils.setMessageKey(getWsdlFileNameTextField(), METHOD_IMPORT_WSDL_FILE);
 
         validateImportInput();
         updateImportComponentTreeSeverity();
         updateDoneButton();
     }
 
 
     private void validateImportInput() {
 
         ValidationResult result = new ValidationResult();
 
         if (getIsImportedCheckBox().isSelected()) {
             if (!getIsFromIntroduceCheckBox().isSelected()) {
 
                 if (ValidationUtils.isNotBlank(this.getWsdlImportPackageNameTextField().getText())
                     && !CommonTools.isValidPackageName(this.getWsdlImportPackageNameTextField().getText())) {
                     result.add(new SimpleValidationMessage("Package does not appear to be a valid java package name.",
                         Severity.WARNING, METHOD_IMPORT_PACKAGE));
                 } else if (ValidationUtils.isBlank(this.getWsdlImportPackageNameTextField().getText())) {
                     result.add(new SimpleValidationMessage("Package name cannot be black", Severity.ERROR,
                         METHOD_IMPORT_PACKAGE));
                 }
 
                 if (getWsdlServiceServicesComboBox().getItemCount() == 0) {
                     result
                         .add(new SimpleValidationMessage(
                             "You must browse to a wsdl document and choose the port type to import the definition of this operation from.",
                             Severity.ERROR, METHOD_IMPORT_PORT_TYPE));
                 }
 
                 if (ValidationUtils.isBlank(this.getWsdlFileNameTextField().getText())) {
                     result.add(new SimpleValidationMessage(
                         "You must browse to select a WSDL file containing the method description.", Severity.ERROR,
                         METHOD_IMPORT_WSDL_FILE));
                 }
 
             }
         }
         this.methodImportValidationModel.setResult(result);
         updateImportComponentTreeSeverity();
         updateDoneButton();
     }
 
 
     private void updateImportComponentTreeSeverity() {
         ValidationComponentUtils.updateComponentTreeMandatoryAndBlankBackground(this.getImportInformationPanel());
         ValidationComponentUtils.updateComponentTreeSeverityBackground(this.getImportInformationPanel(),
             this.methodImportValidationModel.getResult());
     }
 
 
     /**
      * This method initializes jPanel
      * 
      * @return javax.swing.JPanel
      */
     private JPanel getMainPanel() {
         if (mainPanel == null) {
             GridBagConstraints gridBagConstraints20 = new GridBagConstraints();
             gridBagConstraints20.gridx = 0;
             gridBagConstraints20.weighty = 0.0D;
             gridBagConstraints20.weightx = 1.0D;
             gridBagConstraints20.fill = java.awt.GridBagConstraints.BOTH;
             gridBagConstraints20.gridy = 0;
             GridBagConstraints gridBagConstraints9 = new GridBagConstraints();
             gridBagConstraints9.fill = java.awt.GridBagConstraints.BOTH;
             gridBagConstraints9.weighty = 1.0;
             gridBagConstraints9.gridx = 0;
             gridBagConstraints9.gridy = 1;
             gridBagConstraints9.weightx = 1.0;
             GridBagConstraints gridBagConstraints10 = new GridBagConstraints();
             mainPanel = new JPanel();
             mainPanel.setLayout(new GridBagLayout());
             gridBagConstraints10.gridx = 0;
             gridBagConstraints10.gridy = 4;
             gridBagConstraints10.insets = new java.awt.Insets(2, 2, 2, 2);
             gridBagConstraints10.weightx = 0.0D;
             gridBagConstraints10.weighty = 0.0D;
             gridBagConstraints10.fill = java.awt.GridBagConstraints.BOTH;
             mainPanel.add(getButtonPanel(), gridBagConstraints10);
             mainPanel.add(getTabbedPanel(), gridBagConstraints9);
             mainPanel.add(getMethodPropertiesPanel(), gridBagConstraints20);
         }
         return mainPanel;
     }
 
 
     /**
      * This method initializes jScrollPane
      * 
      * @return javax.swing.JScrollPane
      */
     private JScrollPane getInputParamScrollPanel() {
         if (inputParamScrollPanel == null) {
             inputParamScrollPanel = new JScrollPane();
             inputParamScrollPanel.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
             inputParamScrollPanel.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
             inputParamScrollPanel.setViewportView(getInputParamTable());
         }
         return inputParamScrollPanel;
     }
 
 
     /**
      * This method initializes jTable
      * 
      * @return javax.swing.JTable
      */
     private InputParametersTable getInputParamTable() {
         if (inputParamTable == null) {
             inputParamTable = new InputParametersTable(method);
         }
         return inputParamTable;
     }
 
 
     /**
      * This method initializes jScrollPane
      * 
      * @return javax.swing.JScrollPane
      */
     private JScrollPane getOutputTypejScrollPane() {
         if (outputTypejScrollPane == null) {
             outputTypejScrollPane = new JScrollPane();
             outputTypejScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
             outputTypejScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
             outputTypejScrollPane.setViewportView(getOutputTypeTable());
         }
         return outputTypejScrollPane;
     }
 
 
     /**
      * This method initializes jTable
      * 
      * @return javax.swing.JTable
      */
     private OutputTypeTable getOutputTypeTable() {
         if (outputTypeTable == null) {
             outputTypeTable = new OutputTypeTable(method);
         }
         return outputTypeTable;
     }
 
 
     /**
      * This method initializes jPanel
      * 
      * @return javax.swing.JPanel
      */
     private JPanel getButtonPanel() {
         if (buttonPanel == null) {
             buttonPanel = new JPanel();
             buttonPanel.setLayout(new FlowLayout());
             buttonPanel.add(getDoneButton(), null);
         }
         return buttonPanel;
     }
 
 
     /**
      * This method initializes jButton
      * 
      * @return javax.swing.JButton
      */
     public JButton getDoneButton() {
         if (doneButton == null) {
             doneButton = new JButton(IntroduceLookAndFeel.getDoneIcon());
             doneButton.setText("Done");
             doneButton.addActionListener(new java.awt.event.ActionListener() {
                 public void actionPerformed(java.awt.event.ActionEvent e) {
                     // reset selection on table incase it was being edited....
                     int column = getInputParamTable().getEditingColumn();
                     if (column > -1) {
                         TableCellEditor cellEditor = getInputParamTable().getColumnModel().getColumn(column)
                             .getCellEditor();
                         if (cellEditor == null) {
                             cellEditor = getInputParamTable().getDefaultEditor(
                                 getInputParamTable().getColumnClass(column));
                         }
                         if (cellEditor != null) {
                             cellEditor.stopCellEditing();
                         }
                     }
 
                     boolean valid = true;
                     String message = "";
 
                     try {
                         method.setName(getNameField().getText());
 
                         method.setDescription(getDescriptionTextField().getText());
 
                         try {
                             method.setMethodSecurity(((MethodSecurityPanel) securityContainerPanel)
                                 .getMethodSecurity(method.getName()));
                         } catch (Exception e2) {
                             valid = false;
                             message = e2.getMessage();
                         }
 
                         if (!getIsImportedCheckBox().isSelected()) {
 
                             // process the inputs
                             MethodTypeInputs inputs = new MethodTypeInputs();
                             MethodTypeInputsInput[] inputsA = new MethodTypeInputsInput[getInputParamTable()
                                 .getRowCount()];
 
                             List usedNames = new ArrayList();
                             for (int i = 0; i < getInputParamTable().getRowCount(); i++) {
                                 MethodTypeInputsInput input = getInputParamTable().getRowData(i);
                                 // validate the input param
                                 if (usedNames.contains(input.getName())) {
                                     valid = false;
                                     message = "Method " + method.getName() + " contains more that one parameter named "
                                         + input.getName();
                                 }
                                 usedNames.add(input.getName());
                                 if (!JavaUtils.isJavaId(input.getName())) {
                                     valid = false;
                                     message = "Parameter name must be a valid java identifier: Method: "
                                         + method.getName() + " param: " + input.getName();
                                 }
                                 inputsA[i] = input;
                             }
 
                             inputs.setInput(inputsA);
                             method.setInputs(inputs);
 
                             // process exceptions
                             MethodTypeExceptions exceptions = new MethodTypeExceptions();
                             MethodTypeExceptionsException[] exceptionsA = new MethodTypeExceptionsException[getExceptionsTable()
                                 .getRowCount()];
                             for (int i = 0; i < getExceptionsTable().getRowCount(); i++) {
                                 MethodTypeExceptionsException exception = getExceptionsTable().getRowData(i);
                                 exceptionsA[i] = exception;
                             }
                             exceptions.setException(exceptionsA);
                             method.setExceptions(exceptions);
 
                             // now process the output
                             MethodTypeOutput outputT = getOutputTypeTable().getRowData(0);
                             method.setOutput(outputT);
                         }
 
                         if (getIsProvidedCheckBox().isSelected()) {
                             if ((getProviderClassnameTextField().getText() == null)
                                 || (getProviderClassnameTextField().getText().length() <= 0)) {
                                 JOptionPane
                                     .showMessageDialog(MethodViewer.this,
                                         "Please fill out the \"Provider Information\" tab or uncheck the \"Provided\" checkbox.");
                                 return;
                             }
                             method.setIsProvided(true);
                             MethodTypeProviderInformation pi = new MethodTypeProviderInformation();
                             pi.setProviderClass(getProviderClassnameTextField().getText());
                             method.setProviderInformation(pi);
                         } else {
                             method.setIsProvided(false);
                         }
 
                         if (getIsImportedCheckBox().isSelected()) {
                             if (!method.isIsImported()) {
                                 method.setIsImported(true);
                                 if (getIsFromIntroduceCheckBox().isSelected()) {
                                     // // //this method is to be imported from
                                     // introduce....
 
                                     if (((ServiceHolder) introduceServiceServicesComboBox.getSelectedItem()) == null) {
                                         JOptionPane
                                             .showMessageDialog(MethodViewer.this,
                                                 "Please browse to an Introduce generated service and select the service from which to import this method.");
                                         return;
                                     }
 
                                     ServiceType importService = ((ServiceHolder) introduceServiceServicesComboBox
                                         .getSelectedItem()).getService();
                                     MethodType importMethod = CommonTools.getMethod(importService.getMethods(),
                                         getNameField().getText());
                                     if (importMethod.isIsImported()) {
                                         JOptionPane.showMessageDialog(MethodViewer.this,
                                             "Cannot import an method which itself is an imported method");
                                         return;
                                     }
                                     List requiredNamespaces = new ArrayList();
                                     if ((importMethod.getInputs() != null)
                                         && (importMethod.getInputs().getInput() != null)) {
                                         for (int inputI = 0; inputI < importMethod.getInputs().getInput().length; inputI++) {
                                             MethodTypeInputsInput input = importMethod.getInputs().getInput(inputI);
                                             if (!requiredNamespaces.contains(input.getQName().getNamespaceURI())) {
                                                 requiredNamespaces.add(input.getQName().getNamespaceURI());
                                             }
                                         }
                                     }
                                     if (importMethod.getOutput() != null
                                         && importMethod.getOutput().getQName().getNamespaceURI().length() > 0
                                         && !importMethod.getOutput().getQName().getLocalPart().equals("void")) {
                                         if (!requiredNamespaces.contains(importMethod.getOutput().getQName()
                                             .getNamespaceURI())) {
                                             requiredNamespaces.add(importMethod.getOutput().getQName()
                                                 .getNamespaceURI());
                                         }
                                     }
                                     if ((importMethod.getExceptions() != null)
                                         && (importMethod.getExceptions().getException() != null)) {
                                         for (int exceptionI = 0; exceptionI < importMethod.getExceptions()
                                             .getException().length; exceptionI++) {
                                             if (!requiredNamespaces.contains(importMethod.getExceptions().getException(
                                                 exceptionI).getQname().getNamespaceURI())) {
                                                 requiredNamespaces.add(importMethod.getExceptions().getException(
                                                     exceptionI).getQname().getNamespaceURI());
                                             }
                                         }
                                     }
                                     // for each of the requred namespaces that i
                                     // don't already have i need to copy them
                                     // over
                                     for (int nsI = 0; nsI < requiredNamespaces.size(); nsI++) {
                                         String uri = (String) requiredNamespaces.get(nsI);
                                         if (CommonTools.getNamespaceType(info.getNamespaces(), uri) == null) {
                                             JOptionPane
                                                 .showMessageDialog(MethodViewer.this,
                                                     "There are namespaces/types that are used by the imported method which are not yet imported into this service");
                                             return;
                                         }
                                     }
 
                                     String remoteWsdlFile = introduceServiceLocationTextField.getText()
                                         + File.separator + "schema" + File.separator
                                         + currentImportServiceSescription.getServices().getService(0).getName()
                                         + File.separator + importService.getName() + ".wsdl";
 
                                     if (!(new File(remoteWsdlFile).exists())) {
                                         JOptionPane.showMessageDialog(MethodViewer.this,
                                             "Cannot locate the WSDL file for this imported method");
                                     }
 
                                     Document remoteWsdlDoc = null;
                                     try {
                                         remoteWsdlDoc = XMLUtilities.fileNameToDocument(remoteWsdlFile);
                                     } catch (Exception e1) {
                                         logger.error("ERROR", e1);
                                         return;
                                     }
 
                                     List portTypes = remoteWsdlDoc.getRootElement().getChildren("portType",
                                         Namespace.getNamespace(IntroduceConstants.WSDLAMESPACE));
 
                                     boolean foundMethod = false;
                                     Element methodEl = null;
                                     for (int portTypeI = 0; portTypeI < portTypes.size(); portTypeI++) {
                                         Element portTypeEl = (Element) portTypes.get(portTypeI);
                                         if (portTypeEl.getAttributeValue("name").equals(
                                             importService.getName() + "PortType")) {
                                             List operationEls = portTypeEl.getChildren("operation", Namespace
                                                 .getNamespace(IntroduceConstants.WSDLAMESPACE));
                                             for (int opI = 0; opI < operationEls.size(); opI++) {
                                                 Element opEl = (Element) operationEls.get(opI);
                                                 if (opEl.getAttributeValue("name").equals(getNameField().getText())) {
                                                     foundMethod = true;
                                                     methodEl = opEl;
                                                     break;
                                                 }
                                             }
                                             break;
                                         }
                                     }
 
                                     if (!foundMethod) {
                                         JOptionPane.showMessageDialog(MethodViewer.this,
                                             "Cannot find method in imported services wsdl document");
                                         return;
                                     }
                                     // get the message types
 
                                     Element input = methodEl.getChild("input", Namespace
                                         .getNamespace(IntroduceConstants.WSDLAMESPACE));
                                     String inputMessageType = input.getAttributeValue("message");
                                     int colonIndex = inputMessageType.indexOf(":");
                                     String inputMessageNamespace = remoteWsdlDoc.getRootElement().getNamespace(
                                         inputMessageType.substring(0, colonIndex)).getURI();
                                     String inputMessageName = inputMessageType.substring(colonIndex + 1);
                                     // get the outputMessage
                                     Element output = methodEl.getChild("output", Namespace
                                         .getNamespace(IntroduceConstants.WSDLAMESPACE));
                                     String outputMessageType = output.getAttributeValue("message");
                                     colonIndex = outputMessageType.indexOf(":");
                                     String outputMessageNamespace = remoteWsdlDoc.getRootElement().getNamespace(
                                         outputMessageType.substring(0, colonIndex)).getURI();
                                     String outputMessageName = outputMessageType.substring(colonIndex + 1);
 
                                     // so far we are valid.
                                     // copy over the imports outputs and etc
                                     method.setInputs(importMethod.getInputs());
                                     method.setOutput(importMethod.getOutput());
                                     method.setExceptions(importMethod.getExceptions());
 
                                     String localWsdlFileName = remoteWsdlFile.substring(remoteWsdlFile
                                         .lastIndexOf(File.separator) + 1);
                                     String localWsdlFileLocation = info.getBaseDirectory() + File.separator + "schema"
                                         + File.separator + info.getServices().getService(0).getName() + File.separator
                                         + localWsdlFileName;
 
                                     String mess = "You must make sure that the WSDL file containing the imported method ("
                                         + remoteWsdlFile
                                         + ") and all accompanying XSD documents have been copied over to this services schema location ("
                                         + info.getBaseDirectory()
                                         + File.separator
                                         + "schema"
                                         + File.separator
                                         + info.getServices().getService(0).getName() + ")";
                                     JOptionPane.showMessageDialog(MethodViewer.this, mess);
                                     while (!(new File(localWsdlFileLocation).exists())) {
                                         JOptionPane.showMessageDialog(MethodViewer.this, mess);
                                     }
 
                                     MethodTypeImportInformation importInfo = new MethodTypeImportInformation();
                                     importInfo.setWsdlFile(localWsdlFileName);
                                     importInfo.setNamespace(importService.getNamespace());
                                     importInfo.setPortTypeName(importService.getName() + "PortType");
                                     importInfo.setPackageName(importService.getPackageName());
                                     importInfo.setInputMessage(new QName(inputMessageNamespace, inputMessageName));
                                     importInfo.setOutputMessage(new QName(outputMessageNamespace, outputMessageName));
                                     method.setImportInformation(importInfo);
                                     method.setIsImported(true);
 
                                 } else {
                                     // this method is to be imported from WSDL
                                     // prep the informaiton needed....
                                     String namespace = currentImporWSDL.getRootElement().getAttributeValue(
                                         "targetNamespace");
                                     Element methodEl = ((ElementHolder) getWsdlServiceServicesComboBox()
                                         .getSelectedItem()).getMethodElement();
 
                                     // get the inputMessage
                                     Element input = methodEl.getChild("input", Namespace
                                         .getNamespace(IntroduceConstants.WSDLAMESPACE));
                                     String inputMessageType = input.getAttributeValue("message");
                                     int colonIndex = inputMessageType.indexOf(":");
                                     String inputMessageNamespace = currentImporWSDL.getRootElement().getNamespace(
                                         inputMessageType.substring(0, colonIndex)).getURI();
                                     String inputMessageName = inputMessageType.substring(colonIndex + 1);
                                     // get the outputMessage
                                     Element output = methodEl.getChild("output", Namespace
                                         .getNamespace(IntroduceConstants.WSDLAMESPACE));
                                     String outputMessageType = output.getAttributeValue("message");
                                     colonIndex = outputMessageType.indexOf(":");
                                     String outputMessageNamespace = currentImporWSDL.getRootElement().getNamespace(
                                         outputMessageType.substring(0, colonIndex)).getURI();
                                     String outputMessageName = outputMessageType.substring(colonIndex + 1);
 
                                     MethodTypeImportInformation importInfo = new MethodTypeImportInformation();
                                     importInfo.setFromIntroduce(new Boolean(false));
                                     importInfo.setNamespace(namespace);
                                     importInfo.setWsdlFile(getWsdlFileNameTextField().getText());
                                     importInfo.setPortTypeName(((ElementHolder) getWsdlServiceServicesComboBox()
                                         .getSelectedItem()).getServiceElement().getAttributeValue("name"));
                                     importInfo.setInputMessage(new QName(inputMessageNamespace, inputMessageName));
                                     importInfo.setOutputMessage(new QName(outputMessageNamespace, outputMessageName));
                                     importInfo.setPackageName(wsdlImportPackageNameTextField.getText());
                                     method.setImportInformation(importInfo);
                                     method.setIsImported(true);
                                 }
                             } else {
                                 method.setIsImported(true);
                             }
                         } else {
                             method.setIsImported(false);
                         }
 
                     } catch (Exception ex) {
                         logger.warn("WARNING", ex);
                         // PortalUtils.showErrorDialog(ex);
                         CompositeErrorDialog.showErrorDialog(ex);
                     }
                     if (!valid) {
                         JOptionPane.showMessageDialog(MethodViewer.this, message);
                     } else {
                         dispose();
                     }
                 }
 
             });
 
         }
         return doneButton;
     }
 
 
     /**
      * This method initializes jButton
      * 
      * @return javax.swing.JButton
      */
     private JButton getAddInputParamButton() {
         if (addInputParamButton == null) {
             addInputParamButton = new JButton(PortalLookAndFeel.getAddIcon());
             addInputParamButton.setText("Add");
             addInputParamButton.addActionListener(new java.awt.event.ActionListener() {
                 public void actionPerformed(java.awt.event.ActionEvent e) {
                     if (getInputNamespaceTypesJTree().getCurrentNode() instanceof SchemaElementTypeTreeNode) {
                         NamespaceType nt = ((NamespaceType) ((NamespaceTypeTreeNode) getInputNamespaceTypesJTree()
                             .getCurrentNode().getParent()).getUserObject());
                         SchemaElementType st = ((SchemaElementType) ((SchemaElementTypeTreeNode) getInputNamespaceTypesJTree()
                             .getCurrentNode()).getUserObject());
                         MethodTypeInputsInput input = new MethodTypeInputsInput();
                         input.setQName(new QName(nt.getNamespace(), st.getType()));
                         input.setIsArray(false);
                         input.setName(CommonTools.lowerCaseFirstCharacter(JavaUtils.xmlNameToJava(st.getType())));
                         getInputParamTable().addRow(input);
                     } else {
                         JOptionPane.showMessageDialog(MethodViewer.this, "Please select a type to add");
                     }
                 }
 
             });
         }
         return addInputParamButton;
     }
 
 
     /**
      * This method initializes jPanel
      * 
      * @return javax.swing.JPanel
      */
     private JPanel getNamePanel() {
         if (namePanel == null) {
             GridBagConstraints gridBagConstraints111 = new GridBagConstraints();
             gridBagConstraints111.anchor = GridBagConstraints.CENTER;
             gridBagConstraints111.insets = new java.awt.Insets(0, 30, 0, 0);
             gridBagConstraints111.gridwidth = 2;
             gridBagConstraints111.gridx = 2;
             gridBagConstraints111.gridy = 2;
             gridBagConstraints111.weightx = 0.0D;
             gridBagConstraints111.fill = GridBagConstraints.HORIZONTAL;
             GridBagConstraints gridBagConstraints110 = new GridBagConstraints();
             gridBagConstraints110.gridx = 2;
             gridBagConstraints110.insets = new java.awt.Insets(0, 30, 0, 0);
             gridBagConstraints110.gridy = 0;
             methodLabel = new JLabel();
             methodLabel.setText("Method Name");
 
             GridBagConstraints gridBagConstraints = new GridBagConstraints();
             gridBagConstraints.gridx = 0;
             gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
             gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
             gridBagConstraints.gridheight = 2;
             gridBagConstraints.gridy = 0;
             GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
             namePanel = new JPanel();
             namePanel.setLayout(new GridBagLayout());
             namePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Method Properties",
                 javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                 javax.swing.border.TitledBorder.DEFAULT_POSITION, null, PortalLookAndFeel.getPanelLabelColor()));
             gridBagConstraints2.gridx = 1;
             gridBagConstraints2.gridheight = 2;
             gridBagConstraints2.gridwidth = 1;
             gridBagConstraints2.anchor = java.awt.GridBagConstraints.CENTER;
             gridBagConstraints2.gridy = 0;
             gridBagConstraints2.weightx = 1.0D;
             gridBagConstraints2.fill = java.awt.GridBagConstraints.HORIZONTAL;
             gridBagConstraints2.insets = new java.awt.Insets(2, 2, 2, 2);
             namePanel.add(getNameField(), gridBagConstraints2);
             namePanel.add(methodLabel, gridBagConstraints);
             namePanel.add(getIsImportedCheckBox(), gridBagConstraints110);
             namePanel.add(getIsProvidedCheckBox(), gridBagConstraints111);
         }
         return namePanel;
     }
 
 
     /**
      * This method initializes jTextField
      * 
      * @return javax.swing.JTextField
      */
     private JTextField getNameField() {
         if (nameField == null) {
             nameField = new JTextField();
             nameField.setText(method.getName());
             if (method.isIsImported()) {
                 nameField.setEnabled(false);
                 nameField.setEditable(false);
             }
             nameField.getDocument().addDocumentListener(new DocumentListener() {
 
                 public void removeUpdate(DocumentEvent e) {
                     validateMethodNameInput();
                 }
 
 
                 public void insertUpdate(DocumentEvent e) {
                     validateMethodNameInput();
                 }
 
 
                 public void changedUpdate(DocumentEvent e) {
                     validateMethodNameInput();
                 }
 
             });
             // nameField.setText(methodsTable.getSelectedMethodType().getName());
         }
         return nameField;
     }
 
 
     /**
      * This method initializes jButton
      * 
      * @return javax.swing.JButton
      */
     private JButton getRemoveButton() {
         if (removeButton == null) {
             removeButton = new JButton(PortalLookAndFeel.getRemoveIcon());
             removeButton.setText("Remove");
             removeButton.addActionListener(new java.awt.event.ActionListener() {
                 public void actionPerformed(java.awt.event.ActionEvent e) {
                     try {
                         getInputParamTable().removeSelectedRow();
                     } catch (Exception ex) {
                         ErrorDialog.showError("Please select an input parameter to Remove");
                     }
                 }
             });
         }
         return removeButton;
     }
 
 
     /**
      * This method initializes inputButtonPanel
      * 
      * @return javax.swing.JPanel
      */
     private JPanel getInputButtonPanel() {
         if (inputButtonPanel == null) {
             inputButtonPanel = new JPanel();
             inputButtonPanel.add(getAddInputParamButton(), null);
             inputButtonPanel.add(getRemoveButton(), null);
         }
         return inputButtonPanel;
     }
 
 
     /**
      * This method initializes exceptionsPanel
      */
     private JPanel getExceptionsPanel() {
         if (exceptionsPanel == null) {
             GridBagConstraints gridBagConstraints49 = new GridBagConstraints();
             gridBagConstraints49.gridx = 1;
             gridBagConstraints49.gridheight = 3;
             gridBagConstraints49.gridy = 1;
             GridBagConstraints gridBagConstraints46 = new GridBagConstraints();
             gridBagConstraints46.gridx = 0;
             gridBagConstraints46.fill = java.awt.GridBagConstraints.BOTH;
             gridBagConstraints46.weightx = 1.0D;
             gridBagConstraints46.gridy = 1;
             GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
             gridBagConstraints11.fill = GridBagConstraints.BOTH;
             gridBagConstraints11.gridy = 2;
             gridBagConstraints11.weightx = 1.0D;
             gridBagConstraints11.gridx = 0;
             GridBagConstraints gridBagConstraints12 = new GridBagConstraints();
             gridBagConstraints12.fill = java.awt.GridBagConstraints.BOTH;
             gridBagConstraints12.gridy = 3;
             gridBagConstraints12.weightx = 1.0D;
             gridBagConstraints12.gridx = 0;
             GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
             gridBagConstraints3.fill = java.awt.GridBagConstraints.BOTH;
             gridBagConstraints3.weighty = 1.0;
             gridBagConstraints3.gridx = 0;
             gridBagConstraints3.gridy = 0;
             gridBagConstraints3.gridwidth = 2;
             gridBagConstraints3.weightx = 1.0;
             exceptionsPanel = new JPanel();
             exceptionsPanel.setLayout(new GridBagLayout());
             exceptionsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Faults",
                 javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                 javax.swing.border.TitledBorder.DEFAULT_POSITION, null, PortalLookAndFeel.getPanelLabelColor()));
             exceptionsPanel.add(getExceptionsPanelSplitPane(), gridBagConstraints3);
             exceptionsPanel.add(new IconFeedbackPanel(methodFaultValidationModel, getCreateFaultPanel()),
                 gridBagConstraints12);
             exceptionsPanel.add(getExceptionInputPanel(), gridBagConstraints11);
             exceptionsPanel.add(getFaultsFromTypesPanel(), gridBagConstraints46);
             exceptionsPanel.add(getRemoveFaultPanel(), gridBagConstraints49);
 
         }
         return exceptionsPanel;
     }
 
 
     /**
      * This method initializes jScrollPane
      * 
      * @return javax.swing.JScrollPane
      */
     private JScrollPane getExceptionScrollPane() {
         if (exceptionScrollPane == null) {
             exceptionScrollPane = new JScrollPane();
             exceptionScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
             exceptionScrollPane.setViewportView(getExceptionsTable());
         }
         return exceptionScrollPane;
     }
 
 
     /**
      * This method initializes exceptionInputPanel
      * 
      * @return javax.swing.JPanel
      */
     private JPanel getExceptionInputPanel() {
         if (exceptionInputPanel == null) {
             GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
             gridBagConstraints4.insets = new Insets(2, 2, 2, 2);
             gridBagConstraints4.gridy = 0;
             gridBagConstraints4.gridx = 2;
             GridBagConstraints gridBagConstraints51 = new GridBagConstraints();
             gridBagConstraints51.gridx = 0;
             gridBagConstraints51.fill = java.awt.GridBagConstraints.HORIZONTAL;
             gridBagConstraints51.insets = new java.awt.Insets(2, 2, 2, 10);
             gridBagConstraints51.gridy = 0;
             existingExceptionLabel = new JLabel();
             existingExceptionLabel.setText("Used Faults:");
             GridBagConstraints gridBagConstraints27 = new GridBagConstraints();
             gridBagConstraints27.gridx = 2;
             gridBagConstraints27.fill = java.awt.GridBagConstraints.BOTH;
             gridBagConstraints27.gridheight = 2;
             gridBagConstraints27.gridy = 1;
             GridBagConstraints gridBagConstraints15 = new GridBagConstraints();
             gridBagConstraints15.fill = GridBagConstraints.HORIZONTAL;
             gridBagConstraints15.gridx = 1;
             gridBagConstraints15.gridy = 0;
             gridBagConstraints15.weightx = 1.0;
             gridBagConstraints15.insets = new java.awt.Insets(2, 2, 2, 2);
             gridBagConstraints15.gridheight = 1;
             exceptionInputPanel = new JPanel();
             exceptionInputPanel.setLayout(new GridBagLayout());
             exceptionInputPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Choose Used Fault",
                 javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                 javax.swing.border.TitledBorder.DEFAULT_POSITION, null, PortalLookAndFeel.getPanelLabelColor()));
             exceptionInputPanel.add(getExceptionJComboBox(), gridBagConstraints15);
             exceptionInputPanel.add(getExceptionsInputButtonPanel(), gridBagConstraints27);
             exceptionInputPanel.add(existingExceptionLabel, gridBagConstraints51);
             exceptionInputPanel.add(getAddExceptionButton(), gridBagConstraints4);
         }
         return exceptionInputPanel;
     }
 
 
     /**
      * This method initializes faultsTable
      * 
      * @return javax.swing.JTable
      */
     private ExceptionsTable getExceptionsTable() {
         if (exceptionsTable == null) {
             exceptionsTable = new ExceptionsTable(method, info.getService());
         }
         return exceptionsTable;
     }
 
 
     /**
      * This method initializes addExceptionButton
      * 
      * @return javax.swing.JButton
      */
     private JButton getAddExceptionButton() {
         if (addExceptionButton == null) {
             addExceptionButton = new JButton(PortalLookAndFeel.getAddIcon());
             addExceptionButton.setText("Add Used Fault");
             addExceptionButton.addActionListener(new java.awt.event.ActionListener() {
                 public void actionPerformed(java.awt.event.ActionEvent e) {
                     ExceptionHolder exceptionHolder = null;
                     if (getExceptionJComboBox().getSelectedItem() != null) {
                         exceptionHolder = (ExceptionHolder) getExceptionJComboBox().getSelectedItem();
                     }
                     if (exceptionHolder != null) {
                         // parse qname string into qname
 
                         for (int i = 0; i < getExceptionsTable().getRowCount(); i++) {
                             MethodTypeExceptionsException exception = null;
                             try {
                                 exception = getExceptionsTable().getRowData(i);
                             } catch (Exception e1) {
                                 logger.error("Exception getting data from exceptions table", e1);
                             }
                             if ((exception != null) && (exception.getQname() != null)
                                 && exception.getQname().equals(exceptionHolder.qname)) {
                                 JOptionPane.showMessageDialog(MethodViewer.this, "Exception (" + exceptionHolder
                                     + ") already thrown by method.");
                                 return;
                             }
                         }
                         getExceptionsTable().addRow(exceptionHolder.qname, exceptionHolder.isCreated, "");
                     } else {
                         JOptionPane.showMessageDialog(MethodViewer.this, "Please select an exception first!");
                     }
                 }
             });
         }
         return addExceptionButton;
     }
 
 
     /**
      * This method initializes removeExceptionButton
      * 
      * @return javax.swing.JButton
      */
     private JButton getRemoveExceptionButton() {
         if (removeExceptionButton == null) {
             removeExceptionButton = new JButton(PortalLookAndFeel.getRemoveIcon());
             removeExceptionButton.setText("Remove");
             removeExceptionButton.addActionListener(new java.awt.event.ActionListener() {
                 public void actionPerformed(java.awt.event.ActionEvent e) {
                     try {
                         getExceptionsTable().removeSelectedRow();
                     } catch (Exception ex) {
                         ErrorDialog.showError("Please select an exception to Remove");
                     }
                 }
             });
         }
         return removeExceptionButton;
     }
 
 
     /**
      * This method initializes tabbedPanel
      * 
      * @return javax.swing.JTabbedPane
      */
     private JTabbedPane getTabbedPanel() {
         if (tabbedPanel == null) {
             tabbedPanel = new JTabbedPane();
             tabbedPanel.addTab("Signature", null, getMethodPanel(), null);
             tabbedPanel.addTab("Security", null, getSecurityContainerPanel(), null);
             tabbedPanel.addTab("Provider Information", null, getProviderInfoPanel(), null);
             tabbedPanel.addTab("Import Information", null, getImportInformationPanel(), null);
             tabbedPanel.addChangeListener(new ChangeListener() {
                 public void stateChanged(ChangeEvent e) {
                     updateMethodDescriptionComponentTreeSeverity();
                     updateNewFaultComponentTreeSeverity();
                     updateProviderComponentTreeSeverity();
                     updateImportComponentTreeSeverity();
                 }
 
             });
         }
         return tabbedPanel;
     }
 
 
     /**
      * This method initializes methodPanel
      * 
      * @return javax.swing.JPanel
      */
     private JPanel getMethodPanel() {
         if (methodPanel == null) {
             GridBagConstraints gridBagConstraints43 = new GridBagConstraints();
             gridBagConstraints43.gridx = 0;
             gridBagConstraints43.fill = GridBagConstraints.BOTH;
             gridBagConstraints43.gridy = 0;
             GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
             gridBagConstraints1.fill = GridBagConstraints.BOTH;
             gridBagConstraints1.gridx = 0;
             gridBagConstraints1.gridy = 1;
             gridBagConstraints1.weightx = 1.0;
             gridBagConstraints1.weighty = 1.0;
             gridBagConstraints1.insets = new Insets(2, 2, 2, 2);
             methodPanel = new JPanel();
             methodPanel.setLayout(new GridBagLayout());
             methodPanel.add(getConfigureTabbedPane(), gridBagConstraints1);
             methodPanel.add(new IconFeedbackPanel(methodDescriptionValidationModel, getDescriptionPanel()),
                 gridBagConstraints43);
         }
         return methodPanel;
     }
 
 
     /**
      * This method initializes securityContainerPanel
      * 
      * @return javax.swing.JPanel
      */
     private JPanel getSecurityContainerPanel() {
         if (securityContainerPanel == null) {
             securityContainerPanel = new MethodSecurityPanel(info, info.getService(), method);
             securityContainerPanel.setBorder(BorderFactory.createTitledBorder(null,
                 "Method Level Security Configuration", TitledBorder.DEFAULT_JUSTIFICATION,
                 TitledBorder.DEFAULT_POSITION, null, PortalLookAndFeel.getPanelLabelColor()));
         }
         return securityContainerPanel;
     }
 
 
     /**
      * This method initializes configureTabbedPane
      * 
      * @return javax.swing.JTabbedPane
      */
     private JTabbedPane getConfigureTabbedPane() {
         if (configureTabbedPane == null) {
             configureTabbedPane = new JTabbedPane();
             configureTabbedPane.addTab("Inputs", null, getInputParamsSplitPane(), null);
             configureTabbedPane.addTab("Output", null, getOutputTypeSplitPane(), null);
             configureTabbedPane.addTab("Faults", null, getExceptionsPanel(), null);
         }
         return configureTabbedPane;
     }
 
 
     /**
      * This method initializes exceptionEditText
      * 
      * @return javax.swing.JTextField
      */
     private JComboBox getExceptionJComboBox() {
         if (exceptionJComboBox == null) {
             exceptionJComboBox = new JComboBox();
             // populate with currently used exception names
             ServiceType[] services = info.getServices().getService();
             SortedSet exceptionNameSet = new TreeSet();
             for (int i = 0; (services != null) && (i < services.length); i++) {
                 MethodsType methodsType = services[i].getMethods();
                 if (methodsType != null) {
                     MethodType methods[] = methodsType.getMethod();
                     for (int j = 0; (methods != null) && (j < methods.length); j++) {
                         MethodTypeExceptions exceptionsType = methods[j].getExceptions();
                         if (exceptionsType != null) {
                             MethodTypeExceptionsException[] exceptions = exceptionsType.getException();
                             for (int e = 0; (exceptions != null) && (e < exceptions.length); e++) {
                                 if (exceptions[e].getQname() != null) {
                                     exceptionNameSet.add(new ExceptionHolder(exceptions[e].getQname(), true));
                                 } else {
                                     exceptionNameSet.add(new ExceptionHolder(new QName(info.getService().getNamespace()
                                         + "/types", exceptions[e].getName()), false));
                                 }
                             }
                         }
                     }
                 }
             }
             for (Iterator iter = exceptionNameSet.iterator(); iter.hasNext();) {
                 exceptionJComboBox.addItem(iter.next());
             }
         }
         return exceptionJComboBox;
     }
 
 
     /**
      * This method initializes namespacesPanel
      * 
      * @return javax.swing.JPanel
      */
     private JPanel getInputNamespacesPanel() {
         if (inputNamespacesPanel == null) {
             GridBagConstraints gridBagConstraints19 = new GridBagConstraints();
             gridBagConstraints19.fill = java.awt.GridBagConstraints.BOTH;
             gridBagConstraints19.weighty = 1.0;
             gridBagConstraints19.gridx = 0;
             gridBagConstraints19.gridy = 0;
             gridBagConstraints19.insets = new java.awt.Insets(0, 0, 0, 0);
             gridBagConstraints19.weightx = 1.0;
             inputNamespacesPanel = new JPanel();
             inputNamespacesPanel.setLayout(new GridBagLayout());
             inputNamespacesPanel.add(getInputNamespaceScrollPane(), gridBagConstraints19);
         }
         return inputNamespacesPanel;
     }
 
 
     /**
      * This method initializes namespaceScrollPane
      * 
      * @return javax.swing.JScrollPane
      */
     private JScrollPane getInputNamespaceScrollPane() {
         if (inputNamespaceScrollPane == null) {
             inputNamespaceScrollPane = new JScrollPane();
             inputNamespaceScrollPane.setViewportView(getInputNamespaceTypesJTree());
             inputNamespaceScrollPane.setPreferredSize(new Dimension(200, 200));
             inputNamespaceScrollPane.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Data Types",
                 javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                 javax.swing.border.TitledBorder.DEFAULT_POSITION, null, PortalLookAndFeel.getPanelLabelColor()));
 
         }
         return inputNamespaceScrollPane;
     }
 
 
     /**
      * This method initializes namespaceTypesJTree
      * 
      * @return javax.swing.JTree
      */
     private NamespacesJTree getInputNamespaceTypesJTree() {
         if (inputNamespaceTypesJTree == null) {
             inputNamespaceTypesJTree = new NamespacesJTree(info.getNamespaces(), true);
             inputNamespaceTypesJTree.addMouseListener(new MouseAdapter() {
                 public void mouseClicked(MouseEvent e) {
                     if (e.getClickCount() == 2) {
                         if (getInputNamespaceTypesJTree().getCurrentNode() instanceof SchemaElementTypeTreeNode) {
                             NamespaceType nt = ((NamespaceType) ((NamespaceTypeTreeNode) getInputNamespaceTypesJTree()
                                 .getCurrentNode().getParent()).getUserObject());
                             SchemaElementType st = ((SchemaElementType) ((SchemaElementTypeTreeNode) getInputNamespaceTypesJTree()
                                 .getCurrentNode()).getUserObject());
                             MethodTypeInputsInput input = new MethodTypeInputsInput();
                             input.setQName(new QName(nt.getNamespace(), st.getType()));
                             input.setIsArray(false);
                             input.setName(CommonTools.lowerCaseFirstCharacter(JavaUtils.xmlNameToJava(st.getType())));
                             getInputParamTable().addRow(input);
                         }
                     }
                 }
             });
         }
         return inputNamespaceTypesJTree;
     }
 
 
     /**
      * This method initializes methodPropertiesPanel
      * 
      * @return javax.swing.JPanel
      */
     private JPanel getMethodPropertiesPanel() {
         if (methodPropertiesPanel == null) {
             GridBagConstraints gridBagConstraints13 = new GridBagConstraints();
             gridBagConstraints13.fill = GridBagConstraints.BOTH;
             gridBagConstraints13.gridx = 0;
             gridBagConstraints13.gridy = 0;
             gridBagConstraints13.weightx = 1.0D;
             gridBagConstraints13.weighty = 0.0D;
             gridBagConstraints13.insets = new Insets(2, 2, 2, 2);
             methodPropertiesPanel = new JPanel();
             methodPropertiesPanel.setLayout(new GridBagLayout());
             methodPropertiesPanel.add(new IconFeedbackPanel(methodNameValidationModel, getNamePanel()),
                 gridBagConstraints13);
         }
         return methodPropertiesPanel;
     }
 
 
     /**
      * This method initializes jPanel
      * 
      * @return javax.swing.JPanel
      */
     private JPanel getOutputNamespacePanel() {
         if (outputNamespacePanel == null) {
             GridBagConstraints gridBagConstraints29 = new GridBagConstraints();
             gridBagConstraints29.fill = java.awt.GridBagConstraints.BOTH;
             gridBagConstraints29.weighty = 1.0;
             gridBagConstraints29.gridx = 0;
             gridBagConstraints29.gridy = 3;
             gridBagConstraints29.weightx = 1.0;
             GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
             gridBagConstraints6.fill = java.awt.GridBagConstraints.BOTH;
             gridBagConstraints6.gridy = 0;
             gridBagConstraints6.gridx = 0;
             gridBagConstraints6.gridheight = 3;
             gridBagConstraints6.weighty = 1.0;
             gridBagConstraints6.weightx = 1.0;
             outputNamespacePanel = new JPanel();
             outputNamespacePanel.setLayout(new GridBagLayout());
             outputNamespacePanel.add(getOutputNamespacesTypeScrollPane(), gridBagConstraints6);
             outputNamespacePanel.add(getServicesTypeScrollPane(), gridBagConstraints29);
         }
         return outputNamespacePanel;
     }
 
 
     /**
      * This method initializes jScrollPane
      * 
      * @return javax.swing.JScrollPane
      */
     private JScrollPane getOutputNamespacesTypeScrollPane() {
         if (outputNamespacesTypeScrollPane == null) {
             outputNamespacesTypeScrollPane = new JScrollPane();
             outputNamespacesTypeScrollPane.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Data Types",
                 javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                 javax.swing.border.TitledBorder.DEFAULT_POSITION, null, PortalLookAndFeel.getPanelLabelColor()));
             outputNamespacesTypeScrollPane.setViewportView(getOutputNamespacesJTree());
             outputNamespacesTypeScrollPane.setPreferredSize(new Dimension(200, 200));
         }
         return outputNamespacesTypeScrollPane;
     }
 
 
     /**
      * This method initializes outputNamespacesJTree
      * 
      * @return javax.swing.JTree
      */
     private NamespacesJTree getOutputNamespacesJTree() {
         if (outputNamespacesJTree == null) {
             outputNamespacesJTree = new NamespacesJTree(info.getNamespaces(), true);
             outputNamespacesJTree.addMouseListener(new MouseAdapter() {
                 public void mouseClicked(MouseEvent e) {
                     if (e.getClickCount() == 2) {
                         if (getOutputNamespacesJTree().getCurrentNode() instanceof SchemaElementTypeTreeNode) {
                             NamespaceType nt = ((NamespaceType) ((NamespaceTypeTreeNode) getOutputNamespacesJTree()
                                 .getCurrentNode().getParent()).getUserObject());
                             SchemaElementType st = ((SchemaElementType) ((SchemaElementTypeTreeNode) getOutputNamespacesJTree()
                                 .getCurrentNode()).getUserObject());
                             MethodTypeOutput output = new MethodTypeOutput();
                             output.setQName(new QName(nt.getNamespace(), st.getType()));
                             output.setIsArray(false);
                             try {
                                 getOutputTypeTable().modifyRow(0, output);
                             } catch (Exception ex) {
                                 logger.error("Error modifying output table", ex);
                             }
                         }
                     }
                 }
             });
         }
         return outputNamespacesJTree;
     }
 
 
     /**
      * This method initializes outputTypesTablePanel
      * 
      * @return javax.swing.JPanel
      */
     private JPanel getOutputTypesTablePanel() {
         if (outputTypesTablePanel == null) {
             GridBagConstraints gridBagConstraints26 = new GridBagConstraints();
             gridBagConstraints26.gridx = 0;
             gridBagConstraints26.insets = new java.awt.Insets(2, 2, 2, 2);
             gridBagConstraints26.gridy = 0;
             GridBagConstraints gridBagConstraints22 = new GridBagConstraints();
             gridBagConstraints22.fill = java.awt.GridBagConstraints.BOTH;
             gridBagConstraints22.gridx = 0;
             gridBagConstraints22.gridy = 1;
             gridBagConstraints22.weightx = 1.0;
             gridBagConstraints22.weighty = 1.0;
             gridBagConstraints22.gridwidth = 3;
             gridBagConstraints22.insets = new java.awt.Insets(2, 2, 2, 2);
             outputTypesTablePanel = new JPanel();
             outputTypesTablePanel.setLayout(new GridBagLayout());
             outputTypesTablePanel.add(getOutputTypejScrollPane(), gridBagConstraints22);
             outputTypesTablePanel.add(getClearOutputTypeButton(), gridBagConstraints26);
         }
         return outputTypesTablePanel;
     }
 
 
     /**
      * This method initializes inputTypesTablePanel
      * 
      * @return javax.swing.JPanel
      */
     private JPanel getInputTypesTablePanel() {
         if (inputTypesTablePanel == null) {
             GridBagConstraints gridBagConstraints18 = new GridBagConstraints();
             gridBagConstraints18.gridx = 1;
             gridBagConstraints18.fill = java.awt.GridBagConstraints.VERTICAL;
             gridBagConstraints18.gridy = 0;
             GridBagConstraints gridBagConstraints14 = new GridBagConstraints();
             gridBagConstraints14.fill = GridBagConstraints.HORIZONTAL;
             gridBagConstraints14.gridwidth = 2;
             gridBagConstraints14.gridx = 0;
             gridBagConstraints14.gridy = 1;
             gridBagConstraints14.weightx = 0.0D;
             gridBagConstraints14.weighty = 0.0D;
             gridBagConstraints14.insets = new Insets(2, 2, 2, 2);
             GridBagConstraints gridBagConstraints21 = new GridBagConstraints();
             gridBagConstraints21.fill = java.awt.GridBagConstraints.BOTH;
             gridBagConstraints21.gridx = 0;
             gridBagConstraints21.gridy = 0;
             gridBagConstraints21.weightx = 1.0;
             gridBagConstraints21.weighty = 1.0;
             gridBagConstraints21.insets = new java.awt.Insets(0, 0, 0, 0);
             inputTypesTablePanel = new JPanel();
             inputTypesTablePanel.setLayout(new GridBagLayout());
             inputTypesTablePanel.add(getInputParamScrollPanel(), gridBagConstraints21);
             inputTypesTablePanel.add(getInputButtonPanel(), gridBagConstraints14);
             inputTypesTablePanel.add(getInputTableControlsPanel(), gridBagConstraints18);
         }
         return inputTypesTablePanel;
     }
 
 
     /**
      * This method initializes inputTableControlsPanel1
      * 
      * @return javax.swing.JPanel
      */
     private JPanel getInputTableControlsPanel() {
         if (inputTableControlsPanel == null) {
             GridBagConstraints gridBagConstraints25 = new GridBagConstraints();
             gridBagConstraints25.gridx = 0;
             gridBagConstraints25.gridy = 0;
             GridBagConstraints gridBagConstraints24 = new GridBagConstraints();
             gridBagConstraints24.gridx = 0;
             gridBagConstraints24.gridy = 1;
             downLabel = new JLabel();
             downLabel.setText("");
             downLabel.setIcon(IntroduceLookAndFeel.getDownIcon());
             downLabel.addMouseListener(new MouseAdapter() {
                 public void mouseClicked(MouseEvent e) {
                     super.mouseClicked(e);
                     try {
                         getInputParamTable().moveSelectedRowDown();
                     } catch (Exception e1) {
                         logger.error("Error moving input param down", e1);
                     }
 
                 }
             });
             upLabel = new JLabel();
             upLabel.setText("");
             upLabel.setIcon(IntroduceLookAndFeel.getUpIcon());
             upLabel.addMouseListener(new MouseAdapter() {
                 public void mouseClicked(MouseEvent e) {
                     super.mouseClicked(e);
                     try {
                         getInputParamTable().moveSelectedRowUp();
                     } catch (Exception e1) {
                         logger.error("Error moving input param up", e1);
                     }
                 }
 
             });
             inputTableControlsPanel = new JPanel();
             inputTableControlsPanel.setLayout(new GridBagLayout());
             inputTableControlsPanel.add(upLabel, gridBagConstraints25);
             inputTableControlsPanel.add(downLabel, gridBagConstraints24);
         }
         return inputTableControlsPanel;
     }
 
 
     /**
      * This method initializes clearOutputTypeButton
      * 
      * @return javax.swing.JButton
      */
     private JButton getClearOutputTypeButton() {
         if (clearOutputTypeButton == null) {
             clearOutputTypeButton = new JButton();
             clearOutputTypeButton.setText("Clear Output Type");
             clearOutputTypeButton.addActionListener(new java.awt.event.ActionListener() {
                 public void actionPerformed(java.awt.event.ActionEvent e) {
                     MethodTypeOutput output = new MethodTypeOutput();
                     output.setQName(new QName("", "void"));
                     try {
                         getOutputTypeTable().modifyRow(0, output);
                     } catch (Exception e1) {
                         logger.error("Error modifying row on output table", e1);
                     }
                 }
             });
         }
         return clearOutputTypeButton;
     }
 
 
     /**
      * This method initializes exceptionsInputButtonPanel
      * 
      * @return javax.swing.JPanel
      */
     private JPanel getExceptionsInputButtonPanel() {
         if (exceptionsInputButtonPanel == null) {
             exceptionsInputButtonPanel = new JPanel();
             exceptionsInputButtonPanel.setLayout(new GridBagLayout());
         }
         return exceptionsInputButtonPanel;
     }
 
 
     private JPanel getImportInformationPanel() {
 
         if (importInformationPanel == null) {
             GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
             gridBagConstraints8.gridx = 0;
             gridBagConstraints8.weightx = 1.0D;
             gridBagConstraints8.weighty = 1.0D;
             gridBagConstraints8.fill = java.awt.GridBagConstraints.BOTH;
             gridBagConstraints8.gridy = 1;
             GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
             gridBagConstraints7.gridx = 0;
             gridBagConstraints7.fill = java.awt.GridBagConstraints.BOTH;
             gridBagConstraints7.weightx = 0.0D;
             gridBagConstraints7.weighty = 0.0D;
             gridBagConstraints7.gridy = 0;
             importInformationPanel = new JPanel();
             importInformationPanel.setLayout(new GridBagLayout());
             importInformationPanel.add(getBaseImportInfoPanel(), gridBagConstraints7);
             importInformationPanel.add(getImportTypeCardPanel(), gridBagConstraints8);
         }
         return importInformationPanel;
     }
 
 
     /**
      * This method initializes isImportedCheckBox
      * 
      * @return javax.swing.JCheckBox
      */
     private JCheckBox getIsImportedCheckBox() {
         if (isImportedCheckBox == null) {
             isImportedCheckBox = new JCheckBox();
             isImportedCheckBox
                 .setToolTipText("Check this if you want to import the the WSDL operation from another service");
             isImportedCheckBox.setText("Imported");
             isImportedCheckBox.setSelected(method.isIsImported());
             if (isImportedCheckBox.isSelected()) {
                isImportedCheckBox.setEnabled(false);
                 getTabbedPanel().setEnabledAt(0, false);
                 getTabbedPanel().setEnabledAt(3, false);
                getTabbedPanel().setSelectedIndex(1);
             }
 
             isImportedCheckBox.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent e) {
                     if (isImportedCheckBox.isSelected()) {
                         getTabbedPanel().setEnabledAt(3, true);
                         getTabbedPanel().setEnabledAt(0, false);
                         getTabbedPanel().setSelectedIndex(3);
                     } else {
                         getTabbedPanel().setEnabledAt(3, false);
                         getTabbedPanel().setEnabledAt(0, true);
                         if (getTabbedPanel().getSelectedIndex() == 3) {
                             getTabbedPanel().setSelectedIndex(0);
                         }
                     }
                     validateImportInput();
                 }
 
             });
 
         }
         return isImportedCheckBox;
     }
 
 
     /**
      * This method initializes isProvidedCheckBox
      * 
      * @return javax.swing.JCheckBox
      */
     private JCheckBox getIsProvidedCheckBox() {
         if (isProvidedCheckBox == null) {
             isProvidedCheckBox = new JCheckBox();
             isProvidedCheckBox
                 .setToolTipText("Check this if you want to have another class/library implement this method");
             isProvidedCheckBox.setText("Provided");
             isProvidedCheckBox.setSelected(method.isIsProvided());
             if (isProvidedCheckBox.isSelected()) {
                 getTabbedPanel().setEnabledAt(2, true);
             } else {
                 getTabbedPanel().setEnabledAt(2, false);
                 if (!getIsImportedCheckBox().isSelected()) {
                     getTabbedPanel().setSelectedIndex(0);
                 } else {
                     getTabbedPanel().setSelectedIndex(1);
                 }
             }
             isProvidedCheckBox.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent e) {
                     if (isProvidedCheckBox.isSelected()) {
                         getTabbedPanel().setEnabledAt(2, true);
                         getTabbedPanel().setSelectedIndex(2);
                     } else {
                         getTabbedPanel().setEnabledAt(2, false);
                         getTabbedPanel().setSelectedIndex(1);
 
                     }
                     validateProviderInput();
                 }
 
             });
         }
         return isProvidedCheckBox;
     }
 
 
     /**
      * This method initializes servicesTypeScrollPane
      * 
      * @return javax.swing.JScrollPane
      */
     private JScrollPane getServicesTypeScrollPane() {
         if (servicesTypeScrollPane == null) {
             servicesTypeScrollPane = new JScrollPane();
             servicesTypeScrollPane.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Client Handle Types",
                 javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                 javax.swing.border.TitledBorder.DEFAULT_POSITION, null, PortalLookAndFeel.getPanelLabelColor()));
             servicesTypeScrollPane.setViewportView(getServicesTypeTable());
         }
         return servicesTypeScrollPane;
     }
 
 
     /**
      * This method initializes servicesTypeTable
      * 
      * @return javax.swing.JTable
      */
     private ServiceReferencesTable getServicesTypeTable() {
         if (servicesTypeTable == null) {
             servicesTypeTable = new ServiceReferencesTable(info.getServices());
             servicesTypeTable.addMouseListener(new MouseAdapter() {
                 public void mouseClicked(MouseEvent e) {
                     super.mouseClicked(e);
                     if (e.getClickCount() == 2) {
 
                         // set the epr type as this outputType
                         MethodTypeOutput output = new MethodTypeOutput();
                         try {
                             output.setQName(new QName(getServicesTypeTable().getSelectedRowData().getNamespace()
                                 + "/types", getServicesTypeTable().getSelectedRowData().getName() + "Reference"));
                             output.setIsArray(false);
                             output.setIsClientHandle(new Boolean(true));
                             output.setResourceClientIntroduceServiceName(getServicesTypeTable().getSelectedRowData()
                                 .getName());
                             output.setIsCreatingResourceForClientHandle(new Boolean(true));
                             output.setClientHandleClass(getServicesTypeTable().getSelectedRowData().getPackageName()
                                 + "." + "client" + "." + getServicesTypeTable().getSelectedRowData().getName()
                                 + "Client");
                         } catch (Exception e1) {
                             logger.error("Error setting client handle class", e1);
                         }
                         try {
                             getOutputTypeTable().modifyRow(0, output);
                         } catch (Exception ex) {
                             logger.error("Error modifying row on output types table", ex);
                         }
                     }
                 }
 
             });
         }
         return servicesTypeTable;
     }
 
 
     /**
      * This method initializes providerClassnameTextField
      * 
      * @return javax.swing.JTextField
      */
     private JTextField getProviderClassnameTextField() {
         if (providerClassnameTextField == null) {
             providerClassnameTextField = new JTextField();
             if ((method.getProviderInformation() != null)
                 && (method.getProviderInformation().getProviderClass() != null)) {
                 providerClassnameTextField.setText(method.getProviderInformation().getProviderClass());
             }
             providerClassnameTextField.getDocument().addDocumentListener(new DocumentListener() {
 
                 public void removeUpdate(DocumentEvent e) {
                     validateProviderInput();
                 }
 
 
                 public void insertUpdate(DocumentEvent e) {
                     validateProviderInput();
                 }
 
 
                 public void changedUpdate(DocumentEvent e) {
                     validateProviderInput();
                 }
 
             });
         }
         return providerClassnameTextField;
     }
 
 
     /**
      * This method initializes providerInformationPanel
      * 
      * @return javax.swing.JPanel
      */
     private JPanel getProviderInformationPanel() {
         if (providerInformationPanel == null) {
             GridBagConstraints gridBagConstraints34 = new GridBagConstraints();
             gridBagConstraints34.gridx = 0;
             gridBagConstraints34.insets = new java.awt.Insets(2, 2, 2, 2);
             gridBagConstraints34.fill = java.awt.GridBagConstraints.HORIZONTAL;
             gridBagConstraints34.anchor = java.awt.GridBagConstraints.WEST;
             gridBagConstraints34.gridy = 1;
             GridBagConstraints gridBagConstraints37 = new GridBagConstraints();
             gridBagConstraints37.gridx = 1;
             gridBagConstraints37.insets = new java.awt.Insets(2, 2, 2, 2);
             gridBagConstraints37.fill = java.awt.GridBagConstraints.HORIZONTAL;
             gridBagConstraints37.weightx = 1.0D;
             gridBagConstraints37.gridy = 1;
             providerClassnameLabel = new JLabel();
             providerClassnameLabel.setText("Provider Classname");
             providerInformationPanel = new JPanel();
             providerInformationPanel.setLayout(new GridBagLayout());
             providerInformationPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null,
                 "Provider Information", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                 javax.swing.border.TitledBorder.DEFAULT_POSITION, null, PortalLookAndFeel.getPanelLabelColor()));
             providerInformationPanel.add(providerClassnameLabel, gridBagConstraints34);
             providerInformationPanel.add(getProviderClassnameTextField(), gridBagConstraints37);
         }
         return providerInformationPanel;
     }
 
 
     /**
      * This method initializes jSplitPane
      * 
      * @return javax.swing.JSplitPane
      */
     private JSplitPane getInputParamsSplitPane() {
         if (inputParamsSplitPane == null) {
             inputParamsSplitPane = new JSplitPane();
             inputParamsSplitPane.setOneTouchExpandable(true);
             inputParamsSplitPane.setLeftComponent(getInputNamespacesPanel());
             inputParamsSplitPane.setRightComponent(getInputTypesTablePanel());
             inputParamsSplitPane.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Input Parameters",
                 javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                 javax.swing.border.TitledBorder.DEFAULT_POSITION, null, PortalLookAndFeel.getPanelLabelColor()));
         }
         return inputParamsSplitPane;
     }
 
 
     /**
      * This method initializes jSplitPane
      * 
      * @return javax.swing.JSplitPane
      */
     private JSplitPane getOutputTypeSplitPane() {
         if (outputTypeSplitPane == null) {
             outputTypeSplitPane = new JSplitPane();
             outputTypeSplitPane.setOneTouchExpandable(true);
             outputTypeSplitPane.setLeftComponent(getOutputNamespacePanel());
             outputTypeSplitPane.setRightComponent(getOutputTypesTablePanel());
             outputTypeSplitPane.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Output Type",
                 javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                 javax.swing.border.TitledBorder.DEFAULT_POSITION, null, PortalLookAndFeel.getPanelLabelColor()));
         }
         return outputTypeSplitPane;
     }
 
 
     /**
      * This method initializes createFaultPanel
      * 
      * @return javax.swing.JPanel
      */
     private JPanel getCreateFaultPanel() {
         if (createFaultPanel == null) {
             GridBagConstraints gridBagConstraints48 = new GridBagConstraints();
             gridBagConstraints48.gridx = 1;
             gridBagConstraints48.gridy = 0;
             faultTypeNameLabel = new JLabel();
             faultTypeNameLabel.setText("Fault Type Name:");
             GridBagConstraints gridBagConstraints47 = new GridBagConstraints();
             gridBagConstraints47.gridx = 3;
             gridBagConstraints47.gridy = 0;
             GridBagConstraints gridBagConstraints45 = new GridBagConstraints();
             gridBagConstraints45.fill = java.awt.GridBagConstraints.HORIZONTAL;
             gridBagConstraints45.gridx = 2;
             gridBagConstraints45.gridy = 0;
             gridBagConstraints45.weightx = 1.0;
             gridBagConstraints45.insets = new java.awt.Insets(5, 5, 5, 5);
             createFaultPanel = new JPanel();
             createFaultPanel.setLayout(new GridBagLayout());
             createFaultPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Create New Service Faults",
                 javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                 javax.swing.border.TitledBorder.DEFAULT_POSITION, null, PortalLookAndFeel.getPanelLabelColor()));
             createFaultPanel.add(getNewFaultNameTextField(), gridBagConstraints45);
             createFaultPanel.add(getCreateFaultButton(), gridBagConstraints47);
             createFaultPanel.add(faultTypeNameLabel, gridBagConstraints48);
         }
         return createFaultPanel;
     }
 
 
     /**
      * This method initializes newFaultNameTextField
      * 
      * @return javax.swing.JTextField
      */
     private JTextField getNewFaultNameTextField() {
         if (newFaultNameTextField == null) {
             newFaultNameTextField = new JTextField();
             newFaultNameTextField.getDocument().addDocumentListener(new DocumentListener() {
 
                 public void removeUpdate(DocumentEvent e) {
                     validateNewFaultInput();
                 }
 
 
                 public void insertUpdate(DocumentEvent e) {
                     validateNewFaultInput();
                 }
 
 
                 public void changedUpdate(DocumentEvent e) {
                     validateNewFaultInput();
                 }
 
             });
         }
         return newFaultNameTextField;
     }
 
 
     /**
      * This method initializes createFaultButton
      * 
      * @return javax.swing.JButton
      */
     private JButton getCreateFaultButton() {
         if (createFaultButton == null) {
             createFaultButton = new JButton(PortalLookAndFeel.getAddIcon());
             createFaultButton.setText("Add New Fault");
             createFaultButton
                 .setToolTipText("Creates a new fault under this services types namespace and adds it to the list of available faults.");
 
             createFaultButton.addActionListener(new java.awt.event.ActionListener() {
                 public void actionPerformed(java.awt.event.ActionEvent e) {
                     QName exceptionQName = null;
 
                     exceptionQName = new QName(info.getService().getNamespace() + "/types", getNewFaultNameTextField()
                         .getText());
 
                     ExceptionHolder holder = new ExceptionHolder(exceptionQName, false);
                     getExceptionJComboBox().addItem(holder);
                     getExceptionsTable().addRow(holder.qname, holder.isCreated, "");
                     getNewFaultNameTextField().setText("");
 
                 }
             });
         }
         return createFaultButton;
     }
 
 
     /**
      * This method initializes exceptionsPanelSplitPane
      * 
      * @return javax.swing.JSplitPane
      */
     private JSplitPane getExceptionsPanelSplitPane() {
         if (exceptionsPanelSplitPane == null) {
             exceptionsPanelSplitPane = new JSplitPane();
             exceptionsPanelSplitPane.setRightComponent(getExceptionScrollPane());
             exceptionsPanelSplitPane.setLeftComponent(getExceptionNamespacesScrollPane());
         }
         return exceptionsPanelSplitPane;
     }
 
 
     /**
      * This method initializes exceptionNamespacesScrollPane
      * 
      * @return javax.swing.JScrollPane
      */
     private JScrollPane getExceptionNamespacesScrollPane() {
         if (exceptionNamespacesScrollPane == null) {
             exceptionNamespacesScrollPane = new JScrollPane();
             exceptionNamespacesScrollPane.setViewportView(getNamespacesJTree());
             exceptionNamespacesScrollPane.setPreferredSize(new Dimension(200, 200));
         }
         return exceptionNamespacesScrollPane;
     }
 
 
     /**
      * This method initializes namespacesJTree
      * 
      * @return javax.swing.JTree
      */
     private NamespacesJTree getNamespacesJTree() {
         if (namespacesJTree == null) {
             namespacesJTree = new NamespacesJTree(info.getNamespaces(), false);
         }
         return namespacesJTree;
     }
 
 
     /**
      * This method initializes faultsFromTypesPanel
      * 
      * @return javax.swing.JPanel
      */
     private JPanel getFaultsFromTypesPanel() {
         if (faultsFromTypesPanel == null) {
             faultsFromTypesPanel = new JPanel();
             faultsFromTypesPanel.setLayout(new GridBagLayout());
             faultsFromTypesPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Add Fault From Types",
                 javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                 javax.swing.border.TitledBorder.DEFAULT_POSITION, null, PortalLookAndFeel.getPanelLabelColor()));
             faultsFromTypesPanel.add(getAddFaultFromTypeButton(), new GridBagConstraints());
         }
         return faultsFromTypesPanel;
     }
 
 
     /**
      * This method initializes addFaultFromTypeButton
      * 
      * @return javax.swing.JButton
      */
     private JButton getAddFaultFromTypeButton() {
         if (addFaultFromTypeButton == null) {
             addFaultFromTypeButton = new JButton(PortalLookAndFeel.getAddIcon());
             addFaultFromTypeButton.setText("Add From Type");
             addFaultFromTypeButton.addActionListener(new java.awt.event.ActionListener() {
                 public void actionPerformed(java.awt.event.ActionEvent e) {
                     if (getNamespacesJTree().getCurrentNode() instanceof SchemaElementTypeTreeNode) {
                         NamespaceType nt = ((NamespaceType) ((NamespaceTypeTreeNode) getNamespacesJTree()
                             .getCurrentNode().getParent()).getUserObject());
                         SchemaElementType st = ((SchemaElementType) ((SchemaElementTypeTreeNode) getNamespacesJTree()
                             .getCurrentNode()).getUserObject());
                         try {
                             if (MethodViewer.validateIsFaultType(nt, st, new File(info.getBaseDirectory()
                                 .getAbsolutePath()
                                 + File.separator
                                 + "schema"
                                 + File.separator
                                 + info.getServices().getService(0).getName()))) {
                                 QName qname = new QName(nt.getNamespace(), st.getType());
                                 ExceptionHolder holder = new ExceptionHolder(qname, true);
                                 getExceptionJComboBox().addItem(holder);
                                 getExceptionsTable().addRow(holder.qname, holder.isCreated, "");
                             } else {
                                 JOptionPane.showMessageDialog(MethodViewer.this,
                                     "Type does not appear to extend from {" + IntroduceConstants.BASEFAULTS_NAMESPACE
                                         + "}BaseFaultType");
                             }
                         } catch (Exception ex) {
                             logger.error("Unable to validate type extends from {"
                                 + IntroduceConstants.BASEFAULTS_NAMESPACE + "}BaseFaultType : " + ex.getMessage(), ex);
                             JOptionPane.showMessageDialog(MethodViewer.this, "Unable to validate type extends from {"
                                 + IntroduceConstants.BASEFAULTS_NAMESPACE + "}BaseFaultType : " + ex.getMessage());
                         }
                     } else {
                         JOptionPane.showMessageDialog(MethodViewer.this, "Please select a type to add");
                     }
                 }
             });
         }
         return addFaultFromTypeButton;
     }
 
 
     /**
      * This method initializes removeFaultPanel
      * 
      * @return javax.swing.JPanel
      */
     private JPanel getRemoveFaultPanel() {
         if (removeFaultPanel == null) {
             GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
             gridBagConstraints5.insets = new Insets(2, 2, 2, 2);
             gridBagConstraints5.gridx = -1;
             gridBagConstraints5.gridy = -1;
             gridBagConstraints5.gridheight = 2;
             removeFaultPanel = new JPanel();
             removeFaultPanel.setLayout(new GridBagLayout());
             removeFaultPanel.add(getRemoveExceptionButton(), gridBagConstraints5);
         }
         return removeFaultPanel;
     }
 
 
     /**
      * This method initializes providerInfoPanel
      * 
      * @return javax.swing.JPanel
      */
     private JPanel getProviderInfoPanel() {
         if (providerInfoPanel == null) {
             GridBagConstraints gridBagConstraints36 = new GridBagConstraints();
             gridBagConstraints36.fill = GridBagConstraints.BOTH;
             gridBagConstraints36.gridx = 0;
             gridBagConstraints36.gridy = 0;
             gridBagConstraints36.weighty = 1.0D;
             gridBagConstraints36.weightx = 1.0D;
             gridBagConstraints36.gridwidth = 2;
             providerInfoPanel = new JPanel();
             providerInfoPanel.setLayout(new GridBagLayout());
             providerInfoPanel.add(new IconFeedbackPanel(methodProviderValidationModel, getProviderInformationPanel()),
                 gridBagConstraints36);
         }
         return providerInfoPanel;
     }
 
 
     public static boolean validateIsFaultType(NamespaceType namespace, SchemaElementType type, File baseSchemaDir)
         throws Exception {
         XSOMParser parser = new XSOMParser();
         parser.setErrorHandler(new DefaultErrorHandler());
 
         parser.parse(new File(baseSchemaDir.getAbsolutePath() + File.separator + namespace.getLocation()));
 
         XSSchemaSet sset = parser.getResult();
         XSComplexType bfct = sset.getComplexType(IntroduceConstants.BASEFAULTS_NAMESPACE, "BaseFaultType");
         XSElementDecl ct = sset.getElementDecl(namespace.getNamespace(), type.getType());
         if (ct.getType().isDerivedFrom(bfct)) {
             return true;
         } else {
             return false;
         }
     }
 
 
     /**
      * This method initializes baseImportInfoPanel
      * 
      * @return javax.swing.JPanel
      */
     private JPanel getBaseImportInfoPanel() {
         if (baseImportInfoPanel == null) {
             GridBagConstraints gridBagConstraints16 = new GridBagConstraints();
             gridBagConstraints16.insets = new java.awt.Insets(2, 2, 2, 2);
             gridBagConstraints16.gridy = 0;
             gridBagConstraints16.gridx = 0;
             baseImportInfoPanel = new JPanel();
             baseImportInfoPanel.setLayout(new GridBagLayout());
             baseImportInfoPanel.add(getIsFromIntroduceCheckBox(), gridBagConstraints16);
         }
         return baseImportInfoPanel;
     }
 
 
     /**
      * This method initializes importTypeCardPanel
      * 
      * @return javax.swing.JPanel
      */
     private JPanel getImportTypeCardPanel() {
         if (importTypeCardPanel == null) {
             importTypeCardPanel = new JPanel();
             importTypeCardPanel.setLayout(new CardLayout());
             importTypeCardPanel.add(getFromIntroducePanel(), "from-introduce");
             importTypeCardPanel.add(new IconFeedbackPanel(methodImportValidationModel, getNotFromIntroducePanel()),
                 "not-from-introduce");
             if (getIsFromIntroduceCheckBox().isSelected()) {
                 ((CardLayout) importTypeCardPanel.getLayout()).show(importTypeCardPanel, "from-introduce");
             } else {
                 ((CardLayout) importTypeCardPanel.getLayout()).show(importTypeCardPanel, "not-from-introduce");
             }
         }
         return importTypeCardPanel;
     }
 
 
     /**
      * This method initializes isFromIntroduceCheckBox
      * 
      * @return javax.swing.JCheckBox
      */
     private JCheckBox getIsFromIntroduceCheckBox() {
         if (isFromIntroduceCheckBox == null) {
             isFromIntroduceCheckBox = new JCheckBox();
             isFromIntroduceCheckBox.setText("Method is from an introduce generated service.");
             isFromIntroduceCheckBox.addActionListener(new java.awt.event.ActionListener() {
                 public void actionPerformed(java.awt.event.ActionEvent e) {
                     if (getIsFromIntroduceCheckBox().isSelected()) {
                         ((CardLayout) importTypeCardPanel.getLayout()).show(importTypeCardPanel, "from-introduce");
                     } else {
                         ((CardLayout) importTypeCardPanel.getLayout()).show(importTypeCardPanel, "not-from-introduce");
                     }
                     validateImportInput();
                 }
             });
             isFromIntroduceCheckBox.setSelected(method.isIsImported() && method.getImportInformation() != null
                 && method.getImportInformation().getFromIntroduce() != null
                 && method.getImportInformation().getFromIntroduce().booleanValue());
         }
         return isFromIntroduceCheckBox;
     }
 
 
     /**
      * This method initializes fromIntroducePanel
      * 
      * @return javax.swing.JPanel
      */
     private JPanel getFromIntroducePanel() {
         if (fromIntroducePanel == null) {
             GridBagConstraints gridBagConstraints41 = new GridBagConstraints();
             gridBagConstraints41.gridx = 0;
             gridBagConstraints41.insets = new java.awt.Insets(2, 2, 2, 2);
             gridBagConstraints41.fill = java.awt.GridBagConstraints.BOTH;
             gridBagConstraints41.gridy = 2;
             introduceServiceOperationLabel = new JLabel();
             introduceServiceOperationLabel.setText("");
             introduceServiceOperationLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
             GridBagConstraints gridBagConstraints40 = new GridBagConstraints();
             gridBagConstraints40.fill = java.awt.GridBagConstraints.HORIZONTAL;
             gridBagConstraints40.gridy = 1;
             gridBagConstraints40.weightx = 1.0;
             gridBagConstraints40.insets = new java.awt.Insets(2, 2, 2, 2);
             gridBagConstraints40.gridx = 1;
             GridBagConstraints gridBagConstraints39 = new GridBagConstraints();
             gridBagConstraints39.gridx = 0;
             gridBagConstraints39.insets = new java.awt.Insets(2, 2, 2, 2);
             gridBagConstraints39.fill = java.awt.GridBagConstraints.BOTH;
             gridBagConstraints39.anchor = java.awt.GridBagConstraints.CENTER;
             gridBagConstraints39.gridy = 1;
             introduceServiceServicesLabel = new JLabel();
             introduceServiceServicesLabel.setText("Service");
             introduceServiceServicesLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
             GridBagConstraints gridBagConstraints38 = new GridBagConstraints();
             gridBagConstraints38.gridx = 0;
             gridBagConstraints38.insets = new java.awt.Insets(2, 2, 2, 2);
             gridBagConstraints38.fill = java.awt.GridBagConstraints.BOTH;
             gridBagConstraints38.gridy = 0;
             introduceServiceLocationLabel = new JLabel();
             introduceServiceLocationLabel.setText("Introduce Service Location");
             introduceServiceLocationLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
             GridBagConstraints gridBagConstraints35 = new GridBagConstraints();
             gridBagConstraints35.gridx = 2;
             gridBagConstraints35.insets = new java.awt.Insets(2, 2, 2, 2);
             gridBagConstraints35.fill = java.awt.GridBagConstraints.BOTH;
             gridBagConstraints35.gridy = 0;
             GridBagConstraints gridBagConstraints33 = new GridBagConstraints();
             gridBagConstraints33.fill = java.awt.GridBagConstraints.HORIZONTAL;
             gridBagConstraints33.gridy = 0;
             gridBagConstraints33.weightx = 1.0;
             gridBagConstraints33.insets = new java.awt.Insets(2, 2, 2, 2);
             gridBagConstraints33.gridx = 1;
             fromIntroducePanel = new JPanel();
             fromIntroducePanel.setLayout(new GridBagLayout());
             fromIntroducePanel.setName("fromIntroducePanel");
             fromIntroducePanel.add(getIntroduceServiceLocationTextField(), gridBagConstraints33);
             fromIntroducePanel.add(getIntroduceServiceLocationBrowseButton(), gridBagConstraints35);
             fromIntroducePanel.add(introduceServiceServicesLabel, gridBagConstraints39);
             fromIntroducePanel.add(getIntroduceServiceServicesComboBox(), gridBagConstraints40);
             fromIntroducePanel.add(introduceServiceOperationLabel, gridBagConstraints41);
             fromIntroducePanel.add(introduceServiceLocationLabel, gridBagConstraints38);
         }
         return fromIntroducePanel;
     }
 
 
     /**
      * This method initializes notFromIntroducePanel
      * 
      * @return javax.swing.JPanel
      */
     private JPanel getNotFromIntroducePanel() {
         if (notFromIntroducePanel == null) {
             GridBagConstraints gridBagConstraints42 = new GridBagConstraints();
             gridBagConstraints42.gridx = 0;
             gridBagConstraints42.fill = java.awt.GridBagConstraints.BOTH;
             gridBagConstraints42.insets = new java.awt.Insets(2, 2, 2, 2);
             gridBagConstraints42.gridy = 2;
             wsdlImportPackageNameLabel = new JLabel();
             wsdlImportPackageNameLabel.setText("Package Name");
             wsdlImportPackageNameLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
             GridBagConstraints gridBagConstraints32 = new GridBagConstraints();
             gridBagConstraints32.fill = java.awt.GridBagConstraints.HORIZONTAL;
             gridBagConstraints32.gridy = 2;
             gridBagConstraints32.weightx = 1.0;
             gridBagConstraints32.insets = new java.awt.Insets(2, 2, 2, 2);
             gridBagConstraints32.gridx = 1;
             GridBagConstraints gridBagConstraints31 = new GridBagConstraints();
             gridBagConstraints31.gridx = 0;
             gridBagConstraints31.fill = java.awt.GridBagConstraints.BOTH;
             gridBagConstraints31.insets = new java.awt.Insets(2, 2, 2, 2);
             gridBagConstraints31.gridy = 1;
             portTypeLabel = new JLabel();
             portTypeLabel.setText("Port Type");
             portTypeLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
             GridBagConstraints gridBagConstraints30 = new GridBagConstraints();
             gridBagConstraints30.fill = java.awt.GridBagConstraints.HORIZONTAL;
             gridBagConstraints30.gridy = 1;
             gridBagConstraints30.weightx = 1.0;
             gridBagConstraints30.insets = new java.awt.Insets(2, 2, 2, 2);
             gridBagConstraints30.gridx = 1;
             GridBagConstraints gridBagConstraints28 = new GridBagConstraints();
             gridBagConstraints28.gridx = 2;
             gridBagConstraints28.insets = new java.awt.Insets(2, 2, 2, 2);
             gridBagConstraints28.fill = java.awt.GridBagConstraints.BOTH;
             gridBagConstraints28.gridy = 0;
             GridBagConstraints gridBagConstraints23 = new GridBagConstraints();
             gridBagConstraints23.fill = java.awt.GridBagConstraints.HORIZONTAL;
             gridBagConstraints23.gridy = 0;
             gridBagConstraints23.weightx = 1.0;
             gridBagConstraints23.insets = new java.awt.Insets(2, 2, 2, 2);
             gridBagConstraints23.gridx = 1;
             GridBagConstraints gridBagConstraints17 = new GridBagConstraints();
             gridBagConstraints17.gridx = 0;
             gridBagConstraints17.insets = new java.awt.Insets(2, 2, 2, 2);
             gridBagConstraints17.fill = java.awt.GridBagConstraints.BOTH;
             gridBagConstraints17.gridy = 0;
             wsdlFileLabel = new JLabel();
             wsdlFileLabel.setText("WSDL File");
             wsdlFileLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
             notFromIntroducePanel = new JPanel();
             notFromIntroducePanel.setLayout(new GridBagLayout());
             notFromIntroducePanel.setName("notFromIntroducePanel");
             notFromIntroducePanel.add(wsdlFileLabel, gridBagConstraints17);
             notFromIntroducePanel.add(getWsdlFileNameTextField(), gridBagConstraints23);
             notFromIntroducePanel.add(getWsdlFileBrowseButton(), gridBagConstraints28);
             notFromIntroducePanel.add(getWsdlServiceServicesComboBox(), gridBagConstraints30);
             notFromIntroducePanel.add(portTypeLabel, gridBagConstraints31);
             notFromIntroducePanel.add(getWsdlImportPackageNameTextField(), gridBagConstraints32);
             notFromIntroducePanel.add(wsdlImportPackageNameLabel, gridBagConstraints42);
         }
         return notFromIntroducePanel;
     }
 
 
     /**
      * This method initializes introduceServiceLocationTextField
      * 
      * @return javax.swing.JTextField
      */
     private JTextField getIntroduceServiceLocationTextField() {
         if (introduceServiceLocationTextField == null) {
             introduceServiceLocationTextField = new JTextField();
             introduceServiceLocationTextField.setEditable(false);
         }
         return introduceServiceLocationTextField;
     }
 
 
     /**
      * This method initializes introduceLocationBrowseButton
      * 
      * @return javax.swing.JButton
      */
     private JButton getIntroduceServiceLocationBrowseButton() {
         if (introduceServiceLocationBrowseButton == null) {
             introduceServiceLocationBrowseButton = new JButton();
             introduceServiceLocationBrowseButton.setText("Browse");
             introduceServiceLocationBrowseButton.setIcon(IntroduceLookAndFeel.getBrowseIcon());
             introduceServiceLocationBrowseButton.addActionListener(new java.awt.event.ActionListener() {
                 public void actionPerformed(java.awt.event.ActionEvent e) {
                     // chose the introduce directory
                     JFileChooser chooser = new JFileChooser();
                     chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                     GridApplication.getContext().centerComponent(chooser);
                     int returnVal = chooser.showOpenDialog(MethodViewer.this);
                     if (returnVal != JFileChooser.APPROVE_OPTION) {
                         return;
                     }
                     boolean ok = false;
                     if (chooser.getSelectedFile().isDirectory()) {
                         File[] files = chooser.getSelectedFile().listFiles();
                         for (int i = 0; i < files.length; i++) {
                             if (files[i].getName().equals(IntroduceConstants.INTRODUCE_XML_FILE)) {
                                 ok = true;
                                 break;
                             }
                         }
                     }
                     if (!ok) {
                         JOptionPane.showMessageDialog(MethodViewer.this,
                             "Directory does not appear to contain an introduce generated service.");
                     }
 
                     introduceServiceLocationTextField.setText(chooser.getSelectedFile().getAbsolutePath());
 
                     // get the filename and deserialize the
                     // introduce.xml
                     // document.
                     File introduceFile = new File(chooser.getSelectedFile().getAbsolutePath() + File.separator
                         + IntroduceConstants.INTRODUCE_XML_FILE);
                     ServiceDescription desc = null;
                     try {
                         desc = (ServiceDescription) Utils.deserializeDocument(introduceFile.getAbsolutePath(),
                             ServiceDescription.class);
                     } catch (Exception e1) {
                         logger.error("Unable to deserialize introduce.xml file", e1);
                         return;
                     }
 
                     currentImportServiceSescription = desc;
 
                     getIntroduceServiceServicesComboBox().removeAllItems();
                     // populate the combo boxes
                     for (int serviceI = 0; serviceI < desc.getServices().getService().length; serviceI++) {
                         ServiceType service = desc.getServices().getService(serviceI);
                         if ((service.getMethods() != null) && (service.getMethods().getMethod() != null)) {
                             boolean found = false;
                             for (int methodI = 0; methodI < service.getMethods().getMethod().length; methodI++) {
                                 if (getNameField().getText().equals(service.getMethods().getMethod(methodI).getName())) {
                                     found = true;
                                 }
                             }
                             if (found) {
                                 getIntroduceServiceServicesComboBox().addItem(new ServiceHolder(service));
                             }
                         }
 
                     }
                     if (getIntroduceServiceServicesComboBox().getItemCount() <= 0) {
                         JOptionPane.showMessageDialog(MethodViewer.this,
                             "The selected Introduce generated service does not appear to have any services with the method name: "
                                 + getNameField().getText());
                     }
                 }
             });
         }
         return introduceServiceLocationBrowseButton;
     }
 
 
     /**
      * This method initializes introduceServiceServicesComboBox
      * 
      * @return javax.swing.JComboBox
      */
     private JComboBox getIntroduceServiceServicesComboBox() {
         if (introduceServiceServicesComboBox == null) {
             introduceServiceServicesComboBox = new JComboBox();
             introduceServiceServicesComboBox
                 .setToolTipText("This will only show services which have methods that are the same name as the method which is currently being added");
         }
         return introduceServiceServicesComboBox;
     }
 
 
     /**
      * This method initializes wsdlFileNameTextField
      * 
      * @return javax.swing.JTextField
      */
     private JTextField getWsdlFileNameTextField() {
         if (wsdlFileNameTextField == null) {
             wsdlFileNameTextField = new JTextField();
             wsdlFileNameTextField.setEditable(false);
             if (method.isIsImported()) {
                 wsdlFileNameTextField.setText(method.getImportInformation().getWsdlFile());
             }
             wsdlFileNameTextField.getDocument().addDocumentListener(new DocumentListener() {
 
                 public void removeUpdate(DocumentEvent e) {
                     validateImportInput();
                 }
 
 
                 public void insertUpdate(DocumentEvent e) {
                     validateImportInput();
                 }
 
 
                 public void changedUpdate(DocumentEvent e) {
                     validateImportInput();
                 }
 
             });
         }
         return wsdlFileNameTextField;
     }
 
 
     /**
      * This method initializes wsdlFileBrowseButton
      * 
      * @return javax.swing.JButton
      */
     private JButton getWsdlFileBrowseButton() {
         if (wsdlFileBrowseButton == null) {
             wsdlFileBrowseButton = new JButton();
             wsdlFileBrowseButton.setText("Browse");
             wsdlFileBrowseButton.setIcon(IntroduceLookAndFeel.getBrowseIcon());
             wsdlFileBrowseButton.addActionListener(new java.awt.event.ActionListener() {
                 public void actionPerformed(java.awt.event.ActionEvent e) {
                     JFileChooser chooser = new JFileChooser(info.getBaseDirectory().getAbsolutePath() + File.separator
                         + "schema" + File.separator + info.getServices().getService(0).getName());
                     chooser.setFileFilter(new FileFilter() {
 
                         public String getDescription() {
                             return "Local WSDL Files";
                         }
 
 
                         public boolean accept(File f) {
                             if (f.getName().endsWith(".wsdl")) {
                                 return true;
                             }
                             return false;
                         }
 
                     });
                     chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                     GridApplication.getContext().centerComponent(chooser);
                     int returnVal = chooser.showOpenDialog(MethodViewer.this);
                     if (returnVal != JFileChooser.APPROVE_OPTION) {
                         return;
                     }
 
                     Document wsdlDoc = null;
                     try {
                         wsdlDoc = XMLUtilities.fileNameToDocument(chooser.getSelectedFile().getAbsolutePath());
                         currentImporWSDL = wsdlDoc;
                     } catch (Exception e1) {
                         logger.error("ERROR", e1);
                         return;
                     }
 
                     getWsdlServiceServicesComboBox().removeAllItems();
 
                     List portTypes = wsdlDoc.getRootElement().getChildren("portType",
                         Namespace.getNamespace(IntroduceConstants.WSDLAMESPACE));
                     for (int portTypeI = 0; portTypeI < portTypes.size(); portTypeI++) {
                         Element portTypeEl = (Element) portTypes.get(portTypeI);
                         List operationEls = portTypeEl.getChildren("operation", Namespace
                             .getNamespace(IntroduceConstants.WSDLAMESPACE));
                         boolean found = false;
                         Element methodEl = null;
                         for (int opI = 0; opI < operationEls.size(); opI++) {
                             Element opEl = (Element) operationEls.get(opI);
                             if (opEl.getAttributeValue("name").equals(getNameField().getText())) {
                                 found = true;
                                 methodEl = opEl;
                                 break;
                             }
                         }
                         if (found) {
                             // need to add this service to the comboBox
                             getWsdlServiceServicesComboBox().addItem(new ElementHolder(portTypeEl, methodEl));
                         }
                     }
                     if (getWsdlServiceServicesComboBox().getItemCount() <= 0) {
                         JOptionPane.showMessageDialog(MethodViewer.this,
                             "The WSDL file does not contain a port type with an operation named: "
                                + getNameField().getText());
                     }
                     String schemaDir = info.getBaseDirectory().getAbsolutePath() + File.separator + "schema"
                         + File.separator + info.getServices().getService(0).getName();
                    String relativeFile = chooser.getSelectedFile().getAbsolutePath().substring(
                         chooser.getSelectedFile().getAbsolutePath().indexOf(schemaDir) + schemaDir.length() + 1);
                     getWsdlFileNameTextField().setText(relativeFile);
 
                     String namespace = currentImporWSDL.getRootElement().getAttributeValue("targetNamespace");
                     NamespaceType type = CommonTools.getNamespaceType(info.getNamespaces(), namespace);
                     if (type != null) {
                         wsdlImportPackageNameTextField.setText(type.getPackageName());
                         wsdlImportPackageNameTextField.setEditable(false);
                     } else {
                         // see if the namespace is used by another
                         // service, if
                         // so
                         // we can suggest it's package name for these
                         // imports
                         boolean isUsedAlready = true;
                         ServiceType service = null;
                         for (int serviceI = 0; serviceI < info.getServices().getService().length; serviceI++) {
                             ServiceType tservice = info.getServices().getService(serviceI);
                             if (tservice.getNamespace().equals(namespace)) {
                                 isUsedAlready = true;
                                 service = tservice;
                                 break;
                             }
                         }
                         if ((service != null) && isUsedAlready) {
                             wsdlImportPackageNameTextField.setText(service.getPackageName());
                             wsdlImportPackageNameTextField.setEditable(false);
                         } else {
                             wsdlImportPackageNameTextField.setEditable(true);
                         }
                     }
                 }
             });
         }
         return wsdlFileBrowseButton;
     }
 
 
     /**
      * This method initializes wsdlServiceServicesComboBox
      * 
      * @return javax.swing.JComboBox
      */
     private JComboBox getWsdlServiceServicesComboBox() {
         if (wsdlServiceServicesComboBox == null) {
             wsdlServiceServicesComboBox = new JComboBox();
             if (method.isIsImported()) {
                 wsdlServiceServicesComboBox.addItem(method.getImportInformation().getPortTypeName());
             }
         }
         return wsdlServiceServicesComboBox;
     }
 
 
     /**
      * This method initializes wsdlImportPackageNameTextField
      * 
      * @return javax.swing.JTextField
      */
     private JTextField getWsdlImportPackageNameTextField() {
         if (wsdlImportPackageNameTextField == null) {
             wsdlImportPackageNameTextField = new JTextField();
             if (method.isIsImported()) {
                 wsdlImportPackageNameTextField.setText(method.getImportInformation().getPackageName());
             }
             wsdlImportPackageNameTextField.getDocument().addDocumentListener(new DocumentListener() {
 
                 public void removeUpdate(DocumentEvent e) {
                     validateImportInput();
                 }
 
 
                 public void insertUpdate(DocumentEvent e) {
                     validateImportInput();
                 }
 
 
                 public void changedUpdate(DocumentEvent e) {
                     validateImportInput();
                 }
 
             });
         }
         return wsdlImportPackageNameTextField;
     }
 
 
     /**
      * This method initializes descriptionPanel
      * 
      * @return javax.swing.JPanel
      */
     private JPanel getDescriptionPanel() {
         if (descriptionPanel == null) {
             GridBagConstraints gridBagConstraints44 = new GridBagConstraints();
             gridBagConstraints44.fill = GridBagConstraints.BOTH;
             gridBagConstraints44.insets = new Insets(2, 2, 2, 2);
             gridBagConstraints44.gridy = 0;
             gridBagConstraints44.gridx = 0;
             gridBagConstraints44.weighty = 0.0D;
             gridBagConstraints44.weightx = 1.0;
             descriptionPanel = new JPanel();
             descriptionPanel.setLayout(new GridBagLayout());
             descriptionPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Method Description",
                 javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                 javax.swing.border.TitledBorder.DEFAULT_POSITION, null, PortalLookAndFeel.getPanelLabelColor()));
             descriptionPanel.add(getDescriptionTextField(), gridBagConstraints44);
         }
         return descriptionPanel;
     }
 
 
     /**
      * This method initializes descriptionTextField
      * 
      * @return javax.swing.JTextField
      */
     private JTextField getDescriptionTextField() {
         if (descriptionTextField == null) {
             descriptionTextField = new JTextField();
             if (method.getDescription() != null) {
                 descriptionTextField.setText(method.getDescription());
             }
             descriptionTextField.getDocument().addDocumentListener(new DocumentListener() {
 
                 public void removeUpdate(DocumentEvent e) {
                     validateDescriptionInput();
                 }
 
 
                 public void insertUpdate(DocumentEvent e) {
                     validateDescriptionInput();
                 }
 
 
                 public void changedUpdate(DocumentEvent e) {
                     validateDescriptionInput();
                 }
             });
         }
         return descriptionTextField;
     }
 } // @jve:decl-index=0:visual-constraint="10,10"
