 package org.glite.security.voms.admin.core.validation.strategies.impl;
 
 import java.util.List;
 
 import org.glite.security.voms.admin.core.validation.ValidationManager;
 import org.glite.security.voms.admin.core.validation.strategies.AUPFailingMembersLookupStrategy;
 import org.glite.security.voms.admin.core.validation.strategies.HandleAUPFailingMembersStrategy;
 import org.glite.security.voms.admin.event.EventManager;
 import org.glite.security.voms.admin.event.user.SignAUPTaskAssignedEvent;
 import org.glite.security.voms.admin.persistence.dao.VOMSUserDAO;
 import org.glite.security.voms.admin.persistence.dao.generic.AUPDAO;
 import org.glite.security.voms.admin.persistence.dao.generic.DAOFactory;
 import org.glite.security.voms.admin.persistence.dao.generic.TaskDAO;
 import org.glite.security.voms.admin.persistence.dao.hibernate.HibernateDAOFactory;
 import org.glite.security.voms.admin.persistence.model.AUP;
 import org.glite.security.voms.admin.persistence.model.VOMSUser;
 import org.glite.security.voms.admin.persistence.model.VOMSUser.SuspensionReason;
 import org.glite.security.voms.admin.persistence.model.task.SignAUPTask;
 import org.glite.security.voms.admin.persistence.model.task.Task;
 import org.glite.security.voms.admin.persistence.model.task.Task.TaskStatus;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class SuspendAUPFailingMembersStrategy implements HandleAUPFailingMembersStrategy, AUPFailingMembersLookupStrategy{
 
 	public static final Logger log = LoggerFactory.getLogger(SuspendAUPFailingMembersStrategy.class);
 	
 	
 	public List<VOMSUser> findAUPFailingMembers() {
 		
 		AUPDAO aupDAO = DAOFactory.instance().getAUPDAO();
 		VOMSUserDAO userDAO = VOMSUserDAO.instance();
 		
 		return userDAO.findAUPFailingUsers(aupDAO.getVOAUP());
 		
 	}
 
 	protected synchronized void handleAUPFailingMember(VOMSUser u){
 		
 		AUPDAO aupDAO = HibernateDAOFactory.instance().getAUPDAO();
 		AUP aup = aupDAO.getVOAUP();
 		
 		log.debug("Checking user '" + u + "' compliance with '" + aup.getName()
 				+ "'");
 		TaskDAO taskDAO = DAOFactory.instance().getTaskDAO();
 
 		if (u.getPendingSignAUPTask(aup) == null) {
 
 			SignAUPTask t = taskDAO.createSignAUPTask(aup);
 			u.assignTask(t);
 			log.debug("Sign aup task assigned to user '{}'",u);
 			EventManager.dispatch(new SignAUPTaskAssignedEvent(u, aup));
 
 		} else {
 
 			for (Task t : u.getTasks()) {
 
 				if (t instanceof SignAUPTask) {
 
 					SignAUPTask tt = (SignAUPTask) t;
 
 					if (tt.getAup().equals(aup)
 							&& tt.getStatus().equals(TaskStatus.EXPIRED)
 							&& !u.getSuspended()) {
						log.info("Suspeding user '" + u
 								+ "' that failed to sign AUP in time");
 
 						ValidationManager.instance().suspendUser(u, SuspensionReason.FAILED_TO_SIGN_AUP);
 					}
 
 				}
 			}
 
 		}
 		
 	}
 	
 	public void handleAUPFailingMembers(List<VOMSUser> aupFailingMembers) {
 		
 		if (aupFailingMembers == null || aupFailingMembers.isEmpty()){
 			return;
 		}
 		
 		for (VOMSUser u: aupFailingMembers)
 			handleAUPFailingMember(u);
 		
 	}
 }
