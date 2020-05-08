 // $codepro.audit.disable com.instantiations.assist.eclipse.analysis.audit.rule.effectivejava.constructorsOnlyInvokeFinalMethods, com.instantiations.assist.eclipse.analysis.deserializeabilitySecurity, com.instantiations.assist.eclipse.analysis.enforceCloneableUsageSecurity
 /*******************************************************************************
  * Copyright (c) 2010 Ericsson Research Canada
  * 
  * All rights reserved. This program and the accompanying materials are
  * made available under the terms of the Eclipse Public License v1.0 which
  * accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Description:
  * 
  * This class represents a preference page that is contributed to the Preferences dialog. By 
  * subclassing <samp>FieldEditorPreferencePage</samp>, we can use the field support built into JFace that allows
  * us to create a page that is small and knows how to save, restore and apply itself.
  * <p>
  * This page is used to modify preferences only. They are stored in the preference store that belongs to
  * the main plug-in class. That way, preferences can be accessed directly via the preference store.
  * 
  * Contributors:
  *   Sebastien Dubois - Created for Mylyn Review R4E project
  *   
  *******************************************************************************/
 
 package org.eclipse.mylyn.reviews.r4e.ui.internal.preferences;
 
 import java.io.IOException;
 
 import javax.naming.NamingException;
 
 import org.eclipse.jface.preference.FieldEditorPreferencePage;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EReviewGroup;
 import org.eclipse.mylyn.reviews.r4e.core.model.drules.R4EDesignRuleCollection;
 import org.eclipse.mylyn.reviews.r4e.core.model.serial.impl.ResourceHandlingException;
 import org.eclipse.mylyn.reviews.r4e.ui.R4EUIPlugin;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.editors.FilePathEditor;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIModelController;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.utils.CommandUtils;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.utils.R4EUIConstants;
 import org.eclipse.mylyn.reviews.userSearch.query.IQueryUser;
 import org.eclipse.mylyn.reviews.userSearch.query.QueryUserFactory;
 import org.eclipse.mylyn.reviews.userSearch.userInfo.IUserInfo;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.FocusEvent;
 import org.eclipse.swt.events.FocusListener;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.List;
 import org.eclipse.swt.widgets.TabFolder;
 import org.eclipse.swt.widgets.TabItem;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.IWorkbenchPreferencePage;
 
 /**
  * @author lmcdubo
  * @version $Revision: 1.0 $
  */
 public class R4EPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
 
 	// ------------------------------------------------------------------------
 	// Constants
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Field PREFS_CONTAINER_DATA_SPAN. (value is 1)
 	 */
 	private static final int PREFS_CONTAINER_DATA_SPAN = 1;
 
 	/**
 	 * Field R4E_PREFS_CONTAINER_DATA_SPAN. (value is 2)
 	 */
 	private static final int GROUP_PREFS_CONTAINER_DATA_SPAN = 2; // $codepro.audit.disable constantNamingConvention
 
 	/**
 	 * Field INVALID_FILE_STR. (value is ""<File not found>"")
 	 */
 	private static final String INVALID_FILE_STR = "<File not found>";
 
 	// ------------------------------------------------------------------------
 	// Member Variables
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Field fUserIdTextField.
 	 */
 	private Text fUserIdTextField = null;
 
 	/**
 	 * Field fUserEmailTextField.
 	 */
 	private Text fUserEmailTextField = null;
 
 	/**
 	 * Field fUseDeltasButton.
 	 */
 	private Button fUseDeltasButton = null;
 
 	/**
 	 * Field fAutoImportPostponedButton.
 	 */
 	private Button fAutoImportPostponedButton = null;
 
 	/**
 	 * Field fGroupNameText.
 	 */
 	private Text fGroupNameText = null;
 
 	/**
 	 * Field fGroupDescriptionText.
 	 */
 	private Text fGroupDescriptionText = null;
 
 	/**
 	 * Field fRuleSetNameText.
 	 */
 	private Text fRuleSetNameText = null;
 
 	/**
 	 * Field fGRuleSetVersionText.
 	 */
 	private Text fRuleSetVersionText = null;
 
 	/**
 	 * Field fReviewShowDisabledButton.
 	 */
 	private Button fReviewShowDisabledButton = null;
 
 	/**
 	 * Field fReviewsCompletedFilterButton.
 	 */
 	private Button fReviewsCompletedFilterButton = null;
 
 	/**
 	 * Field fReviewsOnlyFilterButton.
 	 */
 	private Button fReviewsOnlyFilterButton = null;
 
 	/**
 	 * Field fAnomaliesMyFilterButton.
 	 */
 	private Button fAnomaliesMyFilterButton = null;
 
 	/**
 	 * Field fReviewMyFilterButton.
 	 */
 	private Button fReviewMyFilterButton = null;
 
 	/**
 	 * Field fParticipantFilterButton.
 	 */
 	private Button fParticipantFilterButton = null;
 
 	/**
 	 * Field fAnomaliesFilterButton.
 	 */
 	private Button fAnomaliesFilterButton = null;
 
 	/**
 	 * Field fReviewedItemsFilterButton.
 	 */
 	private Button fReviewedItemsFilterButton = null;
 
 	/**
 	 * Field fHideRuleSetsFilterButton.
 	 */
 	private Button fHideRuleSetsFilterButton = null;
 
 	/**
 	 * Field fHideDeltasFilterButton.
 	 */
 	private Button fHideDeltasFilterButton = null;
 
 	/**
 	 * Field fParticipantIdText.
 	 */
 	private Text fParticipantIdText = null;
 
 	// ------------------------------------------------------------------------
 	// Constructors
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Constructor for R4EPreferencePage.
 	 */
 	public R4EPreferencePage() {
 		super(GRID);
 		setPreferenceStore(R4EUIPlugin.getDefault().getPreferenceStore());
 		setDescription(PreferenceConstants.P_DESC);
 	}
 
 	// ------------------------------------------------------------------------
 	// Methods
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Creates the field editors. Field editors are abstractions of the common GUI blocks needed to manipulate various
 	 * types of preferences. Each field editor knows how to save and restore itself.
 	 */
 	@Override
 	public void createFieldEditors() {
 
 		R4EUIPlugin.Ftracer.traceInfo("Build R4E Preference page");
 
 		//The Main preferences composite
 		final Composite prefsContainer = new Composite(getFieldEditorParent(), SWT.NONE);
 		final GridData prefsContainerData = new GridData(GridData.FILL, GridData.FILL, true, false);
 		prefsContainerData.horizontalSpan = PREFS_CONTAINER_DATA_SPAN;
 		prefsContainer.setLayoutData(prefsContainerData);
 		final GridLayout prefsLayout = new GridLayout(PREFS_CONTAINER_DATA_SPAN, false);
 		prefsContainer.setLayout(prefsLayout);
 
 		final TabFolder tabFolder = new TabFolder(prefsContainer, SWT.TOP);
 		final GridData tabFolderData = new GridData(GridData.FILL, GridData.FILL, true, true);
 		tabFolder.setLayoutData(tabFolderData);
 
 		createUserPreferencesTab(tabFolder);
 		createGroupPreferencesTab(tabFolder);
 		createRuleSetsPreferencesTab(tabFolder);
 		createFiltersPreferencesTab(tabFolder);
 	}
 
 	/**
 	 * Method createUserPreferencesTab.
 	 * 
 	 * @param aParent
 	 *            Composite
 	 */
 	private void createUserPreferencesTab(TabFolder aParent) {
 
 		final IPreferenceStore store = R4EUIPlugin.getDefault().getPreferenceStore();
 
 		final TabItem tabItem = new TabItem(aParent, SWT.NONE);
 		tabItem.setText("User");
 
 		// Create a Group to hold R4E user preferences
 		final Group r4EUserPrefsGroup = new Group(aParent, SWT.BORDER_SOLID);
 		tabItem.setControl(r4EUserPrefsGroup);
 		final GridData r4eUserPrefsGroupData = new GridData(GridData.FILL, GridData.FILL, true, false);
 		r4eUserPrefsGroupData.horizontalSpan = GROUP_PREFS_CONTAINER_DATA_SPAN;
 		r4EUserPrefsGroup.setText("User Preferences");
 		r4EUserPrefsGroup.setLayoutData(r4eUserPrefsGroupData);
 		r4EUserPrefsGroup.setLayout(new GridLayout(GROUP_PREFS_CONTAINER_DATA_SPAN, false));
 
 		//dummy spacer label
 		Label r4EUserPrefsSpacer = new Label(r4EUserPrefsGroup, SWT.FILL);
 		GridData r4EUserPrefsSpacerData = new GridData(GridData.FILL, GridData.FILL, true, false);
 		r4EUserPrefsSpacerData.horizontalSpan = GROUP_PREFS_CONTAINER_DATA_SPAN;
 		r4EUserPrefsSpacer.setLayoutData(r4EUserPrefsSpacerData);
 
 		final Label userIdLabel = new Label(r4EUserPrefsGroup, SWT.FILL);
 		final GridData userIdLabelData = new GridData(GridData.FILL, GridData.FILL, false, false);
		userIdLabel.setText(R4EUIConstants.NAME_LABEL);
 		userIdLabel.setLayoutData(userIdLabelData);
 
 		fUserIdTextField = new Text(r4EUserPrefsGroup, SWT.FILL | SWT.BORDER);
 		final GridData userIdTextData = new GridData(GridData.FILL, GridData.FILL, true, false);
 		if (R4EUIModelController.isJobInProgress()) {
 			fUserIdTextField.setEnabled(false);
 			fUserIdTextField.setEditable(false);
 		} else {
 			fUserIdTextField.setEnabled(true);
 			fUserIdTextField.setEditable(true);
 		}
 		fUserIdTextField.setLayoutData(userIdTextData);
 		fUserIdTextField.setText(store.getString(PreferenceConstants.P_USER_ID));
 		fUserIdTextField.addFocusListener(new FocusListener() {
 			public void focusLost(FocusEvent e) {
 				fUserEmailTextField.setText("");
 				if (R4EUIModelController.isUserQueryAvailable()) {
 					if (fUserIdTextField.getText().length() > 0) {
 						fUserIdTextField.setText(fUserIdTextField.getText().toLowerCase());
 						getShell().setCursor(getShell().getDisplay().getSystemCursor(SWT.CURSOR_WAIT));
 
 						final IQueryUser query = new QueryUserFactory().getInstance();
 						try {
 							final java.util.List<IUserInfo> users = query.searchByUserId(fUserIdTextField.getText());
 
 							//Set user Email if found
 							for (IUserInfo user : users) {
 								if (user.getUserId().toLowerCase().equals(fUserIdTextField.getText())) {
 									fUserEmailTextField.setText(user.getEmail());
 									break;
 								}
 							}
 						} catch (NamingException ex) {
 							R4EUIPlugin.Ftracer.traceError("Exception: " + ex.toString() + " (" + ex.getMessage() + ")");
 							R4EUIPlugin.getDefault().logError("Exception: " + ex.toString(), ex);
 						} catch (IOException ex) {
 							R4EUIPlugin.Ftracer.traceError("Exception: " + ex.toString() + " (" + ex.getMessage() + ")");
 							R4EUIPlugin.getDefault().logError("Exception: " + ex.toString(), ex);
 						} finally {
 							getShell().setCursor(getShell().getDisplay().getSystemCursor(SWT.CURSOR_ARROW));
 						}
 					}
 				}
 			}
 
 			public void focusGained(FocusEvent e) {
 				//Nothing to do
 			}
 		});
 
 		final Label userEmailLabel = new Label(r4EUserPrefsGroup, SWT.FILL);
 		final GridData userEmailLabelData = new GridData(GridData.FILL, GridData.FILL, false, false);
		userEmailLabel.setText(R4EUIConstants.NAME_LABEL);
 		userEmailLabel.setLayoutData(userEmailLabelData);
 
 		fUserEmailTextField = new Text(r4EUserPrefsGroup, SWT.FILL | SWT.BORDER);
 		final GridData userEmailTextData = new GridData(GridData.FILL, GridData.FILL, true, false);
 		if (R4EUIModelController.isJobInProgress()) {
 			fUserEmailTextField.setEnabled(false);
 			fUserEmailTextField.setEditable(false);
 		} else {
 			fUserEmailTextField.setEnabled(true);
 			fUserEmailTextField.setEditable(true);
 		}
 		fUserEmailTextField.setLayoutData(userEmailTextData);
 		fUserEmailTextField.setText(store.getString(PreferenceConstants.P_USER_EMAIL));
 
 		//dummy spacer label
 		r4EUserPrefsSpacer = new Label(r4EUserPrefsGroup, SWT.FILL);
 		r4EUserPrefsSpacerData = new GridData(GridData.FILL, GridData.FILL, true, false);
 		r4EUserPrefsSpacerData.horizontalSpan = GROUP_PREFS_CONTAINER_DATA_SPAN;
 		r4EUserPrefsSpacer.setLayoutData(r4EUserPrefsSpacerData);
 
 		//Use deltas for commit items?
 		fUseDeltasButton = new Button(r4EUserPrefsGroup, SWT.CHECK);
 		fUseDeltasButton.setText(PreferenceConstants.P_USE_DELTAS_LABEL);
 		fUseDeltasButton.setLayoutData(r4eUserPrefsGroupData);
 		fUseDeltasButton.setSelection(store.getBoolean(PreferenceConstants.P_USE_DELTAS));
 
 		//Automatically import/update postponed anomalies?
 		fAutoImportPostponedButton = new Button(r4EUserPrefsGroup, SWT.CHECK);
 		fAutoImportPostponedButton.setText(PreferenceConstants.P_AUTO_IMPORT_POSTPONED_LABEL);
 		fAutoImportPostponedButton.setLayoutData(r4eUserPrefsGroupData);
 		fAutoImportPostponedButton.setSelection(store.getBoolean(PreferenceConstants.P_AUTO_IMPORT_POSTPONED));
 	}
 
 	/**
 	 * Method createGroupPreferencesTab.
 	 * 
 	 * @param aParent
 	 *            Composite
 	 */
 	private void createGroupPreferencesTab(TabFolder aParent) {
 
 		final TabItem tabItem = new TabItem(aParent, SWT.NONE);
 		tabItem.setText("Groups");
 
 		// Create a Group to hold R4E Group preferences
 		final Group r4EGroupPrefsGroup = new Group(aParent, SWT.BORDER_SOLID);
 		tabItem.setControl(r4EGroupPrefsGroup);
 		final GridData r4EGroupPrefsGroupData = new GridData(GridData.FILL, GridData.FILL, true, false);
 		r4EGroupPrefsGroupData.horizontalSpan = GROUP_PREFS_CONTAINER_DATA_SPAN;
 		r4EGroupPrefsGroup.setText("Group Preferences");
 		r4EGroupPrefsGroup.setLayoutData(r4EGroupPrefsGroupData);
 		r4EGroupPrefsGroup.setLayout(new GridLayout(GROUP_PREFS_CONTAINER_DATA_SPAN, false));
 
 		//dummy spacer label
 		final Label r4EGroupPrefsSpacer = new Label(r4EGroupPrefsGroup, SWT.FILL); // $codepro.audit.disable variableUsage
 		final GridData r4EGroupPrefsSpacerData = new GridData(GridData.FILL, GridData.FILL, true, false);
 		r4EGroupPrefsSpacerData.horizontalSpan = GROUP_PREFS_CONTAINER_DATA_SPAN;
 		r4EGroupPrefsSpacer.setLayoutData(r4EGroupPrefsSpacerData);
 
 		// File Path Editor for Review Groups
 		final String[] extensions = { PreferenceConstants.P_GROUP_FILE_EXT };
 		final FilePathEditor groupFilesEditor = new FilePathEditor(PreferenceConstants.P_GROUP_FILE_PATH,
 				PreferenceConstants.P_GROUP_FILE_PATH_LABEL, extensions, r4EGroupPrefsGroup);
 		addField(groupFilesEditor);
 		if (R4EUIModelController.isJobInProgress()) {
 			groupFilesEditor.setEnabled(false, r4EGroupPrefsGroup);
 		} else {
 			groupFilesEditor.setEnabled(true, r4EGroupPrefsGroup);
 		}
 		final List filesList = groupFilesEditor.getListControl(r4EGroupPrefsGroup);
 		filesList.addSelectionListener(new SelectionListener() {
 
 			@SuppressWarnings("synthetic-access")
 			public void widgetSelected(SelectionEvent aEvent) {
 				final String selectedGroupFile = groupFilesEditor.getSelection();
 				if (null != selectedGroupFile) {
 					final R4EReviewGroup group = R4EUIModelController.peekReviewGroup(selectedGroupFile);
 					if (null != group) {
 						fGroupNameText.setText(group.getName());
 						fGroupDescriptionText.setText(group.getDescription());
 						R4EUIModelController.FModelExt.closeR4EReviewGroup(group);
 					} else {
 						fGroupNameText.setText(INVALID_FILE_STR);
 					}
 				} else {
 					fGroupNameText.setText("");
 					fGroupDescriptionText.setText("");
 				}
 			}
 
 			public void widgetDefaultSelected(SelectionEvent e) { // $codepro.audit.disable emptyMethod
 				//No implementation
 			}
 		});
 
 		//Group details
 		final Composite groupDetailsContainer = new Composite(r4EGroupPrefsGroup, SWT.NONE);
 		final GridData groupDetailsLayoutData = new GridData(GridData.FILL, GridData.FILL, false, false);
 		groupDetailsContainer.setLayoutData(groupDetailsLayoutData);
 		groupDetailsContainer.setLayout(new GridLayout(GROUP_PREFS_CONTAINER_DATA_SPAN, false));
 
 		final Label groupNameLabel = new Label(groupDetailsContainer, SWT.FILL);
 		final GridData groupNameLabelData = new GridData(GridData.FILL, GridData.FILL, false, false);
 		groupNameLabel.setText(R4EUIConstants.NAME_LABEL);
 		groupNameLabel.setLayoutData(groupNameLabelData);
 
 		fGroupNameText = new Text(groupDetailsContainer, SWT.FILL);
 		final GridData groupNameTextData = new GridData(GridData.FILL, GridData.FILL, true, false);
 		fGroupNameText.setEnabled(true);
 		fGroupNameText.setEditable(false);
 		fGroupNameText.setLayoutData(groupNameTextData);
 
 		final Label groupDescriptionLabel = new Label(groupDetailsContainer, SWT.NONE);
 		final GridData groupDescriptionLabelData = new GridData(GridData.FILL, GridData.FILL, false, false);
 		groupDescriptionLabel.setText(R4EUIConstants.DESCRIPTION_LABEL);
 		groupDescriptionLabel.setLayoutData(groupDescriptionLabelData);
 
 		fGroupDescriptionText = new Text(groupDetailsContainer, SWT.NONE);
 		final GridData groupDescriptionTextData = new GridData(GridData.FILL, GridData.FILL, true, false);
 		fGroupDescriptionText.setEnabled(true);
 		fGroupDescriptionText.setEditable(false);
 		fGroupDescriptionText.setLayoutData(groupDescriptionTextData);
 	}
 
 	/**
 	 * Method createRuleSetsPreferencesTab.
 	 * 
 	 * @param aParent
 	 *            Composite
 	 */
 	private void createRuleSetsPreferencesTab(TabFolder aParent) {
 
 		final TabItem tabItem = new TabItem(aParent, SWT.NONE);
 		tabItem.setText("Rule Sets");
 
 		// Create a Group to hold R4E Rule Set preferences
 		final Group r4ERuleSetPrefsGroup = new Group(aParent, SWT.BORDER_SOLID);
 		tabItem.setControl(r4ERuleSetPrefsGroup);
 		final GridData r4ERuleSetPrefsGroupData = new GridData(GridData.FILL, GridData.FILL, true, false);
 		r4ERuleSetPrefsGroupData.horizontalSpan = GROUP_PREFS_CONTAINER_DATA_SPAN;
 		r4ERuleSetPrefsGroup.setText("Rule Sets Preferences");
 		r4ERuleSetPrefsGroup.setLayoutData(r4ERuleSetPrefsGroupData);
 		r4ERuleSetPrefsGroup.setLayout(new GridLayout(GROUP_PREFS_CONTAINER_DATA_SPAN, false));
 
 		//dummy spacer label
 		final Label r4ERuleSetPrefsSpacer = new Label(r4ERuleSetPrefsGroup, SWT.FILL); // $codepro.audit.disable variableUsage
 		final GridData r4ERuleSetPrefsSpacerData = new GridData(GridData.FILL, GridData.FILL, true, false);
 		r4ERuleSetPrefsSpacerData.horizontalSpan = GROUP_PREFS_CONTAINER_DATA_SPAN;
 		r4ERuleSetPrefsSpacer.setLayoutData(r4ERuleSetPrefsSpacerData);
 
 		// File Path Editor for Rule Sets
 		final String[] ruleSetsExtensions = { PreferenceConstants.P_RULE_SET_FILE_EXT };
 		final FilePathEditor ruleSetFilesEditor = new FilePathEditor(PreferenceConstants.P_RULE_SET_FILE_PATH,
 				PreferenceConstants.P_RULE_SET_FILE_PATH_LABEL, ruleSetsExtensions, r4ERuleSetPrefsGroup);
 		addField(ruleSetFilesEditor);
 		if (R4EUIModelController.isJobInProgress()) {
 			ruleSetFilesEditor.setEnabled(false, r4ERuleSetPrefsGroup);
 		} else {
 			ruleSetFilesEditor.setEnabled(true, r4ERuleSetPrefsGroup);
 		}
 		final List ruleSetfilesList = ruleSetFilesEditor.getListControl(r4ERuleSetPrefsGroup);
 		ruleSetfilesList.addSelectionListener(new SelectionListener() {
 
 			@SuppressWarnings("synthetic-access")
 			public void widgetSelected(SelectionEvent aEvent) {
 				final String selectedRuleSetFile = ruleSetFilesEditor.getSelection();
 				if (null != selectedRuleSetFile) {
 					try {
 						final R4EDesignRuleCollection ruleSet = R4EUIModelController.peekRuleSet(selectedRuleSetFile);
 						fRuleSetNameText.setText(ruleSet.getName());
 						fRuleSetVersionText.setText(ruleSet.getVersion());
 						R4EUIModelController.FModelExt.closeR4EDesignRuleCollection(ruleSet);
 					} catch (ResourceHandlingException e) {
 						R4EUIPlugin.Ftracer.traceWarning("Exception: " + e.toString() + " (" + e.getMessage() + ")");
 						R4EUIPlugin.getDefault().logWarning("Exception: " + e.toString(), e);
 					}
 				} else {
 					fRuleSetNameText.setText("");
 					fRuleSetVersionText.setText("");
 				}
 			}
 
 			public void widgetDefaultSelected(SelectionEvent e) { // $codepro.audit.disable emptyMethod
 				//No implementation
 			}
 		});
 
 		//Group details
 		final Composite ruleSetDetailsContainer = new Composite(r4ERuleSetPrefsGroup, SWT.NONE);
 		final GridData ruleSetDetailsLayoutData = new GridData(GridData.FILL, GridData.FILL, false, false);
 		ruleSetDetailsContainer.setLayoutData(ruleSetDetailsLayoutData);
 		ruleSetDetailsContainer.setLayout(new GridLayout(GROUP_PREFS_CONTAINER_DATA_SPAN, false));
 
 		final Label ruleSetNameLabel = new Label(ruleSetDetailsContainer, SWT.FILL);
 		final GridData ruleSetNameLabelData = new GridData(GridData.FILL, GridData.FILL, false, false);
 		ruleSetNameLabel.setText(R4EUIConstants.NAME_LABEL);
 		ruleSetNameLabel.setLayoutData(ruleSetNameLabelData);
 
 		fRuleSetNameText = new Text(ruleSetDetailsContainer, SWT.FILL);
 		final GridData ruleSetNameTextData = new GridData(GridData.FILL, GridData.FILL, true, false);
 		fRuleSetNameText.setEnabled(true);
 		fRuleSetNameText.setEditable(false);
 		fRuleSetNameText.setLayoutData(ruleSetNameTextData);
 
 		final Label ruleSetVersionLabel = new Label(ruleSetDetailsContainer, SWT.NONE);
 		final GridData ruleSetVersionLabelData = new GridData(GridData.FILL, GridData.FILL, false, false);
 		ruleSetVersionLabel.setText(R4EUIConstants.VERSION_LABEL);
 		ruleSetVersionLabel.setLayoutData(ruleSetVersionLabelData);
 
 		fRuleSetVersionText = new Text(ruleSetDetailsContainer, SWT.NONE);
 		final GridData ruleSetVersionTextData = new GridData(GridData.FILL, GridData.FILL, true, false);
 		fRuleSetVersionText.setEnabled(true);
 		fRuleSetVersionText.setEditable(false);
 		fRuleSetVersionText.setLayoutData(ruleSetVersionTextData);
 	}
 
 	/**
 	 * Method createFiltersPreferencesTab.
 	 * 
 	 * @param aParent
 	 *            Composite
 	 */
 	private void createFiltersPreferencesTab(TabFolder aParent) {
 
 		final TabItem tabItem = new TabItem(aParent, SWT.NONE);
 		tabItem.setText("Filters");
 
 		// Create a Group to hold R4E Navigator view default filters
 		final Group r4EFilterPrefsGroup = new Group(aParent, SWT.BORDER_SOLID);
 		tabItem.setControl(r4EFilterPrefsGroup);
 		final GridData r4EFilterPrefsGroupData = new GridData(GridData.FILL, GridData.FILL, true, false);
 		r4EFilterPrefsGroupData.horizontalSpan = GROUP_PREFS_CONTAINER_DATA_SPAN;
 		r4EFilterPrefsGroup.setText("Default Filters");
 		r4EFilterPrefsGroup.setLayoutData(r4EFilterPrefsGroupData);
 		r4EFilterPrefsGroup.setLayout(new GridLayout(GROUP_PREFS_CONTAINER_DATA_SPAN, false));
 
 		//dummy spacer label
 		final Label r4ERuleSetPrefsSpacer = new Label(r4EFilterPrefsGroup, SWT.FILL); // $codepro.audit.disable variableUsage
 		final GridData r4ERuleSetPrefsSpacerData = new GridData(GridData.FILL, GridData.FILL, true, false);
 		r4ERuleSetPrefsSpacerData.horizontalSpan = GROUP_PREFS_CONTAINER_DATA_SPAN;
 		r4ERuleSetPrefsSpacer.setLayoutData(r4ERuleSetPrefsSpacerData);
 
 		//Filers checkboxes
 		final IPreferenceStore store = R4EUIPlugin.getDefault().getPreferenceStore();
 		fReviewShowDisabledButton = new Button(r4EFilterPrefsGroup, SWT.CHECK);
 		fReviewShowDisabledButton.setText(R4EUIConstants.SHOW_DISABLED_FILTER_NAME);
 		fReviewShowDisabledButton.setLayoutData(r4EFilterPrefsGroupData);
 		fReviewShowDisabledButton.setSelection(store.getBoolean(PreferenceConstants.P_SHOW_DISABLED));
 
 		fReviewsCompletedFilterButton = new Button(r4EFilterPrefsGroup, SWT.CHECK);
 		fReviewsCompletedFilterButton.setText(R4EUIConstants.REVIEWS_COMPLETED_FILTER_NAME);
 		fReviewsCompletedFilterButton.setLayoutData(r4EFilterPrefsGroupData);
 		fReviewsCompletedFilterButton.setSelection(store.getBoolean(PreferenceConstants.P_REVIEWS_COMPLETED_FILTER));
 
 		fReviewsOnlyFilterButton = new Button(r4EFilterPrefsGroup, SWT.CHECK);
 		fReviewsOnlyFilterButton.setText(R4EUIConstants.REVIEWS_ONLY_FILTER_NAME);
 		fReviewsOnlyFilterButton.setLayoutData(r4EFilterPrefsGroupData);
 		fReviewsOnlyFilterButton.setSelection(store.getBoolean(PreferenceConstants.P_REVIEWS_ONLY_FILTER));
 
 		fReviewMyFilterButton = new Button(r4EFilterPrefsGroup, SWT.CHECK);
 		fReviewMyFilterButton.setText(R4EUIConstants.REVIEWS_MY_FILTER_NAME);
 		fReviewMyFilterButton.setLayoutData(r4EFilterPrefsGroupData);
 		fReviewMyFilterButton.setSelection(store.getBoolean(PreferenceConstants.P_REVIEWS_MY_FILTER));
 
 		fParticipantFilterButton = new Button(r4EFilterPrefsGroup, SWT.CHECK);
 		fParticipantFilterButton.setLayoutData(new GridData(GridData.FILL, GridData.FILL, false, false));
 		fParticipantFilterButton.setText(R4EUIConstants.REVIEWS_PARTICIPANT_FILTER_NAME);
 		fParticipantIdText = new Text(r4EFilterPrefsGroup, SWT.BORDER);
 		fParticipantIdText.setLayoutData(new GridData(GridData.FILL, GridData.FILL, false, false));
 		if (store.getString(PreferenceConstants.P_PARTICIPANT_FILTER).equals("")) {
 			fParticipantFilterButton.setSelection(false);
 			fParticipantFilterButton.setEnabled(false);
 			fParticipantIdText.setText("");
 		} else {
 			fParticipantFilterButton.setSelection(true);
 			fParticipantIdText.setText(store.getString(PreferenceConstants.P_PARTICIPANT_FILTER));
 		}
 		fParticipantIdText.addModifyListener(new ModifyListener() {
 			@SuppressWarnings("synthetic-access")
 			public void modifyText(ModifyEvent e) {
 				if (fParticipantIdText.getCharCount() > 0) {
 					fParticipantFilterButton.setEnabled(true);
 				} else {
 					fParticipantFilterButton.setEnabled(false);
 				}
 			}
 		});
 
 		fAnomaliesFilterButton = new Button(r4EFilterPrefsGroup, SWT.CHECK);
 		fAnomaliesFilterButton.setText(R4EUIConstants.ANOMALIES_FILTER_NAME);
 		fAnomaliesFilterButton.setLayoutData(r4EFilterPrefsGroupData);
 		fAnomaliesFilterButton.setSelection(store.getBoolean(PreferenceConstants.P_ANOMALIES_ALL_FILTER));
 
 		fAnomaliesMyFilterButton = new Button(r4EFilterPrefsGroup, SWT.CHECK);
 		fAnomaliesMyFilterButton.setText(R4EUIConstants.ANOMALIES_MY_FILTER_NAME);
 		fAnomaliesMyFilterButton.setLayoutData(r4EFilterPrefsGroupData);
 		fAnomaliesMyFilterButton.setSelection(store.getBoolean(PreferenceConstants.P_ANOMALIES_MY_FILTER));
 
 		fReviewedItemsFilterButton = new Button(r4EFilterPrefsGroup, SWT.CHECK);
 		fReviewedItemsFilterButton.setText(R4EUIConstants.REVIEWED_ELEMS_FILTER_NAME);
 		fReviewedItemsFilterButton.setLayoutData(r4EFilterPrefsGroupData);
 		fReviewedItemsFilterButton.setSelection(store.getBoolean(PreferenceConstants.P_REVIEWED_ITEMS_FILTER));
 
 		fHideRuleSetsFilterButton = new Button(r4EFilterPrefsGroup, SWT.CHECK);
 		fHideRuleSetsFilterButton.setText(R4EUIConstants.HIDE_RULE_SETS_FILTER_NAME);
 		fHideRuleSetsFilterButton.setLayoutData(r4EFilterPrefsGroupData);
 		fHideRuleSetsFilterButton.setSelection(store.getBoolean(PreferenceConstants.P_HIDE_RULE_SETS_FILTER));
 
 		fHideDeltasFilterButton = new Button(r4EFilterPrefsGroup, SWT.CHECK);
 		fHideDeltasFilterButton.setText(R4EUIConstants.HIDE_DELTAS_FILTER_NAME);
 		fHideDeltasFilterButton.setLayoutData(r4EFilterPrefsGroupData);
 		fHideDeltasFilterButton.setSelection(store.getBoolean(PreferenceConstants.P_HIDE_DELTAS_FILTER));
 	}
 
 	/**
 	 * Method init.
 	 * 
 	 * @param workbench
 	 *            IWorkbench
 	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(IWorkbench)
 	 */
 	public void init(IWorkbench workbench) { // $codepro.audit.disable emptyMethod
 	}
 
 	/**
 	 * Method performDefaults.
 	 * 
 	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
 	 */
 	@Override
 	protected void performDefaults() {
 
 		final IPreferenceStore store = R4EUIPlugin.getDefault().getPreferenceStore();
 		PreferenceConstants.setUserEmailDefaultPreferences();
 
 		store.setValue(PreferenceConstants.P_USE_DELTAS, true);
 		fUseDeltasButton.setSelection(true);
 		fAutoImportPostponedButton.setSelection(false);
 
 		//Remove all Filters
 		store.setValue(PreferenceConstants.P_SHOW_DISABLED, false);
 		fReviewShowDisabledButton.setSelection(false);
 		store.setValue(PreferenceConstants.P_REVIEWS_COMPLETED_FILTER, false);
 		fReviewsCompletedFilterButton.setSelection(false);
 		store.setValue(PreferenceConstants.P_REVIEWS_ONLY_FILTER, false);
 		fReviewsOnlyFilterButton.setSelection(false);
 		store.setValue(PreferenceConstants.P_ANOMALIES_MY_FILTER, false);
 		fAnomaliesMyFilterButton.setSelection(false);
 		store.setValue(PreferenceConstants.P_REVIEWS_MY_FILTER, false);
 		fReviewMyFilterButton.setSelection(false);
 		store.setValue(PreferenceConstants.P_PARTICIPANT_FILTER, "");
 		fParticipantFilterButton.setSelection(false);
 		fParticipantIdText.setText("");
 		store.setValue(PreferenceConstants.P_ANOMALIES_ALL_FILTER, false);
 		fAnomaliesFilterButton.setSelection(false);
 		store.setValue(PreferenceConstants.P_REVIEWED_ITEMS_FILTER, false);
 		fReviewedItemsFilterButton.setSelection(false);
 		store.setValue(PreferenceConstants.P_HIDE_RULE_SETS_FILTER, false);
 		fHideRuleSetsFilterButton.setSelection(false);
 		store.setValue(PreferenceConstants.P_HIDE_DELTAS_FILTER, true);
 		fHideDeltasFilterButton.setSelection(true);
 
 		//For field editors
 		super.performDefaults();
 	}
 
 	/**
 	 * Method performOk.
 	 * 
 	 * @return boolean
 	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
 	 */
 	@Override
 	public boolean performOk() {
 		final IPreferenceStore store = R4EUIPlugin.getDefault().getPreferenceStore();
 
 		//Set curerent User Id
 		store.setValue(PreferenceConstants.P_USER_ID, fUserIdTextField.getText().toLowerCase());
 
 		//Set preferences for default filters and apply them
 		store.setValue(PreferenceConstants.P_SHOW_DISABLED, fReviewShowDisabledButton.getSelection());
 		store.setValue(PreferenceConstants.P_REVIEWS_COMPLETED_FILTER, fReviewsCompletedFilterButton.getSelection());
 		store.setValue(PreferenceConstants.P_REVIEWS_ONLY_FILTER, fReviewsOnlyFilterButton.getSelection());
 		store.setValue(PreferenceConstants.P_ANOMALIES_MY_FILTER, fAnomaliesMyFilterButton.getSelection());
 		store.setValue(PreferenceConstants.P_REVIEWS_MY_FILTER, fReviewMyFilterButton.getSelection());
 		if (fParticipantFilterButton.getSelection()) {
 			final String filterUserId = fParticipantIdText.getText().toLowerCase();
 			if (filterUserId.equals(store.getString(PreferenceConstants.P_USER_ID))) {
 				//Set my filter instead
 				store.setValue(PreferenceConstants.P_REVIEWS_MY_FILTER, true);
 			} else {
 				store.setValue(PreferenceConstants.P_PARTICIPANT_FILTER, filterUserId);
 			}
 		} else {
 			store.setValue(PreferenceConstants.P_PARTICIPANT_FILTER, "");
 			fParticipantIdText.setText("");
 		}
 		store.setValue(PreferenceConstants.P_ANOMALIES_ALL_FILTER, fAnomaliesFilterButton.getSelection());
 		store.setValue(PreferenceConstants.P_REVIEWED_ITEMS_FILTER, fReviewedItemsFilterButton.getSelection());
 		store.setValue(PreferenceConstants.P_HIDE_RULE_SETS_FILTER, fHideRuleSetsFilterButton.getSelection());
 		store.setValue(PreferenceConstants.P_HIDE_DELTAS_FILTER, fHideDeltasFilterButton.getSelection());
 
 		if (null != R4EUIModelController.getNavigatorView()) {
 			R4EUIModelController.getNavigatorView().applyDefaultFilters();
 		}
 
 		store.setValue(PreferenceConstants.P_USE_DELTAS, fUseDeltasButton.getSelection());
 		store.setValue(PreferenceConstants.P_AUTO_IMPORT_POSTPONED, fAutoImportPostponedButton.getSelection());
 
 		if (CommandUtils.isEmailValid(fUserEmailTextField.getText())) {
 			store.setValue(PreferenceConstants.P_USER_EMAIL, fUserEmailTextField.getText());
 		} else {
 			//Validation of input failed
 			return false;
 		}
 
 		//For field editors
 		return super.performOk();
 	}
 }
