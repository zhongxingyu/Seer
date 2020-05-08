 package org.ocha.hdx.persistence.dao.view;
 
 import java.util.List;
 
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.TypedQuery;
 
 import org.ocha.hdx.persistence.entity.view.IndicatorMaxDate;
 
 public class IndicatorMaxDateDAOImpl implements IndicatorMaxDateDAO {
 
 	@PersistenceContext
 	private EntityManager em;
 
 	@Override
 	public List<IndicatorMaxDate> getValues(final List<String> entityCodes, final List<String> indicatorTypeCodes, final List<String> sourceCodes) {
 		final StringBuilder builder = new StringBuilder("SELECT imd FROM IndicatorMaxDate imd WHERE ");
 
 		boolean andNeeded = false;
 		if (entityCodes != null && !entityCodes.isEmpty()) {
			builder.append(" imd.entityCode IN (:entityCodes) ");
 			andNeeded = true;
 		}
 
 		if (indicatorTypeCodes != null && !indicatorTypeCodes.isEmpty()) {
 			if (andNeeded) {
 				builder.append(" AND ");
 			}
 			builder.append(" imd.indicatorTypeCode IN (:indicatorTypeCodes) ");
 			andNeeded = true;
 
 		}
 
 		if (sourceCodes != null && !sourceCodes.isEmpty()) {
 			if (andNeeded) {
 				builder.append(" AND ");
 			}
 			builder.append(" imd.sourceCode IN (:sourceCodes) ");
 		}
 		final TypedQuery<IndicatorMaxDate> query = em.createQuery(builder.toString(), IndicatorMaxDate.class);
 
 		if (entityCodes != null) {
 			query.setParameter("entityCodes", entityCodes);
 		}
 		if (indicatorTypeCodes != null) {
 			query.setParameter("indicatorTypeCodes", indicatorTypeCodes);
 		}
 		if (sourceCodes != null) {
 			query.setParameter("sourceCodes", sourceCodes);
 		}
 
 		return query.getResultList();
 	}
 }
