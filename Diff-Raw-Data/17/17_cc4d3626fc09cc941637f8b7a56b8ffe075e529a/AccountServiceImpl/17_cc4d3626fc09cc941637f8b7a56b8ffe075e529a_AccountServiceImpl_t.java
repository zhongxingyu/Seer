 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.team33.services;
 
 import com.team33.entities.Account;
 import com.team33.entities.dao.AccountDaoImpl;
 import com.team33.services.exception.*;
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
     private AccountDaoImpl accountDaoImpl;
     
    public void setAccountDaoImpl(AccountDaoImpl dao) {
         this.accountDaoImpl = dao;
     }
 
     @Transactional
     public Account loginAccount(String username, String password) throws AuthenticationException, AccountNotFoundException, AccountNotActivatedException, LoginException {
             if (username == null || username.equals("")
                     || password == null || password.equals("")) {
                 throw new LoginException("Invalid login info!");
             }
         Account account = this.accountDaoImpl.getAccount(username);
         if (account == null) {
             throw new AccountNotFoundException("Account was not found");
         }
         if (account.getActivated() != Boolean.TRUE) {
             throw new AccountNotActivatedException("Account not activated!");
         } else {
             if (account.getName().matches(username)) {
                 if (!account.getPassword().matches(password)) {
                     throw new AuthenticationException("Unable to authenticate the account");
                 }
             }
         }
         return account;
     }
 
 @Transactional
         @Override
         public void registerAccount(Account account) throws DataAccessException {
         accountDaoImpl.saveAccount(account);
     }
 
     @Transactional
         @Override
         public Account getAccount(Long accountId) throws DataAccessException {
         return accountDaoImpl.getAccount(accountId);
     }
 
     @Transactional
         @Override
         public List<Account> getAccounts() throws DataAccessException {
         return accountDaoImpl.getAccounts();
     }
 
     @Transactional
         @Override
         public void removeAccount(Long accountID) {
         accountDaoImpl.removeAccount(accountID);
     }
 }
