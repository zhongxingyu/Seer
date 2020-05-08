 package org.cotrix.web.importwizard.client.step.selection;
 
 import org.cotrix.web.importwizard.client.resources.CotrixSimplePager;
 import org.cotrix.web.importwizard.client.resources.DataGridResource;
 import org.cotrix.web.importwizard.client.resources.Resources;
 import org.cotrix.web.importwizard.client.util.AlertDialog;
 import org.cotrix.web.importwizard.shared.AssetInfo;
 
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
 import com.google.gwt.user.cellview.client.DataGrid;
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
 	DataGrid<AssetInfo> dataGrid;
 
 	@UiField(provided = true)
 	SimplePager pager;
 	
 	protected AssetInfoDataProvider dataProvider;
 	
 	protected SingleSelectionModel<AssetInfo> selectionModel;
 
 	private AlertDialog alertDialog;
 
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
 		
 		//TODO we should use ResizeComposite instead of Composite
 		//The problem would be in the DeckLayoutPanel that don't call onresize
 		if (visible) dataGrid.onResize();
 	}
 
 
 	protected void setupGrid()
 	{
 
 		dataGrid = new DataGrid<AssetInfo>(6, DataGridResource.INSTANCE, AssetInfoKeyProvider.INSTANCE);
 		dataGrid.setWidth("100%");
 
 		dataGrid.setAutoHeaderRefreshDisabled(true);
 
 		dataGrid.setEmptyTableWidget(new Label("No data"));
 
 		SimplePager.Resources pagerResources = GWT.create(CotrixSimplePager.class);
 		pager = new SimplePager(TextLocation.CENTER, pagerResources, false, 0, true);
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
 		
 		nameColumn.setCellStyleNames(Resources.INSTANCE.css().linkText());
 		
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
 		repositoryColumn.setCellStyleNames(Resources.INSTANCE.css().linkText());
 		
 		dataGrid.addColumn(repositoryColumn, "Origin");
 		dataGrid.setColumnWidth(repositoryColumn, "20%");
 			
 		dataProvider.setDatagrid(dataGrid);
 		dataProvider.addDataDisplay(dataGrid);
 	}
 
 	public void setPresenter(Presenter presenter) {
 		this.presenter = presenter;
 	}
 
 	public void alert(String message) {
 		if(alertDialog == null){
 			alertDialog = new AlertDialog(false);
 		}
 		alertDialog.setMessage(message);
 		alertDialog.show();
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
		dataGrid.redraw();
		dataGrid.setVisibleRangeAndClearData(dataGrid.getVisibleRange(), true);
 	}
 
 }
