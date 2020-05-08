 /*******************************************************************************
  * Copyright (c) 2013 BREDEX GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     BREDEX GmbH - initial API and implementation and/or initial documentation
  *******************************************************************************/
 package org.eclipse.jubula.client.ui.rcp.widgets.autconfig;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Map;
 import org.apache.commons.lang.StringUtils;
 import org.eclipse.jubula.client.core.model.IPersistentObject;
 import org.eclipse.jubula.client.ui.rcp.Plugin;
 import org.eclipse.jubula.client.ui.rcp.businessprocess.RemoteFileBrowserBP;
 import org.eclipse.jubula.client.ui.rcp.i18n.Messages;
 import org.eclipse.jubula.client.ui.rcp.provider.ControlDecorator;
 import org.eclipse.jubula.client.ui.rcp.utils.DialogStatusParameter;
 import org.eclipse.jubula.client.ui.rcp.utils.Utils;
 import org.eclipse.jubula.client.ui.utils.LayoutUtil;
 import org.eclipse.jubula.client.ui.widgets.UIComponentHelper;
 import org.eclipse.jubula.tools.constants.AutConfigConstants;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.FileDialog;
 import org.eclipse.swt.widgets.Text;
 
 /**
  * ConfigurationArea for Win toolkit projects
  */
 public class WinAutConfigComponent extends AutConfigComponent {
     /** the number of lines in a text field */
     protected static final int COMPOSITE_WIDTH = 250;
     
     /** for check if the executable text field is empty */
     private static boolean isExecFieldEmpty = true;
        
     /** path of the executable file directory */
     private static String executablePath;
 
     /** for check if the executable text field is valid */
     private boolean m_isExecFieldValid = true;
 
     /** text field for the executable that will launch the AUT */
     private Text m_execTextField;
     
     /** browse button for the executable */
     private Button m_execButton;
 
     /** text field for the aut_args */
     private Text m_autArgsTextField;
 
     /** the WidgetModifyListener */
     private WidgetModifyListener m_modifyListener;
 
     /** the the WidgetSelectionListener */
     private WidgetSelectionListener m_selectionListener;
 
     /**
      * @param parent {@inheritDoc}
      * @param style {@inheritDoc}
      * @param autConfig data to be displayed/edited
      * @param autName the name of the AUT that will be using this configuration.
      */
     public WinAutConfigComponent(Composite parent, int style,
             Map<String, String> autConfig, String autName) {
 
         super(parent, style, autConfig, autName, false);
     }
 
     /**
      * {@inheritDoc}
      */
     protected void initState() {
         m_execTextField.setEnabled(true);
         m_execButton.setEnabled(true);
         m_autArgsTextField.setEnabled(true);
         checkLocalhostServer();
         RemoteFileBrowserBP.clearCache(); // avoid all caches
     }
 
     /**
      * {@inheritDoc}
      */
     protected boolean checkLocalhostServer() {
         boolean enable = super.checkLocalhostServer();
         boolean browseEnabled = enable || isRemoteRequest();
         m_execButton.setEnabled(browseEnabled && m_execButton.isEnabled());
         return enable;
     }
 
     /**
      * {@inheritDoc}
      */
     protected void createBasicArea(Composite basicAreaComposite) {
         super.createBasicArea(basicAreaComposite);
         // executable chooser
         UIComponentHelper.createLabel(basicAreaComposite, 
             "AUTConfigComponent.exec");  //$NON-NLS-1$
         m_execTextField = UIComponentHelper.createTextField(
                 basicAreaComposite, 1);
         LayoutUtil.setMaxChar(m_execTextField,
                 IPersistentObject.MAX_STRING_LENGTH);
         m_execButton = new Button(
                 UIComponentHelper.createLayoutComposite(basicAreaComposite),
                 SWT.PUSH);
         m_execButton.setText(Messages.AUTConfigComponentBrowse);
         m_execButton.setLayoutData(BUTTON_LAYOUT);
         m_execButton.setEnabled(Utils.isLocalhost());
 
         // parameter editor
         ControlDecorator.decorateInfo(UIComponentHelper.createLabel(
                 basicAreaComposite, "AUTConfigComponent.autArguments"), //$NON-NLS-1$
                 "ControlDecorator.AUTArguments", false); //$NON-NLS-1$
         m_autArgsTextField =
             UIComponentHelper.createTextField(basicAreaComposite, 2);
     }
 
     /**
      * Populates GUI for the basic configuration section.
      * @param data Map representing the data to use for population.
      */
     protected void populateBasicArea(Map<String, String> data) {
         super.populateBasicArea(data);
         // executable filename
         m_execTextField.setText(StringUtils.defaultString(data
                 .get(AutConfigConstants.EXECUTABLE)));
         // AUT arguments
         m_autArgsTextField.setText(StringUtils.defaultString(data.get(
              AutConfigConstants.AUT_ARGUMENTS)));
     }
 
     /**
      * Create this dialog's advanced area component.
      * @param advancedAreaComposite Composite representing the advanced area.
      */
     protected void createAdvancedArea(Composite advancedAreaComposite) {
         // unused
     }
 
     @Override
     protected void populateExpertArea(Map<String, String> data) {
         // unused
     }
 
     @Override
     protected void populateAdvancedArea(Map<String, String> data) {
         // unused
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
         final String[] filterExe = { "*.exe"}; //$NON-NLS-1$
         fileDialog.setFilterExtensions(filterExe);
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
      * Writes the path of the executable file in the AUT Working directory field.
      * @param directory The directory path of the executable file as string.
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
      * {@inheritDoc}
      */
     protected void installListeners() {
         super.installListeners();
 
         WidgetSelectionListener selectionListener = getSelectionListener();
         WidgetModifyListener modifyListener = getModifyListener();

         m_execButton.addSelectionListener(selectionListener);
         m_execTextField.addModifyListener(modifyListener);
         m_autArgsTextField.addModifyListener(modifyListener);
     }
 
     /**
      * {@inheritDoc}
      */
     protected void deinstallListeners() {
         super.deinstallListeners();
 
         WidgetSelectionListener selectionListener = getSelectionListener();
         WidgetModifyListener modifyListener = getModifyListener();
 
         m_execButton.removeSelectionListener(selectionListener);
         m_execTextField.removeModifyListener(modifyListener);
         m_autArgsTextField.removeModifyListener(modifyListener);
     }
 
     /**
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
      * @return the modifier listener.
      */
     @SuppressWarnings("synthetic-access")
     private WidgetModifyListener getModifyListener() {
         if (m_modifyListener == null) {
             m_modifyListener = new WidgetModifyListener();
         }
         return m_modifyListener;
 
     }
 
     /**
      * {@inheritDoc}
      */
     private class WidgetSelectionListener implements SelectionListener {
 
         /**
          * {@inheritDoc}
          */
         public void widgetSelected(SelectionEvent e) {
             Object source = e.getSource();
             if (source.equals(m_execButton)) {
                 //what do with remote selection ?
                 handleExecButtonEvent(new FileDialog(Plugin.getShell(),
                             SWT.APPLICATION_MODAL | SWT.ON_TOP));
                 return;
             }
         }
 
         /**
          * {@inheritDoc}
          */
         public void widgetDefaultSelected(SelectionEvent e) {
          // Do nothing
         }
     }
 
     /**
      * {@inheritDoc}
      */
     private class WidgetModifyListener implements ModifyListener {
         /**
          * {@inheritDoc}
          */
         @SuppressWarnings("synthetic-access")
         public void modifyText(ModifyEvent e) {
             Object source = e.getSource();
             if (source.equals(m_execTextField)
                     || source.equals(m_autArgsTextField)) {
                 if (source.equals(m_execTextField)) {
                     checkAll();
                 } else if (source.equals(m_autArgsTextField)) {
                     checkAll();
                 }
             } else if (source.equals(getServerCombo())) {
                 checkLocalhostServer();
                 checkAll();
             }
         }
     }
 
     /**
      * {@inheritDoc}
      */
     protected void checkAll(java.util.List<DialogStatusParameter> paramList) {
         super.checkAll(paramList);
         addError(paramList, modifyExecTextField());
         addError(paramList, modifyAutParamFieldAction());
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
                     }
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
      * The action of the AUT parameter field.
      * @return <code>null</code> if the new value is valid. Otherwise, returns
      *         a status parameter indicating the cause of the problem.
      */
     DialogStatusParameter modifyAutParamFieldAction() {
         String params = m_autArgsTextField.getText();
         putConfigValue(AutConfigConstants.AUT_ARGUMENTS, params);
         return null;
     }
 
     /**
      * Request for the NagDialog called in NewProject
      * @return true if the field for executable files is empty, false otherwise
      */
     public static boolean isExecFieldEmpty() {
         return isExecFieldEmpty;
     }
 
 }
