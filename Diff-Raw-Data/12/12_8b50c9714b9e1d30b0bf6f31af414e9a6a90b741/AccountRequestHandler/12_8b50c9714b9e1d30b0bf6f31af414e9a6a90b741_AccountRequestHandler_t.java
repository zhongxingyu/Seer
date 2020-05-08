 package handlers;
 
 import assistant.Constants;
 import assistant.SharedHandler;
 import assistant.pair.NullableExtendedParam;
 import com.thetransactioncompany.jsonrpc2.JSONRPC2Error;
 import models.Account;
 
 import javax.persistence.NoResultException;
 import java.util.Map;
 
 /**
  * Created with IntelliJ IDEA.
  * User: OMMatte
  * Date: 2013-03-13
  * Time: 11:57
  */
 public class AccountRequestHandler extends SharedHandler {
 
     public AccountRequestHandler() {
         super();
     }
 
     @Override
     public String[] handledRequests() {
         return new String[]{
                 Constants.Method.LOGIN,
                 Constants.Method.GET_ACCOUNT,
                 Constants.Method.UPDATE_ACCOUNT,
         };
     }
 
     @Override
     public void process(Map<String, Object> jsonrpc2Params) throws Exception {
         if(method.equals(Constants.Method.LOGIN)){
             login(jsonrpc2Params);
         }
         else if(method.equals(Constants.Method.GET_ACCOUNT)){
             getAccount(jsonrpc2Params);
         }
         else if(method.equals(Constants.Method.UPDATE_ACCOUNT)){
             updateAccount(jsonrpc2Params);
         }
         else{
             throwJSONRPC2Error(JSONRPC2Error.METHOD_NOT_FOUND, Constants.Errors.METHOD_NOT_FOUND, method);
         }
     }
 
     private void updateAccount(Map<String, Object> jsonrpc2Params) throws Exception {
         Map<String, Object> updateAccountParams = getParams(jsonrpc2Params,
                 new NullableExtendedParam(Constants.Param.Name.DESCRIPTION, true),
                 new NullableExtendedParam(Constants.Param.Name.SHOW_BIRTH_DATE, true),
                 new NullableExtendedParam(Constants.Param.Name.AVATAR, true));
 
         String description = (String) updateAccountParams.get(Constants.Param.Name.DESCRIPTION);
         Boolean showBirthDate = (Boolean) updateAccountParams.get(Constants.Param.Name.SHOW_BIRTH_DATE);
         //TODO: handle Avatar
 
         Account account = em.find(Account.class, accountId);
         if(description != null){
            account.setProfileDescription(description);
         }
         if(showBirthDate != null){
             account.setShowBirthDate(showBirthDate);
         }
 
         responseParams.put(Constants.Param.Status.STATUS, Constants.Param.Status.SUCCESS);
         responseParams.put(Constants.Param.Name.ACCOUNT, account);
     }
 
     private void getAccount(Map<String, Object> jsonrpc2Params) throws Exception{
         Map<String, Object> getAccountParams = getParams(jsonrpc2Params, Constants.Param.Name.ACCOUNT_ID);
 
         int accountId = (Integer) getAccountParams.get(Constants.Param.Name.ACCOUNT_ID);
         Account account = em.find(Account.class, accountId);
         if(account != null){
             // Account exist, send it back to the client
             responseParams.put(Constants.Param.Status.STATUS, Constants.Param.Status.SUCCESS);
             responseParams.put(Constants.Param.Name.ACCOUNT, account);
         }else{
             // Account does not exist, send back "Account not found"
             responseParams.put(Constants.Param.Status.STATUS, Constants.Param.Status.ACCOUNT_NOT_FOUND);
         }
     }
 
     /**
      * Only accepts login via facebook for now.
      */
     private void login(Map<String, Object> jsonrpc2Params) throws Exception {
         Map<String, Object> loginParams = getParams(jsonrpc2Params, new NullableExtendedParam(Constants.Param.Name.TOKEN, true));
         //TODO: Should not accept token to be null. Also should use token to check the users credentials
 
         int facebookId = accountId;
         String query = "SELECT u FROM Account u WHERE u.facebookId = :" + Constants.Query.FACEBOOK_ID;
         Account account;
         try{
             account = em.createQuery( query, Account.class).setParameter(Constants.Query.FACEBOOK_ID, facebookId).getSingleResult();
             // User found by facebook Id
             responseParams.put(Constants.Param.Status.STATUS, Constants.Param.Status.SUCCESS);
         }catch(NoResultException e){
             // User not found by facebook Id. Create user.
             account = new Account(null, facebookId, null, null, Constants.Database.NO_LIMIT_PER_DAY, Constants.Database.DEFAULT_USE_DEFAULT_COLORS , Constants.Database.DEFAULT_SHOW_BIRTH_DATE);
             persistObjects(account);
             responseParams.put(Constants.Param.Status.STATUS, Constants.Param.Status.FIRST_LOGIN);
         }
         responseParams.put(Constants.Param.Name.ACCOUNT, serialize(account));
     }
 }
