 // $codepro.audit.disable com.instantiations.assist.eclipse.analysis.audit.rule.effectivejava.alwaysOverridetoString.alwaysOverrideToString, staticFieldSecurity, com.instantiations.assist.eclipse.analysis.deserializeabilitySecurity, com.instantiations.assist.eclipse.analysis.enforceCloneableUsageSecurity, com.instantiations.assist.eclipse.analysis.instanceFieldSecurity, sourceLength, explicitThisUsage
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
  * This class implements the tabbed property section for the Review Item model 
  * element
  * 
  * Contributors:
  *   Sebastien Dubois - Created for Mylyn Review R4E project
  *   
  ******************************************************************************/
 
 package org.eclipse.mylyn.reviews.r4e.ui.internal.properties.tabbed;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.List;
 import java.util.TimeZone;
 
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EDecision;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EFormalReview;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EMeetingData;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EParticipant;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EReview;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EReviewDecision;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EReviewPhase;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EReviewPhaseInfo;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EReviewState;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EReviewType;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EUser;
 import org.eclipse.mylyn.reviews.r4e.core.model.serial.Persistence.RModelFactoryExt;
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
 import org.eclipse.swt.custom.ScrolledComposite;
 import org.eclipse.swt.custom.TableEditor;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.layout.FormAttachment;
 import org.eclipse.swt.layout.FormData;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Item;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.swt.widgets.TableItem;
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
 public class ReviewTabPropertySection extends ModelElementTabPropertySection implements IEditableListListener {
 
 	// ------------------------------------------------------------------------
 	// Constants
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Field REVIEW_DETAILS_SECTION_LABEL. (value is ""Review Details"")
 	 */
 	private static final String REVIEW_DETAILS_SECTION_LABEL = "Review Details";
 
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
 	 * Field fNameText.
 	 */
 	private Text fNameText = null;
 
 	/**
 	 * Field fPhaseCombo.
 	 */
 	protected CCombo fPhaseCombo = null;
 
 	/**
 	 * Field fStartDateText.
 	 */
 	private Text fStartDateText = null;
 
 	/**
 	 * Field fEndDateText.
 	 */
 	private Text fEndDateText = null;
 
 	/**
 	 * Field fDescriptionText.
 	 */
 	protected Text fDescriptionText = null;
 
 	/**
 	 * Field fColumnPhase.
 	 */
 	protected TableColumn fColumnPhase = null;
 
 	/**
 	 * Field fColumnOwner.
 	 */
 	protected TableColumn fColumnOwner = null;
 
 	/**
 	 * Field fColumnStartDate.
 	 */
 	protected TableColumn fColumnStartDate = null;
 
 	/**
 	 * Field fColumnEndDate.
 	 */
 	protected TableColumn fColumnEndDate = null;
 
 	/**
 	 * Field fDescriptionText.
 	 */
 	protected Table fPhaseTable = null;
 
 	/**
 	 * Field fPhaseMapLabel.
 	 */
 	private CLabel fPhaseMapLabel = null;
 
 	/**
 	 * Field fPhasePlanning.
 	 */
 	private TableItem fPhasePlanning = null;
 
 	/**
 	 * Field fPhasePreparation.
 	 */
 	private TableItem fPhasePreparation = null;
 
 	/**
 	 * Field fPhaseDecision.
 	 */
 	private TableItem fPhaseDecision = null;
 
 	/**
 	 * Field fPhaseRework.
 	 */
 	private TableItem fPhaseRework = null;
 
 	/**
 	 * Field fPlanningPhaseOwnerCombo.
 	 */
 	protected CCombo fPlanningPhaseOwnerCombo = null;
 
 	/**
 	 * Field fPreparationPhaseOwnerCombo.
 	 */
 	protected CCombo fPreparationPhaseOwnerCombo = null;
 
 	/**
 	 * Field fDecisionPhaseOwnerCombo.
 	 */
 	protected CCombo fDecisionPhaseOwnerCombo = null;
 
 	/**
 	 * Field fReworkPhaseOwnerCombo.
 	 */
 	protected CCombo fReworkPhaseOwnerCombo = null;
 
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
 	 * Field fDecisionSection.
 	 */
 	private ExpandableComposite fDecisionSection = null;
 
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
 	protected Text fMeetingSubjectLabel = null;
 
 	/**
 	 * Field fMeetingStartTimeLabel.
 	 */
 	protected Text fMeetingStartTimeLabel = null;
 
 	/**
 	 * Field fMeetingEndTimeLabel.
 	 */
 	protected Text fMeetingDurationLabel = null;
 
 	/**
 	 * Field fMeetingLocationLabel.
 	 */
 	protected Text fMeetingLocationLabel = null;
 
 	// ------------------------------------------------------------------------
 	// Methods
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Method shouldUseExtraSpace.
 	 * 
 	 * @return boolean
 	 * @see org.eclipse.ui.views.properties.tabbed.ISection#shouldUseExtraSpace()
 	 */
 	@Override
 	public boolean shouldUseExtraSpace() {
 		return true;
 	}
 
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
 		super.createControls(parent, aTabbedPropertySheetPage);
 
 		//Tell element to build its own detailed tab layout
 		final TabbedPropertySheetWidgetFactory widgetFactory = aTabbedPropertySheetPage.getWidgetFactory();
 		final Composite mainForm = widgetFactory.createFlatFormComposite(parent);
 		FormData data = null;
 
 		//Review Name (read-only)
 		widgetFactory.setBorderStyle(SWT.NULL);
 		fNameText = widgetFactory.createText(mainForm, "", SWT.NULL);
 		data = new FormData();
 		data.left = new FormAttachment(0, R4EUIConstants.TABBED_PROPERTY_LABEL_WIDTH);
 		data.right = new FormAttachment(100, 0); // $codepro.audit.disable numericLiterals
 		data.top = new FormAttachment(0, ITabbedPropertyConstants.VSPACE);
 		fNameText.setEditable(false);
 		fNameText.setToolTipText(R4EUIConstants.REVIEW_NAME_TOOLTIP);
 		fNameText.setLayoutData(data);
 
 		final CLabel nameLabel = widgetFactory.createCLabel(mainForm, R4EUIConstants.NAME_LABEL);
 		data = new FormData();
 		data.left = new FormAttachment(0, 0);
 		data.right = new FormAttachment(fNameText, -ITabbedPropertyConstants.HSPACE);
 		data.top = new FormAttachment(fNameText, 0, SWT.CENTER);
 		nameLabel.setToolTipText(R4EUIConstants.REVIEW_NAME_TOOLTIP);
 		nameLabel.setLayoutData(data);
 
 		//Phase
 		widgetFactory.setBorderStyle(SWT.BORDER);
 		fPhaseCombo = widgetFactory.createCCombo(mainForm, SWT.READ_ONLY);
 		data = new FormData();
 		data.left = new FormAttachment(0, R4EUIConstants.TABBED_PROPERTY_LABEL_WIDTH);
 		data.right = new FormAttachment(100, 0); // $codepro.audit.disable numericLiterals
 		data.top = new FormAttachment(fNameText, ITabbedPropertyConstants.VSPACE);
 		fPhaseCombo.setToolTipText(R4EUIConstants.REVIEW_PHASE_TOOLTIP);
 		fPhaseCombo.setLayoutData(data);
 		fPhaseCombo.addListener(SWT.Selection, new Listener() {
 			public void handleEvent(Event event) {
 				R4EReviewPhase phase = null;
 				if (fProperties.getElement() instanceof R4EUIReviewExtended) {
 					phase = ((R4EUIReviewExtended) fProperties.getElement()).getPhaseFromString(fPhaseCombo.getText());
 				} else {
 					phase = ((R4EUIReviewBasic) fProperties.getElement()).getPhaseFromString(fPhaseCombo.getText());
 				}
 				if (!fRefreshInProgress) {
 					UIUtils.changeReviewPhase(fProperties.getElement(), phase);
 					refresh();
 					R4EUIModelController.getNavigatorView().getTreeViewer().refresh();
 				}
 			}
 		});
 		addScrollListener(fPhaseCombo);
 
 		final CLabel phaseLabel = widgetFactory.createCLabel(mainForm, R4EUIConstants.PHASE_LABEL);
 		data = new FormData();
 		data.left = new FormAttachment(0, 0);
 		data.right = new FormAttachment(fPhaseCombo, -ITabbedPropertyConstants.HSPACE);
 		data.top = new FormAttachment(fPhaseCombo, 0, SWT.CENTER);
 		phaseLabel.setToolTipText(R4EUIConstants.REVIEW_PHASE_TOOLTIP);
 		phaseLabel.setLayoutData(data);
 
 		//Review Description
 		fDescriptionText = widgetFactory.createText(mainForm, "", SWT.MULTI);
 		data = new FormData();
 		data.left = new FormAttachment(0, R4EUIConstants.TABBED_PROPERTY_LABEL_WIDTH);
 		data.right = new FormAttachment(100, 0); // $codepro.audit.disable numericLiterals
 		data.top = new FormAttachment(fPhaseCombo, ITabbedPropertyConstants.VSPACE);
 		fDescriptionText.setToolTipText(R4EUIConstants.REVIEW_DESCRIPTION_TOOLTIP);
 		fDescriptionText.setLayoutData(data);
 		fDescriptionText.addListener(SWT.FocusOut, new Listener() {
 			public void handleEvent(Event event) {
 				if (!fRefreshInProgress && fDescriptionText.getForeground().equals(UIUtils.ENABLED_FONT_COLOR)) {
 					try {
 						final String currentUser = R4EUIModelController.getReviewer();
 						final R4EReview modelReview = ((R4EUIReviewBasic) fProperties.getElement()).getReview();
 						String newValue = fDescriptionText.getText().trim();
 						if (!newValue.equals(modelReview.getExtraNotes())) {
 							final Long bookNum = R4EUIModelController.FResourceUpdater.checkOut(modelReview,
 									currentUser);
 							modelReview.setExtraNotes(newValue);
 							R4EUIModelController.FResourceUpdater.checkIn(bookNum);
 						}
 						fDescriptionText.setText(newValue);
 					} catch (ResourceHandlingException e1) {
 						UIUtils.displayResourceErrorDialog(e1);
 					} catch (OutOfSyncException e1) {
 						UIUtils.displaySyncErrorDialog(e1);
 					}
 				}
 			}
 		});
 		UIUtils.addTabbedPropertiesTextResizeListener(fDescriptionText);
 
 		final CLabel descriptionLabel = widgetFactory.createCLabel(mainForm, R4EUIConstants.DESCRIPTION_LABEL);
 		data = new FormData();
 		data.left = new FormAttachment(0, 0);
 		data.right = new FormAttachment(fDescriptionText, -ITabbedPropertyConstants.HSPACE);
 		data.top = new FormAttachment(fDescriptionText, 0, SWT.CENTER);
 		descriptionLabel.setToolTipText(R4EUIConstants.REVIEW_DESCRIPTION_TOOLTIP);
 		descriptionLabel.setLayoutData(data);
 
 		createPhaseTable(widgetFactory, mainForm,
 				createDecisionSection(widgetFactory, mainForm, createReviewDetailsSection(widgetFactory, mainForm)));
 	}
 
 	/**
 	 * Method createReviewDetailsSection.
 	 * 
 	 * @param aWidgetFactory
 	 *            TabbedPropertySheetWidgetFactory
 	 * @param aComposite
 	 *            Composite
 	 * @return Composite
 	 */
 	private Composite createReviewDetailsSection(TabbedPropertySheetWidgetFactory aWidgetFactory,
 			final Composite aComposite) {
 		//Review Details section
 		final ExpandableComposite reviewDetailsSection = aWidgetFactory.createExpandableComposite(aComposite,
 				ExpandableComposite.TWISTIE);
 		final FormData data = new FormData();
 		data.left = new FormAttachment(0, 0);
 		data.right = new FormAttachment(100, 0); // $codepro.audit.disable numericLiterals
 		data.top = new FormAttachment(fDescriptionText, ITabbedPropertyConstants.VSPACE);
 		reviewDetailsSection.setLayoutData(data);
 		reviewDetailsSection.setText(REVIEW_DETAILS_SECTION_LABEL);
 		reviewDetailsSection.addExpansionListener(new ExpansionAdapter() {
 			@Override
 			public void expansionStateChanged(ExpansionEvent e) {
 				final ScrolledComposite scrolledParent = (ScrolledComposite) aComposite.getParent()
 						.getParent()
 						.getParent()
 						.getParent()
 						.getParent();
 				scrolledParent.setMinSize(aComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
 				scrolledParent.layout(true, true);
 			}
 		});
 		reviewDetailsSection.setLayout(new GridLayout(1, false));
 
 		final Composite reviewDetailsSectionClient = aWidgetFactory.createComposite(reviewDetailsSection);
 		reviewDetailsSectionClient.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 		reviewDetailsSectionClient.setLayout(new GridLayout(4, false));
 		reviewDetailsSection.setClient(reviewDetailsSectionClient);
 
 		//Review Start Date (read-only)
 		final CLabel startDateLabel = aWidgetFactory.createCLabel(reviewDetailsSectionClient,
 				R4EUIConstants.START_DATE_LABEL);
 		GridData gridData = new GridData(SWT.FILL, SWT.CENTER, false, false);
 		gridData.horizontalSpan = 1;
 		startDateLabel.setToolTipText(R4EUIConstants.REVIEW_START_DATE_TOOLTIP);
 		startDateLabel.setLayoutData(gridData);
 
 		aWidgetFactory.setBorderStyle(SWT.NULL);
 		fStartDateText = aWidgetFactory.createText(reviewDetailsSectionClient, "", SWT.NULL);
 		gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
 		gridData.horizontalSpan = 3;
 		fStartDateText.setEditable(false);
 		fStartDateText.setToolTipText(R4EUIConstants.REVIEW_START_DATE_TOOLTIP);
 		fStartDateText.setLayoutData(gridData);
 
 		//End Date (read-only)
 		final CLabel endDateLabel = aWidgetFactory.createCLabel(reviewDetailsSectionClient,
 				R4EUIConstants.END_DATE_LABEL);
 		gridData = new GridData(SWT.FILL, SWT.CENTER, false, false);
 		gridData.horizontalSpan = 1;
 		endDateLabel.setToolTipText(R4EUIConstants.REVIEW_END_DATE_TOOLTIP);
 		endDateLabel.setLayoutData(gridData);
 
 		fEndDateText = aWidgetFactory.createText(reviewDetailsSectionClient, "", SWT.NULL);
 		gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
 		gridData.horizontalSpan = 3;
 		fEndDateText.setEditable(false);
 		fEndDateText.setToolTipText(R4EUIConstants.REVIEW_END_DATE_TOOLTIP);
 		fEndDateText.setLayoutData(gridData);
 
 		//Project
 		final CLabel projectLabel = aWidgetFactory.createCLabel(reviewDetailsSectionClient,
 				R4EUIConstants.PROJECT_LABEL);
 		gridData = new GridData(SWT.FILL, SWT.CENTER, false, false);
 		gridData.horizontalSpan = 1;
 		projectLabel.setToolTipText(R4EUIConstants.REVIEW_PROJECT_TOOLTIP);
 		projectLabel.setLayoutData(gridData);
 
 		aWidgetFactory.setBorderStyle(SWT.BORDER);
 		fProjectCombo = aWidgetFactory.createCCombo(reviewDetailsSectionClient);
 		gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
 		gridData.horizontalSpan = 3;
 		fProjectCombo.setToolTipText(R4EUIConstants.REVIEW_PROJECT_TOOLTIP);
 		fProjectCombo.setLayoutData(gridData);
 		fProjectCombo.addListener(SWT.Selection, new Listener() {
 			public void handleEvent(Event event) {
 				if (!fRefreshInProgress) {
 					try {
 						final String currentUser = R4EUIModelController.getReviewer();
 						final R4EReview modelReview = ((R4EUIReviewBasic) fProperties.getElement()).getReview();
 						if (!fProjectCombo.getText().equals(modelReview.getProject())) {
 							final Long bookNum = R4EUIModelController.FResourceUpdater.checkOut(modelReview,
 									currentUser);
 							modelReview.setProject(fProjectCombo.getText());
 							R4EUIModelController.FResourceUpdater.checkIn(bookNum);
 						}
 					} catch (ResourceHandlingException e1) {
 						UIUtils.displayResourceErrorDialog(e1);
 					} catch (OutOfSyncException e1) {
 						UIUtils.displaySyncErrorDialog(e1);
 					}
 				}
 				refresh();
 			}
 		});
 		addScrollListener(fProjectCombo);
 
 		//Components (Read-only)
 		final CLabel componentsLabel = aWidgetFactory.createCLabel(reviewDetailsSectionClient,
 				R4EUIConstants.COMPONENTS_LABEL);
 		gridData = new GridData(SWT.FILL, SWT.CENTER, false, false);
 		gridData.horizontalSpan = 1;
 		componentsLabel.setToolTipText(R4EUIConstants.REVIEW_COMPONENTS_TOOLTIP);
 		componentsLabel.setLayoutData(gridData);
 
 		gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
 		gridData.horizontalSpan = 3;
 		fComponents = new EditableListWidget(aWidgetFactory, reviewDetailsSectionClient, gridData, this, 1,
 				CCombo.class, null);
 		fComponents.setToolTipText(R4EUIConstants.REVIEW_COMPONENTS_TOOLTIP);
 
 		//Entry Criteria
 		final CLabel entryCriteriaLabel = aWidgetFactory.createCLabel(reviewDetailsSectionClient,
 				R4EUIConstants.ENTRY_CRITERIA_LABEL);
 		gridData = new GridData(SWT.FILL, SWT.CENTER, false, false);
 		gridData.horizontalSpan = 1;
 		entryCriteriaLabel.setToolTipText(R4EUIConstants.REVIEW_ENTRY_CRITERIA_TOOLTIP);
 		entryCriteriaLabel.setLayoutData(gridData);
 
 		fEntryCriteriaText = aWidgetFactory.createText(reviewDetailsSectionClient, "", SWT.MULTI);
 		gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
 		gridData.horizontalSpan = 3;
 		fEntryCriteriaText.setToolTipText(R4EUIConstants.REVIEW_ENTRY_CRITERIA_TOOLTIP);
 		fEntryCriteriaText.setLayoutData(gridData);
 		fEntryCriteriaText.addListener(SWT.FocusOut, new Listener() {
 			public void handleEvent(Event event) {
 				if (!fRefreshInProgress && fEntryCriteriaText.getForeground().equals(UIUtils.ENABLED_FONT_COLOR)) {
 					try {
 						final String currentUser = R4EUIModelController.getReviewer();
 						final R4EReview modelReview = ((R4EUIReviewBasic) fProperties.getElement()).getReview();
 						String newValue = fEntryCriteriaText.getText().trim();
 						if (!newValue.equals(modelReview.getEntryCriteria())) {
 							final Long bookNum = R4EUIModelController.FResourceUpdater.checkOut(modelReview,
 									currentUser);
 							modelReview.setEntryCriteria(newValue);
 							R4EUIModelController.FResourceUpdater.checkIn(bookNum);
 						}
 						fEntryCriteriaText.setText(newValue);
 					} catch (ResourceHandlingException e1) {
 						UIUtils.displayResourceErrorDialog(e1);
 					} catch (OutOfSyncException e1) {
 						UIUtils.displaySyncErrorDialog(e1);
 					}
 				}
 			}
 		});
 		UIUtils.addTabbedPropertiesTextResizeListener(fEntryCriteriaText);
 
 		//Objectives
 		final CLabel objectivesLabel = aWidgetFactory.createCLabel(reviewDetailsSectionClient,
 				R4EUIConstants.OBJECTIVES_LABEL);
 		gridData = new GridData(SWT.FILL, SWT.CENTER, false, false);
 		gridData.horizontalSpan = 1;
 		objectivesLabel.setToolTipText(R4EUIConstants.REVIEW_OBJECTIVES_TOOLTIP);
 		objectivesLabel.setLayoutData(gridData);
 
 		fObjectivesText = aWidgetFactory.createText(reviewDetailsSectionClient, "", SWT.MULTI);
 		gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
 		gridData.horizontalSpan = 3;
 		fObjectivesText.setToolTipText(R4EUIConstants.REVIEW_OBJECTIVES_TOOLTIP);
 		fObjectivesText.setLayoutData(gridData);
 		fObjectivesText.addListener(SWT.FocusOut, new Listener() {
 			public void handleEvent(Event event) {
 				if (!fRefreshInProgress && fObjectivesText.getForeground().equals(UIUtils.ENABLED_FONT_COLOR)) {
 					try {
 						final String currentUser = R4EUIModelController.getReviewer();
 						final R4EReview modelReview = ((R4EUIReviewBasic) fProperties.getElement()).getReview();
 						String newValue = fObjectivesText.getText().trim();
 						if (!newValue.equals(modelReview.getObjectives())) {
 							final Long bookNum = R4EUIModelController.FResourceUpdater.checkOut(modelReview,
 									currentUser);
 							modelReview.setObjectives(newValue);
 							R4EUIModelController.FResourceUpdater.checkIn(bookNum);
 						}
 						fObjectivesText.setText(newValue);
 					} catch (ResourceHandlingException e1) {
 						UIUtils.displayResourceErrorDialog(e1);
 					} catch (OutOfSyncException e1) {
 						UIUtils.displaySyncErrorDialog(e1);
 					}
 				}
 			}
 		});
 		UIUtils.addTabbedPropertiesTextResizeListener(fObjectivesText);
 
 		//Reference Material
 		final CLabel referenceMaterialLabel = aWidgetFactory.createCLabel(reviewDetailsSectionClient,
 				R4EUIConstants.REFERENCE_MATERIAL_LABEL);
 		gridData = new GridData(SWT.FILL, SWT.CENTER, false, false);
 		gridData.horizontalSpan = 1;
 		referenceMaterialLabel.setToolTipText(R4EUIConstants.REVIEW_REFERENCE_MATERIAL_TOOLTIP);
 		referenceMaterialLabel.setLayoutData(gridData);
 
 		fReferenceMaterialText = aWidgetFactory.createText(reviewDetailsSectionClient, "", SWT.MULTI);
 		gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
 		gridData.horizontalSpan = 3;
 		fReferenceMaterialText.setToolTipText(R4EUIConstants.REVIEW_REFERENCE_MATERIAL_TOOLTIP);
 		fReferenceMaterialText.setLayoutData(gridData);
 		fReferenceMaterialText.addListener(SWT.FocusOut, new Listener() {
 			public void handleEvent(Event event) {
 				if (!fRefreshInProgress && fReferenceMaterialText.getForeground().equals(UIUtils.ENABLED_FONT_COLOR)) {
 					try {
 						final String currentUser = R4EUIModelController.getReviewer();
 						final R4EReview modelReview = ((R4EUIReviewBasic) fProperties.getElement()).getReview();
 						String newValue = fReferenceMaterialText.getText().trim();
 						if (!newValue.equals(modelReview.getReferenceMaterial())) {
 							final Long bookNum = R4EUIModelController.FResourceUpdater.checkOut(modelReview,
 									currentUser);
 							modelReview.setReferenceMaterial(newValue);
 							R4EUIModelController.FResourceUpdater.checkIn(bookNum);
 						}
 						fReferenceMaterialText.setText(newValue);
 					} catch (ResourceHandlingException e1) {
 						UIUtils.displayResourceErrorDialog(e1);
 					} catch (OutOfSyncException e1) {
 						UIUtils.displaySyncErrorDialog(e1);
 					}
 				}
 			}
 		});
 		UIUtils.addTabbedPropertiesTextResizeListener(fReferenceMaterialText);
 
 		return reviewDetailsSection;
 	}
 
 	/**
 	 * Method createDecisionSection.
 	 * 
 	 * @param aWidgetFactory
 	 *            TabbedPropertySheetWidgetFactory
 	 * @param aComposite
 	 *            Composite
 	 * @param aTopComposite
 	 *            Composite
 	 * @return Composite
 	 */
 	private Composite createDecisionSection(TabbedPropertySheetWidgetFactory aWidgetFactory,
 			final Composite aComposite, final Composite aTopComposite) {
 
 		//Decision section
 		fDecisionSection = aWidgetFactory.createExpandableComposite(aComposite, ExpandableComposite.TWISTIE);
 		final FormData data = new FormData();
 		data.left = new FormAttachment(0, 0);
 		data.right = new FormAttachment(100, 0); // $codepro.audit.disable numericLiterals
 		data.top = new FormAttachment(aTopComposite, ITabbedPropertyConstants.VSPACE);
 		fDecisionSection.setLayoutData(data);
 		fDecisionSection.setText(DECISION_SECTION_LABEL);
 		fDecisionSection.addExpansionListener(new ExpansionAdapter() {
 			@Override
 			public void expansionStateChanged(ExpansionEvent e) {
 				final ScrolledComposite scrolledParent = (ScrolledComposite) aComposite.getParent()
 						.getParent()
 						.getParent()
 						.getParent()
 						.getParent();
 				scrolledParent.setMinSize(aComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
 				scrolledParent.layout(true, true);
 			}
 		});
 		fDecisionSection.setLayout(new GridLayout(1, false));
 
 		final Composite decisionSectionClient = aWidgetFactory.createComposite(fDecisionSection);
 		decisionSectionClient.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 		decisionSectionClient.setLayout(new GridLayout(4, false));
 		fDecisionSection.setClient(decisionSectionClient);
 
 		//Scheduled Meetings
 		final CLabel meetingInfoLabel = aWidgetFactory.createCLabel(decisionSectionClient,
 				R4EUIConstants.DECISION_MEETING_LABEL);
 		meetingInfoLabel.setToolTipText(R4EUIConstants.REVIEW_MEETING_TOOLTIP);
 		meetingInfoLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));
 
 		//Meeting composite
 		fMeetingComposite = aWidgetFactory.createComposite(decisionSectionClient, SWT.BORDER);
 		GridData textGridData = new GridData(SWT.FILL, SWT.FILL, true, true);
 		textGridData.horizontalSpan = 3;
 		fMeetingComposite.setLayoutData(textGridData);
 		fMeetingComposite.setLayout(new GridLayout(4, false));
 
 		//Meeting Subject
 		final CLabel meetingSubjectLabel = aWidgetFactory.createCLabel(fMeetingComposite, R4EUIConstants.SUBJECT_LABEL);
 		meetingSubjectLabel.setToolTipText(R4EUIConstants.REVIEW_MEETING_SUBJECT_TOOLTIP);
 		meetingSubjectLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));
 		aWidgetFactory.setBorderStyle(SWT.NULL);
 		fMeetingSubjectLabel = aWidgetFactory.createText(fMeetingComposite, "", SWT.NULL);
 		textGridData = new GridData(SWT.FILL, SWT.CENTER, false, false);
 		textGridData.horizontalSpan = 2;
 		fMeetingSubjectLabel.setEditable(false);
 		fMeetingSubjectLabel.setToolTipText(R4EUIConstants.REVIEW_MEETING_SUBJECT_TOOLTIP);
 		fMeetingSubjectLabel.setLayoutData(textGridData);
 
 		//Meeting update button
 		fMeetingUpdateButton = aWidgetFactory.createButton(fMeetingComposite, R4EUIConstants.CREATE_LABEL, SWT.PUSH);
 		fMeetingUpdateButton.setToolTipText(R4EUIConstants.REVIEW_MEETING_UPDATE_TOOLTIP);
 		fMeetingUpdateButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));
 		fMeetingUpdateButton.addSelectionListener(new SelectionListener() {
 			public void widgetSelected(SelectionEvent e) {
 				try {
 					MailServicesProxy.sendMeetingRequest();
 					refresh();
 					R4EUIModelController.getNavigatorView().getTreeViewer().refresh();
 				} catch (ResourceHandlingException e1) {
 					UIUtils.displayResourceErrorDialog(e1);
 				} catch (OutOfSyncException e1) {
 					UIUtils.displaySyncErrorDialog(e1);
 				}
 			}
 
 			public void widgetDefaultSelected(SelectionEvent e) {
 				//Nothing to do
 			}
 		});
 
 		//Meeting Start Time
 		final CLabel meetingStartTimeLabel = aWidgetFactory.createCLabel(fMeetingComposite,
 				R4EUIConstants.START_TIME_LABEL);
 		meetingStartTimeLabel.setToolTipText(R4EUIConstants.REVIEW_MEETING_TIME_TOOLTIP);
 		meetingStartTimeLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));
 		fMeetingStartTimeLabel = aWidgetFactory.createText(fMeetingComposite, "", SWT.NULL);
 		textGridData = new GridData(SWT.FILL, SWT.CENTER, false, false);
 		textGridData.horizontalSpan = 2;
 		fMeetingStartTimeLabel.setEditable(false);
 		fMeetingStartTimeLabel.setToolTipText(R4EUIConstants.REVIEW_MEETING_TIME_TOOLTIP);
 		fMeetingStartTimeLabel.setLayoutData(textGridData);
 
 		//Meeting refresh button
 		fMeetingRefreshButton = aWidgetFactory.createButton(fMeetingComposite, R4EUIConstants.REFRESH_LABEL, SWT.PUSH);
 		fMeetingRefreshButton.setToolTipText(REVIEW_MEETING_REFRESH_TOOLTIP);
 		fMeetingRefreshButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));
 		fMeetingRefreshButton.addSelectionListener(new SelectionListener() {
 			public void widgetSelected(SelectionEvent e) {
 				try {
 					((R4EUIReviewBasic) fProperties.getElement()).refreshMeetingData();
 					refresh();
 					R4EUIModelController.getNavigatorView().getTreeViewer().refresh();
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
 		final CLabel meetingDurationLabel = aWidgetFactory.createCLabel(fMeetingComposite,
 				R4EUIConstants.DURATION_LABEL);
 		meetingDurationLabel.setToolTipText(R4EUIConstants.REVIEW_MEETING_DURATION_TOOLTIP);
 		meetingDurationLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));
 		fMeetingDurationLabel = aWidgetFactory.createText(fMeetingComposite, "", SWT.NULL);
 		textGridData = new GridData(SWT.FILL, SWT.CENTER, false, false);
 		textGridData.horizontalSpan = 2;
 		fMeetingDurationLabel.setEditable(false);
 		fMeetingDurationLabel.setToolTipText(R4EUIConstants.REVIEW_MEETING_DURATION_TOOLTIP);
 		fMeetingDurationLabel.setLayoutData(textGridData);
 		aWidgetFactory.createCLabel(fMeetingComposite, ""); //dummy label for alignment purposes
 
 		//Meeting Location
 		final CLabel meetingLocationLabel = aWidgetFactory.createCLabel(fMeetingComposite,
 				R4EUIConstants.LOCATION_LABEL);
 		meetingLocationLabel.setToolTipText(R4EUIConstants.REVIEW_MEETING_LOCATION_TOOLTIP);
 		meetingLocationLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));
 		fMeetingLocationLabel = aWidgetFactory.createText(fMeetingComposite, "", SWT.NULL);
 		textGridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
 		textGridData.horizontalSpan = 2;
 		fMeetingLocationLabel.setEditable(false);
 		fMeetingLocationLabel.setToolTipText(R4EUIConstants.REVIEW_MEETING_LOCATION_TOOLTIP);
 		fMeetingLocationLabel.setLayoutData(textGridData);
 		aWidgetFactory.createCLabel(fMeetingComposite, ""); //dummy label for alignment purposes
 
 		//Exit Decision
 		final CLabel exitDecisionLabel = aWidgetFactory.createCLabel(decisionSectionClient,
 				R4EUIConstants.EXIT_DECISION_LABEL);
 		exitDecisionLabel.setToolTipText(R4EUIConstants.REVIEW_EXIT_DECISION_TOOLTIP);
 		exitDecisionLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));
 
 		aWidgetFactory.setBorderStyle(SWT.BORDER);
 		fExitDecisionCombo = aWidgetFactory.createCCombo(decisionSectionClient, SWT.READ_ONLY);
 		textGridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
 		textGridData.horizontalSpan = 3;
 		fExitDecisionCombo.setToolTipText(R4EUIConstants.REVIEW_EXIT_DECISION_TOOLTIP);
 		fExitDecisionCombo.setLayoutData(textGridData);
 		fExitDecisionCombo.addListener(SWT.Selection, new Listener() {
 			public void handleEvent(Event event) {
 				if (!fRefreshInProgress) {
 					try {
 						final String currentUser = R4EUIModelController.getReviewer();
 						final R4EReview modelReview = ((R4EUIReviewBasic) fProperties.getElement()).getReview();
 						R4EReviewDecision newDecision = R4EUIReviewBasic.getDecisionValueFromString(fExitDecisionCombo.getText());
 
 						//375419: NPE Formal Reviews attempting to update the Exit Decision
 						boolean updateDecision = false;
 						if (modelReview.getDecision() == null) {
 							updateDecision = true;
 						} else {
 							R4EDecision oldDecisionValue = modelReview.getDecision().getValue();
 							if (!newDecision.getValue().equals(oldDecisionValue)) {
 								updateDecision = true;
 							}
 						}
 
 						if (updateDecision) {
 							final Long bookNum = R4EUIModelController.FResourceUpdater.checkOut(modelReview,
 									currentUser);
 							modelReview.setDecision(newDecision);
 							R4EUIModelController.FResourceUpdater.checkIn(bookNum);
 						}
 					} catch (ResourceHandlingException e1) {
 						UIUtils.displayResourceErrorDialog(e1);
 					} catch (OutOfSyncException e1) {
 						UIUtils.displaySyncErrorDialog(e1);
 					}
 				}
 				refresh();
 			}
 		});
 		addScrollListener(fExitDecisionCombo);
 
 		//Decision Participants
 		fDecisionUsersListLabel = aWidgetFactory.createCLabel(decisionSectionClient,
 				R4EUIConstants.DECISION_PARTICIPANTS_LABEL);
 		fDecisionUsersListLabel.setToolTipText(R4EUIConstants.REVIEW_EXIT_DECISION_PARTICIPANTS_TOOLTIP);
 		fDecisionUsersListLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));
 
 		List<String> participants = null;
 		if (null != R4EUIModelController.getActiveReview()) {
 			participants = R4EUIModelController.getActiveReview().getParticipantIDs();
 		} else {
 			participants = new ArrayList<String>();
 		}
 		textGridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
 		textGridData.horizontalSpan = 3;
 		fDecisionUsersList = new EditableListWidget(aWidgetFactory, decisionSectionClient, textGridData, this, 2,
 				CCombo.class, participants.toArray(new String[participants.size()]));
 
 		fDecisionUsersList.setToolTipText(R4EUIConstants.REVIEW_EXIT_DECISION_PARTICIPANTS_TOOLTIP);
 
 		//Decision Time Spent
 		fDecisionTimeSpentLabel = aWidgetFactory.createCLabel(decisionSectionClient,
 				R4EUIConstants.DECISION_TIME_SPENT_LABEL);
 		fDecisionTimeSpentLabel.setToolTipText(R4EUIConstants.REVIEW_EXIT_DECISION_TIME_SPENT_TOOLTIP);
 		fDecisionTimeSpentLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));
 		fDecisionTimeSpentText = aWidgetFactory.createText(decisionSectionClient, "");
 		textGridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
 		textGridData.horizontalSpan = 3;
 		fDecisionTimeSpentText.setToolTipText(R4EUIConstants.REVIEW_EXIT_DECISION_TIME_SPENT_TOOLTIP);
 		fDecisionTimeSpentText.setLayoutData(textGridData);
 		fDecisionTimeSpentText.addListener(SWT.FocusOut, new Listener() {
 			public void handleEvent(Event event) {
 				if (!fRefreshInProgress && fDecisionTimeSpentText.getForeground().equals(UIUtils.ENABLED_FONT_COLOR)) {
 					Integer timeSpent;
 					try {
 						timeSpent = Integer.valueOf(fDecisionTimeSpentText.getText().trim());
 					} catch (NumberFormatException e1) {
 						//Set field to 0
 						timeSpent = 0;
 					}
 					try {
 						final String currentUser = R4EUIModelController.getReviewer();
 						final R4EReview modelReview = ((R4EUIReviewExtended) fProperties.getElement()).getReview();
 						if (null == modelReview.getDecision()
 								|| timeSpent.intValue() != modelReview.getDecision().getSpentTime()) {
 							final Long bookNum = R4EUIModelController.FResourceUpdater.checkOut(modelReview,
 									currentUser);
 
 							//Create decision if it does not already exists
 							if (null == modelReview.getDecision()) {
 								final R4EReviewDecision reviewDecision = RModelFactoryExt.eINSTANCE.createR4EReviewDecision();
 								reviewDecision.setValue(R4EDecision.R4E_REVIEW_DECISION_NONE);
 								modelReview.setDecision(reviewDecision);
 							}
 
 							modelReview.getDecision().setSpentTime(timeSpent.intValue());
 							R4EUIModelController.FResourceUpdater.checkIn(bookNum);
 						}
 					} catch (ResourceHandlingException e1) {
 						UIUtils.displayResourceErrorDialog(e1);
 					} catch (OutOfSyncException e1) {
 						UIUtils.displaySyncErrorDialog(e1);
 					}
 				}
 			}
 		});
 		return fDecisionSection;
 	}
 
 	/**
 	 * Method createPhaseTable.
 	 * 
 	 * @param aWidgetFactory
 	 *            TabbedPropertySheetWidgetFactory
 	 * @param aComposite
 	 *            Composite
 	 * @param aTopComposite
 	 *            Composite
 	 */
 	private void createPhaseTable(TabbedPropertySheetWidgetFactory aWidgetFactory, final Composite aComposite,
 			final Composite aTopComposite) {
 		//Review Phase Table (formal reviews only)
 		fPhaseTable = aWidgetFactory.createTable(aComposite, SWT.HIDE_SELECTION);
 		FormData data = new FormData();
 		data.left = new FormAttachment(0, R4EUIConstants.TABBED_PROPERTY_LABEL_WIDTH);
 		data.right = new FormAttachment(100, 0); // $codepro.audit.disable numericLiterals
 		data.top = new FormAttachment(aTopComposite, ITabbedPropertyConstants.VSPACE);
 		fPhaseTable.setHeaderVisible(true);
 		fPhaseTable.setToolTipText(R4EUIConstants.REVIEW_PHASE_TABLE_TOOLTIP);
 		fPhaseTable.setLayoutData(data);
 		fPhaseTable.addSelectionListener(new SelectionListener() {
 			public void widgetSelected(SelectionEvent e) {
 				fPhaseTable.deselectAll();
 			}
 
 			public void widgetDefaultSelected(SelectionEvent e) {
 				//Nothing to do
 			}
 		});
 
 		fColumnPhase = new TableColumn(fPhaseTable, SWT.LEFT, 0);
 		fColumnOwner = new TableColumn(fPhaseTable, SWT.LEFT, 1);
 		fColumnStartDate = new TableColumn(fPhaseTable, SWT.LEFT, 2);
 		fColumnEndDate = new TableColumn(fPhaseTable, SWT.LEFT, 3);
 		fColumnPhase.setText(R4EUIConstants.PHASE_LABEL);
 		fColumnOwner.setText(R4EUIConstants.PHASE_OWNER_LABEL);
 		fColumnStartDate.setText(R4EUIConstants.START_DATE_LABEL);
 		fColumnEndDate.setText(R4EUIConstants.END_DATE_LABEL);
 		fPhasePlanning = new TableItem(fPhaseTable, SWT.NONE);
 		fPhasePreparation = new TableItem(fPhaseTable, SWT.NONE);
 		fPhaseDecision = new TableItem(fPhaseTable, SWT.NONE);
 		fPhaseRework = new TableItem(fPhaseTable, SWT.NONE);
 		fPhasePlanning.setText(0, R4EUIConstants.PHASE_PLANNING_LABEL);
 		fPhasePreparation.setText(0, R4EUIConstants.PHASE_PREPARATION_LABEL);
 		fPhaseDecision.setText(0, R4EUIConstants.PHASE_DECISION_LABEL);
 		fPhaseRework.setText(0, R4EUIConstants.PHASE_REWORK_LABEL);
 
 		fPhaseMapLabel = aWidgetFactory.createCLabel(aComposite, R4EUIConstants.PHASE_MAP_LABEL);
 		data = new FormData();
 		data.left = new FormAttachment(0, 0);
 		data.right = new FormAttachment(fPhaseTable, -ITabbedPropertyConstants.HSPACE);
 		data.top = new FormAttachment(fPhaseTable, 0, SWT.CENTER);
 		fPhaseMapLabel.setToolTipText(R4EUIConstants.REVIEW_PHASE_TABLE_TOOLTIP);
 		fPhaseMapLabel.setLayoutData(data);
 
 		//Add Control for planning phase owner
 		fPlanningPhaseOwnerCombo = new CCombo(fPhaseTable, SWT.BORDER | SWT.READ_ONLY);
 		fPlanningPhaseOwnerCombo.setToolTipText(R4EUIConstants.REVIEW_PHASE_OWNER_TOOLTIP);
 		fPlanningPhaseOwnerCombo.addListener(SWT.FocusOut, new Listener() {
 			public void handleEvent(Event event) {
 				if (!fRefreshInProgress) {
 					try {
 						final String currentUser = R4EUIModelController.getReviewer();
 						final R4EReview modelReview = ((R4EUIReviewExtended) fProperties.getElement()).getReview();
 						if (!fPlanningPhaseOwnerCombo.getText().equals(
 								((R4EFormalReview) modelReview).getCurrent().getPhaseOwnerID())) {
 							final Long bookNum = R4EUIModelController.FResourceUpdater.checkOut(modelReview,
 									currentUser);
 							((R4EFormalReview) modelReview).getCurrent().setPhaseOwnerID(
 									fPlanningPhaseOwnerCombo.getText());
 							R4EUIModelController.FResourceUpdater.checkIn(bookNum);
 						}
 					} catch (ResourceHandlingException e1) {
 						UIUtils.displayResourceErrorDialog(e1);
 					} catch (OutOfSyncException e1) {
 						UIUtils.displaySyncErrorDialog(e1);
 					}
 				}
 				refresh();
 			}
 		});
 		addScrollListener(fPlanningPhaseOwnerCombo);
 		final TableEditor planningEditor = new TableEditor(fPhaseTable);
 		planningEditor.grabHorizontal = true;
 		planningEditor.grabVertical = true;
 		planningEditor.setEditor(fPlanningPhaseOwnerCombo, fPhasePlanning, 1);
 
 		//Add Controls for preparation phase owner
 		fPreparationPhaseOwnerCombo = new CCombo(fPhaseTable, SWT.BORDER | SWT.READ_ONLY);
 		fPreparationPhaseOwnerCombo.setToolTipText(R4EUIConstants.REVIEW_PHASE_OWNER_TOOLTIP);
 		fPreparationPhaseOwnerCombo.addListener(SWT.FocusOut, new Listener() {
 			public void handleEvent(Event event) {
 				if (!fRefreshInProgress) {
 					try {
 						final String currentUser = R4EUIModelController.getReviewer();
 						final R4EReview modelReview = ((R4EUIReviewExtended) fProperties.getElement()).getReview();
 						if (!fPreparationPhaseOwnerCombo.getText().equals(
 								((R4EFormalReview) modelReview).getCurrent().getPhaseOwnerID())) {
 							final Long bookNum = R4EUIModelController.FResourceUpdater.checkOut(modelReview,
 									currentUser);
 							((R4EFormalReview) modelReview).getCurrent().setPhaseOwnerID(
 									fPreparationPhaseOwnerCombo.getText());
 							R4EUIModelController.FResourceUpdater.checkIn(bookNum);
 						}
 					} catch (ResourceHandlingException e1) {
 						UIUtils.displayResourceErrorDialog(e1);
 					} catch (OutOfSyncException e1) {
 						UIUtils.displaySyncErrorDialog(e1);
 					}
 				}
 				refresh();
 			}
 		});
 		addScrollListener(fPreparationPhaseOwnerCombo);
 		final TableEditor preparationEditor = new TableEditor(fPhaseTable);
 		preparationEditor.grabHorizontal = true;
 		preparationEditor.grabVertical = true;
 		preparationEditor.setEditor(fPreparationPhaseOwnerCombo, fPhasePreparation, 1);
 
 		//Add Controls for decision phase owner
 		fDecisionPhaseOwnerCombo = new CCombo(fPhaseTable, SWT.BORDER | SWT.READ_ONLY);
 		fDecisionPhaseOwnerCombo.setToolTipText(R4EUIConstants.REVIEW_PHASE_OWNER_TOOLTIP);
 		fDecisionPhaseOwnerCombo.addListener(SWT.FocusOut, new Listener() {
 			public void handleEvent(Event event) {
 				if (!fRefreshInProgress) {
 					try {
 						final String currentUser = R4EUIModelController.getReviewer();
 						final R4EReview modelReview = ((R4EUIReviewExtended) fProperties.getElement()).getReview();
 						if (!fDecisionPhaseOwnerCombo.getText().equals(
 								((R4EFormalReview) modelReview).getCurrent().getPhaseOwnerID())) {
 							final Long bookNum = R4EUIModelController.FResourceUpdater.checkOut(modelReview,
 									currentUser);
 							((R4EFormalReview) modelReview).getCurrent().setPhaseOwnerID(
 									fDecisionPhaseOwnerCombo.getText());
 							R4EUIModelController.FResourceUpdater.checkIn(bookNum);
 						}
 					} catch (ResourceHandlingException e1) {
 						UIUtils.displayResourceErrorDialog(e1);
 					} catch (OutOfSyncException e1) {
 						UIUtils.displaySyncErrorDialog(e1);
 					}
 				}
 				refresh();
 			}
 		});
 		addScrollListener(fDecisionPhaseOwnerCombo);
 		final TableEditor decisionEditor = new TableEditor(fPhaseTable);
 		decisionEditor.grabHorizontal = true;
 		decisionEditor.grabVertical = true;
 		decisionEditor.setEditor(fDecisionPhaseOwnerCombo, fPhaseDecision, 1);
 
 		//Add Controls for rework phase owner
 		fReworkPhaseOwnerCombo = new CCombo(fPhaseTable, SWT.BORDER | SWT.READ_ONLY);
 		fReworkPhaseOwnerCombo.setToolTipText(R4EUIConstants.REVIEW_PHASE_OWNER_TOOLTIP);
 		fReworkPhaseOwnerCombo.addListener(SWT.FocusOut, new Listener() {
 			public void handleEvent(Event event) {
 				if (!fRefreshInProgress) {
 					try {
 						final String currentUser = R4EUIModelController.getReviewer();
 						final R4EReview modelReview = ((R4EUIReviewExtended) fProperties.getElement()).getReview();
 						if (!fReworkPhaseOwnerCombo.getText().equals(
 								((R4EFormalReview) modelReview).getCurrent().getPhaseOwnerID())) {
 							final Long bookNum = R4EUIModelController.FResourceUpdater.checkOut(modelReview,
 									currentUser);
 							((R4EFormalReview) modelReview).getCurrent().setPhaseOwnerID(
 									fReworkPhaseOwnerCombo.getText());
 							R4EUIModelController.FResourceUpdater.checkIn(bookNum);
 						}
 					} catch (ResourceHandlingException e1) {
 						UIUtils.displayResourceErrorDialog(e1);
 					} catch (OutOfSyncException e1) {
 						UIUtils.displaySyncErrorDialog(e1);
 					}
 				}
 				refresh();
 			}
 		});
 		addScrollListener(fReworkPhaseOwnerCombo);
 		final TableEditor reworkEditor = new TableEditor(fPhaseTable);
 		reworkEditor.grabHorizontal = true;
 		reworkEditor.grabVertical = true;
 		reworkEditor.setEditor(fReworkPhaseOwnerCombo, fPhaseRework, 1);
 
 		fColumnPhase.pack();
 		fColumnOwner.pack();
 		fColumnStartDate.pack();
 		fColumnEndDate.pack();
 	}
 
 	/**
 	 * Method refresh.
 	 * 
 	 * @see org.eclipse.ui.views.properties.tabbed.ISection#refresh()
 	 */
 	@Override
 	public void refresh() {
 		fRefreshInProgress = true;
 		final DateFormat dateFormat = new SimpleDateFormat(R4EUIConstants.DEFAULT_DATE_FORMAT);
 
 		//Refresh common properties first
 		final R4EUIReviewBasic uiReview = (R4EUIReviewBasic) fProperties.getElement();
 		final R4EReview modelReview = uiReview.getReview();
 		fNameText.setText(modelReview.getName());
 		fDescriptionText.setText(modelReview.getExtraNotes());
 
 		fStartDateText.setText(dateFormat.format(modelReview.getStartDate()));
 		if (null == modelReview.getEndDate()) {
 			fEndDateText.setText("(In Progress)");
 		} else {
 			fEndDateText.setText(dateFormat.format(modelReview.getEndDate()));
 		}
 
 		final String[] availableProjects = (String[]) ((R4EUIReviewGroup) uiReview.getParent()).getReviewGroup()
 				.getAvailableProjects()
 				.toArray();
 		fProjectCombo.removeAll();
 		fProjectCombo.add("");
 		for (String project : availableProjects) {
 			fProjectCombo.add(project);
 		}
 		final String project = modelReview.getProject();
 		if (null != project) {
 			fProjectCombo.setText(project);
 		}
 
 		fComponents.setEditableValues((String[]) ((R4EUIReviewGroup) uiReview.getParent()).getReviewGroup()
 				.getAvailableComponents()
 				.toArray());
 		final String[] components = (String[]) modelReview.getComponents().toArray();
 		fComponents.removeAll();
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
 
 		if (null != modelReview.getEntryCriteria()) {
 			fEntryCriteriaText.setText(modelReview.getEntryCriteria());
 		}
 		if (null != modelReview.getObjectives()) {
 			fObjectivesText.setText(modelReview.getObjectives());
 		}
 		if (null != modelReview.getReferenceMaterial()) {
 			fReferenceMaterialText.setText(modelReview.getReferenceMaterial());
 		}
 
 		final R4EMeetingData meetingData = modelReview.getActiveMeeting();
		if (null != meetingData && null != meetingData.eResource()) {
 			fMeetingUpdateButton.setText(R4EUIConstants.UPDATE_LABEL);
 			fMeetingSubjectLabel.setText(meetingData.getSubject());
 			final DateFormat meetingDateFormat = new SimpleDateFormat(R4EUIConstants.SIMPLE_DATE_FORMAT_MINUTES);
 			meetingDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
 			fMeetingStartTimeLabel.setText(meetingDateFormat.format(new Date(meetingData.getStartTime())));
 			fMeetingDurationLabel.setText(Integer.toString(meetingData.getDuration()));
 			fMeetingLocationLabel.setText(null != meetingData.getLocation() ? meetingData.getLocation() : "");
 		} else {
 			fMeetingUpdateButton.setText(R4EUIConstants.CREATE_LABEL);
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
 
 		//Formal Review Properties next
 		if (fProperties.getElement() instanceof R4EUIReviewExtended) {
 			final R4EUIReviewExtended uiExtendedReview = (R4EUIReviewExtended) fProperties.getElement();
 			final R4EFormalReview modelFormalReview = (R4EFormalReview) uiReview.getReview();
 
 			final List<R4EParticipant> participants = uiExtendedReview.getParticipants();
 			item = null;
 			final int numParticipants = participants.size();
 			fDecisionUsersList.removeAll();
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
 			if (null != modelFormalReview.getDecision()) {
 				fDecisionTimeSpentText.setText(Integer.valueOf(modelFormalReview.getDecision().getSpentTime())
 						.toString());
 			} else {
 				fDecisionTimeSpentText.setText("");
 			}
 
 			fPhaseCombo.setItems(uiExtendedReview.getAvailablePhases());
 			fPhaseCombo.select(uiExtendedReview.mapPhaseToIndex(((R4EReviewState) modelReview.getState()).getState()));
 
 			final int columnWidth = fPhaseTable.getClientArea().width / fPhaseTable.getColumnCount();
 			fColumnPhase.setWidth(columnWidth);
 			fColumnOwner.setWidth(columnWidth);
 			fColumnStartDate.setWidth(columnWidth);
 			fColumnEndDate.setWidth(columnWidth);
 
 			final List<String> participantsList = new ArrayList<String>();
 			for (R4EParticipant participant : participants) {
 				participantsList.add(participant.getId());
 			}
 			final String[] participantsStr = participantsList.toArray(new String[participantsList.size()]);
 
 			R4EReviewPhaseInfo phaseInfo = uiExtendedReview.getPhaseInfo(R4EReviewPhase.R4E_REVIEW_PHASE_STARTED);
 			final R4EReviewPhaseInfo currentPhaseInfo = modelFormalReview.getCurrent();
 			if (null != phaseInfo && null != phaseInfo.getPhaseOwnerID()) {
 				fPhasePlanning.setText(1, phaseInfo.getPhaseOwnerID());
 				fPhasePlanning.setText(2,
 						(null != phaseInfo.getStartDate()) ? dateFormat.format(phaseInfo.getStartDate()) : "");
 				fPhasePlanning.setText(3, (null != phaseInfo.getEndDate())
 						? dateFormat.format(phaseInfo.getEndDate())
 						: "");
 				if (currentPhaseInfo.getType().equals(phaseInfo.getType())) {
 					fPhasePlanning.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_CYAN));
 					fPhasePreparation.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
 					fPhaseDecision.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
 					fPhaseRework.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
 					fPlanningPhaseOwnerCombo.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_CYAN));
 					fPreparationPhaseOwnerCombo.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
 					fDecisionPhaseOwnerCombo.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
 					fReworkPhaseOwnerCombo.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
 					fPlanningPhaseOwnerCombo.setItems(participantsStr);
 					fPlanningPhaseOwnerCombo.select(UIUtils.mapParticipantToIndex(modelFormalReview.getCurrent()
 							.getPhaseOwnerID()));
 				}
 			} else {
 				fPhasePlanning.setText(1, "");
 				fPhasePlanning.setText(2, "");
 				fPhasePlanning.setText(3, "");
 			}
 
 			phaseInfo = uiExtendedReview.getPhaseInfo(R4EReviewPhase.R4E_REVIEW_PHASE_PREPARATION);
 			if (null != phaseInfo && null != phaseInfo.getPhaseOwnerID()) {
 				fPhasePreparation.setText(1, phaseInfo.getPhaseOwnerID());
 				fPhasePreparation.setText(2,
 						(null != phaseInfo.getStartDate()) ? dateFormat.format(phaseInfo.getStartDate()) : "");
 				fPhasePreparation.setText(3,
 						(null != phaseInfo.getEndDate()) ? dateFormat.format(phaseInfo.getEndDate()) : "");
 				if (currentPhaseInfo.getType().equals(phaseInfo.getType())) {
 					fPhasePlanning.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_GREEN));
 					fPhasePreparation.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_CYAN));
 					fPhaseDecision.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
 					fPhaseRework.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
 					fPlanningPhaseOwnerCombo.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_GREEN));
 					fPreparationPhaseOwnerCombo.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_CYAN));
 					fDecisionPhaseOwnerCombo.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
 					fReworkPhaseOwnerCombo.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
 					fPreparationPhaseOwnerCombo.setItems(participantsStr);
 					fPreparationPhaseOwnerCombo.select(UIUtils.mapParticipantToIndex(modelFormalReview.getCurrent()
 							.getPhaseOwnerID()));
 				}
 			} else {
 				fPhasePreparation.setText(1, "");
 				fPhasePreparation.setText(2, "");
 				fPhasePreparation.setText(3, "");
 			}
 
 			phaseInfo = uiExtendedReview.getPhaseInfo(R4EReviewPhase.R4E_REVIEW_PHASE_DECISION);
 			if (null != phaseInfo && null != phaseInfo.getPhaseOwnerID()) {
 				fPhaseDecision.setText(1, phaseInfo.getPhaseOwnerID());
 				fPhaseDecision.setText(2,
 						(null != phaseInfo.getStartDate()) ? dateFormat.format(phaseInfo.getStartDate()) : "");
 				fPhaseDecision.setText(3, (null != phaseInfo.getEndDate())
 						? dateFormat.format(phaseInfo.getEndDate())
 						: "");
 				if (currentPhaseInfo.getType().equals(phaseInfo.getType())) {
 					fPhasePlanning.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_GREEN));
 					fPhasePreparation.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_GREEN));
 					fPhaseDecision.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_CYAN));
 					fPhaseRework.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
 					fPlanningPhaseOwnerCombo.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_GREEN));
 					fPreparationPhaseOwnerCombo.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_GREEN));
 					fDecisionPhaseOwnerCombo.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_CYAN));
 					fReworkPhaseOwnerCombo.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
 					fDecisionPhaseOwnerCombo.setItems(participantsStr);
 					fDecisionPhaseOwnerCombo.select(UIUtils.mapParticipantToIndex(modelFormalReview.getCurrent()
 							.getPhaseOwnerID()));
 				}
 			} else {
 				fPhaseDecision.setText(1, "");
 				fPhaseDecision.setText(2, "");
 				fPhaseDecision.setText(3, "");
 			}
 
 			phaseInfo = uiExtendedReview.getPhaseInfo(R4EReviewPhase.R4E_REVIEW_PHASE_REWORK);
 			if (null != phaseInfo && null != phaseInfo.getPhaseOwnerID()) {
 				fPhaseRework.setText(1, phaseInfo.getPhaseOwnerID());
 				fPhaseRework.setText(2,
 						(null != phaseInfo.getStartDate()) ? dateFormat.format(phaseInfo.getStartDate()) : "");
 				fPhaseRework.setText(3, (null != phaseInfo.getEndDate())
 						? dateFormat.format(phaseInfo.getEndDate())
 						: "");
 				if (currentPhaseInfo.getType().equals(phaseInfo.getType())) {
 					fPhasePlanning.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_GREEN));
 					fPhasePreparation.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_GREEN));
 					fPhaseDecision.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_GREEN));
 					fPhaseRework.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_CYAN));
 					fPlanningPhaseOwnerCombo.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_GREEN));
 					fPreparationPhaseOwnerCombo.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_GREEN));
 					fDecisionPhaseOwnerCombo.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_GREEN));
 					fReworkPhaseOwnerCombo.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_CYAN));
 					fReworkPhaseOwnerCombo.setItems(participantsStr);
 					fReworkPhaseOwnerCombo.select(UIUtils.mapParticipantToIndex(modelFormalReview.getCurrent()
 							.getPhaseOwnerID()));
 				}
 			} else {
 				fPhaseRework.setText(1, "");
 				fPhaseRework.setText(2, "");
 				fPhaseRework.setText(3, "");
 			}
 
 			phaseInfo = uiExtendedReview.getPhaseInfo(R4EReviewPhase.R4E_REVIEW_PHASE_COMPLETED);
 			if (null != phaseInfo && null != phaseInfo.getType()) {
 				if (currentPhaseInfo.getType().equals(phaseInfo.getType())) {
 					fPhasePlanning.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_GREEN));
 					fPhasePreparation.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_GREEN));
 					fPhaseDecision.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_GREEN));
 					fPhaseRework.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_GREEN));
 					fPlanningPhaseOwnerCombo.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_GREEN));
 					fPreparationPhaseOwnerCombo.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_GREEN));
 					fDecisionPhaseOwnerCombo.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_GREEN));
 					fReworkPhaseOwnerCombo.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_GREEN));
 				}
 			}
 		} else {
 			fPhaseCombo.setItems(uiReview.getAvailablePhases());
 			fPhaseCombo.select(uiReview.mapPhaseToIndex(((R4EReviewState) modelReview.getState()).getState()));
 		}
 
 		setEnabledFields();
 		fRefreshInProgress = false;
 		R4EUIModelController.getNavigatorView().getTreeViewer().refresh();
 	}
 
 	/**
 	 * Method setEnabledFields.
 	 */
 	@Override
 	protected void setEnabledFields() {
 		if (R4EUIModelController.isJobInProgress()
 				|| fProperties.getElement().isReadOnly()
 				|| (!((R4EUIReviewBasic) fProperties.getElement()).isOpen())
 				|| ((R4EReviewState) ((R4EUIReviewBasic) fProperties.getElement()).getReview().getState()).getState()
 						.equals(R4EReviewPhase.R4E_REVIEW_PHASE_COMPLETED) || !fProperties.getElement().isEnabled()) {
 			fNameText.setForeground(UIUtils.DISABLED_FONT_COLOR);
 			fPhaseCombo.setEnabled(false);
 			fDescriptionText.setForeground(UIUtils.DISABLED_FONT_COLOR);
 			fDescriptionText.setEditable(false);
 			fStartDateText.setForeground(UIUtils.DISABLED_FONT_COLOR);
 			fEndDateText.setForeground(UIUtils.DISABLED_FONT_COLOR);
 			fProjectCombo.setEnabled(false);
 			fComponents.setEnabled(false);
 			fEntryCriteriaText.setForeground(UIUtils.DISABLED_FONT_COLOR);
 			fEntryCriteriaText.setEditable(false);
 			fObjectivesText.setForeground(UIUtils.DISABLED_FONT_COLOR);
 			fObjectivesText.setEditable(false);
 			fReferenceMaterialText.setForeground(UIUtils.DISABLED_FONT_COLOR);
 			fReferenceMaterialText.setEditable(false);
 			fExitDecisionCombo.setEnabled(false);
 			fMeetingUpdateButton.setEnabled(false);
 			fMeetingRefreshButton.setEnabled(false);
 			fMeetingSubjectLabel.setForeground(UIUtils.DISABLED_FONT_COLOR);
 			fMeetingStartTimeLabel.setForeground(UIUtils.DISABLED_FONT_COLOR);
 			fMeetingDurationLabel.setForeground(UIUtils.DISABLED_FONT_COLOR);
 			fMeetingLocationLabel.setForeground(UIUtils.DISABLED_FONT_COLOR);
 
 			if (fProperties.getElement() instanceof R4EUIReviewExtended) {
 				fPhaseTable.setEnabled(false);
 				fPlanningPhaseOwnerCombo.setEnabled(false);
 				fPreparationPhaseOwnerCombo.setEnabled(false);
 				fDecisionPhaseOwnerCombo.setEnabled(false);
 				fReworkPhaseOwnerCombo.setEnabled(false);
 				fPlanningPhaseOwnerCombo.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_GRAY));
 				fPreparationPhaseOwnerCombo.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_GRAY));
 				fDecisionPhaseOwnerCombo.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_GRAY));
 				fReworkPhaseOwnerCombo.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_GRAY));
 				fPhaseTable.setVisible(true);
 				fPhaseMapLabel.setVisible(true);
 				fDecisionSection.setVisible(true);
 				fDecisionUsersList.setEnabled(false);
 				fDecisionTimeSpentText.setForeground(UIUtils.DISABLED_FONT_COLOR);
 				fDecisionTimeSpentText.setEditable(false);
 				fDecisionUsersListLabel.setVisible(true);
 				fDecisionUsersList.setVisible(true);
 				fDecisionTimeSpentText.setVisible(true);
 				fDecisionTimeSpentLabel.setVisible(true);
 				setPhaseControlVisibility();
 			} else {
 				fPhaseTable.setVisible(false);
 				fPhaseMapLabel.setVisible(false);
 				fPlanningPhaseOwnerCombo.setVisible(false);
 				fPreparationPhaseOwnerCombo.setVisible(false);
 				fDecisionPhaseOwnerCombo.setVisible(false);
 				fReworkPhaseOwnerCombo.setVisible(false);
 				fDecisionUsersListLabel.setVisible(false);
 				fDecisionUsersList.setVisible(false);
 				fDecisionTimeSpentText.setVisible(false);
 				fDecisionTimeSpentLabel.setVisible(false);
 				if (((R4EUIReviewBasic) fProperties.getElement()).getReview()
 						.getType()
 						.equals(R4EReviewType.R4E_REVIEW_TYPE_BASIC)) {
 					fDecisionSection.setVisible(false);
 				} else {
 					fDecisionSection.setVisible(true);
 				}
 			}
 		} else {
 			fNameText.setForeground(UIUtils.ENABLED_FONT_COLOR);
 			fPhaseCombo.setEnabled(true);
 			fStartDateText.setForeground(UIUtils.ENABLED_FONT_COLOR);
 			fEndDateText.setForeground(UIUtils.ENABLED_FONT_COLOR);
 			fDescriptionText.setForeground(UIUtils.ENABLED_FONT_COLOR);
 			fDescriptionText.setEditable(true);
 			fProjectCombo.setEnabled(true);
 			fComponents.setEnabled(true);
 			fEntryCriteriaText.setForeground(UIUtils.ENABLED_FONT_COLOR);
 			fEntryCriteriaText.setEditable(true);
 			fObjectivesText.setForeground(UIUtils.ENABLED_FONT_COLOR);
 			fObjectivesText.setEditable(true);
 			fReferenceMaterialText.setForeground(UIUtils.ENABLED_FONT_COLOR);
 			fReferenceMaterialText.setEditable(true);
 			fMeetingUpdateButton.setEnabled(true);
 			fMeetingRefreshButton.setEnabled(true);
 			fMeetingSubjectLabel.setForeground(UIUtils.ENABLED_FONT_COLOR);
 			fMeetingStartTimeLabel.setForeground(UIUtils.ENABLED_FONT_COLOR);
 			fMeetingDurationLabel.setForeground(UIUtils.ENABLED_FONT_COLOR);
 			fMeetingLocationLabel.setForeground(UIUtils.ENABLED_FONT_COLOR);
 
 			if (fProperties.getElement() instanceof R4EUIReviewExtended) {
 
 				final R4EUIReviewExtended uiReview = (R4EUIReviewExtended) fProperties.getElement();
 
 				if (uiReview.isDecisionDateEnabled()) {
 					fDecisionUsersList.setEnabled(true);
 					fDecisionTimeSpentText.setForeground(UIUtils.ENABLED_FONT_COLOR);
 					fDecisionTimeSpentText.setEditable(true);
 					fExitDecisionCombo.setEnabled(true);
 
 				} else {
 					fDecisionUsersList.setEnabled(false);
 					fDecisionTimeSpentText.setForeground(UIUtils.DISABLED_FONT_COLOR);
 					fDecisionTimeSpentText.setEditable(false);
 					fExitDecisionCombo.setEnabled(false);
 				}
 				fDecisionUsersListLabel.setVisible(true);
 				fDecisionUsersList.setVisible(true);
 				fDecisionTimeSpentText.setVisible(true);
 				fDecisionTimeSpentLabel.setVisible(true);
 				fPhaseTable.setEnabled(true);
 				fPlanningPhaseOwnerCombo.setEnabled(true);
 				fPreparationPhaseOwnerCombo.setEnabled(true);
 				fDecisionPhaseOwnerCombo.setEnabled(true);
 				fReworkPhaseOwnerCombo.setEnabled(true);
 				fPhaseTable.setVisible(true);
 				fPhaseMapLabel.setVisible(true);
 				setPhaseControlVisibility();
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
 				fPhaseTable.setVisible(false);
 				fPhaseMapLabel.setVisible(false);
 				fPlanningPhaseOwnerCombo.setVisible(false);
 				fPreparationPhaseOwnerCombo.setVisible(false);
 				fDecisionPhaseOwnerCombo.setVisible(false);
 				fReworkPhaseOwnerCombo.setVisible(false);
 				if (((R4EUIReviewBasic) fProperties.getElement()).getReview()
 						.getType()
 						.equals(R4EReviewType.R4E_REVIEW_TYPE_BASIC)) {
 					fDecisionSection.setVisible(false);
 				} else {
 					fDecisionSection.setVisible(true);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Method setPhaseControlVisibility.
 	 */
 	private void setPhaseControlVisibility() {
 		final R4EReviewPhase currentPhase = ((R4EFormalReview) ((R4EUIReviewExtended) fProperties.getElement()).getReview()).getCurrent()
 				.getType();
 		switch (currentPhase.getValue()) {
 		case R4EReviewPhase.R4E_REVIEW_PHASE_STARTED_VALUE:
 			fPlanningPhaseOwnerCombo.setVisible(true);
 			fPreparationPhaseOwnerCombo.setVisible(false);
 			fDecisionPhaseOwnerCombo.setVisible(false);
 			fReworkPhaseOwnerCombo.setVisible(false);
 			break;
 
 		case R4EReviewPhase.R4E_REVIEW_PHASE_PREPARATION_VALUE:
 			fPreparationPhaseOwnerCombo.setVisible(true);
 			fPlanningPhaseOwnerCombo.setVisible(false);
 			fDecisionPhaseOwnerCombo.setVisible(false);
 			fReworkPhaseOwnerCombo.setVisible(false);
 			break;
 
 		case R4EReviewPhase.R4E_REVIEW_PHASE_DECISION_VALUE:
 			fDecisionPhaseOwnerCombo.setVisible(true);
 			fPlanningPhaseOwnerCombo.setVisible(false);
 			fPreparationPhaseOwnerCombo.setVisible(false);
 			fReworkPhaseOwnerCombo.setVisible(false);
 			break;
 
 		case R4EReviewPhase.R4E_REVIEW_PHASE_REWORK_VALUE:
 			fReworkPhaseOwnerCombo.setVisible(true);
 			fPlanningPhaseOwnerCombo.setVisible(false);
 			fPreparationPhaseOwnerCombo.setVisible(false);
 			fDecisionPhaseOwnerCombo.setVisible(false);
 			break;
 
 		case R4EReviewPhase.R4E_REVIEW_PHASE_COMPLETED_VALUE:
 			fPlanningPhaseOwnerCombo.setVisible(false);
 			fPreparationPhaseOwnerCombo.setVisible(false);
 			fDecisionPhaseOwnerCombo.setVisible(false);
 			fReworkPhaseOwnerCombo.setVisible(false);
 			break;
 
 		default:
 			//should never happen
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
 
 			if (1 == aInstanceId) {
 				//Update components
 				List<String> newAddComponents = new ArrayList<String>();
 				List<String> newDeleteComponents = new ArrayList<String>();
 				List<String> storedComponents = new ArrayList<String>();
 				storedComponents.addAll(modelReview.getComponents());
 				for (Item item : aItems) {
 					newAddComponents.add(item.getText());
 					newDeleteComponents.add(item.getText());
 				}
 
 				//Add all new elements
 				newAddComponents.removeAll(storedComponents);
 				if (newAddComponents.size() > 0) {
 					final Long bookNum = R4EUIModelController.FResourceUpdater.checkOut(modelReview, currentUser);
 					modelReview.getComponents().addAll(newAddComponents);
 					R4EUIModelController.FResourceUpdater.checkIn(bookNum);
 				}
 
 				//Delete old elements to remove
 				storedComponents.removeAll(newDeleteComponents);
 				if (storedComponents.size() > 0) {
 					final Long bookNum = R4EUIModelController.FResourceUpdater.checkOut(modelReview, currentUser);
 					modelReview.getComponents().removeAll(storedComponents);
 					R4EUIModelController.FResourceUpdater.checkIn(bookNum);
 				}
 			} else { //aInstanceId == 2
 				//Update components
 				List<String> newAddParticipants = new ArrayList<String>();
 				List<String> newDeleteParticipants = new ArrayList<String>();
 				List<String> storedParticipants = new ArrayList<String>();
 				Collection<R4EUser> storedUsers = modelReview.getUsersMap().values();
 				for (R4EUser storedUser : storedUsers) {
 					if (((R4EParticipant) storedUser).isIsPartOfDecision()) {
 						storedParticipants.add(storedUser.getId());
 					}
 				}
 				for (Item item : aItems) {
 					newAddParticipants.add(item.getText());
 					newDeleteParticipants.add(item.getText());
 				}
 
 				//Add all new elements
 				newAddParticipants.removeAll(storedParticipants);
 				if (newAddParticipants.size() > 0) {
 					final Long bookNum = R4EUIModelController.FResourceUpdater.checkOut(modelReview, currentUser);
 					for (String participant : newAddParticipants) {
 						((R4EParticipant) modelReview.getUsersMap().get(participant)).setIsPartOfDecision(true);
 					}
 					R4EUIModelController.FResourceUpdater.checkIn(bookNum);
 				}
 
 				//Delete old elements to remove
 				storedParticipants.removeAll(newDeleteParticipants);
 				if (storedParticipants.size() > 0) {
 					final Long bookNum = R4EUIModelController.FResourceUpdater.checkOut(modelReview, currentUser);
 					for (String participant : storedParticipants) {
 						((R4EParticipant) modelReview.getUsersMap().get(participant)).setIsPartOfDecision(false);
 					}
 					R4EUIModelController.FResourceUpdater.checkIn(bookNum);
 				}
 			}
 			refresh();
 		} catch (ResourceHandlingException e1) {
 			UIUtils.displayResourceErrorDialog(e1);
 		} catch (OutOfSyncException e1) {
 			UIUtils.displaySyncErrorDialog(e1);
 		}
 	}
 
 	/**
 	 * Method itemSelected.
 	 * 
 	 * @param aItem
 	 *            Item
 	 * @param aInstanceId
 	 *            int
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.utils.IEditableListListener#itemSelected(Item, int)
 	 */
 	public void itemSelected(Item aItem, int aInstanceId) {
 		// ignore
 
 	}
 }
