 package com.in6k.mypal.service;
 
 import com.in6k.mypal.dao.CommonDao;
 import com.in6k.mypal.dao.TransactionDao;
 import com.in6k.mypal.dao.UserDao;
 import com.in6k.mypal.domain.Transaction;
 import com.in6k.mypal.domain.User;
 import com.in6k.mypal.service.CreditCard.IncreaseBalanсeService;
 import com.in6k.mypal.util.SecurityUtil;
 import org.junit.Before;
 import org.junit.Test;
 
 import java.io.IOException;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.fail;
 
 public class TransactionServiceTest {
     @Before
     public void clearTables() {
         CommonDao.clearTable("transactions");
         CommonDao.clearTable("users");
     }
 
     @Test
     public void shouldCreateTransactionIfUserExistAndCreditUserHaveMoney() {
         User creditUser = new User();
         creditUser.setEmail("credit@example.com");
         creditUser.setFirstName("CreditName");
         creditUser.setLastName("CreditLastName");
         creditUser.setPassword(SecurityUtil.passwordEncoder("secret"));
         creditUser.setActive(true);
 
         UserDao.save(creditUser);
 
         IncreaseBalanсeService.moneyFromCreditCard("5105105105105100", "100", creditUser.getId(), true);
 
         User debitUser = new User();
         String debitUserEmail = "debit@example.com";
         debitUser.setEmail(debitUserEmail);
         debitUser.setFirstName("DebitName");
         debitUser.setLastName("DebitLastName");
         debitUser.setPassword(SecurityUtil.passwordEncoder("secret"));
         debitUser.setActive(true);
         UserDao.save(debitUser);
 
         try {
             TransactionService.create(creditUser, debitUserEmail, "10");
         } catch (IOException e) {
             fail();
         }
 
         Transaction transaction = TransactionDao.findAllForUser(debitUser).get(0);
         assertNotNull(transaction);
         assertEquals(10.0, transaction.getSum());
         assertEquals(TransactionDao.findAllForUser(creditUser).get(1).getId(), transaction.getId());
         assertEquals(90.0, UserDao.getBalance(creditUser));
         assertEquals(10.0, UserDao.getBalance(debitUser));
     }
 
     @Test
     public void shouldCreateTransactionIfUserNotExistButCreditUserHaveMoney() {
         User creditUser = new User();
         creditUser.setEmail("credit@example.com");
         creditUser.setFirstName("CreditName");
         creditUser.setLastName("CreditLastName");
         creditUser.setPassword(SecurityUtil.passwordEncoder("secret"));
         creditUser.setActive(true);
 
         UserDao.save(creditUser);
 
         IncreaseBalanсeService.moneyFromCreditCard("5105105105105100", "100", creditUser.getId(), true);
 
        User debitUser = new User();
         String debitUserEmail = "debit@example.com";
        debitUser.setEmail(debitUserEmail);
        debitUser.setFirstName("DebitName");
        debitUser.setLastName("DebitLastName");
        debitUser.setPassword(SecurityUtil.passwordEncoder("secret"));
        debitUser.setActive(true);
        UserDao.save(debitUser);
 
         try {
             TransactionService.create(creditUser, debitUserEmail, "10");
         } catch (IOException e) {
             fail();
         }
 
         Transaction transaction = TransactionDao.findAllForUser(debitUser).get(0);
         assertNotNull(transaction);
         assertEquals(10.0, transaction.getSum());
         assertEquals(TransactionDao.findAllForUser(creditUser).get(1).getId(), transaction.getId());
         assertEquals(90.0, UserDao.getBalance(creditUser));
         assertEquals(10.0, UserDao.getBalance(debitUser));
     }
 }
