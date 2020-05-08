 package org.cotrix.web.importwizard.client.step.selection;
 
 import org.cotrix.web.importwizard.client.resources.Resources;
 import org.cotrix.web.importwizard.shared.AssetInfo;
 import org.cotrix.web.share.client.resources.CommonResources;
 import org.cotrix.web.share.client.resources.CotrixSimplePager;
 import org.cotrix.web.share.client.resources.DataGridListResource;
 import org.cotrix.web.share.client.widgets.AlertDialog;
 import org.cotrix.web.share.client.widgets.SelectionCheckBoxCell;
 
 import com.allen_sauer.gwt.log.client.Log;
 import com.google.gwt.cell.client.ClickableTextCell;
 import com.google.gwt.cell.client.FieldUpdater;
 import com.google.gwt.cell.client.TextCell;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiHandler;
 import com.google.gwt.uibinder.client.UiTemplate;
 import com.google.gwt.user.cellview.client.Column;
 import com.google.gwt.user.cellview.client.ColumnSortEvent.AsyncHandler;
 import com.google.gwt.user.cellview.client.PatchedDataGrid;
 import com.google.gwt.user.cellview.client.SimplePager;
 import com.google.gwt.user.cellview.client.TextHeader;
 import com.google.gwt.user.cellview.client.SimplePager.TextLocation;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.ResizeComposite;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.gwt.view.client.SelectionChangeEvent;
 import com.google.gwt.view.client.SingleSelectionModel;
 import com.google.inject.Inject;
 
 /**
  * @author "Federico De Faveri federico.defaveri@fao.org"
  *
  */
 public class SelectionStepViewImpl extends ResizeComposite implements SelectionStepView {
 
 	@UiTemplate("SelectionStep.ui.xml")
 	interface ChannelStepUiBinder extends UiBinder<Widget, SelectionStepViewImpl> {}
 
 	private static ChannelStepUiBinder uiBinder = GWT.create(ChannelStepUiBinder.class);
 	
 	@UiField (provided = true) 
 	PatchedDataGrid<AssetInfo> dataGrid;
 
 	@UiField(provided = true)
 	SimplePager pager;
 	
 	protected AssetInfoDataProvider dataProvider;
 	
 	protected SingleSelectionModel<AssetInfo> selectionModel;
 
 	private Presenter presenter;
 
 	@Inject
 	public SelectionStepViewImpl(AssetInfoDataProvider assetInfoDataProvider) {
 		this.dataProvider = assetInfoDataProvider;
 		Resources.INSTANCE.css().ensureInjected();
 		setupGrid();
 		initWidget(uiBinder.createAndBindUi(this));
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
 
 		dataGrid = new PatchedDataGrid<AssetInfo>(6, DataGridListResource.INSTANCE, AssetInfoKeyProvider.INSTANCE);
 		dataGrid.setWidth("100%");
 
 		dataGrid.setAutoHeaderRefreshDisabled(true);
 
 		dataGrid.setEmptyTableWidget(new Label("No data"));
 
 		pager = new SimplePager(TextLocation.CENTER, CotrixSimplePager.INSTANCE, false, 0, true);
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
 
 
 		// Type
 		Column<AssetInfo, String> typeColumn = new Column<AssetInfo, String>(new TextCell()) {
 			@Override
 			public String getValue(AssetInfo object) {
 				return object.getType();
 			}
 		};
 		
 		typeColumn.setSortable(false);
 		dataGrid.addColumn(typeColumn, "Type");
 		dataGrid.setColumnWidth(typeColumn, "20%");
 		
 
 		// Repository
 		Column<AssetInfo, String> repositoryColumn = new Column<AssetInfo, String>(new ClickableTextCell()) {
 			@Override
 			public String getValue(AssetInfo object) {
 				return object.getRepositoryName();
 			}
 		};
 		repositoryColumn.setSortable(true);
 		repositoryColumn.setDataStoreName(AssetInfo.REPOSITORY_FIELD);
 		repositoryColumn.setFieldUpdater(new FieldUpdater<AssetInfo, String>() {
 
 			@Override
 			public void update(int index, AssetInfo object, String value) {
 				Log.trace("repository details selected for row "+index);
 				presenter.repositoryDetails(object.getRepositoryId());
 			}
 		});
 		repositoryColumn.setCellStyleNames(CommonResources.INSTANCE.css().linkText());
 		
 		dataGrid.addColumn(repositoryColumn, "Origin");
 		dataGrid.setColumnWidth(repositoryColumn, "20%");
 			
 		dataProvider.setDatagrid(dataGrid);
 		dataProvider.addDataDisplay(dataGrid);
 	}
 
 	public void setPresenter(Presenter presenter) {
 		this.presenter = presenter;
 	}
 
 	public void alert(String message) {
 		AlertDialog.INSTANCE.center(message);
 	}
 
 	
 	@UiHandler("refreshButton")
 	protected void refresh(ClickEvent clickEvent)
 	{
 		dataProvider.setForceRefresh(true);
 		dataGrid.setVisibleRangeAndClearData(dataGrid.getVisibleRange(), true);
 	}
 	
 	public void reset()
 	{
 		selectionModel.clear();
 		pager.setPage(0);
 	}
 
 }
