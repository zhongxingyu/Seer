 package com.neopets.services.bank.model.handlers;
 
 import com.neopets.services.bank.model.WithdrawalLimitException;
 import com.neopets.handlers.ExceptionHandler;
 
 public class WithdrawalLimitExceptionHandler implements ExceptionHandler<WithdrawalLimitException> {
 
   @Override
   public void handle(String errorMessage) throws WithdrawalLimitException {
    if (errorMessage == null && errorMessage.contains("you have already attempted to withdraw")) {
       throw new WithdrawalLimitException(errorMessage);
     }
   }
 
 }
