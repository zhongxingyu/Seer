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
 
 package org.eclipse.mylyn.reviews.r4e.ui.properties.tabbed;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.atomic.AtomicReference;
 
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jface.dialogs.ErrorDialog;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EFormalReview;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EParticipant;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EReview;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EReviewPhase;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EReviewPhaseInfo;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EReviewState;
 import org.eclipse.mylyn.reviews.r4e.core.model.serial.impl.OutOfSyncException;
 import org.eclipse.mylyn.reviews.r4e.core.model.serial.impl.ResourceHandlingException;
 import org.eclipse.mylyn.reviews.r4e.ui.Activator;
 import org.eclipse.mylyn.reviews.r4e.ui.model.R4EUIModelController;
 import org.eclipse.mylyn.reviews.r4e.ui.model.R4EUIReviewBasic;
 import org.eclipse.mylyn.reviews.r4e.ui.model.R4EUIReviewExtended;
 import org.eclipse.mylyn.reviews.r4e.ui.utils.MailServicesProxy;
 import org.eclipse.mylyn.reviews.r4e.ui.utils.R4EUIConstants;
 import org.eclipse.mylyn.reviews.r4e.ui.utils.UIUtils;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.CCombo;
 import org.eclipse.swt.custom.CLabel;
 import org.eclipse.swt.custom.TableEditor;
 import org.eclipse.swt.events.FocusEvent;
 import org.eclipse.swt.events.FocusListener;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.layout.FormAttachment;
 import org.eclipse.swt.layout.FormData;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.swt.widgets.TableItem;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
 import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
 import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetWidgetFactory;
 
 /**
  * @author lmcdubo
  * @version $Revision: 1.0 $
  */
 public class ReviewBasicTabPropertySection extends ModelElementTabPropertySection {
 	
 	// ------------------------------------------------------------------------
 	// Member variables
 	// ------------------------------------------------------------------------
 	
 	/**
 	 * Field fNameText.
 	 */
 	private CLabel fNameText = null;
 	
 	/**
 	 * Field fPhaseCombo.
 	 */
 	protected CCombo fPhaseCombo = null;
 	
 	/**
 	 * Field fStartDateText.
 	 */
 	private CLabel fStartDateText = null;
 	
 	/**
 	 * Field fEndDateText.
 	 */
 	private CLabel fEndDateText = null;
 	
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
 
 	
 	// ------------------------------------------------------------------------
 	// Methods
 	// ------------------------------------------------------------------------
 	
 	/**
 	 * Method shouldUseExtraSpace.
 	 * @return boolean
 	 * @see org.eclipse.ui.views.properties.tabbed.ISection#shouldUseExtraSpace()
 	 */
 	@Override
 	public boolean shouldUseExtraSpace() {
 		return true;
 	}
 	
 	/**
 	 * Method createControls.
 	 * @param parent Composite
 	 * @param aTabbedPropertySheetPage TabbedPropertySheetPage
 	 * @see org.eclipse.ui.views.properties.tabbed.ISection#createControls(Composite, TabbedPropertySheetPage)
 	 */
 	@Override
 	public void createControls(Composite parent, TabbedPropertySheetPage aTabbedPropertySheetPage) {
 		super.createControls(parent, aTabbedPropertySheetPage);
 
 		//Tell element to build its own detailed tab layout
 		final TabbedPropertySheetWidgetFactory widgetFactory = aTabbedPropertySheetPage.getWidgetFactory();
 		final Composite mainForm = widgetFactory.createFlatFormComposite(parent);
 	    FormData data = null;
 	   
 	    //Review Name (read-only for now)
 	    fNameText = widgetFactory.createCLabel(mainForm, "");
 	    data = new FormData();
 	    data.left = new FormAttachment(0, R4EUIConstants.TABBED_PROPERTY_LABEL_WIDTH);
 	    data.right = new FormAttachment(100, 0); // $codepro.audit.disable numericLiterals
 	    data.top = new FormAttachment(0, ITabbedPropertyConstants.VSPACE);
 	    fNameText.setLayoutData(data);
 		
 	    final CLabel nameLabel = widgetFactory.createCLabel(mainForm, R4EUIConstants.NAME_LABEL);
 	    data = new FormData();
 	    data.left = new FormAttachment(0, 0);
 	    data.right = new FormAttachment(fNameText, -ITabbedPropertyConstants.HSPACE);
 	    data.top = new FormAttachment(fNameText, 0, SWT.TOP);
 	    nameLabel.setLayoutData(data);
 	    
     	//Phase
 	    fPhaseCombo = widgetFactory.createCCombo(mainForm, SWT.READ_ONLY);
 	    data = new FormData();
 	    data.left = new FormAttachment(0, R4EUIConstants.TABBED_PROPERTY_LABEL_WIDTH);
 	    data.right = new FormAttachment(100, 0); // $codepro.audit.disable numericLiterals
 	    data.top = new FormAttachment(fNameText, ITabbedPropertyConstants.VSPACE);
 	    fPhaseCombo.setLayoutData(data);
 	    fPhaseCombo.addSelectionListener(new SelectionListener() {
 	    	public void widgetSelected(SelectionEvent e) {
 				final AtomicReference<String> aResultMsg = new AtomicReference<String>(null);
 				R4EReviewPhase phase = null;
 				if (fProperties.getElement() instanceof R4EUIReviewExtended) {
 					phase = ((R4EUIReviewExtended)fProperties.getElement()).getPhaseFromString(fPhaseCombo.getText());
 				} else {
 					phase = ((R4EUIReviewBasic)fProperties.getElement()).getPhaseFromString(fPhaseCombo.getText());
 				}
 				if (((R4EUIReviewBasic)fProperties.getElement()).validatePhaseChange(phase, aResultMsg)) {
 	    			if (!fRefreshInProgress) {
 	    				if (null != aResultMsg.get()) {
 	    					final ErrorDialog dialog = new ErrorDialog(null, "Warning", aResultMsg.get(),
 				    			new Status(IStatus.WARNING, Activator.PLUGIN_ID, 0, null, null), IStatus.WARNING);
 	    					dialog.open();
 	    				}
 	    				try {
 	    					if (fProperties.getElement() instanceof R4EUIReviewExtended) {
 	    						((R4EUIReviewExtended)fProperties.getElement()).updatePhase(phase);
 	    						if (((R4EFormalReview)((R4EUIReviewExtended)fProperties.getElement()).getReview()).
 	    								getCurrent().getType().equals(R4EReviewPhase.R4E_REVIEW_PHASE_PREPARATION)) {
 	    							
     					    		MailServicesProxy.sendMeetingRequest();
 	    						}
 	    					} else {
 	    						((R4EUIReviewBasic)fProperties.getElement()).updatePhase(phase);
 	    					}
 	    				} catch (ResourceHandlingException e1) {
 	    					UIUtils.displayResourceErrorDialog(e1);
 	    				} catch (OutOfSyncException e1) {
 	    					UIUtils.displaySyncErrorDialog(e1);
 						} finally {
 							R4EUIModelController.setDialogOpen(false);
 						}
 	    			}
 	    			refresh();
 	    		} else {
 	    			final ErrorDialog dialog = new ErrorDialog(null, R4EUIConstants.REVIEW_NOT_COMPLETED_ERROR, "Review cannot be set to completed",
 			    			new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, aResultMsg.get(), null), IStatus.ERROR);
 			    	dialog.open();
 	    		}
 	    	}
 			public void widgetDefaultSelected(SelectionEvent e) { // $codepro.audit.disable emptyMethod
 				//No implementation needed
 			}
 		});
 	    
 	    final CLabel phaseLabel = widgetFactory.createCLabel(mainForm, R4EUIConstants.PHASE_LABEL);
 	    data = new FormData();
 	    data.left = new FormAttachment(0, 0);
 	    data.right = new FormAttachment(fPhaseCombo, -ITabbedPropertyConstants.HSPACE);
 	    data.top = new FormAttachment(fPhaseCombo, 0, SWT.CENTER);
 	    phaseLabel.setLayoutData(data);
 	    
 	    //Review Start Date (read-only)
 	    fStartDateText = widgetFactory.createCLabel(mainForm, "");
 	    data = new FormData();
 	    data.left = new FormAttachment(0, R4EUIConstants.TABBED_PROPERTY_LABEL_WIDTH);
 	    data.right = new FormAttachment(100, 0); // $codepro.audit.disable numericLiterals
 	    data.top = new FormAttachment(fPhaseCombo, ITabbedPropertyConstants.VSPACE);
 	    fStartDateText.setLayoutData(data);
 
 	    final CLabel creationDateLabel = widgetFactory.createCLabel(mainForm, R4EUIConstants.START_DATE_LABEL);
 	    data = new FormData();
 	    data.left = new FormAttachment(0, 0);
 	    data.right = new FormAttachment(fStartDateText, -ITabbedPropertyConstants.HSPACE);
 	    data.top = new FormAttachment(fStartDateText, 0, SWT.TOP);
 	    creationDateLabel.setLayoutData(data);
 	
 	    //End Date (read-only)
 	    fEndDateText = widgetFactory.createCLabel(mainForm, "");
 	    data = new FormData();
 	    data.left = new FormAttachment(0, R4EUIConstants.TABBED_PROPERTY_LABEL_WIDTH);
 	    data.right = new FormAttachment(100, 0); // $codepro.audit.disable numericLiterals
 	    data.top = new FormAttachment(fStartDateText, ITabbedPropertyConstants.VSPACE);
 	    fEndDateText.setLayoutData(data);
 
 	    final CLabel endDateLabel = widgetFactory.createCLabel(mainForm, R4EUIConstants.END_DATE_LABEL);
 	    data = new FormData();
 	    data.left = new FormAttachment(0, 0);
 	    data.right = new FormAttachment(fEndDateText, -ITabbedPropertyConstants.HSPACE);
 	    data.top = new FormAttachment(fEndDateText, 0, SWT.TOP);
 	    endDateLabel.setLayoutData(data);
 	    
 	    //Review Description
 	    fDescriptionText = widgetFactory.createText(mainForm, "", SWT.MULTI);
 	    data = new FormData();
 	    data.left = new FormAttachment(0, R4EUIConstants.TABBED_PROPERTY_LABEL_WIDTH);
 	    data.right = new FormAttachment(100, 0); // $codepro.audit.disable numericLiterals
 	    data.top = new FormAttachment(fEndDateText, ITabbedPropertyConstants.VSPACE);
 	    fDescriptionText.setLayoutData(data);
 	    fDescriptionText.addFocusListener(new FocusListener() {		
 			public void focusLost(FocusEvent e) {
 	    		if (!fRefreshInProgress) {
 	    			try {
 	    				final String currentUser = R4EUIModelController.getReviewer();
 						final R4EReview modelReview = ((R4EUIReviewBasic)fProperties.getElement()).getReview();
 	    				final Long bookNum = R4EUIModelController.FResourceUpdater.checkOut(modelReview, currentUser);
 	    				modelReview.setExtraNotes(fDescriptionText.getText());
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
 	    UIUtils.addTabbedPropertiesTextResizeListener(fDescriptionText);
 
 	    final CLabel descriptionLabel = widgetFactory.createCLabel(mainForm, R4EUIConstants.DESCRIPTION_LABEL);
 	    data = new FormData();
 	    data.left = new FormAttachment(0, 0);
 	    data.right = new FormAttachment(fDescriptionText, -ITabbedPropertyConstants.HSPACE);
 	    data.top = new FormAttachment(fDescriptionText, 0, SWT.TOP);
 	    descriptionLabel.setLayoutData(data);
 	    
 	    //Review Phase Table (formal reviews only)
 	    fPhaseTable = widgetFactory.createTable(mainForm, SWT.HIDE_SELECTION);
 	    data = new FormData();
 	    data.left = new FormAttachment(0, R4EUIConstants.TABBED_PROPERTY_LABEL_WIDTH);
 	    data.right = new FormAttachment(100, 0); // $codepro.audit.disable numericLiterals
 	    data.top = new FormAttachment(fDescriptionText, ITabbedPropertyConstants.VSPACE);
 	    fPhaseTable.setHeaderVisible(true);
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
 		
 	    fPhaseMapLabel = widgetFactory.createCLabel(mainForm, R4EUIConstants.PHASE_MAP_LABEL);
 	    data = new FormData();
 	    data.left = new FormAttachment(0, 0);
 	    data.right = new FormAttachment(fPhaseTable, -ITabbedPropertyConstants.HSPACE);
 	    data.top = new FormAttachment(fPhaseTable, 0, SWT.TOP);
 	    fPhaseMapLabel.setLayoutData(data);
 	    
 	    //Add Control for planning phase owner
 	    fPlanningPhaseOwnerCombo = new CCombo(fPhaseTable, SWT.BORDER | SWT.READ_ONLY);
 		fPlanningPhaseOwnerCombo.addFocusListener(new FocusListener() {
 			public void focusGained(FocusEvent e) {
 				//Nothing to do
 			}
 
 			public void focusLost(FocusEvent e) {
 	    		if (!fRefreshInProgress) {
 	    			try {
 	    				final String currentUser = R4EUIModelController.getReviewer();
 	    				final R4EReview modelReview = ((R4EUIReviewExtended)fProperties.getElement()).getReview();
 	    				final Long bookNum = R4EUIModelController.FResourceUpdater.checkOut(modelReview, currentUser);
 	    				((R4EFormalReview)modelReview).getCurrent().setPhaseOwnerID(
 	    						(fPlanningPhaseOwnerCombo).getText());
 	    				R4EUIModelController.FResourceUpdater.checkIn(bookNum);
 	    			} catch (ResourceHandlingException e1) {
 	    				UIUtils.displayResourceErrorDialog(e1);
 	    			} catch (OutOfSyncException e1) {
 	    				UIUtils.displaySyncErrorDialog(e1);
 	    			}
 	    		}
 	    		refresh();
 			}
 		});
 		final TableEditor planningEditor = new TableEditor(fPhaseTable);
 		planningEditor.grabHorizontal = true;
 		planningEditor.grabVertical = true;
 		planningEditor.setEditor(fPlanningPhaseOwnerCombo, fPhasePlanning, 1);
 		
 	    //Add Controls for preparation phase owner
 	    fPreparationPhaseOwnerCombo = new CCombo(fPhaseTable, SWT.BORDER | SWT.READ_ONLY);
 		fPreparationPhaseOwnerCombo.addFocusListener(new FocusListener() {
 			public void focusGained(FocusEvent e) {
 				//Nothing to do
 			}
 
 			public void focusLost(FocusEvent e) {
 	    		if (!fRefreshInProgress) {
 	    			try {
 	    				final String currentUser = R4EUIModelController.getReviewer();
 	    				final R4EReview modelReview = ((R4EUIReviewExtended)fProperties.getElement()).getReview();
 	    				final Long bookNum = R4EUIModelController.FResourceUpdater.checkOut(modelReview, currentUser);
 	    				((R4EFormalReview)modelReview).getCurrent().setPhaseOwnerID(
 	    						(fPreparationPhaseOwnerCombo).getText());
 	    				R4EUIModelController.FResourceUpdater.checkIn(bookNum);
 	    			} catch (ResourceHandlingException e1) {
 	    				UIUtils.displayResourceErrorDialog(e1);
 	    			} catch (OutOfSyncException e1) {
 	    				UIUtils.displaySyncErrorDialog(e1);
 	    			}
 	    		}
 	    		refresh();
 			}
 		});
 		final TableEditor preparationEditor = new TableEditor(fPhaseTable);
 		preparationEditor.grabHorizontal = true;
 		preparationEditor.grabVertical = true;
 		preparationEditor.setEditor(fPreparationPhaseOwnerCombo, fPhasePreparation, 1);
 		
 	    //Add Controls for decision phase owner
 	    fDecisionPhaseOwnerCombo = new CCombo(fPhaseTable, SWT.BORDER | SWT.READ_ONLY);
 	    fDecisionPhaseOwnerCombo.addFocusListener(new FocusListener() {
 			public void focusGained(FocusEvent e) {
 				//Nothing to do
 			}
 
 			public void focusLost(FocusEvent e) {
 	    		if (!fRefreshInProgress) {
 	    			try {
 	    				final String currentUser = R4EUIModelController.getReviewer();
 	    				final R4EReview modelReview = ((R4EUIReviewExtended)fProperties.getElement()).getReview();
 	    				final Long bookNum = R4EUIModelController.FResourceUpdater.checkOut(modelReview, currentUser);
 	    				((R4EFormalReview)modelReview).getCurrent().setPhaseOwnerID(
 	    						(fDecisionPhaseOwnerCombo).getText());
 	    				R4EUIModelController.FResourceUpdater.checkIn(bookNum);
 	    			} catch (ResourceHandlingException e1) {
 	    				UIUtils.displayResourceErrorDialog(e1);
 	    			} catch (OutOfSyncException e1) {
 	    				UIUtils.displaySyncErrorDialog(e1);
 	    			}
 	    		}
 	    		refresh();
 			}
 		});
 		final TableEditor decisionEditor = new TableEditor(fPhaseTable);
 		decisionEditor.grabHorizontal = true;
 		decisionEditor.grabVertical = true;
 		decisionEditor.setEditor(fDecisionPhaseOwnerCombo, fPhaseDecision, 1);
 		
 	    //Add Controls for rework phase owner
 	    fReworkPhaseOwnerCombo = new CCombo(fPhaseTable, SWT.BORDER | SWT.READ_ONLY);
 	    fReworkPhaseOwnerCombo.addFocusListener(new FocusListener() {
 			public void focusGained(FocusEvent e) {
 				//Nothing to do
 			}
 
 			public void focusLost(FocusEvent e) {
 	    		if (!fRefreshInProgress) {
 	    			try {
 	    				final String currentUser = R4EUIModelController.getReviewer();
 	    				final R4EReview modelReview = ((R4EUIReviewExtended)fProperties.getElement()).getReview();
 	    				final Long bookNum = R4EUIModelController.FResourceUpdater.checkOut(modelReview, currentUser);
 	    				((R4EFormalReview)modelReview).getCurrent().setPhaseOwnerID(
 	    						(fReworkPhaseOwnerCombo).getText());
 	    				R4EUIModelController.FResourceUpdater.checkIn(bookNum);
 	    			} catch (ResourceHandlingException e1) {
 	    				UIUtils.displayResourceErrorDialog(e1);
 	    			} catch (OutOfSyncException e1) {
 	    				UIUtils.displaySyncErrorDialog(e1);
 	    			}
 	    		}
 	    		refresh();
 			}
 		});
 		final TableEditor reworkEditor = new TableEditor(fPhaseTable);
 		reworkEditor.grabHorizontal = true;
 		reworkEditor.grabVertical = true;
 		reworkEditor.setEditor(fReworkPhaseOwnerCombo, fPhaseRework, 1);
 	}
 	
 	/**
 	 * Method refresh.
 	 * @see org.eclipse.ui.views.properties.tabbed.ISection#refresh()
 	 */
 	@Override
 	public void refresh() {
 		fRefreshInProgress = true;
 		final DateFormat dateFormat = new SimpleDateFormat(R4EUIConstants.DEFAULT_DATE_FORMAT);
 		if (fProperties.getElement() instanceof R4EUIReviewExtended) {
 			final R4EUIReviewExtended uiReview = (R4EUIReviewExtended)fProperties.getElement();
 			final R4EFormalReview modelReview = (R4EFormalReview) uiReview.getReview();
 			fPhaseCombo.setItems(uiReview.getAvailablePhases());
 			fPhaseCombo.select(uiReview.mapPhaseToIndex(((R4EReviewState)modelReview.getState()).getState()));
 			fNameText.setText(modelReview.getName());
 			fStartDateText.setText(dateFormat.format(modelReview.getStartDate()));
 			if (null == modelReview.getEndDate()) {
 				fEndDateText.setText("(In Progress)");
 			} else {
 				fEndDateText.setText(dateFormat.format(modelReview.getEndDate()));
 			}
 			fDescriptionText.setText(modelReview.getExtraNotes());
 			final int columnWidth = fPhaseTable.getClientArea().width/fPhaseTable.getColumnCount();
 			fColumnPhase.setWidth(columnWidth);
 			fColumnOwner.setWidth(columnWidth);
 			fColumnStartDate.setWidth(columnWidth);
 			fColumnEndDate.setWidth(columnWidth);
 			
 	    	final List<R4EParticipant> participants = ((R4EUIReviewBasic)fProperties.getElement()).getParticipants();
 	    	final List<String> participantsList = new ArrayList<String>();
 	    	for (R4EParticipant participant : participants) {
 	    		participantsList.add(participant.getId());
 	    	}
 			final String[] participantsStr = participantsList.toArray(new String[participantsList.size()]);
 			
 			R4EReviewPhaseInfo phaseInfo = uiReview.getPhaseInfo(R4EReviewPhase.R4E_REVIEW_PHASE_STARTED);
 			final R4EReviewPhaseInfo currentPhaseInfo = modelReview.getCurrent();
 			if (null != phaseInfo) {
 				fPhasePlanning.setText(1, phaseInfo.getPhaseOwnerID());
 				fPhasePlanning.setText(2, (null != phaseInfo.getStartDate()) ? dateFormat.format(phaseInfo.getStartDate()) : "");
 				fPhasePlanning.setText(3, (null != phaseInfo.getEndDate()) ? dateFormat.format(phaseInfo.getEndDate()) : "");
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
 					fPlanningPhaseOwnerCombo.select(UIUtils.mapParticipantToIndex((modelReview).getCurrent().getPhaseOwnerID()));
 				}
 			}
 			phaseInfo = uiReview.getPhaseInfo(R4EReviewPhase.R4E_REVIEW_PHASE_PREPARATION);
 			if (null != phaseInfo) {
 				fPhasePreparation.setText(1, phaseInfo.getPhaseOwnerID());
 				fPhasePreparation.setText(2, (null != phaseInfo.getStartDate()) ? dateFormat.format(phaseInfo.getStartDate()) : "");
 				fPhasePreparation.setText(3, (null != phaseInfo.getEndDate()) ? dateFormat.format(phaseInfo.getEndDate()) : "");
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
 					fPreparationPhaseOwnerCombo.select(UIUtils.mapParticipantToIndex((modelReview).getCurrent().getPhaseOwnerID()));
 				}
 			}
 			phaseInfo = uiReview.getPhaseInfo(R4EReviewPhase.R4E_REVIEW_PHASE_DECISION);
 			if (null != phaseInfo) {
 				fPhaseDecision.setText(1, phaseInfo.getPhaseOwnerID());
 				fPhaseDecision.setText(2, (null != phaseInfo.getStartDate()) ? dateFormat.format(phaseInfo.getStartDate()) : "");
 				fPhaseDecision.setText(3, (null != phaseInfo.getEndDate()) ? dateFormat.format(phaseInfo.getEndDate()) : "");
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
 					fDecisionPhaseOwnerCombo.select(UIUtils.mapParticipantToIndex((modelReview).getCurrent().getPhaseOwnerID()));
 				}
 			}
 			phaseInfo = uiReview.getPhaseInfo(R4EReviewPhase.R4E_REVIEW_PHASE_REWORK);
 			if (null != phaseInfo) {
 				fPhaseRework.setText(1, phaseInfo.getPhaseOwnerID());
 				fPhaseRework.setText(2, (null != phaseInfo.getStartDate()) ? dateFormat.format(phaseInfo.getStartDate()) : "");
 				fPhaseRework.setText(3, (null != phaseInfo.getEndDate()) ? dateFormat.format(phaseInfo.getEndDate()) : "");
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
 					fReworkPhaseOwnerCombo.select(UIUtils.mapParticipantToIndex((modelReview).getCurrent().getPhaseOwnerID()));
 				}
 			}
 			phaseInfo = uiReview.getPhaseInfo(R4EReviewPhase.R4E_REVIEW_PHASE_COMPLETED);
 			if (null != phaseInfo) {
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
 			final R4EUIReviewBasic uiReview = (R4EUIReviewBasic)fProperties.getElement();
 			final R4EReview modelReview = uiReview.getReview();
 			fPhaseCombo.setItems(uiReview.getAvailablePhases());
 			fPhaseCombo.select(uiReview.mapPhaseToIndex(((R4EReviewState)modelReview.getState()).getState()));
 			fNameText.setText(modelReview.getName());
 			fStartDateText.setText(dateFormat.format(modelReview.getStartDate()));
 			if (null == modelReview.getEndDate()) {
 				fEndDateText.setText("(In Progress)");
 			} else {
 				fEndDateText.setText(dateFormat.format(modelReview.getEndDate()));
 			}
 			fDescriptionText.setText(modelReview.getExtraNotes());
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
 		if (R4EUIModelController.isDialogOpen() || (!((R4EUIReviewBasic)fProperties.getElement()).isOpen())) {
 			fNameText.setEnabled(false);
 			fPhaseCombo.setEnabled(false);
 			fDescriptionText.setEnabled(false);
 			fStartDateText.setEnabled(false);
 			fEndDateText.setEnabled(false);
 			
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
 				setPhaseControlVisibility();	
 			} else {
 				fPhaseTable.setVisible(false);
 				fPhaseMapLabel.setVisible(false);
 				fPlanningPhaseOwnerCombo.setVisible(false);
 				fPreparationPhaseOwnerCombo.setVisible(false);
 				fDecisionPhaseOwnerCombo.setVisible(false);
 				fReworkPhaseOwnerCombo.setVisible(false);
 			}
 		} else {
 			fNameText.setEnabled(true);
 			fPhaseCombo.setEnabled(true);
 			fStartDateText.setEnabled(true);
 			fEndDateText.setEnabled(true);
 			fDescriptionText.setEnabled(true);
 			if (fProperties.getElement() instanceof R4EUIReviewExtended) {
 				fPhaseTable.setEnabled(true);
 				fPlanningPhaseOwnerCombo.setEnabled(true);
 				fPreparationPhaseOwnerCombo.setEnabled(true);
 				fDecisionPhaseOwnerCombo.setEnabled(true);
 				fReworkPhaseOwnerCombo.setEnabled(true);
 				fPhaseTable.setVisible(true);
 				fPhaseMapLabel.setVisible(true);
 				setPhaseControlVisibility();
 			} else {
 				fPhaseTable.setVisible(false);
 				fPhaseMapLabel.setVisible(false);
 				fPlanningPhaseOwnerCombo.setVisible(false);
 				fPreparationPhaseOwnerCombo.setVisible(false);
 				fDecisionPhaseOwnerCombo.setVisible(false);
 				fReworkPhaseOwnerCombo.setVisible(false);
 			}
 		}
 	}
 	
 	/**
 	 * Method setPhaseControlVisibility.
 	 */
 	private void setPhaseControlVisibility() {
 		final R4EReviewPhase currentPhase = ((R4EFormalReview)((R4EUIReviewExtended)fProperties.getElement()).
 				getReview()).getCurrent().getType();
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
 }
