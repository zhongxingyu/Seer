 package com.vacuumhead.wesplit.application;
 
 import com.vacuumhead.wesplit.dao.IGroupDao;
 import com.vacuumhead.wesplit.dao.IUserDao;
 import com.vacuumhead.wesplit.tables.Group;
 import com.vacuumhead.wesplit.tables.User;
 
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.PersistenceUnit;
 
 /**
  * Created with IntelliJ IDEA.
  * User: pratyushverma
  * Date: 03/03/13
  * Time: 2:28 AM
  * To change this template use File | Settings | File Templates.
  */
 public class GroupApplicationService implements IGroupApplicationService {
     @PersistenceUnit
     private EntityManagerFactory emf;
 
     public void setEmf(EntityManagerFactory emf) {
         this.emf = emf;
     }
 
     private IGroupDao groupDao;
     private IUserDao userDao;
 
     public GroupApplicationService(IGroupDao groupDao, IUserDao userDao) {
         this.groupDao = groupDao;
         this.userDao = userDao;
     }
 
     public Group createGroup(String groupName, Integer accountId) {
         EntityManager entityManager = emf.createEntityManager();
         try{
             entityManager.getTransaction().begin();
             if(retrieveGroupByName(groupName) != null) {
                 return null;
             }
             //if user exists
             User user = userDao.retrieveUserById(entityManager,accountId);
             if(user==null){
                 return  null;
             }
             Group newGroup = new Group(groupName);
             newGroup.getUserList().add(user);
             newGroup.getAdminList().add(user);
             user.getGroupAdminList().add(newGroup);
             //userDao.updateUser(entityManager,user);
             user.getGroupMemberList().add(newGroup);
             groupDao.createGroup(entityManager,newGroup);
 
             //groupDao.updateGroup(newGroup,user);
 
             return newGroup;
         }   finally {
             entityManager.getTransaction().commit();
         }
 
     }
 
     public Group retrieveGroupByName(String groupName) {
         EntityManager entityManager = emf.createEntityManager();
 
             return groupDao.retrieveGroupByName(entityManager,groupName);
 
 
     }
 
     public Group retrieveGroupById(Integer groupId) {
         EntityManager entityManager = emf.createEntityManager();
 
         return groupDao.retrieveGroupById(entityManager,groupId);
     }
 
     public Group addMembersToGroup(Integer groupId, Integer accountId) {
         EntityManager entityManager = emf.createEntityManager();
         try{
             entityManager.getTransaction().begin();
             User user = userDao.retrieveUserById(entityManager,accountId);
             Group group = groupDao.retrieveGroupById(entityManager,groupId);
             user.getGroupMemberList().add(group);
             group.getUserList().add(user);
 
             groupDao.updateGroup(entityManager,group);
             return group;
         }finally {
             entityManager.getTransaction().commit();
         }
 
     }
 
     public Group addAdminToGroup(Integer groupId, Integer accountId) {
         EntityManager entityManager = emf.createEntityManager();
         try{
             entityManager.getTransaction().begin();
         User user = userDao.retrieveUserById(entityManager,accountId);
         Group group = groupDao.retrieveGroupById(entityManager,groupId);
         group.getAdminList().add(user);
         groupDao.updateGroup(entityManager,group);
         return group;
         }finally {
             entityManager.getTransaction().commit();
         }
     }
 
    public boolean isAdmin(Integer groupId, Integer accountId) {
         EntityManager entityManager = emf.createEntityManager();
         User user = userDao.retrieveUserById(entityManager,accountId);
         Group group = groupDao.retrieveGroupById(entityManager,groupId);
        return group.getUserList().contains(user);
     }
 }
