 package com.scholastic.sbam.client.uiobjects.uireports;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
 import com.extjs.gxt.ui.client.Style.SelectionMode;
 import com.extjs.gxt.ui.client.Style.SortDir;
 import com.extjs.gxt.ui.client.data.BaseFilterPagingLoadConfig;
 import com.extjs.gxt.ui.client.data.BasePagingLoader;
 import com.extjs.gxt.ui.client.data.BeanModel;
 import com.extjs.gxt.ui.client.data.BeanModelReader;
 import com.extjs.gxt.ui.client.data.ModelData;
 import com.extjs.gxt.ui.client.data.PagingLoadConfig;
 import com.extjs.gxt.ui.client.data.PagingLoadResult;
 import com.extjs.gxt.ui.client.data.PagingLoader;
 import com.extjs.gxt.ui.client.data.RpcProxy;
 import com.extjs.gxt.ui.client.store.GroupingStore;
 import com.extjs.gxt.ui.client.store.ListStore;
 import com.extjs.gxt.ui.client.widget.ContentPanel;
 import com.extjs.gxt.ui.client.widget.MessageBox;
 import com.extjs.gxt.ui.client.widget.grid.AggregationRowConfig;
 import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
 import com.extjs.gxt.ui.client.widget.grid.ColumnData;
 import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
 import com.extjs.gxt.ui.client.widget.grid.Grid;
 import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
 import com.extjs.gxt.ui.client.widget.grid.GroupSummaryView;
 import com.extjs.gxt.ui.client.widget.grid.HeaderGroupConfig;
 import com.extjs.gxt.ui.client.widget.grid.LiveGridView;
 import com.extjs.gxt.ui.client.widget.grid.SummaryColumnConfig;
 import com.extjs.gxt.ui.client.widget.grid.SummaryType;
 import com.extjs.gxt.ui.client.widget.grid.filters.DateFilter;
 import com.extjs.gxt.ui.client.widget.grid.filters.GridFilters;
 import com.extjs.gxt.ui.client.widget.grid.filters.ListFilter;
 import com.extjs.gxt.ui.client.widget.grid.filters.NumericFilter;
 import com.extjs.gxt.ui.client.widget.grid.filters.StringFilter;
 import com.extjs.gxt.ui.client.widget.layout.FitLayout;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.i18n.client.DateTimeFormat;
 import com.google.gwt.i18n.client.NumberFormat;
 import com.google.gwt.user.client.Timer;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.scholastic.sbam.client.services.SnapshotParameterSetGetService;
 import com.scholastic.sbam.client.services.SnapshotParameterSetGetServiceAsync;
 import com.scholastic.sbam.client.services.SnapshotTakeService;
 import com.scholastic.sbam.client.services.SnapshotTakeServiceAsync;
 import com.scholastic.sbam.client.services.SnapshotTermDataListService;
 import com.scholastic.sbam.client.services.SnapshotTermDataListServiceAsync;
 import com.scholastic.sbam.client.stores.ExtendedStoreSorter;
 import com.scholastic.sbam.client.util.IconSupplier;
 import com.scholastic.sbam.client.util.UiConstants;
 import com.scholastic.sbam.shared.exceptions.ServiceNotReadyException;
 import com.scholastic.sbam.shared.objects.SnapshotParameterSetInstance;
 import com.scholastic.sbam.shared.objects.SnapshotParameterValueObject;
 import com.scholastic.sbam.shared.objects.SnapshotTermDataInstance;
 import com.scholastic.sbam.shared.objects.SnapshotInstance;
 import com.scholastic.sbam.shared.objects.SynchronizedPagingLoadResult;
 import com.scholastic.sbam.shared.reporting.SnapshotParameterNames;
 
 public class TermReportViewDataCard extends SnapshotCardBase {
 	
 	/**
 	 * The BetterListFilter improves on the ListFilter by properly distinguishing between a display value and a key value.
 	 * 
 	 * The valueProperty may be set to the name of whatever property uniquely identifies an instance in a list of instances.
 	 * 
 	 * The displayProperty, as before, can be set to the name of whatever property should be displayed to the user to identify an instance.
 	 * 
 	 * @author Bob Llacatena
 	 *
 	 * @param <M>
 	 */
 	@SuppressWarnings({"unchecked", "rawtypes"})
 	public class BetterListFilter<M> extends ListFilter {
 		
 		private String valueProperty;
 
 		public BetterListFilter(String dataIndex, ListStore store) {
 			super(dataIndex, store);
 		}
 
 		@Override
 		protected <X> X getModelValue(ModelData model) {
 			return (X) ((ModelData) model.get(dataIndex)).get(valueProperty);
 		}
 
 		public String getValueProperty() {
 			return valueProperty;
 		}
 
 		public void setValueProperty(String valueProperty) {
 			this.valueProperty = valueProperty;
 		}
 	}
 	
 	public static final String [] OTHER_TERM_DATA_SORTS = {"institution.institutionName", "ucn", "ucnSuffix", "agreementId", "productCode", "serviceCode", "rowId"};
 	
 	protected SnapshotParameterSetInstance								snapshotParameterSet	= null;
 	
 	protected long														searchSyncId;
 	protected LiveGridView												liveView;
 	protected PagingLoader<PagingLoadResult<SnapshotTermDataInstance>>	termDataLoader;
 
 	protected ContentPanel								contentPanel	=	 getNewContentPanel();
 	
 	protected ListStore<BeanModel>						gridStore;
 	protected Grid<BeanModel>							grid;
 	protected GridFilters 								gridFilters;
 	
 	protected final SnapshotTakeServiceAsync 			takeSnapshotService					= GWT.create(SnapshotTakeService.class);
 	protected final SnapshotTermDataListServiceAsync 	snapshotTermDataListService 		= GWT.create(SnapshotTermDataListService.class);
	protected final SnapshotParameterSetGetServiceAsync	snapshotParameterSetGetService		= GWT.create(SnapshotParameterSetGetService.class);
 	
 	public TermReportViewDataCard() {
 		super();
 		this.headingToolTip = "Use this panel to view a terms based report.";
 	}
 
 	@Override
 	public void addPanelContent() {
 //		contentPanel = new ContentPanel();
 		contentPanel.setLayout(new FitLayout());
 //		contentPanel.setHeading("Snapshot Terms Data View");
 		IconSupplier.setIcon(contentPanel, IconSupplier.getReportIconName());
 		
 		grid = getGrid(); 
 		contentPanel.add(grid);
 		
 		add(contentPanel);
 		
 		if (snapshot != null)
 			gridStore.getLoader().load();
 	}
 	
 	public ContentPanel getNewContentPanel() {
 		ContentPanel contentPanel = new ContentPanel();
 		contentPanel.setLayout(new FitLayout());
 		contentPanel.setHeading(getPanelTitle());
 		IconSupplier.setIcon(contentPanel, IconSupplier.getReportIconName());
 		return contentPanel;
 	}
 	
 	protected Grid<BeanModel> getGrid() {
 
 		gridFilters = new GridFilters(); // Have to create this here, before adding the grid columns, so the filters get created with the columns
 		
 		List<ColumnConfig> columns = new ArrayList<ColumnConfig>();
 		
 		addGridColumns(columns);
 		
 		ColumnModel cm = new ColumnModel(columns);  
 		
 		addAggregationRows(cm);
 		addHeaderGroups(cm);
 
 		gridStore = getNewGridStore();
 		
 		grid = new Grid<BeanModel>(gridStore, cm); 
 //		grid.addPlugin(expander);
 		grid.setBorders(true);  
 //		grid.setAutoExpandColumn(autoExpandColumn);  
 		grid.setLoadMask(true);
 		grid.setStripeRows(true);
 		grid.setColumnLines(false);
 		grid.setHideHeaders(false);
 //		grid.setHeight(600);
 		
 		gridFilters.setLocal(areGridFiltersLocal()); 
 		grid.addPlugin(gridFilters);
 
 		grid.setAutoExpandColumn("institution.institutionName");
 //		addGridPlugins(grid);	
 //		addRowListener(grid);
 		
 		grid.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
 		
 	    GroupSummaryView summary = new GroupSummaryView();  
 	    summary.setForceFit(true);  
 	    summary.setShowGroupedColumn(false);
 	    summary.setEmptyText("No data (agreements or product terms) qualified for this snapshot.");
 	    
 	    grid.setView(summary);
 
 		return grid;
 	}
 
 	public void addGridColumns(List<ColumnConfig> columns) {
 		columns.add(getDisplayColumn("rowId",					"Row",						60,
 					"A unique number for the data row."));
 		columns.add(getDisplayColumn("agreementIdCheckDigit",	"Agreement #",				80,
 					"This is the agreement number for this service."));
 		columns.add(getDisplayColumn("ucn",						"UCN",						80,
 					"This is the UCN for this service."));
 		columns.add(getDisplayColumn("institution.institutionName",	"Institution",			200,
 					"This is the institution for this service."));
 		columns.add(getHiddenColumn("institution.city",			"City",						100,
 					"This is the institution city for this service."));
 		columns.add(getHiddenColumn("institution.state",		"State",					60,
 					"This is the institution state for this service."));
 		columns.add(getHiddenColumn("productCode",				"Product Code",				80,
 					"This is the product code for this product term."));
 		columns.add(getDisplayColumn("product.description",		"Product",					200,
 					"This is the product for this product term."));
 		columns.add(getHiddenColumn("serviceCode",				"Service Code",				80,
 					"This is the service code for a service supplied with this product term."));
 		columns.add(getDisplayColumn("service.description",		"Service",					200,
 					"This is a service supplied with this product term."));
 		columns.add(getDisplayColumn("startDate",				"Start",					80,		true, UiConstants.APP_DATE_TIME_FORMAT,
 					"This is the service start date for a product term."));
 		columns.add(getDisplayColumn("endDate",					"End",						80,		true, UiConstants.APP_DATE_TIME_FORMAT,
 					"This is the service end date for a product term."));
 		columns.add(getDisplayColumn("terminateDate",			"Terminate",				80,		true, UiConstants.APP_DATE_TIME_FORMAT,
 					"This is the actual service termination date for a product term."));
 		columns.add(getDisplayColumn("dollarValue",				"Full Value",				100,	true, UiConstants.DOLLARS_FORMAT,
 					"This is the full value of the product term."));
 		columns.add(getGridSummaryColumn("dollarFraction",		"Row Value",			100,	false, true, UiConstants.DOLLARS_FORMAT,
 					"This is the fraction of the total term value for this particular row (this service for this UCN)."));
 		columns.add(getHiddenColumn("dollarServiceFraction",	"Service Value",			80,		true, UiConstants.DOLLARS_FORMAT,
 					"This is the fraction of the total term value for this particular service."));
 		columns.add(getHiddenColumn("dollarUcnFraction",		"UCN Value",				80,		true, UiConstants.DOLLARS_FORMAT,
 					"This is the fraction of the total term value for this particular UCN."));
 		columns.add(getHiddenColumn("termTypeCode",				"Type Code",				75,
 					"This is the term type code for this product term."));
 		columns.add(getDisplayColumn("termType.description",	"Term Type",				80,
 					"This is the type of product term.", UiConstants.getTermTypes(), "termTypeCode", "description"));
 		columns.add(getHiddenColumn("primaryTerm",				"Primary?",					50,
 					"Is this the primary product term for the agreement?"));
 	}
 	
 	protected void addAggregationRows(ColumnModel cm) {
 		AggregationRowConfig<BeanModel> averages = new AggregationRowConfig<BeanModel>();  
 	    averages.setHtml("dollarValue", "Average");  
 	      
 	    // with summary type and format  
 	    averages.setSummaryType("dollarFraction", SummaryType.AVG);  
 	    averages.setSummaryFormat("dollarFraction", UiConstants.DOLLARS_FORMAT);  
 	      
 //	    // with renderer  
 //	    averages.setSummaryType("change", SummaryType.AVG);  
 //	    averages.setRenderer("change", new AggregationRenderer<BeanModel>() {  
 //	      public Object render(Number value, int colIndex, Grid<BeanModel> grid, ListStore<BeanModel> store) {  
 //	        // you can return html here  
 //	        return number.format(value.doubleValue());  
 //	      }  
 //	    });  
 
 	    cm.addAggregationRow(averages);  
 	      
 	    averages = new AggregationRowConfig<BeanModel>();  
 	    averages.setHtml("dollarValue", "Maximum");  
 	      
 	      
 	    averages.setSummaryType("dollarFraction", SummaryType.MAX);  
 	    averages.setSummaryFormat("dollarFraction", UiConstants.DOLLARS_FORMAT);  
 	  
 	    cm.addAggregationRow(averages);  
 	      
 	    averages = new AggregationRowConfig<BeanModel>();  
 	    averages.setHtml("dollarValue", "Total");  
 	      
 	    averages.setSummaryType("dollarFraction", SummaryType.SUM);  
 	    averages.setSummaryFormat("dollarFraction", UiConstants.DOLLARS_FORMAT);  
 	  
  
 	    cm.addAggregationRow(averages);  
 	      
 	    averages = new AggregationRowConfig<BeanModel>();  
 	    averages.setHtml("dollarValue", "Count");     
 	      
 	    averages.setSummaryType("dollarFraction", SummaryType.COUNT);  
 	    averages.setSummaryFormat("dollarFraction", UiConstants.INTEGER_FORMAT);  
  
 	    cm.addAggregationRow(averages);  
 	}
 	
 	protected void addHeaderGroups(ColumnModel cm) {
 		cm.addHeaderGroup(0, 2, new HeaderGroupConfig("Customer", 1, 2));
 		cm.addHeaderGroup(0, 4, new HeaderGroupConfig("Product", 1, 4));
 		cm.addHeaderGroup(0, 8, new HeaderGroupConfig("Term Dates", 1, 3));
 		cm.addHeaderGroup(0, 11, new HeaderGroupConfig("Dollars", 1, 4));
 	}
 	
 	protected ColumnConfig getDisplayColumn(String column, String heading, int size) {
 		return getGridColumn(column, heading, size, false, true, null, null);
 	}
 	
 	protected ColumnConfig getDisplayColumn(String column, String heading, int size, String toolTip) {
 		return getGridColumn(column, heading, size, false, true, null, null, toolTip);
 	}
 	
 	protected ColumnConfig getDisplayColumn(String column, String heading, int size, boolean sortable) {
 		return getGridColumn(column, heading, size, false, sortable, null, null);
 	}
 	
 	protected ColumnConfig getDisplayColumn(String column, String heading, int size, boolean sortable, String toolTip) {
 		return getGridColumn(column, heading, size, false, sortable, null, null, toolTip);
 	}
 	
 	protected ColumnConfig getDisplayColumn(String column, String heading, int size, boolean sortable, DateTimeFormat dateFormat) {
 		return getGridColumn(column, heading, size, false, sortable, dateFormat, null);
 	}
 	
 	protected ColumnConfig getDisplayColumn(String column, String heading, int size, boolean sortable, NumberFormat numberFormat) {
 		return getGridColumn(column, heading, size, false, sortable, null, numberFormat);
 	}
 	
 	protected ColumnConfig getDisplayColumn(String column, String heading, int size, boolean sortable, DateTimeFormat dateFormat, String toolTip) {
 		return getGridColumn(column, heading, size, false, sortable, dateFormat, null, toolTip);
 	}
 	
 	protected ColumnConfig getDisplayColumn(String column, String heading, int size, boolean sortable, NumberFormat numberFormat, String toolTip) {
 		return getGridColumn(column, heading, size, false, sortable, null, numberFormat, toolTip);
 	}
 	
 	protected ColumnConfig getHiddenColumn(String column, String heading, int size) {
 		return getGridColumn(column, heading, size, true, true, null, null);
 	}
 	
 	protected ColumnConfig getHiddenColumn(String column, String heading, int size, String toolTip) {
 		return getGridColumn(column, heading, size, true, true, null, null, toolTip);
 	}
 	
 	protected ColumnConfig getHiddenColumn(String column, String heading, int size, boolean sortable) {
 		return getGridColumn(column, heading, size, true, sortable, null, null);
 	}
 	
 	protected ColumnConfig getHiddenColumn(String column, String heading, int size, boolean sortable, String toolTip) {
 		return getGridColumn(column, heading, size, true, sortable, null, null, toolTip);
 	}
 	
 	protected ColumnConfig getHiddenColumn(String column, String heading, int size, boolean sortable, DateTimeFormat dateFormat) {
 		return getGridColumn(column, heading, size, true, sortable, dateFormat, null);
 	}
 	
 	protected ColumnConfig getHiddenColumn(String column, String heading, int size, boolean sortable, NumberFormat numberFormat) {
 		return getGridColumn(column, heading, size, true, sortable, null, numberFormat);
 	}
 	
 	protected ColumnConfig getHiddenColumn(String column, String heading, int size, boolean sortable, DateTimeFormat dateFormat, String toolTip) {
 		return getGridColumn(column, heading, size, true, sortable, dateFormat, null, toolTip);
 	}
 	
 	protected ColumnConfig getHiddenColumn(String column, String heading, int size, boolean sortable, NumberFormat numberFormat, String toolTip) {
 		return getGridColumn(column, heading, size, true, sortable, null, numberFormat, toolTip);
 	}
 	
 	protected ColumnConfig getGridColumn(String column, String heading, int size, boolean hidden, boolean sortable, DateTimeFormat dateFormat, NumberFormat numberFormat) {
 		return getGridColumn(column, heading, size, hidden, sortable, dateFormat, numberFormat, null);
 	}
 	
 	@SuppressWarnings("rawtypes")
 	protected ColumnConfig getGridColumn(String column, String heading, int size, boolean hidden, boolean sortable, DateTimeFormat dateFormat, NumberFormat numberFormat, String toolTip) {
 		SummaryColumnConfig cc = new SummaryColumnConfig(column,		heading, 		size);
 		cc.setId(column);
 		cc.setHidden(hidden);
 		cc.setSortable(sortable);
 		if (toolTip != null)
 			cc.setToolTip(toolTip);
 		if (dateFormat != null)
 			cc.setDateTimeFormat(dateFormat);
 		if (numberFormat != null) {
 			cc.setAlignment(HorizontalAlignment.RIGHT);
 			if (numberFormat.getPattern().equals("BWZ")) {
 				// Special implementation for blank when zero
 				cc.setRenderer(new GridCellRenderer<ModelData>() {  
 				  public Object render(ModelData model, String property, ColumnData config, int rowIndex, int colIndex,  
 				      ListStore<ModelData> store, Grid<ModelData> grid) {
 					  if (model.get(property).toString().equals("0"))
 						  return "";
 					  return model.get(property);
 				  }  
 				});
 			} else {
 				cc.setNumberFormat(numberFormat);
 			}
 		}
 		
 		if (gridFilters != null) {
 			if (dateFormat != null)
 				gridFilters.addFilter(new DateFilter(column));
 			else if (numberFormat != null)
 				gridFilters.addFilter(new NumericFilter(column));
 			else
 				gridFilters.addFilter(new StringFilter(column));
 		}
 		
 		return cc;
 	}
 	
 	protected SummaryColumnConfig<Double> getGridSummaryColumn(String column, String heading, int size, boolean hidden, boolean sortable, NumberFormat numberFormat, String toolTip) {
 		SummaryColumnConfig<Double> cc = new SummaryColumnConfig<Double>(column,		heading, 		size);
 		cc.setId(column);
 		cc.setHidden(hidden);
 		cc.setSortable(sortable);
 		cc.setSummaryType(SummaryType.SUM);
 		if (toolTip != null)
 			cc.setToolTip(toolTip);
 		if (numberFormat != null) {
 			cc.setAlignment(HorizontalAlignment.RIGHT);
 			if (numberFormat.getPattern().equals("BWZ")) {
 				// Special implementation for blank when zero
 				cc.setRenderer(new GridCellRenderer<ModelData>() {  
 				  public Object render(ModelData model, String property, ColumnData config, int rowIndex, int colIndex,  
 				      ListStore<ModelData> store, Grid<ModelData> grid) {
 					  if (model.get(property).toString().equals("0"))
 						  return "";
 					  return model.get(property);
 				  }  
 				});
 			} else {
 				cc.setNumberFormat(numberFormat);
 				cc.setSummaryFormat(numberFormat);
 			}
 		}
 		
 		if (gridFilters != null) {
 			if (numberFormat != null)
 				gridFilters.addFilter(new NumericFilter(column));
 			else
 				gridFilters.addFilter(new StringFilter(column));
 		}
 		
 		return cc;
 	}
 	
 	protected ColumnConfig getDisplayColumn(String name, String header, int width, String toolTip, ListStore<BeanModel> listStore) {
 		return getDisplayColumn(name, header, width, toolTip, listStore, "code", "name");
 	}
 	
 	protected ColumnConfig getDisplayColumn(String name, String header, int width, String toolTip, ListStore<BeanModel> listStore, String valueField, String displayField) {
 		
 		ColumnConfig column = getDisplayColumn(name, header, width, toolTip);
 		
 		if (gridFilters != null) {
 			BetterListFilter<BeanModel> listFilter = new BetterListFilter<BeanModel>(name, listStore);   
 			listFilter.setDisplayProperty(displayField); 
 			listFilter.setValueProperty(displayField);
 			gridFilters.addFilter(listFilter);
 		}
 	
 		return column; 
 	}
 	
 	public boolean areGridFiltersLocal() {
 		return true;
 	}
 	
 	public ListStore<BeanModel> getNewGridStore() {
 		termDataLoader = getTermDataLoader();
 		
 //		termDataLoader.setLimit(PAGE_LOAD_LIMIT);
 //		termDataLoader.setRemoteSort(true);
 		
 		termDataLoader.setRemoteSort(false);
 		termDataLoader.setSortField("institution.institutionName");
 		termDataLoader.setSortDir(SortDir.ASC);
 
 //		ListStore<BeanModel> gridStore = new ListStore<BeanModel>(termDataLoader);
 		GroupingStore<BeanModel> gridStore = new GroupingStore<BeanModel>(termDataLoader);
 	
 		gridStore.setStoreSorter(new ExtendedStoreSorter(OTHER_TERM_DATA_SORTS));
 //		gridStore.setDefaultSort("institution.institutionName", SortDir.ASC);
 		gridStore.setSortField("institution.institutionName");
 		gridStore.setSortDir(SortDir.ASC);
 		
 		gridStore.setGroupOnSort(true);
 		gridStore.groupBy("institution.institutionName");
 		
 		return gridStore;
 	}
 	
 	@Override
 	public ContentPanel getContentPanel() {
 		return contentPanel;
 	}
 	
 	@Override
 	public void setSnapshot(SnapshotInstance snapshot) {
 		super.setSnapshot(snapshot);
 		loadSnapshotParameters();
 		if (snapshot != null) {
 			if (snapshot.getSnapshotTaken() == null) {
 				takeSnapshot();
 			} else {
 				if (gridStore != null)
 					gridStore.getLoader().load();
 			}
 		}
 	}
 	
 	protected void takeSnapshot() {
 		if (snapshot.getSnapshotTaken() != null)
 			return;
 		
 		contentPanel.mask("Please wait while the snapshot is compiled.");
 		
 		takeSnapshotService.takeSnapshot(snapshot.getSnapshotId(),
 				new AsyncCallback<SnapshotInstance>() {
 					public void onFailure(Throwable caught) {
 						// Show the RPC error message to the user
 						if (caught instanceof IllegalArgumentException)
 							MessageBox.alert("Alert", caught.getMessage(), null);
 						else {
 							MessageBox.alert("Alert", "Take snapshot failed unexpectedly.", null);
 							System.out.println(caught.getClass().getName());
 							System.out.println(caught.getMessage());
 						}
 						contentPanel.unmask();
 					}
 
 					public void onSuccess(SnapshotInstance result) {
 						snapshot.setSnapshotTaken(result.getSnapshotTaken());
 						snapshot.setSnapshotRows(result.getSnapshotRows());
 						snapshot.setExcelFilename(result.getExcelFilename());
 						if (parentCardPanel != null) {
 							parentCardPanel.reflectSnapshotChanges(snapshot);
 						}
 						contentPanel.unmask();
 						if (gridStore != null)
 							gridStore.getLoader().load();
 					}
 				});
 	}
 	
 	/**
 	 * Construct and return a loader to handle returning a list of institutions.
 	 * @return
 	 */
 	protected PagingLoader<PagingLoadResult<SnapshotTermDataInstance>> getTermDataLoader() {
 		// proxy and reader  
 		RpcProxy<PagingLoadResult<SnapshotTermDataInstance>> proxy = new RpcProxy<PagingLoadResult<SnapshotTermDataInstance>>() {  
 			@Override  
 			public void load(Object loadConfig, final AsyncCallback<PagingLoadResult<SnapshotTermDataInstance>> callback) {
 		    	
 				// This could be as simple as calling userListService.getUsers and passing the callback
 				// Instead, here the callback is overridden so that it can catch errors and alert the users.  Then the original callback is told of the failure.
 				// On success, the original callback is just passed the onSuccess message, and the response (the list).
 				
 				AsyncCallback<SynchronizedPagingLoadResult<SnapshotTermDataInstance>> myCallback = new AsyncCallback<SynchronizedPagingLoadResult<SnapshotTermDataInstance>>() {
 					public void onFailure(Throwable caught) {
 						// Show the RPC error message to the user
 						if (caught instanceof IllegalArgumentException)
 							MessageBox.alert("Alert", caught.getMessage(), null);
 						else if (caught instanceof ServiceNotReadyException)
 								MessageBox.alert("Alert", "The " + caught.getMessage() + " is not available at this time.  Please try again in a few minutes.", null);
 						else {
 							System.out.println(caught.getClass().getName());
 							System.out.println(caught.getMessage());
 							MessageBox.alert("Alert", "Snapshot data load failed unexpectedly.", null);
 						}
 						callback.onFailure(caught);
 					}
 
 					public void onSuccess(SynchronizedPagingLoadResult<SnapshotTermDataInstance> syncResult) {
 						if (syncResult.getSyncId() != searchSyncId)
 							return;
 						
 						PagingLoadResult<SnapshotTermDataInstance> result = syncResult.getResult();
 
 						callback.onSuccess(result);
 
 						grid.unmask();
 					}
 				};
 				
 				if (grid.isMasked()) grid.unmask();	//	To put our own mask message up
 				if (!grid.isMasked()) grid.mask("Loading snapshot data... Please wait...");		// Required because GXT forgets to do this when a remote sort is initiated through the columns
 				searchSyncId = System.currentTimeMillis();
 				invokeSnapshotTermDataListService((PagingLoadConfig) loadConfig, snapshot.getSnapshotId(), searchSyncId, myCallback);
 		    }  
 		};
 		BeanModelReader reader = new BeanModelReader();
 		
 		// loader and store  
 		BasePagingLoader<PagingLoadResult<SnapshotTermDataInstance>> loader = new BasePagingLoader<PagingLoadResult<SnapshotTermDataInstance>>(proxy, reader) {
 			@Override
 			  protected Object newLoadConfig() {
 				return new BaseFilterPagingLoadConfig();
 			}
 		};
 		loader.setReuseLoadConfig(false);
 		return loader;
 	}
 	
 	public void invokeSnapshotTermDataListService(PagingLoadConfig loadConfig, int snapshotId, long searchSyncId, AsyncCallback<SynchronizedPagingLoadResult<SnapshotTermDataInstance>> myCallback) {
 		snapshotTermDataListService.getSnapshotTermData((PagingLoadConfig) loadConfig, snapshotId, searchSyncId, myCallback);
 	}
 
 	protected void loadSnapshotParameters() {
 		setSnapshotParameterSet(null);
 		snapshotParameterSetGetService.getSnapshotParameterSet(snapshot.getSnapshotId(), null,	//	null for all sources
 				new AsyncCallback<SnapshotParameterSetInstance>() {
 					public void onFailure(Throwable caught) {
 						// Show the RPC error message to the user
 						if (caught instanceof IllegalArgumentException)
 							MessageBox.alert("Alert", caught.getMessage(), null);
 						else {
 							MessageBox.alert("Alert", "Snapshot parameter load failed unexpectedly.", null);
 							System.out.println(caught.getClass().getName());
 							System.out.println(caught.getMessage());
 						}
 					}
 
 					public void onSuccess(SnapshotParameterSetInstance snapshotParameterSet) {
 						setSnapshotParameterSet(snapshotParameterSet);
 					}
 			});
 	}
 	
 	protected void setSnapshotParameterSet(SnapshotParameterSetInstance snapshotParameterSet) {
 		this.snapshotParameterSet = snapshotParameterSet;
 		formatToolTip();
 	}
 	
 	/**
 	 * Format the contents of the tooltip.
 	 * 
 	 * Note that this function must wait for the states to load (if they are needed) before continuing.  This is done with a timer.
 	 */
 	protected void formatToolTip() {
 		if (snapshotParameterSet != null && snapshotParameterSet.getValues().containsKey(SnapshotParameterNames.INSTITUTION_STATE) && !UiConstants.areInstitutionStatesLoaded()) {
 			UiConstants.loadInstitutionStates();
 			Timer timer = new Timer() {
 				@Override
 				public void run() {
 					if (UiConstants.areInstitutionStatesLoaded()) {
 						this.cancel();
 						formatToolTipNoWait();
 					}
 				}
 			};
 			timer.scheduleRepeating(200);
 		} else {
 			formatToolTipNoWait();
 		}
 	}
 		
 	protected void formatToolTipNoWait() {
 		StringBuffer sb = new StringBuffer();
 		if (snapshot == null) {
 			sb.append("No snapshot.");
 			sb.append("<br />");
 			sb.append("<br />");
 		} else {
 			sb.append("Snapshot <span class=\"sbam-report-title\">#");
 			sb.append(snapshot.getSnapshotId());
 			sb.append(" : ");
 			sb.append(snapshot.getSnapshotName());
 			sb.append("</span>");
 			sb.append("<br />");
 			sb.append("<br />");
 		}
 		if (snapshotParameterSet == null) {
 			sb.append("No parameters.");
 		} else {
 			sb.append("<table class=\"sbam-report-parms\">");
 			for (String parameterName : snapshotParameterSet.getValues().keySet()) {
 				List<SnapshotParameterValueObject> values = snapshotParameterSet.getValues(parameterName);
 				if (values != null && values.size() > 0) {
 					sb.append("<tr><td class=\"sbam-report-parm\">");
 					sb.append(SnapshotParameterNames.getLabel(parameterName));
 					sb.append("</td><td class=\"sbam-report-value\">");
 					int count = 0;
 					for (SnapshotParameterValueObject value : values) {
 						if (count > 0)
 							sb.append(", ");
 						sb.append(getTranslatedValue(parameterName, value.toString()));
 						count++;
 					}
 				}
 				sb.append("</td></tr>");
 			}
 			sb.append("</table>");
 		}
 		getContentPanel().getHeader().setToolTip(sb.toString());
 		getContentPanel().getHeader().getToolTip().getToolTipConfig().setCloseable(true);
 	}
 	
 	protected String getTranslatedValue(String name, String value) {
 		if (name.equals(SnapshotParameterNames.UCN_TYPE)) {
 			return snapshot.getUcnTypeDescription();
 		} else if (name.equals(SnapshotParameterNames.PRODUCT_SERVICE_TYPE)) {
 			return snapshot.getProductServiceDescription();
 		} else if (name.equals(SnapshotParameterNames.TERM_TYPES)) {
 			return getCodeDescription(value, UiConstants.getTermTypes());
 		} else if (name.equals(SnapshotParameterNames.INSTITUTION_STATE)) {
 			return getCodeDescription(value, UiConstants.getInstitutionStates());
 		} else if (name.equals(SnapshotParameterNames.PROD_COMM_CODES)
 				|| name.equals(SnapshotParameterNames.AGREEMENT_COMM_CODES)
 				|| name.equals(SnapshotParameterNames.TERM_COMM_CODES)) {
 			return getCodeDescription(value, UiConstants.getCommissionTypes());
 		}
 		return value;
 	}
 	
 	protected String getCodeDescription(String value, ListStore<BeanModel> store) {
 		BeanModel model = store.findModel(value);
 		if (model != null && model.get("description") != null)
 			return model.get("description").toString();
 		return value;
 	}
 	
 	@Override
 	public String getPanelTitle() {
 		return "Snapshot Data View";
 	}
 	
 	@Override
 	public boolean okayToReturn() {
 		if (gridStore != null) {
 			gridStore.removeAll();
 		}
 		return true;
 	}
 	
 }
