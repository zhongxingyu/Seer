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
 package org.eclipse.jubula.client.ui.preferences;
 
 import java.util.HashSet;
 import java.util.Set;
 
import org.eclipse.jface.bindings.keys.KeySequenceText;
 import org.eclipse.jface.preference.PreferencePage;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher;
 import org.eclipse.jubula.client.ui.Plugin;
 import org.eclipse.jubula.client.ui.constants.Constants;
 import org.eclipse.jubula.client.ui.constants.ContextHelpIds;
 import org.eclipse.jubula.client.ui.constants.InputCodeHelper;
 import org.eclipse.jubula.client.ui.constants.InputCodeHelper.UserInput;
 import org.eclipse.jubula.client.ui.constants.Layout;
 import org.eclipse.jubula.client.ui.i18n.Messages;
 import org.eclipse.jubula.client.ui.preferences.utils.InputComboUtil;
 import org.eclipse.jubula.client.ui.preferences.utils.Utils;
 import org.eclipse.jubula.client.ui.provider.ControlDecorator;
 import org.eclipse.jubula.client.ui.widgets.DirectCombo;
 import org.eclipse.jubula.client.ui.widgets.ModifiableTriggerList;
 import org.eclipse.jubula.tools.constants.StringConstants;
 import org.eclipse.jubula.tools.exception.JBException;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.IWorkbenchPreferencePage;
 
 
 /**
  * @author BREDEX GmbH
  * @created 19.06.2006
  */
 public class ObservationPreferencePage extends PreferencePage implements
     IWorkbenchPreferencePage {
     
     /** 1 column */
     private static final int NUM_COLUMNS = 4;
 
     /** 10 horizontal spaces */
     private static final int HORIZONTAL_SPACING_10 = 10;
 
     /** 10 vertical spaces */
     private static final int VERTICAL_SPACING_10 = 10;
 
     /** margin height = 10 */
     private static final int MARGIN_HEIGHT_10 = 10;
 
     /** margin width = 10 */
     private static final int MARGIN_WIDTH_10 = 10;
     
     /** combo for start/stop checkmode shortcut modifier */
     private Combo m_startStopCheckMods = null;
 
     /** combo for start/stop checkmode shortcut key */
     private DirectCombo<UserInput> m_startStopCheckKey = null;
 
     /** combo for check component shortcut modifier */
     private Combo m_checkCompMods = null;
     
     /** combo for check component shortcut key */
     private DirectCombo<UserInput> m_checkCompKey = null;
     
     /** checkbox for recorded action dialog */
     private Button m_showDialog = null;
         
     /** container for management of singleLine trigger keys */
     private ModifiableTriggerList m_singleLineTrigger;
     
     /** container for management of multiLine trigger keys */
     private ModifiableTriggerList m_multiLineTrigger;
         
    
    /**
     * The manager for the text widget that traps incoming key events. This
     * manager should be used to access the widget, rather than accessing the
     * widget directly.
     */
    private KeySequenceText m_textTriggerSequenceManager;
 
   
     /**
      * 
      */
     public ObservationPreferencePage() {
         setPreferenceStore(Plugin.getDefault().getPreferenceStore());
     }
 
 
     /**
      * {@inheritDoc}
      * 
      * @param parent
      * @return
      */
     protected Control createContents(Composite parent) {
         /** Add layer to parent widget */
         Composite composite = new Composite(parent, SWT.NONE);
 
         /** Define laout rules for widget placement */
         compositeGridData(composite);
         createObservModeArea(composite);
         Plugin.getHelpSystem().setHelp(parent, ContextHelpIds.PREFPAGE_OBSERV);
         initPreferences();
         return composite;
     }
     
 
     /**
      * {@inheritDoc}
      * @param workbench
      */
     public void init(IWorkbench workbench) {
         setDescription(Messages.ObservationPreferencePageDescription);
     }
     
     /**
      * @param parent parent of this Combo
      */
     private void createObservModeArea(Composite parent) {
         InputCodeHelper keys = InputCodeHelper.getInstance();
         
         Composite composite = new Composite(parent, SWT.NONE);
         compositeGridData(composite);
         
         Label keycombLabel = new Label(composite, SWT.NONE);
         keycombLabel.setText(Messages.ObservationPreferencePageKeycombos);
         keycombLabel.setFont(Layout.BOLD_TAHOMA);
         GridData data = new GridData();
         data.horizontalSpan = 4;
         keycombLabel.setLayoutData(data);
         
         Label checkmodeLabel = new Label(composite, SWT.NONE);
         checkmodeLabel.setText(Messages.ObservationPreferencePageCheckMode);
         
         m_startStopCheckMods = new Combo(
                 composite, SWT.DROP_DOWN | SWT.READ_ONLY);
         m_startStopCheckMods.setItems(keys.getModifierString());
         Label pluslabel = new Label(composite, SWT.NONE);
         pluslabel.setText(StringConstants.SPACE + StringConstants.PLUS 
                 + StringConstants.SPACE);
         m_startStopCheckKey = InputComboUtil.createKeyCombo(
                 composite, SWT.DROP_DOWN | SWT.READ_ONLY);
 
         Label checkCompLabel = new Label(composite, SWT.NONE);
         checkCompLabel.setText(Messages.ObservationPreferencePageCheckComp);
         
         m_checkCompMods = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
         m_checkCompMods.setItems(keys.getModifierString());
         Label pluslabel2 = new Label(composite, SWT.NONE);
         pluslabel2.setText(" + "); //$NON-NLS-1$
         m_checkCompKey = InputComboUtil.createKeyCombo(
                 composite, SWT.DROP_DOWN | SWT.READ_ONLY);
         
         SelectionListener selListener = new SelectionListener() {
             public void widgetSelected(SelectionEvent e) {
                 checkValidKeys();
             }
             public void widgetDefaultSelected(SelectionEvent e) {
                 // ok
             }
         };
         m_startStopCheckMods.addSelectionListener(selListener);
         m_startStopCheckKey.addSelectionListener(selListener);
         m_checkCompMods.addSelectionListener(selListener);
         m_checkCompKey.addSelectionListener(selListener);
 
         Label dialogLabel = new Label(composite, SWT.NONE);
         dialogLabel.setText(Messages.ObservationPreferencePageRecActDialog);
         dialogLabel.setFont(Layout.BOLD_TAHOMA);
         GridData data2 = new GridData();
         data2.horizontalSpan = 4;
         dialogLabel.setLayoutData(data2);        
         ControlDecorator.decorateInfo(dialogLabel,  
                 "GDControlDecorator.ObervationConsole", false); //$NON-NLS-1$
               
         m_showDialog = new Button(composite, SWT.CHECK);
         m_showDialog.setText(Messages.ObservationPreferencePageShowDialog);
         //
 
         createTriggerArea(composite);
     }
     
     /**
      * @param composite Composite
      */
     private void createTriggerArea(Composite composite) {
         Label triggerLabel = new Label(composite, SWT.NONE);
         triggerLabel.setText(Messages.ObservationPreferencePageTrigger);
         triggerLabel.setFont(Layout.BOLD_TAHOMA);
         GridData data3 = new GridData();
         data3.horizontalSpan = 4;
         triggerLabel.setLayoutData(data3);
         ControlDecorator.decorateInfo(triggerLabel, 
                "GDControlDecorator.ObservationTriggerReplaceText", false);
         
         Set<String> values = new HashSet<String>();
         m_singleLineTrigger = new ModifiableTriggerList(composite, SWT.NONE, 
                 Messages.ObservationPreferencePageSingleLine,
                 values, true);
         GridData gridData2 = new GridData(GridData.FILL_VERTICAL);
         gridData2.horizontalSpan = 2;
         gridData2.widthHint = 200;
         gridData2.heightHint = 300;
         m_singleLineTrigger.setLayoutData(gridData2);
         
         
         Set<String> values2 = new HashSet<String>();
         m_multiLineTrigger = new ModifiableTriggerList(composite, SWT.NONE, 
                 Messages.ObservationPreferencePageMultiLine,
                 values2, true);
         GridData gridData3 = new GridData(GridData.FILL_VERTICAL);
         gridData3.horizontalSpan = 2;
         gridData3.widthHint = 200;
         gridData3.heightHint = 300;
         m_multiLineTrigger.setLayoutData(gridData3);
         
         Label keycombLabel = new Label(composite, SWT.NONE);
         keycombLabel.setText(Messages.ObservationPreferencePageHhint);
         keycombLabel.setFont(Layout.BOLD_TAHOMA);
         GridData data = new GridData();
         data.horizontalSpan = 4;
         keycombLabel.setLayoutData(data);
 
     }
     
     /**
      * checks if both combinations not using same
      * keys
      */
     protected void checkValidKeys() {
         final String checkModeShortcut = m_startStopCheckMods.getText() 
             + m_startStopCheckKey.getText();
         final String checkCompShortcut = m_checkCompMods.getText() 
             + m_checkCompKey.getText();
         // All Observation shortcuts must differ
         if (checkModeShortcut.equals(checkCompShortcut)) {
             
             setErrorMessage(Messages.ObservationPreferencePageRecordInvalidKey);
             setValid(false);
         } else {
             setErrorMessage(null);
             setValid(true);
         }
     }
 
 
     /**
      * @param composite The composite.
      */
     private void compositeGridData(Composite composite) {
         GridLayout compositeLayout = new GridLayout();
         compositeLayout.numColumns = NUM_COLUMNS;
         compositeLayout.horizontalSpacing = HORIZONTAL_SPACING_10;
         compositeLayout.verticalSpacing = VERTICAL_SPACING_10;
         compositeLayout.marginHeight = MARGIN_HEIGHT_10;
         compositeLayout.marginWidth = MARGIN_WIDTH_10;
         composite.setLayout(compositeLayout);
         GridData compositeData = new GridData(GridData.FILL_BOTH);
         compositeData.grabExcessHorizontalSpace = true;
         compositeData.grabExcessVerticalSpace = false;
         composite.setLayoutData(compositeData);
     }
     
     /**
      * Performs special processing when this page's Restore Defaults button has
      * been pressed. Sets the contents of the nameEntry field to be the default
      */
     protected void initPreferences() {
         // sets widgets to default values
         m_startStopCheckMods.select(InputCodeHelper.getInstance()
                 .getIndexOfModifier(getPreferenceStore().getInt(
                         Constants.CHECKMODE_MODS_KEY)));
         InputComboUtil.setSelectedKey(
                 m_startStopCheckKey, 
                 getPreferenceStore().getInt(Constants.CHECKMODE_KEY_KEY));
         m_checkCompMods.select(InputCodeHelper.getInstance().getIndexOfModifier(
             getPreferenceStore().getInt(Constants.CHECKCOMP_MODS_KEY)));
         InputComboUtil.setSelectedKey(
                 m_checkCompKey, 
                 getPreferenceStore().getInt(Constants.CHECKCOMP_KEY_KEY));
         
         m_showDialog.setSelection(getPreferenceStore().
                 getBoolean(Constants.SHOWRECORDDIALOG_KEY));
         
         try {
             m_singleLineTrigger.setValues(
                     Utils.decodeStringToSet(getPreferenceStore().getString(
                             Constants.SINGLELINETRIGGER_KEY),
                             StringConstants.SEMICOLON));
             m_multiLineTrigger.setValues(
                     Utils.decodeStringToSet(getPreferenceStore().getString(
                             Constants.MULTILINETRIGGER_KEY),
                             StringConstants.SEMICOLON));
         } catch (JBException e) {
             e.printStackTrace();
         }
     }
     
 
 
     
     /**
      * Performs special processing when this page's Restore Defaults button has
      * been pressed. Sets the contents of the nameEntry field to be the default
      */
     protected void performDefaults() {
         // sets widgets to default values
         m_startStopCheckMods.select(InputCodeHelper.getInstance()
                 .getIndexOfModifier(
                         getDefaultPrefsInt(Constants.CHECKMODE_MODS_KEY)));
         InputComboUtil.setSelectedKey(m_startStopCheckKey,
                 getDefaultPrefsInt(Constants.CHECKMODE_KEY_KEY));
         m_checkCompMods.select(InputCodeHelper.getInstance()
                 .getIndexOfModifier(
                         getDefaultPrefsInt(Constants.CHECKCOMP_MODS_KEY)));
         InputComboUtil.setSelectedKey(m_checkCompKey,
                 getDefaultPrefsInt(Constants.CHECKCOMP_KEY_KEY));
 
         m_showDialog.setSelection(
                 getDefaultPrefsBool(Constants.SHOWRECORDDIALOG_KEY));
 
         try {
             m_singleLineTrigger.setValues(Utils.decodeStringToSet(
                     getDefaultPrefsString(Constants.SINGLELINETRIGGER_KEY),
                     StringConstants.SEMICOLON));
             m_multiLineTrigger.setValues(Utils.decodeStringToSet(
                     getDefaultPrefsString(Constants.MULTILINETRIGGER_KEY),
                     StringConstants.SEMICOLON));
         } catch (JBException e) {
             e.printStackTrace();
         }
 
         checkValidKeys();
     }
     
     /**
      * @return default value
      * @param key
      *            preference key
      */
     private boolean getDefaultPrefsBool(String key) {
         return getPreferenceStore().getDefaultBoolean(key);
     }
     
     /**
      * @return default value
      * @param key preference key
      */
     private int getDefaultPrefsInt(String key) {
         return getPreferenceStore().getDefaultInt(key);
     }
     
     /**
      * @return default value
      * @param key
      *            preference key
      */
     private String getDefaultPrefsString(String key) {
         return getPreferenceStore().getDefaultString(key);
     }
     
     /**
      * Method declared on IPreferencePage. 
      * 
      * @return performOK
      */
     public boolean performOk() {
         // read preferences from widgets
         // set preferences in store
         getPreferenceStore().setValue(Constants.CHECKMODE_MODS_KEY,
                         InputCodeHelper.getInstance().getModifier()[
                              m_startStopCheckMods.getSelectionIndex()]);
         InputComboUtil.setPrefCode(m_startStopCheckKey, getPreferenceStore(),
                 Constants.CHECKMODE_KEY_KEY);
         getPreferenceStore().setValue(
                 Constants.CHECKCOMP_MODS_KEY,
                 InputCodeHelper.getInstance().getModifier()[m_checkCompMods
                         .getSelectionIndex()]);
         InputComboUtil.setPrefCode(m_checkCompKey, getPreferenceStore(),
                 Constants.CHECKCOMP_KEY_KEY);
         getPreferenceStore().setValue(Constants.SHOWRECORDDIALOG_KEY,
                 m_showDialog.getSelection());
 
         storeTriggerList();
         return super.performOk();
     }
     
     /**
      * Stores the trigger list in the preferences.
      */
     public void storeTriggerList() {
         String singleStorage = StringConstants.EMPTY;
         String[] singleTriggers = m_singleLineTrigger.getValues();
         singleStorage = Utils.encodeStringArray(singleTriggers,
                 StringConstants.SEMICOLON);
       
         Plugin.getDefault().getPreferenceStore().setValue(
             Constants.SINGLELINETRIGGER_KEY, singleStorage);
         
         
         String multiStorage = StringConstants.EMPTY;
         String[] multiTriggers = m_multiLineTrigger.getValues();
         multiStorage = Utils.encodeStringArray(multiTriggers,
                 StringConstants.SEMICOLON);
         
         Plugin.getDefault().getPreferenceStore().setValue(
             Constants.MULTILINETRIGGER_KEY, multiStorage);
 
         DataEventDispatcher.getInstance().fireServerPreferencesChanged();
     }
 }
