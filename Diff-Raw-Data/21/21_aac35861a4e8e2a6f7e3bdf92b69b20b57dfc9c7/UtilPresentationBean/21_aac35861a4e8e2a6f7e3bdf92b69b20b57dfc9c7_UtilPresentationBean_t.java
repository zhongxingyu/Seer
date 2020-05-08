 package presentationLayer;
 
 import java.io.Serializable;
import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.ejb.EJB;
 import javax.enterprise.context.Dependent;
 import javax.faces.model.SelectItem;
import javax.inject.Inject;
 import javax.inject.Named;
 
 import usersManagement.RolePermission;
 import util.Messages;
import annotations.TransferObj;
 import businessLayer.Agreement;
 import businessLayer.AgreementType;
 import businessLayer.ChiefScientist;
 import businessLayer.Company;
 import businessLayer.Contract;
 import businessLayer.Department;
 import businessLayer.Funding;
 import businessLayer.Installment;
 import daoLayer.ChiefScientistDaoBean;
 import daoLayer.CompanyDaoBean;
 import daoLayer.DepartmentDaoBean;
 
 @Named("utilPB")
 @Dependent
 public class UtilPresentationBean implements Serializable {
 	private static final long serialVersionUID = 1L;
 	@EJB
 	private ChiefScientistDaoBean chiefDaoBean;
 	@EJB
 	private CompanyDaoBean companyDaoBean;
 	@EJB
 	private DepartmentDaoBean deptDao;
	
	@Inject 
	@TransferObj
	private Contract c;
 
 	private static final Map<Class<? extends Contract>, String> contractTypeName;
 
 	static {
 		contractTypeName = new HashMap<>();
 		contractTypeName.put(Contract.class, Messages.getString("contract"));
 		contractTypeName.put(Agreement.class, Messages.getString("agreement"));
 		contractTypeName.put(Funding.class, Messages.getString("funding"));
 	}
 
 
 	public String getNameFromClass(Class<? extends Contract> c) {
 		return contractTypeName.get(c);
 	}
 
 
 	public SelectItem[] getAgreementTypeItems() {
 		AgreementType[] types = AgreementType.values();
 		SelectItem[] result = new SelectItem[types.length];
 		for (int i = 0; i < types.length; i++) {
 			result[i] = new SelectItem(types[i], types[i].getDescription());
 		}
 		return result;
 	}
 
 
 	public SelectItem[] getRolesItems() {
 		RolePermission[] types = RolePermission.getUserRolePermission();
 		SelectItem[] result = new SelectItem[types.length];
 		for (int i = 0; i < types.length; i++) {
 			result[i] = new SelectItem(types[i], types[i].getDisplayString());
 		}
 		return result;
 
 	}
 
 
 	public SelectItem[] getChiefItems() {
 		List<ChiefScientist> chiefs = chiefDaoBean.getAll();
 		return getChiefsFromList(chiefs);
 	}
	
	public SelectItem[] getChiefItemsForCurrentDept(){

		List<String> deptSerial = new ArrayList<String>();
		if(c.getDepartment()!=null){
		deptSerial.add(c.getDepartment().getCode());
		}
		List<ChiefScientist> chiefs = chiefDaoBean.getByDeptSerials(deptSerial);
		return getChiefsFromList(chiefs);
	}
 
 
 	public SelectItem[] getDepthItems() {
 		List<Department> depths = deptDao.getAll();
 		SelectItem[] result = new SelectItem[depths.size()];
 		Department current = null;
 		for (int i = 0; i < depths.size(); i++) {
 			current = depths.get(i);
 			result[i] = new SelectItem(current, current.getDisplayName());
 		}
 		return result;
 	}
 
 
 	public SelectItem[] getCompanyItems() {
 		List<Company> companies = companyDaoBean.getAll();
 		SelectItem[] result = new SelectItem[companies.size()];
 		Company current = null;
 		for (int i = 0; i < companies.size(); i++) {
 			current = companies.get(i);
 			result[i] = new SelectItem(current, current.getName());
 		}
 		return result;
 	}
 
 
 	public SelectItem[] getFilterChiefItems() {
 		List<ChiefScientist> chiefs = chiefDaoBean.getAll();
 		SelectItem[] result = new SelectItem[chiefs.size() + 1];
 		result[0] = new SelectItem("", Messages.getString("allM"));
 		ChiefScientist current = null;
 		for (int i = 0; i < chiefs.size(); i++) {
 			current = chiefs.get(i);
 			result[i + 1] = new SelectItem(current.getId(), current.getCompleteName());
 		}
 		return result;
 	}
 
 
 	public SelectItem[] getFilterCompanyItems() {
 		List<Company> companies = companyDaoBean.getAll();
 		SelectItem[] result = new SelectItem[companies.size() + 1];
 		result[0] = new SelectItem("", Messages.getString("allF"));
 		Company current = null;
 		for (int i = 0; i < companies.size(); i++) {
 			current = companies.get(i);
 			result[i + 1] = new SelectItem(current.getId(), current.getName());
 		}
 		return result;
 	}
 
 
 	public SelectItem[] getChiefsItemsNotIn(Collection<ChiefScientist> list) {
 		List<ChiefScientist> chiefs = chiefDaoBean.getAll();
 		chiefs.removeAll(list);
 		return getChiefsFromList(chiefs);
 	}
 
 
 	private SelectItem[] getChiefsFromList(List<ChiefScientist> list) {
 		SelectItem[] result = new SelectItem[list.size()];
 		ChiefScientist current = null;
 		for (int i = 0; i < list.size(); i++) {
 			current = list.get(i);
 			result[i] = new SelectItem(current, current.getCompleteName());
 		}
 		return result;
 	}
 
 
 	public SelectItem[] getBooleanFilter() {
 		return getBooleanFilter(Boolean.TRUE.toString(), Boolean.FALSE.toString());
 	}
 
 
 	public SelectItem[] getBooleanFilter(String trueLabel, String falseLabel) {
 		SelectItem[] result = new SelectItem[3];
 		result[0] = new SelectItem("", Messages.getString("allM"));
 		result[1] = new SelectItem(Boolean.TRUE.toString(), trueLabel);
 		result[2] = new SelectItem(Boolean.FALSE.toString(), falseLabel);
 		return result;
 	}
 
 
 	public SelectItem[] getContractFilter() {
 		SelectItem[] result = new SelectItem[3];
 		result[0] = new SelectItem(Contract.class.getName(), Messages.getString("allM"));
 		result[1] = new SelectItem(Agreement.class.getName(), Messages.getString("agreement"));
 		result[2] = new SelectItem(Funding.class.getName(), Messages.getString("funding"));
 		return result;
 	}
 
 
 	public Date findClosestDeadline(Contract contract, Date minDate) {
 		// TODO eliminare
 		// List<Installment> insts = contract.getInstallments();
 		// Date closestDeadline = null;
 		// boolean found = false;
 		// Iterator<Installment> it = insts.iterator();
 		// while (it.hasNext() && !found) {
 		// closestDeadline = it.next().getDate();
 		// if (null == minDate || !closestDeadline.before(minDate)) {
 		// found = true;
 		// }
 		// }
 		// return closestDeadline;
 
 		List<? extends Installment> insts = contract.getInstallments();
 		Date current, closestDeadline = null;
 
 		for (Installment i : insts) {
 			if (!i.isPaidInvoice()) {
 				current = i.getDate();
 
 				if (null == current) {
 					System.err.println(new Date() + ": null installment date (installment #" + i.getId() + ")");
 				}
 				else if (null == minDate || !current.before(minDate)) {
 					if (null == closestDeadline || current.before(closestDeadline)) {
 						closestDeadline = current;
 					}
 				}
 			}
 		}
 
 		return closestDeadline;
 	}
 
 
 	public UtilPresentationBean() {}
 }
