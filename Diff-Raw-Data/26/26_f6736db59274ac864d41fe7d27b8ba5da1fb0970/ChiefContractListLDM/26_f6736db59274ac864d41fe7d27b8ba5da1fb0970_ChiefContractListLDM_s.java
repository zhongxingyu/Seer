 package presentationLayer.lazyModel;
 
 import java.util.Date;
 import java.util.Map;
 
 import javax.ejb.EJB;
 import javax.enterprise.context.Dependent;
 
 import org.primefaces.model.SortOrder;
 
 import businessLayer.Agreement;
 import businessLayer.Contract;
 import daoLayer.ContractSearchService;
 import daoLayer.ResultPagerBean;
 
 @Dependent
 public class ChiefContractListLDM extends ContractTableLazyDataModel {
 	private static final long serialVersionUID = 6458813690566381587L;
 
 	@EJB
 	private ContractSearchService contractSearch;
 
 	protected Date filterContractMinDate, filterContractMaxDate, filterInstMinDate, filterInstMaxDate;
 	protected Integer filterCompanyId;
 	protected Boolean filterActiveContract;
 
 
 	public ChiefContractListLDM() {
 		super();
 		this.filterContractMinDate = null;
 		this.filterContractMaxDate = null;
 		this.filterInstMinDate = null;
 		this.filterInstMaxDate = null;
 		this.filterCompanyId = null;
 		this.filterActiveContract = null;
 	}
 
 
 	public Boolean getFilterActiveContract() {
 		return filterActiveContract;
 	}
 
 
 	public void setFilterActiveContract(Boolean filterActiveContract) {
 		this.filterActiveContract = filterActiveContract;
 	}
 
 
 	public Date getFilterContractMinDate() {
 		return filterContractMinDate;
 	}
 
 
 	public void setFilterContractMinDate(Date filterContractMinDate) {
 		this.filterContractMinDate = filterContractMinDate;
 	}
 
 
 	public Date getFilterContractMaxDate() {
 		return filterContractMaxDate;
 	}
 
 
 	public void setFilterContractMaxDate(Date filterContractMaxDate) {
 		this.filterContractMaxDate = filterContractMaxDate;
 	}
 
 
 	public Date getFilterInstMinDate() {
 		return filterInstMinDate;
 	}
 
 
 	public void setFilterInstMinDate(Date filterInstMinDate) {
 		this.filterInstMinDate = filterInstMinDate;
 	}
 
 
 	public Date getFilterInstMaxDate() {
 		return filterInstMaxDate;
 	}
 
 
 	public void setFilterInstMaxDate(Date filterInstMaxDate) {
 		this.filterInstMaxDate = filterInstMaxDate;
 	}
 
 
 	public Integer getFilterCompanyId() {
 		return filterCompanyId;
 	}
 
 
 	public void setFilterCompanyId(Integer filterCompanyId) {
 		this.filterCompanyId = filterCompanyId;
 	}
 
 
 	@Override
 	protected void updateFields(String sortField, SortOrder sortOrder, Map<String, String> filters) {
 		if (!ignoreUiTableFilters && filters != null) {
 			Integer newCompanyId = null;
 			String tmp = filters.get("company.id");
 			if (tmp != null) {
 				newCompanyId = Integer.parseInt(tmp);
 			}
 			setFilterCompanyId(newCompanyId);
 
 			Boolean newFilterAct = null;
			tmp = filters.get("closed");
			System.out.println("Filter[closed]:" + newFilterAct);
 			if (tmp != null) {
 				newFilterAct = Boolean.valueOf(tmp);
 			}
 			setFilterActiveContract(newFilterAct);
 		}
 		System.out.println("Company ID: " + filterCompanyId + "; closed: " + filterActiveContract);
 	}
 
 
 	@Override
 	protected void initPager(Map<String, String> filters) {
 		System.out.println("Contract: min date = " + filterContractMinDate + "; max date = " + filterContractMaxDate);
 		System.out.println("Installment: min date = " + filterInstMinDate + "; max date = " + filterInstMaxDate);
 
 		System.out.println("Querying");
 		contractSearch.initWithLoggedUserCode(filterContractMinDate, filterContractMaxDate, filterCompanyId, null, Agreement.class,
 				filterActiveContract, filterInstMinDate, filterInstMaxDate);
 
 	}
 
 
 	@Override
 	protected FilterList initFilterList() {
 		FilterList l = new FilterList();
 
 		if (filterActiveContract != null) {
			l.put("closed", filterActiveContract.toString());
 		}
 		if (filterCompanyId != null) {
 			l.put("company.id", filterCompanyId.toString());
 		}
 		if (filterContractMinDate != null) {
 			l.put("fcontrmindate", String.valueOf(filterContractMinDate.getTime()));
 		}
 		if (filterContractMaxDate != null) {
 			l.put("fcontrmaxdate", String.valueOf(filterContractMaxDate.getTime()));
 		}
 		if (filterInstMinDate != null) {
 			l.put("finstmindate", String.valueOf(filterInstMinDate.getTime()));
 		}
 		if (filterInstMaxDate != null) {
 			l.put("finstmaxdate", String.valueOf(filterInstMaxDate.getTime()));
 		}
 
 		return l;
 	}
 
 
 	@Override
 	protected ResultPagerBean<Contract> getPager() {
 		return contractSearch;
 	}
 
 }
