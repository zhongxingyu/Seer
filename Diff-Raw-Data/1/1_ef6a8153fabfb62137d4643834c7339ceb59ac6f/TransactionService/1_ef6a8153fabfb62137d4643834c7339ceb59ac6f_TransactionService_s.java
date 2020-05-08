 package com.in6k.mypal.service;
 
 import com.in6k.mypal.dao.TransactionDao;
 import com.in6k.mypal.dao.UserDao;
 import com.in6k.mypal.domain.Transaction;
 import com.in6k.mypal.domain.User;
 
 import java.io.IOException;
 
 public class TransactionService {
     private static final String VALID_EMAIL_REGEXP = "^[A-Za-z0-9](([_\\.\\-]?[a-zA-Z0-9]+)*)@([A-Za-z0-9]+)(([\\.\\-]?[a-zA-Z0-9]+)*)\\.([A-Za-z]{2,})$";
 
     public static boolean create(User creditUser, String debitUserEmail, String inputSum) throws IOException {
         User debitUser = UserDao.getByEmail(debitUserEmail);
         double sum = validateSum(creditUser, inputSum);
 
         Transaction transaction = new Transaction();
 
         if (sum != 0 && isEmailValid(debitUserEmail)) {
             if (debitUser == null) {
                 InviteService.sendEmail(creditUser.getFirstName(), debitUserEmail, sum);
 
                 User newUser = new User();
                 newUser.setEmail(debitUserEmail);
                 newUser.setFirstName("inactive");
                 newUser.setLastName("inactive");
                 newUser.setPassword("inactive");
                 newUser.setActive(false);
 
                 UserDao.save(newUser);
                 transaction.setDebit(newUser);
             } else {
                 transaction.setDebit(debitUser);
             }
             transaction.setCredit(creditUser);
             transaction.setSum(sum);
 
             TransactionDao.create(transaction);
             return true;
         }
         return false;
     }
 
     private static double validateSum(User user, String inputSum) {
         double sum;
         try {
             sum = Double.parseDouble(inputSum);
         }
         catch (NumberFormatException exception) {
             return 0;
         }
         if (sum > UserDao.getBalance(user)) {
             return 0;
         }
         return sum;
     }
 
     private static boolean isEmailValid(String email) {
         return email.matches(VALID_EMAIL_REGEXP);
     }
 }
