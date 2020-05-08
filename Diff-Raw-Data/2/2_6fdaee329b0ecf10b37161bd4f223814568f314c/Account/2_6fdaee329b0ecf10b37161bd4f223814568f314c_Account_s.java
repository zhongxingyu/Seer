 package com.galineer.suzy.accountsim.framework;
 
 import org.joda.money.CurrencyMismatchException;
 import org.joda.money.CurrencyUnit;
 import org.joda.money.Money;
 
 public class Account {
   // control outcome of deposit/withdrawal when crossing zero balance point
   public enum Policy {
     ALLOW,  // operation succeeds
     TRUNCATE,  // operation partially succeeds, no error
     IGNORE,  // operation fails, no error
     ERROR  // operation fails with error
   }
 
   private String name;
   private Money balance;
   private Policy positivePolicy;
   private Policy negativePolicy;
 
   public Account(String name, Money initialBalance) {
     this(name, initialBalance, Policy.ALLOW, Policy.ALLOW);
   }
 
   public Account(String name, Money initialBalance, Policy positivePolicy,
                  Policy negativePolicy)
       throws NullPointerException, IllegalArgumentException {
 
     this.checkArgsNotNull(name, initialBalance, positivePolicy, negativePolicy);
     this.name = name;
     this.positivePolicy = positivePolicy;
     this.negativePolicy = negativePolicy;
 
     if (this.positivePolicy != Policy.ALLOW && initialBalance.isPositive()) {
       throw new IllegalArgumentException(
           "Specified account may not be positive but gave positive " +
           "initial balance");
     }
     if (this.negativePolicy != Policy.ALLOW && initialBalance.isNegative()) {
       throw new IllegalArgumentException(
           "Specified account may not be negative but gave negative " +
           "initial balance");
     }
     this.balance = initialBalance;
   }
 
   private void checkArgsNotNull(String name, Money initialBalance,
                             Policy positivePolicy, Policy negativePolicy)
       throws NullPointerException {
 
     if (name == null) {
       throw new NullPointerException("Account name cannot be null");
     }
     if (initialBalance == null) {
       throw new NullPointerException("Account initial balance cannot be null");
     }
     if (positivePolicy == null || negativePolicy == null) {
       throw new NullPointerException("Policy specifications cannot be null");
     }
   }
 
   public String getName() {
     return this.name;
   }
 
   public Money getBalance() {
     return this.balance;
   }
 
   public CurrencyUnit getCurrency() {
     return this.balance.getCurrencyUnit();
   }
 
   public String toString() {
     return this.name + " (" + this.balance.toString() + ")";
   }
 
   public Money deposit(Money amount)
       throws IllegalArgumentException, CurrencyMismatchException {
 
     Money amountDeposited = this.depositDryRun(amount);
     this.balance = this.balance.plus(amountDeposited);
     return amountDeposited;
   }
 
   public Money depositDryRun(Money amount)
       throws IllegalArgumentException, CurrencyMismatchException {
 
     if (amount.isNegative()) {
       throw new IllegalArgumentException(
           "Amount to be deposited must not be negative");
     }
 
     Money newBalance = this.balance.plus(amount);
     Money amountDeposited = amount;
     if (this.balance.isNegativeOrZero() && newBalance.isPositive()) {
       amountDeposited = this.adjustDepositByPositivePolicy(amount);
     }
     return amountDeposited;
   }
 
   private Money adjustDepositByPositivePolicy(Money amount)
       throws IllegalArgumentException {
 
     switch(this.positivePolicy) {
       case ALLOW:
         return amount;
 
       case TRUNCATE:
         return this.balance.negated();
 
       case ERROR:
         throw new IllegalArgumentException(
             "Deposit would cause balance to become positive on account " +
             "that may not be positive");
 
       case IGNORE:
       default:
         return Money.zero(this.balance.getCurrencyUnit());
     }
   }
 
   public Money withdraw(Money amount)
       throws IllegalArgumentException, CurrencyMismatchException {
 
     Money amountWithdrawn = this.withdrawDryRun(amount);
     this.balance = this.balance.minus(amountWithdrawn);
     return amountWithdrawn;
   }
 
   public Money withdrawDryRun(Money amount)
       throws IllegalArgumentException, CurrencyMismatchException {
 
     if (amount.isNegative()) {
       throw new IllegalArgumentException(
           "Amount to be withdrawn must not be negative");
     }
 
     Money newBalance = this.balance.minus(amount);
     Money amountWithdrawn = amount;
     if (this.balance.isPositiveOrZero() && newBalance.isNegative()) {
       amountWithdrawn = this.adjustWithdrawalByNegativePolicy(amount);
     }
     return amountWithdrawn;
   }
 
   private Money adjustWithdrawalByNegativePolicy(Money amount)
       throws IllegalArgumentException {
 
     switch(this.negativePolicy) {
       case ALLOW:
         return amount;
 
       case TRUNCATE:
         return this.balance;
 
       case ERROR:
         throw new IllegalArgumentException(
            "Deposit would cause balance to become negative on account " +
             "that may not be negative");
 
       case IGNORE:
       default:
         return Money.zero(this.balance.getCurrencyUnit());
     }
   }
 }
