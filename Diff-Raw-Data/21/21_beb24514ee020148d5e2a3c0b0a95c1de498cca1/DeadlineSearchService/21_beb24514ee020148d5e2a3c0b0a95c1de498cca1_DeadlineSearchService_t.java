 package daoLayer;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import javax.ejb.Stateful;
 import javax.enterprise.context.ConversationScoped;
 import javax.persistence.TemporalType;
 import javax.persistence.criteria.CriteriaBuilder;
 import javax.persistence.criteria.CriteriaQuery;
 import javax.persistence.criteria.ParameterExpression;
 import javax.persistence.criteria.Predicate;
 import javax.persistence.criteria.Root;
 
 import org.primefaces.model.SortOrder;
 
 import security.AdminAllowed;
 import businessLayer.Contract;
 
 @Stateful
 @ConversationScoped
 public class DeadlineSearchService extends ResultPagerBean<Contract> {
 
 	public DeadlineSearchService() {
 	}
 
 	@AdminAllowed
 	public void init(Date lowerDate, Date upperDate, Integer chiefId,
 			Integer companyId, SortOrder order, Class<? extends Contract> contractClass) {
 		currentPage = 0;
 
 		CriteriaBuilder cb = em.getCriteriaBuilder();
 		CriteriaQuery<Contract> c = cb.createQuery(Contract.class);
 		Root<? extends Contract> agr = c.from(contractClass);
 		//Root<AgreementInstallment> inst = c.from(AgreementInstallment.class);
 	
 		// TODO funziona sul serio?
		c.select(agr).distinct(true).where(	cb.equal(agr.join("installments").get("paidInvoice"), false));
 //		c.select(agr)
 //				.distinct(true)
 //						.where(cb.equal(agr,
 //						inst.get("contract")));
 
 		List<Predicate> criteria = new ArrayList<Predicate>();
 		
 //		criteria.add(cb.equal(inst.get("paidInvoice"), false));
 		if (lowerDate != null) {
 
 			ParameterExpression<Date> p = cb.parameter(Date.class, "lowerDate");
 			criteria.add(cb.greaterThanOrEqualTo(agr.join("installments").<Date> get("date"), p));
 
 		}
 
 		if (upperDate != null) {
 
 			ParameterExpression<Date> p = cb.parameter(Date.class, "upperDate");
 			criteria.add(cb.lessThanOrEqualTo(agr.join("installments").<Date> get("date"), p));
 
 		}
 
 		if (chiefId != null) {
 
 			ParameterExpression<Integer> p = cb.parameter(Integer.class,
 					"chiefId");
 			criteria.add(cb.equal(agr.get("chief").get("id"), p));
 
 		}
 		if (companyId != null) {
 
 			ParameterExpression<Integer> p = cb.parameter(Integer.class,
 					"companyId");
 			criteria.add(cb.equal(agr.get("company").get("id"), p));
 
 		}
 
 		if (order == SortOrder.ASCENDING) {
 
 			c.orderBy(cb.asc(agr.join("installments").<Date> get("date")));
 		} else if (order == SortOrder.DESCENDING) {
 
 			c.orderBy(cb.desc(agr.join("installments").<Date> get("date")));
 
 		}
 
 		if (criteria.size() != 0) {
 
 			if (criteria.size() == 1) {
 				c.where(criteria.get(0));
 			}
 
 			else {
 				c.where(cb.and(criteria.toArray(new Predicate[0])));
 			}
 
 			query = em.createQuery(c);
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
 
 		} else {
 
 			query = em.createQuery(c);
 		}
 
 	}
 
 }
