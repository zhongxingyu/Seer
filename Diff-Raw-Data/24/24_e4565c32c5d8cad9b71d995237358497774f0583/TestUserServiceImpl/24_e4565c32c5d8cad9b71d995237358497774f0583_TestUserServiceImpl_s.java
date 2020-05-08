 package com.epam.kharkiv.cdp.oleshchuk.cinema.service.impl;
 
 import com.epam.kharkiv.cdp.oleshchuk.cinema.dao.jdbc.impl.TestUserJdbcMySqlDao;
 import com.epam.kharkiv.cdp.oleshchuk.cinema.service.TestUserService;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Propagation;
 import org.springframework.transaction.annotation.Transactional;
 
 @Service
@Transactional(rollbackFor = Exception.class)
 public class TestUserServiceImpl implements TestUserService {
 
     @Autowired
     private TestUserJdbcMySqlDao userDao;
 
     @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
     public void insertUserReadOnlyPropagationSupports(String userName) {
         userDao.insertUser(userName);
     }
 
     @Transactional(readOnly = true, propagation = Propagation.REQUIRED)
     public void insertUserReadOnlyPropagationRequired(String userName) {
         userDao.insertUser(userName);
     }
 
     @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void cleanTable() {
        userDao.cleanAllTable();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
     public void insertUserPropagationRequiresNew(String userName) throws Exception{
         userDao.insertUser(userName);
         insertUserPropagationRequiresNewInner("innerUserName");
        throw new Exception("Something going wrong");
     }
 
     @Transactional(propagation = Propagation.REQUIRES_NEW)
     public void insertUserPropagationRequiresNewInner(String userName) throws Exception{
         userDao.insertUser(userName);
     }
 
 
 
     @Transactional(propagation=Propagation.REQUIRED)
     public void testRequiredWithRequiresNew(String userName) throws Exception {
         userDao.insertUser(userName);
         requiresNewThrowsRuntimeException();
     }
 
     @Transactional(propagation=Propagation.REQUIRES_NEW)
     public void requiresNewThrowsRuntimeException() {
         throw new RuntimeException("Rollback this transaction!");
     }
 
     @Transactional(propagation=Propagation.REQUIRED)
     public void testRequiredWithSupports(String userName) throws Exception {
         userDao.insertUser(userName);
         supportsThrowsRuntimeException();
     }
 
     @Transactional(propagation=Propagation.SUPPORTS)
     public void supportsThrowsRuntimeException() {
         throw new RuntimeException("Rollback this transaction!");
     }
 
     @Transactional(propagation=Propagation.REQUIRED)
     public void testRequiredWithRequired(String userName) throws Exception {
         userDao.insertUser(userName);
         requiredThrowsRuntimeException();
     }
 
     @Transactional(propagation=Propagation.REQUIRED)
     public void requiredThrowsRuntimeException() {
         throw new RuntimeException("Rollback this transaction!");
     }
 
     @Transactional(propagation=Propagation.REQUIRED)
     public void testRequiredWithNestedAndThrowException(String userName) throws Exception {
         userDao.insertUser(userName);
         nestedInsertUser("nestedUser");
         throw new RuntimeException("Rollback this transaction!");
     }
 
     @Transactional(propagation=Propagation.NESTED)
     public void nestedInsertUser(String userName) {
         userDao.insertUser(userName);
     }
 
     public int getAllCount() {
         return userDao.getAllCount();
     }
 
 }
