 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.cdstoreserver.ws.accountprocessing;
 
 import com.cdstoreserver.dbagent.beans.AddressBean;
 import com.cdstoreserver.dbagent.beans.AddressList;
 import com.cdstoreserver.dbagent.beans.UserBean;
 import com.cdstoreserver.dbagent.beans.UserList;
 import com.cdstoreserver.dbagent.dao.AddressDao;
 import com.cdstoreserver.dbagent.dao.UserDao;
 import javax.jws.WebMethod;
 import javax.jws.WebParam;
 import javax.jws.WebService;
 
 /**
  *
  * @author Utkarsh
  */
 @WebService(serviceName = "AccountProcessingWS")
 public class AccountProcessingWS {
     
     /**
      * Web service operation
      */
     @WebMethod(operationName = "getUserInfo")
     public UserBean getUserInfo(@WebParam(name = "userName") String userName,@WebParam(name = "password") String password) {
         //TODO write your implementation code here:
         UserBean responseObj = new UserBean();
         
         UserDao dao = new UserDao();
         
         responseObj =  dao.getUserInfo(userName, password);
         
        if(responseObj==null) {
             responseObj.status= "error";
             responseObj.errormessage = "No user data found!";
         } else {
             responseObj.status = "success";
             responseObj.errormessage = "";
         }
         
         return responseObj;       
         
     }
     
     /**
      * Web service operation
      */
     @WebMethod(operationName = "getUserAddresses")
     public AddressList getUserAddresses(@WebParam(name = "userId") Integer userId) {
         //TODO write your implementation code here:
         AddressList responseObj = new AddressList();
         
         AddressDao dao = new AddressDao();
         
         responseObj.address =  dao.getAddresses(userId);
         
         if(responseObj.address.size() == 0) {
             responseObj.status = "error";
             responseObj.errormessage = "No addresses found associated with specified user!";
         } else {
             responseObj.status = "success";
             responseObj.errormessage = "";
         }
         
         return responseObj;       
         
     }
 
     /**
      * Web service operation
      */
    /* @WebMethod(operationName = "addUserAddress")
     public AddressList addUserAddress(@WebParam(name = "address") AddressBean address) {
         
         AddressList responseObj = new AddressList();        
         AddressDao dao = new AddressDao();        
         responseObj.address =  dao.addAddress(address);
         if(responseObj.address==null){
             responseObj.status = "error";
             responseObj.errormessage = "Failed to add address!";
         } else {
             responseObj.status = "success";
             responseObj.errormessage = "";
         }
         return responseObj;       
     }*/
 
     /**
      * Web service operation
      */
     @WebMethod(operationName = "addUser")
     public UserBean addUser(@WebParam(name = "user") UserBean user) {
         //TODO write your implementation code here:
         UserBean responseObj = new UserBean();        
         UserDao dao = new UserDao();        
         responseObj=  dao.addUser(user);
         if(responseObj==null){
             responseObj.status = "error";
             responseObj.errormessage = "User already exists!";
         } else {
             responseObj.status = "success";
             responseObj.errormessage = "";
         }
         return responseObj;       
     }
 }
