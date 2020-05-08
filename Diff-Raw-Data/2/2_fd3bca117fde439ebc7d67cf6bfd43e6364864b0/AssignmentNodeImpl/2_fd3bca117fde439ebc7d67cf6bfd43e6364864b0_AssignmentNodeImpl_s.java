 package org.devilry.core.dao;
 
 import java.util.Date;
 import java.util.List;
 
 import javax.ejb.EJB;
 import javax.ejb.TransactionAttribute;
 import javax.ejb.TransactionAttributeType;
 import javax.ejb.Stateless;
 import javax.persistence.*;
 
 import org.devilry.core.daointerfaces.AssignmentNodeLocal;
 import org.devilry.core.daointerfaces.AssignmentNodeRemote;
 import org.devilry.core.entity.*;
 
 @Stateless
 public class AssignmentNodeImpl extends BaseNodeImpl implements
 		AssignmentNodeRemote, AssignmentNodeLocal {
 
 	
 	private AssignmentNode getAssignmentNode(long nodeId) {
 		return (AssignmentNode) getNode(nodeId);
 	}
 
 	@SuppressWarnings("unchecked")
 	public List<Long> getDeliveries(long nodeId) {
 		Query q = em.createQuery("SELECT d.id FROM Delivery d WHERE d.assignment.id = :id");
 		q.setParameter("id", nodeId);
 		return q.getResultList();
 	}
 
 	
 	public List<Long> getDeliveriesWhereIsStudent(long assignmentId) {
 		long userId = userBean.getAuthenticatedUser();
 		
 		System.err.println("authenticated user:" + userId);
 		//System.err.println("authenticated user:" + );
 		
 		Query q = em.createQuery("SELECT d.id FROM Delivery d INNER JOIN d.students user WHERE user.id = :userId AND d.assignment.id =:assignmentId");
 		q.setParameter("assignmentId", assignmentId);
 		q.setParameter("userId", userId);
 		
 		return q.getResultList();
 	}
 	
 	public List<Long> getDeliveriesWhereIsExaminer(long assignmentId) {
 		long userId = userBean.getAuthenticatedUser();
 		
 		Query q = em.createQuery("SELECT d.id FROM Delivery d INNER JOIN d.examiners user WHERE user.id = :userId AND d.assignment.id =:assignmentId");
		q.setParameter("assignmentId", userId);
 		q.setParameter("userId", userId);
 		
 		return q.getResultList();
 	}
 		
 	public List<Long> getChildren(long nodeId) {
 		throw new UnsupportedOperationException(
 				"AssignmentNode does not have any children. Did you mean getDeliveries?");
 	}
 
 	public Date getDeadline(long nodeId) {
 		return getAssignmentNode(nodeId).getDeadline();
 	}
 
 	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
 	public void setDeadline(long nodeId, Date deadline) {
 		AssignmentNode a = getAssignmentNode(nodeId);
 		a.setDeadline(deadline);
 		em.persist(a);
 	}
 
 	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
 	public long create(String name, String displayName, Date deadline,
 			long parentId) {
 		AssignmentNode node = new AssignmentNode();
 		node.setName(name.toLowerCase());
 		node.setDisplayName(displayName);
 		node.setDeadline(deadline);
 		node.setParent(getNode(parentId));
 		em.persist(node);
 		em.flush();
 		return node.getId();
 	}
 
 //	@TransactionAttribute(TransactionAttributeType.REQUIRED)
 //	public void remove(long nodeId) {
 //		// TODO: Find out why this also deletes Delivery objects with this assignment as parent!
 //		Query q = em.createQuery("DELETE FROM Node n WHERE n.id = :id");
 //		q.setParameter("id", nodeId);
 //		q.executeUpdate();
 //	}
 }
