 package presentationLayer.lazyModel;
 
 import java.util.Map;
 
 import javax.ejb.EJB;
 import javax.enterprise.context.Dependent;
 
 import businessLayer.Contract;
 import daoLayer.DeadlineSearchService;
 import daoLayer.ResultPagerBean;
 
 @Dependent
 public class OperatorContractScheduleLDM extends OperatorContractTableLDM {
 	private static final long serialVersionUID = 1L;
 
 	@EJB
 	private DeadlineSearchService searchService;
 
 
 	public OperatorContractScheduleLDM() {
 		super();
 	}
 
 
 	@Override
 	protected ResultPagerBean<Contract> getPager() {
 		return searchService;
 	}
 
 
 	@Override
 	protected void initPager(Map<String, String> filters) {
 		System.out.println("Min date: " + filterMinDate + "; max date: " + filterMaxDate);
 
 		System.out.println("Querying");
		searchService.init(filterMinDate, filterMaxDate, filterChiefId, filterCompanyId, null, getClassFromFilter());
 	}
 }
