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
 package org.eclipse.jubula.client.ui.widgets;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.lang.StringUtils;
 import org.eclipse.core.databinding.validation.IValidator;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jface.dialogs.ErrorDialog;
 import org.eclipse.jface.dialogs.IMessageProvider;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.preference.PreferenceDialog;
 import org.eclipse.jface.window.Window;
 import org.eclipse.jubula.client.core.communication.ConnectionException;
 import org.eclipse.jubula.client.core.communication.ServerConnection;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher;
 import org.eclipse.jubula.client.ui.Plugin;
 import org.eclipse.jubula.client.ui.businessprocess.ConnectServerBP;
 import org.eclipse.jubula.client.ui.constants.Constants;
 import org.eclipse.jubula.client.ui.constants.Layout;
 import org.eclipse.jubula.client.ui.dialogs.RemoteFileBrowserDialog;
 import org.eclipse.jubula.client.ui.i18n.Messages;
 import org.eclipse.jubula.client.ui.provider.ControlDecorator;
 import org.eclipse.jubula.client.ui.utils.DialogStatusParameter;
 import org.eclipse.jubula.client.ui.utils.DialogUtils;
 import org.eclipse.jubula.client.ui.utils.RemoteFileStore;
 import org.eclipse.jubula.client.ui.utils.ServerManager;
 import org.eclipse.jubula.client.ui.utils.ServerManager.Server;
 import org.eclipse.jubula.client.ui.utils.Utils;
 import org.eclipse.jubula.tools.constants.AutConfigConstants;
 import org.eclipse.jubula.tools.constants.StringConstants;
 import org.eclipse.jubula.tools.exception.Assert;
 import org.eclipse.jubula.tools.i18n.I18n;
 import org.eclipse.jubula.tools.messagehandling.MessageIDs;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.ScrolledComposite;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.DirectoryDialog;
 import org.eclipse.swt.widgets.Widget;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.dialogs.PreferencesUtil;
 
 
 /**
  * @author BREDEX GmbH
  * @created Sep 12, 2007
  * 
  */
 public abstract class AutConfigComponent extends ScrolledComposite {
 
     /** layout for buttons */
     public static final GridData BUTTON_LAYOUT;
     /** number of columns for layout purposes */
     public static final int NUM_COLUMNS = 3;
 
     static {
         BUTTON_LAYOUT = new GridData();
         BUTTON_LAYOUT.horizontalAlignment = GridData.FILL;
         BUTTON_LAYOUT.grabExcessHorizontalSpace = true;
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
             boolean checked = false;
             if (source.equals(m_autConfigNameTextField)) {
                 checked = true;
             } else if (source.equals(m_autIdTextField)) {
                 checked = true;
             } else if (source.equals(m_autWorkingDirectoryTextField)) {
                 checked = true;
             }
             if (checked) {
                 checkAll();
                 return;
             }
             Assert.notReached(Messages.EventActivatedByUnknownWidget 
                     + StringConstants.DOT);
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
             if (source.equals(m_addServerButton)) {
                 handleAddServerButtonEvent();
                 return;
             } else if (source.equals(m_basicModeButton)
                     || source.equals(m_advancedModeButton)
                     || source.equals(m_expertModeButton)) {
                 
                 selectModeButton((Button)source);
                 return;
             } else if (source.equals(m_autWorkingDirectoryButton)) {
                 if (isRemoteRequest()) {
                     remoteBrowse(true, AutConfigConstants.WORKING_DIR,
                             m_autWorkingDirectoryTextField,
                             Messages.AUTConfigComponentSelectWorkDir);
                 } else {
                     DirectoryDialog directoryDialog = new DirectoryDialog(
                             Plugin.getShell(), SWT.APPLICATION_MODAL
                                     | SWT.ON_TOP);
                     handleWorkDirButtonEvent(directoryDialog);
 
                 }
                 return;
             }
             Assert.notReached(Messages.EventActivatedByUnknownWidget 
                     + StringConstants.LEFT_PARENTHESES + source 
                     + StringConstants.RIGHT_PARENTHESES + StringConstants.DOT);
         }
         /**
          * {@inheritDoc}
          */
         public void widgetDefaultSelected(SelectionEvent e) {
             // nothing
         }
     }
     
     /**
      * Possible modes for the dialog
      *
      * @author BREDEX GmbH
      * @created Sep 10, 2007
      */
     public static enum Mode {
         /** basic mode */
         BASIC,
         /** advanced mode */
         ADVANCED, 
         /** expert mode */
         EXPERT
     }
     
     /** the current mode */
     private Mode m_mode;
     /** mapping from mode buttons to modes */
     private Map<Button, Mode> m_buttonToModeMap = new HashMap<Button, Mode>();
 
     /** name of the AUT that will be using this configuration */
     private String m_autName;
     /** if the name of the aut config has been modified from its default */
     private boolean m_nameModified;
     /** The IAUTConfigPO this component displays/edits */
     private Map<String, String> m_autConfig;
 
     /** list of the stored servers */
     private ServerManager m_listOfServers = ServerManager.getInstance();
     /** parameter list for callback method of the <code>IDialogStatusListener</code> */
     private java.util.List<DialogStatusParameter> m_paramList = 
         new ArrayList<DialogStatusParameter>();
 
     /** the the WidgetSelectionListener */
     private WidgetSelectionListener m_selectionListener;
     /** the WidgetModifyListener */
     private WidgetModifyListener m_modifyListener;
 
     /** Composite representing the basic area */
     private Composite m_basicAreaComposite;
     /** Composite representing the advanced area */
     private Composite m_advancedAreaComposite;
     /** Composite representing the expert area */
     private Composite m_expertAreaComposite;
     /** Composite representing the monitoring area */
     private Composite m_monitoringAreaComposite;
     /** Composite holding the entire contents of this config component */
     private Composite m_contentComposite;
     /** gui component */
     private JBText m_autConfigNameTextField;
     /** gui component */
     private Combo m_serverCombo;
     /** gui component */
     private Button m_addServerButton;
     /** gui component */
     private JBText m_autWorkingDirectoryTextField;
     /** gui component */
     private Button m_autWorkingDirectoryButton;
     /** gui component */
     private JBText m_autIdTextField;
     /** validator for the AUT ID text field */
     private IValidator m_autIdValidator;
 
     /** The basic mode button. */
     private Button m_basicModeButton;
     /** The advanced button. */
     private Button m_advancedModeButton;
     /** The expert mode button. */
     private Button m_expertModeButton;
     /** Whether the AUT config component supports multiple modes */
     private boolean m_isMultiMode;
     /** Whether this object has finished initializing */
     private boolean m_isinitialized;
     
     /**
      * @param parent {@inheritDoc}
      * @param style {@inheritDoc}
      * @param autConfig data to be displayed/edited
      * @param autName the name of the AUT that will be using this configuration.
      * @param isMultiMode whether this component supports multiple modes.
      */
     public AutConfigComponent(Composite parent, int style,
         Map<String, String> autConfig, String autName, boolean isMultiMode) {
         
         super(parent, style);
         m_isinitialized = false;
         m_autConfig = autConfig;
         m_autName = autName;
         m_nameModified = !isDataNew(autConfig) 
             && autConfig.get(AutConfigConstants.CONFIG_NAME).equals(
                 NLS.bind(Messages.AUTConfigComponentDefaultAUTConfigName, 
                         new String [] {
                             autName, 
                             autConfig.get(AutConfigConstants.SERVER)
                         }));
         m_isMultiMode = isMultiMode;
         if (m_isMultiMode) {
             IPreferenceStore prefStore = PlatformUI.getPreferenceStore();
             String prefMode = 
                 prefStore.getString(Constants.AUT_CONFIG_DIALOG_MODE);
             if (prefMode != null && prefMode.length() != 0) {
                 m_mode = Mode.valueOf(prefMode);
             } else {
                 m_mode = Mode.BASIC;
             }
         }
         createGUI();
         populateGUI(autConfig);
         init();
 
         if (m_isMultiMode) {
             setCurrentMode(m_mode);
             getDisplay().asyncExec(new Runnable() {
                 public void run() {
                     // Since we're resizing after everything is initialized,
                     // it's possible that the dialog is already disposed here.
                     if (!isDisposed()) {
                         resize();
                         getShell().pack(true);
                     }
                 }
             });
         } else {
             setCompositeVisible(m_basicAreaComposite, true);
             resize();
         }
         
         m_isinitialized = true;
     }
     
     /**
      * transmits that a Java toolkit is to be used
      * @return true if Java toolkit is used
      */
     protected boolean isJavaAut() {
         return false;
     }
     
     /**
      * @return the text field for the AUT working directory
      */
     protected JBText getAutWorkingDirField() {
         return m_autWorkingDirectoryTextField;
     }
     
     /**
      * @return the monitoringAreaComposite
      */
     public Composite getMonitoringAreaComposite() {
         return m_monitoringAreaComposite;
     }
     
     /**
      * Populates all areas with data from the given map.
      * 
      * @param data The data to use for population.
      */
     private void populateGUI(Map<String, String> data) {
         populateBasicArea(data);
         populateAdvancedArea(data);
         populateExpertArea(data);
         populateMonitoringArea(data);
     }
 
     /**
      * {@inheritDoc}
      */
     public void setVisible(boolean visible) {
         if (StringConstants.EMPTY.equals(m_autConfigNameTextField.getText())) {
             m_autConfigNameTextField.setFocus();
             m_autConfigNameTextField.setText(
                 NLS.bind(Messages.AUTConfigComponentDefaultAUTConfigName,
                     new String [] {
                         m_autName, 
                         m_serverCombo.getText()
                     }));
             m_autConfigNameTextField.setSelection(0, 
                 m_autConfigNameTextField.getText().length());
         }
     }
 
     /**
      * @return the AUT-Config-Map
      */
     private Map<String, String> getAutConfig() {
         return m_autConfig;
     }
 
     /** initialize the GUI components */
     private void createGUI() {
         m_contentComposite = new Composite(this, SWT.NONE);
         initGuiLayout(m_contentComposite);
         createHeader(m_contentComposite);
         if (m_isMultiMode) {
             createModeButtons(m_contentComposite);
         }
         m_basicAreaComposite = new Composite(m_contentComposite, SWT.NONE);
         m_advancedAreaComposite = new Composite(m_contentComposite, SWT.NONE);
         m_expertAreaComposite = new Composite(m_contentComposite, SWT.NONE);
         m_monitoringAreaComposite = new Composite(
                 m_contentComposite, SWT.NONE);
         
         createLayout(m_basicAreaComposite);
         createLayout(m_advancedAreaComposite);
         createLayout(m_expertAreaComposite);
         createLayout(m_monitoringAreaComposite);
         
         createBasicArea(m_basicAreaComposite);
         createAdvancedArea(m_advancedAreaComposite);
         createExpertArea(m_expertAreaComposite);
         createMonitoringArea(m_monitoringAreaComposite);        
         
         setContent(m_contentComposite);
 
     }
 
     /**
      * @param areaComposite The composite for which to create and set a layout.
      */
     private void createLayout(Composite areaComposite) {
         areaComposite.setLayout(
             Layout.createDefaultGridLayout(NUM_COLUMNS));
         GridData gridData = new GridData(GridData.BEGINNING);
         gridData.horizontalSpan = NUM_COLUMNS;
         areaComposite.setLayoutData(gridData);
     }
 
     /**
      * Creates the header area (above the mode buttons).
      * 
      * @param parent The parent composite
      */
     protected abstract void createHeader(Composite parent);
 
     /**
      * Populates GUI for the advanced configuration section.
      * 
      * @param data Map representing the data to use for population.
      */
     protected abstract void populateExpertArea(Map<String, String> data);
 
     /**
      * Populates GUI for the advanced configuration section. Subclasses may
      * override this empty implementation.
      * 
      * @param data
      *            Map representing the data to use for population.
      */
     protected void populateMonitoringArea(Map<String, String> data)  {
         // by default do nothing 
     }
      
     /**
      * Populates GUI for the advanced configuration section.
      * 
      * @param data Map representing the data to use for population.
      */
     protected abstract void populateAdvancedArea(Map<String, String> data);
 
     /**
      * Populates GUI for the basic configuration section.
      * 
      * @param data Map representing the data to use for population.
      */
     protected void populateBasicArea(Map<String, String> data) {
         fillServerCombo();
         if (!isDataNew(data)) {
             m_serverCombo.select(m_serverCombo.indexOf(StringUtils
                     .defaultString(data.get(AutConfigConstants.SERVER))));
             m_autConfigNameTextField.setText(StringUtils.defaultString(
                 data.get(AutConfigConstants.CONFIG_NAME),
                     NLS.bind(Messages.AUTConfigComponentDefaultAUTConfigName, 
                         new String [] {
                             m_autName, 
                             m_serverCombo.getText()
                         })));
             m_autIdTextField.setText(StringUtils.defaultString(
                     data.get(AutConfigConstants.AUT_ID)));
             m_autWorkingDirectoryTextField.setText(StringUtils
                     .defaultString(data.get(AutConfigConstants.WORKING_DIR)));
         } else {
             // set some default values
             m_serverCombo.select(m_serverCombo.indexOf(StringUtils
                 .defaultString(Constants.LOCALHOST1)));
 
             m_autConfigNameTextField.setText(
                 NLS.bind(Messages.AUTConfigComponentDefaultAUTConfigName, 
                     new String [] {
                         m_autName, 
                         m_serverCombo.getText()
                     }));
         }
     }
 
     /**
      * @param data The map to check.
      * @return <code>true</code> if the given map is newly created and defaults
      *         should be used. Otherwise, <code>false</code>.
      */
     protected boolean isDataNew(Map<String, String> data) {
         return data == null || data.isEmpty();
     }
 
     /**
      * @param basicAreaComposite The composite that represents the basic area.
      */
     protected void createBasicArea(Composite basicAreaComposite) {
         initGUIConfigAndServer(basicAreaComposite);
     }
     
     /**
      * Create this dialog's advanced area component.
      * 
      * @param advancedAreaComposite Composite representing the advanced area.
      */
     protected void createAdvancedArea(Composite advancedAreaComposite) {
         setCompositeVisible(advancedAreaComposite, false);
     }
 
     /**
      * Create this dialog's expert area component.
      * 
      * @param expertAreaComposite Composite representing the expert area.
      */
     protected void createExpertArea(Composite expertAreaComposite) {
         setCompositeVisible(expertAreaComposite, false);
        
     }
     /**
      * Create this dialog's monitoring area component  
      * @param monitoringComposite Compostie representing the ccArea
      * 
      */
     protected void createMonitoringArea(Composite monitoringComposite) {
         setCompositeVisible(monitoringComposite, true);
     }
     /**
      * Inits the AUT-Configuration and Server area.
      * 
      * @param parent The parent Composite.
      */
     private void initGUIConfigAndServer(Composite parent) {
         // name property
         UIComponentHelper.createLabel(parent, "AUTConfigComponent.configName"); //$NON-NLS-1$ 
         m_autConfigNameTextField = UIComponentHelper.createTextField(parent, 2);
         
         // server chooser
         initGuiServerChooser(parent);
 
         // AUT ID field
         ControlDecorator.decorateInfo(
             UIComponentHelper.createLabel(parent, "AUTConfigComponent.autId"), //$NON-NLS-1$, 
             "AUTConfigComponent.autId.helpText", false); //$NON-NLS-1$
          
         m_autIdTextField = UIComponentHelper.createTextField(parent, 2);
         
         UIComponentHelper.createSeparator(parent, NUM_COLUMNS);
         
         // AUT directory editor
         if (!isJavaAut()) {
             createAutDirectoryEditor(parent);
         }
     }
 
     /**
      * Inits the AUT working dir area.
      * 
      * @param parent The parent Composite.
      */
     protected void createAutDirectoryEditor(Composite parent) {
         
         UIComponentHelper.createLabel(parent, "AUTConfigComponent.workDir"); //$NON-NLS-1$ 
         m_autWorkingDirectoryTextField = UIComponentHelper.createTextField(
             parent, 1);
         
         m_autWorkingDirectoryButton = new Button(UIComponentHelper
                 .createLayoutComposite(parent), SWT.PUSH);
         m_autWorkingDirectoryButton.setText(Messages.AUTConfigComponentBrowse);
         m_autWorkingDirectoryButton.setLayoutData(BUTTON_LAYOUT);
     }
     
     
     /**
      * 
      * @param parent The parent Composite.
      */
     private void initGuiServerChooser(Composite parent) {
         UIComponentHelper.createLabel(parent, "AUTConfigComponent.server"); //$NON-NLS-1$ 
         m_serverCombo = new Combo(parent, SWT.READ_ONLY);
         GridData comboGrid = new GridData(GridData.FILL, GridData.CENTER, true,
             false, 1, 1);
         Layout.addToolTipAndMaxWidth(comboGrid, m_serverCombo);
         m_serverCombo.setLayoutData(comboGrid);
         m_addServerButton = new Button(UIComponentHelper
             .createLayoutComposite(parent), SWT.PUSH);
         m_addServerButton.setText(Messages.AUTConfigComponentAddServer);
         m_addServerButton.setLayoutData(BUTTON_LAYOUT);
     }
 
     /**
      * Sets the current mode for the dialog (ex. basic, advanced, expert).
      * 
      * @param mode Button representing the mode to activate.
      */
     private void setCurrentMode(Mode mode) {
         m_basicModeButton.setSelection(false);
         m_advancedModeButton.setSelection(false);
         m_expertModeButton.setSelection(false);
         for (Button key : m_buttonToModeMap.keySet()) {
             if (m_buttonToModeMap.get(key) == mode) {
                 key.setSelection(true);
                 break;
             }
         }
         IPreferenceStore prefStore = PlatformUI.getPreferenceStore();
         if (mode == Mode.BASIC) {
             prefStore.setValue(
                 Constants.AUT_CONFIG_DIALOG_MODE, Mode.BASIC.name());
             setCompositeVisible(m_advancedAreaComposite, false);
             setCompositeVisible(m_expertAreaComposite, false);
             setCompositeVisible(m_monitoringAreaComposite, false);
         } else  if (mode == Mode.ADVANCED) {
             prefStore.setValue(
                 Constants.AUT_CONFIG_DIALOG_MODE, Mode.ADVANCED.name());
             setCompositeVisible(m_advancedAreaComposite, true);
             setCompositeVisible(m_expertAreaComposite, false);
             setCompositeVisible(m_monitoringAreaComposite, false);
         } else if (mode == Mode.EXPERT) {
             prefStore.setValue(
                 Constants.AUT_CONFIG_DIALOG_MODE, Mode.EXPERT.name());
             setCompositeVisible(m_advancedAreaComposite, true);
             setCompositeVisible(m_expertAreaComposite, true);
             setCompositeVisible(m_monitoringAreaComposite, true);
         }
         resize();
         getShell().pack(true);
     }
     
     /**
      * checks if BASIC mode is selected
      * @return mode == BASIC
      */
     protected boolean isBasicMode() {
         IPreferenceStore prefStore = PlatformUI.getPreferenceStore();
         String mode = prefStore.getString(Constants.AUT_CONFIG_DIALOG_MODE);
         return mode.equals(Mode.BASIC.name());
     }
 
     /**
      * Resizes the content composite based on added/removed components.
      */
     protected void resize() {
         Point newSize = 
             m_contentComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT);
         m_contentComposite.setSize(newSize);
         m_contentComposite.layout();
     }
 
     /**
      * Sets the visibility of the given composite and adjusts the parent
      * layout as necessary. Assumes that the composite is part of a grid layout.
      * 
      * @param composite The composite for which to set the visibility.
      * @param visible Whether the composite should be made visible or invisible.
      */
     private void setCompositeVisible(Composite composite, boolean visible) {
         composite.setVisible(visible);
         ((GridData)composite.getLayoutData()).exclude = !visible;
     }
 
     /**
      * set the layout for the component
      * @param comp the composite to set the layout for
      */
     private void initGuiLayout(Composite comp) {
         GridLayout compositeLayout = new GridLayout();
         compositeLayout.numColumns = NUM_COLUMNS;
         compositeLayout.horizontalSpacing = Layout.SMALL_HORIZONTAL_SPACING;
         compositeLayout.verticalSpacing = Layout.SMALL_VERTICAL_SPACING;
         compositeLayout.marginHeight = Layout.SMALL_MARGIN_HEIGHT;
         compositeLayout.marginWidth = Layout.SMALL_MARGIN_WIDTH;
         comp.setLayout(compositeLayout);
         GridData compositeData = new GridData(SWT.FILL, SWT.FILL, true, true);
         compositeData.grabExcessHorizontalSpace = false;
         compositeData.grabExcessVerticalSpace = true;
         comp.setLayoutData(compositeData);
     }
 
     /**
      * Fills the server combo box.
      */
     private void fillServerCombo() {
         boolean checkListeners = m_selectionListener != null;
         if (checkListeners) {
             deinstallListeners();
         }
         
         m_serverCombo.removeAll();
         String currentlySelectedServer = 
             getConfigValue(AutConfigConstants.SERVER);
         if (currentlySelectedServer != null 
             && currentlySelectedServer.trim().length() != 0
             && !m_listOfServers.getServerNames().contains(
                 currentlySelectedServer)) {
 
             String defaultServerInfo = Plugin.getDefault().getPreferenceStore()
                 .getDefaultString(Constants.SERVER_SETTINGS_KEY);
             String defaultServerPort = 
                 defaultServerInfo.substring(defaultServerInfo.indexOf(
                         StringConstants.COLON) + 1);
             m_listOfServers.addServer(
                 new ServerManager.Server(
                     currentlySelectedServer, 
                     Integer.valueOf(defaultServerPort)));
 
             Utils.createMessageDialog(MessageIDs.I_SERVER_NAME_ADDED, null, 
                 new String [] {Messages.ServerName + StringConstants.COLON 
                         + StringConstants.SPACE + currentlySelectedServer 
                         + StringConstants.NEWLINE + Messages.ServerPortDefault 
                         + defaultServerPort});
         }
         for (String serverName : m_listOfServers.getServerNames()) {
             m_serverCombo.add(serverName);
         }
 
         if (checkListeners) {
             installListeners();
         }
     }
     
     /**
      * Inits the buttons that control the mode (basic, advanced, expert).
      * 
      * @param parent The parent Composite.
      */
     protected void createModeButtons(Composite parent) {
         Composite modeButtons = 
             UIComponentHelper.createLayoutComposite(parent, 3);
         GridData modeButtonsData = new GridData(GridData.BEGINNING);
         modeButtonsData.horizontalSpan = NUM_COLUMNS;
         modeButtons.setLayoutData(modeButtonsData);
         
         m_basicModeButton = new Button(modeButtons, SWT.TOGGLE);
         m_basicModeButton.setText(Messages.AUTConfigComponentShowBasic);
         m_basicModeButton.setSelection(true);
         m_buttonToModeMap.put(m_basicModeButton, Mode.BASIC);
 
         m_advancedModeButton = new Button(modeButtons, SWT.TOGGLE);
         m_advancedModeButton.setText(Messages.AUTConfigComponentShowAdvanced);
         m_buttonToModeMap.put(m_advancedModeButton, Mode.ADVANCED);
         
         m_expertModeButton = new Button(modeButtons, SWT.TOGGLE);
         m_expertModeButton.setText(Messages.AUTConfigComponentShowExpert);
         m_buttonToModeMap.put(m_expertModeButton, Mode.EXPERT);
     }
 
     /**
      * installs all listeners to the gui components. All components visualizing
      * a property do have some sort of modification listeners which store
      * edited data in the edited instance. Some gui components have additional
      * listeners for data validatuion or permission reevaluation.
      */
     protected void installListeners() {
         WidgetSelectionListener selectionListener = getSelectionListener();
         WidgetModifyListener modifyListener = getModifyListener();
         if (m_isMultiMode) {
             m_basicModeButton.addSelectionListener(selectionListener);
             m_advancedModeButton.addSelectionListener(selectionListener);
             m_expertModeButton.addSelectionListener(selectionListener);
         }
         m_addServerButton.addSelectionListener(selectionListener);
         m_autConfigNameTextField.addModifyListener(modifyListener);        
         m_autIdTextField.addModifyListener(modifyListener);
         
         m_autWorkingDirectoryButton.addSelectionListener(selectionListener);
         m_autWorkingDirectoryTextField.addModifyListener(modifyListener);
         
     }
     
     /**
      * deinstalls all listeners to the gui components. All components visualizing
      * a property do have some sort of modification listeners which store
      * edited data in the edited instance. Some gui components have additional
      * listeners for data validatuion or permission reevaluation.
      */
     protected void deinstallListeners() {
         WidgetSelectionListener selectionListener = getSelectionListener();
         WidgetModifyListener modifyListener = getModifyListener();
         if (m_isMultiMode) {
             m_basicModeButton.removeSelectionListener(selectionListener);
             m_advancedModeButton.removeSelectionListener(selectionListener);
             m_expertModeButton.removeSelectionListener(selectionListener);
         }
         m_addServerButton.removeSelectionListener(selectionListener);
         m_autConfigNameTextField.removeModifyListener(modifyListener);
         m_autIdTextField.removeModifyListener(modifyListener);
         m_autWorkingDirectoryButton.removeSelectionListener(
             selectionListener);        
         m_autWorkingDirectoryTextField.removeModifyListener(modifyListener);
         
     }
 
     /**
      * Maps the given value to the given key for the AUT Configuration. If
      * the given key already has a value mapped to it, this value will be 
      * overwritten.
      *  
      * Updates the enablement of all fields based on the new status of 
      * the AUT Configuration. Subclasses should extend this method to update the
      * fields that they define.
      * 
      * @param key The key for the mapping.
      * @param value The value for the mapping.
      * @return <code>true</code> if the configuration was changed as a result 
      *         of this method call (i.e. if <code>value</code> was <em>not</em>
      *         already mapped to <code>key</code>). Otherwise 
      *         <code>false</code>. Note that there is no need to update fields
      *         if the mapping has not changed.
      */
     protected boolean putConfigValue(String key, String value) {
 
         String previousValue = 
             StringUtils.defaultString(getAutConfig().put(key, value));
         boolean wasEmpty = previousValue.length() == 0; 
         boolean isEmpty = StringUtils.defaultString(value).length() == 0;
         boolean areBothEmpty = wasEmpty && isEmpty;
         
         if (isEmpty) {
             getAutConfig().remove(key);
         }
         
         return (!m_isinitialized && !areBothEmpty) 
             || !value.equals(previousValue);
     }
     
     /**
      * 
      * @param key The key being searched for in the AUT Configuration.
      * @return The value mapped to <code>key</code> in the AUT Configuration.
      */
     protected String getConfigValue(String key) {
         final String value = getAutConfig().get(key);
         return value != null ? value : StringConstants.EMPTY;
     }
     
     /**
      * Overwrites the AUT Configuration with keys and values from the given
      * map.
      * 
      * @param newConfig The new configuration data.
      */
     protected void setConfig(Map<String, String> newConfig) {
         Utils.makeAutConfigCopy(newConfig, getAutConfig());
     }
 
     /**
      * Checks whether the currently selected server is localhost. Subclasses
      * may extend this method to enable/disable browse buttons.
      * @return <code>true</code>, if the currently selected server is localhost
      */
     protected boolean checkLocalhostServer() {
         boolean enable = isLocalhost(); 
         
         m_autWorkingDirectoryButton.setEnabled(enable || isRemoteRequest());
 
         return enable;
     }
 
     /**
      * @return if the current aut starter is localhost
      */
     private boolean isLocalhost() {
         boolean enable;
         try {
             final String serverComboText = getServerCombo().getText();
             final InetAddress localHost = InetAddress.getLocalHost();
             final String canonicalHostName = localHost.getCanonicalHostName();
             enable = (Constants.LOCALHOST1.equals(serverComboText.toLowerCase())
                 || Constants.LOCALHOST2.equals(serverComboText)
                 || localHost.getHostName().equals(serverComboText) 
                 || localHost.getHostAddress().equals(serverComboText)
                 || (canonicalHostName != null
                     && canonicalHostName.equals(serverComboText)));
         } catch (UnknownHostException e) {
             enable = false;
         }
         return enable;
     }
     /**
      * Handles the button event.
      */
     public void handleAddServerButtonEvent() {
         openServerPrefPage();
     }
     
     /** 
      * The action of the AUT config name field.
      * @return <code>null</code> if the new value is valid. Otherwise, returns
      *         a status parameter indicating the cause of the problem.
      */
     public DialogStatusParameter modifyAutConfigFieldAction() {
         m_nameModified = !m_autConfigNameTextField.getText().equals(
             NLS.bind(Messages.AUTConfigComponentDefaultAUTConfigName,
                 new String [] {
                     m_autName, 
                     m_serverCombo.getText()
                 }));
         DialogStatusParameter error = null;
         
         putConfigValue(AutConfigConstants.CONFIG_NAME, 
                 m_autConfigNameTextField.getText());
 
         if (!isValid(m_autConfigNameTextField, false)) {
             if (m_autConfigNameTextField.getText().length() == 0) {
                 error = createErrorStatus(
                         Messages.AUTConfigComponentEmptyAUTConfigName);
             } else {
                 error = createErrorStatus(
                         Messages.AUTConfigComponentWrongAUTConfigName);
             }
         }
         return error;
     }
     
     /** 
      * The action of the AUT ID field.
      * @return <code>null</code> if the new value is valid. Otherwise, returns
      *         a status parameter indicating the cause of the problem.
      */
     public DialogStatusParameter modifyAutIdFieldAction() {
         DialogStatusParameter error = null;
         String newAutIdValue = m_autIdTextField.getText();
 
         // FIXME zeb This null check is necessary at the moment because the creator 
         //           of AutConfigComponents is in the ToolkitSupport project, which 
         //           is not aware of model (PO) objects nor of databinding classes 
         //           (IValidator). This dependency issue should be resolved, and
         //           the validator should be set in the constructor, rather than
         //           in a separate setter method.
         if (m_autIdValidator != null) {
             IStatus validationStatus = m_autIdValidator.validate(newAutIdValue);
             if (!validationStatus.isOK()) {
                 if (validationStatus.getSeverity() == IStatus.ERROR) {
                     error = createErrorStatus(validationStatus.getMessage());
                 } else {
                     error = createWarningStatus(validationStatus.getMessage());
                 }
             }
         }
         putConfigValue(AutConfigConstants.AUT_ID, newAutIdValue);
         
         return error;
     }
     
     /**
      * Opens the server preference page
      */
     protected void openServerPrefPage() {
         PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(
                getShell(), Constants.JB_PREF_PAGE_AUTAGENT, null, null);
         DialogUtils.setWidgetNameForModalDialog(dialog);
         dialog.open();
         m_listOfServers = ServerManager.getInstance();
         String oldServer = m_serverCombo.getText();
         fillServerCombo();
         m_serverCombo.setText(oldServer);
     }
     
     /**
      * 
      * @return The server selection combo box.
      */
     protected Combo getServerCombo() {
         return m_serverCombo;
     }
 
     /**
      * 
      * @return The server list.
      */
     protected ServerManager getServerList() {
         return m_listOfServers;
     }
 
     /**
      * @param modifiedWidget The modified widget.
      * @param emptyAllowed true, if an empty string is allowed as input.
      * @return True, if the input of the widget is correct.False, otherwise.
      */
     protected boolean isValid(Widget modifiedWidget, boolean emptyAllowed) {
         if (modifiedWidget instanceof DirectCombo) {
             DirectCombo combo = (DirectCombo)modifiedWidget;
             int textLength = combo.getText().length();
             String text = combo.getText();
             return checkTextInput(emptyAllowed, textLength, text);
         } 
         if (modifiedWidget instanceof JBText) {
             JBText textField = (JBText)modifiedWidget;
             int textLength = textField.getText().length();
             String text = textField.getText();
             return checkTextInput(emptyAllowed, textLength, text);
         }
         return true;
     }
 
     /**
      * @return true if the aut config is for the currently connected AUT starter
      */
     protected boolean isRemoteRequest() {        
         boolean enable;
         try {
             final Server currentServer = 
                 ConnectServerBP.getInstance().getCurrentServer();
             if (currentServer == null) {
                 return false;
             }
             if (isLocalhost()) {
                 return false;
             }
 
             final String serverName = getServerCombo().getText().toLowerCase();
             final InetAddress serverAddress = InetAddress.getByName(serverName);
             final String canonicalServerName = 
                 serverAddress.getCanonicalHostName().toLowerCase();
             
             final String currentServerName = 
                 currentServer.getName().toLowerCase();
             final InetAddress currentServerAddress = 
                 InetAddress.getByName(currentServerName);
             final String canonicalCurrentServerName = 
                 currentServerAddress.getCanonicalHostName().toLowerCase();
             
             
             
             enable = currentServerName.equals(serverName)
                     || currentServerAddress.equals(serverAddress)
                     || canonicalCurrentServerName.equals(canonicalServerName);
         } catch (UnknownHostException e) {
             enable = false;
         }
         
         return enable;
     }
 
 
     /**
      * @param emptyAllowed true, if an empty string is allowed as input.
      * @param textLength the text length
      * @param text the text input
      * @return true, if text input is validated successful
      */
     private boolean checkTextInput(boolean emptyAllowed, int textLength, 
         String text) {
         
         if ((textLength == 0 && !emptyAllowed) 
             || (text.startsWith(" ") || text.endsWith(" "))) { //$NON-NLS-1$ //$NON-NLS-2$     
             return false;
         }
         return true;
     }
     
     /**
      * Notifies the DialogStatusListenerManager about the text input state
      */
     private void fireError() {
         DataEventDispatcher.getInstance().getDialogStatusListenerMgr()
             .fireNotification(m_paramList);
     }
 
     /**
      * Notifies the DialogStatusListenerManager about the text input state
      * @param message The message for the new status.
      * @return the new status parameter.
      */
     protected DialogStatusParameter createWarningStatus(String message) {
         DialogStatusParameter param = new DialogStatusParameter();
         param.setButtonState(true);
         param.setStatusType(IMessageProvider.WARNING);
         param.setMessage(message);
         return param;
     }
     
     /**
      * Notifies the DialogStatusListenerManager about the text input state
      * @param message The message for the new status.
      * @return the new status parameter.
      */
     protected DialogStatusParameter createErrorStatus(String message) {
         DialogStatusParameter param = new DialogStatusParameter();
         param.setButtonState(false);
         param.setStatusType(IMessageProvider.ERROR);
         param.setMessage(message);
         return param;
     }
 
     /**
      * Sets no error text / image.
      * @param noEntries true if there are neither errors nor warnings
      */
     private void setIsValid(boolean noEntries) {
         if (noEntries) {
             DialogStatusParameter param = new DialogStatusParameter();
             param.setButtonState(true);
             param.setStatusType(IMessageProvider.NONE);
             param.setMessage(Messages.ProjectWizardAUTData);
             m_paramList.clear();
             m_paramList.add(param);
         }
         DataEventDispatcher.getInstance().getDialogStatusListenerMgr()
             .fireNotification(m_paramList);
     }
 
     /**
      * @return <code>true</code> if the Aut Config is newly created and defaults
      *         should be used. Otherwise, <code>false</code>.
      */
     protected boolean isConfigNew() {
         return m_autConfig == null || m_autConfig.isEmpty();
     }
 
     /**
      * Enables the given mode button, deselecting all other mode buttons.
      * 
      * @param button The mode button to select.
      */
     private void selectModeButton(Button button) {
         setCurrentMode(m_buttonToModeMap.get(button));
     }
 
     /**
      * Inits the state, installs all needed listeners, and performs an initial
      * check for validity of all fields.
      */
     protected void init() {
         initState();
         installListeners();
         checkAll(m_paramList);
     }
 
     /**
      * Checks validity of all fields.
      */
     public final void checkAll() {        
         checkAll(m_paramList);
         if (m_paramList.isEmpty()) {
             setIsValid(true);
         } else {
             boolean isValid = true;
             for (DialogStatusParameter entry : m_paramList) {
                 if (entry.getStatusType() == IMessageProvider.ERROR) {
                     isValid = false;
                     break;
                 }
             }
             if (isValid) {
                 setIsValid(false);                
             } else {
                 fireError();
             }
         }
         m_paramList.clear();
     }
     
     /**
      * Checks validity of all fields. Subclasses should extend this method to
      * validate their added fields.
      * 
      * @param paramList A list to which status messages (errors) can be added.
      */
     protected void checkAll(java.util.List<DialogStatusParameter> paramList) {
         addError(paramList, modifyAutConfigFieldAction());
         addError(paramList, modifyAutIdFieldAction());
         addError(paramList, modifyServerComboAction());
         addError(paramList, modifyWorkingDirFieldAction());        
     }
 
     /**
      * Adds the given error to the list, if the error exists.
      * 
      * @param paramList The list to which the error will be added.
      * @param error The error to be added. If this value is <code>null</code>,
      *              the list is not changed.
      */
     protected void addError(List<DialogStatusParameter> paramList, 
         DialogStatusParameter error) {
         
         if (error != null) {
             paramList.add(error);
         }
     }
     /**
      * 
      * @param error Adds an {@link DialogStatusParameter} to the Error list.
      */
     protected void addError(DialogStatusParameter error) {
 
         if (error != null) {
             m_paramList.add(error);
         }
     }
 
     /** 
      * Sets initial editable state for components 
      */
     protected void initState() {
         m_autConfigNameTextField.setEnabled(true);
         m_serverCombo.setEnabled(true);
     }
     
     /** Handles the server-list event. 
      * @return False, if the server name combo box contents an error.
      */
     public DialogStatusParameter modifyServerComboAction() {
         boolean isValid = false;
         if (m_serverCombo.getSelectionIndex() != -1) {
             putConfigValue(AutConfigConstants.SERVER, m_serverCombo.getItem(
                 m_serverCombo.getSelectionIndex()));
             isValid = true;
         }
         if (isValid) {
             if (!m_nameModified) {
                 boolean checkListeners = m_selectionListener != null;
 
                 if (checkListeners) {
                     deinstallListeners();
                 }
                 m_autConfigNameTextField.setText(
                         NLS.bind(
                             Messages.AUTConfigComponentDefaultAUTConfigName,
                         new String [] {
                             m_autName, 
                             m_serverCombo.getText()
                         }));
                 if (checkListeners) {
                     installListeners();
                 }
             }
             return null;
         }
 
         return createErrorStatus(Messages.AUTConfigComponentNoServer);
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
      * @return the Text component for the Configuration name.
      */
     protected JBText getAutConfigNameTextField() {
         return m_autConfigNameTextField;
     }
     
     /** 
      * The action of the working directory field.
      * @return <code>null</code> if the new value is valid. Otherwise, returns
      *         a status parameter indicating the cause of the problem.
      */
     public DialogStatusParameter modifyWorkingDirFieldAction() {
         
         DialogStatusParameter error = null;
         boolean isEmpty = 
             m_autWorkingDirectoryTextField.getText().length() <= 0;
         if (isValid(m_autWorkingDirectoryTextField, true) && !isEmpty) {
             if (checkLocalhostServer()) {
                 File dir = new File(m_autWorkingDirectoryTextField.getText());
                 if (!dir.isAbsolute()) {
                     // Start from server dir, rather than client dir
                     dir = new File("../server/" +  //$NON-NLS-1$
                         m_autWorkingDirectoryTextField.getText());
                 }
                 if (!dir.isDirectory()) {
                     try {
                         error = createWarningStatus(NLS.bind(
                                 Messages.AUTConfigComponentNoDir,
                             new String [] {dir.getCanonicalPath()}));
                     } catch (IOException e) {
                         error = createWarningStatus(NLS.bind(
                                 Messages.AUTConfigComponentFileNotFound,
                             new String [] {
                                 m_autWorkingDirectoryTextField.getText()}));
                     }
                 }
             }
         } else if (!isEmpty) {
             error = createErrorStatus(Messages.AUTConfigComponentWrongWorkDir);
         }
         
         putConfigValue(AutConfigConstants.WORKING_DIR, 
                 m_autWorkingDirectoryTextField.getText());
         
         return error;
     }
     /**
      * 
      * @return the text field for the Working Directory.
      */
     protected JBText getWorkingDirTextField() {
         return m_autWorkingDirectoryTextField;
     }
     
     /**
      * Handles the button event.
      * @param directoryDialog The directory dialog.
      */
     public void handleWorkDirButtonEvent(DirectoryDialog directoryDialog) {
         String directory;
         directoryDialog.setMessage(Messages.AUTConfigComponentSelectWorkDir);
         File path = new File(m_autWorkingDirectoryTextField.getText());
         String filterPath = Utils.getLastDirPath();
         if (path.exists()) {
             try {
                 filterPath = path.getCanonicalPath();
             } catch (IOException e) {
                 // Just use the default path which is already set
             }
         }
         directoryDialog.setFilterPath(filterPath);
         directory = directoryDialog.open();
         if (directory != null) {
             m_autWorkingDirectoryTextField.setText(directory);
             Utils.storeLastDirPath(directoryDialog.getFilterPath());           
             putConfigValue(AutConfigConstants.WORKING_DIR, 
                 m_autWorkingDirectoryTextField.getText());
         }
     }
     
     /**
      * handle the browse request
      * @param folderSelection true if only folders can be selected
      * @param configVarKey key for storing the result
      * @param textfield control for visualizing the value
      * @param title window title
      * @return true if the user selected a new entry and no error occured
      */
     protected boolean remoteBrowse(boolean folderSelection,
             String configVarKey, JBText textfield, String title) {
         
         boolean valueChanged = false;
         
         RemoteFileBrowserDialog directoryDialog = new RemoteFileBrowserDialog(
                 this.getShell(), false, folderSelection ? IResource.FOLDER
                         : IResource.FILE);
 
         try {
             String oldPath = getConfigValue(configVarKey);
             if (oldPath == null || oldPath.length() == 0) {
                 oldPath = "."; //$NON-NLS-1$
             }
             RemoteFileStore baseRemoteFS = new RemoteFileStore(
                     ServerConnection.getInstance().getCommunicator(), ".",  //$NON-NLS-1$
                     folderSelection);
             StringBuilder modPath = new StringBuilder(oldPath);
             baseRemoteFS = handleOldPath(baseRemoteFS, modPath);
             directoryDialog.setInput(baseRemoteFS);
             directoryDialog.setInitialSelection(new RemoteFileStore(
                     ServerConnection.getInstance().getCommunicator(), oldPath
                             .toString(), folderSelection));
             directoryDialog.setFSRoots(baseRemoteFS.getRootFSs());
             directoryDialog.setTitle(title);
             directoryDialog.setMessage(
                     Messages.AUTConfigComponentSelectEntries);
             if (directoryDialog.open() == Window.OK) {
                 final RemoteFileStore resDir = (RemoteFileStore)directoryDialog
                         .getFirstResult();
                 if (resDir != null) {
                     final String path = resDir.getPath();
                     textfield.setText(path);
                     putConfigValue(configVarKey,
                             path);
                     valueChanged = true;
                 }
             }
         } catch (ConnectionException e) {
             //FIXME: tobi NLS not found
             ErrorDialog.openError(Plugin.getShell(), I18n
                     .getString("AutConfigComponent.ERROR_TITLE"), null, //$NON-NLS-1$
                     new Status(IStatus.WARNING, Plugin.PLUGIN_ID, I18n
                             .getString("AutConfigComponent.ERROR_COMM"))); //$NON-NLS-1$
         }
 
         return valueChanged;
     }
 
     /**
      * check for root entries and modify the necessary values
      * @param remoteFS original remote FS
      * @param path original path
      * @return the original or a new RemoteFileStore if the path begins with
      * a root fs entry
      */
     private RemoteFileStore handleOldPath(RemoteFileStore remoteFS,
             StringBuilder path) {
         for (String root : remoteFS.getRootFSs()) {
             if (path.toString().startsWith(root)) {
                 path.delete(0, root.length());
                 return new RemoteFileStore(remoteFS.getCommunicator(),
                         root, remoteFS.fetchInfo().isDirectory());
             }
         }
         return remoteFS;
     }
 
     /**
      * @param validator The validator to set.
      */
     // FIXME zeb This method is necessary at the moment because the creator 
     //           of AutConfigComponents is in the ToolkitSupport project, which 
     //           is not aware of model (PO) objects nor of databinding classes 
     //           (IValidator). This dependency issue should be resolved, and
     //           the validator should be set in the constructor, rather than
     //           in a separate setter method.
     public void setAutIdValidator(IValidator validator) {
         m_autIdValidator = validator;
     }
 
 }
  
