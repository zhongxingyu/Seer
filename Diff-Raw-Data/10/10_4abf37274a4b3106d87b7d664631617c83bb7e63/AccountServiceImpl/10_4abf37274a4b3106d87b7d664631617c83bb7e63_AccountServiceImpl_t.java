 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.team33.services;
 
 import com.team33.services.exception.AuthenticationException;
 import com.team33.entities.Account;
 import com.team33.entities.dao.AccountDao;
 import com.team33.services.exception.AccountNotActivatedException;
 import com.team33.services.exception.AccountNotFoundException;
 import java.util.List;
 import org.springframework.dao.DataAccessException;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 /**
  *
  * @author Samual
  */
 @Service
 public class AccountServiceImpl implements AccountService {
 
     @Autowired
     private AccountDao accountDao;
     
     public void setAccountDAO(AccountDao dao) {
         this.accountDao = dao;
     }
 
 
     @Transactional
     public Account loginAccount(String username, String password) throws AuthenticationException, AccountNotFoundException, AccountNotActivatedException { 
        Account account = this.accountDao.getAccount(username);
         if (account == null)
             throw new AccountNotFoundException("Account was not found");
         if (account.getActivated() != Boolean.TRUE) {
             throw new AccountNotActivatedException("Account not activated!");
         } else {
             if (account.getName().matches(username))
                 if (!account.getPassword().matches(password))
                     throw new AuthenticationException("Unable to authenticate the account");
         }
         return account;
     }
 
     @Transactional
     @Override
     public void registerAccount(Account account) throws DataAccessException {
          accountDao.saveAccount(account);
     }
     
     @Transactional
     @Override
     public Account getAccount(Long accountId) throws DataAccessException{
         return accountDao.getAccount(accountId);
     }
 
     @Transactional
     @Override
     public List<Account> getAccounts() throws DataAccessException {
         return accountDao.getAccounts();
     }
 
     @Transactional
     @Override
     public void removeAccount(Long accountID) {
        accountDao.removeAccount(accountID);
     }
     
 }
 
