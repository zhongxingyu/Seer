 // $codepro.audit.disable com.instantiations.assist.eclipse.analysis.audit.rule.effectivejava.alwaysOverridetoString.alwaysOverrideToString, com.instantiations.assist.eclipse.analysis.audit.rule.effectivejava.constructorsOnlyInvokeFinalMethods, com.instantiations.assist.eclipse.analysis.deserializeabilitySecurity, com.instantiations.assist.eclipse.analysis.disallowReturnMutable, com.instantiations.assist.eclipse.analysis.enforceCloneableUsageSecurity, explicitThisUsage
 /*******************************************************************************
  * Copyright (c) 2010, 2012 Ericsson AB and others.
  * 
  * All rights reserved. This program and the accompanying materials are
  * made available under the terms of the Eclipse Public License v1.0 which
  * accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Description:
  * 
  * This class implements the dialog used to fill-in the Review element details
  * This is a modal dialog
  * 
  * Contributors:
  *   Sebastien Dubois - Created for Mylyn Review R4E project
  *   
  ******************************************************************************/
 
 package org.eclipse.mylyn.reviews.r4e.ui.internal.dialogs;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.jface.dialogs.ErrorDialog;
 import org.eclipse.jface.dialogs.IDialogConstants;
 import org.eclipse.jface.dialogs.IInputValidator;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.window.Window;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EReviewType;
 import org.eclipse.mylyn.reviews.r4e.ui.R4EUIPlugin;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIModelController;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIReviewGroup;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.utils.EditableListWidget;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.utils.R4EUIConstants;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.CCombo;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Item;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.forms.FormDialog;
 import org.eclipse.ui.forms.IManagedForm;
 import org.eclipse.ui.forms.events.ExpansionAdapter;
 import org.eclipse.ui.forms.events.ExpansionEvent;
 import org.eclipse.ui.forms.widgets.ExpandableComposite;
 import org.eclipse.ui.forms.widgets.FormToolkit;
 import org.eclipse.ui.forms.widgets.ScrolledForm;
 import org.eclipse.ui.forms.widgets.Section;
 
 /**
  * @author Sebastien Dubois
  * @version $Revision: 1.0 $
  */
 public class ReviewInputDialog extends FormDialog implements IReviewInputDialog {
 
 	// ------------------------------------------------------------------------
 	// Constants
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Field ADD_REVIEW_DIALOG_TITLE. (value is ""Enter Review details"")
 	 */
 	private static final String ADD_REVIEW_DIALOG_TITLE = "Enter Review Details";
 
 	/**
 	 * Field ADD_REVIEW_TYPE_DIALOG_VALUE. (value is ""Review Type: "")
 	 */
 	private static final String ADD_REVIEW_TYPE_DIALOG_VALUE = "Review Type: ";
 
 	/**
 	 * Field ADD_REVIEW_NAME_DIALOG_VALUE. (value is ""Review Name: "")
 	 */
 	private static final String ADD_REVIEW_NAME_DIALOG_VALUE = "Review Name: ";
 
 	/**
 	 * Field ADD_REVIEW_DESCRIPTION_DIALOG_VALUE. (value is ""Review Description: "")
 	 */
 	private static final String ADD_REVIEW_DESCRIPTION_DIALOG_VALUE = "Review Description: ";
 
 	/**
 	 * Field BASIC_PARAMS_HEADER_MSG. (value is ""Enter the mandatory basic parameters for this Review Group"")
 	 */
 	private static final String BASIC_PARAMS_HEADER_MSG = "Enter the mandatory basic parameters for this Review";
 
 	/**
 	 * Field EXTRA_PARAMS_HEADER_MSG. (value is ""Enter the optional extra parameters for this Review Group"")
 	 */
 	private static final String EXTRA_PARAMS_HEADER_MSG = "Enter the optional extra parameters for this Review";
 
 	/**
 	 * Field ADD_REVIEW_GROUP_PROJECT_DIALOG_VALUE. (value is ""Available Projects:"")
 	 */
 	private static final String ADD_REVIEW_PROJECT_DIALOG_VALUE = "Project:";
 
 	/**
 	 * Field ADD_REVIEW_GROUP_COMPONENTS_DIALOG_VALUE. (value is ""Available Components:"")
 	 */
 	private static final String ADD_REVIEW_COMPONENTS_DIALOG_VALUE = "Components:";
 
 	/**
 	 * Field ADD_REVIEW_GROUP_ENTRY_CRITERIA_DIALOG_VALUE. (value is ""Default Entry Criteria:"")
 	 */
 	private static final String ADD_REVIEW_ENTRY_CRITERIA_DIALOG_VALUE = "Entry Criteria:";
 
 	/**
 	 * Field ADD_REVIEW_GROUP_ENTRY_CRITERIA_DIALOG_VALUE. (value is ""Default Entry Criteria:"")
 	 */
 	private static final String ADD_REVIEW_OBJECTIVES_DIALOG_VALUE = "Objectives:";
 
 	/**
 	 * Field ADD_REVIEW_GROUP_ENTRY_CRITERIA_DIALOG_VALUE. (value is ""Default Entry Criteria:"")
 	 */
 	private static final String ADD_REVIEW_REFERENCE_MATERIAL_DIALOG_VALUE = "Reference Material:";
 
 	/**
 	 * Field REVIEW_TYPES.
 	 */
 	private static final String[] REVIEW_TYPES = { R4EUIConstants.REVIEW_TYPE_BASIC,
 			R4EUIConstants.REVIEW_TYPE_INFORMAL, R4EUIConstants.REVIEW_TYPE_FORMAL };
 
 	// ------------------------------------------------------------------------
 	// Member variables
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Field fParentGroup.
 	 */
 	private R4EUIReviewGroup fParentGroup = null;
 
 	/**
 	 * Field fReviewType.
 	 */
 	private CCombo fReviewType = null;
 
 	/**
 	 * Field fReviewTypeValue.
 	 */
 	private R4EReviewType fReviewTypeValue;
 
 	/**
 	 * The input value; the empty string by default.
 	 */
 	private String fReviewNameValue = "";
 
 	/**
 	 * Input text widget.
 	 */
 	protected Text fReviewNameInputTextField;
 
 	/**
 	 * Field fReviewDescriptionValue.
 	 */
 	private String fReviewDescriptionValue = "";
 
 	/**
 	 * Field fReviewDescriptionInputTextField.
 	 */
 	private Text fReviewDescriptionInputTextField = null;
 
 	/**
 	 * Field fDueDateText.
 	 */
 	protected Text fDueDateText = null;
 
 	/**
 	 * Field fDueDateValue.
 	 */
 	private Date fDueDateValue = null;
 
 	/**
 	 * Field fProjectValue.
 	 */
 	private String fProjectValue = "";
 
 	/**
 	 * Field fProjectsCombo.
 	 */
 	private CCombo fProjectsCombo = null;
 
 	/**
 	 * Field fComponentsValues.
 	 */
 	private String[] fComponentsValues = null;
 
 	/**
 	 * Field fAvailableComponents.
 	 */
 	private EditableListWidget fComponents = null;
 
 	/**
 	 * Field fEntryCriteriaValue.
 	 */
 	private String fEntryCriteriaValue = "";
 
 	/**
 	 * Field fEntryCriteriaTextField.
 	 */
 	private Text fEntryCriteriaTextField = null;
 
 	/**
 	 * Field fObjectivesValue.
 	 */
 	private String fObjectivesValue = "";
 
 	/**
 	 * Field fObjectivesTextField.
 	 */
 	private Text fObjectivesTextField = null;
 
 	/**
 	 * Field fReferenceMaterialValue.
 	 */
 	private String fReferenceMaterialValue = "";
 
 	/**
 	 * Field fReferenceMaterialTextField.
 	 */
 	private Text fReferenceMaterialTextField = null;
 
 	/**
 	 * The input validator, or <code>null</code> if none.
 	 */
 	private final IInputValidator fValidator;
 
 	// ------------------------------------------------------------------------
 	// Constructors
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Constructor for R4EReviewInputDialog.
 	 * 
 	 * @param aParentShell
 	 *            Shell
 	 * @param aParentGroup
 	 *            R4EUIReviewGroup
 	 */
 	public ReviewInputDialog(Shell aParentShell, R4EUIReviewGroup aParentGroup) {
 		super(aParentShell);
 		setBlockOnOpen(true);
 		fValidator = new R4EInputValidator();
 		fParentGroup = aParentGroup;
 	}
 
 	// ------------------------------------------------------------------------
 	// Methods
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Method buttonPressed.
 	 * 
 	 * @param buttonId
 	 *            int
 	 * @see org.eclipse.jface.dialogs.Dialog#buttonPressed(int)
 	 */
 	@Override
 	protected void buttonPressed(int buttonId) {
 		if (buttonId == IDialogConstants.OK_ID) {
 			//Review type (no validation needed as this is a read-only combo box
 			if (fReviewType.getText().equals(R4EUIConstants.REVIEW_TYPE_FORMAL)) {
 				fReviewTypeValue = R4EReviewType.R4E_REVIEW_TYPE_FORMAL;
 			} else if (fReviewType.getText().equals(R4EUIConstants.REVIEW_TYPE_INFORMAL)) {
 				fReviewTypeValue = R4EReviewType.R4E_REVIEW_TYPE_INFORMAL;
 			} else if (fReviewType.getText().equals(R4EUIConstants.REVIEW_TYPE_BASIC)) {
 				fReviewTypeValue = R4EReviewType.R4E_REVIEW_TYPE_BASIC;
 			} else {
 				//Validation of input failed
 				final ErrorDialog dialog = new ErrorDialog(null, R4EUIConstants.DIALOG_TITLE_ERROR,
 						"No input given for Review Type", new Status(IStatus.ERROR, R4EUIPlugin.PLUGIN_ID, 0, null,
 								null), IStatus.ERROR);
 				dialog.open();
 				return;
 			}
 
 			//Validate Review Name
 			String validateResult = validateEmptyInput(fReviewNameInputTextField);
 			if (null != validateResult) {
 				//Validation of input failed
 				final ErrorDialog dialog = new ErrorDialog(null, R4EUIConstants.DIALOG_TITLE_ERROR,
 						"No input given for Review Name", new Status(IStatus.ERROR, R4EUIPlugin.PLUGIN_ID, 0,
 								validateResult, null), IStatus.ERROR);
 				dialog.open();
 				return;
 			}
 			//Check if review already exist
 			validateResult = validateReviewExists(fReviewNameInputTextField, fParentGroup);
 			if (null != validateResult) {
 				//Validate of input failed
 				final ErrorDialog dialog = new ErrorDialog(null, R4EUIConstants.DIALOG_TITLE_ERROR,
 						"Review already exists", new Status(IStatus.ERROR, R4EUIPlugin.PLUGIN_ID, 0, validateResult,
 								null), IStatus.ERROR);
 				dialog.open();
 				return;
 			}
 			fReviewNameValue = fReviewNameInputTextField.getText().trim();
 
 			//Validate Review Description
 			validateResult = validateEmptyInput(fReviewDescriptionInputTextField);
 			if (null != validateResult) {
 				//Validation of input failed
 				final ErrorDialog dialog = new ErrorDialog(null, R4EUIConstants.DIALOG_TITLE_ERROR,
 						"No input given for Review Description", new Status(IStatus.WARNING, R4EUIPlugin.PLUGIN_ID, 0,
 								validateResult, null), IStatus.WARNING);
 				dialog.open();
 			}
 			fReviewDescriptionValue = fReviewDescriptionInputTextField.getText().trim();
 
 			//Validate Project (optional)
 			validateResult = validateEmptyInput(fProjectsCombo.getText());
 			if (null == validateResult) {
 				fProjectValue = fProjectsCombo.getText();
 			}
 
 			//Validate Components (optional)
 			final ArrayList<String> componentsValues = new ArrayList<String>();
 			for (Item item : fComponents.getItems()) {
 				validateResult = validateEmptyInput(item.getText());
 				if (null == validateResult) {
 					componentsValues.add(item.getText());
 				}
 			}
 			fComponentsValues = componentsValues.toArray(new String[componentsValues.size()]);
 
 			//Validate Entry Criteria (optional)
 			validateResult = validateEmptyInput(fEntryCriteriaTextField);
 			if (null == validateResult) {
 				fEntryCriteriaValue = fEntryCriteriaTextField.getText().trim();
 			}
 
 			//Validate Objectives (optional)
 			validateResult = validateEmptyInput(fObjectivesTextField);
 			if (null == validateResult) {
 				fObjectivesValue = fObjectivesTextField.getText().trim();
 			}
 
 			//Validate ReferenceMaterial (optional)
 			validateResult = validateEmptyInput(fReferenceMaterialTextField);
 			if (null == validateResult) {
 				fReferenceMaterialValue = fReferenceMaterialTextField.getText().trim();
 			}
 		} else {
 			fReviewNameValue = null;
 			fReviewDescriptionValue = null;
 			fProjectValue = null;
 			fEntryCriteriaValue = null;
 			fObjectivesValue = null;
 			fReferenceMaterialValue = null;
 		}
 		super.buttonPressed(buttonId);
 	}
 
 	/**
 	 * Method configureShell.
 	 * 
 	 * @param shell
 	 *            Shell
 	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
 	 */
 	@Override
 	protected void configureShell(Shell shell) {
 		super.configureShell(shell);
 		shell.setText(ADD_REVIEW_DIALOG_TITLE);
 		shell.setMinimumSize(R4EUIConstants.DIALOG_DEFAULT_WIDTH, R4EUIConstants.DIALOG_DEFAULT_HEIGHT);
 	}
 
 	/**
 	 * Configures the dialog form and creates form content. Clients should override this method.
 	 * 
 	 * @param mform
 	 *            the dialog form
 	 */
 	@Override
 	protected void createFormContent(final IManagedForm mform) {
 
 		final FormToolkit toolkit = mform.getToolkit();
 		final ScrolledForm sform = mform.getForm();
 		sform.setExpandVertical(true);
 		final Composite composite = sform.getBody();
 		final GridLayout layout = new GridLayout(4, false);
 		composite.setLayout(layout);
 		GridData textGridData = null;
 
 		//Review Type
 		Label label = toolkit.createLabel(composite, ADD_REVIEW_TYPE_DIALOG_VALUE);
 		label.setToolTipText(R4EUIConstants.REVIEW_TYPE_TOOLTIP);
 		label.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false));
 		fReviewType = new CCombo(composite, SWT.BORDER | SWT.READ_ONLY);
 		fReviewType.setItems(REVIEW_TYPES);
 		//fReviewType.select(0);
 		textGridData = new GridData(GridData.FILL, GridData.FILL, true, false);
 		textGridData.horizontalSpan = 3;
 		fReviewType.setToolTipText(R4EUIConstants.REVIEW_TYPE_TOOLTIP);
 		fReviewType.setLayoutData(textGridData);
 		fReviewType.addModifyListener(new ModifyListener() {
 			public void modifyText(ModifyEvent e) {
 				// ignore
 				if (fReviewType.getText().length() > 0 && fReviewNameInputTextField.getText().length() > 0
 						&& fReviewDescriptionInputTextField.getText().length() > 0) {
 					getButton(IDialogConstants.OK_ID).setEnabled(true);
 				} else {
 					getButton(IDialogConstants.OK_ID).setEnabled(false);
 				}
 			}
 		});
 
 		//Basic parameters section
 		final Section basicSection = toolkit.createSection(composite, Section.DESCRIPTION
 				| ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE | ExpandableComposite.EXPANDED);
 		final GridData basicSectionGridData = new GridData(GridData.FILL, GridData.FILL, true, false);
 		basicSectionGridData.horizontalSpan = 4;
 		basicSection.setLayoutData(basicSectionGridData);
 		basicSection.setText(R4EUIConstants.BASIC_PARAMS_HEADER);
 		basicSection.setDescription(BASIC_PARAMS_HEADER_MSG);
 		basicSection.addExpansionListener(new ExpansionAdapter() {
 			@Override
 			public void expansionStateChanged(ExpansionEvent e) {
 				getShell().setSize(getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT));
 			}
 		});
 
 		final Composite basicSectionClient = toolkit.createComposite(basicSection);
 		basicSectionClient.setLayout(layout);
 		basicSection.setClient(basicSectionClient);
 
 		//Review Name
 		label = toolkit.createLabel(basicSectionClient, ADD_REVIEW_NAME_DIALOG_VALUE);
 		label.setToolTipText(R4EUIConstants.REVIEW_NAME_TOOLTIP);
 		label.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false));
 		fReviewNameInputTextField = toolkit.createText(basicSectionClient, "", SWT.SINGLE | SWT.BORDER);
 		textGridData = new GridData(GridData.FILL, GridData.FILL, true, false);
 		textGridData.horizontalSpan = 3;
 		fReviewNameInputTextField.setToolTipText(R4EUIConstants.REVIEW_NAME_TOOLTIP);
 		fReviewNameInputTextField.setLayoutData(textGridData);
 		fReviewNameInputTextField.addModifyListener(new ModifyListener() {
 			public void modifyText(ModifyEvent e) {
 				// ignore
 				if (fReviewType.getText().length() > 0 && fReviewNameInputTextField.getText().length() > 0
 						&& fReviewDescriptionInputTextField.getText().length() > 0) {
 					getButton(IDialogConstants.OK_ID).setEnabled(true);
 				} else {
 					getButton(IDialogConstants.OK_ID).setEnabled(false);
 				}
 			}
 		});
 
 		//Review Description
 		label = toolkit.createLabel(basicSectionClient, ADD_REVIEW_DESCRIPTION_DIALOG_VALUE);
 		label.setToolTipText(R4EUIConstants.REVIEW_DESCRIPTION_TOOLTIP);
 		label.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false));
 		fReviewDescriptionInputTextField = toolkit.createText(basicSectionClient, "", SWT.MULTI | SWT.V_SCROLL
 				| SWT.BORDER);
 		textGridData = new GridData(GridData.FILL, GridData.FILL, true, false);
 		textGridData.horizontalSpan = 3;
 		textGridData.heightHint = fReviewNameInputTextField.getLineHeight() * 6;
 		fReviewDescriptionInputTextField.setToolTipText(R4EUIConstants.REVIEW_DESCRIPTION_TOOLTIP);
 		fReviewDescriptionInputTextField.setLayoutData(textGridData);
 		fReviewDescriptionInputTextField.addModifyListener(new ModifyListener() {
 			public void modifyText(ModifyEvent e) {
 				// ignore
 				if (fReviewType.getText().length() > 0 && fReviewNameInputTextField.getText().length() > 0
 						&& fReviewDescriptionInputTextField.getText().length() > 0) {
 					getButton(IDialogConstants.OK_ID).setEnabled(true);
 				} else {
 					getButton(IDialogConstants.OK_ID).setEnabled(false);
 				}
 			}
 		});
 
 		//Extra parameters section
 		final Section extraSection = toolkit.createSection(composite, Section.DESCRIPTION
 				| ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE);
 		final GridData extraSectionGridData = new GridData(GridData.FILL, GridData.FILL, true, false);
 		extraSectionGridData.horizontalSpan = 4;
 		extraSection.setLayoutData(extraSectionGridData);
 		extraSection.setText(R4EUIConstants.EXTRA_PARAMS_HEADER);
 		extraSection.setDescription(EXTRA_PARAMS_HEADER_MSG);
 		extraSection.addExpansionListener(new ExpansionAdapter() {
 			@Override
 			public void expansionStateChanged(ExpansionEvent e) {
 				getShell().setSize(getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT));
 			}
 		});
 
 		final Composite extraSectionClient = toolkit.createComposite(extraSection);
 		extraSectionClient.setLayout(layout);
 		extraSection.setClient(extraSectionClient);
 
 		final IStructuredSelection selection = (IStructuredSelection) R4EUIModelController.getNavigatorView()
 				.getTreeViewer()
 				.getSelection();
 		final R4EUIReviewGroup parentGroup = (R4EUIReviewGroup) selection.getFirstElement();
 
 		//Due Date
 		toolkit.setBorderStyle(SWT.NULL);
 		label = toolkit.createLabel(extraSectionClient, R4EUIConstants.DUE_DATE_LABEL);
 		textGridData = new GridData(SWT.FILL, SWT.CENTER, false, false);
 		textGridData.horizontalSpan = 1;
 		label.setLayoutData(textGridData);
 
 		final Composite dateComposite = toolkit.createComposite(extraSectionClient);
 		textGridData = new GridData(SWT.FILL, SWT.CENTER, true, true);
		textGridData.horizontalSpan = 2;
 		dateComposite.setToolTipText(R4EUIConstants.REVIEW_DUE_DATE_TOOLTIP);
 		dateComposite.setLayoutData(textGridData);
 		dateComposite.setLayout(new GridLayout(2, false));
 
 		fDueDateText = toolkit.createText(dateComposite, "", SWT.READ_ONLY);
 		fDueDateText.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
 		fDueDateText.setEditable(false);
 		toolkit.setBorderStyle(SWT.BORDER);
 
 		final Composite dateButtonComposite = toolkit.createComposite(dateComposite);
 		textGridData = new GridData(SWT.FILL, SWT.CENTER, true, true);
 		textGridData.horizontalSpan = 1;
		dateButtonComposite.setToolTipText(R4EUIConstants.ANOMALY_DUE_DATE_TOOLTIP);
 		dateButtonComposite.setLayoutData(textGridData);
 		dateButtonComposite.setLayout(new GridLayout(2, false));
 
 		final Button calendarButton = toolkit.createButton(dateButtonComposite, R4EUIConstants.UPDATE_LABEL, SWT.NONE);
 		calendarButton.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
 		calendarButton.addSelectionListener(new SelectionListener() {
 			public void widgetSelected(SelectionEvent e) {
 				final ICalendarDialog dialog = R4EUIDialogFactory.getInstance().getCalendarDialog();
 				final int result = dialog.open();
 				if (result == Window.OK) {
 					final SimpleDateFormat dateFormat = new SimpleDateFormat(R4EUIConstants.SIMPLE_DATE_FORMAT);
 					fDueDateText.setText(dateFormat.format(dialog.getDate()));
 					fDueDateValue = dialog.getDate();
 				}
 			}
 
 			public void widgetDefaultSelected(SelectionEvent e) { // $codepro.audit.disable emptyMethod
 				// No implementation needed
 			}
 		});
 
 		final Button clearButton = toolkit.createButton(dateButtonComposite, R4EUIConstants.CLEAR_LABEL, SWT.NONE);
 		clearButton.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
 		clearButton.addSelectionListener(new SelectionListener() {
 			public void widgetSelected(SelectionEvent e) {
 				fDueDateText.setText("");
 				fDueDateValue = null;
 			}
 
 			public void widgetDefaultSelected(SelectionEvent e) { // $codepro.audit.disable emptyMethod
 				// No implementation needed
 			}
 		});
 
 		//Project
 		label = toolkit.createLabel(extraSectionClient, ADD_REVIEW_PROJECT_DIALOG_VALUE);
 		label.setToolTipText(R4EUIConstants.REVIEW_PROJECT_TOOLTIP);
 		label.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false));
 		fProjectsCombo = new CCombo(extraSectionClient, SWT.BORDER | SWT.READ_ONLY);
 		EList<String> availableProjects = parentGroup.getReviewGroup().getAvailableProjects();
 		final String[] projects = availableProjects.toArray(new String[availableProjects.size()]);
 		if (0 == projects.length) {
 			fProjectsCombo.setEnabled(false);
 		} else {
 			fProjectsCombo.add("");
 			for (String project : projects) {
 				fProjectsCombo.add(project);
 			}
 		}
 		final GridData data1 = new GridData(GridData.FILL, GridData.FILL, true, false);
 		data1.horizontalSpan = 3;
 		fProjectsCombo.setToolTipText(R4EUIConstants.REVIEW_PROJECT_TOOLTIP);
 		fProjectsCombo.setLayoutData(data1);
 
 		//Components
 		label = toolkit.createLabel(extraSectionClient, ADD_REVIEW_COMPONENTS_DIALOG_VALUE);
 		label.setToolTipText(R4EUIConstants.REVIEW_COMPONENTS_TOOLTIP);
 		label.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false));
 		final String[] components = (String[]) parentGroup.getReviewGroup().getAvailableComponents().toArray();
 		textGridData = new GridData(GridData.FILL, GridData.FILL, true, false);
 		textGridData.horizontalSpan = 3;
 		fComponents = new EditableListWidget(toolkit, extraSectionClient, textGridData, null, 0, CCombo.class,
 				components);
 		if (0 == components.length) {
 			fComponents.setEnabled(false);
 		}
 		fComponents.setToolTipText(R4EUIConstants.REVIEW_COMPONENTS_TOOLTIP);
 
 		//Entry Criteria
 		label = toolkit.createLabel(extraSectionClient, ADD_REVIEW_ENTRY_CRITERIA_DIALOG_VALUE);
 		label.setToolTipText(R4EUIConstants.REVIEW_ENTRY_CRITERIA_TOOLTIP);
 		label.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false));
 		fEntryCriteriaTextField = toolkit.createText(extraSectionClient, "", SWT.MULTI | SWT.V_SCROLL | SWT.BORDER);
 		if (null != parentGroup.getReviewGroup().getDefaultEntryCriteria()) {
 			fEntryCriteriaTextField.setText(parentGroup.getReviewGroup().getDefaultEntryCriteria());
 		}
 		textGridData = new GridData(GridData.FILL, GridData.FILL, true, false);
 		textGridData.horizontalSpan = 3;
 		textGridData.heightHint = fReviewNameInputTextField.getLineHeight() * 3;
 		fEntryCriteriaTextField.setToolTipText(R4EUIConstants.REVIEW_ENTRY_CRITERIA_TOOLTIP);
 		fEntryCriteriaTextField.setLayoutData(textGridData);
 
 		//Objectives
 		label = toolkit.createLabel(extraSectionClient, ADD_REVIEW_OBJECTIVES_DIALOG_VALUE);
 		label.setToolTipText(R4EUIConstants.REVIEW_OBJECTIVES_TOOLTIP);
 		label.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false));
 		fObjectivesTextField = toolkit.createText(extraSectionClient, "", SWT.MULTI | SWT.V_SCROLL | SWT.BORDER);
 		textGridData = new GridData(GridData.FILL, GridData.FILL, true, false);
 		textGridData.horizontalSpan = 3;
 		textGridData.heightHint = fReviewNameInputTextField.getLineHeight() * 3;
 		fObjectivesTextField.setToolTipText(R4EUIConstants.REVIEW_OBJECTIVES_TOOLTIP);
 		fObjectivesTextField.setLayoutData(textGridData);
 
 		//Reference Material
 		label = toolkit.createLabel(extraSectionClient, ADD_REVIEW_REFERENCE_MATERIAL_DIALOG_VALUE);
 		label.setToolTipText(R4EUIConstants.REVIEW_REFERENCE_MATERIAL_TOOLTIP);
 		label.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false));
 		fReferenceMaterialTextField = toolkit.createText(extraSectionClient, "", SWT.MULTI | SWT.V_SCROLL | SWT.BORDER);
 		textGridData = new GridData(GridData.FILL, GridData.FILL, true, false);
 		textGridData.horizontalSpan = 3;
 		textGridData.heightHint = fReviewNameInputTextField.getLineHeight() * 3;
 		fReferenceMaterialTextField.setToolTipText(R4EUIConstants.REVIEW_REFERENCE_MATERIAL_TOOLTIP);
 		fReferenceMaterialTextField.setLayoutData(textGridData);
 	}
 
 	/**
 	 * Configures the button bar.
 	 * 
 	 * @param parent
 	 *            the parent composite
 	 * @return Control
 	 */
 	@Override
 	protected Control createButtonBar(Composite parent) {
 		final Control bar = super.createButtonBar(parent);
 		getButton(IDialogConstants.OK_ID).setEnabled(false);
 		return bar;
 	}
 
 	/**
 	 * Method isResizable.
 	 * 
 	 * @return boolean * @see org.eclipse.jface.dialogs.Dialog#isResizable()
 	 */
 	@Override
 	protected boolean isResizable() {
 		return true;
 	}
 
 	/**
 	 * Returns the text area.
 	 * 
 	 * @return the review name text area * @see
 	 *         org.eclipse.mylyn.reviews.r4e.ui.internal.dialogs.IReviewInputDialog#getReviewTypeValue()
 	 */
 	public R4EReviewType getReviewTypeValue() {
 		return fReviewTypeValue;
 	}
 
 	/**
 	 * Returns the string typed into this input dialog.
 	 * 
 	 * @return the review name input string * @see
 	 *         org.eclipse.mylyn.reviews.r4e.ui.internal.dialogs.IReviewInputDialog#getReviewNameValue()
 	 */
 	public String getReviewNameValue() {
 		return fReviewNameValue;
 	}
 
 	/**
 	 * Returns the string typed into this input dialog.
 	 * 
 	 * @return the review description input string * @see
 	 *         org.eclipse.mylyn.reviews.r4e.ui.internal.dialogs.IReviewInputDialog#getReviewDescriptionValue()
 	 */
 	public String getReviewDescriptionValue() {
 		return fReviewDescriptionValue;
 	}
 
 	/**
 	 * Method setDueDate.
 	 * 
 	 * @param aDate
 	 *            Date
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.dialogs.IReviewInputDialog#setDueDate(Date)
 	 */
 	public void setDueDate(Date aDate) {
 		fDueDateValue = aDate;
 		if (null != fDueDateValue) {
 			final SimpleDateFormat dateFormat = new SimpleDateFormat(R4EUIConstants.SIMPLE_DATE_FORMAT);
 			fDueDateText.setText(dateFormat.format(fDueDateValue));
 		} else {
 			fDueDateText.setText("");
 		}
 	}
 
 	/**
 	 * Method getDueDate.
 	 * 
 	 * @return Date
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.dialogs.IReviewInputDialog#getDueDate()
 	 */
 	public Date getDueDate() {
 		return fDueDateValue;
 	}
 
 	/**
 	 * Returns the string typed into this input dialog.
 	 * 
 	 * @return the project input string * @see
 	 *         org.eclipse.mylyn.reviews.r4e.ui.internal.dialogs.IReviewInputDialog#getProjectValue()
 	 */
 	public String getProjectValue() {
 		return fProjectValue;
 	}
 
 	/**
 	 * Returns the strings typed into this input dialog.
 	 * 
 	 * @return the components input strings * @see
 	 *         org.eclipse.mylyn.reviews.r4e.ui.internal.dialogs.IReviewInputDialog#getComponentsValues()
 	 */
 	public String[] getComponentsValues() {
 		return fComponentsValues;
 	}
 
 	/**
 	 * Returns the string typed into this input dialog.
 	 * 
 	 * @return the entry criteria input string * @see
 	 *         org.eclipse.mylyn.reviews.r4e.ui.internal.dialogs.IReviewInputDialog#getEntryCriteriaValue()
 	 */
 	public String getEntryCriteriaValue() {
 		return fEntryCriteriaValue;
 	}
 
 	/**
 	 * Returns the string typed into this input dialog.
 	 * 
 	 * @return the objectives input string * @see
 	 *         org.eclipse.mylyn.reviews.r4e.ui.internal.dialogs.IReviewInputDialog#getObjectivesValue()
 	 */
 	public String getObjectivesValue() {
 		return fObjectivesValue;
 	}
 
 	/**
 	 * Returns the string typed into this input dialog.
 	 * 
 	 * @return the reference material input string * @see
 	 *         org.eclipse.mylyn.reviews.r4e.ui.internal.dialogs.IReviewInputDialog#getReferenceMaterialValue()
 	 */
 	public String getReferenceMaterialValue() {
 		return fReferenceMaterialValue;
 	}
 
 	/**
 	 * Method validateEmptyInput.
 	 * 
 	 * @param aText
 	 *            Text
 	 * @return String
 	 */
 	private String validateEmptyInput(Text aText) {
 		return fValidator.isValid(aText.getText());
 	}
 
 	/**
 	 * Method validateEmptyInput.
 	 * 
 	 * @param aString
 	 *            String
 	 * @return String
 	 */
 	private String validateEmptyInput(String aString) {
 		return fValidator.isValid(aString);
 	}
 
 	/**
 	 * Method validateReviewExists.
 	 * 
 	 * @param aReviewName
 	 *            Text
 	 * @param aParentGroup
 	 *            R4EUIReviewGroup
 	 * @return String (null = valid, or error string)
 	 */
 	private String validateReviewExists(Text aReviewName, R4EUIReviewGroup aParentGroup) {
 		return ((R4EInputValidator) fValidator).isReviewExists(aReviewName.getText(), aParentGroup);
 	}
 }
