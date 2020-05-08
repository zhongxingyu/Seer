 package com.financial.pyramid.service.impl;
 
 import com.financial.pyramid.service.PerfectMoneyService;
 import com.financial.pyramid.service.beans.PerfectMoneyDetails;
 import com.financial.pyramid.service.beans.PerfectMoneyHistoryFilter;
 import com.financial.pyramid.utils.HTTPClient;
 import org.springframework.stereotype.Service;
 
 import java.util.List;
 
 /**
  * User: dbudunov
  * Date: 06.08.13
  * Time: 20:44
  */
 @Service("perfectMoneyService")
 public class PerfectMoneyServiceImpl implements PerfectMoneyService {
 
     protected static final String DEFAULT_CHARSET = "UTF-8";
 
     protected static String SPEND_MONEY_URL = "https://perfectmoney.is/acct/confirm.asp";
     protected static String SPEND_MONEY_VERIFY_URL = "https://perfectmoney.is/acct/verify.asp";
     protected static String CODE_CONFIRMATION_URL = "https://perfectmoney.is/acct/protection.asp";
     protected static String VERIFY_ACCOUNT_URL = "https://perfectmoney.is/acct/acc_name.asp";
     protected static String HISTORY_URL = "https://perfectmoney.is/acct/historycsv.asp";
     protected static String BALANCE_URL = "https://perfectmoney.is/acct/balance.asp";
     protected static String RATES_URL = "http://perfectmoney.is/acct/rates.asp";
 
     @Override
     public String transferMoney(PerfectMoneyDetails perfectMoneyDetails) {
         return null;
     }
 
     @Override
     public String transferMoneyWithVerification(PerfectMoneyDetails perfectMoneyDetails) {
         return null;
     }
 
     @Override
     public String transferMoneyWithProtectionCode(PerfectMoneyDetails perfectMoneyDetails) {
         return null;
     }
 
     @Override
     public String verifyAccount(String accountId, String password, String account) {
         String url = VERIFY_ACCOUNT_URL + "?" + "AccountID={0}&PassPhrase={1}&Account={2}";
         List<String> response = HTTPClient.sendRequest(url, accountId, password, account);
         return processPerfectMoneyResponse(response);
     }
 
     @Override
     public String getTransferHistory(PerfectMoneyHistoryFilter perfectMoneyHistoryFilter) {
         return null;
     }
 
     @Override
     public String getAccountBalance(String accountId, String password) {
        String url = BALANCE_URL+"?"+"AccountID={0}&PassPhrase={1}";
         List<String> response = HTTPClient.sendRequest(url, accountId, password);
         return processPerfectMoneyResponse(response);
     }
 
     @Override
     public String getExchangeRates(String currency) {
         return null;
     }
 
    private String processPerfectMoneyResponse(List<String> response){
         StringBuilder result = new StringBuilder();
        for (String str : response){
            result.append(str);
         }
         return result.toString();
     }
 }
