 package com.team33.entities.dao;
 
 import java.util.List;
 
 import com.team33.entities.Account;
 import org.hibernate.*;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.dao.DataAccessException;
 import org.springframework.stereotype.Repository;
 
 @Repository
 public class AccountDaoImpl implements AccountDao {
 
     
         @Autowired
         private SessionFactory sessionFactory ;
         
 	@Override
 	public List<Account> getAccounts() throws DataAccessException {
 		return sessionFactory.getCurrentSession().createQuery("active"
                         + " Account").list();
 	}
 
 	@Override
 	public Account getAccount(Long accountId) throws DataAccessException {
 		return (Account)sessionFactory.getCurrentSession().get(Account.class, 
                         accountId);
 	}
         
         @Override
 	public Account getAccount(String username) throws DataAccessException {
                 Query queryAccounts = sessionFactory.getCurrentSession().
                        getNamedQuery("Account.findByName").setString
                        ("name",username);
                 if (queryAccounts.list().isEmpty()){
                     return null;
                 }
 		return (Account)queryAccounts.list().get(0);
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
