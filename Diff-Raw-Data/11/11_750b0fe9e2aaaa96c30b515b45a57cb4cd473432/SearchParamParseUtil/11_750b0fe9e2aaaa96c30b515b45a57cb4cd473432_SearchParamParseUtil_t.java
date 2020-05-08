 /**
  * Acceso Inteligente
  *
  * Copyright (C) 2010-2011 Fundación Ciudadano Inteligente
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.accesointeligente.server;
 
 import org.accesointeligente.shared.RequestSearchParams;
 import org.accesointeligente.shared.RequestStatus;
 
 import org.hibernate.Criteria;
import org.hibernate.criterion.*;
 
 public class SearchParamParseUtil {
 
 	public static void criteriaAddSearchParams(Criteria criteria, RequestSearchParams params) {
 		if (params.getKeyWord() != null && !params.getKeyWord().equals("")) {
 			Conjunction keywordConjunction = Restrictions.conjunction();
 			Disjunction keyWordDisjunction;
 			String[] keyWords = params.getKeyWord().split("[ ]");
 
 			for (int i = 0; i < keyWords.length; i++) {
 				keyWords[i]  = keyWords[i].replaceAll("\\W", "");
 				keyWordDisjunction = Restrictions.disjunction();
				keyWordDisjunction.add(Restrictions.ilike("information", keyWords[i], MatchMode.ANYWHERE));
				keyWordDisjunction.add(Restrictions.ilike("context", keyWords[i], MatchMode.ANYWHERE));
				keyWordDisjunction.add(Restrictions.ilike("title", keyWords[i], MatchMode.ANYWHERE));
 				keywordConjunction.add(keyWordDisjunction);
 			}
 			criteria.add(keywordConjunction);
 		}
 
 		if (params.getInstitution() != null) {
 			criteria.add(Restrictions.eq("institution", params.getInstitution()));
 		}
 
 		if (params.getMinDate() != null && params.getMaxDate() != null) {
 			criteria.add(Restrictions.between("confirmationDate", params.getMinDate(), params.getMaxDate()));
 		} else if (params.getMinDate() != null) {
 			criteria.add(Restrictions.ge("confirmationDate", params.getMinDate()));
 		} else if (params.getMaxDate() != null) {
 			criteria.add(Restrictions.le("confirmationDate", params.getMaxDate()));
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
 				statusDisjunction.add(Restrictions.eq("status", RequestStatus.NEW));
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
 				keyWords[i]  = keyWords[i].replaceAll("\\W", "");
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
 			filters += " AND confirmationDate BETWEEN :minDate AND :maxDate";
 		} else if (params.getMinDate() != null) {
 			filters += " AND confirmationDate >= :minDate ";
 		} else if (params.getMaxDate() != null) {
 			filters += " AND confirmationDate <= :maxDate ";
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
 				filters += "'" + RequestStatus.PENDING.toString() + "',";
 				filters += "'" + RequestStatus.NEW.toString() + "'";
 			}
 
 			if (filters.charAt(filters.length() - 1) == ',') {
 				filters = filters.substring(0, filters.length() - 2);
 			}
 
 			filters += " )";
 		}
 		return filters;
 	}
 }
