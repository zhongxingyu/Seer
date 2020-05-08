 /*******************************************************************************
  * Copyright (c) 2012 Ericsson AB and others.
  * 
  * All rights reserved. This program and the accompanying materials are
  * made available under the terms of the Eclipse Public License v1.0 which
  * accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Description:
  * 
  * This class implements the dialog used to clone Anomalies
  * This is a modeless-like dialog
  * 
  * Contributors:
  *   Sebastien Dubois - Created for Mylyn Review R4E project
  *   
  ******************************************************************************/
 
 package org.eclipse.mylyn.reviews.r4e.ui.internal.dialogs;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.eclipse.jface.dialogs.IDialogConstants;
 import org.eclipse.jface.viewers.ArrayContentProvider;
 import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.ViewerCell;
 import org.eclipse.jface.window.ToolTip;
 import org.eclipse.jface.window.Window;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4EAnomaly;
 import org.eclipse.mylyn.reviews.r4e.core.model.R4ECommentType;
 import org.eclipse.mylyn.reviews.r4e.core.model.drules.R4EDesignRule;
 import org.eclipse.mylyn.reviews.r4e.core.model.drules.R4EDesignRuleClass;
 import org.eclipse.mylyn.reviews.r4e.core.model.drules.R4EDesignRuleRank;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.IR4EUIModelElement;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIAnomalyBasic;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIFileContext;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIModelController;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIReviewBasic;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIReviewItem;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.navigator.ReviewNavigatorLabelProvider;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.utils.R4EUIConstants;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.utils.UIUtils;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.CCombo;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
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
 
 public class CloneAnomalyInputDialog extends FormDialog implements IAnomalyInputDialog {
 
 	// ------------------------------------------------------------------------
 	// Constants
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Field CLONE_ANOMALY_DIALOG_TITLE. (value is ""Select Anomaly to Clone"")
 	 */
 	private static final String CLONE_ANOMALY_DIALOG_TITLE = "Select Anomaly to Clone";
 
 	/**
 	 * Field ANOMALY_LIST_HEADER_MSG. (value is ""Available Cloneable Anomalies"")
 	 */
 	private static final String ANOMALY_LIST_HEADER_MSG = "Available Cloneable Anomalies";
 
 	/**
 	 * Field ANOMALY_DETAILS_HEADER_MSG. (value is ""Anomaly Details"")
 	 */
 	private static final String ANOMALY_DETAILS_HEADER_MSG = "Anomaly Details";
 
 	/**
 	 * Field MAX_DISPLAYED_CLONEABLE_ANOMALIES. (value is "10")
 	 */
 	private static final int MAX_DISPLAYED_CLONEABLE_ANOMALIES = 10;
 
 	// ------------------------------------------------------------------------
 	// Member variables
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Field fCloneableAnomalyViewer.
 	 */
 	private TableViewer fCloneableAnomalyViewer = null;
 
 	/**
 	 * Field fAnomalyClass.
 	 */
 	private R4EUIAnomalyBasic fClonedAnomaly = null;
 
 	/**
 	 * Field fAnomalyTitleTextField.
 	 */
 	protected Text fAnomalyTitleTextField = null;
 
 	/**
 	 * Field fAnomalyDescriptionTextField.
 	 */
 	protected Text fAnomalyDescriptionTextField;
 
 	/**
 	 * Field fAnomalyClassTextField.
 	 */
 	protected Text fAnomalyClassTextField = null;
 
 	/**
 	 * Field fAnomalyRankTextField.
 	 */
 	protected Text fAnomalyRankTextField = null;
 
 	/**
 	 * Field fDateText.
 	 */
 	protected Text fDateText = null;
 
 	/**
 	 * Field fAnomalyDueDateValue.
 	 */
 	private Date fAnomalyDueDateValue = null;
 
 	/**
 	 * Field fRuleIdTextField.
 	 */
 	private Text fRuleIdTextField = null;
 
 	/**
 	 * Field fAssignedToCombo.
 	 */
 	protected CCombo fAssignedToCombo = null;
 
 	/**
 	 * Field fAssignedToParticipant.
 	 */
 	private String fAssignedToParticipant = null;
 
 	// ------------------------------------------------------------------------
 	// Constructors
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Constructor for R4EAnomalyInputDialog.
 	 * 
 	 * @param aParentShell
 	 *            Shell
 	 */
 	public CloneAnomalyInputDialog(Shell aParentShell) {
 		super(aParentShell);
 		setBlockOnOpen(false);
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
 			ISelection selection = fCloneableAnomalyViewer.getSelection();
 			if (selection instanceof IStructuredSelection) {
 				if (null != ((IStructuredSelection) selection).getFirstElement()) {
 					if (((IStructuredSelection) selection).getFirstElement() instanceof R4EUIAnomalyBasic) {
 						fClonedAnomaly = (R4EUIAnomalyBasic) ((IStructuredSelection) selection).getFirstElement();
 						fAssignedToParticipant = fAssignedToCombo.getText();
 					}
 				}
 			}
 		} else {
 			fClonedAnomaly = null;
 			fAnomalyDueDateValue = null;
 			fAssignedToParticipant = null;
 		}
 		R4EUIModelController.getNavigatorView().getTreeViewer().refresh();
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
 		shell.setText(CLONE_ANOMALY_DIALOG_TITLE);
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
 		basicSection.setText(ANOMALY_LIST_HEADER_MSG);
 		basicSection.addExpansionListener(new ExpansionAdapter() {
 			@Override
 			public void expansionStateChanged(ExpansionEvent e) {
 				getShell().setSize(getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT));
 			}
 		});
 
 		final Composite basicSectionClient = toolkit.createComposite(basicSection);
 		basicSectionClient.setLayout(layout);
 		basicSection.setClient(basicSectionClient);
 
 		//Cloneable Anomaly Table
 		Set<R4EUIAnomalyBasic> anomalies = getCloneableAnomalies();
 
 		int tableHeight = MAX_DISPLAYED_CLONEABLE_ANOMALIES;
 		if (anomalies.size() < MAX_DISPLAYED_CLONEABLE_ANOMALIES) {
 			tableHeight = anomalies.size();
 		}
 		textGridData = new GridData(GridData.FILL, GridData.FILL, true, true);
 
 		fCloneableAnomalyViewer = new TableViewer(basicSectionClient, SWT.FULL_SELECTION | SWT.BORDER | SWT.READ_ONLY
 				| SWT.H_SCROLL | SWT.V_SCROLL);
 		textGridData.heightHint = fCloneableAnomalyViewer.getTable().getItemHeight() * tableHeight;
 		fCloneableAnomalyViewer.getControl().setLayoutData(textGridData);
 		fCloneableAnomalyViewer.setContentProvider(ArrayContentProvider.getInstance());
 		ColumnViewerToolTipSupport.enableFor(fCloneableAnomalyViewer, ToolTip.NO_RECREATE);
 		fCloneableAnomalyViewer.setLabelProvider(new ReviewNavigatorLabelProvider() {
 			@Override
 			public String getText(Object element) {
 				return ((R4EUIAnomalyBasic) element).getAnomaly().getTitle();
 			}
 
 			@Override
 			public void update(ViewerCell cell) {
 				cell.setText(((R4EUIAnomalyBasic) cell.getElement()).getAnomaly().getTitle());
 				cell.setImage(((R4EUIAnomalyBasic) cell.getElement()).getImage(((R4EUIAnomalyBasic) cell.getElement()).getImageLocation()));
 			}
 		});
 
 		fCloneableAnomalyViewer.setInput(anomalies);
 
 		fCloneableAnomalyViewer.addSelectionChangedListener(new ISelectionChangedListener() {
 			public void selectionChanged(SelectionChangedEvent event) {
 				//Check and set files based on selected cloneable anomaly
 				getButton(IDialogConstants.OK_ID).setEnabled(false);
 				if (event.getSelection() instanceof IStructuredSelection) {
 					if (null != ((IStructuredSelection) event.getSelection()).getFirstElement()) {
 						Object selectedObject = ((IStructuredSelection) event.getSelection()).getFirstElement();
 						if (selectedObject instanceof R4EUIAnomalyBasic) {
 							final R4EUIAnomalyBasic uiAnomaly = (R4EUIAnomalyBasic) selectedObject;
 							R4EAnomaly anomaly = uiAnomaly.getAnomaly();
 							if (null != anomaly) {
 								if (null != anomaly.getTitle()) {
 									fAnomalyTitleTextField.setText(anomaly.getTitle());
 								}
 								if (null != anomaly.getDescription()) {
 									fAnomalyDescriptionTextField.setText(anomaly.getDescription());
 								}
 								if (null != anomaly.getType()) {
 									fAnomalyClassTextField.setText(UIUtils.getClassStr(((R4ECommentType) anomaly.getType()).getType()));
 								}
 								if (null != anomaly.getRank()) {
 									fAnomalyRankTextField.setText(UIUtils.getRankStr(anomaly.getRank()));
 								}
 								if (null != anomaly.getRule() && null != anomaly.getRule().getId()) {
 									fRuleIdTextField.setText(anomaly.getRule().getId());
 								}
 							}
 							getButton(IDialogConstants.OK_ID).setEnabled(true);
 						}
 					}
 				}
 			}
 		});
 
 		//Extra parameters section
 		final Section extraSection = toolkit.createSection(composite, Section.DESCRIPTION
 				| ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE | ExpandableComposite.EXPANDED);
 		final GridData extraSectionGridData = new GridData(GridData.FILL, GridData.FILL, true, true);
 		extraSectionGridData.horizontalSpan = 4;
 		extraSection.setLayoutData(extraSectionGridData);
 		extraSection.setText(ANOMALY_DETAILS_HEADER_MSG);
 		extraSection.addExpansionListener(new ExpansionAdapter() {
 			@Override
 			public void expansionStateChanged(ExpansionEvent e) {
 				getShell().setSize(getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT));
 			}
 		});
 
 		final Composite extraSectionClient = toolkit.createComposite(extraSection);
 		extraSectionClient.setLayout(layout);
 		extraSection.setClient(extraSectionClient);
 		toolkit.setBorderStyle(SWT.NULL);
 
 		//Anomaly Title
 		Label label = toolkit.createLabel(extraSectionClient, R4EUIConstants.ANOMALY_TITLE_LABEL_VALUE);
 		label.setToolTipText(R4EUIConstants.ANOMALY_TITLE_TOOLTIP);
 		label.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false));
 		fAnomalyTitleTextField = toolkit.createText(extraSectionClient, "", SWT.SINGLE);
 		textGridData = new GridData(GridData.FILL, GridData.FILL, true, false);
 		textGridData.horizontalSpan = 3;
 		fAnomalyTitleTextField.setToolTipText(R4EUIConstants.ANOMALY_TITLE_TOOLTIP);
 		fAnomalyTitleTextField.setLayoutData(textGridData);
 		fAnomalyTitleTextField.setEditable(false);
 
 		//Anomaly Description
 		label = toolkit.createLabel(extraSectionClient, R4EUIConstants.ANOMALY_DESCRIPTION_LABEL_VALUE);
 		label.setToolTipText(R4EUIConstants.ANOMALY_DESCRIPTION_TOOLTIP);
 		label.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false));
		fAnomalyDescriptionTextField = toolkit.createText(extraSectionClient, "", SWT.MULTI | SWT.H_SCROLL
				| SWT.V_SCROLL);
 		textGridData = new GridData(GridData.FILL, GridData.FILL, true, false);
 		textGridData.horizontalSpan = 3;
 		textGridData.heightHint = fAnomalyTitleTextField.getLineHeight() * 7;
 		fAnomalyDescriptionTextField.setToolTipText(R4EUIConstants.ANOMALY_DESCRIPTION_TOOLTIP);
 		fAnomalyDescriptionTextField.setLayoutData(textGridData);
 		fAnomalyDescriptionTextField.setEditable(false);
 
 		//Anomaly Class
 		label = toolkit.createLabel(extraSectionClient, R4EUIConstants.CLASS_LABEL);
 		label.setToolTipText(R4EUIConstants.ANOMALY_CLASS_TOOLTIP);
 		label.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false));
 
 		fAnomalyClassTextField = toolkit.createText(extraSectionClient, "", SWT.SINGLE);
 		textGridData = new GridData(GridData.FILL, GridData.FILL, true, false);
 		textGridData.horizontalSpan = 3;
 		fAnomalyClassTextField.setToolTipText(R4EUIConstants.ANOMALY_CLASS_TOOLTIP);
 		fAnomalyClassTextField.setLayoutData(textGridData);
 		fAnomalyClassTextField.setEditable(false);
 
 		//Anomaly Rank 	
 		label = toolkit.createLabel(extraSectionClient, R4EUIConstants.RANK_LABEL);
 		label.setToolTipText(R4EUIConstants.ANOMALY_RANK_TOOLTIP);
 		label.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false));
 
 		fAnomalyRankTextField = toolkit.createText(extraSectionClient, "", SWT.SINGLE);
 		textGridData = new GridData(GridData.FILL, GridData.FILL, true, false);
 		textGridData.horizontalSpan = 3;
 		fAnomalyRankTextField.setToolTipText(R4EUIConstants.ANOMALY_CLASS_TOOLTIP);
 		fAnomalyRankTextField.setLayoutData(textGridData);
 		fAnomalyRankTextField.setEditable(false);
 
 		//Rule ID
 		label = toolkit.createLabel(extraSectionClient, R4EUIConstants.RULE_ID_LABEL);
 		label.setToolTipText(R4EUIConstants.RULE_ID_TOOLTIP);
 		label.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false));
 
 		fRuleIdTextField = toolkit.createText(extraSectionClient, "", SWT.SINGLE);
 		textGridData = new GridData(GridData.FILL, GridData.FILL, true, false);
 		textGridData.horizontalSpan = 3;
 		fRuleIdTextField.setToolTipText(R4EUIConstants.ANOMALY_CLASS_TOOLTIP);
 		fRuleIdTextField.setLayoutData(textGridData);
 		fRuleIdTextField.setEditable(false);
 
 		toolkit.setBorderStyle(SWT.BORDER);
 
 		//Assigned To
 		label = toolkit.createLabel(extraSectionClient, R4EUIConstants.ASSIGNED_TO_LABEL);
 		textGridData = new GridData(GridData.FILL, GridData.FILL, false, false);
 		textGridData.horizontalSpan = 1;
 		label.setLayoutData(textGridData);
 
 		fAssignedToCombo = new CCombo(extraSectionClient, SWT.BORDER | SWT.READ_ONLY);
 		final String[] participants = R4EUIModelController.getActiveReview()
 				.getParticipantIDs()
 				.toArray(new String[R4EUIModelController.getActiveReview().getParticipantIDs().size()]);
 		fAssignedToCombo.removeAll();
 		fAssignedToCombo.add("");
 		for (String participant : participants) {
 			fAssignedToCombo.add(participant);
 		}
 		textGridData = new GridData(GridData.FILL, GridData.FILL, true, false);
 		textGridData.horizontalSpan = 3;
 		fAssignedToCombo.setToolTipText(R4EUIConstants.ASSIGNED_TO_TOOLTIP);
 		fAssignedToCombo.setLayoutData(textGridData);
 
 		//Due Date
 		label = toolkit.createLabel(extraSectionClient, R4EUIConstants.DUE_DATE_LABEL);
 		textGridData = new GridData(GridData.FILL, GridData.FILL, false, false);
 		textGridData.horizontalSpan = 1;
 		label.setLayoutData(textGridData);
 
 		final Composite dateComposite = toolkit.createComposite(extraSectionClient);
 		textGridData = new GridData(GridData.FILL, GridData.FILL, true, true);
 		textGridData.horizontalSpan = 3;
 		dateComposite.setToolTipText(R4EUIConstants.ANOMALY_DUE_DATE_TOOLTIP);
 		dateComposite.setLayoutData(textGridData);
 		dateComposite.setLayout(new GridLayout(2, false));
 
 		fDateText = toolkit.createText(dateComposite, "", SWT.BORDER | SWT.READ_ONLY);
 		fDateText.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));
 		fDateText.setEditable(false);
 
 		final Button calendarButton = toolkit.createButton(dateComposite, "...", SWT.NONE);
 		calendarButton.setLayoutData(new GridData(GridData.FILL, GridData.FILL, false, false));
 		calendarButton.addSelectionListener(new SelectionListener() {
 			public void widgetSelected(SelectionEvent e) {
 				final ICalendarDialog dialog = R4EUIDialogFactory.getInstance().getCalendarDialog();
 				final int result = dialog.open();
 				if (result == Window.OK) {
 					final SimpleDateFormat dateFormat = new SimpleDateFormat(R4EUIConstants.SIMPLE_DATE_FORMAT);
 					fDateText.setText(dateFormat.format(dialog.getDate()));
 					fAnomalyDueDateValue = dialog.getDate();
 				}
 			}
 
 			public void widgetDefaultSelected(SelectionEvent e) { // $codepro.audit.disable emptyMethod
 				// No implementation needed
 			}
 		});
 
 	}
 
 	/**
 	 * Get All cloneable anomalies for the parent review.
 	 * 
 	 * @return R4EUIAnomalyBasic[]
 	 */
 	private Set<R4EUIAnomalyBasic> getCloneableAnomalies() {
 		Set<R4EUIAnomalyBasic> cloneableAnomalies = new HashSet<R4EUIAnomalyBasic>();
 		R4EUIReviewBasic parentReview = R4EUIModelController.getActiveReview();
 		for (R4EUIReviewItem items : parentReview.getReviewItems()) {
 			if (items.getItem().isEnabled()) {
 				for (R4EUIFileContext file : items.getFileContexts()) {
 					if (file.getFileContext().isEnabled()) {
 						for (IR4EUIModelElement anomaly : file.getAnomalyContainerElement().getChildren()) {
 							if (((R4EUIAnomalyBasic) anomaly).getAnomaly().isEnabled()) {
 								boolean isDuplicate = false;
 								for (R4EUIAnomalyBasic oldAnomaly : cloneableAnomalies) {
 									if (oldAnomaly.isSameAs((R4EUIAnomalyBasic) anomaly)) {
 										isDuplicate = true;
 										break;
 									}
 								}
 								if (!isDuplicate) {
 									cloneableAnomalies.add((R4EUIAnomalyBasic) anomaly);
 								}
 							}
 						}
 					}
 				}
 			}
 		}
 		return cloneableAnomalies;
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
 	 * @return the anomaly title input string
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.dialogs.IAnomalyInputDialog#getAnomalyTitleValue()
 	 */
 	public String getAnomalyTitleValue() {
 		String title = null;
 		if (null != fClonedAnomaly && null != fClonedAnomaly.getAnomaly()) {
 			title = fClonedAnomaly.getAnomaly().getTitle();
 		}
 		return title;
 	}
 
 	/**
 	 * Returns the string typed into this input dialog.
 	 * 
 	 * @return the anomaly description input string
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.dialogs.IAnomalyInputDialog#getAnomalyDescriptionValue()
 	 */
 	public String getAnomalyDescriptionValue() {
 		String description = null;
 		if (null != fClonedAnomaly && null != fClonedAnomaly.getAnomaly()) {
 			description = fClonedAnomaly.getAnomaly().getDescription();
 		}
 		return description;
 	}
 
 	/**
 	 * Returns the string typed into this input dialog.
 	 * 
 	 * @return the R4EUIRule reference (if any)
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.dialogs.IAnomalyInputDialog#getRuleReferenceValue()
 	 */
 	public R4EDesignRule getRuleReferenceValue() {
 		R4EDesignRule rule = null;
 		if (null != fClonedAnomaly && null != fClonedAnomaly.getAnomaly()) {
 			rule = fClonedAnomaly.getAnomaly().getRule();
 		}
 		return rule;
 	}
 
 	/**
 	 * Method setClass_.
 	 * 
 	 * @param aClass
 	 *            R4EDesignRuleClass
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.dialogs.IAnomalyInputDialog#setClass_(R4EDesignRuleClass)
 	 */
 	public void setClass_(R4EDesignRuleClass aClass) {
 		if (null != aClass) {
 			fAnomalyClassTextField.setText(UIUtils.getClassStr(aClass));
 		}
 	}
 
 	/**
 	 * Method getClass_.
 	 * 
 	 * @return R4EDesignRuleClass
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.dialogs.IAnomalyInputDialog#getClass_()
 	 */
 	public R4EDesignRuleClass getClass_() {
 		R4EDesignRuleClass class_ = null;
 		if (null != fClonedAnomaly && null != fClonedAnomaly.getAnomaly()
 				&& null != fClonedAnomaly.getAnomaly().getType()) {
 			class_ = ((R4ECommentType) fClonedAnomaly.getAnomaly().getType()).getType();
 		}
 		return class_;
 	}
 
 	/**
 	 * Method setRank.
 	 * 
 	 * @param aRank
 	 *            R4EDesignRuleRank
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.dialogs.IAnomalyInputDialog#setRank(R4EDesignRuleRank)
 	 */
 	public void setRank(R4EDesignRuleRank aRank) {
 		if (null != aRank) {
 			fAnomalyRankTextField.setText(UIUtils.getRankStr(aRank));
 		}
 	}
 
 	/**
 	 * Method getRank.
 	 * 
 	 * @return R4EDesignRuleRank
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.dialogs.IAnomalyInputDialog#getRank()
 	 */
 	public R4EDesignRuleRank getRank() {
 		R4EDesignRuleRank rank = null;
 		if (null != fClonedAnomaly && null != fClonedAnomaly.getAnomaly()) {
 			rank = fClonedAnomaly.getAnomaly().getRank();
 		}
 		return rank;
 	}
 
 	/**
 	 * Method setDueDate.
 	 * 
 	 * @param aDate
 	 *            Date
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.dialogs.IAnomalyInputDialog#setDueDate(Date)
 	 */
 	public void setDueDate(Date aDate) {
 		final SimpleDateFormat dateFormat = new SimpleDateFormat(R4EUIConstants.SIMPLE_DATE_FORMAT);
 		if (null != aDate) {
 			fDateText.setText(dateFormat.format(aDate));
 		}
 	}
 
 	/**
 	 * Method getDueDate.
 	 * 
 	 * @return Date
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.dialogs.IAnomalyInputDialog#getDueDate()
 	 */
 	public Date getDueDate() {
 		if (null != fAnomalyDueDateValue) {
 			return new Date(fAnomalyDueDateValue.getTime());
 		}
 		return null;
 	}
 
 	/**
 	 * Method setTitle.
 	 * 
 	 * @param aTitle
 	 *            String
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.dialogs.IAnomalyInputDialog#setTitle(String)
 	 */
 	public void setTitle(String aTitle) {
 		fAnomalyTitleTextField.setText(aTitle);
 	}
 
 	/**
 	 * Method setDescription.
 	 * 
 	 * @param aDescription
 	 *            String
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.dialogs.IAnomalyInputDialog#setDescription(String)
 	 */
 	public void setDescription(String aDescription) {
 		fAnomalyDescriptionTextField.setText(aDescription);
 	}
 
 	/**
 	 * Method setRuleID.
 	 * 
 	 * @param aId
 	 *            String
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.dialogs.IAnomalyInputDialog#setRuleID(String)
 	 */
 	public void setRuleID(String aId) {
 		fRuleIdTextField.setText(aId);
 	}
 
 	/**
 	 * Method getAssignedParticipant.
 	 * 
 	 * @return String
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.dialogs.IAnomalyInputDialog#getAssigned()
 	 */
 	public String getAssigned() {
 		return fAssignedToParticipant;
 	}
 
 	/**
 	 * Method setAssigned.
 	 * 
 	 * @param aParticipant
 	 *            - String
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.dialogs.IAnomalyInputDialog#setAssigned(String)
 	 */
 	public void setAssigned(String aParticipant) {
 		fAssignedToCombo.setText(aParticipant);
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
 		super.setShellStyle(newstyle);
 	}
 
 	/**
 	 * Method open.
 	 * 
 	 * @return int
 	 * @see org.eclipse.mylyn.reviews.r4e.ui.internal.dialogs.IAnomalyInputDialog#open()
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
