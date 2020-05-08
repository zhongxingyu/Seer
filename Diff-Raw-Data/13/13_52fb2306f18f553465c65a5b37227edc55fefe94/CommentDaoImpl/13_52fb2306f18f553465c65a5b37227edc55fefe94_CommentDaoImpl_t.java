 package com.duggan.workflow.server.dao;
 
 import java.math.BigDecimal;
 import java.math.BigInteger;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import javax.persistence.EntityManager;
 import javax.persistence.Query;
 
 import org.jbpm.task.Comment;
 import org.jbpm.task.Task;
 
 import com.duggan.workflow.server.dao.model.CommentModel;
 import com.duggan.workflow.server.db.DB;
 import com.duggan.workflow.server.helper.auth.LoginHelper;
 import com.duggan.workflow.server.helper.session.SessionHelper;
 import com.duggan.workflow.shared.model.UserGroup;
 
 public class CommentDaoImpl {
 
 	EntityManager em;
 	
 	public CommentDaoImpl(EntityManager em) {
 		this.em = em;
 	}
 
 	public CommentModel getComment(Long id) {
 		
 		List lst = em.createQuery("FROM CommentModel n where id= :id").setParameter("id", id).getResultList();
 		
 		if(lst.size()>0){
 			return (CommentModel)lst.get(0);
 		}
 		return null;
 	}
 
 	public CommentModel saveOrUpdate(CommentModel comment) {
 		if(comment.getId()==null){
 			comment.setCreated(new Date());
 			comment.setCreatedBy(SessionHelper.getCurrentUser().getUserId());
 		}else{
 			comment.setUpdated(new Date());
 			comment.setUpdatedBy(SessionHelper.getCurrentUser().getUserId());
 		}
 		em.persist(comment);
 		return comment;
 	}
 
 	@SuppressWarnings("unchecked")
 	public List<CommentModel> getAllComments(String userId) {
 
 		return em.createQuery("FROM CommentModel n where n.userId=:userId order by created desc")
 				.setParameter("userId", userId)
 				.getResultList();
 	}
 
 	public void delete(Long id) {
 		em.remove(getComment(id));
 	}
 
 	@SuppressWarnings("unchecked")
 	public List<CommentModel> getAllComments(Long documentId) {
 	
 		if(documentId==null){
 			return getAllComments();
 		}
 		
 		return em.createQuery("FROM CommentModel n where n.documentId=:documentId order by created desc")
 				.setParameter("documentId", documentId)
 				.getResultList();
 
 	}
 
 	public List<CommentModel> getAllComments() {
 
 		String userId = SessionHelper.getCurrentUser().getUserId();
 		
 		List<UserGroup> groups = LoginHelper.getHelper().getGroupsForUser(userId);
 		String groupsIds="";
 		for(UserGroup group: groups){
 			groupsIds = groupsIds.concat(group.getName()+",");
 		}
 		
 		if(groupsIds.isEmpty()){
 			return new ArrayList<>();
 		}
 		groupsIds = groupsIds.substring(0, groupsIds.length()-1);
 		
 		StringBuffer hql = new StringBuffer("Select c.id FROM localcomment c "+
 		//"d.id documentId "+
 		"inner join LocalDocument d on (d.id=c.documentId) "+
 		"left join Task t on (t.processInstanceId=d.processInstanceId) "+
 		"left join OrganizationalEntity owner on (owner.id= t.actualOwner_id and owner.DTYPE='User') "+ 
 		"left join PeopleAssignments_PotOwners potowners on (potowners.task_id=t.id)  "+
 		"where "+
 		"t.archived = 0 and "+ 
 		"(d.createdBy=? or " +
 		" owner.id = ? or "+
 		"( potowners.entity_id = ? or potowners.entity_id in (?) )) " +
 		" order by c.created desc"
 		 );
 		
 		Query query = em.createNativeQuery(hql.toString())
 				.setParameter(1, userId)
 				.setParameter(2, userId)
 				.setParameter(3, userId)
 				.setParameter(4, groupsIds);
 		
 		@SuppressWarnings("unchecked")
 		List<BigInteger> commentIds = query.getResultList(); 
 		
 		List<Long> ids = new ArrayList<>();
 		for(BigInteger id: commentIds){
 			ids.add(id.longValue());
 		}
 		
		if(ids==null || ids.isEmpty()){
			return new ArrayList<>();
		}
		
 		@SuppressWarnings("unchecked")
 		List<CommentModel> comments = em.createQuery("FROM CommentModel where id in (:ids)")
 			.setParameter("ids", ids).getResultList();
 		
 		return comments;
 	}
 	
 	
 
 }
