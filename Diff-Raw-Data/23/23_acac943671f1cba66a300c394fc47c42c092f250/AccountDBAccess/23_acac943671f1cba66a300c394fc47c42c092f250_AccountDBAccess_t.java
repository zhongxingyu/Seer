 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package infopharma.data;
 
 import com.mysql.jdbc.Connection;
 import com.mysql.jdbc.Statement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 /**
  *
  * @author dfernandez
  */
 public class AccountDBAccess extends DBAccess{
     
     public HashMap<Integer, String> getUserTypes(){
         HashMap<Integer, String> userTypes = new HashMap<Integer, String>();
         Connection connection = null;
         Statement statement = null;
         ResultSet resultSet = null;
         String sql = "SELECT * FROM UserRoles";
         try{
             connection = makeConnection();
             connection.setTransactionIsolation(connection.TRANSACTION_READ_COMMITTED);
             statement = (Statement) connection.createStatement();
             resultSet = statement.executeQuery(sql);
             while(resultSet.next()){
                 int roleID = resultSet.getInt("roleID");
                 String roleType = resultSet.getString("roleType");
                 userTypes.put(roleID, roleType);
             }
         }catch(SQLException ex){
             System.err.println("Error: " + ex.getMessage());
         }finally{
             try{
                 if(connection != null){
                     closeConnection(connection);
                 }
                 if(statement != null){
                     statement.close();
                 }
             }
             catch(Exception e){
                 System.err.println("Could not close the resources in AccountDBAccess getUserTypes");
             }
         }
         return userTypes;
     }
     
     public ArrayList<Integer> getDiscountPlans(){
         ArrayList<Integer> discountPlans = new ArrayList<Integer>();
         Connection connection = null;
         Statement statement = null;
         ResultSet resultSet = null;
         String sql = "SELECT * FROM DiscountPlans";
         try{
             connection = makeConnection();
             connection.setTransactionIsolation(connection.TRANSACTION_READ_COMMITTED);
             statement = (Statement) connection.createStatement();
             resultSet = statement.executeQuery(sql);
             while(resultSet.next()){
                 int discountID = resultSet.getInt("discountPlanID");
                 discountPlans.add(discountID);
             }
         }catch(SQLException ex){
             System.err.println("Error: " + ex.getMessage());
         }finally{
             try{
                 if(connection != null){
                     closeConnection(connection);
                 }
                 if(statement != null){
                     statement.close();
                 }
             }
             catch(Exception e){
                 System.err.println("Could not close the resources in AccountDBAccess getUserTypes");
             }
         }
         return discountPlans;
     }
     
     public void registerMerchantUser(String username, String password, MerchantAccount merchantAccount) throws SQLException {
         String company = merchantAccount.getCompany();
         String address = merchantAccount.getAddress();
         String postcode = merchantAccount.getPostcode();
         String telNumber = merchantAccount.getTelNumber();
         Double creditLimit = merchantAccount.getCreditLimit();
         int discountPlanID = merchantAccount.getDiscountPlan();
         String status = merchantAccount.getStatus();
         int accountNumber = 0;
         int roleID = 0;
         int statusID = 0;
         
         Connection connection = null;
         
         Statement statementAccountNumber = null;
         Statement statementRoleID = null;
         Statement statementAccountStatus = null;
         Statement statementCreateLoginDetails = null;
         Statement statementCreateAccount = null;
         
         ResultSet resultSetAccountNumber = null;
         ResultSet resultSetRoleID = null;
         ResultSet resultSetAccountStatus = null;
         
         String sqlAccountNumber = "SELECT MAX(accountNumber) AS 'accountNumber' FROM MerchantDetails";
         String sqlRoleID = "SELECT * FROM UserRoles WHERE roleType = 'merchant'";
         String sqlAccountStatus = "SELECT * FROM AccountStatuses WHERE status = '" + status + "'";
 
         try {
             connection = makeConnection();
             connection.setAutoCommit(false);
             connection.setTransactionIsolation(connection.TRANSACTION_SERIALIZABLE);
             
             statementAccountNumber = (Statement) connection.createStatement();
             resultSetAccountNumber = statementAccountNumber.executeQuery(sqlAccountNumber);
             if(resultSetAccountNumber.next()){
                 accountNumber = resultSetAccountNumber.getInt("accountNumber") + 1;
             }
             
             statementRoleID = (Statement) connection.createStatement();
             resultSetRoleID = statementRoleID.executeQuery(sqlRoleID);
             if(resultSetRoleID.next()){
                 roleID = resultSetRoleID.getInt("roleID");
             }
             
             statementAccountStatus = (Statement) connection.createStatement();
             resultSetAccountStatus = statementAccountStatus.executeQuery(sqlAccountStatus);
             if(resultSetAccountStatus.next()){
                 statusID = resultSetAccountStatus.getInt("accountStatusID");
             }
             
             String sqlCreateAccount = "INSERT INTO MerchantDetails VALUES('" + accountNumber + "','" + statusID + "','" + company + "', '" + address + "', '" + postcode + "', '" + telNumber + "', '" + creditLimit + "','0.00','" + discountPlanID + "')";
             statementCreateAccount = (Statement) connection.createStatement();
             statementCreateAccount.executeUpdate(sqlCreateAccount);
             
             String sqlCreateLoginDetails = "INSERT INTO LoginDetails VALUES('" + username + "', '" + password + "', '" + roleID + "', '" + accountNumber + "')";
             statementCreateLoginDetails = (Statement) connection.createStatement();
             statementCreateLoginDetails.executeUpdate(sqlCreateLoginDetails);
             
             connection.commit();
         }catch(SQLException ex){
             System.err.println("Error: "+ex.getMessage());
             connection.rollback();
         }finally{
             try{
                 if(connection != null){
                     closeConnection(connection);
                 }
                 if(statementAccountNumber != null){
                     statementAccountNumber.close();
                 }
                 if(statementRoleID != null){
                     statementRoleID.close();
                 }
                 if(statementCreateLoginDetails != null){
                     statementCreateLoginDetails.close();
                 }
                 if(statementCreateAccount != null){
                     statementCreateAccount.close();
                 }
             }
             catch(Exception e){
                 System.err.println("Could not close the resources in AccountDBAccess registerMerchantUser");
             }
         }
     }
     
     public void registerStaffUser(String username, String password, String role) throws SQLException{
         Connection connection = null;
         Statement statementRoleID = null;
         Statement statementCreateLoginDetails = null;
         ResultSet resultSetRoleID = null;
         //SHOULD WE HAVE AN ACCOUNT NUMBER?
         int roleID = 1;
         String userRoleIDSQL = "SELECT * FROM UserRoles WHERE roleType = '" + role + "'";
         
         try{
             connection = makeConnection();
             connection.setAutoCommit(false);
             connection.setTransactionIsolation(connection.TRANSACTION_SERIALIZABLE);
             
             statementRoleID = (Statement) connection.createStatement();
             resultSetRoleID = statementRoleID.executeQuery(userRoleIDSQL);
             if(resultSetRoleID.next()){
                 roleID = resultSetRoleID.getInt("roleID");
             }
             
             String createLoginDetailsSQL = "INSERT INTO LoginDetails VALUES('" + username + "', '" + password + "', '" + roleID + "', NULL)";
             statementCreateLoginDetails = (Statement) connection.createStatement();
             statementCreateLoginDetails.executeUpdate(createLoginDetailsSQL);
             
             connection.commit();
         }catch(SQLException ex){
             connection.rollback();
             System.err.println("Error: "+ex.getMessage());
         }finally{
             try{
                 if(connection != null){
                     closeConnection(connection);
                 }
                 if(statementRoleID != null){
                     statementRoleID.close();
                 }
                 if(statementCreateLoginDetails != null){
                     statementCreateLoginDetails.close();
                 }
             }
             catch(Exception e){
                 System.err.println("Could not close the resources in AccountDBAccess registerMerchantUser");
             }
         }
         
     }
     
     public ArrayList<MerchantAccount> getAllMerchants(){
         ArrayList<MerchantAccount> merchants = new ArrayList<MerchantAccount>();
         Connection connection = null;
         Statement statementMerchants = null;
         ResultSet resultSetMerchants = null;
         String sqlMerchants = "SELECT * FROM MerchantDetails";
         try{
             connection = makeConnection();
             connection.setTransactionIsolation(connection.TRANSACTION_READ_COMMITTED);
             statementMerchants = (Statement) connection.createStatement();
             resultSetMerchants = statementMerchants.executeQuery(sqlMerchants);
             while(resultSetMerchants.next()){
                 int accountNumber = resultSetMerchants.getInt("accountNumber");
                 int accountStatusID = resultSetMerchants.getInt("accountStatusID");
                 String company = resultSetMerchants.getString("company");
                 String address = resultSetMerchants.getString("address");
                 String postcode = resultSetMerchants.getString("postcode");
                 String phone = resultSetMerchants.getString("phone");
                 Double credit = resultSetMerchants.getDouble("credit");
                 Double balance = resultSetMerchants.getDouble("balance");
                 int discountPlanID = resultSetMerchants.getInt("discountPlanID");
                 MerchantAccount merchant = 
                         new MerchantAccount(accountNumber, 
                                             accountStatusID,
                                             company,
                                             address,
                                             postcode,
                                             phone,
                                             credit,
                                             balance,
                                             discountPlanID);
                 merchants.add(merchant);
             }
         }catch(SQLException ex){
             System.err.println("Error: "+ex.getMessage());
         }finally{
             try{
                 if(connection != null){
                     connection.close();
                 }
                 if(statementMerchants != null){
                     statementMerchants.close();
                 }
             }catch(Exception e){
                 System.err.println("Could not close the resources in AccountDBAccess getAllMerchants");
             }
         }
         return merchants;
     }
     
     public HashMap<Integer, String> getAccountStatuses(){
         HashMap<Integer, String> statuses = new HashMap<Integer, String>();
         Connection connection = null;
         Statement statementStatuses = null;
         ResultSet resultSetStatuses = null;
         String sqlStatuses = "SELECT * FROM AccountStatuses";
         try{
             connection = makeConnection();
             connection.setTransactionIsolation(connection.TRANSACTION_READ_COMMITTED);
             statementStatuses = (Statement) connection.createStatement();
             resultSetStatuses = statementStatuses.executeQuery(sqlStatuses);
             while(resultSetStatuses.next()){
                 int statusID = resultSetStatuses.getInt("accountStatusID");
                 String status = resultSetStatuses.getString("status");
                 statuses.put(statusID, status);
             }
         }catch(SQLException ex){
             System.err.println("Error: "+ex.getMessage());
         }finally{
             try{
                 if(connection != null){
                     connection.close();
                 }
                 if(statementStatuses != null){
                     statementStatuses.close();
                 }
             }catch(Exception e){
                 System.err.println("Could not close the resources in AccountDBAccess getAccountStatuses");
             }
         }
         return statuses;
     }
     
     public boolean updateMerchantStatus(MerchantAccount merchant, int statusID) {
         boolean successful = false;
         int accountNumber = merchant.getAccountNumber();
         Connection connection = null;
         Statement statementUpdate = null;
         String sqlUpdate = "UPDATE MerchantDetails SET accountStatusID='" + statusID + "' WHERE accountNumber = '" + accountNumber + "'";
         try {
             connection = makeConnection();
             connection.setAutoCommit(false);
             connection.setTransactionIsolation(connection.TRANSACTION_SERIALIZABLE);
             statementUpdate = (Statement) connection.createStatement();
             statementUpdate.executeUpdate(sqlUpdate);
             connection.commit();
             successful = true;
         }catch(SQLException ex) {
             try { 
                 connection.rollback();
             } catch (Exception e) {
                 System.err.println("Error: " + e.getMessage());
             }
             System.err.println("Error: " + ex.getMessage());
         }finally {
             try {
                 if(connection != null) {
                     connection.close();
                 }
                 if(statementUpdate != null) {
                     statementUpdate.close();
                 }
             }catch(Exception e) {
                 System.err.println("Could not close the resources in AccountDBAccess updateMerchantStatus");
             }
         }
         return successful;
     }
     
         public MerchantAccount getMerchantByOrder(int orderId) {
         MerchantAccount merchant = null;
         
         Connection connection = null;
         Statement statementMerchantNo = null;
         ResultSet resultSetMerchantNo = null;
         Statement statementMerchantAcc = null;
         ResultSet resultSetMerchantAcc = null;
         String sqlMerchantNo = "SELECT accountNumber FROM OrderDetails WHERE orderDetailsID = '" + orderId + "'";
         try {
             connection = makeConnection();
             connection.setTransactionIsolation(connection.TRANSACTION_READ_COMMITTED);
             statementMerchantNo = (Statement) connection.createStatement();
             resultSetMerchantNo = statementMerchantNo.executeQuery(sqlMerchantNo);
             if(resultSetMerchantNo.next()) {
                 int accountNumber = resultSetMerchantNo.getInt("accountNumber");
                 String sqlMerchantAcc = "SELECT * FROM MerchantDetails WHERE accountNumber = '" + accountNumber + "'";
                 statementMerchantAcc = (Statement) connection.createStatement();
                 resultSetMerchantAcc = statementMerchantAcc.executeQuery(sqlMerchantAcc);
                 if(resultSetMerchantAcc.next()) {
                     int accountStatusID = resultSetMerchantAcc.getInt("accountStatusID");
                     String company = resultSetMerchantAcc.getString("company");
                     String address = resultSetMerchantAcc.getString("address");
                     String postcode = resultSetMerchantAcc.getString("postcode");
                     String phone = resultSetMerchantAcc.getString("phone");
                     Double credit = resultSetMerchantAcc.getDouble("credit");
                     Double balance = resultSetMerchantAcc.getDouble("balance");
                     int discountPlanID = resultSetMerchantAcc.getInt("discountPlanID");
                     merchant = 
                             new MerchantAccount(accountNumber, 
                                                 accountStatusID,
                                                 company,
                                                 address,
                                                 postcode,
                                                 phone,
                                                 credit,
                                                 balance,
                                                 discountPlanID);
                 }
             }
             
         } catch(SQLException ex) {
             System.err.println("Error: " + ex.getMessage());
         } finally {
             try {
                 if(connection != null) {
                     connection.close();
                 }
                 if(statementMerchantNo != null) {
                     statementMerchantNo.close();
                 }
                 
                 if(statementMerchantAcc != null) {
                     statementMerchantAcc.close();
                 }
             } catch(Exception e) {
                 System.err.println("Could not close the resources in AccountDBAccess getMerchantByOrder");
             }
         }
         return merchant;
     }
        
    
     
 }
