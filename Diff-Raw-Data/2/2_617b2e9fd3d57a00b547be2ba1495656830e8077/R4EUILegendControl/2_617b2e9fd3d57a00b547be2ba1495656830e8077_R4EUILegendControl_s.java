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
  * This class implements a legend dialog.  Adapted for Mylyn Commons
  * 
  * Contributors:
  *   Sebastien Dubois - Created for Mylyn Review R4E project
  *   
  *******************************************************************************/
 
 package org.eclipse.mylyn.reviews.r4e.ui.internal.navigator;
 
 import org.eclipse.jface.window.Window;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIAnomalyBasic;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIAnomalyContainer;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIDelta;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIDeltaContainer;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIFileContext;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIModelElement;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIParticipant;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIParticipantContainer;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIPostponedAnomaly;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIPostponedContainer;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIPostponedFile;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIReviewBasic;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIReviewExtended;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIReviewGroup;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIReviewItem;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIRule;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIRuleArea;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIRuleSet;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUIRuleViolation;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUISelection;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.model.R4EUISelectionContainer;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.utils.R4EUIConstants;
 import org.eclipse.mylyn.reviews.r4e.ui.internal.utils.UIUtils;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.ui.ISharedImages;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.forms.events.HyperlinkEvent;
 import org.eclipse.ui.forms.events.IHyperlinkListener;
 import org.eclipse.ui.forms.widgets.ExpandableComposite;
 import org.eclipse.ui.forms.widgets.FormToolkit;
 import org.eclipse.ui.forms.widgets.Hyperlink;
 import org.eclipse.ui.forms.widgets.Section;
 import org.eclipse.ui.forms.widgets.TableWrapData;
 import org.eclipse.ui.forms.widgets.TableWrapLayout;
 
 /**
  * @author Sebastien Dubois
  * @version $Revision: 1.0 $
  */
 public class R4EUILegendControl extends Composite {
 
 	/**
 	 * Field toolkit.
 	 */
 	private final FormToolkit toolkit;
 
 	/**
 	 * Field window.
 	 */
 	private Window window = null;
 
 	/**
 	 * Constructor for R4EUILegendControl.
 	 * 
 	 * @param parent
 	 *            Composite
 	 * @param toolkit
 	 *            FormToolkit
 	 */
 	public R4EUILegendControl(Composite parent, FormToolkit toolkit) {
 		this(parent, toolkit, true, SWT.VERTICAL);
 	}
 
 	/**
 	 * Constructor for R4EUILegendControl.
 	 * 
 	 * @param parent
 	 *            Composite
 	 * @param toolkit
 	 *            FormToolkit
 	 * @param showConnectors
 	 *            boolean
 	 * @param style
 	 *            int
 	 */
 	public R4EUILegendControl(Composite parent, FormToolkit toolkit, boolean showConnectors, int style) {
 		super(parent, SWT.NONE);
 		this.toolkit = toolkit;
 		toolkit.adapt(this);
 
 		final TableWrapLayout layout = new TableWrapLayout();
 		layout.leftMargin = 0;
 		layout.rightMargin = 0;
 		layout.topMargin = 0;
 		layout.bottomMargin = 0;
 
 		if (style == SWT.DEFAULT) {
 			createContentsVertical(layout, showConnectors);
 		} else if (0 != (style & SWT.HORIZONTAL)) {
 			createContentsHorizontal(layout, showConnectors);
 		} else {
 			createContentsVertical(layout, showConnectors);
 		}
 
 		setLayout(layout);
 		setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.FILL_GRAB));
 	}
 
 	/**
 	 * Method setWindow.
 	 * 
 	 * @param window
 	 *            Window
 	 */
 	public void setWindow(Window window) {
 		this.window = window;
 	}
 
 	/**
 	 * Method close.
 	 * 
 	 * @return boolean
 	 */
 	public boolean close() {
 		if (null != window) {
 			return window.close();
 		} else {
 			return false;
 		}
 	}
 
 	/**
 	 * Method createContentsHorizontal.
 	 * 
 	 * @param layout
 	 *            TableWrapLayout
 	 * @param showConnectors
 	 *            boolean
 	 */
 	private void createContentsHorizontal(TableWrapLayout layout, boolean showConnectors) {
 		layout.numColumns = 2;
 		createNavigatorSection(this);
 		createCommandsSection(this);
 
 		final Composite subComp = toolkit.createComposite(this);
 		final TableWrapLayout subLayout = new TableWrapLayout();
 		subLayout.topMargin = 0;
 		subLayout.bottomMargin = 0;
 		subLayout.leftMargin = 0;
 		subLayout.rightMargin = 0;
 		subComp.setLayout(subLayout);
 		subComp.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.FILL_GRAB, 1, 2));
 		createLinkHelpSection(subComp);
 	}
 
 	/**
 	 * Method createContentsVertical.
 	 * 
 	 * @param layout
 	 *            TableWrapLayout
 	 * @param showConnectors
 	 *            boolean
 	 */
 	private void createContentsVertical(TableWrapLayout layout, boolean showConnectors) {
 		layout.numColumns = 1;
 		createNavigatorSection(this);
 		createCommandsSection(this);
 		createLinkHelpSection(this);
 	}
 
 	/**
 	 * Method createNavigatorSection.
 	 * 
 	 * @param parent
 	 *            Composite
 	 */
 	private void createNavigatorSection(Composite parent) {
 		final TableWrapLayout layout = new TableWrapLayout();
 		layout.numColumns = 2;
 		layout.makeColumnsEqualWidth = true;
 		layout.leftMargin = 0;
 		layout.rightMargin = 0;
 		layout.topMargin = 0;
 		layout.bottomMargin = 0;
 
 		final Composite composite = toolkit.createComposite(parent);
 		composite.setLayout(layout);
 		composite.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
 
 		createNavigatorElementsSection(composite);
 		createNavigatorDecoratorsSection(composite);
 	}
 
 	/**
 	 * Method createCommandsSection.
 	 * 
 	 * @param parent
 	 *            Composite
 	 */
 	private void createCommandsSection(Composite parent) {
 		final TableWrapLayout layout = new TableWrapLayout();
 		layout.numColumns = 2;
 		layout.makeColumnsEqualWidth = true;
 		layout.leftMargin = 0;
 		layout.rightMargin = 0;
 		layout.topMargin = 0;
 		layout.bottomMargin = 0;
 
 		final Composite composite = toolkit.createComposite(parent);
 		composite.setLayout(layout);
 		composite.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
 
 		createNavigatorViewCommandsSection(composite);
 		createContextCommandsSection(composite);
 	}
 
 	/**
 	 * Method createNavigatorElementsSection.
 	 * 
 	 * @param parent
 	 *            Composite
 	 */
 	private void createNavigatorElementsSection(Composite parent) {
 		final Section section = toolkit.createSection(parent, ExpandableComposite.TITLE_BAR);
 		section.setText("Navigator Tree Elements");
 		section.setLayout(new TableWrapLayout());
 		section.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
 
 		final TableWrapLayout layout = new TableWrapLayout();
 		layout.numColumns = 4;
 		layout.makeColumnsEqualWidth = false;
 		layout.verticalSpacing = 1;
 		layout.topMargin = 1;
 		layout.bottomMargin = 1;
 
 		final Composite r4eClient = toolkit.createComposite(section);
 		r4eClient.setLayout(layout);
 		r4eClient.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
 		section.setClient(r4eClient);
 
 		Label imageLabel;
 		imageLabel = toolkit.createLabel(r4eClient, ""); //$NON-NLS-1$
 		imageLabel.setImage(UIUtils.loadIcon(R4EUIReviewGroup.REVIEW_GROUP_ICON_FILE));
 		toolkit.createLabel(r4eClient, "Review Group (Open)");
 
 		imageLabel = toolkit.createLabel(r4eClient, ""); //$NON-NLS-1$
 		imageLabel.setImage(UIUtils.loadIcon(R4EUIReviewGroup.REVIEW_GROUP_CLOSED_ICON_FILE));
 		toolkit.createLabel(r4eClient, "Review Group (Closed)");
 
 		imageLabel = toolkit.createLabel(r4eClient, ""); //$NON-NLS-1$
 		imageLabel.setImage(UIUtils.loadIcon(R4EUIReviewBasic.REVIEW_BASIC_ICON_FILE));
 		toolkit.createLabel(r4eClient, "Basic Review (Open)");
 
 		imageLabel = toolkit.createLabel(r4eClient, ""); //$NON-NLS-1$
 		imageLabel.setImage(UIUtils.loadIcon(R4EUIReviewBasic.REVIEW_BASIC_CLOSED_ICON_FILE));
 		toolkit.createLabel(r4eClient, "Basic Review (Closed)");
 
 		imageLabel = toolkit.createLabel(r4eClient, ""); //$NON-NLS-1$
 		imageLabel.setImage(UIUtils.loadIcon(R4EUIReviewBasic.REVIEW_INFORMAL_ICON_FILE));
 		toolkit.createLabel(r4eClient, "Informal Review (Open)");
 
 		imageLabel = toolkit.createLabel(r4eClient, ""); //$NON-NLS-1$
 		imageLabel.setImage(UIUtils.loadIcon(R4EUIReviewBasic.REVIEW_INFORMAL_CLOSED_ICON_FILE));
 		toolkit.createLabel(r4eClient, "Informal Review (Closed)");
 
 		imageLabel = toolkit.createLabel(r4eClient, ""); //$NON-NLS-1$
 		imageLabel.setImage(UIUtils.loadIcon(R4EUIReviewExtended.REVIEW_FORMAL_ICON_FILE));
 		toolkit.createLabel(r4eClient, "Formal Review (Open)");
 
 		imageLabel = toolkit.createLabel(r4eClient, ""); //$NON-NLS-1$
 		imageLabel.setImage(UIUtils.loadIcon(R4EUIReviewExtended.REVIEW_FORMAL_CLOSED_ICON_FILE));
 		toolkit.createLabel(r4eClient, "Formal Review (Closed)");
 
 		imageLabel = toolkit.createLabel(r4eClient, ""); //$NON-NLS-1$
 		imageLabel.setImage(UIUtils.loadIcon(R4EUIReviewItem.REVIEW_ITEM_ICON_FILE));
 		toolkit.createLabel(r4eClient, "Review Item");
 
 		imageLabel = toolkit.createLabel(r4eClient, ""); //$NON-NLS-1$
 		imageLabel.setImage(UIUtils.loadIcon(R4EUIFileContext.FILE_CONTEXT_ICON_FILE));
 		toolkit.createLabel(r4eClient, "File Context");
 
 		imageLabel = toolkit.createLabel(r4eClient, ""); //$NON-NLS-1$
 		imageLabel.setImage(UIUtils.loadIcon(R4EUISelectionContainer.SELECTION_CONTAINER_ICON_FILE));
 		toolkit.createLabel(r4eClient, "Selections Container");
 
 		imageLabel = toolkit.createLabel(r4eClient, ""); //$NON-NLS-1$
 		imageLabel.setImage(UIUtils.loadIcon(R4EUISelection.SELECTION_ICON_FILE));
 		toolkit.createLabel(r4eClient, "Selection");
 
 		imageLabel = toolkit.createLabel(r4eClient, ""); //$NON-NLS-1$
 		imageLabel.setImage(UIUtils.loadIcon(R4EUIDeltaContainer.DELTA_CONTAINER_ICON_FILE));
 		toolkit.createLabel(r4eClient, "Deltas Container");
 
 		imageLabel = toolkit.createLabel(r4eClient, ""); //$NON-NLS-1$
 		imageLabel.setImage(UIUtils.loadIcon(R4EUIDelta.DELTA_ICON_FILE));
 		toolkit.createLabel(r4eClient, "Delta");
 
 		imageLabel = toolkit.createLabel(r4eClient, ""); //$NON-NLS-1$
 		imageLabel.setImage(UIUtils.loadIcon(R4EUIParticipantContainer.PARTICIPANT_CONTAINER_ICON_FILE));
 		toolkit.createLabel(r4eClient, "Participants Container");
 
 		imageLabel = toolkit.createLabel(r4eClient, ""); //$NON-NLS-1$
 		imageLabel.setImage(UIUtils.loadIcon(R4EUIParticipant.PARTICIPANT_ICON_FILE));
 		toolkit.createLabel(r4eClient, "Participant (No role)");
 
 		imageLabel = toolkit.createLabel(r4eClient, ""); //$NON-NLS-1$
 		imageLabel.setImage(UIUtils.loadIcon(R4EUIParticipant.PARTICIPANT_ORGANIZER_ICON_FILE));
 		toolkit.createLabel(r4eClient, "Participant (Organizer)");
 
 		imageLabel = toolkit.createLabel(r4eClient, ""); //$NON-NLS-1$
 		imageLabel.setImage(UIUtils.loadIcon(R4EUIParticipant.PARTICIPANT_LEAD_ICON_FILE));
 		toolkit.createLabel(r4eClient, "Participant (Lead)");
 
 		imageLabel = toolkit.createLabel(r4eClient, ""); //$NON-NLS-1$
 		imageLabel.setImage(UIUtils.loadIcon(R4EUIParticipant.PARTICIPANT_AUTHOR_ICON_FILE));
 		toolkit.createLabel(r4eClient, "Participant (Author)");
 
 		imageLabel = toolkit.createLabel(r4eClient, ""); //$NON-NLS-1$
 		imageLabel.setImage(UIUtils.loadIcon(R4EUIParticipant.PARTICIPANT_REVIEWER_ICON_FILE));
 		toolkit.createLabel(r4eClient, "Participant (Reviewer)");
 
 		imageLabel = toolkit.createLabel(r4eClient, ""); //$NON-NLS-1$
 		imageLabel.setImage(UIUtils.loadIcon(R4EUIAnomalyContainer.ANOMALY_CONTAINER_ICON_FILE));
 		toolkit.createLabel(r4eClient, "Anomalies Container");
 
 		imageLabel = toolkit.createLabel(r4eClient, ""); //$NON-NLS-1$
 		imageLabel.setImage(UIUtils.loadIcon(R4EUIAnomalyBasic.ANOMALY_ICON_FILE));
 		toolkit.createLabel(r4eClient, "Anomaly");
 
 		imageLabel = toolkit.createLabel(r4eClient, ""); //$NON-NLS-1$
 		imageLabel.setImage(UIUtils.loadIcon(R4EUIPostponedContainer.POSTPONED_CONTAINER_ICON_FILE));
 		toolkit.createLabel(r4eClient, "Imported Postponed Elements Container");
 
 		imageLabel = toolkit.createLabel(r4eClient, ""); //$NON-NLS-1$
 		imageLabel.setImage(UIUtils.loadIcon(R4EUIPostponedFile.POSTPONED_FILE_ICON_FILE));
 		toolkit.createLabel(r4eClient, "Imported Postponed File");
 
 		imageLabel = toolkit.createLabel(r4eClient, ""); //$NON-NLS-1$
 		imageLabel.setImage(UIUtils.loadIcon(R4EUIPostponedAnomaly.POSTPONED_ANOMALY_ICON_FILE));
 		toolkit.createLabel(r4eClient, "Imported Postponed Anomaly");
 
 		imageLabel = toolkit.createLabel(r4eClient, ""); //$NON-NLS-1$
 		imageLabel.setImage(UIUtils.loadIcon(R4EUIRuleSet.RULE_SET_ICON_FILE));
 		toolkit.createLabel(r4eClient, "Rule Set (Open)");
 
 		imageLabel = toolkit.createLabel(r4eClient, ""); //$NON-NLS-1$
 		imageLabel.setImage(UIUtils.loadIcon(R4EUIRuleSet.RULE_SET_CLOSED_ICON_FILE));
 		toolkit.createLabel(r4eClient, "Rule Set (Closed)");
 
 		imageLabel = toolkit.createLabel(r4eClient, ""); //$NON-NLS-1$
 		imageLabel.setImage(UIUtils.loadIcon(R4EUIRuleArea.RULE_AREA_ICON_FILE));
 		toolkit.createLabel(r4eClient, "Rule Area");
 
 		imageLabel = toolkit.createLabel(r4eClient, ""); //$NON-NLS-1$
 		imageLabel.setImage(UIUtils.loadIcon(R4EUIRuleViolation.RULE_VIOLATION_ICON_FILE));
 		toolkit.createLabel(r4eClient, "Rule Violation");
 
 		imageLabel = toolkit.createLabel(r4eClient, ""); //$NON-NLS-1$
 		imageLabel.setImage(UIUtils.loadIcon(R4EUIRule.RULE_ICON_FILE));
 		toolkit.createLabel(r4eClient, "Rule");
 
 		//imageLabel = toolkit.createLabel(r4eClient, ""); //$NON-NLS-1$
 		//imageLabel.setImage(CommonImages.getImage(CommonImages.BLANK));
 	}
 
 	/**
 	 * Method createNavigatorDecoratorsSection.
 	 * 
 	 * @param parent
 	 *            Composite
 	 */
 	private void createNavigatorDecoratorsSection(Composite parent) {
 		final Section section = toolkit.createSection(parent, ExpandableComposite.TITLE_BAR);
 		section.setText("Navigator and Editor Elements Decorators");
 		section.setLayout(new TableWrapLayout());
 		section.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
 
 		final TableWrapLayout layout = new TableWrapLayout();
 		layout.numColumns = 2;
 		layout.makeColumnsEqualWidth = false;
 		layout.verticalSpacing = 1;
 		layout.topMargin = 1;
 		layout.bottomMargin = 1;
 
 		final Composite r4eClient = toolkit.createComposite(section);
 		r4eClient.setLayout(layout);
 		r4eClient.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
 		section.setClient(r4eClient);
 
 		Label imageLabel;
 		imageLabel = toolkit.createLabel(r4eClient, ""); //$NON-NLS-1$
 		imageLabel.setImage(UIUtils.loadIcon(R4EUIModelElement.DISABLED_OVERLAY_ICON_FILE));
 		toolkit.createLabel(r4eClient, "Element Disabled");
 
 		imageLabel = toolkit.createLabel(r4eClient, ""); //$NON-NLS-1$
 		imageLabel.setImage(UIUtils.loadIcon(R4EUIModelElement.READONLY_OVERLAY_ICON_FILE));
 		toolkit.createLabel(r4eClient, "Element Read-Only");
 
 		imageLabel = toolkit.createLabel(r4eClient, ""); //$NON-NLS-1$
 		imageLabel.setImage(UIUtils.loadIcon(R4EUIModelElement.REVIEWED_OVERLAY_ICON_FILE));
 		toolkit.createLabel(r4eClient, "Element Reviewed by User");
 
 		imageLabel = toolkit.createLabel(r4eClient, ""); //$NON-NLS-1$
 		imageLabel.setImage(UIUtils.loadIcon(R4EUIFileContext.ADDED_OVERLAY_ICON_FILE));
 		toolkit.createLabel(r4eClient, "Element Added (File Contexts only)");
 
 		imageLabel = toolkit.createLabel(r4eClient, ""); //$NON-NLS-1$
 		imageLabel.setImage(UIUtils.loadIcon(R4EUIFileContext.REMOVED_OVERLAY_ICON_FILE));
 		toolkit.createLabel(r4eClient, "Element Removed (File Contexts only)");
 
 		imageLabel = toolkit.createLabel(r4eClient, ""); //$NON-NLS-1$
 		imageLabel.setImage(UIUtils.loadIcon(R4EUIModelElement.BOLD_ICON_FILE));
 		toolkit.createLabel(r4eClient, "(Bold font) Active Review (Reviews Only)");
 
 		imageLabel = toolkit.createLabel(r4eClient, ""); //$NON-NLS-1$
 		imageLabel.setImage(UIUtils.loadIcon(R4EUIModelElement.ITALIC_ICON_FILE));
 		toolkit.createLabel(r4eClient, "(Italic font) Element of a Review User is Participating in");
 
 		imageLabel = toolkit.createLabel(r4eClient, ">"); //$NON-NLS-1$
 		toolkit.createLabel(r4eClient, "File Out of Sync with Worskspace (File Contexts only)");
 
 		imageLabel = toolkit.createLabel(r4eClient, ""); //$NON-NLS-1$
 		imageLabel.setImage(UIUtils.loadIcon("icons/obj16/anmlymkr_obj.gif"));
 		toolkit.createLabel(r4eClient, "Anomaly Annotation Marker (R4E Editor Views)");
 
 		imageLabel = toolkit.createLabel(r4eClient, ""); //$NON-NLS-1$
 		imageLabel.setImage(UIUtils.loadIcon("icons/obj16/delta_obj.gif"));
 		toolkit.createLabel(r4eClient, "Delta Annotation Marker (R4E Editor Views)");
 
 		imageLabel = toolkit.createLabel(r4eClient, ""); //$NON-NLS-1$
 		imageLabel.setImage(UIUtils.loadIcon("icons/obj16/sel_obj.gif"));
 		toolkit.createLabel(r4eClient, "Selection Annotation Marker (R4E Editor Views)");
 
 		imageLabel = toolkit.createLabel(r4eClient, ""); //$NON-NLS-1$
 		imageLabel.setImage(UIUtils.loadIcon("icons/ovr16/duedateovr_tsk.gif"));
 		toolkit.createLabel(r4eClient, "Element is overdue");
 	}
 
 	/**
 	 * Method createNavigatorViewCommandsSection.
 	 * 
 	 * @param parent
 	 *            Composite
 	 */
 	private void createNavigatorViewCommandsSection(Composite parent) {
 		final Section section = toolkit.createSection(parent, ExpandableComposite.TITLE_BAR);
 		section.setText("Navigator and Editor View Commands");
 		section.setLayout(new TableWrapLayout());
 		section.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
 
 		final TableWrapLayout layout = new TableWrapLayout();
 		layout.numColumns = 2;
 		layout.makeColumnsEqualWidth = false;
 		layout.verticalSpacing = 1;
 		layout.topMargin = 1;
 		layout.bottomMargin = 1;
 
 		final Composite r4eClient = toolkit.createComposite(section);
 		r4eClient.setLayout(layout);
 		r4eClient.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
 		section.setClient(r4eClient);
 
 		Label imageLabel;
 		imageLabel = toolkit.createLabel(r4eClient, ""); //$NON-NLS-1$
 		imageLabel.setImage(UIUtils.loadIcon(R4EUIConstants.ALPHA_SORTER_ICON_FILE));
 		toolkit.createLabel(r4eClient, "Sort Elements Alphabetically");
 
 		imageLabel = toolkit.createLabel(r4eClient, ""); //$NON-NLS-1$
 		imageLabel.setImage(UIUtils.loadIcon(R4EUIConstants.REVIEW_TYPE_SORTER_ICON_FILE));
 		toolkit.createLabel(r4eClient, "Sort Reviews by Type");
 
 		imageLabel = toolkit.createLabel(r4eClient, ""); //$NON-NLS-1$
 		imageLabel.setImage(UIUtils.loadIcon(R4EUIReviewGroup.REVIEW_GROUP_ICON_FILE));
 		toolkit.createLabel(r4eClient, "New Review Group");
 
 		imageLabel = toolkit.createLabel(r4eClient, ""); //$NON-NLS-1$
 		imageLabel.setImage(UIUtils.loadIcon(R4EUIRuleSet.RULE_SET_ICON_FILE));
 		toolkit.createLabel(r4eClient, "New Rule Set");
 
 		imageLabel = toolkit.createLabel(r4eClient, ""); //$NON-NLS-1$
 		imageLabel.setImage(UIUtils.loadIcon("icons/elcl16/nxtstate_menu.gif"));
 		toolkit.createLabel(r4eClient, "Progress (Advance) Element State");
 
 		imageLabel = toolkit.createLabel(r4eClient, ""); //$NON-NLS-1$
 		imageLabel.setImage(UIUtils.loadIcon("icons/elcl16/prevstate_menu.gif"));
 		toolkit.createLabel(r4eClient, "Regress (Rewind) Element State");
 
 		imageLabel = toolkit.createLabel(r4eClient, ""); //$NON-NLS-1$
 		imageLabel.setImage(UIUtils.loadIcon("icons/elcl16/chgdisplay_menu.gif"));
 		toolkit.createLabel(r4eClient, "Change Display Type");
 
 		imageLabel = toolkit.createLabel(r4eClient, ""); //$NON-NLS-1$
 		imageLabel.setImage(UIUtils.loadIcon("icons/elcl16/expndall_menu.gif"));
 		toolkit.createLabel(r4eClient, "Expand Navigator Tree");
 
 		imageLabel = toolkit.createLabel(r4eClient, ""); //$NON-NLS-1$
 		imageLabel.setImage(UIUtils.loadIcon("icons/elcl16/clpseall_menu.gif"));
 		toolkit.createLabel(r4eClient, "Collapse Navigator Tree");
 
 		imageLabel = toolkit.createLabel(r4eClient, ""); //$NON-NLS-1$
 		imageLabel.setImage(UIUtils.loadIcon("icons/elcl16/focus_menu.gif"));
 		toolkit.createLabel(r4eClient, "Go Into (Focus on selected Element)");
 
 		imageLabel = toolkit.createLabel(r4eClient, ""); //$NON-NLS-1$
 		imageLabel.setImage(UIUtils.loadIcon("icons/elcl16/nxtelem_menu.gif"));
 		toolkit.createLabel(r4eClient, "Select Next Element (and Open if possible)");
 
 		imageLabel = toolkit.createLabel(r4eClient, ""); //$NON-NLS-1$
 		imageLabel.setImage(UIUtils.loadIcon("icons/elcl16/prvelem_menu.gif"));
 		toolkit.createLabel(r4eClient, "Select Previous Element (and Open if possible)");
 
 		imageLabel = toolkit.createLabel(r4eClient, ""); //$NON-NLS-1$
 		imageLabel.setImage(UIUtils.loadIcon("icons/elcl16/nxtanmly_menu.png"));
 		toolkit.createLabel(r4eClient, "Go To Next Open Anomaly Annotation (Compare Editor Window)");
 
 		imageLabel = toolkit.createLabel(r4eClient, ""); //$NON-NLS-1$
 		imageLabel.setImage(UIUtils.loadIcon("icons/elcl16/prevanmly_menu.png"));
 		toolkit.createLabel(r4eClient, "Go To Previous Open Anomaly Annotation (Compare Editor Window)");
 
 		imageLabel = toolkit.createLabel(r4eClient, ""); //$NON-NLS-1$
 		imageLabel.setImage(UIUtils.loadIcon("icons/view16/icons-legend.gif"));
 		toolkit.createLabel(r4eClient, "Show UI Legend Dialog (You are looking at it right now!)");
 	}
 
 	/**
 	 * Method createContextCommandsSection.
 	 * 
 	 * @param parent
 	 *            Composite
 	 */
 	private void createContextCommandsSection(Composite parent) {
 		final Section section = toolkit.createSection(parent, ExpandableComposite.TITLE_BAR);
 		section.setText("Contextual Commands");
 		section.setLayout(new TableWrapLayout());
 		section.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
 
 		final TableWrapLayout layout = new TableWrapLayout();
 		layout.numColumns = 2;
 		layout.makeColumnsEqualWidth = false;
 		layout.verticalSpacing = 1;
 		layout.topMargin = 1;
 		layout.bottomMargin = 1;
 
 		final Composite r4eClient = toolkit.createComposite(section);
 		r4eClient.setLayout(layout);
 		r4eClient.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
 		section.setClient(r4eClient);
 
 		Label imageLabel;
 		imageLabel = toolkit.createLabel(r4eClient, ""); //$NON-NLS-1$
 		imageLabel.setImage(UIUtils.loadIcon("icons/view16/finditms_tsk.gif"));
 		toolkit.createLabel(r4eClient, "Find Review Items in Version Control System");
 
 		imageLabel = toolkit.createLabel(r4eClient, ""); //$NON-NLS-1$
 		imageLabel.setImage(UIUtils.loadIcon("icons/view16/newitm_tsk.png"));
 		toolkit.createLabel(r4eClient, "New Review Item");
 
 		imageLabel = toolkit.createLabel(r4eClient, ""); //$NON-NLS-1$
 		imageLabel.setImage(UIUtils.loadIcon("icons/view16/newanmly_tsk.png"));
 		toolkit.createLabel(r4eClient, "New Anomaly");
 
 		imageLabel = toolkit.createLabel(r4eClient, ""); //$NON-NLS-1$
 		imageLabel.setImage(UIUtils.loadIcon(R4EUIConstants.SEND_EMAIL_ICON_FILE));
 		toolkit.createLabel(r4eClient, "Send Email or Notification");
 
 		imageLabel = toolkit.createLabel(r4eClient, ""); //$NON-NLS-1$
 		imageLabel.setImage(UIUtils.loadIcon(R4EUIConstants.OPEN_ELEMENT_ICON_FILE));
 		toolkit.createLabel(r4eClient, "Open (Load) Element");
 
 		imageLabel = toolkit.createLabel(r4eClient, ""); //$NON-NLS-1$
 		imageLabel.setImage(UIUtils.loadIcon(R4EUIConstants.CLOSE_ELEMENT_ICON_FILE));
 		toolkit.createLabel(r4eClient, "Close (Unload) Element");
 
 		imageLabel = toolkit.createLabel(r4eClient, ""); //$NON-NLS-1$
 		imageLabel.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ADD));
 		toolkit.createLabel(r4eClient, "Add Child Element");
 
 		imageLabel = toolkit.createLabel(r4eClient, ""); //$NON-NLS-1$
 		imageLabel.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_TOOL_DELETE));
 		toolkit.createLabel(r4eClient, "Remove Element");
 
 		imageLabel = toolkit.createLabel(r4eClient, ""); //$NON-NLS-1$
 		imageLabel.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_TOOL_UNDO));
 		toolkit.createLabel(r4eClient, "Restore Disabled Element");
 
 		imageLabel = toolkit.createLabel(r4eClient, ""); //$NON-NLS-1$
 		imageLabel.setImage(UIUtils.loadIcon(R4EUIConstants.OPEN_EDITOR_ICON_FILE));
 		toolkit.createLabel(r4eClient, "Open File in Editor");
 
 		imageLabel = toolkit.createLabel(r4eClient, ""); //$NON-NLS-1$
 		imageLabel.setImage(UIUtils.loadIcon(R4EUIConstants.CHANGE_REVIEW_STATE_ICON_FILE));
 		toolkit.createLabel(r4eClient, "Toggle Element's User Review State (Reviewed/Not Reviewed");
 
 		imageLabel = toolkit.createLabel(r4eClient, ""); //$NON-NLS-1$
 		imageLabel.setImage(UIUtils.loadIcon(R4EUIConstants.ASSIGN_TO_ICON_FILE));
 		toolkit.createLabel(r4eClient, "Assign element to Participant");
 
 		imageLabel = toolkit.createLabel(r4eClient, ""); //$NON-NLS-1$
 		imageLabel.setImage(UIUtils.loadIcon(R4EUIConstants.UNASSIGN_TO_ICON_FILE));
 		toolkit.createLabel(r4eClient, "Unassign Participant");
 
 		imageLabel = toolkit.createLabel(r4eClient, ""); //$NON-NLS-1$
 		imageLabel.setImage(UIUtils.loadIcon(R4EUIConstants.IMPORT_POSTPONED_ICON_FILE));
 		toolkit.createLabel(r4eClient, "Import Postponed Elements");
 
 		imageLabel = toolkit.createLabel(r4eClient, ""); //$NON-NLS-1$
 		imageLabel.setImage(UIUtils.loadIcon(R4EUIConstants.REPORT_ELEMENT_ICON_FILE));
 		toolkit.createLabel(r4eClient, "Generate Report for Review");
 
 		imageLabel = toolkit.createLabel(r4eClient, ""); //$NON-NLS-1$
 		imageLabel.setImage(UIUtils.loadIcon(R4EUIConstants.SHOW_PROPERTIES_ICON_FILE));
		toolkit.createLabel(r4eClient, "Show the properties view for the selected item");
 	}
 
 	/**
 	 * Method createLinkHelpSection.
 	 * 
 	 * @param parent
 	 *            Composite
 	 */
 	private void createLinkHelpSection(Composite parent) {
 		final TableWrapLayout layout = new TableWrapLayout();
 		layout.verticalSpacing = 0;
 		layout.leftMargin = 0;
 		layout.rightMargin = 0;
 		layout.topMargin = 0;
 		layout.bottomMargin = 0;
 
 		final Composite hyperlinkClient = toolkit.createComposite(parent);
 		hyperlinkClient.setLayout(layout);
 		hyperlinkClient.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
 
 		final Hyperlink gettingStartedLink = toolkit.createHyperlink(hyperlinkClient, "See also R4E online help",
 				SWT.WRAP);
 		gettingStartedLink.addHyperlinkListener(new IHyperlinkListener() {
 			public void linkActivated(HyperlinkEvent e) {
 				close();
 				UIUtils.openUrl("http://wiki.eclipse.org/Reviews/R4E/User_Guide");
 			}
 
 			public void linkEntered(HyperlinkEvent e) {
 				// ignore
 			}
 
 			public void linkExited(HyperlinkEvent e) {
 				// ignore
 			}
 		});
 	}
 }
