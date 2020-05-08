 // $codepro.audit.disable com.instantiations.assist.eclipse.analysis.audit.rule.effectivejava.alwaysOverridetoString.alwaysOverrideToString, com.instantiations.assist.eclipse.analysis.audit.rule.effectivejava.constructorsOnlyInvokeFinalMethods, useForLoop, com.instantiations.assist.eclipse.analysis.deserializeabilitySecurity, com.instantiations.assist.eclipse.analysis.disallowReturnMutable, com.instantiations.assist.eclipse.analysis.enforceCloneableUsageSecurity, explicitThisUsage
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
  * This class implements the dialog used to fill-in the Comment element details.
  * This is a modeless-like dialog
  * 
  * Contributors:
  *   Sebastien Dubois - Created for Mylyn Review R4E project
  *   
  ******************************************************************************/
 
 package org.eclipse.mylyn.reviews.r4e.ui.internal.dialogs;
 
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jface.dialogs.ErrorDialog;
 import org.eclipse.jface.dialogs.IDialogConstants;
 import org.eclipse.jface.dialogs.IInputValidator;
 import org.eclipse.mylyn.reviews.r4e.ui.R4EUIPlugin;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.utils.R4EUIConstants;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
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
 public class CommentInputDialog extends FormDialog implements ICommentInputDialog {
 
 	// ------------------------------------------------------------------------
 	// Constants
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Field ADD_COMMENT_DIALOG_TITLE. (value is ""Enter Comment details"")
 	 */
 	private static final String ADD_COMMENT_DIALOG_TITLE = "Enter Comment Details";
 
 	/**
 	 * Field ADD_COMMENT_DIALOG_VALUE. (value is ""Enter your new Comments for this Anomaly:"")
 	 */
 	private static final String ADD_COMMENT_DIALOG_VALUE = "Comments Description:";
 
 	/**
 	 * Field BASIC_PARAMS_HEADER_MSG. (value is ""Enter the mandatory basic parameters for this comment"")
 	 */
 	private static final String BASIC_PARAMS_HEADER_MSG = "Enter the mandatory basic parameters for this comment";
 
 	// ------------------------------------------------------------------------
 	// Member variables
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Field fCommentValue.
 	 */
 	private String fCommentValue = "";
 
 	/**
 	 * Field fCommentInputTextField.
 	 */
 	private Text fCommentInputTextField;
 
 	/**
 	 * The input validator, or <code>null</code> if none.
 	 */
 	private final IInputValidator fValidator;
 
 	// ------------------------------------------------------------------------
 	// Constructors
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Constructor for R4ECommentInputDialog.
 	 * 
 	 * @param aParentShell
 	 *            Shell
 	 */
 	public CommentInputDialog(Shell aParentShell) {
 		super(aParentShell);
 		setBlockOnOpen(false);
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
 			//Validate Name
 			final String validateResult = validateEmptyInput(fCommentInputTextField);
 			if (null != validateResult) {
 				//Validation of input failed
 				final ErrorDialog dialog = new ErrorDialog(null, R4EUIConstants.DIALOG_TITLE_ERROR,
 						"No input given for Comment", new Status(IStatus.ERROR, R4EUIPlugin.PLUGIN_ID, 0,
 								validateResult, null), IStatus.ERROR);
 				dialog.open();
 				return;
 			}
 			fCommentValue = fCommentInputTextField.getText().trim();
 		} else {
 			fCommentValue = null;
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
 		shell.setText(ADD_COMMENT_DIALOG_TITLE);
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
 		final GridData basicSectionGridData = new GridData(GridData.FILL, GridData.FILL, true, true);
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
 
 		//Comment Description
 		final Label label = toolkit.createLabel(basicSectionClient, ADD_COMMENT_DIALOG_VALUE);
 		label.setToolTipText(R4EUIConstants.COMMENT_DESCRIPTION_TOOLTIP);
 		label.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false));
		fCommentInputTextField = toolkit.createText(basicSectionClient, "", SWT.MULTI | SWT.V_SCROLL | SWT.BORDER
				| SWT.WRAP);
 		textGridData = new GridData(GridData.FILL, GridData.FILL, true, true);
 		textGridData.horizontalSpan = 3;
 		textGridData.heightHint = fCommentInputTextField.getLineHeight() * 7;
 		fCommentInputTextField.setToolTipText(R4EUIConstants.COMMENT_DESCRIPTION_TOOLTIP);
 		fCommentInputTextField.setLayoutData(textGridData);
 		fCommentInputTextField.addModifyListener(new ModifyListener() {
 			public void modifyText(ModifyEvent e) {
 				// ignore
 				if (fCommentInputTextField.getText().length() > 0) {
 					getButton(IDialogConstants.OK_ID).setEnabled(true);
 				} else {
 					getButton(IDialogConstants.OK_ID).setEnabled(false);
 				}
 			}
 		});
 
 		//Set default focus
 		fCommentInputTextField.setFocus();
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
 	 * @return the comment input string
 	 */
 	public String getCommentValue() {
 		return fCommentValue;
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
 
 	/**
 	 * Method setShellStyle.
 	 * 
 	 * @param newShellStyle
 	 *            int
 	 */
 	@Override
 	protected void setShellStyle(int newShellStyle) {
 		int newstyle = newShellStyle & ~SWT.APPLICATION_MODAL; /* turn off APPLICATION_MODAL */
 		newstyle |= SWT.MODELESS; /* turn on MODELESS */
 		super.setShellStyle(newstyle);
 	}
 
 	/**
 	 * Method open.
 	 * 
 	 * @return int
 	 */
 	@Override
 	public int open() {
 		super.open();
 		pumpMessages(); /* this will let the caller wait till OK, Cancel is pressed, but will let the other GUI responsive */
 		return super.getReturnCode();
 	}
 
 	/**
 	 * Method pumpMessages.
 	 */
 	protected void pumpMessages() {
 		final Shell sh = getShell();
 		final Display disp = sh.getDisplay();
 		while (!sh.isDisposed()) { // $codepro.audit.disable methodInvocationInLoopCondition
 			if (!disp.readAndDispatch()) {
 				disp.sleep();
 			}
 		}
 		disp.update();
 	}
 }
