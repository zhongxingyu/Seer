 package org.cotrix.web.manage.client.codelist.codes.editor;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.cotrix.web.common.client.factory.UIFactories;
 import org.cotrix.web.common.client.feature.FeatureBinder;
 import org.cotrix.web.common.client.feature.FeatureToggler;
 import org.cotrix.web.common.client.resources.CommonResources;
 import org.cotrix.web.common.client.resources.CotrixSimplePager;
 import org.cotrix.web.common.client.util.ValueUtils;
 import org.cotrix.web.common.client.widgets.HasEditing;
 import org.cotrix.web.common.client.widgets.ItemToolbar;
 import org.cotrix.web.common.client.widgets.ItemToolbar.ButtonClickedEvent;
 import org.cotrix.web.common.client.widgets.ItemToolbar.ButtonClickedHandler;
 import org.cotrix.web.common.client.widgets.ItemToolbar.ItemButton;
 import org.cotrix.web.common.client.widgets.LoadingPanel;
 import org.cotrix.web.common.client.widgets.PageSizer;
 import org.cotrix.web.common.client.widgets.PageSizer.PageSizerStyle;
 import org.cotrix.web.common.client.widgets.PageSizer.PagerSizerResource;
 import org.cotrix.web.common.client.widgets.cell.DoubleClickEditTextCell;
 import org.cotrix.web.common.client.widgets.cell.SafeHtmlRendererCell;
 import org.cotrix.web.common.client.widgets.cell.StyledSafeHtmlRenderer;
 import org.cotrix.web.common.client.widgets.dialog.ConfirmDialog;
 import org.cotrix.web.common.client.widgets.dialog.ConfirmDialog.ConfirmDialogListener;
 import org.cotrix.web.common.client.widgets.dialog.ConfirmDialog.DialogButton;
 import org.cotrix.web.common.client.widgets.dialog.ConfirmDialog.DialogButtonDefaultSet;
 import org.cotrix.web.common.shared.codelist.UIAttribute;
 import org.cotrix.web.common.shared.codelist.UICode;
 import org.cotrix.web.manage.client.codelist.codes.editor.filter.FilterWordUpdatedEvent;
 import org.cotrix.web.manage.client.codelist.codes.event.CodeSelectedEvent;
 import org.cotrix.web.manage.client.codelist.codes.event.CodeUpdatedEvent;
 import org.cotrix.web.manage.client.codelist.codes.event.FilterOptionUpdatedEvent;
 import org.cotrix.web.manage.client.codelist.codes.event.GroupsSwitchedEvent;
 import org.cotrix.web.manage.client.codelist.codes.event.MarkerHighlightEvent;
 import org.cotrix.web.manage.client.codelist.codes.event.SwitchGroupsEvent;
 import org.cotrix.web.manage.client.codelist.codes.marker.MarkerRenderer;
 import org.cotrix.web.manage.client.codelist.codes.marker.MarkerType;
 import org.cotrix.web.manage.client.codelist.codes.marker.MarkerTypeUtil;
 import org.cotrix.web.manage.client.codelist.common.GroupFactory;
 import org.cotrix.web.manage.client.codelist.common.RemoveItemController;
 import org.cotrix.web.manage.client.data.CodeAttribute;
 import org.cotrix.web.manage.client.data.DataEditor;
 import org.cotrix.web.manage.client.data.event.DataEditEvent;
 import org.cotrix.web.manage.client.data.event.DataEditEvent.DataEditHandler;
 import org.cotrix.web.manage.client.data.event.EditType;
 import org.cotrix.web.manage.client.di.CodelistBus;
 import org.cotrix.web.manage.client.di.CurrentCodelist;
 import org.cotrix.web.manage.client.resources.CotrixManagerResources;
 import org.cotrix.web.manage.shared.AttributeGroup;
 import org.cotrix.web.manage.shared.Group;
 import org.cotrix.web.manage.shared.ManagerUIFeature;
 
 import com.allen_sauer.gwt.log.client.Log;
 import com.google.gwt.cell.client.Cell;
 import com.google.gwt.cell.client.FieldUpdater;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.dom.client.Style.Unit;
 import com.google.gwt.resources.client.CssResource;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.user.cellview.client.Column;
 import com.google.gwt.user.cellview.client.ColumnSortEvent.AsyncHandler;
 import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
 import com.google.gwt.user.cellview.client.Header;
 import com.google.gwt.user.cellview.client.PatchedDataGrid;
 import com.google.gwt.user.cellview.client.SafeHtmlHeader;
 import com.google.gwt.user.cellview.client.SimplePager;
 import com.google.gwt.user.cellview.client.SimplePager.Style;
 import com.google.gwt.user.cellview.client.SimplePager.TextLocation;
 import com.google.gwt.user.cellview.client.TextHeader;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.gwt.view.client.SelectionChangeEvent;
 import com.google.gwt.view.client.SingleSelectionModel;
 import com.google.inject.Inject;
 import com.google.web.bindery.event.shared.EventBus;
 import com.google.web.bindery.event.shared.binder.EventBinder;
 import com.google.web.bindery.event.shared.binder.EventHandler;
 
 /**
  * @author "Federico De Faveri federico.defaveri@fao.org"
  *
  */
 public class CodesEditor extends LoadingPanel implements HasEditing {
 
 	interface Binder extends UiBinder<Widget, CodesEditor> {}
 	interface CodelistEditorEventBinder extends EventBinder<CodesEditor> {}
 
 	private DialogButton CONTINUE_BUTTON;
 	private DialogButton MARK_BUTTON;
 	private DialogButton CANCEL_BUTTON;
 
 	interface EditorStyle extends CssResource {
 		String dialogButton();
 		String cancelButton();
 	}
 
 	interface DataGridResources extends PatchedDataGrid.Resources {
 
 		@Source({"CodelistEditor.css","definitions.css"})
 		DataGridStyle dataGridStyle();
 	}
 
 	interface DataGridStyle extends PatchedDataGrid.Style {
 
 		String codeCell();
 
 		String headerCell();
 
 		String markerHeader();
 
 		String textCell();
 
 		String language();
 
 		String closeGroup();
 
 		String emptyTableWidget();
 	}
 
 	interface GridPageSizerResource extends PagerSizerResource {
 		@Source({"PageSizer.css","definitions.css"})
 		GridPagerSizerStyle style();
 	}
 
 	interface GridPagerSizerStyle extends PageSizerStyle {
 	}
 
 	interface GridPagerResource extends CotrixSimplePager {
 		@Source({"SimplePager.css","definitions.css"})
 		Style simplePagerStyle();
 	}
 
 	@UiField(provided = true)
 	PatchedDataGrid<UICode> dataGrid;
 
 	@UiField(provided = true)
 	SimplePager pager;
 
 	@UiField(provided = true)
 	PageSizer pageSizer;
 
 	@UiField ItemToolbar toolBar;
 
 	@UiField EditorStyle style;
 
 	private static DataGridResources resource = GWT.create(DataGridResources.class);
 	private static GridPageSizerResource gridPageSizerResource = GWT.create(GridPageSizerResource.class);
 	private static GridPagerResource gridPagerResource = GWT.create(GridPagerResource.class);
 
 	private List<Group> groupsAsColumn = new ArrayList<Group>();
 	private Map<Group, Column<UICode, String>> groupsColumns = new HashMap<Group, Column<UICode,String>>(); 
 	private List<DoubleClickEditTextCell> editableCells = new ArrayList<DoubleClickEditTextCell>();
 	private boolean editable = true;
 
 	private Column<UICode, String> nameColumn;
 
 	@Inject @CodelistBus
 	private EventBus codelistBus;
 
 	private SingleSelectionModel<UICode> selectionModel;
 
 	@Inject
 	private CodesProvider dataProvider;
 
 	private DataEditor<UICode> codeEditor;
 
 	private DataEditor<CodeAttribute> attributeEditor;
 
 	@Inject
 	private CotrixManagerResources resources;
 
 	private StyledSafeHtmlRenderer cellRenderer;
 
 	@Inject @CurrentCodelist
 	private String codelistId;
 
 	@Inject
 	private UIFactories factories;
 
 	@Inject
 	private RemoveItemController codeRemotionController;
 
 	@Inject
 	private MarkerRenderer markerRenderer;
 
 	@Inject
 	private ConfirmDialog confirmDialog;
 
 	@Inject
 	private MarkerTypeUtil markerTypeResolver;
 
 	@Inject
 	private CodesEditorRowHightlighter hightlighter;
 
 	@Inject
 	private GroupFactory groupFactory;
 
 	@Inject
 	private void init() {
 		this.codeEditor = DataEditor.build(this);
 		this.attributeEditor = DataEditor.build(this);
 
 		cellRenderer = new StyledSafeHtmlRenderer(resource.dataGridStyle().textCell());
 		cellRenderer.setAddTitle(true);
 
 		dataGrid = new PatchedDataGrid<UICode>(20, resource, CodeKeyProvider.INSTANCE) {
 
 			/** 
 			 * {@inheritDoc}
 			 */
 			@Override
 			protected int getTableWidth() {
 				int internal = super.getTableWidth();
 				return internal>0?internal:CodesEditor.this.getElement().getOffsetWidth()-31;
 			}
 
 		};
 		dataGrid.setAutoHeaderRefreshDisabled(true);
 
 		Label emptyTable = new Label("No codes");
 		emptyTable.setStyleName(resource.dataGridStyle().emptyTableWidget());
 		dataGrid.setEmptyTableWidget(emptyTable);
 
 		dataGrid.setTableWidth(100, Unit.PCT);
 		dataGrid.setAutoAdjust(true);
 		dataGrid.setLastColumnSpan(true);
 
 		dataGrid.setRowStyles(hightlighter);
 
 		AsyncHandler asyncHandler = new AsyncHandler(dataGrid);
 		dataGrid.addColumnSortHandler(asyncHandler);
 
 		// Create a Pager to control the table.
 		pager = new SimplePager(TextLocation.CENTER, gridPagerResource, false, 0, true);
 		pager.setDisplay(dataGrid);
 
 		setupColumns();
 
 		selectionModel = new SingleSelectionModel<UICode>(CodeKeyProvider.INSTANCE);
 		selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
 
 			@Override
 			public void onSelectionChange(SelectionChangeEvent event) {
 				UICode code = selectionModel.getSelectedObject();
 
 				codeRemotionController.setItemCanBeRemoved(code!=null);
 				updateRemoveButtonVisibility(false);
 
 				Log.trace("onSelectionChange code: "+code);
 				codelistBus.fireEvent(new CodeSelectedEvent(code));
 			}
 		});
 
 		dataGrid.setSelectionModel(selectionModel);
 		dataGrid.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.BOUND_TO_SELECTION);
 
 		dataProvider.addDataDisplay(dataGrid);
 
 		pageSizer = new PageSizer(new int[]{25,50,100}, 25, gridPageSizerResource);
 		pageSizer.setDisplay(dataGrid);
 
 		Binder uiBinder = GWT.create(Binder.class);
 		initWidget(uiBinder.createAndBindUi(this));
 
 		toolBar.addButtonClickedHandler(new ButtonClickedHandler() {
 
 			@Override
 			public void onButtonClicked(ButtonClickedEvent event) {
 				switch (event.getButton()) {
 					case MINUS: removeSelectedCode(); break;
 					case PLUS: addNewCode(); break;
 				}
 			}
 		});
 
 		CONTINUE_BUTTON = new ConfirmDialog.SimpleDialogButton("Continue And Remove", CommonResources.INSTANCE.css().blueButton() + " " + style.dialogButton(), 220);
 		MARK_BUTTON =  new ConfirmDialog.SimpleDialogButton("Mark Deleted Instead", CommonResources.INSTANCE.css().blueButton() + " " + style.dialogButton(), 220);
 		CANCEL_BUTTON =  new ConfirmDialog.SimpleDialogButton(DialogButtonDefaultSet.CANCEL.getLabel(), DialogButtonDefaultSet.CANCEL.getStyleName() + " " + style.cancelButton(), 98);
 
 		codeRemotionController.setItemCanBeRemoved(false);
 		updateRemoveButtonVisibility(false);
 	}
 
 	@Inject
 	private void bind(CodelistEditorEventBinder binder) {
 		binder.bindEventHandlers(this, codelistBus);
 	}
 
 	public void reload() {
 		selectionModel.clear();
 		dataGrid.setVisibleRangeAndClearData(dataGrid.getVisibleRange(), true);
 	}
 
 	private void updateRemoveButtonVisibility(boolean animate) {
 		toolBar.setEnabled(ItemButton.MINUS, codeRemotionController.canRemove(), animate);
 	}
 
 	private void setupColumns() {
 
 		Column<UICode, UICode> markerColumn = new Column<UICode, UICode>(new SafeHtmlRendererCell<UICode>(markerRenderer)) {
 
 			@Override
 			public UICode getValue(UICode object) {
 				return object;
 			}
 		};
 
 		dataGrid.addFixedWidthColumn(markerColumn, new TextHeader(""), 10);
 
 		StyledSafeHtmlRenderer codeCellRenderer = new StyledSafeHtmlRenderer(resource.dataGridStyle().textCell() + " " + resource.dataGridStyle().codeCell());
 		cellRenderer.setAddTitle(true);
 		String codeCellStyle = CommonResources.INSTANCE.css().textBox() + " " + resources.css().editor() + " " + resource.dataGridStyle().codeCell();
 		final DoubleClickEditTextCell codeCell = new DoubleClickEditTextCell(codeCellStyle, codeCellRenderer);
 		codeCell.setReadOnly(!editable);
 		editableCells.add(codeCell);
 
 		nameColumn = new CodeColumn(codeCell);
 
 		nameColumn.setSortable(true);
 
 		nameColumn.setFieldUpdater(new FieldUpdater<UICode, String>() {
 
 			@Override
 			public void update(int index, UICode row, String value) {
 				if (value != null && !value.isEmpty()) {
 					if (!value.equals(row.getName().getLocalPart())) {
 						row.getName().setLocalPart(value);
 						codeEditor.updated(row);
 					}
 				} else {
 					codeCell.clearViewData(CodeKeyProvider.INSTANCE.getKey(row));
 					dataGrid.redrawRow(index);
 				}
 			}
 		});
 
 		TextHeader nameColumnHeader = new TextHeader("Code");
 		nameColumnHeader.setHeaderStyleNames(resource.dataGridStyle().headerCell() + " " + resource.dataGridStyle().codeCell());
 		dataGrid.addColumn(nameColumn, nameColumnHeader);
 	}
 
 	public void showAllGroupsAsColumn(final boolean reloadData)
 	{
 		showLoader();
 		setGroups(groupFactory.getGroups());
 		hideLoader();
 		dataGrid.refreshColumnSizes();
 		if (reloadData) reload();
 	}
 
 	public void setEditable(boolean editable)
 	{
 		this.editable = editable;
 		for (DoubleClickEditTextCell cell:editableCells) cell.setReadOnly(!editable);
 	}
 
 	@Inject
 	private void bind(@CurrentCodelist String codelistId, FeatureBinder featureBinder)
 	{
 		featureBinder.bind(new FeatureToggler() {
 
 			@Override
 			public void toggleFeature(boolean active) {
 				toolBar.setEnabled(ItemButton.PLUS, active);
 			}
 		}, codelistId, ManagerUIFeature.ADD_CODE);
 
 		featureBinder.bind(new FeatureToggler() {
 
 			@Override
 			public void toggleFeature(boolean active) {
 				codeRemotionController.setUserCanEdit(active);
 				//we animate only if the user obtain the edit permission
 				updateRemoveButtonVisibility(active);
 			}
 		}, codelistId, ManagerUIFeature.REMOVE_CODE);
 
 		codelistBus.addHandler(DataEditEvent.getType(UICode.class), new DataEditHandler<UICode>() {
 
 			@Override
 			public void onDataEdit(DataEditEvent<UICode> event) {
 				Log.trace("onDataEdit row: "+event.getData());
 				if (event.getEditType()!=EditType.REMOVE) refreshCode(event.getData());
 			}
 		});
 
 		codelistBus.addHandler(DataEditEvent.getType(CodeAttribute.class), new DataEditHandler<CodeAttribute>() {
 
 			@Override
 			public void onDataEdit(DataEditEvent<CodeAttribute> event) {
 				refreshCode(event.getData().getCode());
 			}
 		});
 	}
 
 	@EventHandler
 	void onCodeUpdated(CodeUpdatedEvent event) {
 		refreshCode(event.getCode());
 	}
 
 	@EventHandler
 	void onSwitchGroups(SwitchGroupsEvent event) {
 		setGroups(event.getGroups());
 	}
 
 	@EventHandler
 	void onMarkerHighlight(MarkerHighlightEvent event) {
 		dataGrid.redraw();
 	}
 
 	@EventHandler
 	void onFilterOptionUpdated(FilterOptionUpdatedEvent event) {
 		dataProvider.setFilterOptions(event.getFilterOptions());
 		reload();
 	}
 
 	@EventHandler
 	void onWordUpdate(FilterWordUpdatedEvent event) {
 		dataGrid.redraw();
 	}
 
 	private void refreshCode(UICode code)
 	{
 		int absoluteIndex = dataProvider.getCache().indexOf(code) + dataGrid.getPageStart();
 		if (absoluteIndex>=0) dataGrid.redrawRow(absoluteIndex);
 	}
 
 	private void removeSelectedCode()
 	{
 		Log.trace("removeSelectedCode");
 		final UICode code = selectionModel.getSelectedObject();
 		if (code!=null) {
 
 			confirmDialog.center("Do you want to go ahead, or just mark the code as DELETED (reccomended)?", new ConfirmDialogListener() {
 
 				@Override
 				public void onButtonClick(DialogButton button) {
 					if (button == CONTINUE_BUTTON) doRemoveCode(code);
 					if (button == MARK_BUTTON) doMarkCodeDeleted(code);
 				}
 			}, CONTINUE_BUTTON, MARK_BUTTON, CANCEL_BUTTON);
 		}
 	}
 
 	private void doRemoveCode(UICode code) {
 		dataProvider.remove(code);
 		codeEditor.removed(code);
 	}
 
 	private void doMarkCodeDeleted(UICode code) {
 		UIAttribute attribute = markerTypeResolver.toAttribute(MarkerType.DELETED, "");
 		code.addAttribute(attribute);
 		attributeEditor.added(new CodeAttribute(code, attribute));
 	}
 
 	private void addNewCode()
 	{
 		Log.trace("addNewCode");
 		final UICode code = factories.createCode();
 		dataProvider.add(0, code);
 
 		//workaround to avoid select model setSelect method use due to the missing id in the new code and used by the key provider to resolve the row
 		dataGrid.setKeyboardSelectedRow(0, true);
 
 		dataGrid.getScrollPanel().setVerticalScrollPosition(0);
 		dataGrid.getScrollPanel().setHorizontalScrollPosition(0);
 		codeEditor.added(code);
 	}
 
 	private DoubleClickEditTextCell createCell(boolean isEditable)
 	{
 		String editorStyle = CommonResources.INSTANCE.css().textBox() + " " + resources.css().editor();
 		DoubleClickEditTextCell cell = new DoubleClickEditTextCell(editorStyle, cellRenderer);
 		if (isEditable) {
 			cell.setReadOnly(!editable);
 			editableCells.add(cell);
 		}
 		return cell;
 	}
 
 	private Column<UICode, String> getGroupColumn(final Group group)
 	{
 		Log.trace("getGroupColumn group: "+group);
 		Column<UICode, String> column = groupsColumns.get(group);
 		if (column == null) {
 			column = new GroupColumn(createCell(group.isEditable()), group);
 			//column.setCellStyleNames(group.isSystemGroup()?resources.css().systemProperty():"");
 			column.setSortable(group.isSortable());
 
 			if (group.isEditable()) {
 				column.setFieldUpdater(new FieldUpdater<UICode, String>() {
 
 					@Override
 					public void update(int index, UICode code, String value) {
 						groupColumnUpdated(code, group, value);
 					}
 				});
 			}
 
 
 			groupsColumns.put(group, column);
 		}
 		return column;
 	}
 
 	private void groupColumnUpdated(UICode code, Group group, String value) {
 
 		if (group instanceof AttributeGroup) {
 			AttributeGroup attributeGroup = (AttributeGroup)group;
 
 			UIAttribute attribute = attributeGroup.match(code.getAttributes());
 			if (attribute!=null) {
 				if (!value.equals(attribute.getValue())) {
 					attribute.setValue(value);
 					attributeEditor.updated(new CodeAttribute(code, attribute));
 				}
 			} else {
 				attribute = factories.createAttribute();
 				attribute.setName(attributeGroup.getName());
 				attribute.setLanguage(attributeGroup.getLanguage());
 				attribute.setValue(value);
				attribute.setDefinitionId(attributeGroup.getDefinition().getId());
 				code.addAttribute(attribute);
 				attributeEditor.added(new CodeAttribute(code, attribute));
 			}
 		}
 	}
 
 	private void setGroups(List<Group> groups) {
 		Log.trace("setGroups groups: "+groups);
 
 		showLoader();
 
 		List<Column<UICode, ?>> oldColumns = toColumns(groupsAsColumn);
 		groupsAsColumn.clear();
 
 		List<Column<UICode, ?>> columns = new ArrayList<Column<UICode, ?>>(groups.size());
 		List<Header<?>> headers = new ArrayList<Header<?>>(groups.size());
 
 		Log.trace("preparing columns");
 		for (Group group:groups) {
 			Column<UICode, String> column = getGroupColumn(group);
 			columns.add(column);
 
 			groupsAsColumn.add(group);
 			
 			SafeHtmlHeader header = new SafeHtmlHeader(group.getLabel());
 			header.setHeaderStyleNames(resource.dataGridStyle().headerCell());
 			headers.add(header);
 		}
 		dataGrid.replaceColumns(oldColumns, columns, headers);
 
 		Log.trace("done");
 		
 		codelistBus.fireEvent(new GroupsSwitchedEvent(groups));
 
 		hideLoader();
 	}
 
 	private List<Column<UICode, ?>> toColumns(List<Group> groups) {
 		List<Column<UICode, ?>> columns = new ArrayList<Column<UICode, ?>>(groups.size());
 		for (Group group:groups) columns.add(getGroupColumn(group));
 		return columns;
 	}
 
 	public static abstract class CodelistEditorColumn<C> extends Column<UICode, C> {
 
 		public CodelistEditorColumn(Cell<C> cell) {
 			super(cell);
 		}
 	}
 
 	public static class CodeColumn extends CodelistEditorColumn<String> {
 
 		public CodeColumn(Cell<String> cell) {
 			super(cell);
 		}
 
 		@Override
 		public String getValue(UICode object) {
 			if (object == null) return null;
 			return ValueUtils.getValue(object.getName());
 		}
 	}
 
 	public static class GroupColumn extends CodelistEditorColumn<String> {
 
 		protected Group group;
 
 		public GroupColumn(Cell<String> cell, Group group) {
 			super(cell);
 			this.group = group;
 		}
 
 		@Override
 		public String getValue(UICode code) {
 			if (code == null) return "";
 			return group.getValue(code);
 		}
 
 		public Group getGroup() {
 			return group;
 		}
 	}
 	
 }
