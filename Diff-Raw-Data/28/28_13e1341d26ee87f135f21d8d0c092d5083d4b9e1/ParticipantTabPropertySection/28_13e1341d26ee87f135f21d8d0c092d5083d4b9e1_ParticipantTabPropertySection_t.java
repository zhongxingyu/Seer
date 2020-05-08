 // $codepro.audit.disable com.instantiations.assist.eclipse.analysis.audit.rule.effectivejava.alwaysOverridetoString.alwaysOverrideToString, staticFieldSecurity, com.instantiations.assist.eclipse.analysis.deserializeabilitySecurity, com.instantiations.assist.eclipse.analysis.enforceCloneableUsageSecurity, explicitThisUsage
 /*******************************************************************************
  * Copyright (c) 2010, 2012 Ericsson AB and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Ericsson AB - initial API and implementation
  *******************************************************************************/
 
 package org.eclipse.mylyn.reviews.r4e.ui.internal.properties.tabbed;
 
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.common.util.EMap;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EAnomaly;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EComment;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EParticipant;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EReviewPhase;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EReviewState;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EReviewType;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EUserRole;
 import org.eclipse.mylyn.reviews.r4e.core.model.serial.impl.OutOfSyncException;
 import org.eclipse.mylyn.reviews.r4e.core.model.serial.impl.ResourceHandlingException;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIModelController;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIParticipant;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIReviewExtended;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.utils.CommandUtils;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.utils.EditableListWidget;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.utils.IEditableListListener;
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
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Item;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.TableItem;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.forms.events.ExpansionAdapter;
 import org.eclipse.ui.forms.events.ExpansionEvent;
 import org.eclipse.ui.forms.widgets.ExpandableComposite;
 import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
 import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
 import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetWidgetFactory;
 
 /**
  * This class implements the tabbed property section for the Participant model element
  * 
  * @author Sebastien Dubois
  * @version $Revision: 1.0 $
  */
 public class ParticipantTabPropertySection extends ModelElementTabPropertySection implements IEditableListListener {
 
 	// ------------------------------------------------------------------------
 	// Constants
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Field USER_DETAILS_LABEL. (value is ""User Details"")
 	 */
 	private static final String USER_DETAILS_LABEL = "User Details";
 
 	/**
 	 * Field ROLE_SECTION_LABEL. (value is ""Role Management"")
 	 */
 	private static final String ROLE_SECTION_LABEL = "Role Management";
 
 	/**
 	 * Field TIME_SECTION_LABEL. (value is ""Time Management"")
 	 */
 	private static final String TIME_SECTION_LABEL = "Time Management";
 
 	// ------------------------------------------------------------------------
 	// Member variables
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Field FIdText.
 	 */
 	private Text fIdText = null;
 
 	/**
 	 * Field fEmailText.
 	 */
 	protected Text fEmailText = null;
 
 	/**
 	 * Field FNumItemsText.
 	 */
 	private Text fNumItemsText = null;
 
 	/**
 	 * Field FNumAnomaliesText.
 	 */
 	private Text fNumAnomaliesText = null;
 
 	/**
 	 * Field FNumCommentsText.
 	 */
 	private Text fNumCommentsText = null;
 
 	/**
 	 * Field fDetailsText.
 	 */
 	private Text fDetailsText = null;
 
 	/**
 	 * Field fTimeSection.
 	 */
 	private ExpandableComposite fTimeSection = null;
 
 	/**
 	 * Field fTimeSpentDetailedList.
 	 */
 	protected EditableListWidget fTimeSpentDetailedList = null;
 
 	/**
 	 * Field fRolesSection.
 	 */
 	private ExpandableComposite fRolesSection = null;
 
 	/**
 	 * Field fRolesList.
 	 */
 	private EditableListWidget fRolesList = null;
 
 	/**
 	 * Field fFocusAreaText.
 	 */
 	protected Text fFocusAreaText = null;
 
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
 		super.createControls(parent, aTabbedPropertySheetPage);
 
 		//Tell element to build its own detailed tab layout
 		final TabbedPropertySheetWidgetFactory widgetFactory = aTabbedPropertySheetPage.getWidgetFactory();
 		final Composite mainForm = widgetFactory.createFlatFormComposite(parent);
 		FormData data = null;
 
 		//Author (read-only)
 		widgetFactory.setBorderStyle(SWT.NULL);
 		fIdText = widgetFactory.createText(mainForm, "", SWT.NULL);
 		data = new FormData();
 		data.left = new FormAttachment(0, R4EUIConstants.TABBED_PROPERTY_LABEL_WIDTH);
 		data.right = new FormAttachment(100, 0); // $codepro.audit.disable numericLiterals
 		data.top = new FormAttachment(0, ITabbedPropertyConstants.VSPACE);
 		fIdText.setEditable(false);
 		fIdText.setToolTipText(R4EUIConstants.PARTICIPANT_ID_TOOLTIP);
 		fIdText.setLayoutData(data);
 
 		final CLabel idLabel = widgetFactory.createCLabel(mainForm, R4EUIConstants.ID_LABEL);
 		data = new FormData();
 		data.left = new FormAttachment(0, 0);
 		data.right = new FormAttachment(fIdText, -ITabbedPropertyConstants.HSPACE);
 		data.top = new FormAttachment(fIdText, 0, SWT.CENTER);
 		idLabel.setToolTipText(R4EUIConstants.PARTICIPANT_ID_TOOLTIP);
 		idLabel.setLayoutData(data);
 
 		//Email
 		widgetFactory.setBorderStyle(SWT.BORDER);
 		fEmailText = widgetFactory.createText(mainForm, "");
 		data = new FormData();
 		data.left = new FormAttachment(0, R4EUIConstants.TABBED_PROPERTY_LABEL_WIDTH);
 		data.right = new FormAttachment(100, 0); // $codepro.audit.disable numericLiterals
 		data.top = new FormAttachment(fIdText, ITabbedPropertyConstants.VSPACE);
 		fEmailText.setToolTipText(R4EUIConstants.PARTICIPANT_EMAIL_TOOLTIP);
 		fEmailText.setLayoutData(data);
 		fEmailText.addListener(SWT.FocusOut, new Listener() {
 			public void handleEvent(Event event) {
 				if (!fRefreshInProgress && fEmailText.getForeground().equals(UIUtils.ENABLED_FONT_COLOR)) {
 					try {
 						String newValue = fEmailText.getText().trim();
 						if (!CommandUtils.isEmailValid(newValue)) {
 							//Validation of input failed
 							return;
 						}
 						final String currentUser = R4EUIModelController.getReviewer();
 						final R4EParticipant modelParticipant = ((R4EUIParticipant) fProperties.getElement()).getParticipant();
 						if (!newValue.equals(modelParticipant.getEmail())) {
 							final Long bookNum = R4EUIModelController.FResourceUpdater.checkOut(modelParticipant,
 									currentUser);
 							modelParticipant.setEmail(newValue);
 							R4EUIModelController.FResourceUpdater.checkIn(bookNum);
 						}
 						fEmailText.setText(newValue);
 					} catch (ResourceHandlingException e1) {
 						UIUtils.displayResourceErrorDialog(e1);
 					} catch (OutOfSyncException e1) {
 						UIUtils.displaySyncErrorDialog(e1);
 					}
 				}
 			}
 		});
 		UIUtils.addTabbedPropertiesTextResizeListener(fEmailText);
 
 		final CLabel emailLabel = widgetFactory.createCLabel(mainForm, R4EUIConstants.EMAIL_LABEL);
 		data = new FormData();
 		data.left = new FormAttachment(0, 0);
 		data.right = new FormAttachment(fEmailText, -ITabbedPropertyConstants.HSPACE);
 		data.top = new FormAttachment(fEmailText, 0, SWT.CENTER);
 		emailLabel.setToolTipText(R4EUIConstants.PARTICIPANT_EMAIL_TOOLTIP);
 		emailLabel.setLayoutData(data);
 
 		//Number of Review Items added (read-only)
 		widgetFactory.setBorderStyle(SWT.NULL);
 		fNumItemsText = widgetFactory.createText(mainForm, "", SWT.NULL);
 		fNumItemsText.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
 		data = new FormData();
 		data.left = new FormAttachment(0, R4EUIConstants.TABBED_PROPERTY_LABEL_WIDTH);
 		data.right = new FormAttachment(100, 0); // $codepro.audit.disable numericLiterals
 		data.top = new FormAttachment(fIdText, ITabbedPropertyConstants.VSPACE);
 		fNumItemsText.setEditable(false);
 		fNumItemsText.setToolTipText(R4EUIConstants.PARTICIPANT_NUM_ITEMS_TOOLTIP);
 		fNumItemsText.setLayoutData(data);
 
 		final CLabel numItemsLabel = widgetFactory.createCLabel(mainForm, R4EUIConstants.NUM_ITEMS_LABEL);
 		data = new FormData();
 		data.left = new FormAttachment(0, 0);
 		data.right = new FormAttachment(fNumItemsText, -ITabbedPropertyConstants.HSPACE);
 		data.top = new FormAttachment(fNumItemsText, 0, SWT.CENTER);
 		numItemsLabel.setToolTipText(R4EUIConstants.PARTICIPANT_NUM_ITEMS_TOOLTIP);
 		numItemsLabel.setLayoutData(data);
 
 		//Number of Anomalies added (read-only)
 		fNumAnomaliesText = widgetFactory.createText(mainForm, "", SWT.NULL);
 		data = new FormData();
 		data.left = new FormAttachment(0, R4EUIConstants.TABBED_PROPERTY_LABEL_WIDTH);
 		data.right = new FormAttachment(100, 0); // $codepro.audit.disable numericLiterals
 		data.top = new FormAttachment(fNumItemsText, ITabbedPropertyConstants.VSPACE);
 		fNumAnomaliesText.setEditable(false);
 		fNumAnomaliesText.setToolTipText(R4EUIConstants.PARTICIPANT_NUM_ANOMALIES_TOOLTIP);
 		fNumAnomaliesText.setLayoutData(data);
 
 		final CLabel numAnomaliesLabel = widgetFactory.createCLabel(mainForm, R4EUIConstants.NUM_ANOMALIES_LABEL);
 		data = new FormData();
 		data.left = new FormAttachment(0, 0);
 		data.right = new FormAttachment(fNumAnomaliesText, -ITabbedPropertyConstants.HSPACE);
 		data.top = new FormAttachment(fNumAnomaliesText, 0, SWT.CENTER);
 		numAnomaliesLabel.setToolTipText(R4EUIConstants.PARTICIPANT_NUM_ANOMALIES_TOOLTIP);
 		numAnomaliesLabel.setLayoutData(data);
 
 		//Number of Comments added (read-only)
 		fNumCommentsText = widgetFactory.createText(mainForm, "", SWT.NULL);
 		data = new FormData();
 		data.left = new FormAttachment(0, R4EUIConstants.TABBED_PROPERTY_LABEL_WIDTH);
 		data.right = new FormAttachment(100, 0); // $codepro.audit.disable numericLiterals
 		data.top = new FormAttachment(fNumAnomaliesText, ITabbedPropertyConstants.VSPACE);
 		fNumCommentsText.setEditable(false);
 		fNumCommentsText.setToolTipText(R4EUIConstants.PARTICIPANT_NUM_COMMENTS_TOOLTIP);
 		fNumCommentsText.setLayoutData(data);
 
 		final CLabel numCommentsLabel = widgetFactory.createCLabel(mainForm, R4EUIConstants.NUM_COMMENTS_LABEL);
 		data = new FormData();
 		data.left = new FormAttachment(0, 0);
 		data.right = new FormAttachment(fNumCommentsText, -ITabbedPropertyConstants.HSPACE);
 		data.top = new FormAttachment(fNumCommentsText, 0, SWT.CENTER);
 		numCommentsLabel.setToolTipText(R4EUIConstants.PARTICIPANT_NUM_COMMENTS_TOOLTIP);
 		numCommentsLabel.setLayoutData(data);
 
 		//Focus Area
 		widgetFactory.setBorderStyle(SWT.BORDER);
 		fFocusAreaText = widgetFactory.createText(mainForm, "");
 		data = new FormData();
 		data.left = new FormAttachment(0, R4EUIConstants.TABBED_PROPERTY_LABEL_WIDTH);
 		data.right = new FormAttachment(100, 0); // $codepro.audit.disable numericLiterals
 		data.top = new FormAttachment(fNumCommentsText, ITabbedPropertyConstants.VSPACE);
 		fFocusAreaText.setToolTipText(R4EUIConstants.PARTICIPANT_FOCUS_AREA_TOOLTIP);
 		fFocusAreaText.setLayoutData(data);
 		fFocusAreaText.addListener(SWT.FocusOut, new Listener() {
 			public void handleEvent(Event event) {
 				if (!fRefreshInProgress && fFocusAreaText.getForeground().equals(UIUtils.ENABLED_FONT_COLOR)) {
 					try {
 						final String currentUser = R4EUIModelController.getReviewer();
 						final R4EParticipant modelParticipant = ((R4EUIParticipant) fProperties.getElement()).getParticipant();
 						String newValue = fFocusAreaText.getText().trim();
 						if (!newValue.equals(modelParticipant.getFocusArea())) {
 							final Long bookNum = R4EUIModelController.FResourceUpdater.checkOut(modelParticipant,
 									currentUser);
 							modelParticipant.setFocusArea(newValue);
 							R4EUIModelController.FResourceUpdater.checkIn(bookNum);
 						}
 						fFocusAreaText.setText(newValue);
 					} catch (ResourceHandlingException e1) {
 						UIUtils.displayResourceErrorDialog(e1);
 					} catch (OutOfSyncException e1) {
 						UIUtils.displaySyncErrorDialog(e1);
 					}
 				}
 			}
 		});
 		UIUtils.addTabbedPropertiesTextResizeListener(fFocusAreaText);
 
 		final CLabel focusAreaLabel = widgetFactory.createCLabel(mainForm, R4EUIConstants.FOCUS_AREA_LABEL);
 		data = new FormData();
 		data.left = new FormAttachment(0, 0);
 		data.right = new FormAttachment(fFocusAreaText, -ITabbedPropertyConstants.HSPACE);
 		data.top = new FormAttachment(fFocusAreaText, 0, SWT.CENTER);
 		focusAreaLabel.setToolTipText(R4EUIConstants.PARTICIPANT_FOCUS_AREA_TOOLTIP);
 		focusAreaLabel.setLayoutData(data);
 
 		createDetailsSections(widgetFactory, mainForm);
 	}
 
 	/**
 	 * Method createParticipantDetailsSection.
 	 * 
 	 * @param aWidgetFactory
 	 *            TabbedPropertySheetWidgetFactory
 	 * @param aComposite
 	 *            Composite
 	 */
 	private void createDetailsSections(TabbedPropertySheetWidgetFactory aWidgetFactory, final Composite aComposite) {
 		//Participant Details section
 		final ExpandableComposite partDetailsSection = aWidgetFactory.createExpandableComposite(aComposite,
 				ExpandableComposite.TWISTIE);
 		FormData data = new FormData();
 		data.left = new FormAttachment(0, 0);
 		data.right = new FormAttachment(100, 0); // $codepro.audit.disable numericLiterals
 		data.top = new FormAttachment(fFocusAreaText, ITabbedPropertyConstants.VSPACE);
 		partDetailsSection.setLayoutData(data);
 		partDetailsSection.setText(USER_DETAILS_LABEL);
 		partDetailsSection.addExpansionListener(new ExpansionAdapter() {
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
 		partDetailsSection.setLayout(new GridLayout(1, false));
 
 		final Composite partDetailsSectionClient = aWidgetFactory.createComposite(partDetailsSection);
 		partDetailsSectionClient.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
 		partDetailsSectionClient.setLayout(new GridLayout(4, false));
 		partDetailsSection.setClient(partDetailsSectionClient);
 
 		//Participant Details (read-only)
 		final CLabel detailsLabel = aWidgetFactory.createCLabel(partDetailsSectionClient,
 				R4EUIConstants.USER_DETAILS_LABEL);
 		GridData gridData = new GridData(GridData.FILL, GridData.FILL, false, false);
 		gridData.horizontalSpan = 1;
 		detailsLabel.setToolTipText(R4EUIConstants.PARTICIPANT_DETAILS_TOOLTIP);
 		detailsLabel.setLayoutData(gridData);
 
 		aWidgetFactory.setBorderStyle(SWT.NULL);
 		fDetailsText = aWidgetFactory.createText(partDetailsSectionClient, "", SWT.MULTI);
 		gridData = new GridData(GridData.FILL, GridData.FILL, true, true);
 		gridData.horizontalSpan = 3;
 		fDetailsText.setEditable(false);
 		fDetailsText.setToolTipText(R4EUIConstants.PARTICIPANT_DETAILS_TOOLTIP);
 		fDetailsText.setLayoutData(gridData);
 		aWidgetFactory.setBorderStyle(SWT.BORDER);
 
 		//Roles section
 		fRolesSection = aWidgetFactory.createExpandableComposite(aComposite, ExpandableComposite.TWISTIE);
 		data = new FormData();
 		data.left = new FormAttachment(0, 0);
 		data.right = new FormAttachment(100, 0); // $codepro.audit.disable numericLiterals
 		data.top = new FormAttachment(partDetailsSection, ITabbedPropertyConstants.VSPACE);
 		fRolesSection.setLayoutData(data);
 		fRolesSection.setText(ROLE_SECTION_LABEL);
 		fRolesSection.addExpansionListener(new ExpansionAdapter() {
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
 		fRolesSection.setLayout(new GridLayout(1, false));
 
 		final Composite rolesSectionClient = aWidgetFactory.createComposite(fRolesSection);
 		rolesSectionClient.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
 		rolesSectionClient.setLayout(new GridLayout(4, false));
 		fRolesSection.setClient(rolesSectionClient);
 
 		//Roles
 		final CLabel rolesLabel = aWidgetFactory.createCLabel(rolesSectionClient, R4EUIConstants.ROLES_LABEL);
 		gridData = new GridData(GridData.FILL, GridData.FILL, false, false);
 		gridData.horizontalSpan = 1;
 		rolesLabel.setToolTipText(R4EUIConstants.PARTICIPANT_ROLES_TOOLTIP);
 		rolesLabel.setLayoutData(gridData);
 
 		gridData = new GridData(GridData.FILL, GridData.FILL, true, true);
 		gridData.horizontalSpan = 3;
 		fRolesList = new EditableListWidget(aWidgetFactory, rolesSectionClient, gridData, this, 2, CCombo.class,
				R4EUIConstants.PARTICIPANT_ROLES.toArray(new String[R4EUIConstants.PARTICIPANT_ROLES.size()]));
 		fRolesList.setToolTipText(R4EUIConstants.PARTICIPANT_ROLES_TOOLTIP);
 
 		//Time Management section
 		fTimeSection = aWidgetFactory.createExpandableComposite(aComposite, ExpandableComposite.TWISTIE);
 		data = new FormData();
 		data.left = new FormAttachment(0, 0);
 		data.right = new FormAttachment(100, 0); // $codepro.audit.disable numericLiterals
 		data.top = new FormAttachment(fRolesSection, ITabbedPropertyConstants.VSPACE);
 		data.bottom = new FormAttachment(100, 0); // $codepro.audit.disable numericLiterals
 		fTimeSection.setLayoutData(data);
 		fTimeSection.setText(TIME_SECTION_LABEL);
 		fTimeSection.addExpansionListener(new ExpansionAdapter() {
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
 		fTimeSection.setLayout(new GridLayout(1, false));
 
 		final Composite timeSectionClient = aWidgetFactory.createComposite(fTimeSection);
 		timeSectionClient.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
 		timeSectionClient.setLayout(new GridLayout(4, false));
 		fTimeSection.setClient(timeSectionClient);
 
 		//Time Spent (detailed)
 		final CLabel timeSpentDetailedLabel = aWidgetFactory.createCLabel(timeSectionClient,
 				R4EUIConstants.TIME_SPENT_LABEL);
 		gridData = new GridData(GridData.FILL, GridData.FILL, false, false);
 		gridData.horizontalSpan = 1;
 		timeSpentDetailedLabel.setToolTipText(R4EUIConstants.PARTICIPANT_TIME_SPENT_TOOLTIP);
 		timeSpentDetailedLabel.setLayoutData(gridData);
 
 		gridData = new GridData(GridData.FILL, GridData.FILL, true, true);
 		gridData.horizontalSpan = 3;
 		fTimeSpentDetailedList = new EditableListWidget(aWidgetFactory, timeSectionClient, gridData, this, 1,
 				Date.class, null);
 		fTimeSpentDetailedList.setToolTipText(R4EUIConstants.PARTICIPANT_TIME_SPENT_TOOLTIP);
 	}
 
 	/**
 	 * Method refresh.
 	 * 
 	 * @see org.eclipse.ui.views.properties.tabbed.ISection#refresh()
 	 */
 	@Override
 	public void refresh() {
 		fRefreshInProgress = true;
 		final R4EParticipant modelUser = ((R4EUIParticipant) fProperties.getElement()).getParticipant();
 		fIdText.setText(modelUser.getId());
 
 		String email = modelUser.getEmail();
 		if (null != email) {
 			fEmailText.setText(email);
 		} else {
 			fEmailText.setText("");
 		}
 
 		fNumItemsText.setText(String.valueOf(modelUser.getAddedItems().size()));
 
 		int numAnomalies = 0;
 		int numComments = 0;
 		final EList<R4EComment> comments = modelUser.getAddedComments();
 		final int commentsSize = comments.size();
 		for (int i = 0; i < commentsSize; i++) {
 			if (comments.get(i) instanceof R4EAnomaly) {
 				++numAnomalies;
 			} else {
 				++numComments;
 			}
 		}
 		fNumAnomaliesText.setText(String.valueOf(numAnomalies));
 		fNumCommentsText.setText(String.valueOf(numComments));
 		final String details = ((R4EUIParticipant) fProperties.getElement()).getParticipantDetails();
 		if (null != details) {
 			fDetailsText.setText(details);
 		} else {
 			fDetailsText.setText("");
 		}
 
 		final int numTimeEntries = modelUser.getTimeLog().size();
 		fTimeSpentDetailedList.removeAll();
 		int totalTimeSpent = 0;
 		Item item = null;
 		Entry<Date, Integer> timeEntry = null;
 
 		final DateFormat dateFormat = new SimpleDateFormat(R4EUIConstants.DEFAULT_DATE_FORMAT);
 
 		for (int i = 0; i < numTimeEntries; i++) {
 			timeEntry = modelUser.getTimeLog().get(i);
 			if (i >= fTimeSpentDetailedList.getItemCount()) {
 				item = fTimeSpentDetailedList.addItem();
 			} else {
 				item = fTimeSpentDetailedList.getItem(i);
 				if (null == item) {
 					item = fTimeSpentDetailedList.addItem();
 				}
 			}
 			String[] data = { timeEntry.getValue().toString(), dateFormat.format(timeEntry.getKey()) };
 			((TableItem) item).setText(data);
 			totalTimeSpent += timeEntry.getValue().intValue();
 		}
 		fTimeSpentDetailedList.setTableHeader(0, "Time Spent: " + Integer.toString(totalTimeSpent) + " minutes");
 		fTimeSpentDetailedList.updateButtons();
 
 		final String[] roles = ((R4EUIParticipant) fProperties.getElement()).getRoles(modelUser.getRoles());
 		fRolesList.removeAll();
 		String role = null;
 		for (int i = 0; i < roles.length; i++) {
 			role = roles[i];
 			if (i >= fRolesList.getItemCount()) {
 				item = fRolesList.addItem();
 			} else {
 				item = fRolesList.getItem(i);
 				if (null == item) {
 					item = fRolesList.addItem();
 				}
 			}
 			item.setText(role);
 		}
 		fRolesList.updateButtons();
 
 		String focusArea = modelUser.getFocusArea();
 		if (null != focusArea) {
 			fFocusAreaText.setText(focusArea);
 		} else {
 			fFocusAreaText.setText("");
 		}
 
 		setEnabledFields();
 		fRefreshInProgress = false;
 	}
 
 	/**
 	 * Method setEnabledFields.
 	 */
 	@Override
 	protected void setEnabledFields() {
 		if (R4EUIModelController.isJobInProgress()
 				|| fProperties.getElement().isReadOnly()
 				|| null == R4EUIModelController.getActiveReview()
 				|| ((R4EReviewState) R4EUIModelController.getActiveReview().getReview().getState()).getState().equals(
 						R4EReviewPhase.R4E_REVIEW_PHASE_COMPLETED) || !fProperties.getElement().isEnabled()) {
 			fIdText.setForeground(UIUtils.DISABLED_FONT_COLOR);
 			fEmailText.setForeground(UIUtils.DISABLED_FONT_COLOR);
 			fEmailText.setEditable(false);
 			fNumItemsText.setForeground(UIUtils.DISABLED_FONT_COLOR);
 			fNumAnomaliesText.setForeground(UIUtils.DISABLED_FONT_COLOR);
 			fNumCommentsText.setForeground(UIUtils.DISABLED_FONT_COLOR);
 			fDetailsText.setForeground(UIUtils.DISABLED_FONT_COLOR);
 			fTimeSpentDetailedList.setEnabled(false);
 			fRolesList.setEnabled(false);
 			fFocusAreaText.setForeground(UIUtils.DISABLED_FONT_COLOR);
 			fFocusAreaText.setEditable(false);
 		} else {
 			fIdText.setForeground(UIUtils.ENABLED_FONT_COLOR);
 			fEmailText.setForeground(UIUtils.ENABLED_FONT_COLOR);
 			fEmailText.setForeground(UIUtils.ENABLED_FONT_COLOR);
 			fNumItemsText.setForeground(UIUtils.ENABLED_FONT_COLOR);
 			fNumAnomaliesText.setForeground(UIUtils.ENABLED_FONT_COLOR);
 			fNumCommentsText.setForeground(UIUtils.ENABLED_FONT_COLOR);
 			fDetailsText.setForeground(UIUtils.ENABLED_FONT_COLOR);
 
 			if (R4EUIModelController.getActiveReview() instanceof R4EUIReviewExtended) {
 				final R4EUIReviewExtended uiReview = (R4EUIReviewExtended) R4EUIModelController.getActiveReview();
 				fTimeSection.setVisible(true);
 				fRolesSection.setVisible(true);
 
 				if (uiReview.isParticipantTimeSpentEnabled()) {
 					fTimeSpentDetailedList.setEnabled(true);
 				} else {
 					fTimeSpentDetailedList.setEnabled(false);
 				}
 
 				if (uiReview.isParticipantExtraDetailsEnabled()) {
 					fRolesList.setEnabled(true);
 					fFocusAreaText.setForeground(UIUtils.ENABLED_FONT_COLOR);
 					fFocusAreaText.setEditable(true);
 				} else {
 					fRolesList.setEnabled(false);
 					fFocusAreaText.setForeground(UIUtils.DISABLED_FONT_COLOR);
 					fFocusAreaText.setEditable(false);
 				}
 			} else {
 				if (R4EUIModelController.getActiveReview()
 						.getReview()
 						.getType()
 						.equals(R4EReviewType.R4E_REVIEW_TYPE_BASIC)) {
 					fTimeSection.setVisible(false);
 					fRolesSection.setVisible(false);
 				} else {
 					fTimeSection.setVisible(true);
 					fRolesSection.setVisible(true);
 					fTimeSpentDetailedList.setEnabled(true);
 					fRolesList.setEnabled(true);
 				}
 				fFocusAreaText.setForeground(UIUtils.ENABLED_FONT_COLOR);
 				fFocusAreaText.setEditable(true);
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
 			final R4EParticipant modelParticipant = ((R4EUIParticipant) fProperties.getElement()).getParticipant();
 			final String currentUser = R4EUIModelController.getReviewer();
 
 			if (1 == aInstanceId) {
 				//Update spent time
 				final EMap<Date, Integer> timeMap = modelParticipant.getTimeLog();
 				final DateFormat dateFormat = new SimpleDateFormat(R4EUIConstants.DEFAULT_DATE_FORMAT);
 				Map<Date, Integer> newAddTimes = new HashMap<Date, Integer>();
 				Map<Date, Integer> newDeleteTimes = new HashMap<Date, Integer>();
 				Map<Date, Integer> storedTimes = new HashMap<Date, Integer>();
 				storedTimes.putAll(modelParticipant.getTimeLog().map());
 				for (Item item : aItems) {
 					try {
 						newAddTimes.put(dateFormat.parse(((TableItem) item).getText(1)),
 								Integer.valueOf(((TableItem) item).getText(0)));
 						newDeleteTimes.put(dateFormat.parse(((TableItem) item).getText(1)),
 								Integer.valueOf(((TableItem) item).getText(0)));
 					} catch (ParseException e) {
 						//just ignore this entry
 						continue;
 					}
 				}
 
 				//Add all new elements
 				Set<Date> storedKeys = storedTimes.keySet();
 				for (Date storedKey : storedKeys) {
 					newAddTimes.remove(storedKey);
 				}
 				if (newAddTimes.size() > 0) {
 					Long bookNum = R4EUIModelController.FResourceUpdater.checkOut(modelParticipant, currentUser);
 					timeMap.putAll(newAddTimes);
 					R4EUIModelController.FResourceUpdater.checkIn(bookNum);
 				}
 
 				//Delete old elements to remove
 				Set<Date> deleteKeys = newDeleteTimes.keySet();
 				for (Date deleteKey : deleteKeys) {
 					storedTimes.remove(deleteKey);
 				}
 				if (storedTimes.size() > 0) {
 					Long bookNum = R4EUIModelController.FResourceUpdater.checkOut(modelParticipant, currentUser);
 					for (Date storedKey : storedTimes.keySet()) {
 						timeMap.remove(storedKey);
 					}
 					R4EUIModelController.FResourceUpdater.checkIn(bookNum);
 				}
 			} else if (2 == aInstanceId) {
 				//Update roles
 				List<R4EUserRole> newAddUserRoles = new ArrayList<R4EUserRole>();
 				List<R4EUserRole> newDeleteUserRoles = new ArrayList<R4EUserRole>();
 				List<R4EUserRole> storedUserRoles = new ArrayList<R4EUserRole>();
 				storedUserRoles.addAll(modelParticipant.getRoles());
 				for (Item item : aItems) {
 					R4EUserRole role = R4EUIParticipant.mapStringToRole(item.getText());
 					if (null != role) {
 						newAddUserRoles.add(role);
 						newDeleteUserRoles.add(role);
 					}
 				}
 
 				//Add all new elements
 				newAddUserRoles.removeAll(storedUserRoles);
 				if (newAddUserRoles.size() > 0) {
 					Long bookNum = R4EUIModelController.FResourceUpdater.checkOut(modelParticipant, currentUser);
 					modelParticipant.getRoles().addAll(newAddUserRoles);
 					R4EUIModelController.FResourceUpdater.checkIn(bookNum);
 				}
 
 				//Delete old elements to remove
 				storedUserRoles.removeAll(newDeleteUserRoles);
 				if (storedUserRoles.size() > 0) {
 					Long bookNum = R4EUIModelController.FResourceUpdater.checkOut(modelParticipant, currentUser);
 					modelParticipant.getRoles().removeAll(storedUserRoles);
 					R4EUIModelController.FResourceUpdater.checkIn(bookNum);
 				}
 
 				((R4EUIParticipant) fProperties.getElement()).setImage(((R4EUIParticipant) fProperties.getElement()).getImageLocation());
 				R4EUIModelController.getNavigatorView().getTreeViewer().refresh();
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
