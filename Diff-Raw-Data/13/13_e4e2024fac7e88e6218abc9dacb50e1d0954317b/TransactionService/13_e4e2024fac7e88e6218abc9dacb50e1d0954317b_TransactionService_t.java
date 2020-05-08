 package com.in6k.mypal.service;
 
 import com.in6k.mypal.dao.TransactionDao;
 import com.in6k.mypal.dao.UserDao;
 import com.in6k.mypal.domain.Transaction;
 import com.in6k.mypal.domain.User;
 
 import java.io.IOException;
 
 public class TransactionService {
    public static void create(User creditUser, String debitUserEmail, String inputSum) throws IOException {
        User debitUser = UserDao.getByEmail(debitUserEmail);
         double sum = validateSum(creditUser, inputSum);
         if (sum != 0) {
             if (debitUser == null) {
                InviteService.sendEmail(creditUser.getFirstName(), debitUserEmail, sum);
             } else {
                 Transaction transaction = new Transaction();
                 transaction.setDebit(debitUser);
                 transaction.setCredit(creditUser);
                 transaction.setSum(sum);
 
                 TransactionDao.create(transaction);
             }
         }
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
 }
