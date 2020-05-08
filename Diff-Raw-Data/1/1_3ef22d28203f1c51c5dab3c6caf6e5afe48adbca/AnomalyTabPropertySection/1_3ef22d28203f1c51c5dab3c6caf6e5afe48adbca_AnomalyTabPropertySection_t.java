 // $codepro.audit.disable com.instantiations.assist.eclipse.analysis.audit.rule.effectivejava.alwaysOverridetoString.alwaysOverrideToString, staticFieldSecurity, com.instantiations.assist.eclipse.analysis.deserializeabilitySecurity, com.instantiations.assist.eclipse.analysis.enforceCloneableUsageSecurity, com.instantiations.assist.eclipse.analysis.instanceFieldSecurity
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
  * This class implements the tabbed property section for the Anomaly model element
  * 
  * Contributors:
  *   Sebastien Dubois - Created for Mylyn Review R4E project
  *   
  ******************************************************************************/
 
 package org.eclipse.mylyn.reviews.r4e.ui.internal.properties.tabbed;
 
 import java.text.SimpleDateFormat;
 import java.util.List;
 
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jface.dialogs.ErrorDialog;
 import org.eclipse.jface.window.Window;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EAnomaly;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EAnomalyState;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4ECommentType;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EReviewPhase;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EReviewState;
 import org.eclipse.mylyn.reviews.r4e.core.model.RModelFactory;
 import org.eclipse.mylyn.reviews.r4e.core.model.drules.R4EDesignRule;
 import org.eclipse.mylyn.reviews.r4e.core.model.drules.R4EDesignRuleClass;
 import org.eclipse.mylyn.reviews.r4e.core.model.drules.R4EDesignRuleRank;
 import org.eclipse.mylyn.reviews.r4e.core.model.serial.Persistence.RModelFactoryExt;
 import org.eclipse.mylyn.reviews.r4e.core.model.serial.impl.OutOfSyncException;
 import org.eclipse.mylyn.reviews.r4e.core.model.serial.impl.ResourceHandlingException;
 import org.eclipse.mylyn.reviews.r4e.ui.R4EUIPlugin;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.dialogs.IAnomalyInputDialog;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.dialogs.ICalendarDialog;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.dialogs.R4EUIDialogFactory;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIPosition;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIAnomalyBasic;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIAnomalyExtended;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIFileContext;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIModelController;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIPostponedAnomaly;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIReviewBasic;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.utils.R4EUIConstants;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.utils.UIUtils;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.CCombo;
 import org.eclipse.swt.custom.CLabel;
 import org.eclipse.swt.custom.ScrolledComposite;
 import org.eclipse.swt.layout.FormAttachment;
 import org.eclipse.swt.layout.FormData;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.forms.events.ExpansionAdapter;
 import org.eclipse.ui.forms.events.ExpansionEvent;
 import org.eclipse.ui.forms.widgets.ExpandableComposite;
 import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
 import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
 import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetWidgetFactory;
 
 /**
  * @author Sebastien Dubois
  * @version $Revision: 1.0 $
  */
 public class AnomalyTabPropertySection extends ModelElementTabPropertySection {
 
 	// ------------------------------------------------------------------------
 	// Constants
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Field ANOMALY_DETAILS_SECTION_LABEL. (value is ""Anomaly Details"")
 	 */
 	private static final String ANOMALY_DETAILS_SECTION_LABEL = "Anomaly Details";
 
 	/**
 	 * Field PARTICIPANT_DETAILS_SECTION_LABEL. (value is ""Participants Details"")
 	 */
 	private static final String PARTICIPANT_DETAILS_SECTION_LABEL = "Participants Details";
 
 	// ------------------------------------------------------------------------
 	// Member variables
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Field fPosition.
 	 */
 	private IR4EUIPosition fPosition;
 
 	/**
 	 * Field fTitleText.
 	 */
 	protected Text fTitleText = null;
 
 	/**
 	 * Field fAuthorText.
 	 */
 	protected Text fAuthorText = null;
 
 	/**
 	 * Field fCreationDateText.
 	 */
 	protected Text fCreationDateText = null;
 
 	/**
 	 * Field fPositionText.
 	 */
 	protected Text fPositionText = null;
 
 	/**
 	 * Field fDescriptionText.
 	 */
 	protected Text fDescriptionText = null;
 
 	/**
 	 * Field fStateLabel.
 	 */
 	private CLabel fStateLabel = null;
 
 	/**
 	 * Field fStateCombo.
 	 */
 	protected CCombo fStateCombo = null;
 
 	/**
 	 * Field fClassCombo.
 	 */
 	protected CCombo fClassCombo = null;
 
 	/**
 	 * Field fRankCombo.
 	 */
 	protected CCombo fRankCombo = null;
 
 	/**
 	 * Field fRuleId.
 	 */
 	protected Text fRuleId = null;
 
 	/**
 	 * Field fRuleButton.
 	 */
 	protected Button fRuleButton = null;
 
 	/**
 	 * Field fDateText.
 	 */
 	protected Text fDateText = null;
 
 	/**
 	 * Field fCalendarButton.
 	 */
 	protected Button fCalendarButton = null;
 
 	/**
 	 * Field fDecidedByLabel.
 	 */
 	private CLabel fDecidedByLabel = null;
 
 	/**
 	 * Field fDecidedByCombo.
 	 */
 	protected CCombo fDecidedByCombo = null;
 
 	/**
 	 * Field fFixedByLabel.
 	 */
 	private CLabel fFixedByLabel = null;
 
 	/**
 	 * Field fFixedByCombo.
 	 */
 	protected CCombo fFixedByCombo = null;
 
 	/**
 	 * Field fFollowUpByLabel.
 	 */
 	private CLabel fFollowUpByLabel = null;
 
 	/**
 	 * Field fFollowUpByCombo.
 	 */
 	protected CCombo fFollowUpByCombo = null;
 
 	/**
 	 * Field fAssignedToCombo.
 	 */
 	private CCombo fAssignedToCombo;
 
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
 		final Composite composite = widgetFactory.createFlatFormComposite(parent);
 		FormData data = null;
 
 		//Anomaly title
 		widgetFactory.setBorderStyle(SWT.BORDER);
 		fTitleText = widgetFactory.createText(composite, "", SWT.MULTI);
 		data = new FormData();
 		data.left = new FormAttachment(0, R4EUIConstants.TABBED_PROPERTY_LABEL_WIDTH);
 		data.right = new FormAttachment(100, 0); // $codepro.audit.disable numericLiterals
 		data.top = new FormAttachment(0, ITabbedPropertyConstants.VSPACE);
 		fTitleText.setToolTipText(R4EUIConstants.ANOMALY_TITLE_TOOLTIP);
 		fTitleText.setLayoutData(data);
 		fTitleText.addListener(SWT.FocusOut, new Listener() {
 			public void handleEvent(Event event) {
 				if (!fRefreshInProgress) {
 					try {
 						//Set new model data
 						final String currentUser = R4EUIModelController.getReviewer();
 						final R4EAnomaly modelAnomaly = ((R4EUIAnomalyBasic) fProperties.getElement()).getAnomaly();
 						String newValue = fTitleText.getText().trim();
 						if (!newValue.equals(modelAnomaly.getTitle())) {
 							final Long bookNum = R4EUIModelController.FResourceUpdater.checkOut(modelAnomaly,
 									currentUser);
 							modelAnomaly.setTitle(newValue);
 							R4EUIModelController.FResourceUpdater.checkIn(bookNum);
 
 							//Set new UI display
 							if (fProperties.getElement() instanceof R4EUIAnomalyExtended) {
 								fProperties.getElement().setName(
 										R4EUIAnomalyExtended.buildAnomalyExtName(modelAnomaly,
 												((R4EUIAnomalyExtended) fProperties.getElement()).getPosition()));
 							} else {
 								fProperties.getElement().setName(
 										R4EUIAnomalyBasic.buildAnomalyName(modelAnomaly,
 												((R4EUIAnomalyBasic) fProperties.getElement()).getPosition()));
 							}
 						}
 						fTitleText.setText(newValue);
 					} catch (ResourceHandlingException e1) {
 						UIUtils.displayResourceErrorDialog(e1);
 					} catch (OutOfSyncException e1) {
 						UIUtils.displaySyncErrorDialog(e1);
 					}
 				}
 				R4EUIModelController.getNavigatorView().getTreeViewer().refresh();
 				refresh();
 			}
 		});
 		UIUtils.addTabbedPropertiesTextResizeListener(fTitleText);
 
 		final CLabel titleLabel = widgetFactory.createCLabel(composite, R4EUIConstants.TITLE_LABEL);
 		data = new FormData();
 		data.left = new FormAttachment(0, 0);
 		data.right = new FormAttachment(fTitleText, -ITabbedPropertyConstants.HSPACE);
 		data.top = new FormAttachment(fTitleText, 0, SWT.CENTER);
 		titleLabel.setToolTipText(R4EUIConstants.ANOMALY_TITLE_TOOLTIP);
 		titleLabel.setLayoutData(data);
 
 		//Anomaly Description (Read-Only)
 		widgetFactory.setBorderStyle(SWT.NULL);
 		fDescriptionText = widgetFactory.createText(composite, "", SWT.MULTI);
 		data = new FormData();
 		data.left = new FormAttachment(0, R4EUIConstants.TABBED_PROPERTY_LABEL_WIDTH);
 		data.right = new FormAttachment(100, 0); // $codepro.audit.disable numericLiterals
 		data.top = new FormAttachment(fTitleText, ITabbedPropertyConstants.VSPACE);
 		fDescriptionText.setEditable(false);
 		fDescriptionText.setLayoutData(data);
 		fDescriptionText.setToolTipText(R4EUIConstants.ANOMALY_DESCRIPTION_TOOLTIP);
 
 		final CLabel descriptionLabel = widgetFactory.createCLabel(composite, R4EUIConstants.DESCRIPTION_LABEL);
 		data = new FormData();
 		data.left = new FormAttachment(0, 0);
 		data.right = new FormAttachment(fDescriptionText, -ITabbedPropertyConstants.HSPACE);
 		data.top = new FormAttachment(fDescriptionText, 0, SWT.CENTER);
 		descriptionLabel.setToolTipText(R4EUIConstants.ANOMALY_DESCRIPTION_TOOLTIP);
 		descriptionLabel.setLayoutData(data);
 
 		//State
 		widgetFactory.setBorderStyle(SWT.BORDER);
 		fStateCombo = widgetFactory.createCCombo(composite, SWT.READ_ONLY);
 		data = new FormData();
 		data.left = new FormAttachment(0, R4EUIConstants.TABBED_PROPERTY_LABEL_WIDTH);
 		data.right = new FormAttachment(100, 0); // $codepro.audit.disable numericLiterals
 		data.top = new FormAttachment(fDescriptionText, ITabbedPropertyConstants.VSPACE);
 		fStateCombo.setToolTipText(R4EUIConstants.ANOMALY_STATE_TOOLTIP);
 		fStateCombo.setVisibleItemCount(6);
 		fStateCombo.setLayoutData(data);
 		fStateCombo.addListener(SWT.Selection, new Listener() {
 			public void handleEvent(Event event) {
 				if (!fRefreshInProgress) {
 					final R4EAnomalyState newState = R4EUIAnomalyExtended.getStateFromString(fStateCombo.getText());
 					final R4EAnomalyState oldState = ((R4EUIAnomalyBasic) fProperties.getElement()).getAnomaly()
 							.getState();
 					if (!newState.equals(oldState)) {
 						if (newState.equals(R4EAnomalyState.R4E_ANOMALY_STATE_REJECTED)
 								&& !oldState.equals(R4EAnomalyState.R4E_ANOMALY_STATE_REJECTED)) {
 							final boolean commentResult = ((R4EUIAnomalyBasic) fProperties.getElement()).createComment(true);
 							if (commentResult) {
 								UIUtils.changeAnomalyState(fProperties.getElement(), newState);
 								return;
 							} else {
 								final ErrorDialog dialog = new ErrorDialog(null, R4EUIConstants.DIALOG_TITLE_ERROR,
 										"Cannot change Anomaly State", new Status(IStatus.ERROR, R4EUIPlugin.PLUGIN_ID,
 												0, "Please enter a reason for rejecting this anomaly", null),
 										IStatus.ERROR);
 								Display.getDefault().syncExec(new Runnable() {
 									public void run() {
 										dialog.open();
 									}
 								});
 								refresh();
 								return;
 							}
 						}
 						UIUtils.changeAnomalyState(fProperties.getElement(), newState);
 					}
 				}
 				refresh();
 				R4EUIModelController.getNavigatorView().getTreeViewer().refresh();
 			}
 		});
 		addScrollListener(fStateCombo);
 
 		fStateLabel = widgetFactory.createCLabel(composite, R4EUIConstants.STATE_LABEL);
 		data = new FormData();
 		data.left = new FormAttachment(0, 0);
 		data.right = new FormAttachment(fStateCombo, -ITabbedPropertyConstants.HSPACE);
 		data.top = new FormAttachment(fStateCombo, 0, SWT.CENTER);
 		fStateLabel.setToolTipText(R4EUIConstants.ANOMALY_STATE_TOOLTIP);
 		fStateLabel.setLayoutData(data);
 
 		//Assigned To
 		fAssignedToCombo = widgetFactory.createCCombo(composite, SWT.READ_ONLY);
 		data = new FormData();
 		data.left = new FormAttachment(0, R4EUIConstants.TABBED_PROPERTY_LABEL_WIDTH);
 		data.right = new FormAttachment(100, 0); // $codepro.audit.disable numericLiterals
 		data.top = new FormAttachment(fStateCombo, ITabbedPropertyConstants.VSPACE);
 		fAssignedToCombo.setToolTipText(R4EUIConstants.ASSIGNED_TO_TOOLTIP);
 		fAssignedToCombo.setLayoutData(data);
 		fAssignedToCombo.addListener(SWT.Selection, new Listener() {
 			public void handleEvent(Event event) {
 				if (!fRefreshInProgress) {
 					try {
 						final String currentUser = R4EUIModelController.getReviewer();
 						final R4EAnomaly modelAnomaly = ((R4EUIAnomalyBasic) fProperties.getElement()).getAnomaly();
 						if (!modelAnomaly.getAssignedTo().contains(fAssignedToCombo.getText())) {
 							final Long bookNum = R4EUIModelController.FResourceUpdater.checkOut(modelAnomaly,
 									currentUser);
 							modelAnomaly.getAssignedTo().clear();
 							modelAnomaly.getAssignedTo().add(fAssignedToCombo.getText());
 							R4EUIModelController.FResourceUpdater.checkIn(bookNum);
 						}
 					} catch (ResourceHandlingException e1) {
 						UIUtils.displayResourceErrorDialog(e1);
 					} catch (OutOfSyncException e1) {
 						UIUtils.displaySyncErrorDialog(e1);
 					}
 				}
 				R4EUIModelController.getNavigatorView().getTreeViewer().refresh();
 				refresh();
 			}
 		});
 		addScrollListener(fAssignedToCombo);
 
 		final CLabel assignedToLabel = widgetFactory.createCLabel(composite, R4EUIConstants.ASSIGNED_TO_LABEL);
 		data = new FormData();
 		data.left = new FormAttachment(0, 0);
 		data.right = new FormAttachment(fAssignedToCombo, -ITabbedPropertyConstants.HSPACE);
 		data.top = new FormAttachment(fAssignedToCombo, 0, SWT.CENTER);
 		assignedToLabel.setToolTipText(R4EUIConstants.ASSIGNED_TO_TOOLTIP);
 		assignedToLabel.setLayoutData(data);
 
 		createParticipantDetailsSection(widgetFactory, composite, createAnomalyDetailsSection(widgetFactory, composite));
 	}
 
 	/**
 	 * Method createAnomalyDetailsSection.
 	 * 
 	 * @param aWidgetFactory
 	 *            TabbedPropertySheetWidgetFactory
 	 * @param aComposite
 	 *            Composite
 	 * @return Composite
 	 */
 	private Composite createAnomalyDetailsSection(TabbedPropertySheetWidgetFactory aWidgetFactory,
 			final Composite aComposite) {
 		//Anomaly Details section
 		final ExpandableComposite anomalyDetailsSection = aWidgetFactory.createExpandableComposite(aComposite,
 				ExpandableComposite.TWISTIE);
 		final FormData data = new FormData();
 		data.left = new FormAttachment(0, 0);
 		data.right = new FormAttachment(100, 0); // $codepro.audit.disable numericLiterals
 		data.top = new FormAttachment(fAssignedToCombo, ITabbedPropertyConstants.VSPACE);
 		anomalyDetailsSection.setLayoutData(data);
 		anomalyDetailsSection.setText(ANOMALY_DETAILS_SECTION_LABEL);
 		anomalyDetailsSection.addExpansionListener(new ExpansionAdapter() {
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
 		anomalyDetailsSection.setLayout(new GridLayout(1, false));
 
 		final Composite anomalyDetailsSectionClient = aWidgetFactory.createComposite(anomalyDetailsSection);
 		anomalyDetailsSectionClient.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 		anomalyDetailsSectionClient.setLayout(new GridLayout(4, false));
 		anomalyDetailsSection.setClient(anomalyDetailsSectionClient);
 
 		//Anomaly Creation Date (read-only)
 		final CLabel creationDateLabel = aWidgetFactory.createCLabel(anomalyDetailsSectionClient,
 				R4EUIConstants.CREATION_DATE_LABEL);
 		GridData gridData = new GridData(SWT.FILL, SWT.CENTER, false, false);
 		gridData.horizontalSpan = 1;
 		creationDateLabel.setToolTipText(R4EUIConstants.ANOMALY_CREATION_DATE_TOOLTIP);
 		creationDateLabel.setLayoutData(gridData);
 
 		aWidgetFactory.setBorderStyle(SWT.NULL);
 		fCreationDateText = aWidgetFactory.createText(anomalyDetailsSectionClient, "", SWT.NULL);
 		gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
 		gridData.horizontalSpan = 3;
 		fCreationDateText.setEditable(false);
 		fCreationDateText.setToolTipText(R4EUIConstants.ANOMALY_CREATION_DATE_TOOLTIP);
 		fCreationDateText.setLayoutData(gridData);
 
 		//Anomaly position (read-only)
 		final CLabel positionLabel = aWidgetFactory.createCLabel(anomalyDetailsSectionClient,
 				R4EUIConstants.POSITION_LABEL);
 		gridData = new GridData(SWT.FILL, SWT.CENTER, false, false);
 		gridData.horizontalSpan = 1;
 		positionLabel.setToolTipText(R4EUIConstants.ANOMALY_POSITION_TOOLTIP);
 		positionLabel.setLayoutData(gridData);
 
 		fPositionText = aWidgetFactory.createText(anomalyDetailsSectionClient, "", SWT.NULL);
 		gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
 		gridData.horizontalSpan = 3;
 		fPositionText.setEditable(false);
 		fPositionText.setToolTipText(R4EUIConstants.ANOMALY_POSITION_TOOLTIP);
 		fPositionText.setLayoutData(gridData);
 
 		//Class
 		final CLabel classLabel = aWidgetFactory.createCLabel(anomalyDetailsSectionClient, R4EUIConstants.CLASS_LABEL);
 		gridData = new GridData(SWT.FILL, SWT.CENTER, false, false);
 		gridData.horizontalSpan = 1;
 		classLabel.setToolTipText(R4EUIConstants.ANOMALY_CLASS_TOOLTIP);
 		classLabel.setLayoutData(gridData);
 
 		aWidgetFactory.setBorderStyle(SWT.BORDER);
 		fClassCombo = aWidgetFactory.createCCombo(anomalyDetailsSectionClient, SWT.READ_ONLY);
 		int size = UIUtils.getClasses().length;
 		fClassCombo.setVisibleItemCount(size);
 		gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
 		gridData.horizontalSpan = 3;
 		fClassCombo.setToolTipText(R4EUIConstants.ANOMALY_CLASS_TOOLTIP);
 		fClassCombo.setLayoutData(gridData);
 		fClassCombo.addListener(SWT.Selection, new Listener() {
 			public void handleEvent(Event event) {
 				if (!fRefreshInProgress) {
 					try {
 						final String currentUser = R4EUIModelController.getReviewer();
 						final R4EAnomaly modelAnomaly = ((R4EUIAnomalyBasic) fProperties.getElement()).getAnomaly();
 						R4EDesignRuleClass newClass = UIUtils.getClassFromString(fClassCombo.getText());
 						R4EDesignRuleClass oldClass = null;
 						if (null != modelAnomaly.getType()) {
 							oldClass = ((R4ECommentType) modelAnomaly.getType()).getType();
 						}
 						if (!newClass.equals(oldClass)) {
 							final Long bookNum = R4EUIModelController.FResourceUpdater.checkOut(modelAnomaly,
 									currentUser);
 							final R4ECommentType type = RModelFactoryExt.eINSTANCE.createR4ECommentType();
 							type.setType(newClass);
 							modelAnomaly.setType(type);
 							R4EUIModelController.FResourceUpdater.checkIn(bookNum);
 						}
 					} catch (ResourceHandlingException e1) {
 						UIUtils.displayResourceErrorDialog(e1);
 					} catch (OutOfSyncException e1) {
 						UIUtils.displaySyncErrorDialog(e1);
 					}
 					refresh();
 				}
 			}
 		});
 		addScrollListener(fClassCombo);
 
 		//Rank
 		final CLabel rankLabel = aWidgetFactory.createCLabel(anomalyDetailsSectionClient, R4EUIConstants.RANK_LABEL);
 		gridData = new GridData(SWT.FILL, SWT.CENTER, false, false);
 		gridData.horizontalSpan = 1;
 		rankLabel.setToolTipText(R4EUIConstants.ANOMALY_RANK_TOOLTIP);
 		rankLabel.setLayoutData(gridData);
 
 		fRankCombo = aWidgetFactory.createCCombo(anomalyDetailsSectionClient, SWT.READ_ONLY);
 		fRankCombo.setToolTipText(R4EUIConstants.ANOMALY_RANK_TOOLTIP);
 		gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
 		gridData.horizontalSpan = 3;
 		fRankCombo.setLayoutData(gridData);
 		fRankCombo.addListener(SWT.Selection, new Listener() {
 			public void handleEvent(Event event) {
 				if (!fRefreshInProgress) {
 					try {
 						final String currentUser = R4EUIModelController.getReviewer();
 						final R4EAnomaly modelAnomaly = ((R4EUIAnomalyBasic) fProperties.getElement()).getAnomaly();
 						R4EDesignRuleRank newRank = UIUtils.getRankFromString(fRankCombo.getText());
 						R4EDesignRuleRank oldRank = modelAnomaly.getRank();
 						if (!newRank.equals(oldRank)) {
 							final Long bookNum = R4EUIModelController.FResourceUpdater.checkOut(modelAnomaly,
 									currentUser);
 							modelAnomaly.setRank(newRank);
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
 		addScrollListener(fRankCombo);
 
 		//RuleId
 		final CLabel ruleIdLabel = aWidgetFactory.createCLabel(anomalyDetailsSectionClient,
 				R4EUIConstants.RULE_ID_LABEL);
 		gridData = new GridData(SWT.FILL, SWT.CENTER, false, false);
 		gridData.horizontalSpan = 1;
 		ruleIdLabel.setToolTipText(R4EUIConstants.ANOMALY_RULE_ID_TOOLTIP);
 		ruleIdLabel.setLayoutData(gridData);
 
 		final Composite ruleComposite = aWidgetFactory.createComposite(anomalyDetailsSectionClient);
 		gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
 		gridData.horizontalSpan = 3;
 		ruleComposite.setToolTipText(R4EUIConstants.ANOMALY_RULE_ID_TOOLTIP);
 		ruleComposite.setLayoutData(gridData);
 		ruleComposite.setLayout(new GridLayout(2, false));
 
 		fRuleId = aWidgetFactory.createText(ruleComposite, "", SWT.NULL);
 		fRuleId.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
 		fRuleId.setEditable(false);
 		fRuleButton = aWidgetFactory.createButton(ruleComposite, R4EUIConstants.UPDATE_LABEL, SWT.NONE);
 		fRuleButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
 		fRuleButton.addListener(SWT.Selection, new Listener() {
 			public void handleEvent(Event event) {
 				//Modify anomaly
 				R4EUIModelController.setJobInProgress(true);
 				final R4EAnomaly modelAnomaly = ((R4EUIAnomalyBasic) fProperties.getElement()).getAnomaly();
 				final IAnomalyInputDialog dialog = R4EUIDialogFactory.getInstance().getNewAnomalyInputDialog();
 				dialog.create();
 				dialog.setTitle(modelAnomaly.getTitle());
 				dialog.setDescription(modelAnomaly.getDescription());
 				dialog.setDueDate(modelAnomaly.getDueDate());
 				if (modelAnomaly.getAssignedTo().size() > 0) {
 					dialog.setAssigned(modelAnomaly.getAssignedTo().get(0));
 				}
 				if (null != modelAnomaly.getType()) {
 					dialog.setClass_(((R4ECommentType) modelAnomaly.getType()).getType());
 				}
 				dialog.setRank(modelAnomaly.getRank());
 				dialog.setRuleID(modelAnomaly.getRuleID());
 				final int result = dialog.open();
 				if (result == Window.OK) {
 					if (!fRefreshInProgress) {
 						try {
 							//Set new model data
 							final String currentUser = R4EUIModelController.getReviewer();
 							final Long bookNum = R4EUIModelController.FResourceUpdater.checkOut(modelAnomaly,
 									currentUser);
 							modelAnomaly.setTitle(dialog.getAnomalyTitleValue());
 							modelAnomaly.setDescription(dialog.getAnomalyDescriptionValue());
 							modelAnomaly.setDueDate(dialog.getDueDate());
 							modelAnomaly.getAssignedTo().clear();
 							modelAnomaly.getAssignedTo().add(dialog.getAssigned());
 							if (null != dialog.getRuleReferenceValue()) {
 								final R4EDesignRule rule = dialog.getRuleReferenceValue();
 								final R4ECommentType commentType = RModelFactory.eINSTANCE.createR4ECommentType();
 								commentType.setType(rule.getClass_());
 								modelAnomaly.setType(commentType);
 								modelAnomaly.setRank(rule.getRank());
 								modelAnomaly.setRuleID(rule.getId());
 							} else {
 								final R4ECommentType commentType = RModelFactory.eINSTANCE.createR4ECommentType();
 								commentType.setType(dialog.getClass_());
 								modelAnomaly.setType(commentType);
 								modelAnomaly.setRank(dialog.getRank());
 								modelAnomaly.setRuleID("");
 							}
 							R4EUIModelController.FResourceUpdater.checkIn(bookNum);
 
 							//Set new UI display
 							if (fProperties.getElement() instanceof R4EUIAnomalyExtended) {
 								fProperties.getElement().setName(
 										R4EUIAnomalyExtended.buildAnomalyExtName(modelAnomaly,
 												((R4EUIAnomalyBasic) fProperties.getElement()).getPosition()));
 							} else {
 								fProperties.getElement().setName(
 										R4EUIAnomalyBasic.buildAnomalyName(modelAnomaly,
 												((R4EUIAnomalyBasic) fProperties.getElement()).getPosition()));
 							}
 						} catch (ResourceHandlingException e1) {
 							UIUtils.displayResourceErrorDialog(e1);
 						} catch (OutOfSyncException e1) {
 							UIUtils.displaySyncErrorDialog(e1);
 						}
 					}
 				}
 				R4EUIModelController.setJobInProgress(false); //Enable view
				R4EUIModelController.getNavigatorView().getTreeViewer().refresh();
 				refresh();
 			}
 		});
 
 		//Due Date
 		final CLabel dateLabel = aWidgetFactory.createCLabel(anomalyDetailsSectionClient, R4EUIConstants.DUE_DATE_LABEL);
 		gridData = new GridData(SWT.FILL, SWT.CENTER, false, false);
 		gridData.horizontalSpan = 1;
 		dateLabel.setLayoutData(gridData);
 
 		final Composite dateComposite = aWidgetFactory.createComposite(anomalyDetailsSectionClient);
 		gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
 		gridData.horizontalSpan = 3;
 		dateComposite.setToolTipText(R4EUIConstants.ANOMALY_DUE_DATE_TOOLTIP);
 		dateComposite.setLayoutData(gridData);
 		dateComposite.setLayout(new GridLayout(2, false));
 
 		fDateText = aWidgetFactory.createText(dateComposite, "", SWT.NULL);
 		fDateText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
 		fDateText.setEditable(false);
 		fCalendarButton = aWidgetFactory.createButton(dateComposite, "...", SWT.NONE);
 		fCalendarButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
 		fCalendarButton.addListener(SWT.Selection, new Listener() {
 			public void handleEvent(Event event) {
 				final ICalendarDialog dialog = R4EUIDialogFactory.getInstance().getCalendarDialog();
 				final int result = dialog.open();
 				if (result == Window.OK) {
 					final SimpleDateFormat dateFormat = new SimpleDateFormat(R4EUIConstants.SIMPLE_DATE_FORMAT);
 					fDateText.setText(dateFormat.format(dialog.getDate()));
 					if (!fRefreshInProgress) {
 						try {
 							final String currentUser = R4EUIModelController.getReviewer();
 							final R4EAnomaly modelAnomaly = ((R4EUIAnomalyBasic) fProperties.getElement()).getAnomaly();
 							final Long bookNum = R4EUIModelController.FResourceUpdater.checkOut(modelAnomaly,
 									currentUser);
 							modelAnomaly.setDueDate(dialog.getDate());
 							R4EUIModelController.FResourceUpdater.checkIn(bookNum);
 						} catch (ResourceHandlingException e1) {
 							UIUtils.displayResourceErrorDialog(e1);
 						} catch (OutOfSyncException e1) {
 							UIUtils.displaySyncErrorDialog(e1);
 						}
 					}
 					refresh();
 				}
 			}
 		});
 
 		return anomalyDetailsSection;
 	}
 
 	/**
 	 * Method createParticipantDetailsSection.
 	 * 
 	 * @param aWidgetFactory
 	 *            TabbedPropertySheetWidgetFactory
 	 * @param aComposite
 	 *            Composite
 	 * @param aTopComposite
 	 *            Composite
 	 */
 	private void createParticipantDetailsSection(TabbedPropertySheetWidgetFactory aWidgetFactory,
 			final Composite aComposite, final Composite aTopComposite) {
 		//Participants Details section
 		final ExpandableComposite participantDetailsSection = aWidgetFactory.createExpandableComposite(aComposite,
 				ExpandableComposite.TWISTIE);
 		final FormData data = new FormData();
 		data.left = new FormAttachment(0, 0);
 		data.right = new FormAttachment(100, 0); // $codepro.audit.disable numericLiterals
 		data.top = new FormAttachment(aTopComposite, ITabbedPropertyConstants.VSPACE);
 		participantDetailsSection.setLayoutData(data);
 		participantDetailsSection.setText(PARTICIPANT_DETAILS_SECTION_LABEL);
 		participantDetailsSection.addExpansionListener(new ExpansionAdapter() {
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
 		participantDetailsSection.setLayout(new GridLayout(1, false));
 
 		final Composite participantDetailsSectionClient = aWidgetFactory.createComposite(participantDetailsSection);
 		participantDetailsSectionClient.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 		participantDetailsSectionClient.setLayout(new GridLayout(4, false));
 		participantDetailsSection.setClient(participantDetailsSectionClient);
 
 		//Anomaly Author (read-only)
 		final CLabel authorLabel = aWidgetFactory.createCLabel(participantDetailsSectionClient,
 				R4EUIConstants.AUTHOR_LABEL);
 		GridData gridData = new GridData(SWT.FILL, SWT.CENTER, false, false);
 		gridData.horizontalSpan = 1;
 		authorLabel.setToolTipText(R4EUIConstants.ANOMALY_AUTHOR_TOOLTIP);
 		authorLabel.setLayoutData(gridData);
 
 		aWidgetFactory.setBorderStyle(SWT.NULL);
 		fAuthorText = aWidgetFactory.createText(participantDetailsSectionClient, "", SWT.NULL);
 		gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
 		gridData.horizontalSpan = 3;
 		fAuthorText.setEditable(false);
 		fAuthorText.setToolTipText(R4EUIConstants.ANOMALY_AUTHOR_TOOLTIP);
 		fAuthorText.setLayoutData(gridData);
 
 		//Decided by
 		aWidgetFactory.setBorderStyle(SWT.BORDER);
 		fDecidedByLabel = aWidgetFactory.createCLabel(participantDetailsSectionClient, R4EUIConstants.DECIDED_BY_LABEL);
 		gridData = new GridData(SWT.FILL, SWT.CENTER, false, false);
 		gridData.horizontalSpan = 1;
 		fDecidedByLabel.setToolTipText(R4EUIConstants.ANOMALY_DECIDED_BY_TOOLTIP);
 		fDecidedByLabel.setLayoutData(gridData);
 
 		fDecidedByCombo = aWidgetFactory.createCCombo(participantDetailsSectionClient, SWT.READ_ONLY);
 		gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
 		gridData.horizontalSpan = 3;
 		fDecidedByCombo.setToolTipText(R4EUIConstants.ANOMALY_DECIDED_BY_TOOLTIP);
 		fDecidedByCombo.setLayoutData(gridData);
 		fDecidedByCombo.addListener(SWT.Selection, new Listener() {
 			public void handleEvent(Event event) {
 				if (!fRefreshInProgress) {
 					try {
 						final String currentUser = R4EUIModelController.getReviewer();
 						final R4EAnomaly modelAnomaly = ((R4EUIAnomalyExtended) fProperties.getElement()).getAnomaly();
 
 						if (!fDecidedByCombo.getText().equals(modelAnomaly.getDecidedByID())) {
 							final Long bookNum = R4EUIModelController.FResourceUpdater.checkOut(modelAnomaly,
 									currentUser);
 							modelAnomaly.setDecidedByID(fDecidedByCombo.getText());
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
 		addScrollListener(fDecidedByCombo);
 
 		//Fixed by
 		fFixedByLabel = aWidgetFactory.createCLabel(participantDetailsSectionClient, R4EUIConstants.FIXED_BY_LABEL);
 		gridData = new GridData(SWT.FILL, SWT.CENTER, false, false);
 		gridData.horizontalSpan = 1;
 		fFixedByLabel.setToolTipText(R4EUIConstants.ANOMALY_FIXED_BY_TOOLTIP);
 		fFixedByLabel.setLayoutData(gridData);
 
 		fFixedByCombo = aWidgetFactory.createCCombo(participantDetailsSectionClient, SWT.READ_ONLY);
 		gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
 		gridData.horizontalSpan = 3;
 		fFixedByCombo.setToolTipText(R4EUIConstants.ANOMALY_FIXED_BY_TOOLTIP);
 		fFixedByCombo.setLayoutData(gridData);
 		fFixedByCombo.addListener(SWT.Selection, new Listener() {
 			public void handleEvent(Event event) {
 				if (!fRefreshInProgress) {
 					try {
 						final String currentUser = R4EUIModelController.getReviewer();
 						final R4EAnomaly modelAnomaly = ((R4EUIAnomalyExtended) fProperties.getElement()).getAnomaly();
 						if (!fFixedByCombo.getText().equals(modelAnomaly.getFixedByID())) {
 							final Long bookNum = R4EUIModelController.FResourceUpdater.checkOut(modelAnomaly,
 									currentUser);
 							modelAnomaly.setFixedByID(fFixedByCombo.getText());
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
 		addScrollListener(fFixedByCombo);
 
 		//Follow-up by
 		fFollowUpByLabel = aWidgetFactory.createCLabel(participantDetailsSectionClient,
 				R4EUIConstants.FOLLOWUP_BY_LABEL);
 		gridData = new GridData(SWT.FILL, SWT.CENTER, false, false);
 		gridData.horizontalSpan = 1;
 		fFollowUpByLabel.setToolTipText(R4EUIConstants.ANOMALY_FOLLOWUP_BY_TOOLTIP);
 		fFollowUpByLabel.setLayoutData(gridData);
 
 		fFollowUpByCombo = aWidgetFactory.createCCombo(participantDetailsSectionClient, SWT.READ_ONLY);
 		gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
 		gridData.horizontalSpan = 3;
 		fFollowUpByCombo.setToolTipText(R4EUIConstants.ANOMALY_FOLLOWUP_BY_TOOLTIP);
 		fFollowUpByCombo.setLayoutData(gridData);
 		fFollowUpByCombo.addListener(SWT.Selection, new Listener() {
 			public void handleEvent(Event event) {
 				if (!fRefreshInProgress) {
 					try {
 						final String currentUser = R4EUIModelController.getReviewer();
 						final R4EAnomaly modelAnomaly = ((R4EUIAnomalyExtended) fProperties.getElement()).getAnomaly();
 						if (!fFollowUpByCombo.getText().equals(modelAnomaly.getFollowUpByID())) {
 							final Long bookNum = R4EUIModelController.FResourceUpdater.checkOut(modelAnomaly,
 									currentUser);
 							modelAnomaly.setFollowUpByID(fFollowUpByCombo.getText());
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
 		addScrollListener(fFollowUpByCombo);
 	}
 
 	/**
 	 * Method refresh.
 	 * 
 	 * @see org.eclipse.ui.views.properties.tabbed.ISection#refresh()
 	 */
 	@Override
 	public void refresh() {
 		fRefreshInProgress = true;
 		final R4EUIAnomalyBasic uiModelAnomaly = (R4EUIAnomalyBasic) fProperties.getElement();
 		final R4EAnomaly modelAnomaly = uiModelAnomaly.getAnomaly();
 		fTitleText.setText(modelAnomaly.getTitle());
 		fAuthorText.setText(modelAnomaly.getUser().getId());
 		fCreationDateText.setText(modelAnomaly.getCreatedOn().toString());
 		fPosition = ((R4EUIAnomalyBasic) fProperties.getElement()).getPosition();
 		if (null == fPosition) {
 			fPositionText.setText(R4EUIConstants.GLOBAL_ANOMALY_PROPERTY_VALUE);
 		} else {
 			fPositionText.setText(fPosition.toString());
 		}
 		fDescriptionText.setText(modelAnomaly.getDescription());
 
 		//Here we need to get a handle to the parent review to get the list of participants
 		String[] participants = { "" };
 		IR4EUIModelElement element = uiModelAnomaly.getParent().getParent();
 		if (element instanceof R4EUIReviewBasic) { //Global anomaly
 			List<String> participantsStr = ((R4EUIReviewBasic) element).getParticipantIDs();
 			participants = participantsStr.toArray(new String[participantsStr.size()]);
 		} else if (element instanceof R4EUIFileContext) { //Local anomaly
 			List<String> participantsStr = ((R4EUIReviewBasic) element.getParent().getParent()).getParticipantIDs();
 			participants = participantsStr.toArray(new String[participantsStr.size()]);
 		}
 		//Reset assignment combo values to current participants
 		fAssignedToCombo.removeAll();
 		fAssignedToCombo.add("");
 		for (String participant : participants) {
 			fAssignedToCombo.add(participant);
 		}
 		if (modelAnomaly.getAssignedTo().size() > 0 && null != modelAnomaly.getAssignedTo().get(0)) {
 			fAssignedToCombo.setText(modelAnomaly.getAssignedTo().get(0));
 		} else {
 			fAssignedToCombo.setText("");
 		}
 
 		fClassCombo.setItems(UIUtils.getClasses());
 		if (null != modelAnomaly.getType() && null != ((R4ECommentType) modelAnomaly.getType()).getType()) {
 			fClassCombo.select(((R4ECommentType) modelAnomaly.getType()).getType().getValue());
 		} else {
 			fClassCombo.setText("");
 		}
 
 		fRankCombo.setItems(UIUtils.getRanks());
 		//Bug 368865:  Mapping needed for DEPRECATED value to MINOR
 		final int rankValue = modelAnomaly.getRank().getValue();
 		fRankCombo.select(rankValue == R4EDesignRuleRank.R4E_RANK_DEPRECATED_VALUE
 				? R4EDesignRuleRank.R4E_RANK_MINOR_VALUE
 				: rankValue);
 
 		if (null != modelAnomaly.getRuleID()) {
 			fRuleId.setText(modelAnomaly.getRuleID());
 		}
 		if (null != modelAnomaly.getDueDate()) {
 			final SimpleDateFormat dateFormat = new SimpleDateFormat(R4EUIConstants.SIMPLE_DATE_FORMAT);
 			fDateText.setText(dateFormat.format(modelAnomaly.getDueDate()));
 		} else {
 			fDateText.setText("");
 		}
 
 		if (fProperties.getElement() instanceof R4EUIAnomalyExtended) {
 			fStateCombo.setItems(((R4EUIAnomalyExtended) uiModelAnomaly).getAvailableStates());
 			fStateCombo.select(((R4EUIAnomalyExtended) uiModelAnomaly).mapStateToIndex(modelAnomaly.getState()));
 
 			if (null != R4EUIModelController.getActiveReview()) {
 				fDecidedByCombo.setItems(participants);
 				fDecidedByCombo.select(UIUtils.mapParticipantToIndex(modelAnomaly.getDecidedByID()));
 				fFixedByCombo.setItems(participants);
 				fFixedByCombo.select(UIUtils.mapParticipantToIndex(modelAnomaly.getFixedByID()));
 				fFollowUpByCombo.setItems(participants);
 				fFollowUpByCombo.select(UIUtils.mapParticipantToIndex(modelAnomaly.getFollowUpByID()));
 			}
 		}
 		setEnabledFields();
 		fRefreshInProgress = false;
 	}
 
 	/**
 	 * Method setEditableFields.
 	 */
 	@Override
 	protected void setEnabledFields() {
 		//Since it is a subclass of this class we need this here.  This should be improved later
 		//Do not show Anomaly elements for the Postponed Anomaly.
 		if (fProperties.getElement() instanceof R4EUIPostponedAnomaly) {
 			fTitleText.getParent().getParent().setVisible(false);
 			return;
 		}
 
 		if (R4EUIModelController.isJobInProgress()
 				|| fProperties.getElement().isReadOnly()
 				|| null == R4EUIModelController.getActiveReview()
 				|| ((R4EReviewState) R4EUIModelController.getActiveReview().getReview().getState()).getState().equals(
 						R4EReviewPhase.R4E_REVIEW_PHASE_COMPLETED) || !fProperties.getElement().isEnabled()) {
 			fTitleText.setForeground(UIUtils.DISABLED_FONT_COLOR);
 			fTitleText.setEditable(false);
 			fAuthorText.setForeground(UIUtils.DISABLED_FONT_COLOR);
 			fCreationDateText.setForeground(UIUtils.DISABLED_FONT_COLOR);
 			fPositionText.setForeground(UIUtils.DISABLED_FONT_COLOR);
 			fDescriptionText.setForeground(UIUtils.DISABLED_FONT_COLOR);
 			fStateCombo.setEnabled(false);
 			fClassCombo.setEnabled(false);
 			fRankCombo.setEnabled(false);
 			fDateText.setForeground(UIUtils.DISABLED_FONT_COLOR);
 			fCalendarButton.setEnabled(false);
 			fDecidedByCombo.setEnabled(false);
 			fFixedByCombo.setEnabled(false);
 			fFollowUpByCombo.setEnabled(false);
 			fRuleButton.setEnabled(false);
 			fRuleId.setForeground(UIUtils.DISABLED_FONT_COLOR);
 			fAssignedToCombo.setEnabled(false);
 			if (fProperties.getElement() instanceof R4EUIAnomalyExtended) {
 				fStateLabel.setVisible(true);
 				fStateCombo.setVisible(true);
 				fDecidedByLabel.setVisible(true);
 				fDecidedByCombo.setVisible(true);
 				fFixedByLabel.setVisible(true);
 				fFixedByCombo.setVisible(true);
 				fFollowUpByLabel.setVisible(true);
 				fFollowUpByCombo.setVisible(true);
 			} else {
 				fStateLabel.setVisible(false);
 				fStateCombo.setVisible(false);
 				fDecidedByLabel.setVisible(false);
 				fDecidedByCombo.setVisible(false);
 				fFixedByLabel.setVisible(false);
 				fFixedByCombo.setVisible(false);
 				fFollowUpByLabel.setVisible(false);
 				fFollowUpByCombo.setVisible(false);
 			}
 		} else {
 			fAuthorText.setForeground(UIUtils.ENABLED_FONT_COLOR);
 			fCreationDateText.setForeground(UIUtils.ENABLED_FONT_COLOR);
 			fPositionText.setForeground(UIUtils.ENABLED_FONT_COLOR);
 			fDescriptionText.setForeground(UIUtils.ENABLED_FONT_COLOR);
 			fRuleButton.setEnabled(true);
 			fRuleId.setForeground(UIUtils.ENABLED_FONT_COLOR);
 			fAssignedToCombo.setEnabled(true);
 
 			final R4EUIAnomalyBasic uiAnomaly = (R4EUIAnomalyBasic) fProperties.getElement();
 
 			if (uiAnomaly.isTitleEnabled()) {
 				fTitleText.setForeground(UIUtils.ENABLED_FONT_COLOR);
 				fTitleText.setEditable(true);
 			} else {
 				fTitleText.setForeground(UIUtils.DISABLED_FONT_COLOR);
 				fTitleText.setEditable(false);
 			}
 
 			if (uiAnomaly.isDueDateEnabled()) {
 				fDateText.setForeground(UIUtils.ENABLED_FONT_COLOR);
 				fCalendarButton.setEnabled(true);
 			} else {
 				fDateText.setForeground(UIUtils.DISABLED_FONT_COLOR);
 				fCalendarButton.setEnabled(false);
 			}
 
 			if (uiAnomaly.isClassEnabled()) {
 				fClassCombo.setEnabled(true);
 			} else {
 				fClassCombo.setEnabled(false);
 			}
 
 			if (uiAnomaly.isRankEnabled()) {
 				fRankCombo.setEnabled(true);
 			} else {
 				fRankCombo.setEnabled(false);
 			}
 
 			if (fProperties.getElement() instanceof R4EUIAnomalyExtended) {
 				fStateLabel.setVisible(true);
 				fStateCombo.setVisible(true);
 				fStateCombo.setEnabled(true);
 
 				fDecidedByLabel.setVisible(true);
 				fDecidedByCombo.setVisible(true);
 				if (((R4EUIAnomalyExtended) uiAnomaly).isDecidedByEnabled()) {
 					fDecidedByCombo.setEnabled(true);
 				} else {
 					fDecidedByCombo.setEnabled(false);
 				}
 
 				fFixedByLabel.setVisible(true);
 				fFixedByCombo.setVisible(true);
 				if (((R4EUIAnomalyExtended) uiAnomaly).isFixedByEnabled()) {
 					fFixedByCombo.setEnabled(true);
 				} else {
 					fFixedByCombo.setEnabled(false);
 				}
 
 				fFollowUpByLabel.setVisible(true);
 				fFollowUpByCombo.setVisible(true);
 				if (((R4EUIAnomalyExtended) uiAnomaly).isFollowUpByEnabled()) {
 					fFollowUpByCombo.setEnabled(true);
 				} else {
 					fFollowUpByCombo.setEnabled(false);
 				}
 			} else {
 				fStateLabel.setVisible(false);
 				fStateCombo.setVisible(false);
 				fDecidedByCombo.setEnabled(false);
 				fFixedByCombo.setEnabled(false);
 				fFollowUpByCombo.setEnabled(false);
 				fDecidedByLabel.setVisible(false);
 				fDecidedByCombo.setVisible(false);
 				fFixedByLabel.setVisible(false);
 				fFixedByCombo.setVisible(false);
 				fFollowUpByLabel.setVisible(false);
 				fFollowUpByCombo.setVisible(false);
 			}
 		}
 	}
 }
