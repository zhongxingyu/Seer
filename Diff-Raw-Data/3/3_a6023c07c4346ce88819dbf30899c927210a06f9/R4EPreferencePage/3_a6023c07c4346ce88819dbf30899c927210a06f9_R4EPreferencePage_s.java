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
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import javax.naming.NamingException;
 
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.commands.NotEnabledException;
 import org.eclipse.core.commands.NotHandledException;
 import org.eclipse.core.commands.common.NotDefinedException;
 import org.eclipse.jface.preference.FieldEditorPreferencePage;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EReviewGroup;
 import org.eclipse.mylyn.reviews.r4e.core.model.drules.R4EDesignRuleCollection;
 import org.eclipse.mylyn.reviews.r4e.core.model.serial.impl.CompatibilityException;
 import org.eclipse.mylyn.reviews.r4e.core.model.serial.impl.ResourceHandlingException;
 import org.eclipse.mylyn.reviews.r4e.ui.R4EUIPlugin;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.editors.FilePathEditor;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIModelController;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIReviewGroup;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.navigator.ReviewNavigatorActionGroup;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.utils.CommandUtils;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.utils.EditableListWidget;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.utils.IEditableListListener;
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
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Item;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.List;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.TabFolder;
 import org.eclipse.swt.widgets.TabItem;
 import org.eclipse.swt.widgets.TableItem;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.IWorkbenchPreferencePage;
 
 /**
  * @author lmcdubo
  * @version $Revision: 1.0 $
  */
 public class R4EPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage,
 		IEditableListListener {
 
 	// ------------------------------------------------------------------------
 	// Constants
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Field PREFS_CONTAINER_DATA_SPAN. (value is 1)
 	 */
 	private static final int PREFS_CONTAINER_DATA_SPAN = 1;
 
 	/**
 	 * Field R4E_PREFS_CONTAINER_DATA_SPAN. (value is 4)
 	 */
 	private static final int GROUP_PREFS_CONTAINER_DATA_SPAN = 4; // $codepro.audit.disable constantNamingConvention
 
 	/**
 	 * Field INVALID_FILE_STR. (value is ""<File not found>"")
 	 */
 	private static final String INVALID_FILE_STR = "<File not found>"; //$NON-NLS-1$
 
 	/**
 	 * Field USERS_GROUPS_LABEL. (value is ""Participants Lists: "")
 	 */
 	private static final String PARTICIPANTS_LISTS_LABEL = "Participants Lists: "; //$NON-NLS-1$
 
 	// ------------------------------------------------------------------------
 	// Member Variables
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Field fR4EPreferencesTabFolder.
 	 */
 	private TabFolder fR4EPrefsTabFolder = null;
 
 	/**
 	 * Field fUserPrefsTab.
 	 */
 	private TabItem fUserPrefsTab = null;
 
 	/**
 	 * Field fGroupsPrefsTab.
 	 */
 	private TabItem fGroupsPrefsTab = null;
 
 	/**
 	 * Field fRuleSetsPrefsTab.
 	 */
 	private TabItem fRuleSetsPrefsTab = null;
 
 	/**
 	 * Field fFiltersPrefsTab.
 	 */
 	private TabItem fFiltersPrefsTab = null;
 
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
 	 * Field fR4EGroupPrefsGroup.
 	 */
 	private Composite fR4EGroupPrefsGroup = null;
 
 	/**
 	 * Field fGroupFilesEditor.
 	 */
 	private FilePathEditor fGroupFilesEditor = null;
 
 	/**
 	 * Field fGroupNameText.
 	 */
 	private Text fGroupNameText = null;
 
 	/**
 	 * Field fGroupDescriptionText.
 	 */
 	private Text fGroupDescriptionText = null;
 
 	/**
 	 * Field fR4ERuleSetPrefsGroup.
 	 */
 	private Composite fR4ERuleSetPrefsGroup = null;
 
 	/**
 	 * Field fRuleSetFilesEditor.
 	 */
 	private FilePathEditor fRuleSetFilesEditor = null;
 
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
 	 * Field fAssignMyFilterButton.
 	 */
 	private Button fAssignMyFilterButton = null;
 
 	/**
 	 * Field fAssignFilterButton.
 	 */
 	private Button fAssignFilterButton = null;
 
 	/**
 	 * Field fUnassignFilterButton.
 	 */
 	private Button fUnassignFilterButton = null;
 
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
 
 	/**
 	 * Field fAssignIdText.
 	 */
 	private Text fAssignIdText = null;
 
 	/**
 	 * Field fGroupsList.
 	 */
 	private EditableListWidget fParticipantsLists;
 
 	/**
 	 * Field fUsersList.
 	 */
 	private EditableListWidget fParticipantsList;
 
 	/**
 	 * Field fUsersGroupsHashMap.
 	 */
 	private static final Map<String, java.util.List<String>> FParticipantsListsHashMap = new HashMap<String, java.util.List<String>>();
 
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
 		populateParticipantListMap();
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
 
 		R4EUIPlugin.Ftracer.traceInfo("Build R4E Preference page"); //$NON-NLS-1$
 
 		//The Main preferences composite
 		final Composite prefsContainer = new Composite(getFieldEditorParent(), SWT.NONE);
 		final GridData prefsContainerData = new GridData(GridData.FILL, GridData.FILL, true, true);
 		prefsContainerData.horizontalSpan = PREFS_CONTAINER_DATA_SPAN;
 		prefsContainer.setLayoutData(prefsContainerData);
 		final GridLayout prefsLayout = new GridLayout(PREFS_CONTAINER_DATA_SPAN, false);
 		prefsContainer.setLayout(prefsLayout);
 
 		fR4EPrefsTabFolder = new TabFolder(prefsContainer, SWT.TOP);
 		final GridData tabFolderData = new GridData(GridData.FILL, GridData.FILL, true, true);
 		fR4EPrefsTabFolder.setLayoutData(tabFolderData);
 
 		createUserPreferencesTab(fR4EPrefsTabFolder);
 		createGroupPreferencesTab(fR4EPrefsTabFolder);
 		createRuleSetsPreferencesTab(fR4EPrefsTabFolder);
 		createFiltersPreferencesTab(fR4EPrefsTabFolder);
 	}
 
 	/**
 	 * Method createUserPreferencesTab.
 	 * 
 	 * @param aParent
 	 *            Composite
 	 */
 	private void createUserPreferencesTab(TabFolder aParent) {
 
 		final IPreferenceStore store = R4EUIPlugin.getDefault().getPreferenceStore();
 
 		fUserPrefsTab = new TabItem(aParent, SWT.NONE);
 		fUserPrefsTab.setText("User"); //$NON-NLS-1$
 
 		// Create a Group to hold R4E user preferences
 		final Composite r4EUserPrefsGroup = new Composite(aParent, SWT.NONE);
 		fUserPrefsTab.setControl(r4EUserPrefsGroup);
 		final GridData r4eUserPrefsGroupData = new GridData(GridData.FILL, GridData.FILL, true, true);
 		r4eUserPrefsGroupData.horizontalSpan = GROUP_PREFS_CONTAINER_DATA_SPAN;
 		r4EUserPrefsGroup.setLayoutData(r4eUserPrefsGroupData);
 		r4EUserPrefsGroup.setLayout(new GridLayout(GROUP_PREFS_CONTAINER_DATA_SPAN, false));
 
 		//dummy spacer label
 		final Label r4EUserPrefsSpacer = new Label(r4EUserPrefsGroup, SWT.FILL);
 		final GridData r4EUserPrefsSpacerData = new GridData(GridData.FILL, GridData.FILL, true, false);
 		r4EUserPrefsSpacerData.horizontalSpan = GROUP_PREFS_CONTAINER_DATA_SPAN;
 		r4EUserPrefsSpacer.setLayoutData(r4EUserPrefsSpacerData);
 
 		final Label userIdLabel = new Label(r4EUserPrefsGroup, SWT.FILL);
 		final GridData userIdLabelData = new GridData(GridData.BEGINNING, GridData.FILL, false, false);
 		userIdLabelData.horizontalSpan = 1;
 		userIdLabel.setText(PreferenceConstants.P_USER_ID_LABEL);
 		userIdLabel.setLayoutData(userIdLabelData);
 
 		fUserIdTextField = new Text(r4EUserPrefsGroup, SWT.FILL | SWT.BORDER);
 		final GridData userIdTextData = new GridData(GridData.FILL, GridData.FILL, true, false);
 		userIdTextData.horizontalSpan = 3;
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
 				fUserEmailTextField.setText(""); //$NON-NLS-1$
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
 							R4EUIPlugin.Ftracer.traceError("Exception: " + ex.toString() + " (" + ex.getMessage() + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 							R4EUIPlugin.getDefault().logError("Exception: " + ex.toString(), ex); //$NON-NLS-1$
 						} catch (IOException ex) {
 							R4EUIPlugin.Ftracer.traceError("Exception: " + ex.toString() + " (" + ex.getMessage() + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 							R4EUIPlugin.getDefault().logError("Exception: " + ex.toString(), ex); //$NON-NLS-1$
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
 		final GridData userEmailLabelData = new GridData(GridData.BEGINNING, GridData.FILL, false, false);
 		userEmailLabelData.horizontalSpan = 1;
 		userEmailLabel.setText(PreferenceConstants.P_USER_EMAIL_LABEL);
 		userEmailLabel.setLayoutData(userEmailLabelData);
 
 		fUserEmailTextField = new Text(r4EUserPrefsGroup, SWT.FILL | SWT.BORDER);
 		final GridData userEmailTextData = new GridData(GridData.FILL, GridData.FILL, true, false);
 		userEmailTextData.horizontalSpan = 3;
 		if (R4EUIModelController.isJobInProgress()) {
 			fUserEmailTextField.setEnabled(false);
 			fUserEmailTextField.setEditable(false);
 		} else {
 			fUserEmailTextField.setEnabled(true);
 			fUserEmailTextField.setEditable(true);
 		}
 		fUserEmailTextField.setLayoutData(userEmailTextData);
 		fUserEmailTextField.setText(store.getString(PreferenceConstants.P_USER_EMAIL));
 
 		//Participants Lists
 		Label label = new Label(r4EUserPrefsGroup, SWT.NONE);
 		label.setText(PARTICIPANTS_LISTS_LABEL);
 		label.setToolTipText(R4EUIConstants.PARTICIPANTS_LISTS_TOOLTIP);
 		final GridData participantsListsLabelGridData = new GridData(GridData.BEGINNING, GridData.BEGINNING, false,
 				false);
 		participantsListsLabelGridData.horizontalSpan = 1;
 		label.setLayoutData(participantsListsLabelGridData);
 		final GridData participantsListsGridData = new GridData(GridData.FILL, GridData.FILL, true, false);
 		participantsListsGridData.horizontalSpan = 3;
 		fParticipantsLists = new EditableListWidget(null, r4EUserPrefsGroup, participantsListsGridData, this, 0,
 				Text.class, null);
 		fParticipantsLists.setToolTipText(R4EUIConstants.PARTICIPANTS_LISTS_TOOLTIP);
 
 		//Get Participants Lists from preferences to initially populate the list
 		final String[] participantsListsStr = store.getString(PreferenceConstants.P_PARTICIPANTS_LISTS).split(
 				R4EUIConstants.LIST_SEPARATOR);
 		fParticipantsLists.removeAll();
 		Item item = null;
 		String participantsListStr = null;
 		for (int i = 0; i < participantsListsStr.length; i++) {
 			participantsListStr = participantsListsStr[i];
 			if (null != participantsListStr && !("".equals(participantsListStr))) { //$NON-NLS-1$
 				if (i >= fParticipantsLists.getItemCount()) {
 					item = fParticipantsLists.addItem();
 				} else {
 					item = fParticipantsLists.getItem(i);
 					if (null == item) {
 						item = fParticipantsLists.addItem();
 					}
 				}
 				item.setText(participantsListStr);
 			}
 		}
 		fParticipantsLists.updateButtons();
 
 		//Participants
 		label = new Label(r4EUserPrefsGroup, SWT.NONE);
 		label.setText(R4EUIConstants.PARTICIPANTS_LABEL);
 		label.setToolTipText(R4EUIConstants.PARTICIPANTS_TOOLTIP);
 		final GridData participantsLabelGridData = new GridData(GridData.BEGINNING, GridData.BEGINNING, false, true);
 		participantsLabelGridData.horizontalSpan = 1;
 		label.setLayoutData(participantsLabelGridData);
 		final GridData participantsGridData = new GridData(GridData.FILL, GridData.FILL, true, true);
 		participantsGridData.horizontalSpan = 3;
 		fParticipantsList = new EditableListWidget(null, r4EUserPrefsGroup, participantsGridData, this, 1, Label.class,
 				null);
 		fParticipantsList.setToolTipText(R4EUIConstants.PARTICIPANTS_TOOLTIP);
 		fParticipantsList.removeAll();
 		fParticipantsList.setEnabled(false);
 
 		final GridData filtersButtonData = new GridData(GridData.FILL, GridData.FILL, true, false);
 		filtersButtonData.horizontalSpan = GROUP_PREFS_CONTAINER_DATA_SPAN;
 
 		//Use deltas for commit items?
 		fUseDeltasButton = new Button(r4EUserPrefsGroup, SWT.CHECK);
 		fUseDeltasButton.setText(PreferenceConstants.P_USE_DELTAS_LABEL);
 		fUseDeltasButton.setLayoutData(filtersButtonData);
 		fUseDeltasButton.setSelection(store.getBoolean(PreferenceConstants.P_USE_DELTAS));
 	}
 
 	/**
 	 * Method createGroupPreferencesTab.
 	 * 
 	 * @param aParent
 	 *            Composite
 	 */
 	private void createGroupPreferencesTab(TabFolder aParent) {
 
 		fGroupsPrefsTab = new TabItem(aParent, SWT.NONE);
 		fGroupsPrefsTab.setText("Review Groups"); //$NON-NLS-1$
 
 		// Create a Group to hold R4E Group preferences
 		fR4EGroupPrefsGroup = new Composite(aParent, SWT.NONE);
 		fGroupsPrefsTab.setControl(fR4EGroupPrefsGroup);
 		final GridData r4EGroupPrefsGroupData = new GridData(GridData.FILL, GridData.FILL, true, true);
 		r4EGroupPrefsGroupData.horizontalSpan = GROUP_PREFS_CONTAINER_DATA_SPAN;
 		fR4EGroupPrefsGroup.setLayoutData(r4EGroupPrefsGroupData);
 		fR4EGroupPrefsGroup.setLayout(new GridLayout(GROUP_PREFS_CONTAINER_DATA_SPAN, false));
 
 		//dummy spacer label
 		final Label r4EGroupPrefsSpacer = new Label(fR4EGroupPrefsGroup, SWT.FILL); // $codepro.audit.disable variableUsage
 		final GridData r4EGroupPrefsSpacerData = new GridData(GridData.FILL, GridData.FILL, true, false);
 		r4EGroupPrefsSpacerData.horizontalSpan = GROUP_PREFS_CONTAINER_DATA_SPAN;
 		r4EGroupPrefsSpacer.setLayoutData(r4EGroupPrefsSpacerData);
 
 		// File Path Editor for Review Groups
 		final String[] extensions = { PreferenceConstants.P_GROUP_FILE_EXT };
 		fGroupFilesEditor = new FilePathEditor(PreferenceConstants.P_GROUP_FILE_PATH,
 				PreferenceConstants.P_GROUP_FILE_PATH_LABEL, extensions, fR4EGroupPrefsGroup);
 		addField(fGroupFilesEditor);
 		if (R4EUIModelController.isJobInProgress()) {
 			fGroupFilesEditor.setEnabled(false, fR4EGroupPrefsGroup);
 		} else {
 			fGroupFilesEditor.setEnabled(true, fR4EGroupPrefsGroup);
 		}
 		final List filesList = fGroupFilesEditor.getListControl(fR4EGroupPrefsGroup);
 		filesList.addSelectionListener(new SelectionListener() {
 
 			@SuppressWarnings("synthetic-access")
 			public void widgetSelected(SelectionEvent aEvent) {
 				final String selectedGroupFile = fGroupFilesEditor.getSelection();
 				fGroupNameText.setText(""); //$NON-NLS-1$
 				fGroupDescriptionText.setText(""); //$NON-NLS-1$
 				if (null != selectedGroupFile) {
 					try {
 						final R4EReviewGroup group = R4EUIModelController.peekReviewGroup(selectedGroupFile);
 						if (null != group) {
 							fGroupNameText.setText(group.getName());
 							fGroupDescriptionText.setText(group.getDescription());
 							R4EUIModelController.FModelExt.closeR4EReviewGroup(group);
 						} else {
 							fGroupNameText.setText(INVALID_FILE_STR);
 						}
 					} catch (ResourceHandlingException e) {
 						R4EUIPlugin.Ftracer.traceWarning("Exception: " + e.toString() + " (" + e.getMessage() + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 						R4EUIPlugin.getDefault().logWarning("Exception: " + e.toString(), e); //$NON-NLS-1$
 						fGroupDescriptionText.setText("<Error:  Resource Error>"); //$NON-NLS-1$
 					} catch (CompatibilityException e) {
 						R4EUIPlugin.Ftracer.traceWarning("Exception: " + e.toString() + " (" + e.getMessage() + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 						R4EUIPlugin.getDefault().logWarning("Exception: " + e.toString(), e); //$NON-NLS-1$
 						fGroupDescriptionText.setText("<Error:  Version Mismatch>"); //$NON-NLS-1$
 					}
 				}
 			}
 
 			public void widgetDefaultSelected(SelectionEvent e) { // $codepro.audit.disable emptyMethod
 				//No implementation
 			}
 		});
 
 		//Group details
 		final Composite groupDetailsContainer = new Composite(fR4EGroupPrefsGroup, SWT.NONE);
 		final GridData groupDetailsLayoutData = new GridData(GridData.FILL, GridData.FILL, false, false);
 		groupDetailsContainer.setLayoutData(groupDetailsLayoutData);
 		groupDetailsContainer.setLayout(new GridLayout(GROUP_PREFS_CONTAINER_DATA_SPAN, false));
 
 		final Label groupNameLabel = new Label(groupDetailsContainer, SWT.FILL);
 		final GridData groupNameLabelData = new GridData(GridData.FILL, GridData.FILL, false, false);
 		groupNameLabel.setText(R4EUIConstants.NAME_LABEL);
 		groupNameLabel.setLayoutData(groupNameLabelData);
 
 		fGroupNameText = new Text(groupDetailsContainer, SWT.FILL);
 		final GridData groupNameTextData = new GridData(GridData.FILL, GridData.FILL, true, false);
 		groupNameTextData.horizontalSpan = 3;
 		fGroupNameText.setEnabled(true);
 		fGroupNameText.setEditable(false);
 		fGroupNameText.setLayoutData(groupNameTextData);
 
 		final Label groupDescriptionLabel = new Label(groupDetailsContainer, SWT.NONE);
 		final GridData groupDescriptionLabelData = new GridData(GridData.FILL, GridData.FILL, false, false);
 		groupDescriptionLabel.setText(R4EUIConstants.DESCRIPTION_LABEL);
 		groupDescriptionLabel.setLayoutData(groupDescriptionLabelData);
 
 		fGroupDescriptionText = new Text(groupDetailsContainer, SWT.NONE);
 		final GridData groupDescriptionTextData = new GridData(GridData.FILL, GridData.FILL, true, false);
 		groupDescriptionTextData.horizontalSpan = 3;
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
 
 		fRuleSetsPrefsTab = new TabItem(aParent, SWT.NONE);
 		fRuleSetsPrefsTab.setText("Rule Sets"); //$NON-NLS-1$
 
 		// Create a Group to hold R4E Rule Set preferences
 		fR4ERuleSetPrefsGroup = new Composite(aParent, SWT.NONE);
 		fRuleSetsPrefsTab.setControl(fR4ERuleSetPrefsGroup);
 		final GridData r4ERuleSetPrefsGroupData = new GridData(GridData.FILL, GridData.FILL, true, true);
 		r4ERuleSetPrefsGroupData.horizontalSpan = GROUP_PREFS_CONTAINER_DATA_SPAN;
 		fR4ERuleSetPrefsGroup.setLayoutData(r4ERuleSetPrefsGroupData);
 		fR4ERuleSetPrefsGroup.setLayout(new GridLayout(GROUP_PREFS_CONTAINER_DATA_SPAN, false));
 
 		//dummy spacer label
 		final Label r4ERuleSetPrefsSpacer = new Label(fR4ERuleSetPrefsGroup, SWT.FILL); // $codepro.audit.disable variableUsage
 		final GridData r4ERuleSetPrefsSpacerData = new GridData(GridData.FILL, GridData.FILL, true, false);
 		r4ERuleSetPrefsSpacerData.horizontalSpan = GROUP_PREFS_CONTAINER_DATA_SPAN;
 		r4ERuleSetPrefsSpacer.setLayoutData(r4ERuleSetPrefsSpacerData);
 
 		// File Path Editor for Rule Sets
 		final String[] ruleSetsExtensions = { PreferenceConstants.P_RULE_SET_FILE_EXT };
 		fRuleSetFilesEditor = new FilePathEditor(PreferenceConstants.P_RULE_SET_FILE_PATH,
 				PreferenceConstants.P_RULE_SET_FILE_PATH_LABEL, ruleSetsExtensions, fR4ERuleSetPrefsGroup);
 		addField(fRuleSetFilesEditor);
 		if (R4EUIModelController.isJobInProgress()) {
 			fRuleSetFilesEditor.setEnabled(false, fR4ERuleSetPrefsGroup);
 		} else {
 			fRuleSetFilesEditor.setEnabled(true, fR4ERuleSetPrefsGroup);
 		}
 		final List ruleSetfilesList = fRuleSetFilesEditor.getListControl(fR4ERuleSetPrefsGroup);
 		ruleSetfilesList.addSelectionListener(new SelectionListener() {
 
 			@SuppressWarnings("synthetic-access")
 			public void widgetSelected(SelectionEvent aEvent) {
 				final String selectedRuleSetFile = fRuleSetFilesEditor.getSelection();
 				fRuleSetNameText.setText(""); //$NON-NLS-1$
 				fRuleSetVersionText.setText(""); //$NON-NLS-1$
 				if (null != selectedRuleSetFile) {
 					try {
 						final R4EDesignRuleCollection ruleSet = R4EUIModelController.peekRuleSet(selectedRuleSetFile);
 						if (null != ruleSet) {
 							fRuleSetNameText.setText(ruleSet.getName());
 							fRuleSetVersionText.setText(ruleSet.getVersion());
 							R4EUIModelController.FModelExt.closeR4EDesignRuleCollection(ruleSet);
 						} else {
 							fRuleSetNameText.setText(INVALID_FILE_STR);
 						}
 					} catch (ResourceHandlingException e) {
 						R4EUIPlugin.Ftracer.traceWarning("Exception: " + e.toString() + " (" + e.getMessage() + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 						R4EUIPlugin.getDefault().logWarning("Exception: " + e.toString(), e); //$NON-NLS-1$
 						fRuleSetVersionText.setText("<Error:  Resource Error>"); //$NON-NLS-1$
 					} catch (CompatibilityException e) {
 						R4EUIPlugin.Ftracer.traceWarning("Exception: " + e.toString() + " (" + e.getMessage() + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 						R4EUIPlugin.getDefault().logWarning("Exception: " + e.toString(), e); //$NON-NLS-1$
 						fRuleSetVersionText.setText("<Error:  Version Mismatch>"); //$NON-NLS-1$
 					}
 				}
 			}
 
 			public void widgetDefaultSelected(SelectionEvent e) { // $codepro.audit.disable emptyMethod
 				//No implementation
 			}
 		});
 
 		//Group details
 		final Composite ruleSetDetailsContainer = new Composite(fR4ERuleSetPrefsGroup, SWT.NONE);
 		final GridData ruleSetDetailsLayoutData = new GridData(GridData.FILL, GridData.FILL, false, false);
 		ruleSetDetailsContainer.setLayoutData(ruleSetDetailsLayoutData);
 		ruleSetDetailsContainer.setLayout(new GridLayout(GROUP_PREFS_CONTAINER_DATA_SPAN, false));
 
 		final Label ruleSetNameLabel = new Label(ruleSetDetailsContainer, SWT.FILL);
 		final GridData ruleSetNameLabelData = new GridData(GridData.FILL, GridData.FILL, false, false);
 		ruleSetNameLabel.setText(R4EUIConstants.NAME_LABEL);
 		ruleSetNameLabel.setLayoutData(ruleSetNameLabelData);
 
 		fRuleSetNameText = new Text(ruleSetDetailsContainer, SWT.FILL);
 		final GridData ruleSetNameTextData = new GridData(GridData.FILL, GridData.FILL, true, false);
 		ruleSetNameTextData.horizontalSpan = 3;
 		fRuleSetNameText.setEnabled(true);
 		fRuleSetNameText.setEditable(false);
 		fRuleSetNameText.setLayoutData(ruleSetNameTextData);
 
 		final Label ruleSetVersionLabel = new Label(ruleSetDetailsContainer, SWT.NONE);
 		final GridData ruleSetVersionLabelData = new GridData(GridData.FILL, GridData.FILL, false, false);
 		ruleSetVersionLabel.setText(R4EUIConstants.VERSION_LABEL);
 		ruleSetVersionLabel.setLayoutData(ruleSetVersionLabelData);
 
 		fRuleSetVersionText = new Text(ruleSetDetailsContainer, SWT.NONE);
 		final GridData ruleSetVersionTextData = new GridData(GridData.FILL, GridData.FILL, true, false);
 		ruleSetVersionTextData.horizontalSpan = 3;
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
 
 		fFiltersPrefsTab = new TabItem(aParent, SWT.NONE);
 		fFiltersPrefsTab.setText("Default Filters"); //$NON-NLS-1$
 
 		// Create a Group to hold R4E Navigator view default filters
 		final Composite r4EFilterPrefsGroup = new Composite(aParent, SWT.NONE);
 		fFiltersPrefsTab.setControl(r4EFilterPrefsGroup);
 		final GridData r4EFilterPrefsGroupData = new GridData(GridData.FILL, GridData.FILL, true, true);
 		r4EFilterPrefsGroupData.horizontalSpan = GROUP_PREFS_CONTAINER_DATA_SPAN;
 		r4EFilterPrefsGroup.setLayoutData(r4EFilterPrefsGroupData);
 		r4EFilterPrefsGroup.setLayout(new GridLayout(GROUP_PREFS_CONTAINER_DATA_SPAN, false));
 
 		//dummy spacer label
 		final Label r4ERuleSetPrefsSpacer = new Label(r4EFilterPrefsGroup, SWT.FILL); // $codepro.audit.disable variableUsage
 		final GridData r4ERuleSetPrefsSpacerData = new GridData(GridData.FILL, GridData.FILL, true, false);
 		r4ERuleSetPrefsSpacerData.horizontalSpan = GROUP_PREFS_CONTAINER_DATA_SPAN;
 		r4ERuleSetPrefsSpacer.setLayoutData(r4ERuleSetPrefsSpacerData);
 
 		//Filers checkboxes
 		final GridData filtersButtonData = new GridData(GridData.FILL, GridData.FILL, true, false);
 		filtersButtonData.horizontalSpan = GROUP_PREFS_CONTAINER_DATA_SPAN;
 
 		final IPreferenceStore store = R4EUIPlugin.getDefault().getPreferenceStore();
 		fReviewShowDisabledButton = new Button(r4EFilterPrefsGroup, SWT.CHECK);
 		fReviewShowDisabledButton.setText(R4EUIConstants.SHOW_DISABLED_FILTER_NAME);
 		fReviewShowDisabledButton.setLayoutData(filtersButtonData);
 		fReviewShowDisabledButton.setSelection(store.getBoolean(PreferenceConstants.P_SHOW_DISABLED));
 		if (null != R4EUIModelController.getNavigatorView()
 				&& R4EUIModelController.getNavigatorView().isDefaultDisplay()) {
 			fReviewShowDisabledButton.setEnabled(true);
 		} else {
 			fReviewShowDisabledButton.setEnabled(false);
 		}
 
 		fReviewsCompletedFilterButton = new Button(r4EFilterPrefsGroup, SWT.CHECK);
 		fReviewsCompletedFilterButton.setText(R4EUIConstants.REVIEWS_COMPLETED_FILTER_NAME);
 		fReviewsCompletedFilterButton.setLayoutData(filtersButtonData);
 		fReviewsCompletedFilterButton.setSelection(store.getBoolean(PreferenceConstants.P_REVIEWS_COMPLETED_FILTER));
 
 		fReviewsOnlyFilterButton = new Button(r4EFilterPrefsGroup, SWT.CHECK);
 		fReviewsOnlyFilterButton.setText(R4EUIConstants.REVIEWS_ONLY_FILTER_NAME);
 		fReviewsOnlyFilterButton.setLayoutData(filtersButtonData);
 		fReviewsOnlyFilterButton.setSelection(store.getBoolean(PreferenceConstants.P_REVIEWS_ONLY_FILTER));
 
 		fReviewMyFilterButton = new Button(r4EFilterPrefsGroup, SWT.CHECK);
 		fReviewMyFilterButton.setText(R4EUIConstants.REVIEWS_MY_FILTER_NAME);
 		fReviewMyFilterButton.setLayoutData(filtersButtonData);
 		fReviewMyFilterButton.setSelection(store.getBoolean(PreferenceConstants.P_REVIEWS_MY_FILTER));
 
 		fParticipantFilterButton = new Button(r4EFilterPrefsGroup, SWT.CHECK);
 		fParticipantFilterButton.setLayoutData(new GridData(GridData.FILL, GridData.FILL, false, false));
 		fParticipantFilterButton.setText(R4EUIConstants.REVIEWS_PARTICIPANT_FILTER_NAME);
 		fParticipantIdText = new Text(r4EFilterPrefsGroup, SWT.BORDER);
 		fParticipantIdText.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));
 		if (store.getString(PreferenceConstants.P_PARTICIPANT_FILTER).equals("")) { //$NON-NLS-1$
 			fParticipantFilterButton.setSelection(false);
 			fParticipantFilterButton.setEnabled(false);
 			fParticipantIdText.setText(""); //$NON-NLS-1$
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
 
 		fAssignMyFilterButton = new Button(r4EFilterPrefsGroup, SWT.CHECK);
 		fAssignMyFilterButton.setText(R4EUIConstants.ASSIGN_MY_FILTER_NAME);
 		fAssignMyFilterButton.setLayoutData(filtersButtonData);
 		fAssignMyFilterButton.setSelection(store.getBoolean(PreferenceConstants.P_ASSIGN_MY_FILTER));
 		fAssignMyFilterButton.addListener(SWT.Selection, new Listener() {
 			public void handleEvent(Event event) {
 				if (fAssignMyFilterButton.getSelection()) {
 					fAssignIdText.setEnabled(false);
 					fAssignFilterButton.setEnabled(false);
 					fUnassignFilterButton.setEnabled(false);
 				} else {
 					fAssignIdText.setEnabled(true);
 					if (fAssignIdText.getCharCount() > 0) {
 						fAssignFilterButton.setEnabled(true);
 					} else {
 						fAssignFilterButton.setEnabled(false);
 					}
 					fUnassignFilterButton.setEnabled(true);
 				}
 			}
 		});
 
 		fAssignFilterButton = new Button(r4EFilterPrefsGroup, SWT.CHECK);
 		fAssignFilterButton.setLayoutData(new GridData(GridData.FILL, GridData.FILL, false, false));
 		fAssignFilterButton.setText(R4EUIConstants.ASSIGN_FILTER_NAME);
 		fAssignIdText = new Text(r4EFilterPrefsGroup, SWT.BORDER);
 		fAssignIdText.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));
 		if (store.getString(PreferenceConstants.P_PARTICIPANT_FILTER).equals("")) { //$NON-NLS-1$
 			fAssignFilterButton.setSelection(false);
 			fAssignFilterButton.setEnabled(false);
 			fAssignIdText.setText(""); //$NON-NLS-1$
 		} else {
 			fAssignFilterButton.setSelection(true);
 			fAssignIdText.setText(store.getString(PreferenceConstants.P_PARTICIPANT_FILTER));
 		}
 		fAssignIdText.addModifyListener(new ModifyListener() {
 			@SuppressWarnings("synthetic-access")
 			public void modifyText(ModifyEvent e) {
 				if (fAssignIdText.getCharCount() > 0) {
 					fAssignFilterButton.setEnabled(true);
 				} else {
 					fAssignFilterButton.setEnabled(false);
 				}
 			}
 		});
 		fAssignFilterButton.addListener(SWT.Selection, new Listener() {
 			public void handleEvent(Event event) {
 				if (fAssignFilterButton.getSelection()) {
 					fAssignMyFilterButton.setEnabled(false);
 					fUnassignFilterButton.setEnabled(false);
 				} else {
 					fAssignMyFilterButton.setEnabled(true);
 					fUnassignFilterButton.setEnabled(true);
 				}
 			}
 		});
 
 		fUnassignFilterButton = new Button(r4EFilterPrefsGroup, SWT.CHECK);
 		fUnassignFilterButton.setText(R4EUIConstants.UNASSIGN_FILTER_NAME);
 		fUnassignFilterButton.setLayoutData(filtersButtonData);
 		fUnassignFilterButton.setSelection(store.getBoolean(PreferenceConstants.P_UNASSIGN_FILTER));
 		fUnassignFilterButton.addListener(SWT.Selection, new Listener() {
 			public void handleEvent(Event event) {
 				if (fUnassignFilterButton.getSelection()) {
 					fAssignMyFilterButton.setEnabled(false);
 					fAssignIdText.setEnabled(false);
 					fAssignFilterButton.setEnabled(false);
 				} else {
 					fAssignMyFilterButton.setEnabled(true);
 					fAssignIdText.setEnabled(true);
 					if (fAssignIdText.getCharCount() > 0) {
 						fAssignFilterButton.setEnabled(true);
 					} else {
 						fAssignFilterButton.setEnabled(false);
 					}
 				}
 			}
 		});
 
 		fAnomaliesFilterButton = new Button(r4EFilterPrefsGroup, SWT.CHECK);
 		fAnomaliesFilterButton.setText(R4EUIConstants.ANOMALIES_FILTER_NAME);
 		fAnomaliesFilterButton.setLayoutData(filtersButtonData);
 		fAnomaliesFilterButton.setSelection(store.getBoolean(PreferenceConstants.P_ANOMALIES_ALL_FILTER));
 
 		fAnomaliesMyFilterButton = new Button(r4EFilterPrefsGroup, SWT.CHECK);
 		fAnomaliesMyFilterButton.setText(R4EUIConstants.ANOMALIES_MY_FILTER_NAME);
 		fAnomaliesMyFilterButton.setLayoutData(filtersButtonData);
 		fAnomaliesMyFilterButton.setSelection(store.getBoolean(PreferenceConstants.P_ANOMALIES_MY_FILTER));
 
 		fReviewedItemsFilterButton = new Button(r4EFilterPrefsGroup, SWT.CHECK);
 		fReviewedItemsFilterButton.setText(R4EUIConstants.REVIEWED_ELEMS_FILTER_NAME);
 		fReviewedItemsFilterButton.setLayoutData(filtersButtonData);
 		fReviewedItemsFilterButton.setSelection(store.getBoolean(PreferenceConstants.P_REVIEWED_ITEMS_FILTER));
 
 		fHideRuleSetsFilterButton = new Button(r4EFilterPrefsGroup, SWT.CHECK);
 		fHideRuleSetsFilterButton.setText(R4EUIConstants.HIDE_RULE_SETS_FILTER_NAME);
 		fHideRuleSetsFilterButton.setLayoutData(filtersButtonData);
 		fHideRuleSetsFilterButton.setSelection(store.getBoolean(PreferenceConstants.P_HIDE_RULE_SETS_FILTER));
 
 		fHideDeltasFilterButton = new Button(r4EFilterPrefsGroup, SWT.CHECK);
 		fHideDeltasFilterButton.setText(R4EUIConstants.HIDE_DELTAS_FILTER_NAME);
 		fHideDeltasFilterButton.setLayoutData(filtersButtonData);
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
 
 		//Set default filters and options
 		store.setValue(PreferenceConstants.P_USE_DELTAS, true);
 		fUseDeltasButton.setSelection(true);
 		store.setValue(PreferenceConstants.P_REVIEWS_COMPLETED_FILTER, true);
 		fReviewsCompletedFilterButton.setSelection(true);
 		store.setValue(PreferenceConstants.P_HIDE_DELTAS_FILTER, true);
 		fHideDeltasFilterButton.setSelection(true);
 
 		//Remove non-default Filters
 		store.setValue(PreferenceConstants.P_SHOW_DISABLED, false);
 		fReviewShowDisabledButton.setSelection(false);
 		store.setValue(PreferenceConstants.P_REVIEWS_ONLY_FILTER, false);
 		fReviewsOnlyFilterButton.setSelection(false);
 		store.setValue(PreferenceConstants.P_REVIEWS_MY_FILTER, false);
 		fReviewMyFilterButton.setSelection(false);
 		store.setValue(PreferenceConstants.P_PARTICIPANT_FILTER, ""); //$NON-NLS-1$
 		fParticipantFilterButton.setSelection(false);
 		fParticipantIdText.setText(""); //$NON-NLS-1$
 		store.setValue(PreferenceConstants.P_ASSIGN_MY_FILTER, false);
 		fAssignMyFilterButton.setSelection(false);
 		fAssignMyFilterButton.setEnabled(true);
 		store.setValue(PreferenceConstants.P_ASSIGN_FILTER, false);
 		fAssignFilterButton.setSelection(false);
 		fAssignIdText.setEnabled(true);
 		fAssignIdText.setText(""); //$NON-NLS-1$
 		store.setValue(PreferenceConstants.P_UNASSIGN_FILTER, false);
 		fUnassignFilterButton.setSelection(false);
 		fUnassignFilterButton.setEnabled(true);
 		store.setValue(PreferenceConstants.P_ANOMALIES_ALL_FILTER, false);
 		fAnomaliesFilterButton.setSelection(false);
 		store.setValue(PreferenceConstants.P_ANOMALIES_MY_FILTER, false);
 		fAnomaliesMyFilterButton.setSelection(false);
 		store.setValue(PreferenceConstants.P_REVIEWED_ITEMS_FILTER, false);
 		fReviewedItemsFilterButton.setSelection(false);
 		store.setValue(PreferenceConstants.P_HIDE_RULE_SETS_FILTER, false);
 		fHideRuleSetsFilterButton.setSelection(false);
 
 		//For field editors
 		super.performDefaults();
 
 		//Here, since we erase all group data, we need to make sure that we are in the default display view
 		if (!R4EUIModelController.getNavigatorView().isDefaultDisplay()) {
 			checkToChangeDisplay();
 		}
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
 
 		//Set current User Id
 		store.setValue(PreferenceConstants.P_USER_ID, fUserIdTextField.getText().toLowerCase());
 
 		//Set current groups and groups users
 		final Item[] groupItems = fParticipantsLists.getItems();
 		final StringBuffer buffer = new StringBuffer();
 		for (Item item : groupItems) {
 			buffer.append(item.getText() + R4EUIConstants.LIST_SEPARATOR);
 		}
 		store.setValue(PreferenceConstants.P_PARTICIPANTS_LISTS, buffer.toString());
 		store.setValue(PreferenceConstants.P_PARTICIPANTS, formatParticipantsListsPreferences());
 
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
 			store.setValue(PreferenceConstants.P_PARTICIPANT_FILTER, ""); //$NON-NLS-1$
 			fParticipantIdText.setText(""); //$NON-NLS-1$
 		}
 		store.setValue(PreferenceConstants.P_ASSIGN_MY_FILTER, fAssignMyFilterButton.getSelection());
 		if (fAssignFilterButton.getSelection()) {
 			final String filterUserId = fAssignIdText.getText().toLowerCase();
 			if (filterUserId.equals(store.getString(PreferenceConstants.P_USER_ID))) {
 				//Set my filter instead
 				store.setValue(PreferenceConstants.P_ASSIGN_MY_FILTER, true);
 			} else {
 				store.setValue(PreferenceConstants.P_ASSIGN_FILTER, filterUserId);
 			}
 		} else {
 			store.setValue(PreferenceConstants.P_ASSIGN_FILTER, ""); //$NON-NLS-1$
 			fAssignIdText.setText(""); //$NON-NLS-1$
 		}
 		store.setValue(PreferenceConstants.P_UNASSIGN_FILTER, fUnassignFilterButton.getSelection());
 
 		store.setValue(PreferenceConstants.P_ANOMALIES_ALL_FILTER, fAnomaliesFilterButton.getSelection());
 		store.setValue(PreferenceConstants.P_REVIEWED_ITEMS_FILTER, fReviewedItemsFilterButton.getSelection());
 		store.setValue(PreferenceConstants.P_HIDE_RULE_SETS_FILTER, fHideRuleSetsFilterButton.getSelection());
 		store.setValue(PreferenceConstants.P_HIDE_DELTAS_FILTER, fHideDeltasFilterButton.getSelection());
 
 		if (null != R4EUIModelController.getNavigatorView()
 				&& !R4EUIModelController.getNavigatorView().getTreeViewer().getTree().isDisposed()) {
 			R4EUIModelController.getNavigatorView().applyDefaultFilters();
 		}
 		store.setValue(PreferenceConstants.P_USE_DELTAS, fUseDeltasButton.getSelection());
 
 		if (CommandUtils.isEmailValid(fUserEmailTextField.getText())) {
 			store.setValue(PreferenceConstants.P_USER_EMAIL, fUserEmailTextField.getText());
 		} else {
 			//Validation of input failed
 			return false;
 		}
 
		if (!R4EUIModelController.getNavigatorView().isDefaultDisplay()) {
 			checkToChangeDisplay();
 		}
 
 		//For field editors
 		return super.performOk();
 	}
 
 	private void checkToChangeDisplay() {
 		//Verify if we are removing the active review's parent group from preferences
 		if (null != R4EUIModelController.getNavigatorView()
 				&& !R4EUIModelController.getNavigatorView().getTreeViewer().getTree().isDisposed()) {
 			boolean parentGroupRemoved = true;
 			if (null != R4EUIModelController.getActiveReview()) {
 				String[] groupFiles = fGroupFilesEditor.getListControl(fR4EGroupPrefsGroup).getItems();
 				for (String groupFile : groupFiles) {
 					if (groupFile.equals(((R4EUIReviewGroup) R4EUIModelController.getActiveReview().getParent()).getReviewGroup()
 							.eResource()
 							.getURI()
 							.toFileString())) {
 						parentGroupRemoved = false;
 						break;
 					}
 				}
 			}
 
 			if (parentGroupRemoved) {
 				//We are currently removing the active review's parent group.  If we are in the TreeTable display, revert back to default Tree display
 				try {
 					((ReviewNavigatorActionGroup) R4EUIModelController.getNavigatorView().getActionSet()).changeDisplayCommand();
 					R4EUIModelController.getNavigatorView().resetInput();
 				} catch (ExecutionException e) {
 					R4EUIPlugin.getDefault().logError("Exception: " + e.toString(), e); //$NON-NLS-1$
 				} catch (NotDefinedException e) {
 					R4EUIPlugin.getDefault().logError("Exception: " + e.toString(), e); //$NON-NLS-1$
 				} catch (NotEnabledException e) {
 					R4EUIPlugin.getDefault().logError("Exception: " + e.toString(), e); //$NON-NLS-1$
 				} catch (NotHandledException e) {
 					R4EUIPlugin.getDefault().logError("Exception: " + e.toString(), e); //$NON-NLS-1$
 				}
 			}
 		}
 	}
 
 	//Getters and Setters.  These are used in JUnit testing and could
 	//	also be used in headless mode
 
 	/**
 	 * Method getUser.
 	 * 
 	 * @return String
 	 */
 	public String getUser() {
 		return fUserIdTextField.getText();
 
 	}
 
 	/**
 	 * Method setUser.
 	 * 
 	 * @param aUser
 	 *            - String
 	 */
 	public void setUser(String aUser) {
 		fUserIdTextField.setText(aUser);
 	}
 
 	/**
 	 * Method addGroupPrefs.
 	 * 
 	 * @param aGroupPath
 	 */
 	public void addGroupPrefs(String aGroupPath) {
 		fR4EPrefsTabFolder.setSelection(fGroupsPrefsTab);
 		fGroupFilesEditor.getListControl(fR4EGroupPrefsGroup).add(aGroupPath);
 	}
 
 	/**
 	 * Method removeGroupPrefs.
 	 * 
 	 * @param aGroupPath
 	 */
 	public void removeGroupPrefs(String aGroupPath) {
 		fR4EPrefsTabFolder.setSelection(fGroupsPrefsTab);
 		fGroupFilesEditor.getListControl(fR4EGroupPrefsGroup).remove(aGroupPath);
 	}
 
 	/**
 	 * Method addRuleSetPrefs.
 	 * 
 	 * @param aRuleSetPath
 	 *            String
 	 */
 	public void addRuleSetPrefs(String aRuleSetPath) {
 		fR4EPrefsTabFolder.setSelection(fRuleSetsPrefsTab);
 		fRuleSetFilesEditor.getListControl(fR4ERuleSetPrefsGroup).add(aRuleSetPath);
 	}
 
 	/**
 	 * Method removeRuleSetPrefs.
 	 * 
 	 * @param aRuleSetPath
 	 *            String
 	 */
 	public void removeRuleSetPrefs(String aRuleSetPath) {
 		fR4EPrefsTabFolder.setSelection(fRuleSetsPrefsTab);
 		fRuleSetFilesEditor.getListControl(fR4ERuleSetPrefsGroup).remove(aRuleSetPath);
 	}
 
 	/**
 	 * Method itemsUpdated (callback).
 	 * 
 	 * @param aItems
 	 * @param aInstanceId
 	 */
 	public void itemsUpdated(Item[] aItems, int aInstanceId) {
 		if (0 == aInstanceId) {
 			//Iterate the map and remove elements that are not present anymore
 			final Iterator<Map.Entry<String, java.util.List<String>>> entries = FParticipantsListsHashMap.entrySet()
 					.iterator();
 			boolean itemFound;
 			while (entries.hasNext()) {
 				itemFound = false;
 				Map.Entry<String, java.util.List<String>> entry = entries.next();
 				for (Item item : aItems) {
 					if (item.getText().equals(entry.getKey())) {
 						itemFound = true;
 						continue;
 					}
 				}
 				if (!itemFound) {
 					FParticipantsListsHashMap.remove(entry.getKey());
 				}
 			}
 
 			//Remove users from group table if needed
 			if (null == fParticipantsLists.getSelectedItem()) {
 				fParticipantsList.removeAll();
 				fParticipantsList.setEnabled(false);
 			}
 		} else if (1 == aInstanceId) {
 			final Item selectedGroup = fParticipantsLists.getSelectedItem();
 			if (null != selectedGroup) {
 				FParticipantsListsHashMap.remove(selectedGroup.getText());
 				final java.util.List<String> newParticipants = new ArrayList<String>(aItems.length);
 				for (Item item : aItems) {
 					StringBuffer buffer = new StringBuffer();
 					int numColumns = ((TableItem) item).getParent().getColumnCount();
 					for (int i = 0; i < numColumns; i++) {
 						buffer.append(((TableItem) item).getText(i) + R4EUIConstants.LIST_SEPARATOR);
 					}
 					buffer.deleteCharAt(buffer.length() - 1); //Strip last separator
 					newParticipants.add(buffer.toString());
 				}
 				FParticipantsListsHashMap.put(selectedGroup.getText(), newParticipants);
 			}
 		}
 	}
 
 	/**
 	 * Method itemSelected (callback).
 	 * 
 	 * @param aItem
 	 * @param aInstanceId
 	 */
 	public void itemSelected(Item aItem, int aInstanceId) {
 		if (0 == aInstanceId) {
 			if (null != aItem) {
 				fParticipantsList.setEnabled(true);
 			} else {
 				fParticipantsList.removeAll();
 				return;
 			}
 			//Populate the users table for the selected participant list
 			final java.util.List<String> participantsStr = FParticipantsListsHashMap.get(aItem.getText());
 			fParticipantsList.removeAll();
 			Item item = null;
 			String participantStr = null;
 			if (null != participantsStr) {
 				for (int i = 0; i < participantsStr.size(); i++) {
 					participantStr = participantsStr.get(i);
 					if (i >= fParticipantsList.getItemCount()) {
 						item = fParticipantsList.addItem();
 					} else {
 						item = fParticipantsList.getItem(i);
 						if (null == item) {
 							item = fParticipantsList.addItem();
 						}
 					}
 					((TableItem) item).setText(participantStr.split(R4EUIConstants.LIST_SEPARATOR));
 				}
 				fParticipantsList.updateButtons();
 			}
 		}
 	}
 
 	/**
 	 * Method populateParticipantListMap
 	 */
 	public static void populateParticipantListMap() {
 		//Format in preferences is <number of lists>;<list1 name>;<number of participant included>;<participant1>;...;<participantN>;...;<listN name>...
 		final IPreferenceStore store = R4EUIPlugin.getDefault().getPreferenceStore();
 		final String[] participantListStrs = store.getString(PreferenceConstants.P_PARTICIPANTS).split(
 				R4EUIConstants.LIST_SEPARATOR);
 		if (null != participantListStrs && null != participantListStrs[0] && !("".equals(participantListStrs[0]))) { //$NON-NLS-1$
 			int readIndex = 0;
 			final int numLists = Integer.parseInt(participantListStrs[readIndex++]);
 			for (int i = 0; i < numLists; i++) {
 				String participantListName = participantListStrs[readIndex++];
 				int numParticipants = Integer.parseInt(participantListStrs[readIndex++]);
 				java.util.List<String> users = new ArrayList<String>(numParticipants);
 				for (int j = 0; j < numParticipants; j++) {
 					users.add(participantListStrs[readIndex++] + R4EUIConstants.LIST_SEPARATOR
 							+ participantListStrs[readIndex++]); //UserId;User Email
 				}
 				FParticipantsListsHashMap.put(participantListName, users);
 			}
 		}
 	}
 
 	/**
 	 * Method formatParticipantsListsPreferences
 	 * 
 	 * @return String
 	 */
 	private String formatParticipantsListsPreferences() {
 		//Format in preferences is <number of lists>;<list1 name>;<number of participant included>;<participant1>;...;<participantN>;...;<listN name>...
 		final StringBuffer buffer = new StringBuffer();
 
 		buffer.append(FParticipantsListsHashMap.size() + R4EUIConstants.LIST_SEPARATOR);
 		final Iterator<Map.Entry<String, java.util.List<String>>> participantsLists = FParticipantsListsHashMap.entrySet()
 				.iterator();
 		while (participantsLists.hasNext()) {
 			Map.Entry<String, java.util.List<String>> participantList = participantsLists.next();
 			buffer.append(participantList.getKey() + R4EUIConstants.LIST_SEPARATOR);
 			java.util.List<String> participants = participantList.getValue();
 			buffer.append(participants.size() + R4EUIConstants.LIST_SEPARATOR);
 			for (String participant : participants) {
 				buffer.append(participant + R4EUIConstants.LIST_SEPARATOR);
 			}
 		}
 		return buffer.toString();
 	}
 
 	/**
 	 * Method getParticipantsFromList
 	 * 
 	 * @param aQueryStr
 	 *            - String
 	 * @return java.util.List<String>
 	 */
 	public static java.util.List<String> getParticipantsFromList(String aQueryStr) {
 		java.util.List<String> participants = FParticipantsListsHashMap.get(aQueryStr);
 		if (null == participants) {
 			participants = new ArrayList<String>();
 		}
 		if (0 == participants.size()) {
 			participants.add(aQueryStr); //No group exists with this name, just piggyback the query
 		}
 		return participants;
 	}
 
 	/**
 	 * Method getParticipantsLists
 	 * 
 	 * @return String[]
 	 */
 	public static String[] getParticipantsLists() {
 		final java.util.List<String> activeParticipantsLists = new ArrayList<String>();
 		final Iterator<Map.Entry<String, java.util.List<String>>> participantsLists = FParticipantsListsHashMap.entrySet()
 				.iterator();
 		while (participantsLists.hasNext()) {
 			Map.Entry<String, java.util.List<String>> participantList = participantsLists.next();
 			if (participantList.getValue().size() > 0) {
 				activeParticipantsLists.add(participantList.getKey());
 			}
 		}
 		return activeParticipantsLists.toArray(new String[0]);
 	}
 }
