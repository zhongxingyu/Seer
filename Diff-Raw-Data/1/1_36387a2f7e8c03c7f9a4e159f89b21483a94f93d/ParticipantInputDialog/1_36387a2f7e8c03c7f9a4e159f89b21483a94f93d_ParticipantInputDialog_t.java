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
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.jface.dialogs.ErrorDialog;
 import org.eclipse.jface.dialogs.IDialogConstants;
 import org.eclipse.jface.dialogs.IInputValidator;
 import org.eclipse.jface.layout.TableColumnLayout;
 import org.eclipse.jface.resource.JFaceResources;
 import org.eclipse.jface.viewers.ColumnWeightData;
 import org.eclipse.jface.window.Window;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EParticipant;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EReviewType;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EUserRole;
 import org.eclipse.mylyn.reviews.r4e.core.model.RModelFactory;
 import org.eclipse.mylyn.reviews.r4e.ui.R4EUIPlugin;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIModelController;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIParticipant;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.utils.CommandUtils;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.utils.EditableListWidget;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.utils.IEditableListListener;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.utils.R4EUIConstants;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.utils.UIUtils;
 import org.eclipse.mylyn.reviews.userSearch.query.IQueryUser;
 import org.eclipse.mylyn.reviews.userSearch.query.QueryUserFactory;
 import org.eclipse.mylyn.reviews.userSearch.userInfo.IUserInfo;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.CCombo;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Item;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.swt.widgets.TableItem;
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
 public class ParticipantInputDialog extends FormDialog implements IParticipantInputDialog, IEditableListListener {
 
 	// ------------------------------------------------------------------------
 	// Constants
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Field ADD_PARTICIPANT_DIALOG_TITLE. (value is ""Enter Participants details"")
 	 */
 	private static final String ADD_PARTICIPANT_DIALOG_TITLE = "Enter Participants Details";
 
 	/**
 	 * Field PARTICIPANTS_GROUP_LABEL. (value is ""Participants"")
 	 */
 	private static final String PARTICIPANTS_GROUP_LABEL = "Participants";
 
 	/**
 	 * Field PARTICIPANT_LABEL. (value is ""participant"")
 	 */
 	private static final String PARTICIPANT_LABEL = "participant";
 
 	/**
 	 * Field ADD_BUTTON_LABEL. (value is ""Add"")
 	 */
 	private static final String ADD_BUTTON_LABEL = "Add";
 
 	/**
 	 * Field REMOVE_BUTTON_LABEL. (value is ""Erase"")
 	 */
 	private static final String REMOVE_BUTTON_LABEL = "Erase";
 
 	/**
 	 * Field FIND_BUTTON_LABEL. (value is ""Find"")
 	 */
 	private static final String FIND_BUTTON_LABEL = "Find";
 
 	/**
 	 * Field CLEAR_BUTTON_LABEL. (value is ""Clear"")
 	 */
 	private static final String CLEAR_BUTTON_LABEL = "Clear";
 
 	// ------------------------------------------------------------------------
 	// Member variables
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Field fParticipantIdInputTextField.
 	 */
 	protected Text fParticipantIdInputTextField;
 
 	/**
 	 * Field fParticipantEmailInputTextField.
 	 */
 	protected Text fParticipantEmailInputTextField;
 
 	/**
 	 * Field fParticipantDetailsInputTextField.
 	 */
 	protected Text fParticipantDetailsInputTextField;
 
 	/**
 	 * Field fParticipantsDetailsValue.
 	 */
 	private final List<String> fParticipantsDetailsValues = new ArrayList<String>();;
 
 	/**
 	 * Field fRoleValues.
 	 */
 	private EditableListWidget fRoleValues = null;
 
 	/**
 	 * Field fFocusAreaTextField.
 	 */
 	private Text fFocusAreaTextField;
 
 	/**
 	 * The input validator, or <code>null</code> if none.
 	 */
 	private final IInputValidator fValidator;
 
 	/**
 	 * Field fAddedParticipantsTable.
 	 */
 	private Table fAddedParticipantsTable;
 
 	/**
 	 * Field fUserToAddCombo.
 	 */
 	private Text fUserToAddCombo;
 
 	/**
 	 * Field fParticipants.
 	 */
 	private List<R4EParticipant> fParticipants = new ArrayList<R4EParticipant>();
 
 	/**
 	 * Field fSelectedParticipantIndex.
 	 */
 	protected int fSelectedParticipantIndex = R4EUIConstants.INVALID_VALUE;
 
 	/**
 	 * Field fAddUserButton.
 	 */
 	private Button fAddUserButton;
 
 	/**
 	 * Field fRemoveParticipantsButton.
 	 */
 	private Button fRemoveUserButton;
 
 	/**
 	 * Field fClearParticipantsButton.
 	 */
 	private Button fFindUserButton;
 
 	/**
 	 * Field fClearParticipantsButton.
 	 */
 	private Button fClearParticipantsButton;
 
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
 			List<R4EParticipant> validatedParticipants = new ArrayList<R4EParticipant>();
 
 			for (R4EParticipant participant : fParticipants) {
 				//Validate Participant Id
 				String validateResult = validateEmptyInput(participant.getId());
 				if (null != validateResult) {
 					//Validation of input failed
 					final ErrorDialog dialog = new ErrorDialog(null, R4EUIConstants.DIALOG_TITLE_ERROR,
 							"No input given for Participant Id", new Status(IStatus.ERROR, R4EUIPlugin.PLUGIN_ID, 0,
 									validateResult, null), IStatus.ERROR);
 					dialog.open();
 					continue;
 				}
 
 				//Check if participant already exists
 				if (R4EUIModelController.getActiveReview().getParticipantIDs().contains(participant.getId())) {
 					final ErrorDialog dialog = new ErrorDialog(null, R4EUIConstants.DIALOG_TITLE_ERROR,
 							"Cannot Add Participant " + participant.getId(), new Status(IStatus.ERROR,
 									R4EUIPlugin.PLUGIN_ID, 0, "Participant already part of this Review", null),
 							IStatus.ERROR);
 					dialog.open();
 					continue;
 				}
 
 				//Validate Participant Email
 				validateResult = validateEmptyInput(participant.getEmail());
 				if (null != validateResult) {
 					//Validation of input failed
 					final ErrorDialog dialog = new ErrorDialog(null, R4EUIConstants.DIALOG_TITLE_ERROR,
 							"No Email given for Participant " + participant.getId(), new Status(IStatus.ERROR,
 									R4EUIPlugin.PLUGIN_ID, 0, validateResult, null), IStatus.ERROR);
 					dialog.open();
 					continue;
 				}
 				if (!CommandUtils.isEmailValid(participant.getEmail())) {
 					continue;
 				}
 
 				//Validate Roles (optional)
 				if (participant.getRoles().size() == 0) {
 					//If there is no roles defined, put one as default depending on the review type
 					if (R4EUIModelController.getActiveReview()
 							.getReview()
 							.getType()
 							.equals(R4EReviewType.R4E_REVIEW_TYPE_BASIC)) {
 						participant.getRoles().add(R4EUserRole.R4E_ROLE_LEAD);
 					} else {
 						participant.getRoles().add(R4EUserRole.R4E_ROLE_REVIEWER);
 					}
 				}
 				validatedParticipants.add(participant);
 			}
 			//Set the participant list to include only the validated participants
 			fParticipants = validatedParticipants;
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
 		shell.setText(ADD_PARTICIPANT_DIALOG_TITLE);
 		shell.setMinimumSize(R4EUIConstants.DIALOG_DEFAULT_WIDTH, R4EUIConstants.DIALOG_DEFAULT_HEIGHT);
 	}
 
 	/**
 	 * Method open.
 	 * 
 	 * @return int
 	 * @see org.eclipse.ui.forms.FormDialog#open()
 	 */
 	@Override
 	public int open() {
 		fParticipantsDetailsValues.clear();
 		fParticipants.clear();
 		return super.open();
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
 
 		createParticipantsGroup(toolkit, composite);
 		createBasicParameters(toolkit, composite);
 		createExtraParameters(toolkit, composite);
 	}
 
 	/**
 	 * method createParticipantsGroup
 	 * 
 	 * @param aToolkit
 	 * @param aComposite
 	 */
 	private void createParticipantsGroup(FormToolkit aToolkit, Composite aComposite) {
 		//Users to Add
 		Group usersToAddGroup = new Group(aComposite, SWT.NONE);
 		usersToAddGroup.setText(PARTICIPANTS_GROUP_LABEL);
 		usersToAddGroup.setBackground(getShell().getDisplay().getSystemColor(SWT.COLOR_WHITE));
 		usersToAddGroup.setLayout(new GridLayout(4, false));
 		final GridData groupGridData = new GridData(GridData.FILL, GridData.FILL, true, true);
 		usersToAddGroup.setLayoutData(groupGridData);
 
 		//Participants data composite
 		Composite dataComposite = aToolkit.createComposite(usersToAddGroup);
 		dataComposite.setLayout(new GridLayout());
 		final GridData dataCompositeData = new GridData(GridData.FILL, GridData.FILL, true, true);
 		dataCompositeData.horizontalSpan = 3;
 		dataComposite.setLayoutData(dataCompositeData);
 
 		//Participants button composite
 		Composite buttonsComposite = aToolkit.createComposite(usersToAddGroup);
 		buttonsComposite.setLayout(new GridLayout());
 		final GridData buttonsCompositeData = new GridData(GridData.END, GridData.FILL, false, true);
 		buttonsComposite.setLayoutData(buttonsCompositeData);
 
 		//fUserToAddCombo = new CCombo(usersToAddGroup, SWT.SINGLE | SWT.BORDER);  //TODO add later
 		fUserToAddCombo = aToolkit.createText(dataComposite, "", SWT.SINGLE | SWT.BORDER);
 		GridData textGridData = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
 		fUserToAddCombo.setEditable(true);
 		fUserToAddCombo.setToolTipText(R4EUIConstants.PARTICIPANT_ADD_USER_TOOLTIP);
 		fUserToAddCombo.setLayoutData(textGridData);
 		fUserToAddCombo.addListener(SWT.Modify, new Listener() {
 			public void handleEvent(Event event) {
 				if (fUserToAddCombo.getText().trim().length() > 0) {
 					fAddUserButton.setEnabled(true);
 				} else {
 					fAddUserButton.setEnabled(false);
 				}
 			}
 		});
 		//Trap \R key (Return)
 		fUserToAddCombo.addListener(SWT.KeyDown, new Listener() {
 			public void handleEvent(Event event) {
 				if (event.character == '\r') {
 					getShell().setCursor(getShell().getDisplay().getSystemCursor(SWT.CURSOR_WAIT));
 
 					//Tokenize the users list
 					String[] users = fUserToAddCombo.getText().split(R4EUIConstants.LIST_SEPARATOR);
 					for (String user : users) {
 						addUserToParticipantList(user);
 					}
 					fUserToAddCombo.setText("");
 					getShell().setCursor(getShell().getDisplay().getSystemCursor(SWT.CURSOR_ARROW));
 				}
 			}
 		});
 
 		//Participants table
 		final Composite tableComposite = aToolkit.createComposite(dataComposite);
 		final GridData tableCompositeData = new GridData(GridData.FILL, GridData.FILL, true, true);
 		fAddedParticipantsTable = aToolkit.createTable(tableComposite, SWT.FULL_SELECTION | SWT.BORDER);
 		fAddedParticipantsTable.setHeaderVisible(true);
 		fAddedParticipantsTable.setLinesVisible(true);
 		fAddedParticipantsTable.setToolTipText(R4EUIConstants.PARTICIPANTS_ADD_TOOLTIP);
 		fAddedParticipantsTable.setLinesVisible(true);
 		fAddedParticipantsTable.setItemCount(0);
 		final GridData tableGridData = new GridData(GridData.FILL, GridData.FILL, true, true);
 		fAddedParticipantsTable.setLayoutData(tableGridData);
 
 		final TableColumnLayout tableColumnLayout = new TableColumnLayout();
 		TableColumn idColumn = new TableColumn(fAddedParticipantsTable, SWT.NONE, 0);
 		idColumn.setText(R4EUIConstants.ID_LABEL);
 		TableColumn emailColumn = new TableColumn(fAddedParticipantsTable, SWT.NONE, 1);
 		emailColumn.setText(R4EUIConstants.EMAIL_LABEL);
 
 		tableColumnLayout.setColumnData(idColumn, new ColumnWeightData(30, idColumn.getWidth() * 2, true));
 		tableColumnLayout.setColumnData(emailColumn, new ColumnWeightData(70, emailColumn.getWidth(), true));
 
 		tableComposite.setLayout(tableColumnLayout);
 		tableComposite.setLayoutData(tableCompositeData);
 
 		fAddedParticipantsTable.addListener(SWT.Selection, new Listener() {
 			public void handleEvent(Event event) {
 				fSelectedParticipantIndex = fAddedParticipantsTable.getSelectionIndex();
 				if (fSelectedParticipantIndex >= 0) {
 					R4EParticipant participant = fParticipants.get(fSelectedParticipantIndex);
 					if (null != participant) {
 						fParticipantIdInputTextField.setText(participant.getId());
 						if (null != participant.getEmail()) {
 							fParticipantEmailInputTextField.setText(participant.getEmail());
 						} else {
 							fParticipantEmailInputTextField.setText("");
 						}
 						if (fSelectedParticipantIndex < fParticipantsDetailsValues.size()) {
 							fParticipantDetailsInputTextField.setText(fParticipantsDetailsValues.get(fSelectedParticipantIndex));
 						} else {
 							fParticipantDetailsInputTextField.setText("");
 						}
 						fRoleValues.removeAll();
 						EList<R4EUserRole> roles = participant.getRoles();
 						for (R4EUserRole role : roles) {
 							Item newItem = fRoleValues.addItem();
 							newItem.setText(R4EUIParticipant.mapRoleToString(role));
 						}
 						if (null != participant.getFocusArea()) {
 							fFocusAreaTextField.setText(participant.getFocusArea());
 						} else {
 							fFocusAreaTextField.setText("");
 						}
 					}
 
 					//Make sure fields are enabled
 					fParticipantIdInputTextField.setEnabled(true);
 					fParticipantEmailInputTextField.setEnabled(true);
 					fParticipantDetailsInputTextField.setEnabled(true);
 					fRoleValues.setEnabled(true);
 					fFocusAreaTextField.setEnabled(true);
 				}
 			}
 		});
 
 		//Add user button
 		fAddUserButton = aToolkit.createButton(buttonsComposite, ADD_BUTTON_LABEL, SWT.NONE);
 		final GridData addButtonGridData = new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false);
 		fAddUserButton.setEnabled(false);
 		fAddUserButton.setToolTipText(R4EUIConstants.PARTICIPANT_ADD_USER_TOOLTIP);
 		fAddUserButton.setLayoutData(addButtonGridData);
 		fAddUserButton.addListener(SWT.Selection, new Listener() {
 			public void handleEvent(Event event) {
 				getShell().setCursor(getShell().getDisplay().getSystemCursor(SWT.CURSOR_WAIT));
 
 				//Tokenize the users list
 				String[] users = fUserToAddCombo.getText().split(R4EUIConstants.LIST_SEPARATOR);
 				for (String user : users) {
 					addUserToParticipantList(user);
 				}
 				getShell().setCursor(getShell().getDisplay().getSystemCursor(SWT.CURSOR_ARROW));
 			}
 		});
 
 		fFindUserButton = aToolkit.createButton(buttonsComposite, FIND_BUTTON_LABEL, SWT.NONE);
 		final GridData findButtonGridData = new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false);
 		fFindUserButton.setToolTipText(R4EUIConstants.PARTICIPANT_FIND_USER_TOOLTIP);
 		fFindUserButton.setLayoutData(findButtonGridData);
 		if (!R4EUIModelController.isUserQueryAvailable()) {
 			fFindUserButton.setEnabled(false);
 		}
 		fFindUserButton.addListener(SWT.Selection, new Listener() {
 			public void handleEvent(Event event) {
 				final IFindUserDialog dialog = R4EUIDialogFactory.getInstance().getFindUserDialog();
 				dialog.create();
 				dialog.setDialogsDefaults();
 				final int result = dialog.open();
 				if (result == Window.OK) {
 					List<IUserInfo> usersInfos = dialog.getUserInfos();
 					for (IUserInfo user : usersInfos) {
 						addUserToParticipantList(user);
 					}
 				}
 			}
 		});
 
 		fRemoveUserButton = aToolkit.createButton(buttonsComposite, REMOVE_BUTTON_LABEL, SWT.NONE);
 		final GridData removeButtonGridData = new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false);
 		fRemoveUserButton.setEnabled(false);
 		fRemoveUserButton.setToolTipText(R4EUIConstants.PARTICIPANT_REMOVE_TOOLTIP);
 		fRemoveUserButton.setLayoutData(removeButtonGridData);
 		if (!R4EUIModelController.isUserQueryAvailable()) {
 			fRemoveUserButton.setEnabled(false);
 		}
 		fRemoveUserButton.addListener(SWT.Selection, new Listener() {
 			public void handleEvent(Event event) {
 				fAddedParticipantsTable.remove(fSelectedParticipantIndex);
 				fParticipants.remove(fSelectedParticipantIndex);
 				fParticipantsDetailsValues.remove(fSelectedParticipantIndex);
 				clearParametersFields();
 			}
 		});
 
 		//Clear participants list button
 		fClearParticipantsButton = aToolkit.createButton(buttonsComposite, CLEAR_BUTTON_LABEL, SWT.NONE);
 		final GridData clearButtonGridData = new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false);
 		fClearParticipantsButton.setEnabled(false);
 		fClearParticipantsButton.setToolTipText(R4EUIConstants.PARTICIPANTS_CLEAR_TOOLTIP);
 		fClearParticipantsButton.setLayoutData(clearButtonGridData);
 		fClearParticipantsButton.addListener(SWT.Selection, new Listener() {
 			public void handleEvent(Event event) {
 				fParticipants.clear();
 				fParticipantsDetailsValues.clear();
 				fAddedParticipantsTable.removeAll();
 				fSelectedParticipantIndex = R4EUIConstants.INVALID_VALUE;
 				clearParametersFields();
 			}
 		});
 	}
 
 	/**
 	 * method createBasicParameters
 	 * 
 	 * @param aToolkit
 	 * @param aComposite
 	 */
 	private void createBasicParameters(FormToolkit aToolkit, Composite aComposite) {
 
 		//Basic parameters section
 		final Section basicSection = aToolkit.createSection(aComposite, Section.DESCRIPTION
 				| ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE | ExpandableComposite.EXPANDED);
 		final GridData basicSectionGridData = new GridData(GridData.FILL, GridData.FILL, true, false);
 		basicSectionGridData.horizontalSpan = 4;
 		basicSection.setLayoutData(basicSectionGridData);
 		basicSection.setText(R4EUIConstants.BASIC_PARAMS_HEADER);
 		basicSection.setDescription(R4EUIConstants.BASIC_PARAMS_HEADER_DETAILS + PARTICIPANT_LABEL);
 		basicSection.addExpansionListener(new ExpansionAdapter() {
 			@Override
 			public void expansionStateChanged(ExpansionEvent e) {
 				getShell().setSize(getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT));
 			}
 		});
 
 		final Composite basicSectionClient = aToolkit.createComposite(basicSection);
 		final GridLayout layout = new GridLayout(4, false);
 		basicSectionClient.setLayout(layout);
 		basicSection.setClient(basicSectionClient);
 
 		//Participant Id
 		Label label = aToolkit.createLabel(basicSectionClient, R4EUIConstants.ID_LABEL);
 		label.setToolTipText(R4EUIConstants.PARTICIPANT_ID_TOOLTIP);
 		label.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false));
 		aToolkit.setBorderStyle(SWT.NULL);
 		fParticipantIdInputTextField = aToolkit.createText(basicSectionClient, "");
 		GridData textGridData = new GridData(GridData.FILL, GridData.FILL, true, false);
 		textGridData.horizontalSpan = 3;
 		fParticipantIdInputTextField.setEnabled(false);
		fParticipantIdInputTextField.setEditable(false);
 		fParticipantIdInputTextField.setToolTipText(R4EUIConstants.PARTICIPANT_ID_TOOLTIP);
 		fParticipantIdInputTextField.setLayoutData(textGridData);
 
 		//Participant Email
 		label = aToolkit.createLabel(basicSectionClient, R4EUIConstants.EMAIL_LABEL);
 		label.setToolTipText(R4EUIConstants.PARTICIPANT_EMAIL_TOOLTIP);
 		label.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false));
 		aToolkit.setBorderStyle(SWT.BORDER);
 		fParticipantEmailInputTextField = aToolkit.createText(basicSectionClient, "", SWT.SINGLE);
 		textGridData = new GridData(GridData.FILL, GridData.FILL, true, false);
 		textGridData.horizontalSpan = 3;
 		fParticipantEmailInputTextField.setToolTipText(R4EUIConstants.PARTICIPANT_EMAIL_TOOLTIP);
 		fParticipantEmailInputTextField.setLayoutData(textGridData);
 		fParticipantEmailInputTextField.setEnabled(false);
 		fParticipantEmailInputTextField.addListener(SWT.FocusOut, new Listener() {
 			public void handleEvent(Event event) {
 				if (fSelectedParticipantIndex >= 0) {
 					R4EParticipant participant = fParticipants.get(fSelectedParticipantIndex);
 					participant.setEmail(fParticipantEmailInputTextField.getText());
 					TableItem item = fAddedParticipantsTable.getItem(fSelectedParticipantIndex);
 					if (null != participant.getEmail()) {
 						item.setFont(JFaceResources.getFontRegistry().get(JFaceResources.DEFAULT_FONT));
 						item.setText(1, participant.getEmail());
 					} else {
 						item.setFont(JFaceResources.getFontRegistry().getItalic(JFaceResources.DEFAULT_FONT));
 					}
 				}
 			}
 		});
 
 		//User details
 		label = aToolkit.createLabel(basicSectionClient, R4EUIConstants.USER_DETAILS_LABEL);
 		label.setToolTipText(R4EUIConstants.PARTICIPANT_DETAILS_TOOLTIP);
 		label.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false));
 		aToolkit.setBorderStyle(SWT.NULL);
 		fParticipantDetailsInputTextField = aToolkit.createText(basicSectionClient, "", SWT.MULTI | SWT.V_SCROLL
 				| SWT.READ_ONLY);
 		textGridData = new GridData(GridData.FILL, GridData.FILL, true, false);
 		textGridData.horizontalSpan = 3;
 		textGridData.heightHint = fParticipantDetailsInputTextField.getLineHeight() << 3;
 		fParticipantDetailsInputTextField.setEnabled(false);
 		fParticipantDetailsInputTextField.setEditable(false);
 		fParticipantDetailsInputTextField.setToolTipText(R4EUIConstants.PARTICIPANT_DETAILS_TOOLTIP);
 		fParticipantDetailsInputTextField.setLayoutData(textGridData);
 	}
 
 	/**
 	 * method createExtraParameters
 	 * 
 	 * @param aToolkit
 	 * @param aComposite
 	 */
 	private void createExtraParameters(FormToolkit aToolkit, Composite aComposite) {
 
 		//Extra parameters section
 		final Section extraSection = aToolkit.createSection(aComposite, Section.DESCRIPTION
 				| ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE);
 		final GridData extraSectionGridData = new GridData(GridData.FILL, GridData.FILL, true, false);
 		extraSectionGridData.horizontalSpan = 4;
 		extraSection.setLayoutData(extraSectionGridData);
 		extraSection.setText(R4EUIConstants.EXTRA_PARAMS_HEADER);
 		extraSection.setDescription(R4EUIConstants.EXTRA_PARAMS_HEADER_DETAILS + PARTICIPANT_LABEL);
 		extraSection.addExpansionListener(new ExpansionAdapter() {
 			@Override
 			public void expansionStateChanged(ExpansionEvent e) {
 				getShell().setSize(getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT));
 			}
 		});
 
 		final Composite extraSectionClient = aToolkit.createComposite(extraSection);
 		final GridLayout layout = new GridLayout(4, false);
 		extraSectionClient.setLayout(layout);
 		extraSection.setClient(extraSectionClient);
 
 		//Roles
 		if (!R4EUIModelController.getActiveReview().getReview().getType().equals(R4EReviewType.R4E_REVIEW_TYPE_BASIC)) {
 			Label label = aToolkit.createLabel(extraSectionClient, R4EUIConstants.ROLES_LABEL);
 			label.setToolTipText(R4EUIConstants.PARTICIPANT_ROLES_TOOLTIP);
 			label.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false));
 			GridData textGridData = new GridData(GridData.FILL, GridData.FILL, true, false);
 			textGridData.horizontalSpan = 3;
 			fRoleValues = new EditableListWidget(aToolkit, extraSectionClient, textGridData, this, 0, CCombo.class,
 					R4EUIConstants.PARTICIPANT_ROLES);
 			fRoleValues.setToolTipText(R4EUIConstants.PARTICIPANT_ROLES_TOOLTIP);
 			fRoleValues.setEnabled(false);
 
 		}
 
 		//Focus Area
 		Label label = aToolkit.createLabel(extraSectionClient, R4EUIConstants.FOCUS_AREA_LABEL);
 		label.setToolTipText(R4EUIConstants.PARTICIPANT_FOCUS_AREA_TOOLTIP);
 		label.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false));
 		aToolkit.setBorderStyle(SWT.BORDER);
 		fFocusAreaTextField = aToolkit.createText(extraSectionClient, "", SWT.SINGLE | SWT.V_SCROLL | SWT.BORDER);
 		GridData textGridData = new GridData(GridData.FILL, GridData.FILL, true, false);
 		textGridData.horizontalSpan = 3;
 		textGridData.heightHint = fFocusAreaTextField.getLineHeight() * 3;
 		fFocusAreaTextField.setEnabled(false);
 		fFocusAreaTextField.setToolTipText(R4EUIConstants.PARTICIPANT_FOCUS_AREA_TOOLTIP);
 		fFocusAreaTextField.setLayoutData(textGridData);
 		fFocusAreaTextField.addListener(SWT.FocusOut, new Listener() {
 			public void handleEvent(Event event) {
 				if (fSelectedParticipantIndex >= 0) {
 					R4EParticipant participant = fParticipants.get(fSelectedParticipantIndex);
 					participant.setFocusArea(fFocusAreaTextField.getText());
 				}
 			}
 		});
 
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
 	 * Method validateEmptyInput.
 	 * 
 	 * @param aText
 	 *            Text
 	 * @return String
 	 */
 	private String validateEmptyInput(String aText) {
 		return fValidator.isValid(aText);
 	}
 
 	/**
 	 * Method getParticpants.
 	 * 
 	 * @return List<R4EParticipant>
 	 */
 	public List<R4EParticipant> getParticipants() {
 		return fParticipants;
 	}
 
 	/**
 	 * Method clearParametersFields.
 	 */
 	private void clearParametersFields() {
 		fParticipantIdInputTextField.setText("");
 		fParticipantEmailInputTextField.setText("");
 		fParticipantDetailsInputTextField.setText("");
 		fRoleValues.removeAll();
 		fFocusAreaTextField.setText("");
 		if (fParticipants.size() == 0) {
 			fParticipantDetailsInputTextField.setEnabled(false);
 			fParticipantIdInputTextField.setEnabled(false);
 			fParticipantEmailInputTextField.setEnabled(false);
 			fRoleValues.setEnabled(false);
 			fFocusAreaTextField.setEnabled(false);
 			fClearParticipantsButton.setEnabled(false);
 			fRemoveUserButton.setEnabled(false);
 			getButton(IDialogConstants.OK_ID).setEnabled(false);
 		}
 	}
 
 	/**
 	 * Method addUserToParticipantList.
 	 * 
 	 * @param aUser
 	 *            - String
 	 */
 	private void addUserToParticipantList(String aUser) {
 		//TODO:  Here we need to resolve any group aliases to multiple users
 
 		//Resolve user
 		R4EParticipant participant = getParticipant(aUser.trim().toLowerCase());
 		if (null == participant) {
 			return; //Participant already in list
 		}
 		fParticipants.add(participant);
 
 		updateComponents(participant);
 	}
 
 	/**
 	 * Method addUserToParticipantList.
 	 * 
 	 * @param aUser
 	 *            - String
 	 */
 	private void addUserToParticipantList(IUserInfo aUserInfo) {
 		//TODO:  Here we need to resolve any group aliases to multiple users
 
 		//First check if the participant already exist in the participant list
 		for (R4EParticipant tmpPart : fParticipants) {
 			if (aUserInfo.getUserId().equalsIgnoreCase(tmpPart.getId())) {
 				return;
 			}
 		}
 
 		//Add User to List
 		R4EParticipant participant = RModelFactory.eINSTANCE.createR4EParticipant();
 		participant.setId(aUserInfo.getUserId());
 		participant.setEmail(aUserInfo.getEmail());
 		fParticipantsDetailsValues.add(UIUtils.buildUserDetailsString(aUserInfo));
 		fParticipants.add(participant);
 
 		updateComponents(participant);
 
 	}
 
 	/**
 	 * Method updateComponents.
 	 * 
 	 * @param aParticipant
 	 *            - R4EParticipant
 	 */
 	private void updateComponents(R4EParticipant aParticipant) {
 		//Add item to the participants table
 		TableItem item = new TableItem(fAddedParticipantsTable, SWT.NONE);
 		item.setText(0, aParticipant.getId());
 		if (null != aParticipant.getEmail() && !("".equals(aParticipant.getEmail()))) {
 			item.setFont(JFaceResources.getFontRegistry().get(JFaceResources.DEFAULT_FONT));
 			item.setText(1, aParticipant.getEmail());
 		} else {
 			//Mark table item as not complete
 			item.setFont(JFaceResources.getFontRegistry().getItalic(JFaceResources.DEFAULT_FONT));
 		}
 		fAddedParticipantsTable.showItem(item);
 
 		if (fParticipants.size() > 0) {
 			fClearParticipantsButton.setEnabled(true);
 			fRemoveUserButton.setEnabled(true);
 			fUserToAddCombo.setText("");
 			getButton(IDialogConstants.OK_ID).setEnabled(true);
 			getButton(IDialogConstants.OK_ID).setSelection(false);
 		} else {
 			getButton(IDialogConstants.OK_ID).setEnabled(false);
 		}
 	}
 
 	/**
 	 * Method getParticipant.
 	 * 
 	 * @param aId
 	 *            - String
 	 * @return R4EParticipant
 	 */
 	protected R4EParticipant getParticipant(String aId) {
 		//First check if the participant already exist in the participant list
 		for (R4EParticipant tmpPart : fParticipants) {
 			if (aId.equalsIgnoreCase(tmpPart.getId())) {
 				return null;
 			}
 		}
 		R4EParticipant participant = RModelFactory.eINSTANCE.createR4EParticipant();
 		if (R4EUIModelController.isUserQueryAvailable()) {
 			final IQueryUser query = new QueryUserFactory().getInstance();
 			try {
 				final List<IUserInfo> users = query.searchByUserId(aId);
 
 				//Fill info with first user returned
 				for (IUserInfo user : users) {
 					if (user.getUserId().toLowerCase().equals(aId)) {
 						participant.setId(user.getUserId().toLowerCase());
 						participant.setEmail(user.getEmail());
 						fParticipantsDetailsValues.add(UIUtils.buildUserDetailsString(user));
 						return participant;
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
 		participant.setId(aId);
 		fParticipantsDetailsValues.add("");
 		return participant;
 	}
 
 	/**
 	 * Method itemsUpdated.
 	 * 
 	 * @param aItems
 	 *            Item[]
 	 * @param aInstanceId
 	 *            int
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.utils.IEditableListListener#itemsUpdated(Item[], int)
 	 */
 	public void itemsUpdated(Item[] aItems, int aInstanceId) {
 		if (fSelectedParticipantIndex >= 0) {
 			final R4EParticipant participant = fParticipants.get(fSelectedParticipantIndex);
 			if (0 == aInstanceId) {
 				//Update roles
 				participant.getRoles().clear();
 				for (Item item : aItems) {
 					R4EUserRole role = R4EUIParticipant.mapStringToRole(item.getText());
 					if (null != role) {
 						participant.getRoles().add(role);
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * This is done to change the default button behavior of the OK button
 	 * 
 	 * @param parent
 	 *            the button bar composite
 	 * @see org.eclipse.jface.dialogs#createButtonsForButtonBar(Composite)
 	 */
 	@Override
 	protected void createButtonsForButtonBar(Composite parent) {
 		// create OK and Cancel buttons by default
 		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, false);
 		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
 	}
 }
