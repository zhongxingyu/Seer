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
 package org.accesointeligente.server.robots;
 
 import org.accesointeligente.model.Request;
 import org.accesointeligente.server.HibernateUtil;
 import org.accesointeligente.server.RobotContext;
 import org.accesointeligente.shared.RequestStatus;
 
 import org.apache.log4j.Logger;
 import org.hibernate.Criteria;
 import org.hibernate.FetchMode;
 import org.hibernate.Session;
 import org.hibernate.criterion.Restrictions;
 
 import java.util.List;
 
 public class RequestCreator {
 	private static final Logger logger = Logger.getLogger(RequestCreator.class);
 
 	public void createRequests() {
 		Session hibernate = null;
 
 		try {
 			hibernate = HibernateUtil.getSession();
 			hibernate.beginTransaction();
 			Criteria criteria = hibernate.createCriteria(Request.class);
 			criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
 			criteria.setFetchMode("institution", FetchMode.JOIN);
 			criteria.add(Restrictions.eq("status", RequestStatus.NEW));
			criteria.createAlias("institution", "ins").add(Restrictions.eq("ins.masterEnabled", "true"));
 			List<Request> newRequests = criteria.list();
 			hibernate.getTransaction().commit();
 
 			for (Request request : newRequests) {
 				if (!request.getInstitution().getEnabled()) {
 					continue;
 				}
 
 				logger.info("requestId = " + request.getId());
 
 				try {
 					Robot robot = RobotContext.getRobot(request.getInstitution().getInstitutionClass());
 
 					if (robot != null) {
 						request = robot.makeRequest(request);
 						hibernate = HibernateUtil.getSession();
 						hibernate.beginTransaction();
 						hibernate.update(request);
 						hibernate.getTransaction().commit();
 					}
 				} catch (Exception ex) {
 					logger.error("requestId = " + request.getId(), ex);
 				}
 			}
 		} catch (Exception ex) {
 			if (hibernate != null && hibernate.isOpen() && hibernate.getTransaction().isActive()) {
 				hibernate.getTransaction().rollback();
 				logger.error("Failure", ex);
 			}
 		}
 	}
 }
