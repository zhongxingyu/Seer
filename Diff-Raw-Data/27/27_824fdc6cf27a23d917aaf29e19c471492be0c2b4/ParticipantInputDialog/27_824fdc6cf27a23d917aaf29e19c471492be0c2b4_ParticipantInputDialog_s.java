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
  * This class implements the dialog used to fill-in the Participant element details.
  *  This is a modal dialog
  * 
  * Contributors:
  *   Sebastien Dubois - Created for Mylyn Review R4E project
  *   
  ******************************************************************************/
 
 package org.eclipse.mylyn.reviews.r4e.ui.internal.dialogs;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.naming.NamingException;
 
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jface.dialogs.ErrorDialog;
 import org.eclipse.jface.dialogs.IDialogConstants;
 import org.eclipse.jface.dialogs.IInputValidator;
 import org.eclipse.jface.window.Window;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EReviewType;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EUserRole;
 import org.eclipse.mylyn.reviews.r4e.ui.R4EUIPlugin;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIModelController;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.utils.CommandUtils;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.utils.EditableListWidget;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.utils.R4EUIConstants;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.utils.UIUtils;
 import org.eclipse.mylyn.reviews.userSearch.query.IQueryUser;
 import org.eclipse.mylyn.reviews.userSearch.query.QueryUserFactory;
 import org.eclipse.mylyn.reviews.userSearch.userInfo.IUserInfo;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.CCombo;
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
  * @author lmcdubo
  * @version $Revision: 1.0 $
  */
 public class ParticipantInputDialog extends FormDialog implements IParticipantInputDialog {
 
 	// ------------------------------------------------------------------------
 	// Constants
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Field ADD_PARTICIPANT_DIALOG_TITLE. (value is ""Enter Review details"")
 	 */
 	private static final String ADD_PARTICIPANT_DIALOG_TITLE = "Enter Participant Details";
 
 	/**
 	 * Field FIND_BUTTON_LABEL. (value is ""Find"")
 	 */
 	private static final String FIND_BUTTON_LABEL = "Find";
 
 	// ------------------------------------------------------------------------
 	// Member variables
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Field fParticipantIdValue.
 	 */
 	private String fParticipantIdValue = "";
 
 	/**
 	 * Field fParticipantIdInputTextField.
 	 */
 	protected Text fParticipantIdInputTextField;
 
 	/**
 	 * Field fParticipantEmailInputTextField.
 	 */
 	protected Text fParticipantEmailInputTextField;
 
 	/**
 	 * Field fParticipantEmailValue.
 	 */
 	private String fParticipantEmailValue;
 
 	/**
 	 * Field fParticipantDetailsInputTextField.
 	 */
 	protected Text fParticipantDetailsInputTextField;
 
 	/**
 	 * Field fParticipantDetailsValue.
 	 */
 	private String fParticipantDetailsValue;
 
 	/**
 	 * Field fAvailableComponents.
 	 */
 	private EditableListWidget fRoleTypes = null;
 
 	/**
 	 * Field fRolesValue.
 	 */
 	private List<R4EUserRole> fRolesValue = null;
 
 	/**
 	 * Field fFocusAreaValue.
 	 */
 	private String fFocusAreaValue = "";
 
 	/**
 	 * Field fFocusAreaTextField.
 	 */
 	private Text fFocusAreaTextField;
 
 	/**
 	 * The input validator, or <code>null</code> if none.
 	 */
 	private final IInputValidator fValidator;
 
 	// ------------------------------------------------------------------------
 	// Constructors
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Constructor for ParticipantInputDialog.
 	 * 
 	 * @param aParentShell
 	 *            Shell
 	 */
 	public ParticipantInputDialog(Shell aParentShell) {
 		super(aParentShell);
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
 			this.getShell().setCursor(this.getShell().getDisplay().getSystemCursor(SWT.CURSOR_WAIT));
 
 			//Validate Participant Id
 			String validateResult = validateEmptyInput(fParticipantIdInputTextField);
 			if (null != validateResult) {
 				//Validation of input failed
 				final ErrorDialog dialog = new ErrorDialog(null, R4EUIConstants.DIALOG_TITLE_ERROR,
 						"No input given for Participant Id", new Status(IStatus.ERROR, R4EUIPlugin.PLUGIN_ID, 0,
 								validateResult, null), IStatus.ERROR);
 				dialog.open();
 				this.getShell().setCursor(this.getShell().getDisplay().getSystemCursor(SWT.CURSOR_ARROW));
 				return;
 			}
 			//Check if participant already exists
 			if (R4EUIModelController.getActiveReview()
 					.getParticipantIDs()
 					.contains(fParticipantIdInputTextField.getText())) {
 				final ErrorDialog dialog = new ErrorDialog(null, R4EUIConstants.DIALOG_TITLE_ERROR,
 						"Cannot Add Participant", new Status(IStatus.ERROR, R4EUIPlugin.PLUGIN_ID, 0,
 								"Participant already part of this Review", null), IStatus.ERROR);
 				dialog.open();
 				this.getShell().setCursor(this.getShell().getDisplay().getSystemCursor(SWT.CURSOR_ARROW));
 				return;
 			}
 			fParticipantIdValue = fParticipantIdInputTextField.getText();
 
 			//Validate Participant Email
 			validateResult = validateEmptyInput(fParticipantEmailInputTextField);
 			if (null != validateResult) {
 				//Validation of input failed
 				final ErrorDialog dialog = new ErrorDialog(null, R4EUIConstants.DIALOG_TITLE_ERROR,
 						"No input given for Participant Email", new Status(IStatus.ERROR, R4EUIPlugin.PLUGIN_ID, 0,
 								validateResult, null), IStatus.ERROR);
 				dialog.open();
 				this.getShell().setCursor(this.getShell().getDisplay().getSystemCursor(SWT.CURSOR_ARROW));
 				return;
 			}
 			if (!CommandUtils.isEmailValid(fParticipantEmailInputTextField.getText())) {
 				this.getShell().setCursor(this.getShell().getDisplay().getSystemCursor(SWT.CURSOR_ARROW));
 				return;
 			}
 			fParticipantEmailValue = fParticipantEmailInputTextField.getText();
 
 			//Validate Roles (optional)
 			fRolesValue = new ArrayList<R4EUserRole>();
 			if (null != fRoleTypes && fRoleTypes.getItems().length > 0) {
 				for (Item item : fRoleTypes.getItems()) {
 					//Review type (no validation needed as this is a read-only combo box
 					if (item.getText().equals(R4EUIConstants.USER_ROLE_LEAD)) {
 						fRolesValue.add(R4EUserRole.R4E_ROLE_LEAD);
 					} else if (item.getText().equals(R4EUIConstants.USER_ROLE_AUTHOR)) {
 						fRolesValue.add(R4EUserRole.R4E_ROLE_AUTHOR);
 					} else if (item.getText().equals(R4EUIConstants.USER_ROLE_REVIEWER)) {
 						fRolesValue.add(R4EUserRole.R4E_ROLE_REVIEWER);
 					} else if (item.getText().equals(R4EUIConstants.USER_ROLE_ORGANIZER)) {
 						fRolesValue.add(R4EUserRole.R4E_ROLE_ORGANIZER);
 					}
 				}
 			} else {
 				//If there is no roles defined, put one as default depenmding on the review type
 				if (R4EUIModelController.getActiveReview()
 						.getReview()
 						.getType()
 						.equals(R4EReviewType.R4E_REVIEW_TYPE_BASIC)) {
 					fRolesValue.add(R4EUserRole.R4E_ROLE_LEAD);
 				} else {
 					fRolesValue.add(R4EUserRole.R4E_ROLE_REVIEWER);
 				}
 			}
 
 			//Validate Focus Area (optional)
 			validateResult = validateEmptyInput(fFocusAreaTextField);
 			if (null == validateResult) {
 				fFocusAreaValue = fFocusAreaTextField.getText();
 			}
 
 			fParticipantDetailsValue = fParticipantDetailsInputTextField.getText(); //No validation needed
 		} else {
 			fParticipantIdValue = null;
 			fFocusAreaValue = null;
 			fParticipantEmailValue = null;
 			fParticipantDetailsValue = null;
 		}
 		R4EUIModelController.setJobInProgress(false); //Do this here to refresh the view properly
 		this.getShell().setCursor(this.getShell().getDisplay().getSystemCursor(SWT.CURSOR_ARROW));
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
 		shell.setText(ADD_PARTICIPANT_DIALOG_TITLE);
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
 		basicSection.setText("Basic Parameters");
 		basicSection.setDescription("Enter the mandatory basic parameters for this participant");
 		basicSection.addExpansionListener(new ExpansionAdapter() {
 			@Override
 			public void expansionStateChanged(ExpansionEvent e) {
 				getShell().setSize(getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT));
 			}
 		});
 
 		final Composite basicSectionClient = toolkit.createComposite(basicSection);
 		basicSectionClient.setLayout(layout);
 		basicSection.setClient(basicSectionClient);
 
 		//Participant Id
 		Label label = toolkit.createLabel(basicSectionClient, R4EUIConstants.ID_LABEL);
 		label.setToolTipText(R4EUIConstants.PARTICIPANT_ID_TOOLTIP);
 		label.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false));
 		fParticipantIdInputTextField = toolkit.createText(basicSectionClient, "", SWT.SINGLE | SWT.BORDER);
 		textGridData = new GridData(GridData.FILL, GridData.FILL, true, false);
 		textGridData.horizontalSpan = 2;
 		fParticipantIdInputTextField.setToolTipText(R4EUIConstants.PARTICIPANT_ID_TOOLTIP);
 		fParticipantIdInputTextField.setLayoutData(textGridData);
 		fParticipantIdInputTextField.addFocusListener(new FocusListener() {
 
 			public void focusLost(FocusEvent e) {
 				if (R4EUIModelController.isUserQueryAvailable()) {
 					if (fParticipantIdInputTextField.getText().length() > 0) {
 						fParticipantIdInputTextField.setText(fParticipantIdInputTextField.getText().toLowerCase());
 						getShell().setCursor(getShell().getDisplay().getSystemCursor(SWT.CURSOR_WAIT));
 						getUserInfo();
 						getShell().setCursor(getShell().getDisplay().getSystemCursor(SWT.CURSOR_ARROW));
 					} else {
 						fParticipantEmailInputTextField.setText("");
 						fParticipantDetailsInputTextField.setText("");
 					}
 
 				}
 			}
 
 			public void focusGained(FocusEvent e) {
 				//Nothing to do
 			}
 		});
 		fParticipantIdInputTextField.addModifyListener(new ModifyListener() {
 			public void modifyText(ModifyEvent e) {
 				// ignore
 				if (fParticipantIdInputTextField.getText().length() > 0
 						&& fParticipantEmailInputTextField.getText().length() > 0) {
 					getButton(IDialogConstants.OK_ID).setEnabled(true);
 				} else {
 					getButton(IDialogConstants.OK_ID).setEnabled(false);
 				}
 			}
 		});
 
 		//Find user button
 		final Button findUserButton = toolkit.createButton(basicSectionClient, FIND_BUTTON_LABEL, SWT.NONE);
 		final GridData buttonGridData = new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false);
 		buttonGridData.horizontalSpan = 1;
 		findUserButton.setToolTipText(R4EUIConstants.PARTICIPANT_FIND_USER_TOOLTIP);
 		findUserButton.setLayoutData(buttonGridData);
 		if (!R4EUIModelController.isUserQueryAvailable()) {
 			findUserButton.setEnabled(false);
 		}
 		findUserButton.addSelectionListener(new SelectionListener() {
 			public void widgetSelected(SelectionEvent e) {
 				final IFindUserDialog dialog = R4EUIDialogFactory.getInstance().getFindUserDialog();
 				dialog.create();
 				dialog.setDialogsDefaults();
 				final int result = dialog.open();
 
 				if (result == Window.OK) {
 					fParticipantIdInputTextField.setText(dialog.getUserIdValue());
 					fParticipantEmailInputTextField.setText(dialog.getUserEmailValue());
 					fParticipantDetailsInputTextField.setText(dialog.getUserDetailsValue());
 				}
 			}
 
 			public void widgetDefaultSelected(SelectionEvent e) {
 				//Nothing to do
 			}
 		});
 
 		//Participant Email
 		label = toolkit.createLabel(basicSectionClient, R4EUIConstants.EMAIL_LABEL);
 		label.setToolTipText(R4EUIConstants.PARTICIPANT_EMAIL_TOOLTIP);
 		label.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false));
 		fParticipantEmailInputTextField = toolkit.createText(basicSectionClient, "", SWT.SINGLE | SWT.BORDER);
 		textGridData = new GridData(GridData.FILL, GridData.FILL, true, false);
 		textGridData.horizontalSpan = 3;
 		fParticipantEmailInputTextField.setToolTipText(R4EUIConstants.PARTICIPANT_EMAIL_TOOLTIP);
 		fParticipantEmailInputTextField.setLayoutData(textGridData);
 		fParticipantEmailInputTextField.addModifyListener(new ModifyListener() {
 			public void modifyText(ModifyEvent e) {
 				// ignore
 				if (fParticipantIdInputTextField.getText().length() > 0
 						&& fParticipantEmailInputTextField.getText().length() > 0) {
 					getButton(IDialogConstants.OK_ID).setEnabled(true);
 				} else {
 					getButton(IDialogConstants.OK_ID).setEnabled(false);
 				}
 			}
 		});
 
 		//User details
 		label = toolkit.createLabel(basicSectionClient, R4EUIConstants.USER_DETAILS_LABEL);
 		label.setToolTipText(R4EUIConstants.PARTICIPANT_DETAILS_TOOLTIP);
 		label.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false));
 		fParticipantDetailsInputTextField = toolkit.createText(basicSectionClient, "", SWT.MULTI | SWT.V_SCROLL
 				| SWT.READ_ONLY);
 		textGridData = new GridData(GridData.FILL, GridData.FILL, true, false);
 		textGridData.horizontalSpan = 3;
 		textGridData.heightHint = fParticipantDetailsInputTextField.getLineHeight() << 3;
 		fParticipantDetailsInputTextField.setToolTipText(R4EUIConstants.PARTICIPANT_DETAILS_TOOLTIP);
 		fParticipantDetailsInputTextField.setLayoutData(textGridData);
 
 		//Extra parameters section
 		final Section extraSection = toolkit.createSection(composite, Section.DESCRIPTION
 				| ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE);
 		final GridData extraSectionGridData = new GridData(GridData.FILL, GridData.FILL, true, false);
 		extraSectionGridData.horizontalSpan = 4;
 		extraSection.setLayoutData(extraSectionGridData);
 		extraSection.setText("Extra Parameters");
 		extraSection.setDescription("Enter the optional extra parameters for this participant");
 		extraSection.addExpansionListener(new ExpansionAdapter() {
 			@Override
 			public void expansionStateChanged(ExpansionEvent e) {
 				getShell().setSize(getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT));
 			}
 		});
 
 		final Composite extraSectionClient = toolkit.createComposite(extraSection);
 		extraSectionClient.setLayout(layout);
 		extraSection.setClient(extraSectionClient);
 
 		//Roles
 		if (!R4EUIModelController.getActiveReview().getReview().getType().equals(R4EReviewType.R4E_REVIEW_TYPE_BASIC)) {
 			label = toolkit.createLabel(extraSectionClient, R4EUIConstants.ROLES_LABEL);
 			label.setToolTipText(R4EUIConstants.PARTICIPANT_ROLES_TOOLTIP);
 			label.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false));
 			textGridData = new GridData(GridData.FILL, GridData.FILL, true, false);
 			textGridData.horizontalSpan = 3;
 			fRoleTypes = new EditableListWidget(toolkit, extraSectionClient, textGridData, null, 0, CCombo.class,
 					R4EUIConstants.PARTICIPANT_ROLES);
 			fRoleTypes.setToolTipText(R4EUIConstants.PARTICIPANT_ROLES_TOOLTIP);
 		}
 
 		//Focus Area
 		label = toolkit.createLabel(extraSectionClient, R4EUIConstants.FOCUS_AREA_LABEL);
 		label.setToolTipText(R4EUIConstants.PARTICIPANT_FOCUS_AREA_TOOLTIP);
 		label.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false));
 		fFocusAreaTextField = toolkit.createText(extraSectionClient, "", SWT.SINGLE | SWT.V_SCROLL | SWT.BORDER);
 		textGridData = new GridData(GridData.FILL, GridData.FILL, true, false);
 		textGridData.horizontalSpan = 3;
 		textGridData.heightHint = fFocusAreaTextField.getLineHeight() * 3;
 		fFocusAreaTextField.setToolTipText(R4EUIConstants.PARTICIPANT_FOCUS_AREA_TOOLTIP);
 		fFocusAreaTextField.setLayoutData(textGridData);
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
 	 * @see org.eclipse.jface.dialogs.Dialog#isResizable()
 	 */
 	@Override
 	protected boolean isResizable() {
 		return true;
 	}
 
 	/**
 	 * Returns the string typed into this input dialog.
 	 * 
 	 * @return the participant id input string
 	 */
 	public String getParticipantIdValue() {
 		return fParticipantIdValue;
 	}
 
 	/**
 	 * Returns the string typed into this input dialog.
 	 * 
 	 * @return the participant email input string
 	 */
 	public String getParticipantEmailValue() {
 		return fParticipantEmailValue;
 	}
 
 	/**
 	 * Returns the string typed into this input dialog.
 	 * 
 	 * @return the participant details input string
 	 */
 	public String getParticipantDetailsValue() {
 		return fParticipantDetailsValue;
 	}
 
 	/**
 	 * Returns the participant role values
 	 * 
 	 * @return the participant roles as a List
 	 */
 	public List<R4EUserRole> getParticipantRolesValue() {
 		return fRolesValue;
 	}
 
 	/**
 	 * Returns the string typed into this input dialog.
 	 * 
 	 * @return the focus area input string
 	 */
 	public String getFocusAreaValue() {
 		return fFocusAreaValue;
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
 	 * Method getUserInfo.
 	 */
 	protected void getUserInfo() {
 		final IQueryUser query = new QueryUserFactory().getInstance();
 		try {
 			final List<IUserInfo> users = query.searchByUserId(fParticipantIdInputTextField.getText());
 
 			//Fill info with first user
 			for (IUserInfo user : users) {
 				if (user.getUserId().equals(fParticipantIdInputTextField.getText())) {
 					fParticipantEmailInputTextField.setText(user.getEmail());
 					fParticipantDetailsInputTextField.setText(UIUtils.buildUserDetailsString(user));
 					return;
 				}
 			}
 		} catch (NamingException e) {
 			R4EUIPlugin.Ftracer.traceError("Exception: " + e.toString() + " (" + e.getMessage() + ")");
 			R4EUIPlugin.getDefault().logError("Exception: " + e.toString(), e);
 		} catch (IOException e) {
 			R4EUIPlugin.Ftracer.traceError("Exception: " + e.toString() + " (" + e.getMessage() + ")");
 			R4EUIPlugin.getDefault().logError("Exception: " + e.toString(), e);
 		}
 	}
 }
