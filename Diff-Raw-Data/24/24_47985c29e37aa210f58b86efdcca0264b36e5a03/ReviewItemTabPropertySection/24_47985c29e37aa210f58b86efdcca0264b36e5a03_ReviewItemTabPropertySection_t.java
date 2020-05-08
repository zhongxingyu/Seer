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
 import java.text.SimpleDateFormat;
 
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EItem;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EParticipant;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EReviewPhase;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EReviewState;
 import org.eclipse.mylyn.reviews.r4e.core.model.serial.impl.OutOfSyncException;
 import org.eclipse.mylyn.reviews.r4e.core.model.serial.impl.ResourceHandlingException;
 import org.eclipse.mylyn.reviews.r4e.ui.R4EUIPlugin;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIModelController;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIReviewItem;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.utils.R4EUIConstants;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.utils.UIUtils;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.CLabel;
 import org.eclipse.swt.layout.FormAttachment;
 import org.eclipse.swt.layout.FormData;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.List;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
 import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
 import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetWidgetFactory;
 
 /**
  * This class implements the tabbed property section for the Review Item model element
  * 
  * @author Sebastien Dubois
  * @version $Revision: 1.0 $
  */
 public class ReviewItemTabPropertySection extends ModelElementTabPropertySection {
 
 	// ------------------------------------------------------------------------
 	// Member variables
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Field fAuthorText.
 	 */
 	private Text fAuthorText = null;
 
 	/**
 	 * Field fAuthorRepText.
 	 */
 	private Text fAuthorRepText = null;
 
 	/**
 	 * Field fProjectIdList.
 	 */
 	private List fProjectIdList = null;
 
 	/**
 	 * Field fRepositoryText.
 	 */
 	private Text fRepositoryText = null;
 
 	/**
 	 * Field fDateSubmitted.
 	 */
 	private Text fDateSubmitted = null;
 
 	/**
 	 * Field fDescriptionText.
 	 */
 	protected Text fDescriptionText = null;
 
 	/**
 	 * Field fAssignedToComposite.
 	 */
 	private Composite fAssignedToComposite;
 
 	/**
 	 * Field fAssignedToText.
 	 */
 	private Text fAssignedToText;
 
 	/**
 	 * Field fAssignedToButton.
 	 */
 	private Button fAssignedToButton;
 
 	/**
 	 * Field fUnassignedFromButton.
 	 */
 	private Button fUnassignedFromButton;
 
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
 
 		//Author (read-only)
 		widgetFactory.setBorderStyle(SWT.NULL);
 		fAuthorText = widgetFactory.createText(composite, "", SWT.NULL);
 		data = new FormData();
 		data.left = new FormAttachment(0, R4EUIConstants.TABBED_PROPERTY_LABEL_WIDTH);
 		data.right = new FormAttachment(100, 0); // $codepro.audit.disable numericLiterals
 		data.top = new FormAttachment(0, ITabbedPropertyConstants.VSPACE);
 		fAuthorText.setEditable(false);
 		fAuthorText.setToolTipText(R4EUIConstants.REVIEW_ITEM_AUTHOR_TOOLTIP);
 		fAuthorText.setLayoutData(data);
 
 		final CLabel authorLabel = widgetFactory.createCLabel(composite, R4EUIConstants.AUTHOR_LABEL);
 		data = new FormData();
 		data.left = new FormAttachment(0, 0);
 		data.right = new FormAttachment(fAuthorText, -ITabbedPropertyConstants.HSPACE);
 		data.top = new FormAttachment(fAuthorText, 0, SWT.CENTER);
 		authorLabel.setToolTipText(R4EUIConstants.REVIEW_ITEM_AUTHOR_TOOLTIP);
 		authorLabel.setLayoutData(data);
 
 		//AuthorRep (read-only)
 		fAuthorRepText = widgetFactory.createText(composite, "", SWT.NULL);
 		data = new FormData();
 		data.left = new FormAttachment(0, R4EUIConstants.TABBED_PROPERTY_LABEL_WIDTH);
 		data.right = new FormAttachment(100, 0); // $codepro.audit.disable numericLiterals
 		data.top = new FormAttachment(fAuthorText, ITabbedPropertyConstants.VSPACE);
 		fAuthorRepText.setEditable(false);
 		fAuthorRepText.setToolTipText(R4EUIConstants.REVIEW_ITEM_AUTHOR_REP_TOOLTIP);
 		fAuthorRepText.setLayoutData(data);
 
 		final CLabel authorRepLabel = widgetFactory.createCLabel(composite, R4EUIConstants.EMAIL_LABEL);
 		data = new FormData();
 		data.left = new FormAttachment(0, 0);
 		data.right = new FormAttachment(fAuthorRepText, -ITabbedPropertyConstants.HSPACE);
 		data.top = new FormAttachment(fAuthorRepText, 0, SWT.CENTER);
 		authorRepLabel.setToolTipText(R4EUIConstants.REVIEW_ITEM_AUTHOR_REP_TOOLTIP);
 		authorRepLabel.setLayoutData(data);
 
 		//ProjectId (read-only)
 		fProjectIdList = widgetFactory.createList(composite, SWT.READ_ONLY);
 		data = new FormData();
 		data.left = new FormAttachment(0, R4EUIConstants.TABBED_PROPERTY_LABEL_WIDTH);
 		data.right = new FormAttachment(100, 0); // $codepro.audit.disable numericLiterals
 		data.top = new FormAttachment(fAuthorRepText, ITabbedPropertyConstants.VSPACE);
 		fProjectIdList.setToolTipText(R4EUIConstants.REVIEW_ITEM_PROJECT_IDS_TOOLTIP);
 		fProjectIdList.setLayoutData(data);
 
 		final CLabel projectIdLabel = widgetFactory.createCLabel(composite, R4EUIConstants.PROJECT_LABEL);
 		data = new FormData();
 		data.left = new FormAttachment(0, 0);
 		data.right = new FormAttachment(fProjectIdList, -ITabbedPropertyConstants.HSPACE);
 		data.top = new FormAttachment(fProjectIdList, 0, SWT.CENTER);
 		projectIdLabel.setToolTipText(R4EUIConstants.REVIEW_ITEM_PROJECT_IDS_TOOLTIP);
 		projectIdLabel.setLayoutData(data);
 
 		//Change Id (read-only)
 		fRepositoryText = widgetFactory.createText(composite, "", SWT.NULL);
 		data = new FormData();
 		data.left = new FormAttachment(0, R4EUIConstants.TABBED_PROPERTY_LABEL_WIDTH);
 		data.right = new FormAttachment(100, 0); // $codepro.audit.disable numericLiterals
 		data.top = new FormAttachment(fProjectIdList, ITabbedPropertyConstants.VSPACE);
 		fRepositoryText.setEditable(false);
 		fRepositoryText.setToolTipText(R4EUIConstants.REVIEW_ITEM_CHANGE_ID_TOOLTIP);
 		fRepositoryText.setLayoutData(data);
 
 		final CLabel repositoryLabel = widgetFactory.createCLabel(composite, R4EUIConstants.CHANGE_ID_LABEL);
 		data = new FormData();
 		data.left = new FormAttachment(0, 0);
 		data.right = new FormAttachment(fRepositoryText, -ITabbedPropertyConstants.HSPACE);
 		data.top = new FormAttachment(fRepositoryText, 0, SWT.CENTER);
 		repositoryLabel.setToolTipText(R4EUIConstants.REVIEW_ITEM_CHANGE_ID_TOOLTIP);
 		repositoryLabel.setLayoutData(data);
 
 		//Date Submitted (read-only)
 		fDateSubmitted = widgetFactory.createText(composite, "", SWT.NULL);
 		data = new FormData();
 		data.left = new FormAttachment(0, R4EUIConstants.TABBED_PROPERTY_LABEL_WIDTH);
 		data.right = new FormAttachment(100, 0); // $codepro.audit.disable numericLiterals
 		data.top = new FormAttachment(fRepositoryText, ITabbedPropertyConstants.VSPACE);
 		fDateSubmitted.setEditable(false);
 		fDateSubmitted.setToolTipText(R4EUIConstants.REVIEW_ITEM_DATE_SUBMITTED_TOOLTIP);
 		fDateSubmitted.setLayoutData(data);
 
 		final CLabel dateSubmittedLabel = widgetFactory.createCLabel(composite, R4EUIConstants.DATE_SUBMITTED_LABEL);
 		data = new FormData();
 		data.left = new FormAttachment(0, 0);
 		data.right = new FormAttachment(fDateSubmitted, -ITabbedPropertyConstants.HSPACE);
 		data.top = new FormAttachment(fDateSubmitted, 0, SWT.CENTER);
 		dateSubmittedLabel.setToolTipText(R4EUIConstants.REVIEW_ITEM_DATE_SUBMITTED_TOOLTIP);
 		dateSubmittedLabel.setLayoutData(data);
 
 		//Description
 		widgetFactory.setBorderStyle(SWT.BORDER);
 		fDescriptionText = widgetFactory.createText(composite, "", SWT.MULTI);
 		data = new FormData();
 		data.left = new FormAttachment(0, R4EUIConstants.TABBED_PROPERTY_LABEL_WIDTH);
 		data.right = new FormAttachment(100, 0); // $codepro.audit.disable numericLiterals
 		data.top = new FormAttachment(fDateSubmitted, ITabbedPropertyConstants.VSPACE);
 		fDescriptionText.setToolTipText(R4EUIConstants.REVIEW_ITEM_DESCRIPTION_TOOLTIP);
 		fDescriptionText.setLayoutData(data);
 		fDescriptionText.addListener(SWT.FocusOut, new Listener() {
 			public void handleEvent(Event event) {
 				if (!fRefreshInProgress && fDescriptionText.getForeground().equals(UIUtils.ENABLED_FONT_COLOR)) {
 					try {
 						final String currentUser = R4EUIModelController.getReviewer();
 						final R4EItem modelItem = ((R4EUIReviewItem) fProperties.getElement()).getItem();
 						String newValue = fDescriptionText.getText().trim();
 						if (!newValue.equals(modelItem.getDescription())) {
 							final Long bookNum = R4EUIModelController.FResourceUpdater.checkOut(modelItem, currentUser);
 							modelItem.setDescription(newValue);
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
 
 		final CLabel descriptionLabel = widgetFactory.createCLabel(composite, R4EUIConstants.DESCRIPTION_LABEL);
 		data = new FormData();
 		data.left = new FormAttachment(0, 0);
 		data.right = new FormAttachment(fDescriptionText, -ITabbedPropertyConstants.HSPACE);
 		data.top = new FormAttachment(fDescriptionText, 0, SWT.CENTER);
 		descriptionLabel.setToolTipText(R4EUIConstants.REVIEW_ITEM_DESCRIPTION_TOOLTIP);
 		descriptionLabel.setLayoutData(data);
 
 		//Assigned To
 		fAssignedToComposite = widgetFactory.createComposite(composite);
 		data = new FormData();
 		data.left = new FormAttachment(0, R4EUIConstants.TABBED_PROPERTY_LABEL_WIDTH);
 		data.right = new FormAttachment(100, 0); // $codepro.audit.disable numericLiterals
 		data.top = new FormAttachment(fDescriptionText, ITabbedPropertyConstants.VSPACE);
 		fAssignedToComposite.setToolTipText(R4EUIConstants.ASSIGNED_TO_TOOLTIP);
 		fAssignedToComposite.setLayoutData(data);
 		fAssignedToComposite.setLayout(new GridLayout(3, false));
 
 		fAssignedToText = widgetFactory.createText(fAssignedToComposite, "");
 		fAssignedToText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
 		fAssignedToText.setEditable(false);
 		fAssignedToButton = widgetFactory.createButton(fAssignedToComposite, R4EUIConstants.ADD_LABEL, SWT.NONE);
 		fAssignedToButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
 		fAssignedToButton.addListener(SWT.Selection, new Listener() {
 			public void handleEvent(Event event) {
 				((R4EUIReviewItem) fProperties.getElement()).addAssignees(UIUtils.getAssignParticipants());
 				refresh();
 				R4EUIModelController.getNavigatorView().getTreeViewer().refresh();
 			}
 		});
 
 		fUnassignedFromButton = widgetFactory.createButton(fAssignedToComposite, R4EUIConstants.REMOVE_LABEL, SWT.NONE);
 		fUnassignedFromButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
 		fUnassignedFromButton.addListener(SWT.Selection, new Listener() {
 			public void handleEvent(Event aEvent) {
 				((R4EUIReviewItem) fProperties.getElement()).removeAssignees(UIUtils.getUnassignParticipants(fProperties.getElement()));
 				refresh();
 				R4EUIModelController.getNavigatorView().getTreeViewer().refresh();
 			}
 		});
 
 		final CLabel assignedToLabel = widgetFactory.createCLabel(composite, R4EUIConstants.ASSIGNED_TO_LABEL);
 		data = new FormData();
 		data.left = new FormAttachment(0, 0);
 		data.right = new FormAttachment(fAssignedToComposite, -ITabbedPropertyConstants.HSPACE);
 		data.top = new FormAttachment(fAssignedToComposite, 0, SWT.CENTER);
 		assignedToLabel.setToolTipText(R4EUIConstants.ASSIGNED_TO_TOOLTIP);
 		assignedToLabel.setLayoutData(data);
 	}
 
 	/**
 	 * Method refresh.
 	 * 
 	 * @see org.eclipse.ui.views.properties.tabbed.ISection#refresh()
 	 */
 	@Override
 	public void refresh() {
 		if (null == fProperties) {
 			return; //R4EUIPostponedContainer (subclass of R4EUIReviewItem) does not have any properties
 		}
 		fRefreshInProgress = true;
 		final R4EItem modelItem = ((R4EUIReviewItem) fProperties.getElement()).getItem();
 		fAuthorText.setText(modelItem.getAddedById());
 		if (null != modelItem.getAuthorRep()) {
 			fAuthorRepText.setText(modelItem.getAuthorRep());
 		} else {
 			try {
 				final R4EParticipant participant = R4EUIModelController.getActiveReview().getParticipant(
 						modelItem.getAddedById(), false);
 				if (null != participant && null != participant.getEmail()) {
 					fAuthorRepText.setText(participant.getEmail());
 				} else {
 					fAuthorRepText.setText("");
 				}
 			} catch (ResourceHandlingException e) {
 				R4EUIPlugin.Ftracer.traceWarning("Exception: " + e.toString() + " (" + e.getMessage() + ")");
 				fAuthorRepText.setText("");
 			}
 		}

		EList<String> projects = modelItem.getProjectURIs();
		fProjectIdList.setItems(projects.toArray(new String[projects.size()]));
 		fRepositoryText.setText(null != modelItem.getRepositoryRef() ? modelItem.getRepositoryRef() : "");
 		if (null != modelItem.getSubmitted()) {
 			final DateFormat dateFormat = new SimpleDateFormat(R4EUIConstants.DEFAULT_DATE_FORMAT);
 			fDateSubmitted.setText(dateFormat.format(modelItem.getSubmitted()));
 		} else {
 			fDateSubmitted.setText("");
 		}
 		if (null != modelItem.getDescription()) {
 			fDescriptionText.setText(modelItem.getDescription());
 		} else {
 			fDescriptionText.setText("");
 		}
 
 		final EList<String> assignedParticipants = modelItem.getAssignedTo();
 		fAssignedToText.setText(UIUtils.formatAssignedParticipants(assignedParticipants));
 
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
 			fAuthorText.setForeground(UIUtils.DISABLED_FONT_COLOR);
 			fAuthorRepText.setForeground(UIUtils.DISABLED_FONT_COLOR);
 			fRepositoryText.setForeground(UIUtils.DISABLED_FONT_COLOR);
 			fDateSubmitted.setForeground(UIUtils.DISABLED_FONT_COLOR);
 			fProjectIdList.setEnabled(false);
 			fDescriptionText.setForeground(UIUtils.DISABLED_FONT_COLOR);
 			fDescriptionText.setEditable(false);
 			fAssignedToText.setForeground(UIUtils.DISABLED_FONT_COLOR);
 			fAssignedToButton.setEnabled(false);
 			fUnassignedFromButton.setEnabled(false);
 		} else {
 			fAuthorText.setForeground(UIUtils.ENABLED_FONT_COLOR);
 			fAuthorRepText.setForeground(UIUtils.ENABLED_FONT_COLOR);
 			fRepositoryText.setForeground(UIUtils.ENABLED_FONT_COLOR);
 			fDateSubmitted.setForeground(UIUtils.ENABLED_FONT_COLOR);
 			fProjectIdList.setEnabled(true);
 			fDescriptionText.setForeground(UIUtils.ENABLED_FONT_COLOR);
 			fDescriptionText.setEditable(true);
 			fAssignedToText.setForeground(UIUtils.ENABLED_FONT_COLOR);
 			fAssignedToButton.setEnabled(true);
 			if (fAssignedToText.getText().length() > 0) {
 				fUnassignedFromButton.setEnabled(true);
 			} else {
 				fUnassignedFromButton.setEnabled(false);
 			}
 		}
 	}
 }
