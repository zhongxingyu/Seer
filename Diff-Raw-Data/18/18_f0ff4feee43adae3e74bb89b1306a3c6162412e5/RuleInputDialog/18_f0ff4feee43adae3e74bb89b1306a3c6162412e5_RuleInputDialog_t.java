 // $codepro.audit.disable com.instantiations.assist.eclipse.analysis.audit.rule.effectivejava.alwaysOverridetoString.alwaysOverrideToString, com.instantiations.assist.eclipse.analysis.audit.rule.effectivejava.constructorsOnlyInvokeFinalMethods, useForLoop, com.instantiations.assist.eclipse.analysis.deserializeabilitySecurity, com.instantiations.assist.eclipse.analysis.disallowReturnMutable, com.instantiations.assist.eclipse.analysis.enforceCloneableUsageSecurity, explicitThisUsage
 /*******************************************************************************
  * Copyright (c) 2011, 2012 Ericsson AB and others.
  * 
  * All rights reserved. This program and the accompanying materials are
  * made available under the terms of the Eclipse Public License v1.0 which
  * accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Description:
  * 
  * This class implements the dialog used to fill-in the Rule element details.
  * This is a modal dialog
  * 
  * Contributors:
  *   Sebastien Dubois - Created for Mylyn Review R4E project
  *   
  ******************************************************************************/
 
 package org.eclipse.mylyn.reviews.r4e.ui.internal.dialogs;
 
 import java.util.List;
 
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jface.dialogs.ErrorDialog;
 import org.eclipse.jface.dialogs.IDialogConstants;
 import org.eclipse.jface.dialogs.IInputValidator;
 import org.eclipse.mylyn.reviews.r4e.core.model.drules.R4EDesignRule;
 import org.eclipse.mylyn.reviews.r4e.core.model.drules.R4EDesignRuleClass;
 import org.eclipse.mylyn.reviews.r4e.core.model.drules.R4EDesignRuleRank;
 import org.eclipse.mylyn.reviews.r4e.core.model.drules.R4EDesignRuleViolation;
 import org.eclipse.mylyn.reviews.r4e.ui.R4EUIPlugin;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.utils.R4EUIConstants;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.utils.UIUtils;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.CCombo;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
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
 
 // ------------------------------------------------------------------------
 // Constants
 // ------------------------------------------------------------------------
 
 /**
  * @author Sebastien Dubois
  * @version $Revision: 1.0 $
  */
 public class RuleInputDialog extends FormDialog implements IRuleInputDialog {
 
 	// ------------------------------------------------------------------------
 	// Constants
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Field ADD_RULE_DIALOG_TITLE. (value is ""Enter Rule Details"")
 	 */
 	private static final String ADD_RULE_DIALOG_TITLE = "Enter Rule Details";
 
 	/**
 	 * Field ADD_RULE_ID_DIALOG_VALUE. (value is ""Rule Id:"")
 	 */
 	private static final String ADD_RULE_ID_DIALOG_VALUE = "Rule Id:";
 
 	/**
 	 * Field ADD_RULE_TITLE_DIALOG_VALUE. (value is ""Rule Title:"")
 	 */
 	private static final String ADD_RULE_TITLE_DIALOG_VALUE = "Rule Title:";
 
 	/**
 	 * Field ADD_RULE_CLASS_DIALOG_VALUE. (value is ""Rule Class:"")
 	 */
 	private static final String ADD_RULE_CLASS_DIALOG_VALUE = "Rule Class:";
 
 	/**
 	 * Field ADD_RULE_RANK_DIALOG_VALUE. (value is ""Rule Rank:"")
 	 */
 	private static final String ADD_RULE_RANK_DIALOG_VALUE = "Rule Rank:";
 
 	/**
 	 * Field ADD_RULE_DESCRIPTION_DIALOG_VALUE. (value is ""Rule Description:"")
 	 */
 	private static final String ADD_RULE_DESCRIPTION_DIALOG_VALUE = "Rule Description:";
 
 	/**
 	 * Field BASIC_PARAMS_HEADER_MSG. (value is ""Enter the mandatory basic parameters for this Rule Violation"")
 	 */
 	private static final String BASIC_PARAMS_HEADER_MSG = "Enter the mandatory basic parameters for this Rule";
 
 	// ------------------------------------------------------------------------
 	// Member variables
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Field fIdValue.
 	 */
 	private String fIdValue = "";
 
 	/**
 	 * Field fIdInputTextField.
 	 */
 	private Text fIdInputTextField;
 
 	/**
 	 * Field fTitleValue.
 	 */
 	private String fTitleValue = "";
 
 	/**
 	 * Field fTitleInputTextField.
 	 */
 	private Text fTitleInputTextField;
 
 	/**
 	 * Field fDescriptionValue.
 	 */
 	private String fDescriptionValue = "";
 
 	/**
 	 * Field fDescriptionInputTextField.
 	 */
 	private Text fDescriptionInputTextField;
 
 	/**
 	 * Field fClassType.
 	 */
 	private CCombo fClassCombo = null;
 
 	/**
 	 * Field fClassTypeValue.
 	 */
 	private R4EDesignRuleClass fClassValue;
 
 	/**
 	 * Field fRankType.
 	 */
 	private CCombo fRankCombo = null;
 
 	/**
 	 * Field fRankTypeValue.
 	 */
 	private R4EDesignRuleRank fRankValue;
 
 	/**
 	 * The input validator, or <code>null</code> if none.
 	 */
 	private final IInputValidator fValidator;
 
 	/**
 	 * The parent structure for a ruleId, the rule violation
 	 */
 	private R4EDesignRuleViolation fDesignRuleViolation = null;
 
 	// ------------------------------------------------------------------------
 	// Constructors
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Constructor for RuleInputDialog.
 	 * 
 	 * @param aParentShell
 	 *            Shell
 	 */
 	public RuleInputDialog(Shell aParentShell, R4EDesignRuleViolation aViolation) {
 		super(aParentShell);
 		fDesignRuleViolation = aViolation;
 		setBlockOnOpen(true);
 		fValidator = new R4EInputValidator();
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
 			//Validate Id
 			String validateResult = validateEmptyInput(fIdInputTextField);
 			if (null != validateResult) {
 				//Validation of input failed
 				final ErrorDialog dialog = new ErrorDialog(null, R4EUIConstants.DIALOG_TITLE_ERROR,
 						"No input given for Rule Id", new Status(IStatus.ERROR, R4EUIPlugin.PLUGIN_ID, 0,
 								validateResult, null), IStatus.ERROR);
 				dialog.open();
 				return;
 			}
 			fIdValue = fIdInputTextField.getText().trim();
 
 			//Validate Title
 			validateResult = validateEmptyInput(fTitleInputTextField);
 			if (null != validateResult) {
 				//Validation of input failed
 				final ErrorDialog dialog = new ErrorDialog(null, R4EUIConstants.DIALOG_TITLE_ERROR,
 						"No input given for Rule Title", new Status(IStatus.ERROR, R4EUIPlugin.PLUGIN_ID, 0,
 								validateResult, null), IStatus.ERROR);
 				dialog.open();
 				return;
 			}
 			fTitleValue = fTitleInputTextField.getText().trim();
 
 			//Validate Class (no validation needed as this is a read-only combo box
 			fClassValue = UIUtils.getClassFromString(fClassCombo.getText());
 
 			//Validate Rank (no validation needed as this is a read-only combo box
 			fRankValue = UIUtils.getRankFromString(fRankCombo.getText());
 
 			//Validate Description
 			validateResult = validateEmptyInput(fDescriptionInputTextField);
 			if (null != validateResult) {
 				//Validation of input failed
 				final ErrorDialog dialog = new ErrorDialog(null, R4EUIConstants.DIALOG_TITLE_ERROR,
 						"No input given for Rule Description", new Status(IStatus.ERROR, R4EUIPlugin.PLUGIN_ID, 0,
 								validateResult, null), IStatus.ERROR);
 				dialog.open();
 				return;
 			}
 			fDescriptionValue = fDescriptionInputTextField.getText().trim();
 
 			//Validate is the ruleId already exist
 			if (!isValidRuleId()) {
 				return;
 			}
 		} else {
 			fIdValue = null;
 			fTitleValue = null;
 			fDescriptionValue = null;
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
 		shell.setText(ADD_RULE_DIALOG_TITLE);
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
 
 		//Rule Id
 		Label label = toolkit.createLabel(basicSectionClient, ADD_RULE_ID_DIALOG_VALUE);
 		label.setToolTipText(R4EUIConstants.RULE_ID_TOOLTIP);
 		label.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false));
 		fIdInputTextField = toolkit.createText(basicSectionClient, "", SWT.SINGLE | SWT.BORDER);
 		textGridData = new GridData(GridData.FILL, GridData.FILL, true, false);
 		textGridData.horizontalSpan = 3;
 		fIdInputTextField.setToolTipText(R4EUIConstants.RULE_ID_TOOLTIP);
 		fIdInputTextField.setLayoutData(textGridData);
 		fIdInputTextField.addModifyListener(new ModifyListener() {
 			public void modifyText(ModifyEvent e) {
 				// ignore
 				if (fIdInputTextField.getText().length() > 0 && fTitleInputTextField.getText().length() > 0
 						&& fDescriptionInputTextField.getText().length() > 0) {
 					getButton(IDialogConstants.OK_ID).setEnabled(true);
 				} else {
 					getButton(IDialogConstants.OK_ID).setEnabled(false);
 				}
 			}
 		});
 
 		//Rule Title
 		label = toolkit.createLabel(basicSectionClient, ADD_RULE_TITLE_DIALOG_VALUE);
 		label.setToolTipText(R4EUIConstants.RULE_TITLE_TOOLTIP);
 		label.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false));
 		fTitleInputTextField = toolkit.createText(basicSectionClient, "", SWT.SINGLE | SWT.BORDER);
 		textGridData = new GridData(GridData.FILL, GridData.FILL, true, false);
 		textGridData.horizontalSpan = 3;
 		fTitleInputTextField.setToolTipText(R4EUIConstants.RULE_TITLE_TOOLTIP);
 		fTitleInputTextField.setLayoutData(textGridData);
 		fTitleInputTextField.addModifyListener(new ModifyListener() {
 			public void modifyText(ModifyEvent e) {
 				// ignore
 				if (fIdInputTextField.getText().length() > 0 && fTitleInputTextField.getText().length() > 0
 						&& fDescriptionInputTextField.getText().length() > 0) {
 					getButton(IDialogConstants.OK_ID).setEnabled(true);
 				} else {
 					getButton(IDialogConstants.OK_ID).setEnabled(false);
 				}
 			}
 		});
 
 		//Rule Description
 		label = toolkit.createLabel(basicSectionClient, ADD_RULE_DESCRIPTION_DIALOG_VALUE);
 		label.setToolTipText(R4EUIConstants.RULE_DESCRIPTION_TOOLTIP);
 		label.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false));
		fDescriptionInputTextField = toolkit.createText(basicSectionClient, "", SWT.MULTI | SWT.V_SCROLL | SWT.BORDER
				| SWT.WRAP);
 		textGridData = new GridData(GridData.FILL, GridData.FILL, true, false);
 		textGridData.horizontalSpan = 3;
 		textGridData.heightHint = fDescriptionInputTextField.getLineHeight() * 3;
 		fDescriptionInputTextField.setToolTipText(R4EUIConstants.RULE_DESCRIPTION_TOOLTIP);
 		fDescriptionInputTextField.setLayoutData(textGridData);
 		fDescriptionInputTextField.addModifyListener(new ModifyListener() {
 			public void modifyText(ModifyEvent e) {
 				// ignore
 				if (fIdInputTextField.getText().length() > 0 && fTitleInputTextField.getText().length() > 0
 						&& fDescriptionInputTextField.getText().length() > 0) {
 					getButton(IDialogConstants.OK_ID).setEnabled(true);
 				} else {
 					getButton(IDialogConstants.OK_ID).setEnabled(false);
 				}
 			}
 		});
 
 		//Rule Class
 		label = toolkit.createLabel(basicSectionClient, ADD_RULE_CLASS_DIALOG_VALUE);
 		label.setToolTipText(R4EUIConstants.RULE_CLASS_TOOLTIP);
 		label.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false));
 		fClassCombo = new CCombo(basicSectionClient, SWT.BORDER | SWT.READ_ONLY);
 		fClassCombo.setItems(UIUtils.getClasses());
 		fClassCombo.select(0);
 		textGridData = new GridData(GridData.FILL, GridData.FILL, true, false);
 		textGridData.horizontalSpan = 3;
 		fClassCombo.setToolTipText(R4EUIConstants.RULE_CLASS_TOOLTIP);
 		fClassCombo.setLayoutData(textGridData);
 
 		//Rule Rank
 		label = toolkit.createLabel(basicSectionClient, ADD_RULE_RANK_DIALOG_VALUE);
 		label.setToolTipText(R4EUIConstants.RULE_RANK_TOOLTIP);
 		label.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false));
 		fRankCombo = new CCombo(basicSectionClient, SWT.BORDER | SWT.READ_ONLY);
 		fRankCombo.setItems(UIUtils.getRanks());
 		fRankCombo.select(0);
 		textGridData = new GridData(GridData.FILL, GridData.FILL, true, false);
 		textGridData.horizontalSpan = 3;
 		fRankCombo.setToolTipText(R4EUIConstants.RULE_RANK_TOOLTIP);
 		fRankCombo.setLayoutData(textGridData);
 
 		//Set default focus
 		fIdInputTextField.setFocus();
 	}
 
 	/**
 	 * Test if the rule iD already exist or not under the same rule violation
 	 * 
 	 * @return Boolean
 	 */
 	private Boolean isValidRuleId() {
 		// Validate if the same ruleId already exist 
 		R4EUIPlugin.Ftracer.traceInfo("Rule id: " + fIdValue); //$NON-NLS-1$
 		List<R4EDesignRule> rulesList = fDesignRuleViolation.getRules();
 		int size = rulesList.size();
 		for (int i = 0; i < size; i++) {
 			if (((R4EDesignRule) rulesList.get(i)).getId().equals(fIdValue)) {
 				final ErrorDialog dialog = new ErrorDialog(null, R4EUIConstants.DIALOG_TITLE_ERROR,
 						"This Rule Id already exist", new Status(IStatus.ERROR, R4EUIPlugin.PLUGIN_ID, 0, fIdValue,
 								null), IStatus.ERROR);
 				dialog.open();
 
 				return false;
 			}
 		}
 
 		return true;
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
 	 * @return boolean
 	 */
 	@Override
 	protected boolean isResizable() {
 		return true;
 	}
 
 	/**
 	 * Returns the string typed into this input dialog.
 	 * 
 	 * @return the id input string
 	 */
 	public String getIdValue() {
 		return fIdValue;
 	}
 
 	/**
 	 * Returns the string typed into this input dialog.
 	 * 
 	 * @return the title input string
 	 */
 	public String getTitleValue() {
 		return fTitleValue;
 	}
 
 	/**
 	 * Returns the string typed into this input dialog.
 	 * 
 	 * @return the Class input string
 	 */
 	public R4EDesignRuleClass getClassValue() {
 		return fClassValue;
 	}
 
 	/**
 	 * Returns the string typed into this input dialog.
 	 * 
 	 * @return the Rank input string
 	 */
 	public R4EDesignRuleRank getRankValue() {
 		return fRankValue;
 	}
 
 	/**
 	 * Returns the string typed into this input dialog.
 	 * 
 	 * @return the description input string
 	 */
 	public String getDescriptionValue() {
 		return fDescriptionValue;
 	}
 
 	/**
 	 * Method validateEmptyInput.
 	 * 
 	 * @param aText
 	 *            Text
 	 * @return String
 	 */
 	private String validateEmptyInput(Text aText) {
 		if (null != fValidator) {
 			return fValidator.isValid(aText.getText());
 		}
 		return null;
 	}
 }
