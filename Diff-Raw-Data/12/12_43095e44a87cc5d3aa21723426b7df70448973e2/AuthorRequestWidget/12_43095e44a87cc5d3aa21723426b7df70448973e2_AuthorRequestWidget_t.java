 /*******************************************************************************
  * Copyright (c) 2013 Pronoia Health LLC.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Pronoia Health LLC - initial API and implementation
  *******************************************************************************/
 package com.pronoiahealth.olhie.client.pages.admin.widgets;
 
 import java.util.Arrays;
 import java.util.Date;
 import java.util.List;
 
 import javax.annotation.PostConstruct;
 import javax.enterprise.context.Dependent;
 import javax.enterprise.event.Event;
 import javax.enterprise.event.Observes;
 import javax.inject.Inject;
 
 import org.jboss.errai.ui.shared.api.annotations.DataField;
 import org.jboss.errai.ui.shared.api.annotations.EventHandler;
 import org.jboss.errai.ui.shared.api.annotations.Templated;
 
 import com.github.gwtbootstrap.client.ui.Button;
 import com.github.gwtbootstrap.client.ui.Label;
 import com.google.gwt.cell.client.DateCell;
 import com.google.gwt.cell.client.FieldUpdater;
 import com.google.gwt.cell.client.SelectionCell;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.dom.client.Style.Unit;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.user.cellview.client.CellTable;
 import com.google.gwt.user.cellview.client.Column;
 import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
 import com.google.gwt.user.cellview.client.SimplePager;
 import com.google.gwt.user.cellview.client.SimplePager.TextLocation;
 import com.google.gwt.user.cellview.client.TextColumn;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.SimplePanel;
 import com.google.gwt.view.client.DefaultSelectionEventManager;
 import com.google.gwt.view.client.ListDataProvider;
 import com.google.gwt.view.client.ProvidesKey;
 import com.google.gwt.view.client.SelectionModel;
 import com.google.gwt.view.client.SingleSelectionModel;
 import com.pronoiahealth.olhie.client.shared.constants.RequestFormAuthorApproval;
 import com.pronoiahealth.olhie.client.shared.events.admin.AuthorPendingRequestEvent;
 import com.pronoiahealth.olhie.client.shared.events.admin.AuthorPendingResponseEvent;
 import com.pronoiahealth.olhie.client.shared.events.admin.AuthorRequestStatusChangeEvent;
 import com.pronoiahealth.olhie.client.shared.vo.RegistrationForm;
 
 /**
  * AuthorRequestWidget.java<br/>
  * Responsibilities:<br/>
  * 1.
  * 
  * @author John DeStefano
  * @version 1.0
  * @since Jan 19, 2014
  * 
  */
 @Templated("#root")
 @Dependent
 public class AuthorRequestWidget extends Composite {
 
 	private CellTable<RegistrationForm> authorReqGrid;
 	private SimplePager pager;
 	private ListDataProvider<RegistrationForm> dataProvider;
 	private static final String authStatus[] = new String[] {
 			RequestFormAuthorApproval.PENDING.toString(),
 			RequestFormAuthorApproval.APPROVED.toString(),
 			RequestFormAuthorApproval.DENIED.toString() };
 
 	@Inject
 	@DataField
 	private SimplePanel authorRequestTbl;
 
 	@Inject
 	@DataField
 	private SimplePanel pagerContainer;
 
 	@Inject
 	@DataField
 	private Button refreshBtn;
 
 	/**
 	 * The key provider that provides the unique ID of a contact.
 	 */
 	public static final ProvidesKey<RegistrationForm> KEY_PROVIDER = new ProvidesKey<RegistrationForm>() {
 		@Override
 		public Object getKey(RegistrationForm item) {
 			return item == null ? null : item.getId();
 		}
 	};
 
 	@Inject
 	private Event<AuthorPendingRequestEvent> authorPendingRequestEvent;
 
 	@Inject
 	private Event<AuthorRequestStatusChangeEvent> authorRequestStatusChangeEvent;
 
 	/**
 	 * Constructor
 	 * 
 	 */
 	public AuthorRequestWidget() {
 	}
 
 	/**
 	 * Build grid
 	 */
 	@PostConstruct
 	protected void postConstruct() {
 
 		// Data Provider
 		dataProvider = new ListDataProvider<RegistrationForm>();
 
 		// Grid configuration
 		authorReqGrid = new CellTable<RegistrationForm>(10, KEY_PROVIDER);
 		authorReqGrid.setWidth("100%");
 		authorReqGrid.setAutoHeaderRefreshDisabled(true);
 		authorReqGrid.setAutoFooterRefreshDisabled(true);
 		authorReqGrid.setEmptyTableWidget(new Label("Nothing to show"));
 		authorReqGrid
 				.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.ENABLED);
 		authorReqGrid.setWidth("100%", true);
 
 		// Selection Model
 		final SelectionModel<RegistrationForm> selectionModel = new SingleSelectionModel<RegistrationForm>(
 				KEY_PROVIDER);
 		authorReqGrid.setSelectionModel(selectionModel,
 				DefaultSelectionEventManager
 						.<RegistrationForm> createCheckboxManager());
 
 		// Init columns
 		// Id
 		TextColumn<RegistrationForm> idColumn = new TextColumn<RegistrationForm>() {
 			@Override
 			public String getValue(RegistrationForm object) {
 				return object.getId();
 			}
 		};
 		idColumn.setCellStyleNames("ph-Admin-tbl-cell-text-align-left");
 		authorReqGrid.addColumn(idColumn, "ID");
 		authorReqGrid.setColumnWidth(idColumn, 5, Unit.PCT);
 
 		// First Name
 		TextColumn<RegistrationForm> firstColumn = new TextColumn<RegistrationForm>() {
 			@Override
 			public String getValue(RegistrationForm object) {
 				return object.getFirstName();
 			}
 		};
 		firstColumn.setCellStyleNames("ph-Admin-tbl-cell-text-align-left");
 		authorReqGrid.addColumn(firstColumn, "First");
 		authorReqGrid.setColumnWidth(firstColumn, 7, Unit.PCT);
 
 		// Last Name
 		TextColumn<RegistrationForm> lastColumn = new TextColumn<RegistrationForm>() {
 			@Override
 			public String getValue(RegistrationForm object) {
 				return object.getLastName();
 			}
 		};
 		lastColumn.setCellStyleNames("ph-Admin-tbl-cell-text-align-left");
 		authorReqGrid.addColumn(lastColumn, "Last");
 		authorReqGrid.setColumnWidth(lastColumn, 12, Unit.PCT);
 
 		// userId
 		TextColumn<RegistrationForm> userIdColumn = new TextColumn<RegistrationForm>() {
 			@Override
 			public String getValue(RegistrationForm object) {
 				return object.getUserId();
 			}
 		};
 		userIdColumn.setCellStyleNames("ph-Admin-tbl-cell-text-align-left");
 		authorReqGrid.addColumn(userIdColumn, "UserId");
 		authorReqGrid.setColumnWidth(userIdColumn, 10, Unit.PCT);
 
 		// eMail
 		TextColumn<RegistrationForm> eMailColumn = new TextColumn<RegistrationForm>() {
 			@Override
 			public String getValue(RegistrationForm object) {
 				return object.getEmail();
 			}
 		};
 		eMailColumn.setCellStyleNames("ph-Admin-tbl-cell-text-align-left");
 		authorReqGrid.addColumn(eMailColumn, "eMail");
 		authorReqGrid.setColumnWidth(eMailColumn, 15, Unit.PCT);
 
 		// Registration Date
 		DateCell dateCell = new DateCell();
 		Column<RegistrationForm, Date> dateColumn = new Column<RegistrationForm, Date>(
 				dateCell) {
 			@Override
 			public Date getValue(RegistrationForm object) {
 				return object.getRegDate();
 			}
 		};
 		dateColumn.setCellStyleNames("ph-Admin-tbl-cell-text-align-left");
 		authorReqGrid.addColumn(dateColumn, "Reg Date");
 		authorReqGrid.setColumnWidth(dateColumn, 17, Unit.PCT);
 
 		// Organization
 		TextColumn<RegistrationForm> orgColumn = new TextColumn<RegistrationForm>() {
 			@Override
 			public String getValue(RegistrationForm object) {
 				return object.getOrganization();
 			}
 		};
 		orgColumn.setCellStyleNames("ph-Admin-tbl-cell-text-align-left");
 		authorReqGrid.addColumn(orgColumn, "Organization");
 		authorReqGrid.setColumnWidth(orgColumn, 15, Unit.PCT);
 
 		// Status
 		List<String> statusNames = Arrays.asList(authStatus);
 		SelectionCell statusCell = new SelectionCell(statusNames);
 		Column<RegistrationForm, String> statusColumn = new Column<RegistrationForm, String>(
 				statusCell) {
 			@Override
 			public String getValue(RegistrationForm object) {
 				return object.getAuthorStatus();
 			}
 		};
 		statusColumn.setCellStyleNames("ph-Admin-tbl-cell-text-align-left");
 		authorReqGrid.addColumn(statusColumn, "Status");
 
 		// Status column updated
 		statusColumn
 				.setFieldUpdater(new FieldUpdater<RegistrationForm, String>() {
 					@Override
 					public void update(int index, RegistrationForm object,
 							String value) {
 						object.setAuthorStatus(value);
 						dataProvider.refresh();
 
 						// Tell the AdminService about the change
 						authorRequestStatusChangeEvent
 								.fire(new AuthorRequestStatusChangeEvent(object
 										.getId(), value));
 					}
 				});
		authorReqGrid.setColumnWidth(statusColumn, 19, Unit.PCT);
 
 		// Set the dataprovider
 		dataProvider.addDataDisplay(authorReqGrid);
 
 		// Pager
 		SimplePager.Resources pagerResources = GWT
 				.create(SimplePager.Resources.class);
 		pager = new SimplePager(TextLocation.CENTER, pagerResources, false, 0,
 				true);
 		pager.setDisplay(authorReqGrid);
 		pager.setPageSize(10);
 		pager.getElement().setAttribute("style", "margin: auto;");
 
 		// Redraw the grid
 		authorReqGrid.redraw();
 		authorReqGrid.setVisible(true);
 
 		// Add to container
 		authorRequestTbl.add(authorReqGrid);
 		pagerContainer.add(pager);
 	}
 
 	/**
 	 * Click event - Refresh the data
 	 * 
 	 * @param evt
 	 */
 	@EventHandler("refreshBtn")
 	private void handleRefreshClick(ClickEvent evt) {
 		authorPendingRequestEvent.fire(new AuthorPendingRequestEvent());
 	}
 
 	/**
 	 * Get the list of unprocessed request
 	 */
 	@Override
 	protected void onLoad() {
 		super.onLoad();
 		authorPendingRequestEvent.fire(new AuthorPendingRequestEvent());
 	}
 
 	/**
 	 * Add the unprocessed requests to the cell table
 	 * 
 	 * @param authorPendingResponseEvent
 	 */
 	protected void observesAuthorPendingResponseEvent(
 			@Observes AuthorPendingResponseEvent authorPendingResponseEvent) {
 		List<RegistrationForm> forms = authorPendingResponseEvent.getForms();
 		authorReqGrid.setRowCount(0);
 		dataProvider.setList(forms);
 		dataProvider.refresh();
 	}
 }
