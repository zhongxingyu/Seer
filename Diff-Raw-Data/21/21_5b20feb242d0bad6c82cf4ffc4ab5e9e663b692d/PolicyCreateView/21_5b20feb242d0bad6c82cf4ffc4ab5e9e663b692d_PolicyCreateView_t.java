 /*******************************************************************************
  * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
  * Licensed under the Apache License, Version 2.0 (the "License"); 
  * you may not use this file except in compliance with the License. 
  * You may obtain a copy of the License at 
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *******************************************************************************/
 package org.ebayopensource.turmeric.policy.adminui.client.view.policy;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.ebayopensource.turmeric.policy.adminui.client.Display;
 import org.ebayopensource.turmeric.policy.adminui.client.PolicyAdminUIUtil;
 import org.ebayopensource.turmeric.policy.adminui.client.model.UserAction;
 import org.ebayopensource.turmeric.policy.adminui.client.model.policy.Operation;
 import org.ebayopensource.turmeric.policy.adminui.client.model.policy.PolicySubjectAssignment;
 import org.ebayopensource.turmeric.policy.adminui.client.model.policy.Resource;
 import org.ebayopensource.turmeric.policy.adminui.client.model.policy.Subject;
 import org.ebayopensource.turmeric.policy.adminui.client.model.policy.SubjectGroup;
 import org.ebayopensource.turmeric.policy.adminui.client.presenter.policy.PolicyCreatePresenter.PolicyCreateDisplay;
 import org.ebayopensource.turmeric.policy.adminui.client.presenter.policy.PolicyCreatePresenter.ResourcesContentDisplay;
 import org.ebayopensource.turmeric.policy.adminui.client.presenter.policy.PolicyCreatePresenter.SubjectContentDisplay;
 import org.ebayopensource.turmeric.policy.adminui.client.view.ErrorDialog;
 import org.ebayopensource.turmeric.policy.adminui.client.view.InfoDialog;
 import org.ebayopensource.turmeric.policy.adminui.client.view.common.AbstractGenericView;
 import org.ebayopensource.turmeric.policy.adminui.client.view.common.SelectBoxesWidget;
 import org.ebayopensource.turmeric.policy.adminui.client.view.common.TurmericPager;
 import org.ebayopensource.turmeric.policy.adminui.client.view.common.TurmericStackPanel;
 
 import com.google.gwt.cell.client.Cell;
 import com.google.gwt.cell.client.Cell.Context;
 import com.google.gwt.cell.client.CheckboxCell;
 import com.google.gwt.cell.client.ClickableTextCell;
 import com.google.gwt.cell.client.FieldUpdater;
 import com.google.gwt.dom.client.Element;
 import com.google.gwt.dom.client.NativeEvent;
 import com.google.gwt.event.dom.client.ChangeHandler;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.HasChangeHandlers;
 import com.google.gwt.event.dom.client.HasClickHandlers;
 import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
 import com.google.gwt.user.cellview.client.CellTable;
 import com.google.gwt.user.cellview.client.Column;
 import com.google.gwt.user.cellview.client.Header;
 import com.google.gwt.user.cellview.client.TextColumn;
 import com.google.gwt.user.client.ui.Anchor;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.CheckBox;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.DisclosurePanel;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.Grid;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment;
 import com.google.gwt.user.client.ui.HasVerticalAlignment;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.Panel;
 import com.google.gwt.user.client.ui.PopupPanel;
 import com.google.gwt.user.client.ui.PushButton;
 import com.google.gwt.user.client.ui.ResizeComposite;
 import com.google.gwt.user.client.ui.ScrollPanel;
 import com.google.gwt.user.client.ui.SimplePanel;
 import com.google.gwt.user.client.ui.TextArea;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.gwt.view.client.ListDataProvider;
 import com.google.gwt.view.client.MultiSelectionModel;
 import com.google.gwt.view.client.ProvidesKey;
 import com.google.gwt.view.client.SelectionChangeEvent;
 
 /**
  * The Class PolicyCreateView.
  */
 public abstract class PolicyCreateView extends ResizeComposite implements
 		PolicyCreateDisplay {
 
 	private Panel mainPanel;
 	private ScrollPanel scrollPanel;
 
 	/** The content view. */
 	protected Display contentView;
 	private ResourcesContentDisplay resourceContentView;
 	private SubjectContentDisplay subjectContentView;
 
 	/** The policy name. */
 	protected TextBox policyName;
 	
 	/** The policy desc. */
 	protected TextArea policyDesc;
 	
 	/** The policy type. */
 	protected Label policyType;
 	
 	/** The policy status. */
 	protected ListBox policyStatus;
 	
 	/** The policy enabled. */
 	protected boolean policyEnabled;
 
 	private Button saveButton;
 	private Button cancelButton;
 
 	/** The extra fields grid. */
 	protected Grid extraFieldsGrid = new Grid(1, 1);
 
 	public native static String getUserAgent() /*-{
 		return navigator.userAgent.toLowerCase();
 	}-*/;
 
 	
 	private static interface GetRsValue<C> {
 		C getValue(Resource rs);
 	}
 
 	private static interface GetSubjectValue<C> {
 		C getValue(PolicySubjectAssignment sb);
 	}
 
 	/**
 	 * Gets the title form.
 	 * 
 	 * @return the title form
 	 */
 	protected abstract String getTitleForm();
 
 	/**
 	 * PolicyConditionWidget
 	 * 
 	 */
 	private class PolicyConditionWidget extends Composite {
 		private FlowPanel mainPanel;
 		private Button addConditionButton;
 
 		TextBox quantityBox;
 
 		private Label rsLabel;
 		private Label opLabel;
 
 		private ListBox rsListBox;
 		private ListBox opListBox;
 		private ListBox subjectOrSGListBox;
 		private ListBox conditionListBox;
 		private ListBox aritmSignListBox;
 		private ListBox logicOpListBox;
 
 		public PolicyConditionWidget() {
 			mainPanel = new FlowPanel();
 
 			final Grid grid = new Grid(3, 8);
 
 			rsLabel = new Label(
 					PolicyAdminUIUtil.policyAdminConstants.service());
 			opLabel = new Label(
 					PolicyAdminUIUtil.policyAdminConstants.operationName());
 
 			rsListBox = new ListBox(false);
 			opListBox = new ListBox(false);
 			subjectOrSGListBox = new ListBox(false);
 			conditionListBox = new ListBox(false);
 			aritmSignListBox = new ListBox(false);
 			logicOpListBox = new ListBox(false);
 			
 			setSubjectOrSGListBox();
 			setAritmSignListBoxValues();
 			setLogicOpListBoxValues();
 
 			quantityBox = new TextBox();
 
 			addConditionButton = new Button(
 					PolicyAdminUIUtil.policyAdminConstants.add());
 
 			grid.setWidget(0, 0, rsLabel);
 			grid.setWidget(0, 2, opLabel);
 
 			grid.setWidget(1, 0, rsListBox);
 			grid.setWidget(1, 1, new Label(":"));
 			grid.setWidget(1, 2, opListBox);
 			grid.setWidget(1, 3, subjectOrSGListBox);
 			grid.setWidget(1, 4, conditionListBox);
 			grid.setWidget(1, 5, aritmSignListBox);
 			grid.setWidget(1, 6, quantityBox);
 			grid.setWidget(1, 7, logicOpListBox);
 
 			grid.setWidget(2, 4, addConditionButton);
 
 			mainPanel.add(grid);
 			initWidget(mainPanel);
 		}
 
 		private void setAritmSignListBoxValues() {
 			aritmSignListBox.addItem("<");
 			aritmSignListBox.addItem(">");
 			aritmSignListBox.addItem("<=");
 			aritmSignListBox.addItem(">=");
 			aritmSignListBox.addItem("=");
 		}
 
 		private void setSubjectOrSGListBox(){
 			subjectOrSGListBox.addItem("");
 			subjectOrSGListBox.addItem("SubjectGroup");
 			subjectOrSGListBox.addItem("SubjectGroup.Subject");
 		}
 		
 		private void setLogicOpListBoxValues() {
 			logicOpListBox.addItem("");
 			logicOpListBox.addItem(" && ");
 			logicOpListBox.addItem(" || ");
 		}
 
 		public void clearConditionBuilder() {
 			// select nothing
 			rsListBox.setSelectedIndex(-1);
 			opListBox.setSelectedIndex(-1);
 			subjectOrSGListBox.setSelectedIndex(-1);
 			conditionListBox.setSelectedIndex(-1);
 			aritmSignListBox.setSelectedIndex(-1);
 			logicOpListBox.setSelectedIndex(-1);
 			quantityBox.setText("");
 		}
 
 		public boolean validAllFields() {
 			boolean valid = true;
 
 			if (this.rsListBox.getSelectedIndex() == -1) {
 				return false;
 			}
 			if (this.opListBox.getSelectedIndex() == -1) {
 				return false;
 			}
 			if (this.aritmSignListBox.getSelectedIndex() == -1) {
 				return false;
 			}
 			if ("".equals(this.quantityBox.getText())) {
 				return false;
 			}
 
 			return valid;
 		}
 
 		public HasClickHandlers getAddConditionButton() {
 			return addConditionButton;
 		}
 
 		public HasChangeHandlers getRsListBox() {
 			return rsListBox;
 		}
 
 		public String getQuantityBox() {
 			return quantityBox.getValue();
 		}
 
 		public String getRsNameSelected() {
 			if (rsListBox.getSelectedIndex() >= 0)
 				return rsListBox.getItemText(rsListBox.getSelectedIndex());
 			else
 				return null;
 		}
 
 		public String getOpNameSelected() {
 			if (opListBox.getSelectedIndex() >= 0)
 				return opListBox.getItemText(opListBox.getSelectedIndex());
 			else
 				return null;
 		}
 
 		public String getConditionSelected() {
 			if (conditionListBox.getSelectedIndex() >= 0)
 				return conditionListBox.getItemText(conditionListBox
 						.getSelectedIndex());
 			else
 				return null;
 		}
 
 		public String getAritmSignSelected() {
 			if (aritmSignListBox.getSelectedIndex() >= 0)
 				return aritmSignListBox.getItemText(aritmSignListBox
 						.getSelectedIndex());
 			else
 				return null;
 		}
 
 		public String getLogicOpSelected() {
 			if (logicOpListBox.getSelectedIndex() >= 0)
 				return logicOpListBox.getItemText(logicOpListBox
 						.getSelectedIndex());
 			else
 				return null;
 		}
 		
 		public String getSubjectOrSGSelected() {
 			if (subjectOrSGListBox.getSelectedIndex() > 0)
 				return ".".concat(subjectOrSGListBox.getItemText(subjectOrSGListBox
 						.getSelectedIndex()));
 			else
 				return null;
 		}
 
 		public void setRsNames(List<String> names) {
 			rsListBox.clear();
 			for (String s : names) {
 				rsListBox.addItem(s);
 			}
 		}
 
 		public void initializeRsListBox() {
 			rsListBox.setSelectedIndex(-1);
 		}
 
 		public void setOpNames(List<String> names) {
 			opListBox.clear();
 			for (String s : names) {
 				opListBox.addItem(s);
 			}
 		}
 
 		public void setConditionNames(List<String> conditions) {
 			conditionListBox.clear();
 			for (String s : conditions) {
 				conditionListBox.addItem(s);
 			}
 		}
 
 	}
 
 	/**
 	 * Instantiates a new policy create view.
 	 */
 	public PolicyCreateView() {
 
 		scrollPanel = new ScrollPanel();
 		mainPanel = new FlowPanel();
 		initWidget(scrollPanel);
       
 		initialize();
 
 	}
 
 	/**
 	 * Gets the selected action.
 	 * 
 	 * @return the selected action
 	 */
 	public abstract UserAction getSelectedAction();
 
 	/**
 	 * Initialize.
 	 */
 	public void initialize() {
 
 		policyName = new TextBox();
 		policyDesc = new TextArea();
 		policyType = new Label();
 		policyStatus = new ListBox();
 		policyStatus.addItem(PolicyAdminUIUtil.policyAdminConstants.enable(),
 				PolicyAdminUIUtil.policyAdminConstants.enable());
 		policyStatus.addItem(PolicyAdminUIUtil.policyAdminConstants.disable(),
 				PolicyAdminUIUtil.policyAdminConstants.disable());
 		//Turmeric-1401
 		policyStatus.setEnabled(false);
 		
 		initializeExtraFields();
 
 		// CONTENT
 		TurmericStackPanel policyContentPanel = new TurmericStackPanel();
 		policyContentPanel.add(initContentView(), getTitleForm());
 
 		policyContentPanel.add(initResourceContentView(),
 				PolicyAdminUIUtil.policyAdminConstants.resources(), true);
 		policyContentPanel.add(initSubjectContentView(),
 				PolicyAdminUIUtil.policyAdminConstants
 						.subjectsAndSubjectGroups());
 		policyContentPanel.setWidth("100%");
 
 		saveButton = new Button(PolicyAdminUIUtil.constants.save());
 		cancelButton = new Button(PolicyAdminUIUtil.constants.cancel());
 		saveButton.setWidth("80px");
 		cancelButton.setWidth("80px");
 
 		HorizontalPanel buttonsPannel = new HorizontalPanel();
 		buttonsPannel.add(saveButton);
 		buttonsPannel.add(cancelButton);
 
 		mainPanel.add(policyContentPanel);
 		mainPanel.add(buttonsPannel);
 		scrollPanel.setHeight("95%");
 		scrollPanel.add(mainPanel);
 		
 	}
 
 	/**
 	 * Initialize extra fields.
 	 */
 	protected abstract void initializeExtraFields();
 
 	/**
 	 * Inits the content view.
 	 * 
 	 * @return the widget
 	 */
 	protected Widget initContentView() {
 		ScrollPanel actionPanel = new ScrollPanel();
 		contentView = new ContentView();
 		actionPanel.add(contentView.asWidget());
 		return actionPanel;
 	}
 
 	/**
 	 * Inits the resource content view.
 	 * 
 	 * @return the widget
 	 */
 	protected Widget initResourceContentView() {
 		ScrollPanel actionPanel = new ScrollPanel();
 		resourceContentView = new ResourceContentView();
 		actionPanel.add(resourceContentView.asWidget());
 
 		return actionPanel;
 	}
 
 	/**
 	 * Inits the subject content view.
 	 * 
 	 * @return the widget
 	 */
 	protected Widget initSubjectContentView() {
 		ScrollPanel actionPanel = new ScrollPanel();
 		subjectContentView = new SubjectContentView();
 		actionPanel.add(subjectContentView.asWidget());
 
 		return actionPanel;
 	}
 
 	/**
 	 * The Class ContentView.
 	 */
 	protected class ContentView extends AbstractGenericView implements Display {
 		private VerticalPanel mainPanel;
 		private Grid policyInfoGrid;
 		private PolicyConditionWidget conditionWidget;
 		
 		/** The condition panel. */
 		protected DisclosurePanel conditionPanel;
 
 		/**
 		 * Instantiates a new content view.
 		 */
 		public ContentView() {
 			mainPanel = new VerticalPanel();
 			initWidget(mainPanel);
 			initialize();
 		}
 
 		/* (non-Javadoc)
 		 * @see org.ebayopensource.turmeric.policy.adminui.client.Display#activate()
 		 */
 		public void activate() {
 			// do nothing for now
 		}
 
 		/* (non-Javadoc)
 		 * @see org.ebayopensource.turmeric.policy.adminui.client.view.common.AbstractGenericView#initialize()
 		 */
 		@Override
 		public void initialize() {
 			mainPanel.clear();
 
 			policyName.setWidth("300px");
 			policyDesc.setWidth("550px");
 			policyStatus.setWidth("100px");
 			policyType.setWidth("300px");
 
 			policyInfoGrid = new Grid(4, 2);
 
 			policyInfoGrid.setWidget(0, 0, new Label(
 					PolicyAdminUIUtil.policyAdminConstants.policyType() + ":"));
 			policyInfoGrid.setWidget(0, 1, policyType);
 
 			policyInfoGrid.setWidget(1, 0, new Label(
 					PolicyAdminUIUtil.policyAdminConstants.policyName() + ":"));
 			policyInfoGrid.setWidget(1, 1, policyName);
 
 			policyInfoGrid.setWidget(2, 0, new Label(
 					PolicyAdminUIUtil.policyAdminConstants.policyDescription()
 							+ ":"));
 			policyInfoGrid.setWidget(2, 1, policyDesc);
 
 			policyInfoGrid.setWidget(3, 0, new Label(
 					PolicyAdminUIUtil.policyAdminConstants.status() + ":"));
 			policyInfoGrid.setWidget(3, 1, policyStatus);
 
 			mainPanel.add(policyInfoGrid);
 			mainPanel.add(extraFieldsGrid);
 
 			conditionWidget = new PolicyConditionWidget();
 			conditionPanel = new DisclosurePanel(
 					PolicyAdminUIUtil.policyAdminConstants.conditionBuilder());
 			conditionPanel.setContent(conditionWidget);
 			conditionPanel.setStyleName("policy-content");
 
 			mainPanel.add(conditionPanel);
 
 		}
 	}
 
 	private class ResourceContentView extends AbstractGenericView implements
 			ResourcesContentDisplay {
 		private FlowPanel mainPanel;
 
 		private CellTable<Resource> cellTable;
 		MultiSelectionModel<Resource> selectionModel;
 		ProvidesKey<Resource> keyProvider;
 		private PolicyResourceAssignmentWidget resourceAssignmentWidget;
 		private PopupPanel resourceAssignmentPopup;
 		private Button addResourceButton;
 		private Button cancelResourceButton;
 		private FlowPanel resourceAssignmentButtonPanel;
 		private PushButton delButton;
 		private PushButton editButton;
 		private List<String> allAvailableRsLevels;
 		private List<Resource> selections = new ArrayList<Resource>();
 		private ListDataProvider<Resource> dataProvider;
 		private List<UserAction> permissions;
 
 		public ResourceContentView() {
 			mainPanel = new FlowPanel();
 			initWidget(mainPanel);
 			initialize();
 		}
 
 		public void setUserActions(List<UserAction> actions) {
 			permissions = actions;
 
 			enableActions();
 		}
 
 		public void activate() {
 			// do nothing for now
 		}
 
 		public void clearAssignmentWidget() {
 			resourceAssignmentWidget.clear();
 		}
 
 		public void show() {
 			resourceAssignmentPopup.center();
 		}
 
 		public void hide() {
 			clearAssignmentWidget();
 			resourceAssignmentPopup.hide();
 		}
 
 		@Override
 		public void initialize() {
 			mainPanel.clear();
 			createResourceAssignmentFields();
 
 			// bottom part of panel is a table with search results
 			Grid summaryGrid = new Grid(3, 1);
 			summaryGrid.setStyleName("sggrid");
 
 			Grid actionGrid = new Grid(1, 2);
 			actionGrid.setWidth("100%");
 
 			editButton = new PushButton("[ "
 					+ PolicyAdminUIUtil.policyAdminConstants.edit() + " ]");
 			editButton.setVisible(false);
 
 			delButton = new PushButton("[ "
 					+ PolicyAdminUIUtil.policyAdminConstants.delete() + " ]");
 			delButton.setWidth("60px");
 
 			delButton.setEnabled(false);
 
 			Anchor assignResourcesClickable = new Anchor(
 					"[ "
 							+ PolicyAdminUIUtil.policyAdminConstants
 									.resourceAssignResources() + " ]");
 			assignResourcesClickable.addClickHandler(new ClickHandler() {
 				@Override
 				public void onClick(ClickEvent event) {
 					subjectContentView.hide();
 					clearAssignmentWidget();
 					getResourceContentView().setResourceLevel(
 							allAvailableRsLevels);
 					getResourceContentView().getResourceLevelBox()
 							.setSelectedIndex(-1);
 					resourceAssignmentPopup.center();
 				}
 			});
 
 			actionGrid.setWidget(0, 0, delButton);
 			actionGrid.getCellFormatter().setWidth(0, 0, "20%");
 
 			actionGrid.setWidget(0, 1, assignResourcesClickable);
 			actionGrid.getCellFormatter().setWidth(0, 1, "80%");
 			actionGrid.getCellFormatter().setAlignment(0, 1,
 					HasHorizontalAlignment.ALIGN_RIGHT,
 					HasVerticalAlignment.ALIGN_MIDDLE);
 			summaryGrid.setWidget(0, 0, actionGrid);
 
 			keyProvider = new ProvidesKey<Resource>() {
 				public Object getKey(Resource assignment) {
 					return assignment == null ? null : assignment
 							.getResourceType() + assignment.getResourceName();
 				}
 			};
 
 			cellTable = new CellTable<Resource>(keyProvider);
 			cellTable.setWidth("70%");
 			selectionModel = new MultiSelectionModel<Resource>(keyProvider);
 			cellTable.setSelectionModel(selectionModel);
 			selectionModel
 					.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
 						public void onSelectionChange(SelectionChangeEvent event) {
 							Set<Resource> x = selectionModel.getSelectedSet();
 							selections.clear();
 							if (x != null)
 								selections.addAll(x);
 							enableActions();
 						}
 					});
 			dataProvider = new ListDataProvider<Resource>();
 			dataProvider.addDataDisplay(cellTable);
 		    
 			TurmericPager pager = new TurmericPager();
 			
 			pager.setDisplay(cellTable);
 
 			// checkbox column for selection
 			Column<Resource, Boolean> checkColumn = new Column<Resource, Boolean>(
 					new CheckboxCell(true)) {
 				public Boolean getValue(Resource rs) {
 					// Get the value from the selection model.
 					return selectionModel.isSelected(rs);
 				}
 			};
 			checkColumn.setFieldUpdater(new FieldUpdater<Resource, Boolean>() {
 				public void update(int index, Resource rs, Boolean value) {
 					// Called when the user clicks on a checkbox.
 					selectionModel.setSelected(rs, value);
 				}
 			});
 
 			final CheckboxCell cb = new CheckboxCell();
 			Header<Boolean> hdr = new Header<Boolean>(cb) {
 				boolean selected = false;
 
 				@Override
 				public void onBrowserEvent(Context context, Element elem,
 						NativeEvent event) {
 					super.onBrowserEvent(context, elem, event);
 					selected = !selected;
 					selectionModel.clear();
 					for (Resource visibleItem : cellTable.getVisibleItems()) {
 						selectionModel.setSelected(visibleItem, selected);
 					}
 				}
 
 				@Override
 				public Boolean getValue() {
 					return false;
 				}
 			};
 			cellTable.addColumn(checkColumn, hdr);
 
 			// resource type
 			TextColumn<Resource> resourceTypeCol = new TextColumn<Resource>() {
 				public String getValue(Resource assignment) {
 					if (assignment == null
 							|| assignment.getResourceType() == null) {
 						return null;
 					}
 					return assignment.getResourceType();
 				}
 			};
 			cellTable.addColumn(resourceTypeCol,
 					PolicyAdminUIUtil.policyAdminConstants.resourceType());
 
 			// resource Name
 			ClickableTextCell resourceNameCellClickable = new ClickableTextCell();
 			Column<Resource, String> resourceNameCol = createCell(
 					resourceNameCellClickable, new GetRsValue<String>() {
 						public String getValue(Resource assignment) {
 							if (assignment == null
 									|| assignment.getResourceName() == null) {
 								return null;
 							}
 							return assignment.getResourceName();
 						}
 					}, new FieldUpdater<Resource, String>() {
 						public void update(int index, Resource rs, String value) {
 							selections.add(rs);
 							editButton.fireEvent(new ClickEvent() {
 							});
 						}
 					});
 			cellTable.addColumn(resourceNameCol,
 					PolicyAdminUIUtil.policyAdminConstants.resourceName());
 
 			// operations
 			Column<Resource, List<String>> resourceOpsCol = new Column<Resource, List<String>>(
 					new CustomListCell(MIN_SCROLLBAR_SIZE)) {
 				public List<String> getValue(Resource resource) {
 
 					if (resource == null || resource.getOpList() == null) {
 						return null;
 					}
 					ArrayList<String> opsNamesList = new ArrayList<String>();
 					for (Operation op : resource.getOpList()) {
 						opsNamesList.add(op.getOperationName());
 					}
 					return opsNamesList;
 				}
 			};
 
 			cellTable.addColumn(resourceOpsCol,
 					PolicyAdminUIUtil.policyAdminConstants.operations());
 			summaryGrid.setWidget(1, 0, cellTable);
 			summaryGrid.setWidget(2, 0, pager);
 
 			mainPanel.addStyleName("resource-summary");
 
 			summaryGrid.addStyleName("resource-content");
 			mainPanel.add(summaryGrid);
 		}
 
 		private <C> Column<Resource, C> createCell(Cell<C> cell,
 				final GetRsValue<C> getter,
 				FieldUpdater<Resource, C> fieldUpdater) {
 			Column<Resource, C> column = new Column<Resource, C>(cell) {
 
 				@Override
 				public C getValue(Resource object) {
 					return getter.getValue(object);
 				}
 
 				@Override
 				public void render(Cell.Context context, Resource object,
 						SafeHtmlBuilder sb) {
 					sb.appendHtmlConstant("<a href='javascript:void(0);'>");
 					super.render(context, object, sb);
 					sb.appendHtmlConstant("</a>");
 				}
 			};
 			column.setFieldUpdater(fieldUpdater);
 			return column;
 		}
 
 		protected void createResourceAssignmentFields() {
 			resourceAssignmentWidget = new PolicyResourceAssignmentWidget();
 			resourceAssignmentPopup = new PopupPanel();
 			resourceAssignmentPopup.setGlassEnabled(true);
 
 			resourceAssignmentPopup.setWidth("600px");
 			resourceAssignmentPopup.setHeight("300px");
 
 			addResourceButton = new Button(
 					PolicyAdminUIUtil.policyAdminConstants.save(),
 					new ClickHandler() {
 
 						@Override
 						public void onClick(ClickEvent paramClickEvent) {
 							resourceAssignmentPopup.hide();
 						}
 					});
 
 			cancelResourceButton = new Button(
 					PolicyAdminUIUtil.policyAdminConstants.cancel());
 
 			resourceAssignmentButtonPanel = new FlowPanel();
 			resourceAssignmentButtonPanel.add(addResourceButton);
 			resourceAssignmentButtonPanel.add(cancelResourceButton);
 
 			Grid resourceAssignmentGrid = new Grid(3, 1);
 			Label title = new Label(
 					PolicyAdminUIUtil.policyAdminConstants.assignResources());
 			title.setAutoHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
 			resourceAssignmentGrid.setWidget(0, 0, title);
 
 			resourceAssignmentGrid.setWidget(1, 0, resourceAssignmentWidget);
 			resourceAssignmentGrid.getCellFormatter().setVerticalAlignment(1,
 					0, HasVerticalAlignment.ALIGN_TOP);
 
 			resourceAssignmentGrid.setWidget(2, 0,
 					resourceAssignmentButtonPanel);
 			resourceAssignmentGrid.getCellFormatter().setVerticalAlignment(2,
 					0, HasVerticalAlignment.ALIGN_BOTTOM);
 			resourceAssignmentGrid.getCellFormatter().setHorizontalAlignment(2,
 					0, HasHorizontalAlignment.ALIGN_CENTER);
 			resourceAssignmentPopup.add(resourceAssignmentGrid);
 
 		}
 
 		public List<Resource> getSelections() {
 			return selections;
 		}
 
 		private void enableActions() {
 			boolean permitted = permissions != null
 					&& permissions.contains(UserAction.POLICY_EDIT);
 			delButton.setEnabled(permitted && selections.size() >= 1);
 		}
 
 		@Override
 		public void setAvailableOperations(List<String> operations) {
 			resourceAssignmentWidget.setAvailableOperations(operations);
 		}
 
 		@Override
 		public void setSelectedOperations(List<String> operations) {
 			resourceAssignmentWidget.setSelectedOperations(operations);
 		}
 
 		@Override
 		public void setResourceTypes(List<String> resourceTypes) {
 			resourceAssignmentWidget.setResourceTypes(resourceTypes);
 		}
 
 		@Override
 		public void setResourceNames(List<String> resourceNames) {
 			resourceAssignmentWidget.setResourceNames(resourceNames);
 
 		}
 
 		@Override
 		public void setResourceLevel(List<String> resourceLevels) {
 			allAvailableRsLevels = new ArrayList<String>(resourceLevels);
 			resourceAssignmentWidget.setResourceLevels(allAvailableRsLevels);
 		}
 
 		@Override
 		public HasClickHandlers getAddResourceButton() {
 			return addResourceButton;
 		}
 
 		@Override
 		public HasClickHandlers getCancelResourceButton() {
 			return cancelResourceButton;
 		}
 
 		@Override
 		public String getResourceName() {
 			return resourceAssignmentWidget.getResourceName();
 		}
 
 		@Override
 		public String getResourceLevel() {
 			return resourceAssignmentWidget.getResourceLevel();
 		}
 
 		public HasClickHandlers getDelButton() {
 			return delButton;
 		}
 
 		public MultiSelectionModel<Resource> getSelectionModel() {
 			return selectionModel;
 		}
 		
 		@Override
 		public ListBox getResourceTypeBox() {
 			return resourceAssignmentWidget.getResourceTypeBox();
 		}
 
 		@Override
 		public ListBox getResourceLevelBox() {
 			return resourceAssignmentWidget.getResourceLevelBox();
 		}
 
 		@Override
 		public String getResourceType() {
 			return resourceAssignmentWidget.getResourceType();
 		}
 
 		@Override
 		public void error(String msg) {
 			ErrorDialog dialog = new ErrorDialog(true);
 			dialog.setMessage(msg);
 			dialog.getDialog().center();
 			dialog.show();
 
 		}
 
 		@Override
 		public ListBox getResourceNameBox() {
 			return resourceAssignmentWidget.getResourceNameBox();
 		}
 
 		@Override
 		public List<String> getSelectedOperations() {
 			return resourceAssignmentWidget.getSelectedOperations();
 		}
 
 		@Override
 		public Label getResourceTypeLabel() {
 			return resourceAssignmentWidget.getResourceTypeLabel();
 		}
 
 		@Override
 		public Label getResourceLevelLabel() {
 			return resourceAssignmentWidget.getResourceLevelLabel();
 		}
 
 		@Override
 		public Label getResourceNameLabel() {
 			return resourceAssignmentWidget.getResourceNameLabel();
 		}
 
 		@Override
 		public SelectBoxesWidget getSelectBoxesWidget() {
 			return resourceAssignmentWidget.getSelectBoxesWidget();
 		}
 
 		public void setAssignments(List<Resource> assignments) {
 			selections.clear();
 			List<Resource> data = dataProvider.getList();
 			data.clear();
 			if (assignments != null)
 				data.addAll(assignments);
 			cellTable.redraw();
 		}
 
 		@Override
 		public HasClickHandlers getEditButton() {
 			return editButton;
 		}
 
 		@Override
 		public List<String> getAvailableOperations() {
 			return resourceAssignmentWidget.getAvailableOperations();
 		}
 		
 	}
 
 	private class SubjectContentView extends AbstractGenericView implements
 			SubjectContentDisplay {
 		private SimplePanel mainPanel;
 		private Grid mainGrid;
 		private PolicySubjectAssignmentWidget subjectAssignmentWidget;
 		private PopupPanel subjectAssignmentPopup;
 		private FlowPanel subjectAssignmentButtonPanel;
 		private Button addSubjectButton;
 		private Button cancelSubjectButton;
 		private CellTable<PolicySubjectAssignment> cellTable;
 		private ProvidesKey<PolicySubjectAssignment> keyProvider;
 		private MultiSelectionModel<PolicySubjectAssignment> selectionModel;
 		private List<PolicySubjectAssignment> selections = new ArrayList<PolicySubjectAssignment>();
 		private ListDataProvider<PolicySubjectAssignment> dataProvider;
 		private Grid subjectGrid;
 		private Grid actionGrid;
 		private TurmericPager pager;
 		private PushButton editButton;
 		private PushButton delButton;
 		private List<UserAction> permissions;
 
 		public SubjectContentView() {
 			mainPanel = new SimplePanel();
 			mainGrid = new Grid(2, 1);
 			mainPanel.add(mainGrid);
 			initWidget(mainPanel);
 
 			initialize();
 		}
 
 		public void activate() {
 			// do nothing for now
 		}
 
 		@Override
 		public void initialize() {
 			
 			subjectGrid = new Grid(3, 1);
 			createButtonMenu();
 			subjectGrid.setWidget(0, 0, actionGrid);
 			createSubjectTableFields();
 			subjectGrid.setWidget(1, 0, cellTable);
 			subjectGrid.setWidget(2, 0, pager);
 
 			createSubjectAssignmentFields();
 			mainGrid.setWidget(1, 0, subjectGrid);
 			mainGrid.setWidth("60%");
 			
 			
 			
 		}
 
 		
 		
 		protected void createButtonMenu() {
 			// Edit (subject assignment) button - depends on POLICY_EDIT
 			// permission
 			// Delete (subject assignment) button - depends on POLICY_EDIT
 			// permission
 			// DeleteAll (subject assignment) button - depends on POLICY_EDIT
 			// permission
 			actionGrid = new Grid(1, 2);
 			actionGrid.setWidth("100%");
 
 			editButton = new PushButton("[ "
 					+ PolicyAdminUIUtil.policyAdminConstants.edit() + " ]");
 			editButton.setVisible(false);
 
 			delButton = new PushButton("[ "
 					+ PolicyAdminUIUtil.policyAdminConstants.delete() + " ]");
 			delButton.setWidth("60px");
 			delButton.setEnabled(false);
 
 			Anchor assignSubjectsClickable = new Anchor(
 					"[ "
 							+ PolicyAdminUIUtil.policyAdminConstants
 									.subjectAssignSubjects() + " ]");
 			assignSubjectsClickable.addClickHandler(new ClickHandler() {
 				@Override
 				public void onClick(ClickEvent event) {
 					resourceContentView.hide();
 					subjectAssignmentPopup.center();
 				}
 			});
 
 			actionGrid.setWidget(0, 0, delButton);
 			actionGrid.getCellFormatter().setWidth(0, 0, "20%");
 
 			actionGrid.setWidget(0, 1, assignSubjectsClickable);
 			actionGrid.getCellFormatter().setWidth(0, 1, "80%");
 			actionGrid.getCellFormatter().setAlignment(0, 1,
 					HasHorizontalAlignment.ALIGN_RIGHT,
 					HasVerticalAlignment.ALIGN_MIDDLE);
 
 		}
 
 		protected void createSubjectTableFields() {
 
 			// SubjectAssignment
 			// - SubjectType
 			// - List of Subject
 			// - List of exclusion Subject
 			// - List of SubjectGroup
 			// - List of exclusion SubjectGroup
 
 			keyProvider = new ProvidesKey<PolicySubjectAssignment>() {
 				public Object getKey(PolicySubjectAssignment assignment) {
 					return assignment == null ? null : assignment
 							.getSubjectType();
 				}
 			};
 
 			cellTable = new CellTable<PolicySubjectAssignment>(keyProvider);
 			selectionModel = new MultiSelectionModel<PolicySubjectAssignment>(
 					keyProvider);
 			cellTable.setSelectionModel(selectionModel);
 			selectionModel
 					.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
 						public void onSelectionChange(SelectionChangeEvent event) {
 							Set<PolicySubjectAssignment> x = selectionModel
 									.getSelectedSet();
 							selections.clear();
 							if (x != null)
 								selections.addAll(x);
 							enableActions();
 						}
 					});
 			dataProvider = new ListDataProvider<PolicySubjectAssignment>();
 			dataProvider.addDataDisplay(cellTable);
 
 			pager = new TurmericPager();
 			pager.setDisplay(cellTable);
 
 			// checkbox column for selection
 			Column<PolicySubjectAssignment, Boolean> checkColumn = new Column<PolicySubjectAssignment, Boolean>(
 					new CheckboxCell(true)) {
 				public Boolean getValue(PolicySubjectAssignment assignment) {
 					// Get the value from the selection model.
 					return selectionModel.isSelected(assignment);
 				}
 			};
 
 			checkColumn
 					.setFieldUpdater(new FieldUpdater<PolicySubjectAssignment, Boolean>() {
 						public void update(int index,
 								PolicySubjectAssignment assignment,
 								Boolean value) {
 							// Called when the user clicks on a checkbox.
 							selectionModel.setSelected(assignment, value);
 						}
 					});
 
 			final CheckboxCell cb = new CheckboxCell();
 			Header<Boolean> hdr = new Header<Boolean>(cb) {
 				boolean selected = false;
 
 				@Override
 				public void onBrowserEvent(Context context, Element elem,
 						NativeEvent event) {
 					super.onBrowserEvent(context, elem, event);
 					selected = !selected;
 					selectionModel.clear();
 					for (PolicySubjectAssignment visibleItem : cellTable
 							.getVisibleItems()) {
 						selectionModel.setSelected(visibleItem, selected);
 					}
 				}
 
 				@Override
 				public Boolean getValue() {
 					return false;
 				}
 			};
 
 			cellTable.addColumn(checkColumn, hdr);
 
 			// text column for type
 			ClickableTextCell sbTypeCellClickable = new ClickableTextCell();
 			Column<PolicySubjectAssignment, String> typeCol = createCell(
 					sbTypeCellClickable, new GetSubjectValue<String>() {
 						public String getValue(
 								PolicySubjectAssignment assignment) {
 							if (assignment == null
 									|| assignment.getSubjectType() == null) {
 								return null;
 							}
 							return assignment.getSubjectType();
 						}
 					}, new FieldUpdater<PolicySubjectAssignment, String>() {
 						public void update(int index,
 								PolicySubjectAssignment assignment, String value) {
 							selections.add(assignment);
 							editButton.fireEvent(new ClickEvent() {
 							});
 						}
 					});
 			cellTable.addColumn(typeCol,
 					PolicyAdminUIUtil.policyAdminConstants.subjectType());
 
 			// text column for Subject names
 			Column<PolicySubjectAssignment, List<String>> subjectNamesCol = new Column<PolicySubjectAssignment, List<String>>(
 					new CustomListCell(MIN_SCROLLBAR_SIZE)) {
 				public List<String> getValue(PolicySubjectAssignment assignment) {
 
 					if (assignment == null || assignment.getSubjects() == null) {
 						return null;
 					}
 					ArrayList<String> namesList = new ArrayList<String>();
 					for (Subject subject : assignment.getSubjects()) {
 						//name = null means apply all subject for that type 
 						namesList.add((subject.getName()==null ? PolicyAdminUIUtil.policyAdminConstants.all() : subject.getName()));
 					}
 
 					return namesList;
 				}
 			};
 
 			cellTable.addColumn(subjectNamesCol,
 					PolicyAdminUIUtil.policyAdminConstants.subjects());
 
 			// text column for SubjectGroup names
 			Column<PolicySubjectAssignment, List<String>> subjectGroupNamesCol = new Column<PolicySubjectAssignment, List<String>>(
 					new CustomListCell(MIN_SCROLLBAR_SIZE)) {
 				public List<String> getValue(PolicySubjectAssignment assignment) {
 
 					if (assignment == null
 							|| assignment.getSubjectGroups() == null) {
 						return null;
 					}
 					ArrayList<String> namesList = new ArrayList<String>();
 					for (SubjectGroup subjectGroup : assignment
 							.getSubjectGroups()) {
 						namesList.add(subjectGroup.getName());
 					}
 
 					return namesList;
 				}
 			};
 
 			cellTable.addColumn(subjectGroupNamesCol,
 					PolicyAdminUIUtil.policyAdminConstants.subjectGroups());
 		}
 
 		private <C> Column<PolicySubjectAssignment, C> createCell(Cell<C> cell,
 				final GetSubjectValue<C> getter,
 				FieldUpdater<PolicySubjectAssignment, C> fieldUpdater) {
 			Column<PolicySubjectAssignment, C> column = new Column<PolicySubjectAssignment, C>(
 					cell) {
 
 				@Override
 				public C getValue(PolicySubjectAssignment object) {
 					return getter.getValue(object);
 				}
 
 				@Override
 				public void render(Cell.Context context,
 						PolicySubjectAssignment object, SafeHtmlBuilder sb) {
 					sb.appendHtmlConstant("<a href='javascript:void(0);'>");
 					super.render(context, object, sb);
 					sb.appendHtmlConstant("</a>");
 				}
 			};
 			column.setFieldUpdater(fieldUpdater);
 			return column;
 		}
 
 		protected boolean  enableAddSubjectButton(){
 			return (this.getSelectAllSubjects()
 					|| !this.getSelectedSubjects().isEmpty()
 					|| !this.getSelectedExclusionSubjects().isEmpty()
 					|| !this.getSelectedSubjectGroups().isEmpty()
 					|| !this.getSelectedExclusionSG().isEmpty());
 		}
 		
 		protected void createSubjectAssignmentFields() {
 			subjectAssignmentWidget = new PolicySubjectAssignmentWidget();
 
 			subjectAssignmentPopup = new PopupPanel();
 			subjectAssignmentPopup.setGlassEnabled(true);
 			subjectAssignmentPopup.setWidth("1000px");
 			subjectAssignmentPopup.setHeight("300px");
 
 			addSubjectButton = new Button(PolicyAdminUIUtil.constants.apply(),
 					new ClickHandler() {
 
 						@Override
 						public void onClick(ClickEvent paramClickEvent) {
 							if(enableAddSubjectButton()){
 								subjectAssignmentPopup.hide();
 							}
 						}
 					});
 			cancelSubjectButton = new Button(
 					PolicyAdminUIUtil.constants.cancel());
 
 			subjectAssignmentButtonPanel = new FlowPanel();
 			subjectAssignmentButtonPanel.add(addSubjectButton);
 			subjectAssignmentButtonPanel.add(cancelSubjectButton);
 
 			Grid subjectAssignmentGrid = new Grid(3, 1);
 			
 			Label title = new Label(
 					PolicyAdminUIUtil.policyAdminConstants
 							.assignSubjectsAndSubjectGroups());
 			title.setAutoHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
 			subjectAssignmentGrid.setWidget(0, 0, title);
 
 			subjectAssignmentGrid.setWidget(1, 0, subjectAssignmentWidget);
 			subjectAssignmentGrid.getCellFormatter().setVerticalAlignment(1, 0,
 					HasVerticalAlignment.ALIGN_TOP);
 			
 			subjectAssignmentGrid.setWidget(2, 0, subjectAssignmentButtonPanel);
 			subjectAssignmentGrid.getCellFormatter().setVerticalAlignment(2, 0,
 					HasVerticalAlignment.ALIGN_BOTTOM);
 			subjectAssignmentGrid.getCellFormatter().setHorizontalAlignment(2,
 					0, HasHorizontalAlignment.ALIGN_CENTER);
 
 			subjectAssignmentPopup.add(subjectAssignmentGrid);
 
 		}
 
 		public void show() {
 			subjectAssignmentPopup.center();
 		}
 
 		public void hide() {
 			subjectAssignmentWidget.clear();
 			subjectAssignmentPopup.hide();
 		}
 
 		public List<PolicySubjectAssignment> getSelectedSubjectAssignments() {
 			return selections;
 		}
 
 		public CellTable<PolicySubjectAssignment> getCellTable() {
 			return cellTable;
 		}
 
 		public HasClickHandlers getEditButton() {
 			return editButton;
 		}
 
 		public HasClickHandlers getDelButton() {
 			return delButton;
 		}
 
 		public void setUserActions(List<UserAction> actions) {
 			permissions = actions;
 			enableActions();
 		}
 
 		public void setExclusionListVisible(boolean visible) {
 			subjectAssignmentWidget.setExclusionListVisible(visible);
 		}
 
 		public void setAssignments(List<PolicySubjectAssignment> assignments) {
 			HashSet<PolicySubjectAssignment> tmp = new HashSet(
 					selectionModel.getSelectedSet());
 			for (PolicySubjectAssignment a : tmp) {
 				selectionModel.setSelected(a, false);
 			}
 			selections.clear();
 
 			List<PolicySubjectAssignment> list = (assignments == null ? new ArrayList<PolicySubjectAssignment>()
 					: new ArrayList<PolicySubjectAssignment>(assignments));
 			dataProvider.setList(list);
 			dataProvider.refresh();
 
 			cellTable.setRowCount(list.size());
 			cellTable.redraw();
 		}
 
 		public List<PolicySubjectAssignment> getAssignments() {
 			return dataProvider.getList();
 		}
 
 		public HasClickHandlers getAddButton() {
 			return addSubjectButton;
 		}
 
 		public HasClickHandlers getCancelButton() {
 			return cancelSubjectButton;
 		}
 
 		public void setAvailableSubjectTypes(List<String> availableSubjectTypes) {
 			subjectAssignmentWidget
 					.setAvailableSubjectTypes(availableSubjectTypes);
 		}
 
 		public MultiSelectionModel<PolicySubjectAssignment> getSelectionModel() {
 			return selectionModel;
 		}
 		
 		public void setAvailableSubjectGroups(List<String> list) {
 			subjectAssignmentWidget.setAvailableGroups(list);
 		}
 
 		public HasClickHandlers getGroupSearchButton() {
 			return subjectAssignmentWidget.getGroupSearchButton();
 		}
 
 		public String getGroupSearchTerm() {
 			return subjectAssignmentWidget.getGroupSearchTerm();
 		}
 
 		public String getSubjectSearchTerm() {
 			return subjectAssignmentWidget.getSearchTerm();
 		}
 
 		public String getSubjectType() {
 			return subjectAssignmentWidget.getSubjectType();
 		}
 		
 		public boolean getSelectAllSubjects() {
 			return subjectAssignmentWidget.getSelectAllSubjectsXB();
 		}
 		
 		public void setSelectAllSubjects(boolean selected) {
 			subjectAssignmentWidget.setSelectAllSubjectsXB(selected);
 		}
 		
 		/**
 		 * @see org.ebayopensource.turmeric.policy.adminui.client.presenter.policy.PolicyCreatePresenter.SubjectContentDisplay#getSelectedSubjectGroups()
 		 */
 		@Override
 		public List<String> getSelectedSubjectGroups() {
 			return subjectAssignmentWidget.getSelectedGroups();
 		}
 
 		/**
 		 * @see org.ebayopensource.turmeric.policy.adminui.client.presenter.policy.PolicyCreatePresenter.SubjectContentDisplay#getSelectedSubjects()
 		 */
 		@Override
 		public List<String> getSelectedSubjects() {
 			return subjectAssignmentWidget.getSelectedSubjects();
 		}
 
 		/**
 		 * @see org.ebayopensource.turmeric.policy.adminui.client.presenter.policy.PolicyCreatePresenter.SubjectContentDisplay#getSubjectSearchButton()
 		 */
 		@Override
 		public HasClickHandlers getSubjectSearchButton() {
 			return subjectAssignmentWidget.getSearchButton();
 		}
 
 		/**
 		 * @see org.ebayopensource.turmeric.policy.adminui.client.presenter.policy.PolicyCreatePresenter.SubjectContentDisplay#setAvailableSubjects(java.util.List)
 		 */
 		@Override
 		public void setAvailableSubjects(List<String> list) {
 			subjectAssignmentWidget.setAvailableSubjects(list);
 		}
 
 		@Override
 		public void clearAssignmentWidget() {
 			subjectAssignmentWidget.clear();
 		}
 
 		private void enableActions() {
 			boolean permitted = permissions != null
 					&& permissions.contains(UserAction.POLICY_EDIT);
 			delButton.setEnabled(permitted && selections.size() >= 1);
 		}
 
 		/**
 		 * @see org.ebayopensource.turmeric.policy.adminui.client.presenter.policy.PolicyCreatePresenter.SubjectContentDisplay#setSelectedSubjectGroups(java.util.List)
 		 */
 		@Override
 		public void setSelectedSubjectGroups(List<String> list) {
 			subjectAssignmentWidget.setSelectedGroups(list);
 		}
 
 		/**
 		 * @see org.ebayopensource.turmeric.policy.adminui.client.presenter.policy.PolicyCreatePresenter.SubjectContentDisplay#setSelectedSubjects(java.util.List)
 		 */
 		@Override
 		public void setSelectedSubjects(List<String> list) {
 			subjectAssignmentWidget.setSelectedSubjects(list);
 		}
 
 		@Override
 		public List<String> getSelectedExclusionSubjects() {
 			return subjectAssignmentWidget.getSelectedExclusionSubjects();
 		}
 
 		@Override
 		public List<String> getSelectedExclusionSG() {
 			return subjectAssignmentWidget.getSelectedExclusionSG();
 		}
 
 		@Override
 		public void setAvailableExclusionSubjects(List<String> list) {
 			subjectAssignmentWidget.setAvailableExclusionSubjects(list);
 		}
 
 		/**
 		 * @see org.ebayopensource.turmeric.policy.adminui.client.presenter.policy.PolicyCreatePresenter.SubjectContentDisplay#setSelectedExclusionSubjects(java.util.List)
 		 */
 		@Override
 		public void setSelectedExclusionSubjects(final List<String> list) {
 			subjectAssignmentWidget.setSelectedExclusionSubjects(list);
 		}
 
 		@Override
 		public void setAvailableExclusionSG(List<String> list) {
 			subjectAssignmentWidget.setAvailableExclusionSG(list);
 		}
 
 		@Override
 		public void setSelectedExclusionSG(final List<String> list) {
 			subjectAssignmentWidget.setSelectedExclusionSG(list);
 		}
 
 	}
 
 	/* (non-Javadoc)
 	 * @see org.ebayopensource.turmeric.policy.adminui.client.Display#activate()
 	 */
 	public void activate() {
 		contentView.activate();
 		this.setVisible(true);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.ebayopensource.turmeric.policy.adminui.client.presenter.policy.PolicyCreatePresenter.PolicyCreateDisplay#getPolicyName()
 	 */
 	public TextBox getPolicyName() {
 		return policyName;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.ebayopensource.turmeric.policy.adminui.client.presenter.policy.PolicyCreatePresenter.PolicyCreateDisplay#getPolicyEnabled()
 	 */
 	public boolean getPolicyEnabled() {
 		return policyEnabled;
 	}
 
 	/**
 	 * Gets the policy status list.
 	 * 
 	 * @return the policy status list
 	 */
 	public ListBox getPolicyStatusList() {
 		return policyStatus;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.ebayopensource.turmeric.policy.adminui.client.presenter.policy.PolicyCreatePresenter.PolicyCreateDisplay#getPolicyDesc()
 	 */
 	public TextArea getPolicyDesc() {
 		return policyDesc;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.ebayopensource.turmeric.policy.adminui.client.view.common.PolicyTemplateDisplay.PolicyPageTemplateDisplay#getContentView()
 	 */
 	public Display getContentView() {
 		return contentView;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.ebayopensource.turmeric.policy.adminui.client.presenter.policy.PolicyCreatePresenter.PolicyCreateDisplay#getResourceContentView()
 	 */
 	public ResourcesContentDisplay getResourceContentView() {
 		return resourceContentView;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.ebayopensource.turmeric.policy.adminui.client.presenter.policy.PolicyCreatePresenter.PolicyCreateDisplay#getSaveButton()
 	 */
 	public Button getSaveButton() {
 		return saveButton;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.ebayopensource.turmeric.policy.adminui.client.presenter.policy.PolicyCreatePresenter.PolicyCreateDisplay#getCancelButton()
 	 */
 	public Button getCancelButton() {
 		return cancelButton;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.ebayopensource.turmeric.policy.adminui.client.presenter.policy.PolicyCreatePresenter.PolicyCreateDisplay#getSubjectContentView()
 	 */
 	@Override
 	public SubjectContentDisplay getSubjectContentView() {
 		return subjectContentView;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.ebayopensource.turmeric.policy.adminui.client.presenter.policy.PolicyCreatePresenter.PolicyCreateDisplay#setUserActions(java.util.List)
 	 */
 	@Override
 	public void setUserActions(List<UserAction> permittedActions) {
 		resourceContentView.setUserActions(permittedActions);
 		subjectContentView.setUserActions(permittedActions);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.ebayopensource.turmeric.policy.adminui.client.presenter.policy.PolicyCreatePresenter.PolicyCreateDisplay#clear()
 	 */
 	public void clear() {
 		policyName.setText("");
 		policyDesc.setText("");
 		policyStatus.setSelectedIndex(-1);
 		this.resourceContentView.clearAssignmentWidget();
 		this.subjectContentView.clearAssignmentWidget();
 		this.resourceContentView.setAssignments(Collections.EMPTY_LIST);
 		this.subjectContentView.setAssignments(Collections.EMPTY_LIST);
 		((ContentView) contentView).conditionWidget.clearConditionBuilder();
 
 	}
 
 	/* (non-Javadoc)
 	 * @see org.ebayopensource.turmeric.policy.adminui.client.presenter.policy.PolicyCreatePresenter.PolicyCreateDisplay#error(java.lang.String)
 	 */
 	public void error(String msg) {
 		ErrorDialog dialog = new ErrorDialog(true);
 		dialog.setMessage(msg);
 		dialog.getDialog().center();
 		dialog.show();
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.ebayopensource.turmeric.policy.adminui.client.presenter.policy.PolicyCreatePresenter.PolicyCreateDisplay#info(java.lang.String)
 	 */
 	public void info(String msg) {
 		InfoDialog dialog = new InfoDialog(true);
 		dialog.setMessage(msg);
 		dialog.getDialog().center();
 		dialog.show();
 
 	}
 	
 
 	/* (non-Javadoc)
 	 * @see org.ebayopensource.turmeric.policy.adminui.client.presenter.policy.PolicyCreatePresenter.PolicyCreateDisplay#getExtraFieldValue(int)
 	 */
 	@Override
 	public String getExtraFieldValue(int order) {
 		String value = null;
 		if (extraFieldsGrid.getWidget(order, 1) instanceof TextBox) {
 			value = ((TextBox) extraFieldsGrid.getWidget(order, 1)).getText();
 
 		} else if (extraFieldsGrid.getWidget(order, 1) instanceof TextArea) {
 			value = ((TextArea) extraFieldsGrid.getWidget(order, 1)).getText();
 
 		} else if (extraFieldsGrid.getWidget(order, 1) instanceof CheckBox) {
 			Boolean booleanValue = ((CheckBox) extraFieldsGrid.getWidget(order,
 					1)).getValue();
 			value = (String.valueOf(booleanValue));
 
 		} else if (extraFieldsGrid.getWidget(order, 1) instanceof ListBox) {
 			ListBox list = (ListBox) extraFieldsGrid.getWidget(order, 1);
 			int index = list.getSelectedIndex();
 			if (index >= 0) {
 				value = list.getItemText(index);
 			}
 		}
 		return value;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.ebayopensource.turmeric.policy.adminui.client.presenter.policy.PolicyCreatePresenter.PolicyCreateDisplay#setExtraFieldValue(int, java.lang.String, boolean)
 	 */
 	@Override
 	public void setExtraFieldValue(int order, String value, boolean append) {
 		// textArea widet type
 		if (extraFieldsGrid.getWidget(order, 1) instanceof TextArea) {
 			TextArea text = (TextArea) extraFieldsGrid.getWidget(order, 1);
 			if (append) {
 				value = text.getText() + value;
 			}
 			text.setText(value);
 
 		} else if (extraFieldsGrid.getWidget(order, 1) instanceof TextBox) {
 			TextBox text = (TextBox) extraFieldsGrid.getWidget(order, 1);
 			text.setText(value);
 
 		} else if (extraFieldsGrid.getWidget(order, 1) instanceof ListBox) {
 			ListBox list = (ListBox) extraFieldsGrid.getWidget(order, 1);
 			for (int i = 0; i < list.getItemCount(); i++) {
 				if (value.equalsIgnoreCase(list.getItemText(i))) {
 					list.setSelectedIndex(i);
 					break;
 				}
 			}
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.ebayopensource.turmeric.policy.adminui.client.presenter.policy.PolicyCreatePresenter.PolicyCreateDisplay#getAddConditionButton()
 	 */
 	@Override
 	public HasClickHandlers getAddConditionButton() {
 		return ((ContentView) contentView).conditionWidget
 				.getAddConditionButton();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.ebayopensource.turmeric.policy.adminui.client.presenter.policy.PolicyCreatePresenter.PolicyCreateDisplay#getRsListBox()
 	 */
 	@Override
 	public HasChangeHandlers getRsListBox() {
 		return ((ContentView) contentView).conditionWidget.getRsListBox();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.ebayopensource.turmeric.policy.adminui.client.presenter.policy.PolicyCreatePresenter.PolicyCreateDisplay#getQuantityBox()
 	 */
 	@Override
 	public String getQuantityBox() {
 		return ((ContentView) contentView).conditionWidget.getQuantityBox();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.ebayopensource.turmeric.policy.adminui.client.presenter.policy.PolicyCreatePresenter.PolicyCreateDisplay#getRsNameSelected()
 	 */
 	@Override
 	public String getRsNameSelected() {
 		return ((ContentView) contentView).conditionWidget.getRsNameSelected();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.ebayopensource.turmeric.policy.adminui.client.presenter.policy.PolicyCreatePresenter.PolicyCreateDisplay#getOpNameSelected()
 	 */
 	@Override
 	public String getOpNameSelected() {
 		return ((ContentView) contentView).conditionWidget.getOpNameSelected();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.ebayopensource.turmeric.policy.adminui.client.presenter.policy.PolicyCreatePresenter.PolicyCreateDisplay#getConditionSelected()
 	 */
 	@Override
 	public String getConditionSelected() {
 		return ((ContentView) contentView).conditionWidget
 				.getConditionSelected();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.ebayopensource.turmeric.policy.adminui.client.presenter.policy.PolicyCreatePresenter.PolicyCreateDisplay#getAritmSignSelected()
 	 */
 	@Override
 	public String getAritmSignSelected() {
 		return ((ContentView) contentView).conditionWidget
 				.getAritmSignSelected();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.ebayopensource.turmeric.policy.adminui.client.presenter.policy.PolicyCreatePresenter.PolicyCreateDisplay#getLogicOpSelected()
 	 */
 	@Override
 	public String getLogicOpSelected() {
 		return ((ContentView) contentView).conditionWidget.getLogicOpSelected();
 	}
 
 	@Override
 	public String getSubjectOrSGSelected() {
 		return ((ContentView) contentView).conditionWidget.getSubjectOrSGSelected();
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.ebayopensource.turmeric.policy.adminui.client.presenter.policy.PolicyCreatePresenter.PolicyCreateDisplay#setRsNames(java.util.List)
 	 */
 	@Override
 	public void setRsNames(List<String> names) {
 		((ContentView) contentView).conditionWidget.setRsNames(names);
 		((ContentView) contentView).conditionWidget.initializeRsListBox();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.ebayopensource.turmeric.policy.adminui.client.presenter.policy.PolicyCreatePresenter.PolicyCreateDisplay#setOpNames(java.util.List)
 	 */
 	@Override
 	public void setOpNames(List<String> names) {
 		((ContentView) contentView).conditionWidget.setOpNames(names);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.ebayopensource.turmeric.policy.adminui.client.presenter.policy.PolicyCreatePresenter.PolicyCreateDisplay#setConditionNames(java.util.List)
 	 */
 	@Override
 	public void setConditionNames(List<String> conditions) {
 		((ContentView) contentView).conditionWidget
 				.setConditionNames(conditions);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.ebayopensource.turmeric.policy.adminui.client.presenter.policy.PolicyCreatePresenter.PolicyCreateDisplay#setPolicyStatus(boolean)
 	 */
 	@Override
 	public void setPolicyStatus(final boolean enabled) {
 		this.policyStatus.setSelectedIndex(1);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.ebayopensource.turmeric.policy.adminui.client.presenter.policy.PolicyCreatePresenter.PolicyCreateDisplay#setPolicyType(java.lang.String)
 	 */
 	@Override
 	public void setPolicyType(final String policyType) {
 		if ("RL".equalsIgnoreCase(policyType)) {
 			CellTable<PolicySubjectAssignment> cellTable = ((SubjectContentView) subjectContentView)
 					.getCellTable();
 
 			// text column for Exclusion Subject names
 			Column<PolicySubjectAssignment, List<String>> exclusionSubjectNamesCol = new Column<PolicySubjectAssignment, List<String>>(
 					new CustomListCell(MIN_SCROLLBAR_SIZE)) {
 				public List<String> getValue(PolicySubjectAssignment assignment) {
 
 					if (assignment == null
 							|| assignment.getExclusionSubjects() == null) {
 						return null;
 					}
 					ArrayList<String> namesList = new ArrayList<String>();
 					for (Subject subject : assignment.getExclusionSubjects()) {
 						namesList.add(subject.getName());
 					}
 
 					return namesList;
 				}
 			};
 
		
			if(cellTable.getColumnCount() < 6){
				cellTable.addColumn(exclusionSubjectNamesCol,
						PolicyAdminUIUtil.policyAdminConstants.exclusionSubjects());
			}
			
 			// text column for Exclusion Subject Group names
 			Column<PolicySubjectAssignment, List<String>> exclusionSGNamesCol = new Column<PolicySubjectAssignment, List<String>>(
 					new CustomListCell(MIN_SCROLLBAR_SIZE)) {
 				public List<String> getValue(PolicySubjectAssignment assignment) {
 
 					if (assignment == null
 							|| assignment.getExclusionSubjectGroups() == null) {
 						return null;
 					}
 					ArrayList<String> namesList = new ArrayList<String>();
 					for (SubjectGroup subjectGroup : assignment
 							.getExclusionSubjectGroups()) {
 						namesList.add(subjectGroup.getName());
 					}
 
 					return namesList;
 				}
 			};
			
			if(cellTable.getColumnCount() < 6){
				cellTable.addColumn(exclusionSGNamesCol,
						PolicyAdminUIUtil.policyAdminConstants
						.exclusionSubjectGroups());
			}
				
 		}
 		this.policyType.setText(policyType);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.ebayopensource.turmeric.policy.adminui.client.presenter.policy.PolicyCreatePresenter.PolicyCreateDisplay#clearConditionBuilder()
 	 */
 	@Override
 	public void clearConditionBuilder() {
 		((ContentView) contentView).conditionWidget.clearConditionBuilder();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.ebayopensource.turmeric.policy.adminui.client.presenter.policy.PolicyCreatePresenter.PolicyCreateDisplay#setConditionBuilderVisible(boolean)
 	 */
 	@Override
 	public void setConditionBuilderVisible(boolean visible) {
 		((ContentView) contentView).conditionPanel.setVisible(visible);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.ebayopensource.turmeric.policy.adminui.client.presenter.policy.PolicyCreatePresenter.PolicyCreateDisplay#setExclusionListsVisible(boolean)
 	 */
 	@Override
 	public void setExclusionListsVisible(boolean visible) {
 		((SubjectContentView) subjectContentView)
 				.setExclusionListVisible(visible);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.ebayopensource.turmeric.policy.adminui.client.presenter.policy.PolicyCreatePresenter.PolicyCreateDisplay#validAllConditionFields()
 	 */
 	public boolean validAllConditionFields() {
 		return ((ContentView) contentView).conditionWidget.validAllFields();
 	}
 
 
 	@Override
 	public void addSaveButtonClickHandler(ClickHandler clickHandler) {
 		getSaveButton().addClickHandler(clickHandler);		
 	}
 
 
 	@Override
 	public void addCancelButtonClickHandler(ClickHandler clickHandler) {
 		getCancelButton().addClickHandler(clickHandler);		
 	}
 
 	/**
 	 * RESOURCE SECTION
 	 */
 	
 	@Override
 	public void addRsResourceLevelBoxChange(ChangeHandler changeHandler) {
 		getResourceContentView().getResourceLevelBox().addChangeHandler(
 				changeHandler);
 	}
 
 	@Override
 	public void addRsEditButtonClick(ClickHandler clickHandler) {
 		getResourceContentView().getEditButton().addClickHandler(clickHandler);
 		
 	}
 
 	@Override
 	public void addRsCancelResourceButtonClick(ClickHandler clickHandler) {
 		getResourceContentView().getCancelResourceButton()
 		.addClickHandler(clickHandler);	
 	}
 	
 	@Override
 	public void addRsResourceTypeBoxChange(ChangeHandler changeHandler) {
 		getResourceContentView().getResourceTypeBox()
 		.addChangeHandler(changeHandler);
 	}
 	
 	@Override
 	public void addRsResourceNameBoxClick(ClickHandler clickHandler) {
 		getResourceContentView().getResourceNameBox()
 		.addClickHandler(clickHandler);	
 	}
 
 	@Override
 	public void addRsResourceButtonClick(ClickHandler clickHandler) {
 		getResourceContentView().getAddResourceButton()
 		.addClickHandler(clickHandler);
 	}
 	
 	@Override
 	public void addRsDelButtonClick(ClickHandler clickHandler) {
 		getResourceContentView().getDelButton().addClickHandler(clickHandler);
 	}
 
 	/**
 	 * SUBJECT SECTION
 	 */
 	@Override
 	public void addSbGroupSearchButtonClickHandler(ClickHandler clickHandler) {
 		getSubjectContentView().getGroupSearchButton()
 		.addClickHandler(clickHandler);	
 	}
 	
 	@Override
 	public void addSbSubjectSearchButtonClickHandler(ClickHandler clickHandler) {
 		getSubjectContentView().getSubjectSearchButton()
 		.addClickHandler(clickHandler);		
 	}
 	
 	@Override
 	public void addSbEditButtonClickHandler(ClickHandler clickHandler) {
 		getSubjectContentView().getEditButton()
 		.addClickHandler(clickHandler);		
 	}
 
 	@Override
 	public void addSbDelButtonClickHandler(ClickHandler clickHandler) {
 		getSubjectContentView().getDelButton()
 		.addClickHandler(clickHandler);		
 	}
 
 	@Override
 	public void addSbAddButtonClickHandler(ClickHandler clickHandler) {
 		getSubjectContentView().getAddButton()
 		.addClickHandler(clickHandler);		
 	}
 
 	@Override
 	public void addSbCancelButtonClickHandler(ClickHandler clickHandler) {
 		getSubjectContentView().getCancelButton()
 		.addClickHandler(clickHandler);		
 	}
 	
 	@Override
 	public void addAddConditionButtonClickHandler(ClickHandler clickHandler) {
 		//do nothing
 	}
 	
 	@Override
 	public void addRsListBoxChangeHandler(ChangeHandler changeHandler) {
 		//do nothing		
 	}
 }
