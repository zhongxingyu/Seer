 package com.financial.pyramid.service.impl;
 
 import com.financial.pyramid.dao.AccountDao;
 import com.financial.pyramid.domain.Account;
 import com.financial.pyramid.service.AccountService;
 import org.joda.time.DateTime;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 import java.util.Date;
 
 /**
  * User: dbudunov
  * Date: 29.08.13
  * Time: 14:31
  */
 @Service("accountService")
 @Transactional
 public class AccountServiceImpl implements AccountService {
 
     @Autowired
     private AccountDao accountDao;
 
     @Override
     public void activate(Account account) {
         Date currentDate = new Date();
         Date newActivationDate = null;
         if (account.getDateExpired().before(currentDate)){
             newActivationDate = currentDate;
         } else {
             newActivationDate = account.getDateExpired();
         }
         account.setLocked(false);
         account.setDateActivated(newActivationDate);
        account.setDateExpired(new DateTime(newActivationDate).plusMonths(1).plusDays(1).toDate());
         update(account);
     }
 
     @Override
     public void deactivate(Account account) {
         account.setLocked(true);
         update(account);
     }
 
     @Override
     public void calculateSum(Account account, Double sum) {
         account.setEarningsSum(account.getEarningsSum() + sum);
         update(account);
     }
 
     @Override
     public void update(Account account) {
         accountDao.saveOrUpdate(account);
     }
 
     @Override
     public Account findById(Long id) {
         return accountDao.findById(id);
     }
 }
