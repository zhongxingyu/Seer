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
 package org.eclipse.jubula.client.ui.dialogs;
 
 import java.util.List;
 
 import org.apache.commons.lang.StringUtils;
 import org.eclipse.jface.dialogs.IDialogConstants;
 import org.eclipse.jface.dialogs.TitleAreaDialog;
 import org.eclipse.jface.layout.RowLayoutFactory;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.viewers.ArrayContentProvider;
 import org.eclipse.jface.viewers.ComboViewer;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.LabelProvider;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jubula.client.core.persistence.DatabaseConnectionInfo;
 import org.eclipse.jubula.client.core.persistence.Persistor;
 import org.eclipse.jubula.client.core.preferences.database.DatabaseConnection;
 import org.eclipse.jubula.client.core.preferences.database.DatabaseConnectionConverter;
 import org.eclipse.jubula.client.ui.Plugin;
 import org.eclipse.jubula.client.ui.businessprocess.SecurePreferenceBP;
 import org.eclipse.jubula.client.ui.constants.Constants;
 import org.eclipse.jubula.client.ui.constants.ContextHelpIds;
 import org.eclipse.jubula.client.ui.constants.IconConstants;
 import org.eclipse.jubula.client.ui.i18n.Messages;
 import org.eclipse.jubula.client.ui.utils.DialogUtils;
 import org.eclipse.jubula.client.ui.utils.LayoutUtil;
 import org.eclipse.jubula.tools.constants.StringConstants;
 import org.eclipse.jubula.tools.constants.SwtAUTHierarchyConstants;
 import org.eclipse.persistence.config.PersistenceUnitProperties;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Link;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.PlatformUI;
 
 
 /**
  * Creates a pop up dialog to enter username and password for database connection.
  * @author BREDEX GmbH
  * @created 18.08.2005
  */
 public class DBLoginDialog extends TitleAreaDialog {
     
     /** number of columns = 1 */
     private static final int NUM_COLUMNS_1 = 1;
 
     /** number of columns = 2 */
     private static final int NUM_COLUMNS_3 = 3;
 
     /** vertical spacing = 2 */
     private static final int VERTICAL_SPACING = 2;
 
     /** margin width = 0 */
     private static final int MARGIN_WIDTH = 10;
 
     /** margin height = 2 */
     private static final int MARGIN_HEIGHT = 10;
 
     /** width hint = 300 */
     private static final int WIDTH_HINT = 300;
 
     /** horizontal span = 2 */
     private static final int HORIZONTAL_SPAN = 2;
     
     
     /** the username m_text field */
     private Text m_userText;
     /** the username label */
     private Label m_userLabel;
     /** the password m_text field */
     private Text m_pwdText;
     /** the password label */
     private Label m_pwdLabel;
     /** the connection combobox viewer */
     private ComboViewer m_connectionComboViewer;
     /** save database profile check box */
     private Button m_profileSave;
     /** automatic database connection check box */
     private Button m_automConn; 
     /** save check box description */
     private Link m_secureStorageLink;
     
     /** the username */
     private String m_user;
     /** the password */
     private String m_pwd;
     /** the database connection */
     private DatabaseConnection m_dbConn;
 
     /** the connections from which the user can choose */
     private List<DatabaseConnection> m_availableConnections;
     
     /** true, if selected db is embedded db */
     private boolean m_isEmbeddedOrNoSelection = false;
 
 
     /**
      * @param parentShell The parent Shell.
      */
     public DBLoginDialog(Shell parentShell) {
         super(parentShell);
     }
     
     
     /**
      * {@inheritDoc}
      */
     protected Control createDialogArea(Composite parent) {
         m_availableConnections = 
             DatabaseConnectionConverter.computeAvailableConnections();
         
         setMessage(Messages.DBLoginDialogMessage);
         setTitle(Messages.DBLoginDialogTitle);
         setTitleImage(IconConstants.DB_LOGIN_DIALOG_IMAGE);
         getShell().setText(Messages.DBLoginDialogShell);
         
         final GridLayout gridLayoutParent = new GridLayout();
         gridLayoutParent.numColumns = NUM_COLUMNS_1;
         gridLayoutParent.verticalSpacing = VERTICAL_SPACING;
         gridLayoutParent.marginWidth = MARGIN_WIDTH;
         gridLayoutParent.marginHeight = MARGIN_HEIGHT;
         parent.setLayout(gridLayoutParent);
 
         LayoutUtil.createSeparator(parent);
 
         Composite area = new Composite(parent, SWT.NONE);
         final GridLayout gridLayout = new GridLayout();
         gridLayout.numColumns = NUM_COLUMNS_3;
         area.setLayout(gridLayout);
         GridData gridData = new GridData();
         gridData.grabExcessHorizontalSpace = true;
         gridData.grabExcessVerticalSpace = true;
         gridData.horizontalAlignment = GridData.FILL;
         gridData.verticalAlignment = GridData.FILL;
         gridData.widthHint = WIDTH_HINT;
         area.setLayoutData(gridData);
 
         createSchemaCombobox(area);
         createUserTextField(area);
         createPasswordTextField(area);
         createSavePasswordCheckbox(area);
         createAutomaticConnectionCheckbox(area);
         fillConnectionCombobox();
         fillUserNameAndPasswordField();
         m_automConn.setEnabled(m_profileSave.getSelection());
         
         setUserAndPwdAndPwdCheckboxVisible(!m_isEmbeddedOrNoSelection);
         
         LayoutUtil.createSeparator(parent);
         
         PlatformUI.getWorkbench().getHelpSystem().setHelp(
                 parent, ContextHelpIds.DB_LOGIN_ID);
         setHelpAvailable(true);
         
         return area;
     }
 
     /**
      * {@inheritDoc}
      */
     public int open() {
         setMessage(Messages.DBLoginDialogMessage);
         return super.open();
     }
     
     /**
      * {@inheritDoc}
      */
     protected Button createButton(Composite parent, int id, String label, 
         boolean defaultButton) {
         
         Button button = 
             super.createButton(parent, id, label, defaultButton);
         if (m_userText.getText().length() == 0
                 || m_connectionComboViewer.getSelection().isEmpty()
                 || m_availableConnections.isEmpty()) {
             getButton(IDialogConstants.OK_ID).setEnabled(false);
         }
         return button;
     }
     
     /**
      * Creates the Textfield to select the user name.
      * @param area The parent composite. 
      */
     private void createUserTextField(Composite area) {
         new Label(area, SWT.NONE).setLayoutData(new GridData(GridData.FILL, 
             GridData.CENTER, false, false, HORIZONTAL_SPAN + 1, 1));
         m_userLabel = new Label(area, SWT.NONE);
         m_userLabel.setText(Messages.DBLoginDialogUserLabel);
         m_userLabel.setData(SwtAUTHierarchyConstants.WIDGET_NAME, "DBLoginDialog.userLabel"); //$NON-NLS-1$
         m_userText = new Text(area, SWT.BORDER);
         m_userText.setData(SwtAUTHierarchyConstants.WIDGET_NAME, "DBLoginDialog.userTxf"); //$NON-NLS-1$
         GridData gridData = newGridData();
         LayoutUtil.addToolTipAndMaxWidth(gridData, m_userText);
         m_userText.setLayoutData(gridData);
         LayoutUtil.setMaxChar(m_userText);
         IPreferenceStore store = Plugin.getDefault().getPreferenceStore();
         m_userText.setText(store.getString(Constants.USER_KEY));
         m_userText.selectAll();
         m_userText.addModifyListener(new ModifyListener() {
             public void modifyText(ModifyEvent e) {
                 if (modifyUsernameFieldAction()) {
                     modifyPasswordFieldAction();
                 }
             }            
         });
     }
     
     /**
      * Creates the m_text field to enter the password.
      * @param area The composite. 
      */
     private void createPasswordTextField(Composite area) {
         new Label(area, SWT.NONE).setLayoutData(new GridData(GridData.FILL, 
             GridData.CENTER, false, false, HORIZONTAL_SPAN + 1, 1));
         m_pwdLabel = new Label(area, SWT.NONE);
         m_pwdLabel.setText(Messages.DBLoginDialogPwdLabel);
         m_pwdLabel.setData(SwtAUTHierarchyConstants.WIDGET_NAME, "DBLoginDialog.pwdLabel"); //$NON-NLS-1$
         m_pwdText = new Text(area, SWT.PASSWORD | SWT.BORDER);
         m_pwdText.setData(SwtAUTHierarchyConstants.WIDGET_NAME, "DBLoginDialog.pwdTxf"); //$NON-NLS-1$
         GridData gridData = newGridData();
        LayoutUtil.addToolTipAndMaxWidth(gridData, m_pwdText);
         m_pwdText.setLayoutData(gridData);
         LayoutUtil.setMaxChar(m_pwdText);
         if (!StringUtils.isEmpty(m_userText.getText())) {
             m_pwdText.setFocus();
         }
         m_pwdText.addModifyListener(new ModifyListener() {
             public void modifyText(ModifyEvent e) {
                 if (modifyPasswordFieldAction()) {
                     modifyUsernameFieldAction();
                 }
             }            
         });
     }
     
     /**
      * Creates the Combobox to select the Database Schema.
      * @param area The parent composite. 
      */
     private void createSchemaCombobox(Composite area) {
         new Label(area, SWT.NONE).setLayoutData(new GridData(GridData.FILL, 
             GridData.CENTER, false, false, HORIZONTAL_SPAN + 1, 1));
         new Label(area, SWT.NONE).setText(
                 Messages.DBLoginDialogConnectionLabel);
         Combo connectionCombo = new Combo(area, SWT.DROP_DOWN | SWT.READ_ONLY);
         GridData gridData = newGridData();
         connectionCombo.setLayoutData(gridData);
         m_connectionComboViewer = new ComboViewer(connectionCombo);
         m_connectionComboViewer.setContentProvider(new ArrayContentProvider());
         m_connectionComboViewer.setLabelProvider(new LabelProvider() {
             @Override
             public String getText(Object element) {
                 if (element instanceof DatabaseConnection) {
                     return ((DatabaseConnection)element).getName();
                 }
                 return super.getText(element);
             }
         });
     }
     
     /**
      * Fills the Combobox to select the Database Schema.
      */
     private void fillConnectionCombobox() {
         m_connectionComboViewer.setInput(m_availableConnections);       
         
         if (m_availableConnections.size() == 1) {
             m_connectionComboViewer.setSelection(
                     new StructuredSelection(m_availableConnections.get(0)));
         } else {
             String previouslySelectedConn = 
                 Plugin.getDefault().getPreferenceStore().getString(
                         Constants.SCHEMA_KEY);
             for (DatabaseConnection conn : m_availableConnections) {
                 if (conn.getName().equals(previouslySelectedConn)) {
                     m_connectionComboViewer.setSelection(
                             new StructuredSelection(conn));
                     break;
                 }
             }
         }        
         
         //check if a schema is selected
         selectSchemaCbxAction();
         //if db is embedded hide textfields
         IStructuredSelection sel = 
             (IStructuredSelection)m_connectionComboViewer.getSelection();
         selectSchemaCbxAction();
         checkEmbeddedDbOrNoSchemaSelected(
                 (DatabaseConnection)sel.getFirstElement());
         
         m_connectionComboViewer
                 .addSelectionChangedListener(new ISelectionChangedListener() {
 
                     public void selectionChanged(SelectionChangedEvent event) {
                         IStructuredSelection csel = (IStructuredSelection) event
                                 .getSelection();
                         selectSchemaCbxAction();
                         checkEmbeddedDbOrNoSchemaSelected((DatabaseConnection) 
                                 csel.getFirstElement());
                         setUserAndPwdAndPwdCheckboxVisible(
                                 !m_isEmbeddedOrNoSelection);
                     }
                 });
     }
     
     /**
      * Creates the CheckBox to decide saving database password
      * @param area The parent composite
      */
     private void createSavePasswordCheckbox(Composite area) {
         new Label(area, SWT.NONE).setLayoutData(new GridData(SWT.FILL,
                 SWT.CENTER, false, false,
                 HORIZONTAL_SPAN + 1, 1));
         new Label(area, SWT.NONE).setText(StringConstants.EMPTY);
         Composite saveProfileComp = new Composite(area, SWT.NONE);
         saveProfileComp.setLayout(RowLayoutFactory.fillDefaults()
                 .spacing(0).create());
         m_profileSave = new Button(saveProfileComp, SWT.CHECK);
         m_profileSave.addSelectionListener(new SelectionListener() {
             public void widgetSelected(SelectionEvent e) {
                 m_automConn.setEnabled(m_profileSave.getSelection());
             }
             public void widgetDefaultSelected(SelectionEvent e) {
                 // do nothing
             }
         });
         m_secureStorageLink = DialogUtils
                 .createLinkToSecureStoragePreferencePage(saveProfileComp,
                         Messages.DBLoginDialogSaveDBPassword);
     }
     /**
      * Fills the username and the password field
      */
     private void fillUserNameAndPasswordField() {
         SecurePreferenceBP spBP = SecurePreferenceBP.getInstance();
         IStructuredSelection sel = 
                 (IStructuredSelection) m_connectionComboViewer
                 .getSelection();
         DatabaseConnection conn = (DatabaseConnection)sel.getFirstElement();
         if (conn != null) {
             String profileName = conn.getName();
             m_profileSave.setSelection(spBP
                     .isSaveCredentialsActive(profileName));
             if (m_profileSave.getSelection()) {
                 
                 String userName = spBP.getUserName(profileName);
                 String databasePassword = spBP.getPassword(profileName);
                 
                 m_userText.setText(userName);
                 m_pwdText.setText(databasePassword);
             } else {
                 String userName = spBP.getUserName(profileName);
                 
                 m_userText.setText(userName);
                 m_pwdText.setText(StringConstants.EMPTY);
                 
             }
             
         }
     }
 
     
     /**
      * saves the database profile with username and encrypted password
      */
     private void saveDatabaseProfile() {
         SecurePreferenceBP spBP = SecurePreferenceBP.getInstance();
         IStructuredSelection sel = 
                 (IStructuredSelection) m_connectionComboViewer
                 .getSelection();
         DatabaseConnection conn = (DatabaseConnection)sel.getFirstElement();
         String profileName = conn.getName().toString();
         
         String userName = m_userText.getText();
         
         spBP.saveProfile(profileName, userName);
     }
     
     /**
      * Creates the check box to decide automatic database connection or not
      * @param area The parent composite
      */
     private void createAutomaticConnectionCheckbox(Composite area) {
         new Label(area, SWT.NONE).setLayoutData(new GridData(SWT.FILL,
                 SWT.CENTER, false, false,
                 HORIZONTAL_SPAN + 1, 1));
         new Label(area, SWT.NONE).setText(StringConstants.EMPTY);
         m_automConn = new Button(area, SWT.CHECK);
         m_automConn.setText(Messages.DBLoginDialogAutoDbConnection);
     }
     
     /**
      * Checks whether the given connection is embedded or <code>null</code>
      * and sets internal state based on the result.
      * 
      * @param connection The connection to check. May be <code>null</code>.
      */
     private void checkEmbeddedDbOrNoSchemaSelected(
             DatabaseConnection connection) {
         
         //if no item is selected, hide user and password field
         if (connection == null) {
             m_isEmbeddedOrNoSelection = true;
             return;
         }
         
         DatabaseConnectionInfo connInfo = 
             connection.getConnectionInfo();
         String username = 
             connInfo.getProperty(PersistenceUnitProperties.JDBC_USER);
         String password = 
             connInfo.getProperty(PersistenceUnitProperties.JDBC_PASSWORD);
         if (username != null && password != null) {
             m_isEmbeddedOrNoSelection = true;
             Persistor.setUser(username);
             Persistor.setPw(password);
             m_userText.setText(username);
             m_pwdText.setText(password);
             enableOKButton(true);            
         } else {
             fillUserNameAndPasswordField();
             m_automConn.setEnabled(m_profileSave.getSelection());
             m_isEmbeddedOrNoSelection = false;
         }
     }
     
     /** 
      * set visible state of username and pwd 
      * @param visible true if user and pw should be visible, false otherwise
      */
     private void setUserAndPwdAndPwdCheckboxVisible(boolean visible) {
         m_userText.setVisible(visible);
         m_userLabel.setVisible(visible);
         m_pwdText.setVisible(visible);
         m_pwdLabel.setVisible(visible);
         m_profileSave.setVisible(visible);
         m_secureStorageLink.setVisible(visible);
         m_automConn.setVisible(visible);
     }
     
     /** 
      * The action of the user name field.
      * @return false, if the user name field contents an error:
      * the user name starts or end with a blank, or the field is empty
      */
     boolean modifyUsernameFieldAction() {
         m_userText.clearSelection();
         boolean isCorrect = true;
         int serverNameLength = m_userText.getText().length();
         if ((serverNameLength == 0)
                 || (m_userText.getText().startsWith(" ")) || //$NON-NLS-1$
             (m_userText.getText().charAt(
                 serverNameLength - 1) == ' ')) {
             isCorrect = false;
         }
         if (isCorrect) {
             setErrorMessage(null);
         } else {
             if (serverNameLength == 0) {
                 setErrorMessage(Messages.DBLoginDialogEmptyUser);
             } else {
                 setErrorMessage(Messages.DBLoginDialogWrongUser);
             }
         }
         enableOKButton(isCorrect);
         return isCorrect;
     }
     
     /** 
      * The action of the password name field.
      * @return false, if the password name field contents an error:
      * the field is empty
      */
     boolean modifyPasswordFieldAction() {
         boolean isCorrect = true;
         if ((m_pwdText.getText().startsWith(StringConstants.SPACE))
             || (m_pwdText.getText().endsWith(StringConstants.SPACE))) {
             
             isCorrect = false;
         }
         if (isCorrect) {
             setMessage(Messages.DBLoginDialogMessage); 
         } else {
             setErrorMessage(Messages.DBLoginDialogWrongPwd); 
         }
         enableOKButton(isCorrect);
         return isCorrect;
     }
     
     /** 
      * show warning if no scheme selected or available and disable ok button.
      */
     private void selectSchemaCbxAction() {
         boolean isCorrect = true;
         if (m_availableConnections.isEmpty()) {
             setErrorMessage(Messages.DBLoginDialogNoSchemaAvailable);
             isCorrect = false;
         } else if (m_connectionComboViewer.getSelection().isEmpty()) {
             setErrorMessage(Messages.DBLoginDialogNoSchemaSelected);
             isCorrect = false;
         } else {
             setErrorMessage(null); 
         }
         enableOKButton(isCorrect);
     }
 
     /**
      * enables the OK button
      * @param enabled True, if the ok button should be enabled.
      */
     void enableOKButton(boolean enabled) {
         if (getButton(IDialogConstants.OK_ID) != null) {
             getButton(IDialogConstants.OK_ID).setEnabled(enabled);
         }
     }
 
     /**
      * This method is called, when the OK button was pressed
      */
     protected void okPressed() {
         IStructuredSelection sel = 
             ((IStructuredSelection)m_connectionComboViewer.getSelection());
         m_user = m_userText.getText(); 
         m_pwd = m_pwdText.getText();
         m_dbConn = (DatabaseConnection)sel.getFirstElement();
         IPreferenceStore store = Plugin.getDefault().getPreferenceStore();
         store.setValue(Constants.USER_KEY, m_user);
         store.setValue(Constants.SCHEMA_KEY, m_dbConn.getName());
         saveDatabaseProfile();
         SecurePreferenceBP spBP = SecurePreferenceBP.getInstance();
         if (m_profileSave.getSelection()) {
             spBP.setSaveCredentialStatus(m_dbConn.getName(), true);
             String databasePassword = m_pwdText.getText();
             spBP.saveProfilePassword(m_dbConn.getName(), databasePassword);
             if (m_automConn.getSelection()) {
                 store.setValue(Constants.AUTOMATIC_DATABASE_CONNECTION_KEY,
                         m_dbConn.getName());
             } else {
                 store.setToDefault(Constants.AUTOMATIC_DATABASE_CONNECTION_KEY);
             }
         } else {
             spBP.setSaveCredentialStatus(m_dbConn.getName(), false);
             spBP.removePassword(m_dbConn.getName());
         }
         setReturnCode(OK);
         close();
     }
 
     /**
      * Creates a new GridData.
      * @return grid data
      */
     private GridData newGridData() {
         GridData gridData = new GridData();
         gridData.grabExcessHorizontalSpace = true;
         gridData.horizontalAlignment = GridData.FILL;
         gridData.horizontalSpan = HORIZONTAL_SPAN;
         return gridData;
     }
 
     /**
      * @return Returns username.
      */
     public String getUser() {
         return m_user;
     }
     
     /**
      * @return Returns the password.
      */
     public String getPwd() {
         return m_pwd;
     }
     
     /**
      * @return Returns the schema name.
      */
     public DatabaseConnection getDatabaseConnection() {
         return m_dbConn;
     }
     /**
      * @return Returns the schema combo viewer
      */
     public ComboViewer getConnectionComboViewer() {
         return m_connectionComboViewer;
     }
 }
