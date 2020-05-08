 package com.duggan.workflow.server.dao;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import javax.persistence.EntityManager;
 import javax.persistence.NoResultException;
 import javax.persistence.Query;
 
 import com.duggan.workflow.server.dao.model.Group;
 import com.duggan.workflow.server.dao.model.User;
 
 public class UserGroupDaoImpl extends BaseDaoImpl{
 
 	
 	public UserGroupDaoImpl(EntityManager em){
 		super(em);
 	}
 	
 	public User getUser(String userId){
 		Query query = em.createQuery("from BUser u where u.userId=:userId");
 		query.setParameter("userId", userId);
		Object user= null;
		try{
			user = query.getSingleResult();
			return (User)user;
		}catch(NoResultException e){
			
		}
		
 		
		return null;
 	}
 	
 	public Group getGroup(String groupId){
 		Query query = em.createQuery("from BGroup p where p.name=:name");
 		query.setParameter("name", groupId);
 		
 		Object group =null;
 		
 		try{
 			group  = query.getSingleResult();
 		}catch(NoResultException e){}
 		
 		return (Group)group;
 	}
 	
 	public void saveGroup(Group group){
 		save(group);
 	}
 	
 	public void saveUser(User user){
 		List<Group> groups = new ArrayList<>();
 		groups.addAll(user.getGroups());
 		
 		user.setGroups(null);
 		save(user);
 		
 		if(groups!=null){
 			
 			for(Group group: groups){
 				prepare(group);
 				//em.merge(group);
 			}
 		}
 		
 		user.setGroups(groups);
 		em.merge(user);		
 	}
 
 	@SuppressWarnings("unchecked")
 	public List<User> getAllUsers() {
 		Query query = em.createQuery("FROM BUser");		
 		return query.getResultList();
 	}
 
 	@SuppressWarnings("unchecked")
 	public List<Group> getAllGroups() {
 		Query query = em.createQuery("FROM BGroup");		
 		return query.getResultList();
 	}
 
 	public User getUser(Long id) {
 		
 		return em.find(User.class, id);
 	}
 
 	public Collection<Group> getAllGroups(String userId) {
 		
 		User user = getUser(userId);
 		
 		assert user!=null;
 		
 		return user.getGroups();
 	}
 
 	public Collection<User> getAllUsers(String groupId) {
 		Group group = getGroup(groupId);
 		
 		return group.getMembers();
 	}
 	
 
 }
