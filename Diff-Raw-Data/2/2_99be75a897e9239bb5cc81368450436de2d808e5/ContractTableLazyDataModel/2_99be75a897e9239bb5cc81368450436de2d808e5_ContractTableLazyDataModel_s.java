 package presentationLayer.lazyModel;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.primefaces.component.datatable.DataTable;
 import org.primefaces.model.LazyDataModel;
 import org.primefaces.model.SortOrder;
 
 import businessLayer.Contract;
 import daoLayer.ResultPagerBean;
 
 public abstract class ContractTableLazyDataModel extends LazyDataModel<Contract> {
 	// NB: i filtri di PF lavorano con chiavi di tipo stringa. Queste stesse
 	// chiavi vengono utilizzate anche nel passaggio dei parametri via URL. Per
 	// un corretto funzionamento della tabella, le chiavi relative ad uno stesso
 	// parametro devono coincidere (i.e., chiave filtro = chiave parametro URL).
 	// Questa uguaglianza viene mantenuta "a mano" (quindi bisogna prestare
 	// attenzione se si modificano le chiavi, perché bisogna cambiarle in più
 	// punti, anche nel relativo xhtml).
 
 	private static final long serialVersionUID = 7443717427722158551L;
 
 	protected DataTable dataTable;
 
 	protected List<Contract> displayedContracts;
 	protected int pageFirst, pageRows, totalRows;
 	protected Contract selectedValue;
 
 	protected boolean ignoreUiTableFilters;
 
 
 	public ContractTableLazyDataModel() {
 		this.ignoreUiTableFilters = false;
 	}
 
 
 	@Override
 	public final Contract getRowData(String rowKey) {
 		int key = Integer.parseInt(rowKey);
 		Contract current = null;
 		boolean found = false;
 		Iterator<Contract> it = displayedContracts.iterator();
 		while (false == found && it.hasNext()) {
 			current = it.next();
 			if (current.getId() == key) {
 				found = true;
 			}
 		}
 		if (false == found) {
 			throw new IllegalStateException("Cannot retrieve agreement from a displayed row");
 		}
 		return current;
 	}
 
 
 	@Override
 	public final Object getRowKey(Contract contract) {
 		return contract.getId();
 	}
 
 
 	@Override
 	public final List<Contract> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, String> filters) {
 		this.pageFirst = first;
 		this.pageRows = pageSize;
 		/* TODO Blocco stampe. Eliminare */
 		System.out.println("-----------------");
 		System.out.print("Filters: { ");
 		for (Iterator<String> it = filters.keySet().iterator(); it.hasNext();) {
 			String key = it.next();
 			String value = filters.get(key);
 			System.out.print("[" + key + ", " + value + "]   ");
 		}
 		System.out.println("}");
 		/* Fine blocco stampe. Ce ne sono altre, però! */
 		updateFields(sortField, sortOrder, filters);
 		displayedContracts = getData(filters);
 
 		// rowCount
 		// this.setRowCount(displayedContracts.size());
 		this.setRowCount(totalRows);
 		System.out.println("getRowCount = " + this.getRowCount());
 		System.out.println("First: " + first + "; pageFirst: " + pageFirst + "|| page size: " + pageSize + "; pageRows: " + pageRows);
 
 		// System.out.println("First: " + dataTable.getFirst() + "; page: " +
 		// dataTable.getPage() + "; page count: " + dataTable.getPageCount()
 		// + "; rows to render: " + dataTable.getRowsToRender() + "; rows: " +
 		// dataTable.getRows());
 
 		// RequestContext requestContext = RequestContext.getCurrentInstance();
 		// if (requestContext != null) {
 		// System.out.println("Param total records: " +
 		// requestContext.getCallbackParams().get("totalRecords"));
 		// requestContext.addCallbackParam("totalRecords",
 		// lazyModel.getRowCount());
 		// }
 
 		ignoreUiTableFilters = false;
 		System.out.println("--------------------------------------------");
 		return displayedContracts;
 	}
 
 
 	protected List<Contract> getData(Map<String, String> filters) {
 		initPager(filters);
 		getPager().setPageSize(pageRows);
 
 		// int currentPage = (pageSize != 0) ? first / pageSize : 0;
 		int currentPage = pageFirst / pageRows;
 		getPager().setCurrentPage(currentPage);
 
 		List<Contract> result = getPager().getCurrentResults();
 		getPager().next();
 		result.addAll(getPager().getCurrentResults());
 
		this.totalRows = result.size() + pageFirst;
 		// chiederlo alla query
 
 		return result;
 
 		// initPager(filters);
 		// getPager().setPageSize(pageRows);
 		//
 		// // int currentPage = (pageSize != 0) ? first / pageSize : 0;
 		// List<Contract> result = new ArrayList<>();
 		// int currentPage = pageFirst / pageRows;
 		// int pageToFetch = 2;
 		//
 		// if (currentPage > 0) {
 		// pageToFetch++;
 		// currentPage--;
 		// }
 		//
 		// ResultPagerBean<Contract> pager = getPager();
 		// for (int i = 0; i < pageToFetch; i++) {
 		// pager.setCurrentPage(currentPage + i);
 		// result.addAll(pager.getCurrentResults());
 		// }
 		//
 		// System.out.print("{ ");
 		// for(Contract c : result){
 		// System.out.print(c.getId() + " ");
 		// }
 		// System.out.println("} [" + result.get(pageFirst).getId() + "]");
 		//
 		// return result;
 	}
 
 
 	protected abstract void updateFields(String sortField, SortOrder sortOrder, Map<String, String> filters);
 
 
 	protected abstract void initPager(Map<String, String> filters);
 
 
 	public Contract getSelectedValue() {
 		return selectedValue;
 	}
 
 
 	public void setSelectedValue(Contract selectedValue) {
 		this.selectedValue = selectedValue;
 	}
 
 
 	public int getPageFirst() {
 		return pageFirst;
 	}
 
 
 	public void setPageFirst(int pageFirst) {
 		this.pageFirst = pageFirst;
 	}
 
 
 	public int getPageRows() {
 		return pageRows;
 	}
 
 
 	public void setPageRows(int pageRows) {
 		this.pageRows = pageRows;
 	}
 
 
 	public int getTotalRows() {
 		return totalRows;
 	}
 
 
 	public void setTotalRows(int totalRows) {
 		this.totalRows = totalRows;
 	}
 
 
 	public DataTable getDataTable() {
 		return dataTable;
 	}
 
 
 	public void setDataTable(DataTable dataTable) {
 		this.dataTable = dataTable;
 	}
 
 
 	public void filterOnReload() {
 		System.out.println("Filtering on reload");
 		ignoreUiTableFilters = true;
 	}
 
 
 	public void loadTableFilters() {
 		if (ignoreUiTableFilters) {
 			Map<String, String> filters = buildFilterList().filters;
 			dataTable.setFilters(filters);
 			// in teoria, in questo modo vengono passati più filtri di quelli
 			// realmente esistenti.
 			if (filters.containsKey("sortorder")) {
 				// TODO forse servirebbe una condizione migliore
 				dataTable.setSortOrder(filters.get("sortorder"));
 			}
 
 			if (filters.containsKey("first")) {
 				try {
 					dataTable.setFirst(Integer.parseInt(filters.get("first")));
 				} catch (NumberFormatException e) {}
 			}
 
 			if (filters.containsKey("rows")) {
 				try {
 					int value = Integer.parseInt(filters.get("rows"));
 					dataTable.setRows((value > 0) ? value : 10);
 				} catch (NumberFormatException e) {}
 			}
 		}
 	}
 
 
 	public final String getFiltersAsParameterList() {
 		return buildFilterList().asParameterList();
 	}
 
 
 	private FilterList buildFilterList() {
 		FilterList l = initFilterList();
 		l.put("first", String.valueOf(pageFirst));
 		l.put("rows", String.valueOf(pageRows));
 		l.put("totalRows", String.valueOf(totalRows));
 		return l;
 	}
 
 
 	protected abstract FilterList initFilterList();
 
 
 	public void closePager() {
 		getPager().finished();
 	}
 
 
 	protected abstract ResultPagerBean<Contract> getPager();
 
 
 
 	public static final class FilterList {
 		private Map<String, String> filters;
 
 
 		protected FilterList() {
 			super();
 			this.filters = new HashMap<>();
 		}
 
 
 		protected void put(String key, String value) {
 			filters.put(key, value);
 		}
 
 
 		public String get(String key) {
 			return filters.get(key);
 		}
 
 
 		public String asParameterList() {
 			String result = "";
 			for (Iterator<String> it = filters.keySet().iterator(); it.hasNext();) {
 				String s = it.next();
 				result += s + "=" + filters.get(s);
 				if (it.hasNext()) {
 					result += "&";
 				}
 			}
 			return result;
 		}
 	}
 
 }
