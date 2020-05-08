 package org.cotrix.web.ingest.client.step.selection;
 
 import org.cotrix.web.common.client.resources.CommonResources;
 import org.cotrix.web.common.client.resources.DataGridListResource;
 import org.cotrix.web.common.client.widgets.PageSizer;
 import org.cotrix.web.common.client.widgets.SearchBox;
 import org.cotrix.web.common.client.widgets.cell.ClickableCell;
 import org.cotrix.web.common.client.widgets.cell.SelectionCheckBoxCell;
 import org.cotrix.web.common.client.widgets.cell.UIQNameRenderer;
 import org.cotrix.web.common.client.widgets.dialog.AlertDialog;
 import org.cotrix.web.common.shared.codelist.UIQName;
 import org.cotrix.web.ingest.shared.AssetInfo;
 
 import com.allen_sauer.gwt.log.client.Log;
 import com.google.gwt.cell.client.ClickableTextCell;
 import com.google.gwt.cell.client.FieldUpdater;
 import com.google.gwt.cell.client.TextCell;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.logical.shared.ValueChangeEvent;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiHandler;
 import com.google.gwt.uibinder.client.UiTemplate;
 import com.google.gwt.user.cellview.client.Column;
 import com.google.gwt.user.cellview.client.ColumnSortEvent.AsyncHandler;
 import com.google.gwt.user.cellview.client.PatchedDataGrid;
 import com.google.gwt.user.cellview.client.SimplePager;
 import com.google.gwt.user.cellview.client.TextHeader;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.ResizeComposite;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.gwt.view.client.SelectionChangeEvent;
 import com.google.gwt.view.client.SingleSelectionModel;
 import com.google.inject.Inject;
 import com.google.inject.Singleton;
 
 /**
  * @author "Federico De Faveri federico.defaveri@fao.org"
  *
  */
 @Singleton
 public class SelectionStepViewImpl extends ResizeComposite implements SelectionStepView {
 
 	@UiTemplate("SelectionStep.ui.xml")
 	interface ChannelStepUiBinder extends UiBinder<Widget, SelectionStepViewImpl> {}
 
 	private static ChannelStepUiBinder uiBinder = GWT.create(ChannelStepUiBinder.class);
 	
 	@UiField (provided = true) 
 	PatchedDataGrid<AssetInfo> dataGrid;
 
 	@UiField(provided = true)
 	SimplePager pager;
 	
 	@UiField
 	PageSizer pageSizer;
 	
 	@UiField
 	SearchBox searchBox;
 	
 	protected AssetInfoDataProvider dataProvider;
 	
 	protected SingleSelectionModel<AssetInfo> selectionModel;
 
 	private Presenter presenter;
 	
 	@Inject
 	AlertDialog alertDialog;
 
 	@Inject
 	public SelectionStepViewImpl(AssetInfoDataProvider assetInfoDataProvider) {
 		this.dataProvider = assetInfoDataProvider;
 		setupGrid();
 		initWidget(uiBinder.createAndBindUi(this));
 		pageSizer.setDisplay(dataGrid);
 	}
 
 	/** 
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void setVisible(boolean visible) {
 		super.setVisible(visible);
 		
 		//GWT issue 7188 workaround
 		if (visible) dataGrid.onResize();
 	}
 
 
 	protected void setupGrid()
 	{
 
 		dataGrid = new PatchedDataGrid<AssetInfo>(25, DataGridListResource.INSTANCE, AssetInfoKeyProvider.INSTANCE);
 		dataGrid.setWidth("100%");
 
 		dataGrid.setAutoHeaderRefreshDisabled(true);

		dataGrid.setEmptyTableWidget(new Label("No data"));
 
 		pager = new SimplePager();
 		pager.setDisplay(dataGrid);
 		
 		dataGrid.addColumnSortHandler(new AsyncHandler(dataGrid));
 		
 		selectionModel = new SingleSelectionModel<AssetInfo>(AssetInfoKeyProvider.INSTANCE);
 		selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
 			
 			@Override
 			public void onSelectionChange(SelectionChangeEvent event) {
 				AssetInfo selected = selectionModel.getSelectedObject();
 				if (selected!=null) presenter.assetSelected(selected);	 
 			}
 		});
 		
 		dataGrid.setSelectionModel(selectionModel);
 
 		// Check
 		TextHeader nameHeader = new TextHeader("Name");
 		
 		Column<AssetInfo, Boolean> checkColumn = new Column<AssetInfo, Boolean>(new SelectionCheckBoxCell(true, false)) {
 			
 			@Override
 			public Boolean getValue(AssetInfo object) {
 				boolean selected = selectionModel.isSelected(object);
 				return selected;
 			}
 		};
 		
 		dataGrid.addColumn(checkColumn, nameHeader);
 		dataGrid.setColumnWidth(checkColumn, "35px");
 		
 		// Name
 		Column<AssetInfo, String> nameColumn = new Column<AssetInfo, String>(new ClickableTextCell()) {
 			@Override
 			public String getValue(AssetInfo object) {
 				return object.getName();
 			}
 		};
 		nameColumn.setSortable(true);
 		nameColumn.setDataStoreName(AssetInfo.NAME_FIELD);
 		
 		nameColumn.setFieldUpdater(new FieldUpdater<AssetInfo, String>() {
 
 			@Override
 			public void update(int index, AssetInfo object, String value) {
 				Log.trace("details selected for row "+index);
 				presenter.assetDetails(object);
 			}
 		});
 		
 		nameColumn.setCellStyleNames(CommonResources.INSTANCE.css().linkText());
 		
 		dataGrid.addColumn(nameColumn, nameHeader);
 		
 		// Version
 		Column<AssetInfo, String> versionColumn = new Column<AssetInfo, String>(new ClickableTextCell()) {
 			@Override
 			public String getValue(AssetInfo object) {
 				return object.getVersion();
 			}
 		};
 		versionColumn.setSortable(true);
 		versionColumn.setDataStoreName(AssetInfo.VERSION_FIELD);
 		
 		dataGrid.addColumn(versionColumn, "Version");
		dataGrid.setColumnWidth(versionColumn, "70px");
 
 
 		// Type
 		Column<AssetInfo, String> typeColumn = new Column<AssetInfo, String>(new TextCell()) {
 			@Override
 			public String getValue(AssetInfo object) {
 				return object.getType();
 			}
 		};
 		
 		typeColumn.setSortable(false);
 		dataGrid.addColumn(typeColumn, "Type");
 		dataGrid.setColumnWidth(typeColumn, "70px");
 		
 
 		// Repository
 		Column<AssetInfo, UIQName> repositoryColumn = new Column<AssetInfo, UIQName>(new ClickableCell<UIQName>(new UIQNameRenderer(true))) {
 			@Override
 			public UIQName getValue(AssetInfo object) {
 				return object.getRepositoryName();
 			}
 		};
 		repositoryColumn.setSortable(true);
 		repositoryColumn.setDataStoreName(AssetInfo.REPOSITORY_FIELD);
 		repositoryColumn.setFieldUpdater(new FieldUpdater<AssetInfo, UIQName>() {
 
 			@Override
 			public void update(int index, AssetInfo object, UIQName value) {
 				Log.trace("repository details selected for row "+index);
 				presenter.repositoryDetails(object.getRepositoryId());
 			}
 		});
 		repositoryColumn.setCellStyleNames(CommonResources.INSTANCE.css().linkText());
 		
 		dataGrid.addColumn(repositoryColumn, "Origin");
 		dataGrid.setColumnWidth(repositoryColumn, "30%");
 			
 		dataProvider.setDatagrid(dataGrid);
 		dataProvider.addDataDisplay(dataGrid);
 	}
 
 	public void setPresenter(Presenter presenter) {
 		this.presenter = presenter;
 	}
 
 	public void alert(String message) {
 		alertDialog.center(message);
 	}
 
 	
 	@UiHandler("refreshButton")
 	protected void refresh(ClickEvent clickEvent)
 	{
 		dataProvider.setRequestDiscovery(true);
 		dataGrid.setVisibleRangeAndClearData(dataGrid.getVisibleRange(), true);
 	}
 	
 	@UiHandler("searchBox")
 	protected void onValueChange(ValueChangeEvent<String> event) {
 		updateFilter(event.getValue());
 	}
 	
 	private void updateFilter(String query) {
 		dataProvider.setQuery(query);
 		dataGrid.setVisibleRangeAndClearData(dataGrid.getVisibleRange(), true);
 	}
 	
 	private void cleanFilter() {
 		dataProvider.setQuery("");
 		searchBox.clear();
 	}
 	
 	public void reset()
 	{
 		cleanFilter();	
 		selectionModel.clear();
 		Log.trace("setRefreshCache");
 		dataProvider.setRefreshCache(true);
 		pager.setPage(0);
 		dataGrid.setVisibleRangeAndClearData(dataGrid.getVisibleRange(), true);
 	}
 
 }
