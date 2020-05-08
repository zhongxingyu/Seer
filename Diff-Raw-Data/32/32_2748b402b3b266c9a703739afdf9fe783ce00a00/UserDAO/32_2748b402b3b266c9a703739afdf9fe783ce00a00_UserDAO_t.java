 package com.biscato.kollaidoscope.persistence;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import javax.jdo.PersistenceManager;
 import javax.jdo.Query;
 
 import com.biscato.kollaidoscope.model.User;
 
 public class UserDAO implements EntityDAO<User> {
 
 	@Override
 	public User createEntity(User entity) {
 		PersistenceManager pm = PMF.get().getPersistenceManager();
 		User newEntity;
 		
 		try{
 			newEntity = pm.makePersistent(entity);
 		}
 		finally{
 			pm.close();
 		}
 		return newEntity;
 	}
 
 	@Override
 	public List<User> createAllEntities(List<User> entities) {
 		Collection<User> newEntities;
 		List<User> createdUsers;
 		
 		PersistenceManager pm = PMF.get().getPersistenceManager();
 		Collection<User> collection = new ArrayList<User>(entities);
 		
 		try{
 			newEntities = pm.makePersistentAll(collection);
 		}
 		finally{
 			pm.close();
 		}
 		createdUsers = new ArrayList<User>(newEntities);
 		return createdUsers;
 	}
 
 	@Override
 	public boolean deleteEntity(User entity) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean deleteAllEntities() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public User updateEntity(User entity) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 	
 	@Override
 	public User getEntityForId(long id) {
 		PersistenceManager pm = PMF.get().getPersistenceManager();
 		return (User) pm.getObjectById(User.class, id);
 	}
 
 	@Override
 	public List<User> getAllEntities() {
 		PersistenceManager pm = PMF.get().getPersistenceManager();
 		Query query = pm.newQuery(User.class);
 		Collection<User> myCol = (Collection<User>) query.execute();
 		return new ArrayList<User>(myCol);
 	}
 
 }
