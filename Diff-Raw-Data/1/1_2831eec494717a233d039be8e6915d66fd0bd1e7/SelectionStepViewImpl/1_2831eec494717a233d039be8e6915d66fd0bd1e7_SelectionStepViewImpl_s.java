 package org.cotrix.web.importwizard.client.step.selection;
 
 import org.cotrix.web.importwizard.client.util.AlertDialog;
 import org.cotrix.web.importwizard.shared.AssetDetails;
 import org.cotrix.web.importwizard.shared.AssetInfo;
 
 import com.google.gwt.cell.client.ButtonCell;
 import com.google.gwt.cell.client.FieldUpdater;
 import com.google.gwt.cell.client.TextCell;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiTemplate;
 import com.google.gwt.user.cellview.client.Column;
 import com.google.gwt.user.cellview.client.DataGrid;
 import com.google.gwt.user.cellview.client.SimplePager;
 import com.google.gwt.user.cellview.client.SimplePager.TextLocation;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.gwt.view.client.SelectionChangeEvent;
 import com.google.gwt.view.client.SelectionChangeEvent.Handler;
 import com.google.gwt.view.client.SingleSelectionModel;
 import com.google.inject.Inject;
 
 /**
  * @author "Federico De Faveri federico.defaveri@fao.org"
  *
  */
 public class SelectionStepViewImpl extends Composite implements SelectionStepView {
 
 	protected static final String[] headers = new String[]{"Name", "Type", "Repository"};
 
 	@UiTemplate("SelectionStep.ui.xml")
 	interface ChannelStepUiBinder extends UiBinder<Widget, SelectionStepViewImpl> {}
 
 	private static ChannelStepUiBinder uiBinder = GWT.create(ChannelStepUiBinder.class);
 
 	@UiField (provided = true) 
 	DataGrid<AssetInfo> dataGrid;
 
 	@UiField(provided = true)
 	SimplePager pager;
 	
 	protected AssetInfoDataProvider assetInfoDataProvider;
 
 	private AlertDialog alertDialog;
 	
 	protected CodelistDetailsDialog detailsDialog;
 
 	private Presenter presenter;
 
 	@Inject
 	public SelectionStepViewImpl(AssetInfoDataProvider assetInfoDataProvider) {
 		this.assetInfoDataProvider = assetInfoDataProvider;
 		
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
 		dataGrid = new DataGrid<AssetInfo>(AssetInfoKeyProvider.INSTANCE);
 		dataGrid.setWidth("100%");
 		dataGrid.setPageSize(10);
 
 		dataGrid.setAutoHeaderRefreshDisabled(true);
 
 		dataGrid.setEmptyTableWidget(new Label("No data"));
 
 		SimplePager.Resources pagerResources = GWT.create(SimplePager.Resources.class);
 		pager = new SimplePager(TextLocation.CENTER, pagerResources, false, 0, true);
 		pager.setDisplay(dataGrid);
 
 		final SingleSelectionModel<AssetInfo> selectionModel = new SingleSelectionModel<AssetInfo>(AssetInfoKeyProvider.INSTANCE);
 		selectionModel.addSelectionChangeHandler(new Handler() {
 			
 			@Override
 			public void onSelectionChange(SelectionChangeEvent event) {
 				AssetInfo selectedAsset = selectionModel.getSelectedObject();
 				presenter.assetSelected(selectedAsset);
 			}
 		});
 		dataGrid.setSelectionModel(selectionModel);
 
 		// Name
 		Column<AssetInfo, String> nameColumn = new Column<AssetInfo, String>(new TextCell()) {
 			@Override
 			public String getValue(AssetInfo object) {
 				return object.getName();
 			}
 		};
 		nameColumn.setSortable(false);
 		/*sortHandler.setComparator(firstNameColumn, new Comparator<ContactInfo>() {
 	      @Override
 	      public int compare(ContactInfo o1, ContactInfo o2) {
 	        return o1.getFirstName().compareTo(o2.getFirstName());
 	      }
 	    });*/
 		dataGrid.addColumn(nameColumn, "Name");
 		
 
 		// type
 		Column<AssetInfo, String> typeColumn = new Column<AssetInfo, String>(new TextCell()) {
 			@Override
 			public String getValue(AssetInfo object) {
 				return object.getType();
 			}
 		};
 		typeColumn.setSortable(false);
 		/*sortHandler.setComparator(firstNameColumn, new Comparator<ContactInfo>() {
 			      @Override
 			      public int compare(ContactInfo o1, ContactInfo o2) {
 			        return o1.getFirstName().compareTo(o2.getFirstName());
 			      }
 			    });*/
 		dataGrid.addColumn(typeColumn, "Type");
 		
 
 		// repository
 		Column<AssetInfo, String> repositoryColumn = new Column<AssetInfo, String>(new TextCell()) {
 			@Override
 			public String getValue(AssetInfo object) {
 				return object.getRepositoryName();
 			}
 		};
 		repositoryColumn.setSortable(false);
 		/*sortHandler.setComparator(firstNameColumn, new Comparator<ContactInfo>() {
 			      @Override
 			      public int compare(ContactInfo o1, ContactInfo o2) {
 			        return o1.getFirstName().compareTo(o2.getFirstName());
 			      }
 			    });*/
 		dataGrid.addColumn(repositoryColumn, "Repository");
 		
 		
 		//details
 		Column<AssetInfo, String> detailsColumn = new Column<AssetInfo, String>(new ButtonCell()) {
 			@Override
 			public String getValue(AssetInfo object) {
 				return "Details";
 			}
 		};
 		detailsColumn.setSortable(false);
 		detailsColumn.setFieldUpdater(new FieldUpdater<AssetInfo, String>() {
 			
 			@Override
 			public void update(int index, AssetInfo object, String value) {
 				presenter.assetDetails(object);
 			}
 		});
 		
 		dataGrid.addColumn(detailsColumn, "");
 		
 		
 		
 		assetInfoDataProvider.addDataDisplay(dataGrid);
 		
 	}
 
 	public void setPresenter(Presenter presenter) {
 		this.presenter = presenter;
 	}
 
 	public void alert(String message) {
 		if(alertDialog == null){
 			alertDialog = new AlertDialog();
 		}
 		alertDialog.setMessage(message);
 		alertDialog.show();
 	}
 
 
 	@Override
 	public void showAssetDetails(AssetDetails asset) {
 		if (detailsDialog == null) detailsDialog = new CodelistDetailsDialog();
 		detailsDialog.setAsset(asset);
 		detailsDialog.center();
 	}
 	
 	public void reset()
 	{
 		dataGrid.redraw();
 	}
 
 
 
 }
