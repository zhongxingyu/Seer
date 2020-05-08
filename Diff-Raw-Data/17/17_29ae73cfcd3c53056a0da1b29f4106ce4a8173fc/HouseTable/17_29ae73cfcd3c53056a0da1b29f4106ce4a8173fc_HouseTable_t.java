 package cpsc310.client;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.google.gwt.cell.client.NumberCell;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.i18n.client.NumberFormat;
 import com.google.gwt.user.cellview.client.CellTable;
 import com.google.gwt.user.cellview.client.Column;
 import com.google.gwt.user.cellview.client.ColumnSortEvent;
 import com.google.gwt.user.cellview.client.ColumnSortEvent.AsyncHandler;
 import com.google.gwt.user.cellview.client.TextColumn;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.view.client.AsyncDataProvider;
 import com.google.gwt.view.client.HasData;
 import com.google.gwt.view.client.MultiSelectionModel;
 
 
 /**
  * Singleton class which encapsulates the CellTable on main UI.
  */
 public class HouseTable {
 	
 	private static int defaultPageSize = 8;
 	private static HouseTable houseTable = null;
 	private CellTable<HouseData> homesCellTable = null;
 	private CellTable.Resources resource = GWT.create(CellTableResources.class);
 	private HouseDataServiceAsync houseDataSvc = GWT.create(HouseDataService.class);
 	private AsyncDataProvider<HouseData> dataProvider;
 	private int currentStartItem = 0;
 	private int pageLength = 0;
 	private int databaseLength = 0;
 	private boolean isSearching = false;
 	private ArrayList<String> columnName = new ArrayList<String>();
 	
 	/**
 	 * Singleton constructor. Create one CellTable which contains HouseData. 
 	 */
 	private HouseTable() {
 		homesCellTable = createCellTable();
 	}
 	
 	/**
 	 * Create only one instance for HouseTable
 	 * 
 	 * @return singleton HouseTable instance
 	 */
 	public static HouseTable createHouseTable() {
 		if (houseTable == null) {
 			houseTable = new HouseTable();
 		}
 		return houseTable;
 	}
 	
 	/**
 	 * Build CellTable<HouseData>, assemble sorting, and populate table with HouseData
 	 * fetched from server.
 	 * 
 	 * @return populated CellTable<HouseData> with sorting ability
 	 */
 	private CellTable<HouseData> createCellTable() {
 		homesCellTable = new CellTable<HouseData>(defaultPageSize, resource, HouseData.KEY_PROVIDER);	  	
 		addColumns();
 		createSort();
 		populateTable();
 		return homesCellTable;
 	}
 	
 	/**
 	 * Helper to createCellTable(). Adds columns to the table.
 	 */
 	private void addColumns() {	  	
 		NumberFormat yearFormat = NumberFormat.getFormat("0000");
 		
 	  	// Address column
 	  	TextColumn<HouseData> addrColumn = new TextColumn<HouseData>() {
 	  		@Override
 	  		public String getValue(HouseData house) {
 	  			return house.getAddress();
 	  		}
 	  	};
 	  	homesCellTable.addColumn(addrColumn, "Address");		
 	  	addrColumn.setSortable(true);
 	  	columnName.add("Address");
 	  	
 	  	// Postal code column
 	  	TextColumn<HouseData> postalColumn = new TextColumn<HouseData>() {
 			@Override
 			public String getValue(HouseData house) {
 				return house.getPostalCode();
 			}
 		};
 		homesCellTable.addColumn(postalColumn, "Postal Code");		
 	  	postalColumn.setSortable(true);
 	  	columnName.add("Postal Code");
 	  		  	
 	  	// Current Land Value column
 	  	Column<HouseData, Number> currlandValColumn = 
 	  			new Column<HouseData, Number>(new NumberCell()) {
 	  		@Override
 	  		public Number getValue(HouseData house) {
 	  			return house.getCurrentLandValue();
 	  		}
 	  	};
 	  	homesCellTable.addColumn(currlandValColumn, "Current Land Value");				
 	  	currlandValColumn.setSortable(true);
 	  	columnName.add("Current Land Value");
 	  	
 	  	// Current Improvement Value column
 	  	Column<HouseData, Number> currImprovValColumn = 
 	  			new Column<HouseData, Number>(new NumberCell()) {
 	  		@Override
 	  		public Number getValue(HouseData house) {
 	  			return house.getCurrentImprovementValue();
 	  		}
 	  	};
 	  	homesCellTable.addColumn(currImprovValColumn, "Current Improvement Value");				
 	  	currImprovValColumn.setSortable(true);
 	  	columnName.add("Current Improvement Value");
 	  	
 	  	
 	  	// Assessment Year column
 	  	Column<HouseData, Number> assYearColumn = 
 	  			new Column<HouseData, Number>(new NumberCell(yearFormat)) {
 	  		@Override
 	  		public Number getValue(HouseData house) {
 	  			return house.getAssessmentYear();
 	  		}
 	  	};
 	  	homesCellTable.addColumn(assYearColumn, "Assessment Year");				
 	  	assYearColumn.setSortable(true);
 	  	columnName.add("Assessment Year");
 	  	
 	  	// Previous Land Value column
 	  	Column<HouseData, Number> prevlandValColumn = 
 	  			new Column<HouseData, Number>(new NumberCell()) {
 	  		@Override
 	  		public Number getValue(HouseData house) {
 	  			return house.getPreviousLandValue();
 	  		}
 	  	};
 	  	homesCellTable.addColumn(prevlandValColumn, "Previous Land Value");				
 	  	prevlandValColumn.setSortable(true);
 	  	columnName.add("Previous Land Value");
 	  	
 	  	// Previous Improvement Value column
 	  	Column<HouseData, Number> prevImprovValColumn = 
 	  			new Column<HouseData, Number>(new NumberCell()) {
 	  		@Override
 	  		public Number getValue(HouseData house) {
 	  			return house.getPreviousImprovementValue();
 	  		}
 	  	};
 	  	homesCellTable.addColumn(prevImprovValColumn, "Previous Improvement Value");				
 	  	prevImprovValColumn.setSortable(true);  	  	
 	  	columnName.add("Previous Improvement Value");
 	  	
 	  	// Built Year column
 	  	Column<HouseData, Number> yrBuiltColumn = 
 	  			new Column<HouseData, Number>(new NumberCell(yearFormat)) {
 	  		@Override
 	  		public Number getValue(HouseData house) {
 	  			return house.getYearBuilt();
 	  		}
 	  	};
 	  	homesCellTable.addColumn(yrBuiltColumn, "Year Built");				
 	  	yrBuiltColumn.setSortable(true);
 	  	columnName.add("Year Built");
 	  	
 	  	// Big Improvement Year column
 	  	Column<HouseData, Number> improvYearColumn = 
 	  			new Column<HouseData, Number>(new NumberCell(yearFormat)) {
 	  		@Override
 	  		public Number getValue(HouseData house) {
 	  			return house.getBigImprovementYear();
 	  		}
 	  	};
 	  	homesCellTable.addColumn(improvYearColumn, "Big Improvement Year");				
 	  	improvYearColumn.setSortable(true);
 	  	columnName.add("Big Improvement Year");
 	  	
 	  	
 	  	/* User specified column begins.
 	  	 * User specified columns are class variables because enableEdit() needs to
 	  	 * replace following columns with editable cells.
 	  	 */
 	  	// Realtor column 
 	  	Column<HouseData, String> ownerColumn = new TextColumn<HouseData>() {
 	  		@Override
 	  		public String getValue(HouseData house) {
 	  			return house.getOwner();
 	  		}
 	  	};
 	  	homesCellTable.addColumn(ownerColumn, "Realtor");
 	  	ownerColumn.setSortable(true);
 	  	columnName.add("Realtor");
 	  	
 	  	// Price column
	  	Column<HouseData, Number> priceColumn = 
	  			new Column<HouseData, Number>(new NumberCell()) {
 	  		@Override
	  		public Number getValue(HouseData house) {
	  			return house.getPrice();
 	  		}
 	  	};
 	  	homesCellTable.addColumn(priceColumn, "Price");
 	  	priceColumn.setSortable(true);
 	  	columnName.add("Price");
 	  	
 	  	// For Sale column
 	  	Column<HouseData, String> isSellingColumn = new TextColumn<HouseData>() {
 	  		@Override
 	  		public String getValue(HouseData house) {
 	  			if (house.getIsSelling())
 	  				return "For Sale";
 	  			return "";
 	  		}
 	  	};
 	  	homesCellTable.addColumn(isSellingColumn, "Sale");
 	  	isSellingColumn.setSortable(true);
 	  	columnName.add("Sale");
 	  	  	
 	}
 	
 	/**
 	 * Helper to createTable(). Populate table with data fetched by rpc call to the server.
 	 */
 	private void populateTable() {
 		// Initialize the service proxy
 		if (houseDataSvc == null) {
 			houseDataSvc = GWT.create(HouseDataService.class);
 		}		
 		
 		updateRowCount();
 		
 		// Data provider to populate Table
 		dataProvider = new AsyncDataProvider<HouseData>() {
 			@Override
 			protected void onRangeChanged(HasData<HouseData> display) {
 				currentStartItem = display.getVisibleRange().getStart();
 				int range = display.getVisibleRange().getLength();
 				pageLength = range;
 								
 				AsyncCallback<List<HouseData>> callback = new AsyncCallback<List<HouseData>> () {
 					@Override
 					public void onFailure (Throwable caught) {
 						Window.alert(caught.getMessage());	
 					}
 					@Override
 					public void onSuccess (List<HouseData> result) {
 						updateRowData(currentStartItem, result);
 					}
 				};
 				houseDataSvc.getHouses(currentStartItem, range, callback);									
 			}
 		};
 		dataProvider.addDataDisplay(homesCellTable);		
 		
 	}
 		
 	
 	/**
 	 * Update the number of rows to the length of the current working set in
 	 * in the server. Current working set refers to either search results,
 	 * or the entire database.
 	 */
 	private void updateRowCount() {
 		AsyncCallback<Integer> callback = new AsyncCallback<Integer> () {
 			@Override
 			public void onFailure (Throwable caught) {
 				Window.alert(caught.getMessage());	
 			}
 			@Override
 			public void onSuccess (Integer result) {
 				dataProvider.updateRowCount(result, true);
 				databaseLength = result;
 			}
 		};
 		houseDataSvc.getHouseDatabaseLength(callback);
 	}
 
 	/**
 	 * Helper to createTable(). Creates ColumnSortEvent.Handler for homesCellTable,
 	 * attaches comparators to enable sorting, and attaches the hander to the table.
 	 */
 	private void createSort() {
 		/* local sort
 		 // Create sort handler, associate sort handler to the table
 		homesCellTable.addColumnSortHandler( new ColumnSortEvent.Handler() {
 			public void onColumnSort(ColumnSortEvent event) {
 				@SuppressWarnings("unchecked")
 				Column<HouseData,?> sortedColumn = (Column<HouseData, ?>) event.getColumn();
 				int sortedIndex = homesCellTable.getColumnIndex(sortedColumn);
 				List<HouseData> newData = new ArrayList<HouseData>(homesCellTable.getVisibleItems());
 				
 				Comparator<HouseData> c = HouseData.HouseAddrComparator;
 				switch(sortedIndex) {
 				case 0:
 					c = HouseData.HouseAddrComparator;
 					break;
 				case 1:
 					c = HouseData.HousePostalCodeComparator;
 					break;
 				case 2:
 					c = HouseData.HouseCurrentLandValueComparator;
 					break;
 				case 3:
 					c = HouseData.HouseCurrentImprovementValueComparator;
 					break;
 				case 4:
 					c = HouseData.HouseAssYearComparator;
 					break;
 				case 5:
 					c = HouseData.HousePrevLandValueComparator;
 					break;				
 				case 6:
 					c = HouseData.HousePrevImprovementValueComparator;
 					break;
 				case 7:
 					c = HouseData.HouseYearBuiltComparator;
 					break;
 				case 8:
 					c = HouseData.HouseBigImprYearComparator;
 					break;
 				case 9:
 					c = HouseData.HouseOwnerComparator;
 					break;
 				case 10:
 					c = HouseData.HousePriceComparator;
 					break;
 				case 11:
 					c = HouseData.HouseIsSellingComparator;
 					break;					
 				default:
 					break;
 				}
 				if (event.isSortAscending()) {
 					Collections.sort(newData, c);
 				}
 				else {
 					Collections.sort(newData, Collections.reverseOrder(c));
 				}
 				homesCellTable.setRowData(homesCellTable.getPageStart(), newData);
 			}
 		});
 		// Get the ColumnSortInfo from the table.
 		        		
 		*/
 		// Server-side sort
 		AsyncHandler columnSortHandler = new ColumnSortEvent.AsyncHandler(homesCellTable) {
 			public void onColumnSort(ColumnSortEvent event) {
 				
 				@SuppressWarnings("unchecked")
 				Column<HouseData,?> sortedColumn = (Column<HouseData, ?>) event.getColumn();				
 				int columnIndex = homesCellTable.getColumnIndex(sortedColumn);
 				boolean isSortAscending = event.isSortAscending();
 				
 				
 				AsyncCallback<Void> callback = new AsyncCallback<Void> () {
 					@Override
 					public void onFailure (Throwable caught) {
 						Window.alert(caught.getMessage());	
 					}
 					@Override
 					public void onSuccess (Void result) {
 						refreshTableCurrentView();
 					}
 				};
 				switch(columnIndex) {
 					case 0:
 						houseDataSvc.sortByAddress(isSortAscending, callback);
 						break;
 					case 1:
 						houseDataSvc.sortByPostalCode(isSortAscending, callback);
 						break;
 					case 2:
 						houseDataSvc.sortByCurrentLandValue(isSortAscending, callback);
 						break;
 					case 3:
 						houseDataSvc.sortByCurrentImprovementValue(isSortAscending, callback);
 						break;
 					case 4:
 						houseDataSvc.sortByAssessmentYear(isSortAscending, callback);
 						break;
 					case 5:
 						houseDataSvc.sortByPreviousLandValue(isSortAscending, callback);
 						break;				
 					case 6:
 						houseDataSvc.sortByPreviousImprovementValue(isSortAscending, callback);
 						break;
 					case 7:
 						houseDataSvc.sortByYearBuilt(isSortAscending, callback);
 						break;
 					case 8:
 						houseDataSvc.sortByBigImprovementYear(isSortAscending, callback);
 						break;
 					case 9:
 						houseDataSvc.sortByOwner(isSortAscending, callback);
 						break;
 					case 10:
 						houseDataSvc.sortByPrice(isSortAscending, callback);
 						break;
 					case 11:
 						houseDataSvc.sortByForSale(isSortAscending, callback);
 						break;					
 					default:
 						break;
 					}				 
 			}
 		};
 		homesCellTable.addColumnSortHandler(columnSortHandler);		
 	}
 	
 	// Getter begins
 	/**
 	 * Getter for CellTable.
 	 * @return CellTable<HouseData>
 	 */
 	public CellTable<HouseData> getHouseTable() {
 		return this.homesCellTable;
 	}
 	
 	/**
 	 * Getter for page length
 	 * @return length of a cell table page
 	 */
 	public int getPageLength() {
 		return this.pageLength;
 	}
 	
 	/**
 	 * Getter for start item
 	 * @return index of current start item in the table
 	 */
 	public int getCurrentStartItem() {
 		return this.currentStartItem;
 	}
 	
 	/**
 	 * Attach selection model to homesCellTable
 	 * @param model MultiSelectionModel
 	 */
 	public void enableSelection (MultiSelectionModel<HouseData> model) {
 		// Associate the selection model with the table
 		homesCellTable.setSelectionModel(model);
 	}
 	
 	/**
 	 * Refreshes the table from the beginning.
 	 * This is expected to be called by search methods to update table
 	 * with the search results.
 	 */
 	public void refreshTableFromBeginning() {
 		// Refresh the count of rows (Because now it's showing search results)
 		updateRowCount();
 		
 		// Fetch new data from the server
 		AsyncCallback<List<HouseData>> callback = new AsyncCallback<List<HouseData>> () {
 			@Override
 			public void onFailure (Throwable caught) {
 				Window.alert(caught.getMessage());
 			}
 			@Override
 			public void onSuccess (List<HouseData> result) {
 				dataProvider.updateRowData(0, result);
 			}
 		};
 		houseDataSvc.getHouses(0, pageLength, callback);
 	}
 	
 	
 	/**
 	 * Refresh current view of table, fetching new data from server by
 	 * asynchronous call.
 	 * This is expected to be called by any methods that want to
 	 * refresh the view of table after edits to the database.
 	 */
 	public void refreshTableCurrentView() {
 		AsyncCallback<List<HouseData>> callback = new AsyncCallback<List<HouseData>> () {
 			@Override
 			public void onFailure (Throwable caught) {
 				Window.alert(caught.getMessage());
 			}
 			@Override
 			public void onSuccess (List<HouseData> result) {
 				dataProvider.updateRowData(currentStartItem, result);
 			}
 		};
 		houseDataSvc.getHouses(currentStartItem, pageLength, callback);
 	}
 		
 	/**
 	 * Expand or shrink the page size of the table.
 	 * @param pageSize - size of the page to which the table will expand
 	 * -1 indicates default page size.
 	 */
 	public void expandShrinkTable(int pageSize) {
 		if (pageSize == -1)
 			this.homesCellTable.setPageSize(defaultPageSize);
 		else
 			this.homesCellTable.setPageSize(pageSize);
 		this.refreshTableCurrentView();
 	}
 	
 }
