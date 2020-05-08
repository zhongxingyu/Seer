 /*******************************************************************************
  * Copyright (c) 2003, 2004 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * IBM Corporation - initial API and implementation
  *******************************************************************************/
 /*
  * Created on Nov 10, 2003
  *
  * To change the template for this generated file go to
  * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
  */
 package org.eclipse.jst.j2ee.internal.wizard;
 
 import java.io.File;
 
 import org.eclipse.jem.util.emf.workbench.ProjectUtilities;
 import org.eclipse.jface.dialogs.IDialogSettings;
 import org.eclipse.jst.j2ee.datamodel.properties.IJ2EEComponentCreationDataModelProperties;
 import org.eclipse.jst.j2ee.internal.plugin.J2EEUIMessages;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ControlEvent;
 import org.eclipse.swt.events.ControlListener;
 import org.eclipse.swt.events.DisposeEvent;
 import org.eclipse.swt.events.DisposeListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.graphics.Cursor;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.DirectoryDialog;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.wst.common.frameworks.datamodel.DataModelPropertyDescriptor;
 import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
 import org.eclipse.wst.common.frameworks.internal.FlexibleJavaProjectPreferenceUtil;
 import org.eclipse.wst.common.frameworks.internal.datamodel.ui.DataModelWizardPage;
 import org.eclipse.wst.server.ui.ServerUIUtil;
 
 
 public abstract class J2EEComponentCreationWizardPage extends DataModelWizardPage implements IJ2EEComponentCreationDataModelProperties{
 
     private static final boolean isWindows = SWT.getPlatform().toLowerCase().startsWith("win"); //$NON-NLS-1$
     protected static final String MODULE_VERSION = "Module Version:";
     protected NewModuleGroup projectNameGroup;
     protected Composite advancedComposite;
     protected Button advancedButton;
     protected boolean showAdvanced = false;
     protected AdvancedSizeController advancedController;
     protected boolean advancedControlsBuilt = false;
     private ServerEarAndStandaloneGroup earGroup;
     protected Combo serverTargetCombo;
     protected Text moduleNameText = null;
     protected Text locationPathField = null;
     protected Button browseButton = null;
     
     private static final int SIZING_TEXT_FIELD_WIDTH = 305;
     private static final String NEW_LABEL_UI = J2EEUIMessages.getResourceString(J2EEUIMessages.NEW_THREE_DOTS_E); //$NON-NLS-1$
     private static final String MODULE_NAME_UI = J2EEUIMessages.getResourceString(J2EEUIMessages.NAME_LABEL); //$NON-NLS-1$
     private String defBrowseButtonLabel = J2EEUIMessages.getResourceString(J2EEUIMessages.BROWSE_LABEL); //$NON-NLS-1$
     private static final String defDirDialogLabel = "Directory"; //$NON-NLS-1$
 
     /**
      *  This type is responsible for setting the Shell size based on the showAdvanced flag. It will
      * track the original size of the Shell even if the user resizes it. One problem that we may
      * face is that the Shell size could change by the framework prior to the Shell being made
      * visible but the page will already get an enter call. This means that we will need to set the
      * Shell size based on the showAdvanced flag when the Shell resize event is called and the Shell
      * is visible.
      */
     private class AdvancedSizeController implements ControlListener {
         private int advancedHeight = -1;
         private Point originalSize;
         private boolean ignoreShellResize = false;
 
         private AdvancedSizeController(Shell aShell) {
             originalSize = aShell.getSize();
             aShell.addControlListener(this);
         }
 
         public void controlMoved(ControlEvent e) {
             //do nothing
         }
 
         public void controlResized(ControlEvent e) {
             if (!ignoreShellResize) {
                 Control control = (Control) e.getSource();
                 if (control.isVisible()) {
                     originalSize = control.getSize();
                     if (advancedHeight == -1)
                         setShellSizeForAdvanced();
                 }
             }
         }
 
         protected void resetOriginalShellSize() {
             setShellSize(originalSize.x, originalSize.y);
         }
 
         private void setShellSize(int x, int y) {
             ignoreShellResize = true;
             try {
                 getShell().setSize(x, y);
             } finally {
                 ignoreShellResize = false;
             }
         }
 
         protected void setShellSizeForAdvanced() {
             int height = calculateAdvancedShellHeight();
             if (height != -1)
                 setShellSize(getShell().getSize().x, height);
         }
 
         private int calculateAdvancedShellHeight() {
             Point advancedCompSize = advancedComposite.getSize();
             if (advancedCompSize.x == 0)
                 return -1;
             int height = computeAdvancedHeight();
             if (!showAdvanced && height != -1)
                 height = height - advancedComposite.getSize().y;
             return height;
         }
 
         /*
          * Compute the height with the advanced section showing. @return
          */
         private int computeAdvancedHeight() {
             if (advancedHeight == -1) {
                 Point controlSize = getControl().getSize();
                 if (controlSize.x != 0) {
                     int minHeight = originalSize.y - controlSize.y;
                     Point pageSize = getControl().computeSize(SWT.DEFAULT, SWT.DEFAULT);
                     advancedHeight = pageSize.y + minHeight;
                 }
             }
             return advancedHeight;
         }
     }
     
     public J2EEComponentCreationWizardPage(IDataModel dataModel, String pageName) {
         super(dataModel, pageName);
     }
     
     protected Composite createTopLevelComposite(Composite parent) {
         Composite top = new Composite(parent, SWT.NONE);
         PlatformUI.getWorkbench().getHelpSystem().setHelp(top, getInfopopID());
         top.setLayout(new GridLayout());
         top.setData(new GridData(GridData.FILL_BOTH));
         Composite composite = new Composite(top, SWT.NONE);
         GridLayout layout = new GridLayout(3, false);
         composite.setLayout(layout);
         createModuleGroup(composite);
         Composite detail = new Composite(top, SWT.NONE);
         detail.setLayout(new GridLayout());
         detail.setData(new GridData(GridData.FILL_BOTH));
         createAdvancedComposite(detail);
         return top;
     }
 
     protected Composite createAdvancedComposite(Composite parent) {
         advancedControlsBuilt = true;
         advancedButton = new Button(parent, SWT.TOGGLE);
         setAdvancedLabelText();
         final Cursor hand = new Cursor(advancedButton.getDisplay(), SWT.CURSOR_HAND);
         advancedButton.addDisposeListener(new DisposeListener() {
             public void widgetDisposed(DisposeEvent e) {
                 hand.dispose();
             }
         });
         advancedComposite = new Composite(parent, SWT.NONE);
         //toggleAdvanced(false);
         GridLayout layout = new GridLayout(3, false);
         GridData data = new GridData();
         advancedComposite.setData(data);
         advancedComposite.setLayout(layout);
         advancedButton.addSelectionListener(new SelectionListener() {
             public void widgetSelected(SelectionEvent e) {
                 toggleAdvanced(true);
             }
 
             public void widgetDefaultSelected(SelectionEvent e) {
                 //do nothing
             }
         });
         advancedButton.addListener(SWT.MouseHover, new Listener() {
             public void handleEvent(Event event) {
                 if (event.type == SWT.MouseHover)
                     advancedButton.setCursor(hand);
             }
         });
         addToAdvancedComposite(advancedComposite);
         return advancedComposite;
     }
     
     private void createModuleGroup(Composite parent) {
         // Add the module name label
         if(FlexibleJavaProjectPreferenceUtil.getMultipleModulesPerProjectProp()){
             new NewModuleDataModelGroup(parent, getDataModel(),synchHelper);
         } else {
             createProjectNameGroup(parent);
             createProjectLocationGroup(parent);
         }
     }
     
     /**
      *  
      */
     private void createProjectNameGroup(Composite parent) {
         // set up project name label
         Label projectNameLabel = new Label(parent, SWT.NONE);
         projectNameLabel.setText(MODULE_NAME_UI);
         GridData data = new GridData();
         projectNameLabel.setLayoutData(data);
         // set up project name entry field
         moduleNameText = new Text(parent, SWT.BORDER);
         data = new GridData(GridData.FILL_HORIZONTAL);
         data.widthHint = SIZING_TEXT_FIELD_WIDTH;
         moduleNameText.setLayoutData(data);
         new Label(parent, SWT.NONE); // pad
         synchHelper.synchText(moduleNameText, COMPONENT_NAME, new Control[]{projectNameLabel});
        moduleNameText.setFocus();
     }
 
     /**
      *  
      */
     private void createProjectLocationGroup(Composite parent) {
         //      set up location path label
         Label locationPathLabel = new Label(parent, SWT.NONE);
         locationPathLabel.setText(J2EEUIMessages.getResourceString(J2EEUIMessages.PROJECT_LOC_LBL));
         GridData data = new GridData();
         locationPathLabel.setLayoutData(data);
         // set up location path entry field
         locationPathField = new Text(parent, SWT.BORDER | SWT.READ_ONLY);
         data = new GridData(GridData.FILL_HORIZONTAL);
         data.widthHint = SIZING_TEXT_FIELD_WIDTH;
         locationPathField.setLayoutData(data);
         // set up browse button
         browseButton = new Button(parent, SWT.PUSH);
         browseButton.setText(defBrowseButtonLabel);
         browseButton.setLayoutData((new GridData(GridData.FILL_HORIZONTAL)));
         browseButton.addSelectionListener(new SelectionAdapter() {
             public void widgetSelected(SelectionEvent e) {
                 handleLocationBrowseButtonPressed();
             }
         });
         browseButton.setEnabled(true);
         synchHelper.synchText(locationPathField, LOCATION, null);
     }
     /**
      * Open an appropriate directory browser
      */
     protected void handleLocationBrowseButtonPressed() {
         DirectoryDialog dialog = new DirectoryDialog(locationPathField.getShell());
         dialog.setMessage(defDirDialogLabel);
         String dirName = getDataModel().getStringProperty(LOCATION);
         if ((dirName != null) && (dirName.length() != 0)) {
             File path = new File(dirName);
             if (path.exists()) {
                 dialog.setFilterPath(dirName);
             }
         }
         String selectedDirectory = dialog.open();
         if (selectedDirectory != null) {
             getDataModel().setProperty(LOCATION, selectedDirectory);
         }
     }
 
     protected void addToAdvancedComposite(Composite advanced) {
         if(!FlexibleJavaProjectPreferenceUtil.getMultipleModulesPerProjectProp())
             createServerTargetComposite(advanced);
         createVersionComposite(advanced);
         createServerEarAndStandaloneGroup(advanced);
     }
 
     protected void createServerTargetComposite(Composite parent) {
         Label label = new Label(parent, SWT.NONE);
         label.setText(J2EEUIMessages.getResourceString(J2EEUIMessages.TARGET_SERVER_LBL));
         serverTargetCombo = new Combo(parent, SWT.BORDER | SWT.READ_ONLY);
         serverTargetCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
         Button newServerTargetButton = new Button(parent, SWT.NONE);
         newServerTargetButton.setText(J2EEUIMessages.getResourceString(J2EEUIMessages.NEW_THREE_DOTS_E));
         newServerTargetButton.addSelectionListener(new SelectionAdapter() {
             public void widgetSelected(SelectionEvent e) {
                 //J2EEComponentCreationWizardPage.launchNewRuntimeWizard(getShell(), (IDataModel)model.getNestedModel(NESTED_PROJECT_CREATION_DM));
                 J2EEComponentCreationWizardPage.launchNewRuntimeWizard(getShell(), model);
             }
         });
         Control[] deps = new Control[]{label, newServerTargetButton};
         //synchHelper.synchCombo(serverTargetCombo, RUNTIME_TARGET_ID, deps);
         synchHelper.synchCombo(serverTargetCombo, RUNTIME_TARGET_ID, deps);
         if(serverTargetCombo.getVisibleItemCount() != 0)
             serverTargetCombo.select(0);
     }
 
     protected void createServerEarAndStandaloneGroup(Composite parent) {
         earGroup = new ServerEarAndStandaloneGroup(parent, getDataModel(), synchHelper);
     }
 
     protected String[] getValidationPropertyNames() {
         return new String[]{IJ2EEComponentCreationDataModelProperties.PROJECT_NAME, RUNTIME_TARGET_ID, COMPONENT_VERSION, COMPONENT_NAME, LOCATION, EAR_COMPONENT_NAME, ADD_TO_EAR };
     }
 
     protected void createVersionComposite(Composite parent) {
         createVersionComposite(parent, getVersionLabel(), COMPONENT_VERSION);
     }
 
     protected String getVersionLabel() {
         return MODULE_VERSION;
     }
 
     public void dispose() {
         super.dispose();
         if (earGroup != null)
             earGroup.dispose();
         if (projectNameGroup != null)
             projectNameGroup.dispose();
     }
     
     /*
      * (non-Javadoc)
      * 
      * @see org.eclipse.wst.common.frameworks.internal.ui.wizard.WTPWizardPage#enter()
      */
     protected void enter() {
         if (advancedControlsBuilt) {
             if (isFirstTimeToPage)
                 initializeAdvancedController();
             if (isWindows) {
                 advancedController.setShellSizeForAdvanced();
             }
         }
         super.enter();
     }
 
     private void initializeAdvancedController() {
         advancedController = new AdvancedSizeController(getShell());
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.eclipse.wst.common.frameworks.internal.ui.wizard.WTPWizardPage#exit()
      */
     protected void exit() {
         if (advancedControlsBuilt && isWindows && advancedController!=null) {
             advancedController.resetOriginalShellSize();
         }
         super.exit();
     }
     
     /*
      * (non-Javadoc)
      * @see org.eclipse.wst.common.frameworks.internal.ui.wizard.WTPWizardPage#storeDefaultSettings()
      */
     public void storeDefaultSettings() {
         super.storeDefaultSettings();
         if (advancedControlsBuilt) {
             IDialogSettings settings = getDialogSettings();
             if (settings != null)
                 settings.put(getShowAdvancedKey(), showAdvanced);
         }
     }
 
     protected String getShowAdvancedKey() {
         return getClass().getName() + "_SHOW_ADVANCED"; //$NON-NLS-1$
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.eclipse.wst.common.frameworks.internal.ui.wizard.WTPWizardPage#restoreDefaultSettings()
      */
     protected void restoreDefaultSettings() {
         super.restoreDefaultSettings();
         if (advancedControlsBuilt) {
             IDialogSettings settings = getDialogSettings();
             if (settings != null)
                 showAdvanced = !settings.getBoolean(getShowAdvancedKey());
             advancedButton.setSelection(!showAdvanced); //set opposite b/c toggleAdvanced(boolean)
             // will flip it
             toggleAdvanced(false);
         }
     }
 
     /**
      * @param advancedLabel
      */
     private void setAdvancedLabelText() {
         if (advancedControlsBuilt) {
             if (showAdvanced)
                 advancedButton.setText(J2EEUIMessages.getResourceString("J2EEProjectCreationPage_UI_0")); //$NON-NLS-1$
             else
                 advancedButton.setText(J2EEUIMessages.getResourceString("J2EEProjectCreationPage_UI_1")); //$NON-NLS-1$
         }
     }
 
     /**
      * @param advancedLabel
      */
     protected void toggleAdvanced(boolean setSize) {
         if (advancedControlsBuilt) {
             showAdvanced = !showAdvanced;
             advancedComposite.setVisible(showAdvanced);
             setAdvancedLabelText();
             if (setSize && isWindows) {
                 if (advancedControlsBuilt) {
                     if (advancedController == null)
                         initializeAdvancedController();
                     if (isWindows) {
                         advancedController.setShellSizeForAdvanced();
                     }
                 }
                 advancedController.setShellSizeForAdvanced();
             }   
         }
     }
 
     protected void createVersionComposite(Composite parent, String labelText, String versionProp) {
         Label label = new Label(parent, SWT.NONE);
         label.setText(labelText);
         Combo versionCombo = new Combo(parent, SWT.BORDER | SWT.READ_ONLY);
         GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
         gridData.widthHint = 305;
         versionCombo.setLayoutData(gridData);
         Control[] deps = new Control[]{label};
         synchHelper.synchCombo(versionCombo, versionProp, deps);
         String[] items = versionCombo.getItems();
         if (items != null && items.length > 0)
             versionCombo.select(items.length - 1);
         new Label(parent, SWT.NONE); //pad
     }   
     
 
     
     public static boolean launchNewRuntimeWizard(Shell shell, IDataModel model) {
         DataModelPropertyDescriptor[] preAdditionDescriptors = model.getValidPropertyDescriptors(RUNTIME_TARGET_ID);
         boolean isOK = ServerUIUtil.showNewRuntimeWizard(shell, "", "");  //$NON-NLS-1$  //$NON-NLS-2$
         if (isOK && model != null) {
 
             DataModelPropertyDescriptor[] postAdditionDescriptors = model.getValidPropertyDescriptors(RUNTIME_TARGET_ID);
             Object[] preAddition = new Object[preAdditionDescriptors.length];
             for (int i = 0; i < preAddition.length; i++) {
                 preAddition[i] = preAdditionDescriptors[i].getPropertyValue();
             }
             Object[] postAddition = new Object[postAdditionDescriptors.length];
             for (int i = 0; i < postAddition.length; i++) {
                 postAddition[i] = postAdditionDescriptors[i].getPropertyValue();
             }
             Object newAddition = ProjectUtilities.getNewObject(preAddition, postAddition);
 
             model.notifyPropertyChange(RUNTIME_TARGET_ID, IDataModel.VALID_VALUES_CHG);
             if (newAddition != null)
                 model.setProperty(RUNTIME_TARGET_ID, newAddition);
         }
         return isOK;
     }   
 
 }
