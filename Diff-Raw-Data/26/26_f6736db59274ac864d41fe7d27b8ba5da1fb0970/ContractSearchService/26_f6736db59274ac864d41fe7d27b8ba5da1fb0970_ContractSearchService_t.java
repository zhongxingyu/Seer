 package daoLayer;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import javax.ejb.Stateful;
 import javax.enterprise.context.ConversationScoped;
 import javax.inject.Inject;
 import javax.persistence.TemporalType;
 import javax.persistence.criteria.CriteriaBuilder;
 import javax.persistence.criteria.CriteriaQuery;
 import javax.persistence.criteria.ParameterExpression;
 import javax.persistence.criteria.Predicate;
 import javax.persistence.criteria.Root;
 
 import org.primefaces.model.SortOrder;
 
 import security.AlterContractsAllowed;
 import security.ChiefScientistAllowed;
 import security.Principal;
 import annotations.Logged;
 import businessLayer.Contract;
 
 @Stateful
 @ConversationScoped
 public class ContractSearchService extends ResultPagerBean<Contract> {
 
 	@Inject
 	@Logged
 	private Principal principal;
 
 
 	@ChiefScientistAllowed
 	public void initWithLoggedUserCode(Date lowerDeadLineDate, Date upperDeadLineDate, Integer companyId, SortOrder order,
 			Class<? extends Contract> contractClass, Boolean closed, Date lowerInstDeadlineDate, Date upperInstDeadlineDate) {
 
 		String code = principal.getSerialNumber();
 
 		_init(lowerDeadLineDate, upperDeadLineDate, null, companyId, order, contractClass, code, closed, upperInstDeadlineDate, lowerInstDeadlineDate);
 
 	}
 
 
 	@AlterContractsAllowed
 	public void init(Date lowerDeadLineDate, Date upperDeadLineDate, Integer chiefId, Integer companyId, SortOrder order,
 			Class<? extends Contract> contractClass, Boolean closed) {
 
 		_init(lowerDeadLineDate, upperDeadLineDate, chiefId, companyId, order, contractClass, null, closed, null, null);
 
 	}
 
 
 	private void _init(Date lowerDate, Date upperDate, Integer chiefId, Integer companyId, SortOrder order, Class<? extends Contract> contractClass,
 			String principalSerialNumber, Boolean closed, Date lowerInstDeadlineDate, Date upperInstDeadlineDate) {
 		currentPage = 0;
 
 		CriteriaBuilder cb = em.getCriteriaBuilder();
 		CriteriaQuery<Contract> c = cb.createQuery(Contract.class);
 		Root<? extends Contract> agr;
 
 		agr = c.from(contractClass);
 		if (upperInstDeadlineDate != null || lowerInstDeadlineDate != null) {
 
 			c.select(agr).distinct(true).where(cb.equal(agr.join("installments").get("paidInvoice"), false));
 		} else {
 
 			c.select(agr);
 		}
 
 		List<Predicate> criteria = new ArrayList<Predicate>();
 
 		if (lowerInstDeadlineDate != null) {
 
 			ParameterExpression<Date> p = cb.parameter(Date.class, "lowerInstDate");
 			criteria.add(cb.greaterThanOrEqualTo(agr.join("installments").<Date> get("date"), p));
 		}
 
 		if (upperInstDeadlineDate != null) {
 
 			ParameterExpression<Date> p = cb.parameter(Date.class, "upperInstDate");
 			criteria.add(cb.lessThanOrEqualTo(agr.join("installments").<Date> get("date"), p));
 		}
 
 		if (lowerDate != null) {
 
 			ParameterExpression<Date> p = cb.parameter(Date.class, "lowerDate");
 			criteria.add(cb.greaterThanOrEqualTo(agr.<Date> get("deadlineDate"), p));
 
 		}
 
 		if (upperDate != null) {
 
 			ParameterExpression<Date> p = cb.parameter(Date.class, "upperDate");
 			criteria.add(cb.lessThanOrEqualTo(agr.<Date> get("deadlineDate"), p));
 
 		}
 
 		if (chiefId != null) {
 
 			ParameterExpression<Integer> p = cb.parameter(Integer.class, "chiefId");
 			criteria.add(cb.equal(agr.get("chief").get("id"), p));
 
 		}
 		if (companyId != null) {
 
 			ParameterExpression<Integer> p = cb.parameter(Integer.class, "companyId");
 			criteria.add(cb.equal(agr.get("company").get("id"), p));
 
 		}
 		if (principalSerialNumber != null) {
 			ParameterExpression<String> p = cb.parameter(String.class, "principalSerialNumber");
 			criteria.add(cb.equal(agr.get("chief").get("serialNumber"), p));
 		}
 		if (closed != null) {
			System.out.println("Query. Closed = " + closed);
 			ParameterExpression<Boolean> p = cb.parameter(Boolean.class, "closed");
 			criteria.add(cb.equal(agr.get("closed"), p));
 		}
 
 		if (order == SortOrder.ASCENDING) {
 
 			c.orderBy(cb.asc(agr.<Date> get("deadlineDate")));
 		} else if (order == SortOrder.DESCENDING) {
 
 			c.orderBy(cb.desc(agr.<Date> get("deadlineDate")));
 
 		}
 
 		if (criteria.size() != 0) {
 
 			if (criteria.size() == 1) {
 				c.where(criteria.get(0));
 			}
 
 			else {
 				c.where(cb.and(criteria.toArray(new Predicate[0])));
 			}
 
 			query = em.createQuery(c);
 
 			if (lowerInstDeadlineDate != null) {
 				query.setParameter("lowerInstDate", lowerInstDeadlineDate, TemporalType.DATE);
 			}
 			if (upperInstDeadlineDate != null) {
 				query.setParameter("upperInstDate", upperInstDeadlineDate, TemporalType.DATE);
 			}
 
 			if (lowerDate != null) {
 				query.setParameter("lowerDate", lowerDate, TemporalType.DATE);
 			}
 			if (upperDate != null) {
 				query.setParameter("upperDate", upperDate, TemporalType.DATE);
 			}
 			if (chiefId != null) {
 				query.setParameter("chiefId", chiefId);
 			}
 			if (companyId != null) {
 				query.setParameter("companyId", companyId);
 			}
 			if (principalSerialNumber != null) {
 				query.setParameter("principalSerialNumber", principalSerialNumber);
 			}
 			if (closed != null) {
 				query.setParameter("closed", closed);
 			}
 
 		} else {
 
 			query = em.createQuery(c);
 		}
 
 	}
 
 }
