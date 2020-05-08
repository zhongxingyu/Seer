 package se.liu.tdp024.logic.bean;
 
 import com.google.gson.*;
 import java.util.*;
 import se.liu.tdp024.entity.Account;
 import se.liu.tdp024.facade.AccountFacade;
 import se.liu.tdp024.logic.util.HTTPHelper;
 import se.liu.tdp024.util.Monlog;
 
 public abstract class AccountBean {
     public static final Monlog logger = Monlog.getLogger();
 
     private static String PersonAPI_URL = "http://enterprise-systems.appspot.com/person/";
     private static String BankAPI_URL =   "http://enterprise-systems.appspot.com/bank/";
 
     private static boolean personExists(String personKey) {
         String resp = HTTPHelper.get(PersonAPI_URL + "find.key", "key", personKey);
         JsonParser jp = new JsonParser();
        return jp.parse(resp).isJsonObject();
     }
 
     private static boolean bankExists(String bankKey) {
         String resp = HTTPHelper.get(BankAPI_URL + "find.key", "key", bankKey);
         JsonParser jp = new JsonParser();
        return jp.parse(resp).isJsonObject();
     }
 
     public static Account create(int accountType,
                               String personKey,
                               String bankKey) {
         if (!personExists(personKey)) {
             logger.log(Monlog.Severity.WARNING,
                     "Tried to create account without PersonKey",
                     "AccountType: "+ String.valueOf(accountType) +
                     "\nPersonKey: "+ personKey +
                     "\nBankKey: " + bankKey);
             return null;
         }
         if (!bankExists(bankKey)) {
             //log
             return null;
         }
         return AccountFacade.create(accountType, personKey, bankKey);
     }
 
     public static Account getAccount(long accountNumber) {
         return AccountFacade.find(accountNumber);
     }
 
     public static List<Account> findByPersonKey(String personKey) {
         return AccountFacade.findByPersonKey(personKey);
     }
 
     public static List<Account> findByBankKey(String bankKey) {
         return AccountFacade.findByBankKey(bankKey);
     }
 
     public static long balance(long accountNumber) {
         return AccountFacade.balance(accountNumber);
     }
 
     public static boolean transfer(long sender, long reciever, long amount) {
         return AccountFacade.transfer(sender, reciever, amount);
     }
 
     public static boolean depositCash(long account, long amount) {
         return AccountFacade.depositCash(account, amount);
     }
 
     public static boolean withdrawCash(long account, long amount) {
         return AccountFacade.withdrawCash(account, amount);
     }
    
 }
