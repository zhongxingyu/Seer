 /*******************************************************************************
  * Copyright (c) 2011 Ericsson Research Canada
  * 
  * All rights reserved. This program and the accompanying materials are
  * made available under the terms of the Eclipse Public License v1.0 which
  * accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Description:
  * 
  * This class implements the tabbed property section for the additional properties
  * for the Review model element.
  * 
  * Contributors:
  *   Sebastien Dubois - Created for Mylyn Review R4E project
  *   
  ******************************************************************************/
 package org.eclipse.mylyn.reviews.r4e.ui.internal.properties.tabbed;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EMeetingData;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EParticipant;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EReview;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EReviewPhase;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EReviewState;
 import org.eclipse.mylyn.reviews.r4e.core.model.serial.impl.OutOfSyncException;
 import org.eclipse.mylyn.reviews.r4e.core.model.serial.impl.ResourceHandlingException;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIModelController;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIReviewBasic;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIReviewExtended;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIReviewGroup;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.utils.EditableListWidget;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.utils.IEditableListListener;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.utils.MailServicesProxy;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.utils.R4EUIConstants;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.utils.UIUtils;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.CCombo;
 import org.eclipse.swt.custom.CLabel;
 import org.eclipse.swt.events.FocusEvent;
 import org.eclipse.swt.events.FocusListener;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.layout.FormAttachment;
 import org.eclipse.swt.layout.FormData;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Item;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.forms.events.ExpansionAdapter;
 import org.eclipse.ui.forms.events.ExpansionEvent;
 import org.eclipse.ui.forms.widgets.ExpandableComposite;
 import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
 import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
 import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetWidgetFactory;
 
 /**
  * @author lmcdubo
  * @version $Revision: 1.0 $
  */
 public class ReviewExtraTabPropertySection extends ModelElementTabPropertySection implements IEditableListListener {
 
 	// ------------------------------------------------------------------------
 	// Constants
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Field DECISION_SECTION_LABEL. (value is ""Decision information"")
 	 */
 	private static final String DECISION_SECTION_LABEL = "Decision Information";
 
 	/**
 	 * Field REVIEW_MEETING_REFRESH_TOOLTIP. (value is ""Refresh meetinmg information for mail server"")
 	 */
 	private static final String REVIEW_MEETING_REFRESH_TOOLTIP = "Refresh meetinmg information for mail server";
 
 	// ------------------------------------------------------------------------
 	// Member variables
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Field fProjectCombo.
 	 */
 	protected CCombo fProjectCombo = null;
 
 	/**
 	 * Field FComponents.
 	 */
 	protected EditableListWidget fComponents = null;
 
 	/**
 	 * Field FEntryCriteriaText.
 	 */
 	protected Text fEntryCriteriaText = null;
 
 	/**
 	 * Field FObjectivesText.
 	 */
 	protected Text fObjectivesText = null;
 
 	/**
 	 * Field FReferenceMaterialText.
 	 */
 	protected Text fReferenceMaterialText = null;
 
 	/**
 	 * Field fExitDecision.
 	 */
 	protected CCombo fExitDecisionCombo = null;
 
 	/**
 	 * Field fDecisionUsersList.
 	 */
 	protected EditableListWidget fDecisionUsersList = null;
 
 	/**
 	 * Field fDecisionUsersListLabel.
 	 */
 	protected CLabel fDecisionUsersListLabel = null;
 
 	/**
 	 * Field fDecisionTimeSpentText.
 	 */
 	protected Text fDecisionTimeSpentText = null;
 
 	/**
 	 * Field fDecisionTimeSpentLabel.
 	 */
 	protected CLabel fDecisionTimeSpentLabel = null;
 
 	/**
 	 * Field fMeetingComposite.
 	 */
 	private Composite fMeetingComposite = null;
 
 	/**
 	 * Field fMeetingUpdateButton.
 	 */
 	private Button fMeetingUpdateButton = null;
 
 	/**
 	 * Field fMeetingRefreshButton.
 	 */
 	private Button fMeetingRefreshButton = null;
 
 	/**
 	 * Field fMeetingSubjectLabel.
 	 */
 	protected CLabel fMeetingSubjectLabel = null;
 
 	/**
 	 * Field fMeetingStartTimeLabel.
 	 */
 	protected CLabel fMeetingStartTimeLabel = null;
 
 	/**
 	 * Field fMeetingEndTimeLabel.
 	 */
 	protected CLabel fMeetingDurationLabel = null;
 
 	/**
 	 * Field fMeetingLocationLabel.
 	 */
 	protected CLabel fMeetingLocationLabel = null;
 
 	// ------------------------------------------------------------------------
 	// Methods
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Method createControls.
 	 * 
 	 * @param parent
 	 *            Composite
 	 * @param aTabbedPropertySheetPage
 	 *            TabbedPropertySheetPage
 	 * @see org.eclipse.ui.views.properties.tabbed.ISection#createControls(Composite, TabbedPropertySheetPage)
 	 */
 	@Override
 	public void createControls(Composite parent, TabbedPropertySheetPage aTabbedPropertySheetPage) {
 		super.createControls(parent.getParent(), aTabbedPropertySheetPage);
 
 		final TabbedPropertySheetWidgetFactory widgetFactory = aTabbedPropertySheetPage.getWidgetFactory();
 		FormData data = null;
 		final Composite mainForm = widgetFactory.createFlatFormComposite(parent);
 
 		//Project
 		fProjectCombo = widgetFactory.createCCombo(mainForm, SWT.READ_ONLY);
 		data = new FormData();
 		data.left = new FormAttachment(0, R4EUIConstants.TABBED_PROPERTY_LABEL_WIDTH);
 		data.right = new FormAttachment(100, 0); // $codepro.audit.disable numericLiterals
 		data.top = new FormAttachment(mainForm, ITabbedPropertyConstants.VSPACE);
 		fProjectCombo.setToolTipText(R4EUIConstants.REVIEW_PROJECT_TOOLTIP);
 		fProjectCombo.setLayoutData(data);
 		fProjectCombo.addSelectionListener(new SelectionListener() {
 			public void widgetSelected(SelectionEvent e) {
 				if (!fRefreshInProgress) {
 					try {
 						final String currentUser = R4EUIModelController.getReviewer();
 						final R4EReview modelReview = ((R4EUIReviewBasic) fProperties.getElement()).getReview();
 						final Long bookNum = R4EUIModelController.FResourceUpdater.checkOut(modelReview, currentUser);
 						modelReview.setProject(fProjectCombo.getText());
 						R4EUIModelController.FResourceUpdater.checkIn(bookNum);
 					} catch (ResourceHandlingException e1) {
 						UIUtils.displayResourceErrorDialog(e1);
 					} catch (OutOfSyncException e1) {
 						UIUtils.displaySyncErrorDialog(e1);
 					}
 				}
 				refresh();
 			}
 
 			public void widgetDefaultSelected(SelectionEvent e) { // $codepro.audit.disable emptyMethod
 				//No implementation needed
 			}
 		});
 
 		final CLabel projectLabel = widgetFactory.createCLabel(mainForm, R4EUIConstants.PROJECT_LABEL);
 		data = new FormData();
 		data.left = new FormAttachment(0, 0);
 		data.right = new FormAttachment(fProjectCombo, -ITabbedPropertyConstants.HSPACE);
 		data.top = new FormAttachment(fProjectCombo, 0, SWT.TOP);
 		projectLabel.setToolTipText(R4EUIConstants.REVIEW_PROJECT_TOOLTIP);
 		projectLabel.setLayoutData(data);
 
 		//Components (Read-only)
 		data = new FormData();
 		data.left = new FormAttachment(0, R4EUIConstants.TABBED_PROPERTY_LABEL_WIDTH);
 		data.right = new FormAttachment(100, 0); // $codepro.audit.disable numericLiterals
 		data.top = new FormAttachment(fProjectCombo, ITabbedPropertyConstants.VSPACE);
 		fComponents = new EditableListWidget(widgetFactory, mainForm, data, this, 1, CCombo.class, null);
 		fComponents.setToolTipText(R4EUIConstants.REVIEW_COMPONENTS_TOOLTIP);
 
 		final CLabel componentsLabel = widgetFactory.createCLabel(mainForm, R4EUIConstants.COMPONENTS_LABEL);
 		data = new FormData();
 		data.left = new FormAttachment(0, 0);
 		data.right = new FormAttachment(fComponents.getComposite(), -ITabbedPropertyConstants.HSPACE);
 		data.top = new FormAttachment(fComponents.getComposite(), 0, SWT.TOP);
 		componentsLabel.setToolTipText(R4EUIConstants.REVIEW_COMPONENTS_TOOLTIP);
 		componentsLabel.setLayoutData(data);
 
 		//Entry Criteria
 		fEntryCriteriaText = widgetFactory.createText(mainForm, "", SWT.BORDER);
 		data = new FormData();
 		data.left = new FormAttachment(0, R4EUIConstants.TABBED_PROPERTY_LABEL_WIDTH);
 		data.right = new FormAttachment(100, 0); // $codepro.audit.disable numericLiterals
 		data.top = new FormAttachment(fComponents.getComposite(), ITabbedPropertyConstants.VSPACE);
 		fEntryCriteriaText.setToolTipText(R4EUIConstants.REVIEW_ENTRY_CRITERIA_TOOLTIP);
 		fEntryCriteriaText.setLayoutData(data);
 
 		final CLabel entryCriteriaLabel = widgetFactory.createCLabel(mainForm, R4EUIConstants.ENTRY_CRITERIA_LABEL);
 		data = new FormData();
 		data.left = new FormAttachment(0, 0);
 		data.right = new FormAttachment(fEntryCriteriaText, -ITabbedPropertyConstants.HSPACE);
 		data.top = new FormAttachment(fEntryCriteriaText, 0, SWT.TOP);
 		entryCriteriaLabel.setToolTipText(R4EUIConstants.REVIEW_ENTRY_CRITERIA_TOOLTIP);
 		entryCriteriaLabel.setLayoutData(data);
 		fEntryCriteriaText.addFocusListener(new FocusListener() {
 			public void focusLost(FocusEvent e) {
 				if (!fRefreshInProgress) {
 					try {
 						final String currentUser = R4EUIModelController.getReviewer();
 						final R4EReview modelReview = ((R4EUIReviewBasic) fProperties.getElement()).getReview();
 						final Long bookNum = R4EUIModelController.FResourceUpdater.checkOut(modelReview, currentUser);
 						modelReview.setEntryCriteria(fEntryCriteriaText.getText());
 						R4EUIModelController.FResourceUpdater.checkIn(bookNum);
 					} catch (ResourceHandlingException e1) {
 						UIUtils.displayResourceErrorDialog(e1);
 					} catch (OutOfSyncException e1) {
 						UIUtils.displaySyncErrorDialog(e1);
 					}
 				}
 			}
 
 			public void focusGained(FocusEvent e) { // $codepro.audit.disable emptyMethod
 				//Nothing to do
 			}
 		});
 		UIUtils.addTabbedPropertiesTextResizeListener(fEntryCriteriaText);
 
 		//Objectives
 		fObjectivesText = widgetFactory.createText(mainForm, "", SWT.BORDER);
 		data = new FormData();
 		data.left = new FormAttachment(0, R4EUIConstants.TABBED_PROPERTY_LABEL_WIDTH);
 		data.right = new FormAttachment(100, 0); // $codepro.audit.disable numericLiterals
 		data.top = new FormAttachment(fEntryCriteriaText, ITabbedPropertyConstants.VSPACE);
 		fObjectivesText.setToolTipText(R4EUIConstants.REVIEW_OBJECTIVES_TOOLTIP);
 		fObjectivesText.setLayoutData(data);
 		fObjectivesText.addFocusListener(new FocusListener() {
 			public void focusLost(FocusEvent e) {
 				if (!fRefreshInProgress) {
 					try {
 						final String currentUser = R4EUIModelController.getReviewer();
 						final R4EReview modelReview = ((R4EUIReviewBasic) fProperties.getElement()).getReview();
 						final Long bookNum = R4EUIModelController.FResourceUpdater.checkOut(modelReview, currentUser);
 						modelReview.setObjectives(fObjectivesText.getText());
 						R4EUIModelController.FResourceUpdater.checkIn(bookNum);
 					} catch (ResourceHandlingException e1) {
 						UIUtils.displayResourceErrorDialog(e1);
 					} catch (OutOfSyncException e1) {
 						UIUtils.displaySyncErrorDialog(e1);
 					}
 				}
 			}
 
 			public void focusGained(FocusEvent e) { // $codepro.audit.disable emptyMethod
 				//Nothing to do
 			}
 		});
 		UIUtils.addTabbedPropertiesTextResizeListener(fObjectivesText);
 
 		final CLabel objectivesLabel = widgetFactory.createCLabel(mainForm, R4EUIConstants.OBJECTIVES_LABEL);
 		data = new FormData();
 		data.left = new FormAttachment(0, 0);
 		data.right = new FormAttachment(fObjectivesText, -ITabbedPropertyConstants.HSPACE);
 		data.top = new FormAttachment(fObjectivesText, 0, SWT.TOP);
 		objectivesLabel.setToolTipText(R4EUIConstants.REVIEW_OBJECTIVES_TOOLTIP);
 		objectivesLabel.setLayoutData(data);
 
 		//Reference Material
 		fReferenceMaterialText = widgetFactory.createText(mainForm, "", SWT.BORDER);
 		data = new FormData();
 		data.left = new FormAttachment(0, R4EUIConstants.TABBED_PROPERTY_LABEL_WIDTH);
 		data.right = new FormAttachment(100, 0); // $codepro.audit.disable numericLiterals
 		data.top = new FormAttachment(fObjectivesText, ITabbedPropertyConstants.VSPACE);
 		fReferenceMaterialText.setToolTipText(R4EUIConstants.REVIEW_REFERENCE_MATERIAL_TOOLTIP);
 		fReferenceMaterialText.setLayoutData(data);
 		fReferenceMaterialText.addFocusListener(new FocusListener() {
 			public void focusLost(FocusEvent e) {
 				if (!fRefreshInProgress) {
 					try {
 						final String currentUser = R4EUIModelController.getReviewer();
 						final R4EReview modelReview = ((R4EUIReviewBasic) fProperties.getElement()).getReview();
 						final Long bookNum = R4EUIModelController.FResourceUpdater.checkOut(modelReview, currentUser);
 						modelReview.setReferenceMaterial(fReferenceMaterialText.getText());
 						R4EUIModelController.FResourceUpdater.checkIn(bookNum);
 					} catch (ResourceHandlingException e1) {
 						UIUtils.displayResourceErrorDialog(e1);
 					} catch (OutOfSyncException e1) {
 						UIUtils.displaySyncErrorDialog(e1);
 					}
 				}
 			}
 
 			public void focusGained(FocusEvent e) { // $codepro.audit.disable emptyMethod
 				//Nothing to do
 			}
 		});
 		UIUtils.addTabbedPropertiesTextResizeListener(fReferenceMaterialText);
 
 		final CLabel referenceMaterialLabel = widgetFactory.createCLabel(mainForm,
 				R4EUIConstants.REFERENCE_MATERIAL_LABEL);
 		data = new FormData();
 		data.left = new FormAttachment(0, 0);
 		data.right = new FormAttachment(fReferenceMaterialText, -ITabbedPropertyConstants.HSPACE);
 		data.top = new FormAttachment(fReferenceMaterialText, 0, SWT.TOP);
 		referenceMaterialLabel.setToolTipText(R4EUIConstants.REVIEW_REFERENCE_MATERIAL_TOOLTIP);
 		referenceMaterialLabel.setLayoutData(data);
 
 		//Decision section
 		final ExpandableComposite decisionSection = widgetFactory.createExpandableComposite(mainForm,
 				ExpandableComposite.TWISTIE | ExpandableComposite.EXPANDED);
 		data = new FormData();
 		data.left = new FormAttachment(0, 0);
 		data.right = new FormAttachment(100, 0); // $codepro.audit.disable numericLiterals
 		data.top = new FormAttachment(fReferenceMaterialText, ITabbedPropertyConstants.VSPACE);
 		data.bottom = new FormAttachment(100, 0); // $codepro.audit.disable numericLiterals
 		decisionSection.setLayoutData(data);
 		decisionSection.setText(DECISION_SECTION_LABEL);
 		decisionSection.addExpansionListener(new ExpansionAdapter() {
 			@Override
 			public void expansionStateChanged(ExpansionEvent e) {
 				mainForm.layout();
 			}
 		});
 		decisionSection.setLayout(new GridLayout(1, false));
 
 		final Composite decisionSectionClient = widgetFactory.createComposite(decisionSection);
 		decisionSectionClient.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
 		decisionSectionClient.setLayout(new GridLayout(4, false));
 		decisionSection.setClient(decisionSectionClient);
 
 		//Scheduled Meetings
 		final CLabel meetingInfoLabel = widgetFactory.createCLabel(decisionSectionClient,
 				R4EUIConstants.DECISION_MEETING_LABEL);
 		meetingInfoLabel.setToolTipText(R4EUIConstants.REVIEW_MEETING_TOOLTIP);
 		meetingInfoLabel.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false));
 
 		//Meeting composite
 		fMeetingComposite = widgetFactory.createComposite(decisionSectionClient, SWT.BORDER);
 		GridData textGridData = new GridData(GridData.FILL, GridData.FILL, true, true);
 		textGridData.horizontalSpan = 3;
 		fMeetingComposite.setLayoutData(textGridData);
 		fMeetingComposite.setLayout(new GridLayout(4, false));
 
 		//Meeting Subject
 		final CLabel meetingSubjectLabel = widgetFactory.createCLabel(fMeetingComposite, R4EUIConstants.SUBJECT_LABEL);
 		meetingSubjectLabel.setToolTipText(R4EUIConstants.REVIEW_MEETING_SUBJECT_TOOLTIP);
 		meetingSubjectLabel.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false));
 		fMeetingSubjectLabel = widgetFactory.createCLabel(fMeetingComposite, null);
 		textGridData = new GridData(GridData.FILL, GridData.FILL, false, false);
 		textGridData.horizontalSpan = 2;
 		fMeetingSubjectLabel.setToolTipText(R4EUIConstants.REVIEW_MEETING_SUBJECT_TOOLTIP);
 		fMeetingSubjectLabel.setLayoutData(textGridData);
 
 		//Meeting update button
 		fMeetingUpdateButton = widgetFactory.createButton(fMeetingComposite, R4EUIConstants.UPDATE_LABEL, SWT.PUSH);
 		fMeetingUpdateButton.setToolTipText(R4EUIConstants.REVIEW_MEETING_UPDATE_TOOLTIP);
 		fMeetingUpdateButton.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false));
 		fMeetingUpdateButton.addSelectionListener(new SelectionListener() {
 			public void widgetSelected(SelectionEvent e) {
 				try {
 					MailServicesProxy.sendMeetingRequest();
 				} catch (ResourceHandlingException e1) {
 					UIUtils.displayResourceErrorDialog(e1);
 				} catch (OutOfSyncException e1) {
 					UIUtils.displaySyncErrorDialog(e1);
 				} finally {
 					R4EUIModelController.setDialogOpen(false);
 				}
 			}
 
 			public void widgetDefaultSelected(SelectionEvent e) {
 				//Nothing to do
 			}
 		});
 
 		//Meeting Start Time
 		final CLabel meetingStartTimeLabel = widgetFactory.createCLabel(fMeetingComposite,
 				R4EUIConstants.START_TIME_LABEL);
 		meetingStartTimeLabel.setToolTipText(R4EUIConstants.REVIEW_MEETING_TIME_TOOLTIP);
 		meetingStartTimeLabel.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false));
 		fMeetingStartTimeLabel = widgetFactory.createCLabel(fMeetingComposite, "");
 		textGridData = new GridData(GridData.FILL, GridData.FILL, false, false);
 		textGridData.horizontalSpan = 2;
 		fMeetingStartTimeLabel.setToolTipText(R4EUIConstants.REVIEW_MEETING_TIME_TOOLTIP);
 		fMeetingStartTimeLabel.setLayoutData(textGridData);
 
 		//Meeting refresh button
 		fMeetingRefreshButton = widgetFactory.createButton(fMeetingComposite, R4EUIConstants.REFRESH_LABEL, SWT.PUSH);
 		fMeetingRefreshButton.setToolTipText(REVIEW_MEETING_REFRESH_TOOLTIP);
 		fMeetingRefreshButton.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false));
 		fMeetingRefreshButton.addSelectionListener(new SelectionListener() {
 			public void widgetSelected(SelectionEvent e) {
 				try {
 					((R4EUIReviewBasic) fProperties.getElement()).refreshMeetingData();
 				} catch (OutOfSyncException ex) {
 					UIUtils.displaySyncErrorDialog(ex);
 				} catch (ResourceHandlingException ex) {
 					UIUtils.displayResourceErrorDialog(ex);
 				}
 			}
 
 			public void widgetDefaultSelected(SelectionEvent e) {
 				//Nothing to do
 			}
 		});
 
 		//Meeting Duration
 		final CLabel meetingDurationLabel = widgetFactory.createCLabel(fMeetingComposite, R4EUIConstants.DURATION_LABEL);
 		meetingDurationLabel.setToolTipText(R4EUIConstants.REVIEW_MEETING_DURATION_TOOLTIP);
 		meetingDurationLabel.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false));
 		fMeetingDurationLabel = widgetFactory.createCLabel(fMeetingComposite, "");
 		textGridData = new GridData(GridData.FILL, GridData.FILL, false, false);
 		textGridData.horizontalSpan = 2;
 		fMeetingDurationLabel.setToolTipText(R4EUIConstants.REVIEW_MEETING_DURATION_TOOLTIP);
 		fMeetingDurationLabel.setLayoutData(textGridData);
 		widgetFactory.createCLabel(fMeetingComposite, ""); //dummy label for alignment purposes
 
 		//Meeting Location
 		final CLabel meetingLocationLabel = widgetFactory.createCLabel(fMeetingComposite, R4EUIConstants.LOCATION_LABEL);
 		meetingLocationLabel.setToolTipText(R4EUIConstants.REVIEW_MEETING_LOCATION_TOOLTIP);
 		meetingLocationLabel.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false));
 		fMeetingLocationLabel = widgetFactory.createCLabel(fMeetingComposite, "");
 		textGridData = new GridData(GridData.FILL, GridData.FILL, true, false);
 		textGridData.horizontalSpan = 2;
 		fMeetingLocationLabel.setToolTipText(R4EUIConstants.REVIEW_MEETING_LOCATION_TOOLTIP);
 		fMeetingLocationLabel.setLayoutData(textGridData);
 		widgetFactory.createCLabel(fMeetingComposite, ""); //dummy label for alignment purposes
 
 		//Exit Decision
 		final CLabel exitDecisionLabel = widgetFactory.createCLabel(decisionSectionClient,
 				R4EUIConstants.EXIT_DECISION_LABEL);
 		exitDecisionLabel.setToolTipText(R4EUIConstants.REVIEW_EXIT_DECISION_TOOLTIP);
 		exitDecisionLabel.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false));
 
 		fExitDecisionCombo = widgetFactory.createCCombo(decisionSectionClient, SWT.READ_ONLY);
 		textGridData = new GridData(GridData.FILL, GridData.FILL, true, false);
 		textGridData.horizontalSpan = 3;
 		fExitDecisionCombo.setToolTipText(R4EUIConstants.REVIEW_EXIT_DECISION_TOOLTIP);
 		fExitDecisionCombo.setLayoutData(textGridData);
 		fExitDecisionCombo.addSelectionListener(new SelectionListener() {
 			public void widgetSelected(SelectionEvent e) {
 				if (!fRefreshInProgress) {
 					try {
 						final String currentUser = R4EUIModelController.getReviewer();
 						final R4EReview modelReview = ((R4EUIReviewBasic) fProperties.getElement()).getReview();
 						final Long bookNum = R4EUIModelController.FResourceUpdater.checkOut(modelReview, currentUser);
 						modelReview.setDecision(R4EUIReviewBasic.getDecisionValueFromString(fExitDecisionCombo.getText()));
 						R4EUIModelController.FResourceUpdater.checkIn(bookNum);
 					} catch (ResourceHandlingException e1) {
 						UIUtils.displayResourceErrorDialog(e1);
 					} catch (OutOfSyncException e1) {
 						UIUtils.displaySyncErrorDialog(e1);
 					}
 				}
 				refresh();
 			}
 
 			public void widgetDefaultSelected(SelectionEvent e) { // $codepro.audit.disable emptyMethod
 				//No implementation needed
 			}
 		});
 
 		//Decision Participants
 		fDecisionUsersListLabel = widgetFactory.createCLabel(decisionSectionClient,
 				R4EUIConstants.DECISION_PARTICIPANTS_LABEL);
 		fDecisionUsersListLabel.setToolTipText(R4EUIConstants.REVIEW_EXIT_DECISION_PARTICIPANTS_TOOLTIP);
 		fDecisionUsersListLabel.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false));
 
 		List<String> participants = null;
 		if (null != R4EUIModelController.getActiveReview()) {
 			participants = R4EUIModelController.getActiveReview().getParticipantIDs();
 		} else {
 			participants = new ArrayList<String>();
 		}
 		textGridData = new GridData(GridData.FILL, GridData.FILL, true, false);
 		textGridData.horizontalSpan = 3;
 		fDecisionUsersList = new EditableListWidget(widgetFactory, decisionSectionClient, textGridData, this, 2,
 				CCombo.class, participants.toArray(new String[participants.size()]));
 		fDecisionUsersList.setToolTipText(R4EUIConstants.REVIEW_EXIT_DECISION_PARTICIPANTS_TOOLTIP);
 
 		//Decision Time Spent
 		fDecisionTimeSpentLabel = widgetFactory.createCLabel(decisionSectionClient,
 				R4EUIConstants.DECISION_TIME_SPENT_LABEL);
 		fDecisionTimeSpentLabel.setToolTipText(R4EUIConstants.REVIEW_EXIT_DECISION_TIME_SPENT_TOOLTIP);
 		fDecisionTimeSpentLabel.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false));
 
 		fDecisionTimeSpentText = widgetFactory.createText(decisionSectionClient, "");
 		textGridData = new GridData(GridData.FILL, GridData.FILL, true, false);
 		textGridData.horizontalSpan = 3;
 		fDecisionTimeSpentText.setToolTipText(R4EUIConstants.REVIEW_EXIT_DECISION_TIME_SPENT_TOOLTIP);
 		fDecisionTimeSpentText.setLayoutData(textGridData);
 		fDecisionTimeSpentText.addFocusListener(new FocusListener() {
 			public void focusLost(FocusEvent e) {
 				if (!fRefreshInProgress) {
 					try {
 						final String currentUser = R4EUIModelController.getReviewer();
 						final R4EReview modelReview = ((R4EUIReviewExtended) fProperties.getElement()).getReview();
 						final Long bookNum = R4EUIModelController.FResourceUpdater.checkOut(modelReview, currentUser);
						modelReview.getDecision().setSpentTime(
								Integer.valueOf(fDecisionTimeSpentText.getText()).intValue());
 						R4EUIModelController.FResourceUpdater.checkIn(bookNum);
 					} catch (ResourceHandlingException e1) {
 						UIUtils.displayResourceErrorDialog(e1);
 					} catch (OutOfSyncException e1) {
 						UIUtils.displaySyncErrorDialog(e1);
 					}
 				}
 			}
 
 			public void focusGained(FocusEvent e) { // $codepro.audit.disable emptyMethod
 				//Nothing to do
 			}
 		});
 	}
 
 	/**
 	 * Method refresh.
 	 * 
 	 * @see org.eclipse.ui.views.properties.tabbed.ISection#refresh()
 	 */
 	@Override
 	public void refresh() {
 		fRefreshInProgress = true;
 		final R4EReview modelReview = ((R4EUIReviewBasic) fProperties.getElement()).getReview();
 		final String[] availableProjects = (String[]) ((R4EUIReviewGroup) ((R4EUIReviewBasic) fProperties.getElement()).getParent()).getGroup()
 				.getAvailableProjects()
 				.toArray();
 		fProjectCombo.setItems(availableProjects);
 		final String project = modelReview.getProject();
 		for (int i = 0; i < availableProjects.length; i++) {
 			if (project.equals(availableProjects[i])) {
 				fProjectCombo.select(i);
 				break;
 			}
 		}
 		fComponents.setEditableValues((String[]) ((R4EUIReviewGroup) ((R4EUIReviewBasic) fProperties.getElement()).getParent()).getGroup()
 				.getAvailableComponents()
 				.toArray());
 		final String[] components = (String[]) modelReview.getComponents().toArray();
 		fComponents.clearAll();
 		Item item = null;
 		String component = null;
 
 		for (int i = 0; i < components.length; i++) {
 			component = components[i];
 			if (i >= fComponents.getItemCount()) {
 				item = fComponents.addItem();
 			} else {
 				item = fComponents.getItem(i);
 				if (null == item) {
 					item = fComponents.addItem();
 				}
 			}
 			item.setText(component);
 		}
 
 		fEntryCriteriaText.setText(modelReview.getEntryCriteria());
 		fObjectivesText.setText(modelReview.getObjectives());
 		fReferenceMaterialText.setText(modelReview.getReferenceMaterial());
 
 		final R4EMeetingData meetingData = modelReview.getActiveMeeting();
 		if (null != meetingData) {
 			fMeetingSubjectLabel.setText(meetingData.getSubject());
 			final SimpleDateFormat dateFormat = new SimpleDateFormat(R4EUIConstants.SIMPLE_DATE_FORMAT_MINUTES);
 			fMeetingStartTimeLabel.setText(dateFormat.format(new Date(meetingData.getStartTime())));
 			fMeetingDurationLabel.setText(Integer.toString(meetingData.getDuration()));
 			fMeetingLocationLabel.setText(meetingData.getLocation());
 		} else {
 			fMeetingSubjectLabel.setText("");
 			fMeetingStartTimeLabel.setText("");
 			fMeetingDurationLabel.setText("");
 			fMeetingLocationLabel.setText("");
 		}
 
 		fExitDecisionCombo.setItems(R4EUIReviewBasic.getExitDecisionValues());
 		if (null != modelReview.getDecision()) {
 			fExitDecisionCombo.select((null == modelReview.getDecision().getValue()) ? 0 : modelReview.getDecision()
 					.getValue()
 					.getValue());
 		} else {
 			fExitDecisionCombo.setText("");
 		}
 
 		if (fProperties.getElement() instanceof R4EUIReviewExtended) {
 			final List<R4EParticipant> participants = ((R4EUIReviewBasic) fProperties.getElement()).getParticipants();
 
 			item = null;
 			final int numParticipants = participants.size();
 			fDecisionUsersList.clearAll();
 			for (int i = 0; i < numParticipants; i++) {
 				if (participants.get(i).isIsPartOfDecision()) {
 					if (i >= fDecisionUsersList.getItemCount()) {
 						item = fDecisionUsersList.addItem();
 					} else {
 						item = fDecisionUsersList.getItem(i);
 						if (null == item) {
 							item = fDecisionUsersList.addItem();
 						}
 					}
 					item.setText(participants.get(i).getId());
 				}
 			}
 			if (null != modelReview.getDecision()) {
 				fDecisionTimeSpentText.setText(Integer.valueOf(modelReview.getDecision().getSpentTime()).toString());
 			} else {
 				fDecisionTimeSpentText.setText("");
 			}
 		}
 		setEnabledFields();
 		fRefreshInProgress = false;
 	}
 
 	/**
 	 * Method setEnabledFields.
 	 */
 	@Override
 	protected void setEnabledFields() {
 		if (R4EUIModelController.isDialogOpen()
 				|| (!((R4EUIReviewBasic) fProperties.getElement()).isOpen())
 				|| ((R4EReviewState) ((R4EUIReviewBasic) fProperties.getElement()).getReview().getState()).getState()
 						.equals(R4EReviewPhase.R4E_REVIEW_PHASE_COMPLETED)) {
 			fProjectCombo.setEnabled(false);
 			fComponents.setEnabled(false);
 			fEntryCriteriaText.setEnabled(false);
 			fObjectivesText.setEnabled(false);
 			fReferenceMaterialText.setEnabled(false);
 			fExitDecisionCombo.setEnabled(false);
 			fMeetingUpdateButton.setEnabled(false);
 			fMeetingRefreshButton.setEnabled(false);
 
 			if (fProperties.getElement() instanceof R4EUIReviewExtended) {
 				fDecisionUsersList.setEnabled(false);
 				fDecisionTimeSpentText.setEnabled(false);
 				fDecisionUsersListLabel.setVisible(true);
 				fDecisionUsersList.setVisible(true);
 				fDecisionTimeSpentText.setVisible(true);
 				fDecisionTimeSpentLabel.setVisible(true);
 			} else {
 				fDecisionUsersListLabel.setVisible(false);
 				fDecisionUsersList.setVisible(false);
 				fDecisionTimeSpentText.setVisible(false);
 				fDecisionTimeSpentLabel.setVisible(false);
 			}
 		} else {
 			fProjectCombo.setEnabled(true);
 			fComponents.setEnabled(true);
 			fEntryCriteriaText.setEnabled(true);
 			fObjectivesText.setEnabled(true);
 			fReferenceMaterialText.setEnabled(true);
 			fMeetingUpdateButton.setEnabled(true);
 			fMeetingRefreshButton.setEnabled(true);
 
 			if (fProperties.getElement() instanceof R4EUIReviewExtended) {
 				final R4EUIReviewExtended uiReview = (R4EUIReviewExtended) fProperties.getElement();
 
 				if (uiReview.isDecisionDateEnabled()) {
 					fDecisionUsersList.setEnabled(true);
 					fDecisionTimeSpentText.setEnabled(true);
 					fExitDecisionCombo.setEnabled(true);
 
 				} else {
 					fDecisionUsersList.setEnabled(false);
 					fDecisionTimeSpentText.setEnabled(false);
 					fExitDecisionCombo.setEnabled(false);
 				}
 				fDecisionUsersListLabel.setVisible(true);
 				fDecisionUsersList.setVisible(true);
 				fDecisionTimeSpentText.setVisible(true);
 				fDecisionTimeSpentLabel.setVisible(true);
 			} else {
 				fDecisionUsersListLabel.setVisible(false);
 				fDecisionUsersList.setVisible(false);
 				fDecisionTimeSpentText.setVisible(false);
 				fDecisionTimeSpentLabel.setVisible(false);
 				if (((R4EUIReviewBasic) fProperties.getElement()).isExitDecisionEnabled()) {
 					fExitDecisionCombo.setEnabled(true);
 				} else {
 					fExitDecisionCombo.setEnabled(false);
 				}
 			}
 		}
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
 		try {
 			final R4EReview modelReview = ((R4EUIReviewBasic) fProperties.getElement()).getReview();
 			final String currentUser = R4EUIModelController.getReviewer();
 			final Long bookNum = R4EUIModelController.FResourceUpdater.checkOut(modelReview, currentUser);
 
 			if (1 == aInstanceId) {
 				//Update components
 				modelReview.getComponents().clear();
 				for (Item item : aItems) {
 					modelReview.getComponents().add(item.getText());
 				}
 			} else { //aInstanceId == 2
 				for (Item item : aItems) {
 					R4EParticipant participant = (R4EParticipant) modelReview.getUsersMap().get(item.getText());
 					if (null != participant) {
 						participant.setIsPartOfDecision(true);
 					}
 				}
 			}
 			R4EUIModelController.FResourceUpdater.checkIn(bookNum);
 			refresh();
 		} catch (ResourceHandlingException e1) {
 			UIUtils.displayResourceErrorDialog(e1);
 		} catch (OutOfSyncException e1) {
 			UIUtils.displaySyncErrorDialog(e1);
 		}
 	}
 }
