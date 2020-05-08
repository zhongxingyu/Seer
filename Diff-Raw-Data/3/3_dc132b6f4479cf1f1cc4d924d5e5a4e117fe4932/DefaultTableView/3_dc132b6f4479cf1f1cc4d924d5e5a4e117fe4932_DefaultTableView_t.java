 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 package de.unioninvestment.eai.portal.portlet.crud.mvp.views;
 
 import com.vaadin.data.Buffered;
 import com.vaadin.data.Buffered.SourceException;
 import com.vaadin.data.Item;
 import com.vaadin.data.Property;
 import com.vaadin.data.Property.ValueChangeEvent;
 import com.vaadin.data.Validator.InvalidValueException;
 import com.vaadin.data.util.converter.Converter.ConversionException;
 import com.vaadin.event.ItemClickEvent;
 import com.vaadin.server.Page;
 import com.vaadin.server.WebBrowser;
 import com.vaadin.shared.ui.MultiSelectMode;
 import com.vaadin.ui.*;
 import com.vaadin.ui.Button.ClickEvent;
 import com.vaadin.ui.Component;
 import com.vaadin.ui.Notification.Type;
 import com.vaadin.ui.Table.ColumnGenerator;
 import de.unioninvestment.eai.portal.portlet.crud.CrudErrorHandler;
 import de.unioninvestment.eai.portal.portlet.crud.domain.container.EditorSupport;
 import de.unioninvestment.eai.portal.portlet.crud.domain.exception.ContainerException;
 import de.unioninvestment.eai.portal.portlet.crud.domain.model.*;
 import de.unioninvestment.eai.portal.portlet.crud.domain.model.Table;
 import de.unioninvestment.eai.portal.portlet.crud.domain.model.Table.Mode;
 import de.unioninvestment.eai.portal.portlet.crud.domain.model.Table.SelectionMode;
 import de.unioninvestment.eai.portal.portlet.crud.export.DownloadExportTask;
 import de.unioninvestment.eai.portal.portlet.crud.export.ExportDialog;
 import de.unioninvestment.eai.portal.portlet.crud.export.ExportTask;
 import de.unioninvestment.eai.portal.portlet.crud.export.streaming.CsvExporter;
 import de.unioninvestment.eai.portal.portlet.crud.export.streaming.ExcelExporter;
 import de.unioninvestment.eai.portal.portlet.crud.export.streaming.StreamingExporterDownload;
 import de.unioninvestment.eai.portal.portlet.crud.mvp.views.ui.*;
 import de.unioninvestment.eai.portal.support.vaadin.context.Context;
 import de.unioninvestment.eai.portal.support.vaadin.support.BufferedTable;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Configurable;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.core.task.TaskExecutor;
 
 import java.sql.SQLException;
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 import static de.unioninvestment.eai.portal.support.vaadin.context.Context.getMessage;
 import static java.util.Collections.emptySet;
 import static java.util.Collections.singleton;
 
 /**
  * View-Objekt, dass die Anzeige eine Tabelle kapselt.
  * 
  * @author carsten.mjartan
  */
 @Configurable
 @SuppressWarnings("serial")
 public class DefaultTableView extends VerticalLayout implements TableView {
 
 	private static final long serialVersionUID = 1L;
 
 	private static final Logger LOG = LoggerFactory
 			.getLogger(DefaultTableView.class);
 
 	public static final int HUNDRET = 100;
 
 	private Button editButton;
 	private Button saveButton;
 	private Button insertButton;
 	private Button revertButton;
 	private Button removeButton;
 
 	private Map<String, Button> actionButtons = new HashMap<String, Button>();
 	private Map<Button, TableAction> buttonToTableActionMap = new HashMap<Button, TableAction>();
 
 	/**
 	 * @deprecated Use {@link TableAction#isExportAction()} instead
 	 */
 	private Button excelExportButton;
 
 	/**
 	 * @deprecated Use {@link TableAction#isExportAction()} instead
 	 */
 	private Button csvExportButton;
 
 	private CrudTable table;
 
 	private TableView.Presenter presenter;
 
 	DataContainer container;
 
 	private boolean ignoreSelectionChange = false;
 
 	private Object uncommittedItemId;
 
 	private Set<String> lastCollapsedColumns;
 
 	private boolean removalInProgress = false;
 
 	Table tableModel;
 
 	private int tableActionDummyIdCounter = 0;
 
 	@Autowired
 	@Qualifier("exportExecutor")
 	private TaskExecutor exportExecutor;
 
 	/**
 	 * Konstruktor.
 	 */
 	DefaultTableView() {
 		setSpacing(true);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see de.unioninvestment.eai.portal.portlet.crud.mvp.views.TableView#initialize(de.unioninvestment.eai.portal.portlet.crud.mvp.views.TableView.Presenter,
 	 *      de.unioninvestment.eai.portal.portlet.crud.domain.model.DataContainer,
 	 *      de.unioninvestment.eai.portal.portlet.crud.domain.model.Table, int,
 	 *      double)
 	 */
 	@Override
 	public void initialize(TableView.Presenter presenter,
 			DataContainer databaseContainer, Table tableModel, int pageLength,
 			double cacheRate) {
 
 		this.presenter = presenter;
 		this.container = databaseContainer;
 		this.tableModel = tableModel;
 
 		// @since 1.45
 		if (tableModel.getWidth() != null) {
 			setWidth(tableModel.getWidth());
 		}
 		// @since 1.45
 		if (tableModel.getHeight() != null) {
 			setHeight(tableModel.getHeight());
 		}
 
 		table = new CrudTable(container, tableModel.getColumns(),
 				tableModel.isSortingEnabled());
 		table.disableContentRefreshing();
 		table.setColumnCollapsingAllowed(true);
 		table.setColumnReorderingAllowed(true);
 		table.setSizeFull();
 		table.setImmediate(true);
 		table.setEditable(false);
 
 		if (tableModel.getSelectionMode() == SelectionMode.MULTIPLE) {
 			table.setSelectable(true);
 			table.setNullSelectionAllowed(false);
 			table.setMultiSelect(true);
 			table.setMultiSelectMode(MultiSelectMode.DEFAULT);
 		} else if (tableModel.getSelectionMode() == SelectionMode.SINGLE) {
 			table.setSelectable(true);
 			table.setNullSelectionAllowed(false);
 		}
 
 		if (!tableModel.isSortingEnabled()) {
 			table.setSortEnabled(false);
 		}
 
 		table.addStyleName("crudViewMode");
 		table.addStyleName("crudTable");
 
 		Integer rowHeight = tableModel.getRowHeight();
 		if (rowHeight != null) {
 			table.addStyleName("rowheight" + rowHeight);
 			String css = ".v-table-rowheight" + rowHeight
 					+ " .v-table-cell-content { height: " + rowHeight + "px; }";
 			css += "div.crudTable td div.v-table-cell-wrapper { max-height: "
 					+ rowHeight + "px; }";
 			Page.getCurrent().getStyles().add(css);
 		}
 
 		table.setPageLength(pageLength);
 		table.setCacheRate(cacheRate);
 
 		initializeTableFieldFactory();
 
 		// since 1.45
 		table.setHeight("100%");
 		addComponent(table);
 		setExpandRatio(table, 1);
 
 		Layout buttonBar = initButtonBar();
 		if (buttonBar.iterator().hasNext()) {
 			addComponent(buttonBar);
 		}
 
 		renderTableHeader();
 
 		updateColumnWidths();
 
 		setColumnGenerator(table, rowHeight);
 
 		updateVisibleColumns(false);
 
 		initializeListener();
 
 		setupErrorHandling();
 
 		setTableStyleRenderer();
 
 		presenter.doInitialize();
 
 		if (tableModel.getMode() == Mode.EDIT) {
 			switchToEditMode();
 		}
 
 		table.enableContentRefreshing(false);
 	}
 
 	public BufferedTable getTable() {
 		return table;
 	}
 
 	private void initializeListener() {
 		addSelectionChangeListener();
 		addDoubleClickListener();
 
 		if (tableModel.isEditable()) {
 			addCrudButtonListeners();
 		}
 		addExportButtonListeners();
 		addCustomButtonsListeners();
 	}
 
 	private void setTableStyleRenderer() {
 		this.table.setCellStyleGenerator(new CrudCellStyleGenerator(tableModel,
 				container));
 	}
 
 	private void addSelectionChangeListener() {
 		table.addValueChangeListener(new Property.ValueChangeListener() {
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public void valueChange(ValueChangeEvent event) {
 				onSelectionChanged();
 			}
 		});
 	}
 
 	private void addDoubleClickListener() {
 		table.addItemClickListener(new ItemClickEvent.ItemClickListener() {
 			private static final long serialVersionUID = 1L;
 
 			public void itemClick(ItemClickEvent event) {
 				if (event.isDoubleClick()) {
 					Item item = event.getItem();
 					presenter.doubleClick(item);
 				}
 			}
 		});
 	}
 
 	private void addCrudButtonListeners() {
 		if (editButton != null) {
 			editButton.addClickListener(new Button.ClickListener() {
 				@Override
 				public void buttonClick(ClickEvent event) {
 					presenter.switchMode(tableModel.getMode() == Mode.VIEW ? Mode.EDIT
 							: Mode.VIEW);
 				}
 			});
 		} else if (saveButton != null) {
 			saveButton.addClickListener(new Button.ClickListener() {
 				@Override
 				public void buttonClick(ClickEvent event) {
 					commit();
 				}
 			});
 		}
 		revertButton.addClickListener(new Button.ClickListener() {
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public void buttonClick(ClickEvent event) {
 				onRevertChanges();
 			}
 		});
 		if (presenter.isInsertable()) {
 			insertButton.addClickListener(new Button.ClickListener() {
 				@Override
 				public void buttonClick(ClickEvent event) {
 					onAddBlankRow();
 				}
 			});
 		}
 		if (presenter.isDeleteable()) {
 			removeButton.addClickListener(new Button.ClickListener() {
 				@Override
 				public void buttonClick(ClickEvent event) {
 					onRemoveRow();
 				}
 			});
 		}
 	}
 
 	private void addExportButtonListeners() {
 		if (presenter.isExcelExport()) {
 			excelExportButton.addClickListener(new Button.ClickListener() {
 				@Override
 				public void buttonClick(ClickEvent event) {
 					exportExcelSheet(null);
 				}
 
 			});
 		}
 
 		if (presenter.isCSVExport()) {
 			csvExportButton.addClickListener(new Button.ClickListener() {
 				@Override
 				public void buttonClick(ClickEvent event) {
 					exportCSVSheet(null);
 				}
 			});
 		}
 	}
 
 	private void addCustomButtonsListeners() {
 		if (!actionButtons.isEmpty()) {
 			for (final Button button : actionButtons.values()) {
 				button.setDisableOnClick(true);
 				final TableAction action = buttonToTableActionMap.get(button);
 				button.addClickListener(new Button.ClickListener() {
 
 					private static final long serialVersionUID = 42L;
 
 					@Override
 					public void buttonClick(ClickEvent event) {
 						try {
 							presenter.callClosure(action);
 
 							if (action.isExportAction()) {
 								switch (action.getExportType()) {
 								case XLS:
 									exportExcelSheet(action
 											.generateExportFilename());
 									break;
 								case CSV:
 									exportCSVSheet(action
 											.generateExportFilename());
 									break;
 								default:
 									throw new IllegalArgumentException(
 											"Unknown export type '"
 													+ action.getExportType()
 													+ "' set on action with title '"
 													+ action.getTitle()
 													+ "' and id '"
 													+ action.getId() + "'");
 								}
 							}
 						} finally {
 							button.setEnabled(true);
 						}
 					}
 				});
 			}
 		}
 	}
 
 	private void initializeTableFieldFactory() {
 		DefaultCrudFieldFactory fieldFactory = new DefaultCrudFieldFactory(
 				table, tableModel);
 
 		fieldFactory.setCreateFormFieldForTable(!presenter.isFormEditEnabled());
 
 		ValidationFieldFactoryWrapper validatingFieldFactory = new ValidationFieldFactoryWrapper(
 				container, fieldFactory, tableModel.getColumns());
 		table.setTableFieldFactory(validatingFieldFactory);
 	}
 
 	/**
 	 * Ersetzt den Spaltennamen mit einem alternativen Titel. Wenn ein
 	 * "longtitle" angegeben ist, wird dieser als Tooltip in die Seite
 	 * gerendert.
 	 */
 	void renderTableHeader() {
 		if (tableModel.getColumns() != null) {
 			for (TableColumn tc : tableModel.getColumns()) {
 				String title = tc.getTitle();
 				String longTitle = tc.getLongTitle();
 				if (longTitle != null) {
 					if (title == null) {
 						title = tc.getName();
 					}
 					table.setColumnHeader(tc.getName(), "<span title=\""
 							+ longTitle + "\">" + title + "</span>");
 
 				} else if (title != null) {
 					table.setColumnHeader(tc.getName(), title);
 				}
 			}
 		}
 	}
 
 	private void setColumnGenerator(BufferedTable tableComponent,
 			Integer columnHeight) {
 		TableColumns columns = tableModel.getColumns();
 		if (columns != null) {
 			Iterator<TableColumn> iter = columns.iterator();
 			while (iter.hasNext()) {
 				TableColumn c = iter.next();
 				String columnName = c.getName();
 				if (c.isGenerated()) {
 					VaadinCustomColumnGenerator generator = createVaadinColumnGenerator(c);
 					LOG.debug(
 							"Adding column generator for generated column '{}'",
 							columnName);
 					tableComponent.addGeneratedColumn(columnName, generator);
 
 				} else if (c.isBinary()) {
 					BLobColumnGenerator generator = new BLobColumnGenerator(
 							container, columns);
 					tableComponent.addGeneratedColumn(columnName, generator);
 				} else {
 					Class<?> columnType = container.getType(columnName);
 					EditorSupport editor = container.findEditor(columnName);
 					CrudTableColumnGenerator generator = new CrudTableColumnGenerator(
 							columnName, columnType, columnHeight, columns,
 							columns.getAllNames().get(0), table, container,
 							editor);
 					if (LOG.isDebugEnabled()) {
 						LOG.debug(
 								"Adding column generator for column '{}' of type '{}'",
 								columnName, columnType.getName());
 					}
 					tableComponent.addGeneratedColumn(columnName, generator);
 				}
 			}
 		}
 	}
 
 	private VaadinCustomColumnGenerator createVaadinColumnGenerator(
 			TableColumn c) {
 		if (c.getGeneratedValueGenerator() != null
 				&& c.getGeneratedType() != null) {
 			return new VaadinExportableColumnGenerator(c, container);
 		} else {
 			return new VaadinCustomColumnGenerator(c, container);
 		}
 	}
 
 	private void updateColumnWidths() {
 		if (tableModel.getColumns() != null) {
 			Collection<?> props = table.getContainerPropertyIds();
 			for (Object p : props) {
 				for (TableColumn c : tableModel.getColumns()) {
 					if (c.getName().equals(p)) {
 						if (c.getWidth() != null && c.getWidth() > 0)
 							table.setColumnWidth(c.getName(), c.getWidth());
 						break;
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * On Selection.
 	 */
 	public void onSelectionChanged() {
 
 		Set<Object> selection = getCurrentSelection();
 
 		if (shouldCommitOnSelectionChange(selection)) {
 			if (!commit()) {
 				rollbackSelection();
 			} else {
 				updateUncommittedItemId(selection);
 				presenter.changeSelection(selection);
 			}
 
 			table.enableContentRefreshing(true);
 		} else {
 			applySelection(selection);
 			presenter.changeSelection(selection);
			if (inEditMode() && !presenter.isFormEditEnabled()) {
 				table.refreshRowCache();
 				table.enableContentRefreshing(true);
 			}
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	private Set<Object> getCurrentSelection() {
 		if (table.isMultiSelect()) {
 			return (Set<Object>) table.getValue();
 		} else {
 			Object value = table.getValue();
 			return value != null ? singleton(value) : emptySet();
 		}
 	}
 
 	private boolean shouldCommitOnSelectionChange(Set<Object> newSelection) {
 		return !ignoreSelectionChange && !removalInProgress && inEditMode()
 				&& !presenter.isFormEditEnabled()
 				&& isLeavingUncommittedRow(newSelection);
 	}
 
 	private boolean isSingleSelection(Set<Object> selection) {
 		return selection != null && selection.size() == 1;
 	}
 
 	private boolean isLeavingUncommittedRow(Set<Object> selection) {
 		if (uncommittedItemId == null) {
 			return false;
 		} else if (selection.size() == 1
 				&& uncommittedItemId.equals(selection.iterator().next())) {
 			return false;
 		} else {
 			return true;
 		}
 	}
 
 	void updateUncommittedItemId(Set<Object> selection) {
 		if (selection == null || selection.size() != 1) {
 			uncommittedItemId = null;
 		} else {
 			uncommittedItemId = selection.iterator().next();
 		}
 		presenter.updateUncommittedItemId(uncommittedItemId);
 		updateRemoveButtonStatus(selection, uncommittedItemId);
 	}
 
 	private void updateRemoveButtonStatus(Set<Object> selection,
 			Object singleItemId) {
 		if (removeButton != null) {
 			boolean deletable = false;
 			if (inEditMode() && presenter.isDeleteable()) {
 				if (singleItemId != null
 						&& presenter.isRowDeletable(singleItemId)) {
 					deletable = true;
 				} else if (selection.size() > 1) {
 					deletable = true;
 				}
 			}
 			removeButton.setEnabled(deletable);
 		}
 	}
 
 	private boolean inEditMode() {
 		return table.isEditable();
 	}
 
 	private void rollbackSelection() {
 		if (uncommittedItemId == null) {
 			select(emptySet());
 		} else {
 			select(singleton(uncommittedItemId));
 		}
 	}
 
 	private void applySelection(Set<Object> selection) {
 		updateUncommittedItemId(selection);
 		select(selection);
 	}
 
 	private void select(Set<Object> selection) {
 		if (table.isMultiSelect()) {
 			table.setValue(selection);
 		} else if (selection.isEmpty()) {
 			table.setValue(null);
 		} else {
 			table.setValue(selection.iterator().next());
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see de.unioninvestment.eai.portal.portlet.crud.mvp.views.TableView#addItemToTable()
 	 */
 	@Override
 	public Object addItemToTable() {
 		return table.addItem();
 	}
 
 	/**
 	 * Action Methode fuer das Einfuegen einer neuen Datenzeile.
 	 */
 	void onAddBlankRow() {
 		if (commit()) {
 			Object newItemId = addItemToTable();
 			selectNewRow(newItemId, true);
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see de.unioninvestment.eai.portal.portlet.crud.mvp.views.TableView#selectItemForEditing(java.lang.Object,
 	 *      boolean)
 	 */
 	@Override
 	public void selectItemForEditing(Object newItemId, boolean suppressCommit) {
 		selectNewRow(newItemId, suppressCommit);
 	}
 
 	private void selectNewRow(Object newItemId, boolean suppressCommit) {
 		try {
 			// Der Selection-Change-Listener löst bei unselectAll u. a. implizit
 			// ein Commit aus
 			ignoreSelectionChange = suppressCommit;
 			select(emptySet());
 			updateUncommittedItemId(singleton(newItemId));
 			select(singleton(newItemId));
 			table.setCurrentPageFirstItemId(newItemId);
 			if (presenter.isFormEditEnabled()) {
 				presenter.openRowEditingForm();
 			}
 		} finally {
 			ignoreSelectionChange = false;
 		}
 	}
 
 	/**
 	 * Action Methode fuer das Löschen von Datenzeilen.
 	 */
 	public void onRemoveRow() {
 		if (table.isEditable() && uncommittedItemId != null) {
 			removeCurrentRowSelectNextRow();
 		} else {
 			removeCurrentSelectionItemsThatAreDeletable();
 		}
 	}
 
 	private void removeCurrentRowSelectNextRow() {
 		Object nextItemId = getRowToSelectAfterRemoval();
 		try {
 			removalInProgress = true;
 
 			table.removeItem(uncommittedItemId);
 			commit();
 
 		} finally {
 			removalInProgress = false;
 		}
 		updateUncommittedItemId(singleton(nextItemId));
 		select(singleton(nextItemId));
 	}
 
 	private Object getRowToSelectAfterRemoval() {
 		Object nextItemId = table.nextItemId(uncommittedItemId);
 		if (nextItemId == null) {
 			nextItemId = table.prevItemId(uncommittedItemId);
 		}
 		return nextItemId;
 	}
 
 	@SuppressWarnings("unchecked")
 	private void removeCurrentSelectionItemsThatAreDeletable() {
 		Set<Object> selection = getCurrentSelection();
 		try {
 			removalInProgress = true;
 			int all = selection.size();
 			int removed = 0;
 			for (Object selectedItemId : selection) {
 				if (presenter.isRowDeletable(selectedItemId)) {
 					table.removeItem(selectedItemId);
 					removed++;
 				}
 			}
 			commit();
 			if (removed < all) {
 				Notification.show(
 						getMessage("portlet.crud.table.rowsPartlyDeleted",
 								removed, all), Type.WARNING_MESSAGE);
 			}
 		} finally {
 			removalInProgress = false;
 		}
 	}
 
 	@Override
 	public void onRevertChanges() {
 		try {
 			table.discardFieldValues();
 			table.discard();
 
 			container.rollback();
 			try {
 				ignoreSelectionChange = true;
 				select(emptySet());
 			} finally {
 				ignoreSelectionChange = false;
 			}
 
 		} catch (Exception e) {
 			onError(e);
 		}
 	}
 
 	/**
 	 * Wechselt in den Anzeigemodus. Beim Verlassen des Editiermodus wird ein
 	 * Commit durchgeführt.
 	 */
 	@Override
 	public void switchToViewMode() {
 		if (table.isEditable() && commit()) {
 
 			table.setEditable(false);
 
 			if (editButton != null) {
 				editButton
 						.setCaption(getMessage("portlet.crud.button.editMode"));
 				table.removeStyleName("crudEditMode");
 				table.addStyleName("crudViewMode");
 			} else if (saveButton != null) {
 				saveButton.setVisible(false);
 			}
 
 			insertButton.setVisible(false);
 			revertButton.setVisible(false);
 			removeButton.setVisible(false);
 
 			table.setColumnCollapsingAllowed(true);
 			setCollapsedColumns(lastCollapsedColumns);
 
 			updateVisibleColumns(false);
 			LOG.debug("Setze den Ansichtsmodus");
 		}
 	}
 
 	@Override
 	public void switchToEditMode() {
 		table.setEditable(true);
 
 		if (editButton != null) {
 			editButton.setCaption(getMessage("portlet.crud.button.viewMode"));
 			table.removeStyleName("crudViewMode");
 			table.addStyleName("crudEditMode");
 		} else if (saveButton != null) {
 			saveButton.setVisible(true);
 		}
 
 		insertButton.setVisible(true);
 		revertButton.setVisible(true);
 		removeButton.setVisible(true);
 
 		lastCollapsedColumns = getCollapsedColumns();
 		table.setColumnCollapsingAllowed(false);
 		updateVisibleColumns(true);
 
 		updateRemoveButtonStatus(getCurrentSelection(), uncommittedItemId);
 
 		LOG.debug("Setze den Editiermodus");
 	}
 
 	private Layout initButtonBar() {
 		CssLayout buttonbar = new CssLayout();
 		buttonbar.setStyleName("actions");
 
 		if (tableModel.isEditable()) {
 			if (tableModel.isModeChangeable()) {
 				editButton = new Button(
 						getMessage("portlet.crud.button.editMode"));
 				editButton.setEnabled(true);
 				buttonbar.addComponent(editButton);
 			} else if (!tableModel.isFormEditEnabled()) {
 				saveButton = new Button(getMessage("portlet.crud.button.save"));
 				saveButton.setVisible(false);
 				saveButton.setEnabled(true);
 				buttonbar.addComponent(saveButton);
 			}
 
 			revertButton = new Button(getMessage("portlet.crud.button.reset"));
 			revertButton.setVisible(false);
 			revertButton.setEnabled(true);
 			buttonbar.addComponent(revertButton);
 
 			insertButton = new Button(
 					getMessage("portlet.crud.button.blankRow"));
 			insertButton.setVisible(false);
 			insertButton.setEnabled(presenter.isInsertable());
 			buttonbar.addComponent(insertButton);
 
 			removeButton = new Button(
 					getMessage("portlet.crud.button.removeRow"));
 			removeButton.setVisible(false);
 			removeButton.setEnabled(presenter.isDeleteable());
 			buttonbar.addComponent(removeButton);
 		}
 		if (presenter.isExcelExport()) {
 			excelExportButton = new Button("Excel");
 			buttonbar.addComponent(excelExportButton);
 		}
 
 		if (presenter.isCSVExport()) {
 			csvExportButton = new Button("CSV");
 			buttonbar.addComponent(csvExportButton);
 		}
 
 		for (TableAction action : tableModel.getActions()) {
 			Button actionButton = new Button(action.getTitle());
 			if (action.getId() != null) {
 				actionButtons.put(action.getId(), actionButton);
 			} else {
 				actionButtons.put(
 						"table-action-" + tableActionDummyIdCounter++,
 						actionButton);
 			}
 			buttonToTableActionMap.put(actionButton, action);
 			buttonbar.addComponent(actionButton);
 		}
 
 		return buttonbar;
 	}
 
 	@Override
 	public void commitChangesToContainer() {
 		table.commitFieldValues();
 		table.commit();
 	}
 
 	public Map<String, Object> getModifiedColumnNames() {
 		return table.getModifiedColumnNames();
 	}
 
 	/**
 	 * @return <code>true</code>, falls der Commit erfolgreich durchgeführt
 	 *         werden konnte.
 	 */
 	private boolean commit() {
 		try {
 			container.commit();
 			LOG.debug("Committing changes successfull");
 			return true;
 
 		} catch (Buffered.SourceException e) {
 			onError(e);
 			return false;
 		} catch (Exception e) {
 			onError(e);
 			return false;
 		}
 	}
 
 	/**
 	 * Konfiguriert einen ErrorHandler, der Fehler möglichst lesbar als
 	 * Notification anzeigt.
 	 */
 	@SuppressWarnings("serial")
 	protected void setupErrorHandling() {
 		table.setErrorHandler(new CrudErrorHandler() {
 			@Override
 			public void error(com.vaadin.server.ErrorEvent event) {
 				Throwable throwable = event.getThrowable();
 				if (throwable instanceof SourceException) {
 					onError(throwable);
 				} else {
 					super.error(event);
 				}
 			}
 		});
 	}
 
 	/**
 	 * Erzeugt eine Notification für den angegebenen Fehler.
 	 * 
 	 * @param throwable
 	 *            der anzuzeigende Fehler
 	 */
 	private void onError(Throwable throwable) {
 		String message = throwable.getMessage();
 		if (throwable.getCause() instanceof ConversionException
 				|| throwable.getCause() instanceof InvalidValueException) {
 			message = throwable.getCause().getMessage();
 		}
 		if (throwable instanceof SourceException
 				&& ((SourceException) throwable).getSource() instanceof Component) {
 			Buffered source = ((SourceException) throwable).getSource();
 			String sourceCaption = ((AbstractField<?>) source).getCaption();
 
 			LOG.debug("Field error for {}: {}", sourceCaption,
 					throwable.getMessage());
 
 			Notification.show(sourceCaption + ": " + message,
 					Notification.Type.ERROR_MESSAGE);
 			return;
 		} else if (throwable instanceof ConcurrentModificationException) {
 			message = getMessage("portlet.crud.error.concurrentModification");
 		} else if (throwable instanceof ContainerException
 				&& throwable.getCause() instanceof SQLException
 				&& throwable.getCause().getMessage()
 						.contains("Removal failed for row")) {
 			message = getMessage("portlet.crud.error.concurrentModification");
 		} else {
 			LOG.error("Error in table operation", throwable);
 		}
 
 		if (message == null || message.length() == 0) {
 			message = getMessage("portlet.crud.error.ofType", throwable
 					.getClass().getName());
 		}
 		Notification.show(message, Notification.Type.ERROR_MESSAGE);
 	}
 
 	private void updateVisibleColumns(boolean isEditMode) {
 		Object[] visibles;
 		TableColumns columns = tableModel.getColumns();
 		if (columns != null) {
 			visibles = hideUnsupportedDataTypes(
 					columns.getVisibleNamesForTable(), columns);
 		} else {
 			visibles = hideUnsupportedDataTypes(
 					Arrays.asList(table.getVisibleColumns()), columns);
 		}
 		table.setVisibleColumns(visibles);
 	}
 
 	private Object[] hideUnsupportedDataTypes(List<?> visibles,
 			TableColumns columns) {
 		ArrayList<String> visibleCols = new ArrayList<String>();
 		for (Object id : visibles) {
 			if (columns != null && columns.get((String) id).isGenerated()) {
 				visibleCols.add((String) id);
 			} else if (columns != null && columns.get((String) id).isBinary()) {
 				visibleCols.add((String) id);
 			} else {
 				Class<?> type = container.getType((String) id);
 				if (type != null
 						&& container.findDisplayer((String) id) != null) {
 					visibleCols.add((String) id);
 				}
 			}
 		}
 		return visibleCols.toArray();
 	}
 
 	private Set<String> getCollapsedColumns() {
 		Set<String> collapsedColumns = new HashSet<String>();
 		for (Object id : table.getContainerPropertyIds()) {
 			if (table.isColumnCollapsed(id)) {
 				collapsedColumns.add((String) id);
 			}
 		}
 		return collapsedColumns;
 	}
 
 	private void setCollapsedColumns(Set<String> collapsedColumns) {
 		for (Object id : table.getContainerPropertyIds()) {
 			table.setColumnCollapsed(id, collapsedColumns.contains(id));
 		}
 	}
 
 	@Override
 	public void showNotification(String msgKey,
 			Notification.Type notificationType) {
 		Notification.show(msgKey, notificationType);
 	}
 
 	void setTable(CrudTable table) {
 		this.table = table;
 	}
 
 	private void exportCSVSheet(String customFilename) {
 		CsvExporter exporter = new CsvExporter();
 		String filename = customFilename != null ? customFilename : "export_"
 				+ createFilenameTime() + ".csv";
 		Download download = new StreamingExporterDownload(filename,
 				CsvExporter.CSV_MIMETYPE, tableModel, exporter);
 		DownloadExportTask exportTask = new DownloadExportTask(UI.getCurrent(),
 				tableModel, download, automaticDownloadIsPossible());
 		executeExport(exportTask);
 	}
 
 	private void exportExcelSheet(String customFilename) {
 		ExcelExporter exporter = Context.getBean(ExcelExporter.class);
 		String filename = customFilename != null ? customFilename : "export_"
 				+ createFilenameTime() + ".xlsx";
 		Download download = new StreamingExporterDownload(filename,
 				ExcelExporter.EXCEL_XSLX_MIMETYPE, tableModel, exporter);
 		DownloadExportTask exportTask = new DownloadExportTask(UI.getCurrent(),
 				tableModel, download, automaticDownloadIsPossible());
 		executeExport(exportTask);
 	}
 
 	@Override
 	public void download(Download download) {
 		DownloadExportTask downloadTask = new DownloadExportTask(
 				UI.getCurrent(), tableModel, download,
 				automaticDownloadIsPossible());
 		executeExport(downloadTask);
 	}
 
 	protected String createFilenameTime() {
 		SimpleDateFormat sdf = new SimpleDateFormat("dd_MM_yyyy");
 		return sdf.format(new Date());
 	}
 
 	private void executeExport(ExportTask exportTask) {
 		table.setEnabled(false);
 		boolean automaticDownload = automaticDownloadIsPossible();
 
 		ExportDialog dialog = new ExportDialog(table, exportTask,
 				automaticDownload);
 		UI.getCurrent().addWindow(dialog);
 		exportExecutor.execute(exportTask);
 	}
 
 	private boolean automaticDownloadIsPossible() {
 		WebBrowser browser = Page.getCurrent().getWebBrowser();
 		return browser != null && !browser.isIE();
 	}
 
 	@Override
 	public void selectionUpdatedExternally(Set<Object> selection) {
 		if (tableModel.isFormEditEnabled()) {
 			select(selection);
 		} else {
 			throw new UnsupportedOperationException(
 					"Currently only implemented to be used from inside form-edit dialog");
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see de.unioninvestment.eai.portal.portlet.crud.mvp.views.TableView#addGeneratedColumn(String,
 	 *      String, com.vaadin.ui.Table.ColumnGenerator)
 	 */
 	@Override
 	public void addGeneratedColumn(String id, String title,
 			ColumnGenerator columnGenerator) {
 		this.table.addGeneratedColumn(id, columnGenerator);
 		this.table.setColumnHeader(id, title);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see de.unioninvestment.eai.portal.portlet.crud.mvp.views.TableView#removeGeneratedColumn(java.lang.String)
 	 */
 	@Override
 	public void removeGeneratedColumn(String id) {
 		this.table.removeGeneratedColumn(id);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see de.unioninvestment.eai.portal.portlet.crud.mvp.views.TableView#enableContentRefreshing(boolean)
 	 */
 	@Override
 	public void enableContentRefreshing(boolean refreshContent) {
 		table.enableContentRefreshing(refreshContent);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see de.unioninvestment.eai.portal.portlet.crud.mvp.views.TableView#disableContentRefreshing()
 	 */
 	@Override
 	public boolean disableContentRefreshing() {
 		return table.disableContentRefreshing();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see de.unioninvestment.eai.portal.portlet.crud.mvp.views.TableView#getVisibleColumns()
 	 */
 	@Override
 	public List<String> getVisibleColumns() {
 		List<String> visibleColumns = new ArrayList<String>();
 		for (Object vaadinColumnId : table.getVisibleColumns()) {
 			if (vaadinColumnId instanceof String) {
 				visibleColumns.add((String) vaadinColumnId);
 			} else {
 				throw new IllegalStateException(
 						"columnId ["
 								+ vaadinColumnId.toString()
 								+ "] ist nicht vom Typ java.lang.String sondern vom Typ "
 								+ vaadinColumnId.getClass().getName() + ".");
 			}
 		}
 		return visibleColumns;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see de.unioninvestment.eai.portal.portlet.crud.mvp.views.TableView#setVisibleColumns(java.util.List)
 	 */
 	@Override
 	public void setVisibleColumns(List<String> visibleColumns) {
 		table.setVisibleColumns(visibleColumns.toArray());
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see de.unioninvestment.eai.portal.portlet.crud.mvp.views.TableView#setTableActionVisibility(java.lang.String,
 	 *      boolean)
 	 */
 	public void setTableActionVisibility(String id, boolean visible) {
 		Button button = actionButtons.get(id);
 		if (button != null) {
 			button.setVisible(visible);
 		} else {
 			LOG.warn(
 					"Ein Button mit der ID {} existiert nicht. Die Sichtbarkeit kann nicht auf {} gesetzt werden.",
 					id, visible);
 		}
 	}
 
 	Map<String, Button> getActionButtons() {
 		return Collections.unmodifiableMap(actionButtons);
 	}
 
 }
