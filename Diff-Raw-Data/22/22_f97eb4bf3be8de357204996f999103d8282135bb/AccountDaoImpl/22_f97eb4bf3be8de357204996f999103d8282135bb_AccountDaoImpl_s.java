 package com.team33.entities.dao;
 
 import java.util.List;
 
 import com.team33.entities.Account;
 import org.hibernate.*;
 import org.springframework.beans.factory.annotation.Autowired;
 import com.team33.services.exception.DataAccessException;
 import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
 import org.springframework.stereotype.Repository;
 
 @Repository
 public class AccountDaoImpl extends HibernateDaoSupport implements AccountDao {
 
     private static final int FIRST = 0;
     @Autowired
     private SessionFactory sessionFactory;
 
     @Override
     public List<Account> getAccounts() throws DataAccessException {
         return sessionFactory.getCurrentSession().createQuery("from"
                 + " Account").list();
     }
 
     @Override
     public Account getAccount(Long accountId) throws DataAccessException {
         return (Account) sessionFactory.getCurrentSession().get(Account.class,
                 accountId);
     }
 
     @Override
     public Account getAccount(String username) throws DataAccessException {
        Account currentUser = new Account();
        currentUser.setName(username);
        this.sessionFactory = this.getSessionFactory();
         try {
            return (Account) sessionFactory.getCurrentSession().getNamedQuery("Account.findByName").setString("name", username).list().get(FIRST);
         } catch (DataAccessException dae) {
            throw new DataAccessException("Data Access invalid"
                    + dae.getMessage());
         }
 
 
     }
 
     @Override
     public void saveAccount(Account account) throws DataAccessException {
         sessionFactory.getCurrentSession().save(account);
 
     }
 
     @Override
     public void removeAccount(Long accountId) {
         Account account = (Account) sessionFactory.getCurrentSession().load(
                 Account.class, accountId);
         if (null != account) {
             sessionFactory.getCurrentSession().delete(account);
         }
 
     }
 }
