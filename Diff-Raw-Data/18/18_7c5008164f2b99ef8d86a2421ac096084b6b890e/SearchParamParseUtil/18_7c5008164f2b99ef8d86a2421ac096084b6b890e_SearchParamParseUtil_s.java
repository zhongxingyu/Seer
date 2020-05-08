 package org.accesointeligente.server;
 
 import org.accesointeligente.shared.RequestSearchParams;
 import org.accesointeligente.shared.RequestStatus;
 
 import org.hibernate.Criteria;
 import org.hibernate.criterion.Disjunction;
 import org.hibernate.criterion.Restrictions;
 
 public class SearchParamParseUtil {
 
 	public static void criteriaAddSearchParams(Criteria criteria, RequestSearchParams params) {
 		if (params.getKeyWord() != null && !params.getKeyWord().equals("")) {
 			Disjunction keyWordDisjunction = Restrictions.disjunction();
 			String[] keyWords = params.getKeyWord().split("[ ]");
 			for (int i = 0; i < keyWords.length; i++) {
 				keyWordDisjunction.add(Restrictions.ilike("information", keyWords[i]));
 				keyWordDisjunction.add(Restrictions.ilike("context", keyWords[i]));
 				keyWordDisjunction.add(Restrictions.ilike("title", keyWords[i]));
 			}
 			criteria.add(keyWordDisjunction);
 		}
 
 		if (params.getInstitution() != null) {
 			criteria.add(Restrictions.eq("institution", params.getInstitution()));
 		}
 
 		if (params.getMinDate() != null && params.getMaxDate() != null) {
 			criteria.add(Restrictions.between("date", params.getMinDate(), params.getMaxDate()));
 		} else if (params.getMinDate() != null) {
 			criteria.add(Restrictions.ge("date", params.getMinDate()));
 		} else if (params.getMaxDate() != null) {
 			criteria.add(Restrictions.le("date", params.getMaxDate()));
 		}
 
 		if (params.getStatusClosed() || params.getStatusDerived() || params.getStatusExpired() || params.getStatusPending()) {
 			Disjunction statusDisjunction = Restrictions.disjunction();
 
 			// Closed Marked
 			if (params.getStatusClosed()) {
 				statusDisjunction.add(Restrictions.eq("status", RequestStatus.CLOSED));
 			}
 
 			// Derived Marked
 			if (params.getStatusDerived()) {
 				statusDisjunction.add(Restrictions.eq("status", RequestStatus.DERIVED));
 			}
 
 			// Expired Marked
 			if (params.getStatusExpired()) {
 				statusDisjunction.add(Restrictions.eq("status", RequestStatus.EXPIRED));
 			}
 
 			// Pending Marked
 			if (params.getStatusPending()) {
 				statusDisjunction.add(Restrictions.eq("status", RequestStatus.PENDING));
 			}
 			criteria.add(statusDisjunction);
 		}
 	}
 
 	public static String queryAddSearchParams(RequestSearchParams params) {
 		String filters = "";
 
 		if (params.getKeyWord() != null && !params.getKeyWord().equals("")) {
 			filters += " AND (";
 			String[] keyWords = params.getKeyWord().split("[ ]");
 
 			for (int i = 0; i < keyWords.length; i++) {
 				if (i != 0) {
 					filters += " OR ";
 				}
 				filters += " lower(information) like lower('%" + keyWords[i] + "%') OR ";
 				filters += " lower(context) like lower('%" + keyWords[i] + "%') OR ";
 				filters += " lower(title) like lower('%" + keyWords[i] + "%') ";
 			}
 			filters += ") ";
 		}
 
 		if (params.getInstitution() != null) {
 			filters += " AND (institutionId = " + params.getInstitution().getId() + ") ";
 		}
 
 		if (params.getMinDate() != null && params.getMaxDate() != null) {
 			filters += " AND date BETWEEN :minDate AND :maxDate";
 		} else if (params.getMinDate() != null) {
 			filters += " AND date >= :minDate ";
 		} else if (params.getMaxDate() != null) {
 			filters += " AND date <= :maxDate ";
 		}
 
 		if (params.getStatusClosed() || params.getStatusDerived() || params.getStatusExpired() || params.getStatusPending()) {
 			filters += " AND status IN (";
 
 			// Closed Marked
 			if (params.getStatusClosed()) {
 				filters += "'" + RequestStatus.CLOSED.toString() + "',";
 			}
 
 			// Derived Marked
 			if (params.getStatusDerived()) {
 				filters += "'" + RequestStatus.DERIVED.toString() + "',";
 			}
 
 			// Expired Marked
 			if (params.getStatusExpired()) {
 				filters += "'" + RequestStatus.EXPIRED.toString() + "',";
 			}
 
 			// Pending Marked
 			if (params.getStatusPending()) {
				filters += "'" + RequestStatus.PENDING.toString() + "'";
 			}
 
 			if (filters.charAt(filters.length() - 1) == ',') {
 				filters = filters.substring(0, filters.length() - 2);
 			}
 
 			filters += " )";
 		}
 		return filters;
 	}
 }
