 /*******************************************************************************
  * Copyright (c) 2004, 2010 BREDEX GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     BREDEX GmbH - initial API and implementation and/or initial documentation
  *******************************************************************************/
 package org.eclipse.jubula.client.ui.rcp.widgets;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.jar.JarFile;
 import java.util.jar.Manifest;
 import java.util.zip.ZipException;
 
 import org.apache.commons.lang.StringUtils;
 import org.eclipse.core.databinding.validation.IValidator;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.jface.dialogs.Dialog;
 import org.eclipse.jface.window.Window;
 import org.eclipse.jubula.client.core.agent.AutAgentRegistration;
 import org.eclipse.jubula.client.core.model.IAUTConfigPO;
 import org.eclipse.jubula.client.core.model.IAUTConfigPO.ActivationMethod;
 import org.eclipse.jubula.client.ui.constants.ContextHelpIds;
 import org.eclipse.jubula.client.ui.constants.IconConstants;
 import org.eclipse.jubula.client.ui.rcp.Plugin;
 import org.eclipse.jubula.client.ui.rcp.businessprocess.RemoteFileBrowserBP;
 import org.eclipse.jubula.client.ui.rcp.dialogs.ClassPathDialog;
 import org.eclipse.jubula.client.ui.rcp.dialogs.NagDialog;
 import org.eclipse.jubula.client.ui.rcp.i18n.Messages;
 import org.eclipse.jubula.client.ui.rcp.provider.ControlDecorator;
 import org.eclipse.jubula.client.ui.rcp.utils.DialogStatusParameter;
 import org.eclipse.jubula.client.ui.rcp.utils.Utils;
 import org.eclipse.jubula.client.ui.utils.DialogUtils;
 import org.eclipse.jubula.client.ui.utils.ErrorHandlingUtil;
 import org.eclipse.jubula.client.ui.utils.LayoutUtil;
 import org.eclipse.jubula.client.ui.widgets.DirectCombo;
 import org.eclipse.jubula.client.ui.widgets.I18nEnumCombo;
 import org.eclipse.jubula.client.ui.widgets.JBText;
 import org.eclipse.jubula.client.ui.widgets.UIComponentHelper;
 import org.eclipse.jubula.toolkit.common.monitoring.MonitoringAttribute;
 import org.eclipse.jubula.toolkit.common.monitoring.MonitoringUtils;
 import org.eclipse.jubula.tools.constants.AutConfigConstants;
 import org.eclipse.jubula.tools.constants.MonitoringConstants;
 import org.eclipse.jubula.tools.constants.StringConstants;
 import org.eclipse.jubula.tools.exception.Assert;
 import org.eclipse.jubula.tools.messagehandling.MessageIDs;
 import org.eclipse.jubula.tools.registration.AutIdentifier;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.StyleRange;
 import org.eclipse.swt.custom.StyledText;
 import org.eclipse.swt.events.FocusEvent;
 import org.eclipse.swt.events.FocusListener;
 import org.eclipse.swt.events.KeyAdapter;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.DirectoryDialog;
 import org.eclipse.swt.widgets.FileDialog;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.List;
 
 
 
 /**
  * @author BREDEX GmbH
  * @created 13.02.2006
  * 
  */
 public abstract class JavaAutConfigComponent extends AutConfigComponent {
     
     /** Value for the default AUT Config mode */
     public static final String AUT_CONFIG_DIALOG_MODE_KEY_DEFAULT = 
         JavaAutConfigComponent.Mode.BASIC.name();
 
     /** the number of lines in a text field */
     protected static final int COMPOSITE_WIDTH = 250;
     
     /**
      * <code>MAX_TEXT_LENGTH</code>
      */
     private static final int MAX_TEXT_LENGTH = 4000;
     
     /** String constant for getting the main class from a JAR manifest */
     private static final String MAIN_CLASS = "Main-Class"; //$NON-NLS-1$
     
     /** default jre path for Windows */
     private static final String DEFAULT_WIN_JRE = "../jre/bin/java.exe"; //$NON-NLS-1$
      
     /** for check if the executable text field is empty */
     private static boolean isExecFieldEmpty = true;
        
     /** path of the executable file directory */
     private static String executablePath;
 
     /** for check if the executable text field is valid */
     private boolean m_isExecFieldValid = true;
         
     // internally used classes for data handling
     // internally used GUI components
     /** gui component */
     private JBText m_jarTextField;
     /** gui component */
     private Button m_jarButton;
     /** text field for the executable that will launch the AUT */
     private JBText m_execTextField;
     /** browse button for the executable */
     private Button m_execButton;
     /** gui component */
     private List m_classPathListField;
     /** gui component */
     private Composite m_classPathButtonComposite;
     /** gui component */
     private Button m_addElementButton;
     /** gui component */
     private Button m_editElementButton;
     /** gui component */
     private Button m_removeElementButton;
     /** move up button */
     private Button m_moveElementUpButton;
     /** move down button */
     private Button m_moveElementDownButton;
     /** gui component */
     private JBText m_classNameTextField;
     /** gui component */
     private JBText m_autArgsTextField;
     /** gui component */
     private I18nEnumCombo<ActivationMethod> m_activationMethodCombo;
     /** gui component */
     private JBText m_autJreTextField;
     /** gui component */
     private Button m_autJreButton;
     /** gui component */
     private Composite m_autJreComposite;
     /** gui component */
     private JBText m_autJreParamTextField;
     /** gui component */
     private DirectCombo<String> m_monitoringCombo;
     /** gui component */
     private JBText m_envTextArea;
     /** the WidgetModifyListener */
     private WidgetModifyListener m_modifyListener;
     /** the WidgetFocusListener */
     private WidgetFocusListener m_focusListener;
     /** the the WidgetSelectionListener */
     private WidgetSelectionListener m_selectionListener;
     /** the the WidgetKeyListener */
     private WidgetKeyListener m_keyListener;
     
     /**
      * @param parent {@inheritDoc}
      * @param style {@inheritDoc}
      * @param autConfig data to be displayed/edited
      * @param autName the name of the AUT that will be using this configuration.
      */
     public JavaAutConfigComponent(Composite parent, int style,
         Map<String, String> autConfig, String autName) {
         
         super(parent, style, autConfig, autName, true);
     }
     
     /**
      * Request for the NagDialog called in NewProject
      * @return true if the field for executable files is empty, false otherwise
      */
     public static boolean isExecFieldEmpty() {
         return isExecFieldEmpty;
     }
     
     /**
      * @param basicAreaComposite The composite that represents the basic area.
      */
     protected void createBasicArea(Composite basicAreaComposite) {
         super.createBasicArea(basicAreaComposite);
         initGUIAutConfigSettings(basicAreaComposite);
     }
     /**
      * {@inheritDoc}
      */
     protected void createMonitoringArea(Composite monitoringComposite) {     
         
         GridLayout result = (GridLayout)monitoringComposite.getLayout();        
         result.horizontalSpacing = 40;       
         result.numColumns = 2;
         monitoringComposite.setLayout(result);        
         final String monitoringID = super.getConfigValue(
                 AutConfigConstants.MONITORING_AGENT_ID);
               
         if (!StringUtils.isEmpty(monitoringID)) {  
             IConfigurationElement monitoringExtension = 
                 MonitoringUtils.getElement(monitoringID);
             if (monitoringExtension != null) {
                 createMonitoringUIComponents(monitoringComposite, 
                         MonitoringUtils.getAttributes(monitoringExtension));
             } else {
                 StyledText missingExtensionLabel = 
                     new StyledText(monitoringComposite, SWT.WRAP);
                 missingExtensionLabel.setText(
                         Messages.MissingMonitoringExtension);
                 missingExtensionLabel.setEditable(false);
                 missingExtensionLabel.setEnabled(false);
                 missingExtensionLabel.setStyleRange(new StyleRange(
                         0, missingExtensionLabel.getText().length(), 
                         null, null, SWT.ITALIC));
                 ControlDecorator.decorateWarning(
                         missingExtensionLabel, SWT.LEAD, 
                         "MissingMonitoringExtension.fieldDecorationText"); //$NON-NLS-1$
             }
         }
         resize();
         getShell().pack();
         super.createMonitoringArea(monitoringComposite);
                
     }
     /**
      * Dynamically creates GUI components for monitoring composite
      * @param monitoringComposite The composite to add the components to
      * @param monitoringAttributeList This list contains attributes from the extension point
      * 
      */
     private void createMonitoringUIComponents(Composite monitoringComposite, 
             java.util.List<MonitoringAttribute> monitoringAttributeList) {
         
         for (int i = 0; i < monitoringAttributeList.size(); i++) {
             final MonitoringAttribute attribute = 
                 monitoringAttributeList.get(i);
             if (!attribute.isRender()) { 
                 putConfigValue(attribute.getId(), attribute.getDefaultValue());
             } else {                     
                 if (attribute.getType().equalsIgnoreCase(
                         MonitoringConstants.RENDER_AS_TEXTFIELD)) { 
                     createMonitoringWidgetLabel(monitoringComposite, attribute);
                     createMonitoringTextFieldWidget(
                             monitoringComposite, attribute);
                 }                
                 if (attribute.getType().equalsIgnoreCase(
                         MonitoringConstants.RENDER_AS_FILEBROWSE)) {
                     createMonitoringWidgetLabel(monitoringComposite, attribute);
                     createMonitoringFilebrowse(monitoringComposite, attribute);
                 }
                 if (attribute.getType().equalsIgnoreCase(
                         MonitoringConstants.RENDER_AS_CHECKBOX)) { 
                     createMonitoringWidgetLabel(monitoringComposite, attribute);
                     createMonitoringCheckBoxWidget(
                             monitoringComposite, attribute);
                 }
             }
         }
     }
     /**
      * Creates the label for the monitoring widget
      * @param composite The monitoringComposite
      * @param attribute The MonitoringAttribute to get the information from
      */
     public void createMonitoringWidgetLabel(Composite composite, 
             MonitoringAttribute attribute) {
         
         Label widgetLabel = UIComponentHelper.createLabel(composite, 
                 attribute.getDescription());
         if (!StringUtils.isEmpty(attribute.getInfoBobbleText())) {
             ControlDecorator.decorateInfo(widgetLabel, 
                     attribute.getInfoBobbleText(), false);
         }
     }    
     /**
      * Creates a text field and a browse button
      * for the given monitoring composite 
      * @param composite The composite to add the widget on
      * @param att The current attribute to render   
      */
     private void createMonitoringFilebrowse(Composite composite, 
             MonitoringAttribute att) {
         
         Composite c = UIComponentHelper.createLayoutComposite(composite, 2);
         final JBText textField = createMonitoringTextFieldWidget(c, att);
         final Button browseButton = new Button(c, SWT.PUSH);
         browseButton.setText(Messages.AUTConfigComponentBrowse);
         browseButton.setLayoutData(BUTTON_LAYOUT);
         browseButton.setData(
                 MonitoringConstants.MONITORING_KEY, att.getId());
         browseButton.addSelectionListener(new SelectionAdapter() {
             public void widgetSelected(SelectionEvent event) { 
                 monitoringBrowseButtonSelected(textField);
             }
         });    
         
     }
        
     /**
      * Creates a Checkbox for the given monitoring composite, 
      * which was specified in the extension point.     * 
      * @param composite The composite to add the widget on
      * @param att The current attribute
      * 
      */
     private void createMonitoringCheckBoxWidget(Composite composite, 
             final MonitoringAttribute att) {
         
         final String autId = super.getConfigValue(AutConfigConstants.AUT_ID);  
         final Button b = 
             UIComponentHelper.createToggleButton(composite, 1);
         b.setData(MonitoringConstants.MONITORING_KEY, att.getId());
         b.addSelectionListener(new SelectionAdapter() {
             public void widgetSelected(SelectionEvent event) {
                 showMonitoringInfoDialog(autId); 
                 putConfigValue(att.getId(), 
                         String.valueOf(b.getSelection()));     
             }
         });
         
     }
     /**
      * creates a Textfield for a given monitoring composite,
      * which was specified in the extension point.
      * @param composite The monitoring composite
      * @param att the MonitoringAttribute
      * @return A monitoring textfield
      * 
      */
     private JBText createMonitoringTextFieldWidget(Composite composite, 
             final MonitoringAttribute att) {
         
         final JBText textField = UIComponentHelper.createTextField(
                 composite, 1);
         textField.setData(MonitoringConstants.MONITORING_KEY, 
                 att.getId());
         textField.setText(getConfigValue(att.getId()));
         final IValidator validator = att.getValidator();
         textField.addModifyListener(new ModifyListener() {              
             public void modifyText(ModifyEvent e) {                  
                 if (validator != null) {                      
                     IStatus status = 
                         validator.validate(textField.getText());      
                     if (!status.isOK()) {
                         DialogStatusParameter error = 
                             createErrorStatus(status.getMessage());
                         addError(error);                                     
                     } 
                     checkAll();                   
                 }
                 putConfigValue(att.getId(), textField.getText());     
             }
         });  
         final String autId = super.getConfigValue(AutConfigConstants.AUT_ID);
         textField.addFocusListener(new FocusListener() {            
             private String m_oldText = StringConstants.EMPTY;
             public void focusLost(FocusEvent e) {
                 String currentText = textField.getText();
                 if (!currentText.equals(m_oldText)) {
                     showMonitoringInfoDialog(autId);       
                 } 
                 putConfigValue(att.getId(), textField.getText()); 
             }                            
             public void focusGained(FocusEvent e) {
                 m_oldText = textField.getText();
             }
         });            
         return textField;
         
     }    
         
     /** if monitoring parameters changed, the AUT must be restarted, only than
      *  the changes will be active. This must be done, because at start up the
      *  autConfigMap will be stored in the MonitoringDataStore. 
      *  @param autId The autId
      */
     private void showMonitoringInfoDialog(String autId) {  
         
         LinkedList<AutIdentifier> l = 
             (LinkedList<AutIdentifier>)
             AutAgentRegistration.getInstance().getRegisteredAuts();
         String message = NLS.bind(Messages.ClientMonitoringInfoDialog, autId);
         for (AutIdentifier a : l) {
             if (a.getExecutableName().equals(autId)) {                
                 NagDialog.runNagDialog(null, message, 
                         ContextHelpIds.AUT_CONFIG_PROP_DIALOG);
             }
             
         }        
         
     }
     /**
      * This method handles the event which was fired when an item in the
      * Combobox is selected.    
      */
     private void handleMonitoringComboEvent() {  
         
         if (m_monitoringCombo.getSelectedObject() != null) {         
             
             putConfigValue(AutConfigConstants.MONITORING_AGENT_ID,   
                     m_monitoringCombo.getSelectedObject().toString()); 
           
             cleanComposite(getMonitoringAreaComposite());
             createMonitoringArea(getMonitoringAreaComposite());    
             
         } else {
             cleanComposite(getMonitoringAreaComposite());
             putConfigValue(AutConfigConstants.MONITORING_AGENT_ID, 
                     StringConstants.EMPTY);            
         }        
         String autId = super.getConfigValue(AutConfigConstants.AUT_ID);
         showMonitoringInfoDialog(autId);
         
     }
     /**
      * deletes all GUI elements form the given composite.
      * @param compostie A Composite from which all gui elements should be deleted
      */    
     private void cleanComposite(Composite compostie) { 
         
         Control[] ca = compostie.getChildren();
         for (int i = 0; i < ca.length; i++) {
             ca[i].dispose();
         }        
         resize();
         getShell().pack();
     }    
     
     /**
      * Inits the AUT configuration settings area.
      * 
      * @param parent The parent Composite.
      */
     private void initGUIAutConfigSettings(Composite parent) {
         // executable chooser
         UIComponentHelper.createLabel(parent, 
             "AUTConfigComponent.exec"); //$NON-NLS-1$ 
         m_execTextField = UIComponentHelper.createTextField(
                 parent, 1);
         UIComponentHelper.setMaxTextChars(m_execTextField, MAX_TEXT_LENGTH);
         m_execButton = new Button(
                 UIComponentHelper.createLayoutComposite(parent),
                 SWT.PUSH);
         m_execButton.setText(Messages.AUTConfigComponentBrowse);
         m_execButton.setLayoutData(BUTTON_LAYOUT);
         m_execButton.setEnabled(Utils.isLocalhost());
     }
 
     
     /**
      * {@inheritDoc}
      */
     protected boolean putConfigValue(String key, String value) {
         boolean hasChanged = super.putConfigValue(key, value); 
 
         if (hasChanged) {
             
             for (Control field : getJavaRelatedFields()) {
                 field.setEnabled(true);
             }
         
             boolean enableJarField = StringUtils.defaultString(
                 getConfigValue(AutConfigConstants.CLASSNAME)).length() == 0;
             m_jarTextField.setEnabled(enableJarField);
             m_jarButton.setEnabled(enableJarField
                     && (checkLocalhostServer() || isRemoteRequest()));
 
             m_classNameTextField.setEnabled(StringUtils.defaultString(
                 getConfigValue(AutConfigConstants.JAR_FILE)).length() == 0);
 
             String exe = getConfigValue(AutConfigConstants.EXECUTABLE);
             boolean isEmpty = exe == null || exe.length() == 0;
             for (Control field : getJavaRelatedFields()) {
                 field.setEnabled(isEmpty 
                     && field.isEnabled());
             }
             if (!isEmpty) {
                 m_classPathListField.setSelection(-1);
                 checkClasspathButtons();
             }
 
             String classname = getConfigValue(AutConfigConstants.CLASSNAME);
             String jar = getConfigValue(AutConfigConstants.JAR_FILE);
             boolean isClassnameEmpty = 
                 classname == null || classname.length() == 0;
             boolean isJarEmpty =  
                 jar == null || jar.length() == 0;
             boolean enableExe = isJarEmpty && isClassnameEmpty;
             m_execTextField.setEnabled(enableExe);
             m_execButton.setEnabled(enableExe
                     && (checkLocalhostServer() || isRemoteRequest()));
         
         }
         
         return hasChanged;
     }
 
     /**
      * installs all listeners to the gui components. All components visualizing
      * a property do have some sort of modification listeners which store edited
      * data in the edited instance. Some gui components have additional
      * listeners for data validation or permission reevaluation.
      */
     protected void installListeners() {
         super.installListeners();
 
         WidgetModifyListener modifyListener = getModifyListener();
         WidgetKeyListener keyListener = getKeyListener();
         WidgetSelectionListener selectionListener = getSelectionListener();
 
         m_activationMethodCombo.addSelectionListener(selectionListener);
         m_autJreParamTextField.addModifyListener(modifyListener);
         m_envTextArea.addModifyListener(modifyListener);
         m_classPathListField.addKeyListener(keyListener);
         m_classPathListField.addSelectionListener(selectionListener);
         m_addElementButton.addSelectionListener(selectionListener);
         m_editElementButton.addSelectionListener(selectionListener);
         m_moveElementUpButton.addSelectionListener(selectionListener);
         m_moveElementDownButton.addSelectionListener(selectionListener);
         m_removeElementButton.addSelectionListener(selectionListener);
         m_jarButton.addSelectionListener(selectionListener);
         m_jarTextField.addModifyListener(modifyListener);
         m_classNameTextField.addModifyListener(modifyListener);
         getServerCombo().addModifyListener(modifyListener);
         m_autJreButton.addSelectionListener(selectionListener);
         m_autJreTextField.addModifyListener(modifyListener);
         m_autArgsTextField.addModifyListener(modifyListener);
         m_execTextField.addFocusListener(getFocusListener());
         m_execTextField.addModifyListener(modifyListener);
         m_execButton.addSelectionListener(selectionListener);
         m_monitoringCombo.addSelectionListener(selectionListener);
         
     }
 
     /**
      * deinstalls all listeners to the gui components. All components
      * visualizing a property do have some sort of modification listeners which
      * store edited data in the edited instance. Some gui components have
      * additional listeners for data validatuion or permission reevaluation.
      */
     protected void deinstallListeners() {
         super.deinstallListeners();
 
         WidgetModifyListener modifyListener = getModifyListener();
         WidgetKeyListener keyListener = getKeyListener();
         WidgetSelectionListener selectionListener = getSelectionListener();
 
         m_activationMethodCombo.removeSelectionListener(selectionListener);
         m_autJreParamTextField.removeModifyListener(modifyListener);
         m_envTextArea.removeModifyListener(modifyListener);
         m_classPathListField.removeKeyListener(keyListener);
         m_addElementButton.removeSelectionListener(selectionListener);
         m_autJreButton.removeSelectionListener(selectionListener);
         m_autJreTextField.removeModifyListener(modifyListener);
         m_classNameTextField.removeModifyListener(modifyListener);
         m_classPathListField.removeSelectionListener(selectionListener);
         m_editElementButton.removeSelectionListener(selectionListener);
         m_jarButton.removeSelectionListener(selectionListener);
         m_jarTextField.removeModifyListener(modifyListener);
         m_autArgsTextField.removeModifyListener(modifyListener);
         m_removeElementButton.removeSelectionListener(selectionListener);
         getServerCombo().removeModifyListener(modifyListener);
         m_execTextField.removeFocusListener(getFocusListener());
         m_execTextField.removeModifyListener(modifyListener);
         m_execButton.removeSelectionListener(selectionListener);
         m_monitoringCombo.removeSelectionListener(selectionListener);
 
     }
     /**
      * 
      * @param parent The parent Composite.
      */
     private void initGuiEnvironmentEditor(Composite parent) {
         UIComponentHelper.createLabel(parent, "AUTConfigComponent.envVariables"); //$NON-NLS-1$ 
         m_envTextArea = new JBText(parent, LayoutUtil.MULTI_TEXT_STYLE);
         LayoutUtil.setMaxChar(m_envTextArea, 4000);
         GridData textGridData = new GridData();
         textGridData.horizontalAlignment = GridData.FILL;
         textGridData.horizontalSpan = 2;
         textGridData.grabExcessHorizontalSpace = false;
         textGridData.widthHint = COMPOSITE_WIDTH;
         textGridData.heightHint = Dialog.convertHeightInCharsToPixels(LayoutUtil
             .getFontMetrics(m_envTextArea), 5);
         m_envTextArea.setLayoutData(textGridData);
     }
 
     /**
      * 
      * @param parent The parent Composite.
      */
     private void initGuiJarChooser(Composite parent) {
         UIComponentHelper.createLabel(parent, "AUTConfigComponent.jar"); //$NON-NLS-1$ 
         m_jarTextField = UIComponentHelper.createTextField(parent, 1);
         UIComponentHelper.setMaxTextChars(m_jarTextField, MAX_TEXT_LENGTH);
         m_jarButton = new Button(UIComponentHelper
                 .createLayoutComposite(parent), SWT.PUSH);
         m_jarButton.setText(Messages.AUTConfigComponentBrowse);
         m_jarButton.setLayoutData(BUTTON_LAYOUT);
         m_jarButton.setEnabled(Utils.isLocalhost());
     }
 
     /**
      * Creates three buttons for the class path editor.
      * 
      * @param parent The parent composite.
      */
     private void initGuiClasspathEditor(Composite parent) {
         UIComponentHelper.createLabel(
             parent, "AUTConfigComponent.classPath"); //$NON-NLS-1$ 
         m_classPathListField = new List(parent, 
             LayoutUtil.MULTI_TEXT_STYLE | SWT.SINGLE);
         GridData textGridData = new GridData();
         textGridData.horizontalAlignment = GridData.FILL;
         textGridData.grabExcessHorizontalSpace = true;
         textGridData.heightHint = Dialog.convertHeightInCharsToPixels(LayoutUtil
             .getFontMetrics(m_classPathListField), 8);
         LayoutUtil.addToolTipAndMaxWidth(textGridData, m_classPathListField);
         m_classPathListField.setLayoutData(textGridData);
         m_classPathButtonComposite = 
             UIComponentHelper.createLayoutComposite(parent);
         m_addElementButton = new Button(m_classPathButtonComposite, SWT.PUSH);
         m_addElementButton.setText(Messages.AUTConfigComponentElement);
         m_addElementButton.setLayoutData(BUTTON_LAYOUT);
         
         m_moveElementUpButton = 
             new Button(m_classPathButtonComposite, SWT.PUSH);
         m_moveElementUpButton.setImage(IconConstants.UP_ARROW_DIS_IMAGE);
         m_moveElementUpButton.setToolTipText(
             Messages.AutConfigDialogMoveCpUpToolTip);
         m_moveElementUpButton.setLayoutData(BUTTON_LAYOUT);
 
         m_moveElementDownButton = 
             new Button(m_classPathButtonComposite, SWT.PUSH);
         m_moveElementDownButton.setImage(IconConstants.DOWN_ARROW_DIS_IMAGE);
         m_moveElementDownButton.setToolTipText(
             Messages.AutConfigDialogMoveCpDownToolTip);
         m_moveElementDownButton.setLayoutData(BUTTON_LAYOUT);
 
         m_editElementButton = new Button(m_classPathButtonComposite, SWT.PUSH);
         m_editElementButton.setText(Messages.AUTConfigComponentEdit);
         m_editElementButton.setLayoutData(BUTTON_LAYOUT);
 
         m_removeElementButton = 
             new Button(m_classPathButtonComposite, SWT.PUSH);
         m_removeElementButton.setText(Messages.AUTConfigComponentRemove);
         m_removeElementButton.setLayoutData(BUTTON_LAYOUT);
         
         checkClasspathButtons();
     }
 
     /**
      * @param classPath The classPath to set.
      */
     private void initDataClassPath(String classPath) {
         m_classPathListField.removeAll();
         if (!StringUtils.isEmpty(classPath)) {
             String[] pathList = classPath.split(StringConstants.SEMICOLON);
             for (int i = 0; i < pathList.length; i++) {
                 m_classPathListField.add(pathList[i]);
             }
         }
     }
 
     /**
      * 
      * {@inheritDoc}
      */
     protected void initState() {
         m_activationMethodCombo.setEnabled(true);
         m_autJreParamTextField.setEnabled(true);
         m_envTextArea.setEnabled(true);
         m_jarTextField.setEnabled(true);
         m_jarButton.setEnabled(true);
         checkLocalhostServer();
         RemoteFileBrowserBP.clearCache(); // avoid all caches
     }
     
     /** 
      * The action of the environment field.
      * @return <code>null</code> if the new value is valid. Otherwise, returns
      *         a status parameter indicating the cause of the problem.
      */
     DialogStatusParameter modifyEnvFieldAction() {
         putConfigValue(AutConfigConstants.ENVIRONMENT, m_envTextArea.getText());
         return null;
     }
     
     /** 
      * The action of the jre parameter field.
      * @return <code>null</code> if the new value is valid. Otherwise, returns
      *         a status parameter indicating the cause of the problem.
      */
     DialogStatusParameter modifyJreParamFieldAction() {
         putConfigValue(AutConfigConstants.JRE_PARAMETER, 
                 m_autJreParamTextField.getText());
         return null;
     }
     
     /** 
      * The action of the aut parameter field.
      * @return <code>null</code> if the new value is valid. Otherwise, returns
      *         a status parameter indicating the cause of the problem.
      */
     DialogStatusParameter modifyAutParamFieldAction() {
         String params = m_autArgsTextField.getText();
         putConfigValue(AutConfigConstants.AUT_ARGUMENTS, params);
         return null;
     }
     
     /** 
      * The action of the activation combo
      * @return true
      */
     boolean handleActivationComboEvent() {
         putConfigValue(AutConfigConstants.ACTIVATION_METHOD,
                 ActivationMethod.getRCString(m_activationMethodCombo
                         .getSelectedObject()));
         return true;
     }
 
     /** 
      * The action of the class name field.
      * @return <code>null</code> if the new value is valid. Otherwise, returns
      *         a status parameter indicating the cause of the problem.
      */
     DialogStatusParameter modifyClassNameFieldAction() {
         if (m_classNameTextField == null || m_classNameTextField.isDisposed()) {
             return null;
         }
 
         DialogStatusParameter error = null;
         putConfigValue(AutConfigConstants.CLASSNAME, 
                 m_classNameTextField.getText());
         if (!isValid(m_classNameTextField, true) 
             && m_classNameTextField.getText().length() != 0) {
 
             error = createErrorStatus(
                     Messages.AUTConfigComponentWrongClassName);
         }
         
         return error;
     }
 
     /** 
      * The action of the JAR name field.
      * @return <code>null</code> if the new value is valid. Otherwise, returns
      *         a status parameter indicating the cause of the problem.
      */
     DialogStatusParameter modifyJarFieldAction() {
 
         DialogStatusParameter error = null;
         boolean isEmpty = m_jarTextField.getText().length() == 0;
         if (isValid(m_jarTextField, true) && !isEmpty) {
             if (checkLocalhostServer()) {
                 String filename = m_jarTextField.getText();
                 File file = new File(filename);
                 String workingDir = StringUtils.defaultString(
                         getConfigValue(AutConfigConstants.WORKING_DIR));
                 if (!file.isAbsolute()
                     && workingDir.length() != 0) {
                     
                     filename = workingDir + "/" + filename; //$NON-NLS-1$
                     file = new File(filename);
                 }
                 try {
                     if (!file.exists()) {
                         error = createWarningStatus(
                             NLS.bind(Messages.AUTConfigComponentFileNotFound,
                                     file.getCanonicalPath()));
                     } else {
                         JarFile jarFile = new JarFile(file);
                         Manifest jarManifest = jarFile.getManifest();
                         if (jarManifest == null) {
                             // no manifest for JAR
                             error = createErrorStatus(
                                     Messages.AUTConfigComponentNoManifest);
                         } else if (
                             jarManifest.getMainAttributes().getValue(MAIN_CLASS)
                                 == null) {
                             
                             // no main class defined in JAR manifest
                             error = createErrorStatus(
                                     Messages.AUTConfigComponentNoMainClass);
                         }
                     }
                 } catch (ZipException ze) {
                     // given file is not a jar file
                     error = createErrorStatus(
                         NLS.bind(Messages.AUTConfigComponentFileNotJar,
                                 filename));
                 } catch (IOException e) {
                     // could not find jar file
                     error = createWarningStatus(NLS.bind(
                             Messages.AUTConfigComponentFileNotFound,
                                 filename));
                 }
             }
         } else if (!isEmpty) {
             error = createErrorStatus(Messages.AUTConfigComponentWrongJAR);
         }
 
         putConfigValue(AutConfigConstants.JAR_FILE, m_jarTextField.getText());
         
         return error;
     }
     /**
      * Populates GUI for the advanced configuration section, and Displays the
      * current Values of the Activation_METHOD and monitoring agents in the 
      * ComboBoxes
      * @param data map representing the data to use for population.
      */    
     
     protected void populateExpertArea(Map<String, String> data) {
 
         m_activationMethodCombo.setSelectedObject(
                 ActivationMethod.getEnum(data
                         .get(AutConfigConstants.ACTIVATION_METHOD)));
         
         String monitoringAgentId = data.get(
                 AutConfigConstants.MONITORING_AGENT_ID);
         if (StringUtils.isEmpty(monitoringAgentId)) { 
             m_monitoringCombo.deselectAll();
         } else {
             m_monitoringCombo.setSelectedObject(monitoringAgentId);
             if (m_monitoringCombo.getSelectedObject() == null) {
                 // additional handling for missing Monitoring extension
                 ArrayList<String> values = 
                     new ArrayList<String>(m_monitoringCombo.getValues());
                 ArrayList<String> displayValues = new ArrayList<String>(
                         Arrays.asList(m_monitoringCombo.getItems()));
                 values.add(0, monitoringAgentId);
                 values.remove(null);
                 displayValues.add(0, monitoringAgentId);
                 displayValues.remove(StringUtils.EMPTY);
                 
                 m_monitoringCombo.setItems(values, displayValues);
                 m_monitoringCombo.setSelectedObject(monitoringAgentId);
             }
         }
         
         if (!isDataNew(data)) {
             m_autJreParamTextField.setText(StringUtils.defaultString(data
                     .get(AutConfigConstants.JRE_PARAMETER)));
             m_envTextArea.setText(StringUtils.defaultString(data
                     .get(AutConfigConstants.ENVIRONMENT)));
         }
 
     }
     /**
      * {@inheritDoc}
      */
     protected void populateMonitoringArea(Map<String, String> data) {
         Composite composite = getMonitoringAreaComposite();
         Control[] ca = composite.getChildren();
         
         for (int i = 0; i < ca.length; i++) {                       
             if (ca[i].getData(MonitoringConstants.MONITORING_KEY) != null) {
                 if (ca[i] instanceof JBText) {
                     JBText t = (JBText) ca[i];
                     String value = data.get(String.valueOf(
                             t.getData(MonitoringConstants.MONITORING_KEY)));
                     if (value != null && !value.equals(StringConstants.EMPTY)) {
                         t.setText(value);
                     }
                 }
                 if (ca[i] instanceof Button) {
                     Button b = (Button) ca[i];
                     String value = data.get(String.valueOf(b
                             .getData(MonitoringConstants.MONITORING_KEY)));
                     if (value != null) {
                         b.setSelection(Boolean.valueOf(value));
                     }
                 }
         
             }
         }
     }
     /**
      * Populates GUI for the advanced configuration section.
      * 
      * @param data Map representing the data to use for population.
      */
     protected void populateAdvancedArea(Map<String, String> data) {
         // class name
         m_classNameTextField.setText(
             StringUtils.defaultString(data.get(AutConfigConstants.CLASSNAME)));
 
         // class path
         initDataClassPath(
             StringUtils.defaultString(data.get(AutConfigConstants.CLASSPATH)));
 
         // aut arguments
         m_autArgsTextField.setText(
             StringUtils.defaultString(data.get(
                     AutConfigConstants.AUT_ARGUMENTS)));
         // JRE
         String jreDirectory = StringUtils.defaultString(
                 getConfigValue(AutConfigConstants.JRE_BINARY));
         if (isConfigNew()) {
             jreDirectory = getDefaultJreBin();
             modifyJREFieldAction();
         }
         m_autJreTextField.setText(jreDirectory);
     }
 
     /**
      * 
      * @return the absolute path and filename of the executable used to start
      *         the current JVM. Returns the empty string if the executable 
      *         cannot be found for any reason.
      */
     private String getDefaultJreBin() {
         File jreBin = new File(DEFAULT_WIN_JRE);
         if (jreBin.exists()) {
             return DEFAULT_WIN_JRE;
         }
 
         return StringConstants.EMPTY;
     }
     
     /**
      * Populates GUI for the basic configuration section.
      * 
      * @param data Map representing the data to use for population.
      */
     protected void populateBasicArea(Map<String, String> data) {
 
         super.populateBasicArea(data);
         
         if (!isDataNew(data)) {
             getServerCombo().select(getServerCombo().indexOf(StringUtils
                     .defaultString(data.get(AutConfigConstants.SERVER))));
             m_jarTextField.setText(StringUtils.defaultString(data
                 .get(AutConfigConstants.JAR_FILE)));
             m_execTextField.setText(StringUtils.defaultString(data
                 .get(AutConfigConstants.EXECUTABLE)));
 
         }
  
     }
 
     /** 
      * The action of the JRE name field.
      * @return <code>null</code> if the new value is valid. Otherwise, returns
      *         a status parameter indicating the cause of the problem.
      */
     DialogStatusParameter modifyJREFieldAction() {
         DialogStatusParameter error = null;
         putConfigValue(AutConfigConstants.JRE_BINARY, 
                 m_autJreTextField.getText());
         if (!isValid(m_autJreTextField, true) 
             && m_autJreTextField.getText().length() != 0) {
             
             error = createErrorStatus(Messages.AUTConfigComponentWrongJRE);
         }
         return error;
     }
 
     /**
      * Writes the path of the executable file in the AUT Working Dir field.
      * @param directory The dir path of the executable file as string.
      */
     private void setWorkingDirToExecFilePath(String directory) {
         if ((StringUtils.isEmpty(getAutWorkingDirField().getText()) 
                 || isBasicMode())
                 && isFilePathAbsolute(directory) && m_isExecFieldValid) {
             File wd = new File(directory);
             wd = wd.getParentFile();
             if (wd != null) {
                 String execPath = wd.getAbsolutePath();
 
                 getAutWorkingDirField().setText(execPath);
                 putConfigValue(AutConfigConstants.WORKING_DIR, execPath);
             }
         }
     }
     
     /**
      * @param filename to check
      * @return true if the path of the given executable file is absolute
      */
     private static boolean isFilePathAbsolute(String filename) {
         final File execFile = new File(filename);
         return execFile.isAbsolute();
     }
     
     /**
      * Sets the enablement for the move classpath entry up button.
      * @param enabled if the button should be enabled.
      */
     public void setClasspathUpEnabled(boolean enabled) {
         m_moveElementUpButton.setEnabled(enabled);
         if (enabled) {
             m_moveElementUpButton.setImage(IconConstants.UP_ARROW_IMAGE);
         } else {
             m_moveElementUpButton.setImage(IconConstants.UP_ARROW_DIS_IMAGE);
         }
     }
     
     /**
      * Sets the enablement for the move classpath entry down button.
      * @param enabled if the button should be enabled.
      */
     public void setClasspathDownEnabled(boolean enabled) {
         m_moveElementDownButton.setEnabled(enabled);
         if (enabled) {
             m_moveElementDownButton.setImage(IconConstants.DOWN_ARROW_IMAGE);
         } else {
             m_moveElementDownButton.setImage(
                 IconConstants.DOWN_ARROW_DIS_IMAGE);
         }
     }
 
     /** Handles the aut-list event. */
     void handleClassPathListEvent() {
         checkClasspathButtons();
         m_classPathListField.setFocus();
     }
     
     /**
      * Handle the selection event of the move element down button
      */
     void handleDownButtonEvent() {
         int [] selectedIndices = m_classPathListField.getSelectionIndices();
         Arrays.sort(selectedIndices);
         int [] newSelectedIndices = new int [selectedIndices.length];
         int greatestIndex = m_classPathListField.getItemCount() - 1;
         if (selectedIndices.length > 0 
             && selectedIndices[selectedIndices.length - 1] 
                                < greatestIndex) {
             
             for (int i = 0; i < selectedIndices.length; i++) {
                 int index = selectedIndices[i];
                 int newIndex = index + 1;
                 String item = m_classPathListField.getItem(index);
                 m_classPathListField.remove(index);
                 m_classPathListField.add(item, newIndex);
                 newSelectedIndices[i] = newIndex;
             }
             m_classPathListField.setSelection(newSelectedIndices);
         }
         
         checkClasspathButtons();
     }
 
     /**
      * Handle the selection event of the move element up button
      */
     public void handleUpButtonEvent() {
         int [] selectedIndices = m_classPathListField.getSelectionIndices();
         Arrays.sort(selectedIndices);
         int [] newSelectedIndices = new int [selectedIndices.length];
         if (selectedIndices.length > 0 && selectedIndices[0] > 0) {
             for (int i = 0; i < selectedIndices.length; i++) {
                 int index = selectedIndices[i];
                 int newIndex = index - 1;
                 String item = m_classPathListField.getItem(index);
                 m_classPathListField.remove(index);
                 m_classPathListField.add(item, newIndex);
                 newSelectedIndices[i] = newIndex;
             }
             m_classPathListField.setSelection(newSelectedIndices);
         }
         
         checkClasspathButtons();
     }
 
     /**
      * handle the browse request locally
      * 
      * @param extensionFilters Only files with one of the 
      *                         provided extensions will be shown in the dialog.
      *                         May be <code>null</code>, in which case all 
      *                         files will be shown.
      * @param configVarKey key for storing the result
      * @param textField control for visualizing the value
      * @param title window title
      */
     void browseLocal(String [] extensionFilters, String title, 
             JBText textField, String configVarKey) {
         String directory;
         FileDialog fileDialog = 
             new FileDialog(getShell(), SWT.APPLICATION_MODAL | SWT.ON_TOP);
         if (extensionFilters != null) {
             fileDialog.setFilterExtensions(extensionFilters);
         }
         fileDialog.setText(title);
         String filterPath = Utils.getLastDirPath();
         File path = new File(textField.getText());
         if (!path.isAbsolute()) {
             path = new File(getConfigValue(AutConfigConstants.WORKING_DIR), 
                 textField.getText());
         }
         if (path.exists()) {
             try {
                 if (path.isDirectory()) {
                     filterPath = path.getCanonicalPath();
                 } else {
                     filterPath = new File(path.getParent()).getCanonicalPath();
                 }
             } catch (IOException e) {
                 // Just use the default filter path which is already set
             }
         }
         fileDialog.setFilterPath(filterPath);
         directory = fileDialog.open();
         if (directory != null) {
             textField.setText(directory);
             Utils.storeLastDirPath(fileDialog.getFilterPath());
             putConfigValue(configVarKey, directory);
         }
     }
 
     /**
      * Handles the button event.
      * @param fileDialog The file dialog.
      */
     void handleExecButtonEvent(FileDialog fileDialog) {
         String directory;
         fileDialog.setText(
             Messages.AUTConfigComponentSelectExecutable);
         String filterPath = Utils.getLastDirPath();
         File path = new File(getConfigValue(AutConfigConstants.EXECUTABLE));
         if (!path.isAbsolute()) {
             path = new File(getConfigValue(AutConfigConstants.WORKING_DIR), 
                 getConfigValue(AutConfigConstants.EXECUTABLE));
         }
         if (path.exists()) {
             try {
                 if (path.isDirectory()) {
                     filterPath = path.getCanonicalPath();
                 } else {
                     filterPath = new File(path.getParent()).getCanonicalPath();
                 }
             } catch (IOException e) {
                 // Just use the default filter path which is already set
             }
         }
         fileDialog.setFilterPath(filterPath);
         directory = fileDialog.open();
         if (directory != null) {
             m_execTextField.setText(directory);
             Utils.storeLastDirPath(fileDialog.getFilterPath());
             putConfigValue(AutConfigConstants.EXECUTABLE, directory);
             executablePath = directory;
             
             setWorkingDirToExecFilePath(executablePath);
         }
     }
     
     /**
      * handler for remote browsing
      */
     private void handleExecButtonEventForRemote() {
 
         if (remoteBrowse(false, AutConfigConstants.EXECUTABLE, m_execTextField,
                 Messages.AUTConfigComponentSelectExecutable)) { 
             setWorkingDirToExecFilePath(executablePath);
         }
     }
     
     /**
      * The action of the working directory field.
      * @return <code>null</code> if the new value is valid. Otherwise, returns
      *         a status parameter indicating the cause of the problem.
      */
     DialogStatusParameter modifyExecTextField() {
         DialogStatusParameter error = null;
         m_isExecFieldValid = true;
         isExecFieldEmpty = m_execTextField.getText().length() == 0;
         String filename = m_execTextField.getText();
         if (isValid(m_execTextField, true) && !isExecFieldEmpty) { 
             if (checkLocalhostServer()) {
                 File file = new File(filename);
                 if (!file.isAbsolute()) {
                     String workingDirString = 
                         getConfigValue(AutConfigConstants.WORKING_DIR);
                     if (workingDirString != null 
                         && workingDirString.length() != 0) {
                         
                         filename = workingDirString + "/" + filename; //$NON-NLS-1$
                         file = new File(filename);
                     }
                 }
 
                 try {
                     if (!file.isFile()) {
                         error = createWarningStatus(NLS.bind(
                             Messages.AUTConfigComponentFileNotFound,
                                 file.getCanonicalPath()));
                     } else {
                         // Make sure that the user has not entered an executable
                         // JAR file in the wrong field.
                         new JarFile(file);
                         error = createErrorStatus(NLS.bind(
                             Messages.AUTConfigComponentFileJar,
                                 file.getCanonicalPath()));
                     }
                 } catch (ZipException ze) {
                     // Expected. This occurs if the given file exists but is not 
                     // a JAR file.
                 } catch (IOException e) {
                     // could not find file
                     error = createWarningStatus(NLS.bind(
                         Messages.AUTConfigComponentFileNotFound,
                             filename));
                 }
             }
         } else if (!isExecFieldEmpty) {
             error = createErrorStatus(
                     Messages.AUTConfigComponentWrongExecutable);
         }
         if (error != null) {
             m_isExecFieldValid = false;
         }
         putConfigValue(AutConfigConstants.EXECUTABLE, 
                 m_execTextField.getText());
         executablePath = filename;
         
         return error;
     }
 
     /**
      * Checks and sets the enablement for classpath area buttons
      */
     public void checkClasspathButtons() {
         if (m_classPathListField.getItemCount() == 0) {
             m_removeElementButton.setEnabled(false);
             m_editElementButton.setEnabled(false);
             setClasspathUpEnabled(false);
             setClasspathDownEnabled(false);
             return;
         }
         if (m_classPathListField.getSelectionCount() > 0) {
             String[] selection = m_classPathListField.getSelection();
             if (!StringConstants.EMPTY.equals(selection[0])) { 
                 m_removeElementButton.setEnabled(true);
                 m_editElementButton.setEnabled(true);
             
                 int [] indices = m_classPathListField.getSelectionIndices();
                 int smallestIndex = indices[0];
                 int largestIndex = indices[0];
                 for (int index : indices) {
                     smallestIndex = Math.min(smallestIndex, index);
                     largestIndex = Math.max(largestIndex, index);
                 }
                     
                 setClasspathUpEnabled(smallestIndex > 0);
                 setClasspathDownEnabled(
                     largestIndex < m_classPathListField.getItemCount() - 1);
             }
         } else {
             m_removeElementButton.setEnabled(false);
             m_editElementButton.setEnabled(false);
             m_moveElementDownButton.setEnabled(false);
             m_moveElementUpButton.setEnabled(false);
         }
         
     }
     
     /**
      * Handles the button event.
      */
     public void handleRemoveButtonEvent() {
         int selectionIndex = m_classPathListField.getSelectionIndex();
         m_classPathListField.remove(m_classPathListField
                 .getSelection()[0]);
         if (m_classPathListField.getItemCount() >= selectionIndex) {
             m_classPathListField.select(selectionIndex - 1);
         }
         if (m_classPathListField.getItemCount() == 1) {
             m_classPathListField.select(0);
         }
         if (m_classPathListField.getSelectionCount() == 0) {
             m_classPathListField.select(0);
         }
         handleClassPathListEvent();
         storeClassPath();
     }
     
     /**
      * Stores the classpath list to the AUT Configuration.
      */
     private void storeClassPath() {
         
         String classPath = StringConstants.EMPTY;
         
         for (int i = 0; i < m_classPathListField.getItemCount(); i++) {
             classPath = 
                 classPath.concat(m_classPathListField.getItem(i) + ";"); //$NON-NLS-1$
         }
         if (!StringConstants.EMPTY.equals(classPath)) {
             // cut off the last semicolon
             classPath = classPath.substring(0, classPath.length() - 1);
         }
         putConfigValue(AutConfigConstants.CLASSPATH, classPath);
     }
 
     /**
      * checks if adding an element to classpath is allowed, or if classpath
      * exceeds maximum length
      * @param elementToAdd String
      * @return boolean
      */
     private boolean isClassPathLengthAllowed(String elementToAdd) {
         String classPath = StringConstants.EMPTY;
         for (int i = 0; i < m_classPathListField.getItemCount(); i++) {
             classPath = classPath.concat(m_classPathListField.getItem(i) + ";"); //$NON-NLS-1$
         }
         classPath = classPath.concat(elementToAdd + ";"); //$NON-NLS-1$
         if (classPath.length() <= IAUTConfigPO.MAX_CLASSPATH_LENGTH) {
             return true;
         }
         return false;
     }
 
     /**
      * Handles the button event.
      * @param editButtonWasPressed true, if edit button was pressed
      */
     public void handleAddElementButtonEvent(boolean editButtonWasPressed) {
         int maxLength = IAUTConfigPO.MAX_CLASSPATH_LENGTH 
             - getClassPathLength();
         if (maxLength < 1) {
             ErrorHandlingUtil.createMessageDialog(
                     MessageIDs.I_TOO_LONG_CLASSPATH, 
                     new Object[] {IAUTConfigPO.MAX_CLASSPATH_LENGTH}, null);
             return;
         }
         String oldText = StringConstants.EMPTY;
         if (editButtonWasPressed) {
             oldText = m_classPathListField.getSelection()[0];
         }
         ClassPathDialog dialog = new ClassPathDialog(
                 Plugin.getShell(), 
                 Messages.AUTConfigComponentClassPathDialogTitle,
                 oldText, Messages.AUTConfigComponentMessage, 
                 Messages.AUTConfigComponentLabel,
                 Messages.AUTConfigComponentWrongInputMessage,
                 StringConstants.EMPTY, IconConstants.CLASS_PATH_STRING,
                 Messages.AUTConfigComponentShellText,
                 false, maxLength, checkLocalhostServer());
         dialog.setStyle(SWT.APPLICATION_MODAL);
         dialog.create();
         DialogUtils.setWidgetNameForModalDialog(dialog);
         dialog.open();
         if (dialog.getReturnCode() == Window.OK) {
             String[] elements = dialog.getName().split(System.getProperty("path.separator")); //$NON-NLS-1$
             for (int i = 0; i < elements.length; i++) {
                 if (!StringConstants.EMPTY.equals(elements[i])) {
                     if (isClassPathLengthAllowed(elements[i])) {
                         if (editButtonWasPressed) {
                             m_classPathListField.remove(oldText);
                         }
                         m_classPathListField.add(elements[i]);
                     } else {
                         ErrorHandlingUtil.createMessageDialog(MessageIDs
                                 .I_TOO_LONG_CLASSPATH, new Object[] {
                                     IAUTConfigPO.MAX_CLASSPATH_LENGTH}, null);
                         return;
                     }
                 }
             }
             storeClassPath();
         }
     }
     
     /**
      * returns the classpath length
      * @return int
      */
     private int getClassPathLength() {
         String classPath = StringConstants.EMPTY;
         for (int i = 0; i < m_classPathListField.getItemCount(); i++) {
             classPath = classPath.concat(m_classPathListField.getItem(i) + ";"); //$NON-NLS-1$
         }
         return classPath.length();
     }
     
     /**
      * 
      * {@inheritDoc}
      */
     protected boolean checkLocalhostServer() {
         boolean enable = super.checkLocalhostServer();
         boolean browseEnabled = enable || isRemoteRequest();
         m_jarButton.setEnabled(browseEnabled && m_jarTextField.getEnabled());
         m_execButton.setEnabled(browseEnabled && m_execButton.isEnabled());
         m_autJreButton.setEnabled(browseEnabled && m_autJreButton.isEnabled());
         return enable;
     }
     
     /**
      * This private inner class contains a new ModifyListener.
      * 
      * @author BREDEX GmbH
      * @created 22.11.2006
      */
     private class WidgetModifyListener implements ModifyListener {
         /**
          * {@inheritDoc}
          */
         @SuppressWarnings("synthetic-access")
         public void modifyText(ModifyEvent e) {
             Object source = e.getSource();
             if (source.equals(getServerCombo())) {
                 checkLocalhostServer();
                 boolean checkListeners = m_selectionListener != null;
                 if (checkListeners) {
                     deinstallListeners();
                 }
                 if (checkListeners) {
                     installListeners();
                 }
             } 
 
             checkAll();         
         }
     }
         
     /**
      * This private inner class contains a new FocusListener.
      * 
      * @author BREDEX GmbH
      * @created 03.07.2008
      */
     @SuppressWarnings("synthetic-access")
     private class WidgetFocusListener implements FocusListener {
 
         /**
          * {@inheritDoc}
          */
         public void focusGained(FocusEvent e) {
             // do nothing
         }
 
         /**
          * {@inheritDoc}
          */
         public void focusLost(FocusEvent e) {
             Object source = e.getSource();
             if (source.equals(m_execTextField)) {
                 setWorkingDirToExecFilePath(executablePath);
             }
         }
     }
     
     /**
      * {@inheritDoc}
      */
     protected boolean isJavaAut() {
         return true;
     }
     
     /**
      * This private inner class contains a new KeyListener.
      * 
      * @author BREDEX GmbH
      * @created 10.09.2007
      */
     private class WidgetKeyListener extends KeyAdapter {
 
         /**
          * {@inheritDoc}
          */
         @SuppressWarnings("synthetic-access")
         public void keyPressed(KeyEvent e) {
             if (e.widget == m_classPathListField
                 && e.stateMask == SWT.ALT) {
                 
                 if (e.keyCode == SWT.ARROW_DOWN) {
                     handleDownButtonEvent();
                 } else if (e.keyCode == SWT.ARROW_UP) {
                     handleUpButtonEvent();
                 }
             }
         }
         
     }
 
     /**
      * This private inner class contains a new SelectionListener.
      * 
      * @author BREDEX GmbH
      * @created 13.07.2005
      */
     private class WidgetSelectionListener implements SelectionListener {
 
         /**
          * {@inheritDoc}
          */
         @SuppressWarnings("synthetic-access")
         public void widgetSelected(SelectionEvent e) {
             Object source = e.getSource();
 
             if (source.equals(m_autJreButton)) {
                 if (isRemoteRequest()) {
                     remoteBrowse(false, AutConfigConstants.JRE_BINARY,
                             m_autJreTextField, 
                                 Messages.AUTConfigComponentSelectJRE);
                 } else {
                     browseLocal(null, 
                             Messages.AUTConfigComponentSelectJRE,
                             m_autJreTextField, AutConfigConstants.JRE_BINARY);
                 }
                 return;
             } else if (source.equals(m_jarButton)) {
                 if (isRemoteRequest()) {
                     remoteBrowse(false, AutConfigConstants.JAR_FILE,
                         m_jarTextField, Messages.AUTConfigComponentSelectJAR);
                 } else {
                     browseLocal(new String[] { "*.jar" }, //$NON-NLS-1$
                             Messages.AUTConfigComponentSelectJAR,
                             m_jarTextField, AutConfigConstants.JAR_FILE);
                 }
                 return;
             } else if (source.equals(m_execButton)) {
                 if (isRemoteRequest()) {
                     handleExecButtonEventForRemote();
                 } else {
                     handleExecButtonEvent(new FileDialog(Plugin.getShell(),
                             SWT.APPLICATION_MODAL | SWT.ON_TOP));
                 }
                 return;
             } else if (source.equals(m_removeElementButton)) {
                 handleRemoveButtonEvent();
                 return;
             } else if (source.equals(m_moveElementUpButton)) {
                 handleUpButtonEvent();
                 return;
             } else if (source.equals(m_moveElementDownButton)) {
                 handleDownButtonEvent();
                 return;
             } else if (source.equals(m_editElementButton)) {
                 handleAddElementButtonEvent(true);
                 if (modifyClassNameFieldAction() != null) {
                     m_classNameTextField.setFocus();
                 }
                 return;
             } else if (source.equals(m_addElementButton)) {
                 handleAddElementButtonEvent(false);
                 if (modifyClassNameFieldAction() != null) {
                     m_classNameTextField.setFocus();
                 }
                 return;
             } else if (source.equals(m_classPathListField)) {
                 handleClassPathListEvent();
                 return;
             } else if (source.equals(m_activationMethodCombo)) {
                 handleActivationComboEvent();
                 return;
             } else if (source.equals(m_monitoringCombo)) {
                 handleMonitoringComboEvent();
                 return;
             } 
 
             Assert.notReached(Messages.EventActivatedByUnknownWidget 
                     + StringConstants.LEFT_PARENTHESES + source 
                     + StringConstants.RIGHT_PARENTHESES + StringConstants.DOT);
         }
 
         /**
          * Reacts, when an object is double clicked.
          * {@inheritDoc}
          */
         @SuppressWarnings("synthetic-access")
         public void widgetDefaultSelected(SelectionEvent e) {
             Object source = e.getSource();
             DirectoryDialog directoryDialog = new DirectoryDialog(Plugin
                     .getShell(), SWT.APPLICATION_MODAL);
             String directory;
             if (source.equals(m_classPathListField)) {
                 directoryDialog.setMessage(Messages.AUTConfigComponentEdit);
                 directoryDialog.setFilterPath(m_classPathListField
                         .getSelection()[0]);
                 int selectionIndex = m_classPathListField.getSelectionIndex();
                 directory = directoryDialog.open();
                 if (directory != null) {
                     m_classPathListField.remove(m_classPathListField
                         .getSelection()[0]);
                     m_classPathListField.add(directory, selectionIndex);
                 }
                 handleClassPathListEvent();
                 return;
             }
             Assert.notReached(Messages.EventActivatedByUnknownWidget 
                     + StringConstants.LEFT_PARENTHESES + source 
                     + StringConstants.RIGHT_PARENTHESES + StringConstants.DOT);
         }
     }
     
     /**
      * Create this dialog's advanced area component.
      * 
      * @param advancedAreaComposite Composite representing the advanced area.
      */
     protected void createAdvancedArea(Composite advancedAreaComposite) {
         // jar chooser
         initGuiJarChooser(advancedAreaComposite); 
         
         // AUT directory editor
         createAutDirectoryEditor(advancedAreaComposite);
 
         // class name editor
         UIComponentHelper.createLabel(advancedAreaComposite,
                 "AUTConfigComponent.className"); //$NON-NLS-1$ 
         m_classNameTextField = UIComponentHelper.createTextField(
                 advancedAreaComposite, 2);
         UIComponentHelper
                 .setMaxTextChars(m_classNameTextField, MAX_TEXT_LENGTH);
         m_classNameTextField.setText(StringUtils
                 .defaultString(getConfigValue(AutConfigConstants.CLASSNAME)));
         
         // class path editor
         initGuiClasspathEditor(advancedAreaComposite); 
         // parameter editor
        ControlDecorator.decorateInfo(UIComponentHelper.createLabel(
                advancedAreaComposite, "AUTConfigComponent.autArguments"), //$NON-NLS-1$
                "GDControlDecorator.AUTArguments", false); //$NON-NLS-1$
         m_autArgsTextField = 
             UIComponentHelper.createTextField(advancedAreaComposite, 2); 
         // JRE directory editor
         UIComponentHelper.createLabel(
             advancedAreaComposite, "AUTConfigComponent.jre"); //$NON-NLS-1$ 
         m_autJreTextField = 
             UIComponentHelper.createTextField(advancedAreaComposite, 1);
         UIComponentHelper.setMaxTextChars(
                 m_autJreTextField, MAX_TEXT_LENGTH);
         GridData comboGrid = new GridData(GridData.FILL, GridData.CENTER, 
             true , false, 1, 1);
         LayoutUtil.addToolTipAndMaxWidth(comboGrid, m_autJreTextField);
         m_autJreTextField.setLayoutData(comboGrid);
         ((GridData)m_autJreTextField.getLayoutData()).widthHint = 
             COMPOSITE_WIDTH;
         m_autJreComposite = 
             UIComponentHelper.createLayoutComposite(advancedAreaComposite);
         m_autJreButton = 
             new Button(m_autJreComposite, SWT.PUSH);
         m_autJreButton.setText(Messages.AUTConfigComponentBrowse);
         m_autJreButton.setLayoutData(BUTTON_LAYOUT);
 
         super.createAdvancedArea(advancedAreaComposite);
     }
 
     /**
      * Create this dialog's expert area component.
      * 
      * @param expertAreaComposite Composite representing the expert area.
      */
     protected void createExpertArea(Composite expertAreaComposite) {
         
         // JRE parameter editor
        ControlDecorator.decorateInfo(UIComponentHelper.createLabel(
                expertAreaComposite, "AUTConfigComponent.jreArguments"), //$NON-NLS-1$
                "GDControlDecorator.JREArguments", false); //$NON-NLS-1$
         m_autJreParamTextField = UIComponentHelper.createTextField(
                 expertAreaComposite, 2);
         // Environment editor
         initGuiEnvironmentEditor(expertAreaComposite);
         // activation method editor
         UIComponentHelper.createLabel(expertAreaComposite,
                 "AUTConfigComponent.activationMethod"); //$NON-NLS-1$ 
         m_activationMethodCombo = UIComponentHelper.createEnumCombo(
                 expertAreaComposite, 2,
                 "AUTConfigComponent.ActivationMethod", ActivationMethod.class); //$NON-NLS-1$
 
         UIComponentHelper.createSeparator(expertAreaComposite, 3);
         
         ControlDecorator.decorateInfo(UIComponentHelper.createLabel(
                 expertAreaComposite, "AUTConfigComponent.labelMonitoring"), //$NON-NLS-1$, 
                 "AUTConfigComponent.labelMonitoring.helpText", false); //$NON-NLS-1$
                         
         m_monitoringCombo = UIComponentHelper.createCombo(
                 expertAreaComposite, 2, 
                 MonitoringUtils.getAllRegisteredMonitoringIds(), 
                 MonitoringUtils.getAllRegisteredMonitoringNames(), true); 
         
         super.createExpertArea(expertAreaComposite);
        
     }
 
     /**
      * 
      * {@inheritDoc}
      */
     protected void openServerPrefPage() {
         super.openServerPrefPage();
         boolean checkListeners = m_selectionListener != null;
 
         if (checkListeners) {
             deinstallListeners();
         }
         if (checkListeners) {
             installListeners();
         }
     }
 
     /**
      * {@inheritDoc}
      */
     protected void createHeader(Composite parent) {
         // not needed
     }
 
     /**
      * 
      * @return a List containing all components that configure Java-related
      *         settings.
      */
     protected java.util.List<Control> getJavaRelatedFields() {
         java.util.List<Control> javaFields = new ArrayList<Control>();
         
         javaFields.add(m_autJreButton);
         javaFields.add(m_autJreTextField);
         javaFields.add(m_autJreComposite);
         javaFields.add(m_autJreParamTextField);
         javaFields.add(m_classNameTextField);
         javaFields.add(m_classPathButtonComposite);
         javaFields.add(m_addElementButton);
         javaFields.add(m_classPathListField);
         javaFields.add(m_jarButton);
         javaFields.add(m_jarTextField);
         
         return javaFields;
     }
     
     /**
      * 
      * @return the single instance of the key listener.
      */
     @SuppressWarnings("synthetic-access")
     private WidgetKeyListener getKeyListener() {
         if (m_keyListener == null) {
             m_keyListener = new WidgetKeyListener();
         }
         
         return m_keyListener;
     }
 
     /**
      * 
      * @return the single instance of the selection listener.
      */
     @SuppressWarnings("synthetic-access")
     private WidgetSelectionListener getSelectionListener() {
         if (m_selectionListener == null) {
             m_selectionListener = new WidgetSelectionListener();
         }
         
         return m_selectionListener;
     }
 
     /**
      * 
      * @return the single instance of the modify listener.
      */
     @SuppressWarnings("synthetic-access")
     private WidgetModifyListener getModifyListener() {
         if (m_modifyListener == null) {
             m_modifyListener = new WidgetModifyListener();
         }
         
         return m_modifyListener;
     }
     
     /**
      * 
      * @return the single instance of the modify listener.
      */
     @SuppressWarnings("synthetic-access")
     private WidgetFocusListener getFocusListener() {
         if (m_focusListener == null) {
             m_focusListener = new WidgetFocusListener();
         }
         
         return m_focusListener;
     }
     
     /**
      * 
      * {@inheritDoc}
      */
     protected void checkAll(java.util.List<DialogStatusParameter> paramList) {
         super.checkAll(paramList);
         addError(paramList, modifyAutConfigFieldAction());
         addError(paramList, modifyAutParamFieldAction());
         addError(paramList, modifyClassNameFieldAction());
         addError(paramList, modifyEnvFieldAction());
         addError(paramList, modifyJarFieldAction());
         addError(paramList, modifyExecTextField());
         addError(paramList, modifyJREFieldAction());
         addError(paramList, modifyJreParamFieldAction());
         addError(paramList, modifyServerComboAction());
 
     }
 
     /**
      * {@inheritDoc}
      */
     public void dispose() {
         RemoteFileBrowserBP.clearCache(); // get rid of cached directories
         super.dispose();
     }    
     /**
      * handles the button event, which was thrown by the browse button for
      * the m_autInstallDirectoryTextField       
      * @param directoryDialog The DirectoryDialog
      * @param textField The textField
      */
     private void handleBrowseDirButtonEvent(JBText textField,
             DirectoryDialog directoryDialog) {
         String directory = null;
         directoryDialog.setMessage(Messages.AUTConfigComponentSelectDir);
         File path = new File(textField.getText());
         String filterPath = Utils.getLastDirPath();
         if (path.exists()) {
             try {
                 filterPath = path.getCanonicalPath();
             } catch (IOException e) {
                 //empty
             }
         }
         directoryDialog.setFilterPath(filterPath);
         directory = directoryDialog.open();
         if (directory != null) {
             textField.setText(directory);
             Utils.storeLastDirPath(directoryDialog.getFilterPath());  
             putConfigValue(String.valueOf(textField.getData(
                     MonitoringConstants.MONITORING_KEY)), 
                     textField.getText());
         }
     }
     
     /**
      * Handles a selection of the "Browse" button,
      * corresponding to the text field in the monitoring area.
      * @param textField The text field to set the path to.
      */
     private void monitoringBrowseButtonSelected(JBText textField) {
         if (isRemoteRequest()) {
             remoteBrowse(true, 
                     String.valueOf(textField.getData(
                             MonitoringConstants.MONITORING_KEY)),
                     textField,
                     Messages.AUTConfigComponentSelectDir);
         } else {
             DirectoryDialog directoryDialog = new DirectoryDialog(
                     Plugin.getShell(), SWT.APPLICATION_MODAL
                             | SWT.ON_TOP);
             handleBrowseDirButtonEvent(textField, directoryDialog);
 
         }
     }    
 
 } 
